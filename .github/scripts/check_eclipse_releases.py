#!/usr/bin/env python3
"""
Script to check for new Eclipse platform releases and update target files and workflows.
Eclipse releases quarterly: March (03), June (06), September (09), December (12).
"""

import os
import re
import sys
from datetime import datetime
from pathlib import Path
import urllib.request


def get_eclipse_release_versions():
    """
    Generate list of Eclipse release versions based on quarterly release schedule.
    Returns versions from 2024-06 onwards up to 2 years in the future.
    """
    start_year = 2024
    start_month = 6  # Start from 2024-06
    
    # Calculate end date (current year + 2 years)
    current_year = datetime.now().year
    end_year = current_year + 2
    
    versions = []
    months = [3, 6, 9, 12]  # March, June, September, December
    
    for year in range(start_year, end_year + 1):
        for month in months:
            # Skip versions before 2024-06
            if year == start_year and month < start_month:
                continue
            version = f"{year}-{month:02d}"
            versions.append(version)
    
    return versions


def check_eclipse_release_exists(version):
    """
    Check if an Eclipse release exists by trying to access the repository URL.
    Uses the Aliyun mirror as it's used in the existing target files.
    """
    url = f"https://mirrors.aliyun.com/eclipse/releases/{version}/"
    try:
        req = urllib.request.Request(url, method='HEAD')
        with urllib.request.urlopen(req, timeout=10) as response:
            return response.status == 200
    except Exception as e:
        print(f"  Release {version} not available: {e}")
        return False


def get_existing_targets():
    """Get list of existing target versions from the targets directory."""
    targets_dir = Path("targets")
    if not targets_dir.exists():
        return []
    
    target_files = list(targets_dir.glob("*.target"))
    versions = []
    
    for target_file in target_files:
        # Extract version from filename (e.g., 2024-06.target -> 2024-06)
        match = re.match(r'(\d{4}-\d{2})\.target', target_file.name)
        if match:
            versions.append(match.group(1))
    
    return sorted(versions)


def create_target_file(version):
    """Create a new target file for the given Eclipse version."""
    target_content = f"""<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<?pde version="3.8"?>
<target includeMode="feature" name="target-platform">
    <locations>
        <location includeAllPlatforms="false" includeConfigurePhase="true" includeMode="planner" includeSource="true" type="InstallableUnit">
            <repository location="https://mirrors.aliyun.com/eclipse/releases/{version}/"/>
            <unit id="org.eclipse.platform.feature.group" version="0.0.0"/>
            <unit id="org.eclipse.jdt.feature.group" version="0.0.0"/>
        </location>
    </locations>
</target>
"""
    
    target_file = Path(f"targets/{version}.target")
    target_file.write_text(target_content)
    print(f"✓ Created target file: {target_file}")
    return target_file


def update_compatibility_workflow(new_version):
    """Update the compatibility.yml workflow to include the new target version."""
    workflow_file = Path(".github/workflows/compatibility.yml")
    
    if not workflow_file.exists():
        print(f"✗ Workflow file not found: {workflow_file}")
        return False
    
    content = workflow_file.read_text()
    
    # Find the matrix target section
    matrix_pattern = r'(matrix:\s*\n\s*target:\s*\n)((?:\s*-\s+\d{4}-\d{2}\s*\n)+)'
    match = re.search(matrix_pattern, content)
    
    if not match:
        error_msg = (
            "Could not find matrix target section in compatibility.yml. "
            "The file may have been reformatted or restructured. "
            "Please check the workflow file manually."
        )
        print(f"✗ {error_msg}")
        return False
    
    prefix = match.group(1)
    targets_section = match.group(2)
    
    # Extract existing targets
    existing_targets = re.findall(r'-\s+(\d{4}-\d{2})', targets_section)
    
    # Check if new version already exists
    if new_version in existing_targets:
        print(f"  Version {new_version} already exists in compatibility.yml")
        return False
    
    # Add new version in sorted order
    all_targets = sorted(existing_targets + [new_version])
    new_targets_section = ''.join([f'          - {target}\n' for target in all_targets])
    
    # Replace the matrix target section
    new_content = content[:match.start()] + prefix + new_targets_section + content[match.end():]
    
    workflow_file.write_text(new_content)
    print(f"✓ Updated compatibility.yml with version {new_version}")
    return True


def main():
    """Main function to check for new Eclipse releases and update files."""
    print("Checking for new Eclipse platform releases...")
    print()
    
    # Get existing targets
    existing_versions = get_existing_targets()
    print(f"Existing target versions: {', '.join(existing_versions)}")
    print()
    
    # Get potential Eclipse versions
    potential_versions = get_eclipse_release_versions()
    
    # Find new versions that don't have target files yet
    new_versions = [v for v in potential_versions if v not in existing_versions]
    
    if not new_versions:
        print("✓ All known Eclipse releases already have target files.")
        # Set output for GitHub Actions
        if 'GITHUB_OUTPUT' in os.environ:
            with open(os.environ['GITHUB_OUTPUT'], 'a') as f:
                f.write("changes=false\n")
        else:
            print("Note: GITHUB_OUTPUT not set (local testing)")
        return 0
    
    print(f"Checking {len(new_versions)} potential new releases...")
    print()
    
    # Check which new versions actually exist
    available_new_versions = []
    for version in new_versions:
        print(f"Checking Eclipse {version}...")
        if check_eclipse_release_exists(version):
            print(f"  ✓ Eclipse {version} is available!")
            available_new_versions.append(version)
        else:
            print(f"  ✗ Eclipse {version} is not available yet")
    
    print()
    
    if not available_new_versions:
        print("✓ No new Eclipse releases found.")
        # Set output for GitHub Actions
        if 'GITHUB_OUTPUT' in os.environ:
            with open(os.environ['GITHUB_OUTPUT'], 'a') as f:
                f.write("changes=false\n")
        else:
            print("Note: GITHUB_OUTPUT not set (local testing)")
        return 0
    
    # Process the newest available version
    # (only add one at a time to make PRs manageable)
    new_version = sorted(available_new_versions)[-1]  # Explicitly sort and take the newest
    
    print(f"Adding support for Eclipse {new_version}...")
    print()
    
    # Create target file
    try:
        create_target_file(new_version)
    except Exception as e:
        print(f"✗ Failed to create target file: {e}")
        return 1
    
    # Update compatibility workflow
    try:
        update_compatibility_workflow(new_version)
    except Exception as e:
        print(f"✗ Failed to update compatibility.yml: {e}")
        return 1
    
    print()
    print(f"✓ Successfully added support for Eclipse {new_version}")
    print()
    print("Changes:")
    print(f"  - Created targets/{new_version}.target")
    print(f"  - Updated .github/workflows/compatibility.yml")
    
    # Set output for GitHub Actions
    if 'GITHUB_OUTPUT' in os.environ:
        with open(os.environ['GITHUB_OUTPUT'], 'a') as f:
            f.write("changes=true\n")
            f.write(f"new_version={new_version}\n")
    else:
        print()
        print("Note: GITHUB_OUTPUT not set (local testing)")
    
    return 0


if __name__ == "__main__":
    sys.exit(main())

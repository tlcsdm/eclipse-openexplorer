# Eclipse Platform Auto-Update Workflow

This directory contains scripts and workflows for automatically checking and updating Eclipse platform target configurations.

## Overview

The Eclipse IDE releases new versions quarterly (March, June, September, December). This workflow automates the process of:
1. Detecting new Eclipse platform releases
2. Creating corresponding target files
3. Updating the compatibility workflow
4. Creating a pull request with the changes

## Files

### `check-eclipse-releases.yml`
GitHub Actions workflow that runs monthly on the 1st day of each month. It can also be triggered manually via workflow_dispatch.

**Schedule:** 
- Automatic: Monthly on the 1st at 00:00 UTC
- Manual: Via GitHub Actions UI

**What it does:**
1. Runs the Python script to check for new Eclipse releases
2. If a new release is found, creates a PR with:
   - New target file in `targets/` directory
   - Updated `.github/workflows/compatibility.yml` with the new target in the matrix

### `check_eclipse_releases.py`
Python script that:
1. Scans for potential Eclipse release versions (quarterly releases)
2. Checks if each release exists by verifying the repository URL
3. Compares against existing target files
4. Creates new target files for newly available releases
5. Updates the compatibility workflow matrix

**Features:**
- Only processes one new version at a time to keep PRs manageable
- Uses the Aliyun mirror (same as existing target files)
- Validates release availability before creating files
- Automatically sorts versions in the compatibility matrix

## Manual Testing

You can test the script locally:

```bash
cd /path/to/eclipse-openexplorer
python3 .github/scripts/check_eclipse_releases.py
```

This will:
- Check for new Eclipse releases
- Create any missing target files
- Update the compatibility.yml if needed
- Print a summary of changes

## Workflow Behavior

### When a new Eclipse release is detected:
1. A new branch is created: `auto/eclipse-YYYY-MM`
2. Changes are committed to this branch
3. A pull request is opened with:
   - Title: `feat(target-platform): add support for Eclipse YYYY-MM`
   - Labels: `automated`, `target-platform`
   - Description with changes summary

### When no new releases are found:
- Workflow completes successfully
- No PRs are created
- No changes are made

## Target File Format

Each target file follows this structure:
```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<?pde version="3.8"?>
<target includeMode="feature" name="target-platform">
    <locations>
        <location includeAllPlatforms="false" includeConfigurePhase="true" includeMode="planner" includeSource="true" type="InstallableUnit">
            <repository location="https://mirrors.aliyun.com/eclipse/releases/YYYY-MM/"/>
            <unit id="org.eclipse.platform.feature.group" version="0.0.0"/>
            <unit id="org.eclipse.jdt.feature.group" version="0.0.0"/>
        </location>
    </locations>
</target>
```

## Troubleshooting

### Workflow fails to detect a known release
- Check if the Aliyun mirror has synchronized the release
- Verify the release URL: `https://mirrors.aliyun.com/eclipse/releases/YYYY-MM/`
- Consider running the workflow manually after a few days

### PR is not created
- Check workflow logs for errors
- Ensure GITHUB_TOKEN has proper permissions (contents: write, pull-requests: write)
- Verify the script exits with changes=true in GITHUB_OUTPUT

### Script creates incorrect versions
- Review the `get_eclipse_release_versions()` function
- Ensure the start date and release schedule are correct
- Check if Eclipse has changed their release cadence

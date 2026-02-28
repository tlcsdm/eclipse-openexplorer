package com.tlcsdm.eclipse.openexplorer.actions;

/**
 * Copyright (c) 2011 Samson Wu
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 */

import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.tlcsdm.eclipse.openexplorer.Activator;
import com.tlcsdm.eclipse.openexplorer.util.Messages;
import com.tlcsdm.eclipse.openexplorer.util.OperatingSystem;

/**
 * @author <a href="mailto:samson959@gmail.com">Samson Wu</a>
 * @version 1.4.0
 */
public abstract class AbstractOpenExplorerAction extends AbstractHandler
		implements IActionDelegate, IPropertyChangeListener {

	private static final ILog LOG = Platform.getLog(AbstractOpenExplorerAction.class);

	protected IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	protected Shell shell;
	protected ISelection currentSelection;

	protected String systemBrowser;

	protected AbstractOpenExplorerAction() {
		this.systemBrowser = OperatingSystem.INSTANCE.getSystemBrowser();
		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (OperatingSystem.INSTANCE.isLinux()) {
			this.systemBrowser = OperatingSystem.INSTANCE.getSystemBrowser();
		}
	}

	@Override
	public void dispose() {
		Activator activator = Activator.getDefault();
		if (activator != null) {
			activator.getPreferenceStore().removePropertyChangeListener(this);
		}
		super.dispose();
	}

	@Override
	public void run(IAction action) {
		if (this.currentSelection == null || this.currentSelection.isEmpty()) {
			return;
		}
		if (this.currentSelection instanceof ITreeSelection treeSelection) {
			for (TreePath path : treeSelection.getPaths()) {
				IResource resource = resolveResource(path.getLastSegment());
				if (resource == null) {
					continue;
				}
				openResource(resource);
			}
		} else if (this.currentSelection instanceof ITextSelection
				|| this.currentSelection instanceof IStructuredSelection) {
			IEditorPart editor = window.getActivePage().getActiveEditor();
			if (editor != null) {
				IFile currentFile = editor.getEditorInput().getAdapter(IFile.class);
				if (currentFile != null) {
					openResource(currentFile);
				}
			}
		}
	}

	protected void openInBrowser(String browser, String location) {
		try {
			String[] args = browser.split(" ");
			String[] fullArgs = new String[args.length + 1];
			System.arraycopy(args, 0, fullArgs, 0, args.length);
			fullArgs[args.length] = location;
			new ProcessBuilder(fullArgs).start();
		} catch (IOException e) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.OpenExploer_Error,
					Messages.Cant_Open + " \"" + location + "\"");
			LOG.error("Failed to open file browser for: " + location, e);
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.currentSelection = selection;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);

		if (selection == null || selection.isEmpty()) {
			return null;
		}

		if (selection instanceof ITreeSelection treeSelection) {
			for (TreePath path : treeSelection.getPaths()) {
				IResource resource = resolveResource(path.getLastSegment());
				if (resource == null) {
					continue;
				}
				openResource(resource);
			}
		} else if (selection instanceof ITextSelection || selection instanceof IStructuredSelection) {
			IEditorPart editor = HandlerUtil.getActiveEditor(event);
			if (editor != null) {
				IFile currentFile = editor.getEditorInput().getAdapter(IFile.class);
				if (currentFile != null) {
					openResource(currentFile);
				}
			}
		}
		return null;
	}

	private IResource resolveResource(Object segment) {
		if (segment instanceof IResource resource) {
			return resource;
		} else if (segment instanceof IJavaElement javaElement) {
			return javaElement.getResource();
		}
		return null;
	}

	private void openResource(IResource resource) {
		IPath resourceLocation = resource.getLocation();
		if (resourceLocation == null) {
			return;
		}
		String browser = this.systemBrowser;
		String location = resourceLocation.toOSString();
		if (resource instanceof IFile file) {
			IPath parentLocation = file.getParent().getLocation();
			if (parentLocation == null) {
				return;
			}
			location = parentLocation.toOSString();
			if (OperatingSystem.INSTANCE.isWindows()) {
				browser = this.systemBrowser + " /select,";
				location = resourceLocation.toOSString();
			}
		}
		openInBrowser(browser, location);
	}
}

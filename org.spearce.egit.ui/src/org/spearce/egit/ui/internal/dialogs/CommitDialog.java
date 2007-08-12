/*
 *  Copyright (C) 2007 David Watson <dwatson@mimvista.com>
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License, version 2.1, as published by the Free Software Foundation.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 */

package org.spearce.egit.ui.internal.dialogs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.spearce.egit.core.project.RepositoryMapping;
import org.spearce.jgit.lib.GitIndex;
import org.spearce.jgit.lib.PersonIdent;
import org.spearce.jgit.lib.Repository;
import org.spearce.jgit.lib.Tree;
import org.spearce.jgit.lib.TreeEntry;
import org.spearce.jgit.lib.GitIndex.Entry;

/**
 * @author dwatson Dialog is shown to user when they request to commit files.
 *         Changes in the selected portion of the tree are shown.
 */
public class CommitDialog extends Dialog {

	class CommitLabelProvider extends WorkbenchLabelProvider implements
			ITableLabelProvider {
		public String getColumnText(Object obj, int columnIndex) {
			IFile file = (IFile) obj;
			if (columnIndex == 1)
				return file.getProject().getName() + ": "
						+ file.getProjectRelativePath();

			else if (columnIndex == 0) {
				String prefix = "Unknown";

				try {
					RepositoryMapping repositoryMapping = RepositoryMapping.getMapping(file.getProject());

					Repository repo = repositoryMapping.getRepository();
					GitIndex index = repo.getIndex();
					Tree headTree = repo.mapTree("HEAD");

					String repoPath = repositoryMapping
							.getRepoRelativePath(file);
					TreeEntry headEntry = headTree.findBlobMember(repoPath);
					boolean headExists = headTree.existsBlob(repoPath);

					Entry indexEntry = index.getEntry(repoPath);
					if (headEntry == null) {
						prefix = "Added";
						if (indexEntry.isModified(repositoryMapping.getWorkDir()))
							prefix = "Added, index diff";
					} else if (indexEntry == null) {
						prefix = "Removed";
					} else if (headExists
							&& !headEntry.getId().equals(
									indexEntry.getObjectId())) {
						prefix = "Modified";


						if (indexEntry.isModified(repositoryMapping.getWorkDir()))
							prefix = "Mod., index diff";
					} else if (!new File(repositoryMapping.getWorkDir(), indexEntry.getName()).isFile()) {
						prefix = "Rem., not staged";
					} else if (indexEntry.isModified(repositoryMapping.getWorkDir())) {
						prefix = "Mod., not staged";
					}

				} catch (Exception e) {
				}

				return prefix;
			}
			return null;
		}

		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex == 0)
				return getImage(element);
			return null;
		}
	}

	ArrayList<IFile> files;

	/**
	 * @param parentShell
	 */
	public CommitDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.SELECT_ALL_ID, "Select All", false);
		createButton(parent, IDialogConstants.DESELECT_ALL_ID, "Deselect All", false);

		createButton(parent, IDialogConstants.OK_ID, "Commit", true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	Text commitText;
	Text authorText;
	Button amendingButton;
	Button signedOffButton;
	
	CheckboxTableViewer filesViewer;

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		parent.getShell().setText("Commit Changes");

		GridLayout layout = new GridLayout(2, false);
		container.setLayout(layout);

		Label label = new Label(container, SWT.LEFT);
		label.setText("Commit Message:");
		label.setLayoutData(GridDataFactory.fillDefaults().span(2, 1).create());

		commitText = new Text(container, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
		commitText.setLayoutData(GridDataFactory.fillDefaults().span(2, 1)
				.hint(600, 200).create());

		// allow to commit with ctrl-enter
		commitText.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent arg0) {
				if (arg0.keyCode == SWT.CR
						&& (arg0.stateMask & SWT.CONTROL) > 0) {
					okPressed();
				} else if (arg0.keyCode == SWT.TAB
						&& (arg0.stateMask & SWT.SHIFT) == 0) {
					arg0.doit = false;
					commitText.traverse(SWT.TRAVERSE_TAB_NEXT);
				}
			}
		});

		new Label(container, SWT.LEFT).setText("Author: ");
		authorText = new Text(container, SWT.BORDER);
		authorText.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		if (author != null)
			authorText.setText(author);

		amendingButton = new Button(container, SWT.CHECK);
		if (amending) {
			amendingButton.setSelection(amending);
			amendingButton.setEnabled(false); // if already set, don't allow any changes
			commitText.setText(previousCommitMessage);
		} else if (!amendAllowed) {
			amendingButton.setEnabled(false);
		}
		amendingButton.addSelectionListener(new SelectionListener() {
			boolean alreadyAdded = false;
			public void widgetSelected(SelectionEvent arg0) {
				if (alreadyAdded)
					return;
				if (amendingButton.getSelection()) {
					alreadyAdded = true;
					String curText = commitText.getText();
					if (curText.length() > 0)
						curText += "\n";
					commitText.setText(curText + previousCommitMessage);
				}
			}
		
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
		
		amendingButton.setText("Amend previous commit");
		amendingButton.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).span(2, 1).create());

		signedOffButton = new Button(container, SWT.CHECK);
		signedOffButton.setSelection(signedOff);
		signedOffButton.setText("Add Signed-off-by");
		signedOffButton.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).span(2, 1).create());
		
		Table resourcesTable = new Table(container, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.MULTI | SWT.CHECK | SWT.BORDER);
		resourcesTable.setLayoutData(GridDataFactory.fillDefaults().hint(600,
				200).span(2,1).create());

		resourcesTable.setHeaderVisible(true);
		TableColumn statCol = new TableColumn(resourcesTable, SWT.LEFT);
		statCol.setText("Status");
		statCol.setWidth(150);

		TableColumn resourceCol = new TableColumn(resourcesTable, SWT.LEFT);
		resourceCol.setText("File");
		resourceCol.setWidth(415);

		filesViewer = new CheckboxTableViewer(resourcesTable);
		filesViewer.setContentProvider(new IStructuredContentProvider() {

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
			}

			public void dispose() {
			}

			public Object[] getElements(Object inputElement) {
				return files.toArray();
			}

		});
		filesViewer.setLabelProvider(new CommitLabelProvider());
		filesViewer.setInput(files);
		filesViewer.setAllChecked(true);
		filesViewer.getTable().setMenu(getContextMenu());

		container.pack();
		return container;
	}

	private Menu getContextMenu() {
		Menu menu = new Menu(filesViewer.getTable());
		MenuItem item = new MenuItem(menu, SWT.PUSH);
		item.setText("Add file on disk to index");
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event arg0) {
				IStructuredSelection sel = (IStructuredSelection) filesViewer.getSelection();
				if (sel.isEmpty()) {
					return;
				}
				try {
					ArrayList<GitIndex> changedIndexes = new ArrayList<GitIndex>();
					for (Iterator<Object> it = sel.iterator(); it.hasNext();) {
						IFile file = (IFile) it.next();

						IProject project = file.getProject();
						RepositoryMapping map = RepositoryMapping.getMapping(project);

						Repository repo = map.getRepository();
						GitIndex index = null;
						index = repo.getIndex();
						Entry entry = index.getEntry(map.getRepoRelativePath(file));
						if (entry != null && entry.isModified(map.getWorkDir())) {
							entry.update(new File(map.getWorkDir(), entry.getName()), repo);
							if (!changedIndexes.contains(index))
								changedIndexes.add(index);
						}
					}
					if (!changedIndexes.isEmpty()) {
						for (GitIndex idx : changedIndexes) {
							idx.write();
						}
						filesViewer.refresh(true);
					}
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		});
		
		return menu;
	}

	public String getCommitMessage() {
		return commitMessage;
	}

	public void setCommitMessage(String s) {
		this.commitMessage = s;
	}

	private String commitMessage = "";
	private String author = null;
	private boolean signedOff = false;
	private boolean amending = false;
	private boolean amendAllowed = true;

	private ArrayList<IFile> selectedItems = new ArrayList<IFile>();
	private String previousCommitMessage = "";

	public void setSelectedItems(IFile[] items) {
		Collections.addAll(selectedItems, items);
	}

	public IFile[] getSelectedItems() {
		return selectedItems.toArray(new IFile[0]);
	}

	@Override
	protected void okPressed() {
		commitMessage = commitText.getText();
		author = authorText.getText().trim();
		signedOff = signedOffButton.getSelection();
		amending = amendingButton.getSelection();
		
		Object[] checkedElements = filesViewer.getCheckedElements();
		selectedItems.clear();
		for (Object obj : checkedElements)
			selectedItems.add((IFile) obj);

		if (commitMessage.trim().length() == 0) {
			MessageDialog.openWarning(getShell(), "No message", "You must enter a commit message");
			return;
		}
		
		if (author.length() > 0) {
			try {
				new PersonIdent(author);
			} catch (IllegalArgumentException e) {
				MessageDialog.openWarning(getShell(), "Invalid author", "Invalid author specified. Please use the form:\nA U Thor <author@example.com>");
				return;
			}
		} else author = null;

		if (selectedItems.isEmpty() && !amending) {
			MessageDialog.openWarning(getShell(), "No items selected", "No items are currently selected to be committed.");
			return;
		}
		super.okPressed();
	}

	public void setFileList(ArrayList<IFile> files) {
		this.files = files;
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (IDialogConstants.SELECT_ALL_ID == buttonId) {
			filesViewer.setAllChecked(true);
		}
		if (IDialogConstants.DESELECT_ALL_ID == buttonId) {
			filesViewer.setAllChecked(false);
		}
		super.buttonPressed(buttonId);
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public boolean isSignedOff() {
		return signedOff;
	}

	public void setSignedOff(boolean signedOff) {
		this.signedOff = signedOff;
	}

	public boolean isAmending() {
		return amending;
	}

	public void setAmending(boolean amending) {
		this.amending = amending;
	}

	public void setPreviousCommitMessage(String string) {
		this.previousCommitMessage = string;
	}

	public void setAmendAllowed(boolean amendAllowed) {
		this.amendAllowed = amendAllowed;
	}
}

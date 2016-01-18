/*******************************************************************************
 *  Manik Hot Deploy
 *  Copyright (C) 2010 Ralph Soika  
 *  http://www.imixs.com
 *  
 *  This program is free software; you can redistribute it and/or 
 *  modify it under the terms of the GNU General Public License 
 *  as published by the Free Software Foundation; either version 2 
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful, 
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 *  General Public License for more details.
 *  
 *  You can receive a copy of the GNU General Public
 *  License at http://www.gnu.org/licenses/gpl.html
 *  
 *  Contributors:  
 *  	Ralph Soika 
 * 
 *******************************************************************************/

package org.imixs.eclipse.manik;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * Property Page to store the target folder from the app server
 * 
 * @author rsoika
 * 
 */
public class TargetPropertyPage extends PropertyPage {

	private static final String AUTODEPLOY_TITLE = "Autodeployment:";
	private static final String HOTDEPLOY_TITLE = "Hotdeployment:";
	public static final String AUTODEPLOY_DIR_PROPERTY = "AUTODEPLOY_TARGET";
	public static final String HOTDEPLOY_DIR_PROPERTY = "HOTDEPLOY_TARGET";
	public static final String EXTRACT_ARTIFACTS_PROPERTY = "EXTRACT_ARTIFACTS";
	public static final String WILDFLY_SUPPORT_PROPERTY = "WILDFLY_SUPPORT";
	private static final String DEFAULT_DIR = "";

	private static final int TEXT_FIELD_WIDTH = 50;

	private Text hotdeployText;
	private Text autodeployText;
	private Button checkExplodeArtifacts;
	private Button checkWildFlySupport;

	public TargetPropertyPage() {
		super();
	}

	public Shell getShell() {
		return super.getShell();

	}

	private void addTargetSection(Composite parent) {

		/*
		 * ###############################
		 * 
		 * Autodeploy Field
		 */
		Group groupSelectAutoDeploy = new Group(parent, SWT.NONE);
		groupSelectAutoDeploy.setText(AUTODEPLOY_TITLE);
		groupSelectAutoDeploy.setLayout(new GridLayout(2, false));

		Label helpLabel = new Label(groupSelectAutoDeploy, SWT.NONE);
		helpLabel.setText(
				"Select the target autodeploy directory of your\napplication server to deploy your application.");

		// add dummy label
		new Label(groupSelectAutoDeploy, SWT.NONE).setText("");

		autodeployText = new Text(groupSelectAutoDeploy, SWT.SINGLE | SWT.BORDER);
		GridData gd = new GridData();
		gd.widthHint = convertWidthInCharsToPixels(TEXT_FIELD_WIDTH);
		autodeployText.setLayoutData(gd);

		// Clicking the button will allow the user
		// to select a directory
		Button button = new Button(groupSelectAutoDeploy, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				DirectoryDialog dlg = new DirectoryDialog(getShell());

				// Set the initial filter path according
				// to anything they've selected or typed in
				dlg.setFilterPath(autodeployText.getText());

				// Change the title bar text
				dlg.setText(AUTODEPLOY_TITLE);

				// Customizable message displayed in the dialog
				dlg.setMessage("Select a directory");

				// Calling open() will open and run the dialog.
				// It will return the selected directory, or
				// null if user cancels
				String dir = dlg.open();
				if (dir != null) {
					// Set the text box to the new selection
					autodeployText.setText(dir);
				}
			}
		});

		// check box for Explode Artefacts
		checkExplodeArtifacts = new Button(groupSelectAutoDeploy, SWT.CHECK);
		checkExplodeArtifacts.setSelection(false);
		checkExplodeArtifacts.setText("Explode Artifacts (.war, .ear)");

		// add dummy label
		new Label(groupSelectAutoDeploy, SWT.NONE).setText("");

		// check box WildFly Support
		checkWildFlySupport = new Button(groupSelectAutoDeploy, SWT.CHECK);
		checkWildFlySupport.setSelection(false);
		checkWildFlySupport.setText("WildFly Support");

		/*
		 * ###############################
		 * 
		 * Hotdeploy Field
		 */
		// ownerLabel = new Label(composite, SWT.NONE);
		// ownerLabel.setText(HOTDEPLOY_TITLE);

		Group groupSelectHotDeploy = new Group(parent, SWT.NONE);
		groupSelectHotDeploy.setText(HOTDEPLOY_TITLE);

		groupSelectHotDeploy.setLayout(new GridLayout(2, false));

		helpLabel = new Label(groupSelectHotDeploy, SWT.NONE);
		helpLabel.setText("Select the target hotodeploy directory from a already\ndeployed web application. ");

		// add dummy label
		new Label(groupSelectHotDeploy, SWT.NONE).setText("");

		hotdeployText = new Text(groupSelectHotDeploy, SWT.SINGLE | SWT.BORDER);
		gd = new GridData();
		gd.widthHint = convertWidthInCharsToPixels(TEXT_FIELD_WIDTH);
		hotdeployText.setLayoutData(gd);

		// Clicking the button will allow the user
		// to select a directory
		button = new Button(groupSelectHotDeploy, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				DirectoryDialog dlg = new DirectoryDialog(getShell());

				// Set the initial filter path according
				// to anything they've selected or typed in
				dlg.setFilterPath(autodeployText.getText());

				// Change the title bar text
				dlg.setText(HOTDEPLOY_TITLE);

				// Customizable message displayed in the dialog
				dlg.setMessage("Select a directory");

				// Calling open() will open and run the dialog.
				// It will return the selected directory, or
				// null if user cancels
				String dir = dlg.open();
				if (dir != null) {
					// Set the text box to the new selection
					hotdeployText.setText(dir);
				}
			}
		});

		// Populate autodeploy text field
		try {
			String owner = ((IResource) getElement())
					.getPersistentProperty(new QualifiedName("", AUTODEPLOY_DIR_PROPERTY));
			autodeployText.setText((owner != null) ? owner : DEFAULT_DIR);
		} catch (CoreException e) {
			autodeployText.setText(DEFAULT_DIR);
		}

		// Populate autodeploy text field
		try {
			String owner = ((IResource) getElement())
					.getPersistentProperty(new QualifiedName("", HOTDEPLOY_DIR_PROPERTY));
			hotdeployText.setText((owner != null) ? owner : DEFAULT_DIR);
		} catch (CoreException e) {
			hotdeployText.setText(DEFAULT_DIR);
		}

		// Populate checkExplodeArtifacts selection
		try {
			String extract = ((IResource) getElement())
					.getPersistentProperty(new QualifiedName("", EXTRACT_ARTIFACTS_PROPERTY));

			if (extract != null && "true".equals(extract))
				checkExplodeArtifacts.setSelection(true);
			else
				checkExplodeArtifacts.setSelection(false);
		} catch (CoreException e) {
			checkExplodeArtifacts.setSelection(false);
		}

		// Populate checkWildFlySupport selection
		try {
			String extract = ((IResource) getElement())
					.getPersistentProperty(new QualifiedName("", WILDFLY_SUPPORT_PROPERTY));

			if (extract != null && "true".equals(extract))
				checkWildFlySupport.setSelection(true);
			else
				checkWildFlySupport.setSelection(false);
		} catch (CoreException e) {
			checkWildFlySupport.setSelection(false);
		}

	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		addTargetSection(composite);
		return composite;
	}


	protected void performDefaults() {
		// Populate the owner text field with the default value
		autodeployText.setText(DEFAULT_DIR);
		hotdeployText.setText(DEFAULT_DIR);
		checkExplodeArtifacts.setSelection(false);
		checkWildFlySupport.setSelection(false);
	}

	public boolean performOk() {
		// store the value in the owner text field
		try {
			String target = autodeployText.getText();

			((IResource) getElement()).setPersistentProperty(new QualifiedName("", AUTODEPLOY_DIR_PROPERTY),
					target.trim());

			target = hotdeployText.getText();
			((IResource) getElement()).setPersistentProperty(new QualifiedName("", HOTDEPLOY_DIR_PROPERTY),
					target.trim());

			// extractartefacts.
			if (checkExplodeArtifacts.getSelection() == true) {
				((IResource) getElement()).setPersistentProperty(new QualifiedName("", EXTRACT_ARTIFACTS_PROPERTY),
						"true");
			} else {
				((IResource) getElement()).setPersistentProperty(new QualifiedName("", EXTRACT_ARTIFACTS_PROPERTY),
						"false");
			}

			// Wildfly support.
			if (checkWildFlySupport.getSelection() == true) {
				((IResource) getElement()).setPersistentProperty(new QualifiedName("", WILDFLY_SUPPORT_PROPERTY),
						"true");
			} else {
				((IResource) getElement()).setPersistentProperty(new QualifiedName("", WILDFLY_SUPPORT_PROPERTY),
						"false");
			}

		} catch (CoreException e) {
			return false;
		}
		return true;
	}

}

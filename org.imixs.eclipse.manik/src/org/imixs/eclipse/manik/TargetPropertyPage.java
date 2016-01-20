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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.imixs.eclipse.manik.cfg.Configuration;

/**
 * Property Page to store the target folder from the app server
 * 
 * @author rsoika
 * 
 */
public class TargetPropertyPage extends PropertyPage {

	private static final String WFLY_PARAMS = "WildFly params:";
	private static final String DESTINATIONS = "Destination Paterns:";
//	public static final QualifiedName AUTODEPLOY_DIR_PROPERTY = new QualifiedName("", "AUTODEPLOY_TARGET");
//	public static final QualifiedName HOTDEPLOY_DIR_PROPERTY = new QualifiedName("", "HOTDEPLOY_TARGET");
//	public static final QualifiedName EXTRACT_ARTIFACTS_PROPERTY = new QualifiedName("", "EXTRACT_ARTIFACTS");
//	public static final QualifiedName WILDFLY_SUPPORT_PROPERTY = new QualifiedName("", "WILDFLY_SUPPORT");
	private static final String DEFAULT_DIR = "";

	private static final int TEXT_FIELD_WIDTH = 50;

	
	private final static int H = 4;

	
	private Text wildFlyDir;
	
	private List<Text> globs = new ArrayList<>();
	
	private Configuration c = new Configuration();
	
	public TargetPropertyPage() {
		super();
	}

	public Shell getShell() {
		return super.getShell();

	}

	@SuppressWarnings("static-access")
	private void addTargetSection(Composite parent) {
		c = this.load(((IResource) getElement()));
		
		/*
		 * ###############################
		 * 
		 * Autodeploy Field
		 */
		Group groupSelectAutoDeploy = new Group(parent, SWT.NONE);
		groupSelectAutoDeploy.setText(WFLY_PARAMS);
		groupSelectAutoDeploy.setLayout(new GridLayout(2, false));

		Label helpLabel = new Label(groupSelectAutoDeploy, SWT.NONE);
		helpLabel.setText("Select WildFly directory");

		// add dummy label
		new Label(groupSelectAutoDeploy, SWT.NONE).setText("");

		wildFlyDir = new Text(groupSelectAutoDeploy, SWT.SINGLE | SWT.BORDER);
		GridData gd = new GridData();
		gd.widthHint = convertWidthInCharsToPixels(TEXT_FIELD_WIDTH);
		wildFlyDir.setLayoutData(gd);

		// Clicking the button will allow the user
		// to select a directory
		Button button = new Button(groupSelectAutoDeploy, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				DirectoryDialog dlg = new DirectoryDialog(getShell());

				// Set the initial filter path according
				// to anything they've selected or typed in
				dlg.setFilterPath(wildFlyDir.getText());

				// Change the title bar text
				dlg.setText(WFLY_PARAMS);

				// Customizable message displayed in the dialog
				dlg.setMessage("Select a directory");

				// Calling open() will open and run the dialog.
				// It will return the selected directory, or
				// null if user cancels
				String dir = dlg.open();
				if (dir != null) {
					// Set the text box to the new selection
					wildFlyDir.setText(dir);
				}
			}
		});
		wildFlyDir.setText(c.getWildflyPath());

		getHotDeployRegex(parent);
	}

	private Group getHotDeployRegex(Composite parent){
		Group groupSelectHotDeploy = new Group(parent, SWT.NONE);
		groupSelectHotDeploy.setText(DESTINATIONS);

		groupSelectHotDeploy.setLayout(new GridLayout(2, false));

		Label helpLabel = new Label(groupSelectHotDeploy, SWT.NONE);
		helpLabel.setText("Select the target glob were files will be copied");

		// add dummy label
		new Label(groupSelectHotDeploy, SWT.NONE).setText("");

		for(int i = 0; i<H; i++) {
			hotDeployRegex(groupSelectHotDeploy, i);
		}
		return groupSelectHotDeploy;
	}
	
	private void hotDeployRegex(Group groupSelectHotDeploy, final int i){
		new Label(groupSelectHotDeploy, SWT.NONE).setText("Ant glob " + i);
		Text textField = new Text(groupSelectHotDeploy, SWT.SINGLE | SWT.BORDER);
		GridData gd = new GridData();
		gd.widthHint = convertWidthInCharsToPixels(TEXT_FIELD_WIDTH);
		textField.setLayoutData(gd);
		textField.setText(c.getGlobs().get(i));
		textField.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent evt) {
				c.getGlobs().set(i, (String) evt.data);
			}
		});
		this.globs.add(textField);
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
		c = new Configuration();
		// Populate the owner text field with the default value
		wildFlyDir.setText(c.getWildflyPath());
		for(Text t: globs) {
			t.setText(DEFAULT_DIR);
		}
	}

	public boolean performOk() {
		return save(((IResource) getElement()), c);
	}

	private static String WFLYDIR ="WFLYDIR";
	private static String GLOB ="GLOB_";
	
	
	public static Configuration load(IResource resource) {
		Configuration c = new Configuration();
		
		// read deployment directory settings....
		try {
			String s = resource.getPersistentProperty(new QualifiedName("", WFLYDIR));
			c.setWildflyPath(null!=s?s.trim():"");
			
			for(int i =0; i< H; i++) {
				s = resource.getPersistentProperty(new QualifiedName("", GLOB + i));
				c.getGlobs().add(null!=s?s.trim():"");
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return c;
	}
	
	public boolean save(IResource resource, Configuration c) {
		// store the value in the owner text field
		try {
			resource.setPersistentProperty(new QualifiedName("", WFLYDIR), wildFlyDir.getText().trim());
			for(int i =0; i< H; i++) {
				if(i<c.getGlobs().size()){
					resource.setPersistentProperty(new QualifiedName("", GLOB + i), globs.get(i).getText().trim());
				} else {
					resource.setPersistentProperty(new QualifiedName("", GLOB + i), "");
				}
			}
		} catch (CoreException e) {
			return false;
		}
		return true;
	}
}

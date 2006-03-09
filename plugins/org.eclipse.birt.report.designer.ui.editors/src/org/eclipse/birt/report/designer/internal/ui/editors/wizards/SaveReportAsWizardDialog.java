/*******************************************************************************
 * Copyright (c) 2004 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.report.designer.internal.ui.editors.wizards;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * A WizardDialog witch can return a select path.
 */

public class SaveReportAsWizardDialog extends WizardDialog
{

	private IPath saveAsPath;

	public SaveReportAsWizardDialog( Shell parentShell, IWizard newWizard )
	{
		super( parentShell, newWizard );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.WizardDialog#finishPressed()
	 */
	protected void finishPressed( )
	{
		super.finishPressed( );
		IWizardPage page = getCurrentPage( );
		IWizard wizard = page.getWizard();
		this.saveAsPath = ( (SaveReportAsWizard) wizard ).getSaveAsPath();
	}

	public IPath getResult( )
	{
		return this.saveAsPath;
	}

}

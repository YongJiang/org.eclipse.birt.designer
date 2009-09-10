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

package org.eclipse.birt.report.designer.ui.wizards;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.wizard.IWizardPage;

/**
 * INewLibraryCreationPage
 */
public interface INewLibraryCreationPage extends IWizardPage
{

	void setContainerFullPath( IPath initPath );

	void setFileName( String initFileName );

	IPath getContainerFullPath( );

	String getFileName( );

	boolean performFinish( );

	void updatePerspective( IConfigurationElement configElement );
}

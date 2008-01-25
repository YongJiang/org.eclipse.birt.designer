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

package org.eclipse.birt.report.designer.internal.ui.views.actions;

import org.eclipse.birt.report.designer.internal.ui.util.UIUtil;
import org.eclipse.birt.report.designer.internal.ui.wizards.PublishTemplateWizard;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.designer.ui.ReportPlugin;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.jface.wizard.WizardDialog;

/**
 * 
 */

public class PublishTemplateViewAction extends AbstractViewAction
{

	private static String DISPLAY_TEXT = Messages.getString( "PublishTemplateViewAction.action.displaytext" ); //$NON-NLS-1$
	private static String TEMPLATE_SUFFIX = Messages.getString( "DefaultNodeProvider.template.suffix" ); //$NON-NLS-1$

	/**
	 * @param selectedObject
	 */
	public PublishTemplateViewAction( Object selectedObject )
	{
		super( selectedObject, DISPLAY_TEXT );
	}

	/**
	 * @param selectedObject
	 * @param text
	 */
	public PublishTemplateViewAction( Object selectedObject, String text )
	{
		super( selectedObject, text );
	}

	/*
	 * (non-Javadoc) Method declared on IAction.
	 */
	public boolean isEnabled( )
	{
		Object object = getSelection( );

		if ( ( object == null ) || !( object instanceof ReportDesignHandle ) )
		{
			return false;
		}
		ReportDesignHandle report = (ReportDesignHandle) object;
		if ( report.getModuleHandle( )
				.getFileName( )
				.endsWith( TEMPLATE_SUFFIX )
				|| ReportPlugin.getDefault( )
						.isReportDesignFile( report.getModuleHandle( )
								.getFileName( ) ) )
		{
			return true;
		}
		return false;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run( )
	{

		WizardDialog dialog = new WizardDialog( UIUtil.getDefaultShell( ),
				new PublishTemplateWizard( (ReportDesignHandle) getSelection( ) ) );
		dialog.setPageSize( 500, 250 );
		dialog.open( );
	}

}

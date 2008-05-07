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

package org.eclipse.birt.report.designer.ui.ide.navigator;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.birt.report.designer.internal.ui.util.ExceptionHandler;
import org.eclipse.birt.report.designer.ui.ReportPlugin;
import org.eclipse.birt.report.viewer.utilities.WebViewer;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;

/**
 * The action to generate report document in navigator view
 */
public class GenerateDocumentAction extends AbstractViewAction
{

	public void run( IAction action )
	{
		IFile file = getSelectedFile( );
		if ( file != null )
		{
			String url = file.getLocation( ).toOSString( );
			try
			{
				Map options = new HashMap( );
				options.put( WebViewer.RESOURCE_FOLDER_KEY,
						ReportPlugin.getDefault( )
								.getResourceFolder( file.getProject( ) ) );
				options.put( WebViewer.SERVLET_NAME_KEY,
						WebViewer.VIEWER_DOCUMENT );
				WebViewer.display( url, options );
			}
			catch ( Exception e )
			{
				ExceptionHandler.handle( e );
				return;
			}
		}
		else
		{
			action.setEnabled( false );
		}
	}

}

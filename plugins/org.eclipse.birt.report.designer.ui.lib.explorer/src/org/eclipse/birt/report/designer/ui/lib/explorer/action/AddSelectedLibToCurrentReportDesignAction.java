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

package org.eclipse.birt.report.designer.ui.lib.explorer.action;

import org.eclipse.birt.report.designer.core.model.SessionHandleAdapter;
import org.eclipse.birt.report.designer.internal.ui.util.ExceptionHandler;
import org.eclipse.birt.report.designer.internal.ui.util.UIUtil;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.model.api.LibraryHandle;
import org.eclipse.birt.report.model.api.ModuleHandle;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;

/**
 * The action used to add library to a report design
 */

public class AddSelectedLibToCurrentReportDesignAction extends Action
{

	private StructuredViewer viewer;

	private static final String ACTION_TEXT = Messages.getString( "UseLibraryAction.Text" ); //$NON-NLS-1$

	public AddSelectedLibToCurrentReportDesignAction( StructuredViewer viewer )
	{
		super( ACTION_TEXT );
		this.viewer = viewer;
	}

	public boolean isEnabled( )
	{
		LibraryHandle library = getSelectedLibrary( );
		ModuleHandle moduleHandle = SessionHandleAdapter.getInstance( )
		.getReportDesignHandle( );

		if ( library != null  && moduleHandle!=null)
		{
			return !moduleHandle.isInclude( library );
		}
		return false;
	}

	public void run( )
	{
		if ( isEnabled( ) )
		{
			try
			{
				UIUtil.includeLibrary( getSelectedLibrary( ) );
			}
			catch ( Exception e )
			{
				ExceptionHandler.handle( e );
			}
		}
	}

	private LibraryHandle getSelectedLibrary( )
	{
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection( );
		if ( selection != null )
		{
			if ( selection.getFirstElement( ) instanceof LibraryHandle )
			{
				return (LibraryHandle) selection.getFirstElement( );
			}
		}
		return null;
	}

}

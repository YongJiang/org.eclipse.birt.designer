/*************************************************************************************
 * Copyright (c) 2004 Actuate Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Actuate Corporation - Initial implementation.
 ************************************************************************************/

package org.eclipse.birt.report.designer.internal.ui.views.actions;

import org.eclipse.birt.report.designer.internal.ui.util.DataSetManager;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.model.api.DataSetHandle;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * Refresh action
 */
public class RefreshAction extends AbstractViewerAction
{

	private static final String TEXT = Messages.getString( "RefreshAction.text" ); //$NON-NLS-1$

	/**
	 * Create a new refresh action with given selection and default text
	 * 
	 * @param selectedObject
	 *            the selected object,which cannot be null
	 *  
	 */
	public RefreshAction( TreeViewer sourceViewer )
	{
		this( sourceViewer, TEXT );
	}

	/**
	 * Create a new refresh action with given selection and text
	 * 
	 * @param selectedObject
	 *            the selected object,which cannot be null
	 * @param text
	 *            the text of the action
	 */
	public RefreshAction( TreeViewer sourceViewer, String text )
	{
		super( sourceViewer, text );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#isEnabled()
	 */
	public boolean isEnabled( )
	{
		Object obj = getSelectedObjects( ).getFirstElement( );
		if ( obj instanceof DataSetHandle )
		{
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run( )
	{
		if ( isEnabled( ) )
		{
			DataSetHandle handle = (DataSetHandle) getSelectedObjects( ).getFirstElement( );
			DataSetManager.getCurrentInstance( ).refresh( handle );
			getSourceViewer( ).refresh( handle );
		}
	}
}
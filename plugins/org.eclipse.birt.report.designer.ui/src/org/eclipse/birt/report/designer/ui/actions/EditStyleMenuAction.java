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

package org.eclipse.birt.report.designer.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.birt.report.designer.internal.ui.editors.schematic.actions.EditStyleAction;
import org.eclipse.birt.report.designer.util.DEUtil;
import org.eclipse.birt.report.model.api.SharedStyleHandle;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Updates "Edit style" menu
 */

public class EditStyleMenuAction extends MenuUpdateAction
{

	public static final String ID = "edit style menu"; //$NON-NLS-1$

	/**
	 * @param part
	 */
	public EditStyleMenuAction( IWorkbenchPart part )
	{
		super( part );
		setId( ID );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.ui.actions.MenuUpdateAction#getItems()
	 */
	protected List getItems( )
	{

		ArrayList actionList = new ArrayList( );
		Iterator iterator = DEUtil.getStyles(false);
		if(iterator!=null)
		{
			while ( iterator.hasNext( ) )
			{
				SharedStyleHandle handle = (SharedStyleHandle) iterator.next( );
				EditStyleAction action = new EditStyleAction( handle );
				action.setSelection( getSelection( ) );
				actionList.add( action );
			}
		}
		return actionList;
	}
}
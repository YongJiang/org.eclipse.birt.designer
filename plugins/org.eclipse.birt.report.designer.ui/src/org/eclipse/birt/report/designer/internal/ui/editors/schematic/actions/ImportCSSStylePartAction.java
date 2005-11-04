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

package org.eclipse.birt.report.designer.internal.ui.editors.schematic.actions;

import org.eclipse.birt.report.designer.internal.ui.views.actions.ImportCSSStyleAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Wrappers ImportCSSStyleAction from a viewer action to a part selection
 * action.
 */

public class ImportCSSStylePartAction extends WrapperSelectionAction
{
	public ImportCSSStylePartAction( IWorkbenchPart part )
	{
		super( part );
		setText(ImportCSSStyleAction.ACTION_TEXT);
	}

	public String getId( )
	{
		return ImportCSSStyleAction.ID;
	}

	protected IAction createActionHandler( ISelection model )
	{
		if ( actionHandler == null )
		{
			actionHandler = new ImportCSSStyleAction( model );
		}
		return actionHandler;
	}

}

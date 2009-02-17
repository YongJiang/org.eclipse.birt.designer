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

package org.eclipse.birt.report.designer.internal.ui.command;

import org.eclipse.birt.report.designer.internal.ui.util.Policy;
import org.eclipse.birt.report.designer.internal.ui.views.actions.DeleteAction;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.designer.util.DEUtil;
import org.eclipse.birt.report.designer.util.DNDUtil;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.gef.ui.actions.Clipboard;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * 
 */

public class CutHandler extends SelectionHandler
{
	private static final String DEFAULT_TEXT = Messages.getString( "CutAction.text" ); //$NON-NLS-1$

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute( ExecutionEvent event ) throws ExecutionException
	{

		super.execute( event );
		
		Object selection = getElementHandles().toArray( );
		if ( Policy.TRACING_ACTIONS )
		{
			System.out.println( "Cut action >> Cut " + selection ); //$NON-NLS-1$
		}
		Object cloneElements = DNDUtil.cloneSource( selection );
		DeleteAction action = createDeleteAction( selection );
		action.run( );
		if ( action.hasExecuted( ) )
		{
			Clipboard.getDefault( ).setContents( cloneElements );
		}
		
		return Boolean.valueOf( true );
	}
	
	protected DeleteAction createDeleteAction( final Object objects )
	{
		return new DeleteAction( objects ) {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.birt.report.designer.internal.ui.views.actions.DeleteAction#getTransactionLabel()
			 */
			protected String getTransactionLabel( )
			{
				if ( objects instanceof IStructuredSelection )
				{
					return Messages.getString( "CutAction.trans" ); //$NON-NLS-1$
				}
				return DEFAULT_TEXT + " " + DEUtil.getDisplayLabel( objects ); //$NON-NLS-1$
			}
		};
	}
	
}

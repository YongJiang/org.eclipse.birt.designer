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
import org.eclipse.birt.report.designer.ui.dialogs.StyleBuilder;
import org.eclipse.birt.report.model.api.SharedStyleHandle;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ui.PlatformUI;

/**
 * 
 */

public class EditStyleHandler extends SelectionHandler
{

	SharedStyleHandle handle;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute( ExecutionEvent event ) throws ExecutionException
	{
		super.execute( event );

		IEvaluationContext context = (IEvaluationContext) event.getApplicationContext( );
		Object obj = context.getVariable( ICommandParameterNameContants.EDIT_STYLE_SHARED_STYLE_HANDLE_NAME );
		if ( obj != null && obj instanceof SharedStyleHandle )
		{
			handle = (SharedStyleHandle) obj;
		}

		if ( handle == null )
		{
			return Boolean.valueOf( false );
		}

		if ( Policy.TRACING_ACTIONS )
		{
			System.out.println( "Edit style action >> Run ..." ); //$NON-NLS-1$
		}
		StyleBuilder builder = new StyleBuilder( PlatformUI.getWorkbench( )
				.getDisplay( )
				.getActiveShell( ), handle, StyleBuilder.DLG_TITLE_EDIT );
		builder.open( );

		return Boolean.valueOf( true );
	}
}

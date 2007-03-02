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

import java.util.List;

import org.eclipse.birt.report.designer.internal.ui.command.CommandUtils;
import org.eclipse.birt.report.designer.internal.ui.command.ICommandParameterNameContants;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.ReportElementHandle;
import org.eclipse.birt.report.model.api.SharedStyleHandle;
import org.eclipse.birt.report.model.api.ThemeHandle;
import org.eclipse.birt.report.model.api.command.StyleException;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Add style rule action
 */

public class AddStyleAction extends ContextSelectionAction
{

	private static final String ACTION_MSG_ADD_STYLE_RULE = Messages.getString( "AddStyleAction.actionMsg.addStyleRule" ); //$NON-NLS-1$

	/** action ID */
	public static final String ID = "AddStyleAction"; //$NON-NLS-1$

	private ThemeHandle themeHandle;

	/**
	 * Contructor
	 * 
	 * @param part
	 */
	public AddStyleAction( IWorkbenchPart part )
	{
		super( part );
		setId( ID );
		setText( ACTION_MSG_ADD_STYLE_RULE );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#calculateEnabled()
	 */
	protected boolean calculateEnabled( )
	{
		return true;
	}

	/**
	 * Runs action.
	 * 
	 */
	public void run( )
	{
//		if ( Policy.TRACING_ACTIONS )
//		{
//			System.out.println( "Add Style rule action >> Run ..." ); //$NON-NLS-1$
//		}
//		CommandStack stack = getActiveCommandStack( );
//		stack.startTrans( STACK_MSG_ADD_STYLE );
//
//		ModuleHandle reportDesignHandle = SessionHandleAdapter.getInstance( )
//				.getReportDesignHandle( );
//		// StyleHandle styleHandle = reportDesignHandle.getElementFactory( )
//		// .newStyle( null );
//		StyleHandle styleHandle = DesignElementFactory.getInstance( reportDesignHandle )
//				.newStyle( null );
//
//		try
//		{
//			StyleBuilder dialog = new StyleBuilder( PlatformUI.getWorkbench( )
//					.getDisplay( )
//					.getActiveShell( ), styleHandle, StyleBuilder.DLG_TITLE_NEW );
//			if ( dialog.open( ) == Window.OK )
//			{
//				if ( themeHandle != null )
//				{
//					themeHandle.getStyles( ).add( styleHandle );
//				}
//				else
//				{
//					reportDesignHandle.getStyles( ).add( styleHandle );
//				}
//				if ( !styleHandle.isPredefined( ) )
//				{
//					applyStyle( (SharedStyleHandle) styleHandle );
//				}
//				stack.commit( );
//			}
//		}
//		catch ( Exception e )
//		{
//			stack.rollbackAll( );
//			ExceptionHandler.handle( e );
//		}
		
		if(themeHandle != null)
		{
			CommandUtils.setVariable(ICommandParameterNameContants.NEW_STYLE_THEME_HANDLE_NAME, themeHandle);	
		}
		
		try
		{
			CommandUtils.executeCommand( "org.eclipse.birt.report.designer.ui.command.newStyleCommand" );
		}
		catch ( Exception e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 * Applys style to selected elements.
	 * 
	 * @param styleHandle
	 */
	private void applyStyle( SharedStyleHandle styleHandle )
	{
		List handles = getElementHandles( );
		for ( int i = 0; i < handles.size( ); i++ )
		{
			try
			{
				if ( handles.get( i ) instanceof ReportElementHandle )
				{
					// set style
					( (DesignElementHandle) handles.get( i ) ).setStyle( styleHandle );
				}
			}
			catch ( StyleException e )
			{
				e.printStackTrace( );
			}
		}
	}

	public void setThemeHandle( ThemeHandle themeHandle )
	{
		this.themeHandle = themeHandle;
	}

}
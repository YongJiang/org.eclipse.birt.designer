/*******************************************************************************
 * Copyright (c) 2005 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.report.designer.internal.ui.editors.schematic.actions;

import org.eclipse.birt.report.designer.core.model.SessionHandleAdapter;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.editparts.TableEditPart;
import org.eclipse.birt.report.designer.internal.ui.util.Policy;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.designer.ui.dialogs.DataBindingDialog;
import org.eclipse.birt.report.model.api.CommandStack;
import org.eclipse.birt.report.model.api.ReportItemHandle;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

/**
 * Edit biding action
 */
public class EditBindingAction extends InsertRowAction
{

	public static final String ID = "org.eclipse.birt.report.designer.action.editBinding"; //$NON-NLS-1$

	/**
	 * @param part
	 */
	public EditBindingAction( IWorkbenchPart part )
	{
		super( part );
		setId( ID );
		setText( Messages
				.getString( "DesignerActionBarContributor.menu.element.editDataBinding" ) ); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#calculateEnabled()
	 */
	protected boolean calculateEnabled( )
	{
		return !SessionHandleAdapter.getInstance( ).getReportDesignHandle( )
				.getVisibleDataSets( ).isEmpty( )
				|| ( getSelectedElement() != null && getSelectedElement( )
						.getDataSet( ) != null );
	}

	private ReportItemHandle getSelectedElement( )
	{
		if ( getTableEditPart( ) != null
				&& getTableEditPart( ).getModel( ) instanceof ReportItemHandle )
		{
			return (ReportItemHandle) getTableEditPart( ).getModel( );

		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run( )
	{
		if ( Policy.TRACING_ACTIONS )
		{
			System.out.println( "Edit binding action >> Run ..." ); //$NON-NLS-1$
		}
		// Get the first item in the list and pass the model object to the
		// dialog
		TableEditPart editPart = getTableEditPart( );
		if ( editPart != null )
		{
			CommandStack stack = SessionHandleAdapter.getInstance( )
					.getCommandStack( );

			stack
					.startTrans( Messages
							.getString( "DesignerActionBarContributor.menu.element.editDataBinding" ) ); //$NON-NLS-1$
			DataBindingDialog dialog = new DataBindingDialog( PlatformUI
					.getWorkbench( ).getDisplay( ).getActiveShell( ),
					getSelectedElement( ) );

			if ( dialog.open( ) == Dialog.OK )
			{
				stack.commit( );
			}
			else
			{
				stack.rollback( );
			}
		}
	}
}
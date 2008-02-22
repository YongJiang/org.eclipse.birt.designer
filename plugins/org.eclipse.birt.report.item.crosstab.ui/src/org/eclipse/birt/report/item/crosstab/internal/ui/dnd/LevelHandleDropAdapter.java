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

package org.eclipse.birt.report.item.crosstab.internal.ui.dnd;

import org.eclipse.birt.report.designer.core.DesignerConstants;
import org.eclipse.birt.report.designer.internal.ui.dnd.DNDLocation;
import org.eclipse.birt.report.designer.internal.ui.dnd.DNDService;
import org.eclipse.birt.report.designer.internal.ui.dnd.IDropAdapter;
import org.eclipse.birt.report.designer.util.IVirtualValidator;
import org.eclipse.birt.report.item.crosstab.core.de.CrosstabCellHandle;
import org.eclipse.birt.report.item.crosstab.core.de.CrosstabReportItemHandle;
import org.eclipse.birt.report.item.crosstab.internal.ui.AggregationCellProviderWrapper;
import org.eclipse.birt.report.item.crosstab.internal.ui.editors.model.CrosstabCellAdapter;
import org.eclipse.birt.report.item.crosstab.internal.ui.editors.model.VirtualCrosstabCellAdapter;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.olap.LevelHandle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.CreateRequest;

/**
 * Support drag the levelhandle to the crosstab.Gets the command from the editpart to execute.
 */

public class LevelHandleDropAdapter implements IDropAdapter
{

	/* (non-Javadoc)
	 * @see org.eclipse.birt.report.designer.internal.ui.dnd.IDropAdapter#canDrop(java.lang.Object, java.lang.Object, int, org.eclipse.birt.report.designer.internal.ui.dnd.DNDLocation)
	 */
	public int canDrop( Object transfer, Object target, int operation,
			DNDLocation location )
	{
		if ( !isLevelHandle( transfer ) )
		{
			return DNDService.LOGIC_UNKNOW;
		}

		if ( target instanceof EditPart )
		{
			EditPart editPart = (EditPart) target;
			if ( editPart.getModel( ) instanceof IVirtualValidator )
			{
				if ( ( (IVirtualValidator) editPart.getModel( ) ).handleValidate( transfer ) )
					return DNDService.LOGIC_TRUE;
				else
					return DNDService.LOGIC_FALSE;
			}
		}
		return DNDService.LOGIC_UNKNOW;
	}

	private boolean isLevelHandle( Object transfer )
	{
		if ( transfer instanceof Object[] )
		{
			DesignElementHandle container = null;
			Object[] items = (Object[]) transfer;
			for ( int i = 0; i < items.length; i++ )
			{
				if ( !( items[i] instanceof LevelHandle ) )
					return false;
				if ( container == null )
					container = ( (LevelHandle) items[i] ).getContainer( );
				else if ( container != ( (LevelHandle) items[i] ).getContainer( ) )
					return false;
			}
			return true;
		}
		return transfer instanceof LevelHandle;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.birt.report.designer.internal.ui.dnd.IDropAdapter#performDrop(java.lang.Object, java.lang.Object, int, org.eclipse.birt.report.designer.internal.ui.dnd.DNDLocation)
	 */
	public boolean performDrop( Object transfer, Object target, int operation,
			DNDLocation location )
	{

		if ( target instanceof EditPart )//drop on layout
		{
			EditPart editPart = (EditPart) target;

			if ( editPart != null )
			{
				CreateRequest request = new CreateRequest( );

				request.getExtendedData( )
						.put( DesignerConstants.KEY_NEWOBJECT, transfer );
				request.setLocation( location.getPoint( ) );
				Command command = editPart.getCommand( request );
				if ( command != null && command.canExecute( ) )
				{
					editPart.getViewer( )
							.getEditDomain( )
							.getCommandStack( )
							.execute( command );
					CrosstabReportItemHandle crosstab = getCrosstab( editPart );
					if ( crosstab != null )
					{
						AggregationCellProviderWrapper providerWrapper = new AggregationCellProviderWrapper( crosstab );
						providerWrapper.updateAllAggregationCells( );
					}
					return true;
				}
				else
					return false;
			}
		}
		return false;
	}

	private CrosstabReportItemHandle getCrosstab( EditPart editPart )
	{
		CrosstabReportItemHandle crosstab = null;
		Object tmp = editPart.getModel( );
		if ( !( tmp instanceof CrosstabCellAdapter ) )
		{
			return null;
		}
		if ( tmp instanceof VirtualCrosstabCellAdapter )
		{
			return ( (VirtualCrosstabCellAdapter) tmp ).getCrosstabReportItemHandle( );
		}

		CrosstabCellHandle handle = ( (CrosstabCellAdapter) tmp ).getCrosstabCellHandle( );
		if ( handle != null )
		{
			crosstab = handle.getCrosstab( );
		}

		return crosstab;

	}
}

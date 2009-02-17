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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.birt.report.designer.core.model.schematic.HandleAdapterFactory;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.editparts.DummyEditpart;
import org.eclipse.birt.report.designer.internal.ui.util.Policy;
import org.eclipse.birt.report.model.api.RowHandle;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;

/**
 * 
 */

public class InsertRowHandler extends SelectionHandler
{
	public Object execute( ExecutionEvent event ) throws ExecutionException
	{
		super.execute( event );
		
		IEvaluationContext context = (IEvaluationContext) event.getApplicationContext( );
		
		Object position = context.getVariable(ICommandParameterNameContants.INSERT_ROW_POSITION);
		int intPos = -1;
		if(position != null && position instanceof Integer)
		{
			intPos = ((Integer)position).intValue( );
		}
		
		if ( Policy.TRACING_ACTIONS )
		{
			System.out.println( "Insert row above action >> Run ..." ); //$NON-NLS-1$
		}
		if ( getTableEditPart( ) != null && !getRowHandles( ).isEmpty( ) )
		{
			// has combined two behavior into one.
			getTableEditPart( ).insertRows( intPos, getRowNumbers( ) );
		}
		
		return Boolean.valueOf( true );
	}
	
	/**
	 * Gets the current selected row objects.
	 * 
	 * @return The current selected row objects.
	 */

	protected List getRowHandles( )
	{
	
		List list = getSelectedObjects();
		if ( list.isEmpty( ) )
		{
			return Collections.EMPTY_LIST;
		}
		List rowHandles = new ArrayList( );
		for ( int i = 0; i < list.size( ); i++ )
		{
			Object obj = list.get( i );
			if ( obj instanceof DummyEditpart )
			{
				if ( ( (DummyEditpart) obj ).getModel( ) instanceof RowHandle )
				{
					rowHandles.add( ( (DummyEditpart) obj ).getModel( ) );
				}
			}
		}
		return rowHandles;
	}
	
	/**
	 * Gets row numbers of selected rows. And sorts the array of ints into
	 * ascending numerical order.
	 */
	protected int[] getRowNumbers( )
	{
		List rowHandles = getRowHandles( );
		if ( rowHandles.isEmpty( ) )
		{
			return new int[0];
		}
		int size = rowHandles.size( );
		int[] rowNumbers = new int[size];

		for ( int i = 0; i < size; i++ )
		{
			rowNumbers[i] = getRowNumber( rowHandles.get( i ) );
		}

		// sorts array before returning.
		int[] a = rowNumbers;
		Arrays.sort( a );
		return a;
	}
	
	/**
	 * Gets row number given the row handle.
	 * 
	 * @return The row number of the selected row object.
	 */
	public int getRowNumber( Object rowHandle )
	{
		return HandleAdapterFactory.getInstance( )
				.getRowHandleAdapter( rowHandle )
				.getRowNumber( );
	}
	
}

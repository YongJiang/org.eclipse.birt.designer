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

package org.eclipse.birt.report.designer.core.model.schematic;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.birt.report.designer.core.model.DesignElementHandleAdapter;
import org.eclipse.birt.report.designer.core.model.IModelAdapterHelper;
import org.eclipse.birt.report.model.activity.SemanticException;
import org.eclipse.birt.report.model.api.CellHandle;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.ReportElementHandle;
import org.eclipse.birt.report.model.api.RowHandle;
import org.eclipse.birt.report.model.api.TableGroupHandle;
import org.eclipse.birt.report.model.elements.Cell;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.util.Assert;

/**
 * Adapter class to adapt model handle. This adapter provides convenience.
 * methods to GUI requirement CellHandleAdapter responds to model CellHandle
 *  
 */

public class CellHandleAdapter extends DesignElementHandleAdapter
{

	/**
	 * Constructor
	 * 
	 * @param cellHandle
	 *            The cell handle.
	 * @param mark
	 */
	public CellHandleAdapter( ReportElementHandle cellHandle, IModelAdapterHelper mark )
	{
		super( cellHandle, mark );
	}

	/**
	 * Gets the Children iterator. This children relationship is determined by
	 * GUI requirement. This is not the model children relationship.
	 * 
	 * @return Children iterator
	 */

	public List getChildren( )
	{
		List list = new ArrayList( );
		insertIteratorToList( getCellHandle( ).getContent( ).iterator( ), list );
		return list;
	}

	/**
	 * Gets the row number.
	 * 
	 * @return The row number.
	 */
	public int getRowNumber( )
	{
		Assert.isLegal( getCellHandle( ).getContainer( ) instanceof RowHandle );
		return HandleAdapterFactory.getInstance( )
				.getRowHandleAdapter( getCellHandle( ).getContainer( ) )
				.getRowNumber( );
	}

	/**
	 * Gets the column number
	 * 
	 * @return The column number.
	 */
	public int getColumnNumber( )
	{
		Assert.isLegal( getCellHandle( ).getContainer( ) instanceof RowHandle );

		if ( getCellHandle( ).getColumn( ) == 0 )
		{
			TableHandleAdapter adapt = HandleAdapterFactory.getInstance( )
					.getTableHandleAdapter( getTableParent( ) );
			TableHandleAdapter.RowUIInfomation info = adapt.getRowInfo( getHandle( ).getContainer( ) );
			return info.getAllChildren( ).indexOf( getHandle( ) ) + 1;
		}

		return getCellHandle( ).getColumn( );
	}

	/**
	 * Gets the row span.
	 * 
	 * @return The row span.
	 */
	public int getRowSpan( )
	{
		return getCellHandle( ).getRowSpan( );
	}

	/**
	 * Gets the column span
	 * 
	 * @return the column span.
	 */
	public int getColumnSpan( )
	{
		return getCellHandle( ).getColumnSpan( );
	}

	/**
	 * Gets the location.
	 * 
	 * @return The location.
	 */
	public Point getLocation( )
	{
		return new Point( 1, 1 );
	}

	/**
	 * Cell sets location do nothing
	 * 
	 * @param location
	 * @throws SemanticException
	 */
	public void setLocation( Point location ) throws SemanticException
	{

	}

	/**
	 * Gets the size.
	 * 
	 * @return The size
	 */
	public Dimension getSize( )
	{
		return new Dimension( 60, 40 );
	}

	/**
	 * Sets the size
	 * 
	 * @param size
	 *            The new size to be set.
	 * @throws SemanticException
	 */
	public void setSize( Dimension size ) throws SemanticException
	{

	}

	/**
	 * Gets the bounds.
	 * 
	 * @return The bounds
	 */

	public Rectangle getBounds( )
	{
		return new Rectangle( getLocation( ).x,
				getLocation( ).y,
				getSize( ).width,
				getSize( ).height );
	}

	/**
	 * Sets bounds.
	 * 
	 * @param bounds
	 *            The bounds
	 * @throws SemanticException
	 *             The semantic exception
	 */

	public void setBounds( Rectangle bounds ) throws SemanticException
	{
		setSize( bounds.getSize( ) );
		setLocation( new Point( bounds.getLocation( ).x,
				bounds.getLocation( ).y ) );
	}

	private CellHandle getCellHandle( )
	{
		return (CellHandle) getHandle( );
	}

	/**
	 * Set column span.
	 * 
	 * @param colSpan
	 *            The new column span.
	 * @throws SemanticException
	 */
	public void setColumnSpan( int colSpan ) throws SemanticException
	{
		this.getCellHandle( ).setProperty( Cell.COL_SPAN_PROP,
				new Integer( colSpan ) );
	}

	/**
	 * Set row span.
	 * 
	 * @param rowSpan
	 *            The new row span.
	 * @throws SemanticException
	 */
	public void setRowSpan( int rowSpan ) throws SemanticException
	{
		this.getCellHandle( ).setProperty( Cell.ROW_SPAN_PROP,
				new Integer( rowSpan ) );

	}

	private Object getTableParent( )
	{
		DesignElementHandle item = getCellHandle( ).getContainer( )
				.getContainer( );
		if ( item instanceof TableGroupHandle )
		{
			item = item.getContainer( );
		}
		return item;
	}
}
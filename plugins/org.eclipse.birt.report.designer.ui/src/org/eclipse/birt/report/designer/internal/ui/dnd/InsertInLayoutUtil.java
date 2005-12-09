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

package org.eclipse.birt.report.designer.internal.ui.dnd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.birt.report.designer.core.model.schematic.HandleAdapterFactory;
import org.eclipse.birt.report.designer.core.model.schematic.ListBandProxy;
import org.eclipse.birt.report.designer.core.model.schematic.TableHandleAdapter;
import org.eclipse.birt.report.designer.core.model.views.data.DataSetItemModel;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.editparts.ReportElementEditPart;
import org.eclipse.birt.report.designer.internal.ui.util.DataSetManager;
import org.eclipse.birt.report.designer.internal.ui.util.ExceptionHandler;
import org.eclipse.birt.report.designer.ui.newelement.DesignElementFactory;
import org.eclipse.birt.report.designer.util.DEUtil;
import org.eclipse.birt.report.designer.util.DNDUtil;
import org.eclipse.birt.report.model.api.CellHandle;
import org.eclipse.birt.report.model.api.DataItemHandle;
import org.eclipse.birt.report.model.api.DataSetHandle;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.FreeFormHandle;
import org.eclipse.birt.report.model.api.GridHandle;
import org.eclipse.birt.report.model.api.GroupHandle;
import org.eclipse.birt.report.model.api.LabelHandle;
import org.eclipse.birt.report.model.api.ListHandle;
import org.eclipse.birt.report.model.api.ModuleHandle;
import org.eclipse.birt.report.model.api.ReportItemHandle;
import org.eclipse.birt.report.model.api.RowHandle;
import org.eclipse.birt.report.model.api.ScalarParameterHandle;
import org.eclipse.birt.report.model.api.SlotHandle;
import org.eclipse.birt.report.model.api.TableGroupHandle;
import org.eclipse.birt.report.model.api.TableHandle;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.eclipse.birt.report.model.api.elements.DesignChoiceConstants;
import org.eclipse.birt.report.model.api.elements.ReportDesignConstants;
import org.eclipse.gef.EditPart;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * Utility for creation from data view to layout
 */

public class InsertInLayoutUtil
{

	/**
	 * Rule interface for defining insertion rule
	 */
	abstract static interface InsertInLayoutRule
	{

		public boolean canInsert( );

		public Object getInsertPosition( );

		public void insert( Object object ) throws SemanticException;
	}

	/**
	 * 
	 * Rule for inserting label after inserting data set column
	 */
	static class LabelAddRule implements InsertInLayoutRule
	{

		private Object container;

		private CellHandle newTarget;

		public LabelAddRule( Object container )
		{
			this.container = container;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.report.designer.internal.ui.views.actions.InsertInLayoutAction.InsertInLayoutRule#canInsert()
		 */
		public boolean canInsert( )
		{
			if ( container instanceof SlotHandle )
			{
				container = ( (SlotHandle) container ).getElementHandle( );
			}
			if ( !( container instanceof CellHandle ) )
				return false;

			CellHandle cell = (CellHandle) container;

			// Validates source position of data item
			boolean canInsert = false;
			if ( cell.getContainer( ).getContainer( ) instanceof TableGroupHandle )
			{
				canInsert = true;
			}
			else
			{
				if ( cell.getContainer( ).getContainerSlotHandle( ).getSlotID( ) == TableHandle.DETAIL_SLOT )
				{
					canInsert = true;
				}
			}

			// Validates column count and gets the target
			if ( canInsert )
			{
				TableHandle table = null;
				if ( cell.getContainer( ).getContainer( ) instanceof TableHandle )
				{
					table = (TableHandle) cell.getContainer( ).getContainer( );
				}
				else
				{
					table = (TableHandle) cell.getContainer( )
							.getContainer( )
							.getContainer( );
				}
				SlotHandle header = table.getHeader( );
				if ( header != null && header.getCount( ) > 0 )
				{
					int columnNum = HandleAdapterFactory.getInstance( )
							.getCellHandleAdapter( cell )
							.getColumnNumber( );
					newTarget = (CellHandle) HandleAdapterFactory.getInstance( )
							.getTableHandleAdapter( table )
							.getCell( 1, columnNum, false );
					return newTarget != null
							&& newTarget.getContent( ).getCount( ) == 0;
				}
			}
			return false;
		}

		/**
		 * Returns new Label insert position in form of <code>CellHandle</code>
		 */
		public Object getInsertPosition( )
		{
			return newTarget;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.report.designer.internal.ui.dnd.InsertInLayoutUtil.InsertInLayoutRule#insert()
		 */
		public void insert( Object object ) throws SemanticException
		{
			Assert.isTrue( object instanceof DesignElementHandle );
			newTarget.addElement( (DesignElementHandle) object,
					CellHandle.CONTENT_SLOT );
		}
	}

	/**
	 * 
	 * Rule for inserting multiple data into table, and populating adjacent
	 * cells
	 */
	static class MultiItemsExpandRule implements InsertInLayoutRule
	{

		private Object[] items;
		private Object target;
		private int focusIndex = 0;

		public MultiItemsExpandRule( Object[] items, Object target )
		{
			this.items = items;
			this.target = target;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.report.designer.internal.ui.views.actions.InsertInLayoutAction.InsertInLayoutRule#canInsert()
		 */
		public boolean canInsert( )
		{
			return items != null
					&& items.length > 1
					&& target != null
					&& ( target instanceof DesignElementHandle || target instanceof ListBandProxy );
		}

		/**
		 * 
		 * Returns multiple insert positions in form of array
		 */
		public Object getInsertPosition( )
		{
			Object[] positions = new Object[items.length];

			if ( target instanceof CellHandle )
			{
				CellHandle firstCell = (CellHandle) target;
				TableHandleAdapter tableAdapter = HandleAdapterFactory.getInstance( )
						.getTableHandleAdapter( getTableHandle( firstCell ) );
				int currentColumn = HandleAdapterFactory.getInstance( )
						.getCellHandleAdapter( firstCell )
						.getColumnNumber( );
				int currentRow = HandleAdapterFactory.getInstance( )
						.getCellHandleAdapter( firstCell )
						.getRowNumber( );
				int columnDiff = currentColumn
						+ items.length - tableAdapter.getColumnCount( ) - 1;

				// Insert columns if table can not contain all items
				if ( columnDiff > 0 )
				{
					int insertColumn = tableAdapter.getColumnCount( );
					try
					{
						tableAdapter.insertColumns( columnDiff, insertColumn );
					}
					catch ( SemanticException e )
					{
						ExceptionHandler.handle( e );
						return null;
					}
				}

				for ( int i = 0; i < positions.length; i++ )
				{
					positions[i] = tableAdapter.getCell( currentRow,
							currentColumn++ );
				}
				focusIndex = 0;
			}
			else
			{
				for ( int i = 0; i < positions.length; i++ )
				{
					positions[i] = target;
				}
				focusIndex = items.length - 1;
			}
			return positions;
		}

		protected TableHandle getTableHandle( CellHandle firstCell )
		{
			DesignElementHandle tableContainer = firstCell.getContainer( )
					.getContainer( );
			if ( tableContainer instanceof TableHandle )
			{
				return (TableHandle) tableContainer;
			}
			return (TableHandle) tableContainer.getContainer( );
		}

		/**
		 * Returns the index of the focus element in the items
		 */
		public int getFocusIndex( )
		{
			return focusIndex;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.report.designer.internal.ui.dnd.InsertInLayoutUtil.InsertInLayoutRule#insert()
		 */
		public void insert( Object object ) throws SemanticException
		{
			// TODO Auto-generated method stub

		}
	}

	/**
	 * 
	 * Rule for setting key when inserting data set column to group handle
	 */
	static class GroupKeySetRule implements InsertInLayoutRule
	{

		private Object container;
		private DataSetItemModel dataSetColumn;

		public GroupKeySetRule( Object container, DataSetItemModel dataSetColumn )
		{
			this.container = container;
			this.dataSetColumn = dataSetColumn;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.report.designer.internal.ui.dnd.InsertInLayoutUtil.InsertInLayoutRule#canInsert()
		 */
		public boolean canInsert( )
		{
			return getGroupContainer( container ) != null
					&& getGroupHandle( container ).getKeyExpr( ) == null
					&& ( getGroupContainer( container ).getDataSet( ) == getDataSetHandle( dataSetColumn ) || getGroupContainer( container ).getDataSet( ) == null );
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.report.designer.internal.ui.dnd.InsertInLayoutUtil.InsertInLayoutRule#getInsertPosition()
		 */
		public Object getInsertPosition( )
		{
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.report.designer.internal.ui.dnd.InsertInLayoutUtil.InsertInLayoutRule#insert(java.lang.Object)
		 */
		public void insert( Object object ) throws SemanticException
		{
			Assert.isTrue( object instanceof DataSetItemModel );
			Assert.isTrue( object == dataSetColumn || object == null );
			getGroupContainer( container ).setDataSet( getDataSetHandle( dataSetColumn ) );
			getGroupHandle( container ).setKeyExpr( DEUtil.getExpression( dataSetColumn ) );
		}

		protected DataSetHandle getDataSetHandle( DataSetItemModel model )
		{
			return (DataSetHandle) model.getParent( );
		}

		protected GroupHandle getGroupHandle( Object target )
		{
			DesignElementHandle handle = null;
			if ( target instanceof CellHandle )
			{
				handle = ( (CellHandle) target ).getContainer( ).getContainer( );
			}
			else if ( target instanceof ListBandProxy )
			{
				handle = ( (ListBandProxy) target ).getElemtHandle( );
			}

			if ( handle instanceof GroupHandle )
			{
				return (GroupHandle) handle;
			}
			return null;
		}

		protected ReportItemHandle getGroupContainer( Object target )
		{
			GroupHandle group = getGroupHandle( target );
			if ( group != null
					&& group.getContainer( ) instanceof ReportItemHandle )
				return (ReportItemHandle) group.getContainer( );
			return null;
		}
	}

	/**
	 * Creates a object to insert.
	 * 
	 * @param insertObj
	 *            object insert to layout
	 * @param target
	 *            insert target, like cell or ListBandProxy
	 * @param targetParent
	 *            insert target's non-dummy container, like table or list
	 * @return new object in layout
	 * @throws SemanticException
	 */
	public static DesignElementHandle performInsert( Object insertObj,
			Object target, Object targetParent ) throws SemanticException
	{
		Assert.isNotNull( insertObj );
		Assert.isNotNull( target );
		if ( insertObj instanceof DataSetHandle )
		{
			return performInsertDataSet( (DataSetHandle) insertObj );
		}
		else if ( insertObj instanceof DataSetItemModel )
		{
			return performInsertDataSetColumn( (DataSetItemModel) insertObj,
					target,
					targetParent );
		}
		else if ( insertObj instanceof ScalarParameterHandle )
		{
			return performInsertParameter( (ScalarParameterHandle) insertObj );
		}
		else if ( insertObj instanceof String )
		{
			// Such as invalid group key
			return performInsertString( (String) insertObj, target );
		}
		else if ( insertObj instanceof Object[] )
		{
			return performMultiInsert( (Object[]) insertObj,
					target,
					targetParent );
		}
		else if ( insertObj instanceof IStructuredSelection )
		{
			return performMultiInsert( ( (IStructuredSelection) insertObj ).toArray( ),
					target,
					targetParent );
		}
		return null;
	}

	/**
	 * Creates a object, "Add" operation to layout needs to handle later.
	 * <p>
	 * Must make sure operation legal before execution.
	 * </p>
	 * 
	 * @param insertObj
	 *            object insert to layout
	 * @param editPart
	 *            target EditPart
	 * @return new object in layout
	 * @throws SemanticException
	 */
	public static DesignElementHandle performInsert( Object insertObj,
			EditPart editPart ) throws SemanticException
	{
		Assert.isNotNull( insertObj );
		Assert.isNotNull( editPart );
		return performInsert( insertObj,
				editPart.getModel( ),
				editPart.getParent( ).getModel( ) );
	}

	/**
	 * Creates multiple objects
	 * 
	 * @param array
	 *            multiple creation source
	 * @param target
	 * @param targetParent
	 * @return first creation in layout
	 * @throws SemanticException
	 */
	protected static DesignElementHandle performMultiInsert( Object[] array,
			Object target, Object targetParent ) throws SemanticException
	{
		DesignElementHandle result = null;

		MultiItemsExpandRule rule = new MultiItemsExpandRule( array, target );
		if ( rule.canInsert( ) )
		{
			Object[] positions = (Object[]) rule.getInsertPosition( );
			if ( positions != null )
			{
				for ( int i = 0; i < array.length; i++ )
				{
					DesignElementHandle newObj = performInsert( array[i],
							positions[i],
							targetParent );
					if ( i == rule.getFocusIndex( ) )
					{
						result = newObj;
					}
					else
					{
						DNDUtil.addElementHandle( positions[i], newObj );
					}
				}
			}
		}
		else if ( array.length != 0 )
		{
			result = performInsert( array[0], target, targetParent );
		}
		return result;
	}

	protected static DataItemHandle performInsertParameter(
			ScalarParameterHandle model ) throws SemanticException
	{
		// DataItemHandle dataHandle = SessionHandleAdapter.getInstance( )
		// .getReportDesignHandle( )
		// .getElementFactory( )
		// .newDataItem( null );
		DataItemHandle dataHandle = DesignElementFactory.getInstance( )
				.newDataItem( null );
		dataHandle.setValueExpr( DEUtil.getExpression( model ) );
		return dataHandle;
	}

	/**
	 * Inserts dataset column into the target. Add label or group key if
	 * possible
	 * 
	 * @param model
	 *            column item
	 * @param target
	 *            insert target like cell or ListBandProxy
	 * @param targetParent
	 *            target container like table or list
	 * @return to be inserted data item
	 * @throws SemanticException
	 */
	protected static DataItemHandle performInsertDataSetColumn(
			DataSetItemModel model, Object target, Object targetParent )
			throws SemanticException
	{
		// DataItemHandle dataHandle = SessionHandleAdapter.getInstance( )
		// .getReportDesignHandle( )
		// .getElementFactory( )
		// .newDataItem( null );
		DataItemHandle dataHandle = DesignElementFactory.getInstance( )
				.newDataItem( null );
		DataSetHandle dataSet = (DataSetHandle) model.getParent( );

		dataHandle.setValueExpr( DEUtil.getExpression( model ) );

		if ( targetParent instanceof ReportItemHandle )
		{
			ReportItemHandle container = (ReportItemHandle) targetParent;
			if ( !DEUtil.getDataSetList( container ).contains( dataSet ) )
			{
				if ( container.getDataSet( ) == null )
				{
					container.setDataSet( dataSet );
				}
			}
		}
		else
		{
			// Adds dataset to the single query in the top level of the report
			dataHandle.setDataSet( dataSet );
		}

		InsertInLayoutRule rule = new LabelAddRule( target );
		if ( rule.canInsert( ) )
		{
			// LabelHandle label = SessionHandleAdapter.getInstance( )
			// .getReportDesignHandle( )
			// .getElementFactory( )
			// .newLabel( null );
			LabelHandle label = DesignElementFactory.getInstance( )
					.newLabel( null );
			label.setText( model.getDisplayName( ) );
			rule.insert( label );
		}

		rule = new GroupKeySetRule( target, model );
		if ( rule.canInsert( ) )
		{
			rule.insert( model );
		}

		return dataHandle;
	}

	/**
	 * Inserts invalid column string into the target. Add label if possible
	 * 
	 * @param expression
	 *            invalid column or other expression
	 * @param target
	 *            insert target like cell or ListBandProxy
	 * @return to be inserted data item
	 * @throws SemanticException
	 */
	protected static DesignElementHandle performInsertString(
			String expression, Object target ) throws SemanticException
	{
		// DataItemHandle dataHandle = SessionHandleAdapter.getInstance( )
		// .getReportDesignHandle( )
		// .getElementFactory( )
		// .newDataItem( null );
		DataItemHandle dataHandle = DesignElementFactory.getInstance( )
				.newDataItem( null );
		dataHandle.setValueExpr( expression );

		InsertInLayoutRule rule = new LabelAddRule( target );
		if ( rule.canInsert( ) )
		{
			// LabelHandle label = SessionHandleAdapter.getInstance( )
			// .getReportDesignHandle( )
			// .getElementFactory( )
			// .newLabel( null );
			LabelHandle label = DesignElementFactory.getInstance( )
					.newLabel( null );
			label.setText( expression );
			rule.insert( label );
		}

		return dataHandle;
	}

	protected static TableHandle performInsertDataSet( DataSetHandle model )
			throws SemanticException
	{
		DataSetItemModel[] columns = DataSetManager.getCurrentInstance( )
				.getColumns( model, false );
		if ( columns == null || columns.length == 0 )
		{
			return null;
		}
		// TableHandle tableHandle = SessionHandleAdapter.getInstance( )
		// .getReportDesignHandle( )
		// .getElementFactory( )
		// .newTableItem( null, columns.length );
		TableHandle tableHandle = DesignElementFactory.getInstance( )
				.newTableItem( null, columns.length );

		setInitWidth( tableHandle );
		insertToCell( tableHandle.getHeader( ), columns, true );
		insertToCell( tableHandle.getDetail( ), columns, false );

		tableHandle.setDataSet( model );
		return tableHandle;
	}

	/**
	 * Validates object can be inserted to layout. Support the multiple.
	 * 
	 * @param insertObj
	 *            single inserted object or multi-objects
	 * @param targetPart
	 * @return if can be inserted to layout
	 */
	public static boolean handleValidateInsertToLayout( Object insertObj,
			EditPart targetPart )
	{
		if ( targetPart == null )
		{
			return false;
		}
		if ( insertObj instanceof Object[] )
		{
			Object[] array = (Object[]) insertObj;
			if ( !checkSameDataSetInMultiColumns( array ) )
			{
				return false;
			}
			for ( int i = 0; i < array.length; i++ )
			{
				if ( !handleValidateInsertToLayout( array[i], targetPart ) )
				{
					return false;
				}
			}
			return true;
		}
		else if ( insertObj instanceof IStructuredSelection )
		{
			return handleValidateInsertToLayout( ( (IStructuredSelection) insertObj ).toArray( ),
					targetPart );
		}
		else if ( insertObj instanceof DataSetHandle )
		{
			return isHandleValid( (DataSetHandle) insertObj )
					&& ( (DataSetHandle) insertObj ).getDataSource( ) != null
					&& handleValidateDataSet( targetPart );
		}
		else if ( insertObj instanceof DataSetItemModel )
		{
			return handleValidateDataSetColumn( (DataSetItemModel) insertObj,
					targetPart );
		}
		else if ( insertObj instanceof ScalarParameterHandle )
		{
			return isHandleValid( (ScalarParameterHandle) insertObj )
					&& handleValidateParameter( targetPart );
		}
		return false;
	}

	/**
	 * Checks if all the DataSetColumn has the same DataSet.
	 * 
	 * @param array
	 *            all elements
	 * @return false if not same; true if every column has the same DataSet or
	 *         the element is not an instance of DataSetColumn
	 */
	protected static boolean checkSameDataSetInMultiColumns( Object[] array )
	{
		if ( array == null )
			return false;
		Object dataSet = null;
		for ( int i = 0; i < array.length; i++ )
		{
			if ( array[i] instanceof DataSetItemModel )
			{
				Object currDataSet = ( (DataSetItemModel) array[i] ).getParent( );
				if ( currDataSet == null )
				{
					return false;
				}

				if ( dataSet == null )
				{
					dataSet = currDataSet;
				}
				else
				{
					if ( dataSet != currDataSet )
					{
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Validates container of drop target from data set in data view
	 * 
	 * @param dropPart
	 * @return validate result
	 */
	protected static boolean handleValidateDataSetDropContainer(
			EditPart dropPart )
	{
		if ( dropPart.getParent( ) == null )
		{
			return false;
		}
		Object container = dropPart.getParent( ).getModel( );
		return ( container instanceof GridHandle
				|| container instanceof TableHandle
				|| container instanceof FreeFormHandle
				|| container instanceof ListHandle || dropPart.getModel( ) instanceof ModuleHandle );
	}

	/**
	 * Validates container of drop target from data set column in data view
	 * 
	 * @param dropPart
	 * @return validate result
	 */
	protected static boolean handleValidateDataSetColumnDropContainer(
			EditPart dropPart )
	{
		if ( dropPart.getParent( ) == null )
		{
			return false;
		}
		Object container = dropPart.getParent( ).getModel( );
		return ( container instanceof GridHandle
				|| container instanceof TableHandle
				|| container instanceof FreeFormHandle
				|| container instanceof ListHandle || dropPart.getModel( ) instanceof ModuleHandle );
	}

	/**
	 * Validates container of drop target from scalar parameter in data view
	 * 
	 * @param dropPart
	 * @return validate result
	 */
	protected static boolean handleValidateParameterDropContainer(
			EditPart dropPart )
	{
		if ( dropPart.getParent( ) == null )
		{
			return false;
		}
		Object container = dropPart.getParent( ).getModel( );
		return ( container instanceof GridHandle
				|| container instanceof TableHandle
				|| container instanceof FreeFormHandle
				|| container instanceof ListHandle || dropPart.getModel( ) instanceof ModuleHandle );
	}

	/**
	 * Validates drop target from data set in data view.
	 * 
	 * @return validate result
	 */
	protected static boolean handleValidateDataSet( EditPart target )
	{
		return handleValidateDataSetDropContainer( target )
				&& DNDUtil.handleValidateTargetCanContainType( target.getModel( ),
						ReportDesignConstants.TABLE_ITEM );
	}

	/**
	 * Validates drop target from data set column in data view.
	 * 
	 * @return validate result
	 */
	protected static boolean handleValidateDataSetColumn(
			DataSetItemModel insertObj, EditPart target )
	{
		if ( handleValidateDataSetColumnDropContainer( target )
				&& DNDUtil.handleValidateTargetCanContainType( target.getModel( ),
						ReportDesignConstants.DATA_ITEM ) )
		{
			// Validates target is report root
			if ( target.getModel( ) instanceof ModuleHandle )
			{
				return true;
			}
			// Validates target's dataset is null or the same with the inserted
			DesignElementHandle handle = (DesignElementHandle) target.getParent( )
					.getModel( );
			if ( handle instanceof ReportItemHandle
					&& ( (ReportItemHandle) handle ).getDataSet( ) == null )
			{
				return true;
			}
			return DEUtil.getDataSetList( handle )
					.contains( insertObj.getParent( ) );
		}
		return false;
	}

	/**
	 * Validates drop target from scalar parameter in data view.
	 * 
	 * @return validate result
	 */
	protected static boolean handleValidateParameter( EditPart target )
	{
		return handleValidateParameterDropContainer( target )
				&& DNDUtil.handleValidateTargetCanContainType( target.getModel( ),
						ReportDesignConstants.DATA_ITEM );
	}

	/**
	 * Validates drag source from data view to layout. Support the multiple.
	 * 
	 * @return validate result
	 */
	public static boolean handleValidateInsert( Object insertObj )
	{
		if ( insertObj instanceof Object[] )
		{
			Object[] array = (Object[]) insertObj;
			if(array.length == 0)
			{
				return false;
			}
			for ( int i = 0; i < array.length; i++ )
			{
				if ( !handleValidateInsert( array[i] ) )
					return false;
			}
			return true;
		}
		else if ( insertObj instanceof IStructuredSelection )
		{
			return handleValidateInsert( ( (IStructuredSelection) insertObj ).toArray( ) );
		}
//		else if ( insertObj instanceof ParameterHandle )
//		{
//			if ( ( (ParameterHandle) insertObj ).getRoot( ) instanceof LibraryHandle )
//				return false;
//		}
		return insertObj instanceof DataSetHandle
				|| insertObj instanceof DataSetItemModel
				|| insertObj instanceof ScalarParameterHandle;
	}

	protected static void insertToCell( SlotHandle slot,
			DataSetItemModel[] columns, boolean isLabel )
	{
		for ( int i = 0; i < slot.getCount( ); i++ )
		{
			SlotHandle cells = ( (RowHandle) slot.get( i ) ).getCells( );
			for ( int j = 0; j < cells.getCount( ); j++ )
			{
				CellHandle cell = (CellHandle) cells.get( j );

				try
				{
					if ( isLabel )
					{
						// LabelHandle labelItemHandle =
						// SessionHandleAdapter.getInstance( )
						// .getReportDesignHandle( )
						// .getElementFactory( )
						// .newLabel( null );
						LabelHandle labelItemHandle = DesignElementFactory.getInstance( )
								.newLabel( null );
						labelItemHandle.setText( columns[j].getDisplayName( ) );
						cell.addElement( labelItemHandle, cells.getSlotID( ) );
					}
					else
					{
						// DataItemHandle dataHandle =
						// SessionHandleAdapter.getInstance( )
						// .getReportDesignHandle( )
						// .getElementFactory( )
						// .newDataItem( null );
						DataItemHandle dataHandle = DesignElementFactory.getInstance( )
								.newDataItem( null );
						dataHandle.setValueExpr( DEUtil.getExpression( columns[j] ) );
						cell.addElement( dataHandle, cells.getSlotID( ) );
					}
				}
				catch ( Exception e )
				{
					ExceptionHandler.handle( e );
				}
			}
		}
	}

	/**
	 * Sets initial width to new object
	 * 
	 * @param object
	 *            new object
	 */
	public static void setInitWidth( Object object )
	{
		int percentAll = 100;
		try
		{
			if ( object instanceof TableHandle )
			{
				TableHandle table = (TableHandle) object;
				table.setWidth( percentAll
						+ DesignChoiceConstants.UNITS_PERCENTAGE );
			}
			else if ( object instanceof GridHandle )
			{
				GridHandle grid = (GridHandle) object;
				grid.setWidth( percentAll
						+ DesignChoiceConstants.UNITS_PERCENTAGE );
			}
			else
				return;
		}
		catch ( SemanticException e )
		{
			ExceptionHandler.handle( e );
		}
	}

	protected static boolean isHandleValid( DesignElementHandle handle )
	{
		return handle.isValid( ) && handle.getSemanticErrors( ).isEmpty( );
	}

	/**
	 * Converts edit part selection into model selection.
	 * 
	 * @param selection
	 *            edit part
	 * @return model, return Collections.EMPTY_LIST if selection is null or
	 *         empty.
	 */
	public static IStructuredSelection editPart2Model( ISelection selection )
	{
		if ( selection == null || !( selection instanceof IStructuredSelection ) )
			return new StructuredSelection( Collections.EMPTY_LIST );
		List list = ( (IStructuredSelection) selection ).toList( );
		List resultList = new ArrayList( );
		for ( int i = 0; i < list.size( ); i++ )
		{
			Object obj = list.get( i );
			if ( obj instanceof ReportElementEditPart )
			{
				Object model = ( (ReportElementEditPart) obj ).getModel( );
				if ( model instanceof ListBandProxy )
				{
					model = ( (ListBandProxy) model ).getSlotHandle( );
				}
				resultList.add( model );
			}
		}
		return new StructuredSelection( resultList );
	}
}
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

package org.eclipse.birt.report.designer.data.ui.dataset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.birt.core.data.ExpressionUtil;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.data.engine.api.aggregation.AggregationManager;
import org.eclipse.birt.data.engine.api.aggregation.IAggrFunction;
import org.eclipse.birt.data.engine.api.aggregation.IParameterDefn;
import org.eclipse.birt.report.data.adapter.api.DataAdapterUtil;
import org.eclipse.birt.report.designer.data.ui.property.AbstractDescriptionPropertyPage;
import org.eclipse.birt.report.designer.data.ui.util.ControlProvider;
import org.eclipse.birt.report.designer.data.ui.util.DataSetExpressionProvider;
import org.eclipse.birt.report.designer.data.ui.util.DataSetProvider;
import org.eclipse.birt.report.designer.data.ui.util.DataUtil;
import org.eclipse.birt.report.designer.data.ui.util.Utility;
import org.eclipse.birt.report.designer.internal.ui.util.ExceptionHandler;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.designer.ui.ReportPlugin;
import org.eclipse.birt.report.designer.ui.dialogs.ExpressionBuilder;
import org.eclipse.birt.report.designer.util.DEUtil;
import org.eclipse.birt.report.model.api.AggregationArgumentHandle;
import org.eclipse.birt.report.model.api.ColumnHintHandle;
import org.eclipse.birt.report.model.api.ComputedColumnHandle;
import org.eclipse.birt.report.model.api.DataSetHandle;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.PropertyHandle;
import org.eclipse.birt.report.model.api.StructureFactory;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.eclipse.birt.report.model.api.elements.structures.AggregationArgument;
import org.eclipse.birt.report.model.api.elements.structures.ComputedColumn;
import org.eclipse.birt.report.model.api.metadata.IChoice;
import org.eclipse.birt.report.model.core.Structure;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

/**
 * TODO: Please document
 * 
 * @version $Revision$ $Date$
 */
public class DataSetComputedColumnsPage extends AbstractDescriptionPropertyPage
		implements
			ITableLabelProvider
{

	private transient PropertyHandle computedColumns = null;
	private transient PropertyHandleTableViewer viewer = null;
	
	private static final int COLUMN_NAME_INDEX = 0;
	private static final int DATA_TYPE_INDEX = 1;
	private static final int EXPRESSION_INDEX = 2;
	private static final int AGGREGATION_INDEX = 3;
//	private static final int ARGUMENT_INDEX = 4;
	private static final int FILTER_INDEX = 5;
	
	private static String[] cellLabels = new String[]{
			Messages.getString( "dataset.editor.title.columnName" ),//$NON-NLS-1$
			Messages.getString( "dataset.editor.title.dataType" ),//$NON-NLS-1$
			Messages.getString( "dataset.editor.title.expression" ),//$NON-NLS-1$
			Messages.getString( "dataset.editor.title.aggrFunc" ), //$NON-NLS-1$
			Messages.getString( "dataset.editor.title.aggrArgu" ), //$NON-NLS-1$
			Messages.getString( "dataset.editor.title.filter" ) //$NON-NLS-1$
	};

	private static String[] cellProperties = new String[]{
			ComputedColumn.NAME_MEMBER,
			ComputedColumn.DATA_TYPE_MEMBER,
			ComputedColumn.EXPRESSION_MEMBER,
			ComputedColumn.AGGREGATEON_FUNCTION_MEMBER,
			ComputedColumn.ARGUMENTS_MEMBER,
			ComputedColumn.FILTER_MEMBER
	};

	private static IChoice[] dataTypes = DEUtil.getMetaDataDictionary( )
			.getStructure( ComputedColumn.COMPUTED_COLUMN_STRUCT )
			.getMember( ComputedColumn.DATA_TYPE_MEMBER )
			.getAllowedChoices( )
			.getChoices( );
	
	private static  String[] dialogLabels = new String[]{
		Messages.getString( "dataset.editor.inputDialog.columnName" ),//$NON-NLS-1$
		Messages.getString( "dataset.editor.inputDialog.dataType" ),//$NON-NLS-1$
		Messages.getString( "dataset.editor.inputDialog.expression" ),//$NON-NLS-1$
		Messages.getString( "dataset.editor.inputDialog.aggrFunc" ), //$NON-NLS-1$
		Messages.getString( "dataset.editor.inputDialog.aggrArgu" ), //$NON-NLS-1$
		Messages.getString( "dataset.editor.inputDialog.filter" ) //$NON-NLS-1$
	};

	private AggregationManager aggregationManager;
	
	

	/**
	 * 
	 */
	public DataSetComputedColumnsPage( )
	{
		super( );
		try
		{
			aggregationManager = DataUtil.getAggregationManager( ).getInstance( );
		}
		catch ( BirtException e )
		{
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.ui.dialogs.properties.IPropertyPage#createPageControl(org.eclipse.swt.widgets.Composite)
	 */
	public Control createContents( Composite parent )
	{
		computedColumns = ( (DataSetHandle) getContainer( ).getModel( ) ).getPropertyHandle( DataSetHandle.COMPUTED_COLUMNS_PROP );;
		viewer = new PropertyHandleTableViewer( parent, true, true, true );
		TableColumn column = new TableColumn( viewer.getViewer( ).getTable( ),
				SWT.LEFT );
		column.setText( cellLabels[COLUMN_NAME_INDEX] );
		column.setWidth( 150 );
		column = new TableColumn( viewer.getViewer( ).getTable( ), SWT.LEFT );
		column.setText( cellLabels[DATA_TYPE_INDEX] );
		column.setWidth( 200 );
		column = new TableColumn( viewer.getViewer( ).getTable( ), SWT.LEFT );
		column.setText( cellLabels[EXPRESSION_INDEX] );
		column.setWidth( 200 );
		column = new TableColumn( viewer.getViewer( ).getTable( ), SWT.LEFT );
		column.setText( cellLabels[AGGREGATION_INDEX] );
		column.setWidth( 200 );
		column = new TableColumn( viewer.getViewer( ).getTable( ), SWT.LEFT );
		column.setText( cellLabels[FILTER_INDEX] );
		column.setWidth( 200 );
		viewer.getViewer( )
				.setContentProvider( new IStructuredContentProvider( ) {

					public Object[] getElements( Object inputElement )
					{
						ArrayList computedColumnsList = new ArrayList( 10 );
						Iterator iter = computedColumns.iterator( );
						if ( iter != null )
						{
							while ( iter.hasNext( ) )
							{
								computedColumnsList.add( iter.next( ) );
							}
						}
						return computedColumnsList.toArray( );
					}

					public void dispose( )
					{

					}

					public void inputChanged( Viewer viewer, Object oldInput,
							Object newInput )
					{

					}
				} );
		viewer.getViewer( ).setLabelProvider( this );
		viewer.getViewer( ).setInput( computedColumns );
		addListeners( );
		setToolTips( );
		return viewer.getControl( );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.ui.dialogs.properties.IPropertyPage#pageActivated()
	 */
	public void pageActivated( )
	{
		refreshColumnNames( );
		getContainer( ).setMessage( Messages.getString( "dataset.editor.computedColumns" ), //$NON-NLS-1$
				IMessageProvider.NONE ); // $-NON-NLS-1
		// //$NON-NLS-1$
		setPageProperties( );

		computedColumns = ( (DataSetHandle) getContainer( ).getModel( ) ).getPropertyHandle( DataSetHandle.COMPUTED_COLUMNS_PROP );;
		viewer.getViewer( ).setInput( computedColumns );
		viewer.getViewer( ).getTable( ).select( 0 );
	}

	/**
	 * Refresh columns meta data
	 */
	private void refreshColumnNames( )
	{
		( (DataSetEditor) this.getContainer( ) ).getCurrentItemModel( true,
				true );
	}

	private void addListeners( )
	{
		viewer.getNewButton( ).addSelectionListener( new SelectionAdapter( ) {

			public void widgetSelected( SelectionEvent e )
			{
				doNew( );
			}
		} );

		viewer.getEditButton( ).addSelectionListener( new SelectionAdapter( ) {

			public void widgetSelected( SelectionEvent e )
			{
				doEdit( );
			}
		} );

		viewer.getViewer( ).getTable( ).addMouseListener( new MouseAdapter( ) {

			public void mouseDoubleClick( MouseEvent e )
			{
				doEdit( );
			}
		} );

		viewer.getViewer( ).getTable( ).addKeyListener( new KeyListener( ) {

			public void keyPressed( KeyEvent e )
			{
			}

			public void keyReleased( KeyEvent e )
			{
				if ( e.keyCode == SWT.DEL )
				{
					setPageProperties( );
				}
			}

		} );

		viewer.getRemoveButton( )
				.addSelectionListener( new SelectionListener( ) {

					public void widgetSelected( SelectionEvent e )
					{
						setPageProperties( );
						updateColumnsOfDataSetHandle( );
					}

					public void widgetDefaultSelected( SelectionEvent e )
					{
					}

				} );

		viewer.getRemoveMenuItem( )
				.addSelectionListener( new SelectionListener( ) {

					public void widgetSelected( SelectionEvent e )
					{
						setPageProperties( );
						updateColumnsOfDataSetHandle( );
					}

					public void widgetDefaultSelected( SelectionEvent e )
					{
					}

				} );

		viewer.getRemoveAllMenuItem( )
				.addSelectionListener( new SelectionListener( ) {

					public void widgetSelected( SelectionEvent e )
					{
						setPageProperties( );
						updateColumnsOfDataSetHandle( );
					}

					public void widgetDefaultSelected( SelectionEvent e )
					{
						widgetSelected( e );
					}
				} );

		viewer.getViewer( )
				.addSelectionChangedListener( new ViewerSelectionListener( ) );
	}

	private void doNew( )
	{
		doEdit( new ComputedColumn( ) );
	}

	private void doEdit( )
	{
		int index = viewer.getViewer( ).getTable( ).getSelectionIndex( );
		if ( index == -1 )
			return;

		ComputedColumnHandle handle = (ComputedColumnHandle) viewer.getViewer( )
				.getTable( )
				.getItem( index )
				.getData( );

		doEdit( handle );
	}

	private void doEdit( Object structureOrHandle )
	{
		ComputedColumnInputDialog dlg = new ComputedColumnInputDialog( structureOrHandle );

		if ( dlg.open( ) == Window.OK )
		{
			update( structureOrHandle );
		}
	}

	private void update( Object structureOrHandle )
	{
		if ( structureOrHandle instanceof ComputedColumn )
		{
			try
			{
				computedColumns.addItem( (ComputedColumn) structureOrHandle );
				viewer.getViewer( ).refresh( );
			}
			catch ( SemanticException e )
			{
				ExceptionHandler.handle( e );
			}
		}
		else
		{
			viewer.getViewer( ).update( structureOrHandle, null );
		}

		if ( validateAllComputedColumns( ) )
			getContainer( ).setMessage( Messages.getString( "dataset.editor.computedColumns" ), //$NON-NLS-1$
					IMessageProvider.NONE );
	}

	private ComputedColumn getStructure( Object structureOrHandle )
	{
		ComputedColumn structure = null;
		if ( structureOrHandle instanceof ComputedColumn )
		{
			structure = (ComputedColumn) structureOrHandle;
		}
		else
		{
			structure = (ComputedColumn) ( (ComputedColumnHandle) structureOrHandle ).getStructure( );
		}

		return structure;
	}

	private final String getTypeName( String typeDisplayName )
	{
		for ( int i = 0; i < dataTypes.length; i++ )
		{
			if ( dataTypes[i].getDisplayName( ).equals( typeDisplayName ) )
				return dataTypes[i].getName( );
		}

		return dataTypes[0].getName( );
	}

	private final String getTypeDisplayName( String typeName )
	{
		for ( int i = 0; i < dataTypes.length; i++ )
		{
			if ( dataTypes[i].getName( ).equals( typeName ) )
			{
				return dataTypes[i].getDisplayName( );
			}
		}

		return typeName;
	}

	private void setToolTips( )
	{
		viewer.getNewButton( )
				.setToolTipText( Messages.getString( "DataSetComputedColumnsPage.toolTipText.New" ) );//$NON-NLS-1$
		viewer.getEditButton( )
				.setToolTipText( Messages.getString( "DataSetComputedColumnsPage.toolTipText.Edit" ) );//$NON-NLS-1$
		viewer.getRemoveButton( )
				.setToolTipText( Messages.getString( "DataSetComputedColumnsPage.toolTipText.Remove" ) );//$NON-NLS-1$
		viewer.getUpButton( )
				.setToolTipText( Messages.getString( "DataSetComputedColumnsPage.toolTipText.Up" ) );//$NON-NLS-1$
		viewer.getDownButton( )
				.setToolTipText( Messages.getString( "DataSetComputedColumnsPage.toolTipText.Down" ) );//$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
	 *      int)
	 */
	public Image getColumnImage( Object element, int columnIndex )
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
	 *      int)
	 */
	public String getColumnText( Object element, int columnIndex )
	{
		String value = null;
		ComputedColumn computedColumn = getStructure( element );

		try
		{
			switch ( columnIndex )
			{
				case 0 :
				{
					value = computedColumn.getName( );
					break;
				}
				case 1 :
				{
					value = getTypeDisplayName( computedColumn.getDataType( ) );
					break;
				}
				case 2 :
				{
					// fetch the first argument as the expression value for
					// backward capability
					ComputedColumnHandle handle = (ComputedColumnHandle) computedColumn.getHandle( computedColumns );
					Iterator iterator = handle.argumentsIterator( );
					if ( iterator.hasNext( ) )
					{
						AggregationArgumentHandle argHandle = (AggregationArgumentHandle) iterator.next( );
						value = argHandle.getValue( );
					}
					if ( value == null )
					{
						value = computedColumn.getExpression( );
					}
					break;
				}
				case 3 :
				{
					value = computedColumn.getAggregateFunction( );
					IAggrFunction aggrFunc = aggregationManager.getAggregation( value );
					value = aggrFunc != null ? aggrFunc.getDisplayName( )
							: value;
					break;
				}
				case 4 :
				{
					value = computedColumn.getFilterExpression( );
					break;
				}

			}
		}
		catch ( Exception e )
		{
			ExceptionHandler.handle( e );
		}

		return ( value == null ? "" : value ); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void addListener( ILabelProviderListener listener )
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose( )
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object,
	 *      java.lang.String)
	 */
	public boolean isLabelProperty( Object element, String property )
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void removeListener( ILabelProviderListener listener )
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.ui.dialogs.properties.AbstractDescriptionPropertyPage#getPageDescription()
	 */
	public String getPageDescription( )
	{
		return Messages.getString( "DataSetComputedColumnsPage.description" ); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.ui.dialogs.properties.AbstractPropertyPage#performOk()
	 */
	public boolean performOk( )
	{
		if ( validateAllComputedColumns( ) )
		{
			return super.performOk( );
		}
		return false;
	}

	private class ViewerSelectionListener implements ISelectionChangedListener
	{

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
		 */
		public void selectionChanged( SelectionChangedEvent event )
		{
			setPageProperties( );
		}

	}

	/**
	 * Depending on the Computed the properties of various controls on this page
	 * are set
	 */
	private void setPageProperties( )
	{
		boolean computedColumnsExist = false;

		computedColumnsExist = ( computedColumns != null
				&& computedColumns.getListValue( ) != null && computedColumns.getListValue( )
				.size( ) > 0 );
		viewer.getEditButton( ).setEnabled( computedColumnsExist );
		viewer.getDownButton( ).setEnabled( computedColumnsExist
				&& computedColumns.getListValue( ).size( ) > 1 );
		viewer.getUpButton( ).setEnabled( computedColumnsExist
				&& computedColumns.getListValue( ).size( ) > 1 );
		viewer.getRemoveButton( ).setEnabled( computedColumnsExist );
		viewer.getRemoveMenuItem( ).setEnabled( computedColumnsExist );
		viewer.getRemoveAllMenuItem( ).setEnabled( computedColumnsExist );
		validateAllComputedColumns( );
	}

	/**
	 * Update the computed columns after removing an established one and cache
	 * the updated DataSetViewData[]
	 * 
	 */
	private void updateColumnsOfDataSetHandle( )
	{
		DataSetHandle dataSet = ( (DataSetEditor) getContainer( ) ).getHandle( );
		DataSetViewData[] items = DataSetProvider.getCurrentInstance( )
				.getColumns( dataSet, false, true, true );
		int inexistence = 0;
		for ( int i = 0; i < items.length; i++ )
		{
			boolean exist = false;
			if ( items[i].isComputedColumn( ) )
			{
				Iterator iter = computedColumns.iterator( );
				while ( iter.hasNext( ) )
				{
					ComputedColumn computedColumn = null;
					Object obj = iter.next( );
					if ( obj instanceof ComputedColumnHandle )
					{
						computedColumn = (ComputedColumn) ( (ComputedColumnHandle) obj ).getStructure( );
					}
					if ( items[i].getName( ).equals( computedColumn.getName( ) ) )
					{
						exist = true;
					}
				}
				if ( !exist )
				{
					items[i] = null;
					inexistence++;
				}
			}
		}
		if ( inexistence == 0 )
			return;

		DataSetViewData[] existItems = new DataSetViewData[items.length
				- inexistence];
		int index = 0;
		for ( int i = 0; i < items.length; i++ )
		{
			if ( items[i] != null )
			{
				existItems[index] = items[i];
				index++;
			}
		}
		DataSetProvider.getCurrentInstance( )
				.updateColumnsOfDataSetHandle( dataSet, existItems );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.ui.dialogs.properties.IPropertyPage#getToolTip()
	 */
	public String getToolTip( )
	{
		return Messages.getString( "DataSetComputedColumnsPage.ComputedColumns.Tooltip" ); //$NON-NLS-1$
	}

	public boolean canLeave( )
	{
		return validateAllComputedColumns( );
	}

	/*
	 * Exam whether all registered computed columns are valid. Set the message
	 * of dialog as appropriated
	 */
	private boolean validateAllComputedColumns( )
	{
		if ( computedColumns != null )
		{
			Iterator iter = computedColumns.iterator( );
			if ( iter != null )
			{
				while ( iter.hasNext( ) )
				{
					Object o = iter.next( );
					ComputedColumn computedColumn = null;
					if ( o instanceof ComputedColumnHandle )
					{
						computedColumn = (ComputedColumn) ( (ComputedColumnHandle) o ).getStructure( );
					}
					else
					{
						computedColumn = (ComputedColumn) o;
					}
					if ( !validateSingleColumn( computedColumn ) )
						return false;
				}
			}
		}
		return true;
	}

	/*
	 * Exam whether one computed column is valid or not.
	 */
	private boolean validateSingleColumn( ComputedColumn computedColumn )
	{
		if ( computedColumn.getName( ) == null
				|| computedColumn.getName( ).trim( ).length( ) == 0 )
		{
			getContainer( ).setMessage( Messages.getFormattedString( "dataset.editor.error.missingComputedColumnName", new Object[]{computedColumn.getName( )} ), IMessageProvider.ERROR ); //$NON-NLS-1$
			return false;
		}
//		if ( computedColumn.getExpression( ) == null
//				|| computedColumn.getExpression( ).trim( ).length( ) == 0 )
//		{
//			if ( isFunctionCount( computedColumn.getAggregateFunction( ) ) == false )
//			{
//				getContainer( ).setMessage( Messages.getFormattedString( "dataset.editor.error.missingComputedColumnExpression", new Object[]{computedColumn.getName( )} ), IMessageProvider.ERROR ); //$NON-NLS-1$
//				return false;
//			}
//		}

		Iterator iter = ( (DataSetHandle) getContainer( ).getModel( ) ).getPropertyHandle( DataSetHandle.COLUMN_HINTS_PROP )
				.iterator( );
		while ( iter.hasNext( ) )
		{
			ColumnHintHandle hint = (ColumnHintHandle) iter.next( );
			if ( computedColumn.getName( ).equals( hint.getAlias( ) ) )
			{
				getContainer( ).setMessage( Messages.getFormattedString( "dataset.editor.error.computedColumnNameAlreadyUsed", new Object[]{computedColumn.getName( )} ), IMessageProvider.ERROR ); //$NON-NLS-1$
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 
	 */
	private class DummyParamDefn implements IParameterDefn
	{
		String name;
		String displayName;
		String description = "";//$NON-NLS-1$
		boolean isDataField;
		boolean isOptional;

		/**
		 * 
		 * @param name
		 * @param displayName
		 * @param isOptional
		 * @param isDataField
		 */
		public DummyParamDefn( String name, String displayName,
				boolean isOptional, boolean isDataField )
		{
			this.name = name;
			this.displayName = displayName;
			this.isDataField = isDataField;
			this.isOptional = isOptional;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.data.engine.api.aggregation.IParameterDefn#getDescription()
		 */
		public String getDescription( )
		{
			return description;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.data.engine.api.aggregation.IParameterDefn#getDisplayName()
		 */
		public String getDisplayName( )
		{
			return displayName;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.data.engine.api.aggregation.IParameterDefn#isDataField()
		 */
		public boolean isDataField( )
		{
			return isDataField;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.data.engine.api.aggregation.IParameterDefn#isOptional()
		 */
		public boolean isOptional( )
		{
			return isOptional;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.data.engine.api.aggregation.IParameterDefn#supportDataType(int)
		 */
		public boolean supportDataType( int dataType )
		{
			return true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.data.engine.api.aggregation.IParameterDefn#getName()
		 */
		public String getName( )
		{
			return name;
		}
	}


	private class ComputedColumnInputDialog extends PropertyHandleInputDialog
	{

		private static final String BLANK = "";//$NON-NLS-1$
		private Text txtColumnName = null;
		private Combo cmbDataType = null;
		private Combo cmbAggregation = null;
		
		private Label lblFilter = null;
		private Text txtFilter = null;
		private Button btnFilter = null;
		private String columnName = null;
		
		private Text[] txtParams = null; // parameter controls
		
		private IAggrFunction[] functions = null;
		private Composite parameterContainer;
		private Composite composite;
		private String lastExpression;
		private Label firstLabel;

		/**
		 * 
		 * @param structureOrHandle
		 */
		private ComputedColumnInputDialog( Object structureOrHandle )
		{
			super( structureOrHandle );
			populateFunctions( );
		}

		/**
		 * 
		 */
		private void populateFunctions( )
		{
			List aggrList = aggregationManager.getAggregations( AggregationManager.AGGR_TABULAR );
			functions = new IAggrFunction[aggrList.size( )];
			aggrList.toArray( functions );
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.report.designer.data.ui.dataset.PropertyHandleInputDialog#createCustomControls(org.eclipse.swt.widgets.Composite)
		 */
		protected void createCustomControls( Composite parent )
		{
			this.composite = parent;
			
			createTextCell( parent, COLUMN_NAME_INDEX );
			createComboBoxCell( parent, DATA_TYPE_INDEX );
			createAggrListCell( parent, AGGREGATION_INDEX );
			createParameterContainer( parent );
			createFilterCell( parent, FILTER_INDEX );
			handleAggrSelectEvent( getStructureOrHandle() instanceof Structure );
			createSpaceForResize( parent );
		}

		/**
		 * @param parent
		 */
		private void createSpaceForResize( Composite parent )
		{
			Label space = ControlProvider.createLabel( parent, null );
			space.setLayoutData( ControlProvider.getGridDataWithHSpan( 3 ) );
			space = ControlProvider.createLabel( parent, null );
			space.setLayoutData( ControlProvider.getGridDataWithHSpan( 3 ) );
			space = ControlProvider.createLabel( parent, null );
			space.setLayoutData( ControlProvider.getGridDataWithHSpan( 3 ) );
			space = ControlProvider.createLabel( parent, null );
			space.setLayoutData( ControlProvider.getGridDataWithHSpan( 3 ) );
		}

		/**
		 * 
		 * @param parent
		 */
		private void createParameterContainer( Composite parent )
		{
			parameterContainer = new Composite( parent, SWT.NONE );
			GridData gridData = new GridData( GridData.FILL_HORIZONTAL
					| GridData.GRAB_HORIZONTAL );
			gridData.horizontalIndent = 0;
			gridData.horizontalSpan = 3;
			gridData.exclude = true;
			parameterContainer.setLayoutData( gridData );
			GridLayout layout = new GridLayout( );
			// layout.horizontalSpacing = layout.verticalSpacing = 0;
			layout.marginWidth = layout.marginHeight = 0;
			layout.numColumns = 3;
			parameterContainer.setLayout( layout );
		}

		
		/**
		 * 
		 * @param parent
		 * @param index
		 */
		private void createTextCell( Composite parent, final int index )
		{
			firstLabel = ControlProvider.createLabel( parent, dialogLabels[index] );

			txtColumnName = ControlProvider.createText( parent,
					(String) getProperty( getStructureOrHandle( ),
							cellProperties[index] ) );
			txtColumnName.setLayoutData( ControlProvider.getGridDataWithHSpan( 2 ) );
			columnName = txtColumnName.getText( );
			txtColumnName.addModifyListener( new ModifyListener( ) {

				public void modifyText( ModifyEvent e )
				{
					validateSyntax( );
				}
			} );
		}

		/**
		 * 
		 * @param parent
		 * @param index
		 */
		private void createComboBoxCell( Composite parent, final int index )
		{
			ControlProvider.createLabel( parent, dialogLabels[index] );

			cmbDataType = ControlProvider.createCombo( parent, SWT.READ_ONLY );
			cmbDataType.setLayoutData( ControlProvider.getGridDataWithHSpan( 2 ) );
			populateDataTypeComboItems( );
			cmbDataType.select( Utility.findIndex( cmbDataType.getItems( ),
					getTypeDisplayName( (String) getProperty( getStructureOrHandle( ),
							cellProperties[index] ) ) ) );
			cmbDataType.addSelectionListener( new SelectionAdapter( ) {

				public void widgetSelected( SelectionEvent e )
				{
					validateSyntax( );
				}

			} );
		}

		/**
		 * 
		 */
		private void populateDataTypeComboItems( )
		{
			Map<String, String> indexMap = new HashMap<String, String>( );
			String[] dataTypeDisplayNames = new String[dataTypes.length];
			for ( int i = 0; i < dataTypes.length; i++ )
			{
				dataTypeDisplayNames[i] = dataTypes[i].getDisplayName( );
				indexMap.put( dataTypeDisplayNames[i], dataTypes[i].getName( ) );
			}
			Arrays.sort( dataTypeDisplayNames );
			cmbDataType.setItems( dataTypeDisplayNames );
			for ( int i = 0; i < dataTypeDisplayNames.length; i++ )
			{
				String name = (String) indexMap.get( dataTypeDisplayNames[i] );
				cmbDataType.setData( name, new Integer( i ) );
			}
		}

		/**
		 * 
		 * @param parent
		 * @param index
		 */
		private void createAggrListCell( final Composite parent, final int index )
		{
			ControlProvider.createLabel( parent, dialogLabels[index] );

			cmbAggregation = ControlProvider.createCombo( parent, SWT.READ_ONLY );
			cmbAggregation.setLayoutData( ControlProvider.getGridDataWithHSpan( 2 ) );
			cmbAggregation.add( BLANK ); 
			cmbAggregation.setData( BLANK, new Integer( 0 ) );
			for ( int i = 0; i < functions.length; i++ )
			{
				cmbAggregation.add( functions[i].getDisplayName( ) );
				cmbAggregation.setData( functions[i].getName( ), new Integer( i + 1 ) );
			}

			String aggrFuncName = (String) getProperty( getStructureOrHandle( ),
					cellProperties[index] );
			int selectionIndex = getAggrFuncIndex( aggrFuncName );
			cmbAggregation.select( selectionIndex );
			
			cmbAggregation.setToolTipText( selectionIndex > 0
					? functions[selectionIndex - 1].getDescription( ) : BLANK );
			
			cmbAggregation.addSelectionListener( new SelectionAdapter( ) {
				
				public void widgetSelected( SelectionEvent e )
				{
					handleAggrSelectEvent( true );
					validateSyntax( );
					updateFilterUIStatus( );
				}
			}
			);
			
		}

		/**
		 * 
		 */
		private void handleAggrSelectEvent( boolean resetDataType )
		{
			disposeOldParameterUI( );
			IAggrFunction function = getSelectedFunction( );
			IParameterDefn[] params = null;
			if ( function != null )
			{
				params = function.getParameterDefn( );
			}
			else
			{
				params = new IParameterDefn[]{
					new DummyParamDefn( cellLabels[EXPRESSION_INDEX],
							dialogLabels[EXPRESSION_INDEX],
							false,
							true )
				};
			}
			if ( params.length > 0 )
			{
				showParameterUISection( );
				createParameterUISection( params );
			}
			else
			{
				hideParameterUISection( );
			}
			
			if( resetDataType )
				updateDataTypeCombo( );
			parameterContainer.layout( );
			composite.layout( );
		}

		/**
		 * @param params
		 */
		private void createParameterUISection( IParameterDefn[] params )
		{
			txtParams = new Text[params.length];
			for ( int i = 0; i < params.length; i++ )
			{
				IParameterDefn param = params[i];
				Label paramLabel = new Label( parameterContainer, SWT.NONE
						| SWT.WRAP );
				paramLabel.setText( Utility.getNonNullString( param.getDisplayName( ) ) );

				GridData gd = new GridData( );
				final int widthHint = firstLabel.computeSize( -1, -1 ).x
						- firstLabel.getBorderWidth( );
				gd.widthHint = widthHint;
				paramLabel.setLayoutData( gd );
				Composite composite = ControlProvider.getDefaultComposite( parameterContainer );
				if ( param.isDataField( ) )
				{
					String text = BLANK;
					if ( lastExpression != null )
					{
						text = lastExpression;
					}
					else
					{
						text = (String) getProperty( getStructureOrHandle( ),
								cellProperties[EXPRESSION_INDEX] );
					}
					txtParams[i] = ControlProvider.createText( composite, text );
					final Text txtDataField = txtParams[i];
					
					txtDataField.setLayoutData( ControlProvider.getGridDataWithHSpan( 1 ) );
					txtDataField.addModifyListener( new ModifyListener( ) {

						public void modifyText( ModifyEvent e )
						{
							lastExpression = txtDataField.getText( );
							validateSyntax( );
						}

					} );
				}
				else
				{
					Text txtArgument = ControlProvider.createText( composite,
							null );
					txtArgument.setLayoutData( ControlProvider.getGridDataWithHSpan( 1 ) );
					txtArgument.addModifyListener( new ModifyListener( ) {

						public void modifyText( ModifyEvent e )
						{
							validateSyntax( );
						}

					} );
					txtParams[i] = txtArgument;
				}
				txtParams[i].setToolTipText( param.getDescription( ) );
				createExpressionButton( composite, txtParams[i] );
			}
			
			// update parameters' values from ComputedColumnHandle
			updateParametersText( params );
		}

		/**
		 * 
		 */
		private void updateDataTypeCombo( )
		{
			final IAggrFunction aggrFunc = getSelectedFunction( );
			if ( aggrFunc != null )
			{
				String dataType = DataAdapterUtil.adapterToModelDataType( aggrFunc.getDataType( ) );
				Integer index = (Integer) cmbDataType.getData( dataType );
				cmbDataType.select( index != null ? index.intValue( ) : 0 );
			}
		}
		
		
		

		/**
		 * 
		 */
		private void disposeOldParameterUI( )
		{
			Control[] children = parameterContainer.getChildren( );
			for ( int i = 0; i < children.length; i++ )
			{
				children[i].dispose( );
			}
		}

		/**
		 * 
		 */
		private void hideParameterUISection( )
		{
			( (GridData) parameterContainer.getLayoutData( ) ).heightHint = 0;
		}

		/**
		 * 
		 */
		private void showParameterUISection( )
		{
			( (GridData) parameterContainer.getLayoutData( ) ).exclude = false;
			( (GridData) parameterContainer.getLayoutData( ) ).heightHint = SWT.DEFAULT;
		}

		/**
		 * update the arguments' UI elements from the compute column handle.
		 */
		private void updateParametersText( IParameterDefn[] params )
		{
			if ( params.length == 0 )
				return;
			Object handle = getStructureOrHandle( );
			if ( handle instanceof ComputedColumnHandle )
			{
				ComputedColumnHandle cHandle = (ComputedColumnHandle) handle;
				String expr = cHandle.getExpression( );
				if ( expr != null )
				{
					txtParams[0].setText( expr );
				}
				else
				{
					Iterator itr = cHandle.argumentsIterator( );
					List argHandles = new ArrayList( );
					while ( itr.hasNext( ) )
					{
						argHandles.add( itr.next( ) );
					}
					int i = 0;
					if ( params[0].isDataField( ) && lastExpression != null )
					{
						txtParams[0].setText( lastExpression );
						i++;
					}
					for ( ; i < params.length; i++ )
					{
						AggregationArgumentHandle argHandle = null;
						if ( i < argHandles.size( ) )
						{
							argHandle = (AggregationArgumentHandle) argHandles.get( i );
						}
						final String value = argHandle != null
								? argHandle.getValue( ) : null;
						txtParams[i].setText( value != null ? value : BLANK );
					}
				}
			}
		}

		/**
		 * 
		 * @param aggrFuncName
		 * @return
		 */
		private int getAggrFuncIndex( String aggrFuncName )
		{
			Integer selectionIndex = (Integer) cmbAggregation.getData( aggrFuncName != null
					? aggrFuncName : BLANK );
			return selectionIndex != null ? selectionIndex.intValue( ) : 0;
		}

		/**
		 * @return whether filter component should be enabled
		 */
		private boolean needFilter( )
		{
			if ( cmbAggregation.getText( ) != null
					&& cmbAggregation.getText( ).trim( ).length( ) > 0 )
			{
				return true;
			}
			else
			{
				return false;
			}
		}

		/**
		 * Update the Filter component status
		 */
		private void updateFilterUIStatus( )
		{
			if ( needFilter( ) )
			{
				txtFilter.setEnabled( true );
				lblFilter.setEnabled( true );
				btnFilter.setEnabled( true );
			}
			else
			{
				txtFilter.setText( BLANK );
				txtFilter.setEnabled( false );
				lblFilter.setEnabled( false );
				btnFilter.setEnabled( false );
			}
		}



		/**
		 * 
		 * @param parent
		 * @param index
		 */
		private void createFilterCell( Composite parent, final int index )
		{
			lblFilter = ControlProvider.createLabel( parent,
					dialogLabels[index] );

			Composite composite = ControlProvider.getDefaultComposite( parent );
			Object handle = this.getStructureOrHandle( );
			if ( handle instanceof ComputedColumnHandle )
			{
				ComputedColumnHandle cHandle = (ComputedColumnHandle) handle;
				txtFilter = ControlProvider.createText( composite,
						cHandle.getFilterExpression( ) );

			}
			else
				txtFilter = ControlProvider.createText( composite,
						(String) getProperty( getStructureOrHandle( ),
								cellProperties[index] ) );
			txtFilter.setLayoutData( ControlProvider.getGridDataWithHSpan( 1 ) );
			txtFilter.setEnabled( false );
			txtFilter.addModifyListener( new ModifyListener( ) {

				public void modifyText( ModifyEvent e )
				{
					validateSyntax( );
				}

			} );

			SelectionAdapter listener = new SelectionAdapter( ) {

				public void widgetSelected( SelectionEvent event )
				{
					ExpressionBuilder expressionBuilder = new ExpressionBuilder( txtFilter.getText( ) );
					expressionBuilder.setExpressionProvier( new DataSetExpressionProvider( (DesignElementHandle) getContainer( ).getModel( ) ) );
					String expression = txtFilter == null ? null
							: txtFilter.getText( ); 
					setExprBuilderDefaultSelection( expressionBuilder, expression );

					if ( expressionBuilder.open( ) == OK )
					{
						txtFilter.setText( expressionBuilder.getResult( ).trim( ) );
					}
				}
			};

			btnFilter = ControlProvider.createButton( composite,
					SWT.PUSH,
					listener );
			updateFilterUIStatus( );
		}


		/**
		 * 
		 * @param composite
		 * @param text 
		 * @return
		 */
		private Button createExpressionButton( Composite composite, final Text text )
		{
			SelectionAdapter listener = new SelectionAdapter( ) {

				public void widgetSelected( SelectionEvent event )
				{
					ExpressionBuilder expressionBuilder = new ExpressionBuilder( text.getText( ) );
					expressionBuilder.setExpressionProvier( new DataSetExpressionProvider( (DesignElementHandle) getContainer( ).getModel( ) ) );
					String expression = ( txtParams.length > 0 && txtParams[0] != null )
							? txtParams[0].getText( ) : null;
					setExprBuilderDefaultSelection( expressionBuilder,
							expression );

					if ( expressionBuilder.open( ) == OK )
					{
						text.setText( expressionBuilder.getResult( )
								.trim( ) );
					}
				}
			};

			return ControlProvider.createButton( composite, SWT.PUSH, listener );
		}

		/**
		 * Set the default selection in the expression builder
		 * 
		 * @param expressionBuilder
		 */
		private void setExprBuilderDefaultSelection(
				ExpressionBuilder expressionBuilder, String expression )
		{
			Object[] selection = null;
			String dataSets = DataSetExpressionProvider.DATASETS;
			DataSetHandle dsHandle = (DataSetHandle) getContainer( ).getModel( );
			Object handle = this.getStructureOrHandle( );
			if ( handle instanceof ComputedColumnHandle )
			{
				try
				{
					String columnName = ExpressionUtil.getColumnBindingName( expression );
					DataSetViewData viewData = findDataSetViewData( columnName,
							dsHandle );
					selection = new Object[]{
							dataSets, dsHandle, viewData
					};
				}
				catch ( BirtException e )
				{
					selection = new Object[]{ dataSets, dsHandle };
				}
			}
			else
			{
				selection = new Object[]{ dataSets, dsHandle };
			}
			expressionBuilder.setDefaultSelection( selection );
		}

		/**
		 * Finds the DataSetViewData instance according to the given column name
		 * 
		 * @param columnName
		 * @param handle
		 * @return
		 */
		private DataSetViewData findDataSetViewData( String columnName,
				DataSetHandle handle )
		{
			if ( columnName == null || columnName.trim( ).length( ) == 0 )
			{
				return null;
			}
			DataSetViewData[] items = DataSetProvider.getCurrentInstance( )
					.getColumns( handle, false, true, true );
			for ( int i = 0; i < items.length; i++ )
			{
				if ( columnName.equals( items[i].getName( ) ) )
				{
					return items[i];
				}
			}
			return null;
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.report.designer.data.ui.dataset.PropertyHandleInputDialog#validateSemantics(java.lang.Object)
		 */
		protected IStatus validateSemantics( Object structureOrHandle )
		{
			AggregationArgument aggrArgument = null;
			String columnName = txtColumnName.getText( );
			String dataTypeName = getTypeName( cmbDataType.getText( ) );
			IAggrFunction aggrFunc = getSelectedFunction( );

			try
			{
				String funcName = aggrFunc != null ? aggrFunc.getName( )
						: BLANK;
				String filter = this.txtFilter.getText( );
				if ( structureOrHandle instanceof ComputedColumnHandle )
				{
					ComputedColumnHandle handle = (ComputedColumnHandle) structureOrHandle;
					handle.setName( columnName );
					handle.setDataType( dataTypeName );
					handle.setAggregateFunction( funcName );
					if ( aggrFunc == null )
					{
						handle.setExpression( txtParams[0].getText( ).trim( ) );
					}
					else
					{
						handle.setExpression( BLANK );
						handle.setFilterExpression( filter );
						handle.clearArgumentList( );
						IParameterDefn[] params = aggrFunc.getParameterDefn( );
						for ( int i = 0; i < params.length; i++ )
						{
							if( txtParams[i].getText( ).trim( ).length( )== 0)
								continue;
							aggrArgument = StructureFactory.createAggregationArgument( );
							aggrArgument.setName( params[i].getName( ) );
							aggrArgument.setValue( txtParams[i].getText( )
									.trim( ) );
							handle.addArgument( aggrArgument );
						}
					}
				}
				else if ( structureOrHandle instanceof ComputedColumn )
				{
					ComputedColumn handle = (ComputedColumn) structureOrHandle;
					handle.setName( columnName );
					handle.setDataType( dataTypeName );
					handle.setAggregateFunction( funcName );
					if ( aggrFunc == null )
					{
						handle.setExpression( txtParams[0].getText( ).trim( ) );
					}
					else
					{
						handle.setExpression( BLANK );
						handle.setFilterExpression( filter );
						IParameterDefn[] params = aggrFunc.getParameterDefn( );
						for ( int i = 0; i < params.length; i++ )
						{
							if( txtParams[i].getText( ).trim( ).length( )== 0)
								continue;
							aggrArgument = StructureFactory.createAggregationArgument( );
							aggrArgument.setName( params[i].getName( ) );
							aggrArgument.setValue( txtParams[i].getText( )
									.trim( ) );
							handle.addArgument( aggrArgument );
						}
					}
					updateComputedColumns( handle );
				}
			}
			catch ( Exception e )
			{
				Status status = new Status( IStatus.ERROR,
						ReportPlugin.REPORT_UI,
						BLANK,
						e );
				return status;
			}
			return getOKStatus( );
		}

		/**
		 * 
		 * @return
		 */
		private IAggrFunction getSelectedFunction( )
		{
			int index = cmbAggregation.getSelectionIndex( );
			return index > 0 ? functions[index - 1] : null;
		}

		/**
		 * Update the computed columns after adding a new column and cache the
		 * updated DataSetViewData[]
		 * 
		 */
		private void updateComputedColumns( ComputedColumn handle )
		{
			DataSetHandle dataSet = ( (DataSetEditor) getContainer( ) ).getHandle( );
			DataSetViewData[] items = DataSetProvider.getCurrentInstance( )
					.getColumns( dataSet, false, true, true );
			int count = items.length;
			DataSetViewData[] newItems = new DataSetViewData[count + 1];
			System.arraycopy( items, 0, newItems, 0, count );
			newItems[count] = new DataSetViewData( );
			newItems[count].setName( handle.getName( ) );
			newItems[count].setDataTypeName( handle.getDataType( ) );
			newItems[count].setAlias( handle.getDisplayName( ) );
			newItems[count].setComputedColumn( true );
			newItems[count].setPosition( count + 1 );
			newItems[count].setDataType( DataAdapterUtil.adaptModelDataType( handle.getDataType( ) ) );

			DataSetProvider.getCurrentInstance( )
					.updateColumnsOfDataSetHandle( dataSet, newItems );
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.report.designer.data.ui.dataset.PropertyHandleInputDialog#validateSyntax(java.lang.Object)
		 */
		protected IStatus validateSyntax( Object structureOrHandle )
		{
			// duplicated columnName check
			if ( !isUniqueColumnName( ) )
				return getMiscStatus( IStatus.ERROR,
						Messages.getString( "DataSetComputedColumnsPage.duplicatedName" ) ); //$NON-NLS-1$

			// blankProperty check
			if ( isBlankProperty( txtColumnName.getText( ) ) )
				return getBlankPropertyStatus( dialogLabels[COLUMN_NAME_INDEX] );
			if ( isBlankProperty( cmbDataType.getText( ) ) )
				return getBlankPropertyStatus( dialogLabels[DATA_TYPE_INDEX] );
//			if ( expression!=null && isBlankProperty( expression.getText( ) ) )
//			{
//				String funcName = getSelectedFunction( ).getName( );
//				if ( isFunctionCount( funcName ) == false )
//					return getBlankPropertyStatus( cellLabels[EXPRESSION_INDEX] );
//			}
			// validate arguments
			IAggrFunction aggrFunc = getSelectedFunction( );
			if ( aggrFunc != null )
			{
				IParameterDefn[] paramDefns = aggrFunc.getParameterDefn( );
				for ( int i = 0; i < paramDefns.length; i++ )
				{
					if ( !paramDefns[i].isOptional( )
							&& isBlankProperty( txtParams[i].getText( ) ) )
					{
						return getBlankPropertyStatus( paramDefns[i].getDisplayName( ) );
					}
				}
			}
			else
			{
				if ( txtParams != null
						&& isBlankProperty( txtParams[0].getText( ) ) )
				{
					return getBlankPropertyStatus( dialogLabels[EXPRESSION_INDEX] );
				}
			}
			return getOKStatus( );
		}

		/**
		 * 
		 * @return
		 */
		private final boolean isUniqueColumnName( )
		{
			DataSetViewData[] items = DataSetProvider.getCurrentInstance( )
					.getColumns( ( (DataSetEditor) getContainer( ) ).getHandle( ),
							false,
							true,
							true );

			for ( int i = 0; i < items.length; i++ )
			{
				if ( !items[i].getName( ).equals( columnName ) )
				{
					if ( !items[i].isComputedColumn( ) )
					{
						if ( ( items[i].getAlias( ) != null && items[i].getAlias( )
								.equals( txtColumnName.getText( ) ) )
								|| ( items[i].getName( ) != null && items[i].getName( )
										.equals( txtColumnName.getText( ) ) ) )
						{
							return false;
						}
					}
				}

			}
			Iterator iter = computedColumns.iterator( );
			if ( iter != null )
			{
				while ( iter.hasNext( ) )
				{
					ComputedColumnHandle handle = (ComputedColumnHandle) iter.next( );
					if ( getStructure( getStructureOrHandle( ) ) != handle.getStructure( )
							&& handle.getName( ).equals( txtColumnName.getText( ) ) )
					{
						return false;
					}
				}
			}
			Iterator iter2 = ( (DataSetHandle) getContainer( ).getModel( ) ).getPropertyHandle( DataSetHandle.COLUMN_HINTS_PROP )
					.iterator( );
			while ( iter2.hasNext( ) )
			{
				ColumnHintHandle hint = (ColumnHintHandle) iter2.next( );
				if ( !hint.getColumnName( ).equals( columnName ) )
				{
					if ( txtColumnName.getText( ).equals( hint.getAlias( ) )
							|| txtColumnName.getText( )
									.equals( hint.getColumnName( ) ) )
					{
						return false;
					}
				}

			}

			return true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.report.designer.data.ui.dataset.PropertyHandleInputDialog#rollback()
		 */
		protected void rollback( )
		{
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.report.designer.data.ui.dataset.PropertyHandleInputDialog#getTitle()
		 */
		protected String getTitle( )
		{
			if ( this.getStructureOrHandle( ) instanceof Structure )
			{
				return Messages.getString( "DataSetComputedColumnsPage.toolTipText.New" ); //$NON-NLS-1$
			}
			return Messages.getString( "DataSetComputedColumnsPage.toolTipText.Edit" );//$NON-NLS-1$
		}

	}

}

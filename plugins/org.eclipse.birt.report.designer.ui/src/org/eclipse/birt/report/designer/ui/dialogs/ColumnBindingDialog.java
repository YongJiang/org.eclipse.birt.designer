/*******************************************************************************
 * Copyright (c) 2004, 2007 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.report.designer.ui.dialogs;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.birt.core.data.ExpressionUtil;
import org.eclipse.birt.report.designer.core.model.SessionHandleAdapter;
import org.eclipse.birt.report.designer.data.ui.dataset.DataSetUIUtil;
import org.eclipse.birt.report.designer.internal.ui.dialogs.BaseDialog;
import org.eclipse.birt.report.designer.internal.ui.dialogs.DataColumnBindingDialog;
import org.eclipse.birt.report.designer.internal.ui.dialogs.IBindingDialogHelper;
import org.eclipse.birt.report.designer.internal.ui.util.DataUtil;
import org.eclipse.birt.report.designer.internal.ui.util.ExceptionHandler;
import org.eclipse.birt.report.designer.internal.ui.util.IHelpContextIds;
import org.eclipse.birt.report.designer.internal.ui.util.UIUtil;
import org.eclipse.birt.report.designer.internal.ui.util.WidgetUtil;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.designer.ui.views.ElementAdapterManager;
import org.eclipse.birt.report.designer.ui.views.attributes.providers.ChoiceSetFactory;
import org.eclipse.birt.report.designer.util.DEUtil;
import org.eclipse.birt.report.model.api.CachedMetaDataHandle;
import org.eclipse.birt.report.model.api.CommandStack;
import org.eclipse.birt.report.model.api.ComputedColumnHandle;
import org.eclipse.birt.report.model.api.DataItemHandle;
import org.eclipse.birt.report.model.api.DataSetHandle;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.GroupHandle;
import org.eclipse.birt.report.model.api.ImageHandle;
import org.eclipse.birt.report.model.api.ReportItemHandle;
import org.eclipse.birt.report.model.api.ResultSetColumnHandle;
import org.eclipse.birt.report.model.api.StructureFactory;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.eclipse.birt.report.model.api.elements.structures.ComputedColumn;
import org.eclipse.birt.report.model.api.metadata.IChoiceSet;
import org.eclipse.birt.report.model.api.metadata.PropertyValueException;
import org.eclipse.birt.report.model.api.util.StringUtil;
import org.eclipse.birt.report.model.elements.interfaces.IReportItemModel;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * The dialog to select and edit column bindings
 */

public class ColumnBindingDialog extends BaseDialog
{

	public static class BindingInfo
	{

		private int bindingType;
		private Object bindingValue;

		public BindingInfo( )
		{
		}

		public BindingInfo( int type, Object value )
		{
			this.bindingType = type;
			this.bindingValue = value;
		}

		public int getBindingType( )
		{
			return bindingType;
		}

		public Object getBindingValue( )
		{
			return bindingValue;
		}

		public void setBindingType( int bindingType )
		{
			this.bindingType = bindingType;
		}

		public void setBindingValue( Object bindingValue )
		{
			this.bindingValue = bindingValue;
		}
	}

	private static final String ALL = Messages.getString( "ColumnBindingDialog.All" );//$NON-NLS-1$

	private static final String CHOICE_DATASET_FROM_CONTAINER = Messages.getString( "ColumnBindingDialog.Choice.DatasetFromContainer" );//$NON-NLS-1$

	private static final String CHOICE_NONE = Messages.getString( "ColumnBindingDialog.NONE" );//$NON-NLS-1$

	private static final String CHOICE_REPORTITEM_FROM_CONTAINER = Messages.getString( "ColumnBindingDialog.Choice.ReportItemFromContainer" );//$NON-NLS-1$

	private static final String COLUMN_AGGREGATEON = Messages.getString( "ColumnBindingDialog.Column.AggregateOn" ); //$NON-NLS-1$

	private static final String COLUMN_FILTER = Messages.getString( "ColumnBindingDialog.Column.Filter" ); //$NON-NLS-1$

	private static final String COLUMN_DATATYPE = Messages.getString( "ColumnBindingDialog.Column.DataType" ); //$NON-NLS-1$

	private static final String COLUMN_DISPLAYNAME = Messages.getString( "ColumnBindingDialog.Column.DisplayName" ); //$NON-NLS-1$

	private static final String COLUMN_EXPRESSION = Messages.getString( "ColumnBindingDialog.Column.Expression" ); //$NON-NLS-1$

	// private static final String BUTTON_GENERATE = Messages.getString(
	// "ColumnBindingDialog.Button.Generate" ); //$NON-NLS-1$
	private static final String COLUMN_NAME = Messages.getString( "ColumnBindingDialog.Column.Name" ); //$NON-NLS-1$

	private static final IChoiceSet DATA_TYPE_CHOICE_SET = DEUtil.getMetaDataDictionary( )
			.getStructure( ComputedColumn.COMPUTED_COLUMN_STRUCT )
			.getMember( ComputedColumn.DATA_TYPE_MEMBER )
			.getAllowedChoices( );

	public static final String DEFAULT_DLG_TITLE = Messages.getString( "ColumnBindingDialog.DialogTitle" ); //$NON-NLS-1$

	private static final String dummyChoice = "dummy"; //$NON-NLS-1$

	private static final String INPUT_PROPMT = Messages.getString( "ColumnBindingDialog.InputPrompt" ); //$NON-NLS-1$

	private static final String LABEL_COLUMN_BINDINGS = Messages.getString( "ColumnBindingDialog.Label.DataSet" ); //$NON-NLS-1$

	private static final String MSG_ADD = Messages.getString( "ColumnBindingDialog.Text.Add" ); //$NON-NLS-1$

	private static final String MSG_DELETE = Messages.getString( "ColumnBindingDialog.Text.Del" ); //$NON-NLS-1$

	private static final String MSG_REFRESH = Messages.getString( "ColumnBindingDialog.Text.Refresh" ); //$NON-NLS-1$

	private static final String MSG_ADDAGGREGATEON = Messages.getString( "ColumnBindingDialog.Text.AddAggr" ); //$NON-NLS-1$

	private static final String MSG_EDIT = Messages.getString( "ColumnBindingDialog.Text.Edit" ); //$NON-NLS-1$

	private static final String NONE_AGGREGATEON = Messages.getString( "ColumnBindingDialog.AGGREGATEON.NONE" );//$NON-NLS-1$

	private static final String WARN_COLUMN_BINDINGS = Messages.getString( "ColumnBingingDialog.Label.Warn" ); //$NON-NLS-1$

	// private Button generateButton;
	protected TableViewer bindingTable;

	protected Button btnAdd;

	// private List bindingList;

	protected Button btnDel;

	protected Button btnEdit;

	private boolean canAggregate = false;

	private boolean canSelect = false;

	private Composite composite;

	private IStructuredContentProvider contentProvider = new IStructuredContentProvider( ) {

		public void dispose( )
		{
		}

		public Object[] getElements( Object inputElement )
		{
			List elementsList = getBindingList( (DesignElementHandle) inputElement );
			// elementsList.add( dummyChoice );
			return elementsList.toArray( );
		}

		public void inputChanged( Viewer viewer, Object oldInput,
				Object newInput )
		{
		}
	};

	private Combo datasetCombo;

	private Button datasetRadio;

	private transient boolean enableAutoCommit = false;

	protected ExpressionProvider expressionProvider;

	private List groupList = Collections.EMPTY_LIST;

	private String[] groups;

	protected ReportItemHandle inputElement;

	private boolean isDataSetVisible;

	private ITableLabelProvider labelProvider = new ITableLabelProvider( ) {

		public void addListener( ILabelProviderListener listener )
		{
		}

		public void dispose( )
		{
		}

		public Image getColumnImage( Object element, int columnIndex )
		{
			return null;
		}

		public String getColumnText( Object element, int columnIndex )
		{
			if ( element == dummyChoice )
			{
				if ( columnIndex == 1 )
				{
					return INPUT_PROPMT;
				}
				return ""; //$NON-NLS-1$
			}
			ComputedColumnHandle handle = ( (ComputedColumnHandle) element );
			String text = null;

			switch ( columnIndex )
			{
				case 1 :
					text = handle.getName( );
					break;
				case 2 :
					text = handle.getDisplayName( );
					break;
				case 3 :
					text = ChoiceSetFactory.getDisplayNameFromChoiceSet( handle.getDataType( ),
							DATA_TYPE_CHOICE_SET );
					break;
				case 4 :
					text = org.eclipse.birt.report.designer.data.ui.util.DataUtil.getAggregationExpression( handle );
					break;
				case 5 :
					text = handle.getFilterExpression( );
					break;
				case 6 :
					String value = DEUtil.getAggregateOn( handle );
					String groupType = DEUtil.getGroupControlType( inputElement );
					if ( value == null )
					{
						if ( ExpressionUtil.hasAggregation( handle.getExpression( ) )
								&& groupType != DEUtil.TYPE_GROUP_NONE )
						{
							text = ALL;
						}
						else
							text = NONE_AGGREGATEON;
					}
					else
					{
						text = value;
					}

					break;
			}

			if ( text == null )
			{
				text = ""; //$NON-NLS-1$
			}
			return text;
		}

		public boolean isLabelProperty( Object element, String property )
		{
			return false;
		}

		public void removeListener( ILabelProviderListener listener )
		{
		}

	};

	private String NullDatasetChoice = null;

	private String NullReportItemChoice = null;

	private Combo reportItemCombo;

	private Button reportItemRadio;

	private String selectedColumnName = null;

	private int selectIndex;

	private CLabel warnLabel;

	public ColumnBindingDialog( ReportItemHandle input )
	{
		super( DEFAULT_DLG_TITLE );
		setInput( input );
	}

	public ColumnBindingDialog( ReportItemHandle input, boolean canSelect )
	{
		super( DEFAULT_DLG_TITLE );
		setInput( input );
		this.canSelect = canSelect;

	}

	public ColumnBindingDialog( ReportItemHandle input, Shell parent,
			boolean canSelect )
	{
		this( input, parent, DEFAULT_DLG_TITLE, canSelect, true );
	}

	public ColumnBindingDialog( ReportItemHandle input, Shell parent,
			boolean canSelect, boolean canAggregate )
	{
		this( input, parent, DEFAULT_DLG_TITLE, canSelect, canAggregate );
	}

	public ColumnBindingDialog( ReportItemHandle input, Shell parent,
			String title, boolean canSelect, boolean canAggregate )
	{
		super( parent, title );
		setInput( input );
		this.canSelect = canSelect;
		this.canAggregate = canAggregate;
	}

	public ColumnBindingDialog( ReportItemHandle input, String title )
	{
		super( title );
		setInput( input );
	}

	public ColumnBindingDialog( ReportItemHandle input, String title,
			boolean canAggregate )
	{
		super( title );
		setInput( input );
		this.canAggregate = canAggregate;
	}

	protected void addBinding( ComputedColumn column )
	{
		try
		{
			DEUtil.addColumn( DEUtil.getBindingHolder( inputElement ),
					column,
					false );
		}
		catch ( SemanticException e )
		{
			ExceptionHandler.handle( e );
		}
	}

	/**
	 * Adds buttons in Button area.
	 * 
	 * @param cmp
	 *            parent composite
	 * @param table
	 *            the Table widget affected by Buttons
	 * @return the number of added buttons
	 */
	protected int addButtons( Composite cmp, final Table table )
	{
		Button btnRefresh = new Button( cmp, SWT.PUSH );
		btnRefresh.setText( MSG_REFRESH ); //$NON-NLS-1$
		GridData data = new GridData( GridData.VERTICAL_ALIGN_BEGINNING );
		data.widthHint = Math.max( 60, btnRefresh.computeSize( SWT.DEFAULT,
				SWT.DEFAULT,
				true ).x );
		btnRefresh.setLayoutData( data );
		btnRefresh.addListener( SWT.Selection, new Listener( ) {

			public void handleEvent( Event event )
			{
				if ( inputElement != null )
				{
					DataSetHandle datasetHandle = inputElement.getDataSet( );
					if ( datasetHandle != null )
					{
						try
						{
							CachedMetaDataHandle cmdh = DataSetUIUtil.getCachedMetaDataHandle( datasetHandle );
							for ( Iterator iter = cmdh.getResultSet( )
									.iterator( ); iter.hasNext( ); )
							{
								ResultSetColumnHandle element = (ResultSetColumnHandle) iter.next( );
								ComputedColumn bindingColumn = StructureFactory.newComputedColumn( inputElement,
										element.getColumnName( ) );
								bindingColumn.setDataType( element.getDataType( ) );
								bindingColumn.setExpression( DEUtil.getExpression( element ) );

								inputElement.addColumnBinding( bindingColumn,
										false );

							}
						}
						catch ( SemanticException e )
						{
							ExceptionHandler.handle( e );
						}
						refreshBindingTable( );
						updateButtons( );
					}
				}
			}
		} );
		return 1;
	}

	private void commit( )
	{
		if ( isEnableAutoCommit( ) )
		{
			getActionStack( ).commit( );
		}
	}

	protected Control createDialogArea( Composite parent )
	{

		UIUtil.bindHelp( parent, IHelpContextIds.COLUMNBINDING_DIALOG_ID );
		Composite parentComposite = (Composite) super.createDialogArea( parent );

		if ( this.canSelect )
		{
			composite = new Composite( parentComposite, SWT.NONE );
			composite.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
			composite.setLayout( UIUtil.createGridLayoutWithoutMargin( 2, false ) );

			warnLabel = new CLabel( composite, SWT.NONE );
			warnLabel.setImage( PlatformUI.getWorkbench( )
					.getSharedImages( )
					.getImage( ISharedImages.IMG_OBJS_WARN_TSK ) );
			warnLabel.setText( WARN_COLUMN_BINDINGS );
			GridData gd = new GridData( GridData.FILL_HORIZONTAL );
			gd.horizontalSpan = 2;
			warnLabel.setLayoutData( gd );

			datasetRadio = new Button( composite, SWT.RADIO );
			datasetRadio.setText( LABEL_COLUMN_BINDINGS );
			datasetRadio.setLayoutData( new GridData( GridData.BEGINNING ) );
			datasetRadio.addSelectionListener( new SelectionAdapter( ) {

				public void widgetSelected( SelectionEvent e )
				{
					refreshBinding( );
					if ( datasetRadio.getSelection( )
							&& inputElement.getDataBindingType( ) == ReportItemHandle.DATABINDING_TYPE_REPORT_ITEM_REF
							&& ( DEUtil.getBindingHolder( inputElement, true ) == null || DEUtil.getBindingHolder( inputElement,
									true )
									.getDataBindingType( ) != ReportItemHandle.DATABINDING_TYPE_REPORT_ITEM_REF ) )
						saveBinding( );
				}

			} );

			datasetCombo = new Combo( composite, SWT.READ_ONLY | SWT.BORDER );
			datasetCombo.setBackground( PlatformUI.getWorkbench( )
					.getDisplay( )
					.getSystemColor( SWT.COLOR_LIST_BACKGROUND ) );
			String[] dataSets = ChoiceSetFactory.getDataSets( );
			String[] newList = new String[dataSets.length + 1];
			newList[0] = NullDatasetChoice;
			System.arraycopy( dataSets, 0, newList, 1, dataSets.length );
			datasetCombo.setItems( newList );
			String dataSetName = getDataSetName( );
			datasetCombo.deselectAll( );

			if ( dataSetName != null )
			{
				datasetCombo.setText( dataSetName );
			}
			else
			{
				datasetCombo.select( 0 );
			}
			datasetCombo.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
			gd = new GridData( );
			gd.widthHint = 250;
			datasetCombo.setLayoutData( gd );
			datasetCombo.addSelectionListener( new SelectionAdapter( ) {

				public void widgetSelected( SelectionEvent event )
				{
					saveBinding( );
				}
			} );

			reportItemRadio = new Button( composite, SWT.RADIO );
			reportItemRadio.setText( Messages.getString( "BindingPage.ReportItem.Label" ) ); //$NON-NLS-1$
			reportItemRadio.addSelectionListener( new SelectionAdapter( ) {

				public void widgetSelected( SelectionEvent e )
				{
					refreshBinding( );
					if ( reportItemRadio.getSelection( )
							&& inputElement.getDataBindingType( ) == ReportItemHandle.DATABINDING_TYPE_DATA
							&& ( DEUtil.getBindingHolder( inputElement, true ) == null || DEUtil.getBindingHolder( inputElement,
									true )
									.getDataBindingType( ) == ReportItemHandle.DATABINDING_TYPE_REPORT_ITEM_REF ) )
						saveBinding( );
				}
			} );
			reportItemCombo = new Combo( composite, SWT.READ_ONLY | SWT.BORDER );
			reportItemCombo.setBackground( PlatformUI.getWorkbench( )
					.getDisplay( )
					.getSystemColor( SWT.COLOR_LIST_BACKGROUND ) );
			gd = new GridData( );
			gd.widthHint = 250;
			reportItemCombo.setLayoutData( gd );
			reportItemCombo.addSelectionListener( new SelectionAdapter( ) {

				public void widgetSelected( SelectionEvent e )
				{
					saveBinding( );
				}
			} );
		}

		Composite contentComposite = new Composite( parentComposite, SWT.NONE );
		contentComposite.setLayoutData( new GridData( GridData.FILL_BOTH ) );
		contentComposite.setLayout( UIUtil.createGridLayoutWithoutMargin( 2,
				false ) );
		/**
		 * Binding table
		 */
		final Table table = new Table( contentComposite, SWT.SINGLE
				| SWT.FULL_SELECTION
				| SWT.BORDER
				| ( canSelect ? SWT.CHECK : 0 ) );
		GridData gd = new GridData( GridData.FILL_BOTH );
		gd.heightHint = 200;
		gd.verticalSpan = 5;
		table.setLayoutData( gd );
		table.setLinesVisible( true );
		table.setHeaderVisible( true );
		// table.addKeyListener( new KeyAdapter( ) {
		//
		// /**
		// * @see
		// org.eclipse.swt.events.KeyAdapter#keyReleased(org.eclipse.swt.events.KeyEvent)
		// */
		// public void keyReleased( KeyEvent e )
		// {
		// // If Delete pressed, delete the selected row
		// if ( e.keyCode == SWT.DEL )
		// {
		// IStructuredSelection selection = (IStructuredSelection)
		// bindingTable.getSelection( );
		// if ( selection.getFirstElement( ) instanceof ComputedColumnHandle )
		// {
		// deleteRow( (ComputedColumnHandle) selection.getFirstElement( ) );
		// }
		// }
		// }
		// } );

		table.addKeyListener( new KeyAdapter( ) {

			public void keyPressed( KeyEvent e )
			{
				if ( e.keyCode == SWT.DEL
						&& ( DEUtil.getBindingHolder( inputElement )
								.getDataBindingType( ) == ReportItemHandle.DATABINDING_TYPE_DATA || ( DEUtil.getBindingHolder( inputElement )
								.getDataBindingType( ) == ReportItemHandle.DATABINDING_TYPE_NONE && ( DEUtil.getBindingHolder( inputElement,
								true ) == null || DEUtil.getBindingHolder( inputElement,
								true )
								.getDataBindingType( ) != ReportItemHandle.DATABINDING_TYPE_REPORT_ITEM_REF ) ) ) )
				{
					int itemCount = table.getItemCount( );
					if ( selectIndex == itemCount )
					{
						return;
					}
					if ( selectIndex == itemCount - 1 )
					{
						selectIndex--;
					}
					try
					{
						handleDelEvent( );
					}
					catch ( Exception e1 )
					{
						WidgetUtil.processError( getShell( ), e1 );
					}
					refreshBindingTable( );
				}
			}
		} );

		table.addSelectionListener( new SelectionAdapter( ) {

			public void widgetSelected( SelectionEvent e )
			{
				selectIndex = table.getSelectionIndex( );
				updateButtons( );
			}
		} );

		table.addMouseListener( new MouseAdapter( ) {

			/**
			 * @param e
			 */
			public void mouseDoubleClick( MouseEvent e )
			{
				editSelectedBinding( table.getSelectionIndex( ) );
			}
		} );
		String[] columns = null;
		int[] columnWidth = null;

		groups = new String[groupList.size( ) + 1];
		groups[0] = ALL;
		for ( int i = 0; i < groupList.size( ); i++ )
		{
			groups[i + 1] = ( (GroupHandle) groupList.get( i ) ).getName( );
		}

		if ( canAggregate )
		{
			columns = new String[]{
					null,
					COLUMN_NAME,
					COLUMN_DISPLAYNAME,
					COLUMN_DATATYPE,
					COLUMN_EXPRESSION,
					COLUMN_FILTER,
					COLUMN_AGGREGATEON
			};
			columnWidth = new int[]{
					canSelect ? 25 : 20, 130, 130, 70, 130, 130, 130,
			};
		}
		else
		{
			columns = new String[]{
					null,
					COLUMN_NAME,
					COLUMN_DISPLAYNAME,
					COLUMN_DATATYPE,
					COLUMN_EXPRESSION
			};
			columnWidth = new int[]{
					canSelect ? 25 : 20, 150, 150, 70, 150
			};
		}

		for ( int i = 0; i < columns.length; i++ )
		{
			TableColumn column = new TableColumn( table, SWT.LEFT );
			column.setResizable( columns[i] != null );
			if ( columns[i] != null )
			{
				column.setText( columns[i] );
			}
			column.setWidth( columnWidth[i] );
		}

		if ( canSelect )
		{
			bindingTable = new CheckboxTableViewer( table );
			( (CheckboxTableViewer) bindingTable ).addCheckStateListener( new ICheckStateListener( ) {

				public void checkStateChanged( CheckStateChangedEvent event )
				{

					if ( event.getElement( ) instanceof ComputedColumnHandle )
					{
						ComputedColumnHandle handle = (ComputedColumnHandle) event.getElement( );
						if ( handle.getName( ).equals( selectedColumnName ) )
						{
							selectedColumnName = null;
						}
						else
						{
							selectedColumnName = handle.getName( );
						}
						updateSelection( );
						updateButtons( );
					}
					else
					{
						( (CheckboxTableViewer) bindingTable ).setChecked( dummyChoice,
								false );
					}
				}
			} );
		}
		else
		{
			bindingTable = new TableViewer( table );
		}
		bindingTable.setColumnProperties( columns );
		bindingTable.setContentProvider( contentProvider );
		bindingTable.setLabelProvider( labelProvider );
		// bindingTable.setCellModifier( cellModifier );
		bindingTable.setInput( inputElement );

		bindingTable.addSelectionChangedListener( new ISelectionChangedListener( ) {

			public void selectionChanged( SelectionChangedEvent event )
			{
				updateButtons( );
			}

		} );

		btnAdd = new Button( contentComposite, SWT.PUSH );
		btnAdd.setText( MSG_ADD );
		GridData data = new GridData( );
		data.widthHint = Math.max( 60, btnAdd.computeSize( SWT.DEFAULT,
				SWT.DEFAULT,
				true ).x );
		btnAdd.setLayoutData( data );
		btnAdd.addSelectionListener( new SelectionAdapter( ) {

			public void widgetSelected( SelectionEvent e )
			{
				handleAddEvent( );
				refreshBindingTable( );
				if ( table.getItemCount( ) > 0 )
					selectIndex = ( table.getItemCount( ) - 1 );
				updateButtons( );
			}

		} );

		if ( canAggregate )
		{
			Button btnAddAggr = new Button( contentComposite, SWT.PUSH );
			btnAddAggr.setText( MSG_ADDAGGREGATEON ); //$NON-NLS-1$
			data = new GridData( );
			data.widthHint = Math.max( 60, btnAddAggr.computeSize( SWT.DEFAULT,
					SWT.DEFAULT,
					true ).x );
			btnAddAggr.setLayoutData( data );
			btnAddAggr.addListener( SWT.Selection, new Listener( ) {

				public void handleEvent( Event event )
				{
					DataColumnBindingDialog dialog = new DataColumnBindingDialog( true );
					dialog.setInput( inputElement );
					dialog.setExpressionProvider( expressionProvider );
					dialog.setAggreate( true );
					if ( dialog.open( ) == Dialog.OK )
					{
						if ( bindingTable != null )
						{
							refreshBindingTable( );
							bindingTable.getTable( )
									.setSelection( bindingTable.getTable( )
											.getItemCount( ) - 1 );
						}
					}

					refreshBindingTable( );
					if ( table.getItemCount( ) > 0 )
						setSelectionInTable( table.getItemCount( ) - 1 );
					updateButtons( );
				}

			} );
		}

		btnEdit = new Button( contentComposite, SWT.PUSH );
		btnEdit.setText( MSG_EDIT );
		data = new GridData( );
		data.widthHint = Math.max( 60, btnEdit.computeSize( SWT.DEFAULT,
				SWT.DEFAULT,
				true ).x );
		btnEdit.setLayoutData( data );
		btnEdit.addSelectionListener( new SelectionAdapter( ) {

			public void widgetSelected( SelectionEvent e )
			{
				handleEditEvent( );
				refreshBindingTable( );
			}

		} );
		btnDel = new Button( contentComposite, SWT.PUSH );
		btnDel.setText( MSG_DELETE );
		data = new GridData( );
		data.widthHint = Math.max( 60, btnDel.computeSize( SWT.DEFAULT,
				SWT.DEFAULT,
				true ).x );
		btnDel.setLayoutData( data );
		btnDel.addSelectionListener( new SelectionAdapter( ) {

			public void widgetSelected( SelectionEvent e )
			{
				if ( bindingTable.isCellEditorActive( ) )
				{
					bindingTable.cancelEditing( );
				}
				int pos = bindingTable.getTable( ).getSelectionIndex( );
				if ( pos == -1 )
				{
					bindingTable.getTable( ).setFocus( );
					return;
				}
				selectIndex = pos;
				int itemCount = bindingTable.getTable( ).getItemCount( );
				if ( selectIndex == itemCount - 1 )
				{
					selectIndex--;
				}
				try
				{
					handleDelEvent( );
				}
				catch ( Exception e1 )
				{
					WidgetUtil.processError( getShell( ), e1 );
				}
				refreshBindingTable( );
			}
		} );

		// initTableCellColor( );

		// Add custom buttons
		int buttonsNumber = addButtons( contentComposite, table );
		if ( buttonsNumber > 0 )
		{
			// Adjust UI layout
			if ( table.getLayoutData( ) instanceof GridData )
			{
				( (GridData) table.getLayoutData( ) ).verticalSpan += buttonsNumber;
			}
		}

		if ( !isDataSetVisible )
		{
			if ( composite != null )
				( (GridData) composite.getLayoutData( ) ).exclude = true;
		}

		return parentComposite;
	}

	private void deleteRow( ComputedColumnHandle handle )
	{
		try
		{
			if ( handle.getName( ).equals( selectedColumnName ) )
			{
				selectedColumnName = null;
			}
			handle.drop( );
		}
		catch ( PropertyValueException e )
		{
			ExceptionHandler.handle( e );
		}
	}

	private void generateBindingColumns( ) throws SemanticException
	{
		List columnList = DataUtil.generateComputedColumns( inputElement );
		if ( columnList.size( ) > 0 )
		{
			for ( Iterator iter = columnList.iterator( ); iter.hasNext( ); )
			{
				addBinding( (ComputedColumn) iter.next( ) );
			}
		}
		refreshBindingTable( );
	}

	/**
	 * Gets the DE CommandStack instance
	 * 
	 * @return CommandStack instance
	 */
	private CommandStack getActionStack( )
	{
		return SessionHandleAdapter.getInstance( ).getCommandStack( );
	}

	public String[] getAvailableDatasetItems( )
	{
		String[] dataSets = ChoiceSetFactory.getDataSets( );
		String[] newList = new String[dataSets.length + 1];
		newList[0] = NullDatasetChoice;
		System.arraycopy( dataSets, 0, newList, 1, dataSets.length );
		return newList;
	}

	protected List getBindingList( DesignElementHandle inputElement )
	{
		return DEUtil.getVisiableColumnBindingsList( inputElement );
	}

	private String getColumnName( String expression )
	{
		List columnList = DEUtil.getVisiableColumnBindingsList( inputElement );
		for ( Iterator iter = columnList.iterator( ); iter.hasNext( ); )
		{
			ComputedColumnHandle cachedColumn = (ComputedColumnHandle) iter.next( );
			String columnName = cachedColumn.getName( );
			if ( DEUtil.getColumnExpression( columnName ).equals( expression ) )
			{
				return columnName;
			}
		}

		return null;
	}

	private String getDataSetName( )
	{
		if ( inputElement.getDataSet( ) == null )
		{
			return null;
		}
		String dataSetName = inputElement.getDataSet( ).getQualifiedName( );
		if ( StringUtil.isBlank( dataSetName ) )
		{
			dataSetName = null;
		}
		return dataSetName;
	}

	protected Map<String, ReportItemHandle> referMap = new HashMap<String, ReportItemHandle>( );

	protected String[] getReferences( )
	{
		List referenceList = inputElement.getAvailableDataSetBindingReferenceList( );
		String[] references = new String[referenceList.size( ) + 1];
		references[0] = NullReportItemChoice;
		referMap.put( references[0], null );
		int j = 0;
		for ( int i = 0; i < referenceList.size( ); i++ )
		{
			ReportItemHandle item = ( (ReportItemHandle) referenceList.get( i ) );
			if ( item.getName( ) != null )
			{
				references[++j] = item.getQualifiedName( );
				referMap.put( references[j], item );
			}
		}
		int tmp = j + 1;
		Arrays.sort( references, 1, tmp );
		for ( int i = 0; i < referenceList.size( ); i++ )
		{
			ReportItemHandle item = ( (ReportItemHandle) referenceList.get( i ) );
			if ( item.getName( ) == null )
			{
				references[++j] = item.getElement( )
						.getDefn( )
						.getDisplayName( )
						+ " (ID " //$NON-NLS-1$
						+ item.getID( )
						+ ") - " //$NON-NLS-1$
						+ Messages.getString( "BindingPage.ReportItem.NoName" ); //$NON-NLS-1$
				referMap.put( references[j], item );
			}
		}
		Arrays.sort( references, tmp, referenceList.size( ) + 1 );
		return references;
	}

	private ComputedColumnHandle getSelectColumnHandle( )
	{
		if ( selectedColumnName != null )
		{
			for ( int i = 0; i < bindingTable.getTable( ).getItemCount( ); i++ )
			{
				ComputedColumnHandle handle = (ComputedColumnHandle) bindingTable.getElementAt( i );
				if ( selectedColumnName.equals( handle.getName( ) ) )
				{
					return handle;
				}
			}
		}
		return null;
	}

	protected void handleAddEvent( )
	{
		DataColumnBindingDialog dialog = new DataColumnBindingDialog( true );
		dialog.setInput( inputElement );
		dialog.setExpressionProvider( expressionProvider );
		if ( dialog.open( ) == Dialog.OK )
		{
			if ( bindingTable != null )
			{
				refreshBindingTable( );
				bindingTable.getTable( ).setSelection( bindingTable.getTable( )
						.getItemCount( ) - 1 );
			}
		}

	}

	protected void handleDelEvent( )
	{
		int pos = bindingTable.getTable( ).getSelectionIndex( );
		if ( pos > -1 )
		{
			try
			{
				ComputedColumnHandle handle = (ComputedColumnHandle) ( DEUtil.getBindingHolder( inputElement ) ).getColumnBindings( )
						.getAt( pos );
				deleteRow( handle );
			}
			catch ( Exception e1 )
			{
				ExceptionHandler.handle( e1 );
			}
		}
	}

	protected void handleEditEvent( )
	{
		int pos = bindingTable.getTable( ).getSelectionIndex( );
		editSelectedBinding( pos );
	}

	/**
	 * Edits the selected binding of table.
	 * 
	 * @param bindingIndex
	 */
	private void editSelectedBinding( int bindingIndex )
	{
		ComputedColumnHandle bindingHandle = null;
		if ( bindingIndex > -1 )
		{
			bindingHandle = (ComputedColumnHandle) ( DEUtil.getBindingHolder( inputElement ) ).getColumnBindings( )
					.getAt( bindingIndex );
		}
		if ( bindingHandle == null )
			return;

		String bindingName = bindingHandle.getName( );
		DataColumnBindingDialog dialog = new DataColumnBindingDialog( false );
		dialog.setInput( inputElement, bindingHandle );
		dialog.setExpressionProvider( expressionProvider );
		if ( dialog.open( ) == Dialog.OK )
		{
			if ( bindingTable != null )
				bindingTable.getTable( ).setSelection( bindingIndex );
			if ( selectedColumnName != null
					&& selectedColumnName.equals( bindingName ) )
				selectedColumnName = bindingHandle.getName( );
		}
	}

	protected boolean initDialog( )
	{
		if ( canSelect )
		{
			if ( inputElement instanceof DataItemHandle )
			{
				selectedColumnName = ( (DataItemHandle) inputElement ).getResultSetColumn( );
				updateSelection( );
			}
			else if ( inputElement instanceof ImageHandle )
			{
				selectedColumnName = getColumnName( ( (ImageHandle) inputElement ).getValueExpression( ) );
				updateSelection( );
			}
		}
		load( );
		return super.initDialog( );
	}

	/**
	 * @return Returns the enableAutoCommit.
	 */
	public boolean isEnableAutoCommit( )
	{
		return enableAutoCommit;
	}

	public void load( )
	{
		if ( canSelect )
		{
			datasetRadio.setEnabled( true );
			reportItemRadio.setEnabled( true );
			BindingInfo info = (BindingInfo) loadValue( );
			if ( info != null )
			{
				refreshBindingInfo( info );
			}
		}
		refreshBindingTable( );
	}

	public Object loadValue( )
	{
		if ( canSelect )
		{
			int type = inputElement.getDataBindingType( );
			Object value;
			if ( type == ReportItemHandle.DATABINDING_TYPE_NONE )
				type = DEUtil.getBindingHolder( inputElement )
						.getDataBindingType( );
			switch ( type )
			{
				case ReportItemHandle.DATABINDING_TYPE_DATA :
					DataSetHandle dataset = inputElement.getDataSet( );
					if ( dataset == null )
						value = NullDatasetChoice;
					else
						value = dataset.getQualifiedName( );
					break;
				case ReportItemHandle.DATABINDING_TYPE_REPORT_ITEM_REF :
					ReportItemHandle reference = inputElement.getDataBindingReference( );
					if ( reference == null )
						value = NullReportItemChoice;
					else
						value = reference.getQualifiedName( );
					break;
				default :
					value = NullDatasetChoice;
			}
			BindingInfo info = new BindingInfo( type, value );
			return info;
		}
		return null;
	}

	protected void okPressed( )
	{
		if ( canSelect )
		{
			setResult( selectedColumnName );
			if ( inputElement instanceof DataItemHandle )
			{
				try
				{
					( (DataItemHandle) inputElement ).setResultSetColumn( selectedColumnName );
				}
				catch ( SemanticException e )
				{
					ExceptionHandler.handle( e );
				}
			}
		}
		super.okPressed( );
	}

	private void refreshBinding( )
	{
		if ( datasetRadio.getSelection( ) )
		{
			datasetRadio.setSelection( true );
			datasetCombo.setEnabled( true );
			reportItemRadio.setSelection( false );
			reportItemCombo.setEnabled( false );
			if ( datasetCombo.getSelectionIndex( ) == -1 )
			{
				datasetCombo.setItems( getAvailableDatasetItems( ) );
				datasetCombo.select( 0 );
			}
		}
		else
		{
			datasetRadio.setSelection( false );
			datasetCombo.setEnabled( false );
			reportItemRadio.setSelection( true );
			reportItemCombo.setEnabled( true );
			if ( reportItemCombo.getSelectionIndex( ) == -1 )
			{
				reportItemCombo.setItems( getReferences( ) );
				reportItemCombo.select( 0 );
			}
		}
	}

	private void refreshBindingInfo( BindingInfo info )
	{
		if ( canSelect )
		{
			int type = info.getBindingType( );
			Object value = info.getBindingValue( );
			datasetCombo.setItems( getAvailableDatasetItems( ) );
			reportItemCombo.setItems( getReferences( ) );
			if ( type == ReportItemHandle.DATABINDING_TYPE_NONE )
				type = DEUtil.getBindingHolder( inputElement )
						.getDataBindingType( );
			switch ( type )
			{
				case ReportItemHandle.DATABINDING_TYPE_NONE :
				case ReportItemHandle.DATABINDING_TYPE_DATA :
					datasetRadio.setSelection( true );
					datasetCombo.setEnabled( true );
					datasetCombo.setText( value.toString( ) );
					reportItemRadio.setSelection( false );
					reportItemCombo.setEnabled( false );
					break;
				case ReportItemHandle.DATABINDING_TYPE_REPORT_ITEM_REF :
					datasetRadio.setSelection( false );
					datasetCombo.setEnabled( false );
					reportItemRadio.setSelection( true );
					reportItemCombo.setEnabled( true );
					reportItemCombo.setText( value.toString( ) );
			}
		}
	}

	protected void refreshBindingTable( )
	{
		bindingTable.refresh( );
		if ( canSelect )
		{
			updateSelection( );
		}
		updateButtons( );
	}

	private void resetDataSetReference( Object value, boolean clearHistory )
	{
		try
		{
			startTrans( "" ); //$NON-NLS-1$
			inputElement.setDataBindingReference( null );
			DataSetHandle dataSet = null;
			if ( value != null )
			{
				dataSet = SessionHandleAdapter.getInstance( )
						.getReportDesignHandle( )
						.findDataSet( value.toString( ) );
			}
			if ( inputElement.getDataBindingType( ) == ReportItemHandle.DATABINDING_TYPE_REPORT_ITEM_REF )
			{
				inputElement.setDataBindingReference( null );
			}
			inputElement.setDataSet( dataSet );
			if ( clearHistory )
			{
				inputElement.getColumnBindings( ).clearValue( );
				inputElement.getPropertyHandle( ReportItemHandle.PARAM_BINDINGS_PROP )
						.clearValue( );
			}
			generateBindingColumns( );

			selectedColumnName = null;
			commit( );
		}
		catch ( SemanticException e )
		{
			rollback( );
			ExceptionHandler.handle( e );
		}
		load( );
	}

	private void resetReference( Object value )
	{
		if ( value == null
				&& inputElement.getDataBindingType( ) == ReportItemHandle.DATABINDING_TYPE_DATA )
		{
			resetDataSetReference( null, true );
		}
		else
		{
			try
			{
				startTrans( Messages.getString( "DataColumBindingDialog.stackMsg.resetReference" ) ); //$NON-NLS-1$
				ReportItemHandle element = null;
				if ( value != null )
				{
					element = (ReportItemHandle) SessionHandleAdapter.getInstance( )
							.getReportDesignHandle( )
							.findElement( value.toString( ) );
				}
				inputElement.setDataBindingReference( element );
				selectedColumnName = null;
				commit( );
			}
			catch ( SemanticException e )
			{
				rollback( );
				ExceptionHandler.handle( e );
			}
			load( );
		}
	}

	private void rollback( )
	{
		if ( isEnableAutoCommit( ) )
		{
			getActionStack( ).rollback( );
		}
	}

	public void save( Object saveValue ) throws SemanticException
	{
		if ( saveValue instanceof BindingInfo )
		{
			BindingInfo info = (BindingInfo) saveValue;
			int type = info.getBindingType( );
			String value = info.getBindingValue( ).toString( );
			switch ( type )
			{
				case ReportItemHandle.DATABINDING_TYPE_DATA :
					if ( value.equals( NullDatasetChoice ) )
					{
						value = null;
					}
					int ret = 0;
					if ( !NullDatasetChoice.equals( ( (BindingInfo) loadValue( ) ).getBindingValue( )
							.toString( ) )
							|| inputElement.getColumnBindings( )
									.iterator( )
									.hasNext( ) )
					{
						MessageDialog prefDialog = new MessageDialog( UIUtil.getDefaultShell( ),
								Messages.getString( "dataBinding.title.changeDataSet" ),//$NON-NLS-1$
								null,
								Messages.getString( "dataBinding.message.changeDataSet" ),//$NON-NLS-1$
								MessageDialog.QUESTION,
								new String[]{
										Messages.getString( "AttributeView.dialg.Message.Yes" ),//$NON-NLS-1$
										Messages.getString( "AttributeView.dialg.Message.No" ),//$NON-NLS-1$
										Messages.getString( "AttributeView.dialg.Message.Cancel" )}, 0 );//$NON-NLS-1$

						ret = prefDialog.open( );
					}

					switch ( ret )
					{
						// Clear binding info
						case 0 :
							resetDataSetReference( value, true );
							break;
						// Doesn't clear binding info
						case 1 :
							resetDataSetReference( value, false );
							break;
						// Cancel.
						case 2 :
							load( );
					}
					break;
				case ReportItemHandle.DATABINDING_TYPE_REPORT_ITEM_REF :
					if ( value.equals( NullReportItemChoice ) )
					{
						value = null;
					}
					else if ( referMap.get( value ).getName( ) == null )
					{
						MessageDialog dialog = new MessageDialog( UIUtil.getDefaultShell( ),
								Messages.getString( "dataBinding.title.haveNoName" ),//$NON-NLS-1$
								null,
								Messages.getString( "dataBinding.message.haveNoName" ),//$NON-NLS-1$
								MessageDialog.QUESTION,
								new String[]{
									Messages.getString( "dataBinding.button.OK" )//$NON-NLS-1$
								},
								0 );

						dialog.open( );
						load( );
						return;
					}
					int ret1 = 0;
					if ( !NullReportItemChoice.equals( ( (BindingInfo) loadValue( ) ).getBindingValue( )
							.toString( ) )
							|| inputElement.getColumnBindings( )
									.iterator( )
									.hasNext( ) )
					{
						MessageDialog prefDialog = new MessageDialog( UIUtil.getDefaultShell( ),
								Messages.getString( "dataBinding.title.changeDataSet" ),//$NON-NLS-1$
								null,
								Messages.getString( "dataBinding.message.changeDataSet" ),//$NON-NLS-1$
								MessageDialog.QUESTION,
								new String[]{
										Messages.getString( "AttributeView.dialg.Message.Yes" ),//$NON-NLS-1$
										Messages.getString( "AttributeView.dialg.Message.Cancel" )}, 0 );//$NON-NLS-1$

						ret1 = prefDialog.open( );
					}

					switch ( ret1 )
					{
						// Clear binding info
						case 0 :
							resetReference( value );
							break;
						// Cancel.
						case 1 :
							load( );
					}
			}
		}
	}

	private void saveBinding( )
	{
		BindingInfo info = new BindingInfo( );
		if ( datasetRadio.getSelection( ) )
		{
			info.setBindingType( ReportItemHandle.DATABINDING_TYPE_DATA );
			info.setBindingValue( datasetCombo.getText( ) );
		}
		else
		{
			info.setBindingType( ReportItemHandle.DATABINDING_TYPE_REPORT_ITEM_REF );
			info.setBindingValue( reportItemCombo.getText( ) );
		}
		try
		{
			save( info );
		}
		catch ( SemanticException e )
		{
			ExceptionHandler.handle( e );
		}
	}

	/**
	 * @param enableAutoCommit
	 *            The enableAutoCommit to set.
	 */
	public void setEnableAutoCommit( boolean enableAutoCommit )
	{
		this.enableAutoCommit = enableAutoCommit;
	}

	public void setExpressionProvider( ExpressionProvider provider )
	{
		expressionProvider = provider;
	}

	/*
	 * Set data for Group List
	 */
	public void setGroupList( List groupList )
	{
		this.groupList = groupList;
	}

	/*
	 * Set input for dialog
	 */
	private void setInput( ReportItemHandle input )
	{
		this.inputElement = input;
		ReportItemHandle container = DEUtil.getBindingHolder( input.getContainer( ) );
		if ( container != null
				&& ( container.getDataSet( ) != null || container.columnBindingsIterator( )
						.hasNext( ) ) )
		{
			NullDatasetChoice = CHOICE_DATASET_FROM_CONTAINER;
		}
		else
		{
			NullDatasetChoice = CHOICE_NONE;
		}

		if ( container != null && container.getDataBindingReference( ) != null )
		{
			NullReportItemChoice = CHOICE_REPORTITEM_FROM_CONTAINER;
		}
		else
		{
			NullReportItemChoice = CHOICE_NONE;
		}

		isDataSetVisible = DEUtil.getBindingHolder( inputElement )
				.getElement( )
				.getDefn( )
				.isPropertyVisible( IReportItemModel.DATA_SET_PROP );

		IBindingDialogHelper dialogHelper = (IBindingDialogHelper) ElementAdapterManager.getAdapter( inputElement,
				IBindingDialogHelper.class );
		if ( dialogHelper != null )
			dialogHelper.setBindingHolder( DEUtil.getBindingHolder( inputElement ) );
		canAggregate = dialogHelper == null ? false
				: dialogHelper.canProcessAggregation( );
	}

	protected void setSelectionInTable( int selectedIndex )
	{
		this.selectIndex = selectedIndex;
	}

	private void startTrans( String name )
	{
		if ( isEnableAutoCommit( ) )
		{
			getActionStack( ).startTrans( name );
		}
	}

	protected void updateButtons( )
	{
		boolean okEnable = false;

		if ( !canSelect || ( !isDataSetVisible && selectedColumnName != null )
		// || ( selectedColumnName != null && getDataSetName( ) != null )
				// || ( selectedColumnName != null && DEUtil.getBindingHolder(
				// inputElement )
				// .getDataSet( ) != null )
				|| getSelectColumnHandle( ) != null )
		{
			okEnable = true;
		}
		getOkButton( ).setEnabled( okEnable );
		int min = 0;
		int max = bindingTable.getTable( ).getItemCount( ) - 1;

		if ( ( min <= selectIndex ) && ( selectIndex <= max ) )
		{
			btnDel.setEnabled( true );
			if ( btnEdit != null )
				btnEdit.setEnabled( true );
		}
		else
		{
			btnDel.setEnabled( false );
			if ( btnEdit != null )
				btnEdit.setEnabled( false );
		}
		bindingTable.getTable( ).select( selectIndex );
		if ( DEUtil.getBindingHolder( inputElement ).getDataBindingType( ) == ReportItemHandle.DATABINDING_TYPE_DATA )
		{
			btnAdd.setEnabled( true );
		}
		else if ( DEUtil.getBindingHolder( inputElement ).getDataBindingType( ) == ReportItemHandle.DATABINDING_TYPE_NONE
				&& ( DEUtil.getBindingHolder( inputElement, true ) == null || DEUtil.getBindingHolder( inputElement,
						true )
						.getDataBindingType( ) != ReportItemHandle.DATABINDING_TYPE_REPORT_ITEM_REF ) )
		{
			btnAdd.setEnabled( true );
		}
		else
		{
			btnAdd.setEnabled( false );
			btnEdit.setEnabled( false );
			btnDel.setEnabled( false );
		}
	}

	private void updateSelection( )
	{
		if ( canSelect )
		{
			( (CheckboxTableViewer) bindingTable ).setAllChecked( false );
			( (CheckboxTableViewer) bindingTable ).setGrayed( dummyChoice, true );
			if ( getSelectColumnHandle( ) != null )
			{
				( (CheckboxTableViewer) bindingTable ).setChecked( getSelectColumnHandle( ),
						true );
			}
		}
	}

}
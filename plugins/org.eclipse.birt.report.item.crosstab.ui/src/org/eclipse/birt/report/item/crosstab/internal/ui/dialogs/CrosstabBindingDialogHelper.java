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

package org.eclipse.birt.report.item.crosstab.internal.ui.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.birt.core.data.ExpressionUtil;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.data.engine.api.IBinding;
import org.eclipse.birt.data.engine.api.aggregation.AggregationManager;
import org.eclipse.birt.data.engine.api.aggregation.IAggrFunction;
import org.eclipse.birt.data.engine.api.aggregation.IParameterDefn;
import org.eclipse.birt.data.engine.api.querydefn.Binding;
import org.eclipse.birt.data.engine.api.querydefn.ScriptExpression;
import org.eclipse.birt.report.data.adapter.api.AdapterException;
import org.eclipse.birt.report.data.adapter.api.CubeQueryUtil;
import org.eclipse.birt.report.data.adapter.api.DataAdapterUtil;
import org.eclipse.birt.report.data.adapter.api.DataRequestSession;
import org.eclipse.birt.report.data.adapter.api.DataSessionContext;
import org.eclipse.birt.report.designer.data.ui.util.DataUtil;
import org.eclipse.birt.report.designer.internal.ui.dialogs.AbstractBindingDialogHelper;
import org.eclipse.birt.report.designer.internal.ui.util.ExceptionHandler;
import org.eclipse.birt.report.designer.internal.ui.util.UIUtil;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.designer.ui.dialogs.ExpressionBuilder;
import org.eclipse.birt.report.designer.ui.views.attributes.providers.ChoiceSetFactory;
import org.eclipse.birt.report.designer.util.DEUtil;
import org.eclipse.birt.report.item.crosstab.core.ICrosstabConstants;
import org.eclipse.birt.report.item.crosstab.core.de.AggregationCellHandle;
import org.eclipse.birt.report.item.crosstab.core.de.ComputedMeasureViewHandle;
import org.eclipse.birt.report.item.crosstab.core.de.CrosstabReportItemHandle;
import org.eclipse.birt.report.item.crosstab.core.de.CrosstabViewHandle;
import org.eclipse.birt.report.item.crosstab.core.de.DimensionViewHandle;
import org.eclipse.birt.report.item.crosstab.core.de.LevelViewHandle;
import org.eclipse.birt.report.item.crosstab.core.de.MeasureViewHandle;
import org.eclipse.birt.report.item.crosstab.core.util.CrosstabUtil;
import org.eclipse.birt.report.model.api.AggregationArgumentHandle;
import org.eclipse.birt.report.model.api.ComputedColumnHandle;
import org.eclipse.birt.report.model.api.ExtendedItemHandle;
import org.eclipse.birt.report.model.api.ModuleHandle;
import org.eclipse.birt.report.model.api.ReportItemHandle;
import org.eclipse.birt.report.model.api.StructureFactory;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.eclipse.birt.report.model.api.elements.structures.AggregationArgument;
import org.eclipse.birt.report.model.api.elements.structures.ComputedColumn;
import org.eclipse.birt.report.model.api.extension.ExtendedElementException;
import org.eclipse.birt.report.model.api.metadata.IChoice;
import org.eclipse.birt.report.model.api.metadata.IChoiceSet;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * 
 */

public class CrosstabBindingDialogHelper extends AbstractBindingDialogHelper
{

	protected static final String NAME = Messages.getString( "BindingDialogHelper.text.Name" ); //$NON-NLS-1$
	protected static final String DATA_TYPE = Messages.getString( "BindingDialogHelper.text.DataType" ); //$NON-NLS-1$
	protected static final String FUNCTION = Messages.getString( "BindingDialogHelper.text.Function" ); //$NON-NLS-1$
	protected static final String DATA_FIELD = Messages.getString( "BindingDialogHelper.text.DataField" ); //$NON-NLS-1$
	protected static final String FILTER_CONDITION = Messages.getString( "BindingDialogHelper.text.Filter" ); //$NON-NLS-1$
	protected static final String AGGREGATE_ON = Messages.getString( "BindingDialogHelper.text.AggOn" ); //$NON-NLS-1$
	protected static final String EXPRESSION = Messages.getString( "BindingDialogHelper.text.Expression" ); //$NON-NLS-1$
	protected static final String ALL = Messages.getString( "CrosstabBindingDialogHelper.AggOn.All" ); //$NON-NLS-1$
	protected static final String DISPLAY_NAME = Messages.getString( "BindingDialogHelper.text.displayName" ); //$NON-NLS-1$

	protected static final String DEFAULT_ITEM_NAME = Messages.getString( "BindingDialogHelper.bindingName.dataitem" ); //$NON-NLS-1$
	protected static final String DEFAULT_AGGREGATION_NAME = Messages.getString( "BindingDialogHelper.bindingName.aggregation" ); //$NON-NLS-1$

	protected static final IChoiceSet DATA_TYPE_CHOICE_SET = DEUtil.getMetaDataDictionary( )
			.getStructure( ComputedColumn.COMPUTED_COLUMN_STRUCT )
			.getMember( ComputedColumn.DATA_TYPE_MEMBER )
			.getAllowedChoices( );
	protected static final IChoice[] DATA_TYPE_CHOICES = DATA_TYPE_CHOICE_SET.getChoices( null );
	protected String[] dataTypes = ChoiceSetFactory.getDisplayNamefromChoiceSet( DATA_TYPE_CHOICE_SET );

	private Text txtName, txtFilter, txtExpression;
	private Combo cmbType, cmbFunction, cmbAggOn;
	private Composite paramsComposite;

	private Map<String, Control> paramsMap = new HashMap<String, Control>( );
	private Map<String, String> paramsValueMap = new HashMap<String, String>( );

	private Composite composite;
	private Text txtDisplayName;
	private ComputedColumn newBinding;
	private CLabel messageLine;
	private Label lbName;
	private Object container;

	public void createContent( Composite parent )
	{
		composite = parent;

		( (GridLayout) composite.getLayout( ) ).numColumns = 3;

		lbName = new Label( composite, SWT.NONE );
		lbName.setText( NAME );

		txtName = new Text( composite, SWT.BORDER );

		GridData gd = new GridData( GridData.FILL_HORIZONTAL );
		gd.horizontalSpan = 2;
		gd.widthHint = 250;
		txtName.setLayoutData( gd );
		// WidgetUtil.createGridPlaceholder( composite, 1, false );

		txtName.addModifyListener( new ModifyListener( ) {

			public void modifyText( ModifyEvent e )
			{
				validate( );
			}

		} );

		new Label( composite, SWT.NONE ).setText( DISPLAY_NAME );
		txtDisplayName = new Text( composite, SWT.BORDER );
		gd = new GridData( GridData.FILL_HORIZONTAL );
		gd.horizontalSpan = 2;
		txtDisplayName.setLayoutData( gd );
		// WidgetUtil.createGridPlaceholder( composite, 1, false );

		new Label( composite, SWT.NONE ).setText( DATA_TYPE );
		cmbType = new Combo( composite, SWT.BORDER | SWT.READ_ONLY );
		cmbType.setLayoutData( gd );
		// WidgetUtil.createGridPlaceholder( composite, 1, false );

		if ( isAggregate( ) )
		{
			createAggregateSection( composite );
		}
		else
		{
			createCommonSection( composite );
		}
		createMessageSection( composite );

		gd = new GridData( GridData.FILL_BOTH );
		composite.setLayoutData( gd );
		setContentSize( composite );
	}

	public void initDialog( )
	{
		cmbType.setItems( dataTypes );

		if ( isAggregate( ) )
		{
			initFunction( );
			initFilter( );
			initAggOn( );
		}

		if ( getBinding( ) == null )// create
		{
			setTypeSelect( dataTypes[0] );
			this.newBinding = StructureFactory.newComputedColumn( getBindingHolder( ),
					isAggregate( ) ? DEFAULT_AGGREGATION_NAME
							: DEFAULT_ITEM_NAME );
			setName( this.newBinding.getName( ) );
		}
		else
		{
			setName( getBinding( ).getName( ) );
			setDisplayName( getBinding( ).getDisplayName( ) );
			if ( getBinding( ).getDataType( ) != null )
				if ( DATA_TYPE_CHOICE_SET.findChoice( getBinding( ).getDataType( ) ) != null )
					setTypeSelect( DATA_TYPE_CHOICE_SET.findChoice( getBinding( ).getDataType( ) )
							.getDisplayName( ) );
				else
					cmbType.setText( "" );
			if ( getBinding( ).getExpression( ) != null )
				setDataFieldExpression( getBinding( ).getExpression( ) );
		}

		if ( this.getBinding( ) != null )
		{
			this.txtName.setEnabled( false );
		}

		validate( );
	}

	private void initAggOn( )
	{
		try
		{
			CrosstabReportItemHandle xtabHandle = (CrosstabReportItemHandle) ( (ExtendedItemHandle) getBindingHolder( ) ).getReportItem( );
			String[] aggOns = getAggOns( xtabHandle );
			cmbAggOn.setItems( aggOns );

			String aggstr = ""; //$NON-NLS-1$
			if ( getBinding( ) != null )
			{
				List aggOnList = getBinding( ).getAggregateOnList( );
				int i = 0;
				for ( Iterator iterator = aggOnList.iterator( ); iterator.hasNext( ); )
				{
					if ( i > 0 )
						aggstr += ","; //$NON-NLS-1$
					String name = (String) iterator.next( );
					aggstr += name;
					i++;
				}
			}
			else if ( getDataItemContainer( ) instanceof AggregationCellHandle )
			{
				AggregationCellHandle cellHandle = (AggregationCellHandle) getDataItemContainer( );
				if ( cellHandle.getAggregationOnRow( ) != null )
				{
					aggstr += cellHandle.getAggregationOnRow( ).getFullName( );
					if ( cellHandle.getAggregationOnColumn( ) != null )
					{
						aggstr += ","; //$NON-NLS-1$
					}
				}
				if ( cellHandle.getAggregationOnColumn( ) != null )
				{
					aggstr += cellHandle.getAggregationOnColumn( )
							.getFullName( );
				}
			}
			else if ( container instanceof AggregationCellHandle )
			{
				AggregationCellHandle cellHandle = (AggregationCellHandle) container;
				if ( cellHandle.getAggregationOnRow( ) != null )
				{
					aggstr += cellHandle.getAggregationOnRow( ).getFullName( );
					if ( cellHandle.getAggregationOnColumn( ) != null )
					{
						aggstr += ","; //$NON-NLS-1$
					}
				}
				if ( cellHandle.getAggregationOnColumn( ) != null )
				{
					aggstr += cellHandle.getAggregationOnColumn( )
							.getFullName( );
				}
			}
			for ( int j = 0; j < aggOns.length; j++ )
			{
				if ( aggOns[j].equals( aggstr ) )
				{
					cmbAggOn.select( j );
					return;
				}
			}
			cmbAggOn.select( 0 );
		}
		catch ( ExtendedElementException e )
		{
			ExceptionHandler.handle( e );
		}
	}

	private String[] getAggOns( CrosstabReportItemHandle xtabHandle )
	{

		List rowLevelList = getCrosstabViewHandleLevels( xtabHandle,
				ICrosstabConstants.ROW_AXIS_TYPE );
		List columnLevelList = getCrosstabViewHandleLevels( xtabHandle,
				ICrosstabConstants.COLUMN_AXIS_TYPE );
		List aggOnList = new ArrayList( );
		aggOnList.add( ALL );
		for ( Iterator iterator = rowLevelList.iterator( ); iterator.hasNext( ); )
		{
			String name = (String) iterator.next( );
			aggOnList.add( name );
		}
		for ( Iterator iterator = columnLevelList.iterator( ); iterator.hasNext( ); )
		{
			String name = (String) iterator.next( );
			aggOnList.add( name );
		}
		for ( Iterator iterator = rowLevelList.iterator( ); iterator.hasNext( ); )
		{
			String name = (String) iterator.next( );
			for ( Iterator iterator2 = columnLevelList.iterator( ); iterator2.hasNext( ); )
			{
				String name2 = (String) iterator2.next( );
				aggOnList.add( name + "," + name2 ); //$NON-NLS-1$
			}
		}
		return (String[]) aggOnList.toArray( new String[aggOnList.size( )] );
	}

	private List getCrosstabViewHandleLevels( CrosstabReportItemHandle xtab,
			int type )
	{
		List levelList = new ArrayList( );
		CrosstabViewHandle viewHandle = xtab.getCrosstabView( type );
		if ( viewHandle != null )
		{
			int dimensions = viewHandle.getDimensionCount( );
			for ( int i = 0; i < dimensions; i++ )
			{
				DimensionViewHandle dimension = viewHandle.getDimension( i );
				int levels = dimension.getLevelCount( );
				for ( int j = 0; j < levels; j++ )
				{
					LevelViewHandle level = dimension.getLevel( j );
					levelList.add( level.getCubeLevel( ).getFullName( ) );
				}
			}
		}
		return levelList;
	}

	private void initFilter( )
	{
		if ( binding != null && binding.getFilterExpression( ) != null )
		{
			txtFilter.setText( binding.getFilterExpression( ) );
		}
	}

	private void initFunction( )
	{
		cmbFunction.setItems( getFunctionDisplayNames( ) );
		// cmbFunction.add( NULL, 0 );
		if ( binding == null )
		{
			cmbFunction.select( 0 );
			handleFunctionSelectEvent( );
			return;
		}
		try
		{
			String functionString = getFunctionDisplayName( DataAdapterUtil.adaptModelAggregationType( binding.getAggregateFunction( ) ) );
			int itemIndex = getItemIndex( getFunctionDisplayNames( ),
					functionString );
			cmbFunction.select( itemIndex );
			handleFunctionSelectEvent( );
		}
		catch ( AdapterException e )
		{
			ExceptionHandler.handle( e );
		}
		// List args = getFunctionArgs( functionString );
		// bindingColumn.argumentsIterator( )
		for ( Iterator iterator = binding.argumentsIterator( ); iterator.hasNext( ); )
		{
			AggregationArgumentHandle arg = (AggregationArgumentHandle) iterator.next( );
			String argName = DataAdapterUtil.adaptArgumentName( arg.getName( ) );
			if ( paramsMap.containsKey( argName ) )
			{
				if ( arg.getValue( ) != null )
				{
					Control control = paramsMap.get( argName );
					if ( control instanceof Text )
					{
						( (Text) control ).setText( arg.getValue( ) );
					}
					else if ( control instanceof Combo )
					{
						( (Combo) control ).setText( arg.getValue( ) );
					}
				}
			}
		}
	}

	private String[] getFunctionDisplayNames( )
	{
		IAggrFunction[] choices = getFunctions( );
		if ( choices == null )
			return new String[0];

		String[] displayNames = new String[choices.length];
		for ( int i = 0; i < choices.length; i++ )
		{
			displayNames[i] = choices[i].getDisplayName( );
		}
		return displayNames;
	}

	private IAggrFunction getFunctionByDisplayName( String displayName )
	{
		IAggrFunction[] choices = getFunctions( );
		if ( choices == null )
			return null;

		for ( int i = 0; i < choices.length; i++ )
		{
			if ( choices[i].getDisplayName( ).equals( displayName ) )
			{
				return choices[i];
			}
		}
		return null;
	}

	private String getFunctionDisplayName( String function )
	{
		try
		{
			return DataUtil.getAggregationManager( )
					.getAggregation( function )
					.getDisplayName( );
		}
		catch ( BirtException e )
		{
			ExceptionHandler.handle( e );
			return null;
		}
	}

	private IAggrFunction[] getFunctions( )
	{
		try
		{
			List aggrInfoList = DataUtil.getAggregationManager( )
					.getAggregations( AggregationManager.AGGR_XTAB );
			return (IAggrFunction[]) aggrInfoList.toArray( new IAggrFunction[0] );
		}
		catch ( BirtException e )
		{
			ExceptionHandler.handle( e );
			return new IAggrFunction[0];
		}
	}

	private String getDataTypeDisplayName( String dataType )
	{
		for ( int i = 0; i < DATA_TYPE_CHOICES.length; i++ )
		{
			if ( dataType.equals( DATA_TYPE_CHOICES[i].getName( ) ) )
			{
				return DATA_TYPE_CHOICES[i].getDisplayName( );
			}
		}

		return ""; //$NON-NLS-1$
	}

	private void initTextField( Text txtParam, IParameterDefn param )
	{
		if ( paramsValueMap.containsKey( param.getName( ) ) )
		{
			txtParam.setText( paramsValueMap.get( param.getName( ) ) );
			return;
		}
		if ( binding != null )
		{
			for ( Iterator iterator = binding.argumentsIterator( ); iterator.hasNext( ); )
			{
				AggregationArgumentHandle arg = (AggregationArgumentHandle) iterator.next( );
				if ( arg.getName( ).equals( param.getName( ) ) )
				{
					if ( arg.getValue( ) != null )
						txtParam.setText( arg.getValue( ) );
					return;
				}
			}
		}
	}

	/**
	 * fill the cmbDataField with binding holder's bindings
	 * 
	 * @param param
	 */
	private void initDataFields( Combo cmbDataField, IParameterDefn param )
	{
		List<String> datas = getMesures( );
		datas.addAll( getDatas( ) );
		String[] items = datas.toArray( new String[datas.size( )] );
		cmbDataField.setItems( items );

		if ( paramsValueMap.containsKey( param.getName( ) ) )
		{
			cmbDataField.setText( paramsValueMap.get( param.getName( ) ) );
			return;
		}
		if ( binding != null )
		{
			for ( Iterator iterator = binding.argumentsIterator( ); iterator.hasNext( ); )
			{
				AggregationArgumentHandle arg = (AggregationArgumentHandle) iterator.next( );
				if ( arg.getName( ).equals( param.getName( ) ) )
				{
					if ( arg.getValue( ) != null )
					{
						for ( int i = 0; i < items.length; i++ )
						{
							if ( items[i].equals( arg.getValue( ) ) )
							{
								cmbDataField.select( i );
								break;
							}
						}
						return;
					}
				}
			}
			// backforward compatble
			if ( binding.getExpression( ) != null )
			{
				for ( int i = 0; i < items.length; i++ )
				{
					if ( items[i].equals( binding.getExpression( ) ) )
					{
						cmbDataField.select( i );
					}
				}
			}
		}
	}

	private List<String> getMesures( )
	{
		List<String> measures = new ArrayList<String>( );
		try
		{
			CrosstabReportItemHandle xtabHandle = (CrosstabReportItemHandle) ( (ExtendedItemHandle) getBindingHolder( ) ).getReportItem( );

			measures.add( "" ); //$NON-NLS-1$

			for ( int i = 0; i < xtabHandle.getMeasureCount( ); i++ )
			{
				MeasureViewHandle mv = xtabHandle.getMeasure( i );

				if ( mv instanceof ComputedMeasureViewHandle )
				{
					continue;
				}
				measures.add( DEUtil.getExpression( mv.getCubeMeasure( ) ) );
			}
		}
		catch ( ExtendedElementException e )
		{
		}
		return measures;
	}

	private List<String> getDatas( )
	{
		List<String> datas = new ArrayList<String>( );
		try
		{
			CrosstabReportItemHandle xtabHandle = (CrosstabReportItemHandle) ( (ExtendedItemHandle) getBindingHolder( ) ).getReportItem( );

			try
			{
				IBinding[] aggregateBindings = CubeQueryUtil.getAggregationBindings( getCrosstabBindings( xtabHandle ) );
				for ( IBinding binding : aggregateBindings )
				{
					if ( getBinding( ) == null
							|| !getBinding( ).getName( )
									.equals( binding.getBindingName( ) ) )
						datas.add( ExpressionUtil.createJSDataExpression( binding.getBindingName( ) ) );
				}
			}
			catch ( AdapterException e )
			{
			}
			catch ( BirtException e )
			{
			}

		}
		catch ( ExtendedElementException e )
		{
		}
		return datas;
	}

	private IBinding[] getCrosstabBindings( CrosstabReportItemHandle xtabHandle )
			throws BirtException
	{
		Iterator bindingItr = ( (ExtendedItemHandle) xtabHandle.getModelHandle( ) ).columnBindingsIterator( );
		ModuleHandle module = ( (ExtendedItemHandle) xtabHandle.getModelHandle( ) ).getModuleHandle( );

		List<IBinding> bindingList = new ArrayList<IBinding>( );

		if ( bindingItr != null )
		{
			Map cache = new HashMap( );

			List rowLevelNameList = new ArrayList( );
			List columnLevelNameList = new ArrayList( );

			while ( bindingItr.hasNext( ) )
			{
				ComputedColumnHandle column = (ComputedColumnHandle) bindingItr.next( );

				Binding binding = new Binding( column.getName( ) );
				binding.setAggrFunction( column.getAggregateFunction( ) == null ? null
						: DataAdapterUtil.adaptModelAggregationType( column.getAggregateFunction( ) ) );
				binding.setExpression( column.getExpression( ) == null ? null
						: new ScriptExpression( column.getExpression( ) ) );
				binding.setDataType( DataAdapterUtil.adaptModelDataType( column.getDataType( ) ) );

				if ( column.getFilterExpression( ) != null )
				{
					binding.setFilter( new ScriptExpression( column.getFilterExpression( ) ) );
				}

				for ( Iterator argItr = column.argumentsIterator( ); argItr.hasNext( ); )
				{
					AggregationArgumentHandle aah = (AggregationArgumentHandle) argItr.next( );
					if ( aah.getValue( ) != null )
					{
						binding.addArgument( new ScriptExpression( aah.getValue( ) ) );
					}
				}

				List aggrList = column.getAggregateOnList( );

				if ( aggrList != null )
				{
					for ( Iterator aggrItr = aggrList.iterator( ); aggrItr.hasNext( ); )
					{
						String baseLevel = (String) aggrItr.next( );

						CrosstabUtil.addHierachyAggregateOn( module,
								binding,
								baseLevel,
								rowLevelNameList,
								columnLevelNameList,
								cache );
					}
				}
				bindingList.add( binding );
			}
		}
		return bindingList.toArray( new IBinding[bindingList.size( )] );
	}

	private void setDataFieldExpression( String expression )
	{
		if ( expression != null )
		{
			// if ( cmbDataField != null && !cmbDataField.isDisposed( ) )
			// {
			// cmbDataField.setText( expression );
			// }
			if ( txtExpression != null && !txtExpression.isDisposed( ) )
			{
				txtExpression.setText( expression );
			}
		}
	}

	private void setName( String name )
	{
		if ( name != null && txtName != null )
			txtName.setText( name );
	}

	private void setDisplayName( String displayName )
	{
		if ( displayName != null && txtDisplayName != null )
			txtDisplayName.setText( displayName );
	}

	private void setTypeSelect( String typeSelect )
	{
		if ( cmbType != null )
		{
			if ( typeSelect != null )
				cmbType.select( getItemIndex( cmbType.getItems( ), typeSelect ) );
			else
				cmbType.select( 0 );
		}
	}

	private int getItemIndex( String[] items, String item )
	{
		for ( int i = 0; i < items.length; i++ )
		{
			if ( items[i].equals( item ) )
				return i;
		}
		return -1;
	}

	private void createAggregateSection( Composite composite )
	{

		new Label( composite, SWT.NONE ).setText( FUNCTION );
		cmbFunction = new Combo( composite, SWT.BORDER | SWT.READ_ONLY );
		GridData gd = new GridData( GridData.FILL_HORIZONTAL );
		gd.horizontalSpan = 2;
		cmbFunction.setLayoutData( gd );

		// WidgetUtil.createGridPlaceholder( composite, 1, false );

		cmbFunction.addSelectionListener( new SelectionAdapter( ) {

			public void widgetSelected( SelectionEvent e )
			{
				handleFunctionSelectEvent( );
				validate( );
			}
		} );

		paramsComposite = new Composite( composite, SWT.NONE );
		GridData gridData = new GridData( GridData.FILL_HORIZONTAL );
		gridData.horizontalSpan = 3;
		gridData.exclude = true;
		paramsComposite.setLayoutData( gridData );
		GridLayout layout = new GridLayout( );
		// layout.horizontalSpacing = layout.verticalSpacing = 0;
		layout.marginWidth = layout.marginHeight = 0;
		layout.numColumns = 3;
		paramsComposite.setLayout( layout );

		new Label( composite, SWT.NONE ).setText( FILTER_CONDITION );
		txtFilter = new Text( composite, SWT.BORDER );
		gridData = new GridData( GridData.FILL_HORIZONTAL );
		txtFilter.setLayoutData( gridData );

		createExpressionButton( composite, txtFilter );

		Label lblAggOn = new Label( composite, SWT.NONE );
		lblAggOn.setText( AGGREGATE_ON );
		gridData = new GridData( );
		gridData.verticalAlignment = GridData.BEGINNING;
		lblAggOn.setLayoutData( gridData );

		cmbAggOn = new Combo( composite, SWT.BORDER | SWT.READ_ONLY );
		gridData = new GridData( GridData.FILL_HORIZONTAL );
		gridData.horizontalSpan = 2;
		cmbAggOn.setLayoutData( gridData );

	}

	private void createCommonSection( Composite composite )
	{
		new Label( composite, SWT.NONE ).setText( EXPRESSION );
		txtExpression = new Text( composite, SWT.BORDER );
		txtExpression.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
		createExpressionButton( composite, txtExpression );
		txtExpression.addModifyListener( new ModifyListener( ) {

			public void modifyText( ModifyEvent e )
			{
				validate( );
			}

		} );
	}

	private void createMessageSection( Composite composite )
	{
		messageLine = new CLabel( composite, SWT.LEFT );
		GridData layoutData = new GridData( GridData.FILL_HORIZONTAL );
		layoutData.horizontalSpan = 3;
		messageLine.setLayoutData( layoutData );
	}

	protected void handleFunctionSelectEvent( )
	{
		Control[] children = paramsComposite.getChildren( );
		for ( int i = 0; i < children.length; i++ )
		{
			children[i].dispose( );
		}

		IAggrFunction function = getFunctionByDisplayName( cmbFunction.getText( ) );
		if ( function != null )
		{
			paramsMap.clear( );
			IParameterDefn[] params = function.getParameterDefn( );
			if ( params.length > 0 )
			{
				( (GridData) paramsComposite.getLayoutData( ) ).exclude = false;
				( (GridData) paramsComposite.getLayoutData( ) ).heightHint = SWT.DEFAULT;
				for ( final IParameterDefn param : params )
				{
					Label lblParam = new Label( paramsComposite, SWT.NONE );
					lblParam.setText( param.getDisplayName( ) + ":" ); //$NON-NLS-1$
					GridData gd = new GridData( );
					gd.widthHint = lbName.getBounds( ).width
							- lbName.getBorderWidth( );
					lblParam.setLayoutData( gd );

					if ( param.isDataField( ) )
					{
						final Combo cmbDataField = new Combo( paramsComposite,
								SWT.BORDER );
						cmbDataField.setLayoutData( GridDataFactory.fillDefaults( )
								.grab( true, false )
								.span( 2, 1 )
								.create( ) );

						initDataFields( cmbDataField, param );

						cmbDataField.addModifyListener( new ModifyListener( ) {

							public void modifyText( ModifyEvent e )
							{
								validate( );
								paramsValueMap.put( param.getName( ),
										cmbDataField.getText( ) );
							}
						} );

						paramsMap.put( param.getName( ), cmbDataField );
					}
					else
					{
						final Text txtParam = new Text( paramsComposite,
								SWT.BORDER );
						initTextField( txtParam, param );
						txtParam.addModifyListener( new ModifyListener( ) {

							public void modifyText( ModifyEvent e )
							{
								validate( );
								paramsValueMap.put( param.getName( ),
										txtParam.getText( ) );
							}
						} );
						GridData gridData = new GridData( GridData.FILL_HORIZONTAL );
						gridData.horizontalIndent = 0;
						txtParam.setLayoutData( gridData );
						createExpressionButton( paramsComposite, txtParam );
						paramsMap.put( param.getName( ), txtParam );
					}
				}
			}
			else
			{
				( (GridData) paramsComposite.getLayoutData( ) ).heightHint = 0;
				// ( (GridData) argsComposite.getLayoutData( ) ).exclude = true;
			}

			// this.cmbDataField.setEnabled( function.needDataField( ) );
			try
			{
				cmbType.setText( getDataTypeDisplayName( DataAdapterUtil.adapterToModelDataType( DataUtil.getAggregationManager( )
						.getAggregation( function.getName( ) )
						.getDataType( ) ) ) );
			}
			catch ( BirtException e )
			{
				ExceptionHandler.handle( e );
			}
		}
		else
		{
			( (GridData) paramsComposite.getLayoutData( ) ).heightHint = 0;
			// ( (GridData) argsComposite.getLayoutData( ) ).exclude = true;
			// new Label( argsComposite, SWT.NONE ).setText( "no args" );
		}
		composite.layout( true );
		setContentSize( composite );
	}

	private void createExpressionButton( final Composite parent, final Text text )
	{
		Button expressionButton = new Button( parent, SWT.PUSH );

		if ( expressionProvider == null )
		{
			if ( isAggregate( ) )
				expressionProvider = new CrosstabAggregationExpressionProvider( this.bindingHolder,
						this.binding );
			else
				expressionProvider = new CrosstabBindingExpressionProvider( this.bindingHolder,
						this.binding );
		}

		UIUtil.setExpressionButtonImage( expressionButton );
		expressionButton.addSelectionListener( new SelectionAdapter( ) {

			public void widgetSelected( SelectionEvent e )
			{
				ExpressionBuilder expression = new ExpressionBuilder( text.getText( ) );
				expression.setExpressionProvier( expressionProvider );

				if ( expression.open( ) == Window.OK )
				{
					if ( expression.getResult( ) != null )
						text.setText( expression.getResult( ) );
				}
			}
		} );
	}

	private String getArgumentDisplayNameByName( String functionName,
			String argument )
	{
		try
		{
			IAggrFunction function = DataUtil.getAggregationManager( )
					.getAggregation( functionName );
			for ( IParameterDefn param : function.getParameterDefn( ) )
			{
				if ( param.getName( ).equals( argument ) )
					return param.getDisplayName( );
			}
		}
		catch ( BirtException e )
		{
			ExceptionHandler.handle( e );
		}
		return null;
	}

	public void validate( )
	{
		if ( txtName != null
				&& ( txtName.getText( ) == null || txtName.getText( )
						.trim( )
						.equals( "" ) ) ) //$NON-NLS-1$
		{
			dialog.setCanFinish( false );
		}
		else if ( txtExpression != null
				&& ( txtExpression.getText( ) == null || txtExpression.getText( )
						.trim( )
						.equals( "" ) ) ) //$NON-NLS-1$
		{
			dialog.setCanFinish( false );
		}
		else
		{
			if ( this.binding == null )// create bindnig, we should check if
			// the binding name already exists.
			{
				for ( Iterator iterator = this.bindingHolder.getColumnBindings( )
						.iterator( ); iterator.hasNext( ); )
				{
					ComputedColumnHandle computedColumn = (ComputedColumnHandle) iterator.next( );
					if ( computedColumn.getName( ).equals( txtName.getText( ) ) )
					{
						dialog.setCanFinish( false );
						this.messageLine.setText( Messages.getFormattedString( "BindingDialogHelper.error.nameduplicate", //$NON-NLS-1$
								new Object[]{
									txtName.getText( )
								} ) );
						this.messageLine.setImage( PlatformUI.getWorkbench( )
								.getSharedImages( )
								.getImage( ISharedImages.IMG_OBJS_ERROR_TSK ) );
						return;
					}
				}
			}
			// bugzilla 273368
			// if expression is "measure['...']", aggregation do not support
			// IAggrFunction.RUNNING_AGGR function
			IAggrFunction function = getFunctionByDisplayName( cmbFunction.getText( ) );
			IParameterDefn[] params = function.getParameterDefn( );
			if ( params.length > 0 )
			{
				for ( final IParameterDefn param : params )
				{

					if ( param.isDataField( ) )
					{
						Combo cmbDataField = (Combo) paramsMap.get( param.getName( ) );
						String expression = cmbDataField.getText( );
						DataRequestSession session = null;
						try
						{
							session = DataRequestSession.newSession( new DataSessionContext( DataSessionContext.MODE_DIRECT_PRESENTATION ) );
							if ( session.getCubeQueryUtil( )
									.getReferencedMeasureName( expression ) != null
									&& function.getType( ) == IAggrFunction.RUNNING_AGGR )
							{
								dialog.setCanFinish( false );
								this.messageLine.setText( Messages.getFormattedString( "BindingDialogHelper.error.improperexpression", //$NON-NLS-1$
										new Object[]{
											function.getName( )
										} ) );
								this.messageLine.setImage( PlatformUI.getWorkbench( )
										.getSharedImages( )
										.getImage( ISharedImages.IMG_OBJS_ERROR_TSK ) );
								return;
							}
						}
						catch ( BirtException e )
						{
						}
						finally
						{
							if ( session != null )
							{
								session.shutdown( );
							}
						}
					}
				}
			}

			dialog.setCanFinish( true );
			this.messageLine.setText( "" ); //$NON-NLS-1$
			this.messageLine.setImage( null );

			if ( txtExpression != null
					&& ( txtExpression.getText( ) == null || txtExpression.getText( )
							.trim( )
							.equals( "" ) ) ) //$NON-NLS-1$
			{
				dialog.setCanFinish( false );
				return;
			}
			if ( isAggregate( ) )
			{
				try
				{
					IAggrFunction aggregation = DataUtil.getAggregationManager( )
							.getAggregation( getFunctionByDisplayName( cmbFunction.getText( ) ).getName( ) );

					if ( aggregation.getParameterDefn( ).length > 0 )
					{
						IParameterDefn[] parameters = aggregation.getParameterDefn( );
						for ( IParameterDefn param : parameters )
						{
							if ( !param.isOptional( ) )
							{
								String paramValue = getControlValue( paramsMap.get( param.getName( ) ) );
								if ( paramValue == null
										|| paramValue.trim( ).equals( "" ) ) //$NON-NLS-1$
								{
									dialog.setCanFinish( false );
									return;
								}
							}
						}
					}
				}
				catch ( BirtException e )
				{
					// TODO show error message in message panel
				}
			}
			dialog.setCanFinish( true );
		}
	}

	public boolean differs( ComputedColumnHandle binding )
	{
		if ( isAggregate( ) )
		{
			if ( !strEquals( binding.getName( ), txtName.getText( ) ) )
				return true;
			if ( !strEquals( binding.getDisplayName( ),
					txtDisplayName.getText( ) ) )
				return true;
			if ( !strEquals( binding.getDataType( ), getDataType( ) ) )
				return true;
			try
			{
				if ( !strEquals( DataAdapterUtil.adaptModelAggregationType( binding.getAggregateFunction( ) ),
						getFunctionByDisplayName( cmbFunction.getText( ) ).getName( ) ) )
					return true;
			}
			catch ( AdapterException e )
			{
			}
			if ( !strEquals( binding.getFilterExpression( ),
					txtFilter.getText( ) ) )
				return true;
			if ( !strEquals( cmbAggOn.getText( ),
					DEUtil.getAggregateOn( binding ) ) )
				return true;

			IAggrFunction function = getFunctionByDisplayName( cmbFunction.getText( ) );
			if ( function != null )
			{
				IParameterDefn[] params = function.getParameterDefn( );
				for ( final IParameterDefn param : params )
				{
					if ( paramsMap.containsKey( param.getName( ) ) )
					{
						String paramValue = getControlValue( paramsMap.get( param.getName( ) ) );
						for ( Iterator iterator = binding.argumentsIterator( ); iterator.hasNext( ); )
						{
							AggregationArgumentHandle handle = (AggregationArgumentHandle) iterator.next( );
							if ( param.getName( ).equals( handle.getName( ) )
									&& !strEquals( handle.getValue( ),
											paramValue ) )
							{
								return true;
							}
						}
						if ( param.isDataField( )
								&& binding.getExpression( ) != null
								&& !strEquals( binding.getExpression( ),
										paramValue ) )
						{
							return true;
						}
					}
				}
			}
		}
		else
		{
			if ( !strEquals( txtName.getText( ), binding.getName( ) ) )
				return true;
			if ( !strEquals( txtDisplayName.getText( ),
					binding.getDisplayName( ) ) )
				return true;
			if ( !strEquals( getDataType( ), binding.getDataType( ) ) )
				return true;
			if ( !strEquals( txtExpression.getText( ), binding.getExpression( ) ) )
				return true;
		}
		return false;
	}

	private String getControlValue( Control control )
	{
		if ( control instanceof Text )
		{
			return ( (Text) control ).getText( );
		}
		else if ( control instanceof Combo )
		{
			return ( (Combo) control ).getText( );
		}
		return null;
	}

	private boolean strEquals( String left, String right )
	{
		if ( left == right )
			return true;
		if ( left == null )
			return "".equals( right ); //$NON-NLS-1$
		if ( right == null )
			return "".equals( left ); //$NON-NLS-1$
		return left.equals( right );
	}

	private String getDataType( )
	{
		for ( int i = 0; i < DATA_TYPE_CHOICES.length; i++ )
		{
			if ( DATA_TYPE_CHOICES[i].getDisplayName( )
					.equals( cmbType.getText( ) ) )
			{
				return DATA_TYPE_CHOICES[i].getName( );
			}
		}
		return ""; //$NON-NLS-1$
	}

	public ComputedColumnHandle editBinding( ComputedColumnHandle binding )
			throws SemanticException
	{
		if ( isAggregate( ) )
		{
			binding.setDisplayName( txtDisplayName.getText( ) );

			for ( int i = 0; i < DATA_TYPE_CHOICES.length; i++ )
			{
				if ( DATA_TYPE_CHOICES[i].getDisplayName( )
						.equals( cmbType.getText( ) ) )
				{
					binding.setDataType( DATA_TYPE_CHOICES[i].getName( ) );
					break;
				}
			}

			binding.setAggregateFunction( getFunctionByDisplayName( cmbFunction.getText( ) ).getName( ) );
			binding.setFilterExpression( txtFilter.getText( ) );

			binding.clearAggregateOnList( );
			String aggStr = cmbAggOn.getText( );
			StringTokenizer token = new StringTokenizer( aggStr, "," ); //$NON-NLS-1$

			while ( token.hasMoreTokens( ) )
			{
				String agg = token.nextToken( );
				if ( !agg.equals( ALL ) )
					binding.addAggregateOn( agg );
			}

			// remove expression created in old version.
			binding.setExpression( null );
			binding.clearArgumentList( );

			for ( Iterator iterator = paramsMap.keySet( ).iterator( ); iterator.hasNext( ); )
			{
				String arg = (String) iterator.next( );
				String value = getControlValue( paramsMap.get( arg ) );
				if ( value != null )
				{
					AggregationArgument argHandle = StructureFactory.createAggregationArgument( );
					argHandle.setName( arg );
					argHandle.setValue( value );
					binding.addArgument( argHandle );
				}
			}
		}
		else
		{
			for ( int i = 0; i < DATA_TYPE_CHOICES.length; i++ )
			{
				if ( DATA_TYPE_CHOICES[i].getDisplayName( )
						.equals( cmbType.getText( ) ) )
				{
					binding.setDataType( DATA_TYPE_CHOICES[i].getName( ) );
					break;
				}
			}
			binding.setDisplayName( txtDisplayName.getText( ) );
			binding.setExpression( txtExpression.getText( ) );
		}
		return binding;
	}

	public ComputedColumnHandle newBinding( ReportItemHandle bindingHolder,
			String name ) throws SemanticException
	{
		ComputedColumn column = StructureFactory.newComputedColumn( bindingHolder,
				name == null ? txtName.getText( ) : name );
		ComputedColumnHandle binding = DEUtil.addColumn( bindingHolder,
				column,
				true );
		return editBinding( binding );
	}

	public void setContainer( Object container )
	{
		this.container = container;
	}

	public boolean canProcessAggregation( )
	{
		return true;
	}

}

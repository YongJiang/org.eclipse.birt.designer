/*******************************************************************************
 * Copyright (c) 2005, 2008 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.report.designer.data.ui.dataset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.birt.data.engine.api.IParameterMetaData;
import org.eclipse.birt.data.engine.impl.ParameterMetaData;
import org.eclipse.birt.report.data.adapter.api.DataAdapterUtil;
import org.eclipse.birt.report.designer.data.ui.property.AbstractDescriptionPropertyPage;
import org.eclipse.birt.report.designer.data.ui.util.ControlProvider;
import org.eclipse.birt.report.designer.data.ui.util.DataSetProvider;
import org.eclipse.birt.report.designer.data.ui.util.Utility;
import org.eclipse.birt.report.designer.internal.ui.processor.ElementProcessorFactory;
import org.eclipse.birt.report.designer.internal.ui.util.ExceptionHandler;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.designer.ui.dialogs.ExpressionBuilder;
import org.eclipse.birt.report.designer.ui.dialogs.ParameterDialog;
import org.eclipse.birt.report.model.adapter.oda.ReportParameterAdapter;
import org.eclipse.birt.report.model.api.DataSetHandle;
import org.eclipse.birt.report.model.api.DataSetParameterHandle;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.JointDataSetHandle;
import org.eclipse.birt.report.model.api.OdaDataSetHandle;
import org.eclipse.birt.report.model.api.OdaDataSetParameterHandle;
import org.eclipse.birt.report.model.api.ParameterHandle;
import org.eclipse.birt.report.model.api.PropertyHandle;
import org.eclipse.birt.report.model.api.ScalarParameterHandle;
import org.eclipse.birt.report.model.api.SlotHandle;
import org.eclipse.birt.report.model.api.activity.NotificationEvent;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.eclipse.birt.report.model.api.command.ContentException;
import org.eclipse.birt.report.model.api.command.NameException;
import org.eclipse.birt.report.model.api.core.Listener;
import org.eclipse.birt.report.model.api.elements.DesignChoiceConstants;
import org.eclipse.birt.report.model.api.elements.structures.DataSetParameter;
import org.eclipse.birt.report.model.api.elements.structures.OdaDataSetParameter;
import org.eclipse.birt.report.model.api.metadata.PropertyValueException;
import org.eclipse.birt.report.model.api.util.StringUtil;
import org.eclipse.birt.report.model.core.Structure;
import org.eclipse.birt.report.model.util.DataTypeConversionUtil;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * Property page to define dataset parameters. If they could be retrieved from
 * DataSetParameter metadata, they will be displayed automatically. Five
 * properties will be shown in this page, which is name, data type, mode,
 * default value, and linked <code>ScalarParameter</code> parameter. User
 * could edit those properties to construct data set parameter for query.
 */
public class DataSetParametersPage extends AbstractDescriptionPropertyPage
		implements
			Listener
{

	private boolean modelChanged = true;
	private PropertyHandle parameters;
	private PropertyHandleTableViewer viewer;
	private DataSetParameter originalStructure = null;

	private String parameterName;


	private static String DEFAULT_MESSAGE = Messages.getString( "dataset.editor.parameters" ); //$NON-NLS-1$
	private static String NONE_DEFAULT_VALUE = Messages.getString( "DataSetParametersPage.default.None" );//$NON-NLS-1$
	private static String UNLINKED_REPORT_PARAM = Messages.getString( "DataSetParametersPage.reportParam.None" );//$NON-NLS-1$
	private static final char RENAME_SEPARATOR = '_'; //$NON-NLS-1$
	private static final String PARAM_PREFIX = "param" + RENAME_SEPARATOR; //$NON-NLS-1$

	/**
	 * constructor
	 */
	public DataSetParametersPage( )
	{
		super( );
	}

	/*
	 * @see org.eclipse.birt.report.designer.ui.dialogs.properties.AbstractDescriptionPropertyPage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	public Control createContents( Composite parent )
	{
		parameters = ( (DataSetHandle) getContainer( ).getModel( ) ).getPropertyHandle( DataSetHandle.PARAMETERS_PROP );
		return createParameterPageControl( parent );
	}

	/**
	 * Create parameter page control, if the datasetHandle is ODA, add the
	 * "linked with report parameter" cell.
	 * 
	 * @param parent
	 * @return
	 */
	private Control createParameterPageControl( Composite parent )
	{
		viewer = new PropertyHandleTableViewer( parent, true, true, true );
		TableColumn column = new TableColumn( viewer.getViewer( ).getTable( ),
				SWT.LEFT );
		column.setText( " " ); //$NON-NLS-1$
		column.setResizable( false );
		column.setWidth( 23 );
		DataSetHandle dataSetHandle = (DataSetHandle) getContainer( ).getModel( );
		boolean isOdaDataSetHandle = ParameterPageUtil.isOdaDataSetHandle( dataSetHandle );
		boolean isJointDataSetHandle = ParameterPageUtil.isJointDataSetHandle( dataSetHandle );
		
		if ( isOdaDataSetHandle )
		{
			String[] cellLabels = ParameterPageUtil.odaCellLabels;
			column = new TableColumn( viewer.getViewer( ).getTable( ), SWT.LEFT );
			column.setText( cellLabels[0] );
			column.setWidth( 100 );

			column = new TableColumn( viewer.getViewer( ).getTable( ), SWT.LEFT );
			column.setText( cellLabels[1] );
			column.setWidth( 100 );

			column = new TableColumn( viewer.getViewer( ).getTable( ), SWT.LEFT );
			column.setText( cellLabels[2] );
			column.setWidth( 100 );

			column = new TableColumn( viewer.getViewer( ).getTable( ), SWT.LEFT );
			column.setText( cellLabels[3] );
			column.setWidth( 100 );

			column = new TableColumn( viewer.getViewer( ).getTable( ), SWT.LEFT );
			column.setText( cellLabels[4] );
			column.setWidth( 100 );

			column = new TableColumn( viewer.getViewer( ).getTable( ), SWT.LEFT );
			column.setText( cellLabels[5] );
			column.setWidth( 180 );
		}
		else
		{
			String[] cellLabels = ParameterPageUtil.cellLabels;

			column = new TableColumn( viewer.getViewer( ).getTable( ), SWT.LEFT );
			column.setText( cellLabels[0] );
			column.setWidth( 100 );

			column = new TableColumn( viewer.getViewer( ).getTable( ), SWT.LEFT );
			column.setText( cellLabels[1] );
			column.setWidth( 100 );

			column = new TableColumn( viewer.getViewer( ).getTable( ), SWT.LEFT );
			column.setText( cellLabels[2] );
			column.setWidth( 100 );

			column = new TableColumn( viewer.getViewer( ).getTable( ), SWT.LEFT );
			column.setText( cellLabels[3] );
			column.setWidth( 100 );
			
			if ( isJointDataSetHandle )
			{
				column = new TableColumn( viewer.getViewer( ).getTable( ),
						SWT.LEFT );
				column.setText( ParameterPageUtil.odaCellLabels[5] );
				column.setWidth( 180 );
			}
		}

		viewer.getViewer( )
				.setContentProvider( new ParameterViewContentProvider( ) );
		viewer.getViewer( )
				.setLabelProvider( new ParameterViewLableProvider( dataSetHandle ) );
		adjustParameterOnPosition( parameters );
		if ( isJointDataSetHandle )
		{
			viewer.getViewer( ).setInput( (JointDataSetHandle)dataSetHandle );
		}
		else
		{
			viewer.getViewer( ).setInput( parameters );
		}
		setToolTips( );
		if ( !ParameterPageUtil.isJointDataSetHandle( dataSetHandle ) )
		{
			addRefreshMenu( );
			addListeners( );
			dataSetHandle.addListener( this );
		}
		return viewer.getControl( );
	}

	/**
	 * Add refresh menu to refresh the parameter.If the parameter metadata could
	 * get from database, list the parameter
	 * 
	 */
	private void addRefreshMenu( )
	{
		MenuItem itmRefresh = new MenuItem( viewer.getMenu( ), SWT.NONE, 0 );
		new MenuItem( viewer.getMenu( ), SWT.SEPARATOR, 1 );
		itmRefresh.setText( Messages.getString( "parameters.menuItem.reset" ) ); //$NON-NLS-1$
		itmRefresh.addSelectionListener( new SelectionAdapter( ) {

			public void widgetSelected( SelectionEvent e )
			{
				PropertyHandle handle = (PropertyHandle) viewer.getViewer( )
						.getInput( );
				try
				{
					handle.clearValue( );
				}
				catch ( SemanticException e1 )
				{
				}
				refreshParameters( );
				viewer.getViewer( ).refresh( );
			}
		} );
	}

	/**
	 * get the right direction for different dataset type
	 * 
	 * @return
	 */
	private String[] getDirections( )
	{
		String[] directions;
		boolean supportInput = ( (DataSetEditor) this.getContainer( ) ).supportsInParameters( );
		boolean supportOutput = ( (DataSetEditor) this.getContainer( ) ).supportsOutputParameters( );
		if ( supportInput && supportOutput )
		{
			directions = new String[]{
					Messages.getString( "label.input" ), //$NON-NLS-1$
					Messages.getString( "label.output" ), //$NON-NLS-1$
					Messages.getString( "label.inputOutput" ) //$NON-NLS-1$
			};
		}
		else if ( supportInput )
		{
			directions = new String[]{
				Messages.getString( "label.input" ) //$NON-NLS-1$
			};
		}
		else if ( supportOutput )
		{
			directions = new String[]{
				Messages.getString( "label.output" ) //$NON-NLS-1$
			};
		}
		else
		{
			directions = new String[]{
					Messages.getString( "label.input" ), //$NON-NLS-1$
					Messages.getString( "label.output" ), //$NON-NLS-1$
					Messages.getString( "label.inputOutput" ) //$NON-NLS-1$
			};
		}
		return directions;
	}

	/**
	 * 
	 *
	 */
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
					refreshMessage( );
				}
			}

		} );

		viewer.getRemoveButton( )
				.addSelectionListener( new SelectionListener( ) {

					public void widgetSelected( SelectionEvent e )
					{
						setPageProperties( );
						refreshMessage( );
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
						refreshMessage( );
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
						refreshMessage( );
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
		DataSetParameter newParam = null;
		if ( ParameterPageUtil.isOdaDataSetHandle( (DataSetHandle) getContainer( ).getModel( ) ) )
			newParam = new OdaDataSetParameter( );
		else
			newParam = new DataSetParameter( );

		int position = viewer.getViewer( ).getTable( ).getItemCount( );
		newParam.setName( getUniqueName( ) );
		newParam.setIsInput( true );
		newParam.setPosition( new Integer( position + 1 ) );
		
		doEdit( newParam );
	}

	private void doEdit( )
	{
		int index = viewer.getViewer( ).getTable( ).getSelectionIndex( );
		if ( index == -1 )
			return;

		DataSetParameterHandle handle = (DataSetParameterHandle) viewer.getViewer( )
				.getTable( )
				.getItem( index )
				.getData( );
		originalStructure = (DataSetParameter) handle.getStructure( ).copy( );

		doEdit( handle );
	}

	private void doEdit( Object structureOrHandle )
	{
		ParameterInputDialog dlg = new ParameterInputDialog( structureOrHandle,
				ParameterPageUtil.isOdaDataSetHandle( (DataSetHandle) getContainer( ).getModel( ) ) );

		if ( dlg.open( ) == Window.OK )
		{
			viewer.getViewer( ).refresh( );
			refreshMessage( );
			refreshLinkedReportParamStatus( );
		}
	}
	
	private void refreshLinkedReportParamStatus( )
	{
		TableItem items[] = viewer.getViewer( ).getTable( ).getItems( );
		for ( int i = 0; i < items.length; i++ )
		{
			if ( items[i].getData( ) instanceof OdaDataSetParameterHandle )
			{
				OdaDataSetParameterHandle handle = (OdaDataSetParameterHandle) items[i].getData( );
				if ( handle.getParamName( ) != null
						&& !doesLinkedReportParamExist( handle ) )
				{
					getContainer( ).setMessage( Messages.getFormattedString( "DataSetParametersPage.errorMessage.LinkedReportParamNotFound",
							new Object[]{
									handle.getParamName( ), handle.getName( )
							} ),
							IMessageProvider.ERROR );
					viewer.getViewer( ).refresh( );
					break;
				}
			}
		}
	}

	private boolean doesLinkedReportParamExist( OdaDataSetParameterHandle handle )
	{
		String reportParamNames[] = ParameterPageUtil.getLinkedReportParameterNames( handle );
		boolean exists = false;
		for ( int i = 0; i < reportParamNames.length; i++ )
		{
			if ( reportParamNames[i].equalsIgnoreCase( handle.getParamName( ) ) )
			{
				exists = true;
				break;
			}
		}
		return exists;
	}
	
	private void refreshMessage( )
	{
		getContainer( ).setMessage( DEFAULT_MESSAGE, IMessageProvider.NONE );
		if ( !doSaveEmptyParameter( parameters ) )
		{
			getContainer( ).setMessage( Messages.getFormattedString( "dataset.editor.error.noInputParameterDefaultValue", new Object[]{this.getNoneValuedParameterName( )} ), IMessageProvider.ERROR ); //$NON-NLS-1$
		}
	}
	
	private DataSetParameter getStructure( Object structureOrHandle )
	{
		DataSetParameter structure = null;
		if ( structureOrHandle instanceof DataSetParameter )
		{
			structure = (DataSetParameter) structureOrHandle;
		}
		else
		{
			structure = (DataSetParameter) ( (DataSetParameterHandle) structureOrHandle ).getStructure( );
		}

		return structure;
	}
	
	/**
	 * adjust the parameter position based on its native position if it is available 
	 * @param handle
	 */
	private void adjustParameterOnPosition( PropertyHandle handle )
	{
		if ( handle.getListValue( ) != null )
		{
			for ( int i = 1; i <= handle.getListValue( ).size( ); i++ )
			{
				for ( int position = i - 1; position < handle.getListValue( )
						.size( ); position++ )
				{
					DataSetParameterHandle param = ( (DataSetParameterHandle) handle.getAt( position ) );
					if ( param.getPosition( ).intValue( ) == i )
					{
						try
						{
							handle.moveItem( position, i - 1 );
							break;
						}
						catch ( PropertyValueException e )
						{
						}
					}
				}
			}
		}
	}
	
    private void setToolTips( )
	{
		viewer.getNewButton( )
				.setToolTipText( Messages.getString( "DataSetParameterPage.toolTipText.New" ) );//$NON-NLS-1$
		viewer.getEditButton( )
				.setToolTipText( Messages.getString( "DataSetParameterPage.toolTipText.Edit" ) );//$NON-NLS-1$
		viewer.getRemoveButton( )
				.setToolTipText( Messages.getString( "DataSetParameterPage.toolTipText.Remove" ) );//$NON-NLS-1$
		viewer.getUpButton( )
				.setToolTipText( Messages.getString( "DataSetParameterPage.toolTipText.Up" ) );//$NON-NLS-1$
		viewer.getDownButton( )
				.setToolTipText( Messages.getString( "DataSetParameterPage.toolTipText.Down" ) );//$NON-NLS-1$
	}

	/**
	 * 
	 * @param isInput
	 * @param isOutput
	 * @return
	 */
	protected final String getDirectionDisplayName( boolean isInput,
			boolean isOutput )
	{
		if ( isInput && isOutput )
		{
			return ParameterPageUtil.directions[2];
		}
		else if ( isOutput )
		{
			return ParameterPageUtil.directions[1];
		}
		return ParameterPageUtil.directions[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.ui.dialogs.properties.IPropertyPage#pageActivated()
	 */
	public void pageActivated( )
	{
		refreshMessage( );

		if ( viewer != null && this.modelChanged )
		{
			modelChanged = false;
			List invalidReportParam = getMultipleValueReportParameter( parameters );
			if ( !invalidReportParam.isEmpty( ) )
			{
				getContainer( ).setMessage( Messages.getFormattedString( "dataset.editor.error.invalidLinkedParameter", //$NON-NLS-1$
						invalidReportParam.toArray( ) ),
						IMessageProvider.ERROR );
			}
			adjustParameterOnPosition( parameters );
			viewer.getViewer( ).refresh( );
		}
		// The properties of the various controls on the page
		// will be set depending on the parameters
		setPageProperties( );
		refreshLinkedReportParamStatus( );
		viewer.getViewer( ).getTable( ).select( 0 );
	}

	/**
	 * Refreshes the parameter list adds any new parameter retrieved. This
	 * method doesn't clear unused parameters. It is the users responsibility to
	 * delete individual parameters through the UI.
	 */
	private void refreshParameters( )
	{
		try
		{
			DataSetHandle ds = ( (DataSetHandle) getContainer( ).getModel( ) );
			Collection paramsFromDataSet = null;

			paramsFromDataSet = DataSetProvider.getCurrentInstance( )
					.getParametersFromDataSet( ds );

			// iterate through the list of parameters and find the parameter
			// with
			// the same position in the data set
			if ( paramsFromDataSet != null )
			{
				Iterator iter = paramsFromDataSet.iterator( );
				while ( iter.hasNext( ) )
				{
					IParameterMetaData paramFromDataSet = (IParameterMetaData) iter.next( );

					DataSetParameter parameter = null;
					if ( paramFromDataSet.getPosition( ) > 0 )
					{
						parameter = findParameterByPosition( paramFromDataSet.getPosition( ) );
					}
					else
					{
						parameter = findParameterByName( paramFromDataSet.getName( ) );
					}
					if ( parameter != null )
					{
						DataSetParameter newParameter = newParameter( paramFromDataSet );
						// if the parameter is unsame with new parameter,replace
						// the old one with the new one.or keep the old
						// parameter
						if ( !isSameParameters( parameter, newParameter ) )
						{
							parameters.replaceItem( parameter, newParameter );
						}
					}
					else
					{
						parameters.addItem( newParameter( paramFromDataSet ) );
					}
				}
				// if the paremeters has more value, remove these extra value
				if ( parameters.getListValue( ) != null
						&& paramsFromDataSet.size( ) < parameters.getListValue( )
								.size( ) )
				{
					int size = parameters.getListValue( ).size( );
					while ( size > paramsFromDataSet.size( ) )
					{
						parameters.removeItem( size - 1 );
						size = parameters.getListValue( ).size( );
					}
				}
				updateParams2UniqueName( parameters.getListValue( ) );
			}
			else
			{
				if ( viewer != null )
				{
					PropertyHandle handle = (PropertyHandle) viewer.getViewer( )
							.getInput( );
					handle.clearValue( );
				}
			}
			refreshPositions( );
			setPageProperties( );
		}
		catch ( Exception e )
		{
		}
	}

	/**
	 * create a parameter according to the parameter's metadata
	 * 
	 * @param paramFromDataSet
	 * @return
	 */
	private DataSetParameter newParameter( IParameterMetaData paramFromDataSet )
	{
		DataSetParameter parameter = null;
		if ( ParameterPageUtil.isOdaDataSetHandle( (DataSetHandle) getContainer( ).getModel( ) ) )
		{
			parameter = new OdaDataSetParameter( );
			if ( paramFromDataSet instanceof ParameterMetaData )
			{
				( (OdaDataSetParameter) parameter ).setNativeName( ( (ParameterMetaData) paramFromDataSet ).getNativeName( ) );
			}
			if ( "REF CURSOR".equals( paramFromDataSet.getNativeTypeName( ) ) )
				parameter.setNativeDataType( new Integer( -10 ) );
		}
		else
			parameter = new DataSetParameter( );
		try
		{
			parameter.setParameterDataType( DataAdapterUtil.coreDataTypeToModelDataType( paramFromDataSet.getDataTypeName( ) ) );
		}
		catch ( Exception e )
		{
		}
		parameter.setDefaultValue( paramFromDataSet.getDefaultInputValue( ) );
		if ( paramFromDataSet.isOptional( ) != null )
		{
			parameter.setIsOptional( paramFromDataSet.isOptional( )
					.booleanValue( ) );
		}
		if ( paramFromDataSet.isNullable( ) != null )
		{
			parameter.setAllowNull( paramFromDataSet.isNullable( )
					.booleanValue( ) );
		}

		if ( paramFromDataSet.isInputMode( ) != null )
		{
			parameter.setIsInput( paramFromDataSet.isInputMode( )
					.booleanValue( ) );
		}
		if ( paramFromDataSet.isOutputMode( ) != null )
		{
			parameter.setIsOutput( paramFromDataSet.isOutputMode( )
					.booleanValue( ) );
		}
		if ( ( paramFromDataSet.isInputMode( ) == null && paramFromDataSet.isOutputMode( ) == null )
				|| ( parameter.isInput( ) == false && parameter.isOutput( ) == false ) )
		{
			parameter.setIsInput( true );
		}

		if ( paramFromDataSet.getName( ) == null
				|| paramFromDataSet.getName( ).trim( ).length( ) == 0 )
			parameter.setName( getUniqueName( ) );
		else
			parameter.setName( paramFromDataSet.getName( ) );
		parameter.setPosition( new Integer( paramFromDataSet.getPosition( ) ) );
		return parameter;
	}

	/**
	 * whether the param1 is same with param2
	 * 
	 * @param param1
	 * @param param2
	 * @return
	 */
	private boolean isSameParameters( DataSetParameter param1,
			DataSetParameter param2 )
	{
		boolean isSame = false;
		if ( param1 == param2 )
			isSame = true;
		else if ( param1 == null || param2 == null )
			isSame = false;
		else if ( param1.getParameterDataType( ).equals( param2.getParameterDataType( ) )
				&& ( param1.isInput( ) == param2.isInput( ) )
				&& ( param1.isOutput( ) == param2.isOutput( ) ) )
			isSame = true;
		return isSame;
	}

	private void updateParams2UniqueName( List parameters )
	{
		List existedNames = collectParameterNames( parameters );

		List newNames = new ArrayList( );

		for ( int i = 0; i < parameters.size( ); i++ )
		{
			DataSetParameter param = (DataSetParameter) parameters.get( i );
			String name = param.getName( );
			if ( newNames.contains( name ) )
			{
				String prefix = name + RENAME_SEPARATOR;

				int n = 1;
				while ( true )
				{
					name = prefix + n;

					if ( !existedNames.contains( name )
							&& !newNames.contains( name ) )
						break;
					n++;
				}

				param.setName( name );
			}

			newNames.add( name );
		}
	}
	
	private static List collectParameterNames( List parameters )
	{
		List names = new ArrayList( );
		for ( int i = 0; i < parameters.size( ); i++ )
		{
			DataSetParameter param = (DataSetParameter) parameters.get( i );
			String name = param.getName( );
			if ( !StringUtil.isBlank( name ) && !names.contains( name ) )
				names.add( name );
		}

		return names;
	}
	
	/*
	 * @see org.eclipse.birt.report.model.core.Listener#elementChanged(org.eclipse.birt.report.model.api.DesignElementHandle,
	 *      org.eclipse.birt.report.model.activity.NotificationEvent)
	 */
	public void elementChanged( DesignElementHandle focus, NotificationEvent ev )
	{
		modelChanged = true;
	}

	protected final DataSetParameter findParameterByPosition( int position )
	{
		if ( parameters != null )
		{
			Iterator iter = parameters.iterator( );
			if ( iter != null )
			{
				while ( iter.hasNext( ) )
				{
					DataSetParameterHandle parameter = (DataSetParameterHandle) iter.next( );
					if ( parameter.getPosition( ) != null
							&& parameter.getPosition( ).intValue( ) == position )
					{
						return (DataSetParameter) parameter.getStructure( );
					}
				}
			}
		}
		return null;
	}

	protected final DataSetParameter findParameterByName( String name )
	{
		if ( parameters != null )
		{
			Iterator iter = parameters.iterator( );
			if ( iter != null )
			{
				while ( iter.hasNext( ) )
				{
					DataSetParameterHandle parameter = (DataSetParameterHandle) iter.next( );
					if ( name != null && name.equals( parameter.getName( ) ) )
					{
						return (DataSetParameter) parameter.getStructure( );
					}
				}
			}
		}
		return null;
	}

	/**
	 * Re-indexes the parameters starting at 1 from the 1st in the list.
	 */
	protected final void refreshPositions( )
	{
		if ( parameters == null )
		{
			parameters = ( (DataSetHandle) getContainer( ).getModel( ) ).getPropertyHandle( DataSetHandle.PARAMETERS_PROP );
		}
		if ( parameters != null )
		{
			int position = 1;
			Iterator iter = parameters.iterator( );
			if ( iter != null )
			{
				while ( iter.hasNext( ) )
				{
					DataSetParameterHandle parameter = (DataSetParameterHandle) iter.next( );
					if ( parameter instanceof OdaDataSetParameterHandle )
					{
						boolean hasNativeName = ( (OdaDataSetParameterHandle) parameter ).getNativeName( ) != null &&
								( (OdaDataSetParameterHandle) parameter ).getNativeName( )
										.trim( )
										.length( ) > 0;
						if ( !hasNativeName &&
								( parameter.getPosition( ) == null || parameter.getPosition( )
										.intValue( ) != position ) )
						{
							parameter.setPosition( new Integer( position ) );
						}
						else if ( hasNativeName &&
								parameter.getPosition( ) != null &&
								parameter.getPosition( ).intValue( ) > 0 &&
								parameter.getPosition( ).intValue( ) != position )
						{
							parameter.setPosition( new Integer( position ) );
						}
						position++;
					}
					else
					{
						parameter.setPosition( new Integer( position++ ) );
					}
				}
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	protected final String getUniqueName( )
	{
		int n = 1;
		StringBuffer buf = new StringBuffer( );
		while ( buf.length( ) == 0 )
		{
			buf.append( PARAM_PREFIX ).append( n++ );
			if ( parameters != null )
			{
				Iterator iter = parameters.iterator( );
				if ( iter != null )
				{
					while ( iter.hasNext( ) && buf.length( ) > 0 )
					{
						DataSetParameterHandle parameter = (DataSetParameterHandle) iter.next( );
						if ( buf.toString( )
								.equalsIgnoreCase( parameter.getName( ) ) )
						{
							buf.setLength( 0 );
						}
					}
				}
			}
		}
		return buf.toString( );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.birt.report.designer.ui.dialogs.properties.AbstractPropertyPage#performCancel()
	 */
	public boolean performCancel( )
	{
		//   selectorImage.dispose();
		( (DataSetHandle) getContainer( ).getModel( ) ).removeListener( this );
		return super.performCancel( );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.birt.report.designer.ui.dialogs.properties.AbstractPropertyPage#performOk()
	 */
	public boolean performOk( )
	{
		if ( doSaveEmptyParameter( parameters ) )
		{
			// selectorImage.dispose();
			refreshPositions( );
			if ( this.modelChanged
					&& this.getContainer( ) != null
					&& this.getContainer( ) instanceof DataSetEditor )
			{
				this.modelChanged = false;
				( (DataSetHandle) getContainer( ).getModel( ) ).removeListener( this );
				( (DataSetEditor) getContainer( ) ).updateDataSetDesign( this );
			}
			return super.performOk( );
		}
		else
		{
			String name = getNoneValuedParameterName( );
			boolean confirm = MessageDialog.openConfirm( null,
					Messages.getString( "dataset.editor.error.title" ), //$NON-NLS-1$
					Messages.getFormattedString( "dataset.editor.error.validationParameter", //$NON-NLS-1$
							new Object[]{
								name
							} ) );
			if ( confirm )
				( (DataSetEditor) getContainer( ) ).updateDataSetDesign( this );
			return confirm;
		}
	}

	/**
	 * whether to save empty parameter
	 * @param parameters
	 * @return
	 */
	private boolean doSaveEmptyParameter( PropertyHandle parameters )
	{
		if ( ParameterPageUtil.isJointDataSetHandle( (DataSetHandle) getContainer( ).getModel( ) ) )
		{
			return true;
		}
		if ( parameters == null )
		{
			parameters = ( (DataSetHandle) getContainer( ).getModel( ) ).getPropertyHandle( DataSetHandle.PARAMETERS_PROP );
		}
		if ( parameters != null )
		{
			Iterator iter = parameters.iterator( );
			String paramName = null;
			if ( iter != null )
			{
				while ( iter.hasNext( ) )
				{
					DataSetParameterHandle parameter = (DataSetParameterHandle) iter.next( );
					if ( ParameterPageUtil.isOdaDataSetHandle( (DataSetHandle) getContainer( ).getModel( ) ) )
						paramName = ( (OdaDataSetParameterHandle) parameter ).getParamName( );
					if ( parameter.isInput( )
							&& paramName == null
							&& ( parameter.getDefaultValue( ) == null || parameter.getDefaultValue( )
									.trim( )
									.length( ) == 0 ) ) //$NON-NLS-1$
					{
						setNoneValuedParameterName( parameter.getName( ) );
						return false;
					}
				}
			}
		}
		return true;
	}
	
	private List getMultipleValueReportParameter( PropertyHandle parameters )
	{
		List multipleValueList = new ArrayList( );
		if ( parameters == null )
		{
			parameters = ( (DataSetHandle) getContainer( ).getModel( ) ).getPropertyHandle( DataSetHandle.PARAMETERS_PROP );
		}
		if ( parameters != null &&
				(DataSetHandle) getContainer( ).getModel( ) instanceof OdaDataSetHandle )
		{
			Iterator iter = parameters.iterator( );
			String paramName = null;
			if ( iter != null )
			{
				boolean needsRefresh = true;
				while ( iter.hasNext( ) )
				{
					DataSetParameterHandle parameter = (DataSetParameterHandle) iter.next( );
					paramName = ( (OdaDataSetParameterHandle) parameter ).getParamName( );
					if ( paramName != null )
					{
						ScalarParameterHandle handle = ParameterPageUtil.getScalarParameter( paramName,
								needsRefresh );
						needsRefresh = false;
						if ( handle != null &&
								handle.getQualifiedName( ).equals( paramName ) &&
								( DesignChoiceConstants.SCALAR_PARAM_TYPE_MULTI_VALUE.equals( ( (ScalarParameterHandle) handle ).getParamType( ) ) ) )
						{
							multipleValueList.add( paramName );
						}
					}
				}
			}
		}
		return multipleValueList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.ui.dialogs.properties.AbstractPropertyPage#canLeave()
	 */
	public boolean canLeave( )
	{
		refreshPositions( );
		if ( this.modelChanged
				&& this.getContainer( ) != null
				&& this.getContainer( ) instanceof DataSetEditor )
		{
			this.modelChanged = false;
			( (DataSetEditor) getContainer( ) ).updateDataSetDesign( this );
		}
		return true;
	}

	/**
	 * whether the page can be finished and closed.
	 */
	public boolean canFinish( )
	{
		refreshPositions( );
		return doSaveEmptyParameter( parameters );
	}

	/**
	 * 
	 *
	 */
	private class ViewerSelectionListener implements ISelectionChangedListener
	{

		/*
		 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
		 */
		public void selectionChanged( SelectionChangedEvent event )
		{
			setPageProperties( );
		}
	}

	/**
	 * Depending on the value of the parameters the properties of various controls
	 * on this page are set
	 */
	private void setPageProperties( )
	{
		boolean parametersExist = false, isJointDataSet = ParameterPageUtil.isJointDataSetHandle( (DataSetHandle) getContainer( ).getModel( ) );

		parametersExist = ( parameters != null
				&& parameters.getListValue( ) != null && parameters.getListValue( )
				.size( ) > 0 );
		if ( viewer != null )
		{
			viewer.getNewButton( ).setEnabled( !isJointDataSet );
			viewer.getEditButton( ).setEnabled( !isJointDataSet
					&& parametersExist );
			viewer.getRemoveButton( ).setEnabled( !isJointDataSet
					&& parametersExist );
			viewer.getUpButton( ).setEnabled( !isJointDataSet
					&& parametersExist
					&& parameters.getListValue( ).size( ) > 1 );
			viewer.getDownButton( ).setEnabled( !isJointDataSet
					&& parametersExist
					&& parameters.getListValue( ).size( ) > 1 );
			viewer.getRemoveMenuItem( ).setEnabled( !isJointDataSet
					&& parametersExist );
			viewer.getRemoveAllMenuItem( ).setEnabled( !isJointDataSet
					&& parametersExist );
		}
		if ( parametersExist == false )
			getContainer( ).setMessage( DEFAULT_MESSAGE, IMessageProvider.NONE ); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.ui.dialogs.properties.IPropertyPage#getToolTip()
	 */
	public String getToolTip( )
	{
		return Messages.getString( "DataSetParametersPage.Filter.Tooltip" ); //$NON-NLS-1$
	}

	/**
	 */
	public String getNoneValuedParameterName( )
	{
		return parameterName;
	}

	/**
	 * 
	 * @param name
	 */
	private void setNoneValuedParameterName( String name )
	{
		this.parameterName = name;
	}

	/**
	 * Since the weak link between the dataset parameter and report parameter, we had to 
	 * 
	 */
	private void enableModelChanged( )
	{
		( (DataSetEditor) this.getContainer( ) ).enableLinkedParamChanged( );
	}
	
	/**
	 * The listener on scalar parameter
	 * 
	 */
	private class ScalarParameterListener implements Listener
	{

		/**
		 * 
		 */
		public void elementChanged( DesignElementHandle focus,
				NotificationEvent ev )
		{
			modelChanged = true;
			enableModelChanged( );
		}
	}

	private class ParameterViewContentProvider
			implements
				IStructuredContentProvider
	{
		private final String separator = "::";

		/*
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements( Object inputElement )
		{
			if ( inputElement == null )
				return new Object[0];

			if ( inputElement instanceof JointDataSetHandle )
			{
				JointDataSetHandle handle = (JointDataSetHandle) inputElement;
				List params = getSubDataSetParameters( handle, "", 0 );
				return params.toArray( );
			}

			if ( !( inputElement instanceof PropertyHandle ) )
				return new Object[0];

			Iterator iter = ( (PropertyHandle) inputElement ).iterator( );
			ArrayList params = new ArrayList( 10 );
			if ( iter != null )
			{
				while ( iter.hasNext( ) )
				{
					params.add( iter.next( ) );
				}
			}
            refreshPositions( );
			return params.toArray( );
		}

		/*
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose( )
		{
		}

		/*
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged( Viewer viewer, Object oldInput,
				Object newInput )
		{
		}
		
		/**
		 * Gets the list of all the sub data set parameters
		 * 
		 * @param jointDataSetHandle
		 * @param prefix
		 * @param count
		 * @return
		 */
		private List getSubDataSetParameters(
				JointDataSetHandle jointDataSetHandle, String prefix, int count )
		{
			List subDataSetParams = new ArrayList( );
			Iterator children = jointDataSetHandle.dataSetsIterator( );
			if ( jointDataSetHandle.getDataSetNames( ).size( ) == 1
					&& children.hasNext( ) )
			{
				DataSetHandle nextElement = (DataSetHandle) children.next( );
				prefix += nextElement.getName( );
				if ( nextElement instanceof JointDataSetHandle )
				{
					subDataSetParams.addAll( getSubDataSetParameters( (JointDataSetHandle) nextElement,
							prefix + "1" + separator, count ) );
					
					count += subDataSetParams.size( );
					
					subDataSetParams.addAll( getSubDataSetParameters( (JointDataSetHandle) nextElement,
							prefix + "2" + separator, count ) );
				}
				else
				{
					Iterator params = nextElement.parametersIterator( );
					while ( params.hasNext( ) )
					{
						count++;
						DataSetParameterHandle param = (DataSetParameterHandle) params.next( );
						DataSetParameter newParam1 = createDataSetParameter( param,
								prefix + "1", count++ );
						DataSetParameter newParam2 = createDataSetParameter( param,
								prefix + "2", count );
						subDataSetParams.add( newParam1 );
						subDataSetParams.add( newParam2 );
					}
				}
			}
			else
			{
				while ( children.hasNext( ) )
				{
					DataSetHandle nextElement = (DataSetHandle) children.next( );
					String preFixStr = prefix + nextElement.getName( );
					if ( nextElement instanceof JointDataSetHandle )
					{
						subDataSetParams.addAll( getSubDataSetParameters( (JointDataSetHandle) nextElement,
								preFixStr + separator, count ) );
						count += subDataSetParams.size( );
					}
					else
					{
						Iterator params = nextElement.parametersIterator( );
						while ( params.hasNext( ) )
						{
							count++;
							DataSetParameterHandle param = (DataSetParameterHandle) params.next( );
							subDataSetParams.add( createDataSetParameter( param,
									preFixStr, count ) );
						}
					}
				}
			}
			return subDataSetParams;
		}

		/**
		 * Creates a DataSetParameter instance according to the given arguments
		 * 
		 * @param parameter
		 * @param dataSetName
		 * @param position
		 * @return
		 */
		private DataSetParameter createDataSetParameter(
				DataSetParameterHandle parameter, String dataSetName, int position )
		{
			DataSetParameter dataSetParameter = null;
			if ( parameter instanceof OdaDataSetParameterHandle )
			{
				dataSetParameter = new OdaDataSetParameter( );
				( (OdaDataSetParameter) dataSetParameter ).setParamName( ( (OdaDataSetParameterHandle) parameter ).getParamName( ) );
			}
			else
			{
				dataSetParameter = new DataSetParameter( );
			}
			dataSetParameter.setDataType( parameter.getDataType( ) );
			dataSetParameter.setAllowNull( parameter.allowNull( ) );
			dataSetParameter.setDefaultValue( parameter.getDefaultValue( ) );
			dataSetParameter.setIsInput( parameter.isInput( ) );
			dataSetParameter.setIsOutput( parameter.isOutput( ) );
			dataSetParameter.setName( dataSetName
					+ separator + parameter.getName( ) );
			dataSetParameter.setIsOptional( parameter.isOptional( ) );
			dataSetParameter.setPosition( position );
			return dataSetParameter;
		}
		
	}

	private class ParameterViewLableProvider implements ITableLabelProvider
	{
		private DataSetHandle dataSetHandle;

		public ParameterViewLableProvider( DataSetHandle dataSetHandle )
		{
			this.dataSetHandle = dataSetHandle;
		}

		/*
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
		 *      int)
		 */
		public Image getColumnImage( Object element, int columnIndex )
		{
			return null;
		}

		/*
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
		 *      int)
		 */
		public String getColumnText( Object element, int columnIndex )
		{
			String value = null;			
			DataSetParameter parameter = getStructure( element );

			if ( ParameterPageUtil.isOdaDataSetHandle( dataSetHandle ) )
			{
				value = getOdaParametersValue( (OdaDataSetParameter) parameter,
						columnIndex );
			}
			else if ( ParameterPageUtil.isJointDataSetHandle( dataSetHandle ) )
			{
				value = getJointDataSetParametersValue( parameter, columnIndex );
			}
			else
			{
				value = getParametersValue( parameter, columnIndex );
			}

			return value == null ? "" : value;//$NON-NLS-1$
		}

		/*
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
		 */
		public void addListener( ILabelProviderListener listener )
		{
		}

		/*
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
		 */
		public void dispose( )
		{
		}

		/*
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
		 */
		public boolean isLabelProperty( Object element, String property )
		{
			return false;
		}

		/*
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
		 */
		public void removeListener( ILabelProviderListener listener )
		{
		}
		
		/**
		 * Gets the joint data set parameter's information
		 * 
		 * @param parameter
		 * @param columnIndex
		 * @return
		 */
		private String getJointDataSetParametersValue(
				DataSetParameter parameter, int columnIndex )
		{
			if( columnIndex == 4 )
			{
				if ( parameter instanceof OdaDataSetParameter )
				{
					String linkedParamName = ( (OdaDataSetParameter) parameter ).getParamName( );
					if ( linkedParamName != null
							&& linkedParamName.trim( ).length( ) > 0 )
						return NONE_DEFAULT_VALUE;
				}
				return parameter.getDefaultValue( );
			}
			if ( columnIndex == 5 )
			{
			    String value = null;
				if ( parameter instanceof OdaDataSetParameter )
				{
					value = ( (OdaDataSetParameter) parameter ).getParamName( );
				}
				return value == null || value.trim( ).length( ) == 0
						? UNLINKED_REPORT_PARAM : value;
			}
			return getParametersValue( parameter, columnIndex );
		}

		/**
		 * 
		 * @param parameter
		 * @param columnIndex
		 * @return
		 */
		private String getParametersValue( DataSetParameter parameter,
				int columnIndex )
		{
			String value = null;
			switch ( columnIndex )
			{
				case 0 :
				{
					if ( parameter.getPosition( ) != null )
					{
						value = parameter.getPosition( ).toString( );
					}
					break;
				}
				case 1 :
				{
					value = parameter.getName( );
					break;
				}
				case 2 :
				{
					value = ParameterPageUtil.getTypeDisplayName( parameter.getParameterDataType( ) );
					break;
				}
				case 3 :
				{
					value = getDirectionDisplayName( parameter.isInput( ),
							parameter.isOutput( ) );
					break;
				}
				case 4 :
				{
					value = parameter.getDefaultValue( );
					break;
				}
			}
			return value;
		}

		/**
		 * 
		 * @param parameter
		 * @param columnIndex
		 * @return
		 */
		private String getOdaParametersValue( OdaDataSetParameter parameter,
				int columnIndex )
		{
			String value = null;
			switch ( columnIndex )
			{
				case 0 :
				{
					if ( parameter.getPosition( ) != null && parameter.getPosition( ).intValue( )>0 )
					{
						value = parameter.getPosition( ).toString( );
					}
					else
					{
						value =""; //$NON-NLS-1$
					}
					break;
				}
				case 1 :
				{
					value = parameter.getName( );
					break;
				}
				case 2 :
				{
					value = parameter.getNativeName( );
					break;
				}
				case 3 :
				{
					if ( parameter.getNativeDataType( ) != null
							&& parameter.getNativeDataType( ).intValue( ) == -10 )
					{
						value = null;
					}
					else
					{
						value = ParameterPageUtil.getTypeDisplayName( parameter.getParameterDataType( ) );
					}
					break;
				}
				case 4 :
				{
					value = getDirectionDisplayName( parameter.isInput( ),
							parameter.isOutput( ) );
					break;
				}
				case 5 :
				{
					if ( parameter.getParamName( ) == null
							|| parameter.getParamName( ).trim( ).length( ) == 0 )
						value = parameter.getDefaultValue( );
					else
						value = NONE_DEFAULT_VALUE;
					break;
				}
				case 6 :
				{
					value = parameter.getParamName( );
					if ( value == null || value.trim( ).length( ) == 0 )
						value = UNLINKED_REPORT_PARAM;
					break;
				}
			}
			return value;
		}
	}
	
	private class ParameterInputDialog extends PropertyHandleInputDialog
	{

		private DataSetParameterHandle structureHandle = null;
		private Composite defaultValueComposite = null;
		private Composite reportParamComposite = null;
		private String defaultValueString = ""; //$NON-NLS-1$
		private String directionString = ""; //$NON-NLS-1$
		private Text dataSetParamName = null, nativeParameterName = null;
		private Combo dataType = null;
		private Combo direction = null; 
		private Text defaultValue = null;
		private Combo linkToSalarParameter = null;
		private boolean inputChanged = modelChanged, isOdaDataSetHandle = false;
		
		protected ParameterInputDialog( Object structureOrHandle, boolean isOdaDataSetHandle )
		{
			super( structureOrHandle );
			this.isOdaDataSetHandle = isOdaDataSetHandle;
			structureHandle = getStructureHandle( structureOrHandle );
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.report.designer.data.ui.dataset.PropertyHandleInputDialog#createCustomControls(org.eclipse.swt.widgets.Composite)
		 */
		protected void createCustomControls( Composite parent )
		{
			int index = 0;
			String[] dialogLables;
			if ( !isOdaDataSetHandle )
			{
				dialogLables = ParameterPageUtil.dialogLabels;
				createNameCell( parent, dialogLables[index] );
				createComboCellDataType( parent, dialogLables[++index] );
				createComboCellDirection( parent, dialogLables[++index] );
				createExpressionCell( parent, dialogLables[++index] );
				directionChanged( );;
			}
			else
			{
				dialogLables = ParameterPageUtil.odaDialogLabels;
				createNameCell( parent, dialogLables[index] );
				createNativeNameCell( parent, dialogLables[++index] );
				createComboCellDataType( parent, dialogLables[++index] );
				createComboCellDirection( parent, dialogLables[++index] );
				createExpressionCell( parent, dialogLables[++index] );
				createComboCellParameter( parent, dialogLables[++index] );
				directionChanged( );;
			}
		}

		private void createNameCell( Composite parent, String lable )
		{
			ControlProvider.createLabel( parent, lable );

			dataSetParamName = ControlProvider.createText( parent,
					structureHandle.getName( ) );
			dataSetParamName.setLayoutData( ControlProvider.getGridDataWithHSpan( 2 ) );
			dataSetParamName.addModifyListener( new ModifyListener( ) {

				public void modifyText( ModifyEvent e )
				{
					validateSyntax( );
				}

			} );
		}
		
		private void createNativeNameCell(  Composite parent, String lable  )
		{
			ControlProvider.createLabel( parent, lable );

			nativeParameterName = ControlProvider.createText( parent,
					( (OdaDataSetParameterHandle) structureHandle ).getNativeName( ) );
			nativeParameterName.setLayoutData( ControlProvider.getGridDataWithHSpan( 2 ) );
			nativeParameterName.setEnabled( false );
		}

		private void createComboCellDataType( Composite parent, String label )
		{
			ControlProvider.createLabel( parent, label );

			dataType = ControlProvider.createCombo( parent, SWT.READ_ONLY );
			dataType.setLayoutData( ControlProvider.getGridDataWithHSpan( 2 ) );
			//is ref cursor??
			if ( structureHandle.getNativeDataType( ) != null
					&& structureHandle.getNativeDataType( ) == -10 )
			{
				dataType.setEnabled( false );
			}
			else
			{
				dataType.setItems( ParameterPageUtil.getDataTypeDisplayNames( ) );
				dataType.select( Utility.findIndex( dataType.getItems( ),
						ParameterPageUtil.getTypeDisplayName( structureHandle.getParameterDataType( ) ) ) );
				dataType.addSelectionListener( new SelectionAdapter( ) {

					public void widgetSelected( SelectionEvent e )
					{
						validateSyntax( );
					}

				} );
			}
		}

		private void createComboCellDirection( Composite parent, String label )
		{
			ControlProvider.createLabel( parent, label );

			direction = ControlProvider.createCombo( parent, SWT.READ_ONLY );
			direction.setLayoutData( ControlProvider.getGridDataWithHSpan( 2 ) );
			direction.setItems( getDirections( ) );
			directionString = getDirectionDisplayName( structureHandle.isInput( ),
					structureHandle.isOutput( ) );
			direction.select( Utility.findIndex( direction.getItems( ),
					directionString ) );
			direction.addSelectionListener( new SelectionAdapter( ) {

				public void widgetSelected( SelectionEvent e )
				{
					if ( needsUpdateUI( ) )
					{
						directionString = direction.getText( );
						directionChanged( );
						validateSyntax( );
					}
				}

			} );
		}
		
		// handle defaultValue issue happened consequently
		private boolean needsUpdateUI( )
		{
			if ( directionString.equals( direction.getText( ) ) )
				return false;
			if ( !directionString.equals( ParameterPageUtil.directions[1] )
					&& !isOutputParameter( ) )
				return false;

			return true;
		}

		private void createExpressionCell( Composite parent, String label )
		{
			ControlProvider.createLabel( parent, label );

			defaultValueComposite = ControlProvider.getDefaultComposite( parent );
			defaultValueString = Utility.getNonNullString( structureHandle.getDefaultValue( ) );
			defaultValue = ControlProvider.createText( defaultValueComposite,
					defaultValueString );
			defaultValue.setLayoutData( ControlProvider.getGridDataWithHSpan( 1 ) );
			defaultValue.addModifyListener( new ModifyListener( ) {

				public void modifyText( ModifyEvent e )
				{
					if ( defaultValue.isEnabled( ) )
					{
						validateSyntax( );
					}
				}

			} );

			SelectionAdapter listener = new SelectionAdapter( ) {

				public void widgetSelected( SelectionEvent event )
				{
					ExpressionBuilder expressionBuilder = new ExpressionBuilder( defaultValue.getText( ) );
					expressionBuilder.setExpressionProvier( null );

					if ( expressionBuilder.open( ) == OK )
					{
						defaultValue.setText( expressionBuilder.getResult( )
								.trim( ) );
					}
				}
			};

			ControlProvider.createButton( defaultValueComposite,
					SWT.PUSH,
					listener );
		}

		private void createComboCellParameter( Composite parent, String label )
		{
			ControlProvider.createLabel( parent, label );

			reportParamComposite = ControlProvider.getDefaultComposite( parent );
			linkToSalarParameter = new Combo( reportParamComposite, SWT.READ_ONLY );
			linkToSalarParameter.setLayoutData( ControlProvider.getGridDataWithHSpan( 1 ) );
			linkToSalarParameter.setItems( ParameterPageUtil.getLinkedReportParameterNames( (OdaDataSetParameterHandle) structureHandle ) );
			linkToSalarParameter.select( Utility.findIndex( linkToSalarParameter.getItems( ),
					( (OdaDataSetParameterHandle) structureHandle ).getParamName( ) ) );
			linkToSalarParameter.addModifyListener( new ModifyListener( ) {

				public void modifyText( ModifyEvent e )
				{
					linkToSalarParameterChanged( );
					if ( linkToSalarParameter.isEnabled( ) )
					{
						validateSyntax( );
					}
				}

			} );

			SelectionAdapter listener = new SelectionAdapter( ) {

				public void widgetSelected( SelectionEvent event )
				{
					updateStructureHandle( );
					OdaDataSetParameterHandle dataSetParameterHandle = (OdaDataSetParameterHandle) structureHandle;
					String originalParamName = dataSetParameterHandle.getParamName( );
					ParameterDialog dialog = null;
					ParameterHandle handle = ParameterPageUtil.getScalarParameter( linkToSalarParameter.getText( ),
							false );
					boolean isCreateMode = true;
					if ( handle == null )
					{
						handle = (ScalarParameterHandle) ElementProcessorFactory.createProcessor( "ScalarParameter" )//$NON-NLS-1$
								.createElement( null );
						dialog = new ParameterDialog( ParameterInputDialog.this.getParentShell( ),
								Messages.getString( "ParameterGroupNodeProvider.Dialogue.ParameterNew" ), false ); //$NON-NLS-1$
						if ( dataSetParameterHandle != null )
						{
							ReportParameterAdapter adapter = new ReportParameterAdapter( );
							try
							{
								adapter.updateLinkedReportParameter( (ScalarParameterHandle) handle,
										dataSetParameterHandle,
										( (DataSetEditor) getContainer( ) ).getCurrentDataSetDesign( ) );
							}
							catch ( SemanticException e )
							{
							}
							catch ( OdaException e )
							{
							}
						}
						isCreateMode = true;
					}
					else
					{
						dialog = new ParameterDialog( ParameterInputDialog.this.getParentShell( ),
								Messages.getString( "ParameterNodeProvider.dial.title.editScalar" ), false ); //$NON-NLS-1$
						isCreateMode = false;
					}
					handle.addListener( new ScalarParameterListener( ) );
					dialog.setInput( handle );
					if ( dialog.open( ) == OK )
					{
						if ( dialog.getResult( ) instanceof ParameterHandle )
						{
							ParameterHandle paramerHandle = (ParameterHandle) dialog.getResult( );
							if ( isCreateMode )
							{
								SlotHandle parameterSlotHandle = Utility.getReportModuleHandle( )
										.getParameters( );
								try
								{
									parameterSlotHandle.add( paramerHandle );
									linkToSalarParameter.add( paramerHandle.getQualifiedName( ) );
								}
								catch ( ContentException e )
								{
									ExceptionHandler.handle( e );
								}
								catch ( NameException e )
								{
									ExceptionHandler.handle( e );
								}
							}
							linkToSalarParameter.setItems( ParameterPageUtil.getLinkedReportParameterNames( (OdaDataSetParameterHandle) structureHandle ) );
							linkToSalarParameter.select( Utility.findIndex( linkToSalarParameter.getItems( ),
									paramerHandle.getQualifiedName( ) ) );
						}
					}
					else
					{
						dataSetParameterHandle.setParamName( originalParamName );
					}
				}
			};

			ControlProvider.createButton( reportParamComposite,
					SWT.PUSH,
					listener );
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.report.designer.data.ui.dataset.PropertyHandleInputDialog#rollback()
		 */
		protected void rollback( )
		{
			DataSetParameter structure = getStructure( getStructureOrHandle( ) );
			if ( originalStructure != null )
			{
				structure.setName( originalStructure.getName( ) );
				structure.setParameterDataType( originalStructure.getParameterDataType( ) );
				structure.setIsInput( originalStructure.isInput( ) );
				structure.setIsOutput( originalStructure.isOutput( ) );
				structure.setDefaultValue( originalStructure.getDefaultValue( ) );

				if ( isOdaDataSetHandle )
					( (OdaDataSetParameter) structure ).setParamName( ( (OdaDataSetParameter) originalStructure ).getParamName( ) );

				originalStructure = null;
			}
			else
			{
				try
				{
					parameters.removeItem( structure );
					viewer.getViewer( ).refresh( );
				}
				catch ( PropertyValueException e )
				{
					ExceptionHandler.handle( e );
				}
			}
			//rollback the model changed status
			modelChanged = inputChanged; 
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.report.designer.data.ui.dataset.PropertyHandleInputDialog#validateSemantics(java.lang.Object)
		 */
		protected IStatus validateSemantics( Object structureOrHandle )
		{
			Status status = updateStructureHandle( );
			if ( status != null && status.getSeverity( ) == IStatus.ERROR )
				return status;

			return getOKStatus( );
		}
		

		/*
		 * necessary in two scenarios 1. linkToSalarParameter 2. okPressed
		 */
		private Status updateStructureHandle( )
		{
			try
			{
				structureHandle.setName( dataSetParamName.getText( ) );
				structureHandle.setParameterDataType( ParameterPageUtil.getTypeName( dataType.getText( ) ) );
				setDirection( direction.getText( ) );
				structureHandle.setDefaultValue( defaultValue.isEnabled( )
						? defaultValue.getText( ).trim( ) : defaultValueString );

				if ( isOdaDataSetHandle )
					( (OdaDataSetParameterHandle) structureHandle ).setParamName( Utility.findIndex( linkToSalarParameter.getItems( ),
							linkToSalarParameter.getText( ) ) == 0 ? null
							: linkToSalarParameter.getText( ) );
			}
			catch ( SemanticException e )
			{
				return getMiscStatus( IStatus.ERROR,
						Utility.getNonNullString( e.getMessage( ) ) );
			}

			return null;
		}
		
		private void setDirection( String direction )
		{
			if ( direction == null || direction.equals( "" ) ) //$NON-NLS-1$
				return;

			if ( direction.equals( ParameterPageUtil.directions[0] ) )
			{
				structureHandle.setIsInput( true );
				structureHandle.setIsOutput( false );
			}
			else if ( direction.equals( ParameterPageUtil.directions[1] ) )
			{
				structureHandle.setIsInput( false );
				structureHandle.setIsOutput( true );
			}
			else if ( direction.equals( ParameterPageUtil.directions[2] ) )
			{
				structureHandle.setIsInput( true );
				structureHandle.setIsOutput( true );
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.report.designer.data.ui.dataset.PropertyHandleInputDialog#validateSyntax(java.lang.Object)
		 */
		protected IStatus validateSyntax( Object structureOrHandle )
		{
			// duplicated paramName check
			if ( !isUniqueName( ) )
				return getMiscStatus( IStatus.ERROR,
						Messages.getString( "dataset.editor.error.duplicateParameterName" ) );//$NON-NLS-1$ 

			// blankProperty check
			if ( isBlankProperty( dataSetParamName.getText( ) ) )
				return getBlankPropertyStatus( ParameterPageUtil.dialogLabels[0] );
			
			// LinkedTo report parameter data type check
			if ( checkParamDataType( ) )
			{
				return getMiscStatus( IStatus.WARNING,
						Messages.getString( "DataSetParameterPage.warning.UnmatchedParamDataType" ) );//$NON-NLS-1$ 
			}

			return getOKStatus( );
		}

		/**
		 * Indicates whether the linked report parameter data type should be checked 
		 * 
		 * @return
		 */
		private boolean checkParamDataType( )
		{
			return linkToSalarParameter != null
					&& linkToSalarParameter.isEnabled( )
					&& linkToSalarParameter.getSelectionIndex( ) > 0
					&& !isMatchedParamDataType( );
		}
		
		/**
		 * Checks whether the linked report parameter's data type matches the current data set parameter's data type.
		 * 
		 * @return
		 */
		private boolean isMatchedParamDataType( )
		{
			String dataSetParamType = ParameterPageUtil.getTypeName( dataType.getText( ) );
			ScalarParameterHandle scalarParam = ParameterPageUtil.getScalarParameter( linkToSalarParameter.getText( ),
					false );
			if ( dataSetParamType != null && scalarParam != null )
			{
				return dataSetParamType.equalsIgnoreCase( scalarParam.getDataType( ) );
			}
			return true;
		}
		
		private boolean isUniqueName( )
		{
			DataSetParameter structure = getStructure( getStructureOrHandle( ) );
			Iterator iter = parameters.iterator( );
			if ( iter != null )
			{
				while ( iter.hasNext( ) )
				{
					DataSetParameterHandle handle = (DataSetParameterHandle) iter.next( );
					if ( structure != handle.getStructure( )
							&& handle.getName( )
									.equals( dataSetParamName.getText( ) ) )
					{
						return false;
					}
				}
			}

			return true;
		}
		
		private void directionChanged( )
		{
			if ( isOutputParameter( ) )
			{
				enableComposite( defaultValueComposite, false );
				defaultValue.setText( "" ); //$NON-NLS-1$
				defaultValueString = ""; //$NON-NLS-1$

				if ( isOdaDataSetHandle )
				{
					enableComposite( reportParamComposite, false );
					linkToSalarParameter.select( 0 );
				}
			}
			else
			{
				if ( isOdaDataSetHandle )
				{
					enableComposite( reportParamComposite, true );
					linkToSalarParameterChanged( );
				}
				else
				{
					enableComposite( defaultValueComposite, true );
				}
			}
		}
		
		private void linkToSalarParameterChanged( )
		{
			String paramName = Utility.findIndex( linkToSalarParameter.getItems( ),
					linkToSalarParameter.getText( ) ) == 0 ? null
					: linkToSalarParameter.getText( );

			if ( paramName == null )
			{
				enableComposite( defaultValueComposite,
						linkToSalarParameter.isEnabled( ) );
				defaultValue.setText( defaultValueString );
			}
			else
			{
				// in case users select report params more than once
				if ( defaultValue.isEnabled( ) )
					defaultValueString = defaultValue.getText( );

				structureHandle.setDefaultValue( "" ); //$NON-NLS-1$
				enableComposite( defaultValueComposite, false );
				defaultValue.setText( NONE_DEFAULT_VALUE );
			}
		}

		private boolean isOutputParameter( )
		{
			return direction.getText( ).equals( ParameterPageUtil.directions[1] );
		}

		private void enableComposite( Composite composite, boolean enable )
		{
			if ( composite.isEnabled( ) != enable )
			{
				composite.setEnabled( enable );
				Control[] controls = composite.getChildren( );
				for ( int i = 0; i < controls.length; i++ )
				{
					controls[i].setEnabled( enable );
				}
			}
		}
		
		private DataSetParameterHandle getStructureHandle( Object structureOrHandle )
		{
			if ( structureOrHandle instanceof DataSetParameterHandle )
			{
				return (DataSetParameterHandle) structureOrHandle;
			}

			else
			{
				DataSetParameterHandle handle = null;
				try
				{
					handle = (DataSetParameterHandle) parameters.addItem( (DataSetParameter) structureOrHandle );
				}
				catch ( SemanticException e )
				{
					ExceptionHandler.handle( e );
				}
				assert handle != null;
				return handle;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.report.designer.data.ui.dataset.PropertyHandleInputDialog#getTitle()
		 */
		protected String getTitle( )
		{
			return getStructureOrHandle( ) instanceof Structure
					? Messages.getString( "DataSetParameterBindingInputDialog.Title.NewParameter" )
					: Messages.getString( "DataSetParameterBindingInputDialog.Title.EditParameter" );
		}

	}
	
}
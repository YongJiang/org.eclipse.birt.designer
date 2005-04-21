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

package org.eclipse.birt.report.designer.internal.ui.dialogs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.PatternSyntaxException;

import org.eclipse.birt.data.engine.api.IPreparedQuery;
import org.eclipse.birt.data.engine.api.IQueryDefinition;
import org.eclipse.birt.data.engine.api.IQueryResults;
import org.eclipse.birt.data.engine.api.IResultIterator;
import org.eclipse.birt.data.engine.api.querydefn.BaseQueryDefinition;
import org.eclipse.birt.data.engine.api.querydefn.BaseTransform;
import org.eclipse.birt.data.engine.api.querydefn.ScriptExpression;
import org.eclipse.birt.report.designer.core.model.SessionHandleAdapter;
import org.eclipse.birt.report.designer.core.model.views.data.DataSetItemModel;
import org.eclipse.birt.report.designer.internal.ui.util.DataSetManager;
import org.eclipse.birt.report.designer.internal.ui.util.ExceptionHandler;
import org.eclipse.birt.report.designer.internal.ui.util.UIUtil;
import org.eclipse.birt.report.designer.ui.views.attributes.providers.ChoiceSetFactory;
import org.eclipse.birt.report.designer.util.DEUtil;
import org.eclipse.birt.report.model.api.DataSetHandle;
import org.eclipse.birt.report.model.api.util.StringUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

/**
 * The dialog used to import values from data sets
 */

public class ImportValueDialog extends BaseDialog
{

	private static final String DLG_TITLE = "Import Values";

	private static final String LABEL_SELECT_DATASET = "Select Data Set:";
	private static final String LABEL_SELECT_COLUMN = "Select Column:";
	private static final String LABEL_SELECT_VALUE = "Select or enter value to add";

	private Combo dataSetChooser, columnChooser;
	private Text valueEditor;
	private List valueList, selectedList;
	private Button add, addAll, remove, removeAll;

	private String currentDataSetName;
	private ArrayList resultList = new ArrayList( );;

	private DataSetItemModel[] columns;
	private int selectedColumnIndex;

	/**
	 * Constructs a new instance of the dialog
	 */
	public ImportValueDialog( )
	{
		super( DLG_TITLE );
	}

	protected Control createDialogArea( Composite parent )
	{
		Composite composite = (Composite) super.createDialogArea( parent );
		createColumnSelectionArea( composite );
		createValueSelectionArea( composite );
		return composite;
	}

	private void createColumnSelectionArea( Composite parent )
	{
		Composite composite = new Composite( parent, SWT.NONE );
		composite.setLayout( new GridLayout( 2, true ) );
		composite.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );

		Composite selectionArea = new Composite( composite, SWT.NONE );
		selectionArea.setLayout( UIUtil.createGridLayoutWithoutMargin( 2, false ) );
		selectionArea.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );

		new Label( composite, SWT.NONE );//Dummy

		new Label( selectionArea, SWT.NONE ).setText( LABEL_SELECT_DATASET );
		dataSetChooser = new Combo( selectionArea, SWT.DROP_DOWN
				| SWT.READ_ONLY );
		dataSetChooser.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
		dataSetChooser.addSelectionListener( new SelectionAdapter( ) {

			public void widgetSelected( SelectionEvent e )
			{
				String newSelection = dataSetChooser.getText( );
				if ( !currentDataSetName.equals( newSelection ) )
				{
					currentDataSetName = newSelection;
					refreshColumns( );
				}
			}

		} );

		new Label( selectionArea, SWT.NONE ).setText( LABEL_SELECT_COLUMN );
		columnChooser = new Combo( selectionArea, SWT.DROP_DOWN | SWT.READ_ONLY );
		columnChooser.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
		columnChooser.addSelectionListener( new SelectionAdapter( ) {

			public void widgetSelected( SelectionEvent e )
			{
				int newSelectedIndex = columnChooser.getSelectionIndex( );
				if ( selectedColumnIndex != newSelectedIndex )
				{
					selectedColumnIndex = newSelectedIndex;
					refreshValues( );
				}
			}

		} );

	}

	private void createValueSelectionArea( Composite parent )
	{
		Composite selectionArea = new Composite( parent, SWT.NONE );
		selectionArea.setLayout( new GridLayout( 3, false ) );
		selectionArea.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );

		Composite subComposite = new Composite( selectionArea, SWT.NONE );
		subComposite.setLayout( UIUtil.createGridLayoutWithoutMargin( ) );
		subComposite.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );

		new Label( subComposite, SWT.NONE ).setText( LABEL_SELECT_VALUE );
		valueEditor = new Text( subComposite, SWT.BORDER | SWT.SINGLE );
		valueEditor.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
		valueEditor.addModifyListener( new ModifyListener( ) {

			public void modifyText( ModifyEvent e )
			{
				filteValues( );

			}
		} );

		GridData gd = new GridData( );
		gd.horizontalSpan = 2;
		new Label( selectionArea, SWT.NONE ).setLayoutData( gd ); //Dummy

		valueList = new List( selectionArea, SWT.MULTI | SWT.BORDER );
		setListLayoutData( valueList );
		valueList.addSelectionListener( new SelectionListener( ) {

			public void widgetDefaultSelected( SelectionEvent e )
			{
				addSelected( );
			}

			public void widgetSelected( SelectionEvent e )
			{
				updateButtons( );
			}
		} );
		Composite buttonBar = new Composite( selectionArea, SWT.NONE );
		GridLayout layout = UIUtil.createGridLayoutWithoutMargin( );
		//layout.verticalSpacing = 10;
		buttonBar.setLayout( layout );
		//buttonBar.setLayoutData( new GridData( GridData.FILL_VERTICAL ) );

		addAll = new Button( buttonBar, SWT.PUSH );
		addAll.setText( ">>" );
		addAll.setLayoutData( new GridData( GridData.FILL_VERTICAL
				| GridData.VERTICAL_ALIGN_END
				| GridData.FILL_HORIZONTAL ) );

		addAll.addSelectionListener( new SelectionAdapter( ) {

			public void widgetSelected( SelectionEvent e )
			{
				addAll( );
			}

		} );

		add = new Button( buttonBar, SWT.PUSH );
		add.setText( ">" );
		add.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
		add.addSelectionListener( new SelectionAdapter( ) {

			public void widgetSelected( SelectionEvent e )
			{
				addSelected( );
			}

		} );

		remove = new Button( buttonBar, SWT.PUSH );
		remove.setText( "<" );
		remove.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
		remove.addSelectionListener( new SelectionAdapter( ) {

			public void widgetSelected( SelectionEvent e )
			{
				removeSelected( );
			}

		} );

		removeAll = new Button( buttonBar, SWT.PUSH );
		removeAll.setText( "<<" );
		removeAll.setLayoutData( new GridData( GridData.FILL_VERTICAL
				| GridData.VERTICAL_ALIGN_BEGINNING
				| GridData.FILL_HORIZONTAL ) );
		removeAll.addSelectionListener( new SelectionAdapter( ) {

			public void widgetSelected( SelectionEvent e )
			{
				removeAll( );
			}

		} );

		selectedList = new List( selectionArea, SWT.MULTI | SWT.BORDER );
		setListLayoutData( selectedList );
		selectedList.addSelectionListener( new SelectionListener( ) {

			public void widgetDefaultSelected( SelectionEvent e )
			{
				removeSelected( );
			}

			public void widgetSelected( SelectionEvent e )
			{
				updateButtons( );
			}
		} );
	}

	private void setListLayoutData( List list )
	{
		GridData gd = new GridData( GridData.FILL_BOTH );
		gd.heightHint = 200;
		gd.widthHint = 200;
		list.setLayoutData( gd );
	}

	private void addSelected( )
	{
		String[] selected = valueList.getSelection( );
		if ( selected.length == 0 )
		{
			selected = new String[]{
				valueEditor.getText( )
			};
		}
		for ( int i = 0; i < selected.length; i++ )
		{
			if ( selectedList.indexOf( selected[i] ) == -1 )
			{
				selectedList.add( selected[i] );
			}
		}
		updateButtons( );
	}

	private void addAll( )
	{
		String[] values = valueList.getItems( );
		for ( int i = 0; i < values.length; i++ )
		{
			if ( selectedList.indexOf( values[i] ) == -1 )
			{
				selectedList.add( values[i] );
			}
		}
		updateButtons( );
	}

	private void removeSelected( )
	{
		String[] selected = selectedList.getSelection( );
		for ( int i = 0; i < selected.length; i++ )
		{
			selectedList.remove( selected[i] );
		}
		updateButtons( );
	}

	private void removeAll( )
	{
		selectedList.removeAll( );
		updateButtons( );
	}

	protected boolean initDialog( )
	{
		dataSetChooser.setItems( ChoiceSetFactory.getDataSets( ) );
		dataSetChooser.select( 0 );
		currentDataSetName = dataSetChooser.getText( );
		refreshColumns( );
		return true;
	}

	private void refreshColumns( )
	{
		columns = DataSetManager.getCurrentInstance( )
				.getColumns( currentDataSetName, false );
		columnChooser.removeAll( );
		if ( columns.length == 0 )
		{
			columnChooser.setEnabled( false );
			columnChooser.setItems( new String[0] );
			selectedColumnIndex = -1;
		}
		else
		{
			columnChooser.setEnabled( true );
			for ( int i = 0; i < columns.length; i++ )
			{
				columnChooser.add( columns[i].getDataSetColumnName( ) );
			}
			columnChooser.select( 0 );
			selectedColumnIndex = 0;
		}
		refreshValues( );
	}

	private void refreshValues( )
	{
		resultList.clear( );
		if ( columnChooser.isEnabled( ) )
		{
			try
			{
				BaseQueryDefinition query = (BaseQueryDefinition) DataSetManager.getCurrentInstance( )
						.getPreparedQuery( getDataSetHandle( ) )
						.getReportQueryDefn( );

				ScriptExpression expression = new ScriptExpression( DEUtil.getExpression( columns[columnChooser.getSelectionIndex( )] ) );

				query.addExpression( expression, BaseTransform.ON_EACH_ROW );

				IPreparedQuery preparedQuery = DataSetManager.getCurrentInstance( )
						.getEngine( )
						.prepare( (IQueryDefinition) query );
				IQueryResults results = preparedQuery.execute( null );
				if ( results != null )
				{
					IResultIterator iter = results.getResultIterator( );
					if ( iter != null )
					{
						while ( iter.next( ) )
						{
							String result = iter.getString( expression );
							if ( !StringUtil.isBlank( result )
									&& !resultList.contains( result ) )
							{
								resultList.add( result );
							}
						}
					}

					results.close( );
				}
			}
			catch ( Exception e )
			{
				ExceptionHandler.handle( e );
				valueList.removeAll( );
				valueList.deselectAll( );
				updateButtons( );
			}
			filteValues( );
		}
		else
		{
			valueList.removeAll( );
			valueList.deselectAll( );
			updateButtons( );
		}
	}

	private void filteValues( )
	{
		valueList.removeAll( );
		valueList.deselectAll( );
		for ( Iterator itor = resultList.iterator( ); itor.hasNext( ); )
		{
			String value = (String) itor.next( );
			try
			{
				if ( value.startsWith( valueEditor.getText( ) )
						|| value.matches( valueEditor.getText( ) ) )
				{
					valueList.add( value );
				}
			}
			catch ( PatternSyntaxException e )
			{
			}
		}
		updateButtons( );
	}

	private void updateButtons( )
	{
		add.setEnabled( valueList.getSelectionCount( ) != 0
				|| valueEditor.getText( ).trim( ).length( ) != 0 );
		addAll.setEnabled( valueList.getItemCount( ) != 0 );
		remove.setEnabled( selectedList.getSelectionCount( ) != 0 );
		removeAll.setEnabled( selectedList.getItemCount( ) != 0 );
		getOkButton( ).setEnabled( selectedList.getItemCount( ) != 0 );
	}

	protected void okPressed( )
	{
		setResult( selectedList.getItems( ) );
		super.okPressed( );
	}

	private DataSetHandle getDataSetHandle( )
	{
		return SessionHandleAdapter.getInstance( )
				.getReportDesignHandle( )
				.findDataSet( currentDataSetName );
	}
}

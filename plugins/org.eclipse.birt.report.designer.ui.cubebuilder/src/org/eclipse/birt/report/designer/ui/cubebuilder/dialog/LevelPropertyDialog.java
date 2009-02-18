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

package org.eclipse.birt.report.designer.ui.cubebuilder.dialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.birt.report.designer.internal.ui.util.ExceptionHandler;
import org.eclipse.birt.report.designer.internal.ui.util.IHelpContextIds;
import org.eclipse.birt.report.designer.internal.ui.util.UIUtil;
import org.eclipse.birt.report.designer.internal.ui.util.WidgetUtil;
import org.eclipse.birt.report.designer.ui.cubebuilder.nls.Messages;
import org.eclipse.birt.report.designer.ui.cubebuilder.provider.CubeExpressionProvider;
import org.eclipse.birt.report.designer.ui.cubebuilder.util.OlapUtil;
import org.eclipse.birt.report.designer.ui.dialogs.ExpressionBuilder;
import org.eclipse.birt.report.designer.ui.dialogs.ExpressionProvider;
import org.eclipse.birt.report.designer.ui.views.attributes.providers.ChoiceSetFactory;
import org.eclipse.birt.report.designer.ui.widget.ExpressionCellEditor;
import org.eclipse.birt.report.designer.util.DEUtil;
import org.eclipse.birt.report.model.api.DataSetHandle;
import org.eclipse.birt.report.model.api.LevelAttributeHandle;
import org.eclipse.birt.report.model.api.ResultSetColumnHandle;
import org.eclipse.birt.report.model.api.RuleHandle;
import org.eclipse.birt.report.model.api.StructureFactory;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.eclipse.birt.report.model.api.elements.DesignChoiceConstants;
import org.eclipse.birt.report.model.api.elements.ReportDesignConstants;
import org.eclipse.birt.report.model.api.elements.structures.LevelAttribute;
import org.eclipse.birt.report.model.api.elements.structures.Rule;
import org.eclipse.birt.report.model.api.metadata.IChoice;
import org.eclipse.birt.report.model.api.metadata.PropertyValueException;
import org.eclipse.birt.report.model.api.olap.TabularHierarchyHandle;
import org.eclipse.birt.report.model.api.olap.TabularLevelHandle;
import org.eclipse.birt.report.model.elements.interfaces.ILevelModel;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

public class LevelPropertyDialog extends TitleAreaDialog
{

	private Composite dynamicArea;
	private DataSetHandle dataset;

	private IChoice[] getAvailableDataTypeChoices( )
	{
		IChoice[] dataTypes = DEUtil.getMetaDataDictionary( )
				.getElement( ReportDesignConstants.LEVEL_ELEMENT )
				.getProperty( ILevelModel.DATA_TYPE_PROP )
				.getAllowedChoices( )
				.getChoices( );
		List choiceList = new ArrayList( );
		for ( int i = 0; i < dataTypes.length; i++ )
		{
			choiceList.add( dataTypes[i] );
		}
		return (IChoice[]) choiceList.toArray( new IChoice[0] );
	}

	public String[] getDataTypeNames( )
	{
		IChoice[] choices = getAvailableDataTypeChoices( );
		if ( choices == null )
			return new String[0];

		String[] names = new String[choices.length];
		for ( int i = 0; i < choices.length; i++ )
		{
			names[i] = choices[i].getName( );
		}
		return names;
	}

	public String getDataTypeDisplayName( String name )
	{
		return ChoiceSetFactory.getDisplayNameFromChoiceSet( name,
				DEUtil.getMetaDataDictionary( )
						.getElement( ReportDesignConstants.LEVEL_ELEMENT )
						.getProperty( ILevelModel.DATA_TYPE_PROP )
						.getAllowedChoices( ) );
	}

	private String[] getDataTypeDisplayNames( )
	{
		IChoice[] choices = getAvailableDataTypeChoices( );
		if ( choices == null )
			return new String[0];

		String[] displayNames = new String[choices.length];
		for ( int i = 0; i < choices.length; i++ )
		{
			displayNames[i] = choices[i].getDisplayName( );
		}
		return displayNames;
	}

	private boolean isNew;

	public LevelPropertyDialog( boolean isNew )
	{
		super( UIUtil.getDefaultShell( ) );
		setShellStyle( getShellStyle( ) | SWT.RESIZE | SWT.MAX );
		this.isNew = isNew;
	}

	protected Control createDialogArea( Composite parent )
	{
		UIUtil.bindHelp( parent, IHelpContextIds.LEVEL_PROPERTY_DIALOG );
		getShell( ).setText( Messages.getString( "LevelPropertyDialog.Shell.Title" ) ); //$NON-NLS-1$
		if ( isNew )
			this.setTitle( Messages.getString( "LevelPropertyDialog.Title.Add" ) ); //$NON-NLS-1$
		else
			this.setTitle( Messages.getString( "LevelPropertyDialog.Title.Edit" ) ); //$NON-NLS-1$
		this.setMessage( Messages.getString( "LevelPropertyDialog.Message" ) ); //$NON-NLS-1$

		Composite area = (Composite) super.createDialogArea( parent );

		Composite contents = new Composite( area, SWT.NONE );
		GridLayout layout = new GridLayout( );
		layout.marginWidth = 20;
		contents.setLayout( layout );
		GridData data = new GridData( GridData.FILL_BOTH );
		data.widthHint = convertWidthInCharsToPixels( 80 );
		data.heightHint = 400;
		contents.setLayoutData( data );

		createChoiceArea( contents );

		dynamicArea = createDynamicArea( contents );
		staticArea = createStaticArea( contents );

		WidgetUtil.createGridPlaceholder( contents, 1, true );

		initLevelDialog( );

		parent.layout( );

		return contents;
	}

	private void initLevelDialog( )
	{
		if ( input != null )
		{
			expressionEditor.setExpressionProvider( new CubeExpressionProvider( input ) );
			if ( input.getLevelType( ) == null )
			{
				try
				{
					input.setLevelType( DesignChoiceConstants.LEVEL_TYPE_DYNAMIC );
				}
				catch ( SemanticException e )
				{
					ExceptionHandler.handle( e );
				}
			}

			refreshDynamicViewer( );
			dataset = OlapUtil.getHierarchyDataset( (TabularHierarchyHandle) input.getContainer( ) );
			if ( dataset != null )
				attributeItems = OlapUtil.getDataFieldNames( dataset );
			resetEditorItems( );

			if ( input.getName( ) != null )
				nameText.setText( input.getName( ) );
			dynamicDataTypeCombo.setItems( getDataTypeDisplayNames( ) );
			dynamicDataTypeCombo.setText( getDataTypeDisplayName( input.getDataType( ) ) );

			fieldCombo.setItems( OlapUtil.getDataFieldNames( dataset ) );
			if ( input.getColumnName( ) != null )
				fieldCombo.setText( input.getColumnName( ) );
			else
				fieldCombo.select( 0 );

			displayKeyCombo.setItems( OlapUtil.getDataFieldNames( dataset ) );
			displayKeyCombo.add( Messages.getString( "LevelPropertyDialog.None" ), 0 ); //$NON-NLS-1$

			if ( input.getDisplayColumnName( ) != null )
			{
				displayKeyCombo.setText( input.getDisplayColumnName( ) );
			}
			else if ( displayKeyCombo.getItemCount( ) > 0 )
				displayKeyCombo.select( 0 );

			staticDataTypeCombo.setItems( getDataTypeDisplayNames( ) );
			staticNameText.setText( input.getName( ) );
			staticDataTypeCombo.setText( getDataTypeDisplayName( input.getDataType( ) ) );
			refreshStaticViewer( );
			// dynamicViewer.setInput( dynamicAttributes );

			if ( input.getLevelType( )
					.equals( DesignChoiceConstants.LEVEL_TYPE_DYNAMIC ) )
			{
				dynamicButton.setSelection( true );
				updateButtonStatus( dynamicButton );
			}
			else
			{
				staticButton.setSelection( true );
				updateButtonStatus( staticButton );
			}
		}
	}

	private void refreshDynamicViewer( )
	{
		Iterator attrIter = input.attributesIterator( );
		final List attrList = new LinkedList( );
		while ( attrIter.hasNext( ) )
		{
			attrList.add( attrIter.next( ) );
		}
		Display.getDefault( ).asyncExec( new Runnable( ) {

			public void run( )
			{
				dynamicViewer.setInput( attrList );
			}

		} );

	}

	private void refreshStaticViewer( )
	{
		Iterator valuesIter = input.staticValuesIterator( );
		final List valuesList = new LinkedList( );
		while ( valuesIter.hasNext( ) )
		{
			valuesList.add( valuesIter.next( ) );
		}
		Display.getDefault( ).asyncExec( new Runnable( ) {

			public void run( )
			{
				staticViewer.setInput( valuesList );
				defaultValueViewer.setInput( input );
			}

		} );

	}

	protected void okPressed( )
	{
		if ( dynamicButton.getSelection( ) )
		{
			try
			{
				input.setLevelType( DesignChoiceConstants.LEVEL_TYPE_DYNAMIC );
				if ( nameText.getText( ) != null
						&& !nameText.getText( ).trim( ).equals( "" ) ) //$NON-NLS-1$
				{
					input.setName( nameText.getText( ) );
				}
				if ( fieldCombo.getText( ) != null )
				{
					input.setColumnName( fieldCombo.getText( ) );
				}
				if ( displayKeyCombo.getText( ).trim( ).length( ) > 0
						&& !displayKeyCombo.getText( )
								.equals( Messages.getString( "LevelPropertyDialog.None" ) ) ) //$NON-NLS-1$
				{
					input.setDisplayColumnName( displayKeyCombo.getText( ) );
				}
				else
					input.setDisplayColumnName( null );
				if ( dynamicDataTypeCombo.getText( ) != null )
				{
					input.setDataType( getDataTypeNames( )[dynamicDataTypeCombo.getSelectionIndex( )] );
				}
				input.getPropertyHandle( ILevelModel.STATIC_VALUES_PROP )
						.clearValue( );
			}
			catch ( SemanticException e )
			{
				ExceptionHandler.handle( e );
				return;
			}

		}
		else if ( staticButton.getSelection( ) )
		{
			try
			{
				input.setLevelType( DesignChoiceConstants.LEVEL_TYPE_MIRRORED );
				if ( staticNameText.getText( ) != null
						&& !staticNameText.getText( ).trim( ).equals( "" ) ) //$NON-NLS-1$
				{
					input.setName( staticNameText.getText( ) );
				}
				if ( staticDataTypeCombo.getText( ) != null )
				{
					input.setDataType( getDataTypeNames( )[staticDataTypeCombo.getSelectionIndex( )] );
				}
				input.getPropertyHandle( ILevelModel.ATTRIBUTES_PROP )
						.clearValue( );
			}
			catch ( SemanticException e )
			{
				ExceptionHandler.handle( e );
				return;
			}

		}
		super.okPressed( );
	}
	private static final String dummyChoice = "dummy"; //$NON-NLS-1$

	private IStructuredContentProvider contentProvider = new IStructuredContentProvider( ) {

		public void dispose( )
		{
		}

		public void inputChanged( Viewer viewer, Object oldInput,
				Object newInput )
		{
		}

		public Object[] getElements( Object input )
		{
			if ( input instanceof List )
			{
				List list = (List) input;
				if ( !list.contains( dummyChoice ) )
					list.add( dummyChoice );
				return list.toArray( );
			}
			return new Object[0];
		}
	};

	private IStructuredContentProvider defaultValueContentProvider = new IStructuredContentProvider( ) {

		public void dispose( )
		{
		}

		public void inputChanged( Viewer viewer, Object oldInput,
				Object newInput )
		{
		}

		public Object[] getElements( Object input )
		{
			if ( input instanceof TabularLevelHandle )
			{
				List list = new ArrayList( );
				if ( ( (TabularLevelHandle) input ).getDefaultValue( ) != null )
					list.add( ( (TabularLevelHandle) input ).getDefaultValue( ) );
				else
					list.add( dummyChoice );
				return list.toArray( );
			}
			return new Object[0];
		}
	};

	private ITableLabelProvider staticLabelProvider = new ITableLabelProvider( ) {

		public Image getColumnImage( Object element, int columnIndex )
		{
			return null;
		}

		public String getColumnText( Object element, int columnIndex )
		{
			if ( columnIndex == 1 )
			{
				if ( element == dummyChoice )
					return Messages.getString( "LevelPropertyDialog.MSG.Static.CreateNew" ); //$NON-NLS-1$
				else
				{
					if ( element instanceof RuleHandle )
					{

						return ( (RuleHandle) element ).getDisplayExpression( );

					}
					return ""; //$NON-NLS-1$
				}
			}
			else if ( columnIndex == 2 )
			{
				if ( element == dummyChoice )
					return ""; //$NON-NLS-1$
				else
				{
					if ( element instanceof RuleHandle )
					{

						return ( (RuleHandle) element ).getRuleExpression( );

					}
					return ""; //$NON-NLS-1$
				}
			}
			else
				return ""; //$NON-NLS-1$
		}

		public void addListener( ILabelProviderListener listener )
		{
		}

		public void dispose( )
		{
		}

		public boolean isLabelProperty( Object element, String property )
		{
			return false;
		}

		public void removeListener( ILabelProviderListener listener )
		{
		}

	};

	private ITableLabelProvider defaultValueLabelProvider = new ITableLabelProvider( ) {

		public Image getColumnImage( Object element, int columnIndex )
		{
			return null;
		}

		public String getColumnText( Object element, int columnIndex )
		{
			if ( columnIndex == 1 )
			{
				if ( element == dummyChoice )
					return Messages.getString( "LevelPropertyDialog.MSG.DefaultValue" ); //$NON-NLS-1$
				else
				{
					if ( element instanceof String )
					{

						return (String) element;

					}
					return ""; //$NON-NLS-1$
				}
			}
			else if ( columnIndex == 2 )
			{
				return Messages.getString( "LevelPropertyDialog.MSG.Tooltip" ); //$NON-NLS-1$
			}
			else
				return ""; //$NON-NLS-1$
		}

		public void addListener( ILabelProviderListener listener )
		{
		}

		public void dispose( )
		{
		}

		public boolean isLabelProperty( Object element, String property )
		{
			return false;
		}

		public void removeListener( ILabelProviderListener listener )
		{
		}

	};

	private ITableLabelProvider dynamicLabelProvider = new ITableLabelProvider( ) {

		public Image getColumnImage( Object element, int columnIndex )
		{
			return null;
		}

		public String getColumnText( Object element, int columnIndex )
		{
			if ( columnIndex == 1 )
			{
				if ( element == dummyChoice )
					return Messages.getString( "LevelPropertyDialog.MSG.Dynamic.CreateNew" ); //$NON-NLS-1$
				else
				{
					if ( element instanceof LevelAttributeHandle )
					{

						return ( (LevelAttributeHandle) element ).getName( );

					}
				}
			}
			return ""; //$NON-NLS-1$
		}

		public void addListener( ILabelProviderListener listener )
		{
		}

		public void dispose( )
		{
		}

		public boolean isLabelProperty( Object element, String property )
		{
			return false;
		}

		public void removeListener( ILabelProviderListener listener )
		{
		}

	};

	private ICellModifier dynamicCellModifier = new ICellModifier( ) {

		public boolean canModify( Object element, String property )
		{
			return true;
		}

		public Object getValue( Object element, String property )
		{
			if ( element instanceof LevelAttributeHandle )
			{
				LevelAttributeHandle handle = (LevelAttributeHandle) element;
				resetEditorItems( handle.getName( ) );
				for ( int i = 0; i < editor.getItems( ).length; i++ )
					if ( handle.getName( ).equals( editor.getItems( )[i] ) )
						return Integer.valueOf( i );
			}
			if ( element instanceof String )
			{
				resetEditorItems( );
			}
			return Integer.valueOf( -1 );
		}

		public void modify( Object element, String property, Object value )
		{
			if ( element instanceof Item )
				element = ( (Item) element ).getData( );

			if ( ( (Integer) value ).intValue( ) > -1
					&& ( (Integer) value ).intValue( ) < editor.getItems( ).length )
			{
				if ( element instanceof LevelAttributeHandle )
				{
					LevelAttributeHandle handle = (LevelAttributeHandle) element;
					try
					{
						handle.setName( editor.getItems( )[( (Integer) value ).intValue( )] );
						if ( dataset != null )
						{
							ResultSetColumnHandle dataField = OlapUtil.getDataField( dataset,
									handle.getName( ) );
							handle.setDataType( dataField.getDataType( ) );
						}
					}
					catch ( SemanticException e )
					{
						ExceptionHandler.handle( e );
					}
				}
				else
				{
					LevelAttribute attribute = StructureFactory.createLevelAttribute( );
					attribute.setName( editor.getItems( )[( (Integer) value ).intValue( )] );
					if ( dataset != null )
					{
						ResultSetColumnHandle dataField = OlapUtil.getDataField( dataset,
								attribute.getName( ) );
						attribute.setDataType( dataField.getDataType( ) );
					}
					try
					{
						input.getPropertyHandle( ILevelModel.ATTRIBUTES_PROP )
								.addItem( attribute );
					}
					catch ( SemanticException e )
					{
						ExceptionHandler.handle( e );
					}
				}
				refreshDynamicViewer( );
			}
		}
	};

	private ICellModifier staticCellModifier = new ICellModifier( ) {

		public boolean canModify( Object element, String property )
		{
			if ( property.equals( Prop_Name ) )
			{
				return true;
			}
			else
			{
				if ( element != dummyChoice )
					return true;
				else
					return false;
			}
		}

		public Object getValue( Object element, String property )
		{
			if ( property.equals( Prop_Name ) )
			{
				if ( element != dummyChoice )
				{
					if ( element instanceof RuleHandle )
						return ( (RuleHandle) element ).getDisplayExpression( );
				}
				return ""; //$NON-NLS-1$
			}
			else
			{
				if ( element != dummyChoice )
				{
					if ( element instanceof RuleHandle )
					{
						String result = ( (RuleHandle) element ).getRuleExpression( );
						return result == null ? "" : result; //$NON-NLS-1$
					}
				}
				return ""; //$NON-NLS-1$
			}
		}

		public void modify( Object element, String property, Object value )
		{
			if ( element instanceof Item )
			{
				element = ( (Item) element ).getData( );
			}
			if ( property.equals( Prop_Name )
					&& !( value.toString( ).trim( ).equals( "" ) || value.equals( dummyChoice ) ) ) //$NON-NLS-1$
			{
				if ( element instanceof RuleHandle )
				{
					( (RuleHandle) element ).setDisplayExpression( value.toString( ) );
				}
				else
				{
					Rule rule = StructureFactory.createRule( );
					rule.setProperty( Rule.DISPLAY_EXPRE_MEMBER,
							value.toString( ) );
					rule.setProperty( Rule.RULE_EXPRE_MEMBER, "" ); //$NON-NLS-1$
					try
					{
						input.getPropertyHandle( ILevelModel.STATIC_VALUES_PROP )
								.addItem( rule );
					}
					catch ( SemanticException e )
					{
						ExceptionHandler.handle( e );
					}
				}
				refreshStaticViewer( );
			}
			else if ( property.equals( prop_Expression ) )
			{
				if ( element != dummyChoice
						&& !( value.toString( ).trim( ).equals( "" ) ) ) //$NON-NLS-1$
				{
					if ( element instanceof RuleHandle )
					{
						( (RuleHandle) element ).setRuleExpression( value.toString( ) );
					}
					refreshStaticViewer( );
				}
			}

		}
	};

	private ICellModifier defaultValueCellModifier = new ICellModifier( ) {

		public boolean canModify( Object element, String property )
		{
			if ( property.equals( Prop_DefaultValue ) )
			{
				return true;
			}
			return false;
		}

		public Object getValue( Object element, String property )
		{
			if ( property.equals( Prop_DefaultValue ) )
			{
				if ( element != dummyChoice )
				{
					if ( element instanceof String )
						return (String) element;
				}

			}
			return ""; //$NON-NLS-1$
		}

		public void modify( Object element, String property, Object value )
		{
			if ( property.equals( Prop_DefaultValue ) ) //$NON-NLS-1$
			{
				try
				{
					if ( !( value.toString( ).trim( ).equals( "" ) || value.equals( dummyChoice ) ) ) //$NON-NLS-1$
						input.setDefaultValue( value.toString( ) );
					else
						input.setDefaultValue( null );
				}
				catch ( SemanticException e )
				{
					ExceptionHandler.handle( e );
				}
			}
			refreshStaticViewer( );
		}
	};

	private int dynamicSelectIndex;

	protected Composite createDynamicArea( Composite parent )
	{
		Composite contents = new Composite( parent, SWT.NONE );
		GridLayout layout = new GridLayout( );
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		contents.setLayout( layout );
		GridData data = new GridData( GridData.FILL_HORIZONTAL );
		contents.setLayoutData( data );

		Group groupGroup = new Group( contents, SWT.NONE );
		layout = new GridLayout( );
		layout.numColumns = 4;
		groupGroup.setLayout( layout );

		groupGroup.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );

		Label nameLabel = new Label( groupGroup, SWT.NONE );
		nameLabel.setText( Messages.getString( "LevelDialog.Label.Name" ) ); //$NON-NLS-1$
		nameText = new Text( groupGroup, SWT.BORDER );
		GridData gd = new GridData( GridData.FILL_HORIZONTAL );
		gd.horizontalSpan = 3;
		nameText.setLayoutData( gd );
		nameText.addModifyListener( new ModifyListener( ) {

			public void modifyText( ModifyEvent e )
			{
				checkOkButtonStatus( );
			}

		} );

		Label fieldLabel = new Label( groupGroup, SWT.NONE );
		fieldLabel.setText( Messages.getString( "LevelPropertyDialog.KeyField" ) ); //$NON-NLS-1$
		fieldCombo = new Combo( groupGroup, SWT.BORDER | SWT.READ_ONLY );
		gd = new GridData( GridData.FILL_HORIZONTAL );
		gd.horizontalSpan = 3;
		fieldCombo.setLayoutData( gd );
		fieldCombo.addSelectionListener( new SelectionAdapter( ) {

			public void widgetSelected( SelectionEvent e )
			{
				checkOkButtonStatus( );
			}

		} );

		Label displayKeyLabel = new Label( groupGroup, SWT.NONE );
		displayKeyLabel.setText( Messages.getString( "LevelPropertyDialog.DisplayField" ) ); //$NON-NLS-1$
		displayKeyCombo = new Combo( groupGroup, SWT.BORDER );
		gd = new GridData( GridData.FILL_HORIZONTAL );
		gd.horizontalSpan = 2;
		displayKeyCombo.setLayoutData( gd );
		displayKeyCombo.addSelectionListener( new SelectionAdapter( ) {

			public void widgetSelected( SelectionEvent e )
			{
				if ( displayKeyCombo.getSelectionIndex( ) > 0 )
				{
					displayKeyCombo.setText( DEUtil.getResultSetColumnExpression( displayKeyCombo.getText( ) ) );

				}
			}
		} );
		Button expressionButton = new Button( groupGroup, SWT.PUSH );
		UIUtil.setExpressionButtonImage( expressionButton );
		expressionButton.addSelectionListener( new SelectionAdapter( ) {

			public void widgetSelected( SelectionEvent event )
			{
				openExpression( );
			}

			private void openExpression( )
			{
				ExpressionBuilder expressionBuilder = new ExpressionBuilder( displayKeyCombo.getText( )
						.trim( )
						.equals( Messages.getString( "LevelPropertyDialog.None" ) ) ? "" //$NON-NLS-1$ //$NON-NLS-2$
						: displayKeyCombo.getText( ).trim( ) );
				ExpressionProvider provider = new CubeExpressionProvider( input );
				expressionBuilder.setExpressionProvier( provider );
				if ( expressionBuilder.open( ) == Window.OK )
				{
					displayKeyCombo.setText( expressionBuilder.getResult( ) );
				}
			}
		} );

		new Label( groupGroup, SWT.NONE ).setText( Messages.getString( "LevelPropertyDialog.DataType" ) ); //$NON-NLS-1$
		dynamicDataTypeCombo = new Combo( groupGroup, SWT.BORDER
				| SWT.READ_ONLY );
		dynamicDataTypeCombo.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
		dynamicDataTypeCombo.addSelectionListener( new SelectionAdapter( ) {

			public void widgetSelected( SelectionEvent e )
			{
				checkOkButtonStatus( );
			}

		} );
		gd = new GridData( GridData.FILL_HORIZONTAL );
		gd.horizontalSpan = 3;
		dynamicDataTypeCombo.setLayoutData( gd );

		dynamicTable = new Table( contents, SWT.SINGLE
				| SWT.FULL_SELECTION
				| SWT.BORDER
				| SWT.VERTICAL
				| SWT.HORIZONTAL );
		gd = new GridData( GridData.FILL_BOTH );
		gd.heightHint = 150;
		dynamicTable.setLayoutData( gd );
		dynamicTable.setLinesVisible( true );
		dynamicTable.setHeaderVisible( true );

		dynamicTable.addKeyListener( new KeyAdapter( ) {

			public void keyPressed( KeyEvent e )
			{
				if ( e.keyCode == SWT.DEL )
				{
					int itemCount = dynamicTable.getItemCount( );
					if ( dynamicSelectIndex == itemCount )
					{
						return;
					}
					if ( dynamicSelectIndex == itemCount - 1 )
					{
						dynamicSelectIndex--;
					}
					try
					{
						handleDynamicDelEvent( );
					}
					catch ( Exception e1 )
					{
						WidgetUtil.processError( getShell( ), e1 );
					}
					refreshDynamicViewer( );
				}
			}
		} );

		dynamicViewer = new TableViewer( dynamicTable );
		String[] columns = new String[]{
				" ", Messages.getString( "LevelPropertyDialog.Label.Attribute" ) //$NON-NLS-1$ //$NON-NLS-2$
		};

		TableColumn column = new TableColumn( dynamicTable, SWT.LEFT );
		column.setText( columns[0] );
		column.setWidth( 15 );

		TableColumn column1 = new TableColumn( dynamicTable, SWT.LEFT );
		column1.setResizable( columns[1] != null );
		if ( columns[1] != null )
		{
			column1.setText( columns[1] );
		}
		column1.setWidth( 200 );

		dynamicViewer.setColumnProperties( new String[]{
				"", prop_Attribute //$NON-NLS-1$
		} );
		editor = new ComboBoxCellEditor( dynamicViewer.getTable( ),
				attributeItems,
				SWT.READ_ONLY );
		CellEditor[] cellEditors = new CellEditor[]{
				null, editor
		};
		dynamicViewer.setCellEditors( cellEditors );

		dynamicViewer.setContentProvider( contentProvider );
		dynamicViewer.setLabelProvider( dynamicLabelProvider );
		dynamicViewer.setCellModifier( dynamicCellModifier );

		return contents;
	}

	String[] attributeItems = new String[0];

	private void resetEditorItems( )
	{
		resetEditorItems( null );
	}

	private void resetEditorItems( String name )
	{
		List list = new ArrayList( );
		list.addAll( Arrays.asList( attributeItems ) );

		Iterator attrIter = input.attributesIterator( );
		while ( attrIter.hasNext( ) )
		{
			LevelAttributeHandle handle = (LevelAttributeHandle) attrIter.next( );
			list.remove( handle.getName( ) );
		}

		list.remove( input.getColumnName( ) );
		if ( name != null && !list.contains( name ) )
		{
			list.add( 0, name );
		}
		String[] temps = new String[list.size( )];
		list.toArray( temps );
		editor.setItems( temps );
	}

	protected void handleDynamicDelEvent( )
	{
		if ( dynamicViewer.getSelection( ) != null
				&& dynamicViewer.getSelection( ) instanceof StructuredSelection )
		{
			Object element = ( (StructuredSelection) dynamicViewer.getSelection( ) ).getFirstElement( );
			if ( element instanceof LevelAttributeHandle )
			{
				try
				{
					( (LevelAttributeHandle) element ).drop( );
				}
				catch ( PropertyValueException e )
				{
					ExceptionHandler.handle( e );
				}
			}
		}

	}

	protected void handleStaticDelEvent( )
	{
		if ( staticViewer.getSelection( ) != null
				&& staticViewer.getSelection( ) instanceof StructuredSelection )
		{
			Object element = ( (StructuredSelection) staticViewer.getSelection( ) ).getFirstElement( );
			if ( element instanceof RuleHandle )
			{
				try
				{
					( (RuleHandle) element ).drop( );
				}
				catch ( PropertyValueException e )
				{
					ExceptionHandler.handle( e );
				}
			}
		}

	}

	private void createChoiceArea( Composite parent )
	{
		Composite contents = new Composite( parent, SWT.NONE );
		GridLayout layout = new GridLayout( );
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 2;
		contents.setLayout( layout );
		GridData data = new GridData( GridData.FILL_HORIZONTAL );
		contents.setLayoutData( data );
		dynamicButton = new Button( contents, SWT.RADIO );
		dynamicButton.addSelectionListener( new SelectionAdapter( ) {

			public void widgetSelected( SelectionEvent e )
			{
				try
				{
					input.setLevelType( DesignChoiceConstants.LEVEL_TYPE_DYNAMIC );
				}
				catch ( SemanticException e1 )
				{
					ExceptionHandler.handle( e1 );
				}

				updateButtonStatus( dynamicButton );
			}

		} );
		staticButton = new Button( contents, SWT.RADIO );
		staticButton.addSelectionListener( new SelectionAdapter( ) {

			public void widgetSelected( SelectionEvent e )
			{
				try
				{
					input.setLevelType( DesignChoiceConstants.LEVEL_TYPE_MIRRORED );
				}
				catch ( SemanticException e1 )
				{
					ExceptionHandler.handle( e1 );
				}

				updateButtonStatus( staticButton );
			}

		} );
		dynamicButton.setText( Messages.getString( "LevelPropertyDialog.Button.Dynamic" ) ); //$NON-NLS-1$
		staticButton.setText( Messages.getString( "LevelPropertyDialog.Button.Static" ) ); //$NON-NLS-1$
	}

	protected void updateButtonStatus( Button button )
	{
		if ( button == dynamicButton )
		{
			staticButton.setSelection( false );
			dynamicButton.setSelection( true );
			setExcludeGridData( staticArea, true );
			setExcludeGridData( dynamicArea, false );
			try
			{
				input.setLevelType( DesignChoiceConstants.LEVEL_TYPE_DYNAMIC );
			}
			catch ( SemanticException e )
			{
				ExceptionHandler.handle( e );
			}
		}
		else if ( button == staticButton )
		{
			dynamicButton.setSelection( false );;
			staticButton.setSelection( true );
			setExcludeGridData( dynamicArea, true );
			setExcludeGridData( staticArea, false );
			try
			{
				input.setLevelType( DesignChoiceConstants.LEVEL_TYPE_MIRRORED );
			}
			catch ( SemanticException e )
			{
				ExceptionHandler.handle( e );
			}
		}
		this.getShell( ).layout( );
		checkOkButtonStatus( );
	}

	private TabularLevelHandle input;
	private TableViewer dynamicViewer;
	private Text nameText;

	private ComboBoxCellEditor editor;
	private TableViewer staticViewer;
	private Composite staticArea;
	private Button dynamicButton;
	private Button staticButton;
	protected int staticSelectIndex;
	private static final String Prop_Name = "Name"; //$NON-NLS-1$
	private static final String prop_Expression = "Expression"; //$NON-NLS-1$
	private static final String prop_Attribute = "Attribute"; //$NON-NLS-1$
	private static final String Prop_DefaultValue = "DefaultValue";//$NON-NLS-1$
	private static final String prop_Tooltip = "Tooltip";//$NON-NLS-1$
	private Table dynamicTable;
	private ExpressionCellEditor expressionEditor;
	private Combo staticDataTypeCombo;
	private Text staticNameText;
	private Combo fieldCombo;
	private Combo dynamicDataTypeCombo;
	private Combo displayKeyCombo;
	private TableViewer defaultValueViewer;

	protected Composite createStaticArea( Composite parent )
	{
		Composite container = new Composite( parent, SWT.NONE );
		GridLayout layout = new GridLayout( );
		layout.marginWidth = layout.marginHeight = 0;
		container.setLayout( layout );
		container.setLayoutData( new GridData( GridData.FILL_BOTH ) );

		Group properties = new Group( container, SWT.NONE );
		layout = new GridLayout( );
		layout.numColumns = 2;
		properties.setLayout( layout );
		properties.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
		new Label( properties, SWT.NONE ).setText( Messages.getString( "LevelPropertyDialog.Name" ) ); //$NON-NLS-1$
		staticNameText = new Text( properties, SWT.BORDER );
		staticNameText.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
		staticNameText.addModifyListener( new ModifyListener( ) {

			public void modifyText( ModifyEvent e )
			{
				checkOkButtonStatus( );
			}

		} );
		new Label( properties, SWT.NONE ).setText( Messages.getString( "LevelPropertyDialog.DataType" ) ); //$NON-NLS-1$
		staticDataTypeCombo = new Combo( properties, SWT.BORDER | SWT.READ_ONLY );
		staticDataTypeCombo.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
		staticDataTypeCombo.addSelectionListener( new SelectionAdapter( ) {

			public void widgetSelected( SelectionEvent e )
			{
				checkOkButtonStatus( );
			}

		} );
		Group contents = new Group( container, SWT.NONE );
		layout = new GridLayout( );
		contents.setLayout( layout );
		contents.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );

		final Table staticTable = new Table( contents, SWT.SINGLE
				| SWT.FULL_SELECTION
				| SWT.BORDER
				| SWT.VERTICAL
				| SWT.HORIZONTAL );
		GridData gd = new GridData( GridData.FILL_BOTH );
		gd.heightHint = 150;
		staticTable.setLayoutData( gd );
		staticTable.setLinesVisible( true );
		staticTable.setHeaderVisible( true );

		staticTable.addKeyListener( new KeyAdapter( ) {

			public void keyPressed( KeyEvent e )
			{
				if ( e.keyCode == SWT.DEL )
				{
					int itemCount = staticTable.getItemCount( );
					if ( staticSelectIndex == itemCount )
					{
						return;
					}
					if ( staticSelectIndex == itemCount - 1 )
					{
						staticSelectIndex--;
					}
					try
					{
						handleStaticDelEvent( );
					}
					catch ( Exception e1 )
					{
						WidgetUtil.processError( getShell( ), e1 );
					}
					refreshStaticViewer( );
				}
			}
		} );

		staticViewer = new TableViewer( staticTable );
		String[] columns = new String[]{
				"", //$NON-NLS-1$
				Messages.getString( "LevelPropertyDialog.Label.Name" ), //$NON-NLS-1$
				Messages.getString( "LevelPropertyDialog.Label.Expression" ) //$NON-NLS-1$
		};

		int[] widths = new int[]{
				15, 150, 150
		};

		for ( int i = 0; i < columns.length; i++ )
		{
			TableColumn column = new TableColumn( staticTable, SWT.LEFT );
			column.setResizable( columns[i] != null );
			if ( columns[i] != null )
			{
				column.setText( columns[i] );
			}
			column.setWidth( widths[i] );
		}

		staticViewer.setColumnProperties( new String[]{
				"", //$NON-NLS-1$
				LevelPropertyDialog.Prop_Name,
				LevelPropertyDialog.prop_Expression
		} );

		expressionEditor = new ExpressionCellEditor( staticTable );
		CellEditor[] cellEditors = new CellEditor[]{
				null, new TextCellEditor( staticTable ), expressionEditor

		};
		staticViewer.setCellEditors( cellEditors );

		staticViewer.setContentProvider( contentProvider );
		staticViewer.setLabelProvider( staticLabelProvider );
		staticViewer.setCellModifier( staticCellModifier );

		final Table defaultValueTable = new Table( contents, SWT.SINGLE
				| SWT.FULL_SELECTION
				| SWT.BORDER
				| SWT.VERTICAL
				| SWT.HORIZONTAL );
		defaultValueTable.setHeaderVisible( false );
		defaultValueTable.setLinesVisible( true );
		gd = new GridData( GridData.FILL_HORIZONTAL );
		gd.heightHint = defaultValueTable.getItemHeight( );
		defaultValueTable.setLayoutData( gd );

		defaultValueViewer = new TableViewer( defaultValueTable );

		for ( int i = 0; i < columns.length; i++ )
		{
			TableColumn column = new TableColumn( defaultValueTable, SWT.LEFT );
			column.setResizable( columns[i] != null );
			column.setWidth( widths[i] );
		}

		defaultValueViewer.setColumnProperties( new String[]{
				"", //$NON-NLS-1$
				LevelPropertyDialog.Prop_DefaultValue,
				LevelPropertyDialog.prop_Tooltip
		} );

		cellEditors = new CellEditor[]{
				null, new TextCellEditor( defaultValueTable ), null
		};

		defaultValueViewer.setCellEditors( cellEditors );

		defaultValueViewer.setContentProvider( defaultValueContentProvider );
		defaultValueViewer.setLabelProvider( defaultValueLabelProvider );
		defaultValueViewer.setCellModifier( defaultValueCellModifier );

		return container;

	}

	public void setInput( TabularLevelHandle level )
	{
		this.input = level;
	}

	public static void setExcludeGridData( Control control, boolean exclude )
	{
		Object obj = control.getLayoutData( );
		if ( obj == null )
			control.setLayoutData( new GridData( ) );
		else if ( !( obj instanceof GridData ) )
			return;
		GridData data = (GridData) control.getLayoutData( );
		if ( exclude )
		{
			data.heightHint = 0;
		}
		else
		{
			data.heightHint = -1;
		}
		control.setLayoutData( data );
		control.getParent( ).layout( );
		control.setVisible( !exclude );
	}

	protected void checkOkButtonStatus( )
	{
		if ( dynamicButton.getSelection( ) )
		{
			if ( nameText.getText( ) == null
					|| nameText.getText( ).trim( ).equals( "" ) //$NON-NLS-1$
					|| fieldCombo.getSelectionIndex( ) == -1
					|| dynamicDataTypeCombo.getSelectionIndex( ) == -1 )
			{
				if ( getButton( IDialogConstants.OK_ID ) != null )
					getButton( IDialogConstants.OK_ID ).setEnabled( false );
				setMessage( null );
				setErrorMessage( Messages.getString( "LevelPropertyDialog.Message.BlankName" ) ); //$NON-NLS-1$
			}
			else if ( !UIUtil.validateDimensionName( nameText.getText( ) ) )
			{
				if ( getButton( IDialogConstants.OK_ID ) != null )
					getButton( IDialogConstants.OK_ID ).setEnabled( false );
				setMessage( null );
				setErrorMessage( Messages.getString( "LevelPropertyDialog.Message.NumericName" ) ); //$NON-NLS-1$
			}
			else
			{
				if ( getButton( IDialogConstants.OK_ID ) != null )
					getButton( IDialogConstants.OK_ID ).setEnabled( true );
				setErrorMessage( null );
				setMessage( Messages.getString( "LevelPropertyDialog.Message" ) ); //$NON-NLS-1$
			}
		}
		else
		{
			if ( staticNameText.getText( ) == null
					|| staticNameText.getText( ).trim( ).equals( "" ) //$NON-NLS-1$
					|| staticDataTypeCombo.getSelectionIndex( ) == -1 )
			{
				if ( getButton( IDialogConstants.OK_ID ) != null )
					getButton( IDialogConstants.OK_ID ).setEnabled( false );
				setMessage( null );
				setErrorMessage( Messages.getString( "LevelPropertyDialog.Message.BlankName" ) ); //$NON-NLS-1$	
			}
			else if ( !UIUtil.validateDimensionName( staticNameText.getText( ) ) )
			{
				if ( getButton( IDialogConstants.OK_ID ) != null )
					getButton( IDialogConstants.OK_ID ).setEnabled( false );
				setMessage( null );
				setErrorMessage( Messages.getString( "LevelPropertyDialog.Message.NumericName" ) ); //$NON-NLS-1$
			}
			else
			{
				if ( getButton( IDialogConstants.OK_ID ) != null )
					getButton( IDialogConstants.OK_ID ).setEnabled( true );
				setErrorMessage( null );
				setMessage( Messages.getString( "LevelPropertyDialog.Message" ) ); //$NON-NLS-1$
			}
		}
	}

	protected void createButtonsForButtonBar( Composite parent )
	{
		super.createButtonsForButtonBar( parent );
		checkOkButtonStatus( );
		if ( input != null && input.getLevelType( ) != null )
		{
			if ( input.getLevelType( )
					.equals( DesignChoiceConstants.LEVEL_TYPE_DYNAMIC ) )
			{
				dynamicButton.setSelection( true );
				staticButton.setSelection( false );
			}
			else
			{
				dynamicButton.setSelection( false );
				staticButton.setSelection( true );
			}
		}
	}
}

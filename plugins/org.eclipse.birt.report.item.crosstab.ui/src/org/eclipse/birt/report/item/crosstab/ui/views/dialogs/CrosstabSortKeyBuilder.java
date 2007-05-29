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

package org.eclipse.birt.report.item.crosstab.ui.views.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.birt.core.data.ExpressionUtil;
import org.eclipse.birt.data.engine.api.IBinding;
import org.eclipse.birt.data.engine.core.DataException;
import org.eclipse.birt.data.engine.olap.api.query.ICubeQueryDefinition;
import org.eclipse.birt.data.engine.olap.api.query.ILevelDefinition;
import org.eclipse.birt.report.data.adapter.api.DataRequestSession;
import org.eclipse.birt.report.data.adapter.api.DataSessionContext;
import org.eclipse.birt.report.designer.core.model.SessionHandleAdapter;
import org.eclipse.birt.report.designer.internal.ui.util.ExceptionHandler;
import org.eclipse.birt.report.designer.internal.ui.util.UIUtil;
import org.eclipse.birt.report.designer.ui.dialogs.ExpressionProvider;
import org.eclipse.birt.report.designer.ui.dialogs.SortkeyBuilder;
import org.eclipse.birt.report.designer.ui.newelement.DesignElementFactory;
import org.eclipse.birt.report.designer.ui.views.attributes.providers.ChoiceSetFactory;
import org.eclipse.birt.report.designer.util.DEUtil;
import org.eclipse.birt.report.item.crosstab.core.ICrosstabConstants;
import org.eclipse.birt.report.item.crosstab.core.ILevelViewConstants;
import org.eclipse.birt.report.item.crosstab.core.de.CrosstabReportItemHandle;
import org.eclipse.birt.report.item.crosstab.core.de.CrosstabViewHandle;
import org.eclipse.birt.report.item.crosstab.core.de.DimensionViewHandle;
import org.eclipse.birt.report.item.crosstab.core.de.LevelViewHandle;
import org.eclipse.birt.report.item.crosstab.internal.ui.editors.model.CrosstabAdaptUtil;
import org.eclipse.birt.report.item.crosstab.internal.ui.util.CrosstabUIHelper;
import org.eclipse.birt.report.item.crosstab.ui.i18n.Messages;
import org.eclipse.birt.report.item.crosstab.ui.views.attributes.widget.ExpressionValueCellEditor;
import org.eclipse.birt.report.model.api.CommandStack;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.ExtendedItemHandle;
import org.eclipse.birt.report.model.api.MemberValueHandle;
import org.eclipse.birt.report.model.api.RuleHandle;
import org.eclipse.birt.report.model.api.SortElementHandle;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.eclipse.birt.report.model.api.extension.ExtendedElementException;
import org.eclipse.birt.report.model.api.metadata.IChoice;
import org.eclipse.birt.report.model.api.olap.DimensionHandle;
import org.eclipse.birt.report.model.api.olap.LevelHandle;
import org.eclipse.birt.report.model.elements.interfaces.IMemberValueModel;
import org.eclipse.birt.report.model.elements.interfaces.ISortElementModel;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * 
 */

public class CrosstabSortKeyBuilder extends SortkeyBuilder
{

	protected final String[] columns = new String[]{
			" ",
			Messages.getString( "SelColumnMemberValue.Column.Level" ),
			Messages.getString( "SelColumnMemberValue.Column.Value" )
	};

	protected SortElementHandle input;
	protected List groupLevelList;
	protected List groupLevelNameList;

	protected Combo textKey;
	protected Combo comboGroupLevel;

	protected LevelViewHandle levelViewHandle;

	protected Table memberValueTable;
	protected TableViewer dynamicViewer;

	protected MemberValueHandle memberValueHandle;
	protected List referencedLevelList;

	public void setHandle( DesignElementHandle handle )
	{
		this.handle = handle;
		if(editor != null)
		{
			editor.setExpressionProvider( new ExpressionProvider(handle) );
		}
	}

	public void setInput( SortElementHandle input,
			LevelViewHandle levelViewHandle )
	{
		this.input = input;
		this.levelViewHandle = levelViewHandle;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.dialogs.BaseDialog#initDialog()
	 */
	protected boolean initDialog( )
	{
		if ( input == null )
		{
			textKey.setText( "" );
			if ( comboGroupLevel.getItemCount( ) == 0 )
			{
				comboGroupLevel.add( DEUtil.resolveNull( null ) );
			}			
			if ( textKey.getItemCount( ) == 0 )
			{
				textKey.add( DEUtil.resolveNull( null ) );
			}		
			comboGroupLevel.select( 0 );
			comboDirection.select( 0 );
			updateBindings( );
			updateMemberValues( );
			return true;
		}

		getLevels( );

		int levelIndex = groupLevelList.indexOf( levelViewHandle );
		if ( levelIndex >= 0 )
		{
			comboGroupLevel.select( levelIndex );
			updateBindings( );
		}

		if ( input.getKey( ) != null && input.getKey( ).trim( ).length( ) != 0 )
		{
			int index = getBindingIndex( input.getKey( ) );
			if ( index != -1 )
			{
				textKey.select( index );
			}
			else
			{
				textKey.setText( input.getKey( ) );
			}

		}

		if ( input.getDirection( ) != null
				&& input.getDirection( ).trim( ).length( ) != 0 )
		{
			String value = input.getDirection( ).trim( );
			IChoice choice = choiceSet.findChoice( value );
			if ( choice != null )
				value = choice.getDisplayName( );
			int index;
			index = comboDirection.indexOf( value );
			index = index < 0 ? 0 : index;
			comboDirection.select( index );
		}
		updateButtons( );
		return true;
	}

	/**
	 * Notifies that the ok button of this dialog has been pressed.
	 * <p>
	 * The <code>Dialog</code> implementation of this framework method sets
	 * this dialog's return code to <code>Window.OK</code> and closes the
	 * dialog. Subclasses may override.
	 * </p>
	 */
	protected void okPressed( )
	{
		LevelViewHandle level = (LevelViewHandle) groupLevelList.get( comboGroupLevel.getSelectionIndex( ) );
		String direction = comboDirection.getText( );
		IChoice choice = choiceSet.findChoiceByDisplayName( direction );
		if ( choice != null )
			direction = choice.getDisplayName( );
		int index;
		index = comboDirection.indexOf( direction );
		CommandStack stack = SessionHandleAdapter.getInstance( )
				.getCommandStack( );
		stack.startTrans( title ); //$NON-NLS-1$ 
		try
		{
			if ( input == null )
			{

				SortElementHandle sortElement = DesignElementFactory.getInstance( )
						.newSortElement( );
				sortElement.setKey( ExpressionUtil.createJSDataExpression( textKey.getText( ) ) );
				if ( index >= 0 )
				{
					sortElement.setDirection( choice.getName( ) );
				}

				// test code -- begin --
//				MemberValueHandle parent = memberValueHandle;
//				while(true)
//				{						
//					if(parent != null)
//					{							
//						parent.getCubeLevelName( );
//						parent.getValue( );
//						MemberValueHandle child = getChildMemberValue(parent);
//						parent = child;
//					}else
//					{
//						break;
//					}
//					
//				}
				// test code --  end  --
				
				if ( referencedLevelList != null && referencedLevelList.size( ) > 0 )
				{
					sortElement.add( ISortElementModel.MEMBER_PROP, memberValueHandle );
				}
				
				DesignElementHandle designElement = level.getModelHandle( );
				designElement.add( ILevelViewConstants.SORT_PROP, sortElement );

			}
			else
			// edit
			{
				if ( level == levelViewHandle )
				{
					input.setKey( ExpressionUtil.createJSDataExpression( textKey.getText( ) ) );
					if ( index >= 0 )
					{
						input.setDirection( choice.getName( ) );
					}
					
					if(input.getMember() != null)
					{
						input.drop( ISortElementModel.MEMBER_PROP, 0 );
					}
					
					// test code -- begin --
//					MemberValueHandle parent = memberValueHandle;
//					while(true)
//					{						
//						if(parent != null)
//						{							
//							parent.getCubeLevelName( );
//							parent.getValue( );
//							MemberValueHandle child = getChildMemberValue(parent);
//							parent = child;
//						}else
//						{
//							break;
//						}
//						
//					}
					// test code --  end  --

					if ( referencedLevelList != null && referencedLevelList.size( ) > 0 )
					{
						input.add(ISortElementModel.MEMBER_PROP, memberValueHandle );
					}
				}
				else
				// The level is changed
				{
					SortElementHandle sortElement = DesignElementFactory.getInstance( )
							.newSortElement( );
					sortElement.setKey( ExpressionUtil.createJSDataExpression( textKey.getText( ) ) );
					if ( index >= 0 )
					{
						sortElement.setDirection( choice.getName( ) );
					}
					
					// test code -- begin --
//					MemberValueHandle parent = memberValueHandle;
//					while(true)
//					{						
//						if(parent != null)
//						{							
//							parent.getCubeLevelName( );
//							parent.getValue( );
//							MemberValueHandle child = getChildMemberValue(parent);
//							parent = child;
//						}else
//						{
//							break;
//						}
//						
//					}
					// test code --  end  --
					
					if ( referencedLevelList != null && referencedLevelList.size( ) > 0 )
					{
						sortElement.add( ISortElementModel.MEMBER_PROP, memberValueHandle );
					}
					levelViewHandle.getModelHandle( )
							.drop( ILevelViewConstants.SORT_PROP, input );
					level.getModelHandle( ).add( ILevelViewConstants.SORT_PROP,
							sortElement );
				}

			}
			stack.commit( );
		}
		catch ( SemanticException e )
		{
			ExceptionHandler.handle( e,
					Messages.getString( "SortkeyBuilder.DialogTitle.Error.SetSortKey.Title" ),
					e.getLocalizedMessage( ) );
			stack.rollback( );
		}

		setReturnCode( OK );
		close( );
	}

	/**
	 * @param title
	 */
	public CrosstabSortKeyBuilder( String title, String message )
	{
		this( UIUtil.getDefaultShell( ), title, message );
	}

	/**
	 * @param parentShell
	 * @param title
	 */
	public CrosstabSortKeyBuilder( Shell parentShell, String title,
			String message )
	{
		super( parentShell, title, message );
	}

	protected Composite createInputContents( Composite parent )
	{
		// Label lb = new Label( parent, SWT.NONE );
		// lb.setText( Messages.getString(
		// "SortkeyBuilder.DialogTitle.Label.Prompt" ) );

		Composite content = new Composite( parent, SWT.NONE );
		content.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
		GridLayout glayout = new GridLayout( 3, false );
		content.setLayout( glayout );
		Label groupLevel = new Label( content, SWT.NONE );
		groupLevel.setText( Messages.getString( "CrosstabSortkeyBuilder.DialogTitle.Label.GroupLevel" ) );
		comboGroupLevel = new Combo( content, SWT.READ_ONLY | SWT.BORDER );
		GridData gdata = new GridData( GridData.FILL_HORIZONTAL );
		comboGroupLevel.setLayoutData( gdata );
		comboGroupLevel.addListener( SWT.Selection, ComboGroupLeveModify );

		getLevels( );
		String groupLeveNames[] = (String[]) groupLevelNameList.toArray( new String[groupLevelNameList.size( )] );
		comboGroupLevel.setItems( groupLeveNames );

		new Label( content, SWT.NONE );

		Label labelKey = new Label( content, SWT.NONE );
		labelKey.setText( Messages.getString( "SortkeyBuilder.DialogTitle.Label.Key" ) );
		textKey = new Combo( content, SWT.READ_ONLY | SWT.BORDER );
		gdata = new GridData( GridData.FILL_HORIZONTAL );
		textKey.setLayoutData( gdata );
		textKey.addListener( SWT.Selection, ComboKeyModify );
		textKey.addModifyListener( new ModifyListener( ) {

			public void modifyText( ModifyEvent e )
			{
				updateMemberValues( );
				updateButtons( );
			}
		} );
		if ( textKey.getItemCount( ) == 0 )
		{
			textKey.add( DEUtil.resolveNull( null ) );
		}

		new Label( content, SWT.NONE );

		Label labelDirection = new Label( content, SWT.NONE );
		labelDirection.setText( Messages.getString( "SortkeyBuilder.DialogTitle.Label.Direction" ) );

		comboDirection = new Combo( content, SWT.READ_ONLY | SWT.BORDER );
		gdata = new GridData( GridData.FILL_HORIZONTAL );
		comboDirection.setLayoutData( gdata );
		String[] displayNames = ChoiceSetFactory.getDisplayNamefromChoiceSet( choiceSet );
		comboDirection.setItems( displayNames );

		createMemberValuesGroup( content );
		return content;
	}

	protected Listener ComboGroupLeveModify = new Listener( ) {

		public void handleEvent( Event e )
		{
			updateBindings( );
			updateMemberValues( );
		}
	};

	protected Listener ComboKeyModify = new Listener( ) {

		public void handleEvent( Event e )
		{			
			updateMemberValues( );
			updateButtons( );			
		}
	};

	protected void createMemberValuesGroup( Composite content )
	{
		Group group = new Group( content, SWT.NONE );
		group.setText( Messages.getString( "CrosstabSortKeyBuilder.Label.SelColumnMemberValue" ) ); //$NON-NLS-1$
		group.setLayout( new GridLayout( ) );

		memberValueTable = new Table( group, SWT.SINGLE
				| SWT.BORDER
				| SWT.H_SCROLL
				| SWT.V_SCROLL
				| SWT.FULL_SELECTION );
		memberValueTable.setLinesVisible( true );
		memberValueTable.setHeaderVisible( true );
		memberValueTable.setLayoutData( new GridData( GridData.FILL_BOTH ) );
		GridData gd = new GridData( GridData.FILL_BOTH );
		gd.heightHint = 150;
		gd.horizontalSpan = 3;
		group.setLayoutData( gd );

		dynamicViewer = new TableViewer( memberValueTable );

		TableColumn column = new TableColumn( memberValueTable, SWT.LEFT );
		column.setText( columns[0] );
		column.setWidth( 15 );

		TableColumn column1 = new TableColumn( memberValueTable, SWT.LEFT );
		column1.setResizable( columns[1] != null );
		if ( columns[1] != null )
		{
			column1.setText( columns[1] );
		}
		column1.setWidth( 200 );

		TableColumn column2 = new TableColumn( memberValueTable, SWT.LEFT );
		column2.setResizable( columns[2] != null );
		if ( columns[2] != null )
		{
			column2.setText( columns[2] );
		}
		column2.setWidth( 200 );

		dynamicViewer.setColumnProperties( columns );
		editor = new ExpressionValueCellEditor( dynamicViewer.getTable( ),
				SWT.READ_ONLY );
		TextCellEditor textEditor = new TextCellEditor( dynamicViewer.getTable( ),
				SWT.READ_ONLY );
		TextCellEditor textEditor2 = new TextCellEditor( dynamicViewer.getTable( ),
				SWT.READ_ONLY );
		CellEditor[] cellEditors = new CellEditor[]{
				textEditor, textEditor2, editor
		};
		if(handle != null)
		{
			editor.setExpressionProvider( new ExpressionProvider(handle) );
			editor.setReportElement((ExtendedItemHandle)handle);
		}
		dynamicViewer.setCellEditors( cellEditors );

		dynamicViewer.setContentProvider( contentProvider );
		dynamicViewer.setLabelProvider( labelProvider );
		dynamicViewer.setCellModifier( cellModifier );
		dynamicViewer.addSelectionChangedListener( selectionChangeListener );
	}

	private ISelectionChangedListener selectionChangeListener = new ISelectionChangedListener(){

		public void selectionChanged( SelectionChangedEvent event )
		{
			// TODO Auto-generated method stub
			ISelection selection = event.getSelection( );
			if(selection instanceof StructuredSelection)
			{
				Object obj = ((StructuredSelection) selection).getFirstElement( );
				if(obj != null && obj instanceof MemberValueHandle && editor != null)
				{
					editor.setMemberValue( (MemberValueHandle)obj );
				}
			}
			
		}};
		
	private String[] valueItems = new String[0];
	private static final String dummyChoice = "dummy"; //$NON-NLS-1$
	private IStructuredContentProvider contentProvider = new IStructuredContentProvider( ) {

		public void dispose( )
		{
		}

		public void inputChanged( Viewer viewer, Object oldInput,
				Object newInput )
		{
		}

		public Object[] getElements( Object inputObj )
		{
			if(!(inputObj instanceof List))
			{
				return new Object[0];
			}
			return ((List)inputObj).toArray( );
		}
	};

	private ITableLabelProvider labelProvider = new ITableLabelProvider( ) {

		public Image getColumnImage( Object element, int columnIndex )
		{
			return null;
		}

		public String getColumnText( Object element, int columnIndex )
		{
			if ( columnIndex == 0 )
			{
				if ( element == dummyChoice )
					return Messages.getString( "LevelPropertyDialog.MSG.CreateNew" );
				else
				{
					if ( element instanceof RuleHandle )
					{
						return ( (RuleHandle) element ).getDisplayExpression( );
					}
					return "";
				}
			}
			else if ( columnIndex == 1 )
			{
				return ( (MemberValueHandle) element ).getLevel( ).getName( );
			}
			else if ( columnIndex == 2 )
			{
				String value = ( (MemberValueHandle) element ).getValue( );
				return  value == null ? "":value;
			}
			return "";
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

	private ICellModifier cellModifier = new ICellModifier( ) {

		public boolean canModify( Object element, String property )
		{
			if ( Arrays.asList( columns ).indexOf( property ) == 2 )
			{
				return true;
			}
			else
			{
				return false;
			}

		}

		public Object getValue( Object element, String property )
		{
			if ( Arrays.asList( columns ).indexOf( property ) != 2 )
			{
				return "";
			}
			String value = ( (MemberValueHandle) element ).getValue( );
			return  value == null ? "":value;

		}

		public void modify( Object element, String property, Object value )
		{
			if ( Arrays.asList( columns ).indexOf( property ) != 2 )
			{
				return;
			}
			TableItem item = (TableItem) element;
			MemberValueHandle memberValue= (MemberValueHandle)item.getData( );
			try
			{
				( (MemberValueHandle) memberValue ).setValue( (String)value );
			}
			catch ( SemanticException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			dynamicViewer.refresh( );
		}
	};
	private ExpressionValueCellEditor editor;

	protected boolean isConditionOK( )
	{
		if ( textKey.getText( ).trim( ).length( ) == 0
				|| comboGroupLevel.getText( ).trim( ).length( ) == 0 )
		{
			return false;
		}
		return true;
	}

	private List getGroupLevelNameList( )
	{
		if ( groupLevelNameList != null || groupLevelNameList.size( ) == 0 )
		{
			return groupLevelNameList;
		}
		getLevels( );
		return groupLevelNameList;
	}

	private List getLevels( )
	{
		if ( groupLevelList != null )
		{
			return groupLevelList;
		}
		groupLevelList = new ArrayList( );
		groupLevelNameList = new ArrayList( );
		ExtendedItemHandle element = (ExtendedItemHandle) handle;
		CrosstabReportItemHandle crossTab = null;
		try
		{
			crossTab = (CrosstabReportItemHandle) element.getReportItem( );
		}
		catch ( ExtendedElementException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace( );
		}
		if ( crossTab == null )
		{
			return groupLevelList;
		}
		if ( crossTab.getCrosstabView( ICrosstabConstants.COLUMN_AXIS_TYPE ) != null )
		{
			DesignElementHandle elementHandle = crossTab.getCrosstabView( ICrosstabConstants.COLUMN_AXIS_TYPE )
					.getModelHandle( );
			getLevel( (ExtendedItemHandle) elementHandle );
		}

		if ( crossTab.getCrosstabView( ICrosstabConstants.ROW_AXIS_TYPE ) != null )
		{
			DesignElementHandle elementHandle = crossTab.getCrosstabView( ICrosstabConstants.ROW_AXIS_TYPE )
					.getModelHandle( );
			getLevel( (ExtendedItemHandle) elementHandle );
		}

		return groupLevelList;
	}

	private void getLevel( ExtendedItemHandle handle )
	{
		CrosstabViewHandle crossTabViewHandle = null;
		try
		{
			crossTabViewHandle = (CrosstabViewHandle) handle.getReportItem( );
		}
		catch ( ExtendedElementException e )
		{
			ExceptionHandler.handle( e );
		}
		if ( crossTabViewHandle == null )
		{
			return;
		}
		int dimensionCount = crossTabViewHandle.getDimensionCount( );
		for ( int i = 0; i < dimensionCount; i++ )
		{
			DimensionViewHandle dimension = crossTabViewHandle.getDimension( i );
			int levelCount = dimension.getLevelCount( );
			for ( int j = 0; j < levelCount; j++ )
			{
				LevelViewHandle levelHandle = dimension.getLevel( j );
				groupLevelList.add( levelHandle );
				groupLevelNameList.add( levelHandle.getCubeLevel( )
						.getFullName( ) );
			}
		}

	}

	private void updateBindings( )
	{
		
		LevelViewHandle level = null;
		if(comboGroupLevel.getSelectionIndex( ) != -1 && groupLevelList != null && groupLevelList.size( ) > 0)
		{			
			level = (LevelViewHandle) groupLevelList.get( comboGroupLevel.getSelectionIndex( ) );
		}
		
		
		if ( level == null )
		{
			textKey.setItems( new String[]{DEUtil.resolveNull( null )} );
			return;
		}

		textKey.removeAll( );
		List bindingList = getReferableBindings( level );
		for ( int i = 0; i < bindingList.size( ); i++ )
		{
			try
			{
				textKey.add( ( (IBinding) ( bindingList.get( i ) ) ).getBindingName( ) );
			}
			catch ( DataException e )
			{
				e.printStackTrace( );
			}
		}

		if ( textKey.getItemCount( ) == 0 )
		{
			textKey.add( DEUtil.resolveNull( null ) );
		}

		if ( textKey.indexOf( textKey.getText( ) ) < 0 )
		{
			textKey.select( 0 );
		}
	}

	private void updateMemberValues( )
	{
		if ( comboGroupLevel.getSelectionIndex( ) < 0
				|| textKey.getText( ).length( ) == 0 )
		{
			memberValueTable.setEnabled( false );
			return;
		}
		LevelViewHandle level = null;
		if(comboGroupLevel.getSelectionIndex( ) != -1 && groupLevelList != null && groupLevelList.size( ) > 0)
		{			
			level = (LevelViewHandle) groupLevelList.get( comboGroupLevel.getSelectionIndex( ) );
		}
		if ( level == null )
		{
			memberValueTable.setEnabled( false );
			return;
		}

		String bindingExpr = ExpressionUtil.createJSDataExpression( textKey.getText( ) );
		referencedLevelList = getReferencedLevels( level, bindingExpr );
		if ( referencedLevelList == null || referencedLevelList.size( ) == 0 )
		{
			memberValueTable.setEnabled( false );
			return;
		}

		editor.setLevel( level.getCubeLevel( ) );
		editor.setReferencedLevelList( referencedLevelList );
		
		memberValueTable.setEnabled( true );
		if ( level == levelViewHandle )
		{
			memberValueHandle = input.getMember( );
		}

		if ( memberValueHandle == null )
		{
			memberValueHandle = DesignElementFactory.getInstance( )
					.newMemberValue( );
		}
		memberValueHandle = updateMemberValuesFromLevelList( referencedLevelList,
				memberValueHandle );
		List memList = getMemberValueList(memberValueHandle);
		dynamicViewer.setInput( memList );
	}

	private List getReferencedLevels( LevelViewHandle level, String bindingExpr )
	{
		List retList = null;

		// get targetLevel
		DimensionHandle dimensionHandle = CrosstabAdaptUtil.getDimensionHandle( level.getCubeLevel( ) );
		String targetLevel = ExpressionUtil.createJSDimensionExpression( dimensionHandle.getName( ),
				level.getCubeLevel( ).getName( ) );

		// get cubeQueryDefn
		ICubeQueryDefinition cubeQueryDefn = null;
		DataRequestSession session = null;
		try
		{
			session = DataRequestSession.newSession( new DataSessionContext( DataSessionContext.MODE_DIRECT_PRESENTATION ) );
			cubeQueryDefn = CrosstabUIHelper.createBindingQuery( level.getCrosstab( ) );
			retList = session.getCubeQueryUtil( )
					.getReferencedLevels( targetLevel,
							bindingExpr,
							cubeQueryDefn );
		}
		catch ( Exception e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace( );
		}

		return retList;
	}

	private List getReferableBindings( LevelViewHandle level )
	{
		List retList = null;
		// get targetLevel
		DimensionHandle dimensionHandle = CrosstabAdaptUtil.getDimensionHandle( level.getCubeLevel( ) );
		String targetLevel = ExpressionUtil.createJSDimensionExpression( dimensionHandle.getName( ),
				level.getCubeLevel( ).getName( ) );

		// get cubeQueryDefn
		ICubeQueryDefinition cubeQueryDefn = null;
		DataRequestSession session = null;
		try
		{
			session = DataRequestSession.newSession( new DataSessionContext( DataSessionContext.MODE_DIRECT_PRESENTATION ) );
			cubeQueryDefn = CrosstabUIHelper.createBindingQuery( level.getCrosstab( ) );
			retList = session.getCubeQueryUtil( )
					.getReferableBindings( targetLevel, cubeQueryDefn, false );
		}
		catch ( Exception e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace( );
		}

		return retList;
	}

	private int getBindingIndex( String dataExpression )
	{
		int ret = -1;
		for ( int i = 0; i < textKey.getItemCount( ); i++ )
		{
			String expression = ExpressionUtil.createJSDataExpression( textKey.getItem( i ) );
			if ( expression.endsWith( dataExpression ) )
			{
				return i;
			}
		}
		return ret;
	}

	private MemberValueHandle getChildMemberValue( MemberValueHandle memberValue )
	{
		if ( memberValue.getContentCount( IMemberValueModel.MEMBER_VALUES_PROP ) != 1
				|| memberValue.getContent( IMemberValueModel.MEMBER_VALUES_PROP, 0 ) == null )
		{
			return null;
		}
		return (MemberValueHandle) memberValue.getContent(IMemberValueModel.MEMBER_VALUES_PROP,
				0 );
	}

	private void dropChildMemberValue( MemberValueHandle memberValue )
	{
		MemberValueHandle child = getChildMemberValue( memberValue );
		if ( child == null )
			return;
		try
		{
			memberValue.drop( IMemberValueModel.MEMBER_VALUES_PROP, 0 );
		}
		catch ( SemanticException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace( );
		}
	}

	private MemberValueHandle updateMemberValuesFromLevelList(
			List referenceLevels, MemberValueHandle memberValue )
	{
		int count = referenceLevels.size( );
		MemberValueHandle lastMemberValue = memberValue;

		int hasCount = 0;
		while ( true )
		{
			hasCount++;
			LevelHandle tempLevel = getLevelHandle( (ILevelDefinition) referenceLevels.get( hasCount - 1 ) );
			if ( lastMemberValue.getLevel( ) != tempLevel )
			{
				try
				{
					lastMemberValue.setLevel( tempLevel );
					dropChildMemberValue( lastMemberValue );
				}
				catch ( SemanticException e )
				{
					// TODO Auto-generated catch block
					e.printStackTrace( );
				}
				break;
			}

			if ( getChildMemberValue( lastMemberValue ) == null )
			{
				break;
			}
			if ( hasCount >= count )
			{
				dropChildMemberValue( lastMemberValue );
				break;
			}
			lastMemberValue = getChildMemberValue( lastMemberValue );

		}

		for ( int i = hasCount; i < count; i++ )
		{
			MemberValueHandle newValue = DesignElementFactory.getInstance( )
					.newMemberValue( );
			LevelHandle tempLevel = getLevelHandle( (ILevelDefinition) referenceLevels.get( i ) );
			try
			{
				newValue.setLevel( tempLevel );
				lastMemberValue.add( IMemberValueModel.MEMBER_VALUES_PROP, newValue );
			}
			catch ( SemanticException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace( );
			}

			lastMemberValue = newValue;
		}

		return memberValue;
	}

	private LevelHandle getLevelHandle( ILevelDefinition levelDef )
	{
		LevelHandle levelHandle = null;
		String levelName = levelDef.getName( );
		String dimensionName = levelDef.getHierarchy( )
				.getDimension( )
				.getName( );
		ExtendedItemHandle extHandle = (ExtendedItemHandle) handle;
		CrosstabReportItemHandle crosstab = null;
		try
		{
			crosstab = (CrosstabReportItemHandle) extHandle.getReportItem( );
		}
		catch ( ExtendedElementException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace( );
		}
		DimensionViewHandle dimension = crosstab.getDimension( dimensionName );
		// LevelViewHandle level = getLevel(dimension, levelName );
		LevelViewHandle level = dimension.findLevel( levelName );
		levelHandle = level.getCubeLevel( );
		return levelHandle;
	}

	private List getMemberValueList(MemberValueHandle parent)
	{
		List list = new ArrayList();
		if ( parent == null  )
		{
			return list;
		}

		MemberValueHandle memberValue = parent;

		while ( true )
		{
			list.add( memberValue );
			if ( memberValue.getContentCount( IMemberValueModel.MEMBER_VALUES_PROP ) != 1
					|| memberValue.getContent( IMemberValueModel.MEMBER_VALUES_PROP,
							0 ) == null )
			{
				break;
			}
			memberValue = (MemberValueHandle) memberValue.getContent( IMemberValueModel.MEMBER_VALUES_PROP,
					0 );
		}
		return list;
	}
}

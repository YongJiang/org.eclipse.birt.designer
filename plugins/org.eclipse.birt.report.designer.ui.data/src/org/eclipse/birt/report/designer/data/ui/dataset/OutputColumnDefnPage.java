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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.birt.report.designer.data.ui.property.AbstractDescriptionPropertyPage;
import org.eclipse.birt.report.designer.data.ui.util.ControlProvider;
import org.eclipse.birt.report.designer.internal.ui.util.ExceptionHandler;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.designer.util.DEUtil;
import org.eclipse.birt.report.model.api.ColumnHintHandle;
import org.eclipse.birt.report.model.api.DataSetHandle;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.PropertyHandle;
import org.eclipse.birt.report.model.api.ResultSetColumnHandle;
import org.eclipse.birt.report.model.api.activity.NotificationEvent;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.eclipse.birt.report.model.api.core.IStructure;
import org.eclipse.birt.report.model.api.core.Listener;
import org.eclipse.birt.report.model.api.elements.DesignChoiceConstants;
import org.eclipse.birt.report.model.api.elements.structures.ColumnHint;
import org.eclipse.birt.report.model.api.elements.structures.ComputedColumn;
import org.eclipse.birt.report.model.api.elements.structures.ResultSetColumn;
import org.eclipse.birt.report.model.api.metadata.IChoice;
import org.eclipse.birt.report.model.api.metadata.PropertyValueException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
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
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

/**
 * Property page to edit script dataset column definition.
 *  
 */
public class OutputColumnDefnPage extends AbstractDescriptionPropertyPage
		implements
			Listener
{

	private boolean modelChanged = true;
	private PropertyHandle rsColumns;
	private PropertyHandle columnHints;
	private ColumnHandles columnHandles;
	private Map rsColumnMap = new HashMap( );
	private Map columnHintMap = new HashMap( );
	
	private OutputColumnTableViewer viewer;
	private static Logger logger = Logger.getLogger( OutputColumnDefnPage.class.getName( ) );

	private static IChoice[] dataTypes = DEUtil.getMetaDataDictionary( )
			.getStructure( ComputedColumn.COMPUTED_COLUMN_STRUCT )
			.getMember( ComputedColumn.DATA_TYPE_MEMBER )
			.getAllowedChoices( )
			.getChoices( ); 

	private static String NAME = "name"; //$NON-NLS-1$
	private static String TYPE = "dataType"; //$NON-NLS-1$
	private static String ALIAS = "alias"; //$NON-NLS-1$
	private static String DISPLAY_NAME = "displayName"; //$NON-NLS-1$
	private static String HELP_TEXT = "helpText"; //$NON-NLS-1$

	private ColumnDefn newDefn = null;

	private static String DEFAULT_MESSAGE = Messages.getString( "dataset.editor.outputColumns" );//$NON-NLS-1$

	private String defaultDataTypeDisplayName;
	private int defaultDataTypeIndex;
	private String[] displayDataTypes;
	
	/**
	 *  
	 */
	public OutputColumnDefnPage( )
	{
		super( );
		this.defaultDataTypeDisplayName = getTypeDisplayName( DesignChoiceConstants.PARAM_TYPE_STRING );
		this.defaultDataTypeIndex = getTypeIndex( DesignChoiceConstants.PARAM_TYPE_STRING );
		this.displayDataTypes = getDataTypeDisplayNames( );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.ui.dialogs.properties.AbstractDescriptionPropertyPage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	public Control createContents( Composite parent )
	{
		rsColumns = ( (DataSetHandle) getContainer( ).getModel( ) ).getPropertyHandle( DataSetHandle.RESULT_SET_HINTS_PROP );
		columnHints = ( (DataSetHandle) getContainer( ).getModel( ) ).getPropertyHandle( DataSetHandle.COLUMN_HINTS_PROP );
		createCachedMap( );
		
		columnHandles = new ColumnHandles( rsColumns, columnHints );
		viewer = new OutputColumnTableViewer( parent, true, true, true );
		TableColumn column = new TableColumn( viewer.getViewer( ).getTable( ),
				SWT.LEFT );
		column.setText( " " ); //$NON-NLS-1$
		column.setResizable( false );
		column.setWidth( 19 );

		column = new TableColumn( viewer.getViewer( ).getTable( ), SWT.LEFT );
		column.setText( Messages.getString( "dataset.editor.title.name" ) ); //$NON-NLS-1$
		column.setWidth( 100 );

		column = new TableColumn( viewer.getViewer( ).getTable( ), SWT.LEFT );
		column.setText( Messages.getString( "dataset.editor.title.type" ) ); //$NON-NLS-1$
		column.setWidth( 100 );

		column = new TableColumn( viewer.getViewer( ).getTable( ), SWT.LEFT );
		column.setText( Messages.getString( "dataset.editor.title.alias" ) ); //$NON-NLS-1$
		column.setWidth( 100 );

		column = new TableColumn( viewer.getViewer( ).getTable( ), SWT.LEFT );
		column.setText( Messages.getString( "dataset.editor.title.displayName" ) ); //$NON-NLS-1$
		column.setWidth( 100 );

		viewer.getViewer( )
				.setContentProvider( new IStructuredContentProvider( ) {

					public Object[] getElements( Object inputElement )
					{
						if ( inputElement == null
								|| !( inputElement instanceof ColumnHandles ) )
							return new Object[0];
						
						return ( ( ColumnHandles )inputElement ).getColumnDefn( ).toArray( );
					}

					public void dispose( )
					{
					}

					public void inputChanged( Viewer viewer, Object oldInput,
							Object newInput )
					{
					}

				} );

		viewer.getViewer( ).setLabelProvider( new ITableLabelProvider( ) {

			public Image getColumnImage( Object element, int columnIndex )
			{
				return null;
			}

			public String getColumnText( Object element, int columnIndex )
			{
				String value = null;
				ColumnDefn defn = null;
				if ( element instanceof ColumnDefn )
				{
					defn = (ColumnDefn) element;
				}
				else
				{
					return ""; //$NON-NLS-1$
				}

				switch ( columnIndex )
				{
					case 1 :
					{
						value = defn.getColumnName( );
						break;
					}
					case 2 :
					{
						if ( defn != newDefn )
							value = getTypeDisplayName( defn.getDataType( ) );
						break;
					}
					case 3 :
					{
						value = defn.getAlias( );
						break;
					}
					case 4 :
					{
						value = defn.getDisplayName( );
						break;
					}
					case 5 :
					{
						value = defn.getHelpText( );
						break;
					}
				}

				if ( value == null )
				{
					value = ""; //$NON-NLS-1$
				}
				return value;
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
		} );
		
		viewer.getViewer( ).setInput( columnHandles );
		viewer.updateButtons( );
        
		( (DataSetHandle) getContainer( ).getModel( ) ).addListener( this );
		return viewer.getControl( );
	}
	
	private void addNewDefn( ColumnDefn defn ) throws SemanticException
	{
		String name = defn.getColumnName( );
		if ( rsColumnMap != null )
		{
			if ( rsColumnMap.get( name ) != null )
			{
				name = getUniqueName( );
				defn.setColumnName( name );
			}
			ResultSetColumnHandle rsHandle;
			if ( rsColumns != null && columnHints != null )
			{
				rsColumns.addItem( defn.getResultSetColumn( ) );
				columnHints.addItem( defn.getColumnHint( ) );

				rsColumnMap.put( name, defn.getResultSetColumn( ) );
				columnHintMap.put( name, defn.getColumnHint( ) );
			}
		}
	}

	private void updateColumnDefMap( String oldName, ColumnDefn column )
	{
		if ( rsColumnMap != null
				&& oldName != null && rsColumnMap.get( oldName ) != null )
		{
			ResultSetColumn rsColumn = (ResultSetColumn)rsColumnMap.get( oldName );
			String newName = column.getColumnName( );
			if ( !oldName.equals( newName ) )
			{
				rsColumnMap.remove( oldName );
				columnHintMap.remove( oldName );
			}
			rsColumnMap.put( newName, column.getResultSetColumn( ) );
			columnHintMap.put( newName, column.getColumnHint( ) );
			
		}
	}

	private final int getTypeIndex( String dataTypeName )
	{
		for ( int n = 0; n < dataTypes.length; n++ )
		{
			if ( dataTypes[n].getName( ).equals( dataTypeName ) )
			{
				return n;
			}
		}

		return this.defaultDataTypeIndex;
	}

	private final String getTypeString( int index )
	{
		if ( index > -1 && index < dataTypes.length )
		{
			return dataTypes[index].getName( );
		}

		return null;
	}

	private final String getTypeDisplayName( String typeName )
	{
		for ( int n = 0; n < dataTypes.length; n++ )
		{
			if ( dataTypes[n].getName( ).equals( typeName ) )
			{
				return dataTypes[n].getDisplayName( );
			}
		}

		return this.defaultDataTypeDisplayName;
	}

	private String[] getDataTypeDisplayNames( )
	{
		String[] dataTypeDisplayNames = new String[dataTypes.length];
		for ( int i = 0; i < dataTypes.length; i++ )
		{
			dataTypeDisplayNames[i] = dataTypes[i].getDisplayName( );
		}
		return dataTypeDisplayNames;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.ui.dialogs.properties.IPropertyPage#pageActivated()
	 */
	public void pageActivated( )
	{
		getContainer( ).setMessage( DEFAULT_MESSAGE, IMessageProvider.NONE );
		if ( modelChanged )
		{
			modelChanged = false;
			refreshColumns( );
		}
	}

	/**
	 * Refreshes the column list adds any new column retrieved. This
	 * method doesn't clear unused column. It is the users responsibility to
	 * delete individual column through the UI.
	 */
	private void refreshColumns( )
	{
		try
		{
			//Bugzilla#104185
			// It's caused by conflict position data. Position from model is
			// staring with 0 in initialization, but from dte is starting
			// with 1. In global perspective, starting with 1 is wider used, and
			// refreshPosition will reset position starting with 1 to model after page activated
//			DataSetItemModel[] items = ( (DataSetEditorDialog) this.getContainer( ) ).getCurrentItemModel( false,
//					true );
//			if ( items != null )
//			{
//				for ( int i = 0; i < items.length; i++ )
//				{
//					DataSetItemModel dsItem = items[i];
//
//					ColumnDefn defn = null;
//					if ( dsItem.getPosition( ) > 0 )
//					{
//						defn = findColumnByPosition( dsItem.getPosition( ) );
//					}
//					else
//					{
//						defn = findColumnByName( dsItem.getName( ) );
//					}
//					if ( defn == null )
//					{
//						defn = new ColumnDefn( );
//						defn.setDataType( dsItem.getDataTypeName( ) );
//						defn.setDisplayName( dsItem.getDisplayName( ) );
//						defn.setHelpText( dsItem.getHelpText( ) );
//						defn.setAlias( dsItem.getAlias( ) );
//						defn.setColumnName( dsItem.getName( ) );
//
//						if ( defn.getColumnName( ) == null )
//						{
//							defn.setColumnName( getUniqueName( ) );
//						}
//						addNewDefn( defn );
//					}
//				}
//			}
			refreshPositions( );
			viewer.getViewer( ).refresh( );
		}
		catch ( Exception e )
		{
			ExceptionHandler.handle( e );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.model.core.Listener#elementChanged(org.eclipse.birt.report.model.api.DesignElementHandle,
	 *      org.eclipse.birt.report.model.activity.NotificationEvent)
	 */
	public void elementChanged( DesignElementHandle focus, NotificationEvent ev )
	{
		modelChanged = true;
	}

	/**
	 * Re-indexes the parameters starting at 1 from the 1st in the list.
	 */
	protected final void refreshPositions( )
	{
		if ( rsColumns == null )
			return;

		int position = 1;
		Iterator iter = rsColumns.iterator( );
		if ( iter != null && rsColumns.isLocal( ) )
		{
			while ( iter.hasNext( ) )
			{
				ResultSetColumnHandle column = (ResultSetColumnHandle) iter.next( );
				column.setPosition( new Integer( position++ ) );
			}
		}
	}

	protected final String getUniqueName( )
	{
		int n = 1;
		String prefix = "column"; //$NON-NLS-1$
		StringBuffer buf = new StringBuffer( );
		while ( buf.length( ) == 0 )
		{
			buf.append( prefix ).append( n++ );
			if ( rsColumns == null )
				continue;

			Iterator iter = rsColumns.iterator( );
			if ( iter != null )
			{
				while ( iter.hasNext( ) && buf.length( ) > 0 )
				{
					ResultSetColumnHandle column = (ResultSetColumnHandle) iter.next( );
					if ( buf.toString( )
							.equalsIgnoreCase( column.getColumnName( ) ) )
					{
						buf.setLength( 0 );
					}
				}
			}
		}
		return buf.toString( );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.ui.dialogs.properties.AbstractPropertyPage#performCancel()
	 */
	public boolean performCancel( )
	{
		disposeAll( );
		return super.performCancel( );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.ui.dialogs.properties.AbstractPropertyPage#performOk()
	 */
	public boolean performOk( )
	{
		if ( isValid( ) )
		{
			refreshPositions( );
			disposeAll( );
			return super.performOk( );
		}
		else
			return false;
	}

	/**
	 * Check the alias names whether is valid. The invalid situation may be the
	 * same name of alias or the same name between column name and alias name.
	 *  
	 */
	private boolean isValid( )
	{
		boolean validate = true;
		String newColumnNameOrAlias;

		if ( columnHints == null )
		{
			columnHints = ( (DataSetHandle) getContainer( ).getModel( ) ).getPropertyHandle( DataSetHandle.COLUMN_HINTS_PROP );
		}

		Iterator iterator1 = columnHints.iterator( );
		for ( int i = 0; iterator1.hasNext( ) && validate; i++ )
		{
			ColumnHintHandle columnHint = (ColumnHintHandle) iterator1.next( );

			newColumnNameOrAlias = columnHint.getAlias( );
			Iterator iterator2 = columnHints.iterator( );
			if ( newColumnNameOrAlias != null
					&& newColumnNameOrAlias.length( ) > 0 )
			{
				for ( int n = 0; iterator2.hasNext( ); n++ )
				{
					ColumnHintHandle columnHint2 = (ColumnHintHandle) iterator2.next( );
					if ( i == n )
						continue;

					if ( ( columnHint2.getColumnName( ) != null && columnHint2.getColumnName( )
							.equals( newColumnNameOrAlias ) )
							|| ( columnHint2.getAlias( ) != null && columnHint2.getAlias( )
									.equals( newColumnNameOrAlias ) ) )
					{
						validate = false;
						getContainer( ).setMessage( Messages.getFormattedString( "dataset.editor.error.columnOrAliasNameAlreadyUsed", //$NON-NLS-1$
								new Object[]{
										newColumnNameOrAlias,
										n>i?new Integer( i + 1 ):new Integer( n + 1 ),
										n>i?new Integer( n + 1 ):new Integer( i + 1 )
								} ),
								IMessageProvider.ERROR );
						break;
					}
				}
			}
		}
		return validate;
	}
	
	private void updateMessage( )
	{
		if ( isValid( ) )
			getContainer( ).setMessage( Messages.getString( "dataset.editor.outputColumns" ), //$NON-NLS-1$
					IMessageProvider.NONE );
	}
	
	private void disposeAll( )
	{
		rsColumnMap = null;
		columnHintMap = null;
	//	selectorImage.dispose( );
		( (DataSetHandle) getContainer( ).getModel( ) ).removeListener( this );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.ui.dialogs.properties.AbstractDescriptionPropertyPage#getPageDescription()
	 */
	public String getPageDescription( )
	{
		return Messages.getString( "OutputColumnDefnPage.description" ); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.ui.dialogs.properties.AbstractPropertyPage#canLeave()
	 */
	public boolean canLeave( )
	{
		if ( isValid( ) )
		{
			refreshPositions( );
			return super.canLeave( );
		}
		else
			return false;
	}
	
	private void refreshCachedMap( )
	{
		columnHintMap.clear( );
		rsColumnMap.clear( );

		for ( Iterator iterator = columnHints.iterator( ); iterator.hasNext( ); )
		{
			ColumnHintHandle handle = (ColumnHintHandle) iterator.next( );
			columnHintMap.put( handle.getColumnName( ), handle.getStructure( ) );
		}

		IStructure toDelete = null;
		if ( rsColumns == null )
			return;

		for ( Iterator iterator = rsColumns.iterator( ); iterator.hasNext( ); )
		{
			ResultSetColumnHandle handle = (ResultSetColumnHandle) iterator.next( );
			if ( columnHintMap.get( handle.getColumnName( ) ) == null )
			{
				toDelete = handle.getStructure( );
				continue;
			}
			rsColumnMap.put( handle.getColumnName( ), handle.getStructure( ) );
		}

		if ( toDelete != null )
		{
			try
			{
				rsColumns.removeItem( toDelete );
			}
			catch ( PropertyValueException e )
			{
				ExceptionHandler.handle( e );
			}
		}
		
	}

	/**
	 * 
	 * 
	 */
	private void createCachedMap( )
	{
		if ( rsColumns == null )
			return;
		
		for ( Iterator iterator = rsColumns.iterator( ); iterator.hasNext( ); )
		{
			ResultSetColumnHandle handle = (ResultSetColumnHandle) iterator.next( );
			rsColumnMap.put( handle.getColumnName( ), handle.getStructure( ) );
		}
		for ( Iterator iterator = columnHints.iterator( ); iterator.hasNext( ); )
		{
			ColumnHintHandle handle = (ColumnHintHandle) iterator.next( );
			columnHintMap.put( handle.getColumnName( ), handle.getStructure( ) );
		}
	}
	
	/**
	 * A class that contain one ResultSetColumnHandle and one ColumnHintHandle.
	 * @author lzhu
	 *
	 */
	private class ColumnHandles
	{
		private PropertyHandle rsColumnHandle;
		private PropertyHandle chHandle;
		private List colList = null;
		
		ColumnHandles( PropertyHandle rsch, PropertyHandle chh )
		{
			this.rsColumnHandle = rsch;
			this.chHandle = chh;
		}
		public PropertyHandle getResultSetColumnHandle( )
		{
			return this.rsColumnHandle;
		}
		public PropertyHandle getColumnHintHandle( )
		{
			return this.chHandle;
		}
		
		public List getColumnDefn( )
		{
			colList = new ArrayList( );
			Iterator rsIter = this.rsColumnHandle.iterator( );
			Iterator hintIter = this.chHandle.iterator( );
			while( rsIter.hasNext( ) )
			{
				colList.add( new ColumnDefn( (ResultSetColumnHandle) rsIter.next( ),
						(ColumnHintHandle) hintIter.next( ) ) );
			}
			return colList;
		}
		
		public int size( )
		{
			if( colList== null )
			{
				getColumnDefn( );
			}
			return this.colList.size( );	
		}
	}
	/**
	 * The class which serves as input data of one single table item in column definition table.
	 * @author lzhu
	 *
	 */
	private class ColumnDefn
	{

		private ResultSetColumnHandle rsColumnHandle;
		private ColumnHintHandle columnHintHandle;
		
		private ResultSetColumn rsColumn;
		private ColumnHint columnHint;
		
		public ColumnDefn(){
			rsColumn = new ResultSetColumn();
			columnHint = new ColumnHint();
			//default type is "string"
			this.setDataType( DesignChoiceConstants.PARAM_TYPE_STRING );
		}
				
		public ColumnDefn( ResultSetColumnHandle rsHandle, ColumnHintHandle colHintHandle )
		{
			this.rsColumnHandle = rsHandle;
			this.columnHintHandle = colHintHandle;
		}
		
		public ResultSetColumn getResultSetColumn( )
		{
			if(this.rsColumnHandle!=null)
				return (ResultSetColumn)this.rsColumnHandle.getStructure();
			else
				return this.rsColumn;
		}

		public ColumnHint getColumnHint( )
		{
			if (this.columnHintHandle!=null)
				return (ColumnHint)this.columnHintHandle.getStructure();
			else
				return this.columnHint;
		}

		public String getColumnName( )
		{
			if ( this.rsColumnHandle!= null)
				return this.rsColumnHandle.getColumnName();
			else if(this.rsColumn != null)
				return this.rsColumn.getColumnName();
			return null;
		}

		/**
		 * @param columnName
		 *            The columnName to set.
		 */
		public void setColumnName( String columnName )
		{
			if ( this.rsColumnHandle != null && this.columnHintHandle != null )
			{
				try
				{
					rsColumnHandle.setColumnName( columnName );
					columnHintHandle.setColumnName( columnName );
				}
				catch ( SemanticException e )
				{
				}
			}
			else if ( this.rsColumn != null && this.columnHint != null )
			{
				rsColumn.setColumnName( columnName );
				columnHint.setProperty( ColumnHint.COLUMN_NAME_MEMBER,
						columnName );
			}
		}

		/**
		 * @return Returns the alias.
		 */
		public String getAlias( )
		{
			if ( this.columnHintHandle!= null)
				return columnHintHandle.getAlias();
			else 
				return (String)columnHint.getProperty(null,ColumnHint.ALIAS_MEMBER);
		}

		/**
		 * @param alias
		 *            The alias to set.
		 */
		public void setAlias( String alias )
		{
			if(this.columnHintHandle!=null)
				columnHintHandle.setAlias( alias );
			else
				columnHint.setProperty(ColumnHint.ALIAS_MEMBER, alias );
		}

		/**
		 * @return Returns the dataType.
		 */
		public String getDataType( )
		{
			if(this.rsColumnHandle!=null)
				return this.rsColumnHandle.getDataType();
			else 
				return this.rsColumn.getDataType();
		}

		/**
		 * @param dataType
		 *            The dataType to set.
		 */
		public void setDataType( String dataType )
		{
			try
			{
				if(rsColumnHandle!=null)
					rsColumnHandle.setDataType( dataType );
				else
					rsColumn.setDataType( dataType );
			}
			catch ( SemanticException e )
			{
				logger.log( Level.FINE, e.getMessage( ), e );
			}
		}

		/**
		 * @return Returns the displayName.
		 */
		public String getDisplayName( )
		{
			if(this.columnHintHandle!=null)
				return this.columnHintHandle.getDisplayName();
			else
				return (String) columnHint.getProperty( null,
						ColumnHint.DISPLAY_NAME_MEMBER );
		}

		/**
		 * @param displayName
		 *            The displayName to set.
		 */
		public void setDisplayName( String displayName )
		{
			if(this.columnHintHandle!=null)
				columnHintHandle.setDisplayName(displayName);
			else
				columnHint.setProperty( ColumnHint.DISPLAY_NAME_MEMBER, displayName );
		}

		/**
		 * @return Returns the helpText.
		 */
		public String getHelpText( )
		{
			if(this.columnHintHandle!=null)
				return columnHintHandle.getHelpText();
			else
				return (String) columnHint.getProperty( null,
						ColumnHint.HELP_TEXT_MEMBER );
		}

		/**
		 * @param helpText
		 *            The helpText to set.
		 */
		public void setHelpText( String helpText )
		{
			if(this.columnHintHandle!=null)
				columnHintHandle.setHelpText(helpText);
			else
				columnHint.setProperty( ColumnHint.HELP_TEXT_MEMBER, helpText );
		}

		public void setProperty( Object property, Object value )
		{
			if ( property.equals( NAME ) )
			{
				setColumnName( (String) value );
			}
			else if ( property.equals( TYPE ) )
			{
				setDataType( (String) value );
			}
			else if ( property.equals( ALIAS ) )
			{
				setAlias( (String) value );
			}
			else if ( property.equals( DISPLAY_NAME ) )
			{
				setDisplayName( (String) value );
			}
			else if ( property.equals( HELP_TEXT ) )
			{
				setHelpText( (String) value );
			}
		}

		public Object getProperty( Object property )
		{
			if ( property.equals( NAME ) )
			{
				return getColumnName( );
			}
			else if ( property.equals( TYPE ) )
			{
				return getDataType( );
			}
			else if ( property.equals( ALIAS ) )
			{
				return getAlias( );
			}
			else if ( property.equals( DISPLAY_NAME ) )
			{
				return getDisplayName( );
			}
			else if ( property.equals( HELP_TEXT ) )
			{
				return getHelpText( );
			}
			return null;
		}
	}
	
	private class OutputColumnTableViewer
	{
	    private TableViewer viewer;
	    private Composite mainControl;
	    private Button btnRemove;
	    private Button btnUp;
	    private Button btnDown;
	    private Button btnAdd;
	    private Button btnEdit;
	    private MenuItem itmRemove;
	    private MenuItem itmRemoveAll;
	    private Menu menu;

	    public OutputColumnTableViewer(Composite parent, boolean showMenus, boolean showButtons, boolean enableKeyStrokes)
	    {
	        mainControl = new Composite(parent, SWT.NONE);
	        GridLayout layout = new GridLayout();
	        layout.numColumns = 2;
	        mainControl.setLayout(layout);

	        GridData data = null;
			viewer = new TableViewer( mainControl, SWT.FULL_SELECTION
					| SWT.BORDER );
	        data = new GridData(GridData.FILL_BOTH);
	        viewer.getControl().setLayoutData(data);
	        
	        viewer.getTable( ).setHeaderVisible( true );
	        viewer.getTable( ).setLinesVisible( true );
			viewer.getTable( ).addSelectionListener( new SelectionAdapter( ) {

				public void widgetSelected( SelectionEvent e )
				{
					updateButtons( );
				}
			} );
			viewer.getTable( ).addMouseListener( new MouseAdapter( ) {

				public void mouseDoubleClick( MouseEvent e )
				{
					if ( viewer.getTable( ).getSelectionCount( ) == 1 )
					{
						doEdit( );
					}
				}
			} );


	        if(showButtons)
	        {
	            Composite btnComposite = new Composite(mainControl, SWT.NONE);
	            data = new GridData();
	            data.verticalAlignment = SWT.CENTER;
	            btnComposite.setLayoutData(data);
	            GridLayout btnLayout = new GridLayout();
	            layout.verticalSpacing = 20;
	            btnComposite.setLayout(btnLayout);
	            
				GridData btnData = new GridData( GridData.CENTER );
				btnData.widthHint = 52;
	            
				btnAdd = new Button( btnComposite, SWT.NONE );
				btnAdd.setText( Messages.getString( "ResultSetColumnPage.button.add" ) );
				btnAdd.setLayoutData( btnData );
				btnAdd.setEnabled( true );
				btnAdd.addSelectionListener(new SelectionListener(){

	                public void widgetSelected(SelectionEvent e)
	                {
	                	doNew( );
	                }

					public void widgetDefaultSelected( SelectionEvent arg0 )
					{
					}
					
	            } );

				btnEdit = new Button( btnComposite, SWT.NONE );
				btnEdit.setText( Messages.getString( "ResultSetColumnPage.button.edit" ) );
				btnEdit.setLayoutData( btnData );
				btnEdit.addSelectionListener( new SelectionListener( ) {

					public void widgetSelected( SelectionEvent e )
					{
						doEdit( );
					}

					public void widgetDefaultSelected( SelectionEvent arg0 )
					{
					}

				} );

				btnRemove = new Button( btnComposite, SWT.NONE );
				btnRemove.setText( Messages.getString( "ResultSetColumnPage.button.delete" ) );
				btnRemove.setLayoutData( btnData );
				btnRemove.addSelectionListener( new SelectionListener( ) {

					public void widgetSelected( SelectionEvent e )
					{
						removeSelectedItem( );
					}

					public void widgetDefaultSelected( SelectionEvent e )
					{
					}

				} );
	            
				btnUp = new Button( btnComposite, SWT.NONE );
				btnUp.setText( Messages.getString( "ResultSetColumnPage.button.up" ) );
				btnUp.setLayoutData( btnData );
				btnUp.addSelectionListener( new SelectionListener( ) {

					public void widgetSelected( SelectionEvent e )
					{
						doMoveUp( );
					}

					public void widgetDefaultSelected( SelectionEvent e )
					{
					}

				} );
	            
				btnDown = new Button( btnComposite, SWT.NONE );
				btnDown.setText( Messages.getString( "ResultSetColumnPage.button.down" ) );
				btnDown.setLayoutData( btnData );
				btnDown.addSelectionListener( new SelectionListener( ) {

					public void widgetSelected( SelectionEvent e )
					{
						doMoveDown( );
					}

					public void widgetDefaultSelected( SelectionEvent e )
					{
					}

				} );
	        }
	        
	        if(showMenus)
	        {
	            menu = new Menu(viewer.getTable());
	            menu.addMenuListener(new MenuAdapter(){
	                public void menuShown(MenuEvent e)
	                {
	                    viewer.cancelEditing();
	                }
	            });
	            itmRemove = new MenuItem(menu, SWT.NONE);
	            itmRemove .setText(Messages.getString("PropertyHandleTableViewer.Menu.Remove")); //$NON-NLS-1$
	            itmRemove .addSelectionListener(new SelectionAdapter(){

	                public void widgetSelected(SelectionEvent e)
	                {
	                    removeSelectedItem();
	                }

	            });
	            itmRemoveAll = new MenuItem(menu, SWT.NONE);
	            itmRemoveAll.setText(Messages.getString("PropertyHandleTableViewer.Menu.RemoveAll")); //$NON-NLS-1$
	            itmRemoveAll.addSelectionListener( new SelectionAdapter( ) {

					public void widgetSelected( SelectionEvent e )
					{
						doRemoveAll( );
					}
				} );

	            viewer.getTable().setMenu(menu);
	        }
	        
	        if(enableKeyStrokes)
	        {
	            viewer.getTable().addKeyListener(new KeyListener(){

	                public void keyPressed(KeyEvent e)
	                {
	                	viewer.getTable();
	                }

	                public void keyReleased(KeyEvent e)
	                {
	                    if ( e.keyCode == SWT.DEL )
	                    {
	                        removeSelectedItem();
	                    }
	                }
	                
	            });
	        }
	    }
	    
		/**
		 * Updates the buttons and menu items
		 * on this page are set
		 */
		private void updateButtons( )
		{
			this.itmRemoveAll.setEnabled( viewer.getTable( ).getItemCount( ) > 0 );
			if ( viewer.getTable( ).getSelectionCount( ) == 1 )
			{
				this.btnEdit.setEnabled( true );
				this.btnRemove.setEnabled( true );
				this.itmRemove.setEnabled( true );

				int index = viewer.getTable( ).getSelectionIndex( );
				this.btnUp.setEnabled( index != 0 );
				this.btnDown.setEnabled( index != ( viewer.getTable( )
						.getItemCount( ) - 1 ) );
			}
			else
			{
				this.btnEdit.setEnabled( false );
				this.btnUp.setEnabled( false );
				this.btnRemove.setEnabled( false );
				this.btnDown.setEnabled( false );
				this.itmRemove.setEnabled( false );
			}
		}
		
		private void doNew( )
		{
			ColumnInputDialog inputDialog = new ColumnInputDialog( mainControl.getShell( ),
					Messages.getString( "ResultSetColumnPage.inputDialog.newColumn.title" ),
					new ColumnDefn( ) );
			if( inputDialog.open( ) == Window.OK )
			{
				ColumnDefn newColumn = inputDialog.getColumnDefn( );
				try
				{
					addNewDefn( newColumn );
					viewer.refresh( );
					updateMessage( );
				}
				catch ( SemanticException e )
				{
					getContainer( ).setMessage( Messages.getString( "OutputColumnPage.error.createNewColumn" ), IMessageProvider.ERROR ); //$NON-NLS-1$
					ExceptionHandler.handle( e );
				}
			}
			updateButtons( );
		}

		private void doEdit( )
		{
			int index = viewer.getTable( ).getSelectionIndex( );
			if ( index >= 0 && index < viewer.getTable( ).getItemCount( ) )
			{
				ColumnDefn currentColumn = (ColumnDefn) viewer.getTable( )
						.getItem( index )
						.getData( );
				String oldName = currentColumn.getColumnName( );
				ColumnInputDialog inputDialog = new ColumnInputDialog( mainControl.getShell( ),
						Messages.getString( "ResultSetColumnPage.inputDialog.editColumn.title" ),
						currentColumn );
				if ( inputDialog.open( ) == Window.OK )
				{
					updateColumnDefMap( oldName, inputDialog.getColumnDefn( ) );
					viewer.refresh( );
					updateMessage( );
				}
			}
			else
			{
				getContainer( ).setMessage( Messages.getString( "OutputColumnPage.error.invalidSelection" ), IMessageProvider.ERROR ); //$NON-NLS-1$
			}
			updateButtons( );
		}

	    public TableViewer getViewer()
	    {
	        return viewer;
	    }
	    
	    public Composite getControl()
	    {
	        return mainControl;
	    }
	    	    	    
	    private final void removeSelectedItem()
	    {
	        int index = viewer.getTable( ).getSelectionIndex( );
	        //Do not allow deletion of the last item.
	        if ( index > -1 && index < columnHandles.size( ) )
	        {
	            try
	            {
					if ( rsColumns != null )
						rsColumns.removeItem( index );
					columnHints.removeItem( index );
				}
	            catch ( Exception e1 )
	            {
	                ExceptionHandler.handle( e1 );
	            }
	            viewer.refresh( );
	            viewer.getTable( ).select( index );
	            refreshCachedMap( );
	        }
	        updateButtons( );
	    }

		private void doMoveUp( )
		{
			// Get the current selection and delete that row
			int index = viewer.getTable( ).getSelectionIndex( );
			if ( index - 1 >= 0 && index < columnHandles.size( ) )
			{
				viewer.cancelEditing( );
				try
				{
					if ( rsColumns != null )
						rsColumns.moveItem( index, index - 1 );
					if ( columnHints != null )
						columnHints.moveItem( index, index - 1 );
				}
				catch ( Exception e1 )
				{
					ExceptionHandler.handle( e1 );
				}
				viewer.refresh( );
				viewer.getTable( ).select( index - 1 );
				updateButtons( );
			}
		}

		private void doMoveDown( )
		{
			// Get the current selection and delete that row
			int index = viewer.getTable( ).getSelectionIndex( );

			if ( index > -1 && index < columnHandles.size( ) - 1 )
			{
				viewer.cancelEditing( );
				try
				{
					if ( rsColumns != null )
						rsColumns.moveItem( index, index + 1 );
					if ( columnHints != null )
						columnHints.moveItem( index, index + 1 );

				}
				catch ( Exception e1 )
				{
					ExceptionHandler.handle( e1 );
				}
				viewer.refresh( );
				viewer.getTable( ).select( index + 1 );
				updateButtons( );
			}
		}

		private void doRemoveAll( )
		{
			try
			{
				if ( rsColumns != null )
					rsColumns.clearValue( );
				columnHints.clearValue( );
				viewer.refresh( );
			}
			catch ( Exception e1 )
			{
				logger.log( Level.FINE, e1.getMessage( ), e1 );
			}
			columnHintMap.clear( );
			rsColumnMap.clear( );
			updateButtons( );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.birt.report.designer.ui.dialogs.properties.IPropertyPage#getToolTip()
	 */
	public String getToolTip( )
	{
		return Messages.getString("OutputColumnDefnPage.OutputColumns.Tooltip"); //$NON-NLS-1$
	}
	
	private class ColumnInputDialog extends PropertyHandleInputDialog
	{
		
		private String title;
		private ColumnDefn columnDefn;
		
		private String columnName, alias, displayName;
		private int dataType;
		private String EMPTY_STRING = "";
		
		public ColumnInputDialog( Shell shell, String title, ColumnDefn columnModel )
		{
			super( shell );
			this.title = title;
			this.columnDefn = columnModel;
			initColumnInfos( );
		}

		protected void createCustomControls( Composite parent )
		{
			Composite composite = new Composite( parent, SWT.NONE );
			GridLayout layout = new GridLayout( );
			layout.numColumns = 2;
			layout.marginTop = 5;
			composite.setLayout( layout );
			GridData layoutData = new GridData( GridData.FILL_BOTH );
			layoutData.widthHint = 320;
			layoutData.heightHint = 200;
			composite.setLayoutData( layoutData );
			
			createDialogContents( composite );
		}

		private void createDialogContents( Composite composite )
		{
			GridData labelData = new GridData( );
			labelData.horizontalSpan = 1;
			
			GridData textData = new GridData( GridData.FILL_HORIZONTAL );
			textData.horizontalSpan = 1;
			
			Label columnNameLabel = new Label( composite, SWT.NONE );
			columnNameLabel.setText( Messages.getString( "ResultSetColumnPage.inputDialog.label.columnName" ) );
			columnNameLabel.setLayoutData( labelData );
			
			final Text columnNameText = new Text( composite, SWT.BORDER );
			columnNameText.setLayoutData( textData );
			columnNameText.setText( columnName );
			columnNameText.addModifyListener( new ModifyListener( ) {

				public void modifyText( ModifyEvent e )
				{
					columnName = columnNameText.getText( ).trim( );
					validateSyntax( );
				}

			} );
			
			Label typeLabel = new Label( composite, SWT.NONE );
			typeLabel.setText( Messages.getString( "ResultSetColumnPage.inputDialog.label.dataType" ) );
			typeLabel.setLayoutData( labelData );			
			
			final Combo typeCombo = ControlProvider.createCombo( composite, SWT.BORDER | SWT.READ_ONLY );
			typeCombo.setItems( displayDataTypes );
			typeCombo.setLayoutData( textData );
			typeCombo.setText( typeCombo.getItem( this.dataType ) );
			
			typeCombo.addSelectionListener( new SelectionListener( ) {

				public void widgetSelected( SelectionEvent e )
				{
					dataType = typeCombo.getSelectionIndex( );
				}

				public void widgetDefaultSelected( SelectionEvent arg0 )
				{
					
				}

			} );
			
			Label aliasLabel = new Label( composite, SWT.NONE );
			aliasLabel.setText( Messages.getString( "ResultSetColumnPage.inputDialog.label.alias" ) );
			aliasLabel.setLayoutData( labelData );
			
			final Text  aliasText = new Text( composite, SWT.BORDER );
			aliasText.setLayoutData( textData );
			aliasText.setText( alias );
			aliasText.addModifyListener( new ModifyListener( ) {

				public void modifyText( ModifyEvent e )
				{
					alias = aliasText.getText( ).trim( );
					validateSyntax( );
				}

			} );
			
			Label displayNameLabel = new Label( composite, SWT.NONE );
			displayNameLabel.setText( Messages.getString( "ResultSetColumnPage.inputDialog.label.displayName" ) );
			displayNameLabel.setLayoutData( labelData );
			
			final Text displayNameText = new Text( composite, SWT.BORDER );
			displayNameText.setLayoutData( textData );
			displayNameText.setText( displayName );
			displayNameText.addModifyListener( new ModifyListener( ) {

				public void modifyText( ModifyEvent e )
				{
					displayName = displayNameText.getText( ).trim( );
				}

			} );
		}
		
		protected boolean isResizable( )
		{
			return true;
		}
		
		protected ColumnDefn getColumnDefn( )
		{
			if( this.columnDefn == null )
			{
				this.columnDefn = new ColumnDefn( );
			}
			this.columnDefn.setColumnName( columnName );
			this.columnDefn.setDataType( getTypeString( dataType ) );
			this.columnDefn.setAlias( alias );
			this.columnDefn.setDisplayName( displayName );
			
			return this.columnDefn;
		}
		
		private void initColumnInfos( )
		{
			if( this.columnDefn != null )
			{
				columnName = resolveNull( this.columnDefn.getColumnName( ) );
				alias = resolveNull( this.columnDefn.getAlias( ) );
				displayName = resolveNull( this.columnDefn.getDisplayName( ) );
				this.dataType = getTypeIndex( this.columnDefn.getDataType( ) );
			}
			else
			{
				columnName = EMPTY_STRING;
				alias = EMPTY_STRING;
				displayName = EMPTY_STRING;
				this.dataType = defaultDataTypeIndex;
			}
		}

		protected void rollback( )
		{
			
		}

		protected IStatus validateSyntax( Object structureOrHandle )
		{
			if ( columnName == null || columnName.trim( ).length( ) == 0 )
			{
				return getMiscStatus( IStatus.ERROR,
						Messages.getString( "ResultSetColumnPage.inputDialog.warning.emptyColumnName" ) );//$NON-NLS-1$ 
			}
			else if ( columnName.equals( alias ) )
			{
				return getMiscStatus( IStatus.ERROR,
						Messages.getString( "ResultSetColumnPage.inputDialog.error.sameValue.columnNameAndAlias" ) );//$NON-NLS-1$ 
			}
			else if ( isDuplicated( columnName ) )
			{
				return getMiscStatus( IStatus.ERROR,
						Messages.getFormattedString( "ResultSetColumnPage.inputDialog.error.duplicatedColumnName",
								new Object[]{
									columnName
								} ) );//$NON-NLS-1$ 
			}
			else if ( isDuplicated( alias ) )
			{
				return getMiscStatus( IStatus.ERROR,
						Messages.getFormattedString( "ResultSetColumnPage.inputDialog.error.duplicatedAlias",
								new Object[]{
									alias
								} ) );//$NON-NLS-1$ 
			}
			return getOKStatus( );
		}
		
		protected IStatus validateSemantics( Object structureOrHandle )
		{
			return validateSyntax( structureOrHandle );
		}

		protected String getTitle( )
		{
			return title;
		}

		private String resolveNull( String value )
		{
			return value == null ? EMPTY_STRING : value.trim( );
		}
				
		private boolean isDuplicated( String newName )
		{
			if( newName == null || newName.trim( ).length( ) == 0 )
			{
				return false;
			}
			Iterator iter = columnHintMap.keySet( ).iterator( );
			while ( iter.hasNext( ) )
			{
				Object value = columnHintMap.get( iter.next( ) );
				if ( value instanceof ColumnHint )
				{
					ColumnHint column = (ColumnHint) value;
					if ( !column.equals( this.columnDefn.getColumnHint( ) ) )
					{
						if ( newName.equals( column.getProperty( null,
								ColumnHint.ALIAS_MEMBER ) )
								|| newName.equals( column.getProperty( null,
										ColumnHint.COLUMN_NAME_MEMBER ) ) )
							return true;
					}
				}
			}
			return false;
		}
		
	}

}
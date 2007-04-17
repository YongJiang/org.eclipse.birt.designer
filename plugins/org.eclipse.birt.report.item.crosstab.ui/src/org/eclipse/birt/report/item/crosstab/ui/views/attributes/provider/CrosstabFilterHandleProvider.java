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

package org.eclipse.birt.report.item.crosstab.ui.views.attributes.provider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.birt.report.designer.internal.ui.views.attributes.provider.AbstractFormHandleProvider;
import org.eclipse.birt.report.item.crosstab.core.ILevelViewConstants;
import org.eclipse.birt.report.item.crosstab.ui.i18n.Messages;
import org.eclipse.birt.report.model.api.ListingHandle;
import org.eclipse.birt.report.model.api.ReportItemHandle;
import org.eclipse.birt.report.model.api.activity.NotificationEvent;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.eclipse.birt.report.model.api.command.NameException;
import org.eclipse.birt.report.model.api.command.PropertyEvent;
import org.eclipse.birt.report.model.api.elements.structures.FilterCondition;
import org.eclipse.birt.report.model.api.metadata.PropertyValueException;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Table;


/**
 * 
 */

public class CrosstabFilterHandleProvider extends AbstractFormHandleProvider
{
	/**
	 * The current selections in outline or Editor.
	 */
	private List input;

	/**
	 * Column properties.
	 */
	private String[] columnKeys = new String[]{
			ILevelViewConstants.LEVEL_PROP,
			FilterCondition.EXPR_MEMBER,
			FilterCondition.OPERATOR_MEMBER,
			FilterCondition.VALUE1_MEMBER,
			FilterCondition.VALUE2_MEMBER
	};

	/**
	 * Column widths.
	 */
	private static int[] columnWidth = new int[]{
			220,170, 120, 170, 170
	};

	/**
	 * Model processor, provide data process of Filter model.
	 */
	private CrosstabFilterModelProvider modelAdapter = new CrosstabFilterModelProvider( );

	/**
	 * The display name of columns.
	 */
	private String[] columnNames;

	/**
	 * Column editors for the Filter form.
	 */
	private CellEditor[] editors;




	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.views.attributes.page.IFormHandleProvider#getColumnNames()
	 */
	public String[] getColumnNames( )
	{
		if ( columnNames == null )
		{
			columnNames = modelAdapter.getColumnNames( columnKeys );
		}
		return columnNames;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.views.attributes.page.IFormHandleProvider#getTitle()
	 */
	public String getDisplayName( )
	{
		return Messages.getString( "FilterHandleProvider.Label.Filterby" ); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.views.attributes.page.IFormHandleProvider#getEditors(org.eclipse.swt.widgets.Table)
	 */
	public CellEditor[] getEditors( final Table table )
	{
		if ( editors == null )
		{
			editors = new CellEditor[columnKeys.length];
			editors[0] = new TextCellEditor( table );
			editors[1] = new TextCellEditor( table );
			editors[2] = new TextCellEditor( table );
			editors[3] = new TextCellEditor( table );
			editors[4] = new TextCellEditor( table );
		}

		return editors;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.views.attributes.page.IFormHandleProvider#doMoveItem(int,
	 *      int)
	 */
	public boolean doMoveItem( int oldPos, int newPos )
			throws PropertyValueException
	{
		// can not move
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.views.attributes.page.IFormHandleProvider#doDeleteItem(int)
	 */
	public boolean doDeleteItem( int pos ) throws PropertyValueException
	{
		return modelAdapter.deleteItem( input.get( 0 ), pos );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.views.attributes.page.IFormHandleProvider#doAddItem(int)
	 */
	public boolean doAddItem( int pos ) throws SemanticException
	{
		return modelAdapter.doAddItem( input.get( 0 ), pos );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.views.attributes.page.IFormHandleProvider#doEditItem(int)
	 */
	public boolean doEditItem( int pos )
	{
		return modelAdapter.doEditItem( input.get( 0 ), pos );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.views.attributes.page.IFormHandleProvider#getColumnText(java.lang.Object,
	 *      int)
	 */
	public String getColumnText( Object element, int columnIndex )
	{
		String key = columnKeys[columnIndex];
		return modelAdapter.getText( element, key );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.views.attributes.page.IFormHandleProvider#getImagePath(java.lang.Object,
	 *      int)
	 */
	public String getImagePath( Object element, int columnIndex )
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.views.attributes.page.IFormHandleProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements( Object inputElement )
	{
		if ( inputElement instanceof List )
		{
			input = (List) inputElement;
		}
		else
		{
			input = new ArrayList( );
			input.add( inputElement );
		}
//		getDataSetColumns( input.get( 0 ) );
		Object[] elements = modelAdapter.getElements( input );
		return elements;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.views.attributes.page.IFormHandleProvider#canModify(java.lang.Object,
	 *      java.lang.String)
	 */
	public boolean canModify( Object element, String property )
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.views.attributes.page.IFormHandleProvider#getValue(java.lang.Object,
	 *      java.lang.String)
	 */
	public Object getValue( Object element, String property )
	{
		int index = Arrays.asList( columnNames ).indexOf( property );
		String columnText = getColumnText( element, index );

		return columnText;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.views.attributes.page.IFormHandleProvider#modify(java.lang.Object,
	 *      java.lang.String, java.lang.Object)
	 */
	public boolean modify( Object data, String property, Object value )
			throws NameException, SemanticException
	{
	
		return false;

	}



	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.views.attributes.page.IFormHandleProvider#getColumnWidths()
	 */
	public int[] getColumnWidths( )
	{
		return columnWidth;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.views.attributes.page.IFormHandleProvider#needRefreshed(org.eclipse.birt.model.activity.NotificationEvent)
	 */
	public boolean needRefreshed( NotificationEvent event )
	{
		if ( event instanceof PropertyEvent )
		{
			String propertyName = ( (PropertyEvent) event ).getPropertyName( );
//			if ( ReportItemHandle.BOUND_DATA_COLUMNS_PROP.equals( propertyName ) )
//			{
//				getDataSetColumns( input.get( 0 ) );
//			}
			if ( ListingHandle.FILTER_PROP.equals( propertyName ) )
			{
				return true;
			}
			if ( ReportItemHandle.PARAM_BINDINGS_PROP.equals( propertyName ) )
			{
				return true;
			}
			
			if(ILevelViewConstants.LEVEL_PROP.equals( propertyName ))
			{
				return true;
			}
		}
		return false;
	}


//	public void updateBindingParameters( )
//	{
//		ParamBindingHandle[] bindingParams = null;
//
//		if ( DEUtil.getInputFirstElement( input ) instanceof ReportItemHandle )
//		{
//			ReportItemHandle inputHandle = (ReportItemHandle) DEUtil.getInputFirstElement( input );
//			List list = new ArrayList( );
//			for ( Iterator iterator = inputHandle.paramBindingsIterator( ); iterator.hasNext( ); )
//			{
//				ParamBindingHandle handle = (ParamBindingHandle) iterator.next( );
//				list.add( handle );
//			}
//			bindingParams = new ParamBindingHandle[list.size( )];
//			list.toArray( bindingParams );
//		}
////		setBindingParams( bindingParams );
//	}
}

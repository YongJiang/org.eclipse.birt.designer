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

package org.eclipse.birt.report.designer.internal.ui.views.dialogs.provider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.designer.ui.views.attributes.providers.SortingModelProvider;
import org.eclipse.birt.report.model.api.ListingHandle;
import org.eclipse.birt.report.model.api.activity.NotificationEvent;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.eclipse.birt.report.model.api.command.NameException;
import org.eclipse.birt.report.model.api.command.PropertyEvent;
import org.eclipse.birt.report.model.api.elements.structures.SortKey;
import org.eclipse.birt.report.model.api.metadata.PropertyValueException;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Table;

/**
 * Sorting FormHandleProvider, provides Sorting sensitive data and processes.
 */
public class SortingHandleProvider implements IFormHandleProvider
{

	/**
	 * The current selections in outline or Editor.
	 */
	private List input;

	/**
	 * Column properties.
	 */
	private String[] columnKeys = new String[]{
			SortKey.KEY_MEMBER, SortKey.DIRECTION_MEMBER
	};

	/**
	 * Column widths.
	 */
	private static int[] columnWidth = new int[]{
			250, 250
	};

	/**
	 * Model processor, provide data process of Sorting model.
	 */
	private SortingModelProvider modelAdapter = new SortingModelProvider( );

	/**
	 * The display name of columns.
	 */
	private String[] columnNames;

	/**
	 * Column editors for the Sorting form.
	 */
	private CellEditor[] editors;

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.birt.report.designer.internal.ui.views.attributes.page.
	 * IFormHandleProvider#getColumnNames()
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
	 * @seeorg.eclipse.birt.report.designer.internal.ui.views.attributes.page.
	 * IFormHandleProvider#getTitle()
	 */
	public String getTitle( )
	{
		return Messages.getString( "SortingHandleProvider.Label.SortOn" ); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.birt.report.designer.internal.ui.views.attributes.page.
	 * IFormHandleProvider#getEditors(org.eclipse.swt.widgets.Table)
	 */
	public CellEditor[] getEditors( Table table )
	{
		if ( editors == null )
		{
			editors = new CellEditor[columnKeys.length];
			editors[0] = new TextCellEditor( table );
			editors[1] = new TextCellEditor( table );
		}
		return editors;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.birt.report.designer.internal.ui.views.attributes.page.
	 * IFormHandleProvider#doMoveItem(int, int)
	 */
	public boolean doMoveItem( int oldPos, int newPos )
			throws PropertyValueException
	{
		return modelAdapter.moveItem( input.get( 0 ), oldPos, newPos );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.birt.report.designer.internal.ui.views.attributes.page.
	 * IFormHandleProvider#doDeleteItem(int)
	 */
	public boolean doDeleteItem( int pos ) throws PropertyValueException
	{
		return modelAdapter.deleteItem( input.get( 0 ), pos );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.birt.report.designer.internal.ui.views.attributes.page.
	 * IFormHandleProvider#doAddItem(int)
	 */
	public boolean doAddItem( int pos ) throws SemanticException
	{
		return modelAdapter.doAddItem( input.get( 0 ), pos );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.birt.report.designer.internal.ui.views.attributes.page.
	 * IFormHandleProvider#doEditItem(int)
	 */
	public boolean doEditItem( int pos )
	{
		return modelAdapter.doEditItem( input.get( 0 ), pos );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.birt.report.designer.internal.ui.views.attributes.page.
	 * IFormHandleProvider#getColumnText(java.lang.Object, int)
	 */
	public String getColumnText( Object element, int columnIndex )
	{
		String key = columnKeys[columnIndex];
		return modelAdapter.getText( element, key );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.birt.report.designer.internal.ui.views.attributes.page.
	 * IFormHandleProvider#getImagePath(java.lang.Object, int)
	 */
	public String getImagePath( Object element, int columnIndex )
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.birt.report.designer.internal.ui.views.attributes.page.
	 * IFormHandleProvider#getElements(java.lang.Object)
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
		Object[] elements = modelAdapter.getElements( input );
		return elements;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.birt.report.designer.internal.ui.views.attributes.page.
	 * IFormHandleProvider#canModify(java.lang.Object, java.lang.String)
	 */
	public boolean canModify( Object element, String property )
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.birt.report.designer.internal.ui.views.attributes.page.
	 * IFormHandleProvider#getValue(java.lang.Object, java.lang.String)
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
	 * @seeorg.eclipse.birt.report.designer.internal.ui.views.attributes.page.
	 * IFormHandleProvider#modify(java.lang.Object, java.lang.String,
	 * java.lang.Object)
	 */
	public boolean modify( Object data, String property, Object value )
			throws NameException, SemanticException
	{
		int index = Arrays.asList( columnNames ).indexOf( property );
		String key = columnKeys[index];

		String strValue;
		if ( value instanceof Integer )
		{
			int intValue = ( (Integer) value ).intValue( );
			if ( intValue == -1 )
			{
				CCombo combo = (CCombo) editors[index].getControl( );
				strValue = combo.getText( );
			}
			else
			{
				String[] choices = modelAdapter.getChoiceSet( input.get( 0 ),
						columnKeys[index] );
				strValue = choices[intValue];
			}
		}
		else
			strValue = (String) value;
		return modelAdapter.setStringValue( input.get( 0 ), data, key, strValue );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.birt.report.designer.internal.ui.views.attributes.page.
	 * IFormHandleProvider#getColumnWidths()
	 */
	public int[] getColumnWidths( )
	{
		return columnWidth;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.birt.report.designer.internal.ui.views.attributes.page.
	 * IFormHandleProvider
	 * #needRefreshed(org.eclipse.birt.model.activity.NotificationEvent)
	 */
	public boolean needRefreshed( NotificationEvent event )
	{
		if ( event instanceof PropertyEvent )
		{
			return true;
		}
		return false;
	}

	public boolean isEditable( )
	{
		return true;
	}
}
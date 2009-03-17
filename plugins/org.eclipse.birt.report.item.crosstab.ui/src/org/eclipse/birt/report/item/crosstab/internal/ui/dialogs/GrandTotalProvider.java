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
import java.util.Arrays;
import java.util.List;

import org.eclipse.birt.report.item.crosstab.core.ICrosstabConstants;
import org.eclipse.birt.report.item.crosstab.core.de.AggregationCellHandle;
import org.eclipse.birt.report.item.crosstab.core.de.CrosstabReportItemHandle;
import org.eclipse.birt.report.item.crosstab.core.de.DimensionViewHandle;
import org.eclipse.birt.report.item.crosstab.core.de.MeasureViewHandle;
import org.eclipse.birt.report.item.crosstab.core.util.CrosstabUtil;
import org.eclipse.birt.report.item.crosstab.internal.ui.AggregationCellProviderWrapper;
import org.eclipse.birt.report.item.crosstab.internal.ui.dialogs.AggregationDialog.GrandTotalInfo;
import org.eclipse.birt.report.item.crosstab.ui.extension.IAggregationCellViewProvider;
import org.eclipse.birt.report.item.crosstab.ui.extension.SwitchCellInfo;
import org.eclipse.birt.report.item.crosstab.ui.i18n.Messages;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Item;

public class GrandTotalProvider extends TotalProvider implements
		ITableLabelProvider,
		IStructuredContentProvider,
		ICellModifier
{

	private int axis;
	private CellEditor[] cellEditor;
	TableViewer viewer;
	private String[] comboItems = null;
	// private IAggregationCellViewProvider[] providers;
	private String[] viewNames;

	private CrosstabReportItemHandle crosstab;
	private AggregationCellProviderWrapper cellProviderWrapper;

	private void initializeItems( GrandTotalInfo grandTotalInfo )
	{
		List<String> viewNameList = new ArrayList<String>( );
		List<String> itemList = new ArrayList<String>( );

		AggregationCellHandle cell = getAggregationCell( grandTotalInfo );
		if ( cell != null && cellProviderWrapper.getMatchProvider( cell ) == null)
		{
			itemList.add( "" );
			viewNameList.add( "" ); //$NON-NLS-1$
		}

		IAggregationCellViewProvider providers[] = cellProviderWrapper.getAllProviders( );
		for ( int i = 0; i < providers.length; i++ )
		{
			IAggregationCellViewProvider tmp = (IAggregationCellViewProvider) providers[i];
			if ( tmp == null )
			{
				continue;
			}
			SwitchCellInfo info = new SwitchCellInfo( crosstab,
					SwitchCellInfo.GRAND_TOTAL );
			info.setGrandTotalInfo( grandTotalInfo, axis );
			if ( !providers[i].canSwitch( info ) )
			{
				continue;
			}
			String displayName = tmp.getViewDisplayName( );
			viewNameList.add( tmp.getViewName( ) );
			itemList.add( Messages.getString( "GrandTotalProvider.ShowAs", //$NON-NLS-1$
					new String[]{
					displayName
					} ) );
		}
		comboItems = (String[]) itemList.toArray( new String[itemList.size( )] );
		viewNames = (String[]) viewNameList.toArray( new String[viewNameList.size( )] );
	}

	public GrandTotalProvider( TableViewer viewer,
			CrosstabReportItemHandle crosstab, int axis )
	{
		this.viewer = viewer;
		this.crosstab = crosstab;
		this.axis = axis;
		cellProviderWrapper = new AggregationCellProviderWrapper( crosstab );
		// initialization( );
	}

	public String[] getColumnNames( )
	{
		return columnNames;
	}

	public CellEditor[] getCellEditors( )
	{
		if ( cellEditor != null )
		{
			return cellEditor;
		}

		ComboBoxCellEditor comboCell = new ComboBoxCellEditor( viewer.getTable( ),
				new String[0],
				SWT.READ_ONLY );
		cellEditor = new CellEditor[]{
				null, null, comboCell
		};
		return cellEditor;
	}

	// private CellEditor[] editors;
	private String[] columnNames = new String[]{
			"", Messages.getString( "GrandTotalProvider.Column.AggregateOn" ),// Messages.getString("GrandTotalProvider.Column.Function")
																		// //$NON-NLS-1$
																			// //$NON-NLS-2$
			// //$NON-NLS-1$
			// //$NON-NLS-2$
			// //$NON-NLS-3$
			Messages.getString( "GrandTotalProvider.Column.View" ) //$NON-NLS-1$
	};

	public Image getColumnImage( Object element, int columnIndex )
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getColumnText( Object element, int columnIndex )
	{
		GrandTotalInfo info = (GrandTotalInfo) element;
		switch ( columnIndex )
		{
			case 0 :
				return ""; //$NON-NLS-1$
			case 1 :
				return info.getMeasureDisplayName( ) == null ? ""
						: info.getMeasureDisplayName( );
			case 2 :
				initializeItems( info );
				( (ComboBoxCellEditor) cellEditor[2] ).setItems( comboItems );
				String expectedView = info.getExpectedView( );
				if ( expectedView == null )
				{
					expectedView = "";
				}
				int index = Arrays.asList( viewNames ).indexOf( expectedView );
				if ( index <= 0 )
				{
					index = 0;
					info.setExpectedView( viewNames[index] );
				}
				return comboItems[index];
			default :
				break;
		}
		return ""; //$NON-NLS-1$
	}

	public Object[] getElements( Object inputElement )
	{
		if ( inputElement instanceof List )
			return ( (List) inputElement ).toArray( );
		return new Object[]{};

	}

	public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
	{
	}

	public int[] columnWidths( )
	{
		return new int[]{
				20, 210, 120
		};
	}

	public boolean canModify( Object element, String property )
	{
		// TODO Auto-generated method stub
		if ( Arrays.asList( columnNames ).indexOf( property ) == 2 )
		{
			if ( viewer instanceof CheckboxTableViewer )
			{
				return ( (CheckboxTableViewer) viewer ).getChecked( element );
			}
			else
			{
				return true;
			}

		}
		else
		{
			return false;
		}
	}

	public Object getValue( Object element, String property )
	{
		// TODO Auto-generated method stub
		if ( element instanceof Item )
		{
			element = ( (Item) element ).getData( );
		}
		Object value = null;

		int index = Arrays.asList( columnNames ).indexOf( property );
		switch ( index )
		{
			case 1 :
				break;
			case 2 :
				initializeItems( (GrandTotalInfo) element );
				( (ComboBoxCellEditor) cellEditor[2] ).setItems( comboItems );
				String expectedView = ( (GrandTotalInfo) ( element ) ).getExpectedView( );
				if ( expectedView == null || expectedView.length( ) == 0 )
				{
					return Integer.valueOf( 0 );
				}
				int sel = Arrays.asList( viewNames ).indexOf( expectedView );
				value = sel <= 0 ? Integer.valueOf( 0 ) : Integer.valueOf( sel );
				break;
			default :
		}
		return value;
	}

	public void modify( Object element, String property, Object value )
	{
		// TODO Auto-generated method stub
		if ( element instanceof Item )
		{
			element = ( (Item) element ).getData( );
		}

		int index = Arrays.asList( columnNames ).indexOf( property );
		switch ( index )
		{
			case 0 :
				break;
			case 1 :
				break;
			case 2 :
				int sel = ( (Integer) value ).intValue( );
				if ( sel == 0 )
				{
					( (GrandTotalInfo) ( element ) ).setExpectedView( "" ); //$NON-NLS-1$
				}
				else
				{
					( (GrandTotalInfo) element ).setExpectedView( viewNames[sel] );
				}
				break;
			default :
		}
		viewer.refresh( );
	}

	private AggregationCellHandle getAggregationCell(
			GrandTotalInfo grandTotalInfo )
	{
		AggregationCellHandle cell = null;
		// MeasureHandle measure = grandTotalInfo.getMeasure( );
		// if(measure == null)
		// {
		// return cell;
		// }
		MeasureViewHandle measureView = crosstab.getMeasure( grandTotalInfo.getMeasureQualifiedName( ) );
		if ( measureView == null )
		{
			return cell;
		}

		int counterAxisType = CrosstabUtil.getOppositeAxisType( axis );
		DimensionViewHandle counterDimension = crosstab.getDimension( counterAxisType,
				crosstab.getDimensionCount( counterAxisType ) - 1 );
		String counterDimensionName = null;
		String counterLevelName = null;
		if ( counterDimension != null )
		{
			counterDimensionName = counterDimension.getCubeDimensionName( );
			counterLevelName = counterDimension.getLevel( counterDimension.getLevelCount( ) - 1 )
					.getCubeLevelName( );
		}

		String rowDimension = null;
		String rowLevel = null;
		String colDimension = null;
		String colLevel = null;

		if ( axis == ICrosstabConstants.ROW_AXIS_TYPE )
		{
			colDimension = counterDimensionName;
			colLevel = counterLevelName;

		}
		else if ( axis == ICrosstabConstants.COLUMN_AXIS_TYPE )
		{
			rowDimension = counterDimensionName;
			rowLevel = counterLevelName;
		}

		cell = measureView.getAggregationCell( rowDimension,
				rowLevel,
				colDimension,
				colLevel );

		return cell;
	}

}
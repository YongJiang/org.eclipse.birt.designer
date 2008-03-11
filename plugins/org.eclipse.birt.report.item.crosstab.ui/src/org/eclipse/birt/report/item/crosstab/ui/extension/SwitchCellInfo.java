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

package org.eclipse.birt.report.item.crosstab.ui.extension;

import org.eclipse.birt.report.item.crosstab.core.ICrosstabConstants;
import org.eclipse.birt.report.item.crosstab.core.de.AggregationCellHandle;
import org.eclipse.birt.report.item.crosstab.core.de.CrosstabReportItemHandle;
import org.eclipse.birt.report.item.crosstab.core.de.DimensionViewHandle;
import org.eclipse.birt.report.item.crosstab.core.de.LevelViewHandle;
import org.eclipse.birt.report.item.crosstab.core.de.MeasureViewHandle;
import org.eclipse.birt.report.item.crosstab.core.util.CrosstabUtil;
import org.eclipse.birt.report.item.crosstab.internal.ui.dialogs.AggregationDialog;
import org.eclipse.birt.report.item.crosstab.internal.ui.dialogs.ShowSummaryFieldDialog;
import org.eclipse.birt.report.model.api.olap.LevelHandle;
import org.eclipse.birt.report.model.api.olap.MeasureHandle;

/**
 * 
 */

public class SwitchCellInfo
{

	public static final int UNKNOWN = 0;
	public static final int GRAND_TOTAL = 1;
	public static final int SUB_TOTAL = 2;
	public static final int MEASURE = 3;

	private int type;
	private CrosstabReportItemHandle crosstab;
	private AggregationCellHandle cell;
	private GrandTotalInfo grandTotal;
	private SubTotalInfo subTotal;
	private MeasureInfo measureInfo;

	public SwitchCellInfo( CrosstabReportItemHandle crosstab, int type )
	{
		this.crosstab = crosstab;
		this.type = type;
	}

	public CrosstabReportItemHandle getCrosstab( )
	{
		return crosstab;
	}

	public GrandTotalInfo getGrandTotalInfo()
	{
		return grandTotal;
	}
	
	public SubTotalInfo getSubTotalInfo()
	{
		return subTotal;
	}
	
	public MeasureInfo getMeasureInfo()
	{
		return measureInfo;
	}
	
	public AggregationCellHandle getAggregationCell( )
	{
		if ( cell != null )
		{
			return cell;
		}
		switch ( type )
		{
			case GRAND_TOTAL :
				cell = getCellFromGrandTotal( );
				break;
			case SUB_TOTAL :
				cell = getCellFromSubTotal( );
				break;
			case MEASURE :
				cell = getCellFromMeasure( );
				break;
			default :
		}
		return cell;
	}

	private AggregationCellHandle getCellFromMeasure( )
	{
		AggregationCellHandle cell = null;
		if(measureInfo == null)
		{
			return cell;
		}
		MeasureHandle measure = measureInfo.measure;
		if ( measure == null )
		{
			return cell;
		}
		MeasureViewHandle measureView = crosstab.getMeasure( measure.getQualifiedName( ) );
		if ( measureView == null )
		{
			return cell;
		}

		cell = measureView.getCell( );
		return cell;
	}

	private AggregationCellHandle getCellFromSubTotal( )
	{

		AggregationCellHandle cell = null;
		if(subTotal == null)
		{
			return cell;
		}
		MeasureHandle measure = subTotal.measure;
		LevelHandle level = subTotal.level;
		if ( measure == null || level == null )
		{
			return cell;
		}
		MeasureViewHandle measureView = crosstab.getMeasure( measure.getQualifiedName( ) );
		LevelViewHandle levelView = findLevelViewHandle( level );
		if ( measureView == null || levelView == null )
		{
			return cell;
		}

		String rowDimension = null;
		String rowLevel = null;
		String colDimension = null;
		String colLevel = null;

		int axisType = levelView.getAxisType( );

		int counterAxisType = CrosstabUtil.getOppositeAxisType( levelView.getAxisType( ) );
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

		String dimensionName = ( (DimensionViewHandle) levelView.getContainer( ) ).getCubeDimensionName( );
		String levelName = levelView.getCubeLevelName( );
		if ( levelName == null || dimensionName == null )
			return cell;

		if ( axisType == ICrosstabConstants.ROW_AXIS_TYPE )
		{
			rowDimension = dimensionName;
			rowLevel = levelName;
			colDimension = counterDimensionName;
			colLevel = counterLevelName;
		}
		else if ( axisType == ICrosstabConstants.COLUMN_AXIS_TYPE )
		{
			colDimension = dimensionName;
			colLevel = levelName;
			rowDimension = counterDimensionName;
			rowLevel = counterLevelName;
		}

		cell = measureView.getAggregationCell( rowDimension,
				rowLevel,
				colDimension,
				colLevel );

		return cell;

	}

	private AggregationCellHandle getCellFromGrandTotal( )
	{
		AggregationCellHandle cell = null;
		if(grandTotal == null)
		{
			return cell;
		}
		MeasureHandle measure = grandTotal.measure;
		if ( measure == null )
		{
			return cell;
		}
		MeasureViewHandle measureView = crosstab.getMeasure( measure.getQualifiedName( ) );
		if ( measureView == null )
		{
			return cell;
		}

		int counterAxisType = CrosstabUtil.getOppositeAxisType( grandTotal.axis );
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

		if ( grandTotal.axis == ICrosstabConstants.ROW_AXIS_TYPE )
		{
			colDimension = counterDimensionName;
			colLevel = counterLevelName;

		}
		else if ( grandTotal.axis == ICrosstabConstants.COLUMN_AXIS_TYPE )
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

	public void setGrandTotalInfo(
			AggregationDialog.GrandTotalInfo grandTotalIn, int axis )
	{
		grandTotal = new GrandTotalInfo( grandTotalIn, axis );
	}

	public void setSubTotalInfo( AggregationDialog.SubTotalInfo subTotalIn )
	{
		subTotal = new SubTotalInfo( subTotalIn );
	}

	public void setMeasureInfo( ShowSummaryFieldDialog.MeasureInfo measureInfoIn )
	{
		measureInfo = new MeasureInfo( measureInfoIn );
	}

	/**
	 * GrandTotalInfo
	 */
	public class GrandTotalInfo
	{

		private MeasureHandle measure;
		private boolean aggregationOn = false;
		private String function = ""; //$NON-NLS-1$
		private boolean isAssociation = false;
		private int axis;

		public GrandTotalInfo( AggregationDialog.GrandTotalInfo grandTotalIn,
				int axis )
		{
			this.aggregationOn = grandTotalIn.isAggregationOn( );
			this.function = new String( grandTotalIn.getFunction( ) );
			this.measure = grandTotalIn.getMeasure( );
			this.isAssociation = grandTotalIn.isAssociation( );

			this.axis = axis;
		}
		public MeasureHandle getMeasure()
		{
			return this.measure;
		}
		
		public boolean isAggregationOn()
		{
			return this.aggregationOn;
		}
		
		public boolean isAssociation()
		{
			return this.isAssociation;
		}
		
		public String getFunction()
		{
			return new String(this.function);
		}
		
		public int getAxisType()
		{
			return this.axis;
		}
	}

	/**
	 * SubTotalInfo
	 */
	public class SubTotalInfo
	{

		private LevelHandle level;
		private MeasureHandle measure;
		private boolean aggregationOn = false;
		private boolean isAssociation = false;
		private String function = ""; //$NON-NLS-1$

		public SubTotalInfo( AggregationDialog.SubTotalInfo subTotalIn )
		{
			this.measure = subTotalIn.getAggregateOnMeasure( );
			this.aggregationOn = subTotalIn.isAggregationOn( );
			this.function = new String( subTotalIn.getFunction( ) );
			this.level = subTotalIn.getLevel( );
			this.isAssociation = subTotalIn.isAssociation( );
		}
		
		public MeasureHandle getAggregateOnMeasure(  )
		{
			return this.measure;
		}		
		
		public LevelHandle getLevelHande()
		{
			return this.level;
		}
		public boolean isAggregation()
		{
			return this.aggregationOn;
		}
		
		public boolean isAssociation()
		{
			return this.isAssociation;
		}
		
		public String getFunction()
		{
			return new String(this.function);
		}
	}

	/**
	 * GrandTotalInfo
	 */
	public class MeasureInfo
	{

		private MeasureHandle measure;
		private boolean isShow = false;

		public MeasureInfo( ShowSummaryFieldDialog.MeasureInfo measureInfoIn )
		{
			this.isShow = measureInfoIn.isShow( );
			this.measure = measureInfoIn.getMeasure( );
		}
		
		public MeasureHandle getMeasureHandle()
		{
			return this.measure;
		}
		
		public boolean isShow()
		{
			return this.isShow;
		}

	}

	public int getType( )
	{
		return type;
	}

	private LevelViewHandle findLevelViewHandle( LevelHandle handle )
	{

		int dimCount = crosstab.getDimensionCount( ICrosstabConstants.ROW_AXIS_TYPE );
		for ( int i = 0; i < dimCount; i++ )
		{
			DimensionViewHandle tmpDimView = crosstab.getDimension( ICrosstabConstants.ROW_AXIS_TYPE,
					i );
			LevelViewHandle levelView = tmpDimView.getLevel( handle.getQualifiedName( ) );
			if ( levelView != null )
			{
				return levelView;
			}
		}

		dimCount = crosstab.getDimensionCount( ICrosstabConstants.COLUMN_AXIS_TYPE );
		for ( int i = 0; i < dimCount; i++ )
		{
			DimensionViewHandle tmpDimView = crosstab.getDimension( ICrosstabConstants.COLUMN_AXIS_TYPE,
					i );
			LevelViewHandle levelView = tmpDimView.getLevel( handle.getQualifiedName( ) );
			if ( levelView != null )
			{
				return levelView;
			}
		}

		return null;
	}

}

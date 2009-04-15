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

package org.eclipse.birt.report.item.crosstab.core.de.internal;

import java.util.List;
import java.util.logging.Level;

import org.eclipse.birt.report.item.crosstab.core.CrosstabException;
import org.eclipse.birt.report.item.crosstab.core.de.CrosstabCellHandle;
import org.eclipse.birt.report.item.crosstab.core.de.CrosstabReportItemHandle;
import org.eclipse.birt.report.item.crosstab.core.de.CrosstabViewHandle;
import org.eclipse.birt.report.item.crosstab.core.de.DimensionViewHandle;
import org.eclipse.birt.report.item.crosstab.core.de.LevelViewHandle;
import org.eclipse.birt.report.item.crosstab.core.de.MeasureViewHandle;
import org.eclipse.birt.report.item.crosstab.core.i18n.MessageConstants;
import org.eclipse.birt.report.item.crosstab.core.i18n.Messages;
import org.eclipse.birt.report.item.crosstab.core.util.CrosstabExtendedItemFactory;
import org.eclipse.birt.report.item.crosstab.core.util.CrosstabUtil;
import org.eclipse.birt.report.model.api.CommandStack;
import org.eclipse.birt.report.model.api.ExtendedItemHandle;
import org.eclipse.birt.report.model.api.PropertyHandle;
import org.eclipse.birt.report.model.api.activity.SemanticException;

/**
 * CrosstabViewTask
 */
public class CrosstabViewTask extends AbstractCrosstabModelTask
{

	protected CrosstabViewHandle crosstabView = null;

	/**
	 * 
	 * @param focus
	 */
	public CrosstabViewTask( CrosstabViewHandle focus )
	{
		super( focus );
		crosstabView = focus;
	}

	/**
	 * 
	 * @param measureList
	 * @param functionList
	 * @return
	 * @throws SemanticException
	 */
	public CrosstabCellHandle addGrandTotal(
			List<MeasureViewHandle> measureList, List<String> functionList )
			throws SemanticException
	{
		return addGrandTotal( measureList, functionList, true );
	}

	CrosstabCellHandle addGrandTotal( List<MeasureViewHandle> measureList,
			List<String> functionList, boolean needTransaction )
			throws SemanticException
	{
		if ( !isValidParameters( functionList, measureList ) )
			return null;

		verifyTotalMeasureFunctions( crosstabView.getAxisType( ),
				functionList,
				measureList );

		PropertyHandle propHandle = crosstabView.getGrandTotalProperty( );

		CommandStack stack = null;

		if ( needTransaction )
		{
			stack = crosstabView.getCommandStack( );
			stack.startTrans( Messages.getString( "CrosstabViewTask.msg.add.grandtotal" ) ); //$NON-NLS-1$
		}

		CrosstabCellHandle totalCell = null;

		try
		{
			ExtendedItemHandle grandTotal = null;
			if ( propHandle.getContentCount( ) <= 0 )
			{
				grandTotal = CrosstabExtendedItemFactory.createCrosstabCell( crosstabView.getModuleHandle( ) );
				propHandle.add( grandTotal );
			}

			// adjust the measure aggregations
			CrosstabReportItemHandle crosstab = crosstabView.getCrosstab( );
			if ( crosstab != null && measureList != null )
			{
				addMeasureAggregations( crosstabView.getAxisType( ),
						measureList,
						functionList,
						false );

				// adjust measure header
				addTotalMeasureHeader( crosstabView.getAxisType( ),
						null,
						measureList );
			}

			validateCrosstab( );

			totalCell = (CrosstabCellHandle) CrosstabUtil.getReportItem( grandTotal );
		}
		catch ( SemanticException e )
		{
			crosstabView.getLogger( ).log( Level.INFO, e.getMessage( ), e );

			if ( needTransaction )
			{
				stack.rollback( );
			}

			throw e;
		}

		if ( needTransaction )
		{
			stack.commit( );
		}

		return totalCell;
	}

	/**
	 * Removes grand total from crosstab if it is not empty, otherwise do
	 * nothing.
	 */
	public void removeGrandTotal( ) throws SemanticException
	{
		removeGrandTotal( true );
	}

	/**
	 * Remove grand total on particular measure
	 */
	public void removeGrandTotal( int measureIndex ) throws SemanticException
	{
		removeGrandTotal( measureIndex, true );
	}

	void removeGrandTotal( int measureIndex, boolean needTransaction )
			throws SemanticException
	{
		PropertyHandle propHandle = crosstabView.getGrandTotalProperty( );

		if ( propHandle.getContentCount( ) > 0 )
		{
			CommandStack stack = null;

			if ( needTransaction )
			{
				stack = crosstabView.getCommandStack( );
				stack.startTrans( Messages.getString( "CrosstabViewTask.msg.remove.grandtotal" ) ); //$NON-NLS-1$
			}

			try
			{
				removeTotalMeasureHeader( crosstabView.getAxisType( ),
						null,
						measureIndex );

				removeMeasureAggregations( crosstabView.getAxisType( ),
						measureIndex );

				if ( new CrosstabReportItemTask( crosstab ).getAggregationMeasures( crosstabView.getAxisType( ) )
						.size( ) == 0 )
				{
					// remove grandtotal header if no grandtotal aggregations on
					// all measures
					propHandle.drop( 0 );
				}
			}
			catch ( SemanticException e )
			{
				crosstabView.getLogger( ).log( Level.INFO, e.getMessage( ), e );

				if ( needTransaction )
				{
					stack.rollback( );
				}

				throw e;
			}

			if ( needTransaction )
			{
				stack.commit( );
			}
		}
	}

	/**
	 * Removes grand total from crosstab if it is not empty, otherwise do
	 * nothing.
	 */
	void removeGrandTotal( boolean needTransaction ) throws SemanticException
	{
		PropertyHandle propHandle = crosstabView.getGrandTotalProperty( );

		if ( propHandle.getContentCount( ) > 0 )
		{
			CommandStack stack = null;

			if ( needTransaction )
			{
				stack = crosstabView.getCommandStack( );
				stack.startTrans( Messages.getString( "CrosstabViewTask.msg.remove.grandtotal" ) ); //$NON-NLS-1$
			}

			try
			{
				// adjust the measure aggregations before remove the grand-total
				// cell, for some adjustment action should depend on the
				// grand-total information; if there is no level in this axis,
				// then we need do nothing about the aggregations
				if ( crosstab != null )
				// && CrosstabModelUtil.getAllLevelCount( crosstab,
				// crosstabView.getAxisType( ) ) > 0 )
				{
					removeTotalMeasureHeader( crosstabView.getAxisType( ), null );

					removeMeasureAggregations( crosstabView.getAxisType( ) );
				}

				propHandle.drop( 0 );
			}
			catch ( SemanticException e )
			{
				crosstabView.getLogger( ).log( Level.INFO, e.getMessage( ), e );

				if ( needTransaction )
				{
					stack.rollback( );
				}

				throw e;
			}

			if ( needTransaction )
			{
				stack.commit( );
			}
		}
	}

	/**
	 * Removes a dimension view that refers a cube dimension name with the given
	 * name from the design tree.
	 * 
	 * @param name
	 *            name of the dimension view to remove
	 * @throws SemanticException
	 */
	public void removeDimension( String name ) throws SemanticException
	{
		DimensionViewHandle dimensionView = crosstabView.getDimension( name );

		if ( dimensionView == null )
		{
			crosstabView.getLogger( ).log( Level.SEVERE,
					MessageConstants.CROSSTAB_EXCEPTION_DIMENSION_NOT_FOUND,
					name );
			throw new CrosstabException( crosstabView.getModelHandle( )
					.getElement( ), new String[]{
					name,
					crosstabView.getModelHandle( )
							.getElement( )
							.getIdentifier( )
			}, MessageConstants.CROSSTAB_EXCEPTION_DIMENSION_NOT_FOUND );
		}

		removeDimension( dimensionView, true );
	}

	/**
	 * Removes a dimension view in the given position. Index is 0-based integer.
	 * 
	 * @param index
	 *            the position index of the dimension to remove, 0-based integer
	 * @throws SemanticException
	 */
	public void removeDimension( int index ) throws SemanticException
	{
		DimensionViewHandle dimensionView = crosstabView.getDimension( index );
		if ( dimensionView == null )
		{
			crosstabView.getLogger( ).log( Level.SEVERE,
					MessageConstants.CROSSTAB_EXCEPTION_DIMENSION_NOT_FOUND,
					String.valueOf( index ) );
			return;
		}

		removeDimension( dimensionView, true );
	}

	public void removeDimension( DimensionViewHandle dimensionView )
			throws SemanticException
	{
		removeDimension( dimensionView, true );
	}

	void removeDimension( DimensionViewHandle dimensionView,
			boolean needTransaction ) throws SemanticException
	{
		if ( dimensionView == null
				|| dimensionView.getContainer( ) != crosstabView )
			return;

		CommandStack stack = null;

		if ( needTransaction )
		{
			stack = crosstabView.getCommandStack( );
			stack.startTrans( Messages.getString( "CrosstabViewTask.msg.remove.dimension" ) ); //$NON-NLS-1$
		}

		int count = dimensionView.getLevelCount( );

		try
		{
			// adjust measure aggregations and then remove dimension view from
			// the design tree, the order can not reversed
			if ( crosstab != null )
			{
				DimensionViewTask dimTask = new DimensionViewTask( dimensionView );

				for ( int i = 0; i < count; i++ )
				{
					LevelViewHandle lv = dimensionView.getLevel( 0 );

					if ( lv != null )
					{
						dimTask.removeLevel( lv, false );
					}
				}
			}

			dimensionView.getModelHandle( ).drop( );

			// check if all dimensions are removed in current view, we need to
			// remove grand total on this axis
			if ( crosstabView.getDimensionCount( ) == 0 )
			{
				removeGrandTotal( false );
			}
		}
		catch ( SemanticException e )
		{
			if ( needTransaction )
			{
				stack.rollback( );
			}

			throw e;
		}

		if ( needTransaction )
		{
			stack.commit( );
		}
	}
}

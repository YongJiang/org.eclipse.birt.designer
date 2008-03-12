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

package org.eclipse.birt.report.item.crosstab.internal.ui.editors.action;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.birt.report.designer.internal.ui.util.ExceptionHandler;
import org.eclipse.birt.report.designer.internal.ui.util.UIUtil;
import org.eclipse.birt.report.designer.ui.newelement.DesignElementFactory;
import org.eclipse.birt.report.item.crosstab.core.de.AggregationCellHandle;
import org.eclipse.birt.report.item.crosstab.core.de.CrosstabReportItemHandle;
import org.eclipse.birt.report.item.crosstab.core.de.MeasureViewHandle;
import org.eclipse.birt.report.item.crosstab.internal.ui.AggregationCellProviderWrapper;
import org.eclipse.birt.report.item.crosstab.internal.ui.dialogs.ShowSummaryFieldDialog;
import org.eclipse.birt.report.item.crosstab.internal.ui.dialogs.ShowSummaryFieldDialog.MeasureInfo;
import org.eclipse.birt.report.item.crosstab.internal.ui.editors.model.CrosstabAdaptUtil;
import org.eclipse.birt.report.item.crosstab.internal.ui.util.CrosstabUIHelper;
import org.eclipse.birt.report.item.crosstab.ui.extension.IAggregationCellViewProvider;
import org.eclipse.birt.report.item.crosstab.ui.extension.SwitchCellInfo;
import org.eclipse.birt.report.item.crosstab.ui.i18n.Messages;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.ExtendedItemHandle;
import org.eclipse.birt.report.model.api.LabelHandle;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.eclipse.birt.report.model.api.olap.CubeHandle;
import org.eclipse.birt.report.model.api.olap.MeasureGroupHandle;
import org.eclipse.birt.report.model.api.olap.MeasureHandle;
import org.eclipse.birt.report.model.elements.interfaces.ICubeModel;
import org.eclipse.birt.report.model.elements.interfaces.IMeasureGroupModel;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;

/**
 * Add the measurehandle to the crosstab.
 */

public class AddMeasureViewHandleAction extends AbstractCrosstabAction
{

	private MeasureViewHandle measureViewHandle;
	
	boolean needUpdateView = false;
	/**
	 * Action displayname
	 */
	// private static final String ACTION_MSG_MERGE = "Show/Hide Measures";
	/** action ID */
	public static final String ID = "org.eclipse.birt.report.item.crosstab.internal.ui.editors.action.AddMesureViewHandleAction"; //$NON-NLS-1$

	/**
	 * Trans name
	 */
	// private static final String NAME = "Add measure handle";
	private static final String NAME = Messages.getString( "AddMesureViewHandleAction.DisplayName" );//$NON-NLS-1$
	private static final String ACTION_MSG_MERGE = Messages.getString( "AddMesureViewHandleAction.TransName" );//$NON-NLS-1$

	/**
	 * Constructor
	 * 
	 * @param handle
	 */
	public AddMeasureViewHandleAction( DesignElementHandle handle )
	{
		super( handle );
		setId( ID );
		setText( ACTION_MSG_MERGE );
		ExtendedItemHandle extendedHandle = CrosstabAdaptUtil.getExtendedItemHandle( handle );
		setHandle( extendedHandle );
		measureViewHandle = CrosstabAdaptUtil.getMeasureViewHandle( extendedHandle );

		Image image = CrosstabUIHelper.getImage( CrosstabUIHelper.SHOW_HIDE_LECEL );
		setImageDescriptor( ImageDescriptor.createFromImage( image ) );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run( )
	{
		transStar( NAME );
		try
		{
			CrosstabReportItemHandle reportHandle = measureViewHandle.getCrosstab( );
			ShowSummaryFieldDialog dialog = new ShowSummaryFieldDialog( UIUtil.getDefaultShell( ),
					reportHandle );
			List list = getDimensionHandles( );
			List input = new ArrayList( );
			for ( int i = 0; i < list.size( ); i++ )
			{
				MeasureHandle handle = (MeasureHandle) list.get( i );
				MeasureInfo info = new MeasureInfo( );
				info.setMeasure( handle );
				info.setExpectedView( "" ); //$NON-NLS-1$
				input.add( info );
			}

			int count = reportHandle.getMeasureCount( );
			for ( int i = 0; i < count; i++ )
			{
				MeasureViewHandle viewHandle = reportHandle.getMeasure( i );
				checkStatus( viewHandle, input );
			}

			dialog.setInput( copyInfo( input ) );
			if ( dialog.open( ) == Window.OK )
			{
				List result = (List) dialog.getResult( );
				boolean isRemove = processor( input, result );
				if ( isRemove )
				{
					CrosstabAdaptUtil.processInvaildBindings( reportHandle );
				}
				
				providerWrapper.switchViews( );
				if(needUpdateView)
				{
					providerWrapper.updateAllAggregationCells( );
				}
			}

		}
		catch ( SemanticException e )
		{
			rollBack( );
			ExceptionHandler.handle( e );
			return;
		}
		transEnd( );
	}

//	private void updateShowStatus( MeasureViewHandle measureView,
//			MeasureInfo info )
//	{
//		if ( info.isShow( ) == false )
//		{
//			return;
//		}
//		String expectedView = info.getExpectedView( );
//		AggregationCellHandle cell = measureView.getCell( );
//		if ( expectedView == null || expectedView.length( ) == 0 )
//		{
//			return;
//		}
//
//
//		IAggregationCellViewProvider provider = providerWrapper.getMatchProvider( cell );
//		if(provider != null)
//		{
//			// if current view is the same view with the expected one, then don't restore
//			if(! provider.getViewName( ).equals( expectedView ))
//			{
//				provider.restoreView( cell );
//			}
//		}
//		
//		providerWrapper.switchView( expectedView, cell );
//
//	}

	private AggregationCellProviderWrapper providerWrapper;

	private void initializeProviders( )
	{

		providerWrapper = new AggregationCellProviderWrapper( (ExtendedItemHandle) measureViewHandle.getCrosstab( )
				.getModelHandle( ) );
	}

	private MeasureInfo getOriMeasureInfo( MeasureInfo info, List list )
	{
		MeasureInfo ret = null;
		for ( int i = 0; i < list.size( ); i++ )
		{
			MeasureInfo comparedOne = (MeasureInfo) list.get( i );
			if ( info.isSameInfo( comparedOne ) )
			{
				return comparedOne;
			}
		}
		return ret;
	}

	private MeasureViewHandle findMeasureViewHandle( MeasureHandle measure )
	{
		return measureViewHandle.getCrosstab( )
				.getMeasure( measure.getQualifiedName( ) );
	}

	private boolean processor( List list, List result )
			throws SemanticException
	{
		initializeProviders( );

		boolean isRemove = false;

		List temp = new ArrayList( result );
		for ( int i = 0; i < result.size( ); i++ )
		{
			MeasureInfo resultOne = (MeasureInfo) result.get( i );
			MeasureInfo originalOne = getOriMeasureInfo( resultOne, list );
			if ( resultOne.isShow( ) == originalOne.isShow( ) )
			{
				MeasureInfo info = (MeasureInfo) result.get( i );
				if ( info.isShow( ) == true
						&& info.getExpectedView( ) != null
						&& info.getExpectedView( ).length( ) != 0 )
				{
//					MeasureViewHandle handle = findMeasureViewHandle( info.getMeasure( ) );
//					updateShowStatus( handle, info );
					SwitchCellInfo swtichCellInfo = new SwitchCellInfo(measureViewHandle.getCrosstab( ),SwitchCellInfo.MEASURE);
					swtichCellInfo.setMeasureInfo( info );
					providerWrapper.addSwitchInfo( swtichCellInfo );
					needUpdateView = true;
				}
				temp.remove( resultOne );
			}
		}
		CrosstabReportItemHandle reportHandle = measureViewHandle.getCrosstab( );
		for ( int i = 0; i < temp.size( ); i++ )
		{
			MeasureInfo info = (MeasureInfo) temp.get( i );
			if ( info.isShow( ) )
			{
				// reportHandle.insertMeasure( info.getMeasure( ),
				// reportHandle.getMeasureCount( ) );
				MeasureViewHandle measureViewHandle = reportHandle.insertMeasure( info.getMeasure( ),
						reportHandle.getMeasureCount( ) );
				measureViewHandle.addHeader( );

				LabelHandle labelHandle = DesignElementFactory.getInstance( )
						.newLabel( null );
				labelHandle.setText( info.getMeasure( ).getName( ) );
				needUpdateView = true;
				measureViewHandle.getHeader( ).addContent( labelHandle );
				if ( info.getExpectedView( ) != null
						&& info.getExpectedView( ).length( ) != 0 )
				{
//					updateShowStatus( measureViewHandle, info );					
					SwitchCellInfo swtichCellInfo = new SwitchCellInfo(measureViewHandle.getCrosstab( ),SwitchCellInfo.MEASURE);
					info.setMeasure( measureViewHandle.getCubeMeasure( ) );
					swtichCellInfo.setMeasureInfo( info );
					providerWrapper.addSwitchInfo( swtichCellInfo );
				}
			}
			else
			{
				reportHandle.removeMeasure( info.getMeasure( )
						.getQualifiedName( ) );
				isRemove = true;	
				needUpdateView = true;
			}
		}
		
		return isRemove;
	}

	private void checkStatus( MeasureViewHandle viewHandle, List list )
	{
		for ( int i = 0; i < list.size( ); i++ )
		{
			MeasureInfo info = (MeasureInfo) list.get( i );
			if ( info.getMeasure( ).equals( viewHandle.getCubeMeasure( ) ) )
			{
				info.setShow( true );
				break;
			}
		}
	}

	private List copyInfo( List list )
	{
		List retValue = new ArrayList( );
		for ( int i = 0; i < list.size( ); i++ )
		{
			retValue.add( ( (MeasureInfo) list.get( i ) ).copy( ) );

		}
		return retValue;
	}

	private List getDimensionHandles( )
	{
		List retValue = new ArrayList( );
		CubeHandle cubeHandle = measureViewHandle.getCrosstab( ).getCube( );
		List list = cubeHandle.getContents( ICubeModel.MEASURE_GROUPS_PROP );
		for ( int i = 0; i < list.size( ); i++ )
		{
			MeasureGroupHandle groupHandle = (MeasureGroupHandle) list.get( i );
			List tempList = groupHandle.getContents( IMeasureGroupModel.MEASURES_PROP );
			retValue.addAll( tempList );
		}

		return retValue;
	}
}

/*******************************************************************************
 * Copyright (c) 2004, 2005 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.report.designer.data.ui.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.birt.core.data.DataType;
import org.eclipse.birt.core.data.ExpressionUtil;
import org.eclipse.birt.core.data.IColumnBinding;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.data.engine.api.IBaseExpression;
import org.eclipse.birt.data.engine.api.IPreparedQuery;
import org.eclipse.birt.data.engine.api.IQueryResults;
import org.eclipse.birt.data.engine.api.IResultIterator;
import org.eclipse.birt.data.engine.api.IResultMetaData;
import org.eclipse.birt.data.engine.api.querydefn.BaseDataSetDesign;
import org.eclipse.birt.data.engine.api.querydefn.Binding;
import org.eclipse.birt.data.engine.api.querydefn.GroupDefinition;
import org.eclipse.birt.data.engine.api.querydefn.InputParameterBinding;
import org.eclipse.birt.data.engine.api.querydefn.JointDataSetDesign;
import org.eclipse.birt.data.engine.api.querydefn.QueryDefinition;
import org.eclipse.birt.data.engine.api.querydefn.ScriptExpression;
import org.eclipse.birt.data.engine.core.DataException;
import org.eclipse.birt.report.data.adapter.api.DataRequestSession;
import org.eclipse.birt.report.data.adapter.api.DataSessionContext;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.model.api.DataSetHandle;
import org.eclipse.birt.report.model.api.DataSetParameterHandle;
import org.eclipse.birt.report.model.api.ModuleHandle;
import org.eclipse.birt.report.model.api.OdaDataSetParameterHandle;
import org.eclipse.birt.report.model.api.PropertyHandle;

/**
 * Utility class to fetch all available value for filter use.
 * 
 */
public class SelectValueFetcher
{

	private final static String BINDING_GROUP_NAME = "GROUP"; //$NON-NLS-1$
	private final static String BINDING_ROW_NAME = "FILTER_SELECT_VALUE"; //$NON-NLS-1$
	private final static String BINDING_GROUP_EXPRESSION = "row.FILTER_SELECT_VALUE"; //$NON-NLS-1$

	/**
	 * private constructor
	 */
	private SelectValueFetcher( )
	{
	}
	
	public static List getSelectValueList( String expression,
			DataSetHandle dataSetHandle, boolean inclFilter ) throws BirtException
	{
		boolean startsWithRow = ExpressionUtility.isColumnExpression( expression,
				true );
		boolean startsWithDataSetRow = ExpressionUtility.isColumnExpression( expression,
				false );
		if ( !startsWithRow && !startsWithDataSetRow )
		{
			throw new DataException( Messages.getString( "SelectValueDialog.messages.info.invalidSelectVauleExpression" ) ); //$NON-NLS-1$
		}
		List selectValueList = new ArrayList( );
		if ( expression != null && expression.trim( ).length( ) > 0 )
		{
			// Execute the query and populate this list
			QueryDefinition query = new QueryDefinition( );
			query.setDataSetName( dataSetHandle.getQualifiedName( ) );
			query.setAutoBinding( true );
			PropertyHandle handle = dataSetHandle.getPropertyHandle( DataSetHandle.PARAMETERS_PROP );

			if ( handle != null )
			{
				Iterator paramIter = handle.iterator( );
				while ( paramIter.hasNext( ) )
				{
					DataSetParameterHandle paramDefn = (DataSetParameterHandle) paramIter.next( );
					if ( paramDefn.isInput( ) )
					{
						String defaultValue = null;
						if ( paramDefn instanceof OdaDataSetParameterHandle
								&& ( (OdaDataSetParameterHandle) paramDefn ).getParamName( ) != null )
						{
							defaultValue = DataUtil.getParamValue( dataSetHandle, (OdaDataSetParameterHandle) paramDefn );
						}
						if ( defaultValue != null )
						{
							InputParameterBinding binding = new InputParameterBinding( paramDefn.getName( ),
									new ScriptExpression( defaultValue ) );
							query.addInputParamBinding( binding );
						}
					}
				}
			}

			IBaseExpression bindingExprGroup = new ScriptExpression( ExpressionUtility.getReplacedColRefExpr( expression ) );
			GroupDefinition groupDefn = new GroupDefinition( BINDING_GROUP_NAME );
			groupDefn.setKeyExpression( BINDING_GROUP_EXPRESSION );
			query.addBinding( new Binding( BINDING_ROW_NAME, bindingExprGroup ) );
			query.addGroup( groupDefn );
			query.setUsesDetails( false );

			DataRequestSession session = DataRequestSession.newSession( new DataSessionContext( DataSessionContext.MODE_DIRECT_PRESENTATION,
					dataSetHandle == null ? null
							: dataSetHandle.getModuleHandle( ) ) );
			if ( dataSetHandle != null )
			{
				defineSourceAndDataSets( session,
						dataSetHandle,
						session.getModelAdaptor( ).adaptDataSet( dataSetHandle ),
						inclFilter );
			}

			IPreparedQuery preparedQuery = session.prepare( query );
			IQueryResults results = preparedQuery.execute( null );
			if ( results != null )
			{
				//if the referenced column is of BLOB type, then throw out an exception
				String columnName = getReferenceColumnName( expression, startsWithRow );
				if ( columnName == null )
				{
					throw new DataException( Messages.getString( "SelectValueDialog.messages.info.invalidSelectVauleExpression" ) ); //$NON-NLS-1$
				}
				IResultMetaData metaData = results.getResultMetaData( );
				for ( int i = 0; i < metaData.getColumnCount( ); i++ )
				{
					if ( metaData.getColumnName( i + 1 ).equals( columnName ) )
					{
						if ( metaData.getColumnType( i + 1 ) == DataType.BLOB_TYPE )
						{
							throw new DataException( Messages.getString( "SelectValueDialog.messages.warning.BlobColumn.unsupported" ) ); //$NON-NLS-1$
						}
						break;
					}
				}
				IResultIterator iter = null;
				iter = results.getResultIterator( );
				if ( iter != null )
				{
					while ( iter.next( ) )
					{
						Object candiateValue = iter.getValue( BINDING_ROW_NAME );
						selectValueList.add( candiateValue );
						iter.skipToEnd( 1 );
					}
				}
				results.close( );
			}
			session.shutdown( );
		}
		return selectValueList;

	}
	
	/**
	 * A private method that helps to define all the children data sets'
	 * data sources and data sets themselves
	 * 
	 * @param session
	 * @param dataSet
	 * @param dataSetDesign
	 * @param inclFilter
	 * 
	 * @throws BirtException
	 */
	private static void defineSourceAndDataSets( DataRequestSession session,
			DataSetHandle dataSet, BaseDataSetDesign dataSetDesign,
			boolean inclFilter ) throws BirtException
	{
		if ( dataSet.getDataSource( ) != null )
		{
			session.defineDataSource( session.getModelAdaptor( ).adaptDataSource( dataSet.getDataSource( ) ) );
		}
		if ( !inclFilter )
		{
			if ( dataSetDesign.getFilters( ) != null )
			{
				dataSetDesign.getFilters( ).clear( );
			}
		}
		session.defineDataSet( dataSetDesign );
		if ( dataSetDesign instanceof JointDataSetDesign )
		{
			defineChildDSetAndDSource( session,
					(JointDataSetDesign) dataSetDesign,
					dataSet.getModuleHandle( ),
					inclFilter,
					true );

			defineChildDSetAndDSource( session,
					(JointDataSetDesign) dataSetDesign,
					dataSet.getModuleHandle( ),
					inclFilter,
					false );
		}
	}

	/**
	 * To help to define the children data sets of a JointDataSet
	 * 
	 * @param session
	 * @param dataSetDesign
	 * @param modulehandle
	 * @param modelAdapter
	 * @param inclFilter
	 * @param isLeft
	 * 
	 * @throws BirtException
	 */
	private static void defineChildDSetAndDSource( DataRequestSession session,
			JointDataSetDesign dataSetDesign, ModuleHandle modulehandle,
			boolean inclFilter, boolean isLeft ) throws BirtException
	{
		DataSetHandle dataSetHandle;
		if ( isLeft )
		{
			dataSetHandle = modulehandle.findDataSet( dataSetDesign.getLeftDataSetDesignName( ) );
		}
		else
		{
			dataSetHandle = modulehandle.findDataSet( dataSetDesign.getRightDataSetDesignName( ) );
		}
		defineSourceAndDataSets( session, dataSetHandle, session.getModelAdaptor( )
				.adaptDataSet( dataSetHandle ), inclFilter );
	}
	
	/**
	 * A help method to get the referenced column name of the given expression
	 * 
	 * @param expression
	 * @return
	 */
	private static String getReferenceColumnName( String expression, boolean mode )
	{
		try
		{
			List columnValue = null;
			columnValue = ExpressionUtil.extractColumnExpressions( expression,
					mode );
			if ( columnValue == null || columnValue.size( ) == 0 )
			{
				if ( columnValue == null || columnValue.size( ) == 0 )
				{
					return null;
				}
			}
			return ( (IColumnBinding) columnValue.get( 0 ) ).getResultSetColumnName( );
		}
		catch ( BirtException e )
		{
			return null;
		}
	}

	/**
	 * 
	 * @param selectValueExpression
	 * @param dataSetHandle
	 * @return
	 * @throws BirtException
	 */
	public static List getSelectValueList( String expression,
			DataSetHandle dataSetHandle ) throws BirtException
	{
		return getSelectValueList( expression, dataSetHandle, true );
	}
}
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

import org.eclipse.birt.core.data.ExpressionUtil;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.data.engine.api.IBaseExpression;
import org.eclipse.birt.data.engine.api.IPreparedQuery;
import org.eclipse.birt.data.engine.api.IQueryResults;
import org.eclipse.birt.data.engine.api.IResultIterator;
import org.eclipse.birt.data.engine.api.querydefn.Binding;
import org.eclipse.birt.data.engine.api.querydefn.GroupDefinition;
import org.eclipse.birt.data.engine.api.querydefn.InputParameterBinding;
import org.eclipse.birt.data.engine.api.querydefn.QueryDefinition;
import org.eclipse.birt.data.engine.api.querydefn.ScriptExpression;
import org.eclipse.birt.report.data.adapter.api.DataRequestSession;
import org.eclipse.birt.report.data.adapter.api.DataSessionContext;
import org.eclipse.birt.report.model.api.DataSetHandle;
import org.eclipse.birt.report.model.api.DataSetParameterHandle;
import org.eclipse.birt.report.model.api.OdaDataSetParameterHandle;
import org.eclipse.birt.report.model.api.PropertyHandle;

/**
 * Utility class to fetch all available value for filter use.
 * 
 */
public class SelectValueFetcher
{
	private final static String BINDING_GROUP_NAME = "GROUP";
	private final static String BINDING_ROW_NAME = "FILTER_SELECT_VALUE";
	private final static String BINDING_GROUP_EXPRESSION = "row.FILTER_SELECT_VALUE";
	
	/**
	 * private constructor
	 */
	private SelectValueFetcher( )
	{
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
		List selectValueList = new ArrayList( );
		if ( expression != null && expression.trim( ).length( ) > 0 )
		{
			// Execute the query and populate this list
			QueryDefinition query = new QueryDefinition( );
			query.setDataSetName( dataSetHandle.getQualifiedName( ) );

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
						if ( paramDefn instanceof OdaDataSetParameterHandle &&
								( (OdaDataSetParameterHandle) paramDefn ).getParamName( ) != null )
							defaultValue = ExpressionUtil.createJSParameterExpression( ( (OdaDataSetParameterHandle) paramDefn ).getParamName( ) );
						else
							defaultValue = paramDefn.getDefaultValue( );
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
					dataSetHandle == null ? null : dataSetHandle.getModuleHandle( ) ) );
			if ( dataSetHandle != null )
			{
				if ( dataSetHandle.getDataSource( ) != null )
					session.defineDataSource( session.getModelAdaptor( )
							.adaptDataSource( dataSetHandle.getDataSource( ) ) );
				session.defineDataSet( session.getModelAdaptor( )
						.adaptDataSet( dataSetHandle ) );
			}
			
			IPreparedQuery preparedQuery = session.prepare( query );
			IQueryResults results = preparedQuery.execute( null );
			if ( results != null )
			{
				IResultIterator iter = null;
				iter = results.getResultIterator( );
				if ( iter != null )
				{
					while ( iter.next( ) )
					{
						Object candiateValue = iter.getValue( BINDING_ROW_NAME );
						if ( candiateValue != null )
						{
							selectValueList.add( candiateValue );
						}
						iter.skipToEnd( 1 );
					}
				}
				results.close( );
			}
		}
		return selectValueList;
	}
}
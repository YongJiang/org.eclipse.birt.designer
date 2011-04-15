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

package org.eclipse.birt.report.designer.data.ui.providers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.birt.core.data.ExpressionUtil;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.data.engine.api.IResultIterator;
import org.eclipse.birt.data.engine.api.querydefn.ScriptExpression;
import org.eclipse.birt.data.engine.core.DataException;
import org.eclipse.birt.report.data.adapter.api.DataRequestSession;
import org.eclipse.birt.report.data.adapter.api.DataSessionContext;
import org.eclipse.birt.report.data.adapter.impl.DataModelAdapter;
import org.eclipse.birt.report.designer.data.ui.dataset.AppContextPopulator;
import org.eclipse.birt.report.designer.data.ui.dataset.AppContextResourceReleaser;
import org.eclipse.birt.report.designer.data.ui.dataset.DataSetPreviewer;
import org.eclipse.birt.report.designer.data.ui.dataset.DataSetPreviewer.PreviewType;
import org.eclipse.birt.report.designer.data.ui.dataset.ExternalUIUtil;
import org.eclipse.birt.report.designer.data.ui.util.DTPUtil;
import org.eclipse.birt.report.designer.data.ui.util.DataSetProvider;
import org.eclipse.birt.report.designer.data.ui.util.DummyEngineTask;
import org.eclipse.birt.report.designer.data.ui.util.ExpressionUtility;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.impl.ReportEngine;
import org.eclipse.birt.report.engine.api.impl.ReportEngineFactory;
import org.eclipse.birt.report.engine.api.impl.ReportEngineHelper;
import org.eclipse.birt.report.model.api.CachedMetaDataHandle;
import org.eclipse.birt.report.model.api.DataSetHandle;
import org.eclipse.birt.report.model.api.Expression;
import org.eclipse.birt.report.model.api.MemberHandle;
import org.eclipse.birt.report.model.api.ModuleHandle;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.ResultSetColumnHandle;
import org.eclipse.birt.report.model.api.core.IDesignElement;
import org.eclipse.birt.report.model.api.elements.structures.ComputedColumn;
import org.eclipse.birt.report.model.core.DesignElement;
import org.eclipse.birt.report.model.elements.DataSet;
import org.eclipse.birt.report.model.elements.interfaces.IDataSetModel;
import org.eclipse.datatools.connectivity.oda.IBlob;
import org.eclipse.datatools.connectivity.oda.util.ResourceIdentifiers;

/**
 * Utility class to fetch all available value for filter use.
 * 
 */
public class DistinctValueSelector
{
	/**
	 * private constructor
	 */
	private DistinctValueSelector( )
	{
	}

	/**
	 * Used in the filter select value dialog in dataset editor
	 * 
	 * @param expression
	 * @param dataSetHandle
	 * @param binding
	 * @param useDataSetFilter
	 * @return
	 * @throws BirtException
	 */
	public static List getSelectValueList( Expression expression,
			DataSetHandle dataSetHandle, boolean useDataSetFilter )
			throws BirtException
	{
		ScriptExpression expr = null;
		DataSetHandle targetHandle = dataSetHandle;
		Map appContext = new HashMap( );
		DataSetPreviewer previewer = null;

		try
		{
			if ( !useDataSetFilter )
			{
				IDesignElement element = dataSetHandle.copy( );
				( (DataSet) element ).setProperty( IDataSetModel.FILTER_PROP,
						new ArrayList( ) );	
				targetHandle =ExternalUIUtil.newDataSetHandle( dataSetHandle, (DesignElement)element );
			}
			previewer = new DataSetPreviewer( targetHandle,
					0,
					PreviewType.RESULTSET );
			
			DataModelAdapter adapter = new DataModelAdapter( new DataSessionContext( DataSessionContext.MODE_DIRECT_PRESENTATION,
					targetHandle.getModuleHandle( ) ) );
			expr = adapter.adaptExpression( expression );

			boolean startsWithRow = ExpressionUtility.isColumnExpression( expr.getText( ),
					true );
			boolean startsWithDataSetRow = ExpressionUtility.isColumnExpression( expr.getText( ),
					false );
			if ( !startsWithRow && !startsWithDataSetRow )
			{
				throw new DataException( Messages.getString( "SelectValueDialog.messages.info.invalidSelectVauleExpression" ) ); //$NON-NLS-1$
			}

			String dataSetColumnName = null;
			if ( startsWithDataSetRow )
			{
				dataSetColumnName = ExpressionUtil.getColumnName( expr.getText( ) );
			}
			else
			{
				dataSetColumnName = ExpressionUtil.getColumnBindingName( expr.getText( ) );
			}			
			
			ResourceIdentifiers identifiers = new ResourceIdentifiers( );
			String resouceIDs = ResourceIdentifiers.ODA_APP_CONTEXT_KEY_CONSUMER_RESOURCE_IDS;					
			identifiers.setApplResourceBaseURI( DTPUtil.getInstance( ).getBIRTResourcePath( ) );
			identifiers.setDesignResourceBaseURI( DTPUtil.getInstance( ).getReportDesignPath( ) );
			appContext.put( resouceIDs,identifiers);
		
			AppContextPopulator.populateApplicationContext( targetHandle, appContext );
			previewer.open( appContext, getEngineConfig( targetHandle.getModuleHandle( ) ) );
			IResultIterator itr = previewer.preview( ) ;
			
			Set visitedValues = new HashSet( );
			Object value = null;
			
			while ( itr.next( ) )
			{
				// default is to return 10000 distinct value
				if ( visitedValues.size( ) > 10000 )
				{
					break;
				}
				value = itr.getValue( dataSetColumnName );
				if ( !visitedValues.contains( value ) )
				{
					visitedValues.add( value );
				}
			}

			if ( visitedValues.isEmpty( ) )
				return Collections.EMPTY_LIST;

			return new ArrayList( visitedValues );
		}
		finally
		{
			AppContextResourceReleaser.release( appContext );
			if ( previewer != null )
				previewer.close( );
		}
	}
	
	
	private static EngineConfig getEngineConfig( ModuleHandle handle )
	{
		EngineConfig ec = new EngineConfig( );
		ClassLoader parent = Thread.currentThread( ).getContextClassLoader( );
		if ( parent == null )
		{
			parent = handle.getClass( ).getClassLoader( );
		}
		ClassLoader customClassLoader = DataSetProvider.getCustomScriptClassLoader( parent, handle );
		ec.getAppContext( ).put( EngineConstants.APPCONTEXT_CLASSLOADER_KEY,
				customClassLoader );
		return ec;
	}
	
	/**
	 * Used in filter select value dialog in layout with group definition.
	 * 
	 * @param expression
	 * @param dataSetHandle
	 * @param binding The iterator of ComputedColumnHandle
	 * @param groupIterator The iterator of GroupHandle
	 * @param useDataSetFilter
	 * @return
	 * @throws BirtException
	 */
	public static List getSelectValueFromBinding( Expression expression,
			DataSetHandle dataSetHandle, Iterator binding,
			Iterator groupIterator, boolean useDataSetFilter )
			throws BirtException
	{
		String columnName = null;
		List bindingList = new ArrayList( );

		if ( binding != null && binding.hasNext( ) )
		{
			while ( binding.hasNext( ) )
			{
				bindingList.add( binding.next( ) );
			}
		}
		ComputedColumn handle = new ComputedColumn( );
		columnName = "TEMP_" + expression.getStringExpression( );
		handle.setExpressionProperty( ComputedColumn.EXPRESSION_MEMBER,
				expression );
		handle.setName( columnName );
		bindingList.add( handle );

		Collection result = null;
		DataRequestSession session = null;
		if ( dataSetHandle != null
				&& ( dataSetHandle.getModuleHandle( ) instanceof ReportDesignHandle ) )
		{
			EngineConfig config = new EngineConfig( );

			ReportDesignHandle copy = (ReportDesignHandle) ( dataSetHandle.getModuleHandle( )
					.copy( ).getHandle( null ) );

			config.setProperty( EngineConstants.APPCONTEXT_CLASSLOADER_KEY,
					DataSetProvider.getCustomScriptClassLoader( Thread.currentThread( )
							.getContextClassLoader( ),
							copy ) );

			ReportEngine engine = (ReportEngine) new ReportEngineFactory( ).createReportEngine( config );

			DummyEngineTask engineTask = new DummyEngineTask( engine,
					new ReportEngineHelper( engine ).openReportDesign( (ReportDesignHandle) copy ),
					copy, dataSetHandle );

			session = engineTask.getDataSession( );

			AppContextPopulator.populateApplicationContext( dataSetHandle,
					session );

			engineTask.run( );
			result = session.getColumnValueSet( dataSetHandle,
					dataSetHandle.getPropertyHandle( DataSetHandle.PARAMETERS_PROP )
							.iterator( ),
					bindingList.iterator( ),
					groupIterator,
					columnName,
					useDataSetFilter,
					null );

			engineTask.close( );
			engine.destroy( );
		}
		else if ( dataSetHandle != null )
		{
			session = DataRequestSession.newSession( new DataSessionContext( DataSessionContext.MODE_DIRECT_PRESENTATION,
					dataSetHandle.getModuleHandle( ) ) );

			AppContextPopulator.populateApplicationContext( dataSetHandle,
					session );

			result = session.getColumnValueSet( dataSetHandle,
					dataSetHandle.getPropertyHandle( DataSetHandle.PARAMETERS_PROP )
							.iterator( ),
					bindingList.iterator( ),
					null,
					columnName,
					useDataSetFilter,
					null );
			session.shutdown( );
		}

		assert result != null;
		if ( result.isEmpty( ) )
			return Collections.EMPTY_LIST;

		Object resultProtoType = result.iterator( ).next( );
		if ( resultProtoType instanceof IBlob
				|| resultProtoType instanceof byte[] )
			return Collections.EMPTY_LIST;

		return new ArrayList( result );
	}
	
	private static String findColumnDataType( DataSetHandle handle,
			String columnName )
	{
		if ( handle.getCachedMetaDataHandle( ) != null )
		{
			CachedMetaDataHandle metaDataHandle = handle.getCachedMetaDataHandle( );
			if ( metaDataHandle == null )
				return null;
			MemberHandle memberHandle = metaDataHandle.getResultSet( );
			if ( memberHandle == null )
				return null;
			Iterator iterator = memberHandle.iterator( );
			while ( iterator.hasNext( ) )
			{
				ResultSetColumnHandle columnHandle = (ResultSetColumnHandle) iterator.next( );
				if ( columnHandle.getColumnName( ).equals( columnName ) )
					return columnHandle.getDataType( );
			}
		}
		return null;
	}
}
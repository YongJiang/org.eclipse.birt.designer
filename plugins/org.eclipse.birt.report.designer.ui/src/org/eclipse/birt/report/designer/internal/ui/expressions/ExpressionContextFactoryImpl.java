/*******************************************************************************
 * Copyright (c) 2008 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.report.designer.internal.ui.expressions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.birt.report.designer.internal.ui.script.JSExpressionContext;
import org.eclipse.birt.report.designer.ui.dialogs.IExpressionProvider;
import org.eclipse.birt.report.designer.ui.expressions.ExpressionFilter;
import org.eclipse.birt.report.designer.ui.expressions.IExpressionFilterSupport;
import org.eclipse.birt.report.designer.ui.views.ElementAdapterManager;
import org.eclipse.birt.report.model.api.ExpressionType;

/**
 * ExpressionContextFactoryImpl
 */
public class ExpressionContextFactoryImpl implements IExpressionContextFactory
{

	private Map<String, IExpressionContext> contexts;

	private List<ExpressionFilter> filters;

	private Map<String, Object> extras = new HashMap<String, Object>( );

	public ExpressionContextFactoryImpl( Object contextObj,
			IExpressionProvider javaScriptExpressionProvider )
	{
		contexts = new HashMap<String, IExpressionContext>( );

		contexts.put( ExpressionType.JAVASCRIPT,
				new JSExpressionContext( javaScriptExpressionProvider,
						contextObj ) );

		if ( javaScriptExpressionProvider instanceof IExpressionFilterSupport )
		{
			filters = ( (IExpressionFilterSupport) javaScriptExpressionProvider ).getFilters( );
		}
	}

	public ExpressionContextFactoryImpl( Object contextObj,
			IExpressionProvider javaScriptExpressionProvider,
			Map<String, Object> extras )
	{
		this( contextObj, javaScriptExpressionProvider );
		this.extras.putAll( extras );
	}

	public ExpressionContextFactoryImpl(
			Map<String, IExpressionContext> contexts )
	{
		this.contexts = contexts;
		if ( contexts != null
				&& contexts.get( ExpressionType.JAVASCRIPT ) instanceof JSExpressionContext )
		{
			IExpressionProvider provider = ( (JSExpressionContext) contexts.get( ExpressionType.JAVASCRIPT ) ).getExpressionProvider( );
			if ( provider instanceof IExpressionFilterSupport )
			{
				filters = ( (IExpressionFilterSupport) provider ).getFilters( );
			}
		}
	}

	public IExpressionContext getContext( String expressionType,
			Object contextObj )
	{
		IExpressionContextFactory factory = (IExpressionContextFactory) ElementAdapterManager.getAdapter( this,
				IExpressionContextFactory.class );

		if ( factory != null )
		{
			IExpressionContext cxt = factory.getContext( expressionType,
					contextObj );

			if ( cxt != null )
			{
				if ( cxt instanceof IExpressionFilterSupport )
				{
					( (IExpressionFilterSupport) cxt ).setFilters( filters );
				}

				return cxt;
			}
		}

		IExpressionContext cxt = contexts.get( expressionType );
		if ( cxt == null )
		{
			DefaultExpressionContext defaultCxt = new DefaultExpressionContext( contextObj );
			defaultCxt.setFilters( filters );

			Iterator iter = extras.keySet( ).iterator( );
			while ( iter.hasNext( ) )
			{
				String key = (String) iter.next( );
				defaultCxt.putExtra( key, extras.get( key ) );
			}
			
			return defaultCxt;
		}
		else
		{
			return cxt;
		}
	}

}

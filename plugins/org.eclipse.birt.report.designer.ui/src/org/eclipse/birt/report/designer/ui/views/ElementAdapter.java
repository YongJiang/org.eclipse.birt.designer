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

package org.eclipse.birt.report.designer.ui.views;

import org.eclipse.core.expressions.Expression;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.Platform;

/**
 * ElementAdapter
 */
public class ElementAdapter
{

	/**
	 * Comment for <code>id</code>
	 */
	private String id;
	/**
	 * Priority for this ElementAdapter.
	 */
	private int priority = 2;
	/**
	 * A id array of ElementAdapters to overwite (hide).
	 */
	private String[] overwrite;
	/**
	 * Does this ElementAdapter look for workbench adapter chain.
	 */
	private boolean includeWorkbenchContribute;
	/**
	 * Adapterabe type.
	 */
	private Class adaptableType;
	/**
	 * Target adapter type.
	 */
	private Class adapterType;
	/**
	 * Comment for <code>adapterInstance</code>
	 */
	private Object adapterInstance;
	/**
	 * Comment for <code>factory</code>
	 */
	private IAdapterFactory factory;
	/**
	 * Comment for <code>isSingleton</code>
	 */
	private boolean isSingleton;
	/**
	 * Adatper object instance.
	 */
	private Object adapter;

	private Expression expression;

	// getters and setters
	public Class getAdaptableType( )
	{
		return adaptableType;
	}

	public void setAdaptableType( Class adaptableType )
	{
		this.adaptableType = adaptableType;
	}

	public IAdapterFactory getFactory( )
	{
		return factory;
	}

	public void setFactory( IAdapterFactory factory )
	{
		this.factory = factory;
	}

	public boolean isIncludeWorkbenchContribute( )
	{
		return includeWorkbenchContribute;
	}

	public void setIncludeWorkbenchContribute(
			boolean includeWorkbenchContribute )
	{
		this.includeWorkbenchContribute = includeWorkbenchContribute;
	}

	public String[] getOverwrite( )
	{
		return overwrite;
	}

	public void setOverwrite( String[] overwrite )
	{
		this.overwrite = overwrite;
	}

	public int getPriority( )
	{
		return priority;
	}

	public void setPriority( int priority )
	{
		this.priority = priority;
	}

	public Class getAdapterType( )
	{
		return adapterType;
	}

	public void setAdapterType( Class type )
	{
		this.adapterType = type;
	}

	public String getId( )
	{
		return id;
	}

	public void setId( String id )
	{
		this.id = id;
	}

	public Object getAdapterInstance( )
	{
		return adapterInstance;
	}

	public void setAdapterInstance( Object adapterInstance )
	{
		this.adapterInstance = adapterInstance;
	}

	public boolean isSingleton( )
	{
		return isSingleton;
	}

	public void setSingleton( boolean isSingleton )
	{
		this.isSingleton = isSingleton;
	}

	public Expression getExpression( )
	{
		return expression;
	}

	public void setExpression( Expression expression )
	{
		this.expression = expression;
	}

	// public methods
	// FIXME singleton, factory
	public Object getAdater( Object adaptableObject )
	{
		if ( this.adapter != null && this.isSingleton )
			return this.adapter;
		if ( this.adapterInstance != null )
			return this.adapter = this.adapterInstance;
		if ( this.factory != null )
			this.adapter = this.factory.getAdapter( adaptableObject,
					this.adapterType );
		if ( this.adapter == null && this.includeWorkbenchContribute )
			this.adapter = Platform.getAdapterManager( )
					.getAdapter( adaptableObject, this.adapterType );
		return adapter;
	}

	public boolean equals( Object obj )
	{
		if ( obj instanceof ElementAdapter )
		{
			return this.getId( ).equals( ( (ElementAdapter) obj ).getId( ) );
		}
		return super.equals( obj );
	}

	public String toString( )
	{
		return this.getId( );
	}
}

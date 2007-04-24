/*******************************************************************************
 * Copyright (c) 2005 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.report.designer.ui.cubebuilder.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.birt.report.designer.internal.ui.util.DataUtil;
import org.eclipse.birt.report.designer.internal.ui.util.ExceptionHandler;
import org.eclipse.birt.report.designer.ui.cubebuilder.nls.Messages;
import org.eclipse.birt.report.designer.ui.dialogs.ExpressionProvider;
import org.eclipse.birt.report.designer.util.DEUtil;
import org.eclipse.birt.report.model.api.DataSetHandle;
import org.eclipse.birt.report.model.api.DataSetParameterHandle;
import org.eclipse.birt.report.model.api.PropertyHandle;
import org.eclipse.birt.report.model.api.ResultSetColumnHandle;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.eclipse.birt.report.model.api.olap.TabularCubeHandle;

public class CubeExpressionProvider extends ExpressionProvider
{

	public static final String DATASETS = Messages.getString( "ExpressionProvider.Category.DataSets" ); //$NON-NLS-1$

	private DataSetHandle dataSetHandle = null;

	public CubeExpressionProvider( TabularCubeHandle handle )
	{
		super( handle );
		dataSetHandle = handle.getDataSet( );
	}

	protected List getCategoryList( )
	{
		List categoryList = super.getCategoryList( );
		if ( dataSetHandle != null )
		{
			categoryList.add( DATASETS );
		}
		return categoryList;
	}

	protected List getChildrenList( Object parent )
	{
		if ( DATASETS.equals( parent ) )
		{
			List dataSeList = new ArrayList( );
			dataSeList.add( dataSetHandle );
			return dataSeList;
		}
		if ( parent instanceof DataSetHandle )
		{
			try
			{
				List columnList = DataUtil.getColumnList( (DataSetHandle) parent );
				List outputList = getOutputList( (DataSetHandle) parent );
				columnList.addAll( outputList );
				return columnList;
			}
			catch ( SemanticException e )
			{
				ExceptionHandler.handle( e );
				return Collections.EMPTY_LIST;
			}
		}
		return super.getChildrenList( parent );
	}

	/**
	 * Get output parameters if handle has.
	 * 
	 * @param handle
	 * @return
	 */
	private List getOutputList( DataSetHandle handle )
	{
		List outputList = new ArrayList( );
		PropertyHandle parameters = handle.getPropertyHandle( DataSetHandle.PARAMETERS_PROP );
		Iterator iter = parameters.iterator( );

		if ( iter != null )
		{
			while ( iter.hasNext( ) )
			{
				Object dataSetParameter = iter.next( );
				if ( ( (DataSetParameterHandle) dataSetParameter ).isOutput( ) == true )
				{
					outputList.add( dataSetParameter );
				}
			}
		}
		return outputList;
	}

	public String getDisplayText( Object element )
	{
		if ( element instanceof DataSetHandle )
		{
			return ( (DataSetHandle) element ).getName( );
		}
		else if ( element instanceof ResultSetColumnHandle )
		{
			return ( (ResultSetColumnHandle) element ).getColumnName( );
		}
		else if ( element instanceof DataSetParameterHandle )
		{
			return ( (DataSetParameterHandle) element ).getName( );
		}
		return super.getDisplayText( element );
	}

	public String getInsertText( Object element )
	{
		if ( element instanceof ResultSetColumnHandle
				|| element instanceof DataSetParameterHandle )
		{
			return DEUtil.getExpression( element );
		}
		return super.getInsertText( element );
	}
}

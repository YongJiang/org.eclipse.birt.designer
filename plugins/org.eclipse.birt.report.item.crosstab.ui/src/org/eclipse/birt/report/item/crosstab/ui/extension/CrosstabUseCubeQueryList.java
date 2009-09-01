/*******************************************************************************
 * Copyright (c) 2007 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.report.item.crosstab.ui.extension;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.birt.data.engine.olap.api.query.ICubeQueryDefinition;
import org.eclipse.birt.report.data.adapter.api.DataRequestSession;
import org.eclipse.birt.report.data.adapter.api.DataSessionContext;
import org.eclipse.birt.report.designer.internal.ui.extension.IUseCubeQueryList;
import org.eclipse.birt.report.designer.ui.preferences.PreferenceFactory;
import org.eclipse.birt.report.designer.ui.util.UIUtil;
import org.eclipse.birt.report.item.crosstab.core.de.CrosstabReportItemHandle;
import org.eclipse.birt.report.item.crosstab.internal.ui.util.CrosstabUIHelper;
import org.eclipse.birt.report.item.crosstab.plugin.CrosstabPlugin;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.ExtendedItemHandle;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.extension.ExtendedElementException;
import org.eclipse.birt.report.model.api.olap.CubeHandle;
import org.eclipse.birt.report.model.api.olap.TabularCubeHandle;

/**
 * CrosstabUseCubeQueryList
 */

public class CrosstabUseCubeQueryList implements IUseCubeQueryList
{

	public List getQueryList( String expression, ExtendedItemHandle extendedItem )
	{
		// TODO Auto-generated method stub			
		CrosstabReportItemHandle crosstab = null;
		CubeHandle cube = null;
		try
		{
			Object obj = ( (ExtendedItemHandle) extendedItem ).getReportItem( );
			DesignElementHandle tmp = extendedItem;

			while ( true )
			{
				if ( obj == null || obj instanceof ReportDesignHandle )
				{
					break;
				}
				else if ( obj instanceof CrosstabReportItemHandle )
				{
					crosstab = (CrosstabReportItemHandle) obj;
					cube = crosstab.getCube( );
					break;
				}
				else if ( tmp instanceof ExtendedItemHandle )
				{
					tmp = tmp.getContainer( );
					if ( tmp instanceof ExtendedItemHandle )
					{
						obj = ( (ExtendedItemHandle) tmp ).getReportItem( );
					}
				}
			}

		}
		catch ( ExtendedElementException e )
		{
			// TODO Auto-generated catch block
			
		}
		
		if ( cube == null
				|| ( !( cube instanceof TabularCubeHandle ) )
				|| expression.length( ) == 0 )
		{
			return new ArrayList( );
		}
		
		Iterator iter = null;

		// get cubeQueryDefn
		ICubeQueryDefinition cubeQueryDefn = null;
		DataRequestSession session = null;
		try
		{
			session = DataRequestSession.newSession( new DataSessionContext( DataSessionContext.MODE_DIRECT_PRESENTATION ) );
			cubeQueryDefn = CrosstabUIHelper.createBindingQuery( crosstab );
			iter = session.getCubeQueryUtil( )
					.getMemberValueIterator( (TabularCubeHandle) cube,
							expression,
							cubeQueryDefn );
		}
		catch ( Exception e )
		{
			// TODO Auto-generated catch block

		}
		List valueList = new ArrayList( );
		int count = 0;
		int MAX_COUNT = PreferenceFactory.getInstance( )
				.getPreferences( CrosstabPlugin.getDefault( ),
						UIUtil.getCurrentProject( ) )
				.getInt( CrosstabPlugin.PREFERENCE_FILTER_LIMIT );
		while ( iter != null && iter.hasNext( ) )
		{
			Object obj = iter.next( );
			if ( obj != null )
			{
				if ( valueList.indexOf( obj ) < 0 )
				{
					valueList.add( obj );
					if ( ++count >= MAX_COUNT )
					{
						break;
					}
				}

			}

		}
		
		if (session != null)
		{
			session.shutdown( );
		}
		return valueList;
		
	}

}

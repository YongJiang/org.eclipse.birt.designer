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

package org.eclipse.birt.report.item.crosstab.internal.ui.dialogs;

import org.eclipse.birt.report.designer.internal.ui.dialogs.ExpressionFilter;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.PropertyHandle;
import org.eclipse.birt.report.model.elements.interfaces.ICubeModel;

/**
 * 
 */

public class CrosstabAggregationExpressionProvider extends
		CrosstabBindingExpressionProvider
{

	protected void addFilterToProvider( )
	{
		this.addFilter( new ExpressionFilter( ) {

			public boolean select( Object parentElement, Object element )
			{
				// Crosstab Aggregation expression can only use measeure of
				// cube.
				if ( parentElement instanceof String )
				{
					String parent = (String) parentElement;
					if ( ExpressionFilter.CATEGORY.equals( parent ) )
					{
						if ( element instanceof String )
						{
							String elementString = (String) element;
							if ( COLUMN_BINDINGS.equals( elementString ) )
							{
								return false;
							}
						}
					}

					if ( CURRENT_CUBE.equals( parent ) )
					{
						if ( element instanceof PropertyHandle )
						{
							PropertyHandle handle = (PropertyHandle) element;
							if ( handle.getPropertyDefn( )
									.getName( )
									.equals( ICubeModel.MEASURE_GROUPS_PROP ) )
							{
								return true;
							}
							return false;
						}
					}
				}
				return true;
			}
		} );
	}

	public CrosstabAggregationExpressionProvider( DesignElementHandle handle )
	{
		super( handle );
		addFilterToProvider( );
	}

}

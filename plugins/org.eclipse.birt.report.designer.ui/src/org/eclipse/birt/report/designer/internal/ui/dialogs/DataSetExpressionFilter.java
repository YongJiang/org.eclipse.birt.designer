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

package org.eclipse.birt.report.designer.internal.ui.dialogs;

import org.eclipse.birt.report.designer.ui.dialogs.ExpressionProvider;
import org.eclipse.birt.report.designer.ui.expressions.ExpressionFilter;

/**
 * Expression filter for data set, filtering birt_objects and parameters when
 * invoking the expressiong builder.
 */

public class DataSetExpressionFilter extends ExpressionFilter
{

	/**
	 * Creates a new instance.
	 */
	public DataSetExpressionFilter( )
	{
		super( );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.dialogs.ExpressionFilter#select(java.lang.Object,
	 *      java.lang.Object)
	 */
	public boolean select( Object parentElement, Object element )
	{
		if ( ExpressionProvider.PARAMETERS.equals( element ) )
		{
			return false;
		}
		return true;
	}
}
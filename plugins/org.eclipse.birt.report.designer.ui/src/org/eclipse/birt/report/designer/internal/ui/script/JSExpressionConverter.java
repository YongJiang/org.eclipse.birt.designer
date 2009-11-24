/*******************************************************************************
 * Copyright (c) 2009 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.report.designer.internal.ui.script;

import org.eclipse.birt.core.data.ExpressionUtil;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.script.JavascriptEvalUtil;
import org.eclipse.birt.report.designer.internal.ui.expressions.AbstractExpressionConverter;
import org.eclipse.birt.report.designer.internal.ui.util.ExceptionHandler;
import org.eclipse.birt.report.model.api.elements.DesignChoiceConstants;

/**
 * JSExpressionConverter
 */
public class JSExpressionConverter extends AbstractExpressionConverter
{

	@Override
	public String getBindingExpression( String bindingName )
	{
		return ExpressionUtil.createJSRowExpression( bindingName );
	}

	@Override
	public String getCubeBindingExpression( String bindingName )
	{
		return ExpressionUtil.createJSDataExpression( bindingName );
	}

	@Override
	public String getDimensionExpression( String dimensionName,
			String levelName, String attributeName )
	{
		if ( attributeName == null )
		{
			return ExpressionUtil.createJSDimensionExpression( dimensionName,
					levelName );
		}
		return ExpressionUtil.createJSDimensionExpression( dimensionName,
				levelName,
				attributeName );
	}

	@Override
	public String getMeasureExpression( String measureName )
	{
		return ExpressionUtil.createJSMeasureExpression( measureName );
	}

	@Override
	public String getParameterExpression( String paramName )
	{
		return ExpressionUtil.createJSParameterExpression( paramName );
	}

	@Override
	public String getBinding( String expression )
	{
		try
		{
			return ExpressionUtil.getColumnBindingName( expression );
		}
		catch ( BirtException e )
		{
			ExceptionHandler.handle( e );
		}
		return null;
	}

	@Override
	public String getResultSetColumnExpression( String columnName )
	{
		return ExpressionUtil.createJSDataSetRowExpression( columnName );
	}

	@Override
	public String getConstantExpression( String value, String dataType )
	{
		if ( dataType == null || value == null )
			return null;
		if ( DesignChoiceConstants.COLUMN_DATA_TYPE_BOOLEAN.equals( dataType )
				|| DesignChoiceConstants.COLUMN_DATA_TYPE_INTEGER.equals( dataType )
				|| DesignChoiceConstants.COLUMN_DATA_TYPE_FLOAT.equals( dataType ) )
		{
			return value;
		}
		else if ( DesignChoiceConstants.COLUMN_DATA_TYPE_DECIMAL.equals( dataType ) )
		{
			return "new java.math.BigDecimal(\"" + value + "\")"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		else
		{
			return "\"" //$NON-NLS-1$
					+ JavascriptEvalUtil.transformToJsConstants( value )
					+ "\""; //$NON-NLS-1$
		}
	}
}

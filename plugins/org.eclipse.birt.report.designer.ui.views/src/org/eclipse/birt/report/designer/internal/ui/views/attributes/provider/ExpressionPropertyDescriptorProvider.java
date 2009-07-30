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

package org.eclipse.birt.report.designer.internal.ui.views.attributes.provider;

import java.util.List;

import org.eclipse.birt.report.designer.internal.ui.util.UIUtil;
import org.eclipse.birt.report.designer.ui.dialogs.ExpressionProvider;
import org.eclipse.birt.report.designer.util.DEUtil;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.Expression;
import org.eclipse.birt.report.model.api.ExpressionHandle;
import org.eclipse.birt.report.model.api.GroupElementFactory;
import org.eclipse.birt.report.model.api.GroupElementHandle;
import org.eclipse.birt.report.model.api.GroupPropertyHandle;
import org.eclipse.birt.report.model.api.ReportElementHandle;

public class ExpressionPropertyDescriptorProvider extends
		PropertyDescriptorProvider
{

	public ExpressionPropertyDescriptorProvider( String property, String element )
	{
		super( property, element );
		// TODO Auto-generated constructor stub
	}

	public boolean isEnable( )
	{
		if ( DEUtil.getInputSize( input ) != 1 )
			return false;
		else
			return true;
	}

	public ExpressionProvider getExpressionProvider( )
	{
		List lst = DEUtil.getInputElements( input );
		if ( lst != null && lst.size( ) == 1 )
		{
			DesignElementHandle elementHandle = (DesignElementHandle) lst.get( 0 );
			return new ExpressionProvider( elementHandle );
		}
		else
			return null;
	}

	public boolean isReadOnly( )
	{
		if ( DEUtil.getInputElements( input ).size( ) > 0 )
		{
			ReportElementHandle handle = (ReportElementHandle) DEUtil.getInputFirstElement( input );
			GroupPropertyHandle propertyHandle = GroupElementFactory.newGroupElement( handle.getModuleHandle( ),
					DEUtil.getInputElements( input ) )
					.getPropertyHandle( getProperty( ) );
			return propertyHandle.isReadOnly( );
		}
		return false;
	}

	public Object load( )
	{
		Object value = null;
		if ( input instanceof GroupElementHandle )
		{
			value = ( (GroupElementHandle) input ).getPropertyHandle( property )
					.getValue( );
		}
		else if ( input instanceof List )
		{
			value = DEUtil.getGroupElementHandle( (List) input )
					.getPropertyHandle( property )
					.getValue( );
		}
		if ( value instanceof String )
		{
			value = new Expression( (String) value,
					UIUtil.getDefaultScriptType( ) );
		}
		else if ( value instanceof ExpressionHandle )
		{
			value = ( (ExpressionHandle) value ).getValue( );
		}
		return value;
	}
}

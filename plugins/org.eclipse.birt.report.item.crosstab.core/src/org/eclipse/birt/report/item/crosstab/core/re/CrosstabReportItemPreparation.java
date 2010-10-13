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

package org.eclipse.birt.report.item.crosstab.core.re;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.report.engine.extension.ReportItemPreparationBase;
import org.eclipse.birt.report.item.crosstab.core.de.CrosstabReportItemHandle;
import org.eclipse.birt.report.item.crosstab.core.script.internal.handler.CrosstabPreparationHandler;
import org.eclipse.birt.report.model.api.ExtendedItemHandle;

/**
 * CrosstabReportItemPreparation
 */
public class CrosstabReportItemPreparation extends ReportItemPreparationBase
{

	@Override
	public void prepare( ) throws BirtException
	{
		if ( !( handle instanceof ExtendedItemHandle ) )
		{
			return;
		}

		CrosstabReportItemHandle crosstab = (CrosstabReportItemHandle) ( (ExtendedItemHandle) handle ).getReportItem( );

		if ( crosstab == null )
		{
			return;
		}

		ExtendedItemHandle modelHandle = (ExtendedItemHandle) crosstab.getModelHandle( );
		String javaClass = modelHandle.getEventHandlerClass( );
		String script = modelHandle.getOnPrepare( );

		if ( ( javaClass != null && javaClass.trim( ).length( ) > 0 )
				|| ( script != null && script.trim( ).length( ) > 0 ) )
		{
			// fix bug 235947, ensure engine script context is initialized at
			// this moment
			context.evaluate( "1" ); //$NON-NLS-1$
		}

		new CrosstabPreparationHandler( crosstab, context ).handle( );
	}
}

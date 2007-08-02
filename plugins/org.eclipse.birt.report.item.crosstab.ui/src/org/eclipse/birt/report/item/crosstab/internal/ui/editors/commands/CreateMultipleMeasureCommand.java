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

package org.eclipse.birt.report.item.crosstab.internal.ui.editors.commands;

import java.util.List;

import org.eclipse.birt.report.designer.internal.ui.util.ExceptionHandler;
import org.eclipse.birt.report.designer.ui.newelement.DesignElementFactory;
import org.eclipse.birt.report.item.crosstab.core.de.CrosstabCellHandle;
import org.eclipse.birt.report.item.crosstab.core.de.CrosstabReportItemHandle;
import org.eclipse.birt.report.item.crosstab.core.de.MeasureViewHandle;
import org.eclipse.birt.report.item.crosstab.internal.ui.editors.model.CrosstabAdaptUtil;
import org.eclipse.birt.report.item.crosstab.internal.ui.editors.model.CrosstabHandleAdapter;
import org.eclipse.birt.report.item.crosstab.ui.i18n.Messages;
import org.eclipse.birt.report.model.api.ComputedColumnHandle;
import org.eclipse.birt.report.model.api.DataItemHandle;
import org.eclipse.birt.report.model.api.ExtendedItemHandle;
import org.eclipse.birt.report.model.api.LabelHandle;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.eclipse.birt.report.model.api.elements.structures.ComputedColumn;
import org.eclipse.birt.report.model.api.olap.MeasureGroupHandle;
import org.eclipse.birt.report.model.api.olap.MeasureHandle;

/**
 * 
 */

public class CreateMultipleMeasureCommand extends AbstractCrosstabCommand
{

	private CrosstabHandleAdapter handleAdpter;
	// private MeasureHandle measureHandle;
	private List list;

	/**
	 * Trans name
	 */
	// private static final String NAME = "Create MeasureViewHandle";
	private static final String NAME = Messages.getString( "CreateMeasureViewCommand.TransName" );//$NON-NLS-1$

	/**
	 * Constructor
	 * 
	 * @param handleAdpter
	 * @param measureHandle
	 */
	public CreateMultipleMeasureCommand( CrosstabHandleAdapter handleAdpter,
			List list )
	{
		super( handleAdpter.getDesignElementHandle( ) );
		assert list == null;
		this.handleAdpter = handleAdpter;
		this.list = list;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#canExecute()
	 */
	public boolean canExecute( )
	{
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute( )
	{
		transStart( NAME );
		try
		{
			for ( int i = 0; i < list.size( ); i++ )
			{
				Object obj = list.get( i );
				if (obj instanceof MeasureHandle)
				{
					addMeasureHandle( (MeasureHandle)obj);
				}
				if (obj instanceof MeasureGroupHandle)
				{
					List children  = ((MeasureGroupHandle)obj).getContents( MeasureGroupHandle.MEASURES_PROP );
					for ( int j = 0; j < children.size( ); j++ )
					{
						Object temp = children.get( j );
						if (temp instanceof MeasureHandle)
						{
							addMeasureHandle( (MeasureHandle)temp);
						}
					}
				}
			}
		}
		catch ( SemanticException e )
		{
			rollBack( );
			ExceptionHandler.handle( e );
			return;
		}
		transEnd( );
	}

	private void addMeasureHandle( MeasureHandle measureHandle )
			throws SemanticException
	{
		CrosstabReportItemHandle reportHandle = (CrosstabReportItemHandle) handleAdpter.getCrosstabItemHandle( );

		if ( reportHandle.getCube( ) == null )
		{
			reportHandle.setCube( CrosstabAdaptUtil.getCubeHandle( measureHandle ) );
		}

		CrosstabAdaptUtil.addMeasureHandle( reportHandle, measureHandle, reportHandle.getMeasureCount( ) );
	}
}

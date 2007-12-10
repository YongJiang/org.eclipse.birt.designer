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

package org.eclipse.birt.report.item.crosstab.core.re.executor;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.olap.OLAPException;

import org.eclipse.birt.report.engine.content.IContent;
import org.eclipse.birt.report.engine.content.IRowContent;
import org.eclipse.birt.report.engine.extension.IReportItemExecutor;
import org.eclipse.birt.report.item.crosstab.core.i18n.Messages;

/**
 * CrosstabCornerHeaderRowExecutor
 */
public class CrosstabCornerHeaderRowExecutor extends BaseCrosstabExecutor
{

	private static Logger logger = Logger.getLogger( CrosstabCornerHeaderRowExecutor.class.getName( ) );

	private int rowSpan, colSpan;
	private int currentChangeType;
	private int currentColIndex;

	private long currentEdgePosition;

	private boolean blankStarted;
	private boolean hasLast;

	public CrosstabCornerHeaderRowExecutor( BaseCrosstabExecutor parent )
	{
		super( parent );
	}

	public IContent execute( )
	{
		IRowContent content = context.getReportContent( ).createRowContent( );

		initializeContent( content, null );

		processRowHeight( crosstabItem.getHeader( ) );

		prepareChildren( );

		return content;
	}

	private void prepareChildren( )
	{
		currentChangeType = ColumnEvent.UNKNOWN_CHANGE;
		currentColIndex = -1;

		currentEdgePosition = -1;

		blankStarted = false;

		rowSpan = 1;
		colSpan = 0;

		hasLast = false;

		walker.reload( );
	}

	public IReportItemExecutor getNextChild( )
	{
		IReportItemExecutor nextExecutor = null;

		try
		{
			while ( walker.hasNext( ) )
			{
				ColumnEvent ev = walker.next( );

				switch ( currentChangeType )
				{
					case ColumnEvent.ROW_EDGE_CHANGE :

						if ( blankStarted
								&& ev.type != ColumnEvent.ROW_EDGE_CHANGE )
						{
							nextExecutor = new CrosstabCellExecutor( this,
									crosstabItem.getHeader( ),
									rowSpan,
									colSpan,
									currentColIndex - colSpan + 1 );

							( (CrosstabCellExecutor) nextExecutor ).setPosition( currentEdgePosition );

							blankStarted = false;
							hasLast = false;
						}
						break;
				}

				if ( !blankStarted && ( ev.type == ColumnEvent.ROW_EDGE_CHANGE ) )
				{
					blankStarted = true;
					rowSpan = 1;
					colSpan = 0;
					hasLast = true;
				}

				currentEdgePosition = ev.dataPosition;

				currentChangeType = ev.type;
				colSpan++;
				currentColIndex++;

				if ( nextExecutor != null )
				{
					return nextExecutor;
				}
			}

		}
		catch ( OLAPException e )
		{
			logger.log( Level.SEVERE,
					Messages.getString( "CrosstabMeasureHeaderRowExecutor.error.generate.child.executor" ), //$NON-NLS-1$
					e );
		}

		if ( hasLast )
		{
			hasLast = false;

			// handle last column
			if ( blankStarted )
			{
				nextExecutor = new CrosstabCellExecutor( this,
						crosstabItem.getHeader( ),
						rowSpan,
						colSpan,
						currentColIndex - colSpan + 1 );

				( (CrosstabCellExecutor) nextExecutor ).setPosition( currentEdgePosition );

				blankStarted = false;
			}
		}

		return nextExecutor;
	}

	public boolean hasNextChild( )
	{
		try
		{
			return walker.hasNext( ) || hasLast;
		}
		catch ( OLAPException e )
		{
			logger.log( Level.SEVERE,
					Messages.getString( "CrosstabMeasureHeaderRowExecutor.error.check.child.executor" ), //$NON-NLS-1$
					e );
		}
		return false;
	}
}

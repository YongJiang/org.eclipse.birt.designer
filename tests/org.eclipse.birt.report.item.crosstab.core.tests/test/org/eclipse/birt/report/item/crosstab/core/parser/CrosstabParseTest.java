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

package org.eclipse.birt.report.item.crosstab.core.parser;

import java.util.List;

import org.eclipse.birt.report.item.crosstab.core.BaseTestCase;
import org.eclipse.birt.report.item.crosstab.core.ICrosstabConstants;
import org.eclipse.birt.report.item.crosstab.core.de.CrosstabReportItemHandle;
import org.eclipse.birt.report.item.crosstab.core.util.CrosstabExtendedItemFactory;
import org.eclipse.birt.report.item.crosstab.core.util.CrosstabUtil;
import org.eclipse.birt.report.model.api.ExtendedItemHandle;
import org.eclipse.birt.report.model.api.olap.CubeHandle;

/**
 * Test parse Crosstab property.
 * 
 */

public class CrosstabParseTest extends BaseTestCase
{

	/**
	 * Test parser
	 * 
	 * @throws Exception
	 */

	public void testParser( ) throws Exception
	{
		openDesign( "CrosstabParseTest.xml" );//$NON-NLS-1$

		List errors = designHandle.getErrorList( );
		assertEquals( 0, errors.size( ) );

		ExtendedItemHandle handle = (ExtendedItemHandle) designHandle.getBody( )
				.get( 0 );
		CrosstabReportItemHandle reportItemHandle = (CrosstabReportItemHandle) handle
				.getReportItem( );

		assertEquals( "hello", reportItemHandle.getCaption( ) );//$NON-NLS-1$
		assertEquals( "hello.crosstab", reportItemHandle.getCaptionKey( ) );//$NON-NLS-1$
		assertEquals( "vertical", reportItemHandle.getMeasureDirection( ) );//$NON-NLS-1$
		assertEquals( "over then down", reportItemHandle.getPageLayout( ) );//$NON-NLS-1$
		assertEquals( 1, reportItemHandle.getMeasureCount( ) );

		assertNotNull( reportItemHandle
				.getCrosstabView( ICrosstabConstants.COLUMN_AXIS_TYPE ) );
		assertNotNull( reportItemHandle
				.getCrosstabView( ICrosstabConstants.ROW_AXIS_TYPE ) );

		assertFalse( reportItemHandle.isRepeatRowHeader( ) );

		assertFalse( reportItemHandle.isRepeatColumnHeader( ) );
	}

	/**
	 * Semantic Check
	 * 
	 * @throws Exception
	 */
	public void testSemanticCheck( ) throws Exception
	{
		openDesign( "CrosstabParseTest.xml" );//$NON-NLS-1$
		List errors = designHandle.getErrorList( );

		assertEquals( 0, errors.size( ) );
	}

	/**
	 * Test Writer
	 * 
	 * @throws Exception
	 */

	public void testWriter( ) throws Exception
	{
		createDesign( );
		CubeHandle cubeHandle = prepareCube( );

		ExtendedItemHandle extendHandle = CrosstabExtendedItemFactory
				.createCrosstabReportItem( designHandle.getRoot( ), cubeHandle, null );
		designHandle.getBody( ).add( extendHandle );
		// create cross tab
		CrosstabReportItemHandle crosstabItem = (CrosstabReportItemHandle) CrosstabUtil
				.getReportItem( extendHandle );

		crosstabItem.setMeasureDirection( "vertical" );//$NON-NLS-1$
		crosstabItem.setPageLayout( "over then down" );//$NON-NLS-1$
		crosstabItem.setRepeatColumnHeader( false );
		crosstabItem.setRepeatRowHeader( false );
		save( designHandle.getRoot( ) );
		compareFile( "CrosstabParseTest_golden.xml" );//$NON-NLS-1$
	}
}
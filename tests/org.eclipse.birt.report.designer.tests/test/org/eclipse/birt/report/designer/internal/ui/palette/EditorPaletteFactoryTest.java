/*******************************************************************************
 * Copyright (c) 2004 Actuate Corporation. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Actuate Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.birt.report.designer.internal.ui.palette;

import junit.framework.TestCase;

import org.eclipse.gef.palette.PaletteRoot;

public class EditorPaletteFactoryTest extends TestCase
{

	/**
	 * Test for default palette root when the parameter is not instance of
	 * ReportGraphicalEditor
	 */
	public void testCreatePalette( )
	{
		PaletteRoot root = DesignerPaletteFactory.createPalette( );

		assertEquals( 3, root.getChildren( ).size( ) );
	}

	/**
	 * Test for default palette root when the parameter is null
	 */
	public void testCreateReportGraphicalEditorPalette( )
	{

		PaletteRoot root = MasterPagePaletteFactory.createPalette( );

		assertEquals( 4, root.getChildren( ).size( ) );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp( ) throws Exception
	{
		super.setUp( );

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown( ) throws Exception
	{
		// TODO Auto-generated method stub
		super.tearDown( );
	}
}
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

package org.eclipse.birt.report.designer.ui;

import org.eclipse.birt.report.designer.testutil.BirtUITestCase;
import org.eclipse.birt.report.designer.ui.editors.ReportEditor;
import org.eclipse.ui.IEditorPart;

/**
 * Simple test for BirtUITestCase
 */

public class SimpleUITest extends BirtUITestCase
{

	/**
	 * 
	 * Test showPerpersite method
	 * 
	 * invoke showPerpersite then check the active perspective' ID and name to
	 * test if it works correctly
	 */

	public void testShowPerspective( ) throws Throwable
	{
		showPerspective( );
		assertEquals( PERSPECTIVE_ID, tPage.getPerspective( ).getId( ) );
		assertEquals( PERSPECTIVE_NAME, tPage.getPerspective( ).getLabel( ) );
	}

	/**
	 * Test openEditor method
	 * 
	 * invoke openEditor then check the active editor's ID and title to test if
	 * it works correctly
	 */

	public void testOpenEditor( ) throws Exception
	{
		IEditorPart tPart = openEditor( );
		assertTrue( tPart instanceof ReportEditor );
		assertEquals( tPart, tPage.getActiveEditor( ) );
		assertEquals( EDITOR_ID, tPart.getSite( ).getId( ) );
		assertEquals( EDITOR_NAME, tPart.getSite( ).getRegisteredName( ) );
		assertEquals( TEST_DESIGN_FILE, tPart.getTitle( ) );
	}

	public void testGetView( )
	{
		assertEquals( DATA_EXPLORER_NAME,
				getView( DATA_EXPLORER_ID ).getTitle( ) );
	}

}
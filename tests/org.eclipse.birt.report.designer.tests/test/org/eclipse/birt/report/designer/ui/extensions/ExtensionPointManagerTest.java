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

package org.eclipse.birt.report.designer.ui.extensions;

import java.util.List;

import org.eclipse.birt.report.designer.internal.ui.extension.ExtendedElementUIPoint;
import org.eclipse.birt.report.designer.internal.ui.extension.ExtensionPointManager;
import org.eclipse.birt.report.designer.internal.ui.extension.IExtensionConstants;
import org.eclipse.birt.report.designer.tests.example.matrix.TestingMatrixUI;
import org.eclipse.birt.report.designer.testutil.BaseTestCase;
import org.eclipse.birt.report.designer.ui.ReportPlatformUIImages;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 *  
 */

public class ExtensionPointManagerTest extends BaseTestCase
{

	public static final String TestExtensionName = "TestingMatrix"; //$NON-NLS-1$

	public void testGetExtendedElementPoints( )
	{
		List list = ExtensionPointManager.getInstance( )
				.getExtendedElementPoints( );
		assertEquals( 1, list.size( ) );
		assertEquals( ExtensionPointManager.getInstance( )
				.getExtendedElementPoint( TestExtensionName ), list.get( 0 ) );
	}

	public void testGetExtendedElementPoint( )
	{
		ExtendedElementUIPoint point = ExtensionPointManager.getInstance( )
				.getExtendedElementPoint( TestExtensionName );
		assertNotNull( point );

		assertEquals( point, ExtensionPointManager.getInstance( )
				.getExtendedElementPoint( TestExtensionName ) );

		assertEquals( TestExtensionName, point.getExtensionName( ) );

		assertTrue( point.getReportItemUI( ) instanceof TestingMatrixUI );

		assertEquals( Boolean.TRUE,
				point.getAttribute( IExtensionConstants.EDITOR_SHOW_IN_DESIGNER ) );
		assertEquals( Boolean.TRUE,
				point.getAttribute( IExtensionConstants.EDITOR_SHOW_IN_MASTERPAGE ) );
		assertEquals( Boolean.FALSE,
				point.getAttribute( IExtensionConstants.EDITOR_CAN_RESIZE ) );

		String paletteIconSymbol = ReportPlatformUIImages.getIconSymbolName( TestExtensionName,
				IExtensionConstants.PALETTE_ICON );
		assertEquals( "TestCategory", //$NON-NLS-1$
				point.getAttribute( IExtensionConstants.PALETTE_CATEGORY ) );
		assertNull( point.getAttribute( IExtensionConstants.PALETTE_CATEGORY_DISPLAYNAME ) );
		assertNull( point.getAttribute( IExtensionConstants.PALETTE_ICON ) );
		assertNull( ReportPlatformUIImages.getImageDescriptor( paletteIconSymbol ) );
		assertNull( ReportPlatformUIImages.getImage( paletteIconSymbol ) );

		String outlineIconSymbol = ReportPlatformUIImages.getIconSymbolName( TestExtensionName,
				IExtensionConstants.OUTLINE_ICON );
		ImageDescriptor descriptor = ReportPlatformUIImages.getImageDescriptor( outlineIconSymbol );
		assertNotNull( point.getAttribute( IExtensionConstants.OUTLINE_ICON ) );
		assertNotNull( descriptor );
		assertEquals( descriptor,
				point.getAttribute( IExtensionConstants.OUTLINE_ICON ) );
		assertNotNull( ReportPlatformUIImages.getImage( outlineIconSymbol ) );
	}
}
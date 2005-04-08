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

package org.eclipse.birt.report.designer.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.birt.report.designer.testutil.BaseTestCase;
import org.eclipse.birt.report.model.elements.ReportDesign;
import org.eclipse.birt.report.model.elements.ReportDesignConstants;
import org.eclipse.birt.report.model.metadata.MetaDataDictionary;

/**
 * Class of test for DEUtil
 *  
 */

public class DEUtilTest extends BaseTestCase
{

	/**
	 * @param name
	 */
	public DEUtilTest( String name )
	{
		super( name );
	}

	/*
	 * Class under test for List getElementSupportList(DesignElementHandle, int)
	 */
	public void testGetElementSupportListDesignElementHandleint( )
	{
		ArrayList expected = new ArrayList( );
		MetaDataDictionary dictionary = MetaDataDictionary.getInstance( );
		expected.add( dictionary.getElement( ReportDesignConstants.LABEL_ITEM ) );
		expected.add( dictionary.getElement( ReportDesignConstants.DATA_ITEM ) );
		expected.add( dictionary.getElement( ReportDesignConstants.TABLE_ITEM ) );
		expected.add( dictionary.getElement( ReportDesignConstants.LIST_ITEM ) );
		expected.add( dictionary.getElement( ReportDesignConstants.GRID_ITEM ) );
		expected.add( dictionary.getElement( ReportDesignConstants.TEXT_ITEM ) );
		expected.add( dictionary.getElement( ReportDesignConstants.IMAGE_ITEM ) );

		List result = DEUtil.getElementSupportList( getReportDesign( ).handle( ),
				ReportDesign.BODY_SLOT );
		assertTrue( result.containsAll( expected ) );
	}
	
	public void testEscape()
	{
		String testString = "abcd\\c\"";
		assertEquals("abcd\\\\c\\\"",DEUtil.escape(testString));
		
	}

	//	public void testFindPos( ) throws SemanticException
	//	{
	//		DesignElementHandle list1 = DEUtil.addElement( getReportDesign( ).handle(
	// ),
	//				ReportDesign.BODY_SLOT,
	//				ListItem.class,
	//				"list1" );
	//
	//		DesignElementHandle child1 = DEUtil.addElement( list1,
	//				ListItem.DETAIL_SLOT,
	//				ListItem.class,
	//				"child1" );
	//
	//		DesignElementHandle child2 = DEUtil.addElement( list1,
	//				ListItem.DETAIL_SLOT,
	//				ListItem.class,
	//				"child2" );
	//
	//		assertEquals( 0, DEUtil.findPos( list1, ListItem.DETAIL_SLOT, child1 ) );
	//		assertEquals( 1, DEUtil.findPos( list1, ListItem.DETAIL_SLOT, child2 ) );
	//	}

	//	public void testGetChildrenAccount( ) throws SemanticException
	//	{
	//		DesignElementHandle list1 = DEUtil.addElement( getReportDesign( ).handle(
	// ),
	//				ReportDesign.BODY_SLOT,
	//				ListItem.class,
	//				"list1" );
	//
	//		DesignElementHandle list2 = DEUtil.addElement( getReportDesign( ).handle(
	// ),
	//				ReportDesign.BODY_SLOT,
	//				ListItem.class,
	//				"list2" );
	//
	//		DEUtil.addElement( list1,
	//				ListItem.DETAIL_SLOT,
	//				ListItem.class,
	//				"child1" );
	//
	//		DEUtil.addElement( list1,
	//				ListItem.DETAIL_SLOT,
	//				ListItem.class,
	//				"child2" );
	//
	//		assertEquals( 2,
	//				DEUtil.getChildrenAccount( list1, ListItem.DETAIL_SLOT ) );
	//		assertEquals( 0,
	//				DEUtil.getChildrenAccount( list2, ListItem.DETAIL_SLOT ) );
	//
	//	}
	//
	//	public void testGetElementName( ) throws SemanticException
	//	{
	//		DesignElementHandle list1 = DEUtil.addElement( getReportDesign( ).handle(
	// ),
	//				ReportDesign.BODY_SLOT,
	//				ListItem.class,
	//				"list1" );
	//
	//		String name = list1.toString( ).substring( list1.toString( )
	//				.lastIndexOf( "." ) + 1 );
	//		assertEquals( name, DEUtil.getElementName( list1 ) );
	//	}
	//
	//	public void testGetMasterPageAccount( ) throws SemanticException
	//	{
	//		DEUtil.addElement( getReportDesign( ).handle( ),
	//				ReportDesign.PAGE_SLOT,
	//				GraphicMasterPage.class,
	//				"Master Page" );
	//		DEUtil.addElement( getReportDesign( ).handle( ),
	//				ReportDesign.PAGE_SLOT,
	//				GraphicMasterPage.class,
	//				"Master Page2" );
	//
	//		assertEquals( 3, DEUtil.getMasterPageAccount( ) );
	//	}
	//
	//	public void testName2Class( )
	//	{
	//		assertEquals( TableItem.class, DEUtil.name2Class( "table" ) );
	//		assertEquals( TableItem.class, DEUtil.name2Class( TableItem.class ) );
	//		assertEquals( null, DEUtil.name2Class( "banian" ) );
	//
	//	}
	//
	//	public void testGetDefaultSlotID( ) throws SemanticException
	//	{
	//		assertEquals( ReportDesign.BODY_SLOT,
	//				DEUtil.getDefaultSlotID( getReportDesign( ).handle( ) ) );
	//
	//		DesignElementHandle parameterGroup = DEUtil.addElement( getReportDesign(
	// ).handle( ),
	//				ReportDesign.PARAMETER_SLOT,
	//				ParameterGroup.class,
	//				"Parameter Group" );
	//		assertEquals( ParameterGroup.PARAMETERS_SLOT,
	//				DEUtil.getDefaultSlotID( parameterGroup ) );
	//
	//		DesignElementHandle masterpage = DEUtil.addElement( getReportDesign(
	// ).handle( ),
	//				ReportDesign.PAGE_SLOT,
	//				GraphicMasterPage.class,
	//				"Master Page" );
	//		assertEquals( GraphicMasterPage.CONTENT_SLOT,
	//				DEUtil.getDefaultSlotID( masterpage ) );
	//
	//		DesignElementHandle table = DEUtil.addElement( getReportDesign( ).handle(
	// ),
	//				ReportDesign.BODY_SLOT,
	//				TableItem.class,
	//				"table" );
	//		// DesignElement row =
	//		// DEUtil.addElement(table,TableItem.DETAIL_SLOT,Row.class,
	//		// "row");
	//		// DesignElement cell = DEUtil.addElement(row,Row)
	//
	//		// DesignElement cell = DEUtil.addElement(table,
	//		// TableItem.DETAIL_SLOT,Cell.class,"cell");
	//		// assertEquals(DEUtil.getDefaultSlotID(cell),Cell.CONTENT_SLOT);
	//
	//		assertEquals( -1, DEUtil.getDefaultSlotID( table ) );
	//
	//	}
	//
	//	public void testFindSlotID( ) throws SemanticException
	//	{
	//		DesignElementHandle list1 = DEUtil.addElement( getReportDesign( ).handle(
	// ),
	//				ReportDesign.BODY_SLOT,
	//				ListItem.class,
	//				"list1" );
	//
	//		DesignElementHandle child1 = DEUtil.addElement( list1,
	//				ListItem.DETAIL_SLOT,
	//				ListItem.class,
	//				"child1" );
	//
	//		assertEquals( ListItem.DETAIL_SLOT, DEUtil.findSlotID( list1, child1 ) );
	//		assertEquals( -1, DEUtil.findSlotID( child1, list1 ) );
	//	}
	//
	//	public void testGetSlotID( ) throws SemanticException
	//	{
	//		DesignElementHandle list1 = DEUtil.addElement( getReportDesign( ).handle(
	// ),
	//				ReportDesign.BODY_SLOT,
	//				ListItem.class,
	//				"list1" );
	//
	//		DesignElementHandle child1 = DEUtil.addElement( list1,
	//				ListItem.DETAIL_SLOT,
	//				ListItem.class,
	//				"child1" );
	//
	//		DesignElementHandle table = DEUtil.addElement( getReportDesign( ).handle(
	// ),
	//				ReportDesign.BODY_SLOT,
	//				TableItem.class,
	//				"table" );
	//		assertEquals( ListItem.DETAIL_SLOT, DEUtil.getSlotID( list1, child1 ) );
	//		assertEquals( -1, DEUtil.getSlotID( list1, null ) );
	//		assertEquals( -1, DEUtil.getSlotID( table, list1 ) );
	//
	//	}
	//
	//	public void testFindInsertPosition( ) throws SemanticException
	//	{
	//		DesignElementHandle list1 = DEUtil.addElement( getReportDesign( ).handle(
	// ),
	//				ReportDesign.BODY_SLOT,
	//				ListItem.class,
	//				"list1" );
	//
	//		assertEquals( 1,
	//				DEUtil.findInsertPosition( getReportDesign( ).handle( ), null ) );
	//		assertEquals( 0,
	//				DEUtil.findInsertPosition( getReportDesign( ).handle( ), list1 ) );
	//
	//	}

}
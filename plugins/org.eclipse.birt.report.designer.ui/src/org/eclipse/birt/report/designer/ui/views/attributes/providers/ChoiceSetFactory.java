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

package org.eclipse.birt.report.designer.ui.views.attributes.providers;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.birt.report.designer.core.model.SessionHandleAdapter;
import org.eclipse.birt.report.model.api.DataSetHandle;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.ReportElementHandle;
import org.eclipse.birt.report.model.api.SlotHandle;
import org.eclipse.birt.report.model.elements.DesignChoiceConstants;
import org.eclipse.birt.report.model.elements.GroupElement;
import org.eclipse.birt.report.model.elements.MasterPage;
import org.eclipse.birt.report.model.elements.ReportDesign;
import org.eclipse.birt.report.model.elements.Style;
import org.eclipse.birt.report.model.elements.structures.FilterCondition;
import org.eclipse.birt.report.model.elements.structures.SortKey;
import org.eclipse.birt.report.model.metadata.Choice;
import org.eclipse.birt.report.model.metadata.ChoiceSet;
import org.eclipse.birt.report.model.metadata.ColorPropertyType;
import org.eclipse.birt.report.model.metadata.IElementPropertyDefn;
import org.eclipse.birt.report.model.metadata.IPropertyDefn;
import org.eclipse.birt.report.model.metadata.MetaDataDictionary;
import org.eclipse.birt.report.model.metadata.PropertyType;

/**
 * ChoiceSetFactory provides common interface to access all kinds of collection
 * on given property.
 */

public class ChoiceSetFactory
{

	public static final String CHOICE_NONE = "None"; //$NON-NLS-1$

	/**
	 * Gets the collection that given property value can selected from them.
	 * 
	 * @param property
	 *            DE Property key.
	 * @return The ChoiceSet instance contains all the allowed values.
	 * @deprecated Use getDEChoiceSet( String property ,String elementName)
	 *             instead
	 */
	public static ChoiceSet getDEChoiceSet( String property )
	{
		String unitKey = DesignChoiceConstants.CHOICE_UNITS;
		if ( AttributeConstant.BACKGROUND_COLOR.equals( property ) )
		{
			unitKey = ColorPropertyType.COLORS_CHOICE_SET;
		}
		else if ( AttributeConstant.FONT_COLOR.equals( property ) )
		{
			unitKey = ColorPropertyType.COLORS_CHOICE_SET;
		}
		else if ( AttributeConstant.FONT_SIZE.equals( property ) )
		{
			unitKey = DesignChoiceConstants.CHOICE_FONT_SIZE;
		}
		else if ( AttributeConstant.FONT_FAMILY.equals( property ) )
		{
			unitKey = DesignChoiceConstants.CHOICE_FONT_FAMILY;
		}
		else if ( AttributeConstant.TEXT_FORMAT.equals( property ) )
		{
			unitKey = DesignChoiceConstants.CHOICE_CONTENT_TYPE;
		}
		else if ( AttributeConstant.BORDER_STYLE.equals( property ) )
		{
			unitKey = DesignChoiceConstants.CHOICE_LINE_STYLE;
		}
		else if ( AttributeConstant.BORDER_WIDTH.equals( property ) )
		{
			unitKey = DesignChoiceConstants.CHOICE_LINE_WIDTH;
		}
		else if ( SortKey.DIRECTION_MEMBER.equals( property ) )
		{
			unitKey = DesignChoiceConstants.CHOICE_SORT_DIRECTION;
		}
		else if ( FilterCondition.OPERATOR_MEMBER.equals( property ) )
		{
			unitKey = DesignChoiceConstants.CHOICE_FILTER_OPERATOR;
		}
		else if ( Style.VERTICAL_ALIGN_PROP.equals( property ) )
		{
			unitKey = DesignChoiceConstants.CHOICE_VERTICAL_ALIGN;
		}
		else if ( Style.TEXT_ALIGN_PROP.equals( property ) )
		{
			unitKey = DesignChoiceConstants.CHOICE_TEXT_ALIGN;
		}
		else if ( MasterPage.ORIENTATION_PROP.equals( property ) )
		{
			unitKey = DesignChoiceConstants.CHOICE_PAGE_ORIENTATION;
		}
		else if ( MasterPage.TYPE_PROP.equals( property ) )
		{
			unitKey = DesignChoiceConstants.CHOICE_PAGE_SIZE;
		}
		else if ( GroupElement.INTERVAL_PROP.equals( property ) )
		{
			unitKey = DesignChoiceConstants.CHOICE_INTERVAL;
		}
		else if ( Style.PAGE_BREAK_BEFORE_PROP.equals( property ) )
		{
			unitKey = DesignChoiceConstants.CHOICE_PAGE_BREAK;
		}
		else if ( Style.PAGE_BREAK_AFTER_PROP.equals( property ) )
		{
			unitKey = DesignChoiceConstants.CHOICE_PAGE_BREAK;
		}
		else if ( Style.PAGE_BREAK_INSIDE_PROP.equals( property ) )
		{
			unitKey = DesignChoiceConstants.CHOICE_PAGE_BREAK_INSIDE;
		}
		return MetaDataDictionary.getInstance( ).getChoiceSet( unitKey );

	}

	/**
	 * Gets the collection that given property value can selected from them.
	 * 
	 * @param elementName
	 *            The name of the element.
	 * @param property
	 *            DE Property key.
	 * @return The ChoiceSet instance contains all the allowed values.
	 */
	public static ChoiceSet getElementChoiceSet( String elementName,
			String property )
	{
		MetaDataDictionary metaData = MetaDataDictionary.getInstance( );
		IElementPropertyDefn propertyDefn = metaData.getElement( elementName )
				.getProperty( property );
		if ( propertyDefn.getTypeCode( ) == PropertyType.DIMENSION_TYPE
				&& propertyDefn.getChoices( ) != null )
		{
			return propertyDefn.getChoices( );
		}
		return propertyDefn.getAllowedChoices( );
	}

	/**
	 * Gets the dimension collection that given property value can selected from
	 * them.
	 * 
	 * @param elementName
	 *            The name of the element.
	 * @param property
	 *            DE Property key.
	 * @return The ChoiceSet instance contains all the allowed values.
	 */
	public static ChoiceSet getDimensionChoiceSet( String elementName,
			String property )
	{
		MetaDataDictionary metaData = MetaDataDictionary.getInstance( );
		IElementPropertyDefn propertyDefn = metaData.getElement( elementName )
				.getProperty( property );
		if ( propertyDefn.getTypeCode( ) == PropertyType.DIMENSION_TYPE )
		{
			return propertyDefn.getAllowedChoices( );
		}
		return null;
	}

	/**
	 * Gets the collection that given struct property value can selected from
	 * them.
	 * 
	 * @param elementName
	 *            The name of the element.
	 * @param property
	 *            DE Property key.
	 * @return The ChoiceSet instance contains all the allowed values.
	 */
	public static ChoiceSet getStructChoiceSet( String structName,
			String property )
	{
		MetaDataDictionary metaData = MetaDataDictionary.getInstance( );
		IPropertyDefn propertyDefn = metaData.getStructure( structName )
				.findProperty( property );
		return propertyDefn.getChoices( );
	}

	/**
	 * Gets all displayNames that a given ChoiceSet instance contained.
	 * 
	 * @param choiceSet
	 *            The ChoiceSet instance.
	 * @return A String array contains displayNames.
	 */
	public static String[] getDisplayNamefromChoiceSet( ChoiceSet choiceSet )
	{
		String[] displayNames = new String[0];
		if ( choiceSet == null )
			return displayNames;
		Choice[] choices = choiceSet.getChoices( );
		if ( choices == null )
			return displayNames;

		displayNames = new String[choices.length];
		for ( int i = 0; i < choices.length; i++ )
		{
			displayNames[i] = choices[i].getDisplayName( );
		}
		return displayNames;
	}

	/**
	 * Gets property 's display names given element name and the property name.
	 * 
	 * @param elemenName
	 *            The design element name.
	 * @param property
	 *            The property name.
	 * @return The given property 's display names
	 */
	public static String[] getPropertyDisplayNames( String elementName,
			String property )
	{
		ChoiceSet choiceSet = getElementChoiceSet( elementName, property );
		return getDisplayNamefromChoiceSet( choiceSet );
	}

	/**
	 * Gets property 's display name given element name, property name and the
	 * property's value.
	 * 
	 * @param elemenName
	 *            The design element name.
	 * @param property
	 *            The property name.
	 * @param valule
	 *            The property 's value.
	 * @return The given property 's display name
	 */
	public static String getPropDisplayName( String elementName,
			String property, String value )
	{
		ChoiceSet set = getElementChoiceSet( elementName, property );
		return getDisplayNameFromChoiceSet( value, set );
	}

	/**
	 * Gets property value given element name, property name and its the
	 * property's display name.
	 * 
	 * @param elemenName
	 *            The design element name.
	 * @param property
	 *            The property name.
	 * @param displayName
	 *            The property 's display name.
	 * @return The given property 's value
	 */
	public static String getPropValue( String elementName, String property,
			String displayName )
	{
		ChoiceSet set = getElementChoiceSet( elementName, property );
		return getValueFromChoiceSet( displayName, set );
	}

	/**
	 * Gets UI display name from a choice set given the the value and the choice
	 * set name.
	 * 
	 * @param value
	 *            The value corresponding to the display name.
	 * @param set
	 *            The choice set name from which to get display name.
	 * @return The display name of the given value
	 */
	public static String getDisplayNameFromChoiceSet( String value,
			ChoiceSet set )
	{
		String name = value;
		if ( set == null )
		{
			return name;
		}
		Choice[] choices = set.getChoices( );
		if ( choices == null )
		{
			return name;
		}
		for ( int i = 0; i < choices.length; i++ )
		{
			if ( choices[i].getName( ).equals( value ) )
			{
				return (String) choices[i].getDisplayName( );
			}
		}
		return name;
	}

	/**
	 * Gets the value from a choice set given the UI display name and the choice
	 * set name.
	 * 
	 * @param displayName
	 *            The UI display name corresponding to the value.
	 * @param set
	 *            The choice set name from which to get property value.
	 * @return The value of the given UI display name.
	 */
	public static String getValueFromChoiceSet( String displayName,
			ChoiceSet set )
	{
		String value = displayName;
		if ( set == null )
		{
			return value;
		}
		Choice[] choices = set.getChoices( );
		if ( choices == null )
		{
			return value;
		}
		for ( int i = 0; i < choices.length; i++ )
		{
			if ( choices[i].getDisplayName( ).equals( displayName ) )
			{
				return (String) choices[i].getName( );
			}
		}
		return value;
	}

	/**
	 * Gets the collection that given property value can selected from them.
	 * 
	 * @param property
	 *            DE Property key.
	 * @return A String array contains all the allowed values.
	 * @deprecated Use getDEChoiceSet( String property ,String elementName)
	 *             instead
	 */

	public static Object[] getChoiceSet( String property )
	{
		//The dataSet has different access method.
		if ( AttributeConstant.DATASET.equals( property ) )
		{
			return getDataSets( );
		}

		String unitKey = DesignChoiceConstants.CHOICE_UNITS;
		if ( AttributeConstant.BACKGROUND_COLOR.equals( property ) )
		{
			unitKey = ColorPropertyType.COLORS_CHOICE_SET;
		}
		else if ( AttributeConstant.FONT_COLOR.equals( property ) )
		{
			unitKey = ColorPropertyType.COLORS_CHOICE_SET;
		}
		else if ( AttributeConstant.FONT_SIZE.equals( property ) )
		{
			unitKey = DesignChoiceConstants.CHOICE_FONT_SIZE;
		}
		else if ( AttributeConstant.FONT_FAMILY.equals( property ) )
		{
			unitKey = DesignChoiceConstants.CHOICE_FONT_FAMILY;
		}
		else if ( AttributeConstant.TEXT_FORMAT.equals( property ) )
		{
			unitKey = DesignChoiceConstants.CHOICE_CONTENT_TYPE;
		}
		else if ( AttributeConstant.BORDER_STYLE.equals( property ) )
		{
			unitKey = DesignChoiceConstants.CHOICE_LINE_STYLE;
		}
		else if ( AttributeConstant.BORDER_WIDTH.equals( property ) )
		{
			unitKey = DesignChoiceConstants.CHOICE_LINE_WIDTH;
		}
		else if ( SortKey.DIRECTION_MEMBER.equals( property ) )
		{
			unitKey = DesignChoiceConstants.CHOICE_SORT_DIRECTION;
		}
		else if ( FilterCondition.OPERATOR_MEMBER.equals( property ) )
		{
			unitKey = DesignChoiceConstants.CHOICE_FILTER_OPERATOR;
		}
		else if ( Style.VERTICAL_ALIGN_PROP.equals( property ) )
		{
			unitKey = DesignChoiceConstants.CHOICE_VERTICAL_ALIGN;
		}
		else if ( Style.TEXT_ALIGN_PROP.equals( property ) )
		{
			unitKey = DesignChoiceConstants.CHOICE_TEXT_ALIGN;
		}
		else if ( MasterPage.ORIENTATION_PROP.equals( property ) )
		{
			unitKey = DesignChoiceConstants.CHOICE_PAGE_ORIENTATION;
		}
		else if ( MasterPage.TYPE_PROP.equals( property ) )
		{
			unitKey = DesignChoiceConstants.CHOICE_PAGE_SIZE;
		}
		else if ( GroupElement.INTERVAL_PROP.equals( property ) )
		{
			unitKey = DesignChoiceConstants.CHOICE_INTERVAL;
		}
		else if ( Style.PAGE_BREAK_BEFORE_PROP.equals( property ) )
		{
			unitKey = DesignChoiceConstants.CHOICE_PAGE_BREAK;
		}
		else if ( Style.PAGE_BREAK_AFTER_PROP.equals( property ) )
		{
			unitKey = DesignChoiceConstants.CHOICE_PAGE_BREAK;
		}
		else if ( Style.PAGE_BREAK_INSIDE_PROP.equals( property ) )
		{
			unitKey = DesignChoiceConstants.CHOICE_PAGE_BREAK_INSIDE;
		}
		return getUnitChoiceSet( unitKey );
	}

	/**
	 * Gets the collection that DE provides.
	 * 
	 * @param unitKey
	 *            Choice type key.
	 * @return A String array contains all the allowed values.
	 */

	private static Object[] getUnitChoiceSet( String unitKey )
	{
		ArrayList list = new ArrayList( );
		ChoiceSet choiceSet = MetaDataDictionary.getInstance( )
				.getChoiceSet( unitKey );
		if ( choiceSet != null )
		{
			Choice[] choices = choiceSet.getChoices( );
			for ( int i = 0; i < choices.length; i++ )
			{
				list.add( choices[i] );
			}
		}
		return (Choice[]) list.toArray( new Choice[0] );
	}

	/**
	 * Gets all the DataSets available.
	 * 
	 * @return A String array contains all the DataSets.
	 */
	public static String[] getDataSets( )
	{
		ArrayList list = new ArrayList( );
		ReportDesign design = SessionHandleAdapter.getInstance( )
				.getReportDesign( );
		ReportDesignHandle handle = (ReportDesignHandle) design.getHandle( design );
		SlotHandle dataSets = handle.getDataSets( );
		if ( dataSets != null )
		{
			Iterator iterator = dataSets.iterator( );
			while ( iterator.hasNext( ) )
			{
				DataSetHandle DataSetHandle = (DataSetHandle) iterator.next( );
				list.add( DataSetHandle.getName( ) );
			}
		}
		return (String[]) list.toArray( new String[0] );
	}

	/**
	 * Gets all the MasterPages available.
	 * 
	 * @return A String array contains all the MasterPages.
	 */
	public static String[] getMasterPages( )
	{
		ArrayList list = new ArrayList( );
		ReportDesign design = SessionHandleAdapter.getInstance( )
				.getReportDesign( );
		ReportDesignHandle handle = (ReportDesignHandle) design.getHandle( design );
		SlotHandle pages = handle.getMasterPages( );
		if ( pages != null )
		{
			Iterator iterator = pages.iterator( );
			while ( iterator.hasNext( ) )
			{
				ReportElementHandle elementHandle = (ReportElementHandle) iterator.next( );
				list.add( elementHandle.getName( ) );
			}
		}
		return (String[]) list.toArray( new String[0] );
	}

	/**
	 * Gets all the Styles available.
	 * 
	 * @return A String array contains all the Styles.
	 */
	public static String[] getStyles( )
	{
		ArrayList list = new ArrayList( );
		list.add( CHOICE_NONE );
		ReportDesign design = SessionHandleAdapter.getInstance( )
				.getReportDesign( );
		ReportDesignHandle handle = (ReportDesignHandle) design.getHandle( design );
		SlotHandle styles = handle.getStyles( );
		if ( styles != null )
		{
			Iterator iterator = styles.iterator( );
			while ( iterator.hasNext( ) )
			{
				ReportElementHandle elementHandle = (ReportElementHandle) iterator.next( );
				list.add( elementHandle.getName( ) );
			}
		}
		return (String[]) list.toArray( new String[0] );
	}
}
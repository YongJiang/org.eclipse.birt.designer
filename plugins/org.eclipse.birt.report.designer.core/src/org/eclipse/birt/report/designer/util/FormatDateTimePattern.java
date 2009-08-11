/*************************************************************************************
 * Copyright (c) 2004 Actuate Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Actuate Corporation - Initial implementation.
 ************************************************************************************/

package org.eclipse.birt.report.designer.util;

import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.model.api.elements.DesignChoiceConstants;

import com.ibm.icu.util.ULocale;

public class FormatDateTimePattern
{

	public static final String DATETIEM_FORMAT_TYPE_YEAR = "datetiem_format_type_year"; //$NON-NLS-1$
	public static final String DATETIEM_FORMAT_TYPE_SHORT_YEAR = "datetiem_format_type_short_year"; //$NON-NLS-1$
	public static final String DATETIEM_FORMAT_TYPE_LONG_MONTH_YEAR = "datetiem_format_type_long_month_year"; //$NON-NLS-1$
	public static final String DATETIEM_FORMAT_TYPE_SHOT_MONTH_YEAR = "datetiem_format_type_shot_month_year"; //$NON-NLS-1$
	public static final String DATETIEM_FORMAT_TYPE_MONTH = "datetiem_format_type_month"; //$NON-NLS-1$
	public static final String DATETIEM_FORMAT_TYPE_LONG_DAY_OF_WEEK = "datetiem_format_type_long_day_of_week"; //$NON-NLS-1$
	public static final String DATETIEM_FORMAT_TYPE_DAY_OF_MONTH = "datetiem_format_type_day_of_month"; //$NON-NLS-1$
	public static final String DATETIEM_FORMAT_TYPE_MEDIUM_DAY_OF_YEAR = "datetiem_format_type_medium_day_of_year"; //$NON-NLS-1$
	public static final String DATETIEM_FORMAT_TYPE_MINUTES = "datetiem_format_type_minutes"; //$NON-NLS-1$
	public static final String DATETIEM_FORMAT_TYPE_SECONTDS = "datetiem_format_type_secontds"; //$NON-NLS-1$
	public static final String DATETIEM_FORMAT_TYPE_GENERAL_TIME = "datetiem_format_type_general_time"; //$NON-NLS-1$

	private static final String[] customCategories = new String[]{
			DATETIEM_FORMAT_TYPE_YEAR,
			DATETIEM_FORMAT_TYPE_SHORT_YEAR,
			DATETIEM_FORMAT_TYPE_LONG_MONTH_YEAR,
			DATETIEM_FORMAT_TYPE_SHOT_MONTH_YEAR,
			DATETIEM_FORMAT_TYPE_MONTH,
			DATETIEM_FORMAT_TYPE_LONG_DAY_OF_WEEK,
			DATETIEM_FORMAT_TYPE_DAY_OF_MONTH,
			DATETIEM_FORMAT_TYPE_MEDIUM_DAY_OF_YEAR,
			DATETIEM_FORMAT_TYPE_MINUTES,
			DATETIEM_FORMAT_TYPE_SECONTDS,
			DATETIEM_FORMAT_TYPE_GENERAL_TIME,
	};

	public static String[] getCustormPatternCategorys( )
	{
		return customCategories;
	}

	public static String getDisplayName4CustomCategory( String custormCategory )
	{
		return Messages.getString( "FormatDateTimePattern." + custormCategory ); //$NON-NLS-1$
	}

	public static String getCustormFormatPattern( String custormCategory,
			ULocale locale )
	{
		if ( locale == null )
		{
			locale = ULocale.getDefault( );
		}

		if ( DATETIEM_FORMAT_TYPE_GENERAL_TIME.equals( custormCategory ) )
		{
			return Messages.getString( "FormatDateTimePattern.pattern." + DATETIEM_FORMAT_TYPE_GENERAL_TIME, locale.toLocale( ) ); //$NON-NLS-1$
		}
		if ( DATETIEM_FORMAT_TYPE_YEAR.equals( custormCategory ) )
		{
			return Messages.getString( "FormatDateTimePattern.pattern." + DATETIEM_FORMAT_TYPE_YEAR, locale.toLocale( ) ); //$NON-NLS-1$
		}
		if ( DATETIEM_FORMAT_TYPE_SHORT_YEAR.equals( custormCategory ) )
		{
			return Messages.getString( "FormatDateTimePattern.pattern." + DATETIEM_FORMAT_TYPE_SHORT_YEAR, locale.toLocale( ) ); //$NON-NLS-1$
		}
		if ( DATETIEM_FORMAT_TYPE_LONG_MONTH_YEAR.equals( custormCategory ) )
		{
			return Messages.getString( "FormatDateTimePattern.pattern." + DATETIEM_FORMAT_TYPE_LONG_MONTH_YEAR, locale.toLocale( ) ); //$NON-NLS-1$
		}
		if ( DATETIEM_FORMAT_TYPE_SHOT_MONTH_YEAR.equals( custormCategory ) )
		{
			return Messages.getString( "FormatDateTimePattern.pattern." + DATETIEM_FORMAT_TYPE_SHOT_MONTH_YEAR, locale.toLocale( ) ); //$NON-NLS-1$
		}
		if ( DATETIEM_FORMAT_TYPE_MONTH.equals( custormCategory ) )
		{
			return Messages.getString( "FormatDateTimePattern.pattern." + DATETIEM_FORMAT_TYPE_MONTH, locale.toLocale( ) ); //$NON-NLS-1$
		}
		if ( DATETIEM_FORMAT_TYPE_LONG_DAY_OF_WEEK.equals( custormCategory ) )
		{
			return Messages.getString( "FormatDateTimePattern.pattern." + DATETIEM_FORMAT_TYPE_LONG_DAY_OF_WEEK, locale.toLocale( ) ); //$NON-NLS-1$
		}
		if ( DATETIEM_FORMAT_TYPE_DAY_OF_MONTH.equals( custormCategory ) )
		{
			return Messages.getString( "FormatDateTimePattern.pattern." + DATETIEM_FORMAT_TYPE_DAY_OF_MONTH, locale.toLocale( ) ); //$NON-NLS-1$
		}
		if ( DATETIEM_FORMAT_TYPE_MEDIUM_DAY_OF_YEAR.equals( custormCategory ) )
		{
			return Messages.getString( "FormatDateTimePattern.pattern." + DATETIEM_FORMAT_TYPE_MEDIUM_DAY_OF_YEAR, locale.toLocale( ) ); //$NON-NLS-1$
		}
		if ( DATETIEM_FORMAT_TYPE_MINUTES.equals( custormCategory ) )
		{
			return Messages.getString( "FormatDateTimePattern.pattern." + DATETIEM_FORMAT_TYPE_MINUTES, locale.toLocale( ) ); //$NON-NLS-1$
		}
		if ( DATETIEM_FORMAT_TYPE_SECONTDS.equals( custormCategory ) )
		{
			return Messages.getString( "FormatDateTimePattern.pattern." + DATETIEM_FORMAT_TYPE_SECONTDS, locale.toLocale( ) ); //$NON-NLS-1$
		}

		return ""; //$NON-NLS-1$
	}

	/**
	 * Retrieves format pattern from arrays given format type categorys.
	 * 
	 * @param category
	 *            Given format type category.
	 * @return The corresponding format pattern string.
	 */

	public static String getPatternForCategory( String category )
	{
		String pattern;
		if ( DesignChoiceConstants.DATETIEM_FORMAT_TYPE_GENERAL_DATE.equals( category )
				|| DesignChoiceConstants.DATE_FORMAT_TYPE_GENERAL_DATE.equals( category ) )
		{
			pattern = "General Date"; //$NON-NLS-1$
		}
		else if ( DesignChoiceConstants.DATETIEM_FORMAT_TYPE_LONG_DATE.equals( category )
				|| DesignChoiceConstants.DATE_FORMAT_TYPE_LONG_DATE.equals( category ) )
		{
			pattern = "Long Date"; //$NON-NLS-1$
		}
		else if ( DesignChoiceConstants.DATETIEM_FORMAT_TYPE_MUDIUM_DATE.equals( category )
				|| DesignChoiceConstants.DATE_FORMAT_TYPE_MUDIUM_DATE.equals( category ) )
		{
			pattern = "Medium Date"; //$NON-NLS-1$
		}
		else if ( DesignChoiceConstants.DATETIEM_FORMAT_TYPE_SHORT_DATE.equals( category )
				|| DesignChoiceConstants.DATE_FORMAT_TYPE_SHORT_DATE.equals( category ) )
		{
			pattern = "Short Date"; //$NON-NLS-1$
		}
		else if ( DesignChoiceConstants.DATETIEM_FORMAT_TYPE_LONG_TIME.equals( category )
				|| DesignChoiceConstants.TIME_FORMAT_TYPE_LONG_TIME.equals( category ) )
		{
			pattern = "Long Time"; //$NON-NLS-1$
		}
		else if ( DesignChoiceConstants.DATETIEM_FORMAT_TYPE_MEDIUM_TIME.equals( category )
				|| DesignChoiceConstants.TIME_FORMAT_TYPE_MEDIUM_TIME.equals( category ) )
		{
			pattern = "Medium Time"; //$NON-NLS-1$
		}
		else if ( DesignChoiceConstants.DATETIEM_FORMAT_TYPE_SHORT_TIME.equals( category )
				|| DesignChoiceConstants.TIME_FORMAT_TYPE_SHORT_TIME.equals( category ) )
		{
			pattern = "Short Time"; //$NON-NLS-1$
		}
		else
		{
			// default, unformatted.
			pattern = ""; //$NON-NLS-1$
		}
		return pattern;
	}
}
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

import org.eclipse.birt.report.model.api.elements.DesignChoiceConstants;

public class FormatStringPattern
{
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

		if ( DesignChoiceConstants.STRING_FORMAT_TYPE_UPPERCASE.equals( category ) )
		{
			pattern = ">"; //$NON-NLS-1$
		}
		else if ( DesignChoiceConstants.STRING_FORMAT_TYPE_LOWERCASE.equals( category ) )
		{
			pattern = "<"; //$NON-NLS-1$
		}
		else if ( DesignChoiceConstants.STRING_FORMAT_TYPE_ZIP_CODE.equals( category ) )
		{
			pattern = "@@@@@"; //$NON-NLS-1$
		}
		else if ( DesignChoiceConstants.STRING_FORMAT_TYPE_ZIP_CODE_4.equals( category ) )
		{
			pattern = "@@@@@-@@@@"; //$NON-NLS-1$
		}
		else if ( DesignChoiceConstants.STRING_FORMAT_TYPE_PHONE_NUMBER.equals( category ) )
		{
			pattern = "(@@@)@@@-@@@@"; //$NON-NLS-1$
		}
		else if ( DesignChoiceConstants.STRING_FORMAT_TYPE_SOCIAL_SECURITY_NUMBER.equals( category ) )
		{
			pattern = "@@@-@@-@@@@"; //$NON-NLS-1$
		}
		else
		{
			pattern = ""; //$NON-NLS-1$
		}
		return pattern;
	}	
}
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

package org.eclipse.birt.report.designer.tests.example.matrix;

import java.util.Locale;
import java.util.ResourceBundle;

import org.eclipse.birt.report.model.api.extension.IMessages;

/**
 * Provides the localized messages.
 */

public class MatrixMessages implements IMessages
{

	/**
	 * The resource bundle for english.
	 */
	
	private ResourceBundle englishResourceBundle = ResourceBundle.getBundle(
			"org.eclipse.birt.report.designer.tests.example.matrix.message", Locale.ENGLISH ); //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.model.extension.IMessages#getMessage(java.lang.String)
	 */

	public String getMessage( String key, Locale locale )
	{
		if ( Locale.ENGLISH.equals( locale ) )
			return englishResourceBundle.getString( key );

		return "Not supported locale"; //$NON-NLS-1$
	}
}
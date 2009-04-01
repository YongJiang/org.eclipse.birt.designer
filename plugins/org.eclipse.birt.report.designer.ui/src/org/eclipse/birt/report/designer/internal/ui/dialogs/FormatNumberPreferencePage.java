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

package org.eclipse.birt.report.designer.internal.ui.dialogs;

import java.util.logging.Level;

import org.eclipse.birt.report.designer.internal.ui.util.IHelpContextIds;
import org.eclipse.birt.report.designer.internal.ui.util.UIUtil;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.eclipse.birt.report.model.api.elements.structures.NumberFormatValue;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.ibm.icu.util.ULocale;

/**
 * Creates a preference page for number format.
 *  
 */

public class FormatNumberPreferencePage extends BaseStylePreferencePage
{

	private String name;

	private IFormatPage formatPage;

	/**
	 * Constructs a format number preference page.
	 * 
	 * @param model
	 *            The model
	 */
	public FormatNumberPreferencePage( Object model )
	{
		super( model );
		setTitle( Messages.getString( "FormatNumberPreferencePage.formatNumber.title" ) ); //$NON-NLS-1$
		setPreferenceName( NumberFormatValue.FORMAT_VALUE_STRUCT );
	}

	/**
	 * Sets the preference name.
	 */
	private void setPreferenceName( String name )
	{
		this.name = name;
	}

	/**
	 * Gets the preference name.
	 */
	public String getPreferenceName( )
	{
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#adjustGridLayout()
	 */
	protected void adjustGridLayout( )
	{
		( (GridLayout) getFieldEditorParent( ).getLayout( ) ).numColumns = 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.dialogs.BaseStylePreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors( )
	{
		super.createFieldEditors( );
		final Composite parent = getFieldEditorParent( );

		formatPage = new FormatNumberPage( parent, SWT.NULL );
		( (Composite) formatPage ).setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
		initiateFormatPage( );
		UIUtil.bindHelp( getFieldEditorParent( ).getParent( ),IHelpContextIds.STYLE_BUILDER_FORMATNUMBER_ID ); 

	}

	private void initiateFormatPage( )
	{
		String category = ( (StylePreferenceStore) getPreferenceStore( ) ).getNumberFormatCategory( );
		String pattern = ( (StylePreferenceStore) getPreferenceStore( ) ).getNumberFormat( );
		ULocale locale = ( (StylePreferenceStore) getPreferenceStore( ) ).getNumberFormatLocale( );

		formatPage.setInput( category, pattern, locale );
		return;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.dialogs.BaseStylePreferencePage#performOk()
	 */
	public boolean performOk( )
	{
		IPreferenceStore ps = getPreferenceStore( );
		if ( ps instanceof StylePreferenceStore )
		{
			( (StylePreferenceStore) ps ).clearError( );
		}
		boolean rt = doStore( );
		if ( ps instanceof StylePreferenceStore )
		{
			return !( (StylePreferenceStore) ps ).hasError( );
		}
		return rt;
	}

	/**
	 * Stores the result pattern string into Preference Store.
	 * 
	 * @return
	 */
	protected boolean doStore( )
	{
		if ( formatPage == null
				|| !formatPage.isFormatModified( )
				|| !formatPage.isDirty( ) )
		{
			return true;
		}
		try
		{
			( (StylePreferenceStore) getPreferenceStore( ) ).setNumberFormatCategory( formatPage.getCategory( ) );
			( (StylePreferenceStore) getPreferenceStore( ) ).setNumberFormat( formatPage.getPattern( ) );
			( (StylePreferenceStore) getPreferenceStore( ) ).setNumberFormatLocale( formatPage.getLocale( ) );
			return true;
		}
		catch ( SemanticException e )
		{
			logger.log( Level.SEVERE, e.getMessage( ), e );
			return false;
		}
	}

	protected String[] getPreferenceNames( )
	{
		return new String[0];
	}
}
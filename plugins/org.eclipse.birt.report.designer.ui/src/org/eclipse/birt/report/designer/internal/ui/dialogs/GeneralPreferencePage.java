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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.birt.report.designer.internal.ui.util.ExceptionHandler;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.designer.util.DEUtil;
import org.eclipse.birt.report.model.api.DesignEngine;
import org.eclipse.birt.report.model.api.SharedStyleHandle;
import org.eclipse.birt.report.model.api.StyleHandle;
import org.eclipse.birt.report.model.metadata.PredefinedStyle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * Provides general preference page.
 */

public class GeneralPreferencePage extends BaseStylePreferencePage
{

	private Object model;

	private Combo preName;

	private Text cusName;

	private Button preStyle;

	private Button cusStyle;

	private int selectedType = -1;

	private static final int TYPE_PREDEFINED = 0;

	private static final int TYPE_CUSTOM = 1;

	/**
	 * Default constructor.
	 * 
	 * @param model,
	 *            the model of preference page.
	 */
	public GeneralPreferencePage( Object model )
	{
		super( model );

		this.model = model;
	}

	/**
	 * @see org.eclipse.jface.preference.
	 *      FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors( )
	{
		super.createFieldEditors( );

		createStyleNameControl( );

		addField( new SeparatorFieldEditor( getFieldEditorParent( ) ) );

		BooleanFieldEditor shrink = new BooleanFieldEditor( StyleHandle.CAN_SHRINK_PROP,
				Messages.getString( ( (StyleHandle) model ).getPropertyHandle( StyleHandle.CAN_SHRINK_PROP )
						.getDefn( )
						.getDisplayNameID( ) ),
				getFieldEditorParent( ) );
		addField( shrink );

		BooleanFieldEditor blank = new BooleanFieldEditor( StyleHandle.SHOW_IF_BLANK_PROP,
				Messages.getString( ( (StyleHandle) model ).getPropertyHandle( StyleHandle.SHOW_IF_BLANK_PROP )
						.getDefn( )
						.getDisplayNameID( ) ),
				getFieldEditorParent( ) );
		addField( blank );

	}

	/**
	 *  
	 */
	private void createStyleNameControl( )
	{
		Composite nameComp = new Composite( getFieldEditorParent( ), SWT.NULL );
		GridData data = new GridData( GridData.FILL_HORIZONTAL );
		data.horizontalSpan = 2;
		nameComp.setLayoutData( data );
		nameComp.setLayout( new GridLayout( 2, false ) );

		preStyle = new Button( nameComp, SWT.RADIO );
		preStyle.setText( Messages.getString( "GeneralPreferencePage.label.predefinedStyle" ) ); //$NON-NLS-1$
		preStyle.addSelectionListener( new SelectionListener( ) {

			public void widgetDefaultSelected( SelectionEvent e )
			{
			}

			public void widgetSelected( SelectionEvent e )
			{
				setPredefinedStyle( true );
				preName.setFocus( );
				if ( preName.getSelectionIndex( ) == -1 )
				{
					preName.select( 0 );
				}
				selectedType = TYPE_PREDEFINED;
			}
		} );
		preName = new Combo( nameComp, SWT.NULL | SWT.READ_ONLY );
		data = new GridData( GridData.FILL_HORIZONTAL );
		preName.setLayoutData( data );
		preName.setItems( getPredefinedStyeNames( ) );

		cusStyle = new Button( nameComp, SWT.RADIO );
		cusStyle.setText( Messages.getString( "GeneralPreferencePage.label.customStyle" ) ); //$NON-NLS-1$
		cusStyle.addSelectionListener( new SelectionListener( ) {

			public void widgetDefaultSelected( SelectionEvent e )
			{
			}

			public void widgetSelected( SelectionEvent e )
			{
				setPredefinedStyle( false );
				cusName.setFocus( );
				selectedType = TYPE_CUSTOM;
			}
		} );

		cusName = new Text( nameComp, SWT.SINGLE | SWT.BORDER );
		data = new GridData( GridData.FILL_HORIZONTAL );
		cusName.setLayoutData( data );
		cusName.addModifyListener( new ModifyListener( ) {

			public void modifyText( ModifyEvent e )
			{
			}

		} );
	}

	private String[] getPredefinedStyeNames( )
	{
		List preStyles = DesignEngine.getMetaDataDictionary( )
				.getPredefinedStyles( );
		if ( preStyles == null )
		{
			return new String[]{};
		}
		String[] names = new String[preStyles.size( )];
		for ( int i = 0; i < preStyles.size( ); i++ )
		{
			names[i] = ( (PredefinedStyle) preStyles.get( i ) ).getName( );
		}
		Arrays.sort( names );
		return names;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#initialize()
	 */
	protected void initialize( )
	{
		if ( model instanceof StyleHandle )
		{
			if ( ( (StyleHandle) model ).isPredefined( ) )
			{
				preStyle.setSelection( true );
				setPredefinedStyle( true );
				preName.setText( ( (StyleHandle) model ).getName( ) );
			}
			else
			{
				cusStyle.setSelection( true );
				setPredefinedStyle( false );
				cusName.setText( ( (StyleHandle) model ).getName( ) );
			}
		}
		super.initialize( );
	}

	private void setPredefinedStyle( boolean b )
	{
		preName.setEnabled( b );
		cusName.setEnabled( !b );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#performOk()
	 */
	public boolean performOk( )
	{
		if ( storeName( ) )
		{
			return super.performOk( );
		}
		cusName.setFocus( );
		return false;
	}

	private boolean storeName( )
	{
		IPreferenceStore ps = getPreferenceStore( );

		( (StylePreferenceStore) ps ).clearError( );

		if ( !checkName( getName( ) ) )
		{
			return false;
		}

		getPreferenceStore( ).setValue( StyleHandle.NAME_PROP, getName( ) );

		return !( (StylePreferenceStore) ps ).hasError( );
	}

	private boolean checkName( String name )
	{
		if ( ( (StyleHandle) model ).isPredefined( )
				&& selectedType == TYPE_CUSTOM )
		{
			Iterator iterator = DEUtil.getStyles( );
			while ( iterator.hasNext( ) )
			{
				SharedStyleHandle handle = (SharedStyleHandle) iterator.next( );

				if ( handle.getName( ).equals( name ) )
				{
					ExceptionHandler.openErrorMessageBox( Messages.getString( "GeneralPreferencePage.errorMsg.duplicate.styleName" ), //$NON-NLS-1$
							Messages.getString( "GeneralPreferencePage.label.styleName" ) //$NON-NLS-1$
									+ ": \"" //$NON-NLS-1$
									+ name
									+ "\" " //$NON-NLS-1$
									+ Messages.getString( "GeneralPreferencePage.label.choose.different.name" ) ); //$NON-NLS-1$
					return false;
				}
			}
		}
		return true;
	}

	private String getName( )
	{
		if ( preStyle.getSelection( ) )
		{
			return preName.getText( );
		}
		return cusName.getText( );
	}
}
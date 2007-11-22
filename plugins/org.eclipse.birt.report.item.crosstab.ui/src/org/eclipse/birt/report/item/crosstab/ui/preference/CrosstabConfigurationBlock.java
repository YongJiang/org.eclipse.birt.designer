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


package org.eclipse.birt.report.item.crosstab.ui.preference;

import org.eclipse.birt.report.designer.internal.ui.util.PixelConverter;
import org.eclipse.birt.report.designer.ui.preferences.IStatusChangeListener;
import org.eclipse.birt.report.designer.ui.preferences.OptionsConfigurationBlock;
import org.eclipse.birt.report.designer.ui.preferences.PreferenceFactory;
import org.eclipse.birt.report.designer.ui.preferences.StatusInfo;
import org.eclipse.birt.report.item.crosstab.plugin.CrosstabPlugin;
import org.eclipse.birt.report.item.crosstab.ui.i18n.Messages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

/**
 */
public class CrosstabConfigurationBlock extends OptionsConfigurationBlock
{

	private static final Key PREF_FILTER_LIMIT = getKey( CrosstabPlugin.ID,
			CrosstabPlugin.PREFERENCE_FILTER_LIMIT );
	private static final Key PREF_CUBE_BUILDER_WARNING = getKey( CrosstabPlugin.ID,
			CrosstabPlugin.CUBE_BUILDER_WARNING_PREFERENCE );
	private static final Key PREF_AUTO_DEL_BINDINGS = getKey( CrosstabPlugin.ID,
			CrosstabPlugin.PREFERENCE_AUTO_DEL_BINDINGS );
	private static final String ENABLED = MessageDialogWithToggle.PROMPT;
	private static final String DISABLED = MessageDialogWithToggle.NEVER;
	private static final int MAX_FILTER_LIMIT = 10000;
	private PixelConverter fPixelConverter;

	public CrosstabConfigurationBlock( IStatusChangeListener context,
			IProject project )
	{
		super( context,
				PreferenceFactory.getInstance( )
						.getPreferences( CrosstabPlugin.getDefault( ), project ),
				project,
				getKeys( ) );
	}

	private static Key[] getKeys( )
	{
		Key[] keys = new Key[]{
				PREF_FILTER_LIMIT,
				PREF_AUTO_DEL_BINDINGS,
				PREF_CUBE_BUILDER_WARNING
		};
		return keys;
	}

	/*
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(Composite)
	 */
	protected Control createContents( Composite parent )
	{
		fPixelConverter = new PixelConverter( parent );
		setShell( parent.getShell( ) );

		Composite mainComp = new Composite( parent, SWT.NONE );
		mainComp.setFont( parent.getFont( ) );
		GridLayout layout = new GridLayout( );
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		mainComp.setLayout( layout );

		Composite othersComposite = createBuildPathTabContent( mainComp );
		GridData gridData = new GridData( GridData.FILL,
				GridData.FILL,
				true,
				true );
		gridData.heightHint = fPixelConverter.convertHeightInCharsToPixels( 20 );
		othersComposite.setLayoutData( gridData );

		validateSettings( null, null, null );

		return mainComp;
	}

	private Composite createBuildPathTabContent( Composite parent )
	{

		Composite pageContent = new Composite( parent, SWT.NONE );

		GridData data = new GridData( GridData.FILL_HORIZONTAL
				| GridData.FILL_VERTICAL
				| GridData.VERTICAL_ALIGN_BEGINNING );
		data.grabExcessHorizontalSpace = true;
		pageContent.setLayoutData( data );

		GridLayout layout = new GridLayout( );
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 3;
		pageContent.setLayout( layout );

		Group group = new Group( pageContent, SWT.NONE );
		group.setText( Messages.getString( "CrosstabPreferencePage.filterLimit" ) );
		group.setLayout( new GridLayout( 3, false ) );
		GridData gd = new GridData( GridData.FILL_HORIZONTAL );
		gd.horizontalSpan = 3;
		group.setLayoutData( gd );

		addTextField( group,
				Messages.getString( "CrosstabPreferencePage.filterLimit.prompt" ),
				PREF_FILTER_LIMIT,
				0,
				0 );

		Group promptGroup = new Group( pageContent, SWT.NONE );
		promptGroup.setText( Messages.getString( "CrosstabPreferencePage.promptGroup" ) );
		promptGroup.setLayout( new GridLayout( 3, false ) );
		gd = new GridData( GridData.FILL_HORIZONTAL );
		gd.horizontalSpan = 3;
		promptGroup.setLayoutData( gd );

		String[] enableDisableValues = new String[]{
				ENABLED, DISABLED
		};

		addCheckBox( promptGroup,
				Messages.getString( "CrosstabPreferencePage.autoDelBindings.Text" ),
				PREF_AUTO_DEL_BINDINGS,
				enableDisableValues,
				0 );

		addCheckBox( promptGroup,
				Messages.getString( "CrosstabPreferencePage.cubePopup.Text" ),
				PREF_CUBE_BUILDER_WARNING,
				enableDisableValues,
				0 );

		return pageContent;
	}

	protected void validateSettings( Key changedKey, String oldValue,
			String newValue )
	{
		fContext.statusChanged( validatePositiveNumber( getValue( PREF_FILTER_LIMIT ) ) );
	}

	protected IStatus validatePositiveNumber( final String number )
	{

		final StatusInfo status = new StatusInfo( );
		String errorMessage = Messages.getString( "CrosstabPreferencePage.Error.MaxRowInvalid", //$NON-NLS-1$
				new Object[]{
					new Integer( MAX_FILTER_LIMIT )
				} );
		if ( number.length( ) == 0 )
		{
			status.setError( errorMessage );
		}
		else
		{
			try
			{
				final int value = Integer.parseInt( number );
				if ( value < 1 || value > MAX_FILTER_LIMIT )
				{
					status.setError( errorMessage );
				}
			}
			catch ( NumberFormatException exception )
			{
				status.setError( errorMessage );
			}
		}
		return status;
	}
}

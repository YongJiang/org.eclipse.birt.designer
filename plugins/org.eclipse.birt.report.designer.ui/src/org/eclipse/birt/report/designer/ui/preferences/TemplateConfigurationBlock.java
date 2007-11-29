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

package org.eclipse.birt.report.designer.ui.preferences;

import org.eclipse.birt.report.designer.internal.ui.util.PixelConverter;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.designer.ui.ReportPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

/**
 */
public class TemplateConfigurationBlock extends OptionsConfigurationBlock
{

	private final Key PREF_TEMPLATE = getReportKey( ReportPlugin.TEMPLATE_PREFERENCE );
	private PixelConverter fPixelConverter;

	public TemplateConfigurationBlock( IStatusChangeListener context,
			IProject project )
	{
		super( context, ReportPlugin.getDefault( ), project );
		setKeys( getKeys( ) );
	}

	private Key[] getKeys( )
	{
		Key[] keys = new Key[]{
			PREF_TEMPLATE
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

	public static final String TITLE_LABEL = Messages.getString( "report.designer.ui.preferences.template.title" ); //$NON-NLS-1$ 
	public static final String TEMPLATE_IMAGE_LABEL = Messages.getString( "report.designer.ui.preferences.template.templatefolder" ); //$NON-NLS-1$
	public static final String BROWSER_BUTTON = Messages.getString( "report.designer.ui.preferences.template.select" ); //$NON-NLS-1$ 
	public static final String OPEN_DIALOG_TITLE = Messages.getString( "report.designer.ui.preferences.template.openDialogTitle" ); //$NON-NLS-1$
	//$NON-NLS-1$

	public static final String DIRCTORY = "templates"; //$NON-NLS-1$
	private Text resourceText;

	private Composite createBuildPathTabContent( Composite parent )
	{

		Label title = new Label( parent, SWT.NULL );
		title.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
		title.setText( TITLE_LABEL );
		new Label( parent, SWT.NONE );

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

		resourceText = addTextField( pageContent,
				Messages.getString( "ResourecePreferencePage.folder" ), //$NON-NLS-1$
				PREF_TEMPLATE,
				0,
				0 );
		new Label( pageContent, SWT.NONE );
		Button browser = new Button( pageContent, SWT.PUSH );
		browser.setText( BROWSER_BUTTON );
		data = new GridData( );
		browser.setLayoutData( data );
		browser.addSelectionListener( new SelectionAdapter( ) {

			public void widgetSelected( SelectionEvent event )
			{
				DirectoryDialog dialog = new DirectoryDialog( PlatformUI.getWorkbench( )
						.getDisplay( )
						.getActiveShell( ) );

				dialog.setText( OPEN_DIALOG_TITLE );
				String folderName = dialog.open( );
				if ( folderName == null )
				{
					return;
				}
				folderName = folderName.replace( '\\', '/' ); //$NON-NLS-1$ //$NON-NLS-2$ 
				if ( !folderName.endsWith( "/" ) ) //$NON-NLS-1$ 
				{
					folderName = folderName + "/"; //$NON-NLS-1$ 
				}
				resourceText.setText( folderName );
			}
		} );
		return pageContent;
	}
}

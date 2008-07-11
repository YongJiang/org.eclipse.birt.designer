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

package org.eclipse.birt.report.designer.internal.ui.views.actions;

import java.io.File;

import org.eclipse.birt.report.designer.core.model.SessionHandleAdapter;
import org.eclipse.birt.report.designer.internal.ui.util.ExceptionHandler;
import org.eclipse.birt.report.designer.internal.ui.util.UIUtil;
import org.eclipse.birt.report.designer.internal.ui.views.ReportResourceChangeEvent;
import org.eclipse.birt.report.designer.internal.ui.wizards.ExportReportWizardPage;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.designer.ui.ReportPlugin;
import org.eclipse.birt.report.designer.ui.views.IReportResourceChangeEvent;
import org.eclipse.birt.report.designer.ui.views.IReportResourceSynchronizer;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.command.LibraryChangeEvent;
import org.eclipse.birt.report.model.api.util.ElementExportUtil;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * 
 */

public class ExportToLibraryAction extends AbstractViewAction
{

	private static String windowTitle = Messages.getString( "ExportToLibraryAction.wizard.windowTitle" ); //$NON-NLS-1$

	private static final String DISPLAY_TEXT = Messages.getString( "ExportToLibraryAction.action.text" ); //$NON-NLS-1$

	/**
	 * ExportToLibraryAction preference key.
	 */
	public static final String PREF_KEY = "ExportToLibraryAction.Pref"; //$NON-NLS-1$

	/**
	 * Overwrite exist element.
	 */
	public static final int PREF_OVERWRITE = 1;

	/**
	 * Do not overwrite exist element.
	 */
	public static final int PREF_NOT_OVERWRITE = 2;

	/**
	 * Prompt
	 */
	public static final int PREF_PROMPT = 0;

	private final static String DIALOG_TITLE = Messages.getString( "ExportToLibraryAction.Dialog.Title" ); //$NON-NLS-1$
	private final static String DIALOG_MESSAGE = Messages.getString( "ExportToLibraryAction.Dialog.Message" ); //$NON-NLS-1$
	private final static String BUTTON_YES = Messages.getString( "ExportToLibraryAction.Button.Yes" ); //$NON-NLS-1$
	private final static String BUTTON_NO = Messages.getString( "ExportToLibraryAction.Button.No" ); //$NON-NLS-1$
	private final static String BUTTON_CANCEL = Messages.getString( "ExportToLibraryAction.Button.Cancel" ); //$NON-NLS-1$
	private final static String REMEMBER_DECISION = Messages.getString( "ExportToLibraryAction.Message.RememberDecision" ); //$NON-NLS-1$

	private boolean saveDecision;
	private int pref;

	public ExportToLibraryAction( Object selectedObject )
	{
		super( selectedObject, DISPLAY_TEXT );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see isEnabled()
	 */
	public boolean isEnabled( )
	{
		if ( getSelection( ) instanceof ReportDesignHandle )
		{
			return true;
		}
		else
			return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run( )
	{

		ExportReportWizard exportReportWizard = new ExportReportWizard( );
		WizardDialog wDialog = new WizardDialog( UIUtil.getDefaultShell( ),
				exportReportWizard );
		wDialog.setPageSize( 500, 250 );
		wDialog.open( );
	}

	public class ExportReportWizard extends Wizard
	{

		private ExportReportWizardPage page;

		/**
		 * 
		 */
		public ExportReportWizard( )
		{
			super( );
			setWindowTitle( windowTitle );
			page = new ExportReportWizardPage( "" ); //$NON-NLS-1$
			addPage( page );

		}

		public Image getDefaultPageImage( )
		{
			return ReportPlugin.getImage( "/icons/wizban/create_project_wizard.gif" ); //$NON-NLS-1$
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.wizard.Wizard#performFinish()
		 */

		public boolean performFinish( )
		{
			// TODO Auto-generated method stub

			try
			{
				String filename = page.getFullName( );

				if ( !filename.endsWith( ".rptlibrary" ) ) //$NON-NLS-1$
				{
					filename += ".rptlibrary"; //$NON-NLS-1$
				}
				pref = ReportPlugin.getDefault( )
						.getPreferenceStore( )
						.getInt( PREF_KEY );
				if ( filename != null )
				{

					if ( pref == PREF_PROMPT && new File( filename ).exists( ) )
					{

						MessageDialog prefDialog = new MessageDialog( UIUtil.getDefaultShell( ),
								DIALOG_TITLE,
								null,
								DIALOG_MESSAGE,
								MessageDialog.INFORMATION,
								new String[]{
										BUTTON_YES, BUTTON_NO, BUTTON_CANCEL
								},
								0 ) {

							/*
							 * (non-Javadoc)
							 * 
							 * @seeorg.eclipse.jface.dialogs.MessageDialog#
							 * createCustomArea
							 * (org.eclipse.swt.widgets.Composite)
							 */
							protected Control createCustomArea( Composite parent )
							{
								Composite container = new Composite( parent,
										SWT.NONE );
								GridLayout gridLayout = new GridLayout( );
								gridLayout.marginWidth = 20;
								// gridLayout.marginTop = 15;
								container.setLayout( gridLayout );

								Button chkbox = new Button( container,
										SWT.CHECK );
								chkbox.setText( REMEMBER_DECISION );
								chkbox.addSelectionListener( new SelectionListener( ) {

									public void widgetSelected( SelectionEvent e )
									{
										saveDecision = !saveDecision;
									}

									public void widgetDefaultSelected(
											SelectionEvent e )
									{
										saveDecision = false;
									}
								} );

								return super.createCustomArea( parent );
							}

							/*
							 * (non-Javadoc)
							 * 
							 * @see
							 * org.eclipse.jface.dialogs.MessageDialog#buttonPressed
							 * (int)
							 */
							protected void buttonPressed( int buttonId )
							{
								switch ( buttonId )
								{
									case 0 :
										pref = PREF_OVERWRITE;
										break;
									case 1 :
										pref = PREF_NOT_OVERWRITE;
										break;
									default :
										break;
								}
								if ( saveDecision )
								{
									ReportPlugin.getDefault( )
											.getPreferenceStore( )
											.setValue( PREF_KEY, pref );
								}
								super.buttonPressed( buttonId );
							}

						};
						if ( prefDialog.open( ) == 2 )
							return true;

					}
					if ( getSelection( ) instanceof ReportDesignHandle )
					{
						ElementExportUtil.exportDesign( (ReportDesignHandle) getSelection( ),
								filename,
								pref == PREF_OVERWRITE,
								true );
					}
					else
					{
						ElementExportUtil.exportElement( (DesignElementHandle) getSelection( ),
								filename,
								pref == PREF_OVERWRITE );
					}

					IReportResourceSynchronizer synchronizer = ReportPlugin.getDefault( )
							.getResourceSynchronizerService( );

					if ( synchronizer != null )
					{
						synchronizer.notifyResourceChanged( new ReportResourceChangeEvent( this,
								Path.fromOSString( filename ), IReportResourceChangeEvent.NewResource ) );
					}
				}
			}
			catch ( Exception e )
			{
				ExceptionHandler.handle( e );
			}

			return true;
		}

	}

}

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

package org.eclipse.birt.report.designer.ui.ide.wizards;

import org.eclipse.birt.report.designer.core.IReportElementConstants;
import org.eclipse.birt.report.designer.internal.ui.util.IHelpContextIds;
import org.eclipse.birt.report.designer.internal.ui.util.UIUtil;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.designer.ui.ReportPlugin;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

/**
 * Creation page for Report Wizard without Advanced control
 * 
 */
public class WizardNewReportCreationPage extends WizardNewFileCreationPage
{

	String fileExtension = IReportElementConstants.DESIGN_FILE_EXTENSION;

	/**
	 * The Constructor.
	 * 
	 * @param pageName
	 * @param selection
	 */
	public WizardNewReportCreationPage( String pageName,
			IStructuredSelection selection )
	{
		super( pageName, selection );
	}

	/**
	 * The Constructor.
	 * 
	 * @param pageName
	 * @param selection
	 * @param fileType
	 */
	public WizardNewReportCreationPage( String pageName,
			IStructuredSelection selection, String fileType )
	{
		this( pageName, selection );

		fileExtension = fileType;
	}

	/**
	 * (non-Javadoc) Method declared on IDialogPage.
	 */
	public void createControl( Composite parent )
	{
		super.createControl( parent );
		if ( fileExtension.equals( IReportElementConstants.TEMPLATE_FILE_EXTENSION ) )
		{
			UIUtil.bindHelp( getControl( ),
					IHelpContextIds.NEW_TEMPLATE_WIZARD_ID );
		}
		else if ( fileExtension.equals( IReportElementConstants.DESIGN_FILE_EXTENSION ) )
		{
			UIUtil.bindHelp( getControl( ),
					IHelpContextIds.NEW_REPORT_WIZARD_ID );
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#createAdvancedControls(org.eclipse.swt.widgets.Composite)
	 */
	protected void createAdvancedControls( Composite parent )
	{
		// does nothing here to remove the linked widget.
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#validateLinkedResource()
	 */
	protected IStatus validateLinkedResource( )
	{
		// always return OK here.
		return new Status( IStatus.OK, ReportPlugin.getDefault( )
				.getBundle( )
				.getSymbolicName( ), IStatus.OK, "", null ); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#validatePage()
	 */
	protected boolean validatePage( )
	{
		boolean rt = super.validatePage( );

		if ( rt )
		{
			String fn = getFileName( );

			if ( !Platform.getOS( ).equals( Platform.OS_WIN32 ) )
			{
				IPath resourcePath;
				if ( !fn.endsWith( "." + fileExtension ) ) //$NON-NLS-1$
				{
					resourcePath = getContainerFullPath( ).append( getFileName( )
							+ "." + fileExtension ); //$NON-NLS-1$
				}
				else
					resourcePath = getContainerFullPath( ).append( getFileName( ) );
				IWorkspace workspace = ResourcesPlugin.getWorkspace( );

				if ( workspace.getRoot( ).getFolder( resourcePath ).exists( )
						|| workspace.getRoot( )
								.getFile( resourcePath )
								.exists( ) )
				{
					setErrorMessage( Messages.getString( "WizardNewReportCreationPage.Errors.nameExists" ) ); //$NON-NLS-1$
					rt = false;
				}

			}
			else
			{
				IPath resourcePath;
				if ( !fn.toLowerCase( )
						.endsWith( ( "." + fileExtension ).toLowerCase( ) ) ) //$NON-NLS-1$
				{

					resourcePath = getContainerFullPath( ).append( getFileName( )
							+ "." + fileExtension ); //$NON-NLS-1$
				}
				else
					resourcePath = getContainerFullPath( ).append( getFileName( ) );
	
				IWorkspace workspace = ResourcesPlugin.getWorkspace( );
				if ( workspace.getRoot( ).getFolder( resourcePath ).getLocation( ).toFile( ).exists( )
						|| workspace.getRoot( )
								.getFile( resourcePath ).getLocation( ).toFile( )
								.exists( ) )
				{
					setErrorMessage( Messages.getString( "WizardNewReportCreationPage.Errors.nameExists" ) ); //$NON-NLS-1$
					rt = false;
				}
			}

		}

		return rt;
	}

	/**
	 * Get File extension
	 * 
	 * @return The file extension
	 */
	public String getFileExtension( )
	{
		return fileExtension;
	}

	/**
	 * Set file extension
	 * 
	 * @param fileExtension
	 */
	public void setFileExtension( String fileExtension )
	{
		this.fileExtension = fileExtension;
	}

}
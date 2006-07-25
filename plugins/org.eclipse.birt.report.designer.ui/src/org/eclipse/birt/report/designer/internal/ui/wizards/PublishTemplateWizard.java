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

package org.eclipse.birt.report.designer.internal.ui.wizards;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.birt.report.designer.core.model.SessionHandleAdapter;
import org.eclipse.birt.report.designer.internal.ui.util.ExceptionHandler;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.designer.ui.ReportPlugin;
import org.eclipse.birt.report.model.api.DesignFileException;
import org.eclipse.birt.report.model.api.ModuleHandle;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;

/**
 * PublishTemplateWizard
 */
public class PublishTemplateWizard extends Wizard
{

	private static final String windowTitle = Messages.getString( "PublishTemplateAction.wizard.title" ); //$NON-NLS-1$
	private static final String PAGE_TITLE = Messages.getString( "PublishTemplateAction.wizard.page.title" ); //$NON-NLS-1$
	private static final String PAGE_DESC = Messages.getString( "PublishTemplateAction.wizard.page.desc" ); //$NON-NLS-1$

	private WizardReportSettingPage page;
	private ReportDesignHandle handle;

	private static final String[] IMAGE_TYPES = new String[]{
			".bmp",
			".jpg",
			".jpeg",
			".jpe",
			".jfif",
			".gif",
			".png",
			".tif",
			".tiff",
			".ico",
			".svg"
	};

	public PublishTemplateWizard( ReportDesignHandle handle )
	{
		setWindowTitle( windowTitle );
		this.handle = handle;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	public void addPages( )
	{
		page = new WizardReportSettingPage( handle );
		page.setTitle( PAGE_TITLE );
		page.setMessage( PAGE_DESC );
		addPage( page );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	public boolean performFinish( )
	{
		// copy to template folder
		String templateFolderPath = ReportPlugin.getDefault( )
				.getTemplatePreference( );

		String filePath = handle.getFileName( );

		if ( !( new File( filePath ).exists( ) ) )
		{
			ExceptionHandler.openErrorMessageBox( Messages.getString( "PublishTemplateAction.wizard.errorTitle" ), //$NON-NLS-1$
					Messages.getString( "PublishTemplateAction.wizard.message.SourceFileNotExist" ) ); //$NON-NLS-1$
			return true;
		}

		String fileName = filePath.substring( filePath.lastIndexOf( File.separator ) + 1 );
		File targetFolder = new File( templateFolderPath );
		if ( !targetFolder.isDirectory( ) )
		{
			ExceptionHandler.openErrorMessageBox( Messages.getString( "PublishTemplateAction.wizard.errorTitle" ), //$NON-NLS-1$
					Messages.getString( "PublishTemplateAction.wizard.notvalidfolder" ) ); //$NON-NLS-1$
			return true;
		}
		if ( !targetFolder.exists( ) )
		{
			targetFolder.mkdirs( );
		}
		String targetFileName = fileName;
		if ( ReportPlugin.getDefault( ).isReportDesignFile( fileName ) )
		{
			int index = fileName.lastIndexOf( "." );
			targetFileName = fileName.substring( 0, index ) + ".rpttemplate";
		}
		File targetFile = new File( targetFolder, targetFileName );
		if ( new File( filePath ).compareTo( targetFile ) == 0 )
		{
			ExceptionHandler.openErrorMessageBox( Messages.getString( "PublishTemplateAction.wizard.errorTitle" ), //$NON-NLS-1$
					Messages.getString( "PublishTemplateAction.wizard.message" ) ); //$NON-NLS-1$
			return true;
		}

		int overwrite = Window.OK;
		try
		{
			if ( targetFile.exists( ) )
			{
				String[] buttons = new String[]{
						IDialogConstants.YES_LABEL,
						IDialogConstants.NO_LABEL,
						IDialogConstants.CANCEL_LABEL
				};
				String question = Messages.getFormattedString( "SaveAsDialog.overwriteQuestion", //$NON-NLS-1$
						new Object[]{
							targetFile.getAbsolutePath( )
						} );
				MessageDialog d = new MessageDialog( getShell( ),
						Messages.getString( "SaveAsDialog.Question" ), //$NON-NLS-1$
						null,
						question,
						MessageDialog.QUESTION,
						buttons,
						0 );
				overwrite = d.open( );
			}
			if ( overwrite == Window.OK
					&& ( targetFile.exists( ) || ( !targetFile.exists( ) && targetFile.createNewFile( ) ) ) )
			{
				copyFile( filePath, targetFile );

				try
				{
					setDesignFile( targetFile.getAbsolutePath( ) );
				}
				catch ( DesignFileException e )
				{
					ExceptionHandler.handle( e );
					return false;
				}
				catch ( SemanticException e )
				{
					ExceptionHandler.handle( e );
					return false;
				}
				catch ( IOException e )
				{
					ExceptionHandler.handle( e );
					return false;
				}
			}
		}
		catch ( IOException e )
		{
			ExceptionHandler.handle( e );
		}

		if ( overwrite == Window.OK )
		{
			if ( page.getPreviewImagePath( ) != null
					&& page.getPreviewImagePath( ).trim( ).length( ) != 0 )
			{
				copyIconFile( page.getPreviewImagePath( ).trim( ) );
			}

		}
		return overwrite != 1;
	}

	private int copyIconFile( String filePath )
	{
		String templateFolderPath = ReportPlugin.getDefault( )
				.getTemplatePreference( );

		if ( !( new File( filePath ).exists( ) ) )
		{
			ExceptionHandler.openErrorMessageBox( Messages.getString( "PublishTemplateAction.wizard.errorTitle" ), //$NON-NLS-1$
					Messages.getString( "PublishTemplateAction.wizard.message.PreviewImageNotExist" ) ); //$NON-NLS-1$
			return 1;
		}

		String fileName = filePath.substring( filePath.lastIndexOf( File.separator ) + 1 );
		File targetFolder = new File( templateFolderPath );
		String targetFileName = fileName;
		if ( !checkExtensions( fileName ) )
		{
			ExceptionHandler.openErrorMessageBox( Messages.getString( "PublishTemplateAction.wizard.errorTitle" ), //$NON-NLS-1$
					Messages.getString( "PublishTemplateAction.wizard.message.PreviewImageNotValid" ) ); //$NON-NLS-1$
			return 1;
		}
		File targetFile = new File( targetFolder, targetFileName );
		if ( new File( filePath ).compareTo( targetFile ) == 0 )
		{
			// if the two files are the same one , then do nothing.
			return 0;
		}

		int overwrite = Window.OK;
		try
		{
			if ( targetFile.exists( ) )
			{
				String[] buttons = new String[]{
						IDialogConstants.YES_LABEL,
						IDialogConstants.NO_LABEL,
						IDialogConstants.CANCEL_LABEL
				};
				String question = Messages.getFormattedString( "SaveAsDialog.overwriteQuestion", //$NON-NLS-1$
						new Object[]{
							targetFile.getAbsolutePath( )
						} );
				MessageDialog d = new MessageDialog( getShell( ),
						Messages.getString( "SaveAsDialog.Question" ), //$NON-NLS-1$
						null,
						question,
						MessageDialog.QUESTION,
						buttons,
						0 );
				overwrite = d.open( );
			}
			if ( overwrite == Window.OK
					&& ( targetFile.exists( ) || ( !targetFile.exists( ) && targetFile.createNewFile( ) ) ) )
			{
				copyFile( filePath, targetFile );
			}
		}
		catch ( IOException e )
		{
			ExceptionHandler.handle( e );
		}
		return overwrite;

	}

	/**
	 * 
	 * set ReportDesignHandle properties.
	 * 
	 * @param fileName
	 * @throws DesignFileException
	 * @throws SemanticException
	 * @throws IOException
	 */
	private void setDesignFile( String fileName ) throws DesignFileException,
			SemanticException, IOException
	{
		ReportDesignHandle handle = SessionHandleAdapter.getInstance( )
				.getSessionHandle( )
				.openDesign( fileName );
		if ( !page.getDisplayName( ).equals( "" ) ) //$NON-NLS-1$
			handle.setDisplayName( page.getDisplayName( ) );

		handle.setProperty( ModuleHandle.DESCRIPTION_PROP,
				page.getDescription( ) );

		if ( !page.getPreviewImagePath( ).equals( "" ) ) //$NON-NLS-1$
		{
			int beginIndex = page.getPreviewImagePath( )
					.lastIndexOf( File.separator );
			String shortName = null;
			if ( beginIndex + 1 >= page.getPreviewImagePath( ).length( )
					|| beginIndex == -1 )
			{
				shortName = "";
			}
			else
			{
				shortName = page.getPreviewImagePath( )
						.substring( beginIndex + 1 );

			}
			handle.setIconFile( shortName );
		}
		else
		{
			handle.setIconFile( "" );
		}
		// if ( !page.getCheetSheetPath( ).equals( "" ) ) //$NON-NLS-1$
		// handle.setCheetSheet( page.getCheetSheetPath( ) );

		handle.save( );
		handle.close( );
	}

	private void copyFile( String in, File targetFile ) throws IOException
	{
		FileInputStream fis = new FileInputStream( in );
		FileOutputStream fos = new FileOutputStream( targetFile );
		byte[] buf = new byte[1024];
		int i = 0;
		while ( ( i = fis.read( buf ) ) != -1 )
		{
			fos.write( buf, 0, i );
		}
		fis.close( );
		fos.close( );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.IWizard#canFinish()
	 */
	public boolean canFinish( )
	{
		return page.canFinish( );
	}

	private boolean checkExtensions( String fileName )
	{
		for ( int i = 0; i < IMAGE_TYPES.length; i++ )
		{
			if ( fileName.toLowerCase( ).endsWith( IMAGE_TYPES[i] ) )
			{
				return true;
			}
		}
		return false;
	}
}
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

package org.eclipse.birt.report.designer.ui.samplesview.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;

import org.eclipse.birt.report.designer.internal.ui.util.ExceptionHandler;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.designer.ui.samplesview.sampleslocator.SampleIncludedSourceEntry;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * This utility class is used to place the sample report or its resources onto
 * assigned path
 */
public class PlaceResources
{

	public static void copy( Shell shell, String path, String reportName,
			String reportDesignFile )
	{
		URL sampleReportURL = null;
		try
		{
			sampleReportURL = new URL( reportDesignFile );
		}
		catch ( IOException e )
		{
			ExceptionHandler.handle( e );
		}
		copy( shell, path, reportName, sampleReportURL );
	}

	public static void copy( Shell shell, String path, String desFileName,
			URL srcURL )
	{
		final File targetfile = new File( path, desFileName );
		if ( targetfile.exists( ) )
		{
			if ( !MessageDialog.openConfirm( shell,
					Messages.getString( "SampleReportsView.MessageDialog.Title" ),
					Messages.getFormattedString( "SampleReportsView.MessageDialog.Message",
							new Object[]{
								desFileName
							} ) ) )
			{
				return;
			}
		}

		OutputStream output = null;
		InputStream input = null;
		// URL sampleReportURL = null;
		try
		{
			output = new FileOutputStream( targetfile );
			// sampleReportURL = new URL( reportDesignFile );
			input = srcURL.openStream( );
			int offset;
			byte[] buf = new byte[1024 * 4];
			while ( ( offset = input.read( buf ) ) > -1 )
			{
				output.write( buf, 0, offset );
			}
		}
		catch ( IOException e )
		{
			ExceptionHandler.handle( e );
		}
		finally
		{
			try
			{
				input.close( );
				output.close( );
			}
			catch ( IOException e )
			{
				ExceptionHandler.handle( e );
			}
		}
	}

	public static void copyIncludedLibraries( Shell shell, String projectPath )
	{
		Enumeration enumeration = SampleIncludedSourceEntry.getIncludedLibraries( );
		while ( enumeration.hasMoreElements( ) )
		{
			URL libraryURL = (URL) enumeration.nextElement( );
			String filename = libraryURL.getFile( );
			String desFileName = filename.substring( filename.lastIndexOf( '/' ) + 1 );

			PlaceResources.copy( shell, projectPath, desFileName, libraryURL );
		}
	}
	
	public static void copyIncludedPng( Shell shell, String projectPath )
	{
		Enumeration enumeration = SampleIncludedSourceEntry.getIncludedPng( );
		while ( enumeration.hasMoreElements( ) )
		{
			URL pngURL = (URL) enumeration.nextElement( );
			String filename = pngURL.getFile( );
			String desFileName = filename.substring( filename.lastIndexOf( '/' ) + 1 );

			PlaceResources.copy( shell, projectPath, desFileName, pngURL );
		}
	}
	
	public static void copyDrillThroughReport( Shell shell, String projectPath, String report )
	{
		Enumeration enumeration = SampleIncludedSourceEntry.getDrillDetailsReports( );
		while ( enumeration.hasMoreElements( ) )
		{
			URL reportURL = (URL) enumeration.nextElement( );
			String filename = reportURL.getFile( );
			String desFileName = filename.substring( filename.lastIndexOf( '/' ) + 1 );
			if ( !desFileName.equals( report ) )
			{
				PlaceResources.copy( shell, projectPath, desFileName, reportURL );
			}			
		}
	}
}


/*******************************************************************************
 * Copyright (c) 2004, 2008 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/
package org.eclipse.birt.report.designer.data.ui.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import org.eclipse.birt.report.designer.ui.IDatasetWorkspaceClasspathFinder;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

/**
 * 
 */

public class DatasetClassPathHelper
{
	public static final String WORKSPACE_CLASSPATH_KEY = "workspace.projectclasspath"; //$NON-NLS-1$
	private static final String FINDER_BUNDLE_NAME = "org.eclipse.birt.report.debug.ui"; //$NON-NLS-1$
	private static final String FINDER_CLASSNAME = "org.eclipse.birt.report.debug.internal.ui.launcher.util.WorkspaceClassPathFinder"; //$NON-NLS-1$

	static protected boolean inDevelopmentMode = false;
	static protected String[] devDefaultClasspath;
	static protected Properties devProperties = null;

	public static final String PROPERTYSEPARATOR = File.pathSeparator;

	static
	{
		// Check the osgi.dev property to see if dev classpath entries have been
		// defined.
		String osgiDev = System.getProperty( "osgi.dev" ); //$NON-NLS-1$
		if ( osgiDev != null )
		{
			try
			{
				inDevelopmentMode = true;
				URL location = new URL( osgiDev );
				devProperties = load( location );
				if ( devProperties != null )
					devDefaultClasspath = getArrayFromList( devProperties
							.getProperty( "*" ) ); //$NON-NLS-1$
			}
			catch ( MalformedURLException e )
			{
				devDefaultClasspath = getArrayFromList( osgiDev );
			}
		}
	}

	/**
	 * Returns the result of converting a list of comma-separated tokens into an
	 * array
	 * 
	 * @return the array of string tokens
	 * @param prop
	 *            the initial comma-separated string
	 */
	private static String[] getArrayFromList( String prop )
	{
		if ( prop == null || prop.trim( ).equals( "" ) ) //$NON-NLS-1$
			return new String[0];
		Vector list = new Vector( );
		StringTokenizer tokens = new StringTokenizer( prop, "," ); //$NON-NLS-1$
		while ( tokens.hasMoreTokens( ) )
		{
			String token = tokens.nextToken( ).trim( );
			if ( !token.equals( "" ) ) //$NON-NLS-1$
				list.addElement( token );
		}
		return list.isEmpty( ) ? new String[0] : (String[]) list
				.toArray( new String[list.size( )] );
	}

	public static boolean inDevelopmentMode( )
	{
		return inDevelopmentMode;
	}

	/*
	 * Load the given properties file
	 */
	private static Properties load( URL url )
	{
		Properties props = new Properties( );
		try
		{
			InputStream is = null;
			try
			{
				is = url.openStream( );
				props.load( is );
			}
			finally
			{
				if ( is != null )
					is.close( );
			}
		}
		catch ( IOException e )
		{
			// TODO consider logging here
		}
		return props;
	}

	/**
	 * Gets the workspace classpath
	 * 
	 * @return
	 */
	public static String getWorkspaceClassPath( )
	{
		try
		{
			Bundle bundle = Platform.getBundle( FINDER_BUNDLE_NAME );
			if ( bundle != null )
			{
				if ( bundle.getState( ) == Bundle.RESOLVED )
				{
					bundle.start( Bundle.START_TRANSIENT );
				}
			}

			if ( bundle == null )
				return null;

			Class clz = bundle.loadClass( FINDER_CLASSNAME );

			// register workspace classpath finder
			IDatasetWorkspaceClasspathFinder finder = (IDatasetWorkspaceClasspathFinder) clz
					.newInstance( );
			if ( finder == null )
				return null;

			return finder.getClassPath( );
		}
		catch ( Exception e )
		{
			e.printStackTrace( );
		}

		return null;
	}

}
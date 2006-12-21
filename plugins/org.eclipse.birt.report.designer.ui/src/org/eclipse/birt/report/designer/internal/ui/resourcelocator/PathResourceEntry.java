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

package org.eclipse.birt.report.designer.internal.ui.resourcelocator;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.birt.report.designer.core.model.SessionHandleAdapter;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.designer.ui.ReportPlugin;
import org.eclipse.birt.report.model.api.DesignFileException;
import org.eclipse.birt.report.model.api.LibraryHandle;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * 
 */

public class PathResourceEntry extends BaseResourceEntity
{

	private String path;
	private URL url;
	private String name;
	private String displayName;
	private FileFilter filter;
	private PathResourceEntry parent;
	private boolean isFolder;
	private boolean isRoot;
	private LibraryHandle library;
	private ArrayList childrenList;
	private boolean isFile;

	public PathResourceEntry( )
	{
		this( null, true );
	}

	public PathResourceEntry( final boolean showFiles )
	{
		this( null, showFiles );
	}

	public PathResourceEntry( final String[] filePattern )
	{
		this( filePattern, true );
	}

	public PathResourceEntry( final String[] filePattern,
			final boolean showFiles )
	{
		if ( filePattern != null )
		{
			filter = new FileFilter( ) {

				public boolean accept( File pathname )
				{
					if ( pathname.isDirectory( ) )
						return true;
					for ( int i = 0; i < filePattern.length; i++ )
					{
						String[] regs = filePattern[i].split( ";" ); //$NON-NLS-1$
						for ( int j = 0; j < regs.length; j++ )
						{
							if ( pathname.getName( )
									.toLowerCase( )
									.endsWith( regs[j].toLowerCase( )
											.substring( 1 ) ) )
								return true;
						}
					}
					return false;
				}

			};
		}
		else
		{
			filter = new FileFilter( ) {

				public boolean accept( File pathname )
				{
					if ( pathname.isDirectory( ) )
						return true;
					return showFiles;
				}

			};
		}
		this.name = Messages.getString( "PathResourceEntry.RootName" ); //$NON-NLS-1$
		this.displayName = Messages.getString( "PathResourceEntry.RootDisplayName" );
		this.isRoot = true;
	}

	private PathResourceEntry( String path, String name,
			PathResourceEntry parent )
	{
		this.path = path;
		this.name = name;
		this.parent = parent;
		this.filter = parent.filter;
		try
		{
			File file = new File( this.path );
			this.isFolder = file.isDirectory( );
			this.url = file.toURL( );
			this.isFile = file.isFile( );
		}
		catch ( Exception e )
		{
		}
	}

	private void initRoot( )
	{
		this.path = ReportPlugin.getDefault( ).getResourceFolder( );
		if ( this.path != null )
		{
			try
			{
				File file = new File( this.path );
				this.isFolder = file.isDirectory( );
				this.url = file.toURL( );
			}
			catch ( Exception e )
			{
			}
		}
	}

	public ResourceEntry[] getChildren( )
	{
		if ( this.childrenList == null )
		{
			this.childrenList = new ArrayList( );
			if ( this.isRoot && this.path == null )
				initRoot( );
			try
			{
				File file = new File( this.path );
				if ( file.isDirectory( ) )
				{
					File[] children = file.listFiles( filter );

					for ( int i = 0; i < children.length; i++ )
					{
						PathResourceEntry child = new PathResourceEntry( children[i].getAbsolutePath( ),
								children[i].getName( ),
								this );
						childrenList.add( child );
					}
				}
			}
			catch ( Exception e )
			{
				e.printStackTrace( );
			}
		}
		return (ResourceEntry[]) childrenList.toArray( new ResourceEntry[childrenList.size( )] );
	}

	public String getName( )
	{
		return this.name;
	}

	public String getDisplayName( )
	{
		return this.displayName;
	}
	
	public Image getImage( )
	{
		if ( this.isFolder || this.isRoot )
			return PlatformUI.getWorkbench( )
					.getSharedImages( )
					.getImage( ISharedImages.IMG_OBJ_FOLDER );
		return super.getImage( );
	}

	public ResourceEntry getParent( )
	{
		return this.parent;
	}

	public URL getURL( )
	{
		return this.url;
	}

	public boolean isFile( )
	{
		return this.isFile;
	}

	public void dispose( )
	{
		if ( this.library != null )
		{
			this.library.close( );
			this.library = null;
		}
		if ( this.childrenList != null )
		{
			for ( Iterator iterator = this.childrenList.iterator( ); iterator.hasNext( ); )
			{
				ResourceEntry entry = (ResourceEntry) iterator.next( );
				entry.dispose( );
			}
		}
	}

	public Object getAdapter( Class adapter )
	{
		if ( adapter == LibraryHandle.class )
		{
			if ( !this.isFolder && this.library == null )
			{
				try
				{
					this.library = SessionHandleAdapter.getInstance( )
							.getSessionHandle( )
							.openLibrary( getURL( ).toString( ) );
				}
				catch ( DesignFileException e )
				{
				}
			}
			return library;
		}
		return null;
	}

}
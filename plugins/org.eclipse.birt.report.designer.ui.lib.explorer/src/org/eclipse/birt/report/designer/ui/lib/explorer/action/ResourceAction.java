/*******************************************************************************
 * Copyright (c) 2008 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.report.designer.ui.lib.explorer.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.birt.report.designer.core.model.SessionHandleAdapter;
import org.eclipse.birt.report.designer.internal.ui.editors.ReportEditorInput;
import org.eclipse.birt.report.designer.internal.ui.resourcelocator.FragmentResourceEntry;
import org.eclipse.birt.report.designer.internal.ui.resourcelocator.PathResourceEntry;
import org.eclipse.birt.report.designer.internal.ui.resourcelocator.ResourceEntry;
import org.eclipse.birt.report.designer.internal.ui.util.ExceptionHandler;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.designer.ui.ReportPlugin;
import org.eclipse.birt.report.designer.ui.editors.IReportEditorContants;
import org.eclipse.birt.report.designer.ui.lib.explorer.LibraryExplorerTreeViewPage;
import org.eclipse.birt.report.designer.ui.lib.explorer.resource.ReportResourceEntry;
import org.eclipse.birt.report.designer.ui.lib.explorer.resource.ResourceEntryWrapper;
import org.eclipse.birt.report.model.api.IResourceLocator;
import org.eclipse.birt.report.model.api.LibraryHandle;
import org.eclipse.birt.report.model.api.ModuleHandle;
import org.eclipse.birt.report.model.api.command.LibraryChangeEvent;
import org.eclipse.birt.report.model.api.css.CssStyleSheetHandle;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * The base class for all actions in reource explorer.
 */
public abstract class ResourceAction extends Action
{

	/** The bundle protocol. */
	private static final String BUNDLE_PROTOCOL = "bundleresource://"; //$NON-NLS-1$

	/** The created files during fils are copied. */
	private static final Collection<File> createdFiles = new HashSet<File>( );

	/** The resource explorer page. */
	private final LibraryExplorerTreeViewPage viewerPage;

	/**
	 * Constructs an action with the specified text and the specified viewer.
	 * 
	 * @param actionText
	 *            the specified text
	 * @param viewer
	 *            the resource explorer page
	 */
	public ResourceAction( String actionText, LibraryExplorerTreeViewPage viewer )
	{
		super( actionText );
		this.viewerPage = viewer;
	}

	/**
	 * Returns the tree viewer in resource explorer.
	 * 
	 * @return the tree viewer in resource explorer.
	 */
	protected TreeViewer getTreeViewer( )
	{
		return viewerPage.getTreeViewer( );
	}

	/**
	 * Returns the shell for this workbench site.
	 * 
	 * @return the shell for this workbench site
	 */
	protected Shell getShell( )
	{
		return viewerPage.getSite( ).getShell( );
	}

	/**
	 * Returns all expanded resources, include sub path.
	 * 
	 * @param resources
	 *            the resources to expand
	 * @return all expanded resources, include sub path.
	 */
	protected Collection<?> expandResources( Collection<?> resources )
	{
		Collection<Object> libraries = new HashSet<Object>( );

		if ( resources != null && !resources.isEmpty( ) )
		{
			retrieveReources( libraries, resources );
		}
		return libraries.size( ) > 0 ? libraries : null;
	}

	/**
	 * Retrieves resources in files to the specified collection.
	 * 
	 * @param libraries
	 *            the specified collection.
	 * @param files
	 *            the resources to be rereieved.
	 */
	private void retrieveReources( Collection<Object> libraries,
			Collection<?> files )
	{
		for ( Iterator<?> iter = files.iterator( ); iter.hasNext( ); )
		{
			Object element = iter.next( );

			if ( element instanceof ResourceEntryWrapper
					&& ( (ResourceEntryWrapper) element ).getType( ) == ResourceEntryWrapper.LIBRARY )
			{
				LibraryHandle library = (LibraryHandle) ( (ResourceEntryWrapper) element ).getAdapter( LibraryHandle.class );
				if ( library.getFileName( ).startsWith( BUNDLE_PROTOCOL ) )
					return;
				libraries.add( library );
			}
			else if ( element instanceof ResourceEntryWrapper
					&& ( (ResourceEntryWrapper) element ).getType( ) == ResourceEntryWrapper.CSS_STYLE_SHEET )
			{
				CssStyleSheetHandle library = (CssStyleSheetHandle) ( (ResourceEntryWrapper) element ).getAdapter( CssStyleSheetHandle.class );
				if ( library.getFileName( ).startsWith( BUNDLE_PROTOCOL ) )
					return;
				libraries.add( library );
			}
			else if ( element instanceof LibraryHandle )
			{
				LibraryHandle library = (LibraryHandle) element;
				if ( library.getFileName( ).startsWith( BUNDLE_PROTOCOL ) )
					return;
				libraries.add( library );
			}
			else if ( element instanceof CssStyleSheetHandle )
			{
				CssStyleSheetHandle library = (CssStyleSheetHandle) element;
				if ( library.getFileName( ).startsWith( BUNDLE_PROTOCOL ) )
					return;
				libraries.add( library );
			}
			else if ( element instanceof ReportResourceEntry
					&& ( (ReportResourceEntry) element ).getReportElement( ) instanceof LibraryHandle )
			{
				LibraryHandle library = (LibraryHandle) ( (ReportResourceEntry) element ).getReportElement( );
				if ( library.getFileName( ).startsWith( BUNDLE_PROTOCOL ) )
					return;
				libraries.add( library );
			}
			else if ( element instanceof ReportResourceEntry
					&& ( (ReportResourceEntry) element ).getReportElement( ) instanceof CssStyleSheetHandle )
			{
				CssStyleSheetHandle library = (CssStyleSheetHandle) ( (ReportResourceEntry) element ).getReportElement( );
				if ( library.getFileName( ).startsWith( BUNDLE_PROTOCOL ) )
					return;
				libraries.add( library );
			}
			else if ( element instanceof PathResourceEntry )
			{
				libraries.add( element );
				if ( !( (PathResourceEntry) element ).isFile( ) )
				{
					retrieveReources( libraries,
							Arrays.asList( ( (PathResourceEntry) element ).getChildren( ) ) );
				}
			}
			else if ( element instanceof FragmentResourceEntry )
			{
				libraries.add( element );
			}
		}
	}

	/**
	 * Returns the currently selected resources.
	 * 
	 * @return the currently selected resources.
	 */
	protected Collection<?> getSelectedResources( )
	{
		Collection<?> resources = new ArrayList<Object>( );
		ISelection selection = ( viewerPage == null ? null
				: viewerPage.getSelection( ) );

		if ( selection instanceof IStructuredSelection )
		{
			resources.addAll( ( (IStructuredSelection) selection ).toList( ) );
		}
		return resources;
	}

	/**
	 * Checks if the selected resources can be modified.
	 * 
	 * @return <code>true</code> if the selected resources can be modified,
	 *         <code>false</code> otherwise.
	 */
	protected boolean canModifySelectedResources( )
	{
		Collection<?> resources = getSelectedResources( );

		if ( resources == null || resources.isEmpty( ) )
		{
			return false;
		}

		for ( Object resource : resources )
		{
			if ( resource instanceof ResourceEntryWrapper )
			{
				if ( ( (ResourceEntryWrapper) resource ).getParent( ) instanceof FragmentResourceEntry )
				{
					return false;
				}
			}
			else if ( resource instanceof PathResourceEntry )
			{
				if ( ( (PathResourceEntry) resource ).isRoot( ) )
				{
					return false;
				}
			}
			else if ( resource instanceof FragmentResourceEntry )
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if the selected container can be insertted into.
	 * 
	 * @return <code>true</code> if the selected container can be insertted
	 *         into, <code>false</code> otherwise.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	protected boolean canInsertIntoSelectedContainer( ) throws IOException
	{
		return getSelectedContainer( ) != null;
	}

	/**
	 * Returns the current selected file.
	 * 
	 * @return the current selected file.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	protected Collection<File> getSelectedFiles( ) throws IOException
	{
		Collection<?> currentResource = getSelectedResources( );
		Collection<File> files = new HashSet<File>( );

		if ( currentResource == null )
		{
			return files;
		}

		for ( Object resource : currentResource )
		{
			File file = null;

			if ( resource instanceof LibraryHandle )
			{
				file = new File( ( (LibraryHandle) resource ).getFileName( ) );
			}
			else if ( resource instanceof CssStyleSheetHandle )
			{
				CssStyleSheetHandle node = (CssStyleSheetHandle) resource;
				ModuleHandle module = SessionHandleAdapter.getInstance( )
						.getReportDesignHandle( );

				URL url = module.findResource( node.getFileName( ),
						IResourceLocator.CASCADING_STYLE_SHEET );

				file = convertToFile( url );
			}
			else if ( resource instanceof ResourceEntry )
			{
				file = convertToFile( ( (ResourceEntry) resource ).getURL( ) );
			}

			if ( file != null && file.exists( ) )
			{
				files.add( file );
			}
		}
		return files;
	}

	/**
	 * Returns the current selected container.
	 * 
	 * @return the current selected container.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	protected File getSelectedContainer( ) throws IOException
	{
		if ( includeFragment( getSelectedResources( ) ) )
		{
			return null;
		}

		Collection<File> files = getSelectedFiles( );
		File folder = null;

		for ( File file : files )
		{
			File container = file.isDirectory( ) ? file : file.getParentFile( );

			if ( container == null )
			{
				return null;
			}

			if ( folder == null )
			{
				folder = container;
			}
			else if ( !folder.equals( container ) )
			{
				return null;
			}
		}
		return folder == null ? null : folder;
	}

	/**
	 * Checks if there is fragment resource in the specified resources.
	 * 
	 * @param resources
	 *            the resources to check.
	 * @return <code>true</code> if any fragment reource is included in the
	 *         soecified resources, <code>false</code> otherwise.
	 */
	protected boolean includeFragment( Collection<?> resources )
	{
		for ( Object resource : resources )
		{
			if ( resource instanceof ResourceEntryWrapper )
			{
				if ( ( (ResourceEntryWrapper) resource ).getParent( ) instanceof FragmentResourceEntry )
				{
					return true;
				}
			}
			else if ( resource instanceof FragmentResourceEntry )
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Creates an instance of <copy>IRunnableWithProgress</copy> for copying
	 * the specified source file to the specified target file.
	 * 
	 * @param srcFile
	 *            the specified source file.
	 * @param targetFile
	 *            the specified target file.
	 * @return the instance of <code>IRunnableWithProgress</code>.
	 */
	protected IRunnableWithProgress createCopyFileRunnable( final File srcFile,
			final File targetFile )
	{
		return new IRunnableWithProgress( ) {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
			 */
			public synchronized final void run( IProgressMonitor monitor )
					throws InvocationTargetException, InterruptedException
			{
				monitor.beginTask( null, IProgressMonitor.UNKNOWN );
				try
				{
					if ( srcFile != null && srcFile.exists( ) )
					{
						if ( srcFile.isDirectory( ) )
						{
							copyFolder( srcFile, targetFile, monitor );
						}
						else
						{
							copyFile( srcFile, targetFile );
						}
						fireResourceChanged( targetFile.getAbsolutePath( ) );
					}
				}
				catch ( IOException e )
				{
					ExceptionHandler.handle( e );
				}
				finally
				{
					monitor.done( );
					createdFiles.clear( );
				}
			}
		};
	}

	/**
	 * Copys a folder to another folder.
	 * 
	 * @param srcFolder
	 *            the source folder
	 * @param targetFolder
	 *            the target folder
	 * @throws IOException
	 *             if an error occurs.
	 */
	public static void copyFolder( File srcFolder, File targetFolder )
			throws IOException
	{
		copyFolder( srcFolder, targetFolder, null );
	}

	/**
	 * Copys a folder to another folder.
	 * 
	 * @param srcFolder
	 *            the source folder
	 * @param targetFolder
	 *            the target folder
	 * @param monitor
	 *            the progress monitor to use to display progress and receive
	 *            requests for cancelation.
	 * @throws IOException
	 *             if an error occurs.
	 */
	public static void copyFolder( File srcFolder, File targetFolder,
			IProgressMonitor monitor ) throws IOException
	{
		if ( createdFiles.contains( srcFolder ) )
		{
			return;
		}

		File[] children = srcFolder.listFiles( );

		if ( targetFolder.mkdirs( ) )
		{
			createdFiles.add( targetFolder );
		}

		for ( File source : children )
		{
			if ( monitor != null && monitor.isCanceled( ) )
			{
				return;
			}
			File target = new Path( targetFolder.getAbsolutePath( ) ).append( source.getName( ) )
					.toFile( );

			if ( source.isDirectory( ) )
			{
				copyFolder( source, target, monitor );
			}
			else
			{
				copyFile( source, target );
			}
		}
	}

	/**
	 * Copys a file to another file.
	 * 
	 * @param srcFile
	 *            the source file
	 * @param destFile
	 *            the target file
	 * @throws IOException
	 *             if an error occurs.
	 */
	public static void copyFile( File srcFile, File destFile )
			throws IOException
	{
		if ( srcFile.equals( destFile ) )
		{
			// Does nothing if fils are same.
			return;
		}

		if ( destFile.createNewFile( ) )
		{
			createdFiles.add( destFile );
		}

		FileInputStream fis = null;
		FileOutputStream fos = null;
		FileChannel fcin = null;
		FileChannel fcout = null;

		try
		{
			fis = new FileInputStream( srcFile );
			fos = new FileOutputStream( destFile );
			fcin = fis.getChannel( );
			fcout = fos.getChannel( );

			// Does the file copy.
			fcin.transferTo( 0, fcin.size( ), fcout );
		}
		finally
		{
			if ( fis != null )
			{
				fis.close( );
			}
			if ( fos != null )
			{
				fos.close( );
			}
			if ( fcin != null )
			{
				fcin.close( );
			}
			if ( fcout != null )
			{
				fcout.close( );
			}
		}
	}

	/**
	 * Converts the specified instance of <code>URL</code> to an instance of
	 * <code>File</code>.
	 * 
	 * @param url
	 *            the specified URL to convert.
	 * @return the instance of <code>File</code>.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public static File convertToFile( URL url ) throws IOException
	{
		if ( url == null )
		{
			throw new IOException( Messages.getString( "ResourceAction.ConvertToFile.URLIsNull" ) ); //$NON-NLS-1$
		}

		URL fileURL = FileLocator.toFileURL( url );
		IPath path = new Path( ( fileURL ).getPath( ) );
		String ref = fileURL.getRef( );
		String fullPath = path.toFile( ).getAbsolutePath( );

		if ( ref != null )
		{
			ref = "#" + ref; //$NON-NLS-1$
			if ( path.toString( ).endsWith( "/" ) ) //$NON-NLS-1$
			{
				return path.append( ref ).toFile( );
			}
			else
			{
				fullPath += ref;
			}
		}
		return new File( fullPath );
	}

	/**
	 * Opens an editor on the specified library file.
	 * 
	 * @param file
	 *            the specified library to open.
	 */
	protected void openLibrary( File file )
	{
		if ( file != null )
		{
			openLibrary( viewerPage, file );
		}
	}

	/**
	 * Opens an editor on the specified library file, and refresh the specified
	 * library explorer page.
	 * 
	 * @param viewer
	 *            the library explorer page
	 * @param file
	 *            the specified library to open.
	 */
	public static void openLibrary( final LibraryExplorerTreeViewPage viewer,
			final File file )
	{
		if ( file == null )
		{
			return;
		}

		Display display;

		if ( viewer != null )
		{
			display = viewer.getSite( ).getShell( ).getDisplay( );
		}
		else
		{
			display = Display.getCurrent( );
		}

		display.asyncExec( new Runnable( ) {

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.lang.Runnable#run()
			 */
			public void run( )
			{
				try
				{
					IWorkbench workbench = PlatformUI.getWorkbench( );
					IWorkbenchWindow window = workbench == null ? null
							: workbench.getActiveWorkbenchWindow( );

					IWorkbenchPage page = window == null ? null
							: window.getActivePage( );

					if ( page != null )
					{
						page.openEditor( new ReportEditorInput( file ),
								IReportEditorContants.LIBRARY_EDITOR_ID,
								true );
					}
				}
				catch ( PartInitException e )
				{
					ExceptionHandler.handle( e );
				}
				finally
				{
					viewer.selectPath( new String[]{
						file.getAbsolutePath( )
					} );
				}
			}
		} );
	}

	/**
	 * Notifies model for the reource chang.
	 * 
	 * @param fileName
	 *            the resource's file name.
	 */
	protected void fireResourceChanged( String fileName )
	{
		SessionHandleAdapter.getInstance( )
				.getSessionHandle( )
				.fireResourceChange( new LibraryChangeEvent( fileName ) );
	}

	/**
	 * Creates an instance of <copy>IRunnableWithProgress</copy> for removing
	 * resources.
	 * 
	 * @return the instance of <code>IRunnableWithProgress</code>.
	 */
	protected IRunnableWithProgress createDeleteRunnable(
			final Collection<File> files )
	{
		return new IRunnableWithProgress( ) {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
			 */
			public synchronized final void run( IProgressMonitor monitor )
					throws InvocationTargetException, InterruptedException
			{
				monitor.beginTask( null, IProgressMonitor.UNKNOWN ); //$NON-NLS-1$

				try
				{
					for ( File file : files )
					{
						remove( file, monitor );
					}
					fireResourceChanged( new File( ReportPlugin.getDefault( )
							.getResourceFolder( ) ).getAbsolutePath( ) );
				}
				catch ( IOException e )
				{
					throw new InvocationTargetException( e );
				}
				catch ( PartInitException e )
				{
					throw new InvocationTargetException( e );
				}
				catch ( CoreException e )
				{
					throw new InvocationTargetException( e );
				}
				finally
				{
					monitor.done( );
				}
			}
		};
	}

	/**
	 * Removes the specified file or folder.
	 * 
	 * @param file
	 *            the specified file or folder to remove.
	 * @throws CoreException
	 *             if the resource changes are disallowed.
	 * @throws IOException
	 *             if an I/O error occurs.
	 * @throws PartInitException
	 *             if workbench part cannot be initialized correctly.
	 */
	protected void remove( File file ) throws CoreException, IOException,
			PartInitException
	{
		remove( file, null );
	}

	/**
	 * Removes the specified file or folder.
	 * 
	 * @param file
	 *            the specified file or folder to remove.
	 * @param monitor
	 *            the progress monitor to use to display progress and receive
	 *            requests for cancelation.
	 * @throws CoreException
	 *             if the resource changes are disallowed.
	 * @throws IOException
	 *             if an I/O error occurs.
	 * @throws PartInitException
	 *             if workbench part cannot be initialized correctly.
	 */
	protected void remove( File file, IProgressMonitor monitor )
			throws CoreException, IOException, PartInitException
	{
		String[] children = file.list( );

		if ( children != null )
		{
			for ( String child : children )
			{
				if ( monitor != null && monitor.isCanceled( ) )
				{
					return;
				}
				remove( new File( file.getAbsolutePath( ), child ) );
			}
		}
		removeFile( file );
	}

	/**
	 * Removes the specified file
	 * 
	 * @param file
	 *            the specified file to remove.
	 * @throws CoreException
	 *             if the resource changes are disallowed.
	 * @throws PartInitException
	 *             if workbench part cannot be initialized correctly.
	 */
	private void removeFile( File file ) throws CoreException,
			PartInitException
	{
		if ( file == null )
		{
			return;
		}

		file.delete( );

		if ( !file.isFile( ) )
		{
			return;
		}

		String resourceFolder = ReportPlugin.getDefault( ).getResourceFolder( );
		String filePath = file.getAbsolutePath( );

		if ( filePath.startsWith( new File( resourceFolder ).getAbsolutePath( ) ) )
		{
			// refresh project
			IProject[] projects = ResourcesPlugin.getWorkspace( )
					.getRoot( )
					.getProjects( );
			for ( int i = 0; i < projects.length; i++ )
			{
				if ( projects[i].getLocation( )
						.toFile( )
						.getPath( )
						.equals( new File( resourceFolder ).getPath( ) ) )
				{
					projects[i].refreshLocal( IResource.DEPTH_INFINITE, null );
					break;
				}
			}

			// close editor
			IWorkbenchPage pg = PlatformUI.getWorkbench( )
					.getActiveWorkbenchWindow( )
					.getActivePage( );

			IEditorReference[] editors = pg.getEditorReferences( );

			for ( int i = 0; i < editors.length; i++ )
			{
				Object adapter = editors[i].getEditorInput( )
						.getAdapter( IFile.class );
				if ( adapter != null )
				{
					if ( ( (IFile) adapter ).getFullPath( )
							.toFile( )
							.getPath( )
							.equals( filePath ) )
						editors[i].getEditor( false ).dispose( );
				}
				else if ( editors[i].getEditorInput( ) instanceof IPathEditorInput )
				{
					File fileSystemFile = ( (IPathEditorInput) editors[i].getEditorInput( ) ).getPath( )
							.toFile( );
					if ( fileSystemFile.getPath( ).equals( filePath ) )
						pg.closeEditor( editors[i].getEditor( false ), false );
				}
			}
		}
	}
}
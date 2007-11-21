/*************************************************************************************
 * Copyright (c) 2004 Actuate Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Actuate Corporation - Initial implementation.
 ************************************************************************************/

package org.eclipse.birt.report.designer.ui.lib.explorer;

import java.io.File;
import java.net.URL;

import org.eclipse.birt.core.preference.IPreferenceChangeListener;
import org.eclipse.birt.core.preference.IPreferences;
import org.eclipse.birt.core.preference.PreferenceChangeEvent;
import org.eclipse.birt.report.designer.core.model.SessionHandleAdapter;
import org.eclipse.birt.report.designer.internal.ui.resourcelocator.PathResourceEntry;
import org.eclipse.birt.report.designer.internal.ui.resourcelocator.ResourceEntry;
import org.eclipse.birt.report.designer.internal.ui.resourcelocator.ResourceLocator;
import org.eclipse.birt.report.designer.internal.ui.util.UIUtil;
import org.eclipse.birt.report.designer.internal.ui.views.ViewsTreeProvider;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.designer.ui.ReportPlugin;
import org.eclipse.birt.report.designer.ui.lib.explorer.dnd.LibraryDragListener;
import org.eclipse.birt.report.designer.ui.lib.explorer.resource.ResourceEntryWrapper;
import org.eclipse.birt.report.designer.ui.preferences.PreferenceFactory;
import org.eclipse.birt.report.designer.ui.widget.TreeViewerBackup;
import org.eclipse.birt.report.model.api.DataSetHandle;
import org.eclipse.birt.report.model.api.DataSourceHandle;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.EmbeddedImageHandle;
import org.eclipse.birt.report.model.api.IResourceLocator;
import org.eclipse.birt.report.model.api.LibraryHandle;
import org.eclipse.birt.report.model.api.ModuleHandle;
import org.eclipse.birt.report.model.api.ParameterGroupHandle;
import org.eclipse.birt.report.model.api.ParameterHandle;
import org.eclipse.birt.report.model.api.ReportItemHandle;
import org.eclipse.birt.report.model.api.ThemeHandle;
import org.eclipse.birt.report.model.api.command.ResourceChangeEvent;
import org.eclipse.birt.report.model.api.core.IResourceChangeListener;
import org.eclipse.birt.report.model.api.css.CssStyleSheetHandle;
import org.eclipse.birt.report.model.api.validators.IValidationListener;
import org.eclipse.birt.report.model.api.validators.ValidationEvent;
import org.eclipse.gef.dnd.TemplateTransfer;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

/**
 * This class represents the tree view page of the data view
 * 
 */
public class LibraryExplorerTreeViewPage extends LibraryExplorerViewPage implements
		IValidationListener,
		IPreferenceChangeListener,
		IResourceChangeListener
{

	// private static final String LABEL_DOUBLE_CLICK = Messages.getString(
	// "DataViewTreeViewerPage.tooltip.DoubleClickToEdit" ); //$NON-NLS-1$

	private static final String BUNDLE_PROTOCOL = "bundleresource://"; //$NON-NLS-1$

	private IPreferences prefs;
	private TreeViewer treeViewer;
	private TreeViewerBackup libraryBackup;

	private static final String[] LIBRARY_FILENAME_PATTERN = new String[]{
			"*.rptlibrary", //$NON-NLS-1$
			"*.RPTLIBRARY", //$NON-NLS-1$
			"*.css", //$NON-NLS-1$
			"*.CSS" //$NON-NLS-1$
	};

	public LibraryExplorerTreeViewPage( )
	{
		super( );
		SessionHandleAdapter.getInstance( )
				.getSessionHandle( )
				.addResourceChangeListener( this );
	}

	/**
	 * Creates the tree view
	 * 
	 * @param parent
	 *            the parent
	 */
	protected TreeViewer createTreeViewer( Composite parent )
	{
		treeViewer = new TreeViewer( parent, SWT.MULTI
				| SWT.H_SCROLL
				| SWT.V_SCROLL );
		configTreeViewer( );
		initPage( );
		refreshRoot( );
		return treeViewer;
	}

	/**
	 * Configures the tree viewer.
	 */
	protected void configTreeViewer( )
	{

		ViewsTreeProvider provider = new LibraryExplorerProvider( );

		treeViewer.setContentProvider( provider );
		treeViewer.setLabelProvider( provider );

		// Adds drag and drop support
		int ops = DND.DROP_COPY | DND.DROP_LINK;
		Transfer[] transfers = new Transfer[]{
			TemplateTransfer.getInstance( )
		};
		treeViewer.addDragSupport( ops,
				transfers,
				new LibraryDragListener( treeViewer ) );

		treeViewer.getControl( ).addKeyListener( new KeyListener( ) {

			public void keyPressed( KeyEvent e )
			{

			}

			public void keyReleased( KeyEvent e )
			{
				if ( e.keyCode == SWT.F5 )
				{
					treeViewer.refresh( );
				}
			}
		} );

		treeViewer.getTree( ).addDisposeListener( new DisposeListener( ) {

			public void widgetDisposed( DisposeEvent e )
			{
				Object input = treeViewer.getInput( );
				if ( input instanceof Object[] )
				{
					Object[] array = (Object[]) input;
					for ( int i = 0; i < array.length; i++ )
					{
						if ( array[i] instanceof ResourceEntry )
							( (ResourceEntry) array[i] ).dispose( );
					}

				}
			}

		} );

		TreeListener libraryTreeListener = new TreeListener( ) {

			public void treeCollapsed( TreeEvent e )
			{
				Item item = (Item) e.item;
				if ( libraryBackup != null )
					libraryBackup.updateCollapsedStatus( treeViewer,
							item.getData( ) );

			}

			public void treeExpanded( TreeEvent e )
			{
				Item item = (Item) e.item;
				if ( libraryBackup != null )
					libraryBackup.updateExpandedStatus( treeViewer,
							item.getData( ) );
			}

		};
		treeViewer.getTree( ).addTreeListener( libraryTreeListener );
	}

	/**
	 * Initializes the data view page.
	 */
	protected void initPage( )
	{
		createContextMenus( );

		// !remove sorter to keep same order with outline view
		// treeViewer.setSorter( new ViewerSorter( ) {
		//
		// public int category( Object element )
		// {
		// if ( element instanceof LibraryHandle )
		// {
		// return 1;
		// }
		// return super.category( element );
		// }
		//
		// } );

		treeViewer.getTree( ).addMouseTrackListener( new MouseTrackAdapter( ) {

			public void mouseHover( MouseEvent event )
			{
				Widget widget = event.widget;
				if ( widget == treeViewer.getTree( ) )
				{
					Point pt = new Point( event.x, event.y );
					TreeItem item = treeViewer.getTree( ).getItem( pt );
					treeViewer.getTree( ).setToolTipText( getTooltip( item ) );
				}
			}
		} );

		// Listen to preference change.
		prefs = (IPreferences) PreferenceFactory.getInstance( )
				.getPreferences( ReportPlugin.getDefault( ),
						UIUtil.getCurrentProject( ) );
		prefs.addPreferenceChangeListener( this );
	}

	/**
	 * Creates the context menu
	 */
	private void createContextMenus( )
	{
		MenuManager menuManager = new LibraryExplorerContextMenuProvider( this );

		Menu menu = menuManager.createContextMenu( treeViewer.getControl( ) );

		treeViewer.getControl( ).setMenu( menu );
		getSite( ).registerContextMenu( "org.eclipse.birt.report.designer.ui.lib.explorer.view", menuManager, //$NON-NLS-1$
				getSite( ).getSelectionProvider( ) );
	}

	private String getTooltip( TreeItem item )
	{
		if ( item != null )
		{
			Object object = item.getData( );
			if ( object instanceof DataSourceHandle
					|| object instanceof DataSetHandle )
			{
				return Messages.getString( "LibraryExplorerTreeViewPage.toolTips.DragAndDropOutline" ); //$NON-NLS-1$
			}
			else if ( object instanceof ThemeHandle )
			{
				return Messages.getString( "LibraryExplorerTreeViewPage.toolTips.DragAndDropLayout" ); //$NON-NLS-1$
			}
			else if ( object instanceof ParameterHandle
					|| object instanceof ParameterGroupHandle
					|| object instanceof EmbeddedImageHandle
					|| object instanceof ReportItemHandle )
			{
				return Messages.getString( "LibraryExplorerTreeViewPage.toolTips.DragAndDropToOutlineORLayout" ); //$NON-NLS-1$
			}
			else if ( object instanceof LibraryHandle )
			{
				return ( (LibraryHandle) object ).getFileName( );
			}
			else if ( object instanceof CssStyleSheetHandle )
			{
				CssStyleSheetHandle CssStyleSheetHandle = (CssStyleSheetHandle) object;
				if ( CssStyleSheetHandle.getFileName( )
						.startsWith( BUNDLE_PROTOCOL ) )
				{
					return CssStyleSheetHandle.getFileName( );
				}
				else
				{
					ModuleHandle moudleHandle = CssStyleSheetHandle.getModule( )
							.getModuleHandle( );
					URL url = moudleHandle.findResource( CssStyleSheetHandle.getFileName( ),
							IResourceLocator.CASCADING_STYLE_SHEET );
					if ( url != null )
					{
						return url.getFile( );
					}
				}
			}
			else if ( object instanceof ResourceEntryWrapper
					&& ( (ResourceEntryWrapper) object ).getType( ) == ResourceEntryWrapper.LIBRARY )
			{
				LibraryHandle libHandle = (LibraryHandle) ( (ResourceEntryWrapper) object ).getAdapter( LibraryHandle.class );

				return libHandle.getFileName( );
			}
			else if ( object instanceof ResourceEntryWrapper
					&& ( (ResourceEntryWrapper) object ).getType( ) == ResourceEntryWrapper.CSS_STYLE_SHEET )
			{
				CssStyleSheetHandle cssHandle = (CssStyleSheetHandle) ( (ResourceEntryWrapper) object ).getAdapter( CssStyleSheetHandle.class );

				if ( cssHandle.getFileName( ).startsWith( BUNDLE_PROTOCOL ) )
				{
					return cssHandle.getFileName( );
				}
				else
				{
					ModuleHandle moudleHandle = cssHandle.getModule( )
							.getModuleHandle( );
					URL url = moudleHandle.findResource( cssHandle.getFileName( ),
							IResourceLocator.CASCADING_STYLE_SHEET );
					if ( url != null )
					{
						return url.getFile( );
					}
				}
			}
			else if ( object instanceof PathResourceEntry )
			{
				return ( (PathResourceEntry) object ).getURL( ).getPath( );
			}
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * The <code>Page</code> implementation of this <code>IPage</code>
	 * method disposes of this page's control (if it has one and it has not
	 * already been disposed). Disposes the visitor of the element
	 */
	public void dispose( )
	{
		if ( prefs != null )
			prefs.removePreferenceChangeListener( this );
		SessionHandleAdapter.getInstance( )
				.getSessionHandle( )
				.removeResourceChangeListener( this );
		libraryBackup.dispose( );
		super.dispose( );
	}

	protected boolean isDisposed( )
	{
		Control ctrl = getControl( );
		return ( ctrl == null || ctrl.isDisposed( ) );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.model.api.validators.IValidationListener#elementValidated(org.eclipse.birt.report.model.api.DesignElementHandle,
	 *      org.eclipse.birt.report.model.api.validators.ValidationEvent)
	 */
	public void elementValidated( DesignElementHandle targetElement,
			ValidationEvent ev )
	{
		if ( treeViewer != null && !treeViewer.getTree( ).isDisposed( ) )
		{
			treeViewer.refresh( );
			treeViewer.setInput( ResourceLocator.getRootEntries( LIBRARY_FILENAME_PATTERN ) );
			handleTreeViewerRefresh( );
		}
	}

	private void handleTreeViewerRefresh( )
	{
		if ( libraryBackup != null )
		{
			libraryBackup.restoreBackup( treeViewer );
		}
		else
		{
			libraryBackup = new TreeViewerBackup( );
			treeViewer.expandToLevel( 2 );
			libraryBackup.updateStatus( treeViewer );
		}
	}

	public void refreshRoot( )
	{
		if ( treeViewer != null && !treeViewer.getTree( ).isDisposed( ) )
		{
			// treeViewer.setInput( new Object[]{
			// new ResourceFolderLibNode( ), new FragmentsLibNode( )
			// } );
			ISelection selection = getSelection( );
			treeViewer.setSelection( null );
			treeViewer.setInput( ResourceLocator.getRootEntries( LIBRARY_FILENAME_PATTERN ) );
			handleTreeViewerRefresh( );
			if ( selection != null )
				setSelection( selection );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.IEclipsePreferences$IPreferenceChangeListener#preferenceChange(org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent)
	 */
	public void preferenceChange( PreferenceChangeEvent event )
	{
		if ( event.getKey( ).equals( PreferenceChangeEvent.SPECIALTODEFAULT )
				|| ReportPlugin.RESOURCE_PREFERENCE.equals( event.getKey( ) ) )
			Display.getDefault( ).asyncExec( new Runnable( ) {

				public void run( )
				{
					refreshRoot( );
				}
			} );
	}

	public void resourceChanged( ModuleHandle module, ResourceChangeEvent event )
	{
		String path = event.getChangedResourcePath( );
		if ( path != null )
		{
			File file = new File( path );
			String resourcePath = ReportPlugin.getDefault( )
					.getResourceFolder( );

			File resource = new File( resourcePath );

			if ( file.exists( )
					&& resource.exists( )
					&& file.toURI( ).toString( ).indexOf( resource.toURI( )
							.toString( ) ) > -1 )
			{
				if ( !isDisposed( ) )
				{
					refreshRoot( );
				}
			}
		}
	}

	public TreeViewer getTreeViewer( )
	{
		return treeViewer;
	}

}
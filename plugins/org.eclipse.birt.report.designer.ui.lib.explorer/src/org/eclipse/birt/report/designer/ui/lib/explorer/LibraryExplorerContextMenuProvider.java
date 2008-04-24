/*************************************************************************************
 * Copyright (c) 2004-2008 Actuate Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Actuate Corporation - Initial implementation.
 ************************************************************************************/

package org.eclipse.birt.report.designer.ui.lib.explorer;

import org.eclipse.birt.report.designer.internal.ui.resourcelocator.PathResourceEntry;
import org.eclipse.birt.report.designer.internal.ui.util.Policy;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.designer.ui.ContextMenuProvider;
import org.eclipse.birt.report.designer.ui.lib.explorer.action.AddElementtoReport;
import org.eclipse.birt.report.designer.ui.lib.explorer.action.AddResourceAction;
import org.eclipse.birt.report.designer.ui.lib.explorer.action.AddSelectedLibToCurrentReportDesignAction;
import org.eclipse.birt.report.designer.ui.lib.explorer.action.CopyResourceAction;
import org.eclipse.birt.report.designer.ui.lib.explorer.action.DeleteResourceAction;
import org.eclipse.birt.report.designer.ui.lib.explorer.action.MoveResourceAction;
import org.eclipse.birt.report.designer.ui.lib.explorer.action.NewFolderAction;
import org.eclipse.birt.report.designer.ui.lib.explorer.action.NewLibraryAction;
import org.eclipse.birt.report.designer.ui.lib.explorer.action.PasteResourceAction;
import org.eclipse.birt.report.designer.ui.lib.explorer.action.RefreshResourceExplorerAction;
import org.eclipse.birt.report.designer.ui.lib.explorer.action.RenameResourceAction;
import org.eclipse.birt.report.designer.ui.lib.explorer.action.UseCssInReportDesignAction;
import org.eclipse.birt.report.designer.ui.lib.explorer.action.UseCssInThemeAction;
import org.eclipse.birt.report.designer.ui.lib.explorer.resource.ReportResourceEntry;
import org.eclipse.birt.report.designer.ui.lib.explorer.resource.ResourceEntryWrapper;
import org.eclipse.birt.report.model.api.CascadingParameterGroupHandle;
import org.eclipse.birt.report.model.api.EmbeddedImageHandle;
import org.eclipse.birt.report.model.api.LibraryHandle;
import org.eclipse.birt.report.model.api.ReportElementHandle;
import org.eclipse.birt.report.model.api.ScalarParameterHandle;
import org.eclipse.birt.report.model.api.StyleHandle;
import org.eclipse.birt.report.model.api.ThemeHandle;
import org.eclipse.birt.report.model.api.css.CssStyleSheetHandle;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.ui.IWorkbenchActionConstants;

/**
 * This class provides the context menu for the single selection and multiple
 * selection
 * 
 * 
 */
public class LibraryExplorerContextMenuProvider extends ContextMenuProvider
{

	// Defines actions
	private final IAction refreshExplorerAction;
	private final IAction useLibraryAction;
	private final IAction deleteLibraryandCssAction;
	private final IAction renameLibraryandCssAction;
	private final IAction pasteLibraryandCssAction;
	private final IAction copyLibraryandCssAction;
	private final IAction moveLibraryandCssAction;
	private final IAction addResourceAction;
	private final IAction newFolderAction;
	private final IAction newLibraryAction;

	private final LibraryExplorerTreeViewPage page;
	private Clipboard clipboard;

	/**
	 * constructor
	 * 
	 * @param page
	 *            the viewer
	 * @param registry
	 *            the registry
	 */
	public LibraryExplorerContextMenuProvider( LibraryExplorerTreeViewPage page )
	{
		super( page.getTreeViewer( ) );
		this.page = page;

		clipboard = new Clipboard( page.getSite( ).getShell( ).getDisplay( ) );

		refreshExplorerAction = new RefreshResourceExplorerAction( page );
		useLibraryAction = new AddSelectedLibToCurrentReportDesignAction( page.getTreeViewer( ) );
		deleteLibraryandCssAction = new DeleteResourceAction( page );
		addResourceAction = new AddResourceAction( page );
		renameLibraryandCssAction = new RenameResourceAction( page );
		newFolderAction = new NewFolderAction( page );
		moveLibraryandCssAction = new MoveResourceAction( page );
		newLibraryAction = new NewLibraryAction( page );
		copyLibraryandCssAction = new CopyResourceAction( page, clipboard );
		pasteLibraryandCssAction = new PasteResourceAction( page, clipboard );
	}

	@Override
	public void dispose( )
	{
		if ( clipboard != null )
		{
			clipboard.dispose( );
			clipboard = null;
		}
		super.dispose( );
	}

	/**
	 * Builds the context menu. Single selection menu and multiple selection
	 * menu are created while selecting just single element or multiple elements
	 * 
	 * 
	 * @param menu
	 *            the menu
	 */
	public void buildContextMenu( IMenuManager menu )
	{
		if ( Policy.TRACING_MENU_SHOW )
		{
			System.out.println( "Menu(for Views) >> Shows for library" ); //$NON-NLS-1$
		}

		menu.removeAll( );
		menu.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );
		menu.add( new Separator( ) );

		IStructuredSelection selection = (IStructuredSelection) getViewer( ).getSelection( );
		if ( selection != null && selection.getFirstElement( ) != null )
		{
			Object selected = selection.getFirstElement( );
			if ( selected instanceof ReportResourceEntry )
				selected = ( (ReportResourceEntry) selected ).getReportElement( );

			if ( selected instanceof ResourceEntryWrapper
					&& ( (ResourceEntryWrapper) selected ).getType( ) == ResourceEntryWrapper.LIBRARY )
			{
				if ( useLibraryAction.isEnabled( ) )
				{
					menu.add( useLibraryAction );
					menu.add( new Separator( ) );
				}

				if ( copyLibraryandCssAction.isEnabled( ) )
				{
					menu.add( copyLibraryandCssAction );
				}
				if ( deleteLibraryandCssAction.isEnabled( ) )
				{
					menu.add( deleteLibraryandCssAction );
				}
				if ( moveLibraryandCssAction.isEnabled( ) )
				{
					menu.add( moveLibraryandCssAction );
				}
				if ( renameLibraryandCssAction.isEnabled( ) )
				{
					menu.add( renameLibraryandCssAction );
				}
				menu.add( new Separator( ) );
			}
			else if ( selected instanceof ResourceEntryWrapper
					&& ( (ResourceEntryWrapper) selected ).getType( ) == ResourceEntryWrapper.CSS_STYLE_SHEET )
			{
				menu.add( new UseCssInReportDesignAction( page ) );
				menu.add( new UseCssInThemeAction( page ) );
				menu.add( new Separator( ) );
				if ( copyLibraryandCssAction.isEnabled( ) )
				{
					menu.add( copyLibraryandCssAction );
				}
				if ( deleteLibraryandCssAction.isEnabled( ) )
				{
					menu.add( deleteLibraryandCssAction );
				}
				if ( moveLibraryandCssAction.isEnabled( ) )
				{
					menu.add( moveLibraryandCssAction );
				}
				if ( renameLibraryandCssAction.isEnabled( ) )
				{
					menu.add( renameLibraryandCssAction );
				}
				menu.add( new Separator( ) );
			}
			else if ( selected instanceof LibraryHandle )
			{
				if ( useLibraryAction.isEnabled( ) )
				{
					menu.add( useLibraryAction );
					menu.add( new Separator( ) );
				}
				// if ( deleteLibraryandCssAction.isEnabled( ) )
				// {
				// menu.add( deleteLibraryandCssAction );
				// }
			}
			else if ( selected instanceof CssStyleSheetHandle )
			{
				menu.add( new UseCssInReportDesignAction( page ) );
				menu.add( new UseCssInThemeAction( page ) );
				menu.add( new Separator( ) );
				// menu.add( deleteLibraryandCssAction );
			}
			else if ( selected instanceof PathResourceEntry )
			{
				if ( !( (PathResourceEntry) selected ).isFile( ) )
				{
					IMenuManager newMenu = new MenuManager( Messages.getString( "NewResource.MenuGroup.Text" ) ); //$NON-NLS-1$

					menu.add( newMenu );

					// Adds all actions into folding group.
					newMenu.add( newFolderAction );
					newMenu.add( newLibraryAction );

					if ( addResourceAction.isEnabled( ) )
					{
						menu.add( addResourceAction );
					}
					menu.add( new Separator( ) );
					if ( pasteLibraryandCssAction.isEnabled( ) )
					{
						menu.add( pasteLibraryandCssAction );
					}
					if ( deleteLibraryandCssAction.isEnabled( ) )
					{
						menu.add( deleteLibraryandCssAction );
					}
					if ( renameLibraryandCssAction.isEnabled( ) )
					{
						menu.add( renameLibraryandCssAction );
					}
					menu.add( new Separator( ) );
				}
				else
				{
					if ( copyLibraryandCssAction.isEnabled( ) )
					{
						menu.add( copyLibraryandCssAction );
					}
					if ( deleteLibraryandCssAction.isEnabled( ) )
					{
						menu.add( deleteLibraryandCssAction );
					}
					if ( moveLibraryandCssAction.isEnabled( ) )
					{
						menu.add( moveLibraryandCssAction );
					}
					if ( renameLibraryandCssAction.isEnabled( ) )
					{
						menu.add( renameLibraryandCssAction );
					}
					menu.add( new Separator( ) );
				}
			}

			if ( canAddtoReport( selected ) )
			{
				if ( selection.size( ) == 1 )
				{
					AddElementtoReport addElementAction = new AddElementtoReport( (StructuredViewer) getViewer( ) );
					addElementAction.setSelectedElement( selected );
					menu.add( addElementAction );
					menu.add( new Separator( ) );
				}
			}
			menu.add( new Separator( ) );
			menu.add( refreshExplorerAction );
		}
		else
		{
			menu.add( addResourceAction );
			menu.add( new Separator( ) );
			menu.add( refreshExplorerAction );
		}
	}

	protected boolean canAddtoReport( Object transfer )
	{
		if ( transfer instanceof ReportResourceEntry )
			transfer = ( (ReportResourceEntry) transfer ).getReportElement( );
		if ( transfer instanceof ReportElementHandle
				|| transfer instanceof EmbeddedImageHandle )
		{
			if ( transfer instanceof ScalarParameterHandle
					&& ( (ScalarParameterHandle) transfer ).getContainer( ) instanceof CascadingParameterGroupHandle )
			{
				return false;
			}
			else if ( transfer instanceof StyleHandle
					&& ( (StyleHandle) transfer ).getContainer( ) instanceof ThemeHandle )
			{
				return false;
			}
			else if ( transfer instanceof ThemeHandle )
			{
				return false;
			}
			else
			{
				return true;
			}
		}
		return false;
	}

}

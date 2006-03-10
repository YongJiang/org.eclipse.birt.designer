/*************************************************************************************
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Actuate Corporation - Initial implementation.
 ************************************************************************************/

package org.eclipse.birt.report.designer.ui.editors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.birt.report.designer.core.model.SessionHandleAdapter;
import org.eclipse.birt.report.designer.core.util.mediator.request.ReportRequest;
import org.eclipse.birt.report.designer.internal.ui.editors.FileReportProvider;
import org.eclipse.birt.report.designer.internal.ui.editors.IReportEditor;
import org.eclipse.birt.report.designer.internal.ui.editors.LibraryProvider;
import org.eclipse.birt.report.designer.internal.ui.editors.parts.GraphicalEditorWithFlyoutPalette;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.ReportMultiBookPage;
import org.eclipse.birt.report.designer.internal.ui.editors.util.EditorUtil;
import org.eclipse.birt.report.designer.internal.ui.extension.EditorContributorManager;
import org.eclipse.birt.report.designer.internal.ui.extension.FormPageDef;
import org.eclipse.birt.report.designer.internal.ui.util.DataSetManager;
import org.eclipse.birt.report.designer.internal.ui.util.UIUtil;
import org.eclipse.birt.report.designer.internal.ui.views.ILibraryProvider;
import org.eclipse.birt.report.designer.internal.ui.views.data.DataViewPage;
import org.eclipse.birt.report.designer.internal.ui.views.outline.DesignerOutlinePage;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.model.api.ModuleHandle;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.ui.views.palette.PalettePage;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.INestableKeyBindingService;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.MultiPageSelectionProvider;
import org.eclipse.ui.part.PageBookView;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * 
 * Base multipage editor for report editors. Clients can subclass this class to
 * create customize report editors. Report editor pages can contributed through
 * Extendtion Point
 * org.eclipse.birt.report.designer.ui.editors.multiPageEditorContributor.
 * 
 * @see IReportEditorPage
 */
public class MultiPageReportEditor extends AbstractMultiPageEditor implements
		IPartListener,
		IReportEditor
{

	private boolean fLastDirtyState;

	private ReportMultiBookPage fPalettePage;

	private ReportMultiBookPage outlinePage;

	private ReportMultiBookPage dataPage;

	private boolean fIsHandlingActivation;

	private long fModificationStamp = -1;;

	private ModuleHandle model;

	// this is a bug because the getActiveEditor() return null, we should change
	// the getActivePage()
	// return the correct current page index.we may delete this class
	// TODO
	private static class FormEditorSelectionProvider extends
			MultiPageSelectionProvider
	{

		private ISelection globalSelection;

		/**
		 * @param multiPageEditor
		 */
		public FormEditorSelectionProvider( FormEditor formEditor )
		{
			super( formEditor );
		}

		public ISelection getSelection( )
		{
			IEditorPart activeEditor = ( (FormEditor) getMultiPageEditor( ) ).getActivePageInstance( );
			// IEditorPart activeEditor = getActivePageInstance( );
			if ( activeEditor != null )
			{
				ISelectionProvider selectionProvider = activeEditor.getSite( )
						.getSelectionProvider( );
				if ( selectionProvider != null )
					return selectionProvider.getSelection( );
			}
			return globalSelection;
		}

		/*
		 * (non-Javadoc) Method declared on <code> ISelectionProvider </code> .
		 */
		public void setSelection( ISelection selection )
		{
			IEditorPart activeEditor = ( (FormEditor) getMultiPageEditor( ) ).getActivePageInstance( );
			if ( activeEditor != null )
			{
				ISelectionProvider selectionProvider = activeEditor.getSite( )
						.getSelectionProvider( );
				if ( selectionProvider != null )
					selectionProvider.setSelection( selection );
			}
			else
			{
				this.globalSelection = selection;
				fireSelectionChanged( new SelectionChangedEvent( this,
						globalSelection ) );
			}
		}
	}

	/**
	 * Constructor
	 */
	public MultiPageReportEditor( )
	{
		super( );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.editor.FormEditor#init(org.eclipse.ui.IEditorSite,
	 *      org.eclipse.ui.IEditorInput)
	 */
	public void init( IEditorSite site, IEditorInput input )
			throws PartInitException
	{
		super.init( site, input );

		getSite( ).getWorkbenchWindow( )
				.getPartService( )
				.addPartListener( this );
		site.setSelectionProvider( new FormEditorSelectionProvider( this ) );

		IReportProvider provider = EditorUtil.getReportProvider( this, input );
		if ( provider != null && provider.getInputPath( input ) != null )
		{
			setPartName( provider.getInputPath( input ).lastSegment( ) );
		}
		else
		{
			setPartName( input.getName( ) );
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.editor.FormEditor#addPages()
	 */
	protected void addPages( )
	{
		List formPageList = EditorContributorManager.getInstance( )
				.getEditorContributor( getEditorSite( ).getId( ) ).formPageList;
		boolean error = false;
		for ( Iterator iter = formPageList.iterator( ); iter.hasNext( ); )
		{
			FormPageDef pagedef = (FormPageDef) iter.next( );
			try
			{
				if ( !error
						|| "org.eclipse.birt.report.designer.ui.editors.design.xmlsource".equals( pagedef.id ) )
				{
					addPage( pagedef.createPage( ), pagedef.displayName );
				}
			}
			catch ( Exception e )
			{
				error = true;
			}
		}

		// if(getPageCount( )>=0)
		// {
		// pageChange(0);
		// }

	}

	/**
	 * Add a IReportEditorPage to multipage editor.
	 * 
	 * @param page
	 * @param title
	 * @return
	 * @throws PartInitException
	 */
	public int addPage( IReportEditorPage page, String title )
			throws PartInitException
	{
		int index = super.addPage( page );
		if ( title != null )
		{
			setPageText( index, title );
		}
		try
		{
			page.init( createSite( page ), getEditorInput( ) );
			page.initialize( this );
		}
		catch ( Exception e )
		{
			removePage( index );
			throw new PartInitException( e.getMessage( ) );
		}
		return index;
	}

	/**
	 * Remove report editor page.
	 * 
	 * @param id
	 *            the page id.
	 */
	public void removePage( String id )
	{
		IFormPage page = findPage( id );
		if ( page != null )
		{
			removePage( page.getIndex( ) );
		}
	}

	/**
	 * Remove all report editor page.
	 */
	public void removeAllPages( )
	{
		for ( int i = pages.toArray( ).length - 1; i >= 0; i-- )
		{
			if ( pages.get( i ) != null )
				this.removePage( ( (IFormPage) pages.get( i ) ).getId( ) );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void doSave( IProgressMonitor monitor )
	{
		getActivePageInstance( ).doSave( monitor );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#doSaveAs()
	 */
	public void doSaveAs( )
	{
		getActivePageInstance( ).doSaveAs( );
		setInput( getActivePageInstance( ).getEditorInput( ) );
		// update site name
		IReportProvider provider = EditorUtil.getReportProvider( this,
				getEditorInput( ) );
		if ( provider != null )
		{
			setPartName( provider.getInputPath( getEditorInput( ) )
					.lastSegment( ) );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
	 */
	public boolean isSaveAsAllowed( )
	{
		return getActivePageInstance( ).isSaveAsAllowed( );
	}

	// /*
	// * (non-Javadoc)
	// *
	// * @see org.eclipse.ui.forms.editor.FormEditor#isDirty()
	// */
	// public boolean isDirty( )
	// {
	// fLastDirtyState = computeDirtyState( );
	// return fLastDirtyState;
	// }
	//
	// private boolean computeDirtyState( )
	// {
	// IFormPage page = getActivePageInstance( );
	// if ( page != null && page.isDirty( ) )
	// return true;
	// return false;
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#getAdapter(java.lang.Class)
	 */
	public Object getAdapter( Class type )
	{
		if ( type == IReportProvider.class )
		{
			return new FileReportProvider( );
		}

		if ( type == ILibraryProvider.class )
		{
			return new LibraryProvider( );
		}

		if ( type == PalettePage.class )
		{
			Object adapter = getPalettePage( );
			updatePaletteView( getActivePageInstance( ) );
			return adapter;
		}

		if ( type == IContentOutlinePage.class )
		{
			Object adapter = getOutlinePage( );
			updateOutLineView( getActivePageInstance( ) );
			return adapter;
		}

		if ( type == DataViewPage.class )
		{
			Object adapter = getDataPage( );
			updateDateView( getActivePageInstance( ) );
			return adapter;
		}

		if ( getActivePageInstance( ) != null )
		{
			return getActivePageInstance( ).getAdapter( type );
		}

		return super.getAdapter( type );
	}

	private void updateDateView( IFormPage activePageInstance )
	{
		if ( dataPage == null )
		{
			return;
		}

		Object adapter = activePageInstance.getAdapter( DataViewPage.class );
		dataPage.setActivePage( (IPageBookViewPage) adapter );
	}

	private void updateOutLineView( IFormPage activePageInstance )
	{
		if ( outlinePage == null )
		{
			return;
		}

		Object designOutLinePage = activePageInstance.getAdapter( IContentOutlinePage.class );
		outlinePage.setActivePage( (IPageBookViewPage) designOutLinePage );
	}

	private Object getDataPage( )
	{
		if ( dataPage == null || dataPage.isDisposed( ) )
		{
			dataPage = new ReportMultiBookPage( );
		}
		return dataPage;
	}

	private Object getOutlinePage( )
	{
		if ( outlinePage == null || outlinePage.isDisposed( ) )
		{
			outlinePage = new ReportMultiBookPage( );
		}
		return outlinePage;
	}

	private Object getPalettePage( )
	{
		if ( fPalettePage == null || fPalettePage.isDisposed( ) )
		{
			fPalettePage = new ReportMultiBookPage( );
		}
		return fPalettePage;
	}

	private void updatePaletteView( IFormPage activePageInstance )
	{

		if ( fPalettePage == null )
		{
			return;
		}

		Object palette = activePageInstance.getAdapter( PalettePage.class );
		fPalettePage.setActivePage( (IPageBookViewPage) palette );
	}

	public void pageChange( String id )
	{
		IFormPage page = findPage( id );
		if ( page != null )
		{
			pageChange( page.getIndex( ) );
		}
	}

	protected void pageChange( int newPageIndex )
	{
		int oldPageIndex = getCurrentPage( );
		if ( oldPageIndex == newPageIndex )
		{
			return;
		}
		if ( oldPageIndex != -1 )
		{
			Object oldPage = pages.get( oldPageIndex );
			Object newPage = pages.get( newPageIndex );
			// change to new page, must do it first, because must check old page
			// is canleave.
			super.pageChange( newPageIndex );
			// check new page status
			if ( !prePageChanges( oldPage, newPage ) )
			{
				super.setActivePage( oldPageIndex );
			}
		}
		else
		{
			super.pageChange( newPageIndex );
		}

		updateRelatedViews( );
		bingdingKey( newPageIndex );
	}

	// this is a bug because the getActiveEditor() return null, we should change
	// the getActivePage()
	// return the correct current page index.we may delete this method
	// TODO
	private void bingdingKey( int newPageIndex )
	{
		final IKeyBindingService service = getSite( ).getKeyBindingService( );
		final IEditorPart editor = (IEditorPart) pages.get( newPageIndex );
		if ( editor != null )
		{
			editor.setFocus( );
			// There is no selected page, so deactivate the active service.
			if ( service instanceof INestableKeyBindingService )
			{
				final INestableKeyBindingService nestableService = (INestableKeyBindingService) service;
				if ( editor != null )
				{
					nestableService.activateKeyBindingService( editor.getEditorSite( ) );
				}
				else
				{
					nestableService.activateKeyBindingService( null );
				}
			}
			else
			{

			}
		}
	}

	private void updateRelatedViews( )
	{
		updatePaletteView( getActivePageInstance( ) );
		updateOutLineView( getActivePageInstance( ) );
		updateDateView( getActivePageInstance( ) );
	}

	private boolean prePageChanges( Object oldPage, Object newPage )
	{

		boolean isNewPageValid = true;
		if ( oldPage instanceof IReportEditorPage
				&& newPage instanceof IReportEditorPage )
		{
			isNewPageValid = ( (IReportEditorPage) newPage ).onBroughtToTop( (IReportEditorPage) oldPage );
			model = SessionHandleAdapter.getInstance( ).getReportDesignHandle( );
		}

		return isNewPageValid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.editor.FormEditor#editorDirtyStateChanged()
	 */
	public void editorDirtyStateChanged( )
	{
		super.editorDirtyStateChanged( );
		markPageStale( );
	}

	private void markPageStale( )
	{
		// int currentIndex = getCurrentPage( );

		IFormPage currentPage = getActivePageInstance( );

		if ( !( currentPage instanceof IReportEditorPage ) )
		{
			return;
		}

		// if ( currentIndex != -1 )
		// {
		// for ( int i = 0; i < pages.size( ); i++ )
		// {
		// if ( i == currentIndex )
		// {
		// continue;
		// }
		// Object page = pages.get( i );
		// if ( page instanceof IReportEditorPage )
		// {
		// ( (IReportEditorPage) page ).markPageStale( ( (IReportEditorPage)
		// currentPage ).getStaleType( ) );
		// }
		// }
		// }
	}

	/**
	 * Get the current report ModuleHandle.
	 * 
	 * @return
	 */
	public ModuleHandle getModel( )
	{
		if ( model == null )
		{
			IReportProvider provider = EditorUtil.getReportProvider( this,
					getEditorInput( ) );
			if ( provider != null )
			{
				model = provider.getReportModuleHandle( getEditorInput( ) );
				return model;
			}
		}
		return model;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partActivated( IWorkbenchPart part )
	{
		if ( part != this )
		{
			if ( part instanceof PageBookView )
			{
				PageBookView view = (PageBookView) part;
				if ( view.getCurrentPage( ) instanceof DesignerOutlinePage )
				{
					ISelectionProvider provider = (ISelectionProvider) view.getCurrentPage( );
					ReportRequest request = new ReportRequest( view.getCurrentPage( ) );
					List list = new ArrayList( );
					if ( provider.getSelection( ) instanceof IStructuredSelection )
					{
						list = ( (IStructuredSelection) provider.getSelection( ) ).toList( );
					}
					request.setSelectionObject( list );
					request.setType( ReportRequest.SELECTION );
					// no convert
					// request.setRequestConvert(new
					// EditorReportRequestConvert());
					SessionHandleAdapter.getInstance( )
							.getMediator( )
							.notifyRequest( request );
					SessionHandleAdapter.getInstance( )
							.getMediator( )
							.pushState( );
				}
			}
			if ( getActivePageInstance( ) instanceof GraphicalEditorWithFlyoutPalette )
			{
				if ( ( (GraphicalEditorWithFlyoutPalette) getActivePageInstance( ) ).getGraphicalViewer( )
						.getEditDomain( )
						.getPaletteViewer( ) != null )
				{
					GraphicalEditorWithFlyoutPalette editor = (GraphicalEditorWithFlyoutPalette) getActivePageInstance( );
					GraphicalViewer view = editor.getGraphicalViewer( );
					view.getEditDomain( ).loadDefaultTool( );
				}

			}
			return;
		}

		if ( part == this )
		{
			// use the asynchronized execution to ensure correct active page
			// index.
			Display.getCurrent( ).asyncExec( new Runnable( ) {

				public void run( )
				{
					if ( getActivePageInstance( ) instanceof GraphicalEditorWithFlyoutPalette )
					{
						GraphicalEditorWithFlyoutPalette editor = (GraphicalEditorWithFlyoutPalette) getActivePageInstance( );
						GraphicalViewer view = editor.getGraphicalViewer( );

						UIUtil.resetViewSelection( view, true );
					}
				};

			} );

			if ( getEditorInput( ).exists( ) )
			{
				handleActivation( );

				SessionHandleAdapter.getInstance( )
						.setReportDesignHandle( getModel( ) );
				DataSetManager.initCurrentInstance( getEditorInput( ) );
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener#partBroughtToTop(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partBroughtToTop( IWorkbenchPart part )
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partClosed( IWorkbenchPart part )
	{
		if ( part == this )
		{
			SessionHandleAdapter.getInstance( ).clear( getModel( ) );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener#partDeactivated(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partDeactivated( IWorkbenchPart part )
	{

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partOpened( IWorkbenchPart part )
	{

	}

	/**
	 * Tell me, i am activated.
	 * 
	 */
	public void handleActivation( )
	{
		if ( fIsHandlingActivation )
			return;

		fIsHandlingActivation = true;
		try
		{
			// TODO: check external changes of file.
			// sanityCheckState( getEditorInput( ) );
		}
		finally
		{
			fIsHandlingActivation = false;
		}
	}

	/**
	 * check the input is modify by file system.
	 * 
	 * @param input
	 */
	protected void sanityCheckState( IEditorInput input )
	{
		if ( fModificationStamp == -1 )
		{
			fModificationStamp = getModificationStamp( input );
		}

		long stamp = getModificationStamp( input );
		if ( stamp != fModificationStamp )
		{
			// reset the stamp whether user choose sync or not to avoid endless
			// snag window.
			fModificationStamp = stamp;

			handleEditorInputChanged( );
		}
	}

	/**
	 * Handles an external change of the editor's input element. Subclasses may
	 * extend.
	 */
	protected void handleEditorInputChanged( )
	{

		String title = Messages.getString( "ReportEditor.error.activated.outofsync.title" ); //$NON-NLS-1$
		String msg = Messages.getString( "ReportEditor.error.activated.outofsync.message" ); //$NON-NLS-1$

		if ( MessageDialog.openQuestion( getSite( ).getShell( ), title, msg ) )
		{
			IEditorInput input = getEditorInput( );

			if ( input == null )
			{
				closeEditor( isSaveOnCloseNeeded( ) );
			}
			else
			{
				// getInputContext( ).setInput( input );
				// rebuildModel( );
				// superSetInput( input );
			}
		}
	}

	protected void closeEditor( boolean save )
	{
		getSite( ).getPage( ).closeEditor( this, save );
	}

	protected long getModificationStamp( Object element )
	{
		if ( element instanceof IEditorInput )
		{
			IReportProvider provider = EditorUtil.getReportProvider( this,
					getEditorInput( ) );
			if ( provider != null )
			{
				return computeModificationStamp( provider.getInputPath( (IEditorInput) element ) );
			}
		}

		return 0;
	}

	protected long computeModificationStamp( IPath path )
	{
		return path.toFile( ).lastModified( );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.editor.FormEditor#dispose()
	 */
	public void dispose( )
	{
		getSite( ).getWorkbenchWindow( )
				.getPartService( )
				.removePartListener( this );
		DataSetManager.removeInstance( this.getEditorInput( ) );

		if ( fPalettePage != null )
		{
			fPalettePage.dispose( );
		}
		if ( outlinePage != null )
		{
			outlinePage.dispose( );
		}
		if ( dataPage != null )
		{
			dataPage.dispose( );
		}

		super.dispose( );
	}

	public IEditorPart getEditorPart( )
	{
		return this;
	}

}

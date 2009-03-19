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

package org.eclipse.birt.report.designer.internal.ui.editors.layout;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.birt.report.designer.core.model.SessionHandleAdapter;
import org.eclipse.birt.report.designer.core.model.schematic.HandleAdapterFactory;
import org.eclipse.birt.report.designer.core.model.schematic.ListBandProxy;
import org.eclipse.birt.report.designer.core.model.schematic.RowHandleAdapter;
import org.eclipse.birt.report.designer.core.util.mediator.IColleague;
import org.eclipse.birt.report.designer.core.util.mediator.request.ReportRequest;
import org.eclipse.birt.report.designer.internal.ui.command.WrapperCommandStack;
import org.eclipse.birt.report.designer.internal.ui.editors.FileReportProvider;
import org.eclipse.birt.report.designer.internal.ui.editors.parts.GraphicalEditorWithFlyoutPalette;
import org.eclipse.birt.report.designer.internal.ui.editors.parts.ReportViewerKeyHandler;
import org.eclipse.birt.report.designer.internal.ui.editors.parts.event.IModelEventProcessor;
import org.eclipse.birt.report.designer.internal.ui.editors.parts.event.ModelEventManager;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.actions.AddGroupAction;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.actions.AddStyleAction;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.actions.ChangeDataColumnPartAction;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.actions.CopyCellContentsContextAction;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.actions.CopyPartAction;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.actions.CreateChartAction;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.actions.CreatePlaceHolderPartAction;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.actions.CutPartAction;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.actions.DeleteColumnAction;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.actions.DeleteListGroupAction;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.actions.DeleteRowAction;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.actions.DeleteTableGroupAction;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.actions.EditBindingAction;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.actions.ExportElementToLibraryPartAction;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.actions.ImportCSSStylePartAction;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.actions.IncludeDetailAction;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.actions.IncludeFooterAction;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.actions.IncludeHeaderAction;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.actions.InsertColumnLeftAction;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.actions.InsertColumnRightAction;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.actions.InsertListGroupAction;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.actions.InsertRowAboveAction;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.actions.InsertRowBelowAction;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.actions.InsertTableGroupAction;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.actions.MergeAction;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.actions.PastePartAction;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.actions.ResetImageSizeAction;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.actions.RevertToReportItemPartAction;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.actions.RevertToTemplatePartAction;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.actions.SelectColumnAction;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.actions.SelectRowAction;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.actions.SplitAction;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.actions.UseLibraryPartAction;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.editparts.GraphicalPartFactory;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.editparts.ReportElementEditPart;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.editparts.ReportRootEditPart;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.editparts.TableEditPart;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.providers.SchematicContextMenuProvider;
import org.eclipse.birt.report.designer.internal.ui.extension.ExtendedElementUIPoint;
import org.eclipse.birt.report.designer.internal.ui.extension.ExtensionPointManager;
import org.eclipse.birt.report.designer.internal.ui.extension.experimental.EditpartExtensionManager;
import org.eclipse.birt.report.designer.internal.ui.extension.experimental.PaletteEntryExtension;
import org.eclipse.birt.report.designer.internal.ui.palette.ReportFlyoutPalettePreferences;
import org.eclipse.birt.report.designer.internal.ui.palette.ReportTemplateTransferDropTargetListener;
import org.eclipse.birt.report.designer.internal.ui.util.ExceptionHandler;
import org.eclipse.birt.report.designer.internal.ui.views.actions.CopyFormatAction;
import org.eclipse.birt.report.designer.internal.ui.views.actions.PasteFormatAction;
import org.eclipse.birt.report.designer.internal.ui.views.data.DataViewPage;
import org.eclipse.birt.report.designer.internal.ui.views.data.DataViewTreeViewerPage;
import org.eclipse.birt.report.designer.internal.ui.views.outline.DesignerOutlinePage;
import org.eclipse.birt.report.designer.internal.ui.views.property.ReportPropertySheetPage;
import org.eclipse.birt.report.designer.ui.actions.ApplyStyleMenuAction;
import org.eclipse.birt.report.designer.ui.actions.ApplyThemeMenuAction;
import org.eclipse.birt.report.designer.ui.actions.DeleteStyleMenuAction;
import org.eclipse.birt.report.designer.ui.actions.EditGroupMenuAction;
import org.eclipse.birt.report.designer.ui.actions.EditStyleMenuAction;
import org.eclipse.birt.report.designer.ui.actions.GeneralInsertMenuAction;
import org.eclipse.birt.report.designer.ui.actions.InsertAggregationAction;
import org.eclipse.birt.report.designer.ui.actions.InsertExpressionMenuAction;
import org.eclipse.birt.report.designer.ui.actions.InsertGroupMenuAction;
import org.eclipse.birt.report.designer.ui.editors.IReportProvider;
import org.eclipse.birt.report.designer.ui.extensions.IExtensionConstants;
import org.eclipse.birt.report.designer.ui.views.attributes.AttributeViewPage;
import org.eclipse.birt.report.model.api.ModuleHandle;
import org.eclipse.birt.report.model.api.RowHandle;
import org.eclipse.birt.report.model.api.SlotHandle;
import org.eclipse.birt.report.model.api.elements.ReportDesignConstants;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.dnd.TemplateTransferDropTargetListener;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.DirectEditAction;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.IPropertySheetPage;

/**
 * Base report graphical editor with flyout palette.
 * 
 */
abstract public class ReportEditorWithPalette extends
		GraphicalEditorWithFlyoutPalette implements IColleague
{

	protected PaletteRoot paletteRoot;

	private EditPartFactory editPartFactoy;

	// private CommandStack commandStack;

	private ModuleHandle model;

	private ModelEventManager manager = createModelEventManager( );

	private FileReportProvider provider;

	private static final String DLG_ERROR_OPEN_ERROR_MSG = "Can't open file"; //$NON-NLS-1$

	public ReportEditorWithPalette( )
	{
		super( );
		setEditDomain( new DefaultEditDomain( this ) );
	}

	/**
	 * Constructor
	 * 
	 * @param parent
	 */
	public ReportEditorWithPalette( IEditorPart parent )
	{
		this( );
	}

	/**
	 * @return
	 */
	protected ModelEventManager createModelEventManager( )
	{
		return new ModelEventManager( );
	}

	/**
	 * @return
	 */
	protected ModelEventManager getModelEventManager( )
	{
		return manager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette#getPaletteRoot()
	 */
	abstract protected PaletteRoot getPaletteRoot( );

	/*
	 * @see org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette#getPalettePreferences()
	 */
	protected FlyoutPreferences getPalettePreferences( )
	{
		return new ReportFlyoutPalettePreferences( );
	}

	/**
	 * Creates reusable actions for all BIRT graphical editors. (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.parts.GraphicalEditor#createActions()
	 */
	protected void createActions( )
	{
		super.createActions( );
		// register merge and split actions
		IAction action = new MergeAction( this );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );

		action = new SplitAction( this );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );

		// add for support the multiple view
		action = new CreateChartAction( this );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );

		// register delete actions
		action = new DeleteRowAction( this );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );

		action = new DeleteColumnAction( this );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );

		// delete table group action
		action = new DeleteTableGroupAction( this );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );

		// register insert group actions
		action = new InsertRowAboveAction( this );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );

		action = new InsertRowBelowAction( this );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );

		action = new InsertTableGroupAction( this );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );

		action = new InsertColumnRightAction( this );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );

		action = new InsertColumnLeftAction( this );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );

		// register copy group actions
		action = new CutPartAction( this );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );

		action = new CopyPartAction( this );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );

		action = new PastePartAction( this );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );

		action = new InsertListGroupAction( this );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );

		action = new DeleteListGroupAction( this );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );

		// add style actions
		action = new AddStyleAction( this );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );

		action = new ImportCSSStylePartAction( this );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );

		// add group actions
		action = new AddGroupAction( this );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );

		action = new InsertGroupMenuAction( this );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );

		action = new ChangeDataColumnPartAction( this );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );

		// add create place holder actions
		action = new CreatePlaceHolderPartAction( this );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );

		// add create Revert to Report Item action
		action = new RevertToReportItemPartAction( this );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );

		// add create Revert to Report template action
		action = new RevertToTemplatePartAction( this );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );

		// add create place holder actions
		action = new ExportElementToLibraryPartAction( this );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );
		
		// // Add page actions
		// action = LayoutPageAction.getInstance( );
		// getActionRegistry( ).registerAction( action );
		// getSelectionActions( ).add( action.getId( ) );
		//
		// action = NormalPageAction.getInstance( );
		// getActionRegistry( ).registerAction( action );
		// getSelectionActions( ).add( action.getId( ) );
		//
		// action = MasterPageAction.getInstance( );
		// getActionRegistry( ).registerAction( action );
		// getSelectionActions( ).add( action.getId( ) );
		//
		// action = PreviewPageAction.getInstance( );
		// getActionRegistry( ).registerAction( action );
		// getSelectionActions( ).add( action.getId( ) );
		//
		// action = CodePageAction.getInstance( );
		// getActionRegistry( ).registerAction( action );
		// getSelectionActions( ).add( action.getId( ) );

		// Adds menu updater action
		action = new ApplyStyleMenuAction( this );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );

		// Adds menu updater action
		action = new ApplyThemeMenuAction( this );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );

		action = new EditStyleMenuAction( this );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );

		// Add menu update action -- delete style
		action = new DeleteStyleMenuAction( this );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );

		action = new EditGroupMenuAction( this );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );

		action = new EditBindingAction( this );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );

		// Add insert actions.
		action = new GeneralInsertMenuAction( this,
				GeneralInsertMenuAction.INSERT_TEXT_ID,
				ReportDesignConstants.TEXT_ITEM );
		getSelectionActions( ).add( action.getId( ) );
		addEditPartAction( (SelectionAction) action );

		action = new GeneralInsertMenuAction( this,
				GeneralInsertMenuAction.INSERT_LABEL_ID,
				ReportDesignConstants.LABEL_ITEM );
		getSelectionActions( ).add( action.getId( ) );
		addEditPartAction( (SelectionAction) action );

		action = new GeneralInsertMenuAction( this,
				GeneralInsertMenuAction.INSERT_DATA_ID,
				ReportDesignConstants.DATA_ITEM );
		getSelectionActions( ).add( action.getId( ) );
		addEditPartAction( (SelectionAction) action );

		action = new GeneralInsertMenuAction( this,
				GeneralInsertMenuAction.INSERT_GRID_ID,
				ReportDesignConstants.GRID_ITEM );
		getSelectionActions( ).add( action.getId( ) );
		addEditPartAction( (SelectionAction) action );

		action = new GeneralInsertMenuAction( this,
				GeneralInsertMenuAction.INSERT_LIST_ID,
				ReportDesignConstants.LIST_ITEM );
		getSelectionActions( ).add( action.getId( ) );
		addEditPartAction( (SelectionAction) action );

		action = new GeneralInsertMenuAction( this,
				GeneralInsertMenuAction.INSERT_TABLE_ID,
				ReportDesignConstants.TABLE_ITEM );
		getSelectionActions( ).add( action.getId( ) );
		addEditPartAction( (SelectionAction) action );

		action = new GeneralInsertMenuAction( this,
				GeneralInsertMenuAction.INSERT_IMAGE_ID,
				ReportDesignConstants.IMAGE_ITEM );
		getSelectionActions( ).add( action.getId( ) );
		addEditPartAction( (SelectionAction) action );

		action = new GeneralInsertMenuAction( this,
				GeneralInsertMenuAction.INSERT_DYNAMIC_TEXT_ID,
				ReportDesignConstants.TEXT_DATA_ITEM );
		getSelectionActions( ).add( action.getId( ) );
		addEditPartAction( (SelectionAction) action );

		action = new InsertAggregationAction( this );
		getSelectionActions( ).add( action.getId( ) );
		addEditPartAction( (SelectionAction) action );

		action = new InsertExpressionMenuAction( this );
		getSelectionActions( ).add( action.getId( ) );
		addEditPartAction( (SelectionAction) action );

		// add key f2 action, sent direct edit request
		action = new DirectEditAction( (IWorkbenchPart) this );
		( (DirectEditAction) action ).setDirectEditRequest( new Request( RequestConstants.REQ_OPEN ) );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );

		// insert table header, footer, detail actions.
		action = new IncludeHeaderAction( this );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );

		action = new IncludeDetailAction( this );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );

		action = new IncludeFooterAction( this );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );

		// add use library action.
		action = new UseLibraryPartAction( this );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );

		// add the selection row and column action
		action = new SelectRowAction( this );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );

		action = new SelectColumnAction( this );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );

		action = new CopyCellContentsContextAction( this );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );

		action = new CopyFormatAction( this );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );

		action = new PasteFormatAction( this );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );

		action = new ResetImageSizeAction( this );
		getActionRegistry( ).registerAction( action );
		getSelectionActions( ).add( action.getId( ) );

		registerInsertExtElementActions( );
	}

	/**
	 * Creates insert extension elements actions, and register actions into edit
	 * part.
	 */
	private void registerInsertExtElementActions( )
	{
		List points = ExtensionPointManager.getInstance( )
				.getExtendedElementPoints( );
		for ( Iterator iter = points.iterator( ); iter.hasNext( ); )
		{
			ExtendedElementUIPoint point = (ExtendedElementUIPoint) iter.next( );
			if ( ( (Boolean) point.getAttribute( IExtensionConstants.ATTRIBUTE_EDITOR_SHOW_IN_DESIGNER ) ).booleanValue( ) )
			{
				IAction action = new GeneralInsertMenuAction( this,
						point.getExtensionName( ),
						point.getExtensionName( ),
						point.getExtensionName( ) );
				getSelectionActions( ).add( action.getId( ) );
				addEditPartAction( (SelectionAction) action );
			}
		}

		PaletteEntryExtension[] entries = EditpartExtensionManager.getPaletteEntries( );
		for ( int i = 0; i < entries.length; i++ )
		{
			IAction action = new GeneralInsertMenuAction( this,
					entries[i].getItemName( ),
					entries[i].getItemName( ),
					entries[i].getLabel( ) );
			getSelectionActions( ).add( action.getId( ) );
			addEditPartAction( (SelectionAction) action );
		}
	}

	// protected void createGraphicalViewer(Composite parent) {
	// DeferredGraphicalViewer viewer = new DeferredGraphicalViewer();
	// viewer.hookRefreshListener(getRefreshManager());
	// viewer.createControl(parent);
	// setGraphicalViewer(viewer);
	// configureGraphicalViewer();
	// hookGraphicalViewer();
	// initializeGraphicalViewer();
	// // super.createGraphicalViewer(parent);
	// SessionHandleAdapter.getInstance().getMediator().addColleague(this);
	// }

	protected void initializeGraphicalViewer( )
	{
		super.initializeGraphicalViewer( );
		GraphicalViewer viewer = getGraphicalViewer( );

		if ( getModel( ) != null )
		{
			setContents( );
			hookModelEventManager( getModel( ) );
		}
		viewer.addDropTargetListener( createTemplateTransferDropTargetListener( viewer ) );
	}

	/**
	 * 
	 */
	protected void setContents( )
	{
		getGraphicalViewer( ).setContents( getModel( ) );
	}

	protected void hookModelEventManager( Object model )
	{
		manager.hookRoot( model );
		Object processor = getGraphicalViewer( ).getRootEditPart( )
				.getAdapter( IModelEventProcessor.class );
		if ( processor instanceof IModelEventProcessor )
		{
			manager.addModelEventProcessor( (IModelEventProcessor) processor );
		}
		if ( getCommandStack( ) instanceof WrapperCommandStack )
		{
			manager.hookCommandStack( (WrapperCommandStack) getCommandStack( ) );
		}
	}

	protected void unhookModelEventManager( Object model )
	{
		manager.unhookRoot( model );
		if ( getCommandStack( ) instanceof WrapperCommandStack )
		{
			manager.unhookCommandStack( (WrapperCommandStack) getCommandStack( ) );
		}
	}

	protected TemplateTransferDropTargetListener createTemplateTransferDropTargetListener(
			EditPartViewer viewer )
	{
		return new ReportTemplateTransferDropTargetListener( viewer );
	}

	@Override
	protected void createGraphicalViewer( Composite parent )
	{
		super.createGraphicalViewer( parent );

		SessionHandleAdapter.getInstance( )
				.getMediator( getModel( ) )
				.addColleague( this );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.parts.GraphicalEditor#configureGraphicalViewer()
	 */
	protected void configureGraphicalViewer( )
	{
		super.configureGraphicalViewer( );

		GraphicalViewer viewer = getGraphicalViewer( );
		ActionRegistry actionRegistry = getActionRegistry( );
		ReportRootEditPart root = new ReportRootEditPart( );
		viewer.setRootEditPart( root );

		// hook zoom actions
		hookZoom( root );

		// set key events
		viewer.setKeyHandler( new ReportViewerKeyHandler( viewer,
				actionRegistry ) );

		// configure the context menu
		ContextMenuProvider provider = new SchematicContextMenuProvider( viewer,
				actionRegistry );
		viewer.setContextMenu( provider );
		// hook the viewer into the EditDomain TODO create a function
		getEditDomain( ).addViewer( viewer );
		// acticate the viewer as selection provider for Eclipse
		getSite( ).setSelectionProvider( viewer );

		// initialize the viewer with input
		viewer.setEditPartFactory( getEditPartFactory( ) );

		ModuleHandle model = getModel( );
		WrapperCommandStack commandStack = new WrapperCommandStack( model == null ? null
				: model.getCommandStack( ) );

		viewer.getEditDomain( ).setCommandStack( commandStack );

	}

	// add supoet the report media, may be use a helpler
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.core.util.mediator.IColleague#performRequest(org.eclipse.birt.report.designer.core.util.mediator.request.ReportRequest)
	 */
	public void performRequest( ReportRequest request )
	{
		if ( ReportRequest.SELECTION.equals( request.getType( ) ) )
		{
			handleSelectionChange( request );
		}
		else if ( ReportRequest.CREATE_ELEMENT.equals( request.getType( ) ) )
		{
			handleCreateElement( request );
		}
	}

	/**
	 * @param request
	 */
	protected void handleCreateElement( ReportRequest request )
	{
		final GraphicalViewer viewer = getGraphicalViewer( );
		if ( !viewer.getControl( ).isVisible( ) )
		{
			return;
		}

		final List list = request.getSelectionModelList( );
		if ( list.size( ) != 1 )
		{
			return;
		}
		Display.getCurrent( ).asyncExec( new Runnable( ) {

			public void run( )
			{

				Object part = viewer.getEditPartRegistry( ).get( list.get( 0 ) );
				if ( part instanceof EditPart )
				{
					Request directEditRequest = new Request( ReportRequest.CREATE_ELEMENT );
					if ( ( (EditPart) part ).understandsRequest( directEditRequest ) )
					{
						( (EditPart) part ).performRequest( directEditRequest );
					}
				}
			}
		} );

	}

	/**
	 * @param request
	 */
	protected void handleSelectionChange( ReportRequest request )
	{
		List select = convertEventToGFE( request );
		if ( select == null )
		{
			return;
		}
		getGraphicalViewer( ).setSelection( new StructuredSelection( select ) );

		if ( select.size( ) > 0 )
			getGraphicalViewer( ).reveal( (EditPart) select.get( select.size( ) - 1 ) );
	}

	/**
	 * Returns the created event if the given event is editpart event
	 * 
	 * @param event
	 *            the selection changed event
	 * @return the created event
	 */
	private List convertEventToGFE( ReportRequest event )
	{
		if ( event.getSource( ) == getGraphicalViewer( ) )
		{
			return null;
		}
		ArrayList tempList = new ArrayList( );
		List list = event.getSelectionModelList( );
		int size = list.size( );

		if ( size != 0 && list.get( 0 ) instanceof RowHandle )
		{
			// Fix Bugzilla Bug 109571
			RowHandle handle = (RowHandle) list.get( 0 );

			RowHandleAdapter adapter = HandleAdapterFactory.getInstance( )
					.getRowHandleAdapter( handle );

			Object tableParent = adapter.getTableParent( );
			if ( tableParent == null )
			{
				return null;
			}
			TableEditPart part = (TableEditPart) getGraphicalViewer( ).getEditPartRegistry( )
					.get( tableParent );
			int[] selectRows = new int[]{
				adapter.getRowNumber( )
			};
			for ( int i = 1; i < size; i++ )
			{
				Object o = list.get( i );
				if ( o instanceof RowHandle )
				{
					handle = (RowHandle) o;
					adapter = HandleAdapterFactory.getInstance( )
							.getRowHandleAdapter( handle );
					// not sample table, return null
					if ( tableParent != adapter.getTableParent( ) )
					{
						return null;
					}

					int len = selectRows.length;
					int temp[] = new int[len + 1];
					System.arraycopy( selectRows, 0, temp, 0, len );
					temp[len] = adapter.getRowNumber( );
					selectRows = temp;
				}
				else
				// not suport this kind of selection
				{
					return null;
				}
			}

			if ( handle.getRoot( ) == null )
			{
				return null;
			}
			// end

			if ( part != null )
			{
				Arrays.sort( selectRows );
				int len = selectRows.length;
				if ( len > 1 )
				{
					for ( int i = 0; i < len - 1; i++ )
					{
						if ( selectRows[i + 1] - selectRows[i] != 1 )
						{
							return null;
						}
					}
				}
				part.selectRow( selectRows );
			}
			return null;
		}
		for ( int i = 0; i < size; i++ )
		{
			Object obj = list.get( i );
			if ( obj instanceof EditPart )
			{
				tempList.add( obj );
			}
			else
			{
				Object part = null;
				// if ( obj instanceof ReportElementModel )
				// {
				// obj = ( ( (ReportElementModel) obj ).getSlotHandle( ) );
				// part = getGraphicalViewer( ).getEditPartRegistry( )
				// .get( new ListBandProxy( (SlotHandle) obj ) );
				// }else
				if ( obj instanceof SlotHandle )
				{
					obj = ( (SlotHandle) obj );
					part = getGraphicalViewer( ).getEditPartRegistry( )
							.get( new ListBandProxy( (SlotHandle) obj ) );
				}
				else
				{
					part = getGraphicalViewer( ).getEditPartRegistry( )
							.get( obj );
					if ( part == null )
					{
						part = getInterestEditPart( getGraphicalViewer( ).getRootEditPart( ),
								obj );
					}
				}
				if ( part instanceof EditPart )
				{
					tempList.add( part );
				}
			}
		}

		if ( tempList.isEmpty( ) )
		{
			return null;
		}

		return tempList;
	}

	private EditPart getInterestEditPart( EditPart part, Object obj )
	{
		List chList = part.getChildren( );
		for ( int i = 0; i < chList.size( ); i++ )
		{
			ReportElementEditPart reportEditPart = (ReportElementEditPart) chList.get( i );
			if ( reportEditPart.isinterestSelection( obj ) )
			{
				return reportEditPart;
			}
			else
			{
				EditPart retValue = getInterestEditPart( reportEditPart, obj );
				if ( retValue != null )
				{
					return retValue;
				}
			}
		}
		return null;
	}

	/**
	 * Gets default edit part factory.
	 */
	protected EditPartFactory getEditPartFactory( )
	{
		if ( editPartFactoy == null )
		{
			editPartFactoy = new GraphicalPartFactory( );
		}
		return editPartFactoy;
	}

	// /*
	// * (non-Javadoc)
	// *
	// * @see org.eclipse.gef.ui.parts.GraphicalEditor#getCommandStack()
	// */
	// protected CommandStack getCommandStack( )
	// {
	// super.getCommandStack( );
	// return new WrapperCommandStack( );
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IEditorPart#init(org.eclipse.ui.IEditorSite,
	 *      org.eclipse.ui.IEditorInput)
	 */
	public void init( IEditorSite site, IEditorInput input )
			throws PartInitException
	{
		super.init( site, input );
		if ( getModel( ) == null )
		{
			throw new PartInitException( DLG_ERROR_OPEN_ERROR_MSG );
		}
	}

	/**
	 * Get report modulehandle. if model havn't been set, get the editor input
	 * modulehandle.
	 * 
	 * @return
	 */
	protected ModuleHandle getModel( )
	{
		if ( model == null )
		{
			IReportProvider reportProvider = getProvider( );
			model = reportProvider.getReportModuleHandle( getEditorInput( ) );
		}

		return model;
	}

	/**
	 * Set the report model
	 * 
	 * @param model
	 */
	protected void setModel( ModuleHandle model )
	{
		this.model = model;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.parts.GraphicalEditor#isDirty()
	 */
	public boolean isDirty( )
	{
		if (getProvider( ).getReportModuleHandle( getEditorInput( ) ) != model && getProvider( ).getReportModuleHandle( getEditorInput( ) ) != null)
		{
			return getProvider( ).getReportModuleHandle( getEditorInput( ) ).needsSave( );
		}
		if ( getModel( ) == null )
		{
			return false;
		}
		return getModel( ).needsSave( );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void doSave( IProgressMonitor monitor )
	{
		// if ( getEditorInput( ) instanceof IFileEditorInput )
		// {
		// ByteArrayOutputStream out = new ByteArrayOutputStream( );
		// try
		// {
		// getModel( ).serialize( out );
		// byte[] bytes = out.toByteArray( );
		// out.close( );
		//
		// ByteArrayInputStream is = new ByteArrayInputStream( bytes );
		// if ( ( (IFileEditorInput) getEditorInput( ) ).getFile( )
		// .exists( ) )
		// {
		// ( (IFileEditorInput) getEditorInput( ) ).getFile( )
		// .setContents( is, true, true, monitor );
		// }
		// else
		// {
		// // Save to new file.
		// ( (IFileEditorInput) getEditorInput( ) ).getFile( )
		// .create( is, true, monitor );
		// }
		// }
		// catch ( IOException e )
		// {
		// ExceptionHandler.handle( e );
		// }
		// catch ( CoreException e )
		// {
		// ExceptionHandler.handle( e );
		// }
		// // TODO: fire model changes.
		// }

		IReportProvider provider = getProvider( );

		if ( provider != null )
		{
			provider.saveReport( getModel( ), getEditorInput( ), monitor );
			firePropertyChange( PROP_DIRTY );
		}
	}

	protected IReportProvider getProvider( )
	{
		if ( provider == null )
		{
			provider = new FileReportProvider( );
		}

		return provider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.parts.GraphicalEditor#doSaveAs()
	 */
	public void doSaveAs( )
	{
		final IReportProvider provider = getProvider( );

		if ( provider != null )
		{
			IPath path = provider.getSaveAsPath( getEditorInput( ) );

			if ( path == null )
			{
				return;
			}

			final IPath origReportPath = provider.getInputPath( getEditorInput( ) );
			final IEditorInput input = provider.createNewEditorInput( path );

			setInput( input );

			IRunnableWithProgress op = new IRunnableWithProgress( ) {

				public synchronized final void run( IProgressMonitor monitor )
						throws InvocationTargetException, InterruptedException
				{
					final InvocationTargetException[] iteHolder = new InvocationTargetException[1];
					try
					{
						IWorkspaceRunnable workspaceRunnable = new IWorkspaceRunnable( ) {

							public void run( IProgressMonitor pm )
									throws CoreException
							{
								try
								{
									execute( pm );
								}
								catch ( InvocationTargetException e )
								{
									// Pass it outside the workspace runnable
									iteHolder[0] = e;
								}
								catch ( InterruptedException e )
								{
									// Re-throw as OperationCanceledException,
									// which
									// will be
									// caught and re-thrown as
									// InterruptedException
									// below.
									throw new OperationCanceledException( e.getMessage( ) );
								}
								// CoreException and OperationCanceledException
								// are
								// propagated
							}
						};

						ResourcesPlugin.getWorkspace( ).run( workspaceRunnable,
								ResourcesPlugin.getWorkspace( ).getRoot( ),
								IResource.NONE,
								monitor );
					}
					catch ( CoreException e )
					{
						throw new InvocationTargetException( e );
					}
					catch ( OperationCanceledException e )
					{
						throw new InterruptedException( e.getMessage( ) );
					}
					// Re-throw the InvocationTargetException, if any occurred
					if ( iteHolder[0] != null )
					{
						throw iteHolder[0];
					}
				}

				public void execute( final IProgressMonitor monitor )
						throws CoreException, InvocationTargetException,
						InterruptedException
				{

					try
					{

						if ( !input.exists( ) )
						{
							// Create the container if non-existent
							// createContainer( input, monitor );
						}

						IReportProvider provider = getProvider( );

						if ( provider != null )
						{
							provider.saveReport( getModel( ),
									getEditorInput( ),
									origReportPath,
									monitor );

							firePropertyChange( PROP_DIRTY );
						}
					}

					catch ( Exception e )
					{
						ExceptionHandler.handle( e );
					}
				}
			};

			try
			{
				new ProgressMonitorDialog( getSite( ).getWorkbenchWindow( )
						.getShell( ) ).run( false, true, op );
			}

			catch ( Exception e )
			{
				ExceptionHandler.handle( e );
			}
		}

	}

	/**
	 * Rebuilds editor base on editor input.
	 */
	protected void rebuildEditor( )
	{

	}

	/**
	 * Rebuilds model base on editor input.
	 */
	// protected void rebuildModel( )
	// {
	// String fileName = ( (IFileEditorInput) getEditorInput( ) ).getFile( )
	// .getLocation( )
	// .toOSString( );
	// getModel( ).setFileName( fileName );
	// }
	//
	// private IFileEditorInput createNewEditorInput( IPath path )
	// {
	// return new FileEditorInput( ResourcesPlugin.getWorkspace( )
	// .getRoot( )
	// .getFile( path ) );
	//
	// }
	// protected IPath getSaveAsPath( )
	// {
	// IEditorPart editor = UIUtil.getActiveEditor( true );
	// Object adapter = getAdapter( WizardDialog.class );
	// if ( adapter != null )
	// {
	// WizardDialog wizardDialog = (WizardDialog) adapter;
	// wizardDialog.open( );
	// if ( wizardDialog instanceof ISaveAsPathProvider )
	// return ( (ISaveAsPathProvider) wizardDialog ).getResult( );
	// }
	//
	// return null;
	// }
	//
	// protected void createContainer( IFileEditorInput input,
	// IProgressMonitor monitor ) throws CoreException
	// {
	// IContainer container = input.getFile( ).getParent( );
	// if ( !container.exists( ) && container instanceof IFolder )
	// {
	// UIUtil.createFolder( (IFolder) container, monitor );
	// }
	// }
	public Object getAdapter( Class type )
	{

		if ( type == IContentOutlinePage.class )
		{

			// ( (NonGEFSynchronizerWithMutiPageEditor)
			// getSelectionSynchronizer( ) ).add( (NonGEFSynchronizer)
			// outlinePage.getAdapter( NonGEFSynchronizer.class ) );

			// Add JS Editor as a selection listener to Outline view selections.
			// outlinePage.addSelectionChangedListener( jsEditor );
			DesignerOutlinePage outlinePage = new DesignerOutlinePage( getModel( ) );
			manager.addModelEventProcessor( outlinePage.getModelProcessor( ) );
			return outlinePage;
		}

		// return the property sheet page
		if ( type == IPropertySheetPage.class )
		{
			ReportPropertySheetPage sheetPage = new ReportPropertySheetPage( getModel( ) );
			return sheetPage;
		}

		if ( type == DataViewPage.class )
		{
			DataViewTreeViewerPage page = new DataViewTreeViewerPage( getModel( ) );
			manager.addModelEventProcessor( page.getModelProcessor( ) );
			return page;
		}

		if ( type == AttributeViewPage.class )
		{
			AttributeViewPage page = new AttributeViewPage( );
			return page;
		}

		if ( type == ModelEventManager.class )
		{
			return manager;
		}

		return super.getAdapter( type );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.editors.parts.GraphicalEditorWithFlyoutPalette#dispose()
	 */
	public void dispose( )
	{
		if ( getCommandStack( ) != null )
		{
			getCommandStack( ).flush( );
		}
		unhookModelEventManager( getModel( ) );

		SessionHandleAdapter.getInstance( )
				.getMediator( getModel( ) )
				.removeColleague( this );

		super.dispose( );

		manager = null;
	}
}
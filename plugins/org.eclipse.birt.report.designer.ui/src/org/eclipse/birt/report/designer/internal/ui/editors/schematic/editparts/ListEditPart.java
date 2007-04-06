/*******************************************************************************
 * Copyright (c) 2004 Actuate Corporation .
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.report.designer.internal.ui.editors.schematic.editparts;

import java.util.List;
import java.util.Map;

import org.eclipse.birt.report.designer.core.model.schematic.ListHandleAdapter;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.border.BaseBorder;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.border.SectionBorder;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.editpolicies.ListLayoutEditPolicy;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.editpolicies.ReportComponentEditPolicy;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.editpolicies.ReportContainerEditPolicy;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.figures.ListFigure;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.handles.AbstractGuideHandle;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.handles.TableGuideHandle;
import org.eclipse.birt.report.designer.internal.ui.util.ExceptionHandler;
import org.eclipse.birt.report.designer.internal.ui.util.UIUtil;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.designer.ui.IReportGraphicConstants;
import org.eclipse.birt.report.designer.ui.ReportPlatformUIImages;
import org.eclipse.birt.report.designer.ui.ReportPlugin;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.ListGroupHandle;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.eclipse.birt.report.model.api.command.ContentEvent;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editpolicies.NonResizableEditPolicy;

/**
 * List element edit part.
 * 
 */
public class ListEditPart extends ReportElementEditPart
{

	private static final String GUIDEHANDLE_TEXT = Messages
			.getString( "ListEditPart.GUIDEHANDLE_TEXT" ); //$NON-NLS-1$

	/**
	 * Constructor.
	 * 
	 * @param obj
	 */
	public ListEditPart( Object obj )
	{
		super( obj );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.editors.schematic.editparts.ReportElementEditPart#createGuideHandle()
	 */
	protected AbstractGuideHandle createGuideHandle( )
	{
		TableGuideHandle handle = new TableGuideHandle( this );
		handle.setIndicatorLabel( GUIDEHANDLE_TEXT );
		handle.setIndicatorIcon( ReportPlatformUIImages
				.getImage( IReportGraphicConstants.ICON_ELEMENT_LIST ) );
		return handle;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.editors.schematic.editparts.ReportElementEditPart#createEditPolicies()
	 */
	protected void createEditPolicies( )
	{
		installEditPolicy( EditPolicy.COMPONENT_ROLE,
				new ReportComponentEditPolicy( ) );
		installEditPolicy( EditPolicy.CONTAINER_ROLE,
				new ReportContainerEditPolicy( ) );
		installEditPolicy( EditPolicy.LAYOUT_ROLE, new ListLayoutEditPolicy( ) );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.editors.schematic.editparts.ReportElementEditPart#refreshFigure()
	 */
	public void refreshFigure( )
	{
		refreshBorder( getListHandleAdapt( ).getHandle( ),
				(BaseBorder) getFigure( ).getBorder( ) );

		( (SectionBorder) ( getFigure( ).getBorder( ) ) )
				.setPaddingInsets( getListHandleAdapt( ).getPadding(
						getFigure( ).getInsets( ) ) );

		refreshMargin( );

		refreshBackground( (DesignElementHandle) getModel( ) );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractEditPart#refreshChildren()
	 */
	public void refreshChildren( )
	{
		super.refreshChildren( );
		List list = getChildren( );
		int size = list.size( );
		for ( int i = 0; i < size; i++ )
		{
			( (ListBandEditPart) list.get( i ) ).refreshChildren( );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	protected IFigure createFigure( )
	{
		ListFigure figure = new ListFigure( );
		figure.setOpaque( false );

		return figure;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.editors.schematic.editparts.ReportElementEditPart#getModelChildren()
	 */
	protected List getModelChildren( )
	{
		return getListHandleAdapt( ).getChildren( );
	}

	private ListHandleAdapter getListHandleAdapt( )
	{
		return (ListHandleAdapter) getModelAdapter( );
	}

	/**
	 * Insert group in list element
	 */
	public boolean insertGroup( )
	{
		return UIUtil.createGroup( getListHandleAdapt( ).getHandle( ) );
	}

	/**
	 * Insert group in list element
	 * 
	 * @param position
	 *            insert position
	 */
	public boolean insertGroup( int position )
	{
		return UIUtil
				.createGroup( getListHandleAdapt( ).getHandle( ), position );
	}

	/**
	 * Remove group
	 * 
	 * @param group
	 */
	public void removeGroup( Object group )
	{
		try
		{
			getListHandleAdapt( ).removeGroup( group );
		}
		catch ( SemanticException e )
		{
			ExceptionHandler.handle( e );
		}
	}

	/**
	 * Check if inlucde header/footer
	 * 
	 * @param bool
	 * @param id
	 */
	public void includeSlotHandle( boolean bool, int id )
	{
		Object model = getListHandleAdapt( ).getChild( id );
		ListBandEditPart part = (ListBandEditPart) getViewer( )
				.getEditPartRegistry( ).get( model );
		if ( part == null )
		{
			return;
		}
		part.setRenderVisile( bool );
	}

	/**
	 * Check if inlucde header/footer
	 * 
	 * @param id
	 */
	public boolean isIncludeSlotHandle( int id )
	{
		Object model = getListHandleAdapt( ).getChild( id );
		ListBandEditPart part = (ListBandEditPart) getViewer( )
				.getEditPartRegistry( ).get( model );
		if ( part == null )
		{
			return false;
		}
		return part.isRenderVisile( );
	}

	public void showTargetFeedback( Request request )
	{
		if ( this.getSelected( ) == 0 && isActive( )
				&& request.getType( ) == RequestConstants.REQ_SELECTION )
		{
			if ( isFigureLeft( request ) )
			{
				this.getViewer( ).setCursor(
						ReportPlugin.getDefault( ).getLeftCellCursor( ) );
			}
			else
			{
				this.getViewer( ).setCursor(
						ReportPlugin.getDefault( ).getRightCellCursor( ) );
			}
		}
		super.showTargetFeedback( request );
	}

	public void eraseTargetFeedback( Request request )
	{
		if ( isActive( ) )
		{
			this.getViewer( ).setCursor( null );
		}
		super.eraseTargetFeedback( request );
	}

	protected void addChildVisual( EditPart part, int index )
	{
		// make sure we don't keep a select cell cursor after new contents
		// are added
		this.getViewer( ).setCursor( null );
		super.addChildVisual( part, index );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.birt.report.designer.internal.ui.editors.schematic.editparts.ReportElementEditPart#isinterest(java.lang.Object)
	 */
	public boolean isinterest( Object model )
	{
		if (model instanceof ListGroupHandle)
		{
			if (getModelAdapter( ).isChildren((DesignElementHandle )model))
			{
				return true;
			}
		}
		return super.isinterest( model ) ;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.birt.report.designer.internal.ui.editors.schematic.editparts.ReportElementEditPart#contentChange(java.util.Map)
	 */
	protected void contentChange( Map info )
	{
		Object action = info.get(GraphicsViewModelEventProcessor.CONTENT_EVENTTYPE );
		if (action instanceof Integer)
		{
			int intValue = ((Integer)action).intValue( );
			if (intValue == ContentEvent.REMOVE)
			{
				List list = (List)info.get( GraphicsViewModelEventProcessor.EVENT_CONTENTS );
				int size = list.size( );
				for (int i=0;i<size; i++)
				{
					Object obj = list.get( i );
					if (obj instanceof DesignElementHandle)
					{
						getListHandleAdapt( ).remove( obj );
					}
				}
			}
		}
		
		
		super.contentChange( info );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.birt.report.designer.internal.ui.editors.schematic.editparts.ReportElementEditPart#getResizePolice(org.eclipse.gef.EditPolicy)
	 */
	public EditPolicy getResizePolice(EditPolicy parentPolice)
	{
		return new NonResizableEditPolicy( );
	}
}
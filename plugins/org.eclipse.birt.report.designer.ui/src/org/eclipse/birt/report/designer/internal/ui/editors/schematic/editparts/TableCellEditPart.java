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

package org.eclipse.birt.report.designer.internal.ui.editors.schematic.editparts;

import java.util.List;

import org.eclipse.birt.report.designer.core.model.schematic.CellHandleAdapter;
import org.eclipse.birt.report.designer.core.model.schematic.HandleAdapterFactory;
import org.eclipse.birt.report.designer.core.model.schematic.RowHandleAdapter;
import org.eclipse.birt.report.designer.core.model.schematic.TableHandleAdapter;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.border.CellBorder;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.border.CellPaddingBorder;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.editpolicies.ReportContainerEditPolicy;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.editpolicies.ReportFlowLayoutEditPolicy;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.figures.CellFigure;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.tools.CellDragTracker;
import org.eclipse.birt.report.designer.internal.ui.layout.ReportFlowLayout;
import org.eclipse.birt.report.designer.internal.ui.util.ExceptionHandler;
import org.eclipse.birt.report.model.activity.NotificationEvent;
import org.eclipse.birt.report.model.activity.SemanticException;
import org.eclipse.birt.report.model.api.CellHandle;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.StyleHandle;
import org.eclipse.birt.report.model.command.PropertyEvent;
import org.eclipse.birt.report.model.elements.Cell;
import org.eclipse.birt.report.model.elements.DesignChoiceConstants;
import org.eclipse.birt.report.model.elements.Style;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;

/**
 * <p>
 * Table cell element editPart
 * </p>
 *  
 */
public class TableCellEditPart extends ReportElementEditPart
{

	/**
	 * Constructor
	 * @param obj
	 */
	public TableCellEditPart( Object obj )
	{
		super( obj );

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractEditPart#getModelChildren()
	 */
	protected List getModelChildren( )
	{
		return getCellAdapter( ).getChildren( );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	protected IFigure createFigure( )
	{
		CellFigure figure = new CellFigure( );
		ReportFlowLayout rflayout = new ReportFlowLayout( );
		figure.setLayoutManager( rflayout );
		figure.setOpaque( false );

		return figure;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.actuate.iard.design.core.Listener#notify(com.actuate.iard.design.core.DesignElement,
	 *      com.actuate.iard.design.activity.NotificationEvent)
	 */
	public void elementChanged( DesignElementHandle focus, NotificationEvent ev )
	{
		markDirty( true );
		switch ( ev.getEventType( ) )
		{
			case NotificationEvent.CONTENT_EVENT :
			{
				( (TableEditPart) getParent( ) ).refreshChildren( );
				refreshChildren( );
				refreshVisuals( );
				break;

			}
			case NotificationEvent.ELEMENT_DELETE_EVENT :
			{
				( (TableEditPart) getParent( ) ).refreshChildren( );
				break;
			}
			case NotificationEvent.PROPERTY_EVENT :
			{
				refresh( );

				PropertyEvent event = (PropertyEvent) ev;
				if ( event.getPropertyName( ).startsWith( "border" ) )//$NON-NLS-1$
				{
					refreshVisuals( );
					getFigure( ).invalidateTree( );
				}
				if ( Cell.COL_SPAN_PROP.equals( event.getPropertyName( ) )
						|| Cell.ROW_SPAN_PROP.equals( event.getPropertyName( ) ) )
				{
					( (TableEditPart) getParent( ) ).refreshChildren( );
					( (TableEditPart) getParent( ) ).reLayout( );
				}

				if ( event.getPropertyName( ).equals( Style.PADDING_TOP_PROP )
						|| event.getPropertyName( )
								.equals( Style.PADDING_BOTTOM_PROP )
						|| event.getPropertyName( )
								.equals( Style.PADDING_LEFT_PROP )
						|| event.getPropertyName( )
								.equals( Style.PADDING_RIGHT_PROP )
						|| event.getPropertyName( )
								.equals( Style.TEXT_ALIGN_PROP )
						|| event.getPropertyName( )
								.equals( Style.VERTICAL_ALIGN_PROP ) )
				{
					getFigure( ).getParent( ).revalidate( );
				}
				break;
			}
			case NotificationEvent.STYLE_EVENT :
			{
				( (TableEditPart) getParent( ) ).markDirty( true );
				getFigure( ).getParent( ).revalidate( );
				refresh( );
			}
			default :
				break;
		}

	}

	private Dimension getCellDimension( )
	{
		int h = 0;
		int w = 0;

		TableEditPart tablePart = (TableEditPart) getParent( );

		int rNumber = getRowNumber( );
		int cNumber = getColumnNumber( );

		if ( rNumber > 0 && cNumber > 0)
		{
			for ( int i = rNumber; i < rNumber + getRowSpan( ); i++ )
			{
				h += tablePart.caleVisualHeight( i );
			}
			
			for ( int j = cNumber; j < cNumber + getColSpan( ); j++ )
			{
				w += tablePart.caleVisualWidth( j );
			}
		}
		return new Dimension( w, h );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.designer.internal.ui.editors.schematic.editparts.AbstractReportEditPart#refreshFigure()
	 */
	public void refreshFigure( )
	{
		Insets ist = getCellAdapter( ).getPadding( getFigure( ).getInsets( ) );
		Dimension dim = getCellDimension( );

		if ( ist.getHeight( ) < dim.height && ist.getWidth( ) < dim.width )
		{
			( (CellPaddingBorder) ( getFigure( ).getBorder( ) ) ).setInsets( ist );
		}

		StyleHandle style = ( (CellHandle) getModel( ) ).getPrivateStyle( );

		String hAlign = style.getTextAlign( );
		String vAlign = style.getVerticalAlign( );

		ReportFlowLayout rflayout = (ReportFlowLayout) getFigure( ).getLayoutManager( );

		if ( DesignChoiceConstants.TEXT_ALIGN_LEFT.equals( hAlign ) )
		{
			rflayout.setMajorAlignment( ReportFlowLayout.ALIGN_LEFTTOP );
		}
		else if ( DesignChoiceConstants.TEXT_ALIGN_CENTER.equals( hAlign ) )
		{
			rflayout.setMajorAlignment( ReportFlowLayout.ALIGN_CENTER );
		}
		else if ( DesignChoiceConstants.TEXT_ALIGN_RIGHT.equals( hAlign ) )
		{
			rflayout.setMajorAlignment( ReportFlowLayout.ALIGN_RIGHTBOTTOM );
		}

		if ( DesignChoiceConstants.VERTICAL_ALIGN_TOP.equals( vAlign ) )
		{
			rflayout.setMinorAlignment( ReportFlowLayout.ALIGN_LEFTTOP );
		}
		else if ( DesignChoiceConstants.VERTICAL_ALIGN_MIDDLE.equals( vAlign ) )
		{
			rflayout.setMinorAlignment( ReportFlowLayout.ALIGN_CENTER );
		}
		else if ( DesignChoiceConstants.VERTICAL_ALIGN_BOTTOM.equals( vAlign ) )
		{
			rflayout.setMinorAlignment( ReportFlowLayout.ALIGN_RIGHTBOTTOM );
		}

		rflayout.layout( getFigure( ) );

		updateBlankString( );

		refreshBorder( getCellAdapter( ).getHandle( ), new CellBorder( ) );

		refreshBackground( (DesignElementHandle) getModel( ) );
	}

	/**
	 * Draws the string when the cell is empty
	 */
	public void updateBlankString( )
	{
		if ( getModelChildren( ).size( ) == 0 )
		{
			TableHandleAdapter tha = ( (TableEditPart) getParent( ) ).getTableAdapter( );

			int col = ( tha.getColumnCount( ) + 1 ) / 2;
			if ( col < 1 )
			{
				col = 1;
			}

			if ( col == getColumnNumber( ) )
			{
				RowHandleAdapter rha = HandleAdapterFactory.getInstance( )
						.getRowHandleAdapter( tha.getRow( getRowNumber( ) ) );

				( (CellFigure) getFigure( ) ).setBlankString( rha.getTypeString( ) );
			}
			else
			{
				( (CellFigure) getFigure( ) ).setBlankString( null );
			}
		}
		else
		{
			( (CellFigure) getFigure( ) ).setBlankString( null );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
	 */
	protected void createEditPolicies( )
	{
		installEditPolicy( EditPolicy.COMPONENT_ROLE, null );
		installEditPolicy( EditPolicy.LAYOUT_ROLE,
				new ReportFlowLayoutEditPolicy( ) );
		installEditPolicy( EditPolicy.CONTAINER_ROLE,
				new ReportContainerEditPolicy( ) );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.EditPart#getDragTracker(org.eclipse.gef.Request)
	 */
	public DragTracker getDragTracker( Request req )
	{
		return new CellDragTracker( this );
	}

	/*
	 * Gets the paint layer (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getLayer(java.lang.Object)
	 */
	public IFigure getLayer( Object key )
	{
		if ( getParent( ) instanceof TableEditPart )
		{
			return ( (TableEditPart) getParent( ) ).getLayer( key );
		}
		return super.getLayer( key );
	}

	/**
	 * Gets the row number
	 * 
	 * @return the row number
	 */
	public int getRowNumber( )
	{
		return getCellAdapter( ).getRowNumber( );
	}

	/**
	 * Gets the column number
	 * 
	 * @return the column number
	 */
	public int getColumnNumber( )
	{
		return getCellAdapter( ).getColumnNumber( );
	}

	/**
	 * Gets the column span
	 * 
	 * @return the column span
	 */
	public int getColSpan( )
	{
		return getCellAdapter( ).getColumnSpan( );
	}

	/**
	 * Sets the column span
	 * 
	 * @param colSpan
	 */
	public void setColumnSpan( int colSpan )
	{
		try
		{
			getCellAdapter( ).setColumnSpan( colSpan );
		}
		catch ( SemanticException e )
		{
			ExceptionHandler.handle( e );
		}
	}

	/**
	 * Gets the row span
	 * 
	 * @return the row span
	 */
	public int getRowSpan( )
	{
		return getCellAdapter( ).getRowSpan( );
	}

	/**
	 * Sets the row span
	 * 
	 * @param rowSpan
	 */
	public void setRowSpan( int rowSpan )
	{
		try
		{
			getCellAdapter( ).setRowSpan( rowSpan );
		}
		catch ( SemanticException e )
		{
			ExceptionHandler.handle( e );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.EditPart#getTargetEditPart(org.eclipse.gef.Request)
	 */
	public EditPart getTargetEditPart( Request request )
	{
		return super.getTargetEditPart( request );
	}

	/**
	 * Gets the edit part bounds
	 * 
	 * @return the edit part bounds
	 */
	public Rectangle getBounds( )
	{
		return getFigure( ).getBounds( );
	}

	protected CellHandleAdapter getCellAdapter( )
	{
		return (CellHandleAdapter) getModelAdapter( );
	}
}
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

package org.eclipse.birt.report.item.crosstab.internal.ui.editors.editparts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.birt.report.designer.core.util.mediator.request.ReportRequest;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.border.BaseBorder;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.editparts.AbstractCellEditPart;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.editparts.AbstractReportEditPart;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.editparts.AbstractTableEditPart;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.editparts.ReportElementEditPart;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.editparts.TableUtil;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.editpolicies.ReportComponentEditPolicy;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.editpolicies.ReportContainerEditPolicy;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.figures.TableFigure;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.handles.AbstractGuideHandle;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.handles.TableGuideHandle;
import org.eclipse.birt.report.designer.internal.ui.layout.ITableLayoutCell;
import org.eclipse.birt.report.designer.internal.ui.layout.ITableLayoutOwner;
import org.eclipse.birt.report.designer.internal.ui.layout.TableLayout;
import org.eclipse.birt.report.designer.util.DEUtil;
import org.eclipse.birt.report.item.crosstab.core.ICrosstabReportItemConstants;
import org.eclipse.birt.report.item.crosstab.core.ILevelViewConstants;
import org.eclipse.birt.report.item.crosstab.core.de.CrosstabReportItemHandle;
import org.eclipse.birt.report.item.crosstab.internal.ui.editors.editpolicies.CrosstabXYLayoutEditPolicy;
import org.eclipse.birt.report.item.crosstab.internal.ui.editors.model.CrosstabHandleAdapter;
import org.eclipse.birt.report.item.crosstab.internal.ui.editors.model.VirtualCrosstabCellAdapter;
import org.eclipse.birt.report.item.crosstab.internal.ui.util.CrosstabUIHelper;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.DimensionHandle;
import org.eclipse.birt.report.model.api.ExtendedItemHandle;
import org.eclipse.birt.report.model.api.elements.DesignChoiceConstants;
import org.eclipse.birt.report.model.api.extension.ExtendedElementException;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayeredPane;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayeredPane;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.GuideLayer;
import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import org.eclipse.swt.graphics.Image;

/**
 * Crosstab item editpart.The modle is extended handle.
 */
// TODO add a element to the empty croass table item
// TODO don't support the table border padding
// TODO Draw the virtual cell figure back fround text half done
public class CrosstabTableEditPart extends AbstractTableEditPart implements PropertyChangeListener
{

	public static final String CELL_HANDLE_LAYER = "Cell handles layer"; //$NON-NLS-1$
	private static final String GUIDEHANDLE_TEXT = "Crosstab";

	// LEFT RIGHT is define virtual editpart column size.
	private static final double LEFT = 30.0;
	private static final double RIGHT = 100.0 - LEFT;

	private static final int DEFAULT_HEIGHT = 23;
	private static final int BIG_DEFAULT_HEIGHT = 85;
	CrosstabHandleAdapter adapter;

	private boolean isReload = false;
	
	/**
	 * Constructor
	 */
	public CrosstabTableEditPart( )
	{
		super( null );
	}

	/**
	 * Constructor
	 * 
	 * @param model
	 */
	public CrosstabTableEditPart( Object model )
	{
		super( model );

	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.birt.report.designer.internal.ui.editors.schematic.editparts.ReportElementEditPart#activate()
	 */
	public void activate( )
	{
		getViewer( ).addPropertyChangeListener( this );
		super.activate( );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.birt.report.designer.internal.ui.editors.schematic.editparts.ReportElementEditPart#deactivate()
	 */
	public void deactivate( )
	{
		getViewer( ).removePropertyChangeListener( this );
		super.deactivate( );
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.editors.schematic.editparts.ReportElementEditPart#setModel(java.lang.Object)
	 */
	public void setModel( Object model )
	{
		// TODO Auto-generated method stub
		super.setModel( model );
		if ( model != null )
		{
			try
			{
				adapter = new CrosstabHandleAdapter( (CrosstabReportItemHandle) ( (ExtendedItemHandle) model ).getReportItem( ) );
			}
			catch ( ExtendedElementException e )
			{
				throw new RuntimeException( "load extended item error" );
			}
		}
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
		Image image = CrosstabUIHelper.getImage( CrosstabUIHelper.CROSSTAB_IMAGE );
		handle.setIndicatorIcon( image );
		//handle.setIndicatorIcon( ReportPlatformUIImages.getImage( IReportGraphicConstants.ICON_ELEMENT_TABLE ) );
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
				new ReportComponentEditPolicy( ) {

					public boolean understandsRequest( Request request )
					{
						if ( RequestConstants.REQ_DIRECT_EDIT.equals( request.getType( ) )
								|| RequestConstants.REQ_OPEN.equals( request.getType( ) )
								|| ReportRequest.CREATE_ELEMENT.equals( request.getType( ) ) )
							return true;
						return super.understandsRequest( request );
					}
				} );
		installEditPolicy( EditPolicy.CONTAINER_ROLE,
				new ReportContainerEditPolicy( ) );

		installEditPolicy( EditPolicy.LAYOUT_ROLE,
				new CrosstabXYLayoutEditPolicy( (XYLayout) getContentPane( ).getLayoutManager( ) ) );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.editors.schematic.editparts.ReportElementEditPart#refreshFigure()
	 */

	// TODO only refresh the boder and background
	public void refreshFigure( )
	{
		refreshBorder( getCrosstabHandleAdapter( ).getDesignElementHandle( ),
				(BaseBorder) getFigure( ).getBorder( ) );
		refreshBackground( getCrosstabHandleAdapter( ).getDesignElementHandle( ) );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	protected IFigure createFigure( )
	{
		TableFigure viewport = new TableFigure( );
		viewport.setOpaque( false );

		innerLayers = new FreeformLayeredPane( );
		createLayers( innerLayers );
		viewport.setContents( innerLayers );
		return viewport;
	}

	/**
	 * Creates the top-most set of layers on the given layered pane.
	 * 
	 * @param layeredPane
	 *            the parent for the created layers
	 */
	protected void createLayers( LayeredPane layeredPane )
	{
		Figure figure = new FreeformLayer( );
		figure.setOpaque( false );
		layeredPane.add( figure, CELL_HANDLE_LAYER );
		layeredPane.add( getPrintableLayers( ), PRINTABLE_LAYERS );
		layeredPane.add( new FreeformLayer( ), HANDLE_LAYER );
		layeredPane.add( new GuideLayer( ), GUIDE_LAYER );
	}

	/**
	 * this layer may be a un-useful layer.
	 * 
	 * @return the layered pane containing all printable content
	 */
	protected LayeredPane getPrintableLayers( )
	{
		if ( printableLayers == null )
			printableLayers = createPrintableLayers( );
		return printableLayers;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractEditPart#getModelChildren()
	 */
	protected List getModelChildren( )
	{
		List retValue = getCrosstabHandleAdapter( ).getModelList( );
		return retValue;
	}

	/**
	 * @return select bounds
	 */
	public Rectangle getSelectBounds( )
	{
		List list = TableUtil.getSelectionCells( this );
		int size = list.size( );
		ITableLayoutCell[] parts = new ITableLayoutCell[size];
		list.toArray( parts );

		ITableLayoutCell[] caleNumber = getMinAndMaxNumber( parts );
		ITableLayoutCell minRow = caleNumber[0];
		ITableLayoutCell maxColumn = caleNumber[3];

		Rectangle min = ( (CrosstabCellEditPart) minRow ).getBounds( )
				.getCopy( );
		Rectangle max = ( (CrosstabCellEditPart) maxColumn ).getBounds( )
				.getCopy( );

		return min.union( max );
	}

	/**
	 * Gets the top, left, right, bottom of edit part.
	 * 
	 * @param parts
	 * @return cell edit parts.
	 */
	public ITableLayoutCell[] getMinAndMaxNumber( ITableLayoutCell[] parts )
	{
		if ( parts == null || parts.length == 0 )
		{
			return null;
		}
		int size = parts.length;
		ITableLayoutCell leftTopPart = parts[0];
		ITableLayoutCell leftBottomPart = parts[0];

		ITableLayoutCell rightBottomPart = parts[0];
		ITableLayoutCell rightTopPart = parts[0];
		for ( int i = 1; i < size; i++ )
		{
			ITableLayoutCell part = parts[i];
			if ( part == null )
			{
				continue;
			}

			if ( part.getRowNumber( ) <= leftTopPart.getRowNumber( )
					&& part.getColumnNumber( ) <= leftTopPart.getColumnNumber( ) )
			{
				leftTopPart = part;
			}

			if ( part.getRowNumber( ) <= rightTopPart.getRowNumber( )
					&& part.getColumnNumber( ) + part.getColSpan( ) - 1 >= leftTopPart.getColumnNumber( ) )
			{
				rightTopPart = part;
			}

			if ( part.getColumnNumber( ) <= leftBottomPart.getColumnNumber( )
					&& part.getRowNumber( ) + part.getRowSpan( ) - 1 >= leftBottomPart.getRowNumber( ) )
			{
				leftBottomPart = part;
			}

			if ( part.getRowNumber( ) + part.getRowSpan( ) - 1 >= rightBottomPart.getRowNumber( )
					&& part.getColumnNumber( ) + part.getColSpan( ) - 1 >= rightBottomPart.getColumnNumber( ) )
			{
				rightBottomPart = part;
			}
		}
		return new ITableLayoutCell[]{
				leftTopPart, rightTopPart, leftBottomPart, rightBottomPart
		};
	}

	/**
	 * @return
	 */
	public CrosstabHandleAdapter getCrosstabHandleAdapter( )
	{
		return adapter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.layout.ITableLayoutOwner#getColumnCount()
	 */
	public int getColumnCount( )
	{
		return getCrosstabHandleAdapter( ).getColumnCount( );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.layout.ITableLayoutOwner#getColumnWidth(int)
	 */
	public ITableLayoutOwner.DimensionInfomation getColumnWidth(
			final int number )
	{
		// return getCrosstabHandleAdapter( ).getColumnWidth( number );
		DimensionHandle handle = getCrosstabHandleAdapter( ).getColumnWidth( number );
		if ( handle == null )// all is virtual editpat
		{
			if (number > 1 && getCrosstabHandleAdapter( ).getColumnWidth( number-1 ) != null)
			{
				return   new ITableLayoutOwner.DimensionInfomation(0, null);
			}
			if (number < getColumnCount( ) && getCrosstabHandleAdapter( ).getColumnWidth( number+1 ) != null)
			{
				return   new ITableLayoutOwner.DimensionInfomation(0, null);
			}
			return getVirtualDimension( new Conditional( ) {

				public boolean evaluate( EditPart editpart )
				{
					Object obj = editpart.getModel( );
					if ( obj instanceof VirtualCrosstabCellAdapter )
					{
						return number == ( (VirtualCrosstabCellAdapter) obj ).getColumnNumber( );
					}
					return super.evaluate( editpart );
				}
			} );
		}
		return new ITableLayoutOwner.DimensionInfomation( handle.getMeasure( ),
				handle.getUnits( ) );
	}

	/**
	 * @param condion
	 * @return
	 */
	private ITableLayoutOwner.DimensionInfomation getVirtualDimension(
			Conditional condion )
	{
		List parts = getChildren( );
		int size = parts.size( );
		for ( int i = 0; i < size; i++ )
		{
			EditPart part = (EditPart) parts.get( i );
			// Object obj = ((EditPart)parts.get( i )).getModel( );
			if ( condion.evaluate( part )
					&& part.getModel( ) instanceof VirtualCrosstabCellAdapter )
			{
				int area = ( (VirtualCrosstabCellAdapter) part.getModel( ) ).getType( );
				// if (number ==
				// ((VirtualCrosstabCellAdapter)obj).getColumnNumber( ))
				{
					return getDimensionInfomation( area );
				}
			}
		}
		return null;
	}

	/**
	 * @param condion
	 * @return
	 */
	private int getRowHeight( Conditional condion )
	{
		List parts = getChildren( );
		int size = parts.size( );
		for ( int i = 0; i < size; i++ )
		{
			EditPart part = (EditPart) parts.get( i );
			// Object obj = ((EditPart)parts.get( i )).getModel( );
			if ( condion.evaluate( part )
					&& part.getModel( ) instanceof VirtualCrosstabCellAdapter )
			{
				int area = ( (VirtualCrosstabCellAdapter) part.getModel( ) ).getType( );
				switch ( area )
				{
					case VirtualCrosstabCellAdapter.IMMACULATE_TYPE :
					case VirtualCrosstabCellAdapter.COLUMN_TYPE :
						return DEFAULT_HEIGHT;
					case VirtualCrosstabCellAdapter.ROW_TYPE :
					case VirtualCrosstabCellAdapter.MEASURE_TYPE :
						return BIG_DEFAULT_HEIGHT;
					default :
						return DEFAULT_HEIGHT;
				}
			}
		}
		return DEFAULT_HEIGHT;
	}

	/**
	 * @param area
	 * @return
	 */
	private ITableLayoutOwner.DimensionInfomation getDimensionInfomation(
			int area )
	{
		switch ( area )
		{
			case VirtualCrosstabCellAdapter.IMMACULATE_TYPE :
			case VirtualCrosstabCellAdapter.ROW_TYPE :
				return new ITableLayoutOwner.DimensionInfomation( LEFT,
						DesignChoiceConstants.UNITS_PERCENTAGE );
			case VirtualCrosstabCellAdapter.COLUMN_TYPE :
			case VirtualCrosstabCellAdapter.MEASURE_TYPE :
				return new ITableLayoutOwner.DimensionInfomation( RIGHT,
						DesignChoiceConstants.UNITS_PERCENTAGE );
			default :
				return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.layout.ITableLayoutOwner#getColumnWidthValue(int)
	 */
	public int getColumnWidthValue( int number )
	{
		DimensionHandle handle = getCrosstabHandleAdapter( ).getColumnWidth( number );
		if ( handle == null )
		{
			return getDefaultWidth( number );
		}
		if ( DesignChoiceConstants.UNITS_PERCENTAGE.equals( handle.getUnits( ) ) )
		{
			Dimension dim = getFigure( ).getParent( )
					.getClientArea( )
					.getSize( );
			int containerWidth = dim.width;
			return (int) ( handle.getMeasure( ) * containerWidth / 100 );

		}
		int px = (int) DEUtil.convertoToPixel( handle );
		if ( px <= 0 )
		{
			return getDefaultWidth( number );
		}

		return px;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.core.model.IModelAdaptHelper#getPreferredSize()
	 */
	public Dimension getPreferredSize( )
	{
		Dimension retValue = getFigure( ).getParent( )
				.getClientArea( )
				.getSize( );
		Rectangle rect = getBounds( );

		if ( rect.width > 0 )
		{
			retValue.width = rect.width;
		}
		if ( rect.height > 0 )
		{
			retValue.height = rect.height;
		}
		return retValue;
	}
	
	/**
	 * Get the default width.
	 * 
	 * @param colNumber
	 *            The column number.
	 * @return The default width.
	 */
	public int getDefaultWidth( int colNumber )
	{
		Dimension size = getPreferredSize( )
				.shrink( getFigure( ).getInsets( ).getWidth( ),
						getFigure( ).getInsets( ).getHeight( ) );;
		if ( getRowCount( ) == 0 )
		{
			return size.width;
		}

		int allNumbers = getColumnCount( );
		if ( allNumbers <= 0 )
		{
			return size.width;
		}
		if ( colNumber <= 0 )
		{
			return size.width;
		}
		int width = size.width;
		int columnNumber = allNumbers;
		for ( int i = 1; i < columnNumber + 1; i++ )
		{
			DimensionHandle dimHandle = getCrosstabHandleAdapter( ).getColumnWidth( colNumber );
			if ( dimHandle != null && dimHandle.getMeasure( ) > 0 )
			{
				allNumbers = allNumbers - 1;
				width = width - getColumnWidthValue( colNumber );
			}
			else if ( dimHandle == null )
			{
				ITableLayoutOwner.DimensionInfomation info = getColumnWidth( colNumber );
				if ( DesignChoiceConstants.UNITS_PERCENTAGE.equals( info.getUnits( ) ) )
				{
					Dimension dim = getFigure( ).getParent( )
							.getClientArea( )
							.getSize( );
					int containerWidth = dim.width;
					// return (int) ( info.getMeasure( ) * containerWidth / 100
					// );
					width = width
							- ( (int) ( info.getMeasure( ) * containerWidth / 100 ) );
				}
			}
		}

		if ( colNumber == allNumbers )
		{
			return width / allNumbers + width % allNumbers;
		}
		return ( width / allNumbers );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.layout.ITableLayoutOwner#getDefinedWidth()
	 */
	public String getDefinedWidth( )
	{
		return getCrosstabHandleAdapter( ).getDefinedWidth( );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.layout.ITableLayoutOwner#getRawWidth(int)
	 */
	public String getRawWidth( int columNumber )
	{
		DimensionHandle handle = getCrosstabHandleAdapter( ).getColumnWidth( columNumber );
		if ( handle == null )
		{
			ITableLayoutOwner.DimensionInfomation info = getColumnWidth( columNumber );
			if (info.getUnits( ) == null)
			{
				return "";
			}
			return String.valueOf( info.getMeasure( ) ) + info.getUnits( );
		}
		String unit = handle.getUnits( );

		if ( unit == null || unit.length( ) == 0 )
		{
			return ""; //$NON-NLS-1$
		}
		else if ( unit.equals( DesignChoiceConstants.UNITS_PERCENTAGE ) )
		{
			return String.valueOf( handle.getMeasure( ) ) + unit;
		}
		else
		{
			int px = (int) DEUtil.convertoToPixel( handle );

			if ( px <= 0 )
			{
				return String.valueOf( getDefaultWidth( columNumber ) );
			}

			return String.valueOf( px );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.layout.ITableLayoutOwner#getRowCount()
	 */
	public int getRowCount( )
	{
		return getCrosstabHandleAdapter( ).getRowCount( );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.layout.ITableLayoutOwner#getRowHeight(int)
	 */
	public ITableLayoutOwner.DimensionInfomation getRowHeight( final int number )
	{
		DimensionHandle handle = getCrosstabHandleAdapter( ).getRowHeight( number );
		if ( handle == null )// all is virtual editpat
		{
			return getVirtualDimension( new Conditional( ) {

				public boolean evaluate( EditPart editpart )
				{
					Object obj = editpart.getModel( );
					if ( obj instanceof VirtualCrosstabCellAdapter )
					{
						return number == ( (VirtualCrosstabCellAdapter) obj ).getRowNumber( );
					}
					return super.evaluate( editpart );
				}
			} );
		}
		return new ITableLayoutOwner.DimensionInfomation( handle.getMeasure( ),
				handle.getUnits( ) );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.layout.ITableLayoutOwner#getRowHeightValue(int)
	 */
	public int getRowHeightValue( final int number )
	{
		DimensionHandle handle = getCrosstabHandleAdapter( ).getRowHeight( number );
		if ( handle == null )
		{
			return getRowHeight( new Conditional( ) {

				public boolean evaluate( EditPart editpart )
				{
					if ( editpart instanceof VirtualCellEditPart
							&& ( (VirtualCellEditPart) editpart ).getRowNumber( ) == number )
					{
						return true;
					}
					return super.evaluate( editpart );
				}
			} );
		}
		int px = (int) DEUtil.convertoToPixel( handle );
		if ( px <= 0 )
		{
			px = DEFAULT_HEIGHT;
		}
		return px;
	}

	private void layoutManagerLayout( )
	{
		( (TableLayout) getContentPane( ).getLayoutManager( ) ).markDirty( );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.editors.schematic.editparts.AbstractTableEditPart#getCell(int,
	 *      int)
	 */
	public AbstractCellEditPart getCell( int rowNumber, int columnNumber )
	{
		List list = getChildren( );
		int size = list.size( );
		for ( int i = 0; i < size; i++ )
		{
			AbstractCellEditPart part = (AbstractCellEditPart) list.get( i );
			if ( rowNumber >= part.getRowNumber( )
					&& rowNumber < part.getRowNumber( ) + part.getRowSpan( )
					&& columnNumber >= part.getColumnNumber( )
					&& columnNumber < part.getColumnNumber( )
							+ part.getColSpan( ) )
			{
				return part;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractEditPart#createChild(java.lang.Object)
	 */
	protected EditPart createChild( Object model )
	{
		EditPart part = CrosstabGraphicsFactory.INSTANCEOF.createEditPart( this,
				model );
		if ( part != null )
		{
			return part;
		}
		return super.createChild( model );
	}

	private static class Conditional
	{

		public boolean evaluate( EditPart editpart )
		{
			return false;
		}
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
			( (ReportElementEditPart) list.get( i ) ).refreshChildren( );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.editors.schematic.editparts.ReportElementEditPart#notifyModelChange()
	 */
	public void notifyModelChange( )
	{
		super.notifyModelChange( );
		layoutManagerLayout( );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.editors.schematic.editparts.ReportElementEditPart#isinterest(java.lang.Object)
	 */
	public boolean isinterest( Object model )
	{
		if ( !( model instanceof DesignElementHandle ) )
		{
			return false;
		}
		DesignElementHandle handle = (DesignElementHandle) model;
		while ( handle != null )
		{
			if ( getModel( ).equals( handle ) )
			{
				return true;
			}
			handle = handle.getContainer( );
		}
		return super.isinterest( model );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#refresh()
	 */
	public void refresh( )
	{
		if (!isReload)
		{
			super.refresh( );
			isReload = true;
		}
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange( PropertyChangeEvent evt )
	{
		if (evt.getPropertyName( ).equals( AbstractReportEditPart.MODEL_EVENT_DISPATCH ))
		{
			if (AbstractReportEditPart.START.equals( evt.getNewValue( ) ))
			{
				isReload = false;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.birt.report.designer.internal.ui.editors.schematic.editparts.ReportElementEditPart#propertyChange(java.util.Map)
	 */
	protected void propertyChange( Map info )
	{
		Set set = info.keySet( );
		Iterator itor = set.iterator( );
		
		while(itor.hasNext( ))
		{
			Object obj = itor.next( );
			if (ICrosstabReportItemConstants.MEASURE_DIRECTION_PROP.equals(obj  )
					|| ICrosstabReportItemConstants.PAGE_LAYOUT_PROP.equals(obj  )
					|| ILevelViewConstants.AGGREGATION_HEADER_LOCATION_PROP.equals(obj  ))
			{
				refresh( );
				return;
			}
		}
		super.propertyChange( info );
	}
	
	/**
	 * @param parentPolice
	 * @return
	 */
	public EditPolicy getResizePolice(EditPolicy parentPolice)
	{
		return new NonResizableEditPolicy( );
	}
}

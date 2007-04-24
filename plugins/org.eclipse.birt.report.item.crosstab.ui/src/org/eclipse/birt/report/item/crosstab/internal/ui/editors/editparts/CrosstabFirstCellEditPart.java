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

import org.eclipse.birt.report.designer.internal.ui.editors.ReportColorConstants;
import org.eclipse.birt.report.designer.internal.ui.editors.parts.DeferredGraphicalViewer;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.handles.NothingLocator;
import org.eclipse.birt.report.designer.internal.ui.layout.ReportFlowLayout;
import org.eclipse.birt.report.item.crosstab.internal.ui.editors.figures.FirstCellFigure;
import org.eclipse.birt.report.item.crosstab.internal.ui.util.CrosstabUIHelper;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Locator;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.handles.AbstractHandle;
import org.eclipse.gef.tools.DragEditPartsTracker;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;

/**
 * 
 */

public class CrosstabFirstCellEditPart extends  CrosstabCellEditPart
{
	private static int MIN_WIDTH = 25;
	private static int MIN_HEIGHT = 20;//mabe define a glob field
	private MenuManager manager;
	Figure contentPane;
	ControlFigure controlFigure;
	public CrosstabFirstCellEditPart( Object model )
	{
		super( model );
	}

	protected IFigure createFigure( )
	{
		Figure figure = new FirstCellFigure();
		
		contentPane = new Figure();
		ReportFlowLayout rflayout = new ReportFlowLayout( )
		{
			public void layout( IFigure parent )
			{
				super.layout( parent );
			}
		};
		contentPane.setLayoutManager( rflayout );
		contentPane.setOpaque( false );
		
		figure.add( contentPane );
		
		controlFigure = new ControlFigure(this, new NothingLocator());
		
		figure.add( controlFigure );
		
		return figure;
	}
	
	public void refreshFigure( )
	{
		super.refreshFigure( );
		setLayoutConstraint( this, controlFigure, controlFigure.getConstraint() );
	}
	
	public IFigure getContentPane( )
	{
		return contentPane;
	}
	
	class ControlFigure extends AbstractHandle
	{

		Image image = CrosstabUIHelper.getImage( CrosstabUIHelper.LEVEL_ARROW );
		
		public ControlFigure(GraphicalEditPart owner, Locator loc)
		{
			super( owner, loc );
		}
		protected DragTracker createDragTracker( )
		{
			DragEditPartsTracker track = new DragEditPartsTracker( CrosstabFirstCellEditPart.this )
			{

				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.gef.tools.SelectEditPartTracker#handleButtonDown(int)
				 */
				protected boolean handleButtonDown( int button )
				{
					if ( getCurrentViewer( ) instanceof DeferredGraphicalViewer )
					{
						( (DeferredGraphicalViewer) getCurrentViewer( ) ).initStepDat( );
					}
					boolean bool = super.handleButtonDown( button );

//					if ( ( button == 3 || button == 1 ) )
//					// && isInState(STATE_INITIAL))
//					{
//						if ( getSourceEditPart( ) instanceof CrosstabFirstCellEditPart )
//						{
//							CrosstabFirstCellEditPart first = (CrosstabFirstCellEditPart) getSourceEditPart( );
//							if ( first.contains( getLocation( ) ) )
//							{
//								//MenuManager manager = new LevelCrosstabPopMenuProvider( getViewer( ) );
//								manager.createContextMenu( getViewer( ).getControl( ) );
//								Menu menu = manager.getMenu( );
//								
//								menu.setVisible( true );
//								return true;
//							}
//						}
//					}
					return bool;
				}
				
				protected boolean handleButtonUp( int button )
				{
					boolean bool = super.handleButtonUp( button );
					if ( ( button == 3 || button == 1 ) )
						// && isInState(STATE_INITIAL))
						{
							if ( getSourceEditPart( ) instanceof CrosstabFirstCellEditPart )
							{
								CrosstabFirstCellEditPart first = (CrosstabFirstCellEditPart) getSourceEditPart( );
								if ( first.contains( getLocation( ) ) )
								{
									//MenuManager manager = new LevelCrosstabPopMenuProvider( getViewer( ) );
									manager.createContextMenu( getViewer( ).getControl( ) );
									Menu menu = manager.getMenu( );
									
									menu.setVisible( true );
									return true;
								}
							}
						}
						return bool;
				}
			};
			return track;
		}
		public Insets getInsets( )
		{
			return new Insets(1,1,1,1);
		}
//		public void paint( Graphics graphics )
//		{
//			super.paint( graphics );
//			graphics.fillRectangle( getBounds( ) );
//		}
		
		public Rectangle getConstraint()
		{
			return new Rectangle(0,0,MIN_WIDTH, MIN_HEIGHT);
		}
		
		public Dimension getMinimumSize( int wHint, int hHint )
		{	
			return getConstraint( ).getSize( );
		}
		
		public Dimension getPreferredSize( int wHint, int hHint )
		{
			Rectangle rect = getConstraint( );
			int height = Math.max( hHint, rect.height );
			return new Dimension(rect.width, height);
		}
		
		public void addNotify( )
		{
			// TODO Auto-generated method stub
			super.addNotify( );
		}
		
		protected void paintFigure( Graphics graphics )
		{
			graphics.setBackgroundColor( ReportColorConstants.greyFillColor );
			graphics.fillRectangle( getClientArea( ) );
			graphics.drawImage( image, getImagePoint( ) );
		}
		
		private Point getImagePoint()
		{
//			Point center = getClientArea( ).getCenter( ).getCopy( );
//			center.x = center.x - image.getBounds( ).width/2;
//			center.y = center.y - image.getBounds( ).height/2;
			Rectangle rect = getClientArea( );
			Point center = getClientArea( ).getCenter( ).getCopy( );
			center.x = center.x - image.getBounds( ).width/2;
			center.y = rect.y;
			return center;
		}
		public boolean contains( Point pt )
		{
			Point p = getImagePoint( );
			Rectangle rect = new Rectangle(p.x, p.y, image.getBounds( ).width, image.getBounds( ).height);
			translateToAbsolute( rect );
			return rect.contains( pt );
		}
		
	}
	

	/**
	 * @param manager
	 */
	public void setManager( MenuManager manager )
	{
		this.manager = manager;
	}
	
	/**The point if in the triangle.
	 * @param pt
	 * @return
	 */
	public boolean contains( Point pt )
	{
//		FirstLevelHandleDataItemFigure figure = (FirstLevelHandleDataItemFigure) getFigure( );
//		Rectangle bounds = figure.getClientArea( );
//		Point center = figure.getCenterPoint( bounds );
//
//		figure.translateToAbsolute( center );
//		return ReportFigureUtilities.isInTriangle( center, FirstLevelHandleDataItemFigure.TRIANGLE_HEIGHT, pt );
//		return true;
		return controlFigure.contains( pt );
	}
}

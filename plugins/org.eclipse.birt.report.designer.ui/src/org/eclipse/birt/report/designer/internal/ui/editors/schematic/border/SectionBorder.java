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

package org.eclipse.birt.report.designer.internal.ui.editors.schematic.border;

import org.eclipse.birt.report.designer.util.ColorManager;
import org.eclipse.birt.report.model.util.ColorUtil;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Image;

/**
 * This class draws section border
 */
public class SectionBorder extends BaseBorder
{

	private static final Insets DEFAULT_CROP = new Insets( 0, 0, 1, 1 );

	private static final Insets DEFAULTINSETS = new Insets( 2, 2, 3, 3 );

	private Insets insets = new Insets( DEFAULTINSETS );
	private Dimension indicatorDimension = new Dimension( );
	protected String indicatorLabel = "";//$NON-NLS-1$
	protected Image image;
	protected int gap = 0;
	protected Insets gapInsets = new Insets( 2, 2, 2, 2 );
	private Rectangle indicatorArea;

	/*
	 * gets the insets (non-Javadoc)
	 * 
	 * @see org.eclipse.draw2d.Border#getInsets(org.eclipse.draw2d.IFigure)
	 */
	public Insets getInsets( IFigure figure )
	{
		return new Insets( insets );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.draw2d.Border#getInsets(org.eclipse.draw2d.IFigure)
	 */
	public void setInsets( Insets in )
	{
		if ( in != null
				&& in.left == 0
				&& in.right == 0
				&& in.top == 0
				&& in.bottom == 0 )
		{
			insets = new Insets( DEFAULTINSETS );
			return;
		}
		insets.top = in.top > 0 ? in.top : DEFAULTINSETS.top;

		insets.bottom = ( in.bottom > indicatorDimension.height && in.bottom > DEFAULTINSETS.bottom ) ? in.bottom
				: DEFAULTINSETS.bottom;

		insets.left = in.left > 0 ? in.left : DEFAULTINSETS.left;
		insets.right = in.right > 0 ? in.right : DEFAULTINSETS.right;
	}

	/*
	 * paint the border ----------------------------- | | | | | | |
	 * ------------------------- |___| (non-Javadoc)
	 * 
	 * @see org.eclipse.draw2d.Border#paint(org.eclipse.draw2d.IFigure,
	 *      org.eclipse.draw2d.Graphics, org.eclipse.draw2d.geometry.Insets)
	 */
	public void paint( IFigure figure, Graphics g, Insets in )
	{
		i_bottom_style = getBorderStyle( bottom_style );
		i_bottom_width = getBorderWidth( bottom_width );

		i_top_style = getBorderStyle( top_style );
		i_top_width = getBorderWidth( top_width );

		i_left_style = getBorderStyle( left_style );
		i_left_width = getBorderWidth( left_width );

		i_right_style = getBorderStyle( right_style );
		i_right_width = getBorderWidth( right_width );

		//draw top line
		drawBorder( figure, g, in, TOP, i_top_style, new int[]{
				i_top_width, i_bottom_width, i_left_width, i_right_width
		}, top_color );

		//draw bottom line
		drawBorder( figure, g, in, BOTTOM, i_bottom_style, new int[]{
				i_top_width, i_bottom_width, i_left_width, i_right_width
		}, bottom_color );

		//draw left line
		drawBorder( figure, g, in, LEFT, i_left_style, new int[]{
				i_top_width, i_bottom_width, i_left_width, i_right_width
		}, left_color );

		//draw right line
		drawBorder( figure, g, in, RIGHT, i_right_style, new int[]{
				i_top_width, i_bottom_width, i_left_width, i_right_width
		}, right_color );

	}

	/**
	 * Draw border of the section
	 * 
	 * @param figure
	 * @param g
	 * @param in
	 * @param side
	 * @param style
	 * @param width
	 * @param color
	 */
	private void drawBorder( IFigure figure, Graphics g, Insets in, int side,
			int style, int[] width, String color )
	{
		Rectangle r = figure.getBounds( )
				.getCropped( DEFAULT_CROP )
				.getCropped( in );

		//Outline the border
		//indicatorDimension = calculateIndicatorDimension( g, width[side] );

		//if the border style is not set to "none", draw line with given style,
		// width and color
		if ( style != 0 )
		{
			//set foreground color
			g.setForegroundColor( ColorManager.getColor( ColorUtil.parseColor( color ) ) );
			if ( style == -2 )
			{
				//drawDouble line
				drawDoubleLine( figure, g, side, width, r );
			}
			else
			{
				//draw single line
				drawSingleLine( figure, g, side, style, width, r );
			}
		}

		//if the border style is set to "none", draw a black solid line as
		// default
		else
		{
			g.setForegroundColor( ColorConstants.lightGray );
			//draw default line
			drawDefaultLine( figure, g, side, r );
		}

		g.restoreState( );
	}

//	/**
//	 * draw the left corner
//	 * 
//	 * @param g
//	 * @param rec
//	 * @param indicatorDimension
//	 */
//	private void drawIndicator( Graphics g, Rectangle rec,
//			Dimension indicatorDimension, int style, int width, int side,
//			boolean db )
//	{
//		Dimension cale = calculateIndicatorDimension( g, width );
//		int indicatorWidth = cale.width;
//		int indicatorHeight = cale.height;
//		indicatorArea = new Rectangle( rec.x,
//				rec.bottom( ) - indicatorHeight,
//				indicatorWidth,
//				indicatorHeight );
//
//		g.setLineStyle( style );
//
//		if ( side == BOTTOM )
//		{
//			if ( db == false )
//			{
//				for ( int i = 0; i < width; i++ )
//				{
//					g.drawLine( indicatorArea.x,
//							indicatorArea.bottom( ) - 1 - i,
//							indicatorArea.x + indicatorDimension.width,
//							indicatorArea.bottom( ) - 1 - i );
//					g.drawLine( indicatorArea.x + indicatorDimension.width + i,
//							indicatorArea.y,
//							indicatorArea.x + indicatorDimension.width + i,
//							indicatorArea.bottom( ) - 1 );
//				}
//			}
//			//if the border style is "double", draw the second line with 1
//			// pixel inside the Indicator
//			else
//			{
//				for ( int i = 0; i < width; i++ )
//				{
//					g.drawLine( indicatorArea.x + leftGap,
//							indicatorArea.bottom( ) - 1 - i - width - 1,
//							indicatorArea.x
//									+ indicatorDimension.width
//									- 1
//									- width,
//							indicatorArea.bottom( ) - 1 - i - width - 1 );
//					g.drawLine( indicatorArea.x
//							+ indicatorDimension.width
//							+ i
//							- width
//							- 1, indicatorArea.y - 1 - width, indicatorArea.x
//							+ indicatorDimension.width
//							+ i
//							- width
//							- 1, indicatorArea.bottom( ) - 1 - 1 - width );
//				}
//			}
//			//draw text "table"
//			int x = indicatorArea.x + gapInsets.left;
//			if ( image != null )
//			{
//				g.drawImage( image, x + 4, indicatorArea.y + gapInsets.top - 3 );
//				x += image.getBounds( ).width + gap;
//			}
//
//			g.drawString( indicatorLabel, x + 2 * width + 2, indicatorArea.y
//					+ gapInsets.top
//					- width );
//
//		}
//		else if ( side == LEFT )
//		{
//			if ( db == false )
//			{
//				for ( int j = 0; j < width; j++ )
//				{
//					g.drawLine( indicatorArea.x + j,
//							indicatorArea.y,
//							indicatorArea.x + j,
//							indicatorArea.bottom( ) - 1 );
//				}
//			}
//			else
//			{
//				for ( int j = 0; j < width; j++ )
//				{
//					g.drawLine( indicatorArea.x + j + width + 1,
//							indicatorArea.y,
//							indicatorArea.x + j + width + 1,
//							indicatorArea.bottom( ) - 1 - bottomGap );
//				}
//			}
//		}
//
//	}

	/**
	 * Sets the left corner label
	 * 
	 * @param indicatorLabel
	 */
	public void setIndicatorLabel( String indicatorLabel )
	{
		if ( indicatorLabel != null )
		{
			this.indicatorLabel = indicatorLabel;
		}
	}

	/**
	 * Sets the left corner
	 * 
	 * @param image
	 */
	public void setIndicatorIcon( Image image )
	{
		this.image = image;
	}

//	/**
//	 * calculates the left corner size
//	 * 
//	 * @return
//	 */
//	private Dimension calculateIndicatorDimension( Graphics g, int width )
//	{
//		return new Dimension( 0, 0 );
//
//		//		gap = 0;
//		//		Dimension iconDimension = new Dimension( );
//		//		if ( image != null )
//		//		{
//		//			iconDimension = new Dimension( image );
//		//			gap = 3;
//		//		}
//		//		Dimension d = FigureUtilities.getTextExtents( indicatorLabel,
//		//				g.getFont( ) );
//		//		int incheight = 0;
//		//		if ( iconDimension.height > d.height )
//		//		{
//		//			incheight = iconDimension.height - d.height;
//		//		}
//		//		d.expand( iconDimension.width
//		//				+ gap
//		//				+ gapInsets.left
//		//				+ gapInsets.right
//		//				+ 4
//		//				* width
//		//				+ 2, incheight + gapInsets.top + gapInsets.bottom );
//		//
//		//		return d;
//	}

	/**
	 * gets the left corner size
	 * 
	 * @return
	 */
	public Rectangle getIndicatorArea( )
	{

		return indicatorArea;
	}

}
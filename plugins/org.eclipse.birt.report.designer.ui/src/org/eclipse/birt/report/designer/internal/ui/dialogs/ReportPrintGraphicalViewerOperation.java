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

package org.eclipse.birt.report.designer.internal.ui.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.ScaledGraphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.editparts.LayerManager;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

/**
 * Paint the figure to the composite.
 */

public class ReportPrintGraphicalViewerOperation
{

	private Composite drawable;
	private GraphicalViewer viewer;
	private List selectedEditParts;
	private IFigure printSource;
	private Color oldBGColor;
	private GC printerGC;
	private SWTGraphics g;
	private CompositePrinterGraphics printerGraphics;

	public ReportPrintGraphicalViewerOperation( GraphicalViewer g,
			Composite able )
	{
		setDrawable( able );
		viewer = g;
		LayerManager lm = (LayerManager) viewer.getEditPartRegistry( )
				.get( LayerManager.ID );
		IFigure f = lm.getLayer( LayerConstants.PRINTABLE_LAYERS );
		setPrintSource( f );
	}

	/**
	 * Sets the printSource.
	 * 
	 * @param printSource
	 *            The printSource to set
	 */
	protected void setPrintSource( IFigure printSource )
	{
		this.printSource = printSource;
	}

	/**
	 * Gets the printSource.
	 * 
	 * @return
	 */
	protected IFigure getPrintSource( )
	{
		return printSource;
	}

	/**
	 * Gets the composite.
	 * 
	 * @return
	 */
	public Composite getDrawable( )
	{
		return drawable;
	}

	/**
	 * Sets the composite.
	 * 
	 * @param drawable
	 */
	public void setDrawable( Composite drawable )
	{
		this.drawable = drawable;
	}

	/**
	 * @param jobName
	 */
	public void run( String jobName )
	{
		// the job name is not use now.
		preparePrintSource( );
		printerGC = new GC( getDrawable( ), SWT.LEFT_TO_RIGHT );
		printPages( );
		restorePrintSource( );
		cleanup( );
	}

	/**
	 * Disposes the PrinterGraphics and GC objects associated with this
	 * PrintOperation.
	 */
	protected void cleanup( )
	{
		if ( g != null )
		{
			printerGraphics.dispose( );
			g.dispose( );
		}
		if ( printerGC != null )
			printerGC.dispose( );
	}

	/**
	 * @see org.eclipse.draw2d.PrintOperation#preparePrintSource()
	 */
	protected void preparePrintSource( )
	{
		oldBGColor = getPrintSource( ).getLocalBackgroundColor( );
		getPrintSource( ).setBackgroundColor( ColorConstants.white );
		selectedEditParts = new ArrayList( viewer.getSelectedEditParts( ) );
		viewer.deselectAll( );
	}

	/**
	 * @see org.eclipse.draw2d.PrintOperation#restorePrintSource()
	 */
	protected void restorePrintSource( )
	{
		getPrintSource( ).setBackgroundColor( oldBGColor );
		oldBGColor = null;
		viewer.setSelection( new StructuredSelection( selectedEditParts ) );
	}

	/**
	 * Prints the pages based on the current print mode.
	 * 
	 * @see org.eclipse.draw2d.PrintOperation#printPages()
	 */
	protected void printPages( )
	{
		Graphics graphics = getFreshGraphics( );
		IFigure figure = getPrintSource( );
		setupPrinterGraphicsFor( graphics, figure );
		Rectangle bounds = figure.getBounds( );
		int x = bounds.x, y = bounds.y;
		Rectangle clipRect = new Rectangle( );
		while ( y < bounds.y + bounds.height )
		{
			while ( x < bounds.x + bounds.width )
			{
				graphics.pushState( );
				graphics.translate( -x, -y );
				graphics.getClip( clipRect );
				clipRect.setLocation( x, y );
				graphics.clipRect( clipRect );
				figure.paint( graphics );
				graphics.popState( );
				x += clipRect.width;
				if ( x == 0 )
				{
					return;
				}
			}
			x = bounds.x;
			y += clipRect.height;
		}
	}

	/**
	 * Returns a new PrinterGraphics setup for the Printer associated with this
	 * PrintOperation.
	 * 
	 * @return PrinterGraphics The new PrinterGraphics
	 */
	protected Graphics getFreshGraphics( )
	{
		if ( printerGraphics != null )
		{
			printerGraphics.dispose( );
			g.dispose( );
			printerGraphics = null;
			g = null;
		}
		g = new SWTGraphics( printerGC );

		printerGraphics = new CompositePrinterGraphics( g,
				getDrawable( ).getDisplay( ) );
		setupGraphicsForPage( printerGraphics );
		return printerGraphics;
	}

	/**
	 * Sets up Graphics object for the given IFigure.
	 * 
	 * @param graphics
	 *            The Graphics to setup
	 * @param figure
	 *            The IFigure used to setup graphics
	 */
	protected void setupPrinterGraphicsFor( Graphics graphics, IFigure figure )
	{
		// Because the ScaleGraphics don't support the scale(float h,float v),so
		// now suppoer fit the page.
		Rectangle printRegion = getPrintRegion( );

		Rectangle bounds = figure.getBounds( );
		double xScale = (double) printRegion.width / bounds.width;
		double yScale = (double) printRegion.height / bounds.height;
		graphics.scale( Math.min( xScale, yScale ) );

		// float xScale = (float) printRegion.width / bounds.width;
		// float yScale = (float) printRegion.height / bounds.height;
		// graphics.scale( xScale, yScale );

		graphics.setForegroundColor( figure.getForegroundColor( ) );
		graphics.setBackgroundColor( figure.getBackgroundColor( ) );
		graphics.setFont( figure.getFont( ) );
	}

	/**
	 * Manipulates the PrinterGraphics to position it to paint in the desired
	 * region of the page. (Default is the top left corner of the page).
	 * 
	 * @param pg
	 *            The PrinterGraphics to setup
	 */
	protected void setupGraphicsForPage( CompositePrinterGraphics pg )
	{
		Rectangle printRegion = getPrintRegion( );
		pg.setClip( printRegion );
		pg.translate( printRegion.getTopLeft( ) );
	}

	/**
	 * Returns a Rectangle that represents the region that can be printed to.
	 * The x, y, height, and width values are using the printers coordinates.
	 * 
	 * @return the print region
	 */
	public Rectangle getPrintRegion( )
	{
		return new Rectangle( getDrawable( ).getBounds( ) );
	}

	public static class CompositePrinterGraphics extends ScaledGraphics
	{

		Map imageCache = new HashMap( );

		Device printer;

		/**
		 * Creates a new PrinterGraphics with Graphics g, using Printer p ;
		 * 
		 * @param g
		 *            Graphics object to draw with
		 * @param p
		 *            Printer to print to
		 */
		public CompositePrinterGraphics( SWTGraphics g, Device p )
		{
			super( g );
			printer = p;
		}

		private Image printerImage( Image image )
		{
			Image result = (Image) imageCache.get( image );
			if ( result != null )
				return result;

			result = new Image( printer, image.getImageData( ) );
			imageCache.put( image, result );
			return result;
		}

		/**
		 * @see org.eclipse.draw2d.Graphics#drawImage(Image, int, int)
		 */
		public void drawImage( Image srcImage, int x, int y )
		{
			super.drawImage( printerImage( srcImage ), x, y );
		}

		/**
		 * @see Graphics#drawImage(Image, int, int, int, int, int, int, int,
		 *      int)
		 */
		public void drawImage( Image srcImage, int sx, int sy, int sw, int sh,
				int tx, int ty, int tw, int th )
		{
			super.drawImage( printerImage( srcImage ),
					sx,
					sy,
					sw,
					sh,
					tx,
					ty,
					tw,
					th );
		}

	}
}

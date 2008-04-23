/*******************************************************************************
 * Copyright (c) 2007 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.report.designer.internal.ui.editors.schematic.handles;

import org.eclipse.birt.report.designer.internal.ui.editors.ReportColorConstants;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Locator;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.handles.ResizeHandle;
import org.eclipse.gef.handles.SquareHandle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;

/**
 * A Handle used to resize a GraphicalEditPart.
 */
public class ReportResizeHandle extends ResizeHandle
{

	/**
	 * Creates a new ResizeHandle for the given GraphicalEditPart.
	 * <code>direction</code> is the relative direction from the center of the
	 * owner figure. For example, <code>SOUTH_EAST</code> would place the
	 * handle in the lower-right corner of its owner figure. These direction
	 * constants can be found in {@link org.eclipse.draw2d.PositionConstants}.
	 * 
	 * @param owner
	 *            owner of the ResizeHandle
	 * @param direction
	 *            relative direction from the center of the owner figure
	 */
	public ReportResizeHandle( GraphicalEditPart owner, int direction )
	{
		super( owner, direction );
	}

	/**
	 * Creates a new ResizeHandle for the given GraphicalEditPart.
	 * 
	 * @see SquareHandle#SquareHandle(GraphicalEditPart, Locator, Cursor)
	 */
	public ReportResizeHandle( GraphicalEditPart owner, Locator loc, Cursor c )
	{
		super( owner, loc, c );
	}

	@Override
	protected Color getBorderColor( )
	{
		return ( isPrimary( ) ) ? ColorConstants.white
				: ReportColorConstants.SelctionFillColor;
	}

	@Override
	protected Color getFillColor( )
	{
		return ( isPrimary( ) ) ? ReportColorConstants.SelctionFillColor
				: ColorConstants.white;
	}
}

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

package org.eclipse.birt.report.designer.core.model.schematic;

import org.eclipse.birt.report.designer.core.model.IModelAdapterHelper;
import org.eclipse.birt.report.designer.core.model.ReportItemtHandleAdapter;
import org.eclipse.birt.report.designer.util.DEUtil;
import org.eclipse.birt.report.model.api.DimensionHandle;
import org.eclipse.birt.report.model.api.ReportItemHandle;
import org.eclipse.draw2d.geometry.Dimension;

/**
 * Adapter class to adapt model handle. This adapter provides convenience
 * methods to GUI requirement LabelHandleAdapter responds to model LabelHandle
 */
public class LabelHandleAdapter extends ReportItemtHandleAdapter
{

	/**
	 * Constructor
	 * 
	 * @param labelHandle
	 *            The label handle.
	 * @param mark
	 */
	public LabelHandleAdapter( ReportItemHandle labelHandle,
			IModelAdapterHelper mark )
	{
		super( labelHandle, mark );
	}

	/**
	 * Gets size of label item.
	 * 
	 * @return the size of label item.
	 */
	public Dimension getSize( )
	{
		DimensionHandle handle = ( (ReportItemHandle) getHandle( ) ).getWidth( );
		int px = (int) DEUtil.convertoToPixel( handle );

		handle = ( (ReportItemHandle) getHandle( ) ).getHeight( );
		int py = (int) DEUtil.convertoToPixel( handle );

		px = Math.max( 0, px );
		py = Math.max( 0, py );

		return new Dimension( px, py );
	}
}
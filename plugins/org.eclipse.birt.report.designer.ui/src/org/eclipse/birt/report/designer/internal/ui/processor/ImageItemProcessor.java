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

package org.eclipse.birt.report.designer.internal.ui.processor;

import org.eclipse.birt.report.designer.internal.ui.dialogs.ImageBuilderDialog;
import org.eclipse.birt.report.designer.internal.ui.util.UIUtil;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.elements.ReportDesignConstants;
import org.eclipse.jface.dialogs.Dialog;

/**
 * The processor for image items
 */

public class ImageItemProcessor extends AbstractElementProcessor
{

	/**
	 * Constructor
	 * 
	 * Creates a new instance of the processor for image items
	 */
	ImageItemProcessor( )
	{
		super( ReportDesignConstants.IMAGE_ITEM );
	}

	public DesignElementHandle createElement( Object extendedData )
	{
		ImageBuilderDialog dialog = new ImageBuilderDialog( UIUtil.getDefaultShell( ) );
		if ( dialog.open( ) == Dialog.OK )
		{
			return (DesignElementHandle) dialog.getResult( );
		}
		return null;
	}

	public boolean editElement( DesignElementHandle handle )
	{
		ImageBuilderDialog dialog = new ImageBuilderDialog( UIUtil.getDefaultShell( ) );
		dialog.setInput( handle );
		return ( dialog.open( ) == Dialog.OK );
	}
}

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

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * Format number page for formatting numbers.
 */
public class FormatNumberPage extends FormatPage
{

	/**
	 * Constructs a page for formatting numbers, default aligns the page
	 * virtically.
	 * 
	 * @param parent
	 *            The container
	 * @param style
	 *            The style of the page
	 */
	public FormatNumberPage( Composite parent, int style )
	{
		this( parent, style, PAGE_ALIGN_VIRTICAL, true );
	}

	/**
	 * Constructs a page for formatting numbers.
	 * 
	 * @param parent
	 *            The container
	 * @param style
	 *            The style of the page
	 * @param pageAlignment
	 *            Aligns the page virtically(PAGE_ALIGN_VIRTICAL) or
	 *            horizontally(PAGE_ALIGN_HORIZONTAL).
	 */
	public FormatNumberPage( Composite parent, int style, int pageAlignment,
			boolean showLocale )
	{
		super( parent, style );

		this.setLayout( new FillLayout( ) );

		layoutPeer = new FormatNumberLayoutPeer( pageAlignment,
				false,
				showLocale );

		layoutPeer.createLayout( this );
	}

}
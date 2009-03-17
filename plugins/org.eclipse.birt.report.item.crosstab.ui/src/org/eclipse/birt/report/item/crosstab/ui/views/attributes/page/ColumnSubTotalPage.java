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

package org.eclipse.birt.report.item.crosstab.ui.views.attributes.page;

import org.eclipse.birt.report.designer.internal.ui.views.attributes.page.AttributePage;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.page.WidgetUtil;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.section.FormSection;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.widget.FormPropertyDescriptor;
import org.eclipse.birt.report.item.crosstab.core.ICrosstabConstants;
import org.eclipse.birt.report.item.crosstab.ui.views.attributes.provider.SubTotalProvider;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.activity.NotificationEvent;
import org.eclipse.swt.widgets.Composite;

/**
 * 
 */

public class ColumnSubTotalPage extends AttributePage
{
	private FormSection subTotalSection;

	public void buildUI( Composite parent )
	{
		super.buildUI( parent );
		container.setLayout( WidgetUtil.createGridLayout( 1 ) );
		final SubTotalProvider subTotalProvider = new SubTotalProvider( );
		subTotalProvider.setAxis( ICrosstabConstants.COLUMN_AXIS_TYPE );
		subTotalSection = new FormSection( subTotalProvider.getDisplayName( ),
				container,
				true );
		subTotalSection.setProvider( subTotalProvider );
		subTotalSection.setButtonWithDialog( true );
		subTotalSection.setStyle( FormPropertyDescriptor.NO_UP_DOWN );
		subTotalSection.setFillForm( true );
		subTotalSection.setHeight( 170 );
		addSection( CrosstabPageSectionId.COLUMN_SUB_TOTALS, subTotalSection );
		createSections( );
		layoutSections( );
	}

	public void addElementEvent( DesignElementHandle focus, NotificationEvent ev )
	{
		if ( checkControl( subTotalSection ) )
			subTotalSection.getFormControl( ).addElementEvent( focus, ev );
	}

	public void clear( )
	{
		if ( checkControl( subTotalSection ) )
			subTotalSection.getFormControl( ).clear( );
	}

	public void postElementEvent( )
	{

		if ( checkControl( subTotalSection ) )
			subTotalSection.getFormControl( ).postElementEvent( );

	}
	
	private boolean checkControl( FormSection form )
	{
		return form != null
				&& form.getFormControl( ) != null
				&& !form.getFormControl( ).getControl( ).isDisposed( );
	}
}
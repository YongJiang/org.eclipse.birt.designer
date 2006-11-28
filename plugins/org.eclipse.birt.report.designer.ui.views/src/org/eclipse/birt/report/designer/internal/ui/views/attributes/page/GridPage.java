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

package org.eclipse.birt.report.designer.internal.ui.views.attributes.page;

import org.eclipse.birt.report.designer.internal.ui.views.attributes.provider.ColorPropertyDescriptorProvider;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.provider.ComboPropertyDescriptorProvider;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.provider.IDescriptorProvider;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.provider.PropertyDescriptorProvider;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.provider.SimpleComboPropertyDescriptorProvider;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.provider.TextPropertyDescriptorProvider;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.provider.UnitPropertyDescriptorProvider;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.section.CheckSection;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.section.ColorSection;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.section.ComboSection;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.section.Section;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.section.SeperatorSection;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.section.SimpleComboSection;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.section.TextSection;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.section.UnitSection;
import org.eclipse.birt.report.model.api.ReportItemHandle;
import org.eclipse.birt.report.model.api.StyleHandle;
import org.eclipse.birt.report.model.api.elements.ReportDesignConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * The general attribute page of Grid element.
 */
public class GridPage extends GeneralPage
{

	

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.views.attributes.page.GeneralPage#buildContent()
	 */
	void buildContent( )
	{
		// Defines providers.

		IDescriptorProvider nameProvider = new TextPropertyDescriptorProvider( ReportItemHandle.NAME_PROP,
				ReportDesignConstants.GRID_ITEM );

		IDescriptorProvider widthProvider = new UnitPropertyDescriptorProvider( ReportItemHandle.WIDTH_PROP,
				ReportDesignConstants.GRID_ITEM );

		IDescriptorProvider heightProvider = new UnitPropertyDescriptorProvider( ReportItemHandle.HEIGHT_PROP,
				ReportDesignConstants.GRID_ITEM );

		IDescriptorProvider canShrinkProvider = new PropertyDescriptorProvider( StyleHandle.CAN_SHRINK_PROP,
				ReportDesignConstants.GRID_ITEM );

		IDescriptorProvider hAlignProvider = new ComboPropertyDescriptorProvider( StyleHandle.TEXT_ALIGN_PROP,
				ReportDesignConstants.STYLE_ELEMENT );

		IDescriptorProvider vAlignProvider = new ComboPropertyDescriptorProvider( StyleHandle.VERTICAL_ALIGN_PROP,
				ReportDesignConstants.STYLE_ELEMENT );

		IDescriptorProvider styleProvider = new SimpleComboPropertyDescriptorProvider( ReportItemHandle.STYLE_PROP,
				ReportDesignConstants.REPORT_ITEM );

		IDescriptorProvider backgroundProvider = new ColorPropertyDescriptorProvider( StyleHandle.BACKGROUND_COLOR_PROP,
				ReportDesignConstants.STYLE_ELEMENT );

		// Defines sections.

		TextSection nameSection = new TextSection( nameProvider.getDisplayName( ),
				container,
				true );

		Section seperatorSection = new SeperatorSection( container, SWT.HORIZONTAL );

		UnitSection widthSection = new UnitSection( widthProvider.getDisplayName( ),
				container,
				true );

		UnitSection heightSection = new UnitSection( heightProvider.getDisplayName( ),
				container,
				true );

		CheckSection canShrinkSection = new CheckSection( container, true );

		ComboSection hAlignSection = new ComboSection( hAlignProvider.getDisplayName( ),
				container,
				true );

		ComboSection vAlignSection = new ComboSection( vAlignProvider.getDisplayName( ),
				container,
				true );

		SimpleComboSection styleSection = new SimpleComboSection( styleProvider.getDisplayName( ),
				container,
				true );

		ColorSection backgroundSection = new ColorSection( backgroundProvider.getDisplayName( ),
				container,
				true );

		// Sets providers.

		nameSection.setProvider( nameProvider );
		widthSection.setProvider( widthProvider );
		heightSection.setProvider( heightProvider );
		canShrinkSection.setProvider( canShrinkProvider );
		hAlignSection.setProvider( hAlignProvider );
		vAlignSection.setProvider( vAlignProvider );
		styleSection.setProvider( styleProvider );
		backgroundSection.setProvider( backgroundProvider );

		// Sets widths.

		nameSection.setWidth( 180 );
		widthSection.setWidth( 180 );
		heightSection.setWidth( 180 );
		hAlignSection.setWidth( 180 );
		vAlignSection.setWidth( 180 );
		styleSection.setWidth( 180 );
		backgroundSection.setWidth( 180 );

		// Sets layout num.

		nameSection.setLayoutNum( 6 );
		widthSection.setLayoutNum( 2 );
		heightSection.setLayoutNum( 2 );
		canShrinkSection.setLayoutNum( 2 );
		hAlignSection.setLayoutNum( 2 );
		vAlignSection.setLayoutNum( 4 );
		styleSection.setLayoutNum( 4 );
		backgroundSection.setLayoutNum( 2 );

		// Sets fill grid num.

		nameSection.setGridPlaceholder( 4, true );
		widthSection.setGridPlaceholder( 0, true );
		heightSection.setGridPlaceholder( 0, true );
		canShrinkSection.setGridPlaceholder( 1, true );
		hAlignSection.setGridPlaceholder( 0, true );
		vAlignSection.setGridPlaceholder( 2, true );
		styleSection.setGridPlaceholder( 2, true );
		backgroundSection.setGridPlaceholder( 0, true );


		addSection( "GRID_NAME_SECTION", nameSection ); //$NON-NLS-1$
		addSection( "SEPERATOR_SECTION", seperatorSection ); //$NON-NLS-1$
		addSection( "GRID_WIDTH_SECTION", widthSection ); //$NON-NLS-1$
		addSection( "GRID_HORIZONTAL_ALIGN_SECTION", hAlignSection ); //$NON-NLS-1$
		addSection( "GRID_CAN_SHRINK_SECTION", canShrinkSection ); //$NON-NLS-1$
		addSection( "GRID_HEIGHT_SECTION", heightSection ); //$NON-NLS-1$
		addSection( "GRID_VERTICAL_ALIGN_SECTION", vAlignSection ); //$NON-NLS-1$
		addSection( "GRID_BACKGROUND_COLOR_SECTION", backgroundSection ); //$NON-NLS-1$
		addSection( "GRID_STYLE_SECTION", styleSection ); //$NON-NLS-1$
		
		createSections( );
		layoutSections( );
	}
}

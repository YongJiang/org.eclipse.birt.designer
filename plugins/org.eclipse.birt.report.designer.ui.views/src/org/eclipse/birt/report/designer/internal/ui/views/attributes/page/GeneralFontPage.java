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
import org.eclipse.birt.report.designer.internal.ui.views.attributes.provider.FontSizePropertyDescriptorProvider;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.provider.FontStylePropertyDescriptorProvider;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.section.ColorSection;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.section.ComboSection;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.section.FontSizeSection;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.section.TogglesSection;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.model.api.StyleHandle;
import org.eclipse.birt.report.model.api.elements.ReportDesignConstants;

public abstract class GeneralFontPage extends AttributePage
{

	protected void addFontsSection( )
	{
		ComboPropertyDescriptorProvider fontFamilyProvider = new ComboPropertyDescriptorProvider( StyleHandle.FONT_FAMILY_PROP,
				ReportDesignConstants.STYLE_ELEMENT );
		ComboSection fontFamilySection = new ComboSection( fontFamilyProvider.getDisplayName( ),
				container,
				true );
		fontFamilySection.setProvider( fontFamilyProvider );
		fontFamilySection.setLayoutNum( 2 );
		fontFamilySection.setWidth( 200 );
		addSection( PageSectionId.FONT_FAMILY, fontFamilySection );

		FontSizePropertyDescriptorProvider fontSizeProvider = new FontSizePropertyDescriptorProvider( StyleHandle.FONT_SIZE_PROP,
				ReportDesignConstants.STYLE_ELEMENT );
		FontSizeSection fontSizeSection = new FontSizeSection( fontSizeProvider.getDisplayName( ),
				container,
				true );
		fontSizeSection.setProvider( fontSizeProvider );
		fontSizeSection.setLayoutNum( 4 );
		fontSizeSection.setWidth( 200 );
		fontSizeSection.setGridPlaceholder( 2, true );
		addSection( PageSectionId.FONT_SIZE, fontSizeSection );

		ColorPropertyDescriptorProvider colorProvider = new ColorPropertyDescriptorProvider( StyleHandle.COLOR_PROP,
				ReportDesignConstants.STYLE_ELEMENT );
		ColorSection colorSection = new ColorSection( colorProvider.getDisplayName( ),
				container,
				true );
		colorSection.setProvider( colorProvider );
		colorSection.setLayoutNum( 2 );
		colorSection.setWidth( 200 );
		addSection( PageSectionId.FONT_COLOR, colorSection );

		String[] textStyles = new String[]{
				StyleHandle.FONT_WEIGHT_PROP,
				StyleHandle.FONT_STYLE_PROP,
				StyleHandle.TEXT_UNDERLINE_PROP,
				StyleHandle.TEXT_LINE_THROUGH_PROP,
		};

		FontStylePropertyDescriptorProvider[] providers = new FontStylePropertyDescriptorProvider[4];
		for ( int i = 0; i < textStyles.length; i++ )
		{
			providers[i] = new FontStylePropertyDescriptorProvider( textStyles[i],
					ReportDesignConstants.STYLE_ELEMENT );
		}
		TogglesSection fontStyleSection = new TogglesSection( container );
		fontStyleSection.setLabelText( Messages.getString( "GeneralFontPage.Label.FontStyle" ) );
		fontStyleSection.setProviders( providers );
		fontStyleSection.setLayoutNum( 4 );
		fontStyleSection.setGridPlaceholder( 2, true );
		addSection( PageSectionId.FONT_STYLE, fontStyleSection );
	}

}

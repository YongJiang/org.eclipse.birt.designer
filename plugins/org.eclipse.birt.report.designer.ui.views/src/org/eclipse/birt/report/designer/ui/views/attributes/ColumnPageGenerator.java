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

package org.eclipse.birt.report.designer.ui.views.attributes;

import java.util.List;

import org.eclipse.birt.report.designer.internal.ui.views.attributes.page.PreviewPage;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.provider.HighlightDescriptorProvider;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.provider.MapDescriptorProvider;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.widget.HighlightPropertyDescriptor;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.widget.MapPropertyDescriptor;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;

/**
 * Instances of ColumnPageGenerator take change of creating attribute page
 * correspond to TableColumn element.
 */
public class ColumnPageGenerator extends AbstractPageGenerator
{

	protected PreviewPage highlightsPage;

	protected PreviewPage mapPage;

	protected void buildItemContent( CTabItem item )
	{
		if ( itemMap.containsKey( item ) && itemMap.get( item ) == null )
		{
			String title = tabFolder.getSelection( ).getText( );
			if ( title.equals( MAPTITLE ) )
			{
				mapPage = new PreviewPage( true );
				mapPage.setPreview( new MapPropertyDescriptor( true ) );
				mapPage.setProvider( new MapDescriptorProvider( ) );
				setPageInput( mapPage );
				refresh( tabFolder, mapPage, true );
				item.setControl( mapPage.getControl( ) );
				itemMap.put( item, mapPage );

			}
			else if ( title.equals( HIGHLIGHTSTITLE ) )
			{
				highlightsPage = new PreviewPage( true );
				highlightsPage.setPreview( new HighlightPropertyDescriptor( true ) );
				highlightsPage.setProvider( new HighlightDescriptorProvider( ) );
				setPageInput( highlightsPage );
				refresh( tabFolder, highlightsPage, true );
				item.setControl( highlightsPage.getControl( ) );
				itemMap.put( item, highlightsPage );
			}
		}
		else if ( itemMap.get( item ) != null )
		{
			setPageInput( itemMap.get( item ) );
			refresh( tabFolder, itemMap.get( item ), false );
		}
	}

	public void createTabItems( List input )
	{
		super.createTabItems( input );
		this.input = input;
		addSelectionListener( this );
		createTabItems( );
		if ( tabFolder.getSelection( ) != null )
			buildItemContent( tabFolder.getSelection( ) );
	}

	protected void createTabItems( )
	{
		createTabItem( MAPTITLE, ATTRIBUTESTITLE );
		createTabItem( HIGHLIGHTSTITLE, MAPTITLE );
	}

	public void createControl( Composite parent, Object input )
	{
		super.createControl( parent, input );
	}
}
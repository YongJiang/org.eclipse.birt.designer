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

import org.eclipse.birt.report.designer.internal.ui.views.attributes.provider.ReferenceDescriptorProvider;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.section.TextAndButtonSection;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;

/**
 * The reference attribute page of Image element.
 */
public class ReferencePage extends AttributePage
{

	private TextAndButtonSection referenceSection;
	private ReferenceDescriptorProvider referenceProvider;

	public void buildUI( Composite parent )
	{
		super.buildUI( parent );
		container.setLayout( WidgetUtil.createGridLayout( 3, 15 ) );

		referenceProvider = new ReferenceDescriptorProvider( );
		referenceSection = new TextAndButtonSection( referenceProvider.getDisplayName( ),
				container,
				true ) {

			public void load( )
			{
				super.load( );
				if ( referenceSection != null
						&& referenceSection.getButtonControl( ) != null )
					referenceSection.getButtonControl( )
							.setEnabled( referenceProvider.isEnableButton( ) );
			}
		};
		referenceSection.setProvider( referenceProvider );
		referenceSection.addSelectionListener( new SelectionAdapter( ) {

			public void widgetSelected( SelectionEvent e )
			{
				referenceProvider.handleSelectEvent( );
			}

		} );
		referenceSection.setWidth( 300 );
		referenceSection.setButtonText( Messages.getString( "ReferencePage.Button.Edit" ) );
		referenceSection.setButtonIsComputeSize( true );
		addSection( PageSectionId.REFERENCE_REFERENCE, referenceSection );

		createSections( );
		layoutSections( );
	}
}
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

package org.eclipse.birt.report.designer.ui;

import org.eclipse.birt.report.designer.ui.views.attributes.AttributeView;
import org.eclipse.birt.report.designer.ui.views.data.DataView;
import org.eclipse.gef.ui.views.palette.PaletteView;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * JRPPerspective generates the initial page layout and visible action set for
 * birt.
 * 
 *  
 */
public class ReportPerspective implements IPerspectiveFactory
{

	public static final String BIRT_REPORT_PERSPECTIVE = "org.eclipse.birt.report.designer.ui.ReportPerspective"; //$NON-NLS-1$

	public static final String NEW_REPORT_ID = "org.eclipse.birt.report.designer.ui.ide.wizards.NewReportWizard";//$NON-NLS-1$
	
	public static final String NEW_TEMPLATE_ID = "org.eclipse.birt.report.designer.ui.ide.wizards.NewTemplateWizard";//$NON-NLS-1$

	/**
	 * Constructs a new Default layout engine.
	 */

	public ReportPerspective( )
	{
		super( );
	}

	/**
	 * Defines the initial layout for a perspective.
	 * 
	 * Implementors of this method may add additional views to a perspective.
	 * The perspective already contains an editor folder with
	 * <code>ID = ILayoutFactory.ID_EDITORS</code>. Add additional views to
	 * the perspective in reference to the editor folder.
	 * 
	 * This method is only called when a new perspective is created. If an old
	 * perspective is restored from a persistence file then this method is not
	 * called.
	 * 
	 * @param layout
	 *            the factory used to add views to the perspective
	 */
	public void createInitialLayout( IPageLayout layout )
	{
		defineLayout( layout );
		defineActions( layout );
	}

	/**
	 * Defines the Actions
	 */
	private void defineActions( IPageLayout layout )
	{
		// Add "new wizards".
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");//$NON-NLS-1$
		layout.addNewWizardShortcut( NEW_REPORT_ID );
		layout.addNewWizardShortcut( NEW_TEMPLATE_ID );
		
		// Add "show views".
		layout.addShowViewShortcut(IPageLayout.ID_RES_NAV);
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		layout.addShowViewShortcut(PaletteView.ID);
		layout.addShowViewShortcut(AttributeView.ID);
		layout.addShowViewShortcut(DataView.ID);
		layout.addShowViewShortcut(IPageLayout.ID_PROP_SHEET);
		layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
		
		layout.addPerspectiveShortcut(BIRT_REPORT_PERSPECTIVE);
		

		
	}

	/**
	 * Defines the initial layout for a page.
	 */
	private void defineLayout( IPageLayout layout )
	{
		// Editors are placed for free.
		String editorArea = layout.getEditorArea( );

		// Top left.
		IFolderLayout topLeft = layout.createFolder( "topLeft", IPageLayout.LEFT, (float) 0.26, editorArea );//$NON-NLS-1$
		topLeft.addView( PaletteView.ID );
		topLeft.addView( DataView.ID );

		// Bottom left.
		IFolderLayout bottomLeft = layout.createFolder( "bottomLeft", IPageLayout.BOTTOM, (float) 0.50,//$NON-NLS-1$
				"topLeft" );//$NON-NLS-1$
		bottomLeft.addView( IPageLayout.ID_RES_NAV );
		bottomLeft.addView( IPageLayout.ID_OUTLINE );

		// Bottom right.
		IFolderLayout bootomRight = layout.createFolder( "bootomRight", IPageLayout.BOTTOM, (float) 0.66, editorArea );//$NON-NLS-1$
		bootomRight.addView( AttributeView.ID );
		bootomRight.addView( IPageLayout.ID_PROBLEM_VIEW );

	}
}
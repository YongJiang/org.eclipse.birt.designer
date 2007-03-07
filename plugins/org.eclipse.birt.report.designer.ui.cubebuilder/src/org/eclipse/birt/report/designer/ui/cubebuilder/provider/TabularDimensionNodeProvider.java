/*************************************************************************************
 * Copyright (c) 2004 Actuate Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Actuate Corporation - Initial implementation.
 ************************************************************************************/

package org.eclipse.birt.report.designer.ui.cubebuilder.provider;

import org.eclipse.birt.report.designer.internal.ui.views.DefaultNodeProvider;
import org.eclipse.birt.report.designer.internal.ui.views.actions.RefreshAction;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.designer.ui.IReportGraphicConstants;
import org.eclipse.birt.report.designer.ui.actions.ShowPropertyAction;
import org.eclipse.birt.report.designer.ui.cubebuilder.action.EditCubeDimensionAction;
import org.eclipse.birt.report.designer.ui.cubebuilder.dialog.DimensionDialog;
import org.eclipse.birt.report.designer.ui.cubebuilder.util.BuilderConstancts;
import org.eclipse.birt.report.designer.ui.cubebuilder.util.UIHelper;
import org.eclipse.birt.report.designer.util.DEUtil;
import org.eclipse.birt.report.model.api.ReportElementHandle;
import org.eclipse.birt.report.model.api.olap.DimensionHandle;
import org.eclipse.birt.report.model.api.olap.HierarchyHandle;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchActionConstants;

/**
 * Deals with dataset node
 * 
 */
public class TabularDimensionNodeProvider extends DefaultNodeProvider
{

	/**
	 * Creates the context menu for the given object. Gets the action from the
	 * actionRegistry and adds the action to the menu.
	 * 
	 * @param menu
	 *            the menu
	 * @param object
	 *            the object
	 */
	public void createContextMenu( TreeViewer sourceViewer, Object object,
			IMenuManager menu )
	{
		super.createContextMenu( sourceViewer, object, menu );

		if ( ( (DimensionHandle) object ).canEdit( ) )
		{
			menu.insertAfter( IWorkbenchActionConstants.MB_ADDITIONS,
					new EditCubeDimensionAction( object,
							Messages.getString( "CubeDimensionNodeProvider.menu.text" ) ) ); //$NON-NLS-1$
		}

		menu.insertBefore( IWorkbenchActionConstants.MB_ADDITIONS + "-refresh", //$NON-NLS-1$
				new ShowPropertyAction( object ) );

		menu.insertAfter( IWorkbenchActionConstants.MB_ADDITIONS + "-refresh", new Separator( ) ); //$NON-NLS-1$
		menu.insertAfter( IWorkbenchActionConstants.MB_ADDITIONS + "-refresh", new RefreshAction( sourceViewer ) ); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.views.INodeProvider#getNodeDisplayName(java.lang.Object)
	 */
	public String getNodeDisplayName( Object model )
	{
		return DEUtil.getDisplayLabel( model, false ) + "(Dimension)";
	}

	/**
	 * Gets the children element of the given model using visitor.
	 * 
	 * @param object
	 *            the handle
	 */
	public Object[] getChildren( Object object )
	{
		HierarchyHandle hierarchy = (HierarchyHandle) ( (DimensionHandle) object ).getContent( DimensionHandle.HIERARCHIES_PROP,
				0 );
		if ( hierarchy.getLevelCount( ) > 0 )
			return new Object[]{
				hierarchy.getLevel( 0 )
			};
		return new Object[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.views.DefaultNodeProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren( Object object )
	{
		HierarchyHandle hierarchy = (HierarchyHandle) ( (DimensionHandle) object ).getContent( DimensionHandle.HIERARCHIES_PROP,
				0 );
		return hierarchy != null && hierarchy.getLevelCount( ) > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.views.INodeProvider#getNodeDisplayName(java.lang.Object)
	 */
	protected boolean performEdit( ReportElementHandle handle )
	{
		DimensionHandle dimensionHandle = (DimensionHandle) handle;
		DimensionDialog dialog = new DimensionDialog( false );
		dialog.setInput( dimensionHandle );

		return dialog.open( ) == Dialog.OK;
	}

	public Image getNodeIcon( Object model )
	{
		return UIHelper.getImage( BuilderConstancts.IMAGE_DIMENSION );
	}

}

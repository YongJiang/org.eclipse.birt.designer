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

package org.eclipse.birt.report.designer.ui.cubebuilder.provider;

import org.eclipse.birt.report.designer.internal.ui.views.DefaultNodeProvider;
import org.eclipse.birt.report.designer.internal.ui.views.actions.RefreshAction;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.designer.ui.actions.ShowPropertyAction;
import org.eclipse.birt.report.designer.ui.cubebuilder.action.EditCubeAction;
import org.eclipse.birt.report.designer.ui.cubebuilder.util.BuilderConstancts;
import org.eclipse.birt.report.designer.ui.cubebuilder.util.UIHelper;
import org.eclipse.birt.report.model.api.PropertyHandle;
import org.eclipse.birt.report.model.api.olap.CubeHandle;
import org.eclipse.birt.report.model.elements.interfaces.ICubeModel;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchActionConstants;

public class CubeSubFolderNodeProvider extends DefaultNodeProvider
{

	public Image getNodeIcon( Object model )
	{
		PropertyHandle property = (PropertyHandle) model;
		String name = property.getPropertyDefn( ).getName( );
		if ( name.equals( ICubeModel.DIMENSIONS_PROP ) )
		{
			return UIHelper.getImage( BuilderConstancts.IMAGE_DIMENSION_FOLDER );
		}
		else if ( name.equals( ICubeModel.MEASURE_GROUPS_PROP ) )
		{
			return UIHelper.getImage( BuilderConstancts.IMAGE_MEASUREGROUP_FOLDER );
		}
		return super.getNodeIcon( model );
	}

	public boolean hasChildren( Object object )
	{
		return getChildren( object ).length > 0;
	}

	public Object getParent( Object model )
	{
		PropertyHandle property = (PropertyHandle) model;
		return property.getElementHandle( );
	}

	public String getNodeDisplayName( Object model )
	{
		PropertyHandle property = (PropertyHandle) model;
		String name = property.getPropertyDefn( ).getName( );
		if ( name.equals( ICubeModel.DIMENSIONS_PROP ) )
		{
			return "Groups(Dimensions)";
		}
		else if ( name.equals( ICubeModel.MEASURE_GROUPS_PROP ) )
		{
			return "Summary Fields(Measures)";
		}
		return null;
	}

	/**
	 * Gets the children element of the given model using visitor.
	 * 
	 * @param object
	 *            the handle
	 */
	public Object[] getChildren( Object object )
	{
		PropertyHandle property = (PropertyHandle) object;
		String name = property.getPropertyDefn( ).getName( );

		if ( name.equals( ICubeModel.DIMENSIONS_PROP ) )
		{
			CubeHandle cube = (CubeHandle) property.getElementHandle( );
			return cube.getContents( CubeHandle.DIMENSIONS_PROP ).toArray( );

		}
		else if ( name.equals( ICubeModel.MEASURE_GROUPS_PROP ) )
		{
			CubeHandle cube = (CubeHandle) property.getElementHandle( );
			return cube.getContents( CubeHandle.MEASURE_GROUPS_PROP ).toArray( );
		}
		return new Object[0];
	}

	public void createContextMenu( TreeViewer sourceViewer, Object object,
			IMenuManager menu )
	{
		PropertyHandle property = (PropertyHandle) object;
		String name = property.getPropertyDefn( ).getName( );

		EditCubeAction action = null;

		if ( name.equals( ICubeModel.DIMENSIONS_PROP ) )
		{
			action = new EditCubeAction( property,
					Messages.getString( "CubeModel.group.new" ) );//$NON-NLS-1$

		}
		else if ( name.equals( ICubeModel.MEASURE_GROUPS_PROP ) )
		{
			action = new EditCubeAction( property,
					Messages.getString( "CubeModel.summaryfield.new" ) );//$NON-NLS-1$
		}

		if ( action != null )
			menu.add( action );
		super.createContextMenu( sourceViewer, object, menu );

		menu.insertBefore( IWorkbenchActionConstants.MB_ADDITIONS + "-refresh", //$NON-NLS-1$
				new ShowPropertyAction( object ) );

		menu.insertAfter( IWorkbenchActionConstants.MB_ADDITIONS + "-refresh", new Separator( ) ); //$NON-NLS-1$
		menu.insertAfter( IWorkbenchActionConstants.MB_ADDITIONS + "-refresh", new RefreshAction( sourceViewer ) ); //$NON-NLS-1$
	}

}

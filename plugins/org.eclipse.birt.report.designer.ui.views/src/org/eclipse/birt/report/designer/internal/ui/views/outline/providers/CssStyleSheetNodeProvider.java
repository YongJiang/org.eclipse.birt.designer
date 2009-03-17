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

package org.eclipse.birt.report.designer.internal.ui.views.outline.providers;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.birt.report.designer.internal.ui.views.DefaultNodeProvider;
import org.eclipse.birt.report.designer.internal.ui.views.actions.EditUseCssStyleAction;
import org.eclipse.birt.report.designer.internal.ui.views.actions.ReloadCssStyleAction;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.designer.ui.ReportPlatformUIImages;
import org.eclipse.birt.report.designer.util.AlphabeticallyComparator;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.ElementDetailHandle;
import org.eclipse.birt.report.model.api.IResourceLocator;
import org.eclipse.birt.report.model.api.IncludedCssStyleSheetHandle;
import org.eclipse.birt.report.model.api.ModuleHandle;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.SharedStyleHandle;
import org.eclipse.birt.report.model.api.ThemeHandle;
import org.eclipse.birt.report.model.api.css.CssStyleSheetHandle;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

/**
 * 
 */

public class CssStyleSheetNodeProvider extends DefaultNodeProvider
{

	/**
	 * Creates the context menu for the given object.
	 * 
	 * @param object
	 * 		the object
	 * @param menu
	 * 		the menu
	 */
	public void createContextMenu( TreeViewer sourceViewer, Object object,
			IMenuManager menu )
	{
		menu.add( new EditUseCssStyleAction( object ) );
		menu.add( new ReloadCssStyleAction( object ) );
		super.createContextMenu( sourceViewer, object, menu );
	}

	public String getNodeDisplayName( Object model )
	{
		String fileName = ( (CssStyleSheetHandle) model ).getFileName( );
		return fileName.substring( fileName.lastIndexOf( "/" ) + 1 ); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.birt.report.designer.ui.views.INodeProvider#getChildren(java
	 * .lang.Object)
	 */
	public Object[] getChildren( Object model )
	{
		if ( model instanceof CssStyleSheetHandle )
		{
			CssStyleSheetHandle cssStyleHandle = (CssStyleSheetHandle) model;
			List childrenList = new ArrayList( );
			for ( Iterator iter = cssStyleHandle.getStyleIterator( ); iter.hasNext( ); )
			{
				SharedStyleHandle styleHandle = (SharedStyleHandle) iter.next( );
				childrenList.add( styleHandle );
			}

			Object[] childrenArray = childrenList.toArray( new SharedStyleHandle[childrenList.size( )] );
			Arrays.sort( childrenArray, new AlphabeticallyComparator( ) );
			return childrenArray;
		}
		return super.getChildren( model );
	}

	/**
	 * Gets the icon image for the given model.
	 * 
	 * @param model
	 * 		the model of the node
	 * 
	 * @return Returns the icon name for the model,or null if no proper one
	 * 	available for the given model
	 */
	public Image getNodeIcon( Object model )
	{
		Image icon = null;

		if ( model instanceof CssStyleSheetHandle )
		{
			icon = ReportPlatformUIImages.getImage( model );
			return icon;
		}
		return super.getNodeIcon( model );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.birt.report.designer.internal.ui.views.INodeProvider#
	 * getNodeTooltip(java.lang.Object)
	 */
	public String getNodeTooltip( Object model )
	{
		if ( model instanceof CssStyleSheetHandle )
		{
			CssStyleSheetHandle cssStyleSheetHandle = (CssStyleSheetHandle) model;
			ModuleHandle moudleHandle = cssStyleSheetHandle.getModule( )
					.getModuleHandle( );
			URL url = moudleHandle.findResource( cssStyleSheetHandle.getFileName( ),
					IResourceLocator.CASCADING_STYLE_SHEET );
			
			if(url != null && url.getFile( ) != null)
			{
				return url.getFile( );
			}
			
		}

		return super.getNodeTooltip( model );

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.views.DefaultNodeProvider#performEdit(org.eclipse.birt.model.api.ElementDetailHandle)
	 */
	protected boolean performEdit( ElementDetailHandle handle )
	{
		EditUseCssStyleAction action = new EditUseCssStyleAction(handle);
		if(!action.isEnabled( ))
		{
			return false;
		}
		action.run( );
		return true;
	}
}
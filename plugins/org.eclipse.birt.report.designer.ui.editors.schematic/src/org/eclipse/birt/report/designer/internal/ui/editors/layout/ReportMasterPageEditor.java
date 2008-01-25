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

package org.eclipse.birt.report.designer.internal.ui.editors.layout;

import org.eclipse.birt.report.designer.core.util.mediator.request.ReportRequest;
import org.eclipse.birt.report.designer.internal.ui.palette.MasterPagePaletteFactory;
import org.eclipse.birt.report.designer.internal.ui.util.ExceptionHandler;
import org.eclipse.birt.report.designer.ui.newelement.DesignElementFactory;
import org.eclipse.birt.report.model.api.MasterPageHandle;
import org.eclipse.birt.report.model.api.ModuleHandle;
import org.eclipse.birt.report.model.api.SimpleMasterPageHandle;
import org.eclipse.birt.report.model.api.command.ContentException;
import org.eclipse.birt.report.model.api.command.NameException;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.ui.IEditorPart;

/**
 * <p>
 * Report master page graphical editor.
 * </p>
 */
public class ReportMasterPageEditor extends
		ReportEditorWithRuler
{

	public ReportMasterPageEditor( )
	{
		super( );
	}

	/**
	 * @see org.eclipse.gef.ui.parts.GraphicalEditor#initializeGraphicalViewer()
	 */
	protected void initializeGraphicalViewer( )
	{
		super.initializeGraphicalViewer( );
		//setViewContentsAsMasterPage( );

	}

	protected void setContents( )
	{
		setViewContentsAsMasterPage( );
	}
	/**
	 * Set view's contents.
	 * 
	 * @param model
	 *            design handle of master page
	 */
	public void setViewContentsAsMasterPage( )
	{
		ModuleHandle designHandle = getModel( );
		SimpleMasterPageHandle masterPage = null;
		if ( designHandle.getMasterPages( ).getCount( ) == 0 )
		{
			// masterPage = designHandle.getElementFactory( )
			// .newSimpleMasterPage( "Simple MasterPage" ); //$NON-NLS-1$
			masterPage = DesignElementFactory.getInstance( designHandle.getModuleHandle( ) )
					.newSimpleMasterPage( null); 
			try
			{
				designHandle.getMasterPages( ).add( masterPage );
			}
			catch ( ContentException e )
			{
				ExceptionHandler.handle( e );
			}
			catch ( NameException e )
			{
				ExceptionHandler.handle( e );
			}
		}
		else
		{
			masterPage = (SimpleMasterPageHandle) designHandle.getMasterPages( )
					.get( 0 );
		}
		getGraphicalViewer( ).setContents( masterPage );
		//re set the processsor
		hookModelEventManager( masterPage );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.birt.report.designer.ui.editors.schematic.layout.AbstractReportGraphicalEditorWithFlyoutPalette#getPaletteRoot()
	 */
	protected PaletteRoot getPaletteRoot( )
	{
		if ( paletteRoot == null )
		{
			paletteRoot = MasterPagePaletteFactory.createPalette( );
		}
		return paletteRoot;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.birt.report.designer.internal.ui.editors.parts.GraphicalEditorWithFlyoutPalette#performRequest(org.eclipse.birt.report.designer.core.util.mediator.request.ReportRequest)
	 */
	public void performRequest( ReportRequest request )
	{
		if ( ReportRequest.LOAD_MASTERPAGE.equals( request.getType( ) )
				&& ( request.getSelectionModelList( ).size( ) == 1 )
				&& request.getSelectionModelList( ).get( 0 ) instanceof MasterPageHandle )
		{
			handlerLoadMasterPage( request );
			return;
		}

		super.performRequest( request );
	}

	/**
	 * @param request
	 */
	protected void handlerLoadMasterPage( ReportRequest request )
	{
		Object handle = request.getSelectionModelList( ).get( 0 );
		if (getGraphicalViewer( ).getContents( ).getModel( ) != handle)
		{
			getGraphicalViewer( ).setContents( handle );
			hookModelEventManager( handle );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.birt.report.designer.internal.ui.editors.parts.GraphicalEditorWithFlyoutPalette#getMultiPageEditor()
	 */
	protected IEditorPart getMultiPageEditor( )
	{
		return null;
	}
}

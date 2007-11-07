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

package org.eclipse.birt.report.designer.ui.lib.explorer.action;

import org.eclipse.birt.report.designer.core.model.SessionHandleAdapter;
import org.eclipse.birt.report.designer.internal.ui.util.ExceptionHandler;
import org.eclipse.birt.report.designer.internal.ui.util.UIUtil;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.designer.ui.lib.explorer.resource.ReportResourceEntry;
import org.eclipse.birt.report.designer.util.DNDUtil;
import org.eclipse.birt.report.model.api.CascadingParameterGroupHandle;
import org.eclipse.birt.report.model.api.DataSetHandle;
import org.eclipse.birt.report.model.api.DataSourceHandle;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.EmbeddedImageHandle;
import org.eclipse.birt.report.model.api.LibraryHandle;
import org.eclipse.birt.report.model.api.ModuleHandle;
import org.eclipse.birt.report.model.api.ParameterGroupHandle;
import org.eclipse.birt.report.model.api.ParameterHandle;
import org.eclipse.birt.report.model.api.olap.CubeHandle;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.views.contentoutline.ContentOutline;

/**
 * 
 */

public class AddElementtoReport extends Action
{

	private StructuredViewer viewer;
	private Object element;
	private int canContain;
	private Object target;

	private static final String ACTION_TEXT = Messages.getString( "AddElementtoAction.Text" ); //$NON-NLS-1$

	public void setSelectedElement( Object element )
	{
		if ( element instanceof ReportResourceEntry )
		{
			this.element = ( (ReportResourceEntry) element ).getReportElement( );
		}
		else
		{
			this.element = element;
		}

	}

	/**
	 * @param text
	 * @param style
	 */
	public AddElementtoReport( StructuredViewer viewer )
	{
		super( ACTION_TEXT );
		this.viewer = viewer;
		canContain = DNDUtil.CONTAIN_NO;
	}

	/*
	 * (non-Javadoc) Method declared on IAction.
	 */
	public boolean isEnabled( )
	{
		Object target = getTarget( );
		this.target = target;

		if ( canContain( target, element ) )
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public Object getTarget( )
	{
		IViewPart viewPart = UIUtil.getView( IPageLayout.ID_OUTLINE );
		if ( !( viewPart instanceof ContentOutline ) )
		{
			return null;
		}
		ContentOutline outlineView = (ContentOutline) viewPart;

		ISelection selection = outlineView.getSelection( );
		if ( selection instanceof StructuredSelection )
		{
			StructuredSelection strSelection = (StructuredSelection) selection;
			if ( strSelection.size( ) == 1 )
			{
				return strSelection.getFirstElement( );
			}
		}
		return null;
	}

	public void run( )
	{
		copyData( target, element );
	}

	protected boolean canContain( Object target, Object transfer )
	{
		//bug#192319
		if ( transfer instanceof DataSetHandle
				|| transfer instanceof DataSourceHandle
				|| transfer instanceof ParameterHandle
				|| transfer instanceof ParameterGroupHandle
				|| transfer instanceof CascadingParameterGroupHandle
				|| transfer instanceof CubeHandle )
			return true;

		if ( DNDUtil.handleValidateTargetCanContainMore( target,
				DNDUtil.getObjectLength( transfer ) ) )
		{
			canContain = DNDUtil.handleValidateTargetCanContain( target,
					transfer,
					true );
			return canContain == DNDUtil.CONTAIN_THIS;
		}
		return false;

	}

	private int getPosition( Object target )
	{

		int position = DNDUtil.calculateNextPosition( target, canContain );
		if ( position > -1 )
		{
			this.target = DNDUtil.getDesignElementHandle( target )
					.getContainerSlotHandle( );
		}
		return position;
	}

	protected boolean copyData( Object target, Object transfer )
	{

		ModuleHandle moduleHandle = SessionHandleAdapter.getInstance( )
				.getReportDesignHandle( );

		//bug#192319
		if ( transfer instanceof DataSetHandle )
		{
			target = moduleHandle.getDataSets( );
		}
		else if ( transfer instanceof DataSourceHandle )
		{
			target = moduleHandle.getDataSources( );
		}
		else if ( transfer instanceof ParameterHandle
				|| transfer instanceof ParameterGroupHandle
				|| transfer instanceof CascadingParameterGroupHandle )
		{
			target = moduleHandle.getParameters( );
		}
		else if ( transfer instanceof CubeHandle )
		{
			target = moduleHandle.getCubes( );
		}

		// When get position, change target value if need be
		int position = getPosition( target );
		boolean result = false;

		if ( transfer != null && transfer instanceof DesignElementHandle )
		{
			DesignElementHandle sourceHandle;
			if ( ( sourceHandle = (DesignElementHandle) transfer ).getRoot( ) instanceof LibraryHandle )
			{
				// transfer element from a library.
				LibraryHandle library = (LibraryHandle) sourceHandle.getRoot( );
				try
				{
					if ( moduleHandle != library )
					{
						// element from other library not itself, create a new
						// extended element.
						if ( UIUtil.includeLibrary( moduleHandle, library ) )
						{
							DNDUtil.addElementHandle( target,
									moduleHandle.getElementFactory( )
											.newElementFrom( sourceHandle,
													sourceHandle.getName( ) ) );
							result = true;
						}
					}
					else
					{
						result = DNDUtil.copyHandles( transfer,
								target,
								position );
					}
				}
				catch ( Exception e )
				{
					ExceptionHandler.handle( e );
				}
			}
			else
			{
				result = DNDUtil.copyHandles( transfer, target, position );
			}
		}
		else if ( transfer != null && transfer instanceof EmbeddedImageHandle )
		{
			result = DNDUtil.copyHandles( transfer, target, position );
		}

		if ( result )
		{
			viewer.reveal( target );
		}

		return result;
	}
}

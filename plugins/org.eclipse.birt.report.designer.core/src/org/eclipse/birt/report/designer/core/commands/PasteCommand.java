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

package org.eclipse.birt.report.designer.core.commands;

import org.eclipse.birt.report.designer.core.DesignerConstants;
import org.eclipse.birt.report.designer.core.model.SessionHandleAdapter;
import org.eclipse.birt.report.designer.util.DEUtil;
import org.eclipse.birt.report.designer.util.DNDUtil;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.ModuleHandle;
import org.eclipse.birt.report.model.api.PropertyHandle;
import org.eclipse.birt.report.model.api.SlotHandle;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.eclipse.birt.report.model.api.command.ContentException;
import org.eclipse.birt.report.model.api.command.NameException;
import org.eclipse.birt.report.model.api.core.IDesignElement;
import org.eclipse.gef.commands.Command;

/**
 * Paste Command
 * 
 */

public class PasteCommand extends Command
{

	/** Null permitted in instance. */
	private DesignElementHandle sourceHandle;

	private Object newContainer;

	private DesignElementHandle afterHandle;

	private IDesignElement cloneElement;

	/** True: cut; False: copy */
	private boolean isCut = false;

	private int slotID = -1;

	private int position = -1;

	private boolean isCloned = false;

	/**
	 * Constructor
	 * 
	 * @param sourceHandle
	 *            the source
	 * @param newContainer
	 *            the new container, class type could be
	 *            <code>DesignElementHandle</code>,<code>SlotHandle</code>
	 *            or <code>ReportElementModel</code>
	 * @param afterHandle
	 *            the handle next to the source
	 * @param isCut
	 *            If true, delete source
	 */
	public PasteCommand( DesignElementHandle sourceHandle, Object newContainer,
			DesignElementHandle afterHandle, boolean isCut )
	{
		this.sourceHandle = sourceHandle;
		this.cloneElement = sourceHandle.copy( );
		this.newContainer = newContainer;
		this.afterHandle = afterHandle;
		this.isCut = isCut;
		isCloned = true;
	}

	/**
	 * Constructor
	 * 
	 * @param sourceHandle
	 *            the source
	 * @param newContainer
	 *            the new container, class type could be
	 *            <code>DesignElementHandle</code>,<code>SlotHandle</code>
	 *            or <code>ReportElementModel</code>
	 * @param position
	 *            the position will be added
	 * @param isCut
	 *            If true, delete source
	 */
	public PasteCommand( DesignElementHandle sourceHandle, Object newContainer,
			int position, boolean isCut )
	{
		this.sourceHandle = sourceHandle;
		this.cloneElement = sourceHandle.copy( );
		this.newContainer = newContainer;
		this.position = position;
		this.isCut = isCut;
		isCloned = true;
	}

	/**
	 * Constructor
	 * 
	 * @param cloneElement
	 *            the copy of the source
	 * @param newContainer
	 *            the new container, class type could be
	 *            <code>DesignElementHandle</code>,<code>SlotHandle</code>
	 *            or <code>ReportElementModel</code>
	 * @param afterHandle
	 *            the handle next to the source
	 */
	public PasteCommand( IDesignElement cloneElement, Object newContainer,
			DesignElementHandle afterHandle )
	{
		this.cloneElement = cloneElement;
		this.newContainer = newContainer;
		this.afterHandle = afterHandle;
		isCloned = false;
	}

	/**
	 * Constructor
	 * 
	 * @param cloneElement
	 *            the copy of the source
	 * @param newContainer
	 *            the new container, class type could be
	 *            <code>DesignElementHandle</code>,<code>SlotHandle</code>
	 *            or <code>ReportElementModel</code>
	 * @param position
	 *            the position will be added
	 */
	public PasteCommand( IDesignElement cloneElement, Object newContainer,
			int position )
	{
		this.cloneElement = cloneElement;
		this.newContainer = newContainer;
		this.position = position;
		isCloned = false;
	}

	/**
	 * Executes the Command.
	 */
	public void execute( )
	{
		if ( DesignerConstants.TRACING_COMMANDS )
		{
			System.out.println( "PasteCommand >> Starts ..." ); //$NON-NLS-1$
		}
		try
		{
			if ( !isCut
					|| sourceHandle == null
					|| sourceHandle.getContainer( ) == null )
			{
				isCut = false;
			}

			calculatePositionAndSlotId( );

			// Drops old source handle if operation is cut
			dropSourceHandle( sourceHandle );

			// Gets new handle
			ModuleHandle currentDesignHandle = SessionHandleAdapter.getInstance( )
					.getReportDesignHandle( );
			DesignElementHandle newHandle = copyNewHandle( cloneElement,
					currentDesignHandle );

			// Adds new handle to report
			addHandleToReport( newHandle );
		}
		catch ( Exception e )
		{
			if ( DesignerConstants.TRACING_COMMANDS )
			{
				System.out.println( "PasteCommand >> Failed." ); //$NON-NLS-1$
			}
			e.printStackTrace( );
		}
	}

	/**	
	 *  Add this design element to report.
	 *  @param newHandle
	 *  	The design element to add
	 */
	private void addHandleToReport( DesignElementHandle newHandle )
			throws ContentException, NameException
	{

		SlotHandle slotHandle = null;
		if ( newContainer instanceof DesignElementHandle )
		{
			slotHandle = ( (DesignElementHandle) newContainer ).getSlot( slotID );
			slotHandle.paste( newHandle, position );
		}
		else if ( newContainer instanceof SlotHandle )
		{
			slotHandle = (SlotHandle) newContainer;
			slotHandle.paste( newHandle, position );
		}
//		else if ( newContainer instanceof ReportElementModel )
//		{
//			slotHandle = ( (ReportElementModel) newContainer ).getElementHandle( )
//					.getSlot( slotID );
//
//		}
		else if(newContainer instanceof PropertyHandle ){
			try
			{
				((PropertyHandle)newContainer).add( newHandle, position );
			}
			catch ( SemanticException e )
			{
				
			}
		}
		if ( DesignerConstants.TRACING_COMMANDS )
		{
			System.out.println( "PasteCommand >>  Finished. Paste " //$NON-NLS-1$
					+ DEUtil.getDisplayLabel( newHandle )
					+ " to the slot " //$NON-NLS-1$
					+ slotHandle.getSlotID( )
					+ " of " //$NON-NLS-1$
					+ DEUtil.getDisplayLabel( slotHandle.getElementHandle( ) )
					+ ",Position: " //$NON-NLS-1$
					+ position );
		}
	}

	/**
	 * 	Caculate the paste position
	 */
	private void calculatePositionAndSlotId( )
	{
		DesignElementHandle container = null;
		if ( newContainer instanceof DesignElementHandle )
		{
			slotID = DEUtil.getDefaultSlotID( newContainer );
			container = (DesignElementHandle) newContainer;
		}
		else if ( newContainer instanceof SlotHandle )
		{
			slotID = ( (SlotHandle) newContainer ).getSlotID( );
			container = ( (SlotHandle) newContainer ).getElementHandle( );
		}
//		else if ( newContainer instanceof ReportElementModel )
//		{
//			slotID = ( (ReportElementModel) newContainer ).getSlotId( );
//			container = ( (ReportElementModel) newContainer ).getElementHandle( );
//		}
		else
		{
			return;
		}

		if ( afterHandle != null )
		{
			position = DEUtil.findInsertPosition( container,
					afterHandle,
					slotID );
		}
		else if ( position > -1
				&& isCut
				&& sourceHandle.getContainer( ) == container )
		{
			int oldPosition = DEUtil.findInsertPosition( container,
					sourceHandle,
					slotID );
			if ( oldPosition < position )
			{
				position--;
			}
		}

	}
 /**
 * 
 *  Drop source handle
 * @param oldHandle
 * 		The source handle
 */
	private void dropSourceHandle( DesignElementHandle oldHandle )
			throws SemanticException
	{
		if ( isCut )
		{
			oldHandle.drop( );
		}
	}

/**
 * Copy new handle
 * @param element
 *   The elemnent to copy
 * @param currentDesignHandle
 * 	 Current design handle
 * @return
 * 	 The copied handle
 * @throws CloneNotSupportedException
 */
	private DesignElementHandle copyNewHandle( IDesignElement element,
			ModuleHandle currentDesignHandle )
			throws CloneNotSupportedException
	{
		IDesignElement newElement = isCloned ? element
				: (IDesignElement) element.clone( );
		DesignElementHandle handle = newElement.getHandle( currentDesignHandle.getModule( ) );
		currentDesignHandle.rename( handle );
		return handle;
	}

	/**
	 * @return <code>true</code> if the command can be executed
	 */
	public boolean canExecute( )
	{
		if ( cloneElement == null )
		{
			return false;
		}
		DesignElementHandle childHandle = sourceHandle;
		if ( childHandle == null )
		{
			childHandle = cloneElement.getHandle( SessionHandleAdapter.getInstance( )
					.getReportDesignHandle( )
					.getModule( ) );
		}
		return DNDUtil.handleValidateTargetCanContain( newContainer,
				childHandle )
				&& DNDUtil.handleValidateTargetCanContainMore( newContainer, 1 );
	}
}
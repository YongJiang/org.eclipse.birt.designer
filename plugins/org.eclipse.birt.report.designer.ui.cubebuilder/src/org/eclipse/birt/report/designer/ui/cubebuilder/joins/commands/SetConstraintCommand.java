/*******************************************************************************
 * Copyright (c) 2005 Actuate Corporation. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Actuate Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.birt.report.designer.ui.cubebuilder.joins.commands;

import org.eclipse.birt.report.designer.core.model.SessionHandleAdapter;
import org.eclipse.birt.report.designer.internal.ui.util.ExceptionHandler;
import org.eclipse.birt.report.designer.ui.cubebuilder.nls.Messages;
import org.eclipse.birt.report.designer.ui.cubebuilder.util.BuilderConstancts;
import org.eclipse.birt.report.designer.ui.cubebuilder.util.UIHelper;
import org.eclipse.birt.report.model.api.CommandStack;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.ModuleHandle;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * 
 * A Command to set the Constraints for a TableNodeEditPart
 * 
 */
public class SetConstraintCommand extends org.eclipse.gef.commands.Command
{

	private Point newPos;

	private Dimension newSize;

	private Object part;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute( )
	{
		if ( part == null || id == null )
			return;

		if ( part instanceof DesignElementHandle )
		{
			DesignElementHandle element = (DesignElementHandle) part;
			ModuleHandle module = element.getModuleHandle( );
			CommandStack stack = SessionHandleAdapter.getInstance( )
					.getCommandStack( );
			stack.startTrans( Messages.getString("SetConstraintCommand.setUserProperty") ); //$NON-NLS-1$
			try
			{
				UIHelper.setIntProperty( module,
						id,
						BuilderConstancts.POSITION_X,
						newPos.x );
				UIHelper.setIntProperty( module,
						id,
						BuilderConstancts.POSITION_Y,
						newPos.y );
				UIHelper.setIntProperty( module,
						id,
						BuilderConstancts.SIZE_WIDTH,
						newSize.width );
				UIHelper.setIntProperty( module,
						id,
						BuilderConstancts.SIZE_HEIGHT,
						newSize.height );
				stack.commit( );
			}
			catch ( SemanticException e )
			{
				ExceptionHandler.handle( e );
				stack.rollback( );
			}
		}
	}

	public void setLocation( Rectangle r )
	{
		setLocation( r.getLocation( ) );
		setSize( r.getSize( ) );
	}

	/**
	 * @param dimension
	 */
	private void setSize( Dimension dimension )
	{
		newSize = dimension;
	}

	/**
	 * Sets the Location of the element
	 * 
	 * @param p
	 */
	public void setLocation( Point p )
	{
		newPos = p;
	}

	/**
	 * Sets the Edit Part for this Event
	 * 
	 * @param part
	 *            The Editr Part to be Set
	 */
	public void setPart( Object part )
	{
		this.part = part;
	}

	private String id;

	public void setId( String id )
	{
		this.id = id;
	}

}
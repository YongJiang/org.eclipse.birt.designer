/*************************************************************************************
 * Copyright (c) 2004 Actuate Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Actuate Corporation - Initial implementation.
 ************************************************************************************/

package org.eclipse.birt.report.designer.internal.ui.command;

import org.eclipse.birt.report.designer.core.model.SessionHandleAdapter;
import org.eclipse.birt.report.model.activity.ActivityStack;
import org.eclipse.birt.report.model.activity.ActivityStackListener;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CommandStackListener;

/**
 * @author David Michonneau
 * 
 * This class is a command stack for the GEF framework. It internally access the
 * ActivityStack class of the DE. No commands are pushed to the command stack or
 * ActivityStack here since the Design handles take care of that when executed.
 * Commands are executed as transactions.
 */
public class WrapperCommandStack extends CommandStack
{

	private ActivityStack ar;

	public WrapperCommandStack( )
	{
		this( SessionHandleAdapter.getInstance( )
				.getReportDesign( )
				.handle( )
				.getCommandStack( ) );
	}

	public WrapperCommandStack(
			org.eclipse.birt.report.model.api.CommandStack ar )
	{
		this.ar = (ActivityStack) ar;
	}

	public boolean canUndo( )
	{
		return ar.canUndo( );
	}

	public boolean canRedo( )
	{
		return ar.canRedo( );
	}

	public void undo( )
	{
		if ( canUndo( ) )
			ar.undo( );
	}

	public void redo( )
	{
		if ( canRedo( ) )
			ar.redo( );
	}

	public void flush( )
	{
		ar.flush( );
	}

	public Command getRedoCommand( )
	{
		return new CommandWrap4DE( ar.getRedoRecord( ) );
	}

	public Command getUndoCommand( )
	{
		return new CommandWrap4DE( ar.getUndoRecord( ) );
	}

	public void execute( Command command )
	{
		ar.startTrans( );
		command.execute( );
		ar.commit( );

	}

	public void setUndoLimit( int undoLimit )
	{
		ar.setStackLimit( undoLimit );
	}

	public void addCommandStackListener( ActivityStackListener listener )
	{
		ar.addListener( listener );
	}

	public void removeCommandStackListener( ActivityStackListener listener )
	{
		ar.removeListener( listener );
	}

	/*
	 * Do not use, use addCommandStackListener(ActivityStackListener) instead
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.CommandStack#addCommandStackListener(org.eclipse.gef.commands.CommandStackListener)
	 */
	public void addCommandStackListener( CommandStackListener listener )
	{
		// use addCommandStackListener(ActivityStackListener) instead
		assert false;
	}

	/*
	 * Do not use, use removeCommandStackListener(ActivityStackListener) instead
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.CommandStack#removeCommandStackListener(org.eclipse.gef.commands.CommandStackListener)
	 */
	public void removeCommandStackListener( CommandStackListener listener )
	{
		// use removeCommandStackListener(ActivityStackListener) instead
		assert false;
	}

	public void setActivityStack( ActivityStack ar )
	{
		this.ar = ar;
	}

}
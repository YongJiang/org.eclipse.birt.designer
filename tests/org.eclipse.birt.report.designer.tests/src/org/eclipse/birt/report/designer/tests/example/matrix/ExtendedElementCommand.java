/*******************************************************************************
 * Copyright (c) 2004 Actuate Corporation. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Actuate Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.birt.report.designer.tests.example.matrix;

import org.eclipse.birt.report.model.extension.IElementCommand;

public class ExtendedElementCommand implements IElementCommand
{

	private ExtendedElement element = null;

	//	private IPropertyDefinition propDefn = null;
	//	private PropertyDefn propDefn = null;
	private String propName = null;

	public static String commandTag = "initial"; //$NON-NLS-1$	
	public static final String EXECUTE_TAG = "execute"; //$NON-NLS-1$
	public static final String REDO_TAG = "redo"; //$NON-NLS-1$
	public static final String UNDO_TAG = "undo"; //$NON-NLS-1$

	/**
	 * The old value of the property
	 */

	private Object oldValue = null;

	/**
	 * The new value of the property
	 */

	private Object newValue = null;

	/**
	 * Constructor.
	 * 
	 * @param propertyOwner
	 *            the report element that has the property
	 * @param name
	 *            the name of the property to change
	 * @param value
	 *            the new value
	 */

	public ExtendedElementCommand( ExtendedElement propertyOwner, String name,
			Object value )
	{
		assert propertyOwner != null;
		element = propertyOwner;
		assert name != null;

		propName = name;
		newValue = value;
		oldValue = propertyOwner.getProperty( name );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.model.design.core.activity.SimpleRecord#perform(boolean)
	 */

	protected void perform( boolean undo )
	{
		Object value = undo ? oldValue : newValue;
		if ( propName.equals( "company" ) && value != null ) //$NON-NLS-1$
			element.doSetProperty( propName, value.toString( ) + commandTag );
		else if ( propName.equals( "company" ) && value == null ) //$NON-NLS-1$
			element.doSetProperty( propName, commandTag );
		else
			element.doSetProperty( propName, value );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.model.extension.IExtendedElementCommand#execute()
	 */
	public void execute( )
	{
		commandTag = EXECUTE_TAG;
		perform( false );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.model.extension.IExtendedElementCommand#undo()
	 */
	public void undo( )
	{
		commandTag = UNDO_TAG;
		perform( true );

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.model.extension.IExtendedElementCommand#redo()
	 */
	public void redo( )
	{
		commandTag = REDO_TAG;
		perform( false );

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.model.extension.IExtendedElementCommand#canUndo()
	 */
	public boolean canUndo( )
	{
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.model.extension.IExtendedElementCommand#canRedo()
	 */
	public boolean canRedo( )
	{
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.model.extension.IExtendedElementCommand#getLabel()
	 */
	public String getLabel( )
	{
		return "Command"; //$NON-NLS-1$
	}
}
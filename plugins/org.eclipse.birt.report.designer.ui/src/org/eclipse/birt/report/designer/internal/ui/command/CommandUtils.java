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

package org.eclipse.birt.report.designer.internal.ui.command;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractParameterValueConverter;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterType;
import org.eclipse.core.commands.ParameterValueConversionException;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.services.IDisposable;

/**
 * 
 */

public class CommandUtils
{

	public static Parameterization createParameter( Command command,
			String parameterId, Object value ) throws NotDefinedException,
			ExecutionException, ParameterValueConversionException
	{
		ParameterType parameterType = command.getParameterType( parameterId );
		if ( parameterType == null )
		{
			throw new ExecutionException( "Command does not have a parameter type for the given parameter" ); //$NON-NLS-1$
		}

		IParameter param = command.getParameter( parameterId );
		AbstractParameterValueConverter valueConverter = parameterType.getValueConverter( );
		if ( valueConverter == null )
		{
			throw new ExecutionException( "Command does not have a value converter" ); //$NON-NLS-1$
		}

		String valueString = valueConverter.convertToString( value );
		Parameterization parm = new Parameterization( param, valueString );
		return parm;
	}

	public static void disposeParameter( String parameterId, Command command )
			throws NotDefinedException
	{
		ParameterType parameterType = command.getParameterType( parameterId );
		Object valueConverter = parameterType.getValueConverter( );
		if ( valueConverter instanceof IDisposable )
			( (IDisposable) valueConverter ).dispose( );
	}

	public static ICommandService getCommandService( )
	{
		return (ICommandService) PlatformUI.getWorkbench( )
				.getAdapter( ICommandService.class );
	}

	public static IHandlerService getHandlerService( )
	{
		return (IHandlerService) PlatformUI.getWorkbench( )
				.getAdapter( IHandlerService.class );
	}

	public static Command getCommand( String commandId )
	{
		return getCommandService( ).getCommand( commandId );
	}

	public static Object executeCommand( String commandId, Map paramMap )
			throws NotDefinedException, ExecutionException,
			ParameterValueConversionException, NotEnabledException,
			NotHandledException
	{
		Command cmd = CommandUtils.getCommand( commandId );
		List paramList = new ArrayList( );
		if ( paramMap != null )
		{
			for ( Iterator iter = paramMap.keySet( ).iterator( ); iter.hasNext( ); )
			{
				String paramId = (String) iter.next( );
				Object value = paramMap.get( paramId );
				if ( value != null )
				{
					paramList.add( createParameter( cmd, paramId, value ) );
				}
			}
		}
		if ( paramList.size( ) > 0 )
		{
			ParameterizedCommand paramCommand = new ParameterizedCommand( cmd,
					(Parameterization[]) paramList.toArray( new Parameterization[paramList.size( )] ) );

			return getHandlerService( ).executeCommand( paramCommand, null );
		}
		else
		{
			return getHandlerService( ).executeCommand( commandId, null );
		}
	}

	public static Object executeCommand( String commandId )
			throws ExecutionException, NotDefinedException,
			NotEnabledException, NotHandledException
	{
		
		return getHandlerService( ).executeCommand( commandId, null );
	}
	
	public static void setVariable(String name, Object value)
	{
		getHandlerService( ).getCurrentState( ).addVariable( name, value );
	}
	
	public static void removeVariable(String name)
	{
		getHandlerService( ).getCurrentState( ).removeVariable( name );
	}
	
}

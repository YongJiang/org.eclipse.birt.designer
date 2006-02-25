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

package org.eclipse.birt.report.designer.core.model;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.birt.report.designer.core.util.mediator.ReportMediator;
import org.eclipse.birt.report.model.api.CommandStack;
import org.eclipse.birt.report.model.api.DesignEngine;
import org.eclipse.birt.report.model.api.DesignFileException;
import org.eclipse.birt.report.model.api.MasterPageHandle;
import org.eclipse.birt.report.model.api.ModuleHandle;
import org.eclipse.birt.report.model.api.SessionHandle;
import org.eclipse.birt.report.model.api.SimpleMasterPageHandle;
import org.eclipse.birt.report.model.api.SlotHandle;
import org.eclipse.birt.report.model.api.command.ContentException;
import org.eclipse.birt.report.model.api.command.NameException;
import org.eclipse.birt.report.model.api.core.DisposeEvent;
import org.eclipse.birt.report.model.api.core.IDisposeListener;
import org.eclipse.birt.report.model.api.metadata.IMetaDataDictionary;

/**
 * Adapter class to adpat model handle. This adapter provides convenience
 * methods to GUI requirement SessionHandleAdapter responds to model
 * SessionHandle
 * 
 */

public class SessionHandleAdapter
{

	public static final int UNKNOWFILE = -1;
	public static int DESIGNEFILE = 0;
	public static int LIBRARYFILE = 1;
	public static int TEMPLATEFILE = 2;

	private int type = DESIGNEFILE;
	IDisposeListener disposeLitener = new IDisposeListener( ) {

		public void moduleDisposed( ModuleHandle targetElement, DisposeEvent ev )
		{
			ReportMediator media = (ReportMediator) mediatorMap.get( targetElement );
			if ( media != null )
			{
				media.dispose( );
			}
			mediatorMap.remove( targetElement );
			targetElement.removeDisposeListener( this );
		}
	};

	// add field support mediator
	private Map mediatorMap = new WeakHashMap( );

	/**
	 * constructor Mark it to private to avoid new opeartion.
	 */
	private SessionHandleAdapter( )
	{

	}

	public int getFileType( )
	{
		return type;
	}

	private static SessionHandleAdapter sessionAdapter;

	private SessionHandle sessionHandle;

	private ReportDesignHandleAdapter designHandleAdapter;

	/**
	 * Gets singleton instance method
	 * 
	 * @return return SessionHandleAdapter instance
	 */

	public static SessionHandleAdapter getInstance( )
	{
		if ( sessionAdapter == null )
		{
			sessionAdapter = new SessionHandleAdapter( );
		}
		return sessionAdapter;
	}

	/**
	 * @return Session handle
	 */

	public SessionHandle getSessionHandle( )
	{
		if ( sessionHandle == null )
		{
			sessionHandle = DesignEngine.newSession( Locale.getDefault( ) );
			IMetaDataDictionary metadata = DesignEngine.getMetaDataDictionary( );
			metadata.enableElementID( );
		}
		sessionHandle.activate( );
		return sessionHandle;
	}

	public void init( String fileName, InputStream input )
			throws DesignFileException
	{
		init( fileName, input, DESIGNEFILE );
	}

	public void init( String fileName, InputStream input,boolean flag )
			throws DesignFileException
	{
		init( fileName, input, DESIGNEFILE,true );
	}

	/**
	 * Initialize a report design file
	 * 
	 * @param fileName
	 *            design file name
	 * @param input
	 *            File input stream
	 * @throws DesignFileException
	 */

	public void init( String fileName, InputStream input, int type )
			throws DesignFileException
	{

		if ( getReportDesignHandle( ) != null
				&& fileName.equalsIgnoreCase( getReportDesignHandle( ).getFileName( ) ) )
		{
			return;
		}

		init( fileName, input, type, true );

	}

	public void init( String fileName, InputStream input, int type,
			boolean reLoad ) throws DesignFileException
	{

		try
		{
			ModuleHandle handle = null;
			if ( type == DESIGNEFILE || type == TEMPLATEFILE )
			{
				handle = getSessionHandle( ).openDesign( fileName, input );
			}
			else if ( type == LIBRARYFILE )
			{
				handle = getSessionHandle( ).openLibrary( fileName );
			}
			else
			{
				throw new RuntimeException( "Not support this type of file" );
			}
			postInit( handle );
			designHandleAdapter = new ReportDesignHandleAdapter( handle );
		}
		catch ( DesignFileException e )
		{
			throw e;
		}

	}

	/**
	 * @param handle
	 */
	private void postInit( ModuleHandle handle )
	{
		SimpleMasterPageHandle masterPage = null;
		if ( handle.getMasterPages( ).getCount( ) == 0 )
		{
			masterPage = handle.getElementFactory( )
					.newSimpleMasterPage( "Simple MasterPage" ); //$NON-NLS-1$
			try
			{
				handle.getMasterPages( ).add( masterPage );
			}
			catch ( ContentException e )
			{
				new DesignFileException( handle.getFileName( ), e );
			}
			catch ( NameException e )
			{
				new DesignFileException( handle.getFileName( ), e );
			}
		}
	}

	/**
	 * Create report design instance
	 * 
	 * @return created report design instance
	 */
	public ModuleHandle creatReportDesign( )
	{
		return getSessionHandle( ).createDesign( );
	}

	/**
	 * @return wrapped report design handle.
	 */
	public ModuleHandle getReportDesignHandle( )
	{
		if ( designHandleAdapter != null )
		{
			return designHandleAdapter.getModuleHandle( );
		}
		return null;

	}

	/**
	 * Sets report design.
	 * 
	 * @param handle
	 *            the model
	 */
	public void setReportDesignHandle( ModuleHandle handle )
	{

		if(designHandleAdapter!=null)
		{
			designHandleAdapter.setReportDesignHandle( handle );
		}
	}

	/**
	 * @return Command stack of current session.
	 */
	public CommandStack getCommandStack( )
	{
		if ( getReportDesignHandle( ) != null )
		{
			return getReportDesignHandle( ).getCommandStack( );
		}

		return null;
	}

	/**
	 * Gets the first MasterPageHandle
	 * 
	 */
	public MasterPageHandle getMasterPageHandle( )
	{
		return getMasterPageHandle( getReportDesignHandle( ) );
	}

	/**
	 * Gets the first MasterPageHandle
	 * 
	 * @param handle
	 * @return
	 */
	public MasterPageHandle getMasterPageHandle( ModuleHandle handle )
	{
		SlotHandle slotHandle = handle.getMasterPages( );
		Iterator iter = slotHandle.iterator( );
		return (MasterPageHandle) iter.next( );
	}

	/**
	 * 
	 * @param handle
	 *            the model
	 * @return get corresponding mediator
	 */
	public ReportMediator getMediator( ModuleHandle handle )
	{
		if ( handle != null )
		{
			handle.addDisposeListener( disposeLitener );
		}
		ReportMediator mediator = (ReportMediator) mediatorMap.get( handle );
		if ( mediator == null )
		{
			mediator = new ReportMediator( );
			mediatorMap.put( handle, mediator );
		}
		return mediator;
	}

	/**
	 * @return the current mediator
	 */
	public ReportMediator getMediator( )
	{
		return getMediator( getReportDesignHandle( ) );
	}

	/**
	 * @param oldObj
	 *            old model
	 * @param newObj
	 *            new model
	 */
	public void resetReportDesign( Object oldObj, Object newObj )
	{
		ReportMediator mediator = (ReportMediator) mediatorMap.get( oldObj );
		if ( mediator == null )
		{
			return;
		}
		mediatorMap.remove( oldObj );
		mediatorMap.put( newObj, mediator );
	}

	public void clear( )
	{
		mediatorMap.remove( getInstance( ).getReportDesignHandle( ) );
		setReportDesignHandle( null );
		designHandleAdapter = null;
		
	}

}
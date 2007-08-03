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

package org.eclipse.birt.report.designer.internal.ui.views;

import org.eclipse.birt.report.designer.internal.ui.editors.parts.event.IFastConsumerProcessor;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.editparts.AbstractModelEventProcessor;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.ModuleHandle;
import org.eclipse.birt.report.model.api.ParameterHandle;
import org.eclipse.birt.report.model.api.activity.NotificationEvent;
import org.eclipse.birt.report.model.api.command.ContentEvent;
import org.eclipse.birt.report.model.api.command.NameEvent;

/**
 * Processor the model event for the DesignerOutline
 */

public class DataViewEventProcessor extends AbstractModelEventProcessor implements
		IFastConsumerProcessor
{

	public static String EVENT_CONTENT = "Event Content";
	public static String VARIABLE_NAME = "Variable Name";

	/**
	 * @param factory
	 */
	public DataViewEventProcessor( IModelEventFactory factory )
	{
		super( factory );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.editors.schematic.editparts.GraphicsViewModelEventProcessor#createModelEventInfoFactory()
	 */
	protected ModelEventInfoFactory createModelEventInfoFactory( )
	{
		return new DataViewModelEventInfoFactory( );
	}

	/**
	 * OutlineModelEventInfoFactory
	 */
	private static class DataViewModelEventInfoFactory implements
			ModelEventInfoFactory
	{

		/*
		 * Creat the report runnable for the DesignerOutline.
		 * 
		 * @see org.eclipse.birt.report.designer.internal.ui.editors.schematic.editparts.GraphicsViewModelEventProcessor.ModelEventInfoFactory#createModelEventInfo(org.eclipse.birt.report.model.api.DesignElementHandle,
		 *      org.eclipse.birt.report.model.api.activity.NotificationEvent)
		 */
		public ModelEventInfo createModelEventInfo( DesignElementHandle focus,
				NotificationEvent ev )
		{
			switch ( ev.getEventType( ) )
			{
				case NotificationEvent.CONTENT_EVENT :
				{
					if ( ev instanceof ContentEvent
							&& focus instanceof ModuleHandle )
					{
						ContentEvent event = (ContentEvent) ev;
						if ( event.getAction( ) == ContentEvent.REMOVE )
						{
							DesignElementHandle contentHandle = event.getContent( )
									.getHandle( focus.getRoot( ).getModule( ) );
							if ( contentHandle instanceof ParameterHandle )
								return new DataViewParameterModelEventInfo( (ParameterHandle) contentHandle,
										ev );
						}
					}
					return new DataViewContentModelEventInfo( focus, ev );
				}
				case NotificationEvent.NAME_EVENT :
				{
					if ( focus instanceof ParameterHandle )
						return new DataViewParameterModelEventInfo( (ParameterHandle) focus,
								ev );
				}
				case NotificationEvent.PROPERTY_EVENT :
				{
					if ( focus instanceof ParameterHandle )
						return new DataViewParameterModelEventInfo( (ParameterHandle) focus,
								ev );
				}
				default :
				{
					return new RefreshModelEventInfo( focus, ev );
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.editors.schematic.editparts.GraphicsViewModelEventProcessor#includeEventType(int)
	 */
	protected boolean includeEventType( int type )
	{
		return true;
	}

	/**
	 * Process the content model event. OutlineContentModelEventInfo
	 */
	protected static class DataViewContentModelEventInfo extends ModelEventInfo
	{

		/**
		 * @param focus
		 * @param ev
		 */
		private DataViewContentModelEventInfo( DesignElementHandle focus,
				NotificationEvent ev )
		{
			super( focus, ev );
			assert ev instanceof ContentEvent;
			setContent( ( (ContentEvent) ev ).getContent( ) );
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.report.designer.internal.ui.editors.schematic.editparts.GraphicsViewModelEventProcessor.ModelEventInfo#canAcceptModelEvent(org.eclipse.birt.report.model.api.DesignElementHandle,
		 *      org.eclipse.birt.report.model.api.activity.NotificationEvent)
		 */
		public boolean canAcceptModelEvent( ModelEventInfo info )
		{
			return false;
		}

		public Object getContent( )
		{
			return getOtherInfo( ).get( EVENT_CONTENT );
		}

		public void setContent( Object obj )
		{
			getOtherInfo( ).put( EVENT_CONTENT, obj );
		}

	}

	protected static class DataViewParameterModelEventInfo extends
			ModelEventInfo
	{

		/**
		 * @param focus
		 * @param ev
		 */
		private DataViewParameterModelEventInfo( ParameterHandle focus,
				NotificationEvent ev )
		{
			super( focus, ev );
			assert ev instanceof ContentEvent;
			switch ( ev.getEventType( ) )
			{
				case NotificationEvent.CONTENT_EVENT :
				{
					setVariableName( focus.getName( ) );
				}
				case NotificationEvent.NAME_EVENT :
				{
					if ( ev instanceof NameEvent )
						setVariableName( ( (NameEvent) ev ).getOldName( ) );
				}
				case NotificationEvent.PROPERTY_EVENT :
				{
					setVariableName( focus.getName( ) );
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.report.designer.internal.ui.editors.schematic.editparts.GraphicsViewModelEventProcessor.ModelEventInfo#canAcceptModelEvent(org.eclipse.birt.report.model.api.DesignElementHandle,
		 *      org.eclipse.birt.report.model.api.activity.NotificationEvent)
		 */
		public boolean canAcceptModelEvent( ModelEventInfo info )
		{
			return false;
		}

		public Object getVariableName( )
		{
			return getOtherInfo( ).get( VARIABLE_NAME );
		}

		public void setVariableName( Object obj )
		{
			getOtherInfo( ).put( VARIABLE_NAME, obj );
		}

	}

	/**
	 * RefreshModelEventInfo
	 */
	protected static class RefreshModelEventInfo extends ModelEventInfo
	{

		/**
		 * @param focus
		 * @param ev
		 */
		private RefreshModelEventInfo( DesignElementHandle focus,
				NotificationEvent ev )
		{
			super( focus, ev );

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.report.designer.internal.ui.editors.schematic.editparts.GraphicsViewModelEventProcessor.ModelEventInfo#canAcceptModelEvent(org.eclipse.birt.report.model.api.DesignElementHandle,
		 *      org.eclipse.birt.report.model.api.activity.NotificationEvent)
		 */
		public boolean canAcceptModelEvent( ModelEventInfo info )
		{
			return info.getType( ) != NotificationEvent.CONTENT_EVENT;
		}
	}

	public boolean isOverdued( )
	{
		return getFactory( ).isDispose( );
	}
}

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

package org.eclipse.birt.report.designer.internal.ui.editors.schematic.editparts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.birt.report.designer.internal.ui.editors.parts.event.IFastConsumerProcessor;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.activity.NotificationEvent;
import org.eclipse.birt.report.model.api.command.ContentEvent;
import org.eclipse.birt.report.model.api.command.PropertyEvent;
import org.eclipse.birt.report.model.api.core.IDesignElement;

/**
 * Processor the model event for the ReportEditorWithPalette
 */
public class GraphicsViewModelEventProcessor extends AbstractModelEventProcessor implements IFastConsumerProcessor
{

	public static String CONTENT_EVENTTYPE = "Content event type";
	public static String EVENT_CONTENTS = "Event contents";

	/**
	 * @param factory
	 */
	public GraphicsViewModelEventProcessor( IModelEventFactory factory )
	{
		super(factory);
	}


	/**Filter the event.
	 * @param type
	 * @return
	 */
	protected boolean includeEventType( int type )
	{
		return type == NotificationEvent.CONTENT_EVENT
				| type == NotificationEvent.PROPERTY_EVENT
				| type == NotificationEvent.NAME_EVENT
				| type == NotificationEvent.STYLE_EVENT
				| type == NotificationEvent.EXTENSION_PROPERTY_DEFINITION_EVENT
				| type == NotificationEvent.LIBRARY_EVENT
				| type == NotificationEvent.THEME_EVENT
				| type == NotificationEvent.CONTENT_REPLACE_EVENT
				| type == NotificationEvent.TEMPLATE_TRANSFORM_EVENT
				| type == NotificationEvent.ELEMENT_LOCALIZE_EVENT
				| type == NotificationEvent.LIBRARY_RELOADED_EVENT
				| type == NotificationEvent.CSS_EVENT
				| type == NotificationEvent.CSS_RELOADED_EVENT;
	}
	/**Process the content model event
	 * ContentModelEventInfo
	 */
	protected static class ContentModelEventInfo extends ModelEventInfo
	{
		//private int contentActionType;
		private ContentModelEventInfo( DesignElementHandle focus, NotificationEvent ev )
		{	
			super(focus, ev);
			assert ev instanceof ContentEvent;
			setTarget( focus );
			setType( ev.getEventType( ) );
			setContentActionType( ((ContentEvent)ev).getAction( ) );
			addChangeContents( ((ContentEvent)ev).getContent( ) );
		}
		/* (non-Javadoc)
		 * @see org.eclipse.birt.report.designer.internal.ui.editors.schematic.editparts.GraphicsViewModelEventProcessor.ModelEventInfo#canAcceptModelEvent(org.eclipse.birt.report.model.api.DesignElementHandle, org.eclipse.birt.report.model.api.activity.NotificationEvent)
		 */
		public boolean canAcceptModelEvent( ModelEventInfo info )
		{
			if (getContentActionType( ) == ContentEvent.REMOVE && getChangeContents( ).contains( info.getTarget( ) ))
			{
				return true;
			}
			boolean bool = super.canAcceptModelEvent(info );
			if (!(info instanceof ContentModelEventInfo))
			{
				return false;
			}
			return bool & ((ContentModelEventInfo)info).getContentActionType( ) == ((ContentModelEventInfo)info).getContentActionType( );
		}
		
		/**
		 * @return
		 */
		public int getContentActionType( )
		{
			return ((Integer)getOtherInfo( ).get( CONTENT_EVENTTYPE )).intValue( );
		}
		
		/**
		 * @param contentActionType
		 */
		public void setContentActionType( int contentActionType )
		{
			getOtherInfo( ).put(CONTENT_EVENTTYPE ,new Integer(contentActionType));
		}
		
		public List getChangeContents()
		{
			return  (List)getOtherInfo( ).get( EVENT_CONTENTS );
		}
		
		public void addChangeContents(Object obj)
		{
			Map map = getOtherInfo( );
			
			if (obj instanceof IDesignElement)
			{
				obj = ((IDesignElement)obj).getHandle( getTarget( ).getModule( ) );
			}
			List list= (List)map.get( EVENT_CONTENTS );
			if (list == null)
			{
				list = new ArrayList();
				map.put(EVENT_CONTENTS, list);
			}
			list.add(obj);
		}
	}
	
	/**Creat the factor to ctreat the report runnable.
	 * @return
	 */
	protected ModelEventInfoFactory createModelEventInfoFactory()
	{
		return new GraphicsModelEventInfoFactory();
	}
	
	/**
	 * ModelEventInfoFactory
	 */
	protected static class GraphicsModelEventInfoFactory implements ModelEventInfoFactory
	{
		/**Creat the report runnable for the ReportEditorWithPalette.
		 * @param focus
		 * @param ev
		 * @return
		 */
		public ModelEventInfo createModelEventInfo(DesignElementHandle focus, NotificationEvent ev)
		{
			switch ( ev.getEventType( ) )
			{
				case NotificationEvent.CONTENT_EVENT :
				{
					return new ContentModelEventInfo(focus, ev);
				}
				default :
				{
					return new GraphicsViewModelEventInfo(focus, ev);
				}
			}
		}
	}
	private static class GraphicsViewModelEventInfo extends ModelEventInfo
	{

		public GraphicsViewModelEventInfo( DesignElementHandle focus, NotificationEvent ev )
		{
			super( focus, ev );
			if (ev instanceof PropertyEvent)
			{
				PropertyEvent proEvent = (PropertyEvent)ev;
				getOtherInfo( ).put(proEvent.getPropertyName( ), focus);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.birt.report.designer.internal.ui.editors.parts.event.IFastConsumerProcessor#isOverdued()
	 */
	public boolean isOverdued( )
	{
		return getFactory( ).isDispose( );
	}
	
	public void postElementEvent( )
	{
		try
		{
			if (getFactory( ) instanceof IAdvanceModelEventFactory)
			{
				((IAdvanceModelEventFactory)getFactory( )).eventDispathStart();
			}
			super.postElementEvent( );
		}
		finally
		{
			if (getFactory( ) instanceof IAdvanceModelEventFactory)
			{
				((IAdvanceModelEventFactory)getFactory( )).eventDispathEnd();
			}
		}
	}
}

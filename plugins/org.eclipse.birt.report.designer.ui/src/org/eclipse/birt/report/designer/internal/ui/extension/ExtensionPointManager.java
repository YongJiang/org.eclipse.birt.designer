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

package org.eclipse.birt.report.designer.internal.ui.extension;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.birt.report.designer.internal.ui.util.ExceptionHandler;
import org.eclipse.birt.report.designer.internal.ui.util.Policy;
import org.eclipse.birt.report.designer.ui.ReportPlatformUIImages;
import org.eclipse.birt.report.designer.ui.extensions.IExtensionConstants;
import org.eclipse.birt.report.designer.ui.extensions.IMenuBuilder;
import org.eclipse.birt.report.designer.ui.extensions.IProviderFactory;
import org.eclipse.birt.report.designer.util.DEUtil;
import org.eclipse.birt.report.model.api.DesignConfig;
import org.eclipse.birt.report.model.api.DesignEngine;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Assert;

/**
 * The ExtensionPoinyManager is utility class to retrieve IExtendedElementUI
 * extensions by model extension ID, or full list.It caches the information to
 * avoid reading the extensions each time.
 */

public class ExtensionPointManager
{

	private Map reportItemUIMap = null;

	private Map menuBuilderMap = null;

	private Map providerFactoryMap = null;

	private static ExtensionPointManager instance = null;

	private ExtensionPointManager( )
	{
	}

	public static ExtensionPointManager getInstance( )
	{
		if ( instance == null )
		{
			synchronized ( ExtensionPointManager.class )
			{
				if ( instance == null )
				{
					instance = new ExtensionPointManager( );
				}
			}
		}
		return instance;
	}

	/**
	 * Gets the list of all the extended element points.
	 * 
	 * @return Returns the list of all the extended element point
	 *         (ExtendedElementUIPoint).
	 */
	public List getExtendedElementPoints( )
	{
		return Arrays.asList( getReportItemUIMap( ).values( ).toArray( ) );
	}

	/**
	 * Gets the extended element point with the specified extension name.
	 * 
	 * @param extensionName
	 *            the extension name of the extended element
	 * 
	 * @return Returns the extended element point, or null if any problem exists
	 */
	public ExtendedElementUIPoint getExtendedElementPoint( String extensionName )
	{
		Assert.isLegal( extensionName != null );
		return (ExtendedElementUIPoint) getReportItemUIMap( ).get( extensionName );
	}

	/**
	 * Returns the menu builder for the given element.
	 * 
	 * @param elementName
	 *            the name of the element
	 * @return the menu builder, or null if there's no builder defined for the
	 *         element
	 */
	public IMenuBuilder getMenuBuilder( String elementName )
	{
		return (IMenuBuilder) getMenuBuilderMap( ).get( elementName );
	}

	/**
	 * Returns the provider factory for the given element.
	 * 
	 * @param elementName
	 *            the name of the element
	 * @return the provider factory, or null if there's no factory defined for
	 *         the element
	 */
	public IProviderFactory getProviderFactory( String elementName )
	{
		return (IProviderFactory) getProviderFactoryMap( ).get( elementName );
	}

	private Map getReportItemUIMap( )
	{
		synchronized ( this )
		{
			if ( reportItemUIMap == null )
			{
				reportItemUIMap = new HashMap( );

				for ( Iterator iter = getExtensionElements( IExtensionConstants.EXTENSION_REPORT_ITEM_UI ).iterator( ); iter.hasNext( ); )
				{
					IExtension extension = (IExtension) iter.next( );

					ExtendedElementUIPoint point = createReportItemUIPoint( extension );
					if ( point != null )
						reportItemUIMap.put( point.getExtensionName( ), point );
				}
			}
		}
		return reportItemUIMap;
	}

	private Map getMenuBuilderMap( )
	{
		synchronized ( this )
		{
			if ( menuBuilderMap == null )
			{
				menuBuilderMap = new HashMap( );

				for ( Iterator iter = getExtensionElements( IExtensionConstants.EXTENSION_MENU_BUILDERS ).iterator( ); iter.hasNext( ); )
				{
					IExtension extension = (IExtension) iter.next( );
					IConfigurationElement[] elements = extension.getConfigurationElements( );
					for ( int i = 0; i < elements.length; i++ )
					{
						if ( IExtensionConstants.ELEMENT_MENU_BUILDER.equals( elements[i].getName( ) ) )
						{
							String elementId = elements[i].getAttribute( IExtensionConstants.ATTRIBUTE_ELEMENT_NAME );
							try
							{
								Object menuBuilder = elements[i].createExecutableExtension( IExtensionConstants.ATTRIBUTE_CLASS );
								if (  menuBuilder instanceof IMenuBuilder )
								{
									menuBuilderMap.put( elementId, menuBuilder );
								}
							}
							catch ( CoreException e )
							{
							}
						}
					}
				}
			}
		}
		return menuBuilderMap;
	}

	private Map getProviderFactoryMap( )
	{
		synchronized ( this )
		{
			if ( providerFactoryMap == null )
			{
				providerFactoryMap = new HashMap( );

				for ( Iterator iter = getExtensionElements( IExtensionConstants.EXTENSION_PROVIDER_FACTORIES ).iterator( ); iter.hasNext( ); )
				{
					IExtension extension = (IExtension) iter.next( );
					IConfigurationElement[] elements = extension.getConfigurationElements( );
					for ( int i = 0; i < elements.length; i++ )
					{
						if ( IExtensionConstants.ELEMENT_PROVIDER_FACTORY.equals( elements[i].getName( ) ) )
						{
							String elementId = elements[i].getAttribute( IExtensionConstants.ATTRIBUTE_ELEMENT_NAME );
							try
							{
								Object factory = elements[i].createExecutableExtension( IExtensionConstants.ATTRIBUTE_CLASS );
								if ( factory instanceof  IProviderFactory )
								{
									providerFactoryMap.put( elementId, factory );
								}
							}
							catch ( CoreException e )
							{
							}
						}
					}
				}
			}
		}
		return providerFactoryMap;
	}

	private List getExtensionElements( String id )
	{
		IExtensionRegistry registry = Platform.getExtensionRegistry( );
		if ( registry == null )
		{// extension registry cannot be resolved
			return Collections.EMPTY_LIST;
		}
		IExtensionPoint extensionPoint = registry.getExtensionPoint( id );
		if ( extensionPoint == null )
		{// extension point cannot be resolved
			return Collections.EMPTY_LIST;
		}
		return Arrays.asList( extensionPoint.getExtensions( ) );
	}

	private ExtendedElementUIPoint createReportItemUIPoint( IExtension extension )
	{
		IConfigurationElement[] elements = extension.getConfigurationElements( );
		if ( elements != null && elements.length > 0 )
		{
			return loadElements( elements );
		}
		return null;
	}

	private ExtendedElementUIPoint loadElements(
			IConfigurationElement[] elements )
	{

		ExtendedElementUIPoint newPoint = new ExtendedElementUIPoint( );

		if ( elements != null )
		{
			try
			{
				for ( int i = 0; i < elements.length; i++ )
				{
					loadAttributes( newPoint, elements[i] );
				}
			}
			catch ( Exception e )
			{
				ExceptionHandler.handle( e );
				return null;
			}

		}
		if ( DEUtil.getMetaDataDictionary( )
				.getExtension( newPoint.getExtensionName( ) ) == null )
		{
			// Non-defined element. Ignore
			return null;
		}
		if ( Policy.TRACING_EXTENSION_LOAD )
		{
			System.out.println( "GUI Extesion Manager >> Loads " //$NON-NLS-1$
					+ newPoint.getExtensionName( ) );
		}
		return newPoint;
	}

	private void loadAttributes( ExtendedElementUIPoint newPoint,
			IConfigurationElement element ) throws CoreException
	{
		String elementName = element.getName( );
		if ( IExtensionConstants.ELEMENT_MODEL.equals( elementName ) )
		{
			String value = element.getAttribute( IExtensionConstants.ATTRIBUTE_EXTENSION_NAME );
			newPoint.setExtensionName( value );
		}
		else if ( IExtensionConstants.ELEMENT_REPORT_ITEM_FIGURE_UI.equals( elementName )
				|| IExtensionConstants.ELEMENT_REPORT_ITEM_IMAGE_UI.equals( elementName )
				|| IExtensionConstants.ELEMENT_REPORT_ITEM_LABEL_UI.equals( elementName ) )
		{
			String className = element.getAttribute( IExtensionConstants.ATTRIBUTE_CLASS );
			if ( className != null )
			{
				Object ui = element.createExecutableExtension( IExtensionConstants.ATTRIBUTE_CLASS );
				newPoint.setReportItemUI( new ExtendedUIAdapter( ui ) );
			}
		}
		else if ( IExtensionConstants.ELEMENT_BUILDER.equals( elementName ) )
		{
			loadClass( newPoint,
					element,
					IExtensionConstants.ATTRIBUTE_CLASS,
					IExtensionConstants.ELEMENT_BUILDER );
		}
		else if ( IExtensionConstants.ELEMENT_PROPERTYEDIT.equals( elementName ) )
		{
			loadClass( newPoint,
					element,
					IExtensionConstants.ATTRIBUTE_CLASS,
					IExtensionConstants.ELEMENT_PROPERTYEDIT );
		}

		else if ( IExtensionConstants.ELEMENT_PALETTE.equals( elementName ) )
		{
			loadIconAttribute( newPoint,
					element,
					IExtensionConstants.ATTRIBUTE_KEY_PALETTE_ICON,
					false );
			loadStringAttribute( newPoint,
					element,
					IExtensionConstants.ATTRIBUTE_PALETTE_CATEGORY );
			loadStringAttribute( newPoint,
					element,
					IExtensionConstants.ATTRIBUTE_PALETTE_CATEGORY_DISPLAYNAME );
		}
		else if ( IExtensionConstants.ELEMENT_EDITOR.equals( elementName ) )
		{
			loadBooleanAttribute( newPoint,
					element,
					IExtensionConstants.ATTRIBUTE_EDITOR_SHOW_IN_DESIGNER );
			loadBooleanAttribute( newPoint,
					element,
					IExtensionConstants.ATTRIBUTE_EDITOR_SHOW_IN_MASTERPAGE );
			loadBooleanAttribute( newPoint,
					element,
					IExtensionConstants.ATTRIBUTE_EDITOR_CAN_RESIZE );
		}
		else if ( IExtensionConstants.ELEMENT_OUTLINE.equals( elementName ) )
		{
			loadIconAttribute( newPoint,
					element,
					IExtensionConstants.ATTRIBUTE_KEY_OUTLINE_ICON,
					true );
		}
	}

	/**
	 * @param newPoint
	 *            the extension point instance
	 * @param element
	 *            the configuration element
	 * @param className
	 *            the name of the class attribute
	 */
	private void loadClass( ExtendedElementUIPoint newPoint,
			IConfigurationElement element, String className,
			String attributeName )
	{
		String value = element.getAttribute( className );
		if ( value != null )
		{
			try
			{
				newPoint.setClass( attributeName,
						element.createExecutableExtension( className ) );
			}
			catch ( CoreException e )
			{
			}
		}

	}

	private ImageDescriptor getImageDescriptor( IConfigurationElement element )
	{
		Assert.isLegal( element != null );
		IExtension extension = element.getDeclaringExtension( );
		String iconPath = element.getAttribute( IExtensionConstants.ATTRIBUTE_ICON );
		if ( iconPath == null )
		{
			return null;
		}
		URL path = Platform.getBundle( extension.getNamespace( ) )
				.getEntry( "/" ); //$NON-NLS-1$
		try
		{
			return ImageDescriptor.createFromURL( new URL( path, iconPath ) );
		}
		catch ( MalformedURLException e )
		{
		}
		return null;
	}

	private void loadStringAttribute( ExtendedElementUIPoint newPoint,
			IConfigurationElement element, String attributeName )
	{
		String value = element.getAttribute( attributeName );
		if ( value != null )
		{
			newPoint.setAttribute( attributeName, value );
		}

	}

	private void loadBooleanAttribute( ExtendedElementUIPoint newPoint,
			IConfigurationElement element, String attributeName )
	{
		String value = element.getAttribute( attributeName );
		if ( value != null )
		{
			newPoint.setAttribute( attributeName, Boolean.valueOf( value ) );
		}
	}

	private void loadIconAttribute( ExtendedElementUIPoint newPoint,
			IConfigurationElement element, String attributeName, boolean shared )
	{
		ImageDescriptor imageDescriptor = getImageDescriptor( element );
		if ( imageDescriptor != null )
		{
			if ( shared )
			{
				String symbolName = ReportPlatformUIImages.getIconSymbolName( newPoint.getExtensionName( ),
						attributeName );
				ReportPlatformUIImages.declareImage( symbolName,
						imageDescriptor );
			}
			newPoint.setAttribute( attributeName, imageDescriptor );
		}
	}

}
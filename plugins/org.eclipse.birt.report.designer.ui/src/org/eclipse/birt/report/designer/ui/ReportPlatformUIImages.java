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

package org.eclipse.birt.report.designer.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.birt.report.designer.internal.ui.extension.IExtensionConstants;
import org.eclipse.birt.report.designer.internal.ui.util.ExceptionHandler;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.ExtendedItemHandle;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

/**
 * This class provides the UI images for JRP platform.
 * 
 *  
 */
public class ReportPlatformUIImages
{

	private static ImageRegistry imageRegistry = null;

	/**
	 * Declares Common paths
	 */

	public final static String ICONS_PATH = "icons/";//$NON-NLS-1$

	static
	{
		initializeImageRegistry( );
	}

	/**
	 * Declares a workbench image given the path of the image file (relative to
	 * the workbench plug-in). This is a helper method that creates the image
	 * descriptor and passes it to the main <code>declareImage</code> method.
	 * 
	 * @param key
	 *            the symbolic name of the image
	 * @param path
	 *            the path of the image file relative to the base of the
	 *            workbench plug-ins install directory
	 */
	private final static void declareImage( String key, String path )
	{
		URL url = null;
		try
		{
			url = new URL( ReportPlugin.getDefault( )
					.getBundle( )
					.getEntry( "/" ), //$NON-NLS-1$
					path );
		}
		catch ( MalformedURLException e )
		{
			ExceptionHandler.handle( e );
		}
		ImageDescriptor desc = ImageDescriptor.createFromURL( url );
		declareImage( key, desc );
	}

	/**
	 * Declares all the workbench's images, including both "shared" ones and
	 * internal ones.
	 */
	private final static void declareImages( )
	{

		// common icons
		declareImage( IReportGraphicConstants.ICON_NEW_REPORT, ICONS_PATH
				+ "new_report.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_REPORT_FILE, ICONS_PATH
				+ "report.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_QUIK_EDIT, ICONS_PATH
				+ "quick_edit.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_REPORT_PERSPECTIVE,
				ICONS_PATH + "report_perspective.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_REPORT_PROJECT, ICONS_PATH
				+ "report_project.gif" ); //$NON-NLS-1$

		//element icons
		declareImage( IReportGraphicConstants.ICON_ELEMENT_CELL, ICONS_PATH
				+ "cell.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_ELEMENT_DATA, ICONS_PATH
				+ "data.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_ELEMENT_DATA_SET, ICONS_PATH
				+ "data_set.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_ELEMENT_DATA_SOURCE,
				ICONS_PATH + "data_source.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_ELEMENT_ODA_DATA_SET,
				ICONS_PATH + "data_set.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_ELEMENT_ODA_DATA_SOURCE,
				ICONS_PATH + "data_source.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_ELEMENT_GRID, ICONS_PATH
				+ "grid.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_ELEMENT_GROUP, ICONS_PATH
				+ "group.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_ELEMENT_IMAGE, ICONS_PATH
				+ "image.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_ELEMENT_LABEL, ICONS_PATH
				+ "label.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_ELEMENT_LINE, ICONS_PATH
				+ "line.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_ELEMENT_LIST, ICONS_PATH
				+ "list.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_ELEMENT_LIST_GROUP,
				ICONS_PATH + "list_group.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_ELEMNET_MASTERPAGE,
				ICONS_PATH + "master_page.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_ELEMENT_PARAMETER,
				ICONS_PATH + "parameter.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_ELEMENT_PARAMETER_GROUP,
				ICONS_PATH + "parameter_group.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_ELEMENT_ROW, ICONS_PATH
				+ "row.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_ELEMENT_SCALAR_PARAMETER,
				ICONS_PATH + "parameter.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_ELEMNET_SIMPLE_MASTERPAGE,
				ICONS_PATH + "master_page.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_ELEMENT_STYLE, ICONS_PATH
				+ "styles.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_ELEMENT_TABLE, ICONS_PATH
				+ "table.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_ELEMENT_TABLE_GROUP,
				ICONS_PATH + "table_group.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_ELEMENT_TEXT, ICONS_PATH
				+ "text.gif" ); //$NON-NLS-1$		

		//outline icons
		declareImage( IReportGraphicConstants.ICON_NODE_BODY, ICONS_PATH
				+ "body_icon.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_NODE_MASTERPAGES, ICONS_PATH
				+ "master_pages.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_NODE_STYLES, ICONS_PATH
				+ "styles.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_NODE_HEADER, ICONS_PATH
				+ "header.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_NODE_DETAILS, ICONS_PATH
				+ "details.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_NODE_FOOTER, ICONS_PATH
				+ "footer.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_NODE_GROUPS, ICONS_PATH
				+ "group.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_NODE_GROUP_HEADER,
				ICONS_PATH + "group_header.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_NODE_GROUP_FOOTER,
				ICONS_PATH + "group_footer.gif" ); //$NON-NLS-1$
		
		// layout icons
		declareImage( IReportGraphicConstants.ICON_LAYOUT_NORMAL, ICONS_PATH
				+ "normal_page.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_LAYOUT_MASTERPAGE, ICONS_PATH
				+ "master_page.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_LAYOUT_RULER, ICONS_PATH
				+ "show_rulers.gif" ); //$NON-NLS-1$

		// border icons
		declareImage( IReportGraphicConstants.ICON_BORDER_ALL, ICONS_PATH
				+ "borders_frame.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_BORDER_BOTTOM, ICONS_PATH
				+ "border_bottom.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_BORDER_TOP, ICONS_PATH
				+ "border_top.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_BORDER_LEFT, ICONS_PATH
				+ "border_left.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_BORDER_RIGHT, ICONS_PATH
				+ "border_right.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_BORDER_NOBORDER, ICONS_PATH
				+ "border_none.gif" ); //$NON-NLS-1$

		// chart icons
		declareImage( IReportGraphicConstants.ICON_CHART, ICONS_PATH
				+ "chart_icon.gif" ); //$NON-NLS-1$

		// missing image icons
		declareImage( IReportGraphicConstants.ICON_MISSING_IMG, ICONS_PATH
				+ "missing_image.gif" ); //$NON-NLS-1$

		// data explore icons
		declareImage( IReportGraphicConstants.ICON_DATA_EXPLORER_VIEW,
				ICONS_PATH + "data_explore_view.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_NODE_DATA_SETS, ICONS_PATH
				+ "data_set_folder.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_NODE_DATA_SOURCES,
				ICONS_PATH + "data_source_folder.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_NODE_PARAMETERS, ICONS_PATH
				+ "parameter_folder.gif" ); //$NON-NLS-1$

		//**********************************************************
		//expression icons
		declareImage( IReportGraphicConstants.ICON_EXPRESSION_FUNCTION,
				ICONS_PATH + "function.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_EXPRESSION_DATA_TABLE,
				ICONS_PATH + "data_table.gif" ); //$NON-NLS-1$
		declareImage( IReportGraphicConstants.ICON_DATA_COLUMN, ICONS_PATH
				+ "data_column.gif" ); //$NON-NLS-1$
		declareImage( IReportGraphicConstants.ICON_EXPRESSION_OPERATOR,
				ICONS_PATH + "operator.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_DEFINED_EXPRESSION,
				ICONS_PATH + "expression.gif" ); //$NON-NLS-1$
		declareImage( IReportGraphicConstants.ICON_EXPRESSION_CONSTANT,
				ICONS_PATH + "constant.gif" ); //$NON-NLS-1$

		//data wizards
		declareImage( IReportGraphicConstants.ICON_WIZARD_DATASOURCE,
				ICONS_PATH + "datasource_wizard.gif" ); //$NON-NLS-1$
		declareImage( IReportGraphicConstants.ICON_WIZARD_DATASET, ICONS_PATH
				+ "dataset_wizard.gif" ); //$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_WIZARDPAGE_DATASETSELECTION,
				ICONS_PATH + "dataset_wizard_table.gif" ); //$NON-NLS-1$

		/////////////////////attribute image
		declareImage( IReportGraphicConstants.ICON_ATTRIBUTE_FONT_WIDTH,
				ICONS_PATH + "bold.gif" ); //$NON-NLS-1$
		declareImage( IReportGraphicConstants.ICON_ATTRIBUTE_FONT_WIDTH
				+ IReportGraphicConstants.DIS, ICONS_PATH + "bold_disabled.gif" ); //$NON-NLS-1$
		declareImage( IReportGraphicConstants.ICON_ATTRIBUTE_FONT_STYLE,
				ICONS_PATH + "italic.gif" ); //$NON-NLS-1$
		declareImage( IReportGraphicConstants.ICON_ATTRIBUTE_FONT_STYLE
				+ IReportGraphicConstants.DIS, ICONS_PATH
				+ "italic_disabled.gif" ); //$NON-NLS-1$
		declareImage( IReportGraphicConstants.ICON_ATTRIBUTE_TEXT_UNDERLINE,
				ICONS_PATH + "underline.gif" ); //$NON-NLS-1$
		declareImage( IReportGraphicConstants.ICON_ATTRIBUTE_TEXT_UNDERLINE
				+ IReportGraphicConstants.DIS, ICONS_PATH
				+ "underline_disabled.gif" ); //$NON-NLS-1$
		declareImage( IReportGraphicConstants.ICON_ATTRIBUTE_TEXT_LINE_THROUGH,
				ICONS_PATH + "lineSthrough.gif" ); //$NON-NLS-1$
		declareImage( IReportGraphicConstants.ICON_ATTRIBUTE_TEXT_LINE_THROUGH
				+ IReportGraphicConstants.DIS, ICONS_PATH
				+ "lineSthrough_disabled.gif" ); //$NON-NLS-1$
		declareImage( IReportGraphicConstants.ICON_ATTRIBUTE_BORDER_NONE,
				ICONS_PATH + "border_none.gif" ); //$NON-NLS-1$
		declareImage( IReportGraphicConstants.ICON_ATTRIBUTE_BORDER_FRAME,
				ICONS_PATH + "border_frame.gif" ); //$NON-NLS-1$
		declareImage( IReportGraphicConstants.ICON_ATTRIBUTE_BORDER_LEFT,
				ICONS_PATH + "border_left.gif" ); //$NON-NLS-1$
		declareImage( IReportGraphicConstants.ICON_ATTRIBUTE_BORDER_RIGHT,
				ICONS_PATH + "border_right.gif" ); //$NON-NLS-1$
		declareImage( IReportGraphicConstants.ICON_ATTRIBUTE_BORDER_TOP,
				ICONS_PATH + "border_top.gif" ); //$NON-NLS-1$
		declareImage( IReportGraphicConstants.ICON_ATTRIBUTE_BORDER_BOTTOM,
				ICONS_PATH + "border_bottom.gif" ); //$NON-NLS-1$
		declareImage( IReportGraphicConstants.ICON_ATTRIBUTE_TEXT_ALIGN_CENTER,
				ICONS_PATH + "center_align.gif" ); //$NON-NLS-1$
		declareImage( IReportGraphicConstants.ICON_ATTRIBUTE_TEXT_ALIGN_CENTER
				+ IReportGraphicConstants.DIS, ICONS_PATH
				+ "center_align_disabled.gif" ); //$NON-NLS-1$
		declareImage( IReportGraphicConstants.ICON_ATTRIBUTE_TEXT_ALIGN_JUSTIFY,
				ICONS_PATH + "justified_align.gif" ); //$NON-NLS-1$
		declareImage( IReportGraphicConstants.ICON_ATTRIBUTE_TEXT_ALIGN_JUSTIFY
				+ IReportGraphicConstants.DIS, ICONS_PATH
				+ "justified_align_disabled.gif" ); //$NON-NLS-1$
		declareImage( IReportGraphicConstants.ICON_ATTRIBUTE_TEXT_ALIGN_LEFT,
				ICONS_PATH + "left_align.gif" ); //$NON-NLS-1$
		declareImage( IReportGraphicConstants.ICON_ATTRIBUTE_TEXT_ALIGN_LEFT
				+ IReportGraphicConstants.DIS, ICONS_PATH
				+ "left_align_disabled.gif" );//$NON-NLS-1$
		declareImage( IReportGraphicConstants.ICON_ATTRIBUTE_TEXT_ALIGN_RIGHT,
				ICONS_PATH + "right_align.gif" );//$NON-NLS-1$
		declareImage( IReportGraphicConstants.ICON_ATTRIBUTE_TEXT_ALIGN_RIGHT
				+ IReportGraphicConstants.DIS, ICONS_PATH
				+ "right_align_disabled.gif" );//$NON-NLS-1$

		declareImage( IReportGraphicConstants.ICON_ATTRIBUTE_TOP_MARGIN,
				ICONS_PATH + "top_margin.gif" ); //$NON-NLS-1$
		declareImage( IReportGraphicConstants.ICON_ATTRIBUTE_BOTTOM_MARGIN,
				ICONS_PATH + "bottom_margin.gif" ); //$NON-NLS-1$
		declareImage( IReportGraphicConstants.ICON_ATTRIBUTE_LEFT_MARGIN,
				ICONS_PATH + "left_margin.gif" ); //$NON-NLS-1$
		declareImage( IReportGraphicConstants.ICON_ATTRIBUTE_RIGHT_MARGIN,
				ICONS_PATH + "right_margin.gif" ); //$NON-NLS-1$

		//**********************************************************
		//Preview icons
		declareImage( IReportGraphicConstants.ICON_PREVIEW_PARAMETERS,
				ICONS_PATH + "parameters.gif" ); //$NON-NLS-1$
		declareImage( IReportGraphicConstants.ICON_PREVIEW_REFRESH, ICONS_PATH
				+ "refresh.gif" ); //$NON-NLS-1$
	}

	/**
	 * Declares a workbench image.
	 * <p>
	 * The workbench remembers the given image descriptor under the given name,
	 * and makes the image available to plug-ins via
	 * {@link org.eclipse.ui.ISharedImages IWorkbench.getSharedImages()}. For
	 * "shared" images, the workbench remembers the image descriptor and will
	 * manages the image object create from it; clients retrieve "shared" images
	 * via
	 * {@link org.eclipse.ui.ISharedImages#getImage ISharedImages.getImage()}.
	 * For the other, "non-shared" images, the workbench remembers only the
	 * image descriptor; clients retrieve the image descriptor via
	 * {@link org.eclipse.ui.ISharedImages#getImageDescriptor
	 * ISharedImages.getImageDescriptor()} and are entirely responsible for
	 * managing the image objects they create from it. (This is made confusing
	 * by the historical fact that the API interface is called "ISharedImages".)
	 * </p>
	 * 
	 * @param symbolicName
	 *            the symbolic name of the image
	 * @param descriptor
	 *            the image descriptor
	 */
	public static void declareImage( String symbolicName,
			ImageDescriptor descriptor )
	{
		imageRegistry.put( symbolicName, descriptor );
	}

	/**
	 * Returns the image stored in the workbench plugin's image registry under
	 * the given symbolic name. If there isn't any value associated with the
	 * name then <code>null</code> is returned.
	 * 
	 * The returned Image is managed by the workbench plugin's image registry.
	 * Callers of this method must not dispose the returned image.
	 * 
	 * This method is essentially a convenient short form of
	 * WorkbenchImages.getImageRegistry.get(symbolicName).
	 */
	public static Image getImage( String symbolicName )
	{
		return getImageRegistry( ).get( symbolicName );
	}

	/**
	 * Returns the image descriptor stored under the given symbolic name. If
	 * there isn't any value associated with the name then <code>null
	 * </code>
	 * is returned.
	 * 
	 * The class also "caches" commonly used images in the image registry. If
	 * you are looking for one of these common images it is recommended you use
	 * the getImage() method instead.
	 */
	public static ImageDescriptor getImageDescriptor( String symbolicName )
	{
		return imageRegistry.getDescriptor( symbolicName );
	}

	/**
	 * Gets the proper icon image for the given model
	 * 
	 * @param model
	 *            the given model
	 * @return Returns the proper icon image for the given model, or null if no
	 *         proper one exists
	 */

	public static Image getImage( Object model )
	{
		Image image = null;
		if ( model instanceof ExtendedItemHandle )
		{
			image = getImage( getIconSymbolName( ( (ExtendedItemHandle) model ).getExtensionName( ),
					IExtensionConstants.OUTLINE_ICON ) );
			if ( image == null )
			{
				image = getImage( IReportGraphicConstants.ICON_ELEMENT_EXTENDED_ITEM );
			}
		}
		else if ( model instanceof DesignElementHandle )
		{//the icon name for elements is just the same as the element name
			image = getImage( ( (DesignElementHandle) model ).getElement( )
					.getDefn( )
					.getName( ) );
		}
		return image;
	}

	/**
	 * Gets the proper icon image descriptor for the given model
	 * 
	 * @param model
	 *            the given model
	 * @return Returns the proper icon image descriptor for the given model, or
	 *         null if no proper one exists
	 */

	public static ImageDescriptor getImageDescriptor( Object model )
	{
		ImageDescriptor imageDescriptor = null;
		if ( model instanceof ExtendedItemHandle )
		{
			imageDescriptor = getImageDescriptor( getIconSymbolName( ( (ExtendedItemHandle) model ).getExtensionName( ),
					IExtensionConstants.OUTLINE_ICON ) );
			if ( imageDescriptor == null )
			{
				imageDescriptor = getImageDescriptor( IReportGraphicConstants.ICON_ELEMENT_EXTENDED_ITEM );
			}
		}
		else if ( model instanceof DesignElementHandle )
		{//the icon name for elements is just the same as the element name
			imageDescriptor = getImageDescriptor( ( (DesignElementHandle) model ).getElement( )
					.getDefn( )
					.getName( ) );
		}
		return imageDescriptor;
	}

	/**
	 * Returns the ImageRegistry.
	 */
	public static ImageRegistry getImageRegistry( )
	{
		return imageRegistry;
	}

	/**
	 * Initialize the image registry by declaring all of the required graphics.
	 * This involves creating JFace image descriptors describing how to
	 * create/find the image should it be needed. The image is not actually
	 * allocated until requested.
	 * 
	 * Prefix conventions Wizard Banners WIZBAN_ Preference Banners PREF_BAN_
	 * Property Page Banners PROPBAN_ Enable toolbar ETOOL_ Disable toolbar
	 * DTOOL_ Local enabled toolbar ELCL_ Local Disable toolbar DLCL_ Object
	 * large OBJL_ Object small OBJS_ View VIEW_ Product images PROD_ Misc
	 * images MISC_
	 * 
	 * Where are the images? The images (typically gifs) are found in the same
	 * location as this plug-in class. This may mean the same package directory
	 * as the package holding this class. The images are declared using
	 * this.getClass() to ensure they are looked up via this plug-in class.
	 *  
	 */
	public static ImageRegistry initializeImageRegistry( )
	{
		imageRegistry = ReportPlugin.getDefault( ).getImageRegistry( );
		declareImages( );
		return imageRegistry;
	}

	/**
	 * Gets the proper symbol name for the specified icon of the extended
	 * element
	 * 
	 * @param extensionName
	 *            the extension name of the element
	 * @param attrbuteName
	 *            the name of the attribute which defines an icon
	 * 
	 * @return Returns the symbol name generated
	 */
	public static String getIconSymbolName( String extensionName,
			String attrbuteName )
	{
		return attrbuteName + "." + extensionName; //$NON-NLS-1$
	}
}

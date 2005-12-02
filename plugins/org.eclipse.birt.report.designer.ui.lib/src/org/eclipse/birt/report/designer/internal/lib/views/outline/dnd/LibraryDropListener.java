/*******************************************************************************
 * Copyright (c) 2004 Actuate Corporation .
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.report.designer.internal.lib.views.outline.dnd;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.birt.report.designer.core.IReportElementConstants;
import org.eclipse.birt.report.designer.core.model.views.outline.ReportElementModel;
import org.eclipse.birt.report.designer.internal.lib.commands.SetCurrentEditModelCommand;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.extensions.GuiExtensionManager;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.tools.AbstractToolHandleExtends;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.tools.ReportCreationTool;
import org.eclipse.birt.report.designer.internal.ui.extension.ExtendedElementUIPoint;
import org.eclipse.birt.report.designer.internal.ui.extension.ExtensionPointManager;
import org.eclipse.birt.report.designer.internal.ui.palette.BasePaletteFactory;
import org.eclipse.birt.report.designer.internal.ui.palette.ReportElementFactory;
import org.eclipse.birt.report.designer.internal.ui.util.UIUtil;
import org.eclipse.birt.report.designer.internal.ui.views.outline.dnd.DesignerDropListener;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.LibraryHandle;
import org.eclipse.birt.report.model.api.ModuleHandle;
import org.eclipse.gef.EditDomain;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * Supports dropping elements to outline view.
 *  
 */
public class LibraryDropListener extends DesignerDropListener
{

	private static List supportList = new ArrayList( );

	static
	{
		supportList.add( IReportElementConstants.REPORT_ELEMENT_LABEL );
		supportList.add( IReportElementConstants.REPORT_ELEMENT_TEXT );
		supportList.add( IReportElementConstants.REPORT_ELEMENT_DATA );
		supportList.add( IReportElementConstants.REPORT_ELEMENT_GRID );
		supportList.add( IReportElementConstants.REPORT_ELEMENT_TABLE );
		supportList.add( IReportElementConstants.REPORT_ELEMENT_LIST );
		supportList.add( IReportElementConstants.REPORT_ELEMENT_IMAGE );
		supportList.add( IReportElementConstants.REPORT_ELEMENT_TEXTDATA );
		List exts = ExtensionPointManager.getInstance( )
				.getExtendedElementPoints( );

		if ( exts != null )
		{
			for ( Iterator itor = exts.iterator( ); itor.hasNext( ); )
			{
				ExtendedElementUIPoint point = (ExtendedElementUIPoint) itor
						.next( );
				if ( point != null )
				{
					supportList.add( GuiExtensionManager
							.getExtendedPalletTemplateName( point ) );
				}
			}
		}
	}

	/**
	 * @param viewer
	 */
	public LibraryDropListener( TreeViewer viewer )
	{
		super( viewer );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.dnd.DesignElementDropAdapter#validateTarget(java.lang.Object,
	 *      java.lang.Object)
	 */
	protected boolean validateTarget( Object target, Object transfer )
	{
		if (!validateSameParent(target, transfer))
		{
			return false;
		}
		
		boolean retValue = super.validateTarget( target, transfer );
		if ( !retValue )
		{
			retValue = isSupportPalletType( target, transfer )
					&& getCurrentLocation( ) == LOCATION_ON;
		}
		return retValue;
	}

	
	private boolean validateSameParent(Object targetObj, Object transferData)
	{
		if ( targetObj == null || transferData == null )
			return false;

		if (transferData instanceof String)
		{
			return true;
		}
		if (!(targetObj instanceof DesignElementHandle))
		{
			return false;
		}
		if ( transferData instanceof StructuredSelection )
		{
			return validateSameParent( targetObj,
					( (StructuredSelection) transferData ).toArray( ) );
		}
		else if ( transferData instanceof Object[] )
		{
			Object[] array = (Object[]) transferData;
			int len = array.length;
			for (int i=0; i<len; i++)
			{
				if (!validateSameParent(targetObj, array[i]))
				{
					return false;
				}
			}
			return true;
		}
		else if ( transferData instanceof DesignElementHandle )
		{
			return ((DesignElementHandle)targetObj).getContainer() == ((DesignElementHandle)transferData).getContainer();
		}
		
		return false;
	}
	
	private boolean isSupportPalletType( Object target, Object transfer )
	{
		boolean bool = false;
		if ( target instanceof ReportElementModel )
		{
			bool = ( (ReportElementModel) target ).getSlotId( ) == ModuleHandle.COMPONENT_SLOT;
		}
		else if ( target instanceof LibraryHandle )
		{
			bool = true;
		}
		return bool & supportList.indexOf( transfer ) >= 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.birt.report.designer.internal.ui.dnd.DesignElementDropAdapter#moveData(java.lang.Object, java.lang.Object)
	 */
	protected boolean moveData( Object transfer, Object target )
	{
		if ( isSupportPalletType( target, transfer ) )
		{
			AbstractToolHandleExtends pre = BasePaletteFactory.getAbstractToolHandleExtendsFromPaletteName(transfer);
			ReportCreationTool tool = new ReportCreationTool(
					new ReportElementFactory(
							transfer ), pre );
			
			final EditDomain domain = UIUtil.getLayoutEditPartViewer( )
					.getEditDomain( );
			tool.setEditDomain( domain );
			tool.setViewer( UIUtil.getLayoutEditPartViewer( ) );
			tool.performCreation( UIUtil.getCurrentEditPart( ) );
			SetCurrentEditModelCommand command = new SetCurrentEditModelCommand(tool.getNewObjectFromRequest());
			command.execute();
			return true;
		}
		return super.moveData( transfer, target );
	}
}
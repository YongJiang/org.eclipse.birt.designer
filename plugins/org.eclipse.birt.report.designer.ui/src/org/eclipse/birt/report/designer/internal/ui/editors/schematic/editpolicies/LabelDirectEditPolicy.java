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

package org.eclipse.birt.report.designer.internal.ui.editors.schematic.editpolicies;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.birt.report.designer.core.commands.SetPropertyCommand;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.editparts.LabelEditPart;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.figures.LabelFigure;
import org.eclipse.birt.report.designer.util.DEUtil;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.DirectEditPolicy;
import org.eclipse.gef.requests.DirectEditRequest;

/**
 * An EditPolicy for use with container editparts. This policy can be used to
 * contribute commands to direct edit.
 * 
 *  
 */
public class LabelDirectEditPolicy extends DirectEditPolicy
{

	/**
	 * @see DirectEditPolicy#getDirectEditCommand(DirectEditRequest)
	 */
	protected Command getDirectEditCommand( DirectEditRequest edit )
	{
		String labelText = (String) edit.getCellEditor( ).getValue( );
		Map extendsData = new HashMap( );
		extendsData.put( DEUtil.ELEMENT_LABELCONTENT_PROPERTY, labelText );
		LabelEditPart label = (LabelEditPart) getHost( );
		SetPropertyCommand command = new SetPropertyCommand( label.getModel( ),
				extendsData );
		return command;
	}

	/**
	 * @see DirectEditPolicy#showCurrentEditValue(DirectEditRequest)
	 */
	protected void showCurrentEditValue( DirectEditRequest request )
	{
		String value = (String) request.getCellEditor( ).getValue( );
		( (LabelFigure) getHostFigure( ) ).setText( value );
		//hack to prevent async layout from placing the cell editor twice.
		getHostFigure( ).getUpdateManager( ).performUpdate( );

	}
	
	public boolean understandsRequest(Request request) {
		if (RequestConstants.REQ_DIRECT_EDIT.equals(request.getType())
				|| RequestConstants.REQ_OPEN.equals(request.getType()))
			return true;
		return super.understandsRequest(request);
	}

}
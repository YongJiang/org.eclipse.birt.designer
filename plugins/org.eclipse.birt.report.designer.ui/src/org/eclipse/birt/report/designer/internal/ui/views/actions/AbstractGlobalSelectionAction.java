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

package org.eclipse.birt.report.designer.internal.ui.views.actions;

import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 *  Abstract class for all global selection action
 */

public abstract class AbstractGlobalSelectionAction extends SelectionAction
{

	protected AbstractGlobalSelectionAction( ISelectionProvider provider, String id )
	{
		super( null );
		Assert.isNotNull( provider );
		setId( id );
		setSelectionProvider( provider );
		provider.addSelectionChangedListener( new ISelectionChangedListener( ) {

			public void selectionChanged( SelectionChangedEvent event )
			{
				update( );
			}
		} );
		setLazyEnablementCalculation( true );
	}

	protected ISelection getSelection( )
	{
		ISelection selected = super.getSelection( );
		if ( selected == null )
		{
			selected = StructuredSelection.EMPTY;
		}
		return selected;
	}

}
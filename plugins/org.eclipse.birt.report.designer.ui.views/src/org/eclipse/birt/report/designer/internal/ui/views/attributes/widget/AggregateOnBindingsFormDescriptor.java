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

package org.eclipse.birt.report.designer.internal.ui.views.attributes.widget;

import org.eclipse.birt.report.designer.internal.ui.swt.custom.FormWidgetFactory;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.page.WidgetUtil;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.provider.AggregateOnBindingsFormHandleProvider;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.provider.IDescriptorProvider;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.designer.util.DEUtil;
import org.eclipse.birt.report.model.api.ReportElementHandle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Data set binding page.
 */

public class AggregateOnBindingsFormDescriptor extends DataSetColumnBindingsFormDescriptor
{

	private AggregateOnBindingsFormHandleProvider provider;

	public AggregateOnBindingsFormDescriptor( boolean formStyle )
	{
		super( formStyle );
		super.setStyle( FormPropertyDescriptor.FULL_FUNCTION );
		super.setButtonWithDialog( false );
	}

	public void setDescriptorProvider( IDescriptorProvider provider )
	{
		super.setDescriptorProvider( provider );
		if ( provider instanceof AggregateOnBindingsFormHandleProvider )
			this.provider = (AggregateOnBindingsFormHandleProvider) provider;
	}

	protected Button btnAddAggregateOn;

	public Control createControl( Composite parent )
	{
		Control control = super.createControl( parent );

		if ( isFormStyle( ) )
			btnAddAggregateOn = FormWidgetFactory.getInstance( )
					.createButton( (Composite) control, "", SWT.PUSH ); //$NON-NLS-1$
		else
			btnAddAggregateOn = new Button( (Composite) control, SWT.BORDER );

		if ( bAddWithDialog )
			btnAddAggregateOn.setText( Messages.getString( "FormPage.Button.AddWithDialog.AggregateOn" ) ); //$NON-NLS-1$
		else
			btnAddAggregateOn.setText( Messages.getString( "FormPage.Button.Add.AggregateOn" ) ); //$NON-NLS-1$
		btnAddAggregateOn.addSelectionListener( new SelectionAdapter( ) {

			public void widgetSelected( SelectionEvent e )
			{
				handleAddAggregateOnSelectEvent( );
			}
		} );
//		btnAddAggregateOn.setEnabled( false );

		fullLayout( );

		return control;
	}

	protected void handleAddAggregateOnSelectEvent( )
	{
		int pos = table.getSelectionIndex( );
		try
		{
			provider.addAggregateOn( pos );
		}
		catch ( Exception e )
		{
			WidgetUtil.processError( btnAddAggregateOn.getShell( ), e );
			return;
		}

		table.setSelection( table.getItemCount( ) - 1 );
		updateArraw( );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.views.attributes.page.FormPage#fullLayout()
	 */
	protected void fullLayout( )
	{
		super.fullLayout( );

		if ( btnAddAggregateOn != null )
		{
			FormData data = new FormData( );
			data.top = new FormAttachment( btnAdd, 0, SWT.BOTTOM );
			data.left = new FormAttachment( btnAdd, 0, SWT.LEFT );
			data.width = Math.max( btnWidth,
					btnAddAggregateOn.computeSize( SWT.DEFAULT,
							SWT.DEFAULT,
							true ).x );
			btnAddAggregateOn.setLayoutData( data );

			data = new FormData( );
			data.top = new FormAttachment( btnAddAggregateOn, 0, SWT.BOTTOM );
			data.left = new FormAttachment( btnAddAggregateOn, 0, SWT.LEFT );
			data.width = Math.max( btnWidth, btnEdit.computeSize( SWT.DEFAULT,
					SWT.DEFAULT,
					true ).x );
			btnEdit.setLayoutData( data );
		}
		FormData data = new FormData( );
		data.top = new FormAttachment( btnEdit, 0, SWT.BOTTOM );
		data.left = new FormAttachment( btnEdit, 0, SWT.LEFT );
		data.width = Math.max( 60, btnDel.computeSize( SWT.DEFAULT,
				SWT.DEFAULT,
				true ).x );
		btnDel.setLayoutData( data );
	}

	public void setInput( Object object )
	{
		super.setInput( object );
		if ( DEUtil.getInputSize( object ) > 0 )
		{
			Object element = DEUtil.getInputFirstElement( object );
			setBindingObject( (ReportElementHandle) element );
		}
		if(provider.isEnable( ) && provider.isEditable( ))btnAddAggregateOn.setEnabled( true );
		else btnAddAggregateOn.setEnabled( false );
	}

	private void setBindingObject( ReportElementHandle bindingObject )
	{
		provider.setBindingObject( bindingObject );
	}
	
	protected void handleRefreshSelectEvent( )
	{
		provider.generateAllBindingColumns( );
	}
}
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

import org.eclipse.birt.report.designer.internal.ui.dialogs.expression.ExpressionButton;
import org.eclipse.birt.report.designer.internal.ui.swt.custom.FormWidgetFactory;
import org.eclipse.birt.report.designer.internal.ui.util.ExpressionButtonUtil;
import org.eclipse.birt.report.designer.internal.ui.util.UIUtil;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.page.WidgetUtil;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.provider.ExpressionPropertyDescriptorProvider;
import org.eclipse.birt.report.designer.ui.dialogs.ExpressionProvider;
import org.eclipse.birt.report.model.api.Expression;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.eclipse.birt.report.model.api.elements.structures.TOC;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * Property Descriptor for value expression property.
 */

public class ExpressionPropertyDescriptor extends PropertyDescriptor
{

	protected Text text;

	protected Button button;

	private Composite containerPane;

	private Expression deValue;

	private String newValue;

	/**
	 * The constructor.
	 */
	public ExpressionPropertyDescriptor( boolean formStyle )
	{
		setFormStyle( formStyle );
	}

	public Text getTextControl( )
	{
		return text;
	}

	public void setInput( Object handle )
	{
		this.input = handle;
		getDescriptorProvider( ).setInput( input );
	}

	/**
	 * After selection changed, re-sets UI data.
	 */
	public void load( )
	{
		Object value = getDescriptorProvider( ).load( );
		if ( value == null || value instanceof Expression )
		{
			deValue = (Expression) value;

			String stringValue = deValue == null
					|| deValue.getExpression( ) == null ? "" : (String) deValue.getExpression( ); //$NON-NLS-1$
			text.setText( stringValue );

			text.setData( ExpressionButtonUtil.EXPR_TYPE,
					deValue == null || deValue.getType( ) == null ? UIUtil.getDefaultScriptType( )
							: (String) deValue.getType( ) );

			Object button = text.getData( ExpressionButtonUtil.EXPR_BUTTON );
			if ( button instanceof ExpressionButton )
			{
				( (ExpressionButton) button ).refresh( );
			}

			if ( getDescriptorProvider( ) instanceof ExpressionPropertyDescriptorProvider )
			{
				boolean readOnly = ( (ExpressionPropertyDescriptorProvider) getDescriptorProvider( ) ).isReadOnly( );
				boolean enable = ( (ExpressionPropertyDescriptorProvider) getDescriptorProvider( ) ).isEnable( );
				text.setEnabled( enable && ( !readOnly ) );

				if ( button instanceof ExpressionButton )
				{
					( (ExpressionButton) button ).refresh( );
					( (ExpressionButton) button ).setEnabled( enable
							&& ( !readOnly ) );
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.birt.report.designer.internal.ui.views.attributes.widget.
	 * PropertyDescriptor#getControl()
	 */
	public Control getControl( )
	{
		return containerPane;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.birt.report.designer.ui.extensions.IPropertyDescriptor#
	 * createControl(org.eclipse.swt.widgets.Composite)
	 */
	public Control createControl( Composite parent )
	{
		containerPane = new Composite( parent, SWT.NONE );

		GridLayout layout = new GridLayout( );
		layout.numColumns = 2;
		containerPane.setLayout( layout );
		if ( isFormStyle( ) )
			text = FormWidgetFactory.getInstance( ).createText( containerPane,
					"", SWT.MULTI | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL ); //$NON-NLS-1$
		else
			text = new Text( containerPane, SWT.MULTI
					| SWT.WRAP
					| SWT.BORDER
					| SWT.H_SCROLL
					| SWT.V_SCROLL );
		text.setLayoutData( new GridData( GridData.FILL_BOTH ) );
		// text.addSelectionListener( new SelectionAdapter( ) {
		//
		// public void widgetDefaultSelected( SelectionEvent e )
		// {
		// handleSelectEvent( );
		// }
		// } );
		text.addFocusListener( new FocusListener( ) {

			public void focusGained( FocusEvent e )
			{

			}

			public void focusLost( FocusEvent e )
			{
				handleFocusLostEvent( );
			}
		} );

		if ( getDescriptorProvider( ) instanceof ExpressionPropertyDescriptorProvider )
		{
			Listener listener = new Listener( ) {

				public void handleEvent( Event event )
				{
					if ( event.data instanceof String[] )
						newValue = ( (String[]) event.data )[0];
					processAction( );
				}

			};
			ExpressionProvider provider = ( (ExpressionPropertyDescriptorProvider) getDescriptorProvider( ) ).getExpressionProvider( );
			ExpressionButtonUtil.createExpressionButton( containerPane,
					text,
					provider,
					listener,
					false,
					isFormStyle( ) ? SWT.FLAT : SWT.PUSH );
		}

		return containerPane;
	}

	protected void handleSelectEvent( )
	{
		newValue = text.getText( );
		processAction( );
	}

	protected void handleFocusLostEvent( )
	{
		newValue = text.getText( );
		processAction( );
	}

	/**
	 * Processes the save action.
	 */
	private void processAction( )
	{
		String value = newValue;
		if ( value != null && value.length( ) == 0 )
		{
			value = null;
		}

		try
		{
			if ( value == null && deValue != null )
				save( value );
			else
			{
				if ( text.getText( ).trim( ).length( ) == 0 )
					save( null );
				else
				{
					Expression expression = new Expression( text.getText( )
							.trim( ),
							(String) text.getData( ExpressionButtonUtil.EXPR_TYPE ) );
					save( expression );
				}
			}
		}
		catch ( SemanticException e1 )
		{
			text.setText( UIUtil.convertToGUIString( deValue == null ? null
					: deValue.getStringExpression( ) ) );
			WidgetUtil.processError( text.getShell( ), e1 );

		}

	}

	public void setText( String text )
	{
		this.text.setText( text );
	}

	public void save( Object obj ) throws SemanticException
	{
		getDescriptorProvider( ).save( obj );

	}

	public void setHidden( boolean isHidden )
	{
		WidgetUtil.setExcludeGridData( containerPane, isHidden );
	}

	public void setVisible( boolean isVisible )
	{
		containerPane.setVisible( isVisible );
	}
}
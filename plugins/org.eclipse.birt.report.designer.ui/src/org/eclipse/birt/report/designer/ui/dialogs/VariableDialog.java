/*******************************************************************************
 * Copyright (c) 2008 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.report.designer.ui.dialogs;

import org.eclipse.birt.report.designer.internal.ui.dialogs.BaseTitleAreaDialog;
import org.eclipse.birt.report.designer.internal.ui.dialogs.expression.ExpressionButton;
import org.eclipse.birt.report.designer.internal.ui.dialogs.expression.IExpressionHelper;
import org.eclipse.birt.report.designer.internal.ui.util.ExceptionHandler;
import org.eclipse.birt.report.designer.internal.ui.util.UIUtil;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.designer.ui.expressions.ExpressionFilter;
import org.eclipse.birt.report.designer.ui.newelement.DesignElementFactory;
import org.eclipse.birt.report.designer.util.DEUtil;
import org.eclipse.birt.report.model.api.Expression;
import org.eclipse.birt.report.model.api.ExpressionHandle;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.VariableElementHandle;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.eclipse.birt.report.model.api.elements.DesignChoiceConstants;
import org.eclipse.birt.report.model.api.metadata.IChoice;
import org.eclipse.birt.report.model.api.metadata.IChoiceSet;
import org.eclipse.birt.report.model.elements.interfaces.IReportDesignModel;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * 
 */

public class VariableDialog extends BaseTitleAreaDialog
{

	private ReportDesignHandle designHandle;
	private VariableElementHandle variable;

	private Text nameTxt;
	private Text expressionTxt;
	private Button reportRadio;
	private Button pageRadio;

	private static final String EXPR_TYPE = "exprType";//$NON-NLS-1$
	private IExpressionHelper helper;
	private Combo dataTypeCombo;
	private ExpressionButton expressionButton;

	public VariableDialog( String title, ReportDesignHandle designHandle,
			VariableElementHandle variable )
	{
		super( UIUtil.getDefaultShell( ) );
		this.title = title;
		this.designHandle = designHandle;
		this.variable = variable;
		this.helper = new IExpressionHelper( ) {

			public String getExpression( )
			{
				if ( expressionTxt != null )
					return expressionTxt.getText( );
				else
					return ""; //$NON-NLS-1$
			}

			public void setExpression( String expression )
			{
				if ( expressionTxt != null )
					expressionTxt.setText( expression );
			}

			public void notifyExpressionChangeEvent( String oldExpression,
					String newExpression )
			{
			}

			public IExpressionProvider getExpressionProvider( )
			{
				ExpressionProvider provider = new ExpressionProvider( VariableDialog.this.variable );
				provider.addFilter( new ExpressionFilter( ) {

					@Override
					public boolean select( Object parentElement, Object element )
					{
						return !element.equals( VariableDialog.this.variable );
					}

				} );
				return provider;
			}

			public String getExpressionType( )
			{
				return (String) expressionTxt.getData( EXPR_TYPE );
			}

			public void setExpressionType( String exprType )
			{
				expressionTxt.setData( EXPR_TYPE, exprType );
			}

		};

	}

	@Override
	protected Control createDialogArea( Composite parent )
	{
		Composite content = new Composite( parent, SWT.NONE );
		content.setLayoutData( new GridData( GridData.FILL_BOTH ) );
		content.setLayout( GridLayoutFactory.swtDefaults( )
				.numColumns( 3 )
				.create( ) );

		new Label( content, SWT.NONE ).setText( Messages.getString( "VariableDialog.VariableType" ) ); //$NON-NLS-1$

		Composite typeChoices = new Composite( content, SWT.NONE );
		typeChoices.setLayout( GridLayoutFactory.swtDefaults( )
				.numColumns( 2 )
				.create( ) );
		reportRadio = new Button( typeChoices, SWT.RADIO );
		reportRadio.setText( Messages.getString( "VariableDialog.ReportVariable" ) ); //$NON-NLS-1$
		pageRadio = new Button( typeChoices, SWT.RADIO );
		pageRadio.setText( Messages.getString( "VariableDialog.PageVariable" ) ); //$NON-NLS-1$
		new Label( content, SWT.NONE );

		new Label( content, SWT.NONE ).setText( Messages.getString( "VariableDialog.Name" ) ); //$NON-NLS-1$

		nameTxt = new Text( content, SWT.BORDER );
		nameTxt.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
		nameTxt.addModifyListener( new ModifyListener( ) {

			public void modifyText( ModifyEvent e )
			{
				validate( );
			}
		} );
		// dummy
		new Label( content, SWT.NONE );

		new Label( content, SWT.NONE ).setText( Messages.getString( "VariableDialog.DataType" ) ); //$NON-NLS-1$

		dataTypeCombo = new Combo( content, SWT.READ_ONLY );
		dataTypeCombo.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
		new Label( content, SWT.NONE );

		new Label( content, SWT.NONE ).setText( Messages.getString( "VariableDialog.DefaultValue" ) ); //$NON-NLS-1$
		expressionTxt = new Text( content, SWT.BORDER );
		expressionTxt.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );

		expressionButton = UIUtil.createExpressionButton( content,
				SWT.PUSH );
		expressionButton.setExpressionHelper( helper );

		return content;
	}

	@Override
	protected boolean initDialog( )
	{
		IChoiceSet datatypes = DEUtil.getMetaDataDictionary( )
				.getChoiceSet( DesignChoiceConstants.CHOICE_PARAM_TYPE );
		for ( IChoice choice : datatypes.getChoices( ) )
		{
			dataTypeCombo.add( choice.getDisplayName( ) );
		}
		if ( this.variable != null )
		{
			this.nameTxt.setText( this.variable.getName( ) );
			if ( this.variable.getType( ) == null
					|| this.variable.getType( )
							.equals( DesignChoiceConstants.VARIABLE_TYPE_REPORT ) )
				this.reportRadio.setSelection( true );
			else
				this.pageRadio.setSelection( true );
			if ( this.variable.getDataType( ) != null )
			{
				String displayName = getDisplayNameByDataType( this.variable.getDataType( ),
						datatypes );
				for ( int i = 0; i < dataTypeCombo.getItemCount( ); i++ )
				{
					if ( dataTypeCombo.getItem( i ).equals( displayName ) )
					{
						dataTypeCombo.select( i );
						break;
					}
				}
			}
			if ( this.variable.getValue( ) != null )
				this.expressionTxt.setText( this.variable.getValue( ) );
		}
		else
		{
			this.reportRadio.setSelection( true );
			this.dataTypeCombo.select( 0 );
		}
		validate( );
		return true;
	}

	@Override
	protected void okPressed( )
	{
		if ( this.variable == null )
		{
			this.variable = DesignElementFactory.getInstance( this.designHandle )
					.newVariableElement( this.nameTxt.getText( ) );
			try
			{
				this.designHandle.add( IReportDesignModel.PAGE_VARIABLES_PROP,
						this.variable );
			}
			catch ( SemanticException e )
			{
				ExceptionHandler.handle( e );
			}
		}
		try
		{
			this.variable.setVariableName( this.nameTxt.getText( ) );
			if ( this.reportRadio.getSelection( ) )
				this.variable.setType( DesignChoiceConstants.VARIABLE_TYPE_REPORT );
			else
				this.variable.setType( DesignChoiceConstants.VARIABLE_TYPE_PAGE );
			this.variable.setDataType( getDataTypeByDisplayName( this.dataTypeCombo.getText( ),
					DEUtil.getMetaDataDictionary( )
							.getChoiceSet( DesignChoiceConstants.CHOICE_PARAM_TYPE ) ) );
			Expression expression = new Expression( this.expressionTxt.getText( ),
					(String) this.expressionTxt.getData( EXPR_TYPE ) );
			this.variable.setValue( this.expressionTxt.getText( ) );
			this.variable.setExpressionProperty( VariableElementHandle.VALUE_PROP, expression );
		}
		catch ( SemanticException e )
		{
			ExceptionHandler.handle( e );
		}
		super.okPressed( );
	}

	private String getDataTypeByDisplayName( String text, IChoiceSet datatypes )
	{
		for ( IChoice choice : datatypes.getChoices( ) )
		{
			if ( choice.getDisplayName( ).equals( text ) )
				return choice.getName( );
		}
		return null;
	}

	private String getDisplayNameByDataType( String text, IChoiceSet datatypes )
	{
		for ( IChoice choice : datatypes.getChoices( ) )
		{
			if ( choice.getValue( ).equals( text ) )
				return choice.getDisplayName( );
		}
		return null;
	}

	private void validate( )
	{
		if ( this.nameTxt.getText( ) == null
				|| this.nameTxt.getText( ).equals( "" ) ) //$NON-NLS-1$
		{
			getOkButton( ).setEnabled( false );
		}
		else
		{
			getOkButton( ).setEnabled( true );
		}
	}

}
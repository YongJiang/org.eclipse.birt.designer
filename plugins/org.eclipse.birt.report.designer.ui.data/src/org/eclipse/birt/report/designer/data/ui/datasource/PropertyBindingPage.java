/*******************************************************************************
 * Copyright (c) 2004, 2010 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.report.designer.data.ui.datasource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.birt.report.designer.data.ui.property.AbstractDescriptionPropertyPage;
import org.eclipse.birt.report.designer.data.ui.util.DataUIConstants;
import org.eclipse.birt.report.designer.data.ui.util.IHelpConstants;
import org.eclipse.birt.report.designer.data.ui.util.Utility;
import org.eclipse.birt.report.designer.internal.ui.dialogs.expression.ExpressionButton;
import org.eclipse.birt.report.designer.internal.ui.dialogs.expression.ExpressionButtonProvider;
import org.eclipse.birt.report.designer.internal.ui.util.ExceptionHandler;
import org.eclipse.birt.report.designer.internal.ui.util.ExpressionButtonUtil;
import org.eclipse.birt.report.designer.internal.ui.util.UIUtil;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.designer.ui.dialogs.ExpressionProvider;
import org.eclipse.birt.report.model.api.DataSetHandle;
import org.eclipse.birt.report.model.api.DataSourceHandle;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.Expression;
import org.eclipse.birt.report.model.api.ExpressionType;
import org.eclipse.birt.report.model.api.OdaDataSetHandle;
import org.eclipse.birt.report.model.api.OdaDataSourceHandle;
import org.eclipse.birt.report.model.api.ReportElementHandle;
import org.eclipse.birt.report.model.api.metadata.IElementDefn;
import org.eclipse.birt.report.model.api.metadata.IElementPropertyDefn;
import org.eclipse.birt.report.model.api.metadata.IPropertyDefn;
import org.eclipse.birt.report.model.elements.interfaces.IDesignElementModel;
import org.eclipse.datatools.help.HelpUtil;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Property page to define the data source property binding. This page include
 * an editor tab for ODA data source properties. The tab shows all data source
 * properties defined by the extension.
 */
public class PropertyBindingPage extends AbstractDescriptionPropertyPage
{

	private IDesignElementModel ds;

	private List propList = new ArrayList( );

	/**
	 * the binding properties's value list, this list contains all binding
	 * property
	 */
	private List bindingValue = new ArrayList( );

	/**
	 * the label list used in composite
	 */
	private List nameLabelList = new ArrayList( );
	/**
	 * the text list used in composite
	 */
	private List<Text> propertyTextList = new ArrayList( );

	// This is a temporary property for data set property binding
	private final String QUERYTEXT = "queryText"; //$NON-NLS-1$

	private static Logger logger = Logger.getLogger( PropertyBindingPage.class.getName( ) );
	
	private ReportElementHandle handle;

	/**
	 * the content
	 */
	public Control createContents( Composite parent )
	{
		// property binding initialize
		initPropertyBinding( );

		int size = propList.size( );

		Composite composite = new Composite( parent, SWT.NONE );
		composite.setLayout( new GridLayout( 3, false ) );
		GridData gridData = new GridData( GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_FILL );

		composite.setLayoutData( gridData );

		Label nameLabel;
		Text propertyText = null;
		
		// according the binding properties's size, dynamically add the
		// label,text,button group list to composite
		for ( int i = 0; i < size; i++ )
		{
			nameLabel = new Label( composite, SWT.NONE );
			String bindingName = ""; //$NON-NLS-1$
			boolean isEncryptable = false;
			if ( propList.get( i ) instanceof String[] )
			{
				bindingName = ( (String[]) propList.get( i ) )[0];
				nameLabel.setText( ( (String[]) propList.get( i ) )[1]
						+ Messages.getString( "PropertyBindingPage.label.colon" ) ); //$NON-NLS-1$
			}
			else if ( propList.get( i ) instanceof IPropertyDefn )
			{
				IPropertyDefn propDefn = (IPropertyDefn) propList.get( i );
				bindingName = propDefn.getName( );
				nameLabel.setText( propDefn.getDisplayName( )
						+ Messages.getString( "PropertyBindingPage.label.colon" ) ); //$NON-NLS-1$
				isEncryptable = propDefn.isEncryptable( );
			}
			nameLabelList.add( nameLabel );

			GridData data = new GridData( GridData.FILL_HORIZONTAL );
			if ( QUERYTEXT.equals( bindingName ) )
			{
				propertyText = new Text( composite, SWT.BORDER
						| SWT.V_SCROLL | SWT.H_SCROLL );
				data.heightHint = 100;
			}
			else if ( isEncryptable )
			{
				propertyText = new Text( composite, SWT.BORDER );
				if ( ds instanceof DesignElementHandle )
				{
					Expression expr = ( (DesignElementHandle) ds ).getPropertyBindingExpression( bindingName );
					if ( expr != null
							&& ExpressionType.CONSTANT.equals( expr.getType( ) ) )
					{
						Text dummy = new Text( composite, SWT.BORDER
								| SWT.PASSWORD );
						propertyText.setEchoChar( dummy.getEchoChar( ) );
						dummy.dispose( );
					}
				}
			}
			else
				propertyText = new Text( composite, SWT.BORDER );
			propertyText.setLayoutData( data );
			propertyText.setText( (String) bindingValue.get( i ) == null ? "" //$NON-NLS-1$
					: (String) bindingValue.get( i ) );
			propertyTextList.add( propertyText );

			if ( ds instanceof OdaDataSourceHandle )
			{
				handle = (OdaDataSourceHandle)ds;
				OdaDataSourceHandle odsh = (OdaDataSourceHandle) ds;
				String contextId = getDynamicContextId( odsh.getExtensionID( ),
						odsh.getExtensionID( ) ); 
				if ( contextId != null )
				{
					//contextId is provided thru o.e.datatools.help extension point
					Utility.setSystemHelp( composite, contextId );
				}
				else
				{
					// '.' char will interrupt help system
					Utility.setSystemHelp( composite, IHelpConstants.PREFIX
						+ "Wizard_DataSourcePropertyBinding" + "(" //$NON-NLS-1$ //$NON-NLS-2$
						+ odsh.getExtensionID( ).replace( '.', '_' ) + ")" //$NON-NLS-1$
						+ "_ID" ); //$NON-NLS-1$
				}

			}
			else if ( ds instanceof OdaDataSetHandle )
			{
				handle = (OdaDataSetHandle)ds;
				OdaDataSourceHandle odsh = (OdaDataSourceHandle) ( ( (OdaDataSetHandle) ds ).getDataSource( ) );
				String contextId = getDynamicContextId( ((OdaDataSetHandle)ds).getExtensionID( ),
						odsh.getExtensionID( ));
				if ( contextId != null )
				{
					//contextId is provided thru o.e.datatools.help extension point
					Utility.setSystemHelp( composite, contextId );
				}
				else
				{
					// '.' char will interrupt help system
					Utility.setSystemHelp( composite, IHelpConstants.PREFIX
						+ "Wizard_DataSetPropertyBinding" + "(" //$NON-NLS-1$ //$NON-NLS-2$
						+ odsh.getExtensionID( ).replace( '.', '_' ) + ")" //$NON-NLS-1$
						+ "_ID" ); //$NON-NLS-1$
				}
			}
			createExpressionButton( composite,
					propertyText,
					bindingName,
					isEncryptable );

		}
		if ( size <= 0 )
			setEmptyPropertyMessages( composite );
		return composite;
	}
	
	private static String getDynamicContextId( String helpKeyPrefix, String helpPlugin )
	{
		return HelpUtil.getContextId( helpKeyPrefix + ".properties.helpKey", helpPlugin ); //$NON-NLS-1$
	}

	private void createExpressionButton( Composite composite,
			final Text property, String propName, boolean isEncryptable )
	{
		ExpressionButton exprButton = ExpressionButtonUtil.createExpressionButton( composite,
				property,
				new ExpressionProvider( handle ),
				handle,
				true,
				SWT.PUSH );

		if ( isEncryptable )
		{
			exprButton.setExpressionButtonProvider( new ExprButtonProvider( true,
					property ) );
		}

		Expression expr = handle.getPropertyBindingExpression( propName );
		property.setData( ExpressionButtonUtil.EXPR_TYPE, expr == null
				|| expr.getType( ) == null ? UIUtil.getDefaultScriptType( )
				: (String) expr.getType( ) );
		
		property.setText( ( expr == null || expr.getStringExpression( ) == null )
				? "" : expr.getStringExpression( ) );
		exprButton.refresh( );
	}

	/**
	 * initial the property binding. If the property binding has not defined, the
	 * default binding will be the meta data of the property's value
	 * 
	 */
	private void initPropertyBinding( )
	{
		ds = (IDesignElementModel) getContainer( ).getModel( );
		Iterator iterator = null;
		IElementDefn elementDefn = getElementDefn( );

		if ( elementDefn != null )
		{
			iterator = elementDefn.getProperties( ).iterator( );
		}
		if ( ds instanceof DataSetHandle
				&& ( (DataSetHandle) ds ).getPropertyHandle( QUERYTEXT )
						.isVisible( ) )
		{
			propList.add( new String[]{
					QUERYTEXT,
					Messages.getString( "PropertyBindingPage.dataset.queryText" )} ); //$NON-NLS-1$
			bindingValue.add( ( (DataSetHandle) ds ).getPropertyBinding( QUERYTEXT ) == null
					? ""
					: ( (DataSetHandle) ds ).getPropertyBinding( QUERYTEXT ) );
		}
		if ( iterator != null )
		{
			while ( iterator.hasNext( ) )
			{
				IElementPropertyDefn propertyDefn = (IElementPropertyDefn) iterator.next( );
				if ( propertyDefn instanceof IPropertyDefn
						&& propertyDefn.getValueType( ) == IPropertyDefn.ODA_PROPERTY )
				{
					String name = propertyDefn.getName( );

					if ( elementDefn != null
							&& !elementDefn.isPropertyVisible( name ) )
						continue;
					propList.add( propertyDefn );

					if ( ds instanceof DataSetHandle )
					{
						bindingValue.add( ( (DataSetHandle) ds ).getPropertyBinding( name ) == null
								? ""
								: ( (DataSetHandle) ds ).getPropertyBinding( name ) );
					}
					else if ( ds instanceof DataSourceHandle )
					{
						bindingValue.add( ( (DataSourceHandle) ds ).getPropertyBinding( name ) == null
								? ""
								: ( (DataSourceHandle) ds ).getPropertyBinding( name ) );
					}
					else
					{
						bindingValue.add( "" );
					}
				}
			}
		}
	}

	/**
	 * get the elementDefn of datasourceHandle|datasetHandle
	 * @return
	 */
	private IElementDefn getElementDefn( )
	{
		IElementDefn elementDefn = null;
		if ( ds instanceof DataSourceHandle )
		{
			elementDefn = ( (DataSourceHandle) ds ).getDefn( );
		}
		else if ( ds instanceof DataSetHandle )
		{
			elementDefn = ( (DataSetHandle) ds ).getDefn( );
			

		}
		return elementDefn;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.ui.dialogs.properties.AbstractPropertyPage#performOk()
	 */
	public boolean performOk( )
	{
		for ( int i = 0; i < propList.size( ); i++ )
		{
			try
			{
				String value = null;
				Text propertyText = (Text) propertyTextList.get( i );
				
				if ( !propertyText.isDisposed( )
						&& propertyText.getText( ) != null
						&& propertyText.getText( ).trim( ).length( ) > 0 )
				{
					value = propertyText.getText( ).trim( );
				}
				Expression expr = new Expression( value,
						(String) propertyText.getData( DataUIConstants.EXPR_TYPE ) );
				
				if ( ds instanceof DesignElementHandle )
				{
					if ( propList.get( i ) instanceof String[] )
					{
						( (DesignElementHandle) ds ).setPropertyBinding( ( (String[]) propList.get( i ) )[0],
								expr );
					}
					else if ( propList.get( i ) instanceof IPropertyDefn )
					{
						( (DesignElementHandle) ds ).setPropertyBinding( ( (IPropertyDefn) propList.get( i ) ).getName( ),
								expr );
					}
				}
			}
			catch ( Exception e )
			{
				logger.log( Level.FINE, e.getMessage( ), e );
				ExceptionHandler.handle( e );
				return true;
			}
		}
		return super.performOk( );
	}
	
	/**
	 * if the dataset/datasource has no public property, set message to show
	 * this.
	 * 
	 */
	private void setEmptyPropertyMessages( Composite composite )
	{
		Label messageLabel = new Label( composite, SWT.NONE );
		if ( ds instanceof DataSourceHandle )
		{
			messageLabel.setText( Messages.getString( "PropertyBindingPage.datasource.property.empty" ) ); //$NON-NLS-1$
		}
		else if ( ds instanceof DataSetHandle )
		{
			messageLabel.setText( Messages.getString( "PropertyBindingPage.dataset.property.empty" ) ); //$NON-NLS-1$
		}
	}
	
	/**
	 * activate the property binding page
	 */
	public void pageActivated( )
	{
		getContainer( ).setMessage( Messages.getString( "datasource.editor.property" ),//$NON-NLS-1$
				IMessageProvider.NONE );
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.birt.report.designer.data.ui.property.AbstractPropertyPage#getToolTip()
	 */
	public String getToolTip( )
	{
		return Messages.getString( "PropertyBindingPage.property.tooltip" ); //$NON-NLS-1$
	}
	
	private class ExprButtonProvider extends ExpressionButtonProvider 
	{

		private Text propText;
		
		public ExprButtonProvider( boolean allowConstant, Text propText )
		{
			super( allowConstant );
			this.propText = propText;
		}
		
		public void handleSelectionEvent( String exprType )
		{
			super.handleSelectionEvent( exprType );
			if ( ExpressionType.CONSTANT.equals( exprType ) )
			{
				Text dummy = new Text( propText.getParent( ), SWT.BORDER
						| SWT.PASSWORD );
				propText.setEchoChar( dummy.getEchoChar( ) );
				dummy.dispose( );
			}
			else
			{
				propText.setEchoChar( (char) 0 );
			}
		}

	}

}

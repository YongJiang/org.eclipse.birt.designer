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

package org.eclipse.birt.report.item.crosstab.ui.views.dialogs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.birt.data.engine.olap.api.query.ICubeQueryDefinition;
import org.eclipse.birt.report.data.adapter.api.DataRequestSession;
import org.eclipse.birt.report.data.adapter.api.DataSessionContext;
import org.eclipse.birt.report.designer.internal.ui.expressions.IExpressionConverter;
import org.eclipse.birt.report.designer.internal.ui.swt.custom.MultiValueCombo;
import org.eclipse.birt.report.designer.internal.ui.swt.custom.ValueCombo;
import org.eclipse.birt.report.designer.internal.ui.util.ExceptionHandler;
import org.eclipse.birt.report.designer.internal.ui.util.ExpressionButtonUtil;
import org.eclipse.birt.report.designer.internal.ui.util.ExpressionUtility;
import org.eclipse.birt.report.designer.ui.dialogs.ExpressionBuilder;
import org.eclipse.birt.report.designer.ui.dialogs.HighlightRuleBuilder;
import org.eclipse.birt.report.designer.ui.dialogs.SelectValueDialog;
import org.eclipse.birt.report.designer.ui.preferences.PreferenceFactory;
import org.eclipse.birt.report.designer.ui.util.UIUtil;
import org.eclipse.birt.report.designer.ui.views.attributes.providers.HighlightHandleProvider;
import org.eclipse.birt.report.designer.util.DEUtil;
import org.eclipse.birt.report.item.crosstab.core.de.CrosstabCellHandle;
import org.eclipse.birt.report.item.crosstab.core.de.CrosstabReportItemHandle;
import org.eclipse.birt.report.item.crosstab.internal.ui.dialogs.CrosstabBindingExpressionProvider;
import org.eclipse.birt.report.item.crosstab.internal.ui.util.CrosstabUIHelper;
import org.eclipse.birt.report.item.crosstab.plugin.CrosstabPlugin;
import org.eclipse.birt.report.item.crosstab.ui.i18n.Messages;
import org.eclipse.birt.report.model.api.ComputedColumnHandle;
import org.eclipse.birt.report.model.api.DataItemHandle;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.ExpressionType;
import org.eclipse.birt.report.model.api.ExtendedItemHandle;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.extension.ExtendedElementException;
import org.eclipse.birt.report.model.api.extension.IReportItem;
import org.eclipse.birt.report.model.api.olap.CubeHandle;
import org.eclipse.birt.report.model.api.olap.TabularCubeHandle;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * 
 */

public class CrosstabHighlightRuleBuilder extends HighlightRuleBuilder
{

	/**
	 * @param parentShell
	 * @param title
	 * @param provider
	 */
	public CrosstabHighlightRuleBuilder( Shell parentShell, String title,
			HighlightHandleProvider provider )
	{
		super( parentShell, title, provider );
		initializeListener( );
	}

	protected void inilializeColumnList( DesignElementHandle handle )
	{
		super.inilializeColumnList( handle );
		expSelListener = new SelectionAdapter( ) {

			public void widgetSelected( SelectionEvent e )
			{
				IExpressionConverter converter = ExpressionButtonUtil.getCurrentExpressionConverter( getExpressionControl( ) );
				if ( converter != null )
				{
					if ( getExpression( ).equals( VALUE_OF_THIS_DATA_ITEM )
							&& designHandle instanceof DataItemHandle )
					{
						setExpression( ExpressionUtility.getDataExpression( ( (DataItemHandle) designHandle ).getResultSetColumn( ),
								converter ) );
					}
					else
					{
						String newValue = getExpression( );
						Object computedColumn = getResultSetColumn( newValue );
						if ( computedColumn != null )
						{
							String value = ExpressionUtility.getDataExpression( ( (ComputedColumnHandle) computedColumn ).getName( ),
									converter );
							if ( value != null )
								newValue = value;
							setExpression( newValue );
						}
					}
					updateButtons( );
				}
			}
		};
	}

	// protected void popBtnSelectionAction( Combo comboWidget )
	// {
	// int selectionIndex = comboWidget.getSelectionIndex( );
	// if ( selectionIndex < 0 )
	// {
	// return;
	// }
	// String value = comboWidget.getItem( selectionIndex );
	//
	// boolean isAddClick = false;
	// if ( tableViewer != null
	// && ( addBtn != null && ( !addBtn.isDisposed( ) ) ) )
	// {
	// isAddClick = true;
	// }
	//
	// for ( Iterator iter = columnList.iterator( ); iter.hasNext( ); )
	// {
	// String columnName = ( (ComputedColumnHandle) ( iter.next( ) ) ).getName(
	// );
	// if ( DEUtil.getColumnExpression( columnName )
	// .equals( expression.getText( ) ) )
	// {
	// bindingName = columnName;
	// break;
	// }
	// }
	//
	// boolean returnValue = false;
	// if ( value != null )
	// {
	// String newValues[] = new String[1];
	// if ( value.equals( ( actions[0] ) ) )
	// {
	// List selectValueList = getSelectedValueList( );
	// if ( selectValueList == null || selectValueList.size( ) == 0 )
	// {
	// MessageDialog.openInformation( null,
	//							Messages.getString( "SelectValueDialog.selectValue" ), //$NON-NLS-1$
	//							Messages.getString( "SelectValueDialog.messages.info.selectVauleUnavailable" ) ); //$NON-NLS-1$
	//
	// }
	// else
	// {
	// SelectValueDialog dialog = new SelectValueDialog(
	// PlatformUI.getWorkbench( )
	// .getDisplay( )
	// .getActiveShell( ),
	//							Messages.getString( "ExpressionValueCellEditor.title" ) ); //$NON-NLS-1$
	// dialog.setSelectedValueList( selectValueList );
	//
	// if ( isAddClick )
	// {
	// dialog.setMultipleSelection( true );
	// }
	//
	// if ( dialog.open( ) == IDialogConstants.OK_ID )
	// {
	// returnValue = true;
	// newValues = dialog.getSelectedExprValues( );
	// }
	// }
	// }
	// else if ( value.equals( actions[1] ) )
	// {
	// ExpressionBuilder dialog = new ExpressionBuilder(
	// PlatformUI.getWorkbench( )
	// .getDisplay( )
	// .getActiveShell( ),
	// comboWidget.getText( ) );
	//
	// if ( expressionProvider == null
	// || ( !( expressionProvider instanceof CrosstabBindingExpressionProvider )
	// ) )
	// {
	// expressionProvider = new CrosstabBindingExpressionProvider( designHandle,
	// null );
	// }
	//
	// dialog.setExpressionProvier( expressionProvider );
	//
	// if ( dialog.open( ) == IDialogConstants.OK_ID )
	// {
	// returnValue = true;
	// newValues[0] = dialog.getResult( );
	// }
	// }
	// else if ( selectionIndex > 3 )
	// {
	//				newValues[0] = "params[\"" + value + "\"]"; //$NON-NLS-1$ //$NON-NLS-2$
	// }
	// if ( returnValue )
	// {
	// if ( addExpressionValue == comboWidget )
	// {
	//					comboWidget.setText( "" ); //$NON-NLS-1$
	// addBtn.setEnabled( false );
	// }
	// else if ( newValues.length == 1 )
	// {
	// comboWidget.setText( DEUtil.resolveNull( newValues[0] ) );
	// }
	//
	// if ( isAddClick )
	// {
	// boolean change = false;
	// for ( int i = 0; i < newValues.length; i++ )
	// {
	// if ( valueList.indexOf( DEUtil.resolveNull( newValues[i] ) ) < 0 )
	// {
	// valueList.add( DEUtil.resolveNull( newValues[i] ) );
	// change = true;
	// }
	// }
	// if ( change )
	// {
	// tableViewer.refresh( );
	// updateButtons( );
	// addExpressionValue.setFocus( );
	// }
	//
	// }
	// }
	// }
	// }

	private List getSelectedValueList( )
	{
		CubeHandle cube = null;
		CrosstabReportItemHandle crosstab = null;
		if ( designHandle instanceof ExtendedItemHandle )
		{

			try
			{
				Object obj = ( (ExtendedItemHandle) designHandle ).getReportItem( );
				DesignElementHandle tmp = designHandle;

				while ( true )
				{
					if ( obj == null || obj instanceof ReportDesignHandle )
					{
						break;
					}
					else if ( obj instanceof CrosstabReportItemHandle )
					{
						crosstab = (CrosstabReportItemHandle) obj;
						cube = crosstab.getCube( );
						break;
					}
					else if ( tmp instanceof ExtendedItemHandle )
					{
						tmp = tmp.getContainer( );
						if ( tmp instanceof ExtendedItemHandle )
						{
							obj = ( (ExtendedItemHandle) tmp ).getReportItem( );
						}
					}
				}

			}
			catch ( ExtendedElementException e )
			{
				// TODO Auto-generated catch block
				logger.log( Level.SEVERE, e.getMessage( ), e );
			}

		}
		if ( cube == null
				|| ( !( cube instanceof TabularCubeHandle ) )
				|| getExpression( ).length( ) == 0 )
		{
			return new ArrayList( );
		}

		String expression = null;
		IExpressionConverter converter = ExpressionButtonUtil.getCurrentExpressionConverter( getExpressionControl( ) );
		if ( converter != null )
		{
			for ( int i = 0; i < columnList.size( ); i++ )
			{

				ComputedColumnHandle column = columnList.get( i );
				if ( column != null )
				{
					String value = ExpressionUtility.getDataExpression( column.getName( ),
							converter );
					if ( value.equals( getExpression( ) ) )
					{
						expression = ExpressionUtility.getDataExpression( column.getName( ),
								ExpressionUtility.getExpressionConverter( ExpressionType.JAVASCRIPT ) );
						break;
					}
				}
			}
		}

		List valueList = new ArrayList( );

		if ( expression == null )
			return valueList;

		Iterator iter = null;

		// get cubeQueryDefn
		ICubeQueryDefinition cubeQueryDefn = null;
		DataRequestSession session = null;
		try
		{
			session = DataRequestSession.newSession( new DataSessionContext( DataSessionContext.MODE_DIRECT_PRESENTATION ) );
			cubeQueryDefn = CrosstabUIHelper.createBindingQuery( crosstab );
			iter = session.getCubeQueryUtil( )
					.getMemberValueIterator( (TabularCubeHandle) cube,
							expression,
							cubeQueryDefn );
		}
		catch ( Exception e )
		{
			// TODO Auto-generated catch block
			logger.log( Level.SEVERE, e.getMessage( ), e );
		}

		int count = 0;
		int MAX_COUNT = PreferenceFactory.getInstance( )
				.getPreferences( CrosstabPlugin.getDefault( ),
						UIUtil.getCurrentProject( ) )
				.getInt( CrosstabPlugin.PREFERENCE_FILTER_LIMIT );
		while ( iter != null && iter.hasNext( ) )
		{
			Object obj = iter.next( );
			if ( obj != null )
			{
				if ( valueList.indexOf( obj ) < 0 )
				{
					valueList.add( obj );
					if ( ++count >= MAX_COUNT )
					{
						break;
					}
				}

			}

		}
		if ( session != null )
		{
			session.shutdown( );
		}
		return valueList;
	}

	private void initializeListener( )
	{
		expValueAction = new ValueCombo.ISelection( ) {

			public String doSelection( String input )
			{
				String retValue = null;
				// TODO Auto-generated method stub
				ExpressionBuilder dialog = new ExpressionBuilder( PlatformUI.getWorkbench( )
						.getDisplay( )
						.getActiveShell( ),
						input );

				if ( expressionProvider == null
						|| ( !( expressionProvider instanceof CrosstabBindingExpressionProvider ) ) )
				{
					expressionProvider = new CrosstabBindingExpressionProvider( designHandle,
							null );
				}

				dialog.setExpressionProvier( expressionProvider );

				if ( dialog.open( ) == IDialogConstants.OK_ID )
				{
					retValue = dialog.getResult( );
				}

				return retValue;
			}
		};
		selectValueAction = new ValueCombo.ISelection( ) {

			public String doSelection( String input )
			{
				String retValue = null;
				// TODO Auto-generated method stub
				List selectValueList = getSelectedValueList( );
				if ( selectValueList == null || selectValueList.size( ) == 0 )
				{
					MessageDialog.openInformation( null,
							Messages.getString( "SelectValueDialog.selectValue" ), //$NON-NLS-1$
							Messages.getString( "SelectValueDialog.messages.info.selectVauleUnavailable" ) ); //$NON-NLS-1$

				}
				else
				{
					SelectValueDialog dialog = new SelectValueDialog( PlatformUI.getWorkbench( )
							.getDisplay( )
							.getActiveShell( ),
							Messages.getString( "ExpressionValueCellEditor.title" ) ); //$NON-NLS-1$
					dialog.setSelectedValueList( selectValueList );

					if ( dialog.open( ) == IDialogConstants.OK_ID )
					{
						retValue = dialog.getSelectedExprValue( );
					}
				}
				return retValue;
			}
		};

		mAddSelValueAction = new MultiValueCombo.ISelection( ) {

			public void doAfterSelection( MultiValueCombo combo )
			{
				// TODO Auto-generated method stub
				addBtn.setEnabled( false );

				if ( addExpressionValue.getSelStrings( ).length == 1 )
				{
					addExpressionValue.setText( DEUtil.resolveNull( addExpressionValue.getSelStrings( )[0] ) );
				}
				else if ( addExpressionValue.getSelStrings( ).length > 1 )
				{
					addExpressionValue.setText( "" ); //$NON-NLS-1$
				}

				boolean change = false;
				for ( int i = 0; i < addExpressionValue.getSelStrings( ).length; i++ )
				{
					if ( valueList.indexOf( DEUtil.resolveNull( addExpressionValue.getSelStrings( )[i] ) ) < 0 )
					{
						valueList.add( DEUtil.resolveNull( addExpressionValue.getSelStrings( )[i] ) );
						change = true;
					}
				}
				if ( change )
				{
					tableViewer.refresh( );
					updateButtons( );
					addExpressionValue.setFocus( );
				}
			}

			public String[] doSelection( String input )
			{
				String retValue[] = null;
				// TODO Auto-generated method stub

				List selectValueList = getSelectedValueList( );
				if ( selectValueList == null || selectValueList.size( ) == 0 )
				{
					MessageDialog.openInformation( null,
							Messages.getString( "SelectValueDialog.selectValue" ), //$NON-NLS-1$
							Messages.getString( "SelectValueDialog.messages.info.selectVauleUnavailable" ) ); //$NON-NLS-1$
				}
				else
				{
					SelectValueDialog dialog = new SelectValueDialog( PlatformUI.getWorkbench( )
							.getDisplay( )
							.getActiveShell( ),
							Messages.getString( "ExpressionValueCellEditor.title" ) ); //$NON-NLS-1$
					dialog.setSelectedValueList( selectValueList );
					dialog.setMultipleSelection( true );

					if ( dialog.open( ) == IDialogConstants.OK_ID )
					{
						retValue = dialog.getSelectedExprValues( );
					}
				}

				return retValue;
			}
		};

		mAddExpValueAction = new MultiValueCombo.ISelection( ) {

			public void doAfterSelection( MultiValueCombo combo )
			{
				// TODO Auto-generated method stub
				mAddSelValueAction.doAfterSelection( combo );
			}

			public String[] doSelection( String input )
			{
				String retValue[] = null;
				// TODO Auto-generated method stub
				ExpressionBuilder dialog = new ExpressionBuilder( PlatformUI.getWorkbench( )
						.getDisplay( )
						.getActiveShell( ),
						input );

				if ( expressionProvider == null
						|| ( !( expressionProvider instanceof CrosstabBindingExpressionProvider ) ) )
				{
					expressionProvider = new CrosstabBindingExpressionProvider( designHandle,
							null );
				}

				dialog.setExpressionProvier( expressionProvider );

				if ( dialog.open( ) == IDialogConstants.OK_ID )
				{
					if ( dialog.getResult( ).length( ) != 0 )
					{
						retValue = new String[]{
							dialog.getResult( )
						};
					}
				}
				return retValue;
			}
		};

	}

	protected int getHighlightExpCtrType( DesignElementHandle handle )
	{
		if ( handle instanceof ExtendedItemHandle )
		{
			try
			{
				Object obj = ( (ExtendedItemHandle) handle ).getReportItem( );
				if ( obj instanceof CrosstabReportItemHandle )
				{
					return EXPRESSION_CONTROL_TEXT;
				}
			}
			catch ( ExtendedElementException e )
			{
				// TODO Auto-generated catch block
				return EXPRESSION_CONTROL_COMBO;
			}

			return EXPRESSION_CONTROL_COMBO;

		}
		else
		{
			return EXPRESSION_CONTROL_COMBO;
		}
	}

	protected void initilizeDlgDescription( DesignElementHandle handle )
	{
		if ( !( handle instanceof ExtendedItemHandle ) )
		{
			super.initilizeDlgDescription( handle );
			return;
		}

		Class classList[] = new Class[]{
				CrosstabReportItemHandle.class, CrosstabCellHandle.class,
		};
		String desList[] = new String[]{
				Messages.getString( "CrosstabHighlightRuleBuilderDialog.text.Description.Element.Crosstab" ),
				Messages.getString( "CrosstabHighlightRuleBuilderDialog.text.Description.Element.Crosstabcell" ),
		};

		try
		{
			IReportItem reportItem = ( (ExtendedItemHandle) handle ).getReportItem( );
			Class handleClass = reportItem.getClass( );
			for ( int i = 0; i < classList.length; i++ )
			{
				if ( classList[i] == handleClass )
				{
					dlgDescription = desList[i];
					break;
				}
			}
			if ( dlgDescription == null || dlgDescription.length( ) == 0 )
			{
				dlgDescription = Messages.getString( "CrosstabHighlightRuleBuilderDialog.text.Description.Element.ReportElement" );
			}

			dlgDescription = Messages.getFormattedString( "CrosstabHighlightRuleBuilderDialog.text.Description",
					new Object[]{
						dlgDescription
					} );

		}
		catch ( ExtendedElementException e )
		{
			ExceptionHandler.handle( e );
		}

	}
}

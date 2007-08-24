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
import org.eclipse.birt.report.designer.internal.ui.views.dialogs.provider.HighlightHandleProvider;
import org.eclipse.birt.report.designer.ui.dialogs.ExpressionBuilder;
import org.eclipse.birt.report.designer.ui.dialogs.ExpressionValue;
import org.eclipse.birt.report.designer.ui.dialogs.HighlightRuleBuilder;
import org.eclipse.birt.report.designer.ui.dialogs.SelectValueDialog;
import org.eclipse.birt.report.designer.ui.widget.PopupSelectionList;
import org.eclipse.birt.report.designer.util.DEUtil;
import org.eclipse.birt.report.item.crosstab.core.de.CrosstabReportItemHandle;
import org.eclipse.birt.report.item.crosstab.internal.ui.dialogs.CrosstabBindingExpressionProvider;
import org.eclipse.birt.report.item.crosstab.internal.ui.util.CrosstabUIHelper;
import org.eclipse.birt.report.item.crosstab.plugin.CrosstabPlugin;
import org.eclipse.birt.report.item.crosstab.ui.i18n.Messages;
import org.eclipse.birt.report.model.api.ComputedColumnHandle;
import org.eclipse.birt.report.model.api.ExtendedItemHandle;
import org.eclipse.birt.report.model.api.extension.ExtendedElementException;
import org.eclipse.birt.report.model.api.olap.CubeHandle;
import org.eclipse.birt.report.model.api.olap.TabularCubeHandle;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
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
	}

	protected void popBtnSelectionAction(ExpressionValue expressionValue)
	{
		Text valueText =  expressionValue.getTextControl( );
		Rectangle textBounds = valueText.getBounds( );
		Point pt = valueText.toDisplay( textBounds.x, textBounds.y );
		Rectangle rect = new Rectangle( pt.x, pt.y, valueText.getParent( )
				.getBounds( ).width, textBounds.height );

		PopupSelectionList popup = new PopupSelectionList( valueText.getParent( )
				.getShell( ) );
		popup.setItems( popupItems );
		String value = popup.open( rect );
		int selectionIndex = popup.getSelectionIndex( );

		for ( Iterator iter = columnList.iterator( ); iter.hasNext( ); )
		{
			String columnName = ( (ComputedColumnHandle) ( iter.next( ) ) ).getName( );
			if ( DEUtil.getColumnExpression( columnName )
					.equals( expression.getText( ) ) )
			{
				bindingName = columnName;
				break;
			}
		}

		if ( value != null )
		{
			String newValue = null;
			if ( value.equals( ( actions[0] ) ) )
			{
				List selectValueList = getSelectedValueList( );
				if ( selectValueList == null
						|| selectValueList.size( ) == 0 )
				{
					MessageDialog.openInformation( null,
							Messages.getString( "SelectValueDialog.selectValue" ),
							Messages.getString( "SelectValueDialog.messages.info.selectVauleUnavailable" ) );

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
						newValue = dialog.getSelectedExprValue( );
					}
				}
			}
			else if ( value.equals( actions[1] ) )
			{
				ExpressionBuilder dialog = new ExpressionBuilder( PlatformUI.getWorkbench( )
						.getDisplay( )
						.getActiveShell( ),
						valueText.getText( ) );

				if (expressionProvider == null ||( !( expressionProvider instanceof CrosstabBindingExpressionProvider) ))
				{
					expressionProvider = new CrosstabBindingExpressionProvider( designHandle );
				}

				dialog.setExpressionProvier( expressionProvider );

				if ( dialog.open( ) == IDialogConstants.OK_ID )
				{
					newValue = dialog.getResult( );
				}
			}
			else if ( selectionIndex > 3 )
			{
				newValue = "params[\"" + value + "\"]"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			if ( newValue != null )
			{
				valueText.setText( newValue );
			}
		}
	}

	private List getSelectedValueList( )
	{
		CubeHandle cube = null;
		CrosstabReportItemHandle crosstab = null;
		if ( designHandle instanceof ExtendedItemHandle )
		{
			try
			{
				Object obj = ( (ExtendedItemHandle) designHandle ).getReportItem( );
				if ( obj instanceof CrosstabReportItemHandle )
				{
					crosstab = (CrosstabReportItemHandle) obj;
				}

				crosstab = (CrosstabReportItemHandle) ( (ExtendedItemHandle) designHandle ).getReportItem( );
				cube = crosstab.getCube( );
			}
			catch ( ExtendedElementException e )
			{
				// TODO Auto-generated catch block
				logger.log(Level.SEVERE, e.getMessage(),e);
			}

		}
		if ( cube == null
				|| ( !( cube instanceof TabularCubeHandle ) )
				|| expression.getText( ).length( ) == 0 )
		{
			return new ArrayList( );
		}
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
							expression.getText( ),
							cubeQueryDefn );
		}
		catch ( Exception e )
		{
			// TODO Auto-generated catch block
			logger.log(Level.SEVERE, e.getMessage(),e);
		}
		List valueList = new ArrayList( );
		int count = 0;
		int MAX_COUNT = CrosstabPlugin.getDefault( )
				.getPluginPreferences( )
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
		return valueList;
	}
}

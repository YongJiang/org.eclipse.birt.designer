/*******************************************************************************
 * Copyright (c) 2005 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.report.designer.ui.dialogs;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.eclipse.birt.core.data.DataTypeUtil;
import org.eclipse.birt.report.designer.internal.ui.dialogs.BaseDialog;
import org.eclipse.birt.report.designer.internal.ui.util.ExceptionHandler;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.model.api.ParamBindingHandle;
import org.eclipse.birt.report.model.api.elements.DesignChoiceConstants;
import org.eclipse.birt.report.model.api.util.ParameterValidationUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * This dialog takes an expression and a data set and shows a list of unique
 * values for selection from the data set. It allows both multiple and single
 * selection. The default is single selection.
 * 
 * @version $Revision: 1.16 $ $Date: 2006/06/07 08:54:59 $
 */
public class SelectValueDialog extends BaseDialog
{

	private boolean multipleSelection = false;

	private List selectValueList = null;
	private int[] selectedIndices = null;
	private java.util.List modelValueList = new ArrayList( );
	private java.util.List viewerValueList = new ArrayList( );

	private ParamBindingHandle[] bindingParams = null;

	/**
	 * @param parentShell
	 * @param title
	 */
	public SelectValueDialog( Shell parentShell, String title )
	{
		super( parentShell, title);
	}

	/**
	 * @return Returns the paramBindingHandles.
	 */
	public ParamBindingHandle[] getBindingParams( )
	{
		return bindingParams;
	}

	/**
	 *  Set handles for binding parameters
	 */
	public void setBindingParams( ParamBindingHandle[] handles )
	{
		this.bindingParams = handles;
	}

	/**
	 * @param expression
	 *            The expression to set.
	 */
	public void setSelectedValueList( Collection valueList )
	{
		modelValueList.clear( );
		modelValueList.addAll( valueList );
	}

	/**
	 * @return Returns the multipleSelection.
	 */
	public boolean isMultipleSelection( )
	{
		return multipleSelection;
	}

	/**
	 * @param multipleSelection
	 *            The multipleSelection to set.
	 */
	public void setMultipleSelection( boolean multipleSelection )
	{
		this.multipleSelection = multipleSelection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea( Composite parent )
	{
		Composite composite = new Composite( parent, SWT.NONE );
		GridLayout layout = new GridLayout( );
		composite.setLayout( layout );
		Label label = new Label( composite, SWT.NONE );
		label.setText( Messages.getString( "SelectValueDialog.selectValue" ) ); //$NON-NLS-1$

		selectValueList = new List( composite,
				isMultipleSelection( ) ? SWT.MULTI : SWT.SINGLE
						| SWT.V_SCROLL
						| SWT.H_SCROLL );
		GridData data = new GridData( GridData.FILL_BOTH );
		data.heightHint = 250;
		data.widthHint = 300;
		selectValueList.setLayoutData( data );
		selectValueList.add( Messages.getString( "SelectValueDialog.retrieving" ) ); //$NON-NLS-1$
		selectValueList.addMouseListener( new MouseAdapter( ) {

			public void mouseDoubleClick( MouseEvent e )
			{
				if ( selectValueList.getSelectionCount( ) > 0 )
				{
					okPressed( );
				}
			}
		} );

		PlatformUI.getWorkbench( ).getDisplay( ).asyncExec( new Runnable( ) {

			public void run( )
			{
				populateList( );
			}

		} );

		return composite;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed( )
	{
		selectedIndices = selectValueList.getSelectionIndices();
		setResult( selectValueList.getSelection( ) );
		super.okPressed( );
	}

	/*
	 * Return the first selected value if selected result is not null
	 */
	public String getSelectedValue( )
	{
		String[] result = (String[]) getResult( );
		return ( result != null && result.length > 0 ) ? result[0] : null;
	}

	/*
	 * Return all the selected value if selected result is not null
	 */
	public String[] getSelectedValues( )
	{
		String[] result = (String[]) getResult( );
		return ( result != null && result.length > 0 ) ? result : null;
	}

	/**
	 * Return expression string value as expression required format.
	 * For example
	 * 	number type:
	 * 		Integer value 1 to String value "1"
	 *  Boolean type:
	 *      Boolean value true to String value "true"
	 * 	other types:
	 * 		String value "abc" to String value "\"abc\""
	 * 		Date value "2000-10-10" to String value "\"2000-10-10\""
	 * @return expression value
	 */
	public String getSelectedExprValue( )
	{
		String exprValue = null;
		if ( selectedIndices != null && selectedIndices.length > 0 )
		{
			int firstIndex = selectedIndices[0];
			Object modelValue = modelValueList.get( firstIndex );
			String viewerValue = (String) viewerValueList.get( firstIndex );

			if ( modelValue instanceof Boolean
					|| modelValue instanceof Integer
					|| modelValue instanceof Double
					|| modelValue instanceof BigDecimal )
			{
				exprValue = viewerValue;
			}
			else
			{
				exprValue = "\"" + viewerValue + "\"";
			}
		}
		return exprValue;
	}
	
	private void populateList( )
	{
		try
		{
			getOkButton( ).setEnabled( false );
			selectValueList.removeAll( );
			viewerValueList.clear();
			if ( modelValueList != null && modelValueList.size( ) > 0 )
			{
				Iterator iter = modelValueList.iterator();
				while( iter.hasNext()){
				Object candiateValue = iter.next();
				if ( candiateValue != null )
				{
					String displayCandiateValue;
					if ( candiateValue instanceof Date )
						displayCandiateValue = ParameterValidationUtil.getDisplayValue( DesignChoiceConstants.PARAM_TYPE_DATETIME,
								null,
								candiateValue );
					else
						displayCandiateValue = DataTypeUtil.toString( candiateValue );
					viewerValueList.add( displayCandiateValue );
					selectValueList.add( displayCandiateValue );
				}}
			}
			else
			{
				selectValueList.removeAll( );
				modelValueList.clear();
				viewerValueList.clear();
				ExceptionHandler.openErrorMessageBox( Messages.getString( "SelectValueDialog.errorRetrievinglist" ), Messages.getString( "SelectValueDialog.noExpressionSet" ) ); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if ( selectValueList.getItemCount( ) > 0 )
			{
				selectValueList.select( 0 );
				getOkButton( ).setEnabled( true );
			}

		}
		catch ( Exception e )
		{
			ExceptionHandler.handle( e );
		}
	}

	//	private void populateList1( )
	//	{
	//		try
	//		{
	//			//Execute the query and populate this list
	//			OdaDataSetDesign design = (OdaDataSetDesign)
	// DataSetManager.getCurrentInstance( )
	//					.getDataSetDesign( getDataSetHandle( ), true, false );
	//			design.addComputedColumn( new ComputedColumn( "selectValue",
	//					getExpression( ) ) );
	//			QueryDefinition query = DataSetManager.getCurrentInstance( )
	//					.getQueryDefinition( design );
	//			ScriptExpression expression = new ScriptExpression(
	// "row[\"selectValue\"]" );
	//
	//			GroupDefinition defn = new GroupDefinition( );
	//			defn.setKeyExpression( expression.getText( ) );
	//			query.setUsesDetails( false );
	//			query.addGroup( defn );
	//			query.addExpression( expression, BaseTransform.BEFORE_FIRST_ROW );
	//
	//			IPreparedQuery preparedQuery = DataSetManager.getCurrentInstance( )
	//					.getPreparedQuery( query );
	//			IQueryResults results = preparedQuery.execute( null );
	//			selectValueList.removeAll( );
	//			if ( results != null )
	//			{
	//				IResultIterator iter = results.getResultIterator( );
	//				if ( iter != null )
	//				{
	//					while ( iter.next( ) )
	//					{
	//						selectValueList.add( iter.getString( expression ) );
	//					}
	//				}
	//
	//				results.close( );
	//			}
	//		}
	//		catch ( Exception e )
	//		{
	//			e.printStackTrace( );
	//			ExceptionHandler.handle( e );
	//		}
	//	}
}
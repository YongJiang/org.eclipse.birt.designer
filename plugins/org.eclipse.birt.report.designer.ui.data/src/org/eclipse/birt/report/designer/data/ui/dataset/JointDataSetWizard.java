/*******************************************************************************
 * Copyright (c) 2004, 2005 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/
package org.eclipse.birt.report.designer.data.ui.dataset;

import org.eclipse.birt.report.designer.data.ui.util.Utility;
import org.eclipse.birt.report.designer.internal.ui.util.ExceptionHandler;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.model.api.DataSetHandle;
import org.eclipse.birt.report.model.api.DataSourceHandle;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.eclipse.jface.wizard.Wizard;

/**
 * The wizard to joint dataset
 */

public class JointDataSetWizard extends Wizard
{

	private static final String CREATE_DATA_SET_TRANS_NAME = Messages.getString( "AbstractDataSetWizard.ModelTrans.Create" ); //$NON-NLS-1$

	private transient boolean useTransaction = true;
	private JointDataSetPage dataSetPage;
	
	/**
	 *  
	 */
	public JointDataSetWizard( )
	{
		this( null, true );
	}
	
	/**
	 * 
	 * @param dataSourceHandle
	 * @param useTransaction
	 */
	public JointDataSetWizard( DataSourceHandle dataSourceHandle,
			boolean useTransaction )
	{
		super( );
		this.useTransaction = useTransaction;
		dataSetPage = new JointDataSetPage( Messages.getString( "JointDataSetPage.pageName" ) ); //$NON-NLS-1$
		setForcePreviousAndNextButtons( true );
		addPage( dataSetPage );
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	public boolean performFinish( )
	{
		if ( !canFinish( ) )
			return false;

		if ( useTransaction )
		{
			// Start the transaction
			Utility.getCommandStack( ).startTrans( CREATE_DATA_SET_TRANS_NAME );
		}
		DataSetHandle joinDataSetHandle = dataSetPage.createSelectedDataSet( );
		try
		{
			if ( joinDataSetHandle != null )
				DataSetUIUtil.updateColumnCache( joinDataSetHandle );
		}
		catch ( SemanticException e )
		{
			ExceptionHandler.handle( e );
		}
		if ( useTransaction )
		{
			// Start the transaction
			Utility.getCommandStack( ).commit( );
		}
		return true;
	}

}
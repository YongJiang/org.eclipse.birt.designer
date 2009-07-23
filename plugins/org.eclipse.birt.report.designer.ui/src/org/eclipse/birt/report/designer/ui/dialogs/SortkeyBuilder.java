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

package org.eclipse.birt.report.designer.ui.dialogs;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.birt.report.designer.core.model.SessionHandleAdapter;
import org.eclipse.birt.report.designer.internal.ui.dialogs.BaseTitleAreaDialog;
import org.eclipse.birt.report.designer.internal.ui.util.ExceptionHandler;
import org.eclipse.birt.report.designer.internal.ui.util.ExpressionButtonUtil;
import org.eclipse.birt.report.designer.internal.ui.util.IHelpContextIds;
import org.eclipse.birt.report.designer.internal.ui.util.Policy;
import org.eclipse.birt.report.designer.internal.ui.util.UIUtil;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.designer.ui.views.attributes.providers.ChoiceSetFactory;
import org.eclipse.birt.report.designer.util.DEUtil;
import org.eclipse.birt.report.model.api.CommandStack;
import org.eclipse.birt.report.model.api.ComputedColumnHandle;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.ListingHandle;
import org.eclipse.birt.report.model.api.PropertyHandle;
import org.eclipse.birt.report.model.api.SortKeyHandle;
import org.eclipse.birt.report.model.api.StructureFactory;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.eclipse.birt.report.model.api.elements.structures.SortKey;
import org.eclipse.birt.report.model.api.metadata.IChoice;
import org.eclipse.birt.report.model.api.metadata.IChoiceSet;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 */

public class SortkeyBuilder extends BaseTitleAreaDialog
{

	protected Logger logger = Logger.getLogger( SortkeyBuilder.class.getName( ) );

	public static final String DLG_TITLE_NEW = Messages.getString( "SortkeyBuilder.DialogTitle.New" ); //$NON-NLS-1$
	public static final String DLG_MESSAGE_NEW = Messages.getString( "SortkeyBuilder.DialogMessage.New" ); //$NON-NLS-1$
	public static final String DLG_TITLE_EDIT = Messages.getString( "SortkeyBuilder.DialogTitle.Edit" ); //$NON-NLS-1$
	public static final String DLG_MESSAGE_EDIT = Messages.getString( "SortkeyBuilder.DialogMessage.Edit" ); //$NON-NLS-1$
	protected SortKeyHandle sortKey;

	protected DesignElementHandle handle;

	protected IChoiceSet choiceSet;

	protected Combo comboDirection;
	private Combo comboKey;

	private List columnList;

	/**
	 * Constant, represents empty String array.
	 */
	private static final String[] EMPTY = new String[0];

	/**
	 * @param title
	 */
	public SortkeyBuilder( String title, String message )
	{
		this( UIUtil.getDefaultShell( ), title, message );
	}

	/**
	 * @param parentShell
	 * @param title
	 */
	protected String title, message;

	public SortkeyBuilder( Shell parentShell, String title, String message )
	{
		super( parentShell );
		this.title = title;
		this.message = message;
		choiceSet = ChoiceSetFactory.getStructChoiceSet( SortKey.SORT_STRUCT,
				SortKey.DIRECTION_MEMBER );
	}

	protected Control createDialogArea( Composite parent )
	{
		UIUtil.bindHelp( parent, IHelpContextIds.INSERT_EDIT_SORTKEY_DIALOG_ID );

		Composite area = (Composite) super.createDialogArea( parent );
		Composite contents = new Composite( area, SWT.NONE );
		contents.setLayoutData( new GridData( GridData.FILL_BOTH ) );
		contents.setLayout( new GridLayout( ) );

		this.setTitle( title );
		this.setMessage( message );
		getShell( ).setText( title );

		applyDialogFont( contents );
		initializeDialogUnits( area );
		createInputContents( contents );

		Composite space = new Composite( contents, SWT.NONE );
		GridData gdata = new GridData( GridData.FILL_HORIZONTAL );
		gdata.heightHint = 10;
		space.setLayoutData( gdata );

		Label lb = new Label( contents, SWT.SEPARATOR | SWT.HORIZONTAL );
		lb.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
		return area;
	}

	protected Composite createInputContents( Composite parent )
	{
		Label lb = new Label( parent, SWT.NONE );
		lb.setText( Messages.getString( "SortkeyBuilder.DialogTitle.Label.Prompt" ) ); //$NON-NLS-1$

		Composite content = new Composite( parent, SWT.NONE );
		content.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
		GridLayout glayout = new GridLayout( 3, false );
		content.setLayout( glayout );

		Label labelKey = new Label( content, SWT.NONE );
		labelKey.setText( Messages.getString( "SortkeyBuilder.DialogTitle.Label.Key" ) ); //$NON-NLS-1$
		comboKey = new Combo( content, SWT.BORDER );
		GridData gdata = new GridData( GridData.FILL_HORIZONTAL );
		gdata.widthHint = 240;
		comboKey.setLayoutData( gdata );
		comboKey.setItems( getDataSetColumns( ) );
		if ( comboKey.getItemCount( ) == 0 )
		{
			comboKey.add( DEUtil.resolveNull( null ) );
		}
		comboKey.addListener( SWT.Selection, comboKeyModify );
		comboKey.addModifyListener( new ModifyListener( ) {

			public void modifyText( ModifyEvent e )
			{
				updateButtons( );
			}
		} );

		Listener listener = new Listener( ) {

			public void handleEvent( Event event )
			{
				updateButtons( );
			}

		};
		ExpressionButtonUtil.createExpressionButton( content,
				comboKey,
				new ExpressionProvider( handle ),
				listener );
		ExpressionButtonUtil.initJSExpressionButtonCombo( comboKey );

		Label labelDirection = new Label( content, SWT.NONE );
		labelDirection.setText( Messages.getString( "SortkeyBuilder.DialogTitle.Label.Direction" ) ); //$NON-NLS-1$

		comboDirection = new Combo( content, SWT.READ_ONLY | SWT.BORDER );
		String[] displayNames = ChoiceSetFactory.getDisplayNamefromChoiceSet( choiceSet );
		comboDirection.setItems( displayNames );
		gdata = new GridData( GridData.FILL_HORIZONTAL );
		comboDirection.setLayoutData( gdata );
		return content;
	}
	protected Listener comboKeyModify = new Listener( ) {

		public void handleEvent( Event e )
		{
			Assert.isLegal( e.widget instanceof Combo );
			Combo combo = (Combo) e.widget;
			String newValue = combo.getText( );
			String value = DEUtil.getExpression( getResultSetColumn( newValue ) );
			if ( value != null )
				newValue = value;
			combo.setText( newValue );
			updateButtons( );
		}
	};

	private Object getResultSetColumn( String name )
	{
		if ( columnList == null || columnList.isEmpty( ) )
		{
			return null;
		}
		for ( int i = 0; i < columnList.size( ); i++ )
		{
			ComputedColumnHandle column = (ComputedColumnHandle) columnList.get( i );
			if ( column.getName( ).equals( name ) )
			{
				return column;
			}
		}
		return null;
	}

	public int open( )
	{
		if ( getShell( ) == null )
		{
			// create the window
			create( );
		}
		if ( initDialog( ) )
		{
			if ( Policy.TRACING_DIALOGS )
			{
				String[] result = this.getClass( ).getName( ).split( "\\." ); //$NON-NLS-1$
				System.out.println( "Dialog >> Open " //$NON-NLS-1$
						+ result[result.length - 1] );
			}
			return super.open( );
		}

		return Dialog.CANCEL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.birt.report.designer.internal.ui.dialogs.BaseDialog#initDialog
	 * ()
	 */
	protected boolean initDialog( )
	{
		if ( sortKey == null )
		{
			comboKey.setText( "" ); //$NON-NLS-1$
			comboDirection.select( 0 );
			return true;
		}

		ExpressionButtonUtil.initExpressionButtonControl( comboKey,
				sortKey,
				SortKey.KEY_MEMBER );

		if ( sortKey.getDirection( ) != null
				&& sortKey.getDirection( ).trim( ).length( ) != 0 )
		{
			String value = sortKey.getDirection( ).trim( );
			IChoice choice = choiceSet.findChoice( value );
			if ( choice != null )
				value = choice.getDisplayName( );
			int index;
			index = comboDirection.indexOf( value );
			index = index < 0 ? 0 : index;
			comboDirection.select( index );
		}
		updateButtons( );
		return true;
	}

	protected String[] getDataSetColumns( )
	{
		if ( columnList.isEmpty( ) )
		{
			return EMPTY;
		}
		List<String> valueList = new ArrayList<String>( );
		for ( int i = 0; i < columnList.size( ); i++ )
		{
			ComputedColumnHandle columnHandle = ( (ComputedColumnHandle) columnList.get( i ) );
			if ( columnHandle.getAggregateFunction( ) == null )
				valueList.add( columnHandle.getName( ) );
		}
		return valueList.toArray( new String[valueList.size( )] );
	}

	public void setHandle( DesignElementHandle handle )
	{
		this.handle = handle;
		inilializeColumnList( handle );
	}

	private void inilializeColumnList( DesignElementHandle handle )
	{
		columnList = DEUtil.getVisiableColumnBindingsList( handle );
	}

	/**
	 * Notifies that the ok button of this dialog has been pressed.
	 * <p>
	 * The <code>Dialog</code> implementation of this framework method sets this
	 * dialog's return code to <code>Window.OK</code> and closes the dialog.
	 * Subclasses may override.
	 * </p>
	 */
	protected void okPressed( )
	{
		String direction = comboDirection.getText( );
		IChoice choice = choiceSet.findChoiceByDisplayName( direction );
		if ( choice != null )
			direction = choice.getDisplayName( );
		int index;
		index = comboDirection.indexOf( direction );
		CommandStack stack = SessionHandleAdapter.getInstance( )
				.getCommandStack( );
		stack.startTrans( title ); //$NON-NLS-1$ 
		try
		{
			if ( sortKey == null )
			{
				SortKey sortKey = StructureFactory.createSortKey( );
				ExpressionButtonUtil.saveExpressionButtonControl( comboKey,
						sortKey,
						SortKey.KEY_MEMBER );
				if ( index >= 0 )
				{
					sortKey.setDirection( choice.getName( ) );
				}

				PropertyHandle propertyHandle = handle.getPropertyHandle( ListingHandle.SORT_PROP );
				propertyHandle.addItem( sortKey );

			}
			else
			// edit
			{

				ExpressionButtonUtil.saveExpressionButtonControl( comboKey,
						sortKey,
						SortKey.KEY_MEMBER );

				if ( index >= 0 )
				{
					sortKey.setDirection( choice.getName( ) );
				}

			}
			stack.commit( );
		}
		catch ( SemanticException e )
		{
			ExceptionHandler.handle( e,
					Messages.getString( "SortkeyBuilder.DialogTitle.Error.SetSortKey.Title" ), //$NON-NLS-1$
					e.getLocalizedMessage( ) );
			stack.rollback( );
		}
		super.okPressed( );
	}

	public boolean performCancel( )
	{
		return true;
	}

	public boolean performOk( )
	{
		return true;
	}

	/**
	 * Sets the model input.
	 * 
	 * @param input
	 */
	public void setInput( Object input )
	{
		if ( input instanceof SortKeyHandle )
		{
			this.sortKey = (SortKeyHandle) input;
		}
		else
		{
			this.sortKey = null;
		}

	}

	/**
	 * Refreshes the OK button state.
	 * 
	 */
	protected void updateButtons( )
	{
		getButton( IDialogConstants.OK_ID ).setEnabled( isConditionOK( ) );
	}

	protected boolean isConditionOK( )
	{
		if ( comboKey.getText( ).trim( ).length( ) == 0 )
		{
			return false;
		}
		return true;
	}

}

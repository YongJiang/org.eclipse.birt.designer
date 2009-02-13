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

package org.eclipse.birt.report.designer.internal.ui.dialogs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import org.eclipse.birt.report.designer.internal.ui.util.ExceptionHandler;
import org.eclipse.birt.report.designer.internal.ui.util.IHelpContextIds;
import org.eclipse.birt.report.designer.internal.ui.util.UIUtil;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.designer.util.DEUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog class for resource key/value select and edit
 */

public class ResourceEditDialog extends BaseDialog
{

	private Label nameLabel;

	private TableViewer viewer;

	private Text keyText, valueText;

	private Button btnDelete;

	private Properties content = new Properties( );

	private String propFileName;

	private boolean listChanged;

	private URL resourceURL;

	private Button btnAdd;

	/**
	 * PropertyLabelProvider
	 */
	static class PropertyLabelProvider extends LabelProvider implements
			ITableLabelProvider
	{

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.
		 * lang.Object, int)
		 */
		public String getColumnText( Object element, int columnIndex )
		{
			if ( element instanceof Map.Entry )
			{
				Map.Entry entry = (Map.Entry) element;

				switch ( columnIndex )
				{
					case 0 :
						return String.valueOf( entry.getKey( ) );
					case 1 :
						return String.valueOf( entry.getValue( ) );
				}

			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java
		 * .lang.Object, int)
		 */
		public Image getColumnImage( Object element, int columnIndex )
		{
			return null;
		}
	}

	/**
	 * ResourceSorter
	 */
	static class ResourceSorter extends ViewerSorter
	{

		private boolean descent;
		private boolean second;

		/**
		 * The constructor.
		 * 
		 * @param descent
		 *            sorting order.
		 * @param second
		 *            if it's the second column.
		 */
		public ResourceSorter( boolean descent, boolean second )
		{
			super( );

			this.descent = descent;
			this.second = second;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.
		 * viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public int compare( Viewer viewer, Object e1, Object e2 )
		{
			String name1;
			String name2;

			if ( viewer == null || !( viewer instanceof ContentViewer ) )
			{
				if ( descent )
				{
					name1 = e2.toString( );
					name2 = e1.toString( );
				}
				else
				{
					name1 = e1.toString( );
					name2 = e2.toString( );
				}
			}
			else
			{
				IBaseLabelProvider prov = ( (ContentViewer) viewer ).getLabelProvider( );
				if ( prov instanceof ITableLabelProvider )
				{
					ITableLabelProvider lprov = (ITableLabelProvider) prov;
					if ( second )
					{
						if ( descent )
						{
							name1 = lprov.getColumnText( e2, 1 );
							name2 = lprov.getColumnText( e1, 1 );
						}
						else
						{
							name1 = lprov.getColumnText( e1, 1 );
							name2 = lprov.getColumnText( e2, 1 );
						}
					}
					else
					{
						if ( descent )
						{
							name1 = lprov.getColumnText( e2, 0 );
							name2 = lprov.getColumnText( e1, 0 );
						}
						else
						{
							name1 = lprov.getColumnText( e1, 0 );
							name2 = lprov.getColumnText( e2, 0 );
						}
					}
				}
				else
				{
					if ( descent )
					{
						name1 = e2.toString( );
						name2 = e1.toString( );
					}
					else
					{
						name1 = e1.toString( );
						name2 = e2.toString( );
					}
				}
			}
			if ( name1 == null )
			{
				name1 = ""; //$NON-NLS-1$
			}
			if ( name2 == null )
			{
				name2 = ""; //$NON-NLS-1$
			}

			return collator.compare( name1, name2 );
		}
	}

	/**
	 * The constructor.
	 * 
	 * @param parentShell
	 * @param title
	 */
	public ResourceEditDialog( Shell parentShell, String title )
	{
		super( parentShell, title );

		setShellStyle( SWT.CLOSE
				| SWT.TITLE
				| SWT.BORDER
				| SWT.APPLICATION_MODAL
				| SWT.RESIZE );

		listChanged = false;

	}

	/**
	 * Set the resource file URL. The url is computed by Model.
	 * 
	 * @param url
	 */
	public void setResourceURL( URL url )
	{
		this.resourceURL = url;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#create()
	 */
	public void create( )
	{
		super.create( );

		Point pt = getShell( ).computeSize( -1, -1 );
		pt.y = Math.max( pt.y, 400 );
		getShell( ).setSize( pt );

		updateButtonState( );
	}

	/**
	 * Loads the key/value from message file.
	 */
	private void loadMessage( )
	{
		if ( this.resourceURL != null )
		{
			try
			{
				InputStream in = this.resourceURL.openStream( );
				content.load( in );
				in.close( );
				propFileName = DEUtil.getFilePathFormURL( resourceURL );
			}
			catch ( Exception e )
			{
				ExceptionHandler.handle( e );
			}
		}
	}

	/**
	 * Save the key/value to message file, if the file not exists, create it.
	 */
	private boolean saveMessage( )
	{
		if ( this.resourceURL != null )
		{
			if ( this.resourceURL.getProtocol( ).equals( "file" ) //$NON-NLS-1$
					&& listChanged )
			{
				try
				{
					return saveFile( DEUtil.getFilePathFormURL( resourceURL ) );
				}
				catch ( Exception e )
				{
					ExceptionHandler.handle( e );
					return false;
				}
			}
			else
			{
				return true;
			}
		}

		return false;
	}

	private boolean saveFile( String filePath )
	{
		File f = new File( filePath );
		if ( !( f.exists( ) && f.isFile( ) ) )
		{
			MessageDialog.openError( getShell( ),
					Messages.getString( "ResourceEditDialog.NotFile.Title" ), //$NON-NLS-1$
					Messages.getFormattedString( "ResourceEditDialog.NotFile.Message", //$NON-NLS-1$
							new Object[]{
								propFileName
							} ) );
			return false;
		}
		else if ( !f.canWrite( ) )
		{
			MessageDialog.openError( getShell( ),
					Messages.getString( "ResourceEditDialog.ReadOnlyEncounter.Title" ), //$NON-NLS-1$
					Messages.getFormattedString( "ResourceEditDialog.ReadOnlyEncounter.Message", //$NON-NLS-1$
							new Object[]{
								propFileName
							} ) );
			return false;
		}

		try
		{
			if ( f.canWrite( ) )
			{
				FileOutputStream fos = new FileOutputStream( f );

				content.store( fos, "" ); //$NON-NLS-1$

				fos.close( );
			}
			return true;
		}
		catch ( Exception e )
		{
			ExceptionHandler.handle( e );
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets
	 * .Composite)
	 */
	protected Control createDialogArea( Composite parent )
	{
		UIUtil.bindHelp( parent, IHelpContextIds.RESOURCE_EDIT_DIALOG_ID );
		loadMessage( );

		final Composite innerParent = (Composite) super.createDialogArea( parent );

		nameLabel = new Label( innerParent, SWT.NONE );
		nameLabel.setText( Messages.getString( "ResourceEditDialog.message.ResourceFile" ) + propFileName ); //$NON-NLS-1$
		nameLabel.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );

		final Table table = new Table( innerParent, SWT.BORDER
				| SWT.FULL_SELECTION );
		GridData data = new GridData( GridData.FILL_BOTH );
		table.setLayoutData( data );

		table.setHeaderVisible( true );
		table.setLinesVisible( true );

		TableLayout tableLayout = new TableLayout( );
		table.setLayout( tableLayout );

		final TableColumn column1 = new TableColumn( table, SWT.NONE );
		column1.setText( Messages.getString( "ResourceEditDialog.text.Key.TableColumn" ) ); //$NON-NLS-1$
		column1.addSelectionListener( new SelectionAdapter( ) {

			public void widgetSelected( SelectionEvent e )
			{
				table.setSortColumn( column1 );
				viewer.setSorter( new ResourceSorter( table.getSortDirection( ) == SWT.UP,
						false ) );
				table.setSortDirection( table.getSortDirection( ) == SWT.UP ? SWT.DOWN
						: SWT.UP );
			}
		} );

		final TableColumn column2 = new TableColumn( table, SWT.NONE );
		column2.setText( Messages.getString( "ResourceEditDialog.text.Value.TableColumn" ) ); //$NON-NLS-1$
		column2.addSelectionListener( new SelectionAdapter( ) {

			public void widgetSelected( SelectionEvent e )
			{
				table.setSortColumn( column2 );
				viewer.setSorter( new ResourceSorter( table.getSortDirection( ) == SWT.UP,
						true ) );
				table.setSortDirection( table.getSortDirection( ) == SWT.UP ? SWT.DOWN
						: SWT.UP );
			}
		} );

		viewer = new TableViewer( table );
		viewer.setContentProvider( new IStructuredContentProvider( ) {

			public Object[] getElements( Object inputElement )
			{
				if ( inputElement instanceof Properties )
				{
					return ( (Properties) inputElement ).entrySet( ).toArray( );
				}

				return new Object[0];
			}

			public void inputChanged( Viewer viewer, Object oldInput,
					Object newInput )
			{
			}

			public void dispose( )
			{
			}
		} );
		viewer.setLabelProvider( new PropertyLabelProvider( ) );

		viewer.addSelectionChangedListener( new ISelectionChangedListener( ) {

			public void selectionChanged( SelectionChangedEvent event )
			{
				updateSelection( );
			}
		} );

		innerParent.addControlListener( new ControlAdapter( ) {

			// Resize the columns by proportion when the parent control is
			// resized.
			public void controlResized( ControlEvent e )
			{
				Rectangle area = innerParent.getClientArea( );
				Point preferredSize = table.computeSize( SWT.DEFAULT,
						SWT.DEFAULT );
				int width = area.width - 2 * table.getBorderWidth( );
				if ( preferredSize.y > area.height )
				{
					Point vBarSize = table.getVerticalBar( ).getSize( );
					width -= vBarSize.x;
				}
				Point oldSize = table.getSize( );
				if ( oldSize.x > width )
				{
					column1.setWidth( 2 * width / 5 );
					column2.setWidth( width - column1.getWidth( ) );
					table.setSize( width, area.height );
				}
				else
				{
					table.setSize( width, area.height );
					column1.setWidth( 2 * width / 5 );
					column2.setWidth( width - column1.getWidth( ) );
				}
			}
		} );

		table.setSortColumn( column1 );
		table.setSortDirection( SWT.UP );
		viewer.setSorter( new ResourceSorter( false, false ) );

		Group gp = new Group( innerParent, SWT.NONE );
		gp.setText( Messages.getString( "ResourceEditDialog.text.QuickAdd" ) ); //$NON-NLS-1$
		gp.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
		gp.setLayout( new GridLayout( 6, false ) );

		Label lb = new Label( gp, 0 );
		lb.setText( Messages.getString( "ResourceEditDialog.text.Key" ) ); //$NON-NLS-1$

		keyText = new Text( gp, SWT.BORDER | SWT.SINGLE );
		keyText.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );

		lb = new Label( gp, 0 );
		lb.setText( Messages.getString( "ResourceEditDialog.text.Value" ) ); //$NON-NLS-1$

		// lb = new Label( gp, 0 );
		// lb = new Label( gp, 0 );
		valueText = new Text( gp, SWT.BORDER | SWT.SINGLE );
		valueText.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );

		btnAdd = new Button( gp, SWT.PUSH );
		btnAdd.setText( Messages.getString( "ResourceEditDialog.text.Add" ) ); //$NON-NLS-1$
		btnAdd.addSelectionListener( new SelectionAdapter( ) {

			public void widgetSelected( SelectionEvent e )
			{
				addSelection( );
			}
		} );
		btnAdd.setEnabled( isFileSystemFile( ) );

		btnDelete = new Button( gp, SWT.PUSH );
		btnDelete.setText( Messages.getString( "ResourceEditDialog.text.Delete" ) ); //$NON-NLS-1$
		btnDelete.addSelectionListener( new SelectionAdapter( ) {

			public void widgetSelected( SelectionEvent e )
			{
				deleteSelection( );
			}
		} );

		lb = new Label( innerParent, 0 );
		lb.setText( Messages.getString( "ResourceEditDialog.message.AddNote" ) ); //$NON-NLS-1$
		lb.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );

		viewer.setInput( content );

		return innerParent;
	}

	private void updateSelection( )
	{
		if ( viewer.getTable( ).getSelectionCount( ) > 0 )
		{
			keyText.setText( viewer.getTable( ).getSelection( )[0].getText( 0 ) );
			valueText.setText( viewer.getTable( ).getSelection( )[0].getText( 1 ) );
		}

		updateButtonState( );
	}

	private void addSelection( )
	{
		// if the file is read-only then change is not allowed.
		File f = new File( propFileName );
		if ( !( f.exists( ) && f.isFile( ) ) )
		{
			MessageDialog.openError( getShell( ),
					Messages.getString( "ResourceEditDialog.NotFile.Title" ), //$NON-NLS-1$
					Messages.getFormattedString( "ResourceEditDialog.NotFile.Message", //$NON-NLS-1$
							new Object[]{
								propFileName
							} ) );
			return;
		}
		else if ( !f.canWrite( ) )
		{
			MessageDialog.openError( getShell( ),
					Messages.getString( "ResourceEditDialog.ReadOnlyEncounter.Title" ), //$NON-NLS-1$
					Messages.getFormattedString( "ResourceEditDialog.ReadOnlyEncounter.Message", //$NON-NLS-1$
							new Object[]{
								propFileName
							} ) );
			return;
		}

		String key = keyText.getText( );
		String val = valueText.getText( );

		if ( key != null && key.trim( ).length( ) > 0 )
		{
			content.put( key, val );

			viewer.setInput( content );

			listChanged = true;

			updateSelection( );
		}
		else
		{
			MessageDialog.openWarning( getShell( ),
					Messages.getString( "ResourceEditDialog.text.AddWarningTitle" ), //$NON-NLS-1$
					Messages.getString( "ResourceEditDialog.text.AddWarningMsg" ) ); //$NON-NLS-1$
		}
	}

	private void deleteSelection( )
	{
		if ( viewer.getTable( ).getSelectionIndex( ) == -1 )
		{
			return;
		}
		// if the file is read-only then change is not allowed.
		File f = new File( propFileName );
		if ( !( f.exists( ) && f.isFile( ) ) )
		{
			MessageDialog.openError( getShell( ),
					Messages.getString( "ResourceEditDialog.NotFile.Title" ), //$NON-NLS-1$
					Messages.getFormattedString( "ResourceEditDialog.NotFile.Message", //$NON-NLS-1$
							new Object[]{
								propFileName
							} ) );
			return;
		}
		else if ( !f.canWrite( ) )
		{
			MessageDialog.openError( getShell( ),
					Messages.getString( "ResourceEditDialog.ReadOnlyEncounter.Title" ), //$NON-NLS-1$
					Messages.getFormattedString( "ResourceEditDialog.ReadOnlyEncounter.Message", //$NON-NLS-1$
							new Object[]{
								propFileName
							} ) );
			return;
		}

		listChanged = true;

		String key = keyText.getText( );

		content.remove( key );

		viewer.getTable( ).remove( viewer.getTable( ).getSelectionIndex( ) );

		updateSelection( );

	}

	private void updateButtonState( )
	{
		getOkButton( ).setEnabled( viewer.getTable( ).getSelectionCount( ) > 0 );

		btnDelete.setEnabled( isFileSystemFile( )
				&& viewer.getTable( ).getSelectionIndex( ) != -1 );
	}

	private boolean isFileSystemFile( )
	{
		return this.resourceURL == null
				|| this.resourceURL != null
				&& this.resourceURL.getProtocol( ).equals( "file" ); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed( )
	{
		if ( saveMessage( ) == true )
		{
			setResult( viewer.getTable( ).getSelection( )[0].getText( 0 ) );
			setDetailResult( new String[]{
					viewer.getTable( ).getSelection( )[0].getText( 0 ),
					viewer.getTable( ).getSelection( )[0].getText( 1 )
			} );
		}

		super.okPressed( );
	}

	public Object getDetailResult( )
	{
		return detailResult;
	}

	private Object detailResult;

	final protected void setDetailResult( Object value )
	{
		detailResult = value;
	}

	public boolean isKeyValueListChanged( )
	{
		return listChanged;
	}
}
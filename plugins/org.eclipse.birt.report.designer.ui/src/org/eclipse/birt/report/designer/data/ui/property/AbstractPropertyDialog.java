/*******************************************************************************
 * Copyright (c) 2004 Actuate Corporation. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Actuate Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.birt.report.designer.data.ui.property;

import org.eclipse.birt.report.designer.internal.ui.dialogs.BaseDialog;
import org.eclipse.birt.report.designer.ui.dialogs.properties.IPropertyPage;
import org.eclipse.birt.report.designer.ui.dialogs.properties.IPropertyPageContainer;
import org.eclipse.jface.dialogs.DialogMessageArea;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

import com.ibm.icu.util.StringTokenizer;

/**
 * This is the base claas for a dialog box with a tree on the left hand side and
 * pages for each node on the right. It takes a model object in the constructor
 * and maintains a reference to this object. The pages can retrieve this model
 * object by calling the {@link #getModel() getModel}method. It defines two
 * abstract methods {@link #performCancel() performCancel}and
 * {@link #performOk() performOk}. These methods are called when the user
 * presses the Ok or cancel button. <br/><br/>
 * 
 * Pages can be added to this by either calling the
 * {@link #addNodeTo(String, PropertyNode) addNodeTo}method or by calling the
 * {@link #addPageTo(String, String, String, Image, IPropertyPage) addPageTo}
 * method.
 * 
 * @version $Revision: 1.5 $ $Date: 2007/03/21 06:59:18 $
 */

public abstract class AbstractPropertyDialog extends BaseDialog
		implements
			IPropertyPageContainer
{

	private transient Object modelObject = null;

	protected transient PropertyNode rootNode = null;

	private transient StackLayout propertyPaneLayout = null;

	private transient Composite propertyPane = null;

	private transient PropertyNode currentNode = null;

	private transient boolean processSelection = true;

	private transient Label titleImage = null;

	protected TreeViewer viewer = null;

	private DialogMessageArea messageArea = null;

	private String nodeId;
	
	protected boolean showPage = false;

	/**
	 * The only constructor for this dialog. It takes the parentShell and the
	 * model object as parameters.
	 * 
	 * @param parentShell
	 *            The parent shell
	 * @param model
	 *            The model object that the dialog pages will interact with.
	 */
	public AbstractPropertyDialog( Shell parentShell, Object model )
	{
		super( parentShell, "" ); //$NON-NLS-1$
		setModel( model );
		setShellStyle( getShellStyle( ) | SWT.RESIZE | SWT.MAX );
		propertyPaneLayout = new StackLayout( );
		rootNode = new PropertyNode(
				"org.eclipse.birt.report.designer.ui.dialogs.properties.propertydialog.rootnode" ); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.ui.IPropertyPageContainer#setModel(java.lang.Object)
	 */
	public void setModel( Object model )
	{
		modelObject = model;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.ui.IPropertyPageContainer#getModel()
	 */
	public final Object getModel( )
	{
		return modelObject;
	}

	/**
	 * Adds a node to the dialog at the specified path The path needs to point
	 * to an existing node or to the root element. The path elements are
	 * separated by the "/" character. If the path is null then the node is just
	 * added to the root. For e.g. In order to create the following tree
	 * structure: <br/>
	 * 
	 * <pre>
	 * 
	 *  
	 *   
	 *    
	 *     
	 *           Prop1
	 *           |_
	 *             Prop2
	 *      
	 *     
	 *    
	 *   
	 *  
	 * </pre>
	 * 
	 * <br/>The api needs to be called as follows: <br/><code>
	 *  addNodeTo("/", Prop1Node);<br/>
	 *  addNodeTo("/Prop1", Prop2Node);<br/>
	 * </code>
	 * Prop1 and Prop2 being the node ids of the respective nodes.
	 * 
	 * @param path
	 *            The path under which the new node is to be added.
	 * @param node
	 *            The node to be added.
	 */
	public final void addNodeTo( String path, PropertyNode node )
	{
		node.setContainer( this );
		if ( path == null || path.trim( ).equals( "/" ) ) //$NON-NLS-1$
		{
			rootNode.add( node );
		}
		else
		{
			PropertyNode parentNode = getNode( path );
			if ( parentNode != null )
			{
				parentNode.add( node );
			}
		}
	}

	/**
	 * This is a convenience method that can be used to add the PropertyPage to
	 * the dialog. In turn it creates a PropertyNode object and calls the
	 * {@link #addNodeTo(String, PropertyNode) addNodeTo}method.
	 * 
	 * @param path
	 *            The path under which the new page is to be added.
	 * @param nodeId
	 *            The unique id to be assigned to the node
	 * @param nodeLabel
	 *            The label for the node, If this is null then the page name is
	 *            used
	 * @param nodeImage
	 *            The image for the node if any. If this is null then the image
	 *            returned by the page is used
	 * @param page
	 *            The page to be added to the dialog.
	 */
	public final void addPageTo( String path, String nodeId, String nodeLabel,
			Image nodeImage, IPropertyPage page )
	{
		PropertyNode node = new PropertyNode( nodeId, nodeLabel, nodeImage,
				page );
		addNodeTo( path, node );
	}

	/**
	 * Looks up the node based on the path it is givean
	 * 
	 * @param path
	 *            The complete path to the target node.
	 * @return The Node object or null if it is not found.
	 */
	private final PropertyNode getNode( String path )
	{
		PropertyNode currentNode = null;
		if ( path != null )
		{
			path = path.trim( );
			currentNode = rootNode;
			StringTokenizer tokenizer = new StringTokenizer( path, "/" ); //$NON-NLS-1$
			while ( tokenizer.hasMoreTokens( ) )
			{
				currentNode = currentNode.getSubNode( tokenizer.nextToken( ) );
				if ( currentNode == null )
				{
					return null;
				}
			}

		}
		return currentNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea( Composite parent )
	{
		Composite composite = new Composite( parent, SWT.NONE );
		composite.setLayoutData( new GridData( GridData.FILL_BOTH ) );
		GridLayout layout = new GridLayout( );
		layout.numColumns = 1;
		layout.marginWidth = convertHorizontalDLUsToPixels( IDialogConstants.HORIZONTAL_MARGIN );
		layout.marginHeight = convertVerticalDLUsToPixels( IDialogConstants.VERTICAL_MARGIN );
		layout.verticalSpacing = convertVerticalDLUsToPixels( IDialogConstants.VERTICAL_SPACING );
		composite.setLayout( layout );

		SashForm sashForm = new SashForm( composite, SWT.NONE );
		sashForm.setOrientation( SWT.HORIZONTAL );
		sashForm.setLayoutData( new GridData( GridData.FILL_BOTH ) );
		createTreeViewer( sashForm );

		createPropertyPane( sashForm );

		sashForm.setWeights( new int[]{20, 80} );
		Label label = new Label( composite, SWT.HORIZONTAL | SWT.SEPARATOR );
		label.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );

		initTreeSelection( );

		return composite;
	}

	/**
	 * For the first node of tree is not default selected as it is under
	 * Windows, this method set the first node to be selected for Linux.
	 */
	private void initTreeSelection( )
	{
		PropertyNode node = getNode( getDeafultNode( ) );
		if ( node != null )
		{
			StructuredSelection select = new StructuredSelection( node );
			viewer.setSelection( select );
		}
		else
		{
			if(viewer.getTree( ).getItems( ).length>0)
			{
				TreeItem firstNode = viewer.getTree( ).getItems( )[0];
				StructuredSelection select = new StructuredSelection( firstNode.getData( ) );
				viewer.setSelection( select );
			}
		}
	}

	private final Control createTreeViewer( Composite parent )
	{
		viewer = new TreeViewer( parent );
		viewer.addSelectionChangedListener( new ISelectionChangedListener( ) {

			public void selectionChanged( SelectionChangedEvent event )
			{
				final PropertyNode selectedNode = (PropertyNode) ( (IStructuredSelection) event
						.getSelection( ) ).getFirstElement( );
				if ( processSelection )
				{
					BusyIndicator.showWhile( getShell( ).getDisplay( ),
							new Runnable( ) {

								public void run( )
								{
									showSelectionPage( selectedNode );
								}
							} );
				}
				processSelection = true;
			}
		} );

		viewer.getTree( ).addMouseTrackListener( new MouseTrackAdapter( ) {

			public void mouseHover( MouseEvent event )
			{
				Widget widget = event.widget;
				if ( widget == viewer.getTree( ) )
				{
					Point pt = new Point( event.x, event.y );
					TreeItem item = viewer.getTree( ).getItem( pt );
					if ( item == null )
						viewer.getTree( ).setToolTipText( "" );//$NON-NLS-1$
					else
					{
						String text = null;
						if ( item.getData( ) instanceof PropertyNode )
						{
							text = ( (PropertyNode) item.getData( ) ).getPage( )
									.getToolTip( );
						}

						viewer.getTree( ).setToolTipText( text );
					}
				}
			}
		} );

		viewer.setContentProvider( new PropertyContentProvider( ) );
		viewer.setLabelProvider( new PropertyLabelProvider( ) );
		viewer.setInput( rootNode );
		return viewer.getTree( );
	}

	public void showSelectionPage( PropertyNode selectedNode )
	{
		showPage = showPage( selectedNode );
		if ( !showPage )
		{
			processSelection = false;
			viewer.setSelection( new StructuredSelection( currentNode ) );
		}
	}

	private final Composite createPropertyPane( Composite parent )
	{
		//If the title is visible
		//create the title area.
		Composite propertyContainer = new Composite( parent, SWT.NONE );
		GridLayout layout = new GridLayout( );
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		propertyContainer.setLayout( layout );
		
		Composite titleComposite = new Composite( propertyContainer, SWT.NONE );
		layout = new GridLayout( );
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		titleComposite.setLayout( layout );
		GridData titleLayoutData = new GridData( GridData.FILL_HORIZONTAL );
		titleLayoutData.horizontalIndent = IDialogConstants.HORIZONTAL_MARGIN;
		titleComposite.setLayoutData( titleLayoutData );

		if ( isTitleVisible( ) )
		{
			createTitleArea( titleComposite );

			Label separator = new Label( propertyContainer, SWT.HORIZONTAL
					| SWT.SEPARATOR );
			separator.setLayoutData( new GridData( GridData.FILL_HORIZONTAL
					| GridData.GRAB_HORIZONTAL ) );
		}

		GridData data = new GridData( GridData.FILL_BOTH );
		propertyPane = new Composite( propertyContainer, SWT.NONE );
		propertyPane.setLayout( propertyPaneLayout );
		propertyPane.setLayoutData( data );
		return propertyContainer;
	}
	
	/**
	 * Create TitleArea
	 * 
	 * @param parent
	 * @return
	 */
	public Composite createTitleArea( Composite parent )
	{
		Composite messageComposite = new Composite( parent, SWT.NONE );
		GridData data = new GridData( GridData.FILL_HORIZONTAL );
		messageComposite.setLayoutData( data );

		FormLayout messageLayout = new FormLayout( );
		messageComposite.setLayout( messageLayout );
		messageArea = new DialogMessageArea( );
		messageArea.createContents( messageComposite );

		titleImage = new Label( messageComposite, SWT.LEFT );
		FormData imageData = new FormData( );
		imageData.right = new FormAttachment( 100 );
		imageData.top = new FormAttachment( 0 );
		imageData.bottom = new FormAttachment( 100 );
		titleImage.setLayoutData( imageData );

		FormData messageData = new FormData( );
		messageData.left = new FormAttachment( 0, 0 );
		messageData.right = new FormAttachment( titleImage, 0 );
		messageData.bottom = new FormAttachment( titleImage, 0, SWT.BOTTOM );
		messageData.top = new FormAttachment( 0, 0 );
		messageArea.setTitleLayoutData( messageData );
		messageArea.setMessageLayoutData( messageData );

		return messageComposite;
	}
	

	private final boolean showPage( PropertyNode node )
	{
		if ( currentNode != null )
		{
			// First check whether we can change the page.
			if ( node == currentNode || !currentNode.getPage( ).canLeave( ) )
			{
				return false;
			}
		}
		//Check whether the page control has been created
		if ( !node.isPageControlCreated( ) )
		{
			//if not create it
			Control control = node.createPageControl( propertyPane );
			assert ( control != null ) : "Control has not been created for node " + node.getId( ); //$NON-NLS-1$
		}
		currentNode = node;
		//show the page control
		propertyPaneLayout.topControl = node.getPageControl( );
		//Call the activated method to indicate that this page is activated
		node.getPage( ).pageActivated( );
		viewer.setSelection( new StructuredSelection( node ) );
		propertyPane.layout( );
		return true;

	}

	/**
	 * 
	 * @return
	 */
	protected Composite getPropertyPane( )
	{
		return this.propertyPane;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	protected final void buttonPressed( int buttonId )
	{
		switch ( buttonId )
		{
			case IDialogConstants.OK_ID :
			{
				okPressed( );
				break;
			}
			case IDialogConstants.CANCEL_ID :
			{
				cancelPressed( );
				break;
			}
			case IDialogConstants.HELP_ID :
			{
				performHelp( );
				break;
			}
			default :
			{
				super.buttonPressed( buttonId );
			}
		}
	}

	/**
	 * Called when the user pressed the Ok button. It is called only if all the
	 * pages return true for their individual
	 * {@link org.eclipse.birt.report.designer.data.ui.property.IPropertyPage#performOk() performOk}
	 * methods. Derived classes can save the contents of the dialog and perform
	 * any validation if necessary on the contents. <br/>The dialog is closed if
	 * this method returns true.
	 * 
	 * @return boolean indicating whether the method was successful or not.
	 */
	public abstract boolean performOk( );

	/**
	 * Called when the user presses the Cancel button. It is called only if all
	 * the pages return true for their individual
	 * {@link org.eclipse.birt.report.designer.data.ui.property.IPropertyPage#performCancel() performCancel()}
	 * methods. <br/>The dialog is closed if this method returns true.
	 * 
	 * @return boolean indicating whether the method was successful or not.
	 */
	public abstract boolean performCancel( );

	private final void performHelp( )
	{
		//Call the perform help of the current page
		if ( currentNode != null )
		{
			currentNode.getPage( ).performHelp( );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
	 */
	protected final void cancelPressed( )
	{
		//First call cancel on all the pages
		if ( rootNode.hasSubNodes( ) )
		{
			PropertyNode[] nodes = rootNode.getSubNodes( );
			for ( int n = 0; n < nodes.length; n++ )
			{
				//Check whether the current page can be cancelled
				if ( !cancelPressed( nodes[n] ) )
				{
					return;
				}
			}
		}

		//Finally call it on the dialog
		if ( performCancel( ) )
		{
			super.cancelPressed( );
		}
	}

	private final boolean cancelPressed( PropertyNode node )
	{
		if ( node.getPage( ).performCancel( ) )
		{
			//if this node allows performing the cancel operation
			//check the child nodes
			if ( node.hasSubNodes( ) )
			{
				PropertyNode[] nodes = node.getSubNodes( );
				for ( int n = 0; n < nodes.length; n++ )
				{
					//Check whether the current page can be cancelled
					if ( !cancelPressed( nodes[n] ) )
					{
						return false;
					}
				}
			}
			//If we have come here then we know that all the nodes have
			//successfully allowed cancelling
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed( )
	{
		//First call ok on all the pages
		if ( rootNode.hasSubNodes( ) )
		{
			PropertyNode[] nodes = rootNode.getSubNodes( );
			for ( int n = 0; n < nodes.length; n++ )
			{
				//Check whether the current page can be closed
				if ( !okPressed( nodes[n] ) )
				{
					return;
				}
			}
		}
		//Finally call it on the dialog
		if ( performOk( ) )
		{
			super.okPressed( );
		}
	}

	private final boolean okPressed( PropertyNode node )
	{
		if ( node.getPage( ).performOk( ) )
		{
			//if this node allows performing the cancel operation
			//check the child nodes
			if ( node.hasSubNodes( ) )
			{
				PropertyNode[] nodes = node.getSubNodes( );
				for ( int n = 0; n < nodes.length; n++ )
				{
					//Check whether the current page can be cancelled
					if ( !okPressed( nodes[n] ) )
					{
						return false;
					}
				}
			}
			//If we have come here then we know that all the nodes have
			//successfully allowed cancelling
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#handleShellCloseEvent()
	 */
	protected void handleShellCloseEvent( )
	{
		//Same as cancel
		cancelPressed( );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#getInitialSize()
	 */
	protected Point getInitialSize( )
	{
		return new Point( 800, 500 );
	}

	/**
	 * Indicates whether the title area on top of the property page should be
	 * shown or not. It is called only once when the dialog is created. This
	 * default implementation of this method returns true. Sub classes may
	 * override this method to return false if they do not wish to show the
	 * title are.
	 * 
	 * @return boolean indicating whether the title should be displayed or not.
	 */
	protected boolean isTitleVisible( )
	{
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.ui.dialogs.properties.IPropertyPageContainer#setMessage(java.lang.String,
	 *      int)
	 */
	public final void setMessage( String message, int messageType )
	{
		if ( messageArea != null )
		{
			if ( messageType != IMessageProvider.NONE )
			{
				titleImage.setVisible( false );
			}
			else
			{
				titleImage.setVisible( true );
			}
			messageArea.updateText( message, messageType );
		}
	}

	/**
	 * Convenience method to set a message with the message type
	 * IMessageProvider.NONE. It in turn calls the
	 * {@link #setMessage (String, int) setMessage}method.
	 * 
	 * @param message
	 *            The message to display in the title are
	 */
	public final void setMessage( String message )
	{
		setMessage( message, IMessageProvider.NONE );
	}

	/**
	 * @param attribute
	 */
	protected void setDefaultNode( String attribute )
	{
		nodeId = attribute;

	}

	protected String getDeafultNode( )
	{
		return nodeId;
	}
	
	protected PropertyNode getCurrentNode( )
	{
		return currentNode;
	}

}
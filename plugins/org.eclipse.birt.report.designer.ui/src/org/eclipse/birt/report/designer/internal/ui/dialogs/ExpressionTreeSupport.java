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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.birt.report.designer.core.model.SessionHandleAdapter;
import org.eclipse.birt.report.designer.core.model.views.data.DataSetItemModel;
import org.eclipse.birt.report.designer.internal.ui.util.DataSetManager;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.designer.ui.IReportGraphicConstants;
import org.eclipse.birt.report.designer.ui.ReportPlatformUIImages;
import org.eclipse.birt.report.designer.util.DEUtil;
import org.eclipse.birt.report.model.api.DataSetHandle;
import org.eclipse.birt.report.model.api.DesignEngine;
import org.eclipse.birt.report.model.api.ParameterGroupHandle;
import org.eclipse.birt.report.model.api.ParameterHandle;
import org.eclipse.birt.report.model.api.ReportElementHandle;
import org.eclipse.birt.report.model.api.metadata.IArgumentInfo;
import org.eclipse.birt.report.model.api.metadata.IArgumentInfoList;
import org.eclipse.birt.report.model.api.metadata.IClassInfo;
import org.eclipse.birt.report.model.api.metadata.ILocalizableInfo;
import org.eclipse.birt.report.model.api.metadata.IMemberInfo;
import org.eclipse.birt.report.model.api.metadata.IMethodInfo;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.ISharedImages;

/**
 * Deals with tree part of expression builder. Adds some mouse and DND support
 * to tree and corresponding source viewer.
 */

public class ExpressionTreeSupport
{

	//Tree item icon images
	private static final Image IMAGE_FOLDER = ReportPlatformUIImages.getImage( ISharedImages.IMG_OBJ_FOLDER );

	private static final Image IMAGE_OPERATOR = getIconImage( IReportGraphicConstants.ICON_EXPRESSION_OPERATOR );

	private static final Image IMAGE_COLUMN = getIconImage( IReportGraphicConstants.ICON_DATA_COLUMN );

	private static final Image IMAGE_GOLBAL = getIconImage( IReportGraphicConstants.ICON_EXPRESSION_GLOBAL );

	private static final Image IMAGE_METHOD = getIconImage( IReportGraphicConstants.ICON_EXPRESSION_METHOD );

	private static final Image IMAGE_STATIC_METHOD = getIconImage( IReportGraphicConstants.ICON_EXPRESSION_STATIC_METHOD );

	private static final Image IMAGE_MEMBER = getIconImage( IReportGraphicConstants.ICON_EXPRESSION_MEMBER );

	private static final Image IMAGE_STATIC_MEMBER = getIconImage( IReportGraphicConstants.ICON_EXPRESSION_STATIC_MEMBER );

	/** Arithmetic operators and their descriptions */
	private static final String[][] OPERATORS_ASSIGNMENT = new String[][]{
			{
					"=", Messages.getString( "ExpressionBuidler.Operator.Assign" ) //$NON-NLS-1$ //$NON-NLS-2$
			},
			{
					"+=", Messages.getString( "ExpressionBuidler.Operator.AddTo" ) //$NON-NLS-1$ //$NON-NLS-2$
			},
			{
					"-=", Messages.getString( "ExpressionBuidler.Operator.SubFrom" ) //$NON-NLS-1$ //$NON-NLS-2$
			},
			{
					"*=", Messages.getString( "ExpressionBuidler.Operator.MultTo" ) //$NON-NLS-1$ //$NON-NLS-2$
			},
			{
					"/=", Messages.getString( "ExpressionBuidler.Operator.DividingFrom" ) //$NON-NLS-1$ //$NON-NLS-2$
			}
	};

	/** Comparison operators and their descriptions */
	private static final String[][] OPERATORS_COMPARISON = new String[][]{
			{
					"==", Messages.getString( "ExpressionBuidler.Operator.Equals" ) //$NON-NLS-1$ //$NON-NLS-2$
			},
			{
					"<", Messages.getString( "ExpressionBuidler.Operator.Less" ) //$NON-NLS-1$ //$NON-NLS-2$
			},
			{
					"<=",//$NON-NLS-1$
					Messages.getString( "ExpressionBuidler.Operator.LessEqual" ) //$NON-NLS-1$ 
			},
			{
					"!=",//$NON-NLS-1$
					Messages.getString( "ExpressionBuidler.Operator.NotEqual" ) //$NON-NLS-1$ 
			},
			{
					"=", Messages.getString( "ExpressionBuidler.Operator.SingleEquals" ) //$NON-NLS-1$ //$NON-NLS-2$
			},
			{
					">", Messages.getString( "ExpressionBuidler.Operator.Greater" ) //$NON-NLS-1$ //$NON-NLS-2$
			},
			{
					">=",//$NON-NLS-1$
					Messages.getString( "ExpressionBuidler.Operator.GreaterEquals" ) //$NON-NLS-1$ //$NON-NLS-2$
			}
	};

	/** Computational operators and their descriptions */
	private static final String[][] OPERATORS_COMPUTATIONAL = new String[][]{

			{
					"+", Messages.getString( "ExpressionBuidler.Operator.Add" ) //$NON-NLS-1$ //$NON-NLS-2$
			},
			{
					"-", Messages.getString( "ExpressionBuidler.Operator.Sub" ) //$NON-NLS-1$ //$NON-NLS-2$
			},
			{
					"*", Messages.getString( "ExpressionBuidler.Operator.Mult" ) //$NON-NLS-1$ //$NON-NLS-2$
			},
			{
					"/",//$NON-NLS-1$
					Messages.getString( "ExpressionBuidler.Operator.Dvides" ) //$NON-NLS-1$ 
			},
			{
					"++X ",//$NON-NLS-1$
					Messages.getString( "ExpressionBuidler.Operator.Inc" ) //$NON-NLS-1$ 
			},
			{
					"X++ ", Messages.getString( "ExpressionBuidler.Operator.ReturnInc" ) //$NON-NLS-1$ //$NON-NLS-2$
			},
			{
					"--X ", Messages.getString( "ExpressionBuidler.Operator.Dec" ) //$NON-NLS-1$ //$NON-NLS-2$
			},
			{
					"X-- ", Messages.getString( "ExpressionBuidler.Operator.ReturnDec" ) //$NON-NLS-1$ //$NON-NLS-2$
			}
	};

	/** Logical operators and their descriptions */
	private static final String[][] OPERATORS_LOGICAL = new String[][]{
			{
					"&&",//$NON-NLS-1$
					Messages.getString( "ExpressionBuidler.Operator.And" ) //$NON-NLS-1$ 
			}, {
					"||",//$NON-NLS-1$
					Messages.getString( "ExpressionBuidler.Operator.Or" ) //$NON-NLS-1$ 
			}
	};

	private static final String TREE_ITEM_OPERATORS = Messages.getString( "ExpressionBuidler.Tree.Operators" ); //$NON-NLS-1$

	private static final String TREE_ITEM_FUNCTIONS = Messages.getString( "ExpressionBuidler.Tree.Functions" ); //$NON-NLS-1$ 

	private static final String TREE_ITEM_DATASETS = Messages.getString( "ExpressionBuidler.Tree.DataSets" ); //$NON-NLS-1$

	private static final String TREE_ITEM_PARAMETERS = Messages.getString( "ExpressionBuidler.Tree.Parameters" ); //$NON-NLS-1$

	private static final String TREE_ITEM_OBJECTS = Messages.getString( "ExpressionBuidler.Tree.Objects" ); //$NON-NLS-1$

	private static final String TREE_ITEM_LOGICAL = Messages.getString( "ExpressionBuidler.Tree.Logical" ); //$NON-NLS-1$

	private static final String TREE_ITEM_COMPUTATIONAL = Messages.getString( "ExpressionBuidler.Tree.Computational" ); //$NON-NLS-1$

	private static final String TREE_ITEM_COMPARISON = Messages.getString( "ExpressionBuidler.Tree.Comparison" ); //$NON-NLS-1$

	private static final String TREE_ITEM_ASSIGNMENT = Messages.getString( "ExpressionBuidler.Tree.Assignment" ); //$NON-NLS-1$	

	/** Tool tip key of tree item data */
	protected static final String ITEM_DATA_KEY_TOOLTIP = "TOOL_TIP"; //$NON-NLS-1$
	/**
	 * Text key of tree item data, this data is the text string to be inserted
	 * into the text area
	 */
	protected static final String ITEM_DATA_KEY_TEXT = "TEXT"; //$NON-NLS-1$
	protected static final String ITEM_DATA_KEY_ENABLED = "ENABLED"; //$NON-NLS-1$

	private static final String OBJECTS_TYPE_NATIVE = "native";//$NON-NLS-1$
	private static final String OBJECTS_TYPE_BIRT = "birt";//$NON-NLS-1$

	private SourceViewer expressionViewer;

	private Tree tree;

	private DropTarget dropTarget;

	/**
	 * Creates all expression trees in default order
	 * 
	 * @param dataSetList
	 *            list for DataSet tree
	 */
	public void createDefaultExpressionTree( List dataSetList )
	{
		createDataSetsTree( dataSetList );
		createParamtersTree( );
		createNativeObjectsTree( );
		createBirtObjectsTree( );
		createOperatorsTree( );
	}

	/**
	 * Create operators band.Must set Tree before execution.
	 *  
	 */
	public void createOperatorsTree( )
	{
		Assert.isNotNull( tree );
		TreeItem topItem = createTopTreeItem( tree, TREE_ITEM_OPERATORS );
		TreeItem subItem = createSubFolderItem( topItem, TREE_ITEM_ASSIGNMENT );
		createSubTreeItems( subItem, OPERATORS_ASSIGNMENT, IMAGE_OPERATOR );
		subItem = createSubFolderItem( topItem, TREE_ITEM_COMPARISON );
		createSubTreeItems( subItem, OPERATORS_COMPARISON, IMAGE_OPERATOR );
		subItem = createSubFolderItem( topItem, TREE_ITEM_COMPUTATIONAL );
		createSubTreeItems( subItem, OPERATORS_COMPUTATIONAL, IMAGE_OPERATOR );
		subItem = createSubFolderItem( topItem, TREE_ITEM_LOGICAL );
		createSubTreeItems( subItem, OPERATORS_LOGICAL, IMAGE_OPERATOR );
	}

	/**
	 * Create native object band.Must set Tree before execution.
	 *  
	 */
	public void createNativeObjectsTree( )
	{
		Assert.isNotNull( tree );
		TreeItem topItem = createTopTreeItem( tree, TREE_ITEM_FUNCTIONS );
		createObjects( topItem, OBJECTS_TYPE_NATIVE );
	}

	/**
	 * Create parameters band. Must set Tree before execution.
	 *  
	 */
	public void createParamtersTree( )
	{
		Assert.isNotNull( tree );
		TreeItem topItem = createTopTreeItem( tree, TREE_ITEM_PARAMETERS );
		for ( Iterator iterator = SessionHandleAdapter.getInstance( )
				.getReportDesignHandle( )
				.getParameters( )
				.iterator( ); iterator.hasNext( ); )
		{
			ReportElementHandle handle = (ReportElementHandle) iterator.next( );
			if ( handle instanceof ParameterHandle )
			{
				createSubTreeItem( topItem,
						handle.getDisplayLabel( ),
						ReportPlatformUIImages.getImage( handle ),
						DEUtil.getExpression( handle ),
						( (ParameterHandle) handle ).getHelpText( ),
						true );
			}
			else if ( handle instanceof ParameterGroupHandle )
			{
				TreeItem groupItem = createSubTreeItem( topItem,
						handle.getDisplayLabel( ),
						ReportPlatformUIImages.getImage( handle ),
						true );
				for ( Iterator itor = ( (ParameterGroupHandle) handle ).getParameters( )
						.iterator( ); itor.hasNext( ); )
				{
					ParameterHandle parameter = (ParameterHandle) itor.next( );
					createSubTreeItem( groupItem,
							parameter.getDisplayLabel( ),
							ReportPlatformUIImages.getImage( handle ),
							DEUtil.getExpression( parameter ),
							parameter.getDisplayLabel( ),
							true );
				}
			}
		}
	}

	/**
	 * Create data sets band.Must set Tree before execution.
	 *  
	 */
	public void createDataSetsTree( List dataSetList )
	{
		Assert.isNotNull( tree );
		TreeItem topItem = createTopTreeItem( tree, TREE_ITEM_DATASETS );
		for ( Iterator iterator = dataSetList.iterator( ); iterator.hasNext( ); )
		{
			DataSetHandle handle = (DataSetHandle) iterator.next( );
			TreeItem dataSetItem = createSubTreeItem( topItem,
					handle.getDisplayLabel( ),
					ReportPlatformUIImages.getImage( handle ),
					true );
			DataSetItemModel[] columns = DataSetManager.getCurrentInstance( )
					.getColumns( handle, false );
			for ( int i = 0; i < columns.length; i++ )
			{
				createSubTreeItem( dataSetItem,
						columns[i].getDisplayName( ),
						IMAGE_COLUMN,
						DEUtil.getExpression( columns[i] ),
						columns[i].getHelpText( ),
						true );
			}
		}
	}

	/**
	 * Creates birt object tree. Must set Tree before execution.
	 *  
	 */
	public void createBirtObjectsTree( )
	{
		Assert.isNotNull( tree );
		TreeItem topItem = createTopTreeItem( tree, TREE_ITEM_OBJECTS );
		createObjects( topItem, OBJECTS_TYPE_BIRT );
	}

	/**
	 * Creates a top tree item
	 * 
	 * @param parent
	 * @param text
	 * @return tree item
	 */
	private TreeItem createTopTreeItem( Tree parent, String text )
	{
		TreeItem item = new TreeItem( parent, SWT.NONE );
		item.setText( text );
		item.setImage( IMAGE_FOLDER );
		item.setData( ITEM_DATA_KEY_TOOLTIP, "" );//$NON-NLS-1$
		return item;
	}

	private TreeItem createSubTreeItem( TreeItem parent, String text,
			Image image, boolean isEnabled )
	{
		return createSubTreeItem( parent, text, image, null, text, isEnabled );
	}

	private TreeItem createSubTreeItem( TreeItem parent, String text,
			Image image, String textData, String toolTip, boolean isEnabled )
	{
		TreeItem item = new TreeItem( parent, SWT.NONE );
		item.setText( text );
		if ( image != null )
		{
			item.setImage( image );
		}
		item.setData( ITEM_DATA_KEY_TOOLTIP, toolTip );
		item.setData( ITEM_DATA_KEY_TEXT, textData );
		item.setData( ITEM_DATA_KEY_ENABLED, new Boolean( isEnabled ) );
		return item;
	}

	private TreeItem createSubFolderItem( TreeItem parent, String text )
	{
		return createSubTreeItem( parent, text, IMAGE_FOLDER, true );
	}
	
	private TreeItem createSubFolderItem( TreeItem parent, IClassInfo classInfo )
	{
		return createSubTreeItem( parent,
				classInfo.getDisplayName( ),
				IMAGE_FOLDER,
				null,
				classInfo.getToolTip( ),
				true );
	}

	private void createSubTreeItems( TreeItem parent, String[][] texts,
			Image image )
	{
		for ( int i = 0; i < texts.length; i++ )
		{
			createSubTreeItem( parent,
					texts[i][0],
					image,
					texts[i][0],
					texts[i][1],
					true );
		}
	}

	/**
	 * Adds mouse track listener.Must set Tree before execution.
	 *  
	 */
	public void addMouseTrackListener( )
	{
		Assert.isNotNull( tree );
		tree.addMouseTrackListener( new MouseTrackAdapter( ) {

			public void mouseHover( MouseEvent event )
			{
				Widget widget = event.widget;
				if ( widget == tree )
				{
					Point pt = new Point( event.x, event.y );
					TreeItem item = tree.getItem( pt );
					if ( item == null )
						tree.setToolTipText( "" );//$NON-NLS-1$
					else
					{
						String text = (String) item.getData( ITEM_DATA_KEY_TOOLTIP );
						tree.setToolTipText( text );
					}
				}
			}
		} );
	}

	/**
	 * Add double click behaviour. Must set Tree before execution.
	 *  
	 */
	public void addMouseListener( )
	{
		Assert.isNotNull( tree );
		tree.addMouseListener( new MouseAdapter( ) {

			public void mouseDoubleClick( MouseEvent event )
			{
				TreeItem[] selection = getTreeSelection( );
				if ( selection == null || selection.length <= 0 )
					return;
				TreeItem item = selection[0];
				if ( item != null )
				{
					Object obj = item.getData( ITEM_DATA_KEY_TEXT );
					Boolean isEnabled = (Boolean) item.getData( ITEM_DATA_KEY_ENABLED );
					if ( obj != null && isEnabled.booleanValue( ) )
					{
						String text = (String) obj;
						insertText( text );
					}
				}
			}
		} );
	}

	protected TreeItem[] getTreeSelection( )
	{
		return tree.getSelection( );
	}

	/**
	 * Adds drag support to tree..Must set tree before execution.
	 */
	public void addDragSupportToTree( )
	{
		Assert.isNotNull( tree );
		DragSource dragSource = new DragSource( tree, DND.DROP_COPY );
		dragSource.setTransfer( new Transfer[]{
			TextTransfer.getInstance( )
		} );
		dragSource.addDragListener( new DragSourceAdapter( ) {

			public void dragStart( DragSourceEvent event )
			{
				TreeItem[] selection = tree.getSelection( );
				if ( selection.length <= 0
						|| selection[0].getData( ITEM_DATA_KEY_TEXT ) == null
						|| !( (Boolean) selection[0].getData( ITEM_DATA_KEY_ENABLED ) ).booleanValue( ) )
				{
					event.doit = false;
					return;
				}
			}

			public void dragSetData( DragSourceEvent event )
			{
				if ( TextTransfer.getInstance( )
						.isSupportedType( event.dataType ) )
				{
					TreeItem[] selection = tree.getSelection( );
					if ( selection.length > 0 )
					{
						event.data = selection[0].getData( ITEM_DATA_KEY_TEXT );
					}
				}
			}
		} );
	}

	/**
	 * Insert a text string into the text area
	 * 
	 * @param text
	 */
	protected void insertText( String text )
	{
		StyledText textWidget = expressionViewer.getTextWidget( );
		if ( !textWidget.isEnabled( ) )
		{
			return;
		}
		int selectionStart = textWidget.getSelection( ).x;
		if ( text.equalsIgnoreCase( "x++" ) ) //$NON-NLS-1$
		{
			text = textWidget.getSelectionText( ) + "++";//$NON-NLS-1$
		}
		else if ( text.equalsIgnoreCase( "x--" ) )//$NON-NLS-1$
		{
			text = textWidget.getSelectionText( ) + "--";//$NON-NLS-1$
		}
		else if ( text.equalsIgnoreCase( "++x" ) )//$NON-NLS-1$
		{
			text = "++" + textWidget.getSelectionText( );//$NON-NLS-1$
		}
		else if ( text.equalsIgnoreCase( "--x" ) )//$NON-NLS-1$
		{
			text = "--" + textWidget.getSelectionText( );//$NON-NLS-1$
		}

		textWidget.insert( text );
		textWidget.setSelection( selectionStart + text.length( ) );
		textWidget.setFocus( );

		if ( text.endsWith( "()" ) ) //$NON-NLS-1$
		{
			textWidget.setCaretOffset( textWidget.getCaretOffset( ) - 1 ); // Move
		}
	}

	/**
	 * Adds drop support to viewer.Must set viewer before execution.
	 *  
	 */
	public void addDropSupportToViewer( )
	{
		Assert.isNotNull( expressionViewer );
		final StyledText text = expressionViewer.getTextWidget( );
		dropTarget = new DropTarget( text, DND.DROP_COPY | DND.DROP_DEFAULT );
		dropTarget.setTransfer( new Transfer[]{
			TextTransfer.getInstance( )
		} );
		dropTarget.addDropListener( new DropTargetAdapter( ) {

			public void dragEnter( DropTargetEvent event )
			{
				text.setFocus( );
				if ( event.detail == DND.DROP_DEFAULT )
					event.detail = DND.DROP_COPY;
				if ( event.detail != DND.DROP_COPY )
					event.detail = DND.DROP_NONE;
			}

			public void dragOver( DropTargetEvent event )
			{
				event.feedback = DND.FEEDBACK_SCROLL
						| DND.FEEDBACK_INSERT_BEFORE;
			}

			public void dragOperationChanged( DropTargetEvent event )
			{
				dragEnter( event );
			}

			public void drop( DropTargetEvent event )
			{
				if ( event.data instanceof String )
					insertText( (String) event.data );
			}
		} );
	}

	/**
	 * Disposes resources.
	 *  
	 */
	public void dispose( )
	{
		if ( dropTarget != null && !dropTarget.isDisposed( ) )
		{
			dropTarget.dispose( );
		}
	}

	/**
	 * Sets the tree model.
	 * 
	 * @param tree
	 */
	public void setTree( Tree tree )
	{
		this.tree = tree;
	}

	protected Tree getTree( )
	{
		return tree;
	}

	/**
	 * Sets the viewer to use.
	 * 
	 * @param expressionViewer
	 */
	public void setExpressionViewer( SourceViewer expressionViewer )
	{
		this.expressionViewer = expressionViewer;
	}

	protected SourceViewer getExpressionViewer( )
	{
		return expressionViewer;
	}

	/**
	 * Gets an icon image by the key in plugin.properties
	 * 
	 * @param id
	 * @return image
	 */
	private static Image getIconImage( String id )
	{
		return ReportPlatformUIImages.getImage( id );
	}

	private void createObjects( TreeItem topItem, String objectType )
	{
		for ( Iterator itor = DesignEngine.getMetaDataDictionary( )
				.getClasses( )
				.iterator( ); itor.hasNext( ); )
		{
			IClassInfo classInfo = (IClassInfo) itor.next( );
			if ( classInfo.isNative( )
					&& OBJECTS_TYPE_BIRT.equals( objectType )
					|| !classInfo.isNative( )
					&& OBJECTS_TYPE_NATIVE.equals( objectType ) )
			{
				continue;
			}
			TreeItem subItem = createSubFolderItem( topItem, classInfo );
			Image globalImage = null;
			if ( isGlobal( classInfo.getName( ) ) )
			{
				globalImage = IMAGE_GOLBAL;
			}
			
			//Add members
			for ( Iterator iterator = classInfo.getMembers( ).iterator( ); iterator.hasNext( ); )
			{
				IMemberInfo memberInfo = (IMemberInfo) iterator.next( );
				Image image = globalImage;
				if ( image == null )
				{
					if ( memberInfo.isStatic( ) )
					{
						image = IMAGE_STATIC_MEMBER;
					}
					else
					{
						image = IMAGE_MEMBER;
					}
				}
				createSubTreeItem( subItem,
						memberInfo.getDisplayName( ),
						image,
						getMemberTextData( classInfo.getName( ), memberInfo ),
						memberInfo.getToolTip( ),
						true );
			}
			
			//Add constructors and methods
			List methodList = new ArrayList( );
			methodList.add( classInfo.getConstructor( ) );
			methodList.addAll( classInfo.getMethods( ) );
			for ( Iterator iterator = methodList.iterator( ); iterator.hasNext( ); )
			{
				IMethodInfo methodInfo = (IMethodInfo) iterator.next( );
				if ( methodInfo == null )
				{
					//Constructor is null
					continue;
				}
				Image image = globalImage;
				if ( image == null )
				{
					if ( methodInfo.isStatic( ) )
					{
						image = IMAGE_STATIC_METHOD;
					}
					else
					{
						image = IMAGE_METHOD;
					}
				}
				//Split a method with more than one signature into several entries
				List displayList = getMethodArgumentsList( classInfo.getName( ),
						methodInfo );
				for ( int i = 0; i < displayList.size( ); i++ )
				{
					String[] array = (String[]) displayList.get( i );
					createSubTreeItem( subItem,
							array[0],
							image,
							array[1],
							methodInfo.getToolTip( ),
							true );
				}
			}
		}
	}

	private boolean isGlobal( String name )
	{
		//TODO global validation is hard coded
		return name != null && name.startsWith( "Global" ); //$NON-NLS-1$
	}

	private List getMethodArgumentsList( String className, IMethodInfo info )
	{
		List list = new ArrayList( );
		boolean isClassNameAdded = !isGlobal( className ) && isStatic( info );
		String methodStart = info.isConstructor( ) ? "new " : "";  //$NON-NLS-1$//$NON-NLS-2$

		for ( Iterator it = info.argumentListIterator( ); it.hasNext( ); )
		{
			//Includes display text in tree view and expression in source
			// viewer
			String[] array = new String[2];

			StringBuffer displayText = new StringBuffer( methodStart );
			displayText.append( info.getDisplayName( ) );
			displayText.append( "(" );//$NON-NLS-1$

			StringBuffer expression = new StringBuffer( methodStart );
			if ( isClassNameAdded )
			{
				expression.append( className + "." );//$NON-NLS-1$
			}
			expression.append( info.getName( ) );
			expression.append( "(" );//$NON-NLS-1$	

			IArgumentInfoList arguments = (IArgumentInfoList) it.next( );
			boolean firstTime = true;
			for ( Iterator iterator = arguments.argumentsIterator( ); iterator.hasNext( ); )
			{
				IArgumentInfo argument = (IArgumentInfo) iterator.next( );
				if ( !firstTime )
				{
					displayText.append( ", " );//$NON-NLS-1$
				}
				firstTime = false;
				displayText.append( IArgumentInfo.OPTIONAL_ARGUMENT_NAME.equals( argument.getName( ) )
						? argument.getDisplayName( ) : ( argument.getType( )
								+ " " + argument.getDisplayName( ) ) ); //$NON-NLS-1$
			}
			displayText.append( ")" );//$NON-NLS-1$
			expression.append( ")" );//$NON-NLS-1$
			array[0] = displayText.toString( );
			array[1] = expression.toString( );
			list.add( array );
		}
		return list;
	}

	private String getMemberTextData( String className, ILocalizableInfo info )
	{
		StringBuffer textData = new StringBuffer( );
		if ( !isGlobal( className ) && isStatic( info ) )
		{
			textData.append( className + "." );//$NON-NLS-1$
		}
		textData.append( info.getName( ) );
		return textData.toString( );
	}

	private boolean isStatic( ILocalizableInfo info )
	{
		return info instanceof IMethodInfo
				&& ( (IMethodInfo) info ).isStatic( )
				|| info instanceof IMemberInfo
				&& ( (IMemberInfo) info ).isStatic( );
	}
}
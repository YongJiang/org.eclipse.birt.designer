/*************************************************************************************
 * Copyright (c) 2004 Actuate Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Actuate Corporation - Initial implementation.
 ************************************************************************************/

package org.eclipse.birt.report.designer.internal.ui.palette;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.birt.report.designer.core.IReportElementConstants;
import org.eclipse.birt.report.designer.core.model.SessionHandleAdapter;
import org.eclipse.birt.report.designer.core.model.views.data.DataSetItemModel;
import org.eclipse.birt.report.designer.internal.ui.dialogs.ImageBuilderDialog;
import org.eclipse.birt.report.designer.internal.ui.dnd.DNDUtil;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.tools.AbstractToolHandleExtends;
import org.eclipse.birt.report.designer.internal.ui.util.ExceptionHandler;
import org.eclipse.birt.report.designer.internal.ui.views.actions.InsertInLayoutAction;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.model.activity.SemanticException;
import org.eclipse.birt.report.model.api.CellHandle;
import org.eclipse.birt.report.model.api.ColumnHandle;
import org.eclipse.birt.report.model.api.DataItemHandle;
import org.eclipse.birt.report.model.api.DataSetHandle;
import org.eclipse.birt.report.model.api.ElementFactory;
import org.eclipse.birt.report.model.api.GridHandle;
import org.eclipse.birt.report.model.api.LabelHandle;
import org.eclipse.birt.report.model.api.ListHandle;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.RowHandle;
import org.eclipse.birt.report.model.api.ScalarParameterHandle;
import org.eclipse.birt.report.model.api.SlotHandle;
import org.eclipse.birt.report.model.api.TableHandle;
import org.eclipse.birt.report.model.api.TextItemHandle;
import org.eclipse.birt.report.model.elements.DesignChoiceConstants;
import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PanningSelectionToolEntry;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.jface.window.Window;

/**
 * is the base class of Palette factory. Creates default tools here.
 */
public class BasePaletteFactory
{

	private static final String PALETTE_GROUP_TEXT = Messages.getString( "BasePaletteFactory.Group.Items" ); //$NON-NLS-1$
	private static final String AUTOTEXT_LABEL_PAGE_X_OF_Y = Messages.getString( "BasePaletteFactory.AutoTextLabel.PageXofY" ); //$NON-NLS-1$
	private static final String AUTOTEXT_LABEL_LAST_PRINTED = Messages.getString( "BasePaletteFactory.AutoTextLabel.LastPrinted" ); //$NON-NLS-1$
	private static final String AUTOTEXT_LABEL_FILENAME = Messages.getString( "BasePaletteFactory.AutoTextLabel.Filename" ); //$NON-NLS-1$
	private static final String AUTOTEXT_LABEL_CREATE_BY = Messages.getString( "BasePaletteFactory.AutoTextLabel.CreatedBy" ); //$NON-NLS-1$
	private static final String AUTOTEXT_LABEL_CREATE_ON = Messages.getString( "BasePaletteFactory.AutoTextLabel.CreatedOn" ); //$NON-NLS-1$
	private static final String AUTOTEXT_LABEL_PAGE = Messages.getString( "BasePaletteFactory.AutoTextLabel.Page" ); //$NON-NLS-1$

	protected static PaletteRoot createPalette( )
	{
		PaletteRoot root = new PaletteRoot( );
		root.add( createControlGroup( root ) );
		return root;
	}

	/**
	 * Creates default tools category, which include selection and marquee tools
	 * 
	 * @param root
	 *            the root
	 * @return PaletteContainer contains default tools
	 */
	protected static PaletteContainer createControlGroup( PaletteRoot root )
	{
		PaletteGroup controlGroup = new PaletteGroup( PALETTE_GROUP_TEXT );

		List entries = new ArrayList( );

		ToolEntry tool = new PanningSelectionToolEntry( );
		entries.add( tool );
		root.setDefaultEntry( tool );

		tool = new MarqueeToolEntry( );
		entries.add( tool );

		controlGroup.addAll( entries );
		return controlGroup;
	}

	/**
	 * Provides element building support for table element.
	 */
	public static class TableToolExtends extends AbstractToolHandleExtends
	{

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.designer.internal.ui.editors.schematic.tools.IToolHandleExtends#preHandleMouseDown()
		 */
		public boolean preHandleMouseUp( )
		{
			CreateRequest request = getRequest( );
			if ( IReportElementConstants.REPORT_ELEMENT_TABLE.equalsIgnoreCase( (String) request.getNewObjectType( ) ) )
			{
				TableHandle table = SessionHandleAdapter.getInstance( )
						.getReportDesignHandle( )
						.getElementFactory( )
						.newTableItem( null, 3 );
				setInitWidth( table );
				setModel( table );
				return super.preHandleMouseUp( );
			}
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.designer.internal.ui.editors.schematic.tools.AbstractToolHandleExtends#preHandleMouseDown()
		 */
		public boolean preHandleMouseDown( )
		{
			// TODO Auto-generated method stub
			return false;
		}
	}

	/**
	 * Provides element building support for Data element.
	 */
	public static class DataToolExtends extends AbstractToolHandleExtends
	{

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.designer.internal.ui.editors.schematic.tools.IToolHandleExtends#preHandleMouseDown()
		 */
		public boolean preHandleMouseUp( )
		{
			CreateRequest request = getRequest( );
			if ( IReportElementConstants.REPORT_ELEMENT_DATA.equalsIgnoreCase( (String) request.getNewObjectType( ) ) )
			{
				DataItemHandle dataHandle = SessionHandleAdapter.getInstance( )
						.getReportDesignHandle( )
						.getElementFactory( )
						.newDataItem( null );
				setModel( dataHandle );
				return super.preHandleMouseUp( );
			}
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.designer.internal.ui.editors.schematic.tools.AbstractToolHandleExtends#preHandleMouseDown()
		 */
		public boolean preHandleMouseDown( )
		{
			// TODO Auto-generated method stub
			return false;
		}
	}

	/**
	 * Provides element building support for Image element.
	 */
	public static class ImageToolExtends extends AbstractToolHandleExtends
	{

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.designer.internal.ui.editors.schematic.tools.IToolHandleExtends#preHandleMouseDown()
		 */
		public boolean preHandleMouseUp( )
		{
			CreateRequest request = getRequest( );

			if ( IReportElementConstants.REPORT_ELEMENT_IMAGE.equalsIgnoreCase( (String) request.getNewObjectType( ) ) )
			{
				//Open the builder for new image
				ImageBuilderDialog dialog = new ImageBuilderDialog( );
				if ( Window.OK == dialog.open( ) )
				{
					setModel( dialog.getResult( ) );

					//If the dialog popup, mouse up event will not be called
					// automatically, call it explicit
					return super.preHandleMouseUp( );
				}
			}
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.designer.internal.ui.editors.schematic.tools.AbstractToolHandleExtends#preHandleMouseDown()
		 */
		public boolean preHandleMouseDown( )
		{
			// TODO Auto-generated method stub
			return false;
		}
	}

	/**
	 * Provides element building support for text element.
	 */
	public static class TextToolExtends extends AbstractToolHandleExtends
	{

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.designer.internal.ui.editors.schematic.tools.AbstractToolHandleExtends#preHandleMouseDown()
		 */

		public boolean preHandleMouseUp( )
		{
			String type = (String) getRequest( ).getNewObjectType( );
			String text = null;
			ReportDesignHandle reportHandle = SessionHandleAdapter.getInstance( )
					.getReportDesignHandle( );

			TextItemHandle textItemHandle = SessionHandleAdapter.getInstance( )
					.getReportDesignHandle( )
					.getElementFactory( )
					.newTextItem( null );
			try
			{
				if ( IReportElementConstants.AUTOTEXT_PAGE.equalsIgnoreCase( type ) )
				{
					text = AUTOTEXT_LABEL_PAGE;
				}
				else if ( IReportElementConstants.AUTOTEXT_DATE.equalsIgnoreCase( type ) )
				{
					text = "<value-of>new Date()</value-of>"; //$NON-NLS-1$
					textItemHandle.setContentType( DesignChoiceConstants.CONTENT_TYPE_HTML );
				}
				else if ( IReportElementConstants.AUTOTEXT_CREATEDON.equalsIgnoreCase( type ) )
				{
					text = AUTOTEXT_LABEL_CREATE_ON
							+ "<value-of>new Date()</value-of>"; //$NON-NLS-1$
					textItemHandle.setContentType( DesignChoiceConstants.CONTENT_TYPE_HTML );
				}
				else if ( IReportElementConstants.AUTOTEXT_CREATEDBY.equalsIgnoreCase( type ) )
				{
					text = AUTOTEXT_LABEL_CREATE_BY;
					String author = reportHandle.getAuthor( );
					if ( author != null )
					{
						text += author;
					}
				}
				else if ( IReportElementConstants.AUTOTEXT_FILENAME.equalsIgnoreCase( type ) )
				{
					text = AUTOTEXT_LABEL_FILENAME + reportHandle.getFileName( );
				}
				else if ( IReportElementConstants.AUTOTEXT_LASTPRINTED.equalsIgnoreCase( type ) )
				{
					text = AUTOTEXT_LABEL_LAST_PRINTED
							+ "<value-of>new Date()</value-of>"; //$NON-NLS-1$
					textItemHandle.setContentType( DesignChoiceConstants.CONTENT_TYPE_HTML );
				}
				else if ( IReportElementConstants.AUTOTEXT_PAGEXOFY.equalsIgnoreCase( type ) )
				{
					text = AUTOTEXT_LABEL_PAGE_X_OF_Y;
				}
				else if ( !IReportElementConstants.REPORT_ELEMENT_TEXT.equalsIgnoreCase( type ) )
				{
					return false;
				}

				if ( text != null )
				{
					textItemHandle.setContent( text );
				}
			}
			catch ( SemanticException e )
			{
				ExceptionHandler.handle( e );
			}

			setModel( textItemHandle );
			return super.preHandleMouseUp( );
		} /*
		   * (non-Javadoc)
		   * 
		   * @see org.eclipse.birt.designer.internal.ui.editors.schematic.tools.AbstractToolHandleExtends#preHandleMouseDown()
		   */

		public boolean preHandleMouseDown( )
		{
			return false;
		}
	}

	/**
	 * Provides element building support for label element.
	 */
	public static class LabelToolExtends extends AbstractToolHandleExtends
	{

		public boolean preHandleMouseUp( )
		{
			CreateRequest request = getRequest( );
			if ( IReportElementConstants.REPORT_ELEMENT_LABEL.equalsIgnoreCase( (String) request.getNewObjectType( ) ) )
			{
				LabelHandle labelItemHandle = SessionHandleAdapter.getInstance( )
						.getReportDesignHandle( )
						.getElementFactory( )
						.newLabel( null );

				setModel( labelItemHandle );
				return super.preHandleMouseUp( );

			}
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.designer.internal.ui.editors.schematic.tools.AbstractToolHandleExtends#preHandleMouseDown()
		 */
		public boolean preHandleMouseDown( )
		{
			return false;
		}
	}

	/**
	 * Provides element building support for List element.
	 */
	public static class ListToolExtends extends AbstractToolHandleExtends
	{

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.designer.internal.ui.editors.schematic.tools.IToolHandleExtends#preHandleMouseDown()
		 */
		public boolean preHandleMouseUp( )
		{
			CreateRequest request = getRequest( );
			if ( IReportElementConstants.REPORT_ELEMENT_LIST.equalsIgnoreCase( (String) request.getNewObjectType( ) ) )
			{
				ListHandle list = SessionHandleAdapter.getInstance( )
						.getReportDesignHandle( )
						.getElementFactory( )
						.newList( null );

				setModel( list );
				return super.preHandleMouseUp( );
			}
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.designer.internal.ui.editors.schematic.tools.AbstractToolHandleExtends#preHandleMouseDown()
		 */
		public boolean preHandleMouseDown( )
		{
			return false;
		}
	}

	/**
	 * Provides element building support for grid element.
	 */
	public static class GridToolExtends extends AbstractToolHandleExtends
	{

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.designer.internal.ui.editors.schematic.tools.IToolHandleExtends#preHandleMouseDown()
		 */
		public boolean preHandleMouseUp( )
		{
			ReportDesignHandle reportDesignHandle = SessionHandleAdapter.getInstance( )
					.getReportDesignHandle( );
			ElementFactory factory = reportDesignHandle.getElementFactory( );
			String type = (String) getRequest( ).getNewObjectType( );
			GridHandle grid = null;

			if ( IReportElementConstants.AUTOTEXT_AUTHOR_PAGE_DATE.equals( type ) )
			{
				grid = factory.newGridItem( null, 3, 1 );
				try
				{
					List cellList = ( (RowHandle) grid.getRows( ).get( 0 ) ).getCells( )
							.getContents( );

					TextItemHandle textHandle = factory.newTextItem( null );
					String text = AUTOTEXT_LABEL_CREATE_BY;
					if ( reportDesignHandle.getAuthor( ) != null )
					{
						text += reportDesignHandle.getAuthor( );
					}
					textHandle.setContent( text );
					( (CellHandle) cellList.get( 0 ) ).getContent( )
							.add( textHandle );

					textHandle = factory.newTextItem( null );
					textHandle.setContent( AUTOTEXT_LABEL_PAGE );
					( (CellHandle) cellList.get( 1 ) ).getContent( )
							.add( textHandle );

					textHandle = factory.newTextItem( null );
					textHandle.setContent( "<value-of>new Date()</value-of>" ); //$NON-NLS-1$
					textHandle.setContentType( DesignChoiceConstants.CONTENT_TYPE_HTML );
					( (CellHandle) cellList.get( 2 ) ).getContent( )
							.add( textHandle );
				}
				catch ( SemanticException e )
				{
					ExceptionHandler.handle( e );
				}
			}
			else if ( IReportElementConstants.AUTOTEXT_CONFIDENTIAL_PAGE.equals( type ) )
			{
				grid = factory.newGridItem( null, 2, 1 );
				try
				{
					List cellList = ( (RowHandle) grid.getRows( ).get( 0 ) ).getCells( )
							.getContents( );

					TextItemHandle textHandle = factory.newTextItem( null );
					textHandle.setContent( AUTOTEXT_LABEL_CREATE_ON
							+ "<value-of>new Date()</value-of>" ); //$NON-NLS-1$
					textHandle.setContentType( DesignChoiceConstants.CONTENT_TYPE_HTML );
					( (CellHandle) cellList.get( 0 ) ).getContent( )
							.add( textHandle );

					textHandle = factory.newTextItem( null );
					textHandle.setContent( AUTOTEXT_LABEL_PAGE );
					( (CellHandle) cellList.get( 1 ) ).getContent( )
							.add( textHandle );
				}
				catch ( SemanticException e )
				{
					ExceptionHandler.handle( e );
				}
			}
			else if ( IReportElementConstants.REPORT_ELEMENT_GRID.equals( type ) )
			{
				grid = factory.newGridItem( null, 3, 3 );
			}
			else
			{
				return false;
			}
			setInitWidth( grid );
			setModel( grid );
			return super.preHandleMouseUp( );
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.designer.internal.ui.editors.schematic.tools.AbstractToolHandleExtends#preHandleMouseDown()
		 */
		public boolean preHandleMouseDown( )
		{
			return false;
		}
	}

	/**
	 * Provides element building support for data set.
	 */
	public static class DataSetToolExtends extends AbstractToolHandleExtends
	{

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.designer.internal.ui.editors.schematic.tools.IToolHandleExtends#preHandleMouseDown()
		 */
		public boolean preHandleMouseUp( )
		{
			Object object = getSingleTransferData( getRequest( ).getNewObjectType( ) );
			if ( object instanceof DataSetHandle )
			{
				Object newHandle = InsertInLayoutAction.runSingleInsert( object,
						getTargetEditPart( ) );
				if ( newHandle == null )
					return false;

				setInitWidth( newHandle );
				setModel( newHandle );
				return super.preHandleMouseUp( );
			}
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.designer.internal.ui.editors.schematic.tools.AbstractToolHandleExtends#preHandleMouseDown()
		 */
		public boolean preHandleMouseDown( )
		{
			return false;
		}
	}

	/**
	 * Provides element building support for data set column.
	 */
	public static class DataSetColumnToolExtends
			extends
				AbstractToolHandleExtends
	{

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.designer.internal.ui.editors.schematic.tools.IToolHandleExtends#preHandleMouseDown()
		 */
		public boolean preHandleMouseUp( )
		{
			Object object = getSingleTransferData( getRequest( ).getNewObjectType( ) );
			if ( object instanceof DataSetItemModel )
			{
				Object newHandle = InsertInLayoutAction.runSingleInsert( object,
						getTargetEditPart( ) );
				if ( newHandle == null )
					return false;

				setModel( newHandle );
				return super.preHandleMouseUp( );
			}
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.designer.internal.ui.editors.schematic.tools.AbstractToolHandleExtends#preHandleMouseDown()
		 */
		public boolean preHandleMouseDown( )
		{
			return false;
		}

	}

	/**
	 * Provides element building support for parameter.
	 */
	public static class ParameterToolExtends extends AbstractToolHandleExtends
	{

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.designer.internal.ui.editors.schematic.tools.IToolHandleExtends#preHandleMouseDown()
		 */
		public boolean preHandleMouseUp( )
		{
			Object object = getSingleTransferData( getRequest( ).getNewObjectType( ) );
			if ( object instanceof ScalarParameterHandle )
			{
				Object newHandle = InsertInLayoutAction.runSingleInsert( object,
						getTargetEditPart( ) );
				if ( newHandle == null )
					return false;

				setModel( newHandle );
				return super.preHandleMouseUp( );
			}
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.designer.internal.ui.editors.schematic.tools.AbstractToolHandleExtends#preHandleMouseDown()
		 */
		public boolean preHandleMouseDown( )
		{
			return false;
		}
	}

	/**
	 * Provides element building support for parameter.
	 */
	public static class LayoutToolExtends extends AbstractToolHandleExtends
	{

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.designer.internal.ui.editors.schematic.tools.IToolHandleExtends#preHandleMouseDown()
		 */
		public boolean preHandleMouseUp( )
		{
			Object object = getSingleTransferData( getRequest( ).getNewObjectType( ) );
			return DNDUtil.moveHandles( object,
					getTargetEditPart( ).getModel( ),
					-1 );
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.birt.designer.internal.ui.editors.schematic.tools.AbstractToolHandleExtends#preHandleMouseDown()
		 */
		public boolean preHandleMouseDown( )
		{
			return false;
		}
	}

	/**
	 * Gets single transfer data from TemplateTransfer
	 * 
	 * @param template
	 *            object transfered by TemplateTransfer
	 * @return single transfer data in array or itself
	 */
	static Object getSingleTransferData( Object template )
	{
		if ( template instanceof Object[] )
		{
			return ( (Object[]) template )[0];
		}
		return template;
	}

	public static void setInitWidth( Object object )
	{
		final int precision = 10;
		int percentAll = 100 * precision;
		SlotHandle columns = null;
		try
		{
			if ( object instanceof TableHandle )
			{
				TableHandle table = (TableHandle) object;
				columns = table.getColumns( );
				table.setWidth( percentAll
						/ precision + DesignChoiceConstants.UNITS_PERCENTAGE );
			}
			else if ( object instanceof GridHandle )
			{
				GridHandle grid = (GridHandle) object;
				columns = grid.getColumns( );
				grid.setWidth( percentAll
						/ precision + DesignChoiceConstants.UNITS_PERCENTAGE );
			}
			else
				return;

			for ( int i = 0, count = columns.getCount( ); i < count; i++ )
			{
				int currentPercent = i == count - 1 ? percentAll : 1000 / count;
				percentAll -= currentPercent;

				( (ColumnHandle) columns.get( i ) ).getWidth( )
						.setStringValue( (double) currentPercent
								/ precision
								+ DesignChoiceConstants.UNITS_PERCENTAGE );
			}
		}
		catch ( SemanticException e )
		{
			ExceptionHandler.handle( e );
		}
	}
}
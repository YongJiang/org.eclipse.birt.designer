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

package org.eclipse.birt.report.item.crosstab.internal.ui.editors.model;

import org.eclipse.birt.report.designer.util.IVirtualValidator;
import org.eclipse.birt.report.item.crosstab.core.de.CrosstabCellHandle;
import org.eclipse.birt.report.item.crosstab.core.de.CrosstabReportItemHandle;
import org.eclipse.birt.report.item.crosstab.core.de.LevelViewHandle;
import org.eclipse.birt.report.item.crosstab.core.util.CrosstabUtil;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.LevelAttributeHandle;
import org.eclipse.birt.report.model.api.olap.DimensionHandle;
import org.eclipse.birt.report.model.api.olap.LevelHandle;
import org.eclipse.birt.report.model.api.olap.MeasureGroupHandle;
import org.eclipse.birt.report.model.api.olap.MeasureHandle;

/**
 * The default cell adapter
 */
public class NormalCrosstabCellAdapter extends CrosstabCellAdapter implements
		IVirtualValidator
{

	/**
	 * Constructor
	 * 
	 * @param handle
	 */
	public NormalCrosstabCellAdapter( CrosstabCellHandle handle )
	{
		super( handle );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode( )
	{
		return getCrosstabItemHandle( ).hashCode( );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals( Object obj )
	{
		// if ( obj == getCrosstabItemHandle( ) )
		// {
		// return true;
		// }
		// if ( obj instanceof CrosstabCellAdapter )
		// {
		// return getCrosstabItemHandle( ) == ( (CrosstabCellAdapter) obj
		// ).getCrosstabItemHandle( )
		// && getPositionType( ) == ( (CrosstabCellAdapter) obj
		// ).getPositionType( );
		// }
		return super.equals( obj );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.birt.report.designer.util.IVirtualValidator#handleValidate
	 * (java.lang.Object)
	 */
	public boolean handleValidate( Object obj )
	{
		CrosstabReportItemHandle crosstab = getCrosstabCellHandle( ).getCrosstab( );
		if ( obj instanceof Object[] )
		{
			Object[] objects = (Object[]) obj;
			int len = objects.length;
			if ( len == 0 )
			{
				return false;
			}
			if ( len == 1 )
			{
				return handleValidate( objects[0] );
			}
			else
			{
				for ( int i = 0; i < len; i++ )
				{
					Object temp = objects[i];
					if ( temp instanceof MeasureHandle
							|| temp instanceof MeasureGroupHandle )
					{
						if ( getPositionType( ).equals( ICrosstabCellAdapterFactory.CELL_MEASURE )
								&& crosstab.getCube( ) == CrosstabAdaptUtil.getCubeHandle( (DesignElementHandle) temp ) )
						{
							continue;
						}
						else
						{
							return false;
						}
					}
					else if ( temp instanceof LevelHandle )
					{
						if ( i > 0 )
						{
							if ( !( objects[i - 1] instanceof LevelHandle ) )
								return false;
							DesignElementHandle container = ( (LevelHandle) temp ).getContainer( );
							DesignElementHandle preContainer = ( (LevelHandle) objects[i - 1] ).getContainer( );
							if ( container != preContainer )
								return false;
						}
					}
					else
					{
						return false;
					}
				}
				return true;
			}

		}
		if ( obj instanceof DimensionHandle )
		{
			if ( ( getPositionType( ).equals( ICrosstabCellAdapterFactory.CELL_LEVEL_HANDLE ) || getPositionType( ).equals( ICrosstabCellAdapterFactory.CELL_FIRST_LEVEL_HANDLE ) )
					&& CrosstabUtil.canContain( crosstab, (DimensionHandle) obj ) )
			{
				return true;
			}
		}
		if ( obj instanceof LevelHandle )
		{
			return handleValidate( CrosstabAdaptUtil.getDimensionHandle( (LevelHandle) obj ) );
		}

		// LevelAttributeHandle is enable when
		// it's LevelHandle is not in the crosstab
		// or it is not in the crosstab
		if ( obj instanceof LevelAttributeHandle )
		{
			LevelAttributeHandle lah = (LevelAttributeHandle) obj;
			LevelHandle lh = (LevelHandle) lah.getElementHandle( );
			if ( handleValidate( CrosstabAdaptUtil.getDimensionHandle( lh ) ) )
				return true;

			if ( getCrosstabCellHandle( ) != null
					&& getCrosstabCellHandle( ).getContainer( ) instanceof LevelViewHandle )
			{
				LevelViewHandle lvh = (LevelViewHandle) getCrosstabCellHandle( ).getContainer( );
				if ( lvh.getCubeLevel( ) == lh )
					return true;
			}
		}

		if ( obj instanceof MeasureHandle )
		{
			if ( getPositionType( ).equals( ICrosstabCellAdapterFactory.CELL_MEASURE )
					&& CrosstabUtil.canContain( crosstab, (MeasureHandle) obj ) )
			{
				return true;
			}
		}

		if ( obj instanceof MeasureGroupHandle )
		{
			if ( getPositionType( ).equals( ICrosstabCellAdapterFactory.CELL_MEASURE )
					&& crosstab.getCube( ) == CrosstabAdaptUtil.getCubeHandle( (DesignElementHandle) obj ) )
			{
				return true;
			}
		}
		return false;
	}
}

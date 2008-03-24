/*******************************************************************************
 * Copyright (c) 2007 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.report.item.crosstab.ui.extension;

import org.eclipse.birt.report.item.crosstab.core.de.AggregationCellHandle;

/**
 * This inteface is used to create alternative view for aggregation cell in
 * crosstab.
 */
public interface IAggregationCellViewProvider
{
	public static final int SWITCH_VIEW_TYPE = 0;
	public static final int CHANGE_ORIENTATION_TYPE = 1;
	
	int defalutUpdateType = SWITCH_VIEW_TYPE;
	
	int getDefaultUpdateType();
	/**
	 * Return the name of this view
	 */
	String getViewName( );

	/**
	 * Returns if the given aggregation cell matches this view
	 */
	boolean matchView( AggregationCellHandle cell );

	/**
	 * @deprecated use {@link #switchView(SwitchCellInfo)}
	 */
	void switchView( AggregationCellHandle cell );
	
	/**
	 * Switches given aggregation cell to this view
	 */
	void switchView( SwitchCellInfo info );

	/**
	 * Restores given aggregation cell to previous view
	 */
	void restoreView( AggregationCellHandle cell );

	/**
	 * Updates current view when necessary
	 */
	void updateView( AggregationCellHandle cell );
	
	/**
	 * Updates current view when necessary with specified type.
	 */
	void updateView( AggregationCellHandle cell, int type);
	
	/**
	 * @deprecated use {@link #canSwitch(SwitchCellInfo)}
	 */
	boolean canSwitch( AggregationCellHandle cell );
	
	/**
	 * check whether can switch to this view
	 */
	boolean canSwitch(SwitchCellInfo info);

}

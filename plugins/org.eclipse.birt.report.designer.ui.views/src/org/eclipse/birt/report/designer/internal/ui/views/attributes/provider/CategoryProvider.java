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

package org.eclipse.birt.report.designer.internal.ui.views.attributes.provider;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.birt.report.designer.internal.ui.views.attributes.page.CategoryPage;
import org.eclipse.birt.report.designer.ui.views.attributes.ICategoryPage;
import org.eclipse.birt.report.designer.ui.views.attributes.providers.ICategoryProvider;

/**
 * CategoryProvider
 */
public class CategoryProvider implements ICategoryProvider
{

	private ICategoryPage[] categories;

	public CategoryProvider( String categoryKey, String category,
			Class pageClass )
	{
		this( new String[]{
			categoryKey
		}, new String[]{
			category
		}, new Class[]{
			pageClass
		} );
	}

	public CategoryProvider( String[] categoryKeys, String[] categories,
			Class[] pageClasses )
	{
		assert categories.length == pageClasses.length;

		this.categories = new ICategoryPage[categories.length];
		for ( int i = 0; i < categories.length; i++ )
		{
			this.categories[i] = new CategoryPage( categoryKeys[i],
					categories[i],
					pageClasses[i] );
		}
	}

	public void addCategory( String categoryKey, String categorie,
			Class pageClass )
	{
		ICategoryPage page = new CategoryPage( categoryKey,
				categorie,
				pageClass );
		addCategory( page );
	}

	public void addCategory( String categoryKey, String categorie,
			Class pageClass, int index )
	{
		ICategoryPage page = new CategoryPage( categoryKey,
				categorie,
				pageClass );
		addCategory( page, index );
	}

	public void addCategory( ICategoryPage category )
	{
		addCategory( category, categories.length );
	}

	public void addCategory( ICategoryPage category, int index )
	{
		List temp = Arrays.asList( categories );
		List list = new LinkedList( );
		list.addAll( temp );
		list.add( index, category );
		categories = new ICategoryPage[list.size( )];
		list.toArray( categories );
	}

	public int getCategoryIndex( ICategoryPage category )
	{
		return getCategoryIndex( category.getCategoryKey( ) );
	}

	public int getCategoryIndex( String categoryKey )
	{
		for ( int i = 0; i < categories.length; i++ )
		{
			if ( categories[i].getCategoryKey( ).equals( categoryKey ) )
				return i;
		}
		return -1;
	}

	public ICategoryPage[] getCategories( )
	{
		return categories;
	}
}

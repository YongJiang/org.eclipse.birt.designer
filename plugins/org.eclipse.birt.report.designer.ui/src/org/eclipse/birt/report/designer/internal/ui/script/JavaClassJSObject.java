/* Copyright (c) 2004 Actuate Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Actuate Corporation - Initial implementation.
 ************************************************************************************/

package org.eclipse.birt.report.designer.internal.ui.script;

import java.beans.Introspector;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * a JavaClass implementation for JSObjectMetaData use java reflect. Notes: this
 * implementation should better done by Engine because Engine knows what the
 * properties and methods can be used in javascript better.
 */
class JavaClassJSObject implements JSObjectMetaData
{

	private Class clazz;

	JavaClassJSObject( Class clazz )
	{
		this.clazz = clazz;
	}

	public JavaClassJSObject( String className ) throws ClassNotFoundException
	{
		this.clazz = Class.forName( className );
	}

	public String getName( )
	{
		return this.clazz.getName( );
	}

	public JSMethod[] getMethods( )
	{
		List jsMehods = new ArrayList( );
		// if ( this.clazz.isInterface( ) )
		// {
		// jsMehods.addAll( getMethods( Object.class.getMethods( ) ) );
		// }

		jsMehods.addAll( getMethods( this.clazz.getMethods( ) ) );

		Collections.sort( jsMehods );

		return (JSMethod[]) jsMehods.toArray( new JSMethod[jsMehods.size( )] );
	}

	private List getMethods( Method[] methods )
	{
		List jsMehods = new ArrayList( );
		List setMethodList = new ArrayList( );
		List getMethodList = new ArrayList( );
		for ( int i = 0; i < methods.length; i++ )
		{
			if ( methods[i].getName( ).startsWith( "set" ) //$NON-NLS-1$
					&& methods[i].getParameterTypes( ) != null
					&& methods[i].getParameterTypes( ).length == 1 )
			{
				setMethodList.add( methods[i].getName( ).substring( 3 ) );
				continue;
			}

			if ( methods[i].getName( ).startsWith( "get" ) //$NON-NLS-1$
					&& methods[i].getParameterTypes( ) != null
					&& methods[i].getParameterTypes( ).length == 0 )
			{
				getMethodList.add( methods[i].getName( ).substring( 3 ) );
				continue;
			}
			jsMehods.add( new JavaClassMethod( methods[i] ) );
		}

		for ( int i = 0; i < methods.length; i++ )
		{
			if ( methods[i].getName( ).startsWith( "set" ) //$NON-NLS-1$
					&& methods[i].getParameterTypes( ) != null
					&& methods[i].getParameterTypes( ).length == 1 )
			{
				if ( !getMethodList.contains( methods[i].getName( )
						.substring( 3 ) ) )
				{
					jsMehods.add( new JavaClassMethod( methods[i] ) );
				}
				continue;
			}

			if ( methods[i].getName( ).startsWith( "get" ) //$NON-NLS-1$
					&& methods[i].getParameterTypes( ) != null
					&& methods[i].getParameterTypes( ).length == 0 )
			{
				if ( !setMethodList.contains( methods[i].getName( )
						.substring( 3 ) ) )
				{
					jsMehods.add( new JavaClassMethod( methods[i] ) );
				}
				continue;
			}
		}
		return jsMehods;
	}

	public JSField[] getFields( )
	{
		List jsFields = new ArrayList( );
		jsFields.addAll( getFields( this.clazz ) );
		Collections.sort( jsFields );
		return (JSField[]) jsFields.toArray( new JSField[jsFields.size( )] );
	}

	private List getFields( Class clazz )
	{
		Method[] methods = clazz.getMethods( );
		List jsFields = new ArrayList( );
		List setMethodList = new ArrayList( );

		String methodName;
		for ( int i = 0; i < methods.length; i++ )
		{
			methodName = methods[i].getName( );
			if ( methodName.startsWith( "set" ) //$NON-NLS-1$
					&& methods[i].getParameterTypes( ) != null
					&& methods[i].getParameterTypes( ).length == 1 )
			{
				setMethodList.add( methodName.substring( 3 ) );
				continue;
			}
		}
		for ( int i = 0; i < methods.length; i++ )
		{
			methodName = methods[i].getName( );

			if ( methods[i].getName( ).startsWith( "get" ) //$NON-NLS-1$
					&& methods[i].getParameterTypes( ) != null
					&& methods[i].getParameterTypes( ).length == 0 )
			{
				if ( setMethodList.contains( methodName.substring( 3 ) ) )
				{
					Class type = methods[i].getReturnType( );
					JSField field = new JavaClassField( methods[i].getDeclaringClass( ),
							getFieldName( methodName ),
							getSimpleName( type ),
							type.isArray( ) );
					if ( !jsFields.contains( field ) )
						jsFields.add( field );
				}
			}
		}

		Field[] fields = clazz.getFields( );
		for ( int i = 0; i < fields.length; i++ )
		{
			jsFields.add( new JavaClassField( fields[i] ) );
		}

		if ( clazz.isArray( ) )
			jsFields.add( new JavaClassField( clazz,
					"length", //$NON-NLS-1$
					Integer.TYPE.getName( ),
					false ) );

		return jsFields;
	}

	private String getFieldName( String methodName )
	{
		if ( methodName.length( ) == 3 )
		{
			return ""; //$NON-NLS-1$
		}
		return Introspector.decapitalize( methodName.substring( 3 ) );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString( )
	{
		return clazz.getName( );
	}

	public String getDescription( )
	{
		return null;
	}

	public int getVisibility( )
	{
		return VISIBILITY_PUBLIC;
	}

	private class JavaClassMethod implements JSMethod, Comparable
	{

		private Method method;
		private String displayText;

		public JavaClassMethod( Method method )
		{
			this.method = method;
		}

		public String getName( )
		{
			return method.getName( );
		}

		public JSObjectMetaData getReturn( )
		{
			try
			{
				return JSSyntaxContext.getJavaClassMeta( method.getReturnType( )
						.getName( ) );
			}
			catch ( ClassNotFoundException e )
			{
				return null;
			}
		}

		public String getDisplayText( )
		{
			if ( displayText == null )
			{
				StringBuffer strbuf = new StringBuffer( getName( ) );
				strbuf.append( "(" ); //$NON-NLS-1$
				Class[] parameters = method.getParameterTypes( );
				for ( int i = 0; i < parameters.length; i++ )
				{
					if ( i > 0 )
					{
						strbuf.append( ", " ); //$NON-NLS-1$
					}
					strbuf.append( getSimpleName( parameters[i] ) )
							.append( " " ) //$NON-NLS-1$
							.append( "arg" ) //$NON-NLS-1$
							.append( i + 1 );
				}
				strbuf.append( ") " ); //$NON-NLS-1$
				strbuf.append( getSimpleName( method.getReturnType( ) ) );
				strbuf.append( " - " ); //$NON-NLS-1$
				strbuf.append( getSimpleName( method.getDeclaringClass( ) ) );
				displayText = strbuf.toString( );
				strbuf = null;
			}
			return displayText;
		}

		public String getDescription( )
		{
			return null;
		}

		public int getVisibility( )
		{
			if ( ( method.getModifiers( ) & Modifier.STATIC ) != 0 )
			{
				return VISIBILITY_STATIC;
			}
			else if ( ( method.getModifiers( ) & Modifier.PUBLIC ) != 0 )
			{
				return VISIBILITY_PUBLIC;
			}
			else if ( ( method.getModifiers( ) & Modifier.PRIVATE ) != 0 )
			{
				return VISIBILITY_PRIVATE;

			}
			else if ( ( method.getModifiers( ) & Modifier.PROTECTED ) != 0 )
			{
				return VISIBILITY_PROTECTED;
			}
			else
			{
				return VISIBILITY_PUBLIC;
			}
		}

		public int compareTo( Object obj )
		{
			if ( obj instanceof JavaClassMethod
					&& ( (JavaClassMethod) obj ).getName( ) != null )
			{
				if ( getVisibility( ) == VISIBILITY_STATIC )
				{
					if ( ( (JavaClassMethod) obj ).getVisibility( ) == VISIBILITY_PUBLIC )
						return 1;
				}
				return getName( ).compareToIgnoreCase( ( (JavaClassMethod) obj ).getName( ) );
			}
			return 0;
		}

	}

	private class JavaClassField implements JSField, Comparable
	{

		private String name;
		private JSObjectMetaData type;
		private String typeName;
		private Field field;
		private Class declareClazz;
		private String displayText;
		private boolean isArray;

		public JavaClassField( Field field )
		{
			this.field = field;
			this.name = field.getName( );
			try
			{
				this.type = JSSyntaxContext.getJavaClassMeta( field.getType( )
						.getName( ) );
			}
			catch ( ClassNotFoundException e )
			{
			}
		}

		public JavaClassField( Class declareClazz, String name, String type,
				boolean isArray )
		{
			this.declareClazz = declareClazz;
			this.name = name;
			try
			{
				this.type = JSSyntaxContext.getJavaClassMeta( type );
			}
			catch ( ClassNotFoundException e )
			{
				typeName = type;
			}
			this.isArray = isArray;
		}

		public String getName( )
		{
			return this.name;
		}

		public JSObjectMetaData getType( )
		{
			return this.type;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals( Object obj )
		{
			if ( obj == null
					|| !( obj instanceof JavaClassField )
					|| ( (JavaClassField) obj ).getName( ) == null )
			{
				return false;
			}

			return ( (JavaClassField) obj ).getName( ).equals( this.name );
		}

		public String getDisplayText( )
		{
			if ( displayText == null )
			{
				StringBuffer strbuf = new StringBuffer( getName( ) );
				strbuf.append( " " ); //$NON-NLS-1$
				if ( field != null && field.getType( ) != null )
				{
					strbuf.append( getSimpleName( field.getType( ) ) );
					strbuf.append( " - " ); //$NON-NLS-1$
				}
				else if ( type != null )
				{
					strbuf.append( getSimpleName( this.type.getName( ) ) );
					if ( isArray )
					{
						strbuf.append( "[]" ); //$NON-NLS-1$
					}
					strbuf.append( " - " ); //$NON-NLS-1$
				}
				else if ( typeName != null )
				{
					strbuf.append( typeName );
					strbuf.append( " - " ); //$NON-NLS-1$
				}
				if ( field == null )
				{
					strbuf.append( getSimpleName( declareClazz ) );
				}
				else
				{
					strbuf.append( getSimpleName( field.getDeclaringClass( ) ) );
				}
				displayText = strbuf.toString( );
				strbuf = null;
			}
			return displayText;
		}

		public String getDescription( )
		{
			return null;
		}

		public int getVisibility( )
		{
			if ( field == null )
				return VISIBILITY_PUBLIC;

			if ( ( field.getModifiers( ) & Modifier.STATIC ) != 0 )
			{
				return VISIBILITY_STATIC;
			}
			else if ( ( field.getModifiers( ) & Modifier.PUBLIC ) != 0 )
			{
				return VISIBILITY_PUBLIC;
			}
			else if ( ( field.getModifiers( ) & Modifier.PRIVATE ) != 0 )
			{
				return VISIBILITY_PRIVATE;

			}
			else if ( ( field.getModifiers( ) & Modifier.PROTECTED ) != 0 )
			{
				return VISIBILITY_PROTECTED;
			}
			else
			{
				return VISIBILITY_PUBLIC;
			}
		}

		public int compareTo( Object obj )
		{
			if ( obj instanceof JavaClassField
					&& ( (JavaClassField) obj ).getName( ) != null )
			{
				if ( getVisibility( ) == VISIBILITY_STATIC )
				{
					if ( ( (JavaClassField) obj ).getVisibility( ) == VISIBILITY_PUBLIC )
						return 1;
				}
				return getName( ).compareToIgnoreCase( ( (JavaClassField) obj ).getName( ) );
			}
			return 0;
		}

	}

	private String getSimpleName( Class clazz )
	{
		String simpleName = null;
		if ( clazz.isArray( ) )
		{
			simpleName = clazz.getComponentType( ).getName( );
			if ( !clazz.getComponentType( ).isPrimitive( ) )
			{
				simpleName = simpleName.substring( simpleName.lastIndexOf( "." ) + 1 ); //$NON-NLS-1$
			}
			simpleName += "[]"; //$NON-NLS-1$
		}
		else
		{
			simpleName = clazz.getName( );
			if ( !clazz.isPrimitive( ) )
			{
				simpleName = simpleName.substring( simpleName.lastIndexOf( "." ) + 1 ); //$NON-NLS-1$
			}
		}
		return simpleName;
	}

	private Object getSimpleName( String name )
	{
		return name.substring( name.lastIndexOf( "." ) + 1 ); //$NON-NLS-1$
	}

	public JSObjectMetaData getComponentType( )
	{
		if ( this.clazz.isArray( ) )
			return new JavaClassJSObject( this.clazz.getComponentType( ) );
		return null;
	}

}

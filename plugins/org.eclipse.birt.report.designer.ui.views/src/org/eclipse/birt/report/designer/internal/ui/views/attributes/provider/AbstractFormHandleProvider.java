
package org.eclipse.birt.report.designer.internal.ui.views.attributes.provider;

import org.eclipse.birt.report.designer.core.model.SessionHandleAdapter;
import org.eclipse.birt.report.designer.internal.ui.editors.parts.event.IModelEventProcessor;
import org.eclipse.birt.report.designer.internal.ui.util.UIUtil;
import org.eclipse.birt.report.designer.internal.ui.views.dialogs.provider.GroupHandleProvider;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.designer.util.DEUtil;
import org.eclipse.birt.report.model.api.CommandStack;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public abstract class AbstractFormHandleProvider implements IFormProvider
{

	protected Object input;

	public void setInput( Object input )
	{
		this.input = input;

	}

	public boolean isEnable( )
	{
		if ( DEUtil.getInputSize( input ) != 1 )
			return false;
		else
			return true;
	}

	public boolean edit( int pos )
	{
		CommandStack stack = getActionStack( );
		stack.startTrans( Messages.getString( "FormPage.Menu.ModifyProperty" ) ); //$NON-NLS-1$
		if ( !doEditItem( pos ) )
		{
			stack.rollback( );
			return false;
		}
		stack.commit( );
		return true;
	}

	public void add( int pos ) throws Exception
	{
		boolean sucess = false;
		CommandStack stack = getActionStack( );
		stack.startTrans( Messages.getString( "FormPage.Menu.ModifyProperty" ) ); //$NON-NLS-1$
		try
		{
			sucess = doAddItem( pos );
		}
		catch ( Exception e )
		{
			stack.rollback( );
			throw new Exception( e );
		}
		if ( sucess )
		{
			stack.commit( );
		}
		else
		{
			stack.rollback( );
		}
	}

	public void transModify( Object data, String property, Object value )
			throws Exception
	{

		CommandStack stack = getActionStack( );
		stack.startTrans( Messages.getString( "FormPage.Menu.ModifyProperty" ) ); //$NON-NLS-1$
		try
		{
			modify( data, property, value );
			stack.commit( );
		}
		catch ( Exception e )
		{
			stack.rollback( );
			throw new Exception( e );
		}
	}

	private CommandStack getActionStack( )
	{
		return SessionHandleAdapter.getInstance( ).getCommandStack( );
	}

	public FormContentProvider getFormContentProvider(
			IModelEventProcessor listener, IDescriptorProvider provider )
	{
		return new FormContentProvider( listener, provider );
	}

	public class FormContentProvider implements IStructuredContentProvider
	{

		private IModelEventProcessor listener;
		private IDescriptorProvider provider;

		public FormContentProvider( IModelEventProcessor listener,
				IDescriptorProvider provider )
		{
			this.listener = listener;
			this.provider = provider;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements( Object inputElement )
		{
			assert provider instanceof AbstractFormHandleProvider;
			Object[] elements = ( (AbstractFormHandleProvider) provider ).getElements( inputElement );
			registerEventManager( );
			deRegisterEventManager( );
			return elements;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose( )
		{
			if ( !( ( (IFormProvider) provider ) instanceof GroupHandleProvider ) )
				return;

			Object[] elements = ( (IFormProvider) provider ).getElements( input );

			if ( elements == null )
			{
				return;
			}
			deRegisterEventManager( );
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
		 *      java.lang.Object, java.lang.Object)
		 */
		public void inputChanged( Viewer viewer, Object oldInput,
				Object newInput )
		{
		}

		protected void deRegisterEventManager( )
		{
			if ( UIUtil.getModelEventManager( ) != null )
				UIUtil.getModelEventManager( )
						.removeModelEventProcessor( listener );
		}

		/**
		 * Registers model change listener to DE elements.
		 */
		protected void registerEventManager( )
		{
			if ( UIUtil.getModelEventManager( ) != null )
				UIUtil.getModelEventManager( )
						.addModelEventProcessor( listener );
		}
	}

	public Object load( )
	{
		return null;
	}

	public void save( Object value ) throws SemanticException
	{

	}

	public boolean isAddEnable( )
	{
		return true;
	}

}

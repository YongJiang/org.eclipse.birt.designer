
package org.eclipse.birt.report.designer.internal.ui.command;

import org.eclipse.birt.report.designer.core.model.SessionHandleAdapter;
import org.eclipse.birt.report.designer.internal.ui.editors.schematic.editparts.ReportElementEditPart;
import org.eclipse.birt.report.designer.internal.ui.util.ExceptionHandler;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.model.api.CommandStack;
import org.eclipse.birt.report.model.api.GroupHandle;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.gef.EditPartViewer;

public class DeleteGrouphandler extends SelectionHandler
{

	private static final String STACK_MSG_DELETE_GROUP = Messages.getString( "DeleteGroupAction.stackMsg.deleteGroup" ); //$NON-NLS-1$

	public Object execute( ExecutionEvent event ) throws ExecutionException
	{
		super.execute( event );

		GroupHandle handle = null;

		ReportElementEditPart editPart = null;

		IEvaluationContext context = (IEvaluationContext) event.getApplicationContext( );
		Object obj = context.getVariable( ICommandParameterNameContants.DELETE_GROUP_HANDLE );
		if ( ( obj == null ) || ( !( obj instanceof GroupHandle ) ) )
		{
			return new Boolean( false );
		}
		handle = (GroupHandle) obj;

		obj = context.getVariable( ICommandParameterNameContants.DELETE_GROUP_EDIT_PART );
		if ( ( obj == null ) || ( !( obj instanceof ReportElementEditPart ) ) )
		{
			return new Boolean( false );
		}
		editPart = (ReportElementEditPart) obj;

		CommandStack stack = getActiveCommandStack( );
		stack.startTrans( STACK_MSG_DELETE_GROUP );

		if ( handle.canDrop( ) )
		{
			EditPartViewer viewer = editPart.getViewer( );
			try
			{
				handle.drop( );
				stack.commit( );
			}
			catch ( SemanticException e )
			{
				stack.rollbackAll( );
				ExceptionHandler.handle( e );
			}
			viewer.select( editPart );
		}

		return new Boolean( true );
	}

	/**
	 * Gets the activity stack of the report
	 * 
	 * @return returns the stack
	 */
	protected CommandStack getActiveCommandStack( )
	{
		return SessionHandleAdapter.getInstance( ).getCommandStack( );
	}

}

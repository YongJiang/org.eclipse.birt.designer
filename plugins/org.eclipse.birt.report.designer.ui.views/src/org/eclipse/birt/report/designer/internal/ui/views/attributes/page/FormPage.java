
package org.eclipse.birt.report.designer.internal.ui.views.attributes.page;

import org.eclipse.birt.report.designer.internal.ui.util.SortMap;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.provider.IFormProvider;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.section.FormSection;
import org.eclipse.birt.report.designer.internal.ui.views.dialogs.provider.GroupHandleProvider;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.activity.NotificationEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

public class FormPage extends AttributePage
{

	private int style;
	private IFormProvider provider;
	private boolean withDialog = false;
	private boolean isTabbed = false;
	private FormSection formSection;
	private Composite composite;

	public FormPage( int style, IFormProvider provider )
	{
		this.style = style;
		this.provider = provider;
	}

	public FormPage(int style, IFormProvider provider,
			boolean withDialog )
	{
		this.style = style;
		this.provider = provider;
		this.withDialog = withDialog;
	}

	public FormPage( int style, IFormProvider provider,
			boolean withDialog, boolean isTabbed )
	{
		this.style = style;
		this.provider = provider;
		this.withDialog = withDialog;
		this.isTabbed = isTabbed;
	}

	public void buildUI( Composite parent  )
	{
		container = new ScrolledComposite( parent,  SWT.V_SCROLL );
		container.setLayoutData( new GridData( GridData.FILL_BOTH ) );
		((ScrolledComposite)container).setExpandHorizontal( true );
		((ScrolledComposite)container).setExpandVertical( true );
		container.addControlListener( new ControlAdapter( ) {

			public void controlResized( ControlEvent e )
			{
				computeSize( );
			}
		} );
		
		container.addDisposeListener( new DisposeListener( ) {

			public void widgetDisposed( DisposeEvent e )
			{
				deRegisterEventManager( );
			}
		} );
		
		composite = new Composite(container,SWT.NONE);
		composite.setLayoutData( new GridData(GridData.FILL_BOTH) );
		
		if ( sections == null )
			sections = new SortMap( );	
		composite.setLayout( WidgetUtil.createGridLayout( 1 ) );
		formSection = new FormSection( provider.getDisplayName( ),
				composite,
						true,
						isTabbed );
		formSection.setProvider( provider );
		formSection.setButtonWithDialog( withDialog );
		formSection.setStyle( style );
		formSection.setFillForm( true );
		addSection( PageSectionId.FORM_FORM, formSection );

		createSections( );
		layoutSections( );
		
		((ScrolledComposite)container).setContent( composite );
		
	}
	
	private void computeSize( )
	{
		Point size = composite.computeSize( SWT.DEFAULT, SWT.DEFAULT );
		((ScrolledComposite)container).setMinSize( size.x ,size.y+10 );
		container.layout( );
		
	}

	public void dispose( )
	{
		if ( !( provider instanceof GroupHandleProvider ) )
			return;

		Object[] elements = provider.getElements( input );

		if ( elements == null )
		{
			return;
		}
		deRegisterEventManager( );
	}
	
	public void addElementEvent( DesignElementHandle focus, NotificationEvent ev )
	{
		formSection.getFormControl( ).addElementEvent( focus, ev );
	}

	public void clear( )
	{
		formSection.getFormControl( ).clear( );
	}

	public void postElementEvent( )
	{
		formSection.getFormControl( ).postElementEvent( );
	}


}

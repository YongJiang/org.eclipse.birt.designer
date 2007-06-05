
package org.eclipse.birt.report.designer.internal.ui.views.attributes.widget;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.birt.report.designer.internal.ui.views.attributes.page.WidgetUtil;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.provider.IDescriptorProvider;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.provider.IToggleDescriptorProvider;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.provider.PropertyDescriptorProvider;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class FontStylePropertyDescriptor extends PropertyDescriptor
{

	public FontStylePropertyDescriptor( boolean formStyle )
	{
		setFormStyle( formStyle );
	}

	public Control getControl( )
	{
		return composite;
	}

	public Control createControl( Composite parent )
	{
		composite = new Composite( parent, SWT.NONE );
		GridLayout layout = new GridLayout( );
		layout.horizontalSpacing = 0;
		layout.numColumns = 4;
		composite.setLayout( layout );
		composite.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
		Composite styleContainer = new Composite( composite, SWT.NONE );

		layout = new GridLayout( toggleProviderList.size( ), false );
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		styleContainer.setLayout( layout );
		toggles = new TogglePropertyDescriptor[toggleProviderList.size( )];
		for ( int i = 0; i < toggleProviderList.size( ); i++ )
		{
			final TogglePropertyDescriptor toggle = DescriptorToolkit.createTogglePropertyDescriptor( );
			toggle.setDescriptorProvider( (IToggleDescriptorProvider) toggleProviderList.get( i ) );
			toggle.createControl( styleContainer );
			toggle.getControl( ).setLayoutData( new GridData( ) );
			toggle.getControl( ).addDisposeListener( new DisposeListener( ) {

				public void widgetDisposed( DisposeEvent event )
				{
					Control control = toggle.getControl( );
					control = null;
				}
			} );
			toggles[i] = toggle;
		}

		Label separator = FormWidgetFactory.getInstance( )
				.createLabel( composite,
						SWT.SEPARATOR | SWT.VERTICAL | SWT.CENTER,
						true );
		int width = separator.computeSize( SWT.DEFAULT, SWT.DEFAULT ).x + 3 * 2;
		GridData data = new GridData( );
		data.widthHint = width;
		data.heightHint = styleContainer.computeSize( SWT.DEFAULT, SWT.DEFAULT ).y;
		separator.setLayoutData( data );

		fontAlign = DescriptorToolkit.createFontAlignPropertyDescriptor( );
		fontAlign.setDescriptorProvider( fontAlignProvider );
		fontAlign.createControl( composite );
		fontAlign.getControl( ).setLayoutData( new GridData( ) );
		fontAlign.getControl( ).addDisposeListener( new DisposeListener( ) {

			public void widgetDisposed( DisposeEvent event )
			{
				Control control = fontAlign.getControl( );
				control = null;
			}
		} );

		WidgetUtil.createGridPlaceholder( composite, 1, true );

		return composite;
	}

	public void load( )
	{
		for ( int i = 0; i < toggles.length; i++ )
			toggles[i].load( );
		fontAlign.load( );
		composite.layout( );
	}

	public void setInput( Object input )
	{
		assert ( input != null );
		for ( int i = 0; i < toggles.length; i++ )
			toggles[i].setInput( input );
		fontAlign.setInput( input );
	}

	public void save( Object obj ) throws SemanticException
	{
		// TODO Auto-generated method stub

	}

	IDescriptorProvider[] providers;

	public IDescriptorProvider[] getProviders( )
	{
		return providers;
	}

	private List toggleProviderList = new LinkedList( );
	private PropertyDescriptorProvider fontAlignProvider;
	private FontAlignPropertyDescriptor fontAlign;
	private TogglePropertyDescriptor[] toggles;
	private Composite composite;

	public void setProviders( IDescriptorProvider[] providers )
	{
		for ( int i = 0; i < providers.length; i++ )
		{
			if ( providers[i] instanceof IToggleDescriptorProvider )
				toggleProviderList.add( providers[i] );
			else if ( providers[i] instanceof PropertyDescriptorProvider )
				fontAlignProvider = (PropertyDescriptorProvider) providers[i];
		}
	}

	public void setHidden( boolean isHidden )
	{
		if ( composite != null )
			WidgetUtil.setExcludeGridData( composite, isHidden );
	}

	public void setVisible( boolean isVisible )
	{
		if ( composite != null )
			composite.setVisible( isVisible );
	}

}

/**
 * 
 */
package org.eclipse.birt.report.item.crosstab.ui.views.attributes.provider;

import org.eclipse.birt.report.designer.internal.ui.views.attributes.provider.IDescriptorProvider;
import org.eclipse.birt.report.designer.util.DEUtil;
import org.eclipse.birt.report.model.api.ColumnHandle;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.ExtendedItemHandle;
import org.eclipse.birt.report.model.api.ReportItemHandle;
import org.eclipse.birt.report.model.api.RowHandle;
import org.eclipse.birt.report.model.api.activity.SemanticException;

/**
 * @author Administrator
 *
 */
public class FormatOptionPropertyDescriptorProvider implements
		IDescriptorProvider {

	protected Object input;
	
	protected ExtendedItemHandle handle;
	
	/* (non-Javadoc)
	 * @see org.eclipse.birt.report.designer.internal.ui.views.attributes.provider.IDescriptorProvider#getDisplayName()
	 */
	public String getDisplayName() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.birt.report.designer.internal.ui.views.attributes.provider.IDescriptorProvider#load()
	 */
	public Object load() {
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.birt.report.designer.internal.ui.views.attributes.provider.IDescriptorProvider#save(java.lang.Object)
	 */
	public void save(Object value) throws SemanticException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.birt.report.designer.internal.ui.views.attributes.provider.IDescriptorProvider#setInput(java.lang.Object)
	 */
	public void setInput(Object input) {
		this.input = input;
		handle = getFirstElementHandle();
	}
	
	private ExtendedItemHandle getFirstElementHandle( )
	{
		Object obj = DEUtil.getInputFirstElement( input );
		if ( obj instanceof ExtendedItemHandle )
		{
			return (ExtendedItemHandle) obj;
		}
		else
			return null;
	}

}

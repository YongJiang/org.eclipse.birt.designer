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

package org.eclipse.birt.report.designer.internal.ui.util;

/**
 * The interface is used for context-sensitive help IDs.
 */
public interface IHelpContextIds
{

	public static final String PREFIX = "org.eclipse.birt.cshelp."; //$NON-NLS-1$

	public static final String UNKNOWN = PREFIX + "dummy_outer_container";

	/*----------Control: Wizard----------*/
	public static final String NEW_REPORT_PROJECT_ID = PREFIX
			+ "NewReportProjectWizard_ID"; //$NON-NLS-1$

	public static final String NEW_REPORT_WIZARD_ID = PREFIX
			+ "NewReportWizard_ID"; //$NON-NLS-1$

	public static final String NEW_REPORT_COPY_WIZARD_ID = PREFIX
			+ "NewReportCopyWizard_ID";

	public static final String NEW_TEMPLATE_WIZARD_ID = PREFIX
			+ "NewTemplateWizard_ID";

	public static final String NEW_LIBRARY_WIZARD_ID = PREFIX
			+ "NewLibraryWizard_ID";

	public static final String SAVE_AS_WIZARD_ID = PREFIX
			+ "SaveReportAsWizard_ID";

	public static final String PUBLISH_TEMPLATE_WIZARD_ID = PREFIX
			+ "PublishTemplateWizard_ID";

	public static final String IMPORT_CSS_STYLE_WIZARD_ID = PREFIX
			+ "ImportCssStyle_ID";

	public static final String PUBLISH_LIBRARY_WIZARD_ID = PREFIX
			+ "PublishLibraryDialog_ID";

	public static final String EXPORT_TO_LIBRARY_WIZARD_ID = PREFIX
			+ "ExportToLibrary_ID";

	/*----------Control: Dialog----------*/
	public static final String FORMAT_BUILDER_ID = PREFIX + "FormatBuilder_ID";

	public static final String IMPORT_VALUE_DIALOG_ID = PREFIX
			+ "ImportValueDialog_ID";

	public static final String STYLE_BUILDER_GERNERAL_ID = PREFIX
			+ "StyleBuilderGeneral_ID";

	public static final String STYLE_BUILDER_FONT_ID = PREFIX
			+ "StyleBuilderFont_ID";

	public static final String STYLE_BUILDER_BACKGROUND_ID = PREFIX
			+ "StyleBuilderBackground_ID";

	public static final String STYLE_BUILDER_TEXTBLOCK_ID = PREFIX
			+ "StyleBuilderTextBlock_ID";

	public static final String STYLE_BUILDER_BOX_ID = PREFIX
			+ "StyleBuilderBox_ID";

	public static final String STYLE_BUILDER_BORDER_ID = PREFIX
			+ "StyleBuilderBorder_ID";

	public static final String STYLE_BUILDER_FORMATNUMBER_ID = PREFIX
			+ "StyleBuilderFormatNumber_ID";

	public static final String STYLE_BUILDER_FORMATDATATIME_ID = PREFIX
			+ "StyleBuilderFormatDateTime_ID";

	public static final String STYLE_BUILDER_FORMATSTRING_ID = PREFIX
			+ "StyleBuilderFormatString_ID";

	public static final String STYLE_BUILDER_PAGEBREAK_ID = PREFIX
			+ "StyleBuilderPageBreak_ID";

	public static final String STYLE_BUILDER_MAP_ID = PREFIX
			+ "StyleBuilderMap_ID";

	public static final String STYLE_BUILDER_HIGHTLIGHTS_ID = PREFIX
			+ "StyleBuilderHighlights_ID";

	public static final String PARAMETER_GROUP_DIALOG_ID = PREFIX
			+ "ParameterGroupDialog_ID";

	public static final String HYPERLINK_BUILDER_ID = PREFIX
			+ "HyperlinkBuilder_ID";

	public static final String EXPRESSION_BUILDER_ID = PREFIX
			+ "ExpressionBuilder_ID";

	public static final String PARAMETER_DIALOG_ID = PREFIX
			+ "ParameterDialog_ID";

	public static final String CASCADING_PARAMETER_DIALOG_ID = PREFIX
			+ "CascadingParametersDialog_ID";

	public static final String IMAGE_BUIDLER_ID = PREFIX + "ImageBuilder_ID";

	public static final String TABLE_OPTION_DIALOG_ID = PREFIX
			+ "TableOptionDialog_ID";

	public static final String Grid_OPTION_DIALOG_ID = PREFIX
			+ "GridOptionDialog_ID";

	public static final String TEXT_EDITOR_ID = PREFIX + "TextEditor_ID";

	public static final String GROUP_DIALOG_ID = PREFIX + "GroupDialog_ID";

	public static final String DATA_BINDING_DIALOG_ID = PREFIX
			+ "DataBindingDialog_ID";

	public static final String HIGHLIGHT_RULE_BUILDER_ID = PREFIX
			+ "HighlightRuleBuilder_ID";

	public static final String RESOURCE_SELECT_DIALOG_ID = PREFIX
			+ "ResourceSelectDialog_ID";

	public static final String CREATE_TEMPLATE_REPORT_ITEM_DIALOG_ID = PREFIX
			+ "CreateTemplateReportItem_ID";

	public static final String INSERT_EDIT_MAP_RULE_DIALOG_ID = PREFIX
			+ "AddEditMapRule_ID";
	
	public static final String INSERT_EDIT_SORTKEY_DIALOG_ID = PREFIX
	+ "AddEditSortkey_ID";

	public static final String ADD_EDIT_USER_PROPERTIES_DIALOG_ID = PREFIX
			+ "AddEditUserProperties_ID";

	public static final String ADD_EDIT_NAMED_EXPRESSION_DIALOG_ID = PREFIX
			+ "AddEditNamedExpression_ID";

	public static final String RESOURCE_EDIT_DIALOG_ID = PREFIX
			+ "ResourceEditDialog_ID";

	public static final String ADD_LIBRARY_DIALOG_ID = PREFIX
			+ "AddLibraryDialog_ID";

	public static final String COLUMNBINDING_DIALOG_ID = PREFIX
			+ "ColumnBindingDialog_ID";

	public static final String PARAMETERBINDING_DIALOG_ID = PREFIX
			+ "ParameterBindingDialog_ID";

	public static final String DELETE_WARNING_DIALOG_ID = PREFIX
			+ "DeleteWarningDialog_ID";

	public static final String NEW_SECTION_DIALOG = PREFIX
			+ "NewSectionDialog_ID";

	public static final String DIMENSION_BUILDER_DIALOG_DIALOG = PREFIX
			+ "DimensionBuilderDialog_ID";

	public static final String HANDLER_CLASS_SELECTION_DIALOG = PREFIX
			+ "HandlerClassSelectionDialog_ID";

	/*----------Control: Preference----------*/
	public static final String PREFERENCE_BIRT_DATA_SET_EDITOR_ID = PREFIX
			+ "Preference_BIRT_DataSetEditor_ID";

	public static final String PREFERENCE_BIRT_ELEMENT_NAMES_ID = PREFIX
			+ "Preference_BIRT_ElementNames_ID";

	public static final String PREFERENCE_BIRT_LIBRARY_ID = PREFIX
			+ "Preference_BIRT_Library_ID";

	public static final String PREFERENCE_BIRT_PREVIEW_ID = PREFIX
			+ "Preference_BIRT_Preview_ID";

	public static final String PREFERENCE_BIRT_PREVIEW_SERVER_ID = PREFIX
			+ "Preference_BIRT_PreviewServer_ID";

	public static final String PREFERENCE_BIRT_TEMPLATE_ID = PREFIX
			+ "Preference_BIRT_Template_ID";

	public static final String PREFERENCE_BIRT_COMMENTTEMPLATE_ID = PREFIX
			+ "Preference_BIRT_CommentTemplate_ID";

	public static final String PREFERENCE_BIRT_RESOURCE_ID = PREFIX
			+ "Preference_BIRT_Resource_ID";
}

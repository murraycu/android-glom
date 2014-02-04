package org.glom.app.libglom.layout.reportparts;

import android.text.TextUtils;
import org.glom.app.libglom.layout.LayoutGroup;
import org.glom.app.libglom.layout.LayoutItemField;

public class LayoutItemGroupBy extends LayoutGroup {

	private static final long serialVersionUID = -672753948682122432L;
	private LayoutItemField fieldGroupBy = null;
	private LayoutGroup secondaryFields = null;

	/**
	 * @return
	 */
	public boolean getHasFieldGroupBy() {
		if (fieldGroupBy == null) {
			return false;
		}

		return !TextUtils.isEmpty(fieldGroupBy.getName());
	}

	/**
	 * @return
	 */
	public LayoutItemField getFieldGroupBy() {
		return fieldGroupBy;
	}

	/**
	 * @param fieldGroupBy
	 */
	public void setFieldGroupBy(final LayoutItemField fieldGroupBy) {
		this.fieldGroupBy = fieldGroupBy;
	}

	/**
	 * @return
	 */
	public LayoutGroup getSecondaryFields() {
		return secondaryFields;
	}

	/**
	 * @param secondaryFields
	 */
	public void setSecondaryFields(final LayoutGroup secondaryFields) {
		this.secondaryFields = secondaryFields;
	}
}

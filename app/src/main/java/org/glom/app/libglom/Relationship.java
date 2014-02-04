package org.glom.app.libglom;

import android.text.TextUtils;

public class Relationship extends Translatable {

	private static final long serialVersionUID = 851415917396362167L;
	private String fromTable = "";
	private String fromField = "";
	private String toTable = "";
	private String toField = "";

	/**
	 * @return
	 */
	public boolean getHasToTable() {
		return !TextUtils.isEmpty(toTable);
	}

	/**
	 * @return
	 */
	public String getToField() {
		return toField;
	}

	/**
	 * @return
	 */
	public boolean getHasFields() {
		return !TextUtils.isEmpty(toField) && !TextUtils.isEmpty(toTable) && !TextUtils.isEmpty(fromField)
				&& !TextUtils.isEmpty(fromTable);
	}

	/**
	 * @return
	 */
	public String getToTable() {
		return toTable;
	}

	/**
	 * @return
	 */
	public String getFromTable() {
		return fromTable;
	}

	/**
	 * @return
	 */
	public String getFromField() {
		return fromField;
	}

	/**
	 * @param
	 */
	public void setFromTable(final String name) {
		fromTable = name;
	}

	/**
	 * @param
	 */
	public void setFromField(final String name) {
		fromField = name;
	}

	/**
	 * @param
	 */
	public void setToTable(final String name) {
		toTable = name;
	}

	/**
	 * @param
	 */
	public void setToField(final String name) {
		toField = name;
	}

	public boolean equals(final Relationship b) {
		if (b == null) {
			return false;
		}

		if (!TextUtils.equals(this.getName(), b.getName())) {
			return false;
		}

		if (!TextUtils.equals(this.fromTable, b.fromTable)) {
			return false;
		}

		if (!TextUtils.equals(this.fromField, b.fromField)) {
			return false;
		}

		if (!TextUtils.equals(this.toTable, b.toTable)) {
			return false;
		}

		if (!TextUtils.equals(this.toField, b.toField)) {
			return false;
		}

		return true;
	}

}

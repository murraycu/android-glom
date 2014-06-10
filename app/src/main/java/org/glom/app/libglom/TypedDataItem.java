/*
 * Copyright (C) 2011 Openismus GmbH
 *
 * This file is part of GWT-Glom.
 *
 * GWT-Glom is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * GWT-Glom is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GWT-Glom.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.glom.app.libglom;

import android.os.Parcel;
import android.os.Parcelable;

import org.glom.app.libglom.Field.GlomFieldType;

import java.util.Date;

/**
 * This specialization of DataItem can hold a primary key item.
 */
public class TypedDataItem extends DataItem implements Parcelable  {
    private boolean empty = true;
    private GlomFieldType type = GlomFieldType.TYPE_INVALID;
    private String unknown = null;

    public TypedDataItem() {
    }

    private TypedDataItem(Parcel in) {
        //Get the type:
        type = GlomFieldType.valueOf(in.readString());

        //Get the value, depending on the type:
        switch (type) {
            case TYPE_BOOLEAN:
                setBoolean(in.readInt() != 0);
                break;
            case TYPE_NUMERIC:
                setNumber(in.readDouble());
                break;
            case TYPE_TEXT:
                setText(in.readString());
                break;
            //TODO: case TYPE_DATE:
            //TODOcase TYPE_TIME:
            default:
                break;
        }
    }

    public boolean isEmpty() {
        return empty;
    }

    public boolean isUnknownType() {
        return (type == GlomFieldType.TYPE_INVALID);
    }

    /**
     * Get the value.
     * <p/>
     * This is a generic alternative to getNumber(), getText(), etc.
     *
     * @return
     */
    public Object getValue() {
        switch (type) {
            case TYPE_NUMERIC:
                return getNumber();
            case TYPE_TEXT:
                return getText();
            case TYPE_DATE:
                return getDate();
            //TODO: case TYPE_TIME:
            //	return getTime();
            case TYPE_BOOLEAN:
                return getBoolean();
            case TYPE_IMAGE:
                return getImageData();
            case TYPE_INVALID:
                return "value-with-invalid-type";
            default:
                return "value-with-unknown-type";
        }
    }

    //TODO: Why is this override necessary?
    /*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.shared.DataItem#setBoolean(boolean)
	 */
    @Override
    public void setBoolean(final boolean bool) {
        this.empty = false;
        this.type = GlomFieldType.TYPE_BOOLEAN;
        super.setBoolean(bool);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.glom.web.shared.DataItem#setNumber(double)
     */
    @Override
    public void setNumber(final double number) {
        this.empty = false;
        this.type = GlomFieldType.TYPE_NUMERIC;
        super.setNumber(number);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.glom.web.shared.DataItem#setText(java.lang.String)
     */
    @Override
    public void setText(final String text) {
        this.empty = false;
        this.type = GlomFieldType.TYPE_TEXT;
        super.setText(text);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.glom.web.shared.DataItem#setNumber(double)
     */
    @Override
    public void setDate(final Date date) {
        this.empty = false;
        this.type = GlomFieldType.TYPE_DATE;
        super.setDate(date);
    }

    public void setImageData(final byte[] imageData) {
        this.empty = false;
        this.type = GlomFieldType.TYPE_IMAGE;
        super.setImageData(imageData);
    }

    public void setImageDataUrl(final String image) {
        this.empty = false;
        this.type = GlomFieldType.TYPE_IMAGE;
        super.setImageDataUrl(image);
    }

    public String getUnknown() {
        return unknown;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.glom.web.shared.DataItem#setText(java.lang.String)
     */
    public void setUnknown(final String value) {
        this.empty = false;
        this.type = GlomFieldType.TYPE_INVALID;
        this.unknown = value;
    }

    public GlomFieldType getType() {
        return type;
    }

    public static final Parcelable.Creator<DataItem> CREATOR
            = new Parcelable.Creator<DataItem>() {
        public DataItem createFromParcel(Parcel in) {
            return new TypedDataItem(in);
        }

        public DataItem[] newArray(int size) {
            return new DataItem[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int i) {
        out.writeString(type.name());

        //Wrte the value, depending on the type:
        switch (type) {
            case TYPE_BOOLEAN:
                out.writeInt(getBoolean() ? 1 : 0);
                break;
            case TYPE_NUMERIC:
                out.writeDouble(getNumber());
                break;
            case TYPE_TEXT:
                out.writeString(getText());
                break;
            //TODO: case TYPE_DATE:
            //TODOcase TYPE_TIME:
            default:
                break;
        }
    }
}

/*
 * Copyright (C) 2011 Openismus GmbH
 *
 * This file is part of android-glom.
 *
 * android-glom is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * android-glom is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with android-glom.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.glom.app;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import org.glom.app.libglom.Document;
import org.glom.app.libglom.Field;
import org.glom.app.libglom.Field.GlomFieldType;
import org.glom.app.libglom.TypedDataItem;
import org.glom.app.libglom.layout.LayoutGroup;
import org.glom.app.libglom.layout.LayoutItem;
import org.glom.app.libglom.layout.LayoutItemField;
import org.glom.app.libglom.layout.LayoutItemPortal;
import org.glom.app.provider.GlomSystem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//import org.apache.http.client.utils.URIBuilder;

/**
 *
 */
public class Utils {

    /*
     * This method safely converts longs from libglom into ints. This method was taken from stackoverflow:
     *
     * http://stackoverflow.com/questions/1590831/safely-casting-long-to-int-in-java
     */
    public static int safeLongToInt(final long value) {
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(value + " cannot be cast to int without changing its value.");
        }
        return (int) value;
    }

//	/** Build the URL for the service that will return the binary data for an image.
//	 *
//	 * @param primaryKeyValue
//	 * @param field
//	 * @return
//	 */
//	public static String buildImageDataUrl(final TypedDataItem primaryKeyValue, final String documentID, final String tableName, final LayoutItemField field) {
//		final URIBuilder uriBuilder = buildImageDataUrlStart(documentID, tableName);
//
//		//TODO: Handle other types:
//		if(primaryKeyValue != null) {
//			uriBuilder.setParameter("value", Double.toString(primaryKeyValue.getNumber()));
//		}
//
//		uriBuilder.setParameter("field", field.getName());
//		return uriBuilder.toString();
//	}
//
//	/** Build the URL for the service that will return the binary data for an image.
//	 *
//	 * @param primaryKeyValue
//	 * @param field
//	 * @return
//	 */
//	public static String buildImageDataUrl(final String documentID, final String tableName, final String layoutName, final int[] path) {
//		final URIBuilder uriBuilder = buildImageDataUrlStart(documentID, tableName);
//		uriBuilder.setParameter("layout", layoutName);
//		uriBuilder.setParameter("layoutpath", buildLayoutPath(path));
//		return uriBuilder.toString();
//	}
//
//	/**
//	 * @param documentID
//	 * @param tableName
//	 * @return
//	 */
//	private static URIBuilder buildImageDataUrlStart(final String documentID, final String tableName) {
//		final URIBuilder uriBuilder = new URIBuilder();
//		//uriBuilder.setHost(GWT.getModuleBaseURL());
//		uriBuilder.setPath("OnlineGlom/gwtGlomImages"); //The name of our images servlet. See OnlineGlomImagesServlet.
//		uriBuilder.setParameter("document", documentID);
//		uriBuilder.setParameter("table", tableName);
//		return uriBuilder;
//	}

    /**
     * Build a :-separated string to represent the path as a string.
     *
     * @param path
     * @return
     */
    public static String buildLayoutPath(int[] path) {
        if ((path == null) || (path.length == 0)) {
            return null;
        }

        String result = "";
        for (int i : path) {
            if (!result.isEmpty()) {
                result += ":";
            }

            final String strIndex = Integer.toString(i);
            result += strIndex;
        }

        return result;
    }

    /**
     * Get an array of int indices from the :-separated string.
     * See buildLayoutPath().
     *
     * @param attrLayoutPath
     * @return The array of indices of the layout items.
     */
    public static int[] parseLayoutPath(final String attrLayoutPath) {
        if (TextUtils.isEmpty(attrLayoutPath)) {
            return null;
        }

        final String[] strIndices = attrLayoutPath.split(":");
        final int[] indices = new int[strIndices.length];
        for (int i = 0; i < strIndices.length; ++i) {
            final String str = strIndices[i];

            try {
                indices[i] = Integer.parseInt(str);
            } catch (final NumberFormatException nfe) {
                //TODO: Log the error.
                return null;
            }
        }

        return indices;
    }

    public static void transformUnknownToActualType(final TypedDataItem dataItem, final GlomFieldType actualType) {
        if (dataItem.getType() == actualType)
            return;

        String unknownText = dataItem.getUnknown();

        //Avoid repeated checks for null:
        if (unknownText == null) {
            unknownText = "";
        }

        switch (actualType) {
            case TYPE_NUMERIC:
                // TODO: Is this really locale-independent?
                double number = 0;
                if (!TextUtils.isEmpty(unknownText)) {
                    try {
                        number = Double.parseDouble(unknownText);
                    } catch (final NumberFormatException e) {
                        e.printStackTrace();
                    }
                }

                dataItem.setNumber(number);
                break;
            case TYPE_TEXT:
                dataItem.setText(unknownText);
                break;
            case TYPE_DATE:
                final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date date = null;
                try {
                    date = formatter.parse(unknownText);
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                dataItem.setDate(date);
                break;
            case TYPE_TIME:
            /*TODO :
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	        Date date;
			try {
				date = formatter.parse(unknownText);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
			setDate(date); 
			*/
                break;
            case TYPE_BOOLEAN:
                final boolean bool = unknownText.equals("true");
                dataItem.setBoolean(bool); //TODO
                break;
            case TYPE_IMAGE:
                dataItem.setImageDataUrl(unknownText);
                //setImageData(null);//TODO: Though this is only used for primary keys anyway.
                break;
            case TYPE_INVALID:
                break;
            default:
                break; //TODO: Warn because this is unusual?
        }
    }

    /*
          * Gets a list to use when generating an SQL query.
          */
    public static List<LayoutItemField> getFieldsToShowForSQLQuery(final Document document, final String tableName, final List<LayoutGroup> layoutGroupVec) {
        final List<LayoutItemField> listLayoutFIelds = new ArrayList<>();

        // We will show the fields that the document says we should:
        for (final LayoutGroup layoutGroup : layoutGroupVec) {
            // satisfy the precondition of getDetailsLayoutGroup(String tableName, LayoutGroup
            // libglomLayoutGroup)
            if (layoutGroup == null) {
                continue;
            }

            // Get the fields:
            final ArrayList<LayoutItemField> layoutItemFields = getFieldsToShowForSQLQueryAddGroup(document, tableName, layoutGroup);
            for (final LayoutItemField layoutItem_Field : layoutItemFields) {
                listLayoutFIelds.add(layoutItem_Field);
            }
        }
        return listLayoutFIelds;
    }

    /*
             * Gets an ArrayList of LayoutItem_Field objects to use when generating an SQL query.
             *
             * @precondition libglomLayoutGroup must not be null
             */
    private static ArrayList<LayoutItemField> getFieldsToShowForSQLQueryAddGroup(final Document document, final String tableName, final LayoutGroup libglomLayoutGroup) {

        final ArrayList<LayoutItemField> layoutItemFields = new ArrayList<>();
        final List<LayoutItem> items = libglomLayoutGroup.getItems();
        final int numItems = org.glom.app.libglom.Utils.safeLongToInt(items.size());
        for (int i = 0; i < numItems; i++) {
            final LayoutItem layoutItem = items.get(i);

            if (layoutItem instanceof LayoutItemField) {
                final LayoutItemField layoutItemField = (LayoutItemField) layoutItem;
                // the layoutItem is a LayoutItem_Field

                // Make sure that it has full field details:
                // TODO: Is this necessary?
                String tableNameToUse = tableName;
                if (layoutItemField.getHasRelationshipName()) {
                    tableNameToUse = layoutItemField.getTableUsed(tableName);
                }

                final Field field = document.getField(tableNameToUse, layoutItemField.getName());
                if (field != null) {
                    layoutItemField.setFullFieldDetails(field);
                } else {
                    //TODO: Log.w(document.getDatabaseTitleOriginal(), tableName,
                    //        "LayoutItem_Field " + layoutItemField.getLayoutDisplayName()
                    //                + " not found in document field list.");
                }

                // Add it to the list:
                layoutItemFields.add(layoutItemField);
            } else if (layoutItem instanceof LayoutGroup) {
                final LayoutGroup subLayoutGroup = (LayoutGroup) layoutItem;

                if (!(subLayoutGroup instanceof LayoutItemPortal)) {
                    // The subGroup is not a LayoutItemPortal.
                    // We're ignoring portals because they are filled by means of a separate SQL query.
                    layoutItemFields.addAll(getFieldsToShowForSQLQueryAddGroup(document, tableName, subLayoutGroup));
                }
            }
        }
        return layoutItemFields;
    }

    public static Uri buildFileContentUri(final Uri uriSystem, final ContentResolver resolver) {
        final String[] projection = new String[]{GlomSystem.Columns.FILE_URI_COLUMN};
        final Cursor cursor = resolver.query(uriSystem, projection, null, new String[]{}, null);
        if (cursor.getCount() <= 0) {
            Log.error("ContentResolver.query() returned no rows.");
            return null;
        }

        cursor.moveToFirst();
        final int index = cursor.getColumnIndex(GlomSystem.Columns.FILE_URI_COLUMN);
        if (index == -1) {
            Log.error("Cursor.getColumnIndex() failed.");
            return null;
        }

        final String str = cursor.getString(index);
        cursor.close(); //TODO: Should we do this?
        return Uri.parse(str);
    }
}

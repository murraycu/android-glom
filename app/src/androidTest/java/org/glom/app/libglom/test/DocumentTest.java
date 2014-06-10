/*
 * Copyright (C) 2009, 2010, 2011 Openismus GmbH
 *
 * This file is part of gwt-glom
 *
 * gwt-glom is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * gwt-glom is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with gwt-glom.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.glom.app.libglom.test;

import android.test.AndroidTestCase;

import org.glom.app.libglom.DataItem;
import org.glom.app.libglom.Document;
import org.glom.app.libglom.Field;
import org.glom.app.libglom.NumericFormat;
import org.glom.app.libglom.Relationship;
import org.glom.app.libglom.Report;
import org.glom.app.libglom.Translatable;
import org.glom.app.libglom.layout.LayoutGroup;
import org.glom.app.libglom.layout.LayoutItem;
import org.glom.app.libglom.layout.LayoutItemField;
import org.glom.app.libglom.layout.LayoutItemNotebook;
import org.glom.app.libglom.layout.LayoutItemPortal;
import org.glom.app.libglom.layout.LayoutItemText;
import org.glom.app.libglom.layout.SortClause;
import org.glom.app.libglom.layout.StaticText;
import org.glom.app.libglom.layout.TableToViewDetails;
import org.glom.app.libglom.layout.reportparts.LayoutItemGroupBy;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Simple test to ensure that the generated bindings are working.
 */
public class DocumentTest extends AndroidTestCase {

    private static final String defaultLocale = "";
    private static final String germanLocale = "de";
    private static InputStream inputStreamMusicCollection; //TODO: Do not reuse this.
    private static InputStream inputStreamFilmManager;
    private static Document document;

    /*
     * This method safely converts longs from libglom into ints. This method was taken from stackoverflow:
     *
     * http://stackoverflow.com/questions/1590831/safely-casting-long-to-int-in-java
     */
    private static int safeLongToInt(final long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }

    @Override
    public void setUp() {
        //For some reason DocumentTest.class.getResourceAsStream() doesn't work,
        //so we use DocumentTest.class.getClassLoader().getResourceAsStream(), which does.
        inputStreamMusicCollection = DocumentTest.class.getClassLoader().getResourceAsStream("example_music_collection.glom");
        assertNotNull(inputStreamMusicCollection);

        inputStreamFilmManager = DocumentTest.class.getClassLoader().getResourceAsStream("example_film_manager.glom");
        assertNotNull(inputStreamFilmManager);

        document = new Document();
        final boolean retval = document.load(inputStreamMusicCollection);
        assertTrue(retval);
    }

    @Override
    public void tearDown() {
    }

    public void testDocumentInfo() {
        assertEquals(document.getDatabaseTitleOriginal(), "Music Collection");
        assertEquals(document.getDatabaseTitle(defaultLocale), "Music Collection");
        assertEquals(document.getDatabaseTitle(germanLocale), "Musiksammlung");
        assertEquals(document.getDefaultTable(), "artists");
    }

    public void testLocales() {
        final List<String> localeIDs = document.getTranslationAvailableLocales();
        assertEquals(15, localeIDs.size());

        String tables = localeIDs.get(0);
        for (int i = 1; i < localeIDs.size(); i++) {
            tables += ", " + localeIDs.get(i);
        }
        assertEquals("cs, de, el, es, fr, gl, hu, id, lv, pt_BR, sl, sr, sr@latin, zh_CN, en", tables);
    }

    public void testReadTableNames() {
        final List<String> tableNames = document.getTableNames();
        assertEquals(4, tableNames.size());

        String tables = tableNames.get(0);
        for (int i = 1; i < tableNames.size(); i++) {
            tables += ", " + tableNames.get(i);
        }
        assertEquals("albums, songs, publishers, artists", tables);
    }

    String getTitles(final List<Field> list, final String locale) {
        String result = "";
        for (int i = 0; i < list.size(); i++) {
            final Translatable item = list.get(i);

            if (i != 0) {
                result += ", ";
            }

            result += item.getTitleOrName(locale);
        }

        return result;
    }

    public void testReadTableFieldSizes() {

        List<Field> fields = document.getTableFields("albums");
        assertEquals(6, fields.size());

        // TODO: The sequence is not important. It's only important that they are all there.
        assertEquals("Name, Publisher ID, Album ID, Artist ID, Comments, Year", getTitles(fields, defaultLocale));
        assertEquals("Name, Herausgeber-Kennung, Albenkennung, Künstlerkennung, Kommentare, Jahr", getTitles(fields, germanLocale));

        fields = document.getTableFields("artists");
        assertEquals(4, fields.size());

        // TODO: The sequence is not important. It's only important that they are all there.
        assertEquals("Name, Comments, Artist ID, Description", getTitles(fields, defaultLocale));
        assertEquals("Name, Kommentare, Künstlerkennung, Beschreibung", getTitles(fields, germanLocale));

        fields = document.getTableFields("publishers");
        assertEquals(3, fields.size());

        // TODO: The sequence is not important. It's only important that they are all there.
        assertEquals("Name, Comments, Publisher ID", getTitles(fields, defaultLocale));
        assertEquals("Name, Kommentare, Herausgeber-Kennung", getTitles(fields, germanLocale));


        fields = document.getTableFields("songs");
        assertEquals(4, fields.size());

        // TODO: The sequence is not important. It's only important that they are all there.
        assertEquals("Name, Comments, Song ID, Album ID", getTitles(fields, defaultLocale));
        assertEquals("Name, Kommentare, Lied-Kennung, Albenkennung", getTitles(fields, germanLocale));
    }

    public void testReadTableExampleRows() {
        final List<Map<String, DataItem>> exampleRows = document.getExampleRows("albums");
        assertFalse(exampleRows.isEmpty());

        final Map<String, DataItem> row = exampleRows.get(0);
        assertFalse(row.isEmpty());
    }

    public void testReadLayoutListInfo() {
        final String[] tables = {"albums", "artists", "publishers", "songs"};
        final int[] sortClauseSizes = {0, 1, 1, 1};
        final int[] layoutFieldSizes = {7, 4, 3, 4};

        for (int i = 0; i < tables.length; i++) {
            final List<LayoutGroup> layoutList = document.getDataLayoutGroups(Document.LAYOUT_NAME_LIST, tables[i]);
            assertTrue(!layoutList.isEmpty());
            final List<LayoutItem> layoutItems = layoutList.get(0).getItems();
            final List<LayoutItemField> layoutFields = new ArrayList<>();
            final SortClause sortClause = new SortClause(); // TODO: Why use a SortClause instead of a List?
            final int numItems = safeLongToInt(layoutItems.size());
            for (int j = 0; j < numItems; j++) {
                final LayoutItem item = layoutItems.get(j);

                if (item instanceof LayoutItemField) {
                    final LayoutItemField field = (LayoutItemField) item;
                    layoutFields.add(field);
                    final Field details = field.getFullFieldDetails();
                    if (details != null && details.getPrimaryKey()) {
                        sortClause.add(new SortClause.SortField(field, true)); // ascending
                    }
                }
            }
            assertEquals(sortClauseSizes[i], sortClause.size());
            assertEquals(layoutFieldSizes[i], safeLongToInt(layoutFields.size()));
        }
    }

    /*
     * This tests if getting values from a NumericFormat object is working. This test was failing with a JVM crash when
     * using the glom_sharedptr macro with Glom::UsesRelationship and Glom::Formatting.
     */
    public void testGetNumericFormat() {
        final List<String> tableNames = document.getTableNames();

        for (final String table : tableNames) {
            final List<LayoutGroup> layoutList = document.getDataLayoutGroups(Document.LAYOUT_NAME_LIST, table);
            assertTrue(!layoutList.isEmpty());
            final LayoutGroup firstgroup = layoutList.get(0);
            assertNotNull(firstgroup);
            final List<LayoutItem> layoutItems = firstgroup.getItems();
            final int numItems = safeLongToInt(layoutItems.size());
            for (int j = 0; j < numItems; j++) {
                final LayoutItem item = layoutItems.get(j);
                assertNotNull(item);

                if (item instanceof LayoutItemField) {
                    final LayoutItemField itemField = (LayoutItemField) item;
                    // don't keep a reference to the FeildFormatting object
                    final NumericFormat numFormat = itemField.getFormattingUsed().getNumericFormat();
                    assertNotNull(numFormat);

                    // get the values
                    final boolean altForegroundColorForNegatives = numFormat.getUseAltForegroundColorForNegatives();
                    final String currencySymbol = numFormat.getCurrencySymbol();
                    final long decimalPlaces = numFormat.getDecimalPlaces();
                    final boolean decimalPlacesRestricted = numFormat.getDecimalPlacesRestricted();
                    final boolean useThousandsSepator = numFormat.getUseThousandsSeparator();
                    final String alternativeColorForNegatives = NumericFormat
                            .getAlternativeColorForNegativesAsHTMLColor();
                    final long defaultPrecision = NumericFormat.getDefaultPrecision();

                    // Simulate a garbage collection
                    System.gc();
                    System.runFinalization();

                    // re-get the values and test
                    assertEquals(altForegroundColorForNegatives, numFormat.getUseAltForegroundColorForNegatives());
                    assertEquals(currencySymbol, numFormat.getCurrencySymbol());
                    assertEquals(decimalPlaces, numFormat.getDecimalPlaces());
                    assertEquals(decimalPlacesRestricted, numFormat.getDecimalPlacesRestricted());
                    assertEquals(useThousandsSepator, numFormat.getUseThousandsSeparator());
                    assertEquals(alternativeColorForNegatives,
                            NumericFormat.getAlternativeColorForNegativesAsHTMLColor());
                    assertEquals(defaultPrecision, NumericFormat.getDefaultPrecision());

                }
            }
        }
    }

    /*
     * A smoke test for the methods added to LayoutItemField for accessing methods in Glom::UsesRelationship.
     */
    public void testUsesRelationshipMethods() {
        final String table = "albums";
        final List<LayoutGroup> layoutList = document.getDataLayoutGroups(Document.LAYOUT_NAME_LIST, table);
        final List<LayoutItem> layoutItems = layoutList.get(0).getItems();

        String names = null, hasRelationshipNames = null, tablesUsed = null;
        final LayoutItem firstItem = layoutItems.get(0);

        if (firstItem instanceof LayoutItemField) {
            final LayoutItemField firstItemField = (LayoutItemField) firstItem;
            names = firstItemField.getName();
            hasRelationshipNames = "" + firstItemField.getHasRelationshipName();
            tablesUsed = firstItemField.getTableUsed(table);
        }
        final int numItems = safeLongToInt(layoutItems.size());
        for (int j = 1; j < numItems; j++) {
            final LayoutItem item = layoutItems.get(j);

            if (item instanceof LayoutItemField) {
                final LayoutItemField itemField = (LayoutItemField) item;
                names += ", " + itemField.getName();
                hasRelationshipNames += ", " + itemField.getHasRelationshipName();
                tablesUsed += ", " + itemField.getTableUsed(table);
            }
        }
        assertEquals("name, year, artist_id, name, publisher_id, name, comments", names);
        assertEquals("false, false, false, true, false, true, false", hasRelationshipNames);
        assertEquals("albums, albums, albums, artists, albums, publishers, albums", tablesUsed);
    }

    public void testLayoutItemText() {

        // Create a new document for the film manager
        final Document filmManagerDocument = new Document();
        final boolean retval = filmManagerDocument.load(inputStreamFilmManager);
        assertTrue(retval);

        // This relies on specific details of the film manager details
        // view layout. I've included safety checks that will fail if the layout changes.
        final List<LayoutGroup> detailsLayout = filmManagerDocument.getDataLayoutGroups(Document.LAYOUT_NAME_DETAILS, "scenes");
        assertEquals(3, detailsLayout.size());

        LayoutGroup layoutGroup = detailsLayout.get(1);
        assertEquals(Document.LAYOUT_NAME_DETAILS, layoutGroup.getName());

        final List<LayoutItem> items = layoutGroup.getItems();

        final LayoutItem item = items.get(1);
        assertTrue(item instanceof LayoutItemText);

        LayoutItemText itemText = (LayoutItemText) item;
        StaticText text = itemText.getText();
        assertEquals("The location name will be used if the name is empty.", text.getTitle());
    }

    public void testGetSuitableTableToViewDetails() {

        // Create a new document for the film manager
        final Document filmManagerDocument = new Document();
        final boolean retval = filmManagerDocument.load(inputStreamFilmManager);
        assertTrue(retval);

        // Get the "Scene Cast" related list portal. This relies on specific details of the film manager details
        // view layout. I've included safety checks that will fail if the layout changes.
        final List<LayoutGroup> detailsLayout = filmManagerDocument.getDataLayoutGroups(Document.LAYOUT_NAME_DETAILS, "scenes");
        assertEquals(3, detailsLayout.size());

        LayoutGroup layoutGroup = detailsLayout.get(1);
        assertEquals(Document.LAYOUT_NAME_DETAILS, layoutGroup.getName());
        assertEquals("Details", layoutGroup.getTitle(defaultLocale));
        assertEquals("Details", layoutGroup.getTitle(germanLocale));

        layoutGroup = detailsLayout.get(2);
        assertEquals("details_lower", layoutGroup.getName());

        List<LayoutItem> items = layoutGroup.getItems();
        assertEquals(2, items.size());

        final LayoutItem notebookItem = items.get(0);
        assertEquals("notebook", notebookItem.getName());
        assertTrue(notebookItem instanceof LayoutItemNotebook);
        final LayoutItemNotebook notebook = (LayoutItemNotebook) notebookItem;
        items = notebook.getItems();
        assertEquals(7, items.size());
        final LayoutItem portalItem = items.get(0);
        assertTrue(portalItem instanceof LayoutItemPortal);
        final LayoutItemPortal portal = (LayoutItemPortal) portalItem;
        assertNotNull(portal);

        assertEquals("scene_cast", portal.getRelationshipNameUsed());
        assertEquals("Cast", portal.getTitle(defaultLocale));
        assertEquals("Szene Besetzung", portal.getTitle(germanLocale));

        // call getSuitableTableToViewDetails
        final TableToViewDetails viewDetails = filmManagerDocument.getPortalSuitableTableToViewDetails(portal);
        assertNotNull(viewDetails);

        // Simulate a garbage collection
        System.gc();
        System.runFinalization();

        // Check if things are working like we expect
        assertEquals("characters", viewDetails.tableName);
        assertNotNull(viewDetails.usesRelationship);
        final Relationship relationship = viewDetails.usesRelationship.getRelationship();
        assertNotNull(relationship);
        assertEquals("cast", relationship.getName());
        assertTrue(viewDetails.usesRelationship.getRelatedRelationship() == null);

    }

    public void testReadReportNames() {
        final List<String> reportNames = document.getReportNames("albums");
        assertEquals(1, reportNames.size()); // TODO: Test something with more reports.

        String reports = reportNames.get(0);
        for (int i = 1; i < reportNames.size(); i++) {
            reports += ", " + reportNames.get(i);
        }
        assertEquals(reports, "albums_by_artist");
    }

    //TODO: Re-enable this when we know why it fails.
//	// Test thread class that runs all the tests.
//	private class TestThread implements Runnable {
//
//		@Override
//		public void run() {
//			for (int i = 0; i < 10; i++) {
//				testDocumentInfo();
//				testGetNumericFormat();
//				testLayoutItemText();
//				//TODO: testLayoutItemImage(), also testing that it has the expected layout path.
//				testGetSuitableTableToViewDetails();
//				testReadLayoutListInfo();
//				testReadTableFieldSizes();
//				testReadTableNames();
//				testUsesRelationshipMethods();
//			}
//		}
//	}
//
//	/*
//	 * Tests threaded access.
//	 */
//	public void testThreadedAccess() throws InterruptedException {
//		// create the threads
//		final Thread thread1 = new Thread(new TestThread());
//		final Thread thread2 = new Thread(new TestThread());
//		final Thread thread3 = new Thread(new TestThread());
//		final Thread thread4 = new Thread(new TestThread());
//
//		// start the threads
//		thread1.start();
//		thread2.start();
//		thread3.start();
//		thread4.start();
//
//		// wait for the treads to finish
//		try {
//			thread1.join();
//		} catch (final InterruptedException e) {
//			System.out.println("Thread 1 had a problem finishing. " + e);
//			throw e;
//		}
//
//		try {
//			thread2.join();
//		} catch (final InterruptedException e) {
//			System.out.println("Thread 2 had a problem finishing. " + e);
//			throw e;
//		}
//
//		try {
//			thread3.join();
//		} catch (final InterruptedException e) {
//			System.out.println("Thread 3 had a problem finishing. " + e);
//			throw e;
//		}
//
//		try {
//			thread4.join();
//		} catch (final InterruptedException e) {
//			System.out.println("Thread 4 had a problem finishing. " + e);
//			throw e;
//		}
//	}

    public void testReadReportStructure() {
        final Report report = document.getReport("albums", "albums_by_artist");
        assertNotNull(report);

        assertEquals(report.getTitle(defaultLocale), "Albums By Artist");
        assertEquals(report.getTitle(germanLocale), "Alben nach Künstler");

        final LayoutGroup layoutGroup = report.getLayoutGroup();
        assertNotNull(layoutGroup);
        final List<LayoutItem> layoutItems = layoutGroup.getItems();
        final int numItems = safeLongToInt(layoutItems.size());
        assertEquals(1, numItems);

        LayoutItem layoutItem = layoutItems.get(0);
        assertNotNull(layoutItem);
        final LayoutGroup asGroup = (LayoutGroup) layoutItem;
        assertNotNull(asGroup);
        final LayoutItemGroupBy groupby = (LayoutItemGroupBy) layoutItem;
        assertNotNull(groupby);

        assertTrue(groupby.getHasFieldGroupBy());
        final LayoutItemField fieldGroupBy = groupby.getFieldGroupBy();
        assertNotNull(fieldGroupBy);
        assertEquals(fieldGroupBy.getName(), "artist_id");

        final LayoutGroup groupSecondaries = groupby.getSecondaryFields();
        assertNotNull(groupSecondaries);

        final List<LayoutItem> innerItems = groupby.getItems();
        assertNotNull(innerItems);
        final int numInnerItems = safeLongToInt(innerItems.size());
        assertEquals(2, numInnerItems);

        layoutItem = innerItems.get(0);
        assertNotNull(layoutItem);
        assertTrue(layoutItem instanceof LayoutItemField);
        LayoutItemField field = (LayoutItemField) layoutItem;
        assertNotNull(field);
        assertEquals(field.getName(), "name");
        assertEquals(field.getGlomType(), Field.GlomFieldType.TYPE_TEXT);

        layoutItem = innerItems.get(1);
        assertNotNull(layoutItem);
        assertTrue(layoutItem instanceof LayoutItemField);
        field = (LayoutItemField) layoutItem;
        assertNotNull(field);
        assertEquals(field.getName(), "year");
        assertEquals(field.getGlomType(), Field.GlomFieldType.TYPE_NUMERIC);
    }

}

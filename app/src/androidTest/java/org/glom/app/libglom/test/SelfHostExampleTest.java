/*
 * Copyright (C) 2012 Openismus GmbH
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

package org.glom.app.libglom.test;

import android.test.AndroidTestCase;

import static junit.framework.Assert.*;

import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;

import org.glom.app.libglom.Document;
import org.jooq.SQLDialect;

/**
 * @author Murray Cumming <murrayc@openismus.com>
 * 
 */
public class SelfHostExampleTest extends AndroidTestCase {

	private static SelfHoster selfHoster = null;

    /*
	public void testPostgreSQL() throws SQLException {
		doTest(Document.HostingMode.HOSTING_MODE_POSTGRES_SELF);
	}

	public void testMySQL() throws SQLException {
		doTest(Document.HostingMode.HOSTING_MODE_MYSQL_SELF);
	}
	*/

    public void testSQLite() throws SQLException {
        doTest(Document.HostingMode.HOSTING_MODE_SQLITE);
    }
	
	/* This is really a test of our test utility code. */
	public void testSelfHosterEscapeIDSame() {
		final String id = "something";
		assertEquals("\"" + id + "\"", SelfHosterMySQL.quoteAndEscapeSqlId(id, SQLDialect.POSTGRES));
		assertEquals("`" + id + "`", SelfHosterMySQL.quoteAndEscapeSqlId(id, SQLDialect.MYSQL));
	}

	/* This is really a test of our test utility code. */
	public void testSelfHosterEscapeIDNotSame() {
		final String id = "something with a \" and a ` char";
		assertFalse(SelfHosterMySQL.quoteAndEscapeSqlId(id, SQLDialect.POSTGRES).equals("\"" + id + "\""));
		assertFalse(SelfHosterMySQL.quoteAndEscapeSqlId(id, SQLDialect.MYSQL).equals("`" + id + "`"));
        assertFalse(SelfHosterMySQL.quoteAndEscapeSqlId(id, SQLDialect.SQLITE).equals("`" + id + "`"));
	}

	/**
	 * @param hostingMode 
	 * @throws SQLException
	 */
	private void doTest(Document.HostingMode hostingMode) throws SQLException {
		final InputStream inputStream = SelfHostExampleTest.class.getClassLoader().getResourceAsStream("example_music_collection.glom");
		assertNotNull(inputStream);

		final Document document = new Document();
		assertTrue(document.load(inputStream));

		if (hostingMode == Document.HostingMode.HOSTING_MODE_POSTGRES_SELF) {
			selfHoster = new SelfHosterPostgreSQL(document);
		} else if (hostingMode == Document.HostingMode.HOSTING_MODE_MYSQL_SELF) {
			selfHoster = new SelfHosterMySQL(document);
        } else if (hostingMode == Document.HostingMode.HOSTING_MODE_SQLITE) {
            selfHoster = new SelfHosterSqlite(document, getContext());
		} else {
			// TODO: std::cerr << G_STRFUNC << ": This test function does not support the specified hosting_mode: " <<
			// hosting_mode << std::endl;
			assert false;
		}

		final boolean hosted = selfHoster.createAndSelfHostFromExample();
		assertTrue(hosted);
		
		SelfHostTestUtils.testExampleMusiccollectionData(selfHoster, document);
		
		if (selfHoster != null) {
			selfHoster.cleanup();
		}
	}

	public void tearDown() {
		if (selfHoster != null) {
			selfHoster.cleanup();
		}
	}
}

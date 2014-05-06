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

import java.io.InputStream;
import java.sql.SQLException;

import org.glom.app.libglom.Document;

/**
 * @author Murray Cumming <murrayc@openismus.com>
 * 
 */
public class SelfHostExampleNonNumericPrimaryKeysTest extends AndroidTestCase {

	private static SelfHosterSqlite selfHosterSqlite = null;

	public void test() throws SQLException {
		final InputStream inputStream = SelfHostExampleNonNumericPrimaryKeysTest.class.getClassLoader().getResourceAsStream("test_example_music_collection_text_pk_fields.glom");
		assertNotNull(inputStream);

		final Document document = new Document();
		assertTrue(document.load(inputStream));

		selfHosterSqlite = new SelfHosterSqlite(document, getContext());
		final boolean hosted = selfHosterSqlite.createAndSelfHostFromExample();
		assertTrue(hosted);
		
		SelfHostTestUtils.testExampleMusiccollectionData(selfHosterSqlite, document);
	}

	public void tearDown() {
		if (selfHosterSqlite != null) {
			selfHosterSqlite.cleanup();
		}
	}
}

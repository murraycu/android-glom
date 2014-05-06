/*
 * Copyright (C) 2013 Openismus GmbH
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

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.glom.app.SqlUtils;
import org.glom.app.libglom.Document;
import org.glom.app.libglom.DataItem;
import org.glom.app.libglom.Field;
import org.jooq.InsertResultStep;
import org.jooq.InsertSetStep;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.Factory;

import com.google.common.io.Files;

/**
 * @author Murray Cumming <murrayc@openismus.com>
 *
 */
public class SelfHoster {

	protected boolean selfHostingActive = false;
	protected Document document = null;
	protected String username = "";
	protected String password = "";
    private String filePath;

    /**
	 * 
	 */
	public SelfHoster(final Document document) {
		super();
		this.document = document;
	}
	
	public boolean createAndSelfHostFromExample() {
		if (!createAndSelfHostNewEmpty()) {
			// std::cerr << G_STRFUNC << ": test_create_and_selfhost_new_empty() failed." << std::endl;
			return false;
		}

		final boolean recreated = recreateDatabaseFromDocument(); /* TODO: Progress callback */
		if (!recreated) {
			if (!cleanup()) {
				return false;
			}
		}

		return recreated;
	}
	
	/**
	 * @return
	 */
	protected boolean recreateDatabaseFromDocument() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @return
	 */
	protected boolean createAndSelfHostNewEmpty() {
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 *
	 */
	public boolean cleanup() {
		//Derived classes should implement this.
		return false;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	/**
	 * @return
	 */
	protected boolean getSelfHostingActive() {
		return selfHostingActive;
	}

	/**
	 * @param document
	 * @param tableName
	 * @return
	 */
	protected boolean insertExampleData(final Connection connection, final Document document, final String tableName) {

		final Factory factory = new Factory(connection, getSqlDialect());
		final Table<Record> table = Factory.tableByName(tableName);

		final List<Map<String, DataItem>> exampleRows = document.getExampleRows(tableName);
		for (final Map<String, DataItem> row : exampleRows) {
			InsertSetStep<Record> insertStep = factory.insertInto(table);

			for (final Entry<String, DataItem> entry : row.entrySet()) {
				final String fieldName = entry.getKey();
				final DataItem value = entry.getValue();
				if (value == null) {
					continue;
				}

				final Field field = document.getField(tableName, fieldName);
				if (field == null) {
					continue;
				}

				final org.jooq.Field<Object> jooqField = Factory.fieldByName(field.getName());
				if (jooqField == null) {
					continue;
				}

				final Object fieldValue = value.getValue(field.getGlomType());
				insertStep = insertStep.set(jooqField, fieldValue);
			}

			if (!(insertStep instanceof InsertResultStep<?>)) {
				continue;
			}

			// We suppress the warning because we _do_ check the cast above.
			@SuppressWarnings("unchecked")
			final InsertResultStep<Record> insertResultStep = (InsertResultStep<Record>) insertStep;

			try {
				insertResultStep.fetchOne();
			} catch (final DataAccessException e) {
				System.out.println("createAndSelfHostNewEmpty(): insertResultStep failed.");
				e.printStackTrace();
				return false;
			}
			// TODO: Check that it worked.
		}

		return true;
	}


	/**
	 * @param document
	 * @return
	 */
	protected boolean addGroupsFromDocument(final Document document) {
		// TODO Auto-generated method stub
		return true;
	}

	/**
	 * @param document
	 * @return
	 */
	protected boolean setTablePrivilegesGroupsFromDocument(final Document document) {
		// TODO Auto-generated method stub
		return true;
	}
	
	/**
	 * @param filePath
	 * @return
	 */
	protected static boolean fileExists(final String filePath) {
		final File file = new File(filePath);
		return file.exists();
	}

	/**
	 * @param start
	 * @param end
	 * @return
	 */
	protected static int discoverFirstFreePort(final int start, final int end) {
		for (int port = start; port <= end; ++port) {
			try {
				final ServerSocket socket = new ServerSocket(port);
	
				// If the instantiation succeeded then the port was free:
				final int result = socket.getLocalPort(); // This must equal port.
				socket.close();
				return result;
			} catch (final IOException ex) {
				// try next port
			}
		}
	
		return 0;
	}
	
	/**
	 * @param path
	 * @return
	 */
	protected static boolean fileExistsAndIsExecutable(String path) {
		final File file = new File(path);
		if (!file.exists()) {
			return false;
		}

		if (!file.canExecute()) {
			return false;
		}

		return true;
	}
	
	/**
	 * @param portNumber
	 * @return
	 */
	protected String portNumberAsText(final int portNumber) {
		final NumberFormat format = NumberFormat.getInstance(Locale.US);
		format.setGroupingUsed(false); // TODO: Does this change it system-wide?
		return format.format(portNumber);
	}
	
	/**
	 */
	public Connection createConnection(boolean failureExpected) {
		//We don't just use SqlUtils.tryUsernameAndPassword() because it uses ComboPooledDataSource,
		//which does not automatically close its connections,
		//leading to errors because connections are already open.
		final SqlUtils.JdbcConnectionDetails details = SqlUtils.getJdbcConnectionDetails(document);
		if (details == null) {
			return null;
		}
		
		final Properties connectionProps = new Properties();
		connectionProps.put("user", this.username);
		connectionProps.put("password", this.password);

		Connection conn = null;
		try {
			//TODO: Remove these debug prints when we figure out why getConnection sometimes hangs. 
			//System.out.println("debug: SelfHosterPostgreSQL.createConnection(): before createConnection()");
			DriverManager.setLoginTimeout(10);
			conn = DriverManager.getConnection(details.jdbcURL, connectionProps);
			//System.out.println("debug: createConnection(): before createConnection()");
		} catch (final SQLException e) {
			if(!failureExpected) {
				e.printStackTrace();
			}
			return null;
		}

		return conn;
	}

	/**
	 * @param str
	 * @return
	 */
	protected String shellQuote(final String str) {
		// TODO: If we add the quotes then they seem to be used as part of the path, though that is not a problem with
		// the normal command line.
		return str;

		// TODO: Escape.
		// return "'" + str + "'";
	}
	
	/**
	 * @return The temporary directory where the file was saved.
	 */
	protected File saveDocumentCopy(Document.HostingMode hostingMode) {
		// Save a copy, specifying the path to file in a directory:
		// For instance, /tmp/testglom/testglom.glom");
		final String tempFilename = "testglom";
		final File tempFolder = Files.createTempDir();
		final File tempDir = new File(tempFolder, tempFilename);

		final String tempDirPath = tempDir.getPath();
		final String tempFilePath = tempDirPath + File.separator + tempFilename;
		final File file = new File(tempFilePath);

		// Make sure that the file does not exist yet:
		{
			tempDir.delete();
		}

		// Save the example as a real file:
		document.setHostingMode(hostingMode);
		document.setIsExampleFile(false);
		final boolean saved = document.save(file.getPath());
		if (!saved) {
			System.out.println("createAndSelfHostNewEmpty(): Document.save() failed.");
			return null; // TODO: Delete the directory.
		}

        filePath = tempFilePath;

		return tempDir;
	}

	/**
	 * @return
	 */
	public SQLDialect getSqlDialect() {
		//This must be overriden by the derived classes.
		return null;
	}

	/**
	 * @param name
	 * @return
	 */
	public static String quoteAndEscapeSqlId(final String name, final SQLDialect sqlDialect) {
		//final Factory factory = new Factory(connection, getSqlDialect());
		final org.jooq.Name jooqName = Factory.name(name);
		if(jooqName == null) {
			return null;
		}

		final Factory factory = new Factory(sqlDialect);
		return factory.render(jooqName);
	}

    public String getSelfHostedDirectoryPath() {
        final String pathFile = getFilePath();
        if (!TextUtils.isEmpty(pathFile)) {
            final File file = new File(pathFile);
            final File parent = file.getParentFile();
            if (parent == null) {
                // TODO: Warn.
                return "";
            }

            File dataDir = null;
            switch (document.getHostingMode()) {
                case HOSTING_MODE_POSTGRES_SELF:
                    dataDir = new File(parent, "glom_postgres_data");
                    break;
                case HOSTING_MODE_POSTGRES_CENTRAL:
                    dataDir = parent;
                    break;
                case HOSTING_MODE_MYSQL_SELF:
                    dataDir = new File(parent, "glom_mysql_data");
                    break;
                case HOSTING_MODE_MYSQL_CENTRAL:
                    dataDir = parent;
                    break;
                case HOSTING_MODE_SQLITE:
                    dataDir = parent;
                    break;
                default:
                    // TODO: Warn.
                    break;
            }

            if (dataDir != null) {
                return dataDir.getPath();
            }
        }

        // TODO: std::cerr << G_STRFUNC << ": returning empty string." << std::endl;
        return "";
    }

    protected String getFilePath() {
        return filePath;
    }

}
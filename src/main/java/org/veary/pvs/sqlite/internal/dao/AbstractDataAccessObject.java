/*
 * MIT License
 *
 * Copyright (c) 2019 ColonelBlimp
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.veary.pvs.sqlite.internal.dao;

import static com.google.common.base.Preconditions.checkNotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.veary.pvs.core.Constants;
import org.veary.pvs.sqlite.ConnectionManager;

/**
 * Provides common methods for all <i>Data Access Objects</i> subclasses.
 *
 * @author Marc L. Veary
 * @since 1.0
 */
abstract class AbstractDataAccessObject {

    private static final Logger log = LogManager.getLogger(AbstractDataAccessObject.class);
    private final ConnectionManager manager;

    /**
     * Constructor.
     *
     * @param manager a {@link ConnectionManager} object injected at the subclass.
     */
    protected AbstractDataAccessObject(ConnectionManager manager) {
        this.manager = manager;
    }

    /**
     * Executes the given SQL returning a list of results (rows) if there are any.
     *
     * @param sql  the DML statement to be executed
     * @param args a varargs list of Strings
     * @return {@code List<Map<Object, Object>>}. Cannot be {@code null}.
     */
    protected List<Map<Object, Object>> executeSqlAndReturnList(String sql, String... args)
        throws SQLException {
        log.trace(Constants.LOG_CALLED);

        List<Map<Object, Object>> result = new ArrayList<>(0);

        try (Connection conn = this.manager.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(sql,
                PreparedStatement.RETURN_GENERATED_KEYS)) {
                int index = 1;

                for (Object arg : args) {
                    stmt.setObject(index++, arg);
                }

                if (sql.startsWith("SELECT")) {
                    try (ResultSet rset = stmt.executeQuery()) {
                        result = resultSetToList(rset);
                    }
                } else {
                    // INSERT, UPDATE and DELETE
                    stmt.executeUpdate();
                    try (ResultSet rset = stmt.getGeneratedKeys()) {
                        result = resultSetToList(rset);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Returns the identifier (id) of the last inserted row.
     *
     * @param results a {@code List<Map<Object, Object>>}
     * @return if the value is {@code zero} then nothing happened, otherwise the
     *         unique identifier of the row acted upon.
     */
    protected int getRowId(List<Map<Object, Object>> results) {
        int rowid = 0;
        if (!results.isEmpty()) {
            Map<Object, Object> data = results.get(0);
            rowid = ((Integer) data.get("last_insert_rowid()")).intValue();
        }
        return rowid;
    }

    /**
     * Takes a {@code ResultSet} and converts each row to a {@code Map} inserted
     * into a {@code List}.
     *
     * @param rset a {@code ResultSet}
     * @return a {@code List<Map<Object, Object>>}, cannot be {@code null}.
     * @throws SQLException if there is a problem accessing the {@code ResultSet}
     */
    protected List<Map<Object, Object>> resultSetToList(ResultSet rset) throws SQLException {
        log.trace(Constants.LOG_CALLED);
        checkNotNull(rset, "The 'rset' parameter is null!");
        ResultSetMetaData md = rset.getMetaData();
        int columns = md.getColumnCount();
        List<Map<Object, Object>> list = new ArrayList<>();

        while (rset.next()) {
            Map<Object, Object> row = new HashMap<>(columns);
            for (int i = 1; i <= columns; i++) {
                row.put(md.getColumnName(i), rset.getObject(i));
            }
            list.add(row);
        }

        return list;
    }

    /**
     * Mark the start of an SQL transaction.
     * @return A {@code Connection} object
     * @throws SQLException if there is an issue
     */
    protected Connection startTransaction() throws SQLException {
        log.trace(Constants.LOG_CALLED);
        Connection conn = this.manager.getConnection();
        conn.setAutoCommit(false);
        return conn;
    }

    /**
     * Mark the end of an SQL transaction. Commits and resets auto commit.
     * @param conn The {@code Connection} object
     * @param expected The expected number of changes.
     * @param actual The actual number of changes
     * @return {@code true} if everything went well, otherwise {@code false}
     */
    protected boolean endTransaction(Connection conn, int expected, int actual) {
        log.trace(Constants.LOG_CALLED);
        boolean retval = false;

        if (expected != actual) {
            rollbackTransaction(conn);
        }

        try {
            conn.commit();
            conn.setAutoCommit(true);
            retval = true;
        } catch (SQLException e) {
            log.error("Unexpected error (ignored) {}", e); //$NON-NLS-1$
        }

        return retval;
    }

    /**
     * Rolls back any changes.
     * @param conn The {@code Connection} object
     */
    protected void rollbackTransaction(Connection conn) {
        log.trace(Constants.LOG_CALLED);
        try {
            log.warn("Rolling back transaction");
            conn.rollback();
            log.warn("Rollback successful");
        } catch (SQLException e) {
            log.error("Unexpected error (ignored) {}", e); //$NON-NLS-1$
        }
    }
}

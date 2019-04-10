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

package org.veary.pvs.sqlite.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.inject.Inject;

import org.veary.pvs.exceptions.DataAccessException;
import org.veary.pvs.sqlite.ConnectionManager;
import org.veary.pvs.sqlite.DatabaseManager;

final class DatabaseManagerImpl implements DatabaseManager {

    private ConnectionManager manager;

    @Inject
    protected DatabaseManagerImpl(ConnectionManager manager) {
        this.manager = manager;
    }

    @Override
    public void createTables() {
        createAccountTable();
        createPeriodTable();
        createJournalTable();
        createPostingTable();
        createDayBookTable();
    }

    @Override
    public void dropTables() {
        sqliteExecute("DROP TABLE IF EXISTS account"); //$NON-NLS-1$
        sqliteExecute("DROP TABLE IF EXISTS daybook"); //$NON-NLS-1$
        sqliteExecute("DROP TABLE IF EXISTS ledger"); //$NON-NLS-1$
        sqliteExecute("DROP TABLE IF EXISTS period"); //$NON-NLS-1$
        sqliteExecute("DROP TABLE IF EXISTS journal"); //$NON-NLS-1$
    }

    private void createDayBookTable() {
        StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS daybook ("); //$NON-NLS-1$
        sb.append("id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, "); //$NON-NLS-1$
        sb.append("name TEXT NOT NULL UNIQUE, "); //$NON-NLS-1$
        sb.append("description TEXT, "); //$NON-NLS-1$
        sb.append("period_id INTEGER NOT NULL, "); //$NON-NLS-1$
        sb.append("FOREIGN KEY(period_id) REFERENCES period(id) "); //$NON-NLS-1$
        sb.append("ON UPDATE RESTRICT ON DELETE RESTRICT)"); //$NON-NLS-1$
        sqliteExecute(sb.toString());
    }

    private void createAccountTable() {
        StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS account ("); //$NON-NLS-1$
        sb.append("id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, "); //$NON-NLS-1$
        sb.append("name TEXT NOT NULL UNIQUE, "); //$NON-NLS-1$
        sb.append("description TEXT, "); //$NON-NLS-1$
        sb.append("type INTEGER NOT NULL)"); //$NON-NLS-1$
        sqliteExecute(sb.toString());
    }

    private void createPeriodTable() {
        StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS period ("); //$NON-NLS-1$
        sb.append("id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, "); //$NON-NLS-1$
        sb.append("description TEXT, "); //$NON-NLS-1$
        sb.append("name TEXT NOT NULL UNIQUE)"); //$NON-NLS-1$
        sqliteExecute(sb.toString());
    }

    private void createJournalTable() {
        StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS journal ("); //$NON-NLS-1$
        sb.append("id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, "); //$NON-NLS-1$
        sb.append("date TEXT NOT NULL, "); //$NON-NLS-1$
        sb.append("ref TEXT NOT NULL UNIQUE, "); //$NON-NLS-1$
        sb.append("narrative TEXT NOT NULL, "); //$NON-NLS-1$
        sb.append("daybook_id INTEGER NOT NULL, "); //$NON-NLS-1$
        sb.append("FOREIGN KEY(daybook_id) REFERENCES daybook(id) "); //$NON-NLS-1$
        sb.append("ON UPDATE RESTRICT ON DELETE RESTRICT)"); //$NON-NLS-1$
        sqliteExecute(sb.toString());
    }

    private void createPostingTable() {
        StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS ledger ("); //$NON-NLS-1$
        sb.append("journal_id INTEGER NOT NULL, "); //$NON-NLS-1$
        sb.append("account_id INTEGER NOT NULL, "); //$NON-NLS-1$
        sb.append("amount INTEGER NOT NULL, "); //$NON-NLS-1$
        sb.append("FOREIGN KEY(journal_id) REFERENCES journal(id) "); //$NON-NLS-1$
        sb.append("ON UPDATE RESTRICT ON DELETE RESTRICT, "); //$NON-NLS-1$
        sb.append("FOREIGN KEY(account_id) REFERENCES account(id) "); //$NON-NLS-1$
        sb.append("ON UPDATE RESTRICT ON DELETE RESTRICT)"); //$NON-NLS-1$
        sqliteExecute(sb.toString());
    }

    private void sqliteExecute(String sql) {
        try (Connection conn = manager.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}

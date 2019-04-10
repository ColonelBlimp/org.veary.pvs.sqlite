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

package org.veary.pvs.sqlite;

import com.client.tests.AbstractTomcatJndi;
import com.google.inject.Guice;
import com.google.inject.Injector;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Assert;
import org.junit.Test;
import org.veary.pvs.api.GuiceApiModule;


public class DatabaseManagerTest extends AbstractTomcatJndi {

    @Test
    public void createTables() throws SQLException {
        tomcatJndiSetup();
        Injector injector = Guice.createInjector(
            new GuiceApiModule(),
            new GuiceSqliteModule()
            );
        DatabaseManager dbManager = injector.getInstance(DatabaseManager.class);
        Assert.assertNotNull(dbManager);
        dbManager.createTables();

        ConnectionManager connManager = injector.getInstance(ConnectionManager.class);
        Assert.assertNotNull(connManager);

        try (Connection conn = connManager.getConnection()) {
            String sql = "SELECT sql FROM sqlite_master WHERE name = 'account'";

            try (Statement stmt = conn.createStatement()) {
                Assert.assertTrue(stmt.execute(sql));
            }
        }

        try (Connection conn = connManager.getConnection()) {
            String sql = "SELECT sql FROM sqlite_master WHERE name = 'period'";

            try (Statement stmt = conn.createStatement()) {
                Assert.assertTrue(stmt.execute(sql));
            }
        }

        try (Connection conn = connManager.getConnection()) {
            String sql = "SELECT sql FROM sqlite_master WHERE name = 'daybook'";

            try (Statement stmt = conn.createStatement()) {
                Assert.assertTrue(stmt.execute(sql));
            }
        }

        try (Connection conn = connManager.getConnection()) {
            String sql = "SELECT sql FROM sqlite_master WHERE name = 'config'";

            try (Statement stmt = conn.createStatement()) {
                Assert.assertTrue(stmt.execute(sql));
            }
        }

        try (Connection conn = connManager.getConnection()) {
            String sql = "SELECT sql FROM sqlite_master WHERE name = 'journal'";

            try (Statement stmt = conn.createStatement()) {
                Assert.assertTrue(stmt.execute(sql));
            }
        }

        try (Connection conn = connManager.getConnection()) {
            String sql = "SELECT sql FROM sqlite_master WHERE name = 'ledger'";

            try (Statement stmt = conn.createStatement()) {
                Assert.assertTrue(stmt.execute(sql));
            }
        }

        dbManager.dropTables();
    }
}

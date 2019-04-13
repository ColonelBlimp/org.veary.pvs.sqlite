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

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.veary.pvs.api.AccountFacade;
import org.veary.pvs.api.GuiceApiModule;
import org.veary.pvs.exceptions.ApiException;
import org.veary.pvs.exceptions.DataAccessException;
import org.veary.pvs.sqlite.ConnectionManager;

import com.client.tests.AbstractTomcatJndi;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class DaoExceptionTest extends AbstractTomcatJndi {

    private Injector injector;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() {
        tomcatJndiSetup();
        injector = Guice.createInjector(
            new GuiceApiModule(),
            new GuiceSqliteTestModule()
            );
    }

    @After
    public void teardown() {
        this.tomcatJNDI.tearDown();
    }

    @Test
    public void accountDaoExceptionOne() throws ApiException {
        ConnectionManager manager = injector.getInstance(ConnectionManager.class);
        Assert.assertNotNull(manager);
        try {
            Connection conn = manager.getConnection();
        } catch (SQLException e) {
            System.out.println(">>> " + e.getMessage());
        }
        
        thrown.expect(DataAccessException.class);
        AccountFacade facade = injector.getInstance(AccountFacade.class);
        Assert.assertNotNull(facade);
        facade.createAssetAccount("Cash");
    }
}

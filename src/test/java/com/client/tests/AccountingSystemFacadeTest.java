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

package com.client.tests;

import com.google.inject.Guice;
import com.google.inject.Injector;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.veary.pvs.api.AccountFacade;
import org.veary.pvs.api.AccountingSystemFacade;
import org.veary.pvs.api.DayBookFacade;
import org.veary.pvs.api.PeriodFacade;
import org.veary.pvs.api.internal.GuiceApiModule;
import org.veary.pvs.core.Money;
import org.veary.pvs.model.Account;
import org.veary.pvs.model.Account.Type;
import org.veary.pvs.model.DayBook;
import org.veary.pvs.model.Transaction;
import org.veary.pvs.model.internal.GuiceModelModule;
import org.veary.pvs.sqlite.DatabaseManager;
import org.veary.pvs.sqlite.internal.dao.GuiceSqliteDaoModule;

public class AccountingSystemFacadeTest extends AbstractTomcatJndi {

    private static final String CASH_ACC = "Cash";
    private static final String FUEL_ACC = "Fuel";
    private static final String YEAR_PRD = "YEAR";
    private static final String BOOK_JAN = "January";

    private Injector injector;

    private DayBook dayBook;
    private Account fromAccount;
    private Account toAccount;

    @Before
    public void setup() {
        tomcatJndiSetup();
        injector = Guice.createInjector(
            new GuiceApiModule(),
            new GuiceModelModule(),
            new GuiceSqliteDaoModule()
            );
        DatabaseManager manager = injector.getInstance(DatabaseManager.class);
        manager.createTables();

        AccountFacade accountFacade = injector.getInstance(AccountFacade.class);
        Assert.assertTrue(accountFacade.createAccount(CASH_ACC, Type.ASSET) > 0);
        Assert.assertTrue(accountFacade.createAccount(FUEL_ACC, Type.EXPENSE) > 0);
        Optional<Account> from = accountFacade.getAccountByName(CASH_ACC);
        if (from.isPresent()) {
            this.fromAccount = from.get();
        } else {
            throw new AssertionError("Cannot find: " + CASH_ACC);
        }
        Optional<Account> to = accountFacade.getAccountByName(FUEL_ACC);
        if (to.isPresent()) {
            this.toAccount = to.get();
        } else {
            throw new AssertionError("Cannot find: " + FUEL_ACC);
        }
        PeriodFacade periodFacade = injector.getInstance(PeriodFacade.class);
        int periodId = periodFacade.createPeriod(YEAR_PRD);
        Assert.assertTrue(periodId > 0);

        DayBookFacade bookFacade = injector.getInstance(DayBookFacade.class);
        bookFacade.createDayBook(BOOK_JAN, periodId);
        Optional<DayBook> book = bookFacade.getDayBookByName(BOOK_JAN);
        if (book.isPresent()) {
            this.dayBook = book.get();
        } else {
            throw new AssertionError("Cannot find: " + BOOK_JAN);
        }
    }

    @After
    public void teardown() {
        DatabaseManager dbManager = injector.getInstance(DatabaseManager.class);
        dbManager.dropTables();
        this.tomcatJNDI.tearDown();
    }

    @Test
    public void postTransactionAndGet() {
        AccountingSystemFacade facade = injector.getInstance(AccountingSystemFacade.class);
        Assert.assertNotNull(facade);

        Assert.assertTrue(facade.postTransaction(
            ZonedDateTime.now(),
            "Fuel for Land Rover",
            new Money(BigDecimal.valueOf(1000000, 2)),
            this.fromAccount,
            this.toAccount,
            "PV20190331001",
            this.dayBook.getId()));

        List<Transaction> list = facade.getTransactions();
        Assert.assertNotNull(list);
        Assert.assertFalse(list.isEmpty());
        Assert.assertTrue(1 == list.size());
        Transaction tx = list.get(0);
        Assert.assertTrue(2 == tx.getLedgerEntries().size());
    }
}

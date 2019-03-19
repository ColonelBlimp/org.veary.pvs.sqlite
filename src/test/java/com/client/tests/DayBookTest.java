/*
 *  A bespoke Payment Voucher System (PVS).
 *  Copyright (C) 2019, Marc L. Veary
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.client.tests;

import com.google.inject.Guice;
import com.google.inject.Injector;

import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.veary.pvs.api.DayBookFacade;
import org.veary.pvs.api.PeriodFacade;
import org.veary.pvs.api.internal.GuiceApiModule;
import org.veary.pvs.model.DayBook;
import org.veary.pvs.model.Period;
import org.veary.pvs.model.internal.GuiceModelModule;
import org.veary.pvs.sqlite.DatabaseManager;
import org.veary.pvs.sqlite.internal.dao.GuiceSqliteDaoModule;

public class DayBookTest extends AbstractTomcatJndi {

    private static final String PERIOD_NAME = "YEAR";
    private static final String DAYBOOK_NAME = "January";

    private Injector injector;
    private Period period;

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

        PeriodFacade periodFacade = injector.getInstance(PeriodFacade.class);
        Assert.assertNotNull(periodFacade);
        int periodId = periodFacade.createPeriod(PERIOD_NAME);
        Optional<Period> object = periodFacade.getPeriodById(periodId);
        if (object.isPresent()) {
            this.period = object.get();
        } else {
            throw new AssertionError("Unable to create a Period object!");
        }
    }

    @After
    public void teardown() {
        DatabaseManager dbManager = injector.getInstance(DatabaseManager.class);
        dbManager.dropTables();
        this.tomcatJNDI.tearDown();
    }

    @Test
    public void createDayBook() {
        DayBookFacade facade = injector.getInstance(DayBookFacade.class);
        Assert.assertNotNull(facade);
        int id = facade.createDayBook(DAYBOOK_NAME, this.period.getId());
        Assert.assertTrue(id > 0);

        Optional<DayBook> result = facade.getDayBookByName(DAYBOOK_NAME);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isPresent());
        DayBook book = result.get();
        Assert.assertNotNull(book);
        Assert.assertEquals(DAYBOOK_NAME, book.getName());
        Assert.assertTrue(id == book.getId());
        Assert.assertTrue(this.period.getId() == book.getPeriodId());
    }

    @Test
    public void getAllDayBooks() {
        DayBookFacade facade = injector.getInstance(DayBookFacade.class);
        Assert.assertNotNull(facade);
        facade.createDayBook(DAYBOOK_NAME, this.period.getId());
        facade.createDayBook(DAYBOOK_NAME + "1", this.period.getId());
        facade.createDayBook(DAYBOOK_NAME + "2", this.period.getId());
        facade.createDayBook(DAYBOOK_NAME + "3", this.period.getId());

        List<DayBook> list = facade.getDayBooks();
        Assert.assertNotNull(list);
        Assert.assertFalse(list.isEmpty());
        Assert.assertTrue(4 == list.size());

        for (DayBook book : list) {
            Assert.assertTrue(facade.deleteDayBook(book.getId()));
        }

        list = facade.getDayBooks();
        Assert.assertNotNull(list);
        Assert.assertTrue(list.isEmpty());
    }

    @Test
    public void updateDayBooks() {
        DayBookFacade facade = injector.getInstance(DayBookFacade.class);
        Assert.assertNotNull(facade);
        facade.createDayBook(DAYBOOK_NAME, this.period.getId());
        facade.createDayBook(DAYBOOK_NAME + "1", this.period.getId());
        facade.createDayBook(DAYBOOK_NAME + "2", this.period.getId());
        facade.createDayBook(DAYBOOK_NAME + "3", this.period.getId());

        List<DayBook> list = facade.getDayBooks();
        Assert.assertNotNull(list);
        Assert.assertFalse(list.isEmpty());
        Assert.assertTrue(4 == list.size());

        for (DayBook book : list) {
            String name = book.getName();
            Assert.assertTrue(facade.updateDayBook(name, name + "X"));
        }

        list = facade.getDayBooks();
        Assert.assertNotNull(list);
        Assert.assertFalse(list.isEmpty());
        Assert.assertTrue(4 == list.size());

        for (DayBook book : list) {
            Assert.assertTrue(book.getName().endsWith("X"));
        }
    }
}

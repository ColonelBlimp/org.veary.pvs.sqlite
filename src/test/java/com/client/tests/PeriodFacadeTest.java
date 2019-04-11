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
import org.veary.pvs.api.GuiceApiModule;
import org.veary.pvs.api.PeriodFacade;
import org.veary.pvs.exceptions.ApiException;
import org.veary.pvs.model.Period;
import org.veary.pvs.sqlite.DatabaseManager;
import org.veary.pvs.sqlite.GuiceSqliteModule;

public class PeriodFacadeTest extends AbstractTomcatJndi {

    private static final String PERIOD_NAME = "YEAR";

    private Injector injector;

    @Before
    public void setup() {
        tomcatJndiSetup();
        injector = Guice.createInjector(
            new GuiceApiModule(),
            new GuiceSqliteModule()
            );
        DatabaseManager manager = injector.getInstance(DatabaseManager.class);
        manager.createTables();
    }

    @After
    public void teardown() {
        DatabaseManager dbManager = injector.getInstance(DatabaseManager.class);
        dbManager.dropTables();
        this.tomcatJNDI.tearDown();
    }

    @Test
    public void createPeriod() throws ApiException {
        PeriodFacade facade = injector.getInstance(PeriodFacade.class);
        Assert.assertNotNull(facade);
        int id = facade.createPeriod(PERIOD_NAME);
        Assert.assertTrue(id > 0);

        Optional<Period> result = facade.getPeriodByName(PERIOD_NAME);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isPresent());
        Period period = result.get();
        Assert.assertNotNull(period);
        Assert.assertEquals(PERIOD_NAME, period.getName());
        Assert.assertTrue(id == period.getId());
    }

    @Test
    public void getAllPeriods() throws ApiException {
        PeriodFacade facade = injector.getInstance(PeriodFacade.class);
        Assert.assertNotNull(facade);
        facade.createPeriod(PERIOD_NAME);
        facade.createPeriod(PERIOD_NAME + "1");
        facade.createPeriod(PERIOD_NAME + "2");
        facade.createPeriod(PERIOD_NAME + "3");

        List<Period> list = facade.getPeriods();
        Assert.assertNotNull(list);
        Assert.assertFalse(list.isEmpty());
        // Default data adds one to the list!
        Assert.assertTrue(5 == list.size());

        for (Period period : list) {
            Assert.assertTrue(facade.deletePeriod(period.getId()));
        }

        list = facade.getPeriods();
        Assert.assertNotNull(list);
        Assert.assertTrue(list.isEmpty());
    }

    @Test
    public void updatePeriods() throws ApiException {
        PeriodFacade facade = injector.getInstance(PeriodFacade.class);
        Assert.assertNotNull(facade);
        facade.createPeriod(PERIOD_NAME + "1");
        facade.createPeriod(PERIOD_NAME + "2");
        facade.createPeriod(PERIOD_NAME + "3");

        List<Period> list = facade.getPeriods();
        Assert.assertNotNull(list);
        Assert.assertFalse(list.isEmpty());
        Assert.assertTrue(4 == list.size());


        for (Period period : list) {
            String name = period.getName();
            Assert.assertTrue(facade.updatePeriod(name, name + "X"));
        }

        list = facade.getPeriods();
        Assert.assertNotNull(list);
        Assert.assertFalse(list.isEmpty());
        Assert.assertTrue(4 == list.size());

        for (Period period : list) {
            Assert.assertTrue(period.getName().endsWith("X"));
        }
    }

    @Test
    public void uniqueConstraintNameCreate() {
        PeriodFacade facade = injector.getInstance(PeriodFacade.class);
        Assert.assertNotNull(facade);
        try {
            facade.createPeriod(PERIOD_NAME);
            facade.createPeriod(PERIOD_NAME);
        } catch (ApiException e) {
            Assert.assertTrue(e.getMessage().contains(
                "Abort due to constraint violation (UNIQUE constraint failed: period.name)"));
        }
    }

    @Test
    public void uniqueConstraintNameUpdate() {
        PeriodFacade facade = injector.getInstance(PeriodFacade.class);
        Assert.assertNotNull(facade);
        try {
            facade.createPeriod(PERIOD_NAME);
            facade.createPeriod(PERIOD_NAME + "2");
            facade.updatePeriod(PERIOD_NAME, PERIOD_NAME + "2");
        } catch (ApiException e) {
            Assert.assertTrue(e.getMessage().contains(
                "Abort due to constraint violation (UNIQUE constraint failed: period.name)"));
        }
    }
}

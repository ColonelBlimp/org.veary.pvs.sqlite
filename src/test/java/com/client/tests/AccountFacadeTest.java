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
import org.veary.pvs.api.AccountFacade;
import org.veary.pvs.api.GuiceApiModule;
import org.veary.pvs.exceptions.ApiException;
import org.veary.pvs.model.Account;
import org.veary.pvs.model.Account.Type;
import org.veary.pvs.sqlite.DatabaseManager;
import org.veary.pvs.sqlite.GuiceSqliteModule;

public class AccountFacadeTest extends AbstractTomcatJndi {

    private static final String CASH_NAME = "Cash";
    private static final String EXPENSE_NAME = "Fuel";
    private static final String LIABILITY_NAME = "Loan";
    private static final String INCOME_NAME = "Salary";
    private static final String OPENING_BALANCE = "Opening Balances";

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
    public void createAssetAccount() throws ApiException {
        AccountFacade facade = injector.getInstance(AccountFacade.class);
        Assert.assertNotNull(facade);
        int id = facade.createAssetAccount(CASH_NAME);
        Assert.assertTrue(id > 0);

        Optional<Account> result = facade.getAccountByName(CASH_NAME);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isPresent());
        Account account = result.get();
        Assert.assertNotNull(account);
        Assert.assertEquals(CASH_NAME, account.getName());
        Assert.assertEquals(Type.ASSET, account.getType());
        Assert.assertTrue(id == account.getId());
    }

    @Test
    public void createExpenseAccount() throws ApiException {
        AccountFacade facade = injector.getInstance(AccountFacade.class);
        Assert.assertNotNull(facade);
        int id = facade.createExpenseAccount(EXPENSE_NAME);
        Assert.assertTrue(id > 0);

        Optional<Account> result = facade.getAccountById(id);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isPresent());
        Account account = result.get();
        Assert.assertNotNull(account);
        Assert.assertEquals(EXPENSE_NAME, account.getName());
        Assert.assertEquals(Type.EXPENSE, account.getType());
        Assert.assertTrue(id == account.getId());
    }

    @Test
    public void createLiabilityAccount() throws ApiException {
        AccountFacade facade = injector.getInstance(AccountFacade.class);
        Assert.assertNotNull(facade);
        int id = facade.createLiabilityAccount(LIABILITY_NAME);
        Assert.assertTrue(id > 0);

        Optional<Account> result = facade.getAccountById(id);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isPresent());
        Account account = result.get();
        Assert.assertNotNull(account);
        Assert.assertEquals(LIABILITY_NAME, account.getName());
        Assert.assertEquals(Type.LIABILITY, account.getType());
        Assert.assertTrue(id == account.getId());
    }

    @Test
    public void createIncomeAccount() throws ApiException {
        AccountFacade facade = injector.getInstance(AccountFacade.class);
        Assert.assertNotNull(facade);
        int id = facade.createIncomeAccount(INCOME_NAME);
        Assert.assertTrue(id > 0);

        Optional<Account> result = facade.getAccountById(id);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isPresent());
        Account account = result.get();
        Assert.assertNotNull(account);
        Assert.assertEquals(INCOME_NAME, account.getName());
        Assert.assertEquals(Type.INCOME, account.getType());
        Assert.assertTrue(id == account.getId());
    }

    @Test
    public void createRelatedEarningAccount() throws ApiException {
        AccountFacade facade = injector.getInstance(AccountFacade.class);
        Assert.assertNotNull(facade);
        int id = facade.createAccount(OPENING_BALANCE, Account.Type.RETAINED_EARNINGS);
        Assert.assertTrue(id > 0);

        Optional<Account> result = facade.getAccountById(id);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isPresent());
        Account account = result.get();
        Assert.assertNotNull(account);
        Assert.assertEquals(OPENING_BALANCE, account.getName());
        Assert.assertEquals(Type.RETAINED_EARNINGS, account.getType());
        Assert.assertTrue(id == account.getId());
    }

    @Test
    public void getAllAccounts() throws ApiException {
        AccountFacade facade = injector.getInstance(AccountFacade.class);
        Assert.assertNotNull(facade);
        facade.createAssetAccount(CASH_NAME);
        facade.createExpenseAccount(EXPENSE_NAME);
        facade.createLiabilityAccount(LIABILITY_NAME);
        facade.createIncomeAccount(INCOME_NAME);

        List<Account> list = facade.getAccounts();
        Assert.assertNotNull(list);
        Assert.assertFalse(list.isEmpty());
        Assert.assertTrue(4 == list.size());

        for (Account account : list) {
            Assert.assertTrue(facade.deleteAccount(account.getId()));
        }

        list = facade.getAccounts();
        Assert.assertNotNull(list);
        Assert.assertTrue(list.isEmpty());
    }

    @Test
    public void updateAccounts() throws ApiException {
        AccountFacade facade = injector.getInstance(AccountFacade.class);
        Assert.assertNotNull(facade);
        facade.createAssetAccount(CASH_NAME);
        facade.createExpenseAccount(EXPENSE_NAME);
        facade.createLiabilityAccount(LIABILITY_NAME);
        facade.createIncomeAccount(INCOME_NAME);

        List<Account> list = facade.getAccounts();
        Assert.assertNotNull(list);
        Assert.assertFalse(list.isEmpty());
        Assert.assertTrue(4 == list.size());

        for (Account account : list) {
            String name = account.getName();
            Assert.assertTrue(facade.updateAccount(name, name + "1"));
        }

        list = facade.getAccounts();
        Assert.assertNotNull(list);
        Assert.assertFalse(list.isEmpty());
        Assert.assertTrue(4 == list.size());

        for (Account account : list) {
            Assert.assertTrue(account.getName().endsWith("1"));
        }
    }

    @Test
    public void uniqueConstraintNameCreate() {
        AccountFacade facade = injector.getInstance(AccountFacade.class);
        Assert.assertNotNull(facade);
        try {
            facade.createAssetAccount(CASH_NAME);
            facade.createAssetAccount(CASH_NAME);
        } catch (ApiException e) {
            Assert.assertTrue(e.getMessage().contains(
                "Abort due to constraint violation (UNIQUE constraint failed: account.name)"));
        }
    }

    @Test
    public void uniqueConstraintNameUpdate() {
        AccountFacade facade = injector.getInstance(AccountFacade.class);
        Assert.assertNotNull(facade);
        try {
            facade.createAssetAccount(CASH_NAME);
            facade.createExpenseAccount(EXPENSE_NAME);
            facade.updateAccount(CASH_NAME, EXPENSE_NAME);
        } catch (ApiException e) {
            Assert.assertTrue(e.getMessage().contains(
                "Abort due to constraint violation (UNIQUE constraint failed: account.name)"));
        }
    }
}

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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sqlite.SQLiteErrorCode;
import org.sqlite.SQLiteException;
import org.veary.pvs.core.Constants;
import org.veary.pvs.dao.AccountDataAccessObject;
import org.veary.pvs.exceptions.ApiException;
import org.veary.pvs.exceptions.DataAccessException;
import org.veary.pvs.model.Account;
import org.veary.pvs.model.Account.Type;
import org.veary.pvs.model.ModelFactory;
import org.veary.pvs.sqlite.ConnectionManager;

/**
 * Package scoped, concrete implementation of the {@code AccountDataAccessObject} for SQLite.
 *
 * @author Marc L. Veary
 * @since 1.0
 */
@Singleton
final class AccountDataAccessObjectImpl extends AbstractDataAccessObject
implements AccountDataAccessObject {

    private static final Logger log = LogManager.getLogger(AccountDataAccessObjectImpl.class);
    private final ModelFactory factory;

    @Inject
    protected AccountDataAccessObjectImpl(ConnectionManager manager, ModelFactory factory) {
        super(manager);
        this.factory = factory;
    }

    @Override
    public Optional<Account> getById(int id) {
        log.trace(Constants.LOG_CALLED);
        return processSingleResult("SELECT * from account WHERE id=?", String.valueOf(id));
    }

    @Override
    public Optional<Account> getByName(String uniqueName) {
        log.trace(Constants.LOG_CALLED);
        return processSingleResult("SELECT * from account WHERE name=?", uniqueName);
    }

    @Override
    public List<Account> getAccounts() {
        log.trace(Constants.LOG_CALLED);

        try {
            List<Map<Object, Object>> results = executeSqlAndReturnList("SELECT * from account");

            List<Account> list = new ArrayList<>(results.size());
            for (Map<Object, Object> row : results) {
                Optional<Account> account = Optional.ofNullable(this.factory.buildAccountObject(row));
                if (account.isPresent()) {
                    list.add(account.get());
                }
            }

            return list;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    @Override
    public int createAccount(String uniqueName, Type type) throws ApiException {
        log.trace(Constants.LOG_CALLED);

        try {
            List<Map<Object, Object>> results = executeSqlAndReturnList(
                "INSERT INTO account(name,type) VALUES(?,?)", uniqueName,
                String.valueOf(type.getValue()));
            return getRowId(results);
        } catch (SQLException e) {
            SQLiteException ex = (SQLiteException) e;
            if (ex.getResultCode() == SQLiteErrorCode.SQLITE_CONSTRAINT) {
                throw new ApiException(e);
            }
            throw new DataAccessException(e);
        }
    }

    @Override
    public boolean updateAccount(String uniqueName, String newUniqueName) throws ApiException {
        log.trace(Constants.LOG_CALLED);

        try {
            List<Map<Object, Object>> results = executeSqlAndReturnList(
                "UPDATE account SET name=? WHERE name=?", newUniqueName, uniqueName);

            boolean retval = false;
            if (getRowId(results) > 0) {
                retval = true;
            }
            return retval;
        } catch (SQLException e) {
            SQLiteException ex = (SQLiteException) e;
            if (ex.getResultCode() == SQLiteErrorCode.SQLITE_CONSTRAINT) {
                throw new ApiException(e);
            }
            throw new DataAccessException(e);
        }
    }

    @Override
    public boolean deleteAccount(int id) {
        log.trace(Constants.LOG_CALLED);

        try {
            List<Map<Object, Object>> results = executeSqlAndReturnList(
                "DELETE FROM account WHERE id=?", String.valueOf(id));

            boolean retval = false;
            if (getRowId(results) > 0) {
                retval = true;
            }

            return retval;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private Optional<Account> processSingleResult(String sql, String... args) {
        log.trace(Constants.LOG_CALLED);

        try {
            List<Map<Object, Object>> results = executeSqlAndReturnList(sql, args);
            if (results.isEmpty()) {
                return Optional.ofNullable(null);
            }

            return Optional.ofNullable(this.factory.buildAccountObject(results.get(0)));
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}

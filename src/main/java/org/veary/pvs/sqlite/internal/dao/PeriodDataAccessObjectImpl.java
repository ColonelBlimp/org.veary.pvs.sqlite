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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.veary.pvs.core.Constants;
import org.veary.pvs.dao.PeriodDataAccessObject;
import org.veary.pvs.model.ModelFactory;
import org.veary.pvs.model.Period;
import org.veary.pvs.sqlite.ConnectionManager;

/**
 * Package scoped, concrete implementation of the {@code PeriodDataAccessObject} for SQLite.
 *
 * @author Marc L. Veary
 * @since 1.0
 */
@Singleton
final class PeriodDataAccessObjectImpl extends AbstractDataAccessObject
implements PeriodDataAccessObject {

    private static final Logger log = LogManager.getLogger(PeriodDataAccessObjectImpl.class);
    private final ModelFactory factory;

    @Inject
    protected PeriodDataAccessObjectImpl(ConnectionManager manager, ModelFactory factory) {
        super(manager);
        this.factory = factory;
    }

    @Override
    public Optional<Period> getById(int id) {
        log.trace(Constants.LOG_CALLED);

        return processSingleResult("SELECT * from period WHERE id=?", String.valueOf(id));
    }

    @Override
    public Optional<Period> getByName(String uniqueName) {
        log.trace(Constants.LOG_CALLED);

        return processSingleResult("SELECT * from period WHERE name=?", uniqueName);
    }

    @Override
    public List<Period> getPeriods() {
        log.trace(Constants.LOG_CALLED);

        List<Map<Object, Object>> results = executeSqlAndReturnList("SELECT * from period");

        List<Period> list = new ArrayList<>(results.size());
        for (Map<Object, Object> row : results) {
            Optional<Period> object = Optional.ofNullable(this.factory.buildPeriodObject(row));
            if (object.isPresent()) {
                list.add(object.get());
            }
        }

        return list;
    }

    @Override
    public int createPeriod(String uniqueName) {
        log.trace(Constants.LOG_CALLED);

        List<Map<Object, Object>> results = executeSqlAndReturnList(
            "INSERT INTO period(name) VALUES(?)", uniqueName);

        return getRowId(results);
    }

    @Override
    public boolean updatePeriod(String uniqueName, String newUniqueName) {
        log.trace(Constants.LOG_CALLED);

        List<Map<Object, Object>> results = executeSqlAndReturnList(
            "UPDATE period SET name=? WHERE name=?", newUniqueName, uniqueName);

        boolean retval = false;
        if (getRowId(results) > 0) {
            retval = true;
        }
        return retval;
    }

    @Override
    public boolean deletePeriod(int id) {
        log.trace(Constants.LOG_CALLED);

        List<Map<Object, Object>> results = executeSqlAndReturnList(
            "DELETE FROM period WHERE id=?", String.valueOf(id));

        boolean retval = false;
        if (getRowId(results) > 0) {
            retval = true;
        }
        return retval;
    }

    private Optional<Period> processSingleResult(String sql, String... args) {
        log.trace(Constants.LOG_CALLED);

        List<Map<Object, Object>> results = executeSqlAndReturnList(sql, args);
        if (results.isEmpty()) {
            return Optional.ofNullable(null);
        }

        return Optional.ofNullable(this.factory.buildPeriodObject(results.get(0)));
    }
}

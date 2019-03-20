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
import org.veary.pvs.dao.DayBookDataAccessObject;
import org.veary.pvs.model.DayBook;
import org.veary.pvs.model.ModelFactory;
import org.veary.pvs.sqlite.ConnectionManager;

/**
 * Package scoped, concrete implementation of the {@code DayBookDataAccessObject} for SQLite.
 *
 * @author Marc L. Veary
 * @since 1.0
 */
@Singleton
final class DayBookDataAccessObjectImpl extends AbstractDataAccessObject
implements DayBookDataAccessObject {

    private static final Logger log = LogManager.getLogger(DayBookDataAccessObjectImpl.class);
    private final ModelFactory factory;

    @Inject
    public DayBookDataAccessObjectImpl(ConnectionManager manager, ModelFactory factory) {
        super(manager);
        this.factory = factory;
    }

    @Override
    public Optional<DayBook> getById(int id) {
        log.trace(Constants.LOG_CALLED);
        return processSingleResult("SELECT * from daybook WHERE id=?", String.valueOf(id));
    }

    @Override
    public Optional<DayBook> getByName(String uniqueName) {
        log.trace(Constants.LOG_CALLED);
        return processSingleResult("SELECT * from daybook WHERE name=?", uniqueName);
    }

    @Override
    public List<DayBook> getDayBooks() {
        log.trace(Constants.LOG_CALLED);

        List<Map<Object, Object>> results = executeSqlAndReturnList("SELECT * from daybook");

        List<DayBook> list = new ArrayList<>(results.size());
        for (Map<Object, Object> row : results) {
            Optional<DayBook> object = Optional.ofNullable(this.factory.buildDayBookObject(row));
            if (object.isPresent()) {
                list.add(object.get());
            }
        }

        return list;
    }

    @Override
    public int createDayBook(String uniqueName, int periodId) {
        log.trace(Constants.LOG_CALLED);

        List<Map<Object, Object>> results = executeSqlAndReturnList(
            "INSERT INTO daybook(name,period_id) VALUES(?,?)", uniqueName,
            String.valueOf(periodId));

        return getRowId(results);
    }

    @Override
    public boolean updateDayBook(String uniqueName, String newUniqueName) {
        log.trace(Constants.LOG_CALLED);

        List<Map<Object, Object>> results = executeSqlAndReturnList(
            "UPDATE daybook SET name=? WHERE name=?", newUniqueName, uniqueName);

        boolean retval = false;
        if (getRowId(results) > 0) {
            retval = true;
        }
        return retval;
    }

    @Override
    public boolean deleteDayBook(int id) {
        log.trace(Constants.LOG_CALLED);

        List<Map<Object, Object>> results = executeSqlAndReturnList(
            "DELETE FROM daybook WHERE id=?", String.valueOf(id));

        boolean retval = false;
        if (getRowId(results) > 0) {
            retval = true;
        }
        return retval;
    }

    private Optional<DayBook> processSingleResult(String sql, String... args) {
        log.trace(Constants.LOG_CALLED);

        List<Map<Object, Object>> results = executeSqlAndReturnList(sql, args);

        return Optional.ofNullable(this.factory.buildDayBookObject(results.get(0)));
    }
}

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

import org.veary.pvs.dao.AccountDataAccessObject;
import org.veary.pvs.dao.DayBookDataAccessObject;
import org.veary.pvs.dao.PeriodDataAccessObject;
import org.veary.pvs.dao.SystemDataAccessObject;
import org.veary.pvs.sqlite.jdbc.GuiceSqliteJdbcTestModule;

public class GuiceSqliteDaoTestModule extends GuiceSqliteJdbcTestModule {

    @Override
    protected void configure() {
        super.configure();

        // DAO bindings
        bind(AccountDataAccessObject.class).to(AccountDataAccessObjectImpl.class);
        bind(PeriodDataAccessObject.class).to(PeriodDataAccessObjectImpl.class);
        bind(DayBookDataAccessObject.class).to(DayBookDataAccessObjectImpl.class);
        bind(SystemDataAccessObject.class).to(SystemDataAccessObjectImpl.class);
    }
}

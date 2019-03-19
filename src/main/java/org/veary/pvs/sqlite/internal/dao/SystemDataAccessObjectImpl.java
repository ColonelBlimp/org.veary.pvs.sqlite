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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.veary.pvs.core.Constants;
import org.veary.pvs.core.Money;
import org.veary.pvs.dao.SystemDataAccessObject;
import org.veary.pvs.exceptions.DataAccessException;
import org.veary.pvs.model.Account;
import org.veary.pvs.model.Transaction;
import org.veary.pvs.sqlite.ConnectionManager;

final class SystemDataAccessObjectImpl extends AbstractDataAccessObject
implements SystemDataAccessObject {

    private final Logger log = LogManager.getLogger(SystemDataAccessObjectImpl.class);

    @Inject
    protected SystemDataAccessObjectImpl(ConnectionManager manager) {
        super(manager);
    }

    @Override
    public boolean postTransaction(ZonedDateTime timestamp, String narrative, Money amount,
        Account fromAccount, Account toAccount, String reference, int daybookId) {
        log.trace(Constants.LOG_CALLED);
        boolean retval = false;

        int resultCount = 0;
        int journalId = 0;
        try (Connection conn = startTransaction()) {
            journalId = createJournalEntry(conn, timestamp, narrative, reference, daybookId);
            if (journalId == 0) {
                rollbackTransaction(conn);
                throw new DataAccessException("Invalid journal id returned: " + journalId);
            }
            resultCount++;
            if (createLedgerEntry(conn, journalId, fromAccount.getId(), amount.negate()) > 0) {
                resultCount++;
            }
            if (createLedgerEntry(conn, journalId, toAccount.getId(), amount) > 0) {
                resultCount++;
            }
            retval = endTransaction(conn, resultCount);
        } catch (SQLException e) { throw new DataAccessException(e); }

        log.debug("Journal Id: {}", journalId);

        return retval;
    }

    @Override
    public List<Transaction> getTransactions() {
        log.trace(Constants.LOG_CALLED);

        List<Transaction> list = new ArrayList<>();

        return list;
    }

    private int createLedgerEntry(Connection conn, int journalId, int accountId, Money amount) {
        log.trace(Constants.LOG_CALLED);
        int id = 0;
        try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO ledger(journal_id,account_id,amount) VALUES(?,?,?)")) {
            stmt.setObject(1, Integer.valueOf(journalId));
            stmt.setObject(2, Integer.valueOf(accountId));
            stmt.setObject(3, amount.getAmount());
            stmt.executeUpdate();
            try (ResultSet rset = stmt.getGeneratedKeys()) {
                id = getRowId(resultSetToList(rset));
            }
        } catch (SQLException e) {
            rollbackTransaction(conn);
            throw new DataAccessException(e);
        }
        return id;
    }

    private int createJournalEntry(Connection conn, ZonedDateTime timestamp, String narrative, String reference, int daybookId) {
        log.trace(Constants.LOG_CALLED);
        int journalId = 0;

        try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO journal(date,ref,narrative,daybook_id) VALUES(?,?,?,?)")) {
            stmt.setObject(1, reference);
            stmt.setObject(2, timestamp.toString());
            stmt.setObject(3, narrative);
            stmt.setObject(4, daybookId);
            stmt.executeUpdate();
            try (ResultSet rset = stmt.getGeneratedKeys()) {
                journalId = getRowId(resultSetToList(rset));
            }
        } catch (SQLException e) {
            rollbackTransaction(conn);
            throw new DataAccessException(e);
        }

        return journalId;
    }
}

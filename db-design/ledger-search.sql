SELECT journal.id, journal.date, journal.narrative, ledger.amount, account.name, journal.ref
FROM ledger
INNER JOIN journal ON journal.id = ledger.journal_id
INNER JOIN account ON account.id = ledger.account_id
INNER JOIN daybook ON daybook.id = journal.daybook_id

package goorm.tricount.repository;

import goorm.tricount.dto.ExpenseResult;
import goorm.tricount.model.Expense;

import java.util.List;

public interface ExpenseRepository {
    Expense save(Expense expense);

    List<ExpenseResult> findExpensesWithMemberBySettlementId(Long settlementId);
}

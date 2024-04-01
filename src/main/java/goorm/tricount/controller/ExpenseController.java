package goorm.tricount.controller;

import goorm.tricount.dto.ExpenseRequest;
import goorm.tricount.dto.ExpenseResult;
import goorm.tricount.model.ApiResponse;
import goorm.tricount.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/expenses")
public class ExpenseController {
    private final ExpenseService expenseService;

    @PostMapping("/add")
    public ApiResponse<ExpenseResult> addExpenseToSettlement(
            @Valid @RequestBody ExpenseRequest expenseRequest
    ) {
        return new ApiResponse<ExpenseResult>().ok(expenseService.addExpense(expenseRequest));
    }
}

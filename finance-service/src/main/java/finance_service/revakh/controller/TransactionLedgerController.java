package finance_service.revakh.controller;

import finance_service.revakh.DTO.DailyTransactionsRequestDTO;
import finance_service.revakh.DTO.TransactionLedgerRequestDTO;
import finance_service.revakh.DTO.TransactionLedgerResultDTO;
import finance_service.revakh.exceptions.TransactionExceptions.InsufficientBalanceException;
import finance_service.revakh.exceptions.TransactionExceptions.TransactionNotFound;
import finance_service.revakh.exceptions.FinanceUserExceptions.UserNotFoundException;
import finance_service.revakh.models.TransactionLedger;
import finance_service.revakh.service.TransactionLedgerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/finance/users")
@RequiredArgsConstructor
public class TransactionLedgerController{
    private final TransactionLedgerService transactionLedgerService;
    // create transaction
    @Operation(summary = "Create Transaction", description = "Records a new Credit or Debit. Updates wallet balance atomically. Retries on concurrency conflicts. We have to input, the amount, the type either CREDIT or DEBIT, and then also give the category, for source we have MANUAL, UPI, NET-BANKING")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transaction created"),
            @ApiResponse(responseCode = "400", description = "Insufficient Balance / Invalid Category or Type"),
            @ApiResponse(responseCode = "404", description = "User or Category not found")
    })
    @PostMapping("/transaction-ledger")
    public ResponseEntity<?> createTransaction(@Valid @RequestBody TransactionLedgerRequestDTO dto, @RequestHeader("userId") Long userId){ //DONT TRUST USER ID FROM REQUESTBODY
            dto.setUserId(userId);
            TransactionLedgerResultDTO transactionLedgerResultDTO = transactionLedgerService.createTransaction(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(transactionLedgerResultDTO);
    }

    // list all the user transactions
    @Operation(summary = "List Transactions", description = "Get all active transactions for a user.")
    @GetMapping("/transaction-ledger/history")
    public ResponseEntity<?> getAllTransactions(@RequestHeader("userId") Long userId){
            List<TransactionLedgerResultDTO> ledgerResultDTOS = new ArrayList<>();
            List<TransactionLedger> transactionLedgers   = transactionLedgerService.getAllTransactions(userId);
            for(TransactionLedger transactionLedger : transactionLedgers){
                TransactionLedgerResultDTO transactionLedgerResultDTO = TransactionLedgerResultDTO.builder()
                        .transactionId(transactionLedger.getTransactionId())
                        .userId(transactionLedger.getFinanceUser().getUserId())
                        .userName(transactionLedger.getFinanceUser().getUserName())
                        .amount(transactionLedger.getAmount())
                        .categoryName(transactionLedger.getCategory().getName())
                        .type(transactionLedger.getTransactionType())
                        .source(transactionLedger.getSource())
                        .balance(transactionLedger.getBalanceAfterTransaction())
                        .description(transactionLedger.getDescription())
                        .date(transactionLedger.getCreatedAt())
                        .build();
                ledgerResultDTOS.add(transactionLedgerResultDTO);
            }
            return ResponseEntity.status(HttpStatus.OK).body(ledgerResultDTOS);
    }

    @Operation(summary = "List Transactions Daily Transactions", description = "Get all daily transactions for a user under each category.")
    @PostMapping("/transaction-ledger/daily")
    public ResponseEntity<?> getDailyTransactions(
            @RequestHeader("userId") Long userId,
            @Parameter(description = "Date for the report (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate){
        DailyTransactionsRequestDTO dailyTransactionsRequestDTO = DailyTransactionsRequestDTO.builder()
                .userId(userId)
                .targetDate(targetDate)
                .build();

        return transactionLedgerService.getDailyTransactions(dailyTransactionsRequestDTO);
    }
    // fetch a single transaction
    //this is just for our testing purposes and future usecases, we wont be using this on the front end
    @Operation(summary = "Get Single Transaction", description = "Fetch details of a specific transaction ID.")
    @GetMapping("/transaction-ledger/{transactionId}")
    public ResponseEntity<?> getTransaction(@RequestHeader("userId") Long userId, @PathVariable Long transactionId){
            TransactionLedger transactionLedger = transactionLedgerService.getTransaction(userId,transactionId);
            TransactionLedgerResultDTO resultDTO = TransactionLedgerResultDTO.builder()
                    .transactionId(transactionLedger.getTransactionId())
                    .userId(transactionLedger.getFinanceUser().getUserId())
                    .userName(transactionLedger.getFinanceUser().getUserName())
                    .amount(transactionLedger.getAmount())
                    .categoryName(transactionLedger.getCategory().getName())
                    .type(transactionLedger.getTransactionType())
                    .source(transactionLedger.getSource())
                    .balance(transactionLedger.getBalanceAfterTransaction())
                    .description(transactionLedger.getDescription())
                    .build();
            return ResponseEntity.status(HttpStatus.OK).body(resultDTO);
    }

    // delete a transaction
    //this too is for crud only , its not really necessary to show delete option for transactions as its really important to keep logs
    //but if required we can show delete option
    @Operation(summary = "Delete Transaction", description = "Soft deletes a transaction. Note: Does NOT revert the wallet balance (by design).")
    @DeleteMapping("/transaction-ledger/{transactionId}")
    public ResponseEntity<?> deleteTransaction(@RequestHeader("userId") Long userId, @PathVariable Long transactionId){
            transactionLedgerService.deleteOneTransaction(userId,transactionId);
            return ResponseEntity.status(HttpStatus.OK).body("Transaction Deleted");
    }
}

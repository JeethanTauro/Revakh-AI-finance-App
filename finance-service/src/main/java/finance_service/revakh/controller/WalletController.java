package finance_service.revakh.controller;


import finance_service.revakh.DTO.TransactionLedgerRequestDTO;
import finance_service.revakh.DTO.TransactionLedgerResultDTO;
import finance_service.revakh.DTO.WalletDTO;
import finance_service.revakh.exceptions.FinanceUserExceptions.UserNotFoundException;
import finance_service.revakh.models.*;
import finance_service.revakh.service.FinanceUserService;
import finance_service.revakh.service.TransactionLedgerService;
import finance_service.revakh.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/finance/users")
public class WalletController {
    //the userId is sent by the authResponseDTO when the user first registers and is stored on the frontend for further rquests
    //now we can have an issue, anyone can change the userId and then request for someone else's wallet
    //thats where authorisation comes, in, we can check if the logged in userId and the requested userId is the same

    private final WalletService walletService;
    private final TransactionLedgerService transactionLedgerService;
    private final FinanceUserService financeUserService;

    //get wallet information of a user
    @Operation(summary = "Get Wallet Balance", description = "Retrieves the current balance and currency for the user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wallet retrieved"),
            @ApiResponse(responseCode = "404", description = "User or Wallet not found")
    })
    @GetMapping("/{userId}/wallet")
    ResponseEntity<?> getWallet(@PathVariable Long userId){
            FinanceUser user = financeUserService.getUser(userId);
            Wallet wallet = walletService.getWallet(user);
            WalletDTO walletDTO = WalletDTO.builder()
                    .balance(wallet.getBalance())
                    .currency(wallet.getCurrency())
                    .userId(userId)
                    .build();
            return ResponseEntity.status(HttpStatus.OK).body(walletDTO);
    }

    //add money into wallet
    //when we add money the only thing we do is we add the amount and then add the description
    //the other fields are set internally
    @Operation(summary = "Top-Up Wallet", description = "Adds funds to the wallet. Internally creates a 'CREDIT' transaction with 'TOP_UP' category. Only have to input the amount and the description")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Money credited successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid amount or transaction error"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/{userId}/wallet/top-up")
    ResponseEntity<?> creditToWallet(@PathVariable Long userId, @Valid @RequestBody TransactionLedgerRequestDTO dto) {

            dto.setUserId(userId);
            dto.setCategoryName(SystemCategories.TOP_UP.name());
            dto.setTransactionType(TransactionType.CREDIT);
            dto.setSource(Source.MANUAL); // Enforced default source
            dto.setDescription("Topping Up the wallet");

            TransactionLedgerResultDTO result =
                    transactionLedgerService.createTransaction(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto.getAmount()+"Credited to wallet");

    }


}

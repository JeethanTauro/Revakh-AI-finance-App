package finance_service.revakh.controller;

import finance_service.revakh.DTO.FinanceUserDTO;
import finance_service.revakh.DTO.FinanceUserUpdateDTO;
import finance_service.revakh.exceptions.UserNotFoundException;
import finance_service.revakh.models.FinanceUser;
import finance_service.revakh.service.FinanceUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor

public class FinanceUserController {

    private final FinanceUserService financeUserService;
    //user create (handled by message queues)
    //user delete (handled by message queues)
    //get user

    @Operation(summary = "Get Finance Profile", description = "Retrieves profile details (Phone, Name, etc.) specific to the Finance module.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FinanceUserDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found (Sync issue with Auth Service)")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUser(@PathVariable Long userId){
        try{
            FinanceUser financeUser = financeUserService.getUser(userId);
            FinanceUserDTO financeUserDTO = FinanceUserDTO.builder()
                    .userName(financeUser.getUserName())
                    .userEmail(financeUser.getUserEmail())
                    .userInternationalCode(financeUser.getUserInternationalCode())
                    .userPhoneNumber(financeUser.getUserPhone())
                    .build();
            return ResponseEntity.status(HttpStatus.OK).body(financeUserDTO);
        }
        catch (UserNotFoundException u){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No user found");
        }

    }
}

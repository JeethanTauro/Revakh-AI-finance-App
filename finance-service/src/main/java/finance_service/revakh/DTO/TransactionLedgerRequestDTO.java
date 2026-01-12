package finance_service.revakh.DTO;

import finance_service.revakh.models.Source;
import finance_service.revakh.models.TransactionType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionLedgerRequestDTO {
    @NotNull
    private Long userId;


    @NotNull
    @Positive
    private BigDecimal amount;

    @NotBlank
    private String categoryName;


    @Enumerated(EnumType.STRING)
    @NotNull
    private Source source;


    @Size(max = 255)
    private String description;


    @Enumerated(EnumType.STRING)
    @NotNull
    private TransactionType transactionType;
}

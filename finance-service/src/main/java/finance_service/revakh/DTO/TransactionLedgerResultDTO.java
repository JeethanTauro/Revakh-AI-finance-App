package finance_service.revakh.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import finance_service.revakh.models.Source;
import finance_service.revakh.models.TransactionLedger;
import finance_service.revakh.models.TransactionType;
import finance_service.revakh.models.Wallet;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionLedgerResultDTO {
    @NotNull
    private Long transactionId;

    @NotNull
    private BigDecimal amount;

    @NotNull
    private String userName;

    @NotNull
    private Long userId;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private TransactionType type;

    @NotNull
    private String categoryName;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Source source;

    private String description;

    @NotNull
    private BigDecimal balance;

}

package finance_service.revakh.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import finance_service.revakh.models.Period;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BudgetRequestDTO {
    @NotNull
    private Long financeUserId;


    @NotNull(message = "categoryId is required")
    private Long categoryId;


    @NotNull
    @Positive
    @Digits(integer = 12, fraction = 2)
    private BigDecimal limitAmount;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Period period;

    private boolean isActive = true;
}

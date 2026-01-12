package finance_service.revakh.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import finance_service.revakh.models.Period;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetDTO {
    private Long budgetId;
    private Long categoryId;
    private String categoryName;
    private BigDecimal limitAmount;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Period period;

    private LocalDate periodStart;
    private LocalDate periodEnd;
    private boolean isActive;
}

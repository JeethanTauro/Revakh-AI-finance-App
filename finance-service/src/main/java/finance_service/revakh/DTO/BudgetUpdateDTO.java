package finance_service.revakh.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import finance_service.revakh.models.Period;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BudgetUpdateDTO {

    private Long userId;

    @Positive
    @Digits(integer = 12, fraction = 2)
    private BigDecimal limitAmount;


    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Period period; // DAILY/WEEKLY/MONTHLY/YEARLY

    private boolean isActive;
}


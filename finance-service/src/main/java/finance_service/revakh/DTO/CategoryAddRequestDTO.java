package finance_service.revakh.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import finance_service.revakh.models.CategoryType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryAddRequestDTO {
    @NotNull
    String categoryName;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    CategoryType type;
}

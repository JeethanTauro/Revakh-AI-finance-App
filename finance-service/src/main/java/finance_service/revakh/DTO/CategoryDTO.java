package finance_service.revakh.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import finance_service.revakh.models.CategoryType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryDTO {
    @NotNull(message = "categoryId is required")
    private Long categoryId;

    @NotBlank
    @Size(max = 50)
    private String name;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private CategoryType categoryType;

    private boolean isSystem;
    private boolean isActive;
}

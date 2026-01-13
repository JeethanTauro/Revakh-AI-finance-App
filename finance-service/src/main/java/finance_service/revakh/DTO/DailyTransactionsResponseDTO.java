package finance_service.revakh.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DailyTransactionsResponseDTO {

    private LocalDate date;
    private Double totalExpense;
    private Double totalIncome;
    private Double netBalance; // The "Total" shown in your image ($3,846.28)

    // This Map groups transactions by their Category name
    private Map<String, List<TransactionDetailDTO>> categories;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class TransactionDetailDTO {
        private Long transactionId;
        private String description;
        private Double amount;
        private String type; // INCOME or EXPENSE
    }
}

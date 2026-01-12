package finance_service.revakh.models;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "transaction_ledger")
@Builder
public class TransactionLedger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @ManyToOne
    @JoinColumn(name = "user_id",nullable = false)
    private FinanceUser financeUser;

    @Column(nullable = false,precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Source source;

    @NotNull
    private BigDecimal balanceAfterTransaction;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @CreatedDate
    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private boolean isDeleted = false;

}

//Because the ledger is the immutable source of truth and wallet is only a derived summary. Writing the ledger first guarantees that every balance update always has a permanent record behind it, prevents data corruption, and allows the wallet to be rebuilt if anything fails or conflicts.

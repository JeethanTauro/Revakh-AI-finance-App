package finance_service.revakh.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity

@Table(
        name = "category",
        //caegory is unique per user, so user A creates food and user B can also create food
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "name"}) //(user_id,name) is unique
        }
)
@EntityListeners(AuditingEntityListener.class)
@Builder
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private FinanceUser financeUser;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryType type;

    @Column(nullable = false)
    private boolean isActive = true;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean isSystem = true;


}

package com.share.share.trade;

import com.share.share.user.User;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "trades", indexes = {
        @Index(name = "idx_trades_user_date", columnList = "user_id,trade_date")
})
@Getter
@Setter
@NoArgsConstructor
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "trade_date", nullable = false)
    private LocalDate date;

    @Column(name = "stock", nullable = false, length = 100)
    private String stock;

    @Column(name = "position", length = 10)
    private String position; // long | short

    @Column(name = "result", length = 10)
    private String result; // win | draw | lose

    @Column(name = "profit")
    private Long profit; // KRW amount

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "chart_image", columnDefinition = "LONGTEXT")
    private String chartImage; // Data URL (optional)

    @Column(name = "profit_reason", length = 2000)
    private String profitReason;

    @Column(name = "loss_reason", length = 2000)
    private String lossReason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}



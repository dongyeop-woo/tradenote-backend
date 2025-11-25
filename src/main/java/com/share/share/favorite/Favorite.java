package com.share.share.favorite;

import com.share.share.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "favorites",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_favorites_user_symbol", columnNames = {"user_id", "symbol"})
        },
        indexes = {
            @Index(name = "idx_favorites_user_id", columnList = "user_id"),
            @Index(name = "idx_favorites_symbol", columnList = "symbol")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Column(length = 100)
    private String name;

    @CreationTimestamp
    private LocalDateTime createdAt;
}


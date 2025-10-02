package imbuy.backend.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bids")
@Getter
@Setter
@NoArgsConstructor
public class Bid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", nullable = false)
    private Lot lot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bidder_id", nullable = false)
    private User bidder;

    @NotNull(message = "Bid amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Bid amount must be greater than 0")
    @Column(precision = 19, scale = 2)
    private BigDecimal amount;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Bid(Lot lot, User bidder, BigDecimal amount) {
        this.lot = lot;
        this.bidder = bidder;
        this.amount = amount;
    }
}
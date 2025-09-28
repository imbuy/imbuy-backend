package imbuy.backend.domain;

import imbuy.backend.enums.LotStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "lots")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Lot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Start price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Start price must be greater than 0")
    @Column(name = "start_price", precision = 19, scale = 2)
    private BigDecimal startPrice;

    @NotNull
    @Column(name = "current_price", precision = 19, scale = 2)
    private BigDecimal currentPrice;

    @NotNull(message = "Bid step is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Bid step must be greater than 0")
    @Column(name = "bid_step", precision = 19, scale = 2)
    private BigDecimal bidStep;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Enumerated(EnumType.STRING)
    @NotNull
    private LotStatus status;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "lot", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Bid> bids = new HashSet<>();

    @OneToMany(mappedBy = "lot", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Favorite> favorites = new HashSet<>();
}
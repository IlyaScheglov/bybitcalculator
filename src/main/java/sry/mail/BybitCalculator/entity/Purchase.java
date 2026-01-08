package sry.mail.BybitCalculator.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Accessors(chain = true)
@Table(name = "purchases")
public class Purchase {

    @Id
    @GeneratedValue
    UUID id;

    @Column(nullable = false)
    String symbol;

    @Column(nullable = false)
    OffsetDateTime createTimestamp;

    @Column(nullable = false)
    OffsetDateTime updateTimestamp;

    @Column(nullable = false)
    BigDecimal maxPrice;

    @Column(nullable = false)
    BigDecimal atrAmount;

    @Column(nullable = false)
    Integer atrCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    User user;
}

package sry.mail.BybitCalculator.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
@Table(name = "charts")
public class Chart {

    @Id
    @GeneratedValue
    UUID id;

    @Column(nullable = false)
    String symbol;

    @Column(nullable = false)
    BigDecimal price;

    @Column(nullable = false)
    OffsetDateTime timestamp;
}

package sry.mail.BybitCalculator.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Accessors(chain = true)
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue
    UUID id;

    @Column(nullable = false)
    String tgId;

    @Column(nullable = false)
    BigDecimal minPercentOfDump;

    @Column(nullable = false)
    BigDecimal minPercentOfIncome;

    @Column(nullable = false)
    Boolean active;

    @OneToMany(mappedBy = "user")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    List<Purchase> purchases;
}

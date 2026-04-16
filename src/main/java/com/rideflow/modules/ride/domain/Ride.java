package com.rideflow.modules.ride.domain;

import com.rideflow.shared.exception.RideAlreadyAcceptedException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "rides")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"origin", "destination"})
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "driver_id")
    private UUID driverId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", columnDefinition = "ride_status", nullable = false)
    @Builder.Default
    private RideStatus status = RideStatus.PENDING;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "cep", column = @Column(name = "origin_cep")),
        @AttributeOverride(name = "logradouro", column = @Column(name = "origin_logradouro")),
        @AttributeOverride(name = "numero", column = @Column(name = "origin_numero")),
        @AttributeOverride(name = "complemento", column = @Column(name = "origin_complemento")),
        @AttributeOverride(name = "bairro", column = @Column(name = "origin_bairro")),
        @AttributeOverride(name = "cidade", column = @Column(name = "origin_cidade")),
        @AttributeOverride(name = "estado", column = @Column(name = "origin_estado"))
    })
    private Address origin;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "cep", column = @Column(name = "dest_cep")),
        @AttributeOverride(name = "logradouro", column = @Column(name = "dest_logradouro")),
        @AttributeOverride(name = "numero", column = @Column(name = "dest_numero")),
        @AttributeOverride(name = "complemento", column = @Column(name = "dest_complemento")),
        @AttributeOverride(name = "bairro", column = @Column(name = "dest_bairro")),
        @AttributeOverride(name = "cidade", column = @Column(name = "dest_cidade")),
        @AttributeOverride(name = "estado", column = @Column(name = "dest_estado"))
    })
    private Address destination;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    // ── Domain Factory ─────────────────────────────────────
    public static Ride create(UUID userId, Address origin, Address destination) {
        return Ride.builder()
                .userId(userId)
                .origin(origin)
                .destination(destination)
                .build();
    }

    // ── Domain Behaviors ───────────────────────────────────
    public void accept(UUID driverId) {
        if (this.status != RideStatus.PENDING) {
            throw new RideAlreadyAcceptedException(this.id.toString());
        }
        this.status = RideStatus.ACCEPTED;
        this.driverId = driverId;
        this.acceptedAt = Instant.now();
    }
}

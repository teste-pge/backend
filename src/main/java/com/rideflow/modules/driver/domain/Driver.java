package com.rideflow.modules.driver.domain;

import com.rideflow.shared.exception.DriverNotAvailableException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "drivers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = "id")
@ToString
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "vehicle_plate", nullable = false, length = 10)
    private String vehiclePlate;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", columnDefinition = "driver_status", nullable = false)
    @Builder.Default
    private DriverStatus status = DriverStatus.AVAILABLE;

    public void markBusy() {
        if (this.status == DriverStatus.BUSY) {
            throw new DriverNotAvailableException(this.id.toString());
        }
        this.status = DriverStatus.BUSY;
    }

    public void markAvailable() {
        this.status = DriverStatus.AVAILABLE;
    }

    public boolean isAvailable() {
        return this.status == DriverStatus.AVAILABLE;
    }
}

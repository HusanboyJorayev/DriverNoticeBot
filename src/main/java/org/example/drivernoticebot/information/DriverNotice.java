package org.example.drivernoticebot.information;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "driver_notice")
public class DriverNotice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String driverName;
    private String truckUnitNumber;
    private String driverType;
    private String lastWorkingDate;
    private String reasonForLeaving;
    private String returningDateIfAvailable;
    @Column(unique = true)
    private Long chatId;

    @Override
    public String toString() {
        return
                "driverName: " + driverName + "\n" +
                "truckUnitNumber: " + truckUnitNumber +"\n" +
                "driverType: " + driverType + "\n" +
                "lastWorkingDate: " + lastWorkingDate + "\n" +
                "reasonForLeaving: " + reasonForLeaving + "\n" +
                "returningDateIfAvailable: " + returningDateIfAvailable;
    }
}

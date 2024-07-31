package org.example.drivernoticebot.information;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "drivers")
public class Drivers {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String driverName;
    private String office;
    private String dsp;
    private String status;
    private String fromDate;
    private String toDate;
    private String address;
    @Column(columnDefinition = "TEXT")
    private String note;
    //@Column(unique = true)
    private Long chatId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    @Override
    public String toString() {
        return
                "driverName : " + driverName + "\n"+
                "office : " + office + "\n"+
                "dsp : " + dsp  + "\n"+
                "status : " + status  + "\n"+
                "fromDate : "  + fromDate+ "\n"+
                "toDate : " + toDate + "\n"+
                "address : " + address  + "\n"+
                "note : " + note;
    }
}

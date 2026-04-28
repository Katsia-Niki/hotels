package by.nikifarava.hotel.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "arrival_times")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArrivalTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false, unique = true)
    @ToString.Exclude
    private Hotel hotel;

    @Column(name = "check_in", length = 5)
    private String checkIn;

    @Column(name = "check_out", length = 5)
    private String checkOut;
}

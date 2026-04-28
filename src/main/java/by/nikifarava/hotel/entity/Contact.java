package by.nikifarava.hotel.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "contacts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false, unique = true)
    @ToString.Exclude
    private Hotel hotel;

    @Column(nullable = false, length = 40)
    private String phone;

    @Column(nullable = false, length = 255, unique = true)
    private String email;
}

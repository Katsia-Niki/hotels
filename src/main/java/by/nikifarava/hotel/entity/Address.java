package by.nikifarava.hotel.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false, unique = true)
    @ToString.Exclude
    private Hotel hotel;

    @Column(name = "house_number", nullable = false, length = 20)
    private String houseNumber;

    @Column(nullable = false, length = 255)
    private String street;

    @Column(nullable = false, length = 120)
    private String city;

    @Column(nullable = false, length = 120)
    private String country;

    @Column(name = "post_code", nullable = false, length = 20)
    private String postCode;
}

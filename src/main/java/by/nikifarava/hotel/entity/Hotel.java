package by.nikifarava.hotel.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "hotels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "CLOB")
    private String description;

    @Column(nullable = false, length = 120)
    private String brand;

    @ToString.Exclude
    @OneToOne(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private Address address;

    @ToString.Exclude
    @OneToOne(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private Contact contact;

    @ToString.Exclude
    @OneToOne(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private ArrivalTime arrivalTime;

    @Builder.Default
    @ToString.Exclude
    @ManyToMany(mappedBy = "hotels")
    private Set<Amenity> amenities = new HashSet<>();
}

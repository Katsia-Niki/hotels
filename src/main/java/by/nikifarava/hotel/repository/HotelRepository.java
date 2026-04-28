package by.nikifarava.hotel.repository;

import by.nikifarava.hotel.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

    @Query("select h from Hotel h left join fetch h.address left join fetch h.contact")
    List<Hotel> findAllWithAddressAndContact();

    @Query("""
            select h from Hotel h
            left join fetch h.address
            left join fetch h.contact
            left join fetch h.arrivalTime
            left join fetch h.amenities
            where h.id = :id
            """)
    Optional<Hotel> findDetailsById(Long id);

    @Query("select h.brand, count(h) from Hotel h group by h.brand")
    List<Object[]> countByBrand();

    List<Hotel> findByNameContainingIgnoreCase(String name);

    List<Hotel> findByBrandContainingIgnoreCase(String brand);

    List<Hotel> findByAddress_CityContainingIgnoreCase(String city);

    List<Hotel> findByAddress_CountryContainingIgnoreCase(String country);

    List<Hotel> findByNameContainingIgnoreCaseAndBrandContainingIgnoreCase(String name, String brand);

    List<Hotel> findByAddress_CityContainingIgnoreCaseAndAddress_CountryContainingIgnoreCase(String city, String country);

    @Query("""
            select distinct h
            from Hotel h
            join h.amenities a
            where lower(a.name) in :amenityNames
            """)
    List<Hotel> findDistinctByAmenityNames(Collection<String> amenityNames);
}

package by.nikifarava.hotel.repository;

import by.nikifarava.hotel.entity.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AmenityRepository extends JpaRepository<Amenity, Long> {

    Optional<Amenity> findByNameIgnoreCase(String name);

    @Query("""
            select a.name, count(h)
            from Amenity a
            left join a.hotels h
            group by a.name
            """)
    List<Object[]> countByAmenityName();
}

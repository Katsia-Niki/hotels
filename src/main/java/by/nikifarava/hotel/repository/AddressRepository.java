package by.nikifarava.hotel.repository;

import by.nikifarava.hotel.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {

    @Query("select a.city, count(a) from Address a group by a.city")
    List<Object[]> countByCity();

    @Query("select a.country, count(a) from Address a group by a.country")
    List<Object[]> countByCountry();
}

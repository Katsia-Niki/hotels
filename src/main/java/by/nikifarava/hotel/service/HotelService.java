package by.nikifarava.hotel.service;

import by.nikifarava.hotel.dto.CreateHotelRequestDto;
import by.nikifarava.hotel.dto.HotelDetailsResponseDto;
import by.nikifarava.hotel.dto.HotelShortResponseDto;
import by.nikifarava.hotel.repository.AddressRepository;
import by.nikifarava.hotel.repository.AmenityRepository;
import by.nikifarava.hotel.repository.HotelRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@AllArgsConstructor
public class HotelService {

    private static final Set<String> HISTOGRAM_PARAMS = Set.of("brand", "city", "country", "amenities");
    private static final String DEFAULT_CHECK_IN = "14:00";
    private static final String DEFAULT_CHECK_OUT = "12:00";

    private final HotelRepository hotelRepository;
    private final AddressRepository addressRepository;
    private final AmenityRepository amenityRepository;
    // private final HotelMapper hotelMapper; todo

    public List<HotelShortResponseDto> getAllHotels() {
        //todo
        return null;
    }


    public HotelDetailsResponseDto getHotelById(Long id) {
        //todo
        return null;
    }

    public List<HotelShortResponseDto> search(String name, String brand, String city, String country, List<String> amenities) {
        //todo
        return null;
    }

    public HotelShortResponseDto createHotel(@Valid CreateHotelRequestDto request) {
        //todo
        return null;
    }

    public void addAmenities(Long id, List<String> amenities) {
        //todo
    }

    public Map<String, Long> getHistogram(String param) {
        //todo
        return null;
    }
}

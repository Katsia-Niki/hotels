package by.nikifarava.hotel.service;

import by.nikifarava.hotel.dto.request.CreateHotelRequestDto;
import by.nikifarava.hotel.dto.request.HotelSearchRequestDto;
import by.nikifarava.hotel.dto.response.HotelDetailsResponseDto;
import by.nikifarava.hotel.dto.response.HotelShortResponseDto;
import by.nikifarava.hotel.entity.Address;
import by.nikifarava.hotel.entity.ArrivalTime;
import by.nikifarava.hotel.entity.Contact;
import by.nikifarava.hotel.entity.Hotel;
import by.nikifarava.hotel.mapper.HotelMapper;
import by.nikifarava.hotel.repository.AddressRepository;
import by.nikifarava.hotel.repository.AmenityRepository;
import by.nikifarava.hotel.repository.HotelRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.util.*;

import static java.util.stream.Collectors.toSet;

@Service
@RequiredArgsConstructor
public class HotelService {

    private static final Set<String> HISTOGRAM_PARAMS = Set.of("brand", "city", "country", "amenities");
    private static final LocalTime DEFAULT_CHECK_IN = LocalTime.of(14, 0);
    private static final LocalTime DEFAULT_CHECK_OUT = LocalTime.of(12, 0);

    private final HotelRepository hotelRepository;
    private final AddressRepository addressRepository;
    private final AmenityRepository amenityRepository;
    private final HotelMapper hotelMapper;

   

    public void addAmenities(Long id, List<String> amenities) {
        //todo
    }

    public Map<String, Long> getHistogram(String param) {
        //todo
        return null;
    }

    @Transactional
    public HotelShortResponseDto createHotel(@Valid CreateHotelRequestDto request) {

        Hotel hotel = Hotel.builder()
                .name(request.name())
                .description(request.description())
                .brand(request.brand())
                .build();

        Address address = Address.builder()
                .hotel(hotel)
                .houseNumber(request.address().houseNumber())
                .street(request.address().street())
                .city(request.address().city())
                .country(request.address().country())
                .postCode(request.address().postCode())
                .build();
        hotel.setAddress(address);

        Contact contact = Contact.builder()
                .hotel(hotel)
                .phone(request.contacts().phone())
                .email(request.contacts().email())
                .build();
        hotel.setContact(contact);

        LocalTime checkIn = request.arrivalTime() != null && request.arrivalTime().checkIn() != null
                ? request.arrivalTime().checkIn()
                : DEFAULT_CHECK_IN;
        LocalTime checkOut = request.arrivalTime() != null && request.arrivalTime().checkOut() != null
                ? request.arrivalTime().checkOut()
                : DEFAULT_CHECK_OUT;

        ArrivalTime arrivalTime = ArrivalTime.builder()
                .hotel(hotel)
                .checkIn(checkIn)
                .checkOut(checkOut)
                .build();
        hotel.setArrivalTime(arrivalTime);

        Hotel savedHotel = hotelRepository.save(hotel);
        return hotelMapper.toShortDto(savedHotel);
    }

    @Transactional(readOnly = true)
    public List<HotelShortResponseDto> search(HotelSearchRequestDto hotelDto) {

        String name = hotelDto != null ? hotelDto.getName() : null;
        String brand = hotelDto != null ? hotelDto.getBrand() : null;
        String city = hotelDto != null ? hotelDto.getCity() : null;
        String country = hotelDto != null ? hotelDto.getCountry() : null;
        List<String> preparedAmenities = prepareAmenities(hotelDto != null ? hotelDto.getAmenities() : null);
        List<Hotel> hotels = findHotelsByFirstMatch(name, brand, city, country, preparedAmenities);
        hotels = hotels.stream()
                .filter(hotel -> matchesSearchFilters(hotel, name, brand, city, country, preparedAmenities))
                .toList();
        return hotelMapper.toShortDtoList(hotels);
    }

    @Transactional(readOnly = true)
    public HotelDetailsResponseDto getHotelById(Long id) {
        Hotel hotel = hotelRepository.findDetailsById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found"));

        return hotelMapper.toDetailsDto(hotel);
    }

    @Transactional(readOnly = true)
    public List<HotelShortResponseDto> getAllHotels() {
        return hotelMapper.toShortDtoList(hotelRepository.findAllWithAddressAndContact());
    }

    private List<String> prepareAmenities(List<String> amenities) {
        if (amenities == null || amenities.isEmpty()) {
            return List.of();
        }
        return amenities.stream()
                .flatMap(value -> Arrays.stream(value.split(",")))
                .map(String::trim)
                .filter(this::hasText)
                .map(String::toLowerCase)
                .distinct()
                .sorted()
                .toList();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private List<Hotel> findHotelsByFirstMatch(
            String name,
            String brand,
            String city,
            String country,
            List<String> preparedAmenities
    ) {if (hasText(name) && hasText(brand)) {
        return hotelRepository.findByNameContainingIgnoreCaseAndBrandContainingIgnoreCase(name.trim(), brand.trim());
    }
        if (hasText(city) && hasText(country)) {
            return hotelRepository.findByAddress_CityContainingIgnoreCaseAndAddress_CountryContainingIgnoreCase(
                    city.trim(),
                    country.trim()
            );
        }
        if (hasText(name)) {
            return hotelRepository.findByNameContainingIgnoreCase(name.trim());
        }
        if (hasText(brand)) {
            return hotelRepository.findByBrandContainingIgnoreCase(brand.trim());
        }
        if (hasText(city)) {
            return hotelRepository.findByAddress_CityContainingIgnoreCase(city.trim());
        }
        if (hasText(country)) {
            return hotelRepository.findByAddress_CountryContainingIgnoreCase(country.trim());
        }
        if (!preparedAmenities.isEmpty()) {
            return hotelRepository.findDistinctByAmenityNames(preparedAmenities);
        }
        return hotelRepository.findAll();
    }

    private boolean matchesSearchFilters(
            Hotel hotel,
            String name,
            String brand,
            String city,
            String country,
            List<String> preparedAmenities
    ) {if (hasText(name) && doesNotContainIgnoreCase(hotel.getName(), name)) {
        return false;
    }
        if (hasText(brand) && doesNotContainIgnoreCase(hotel.getBrand(), brand)) {
            return false;
        }
        if (hasText(city) && (hotel.getAddress() == null || doesNotContainIgnoreCase(hotel.getAddress().getCity(), city))) {
            return false;
        }
        if (hasText(country)
                && (hotel.getAddress() == null || doesNotContainIgnoreCase(hotel.getAddress().getCountry(), country))) {
            return false;
        }
        if (!preparedAmenities.isEmpty()) {
            Set<String> hotelAmenities = hotel.getAmenities().stream()
                    .map(a -> a.getName().toLowerCase())
                    .collect(toSet());
            return preparedAmenities.stream().anyMatch(hotelAmenities::contains);
        }
        return true;
    }

    private boolean doesNotContainIgnoreCase(String source, String subString) {
        return source == null
                || subString == null
                || !source.toLowerCase().contains(subString.toLowerCase().trim());
    }
}

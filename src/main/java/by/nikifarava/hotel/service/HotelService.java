package by.nikifarava.hotel.service;

import by.nikifarava.hotel.dto.request.CreateHotelRequestDto;
import by.nikifarava.hotel.dto.request.HotelSearchRequestDto;
import by.nikifarava.hotel.dto.response.HotelDetailsResponseDto;
import by.nikifarava.hotel.dto.response.HotelShortResponseDto;
import by.nikifarava.hotel.entity.*;
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
import java.util.stream.Collectors;

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


    @Transactional
    public void addAmenities(Long id, List<String> amenities) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found"));

        if (amenities == null || amenities.isEmpty()) return;

        amenities.stream()
                .flatMap(name -> Arrays.stream(name.split(",")))
                .map(String::trim)
                .filter(name -> !name.isBlank())
                .map(String::toLowerCase)
                .distinct()
                .forEach(name -> {
                    Amenity amenity = amenityRepository.findByNameIgnoreCase(name)
                            .orElseGet(() -> amenityRepository.save(Amenity.builder().name(name).build()));

                    if (!hotel.getAmenities().contains(amenity)) {
                        hotel.getAmenities().add(amenity);
                        amenity.getHotels().add(hotel);
                    }
                });

        hotelRepository.save(hotel);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getHistogram(String param) {
        String preparedParam = (param == null) ? "" : param.toLowerCase();
        if (!HISTOGRAM_PARAMS.contains(preparedParam)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported histogram parameters. Allowed parameters: " + HISTOGRAM_PARAMS
            );
        }

        List<Object[]> rows = switch (preparedParam) {
            case "brand" -> hotelRepository.countByBrand();
            case "city" -> addressRepository.countByCity();
            case "country" -> addressRepository.countByCountry();
            case "amenities" -> amenityRepository.countByAmenityName();
            default -> List.of();
        };

        return rows.stream().collect(Collectors
                .toMap(r -> String.valueOf(r[0]), r -> ((Number) r[1]).longValue()));
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

        if (hotelDto == null) {
            return hotelMapper.toShortDtoList(hotelRepository.findAll());
        }

        String name = hotelDto.getName();
        String brand = hotelDto.getBrand();
        String city = hotelDto.getCity();
        String country = hotelDto.getCountry();

        List<String> amenities = Optional.ofNullable(hotelDto.getAmenities())
                .orElse(List.of())
                .stream()
                .flatMap(s -> Arrays.stream(s.split(",")))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(String::toLowerCase)
                .distinct()
                .toList();

        List<Hotel> hotels = findHotelsByFirstMatch(name, brand, city, country, amenities);

        hotels = hotels.stream()
                .filter(hotel -> matchesSearchFilters(hotel, name, brand, city, country, amenities))
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

    private List<Hotel> findHotelsByFirstMatch(
            String name,
            String brand,
            String city,
            String country,
            List<String> preparedAmenities
    ) {if (name != null && !name.isBlank() && brand != null && !brand.isBlank()) {
        return hotelRepository.findByNameContainingIgnoreCaseAndBrandContainingIgnoreCase(name.trim(), brand.trim());
    }
        if (city != null && !city.isBlank() && country != null && !country.isBlank()) {
            return hotelRepository.findByAddress_CityContainingIgnoreCaseAndAddress_CountryContainingIgnoreCase(
                    city.trim(),
                    country.trim()
            );
        }
        if (name != null && !name.isBlank()) {
            return hotelRepository.findByNameContainingIgnoreCase(name.trim());
        }
        if (brand != null && !brand.isBlank()) {
            return hotelRepository.findByBrandContainingIgnoreCase(brand.trim());
        }
        if (city != null && !city.isBlank()) {
            return hotelRepository.findByAddress_CityContainingIgnoreCase(city.trim());
        }
        if (country != null && !country.isBlank()) {
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
    ) {if (name != null && !name.isBlank() && doesNotContainIgnoreCase(hotel.getName(), name)) {
        return false;
    }
        if (brand != null && !brand.isBlank() && doesNotContainIgnoreCase(hotel.getBrand(), brand)) {
            return false;
        }
        if (city != null && !city.isBlank() && (hotel.getAddress() == null || doesNotContainIgnoreCase(hotel.getAddress().getCity(), city))) {
            return false;
        }
        if (country != null && !country.isBlank()
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

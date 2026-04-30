package by.nikifarava.hotel.service;

import by.nikifarava.hotel.dto.request.CreateHotelRequestDto;
import by.nikifarava.hotel.dto.request.HotelSearchRequestDto;
import by.nikifarava.hotel.dto.response.HotelDetailsResponseDto;
import by.nikifarava.hotel.dto.response.HotelShortResponseDto;
import by.nikifarava.hotel.entity.Amenity;
import by.nikifarava.hotel.entity.Hotel;
import by.nikifarava.hotel.mapper.HotelMapper;
import by.nikifarava.hotel.repository.AddressRepository;
import by.nikifarava.hotel.repository.AmenityRepository;
import by.nikifarava.hotel.repository.HotelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HotelServiceTest {

    @Mock
    private HotelRepository hotelRepository;
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private AmenityRepository amenityRepository;
    @Mock
    private HotelMapper hotelMapper;

    @InjectMocks
    private HotelService hotelService;

    private Hotel baseHotel;
    private HotelShortResponseDto baseShortDto;
    private HotelDetailsResponseDto baseDetailsDto;

    @BeforeEach
    void setUp() {
        baseHotel = Hotel.builder()
                .id(1L)
                .name("Hotel")
                .brand("Brand")
                .amenities(new java.util.HashSet<>())
                .build();
        baseShortDto = new HotelShortResponseDto(1L, "Hotel", "desc", "addr", "phone");
        baseDetailsDto = new HotelDetailsResponseDto(1L, "Hotel", null, null, null, null, null, List.of());
    }

    @Test
    void createHotel_shouldSetDefaultArrivalTime_whenArrivalTimeMissing() {
        CreateHotelRequestDto request = buildCreateRequest(null);

        when(hotelRepository.save(any(Hotel.class))).thenReturn(baseHotel);
        when(hotelMapper.toShortDto(baseHotel)).thenReturn(baseShortDto);

        HotelShortResponseDto actual = hotelService.createHotel(request);

        verify(hotelRepository).save(any(Hotel.class));
        assertEquals(baseShortDto, actual);
    }

    @Test
    void getHistogram_shouldReturnCountsForBrand() {
        when(hotelRepository.countByBrand())
                .thenReturn(List.of(new Object[]{"Hilton", 2L}, new Object[]{"Marriott", 1L}));

        Map<String, Long> actual = hotelService.getHistogram("brand");

        assertEquals(2L, actual.get("Hilton"));
        assertEquals(1L, actual.get("Marriott"));
    }

    @Test
    void getHistogram_shouldThrowBadRequest_whenParamUnsupported() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> hotelService.getHistogram("unknown"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void addAmenities_shouldThrowNotFound_whenHotelMissing() {
        when(hotelRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> hotelService.addAmenities(999L, List.of("Free WiFi")));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void addAmenities_shouldPrepareAndRemoveDeduplicateAmenities() {
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(baseHotel));
        when(amenityRepository.findByNameToLowerCase("free wifi")).thenReturn(Optional.empty());
        when(amenityRepository.findByNameToLowerCase("fitness center")).thenReturn(Optional.of(
                Amenity.builder()
                        .id(10L)
                        .nameToLowerCase("fitness center")
                        .displayName("Fitness center")
                        .hotels(new HashSet<>())
                        .build()
        ));
        when(amenityRepository.save(any(Amenity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        hotelService.addAmenities(1L, List.of(" Free WiFi ", "FREE WIFI", "fitness center"));

        verify(amenityRepository).findByNameToLowerCase("free wifi");
        verify(amenityRepository).findByNameToLowerCase("fitness center");
        verify(hotelRepository).save(baseHotel);
    }

    @Test
    void addAmenities_shouldNotSaveHotel_whenAmenitiesEmpty() {
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(baseHotel));

        hotelService.addAmenities(1L, List.of());

        verify(hotelRepository, never()).save(any(Hotel.class));
    }

    @Test
    void search_shouldReturnAll_whenArgumentIsNull() {
        List<Hotel> hotels = List.of(baseHotel);
        List<HotelShortResponseDto> expected = List.of(baseShortDto);

        when(hotelRepository.findAll()).thenReturn(hotels);
        when(hotelMapper.toShortDtoList(hotels)).thenReturn(expected);

        List<HotelShortResponseDto> actual = hotelService.search(null);

        assertEquals(expected, actual);
    }

    @Test
    void search_shouldFilterByAmenities() {
        HotelSearchRequestDto filter = new HotelSearchRequestDto();
        filter.setAmenities(List.of("Free WiFi"));

        Amenity amenity = Amenity.builder()
                .nameToLowerCase("free wifi")
                .displayName("Free WiFi")
                .build();
        baseHotel.setAmenities(Set.of(amenity));
        List<Hotel> found = List.of(baseHotel);
        List<HotelShortResponseDto> expected = List.of(baseShortDto);

        when(hotelRepository.findDistinctByAmenityNames(List.of("free wifi"))).thenReturn(found);
        when(hotelMapper.toShortDtoList(found)).thenReturn(expected);

        List<HotelShortResponseDto> actual = hotelService.search(filter);

        assertEquals(expected, actual);
    }

    @Test
    void getHotelById_shouldThrowNotFound_whenMissing() {
        when(hotelRepository.findDetailsById(5L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> hotelService.getHotelById(5L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void getHotelById_shouldReturnDetails_whenFound() {
        when(hotelRepository.findDetailsById(1L)).thenReturn(Optional.of(baseHotel));
        when(hotelMapper.toDetailsDto(baseHotel)).thenReturn(baseDetailsDto);

        HotelDetailsResponseDto actual = hotelService.getHotelById(1L);

        assertEquals(baseDetailsDto, actual);
    }

    private CreateHotelRequestDto buildCreateRequest(CreateHotelRequestDto.CreateArrivalTimeDto arrivalTimeDto) {
        return new CreateHotelRequestDto(
                "Hotel",
                "desc",
                "Brand",
                new CreateHotelRequestDto.CreateAddressDto("9", "Street", "Minsk", "Belarus", "220004"),
                new CreateHotelRequestDto.CreateContactDto("+375170000000", "hotel@example.com"),
                arrivalTimeDto
        );
    }
}
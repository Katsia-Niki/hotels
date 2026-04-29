package by.nikifarava.hotel.controller;

import by.nikifarava.hotel.dto.request.CreateHotelRequestDto;
import by.nikifarava.hotel.dto.response.HotelDetailsResponseDto;
import by.nikifarava.hotel.dto.request.HotelSearchRequestDto;
import by.nikifarava.hotel.dto.response.HotelShortResponseDto;
import by.nikifarava.hotel.service.HotelService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/property-view")
@RequiredArgsConstructor
@Slf4j
public class HotelController {

    private final HotelService hotelService;

    @GetMapping("/hotels")
    public List<HotelShortResponseDto> getHotels() {
        log.info("Getting all hotels");
        return hotelService.getAllHotels();
    }

    @GetMapping("/hotels/{id}")
    public HotelDetailsResponseDto getHotelById(@PathVariable Long id) {
        log.info("Getting hotel details by id={}", id);
        return hotelService.getHotelById(id);
    }

    @GetMapping("/search")
    public List<HotelShortResponseDto> searchHotels(@ModelAttribute HotelSearchRequestDto filter) {
        log.debug(
                "Searching hotels with filters name={}, brand={}, city={}, country={}, amenities={}",
                filter.getName(),
                filter.getBrand(),
                filter.getCity(),
                filter.getCountry(),
                filter.getAmenities()
        );
        return hotelService.search(filter);
    }

    @PostMapping("/hotels")
    @ResponseStatus(HttpStatus.CREATED)
    public HotelShortResponseDto createHotel(@Valid @RequestBody CreateHotelRequestDto request) {
        log.info("Creating hotel with name='{}', brand='{}'", request.name(), request.brand());
        return hotelService.createHotel(request);
    }

    @PostMapping("/hotels/{id}/amenities")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addAmenities(
            @PathVariable Long id,
            @RequestBody List<String> amenities
    ) {
        log.info("Adding amenities for hotel id={}, amenities count={}", id, amenities == null ? 0 : amenities.size());
        hotelService.addAmenities(id, amenities);
    }

    @GetMapping("/histogram/{param}")
    public Map<String, Long> getHistogram(@PathVariable String param) {
        log.info("Getting histogram by param={}", param);
        return hotelService.getHistogram(param);
    }
}

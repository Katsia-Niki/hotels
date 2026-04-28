package by.nikifarava.hotel.dto;

import java.util.List;

public record HotelDetailsResponseDto(
        Long id,
        String name,
        String description,
        String brand,
        AddressDto address,
        ContactDto contacts,
        ArrivalTimeDto arrivalTime,
        List<String> amenities
) {
}

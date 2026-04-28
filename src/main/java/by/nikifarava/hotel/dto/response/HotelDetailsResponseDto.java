package by.nikifarava.hotel.dto.response;

import java.util.List;

public record HotelDetailsResponseDto(
        Long id,
        String name,
        String description,
        String brand,
        AddressResponseDto address,
        ContactResponseDto contacts,
        ArrivalTimeResponseDto arrivalTime,
        List<String> amenities
) {
}

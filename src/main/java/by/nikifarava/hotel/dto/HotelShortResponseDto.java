package by.nikifarava.hotel.dto;

public record HotelShortResponseDto(
        Long id,
        String name,
        String description,
        String address,
        String phone
) {
}

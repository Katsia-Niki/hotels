package by.nikifarava.hotel.dto.response;

public record AddressResponseDto(
        String houseNumber,
        String street,
        String city,
        String country,
        String postCode
) {
}

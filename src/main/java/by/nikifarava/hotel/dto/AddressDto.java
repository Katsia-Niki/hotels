package by.nikifarava.hotel.dto;

public record AddressDto(
        String houseNumber,
        String street,
        String city,
        String country,
        String postCode
) {
}

package by.nikifarava.hotel.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateHotelRequestDto(
        @NotBlank @Size(max = 255) String name,
        String description,
        @NotBlank @Size(max = 120) String brand,
        @NotNull @Valid CreateAddressDto address,
        @NotNull @Valid CreateContactDto contacts,
        @Valid CreateArrivalTimeDto arrivalTime
) {
    public record CreateAddressDto(
            @NotBlank @Size(max = 20) String houseNumber,
            @NotBlank @Size(max = 255) String street,
            @NotBlank @Size(max = 120) String city,
            @NotBlank @Size(max = 120) String country,
            @NotBlank @Size(max = 20) String postCode
    ) {
    }

    public record CreateContactDto(
            @NotBlank @Size(max = 40) String phone,
            @NotBlank @Email @Size(max = 255) String email
    ) {
    }

    public record CreateArrivalTimeDto(
            @Size(max = 5) String checkIn,
            @Size(max = 5) String checkOut
    ) {
    }
}

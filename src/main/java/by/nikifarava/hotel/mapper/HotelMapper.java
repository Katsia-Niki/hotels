package by.nikifarava.hotel.mapper;

import by.nikifarava.hotel.dto.response.*;
import by.nikifarava.hotel.entity.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HotelMapper {

    public HotelShortResponseDto toShortDto(Hotel hotel) {
        return new HotelShortResponseDto(
                hotel.getId(),
                hotel.getName(),
                hotel.getDescription(),
                formatAddress(hotel.getAddress()),
                hotel.getContact() != null ? hotel.getContact().getPhone() : null
        );
    }

    public HotelDetailsResponseDto toDetailsDto(Hotel hotel) {
        return new HotelDetailsResponseDto(
                hotel.getId(),
                hotel.getName(),
                hotel.getDescription(),
                hotel.getBrand(),
                toAddressDto(hotel.getAddress()),
                toContactDto(hotel.getContact()),
                toArrivalTimeDto(hotel.getArrivalTime()),
                hotel.getAmenities().stream()
                        .map(Amenity::getDisplayName)
                        .sorted()
                        .toList()
        );
    }

    public List<HotelShortResponseDto> toShortDtoList(List<Hotel> hotels) {
        return hotels.stream().map(this::toShortDto).toList();
    }

    private String formatAddress(Address address) {
        if (address == null) {
            return null;
        }
        String streetAddress = address.getHouseNumber() + " " + address.getStreet();

        return String.join(", ",
                streetAddress,
                address.getCity(),
                address.getPostCode(),
                address.getCountry()
        );
    }

    private AddressResponseDto toAddressDto(Address address) {
        if (address == null) {
            return null;
        }
        return new AddressResponseDto(
                address.getHouseNumber(),
                address.getStreet(),
                address.getCity(),
                address.getCountry(),
                address.getPostCode()
        );
    }

    private ContactResponseDto toContactDto(Contact contact) {
        if (contact == null) {
            return null;
        }
        return new ContactResponseDto(contact.getPhone(), contact.getEmail());
    }

    private ArrivalTimeResponseDto toArrivalTimeDto(ArrivalTime arrivalTime) {
        if (arrivalTime == null) {
            return null;
        }
        return new ArrivalTimeResponseDto(arrivalTime.getCheckIn(), arrivalTime.getCheckOut());
    }
}

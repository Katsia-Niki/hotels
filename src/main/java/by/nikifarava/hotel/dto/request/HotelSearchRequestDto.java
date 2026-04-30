package by.nikifarava.hotel.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class HotelSearchRequestDto {

    private String name;
    private String brand;
    private String city;
    private String country;
    private List<String> amenities;

}

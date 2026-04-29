package by.nikifarava.hotel.api;

import by.nikifarava.hotel.dto.request.CreateHotelRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class HotelApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("success")
    void getHotels_shouldReturnShortHotelList() throws Exception {
        createHotel("Minsk");

        mockMvc.perform(get("/property-view/hotels"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].address").exists())
                .andExpect(jsonPath("$[0].phone").exists());
    }

    @Test
    @DisplayName("success")
    void getHotelById_shouldReturnDetails_whenExists() throws Exception {
        long hotelId = createHotel("Minsk");

        mockMvc.perform(get("/property-view/hotels/{id}", hotelId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(hotelId))
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.brand").exists())
                .andExpect(jsonPath("$.address.city").value("Minsk"))
                .andExpect(jsonPath("$.contacts.phone").exists())
                .andExpect(jsonPath("$.arrivalTime.checkIn").exists())
                .andExpect(jsonPath("$.arrivalTime.checkOut").exists())
                .andExpect(jsonPath("$.amenities").isArray());
    }

    @Test
    @DisplayName("status 404")
    void getHotelById_shouldReturn404_whenNotExists() throws Exception {
        mockMvc.perform(get("/property-view/hotels/{id}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Hotel not found"));
    }

    @Test
    @DisplayName("success")
    void searchHotels_shouldFilterByCity() throws Exception {
        createHotel("Minsk");
        createHotel("Grodno");

        mockMvc.perform(get("/property-view/search").param("city", "Minsk"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").exists());
    }

    @Test
    @DisplayName("success")
    void createHotel_shouldReturn201() throws Exception {

        CreateHotelRequestDto.CreateAddressDto addressDto = new CreateHotelRequestDto.CreateAddressDto("9",
                "Pobediteley", "Minsk", "Belarus", "220100");
        CreateHotelRequestDto.CreateContactDto contactDto = new CreateHotelRequestDto.CreateContactDto("+375 17 300 25 25",
                uniqueEmail());
        CreateHotelRequestDto.CreateArrivalTimeDto arrivalTimeDto = new CreateHotelRequestDto.CreateArrivalTimeDto(LocalTime
                .of(12, 0), LocalTime.of(14, 0));

        CreateHotelRequestDto dto = new CreateHotelRequestDto("DoubleTree by Hilton Brest", "Test description",
                "Hilton", addressDto, contactDto, arrivalTimeDto);

        String json = objectMapper.writeValueAsString(dto);

                mockMvc.perform(post("/property-view/hotels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("DoubleTree by Hilton Brest"))
                .andExpect(jsonPath("$.address").exists())
                .andExpect(jsonPath("$.phone").value("+375 17 300 25 25"));
    }

    @Test
    @DisplayName("success")
    void createHotel_shouldSetDefaultArrivalTime_whenArrivalTimeMissing() throws Exception {
        CreateHotelRequestDto.CreateAddressDto addressDto = new CreateHotelRequestDto.CreateAddressDto("9",
                "Pobediteley", "Minsk", "Belarus", "220100");
        CreateHotelRequestDto.CreateContactDto contactDto = new CreateHotelRequestDto.CreateContactDto("+375 17 300 25 25",
                uniqueEmail());
        CreateHotelRequestDto.CreateArrivalTimeDto arrivalTimeDto = null;

        CreateHotelRequestDto dto = new CreateHotelRequestDto("Hilton", "Test description",
                "Hilton", addressDto, contactDto, arrivalTimeDto);

        String json = objectMapper.writeValueAsString(dto);

        MvcResult createResult = mockMvc.perform(post("/property-view/hotels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode createBody = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long hotelId = createBody.get("id").asLong();

        mockMvc.perform(get("/property-view/hotels/{id}", hotelId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.arrivalTime.checkIn").value("14:00"))
                .andExpect(jsonPath("$.arrivalTime.checkOut").value("12:00"));
    }

    @Test
    @DisplayName("status 204")
    void addAmenities_shouldReturn204() throws Exception {
        long hotelId = createHotel("Gomel");
        String amenitiesJson = """
                ["Free parking", "Free WiFi", "Fitness center"]
                """;

        mockMvc.perform(post("/property-view/hotels/{id}/amenities", hotelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(amenitiesJson))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("status 404")
    void addAmenities_shouldReturn404_whenHotelNotFound() throws Exception {
        mockMvc.perform(post("/property-view/hotels/{id}/amenities", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[\"Free parking\"]"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Hotel not found"));
    }

    @Test
    @DisplayName("success")
    void getHistogram_shouldReturnCityHistogram() throws Exception {
        createHotel("Minsk");
        createHotel("Mogilev");

        mockMvc.perform(get("/property-view/histogram/{param}", "city"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("status 400")
    void getHistogram_shouldReturn400_whenParamInvalid() throws Exception {
        mockMvc.perform(get("/property-view/histogram/{param}", "unknown"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    private long createHotel(String city) throws Exception {

        CreateHotelRequestDto.CreateAddressDto addressDto = new CreateHotelRequestDto.CreateAddressDto("9",
                "Pobediteley", "Minsk", "Belarus", "220100");
        CreateHotelRequestDto.CreateContactDto contactDto = new CreateHotelRequestDto.CreateContactDto("+375 17 300 25 25",
                uniqueEmail());
        CreateHotelRequestDto.CreateArrivalTimeDto arrivalTimeDto = new CreateHotelRequestDto.CreateArrivalTimeDto(LocalTime
                .of(12, 0), LocalTime.of(14, 0));

        CreateHotelRequestDto dto = new CreateHotelRequestDto("Hilton", "Test description",
                "Hilton", addressDto, contactDto, arrivalTimeDto);

        String json = objectMapper.writeValueAsString(dto);

        MvcResult result = mockMvc.perform(post("/property-view/hotels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("id").asLong();
    }

    private String uniqueEmail() {
        return "hotel-" + UUID.randomUUID() + "@hilton.com";
    }

}

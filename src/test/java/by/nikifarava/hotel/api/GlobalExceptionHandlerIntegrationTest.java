package by.nikifarava.hotel.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class GlobalExceptionHandlerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Validation error returns standardized 400 response")
    void shouldReturnValidationErrorResponse() throws Exception {
        String invalidJson = """
                {
                  "name": "",
                  "brand": "",
                  "address": {
                    "houseNumber": "",
                    "street": "",
                    "city": "",
                    "country": "",
                    "postCode": ""
                  },
                  "contacts": {
                    "phone": "",
                    "email": "invalid"
                  }
                }
                """;

        mockMvc.perform(post("/property-view/hotels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/property-view/hotels"))
                .andExpect(jsonPath("$.errors").isMap())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.brand").exists());
    }

    @Test
    @DisplayName("Malformed JSON returns standardized 400 response")
    void shouldReturnBadRequestForMalformedJson() throws Exception {
        String malformedJson = """
                {"name":"x","brand":
                """;

        mockMvc.perform(post("/property-view/hotels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/property-view/hotels"));
    }

    @Test
    @DisplayName("ResponseStatusException(NOT_FOUND) returns standardized 404 response")
    void shouldReturnNotFoundResponse() throws Exception {
        mockMvc.perform(get("/property-view/hotels/{id}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Hotel not found"))
                .andExpect(jsonPath("$.path").value("/property-view/hotels/999999"));
    }
}

package by.nikifarava.hotel.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalTime;

public record ArrivalTimeResponseDto(
        @JsonFormat(pattern = "HH:mm") LocalTime checkIn,
        @JsonFormat(pattern = "HH:mm") LocalTime checkOut
) {
}

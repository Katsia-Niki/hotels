package by.nikifarava.hotel.dto.response;

import java.time.OffsetDateTime;
import java.util.Map;

public record ErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String message,
        String path,
        Map<String, String> errors
) {
}

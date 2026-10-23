package com.nimbusfreight.temperature.api;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class ReadingRequestTest {
    @Test void acceptsExampleReadingShape() {
        ReadingRequest request = new ReadingRequest("TRUCK-101", 9.4, Instant.parse("2026-10-08T10:05:00Z"));
        assertEquals("TRUCK-101", request.truckId());
        assertEquals(9.4, request.temperature());
    }
}

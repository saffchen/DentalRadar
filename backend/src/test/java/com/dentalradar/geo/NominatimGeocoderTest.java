package com.dentalradar.geo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NominatimGeocoderTest {

    private final NominatimGeocoder geocoder = new NominatimGeocoder(new ObjectMapper());

    @Test
    void readsTheFirstPoint() {
        Optional<GeoPoint> point = geocoder.parseBody("[{\"lat\":\"55.75\",\"lon\":\"37.61\"}]");

        assertEquals(55.75, point.orElseThrow().latitude());
        assertEquals(37.61, point.orElseThrow().longitude());
    }

    @Test
    void returnsEmptyWhenTheListIsEmpty() {
        assertTrue(geocoder.parseBody("[]").isEmpty());
    }
}

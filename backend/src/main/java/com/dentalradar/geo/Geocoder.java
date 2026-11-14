package com.dentalradar.geo;

import java.util.Optional;

public interface Geocoder {

    Optional<GeoPoint> geocode(String address);
}

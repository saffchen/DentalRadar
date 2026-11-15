package com.dentalradar.geo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NominatimGeocoder implements Geocoder {

    private static final Logger log = LoggerFactory.getLogger(NominatimGeocoder.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, Optional<GeoPoint>> cache = new ConcurrentHashMap<>();

    public NominatimGeocoder(ObjectMapper objectMapper) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        this.restClient = RestClient.builder().requestFactory(factory).build();
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<GeoPoint> geocode(String address) {
        String key = normalize(address);
        Optional<GeoPoint> cached = cache.get(key);
        if (cached != null) {
            return cached;
        }
        synchronized (this) {
            cached = cache.get(key);
            if (cached != null) {
                return cached;
            }
            Optional<GeoPoint> point = request(address);
            cache.put(key, point);
            return point;
        }
    }

    private Optional<GeoPoint> request(String address) {
        try {
            String body = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("nominatim.openstreetmap.org")
                            .path("/search")
                            .queryParam("q", address)
                            .queryParam("format", "json")
                            .queryParam("limit", "1")
                            .build())
                    .header("User-Agent", "MoiStomatolog")
                    .retrieve()
                    .body(String.class);
            return parseBody(body);
        } catch (RestClientException | GeocodingException exception) {
            log.warn("Геокодирование не выполнено для адреса {}", address, exception);
            throw new GeocodingException("Сервис геокодирования недоступен", exception);
        }
    }

    Optional<GeoPoint> parseBody(String body) {
        try {
            JsonNode node = objectMapper.readTree(body == null ? "[]" : body);
            if (!node.isArray() || node.isEmpty()) {
                return Optional.empty();
            }
            JsonNode first = node.get(0);
            if (first.get("lat") == null || first.get("lon") == null) {
                return Optional.empty();
            }
            return Optional.of(new GeoPoint(first.get("lat").asDouble(), first.get("lon").asDouble()));
        } catch (Exception exception) {
            throw new GeocodingException("Ответ геокодера не прочитан", exception);
        }
    }

    private String normalize(String address) {
        return address.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }
}

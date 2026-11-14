package com.dentalradar;

import com.dentalradar.geo.GeoPoint;
import com.dentalradar.geo.Geocoder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(ApiIntegrationTest.FakeGeocoderConfig.class)
class ApiIntegrationTest {

    private static final Path DATABASE = Path.of(
            System.getProperty("java.io.tmpdir"),
            "dentalradar-it-" + UUID.randomUUID() + ".json"
    );

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("app.storage.path", DATABASE::toString);
        registry.add("app.seed.enabled", () -> "false");
        registry.add("app.jwt.secret", () -> "test-secret-key-must-be-long-enough-32b");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void clearDatabase() throws Exception {
        Files.deleteIfExists(DATABASE);
        FakeGeocoderConfig.calls.set(0);
    }

    @Test
    void registersAndRejectsADuplicateEmail() throws Exception {
        register("user@example.com", "USER", "Анна");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("user@example.com", "USER", "Анна")))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsAShortPassword() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@example.com","password":"short","displayName":"Анна","role":"USER"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void logsInAndRejectsAWrongPassword() throws Exception {
        register("user@example.com", "USER", "Анна");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@example.com","password":"password1"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.token").isNotEmpty());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@example.com","password":"wrong-password"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void blocksTheWrongRoleAndAMissingToken() throws Exception {
        String user = register("user@example.com", "USER", "Анна");
        String clinic = register("clinic@example.com", "CLINIC", "Клиника");

        mockMvc.perform(get("/api/clinics"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/clinics").header("Authorization", "Bearer " + clinic))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/clinic/me").header("Authorization", "Bearer " + user))
                .andExpect(status().isForbidden());
    }

    @Test
    void savesCoordinatesAndClearsThemForAnUnknownAddress() throws Exception {
        String clinic = register("clinic@example.com", "CLINIC", "Клиника");

        mockMvc.perform(put("/api/clinic/me")
                        .header("Authorization", "Bearer " + clinic)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileJson("Клиника", "Москва, Тверская 1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latitude").value(55.75))
                .andExpect(jsonPath("$.longitude").value(37.62))
                .andExpect(jsonPath("$.warning").value(nullValue()));

        mockMvc.perform(put("/api/clinic/me")
                        .header("Authorization", "Bearer " + clinic)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileJson("Клиника новое имя", "Москва, Тверская 1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Клиника новое имя"));
        assertEquals(1, FakeGeocoderConfig.calls.get());

        mockMvc.perform(put("/api/clinic/me")
                        .header("Authorization", "Bearer " + clinic)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileJson("Клиника", "неизвестный адрес")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latitude").value(nullValue()))
                .andExpect(jsonPath("$.warning").value("Адрес не найден. Клиника не показана на карте."));
    }

    @Test
    void updatesAndDeletesServices() throws Exception {
        String clinic = register("clinic@example.com", "CLINIC", "Клиника");
        String other = register("other@example.com", "CLINIC", "Другая");

        MvcResult created = mockMvc.perform(post("/api/clinic/me/services")
                        .header("Authorization", "Bearer " + clinic)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Чистка","price":3500.00}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Чистка"))
                .andReturn();
        String serviceId = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(put("/api/clinic/me/services/" + serviceId)
                        .header("Authorization", "Bearer " + clinic)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Чистка","price":3600.50}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(3600.50));

        mockMvc.perform(delete("/api/clinic/me/services/" + serviceId)
                        .header("Authorization", "Bearer " + other))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/clinic/me/services/" + serviceId)
                        .header("Authorization", "Bearer " + clinic))
                .andExpect(status().isNoContent());
    }

    @Test
    void filtersClinicsAndKeepsOneReviewPerUser() throws Exception {
        String user = register("user@example.com", "USER", "Анна");
        String cleaning = register("clean@example.com", "CLINIC", "Клиника чистки");
        String low = register("low@example.com", "CLINIC", "Клиника с низкой оценкой");
        String implant = register("implant@example.com", "CLINIC", "Клиника имплантации");
        String hidden = register("hidden@example.com", "CLINIC", "Клиника без точки");

        saveAddress(cleaning, "Клиника чистки", "Москва, центр");
        saveAddress(low, "Клиника с низкой оценкой", "Москва, север");
        saveAddress(implant, "Клиника имплантации", "Москва, юг");
        saveAddress(hidden, "Клиника без точки", "неизвестный адрес");

        addService(cleaning, "Чистка", "1000.00");
        addService(low, "Чистка", "2000.00");
        addService(implant, "Имплантация", "9000.00");
        addService(hidden, "Чистка", "1500.00");

        String cleaningId = clinicId(cleaning);
        String lowId = clinicId(low);

        mockMvc.perform(put("/api/clinics/" + cleaningId + "/reviews")
                        .header("Authorization", "Bearer " + user)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"rating":5,"text":"Отлично"}
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/clinics/" + cleaningId + "/reviews")
                        .header("Authorization", "Bearer " + user)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"rating":5,"text":"Обновлённый отзыв"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mine").value(true));
        mockMvc.perform(put("/api/clinics/" + lowId + "/reviews")
                        .header("Authorization", "Bearer " + user)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"rating":3,"text":"Нормально"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/clinics/" + cleaningId)
                        .header("Authorization", "Bearer " + user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewCount").value(1))
                .andExpect(jsonPath("$.reviews[0].text").value("Обновлённый отзыв"))
                .andExpect(jsonPath("$.averageRating").value(5.0));

        mockMvc.perform(get("/api/clinics")
                        .header("Authorization", "Bearer " + user)
                        .param("service", "Чистка"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/clinics")
                        .header("Authorization", "Bearer " + user)
                        .param("service", "Чистка")
                        .param("minRating", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Клиника чистки"))
                .andExpect(jsonPath("$[0].services[0].name").value("Чистка"));

        mockMvc.perform(get("/api/clinics")
                        .header("Authorization", "Bearer " + user)
                        .param("minRating", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    private String register(String email, String role, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson(email, role, name)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private void saveAddress(String token, String name, String address) throws Exception {
        mockMvc.perform(put("/api/clinic/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileJson(name, address)))
                .andExpect(status().isOk());
    }

    private void addService(String token, String name, String price) throws Exception {
        mockMvc.perform(post("/api/clinic/me/services")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","price":%s}
                                """.formatted(name, price)))
                .andExpect(status().isCreated());
    }

    private String clinicId(String token) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/clinic/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("id").asText();
    }

    private String userJson(String email, String role, String name) {
        return """
                {"email":"%s","password":"password1","displayName":"%s","role":"%s"}
                """.formatted(email, name, role);
    }

    private String profileJson(String name, String address) {
        return """
                {"name":"%s","description":"Описание","address":"%s"}
                """.formatted(name, address);
    }

    @TestConfiguration
    static class FakeGeocoderConfig {

        static final AtomicInteger calls = new AtomicInteger();

        @Bean
        @Primary
        Geocoder geocoder() {
            return address -> {
                calls.incrementAndGet();
                if (address.toLowerCase().contains("неизвест")) {
                    return Optional.empty();
                }
                return Optional.of(new GeoPoint(55.75, 37.62));
            };
        }
    }
}

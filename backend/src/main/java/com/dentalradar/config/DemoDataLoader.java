package com.dentalradar.config;

import com.dentalradar.model.Clinic;
import com.dentalradar.model.Review;
import com.dentalradar.model.Role;
import com.dentalradar.model.ServiceItem;
import com.dentalradar.model.UserAccount;
import com.dentalradar.storage.JsonFileStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DemoDataLoader implements CommandLineRunner {

    private final JsonFileStore store;
    private final PasswordEncoder passwordEncoder;

    public DemoDataLoader(JsonFileStore store, PasswordEncoder passwordEncoder) {
        this.store = store;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String passwordHash = passwordEncoder.encode("demo1234");
        store.update(database -> {
            if (!database.getUsers().isEmpty()) {
                return;
            }
            database.getUsers().add(user("user-anna", "user@demo.local", passwordHash, Role.USER, "Анна Петрова"));
            database.getUsers().add(user("user-center", "center@demo.local", passwordHash, Role.CLINIC, "Демо Клиника Центр"));
            database.getUsers().add(user("user-north", "north@demo.local", passwordHash, Role.CLINIC, "Демо Клиника Север"));
            database.getUsers().add(user("user-south", "south@demo.local", passwordHash, Role.CLINIC, "Демо Клиника Юг"));

            database.getClinics().add(clinic(
                    "clinic-center",
                    "user-center",
                    "Демо Клиника Центр",
                    "Терапия и гигиена в центре Москвы.",
                    "ул. Тверская, 12, Москва",
                    55.7570,
                    37.6140
            ));
            database.getClinics().add(clinic(
                    "clinic-north",
                    "user-north",
                    "Демо Клиника Север",
                    "Хирургия и гигиена на севере Москвы.",
                    "Ленинградский проспект, 36, Москва",
                    55.7890,
                    37.5580
            ));
            database.getClinics().add(clinic(
                    "clinic-south",
                    "user-south",
                    "Демо Клиника Юг",
                    "Терапия и имплантация на юге Москвы.",
                    "Варшавское шоссе, 87, Москва",
                    55.6530,
                    37.6200
            ));

            database.getServices().add(service("svc-center-caries", "clinic-center", "Лечение кариеса", "4500.00"));
            database.getServices().add(service("svc-center-clean", "clinic-center", "Профессиональная чистка", "3500.00"));
            database.getServices().add(service("svc-north-extract", "clinic-north", "Удаление зуба", "3000.00"));
            database.getServices().add(service("svc-north-clean", "clinic-north", "Профессиональная чистка", "4000.00"));
            database.getServices().add(service("svc-south-caries", "clinic-south", "Лечение кариеса", "5000.00"));
            database.getServices().add(service("svc-south-implant", "clinic-south", "Имплантация", "45000.00"));

            database.getReviews().add(review(
                    "rev-center",
                    "clinic-center",
                    "user-anna",
                    5,
                    "Хорошая клиника в центре.",
                    "2026-01-15T10:00:00Z"
            ));
            database.getReviews().add(review(
                    "rev-north",
                    "clinic-north",
                    "user-anna",
                    4,
                    "Приняли вовремя.",
                    "2026-02-02T12:30:00Z"
            ));
        });
    }

    private UserAccount user(String id, String email, String passwordHash, Role role, String displayName) {
        UserAccount user = new UserAccount();
        user.setId(id);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setRole(role);
        user.setDisplayName(displayName);
        return user;
    }

    private Clinic clinic(
            String id,
            String ownerUserId,
            String name,
            String description,
            String address,
            double latitude,
            double longitude
    ) {
        Clinic clinic = new Clinic();
        clinic.setId(id);
        clinic.setOwnerUserId(ownerUserId);
        clinic.setName(name);
        clinic.setDescription(description);
        clinic.setAddress(address);
        clinic.setLatitude(latitude);
        clinic.setLongitude(longitude);
        return clinic;
    }

    private ServiceItem service(String id, String clinicId, String name, String price) {
        ServiceItem service = new ServiceItem();
        service.setId(id);
        service.setClinicId(clinicId);
        service.setName(name);
        service.setPrice(new BigDecimal(price));
        return service;
    }

    private Review review(String id, String clinicId, String authorUserId, int rating, String text, String createdAt) {
        Review review = new Review();
        review.setId(id);
        review.setClinicId(clinicId);
        review.setAuthorUserId(authorUserId);
        review.setRating(rating);
        review.setText(text);
        review.setCreatedAt(Instant.parse(createdAt));
        return review;
    }
}

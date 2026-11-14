package com.dentalradar.repository;

import com.dentalradar.model.Clinic;
import com.dentalradar.storage.JsonFileStore;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JsonClinicRepository implements ClinicRepository {

    private final JsonFileStore store;

    public JsonClinicRepository(JsonFileStore store) {
        this.store = store;
    }

    @Override
    public List<Clinic> findAll() {
        return store.read().getClinics();
    }

    @Override
    public Optional<Clinic> findById(String id) {
        return store.read().getClinics().stream()
                .filter(clinic -> clinic.getId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<Clinic> findByOwnerUserId(String ownerUserId) {
        return store.read().getClinics().stream()
                .filter(clinic -> ownerUserId.equals(clinic.getOwnerUserId()))
                .findFirst();
    }

    @Override
    public Clinic save(Clinic clinic) {
        store.update(database -> {
            database.getClinics().removeIf(existing -> existing.getId().equals(clinic.getId()));
            database.getClinics().add(clinic);
        });
        return clinic;
    }
}

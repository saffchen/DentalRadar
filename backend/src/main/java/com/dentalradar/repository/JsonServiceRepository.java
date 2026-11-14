package com.dentalradar.repository;

import com.dentalradar.model.ServiceItem;
import com.dentalradar.storage.JsonFileStore;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JsonServiceRepository implements ServiceRepository {

    private final JsonFileStore store;

    public JsonServiceRepository(JsonFileStore store) {
        this.store = store;
    }

    @Override
    public List<ServiceItem> findByClinicId(String clinicId) {
        return store.read().getServices().stream()
                .filter(service -> clinicId.equals(service.getClinicId()))
                .toList();
    }

    @Override
    public Optional<ServiceItem> findById(String id) {
        return store.read().getServices().stream()
                .filter(service -> service.getId().equals(id))
                .findFirst();
    }

    @Override
    public ServiceItem save(ServiceItem service) {
        store.update(database -> {
            database.getServices().removeIf(existing -> existing.getId().equals(service.getId()));
            database.getServices().add(service);
        });
        return service;
    }

    @Override
    public void deleteById(String id) {
        store.update(database -> database.getServices().removeIf(service -> service.getId().equals(id)));
    }
}

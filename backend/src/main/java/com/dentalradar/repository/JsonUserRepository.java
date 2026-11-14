package com.dentalradar.repository;

import com.dentalradar.model.UserAccount;
import com.dentalradar.storage.JsonFileStore;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JsonUserRepository implements UserRepository {

    private final JsonFileStore store;

    public JsonUserRepository(JsonFileStore store) {
        this.store = store;
    }

    @Override
    public Optional<UserAccount> findById(String id) {
        return store.read().getUsers().stream()
                .filter(user -> user.getId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<UserAccount> findByEmail(String email) {
        String normalized = email.trim().toLowerCase();
        return store.read().getUsers().stream()
                .filter(user -> user.getEmail().equals(normalized))
                .findFirst();
    }

    @Override
    public UserAccount save(UserAccount user) {
        store.update(database -> {
            database.getUsers().removeIf(existing -> existing.getId().equals(user.getId()));
            database.getUsers().add(user);
        });
        return user;
    }
}

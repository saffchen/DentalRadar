package com.dentalradar.repository;

import com.dentalradar.model.UserAccount;

import java.util.Optional;

public interface UserRepository {

    Optional<UserAccount> findById(String id);

    Optional<UserAccount> findByEmail(String email);

    UserAccount save(UserAccount user);
}

package com.dentalradar.repository;

import com.dentalradar.model.Clinic;

import java.util.List;
import java.util.Optional;

public interface ClinicRepository {

    List<Clinic> findAll();

    Optional<Clinic> findById(String id);

    Optional<Clinic> findByOwnerUserId(String ownerUserId);

    Clinic save(Clinic clinic);
}

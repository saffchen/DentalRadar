package com.dentalradar.repository;

import com.dentalradar.model.ServiceItem;

import java.util.List;
import java.util.Optional;

public interface ServiceRepository {

    List<ServiceItem> findByClinicId(String clinicId);

    Optional<ServiceItem> findById(String id);

    ServiceItem save(ServiceItem service);

    void deleteById(String id);
}

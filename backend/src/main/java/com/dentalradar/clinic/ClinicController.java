package com.dentalradar.clinic;

import com.dentalradar.dto.ClinicProfileRequest;
import com.dentalradar.dto.ClinicProfileResponse;
import com.dentalradar.dto.ServiceRequest;
import com.dentalradar.dto.ServiceResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clinic")
public class ClinicController {

    private final ClinicService clinicService;

    public ClinicController(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    @GetMapping("/me")
    public ClinicProfileResponse me() {
        return clinicService.ownProfile();
    }

    @PutMapping("/me")
    public ClinicProfileResponse update(@Valid @RequestBody ClinicProfileRequest request) {
        return clinicService.updateOwnProfile(request);
    }

    @PostMapping("/me/services")
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceResponse addService(@Valid @RequestBody ServiceRequest request) {
        return clinicService.addService(request);
    }

    @PutMapping("/me/services/{id}")
    public ServiceResponse updateService(@PathVariable String id, @Valid @RequestBody ServiceRequest request) {
        return clinicService.updateService(id, request);
    }

    @DeleteMapping("/me/services/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteService(@PathVariable String id) {
        clinicService.deleteService(id);
    }
}

package com.dentalradar.clinic;

import com.dentalradar.dto.ClinicMapResponse;
import com.dentalradar.dto.ClinicProfileResponse;
import com.dentalradar.dto.ReviewRequest;
import com.dentalradar.dto.ReviewResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/clinics")
public class ClinicQueryController {

    private final ClinicService clinicService;

    public ClinicQueryController(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    @GetMapping
    public List<ClinicMapResponse> search(
            @RequestParam(required = false) String service,
            @RequestParam(required = false) Integer minRating
    ) {
        return clinicService.search(service, minRating);
    }

    @GetMapping("/{id}")
    public ClinicProfileResponse detail(@PathVariable String id) {
        return clinicService.detail(id);
    }

    @PutMapping("/{id}/reviews")
    public ReviewResponse saveReview(@PathVariable String id, @Valid @RequestBody ReviewRequest request) {
        return clinicService.saveReview(id, request);
    }
}

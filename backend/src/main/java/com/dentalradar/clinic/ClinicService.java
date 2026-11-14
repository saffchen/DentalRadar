package com.dentalradar.clinic;

import com.dentalradar.auth.CurrentUser;
import com.dentalradar.dto.ClinicMapResponse;
import com.dentalradar.dto.ClinicProfileRequest;
import com.dentalradar.dto.ClinicProfileResponse;
import com.dentalradar.dto.ReviewRequest;
import com.dentalradar.dto.ReviewResponse;
import com.dentalradar.dto.ServiceRequest;
import com.dentalradar.dto.ServiceResponse;
import com.dentalradar.exception.BadRequestException;
import com.dentalradar.exception.NotFoundException;
import com.dentalradar.geo.GeoPoint;
import com.dentalradar.geo.Geocoder;
import com.dentalradar.geo.GeocodingException;
import com.dentalradar.model.Clinic;
import com.dentalradar.model.Review;
import com.dentalradar.model.ServiceItem;
import com.dentalradar.model.UserAccount;
import com.dentalradar.repository.ClinicRepository;
import com.dentalradar.repository.ReviewRepository;
import com.dentalradar.repository.ServiceRepository;
import com.dentalradar.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ClinicService {

    static final String ADDRESS_NOT_FOUND = "Адрес не найден. Клиника не показана на карте.";
    static final String ADDRESS_REQUIRED = "Укажите адрес";
    static final String GEOCODER_UNAVAILABLE = "Сервис геокодирования недоступен. Клиника не показана на карте.";

    private final ClinicRepository clinicRepository;
    private final ServiceRepository serviceRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final Geocoder geocoder;
    private final CurrentUser currentUser;

    public ClinicService(
            ClinicRepository clinicRepository,
            ServiceRepository serviceRepository,
            ReviewRepository reviewRepository,
            UserRepository userRepository,
            Geocoder geocoder,
            CurrentUser currentUser
    ) {
        this.clinicRepository = clinicRepository;
        this.serviceRepository = serviceRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.geocoder = geocoder;
        this.currentUser = currentUser;
    }

    public ClinicProfileResponse ownProfile() {
        return toProfile(requireOwnClinic(), null);
    }

    public ClinicProfileResponse updateOwnProfile(ClinicProfileRequest request) {
        Clinic clinic = requireOwnClinic();
        String address = request.address() == null ? "" : request.address().trim();
        String previousAddress = clinic.getAddress() == null ? "" : clinic.getAddress().trim();
        clinic.setName(request.name().trim());
        clinic.setDescription(request.description() == null ? "" : request.description().trim());
        clinic.setAddress(address);
        String warning = null;
        if (address.isBlank()) {
            clinic.setLatitude(null);
            clinic.setLongitude(null);
            warning = ADDRESS_REQUIRED;
        } else if (!address.equals(previousAddress) || clinic.getLatitude() == null || clinic.getLongitude() == null) {
            warning = applyGeocode(clinic, address);
        }
        clinicRepository.save(clinic);
        return toProfile(clinic, warning);
    }

    public ServiceResponse addService(ServiceRequest request) {
        Clinic clinic = requireOwnClinic();
        ServiceItem service = new ServiceItem();
        service.setId(UUID.randomUUID().toString());
        service.setClinicId(clinic.getId());
        service.setName(request.name().trim());
        service.setPrice(request.price());
        serviceRepository.save(service);
        return toService(service);
    }

    public ServiceResponse updateService(String serviceId, ServiceRequest request) {
        Clinic clinic = requireOwnClinic();
        ServiceItem service = serviceRepository.findById(serviceId)
                .filter(item -> clinic.getId().equals(item.getClinicId()))
                .orElseThrow(() -> new NotFoundException("Услуга не найдена"));
        service.setName(request.name().trim());
        service.setPrice(request.price());
        serviceRepository.save(service);
        return toService(service);
    }

    public void deleteService(String serviceId) {
        Clinic clinic = requireOwnClinic();
        ServiceItem service = serviceRepository.findById(serviceId)
                .filter(item -> clinic.getId().equals(item.getClinicId()))
                .orElseThrow(() -> new NotFoundException("Услуга не найдена"));
        serviceRepository.deleteById(service.getId());
    }

    public List<ClinicMapResponse> search(String service, Integer minRating) {
        if (minRating != null && (minRating < 1 || minRating > 5)) {
            throw new BadRequestException("Оценка фильтра должна быть от 1 до 5");
        }
        String serviceName = service == null ? "" : service.trim();
        return clinicRepository.findAll().stream()
                .filter(clinic -> clinic.getLatitude() != null && clinic.getLongitude() != null)
                .filter(clinic -> serviceName.isEmpty() || hasService(clinic.getId(), serviceName))
                .filter(clinic -> matchesRating(clinic.getId(), minRating))
                .sorted(Comparator.comparing(Clinic::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toMapItem)
                .toList();
    }

    public ClinicProfileResponse detail(String clinicId) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new NotFoundException("Клиника не найдена"));
        return toProfile(clinic, null);
    }

    public ReviewResponse saveReview(String clinicId, ReviewRequest request) {
        if (clinicRepository.findById(clinicId).isEmpty()) {
            throw new NotFoundException("Клиника не найдена");
        }
        String authorId = currentUser.id();
        Review review = reviewRepository.findByClinicAndAuthor(clinicId, authorId).orElseGet(() -> {
            Review created = new Review();
            created.setId(UUID.randomUUID().toString());
            created.setClinicId(clinicId);
            created.setAuthorUserId(authorId);
            return created;
        });
        review.setRating(request.rating());
        review.setText(request.text().trim());
        review.setCreatedAt(Instant.now());
        reviewRepository.save(review);
        return toReview(review, authorId);
    }

    private String applyGeocode(Clinic clinic, String address) {
        try {
            Optional<GeoPoint> point = geocoder.geocode(address);
            if (point.isEmpty()) {
                clinic.setLatitude(null);
                clinic.setLongitude(null);
                return ADDRESS_NOT_FOUND;
            }
            clinic.setLatitude(point.get().latitude());
            clinic.setLongitude(point.get().longitude());
            return null;
        } catch (GeocodingException exception) {
            clinic.setLatitude(null);
            clinic.setLongitude(null);
            return GEOCODER_UNAVAILABLE;
        }
    }

    private boolean hasService(String clinicId, String serviceName) {
        return serviceRepository.findByClinicId(clinicId).stream()
                .anyMatch(service -> service.getName().equals(serviceName));
    }

    private boolean matchesRating(String clinicId, Integer minRating) {
        if (minRating == null) {
            return true;
        }
        Double average = averageRating(clinicId);
        return average != null && average >= minRating;
    }

    private Clinic requireOwnClinic() {
        return clinicRepository.findByOwnerUserId(currentUser.id())
                .orElseThrow(() -> new NotFoundException("Клиника не найдена"));
    }

    private ClinicProfileResponse toProfile(Clinic clinic, String warning) {
        String viewerId = currentUser.id();
        List<Review> reviews = reviewRepository.findByClinicId(clinic.getId());
        return new ClinicProfileResponse(
                clinic.getId(),
                clinic.getName(),
                clinic.getDescription() == null ? "" : clinic.getDescription(),
                clinic.getAddress() == null ? "" : clinic.getAddress(),
                clinic.getLatitude(),
                clinic.getLongitude(),
                averageOf(reviews),
                reviews.size(),
                servicesOf(clinic.getId()),
                reviews.stream()
                        .sorted(Comparator.comparing(Review::getCreatedAt).reversed())
                        .map(review -> toReview(review, viewerId))
                        .toList(),
                warning
        );
    }

    private ClinicMapResponse toMapItem(Clinic clinic) {
        List<Review> reviews = reviewRepository.findByClinicId(clinic.getId());
        return new ClinicMapResponse(
                clinic.getId(),
                clinic.getName(),
                clinic.getAddress(),
                clinic.getLatitude(),
                clinic.getLongitude(),
                averageOf(reviews),
                reviews.size(),
                servicesOf(clinic.getId())
        );
    }

    private List<ServiceResponse> servicesOf(String clinicId) {
        return serviceRepository.findByClinicId(clinicId).stream()
                .sorted(Comparator.comparing(ServiceItem::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toService)
                .toList();
    }

    private ServiceResponse toService(ServiceItem service) {
        return new ServiceResponse(service.getId(), service.getName(), service.getPrice());
    }

    private ReviewResponse toReview(Review review, String viewerId) {
        String authorName = userRepository.findById(review.getAuthorUserId())
                .map(UserAccount::getDisplayName)
                .orElse("Пользователь");
        return new ReviewResponse(
                review.getId(),
                authorName,
                review.getRating(),
                review.getText(),
                review.getCreatedAt(),
                review.getAuthorUserId().equals(viewerId)
        );
    }

    private Double averageRating(String clinicId) {
        return averageOf(reviewRepository.findByClinicId(clinicId));
    }

    private Double averageOf(List<Review> reviews) {
        if (reviews.isEmpty()) {
            return null;
        }
        double average = reviews.stream().mapToInt(Review::getRating).average().orElseThrow();
        return Math.round(average * 10.0) / 10.0;
    }
}

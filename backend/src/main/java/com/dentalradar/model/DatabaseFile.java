package com.dentalradar.model;

import java.util.ArrayList;
import java.util.List;

public class DatabaseFile {

    private List<UserAccount> users = new ArrayList<>();
    private List<Clinic> clinics = new ArrayList<>();
    private List<ServiceItem> services = new ArrayList<>();
    private List<Review> reviews = new ArrayList<>();

    public List<UserAccount> getUsers() {
        return users;
    }

    public void setUsers(List<UserAccount> users) {
        this.users = users;
    }

    public List<Clinic> getClinics() {
        return clinics;
    }

    public void setClinics(List<Clinic> clinics) {
        this.clinics = clinics;
    }

    public List<ServiceItem> getServices() {
        return services;
    }

    public void setServices(List<ServiceItem> services) {
        this.services = services;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }
}

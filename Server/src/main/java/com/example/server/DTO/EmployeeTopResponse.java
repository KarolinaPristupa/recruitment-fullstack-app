package com.example.server.DTO;

public class EmployeeTopResponse {
    private String lastName;
    private String firstName;
    private String position;
    private String photoUrl;
    private long successRate;

    public EmployeeTopResponse(String lastName, String firstName, String position, String photoUrl, long successRate) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.position = position;
        this.photoUrl = photoUrl;
        this.successRate = successRate;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getPosition() {
        return position;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public long getSuccessRate() {
        return successRate;
    }
}
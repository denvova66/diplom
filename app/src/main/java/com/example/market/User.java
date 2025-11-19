package com.example.market;

import java.io.Serializable;

public class User implements Serializable {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String middleName;
    private String avatarUrl;
    private String phoneNumber;

    public User() {}

    public User(String id, String email, String firstName, String lastName, String middleName) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getFullName() {
        StringBuilder fullName = new StringBuilder();
        if (lastName != null && !lastName.isEmpty()) fullName.append(lastName).append(" ");
        if (firstName != null && !firstName.isEmpty()) fullName.append(firstName).append(" ");
        if (middleName != null && !middleName.isEmpty()) fullName.append(middleName);
        return fullName.toString().trim();
    }

    public String getInitials() {
        StringBuilder initials = new StringBuilder();
        if (lastName != null && !lastName.isEmpty()) initials.append(lastName.charAt(0));
        if (firstName != null && !firstName.isEmpty()) initials.append(firstName.charAt(0));
        return initials.toString().toUpperCase();
    }
}
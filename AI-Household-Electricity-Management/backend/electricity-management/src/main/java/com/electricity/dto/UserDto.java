package com.electricity.dto;

public class UserDto {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private String registeredOn;

    public UserDto() {}

    public UserDto(Long id, String fullName, String email, String phone, String role, String registeredOn) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.registeredOn = registeredOn;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getRegisteredOn() { return registeredOn; }
    public void setRegisteredOn(String registeredOn) { this.registeredOn = registeredOn; }
}

package com.electricity.dto;

public class AuthResponse {
    private String status;
    private String message;
    private String token;
    private UserDto user;

    public AuthResponse() {}

    public AuthResponse(String status, String message, String token, UserDto user) {
        this.status = status;
        this.message = message;
        this.token = token;
        this.user = user;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public UserDto getUser() { return user; }
    public void setUser(UserDto user) { this.user = user; }
}

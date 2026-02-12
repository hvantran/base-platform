package com.hoatv.spring.cloud.app.dto;

import java.util.List;

/**
 * Response DTO for user information endpoint
 */
public class UserInfoResponse {
    private String username;
    private String email;
    private String name;
    private List<String> roles;
    private boolean authenticated;

    public UserInfoResponse() {
    }

    public UserInfoResponse(String username, String email, String name, List<String> roles, boolean authenticated) {
        this.username = username;
        this.email = email;
        this.name = name;
        this.roles = roles;
        this.authenticated = authenticated;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }
}

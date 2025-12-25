package auca.ac.rw.Online.quiz.management.controller.dto;

import auca.ac.rw.Online.quiz.management.model.EUserRole;

public class UserCreateRequest {
    private String username;
    private String email;
    private String password;
    private EUserRole role = EUserRole.STUDENT;

    public UserCreateRequest() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public EUserRole getRole() { return role; }
    public void setRole(EUserRole role) { this.role = role; }
}

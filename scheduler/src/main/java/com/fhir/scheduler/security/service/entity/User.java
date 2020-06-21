package com.fhir.scheduler.security.service.entity;

import org.hibernate.annotations.Columns;

import javax.persistence.*;

@Entity

public class User {

    @Id
    private String user_name;

    private String password;
    private boolean active;
    private String roles;

    public User() {

    }

    public User(String user_name, String password, boolean active, String roles) {
        this.user_name = user_name;
        this.password = password;
        this.active = active;
        this.roles = roles;
    }

    public String getUserName() {
        return user_name;
    }

    public void setUserName(String user_name) {
        this.user_name = user_name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }
}

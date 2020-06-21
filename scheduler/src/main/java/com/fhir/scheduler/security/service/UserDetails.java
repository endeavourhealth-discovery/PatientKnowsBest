package com.fhir.scheduler.security.service;

import com.fhir.scheduler.security.service.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class UserDetails implements org.springframework.security.core.userdetails.UserDetails {

private final String userName;
private  final String password;
private final boolean Enabled;
private   List<GrantedAuthority> authorities ;



    public UserDetails(User user) {
        this.userName = user.getUserName();
        this.password = user.getPassword();
        this.Enabled = user.isActive();
        this.authorities = Arrays.stream(user.getRoles().split(",")).map(p->  new SimpleGrantedAuthority(p)).collect(Collectors.toList());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return  authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.userName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {

        return Enabled;
    }
}

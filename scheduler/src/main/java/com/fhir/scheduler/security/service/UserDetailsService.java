package com.fhir.scheduler.security.service;

import com.fhir.scheduler.security.service.entity.User;
import com.fhir.scheduler.security.service.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {


    @Autowired
    UserRepo userRepo;

    @Override

    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {

        User user = userRepo.findByUsername(s);

        if (user == null){
            throw new UsernameNotFoundException("Not Found:"+ s);
        }


        return new com.fhir.scheduler.security.service.UserDetails(user);
    }
}

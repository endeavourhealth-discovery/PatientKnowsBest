package com.fhir.scheduler.security.service.repository;

import com.fhir.scheduler.security.service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.Optional;



public interface UserRepo  extends JpaRepository<User,String> {
  @Query(value="from User where user_name = ?1")
    User findByUsername(String username);
}

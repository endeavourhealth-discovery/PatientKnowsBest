package com.fhir.scheduler.repo;

import com.fhir.scheduler.entity.Available_jobs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Component;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public interface Jobs_repo extends JpaRepository<Available_jobs,String> {


    List<Available_jobs> findAvailable_jobsByStatusFalse();

    @Transactional
    @Modifying
    @Query(value="update available_jobs a set a.status = ? where job_name=?",nativeQuery = true)
    int updateStatus(Boolean flag,String abc);


    @Query(value = "select * from available_jobs  a where a.job_name = ?",nativeQuery = true)
    Available_jobs findAvailable_jobsByJob_name(String job_name);

    @Query(value = "select count(*) from available_jobs  a where a.job_name = ?",nativeQuery = true)
    Integer findByJob_name(String job_name);




}

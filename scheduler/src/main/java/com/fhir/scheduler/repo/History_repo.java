package com.fhir.scheduler.repo;


import com.fhir.scheduler.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Component
public interface History_repo extends JpaRepository<History,Integer> {
   @Query(value = "from History order by job_start_time desc ")
    List<History> getAll();
}









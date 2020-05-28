package com.fhir.scheduler.entity;


import org.springframework.stereotype.Component;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

@Entity

public class History {

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Id
    @GeneratedValue
    private Integer id;

    private String job_name ;

    private String status;

    private  String information;

    private Date job_start_time;

    private Date job_complete_time;

    public Date getJob_start_time() {
        return job_start_time;
    }

    public void setJob_start_time(Date job_start_time) {
        this.job_start_time = job_start_time;
    }

    public Date getJob_complete_time() {
        return job_complete_time;
    }

    public void setJob_complete_time(Date job_complete_time) {
        this.job_complete_time = job_complete_time;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public String getJob_name() {
        return job_name;
    }

    public void setJob_name(String job_name) {
        this.job_name = job_name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

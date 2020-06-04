import { Injectable } from "@angular/core";
import {HttpClient,HttpHeaders,HttpParams } from '@angular/common/http'
import {map,tap}                             from 'rxjs/operators'
import {GetJobs, Job, AvailableJobs, Logs, ConfiguredJobs} from './respose.interfaces'
import {Observable}                         from 'rxjs'




@Injectable()
// This is a singleton class used to provide the data for the ui from rest
export class SchedulerService{

    getJobsUrl = "http://localhost:7080/scheduler/jobs";
    scheduleJobUrl = "http://localhost:7080/scheduler/schedule";
    pauseJobUrl = "http://localhost:7080/scheduler/pause";
    resumeJobUrl = "http://localhost:7080/scheduler/resume";
    deleteJobUrl = "http://localhost:7080/scheduler/delete";
    updateJobUrl = "http://localhost:7080/scheduler/update";
    isJobWithNamePresentUrl = "http://localhost:7080/scheduler/checkJobName";
    stopJobUrl = "http://localhost:7080/scheduler/stop";
    startJobNowUrl = "http://localhost:7080/scheduler/start";
    AvailableJobs = 'http://localhost:7080/scheduler/getAvailableJobs';
    logsUrl = 'http://localhost:7080/scheduler/getLogs';
    addHttpJobUrl = 'http://localhost:7080/scheduler/addHttpJob';
    postClassJobUrl = 'http://localhost:7080/scheduler/addClassJob';
    configuredJobsUrl ='http://localhost:7080/scheduler/getConfiguredJobs';
    deleteConfiguredJobUrl = "http://localhost:7080/scheduler/deleteConfiguredJob"

    constructor(private _http: HttpClient) {
    }

    isJobWithNamePresent(jobName) : Observable<Job>{
        console.log(jobName);
        const params = new HttpParams().append("jobName", jobName.jobName)


        return this._http.get<Job>(this.isJobWithNamePresentUrl, {params:params,observe:'response'}).pipe(
            tap(response => console.log(response)),
        map(response => response.body));
       }




       getJobs():Observable<GetJobs>{
        return this._http.get<GetJobs>(this.getJobsUrl);
    }



    scheduleJob(data):Observable<GetJobs>{
        let params = new HttpParams();
     for(let key in data) {
           params =  params.append(key, data[key]);
        }


        console.log(params);


        return this._http.get<GetJobs>(this.scheduleJobUrl, {params : params});
    }



    startJobNow(data):Observable<Job>{
        const params = new HttpParams().append("jobName", data.jobName)
        return this._http.get<Job>(this.startJobNowUrl, {params:params});
    }





    pauseJob(data):Observable<Job>{
        const params = new HttpParams().append("jobName", data.jobName)
        return this._http.get<Job>(this.pauseJobUrl,{params:params})
    }


    deleteJob(data):Observable<Job>{
        const params = new HttpParams().append("jobName", data.jobName)

        return this._http.get<Job>(this.deleteJobUrl,{params:params});
    }


    updateJob(data):Observable<GetJobs>{
        let params = new HttpParams();
        for(let key in data) {
              params =  params.append(key, data[key]);
           }


        return this._http.get<GetJobs>(this.updateJobUrl,{params : params})

    }
    resumeJob(data):Observable<Job>{

        const params = new HttpParams().append("jobName", data.jobName)


        return this._http.get<Job>(this.resumeJobUrl,{params : params} )

    }



    stopJob(data):Observable<Job>{
        const params = new HttpParams().append("jobName", data.jobName)

        return this._http.get<Job>(this.stopJobUrl,{params : params});

    }


    getAvailableJobs():Observable<AvailableJobs>{
        return this._http.get<AvailableJobs>(this.AvailableJobs,{observe:'response'}).pipe(tap(Response=>console.log(Response)),map(response=>response.body));

    }

getLogs():Observable<Logs>{
      return this._http.get<Logs>(this.logsUrl);
}

 postHttpJob(httpJob):Observable<Job>{


      return this._http.post<Job>(this.addHttpJobUrl,httpJob)
 }


 postClassJob(classJob):Observable<Job>{
   return this._http.post<Job>(this.postClassJobUrl,classJob)
 }


 getConfiguredJobs():Observable<ConfiguredJobs>{
      return this._http.get<ConfiguredJobs>(this.configuredJobsUrl);
 }

 deleteConfiguredJob(jobName):Observable<ConfiguredJobs>{
      let params = new HttpParams().append('jobName',jobName)

      return this._http.delete<ConfiguredJobs>(this.deleteConfiguredJobUrl,{params:params})
 }
}

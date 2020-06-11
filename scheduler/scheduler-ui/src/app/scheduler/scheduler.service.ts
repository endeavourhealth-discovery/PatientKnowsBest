import { Injectable } from "@angular/core";
import {HttpClient,HttpHeaders,HttpParams } from '@angular/common/http'
import {map,tap}                             from 'rxjs/operators'
import {GetJobs, Job, AvailableJobs, Logs, ConfiguredJobs} from './respose.interfaces'
import {Observable}                         from 'rxjs'

@Injectable()
// This is a singleton class used to provide the data for the ui from rest
export class SchedulerService{
    getJobsUrl = "http://10.1.101.59:8081/scheduler/jobs";
    scheduleJobUrl = "http://10.1.101.59:8081/scheduler/schedule";
    pauseJobUrl = "http://10.1.101.59:8081/scheduler/pause";
    resumeJobUrl = "http://10.1.101.59:8081/scheduler/resume";
    deleteJobUrl = "http://10.1.101.59:8081/scheduler/delete";
    updateJobUrl = "http://10.1.101.59:8081/scheduler/update";
    isJobWithNamePresentUrl = "http://10.1.101.59:8081/scheduler/checkJobName";
    stopJobUrl = "http://10.1.101.59:8081/scheduler/stop";
    startJobNowUrl = "http://10.1.101.59:8081/scheduler/start";
    AvailableJobs = 'http://10.1.101.59:8081/scheduler/getAvailableJobs';
    logsUrl = 'http://10.1.101.59:8081/scheduler/getLogs';
    addHttpJobUrl = 'http://10.1.101.59:8081/scheduler/addHttpJob';
    postClassJobUrl = 'http://10.1.101.59:8081/scheduler/addClassJob';
    configuredJobsUrl ='http://10.1.101.59:8081/scheduler/getConfiguredJobs';
    deleteConfiguredJobUrl = "http://10.1.101.59:8081/scheduler/deleteConfiguredJob";
    updateHttpJobUrl = "http://10.1.101.59:8081/scheduler/updateHttpJob";
    updateClassJobUrl = "http://10.1.101.59:8081/scheduler/updateClassJob";


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

 updateHttpJob(jobData:{startUrl: String, stopUrl:String, jobName:String}):Observable<Job>{
      return this._http.put<Job>(this.updateHttpJobUrl,jobData);
 }

  updateClassJob(jobData):Observable<Job>{
    return this._http.put<Job>(this.updateClassJobUrl,jobData);
  }


}

import { Component, OnInit } from '@angular/core';
import {SchedulerService} from "../scheduler/scheduler.service";
import {ServerResponseCode}   from '../scheduler/response.code.constants'
import {LogDetails} from "../scheduler/respose.interfaces";

@Component({
  selector: 'logtable',
  templateUrl: './logtable.component.html',
  styleUrls: ['./logtable.component.css']
})
export class LogtableComponent implements OnInit {
  displayedColumns: String [] = ["id","job_name","status","information","job_start_time","job_complete_time"]
  logData : LogDetails [] ;

  constructor(private _schedulerservice:SchedulerService) { }

  ngOnInit(): void {
    this.getLog();
  }

  getLog(){
    this._schedulerservice.getLogs().subscribe(success=>{
      if (success.statusCode == ServerResponseCode.SUCCESS ){
        this.logData = success.data;
      }
    })
  }

  onSubmit(){
   this.getLog();
  }

}

import {Component, OnInit, ViewChild} from '@angular/core';
import {SchedulerService} from "../scheduler/scheduler.service";
import {ServerResponseCode}   from '../scheduler/response.code.constants'
import {LogDetails} from "../scheduler/respose.interfaces";
import {MatPaginator} from "@angular/material/paginator";
import {MatTableDataSource} from "@angular/material/table";


@Component({
  selector: 'logtable',
  templateUrl: './logtable.component.html',
  styleUrls: ['./logtable.component.css']
})
export class LogtableComponent implements OnInit {
  displayedColumns: String [] = ["id","job_name","status","information","job_start_time","job_complete_time"]
  logData = new MatTableDataSource<LogDetails>();
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private _schedulerservice:SchedulerService) { }

  ngOnInit(): void {
    this.logData.paginator=this.paginator;
    this.getLog();
  }

  getLog(){
    this._schedulerservice.getLogs().subscribe(success=>{
      if (success.statusCode == ServerResponseCode.SUCCESS ){
        this.logData.data = success.data;
      }
    })
  }

  onSubmit(){
   this.getLog();
  }

  onClick(value){
    console.log(value)
  }

}

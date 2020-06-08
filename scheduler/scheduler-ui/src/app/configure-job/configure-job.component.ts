import {Component, OnInit, ViewChild} from '@angular/core';
import {NewJobComponent} from "../new-job/new-job.component";
import {MatDialog} from "@angular/material/dialog";
import {SchedulerService} from "../scheduler/scheduler.service";
import {MatTableDataSource} from "@angular/material/table";
import {ConfiguredJob, ConfiguredJobs} from "../scheduler/respose.interfaces";
import {ServerResponseCode} from "../scheduler/response.code.constants";
import {MatPaginator} from "@angular/material/paginator";
import {HttpParams} from "@angular/common/http";
import {Observable} from "rxjs";

@Component({
  selector: 'app-configure-job',
  templateUrl: './configure-job.component.html',
  styleUrls: ['./configure-job.component.css']
})
export class ConfigureJobComponent implements OnInit {

  dataSource = new MatTableDataSource<any>() ;

  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  displayedColumns = ['job_name','class_name','start_method','stop_method','parameters','start_url','stop_url','Edit']



  constructor(public dialog: MatDialog , private _service : SchedulerService) { }

  ngOnInit(): void {

this.dataSource.paginator  = this.paginator;
    this.getConfiguredJobs();

  }

  openDialog(): void {
    const dialogRef = this.dialog.open(NewJobComponent, {


    });

    dialogRef.afterClosed().subscribe(result => {
       this.getConfiguredJobs();

    });
  }

  getConfiguredJobs(){
    this._service.getConfiguredJobs().subscribe(response=>{
      if(response.statusCode == ServerResponseCode.SUCCESS){
        this.dataSource.data = response.data;
      console.log(response)
      }
    })
  }

  delete(jobName){
    console.log(jobName)
    this._service.deleteConfiguredJob(jobName).subscribe(response=>{
      if(response.statusCode == ServerResponseCode.SUCCESS){
        this.dataSource.data = response.data;
      }
    })


  }


  onUpdate(jobDetails:ConfiguredJob){
   const dialogRef = this.dialog.open(NewJobComponent,{data:jobDetails})

    dialogRef.afterClosed().subscribe(result => {
      this.getConfiguredJobs();

    });

  }


}

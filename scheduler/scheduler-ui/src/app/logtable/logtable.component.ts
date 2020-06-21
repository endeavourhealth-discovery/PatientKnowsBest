import {Component, OnInit, ViewChild} from '@angular/core';
import {SchedulerService} from "../scheduler/scheduler.service";
import {ServerResponseCode}   from '../scheduler/response.code.constants'
import {LogDetails} from "../scheduler/respose.interfaces";
import {MatPaginator} from "@angular/material/paginator";
import {MatTableDataSource} from "@angular/material/table";
import {Observable} from "rxjs";
import {MatSort} from "@angular/material/sort";
import {MatSnackBar} from "@angular/material/snack-bar";


@Component({
  selector: 'logtable',
  templateUrl: './logtable.component.html',
  styleUrls: ['./logtable.component.css']
})
export class LogtableComponent implements OnInit {
  displayedColumns: String [] = ["job_name", "status", "information", "job_start_time", "job_complete_time"]
  logData = new MatTableDataSource<LogDetails>();
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

   isSpin :boolean = false ;
  @ViewChild(MatSort, {static: true}) sort: MatSort
  chartData: { Success: number, Failed: number, Interrupted: number };

  constructor(private _schedulerservice: SchedulerService,private snackBar : MatSnackBar) {
  }

  ngOnInit(): void {
    this.logData.sort = this.sort;
    this.logData.paginator = this.paginator;
    this.getLog();
    this.logData.filterPredicate =(data:LogDetails,filter:string)=>{
      if(filter=='all'){
        return true;
      }
      else if (filter.toLowerCase().trim() == data.status.toLowerCase().trim()){

        return true;
      }
      else{
        return  false;
      }
    }
  }



  getLog() {
    this._schedulerservice.getLogs().subscribe(success => {
      if (success.statusCode == ServerResponseCode.SUCCESS) {
        this.logData.data = success.data;
        this.reduce(success.data);

      }
      this.isSpin = false ;
    },error => {
      this.snackBar.open("Unknown Error Occurred","Okay",{duration:1000});
      this.isSpin=false;
    })
  }

  onSubmit() {
    this.isSpin = true ;
    this.getLog();
  }

  reduce(logDetails: LogDetails []) {
    let list: { Success: number, Failed: number, Interrupted: number,	InterruptFailed:number } = {Success: 0, Failed: 0, Interrupted: 0,	InterruptFailed:0}


    logDetails.forEach(i=>{
     if (i.status == 'Failed'){

       list.Failed ++;

     }else if(i.status == 'Interrupted'){
       list.Interrupted ++;
     }else if(i.status == 'Success'){

       list.Success++;
     }else if(i.status == 'Interrupt failed'){
       list.InterruptFailed ++;
     }
    })
    this.chartData = list;
  }


  onChange(value) {
    console.log(value)
    this.logData.filter = value.value;
    this.paginator.firstPage();

  }
}

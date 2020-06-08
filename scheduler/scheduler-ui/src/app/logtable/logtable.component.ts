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
tableData : LogDetails[];

  chartData :  {Success : number ,Failed :number , Interrupted: number};

  constructor(private _schedulerservice:SchedulerService) { }

  ngOnInit(): void {
    this.logData.paginator=this.paginator;
    this.getLog();

  }

  getLog(){
    this._schedulerservice.getLogs().subscribe(success=>{
      if (success.statusCode == ServerResponseCode.SUCCESS ){
        this.logData.data = success.data;
   this.tableData = success.data;
        this.reduce(success.data);
      }
    })
  }

  onSubmit(){
   this.getLog();
  }

  reduce(logDetails : LogDetails []){
   let list : {Success : number ,Failed :number , Interrupted: number} = {Success : 0 , Failed : 0 , Interrupted : 0  }


    logDetails.forEach(i=>{
     if (i.status == 'Failed'){

       list.Failed ++;

     }else if(i.status == 'Interrupted'){
       list.Interrupted ++;
     }else{

       list.Success++;
     }
    })
    this.chartData = list;
  }


  onClick(value){
    console.log(value)
  }

}

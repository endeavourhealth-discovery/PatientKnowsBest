import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {NgForm} from "@angular/forms";
import {SchedulerService} from "../scheduler/scheduler.service";
import {ServerResponseCode} from "../scheduler/response.code.constants";
import {ConfiguredJob} from "../scheduler/respose.interfaces";



@Component({
  selector: 'app-new-job',
  templateUrl: './new-job.component.html',
  styleUrls: ['./new-job.component.css']
})
export class NewJobComponent implements OnInit {

  success : boolean = false;

  failure : boolean = false;

  isUpdate: boolean = false;

  typeOfJob : Boolean = false;


  @ViewChild('jobForm') newJobForm : NgForm;


  constructor( public dialogRef: MatDialogRef<NewJobComponent>,
               @Inject(MAT_DIALOG_DATA) public data:ConfiguredJob,
               private _service : SchedulerService,
            ) {
  }



  ngOnInit(): void {
if (this.data != null){
  this.isUpdate = true
  this.newJobForm.form.patchValue({
    'jobName' : this.data.job_name



  })
}
  }



  onSubmit(){

    this.success = false;
    this.failure = false ;
if(this.typeOfJob==false) {
//Class Type
let httpGroup:{jobName:String , starturl:String ,stopurl :String  } = this.newJobForm.controls['urlGroup'].value
console.log(httpGroup)
this._service.postHttpJob(httpGroup).subscribe(result=>{
  if(result.statusCode==ServerResponseCode.SUCCESS){
          this.success=true;

  }else if (result.statusCode==ServerResponseCode.JOB_WITH_SAME_NAME_EXIST){
  this.failure = true;

  }
})



}
else{
 this._service.postClassJob(this.newJobForm.controls['classGroup'].value).subscribe(result=>{
   if(result.statusCode==ServerResponseCode.SUCCESS){
     this.success=true;

   }else if (result.statusCode==ServerResponseCode.JOB_WITH_SAME_NAME_EXIST){
     this.failure = true;

   }
 })
}

  }


  onUpdate(){

  }



  close(){
    this.newJobForm.resetForm();
    this.success = false;
    this.failure = false ;
    this.dialogRef.close();

  }

}

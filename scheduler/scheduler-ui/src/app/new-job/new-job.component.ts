import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {FormControl, FormGroup, NgForm, NgModelGroup, Validators} from "@angular/forms";
import {SchedulerService} from "../scheduler/scheduler.service";
import {ServerResponseCode} from "../scheduler/response.code.constants";
import {ConfiguredJob} from "../scheduler/respose.interfaces";
import {MatSnackBar} from "@angular/material/snack-bar";


@Component({
  selector: 'app-new-job',
  templateUrl: './new-job.component.html',
  styleUrls: ['./new-job.component.css']
})
export class NewJobComponent implements OnInit {

  success: boolean = false;

  failure: boolean = false;

  isUpdate: boolean = false;

  typeOfJob: Boolean = true;

  updateSuccess : Boolean;

  updateFailure : Boolean;

  updateForm: FormGroup;

  message: Boolean = false;

  successMessage :String ;

  failureMessage : String ;




  @ViewChild('jobForm', {static: true}) newJobForm: NgForm;
  @ViewChild('f', {static: true}) urlGroup: NgModelGroup;

  constructor(public dialogRef: MatDialogRef<NewJobComponent>,
              @Inject(MAT_DIALOG_DATA) public data: ConfiguredJob,
              private _service: SchedulerService,
              private snackBar: MatSnackBar,

  ) {
  }


  ngOnInit(): void {
    if (this.data != null) {
      this.isUpdate = true
      if(this.data.start_url != null){
          this.typeOfJob = false;
      }
      else if(this.data.class_path !=null) {
        this.typeOfJob = true;
      }
      this.updateForm = new FormGroup({
        'urlGroup': new FormGroup({
          'jobName': new FormControl({value: this.data.job_name, disabled: true}, Validators.required),
          'startUrl': new FormControl(this.data.start_url, Validators.required),
          'stopUrl': new FormControl(this.data.stop_url, Validators.required)

        }),

        'classGroup': new FormGroup({
          'jobName': new FormControl({value: this.data.job_name, disabled: true}, Validators.required),
          'startMethod': new FormControl(this.data.start_method, Validators.required),
          'stopMethod': new FormControl(this.data.stop_method, Validators.required),
          'classPath': new FormControl(this.data.class_path, Validators.required),
          'parameters': new FormControl(this.data.parameters)
        })

      })


      // this.updateForm.reset();
    }
  }


  onSubmit() {

    this.success = false;
    this.failure = false;
    if (this.typeOfJob == false) {
//Class Type
      let httpGroup: { jobName: String, starturl: String, stopurl: String } = this.newJobForm.controls['urlGroup'].value
      console.log(httpGroup)
      this._service.postHttpJob(httpGroup).subscribe(result => {
        if (result.statusCode == ServerResponseCode.SUCCESS) {
          this.success = true;

        } else if (result.statusCode == ServerResponseCode.JOB_WITH_SAME_NAME_EXIST) {
          this.failure = true;

        }
      })


    } else {
      this._service.postClassJob(this.newJobForm.controls['classGroup'].value).subscribe(result => {
        if (result.statusCode == ServerResponseCode.SUCCESS) {
          this.success = true;

        } else if (result.statusCode == ServerResponseCode.JOB_WITH_SAME_NAME_EXIST) {
          this.failure = true;

        }
      })
    }

  }


  onUpdate() {

    if (this.typeOfJob == false) {
      if (this.updateForm.controls['urlGroup'].pristine) {
        this.message = true;
      } else if (!this.updateForm.controls['urlGroup'].pristine) {
        this.message = false;

        let jobData = this.updateForm.controls['urlGroup'].value;
        jobData.jobName = this.updateForm.controls['urlGroup'].get("jobName").value;

        this._service.updateHttpJob(jobData).subscribe(response => {
          if (response.statusCode == ServerResponseCode.SUCCESS) {
            console.log("SuccessFully Updated")
            this.successMessage = "SUCCESS : Job Updated Successfully Please Make Sure Web Service Available ";
            this.updateSuccess = true ;
            this.updateFailure = false;

          } else if (response.statusCode == ServerResponseCode.ERROR) {
            this.updateSuccess = false;
              this.updateFailure = true;
            this.failureMessage = "ERROR : Failed To Update The Job ";

          }
        }, error => {
          this.snackBar.open(error.statusText + " Occurred Unable to Update Jobs", "Okay", {duration: 2000});
          console.log();
        })
      }


    }
    else if(this.typeOfJob == true ){
      if (this.updateForm.controls['classGroup'].pristine) {
        this.message = true;
      }else if(!this.updateForm.controls['classGroup'].pristine){
        this.message = false ;
        let jobData  = this.updateForm.controls['classGroup'].value;
        jobData.jobName = this.updateForm.controls['classGroup'].get("jobName").value;

        this._service.updateClassJob(jobData).subscribe(response => {
          if (response.statusCode == ServerResponseCode.SUCCESS) {
            this.successMessage = "SUCCESS : Job Updated Successfully Please Make Sure Jar Is In Class Path ";
            this.updateSuccess = true ;
            this.updateFailure = false;
          } else if (response.statusCode == ServerResponseCode.ERROR) {
            this.failureMessage = "ERROR : Failed To Update The Job ";
            this.updateFailure = true;
            this.updateSuccess = false;

          }
        }, error => {
          this.snackBar.open(error.statusText + " Occurred Unable to Update Jobs", "Okay", {duration: 2000});
          console.log();
        })


      }
    }


  }


  close() {
    this.newJobForm.resetForm();
    this.success = false;
    this.failure = false;
    this.dialogRef.close();

  }

}

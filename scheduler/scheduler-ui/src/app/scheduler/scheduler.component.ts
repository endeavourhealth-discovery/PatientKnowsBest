import { Component, OnInit,OnDestroy } from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import { Observable, Subscription ,timer}                     from 'rxjs';
import {SchedulerService}     from './scheduler.service';
import {ServerResponseCode}   from './response.code.constants'
import { AvailableJobs, Job_Details } from './respose.interfaces';
import {MatRadioButton} from '@angular/material/radio';
import {MatSnackBar}  from '@angular/material/snack-bar';
import {MatDialog} from "@angular/material/dialog";
import {NewJobComponent} from "../new-job/new-job.component";


@Component({
  selector: 'app-scheduler',
  templateUrl: 'scheduler.component.html',
  styleUrls: ['./scheduler.component.css']
})
export class SchedulerComponent implements OnInit,OnDestroy {
// injecting the formBuilder service

//Used to pause the getAvalilable jobs when job is selected
isNotPause:boolean = true;
//It is for the form group
schedulerForm: FormGroup;
// to check the name exists or not
jobNameStatus: String ;
// it is set to true if it is in edit mode
isEditMode:boolean = false;
// used for the date
date : Date ;
// timer subscriptions to get the jobs
jobRefreshTimerSubscription: Subscription;

// getJobsTimerSubsciption : Subscription;
// job records all the jobs will be assigned to this
jobRecords = [];
// All the available jobs will be assigned to this
availableJobs:Job_Details[] ;

// used ti get the edit job name
editJobName :String;
// used to toggle between the cron job and normal job
jobType:boolean=true;

  constructor(private _formBuilder:FormBuilder,
    private _schedulerService:SchedulerService,
    private _responseCode : ServerResponseCode,
    private  _matSnackBar:MatSnackBar,
              ) {

   this.jobNameStatus='';

   this.date = new Date();
   this.getAvailableJobs();

  }

  ngOnInit(): void {

    this.schedulerForm = this._formBuilder.group({
      jobName:[''],
      year: ['',[Validators.maxLength(4),Validators.minLength(4),Validators.required,Validators.pattern("^(19|20)\\d{2}$")]],
      month: ['',[Validators.maxLength(2),Validators.minLength(1),Validators.required,Validators.pattern("^(0?[1-9]|1[012])$")]],
      day: ['',[Validators.required,Validators.maxLength(2),Validators.minLength(1)]],
      hour: ['',[Validators.minLength(1),Validators.maxLength(2),Validators.pattern("^([0-1]?[0-9]|2[0-3])"),Validators.required]],
      minute: ['',[Validators.minLength(1),Validators.maxLength(2),Validators.pattern("([0-5]?\\d)"),Validators.required]],
      cronExpression: ['0 0/10 * 1/1 * ? *',[Validators.pattern("^\\s*($|#|\\w+\\s*=|(\\?|\\*|(?:[0-5]?\\d)(?:(?:-|\/|\\,)(?:[0-5]?\\d))?(?:,(?:[0-5]?\\d)(?:(?:-|\/|\\,)(?:[0-5]?\\d))?)*)\\s+(\\?|\\*|(?:[0-5]?\\d)(?:(?:-|\/|\\,)(?:[0-5]?\\d))?(?:,(?:[0-5]?\\d)(?:(?:-|\/|\\,)(?:[0-5]?\\d))?)*)\\s+(\\?|\\*|(?:[01]?\\d|2[0-3])(?:(?:-|\/|\\,)(?:[01]?\\d|2[0-3]))?(?:,(?:[01]?\\d|2[0-3])(?:(?:-|\/|\\,)(?:[01]?\\d|2[0-3]))?)*)\\s+(\\?|\\*|(?:0?[1-9]|[12]\\d|3[01])(?:(?:-|\/|\\,)(?:0?[1-9]|[12]\\d|3[01]))?(?:,(?:0?[1-9]|[12]\\d|3[01])(?:(?:-|\/|\\,)(?:0?[1-9]|[12]\\d|3[01]))?)*)\\s+(\\?|\\*|(?:[1-9]|1[012])(?:(?:-|\/|\\,)(?:[1-9]|1[012]))?(?:L|W)?(?:,(?:[1-9]|1[012])(?:(?:-|\/|\\,)(?:[1-9]|1[012]))?(?:L|W)?)*|\\?|\\*|(?:JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)(?:(?:-)(?:JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC))?(?:,(?:JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)(?:(?:-)(?:JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC))?)*)\\s+(\\?|\\*|(?:[0-6])(?:(?:-|\/|\\,|#)(?:[0-6]))?(?:L)?(?:,(?:[0-6])(?:(?:-|\/|\\,|#)(?:[0-6]))?(?:L)?)*|\\?|\\*|(?:MON|TUE|WED|THU|FRI|SAT|SUN)(?:(?:-)(?:MON|TUE|WED|THU|FRI|SAT|SUN))?(?:,(?:MON|TUE|WED|THU|FRI|SAT|SUN)(?:(?:-)(?:MON|TUE|WED|THU|FRI|SAT|SUN))?)*)(|\\s)+(\\?|\\*|(?:|\\d{4})(?:(?:-|\/|\\,)(?:|\\d{4}))?(?:,(?:|\\d{4})(?:(?:-|\/|\\,)(?:|\\d{4}))?)*))$"
      ),Validators.required]]
    });
    this.setDate();
    this.getAvailableJobs();
    let timer_value =timer(2000,3000);

    this.jobRefreshTimerSubscription = timer_value.subscribe(t=>{
      this.getJobs();

    });

    setInterval(()=>{
      if(this.isNotPause){
      this.getAvailableJobs()
      }
    },10*1000);
  }


  ngOnDestroy() {
    this.jobRefreshTimerSubscription.unsubscribe();
  }




/***Gets all the schduled jobs  */
  getJobs(){
    {
      this._schedulerService.getJobs().subscribe(
        success => {
            if(success.statusCode == ServerResponseCode.SUCCESS){
               this.jobRecords=success.data;

            }else{
              alert("Some error while fetching jobs");
            }
        },
        err => {
          this._matSnackBar.open("Error while getting all jobs",'Okay',{duration : 1000,verticalPosition:'bottom',  panelClass: 'blue-snackbar'})
        });
    }


  }

  checkJobExistWith(jobName){
    var data = {
      "jobName": jobName
    }

      this._schedulerService.isJobWithNamePresent(data).subscribe(
        success => {
            if(success.statusCode == ServerResponseCode.SUCCESS){
              if(success.data == true){
                this.jobNameStatus = "Bad :(";
              }else{
                this.jobNameStatus = "Good :)";
              }
            }else if(success.statusCode == ServerResponseCode.JOB_NAME_NOT_PRESENT){
              alert("Job name is mandatory.");
              this.schedulerForm.patchValue({
                jobName: "",
              });
            }
        },
        err => {
          alert("Error while checking job with name exist.");
        });
        this.jobNameStatus = "";
    }



    getFormattedDate(year, month, day, hour, minute) {
      return year + "/" + month + "/" + day + " " + hour+":"+minute;
    }

    resetForm(){
      var dateNow = new Date();
      this.schedulerForm.patchValue({
          jobName: "",
          year: dateNow.getFullYear(),
          month: dateNow.getMonth() + 1,
          day: dateNow.getDate(),
          hour: dateNow.getHours(),
          minute: dateNow.getMinutes()
        });
      this.jobNameStatus = "";
      this.isEditMode=false;
      this.isNotPause = true;
    }


 scheduleJob(){
      var jobName = this.schedulerForm.value.jobName;
      var year = this.schedulerForm.value.year;
      var month = this.schedulerForm.value.month;
      var day = this.schedulerForm.value.day;
      var hour = this.schedulerForm.value.hour;
      var minute = this.schedulerForm.value.minute;

      var data = {
        "jobName": this.schedulerForm.value.jobName,
        "jobScheduleTime": this.getFormattedDate(year, month, day, hour, minute),
        "cronExpression": this.schedulerForm.value.cronExpression,
      }

      this._schedulerService.scheduleJob(data).subscribe(
        success => {
            if(success.statusCode == ServerResponseCode.SUCCESS){
              this._matSnackBar.open("JOb Scheduled Successsfully",'Okay',{duration : 2000,verticalPosition:'bottom'})
              this.jobRecords = success.data;
              this.resetForm();
              this.getAvailableJobs();

            }else if(success.statusCode == ServerResponseCode.JOB_WITH_SAME_NAME_EXIST){
              this._matSnackBar.open("Job with same name exists, Please choose different name.","Okay",{duration:2000});

            }else if(success.statusCode == ServerResponseCode.JOB_NAME_NOT_PRESENT){
              this._matSnackBar.open("Please select a valid Job ","Okay",{duration:2000});
            }else if (success.statusCode == ServerResponseCode.JOB_DETAILS_UNKNOWN){
              this._matSnackBar.open("Please verify the job details ","Okay",{duration:2000});
            }else if(success.statusCode == ServerResponseCode.TIME_ERROR){
              this._matSnackBar.open("Please Enter A Valid Time  ","Okay",{duration:2000});
            }

            this.isNotPause = true;
        },
        err => {
          this._matSnackBar.open("Error while getting all the jobs ","Okay",{duration:2000});
        });
    }


  setDate(){
     this.schedulerForm.patchValue({
          year:this.date.getFullYear(),
          month: this.date.getMonth() + 1,
          day: this.date.getDate(),
          hour: this.date.getHours(),
          minute: this.date.getMinutes()
        })

     }

// This will be used when ever the radio buttons are toggled
resetCron(){
  if (this.jobType==true){
this.schedulerForm.patchValue({
  cronExpression:'0 0/10 * 1/1 * ? *'
})

  }else{
    this.schedulerForm.patchValue({
      cronExpression:''
    })


  }
  // console.log('This is the current corn expression ' +this.schedulerForm.value.cronExpression);
  // this.resetForm();
     }


     updateJob(){
      var jobName = this.schedulerForm.value.jobName;
      var year = this.schedulerForm.value.year;
      var month = this.schedulerForm.value.month;
      var day = this.schedulerForm.value.day;
      var hour = this.schedulerForm.value.hour;
      var minute = this.schedulerForm.value.minute;

      var data = {
        "jobName": this.schedulerForm.value.jobName,
        "jobScheduleTime": this.getFormattedDate(year, month, day, hour, minute),
        "cronExpression": this.schedulerForm.value.cronExpression
      }

      this._schedulerService.updateJob(data).subscribe(
        success => {
            if(success.statusCode == ServerResponseCode.SUCCESS){
              this._matSnackBar.open("Job updated successfully.","Okay",{duration:2000});
              this.resetForm();

            }else if(success.statusCode == ServerResponseCode.JOB_DOESNT_EXIST){
              this._matSnackBar.open("Job no longer exist.","Okay",{duration:2000});

            }else if(success.statusCode == ServerResponseCode.JOB_NAME_NOT_PRESENT){
              this._matSnackBar.open("Please provide job name.","Okay",{duration:2000});
            }else if(success.statusCode==ServerResponseCode.TIME_ERROR){
              this._matSnackBar.open("Please Enter Valid Time To Update","Okay",{duration:2000});
            }
            this.jobRecords = success.data;
            this.getAvailableJobs();
        },
        err => {
          alert("Error while updating job");
        });
    }

    cancelEdit(){
      this.resetForm();
    }

  // whenever the cron expression changes
    cronChange(cronExp){
      this.schedulerForm.patchValue({
          cronExpression: cronExp
        });
    }


    // Scheduled time will be appeared in the for
    editJob(selectedJobRow){
      this.isEditMode = true;

      var d = Date.parse(selectedJobRow.scheduleTime);
      let date = new Date(selectedJobRow.scheduleTime);
      this.schedulerForm.patchValue({
          jobName: selectedJobRow.jobName,
          year: date.getFullYear(),
          month: date.getMonth() + 1,
          day: date.getDate(),
          hour: date.getHours(),
          minute: date.getMinutes()
        });
        this.editJobName = selectedJobRow.jobName;
    }





    stopJob(jobName){
      var data = {
        "jobName": jobName
      }
      this._schedulerService.stopJob(data).subscribe(
        success => {
          if(success.statusCode == ServerResponseCode.SUCCESS && success.data == true){

            this._matSnackBar.open("Job Stoppped Successfully",'Okay',{duration : 1000,verticalPosition:'bottom'})
            this.getAvailableJobs();

          }else if(success.data == false){
            if(success.statusCode == ServerResponseCode.JOB_NOT_IN_RUNNING_STATE){
              alert("Job not started, so cannot be stopped.");

            }else if(success.statusCode == ServerResponseCode.JOB_ALREADY_IN_RUNNING_STATE){
              alert("Job already started.");

            }else if(success.statusCode == ServerResponseCode.JOB_DOESNT_EXIST){
              alert("Job no longer exist.");
            }
          }

          //For updating fresh status of all jobs
          this.getJobs();
        },
        err => {
          alert("Error while pausing job");
        });
  }



    resumeJob(jobName){
      var data = {
        "jobName": jobName
      }
     this._schedulerService.resumeJob(data).subscribe(
      success => {
        if(success.statusCode == ServerResponseCode.SUCCESS && success.data == true){
            alert("Job resumed successfully.");


          }else if(success.data == false){
            if(success.statusCode == ServerResponseCode.JOB_NOT_IN_PAUSED_STATE){
                alert("Job is not in paused state, so cannot be resumed.");
            }
          }

          //For updating fresh status of all jobs
          this.getJobs();
          this.getAvailableJobs();
      },
      err => {
        alert("Error while resuming job");
      });

      //For updating fresh status of all jobs
      this.getJobs();
  }


    pauseJob(jobName){
      var data = {
        "jobName": jobName
      }
      this._schedulerService.pauseJob(data).subscribe(
        success => {
          if(success.statusCode == ServerResponseCode.SUCCESS && success.data == true){
            alert("Job paused successfully.")

          }else if(success.data == false){
            if(success.statusCode == ServerResponseCode.JOB_ALREADY_IN_RUNNING_STATE){
                alert("Job already started/completed, so cannot be paused.");
            }
          }
          this.getJobs();
        },
        err => {
          alert("Error while pausing job");
        });

      //For updating fresh status of all jobs
      this.getJobs();
  }



  // Starts the current job based on the current job name
    startJobNow(jobName){
      var data = {
        "jobName": jobName
      }
      this._schedulerService.startJobNow(data).subscribe(
        success => {
          if(success.statusCode == ServerResponseCode.SUCCESS && success.data == true){
            alert("Job started successfully.")

          }else if(success.data == false){
            if(success.statusCode == ServerResponseCode.ERROR){
                alert("Server error while starting job.");

            }else if(success.statusCode == ServerResponseCode.JOB_ALREADY_IN_RUNNING_STATE){
              alert("Job is already started.");

            }else if(success.statusCode == ServerResponseCode.JOB_DOESNT_EXIST){
              alert("Job no longer exist.");
            }
          }


        },
        err => {
          alert("Error while starting job now.");
        });

      //For updating fresh status of all jobs
      this.getJobs();
  }


/**Deletes the  selected job  */
  deleteJob(jobName){
    var data = {
      "jobName": jobName
    }
    this._schedulerService.deleteJob(data).subscribe(
      success => {
          if(success.statusCode == ServerResponseCode.SUCCESS && success.data == true){
            alert("Job deleted successfully.");

          }else if(success.data == false){
            if(success.statusCode == ServerResponseCode.JOB_ALREADY_IN_RUNNING_STATE){
                alert("Job is already started/completed, so cannot be deleted.");

            }else if(success.statusCode == ServerResponseCode.JOB_DOESNT_EXIST){
              alert("Job no longer exist.");
            }

          }

          //For updating fresh status of all jobs
          this.getJobs();
          this.getAvailableJobs();
          this.resetForm();

      },
      err => {
        alert("Error while deleting job");
      });
  }


/**This will get all the available jobs from the database  */
  getAvailableJobs(){

    this._schedulerService.getAvailableJobs().subscribe(success=>{
      if (success.statusCode==ServerResponseCode.SUCCESS){
     this.availableJobs = success.data
    //  console.log(this.availableJobs)
      }else{
        alert('Unable to get the available Jobs ');
      }
      console.log(this.availableJobs)

    })

   }


  //  getAllJobs():String[]{

  //         var jobs:String []=[] ;

  //       this.availableJobs.forEach(element => {
  //         if (!element.status){
  //         jobs.push(element.job_name);

  //         }
  //       });

  //       return jobs;

  //       }

  /***When ever the job name changes the value will be assigned to the jobName and the jobs intial value will be set to '' */
  assignJob(value:String){
          this.schedulerForm.patchValue({jobName:value})
          this.isNotPause = false;
    }

    onSubmit(){
    console.log(this.schedulerForm)
    }


}

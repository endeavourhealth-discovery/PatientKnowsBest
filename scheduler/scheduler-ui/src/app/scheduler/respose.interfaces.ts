

export interface GetJobs{
    statusCode :Number,
    data :[{
        groupName :String,
        jobName :String ,
        jobStatus:String,
        lastFiredTime:Date,
        nextFireTime:Date,
        scheduleTime:Date
         Stop:Boolean;
    }]
}


// interface for startjob response
export interface Job{
    statusCode :Number,
    data:boolean
}


export interface AvailableJobs{
    statusCode:Number;
    data:Job_Details[]
}

export interface Job_Details{
    'job_name':String,
    'status':Boolean;
    "start_url": String,
    "stop_url": String
}

export interface Logs {
  statusCode : Number ,
  data :LogDetails []
}

export interface LogDetails{
  id : Number,
  job_name : String,
  status : String,
  information : String,
  job_start_time : Date;
  job_complete_time : Date
}

export interface  ConfiguredJobs{
  statusCode : Number ,
  data : ConfiguredJob[]
}


export interface ConfiguredJob {
  "job_name": String,
  "class_path": String,
  "start_method": String,
  "stop_method": String,
  "parameters":String,
  "status": boolean,
  "start_url": String,
  "stop_url": String
}



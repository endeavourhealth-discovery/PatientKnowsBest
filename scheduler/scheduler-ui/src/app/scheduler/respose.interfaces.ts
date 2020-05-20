

export interface GetJobs{
    statusCode :Number,
    data :[{
        groupName :String,
        jobName :String ,
        jobStatus:String,
        lastFiredTime:Date,
        nextFireTime:Date,
        scheduleTime:Date
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



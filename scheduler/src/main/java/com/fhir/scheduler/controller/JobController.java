package com.fhir.scheduler.controller;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fhir.scheduler.entity.Available_jobs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import com.fhir.scheduler.dto.ServerResponse;
import com.fhir.scheduler.job.CronJob;
import com.fhir.scheduler.job.SimpleJob;
import com.fhir.scheduler.service.JobService;
import com.fhir.scheduler.util.ServerResponseCode;


@RestController
@CrossOrigin(origins = "http://10.0.101.59:4200")
@RequestMapping("/scheduler/")
public class JobController {

	@Autowired
	@Lazy
	JobService jobService;


	@Autowired
	Available_jobs jobs;


	@RequestMapping("schedule")
	public ServerResponse schedule(@RequestParam("jobName") String jobName,
								   @RequestParam("jobScheduleTime") @DateTimeFormat(pattern = "yyyy/MM/dd HH:mm") Date jobScheduleTime,
								   @RequestParam("cronExpression") String cronExpression) throws ParseException {
		System.out.println("JobController.schedule()");

		//Job Name is mandatory
		if (jobName == null || jobName.trim().equals("")) {
			return getServerResponse(ServerResponseCode.JOB_NAME_NOT_PRESENT, false);
		}

		//Check if job Name is unique and job details exists

		if (!jobService.isJobWithNamePresent(jobName)) {
			if (jobService.checkJobDetailsExists(jobName)) {
				if (cronExpression == null || cronExpression.trim().equals("")) {
					if (jobService.checkValidDate(jobScheduleTime)) {
						//Single Trigger
						String jobType = jobService.getJobType(jobName);
						boolean status = jobService.scheduleOneTimeJob(jobName, SimpleJob.class, jobScheduleTime, jobType);
						if (status) {
							return getServerResponse(ServerResponseCode.SUCCESS, jobService.getAllJobs());
						} else {
							return getServerResponse(ServerResponseCode.ERROR, false);
						}

					} else {
						return getServerResponse(ServerResponseCode.TIME_ERROR, false);
					}
				} else {
					//Cron Trigger
					String jobType = jobService.getJobType(jobName); //this gives the job type and this must be sent as paramenter to the
					boolean status = jobService.scheduleCronJob(jobName, CronJob.class, new Date(), cronExpression, jobType);
					if (status) {
						return getServerResponse(ServerResponseCode.SUCCESS, jobService.getAllJobs());
					} else {
						return getServerResponse(ServerResponseCode.ERROR, false);
					}
				}
			} else {
				return getServerResponse(ServerResponseCode.JOB_DETAILS_UNKNOWN, false);
			}
		} else {
			return getServerResponse(ServerResponseCode.JOB_WITH_SAME_NAME_EXIST, false);
		}
	}

	@RequestMapping("unschedule")
	public void unschedule(@RequestParam("jobName") String jobName) {
		System.out.println("JobController.unschedule()");
		jobService.unScheduleJob(jobName);
	}

	@RequestMapping("delete")
	public ServerResponse delete(@RequestParam("jobName") String jobName) {
		System.out.println("JobController.delete()");

		if (jobService.isJobWithNamePresent(jobName)) {
			boolean isJobRunning = jobService.isJobRunning(jobName);

			if (!isJobRunning) {
				boolean status = jobService.deleteJob(jobName);
				if (status) {
					return getServerResponse(ServerResponseCode.SUCCESS, true);
				} else {
					return getServerResponse(ServerResponseCode.ERROR, false);
				}
			} else {
				return getServerResponse(ServerResponseCode.JOB_ALREADY_IN_RUNNING_STATE, false);
			}
		} else {
			//Job doesn't exist
			return getServerResponse(ServerResponseCode.JOB_DOESNT_EXIST, false);
		}
	}

	@RequestMapping("pause")
	public ServerResponse pause(@RequestParam("jobName") String jobName) {
		System.out.println("JobController.pause()");

		if (jobService.isJobWithNamePresent(jobName)) {

			boolean isJobRunning = jobService.isJobRunning(jobName);

			if (!isJobRunning) {
				boolean status = jobService.pauseJob(jobName);
				if (status) {
					return getServerResponse(ServerResponseCode.SUCCESS, true);
				} else {
					return getServerResponse(ServerResponseCode.ERROR, false);
				}
			} else {
				return getServerResponse(ServerResponseCode.JOB_ALREADY_IN_RUNNING_STATE, false);
			}

		} else {
			//Job doesn't exist
			return getServerResponse(ServerResponseCode.JOB_DOESNT_EXIST, false);
		}
	}

	@RequestMapping("resume")
	public ServerResponse resume(@RequestParam("jobName") String jobName) {
		System.out.println("JobController.resume()");

		if (jobService.isJobWithNamePresent(jobName)) {
			String jobState = jobService.getJobState(jobName);

			if (jobState.equals("PAUSED")) {
				System.out.println("Job current state is PAUSED, Resuming job...");
				boolean status = jobService.resumeJob(jobName);

				if (status) {
					return getServerResponse(ServerResponseCode.SUCCESS, true);
				} else {
					return getServerResponse(ServerResponseCode.ERROR, false);
				}
			} else {
				return getServerResponse(ServerResponseCode.JOB_NOT_IN_PAUSED_STATE, false);
			}

		} else {
			//Job doesn't exist
			return getServerResponse(ServerResponseCode.JOB_DOESNT_EXIST, false);
		}
	}

	@RequestMapping("update")
	public ServerResponse updateJob(@RequestParam("jobName") String jobName,
									@RequestParam("jobScheduleTime") @DateTimeFormat(pattern = "yyyy/MM/dd HH:mm") Date jobScheduleTime,
									@RequestParam("cronExpression") String cronExpression) throws ParseException {
		System.out.println("JobController.updateJob()");

		//Job Name is mandatory
		if (jobName == null || jobName.trim().equals("")) {
			return getServerResponse(ServerResponseCode.JOB_NAME_NOT_PRESENT, false);
		}

		//Edit Job
		if (jobService.isJobWithNamePresent(jobName)) {

			if (cronExpression == null || cronExpression.trim().equals("")) {
				//Single Trigger
				if (jobService.checkValidDate(jobScheduleTime)) {
					boolean status = jobService.updateOneTimeJob(jobName, jobScheduleTime);
					if (status) {
						return getServerResponse(ServerResponseCode.SUCCESS, jobService.getAllJobs());
					} else {
						return getServerResponse(ServerResponseCode.ERROR, false);
					}
				} else {
					return getServerResponse(ServerResponseCode.TIME_ERROR, false);
				}

			} else {
				//Cron Trigger
				boolean status = jobService.updateCronJob(jobName, new Date(), cronExpression);
				if (status) {
					return getServerResponse(ServerResponseCode.SUCCESS, jobService.getAllJobs());
				} else {
					return getServerResponse(ServerResponseCode.ERROR, false);
				}
			}


		} else {
			return getServerResponse(ServerResponseCode.JOB_DOESNT_EXIST, false);
		}
	}

	@RequestMapping("jobs")
	public ServerResponse getAllJobs() {
//		System.out.println("JobController.getAllJobs()");

		List<Map<String, Object>> list = jobService.getAllJobs();
		return getServerResponse(ServerResponseCode.SUCCESS, list);
	}

	@RequestMapping("checkJobName")
	public ServerResponse checkJobName(@RequestParam("jobName") String jobName) {
//		System.out.println("JobController.checkJobName()");

		//Job Name is mandatory
		if (jobName == null || jobName.trim().equals("")) {
			return getServerResponse(ServerResponseCode.JOB_NAME_NOT_PRESENT, false);
		}

		boolean status = jobService.isJobWithNamePresent(jobName);
		return getServerResponse(ServerResponseCode.SUCCESS, status);
	}

	@RequestMapping("isJobRunning")
	public ServerResponse isJobRunning(@RequestParam("jobName") String jobName) {
//		System.out.println("JobController.isJobRunning()");

		boolean status = jobService.isJobRunning(jobName);
		return getServerResponse(ServerResponseCode.SUCCESS, status);
	}

	@RequestMapping("jobState")
	public ServerResponse getJobState(@RequestParam("jobName") String jobName) {
//		System.out.println("JobController.getJobState()");

		String jobState = jobService.getJobState(jobName);
		return getServerResponse(ServerResponseCode.SUCCESS, jobState);
	}

	@RequestMapping("stop")
	public ServerResponse stopJob(@RequestParam("jobName") String jobName) {
		System.out.println("JobController.stopJob()");

		if (jobService.isJobWithNamePresent(jobName)) {

			if (jobService.isJobRunning(jobName)) {
				boolean status = jobService.stopJob(jobName);
				if (status) {
					return getServerResponse(ServerResponseCode.SUCCESS, true);
				} else {
					//Server error
					return getServerResponse(ServerResponseCode.ERROR, false);
				}

			} else {
				//Job not in running state
				return getServerResponse(ServerResponseCode.JOB_NOT_IN_RUNNING_STATE, false);
			}

		} else {
			//Job doesn't exist
			return getServerResponse(ServerResponseCode.JOB_DOESNT_EXIST, false);
		}
	}

	@RequestMapping("start")
	public ServerResponse startJobNow(@RequestParam("jobName") String jobName) {
//		System.out.println("JobController.startJobNow()");

		if (jobService.isJobWithNamePresent(jobName)) {

			if (!jobService.isJobRunning(jobName)) {
				boolean status = jobService.startJobNow(jobName);

				if (status) {
					//Success
					return getServerResponse(ServerResponseCode.SUCCESS, true);

				} else {
					//Server error
					return getServerResponse(ServerResponseCode.ERROR, false);
				}

			} else {
				//Job already running
				return getServerResponse(ServerResponseCode.JOB_ALREADY_IN_RUNNING_STATE, false);
			}

		} else {
			//Job doesn't exist
			return getServerResponse(ServerResponseCode.JOB_DOESNT_EXIST, false);
		}
	}

	public ServerResponse getServerResponse(int responseCode, Object data) {
		ServerResponse serverResponse = new ServerResponse();
		serverResponse.setStatusCode(responseCode);
		serverResponse.setData(data);
		return serverResponse;
	}


	@RequestMapping("getAvailableJobs")
	public ServerResponse getAvailableJobs() {
//		for (Available_jobs jobs:
//		jobService.getAllJobs()) {
//			System.out.println(jobs.getJob_name());
//		}

		return getServerResponse(ServerResponseCode.SUCCESS, jobService.getAvailableJobs());
	}

	@RequestMapping("getLogs")
	public ServerResponse getLogs() {

		return getServerResponse(ServerResponseCode.SUCCESS, jobService.getLog());
	}


	@PostMapping("addHttpJob")
	public ServerResponse addHttpJob(@RequestBody Map<String, String> payload) {
		boolean exists = jobService.addHttpJob(payload.get("jobName"), payload.get("starturl"), payload.get("stopurl"));


		if (exists == false) {
			return getServerResponse(ServerResponseCode.JOB_WITH_SAME_NAME_EXIST, false);
		} else {
			return getServerResponse(ServerResponseCode.SUCCESS, true);
		}


	}


	@PostMapping("addClassJob")
	public ServerResponse addClassJob(@RequestBody Map<String, String> payload) {
		boolean exists = jobService.addClassJob(payload.get("jobName"), payload.get("startmethod"), payload.get("stopmethod"), payload.get("classpath"), payload.get("parameters"));
		if (exists == false) {
			return getServerResponse(ServerResponseCode.JOB_WITH_SAME_NAME_EXIST, false);
		} else {
			return getServerResponse(ServerResponseCode.SUCCESS, true);
		}
	}


	@GetMapping("getConfiguredJobs")
	public ServerResponse getConfiguredJobs() {
		return getServerResponse(ServerResponseCode.SUCCESS, jobService.getConfiguredJobs());
	}

	@DeleteMapping("deleteConfiguredJob")
	public ServerResponse deleteConfiguredJob(@RequestParam("jobName") String jobName) {
		jobService.deleteConfiguredJob(jobName);
		return getServerResponse(ServerResponseCode.SUCCESS, jobService.getConfiguredJobs());
	}


	@PutMapping("updateHttpJob")
	public ServerResponse updateHttpJob(@RequestBody Map<String, String> payload) {
		boolean status = jobService.updateHttpJob(payload.get("jobName"), payload.get("startUrl"), payload.get("stopUrl"));
		if (status) {
			return getServerResponse(ServerResponseCode.SUCCESS,true);
		} else {
			return getServerResponse(ServerResponseCode.ERROR,false);
		}

	}


	@PutMapping("updateClassJob")
	public ServerResponse updateClassJob(@RequestBody Map<String, String> payload) {
		boolean status = jobService.updateClassJob(payload.get("jobName"), payload.get("startMethod"), payload.get("stopMethod"), payload.get("classPath"), payload.get("parameters"));

		if (status) {
			return getServerResponse(ServerResponseCode.SUCCESS,true);
		} else {
			return getServerResponse(ServerResponseCode.ERROR, false);
		}

	}


}

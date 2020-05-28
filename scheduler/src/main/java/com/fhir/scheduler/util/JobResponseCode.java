package com.fhir.scheduler.util;

public class JobResponseCode {
    public static final int SUCCESS = 200;
    public static final String INVALID_URL ="Invalid URL";
    public static final String INVALID_CLASS_PATH = "Invalid class path";
    public static final String INVALID_START_METHOD_NAME = "Invalid start method";
    public static final String INVALID_STOP_METHOD_NAME ="Invalid stop method";
    public static final String UNKNOWN_ERROR = "Unknown error occurred";
    public static final String SUCCESSFUL = "Success";
    public static final String INVALID_STOP_URL = "Invalid stop url";
    public static final String FAILURE = "Failed";
    public static final String INTERRUPTED = "Interrupted";
    public static final String INTERRUPT_SUCCESSFUL = "Job interrupted successfully";
    public static final String INTERRUPT_FAILED = "Interrupt failed";

    public static final String REQUEST_STOP_ERROR = "unable to complete interrupt request ";

    public static final String COMPLETE = "Successfully executed ";

    public static final String UNABLE_TO_ACCESS = "Unable to access class";
}

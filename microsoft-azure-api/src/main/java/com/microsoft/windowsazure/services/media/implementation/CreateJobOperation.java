package com.microsoft.windowsazure.services.media.implementation;

import com.microsoft.windowsazure.services.media.implementation.content.JobType;

public class CreateJobOperation implements Operation {

    private JobType jobType;

    public CreateJobOperation setJob(JobType jobType) {
        this.jobType = jobType;
        return this;
    }

    public JobType getJob() {
        return this.jobType;
    }

}

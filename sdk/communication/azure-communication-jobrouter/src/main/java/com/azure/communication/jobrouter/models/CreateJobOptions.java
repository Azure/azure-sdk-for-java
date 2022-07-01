// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

/**
 * Request options to create a job.
 * Job: A unit of work to be routed.
 */
public class CreateJobOptions extends JobOptions {
    /**
     * Constructor for CreateJobOptions.
     * @param id The id of the job.
     * @param channelReference Reference to an external parent context, eg. call ID.
     * @param channelId The channel identifier. eg. voice, chat, etc.
     * @param queueId The Id of the Queue that this job is queued to.
     * @param priority The priority of this job.
     */
    public CreateJobOptions(String id, String channelReference, String channelId, String queueId, Integer priority) {
        this.id = id;
        this.channelReference = channelReference;
        this.channelId = channelId;
        this.queueId = queueId;
        this.priority = priority;
    }
}

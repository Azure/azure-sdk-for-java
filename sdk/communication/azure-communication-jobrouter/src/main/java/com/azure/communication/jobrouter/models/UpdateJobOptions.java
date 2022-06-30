package com.azure.communication.jobrouter.models;

import com.azure.communication.jobrouter.implementation.models.JobOptions;

public class UpdateJobOptions extends JobOptions {

    /**
     * Constructor for UpdateJobOptions.
     * @param id The id of the job.
     */
    public UpdateJobOptions(String id) {
        this.id = id;
    }

    /**
     * Sets channelReference.
     * @param channelReference Reference to an external parent context, eg. call ID.
     * @return this
     */
    public UpdateJobOptions setChannelReference(String channelReference) {
        this.channelReference = channelReference;
        return this;
    }

    /**
     * Sets channelId.
     * @param channelId The channel identifier. eg. voice, chat, etc.
     * @return this
     */
    public UpdateJobOptions setChannelId(String channelId) {
        this.channelId = channelId;
        return this;
    }

    /**
     * Sets queueId.
     * @param queueId The Id of the Queue that this job is queued to.
     * @return this
     */
    public UpdateJobOptions setQueueId(String queueId) {
        this.queueId = queueId;
        return this;
    }

    /**
     * Sets priority.
     * @param priority The priority of this job.
     * @return this
     */
    public UpdateJobOptions setPriority(Integer priority) {
        this.priority = priority;
        return this;
    }
}

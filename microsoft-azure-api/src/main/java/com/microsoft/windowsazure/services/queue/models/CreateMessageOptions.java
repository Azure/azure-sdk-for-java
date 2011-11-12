package com.microsoft.windowsazure.services.queue.models;


public class CreateMessageOptions extends QueueServiceOptions {
    private Integer visibilityTimeoutInSeconds;
    private Integer timeToLiveInSeconds;

    public Integer getVisibilityTimeoutInSeconds() {
        return visibilityTimeoutInSeconds;
    }

    public void setVisibilityTimeoutInSeconds(Integer visibilityTimeoutInSeconds) {
        this.visibilityTimeoutInSeconds = visibilityTimeoutInSeconds;
    }

    public Integer getTimeToLiveInSeconds() {
        return timeToLiveInSeconds;
    }

    public void setTimeToLiveInSeconds(Integer timeToLiveInSeconds) {
        this.timeToLiveInSeconds = timeToLiveInSeconds;
    }
}

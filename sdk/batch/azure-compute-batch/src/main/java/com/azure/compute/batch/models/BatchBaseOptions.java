package com.azure.compute.batch.models;

public class BatchBaseOptions {
    private Integer timeOutInSeconds;

    /**
     * Gets the maximum time that the server can spend processing the request, in seconds. The default is 30 seconds.
     *
     * @return The maximum time that the server can spend processing the request, in seconds.
     */
    public Integer getTimeOutInSeconds() {
        return timeOutInSeconds;
    }

    /**
     * Sets the maximum time that the server can spend processing the request, in seconds. The default is 30 seconds.
     *
     * @param timeOutInSeconds The maximum time that the server can spend processing the request, in seconds.
     */
    public void setTimeOutInSeconds(Integer timeOutInSeconds) {
        this.timeOutInSeconds = timeOutInSeconds;
    }


}

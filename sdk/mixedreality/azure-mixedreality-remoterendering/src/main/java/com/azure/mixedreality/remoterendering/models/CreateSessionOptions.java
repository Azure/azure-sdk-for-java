package com.azure.mixedreality.remoterendering.models;

import java.time.Duration;

public final class CreateSessionOptions {

    private Duration maxLeaseTime = Duration.ofMinutes(10);
    private SessionSize size = SessionSize.STANDARD;
    private Duration pollInterval = Duration.ofSeconds(10);


    public CreateSessionOptions setMaxLeaseTime(Duration maxLeaseTime) {
        this.maxLeaseTime = maxLeaseTime;
        return this;
    }

    public CreateSessionOptions setSize(SessionSize size) {
        this.size = size;
        return this;
    }

    /**
     * Set the polling interval to use during long-running beginSession operations.
     * @param interval The new period to use for polling.
     */
    public CreateSessionOptions setPollInterval(Duration interval) {
        this.pollInterval = interval;
        return this;
    }



    /**
     * Get the maxLeaseTime property: The time the session will run after reaching the 'Ready' state.
     *
     * @return the maxLeaseTime value.
     */
    public Duration getMaxLeaseTime() {
        return this.maxLeaseTime;
    }

    /**
     * Get the size property: Size of the server used for the rendering session. Remote Rendering with Standard size
     * server has a maximum scene size of 20 million polygons. Remote Rendering with Premium size does not enforce a
     * hard maximum, but performance may be degraded if your content exceeds the rendering capabilities of the service.
     *
     * @return the size value.
     */
    public SessionSize getSize() {
        return this.size;
    }

    /**
     * Set the polling interval to use during long-running beginSession operations.
     * @return The current polling interval.
     */
    public Duration getPollInterval() { return this.pollInterval; }
}

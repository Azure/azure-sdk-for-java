package com.azure.mixedreality.remoterendering.models;

import java.time.Duration;

public class SessionCreationOptions {
    /*
     * The time in minutes the session will run after reaching the 'Ready'
     * state.
     */
    private final int maxLeaseTimeMinutes;

    /*
     * Size of the server used for the rendering session. Remote Rendering with
     * Standard size server has a maximum scene size of 20 million polygons.
     * Remote Rendering with Premium size does not enforce a hard maximum, but
     * performance may be degraded if your content exceeds the rendering
     * capabilities of the service.
     */
    private final SessionSize size;

    private Duration pollInterval;

    /**
     * Creates an instance of CreateSessionSettings class.
     *
     * @param maxLeaseTimeMinutes the maxLeaseTimeMinutes value to set.
     * @param size the size value to set.
     */
    public SessionCreationOptions(int maxLeaseTimeMinutes, SessionSize size) {
        this.maxLeaseTimeMinutes = maxLeaseTimeMinutes;
        this.size = size;
        this.pollInterval = Duration.ofSeconds(10);
    }

    /**
     * Get the maxLeaseTimeMinutes property: The time in minutes the session will run after reaching the 'Ready' state.
     *
     * @return the maxLeaseTimeMinutes value.
     */
    public int getMaxLeaseTimeMinutes() {
        return this.maxLeaseTimeMinutes;
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

    /**
     * Set the polling interval to use during long-running beginSession operations.
     * @param interval The new period to use for polling.
     */
    public void setPollInterval(Duration interval) {
        this.pollInterval = interval;
    }
}

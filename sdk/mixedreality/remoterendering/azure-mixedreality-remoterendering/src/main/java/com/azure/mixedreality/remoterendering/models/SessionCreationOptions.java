package com.azure.mixedreality.remoterendering.models;

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

    /**
     * Creates an instance of CreateSessionSettings class.
     *
     * @param maxLeaseTimeMinutes the maxLeaseTimeMinutes value to set.
     * @param size the size value to set.
     */
    public SessionCreationOptions(int maxLeaseTimeMinutes, SessionSize size) {
        this.maxLeaseTimeMinutes = maxLeaseTimeMinutes;
        this.size = size;
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
}

package com.azure.mixedreality.remoterendering.models;

import java.time.Duration;

public final class BeginSessionOptions {

    private Duration maxLeaseTime = Duration.ofMinutes(10);
    private RenderingSessionSize size = RenderingSessionSize.STANDARD;

    public BeginSessionOptions setMaxLeaseTime(Duration maxLeaseTime) {
        this.maxLeaseTime = maxLeaseTime;
        return this;
    }

    public BeginSessionOptions setSize(RenderingSessionSize size) {
        this.size = size;
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
    public RenderingSessionSize getSize() {
        return this.size;
    }
}

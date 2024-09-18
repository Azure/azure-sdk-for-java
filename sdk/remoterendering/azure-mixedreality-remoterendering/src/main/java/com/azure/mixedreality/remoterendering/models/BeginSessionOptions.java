// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.remoterendering.models;

import com.azure.core.annotation.Fluent;

import java.time.Duration;

/** Options for a session to be created. */
@Fluent
public final class BeginSessionOptions {

    private Duration maxLeaseTime = Duration.ofMinutes(10);
    private RenderingSessionSize size = RenderingSessionSize.STANDARD;

    /**
     * Set the maxLeaseTime property: The time the session will run after reaching the 'Ready' state.
     *
     * @param maxLeaseTime the maxLeaseTime value
     * @return this BeginSessionOptions object.
     */
    public BeginSessionOptions setMaxLeaseTime(Duration maxLeaseTime) {
        this.maxLeaseTime = maxLeaseTime;
        return this;
    }

    /**
     * Set the size property: Size of the server used for the rendering session. Remote Rendering with Standard size
     * server has a maximum scene size of 20 million polygons. Remote Rendering with Premium size does not enforce a
     * hard maximum, but performance may be degraded if your content exceeds the rendering capabilities of the service.
     *
     * @param size the size value
     * @return this BeginSessionOptions object.
     */
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

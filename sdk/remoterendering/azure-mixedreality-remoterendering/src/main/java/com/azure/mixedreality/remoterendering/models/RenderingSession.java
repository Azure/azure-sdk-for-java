// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.remoterendering.models;

import com.azure.core.annotation.Immutable;

import java.time.Duration;
import java.time.OffsetDateTime;

/** Holds the properties of a rendering session. */
@Immutable
public final class RenderingSession {
    private final String id;
    private final int arrInspectorPort;
    private final int handshakePort;
    private final Duration elapsedTime;
    private final String hostname;
    private final Duration maxLeaseTime;
    private final RenderingSessionSize sessionSize;
    private final RenderingSessionStatus sessionStatus;
    private final float teraflops;
    private final RemoteRenderingServiceError error;
    private final OffsetDateTime creationTime;

    /**
     * Constructs a new RenderingSession object.
     *
     * @param id The id of the session supplied when the conversion was created.
     * @param arrInspectorPort The TCP port at which the Azure Remote Rendering Inspector tool is hosted.
     * @param handshakePort The TCP port used for the handshake.
     * @param elapsedTime Amount of time the session is or has been in Ready state. Time is rounded down to a full minute.
     * @param hostname The hostname under which the rendering session is reachable.
     * @param maxLeaseTime The time the session will run after reaching the 'Ready' state.
     * @param sessionSize Size of the server used for the rendering session. Remote Rendering with Standard size
     *                    server has a maximum scene size of 20 million polygons. Remote Rendering with Premium size does not enforce a
     *                    hard maximum, but performance may be degraded if your content exceeds the rendering capabilities of the service.
     * @param sessionStatus The status of the rendering session. Once the status reached the 'Ready' state it can be
     *                      connected to. The terminal state is 'Stopped'.
     * @param teraflops The computational power of the rendering session GPU measured in Teraflops.
     * @param error The error object containing details about the rendering session startup failure.
     * @param creationTime The time when the rendering session was created. Date and time in ISO 8601 format.
     */
    public RenderingSession(String id,
                            int arrInspectorPort,
                            int handshakePort,
                            Duration elapsedTime,
                            String hostname,
                            Duration maxLeaseTime,
                            RenderingSessionSize sessionSize,
                            RenderingSessionStatus sessionStatus,
                            float teraflops,
                            RemoteRenderingServiceError error,
                            OffsetDateTime creationTime) {
        this.id = id;
        this.arrInspectorPort = arrInspectorPort;
        this.handshakePort = handshakePort;
        this.elapsedTime = elapsedTime;
        this.hostname = hostname;
        this.maxLeaseTime = maxLeaseTime;
        this.sessionSize = sessionSize;
        this.sessionStatus = sessionStatus;
        this.teraflops = teraflops;
        this.error = error;
        this.creationTime = creationTime;
    }


    /**
     * Get the id property: The id of the session supplied when the conversion was created.
     *
     * @return the id value.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Get the arrInspectorPort property: The TCP port at which the Azure Remote Rendering Inspector tool is hosted.
     *
     * @return the arrInspectorPort value.
     */
    public int getArrInspectorPort() {
        return this.arrInspectorPort;
    }

    /**
     * Get the handshakePort property: The TCP port used for the handshake.
     *
     * @return the handshakePort value.
     */
    public int getHandshakePort() {
        return this.handshakePort;
    }

    /**
     * Get the elapsedTime property: Amount of time the session is or has been in Ready state. Time is
     * rounded down to a full minute.
     *
     * @return the elapsedTime value.
     */
    public Duration getElapsedTime() {
        return elapsedTime;
    }

    /**
     * Get the hostname property: The hostname under which the rendering session is reachable.
     *
     * @return the hostname value.
     */
    public String getHostname() {
        return this.hostname;
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
        return this.sessionSize;
    }

    /**
     * Get the status property: The status of the rendering session. Once the status reached the 'Ready' state it can be
     * connected to. The terminal state is 'Stopped'.
     *
     * @return the status value.
     */
    public RenderingSessionStatus getStatus() {
        return this.sessionStatus;
    }

    /**
     * Get the teraflops property: The computational power of the rendering session GPU measured in Teraflops.
     *
     * @return the teraflops value.
     */
    public float getTeraflops() {
        return this.teraflops;
    }

    /**
     * Get the error property: The error object containing details about the rendering session startup failure.
     *
     * @return the error value.
     */
    public RemoteRenderingServiceError getError() {
        return this.error;
    }

    /**
     * Get the creationTime property: The time when the rendering session was created. Date and time in ISO 8601 format.
     *
     * @return the creationTime value.
     */
    public OffsetDateTime getCreationTime() {
        return this.creationTime;
    }
}

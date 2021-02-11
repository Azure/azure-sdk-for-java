// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.remoterendering.models;

import java.time.Duration;
import java.time.OffsetDateTime;

/** Holds the properties of a rendering session. */
public final class RenderingSession {
    private String id;
    private int arrInspectorPort;
    private int handshakePort;
    private Duration elapsedTime;
    private String hostname;
    private Duration maxLeaseTime;
    private RenderingSessionSize sessionSize;
    private RenderingSessionStatus sessionStatus;
    private float teraflops;
    private RemoteRenderingServiceError error;
    private OffsetDateTime creationTime;

    /**
     * Set the id property: The id of the session supplied when the conversion was created.
     *
     * @param id the id value.
     * @return this RenderingSession object.
     */
    public RenderingSession setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Set the arrInspectorPort property: The TCP port at which the Azure Remote Rendering Inspector tool is hosted.
     *
     * @param arrInspectorPort the arrInspectorPort value.
     * @return this RenderingSession object.
     */
    public RenderingSession setArrInspectorPort(int arrInspectorPort) {
        this.arrInspectorPort = arrInspectorPort;
        return this;
    }

    /**
     * Set the handshakePort property: The TCP port used for the handshake.
     *
     * @param handshakePort the handshakePort value.
     * @return this RenderingSession object.
     */
    public RenderingSession setHandshakePort(int handshakePort) {
        this.handshakePort = handshakePort;
        return this;
    }

    /**
     * Set the elapsedTime property: Amount of time the session is or has been in Ready state. Time is
     * rounded down to a full minute.
     *
     * @param elapsedTime the elapsedTime value.
     * @return this RenderingSession object.
     */
    public RenderingSession setElapsedTime(Duration elapsedTime) {
        this.elapsedTime = elapsedTime;
        return this;
    }

    /**
     * Set the hostname property: The hostname under which the rendering session is reachable.
     *
     * @param hostname the hostname value.
     * @return this RenderingSession object.
     */
    public RenderingSession setHostname(String hostname) {
        this.hostname = hostname;
        return this;
    }

    /**
     * Set the maxLeaseTime property: The time the session will run after reaching the 'Ready' state.
     *
     * @param maxLeaseTime the maxLeaseTime value.
     * @return this RenderingSession object.
     */
    public RenderingSession setMaxLeaseTime(Duration maxLeaseTime) {
        this.maxLeaseTime = maxLeaseTime;
        return this;
    }

    /**
     * Set the size property: Size of the server used for the rendering session. Remote Rendering with Standard size
     * server has a maximum scene size of 20 million polygons. Remote Rendering with Premium size does not enforce a
     * hard maximum, but performance may be degraded if your content exceeds the rendering capabilities of the service.
     *
     * @param sessionSize the session size value.
     * @return this RenderingSession object.
     */
    public RenderingSession setSize(RenderingSessionSize sessionSize) {
        this.sessionSize = sessionSize;
        return this;
    }

    /**
     * Set the status property: The status of the rendering session. Once the status reached the 'Ready' state it can be
     * connected to. The terminal state is 'Stopped'.
     *
     * @param sessionStatus the session status value.
     * @return this RenderingSession object.
     */
    public RenderingSession setStatus(RenderingSessionStatus sessionStatus) {
        this.sessionStatus = sessionStatus;
        return this;
    }

    /**
     * Set the teraflops property: The computational power of the rendering session GPU measured in Teraflops.
     *
     * @param teraflops the teraflops value.
     * @return this RenderingSession object.
     */
    public RenderingSession setTeraflops(float teraflops) {
        this.teraflops = teraflops;
        return this;
    }

    /**
     * Set the error property: The error object containing details about the rendering session startup failure.
     *
     * @param error the error value.
     * @return this RenderingSession object.
     */
    public RenderingSession setError(RemoteRenderingServiceError error) {
        this.error = error;
        return this;
    }

    /**
     * Set the creationTime property: The time when the rendering session was created. Date and time in ISO 8601 format.
     *
     * @param creationTime the creationTime value.
     * @return this RenderingSession object.
     */
    public RenderingSession setCreationTime(OffsetDateTime creationTime) {
        this.creationTime = creationTime;
        return this;
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

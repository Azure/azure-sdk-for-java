package com.azure.mixedreality.remoterendering.models;

import java.time.Duration;
import java.time.OffsetDateTime;

public final class Session {
    private String id;
    private int arrInspectorPort;
    private int handshakePort;
    private Duration elapsedTime;
    private String hostname;
    private Duration maxLeaseTime;
    private SessionSize sessionSize;
    private SessionStatus sessionStatus;
    private float teraflops;
    private RemoteRenderingServiceError error;
    private OffsetDateTime creationTime;

    public Session setId(String id) {
        this.id = id;
        return this;
    }

    public Session setArrInspectorPort(int arrInspectorPort) {
        this.arrInspectorPort = arrInspectorPort;
        return this;
    }

    public Session setHandshakePort(int handshakePort) {
        this.handshakePort = handshakePort;
        return this;
    }

    public Session setElapsedTime(Duration elapsedTime) {
        this.elapsedTime = elapsedTime;
        return this;
    }

    public Session setHostname(String hostname) {
        this.hostname = hostname;
        return this;
    }

    public Session setMaxLeaseTime(Duration maxLeaseTime) {
        this.maxLeaseTime = maxLeaseTime;
        return this;
    }

    public Session setSessionSize(SessionSize sessionSize) {
        this.sessionSize = sessionSize;
        return this;
    }

    public Session setSessionStatus(SessionStatus sessionStatus) {
        this.sessionStatus = sessionStatus;
        return this;
    }

    public Session setTeraflops(float teraflops) {
        this.teraflops = teraflops;
        return this;
    }

    public Session setError(RemoteRenderingServiceError error) {
        this.error = error;
        return this;
    }

    public Session setCreationTime(OffsetDateTime creationTime) {
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
     * Get the elapsedTime property: Amount of timethe session is or has been in Ready state. Time is
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
    public SessionSize getSize() {
        return this.sessionSize;
    }

    /**
     * Get the status property: The status of the rendering session. Once the status reached the 'Ready' state it can be
     * connected to. The terminal state is 'Stopped'.
     *
     * @return the status value.
     */
    public SessionStatus getStatus() {
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

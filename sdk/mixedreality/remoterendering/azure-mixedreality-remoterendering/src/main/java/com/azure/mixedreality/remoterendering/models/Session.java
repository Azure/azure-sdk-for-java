package com.azure.mixedreality.remoterendering.models;

import com.azure.mixedreality.remoterendering.models.internal.ModelTranslator;

import java.time.OffsetDateTime;

public class Session {
    private com.azure.mixedreality.remoterendering.implementation.models.SessionProperties session;

    public Session(com.azure.mixedreality.remoterendering.implementation.models.SessionProperties session) {
        this.session = session;
    }

    /**
     * Get the id property: The id of the session supplied when the conversion was created.
     *
     * @return the id value.
     */
    public String getId() {
        return session.getId();
    }

    /**
     * Get the arrInspectorPort property: The TCP port at which the Azure Remote Rendering Inspector tool is hosted.
     *
     * @return the arrInspectorPort value.
     */
    public int getArrInspectorPort() {
        return session.getArrInspectorPort();
    }

    /**
     * Get the handshakePort property: The TCP port used for the handshake.
     *
     * @return the handshakePort value.
     */
    public int getHandshakePort() {
        return session.getHandshakePort();
    }

    /**
     * Get the elapsedTimeMinutes property: Amount of time in minutes the session is or has been in Ready state. Time is
     * rounded down to a full minute.
     *
     * @return the elapsedTimeMinutes value.
     */
    public int getElapsedTimeMinutes() {
        return session.getElapsedTimeMinutes();
    }

    /**
     * Get the hostname property: The hostname under which the rendering session is reachable.
     *
     * @return the hostname value.
     */
    public String getHostname() {
        return session.getHostname();
    }

    /**
     * Get the maxLeaseTimeMinutes property: The time in minutes the session will run after reaching the 'Ready' state.
     *
     * @return the maxLeaseTimeMinutes value.
     */
    public int getMaxLeaseTimeMinutes() {
        return session.getMaxLeaseTimeMinutes();
    }

    /**
     * Get the size property: Size of the server used for the rendering session. Remote Rendering with Standard size
     * server has a maximum scene size of 20 million polygons. Remote Rendering with Premium size does not enforce a
     * hard maximum, but performance may be degraded if your content exceeds the rendering capabilities of the service.
     *
     * @return the size value.
     */
    public SessionSize getSize() {
        return SessionSize.fromString(session.getSize().toString());
    }

    /**
     * Get the status property: The status of the rendering session. Once the status reached the 'Ready' state it can be
     * connected to. The terminal state is 'Stopped'.
     *
     * @return the status value.
     */
    public SessionStatus getStatus() {
        return SessionStatus.fromString(session.getStatus().toString());
    }

    /**
     * Get the teraflops property: The computational power of the rendering session GPU measured in Teraflops.
     *
     * @return the teraflops value.
     */
    public float getTeraflops() {
        return session.getTeraflops();
    }

    /**
     * Get the error property: The error object containing details about the rendering session startup failure.
     *
     * @return the error value.
     */
    public RemoteRenderingServiceError getError() {
        return new RemoteRenderingServiceError(session.getError());
    }

    /**
     * Get the creationTime property: The time when the rendering session was created. Date and time in ISO 8601 format.
     *
     * @return the creationTime value.
     */
    public OffsetDateTime getCreationTime() {
        return session.getCreationTime();
    }
}

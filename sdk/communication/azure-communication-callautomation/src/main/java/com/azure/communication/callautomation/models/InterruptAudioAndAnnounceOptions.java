// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Fluent;

import java.util.ArrayList;
import java.util.List;

/** The InterruptAudioAndAnnounceOptions model. */
@Fluent
public final class InterruptAudioAndAnnounceOptions {
    /*
    * A List of {@link PlaySource} representing the sources to play.
    * Currently only single play source per request is supported.
    */
    private final List<PlaySource> playSources;

    /*
    * The targets to play to
    */
    private final CommunicationIdentifier playTo;

    /*
     * The operation context
     */
    private String operationContext;

    /**
    * Constructor
    * @param playSources A List of {@link PlaySource} representing the sources to play.
    * @param playTo The target to play to.
    */
    public InterruptAudioAndAnnounceOptions(List<PlaySource> playSources, CommunicationIdentifier playTo) {
        this.playSources = playSources;
        this.playTo = playTo;
    }

    /**
     * Constructor
     * @param playSource A {@link PlaySource} representing the source to play.
     * @param playTo The target to play to.
     */
    public InterruptAudioAndAnnounceOptions(PlaySource playSource, CommunicationIdentifier playTo) {
        this.playSources = new ArrayList<>();
        this.playSources.add(playSource);
        this.playTo = playTo;
    }

    /**
     * Get the play sources.
     *
     * @return the playSources value.
     */
    public List<PlaySource> getPlaySources() {
        return this.playSources;
    }

    /**
     * Get the target to play to.
     *
     * @return the playTo value.
     */
    public CommunicationIdentifier getPlayTo() {
        return this.playTo;
    }

    /**
    * Get the operationContext property.
    *
    * @return the operationContext value.
    */
    public String getOperationContext() {
        return this.operationContext;
    }

    /**
    * Set the operationContext property.
    *
    * @param operationContext the operationContext value to set.
    * @return the InterruptAudioAndAnnounceOptions object itself.
    */
    public InterruptAudioAndAnnounceOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }
}

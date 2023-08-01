// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Fluent;

import java.util.Collections;
import java.util.List;

/** The PlayOptions model. */
@Fluent
public final class PlayOptions {
    /*
     * A {@link PlaySource} representing the source to play.
     */
    private final PlaySource playSource;

    /*
     * The targets to play to
     */
    private final List<CommunicationIdentifier> playTo;

    /*
     * The option to play the provided audio source in loop when set to true
     */
    private boolean loop;

    /*
     * The operation context
     */
    private String operationContext;

    /**
     * Constructor
     * @param playSources A List of {@link PlaySource} representing the sources to play.
     * @param playTo The targets to play to.
     */
    public PlayOptions(List<PlaySource> playSources, List<CommunicationIdentifier> playTo) {
        this(playSources.get(0), playTo);
    }

    /**
     * Constructor
     * @param playSource A {@link PlaySource} representing the source to play.
     * @param playTo The targets to play to.
     */
    public PlayOptions(PlaySource playSource, List<CommunicationIdentifier> playTo) {
        this.playSource = playSource;
        this.playTo = playTo;
    }

    /**
     * Get the play source.
     *
     * @return the playSource value.
     */
    public PlaySource getPlaySource() {
        return this.playSource;
    }

    /**
     * Get the play sources.
     *
     * @return the playSources value.
     */
    public List<PlaySource> getPlaySources() {
        return Collections.singletonList(playSource);
    }

    /**
     * Get the list of targets to play to.
     *
     * @return the playTo value.
     */
    public List<CommunicationIdentifier> getPlayTo() {
        return this.playTo;
    }

    /**
     * Get the loop property: The option to play the provided audio source in loop when set to true.
     *
     * @return the loop value.
     */
    public boolean isLoop() {
        return this.loop;
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
     * Set the loop property: The option to play the provided audio source in loop when set to true.
     *
     * @param loop the loop value to set.
     * @return the PlayOptionsInternal object itself.
     */
    public PlayOptions setLoop(boolean loop) {
        this.loop = loop;
        return this;
    }

    /**
     * Set the operationContext property.
     *
     * @param operationContext the operationContext value to set.
     * @return the PlayOptionsInternal object itself.
     */
    public PlayOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import java.util.ArrayList;
import java.util.List;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Fluent;

/** The PlayOptions model. */
@Fluent
public final class PlayOptions {
    /*
     * A List of {@link PlaySource} representing the sources to play.
     * Currently only single play source per request is supported.
     */
    private final List<PlaySource> playSources;

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
     * Set a callback URI that overrides the default callback URI set by CreateCall/AnswerCall for this operation.
     * This setup is per-action. If this is not set, the default callback URI set by CreateCall/AnswerCall will be used.
     */
    private String operationCallbackUrl;

    /**
     * Constructor
     * @param playSources A List of {@link PlaySource} representing the sources to play.
     * @param playTo The targets to play to.
     */
    public PlayOptions(List<PlaySource> playSources, List<CommunicationIdentifier> playTo) {
        this.playSources = playSources;
        this.playTo = playTo;
    }

    /**
     * Constructor
     * @param playSource A {@link PlaySource} representing the source to play.
     * @param playTo The targets to play to.
     */
    public PlayOptions(PlaySource playSource, List<CommunicationIdentifier> playTo) {
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

    /**
     * Get the overridden call back URL override for operation.
     *
     * @return the operationCallbackUrl
     */
    public String getOperationCallbackUrl() {
        return operationCallbackUrl;
    }

    /**
     * Set a callback URI that overrides the default callback URI set by CreateCall/AnswerCall for this operation.
     * This setup is per-action. If this is not set, the default callback URI set by CreateCall/AnswerCall will be used.
     *
     * @param operationCallbackUrl the operationCallbackUrl to set
     * @return the PlayOptions object itself.
     */
    public PlayOptions setOperationCallbackUrl(String operationCallbackUrl) {
        this.operationCallbackUrl = operationCallbackUrl;
        return this;
    }
}

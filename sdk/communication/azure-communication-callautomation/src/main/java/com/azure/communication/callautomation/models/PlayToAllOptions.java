// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import java.util.ArrayList;
import java.util.List;

import com.azure.core.annotation.Fluent;

/** The PlayToAllOptions model. */
@Fluent
public final class PlayToAllOptions {
    /*
     * A List of {@link PlaySource} representing the sources to play.
     * Currently only single play source per request is supported.
     */
    private final List<PlaySource> playSources;

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

    /*
     * If set play can barge into other existing queued-up/currently-processing requests.
    */
    private Boolean interruptCallMediaOperation;

    /**
     * Constructor
     * @param playSources A List of {@link PlaySource} representing the sources to play.
     */
    public PlayToAllOptions(List<PlaySource> playSources) {
        this.playSources = playSources;
    }

    /**
     * Constructor
     * @param playSource A {@link PlaySource} representing the source to play.
     */
    public PlayToAllOptions(PlaySource playSource) {
        this.playSources = new ArrayList<>();
        this.playSources.add(playSource);
    }

    /**
     * Get the play sources.
     *
     * @return the playSource value.
     */
    public List<PlaySource> getPlaySources() {
        return this.playSources;
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
     * Get the overridden call back URL override for operation.
     *
     * @return the operationCallbackUrl
     */
    public String getOperationCallbackUrl() {
        return operationCallbackUrl;
    }

    /**
     * Set the loop property: The option to play the provided audio source in loop when set to true.
     *
     * @param loop the loop value to set.
     * @return the PlayOptionsInternal object itself.
     */
    public PlayToAllOptions setLoop(boolean loop) {
        this.loop = loop;
        return this;
    }

    /**
     * Set the operationContext property.
     *
     * @param operationContext the operationContext value to set.
     * @return the PlayOptionsInternal object itself.
     */
    public PlayToAllOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }

    /**
     * Set a callback URI that overrides the default callback URI set by CreateCall/AnswerCall for this operation.
     * This setup is per-action. If this is not set, the default callback URI set by CreateCall/AnswerCall will be used.
     *
     * @param operationCallbackUrl the operationCallbackUrl to set
     * @return the PlayToAllOptions object itself.
     */
    public PlayToAllOptions setOperationCallbackUrl(String operationCallbackUrl) {
        this.operationCallbackUrl = operationCallbackUrl;
        return this;
    }

     /**
     * Get the interruptCallMediaOperation property: If set play can barge into other existing
     * queued-up/currently-processing requests.
     *
     * @return the interruptCallMediaOperation value.
     */
    public Boolean isInterruptCallMediaOperation() {
        return this.interruptCallMediaOperation;
    }

    /**
     * Set the interruptCallMediaOperation property: If set play can barge into other existing
     * queued-up/currently-processing requests.
     *
     * @param interruptCallMediaOperation the interruptCallMediaOperation value to set.
     * @return the PlayOptionsInternal object itself.
     */
    public PlayToAllOptions setInterruptCallMediaOperation(Boolean interruptCallMediaOperation) {
        this.interruptCallMediaOperation = interruptCallMediaOperation;
        return this;
    }
}

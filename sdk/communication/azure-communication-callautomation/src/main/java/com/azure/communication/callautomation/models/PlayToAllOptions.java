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
}

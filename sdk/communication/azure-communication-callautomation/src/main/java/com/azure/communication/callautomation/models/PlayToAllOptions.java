// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;

import java.util.Collections;
import java.util.List;

/** The PlayToAllOptions model. */
@Fluent
public final class PlayToAllOptions {
    /*
     * A {@link PlaySource} representing the source to play.
     */
    private final PlaySource playSource;

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
        this(playSources.get(0));
    }

    /**
     * Constructor
     * @param playSource A {@link PlaySource} representing the source to play.
     */
    public PlayToAllOptions(PlaySource playSource) {
        this.playSource = playSource;
    }

    /**
     * Get the play sources.
     *
     * @return the playSource value.
     */
    public List<PlaySource> getPlaySources() {
        return Collections.singletonList(playSource);
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

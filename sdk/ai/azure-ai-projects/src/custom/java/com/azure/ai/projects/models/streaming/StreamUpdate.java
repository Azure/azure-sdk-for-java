// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects.models.streaming;

import com.azure.ai.projects.models.AgentStreamEvent;

/**
 * Parent class for all stream updates types.
 */
public abstract class StreamUpdate {

    /**
     * The kind of stream update. This can take any value of the {@link AgentStreamEvent} enum.
     */
    private final AgentStreamEvent kind;

    /**
     * We always want to know to which {@link AgentStreamEvent} this StreamUpdate is associated, therefore we enforce
     * any deriving class to supply it upon construction of a new instance.
     *
     * @param kind The kind of stream update.
     */
    public StreamUpdate(AgentStreamEvent kind) {
        this.kind = kind;
    }

    /**
     * Returns what kind of StreamUpdate this is. See {@link AgentStreamEvent} for possible values.
     *
     * @return the kind of stream update.
     */
    public AgentStreamEvent getKind() {
        return kind;
    }
}

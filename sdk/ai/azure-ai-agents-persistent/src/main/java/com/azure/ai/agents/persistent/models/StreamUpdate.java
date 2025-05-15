// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent.models;

/**
 * Parent class for all stream updates types.
 */
public abstract class StreamUpdate {

    /**
     * The kind of stream update. This can take any value of the {@link PersistentAgentStreamEvent} enum.
     */
    private final PersistentAgentStreamEvent kind;

    /**
     * We always want to know to which {@link PersistentAgentStreamEvent} this StreamUpdate is associated, therefore we enforce
     * any deriving class to supply it upon construction of a new instance.
     *
     * @param kind The kind of stream update.
     */
    public StreamUpdate(PersistentAgentStreamEvent kind) {
        this.kind = kind;
    }

    /**
     * Returns what kind of StreamUpdate this is. See {@link PersistentAgentStreamEvent} for possible values.
     *
     * @return the kind of stream update.
     */
    public PersistentAgentStreamEvent getKind() {
        return kind;
    }
}

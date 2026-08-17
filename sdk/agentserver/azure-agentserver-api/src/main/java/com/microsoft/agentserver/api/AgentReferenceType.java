// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Enumeration of agent reference types used in the agent server protocol.
 */
public enum AgentReferenceType {

    AGENT_REFERENCE("agent_reference");

    private final String value;

    AgentReferenceType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static AgentReferenceType fromValue(String value) {
        for (AgentReferenceType b : AgentReferenceType.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

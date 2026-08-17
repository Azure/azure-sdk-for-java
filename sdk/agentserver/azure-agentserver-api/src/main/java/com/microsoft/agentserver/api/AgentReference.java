// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a reference to an agent in the Foundry platform.
 * Contains identifying information such as name, version, and label.
 */
public record AgentReference(AgentReferenceType type, String name, String version, String label) {
    @JsonCreator
    @JsonIgnoreProperties(ignoreUnknown = true)
    public AgentReference(
        @JsonProperty("type") AgentReferenceType type,
        @JsonProperty("name") String name,
        @JsonProperty("version") String version,
        @JsonProperty("label") String label) {
        this.type = type;
        this.name = name;
        this.version = version;
        this.label = label;
    }
}

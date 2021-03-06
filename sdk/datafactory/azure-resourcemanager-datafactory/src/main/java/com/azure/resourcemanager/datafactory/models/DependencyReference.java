// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.datafactory.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/** Referenced dependency. */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
    defaultImpl = DependencyReference.class)
@JsonTypeName("DependencyReference")
@JsonSubTypes({
    @JsonSubTypes.Type(name = "TriggerDependencyReference", value = TriggerDependencyReference.class),
    @JsonSubTypes.Type(
        name = "SelfDependencyTumblingWindowTriggerReference",
        value = SelfDependencyTumblingWindowTriggerReference.class)
})
@Immutable
public class DependencyReference {
    @JsonIgnore private final ClientLogger logger = new ClientLogger(DependencyReference.class);

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
    }
}

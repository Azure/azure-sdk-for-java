// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.iotcentral.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

/** Managed service identity (either system assigned, or none). */
@Fluent
public class SystemAssignedServiceIdentity {
    /*
     * The service principal ID of the system assigned identity. This property
     * will only be provided for a system assigned identity.
     */
    @JsonProperty(value = "principalId", access = JsonProperty.Access.WRITE_ONLY)
    private UUID principalId;

    /*
     * The tenant ID of the system assigned identity. This property will only
     * be provided for a system assigned identity.
     */
    @JsonProperty(value = "tenantId", access = JsonProperty.Access.WRITE_ONLY)
    private UUID tenantId;

    /*
     * Type of managed service identity (either system assigned, or none).
     */
    @JsonProperty(value = "type", required = true)
    private SystemAssignedServiceIdentityType type;

    /**
     * Get the principalId property: The service principal ID of the system assigned identity. This property will only
     * be provided for a system assigned identity.
     *
     * @return the principalId value.
     */
    public UUID principalId() {
        return this.principalId;
    }

    /**
     * Get the tenantId property: The tenant ID of the system assigned identity. This property will only be provided for
     * a system assigned identity.
     *
     * @return the tenantId value.
     */
    public UUID tenantId() {
        return this.tenantId;
    }

    /**
     * Get the type property: Type of managed service identity (either system assigned, or none).
     *
     * @return the type value.
     */
    public SystemAssignedServiceIdentityType type() {
        return this.type;
    }

    /**
     * Set the type property: Type of managed service identity (either system assigned, or none).
     *
     * @param type the type value to set.
     * @return the SystemAssignedServiceIdentity object itself.
     */
    public SystemAssignedServiceIdentity withType(SystemAssignedServiceIdentityType type) {
        this.type = type;
        return this;
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (type() == null) {
            throw LOGGER
                .logExceptionAsError(
                    new IllegalArgumentException(
                        "Missing required property type in model SystemAssignedServiceIdentity"));
        }
    }

    private static final ClientLogger LOGGER = new ClientLogger(SystemAssignedServiceIdentity.class);
}

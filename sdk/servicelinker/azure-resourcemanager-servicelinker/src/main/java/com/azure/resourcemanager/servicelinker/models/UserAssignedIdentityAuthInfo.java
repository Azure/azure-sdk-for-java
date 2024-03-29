// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.servicelinker.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/** The authentication info when authType is userAssignedIdentity. */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "authType")
@JsonTypeName("userAssignedIdentity")
@Fluent
public final class UserAssignedIdentityAuthInfo extends AuthInfoBase {
    /*
     * Client Id for userAssignedIdentity.
     */
    @JsonProperty(value = "clientId")
    private String clientId;

    /*
     * Subscription id for userAssignedIdentity.
     */
    @JsonProperty(value = "subscriptionId")
    private String subscriptionId;

    /**
     * Get the clientId property: Client Id for userAssignedIdentity.
     *
     * @return the clientId value.
     */
    public String clientId() {
        return this.clientId;
    }

    /**
     * Set the clientId property: Client Id for userAssignedIdentity.
     *
     * @param clientId the clientId value to set.
     * @return the UserAssignedIdentityAuthInfo object itself.
     */
    public UserAssignedIdentityAuthInfo withClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     * Get the subscriptionId property: Subscription id for userAssignedIdentity.
     *
     * @return the subscriptionId value.
     */
    public String subscriptionId() {
        return this.subscriptionId;
    }

    /**
     * Set the subscriptionId property: Subscription id for userAssignedIdentity.
     *
     * @param subscriptionId the subscriptionId value to set.
     * @return the UserAssignedIdentityAuthInfo object itself.
     */
    public UserAssignedIdentityAuthInfo withSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    @Override
    public void validate() {
        super.validate();
    }
}

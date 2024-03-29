// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.streamanalytics.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.streamanalytics.models.PrivateLinkConnectionState;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Bag of properties defining a privatelinkServiceConnection.
 */
@Fluent
public final class PrivateLinkServiceConnectionProperties {
    /*
     * The resource id of the private link service. Required on PUT (CreateOrUpdate) requests.
     */
    @JsonProperty(value = "privateLinkServiceId")
    private String privateLinkServiceId;

    /*
     * The ID(s) of the group(s) obtained from the remote resource that this private endpoint should connect to.
     * Required on PUT (CreateOrUpdate) requests.
     */
    @JsonProperty(value = "groupIds")
    private List<String> groupIds;

    /*
     * A message passed to the owner of the remote resource with this connection request. Restricted to 140 chars.
     */
    @JsonProperty(value = "requestMessage", access = JsonProperty.Access.WRITE_ONLY)
    private String requestMessage;

    /*
     * A collection of read-only information about the state of the connection to the private remote resource.
     */
    @JsonProperty(value = "privateLinkServiceConnectionState")
    private PrivateLinkConnectionState privateLinkServiceConnectionState;

    /**
     * Creates an instance of PrivateLinkServiceConnectionProperties class.
     */
    public PrivateLinkServiceConnectionProperties() {
    }

    /**
     * Get the privateLinkServiceId property: The resource id of the private link service. Required on PUT
     * (CreateOrUpdate) requests.
     * 
     * @return the privateLinkServiceId value.
     */
    public String privateLinkServiceId() {
        return this.privateLinkServiceId;
    }

    /**
     * Set the privateLinkServiceId property: The resource id of the private link service. Required on PUT
     * (CreateOrUpdate) requests.
     * 
     * @param privateLinkServiceId the privateLinkServiceId value to set.
     * @return the PrivateLinkServiceConnectionProperties object itself.
     */
    public PrivateLinkServiceConnectionProperties withPrivateLinkServiceId(String privateLinkServiceId) {
        this.privateLinkServiceId = privateLinkServiceId;
        return this;
    }

    /**
     * Get the groupIds property: The ID(s) of the group(s) obtained from the remote resource that this private
     * endpoint should connect to. Required on PUT (CreateOrUpdate) requests.
     * 
     * @return the groupIds value.
     */
    public List<String> groupIds() {
        return this.groupIds;
    }

    /**
     * Set the groupIds property: The ID(s) of the group(s) obtained from the remote resource that this private
     * endpoint should connect to. Required on PUT (CreateOrUpdate) requests.
     * 
     * @param groupIds the groupIds value to set.
     * @return the PrivateLinkServiceConnectionProperties object itself.
     */
    public PrivateLinkServiceConnectionProperties withGroupIds(List<String> groupIds) {
        this.groupIds = groupIds;
        return this;
    }

    /**
     * Get the requestMessage property: A message passed to the owner of the remote resource with this connection
     * request. Restricted to 140 chars.
     * 
     * @return the requestMessage value.
     */
    public String requestMessage() {
        return this.requestMessage;
    }

    /**
     * Get the privateLinkServiceConnectionState property: A collection of read-only information about the state of the
     * connection to the private remote resource.
     * 
     * @return the privateLinkServiceConnectionState value.
     */
    public PrivateLinkConnectionState privateLinkServiceConnectionState() {
        return this.privateLinkServiceConnectionState;
    }

    /**
     * Set the privateLinkServiceConnectionState property: A collection of read-only information about the state of the
     * connection to the private remote resource.
     * 
     * @param privateLinkServiceConnectionState the privateLinkServiceConnectionState value to set.
     * @return the PrivateLinkServiceConnectionProperties object itself.
     */
    public PrivateLinkServiceConnectionProperties
        withPrivateLinkServiceConnectionState(PrivateLinkConnectionState privateLinkServiceConnectionState) {
        this.privateLinkServiceConnectionState = privateLinkServiceConnectionState;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (privateLinkServiceConnectionState() != null) {
            privateLinkServiceConnectionState().validate();
        }
    }
}

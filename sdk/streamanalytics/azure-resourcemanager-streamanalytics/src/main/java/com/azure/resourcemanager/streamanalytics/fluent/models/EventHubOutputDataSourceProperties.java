// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.streamanalytics.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.streamanalytics.models.AuthenticationMode;
import com.azure.resourcemanager.streamanalytics.models.EventHubDataSourceProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * The properties that are associated with an Event Hub output.
 */
@Fluent
public final class EventHubOutputDataSourceProperties extends EventHubDataSourceProperties {
    /*
     * The key/column that is used to determine to which partition to send event data.
     */
    @JsonProperty(value = "partitionKey")
    private String partitionKey;

    /*
     * The properties associated with this Event Hub output.
     */
    @JsonProperty(value = "propertyColumns")
    private List<String> propertyColumns;

    /**
     * Creates an instance of EventHubOutputDataSourceProperties class.
     */
    public EventHubOutputDataSourceProperties() {
    }

    /**
     * Get the partitionKey property: The key/column that is used to determine to which partition to send event data.
     * 
     * @return the partitionKey value.
     */
    public String partitionKey() {
        return this.partitionKey;
    }

    /**
     * Set the partitionKey property: The key/column that is used to determine to which partition to send event data.
     * 
     * @param partitionKey the partitionKey value to set.
     * @return the EventHubOutputDataSourceProperties object itself.
     */
    public EventHubOutputDataSourceProperties withPartitionKey(String partitionKey) {
        this.partitionKey = partitionKey;
        return this;
    }

    /**
     * Get the propertyColumns property: The properties associated with this Event Hub output.
     * 
     * @return the propertyColumns value.
     */
    public List<String> propertyColumns() {
        return this.propertyColumns;
    }

    /**
     * Set the propertyColumns property: The properties associated with this Event Hub output.
     * 
     * @param propertyColumns the propertyColumns value to set.
     * @return the EventHubOutputDataSourceProperties object itself.
     */
    public EventHubOutputDataSourceProperties withPropertyColumns(List<String> propertyColumns) {
        this.propertyColumns = propertyColumns;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EventHubOutputDataSourceProperties withEventHubName(String eventHubName) {
        super.withEventHubName(eventHubName);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EventHubOutputDataSourceProperties withPartitionCount(Integer partitionCount) {
        super.withPartitionCount(partitionCount);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EventHubOutputDataSourceProperties withServiceBusNamespace(String serviceBusNamespace) {
        super.withServiceBusNamespace(serviceBusNamespace);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EventHubOutputDataSourceProperties withSharedAccessPolicyName(String sharedAccessPolicyName) {
        super.withSharedAccessPolicyName(sharedAccessPolicyName);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EventHubOutputDataSourceProperties withSharedAccessPolicyKey(String sharedAccessPolicyKey) {
        super.withSharedAccessPolicyKey(sharedAccessPolicyKey);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EventHubOutputDataSourceProperties withAuthenticationMode(AuthenticationMode authenticationMode) {
        super.withAuthenticationMode(authenticationMode);
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

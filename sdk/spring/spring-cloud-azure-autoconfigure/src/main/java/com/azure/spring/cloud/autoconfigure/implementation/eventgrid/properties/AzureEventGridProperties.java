// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.eventgrid.properties;

import com.azure.messaging.eventgrid.EventGridServiceVersion;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.AbstractAzureHttpConfigurationProperties;
import com.azure.spring.cloud.service.implementation.eventgrid.properties.EventGridPublisherClientProperties;

public class AzureEventGridProperties extends AbstractAzureHttpConfigurationProperties implements EventGridPublisherClientProperties {

    public static final String PREFIX = "spring.cloud.azure.eventgrid";

    /**
     * Endpoint of an Azure Event Grid Topic or Domain (can be found on Azure Portal). For instance, 'https://{domain-or-topic-name}.xxx.eventgrid.azure.net/api/eventseventgrid.azure.net/api/events'.
     */
    private String endpoint;
    /**
     * Key to authenticate for accessing the Event Grid Topic or Domain.
     */
    private String key;
    /**
     * Shared access signatures (SAS) token used to authorize requests sent to the service.
     */
    private String sasToken;
    /**
     * Event Grid service version used when making API requests.
     */
    private EventGridServiceVersion serviceVersion;

    /**
     * The schema used to publish events. Could be 'EVENT_GRID_EVENT', 'CLOUD_EVENT', or 'CUSTOM_EVENT'.
     */
    private EventSchema eventSchema = EventSchema.EVENT_GRID_EVENT;


    public enum EventSchema {
        EVENT_GRID_EVENT,
        CLOUD_EVENT,
        CUSTOM_EVENT;

    }

    @Override
    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String getSasToken() {
        return sasToken;
    }

    public void setSasToken(String sasToken) {
        this.sasToken = sasToken;
    }

    @Override
    public EventGridServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(EventGridServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public EventSchema getEventSchema() {
        return eventSchema;
    }

    public void setEventSchema(EventSchema eventSchema) {
        this.eventSchema = eventSchema;
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.eventgrid;

import com.azure.messaging.eventgrid.EventGridServiceVersion;
import com.azure.spring.cloud.core.implementation.properties.AzureHttpSdkProperties;
import com.azure.spring.cloud.service.implementation.eventgrid.properties.EventGridPublisherClientProperties;

/**
 * Properties for Azure Storage File Share service.
 */
class AzureEventGridTestProperties extends AzureHttpSdkProperties implements EventGridPublisherClientProperties {

    private String endpoint;
    private EventGridServiceVersion serviceVersion;
    private String key;
    private String sasToken;

    @Override
    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public EventGridServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(EventGridServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
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
}

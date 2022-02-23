// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.properties.core.client;

import com.azure.spring.core.aware.ClientOptionsAware;
import com.azure.spring.core.properties.client.HeaderProperties;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ClientConfigurationProperties implements ClientOptionsAware.Client {

    /**
     * Represents current application and is used for telemetry/monitoring purposes.
     */
    private String applicationId;
    /**
     * Comma-delimited list of headers applied to each request sent with client.
     */
    private final List<HeaderProperties> headers = new ArrayList<>();

    @Override
    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    @Override
    public List<HeaderProperties> getHeaders() {
        return headers;
    }

}

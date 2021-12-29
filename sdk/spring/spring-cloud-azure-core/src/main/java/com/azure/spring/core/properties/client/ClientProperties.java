// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties.client;

import com.azure.spring.core.aware.ClientAware;

import java.util.ArrayList;
import java.util.List;

/**
 * Properties shared by all azure service client builders.
 */
public class ClientProperties implements ClientAware.Client {

    /**
     * Represents current application and is used for telemetry/monitoring purposes.
     */
    private String applicationId;
    /**
     * List of headers applied to each request sent with client.
     */
    private final List<HeaderProperties> headers = new ArrayList<>();

    /**
     * Get the application id.
     * @return The application id.
     */
    public String getApplicationId() {
        return applicationId;
    }

    /**
     * Set the application id.
     * @param applicationId The application id.
     */
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    /**
     * Get the headers.
     * @return The headers.
     */
    public List<HeaderProperties> getHeaders() {
        return headers;
    }

}

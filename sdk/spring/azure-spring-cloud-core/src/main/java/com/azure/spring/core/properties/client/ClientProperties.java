// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties.client;

import com.azure.spring.core.properties.HeaderProperties;

import java.util.List;

/**
 * Properties shared by all azure service client builders.
 */
public class ClientProperties {

    private String applicationId;

    private List<HeaderProperties> headers;

    public String getApplicationId() {
        return applicationId;
    }

    public List<HeaderProperties> getHeaders() {
        return headers;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public void setHeaders(List<HeaderProperties> headers) {
        this.headers = headers;
    }
}

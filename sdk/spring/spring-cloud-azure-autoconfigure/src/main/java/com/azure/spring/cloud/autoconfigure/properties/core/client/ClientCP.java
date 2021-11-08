// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.properties.core.client;

import com.azure.spring.core.aware.ClientAware;
import com.azure.spring.core.properties.client.HeaderProperties;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ClientCP implements ClientAware.Client {

    private String applicationId;
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

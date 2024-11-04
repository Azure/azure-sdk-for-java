// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.properties.core.client;

import com.azure.spring.cloud.core.provider.ClientOptionsProvider;

/**
 *
 */
public class ClientConfigurationProperties implements ClientOptionsProvider.ClientOptions {

    /**
     * Represents current application and is used for telemetry/monitoring purposes.
     */
    private String applicationId;

    @Override
    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

}

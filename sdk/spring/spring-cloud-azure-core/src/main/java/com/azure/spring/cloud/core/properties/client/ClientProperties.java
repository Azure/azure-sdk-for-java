// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.properties.client;

import com.azure.spring.cloud.core.provider.ClientOptionsProvider;

/**
 * Properties shared by all azure service client builders.
 */
public class ClientProperties implements ClientOptionsProvider.ClientOptions {

    /**
     * Represents current application and is used for telemetry/monitoring purposes.
     */
    private String applicationId;

    @Override
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

}

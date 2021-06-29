// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.validation.annotation.Validated;

/**
 * Common properties for Azure SDK clients.
 */
@Validated
@ConfigurationProperties(AzureSpringProperties.PREFIX)
@Import(CredentialProperties.class)
public class AzureSpringProperties {

    public static final String PREFIX = "spring.cloud.azure";

    /**
     * Name of the Azure cloud to connect to.
     */
    private String environment = "Azure";

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }
}

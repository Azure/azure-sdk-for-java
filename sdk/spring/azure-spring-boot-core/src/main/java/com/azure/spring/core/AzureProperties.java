// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core;


import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.util.logging.LogLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.validation.annotation.Validated;

/**
 * Azure related properties.
 */
@Validated
@ConfigurationProperties(AzureProperties.PREFIX)
@Import({CredentialProperties.class, LogProperties.class})
public class AzureProperties {

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

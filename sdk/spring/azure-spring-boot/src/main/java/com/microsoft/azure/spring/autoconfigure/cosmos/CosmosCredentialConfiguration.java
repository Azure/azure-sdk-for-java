// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.autoconfigure.cosmos;

import com.azure.core.credential.AzureKeyCredential;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter({CosmosAutoConfiguration.class})
@ConditionalOnProperty(prefix = "azure.cosmos", name = "credential")
public class CosmosCredentialConfiguration {

    private final CosmosProperties properties;

    private AzureKeyCredential credential;

    public CosmosCredentialConfiguration(CosmosProperties properties) {
        this.properties = properties;
        this.credential = new AzureKeyCredential(properties.getKey());
    }

    /**
     * Switch to the credential to authorize cosmos request
     */
    public void switchToCredential() {
        this.credential.update(properties.getCredential());
    }

    /**
     * Switch to the key to authorize cosmos request
     */
    public void switchToKey() {
        this.credential.update(properties.getKey());
    }

    /**
     * Switch to the key parameter to authorize cosmos request
     * @param key parameter
     */
    public void switchKey(String key) {
        this.credential.update(key);
    }
}

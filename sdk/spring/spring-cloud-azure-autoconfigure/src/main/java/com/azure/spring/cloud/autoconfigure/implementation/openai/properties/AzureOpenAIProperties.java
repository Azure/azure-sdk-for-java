// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.openai.properties;

import com.azure.ai.openai.OpenAIServiceVersion;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.AbstractAzureHttpConfigurationProperties;
import com.azure.spring.cloud.service.implementation.openai.OpenAIClientProperties;

/**
 * Configuration properties for Azure OpenAI.
 */
public class AzureOpenAIProperties extends AbstractAzureHttpConfigurationProperties implements OpenAIClientProperties {

    public static final String PREFIX = "spring.cloud.azure.openai";

    /**
     * Endpoint of the Azure OpenAI. For instance, 'https://{azure-openai-name}.openai.azure.com/'.
     */
    private String endpoint;

    /**
     * Azure OpenAI service version used when making API requests.
     */
    private OpenAIServiceVersion serviceVersion;

    /**
     * The API key to authenticate the non-Azure OpenAI service (https://platform.openai.com/docs/api-reference/authentication).
     */
    private String nonAzureOpenAIKey;

    /**
     * Key to authenticate for accessing the Azure OpenAI.
     */
    private String key;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public OpenAIServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(OpenAIServiceVersion serviceVersion) {
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
    public String getNonAzureOpenAIKey() {
        return nonAzureOpenAIKey;
    }

    public void setNonAzureOpenAIKey(String nonAzureOpenAIKey) {
        this.nonAzureOpenAIKey = nonAzureOpenAIKey;
    }
}

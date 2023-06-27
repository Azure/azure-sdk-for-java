// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.openai;

import com.azure.ai.openai.OpenAIServiceVersion;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.core.provider.RetryOptionsProvider;
import com.azure.spring.cloud.core.provider.authentication.KeyProvider;

/**
 * Configuration properties for Azure OpenAI.
 */
public interface OpenAIClientProperties extends AzureProperties, RetryOptionsProvider, KeyProvider {

    /**
     * Get the azure openai endpoint.
     * @return the azure openai endpoint.
     */
    String getEndpoint();

    /**
     * Get the Azure OpenAI service version used when making API requests.
     * @return the Azure OpenAI service version used when making API requests.
     */
    OpenAIServiceVersion getServiceVersion();

    /**
     * Get the API key to authenticate the non-Azure OpenAI service (https://platform.openai.com/docs/api-reference/authentication).
     * @return The API key to authenticate the non-Azure OpenAI service (https://platform.openai.com/docs/api-reference/authentication).
     */
    String getNonAzureOpenAIKey();
}

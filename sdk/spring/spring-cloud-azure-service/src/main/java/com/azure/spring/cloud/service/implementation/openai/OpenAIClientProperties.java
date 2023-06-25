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
     * Get the azure openai service version.
     * @return the azure openai service version.
     */
    OpenAIServiceVersion getServiceVersion();

    /**
     * Get the NonAzureOpenAiKeyCredential used for public OpenAi authentication.
     * @return the NonAzureOpenAiKeyCredential used for public OpenAi authentication.
     */
    String getNonAzureOpenAIKey();
}

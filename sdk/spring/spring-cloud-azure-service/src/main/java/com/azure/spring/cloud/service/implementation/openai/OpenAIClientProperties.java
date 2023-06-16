// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.openai;

import com.azure.ai.openai.OpenAIServiceVersion;
import com.azure.ai.openai.models.NonAzureOpenAIKeyCredential;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.core.provider.RetryOptionsProvider;
import com.azure.spring.cloud.core.provider.authentication.KeyProvider;

public interface OpenAIClientProperties extends AzureProperties, RetryOptionsProvider, KeyProvider {

    String getEndpoint();

    OpenAIServiceVersion getServiceVersion();

    String getNonAzureOpenAIKeyCredential();
}

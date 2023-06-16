// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.openai;

import com.azure.ai.openai.OpenAIServiceVersion;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.AbstractAzureHttpConfigurationProperties;
import com.azure.spring.cloud.service.implementation.openai.OpenAIClientProperties;

public class AzureOpenAIProperties extends AbstractAzureHttpConfigurationProperties implements OpenAIClientProperties {

    public static final String PREFIX = "spring.cloud.azure.openai";

    private String endpoint;

    private OpenAIServiceVersion serviceVersion;

    private String nonAzureOpenAIKeyCredential;

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
    public String getNonAzureOpenAIKeyCredential() {
        return nonAzureOpenAIKeyCredential;
    }

    public void setNonAzureOpenAIKeyCredential(String nonAzureOpenAIKeyCredential) {
        this.nonAzureOpenAIKeyCredential = nonAzureOpenAIKeyCredential;
    }
}

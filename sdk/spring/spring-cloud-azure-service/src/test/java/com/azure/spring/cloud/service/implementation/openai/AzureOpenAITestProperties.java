// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.openai;

import com.azure.ai.openai.OpenAIServiceVersion;
import com.azure.spring.cloud.core.implementation.properties.AzureHttpSdkProperties;

class AzureOpenAITestProperties extends AzureHttpSdkProperties implements OpenAIClientProperties {

    private String endpoint;
    private OpenAIServiceVersion serviceVersion;
    private String nonAzureOpenAIKeyCredential;
    private String key;

    @Override
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public OpenAIServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(OpenAIServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    @Override
    public String getNonAzureOpenAIKey() {
        return nonAzureOpenAIKeyCredential;
    }

    public void setNonAzureOpenAIKeyCredential(String nonAzureOpenAIKeyCredential) {
        this.nonAzureOpenAIKeyCredential = nonAzureOpenAIKeyCredential;
    }
}

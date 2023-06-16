// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.openai.credential;

import com.azure.ai.openai.models.NonAzureOpenAIKeyCredential;
import com.azure.spring.cloud.core.credential.AzureCredentialResolver;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.service.implementation.openai.OpenAIClientProperties;
import org.springframework.util.StringUtils;

public class NonAzureOpenAIKeyCredentialResolver implements AzureCredentialResolver<NonAzureOpenAIKeyCredential> {

    @Override
    public NonAzureOpenAIKeyCredential resolve(AzureProperties properties) {
        if (!isResolvable(properties)) {
            return null;
        }
        String key = ((OpenAIClientProperties) properties).getNonAzureOpenAIKeyCredential();
        if (!StringUtils.hasText(key)) {
            return null;
        }
        return new NonAzureOpenAIKeyCredential(key);
    }

    @Override
    public boolean isResolvable(AzureProperties properties) {
        return properties instanceof OpenAIClientProperties;
    }

}

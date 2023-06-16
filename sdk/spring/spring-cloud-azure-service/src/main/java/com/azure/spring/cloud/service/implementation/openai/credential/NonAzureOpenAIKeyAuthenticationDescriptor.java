// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.openai.credential;

import com.azure.ai.openai.models.NonAzureOpenAIKeyCredential;
import com.azure.spring.cloud.core.credential.AzureCredentialResolver;
import com.azure.spring.cloud.core.implementation.credential.descriptor.AuthenticationDescriptor;

import java.util.function.Consumer;

public class NonAzureOpenAIKeyAuthenticationDescriptor implements AuthenticationDescriptor<NonAzureOpenAIKeyCredential> {

    private final Consumer<NonAzureOpenAIKeyCredential> consumer;

    public NonAzureOpenAIKeyAuthenticationDescriptor(Consumer<NonAzureOpenAIKeyCredential> consumer) {
        this.consumer = consumer;
    }

    @Override
    public Class<NonAzureOpenAIKeyCredential> getAzureCredentialType() {
        return NonAzureOpenAIKeyCredential.class;
    }

    @Override
    public AzureCredentialResolver<NonAzureOpenAIKeyCredential> getAzureCredentialResolver() {
        return new NonAzureOpenAIKeyCredentialResolver();
    }

    @Override
    public Consumer<NonAzureOpenAIKeyCredential> getConsumer() {
        return consumer;
    }
}

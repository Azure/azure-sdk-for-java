// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation;

import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.spring.cloud.core.implementation.factory.AbstractAzureHttpClientBuilderFactory;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.service.implementation.core.http.TestHttpClientProvider;
import com.azure.spring.cloud.service.implementation.core.http.TestPerCallHttpPipelinePolicy;
import com.azure.spring.cloud.service.implementation.core.http.TestPerRetryHttpPipelinePolicy;
import org.junit.jupiter.api.Test;
import org.mockito.verification.VerificationMode;

import static org.mockito.Mockito.times;

public abstract class AzureHttpClientBuilderFactoryBaseTests<B, P extends AzureProperties,
                                                                  T extends AbstractAzureHttpClientBuilderFactory<B>>
    extends AzureServiceClientBuilderFactoryBaseTests<B, P, T> {

    @Test
    void testHttpClientConfigured() {
        P properties = createMinimalServiceProperties();

        final T builderFactory = getClientBuilderFactoryWithMockBuilder(properties);

        builderFactory.setHttpClientProvider(new TestHttpClientProvider());

        final B builder = builderFactory.build();


        verifyHttpClientCalled(builder, times(1));
    }

    @Test
    void testDefaultHttpPipelinePoliciesConfigured() {
        P properties = createMinimalServiceProperties();

        final T builderFactory = getClientBuilderFactoryWithMockBuilder(properties);

        TestPerCallHttpPipelinePolicy perCallHttpPipelinePolicy = new TestPerCallHttpPipelinePolicy();
        TestPerRetryHttpPipelinePolicy perRetryHttpPipelinePolicy = new TestPerRetryHttpPipelinePolicy();
        builderFactory.addHttpPipelinePolicy(perCallHttpPipelinePolicy);
        builderFactory.addHttpPipelinePolicy(perRetryHttpPipelinePolicy);


        final B builder = builderFactory.build();

        verifyHttpPipelinePolicyAdded(builder, perCallHttpPipelinePolicy, times(1));
        verifyHttpPipelinePolicyAdded(builder, perRetryHttpPipelinePolicy, times(1));

    }

    protected abstract T getClientBuilderFactoryWithMockBuilder(P properties);

    protected abstract void verifyHttpClientCalled(B builder, VerificationMode mode);

    protected abstract void verifyHttpPipelinePolicyAdded(B builder, HttpPipelinePolicy policy, VerificationMode mode);

}

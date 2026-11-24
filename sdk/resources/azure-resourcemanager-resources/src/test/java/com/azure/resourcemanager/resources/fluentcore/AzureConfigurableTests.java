// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.ExponentialBackoff;
import com.azure.core.http.policy.FixedDelay;
import com.azure.core.http.policy.FixedDelayOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.RetryStrategy;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.models.AzureCloud;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.resources.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.time.Duration;

public class AzureConfigurableTests {

    @Test
    public void testRetryOptions() throws NoSuchFieldException, IllegalAccessException {
        // RetryOptions should take effect
        ResourceManager resourceManager = ResourceManager.configure()
            .withRetryOptions(new RetryOptions(new FixedDelayOptions(3, Duration.ofSeconds(1))))
            .withHttpClient(request -> Mono.just(new MockHttpResponse(request, 200)))
            .authenticate(new DefaultAzureCredentialBuilder().build(), new AzureProfile(AzureCloud.AZURE_PUBLIC_CLOUD))
            .withSubscription(Mockito.anyString());

        HttpPipeline httpPipeline = resourceManager.genericResources().manager().httpPipeline();
        validateRetryPolicy(httpPipeline, FixedDelay.class);

        // Default is RetryPolicy with ExponentialBackoff
        resourceManager = ResourceManager.configure()
            .withHttpClient(request -> Mono.just(new MockHttpResponse(request, 200)))
            .authenticate(new DefaultAzureCredentialBuilder().build(), new AzureProfile(AzureCloud.AZURE_PUBLIC_CLOUD))
            .withSubscription(Mockito.anyString());

        httpPipeline = resourceManager.genericResources().manager().httpPipeline();
        validateRetryPolicy(httpPipeline, ExponentialBackoff.class);

        resourceManager = ResourceManager.configure()
            .withHttpClient(request -> Mono.just(new MockHttpResponse(request, 200)))
            .authenticate(new DefaultAzureCredentialBuilder().build(), new AzureProfile(AzureCloud.AZURE_PUBLIC_CLOUD))
            .withSubscription(Mockito.anyString());

        httpPipeline = resourceManager.genericResources().manager().httpPipeline();
        validateRetryPolicy(httpPipeline, ExponentialBackoff.class);
    }

    private static void validateRetryPolicy(HttpPipeline httpPipeline, Class<?> retryStrategyClass)
        throws NoSuchFieldException, IllegalAccessException {
        Assertions.assertNotNull(httpPipeline);

        Field pipelinePoliciesField = HttpPipeline.class.getDeclaredField("pipelinePolicies");
        pipelinePoliciesField.setAccessible(true);
        HttpPipelinePolicy[] pipelinePolicies = (HttpPipelinePolicy[]) pipelinePoliciesField.get(httpPipeline);
        for (HttpPipelinePolicy pipelinePolicy : pipelinePolicies) {
            if (pipelinePolicy instanceof RetryPolicy) {
                validateRetryPolicy((RetryPolicy) pipelinePolicy, retryStrategyClass);
            }
        }
    }

    private static void validateRetryPolicy(RetryPolicy retryPolicy, Class<?> retryStrategyClass)
        throws NoSuchFieldException, IllegalAccessException {
        Assertions.assertNotNull(retryPolicy);

        Field retryStrategyField = RetryPolicy.class.getDeclaredField("retryStrategy");
        retryStrategyField.setAccessible(true);

        RetryStrategy retryStrategy = (RetryStrategy) retryStrategyField.get(retryPolicy);
        Assertions.assertTrue(retryStrategyClass.isAssignableFrom(retryStrategy.getClass()));
    }
}

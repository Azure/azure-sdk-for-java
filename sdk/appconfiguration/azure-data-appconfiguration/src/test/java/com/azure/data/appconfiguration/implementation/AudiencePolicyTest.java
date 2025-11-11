// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.SyncAsyncExtension;
import com.azure.core.test.annotation.SyncAsyncTest;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.http.NoOpHttpClient;
import com.azure.core.util.Context;
import com.azure.data.appconfiguration.models.ConfigurationAudience;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Unit tests for AudiencePolicy
 */
public class AudiencePolicyTest {
    private static final String LOCAL_HOST = "http://localhost";
    private static final String AAD_AUDIENCE_ERROR_CODE = "AADSTS500011";
    private static final String NO_AUDIENCE_ERROR_MESSAGE
        = "Unable to authenticate to Azure App Configuration. No authentication token audience was provided. "
            + "Please set an Audience in your ConfigurationClientBuilder for the target cloud. "
            + "For details on how to configure the authentication token audience visit "
            + "https://aka.ms/appconfig/client-token-audience.";

    private static final String INCORRECT_AUDIENCE_ERROR_MESSAGE
        = "Unable to authenticate to Azure App Configuration. An incorrect token audience was provided. "
            + "Please set the Audience in your ConfigurationClientBuilder to the appropriate audience for this cloud. "
            + "For details on how to configure the authentication token audience visit "
            + "https://aka.ms/appconfig/client-token-audience.";

    @SyncAsyncTest
    public void processWithoutException() {
        AudiencePolicy audiencePolicy = new AudiencePolicy(ConfigurationAudience.AZURE_PUBLIC_CLOUD);

        HttpPipelinePolicy testPolicy = (context, next) -> {
            return next.process();
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                return Mono.just(new MockHttpResponse(request, 200));
            }
        }).policies(audiencePolicy, testPolicy).build();

        SyncAsyncExtension.execute(() -> sendRequestSync(pipeline), () -> sendRequest(pipeline));
    }

    @Test
    public void processWithNonAudienceException() {
        AudiencePolicy audiencePolicy = new AudiencePolicy(ConfigurationAudience.AZURE_PUBLIC_CLOUD);

        HttpPipelinePolicy exceptionPolicy = (context, next) -> {
            HttpResponseException ex
                = new HttpResponseException("Some other error", new MockHttpResponse(context.getHttpRequest(), 401));
            return Mono.error(ex);
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient())
            .policies(audiencePolicy, exceptionPolicy)
            .build();

        StepVerifier.create(sendRequest(pipeline))
            .expectErrorMatches(throwable -> throwable instanceof HttpResponseException
                && throwable.getMessage().equals("Some other error"))
            .verify();

        // Test sync version
        HttpResponseException thrown = assertThrows(HttpResponseException.class, () -> sendRequestSync(pipeline));
        assertEquals("Some other error", thrown.getMessage());
    }

    @Test
    public void processWithAudienceExceptionAndNullAudience() {
        AudiencePolicy audiencePolicy = new AudiencePolicy(null);

        HttpPipelinePolicy exceptionPolicy = (context, next) -> {
            HttpResponseException ex = new HttpResponseException("Error " + AAD_AUDIENCE_ERROR_CODE + " occurred",
                new MockHttpResponse(context.getHttpRequest(), 401));
            return Mono.error(ex);
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient())
            .policies(audiencePolicy, exceptionPolicy)
            .build();

        StepVerifier.create(sendRequest(pipeline))
            .expectErrorMatches(throwable -> throwable instanceof HttpResponseException
                && throwable.getMessage().equals(NO_AUDIENCE_ERROR_MESSAGE))
            .verify();

        // Test sync version
        HttpResponseException thrown = assertThrows(HttpResponseException.class, () -> sendRequestSync(pipeline));
        assertEquals(NO_AUDIENCE_ERROR_MESSAGE, thrown.getMessage());
    }

    @Test
    public void processWithAudienceExceptionAndConfiguredAudience() {
        AudiencePolicy audiencePolicy = new AudiencePolicy(ConfigurationAudience.AZURE_PUBLIC_CLOUD);

        HttpPipelinePolicy exceptionPolicy = (context, next) -> {
            HttpResponseException ex = new HttpResponseException("Error " + AAD_AUDIENCE_ERROR_CODE + " occurred",
                new MockHttpResponse(context.getHttpRequest(), 401));
            return Mono.error(ex);
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient())
            .policies(audiencePolicy, exceptionPolicy)
            .build();

        StepVerifier.create(sendRequest(pipeline))
            .expectErrorMatches(throwable -> throwable instanceof HttpResponseException
                && throwable.getMessage().equals(INCORRECT_AUDIENCE_ERROR_MESSAGE))
            .verify();

        // Test sync version
        HttpResponseException thrown = assertThrows(HttpResponseException.class, () -> sendRequestSync(pipeline));
        assertEquals(INCORRECT_AUDIENCE_ERROR_MESSAGE, thrown.getMessage());
    }

    @Test
    public void handleAudienceExceptionWithNullMessage() {
        AudiencePolicy audiencePolicy = new AudiencePolicy(ConfigurationAudience.AZURE_PUBLIC_CLOUD);

        HttpResponseException originalException
            = new HttpResponseException(null, new MockHttpResponse(new HttpRequest(HttpMethod.GET, LOCAL_HOST), 401));

        // Use reflection to access the private method for testing
        try {
            java.lang.reflect.Method method
                = AudiencePolicy.class.getDeclaredMethod("handleAudienceException", HttpResponseException.class);
            method.setAccessible(true);

            HttpResponseException result = (HttpResponseException) method.invoke(audiencePolicy, originalException);
            assertSame(originalException, result, "Should return original exception when message is null");
        } catch (Exception e) {
            throw new RuntimeException("Failed to test handleAudienceException with null message", e);
        }
    }

    @Test
    public void handleAudienceExceptionWithoutErrorCode() {
        AudiencePolicy audiencePolicy = new AudiencePolicy(ConfigurationAudience.AZURE_PUBLIC_CLOUD);

        HttpResponseException originalException = new HttpResponseException("Some other error",
            new MockHttpResponse(new HttpRequest(HttpMethod.GET, LOCAL_HOST), 401));

        // Use reflection to access the private method for testing
        try {
            java.lang.reflect.Method method
                = AudiencePolicy.class.getDeclaredMethod("handleAudienceException", HttpResponseException.class);
            method.setAccessible(true);

            HttpResponseException result = (HttpResponseException) method.invoke(audiencePolicy, originalException);
            assertSame(originalException, result, "Should return original exception when error code is not found");
        } catch (Exception e) {
            throw new RuntimeException("Failed to test handleAudienceException without error code", e);
        }
    }

    @Test
    public void handleAudienceExceptionWithErrorCodeNullAudience() {
        AudiencePolicy audiencePolicy = new AudiencePolicy(null);

        HttpResponseException originalException
            = new HttpResponseException("Error " + AAD_AUDIENCE_ERROR_CODE + " occurred",
                new MockHttpResponse(new HttpRequest(HttpMethod.GET, LOCAL_HOST), 401));

        // Use reflection to access the private method for testing
        try {
            java.lang.reflect.Method method
                = AudiencePolicy.class.getDeclaredMethod("handleAudienceException", HttpResponseException.class);
            method.setAccessible(true);

            HttpResponseException result = (HttpResponseException) method.invoke(audiencePolicy, originalException);
            assertEquals(NO_AUDIENCE_ERROR_MESSAGE, result.getMessage());
            assertSame(originalException.getResponse(), result.getResponse());
        } catch (Exception e) {
            throw new RuntimeException("Failed to test handleAudienceException with error code and null audience", e);
        }
    }

    @Test
    public void handleAudienceExceptionWithErrorCodeConfiguredAudience() {
        AudiencePolicy audiencePolicy = new AudiencePolicy(ConfigurationAudience.AZURE_PUBLIC_CLOUD);

        HttpResponseException originalException
            = new HttpResponseException("Error " + AAD_AUDIENCE_ERROR_CODE + " occurred",
                new MockHttpResponse(new HttpRequest(HttpMethod.GET, LOCAL_HOST), 401));

        // Use reflection to access the private method for testing
        try {
            java.lang.reflect.Method method
                = AudiencePolicy.class.getDeclaredMethod("handleAudienceException", HttpResponseException.class);
            method.setAccessible(true);

            HttpResponseException result = (HttpResponseException) method.invoke(audiencePolicy, originalException);
            assertEquals(INCORRECT_AUDIENCE_ERROR_MESSAGE, result.getMessage());
            assertSame(originalException.getResponse(), result.getResponse());
        } catch (Exception e) {
            throw new RuntimeException("Failed to test handleAudienceException with error code and configured audience",
                e);
        }
    }

    private Mono<HttpResponse> sendRequest(HttpPipeline pipeline) {
        return pipeline.send(new HttpRequest(HttpMethod.GET, LOCAL_HOST));
    }

    private HttpResponse sendRequestSync(HttpPipeline pipeline) {
        return pipeline.sendSync(new HttpRequest(HttpMethod.GET, LOCAL_HOST), Context.NONE);
    }
}

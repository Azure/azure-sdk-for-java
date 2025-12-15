// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.management.exception.ManagementError;
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.resources.models.Provider;
import com.azure.resourcemanager.resources.models.Providers;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Http Pipeline Policy for automatic provider registration in Azure.
 */
public class ProviderRegistrationPolicy implements HttpPipelinePolicy {
    private static final String MISSING_SUBSCRIPTION_REGISTRATION = "MissingSubscriptionRegistration";
    private Providers providers;

    /**
     * Initialize a provider registration policy to automatically register the provider.
     */
    public ProviderRegistrationPolicy() {
    }

    /**
     * Initialize a provider registration policy to automatically register the provider.
     * @param resourceManager the Resource Manager that provider providers endpoint
     */
    public ProviderRegistrationPolicy(ResourceManager resourceManager) {
        providers = resourceManager.providers();
    }

    /**
     * Initialize a provider registration policy to automatically register the provider.
     * @param providers the providers endpoint
     */
    public ProviderRegistrationPolicy(Providers providers) {
        this.providers = providers;
    }

    /**
     * Sets the providers endpoint after initialized
     * @param providers the providers endpoint
     */
    public void setProviders(Providers providers) {
        this.providers = providers;
    }

    /**
     * Gets the providers endpoint contained in policy.
     *
     * @return the providers endpoint contained in policy
     */
    public Providers getProviders() {
        return providers;
    }

    private boolean isResponseSuccessful(HttpResponse response) {
        return response.getStatusCode() >= 200 && response.getStatusCode() < 300;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if (providers == null) {
            return next.process();
        }
        return next.clone().process().flatMap(response -> {
            if (!isResponseSuccessful(response)) {
                HttpResponse bufferedResponse = response.buffer();
                return FluxUtil.collectBytesInByteBufferStream(bufferedResponse.getBody()).flatMap(body -> {
                    String bodyStr = new String(body, StandardCharsets.UTF_8);

                    SerializerAdapter jacksonAdapter = SerializerFactory.createDefaultManagementSerializerAdapter();
                    ManagementError managementError;
                    try {
                        managementError
                            = jacksonAdapter.deserialize(bodyStr, ManagementError.class, SerializerEncoding.JSON);
                    } catch (IOException e) {
                        return Mono.just(bufferedResponse);
                    }

                    if (managementError != null
                        && MISSING_SUBSCRIPTION_REGISTRATION.equals(managementError.getCode())) {

                        String resourceNamespace = null;

                        if (managementError.getDetails() != null) {
                            // find in details.target
                            resourceNamespace = managementError.getDetails()
                                .stream()
                                .filter(
                                    d -> MISSING_SUBSCRIPTION_REGISTRATION.equals(d.getCode()) && d.getTarget() != null)
                                .map(ManagementError::getTarget)
                                .findFirst()
                                .orElse(null);
                        }
                        if (resourceNamespace == null) {
                            // find in message
                            Pattern providerPattern = Pattern.compile(".*'(.*)'");
                            Matcher providerMatcher = providerPattern.matcher(managementError.getMessage());
                            if (!providerMatcher.find()) {
                                return Mono.just(bufferedResponse);
                            }
                            resourceNamespace = providerMatcher.group(1);
                        }

                        // Retry after registration
                        return registerProviderUntilSuccess(resourceNamespace)
                            // in case error, return the response before registering resource provider
                            // if not error, this will be ignored
                            .then(Mono.just(bufferedResponse))
                            .onErrorReturn(bufferedResponse)
                            .then(next.clone().process());
                    }
                    return Mono.just(bufferedResponse);
                });
            }
            return Mono.just(response);
        });
    }

    private Mono<Void> registerProviderUntilSuccess(String namespace) {
        return providers.registerAsync(namespace).flatMap(provider -> {
            if (isProviderRegistered(provider)) {
                return Mono.empty();
            }
            return providers.getByNameAsync(namespace)
                .flatMap(this::checkProviderRegistered)
                .retryWhen(Retry
                    // 30 * 10sec
                    .fixedDelay(30,
                        ResourceManagerUtils.InternalRuntimeContext.getDelayDuration(Duration.ofSeconds(10)))
                    .filter(ProviderUnregisteredException.class::isInstance));
        });
    }

    private Mono<Void> checkProviderRegistered(Provider provider) throws ProviderUnregisteredException {
        if (isProviderRegistered(provider)) {
            return Mono.empty();
        }
        return Mono.error(new ProviderUnregisteredException());
    }

    private boolean isProviderRegistered(Provider provider) {
        return "Registered".equalsIgnoreCase(provider.registrationState());
    }

    private static class ProviderUnregisteredException extends RuntimeException {
    }
}

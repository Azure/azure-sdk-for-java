// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.policy;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.management.exception.ManagementError;
import com.azure.core.management.serializer.AzureJacksonAdapter;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.resources.models.Provider;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
import com.azure.resourcemanager.resources.ResourceManager;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Http Pipeline Policy for automatic provider registration in Azure.
 */
public class ProviderRegistrationPolicy implements HttpPipelinePolicy {
    private static final String MISSING_SUBSCRIPTION_REGISTRATION = "MissingSubscriptionRegistration";
    private final TokenCredential credential;
    private final AzureProfile profile;

    /**
     * Initialize a provider registration policy with a credential that's authorized
     * to register the provider.
     * @param credential the credential for provider registration
     * @param profile the profile to use
     */
    public ProviderRegistrationPolicy(TokenCredential credential, AzureProfile profile) {
        this.credential = credential;
        this.profile = profile;
    }

    private boolean isResponseSuccessful(HttpResponse response) {
        return response.getStatusCode() >= 200 && response.getStatusCode() < 300;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return next.process().flatMap(
            response -> {
                if (!isResponseSuccessful(response)) {
                    HttpResponse bufferedResponse = response.buffer();
                    return FluxUtil.collectBytesInByteBufferStream(bufferedResponse.getBody()).flatMap(
                        body -> {
                            String bodyStr = new String(body, StandardCharsets.UTF_8);

                            AzureJacksonAdapter jacksonAdapter = new AzureJacksonAdapter();
                            ManagementError cloudError;
                            try {
                                cloudError = jacksonAdapter.deserialize(
                                    bodyStr, ManagementError.class, SerializerEncoding.JSON);
                            } catch (IOException e) {
                                return Mono.just(bufferedResponse);
                            }

                            if (cloudError != null && MISSING_SUBSCRIPTION_REGISTRATION.equals(cloudError.getCode())) {
                                ResourceManager resourceManager = ResourceManager.authenticate(credential, profile)
                                        .withDefaultSubscription();
                                Pattern providerPattern = Pattern.compile(".*'(.*)'");
                                Matcher providerMatcher = providerPattern.matcher(cloudError.getMessage());
                                providerMatcher.find();

                                // Retry after registration
                                return registerProviderUntilSuccess(providerMatcher.group(1), resourceManager)
                                        .flatMap(afterRegistered -> next.process());
                            }
                            return Mono.just(bufferedResponse);
                        }
                    );
                }
                return Mono.just(response);
            }
        );
    }

    private Mono<Void> registerProviderUntilSuccess(String namespace, ResourceManager resourceManager) {
        return resourceManager.providers().registerAsync(namespace)
            .flatMap(
                provider -> {
                    if (isProviderRegistered(provider)) {
                        return Mono.empty();
                    }
                    return resourceManager.providers().getByNameAsync(namespace)
                            .flatMap(providerGet -> checkProviderRegistered(providerGet))
                            .retry(60, ProviderUnregisteredException.class::isInstance);
                }
            );
    }

    private Mono<Void> checkProviderRegistered(Provider provider) throws ProviderUnregisteredException {
        if (isProviderRegistered(provider)) {
            return Mono.empty();
        }
        SdkContext.sleep(5 * 1000);
        return Mono.error(new ProviderUnregisteredException());
    }

    private boolean isProviderRegistered(Provider provider) {
        return provider.registrationState().equalsIgnoreCase("Registered");
    }

    private static class ProviderUnregisteredException extends RuntimeException {
    }
}

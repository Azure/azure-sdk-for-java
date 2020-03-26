/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources.fluentcore.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.management.CloudError;
import com.azure.core.management.serializer.AzureJacksonAdapter;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.management.AzureTokenCredential;
import com.azure.management.RestClient;
import com.azure.management.RestClientBuilder;
import com.azure.management.resources.Provider;
import com.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.azure.management.resources.fluentcore.utils.SdkContext;
import com.azure.management.resources.implementation.ResourceManager;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Http Pipeline Policy for automatic provider registration in Azure.
 */
public class ProviderRegistrationPolicy implements HttpPipelinePolicy {
    private final static String MISSING_SUBSCRIPTION_REGISTRATION = "MissingSubscriptionRegistration";
    private final AzureTokenCredential credential;

    /**
     * Initialize a provider registration policy with a credential that's authorized
     * to register the provider.
     * @param credential the credential for provider registration
     */
    public ProviderRegistrationPolicy(AzureTokenCredential credential) {
        this.credential = credential;
    }

    private boolean isResponseSuccessful(HttpResponse response) {
        return response.getStatusCode() >= 200 && response.getStatusCode() < 300;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        final HttpRequest request = context.getHttpRequest();
        return next.process().flatMap(
            response -> {
                if (!isResponseSuccessful(response)) {
                    HttpResponse bufferedResponse = response.buffer();
                    return FluxUtil.collectBytesInByteBufferStream(bufferedResponse.getBody()).flatMap(
                        body -> {
                            String bodyStr = new String(body, StandardCharsets.UTF_8);

                            AzureJacksonAdapter jacksonAdapter = new AzureJacksonAdapter();
                            CloudError cloudError;
                            try {
                                cloudError = jacksonAdapter.deserialize(bodyStr, CloudError.class, SerializerEncoding.JSON);
                            } catch (IOException e) {
                                return Mono.just(bufferedResponse);
                            }

                            if (cloudError != null && MISSING_SUBSCRIPTION_REGISTRATION.equals(cloudError.getCode())) {
                                String subscriptionId = ResourceUtils.extractFromResourceId(request.getUrl().getPath(), "subscriptions");
                                RestClient restClient = new RestClientBuilder()
                                        .withBaseUrl(String.format("%s://%s", request.getUrl().getProtocol(), request.getUrl().getHost()))
                                        .withCredential(credential)
                                        .withSerializerAdapter(jacksonAdapter).buildClient();
                                // TODO: add proxy in rest client
                                ResourceManager resourceManager = ResourceManager.authenticate(restClient)
                                        .withSubscription(subscriptionId);
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
                    if (isProviderRegistered(provider)) return Mono.empty();
                    return resourceManager.providers().getByNameAsync(namespace)
                            .map(providerGet -> checkProviderRegistered(providerGet))
                            .retry(60, ProviderUnregisteredException.class::isInstance);
                }
            );
    }

    private Void checkProviderRegistered(Provider provider) throws ProviderUnregisteredException {
        if (isProviderRegistered(provider)) return null;
        SdkContext.sleep(5 * 1000);
        throw new ProviderUnregisteredException();
    }

    private boolean isProviderRegistered(Provider provider) {
        return provider.registrationState().equalsIgnoreCase("Registered");
    }

    private class ProviderUnregisteredException extends RuntimeException {
    }
}

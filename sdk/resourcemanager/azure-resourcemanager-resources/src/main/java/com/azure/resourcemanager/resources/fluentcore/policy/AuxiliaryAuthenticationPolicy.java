// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.policy;

import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.exception.ManagementError;
import com.azure.core.management.serializer.AzureJacksonAdapter;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * A Http Pipeline Policy for cross-tenant authorization in Azure.
 */
public class AuxiliaryAuthenticationPolicy implements HttpPipelinePolicy {

    private static final String AUTHORIZATION_AUXILIARY_HEADER = "x-ms-authorization-auxiliary";
    private static final String LINKED_AUTHORIZATION_FAILED = "LinkedAuthorizationFailed";
    private static final String SCHEMA_FORMAT = "Bearer %s";

    private final TokenCredential[] tokenCredentials;
    private final AzureEnvironment environment;

    /**
     * Initialize an auxiliary authentication policy with the list of AzureTokenCredentials.
     *
     * @param environment the Azure environment
     * @param credentials the AzureTokenCredentials list
     */
    public AuxiliaryAuthenticationPolicy(AzureEnvironment environment, TokenCredential... credentials) {
        this.environment = environment;
        this.tokenCredentials = credentials;
    }

    private boolean isResponseSuccessful(HttpResponse response) {
        return response.getStatusCode() >= 200 && response.getStatusCode() < 300;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return next.clone().process().flatMap(
            response -> {
                if (!isResponseSuccessful(response)
                    && this.tokenCredentials != null && this.tokenCredentials.length > 0) {

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

                            if (cloudError != null && LINKED_AUTHORIZATION_FAILED.equals(cloudError.getCode())
                                && context.getHttpRequest().getHeaders()
                                    .getValue(AUTHORIZATION_AUXILIARY_HEADER) == null) {
                                Flux<String> tokens = Flux.fromIterable(Arrays.asList(tokenCredentials))
                                    .flatMap(
                                        credential -> {
                                            String defaultScope = Utils.getDefaultScopeFromRequest(
                                                context.getHttpRequest(), this.environment);
                                            return credential.getToken(
                                                new TokenRequestContext().addScopes(defaultScope))
                                                    .map(accessToken ->
                                                        String.format(SCHEMA_FORMAT, accessToken.getToken()));
                                        });

                                // Retry
                                return tokens.collectList().flatMap(
                                    tokenList -> {
                                        context.getHttpRequest()
                                            .setHeader(AUTHORIZATION_AUXILIARY_HEADER, String.join(",", tokenList));
                                        return next.process();
                                    }
                                );
                            }

                            return Mono.just(bufferedResponse);
                        }
                    );
                }
                return Mono.just(response);
            }
        );
    }
}

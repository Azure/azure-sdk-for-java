/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources.fluentcore.policy;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.management.CloudError;
import com.azure.core.management.serializer.AzureJacksonAdapter;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.management.AzureTokenCredential;
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

    private final AzureTokenCredential[] tokenCredentials;

    public AuxiliaryAuthenticationPolicy(AzureTokenCredential... credentials) {
        this.tokenCredentials = credentials;
    }

    private boolean isResponseSuccessful(HttpResponse response) {
        return response.getStatusCode() >= 200 && response.getStatusCode() < 300;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return next.clone().process().flatMap(
            response -> {
                if (!isResponseSuccessful(response) && this.tokenCredentials != null && this.tokenCredentials.length > 0) {
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

                            if (cloudError != null && LINKED_AUTHORIZATION_FAILED.equals(cloudError.getCode()) &&
                                context.getHttpRequest().getHeaders().getValue(AUTHORIZATION_AUXILIARY_HEADER) == null) {
                                Flux<String> tokens = Flux.fromIterable(Arrays.asList(tokenCredentials))
                                    .flatMap(
                                        credential -> {
                                            String defaultScope = com.azure.management.Utils.getDefaultScopeFromRequest(context.getHttpRequest(), credential.getEnvironment());
                                            return credential.getToken(new TokenRequestContext().addScopes(defaultScope))
                                                    .map(accessToken -> String.format(SCHEMA_FORMAT, accessToken.getToken()));
                                        });

                                // Retry
                                return tokens.collectList().flatMap(
                                    tokenList -> {
                                        context.getHttpRequest().setHeader(AUTHORIZATION_AUXILIARY_HEADER, String.join(",", tokenList));
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

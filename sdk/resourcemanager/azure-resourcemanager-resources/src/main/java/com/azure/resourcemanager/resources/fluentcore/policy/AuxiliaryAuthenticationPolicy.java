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
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;

/**
 * A Http Pipeline Policy for cross-tenant authorization in Azure.
 */
public class AuxiliaryAuthenticationPolicy implements HttpPipelinePolicy {

    private static final String AUTHORIZATION_AUXILIARY_HEADER = "x-ms-authorization-auxiliary";
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

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if (tokenCredentials == null || tokenCredentials.length == 0) {
            return next.process();
        }
        return Flux.fromIterable(Arrays.asList(tokenCredentials))
            .flatMap(credential -> {
                String defaultScope = ResourceManagerUtils.getDefaultScopeFromRequest(
                    context.getHttpRequest(), environment);
                return credential.getToken(new TokenRequestContext().addScopes(defaultScope))
                    .map(token -> String.format(SCHEMA_FORMAT, token.getToken()));
            })
            .collectList()
            .flatMap(tokenList -> {
                context.getHttpRequest()
                    .setHeader(AUTHORIZATION_AUXILIARY_HEADER, String.join(",", tokenList));
                return next.process();
            });
    }
}

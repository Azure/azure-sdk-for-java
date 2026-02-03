// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.util.Context;

import java.util.function.Supplier;

/**
 * Utility class used to forward token authentication to Stainless clients
 */
public final class TokenUtils {

    /**
     * Utility authentication function.
     *
     * @param tokenCredential Token credential to be used
     * @param scopes for which the token credential authentication will be obtained
     * @return token supplier callback
     */
    public static Supplier<String> getBearerTokenSupplier(TokenCredential tokenCredential, String... scopes) {
        HttpPipeline pipeline
            = new HttpPipelineBuilder().policies(new BearerTokenAuthenticationPolicy(tokenCredential, scopes)).build();
        return () -> {
            // This request will never need to go anywhere; it is simply to cause the policy to interact with
            // the user's credential
            HttpRequest req = new HttpRequest(HttpMethod.GET, "https://www.example.com");
            try (HttpResponse res = pipeline.sendSync(req, Context.NONE)) {
                return res.getRequest().getHeaders().get(HttpHeaderName.AUTHORIZATION).getValue().split(" ")[1];
            }
        };
    }
}

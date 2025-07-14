// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.common.implementation;

import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

import static com.azure.core.util.CoreUtils.isNullOrEmpty;

/**
 * Pipeline policy that caches and validates Entra and ACS tokens in HTTP responses.
 */
public final class EntraTokenGuardPolicy implements HttpPipelinePolicy {

    private String entraTokenCache;
    private HttpResponse responseCache;

    /**
     * Default constructor for {@code EntraTokenGuardPolicy}.
     */
    public EntraTokenGuardPolicy() {
        super();
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        String currentEntraToken = context.getHttpRequest().getHeaders().get(HttpHeaderName.AUTHORIZATION).getValue();
        boolean entraTokenCacheValid = isEntraTokenCacheValid(currentEntraToken);

        if (entraTokenCacheValid && isAcsTokenCacheValid()) {
            return Mono.just(responseCache);
        } else {
            entraTokenCache = currentEntraToken;
            return next.process().doOnNext(response -> responseCache = response);
        }
    }

    private boolean isEntraTokenCacheValid(String currentEntraToken) {
        return !isNullOrEmpty(entraTokenCache) && entraTokenCache.equals(currentEntraToken);
    }

    private boolean isAcsTokenCacheValid() {
        return responseCache != null && responseCache.getStatusCode() == 200 && isAcsTokenValid();
    }

    private boolean isAcsTokenValid() {
        try {
            String body = responseCache.getBodyAsString(StandardCharsets.UTF_8).block();
            JsonNode root = new ObjectMapper().readTree(body);
            JsonNode accessTokenNode = root.get("accessToken");
            AccessToken accessToken = new TokenParser().parseJWTToken(accessTokenNode.get("token").asText());

            return OffsetDateTime.now().isBefore(accessToken.getExpiresAt());
        } catch (RuntimeException | IOException e) {
            return false;
        }
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * A policy uses a concurrent map to maintain the latest sync-tokens. When this HTTP pipeline policy is triggered, the
 * policy will retrieve all sync-tokens without sequence number segment from the concurrent map and use it in the HTTP
 * request. Also after received the HTTP response, update the latest sync-tokens to the map.
 */
public final class SyncTokenPolicy implements HttpPipelinePolicy {
    private static final String SYNC_TOKEN = "Sync-Token";
    private final ConcurrentHashMap<String, SyncToken> syncTokenMap = new ConcurrentHashMap<>(); // key is sync-token id

    /**
     * Add or update the sync token to a thread safe map.
     *
     * @param context request context
     * @param next The next policy to invoke.
     * @return A {@link Mono} representing the HTTP response that will arrive asynchronously.
     */
    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {

        // Add all of sync-tokens to HTTP request header
        context.getHttpRequest().setHeader(SYNC_TOKEN, getSyncTokenHeader());

        return next.process().flatMap(httpResponse -> {
            // Get the sync-token from HTTP response header
            final String syncTokenValue = httpResponse.getHeaders().getValue(SYNC_TOKEN);

            // Skip sync-token updates of concurrent map if no 'Sync-Token' header
            if (syncTokenValue == null) {
                return Mono.just(httpResponse);
            }

            // Sync-Token header could have more than one value
            final String[] syncTokens = syncTokenValue.split(",");
            for (final String syncTokenStr : syncTokens) {
                final SyncToken syncToken = SyncToken.parseSyncToken(syncTokenStr);
                final String tokenId = syncToken.getId();
                // If the value is not thread safe and must be updated inside the method with a remapping function
                // to ensure the entire operation is atomic.
                syncTokenMap.compute(tokenId, (s, existingSyncToken) -> {
                    if (existingSyncToken == null
                        || syncToken.getSequenceNumber() > existingSyncToken.getSequenceNumber()) {
                        return syncToken;
                    }
                    return existingSyncToken;
                });
            }

            return Mono.just(httpResponse);
        });
    }

    /**
     * Get all latest sync-tokens from the concurrent map and convert to one sync-token string.
     * All sync-tokens concatenated by a comma delimiter.
     * @return sync-token string
     */
    private String getSyncTokenHeader() {
        return syncTokenMap.values().stream().map(syncToken -> syncToken.getId() + "=" + syncToken.getValue())
            .collect(Collectors.joining(","));
    }
}

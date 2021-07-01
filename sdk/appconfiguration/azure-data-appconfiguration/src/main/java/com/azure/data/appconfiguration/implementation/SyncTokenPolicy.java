// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A policy uses a concurrent map to maintain the latest sync-tokens. When this HTTP pipeline policy is triggered, the
 * policy will retrieve all sync-tokens without sequence number segment from the concurrent map and use it in the HTTP
 * request. Also after received the HTTP response, update the latest sync-tokens to the map.
 */
public final class SyncTokenPolicy implements HttpPipelinePolicy {
    private static final String COMMA = ",";
    private static final String EQUAL = "=";
    private static final String SYNC_TOKEN = "Sync-Token";
    private static final String SKIP_INVALID_TOKEN = "Skipping invalid sync token '{}'.";
    private final Map<String, SyncToken> syncTokenMap = new ConcurrentHashMap<>(); // key is sync-token id
    private final ClientLogger logger = new ClientLogger(SyncTokenPolicy.class);

    /**
     * Add or update the sync token to a thread safe map.
     *
     * @param context request context
     * @param next The next policy to invoke.
     * @return A {@link Mono} representing the HTTP response that will arrive asynchronously.
     */
    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {

        // TODO: https://github.com/Azure/azure-sdk-for-java/issues/20355
        // Add all of sync-tokens to HTTP request header
        context.getHttpRequest().setHeader(SYNC_TOKEN, getSyncTokenHeader());

        return next.process().flatMap(httpResponse -> {
            // Get the sync-token from HTTP response header
            final String syncTokenValue = httpResponse.getHeaders().getValue(SYNC_TOKEN);

            // Skip sync-token updates of concurrent map if no 'Sync-Token' header
            if (syncTokenValue != null) {
                updateSyncToken(syncTokenValue);
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
        return syncTokenMap.values().stream().map(syncToken -> syncToken.getId() + EQUAL + syncToken.getValue())
                   .collect(Collectors.joining(COMMA));
    }

    /**
     * Update the existing synchronization tokens.
     *
     * @param token an external synchronization token to ensure service requests receive up-to-date values.
     */
    public void updateSyncToken(String token) {
        // Sync-Token header could have more than one value
        final String[] syncTokens = token.split(COMMA);
        for (final String syncTokenString : syncTokens) {
            if (CoreUtils.isNullOrEmpty(syncTokenString)) {
                continue;
            }

            final SyncToken syncToken;
            try {
                syncToken = SyncToken.createSyncToken(syncTokenString);
            } catch (Exception ex) {
                logger.info(SKIP_INVALID_TOKEN, syncTokenString);
                continue;
            }

            final String tokenId = syncToken.getId();
            // If the value is not thread safe and must be updated inside the method with a remapping function
            // to ensure the entire operation is atomic.
            syncTokenMap.compute(tokenId, (key, existingSyncToken) -> {
                if (existingSyncToken == null
                        || syncToken.getSequenceNumber() > existingSyncToken.getSequenceNumber()) {
                    return syncToken;
                }
                return existingSyncToken;
            });
        }
    }
}

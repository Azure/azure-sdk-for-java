// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class SyncTokenPolicy implements HttpPipelinePolicy {
    private static final String SYNC_TOKEN = "Sync-Token";
    private final ConcurrentHashMap<String, SyncToken> syncTokenMap; // key is sync-token id

    public SyncTokenPolicy() {
        syncTokenMap = new ConcurrentHashMap<>();
    }

    /**
     * Add or update the sync token id and value to a thread safe map.
     *
     * @param context request context
     * @param next The next policy to invoke.
     * @return
     */
    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {

        // attempt to add header to the request
        context.getHttpRequest().setHeader(SYNC_TOKEN, getSyncTokenHeader());

        return next.process().flatMap(httpResponse -> {
            // get the latest sync token from map
            final String syncTokenValue = httpResponse.getHeaders().getValue(SYNC_TOKEN);
            if (syncTokenValue == null) {
                return Mono.just(httpResponse);
            }

            // Sync-Token header could have more than one value
            final String[] syncTokens = syncTokenValue.split(",");
            for (final String syncTokenStr : syncTokens) {
                final SyncToken syncToken = SyncToken.fromSyncTokenString(syncTokenStr);
                if (syncToken == null) {
                    return Mono.just(httpResponse);
                }

                final String tokenId = syncToken.getId();
                final long tokenSequenceNumber = syncToken.getSequenceNumber();

                if (syncTokenMap.containsKey(tokenId)) {
                    // get the latest sync token from map
                    final SyncToken existSyncToken = syncTokenMap.get(tokenId);
                    if (existSyncToken.getSequenceNumber() < tokenSequenceNumber) {
                        // update the same sync token.
                        syncTokenMap.replace(tokenId, existSyncToken, syncToken);
                    }
                } else {
                    syncTokenMap.put(tokenId, syncToken);
                }
            }

            return Mono.just(httpResponse);
        });
    }

    private String getSyncTokenHeader() {
        return syncTokenMap.values().stream().map(SyncToken::getSyncTokenStringInRequest)
            .collect(Collectors.joining(","));
    }
}

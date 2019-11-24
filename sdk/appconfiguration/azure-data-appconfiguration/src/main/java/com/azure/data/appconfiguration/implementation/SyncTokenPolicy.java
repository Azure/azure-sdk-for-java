// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;

public final class SyncTokenPolicy implements HttpPipelinePolicy {
    private static final String syncTokenHeader = "Sync-Token";
    private ConcurrentHashMap<String, SyncToken> syncTokenMap; // key is sync-token id

    public SyncTokenPolicy() {
        syncTokenMap = new ConcurrentHashMap<>();
    }

    /**
     * Adds the requ
     *
     * @param context request context
     * @param next The next policy to invoke.
     * @return
     */
    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return next.process().flatMap(httpResponse -> {
            // get the latest sync token from map
            final String syncTokenValue = httpResponse.getRequest().getHeaders().getValue(syncTokenHeader);
            if (syncTokenValue == null) {
                return Mono.just(httpResponse);
            }

            // Sync-Token header could have more than one value
            final String[] syncTokens = syncTokenValue.split(",");
            for (final String syncTokenStr : syncTokens) {
                final SyncToken syncToken = new SyncToken().fromSyncTokenString(syncTokenStr);
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
                        syncTokenMap.put(tokenId, syncToken);
                    }
                } else {
                    syncTokenMap.put(tokenId, syncToken);
                }
            }

            httpResponse.getRequest().getHeaders().put(syncTokenHeader, syncTokenValue);
            return Mono.just(httpResponse);
        });
    }
}

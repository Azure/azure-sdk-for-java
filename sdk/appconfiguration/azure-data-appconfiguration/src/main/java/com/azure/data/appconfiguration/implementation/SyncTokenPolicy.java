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
    private ConcurrentHashMap<String, SyncToken> syncTokenMap;

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
        // On Request, get the latest sync token from map

        // On response, update the same sync token.

        return null;
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.perf;

import com.azure.identity.SharedTokenCacheCredential;
import com.azure.identity.SharedTokenCacheCredentialBuilder;
import com.azure.identity.perf.core.ServiceTest;
import com.azure.perf.test.core.PerfStressOptions;
import reactor.core.publisher.Mono;

/**
 * Azure Identity test for cache writing.
 */
public class WriteCache extends ServiceTest<PerfStressOptions> {
    private final SharedTokenCacheCredential credential;

    /**
     * Constructs a new Azure Identity test for cache writing.
     *
     * @param options Configuration options for the cache writing test.
     */
    public WriteCache(PerfStressOptions options) {
        super(options);
        credential = new SharedTokenCacheCredentialBuilder()
            .clientId(CLI_CLIENT_ID)
            .build();
    }

    // Perform the API call to be tested here
    @Override
    public void run() {
        credential.getToken(ARM_TOKEN_REQUEST_CONTEXT).block();
    }

    @Override
    public Mono<Void> runAsync() {
        return credential.getToken(ARM_TOKEN_REQUEST_CONTEXT).then();
    }
}

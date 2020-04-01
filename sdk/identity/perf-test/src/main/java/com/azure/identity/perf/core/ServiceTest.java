// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.perf.core;

import com.azure.core.credential.TokenRequestContext;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;
import reactor.core.publisher.Mono;

public abstract class ServiceTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {
    protected static final String CLI_CLIENT_ID = "04b07795-8ddb-461a-bbee-02f9e1bf7b46";
    protected static final TokenRequestContext ARM_TOKEN_REQUEST_CONTEXT = new TokenRequestContext()
            .addScopes("https://management.azure.com/.default");

    public ServiceTest(TOptions options) {
        super(options);
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        // Populate the token cache for tests
        return super.globalSetupAsync()
//                .then(new InteractiveBrowserCredentialBuilder().port(8765)
//                        .clientId(CLI_CLIENT_ID).build()
//                        .getToken(ARM_TOKEN_REQUEST_CONTEXT))
                .then();
    }
}

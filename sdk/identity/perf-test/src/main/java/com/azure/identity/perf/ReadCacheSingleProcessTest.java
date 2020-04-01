// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.perf;

import com.azure.identity.SharedTokenCacheCredential;
import com.azure.identity.SharedTokenCacheCredentialBuilder;
import com.azure.identity.perf.core.ServiceTest;
import com.azure.perf.test.core.SizeOptions;
import reactor.core.publisher.Mono;

import java.nio.file.Paths;

public class ReadCacheSingleProcessTest extends ServiceTest<SizeOptions> {
    private final SharedTokenCacheCredential credential;

    public ReadCacheSingleProcessTest(SizeOptions options) {
        super(options);
        credential = new SharedTokenCacheCredentialBuilder()
                .clientId(CLI_CLIENT_ID)
                .keychainService("Microsoft.Developer.IdentityService")
                .keychainAccount("MSALCache")
                .cacheFileLocation(Paths.get(System.getProperty("user.home"),
                        ".IdentityService", "msal.cache"))
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

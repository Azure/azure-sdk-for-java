// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.perf;

import com.azure.identity.SharedTokenCacheCredential;
import com.azure.identity.SharedTokenCacheCredentialBuilder;
import com.azure.identity.perf.core.ServiceTest;
import com.azure.perf.test.core.SizeOptions;
import com.sun.jna.Platform;
import reactor.core.publisher.Mono;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

public class WriteCache extends ServiceTest<SizeOptions> {
    private final SharedTokenCacheCredential credential;

    public WriteCache(SizeOptions options) {
        super(options);
        Path cacheFileLocation;
        if (Platform.isWindows()) {
            cacheFileLocation = Paths.get(System.getProperty("user.home"),
                    "AppData", "Local", ".IdentityService", "msal.cache");
        } else {
            cacheFileLocation = Paths.get(System.getProperty("user.home"),
                    ".IdentityService", "msal.cache");
        }
        credential = new SharedTokenCacheCredentialBuilder()
                .clientId(CLI_CLIENT_ID)
                .keychainService("Microsoft.Developer.IdentityService")
                .keychainAccount("MSALCache")
                .cacheFileLocation(cacheFileLocation)
                .tokenRefreshOffset(Duration.ofMinutes(60))
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

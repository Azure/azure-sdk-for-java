/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources.core;


import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.management.AzureEnvironment;
import com.azure.management.ApplicationTokenCredential;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.HashMap;

public class AzureTestCredential extends ApplicationTokenCredential {
    boolean isPlaybackMode;

    public AzureTestCredential(final String mockUrl, String mockTenant, boolean isPlaybackMode) {
        super("test", mockTenant, "test", new AzureEnvironment(new HashMap<String, String>() {{
            put("managementEndpointUrl", mockUrl);
            put("resourceManagerEndpointUrl", mockUrl);
            put("sqlManagementEndpointUrl", mockUrl);
            put("galleryEndpointUrl", mockUrl);
            put("activeDirectoryEndpointUrl", mockUrl);
            put("activeDirectoryResourceId", mockUrl);
            put("activeDirectoryGraphResourceId", mockUrl);

        }}));
        this.isPlaybackMode = isPlaybackMode;
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        if (!isPlaybackMode) {
            super.getToken(request);
        }
        return Mono.just(new AccessToken("https:/asdd.com", OffsetDateTime.MAX));
    }
}

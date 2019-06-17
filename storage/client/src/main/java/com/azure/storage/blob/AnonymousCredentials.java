// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import reactor.core.publisher.Mono;

/**
 * Anonymous credentials are to be used with with HTTP(S) requests that read blobs from public containers or requests
 * that use a Shared Access Signature (SAS). This is because Anonymous credentials will not set an Authorization header.
 * Pass an instance of this class as the credentials parameter when creating a new pipeline (typically with
 * {@link StorageURL}).
 */
public final class AnonymousCredentials implements ICredentials {

    /**
     * Returns an empty instance of {@code AnonymousCredentials}.
     */
    public AnonymousCredentials() {
    }



    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return next.process();
    }
}

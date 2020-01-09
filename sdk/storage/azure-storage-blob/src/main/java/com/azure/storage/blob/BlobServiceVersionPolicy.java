// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.storage.common.implementation.Constants;
import reactor.core.publisher.Mono;

import static com.azure.storage.common.implementation.StorageImplUtils.throwIfContainsHeader;

/**
 * This policy prevents headers and query parameters introduced in new service versions are not accidentally sent when
 * the intended service version is older.
 */
public class BlobServiceVersionPolicy implements HttpPipelinePolicy {

    private static final String ANY_BLOB_API = "any blob API";
    private static final String CREATE_CONTAINER = "create container";

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        HttpHeaders requestHeaders = context.getHttpRequest().getHeaders();
        BlobServiceVersion serviceVersion = toServiceVersion(
            requestHeaders.getValue(Constants.HeaderConstants.SERVICE_VERSION));

        if (serviceVersion.ordinal() == BlobServiceVersion.getLatest().ordinal()) {
            return next.process();
        }

        // 2019_07_07 check
        if (serviceVersion.ordinal() < BlobServiceVersion.V2019_07_07.ordinal()) {
            // Encryption scope
            throwIfContainsHeader(requestHeaders, "x-ms-default-encryption-scope", CREATE_CONTAINER,
                serviceVersion.getVersion());
            throwIfContainsHeader(requestHeaders, "x-ms-deny-encryption-scope-override", CREATE_CONTAINER,
                serviceVersion.getVersion());
            throwIfContainsHeader(requestHeaders, "x-ms-encryption-scope", ANY_BLOB_API, serviceVersion.getVersion());

            // Managed disk range diff
            throwIfContainsHeader(requestHeaders, "x-ms-previous-snapshot-url", ANY_BLOB_API,
                serviceVersion.getVersion());
        }

        return next.process();
    }

    private static BlobServiceVersion toServiceVersion(String version) {
        if (BlobServiceVersion.V2019_02_02.getVersion().equals(version)) {
            return BlobServiceVersion.V2019_02_02;
        } else if (BlobServiceVersion.V2019_07_07.getVersion().equals(version)) {
            return BlobServiceVersion.V2019_07_07;
        } else {
            throw new IllegalStateException(Constants.HeaderConstants.SERVICE_VERSION + " must be set.");
        }
    }
}

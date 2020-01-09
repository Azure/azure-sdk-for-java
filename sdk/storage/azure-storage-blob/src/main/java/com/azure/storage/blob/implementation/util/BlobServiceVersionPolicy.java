package com.azure.storage.blob.implementation.util;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.common.implementation.Constants;
import reactor.core.publisher.Mono;

import static com.azure.storage.common.implementation.StorageImplUtils.throwIfContainsHeader;

public class BlobServiceVersionPolicy implements HttpPipelinePolicy {

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        HttpHeaders requestHeaders = context.getHttpRequest().getHeaders();
        BlobServiceVersion serviceVersion = toServiceVersion(
            requestHeaders.getValue(Constants.HeaderConstants.SERVICE_VERSION));
        String ANY_BLOB_API = "any blob API";

        if (serviceVersion.ordinal() == BlobServiceVersion.getLatest().ordinal()) {
            return next.process();
        }

        // 2019_07_07 check
        if (serviceVersion.ordinal() < BlobServiceVersion.V2019_07_07.ordinal()) {
            // Encryption scope
            String CREATE_CONTAINER = "create container";
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
        if (version.equals(BlobServiceVersion.V2019_02_02.getVersion())) {
            return BlobServiceVersion.V2019_02_02;
        } else if (version.equals(BlobServiceVersion.V2019_07_07.getVersion())) {
            return BlobServiceVersion.V2019_07_07;
        } else {
            throw new NullPointerException(Constants.HeaderConstants.SERVICE_VERSION + " must be set.");
        }
    }
}

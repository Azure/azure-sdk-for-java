// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.implementation.util;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.file.share.ShareServiceVersion;
import reactor.core.publisher.Mono;

import java.net.URL;

import static com.azure.storage.common.implementation.StorageImplUtils.throwIfContainsHeader;
import static com.azure.storage.common.implementation.StorageImplUtils.throwIfContainsQuery;

/**
 * This policy prevents headers and query parameters introduced in new service versions are not accidentally sent when
 * the intended service version is older.
 */
public class ShareServiceVersionPolicy implements HttpPipelinePolicy {

    private static final String ANY_FILE_API = "any file API";

    private static final String START_COPY = "copy file";

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        HttpHeaders requestHeaders = context.getHttpRequest().getHeaders();
        URL requestUrl = context.getHttpRequest().getUrl();
        ShareServiceVersion serviceVersion = toServiceVersion(
            requestHeaders.getValue(Constants.HeaderConstants.SERVICE_VERSION));

        if (serviceVersion.ordinal() == ShareServiceVersion.getLatest().ordinal()) {
            return next.process();
        }

        // 2019_07_07 check
        if (serviceVersion.ordinal() < ShareServiceVersion.V2019_07_07.ordinal()) {
            // File lease
            throwIfContainsHeader(requestHeaders, "x-ms-lease-id", ANY_FILE_API, serviceVersion.getVersion());
            throwIfContainsHeader(requestHeaders, "x-ms-lease-duration", ANY_FILE_API, serviceVersion.getVersion());
            throwIfContainsHeader(requestHeaders, "x-ms-proposed-lease-id", ANY_FILE_API, serviceVersion.getVersion());

            throwIfContainsQuery(requestUrl, "comp=lease", ANY_FILE_API, serviceVersion.getVersion());

            // File copy - new SMB headers
            if (requestHeaders.get("x-ms-copy-source") != null) {
                throwIfContainsHeader(requestHeaders, "x-ms-file-permission", START_COPY, serviceVersion.getVersion());
                throwIfContainsHeader(requestHeaders, "x-ms-file-permission-key", START_COPY,
                    serviceVersion.getVersion());
                throwIfContainsHeader(requestHeaders, "x-ms-file-permission-copy-mode", START_COPY,
                    serviceVersion.getVersion());
                throwIfContainsHeader(requestHeaders, "x-ms-file-copy-ignore-read-only", START_COPY,
                    serviceVersion.getVersion());
                throwIfContainsHeader(requestHeaders, "x-ms-file-copy-set-archive", START_COPY,
                    serviceVersion.getVersion());
                throwIfContainsHeader(requestHeaders, "x-ms-file-attributes", START_COPY,
                    serviceVersion.getVersion());
                throwIfContainsHeader(requestHeaders, "x-ms-file-creation-time", START_COPY,
                    serviceVersion.getVersion());
                throwIfContainsHeader(requestHeaders, "x-ms-file-last-write-time", START_COPY,
                    serviceVersion.getVersion());
            }
        }

        return next.process();
    }

    private static ShareServiceVersion toServiceVersion(String version) {
        if (ShareServiceVersion.V2019_02_02.getVersion().equals(version)) {
            return ShareServiceVersion.V2019_02_02;
        } else if (ShareServiceVersion.V2019_07_07.getVersion().equals(version)) {
            return ShareServiceVersion.V2019_07_07;
        } else {
            throw new IllegalStateException (Constants.HeaderConstants.SERVICE_VERSION + " must be set.");
        }
    }
}

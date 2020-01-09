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

public class ShareServiceVersionPolicy implements HttpPipelinePolicy {

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        HttpHeaders requestHeaders = context.getHttpRequest().getHeaders();
        URL requestUrl = context.getHttpRequest().getUrl();
        ShareServiceVersion serviceVersion = toServiceVersion(
            requestHeaders.getValue(Constants.HeaderConstants.SERVICE_VERSION));
        String ANY_FILE_API = "any file API";

        if (serviceVersion.ordinal() == ShareServiceVersion.getLatest().ordinal()) {
            return next.process();
        }

        // 2019_07_07 check
        if (serviceVersion.ordinal() < ShareServiceVersion.V2019_07_07.ordinal()) {
            // File lease
            throwIfContainsHeader(requestHeaders, "x-ms-lease-id", ANY_FILE_API, serviceVersion.getVersion());
            throwIfContainsHeader(requestHeaders, "x-ms-lease-duration", ANY_FILE_API, serviceVersion.getVersion());
            throwIfContainsHeader(requestHeaders, "x-ms-proposed-lease-id", ANY_FILE_API, serviceVersion.getVersion());

            if (requestUrl.getQuery() != null && requestUrl.getQuery().contains("comp=lease")) {
                throw new IllegalArgumentException("File lease operations are not supported for any file API in "
                    + "service version " + serviceVersion.getVersion());
            }

            // File copy - new SMB headers
            String START_COPY = "copy file";
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
        if (version.equals(ShareServiceVersion.V2019_02_02.getVersion())) {
            return ShareServiceVersion.V2019_02_02;
        } else if (version.equals(ShareServiceVersion.V2019_07_07.getVersion())) {
            return ShareServiceVersion.V2019_07_07;
        } else {
            throw new NullPointerException(Constants.HeaderConstants.SERVICE_VERSION + " must be set.");
        }
    }
}

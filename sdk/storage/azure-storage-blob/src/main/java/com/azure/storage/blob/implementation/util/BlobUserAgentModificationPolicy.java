// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This policy modifies the blob user agent string for clients created in packages that are dependencies of blob.
 * It transforms a User Agent String as follows
 * UAbefore: "azsdk-java-azure-storage-blob/12.11.0-beta.2 (11.0.6; Windows 10; 10.0)"
 * UAafter: "azsdk-java-azure-storage-blob/12.11.0-beta.2 azsdk-java-azure-storage-blob-batch/12.8.0-beta.2 (11.0.6; Windows 10; 10.0) "
 */
public class BlobUserAgentModificationPolicy implements HttpPipelinePolicy {

    private final String clientName;
    private final String clientVersion;

    private static final String USER_AGENT = "User-Agent";
    private static final String REGEX = "(.*? )?(azsdk-java-azure-storage-blob/12\\.\\d{1,2}\\.\\d{1,2}(?:-beta\\.\\d{1,2})?)( .*?)?";
    private static final Pattern PATTERN = Pattern.compile(REGEX);

    /**
     * Creates a new BlobUserAgentModificationPolicy.
     *
     * @param clientName The name of the package.
     * @param clientVersion The version of the package.
     */
    public BlobUserAgentModificationPolicy(String clientName, String clientVersion) {
        this.clientName = clientName;
        this.clientVersion = clientVersion;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {

        String userAgent = context.getHttpRequest().getHeaders().getValue(USER_AGENT);
        Matcher matcher = PATTERN.matcher(userAgent);
        StringBuilder builder = new StringBuilder();
        if (matcher.matches()) {
            builder.append(matcher.group(1) == null ? "" : matcher.group(1))
                .append(matcher.group(2) == null ? "" : matcher.group(2))
                .append(" ").append("azsdk-java-").append(clientName).append("/").append(clientVersion)
                .append(matcher.group(3) == null ? "" : matcher.group(3));
        } else {
            builder.append(userAgent);
        }
        context.getHttpRequest().getHeaders().put("User-Agent", builder.toString());
        return next.process();
    }

    @Override
    public HttpPipelinePosition getPipelinePosition() {
        return HttpPipelinePosition.PER_CALL;
    }
}

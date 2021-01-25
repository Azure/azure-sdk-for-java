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

public class BlobUserAgentModificationPolicy implements HttpPipelinePolicy {

    private final String clientName;
    private final String clientVersion;

    private static final String USER_AGENT = "User-Agent";
    private static final String REGEX = "azsdk-java-azure-storage-blob/\\d+\\.\\d+\\.\\d+[-beta\\.\\d+]*(.)*";
    private static final Pattern PATTERN = Pattern.compile(REGEX);

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
            String[] ua = userAgent.split(" ");
            for (int i = 0; i < ua.length; i++) {
                builder.append(ua[i]).append(" ");
                if (i == 0) {
                    builder.append("azsdk-java-").append(clientName).append("/").append(clientVersion).append(" ");
                }
            }
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

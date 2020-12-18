// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.common;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.logging.ClientLogger;

import reactor.core.publisher.Mono;

/**
 * A policy that logs the Http Response
 */
public class CommunicationLoggerPolicy implements HttpPipelinePolicy {

    final ClientLogger logger = new ClientLogger(CommunicationLoggerPolicy.class);
    /**
     * Creates a policy that logs the Http Response
     */
    public CommunicationLoggerPolicy() {
        // construct logger
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return next.process()
        .flatMap(httpResponse -> {
            final HttpResponse bufferedResponse = httpResponse.buffer();
            logger.info("Response Url " + bufferedResponse.getRequest().getUrl());
            logger.info("MS-CV header: " + bufferedResponse.getHeaderValue("MS-CV"));
            return Mono.just(bufferedResponse);
        });
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.common;

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
    final String testName;

    /**
     * Creates a policy that logs the Http Response
     * @param testName Name of the test to log
     */
    public CommunicationLoggerPolicy(String testName) {
        this.testName = testName;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return next.process()
        .flatMap(httpResponse -> {
            final HttpResponse bufferedResponse = httpResponse.buffer();

            // Should sanitize printed reponse url
            System.out.println("Response Url for " + testName + ": " + bufferedResponse.getRequest().getUrl());
            System.out.println("MS-CV header for " + testName + ": " + bufferedResponse.getHeaderValue("MS-CV"));
            return Mono.just(bufferedResponse);
        });
    }
}

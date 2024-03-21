// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.text;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import java.util.Objects;
import reactor.core.publisher.Mono;

/**
 * Adds MT custom authentication headers to the requests.
 */
class TranslatorResourceIdAuthenticationPolicy implements HttpPipelinePolicy {

    private static final HttpHeaderName RESOURCE_ID_HEADER_NAME = HttpHeaderName.fromString("Ocp-Apim-ResourceId");

    private final String azureResourceId;

    /**
     * Creates an instance of TranslatorResourceIdAuthenticationPolicy class.
     *
     * @param azureResourceId - azureResourceId where the Translator resource was created.
     */
    TranslatorResourceIdAuthenticationPolicy(String azureResourceId) {
        Objects.requireNonNull(azureResourceId, "'azureResourceId' cannot be null.");
        this.azureResourceId = azureResourceId;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy nextPolicy) {
        return Mono.fromRunnable(() -> {
            HttpRequest request = context.getHttpRequest();
            request.setHeader(RESOURCE_ID_HEADER_NAME, this.azureResourceId);

        }).then(nextPolicy.process());
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy nextPolicy) {
        HttpRequest request = context.getHttpRequest();
        request.setHeader(RESOURCE_ID_HEADER_NAME, this.azureResourceId);

        return nextPolicy.processSync();
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.BinaryData;
import org.eclipse.jetty.util.IO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UncheckedIOException;

public class MockFailureResponsePolicy implements HttpPipelinePolicy {

    private int tries;

    public MockFailureResponsePolicy(int tries) {
        this.tries = tries;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return next.process().flatMap(response -> {
            if (this.tries == 0) {
                return Mono.just(response);
            } else {
                this.tries -= 1;
                return Mono.just(new MockDownloadHttpResponse(response, 206, Flux.error(new IOException())));
            }
        });
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        HttpResponse response = next.processSync();
        if (this.tries == 0) {
            return response;
        } else {
            this.tries -= 1;
            return new MockDownloadHttpResponse(response, 206,
                //BinaryData.fromFlux(Flux.error(new IOException())).block()
                //BinaryData.fromObject(new IOException())
                //BinaryData.fromFlux(new IOException()).block()
                new IOException()
            );
        }
    }
}

package com.azure.storage.common.test.shared.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MockPartialResponsePolicy implements HttpPipelinePolicy {
    private final int partialFailures;
    private int count = 0;
    private final byte[] fullResponseData;

    public MockPartialResponsePolicy(int partialFailures, byte[] fullResponseData) {
        this.partialFailures = partialFailures;
        this.fullResponseData = fullResponseData;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return next.process().flatMap(response -> {
            count++;
            System.out.println("count: " + count);
            if (count < partialFailures) {
                // Simulate partial response and timeout
                System.out.println("response body: " + response.getBodyAsBinaryData().toString());
                return Mono.just(new MockDownloadHttpResponse(
                    response,
                    206,
//                    Flux.just(ByteBuffer.wrap(new byte[] { 1 })) // Send 1 byte
//                        .concatWith(Flux.error(new IOException("Simulated timeout"))) // Trigger timeout
                    Flux.just(ByteBuffer.wrap(new byte[] {response.getBodyAsBinaryData().toBytes()[0]}))
                        .concatWith(Flux.error(new IOException("Simulated timeout")))
                ));
            } else {
                // Full response on the nth attempt
//                return Mono.just(
//                    new MockDownloadHttpResponse(
//                    response,
//                    200,
//                    Flux.just(ByteBuffer.wrap(fullResponseData)) // Full response data
//                ));
                return Mono.just(response);
            }
        });
    }
}

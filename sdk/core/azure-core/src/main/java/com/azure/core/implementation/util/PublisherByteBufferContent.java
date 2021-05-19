// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.util.RequestContent;
import com.azure.core.util.RequestOutbound;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.ByteBuffer;

public class PublisherByteBufferContent implements RequestContent {
    private final Publisher<ByteBuffer> content;

    public PublisherByteBufferContent(Publisher<ByteBuffer> content) {
        this.content = content;
    }

    @Override
    public Mono<Void> writeToAsync(RequestOutbound requestOutbound) {
        return Flux.from(content)
            .flatMap(buffer -> {
                try {
                    return Flux.just(requestOutbound.getRequestChannel().write(buffer));
                } catch (IOException e) {
                    return Flux.error(e);
                }
            })
            .then();
    }

    @Override
    public void writeTo(RequestOutbound requestOutbound) {
        writeToAsync(requestOutbound).block();
    }

    @Override
    public Flux<ByteBuffer> asFluxByteBuffer() {
        return Flux.from(content);
    }

    @Override
    public Long getLength() {
        return null;
    }
}

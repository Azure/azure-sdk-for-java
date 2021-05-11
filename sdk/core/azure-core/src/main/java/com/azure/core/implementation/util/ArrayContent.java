// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.util.RequestContent;
import com.azure.core.util.RequestOutbound;
import reactor.core.publisher.Mono;

/**
 * A {@link RequestContent} implementation which is backed by a {@code byte[]}.
 */
public final class ArrayContent extends RequestContent {
    private final byte[] content;
    private final int offset;
    private final int length;

    public ArrayContent(byte[] content, int offset, int length) {
        this.content = content;
        this.offset = offset;
        this.length = length;
    }

    @Override
    public Mono<Void> writeToAsync(RequestOutbound requestOutbound) {
        return Mono.defer(() -> Mono.fromRunnable(() -> writeTo(requestOutbound)));
    }

    @Override
    public void writeTo(RequestOutbound requestOutbound) {

    }

    @Override
    public Long attemptToGetLength() {
        return (long) length;
    }
}

package com.azure.storage.queue.models;

import com.azure.core.credentials.TokenCredential;
import reactor.core.publisher.Mono;

public class SharedKeyCredential extends TokenCredential {
    private final String sharedKey;

    public SharedKeyCredential(String sharedKey) {
        this.sharedKey = sharedKey;
    }

    @Override
    public Mono<String> getTokenAsync(String resource) {
        return Mono.just(sharedKey);
    }
}

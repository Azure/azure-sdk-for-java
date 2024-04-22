package com.azure.ai.openai.assistants.implementation;

import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;

public final class OpenAIServerSentEvents {

    private final Flux<ByteBuffer> source;

    public OpenAIServerSentEvents(Flux<ByteBuffer> source) {
        this.source = source;
    }
}

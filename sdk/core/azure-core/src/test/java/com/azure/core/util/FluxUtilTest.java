// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FluxUtilTest {
    @Test
    public void testCallWithContextGetSingle() {
        String response = getSingle()
            .subscriberContext(reactor.util.context.Context.of("FirstName", "Foo", "LastName", "Bar"))
            .block();
        assertEquals("Hello, Foo Bar", response);
    }

    @Test
    public void testCallWithContextGetCollection() {
        List<String> expectedLines = Arrays.asList("Hello,", "Foo", "Bar");
        List<String> actualLines = new ArrayList<>();
        getCollection()
            .subscriberContext(reactor.util.context.Context.of("FirstName", "Foo", "LastName", "Bar"))
            .doOnNext(actualLines::add)
            .subscribe();
        assertEquals(expectedLines, actualLines);
    }

    @Test
    public void toReactorContextEmpty() {
        reactor.util.context.Context reactorContext = FluxUtil.toReactorContext(null);
        assertTrue(reactorContext.isEmpty());
    }

    @Test
    public void toReactorContext() {
        Context context = new Context("key1", "value1");

        reactor.util.context.Context reactorContext = FluxUtil.toReactorContext(context);
        assertEquals(1, reactorContext.size());
        assertTrue(reactorContext.hasKey("key1"));
        assertEquals("value1", reactorContext.get("key1"));

        context = context.addData("key2", "value2")
            .addData("key1", "value3");

        reactorContext = FluxUtil.toReactorContext(context);
        assertEquals(2, reactorContext.size());
        assertTrue(reactorContext.hasKey("key1"));
        assertEquals("value3", reactorContext.get("key1"));
        assertTrue(reactorContext.hasKey("key2"));
        assertEquals("value2", reactorContext.get("key2"));
    }

    private Mono<String> getSingle() {
        return FluxUtil.withContext(this::serviceCallSingle);
    }

    private Flux<String> getCollection() {
        return FluxUtil
            .fluxContext(this::serviceCallCollection);
    }

    private Mono<String> serviceCallSingle(Context context) {
        String msg = "Hello, "
            + context.getData("FirstName").orElse("Stranger")
            + " "
            + context.getData("LastName").orElse("");
        return Mono.just(msg);
    }

    private Flux<String> serviceCallCollection(Context context) {
        String msg = "Hello, "
            + context.getData("FirstName").orElse("Stranger")
            + " "
            + context.getData("LastName").orElse("");

        return Flux.just(msg.split(" "));
    }
}

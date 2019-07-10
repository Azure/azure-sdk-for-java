// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.service;

import com.azure.core.util.Context;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Unit tests for {@link ServiceUtil}
 */
public class ServiceUtilTest {

    @Test
    public void testCallWithContextGetSingle() {

        String response = this.getSingle("Hello, ")
            .subscriberContext(reactor.util.context.Context.of("FirstName", "Foo", "LastName", "Bar"))
            .block();
        Assert.assertEquals("Hello, Foo Bar", response);
    }

    @Test
    public void testCallWithContextGetCollection() {
        List<String> expectedLines = Arrays.asList("Hello,", "Foo", "Bar");
        List<String> actualLines = new ArrayList<>();
        this.getCollection("Hello, ")
            .subscriberContext(reactor.util.context.Context.of("FirstName", "Foo", "LastName", "Bar"))
            .doOnNext(line -> actualLines.add(line))
            .subscribe();
        Assert.assertEquals(expectedLines, actualLines);
    }

    private Mono<String> getSingle(String prefix) {
        return ServiceUtil.callWithContextGetSingle(context -> serviceCallSingle(prefix, context));
    }

    private Flux<String> getCollection(String prefix) {
        return ServiceUtil.callWithContextGetCollection(context -> serviceCallCollection(prefix, context));
    }

    private Mono<String> serviceCallSingle(String prefix, Context context) {
        String msg = prefix
            + context.getData("FirstName").orElse("Stranger")
            + " "
            + context.getData("LastName").orElse("");
        return Mono.just(msg);
    }

    private Flux<String> serviceCallCollection(String prefix, Context context) {
        String msg = prefix
            + context.getData("FirstName").orElse("Stranger")
            + " "
            + context.getData("LastName").orElse("");

        return Flux.just(msg.split(" "));
    }
}

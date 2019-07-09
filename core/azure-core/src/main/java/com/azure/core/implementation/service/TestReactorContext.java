// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.service;

import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.Context;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class TestReactorContext {
    public static void main(String[] args) {
        TestReactorContext test = new TestReactorContext();

        String result = test
            .clientLibraryAPI("Hello, ")
            .subscriberContext(reactor.util.context.Context.of("FirstName", "Jonathan", "LastName", "Giles"))
            .block();

        System.out.println(result);

        test
            .clientLibraryAPI2("Hello, ")
            .subscriberContext(
                reactor.util.context.Context.of("FirstName", "Jonathan", "LastName", "Giles"))
            .doOnNext(System.out::println)
            .subscribe();
    }

    public Mono<String> clientLibraryAPI(String prefix) {
        return Mono.subscriberContext().map(this::toAzureContext).flatMap(c -> serviceCall(prefix, c));
    }

    public Flux<String> clientLibraryAPI2(String prefix) {
        // calling block here will result in context being empty, so, this will not work
        return ServiceHelper.callWithContext(context -> serviceCall2(prefix, context)).block().collection();
    }

    private Mono<String> serviceCall(String prefix, Context context) {
        String msg = prefix
            + context.getData("FirstName").orElse("Stranger")
            + " "
            + context.getData("LastName").orElse("");
        return Mono.just(msg);
    }

    private Flux<String> serviceCall2(String prefix, Context context) {
        String msg = prefix
            + context.getData("FirstName").orElse("Stranger")
            + " "
            + context.getData("LastName").orElse("");

        return Flux.just(msg.split(" "));
    }

    private Context toAzureContext(reactor.util.context.Context context) {
        Map<Object, Object> keyValues = context.stream()
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        if (ImplUtils.isNullOrEmpty(keyValues)) {
            return Context.NONE;
        }
        return Context.of(keyValues);
    }

}

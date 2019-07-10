// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.service;

import com.azure.core.util.Context;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class TestReactorContext {
    public static void main(String[] args) {
        TestReactorContext test = new TestReactorContext();

        System.out.println("------------------------Mono no overloading - Works-------------------------------------");
        System.out.println(test
            .clientLibraryAPIWithNoOverloadForMono("Hello, ")
            .subscriberContext(reactor.util.context.Context.of("FirstName", "Jonathan", "LastName", "Giles"))
            .block());

        System.out.println("------------------------Flux no overloading - Works-------------------------------------");
        test
            .clientLibraryAPIWithNoOverloadForFlux("Hello, ")
            .subscriberContext(
                reactor.util.context.Context.of("FirstName", "Jonathan", "LastName", "Giles"))
            .doOnNext(System.out::println)
            .subscribe();

        System.out.println("------------------------Mono with block - context is empty-------------------------------------");
        System.out.println(test
            .clientLibraryAPIWithBlockForMono("Hello, ")
            .subscriberContext(reactor.util.context.Context.of("FirstName", "Jonathan", "LastName", "Giles"))
            .block());

        System.out.println("------------------------Flux with block - context is empty-------------------------------------");
        test
            .clientLibraryAPIWithBlockForFlux("Hello, ")
            .subscriberContext(
                reactor.util.context.Context.of("FirstName", "Jonathan", "LastName", "Giles"))
            .doOnNext(System.out::println)
            .subscribe();

        System.out.println("------------------------Mono with response holder - context is empty-------------------------------------");
        System.out.println(test
            .clientLibraryAPIForMono("Hello, ")
            .subscriberContext(reactor.util.context.Context.of("FirstName", "Jonathan", "LastName", "Giles"))
            .block());

        System.out.println("------------------------Flux with response holder - context is empty-------------------------------------");
        test
            .clientLibraryAPIForFlux("Hello, ")
            .subscriberContext(
                reactor.util.context.Context.of("FirstName", "Jonathan", "LastName", "Giles"))
            .doOnNext(System.out::println)
            .subscribe();
    }

    private Mono<String> clientLibraryAPIWithNoOverloadForMono(String prefix) {
        return ServiceHelper.callWithContextGetSingle(context -> serviceCallSingle(prefix, context));
    }

    private Flux<String> clientLibraryAPIWithNoOverloadForFlux(String prefix) {
        return ServiceHelper.callWithContextGetCollection(context -> serviceCallCollection(prefix, context));
    }

    private Mono<String> clientLibraryAPIWithBlockForMono(String prefix) {
        return ServiceHelper.callWithContext(context -> serviceCallSingle(prefix, context)).block().single();
    }

    private Flux<String> clientLibraryAPIWithBlockForFlux(String prefix) {
        return ServiceHelper.callWithContext(context -> serviceCallCollection(prefix, context)).block().collection();
    }

    private Mono<String> clientLibraryAPIForMono(String prefix) {
        return ServiceHelper.callWithContextBlock(context -> serviceCallSingle(prefix, context)).single();
    }

    private Flux<String> clientLibraryAPIForFlux(String prefix) {
        return ServiceHelper.callWithContextBlock(context -> serviceCallCollection(prefix, context)).collection();
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

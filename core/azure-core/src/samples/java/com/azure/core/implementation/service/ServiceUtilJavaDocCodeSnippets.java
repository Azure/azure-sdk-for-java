// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.service;

import com.azure.core.util.Context;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Code snippets for {@link ServiceUtil}
 */
public class ServiceUtilJavaDocCodeSnippets {

    /**
     * Method to show how users can pass in context using {@link reactor.util.context.Context Reactor Context}
     * for api calls that return single entity
     */
    public void serviceWithSingleResponse() {
        ServiceUtilJavaDocCodeSnippets azureClient = this;
        // BEGIN: com.azure.core.implementation.service.serviceutil.usersamplesingle
        Mono<String> response = azureClient
            .clientLibraryApiReturnsSingleItem("Hello, ")
            .subscriberContext(
                reactor.util.context.Context.of("FirstName", "Foo", "LastName", "Bar"));
        System.out.println(response.block());
        // END: com.azure.core.implementation.service.serviceutil.usersamplesingle
    }

    /**
     * Method to show how users can pass in context using {@link reactor.util.context.Context Reactor Context}
     * for api calls that return a collection
     */
    public void serviceWithCollectionResponse() {
        ServiceUtilJavaDocCodeSnippets azureClient = this;
        // BEGIN: com.azure.core.implementation.service.serviceutil.usersamplecollection
        azureClient
            .clientLibraryApiReturnsCollection("Hello, ")
            .subscriberContext(
                reactor.util.context.Context.of("FirstName", "Foo", "LastName", "Bar"))
            .doOnNext(System.out::println)
            .subscribe();
        // END: com.azure.core.implementation.service.serviceutil.usersamplecollection
    }

    /**
     * Code snippet for using {@link ServiceUtil} with single item response
     */
    public void codeSnippetForServiceUtilCallWithSingleResponse() {
        // BEGIN: com.azure.core.implementation.service.serviceutil.callwithcontextgetsingle
        String prefix = "Hello, ";
        Mono<String> response = ServiceUtil
            .callWithContextGetSingle(context -> serviceCall(prefix, context));
        // END: com.azure.core.implementation.service.serviceutil.callwithcontextgetsingle
    }

    /**
     * Code snippet for using {@link ServiceUtil} with collection response
     */
    public void codeSnippetForServiceUtilCallWithCollectionResponse() {
        // BEGIN: com.azure.core.implementation.service.serviceutil.callwithcontextgetcollection
        String prefix = "Hello, ";
        Flux<String> response = ServiceUtil
            .callWithContextGetCollection(context -> serviceCall2(prefix, context));
        // END: com.azure.core.implementation.service.serviceutil.callwithcontextgetcollection
    }

    /**
     * Implementation not provided
     * @param prefix The prefix
     * @param context Azure context
     * @return {@link Flux#empty() empty} response
     */
    private Flux<String> serviceCall2(String prefix, Context context) {
        return Flux.empty();
    }

    /**
     * Implementation not provided
     * @param prefix The prefix
     * @param context Azure context
     * @return {@link Mono#empty() empty} response
     */
    private Mono<String> serviceCall(String prefix, Context context) {
        return Mono.empty();
    }

    /**
     * Implementation not provided
     * @param input The input string
     * @return {@link Flux#empty() empty} response
     */
    private Flux<String> clientLibraryApiReturnsCollection(String input) {
        return Flux.empty();
    }

    /**
     * Implementation not provided
     * @param input The input string
     * @return {@link Mono#empty() empty} response
     */
    private Mono<String> clientLibraryApiReturnsSingleItem(String input) {
        return Mono.empty();
    }

}

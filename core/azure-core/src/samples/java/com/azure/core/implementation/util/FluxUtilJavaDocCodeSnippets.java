// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.util.Context;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Code snippets for {@link FluxUtil}
 */
public class FluxUtilJavaDocCodeSnippets {

    /**
     * Code snippet for using {@link FluxUtil} with single item response
     */
    public void codeSnippetForCallWithSingleResponse() {
        // BEGIN: com.azure.core.implementation.util.fluxutil.callwithcontextgetsingle
        String prefix = "Hello, ";
        Mono<String> response = FluxUtil
            .callWithContextGetSingle(context -> serviceCallReturnsSingle(prefix, context));
        // END: com.azure.core.implementation.util.fluxutil.callwithcontextgetsingle
    }

    /**
     * Code snippet for using {@link FluxUtil} with collection response
     */
    public void codeSnippetForCallWithCollectionResponse() {
        // BEGIN: com.azure.core.implementation.util.fluxutil.callwithcontextgetcollection
        String prefix = "Hello, ";
        Flux<String> response = FluxUtil
            .callWithContextGetCollection(context -> serviceCallReturnsCollection(prefix, context));
        // END: com.azure.core.implementation.util.fluxutil.callwithcontextgetcollection
    }

    /**
     * Implementation not provided
     * @param prefix The prefix
     * @param context Azure context
     * @return {@link Flux#empty() empty} response
     */
    private Flux<String> serviceCallReturnsCollection(String prefix, Context context) {
        return Flux.empty();
    }

    /**
     * Implementation not provided
     * @param prefix The prefix
     * @param context Azure context
     * @return {@link Mono#empty() empty} response
     */
    private Mono<String> serviceCallReturnsSingle(String prefix, Context context) {
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

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
        // BEGIN: com.azure.core.implementation.util.fluxutil.withcontext
        String prefix = "Hello, ";
        Mono<String> response = FluxUtil
            .withContext(context -> serviceCallReturnsSingle(prefix, context));
        // END: com.azure.core.implementation.util.fluxutil.withcontext
    }

    /**
     * Code snippet for using {@link FluxUtil} with collection response
     */
    public void codeSnippetForCallWithCollectionResponse() {
        // BEGIN: com.azure.core.implementation.util.fluxutil.fluxcontext
        String prefix = "Hello, ";
        Flux<String> response = FluxUtil
            .fluxContext(context -> serviceCallReturnsCollection(prefix, context));
        // END: com.azure.core.implementation.util.fluxutil.fluxcontext
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
}

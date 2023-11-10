// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.http.rest;

import com.typespec.core.http.pipeline.HttpPipeline;
import com.typespec.core.http.pipeline.HttpPipelineBuilder;
import com.typespec.core.http.pipeline.HttpPipelinePolicy;
import com.typespec.core.implementation.http.policy.retry.RetryPolicy;
import com.typespec.core.implementation.http.serializer.DefaultJsonSerializer;
import com.typespec.core.models.Context;
import com.typespec.core.http.models.HttpRequestOptions;
import com.typespec.core.util.ClientLogger;
import com.typespec.core.util.serializer.ObjectSerializer;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods that aid processing in RestProxy.
 */
public final class RestProxyUtils {
    public static final ClientLogger LOGGER = new ClientLogger(RestProxyUtils.class);
    public static final String BODY_TOO_LARGE = "Request body emitted %d bytes, more than the expected %d bytes.";
    public static final String BODY_TOO_SMALL = "Request body emitted %d bytes, less than the expected %d bytes.";

    private RestProxyUtils() {
    }

    /**
     * Merges the Context with the Context provided with Options.
     *
     * @param context the Context to merge
     * @param options the options holding the context to merge with
     *
     * @return the merged context.
     */
    public static Context mergeRequestOptionsContext(Context context, HttpRequestOptions options) {
        if (options == null) {
            return context;
        }

        Context optionsContext = options.getContext();

        if (optionsContext != null && optionsContext != Context.NONE) {
            context = Context.mergeContexts(context, optionsContext);
        }

        return context;
    }

    /**
     * Create an instance of the default serializer.
     *
     * @return the default serializer
     */
    public static ObjectSerializer createDefaultJsonSerializer() {
        return new DefaultJsonSerializer();
    }

    /**
     * Create the default HttpPipeline.
     *
     * @return the default HttpPipeline
     */
    public static HttpPipeline createDefaultPipeline() {
        List<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(new RetryPolicy());

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .build();
    }
}

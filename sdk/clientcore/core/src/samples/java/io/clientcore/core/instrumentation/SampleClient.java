// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation;

import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.HttpRequestContext;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;

class SampleClient {
    private final HttpPipeline httpPipeline;
    private final String endpoint;
    private final Instrumentation instrumentation;

    SampleClient(InstrumentationOptions instrumentationOptions, HttpPipeline httpPipeline, String endpoint) {
        this.httpPipeline = httpPipeline;
        this.endpoint = endpoint;
        LibraryInstrumentationOptions libraryOptions = new LibraryInstrumentationOptions("contoso-sample").setEndpoint(endpoint);
        this.instrumentation = Instrumentation.create(instrumentationOptions, libraryOptions);
    }

    public Response<?> downloadContent() {
        return this.downloadContent(null);
    }

    public Response<?> downloadContent(HttpRequestContext options) {
        // BEGIN: io.clientcore.core.instrumentation.instrumentwithresponse
        return instrumentation.instrumentWithResponse("Sample.download", options, this::downloadImpl);
        // END: io.clientcore.core.instrumentation.instrumentwithresponse
    }

    public void create(HttpRequestContext options) {
        // BEGIN: io.clientcore.core.instrumentation.instrument
        instrumentation.instrument("Sample.create", options, this::createImpl);
        // END: io.clientcore.core.instrumentation.instrument
    }

    public Response<?> createWithResponse(HttpRequestContext options) {
        return instrumentation.instrumentWithResponse("create", options, this::createWithResponseImpl);
    }

    private Response<?> downloadImpl(HttpRequestContext options) {
        return httpPipeline.send(new HttpRequest().setMethod(HttpMethod.GET).setUri(endpoint).setRequestContext(options));
    }

    private Response<?> createWithResponseImpl(HttpRequestContext options) {
        return httpPipeline.send(new HttpRequest().setMethod(HttpMethod.POST).setUri(endpoint).setRequestContext(options));
    }

    private void createImpl(HttpRequestContext options) {
        httpPipeline.send(new HttpRequest().setMethod(HttpMethod.POST).setUri(endpoint).setRequestContext(options));
    }
}

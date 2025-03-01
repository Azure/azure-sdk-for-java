// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation;

import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;

class SampleClient {

    private final static LibraryInstrumentationOptions LIBRARY_OPTIONS = new LibraryInstrumentationOptions("contoso-sample");
    private final HttpPipeline httpPipeline;
    private final String endpoint;
    private final Instrumentation instrumentation;

    SampleClient(InstrumentationOptions instrumentationOptions, HttpPipeline httpPipeline, String endpoint) {
        this.httpPipeline = httpPipeline;
        this.endpoint = endpoint;
        this.instrumentation = Instrumentation.create(instrumentationOptions, LIBRARY_OPTIONS, endpoint);
    }

    public Response<?> downloadContent() {
        return this.downloadContent(null);
    }

    public Response<?> downloadContent(RequestOptions options) {
        // BEGIN: io.clientcore.core.instrumentation.instrument
        return instrumentation.instrumentWithResponse("download", options, this::downloadImpl);
        // END: io.clientcore.core.instrumentation.instrument
    }

    public Response<?> create(RequestOptions options) {
        return instrumentation.instrumentWithResponse("create", options, this::createImpl);
    }

    private Response<?> downloadImpl(RequestOptions options) {
        return httpPipeline.send(new HttpRequest().setMethod(HttpMethod.GET).setUri(endpoint).setRequestOptions(options));
    }

    private Response<?> createImpl(RequestOptions options) {
        return httpPipeline.send(new HttpRequest().setMethod(HttpMethod.POST).setUri(endpoint).setRequestOptions(options));
    }
}

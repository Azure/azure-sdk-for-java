// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation;

import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;

import java.net.URI;

class SampleClient {
    private static final LibraryInstrumentationOptions LIBRARY_OPTIONS = new LibraryInstrumentationOptions("contoso-sample");
    private static final String SAMPLE_CLIENT_DURATION_METRIC = "contoso.sample.client.operation.duration";
    private final HttpPipeline httpPipeline;
    private final OperationInstrumentation downloadContentInstrumentation;
    private final OperationInstrumentation createInstrumentation;
    private final String endpoint;

    SampleClient(InstrumentationOptions instrumentationOptions, HttpPipeline httpPipeline, String endpoint) {
        this.httpPipeline = httpPipeline;
        this.endpoint = endpoint;
        Instrumentation instrumentation = Instrumentation.create(instrumentationOptions, LIBRARY_OPTIONS);

        // BEGIN: io.clientcore.core.instrumentation.create
        InstrumentedOperationDetails downloadDetails = new InstrumentedOperationDetails("downloadContent",
            SAMPLE_CLIENT_DURATION_METRIC).endpoint(endpoint);
        this.downloadContentInstrumentation = instrumentation.createOperationInstrumentation(downloadDetails);
        // END: io.clientcore.core.instrumentation.create

        this.createInstrumentation = instrumentation.createOperationInstrumentation(
            new InstrumentedOperationDetails("create", SAMPLE_CLIENT_DURATION_METRIC)
                .endpoint(endpoint));
    }

    public Response<?> downloadContent() {
        return this.downloadContent(null);
    }

    public Response<?> downloadContent(RequestOptions options) {
        // BEGIN: io.clientcore.core.instrumentation.instrument
        return downloadContentInstrumentation.instrument(this::downloadImpl, options);
        // END: io.clientcore.core.instrumentation.instrument
    }

    public Response<?> create(RequestOptions options) {
        return createInstrumentation.instrument(this::createImpl, options);
    }

    private Response<?> downloadImpl(RequestOptions options) {
        return httpPipeline.send(new HttpRequest().setMethod(HttpMethod.GET).setUri(endpoint).setRequestOptions(options));
    }

    private Response<?> createImpl(RequestOptions options) {
        return httpPipeline.send(new HttpRequest().setMethod(HttpMethod.POST).setUri(endpoint).setRequestOptions(options));
    }
}

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
    private final static LibraryInstrumentationOptions LIBRARY_OPTIONS = new LibraryInstrumentationOptions("contoso-sample");
    private final static String SAMPLE_CLIENT_DURATION_METRIC = "contoso.sample.client.operation.duration";
    private final HttpPipeline httpPipeline;
    private final OperationInstrumentation downloadContentInstrumentation;
    private final OperationInstrumentation createInstrumentation;
    private final URI endpoint;

    SampleClient(InstrumentationOptions instrumentationOptions, HttpPipeline httpPipeline, URI endpoint) {
        this.httpPipeline = httpPipeline;
        this.endpoint = endpoint;
        Instrumentation instrumentation = Instrumentation.create(instrumentationOptions, LIBRARY_OPTIONS);

        // BEGIN: io.clientcore.core.telemetry.instrumentation.create
        InstrumentedOperationDetails downloadDetails = new InstrumentedOperationDetails("downloadContent",
            SAMPLE_CLIENT_DURATION_METRIC).endpoint(endpoint);
        this.downloadContentInstrumentation = instrumentation.createOperationInstrumentation(downloadDetails);
        // END: io.clientcore.core.telemetry.instrumentation.create

        this.createInstrumentation = instrumentation.createOperationInstrumentation(
            new InstrumentedOperationDetails("create", SAMPLE_CLIENT_DURATION_METRIC)
                .endpoint(endpoint));
    }

    public Response<?> downloadContent() {
        return this.downloadContent(null);
    }

    public Response<?> downloadContent(RequestOptions options) {
        // BEGIN: io.clientcore.core.telemetry.instrumentation.shouldinstrument
        if (!downloadContentInstrumentation.shouldInstrument(options)) {
            return downloadImpl(options);
        }
        // END: io.clientcore.core.telemetry.instrumentation.shouldinstrument

        if (options == null || options == RequestOptions.none()) {
            options = new RequestOptions();
        }

        // BEGIN: io.clientcore.core.telemetry.instrumentation.startscope
        OperationInstrumentation.Scope scope = downloadContentInstrumentation.startScope(options);
        try {
            return downloadImpl(options);
        } catch (RuntimeException t) {
            scope.setError(t);
            throw t;
        } finally {
            scope.close();
        }

        // END: io.clientcore.core.telemetry.instrumentation.startscope
    }

    public Response<?> create(RequestOptions options) {
        if (!createInstrumentation.shouldInstrument(options)) {
            return httpPipeline.send(new HttpRequest(HttpMethod.POST, endpoint));
        }

        if (options == null || options == RequestOptions.none()) {
            options = new RequestOptions();
        }

        OperationInstrumentation.Scope scope = createInstrumentation.startScope(options);
        try {
            return httpPipeline.send(new HttpRequest(HttpMethod.POST, endpoint));
        } catch (RuntimeException t) {
            scope.setError(t);
            throw t;
        } finally {
            scope.close();
        }
    }

    private Response<?> downloadImpl(RequestOptions options) {
        return httpPipeline.send(new HttpRequest(HttpMethod.GET, endpoint));
    }
}

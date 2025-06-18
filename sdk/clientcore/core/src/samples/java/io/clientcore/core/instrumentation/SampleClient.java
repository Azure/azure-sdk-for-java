// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation;

import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;

class SampleClient {
    private final HttpPipeline httpPipeline;
    private final String endpoint;
    private final Instrumentation instrumentation;

    SampleClient(InstrumentationOptions instrumentationOptions, HttpPipeline httpPipeline, String endpoint) {
        this.httpPipeline = httpPipeline;
        this.endpoint = endpoint;
        SdkInstrumentationOptions sdkOptions = new SdkInstrumentationOptions("contoso-sample").setEndpoint(endpoint);
        this.instrumentation = Instrumentation.create(instrumentationOptions, sdkOptions);
    }

    public Response<?> downloadContent() {
        return this.downloadContent(null);
    }

    public Response<?> downloadContent(RequestContext context) {
        // BEGIN: io.clientcore.core.instrumentation.instrumentwithresponse
        return instrumentation.instrumentWithResponse("Sample.download", context, this::downloadImpl);
        // END: io.clientcore.core.instrumentation.instrumentwithresponse
    }

    public void create(RequestContext context) {
        // BEGIN: io.clientcore.core.instrumentation.instrument
        instrumentation.instrument("Sample.create", context, this::createImpl);
        // END: io.clientcore.core.instrumentation.instrument
    }

    public Response<?> createWithResponse(RequestContext context) {
        return instrumentation.instrumentWithResponse("create", context, this::createWithResponseImpl);
    }

    private Response<?> downloadImpl(RequestContext context) {
        return httpPipeline.send(new HttpRequest().setMethod(HttpMethod.GET).setUri(endpoint).setContext(context));
    }

    private Response<?> createWithResponseImpl(RequestContext context) {
        return httpPipeline.send(new HttpRequest().setMethod(HttpMethod.POST).setUri(endpoint).setContext(context));
    }

    private void createImpl(RequestContext context) {
        httpPipeline.send(new HttpRequest().setMethod(HttpMethod.POST).setUri(endpoint).setContext(context));
    }
}

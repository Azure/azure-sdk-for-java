// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation;

import io.clientcore.core.http.pipeline.HttpInstrumentationOptions;
import io.clientcore.core.http.pipeline.HttpInstrumentationPolicy;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;

class SampleClientBuilder {
    private HttpInstrumentationOptions instrumentationOptions;

    public SampleClientBuilder instrumentationOptions(HttpInstrumentationOptions instrumentationOptions) {
        this.instrumentationOptions = instrumentationOptions;
        return this;
    }

    public SampleClient build() {
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .addPolicy(new HttpInstrumentationPolicy(instrumentationOptions))
            .build();
        return new SampleClient(instrumentationOptions, pipeline, "https://example.com");
    }
}

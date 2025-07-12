// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.implementation.PersistentAgentsAdministrationClientTracer;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.azure.ai.agents.persistent.SampleUtils.configureOpenTelemetryEndpointTracing;

public final class AgentTracingSample {

    @SuppressWarnings("try")
    public static void main(String[] args) {
        final OpenTelemetrySdk telemetrySdk = configureOpenTelemetryEndpointTracing();
        final Tracer tracer = telemetrySdk.getTracer(PersistentAgentsAdministrationClientTracer.class.getName());

        Map<String, Consumer<String[]>> samples = new LinkedHashMap<>();
//        samples.put("AgentBasicSample.main", AgentBasicSample::main);
//        samples.put("AgentStreamingSample.main", AgentStreamingSample::main);
        samples.put("AgentFunctionsStreamingSample.main", AgentFunctionsStreamingSample::main);

        for (Map.Entry<String, Consumer<String[]>> entry : samples.entrySet()) {
            String sampleName = entry.getKey();
            Consumer<String[]> sample = entry.getValue();

            final Span span = tracer.spanBuilder(sampleName).startSpan();
            try (AutoCloseable scope = span.makeCurrent()) {
                System.out.println("Executing sample: " + sampleName);
                sample.accept(args);
            } catch (Exception e) {
                span.setStatus(StatusCode.ERROR, e.getMessage());
                System.err.println("Error executing " + sampleName + ": " + e.getMessage());
            } finally {
                span.end();
            }
        }
    }
}

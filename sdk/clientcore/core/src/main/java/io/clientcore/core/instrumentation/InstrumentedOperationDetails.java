// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation;

import io.clientcore.core.instrumentation.tracing.SpanKind;

import java.net.URI;
import java.util.Objects;

/**
 * Details of client operation to be instrumented.
 */
public class InstrumentedOperationDetails {
    private final String operationName;
    private final String metricName;
    private SpanKind spanKind;
    private URI endpoint;

    /**
     * Creates a new instance of {@link InstrumentedOperationDetails}.
     *
     * @param durationMetricName fully qualified name of the duration metric to report. It should be following {@code {client-name}.client.operation.duration} format.
     * See <a href="https://github.com/open-telemetry/semantic-conventions/blob/main/docs/general/naming.md#metrics">OpenTelemetry naming conventions</a> for more information.
     * The same metric is reported for different operations of the same client, but with different {@code operation.name} attribute.
     *
     * @param operationName the name of the operation. The name is used as a value for the {@code operation.name} attribute.
     */
    public InstrumentedOperationDetails(String durationMetricName, String operationName) {
        Objects.requireNonNull(durationMetricName, "'metricName' cannot be null");
        Objects.requireNonNull(operationName, "'operationName' cannot be null");

        this.operationName = operationName;
        this.metricName = durationMetricName;
        this.spanKind = SpanKind.CLIENT;
    }

    /**
     * Sets the endpoint for the operation.
     * @param endpoint the service endpoint URI. The host and port are used as values for the {@code server.address} and {@code server.port} attributes.
     * @return The updated {@link InstrumentedOperationDetails} object.
     */
    public InstrumentedOperationDetails endpoint(URI endpoint) {
        Objects.requireNonNull(endpoint, "'endpoint' cannot be null");
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Sets the span kind for the operation.
     *
     * @param spanKind the span kind for the operation.
     * @return The updated {@link InstrumentedOperationDetails} object.
     */
    public InstrumentedOperationDetails spanKind(SpanKind spanKind) {
        Objects.requireNonNull(spanKind, "'spanKind' cannot be null");
        this.spanKind = spanKind;
        return this;
    }

    URI getEndpoint() {
        return endpoint;
    }

    SpanKind getSpanKind() {
        return spanKind;
    }

    String getDurationMetricName() {
        return metricName;
    }

    String getOperationName() {
        return operationName;
    }
}

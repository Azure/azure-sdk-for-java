// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * Package containing core observability primitives and configuration options.
 * <p>
 *
 * These primitives are used by the client libraries to emit distributed traces and correlate logs.
 * Instrumentation is not operational without proper configuration of the OpenTelemetry SDK.
 * <p>
 *
 * Application developers who want to consume traces created by the client libraries should
 * use OpenTelemetry-compatible java agent or configure the OpenTelemetry SDK.
 * <p>
 *
 * Follow the https://opentelemetry.io/docs/languages/java/configuration/ for more details.
 * <p>
 *
 * Client libraries auto-discover global OpenTelemetry SDK instance configured by the java agent or
 * in the application code. Just create a client instance as usual as shown in the following code snippet:
 *
 * <p><strong>Clients auto-discover global OpenTelemetry</strong></p>
 *
 * <!-- src_embed io.clientcore.core.telemetry.useglobalopentelemetry -->
 * <pre>
 *
 * AutoConfiguredOpenTelemetrySdk.initialize&#40;&#41;;
 *
 * SampleClient client = new SampleClientBuilder&#40;&#41;.build&#40;&#41;;
 *
 * &#47;&#47; this call will be traced using OpenTelemetry SDK initialized globally
 * client.clientCall&#40;&#41;;
 *
 * </pre>
 * <!-- end io.clientcore.core.telemetry.useglobalopentelemetry -->
 * <p>
 *
 * Alternatively, application developers can pass OpenTelemetry SDK instance explicitly to the client libraries.
 *
 * <p><strong>Pass configured OpenTelemetry instance explicitly</strong></p>
 *
 * <!-- src_embed io.clientcore.core.telemetry.useexplicitopentelemetry -->
 * <pre>
 *
 * OpenTelemetry openTelemetry = AutoConfiguredOpenTelemetrySdk.initialize&#40;&#41;.getOpenTelemetrySdk&#40;&#41;;
 * HttpInstrumentationOptions instrumentationOptions = new HttpInstrumentationOptions&#40;&#41;
 *     .setTelemetryProvider&#40;openTelemetry&#41;;
 *
 * SampleClient client = new SampleClientBuilder&#40;&#41;.instrumentationOptions&#40;instrumentationOptions&#41;.build&#40;&#41;;
 *
 * &#47;&#47; this call will be traced using OpenTelemetry SDK provided explicitly
 * client.clientCall&#40;&#41;;
 *
 * </pre>
 * <!-- end io.clientcore.core.telemetry.useexplicitopentelemetry -->
 * <p>
 *
 * To correlate application and client library telemetry, application developers should
 * leverage implicit context propagation feature of OpenTelemetry API:
 *
 * <p><strong>Make application spans current to correlate them with library telemetry</strong></p>
 *
 * <!-- src_embed io.clientcore.core.telemetry.correlationwithimplicitcontext -->
 * <pre>
 *
 * Tracer tracer = GlobalOpenTelemetry.getTracer&#40;&quot;sample&quot;&#41;;
 * Span span = tracer.spanBuilder&#40;&quot;my-operation&quot;&#41;
 *     .startSpan&#40;&#41;;
 * SampleClient client = new SampleClientBuilder&#40;&#41;.build&#40;&#41;;
 *
 * try &#40;Scope scope = span.makeCurrent&#40;&#41;&#41; &#123;
 *     &#47;&#47; Client library will create span for the clientCall operation
 *     &#47;&#47; and will use current span &#40;my-operation&#41; as a parent.
 *     client.clientCall&#40;&#41;;
 * &#125; finally &#123;
 *     span.end&#40;&#41;;
 * &#125;
 *
 * </pre>
 * <!-- end io.clientcore.core.telemetry.correlationwithimplicitcontext -->
 * <p>
 *
 * Implicit context propagation works best in synchronous code. Implicit context propagation may not work in
 * asynchronous scenarios depending on the async framework used by the application, implementation details,
 * and OpenTelemetry instrumentation's used.
 * <p>
 * When writing asynchronous code, it's recommended to use explicit context propagation.
 *
 * <p><strong>Pass context explicitly to correlate them with library telemetry in async code</strong></p>
 *
 * <!-- src_embed io.clientcore.core.telemetry.correlationwithexplicitcontext -->
 * <pre>
 *
 * Tracer tracer = GlobalOpenTelemetry.getTracer&#40;&quot;sample&quot;&#41;;
 * Span span = tracer.spanBuilder&#40;&quot;my-operation&quot;&#41;
 *     .startSpan&#40;&#41;;
 *
 * SampleClient client = new SampleClientBuilder&#40;&#41;.build&#40;&#41;;
 *
 * &#47;&#47; Propagating context implicitly is preferred way in synchronous code.
 * &#47;&#47; However, in asynchronous code, context may need to be propagated explicitly using RequestContext
 * &#47;&#47; and explicit io.clientcore.core.util.Context.
 *
 * RequestContext context = RequestContext.builder&#40;&#41;
 *     .setInstrumentationContext&#40;Instrumentation.createInstrumentationContext&#40;span&#41;&#41;
 *     .build&#40;&#41;;
 *
 * &#47;&#47; run on another thread - all telemetry will be correlated with the span created above
 * client.clientCall&#40;context&#41;;
 *
 * </pre>
 * <!-- end io.clientcore.core.telemetry.correlationwithexplicitcontext -->
 */
package io.clientcore.core.instrumentation;

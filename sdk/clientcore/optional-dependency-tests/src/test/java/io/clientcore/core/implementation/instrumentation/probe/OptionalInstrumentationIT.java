// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.probe;

import io.clientcore.core.instrumentation.Instrumentation;
import io.clientcore.core.instrumentation.InstrumentationOptions;
import io.clientcore.core.instrumentation.SdkInstrumentationOptions;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.SpanKind;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerBuilder;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.ContextPropagators;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import org.slf4j.simple.SimpleServiceProvider;

import java.io.File;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests optional instrumentation with ClientCore on the module path and providers on the classpath.
 */
@EnabledForJreRange(min = JRE.JAVA_9)
public class OptionalInstrumentationIT {
    @Test
    public void classpathProvidersWorkWithNamedClientCore(@TempDir Path tempDir) throws Exception {
        String output = runProbe(tempDir, "parent");
        assertTrue(output.contains("ERROR " + Probe.class.getName() + " - "), output);
        assertTrue(output.contains("provider dispatch probe"), output);
        assertTrue(output.contains(System.lineSeparator() + "java.lang.IllegalStateException: dispatch throwable"),
            output);
        assertTrue(output.contains("failing span builder reached"), output);
    }

    @Test
    public void endsSpanWhenProviderRejectsStatus(@TempDir Path tempDir) throws Exception {
        String output = runProbe(tempDir, "status");
        assertTrue(output.contains("failing setStatus reached"), output);
        assertTrue(output.contains("provider span ended"), output);
    }

    private static String runProbe(Path tempDir, String failure) throws Exception {
        String classpath = String.join(File.pathSeparator, location(Probe.class), location(LoggerFactory.class),
            location(SimpleServiceProvider.class), location(OpenTelemetry.class), location(Context.class),
            location(Class.forName("io.opentelemetry.common.ComponentLoader")));
        Path outputFile = tempDir.resolve("probe.log");
        Process process = new ProcessBuilder(Paths.get(System.getProperty("java.home"), "bin", "java").toString(),
            "--module-path", location(ClientLogger.class), "--add-modules", "io.clientcore.core",
            "-Dorg.slf4j.simpleLogger.defaultLogLevel=debug", "-cp", classpath, Probe.class.getName(), failure)
                .redirectErrorStream(true)
                .redirectOutput(outputFile.toFile())
                .start();

        if (!process.waitFor(30, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new AssertionError("Optional instrumentation probe timed out.");
        }
        String output = new String(Files.readAllBytes(outputFile), StandardCharsets.UTF_8);
        assertEquals(0, process.exitValue(), output);
        return output;
    }

    private static String location(Class<?> type) throws Exception {
        return Paths.get(type.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
    }

    public static final class Probe {
        public static void main(String[] args) {
            new ClientLogger(Probe.class).atError()
                .setThrowable(new IllegalStateException("dispatch throwable"))
                .log("provider dispatch probe");

            boolean failOnParent = "parent".equals(args[0]);
            AtomicBoolean statusFailed = new AtomicBoolean();
            AtomicBoolean ended = new AtomicBoolean();
            Class<io.opentelemetry.api.trace.Span> spanType = io.opentelemetry.api.trace.Span.class;
            Object providerSpan = Proxy.newProxyInstance(spanType.getClassLoader(), new Class<?>[] { spanType },
                (proxy, method, arguments) -> {
                    switch (method.getName()) {
                        case "isRecording":
                            return true;

                        case "storeInContext":
                            return arguments[0];

                        case "setStatus":
                            statusFailed.set(true);
                            System.out.println("failing setStatus reached");
                            throw new IllegalStateException("Provider rejected span status.");

                        case "end":
                            ended.set(true);
                            System.out.println("provider span ended");
                            return null;

                        default:
                            return proxy;
                    }
                });
            SpanBuilder builder = (SpanBuilder) Proxy.newProxyInstance(SpanBuilder.class.getClassLoader(),
                new Class<?>[] { SpanBuilder.class }, (proxy, method, arguments) -> {
                    if (failOnParent && "setParent".equals(method.getName())) {
                        System.out.println("failing span builder reached");
                        throw new IllegalStateException("Provider could not set span parent.");
                    }
                    return "startSpan".equals(method.getName()) ? providerSpan : proxy;
                });
            TracerProvider tracerProvider = new TracerProvider() {
                @Override
                public Tracer get(String name) {
                    return spanName -> builder;
                }

                @Override
                public Tracer get(String name, String version) {
                    return get(name);
                }

                @Override
                public TracerBuilder tracerBuilder(String name) {
                    return new TracerBuilder() {
                        @Override
                        public TracerBuilder setSchemaUrl(String schemaUrl) {
                            return this;
                        }

                        @Override
                        public TracerBuilder setInstrumentationVersion(String version) {
                            return this;
                        }

                        @Override
                        public Tracer build() {
                            return get(name);
                        }
                    };
                }
            };
            OpenTelemetry provider = new OpenTelemetry() {
                @Override
                public TracerProvider getTracerProvider() {
                    return tracerProvider;
                }

                @Override
                public ContextPropagators getPropagators() {
                    return ContextPropagators.noop();
                }
            };
            Instrumentation instrumentation = Instrumentation.create(
                new InstrumentationOptions().setTelemetryProvider(provider), new SdkInstrumentationOptions("probe"));
            Span span = instrumentation.getTracer().spanBuilder("probe", SpanKind.CLIENT, null).startSpan();
            if (failOnParent) {
                if (span != Span.noop()) {
                    throw new AssertionError("Expected the no-op span after the provider failed.");
                }
            } else {
                span.end(new IllegalStateException("Operation failed."));
                if (!statusFailed.get() || !ended.get()) {
                    throw new AssertionError("Expected the provider span to end after setStatus failed.");
                }
            }
        }
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opentelemetry;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import reactor.util.context.Context;

import static com.azure.core.util.tracing.Tracer.PARENT_SPAN_KEY;

/**
 * Sample to demonstrate using {@link LoggingSpanExporter} to export telemetry events when asynchronously creating
 * and listing secrets from a Key Vault using the {@link SecretAsyncClient}.
 */
public class AsyncListKeyVaultSecretsLoggingExporterSample {

    private static final Tracer TRACER = configureLoggingSpanExporter();
    private static final String VAULT_URL = "<YOUR_VAULT_URL>";

    /**
     * The main method to run the application.
     *
     * @param args Ignored args.
     */
    public static void main(String[] args) {
        doClientWork();
    }

    /**
     * Configure the OpenTelemetry {@link LoggingSpanExporter} to enable tracing.
     *
     * @return The OpenTelemetry {@link Tracer} instance.
     */
    private static Tracer configureLoggingSpanExporter() {
        // Tracer provider configured to export spans with SimpleSpanProcessor using
        // the Azure Monitor exporter.
        SdkTracerProvider tracerProvider =
            SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(new LoggingSpanExporter()))
                .build();

        return OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .buildAndRegisterGlobal()
            .getTracer("Async-List-KV-Secrets-Sample");
    }

    /**
     * Create a secret and list all the secrets for a Key Vault using the
     * {@link SecretAsyncClient} with distributed tracing enabled and using the Azure Monitor Exporter
     * to export telemetry events.
     */
    private static void doClientWork() {
        SecretAsyncClient secretAsyncClient = new SecretClientBuilder()
            .vaultUrl(VAULT_URL)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

        Span userParentSpan = TRACER.spanBuilder("user-parent-span").startSpan();

        Context traceContext = Context.of(PARENT_SPAN_KEY, Span.current());

        secretAsyncClient.setSecret(new KeyVaultSecret("Secret1", "password1"))
            .contextWrite(traceContext)
            .subscribe(secretResponse -> System.out.printf("Secret with name: %s%n", secretResponse.getName()));
        secretAsyncClient.listPropertiesOfSecrets()
            .contextWrite(traceContext)
            .doOnNext(secretBase -> secretAsyncClient.getSecret(secretBase.getName())
                .contextWrite(traceContext)
                .doOnNext(secret -> System.out.printf("Secret with name: %s%n", secret.getName())))
            .blockLast();

        userParentSpan.end();
    }
}

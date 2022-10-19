// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opentelemetry;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

import java.time.Duration;

/**
 * Sample to demonstrate using {@link JaegerGrpcSpanExporter} to export telemetry events when asynchronously creating
 * and listing secrets from a Key Vault using the {@link SecretClient}.
 */
public class ListKeyVaultSecretsJaegerExporterSample {
    private static final Tracer TRACER = configureJaegerExporter();
    private static final String VAULT_URL = "<YOUR_VAULT_URL>";

    /**
     * The main method to run the application.
     *
     * @param args Ignored args.
     */
    public static void main(String[] args) {
        SecretClient secretClient = new SecretClientBuilder()
            .vaultUrl(VAULT_URL)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        doClientWork(secretClient);
    }

    /**
     * Configure the OpenTelemetry {@link JaegerGrpcSpanExporter} to enable tracing.
     *
     * @return The OpenTelemetry {@link Tracer} instance.
     */
    private static Tracer configureJaegerExporter() {
        // Export traces to Jaeger
        JaegerGrpcSpanExporter jaegerExporter =
            JaegerGrpcSpanExporter.builder()
                .setEndpoint("http://localhost:14250")
                .setTimeout(Duration.ofMinutes(30000))
                .build();

        // Set to process the spans by the Jaeger Exporter
        return OpenTelemetrySdk.builder()
            .setTracerProvider(
                SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(jaegerExporter)).build())
            .buildAndRegisterGlobal()
            .getTracer("List-KV-Secrets-Sample");
    }

    /**
     * Create a secret and list all the secrets for a Key Vault using the
     * {@link SecretClient} with distributed tracing enabled and using the Jaeger exporter to export telemetry events.
     */
    @SuppressWarnings("try")
    private static void doClientWork(SecretClient secretClient) {

        Span span = TRACER.spanBuilder("my-span").startSpan();
        try (Scope s = span.makeCurrent()) {
            // current span propagates into synchronous calls automatically. ApplicationInsights or OpenTelemetry agent
            // also propagate context through async reactor calls.
            secretClient.setSecret(new KeyVaultSecret("StorageAccountPassword", "password"));
            secretClient.listPropertiesOfSecrets().forEach(secretProperties -> {
                KeyVaultSecret secret = secretClient.getSecret(secretProperties.getName());
                System.out.printf("Retrieved Secret with name: %s%n", secret.getName());
            });
        } finally {
            span.end();
        }
    }
}

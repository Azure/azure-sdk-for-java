// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opentelemetry.samples;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;

/**
 * Sample to demonstrate using {@link OtlpGrpcSpanExporter} to export telemetry events when asynchronously creating
 * and listing secrets from a Key Vault using the {@link SecretClient}.
 */
public class ListKeyVaultSecretsOtlpExporterSample {
    private static final String VAULT_URL = "<YOUR_VAULT_URL>";

    /**
     * The main method to run the application.
     *
     * @param args Ignored args.
     */
    @SuppressWarnings("try")
    public static void main(String[] args) {
        OpenTelemetrySdk openTelemetry = configureTracing();
        Tracer tracer = openTelemetry.getTracer("sample");

        SecretClient secretClient = new SecretClientBuilder()
            .vaultUrl(VAULT_URL)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        Span span = tracer.spanBuilder("my-span").startSpan();
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

        openTelemetry.close();
    }

    /**
     * Configure the OpenTelemetry to export spans to an OTLP endpoint with {@link OtlpGrpcSpanExporter}.
     */
    private static OpenTelemetrySdk configureTracing() {
        OtlpGrpcSpanExporter spanExporter =
            OtlpGrpcSpanExporter.builder()
                .setEndpoint("http://localhost:4317")
                .build();

        return OpenTelemetrySdk.builder()
            .setTracerProvider(
                SdkTracerProvider.builder()
                    .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
                    .build())
            .buildAndRegisterGlobal();
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opentelemetry;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
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
        doClientWork();
    }

    /**
     * Configure the OpenTelemetry {@link JaegerGrpcSpanExporter} to enable tracing.
     *
     * @return The OpenTelemetry {@link Tracer} instance.
     */
    private static Tracer configureJaegerExporter() {
        // Create a channel towards Jaeger end point
        ManagedChannel jaegerChannel =
            ManagedChannelBuilder.forAddress("localhost", 14250).usePlaintext().build();
        // Export traces to Jaeger
        JaegerGrpcSpanExporter jaegerExporter =
            JaegerGrpcSpanExporter.builder()
                .setChannel(jaegerChannel)
                .setTimeout(Duration.ofMinutes(30000))
                .build();

        // Set to process the spans by the Jaeger Exporter
        OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder()
            .setTracerProvider(
                SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(jaegerExporter)).build())
            .build();
        return openTelemetry.getSdkTracerProvider().get("List-KV-Secrets-Sample");
    }

    /**
     * Create a secret and list all the secrets for a Key Vault using the
     * {@link SecretClient} with distributed tracing enabled and using the Jaeger exporter to export telemetry events.
     */
    private static void doClientWork() {
        SecretClient secretClient = new SecretClientBuilder()
            .vaultUrl(VAULT_URL)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        Span userParentSpan = TRACER.spanBuilder("user-parent-span").startSpan();

        final Scope scope = userParentSpan.makeCurrent();
        try {
            secretClient.setSecret(new KeyVaultSecret("StorageAccountPassword", "password"));
            secretClient.listPropertiesOfSecrets().forEach(secretProperties -> {
                // Thread bound (sync) calls will automatically pick up the parent span and you don't need to pass it explicitly.
                KeyVaultSecret secret = secretClient.getSecret(secretProperties.getName());
                System.out.printf("Retrieved Secret with name: %s%n", secret.getName());
            });
        } finally {
            userParentSpan.end();
            scope.close();
        }
    }
}

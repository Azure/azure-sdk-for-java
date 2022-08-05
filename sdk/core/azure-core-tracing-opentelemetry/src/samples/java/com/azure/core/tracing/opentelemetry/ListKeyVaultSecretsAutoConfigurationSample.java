// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opentelemetry;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import reactor.util.context.Context;

import static com.azure.core.util.tracing.Tracer.PARENT_TRACE_CONTEXT_KEY;

/**
 * Sample to demonstrate configuration using environment variables or system properties with  {@link AutoConfiguredOpenTelemetrySdk}
 * https://github.com/open-telemetry/opentelemetry-java/tree/main/sdk-extensions/autoconfigure
 * and listing secrets from a Key Vault using the {@link SecretAsyncClient}.
 */
public class ListKeyVaultSecretsAutoConfigurationSample {
    private static final String VAULT_URL = "<YOUR_VAULT_URL>";
    @SuppressWarnings("try")
    public void syncClient() {
        Tracer tracer = configureTracing();

        // BEGIN: readme-sample-context-auto-propagation
        SecretClient secretClient = new SecretClientBuilder()
            .vaultUrl(VAULT_URL)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        Span span = tracer.spanBuilder("my-span").startSpan();
        try (Scope s = span.makeCurrent()) {
            // ApplicationInsights or OpenTelemetry agent propagate context through async reactor calls.
            // So SecretClient here creates spans that are children of my-span
            System.out.printf("Secret with name: %s%n", secretClient.setSecret(new KeyVaultSecret("Secret1", "password1")).getName());
            secretClient.listPropertiesOfSecrets().forEach(secretBase ->
                System.out.printf("Secret with name: %s%n", secretClient.getSecret(secretBase.getName())));
        } finally {
            span.end();
        }

        // END: readme-sample-context-auto-propagation
    }

    public void asyncClient() {
        Tracer tracer = configureTracing();

        // BEGIN: readme-sample-context-manual-propagation
        SecretAsyncClient secretAsyncClient = new SecretClientBuilder()
            .vaultUrl(VAULT_URL)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

        Span span = tracer.spanBuilder("my-span").startSpan();
        // when using async clients and instrumenting without ApplicationInsights or OpenTelemetry agent, context needs to be propagated manually
        Context traceContext = Context.of(PARENT_TRACE_CONTEXT_KEY, io.opentelemetry.context.Context.current().with(span));
        try {
            secretAsyncClient.setSecret(new KeyVaultSecret("Secret1", "password1"))
                .contextWrite(traceContext)
                .subscribe(secretResponse -> System.out.printf("Secret with name: %s%n", secretResponse.getName()));
            secretAsyncClient.listPropertiesOfSecrets()
                .contextWrite(traceContext)
                .doOnNext(secretBase -> secretAsyncClient.getSecret(secretBase.getName())
                    .contextWrite(traceContext)
                    .doOnNext(secret -> System.out.printf("Secret with name: %s%n", secret.getName())))
                .blockLast();
        } finally {
            span.end();
        }

        // END: readme-sample-context-manual-propagation
    }

    /**
     * Configure the OpenTelemetry {@link LoggingSpanExporter} to enable tracing.
     *
     * @return The OpenTelemetry {@link Tracer} instance.
     */
    private static Tracer configureTracing() {
        OpenTelemetrySdk sdk = AutoConfiguredOpenTelemetrySdk.initialize()
            .getOpenTelemetrySdk();
        return sdk.getTracer("Async-List-KV-Secrets-Sample");
    }
}

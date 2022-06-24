// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opentelemetry;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
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
public class AsyncListKeyVaultSecretsAutoConfigurationSample {
    private static Tracer tracer = configureTracing();
    private static final String VAULT_URL = "<YOUR_VAULT_URL>";

    /**
     * The main method to run the application.
     *
     * @param args Ignored args.
     */
    public static void main(String[] args) {
        SecretAsyncClient secretAsyncClient = new SecretClientBuilder()
            .vaultUrl(VAULT_URL)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();
        doClientWork(secretAsyncClient);
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

    /**
     * Adds a secret and list all the secrets for a Key Vault using the {@link SecretAsyncClient} with distributed tracing enabled
     * and context propagated magically.
     */
    @SuppressWarnings("try")
    private static void doClientWork(SecretAsyncClient secretAsyncClient) {
        Span span = tracer.spanBuilder("my-span").startSpan();
        try (Scope s = span.makeCurrent()) {
            // current span propagates into synchronous calls automatically. ApplicationInsights or OpenTelemetry agent
            // also propagate context through async reactor calls.
            // if you use async client and instrument manually without agent help, please follow doClientWorkExplicitContext example
            secretAsyncClient.setSecret(new KeyVaultSecret("Secret1", "password1"))
                .subscribe(secretResponse -> System.out.printf("Secret with name: %s%n", secretResponse.getName()));
            secretAsyncClient.listPropertiesOfSecrets()
                .doOnNext(secretBase -> secretAsyncClient.getSecret(secretBase.getName())
                    .doOnNext(secret -> System.out.printf("Secret with name: %s%n", secret.getName())))
                .blockLast();
        } finally {
            span.end();
        }
    }

    /**
     * Adds a secret and list all the secrets for a Key Vault using the {@link SecretAsyncClient} with distributed tracing enabled
     * and context propagated explicitly.
     */
    private static void doClientWorkExplicitContext(SecretAsyncClient secretAsyncClient) {
        Span userParentSpan = tracer.spanBuilder("my-span").startSpan();

        Context traceContext = Context.of(PARENT_TRACE_CONTEXT_KEY, io.opentelemetry.context.Context.current().with(userParentSpan));

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

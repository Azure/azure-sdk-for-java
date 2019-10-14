// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opencensus;

import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.Secret;
import io.opencensus.common.Scope;
import io.opencensus.exporter.trace.zipkin.ZipkinTraceExporter;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.config.TraceParams;
import io.opencensus.trace.samplers.Samplers;
import reactor.util.context.Context;

import java.util.concurrent.Semaphore;

import static com.azure.core.tracing.opencensus.OpenCensusTracer.OPENCENSUS_SPAN_KEY;

/**
 * Sample demonstrates how to list secrets and versions of a given secret in the key vault with tracing enabled.
 */
public class ListOperationsAsync {
    /**
     * Authenticates with the key vault and shows how to list secrets and list versions of a specific secret in the key
     * vault with trace spans exported to Zipkin.
     *
     * Please refer to the <a href=https://zipkin.io/pages/quickstart>Quickstart Zipkin</a> for more documentation on
     * using a Zipkin exporter.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when invalid key vault endpoint is passed.
     * @throws InterruptedException when the thread is interrupted in sleep mode.
     */
    public static void main(String[] args) throws IllegalArgumentException, InterruptedException {
        ZipkinTraceExporter.createAndRegister("http://localhost:9411/api/v2/spans", "tracing-to-zipkin-service");

        TraceConfig traceConfig = Tracing.getTraceConfig();
        TraceParams activeTraceParams = traceConfig.getActiveTraceParams();
        traceConfig.updateActiveTraceParams(activeTraceParams.toBuilder().setSampler(Samplers.alwaysSample()).build());

        Tracer tracer = Tracing.getTracer();

        // Instantiate a client that will be used to call the service. Notice that the client is using default Azure
        // credentials. To make default credentials work, ensure that environment variables 'AZURE_CLIENT_ID',
        // 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.
        SecretAsyncClient client = new SecretClientBuilder()
            .endpoint("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

        Semaphore semaphore = new Semaphore(1);
        Scope scope = tracer.spanBuilder("user-parent-span").startScopedSpan();

        semaphore.acquire();
        Context traceContext = Context.of(OPENCENSUS_SPAN_KEY, tracer.getCurrentSpan());
        // Let's create secrets holding storage and bank accounts credentials. if the secret
        // already exists in the key vault, then a new version of the secret is created.
        client.setSecret(new Secret("StorageAccountPassword", "password"))
            .then(client.setSecret(new Secret("BankAccountPassword", "password")))
            .subscriberContext(traceContext)
            .subscribe(secretResponse ->
                System.out.printf("Secret is created with name %s and value %s %n", secretResponse.getName(), secretResponse.getValue()),
                err -> {
                    System.out.printf("Error thrown when enqueue the message. Error message: %s%n",
                        err.getMessage());
                    scope.close();
                    semaphore.release();
                },
                () -> {
                    semaphore.release();
                });

        semaphore.acquire();
        // You need to check if any of the secrets are sharing same values. Let's list the secrets and print their values.
        // List operations don't return the secrets with value information. So, for each returned secret we call getSecret to get the secret with its value information.
        client.listSecrets()
            .subscriberContext(traceContext)
            .subscribe(secretBase -> client.getSecret(secretBase)
                .subscriberContext(traceContext)
                .subscribe(secret -> System.out.printf("Received secret with name %s and value %s%n",
                    secret.getName(), secret.getValue())));

        // The bank account password got updated, so you want to update the secret in key vault to ensure it reflects the new password.
        // Calling setSecret on an existing secret creates a new version of the secret in the key vault with the new value.
        client.setSecret("BankAccountPassword", "new password")
            .subscriberContext(traceContext)
            .subscribe(secretResponse ->
                System.out.printf("Secret is created with name %s and value %s %n", secretResponse.getName(), secretResponse.getValue()),
                err -> {
                    System.out.printf("Error thrown when enqueue the message. Error message: %s%n",
                        err.getMessage());
                    scope.close();
                    semaphore.release();
                },
                () -> {
                    semaphore.release();
                });

        semaphore.acquire();

        // You need to check all the different values your bank account password secret had previously. Lets print all the versions of this secret.
        client.listSecretVersions("BankAccountPassword")
            .subscriberContext(traceContext)
            .subscribe(secretBase -> System.out.printf("Received secret's version with name %s%n",
                secretBase.getName()));

        scope.close();
        Tracing.getExportComponent().shutdown();
    }
}

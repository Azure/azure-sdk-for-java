// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opencensus;

import com.azure.core.util.Context;
import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.Secret;
import com.azure.security.keyvault.secrets.models.SecretBase;
import io.opencensus.common.Scope;
import io.opencensus.exporter.trace.zipkin.ZipkinTraceExporter;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.config.TraceParams;
import io.opencensus.trace.samplers.Samplers;

import java.time.OffsetDateTime;

import static com.azure.core.util.tracing.Tracer.OPENCENSUS_SPAN_KEY;

/**
 * Sample demonstrates how to list secrets and versions of a given secret in the key vault with tracing enabled.
 */
public class ListOperations {
    /**
     * Authenticates with the key vault and shows how to list secrets and list versions of a specific secret in the key
     * vault with trace spans exported to zipkin.
     *
     * Please refer to the  <a href=https://zipkin.io/pages/quickstart>Quickstart Zipkin</a> for more documentation on
     * using a zipkin exporter.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when invalid key vault endpoint is passed.
     */
    public static void main(String[] args) throws IllegalArgumentException {
        ZipkinTraceExporter.createAndRegister("http://localhost:9411/api/v2/spans", "tracing-to-zipkin-service");

        TraceConfig traceConfig = Tracing.getTraceConfig();
        TraceParams activeTraceParams = traceConfig.getActiveTraceParams();
        traceConfig.updateActiveTraceParams(activeTraceParams.toBuilder().setSampler(Samplers.alwaysSample()).build());

        Tracer tracer = Tracing.getTracer();

        // Instantiate a client that will be used to call the service. Notice that the client is using default Azure
        // credentials. To make default credentials work, ensure that environment variables 'AZURE_CLIENT_ID',
        // 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.
        SecretClient client = new SecretClientBuilder()
            .endpoint("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        Scope scope = tracer.spanBuilder("user-parent-span").startScopedSpan();
        try {

            Context traceContext = new Context(OPENCENSUS_SPAN_KEY, tracer.getCurrentSpan());
            // Let's create secrets holding storage and bank accounts credentials valid for 1 year. if the secret
            // already exists in the key vault, then a new version of the secret is created.
            client.setSecretWithResponse(new Secret("StorageAccountPassword", "password")
                .expires(OffsetDateTime.now().plusYears(1)), traceContext);

            client.setSecretWithResponse(new Secret("BankAccountPassword", "password")
                .expires(OffsetDateTime.now().plusYears(1)), traceContext);

            // You need to check if any of the secrets are sharing same values. Let's list the secrets and print their values.
            // List operations don't return the secrets with value information. So, for each returned secret we call getSecret to get the secret with its value information.
            for (SecretBase secret : client.listSecrets(traceContext)) {
                Secret secretWithValue = client.getSecretWithResponse(secret, traceContext).value();
                System.out.printf("Received secret with name %s and value %s%n", secretWithValue.name(), secretWithValue.value());
            }

            // The bank account password got updated, so you want to update the secret in key vault to ensure it reflects the new password.
            // Calling setSecret on an existing secret creates a new version of the secret in the key vault with the new value.
            client.setSecret("BankAccountPassword", "newPassword");

            // You need to check all the different values your bank account password secret had previously. Lets print all the versions of this secret.
            for (SecretBase secret : client.listSecretVersions("BankAccountPassword", traceContext)) {
                Secret secretWithValue = client.getSecretWithResponse(secret, traceContext).value();
                System.out.printf("Received secret's version with name %s and value %s%n", secretWithValue.name(), secretWithValue.value());
            }
        } finally {
            scope.close();
        }

        Tracing.getExportComponent().shutdown();
    }
}

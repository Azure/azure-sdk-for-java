// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient;
import com.azure.ai.metricsadvisor.administration.models.DataSourceAuthenticationType;
import com.azure.ai.metricsadvisor.administration.models.DataSourceCredentialEntity;
import com.azure.core.http.HttpClient;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.CoreUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DatasourceCredentialAsyncTest extends DatasourceCredentialTestBase {
    private MetricsAdvisorAdministrationAsyncClient client;

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    @LiveOnly
    void createSqlConnectionString(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        final AtomicReference<String> credentialId = new AtomicReference<>();
        try {
            // Arrange
            client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion, false).buildAsyncClient();
            super.creatDatasourceCredentialRunner(expectedCredential ->
                // Act & Assert
                StepVerifier.create(client.createDataSourceCredential(expectedCredential))
                    .assertNext(createdCredential -> {
                        credentialId.set(createdCredential.getId());
                        super.validateCredentialResult(expectedCredential,
                            createdCredential,
                            DataSourceAuthenticationType.AZURE_SQL_CONNECTION_STRING);
                    })
                    .expectComplete()
                    .verify(DEFAULT_TIMEOUT), DataSourceAuthenticationType.AZURE_SQL_CONNECTION_STRING);

        } finally {
            if (!CoreUtils.isNullOrEmpty(credentialId.get())) {
                Mono<Void> deleteCredential = client.deleteDataSourceCredential(credentialId.get());
                StepVerifier.create(deleteCredential)
                    .expectComplete()
                    .verify(DEFAULT_TIMEOUT);
            }
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    @LiveOnly
    void createDataLakeGen2SharedKey(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        final AtomicReference<String> credentialId = new AtomicReference<>();
        try {
            // Arrange
            client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion, false).buildAsyncClient();
            super.creatDatasourceCredentialRunner(expectedCredential ->
                // Act & Assert
                StepVerifier.create(client.createDataSourceCredential(expectedCredential))
                    .assertNext(createdCredential -> {
                        credentialId.set(createdCredential.getId());
                        super.validateCredentialResult(expectedCredential,
                            createdCredential,
                            DataSourceAuthenticationType.DATA_LAKE_GEN2_SHARED_KEY);
                    })
                    .expectComplete()
                    .verify(DEFAULT_TIMEOUT), DataSourceAuthenticationType.DATA_LAKE_GEN2_SHARED_KEY);

        } finally {
            if (!CoreUtils.isNullOrEmpty(credentialId.get())) {
                Mono<Void> deleteCredential = client.deleteDataSourceCredential(credentialId.get());
                StepVerifier.create(deleteCredential)
                    .expectComplete()
                    .verify(DEFAULT_TIMEOUT);
            }
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    @LiveOnly
    void createServicePrincipal(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        final AtomicReference<String> credentialId = new AtomicReference<>();
        try {
            // Arrange
            client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion, false).buildAsyncClient();
            super.creatDatasourceCredentialRunner(expectedCredential ->
                // Act & Assert
                StepVerifier.create(client.createDataSourceCredential(expectedCredential))
                    .assertNext(createdCredential -> {
                        credentialId.set(createdCredential.getId());
                        super.validateCredentialResult(expectedCredential,
                            createdCredential,
                            DataSourceAuthenticationType.SERVICE_PRINCIPAL);
                    })
                    .expectComplete()
                    .verify(DEFAULT_TIMEOUT), DataSourceAuthenticationType.SERVICE_PRINCIPAL);

        } finally {
            if (!CoreUtils.isNullOrEmpty(credentialId.get())) {
                Mono<Void> deleteCredential = client.deleteDataSourceCredential(credentialId.get());
                StepVerifier.create(deleteCredential)
                    .expectComplete()
                    .verify(DEFAULT_TIMEOUT);
            }
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    @LiveOnly
    void createServicePrincipalInKV(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        final AtomicReference<String> credentialId = new AtomicReference<>();
        try {
            // Arrange
            client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion, false).buildAsyncClient();
            super.creatDatasourceCredentialRunner(expectedCredential ->
                // Act & Assert
                StepVerifier.create(client.createDataSourceCredential(expectedCredential))
                    .assertNext(createdCredential -> {
                        credentialId.set(createdCredential.getId());
                        super.validateCredentialResult(expectedCredential,
                            createdCredential,
                            DataSourceAuthenticationType.SERVICE_PRINCIPAL_IN_KV);
                    })
                    .expectComplete()
                    .verify(DEFAULT_TIMEOUT), DataSourceAuthenticationType.SERVICE_PRINCIPAL_IN_KV);

        } finally {
            if (!CoreUtils.isNullOrEmpty(credentialId.get())) {
                Mono<Void> deleteCredential = client.deleteDataSourceCredential(credentialId.get());
                StepVerifier.create(deleteCredential)
                    .expectComplete()
                    .verify(DEFAULT_TIMEOUT);
            }
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @LiveOnly
    void testListDataSourceCredentials(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        AtomicReference<List<String>> createdCredentialIdList = new AtomicReference<>();
        try {
            client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion, false).buildAsyncClient();

            super.listDatasourceCredentialRunner(inputCredentialList -> {
                final List<String> ids =
                    inputCredentialList.stream()
                        .map(credential -> client.createDataSourceCredential(credential).block())
                        .map(credential -> credential.getId())
                        .collect(Collectors.toList());
                createdCredentialIdList.set(ids);

                List<DataSourceCredentialEntity> retrievedCredentialList = new ArrayList<>();
                StepVerifier.create(client.listDataSourceCredentials())
                    .thenConsumeWhile(e -> {
                        retrievedCredentialList.add(e);
                        return retrievedCredentialList.size() < inputCredentialList.size();
                    })
                    .thenCancel().verify(DEFAULT_TIMEOUT);

                assertEquals(inputCredentialList.size(), retrievedCredentialList.size());
            });
        } finally {
            if (!CoreUtils.isNullOrEmpty(createdCredentialIdList.get())) {
                createdCredentialIdList.get().forEach(credentialId ->
                    StepVerifier.create(client.deleteDataSourceCredential(credentialId))
                        .expectComplete()
                        .verify(DEFAULT_TIMEOUT));
            }
        }
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient;
import com.azure.ai.metricsadvisor.administration.models.DatasourceAuthenticationType;
import com.azure.ai.metricsadvisor.administration.models.DatasourceCredentialEntity;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.CoreUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DatasourceCredentialTest extends DatasourceCredentialTestBase {
    private MetricsAdvisorAdministrationClient client;

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    void createSqlConnectionString(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        final AtomicReference<String> credentialId = new AtomicReference<>();
        try {
            // Arrange
            client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion, true).buildClient();
            super.creatDatasourceCredentialRunner(expectedCredential -> {
                // Act & Assert
                DatasourceCredentialEntity createdCredential
                        = client.createDatasourceCredential(expectedCredential);
                credentialId.set(createdCredential.getId());
                super.validateCredentialResult(expectedCredential,
                    createdCredential,
                    DatasourceAuthenticationType.AZURE_SQL_CONNECTION_STRING);
            }, DatasourceAuthenticationType.AZURE_SQL_CONNECTION_STRING);

        } finally {
            if (!CoreUtils.isNullOrEmpty(credentialId.get())) {
                client.deleteDatasourceCredential(credentialId.get());
            }
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    void createDataLakeGen2SharedKey(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        final AtomicReference<String> credentialId = new AtomicReference<>();
        try {
            // Arrange
            client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion, true).buildClient();
            super.creatDatasourceCredentialRunner(expectedCredential -> {
                // Act & Assert
                DatasourceCredentialEntity createdCredential
                    = client.createDatasourceCredential(expectedCredential);
                credentialId.set(createdCredential.getId());
                super.validateCredentialResult(expectedCredential,
                    createdCredential,
                    DatasourceAuthenticationType.DATA_LAKE_GEN2_SHARED_KEY);
            }, DatasourceAuthenticationType.DATA_LAKE_GEN2_SHARED_KEY);

        } finally {
            if (!CoreUtils.isNullOrEmpty(credentialId.get())) {
                client.deleteDatasourceCredential(credentialId.get());
            }
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    void createServicePrincipal(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        final AtomicReference<String> credentialId = new AtomicReference<>();
        try {
            // Arrange
            client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion, true).buildClient();
            super.creatDatasourceCredentialRunner(expectedCredential -> {
                // Act & Assert
                DatasourceCredentialEntity createdCredential
                    = client.createDatasourceCredential(expectedCredential);
                credentialId.set(createdCredential.getId());
                super.validateCredentialResult(expectedCredential,
                    createdCredential,
                    DatasourceAuthenticationType.SERVICE_PRINCIPAL);
            }, DatasourceAuthenticationType.SERVICE_PRINCIPAL);

        } finally {
            if (!CoreUtils.isNullOrEmpty(credentialId.get())) {
                client.deleteDatasourceCredential(credentialId.get());
            }
        }
    }

    @Override
    void createServicePrincipalInKV(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        final AtomicReference<String> credentialId = new AtomicReference<>();
        try {
            // Arrange
            client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion, true).buildClient();
            super.creatDatasourceCredentialRunner(expectedCredential -> {
                // Act & Assert
                DatasourceCredentialEntity createdCredential
                    = client.createDatasourceCredential(expectedCredential);
                credentialId.set(createdCredential.getId());
                super.validateCredentialResult(expectedCredential,
                    createdCredential,
                    DatasourceAuthenticationType.SERVICE_PRINCIPAL_IN_KV);
            }, DatasourceAuthenticationType.SERVICE_PRINCIPAL_IN_KV);

        } finally {
            if (!CoreUtils.isNullOrEmpty(credentialId.get())) {
                client.deleteDatasourceCredential(credentialId.get());
            }
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    void testListDataSourceCredentials(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        AtomicReference<List<String>> createdCredentialIdList = new AtomicReference<>();
        try {
            client = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion, true).buildClient();

            super.listDatasourceCredentialRunner(inputCredentialList -> {
                final List<String> ids =
                    inputCredentialList.stream()
                        .map(credential -> client.createDatasourceCredential(credential))
                        .map(credential -> credential.getId())
                        .collect(Collectors.toList());
                createdCredentialIdList.set(ids);

                List<DatasourceCredentialEntity> retrievedCredentialList = new ArrayList<>();
                PagedIterable<DatasourceCredentialEntity> credentialsIterable = client.listDatasourceCredentials();
                for (DatasourceCredentialEntity credential: credentialsIterable) {
                    retrievedCredentialList.add(credential);
                    if (retrievedCredentialList.size() >= inputCredentialList.size()) {
                        break;
                    }
                }
                assertEquals(inputCredentialList.size(), retrievedCredentialList.size());
            });
        } finally {
            if (!CoreUtils.isNullOrEmpty(createdCredentialIdList.get())) {
                createdCredentialIdList.get().forEach(credentialId -> client.deleteDatasourceCredential(credentialId));
            }
        }
    }
}

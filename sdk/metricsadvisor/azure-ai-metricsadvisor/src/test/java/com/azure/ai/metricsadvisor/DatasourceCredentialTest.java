// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient;
import com.azure.ai.metricsadvisor.administration.models.DataSourceAuthenticationType;
import com.azure.ai.metricsadvisor.administration.models.DataSourceCredentialEntity;
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
                DataSourceCredentialEntity createdCredential
                        = client.createDataSourceCredential(expectedCredential);
                credentialId.set(createdCredential.getId());
                super.validateCredentialResult(expectedCredential,
                    createdCredential,
                    DataSourceAuthenticationType.AZURE_SQL_CONNECTION_STRING);
            }, DataSourceAuthenticationType.AZURE_SQL_CONNECTION_STRING);

        } finally {
            if (!CoreUtils.isNullOrEmpty(credentialId.get())) {
                client.deleteDataSourceCredential(credentialId.get());
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
                DataSourceCredentialEntity createdCredential
                    = client.createDataSourceCredential(expectedCredential);
                credentialId.set(createdCredential.getId());
                super.validateCredentialResult(expectedCredential,
                    createdCredential,
                    DataSourceAuthenticationType.DATA_LAKE_GEN2_SHARED_KEY);
            }, DataSourceAuthenticationType.DATA_LAKE_GEN2_SHARED_KEY);

        } finally {
            if (!CoreUtils.isNullOrEmpty(credentialId.get())) {
                client.deleteDataSourceCredential(credentialId.get());
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
                DataSourceCredentialEntity createdCredential
                    = client.createDataSourceCredential(expectedCredential);
                credentialId.set(createdCredential.getId());
                super.validateCredentialResult(expectedCredential,
                    createdCredential,
                    DataSourceAuthenticationType.SERVICE_PRINCIPAL);
            }, DataSourceAuthenticationType.SERVICE_PRINCIPAL);

        } finally {
            if (!CoreUtils.isNullOrEmpty(credentialId.get())) {
                client.deleteDataSourceCredential(credentialId.get());
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
                DataSourceCredentialEntity createdCredential
                    = client.createDataSourceCredential(expectedCredential);
                credentialId.set(createdCredential.getId());
                super.validateCredentialResult(expectedCredential,
                    createdCredential,
                    DataSourceAuthenticationType.SERVICE_PRINCIPAL_IN_KV);
            }, DataSourceAuthenticationType.SERVICE_PRINCIPAL_IN_KV);

        } finally {
            if (!CoreUtils.isNullOrEmpty(credentialId.get())) {
                client.deleteDataSourceCredential(credentialId.get());
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
                        .map(credential -> client.createDataSourceCredential(credential))
                        .map(credential -> credential.getId())
                        .collect(Collectors.toList());
                createdCredentialIdList.set(ids);

                List<DataSourceCredentialEntity> retrievedCredentialList = new ArrayList<>();
                PagedIterable<DataSourceCredentialEntity> credentialsIterable = client.listDataSourceCredentials();
                for (DataSourceCredentialEntity credential: credentialsIterable) {
                    retrievedCredentialList.add(credential);
                    if (retrievedCredentialList.size() >= inputCredentialList.size()) {
                        break;
                    }
                }
                assertEquals(inputCredentialList.size(), retrievedCredentialList.size());
            });
        } finally {
            if (!CoreUtils.isNullOrEmpty(createdCredentialIdList.get())) {
                createdCredentialIdList.get().forEach(credentialId -> client.deleteDataSourceCredential(credentialId));
            }
        }
    }
}

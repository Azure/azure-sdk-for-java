// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.projects.models.Connection;
import com.azure.ai.projects.models.ConnectionType;
import com.azure.ai.projects.models.DatasetVersion;
import com.azure.ai.projects.models.Deployment;
import com.azure.ai.projects.models.DeploymentType;
import com.azure.ai.projects.models.FileDatasetVersion;
import com.azure.ai.projects.models.Index;
import com.azure.core.http.HttpClient;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

public class ClientTestBase extends TestProxyTestBase {

    private boolean sanitizersRemoved = false;

    protected AIProjectClientBuilder getClientBuilder(HttpClient httpClient,
        AIProjectsServiceVersion aiProjectsServiceVersion) {

        AIProjectClientBuilder builder = new AIProjectClientBuilder()
            .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient);
        TestMode testMode = getTestMode();
        if (testMode != TestMode.LIVE) {
            addCustomMatchers();
            addTestRecordCustomSanitizers();
            if (!sanitizersRemoved) {
                interceptorManager.removeSanitizers("AZSDK3430", "AZSDK3493", "AZSDK2015");
                sanitizersRemoved = true;
            }
        }

        if (testMode == TestMode.PLAYBACK) {
            builder.endpoint("https://localhost:8080").credential(new MockTokenCredential());
        } else if (testMode == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
                .endpoint(Configuration.getGlobalConfiguration().get("AI_PROJECTS_ENDPOINT"))
                .credential(new DefaultAzureCredentialBuilder().build());
        } else {
            builder.endpoint(Configuration.getGlobalConfiguration().get("AI_PROJECTS_ENDPOINT"))
                .credential(new DefaultAzureCredentialBuilder().build());
        }

        String version = Configuration.getGlobalConfiguration().get("SERVICE_VERSION");
        AIProjectsServiceVersion serviceVersion
            = version != null ? AIProjectsServiceVersion.valueOf(version) : aiProjectsServiceVersion;
        builder.serviceVersion(serviceVersion);
        return builder;
    }

    private void addTestRecordCustomSanitizers() {

        ArrayList<TestProxySanitizer> sanitizers = new ArrayList<>();
        sanitizers.add(new TestProxySanitizer("$..key", null, "REDACTED", TestProxySanitizerType.BODY_KEY));
        sanitizers.add(new TestProxySanitizer("$..endpoint", "https://.+?/api/projects/.+?/", "https://REDACTED/",
            TestProxySanitizerType.URL));
        sanitizers.add(new TestProxySanitizer("Content-Type",
            "(^multipart\\/form-data; boundary=[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{2})",
            "multipart\\/form-data; boundary=BOUNDARY", TestProxySanitizerType.HEADER));

        interceptorManager.addSanitizers(sanitizers);

    }

    private void addCustomMatchers() {
        interceptorManager.addMatchers(new CustomMatcher().setExcludedHeaders(Arrays.asList("Cookie", "Set-Cookie",
            "X-Stainless-Arch", "X-Stainless-Lang", "X-Stainless-OS", "X-Stainless-OS-Version",
            "X-Stainless-Package-Version", "X-Stainless-Runtime", "X-Stainless-Runtime-Version")));
    }

    protected Path getPath(String fileName) throws FileNotFoundException, URISyntaxException {
        URL resource = ClientTestBase.class.getClassLoader().getResource(fileName);
        if (resource == null) {
            throw new FileNotFoundException("File not found");
        }

        File file = new File(resource.toURI());
        return file.toPath();
    }

    protected ConnectionsClient getConnectionsClient(HttpClient httpClient,
        AIProjectsServiceVersion aiProjectsServiceVersion) {
        return getClientBuilder(httpClient, aiProjectsServiceVersion).buildConnectionsClient();
    }

    protected ConnectionsAsyncClient getConnectionsAsyncClient(HttpClient httpClient,
        AIProjectsServiceVersion aiProjectsServiceVersion) {
        return getClientBuilder(httpClient, aiProjectsServiceVersion).buildConnectionsAsyncClient();
    }

    protected DatasetsClient getDatasetsClient(HttpClient httpClient,
        AIProjectsServiceVersion aiProjectsServiceVersion) {
        return getClientBuilder(httpClient, aiProjectsServiceVersion).buildDatasetsClient();
    }

    protected DatasetsAsyncClient getDatasetsAsyncClient(HttpClient httpClient,
        AIProjectsServiceVersion aiProjectsServiceVersion) {
        return getClientBuilder(httpClient, aiProjectsServiceVersion).buildDatasetsAsyncClient();
    }

    protected DeploymentsClient getDeploymentsClient(HttpClient httpClient,
        AIProjectsServiceVersion aiProjectsServiceVersion) {
        return getClientBuilder(httpClient, aiProjectsServiceVersion).buildDeploymentsClient();
    }

    protected DeploymentsAsyncClient getDeploymentsAsyncClient(HttpClient httpClient,
        AIProjectsServiceVersion aiProjectsServiceVersion) {
        return getClientBuilder(httpClient, aiProjectsServiceVersion).buildDeploymentsAsyncClient();
    }

    protected EvaluationsClient getEvaluationsClient(HttpClient httpClient,
        AIProjectsServiceVersion aiProjectsServiceVersion) {
        return getClientBuilder(httpClient, aiProjectsServiceVersion).buildEvaluationsClient();
    }

    protected EvaluationsAsyncClient getEvaluationsAsyncClient(HttpClient httpClient,
        AIProjectsServiceVersion aiProjectsServiceVersion) {
        return getClientBuilder(httpClient, aiProjectsServiceVersion).buildEvaluationsAsyncClient();
    }

    protected IndexesClient getIndexesClient(HttpClient httpClient, AIProjectsServiceVersion aiProjectsServiceVersion) {
        return getClientBuilder(httpClient, aiProjectsServiceVersion).buildIndexesClient();
    }

    protected IndexesAsyncClient getIndexesAsyncClient(HttpClient httpClient,
        AIProjectsServiceVersion aiProjectsServiceVersion) {
        return getClientBuilder(httpClient, aiProjectsServiceVersion).buildIndexesAsyncClient();
    }

    /**
     * Helper method to verify a Connection has valid properties.
     * @param connection The connection to validate
     * @param expectedName The expected name of the connection, or null if no specific name is expected
     * @param expectedType The expected connection type, or null if no specific type is expected
     * @param shouldBeDefault Whether the connection should be a default connection, or null if not checking this property
     */
    protected void assertValidConnection(Connection connection, String expectedName, ConnectionType expectedType,
        Boolean shouldBeDefault) {
        Assertions.assertNotNull(connection);
        Assertions.assertNotNull(connection.getName());
        Assertions.assertNotNull(connection.getId());
        Assertions.assertNotNull(connection.getType());
        Assertions.assertNotNull(connection.getTarget());
        Assertions.assertNotNull(connection.getCredentials());

        if (expectedName != null) {
            Assertions.assertEquals(expectedName, connection.getName());
        }

        if (expectedType != null) {
            Assertions.assertEquals(expectedType, connection.getType());
        }

        if (shouldBeDefault != null) {
            Assertions.assertEquals(shouldBeDefault, connection.isDefault());
        }
    }

    /**
     * Helper method to validate common properties of a DatasetVersion
     *
     * @param datasetVersion The dataset version to validate
     * @param expectedName The expected name of the dataset
     * @param expectedVersion The expected version string
     */
    protected void assertDatasetVersion(DatasetVersion datasetVersion, String expectedName, String expectedVersion) {
        Assertions.assertNotNull(datasetVersion, "Dataset version should not be null");
        Assertions.assertEquals(expectedName, datasetVersion.getName(), "Dataset name should match expected value");
        Assertions.assertEquals(expectedVersion, datasetVersion.getVersion(),
            "Dataset version should match expected value");
        Assertions.assertNotNull(datasetVersion.getType(), "Dataset type should not be null");
    }

    /**
     * Helper method to validate common properties of a FileDatasetVersion
     *
     * @param fileDatasetVersion The file dataset version to validate
     * @param expectedName The expected name of the dataset
     * @param expectedVersion The expected version string
     * @param expectedDataUri The expected data URI (optional)
     */
    protected void assertFileDatasetVersion(FileDatasetVersion fileDatasetVersion, String expectedName,
        String expectedVersion, String expectedDataUri) {
        assertDatasetVersion(fileDatasetVersion, expectedName, expectedVersion);
        if (expectedDataUri != null) {
            Assertions.assertEquals(expectedDataUri, fileDatasetVersion.getDataUri(),
                "Dataset dataUri should match expected value");
        }
    }

    /**
     * Helper method to verify a Deployment has valid properties.
     * @param deployment The deployment to validate
     * @param expectedName The expected name of the deployment, or null if no specific name is expected
     * @param expectedType The expected deployment type, or null if no specific type is expected
     */
    protected void assertValidDeployment(Deployment deployment, String expectedName, DeploymentType expectedType) {
        Assertions.assertNotNull(deployment);
        Assertions.assertNotNull(deployment.getName());
        Assertions.assertNotNull(deployment.getType());

        if (expectedName != null) {
            Assertions.assertEquals(expectedName, deployment.getName());
        }

        if (expectedType != null) {
            Assertions.assertEquals(expectedType, deployment.getType());
        }
    }

    /**
     * Helper method to verify an Index has valid properties.
     * @param index The index to validate
     * @param expectedName The expected name of the index, or null if no specific name is expected
     * @param expectedVersion The expected version of the index, or null if no specific version is expected
     */
    protected void assertValidIndex(Index index, String expectedName, String expectedVersion) {
        Assertions.assertNotNull(index);
        Assertions.assertNotNull(index.getName());
        Assertions.assertNotNull(index.getVersion());
        Assertions.assertNotNull(index.getType());

        if (expectedName != null) {
            Assertions.assertEquals(expectedName, index.getName());
        }

        if (expectedVersion != null) {
            Assertions.assertEquals(expectedVersion, index.getVersion());
        }
    }
}

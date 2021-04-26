// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Header;
import com.azure.storage.common.policy.RequestRetryPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.MalformedURLException;
import java.security.SecureRandom;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TableServiceClientBuilderTest {
    private String tableName;
    private String connectionString;
    private TablesServiceVersion serviceVersion;

    @BeforeEach
    public void setUp() {
        tableName = "someTable";
        connectionString = TestUtils.getConnectionString(true);
        serviceVersion = TablesServiceVersion.V2019_02_02;
    }

    @Test
    public void buildSyncClientTest() {
        TableServiceClient tableServiceClient = new TableServiceClientBuilder()
            .connectionString(connectionString)
            .serviceVersion(serviceVersion)
            .buildClient();

        assertNotNull(tableServiceClient);
        assertEquals(TableServiceClient.class.getSimpleName(), tableServiceClient.getClass().getSimpleName());
    }

    @Test
    public void buildSyncClientUsingDefaultApiVersionTest() {
        TableServiceClient tableServiceClient = new TableServiceClientBuilder()
            .connectionString(connectionString)
            .buildClient();

        assertNotNull(tableServiceClient);
        assertEquals(TableServiceClient.class.getSimpleName(), tableServiceClient.getClass().getSimpleName());
    }

    @Test
    public void buildAsyncClientTest() {
        TableServiceAsyncClient tableServiceAsyncClient = new TableServiceClientBuilder()
            .connectionString(connectionString)
            .serviceVersion(serviceVersion)
            .buildAsyncClient();

        assertNotNull(tableServiceAsyncClient);
        assertEquals(TableServiceAsyncClient.class.getSimpleName(), tableServiceAsyncClient.getClass().getSimpleName());
    }

    @Test
    public void buildAsyncClientUsingDefaultApiVersionTest() {
        TableServiceAsyncClient tableServiceAsyncClient = new TableServiceClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();

        assertNotNull(tableServiceAsyncClient);
        assertEquals(TableServiceAsyncClient.class.getSimpleName(), tableServiceAsyncClient.getClass().getSimpleName());
    }

    @Test
    public void emptyEndpointThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new TableServiceClientBuilder().endpoint(""));
    }

    @Test
    public void nullCredentialThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new TableServiceClientBuilder().credential((AzureSasCredential) null));
        assertThrows(NullPointerException.class, () -> new TableServiceClientBuilder().credential((TablesSharedKeyCredential) null));
        assertThrows(NullPointerException.class, () -> new TableServiceClientBuilder().credential((TokenCredential) null));
    }

    @Test
    public void serviceClientFreshDateOnRetry() throws MalformedURLException {
        byte[] randomData = new byte[256];
        new SecureRandom().nextBytes(randomData);

        TableServiceAsyncClient tableServiceAsyncClient = new TableServiceClientBuilder()
            .connectionString(connectionString)
            .httpClient(new TestUtils.FreshDateTestClient())
            .buildAsyncClient();

        StepVerifier.create(tableServiceAsyncClient.getHttpPipeline().send(
            TestUtils.request(tableServiceAsyncClient.getServiceUrl())))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void clientOptionsIsPreferredOverLogOptions() {
        TableServiceClient tableServiceClient = new TableServiceClientBuilder()
            .connectionString(connectionString)
            .httpLogOptions(new HttpLogOptions().setApplicationId("anOldApplication"))
            .clientOptions(new ClientOptions().setApplicationId("aNewApplication"))
            .httpClient(httpRequest -> {
                assertTrue(httpRequest.getHeaders().getValue("User-Agent").contains("aNewApplication"));
                return Mono.error(new HttpResponseException(new MockHttpResponse(httpRequest, 400)));
            })
            .buildClient();

        assertThrows(RuntimeException.class, () -> tableServiceClient.createTable(tableName));
    }

    @Test
    public void applicationIdFallsBackToLogOptions() {
        TableServiceClient tableServiceClient = new TableServiceClientBuilder()
            .connectionString(connectionString)
            .httpLogOptions(new HttpLogOptions().setApplicationId("anOldApplication"))
            .httpClient(httpRequest -> {
                assertTrue(httpRequest.getHeaders().getValue("User-Agent").contains("anOldApplication"));
                return Mono.error(new HttpResponseException(new MockHttpResponse(httpRequest, 400)));
            })
            .buildClient();

        assertThrows(RuntimeException.class, () -> tableServiceClient.createTable(tableName));
    }

    @Test
    public void clientOptionHeadersAreAddedLast() {
        TableServiceClient tableServiceClient = new TableServiceClientBuilder()
            .connectionString(connectionString)
            .clientOptions(new ClientOptions()
                .setHeaders(Collections.singletonList(new Header("User-Agent", "custom"))))
            .httpClient(httpRequest -> {
                assertEquals("custom", httpRequest.getHeaders().getValue("User-Agent"));
                return Mono.error(new HttpResponseException(new MockHttpResponse(httpRequest, 400)));
            })
            .buildClient();

        assertThrows(RuntimeException.class, () -> tableServiceClient.createTable(tableName));
    }

    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials
    // and auth would fail because we changed a signed header.
    @Test
    public void addPerCallPolicy() {
        TableServiceAsyncClient tableServiceAsyncClient = new TableServiceClientBuilder()
            .connectionString(connectionString)
            .addPolicy(new TestUtils.PerCallPolicy())
            .addPolicy(new TestUtils.PerRetryPolicy())
            .buildAsyncClient();

        HttpPipeline pipeline = tableServiceAsyncClient.getHttpPipeline();

        int retryPolicyPosition = -1, perCallPolicyPosition = -1, perRetryPolicyPosition = -1;

        for (int i = 0; i < pipeline.getPolicyCount(); i++) {
            if (pipeline.getPolicy(i).getClass() == RequestRetryPolicy.class) {
                retryPolicyPosition = i;
            }

            if (pipeline.getPolicy(i).getClass() == TestUtils.PerCallPolicy.class) {
                perCallPolicyPosition = i;
            }

            if (pipeline.getPolicy(i).getClass() == TestUtils.PerRetryPolicy.class) {
                perRetryPolicyPosition = i;
            }
        }

        assertTrue(perCallPolicyPosition != -1);
        assertTrue(perCallPolicyPosition < retryPolicyPosition);
        assertTrue(retryPolicyPosition < perRetryPolicyPosition);
    }
}

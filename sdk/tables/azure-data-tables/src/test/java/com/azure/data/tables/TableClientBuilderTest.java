// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables;

import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Header;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.MalformedURLException;
import java.security.SecureRandom;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TableClientBuilderTest {
    private String tableName;
    private String connectionString;
    private TableServiceVersion serviceVersion;

    @BeforeEach
    public void setUp() {
        tableName = "someTable";
        connectionString = TestUtils.getConnectionString(true);
        serviceVersion = TableServiceVersion.V2019_02_02;
    }

    @Test
    public void buildSyncClientTest() {
        TableClient tableClient = new TableClientBuilder()
            .connectionString(connectionString)
            .tableName(tableName)
            .serviceVersion(serviceVersion)
            .buildClient();

        assertNotNull(tableClient);
        assertEquals(TableClient.class.getSimpleName(), tableClient.getClass().getSimpleName());
    }

    @Test
    public void buildSyncClientUsingDefaultApiVersionTest() {
        TableClient tableClient = new TableClientBuilder()
            .connectionString(connectionString)
            .tableName(tableName)
            .buildClient();

        assertNotNull(tableClient);
        assertEquals(TableClient.class.getSimpleName(), tableClient.getClass().getSimpleName());
    }

    @Test
    public void buildAsyncClientTest() {
        TableAsyncClient tableAsyncClient = new TableClientBuilder()
            .connectionString(connectionString)
            .tableName(tableName)
            .serviceVersion(serviceVersion)
            .buildAsyncClient();

        assertNotNull(tableAsyncClient);
        assertEquals(TableAsyncClient.class.getSimpleName(), tableAsyncClient.getClass().getSimpleName());
    }

    @Test
    public void buildAsyncClientUsingDefaultApiVersionTest() {
        TableAsyncClient tableAsyncClient = new TableClientBuilder()
            .connectionString(connectionString)
            .tableName(tableName)
            .buildAsyncClient();

        assertNotNull(tableAsyncClient);
        assertEquals(TableAsyncClient.class.getSimpleName(), tableAsyncClient.getClass().getSimpleName());
    }

    @Test
    public void emptyEndpointThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new TableClientBuilder().endpoint(""));
    }

    @Test
    public void nullCredentialThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new TableClientBuilder().credential((AzureSasCredential) null));
        assertThrows(NullPointerException.class, () -> new TableClientBuilder().credential((AzureNamedKeyCredential) null));
    }

    @Test
    public void serviceClientFreshDateOnRetry() throws MalformedURLException {
        byte[] randomData = new byte[256];
        new SecureRandom().nextBytes(randomData);

        TableAsyncClient tableAsyncClient = new TableClientBuilder()
            .connectionString(connectionString)
            .tableName(tableName)
            .httpClient(new TestUtils.FreshDateTestClient())
            .buildAsyncClient();

        StepVerifier.create(tableAsyncClient.getHttpPipeline().send(
            TestUtils.request(tableAsyncClient.getTableEndpoint())))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void clientOptionsIsPreferredOverLogOptions() {
        TableClient tableClient = new TableClientBuilder()
            .connectionString(connectionString)
            .tableName(tableName)
            .httpLogOptions(new HttpLogOptions().setApplicationId("anOldApplication"))
            .clientOptions(new ClientOptions().setApplicationId("aNewApplication"))
            .httpClient(httpRequest -> {
                assertTrue(httpRequest.getHeaders().getValue("User-Agent").contains("aNewApplication"));
                return Mono.error(new HttpResponseException(new MockHttpResponse(httpRequest, 400)));
            })
            .buildClient();

        assertThrows(RuntimeException.class, tableClient::createTable);
    }

    @Test
    public void applicationIdFallsBackToLogOptions() {
        TableClient tableClient = new TableClientBuilder()
            .connectionString(connectionString)
            .tableName(tableName)
            .httpLogOptions(new HttpLogOptions().setApplicationId("anOldApplication"))
            .httpClient(httpRequest -> {
                assertTrue(httpRequest.getHeaders().getValue("User-Agent").contains("anOldApplication"));
                return Mono.error(new HttpResponseException(new MockHttpResponse(httpRequest, 400)));
            })
            .buildClient();

        assertThrows(RuntimeException.class, tableClient::createTable);
    }

    @Test
    public void clientOptionHeadersAreAddedLast() {
        TableClient tableClient = new TableClientBuilder()
            .connectionString(connectionString)
            .tableName(tableName)
            .clientOptions(new ClientOptions()
                .setHeaders(Collections.singletonList(new Header("User-Agent", "custom"))))
            .httpClient(httpRequest -> {
                assertEquals("custom", httpRequest.getHeaders().getValue("User-Agent"));
                return Mono.error(new HttpResponseException(new MockHttpResponse(httpRequest, 400)));
            })
            .buildClient();

        assertThrows(RuntimeException.class, tableClient::createTable);
    }

    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials
    // and auth would fail because we changed a signed header.
    @Test
    public void addPerCallPolicy() {
        TableAsyncClient tableAsyncClient = new TableClientBuilder()
            .connectionString(connectionString)
            .tableName(tableName)
            .addPolicy(new TestUtils.PerCallPolicy())
            .addPolicy(new TestUtils.PerRetryPolicy())
            .buildAsyncClient();

        HttpPipeline pipeline = tableAsyncClient.getHttpPipeline();

        int retryPolicyPosition = -1, perCallPolicyPosition = -1, perRetryPolicyPosition = -1;

        for (int i = 0; i < pipeline.getPolicyCount(); i++) {
            if (pipeline.getPolicy(i).getClass() == RetryPolicy.class) {
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

    @Test
    public void singleFormOfAuthenticationPresent() {
        assertDoesNotThrow(() -> new TableClientBuilder()
            .sasToken("sasToken")
            .endpoint("https://myAccount.table.core.windows.net")
            .tableName("myTable")
            .buildAsyncClient());

        assertDoesNotThrow(() -> new TableClientBuilder()
            .credential(new AzureSasCredential("sasToken"))
            .endpoint("https://myAccount.table.core.windows.net")
            .tableName("myTable")
            .buildAsyncClient());

        assertDoesNotThrow(() -> new TableClientBuilder()
            .credential(new AzureNamedKeyCredential("name", "key"))
            .endpoint("https://myAccount.table.core.windows.net")
            .tableName("myTable")
            .buildAsyncClient());

        // Should internally create an AzureNamedKeyCredential and not throw when building a client.
        assertDoesNotThrow(() -> new TableClientBuilder()
            .connectionString("DefaultEndpointsProtocol=https;AccountName=myAccount;AccountKey=myKey;EndpointSuffix=core.windows.net")
            .endpoint("https://myAccount.table.core.windows.net")
            .tableName("myTable")
            .buildAsyncClient());

        // Should internally create an AzureSasCredential and not throw when building a client.
        assertDoesNotThrow(() -> new TableClientBuilder()
            .connectionString("TableEndpoint=https://myAccount.table.core.windows.net;SharedAccessSignature=sv=2020-02-10&ss=t&srt=o&sp=rwdlacu&se=2021-06-04T04:45:57Z&st=2021-06-03T20:45:57Z&spr=https&sig=someSignature")
            .endpoint("https://myAccount.table.core.windows.net")
            .tableName("myTable")
            .buildAsyncClient());
    }

    @Test
    public void multipleFormsOfAuthenticationPresent() {
        assertThrows(IllegalStateException.class, () -> new TableClientBuilder()
            .sasToken("sasToken")
            .credential(new AzureNamedKeyCredential("name", "key"))
            .tableName("myTable")
            .endpoint("https://myAccount.table.core.windows.net")
            .buildAsyncClient());

        assertThrows(IllegalStateException.class, () -> new TableClientBuilder()
            .sasToken("sasToken")
            .credential(new AzureSasCredential("sasToken"))
            .tableName("myTable")
            .endpoint("https://myAccount.table.core.windows.net")
            .buildAsyncClient());

        assertThrows(IllegalStateException.class, () -> new TableClientBuilder()
            .credential(new AzureNamedKeyCredential("name", "key"))
            .credential(new AzureSasCredential("sasToken"))
            .tableName("myTable")
            .endpoint("https://myAccount.table.core.windows.net")
            .buildAsyncClient());

        assertThrows(IllegalStateException.class, () -> new TableClientBuilder()
            .sasToken("sasToken")
            .credential(new AzureNamedKeyCredential("name", "key"))
            .credential(new AzureSasCredential("sasToken"))
            .tableName("myTable")
            .endpoint("https://myAccount.table.core.windows.net")
            .buildAsyncClient());
    }

    @Test
    public void buildWithSameSasTokenInConnectionStringDoesNotThrow() {
        assertDoesNotThrow(() -> new TableClientBuilder()
            .sasToken("sv=2020-02-10&ss=t&srt=o&sp=rwdlacu&se=2021-06-04T04:45:57Z&st=2021-06-03T20:45:57Z&spr=https&sig=someSignature")
            .connectionString("TableEndpoint=https://myAccount.table.core.windows.net/;SharedAccessSignature=sv=2020-02-10&ss=t&srt=o&sp=rwdlacu&se=2021-06-04T04:45:57Z&st=2021-06-03T20:45:57Z&spr=https&sig=someSignature")
            .tableName("myTable")
            .buildAsyncClient());
    }

    @Test
    public void buildWithDifferentSasTokenInConnectionStringThrows() {
        assertThrows(IllegalStateException.class, () -> new TableClientBuilder()
            .sasToken("sv=2020-02-10&ss=t&srt=o&sp=rwd&se=2021-06-04T04:45:57Z&st=2021-06-03T20:45:57Z&spr=https&sig=someSignature")
            .connectionString("TableEndpoint=https://myAccount.table.core.windows.net/;SharedAccessSignature=sv=2020-02-10&ss=t&srt=o&sp=rwdlacu&se=2021-06-04T04:45:57Z&st=2021-06-03T20:45:57Z&spr=https&sig=anotherSignature")
            .tableName("myTable")
            .buildAsyncClient());
    }

    @Test
    public void buildWithSameEndpointInConnectionStringDoesNotThrow() {
        assertDoesNotThrow(() -> new TableClientBuilder()
            .endpoint("https://myAccount.table.core.windows.net/")
            .connectionString("TableEndpoint=https://myAccount.table.core.windows.net/;SharedAccessSignature=sv=2020-02-10&ss=t&srt=o&sp=rwdlacu&se=2021-06-04T04:45:57Z&st=2021-06-03T20:45:57Z&spr=https&sig=someSignature")
            .tableName("myTable")
            .buildAsyncClient());
    }

    @Test
    public void buildWithWithTrailingSlashInEndpointOrConnectionStringDoesNotThrow() {
        assertDoesNotThrow(() -> new TableClientBuilder()
            .endpoint("https://myAccount.table.core.windows.net")
            .connectionString("TableEndpoint=https://myAccount.table.core.windows.net/;SharedAccessSignature=sv=2020-02-10&ss=t&srt=o&sp=rwdlacu&se=2021-06-04T04:45:57Z&st=2021-06-03T20:45:57Z&spr=https&sig=someSignature")
            .tableName("myTable")
            .buildAsyncClient());

        assertDoesNotThrow(() -> new TableClientBuilder()
            .endpoint("https://myAccount.table.core.windows.net/")
            .connectionString("TableEndpoint=https://myAccount.table.core.windows.net;SharedAccessSignature=sv=2020-02-10&ss=t&srt=o&sp=rwdlacu&se=2021-06-04T04:45:57Z&st=2021-06-03T20:45:57Z&spr=https&sig=someSignature")
            .tableName("myTable")
            .buildAsyncClient());
    }


    @Test
    public void buildWithDifferentEndpointInConnectionStringThrows() {
        assertThrows(IllegalStateException.class, () -> new TableClientBuilder()
            .endpoint("https://myOtherAccount.table.core.windows.net/")
            .connectionString("TableEndpoint=https://myAccount.table.core.windows.net/;SharedAccessSignature=sv=2020-02-10&ss=t&srt=o&sp=rwdlacu&se=2021-06-04T04:45:57Z&st=2021-06-03T20:45:57Z&spr=https&sig=someSignature")
            .tableName("myTable")
            .buildAsyncClient());

        assertThrows(IllegalStateException.class, () -> new TableClientBuilder()
            .endpoint("https://myAccount.table.core.windows.net/")
            .connectionString("DefaultEndpointsProtocol=https;AccountName=myOtherAccount;AccountKey=myKey;EndpointSuffix=core.windows.net")
            .tableName("myTable")
            .buildAsyncClient());
    }
}

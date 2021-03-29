// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.iot.deviceupdate;

import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.*;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.iot.deviceupdate.models.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class UpdatesClientTests extends TestBase {
    private static final String FILE_NAME = "setup.exe";
    private static final String DEFAULT_SCOPE = "6ee392c4-d339-4083-b04d-6b7947c6cf78/.default";

    private UpdatesAsyncClient createClient() {
        TokenCredential credentials;
        HttpClient httpClient;
        HttpPipelinePolicy recordingPolicy = null;
        HttpPipeline httpPipeline;

        HttpHeaders headers = new HttpHeaders().put("Accept", ContentType.APPLICATION_JSON);
        AddHeadersPolicy addHeadersPolicy = new AddHeadersPolicy(headers);

        if (getTestMode() != TestMode.PLAYBACK) {
            // Record & Live
            credentials = new ClientSecretCredentialBuilder()
                .tenantId(TestData.TENANT_ID)
                .clientId(TestData.CLIENT_ID)
                .clientSecret(System.getenv("AZURE_CLIENT_SECRET"))
                .build();
            httpClient = HttpClient.createDefault();
            if (getTestMode() == TestMode.RECORD) {
                recordingPolicy = interceptorManager.getRecordPolicy();
            }
            BearerTokenAuthenticationPolicy bearerTokenAuthenticationPolicy =
                new BearerTokenAuthenticationPolicy(credentials, DEFAULT_SCOPE);
            httpPipeline = new HttpPipelineBuilder()
                .httpClient(httpClient)
                .policies(bearerTokenAuthenticationPolicy, addHeadersPolicy, recordingPolicy)
                .build();
        }
        else {
            // Playback
            httpClient = interceptorManager.getPlaybackClient();
            httpPipeline = new HttpPipelineBuilder()
                .httpClient(httpClient)
                .policies(addHeadersPolicy)
                .build();
        }

        return new DeviceUpdateClientBuilder()
            .accountEndpoint(TestData.ACCOUNT_ENDPOINT)
            .instanceId(TestData.INSTANCE_ID)
            .pipeline(httpPipeline)
            .buildUpdatesAsyncClient();
    }

    @Test
    public void testImportUpdate() {
        UpdatesAsyncClient client = createClient();

        Map<String, String> hashes = new HashMap<String, String>();
        hashes.put("SHA256", "Ak1xigPLmur511bYfCvzeCwF6r/QxiBKeEDHOvHPzr4=");

        List<FileImportMetadata> files = new ArrayList<FileImportMetadata>();
        files.add(
            new FileImportMetadata()
                .setFilename(FILE_NAME)
                .setUrl("https://adutest.blob.core.windows.net/test/zVknnlx1tyYSMHY28LZVzk?sv=2019-02-02&sr=b&sig=QtS6bAOcHon18wLwIt9uvHIM%2B4M27EoVPNP4RWpMjyw%3D&se=2020-05-08T20%3A52%3A51Z&sp=r"));

        ImportUpdateInput update = new ImportUpdateInput()
            .setImportManifest(
                new ImportManifestMetadata()
                    .setUrl("https://adutest.blob.core.windows.net/test/Ak1xigPLmur511bYfCvzeC?sv=2019-02-02&sr=b&sig=L9RZxCUwduStz0m1cj4YnXt6OJCvWSe9SPseum3cclE%3D&se=2020-05-08T20%3A52%3A51Z&sp=r")
                    .setSizeInBytes(453)
                    .setHashes(hashes)
            )
            .setFiles(files);

        UpdatesImportUpdateResponse response = client.importUpdateWithResponse(update).block();
        assertNotNull(response);
        assertEquals(202, response.getStatusCode());
        assertNotNull(response.getHeaders());
        assertNotNull(response.getHeaders().getValue("Location"));
        assertNotNull(response.getHeaders().getValue("Operation-Location"));
        assertEquals(response.getHeaders().getValue("Location"), response.getHeaders().getValue("Operation-Location"));
    }

    @Test
    public void testGetUpdate() {
        UpdatesAsyncClient client = createClient();
        Update update = client.getUpdate(TestData.PROVIDER, TestData.NAME, TestData.VERSION, null)
            .block();

        assertNotNull(update);
        assertEquals(TestData.PROVIDER, update.getUpdateId().getProvider());
        assertEquals(TestData.NAME, update.getUpdateId().getName());
        assertEquals(TestData.VERSION, update.getUpdateId().getVersion());
    }

    @Test
    public void testGetUpdateNotFound() {
        UpdatesAsyncClient client = createClient();
        try {
            client.getUpdate("foo", "bar", "0.0.0.1", null).block();
            fail("Expected NotFound response");
        } catch (HttpResponseException e) {
            assertEquals(404, e.getResponse().getStatusCode());
        }
    }

    @Test
    public void testGetProviders() {
        UpdatesAsyncClient client = createClient();
        PagedFlux<String> response = client.getProviders();

        assertNotNull(response);
        List<String> providers = new ArrayList<>();
        response.byPage().map(page -> providers.addAll(page.getValue())).blockLast();
        assertTrue(providers.size() > 0);
    }

    @Test
    public void testGetNames() {
        UpdatesAsyncClient client = createClient();
        PagedFlux<String> response = client.getNames(TestData.PROVIDER);
        assertNotNull(response);

        List<String> names = new ArrayList<>();
        response.byPage().map(page -> names.addAll(page.getValue())).blockLast();
        assertTrue(names.size() > 0);
    }

    @Test
    public void testGetNamesNotFound() {
        UpdatesAsyncClient client = createClient();
        try {
            PagedFlux<String> response = client.getNames("foo");
            List<String> names = new ArrayList<>();
            response.byPage().map(page -> names.addAll(page.getValue())).blockLast();
            fail("Expected NotFound response");
        } catch (HttpResponseException e) {
            assertEquals(404, e.getResponse().getStatusCode());
        }
    }

    @Test
    public void testGetVersions() {
        UpdatesAsyncClient client = createClient();
        PagedFlux<String> response = client.getVersions(TestData.PROVIDER, TestData.NAME);

        assertNotNull(response);
        List<String> versions = new ArrayList<>();
        response.byPage().map(page -> versions.addAll(page.getValue())).blockLast();
        assertTrue(versions.size() > 0);
    }

    @Test
    public void testGetVersionsNotFound() {
        UpdatesAsyncClient client = createClient();
        try {
            PagedFlux<String> response = client.getVersions("foo", "bar");
            List<String> versions = new ArrayList<>();
            response.byPage().map(page -> versions.addAll(page.getValue())).blockLast();
            fail("Expected NotFound response");
        } catch (HttpResponseException e) {
            assertEquals(404, e.getResponse().getStatusCode());
        }
    }

    @Test
    public void testGetFiles() {
        UpdatesAsyncClient client = createClient();
        PagedFlux<String> response = client.getFiles(TestData.PROVIDER, TestData.NAME, TestData.VERSION);

        assertNotNull(response);
        List<String> files = new ArrayList<>();
        response.byPage().map(page -> files.addAll(page.getValue())).blockLast();
        assertTrue(files.size() > 0);
    }

    @Test
    public void testGetFilesNotFound() {
        UpdatesAsyncClient client = createClient();
        try {
            PagedFlux<String> response = client.getFiles("foo", "bar", "0.0.0.1");
            List<String> files = new ArrayList<>();
            response.byPage().map(page -> files.addAll(page.getValue())).blockLast();
            fail("Expected NotFound response");
        } catch (HttpResponseException e) {
            assertEquals(404, e.getResponse().getStatusCode());
        }
    }

    @Test
    public void testGetFile() {
        UpdatesAsyncClient client = createClient();
        File file = client.getFile(TestData.PROVIDER, TestData.NAME, TestData.VERSION, "00000", null)
            .block();
        assertNotNull(file);
        assertEquals("00000", file.getFileId());
    }

    @Test
    public void testGetFileNotFound() {
        UpdatesAsyncClient client = createClient();
        try {
            client.getFile(TestData.PROVIDER, TestData.NAME, TestData.VERSION, "foo", null)
                .block();
            fail("Expected NotFound response");
        } catch (HttpResponseException e) {
            assertEquals(404, e.getResponse().getStatusCode());
        }
    }

    @Test
    public void testGetOperation() {
        UpdatesAsyncClient client = createClient();
        Operation operation = client.getOperation(TestData.OPERATION_ID, null)
            .block();
        assertNotNull(operation);
        assertEquals(OperationStatus.SUCCEEDED, operation.getStatus());
    }

    @Test
    public void testGetOperationNotFound() {
        UpdatesAsyncClient client = createClient();
        try {
            client.getOperation("fake", null)
                .block();
            fail("Expected NotFound response");
        } catch (HttpResponseException e) {
            assertEquals(404, e.getResponse().getStatusCode());
        }
    }

    @Test
    public void testGetOperations() {
        UpdatesAsyncClient client = createClient();
        PagedFlux<Operation> response = client.getOperations(null, 1);
        assertNotNull(response);
        List<Operation> operations = new ArrayList<>();
        response.byPage().map(page -> operations.addAll(page.getValue())).blockLast();
        assertTrue(operations.size() > 0);
    }
}

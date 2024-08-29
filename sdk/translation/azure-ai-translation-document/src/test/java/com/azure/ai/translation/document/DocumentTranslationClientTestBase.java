// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.document;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.util.Configuration;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class DocumentTranslationClientTestBase extends TestProxyTestBase {

    private static final String[] DISABLE_SANITIZER_LIST = {"AZSDK3430", "AZSDK2030"};
    private boolean sanitizersRemoved = false;

    @Override
    public void beforeTest() {
        super.beforeTest();
    }

    DocumentTranslationClient getDocumentTranslationClient() {
        return getDTClient(getEndpoint(), getKey());
    }

    DocumentTranslationClient getDTClient(String endpoint, String key) {
        DocumentTranslationClientBuilder documentTranslationClientbuilder = new DocumentTranslationClientBuilder()
            .endpoint(endpoint)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));

        if (interceptorManager.isPlaybackMode()) {
            documentTranslationClientbuilder.httpClient(interceptorManager.getPlaybackClient());
        } else if (interceptorManager.isRecordMode()) {
            documentTranslationClientbuilder.addPolicy(interceptorManager.getRecordPolicy());
        }

        if (!interceptorManager.isLiveMode() && !sanitizersRemoved) {
            interceptorManager.removeSanitizers(DISABLE_SANITIZER_LIST);
            sanitizersRemoved = true;
        }

        documentTranslationClientbuilder.credential(new AzureKeyCredential(key));

        return documentTranslationClientbuilder.buildClient();
    }

    SingleDocumentTranslationClient getSingleDocumentTranslationClient() {
        return getSDTClient(getEndpoint(), getKey());
    }

    private SingleDocumentTranslationClient getSDTClient(String endpoint, String key) {
        SingleDocumentTranslationClientBuilder singleDocumentTranslationClientbuilder = new SingleDocumentTranslationClientBuilder()
            .endpoint(endpoint)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));

        if (interceptorManager.isPlaybackMode()) {
            singleDocumentTranslationClientbuilder.httpClient(interceptorManager.getPlaybackClient());
        } else if (interceptorManager.isRecordMode()) {
            singleDocumentTranslationClientbuilder.addPolicy(interceptorManager.getRecordPolicy());
        }

        if (!interceptorManager.isLiveMode() && !sanitizersRemoved) {
            interceptorManager.removeSanitizers(DISABLE_SANITIZER_LIST);
            sanitizersRemoved = true;
        }

        singleDocumentTranslationClientbuilder.credential(new AzureKeyCredential(key));
        return singleDocumentTranslationClientbuilder.buildClient();
    }

    private String getEndpoint() {
        // NOT REAL ACCOUNT DETAILS
        String playbackEndpoint = "https://fakeendpoint.cognitiveservices.azure.com";
        return interceptorManager.isPlaybackMode()
            ? playbackEndpoint
            : Configuration.getGlobalConfiguration().get("DOCUMENT_TRANSLATION_ENDPOINT");
    }

    private String getKey() {
        String playbackApiKey = "Sanitized";
        return interceptorManager.isPlaybackMode()
            ? playbackApiKey
            : Configuration.getGlobalConfiguration().get("DOCUMENT_TRANSLATION_API_KEY");
    }

    String getStorageName() {
        String playbackStorageName = "Sanitized";
        return interceptorManager.isPlaybackMode()
            ? playbackStorageName
            : Configuration.getGlobalConfiguration().get("DOCUMENT_TRANSLATION_STORAGE_NAME");
    }

    BlobContainerClient getBlobContainerClient(String containerName) {
        String endpoint = String.format("https://%s.blob.core.windows.net", getStorageName());
        BlobContainerClientBuilder blobContainerClientBuilder = new BlobContainerClientBuilder()
            .endpoint(endpoint)
            .containerName(containerName)
            .credential(getIdentityTestCredential(interceptorManager));

        if (interceptorManager.isPlaybackMode()) {
            blobContainerClientBuilder.httpClient(interceptorManager.getPlaybackClient());
        } else if (interceptorManager.isRecordMode()) {
            blobContainerClientBuilder.addPolicy(interceptorManager.getRecordPolicy());
        }

        if (!interceptorManager.isLiveMode() && !sanitizersRemoved) {
            interceptorManager.removeSanitizers(DISABLE_SANITIZER_LIST);
            sanitizersRemoved = true;
        }

        return blobContainerClientBuilder.buildClient();
    }

    protected static final List<TestDocument> ONE_TEST_DOCUMENTS = new ArrayList<TestDocument>() {
        {
            add(new TestDocument("Document1.txt", "First english test document"));
        }
    };

    protected static final List<TestDocument> TWO_TEST_DOCUMENTS = new ArrayList<TestDocument>() {
        {
            add(new TestDocument("Document1.txt", "First english test file"));
            add(new TestDocument("File2.txt", "Second english test file"));
        }
    };

    public List<TestDocument> createDummyTestDocuments(int count) {
        List<TestDocument> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String fileName = "File_" + i + ".txt";
            String text = "some random text";
            result.add(new TestDocument(fileName, text));
        }
        return result;
    }

    String createSourceContainer(List<TestDocument> documents) {
        String containerName = testResourceNamer.randomName("source", 10);
        BlobContainerClient blobContainerClient = createContainer(containerName, documents);        
        String containerUrl = blobContainerClient.getBlobContainerUrl();
        return containerUrl;
    }

    String createTargetContainer(List<TestDocument> documents) {
        String containerName = testResourceNamer.randomName("target", 10);
        BlobContainerClient blobContainerClient = createContainer(containerName, documents);        
        String containerUrl = blobContainerClient.getBlobContainerUrl();
        return containerUrl;
    }

    Map<String, String> createTargetContainerWithClient(List<TestDocument> documents) {

        String containerName = testResourceNamer.randomName("target", 10);
        BlobContainerClient blobContainerClient = createContainer(containerName, documents);        
        String containerUrl = blobContainerClient.getBlobContainerUrl();

        Map<String, String> containerValues = new HashMap<>();
        containerValues.put("containerUrl", containerUrl);
        containerValues.put("containerName", containerName);

        return containerValues;
    }

    String createGlossary(TestDocument document) {
        String containerName = testResourceNamer.randomName("glossary", 10);
        List<TestDocument> documents = new ArrayList<>();
        documents.add(document);
        BlobContainerClient blobContainerClient = createContainer(containerName, documents);
        String containerUrl = blobContainerClient.getBlobContainerUrl();
        String sasUri = containerUrl + "/" + document.getName();
        return sasUri;
    }

    BlobContainerClient createContainer(String containerName, List<TestDocument> documents) {
        BlobContainerClient containerClient = getBlobContainerClient(containerName);

        if (!containerClient.exists()) {
            containerClient.create();
        }

        if (documents != null && !documents.isEmpty()) {
            uploadDocuments(containerClient, documents);
        }

        return containerClient;
    }

    private void uploadDocuments(BlobContainerClient blobContainerClient, List<TestDocument> documents) {
        for (TestDocument document : documents) {
            InputStream stream = new ByteArrayInputStream(document.getContent().getBytes());
            BlobClient blobClient = blobContainerClient.getBlobClient(document.getName());
            blobClient.upload(stream);
        }
    }

    String downloadDocumentStream(String targetContainerName, String blobName) {
        String endpoint = String.format("https://%s.blob.core.windows.net", getStorageName());
        BlobClientBuilder blobClientBuilder = new BlobClientBuilder()
            .endpoint(endpoint)
            .containerName(targetContainerName)
            .credential(getIdentityTestCredential(interceptorManager))
            .blobName(blobName);

        if (interceptorManager.isPlaybackMode()) {
            blobClientBuilder.httpClient(interceptorManager.getPlaybackClient());
        } else if (interceptorManager.isRecordMode()) {
            blobClientBuilder.addPolicy(interceptorManager.getRecordPolicy());
        }
        if (!interceptorManager.isLiveMode() && !sanitizersRemoved) {
            interceptorManager.removeSanitizers(DISABLE_SANITIZER_LIST);
            sanitizersRemoved = true;
        }

        BlobClient blobClient = blobClientBuilder.buildClient();

        InputStream blobIS = blobClient.openInputStream();
        try {
            String content = readInputStreamToString(blobIS);
            return content;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String readInputStreamToString(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(System.lineSeparator()); // Add line separator if needed
            }
        }
        return stringBuilder.toString();
    }

    public TokenCredential getIdentityTestCredential(InterceptorManager interceptorManager) {
        if (interceptorManager.isPlaybackMode()) {
            return new MockTokenCredential();
        }

        Configuration config = Configuration.getGlobalConfiguration();

        ChainedTokenCredentialBuilder builder = new ChainedTokenCredentialBuilder()
                .addLast(new EnvironmentCredentialBuilder().build())
                .addLast(new AzureCliCredentialBuilder().build())
                .addLast(new AzureDeveloperCliCredentialBuilder().build());


        String serviceConnectionId = config.get("AZURESUBSCRIPTION_SERVICE_CONNECTION_ID");
        String clientId = config.get("AZURESUBSCRIPTION_CLIENT_ID");
        String tenantId = config.get("AZURESUBSCRIPTION_TENANT_ID");
        String systemAccessToken = config.get("SYSTEM_ACCESSTOKEN");

        if (!CoreUtils.isNullOrEmpty(serviceConnectionId)
                && !CoreUtils.isNullOrEmpty(clientId)
                && !CoreUtils.isNullOrEmpty(tenantId)
                && !CoreUtils.isNullOrEmpty(systemAccessToken)) {

            AzurePipelinesCredential azurePipelinesCredential = new AzurePipelinesCredentialBuilder()
                    .systemAccessToken(systemAccessToken)
                    .clientId(clientId)
                    .tenantId(tenantId)
                    .serviceConnectionId(serviceConnectionId)
                    .build();

            builder.addLast(trc -> azurePipelinesCredential.getToken(trc).subscribeOn(Schedulers.boundedElastic()));
        }

        builder.addLast(new AzurePowerShellCredentialBuilder().build());


        return builder.build();
    }
}

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
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.OffsetDateTime;
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

    private String getConnectionString() {
        return interceptorManager.isPlaybackMode()
            ? "DefaultEndpointsProtocol=https;AccountName=dummyAccount;AccountKey=xyzDummy;EndpointSuffix=core.windows.net"
            : Configuration.getGlobalConfiguration().get("DOCUMENT_TRANSLATION_CONNECTION_STRING");
    }

    BlobContainerClient getBlobContainerClient(String containerName) {
        BlobContainerClientBuilder blobContainerClientBuilder = new BlobContainerClientBuilder()
            .containerName(containerName)
            .connectionString(getConnectionString());

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
        OffsetDateTime expiresOn = OffsetDateTime.now().plusHours(1);

        BlobContainerSasPermission containerSasPermission = new BlobContainerSasPermission()
            .setReadPermission(true)
            .setListPermission(true);

        BlobServiceSasSignatureValues serviceSasValues
            = new BlobServiceSasSignatureValues(expiresOn, containerSasPermission);

        String sasToken = blobContainerClient.generateSas(serviceSasValues);
        String containerUrl = blobContainerClient.getBlobContainerUrl();
        String sasUri = containerUrl + "?" + sasToken;
        return sasUri;
    }

    String createTargetContainer(List<TestDocument> documents) {
        String containerName = testResourceNamer.randomName("target", 10);
        BlobContainerClient blobContainerClient = createContainer(containerName, documents);
        OffsetDateTime expiresOn = OffsetDateTime.now().plusHours(1);

        BlobContainerSasPermission containerSasPermission = new BlobContainerSasPermission()
            .setWritePermission(true)
            .setListPermission(true);

        BlobServiceSasSignatureValues serviceSasValues
            = new BlobServiceSasSignatureValues(expiresOn, containerSasPermission);

        String sasToken = blobContainerClient.generateSas(serviceSasValues);
        String containerUrl = blobContainerClient.getBlobContainerUrl();
        String sasUri = containerUrl + "?" + sasToken;
        return sasUri;
    }

    Map<String, String> createTargetContainerWithClient(List<TestDocument> documents) {

        String containerName = testResourceNamer.randomName("target", 10);
        BlobContainerClient blobContainerClient = createContainer(containerName, documents);
        OffsetDateTime expiresOn = OffsetDateTime.now().plusHours(1);

        BlobContainerSasPermission containerSasPermission = new BlobContainerSasPermission()
            .setWritePermission(true)
            .setListPermission(true);

        BlobServiceSasSignatureValues serviceSasValues
            = new BlobServiceSasSignatureValues(expiresOn, containerSasPermission);

        String sasToken = blobContainerClient.generateSas(serviceSasValues);
        String containerUrl = blobContainerClient.getBlobContainerUrl();
        String sasUri = containerUrl + "?" + sasToken;

        Map<String, String> containerValues = new HashMap<>();
        containerValues.put("sasUri", sasUri);
        containerValues.put("containerName", containerName);

        return containerValues;
    }

    String createGlossary(TestDocument document) {
        String containerName = testResourceNamer.randomName("glossary", 10);
        List<TestDocument> documents = new ArrayList<>();
        documents.add(document);
        BlobContainerClient blobContainerClient = createContainer(containerName, documents);
        OffsetDateTime expiresOn = OffsetDateTime.now().plusHours(1);

        BlobContainerSasPermission containerSasPermission = new BlobContainerSasPermission()
            .setReadPermission(true)
            .setListPermission(true);

        BlobServiceSasSignatureValues serviceSasValues
            = new BlobServiceSasSignatureValues(expiresOn, containerSasPermission);

        String sasToken = blobContainerClient.generateSas(serviceSasValues);
        String containerUrl = blobContainerClient.getBlobContainerUrl();
        String sasUri = containerUrl + "/" + document.getName() + "?" + sasToken;
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
        BlobClientBuilder blobClientBuilder = new BlobClientBuilder()
            .containerName(targetContainerName)
            .connectionString(getConnectionString())
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
}

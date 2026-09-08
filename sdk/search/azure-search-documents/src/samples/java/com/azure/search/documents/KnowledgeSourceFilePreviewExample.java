// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.BinaryData;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.AzureOpenAIModelName;
import com.azure.search.documents.indexes.models.AzureOpenAIVectorizerParameters;
import com.azure.search.documents.indexes.models.FileKnowledgeSource;
import com.azure.search.documents.indexes.models.FileKnowledgeSourceExtractionMode;
import com.azure.search.documents.indexes.models.FileKnowledgeSourceParameters;
import com.azure.search.documents.indexes.models.FileUploadMetadata;
import com.azure.search.documents.indexes.models.KnowledgeSourceContentExtractionMode;
import com.azure.search.documents.indexes.models.KnowledgeSourceFile;
import com.azure.search.documents.indexes.models.ListingSearchType;
import com.azure.search.documents.indexes.models.SearchIndexerDataUserAssignedIdentity;
import com.azure.search.documents.indexes.models.UpdateKnowledgeSourceFileRequest;
import com.azure.search.documents.indexes.models.UploadKnowledgeSourceFileMultipartRequest;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceAzureOpenAIVectorizer;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceIngestionParameters;
import com.azure.search.documents.models.ContentFileDetails;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Demonstrates the lifecycle of files in a File knowledge source.
 *
 * <p>Set {@code SEARCH_ENDPOINT}, {@code SEARCH_API_KEY}, {@code SEARCH_OPENAI_ENDPOINT},
 * {@code SEARCH_OPENAI_API_KEY}, {@code SEARCH_OPENAI_EMBEDDING_DEPLOYMENT_NAME}, and
 * {@code SEARCH_OPENAI_EMBEDDING_MODEL_NAME} before running this sample. If the Search service uses a user-assigned
 * managed identity for the embedding deployment, also set {@code SEARCH_USER_ASSIGNED_IDENTITY}.</p>
 */
public class KnowledgeSourceFilePreviewExample {
    public static void main(String[] args) {
        String endpoint = System.getenv("SEARCH_ENDPOINT");
        String apiKey = System.getenv("SEARCH_API_KEY");
        String openAiEndpoint = System.getenv("SEARCH_OPENAI_ENDPOINT");
        String openAiApiKey = System.getenv("SEARCH_OPENAI_API_KEY");
        String embeddingDeployment = System.getenv("SEARCH_OPENAI_EMBEDDING_DEPLOYMENT_NAME");
        String embeddingModel = System.getenv("SEARCH_OPENAI_EMBEDDING_MODEL_NAME");
        String userAssignedIdentity = System.getenv("SEARCH_USER_ASSIGNED_IDENTITY");

        SearchIndexClient searchIndexClient = new SearchIndexClientBuilder().endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey))
            .buildClient();

        String knowledgeSourceName
            = "file-ks-sample-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        List<String> uploadedFileIds = new ArrayList<>();
        boolean knowledgeSourceCreated = false;

        try {
            AzureOpenAIVectorizerParameters vectorizerParameters
                = new AzureOpenAIVectorizerParameters().setResourceUrl(openAiEndpoint)
                    .setDeploymentName(embeddingDeployment)
                    .setModelName(AzureOpenAIModelName.fromString(embeddingModel));
            if (userAssignedIdentity == null || userAssignedIdentity.isEmpty()) {
                vectorizerParameters.setApiKey(openAiApiKey);
            } else {
                vectorizerParameters
                    .setAuthIdentity(new SearchIndexerDataUserAssignedIdentity(userAssignedIdentity));
            }
            KnowledgeSourceIngestionParameters ingestionParameters = new KnowledgeSourceIngestionParameters()
                .setContentExtractionMode(KnowledgeSourceContentExtractionMode.MINIMAL)
                .setEmbeddingModel(new KnowledgeSourceAzureOpenAIVectorizer()
                    .setAzureOpenAIParameters(vectorizerParameters));
            FileKnowledgeSource knowledgeSource = new FileKnowledgeSource(knowledgeSourceName,
                new FileKnowledgeSourceParameters().setIngestionParameters(ingestionParameters));
            searchIndexClient.createKnowledgeSource(knowledgeSource);
            knowledgeSourceCreated = true;

            Map<String, String> metadata = new LinkedHashMap<>();
            metadata.put("team", "search");
            metadata.put("release", "2026-08-01-preview");

            KnowledgeSourceFile firstFile = searchIndexClient.uploadKnowledgeSourceFileMultipart(knowledgeSourceName,
                createUploadRequest("release-notes/august/features.md",
                    "# August features\n\nFile knowledge source lifecycle improvements.", metadata));
            verifyUploadedFile(firstFile, "release-notes/august/features.md", "release-notes/august/", metadata);
            uploadedFileIds.add(firstFile.getFileId());

            KnowledgeSourceFile secondFile = searchIndexClient.uploadKnowledgeSourceFileMultipart(knowledgeSourceName,
                createUploadRequest("release-notes/august/migration.md",
                    "# Migration\n\nSteps for adopting the August preview.", Collections.singletonMap("type", "guide")));
            verifyUploadedFile(secondFile, "release-notes/august/migration.md", "release-notes/august/",
                Collections.singletonMap("type", "guide"));
            uploadedFileIds.add(secondFile.getFileId());

            List<PagedResponse<KnowledgeSourceFile>> pages = new ArrayList<>();
            searchIndexClient
                .listKnowledgeSourceFiles(knowledgeSourceName, "release-notes/august/", null, 1,
                    ListingSearchType.PREFIX)
                .iterableByPage()
                .forEach(pages::add);
            verifyPrefixPagination(pages);

            Map<String, String> updatedMetadata = new LinkedHashMap<>(metadata);
            updatedMetadata.put("status", "reviewed");
            KnowledgeSourceFile updatedFile = searchIndexClient.updateKnowledgeSourceFile(firstFile.getFileId(),
                knowledgeSourceName,
                new UpdateKnowledgeSourceFileRequest(
                    new FileUploadMetadata().setFileName(firstFile.getFileName()).setMetadata(updatedMetadata),
                    createFileContent("# August features\n\nUpdated and reviewed.", "features.md")));
            if (!firstFile.getFileId().equals(updatedFile.getFileId())
                || !updatedMetadata.equals(updatedFile.getMetadata())) {
                throw new IllegalStateException("The file wasn't updated in place.");
            }

            searchIndexClient.deleteKnowledgeSourceFile(secondFile.getFileId(), knowledgeSourceName);
            uploadedFileIds.remove(secondFile.getFileId());
            long remainingFiles = searchIndexClient.listKnowledgeSourceFiles(knowledgeSourceName).stream().count();
            if (remainingFiles != 1) {
                throw new IllegalStateException("Deleting one file should leave the other file intact.");
            }

            searchIndexClient.deleteKnowledgeSourceFile(firstFile.getFileId(), knowledgeSourceName);
            uploadedFileIds.remove(firstFile.getFileId());
            if (searchIndexClient.listKnowledgeSourceFiles(knowledgeSourceName).stream().findAny().isPresent()) {
                throw new IllegalStateException("The File knowledge source should be empty after deleting its files.");
            }
        } finally {
            uploadedFileIds.forEach(fileId -> searchIndexClient.deleteKnowledgeSourceFile(fileId, knowledgeSourceName));
            if (knowledgeSourceCreated) {
                searchIndexClient.deleteKnowledgeSource(knowledgeSourceName);
            }
        }
    }

    private static UploadKnowledgeSourceFileMultipartRequest createUploadRequest(String fileName, String contents,
        Map<String, String> metadata) {
        FileUploadMetadata fileMetadata = new FileUploadMetadata().setFileName(fileName).setMetadata(metadata);
        String contentFileName = fileName.substring(fileName.lastIndexOf('/') + 1);
        return new UploadKnowledgeSourceFileMultipartRequest(fileMetadata,
            createFileContent(contents, contentFileName));
    }

    private static ContentFileDetails createFileContent(String contents, String fileName) {
        return new ContentFileDetails(BinaryData.fromString(contents)).setFilename(fileName)
            .setContentType("text/markdown; charset=utf-8");
    }

    private static void verifyUploadedFile(KnowledgeSourceFile file, String expectedName, String expectedPrefix,
        Map<String, String> expectedMetadata) {
        if (file.getFileId() == null || !expectedName.equals(file.getFileName())
            || !expectedPrefix.equals(file.getPrefix()) || !expectedMetadata.equals(file.getMetadata())
            || file.getParsingMode() == null || file.getExtractionMode() == null) {
            throw new IllegalStateException("The uploaded file metadata didn't match the request.");
        }

        FileKnowledgeSourceExtractionMode extractionMode = file.getExtractionMode();
        if (!FileKnowledgeSourceExtractionMode.MINIMAL.equals(extractionMode)
            && !FileKnowledgeSourceExtractionMode.STANDARD.equals(extractionMode)) {
            throw new IllegalStateException("The service returned an unsupported extraction mode.");
        }
        System.out.println("The service selected parsing mode " + file.getParsingMode() + " and extraction mode "
            + extractionMode + ".");
    }

    private static void verifyPrefixPagination(List<PagedResponse<KnowledgeSourceFile>> pages) {
        if (pages.size() != 2 || pages.stream().anyMatch(page -> page.getElements().stream().count() != 1)
            || pages.get(0).getContinuationToken() == null
            || pages.get(pages.size() - 1).getContinuationToken() != null) {
            throw new IllegalStateException("Prefix listing didn't return two cursor-paginated file pages.");
        }
    }
}

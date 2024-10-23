// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.VectorStoreFile;
import com.azure.ai.openai.assistants.models.VectorStoreFileBatch;
import com.azure.ai.openai.assistants.models.VectorStoreStaticChunkingStrategyOptions;
import com.azure.ai.openai.assistants.models.VectorStoreStaticChunkingStrategyResponse;
import com.azure.core.http.HttpClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class VectorStoreTestBase extends AssistantsClientTestBase {
    static final String APPLE_FINANCIAL_STATEMENT = "20220924_aapl_10k.pdf";
    static final String ALPHABET_FINANCIAL_STATEMENT = "20210203_alphabet_10K.pdf";

    public abstract void updateVectorStoreName(HttpClient httpClient, AssistantsServiceVersion serviceVersion);
    public abstract void getVectorStore(HttpClient httpClient, AssistantsServiceVersion serviceVersion);
    public abstract void listVectorStore(HttpClient httpClient, AssistantsServiceVersion serviceVersion);
    public abstract void createVectorStoreFile(HttpClient httpClient, AssistantsServiceVersion serviceVersion);
    public abstract void createVectorStoreFileWithAutoChunkingStrategy(HttpClient httpClient, AssistantsServiceVersion serviceVersion);
    public abstract void createVectorStoreFileWithStaticChunkingStrategy(HttpClient httpClient, AssistantsServiceVersion serviceVersion);
    public abstract void throwExceptionWhenOverrideExistChunkStrategy(HttpClient httpClient, AssistantsServiceVersion serviceVersion);
    public abstract void getVectorStoreFile(HttpClient httpClient, AssistantsServiceVersion serviceVersion);
    public abstract void listVectorStoreFiles(HttpClient httpClient, AssistantsServiceVersion serviceVersion);
    public abstract void deleteVectorStoreFile(HttpClient httpClient, AssistantsServiceVersion serviceVersion);
    public abstract void createVectorStoreFileBatch(HttpClient httpClient, AssistantsServiceVersion serviceVersion);
    public abstract void createVectorStoreFileBatchWithAutoChunkingStrategy(HttpClient httpClient, AssistantsServiceVersion serviceVersion);
    public abstract void createVectorStoreFileBatchWithStaticChunkingStrategy(HttpClient httpClient, AssistantsServiceVersion serviceVersion);
    public abstract void throwExceptionWhenOverrideExistChunkStrategyInBatch(HttpClient httpClient, AssistantsServiceVersion serviceVersion);
    public abstract void getVectorStoreFileBatch(HttpClient httpClient, AssistantsServiceVersion serviceVersion);
    public abstract void listVectorStoreFilesBatch(HttpClient httpClient, AssistantsServiceVersion serviceVersion);
    public abstract void cancelVectorStoreFileBatch(HttpClient httpClient, AssistantsServiceVersion serviceVersion);


    void assertVectorStoreFile(VectorStoreFile vectorStoreFile) {
        assertNotNull(vectorStoreFile);
        assertNotNull(vectorStoreFile.getId());
    }

    void assertVectorStoreFileBatch(VectorStoreFileBatch vectorStoreFileBatch, int fileCounts) {
        assertNotNull(vectorStoreFileBatch);
        assertNotNull(vectorStoreFileBatch.getId());
        assertEquals(fileCounts, vectorStoreFileBatch.getFileCounts().getTotal());
    }

    void assertStaticChunkingStrategy(VectorStoreFile vectorStoreFile, int maxChunkSizeTokens, int chunkOverlapTokens) {
        VectorStoreStaticChunkingStrategyResponse chunkingStrategy = (VectorStoreStaticChunkingStrategyResponse)
            vectorStoreFile.getChunkingStrategy();
        VectorStoreStaticChunkingStrategyOptions staticProperty = chunkingStrategy.getStaticProperty();
        assertNotNull(staticProperty);
        assertEquals(maxChunkSizeTokens, staticProperty.getMaxChunkSizeTokens());
        assertEquals(chunkOverlapTokens, staticProperty.getChunkOverlapTokens());
    }
}

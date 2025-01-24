// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.document;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

public final class ReadmeSamples {

    /**
     * Sample for creating document translation client using API Key and endpoint.
     */
    public void createDocumentTranslationClient() {
        // BEGIN: createDocumentTranslationClient
        String endpoint = System.getenv("DOCUMENT_TRANSLATION_ENDPOINT");
        String apiKey = System.getenv("DOCUMENT_TRANSLATION_API_KEY");

        AzureKeyCredential credential = new AzureKeyCredential(apiKey);

        DocumentTranslationClient client = new DocumentTranslationClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .buildClient();
        // END: createDocumentTranslationClient
    }

    /**
     * Sample for creating single document translation client using API Key and endpoint.
     */
    public void createSingleDocumentTranslationClient() {
        // BEGIN: createSingleDocumentTranslationClient
        String endpoint = System.getenv("DOCUMENT_TRANSLATION_ENDPOINT");
        String apiKey = System.getenv("DOCUMENT_TRANSLATION_API_KEY");

        AzureKeyCredential credential = new AzureKeyCredential(apiKey);

        SingleDocumentTranslationClient client = new SingleDocumentTranslationClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .buildClient();
        // END: createSingleDocumentTranslationClient
    }

    /**
     * Sample for creating document translation client using AAD.
     */
    public void createDocumentTranslationClientWithAAD() {
        // BEGIN: createDocumentTranslationClientWithAAD
        String endpoint = System.getenv("DOCUMENT_TRANSLATION_ENDPOINT");

        TokenCredential defaultCredential = new DefaultAzureCredentialBuilder().build();
        DocumentTranslationClient client = new DocumentTranslationClientBuilder()
            .endpoint(endpoint)
            .credential(defaultCredential)
            .buildClient();
        // END: createDocumentTranslationClientWithAAD
    }

    /**
     * Sample for creating single document translation client using AAD.
     */
    public void createSingleDocumentTranslationClientWithAAD() {
        // BEGIN: createSingleDocumentTranslationClientWithAAD
        String endpoint = System.getenv("DOCUMENT_TRANSLATION_ENDPOINT");

        TokenCredential defaultCredential = new DefaultAzureCredentialBuilder().build();

        SingleDocumentTranslationClient client = new SingleDocumentTranslationClientBuilder()
            .endpoint(endpoint)
            .credential(defaultCredential)
            .buildClient();
        // END: createSingleDocumentTranslationClientWithAAD
    }
}

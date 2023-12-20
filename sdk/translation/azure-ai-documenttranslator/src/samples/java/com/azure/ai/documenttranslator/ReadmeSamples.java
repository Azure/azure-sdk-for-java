// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documenttranslator;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;

/**
 * Code samples for the README.md
 */
public class ReadmeSamples {
    /**
     * Sample for creating low level client.
     */
    public void createClient() {
        // BEGIN: readme-sample-createBatchDocumentTranslationRestClient
        String endpoint = String.format("https://%s.cognitiveservices.azure.com/translator/text/batch/v1.0-preview.1",
            "<document-translator-resource-name>");
        String apiKey = "<document-translator-api-key>";

        BatchDocumentTranslationClient client = new BatchDocumentTranslationClientBuilder()
            .credential(new AzureKeyCredential(apiKey))
            .endpoint(endpoint)
            .httpClient(new NettyAsyncHttpClientBuilder().build())
            .buildClient();
        // END: readme-sample-createBatchDocumentTranslationRestClient
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documenttranslator;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.SyncPoller;

import javax.json.Json;
import javax.json.JsonObject;

/**
 * Sample for translating documents using the document translator client.
 */
public class TranslateDocuments {
    /**
     * Main method to invoke this demo.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(final String[] args) {
        // Step 0: create a client
        BatchDocumentTranslationClient client = new BatchDocumentTranslationClientBuilder()
            .credential(new AzureKeyCredential(System.getenv("API_KEY")))
            .endpoint(System.getenv("API_ENDPOINT"))
            .httpClient(new NettyAsyncHttpClientBuilder().build())
            .buildClient();

        // Step 1: Construct the request object
        JsonObject source = Json.createObjectBuilder()
            .add("sourceUrl", "SOURCE_URL")
            .build();

        JsonObject target = Json.createObjectBuilder()
            .add("language", "zh-Hans")
            .add("targetUrl", "TARGET_URL")
            .build();

        JsonObject input = Json.createObjectBuilder()
            .add("source", source)
            .add("targets", Json.createArrayBuilder().add(target).build())
            .build();

        JsonObject requestBody = Json.createObjectBuilder()
            .add("inputs", Json.createArrayBuilder().add(input).build())
            .build();

        // Step 2: Send the request
        SyncPoller<BinaryData, BinaryData> poller = client.beginStartTranslation(
            BinaryData.fromString(requestBody.toString()), null);

        System.out.println("Translation request submitted...");

        // Step 3: Poll until translation is completed
        poller.waitForCompletion();
    }
}

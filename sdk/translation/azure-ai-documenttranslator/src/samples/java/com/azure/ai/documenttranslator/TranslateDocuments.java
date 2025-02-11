// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documenttranslator;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.SyncPoller;
import com.azure.json.models.JsonArray;
import com.azure.json.models.JsonObject;
import com.azure.json.models.JsonString;

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
        JsonObject source = new JsonObject().setProperty("sourceUrl", new JsonString("SOURCE_URL"));

        JsonObject target = new JsonObject()
            .setProperty("language", new JsonString("zh-Hans"))
            .setProperty("targetUrl", new JsonString("TARGET_URL"));

        JsonObject input = new JsonObject()
            .setProperty("source", source)
            .setProperty("targets", new JsonArray().addElement(target));

        JsonObject requestBody = new JsonObject().setProperty("inputs", new JsonArray().addElement(input));

        // Step 2: Send the request
        SyncPoller<BinaryData, BinaryData> poller = client.beginStartTranslation(BinaryData.fromObject(requestBody),
            null);

        System.out.println("Translation request submitted...");

        // Step 3: Poll until translation is completed
        poller.waitForCompletion();
    }
}

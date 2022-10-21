// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documenttranslator;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.experimental.http.DynamicResponse;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;

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
        BatchDocumentTranslationRestClient client = new BatchDocumentTranslationClientBuilder()
            .credential(new AzureKeyCredential(System.getenv("API_KEY")))
            .endpoint(System.getenv("API_ENDPOINT"))
            .httpClient(new NettyAsyncHttpClientBuilder().build())
            .buildRestClient();

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
        DynamicResponse response = client.startTranslation()
            .setBody(requestBody.toString())
            .send();

        if (response.getStatusCode() / 100 != 2) {
            System.err.println("Received error: " + response.getBody().toString());
            return;
        }

        System.out.println("Translation request submitted...");

        // Step 3: Poll until translation is completed
        while (response.getStatusCode() == 202) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            String operationLocation = response.getHeaders().getValue("Operation-Location");
            DynamicResponse pollResponse = client.invoke()
                .setUrl(operationLocation)
                .setHttpMethod(HttpMethod.GET)
                .send();

            String pollBody = pollResponse.getBody().toString();
            JsonReader jsonReader = Json.createReader(new StringReader(pollBody));
            JsonObject pollResult = jsonReader.readObject();
            String status = pollResult.getString("status");
            if ("NotStarted".equalsIgnoreCase(status)) {
                System.out.println("Translation running...");
            } else if ("Running".equalsIgnoreCase(status)) {
                System.out.println("Translation running...");
            } else if ("Succeeded".equalsIgnoreCase(status)) {
                System.out.println("Translation succeeded.");
                break;
            } else {
                System.err.println("Unexpected status: " + status);
                break;
            }
        }
    }
}

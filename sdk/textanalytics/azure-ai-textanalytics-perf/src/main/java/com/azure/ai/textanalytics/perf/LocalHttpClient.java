// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.perf;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Context;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * A local implementation of {@link HttpClient} used for testing the performance of Text Analytics APIs. This client
 * is used instead of real Azure service to effectively perform the performance tests.
 */
public class LocalHttpClient implements HttpClient {

    private final int docCount;

    /**
     * Creates an instance of the the client.
     * @param docCount The number of documents this client returns in the response.
     */
    public LocalHttpClient(int docCount) {
        this.docCount = docCount;
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest httpRequest) {
        return Mono.empty();
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request, Context context) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            JsonGenerator generator = new JsonFactory().createGenerator(outputStream);

            generator.writeStartObject();
            generator.writeArrayFieldStart("documents");
            for (int i = 0; i < docCount; i++) {
                generator.writeStartObject();
                generator.writeFieldName("id");
                generator.writeString(String.valueOf(i));

                generator.writeFieldName("detectedLanguage");
                generator.writeStartObject();
                generator.writeFieldName("name");
                generator.writeString("English");
                generator.writeFieldName("iso6391Name");
                generator.writeString("en");
                generator.writeFieldName("confidenceScore");
                generator.writeNumber(0.90);
                generator.writeEndObject();

                generator.writeArrayFieldStart("warnings");
                generator.writeEndArray();

                generator.writeEndObject();

            }
            generator.writeEndArray();

            generator.writeArrayFieldStart("errors");
            generator.writeEndArray();

            generator.writeFieldName("statistics");
            generator.writeStartObject();
            generator.writeFieldName("charactersCount");
            generator.writeNumber(90);
            generator.writeFieldName("transactionsCount");
            generator.writeNumber(90);
            generator.writeEndObject();

            generator.writeEndObject();

            generator.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Mono.just(new MockHttpResponse(request, 200, new HttpHeaders(), outputStream.toByteArray()));
    }
}

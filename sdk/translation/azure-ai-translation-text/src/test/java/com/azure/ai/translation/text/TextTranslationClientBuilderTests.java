// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.text;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TextTranslationClientBuilderTests {
    private static final String TRANSLATE_RESPONSE
        = "{\"value\":[{\"detectedLanguage\":{\"language\":\"en\",\"score\":1.0},"
            + "\"translations\":[{\"language\":\"cs\",\"sourceCharacters\":5,\"text\":\"Ahoj\"}]}]}";

    @Test
    public void customEndpointUsesApiVersionQueryParameter() {
        RecordingHttpClient httpClient = new RecordingHttpClient();
        TextTranslationClient client
            = new TextTranslationClientBuilder().endpoint("https://fakeCustomEndpoint.cognitiveservices.azure.com")
                .credential(new AzureKeyCredential("key"))
                .httpClient(httpClient)
                .buildClient();

        client.translate("cs", "Hello");

        assertEquals(
            "https://fakeCustomEndpoint.cognitiveservices.azure.com/translator/text/translate?api-version=2026-06-06",
            httpClient.getRequest().getUrl().toString());
    }

    private static final class RecordingHttpClient implements HttpClient {
        private HttpRequest request;

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            this.request = request;
            HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json");
            return Mono
                .just(new MockHttpResponse(request, 200, headers, TRANSLATE_RESPONSE.getBytes(StandardCharsets.UTF_8)));
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request, Context context) {
            return send(request);
        }

        private HttpRequest getRequest() {
            return request;
        }
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests.samples;

import com.azure.ai.contentunderstanding.ContentUnderstandingAsyncClient;
import com.azure.ai.contentunderstanding.ContentUnderstandingClientBuilder;
import com.azure.ai.contentunderstanding.samples.Sample02_AnalyzeUrlAsync;
import com.azure.core.credential.KeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.test.http.MockHttpResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class Sample02AsyncErrorPropagationTest {
    @Test
    public void analyzeDocumentUrlPropagatesServiceFailure() {
        HttpClient httpClient = request -> Mono.just(new MockHttpResponse(request, 500,
            "{\"error\":{\"code\":\"InternalError\",\"message\":\"boom\"}}".getBytes(StandardCharsets.UTF_8)));
        ContentUnderstandingAsyncClient client = new ContentUnderstandingClientBuilder().endpoint("https://example.com")
            .credential(new KeyCredential("fake-key"))
            .httpClient(httpClient)
            .buildAsyncClient();

        assertThrows(HttpResponseException.class, () -> Sample02_AnalyzeUrlAsync.analyzeDocumentUrl(client));
    }
}

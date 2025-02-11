// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.SyncAsyncExtension;
import com.azure.core.test.annotation.SyncAsyncTest;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.http.NoOpHttpClient;
import com.azure.core.util.Context;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.Constants;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StorageSharedKeyCredentialPolicyTest {
    private static final HttpResponse MOCK_HTTP_RESPONSE = new MockHttpResponse(null, 200, new HttpHeaders());

    @SyncAsyncTest
    public void sharedKeyCredAddsAuthHeader() throws MalformedURLException {
        StorageSharedKeyCredential storageSharedKeyCredential
            = new StorageSharedKeyCredential("testAccountName", "testAccountKey");

        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                return Mono.just(MOCK_HTTP_RESPONSE);
            }
        }).policies(new StorageSharedKeyCredentialPolicy(storageSharedKeyCredential), (context, next) -> {
            String expectedSignature
                = storageSharedKeyCredential.generateAuthorizationHeader(context.getHttpRequest().getUrl(),
                    context.getHttpRequest().getHttpMethod().toString(), context.getHttpRequest().getHeaders(),
                    Boolean.TRUE.equals(context.getData(Constants.STORAGE_LOG_STRING_TO_SIGN).orElse(false)));
            assertEquals(expectedSignature,
                context.getHttpRequest().getHeaders().get(HttpHeaderName.AUTHORIZATION).getValue());
            return next.process();
        }).build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, new URL("http://localhost/"));
        SyncAsyncExtension.execute(() -> pipeline.sendSync(request, Context.NONE), () -> pipeline.send(request));
    }
}

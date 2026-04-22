// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Context;

import reactor.core.publisher.Mono;

import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static com.azure.identity.AuthenticationUtil.getBearerTokenSupplier;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AuthenticationUtilTest {

    @Test
    public void testGetBearerTokenSupplier() {
        HttpClient mockHttpClient = new HttpClient() {

            @Override
            public HttpResponse sendSync(HttpRequest httpRequest, Context context) {
                return new MockHttpResponse(httpRequest, 200);
            }

            @Override
            public Mono<HttpResponse> send(HttpRequest httpRequest) {
                return Mono.just(new MockHttpResponse(httpRequest, 200));
            }
        };

        MockTokenCredential credential = new MockTokenCredential();
        Supplier<String> supplier = getBearerTokenSupplier(credential, mockHttpClient, "scope");
        assertEquals("mockToken", supplier.get());
    }
}

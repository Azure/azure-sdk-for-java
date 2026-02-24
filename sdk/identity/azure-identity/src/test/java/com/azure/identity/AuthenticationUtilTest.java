// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.test.utils.MockTokenCredential;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static com.azure.identity.AuthenticationUtil.getBearerTokenSupplier;
import static org.junit.jupiter.api.Assertions.assertEquals;
import com.azure.core.http.HttpClient;
import com.azure.core.test.http.MockHttpResponse;
import static org.mockito.Mockito.mockStatic;
import org.mockito.MockedStatic;
import reactor.core.publisher.Mono;

public class AuthenticationUtilTest {

    @Test
    public void testGetBearerTokenSupplier() {
        HttpClient mockHttpClient = request -> Mono.just(new MockHttpResponse(request, 200));

        try (MockedStatic<HttpClient> httpClientMock = mockStatic(HttpClient.class)) {
            httpClientMock.when(HttpClient::createDefault).thenReturn(mockHttpClient);

            MockTokenCredential credential = new MockTokenCredential();
            Supplier<String> supplier = getBearerTokenSupplier(credential, "scope");
            assertEquals("mockToken", supplier.get());
        }
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Context;

import org.apache.tools.ant.taskdefs.condition.Http;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.function.Supplier;

import static com.azure.identity.AuthenticationUtil.getBearerTokenSupplier;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class AuthenticationUtilTest {

    @Test
    public void testGetBearerTokenSupplier() {
        HttpClient mockHttpClient = Mockito.mock(HttpClient.class);
        when(mockHttpClient.sendSync(any(HttpRequest.class), any(Context.class)))
            .thenAnswer(invocation -> new MockHttpResponse(invocation.getArgument(0, HttpRequest.class), 200));
        MockTokenCredential credential = new MockTokenCredential();
        Supplier<String> supplier = getBearerTokenSupplier(credential, mockHttpClient, "scope");
        assertEquals("mockToken", supplier.get());
    }
}

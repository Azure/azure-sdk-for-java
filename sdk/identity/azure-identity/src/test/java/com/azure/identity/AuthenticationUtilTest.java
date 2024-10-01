// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.test.utils.MockTokenCredential;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.function.Supplier;

import static com.azure.identity.AuthenticationUtil.getBearerTokenSupplier;
import static com.azure.identity.AuthenticationUtil.getBearerTokenSupplierSync;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AuthenticationUtilTest {

    @Test
    public void testGetBearerTokenSupplierSync() {
        MockTokenCredential credential = new MockTokenCredential();
        Supplier<String> supplier = getBearerTokenSupplierSync(credential, "scope");
        assertEquals("mockToken", supplier.get());
    }

    @Test
    public void testGetBearerTokenSupplier() {
        MockTokenCredential credential = new MockTokenCredential();
        StepVerifier.create(
        Mono.fromSupplier(getBearerTokenSupplier(credential, "https://graph.microsoft.com/.default")).flatMap(s -> s))
            .expectNext("mockToken").verifyComplete();
    }
}

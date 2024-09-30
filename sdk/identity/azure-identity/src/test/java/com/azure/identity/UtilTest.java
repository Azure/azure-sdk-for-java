// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.test.utils.MockTokenCredential;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UtilTest {

    @Test
    public void testGetBearerTokenSupplier() {
        MockTokenCredential credential = new MockTokenCredential();
        Supplier<String> supplier = Util.getBearerTokenSupplier(credential, "scope");
        assertEquals("mockToken", supplier.get());
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.storage.blob.BlobTestBase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class SessionCredentialTest {

    @Test
    public void constructorRejectsNullExpiration() {
        assertThrows(NullPointerException.class, () -> new SessionCredential(BlobTestBase.TEST_SESSION_TOKEN,
            BlobTestBase.TEST_SESSION_KEY, null, BlobTestBase.TEST_SESSION_ACCOUNT_NAME));
    }
}

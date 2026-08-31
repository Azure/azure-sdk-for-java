// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.storage.blob.BlobTestBase;
import com.azure.storage.blob.models.SessionCredential;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SessionCredentialTest {

    @Test
    public void isExpiredReturnsTrueWhenPastExpiration() {
        assertTrue(createCredential(BlobTestBase.createExpiredSessionExpiration()).isExpired());
    }

    @Test
    public void isExpiredReturnsFalseWhenBeforeExpiration() {
        assertFalse(createCredential(BlobTestBase.createValidSessionExpiration()).isExpired());
    }

    @Test
    public void constructorRejectsNullExpiration() {
        assertThrows(NullPointerException.class, () -> new SessionCredential(BlobTestBase.TEST_SESSION_TOKEN,
            BlobTestBase.TEST_SESSION_KEY, null, BlobTestBase.TEST_SESSION_ACCOUNT_NAME));
    }

    private static SessionCredential createCredential(java.time.OffsetDateTime expiration) {
        return new SessionCredential(BlobTestBase.TEST_SESSION_TOKEN, BlobTestBase.TEST_SESSION_KEY, expiration,
            BlobTestBase.TEST_SESSION_ACCOUNT_NAME);
    }
}

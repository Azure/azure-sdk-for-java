// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.storage.blob.models.SessionCredential;
import com.azure.storage.common.test.shared.session.SessionTestHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SessionCredentialTest {

    @Test
    public void isExpiredReturnsTrueWhenPastExpiration() {
        assertTrue(createCredential(SessionTestHelper.createExpiredExpiration()).isExpired());
    }

    @Test
    public void isExpiredReturnsFalseWhenBeforeExpiration() {
        assertFalse(createCredential(SessionTestHelper.createValidExpiration()).isExpired());
    }

    @Test
    public void constructorRejectsNullExpiration() {
        assertThrows(NullPointerException.class, () -> new SessionCredential(SessionTestHelper.TEST_SESSION_TOKEN,
            SessionTestHelper.TEST_SESSION_KEY, null, SessionTestHelper.TEST_ACCOUNT_NAME));
    }

    private static SessionCredential createCredential(java.time.OffsetDateTime expiration) {
        return new SessionCredential(SessionTestHelper.TEST_SESSION_TOKEN, SessionTestHelper.TEST_SESSION_KEY,
            expiration, SessionTestHelper.TEST_ACCOUNT_NAME);
    }
}

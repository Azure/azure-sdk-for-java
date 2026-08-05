// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import java.time.OffsetDateTime;

/**
 * Shared test constants and factories for session-based auth tests.
 */
final class SessionTestHelper {

    // A valid Base64-encoded 32-byte key for testing
    static final String TEST_SESSION_KEY = "dGVzdFNlc3Npb25LZXkxMjM0NTY3ODkwMTIzNDU2Nzg5MA==";
    static final String TEST_SESSION_TOKEN = "test-session-token-abc123";
    static final String TEST_ACCOUNT_NAME = "myaccount";
    static final String TEST_CONTAINER_NAME = "testcontainer";

    static StorageSessionCredential createCredential(OffsetDateTime expiration) {
        return new StorageSessionCredential(TEST_SESSION_TOKEN, TEST_SESSION_KEY, expiration, TEST_ACCOUNT_NAME);
    }

    static StorageSessionCredential createCredential(OffsetDateTime expiration, String accountName) {
        return new StorageSessionCredential(TEST_SESSION_TOKEN, TEST_SESSION_KEY, expiration, accountName);
    }

    static StorageSessionCredential createValidCredential() {
        return createCredential(OffsetDateTime.now().plusHours(1));
    }

    static StorageSessionCredential createExpiredCredential() {
        return createCredential(OffsetDateTime.now().minusMinutes(5));
    }

    private SessionTestHelper() {
    }
}

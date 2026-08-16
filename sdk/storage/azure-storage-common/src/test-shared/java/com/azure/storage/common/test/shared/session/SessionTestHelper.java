// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared.session;

import java.time.OffsetDateTime;

/**
 * Shared constants and expiration fixtures for session authentication tests.
 */
public final class SessionTestHelper {
    public static final String TEST_SESSION_KEY = "dGVzdFNlc3Npb25LZXkxMjM0NTY3ODkwMTIzNDU2Nzg5MA==";
    public static final String TEST_SESSION_TOKEN = "test-session-token-abc123";
    public static final String TEST_ACCOUNT_NAME = "testaccount";
    public static final String TEST_CONTAINER_NAME = "testcontainer";

    /**
     * Creates an expiration time in the future.
     *
     * @return A valid session expiration time.
     */
    public static OffsetDateTime createValidExpiration() {
        return OffsetDateTime.now().plusHours(1);
    }

    /**
     * Creates an expiration time in the past.
     *
     * @return An expired session expiration time.
     */
    public static OffsetDateTime createExpiredExpiration() {
        return OffsetDateTime.now().minusMinutes(5);
    }

    private SessionTestHelper() {
    }
}

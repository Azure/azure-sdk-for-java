// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test;

import com.azure.core.test.models.RecordingRedactor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Test for {@linl RecordingRedactor} that redact the sensitive information while recording.
 */
public class RecordingRedactorTests {
    private static final String DUMMY_SENSITIVE_INFORMATION = "sensitiveInformation";

    // Non-sensitive content data, keep the content as it is
    private static final String NON_SENSITIVE_DATA_CONTENT = "\"Response\" : {\n"
        + "      \"Body\" : \"{\"a\":\"a\",\"expirationDateTimeTicks\":637270217074441783}\",\n"
        + "    },";

    // Access token value pair at Body
    private static final String ACCESS_TOKEN_FIRST_PAIR = "\"Response\" : {\n"
        + "      \"Body\" : \"{\"accessToken\":\"sensitiveData\",\"expirationDateTimeTicks\":637270217074441783}\",\n"
        + "    },";
    private static final String EXPECTED_ACCESS_TOKEN_FIRST_PAIR_REDACTED = "\"Response\" : {\n"
        + "      \"Body\" : \"{\"accessToken\":\"REDACTED\",\"expirationDateTimeTicks\":637270217074441783}\",\n"
        + "    },";

    // Access token pair at the end of Body
    private static final String ACCESS_TOKEN_LAST_PAIR = "\"Response\" : {\n"
        + "      \"Body\" : \"{\"accessToken\":\"sensitiveData\"}\",\n"
        + "    },";
    private static final String EXPECTED_ACCESS_TOKEN_LAST_PAIR_REDACTED = "\"Response\" : {\n"
        + "      \"Body\" : \"{\"accessToken\":\"REDACTED\"}\",\n"
        + "    },";

    // User delegation key: <Value> XML tag
    private static final String USER_DELEGATION_KEY_FOR_VALUE_RESPONSE = "\"Response\" : {\n"
        + "   \"Body\" : <UserDelegationKey><Value>sensitiveInformation=</Value></UserDelegationKey>\",\n"
        + "    },";

    private static final String EXPECTED_USER_DELEGATION_KEY_FOR_VALUE_RESPONSE_REDACTED = "\"Response\" : {\n"
        + "   \"Body\" : <UserDelegationKey><Value>UkVEQUNURUQ=</Value></UserDelegationKey>\",\n"
        + "    },";

    // User delegation key: <SignedOid> XML tag
    private static final String USER_DELEGATION_KEY_FOR_SIGNED_OID_RESPONSE = "\"Response\" : {\n"
        + "   \"Body\" : <UserDelegationKey><SignedOid>sensitiveInformation=</SignedOid></UserDelegationKey>\",\n"
        + "    },";

    // User delegation key: <SignedTid> XML tag
    private static final String USER_DELEGATION_KEY_FOR_SIGNED_TID_RESPONSE = "\"Response\" : {\n"
        + "   \"Body\" : <UserDelegationKey><SignedTid>sensitiveInformation</SignedTid></UserDelegationKey>\",\n"
        + "    },";

    /**
     * Verify if the given content has no key in black list, no change will be required.
     */
    @Test
    public void nonSensitiveDataInContent() {
        assertEquals(NON_SENSITIVE_DATA_CONTENT, new RecordingRedactor().redact(NON_SENSITIVE_DATA_CONTENT));
    }

    /**
     * Verify if the {@code accessToken} pair that is before last pair value is redacted successfully.
     */
    @Test
    public void replaceAccessTokenContent() {
        assertEquals(EXPECTED_ACCESS_TOKEN_FIRST_PAIR_REDACTED, new RecordingRedactor().redact(ACCESS_TOKEN_FIRST_PAIR));
    }

    /**
     * Verify if the {@code accessToken} pair that is the last pair value is redacted successfully.
     */
    @Test
    public void replaceAccessTokenContentAtEnd() {
        assertEquals(EXPECTED_ACCESS_TOKEN_LAST_PAIR_REDACTED, new RecordingRedactor().redact(ACCESS_TOKEN_LAST_PAIR));
    }

    /**
     * Verify if the value in the XML tag {@code <VALUE>} is redacted successfully.
     */
    @Test
    public void replaceUserDelegationKeyForValueTag() {
        assertEquals(EXPECTED_USER_DELEGATION_KEY_FOR_VALUE_RESPONSE_REDACTED, new RecordingRedactor().redact(USER_DELEGATION_KEY_FOR_VALUE_RESPONSE));
    }

    /**
     * Verify if the value in the XML tag {@code <SignedOid>} is redacted successfully.
     */
    @Test
    public void replaceUserDelegationKeyForSignedOidTag() {
        assertFalse(new RecordingRedactor().redact(USER_DELEGATION_KEY_FOR_SIGNED_OID_RESPONSE).contains(DUMMY_SENSITIVE_INFORMATION));
    }

    /**
     * Verify if the value in the XML tag {@code <SignedTid>} is redacted successfully.
     */
    @Test
    public void replaceUserDelegationKeyForSignedTidTag() {
        assertFalse(new RecordingRedactor().redact(USER_DELEGATION_KEY_FOR_SIGNED_TID_RESPONSE).contains(DUMMY_SENSITIVE_INFORMATION));
    }
}

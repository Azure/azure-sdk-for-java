// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test;

import com.azure.core.test.models.RecordingRedactor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RecordingRedactorTests {
    // Access token value pair at Body
    private static final String CONTENT = "\"Response\" : {\n" +
        "      \"Body\" : \"{\"accessToken\":\"sensitiveData\",\"expirationDateTimeTicks\":637270217074441783}\",\n" +
        "    },";
    private static final String EXPECTED_CONTENT_REDACTED = "\"Response\" : {\n" +
        "      \"Body\" : \"{\"accessToken\":\"REDACTED\",\"expirationDateTimeTicks\":637270217074441783}\",\n" +
        "    },";

    // Access token pair at the end of Body
    private static final String CONTENT_ACCESS_TOKEN_END = "\"Response\" : {\n" +
        "      \"Body\" : \"{\"accessToken\":\"sensitiveData\"}\",\n" +
        "    },";
    private static final String EXPECTED_CONTENT_ACCESS_TOKEN_END_REDACTED = "\"Response\" : {\n" +
        "      \"Body\" : \"{\"accessToken\":\"REDACTED\"}\",\n" +
        "    },";

    /**
     * Verify if the {@code accessToken} pair that is before last pair value is redacted successfully.
     */
    @Test
    public void replaceAccessTokenContent() {
        assertEquals(EXPECTED_CONTENT_REDACTED, new RecordingRedactor().redact(CONTENT));
    }

    /**
     * Verify if the {@code accessToken} pair that is the last pair value is redacted successfully.
     */
    @Test
    public void replaceAccessTokenContentAtEnd() {
        assertEquals(EXPECTED_CONTENT_ACCESS_TOKEN_END_REDACTED, new RecordingRedactor().redact(CONTENT_ACCESS_TOKEN_END));
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestHelpersTests {
    @Test
    public void unexpectedHttpFailureRetainsItsDiagnosticMessage() {
        RuntimeException unexpected = new RuntimeException("Unexpected transport failure");
        AssertionError failure
            = assertThrows(AssertionError.class, () -> TestHelpers.verifyHttpResponseError(unexpected, 400, null));

        assertTrue(failure.getMessage().contains(unexpected.toString()));
    }
}

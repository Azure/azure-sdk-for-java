// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for {@link StorageContentValidationDecoderPolicy}.
 * 
 * Note: The policy behavior is primarily validated through integration tests in BlobBaseAsyncApiTests
 * which test the end-to-end download scenarios with structured message validation.
 */
public class StorageContentValidationDecoderPolicyTest {

    @Test
    public void policyCanBeInstantiated() {
        // Verify the policy can be constructed successfully
        StorageContentValidationDecoderPolicy policy = new StorageContentValidationDecoderPolicy();
        assertNotNull(policy);
    }
}

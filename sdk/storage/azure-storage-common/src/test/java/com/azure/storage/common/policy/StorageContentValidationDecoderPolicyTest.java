// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.storage.common.policy.StorageContentValidationDecoderPolicy.DecoderState;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    public void decoderStateTracksDecodedBytes() {
        // Create a decoder state with no bytes to skip
        DecoderState state = new DecoderState(1024, 0);

        assertNotNull(state);
        assertEquals(0, state.getTotalBytesDecoded());
        assertEquals(0, state.getTotalEncodedBytesProcessed());
    }

    @Test
    public void decoderStateWithBytesToSkip() {
        // Create a decoder state with bytes to skip (retry scenario)
        long bytesToSkip = 512;
        DecoderState state = new DecoderState(1024, bytesToSkip);

        assertNotNull(state);
        assertEquals(0, state.getTotalBytesDecoded());
        assertEquals(0, state.getTotalEncodedBytesProcessed());

        // Verify the bytesToSkip field is set correctly
        // Note: We can't directly access bytesToSkip as it's private, 
        // but it will be used internally during decoding
    }
}

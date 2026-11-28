// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The immutability policy mode keeps the values and order it shipped with: its toString() is what requests send and
 * what callers compare against, and its ordinals are visible through values().
 */
public class BlobImmutabilityPolicyModeTests {
    @Test
    public void keepsShippedValuesAndOrder() {
        assertArrayEquals(new BlobImmutabilityPolicyMode[] {
            BlobImmutabilityPolicyMode.MUTABLE,
            BlobImmutabilityPolicyMode.UNLOCKED,
            BlobImmutabilityPolicyMode.LOCKED }, BlobImmutabilityPolicyMode.values());
        assertEquals("Mutable", BlobImmutabilityPolicyMode.MUTABLE.toString());
        assertEquals("Unlocked", BlobImmutabilityPolicyMode.UNLOCKED.toString());
        assertEquals("Locked", BlobImmutabilityPolicyMode.LOCKED.toString());
    }

    @Test
    public void parsesServiceValuesInAnyCase() {
        assertEquals(BlobImmutabilityPolicyMode.UNLOCKED, BlobImmutabilityPolicyMode.fromString("unlocked"));
        assertEquals(BlobImmutabilityPolicyMode.LOCKED, BlobImmutabilityPolicyMode.fromString("Locked"));
    }
}

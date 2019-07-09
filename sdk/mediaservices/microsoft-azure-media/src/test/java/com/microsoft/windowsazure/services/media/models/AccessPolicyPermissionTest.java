/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.windowsazure.services.media.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;

import org.junit.Test;

public class AccessPolicyPermissionTest {

    @Test
    public void testGetFlagValue() {
        assertEquals(0, AccessPolicyPermission.NONE.getFlagValue());
        assertEquals(1, AccessPolicyPermission.READ.getFlagValue());
        assertEquals(2, AccessPolicyPermission.WRITE.getFlagValue());
        assertEquals(4, AccessPolicyPermission.DELETE.getFlagValue());
        assertEquals(8, AccessPolicyPermission.LIST.getFlagValue());
    }

    @Test
    public void testZeroResultsInNonePermission() {
        EnumSet<AccessPolicyPermission> perms = AccessPolicyPermission
                .permissionsFromBits(0);
        assertTrue(perms.contains(AccessPolicyPermission.NONE));
    }

    @Test
    public void testAllBitsSetResultsInAllPermissions() {
        EnumSet<AccessPolicyPermission> perms = AccessPolicyPermission
                .permissionsFromBits(1 + 2 + 4 + 8);

        assertFalse(perms.contains(AccessPolicyPermission.NONE));
        assertTrue(perms.contains(AccessPolicyPermission.READ));
        assertTrue(perms.contains(AccessPolicyPermission.WRITE));
        assertTrue(perms.contains(AccessPolicyPermission.DELETE));
        assertTrue(perms.contains(AccessPolicyPermission.LIST));
    }

    @Test
    public void testWriteBitsResultsInOnlyWritePermissions() {
        EnumSet<AccessPolicyPermission> perms = AccessPolicyPermission
                .permissionsFromBits(2);

        assertFalse(perms.contains(AccessPolicyPermission.NONE));
        assertFalse(perms.contains(AccessPolicyPermission.READ));
        assertTrue(perms.contains(AccessPolicyPermission.WRITE));
        assertFalse(perms.contains(AccessPolicyPermission.DELETE));
        assertFalse(perms.contains(AccessPolicyPermission.LIST));
    }

    @Test
    public void testEmptyPermissionsResultsInZeroBits() {
        EnumSet<AccessPolicyPermission> perms = EnumSet
                .noneOf(AccessPolicyPermission.class);
        int bits = AccessPolicyPermission.bitsFromPermissions(perms);

        assertEquals(0, bits);
    }

    @Test
    public void allPermissionsInSetResultsInCorrectValue() {
        EnumSet<AccessPolicyPermission> perms = EnumSet.of(
                AccessPolicyPermission.READ, AccessPolicyPermission.WRITE,
                AccessPolicyPermission.DELETE, AccessPolicyPermission.LIST,
                AccessPolicyPermission.NONE);
        int bits = AccessPolicyPermission.bitsFromPermissions(perms);

        assertEquals(1 + 2 + 4 + 8, bits);
    }

    @Test
    public void writePermissionsInSetResultsInCorrectValue() {
        EnumSet<AccessPolicyPermission> perms = EnumSet
                .of(AccessPolicyPermission.WRITE);
        int bits = AccessPolicyPermission.bitsFromPermissions(perms);

        assertEquals(2, bits);
    }

    @Test
    public void unknownPermissionBitsAreIgnored() {
        EnumSet<AccessPolicyPermission> perms = AccessPolicyPermission
                .permissionsFromBits(16 + 32);

        assertTrue(perms.contains(AccessPolicyPermission.NONE));
    }
}

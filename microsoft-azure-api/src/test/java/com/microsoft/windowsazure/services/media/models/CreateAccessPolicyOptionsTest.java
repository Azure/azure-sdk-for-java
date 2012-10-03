/**
 * Copyright 2012 Microsoft Corporation
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

import static org.junit.Assert.*;

import java.util.EnumSet;

import org.junit.Test;

public class CreateAccessPolicyOptionsTest {

    private static void assertPermissions(EnumSet<AccessPolicyPermission> expected,
            EnumSet<AccessPolicyPermission> actual) {
        assertTrue(actual.containsAll(expected));
        assertTrue(EnumSet.complementOf(actual).containsAll(EnumSet.complementOf(expected)));
    }

    @Test
    public void optionsCreatedWithNoPermissions() throws Exception {
        CreateAccessPolicyOptions options = new CreateAccessPolicyOptions();

        assertPermissions(EnumSet.noneOf(AccessPolicyPermission.class), options.getPermissions());
    }

    @Test
    public void canGetSetPermissionsAsEnumSet() throws Exception {
        EnumSet<AccessPolicyPermission> expectedPermissions = EnumSet.of(AccessPolicyPermission.LIST,
                AccessPolicyPermission.DELETE);

        CreateAccessPolicyOptions options = new CreateAccessPolicyOptions();

        options.addPermissions(expectedPermissions);

        EnumSet<AccessPolicyPermission> permissions = options.getPermissions();

        assertPermissions(expectedPermissions, permissions);
    }

    @Test
    public void canGetSetPermissionsAsVarargs() throws Exception {
        EnumSet<AccessPolicyPermission> expectedPermissions = EnumSet.of(AccessPolicyPermission.READ,
                AccessPolicyPermission.WRITE);

        CreateAccessPolicyOptions options = new CreateAccessPolicyOptions().addPermissions(AccessPolicyPermission.READ,
                AccessPolicyPermission.WRITE);

        EnumSet<AccessPolicyPermission> permissions = options.getPermissions();

        assertPermissions(expectedPermissions, permissions);
    }

    @Test
    public void canRemovePermissionsAsEnumSet() throws Exception {
        EnumSet<AccessPolicyPermission> originalPermissions = EnumSet.of(AccessPolicyPermission.READ,
                AccessPolicyPermission.WRITE, AccessPolicyPermission.DELETE);
        EnumSet<AccessPolicyPermission> permissionsToRemove = EnumSet.of(AccessPolicyPermission.READ,
                AccessPolicyPermission.DELETE);
        EnumSet<AccessPolicyPermission> expectedPermissions = EnumSet.of(AccessPolicyPermission.WRITE);

        CreateAccessPolicyOptions options = new CreateAccessPolicyOptions().addPermissions(originalPermissions);

        options.removePermissions(permissionsToRemove);

        EnumSet<AccessPolicyPermission> actualPermissions = options.getPermissions();

        assertPermissions(expectedPermissions, actualPermissions);
    }

    @Test
    public void canRemovePermissionsAsVarargs() throws Exception {
        EnumSet<AccessPolicyPermission> originalPermissions = EnumSet.of(AccessPolicyPermission.READ,
                AccessPolicyPermission.WRITE, AccessPolicyPermission.DELETE);
        EnumSet<AccessPolicyPermission> expectedPermissions = EnumSet.of(AccessPolicyPermission.WRITE);

        CreateAccessPolicyOptions options = new CreateAccessPolicyOptions().addPermissions(originalPermissions);

        options.removePermissions(AccessPolicyPermission.READ, AccessPolicyPermission.DELETE);

        EnumSet<AccessPolicyPermission> actualPermissions = options.getPermissions();

        assertPermissions(expectedPermissions, actualPermissions);
    }
}

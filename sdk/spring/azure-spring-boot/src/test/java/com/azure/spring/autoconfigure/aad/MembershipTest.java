// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MembershipTest {
    private static final Membership GROUP_1 = new Membership("12345", Membership.OBJECT_TYPE_GROUP, "test");

    @Test
    public void getDisplayName() {
        Assertions.assertEquals("test", GROUP_1.getDisplayName());
    }

    @Test
    public void getObjectType() {
        Assertions.assertEquals(Membership.OBJECT_TYPE_GROUP, GROUP_1.getObjectType());
    }

    @Test
    public void getObjectID() {
        Assertions.assertEquals("12345", GROUP_1.getObjectID());
    }

    @Test
    public void equals() {
        final Membership group2 = new Membership("12345", Membership.OBJECT_TYPE_GROUP, "test");
        Assertions.assertEquals(GROUP_1, group2);
    }

    @Test
    public void hashCodeTest() {
        final Membership group2 = new Membership("12345", Membership.OBJECT_TYPE_GROUP, "test");
        Assertions.assertEquals(GROUP_1.hashCode(), group2.hashCode());
    }
}

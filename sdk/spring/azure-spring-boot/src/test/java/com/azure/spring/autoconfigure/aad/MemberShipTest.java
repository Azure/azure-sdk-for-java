// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import org.junit.Assert;
import org.junit.Test;

public class MemberShipTest {
    private static final MemberShip GROUP_1 = new MemberShip("12345", MemberShip.OBJECT_TYPE_GROUP, "test");

    @Test
    public void getDisplayName() {
        Assert.assertEquals("test", GROUP_1.getDisplayName());
    }

    @Test
    public void getObjectType() {
        Assert.assertEquals(MemberShip.OBJECT_TYPE_GROUP, GROUP_1.getObjectType());
    }

    @Test
    public void getObjectID() {
        Assert.assertEquals("12345", GROUP_1.getObjectID());
    }

    @Test
    public void equals() {
        final MemberShip group2 = new MemberShip("12345", MemberShip.OBJECT_TYPE_GROUP, "test");
        Assert.assertEquals(GROUP_1, group2);
    }

    @Test
    public void hashCodeTest() {
        final MemberShip group2 = new MemberShip("12345", MemberShip.OBJECT_TYPE_GROUP, "test");
        Assert.assertEquals(GROUP_1.hashCode(), group2.hashCode());
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.autoconfigure.aad;

import org.junit.Assert;
import org.junit.Test;

public class UserGroupTest {
    private static final UserGroup GROUP_1 = new UserGroup("12345", "Group", "test");

    @Test
    public void getDisplayName() {
        Assert.assertEquals("test", GROUP_1.getDisplayName());
    }

    @Test
    public void getObjectType() {
        Assert.assertEquals("Group", GROUP_1.getObjectType());
    }

    @Test
    public void getObjectID() {
        Assert.assertEquals("12345", GROUP_1.getObjectID());
    }

    @Test
    public void equals() {
        final UserGroup group2 = new UserGroup("12345", "Group", "test");
        Assert.assertEquals(GROUP_1, group2);
    }

    @Test
    public void hashCodeTest() {
        final UserGroup group2 = new UserGroup("12345", "Group", "test");
        Assert.assertEquals(GROUP_1.hashCode(), group2.hashCode());
    }
}

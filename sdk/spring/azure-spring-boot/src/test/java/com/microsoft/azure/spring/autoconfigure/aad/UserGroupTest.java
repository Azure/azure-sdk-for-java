// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.autoconfigure.aad;

import org.junit.Assert;
import org.junit.Test;

public class UserGroupTest {
    private static final UserGroup GROUP_1 = new UserGroup("12345", "test");

    @Test
    public void getDisplayName() {
        Assert.assertTrue(GROUP_1.getDisplayName().equals("test"));
    }

    @Test
    public void getObjectID() {
        Assert.assertTrue(GROUP_1.getObjectID().equals("12345"));
    }

    @Test
    public void equals() {
        final UserGroup group2 = new UserGroup("12345", "test");
        Assert.assertTrue(GROUP_1.equals(group2));
    }

    @Test
    public void hashCodeTest() {
        final UserGroup group2 = new UserGroup("12345", "test");
        Assert.assertTrue(GROUP_1.hashCode() == group2.hashCode());
    }
}

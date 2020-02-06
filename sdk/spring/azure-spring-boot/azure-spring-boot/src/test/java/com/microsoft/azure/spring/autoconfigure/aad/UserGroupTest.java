/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.autoconfigure.aad;

import org.junit.Assert;
import org.junit.Test;

public class UserGroupTest {
    private static final UserGroup group1 = new UserGroup("12345", "test");

    @Test
    public void getDisplayName() throws Exception {
        Assert.assertTrue(group1.getDisplayName().equals("test"));
    }

    @Test
    public void getObjectID() throws Exception {
        Assert.assertTrue(group1.getObjectID().equals("12345"));
    }

    @Test
    public void equals() throws Exception {
        final UserGroup group2 = new UserGroup("12345", "test");
        Assert.assertTrue(group1.equals(group2));
    }

    @Test
    public void hashCodeTest() {
        final UserGroup group2 = new UserGroup("12345", "test");
        Assert.assertTrue(group1.hashCode() == group2.hashCode());
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.util.GeneralUtils;
import org.junit.Assert;
import org.junit.Test;

public class ImplUtilsTests {
    @Test
    public void findFirstOfTypeEmptyArgs() {
        Assert.assertNull(GeneralUtils.findFirstOfType(null, Integer.class));
    }

    @Test
    public void findFirstOfTypeWithOneOfType() {
        int expected = 1;
        Object[] args = { "string", expected };
        int actual = GeneralUtils.findFirstOfType(args, Integer.class);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void findFirstOfTypeWithMultipleOfType() {
        int expected = 1;
        Object[] args = { "string", expected, 10 };
        int actual = GeneralUtils.findFirstOfType(args, Integer.class);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void findFirstOfTypeWithNoneOfType() {
        Object[] args = { "string", "anotherString" };
        Assert.assertNull(GeneralUtils.findFirstOfType(args, Integer.class));
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice;

import java.util.Collection;

import com.azure.resourcemanager.appservice.models.RuntimeStack;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UtilsTests {

    @Test
    public void testAttributeCollection() throws Exception {
        Collection<RuntimeStack> runtimeStacks = RuntimeStack.getAll();
        int count = runtimeStacks.size();
        Assertions.assertTrue(count > 30); // a rough count

        RuntimeStack newRuntimeStack = new RuntimeStack("stack", "version"); // new, but not count as pre-defined

        runtimeStacks = RuntimeStack.getAll();
        Assertions.assertEquals(count, runtimeStacks.size());
    }
}

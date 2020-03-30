package com.azure.management.appservice;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;

public class UtilsTests {

    @Test
    public void testAttributeCollection() throws Exception {
        Collection<RuntimeStack> runtimeStacks = RuntimeStack.getAll();
        int count = runtimeStacks.size();
        Assertions.assertTrue(count > 30);      // a rough count

        RuntimeStack newRuntimeStack = new RuntimeStack("stack", "version");    // new, but not count as pre-defined

        runtimeStacks = RuntimeStack.getAll();
        Assertions.assertEquals(count, runtimeStacks.size());
    }
}

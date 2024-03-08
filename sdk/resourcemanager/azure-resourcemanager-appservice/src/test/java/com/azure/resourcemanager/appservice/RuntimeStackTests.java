// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice;

import com.azure.resourcemanager.appservice.models.RuntimeStack;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;

public class RuntimeStackTests {

    @Test
    public void verifyDeprecatedNotInGetAll() {
        Collection<RuntimeStack> stacks = RuntimeStack.getAll();
        Assertions.assertTrue(stacks.contains(RuntimeStack.TOMCAT_10_0_JAVA11));
        Assertions.assertFalse(stacks.contains(RuntimeStack.TOMCAT_10_0_JRE11));
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.compatibility;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ClassNameResolverPredicateTest {

    @Test
    void testResolve() {
        ClassNameResolverPredicate predicate = new ClassNameResolverPredicate();
        Assertions.assertTrue(predicate.resolve("com.azure.spring.cloud.autoconfigure.implementation.compatibility.ClassNameResolverPredicateTest"));
        Assertions.assertTrue(predicate.resolve("com.azure.spring.cloud.autoconfigure.implementation.compatibility.ClassNameResolverPredicateTest,testMethod"));
        Assertions.assertTrue(predicate.resolve("com.azure.spring.cloud.autoconfigure.implementation.compatibility.ClassNameResolverPredicateTest,testMethod2,boolean"));
        Assertions.assertTrue(predicate.resolve("com.azure.spring.cloud.autoconfigure.implementation.compatibility.ClassNameResolverPredicateTest,testMethod3,boolean,java.lang.String"));
        Assertions.assertFalse(predicate.resolve("com.azure.spring.cloud.autoconfigure.implementation.compatibility.ClassNameResolverPredicateTest,testMethod4"));
    }

    public void testMethod() {}
    public void testMethod2(boolean b) {}
    public void testMethod3(boolean b, String a) {}
}

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.dag;

import org.junit.Test;

public class ExecutableWithCreatableTests {
    @Test
    public void testExecutableWithExecutableDependency() {
        BreadSliceImpl breadFetcher1 = new BreadSliceImpl("BreadSlice1");
        BreadSliceImpl breadFetcher2 = new BreadSliceImpl("BreadSlice2");
        breadFetcher1.withAnotherSliceFromStore(breadFetcher2)
                .execute();
    }

    @Test
    public void testExecutableWithCreatableDependency() {
        BreadSliceImpl breadFetcher = new BreadSliceImpl("BreadSlice");
        OrderImpl order = new OrderImpl("OrderForSlice", new OrderInner());
        breadFetcher.withNewOrder(order)
                .execute();
    }

    @Test
    public void testCreatableWithExecutableDependency() {
        SandwichImpl sandwich = new SandwichImpl("Sandwich", new SandwichInner());
        BreadSliceImpl breadFetcher = new BreadSliceImpl("SliceForSandwich");
        OrderImpl order = new OrderImpl("OrderForSlice", new OrderInner());
        breadFetcher.withNewOrder(order);
        sandwich.withBreadSliceFromStore(breadFetcher)
                .create();
    }
}
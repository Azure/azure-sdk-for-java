// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.dag;

import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Executable;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;

/**
 * Represents a type when executed returns a bread from the store.
 */
public interface IBreadSlice extends Indexable, Executable<IBreadSlice> {
    IBreadSlice withAnotherSliceFromStore(Executable<IBreadSlice> breadFetcher);
    IBreadSlice withNewOrder(Creatable<IOrder> order);
}

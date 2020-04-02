/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources.fluentcore.dag;

import com.azure.management.resources.fluentcore.model.Creatable;
import com.azure.management.resources.fluentcore.model.Executable;
import com.azure.management.resources.fluentcore.model.Indexable;

/**
 * Represents a type when executed returns a bread from the store.
 */
public interface IBreadSlice extends Indexable, Executable<IBreadSlice> {
    IBreadSlice withAnotherSliceFromStore(Executable<IBreadSlice> breadFetcher);
    IBreadSlice withNewOrder(Creatable<IOrder> order);
}
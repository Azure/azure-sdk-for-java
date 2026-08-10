// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.dag;

import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Executable;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;

/**
 * Creatable and Indexable sandwich.
 */
public interface ISandwich extends Indexable, Creatable<ISandwich> {
    ISandwich withBreadSliceFromStore(Executable<IBreadSlice> breadFetcher);
}

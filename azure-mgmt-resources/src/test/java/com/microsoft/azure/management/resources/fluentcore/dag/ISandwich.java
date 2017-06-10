/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.dag;

import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Executable;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;

/**
 * Creatable and Indexable sandwich.
 */
public interface ISandwich extends Indexable, Creatable<ISandwich> {
    ISandwich withBreadSliceFromStore(Executable<IBreadSlice> breadFetcher);
}
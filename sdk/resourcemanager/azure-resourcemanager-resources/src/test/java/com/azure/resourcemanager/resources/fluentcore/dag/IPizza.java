// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.dag;

import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;

/**
 * Creatable and Indexable pizza.
 */
interface IPizza extends Indexable, Creatable<IPizza> {
    IPizza withInstantPizza(Creatable<IPizza> anotherPizza);
    IPizza withDelayedPizza(Creatable<IPizza> anotherPizza);
}


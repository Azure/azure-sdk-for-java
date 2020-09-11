// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.dag;

import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;

/**
 * Creatable and Indexable pasta.
 */
interface IPasta extends Indexable, Creatable<IPasta> {
    IPasta withInstantPasta(Creatable<IPasta> anotherPasta);
    IPasta withDelayedPasta(Creatable<IPasta> anotherPasta);
}


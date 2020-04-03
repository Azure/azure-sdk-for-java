// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.resources.fluentcore.dag;

import com.azure.management.resources.fluentcore.model.Creatable;
import com.azure.management.resources.fluentcore.model.Indexable;

/**
 * Creatable and Indexable pancake.
 */
interface IPancake extends Indexable, Creatable<IPancake> {
    IPancake withInstantPancake(Creatable<IPancake> anotherPancake);
    IPancake withDelayedPancake(Creatable<IPancake> anotherPancake);
}


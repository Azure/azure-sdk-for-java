// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.dag;

import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;

/**
 * Type represents a order that is to be created prior to fetching a bread slice from store.
 */
public interface IOrder extends Indexable, Creatable<IOrder> {
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.model;

/**
 * Base interface for all models that can be indexed by a key.
 */
public interface Indexable {
    /**
     * @return the index key.
     */
    String key();
}

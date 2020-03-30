/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources.fluentcore.model;

/**
 * Base interface for all models that can be indexed by a key.
 */
public interface Indexable {
    /**
     * @return the index key.
     */
    String key();
}

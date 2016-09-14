/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.model;

import com.microsoft.azure.management.apigeneration.LangDefinition;

/**
 * Base interface for all models that can be indexed by a key.
 */
@LangDefinition(ContainerName = "ResourceActions", CreateAsyncMultiThreadMethodParam = true)
public interface Indexable {
    /**
     * @return the index key.
     */
    String key();
}

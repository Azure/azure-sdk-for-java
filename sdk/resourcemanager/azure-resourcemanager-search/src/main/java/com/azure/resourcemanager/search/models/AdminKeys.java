// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.search.models;

import com.azure.core.annotation.Fluent;

/**
 * Response containing the primary and secondary admin API keys for a given Azure Cognitive Search service.
 */
@Fluent
public interface AdminKeys {
    /**
     * Get the primaryKey value.
     *
     * @return the primaryKey value
     */
    String primaryKey();

    /**
     * Get the secondaryKey value.
     *
     * @return the secondaryKey value
     */
    String secondaryKey();
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.modelsrepository.implementation.models;

/**
 * {@link RepositoryFeatures} is designated to store
 * attributes which apply to all models in the repository
 */
public class RepositoryFeatures {
    private final boolean expanded;
    private final boolean index;

    public RepositoryFeatures(boolean expanded, boolean index) {
        this.expanded = expanded;
        this.index = index;
    }

    public RepositoryFeatures() {
        this.expanded = false;
        this.index = false;
    }

    public boolean isIndex() {
        return index;
    }

    public boolean isExpanded() {
        return expanded;
    }
}

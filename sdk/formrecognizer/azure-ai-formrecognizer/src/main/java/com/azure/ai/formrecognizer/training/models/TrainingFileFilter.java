// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.training.models;

import com.azure.core.annotation.Fluent;

/**
 * The TrainingFileFilter model.
 */
@Fluent
public final class TrainingFileFilter {

    /*
     * A case-sensitive prefix string to filter documents in the source path
     * for training.
     */
    private String prefix;

    /*
     * A flag to indicate if sub folders within the set of prefix folders will
     * also need to be included when searching for content to be preprocessed.
     */
    private boolean includeSubfolders;

    /**
     * Get the case-sensitive prefix string to filter
     * documents in the source path for training.
     *
     * @return the case-sensitive prefix string to filter documents for training.
     */
    public String getPrefix() {
        return this.prefix;
    }

    /**
     * Set the case-sensitive prefix string to filter documents in the source path for training.
     *
     * @param prefix the prefix value to set.
     *
     * @return the TrainingFileFilter object itself.
     */
    public TrainingFileFilter setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * Get the flag to indicate if sub folders within the set of prefix folders will also need to be included when
     * searching for content to be preprocessed.
     * Not supported if training with labels.
     *
     * @return the includeSubfolders value.
     */
    public Boolean isSubfoldersIncluded() {
        return this.includeSubfolders;
    }

    /**
     * Set the includeSubfolders flag to indicate if sub folders are also to be included when
     * searching for content to be preprocessed.
     * Not supported if training with labels.
     *
     * @param includeSubfolders the includeSubfolders value to set.
     *
     * @return the TrainingFileFilter object itself.
     */
    public TrainingFileFilter setSubfoldersIncluded(boolean includeSubfolders) {
        this.includeSubfolders = includeSubfolders;
        return this;
    }
}

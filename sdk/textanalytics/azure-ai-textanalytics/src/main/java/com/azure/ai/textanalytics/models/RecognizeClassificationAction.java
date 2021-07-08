// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

/**
 *
 */
@Fluent
public final class RecognizeClassificationAction {
    String projectName;
    String modelName;

    /**
     *
     * @return
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     *
     * @param projectName
     * @return
     */
    public RecognizeClassificationAction setProjectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    /**
     *
     * @return
     */
    public String getModelName() {
        return modelName;
    }

    /**
     *
     * @param modelName
     * @return
     */
    public RecognizeClassificationAction setModelName(String modelName) {
        this.modelName = modelName;
        return this;
    }

}

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.containerregistry.implementation;

import com.azure.management.containerregistry.BaseImageDependency;
import com.azure.management.containerregistry.RegistryTaskStep;
import com.azure.management.containerregistry.TaskStepProperties;

import java.util.List;

abstract class RegistryTaskStepImpl implements RegistryTaskStep {
    private final TaskStepProperties taskStepProperties;

    RegistryTaskStepImpl(TaskStepProperties taskStepProperties) {
        this.taskStepProperties = taskStepProperties;
    }

    @Override
    public List<BaseImageDependency> baseImageDependencies() {
        return taskStepProperties.baseImageDependencies();
    }

    @Override
    public String contextPath() {
        return taskStepProperties.contextPath();
    }
}

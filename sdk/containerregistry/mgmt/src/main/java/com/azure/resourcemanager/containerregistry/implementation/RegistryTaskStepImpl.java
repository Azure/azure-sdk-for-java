// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerregistry.implementation;

import com.azure.resourcemanager.containerregistry.BaseImageDependency;
import com.azure.resourcemanager.containerregistry.RegistryTaskStep;
import com.azure.resourcemanager.containerregistry.TaskStepProperties;
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

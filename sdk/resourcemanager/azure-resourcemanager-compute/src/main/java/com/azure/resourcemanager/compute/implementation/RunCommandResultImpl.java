// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.compute.implementation;

import com.azure.resourcemanager.compute.models.InstanceViewStatus;
import com.azure.resourcemanager.compute.models.RunCommandResult;
import com.azure.resourcemanager.compute.fluent.inner.RunCommandResultInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import java.util.List;

/** The implementation of ComputeUsage. */
class RunCommandResultImpl extends WrapperImpl<RunCommandResultInner> implements RunCommandResult {
    RunCommandResultImpl(RunCommandResultInner innerObject) {
        super(innerObject);
    }

    /**
     * Get run command operation response.
     *
     * @return the value value
     */
    @Override
    public List<InstanceViewStatus> value() {
        return inner().value();
    }
}

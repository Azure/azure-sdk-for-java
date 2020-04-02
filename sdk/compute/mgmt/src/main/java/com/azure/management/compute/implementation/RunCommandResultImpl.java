/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.compute.implementation;

import com.azure.management.compute.InstanceViewStatus;
import com.azure.management.compute.RunCommandResult;
import com.azure.management.compute.models.RunCommandResultInner;
import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

import java.util.List;

/**
 * The implementation of ComputeUsage.
 */
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


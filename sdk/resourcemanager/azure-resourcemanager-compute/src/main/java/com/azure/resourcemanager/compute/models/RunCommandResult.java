// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.compute.fluent.inner.RunCommandResultInner;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import java.util.List;

/** Type representing sku for an Azure compute resource. */
@Fluent
public interface RunCommandResult extends HasInner<RunCommandResultInner> {
    /**
     * Get run command operation response.
     *
     * @return the value value
     */
    List<InstanceViewStatus> value();
}

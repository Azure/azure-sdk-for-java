// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.compute.fluent.models.RunCommandResultInner;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import java.util.List;

/** Type representing sku for an Azure compute resource. */
@Fluent
public interface RunCommandResult extends HasInnerModel<RunCommandResultInner> {
    /**
     * Get run command operation response.
     *
     * @return the value value
     */
    List<InstanceViewStatus> value();
}

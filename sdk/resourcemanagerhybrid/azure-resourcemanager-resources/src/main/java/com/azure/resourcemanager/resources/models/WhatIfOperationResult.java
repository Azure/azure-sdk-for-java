// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.management.exception.ManagementError;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluent.models.WhatIfOperationResultInner;

import java.util.List;

/**
 * An immutable client-side representation of an Azure deployment What-if operation result.
 */
@Fluent
public interface WhatIfOperationResult extends
        HasInnerModel<WhatIfOperationResultInner> {

    /**
     * @return status of the What-If operation.
     */
    String status();

    /**
     * @return list of resource changes predicted by What-If operation.
     */
    List<WhatIfChange> changes();

    /**
     * @return error when What-If operation fails.
     */
    ManagementError error();
}

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.compute;

import com.azure.core.annotation.Fluent;
import com.azure.management.compute.models.RunCommandResultInner;
import com.azure.management.resources.fluentcore.model.HasInner;

import java.util.List;

/**
 * Type representing sku for an Azure compute resource.
 */
@Fluent
public interface RunCommandResult extends HasInner<RunCommandResultInner> {
    /**
     * Get run command operation response.
     *
     * @return the value value
     */
    List<InstanceViewStatus> value();
}

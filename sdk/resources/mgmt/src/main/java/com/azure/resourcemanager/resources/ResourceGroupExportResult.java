// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.models.ResourceGroupExportResultInner;

/**
 * An immutable client-side representation of an Azure deployment template export result.
 */
@Fluent
public interface ResourceGroupExportResult extends
        HasInner<ResourceGroupExportResultInner> {

    /**
     * @return the template content
     */
    Object template();

    /**
     * @return the template content as a JSON string
     */
    String templateJson();

    /**
     * @return the error, if any.
     */
    ErrorResponse error();
}

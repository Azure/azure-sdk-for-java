/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.resources.implementation.DeploymentExportResultInner;

/**
 * An immutable client-side representation of an Azure deployment template export result.
 */
@Fluent
public interface DeploymentExportResult extends
        Wrapper<DeploymentExportResultInner> {

    /**
     * @return the template content
     */
    Object template();

    /**
     * @return the template content as a JSON string
     */
    String templateAsJson();
}

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.management.resources.DeploymentExportResult;
import com.microsoft.azure.management.resources.ResourceGroupExportResult;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.microsoft.azure.management.resources.implementation.api.ResourceGroupExportResultInner;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementErrorWithDetails;

/**
 * Implementation for {@link DeploymentExportResult}.
 */
final class ResourceGroupExportResultImpl extends
        WrapperImpl<ResourceGroupExportResultInner>
        implements
        ResourceGroupExportResult {

    private ObjectMapper mapper;

    ResourceGroupExportResultImpl(ResourceGroupExportResultInner innerModel) {
        super(innerModel);
        mapper = new ObjectMapper();
    }

    @Override
    public Object template() {
        return inner().template();
    }

    @Override
    public String templateJson() {
        try {
            return mapper.writeValueAsString(template());
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    @Override
    public ResourceManagementErrorWithDetails error() {
        return inner().error();
    }
}

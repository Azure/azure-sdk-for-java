// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.implementation;

import com.azure.core.management.exception.ManagementError;
import com.azure.resourcemanager.resources.models.DeploymentExportResult;
import com.azure.resourcemanager.resources.models.ResourceGroupExportResult;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.resources.fluent.models.ResourceGroupExportResultInner;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
        return innerModel().template();
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
    public ManagementError error() {
        return innerModel().error();
    }
}

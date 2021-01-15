// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.implementation;

import com.azure.resourcemanager.resources.models.DeploymentExportResult;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.resources.fluent.models.DeploymentExportResultInner;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Implementation for {@link DeploymentExportResult}.
 */
final class DeploymentExportResultImpl extends
        WrapperImpl<DeploymentExportResultInner>
        implements
        DeploymentExportResult {

    private ObjectMapper mapper;

    DeploymentExportResultImpl(DeploymentExportResultInner innerModel) {
        super(innerModel);
        mapper = new ObjectMapper();
    }

    @Override
    public Object template() {
        return innerModel().template();
    }

    @Override
    public String templateAsJson() {
        try {
            return mapper.writeValueAsString(template());
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources.implementation;

import com.azure.management.resources.DeploymentExportResult;
import com.azure.management.resources.ErrorResponse;
import com.azure.management.resources.ResourceGroupExportResult;
import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.management.resources.models.ResourceGroupExportResultInner;
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
        return inner().getTemplate();
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
    public ErrorResponse error() {
        return inner().getError();
    }
}

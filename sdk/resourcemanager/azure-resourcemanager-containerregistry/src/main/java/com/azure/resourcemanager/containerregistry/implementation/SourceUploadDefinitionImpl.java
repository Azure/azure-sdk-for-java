// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.containerregistry.implementation;

import com.azure.resourcemanager.containerregistry.models.SourceUploadDefinition;
import com.azure.resourcemanager.containerregistry.fluent.models.SourceUploadDefinitionInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;

/** Implementation for SourceUploadDefinition. */
public class SourceUploadDefinitionImpl extends WrapperImpl<SourceUploadDefinitionInner>
    implements SourceUploadDefinition {

    /**
     * Creates an instance of the SourceUploadDefinition object.
     *
     * @param innerObject the inner object
     */
    SourceUploadDefinitionImpl(SourceUploadDefinitionInner innerObject) {
        super(innerObject);
    }

    @Override
    public String uploadUrl() {
        return this.innerModel().uploadUrl();
    }

    @Override
    public String relativePath() {
        return this.innerModel().relativePath();
    }
}

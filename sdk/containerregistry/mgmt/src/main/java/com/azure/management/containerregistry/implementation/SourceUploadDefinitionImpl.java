/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.containerregistry.implementation;

import com.azure.management.containerregistry.SourceUploadDefinition;
import com.azure.management.containerregistry.models.SourceUploadDefinitionInner;
import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

/**
 * Implementation for SourceUploadDefinition.
 */
public class SourceUploadDefinitionImpl
    extends WrapperImpl<SourceUploadDefinitionInner>
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
        return this.inner().uploadUrl();
    }

    @Override
    public String relativePath() {
        return this.inner().relativePath();
    }
}

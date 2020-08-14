// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.containerregistry.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.containerregistry.fluent.inner.SourceUploadDefinitionInner;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;

/** The source repository properties for a build task. */
@Fluent
public interface SourceUploadDefinition extends HasInner<SourceUploadDefinitionInner> {
    /** @return the URL where the client can upload the source */
    String uploadUrl();

    /** @return the the relative path to the source; this is used to submit the subsequent queue build request */
    String relativePath();
}

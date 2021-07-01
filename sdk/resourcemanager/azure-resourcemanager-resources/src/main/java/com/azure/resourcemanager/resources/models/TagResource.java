// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.models;

import com.azure.resourcemanager.resources.fluent.models.TagsResourceInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;

import java.util.Map;

/**
 * An immutable client-side representation of an Azure resource with tags.
 */
public interface TagResource extends
    HasId, HasName, HasInnerModel<TagsResourceInner> {

    /**
     * @return the type of the resource
     */
    String type();

    /**
     * @return the tags for the resource
     */
    Map<String, String> tags();
}

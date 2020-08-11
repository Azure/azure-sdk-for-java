// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.cosmos.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.cosmos.fluent.inner.PrivateLinkResourceInner;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import java.util.List;

/** A private link resource. */
@Fluent
public interface PrivateLinkResource extends HasInner<PrivateLinkResourceInner> {

    /**
     * Get the id value.
     *
     * @return the id value
     */
    String id();

    /**
     * Get the name value.
     *
     * @return the name value
     */
    String name();

    /**
     * Get the type value.
     *
     * @return the type value
     */
    String type();

    /**
     * Get the private link resource group id.
     *
     * @return the groupId value
     */
    String groupId();

    /**
     * Get the private link resource required member names.
     *
     * @return the requiredMembers value
     */
    List<String> requiredMembers();
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.resources.fluentcore.arm.models;

/**
 * An interface representing a child that has an immediately available parent.
 *
 * @param <ParentT> the parent type
 */
public interface HasParent<ParentT> {
    /**
     * @return the parent of this child object
     */
    ParentT parent();
}

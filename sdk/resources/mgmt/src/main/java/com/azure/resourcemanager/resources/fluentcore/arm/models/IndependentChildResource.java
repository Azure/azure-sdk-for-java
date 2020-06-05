// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.arm.models;

import com.azure.core.annotation.Fluent;

/**
 * Interface for the child resource which can be CRUDed independently from the parent resource.
 *
 * @param <ManagerT> the client manager type representing the service
 * @param <InnerT> the inner, auto-generated implementation logic object type
 */
@Fluent
public interface IndependentChildResource<ManagerT, InnerT> extends
        GroupableResource<ManagerT, InnerT>,
        IndependentChild<ManagerT> {
}

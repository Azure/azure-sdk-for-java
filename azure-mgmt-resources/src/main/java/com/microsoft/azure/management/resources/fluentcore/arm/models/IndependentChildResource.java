/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm.models;

import com.microsoft.azure.management.apigeneration.Fluent;

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

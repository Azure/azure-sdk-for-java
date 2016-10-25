/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.PolicyAssignment;
import com.microsoft.azure.management.resources.PolicyAssignments;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.CreatableWrappersImpl;
import rx.Observable;
import rx.functions.Func1;

/**
 * The implementation for {@link ResourceGroups} and its parent interfaces.
 */
final class PolicyAssignmentsImpl
        extends CreatableWrappersImpl<PolicyAssignment, PolicyAssignmentImpl, PolicyAssignmentInner>
        implements PolicyAssignments {
    private final PolicyAssignmentsInner client;

    /**
     * Creates an instance of the implementation.
     *
     * @param innerClient the inner policies client
     */
    PolicyAssignmentsImpl(final PolicyAssignmentsInner innerClient) {
        this.client = innerClient;
    }

    @Override
    public PagedList<PolicyAssignment> list() {
        return wrapList(client.list());
    }

    @Override
    public Observable<Void> deleteByIdAsync(String id) {
        return client.deleteByIdAsync(id).map(new Func1<PolicyAssignmentInner, Void>() {
            @Override
            public Void call(PolicyAssignmentInner policyAssignmentInner) {
                return null;
            }
        });
    }

    @Override
    public PolicyAssignmentImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    protected PolicyAssignmentImpl wrapModel(String name) {
        return new PolicyAssignmentImpl(
                new PolicyAssignmentInner().withName(name).withDisplayName(name),
                client);
    }

    @Override
    protected PolicyAssignmentImpl wrapModel(PolicyAssignmentInner inner) {
        return new PolicyAssignmentImpl(inner, client);
    }

    @Override
    public PagedList<PolicyAssignment> listByResource(String resourceId) {
        return wrapList(client.listForResource(
                ResourceUtils.groupFromResourceId(resourceId),
                ResourceUtils.resourceProviderFromResourceId(resourceId),
                ResourceUtils.relativePathFromResourceId(ResourceUtils.parentResourceIdFromResourceId(resourceId)),
                ResourceUtils.resourceTypeFromResourceId(resourceId),
                ResourceUtils.nameFromResourceId(resourceId)
        ));
    }

    @Override
    public PolicyAssignment getById(String id) {
        return wrapModel(client.getById(id));
    }

    @Override
    public PagedList<PolicyAssignment> listByGroup(String resourceGroupName) {
        return wrapList(client.listForResourceGroup(resourceGroupName));
    }
}

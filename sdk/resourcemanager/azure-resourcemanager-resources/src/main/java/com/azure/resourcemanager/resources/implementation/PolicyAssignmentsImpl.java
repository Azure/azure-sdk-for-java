// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.resources.models.PolicyAssignment;
import com.azure.resourcemanager.resources.models.PolicyAssignments;
import com.azure.resourcemanager.resources.models.ResourceGroups;
import com.azure.resourcemanager.resources.fluent.inner.PolicyAssignmentInner;
import com.azure.resourcemanager.resources.fluent.PolicyAssignmentsClient;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.CreatableWrappersImpl;
import reactor.core.publisher.Mono;

/**
 * The implementation for {@link ResourceGroups} and its parent interfaces.
 */
public final class PolicyAssignmentsImpl
        extends CreatableWrappersImpl<PolicyAssignment, PolicyAssignmentImpl, PolicyAssignmentInner>
        implements PolicyAssignments {
    private final PolicyAssignmentsClient client;

    /**
     * Creates an instance of the implementation.
     *
     * @param innerClient the inner policies client
     */
    public PolicyAssignmentsImpl(final PolicyAssignmentsClient innerClient) {
        this.client = innerClient;
    }

    @Override
    public PagedIterable<PolicyAssignment> list() {
        return wrapList(client.list());
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id) {
        return client.deleteByIdAsync(id).then();
    }

    @Override
    public PolicyAssignmentImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    protected PolicyAssignmentImpl wrapModel(String name) {
        return new PolicyAssignmentImpl(name,
                new PolicyAssignmentInner().withDisplayName(name),
                client);
    }

    @Override
    protected PolicyAssignmentImpl wrapModel(PolicyAssignmentInner inner) {
        if (inner == null) {
            return null;
        }
        return new PolicyAssignmentImpl(inner.name(), inner, client);
    }

    @Override
    public PagedIterable<PolicyAssignment> listByResource(String resourceId) {
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
        return getByIdAsync(id).block();
    }

    @Override
    public Mono<PolicyAssignment> getByIdAsync(String id) {
        return client.getByIdAsync(id)
                .map(this::wrapModel);
    }

    @Override
    public PagedIterable<PolicyAssignment> listByResourceGroup(String resourceGroupName) {
        return wrapList(client.listByResourceGroup(resourceGroupName));
    }

    @Override
    public PagedFlux<PolicyAssignment> listAsync() {
        return wrapPageAsync(this.client.listAsync());
    }

    @Override
    public PagedFlux<PolicyAssignment> listByResourceGroupAsync(String resourceGroupName) {
        return wrapPageAsync(this.client.listByResourceGroupAsync(resourceGroupName));
    }
}

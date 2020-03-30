/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.resources.PolicyAssignment;
import com.azure.management.resources.PolicyAssignments;
import com.azure.management.resources.ResourceGroups;
import com.azure.management.resources.models.PolicyAssignmentInner;
import com.azure.management.resources.models.PolicyAssignmentsInner;
import com.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.azure.management.resources.fluentcore.arm.collection.implementation.CreatableWrappersImpl;
import reactor.core.publisher.Mono;

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
        return new PolicyAssignmentImpl(inner.getName(), inner, client);
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
                .onErrorResume(e -> Mono.empty())
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

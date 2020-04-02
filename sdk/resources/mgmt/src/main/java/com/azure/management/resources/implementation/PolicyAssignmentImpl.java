/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources.implementation;

import com.azure.management.resources.GenericResource;
import com.azure.management.resources.PolicyAssignment;
import com.azure.management.resources.PolicyDefinition;
import com.azure.management.resources.ResourceGroup;
import com.azure.management.resources.fluentcore.model.implementation.CreatableImpl;
import com.azure.management.resources.models.PolicyAssignmentInner;
import com.azure.management.resources.models.PolicyAssignmentsInner;
import reactor.core.publisher.Mono;

/**
 * Implementation for {@link PolicyAssignment}.
 */
final class PolicyAssignmentImpl extends
        CreatableImpl<PolicyAssignment, PolicyAssignmentInner, PolicyAssignmentImpl>
        implements
        PolicyAssignment,
        PolicyAssignment.Definition {
    private final PolicyAssignmentsInner innerCollection;

    PolicyAssignmentImpl(String name, PolicyAssignmentInner innerModel, PolicyAssignmentsInner innerCollection) {
        super(name, innerModel);
        this.innerCollection = innerCollection;
    }

    @Override
    public String displayName() {
        return inner().displayName();
    }

    @Override
    public String policyDefinitionId() {
        return inner().policyDefinitionId();
    }

    @Override
    public String scope() {
        return inner().scope();
    }

    @Override
    public String id() {
        return inner().getId();
    }

    @Override
    public String type() {
        return inner().getType();
    }

    @Override
    public PolicyAssignmentImpl withDisplayName(String displayName) {
        inner().withDisplayName(displayName);
        return this;
    }

    @Override
    public PolicyAssignmentImpl forScope(String scope) {
        inner().withScope(scope);
        return this;
    }

    @Override
    public PolicyAssignmentImpl forResourceGroup(ResourceGroup resourceGroup) {
        inner().withScope(resourceGroup.id());
        return this;
    }

    public PolicyAssignmentImpl forResource(GenericResource genericResource) {
        inner().withScope(genericResource.id());
        return this;
    }

    @Override
    public PolicyAssignmentImpl withPolicyDefinitionId(String policyDefinitionId) {
        inner().withPolicyDefinitionId(policyDefinitionId);
        return this;
    }

    @Override
    public PolicyAssignmentImpl withPolicyDefinition(PolicyDefinition policyDefinition) {
        inner().withPolicyDefinitionId(policyDefinition.id());
        return this;
    }

    @Override
    public Mono<PolicyAssignment> createResourceAsync() {
        return innerCollection.createAsync(inner().scope(), name(), inner())
                .map(innerToFluentMap(this));
    }

    @Override
    public boolean isInCreateMode() {
        return id() == null;
    }

    @Override
    protected Mono<PolicyAssignmentInner> getInnerAsync() {
        return innerCollection.getAsync(inner().scope(), name());
    }
}

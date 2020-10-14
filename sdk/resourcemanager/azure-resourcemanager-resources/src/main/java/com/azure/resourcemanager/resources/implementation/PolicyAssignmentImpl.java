// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.implementation;

import com.azure.resourcemanager.resources.models.GenericResource;
import com.azure.resourcemanager.resources.models.PolicyAssignment;
import com.azure.resourcemanager.resources.models.PolicyDefinition;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.CreatableImpl;
import com.azure.resourcemanager.resources.fluent.models.PolicyAssignmentInner;
import com.azure.resourcemanager.resources.fluent.PolicyAssignmentsClient;
import reactor.core.publisher.Mono;

/**
 * Implementation for {@link PolicyAssignment}.
 */
final class PolicyAssignmentImpl extends
        CreatableImpl<PolicyAssignment, PolicyAssignmentInner, PolicyAssignmentImpl>
        implements
        PolicyAssignment,
        PolicyAssignment.Definition {
    private final PolicyAssignmentsClient innerCollection;

    PolicyAssignmentImpl(String name, PolicyAssignmentInner innerModel, PolicyAssignmentsClient innerCollection) {
        super(name, innerModel);
        this.innerCollection = innerCollection;
    }

    @Override
    public String displayName() {
        return innerModel().displayName();
    }

    @Override
    public String policyDefinitionId() {
        return innerModel().policyDefinitionId();
    }

    @Override
    public String scope() {
        return innerModel().scope();
    }

    @Override
    public String id() {
        return innerModel().id();
    }

    @Override
    public String type() {
        return innerModel().type();
    }

    @Override
    public PolicyAssignmentImpl withDisplayName(String displayName) {
        innerModel().withDisplayName(displayName);
        return this;
    }

    @Override
    public PolicyAssignmentImpl forScope(String scope) {
        innerModel().withScope(scope);
        return this;
    }

    @Override
    public PolicyAssignmentImpl forResourceGroup(ResourceGroup resourceGroup) {
        innerModel().withScope(resourceGroup.id());
        return this;
    }

    public PolicyAssignmentImpl forResource(GenericResource genericResource) {
        innerModel().withScope(genericResource.id());
        return this;
    }

    @Override
    public PolicyAssignmentImpl withPolicyDefinitionId(String policyDefinitionId) {
        innerModel().withPolicyDefinitionId(policyDefinitionId);
        return this;
    }

    @Override
    public PolicyAssignmentImpl withPolicyDefinition(PolicyDefinition policyDefinition) {
        innerModel().withPolicyDefinitionId(policyDefinition.id());
        return this;
    }

    @Override
    public Mono<PolicyAssignment> createResourceAsync() {
        return innerCollection.createAsync(innerModel().scope(), name(), innerModel())
                .map(innerToFluentMap(this));
    }

    @Override
    public boolean isInCreateMode() {
        return id() == null;
    }

    @Override
    protected Mono<PolicyAssignmentInner> getInnerAsync() {
        return innerCollection.getAsync(innerModel().scope(), name());
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.implementation;

import com.azure.resourcemanager.resources.models.EnforcementMode;
import com.azure.resourcemanager.resources.models.GenericResource;
import com.azure.resourcemanager.resources.models.ParameterValuesValue;
import com.azure.resourcemanager.resources.models.PolicyAssignment;
import com.azure.resourcemanager.resources.models.PolicyDefinition;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.CreatableImpl;
import com.azure.resourcemanager.resources.fluent.models.PolicyAssignmentInner;
import com.azure.resourcemanager.resources.fluent.PolicyAssignmentsClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Implementation for {@link PolicyAssignment}.
 */
final class PolicyAssignmentImpl extends
        CreatableImpl<PolicyAssignment, PolicyAssignmentInner, PolicyAssignmentImpl>
        implements
        PolicyAssignment,
        PolicyAssignment.Definition {
    private final PolicyAssignmentsClient innerCollection;

    private String scope;

    PolicyAssignmentImpl(String name, PolicyAssignmentInner innerModel, PolicyAssignmentsClient innerCollection) {
        super(name, innerModel);
        this.innerCollection = innerCollection;
        this.scope = innerModel.scope();
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
    public List<String> excludedScopes() {
        return innerModel().notScopes() == null
            ? Collections.emptyList()
            : Collections.unmodifiableList(innerModel().notScopes());
    }

    @Override
    public EnforcementMode enforcementMode() {
        return innerModel().enforcementMode();
    }

    @Override
    public Map<String, ParameterValuesValue> parameters() {
        return innerModel().parameters() == null
            ? Collections.emptyMap()
            : Collections.unmodifiableMap(innerModel().parameters());
    }

    @Override
    public PolicyAssignmentImpl withDisplayName(String displayName) {
        innerModel().withDisplayName(displayName);
        return this;
    }

    @Override
    public PolicyAssignmentImpl forScope(String scope) {
        this.scope = scope;
        return this;
    }

    @Override
    public PolicyAssignmentImpl forResourceGroup(ResourceGroup resourceGroup) {
        this.scope = resourceGroup.id();
        return this;
    }

    public PolicyAssignmentImpl forResource(GenericResource genericResource) {
        this.scope = genericResource.id();
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
        return innerCollection.createAsync(this.scope, name(), innerModel())
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

    @Override
    public PolicyAssignmentImpl withExcludedScope(String scope) {
        if (innerModel().notScopes() == null) {
            innerModel().withNotScopes(new ArrayList<>());
        }
        innerModel().notScopes().add(scope);
        return this;
    }

    @Override
    public PolicyAssignmentImpl withParameter(String name, Object value) {
        if (innerModel().parameters() == null) {
            innerModel().withParameters(new TreeMap<>());
        }
        innerModel().parameters().put(name, new ParameterValuesValue().withValue(value));
        return this;
    }

    @Override
    public PolicyAssignmentImpl withEnforcementMode(EnforcementMode mode) {
        innerModel().withEnforcementMode(mode);
        return this;
    }
}

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.management.resources.PolicyDefinition;
import com.microsoft.azure.management.resources.PolicyType;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import rx.Observable;

import java.io.IOException;

/**
 * Implementation for {@link PolicyDefinition}.
 */
final class PolicyDefinitionImpl extends
        CreatableUpdatableImpl<PolicyDefinition, PolicyDefinitionInner, PolicyDefinitionImpl>
        implements
        PolicyDefinition,
        PolicyDefinition.Definition,
        PolicyDefinition.Update {
    private final PolicyDefinitionsInner innerCollection;

    PolicyDefinitionImpl(PolicyDefinitionInner innerModel, PolicyDefinitionsInner innerCollection) {
        super(innerModel.name(), innerModel);
        this.innerCollection = innerCollection;
    }

    @Override
    public PolicyType policyType() {
        return inner().policyType();
    }

    @Override
    public String displayName() {
        return inner().displayName();
    }

    @Override
    public String description() {
        return inner().description();
    }

    @Override
    public Object policyRule() {
        return inner().policyRule();
    }

    @Override
    public String id() {
        return inner().id();
    }

    @Override
    protected Observable<PolicyDefinitionInner> getInnerAsync() {
        return innerCollection.getAsync(name());
    }

    @Override
    public PolicyDefinitionImpl withDescription(String description) {
        inner().withDescription(description);
        return this;
    }

    @Override
    public PolicyDefinitionImpl withDisplayName(String displayName) {
        inner().withDisplayName(displayName);
        return this;
    }

    @Override
    public PolicyDefinitionImpl withPolicyRule(Object policyRule) {
        inner().withPolicyRule(policyRule);
        return this;
    }

    @Override
    public PolicyDefinitionImpl withPolicyRuleJson(String policyRuleJson) {
        try {
            inner().withPolicyRule(new ObjectMapper().readTree(policyRuleJson));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    @Override
    public PolicyDefinitionImpl withPolicyType(PolicyType policyType) {
        inner().withPolicyType(policyType);
        return this;
    }

    @Override
    public Observable<PolicyDefinition> createResourceAsync() {
        return innerCollection.createOrUpdateAsync(name(), inner())
                .map(innerToFluentMap(this));
    }

    @Override
    public boolean isInCreateMode() {
        return id() == null;
    }
}

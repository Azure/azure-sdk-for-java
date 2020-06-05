// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.resources.models.PolicyDefinition;
import com.azure.resourcemanager.resources.models.PolicyType;
import com.azure.resourcemanager.resources.fluent.inner.PolicyDefinitionInner;
import com.azure.resourcemanager.resources.fluent.PolicyDefinitionsClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import reactor.core.publisher.Mono;

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
    private final PolicyDefinitionsClient innerCollection;
    private final ClientLogger logger = new ClientLogger(getClass());

    PolicyDefinitionImpl(String name, PolicyDefinitionInner innerModel, PolicyDefinitionsClient innerCollection) {
        super(name, innerModel);
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
    protected Mono<PolicyDefinitionInner> getInnerAsync() {
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
            throw logger.logExceptionAsError(new RuntimeException(e));
        }
        return this;
    }

    @Override
    public PolicyDefinitionImpl withPolicyType(PolicyType policyType) {
        inner().withPolicyType(policyType);
        return this;
    }

    @Override
    public Mono<PolicyDefinition> createResourceAsync() {
        return innerCollection.createOrUpdateAsync(name(), inner())
                .map(innerToFluentMap(this));
    }

    @Override
    public boolean isInCreateMode() {
        return id() == null;
    }
}

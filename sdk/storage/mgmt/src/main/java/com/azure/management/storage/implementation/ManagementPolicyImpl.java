/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.storage.implementation;

import com.azure.management.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import com.azure.management.storage.BlobTypes;
import com.azure.management.storage.ManagementPolicy;
import com.azure.management.storage.ManagementPolicyBaseBlob;
import com.azure.management.storage.ManagementPolicyRule;
import com.azure.management.storage.ManagementPolicySchema;
import com.azure.management.storage.ManagementPolicySnapShot;
import com.azure.management.storage.PolicyRule;
import com.azure.management.storage.models.ManagementPoliciesInner;
import com.azure.management.storage.models.ManagementPolicyInner;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


class ManagementPolicyImpl extends
        CreatableUpdatableImpl<ManagementPolicy,
                ManagementPolicyInner,
                ManagementPolicyImpl>
        implements
        ManagementPolicy,
        ManagementPolicy.Definition,
        ManagementPolicy.Update {
    private final StorageManager manager;
    private String resourceGroupName;
    private String accountName;
    private ManagementPolicySchema cpolicy;
    private ManagementPolicySchema upolicy;

    ManagementPolicyImpl(String name, StorageManager manager) {
        super(name, new ManagementPolicyInner());
        this.manager = manager;
        // Set resource name
        this.accountName = name;
        //
        this.cpolicy = new ManagementPolicySchema();
        this.upolicy = new ManagementPolicySchema();
    }

    ManagementPolicyImpl(ManagementPolicyInner inner, StorageManager manager) {
        super(inner.getName(), inner);
        this.manager = manager;
        // Set resource name
        this.accountName = inner.getName();
        // set resource ancestor and positional variables
        this.resourceGroupName = IdParsingUtils.getValueFromIdByName(inner.getId(), "resourceGroups");
        this.accountName = IdParsingUtils.getValueFromIdByName(inner.getId(), "storageAccounts");
        //
        this.cpolicy = new ManagementPolicySchema();
        this.upolicy = new ManagementPolicySchema();
    }

    @Override
    public StorageManager manager() {
        return this.manager;
    }

    @Override
    public Mono<ManagementPolicy> createResourceAsync() {
        ManagementPoliciesInner client = this.manager().inner().managementPolicies();
        return client.createOrUpdateAsync(this.resourceGroupName, this.accountName, cpolicy)
                .map(resource -> {
                    resetCreateUpdateParameters();
                    return resource;
                })
                .map(innerToFluentMap(this));
    }

    @Override
    public Mono<ManagementPolicy> updateResourceAsync() {
        ManagementPoliciesInner client = this.manager().inner().managementPolicies();
        return client.createOrUpdateAsync(this.resourceGroupName, this.accountName, upolicy)
                .map(resource -> {
                    resetCreateUpdateParameters();
                    return resource;
                })
                .map(innerToFluentMap(this));
    }

    @Override
    protected Mono<ManagementPolicyInner> getInnerAsync() {
        ManagementPoliciesInner client = this.manager().inner().managementPolicies();
        return client.getAsync(this.resourceGroupName, this.accountName);
    }

    @Override
    public boolean isInCreateMode() {
        return this.inner().getId() == null;
    }

    private void resetCreateUpdateParameters() {
        this.cpolicy = new ManagementPolicySchema();
        this.upolicy = new ManagementPolicySchema();
    }

    @Override
    public String id() {
        return this.inner().getId();
    }

    @Override
    public OffsetDateTime lastModifiedTime() {
        return this.inner().getLastModifiedTime();
    }

    @Override
    public String name() {
        return this.inner().getName();
    }

    @Override
    public ManagementPolicySchema policy() {
        return this.inner().getPolicy();
    }

    @Override
    public String type() {
        return this.inner().getType();
    }

    @Override
    public List<PolicyRule> rules() {
        List<ManagementPolicyRule> originalRules = this.policy().getRules();
        List<PolicyRule> returnRules = new ArrayList<>();
        for (ManagementPolicyRule originalRule : originalRules) {
            List<String> originalBlobTypes = originalRule.getDefinition().getFilters().getBlobTypes();
            List<BlobTypes> returnBlobTypes = new ArrayList<>();
            for (String originalBlobType : originalBlobTypes) {
                returnBlobTypes.add(BlobTypes.fromString(originalBlobType));
            }
            PolicyRule returnRule = new PolicyRuleImpl(originalRule.getName())
                    .withLifecycleRuleType()
                    .withBlobTypesToFilterFor(returnBlobTypes);

            // building up prefixes to filter on
            if (originalRule.getDefinition().getFilters().getPrefixMatch() != null) {
                ((PolicyRuleImpl) returnRule).withPrefixesToFilterFor(originalRule.getDefinition().getFilters().getPrefixMatch());
            }

            // building up actions on base blob
            ManagementPolicyBaseBlob originalBaseBlobActions = originalRule.getDefinition().getActions().getBaseBlob();
            if (originalBaseBlobActions != null) {
                if (originalBaseBlobActions.getTierToCool() != null) {
                    ((PolicyRuleImpl) returnRule).withTierToCoolActionOnBaseBlob(originalBaseBlobActions.getTierToCool().getDaysAfterModificationGreaterThan());
                }
                if (originalBaseBlobActions.getTierToArchive() != null) {
                    ((PolicyRuleImpl) returnRule).withTierToArchiveActionOnBaseBlob(originalBaseBlobActions.getTierToArchive().getDaysAfterModificationGreaterThan());
                }
                if (originalBaseBlobActions.getDelete() != null) {
                    ((PolicyRuleImpl) returnRule).withDeleteActionOnBaseBlob(originalBaseBlobActions.getDelete().getDaysAfterModificationGreaterThan());
                }
            }

            // build up actions on snapshot
            ManagementPolicySnapShot originalSnapshotActions = originalRule.getDefinition().getActions().getSnapshot();
            if (originalSnapshotActions != null) {
                if (originalSnapshotActions.getDelete() != null) {
                    ((PolicyRuleImpl) returnRule).withDeleteActionOnSnapShot(originalSnapshotActions.getDelete().getDaysAfterCreationGreaterThan());
                }
            }
            returnRules.add(returnRule);
        }
        return Collections.unmodifiableList(returnRules);
    }

    @Override
    public ManagementPolicyImpl withExistingStorageAccount(String resourceGroupName, String accountName) {
        this.resourceGroupName = resourceGroupName;
        this.accountName = accountName;
        return this;
    }

    @Override
    public ManagementPolicyImpl withPolicy(ManagementPolicySchema policy) {
        if (isInCreateMode()) {
            this.cpolicy = policy;
        } else {
            this.upolicy = policy;
        }
        return this;
    }

    @Override
    public PolicyRule.DefinitionStages.Blank defineRule(String name) {
        return new PolicyRuleImpl(this, name);
    }

    void defineRule(PolicyRuleImpl policyRuleImpl) {
        if (isInCreateMode()) {
            if (this.cpolicy.getRules() == null) {
                this.cpolicy.setRules(new ArrayList<ManagementPolicyRule>());
            }
            List<ManagementPolicyRule> rules = this.cpolicy.getRules();
            rules.add(policyRuleImpl.inner());
            this.cpolicy.setRules(rules);
        } else {
            if (this.upolicy.getRules() == null) {
                this.upolicy.setRules(new ArrayList<ManagementPolicyRule>());
            }
            List<ManagementPolicyRule> rules = this.upolicy.getRules();
            rules.add(policyRuleImpl.inner());
            this.upolicy.setRules(rules);
        }
    }

    @Override
    public PolicyRule.Update updateRule(String name) {
        ManagementPolicyRule ruleToUpdate = null;
        for (ManagementPolicyRule rule : this.policy().getRules()) {
            if (rule.getName().equals(name)) {
                ruleToUpdate = rule;
            }
        }
        if (ruleToUpdate == null) {
            throw new UnsupportedOperationException("There is no rule that exists with the name " + name + ". Please define a rule with this name before updating.");
        }
        return new PolicyRuleImpl(ruleToUpdate, this);
    }

    @Override
    public Update withoutRule(String name) {
        ManagementPolicyRule ruleToDelete = null;
        for (ManagementPolicyRule rule : this.policy().getRules()) {
            if (rule.getName().equals(name)) {
                ruleToDelete = rule;
            }
        }
        if (ruleToDelete == null) {
            throw new UnsupportedOperationException("There is no rule that exists with the name " + name + " so this rule can not be deleted.");
        }
        List<ManagementPolicyRule> currentRules = this.upolicy.getRules();
        currentRules.remove(ruleToDelete);
        this.upolicy.setRules(currentRules);
        return this;
    }
}


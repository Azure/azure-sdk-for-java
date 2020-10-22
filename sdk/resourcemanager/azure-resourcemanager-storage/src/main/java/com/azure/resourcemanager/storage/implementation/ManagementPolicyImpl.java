// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.fluent.ManagementPoliciesClient;
import com.azure.resourcemanager.storage.models.BlobTypes;
import com.azure.resourcemanager.storage.models.ManagementPolicy;
import com.azure.resourcemanager.storage.models.ManagementPolicyBaseBlob;
import com.azure.resourcemanager.storage.models.ManagementPolicyName;
import com.azure.resourcemanager.storage.models.ManagementPolicyRule;
import com.azure.resourcemanager.storage.models.ManagementPolicySchema;
import com.azure.resourcemanager.storage.models.ManagementPolicySnapShot;
import com.azure.resourcemanager.storage.models.PolicyRule;
import com.azure.resourcemanager.storage.fluent.models.ManagementPolicyInner;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import reactor.core.publisher.Mono;

class ManagementPolicyImpl extends CreatableUpdatableImpl<ManagementPolicy, ManagementPolicyInner, ManagementPolicyImpl>
    implements ManagementPolicy, ManagementPolicy.Definition, ManagementPolicy.Update {
    private final ClientLogger logger = new ClientLogger(getClass());
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
        super(inner.name(), inner);
        this.manager = manager;
        // Set resource name
        this.accountName = inner.name();
        // set resource ancestor and positional variables
        this.resourceGroupName = IdParsingUtils.getValueFromIdByName(inner.id(), "resourceGroups");
        this.accountName = IdParsingUtils.getValueFromIdByName(inner.id(), "storageAccounts");
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
        ManagementPoliciesClient client = this.manager().serviceClient().getManagementPolicies();
        return client
            .createOrUpdateAsync(this.resourceGroupName, this.accountName, ManagementPolicyName.DEFAULT, cpolicy)
            .map(
                resource -> {
                    resetCreateUpdateParameters();
                    return resource;
                })
            .map(innerToFluentMap(this));
    }

    @Override
    public Mono<ManagementPolicy> updateResourceAsync() {
        ManagementPoliciesClient client = this.manager().serviceClient().getManagementPolicies();
        return client
            .createOrUpdateAsync(this.resourceGroupName, this.accountName, ManagementPolicyName.DEFAULT, upolicy)
            .map(
                resource -> {
                    resetCreateUpdateParameters();
                    return resource;
                })
            .map(innerToFluentMap(this));
    }

    @Override
    protected Mono<ManagementPolicyInner> getInnerAsync() {
        ManagementPoliciesClient client = this.manager().serviceClient().getManagementPolicies();
        return client.getAsync(this.resourceGroupName, this.accountName, ManagementPolicyName.DEFAULT);
    }

    @Override
    public boolean isInCreateMode() {
        return this.innerModel().id() == null;
    }

    private void resetCreateUpdateParameters() {
        this.cpolicy = new ManagementPolicySchema();
        this.upolicy = new ManagementPolicySchema();
    }

    @Override
    public String id() {
        return this.innerModel().id();
    }

    @Override
    public OffsetDateTime lastModifiedTime() {
        return this.innerModel().lastModifiedTime();
    }

    @Override
    public String name() {
        return this.innerModel().name();
    }

    @Override
    public ManagementPolicySchema policy() {
        return this.innerModel().policy();
    }

    @Override
    public String type() {
        return this.innerModel().type();
    }

    @Override
    public List<PolicyRule> rules() {
        List<ManagementPolicyRule> originalRules = this.policy().rules();
        List<PolicyRule> returnRules = new ArrayList<>();
        for (ManagementPolicyRule originalRule : originalRules) {
            List<String> originalBlobTypes = originalRule.definition().filters().blobTypes();
            List<BlobTypes> returnBlobTypes = new ArrayList<>();
            for (String originalBlobType : originalBlobTypes) {
                returnBlobTypes.add(BlobTypes.fromString(originalBlobType));
            }
            PolicyRule returnRule =
                new PolicyRuleImpl(originalRule.name())
                    .withLifecycleRuleType()
                    .withBlobTypesToFilterFor(returnBlobTypes);

            // building up prefixes to filter on
            if (originalRule.definition().filters().prefixMatch() != null) {
                ((PolicyRuleImpl) returnRule)
                    .withPrefixesToFilterFor(originalRule.definition().filters().prefixMatch());
            }

            // building up actions on base blob
            ManagementPolicyBaseBlob originalBaseBlobActions = originalRule.definition().actions().baseBlob();
            if (originalBaseBlobActions != null) {
                if (originalBaseBlobActions.tierToCool() != null) {
                    ((PolicyRuleImpl) returnRule)
                        .withTierToCoolActionOnBaseBlob(
                            originalBaseBlobActions.tierToCool().daysAfterModificationGreaterThan());
                }
                if (originalBaseBlobActions.tierToArchive() != null) {
                    ((PolicyRuleImpl) returnRule)
                        .withTierToArchiveActionOnBaseBlob(
                            originalBaseBlobActions.tierToArchive().daysAfterModificationGreaterThan());
                }
                if (originalBaseBlobActions.delete() != null) {
                    ((PolicyRuleImpl) returnRule)
                        .withDeleteActionOnBaseBlob(
                            originalBaseBlobActions.delete().daysAfterModificationGreaterThan());
                }
            }

            // build up actions on snapshot
            ManagementPolicySnapShot originalSnapshotActions = originalRule.definition().actions().snapshot();
            if (originalSnapshotActions != null) {
                if (originalSnapshotActions.delete() != null) {
                    ((PolicyRuleImpl) returnRule)
                        .withDeleteActionOnSnapShot(originalSnapshotActions.delete().daysAfterCreationGreaterThan());
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
            if (this.cpolicy.rules() == null) {
                this.cpolicy.withRules(new ArrayList<ManagementPolicyRule>());
            }
            List<ManagementPolicyRule> rules = this.cpolicy.rules();
            rules.add(policyRuleImpl.innerModel());
            this.cpolicy.withRules(rules);
        } else {
            if (this.upolicy.rules() == null) {
                this.upolicy.withRules(new ArrayList<ManagementPolicyRule>());
            }
            List<ManagementPolicyRule> rules = this.upolicy.rules();
            rules.add(policyRuleImpl.innerModel());
            this.upolicy.withRules(rules);
        }
    }

    @Override
    public PolicyRule.Update updateRule(String name) {
        ManagementPolicyRule ruleToUpdate = null;
        for (ManagementPolicyRule rule : this.policy().rules()) {
            if (rule.name().equals(name)) {
                ruleToUpdate = rule;
            }
        }
        if (ruleToUpdate == null) {
            throw logger.logExceptionAsError(new UnsupportedOperationException(
                "There is no rule that exists with the name "
                    + name
                    + ". Please define a rule with this name before updating."));
        }
        return new PolicyRuleImpl(ruleToUpdate, this);
    }

    @Override
    public Update withoutRule(String name) {
        ManagementPolicyRule ruleToDelete = null;
        for (ManagementPolicyRule rule : this.policy().rules()) {
            if (rule.name().equals(name)) {
                ruleToDelete = rule;
            }
        }
        if (ruleToDelete == null) {
            throw logger.logExceptionAsError(new UnsupportedOperationException(
                "There is no rule that exists with the name " + name + " so this rule can not be deleted."));
        }
        List<ManagementPolicyRule> currentRules = this.upolicy.rules();
        currentRules.remove(ruleToDelete);
        this.upolicy.withRules(currentRules);
        return this;
    }
}

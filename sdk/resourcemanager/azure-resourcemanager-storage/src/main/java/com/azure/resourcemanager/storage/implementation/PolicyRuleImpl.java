// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.implementation;

import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.storage.models.BlobTypes;
import com.azure.resourcemanager.storage.models.DateAfterCreation;
import com.azure.resourcemanager.storage.models.DateAfterModification;
import com.azure.resourcemanager.storage.models.ManagementPolicy;
import com.azure.resourcemanager.storage.models.ManagementPolicyAction;
import com.azure.resourcemanager.storage.models.ManagementPolicyBaseBlob;
import com.azure.resourcemanager.storage.models.ManagementPolicyDefinition;
import com.azure.resourcemanager.storage.models.ManagementPolicyFilter;
import com.azure.resourcemanager.storage.models.ManagementPolicyRule;
import com.azure.resourcemanager.storage.models.ManagementPolicySnapShot;
import com.azure.resourcemanager.storage.models.PolicyRule;
import com.azure.resourcemanager.storage.models.RuleType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class PolicyRuleImpl implements PolicyRule, PolicyRule.Definition, PolicyRule.Update, HasInnerModel<ManagementPolicyRule> {

    private ManagementPolicyRule inner;
    private ManagementPolicyImpl managementPolicyImpl;

    PolicyRuleImpl(ManagementPolicyImpl managementPolicyImpl, String name) {
        this.inner = new ManagementPolicyRule();
        this.inner.withDefinition(new ManagementPolicyDefinition());
        this.inner.definition().withFilters(new ManagementPolicyFilter());
        this.inner.definition().withActions(new ManagementPolicyAction());
        this.managementPolicyImpl = managementPolicyImpl;
        this.inner.withName(name);
    }

    PolicyRuleImpl(String name) {
        this.inner = new ManagementPolicyRule();
        this.inner.withDefinition(new ManagementPolicyDefinition());
        this.inner.definition().withFilters(new ManagementPolicyFilter());
        this.inner.definition().withActions(new ManagementPolicyAction());
        this.inner.withName(name);
    }

    PolicyRuleImpl(ManagementPolicyRule managementPolicyRule, ManagementPolicyImpl managementPolicyImpl) {
        this.inner = managementPolicyRule;
        this.managementPolicyImpl = managementPolicyImpl;
    }

    @Override
    public String name() {
        return this.inner.name();
    }

    @Override
    public RuleType type() {
        return this.inner.type();
    }

    @Override
    public List<BlobTypes> blobTypesToFilterFor() {
        List<BlobTypes> blobTypes = new ArrayList<>();
        for (String blobTypeString : this.inner.definition().filters().blobTypes()) {
            blobTypes.add(BlobTypes.fromString(blobTypeString));
        }
        return Collections.unmodifiableList(blobTypes);
    }

    @Override
    public List<String> prefixesToFilterFor() {
        return Collections.unmodifiableList(this.inner.definition().filters().prefixMatch());
    }

    @Override
    public ManagementPolicyBaseBlob actionsOnBaseBlob() {
        return this.inner.definition().actions().baseBlob();
    }

    @Override
    public ManagementPolicySnapShot actionsOnSnapShot() {
        return this.inner.definition().actions().snapshot();
    }

    @Override
    public boolean tierToCoolActionOnBaseBlobEnabled() {
        if (this.inner.definition().actions().baseBlob() == null) {
            return false;
        }
        return this.inner.definition().actions().baseBlob().tierToCool() != null;
    }

    @Override
    public boolean tierToArchiveActionOnBaseBlobEnabled() {
        if (this.inner.definition().actions().baseBlob() == null) {
            return false;
        }
        return this.inner.definition().actions().baseBlob().tierToArchive() != null;
    }

    @Override
    public boolean deleteActionOnBaseBlobEnabled() {
        if (this.inner.definition().actions().baseBlob() == null) {
            return false;
        }
        return this.inner.definition().actions().baseBlob().delete() != null;
    }

    @Override
    public boolean deleteActionOnSnapShotEnabled() {
        if (this.inner.definition().actions().snapshot() == null) {
            return false;
        }
        return this.inner.definition().actions().snapshot().delete() != null;
    }

    @Override
    public Float daysAfterBaseBlobModificationUntilCooling() {
        if (this.inner.definition().actions().baseBlob() == null
            || this.inner.definition().actions().baseBlob().tierToCool() == null) {
            return null;
        }
        return this.inner.definition().actions().baseBlob().tierToCool().daysAfterModificationGreaterThan();
    }

    @Override
    public Float daysAfterBaseBlobModificationUntilArchiving() {
        if (this.inner.definition().actions().baseBlob() == null
            || this.inner.definition().actions().baseBlob().tierToArchive() == null) {
            return null;
        }
        return this.inner.definition().actions().baseBlob().tierToArchive().daysAfterModificationGreaterThan();
    }

    @Override
    public Float daysAfterBaseBlobModificationUntilDeleting() {
        if (this.inner.definition().actions().baseBlob() == null
            || this.inner.definition().actions().baseBlob().delete() == null) {
            return null;
        }
        return this.inner.definition().actions().baseBlob().delete().daysAfterModificationGreaterThan();
    }

    @Override
    public Float daysAfterSnapShotCreationUntilDeleting() {
        if (this.inner.definition().actions().snapshot() == null
            || this.inner.definition().actions().snapshot().delete() == null) {
            return null;
        }
        return this.inner.definition().actions().snapshot().delete().daysAfterCreationGreaterThan();
    }

    @Override
    public ManagementPolicyRule innerModel() {
        return this.inner;
    }

    @Override
    public PolicyRuleImpl withLifecycleRuleType() {
        this.inner.withType(RuleType.LIFECYCLE);
        return this;
    }

    @Override
    public PolicyRuleImpl withBlobTypesToFilterFor(List<BlobTypes> blobTypes) {
        List<String> blobTypesString = new ArrayList<>();
        for (BlobTypes blobType : blobTypes) {
            blobTypesString.add(blobType.toString());
        }
        this.inner.definition().filters().withBlobTypes(blobTypesString);
        return this;
    }

    @Override
    public PolicyRuleImpl withBlobTypeToFilterFor(BlobTypes blobType) {
        List<String> blobTypesToFilterFor = this.inner.definition().filters().blobTypes();
        if (blobTypesToFilterFor == null) {
            blobTypesToFilterFor = new ArrayList<>();
        }
        if (blobTypesToFilterFor.contains(blobType.toString())) {
            return this;
        }
        blobTypesToFilterFor.add(blobType.toString());
        this.inner.definition().filters().withBlobTypes(blobTypesToFilterFor);
        return this;
    }

    @Override
    public Update withBlobTypeToFilterForRemoved(BlobTypes blobType) {
        List<String> blobTypesToFilterFor = this.inner.definition().filters().blobTypes();
        blobTypesToFilterFor.remove(blobType.toString());
        this.inner.definition().filters().withBlobTypes(blobTypesToFilterFor);
        return this;
    }

    @Override
    public PolicyRuleImpl withPrefixesToFilterFor(List<String> prefixes) {
        this.inner.definition().filters().withPrefixMatch(prefixes);
        return this;
    }

    @Override
    public PolicyRuleImpl withPrefixToFilterFor(String prefix) {
        List<String> prefixesToFilterFor = this.inner.definition().filters().prefixMatch();
        if (prefixesToFilterFor == null) {
            prefixesToFilterFor = new ArrayList<>();
        }
        if (prefixesToFilterFor.contains(prefix)) {
            return this;
        }
        prefixesToFilterFor.add(prefix);
        this.inner.definition().filters().withPrefixMatch(prefixesToFilterFor);
        return this;
    }

    @Override
    public Update withoutPrefixesToFilterFor() {
        this.inner.definition().filters().withPrefixMatch(null);
        return this;
    }

    @Override
    public PolicyRuleImpl withTierToCoolActionOnBaseBlob(float daysAfterBaseBlobModificationUntilCooling) {
        ManagementPolicyBaseBlob currentBaseBlob = this.inner.definition().actions().baseBlob();
        if (currentBaseBlob == null) {
            currentBaseBlob = new ManagementPolicyBaseBlob();
        }
        currentBaseBlob
            .withTierToCool(
                new DateAfterModification()
                    .withDaysAfterModificationGreaterThan(daysAfterBaseBlobModificationUntilCooling));
        this.inner.definition().actions().withBaseBlob(currentBaseBlob);
        return this;
    }

    @Override
    public PolicyRuleImpl withTierToArchiveActionOnBaseBlob(float daysAfterBaseBlobModificationUntilArchiving) {
        ManagementPolicyBaseBlob currentBaseBlob = this.inner.definition().actions().baseBlob();
        if (currentBaseBlob == null) {
            currentBaseBlob = new ManagementPolicyBaseBlob();
        }
        currentBaseBlob
            .withTierToArchive(
                new DateAfterModification()
                    .withDaysAfterModificationGreaterThan(daysAfterBaseBlobModificationUntilArchiving));
        this.inner.definition().actions().withBaseBlob(currentBaseBlob);
        return this;
    }

    @Override
    public PolicyRuleImpl withDeleteActionOnBaseBlob(float daysAfterBaseBlobModificationUntilDeleting) {
        ManagementPolicyBaseBlob currentBaseBlob = this.inner.definition().actions().baseBlob();
        if (currentBaseBlob == null) {
            currentBaseBlob = new ManagementPolicyBaseBlob();
        }
        currentBaseBlob
            .withDelete(
                new DateAfterModification()
                    .withDaysAfterModificationGreaterThan(daysAfterBaseBlobModificationUntilDeleting));
        this.inner.definition().actions().withBaseBlob(currentBaseBlob);
        return this;
    }

    @Override
    public PolicyRuleImpl withDeleteActionOnSnapShot(float daysAfterSnapShotCreationUntilDeleting) {
        ManagementPolicySnapShot currentSnapShot = new ManagementPolicySnapShot();
        currentSnapShot
            .withDelete(
                new DateAfterCreation().withDaysAfterCreationGreaterThan(daysAfterSnapShotCreationUntilDeleting));
        this.inner.definition().actions().withSnapshot(currentSnapShot);
        return this;
    }

    @Override
    public DefinitionStages.WithPolicyRuleAttachable withActionsOnBaseBlob(ManagementPolicyBaseBlob baseBlobActions) {
        this.inner.definition().actions().withBaseBlob(baseBlobActions);
        return this;
    }

    @Override
    public DefinitionStages.WithPolicyRuleAttachable withActionsOnSnapShot(ManagementPolicySnapShot snapShotActions) {
        this.inner.definition().actions().withSnapshot(snapShotActions);
        return this;
    }

    @Override
    public Update updateActionsOnBaseBlob(ManagementPolicyBaseBlob baseBlobActions) {
        this.inner.definition().actions().withBaseBlob(baseBlobActions);
        return this;
    }

    @Override
    public Update updateActionsOnSnapShot(ManagementPolicySnapShot snapShotActions) {
        this.inner.definition().actions().withSnapshot(snapShotActions);
        return this;
    }

    @Override
    public ManagementPolicyImpl attach() {
        this.managementPolicyImpl.defineRule(this);
        return this.managementPolicyImpl;
    }

    @Override
    public ManagementPolicy.Update parent() {
        this.managementPolicyImpl.defineRule(this);
        return this.managementPolicyImpl;
    }
}

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.storage.implementation;

import com.azure.management.resources.fluentcore.model.HasInner;
import com.azure.management.storage.BlobTypes;
import com.azure.management.storage.DateAfterCreation;
import com.azure.management.storage.DateAfterModification;
import com.azure.management.storage.ManagementPolicy;
import com.azure.management.storage.ManagementPolicyAction;
import com.azure.management.storage.ManagementPolicyBaseBlob;
import com.azure.management.storage.ManagementPolicyDefinition;
import com.azure.management.storage.ManagementPolicyFilter;
import com.azure.management.storage.ManagementPolicyRule;
import com.azure.management.storage.ManagementPolicySnapShot;
import com.azure.management.storage.PolicyRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class PolicyRuleImpl implements
        PolicyRule,
        PolicyRule.Definition,
        PolicyRule.Update,
        HasInner<ManagementPolicyRule> {

    private ManagementPolicyRule inner;
    private ManagementPolicyImpl managementPolicyImpl;

    PolicyRuleImpl(ManagementPolicyImpl managementPolicyImpl, String name) {
        this.inner = new ManagementPolicyRule();
        this.inner.setDefinition(new ManagementPolicyDefinition());
        this.inner.getDefinition().setFilters(new ManagementPolicyFilter());
        this.inner.getDefinition().setActions(new ManagementPolicyAction());
        this.managementPolicyImpl = managementPolicyImpl;
        this.inner.setName(name);
    }

    PolicyRuleImpl(String name) {
        this.inner = new ManagementPolicyRule();
        this.inner.setDefinition(new ManagementPolicyDefinition());
        this.inner.getDefinition().setFilters(new ManagementPolicyFilter());
        this.inner.getDefinition().setActions(new ManagementPolicyAction());
        this.inner.setName(name);
    }

    PolicyRuleImpl(ManagementPolicyRule managementPolicyRule, ManagementPolicyImpl managementPolicyImpl) {
        this.inner = managementPolicyRule;
        this.managementPolicyImpl = managementPolicyImpl;
    }

    @Override
    public String name() {
        return this.inner.getName();
    }

    @Override
    public String type() {
        return this.inner.getType();
    }

    @Override
    public List<BlobTypes> blobTypesToFilterFor() {
        List<BlobTypes> blobTypes = new ArrayList<>();
        for (String blobTypeString : this.inner.getDefinition().getFilters().getBlobTypes()) {
            blobTypes.add(BlobTypes.fromString(blobTypeString));
        }
        return Collections.unmodifiableList(blobTypes);
    }

    @Override
    public List<String> prefixesToFilterFor() {
        return Collections.unmodifiableList(this.inner.getDefinition().getFilters().getPrefixMatch());
    }

    @Override
    public ManagementPolicyBaseBlob actionsOnBaseBlob() {
        return this.inner.getDefinition().getActions().getBaseBlob();
    }

    @Override
    public ManagementPolicySnapShot actionsOnSnapShot() {
        return this.inner.getDefinition().getActions().getSnapshot();
    }

    @Override
    public boolean tierToCoolActionOnBaseBlobEnabled() {
        if (this.inner.getDefinition().getActions().getBaseBlob() == null) {
            return false;
        }
        return this.inner.getDefinition().getActions().getBaseBlob().getTierToCool() != null;
    }

    @Override
    public boolean tierToArchiveActionOnBaseBlobEnabled() {
        if (this.inner.getDefinition().getActions().getBaseBlob() == null) {
            return false;
        }
        return this.inner.getDefinition().getActions().getBaseBlob().getTierToArchive() != null;
    }

    @Override
    public boolean deleteActionOnBaseBlobEnabled() {
        if (this.inner.getDefinition().getActions().getBaseBlob() == null) {
            return false;
        }
        return this.inner.getDefinition().getActions().getBaseBlob().getDelete() != null;
    }

    @Override
    public boolean deleteActionOnSnapShotEnabled() {
        if (this.inner.getDefinition().getActions().getSnapshot() == null) {
            return false;
        }
        return this.inner.getDefinition().getActions().getSnapshot().getDelete() != null;
    }

    @Override
    public Float daysAfterBaseBlobModificationUntilCooling() {
        if (this.inner.getDefinition().getActions().getBaseBlob() == null || this.inner.getDefinition().getActions().getBaseBlob().getTierToCool() == null) {
            return null;
        }
        return this.inner.getDefinition().getActions().getBaseBlob().getTierToCool().getDaysAfterModificationGreaterThan();
    }

    @Override
    public Float daysAfterBaseBlobModificationUntilArchiving() {
        if (this.inner.getDefinition().getActions().getBaseBlob() == null || this.inner.getDefinition().getActions().getBaseBlob().getTierToArchive() == null) {
            return null;
        }
        return this.inner.getDefinition().getActions().getBaseBlob().getTierToArchive().getDaysAfterModificationGreaterThan();
    }

    @Override
    public Float daysAfterBaseBlobModificationUntilDeleting() {
        if (this.inner.getDefinition().getActions().getBaseBlob() == null || this.inner.getDefinition().getActions().getBaseBlob().getDelete() == null) {
            return null;
        }
        return this.inner.getDefinition().getActions().getBaseBlob().getDelete().getDaysAfterModificationGreaterThan();
    }

    @Override
    public Float daysAfterSnapShotCreationUntilDeleting() {
        if (this.inner.getDefinition().getActions().getSnapshot() == null || this.inner.getDefinition().getActions().getSnapshot().getDelete() == null) {
            return null;
        }
        return this.inner.getDefinition().getActions().getSnapshot().getDelete().getDaysAfterCreationGreaterThan();
    }

    @Override
    public ManagementPolicyRule inner() {
        return this.inner;
    }

    @Override
    public PolicyRuleImpl withLifecycleRuleType() {
        this.inner.setType("Lifecycle");
        return this;
    }


    @Override
    public PolicyRuleImpl withBlobTypesToFilterFor(List<BlobTypes> blobTypes) {
        List<String> blobTypesString = new ArrayList<>();
        for (BlobTypes blobType : blobTypes) {
            blobTypesString.add(blobType.toString());
        }
        this.inner.getDefinition().getFilters().setBlobTypes(blobTypesString);
        return this;
    }

    @Override
    public PolicyRuleImpl withBlobTypeToFilterFor(BlobTypes blobType) {
        List<String> blobTypesToFilterFor = this.inner.getDefinition().getFilters().getBlobTypes();
        if (blobTypesToFilterFor == null) {
            blobTypesToFilterFor = new ArrayList<>();
        }
        if (blobTypesToFilterFor.contains(blobType)) {
            return this;
        }
        blobTypesToFilterFor.add(blobType.toString());
        this.inner.getDefinition().getFilters().setBlobTypes(blobTypesToFilterFor);
        return this;
    }

    @Override
    public Update withBlobTypeToFilterForRemoved(BlobTypes blobType) {
        List<String> blobTypesToFilterFor = this.inner.getDefinition().getFilters().getBlobTypes();
        blobTypesToFilterFor.remove(blobType.toString());
        this.inner.getDefinition().getFilters().setBlobTypes(blobTypesToFilterFor);
        return this;
    }

    @Override
    public PolicyRuleImpl withPrefixesToFilterFor(List<String> prefixes) {
        this.inner.getDefinition().getFilters().setPrefixMatch(prefixes);
        return this;
    }

    @Override
    public PolicyRuleImpl withPrefixToFilterFor(String prefix) {
        List<String> prefixesToFilterFor = this.inner.getDefinition().getFilters().getPrefixMatch();
        if (prefixesToFilterFor == null) {
            prefixesToFilterFor = new ArrayList<>();
        }
        if (prefixesToFilterFor.contains(prefix)) {
            return this;
        }
        prefixesToFilterFor.add(prefix);
        this.inner.getDefinition().getFilters().setPrefixMatch(prefixesToFilterFor);
        return this;
    }

    @Override
    public Update withoutPrefixesToFilterFor() {
        this.inner.getDefinition().getFilters().setPrefixMatch(null);
        return this;
    }

    @Override
    public PolicyRuleImpl withTierToCoolActionOnBaseBlob(float daysAfterBaseBlobModificationUntilCooling) {
        ManagementPolicyBaseBlob currentBaseBlob = this.inner.getDefinition().getActions().getBaseBlob();
        if (currentBaseBlob == null) {
            currentBaseBlob = new ManagementPolicyBaseBlob();
        }
        currentBaseBlob.setTierToCool(new DateAfterModification().setDaysAfterModificationGreaterThan(daysAfterBaseBlobModificationUntilCooling));
        this.inner.getDefinition().getActions().setBaseBlob(currentBaseBlob);
        return this;
    }

    @Override
    public PolicyRuleImpl withTierToArchiveActionOnBaseBlob(float daysAfterBaseBlobModificationUntilArchiving) {
        ManagementPolicyBaseBlob currentBaseBlob = this.inner.getDefinition().getActions().getBaseBlob();
        if (currentBaseBlob == null) {
            currentBaseBlob = new ManagementPolicyBaseBlob();
        }
        currentBaseBlob.setTierToArchive(new DateAfterModification().setDaysAfterModificationGreaterThan(daysAfterBaseBlobModificationUntilArchiving));
        this.inner.getDefinition().getActions().setBaseBlob(currentBaseBlob);
        return this;
    }

    @Override
    public PolicyRuleImpl withDeleteActionOnBaseBlob(float daysAfterBaseBlobModificationUntilDeleting) {
        ManagementPolicyBaseBlob currentBaseBlob = this.inner.getDefinition().getActions().getBaseBlob();
        if (currentBaseBlob == null) {
            currentBaseBlob = new ManagementPolicyBaseBlob();
        }
        currentBaseBlob.setDelete(new DateAfterModification().setDaysAfterModificationGreaterThan(daysAfterBaseBlobModificationUntilDeleting));
        this.inner.getDefinition().getActions().setBaseBlob(currentBaseBlob);
        return this;
    }

    @Override
    public PolicyRuleImpl withDeleteActionOnSnapShot(float daysAfterSnapShotCreationUntilDeleting) {
        ManagementPolicySnapShot currentSnapShot = new ManagementPolicySnapShot();
        if (currentSnapShot == null) {
            currentSnapShot = new ManagementPolicySnapShot();
        }
        currentSnapShot.setDelete(new DateAfterCreation().setDaysAfterCreationGreaterThan(daysAfterSnapShotCreationUntilDeleting));
        this.inner.getDefinition().getActions().setSnapshot(currentSnapShot);
        return this;
    }

    @Override
    public DefinitionStages.WithPolicyRuleAttachable withActionsOnBaseBlob(ManagementPolicyBaseBlob baseBlobActions) {
        this.inner.getDefinition().getActions().setBaseBlob(baseBlobActions);
        return this;
    }

    @Override
    public DefinitionStages.WithPolicyRuleAttachable withActionsOnSnapShot(ManagementPolicySnapShot snapShotActions) {
        this.inner.getDefinition().getActions().setSnapshot(snapShotActions);
        return this;
    }

    @Override
    public Update updateActionsOnBaseBlob(ManagementPolicyBaseBlob baseBlobActions) {
        this.inner.getDefinition().getActions().setBaseBlob(baseBlobActions);
        return this;
    }

    @Override
    public Update updateActionsOnSnapShot(ManagementPolicySnapShot snapShotActions) {
        this.inner.getDefinition().getActions().setSnapshot(snapShotActions);
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
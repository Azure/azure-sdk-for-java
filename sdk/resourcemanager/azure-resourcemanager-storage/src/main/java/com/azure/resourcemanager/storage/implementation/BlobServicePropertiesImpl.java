// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.implementation;

import com.azure.resourcemanager.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.fluent.BlobServicesClient;
import com.azure.resourcemanager.storage.fluent.models.BlobServicePropertiesInner;
import com.azure.resourcemanager.storage.models.BlobServiceProperties;
import com.azure.resourcemanager.storage.models.CorsRule;
import com.azure.resourcemanager.storage.models.CorsRules;
import com.azure.resourcemanager.storage.models.DeleteRetentionPolicy;
import com.azure.resourcemanager.storage.models.LastAccessTimeTrackingPolicy;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

class BlobServicePropertiesImpl
    extends CreatableUpdatableImpl<BlobServiceProperties, BlobServicePropertiesInner, BlobServicePropertiesImpl>
    implements BlobServiceProperties, BlobServiceProperties.Definition, BlobServiceProperties.Update {
    private final StorageManager manager;
    private String resourceGroupName;
    private String accountName;

    BlobServicePropertiesImpl(String name, StorageManager manager) {
        super(name, new BlobServicePropertiesInner());
        this.manager = manager;
        // Set resource name
        this.accountName = name;
        //
    }

    BlobServicePropertiesImpl(BlobServicePropertiesInner inner, StorageManager manager) {
        super(inner.name(), inner);
        this.manager = manager;
        // Set resource name
        this.accountName = inner.name();
        // set resource ancestor and positional variables
        this.resourceGroupName = IdParsingUtils.getValueFromIdByName(inner.id(), "resourceGroups");
        this.accountName = IdParsingUtils.getValueFromIdByName(inner.id(), "storageAccounts");
        //
    }

    @Override
    public StorageManager manager() {
        return this.manager;
    }

    @Override
    public Mono<BlobServiceProperties> createResourceAsync() {
        BlobServicesClient client = this.manager().serviceClient().getBlobServices();
        return client
            .setServicePropertiesAsync(this.resourceGroupName, this.accountName, this.innerModel())
            .map(innerToFluentMap(this));
    }

    @Override
    public Mono<BlobServiceProperties> updateResourceAsync() {
        BlobServicesClient client = this.manager().serviceClient().getBlobServices();
        return client
            .setServicePropertiesAsync(this.resourceGroupName, this.accountName, this.innerModel())
            .map(innerToFluentMap(this));
    }

    @Override
    protected Mono<BlobServicePropertiesInner> getInnerAsync() {
        BlobServicesClient client = this.manager().serviceClient().getBlobServices();
        return client.getServicePropertiesAsync(this.resourceGroupName, this.accountName);
    }

    @Override
    public boolean isInCreateMode() {
        return this.innerModel().id() == null;
    }

    @Override
    public CorsRules cors() {
        return this.innerModel().cors();
    }

    @Override
    public String defaultServiceVersion() {
        return this.innerModel().defaultServiceVersion();
    }

    @Override
    public DeleteRetentionPolicy deleteRetentionPolicy() {
        return this.innerModel().deleteRetentionPolicy();
    }

    @Override
    public DeleteRetentionPolicy containerDeleteRetentionPolicy() {
        return this.innerModel().containerDeleteRetentionPolicy();
    }

    @Override
    public String id() {
        return this.innerModel().id();
    }

    @Override
    public String name() {
        return this.innerModel().name();
    }

    @Override
    public String type() {
        return this.innerModel().type();
    }

    @Override
    public Boolean isBlobVersioningEnabled() {
        return this.innerModel().isVersioningEnabled();
    }

    @Override
    public boolean isLastAccessTimeTrackingPolicyEnabled() {
        return this.innerModel().lastAccessTimeTrackingPolicy() != null
            && ResourceManagerUtils.toPrimitiveBoolean(this.innerModel().lastAccessTimeTrackingPolicy().enable());
    }

    @Override
    public LastAccessTimeTrackingPolicy lastAccessTimeTrackingPolicy() {
        return this.innerModel().lastAccessTimeTrackingPolicy();
    }

    @Override
    public BlobServicePropertiesImpl withExistingStorageAccount(String resourceGroupName, String accountName) {
        this.resourceGroupName = resourceGroupName;
        this.accountName = accountName;
        return this;
    }

    @Override
    public BlobServicePropertiesImpl withCORSRules(List<CorsRule> corsRules) {
        this.innerModel().withCors(new CorsRules().withCorsRules(corsRules));
        return this;
    }

    @Override
    public BlobServicePropertiesImpl withCORSRule(CorsRule corsRule) {
        CorsRules corsRules = this.innerModel().cors();
        if (corsRules == null) {
            List<CorsRule> firstCorsRule = new ArrayList<>();
            firstCorsRule.add(corsRule);
            this.innerModel().withCors(new CorsRules().withCorsRules(firstCorsRule));
        } else {
            List<CorsRule> currentCorsRules = corsRules.corsRules();
            currentCorsRules.add(corsRule);
            this.innerModel().withCors(corsRules.withCorsRules(currentCorsRules));
        }
        return this;
    }

    @Override
    public BlobServicePropertiesImpl withDefaultServiceVersion(String defaultServiceVersion) {
        this.innerModel().withDefaultServiceVersion(defaultServiceVersion);
        return this;
    }

    @Override
    public BlobServicePropertiesImpl withDeleteRetentionPolicy(DeleteRetentionPolicy deleteRetentionPolicy) {
        this.innerModel().withDeleteRetentionPolicy(deleteRetentionPolicy);
        return this;
    }

    @Override
    public BlobServicePropertiesImpl withDeleteRetentionPolicyEnabled(int numDaysEnabled) {
        this.innerModel().withDeleteRetentionPolicy(new DeleteRetentionPolicy().withEnabled(true).withDays(numDaysEnabled));
        return this;
    }

    @Override
    public BlobServicePropertiesImpl withDeleteRetentionPolicyDisabled() {
        this.innerModel().withDeleteRetentionPolicy(new DeleteRetentionPolicy().withEnabled(false));
        return this;
    }

    @Override
    public BlobServicePropertiesImpl withBlobVersioningEnabled() {
        this.innerModel().withIsVersioningEnabled(true);
        return this;
    }

    @Override
    public BlobServicePropertiesImpl withBlobVersioningDisabled() {
        this.innerModel().withIsVersioningEnabled(false);
        return this;
    }

    @Override
    public BlobServicePropertiesImpl withContainerDeleteRetentionPolicy(DeleteRetentionPolicy deleteRetentionPolicy) {
        this.innerModel().withContainerDeleteRetentionPolicy(deleteRetentionPolicy);
        return this;
    }

    @Override
    public BlobServicePropertiesImpl withContainerDeleteRetentionPolicyEnabled(int numDaysEnabled) {
        this.innerModel().withContainerDeleteRetentionPolicy(new DeleteRetentionPolicy().withEnabled(true).withDays(numDaysEnabled));
        return this;
    }

    @Override
    public BlobServicePropertiesImpl withContainerDeleteRetentionPolicyDisabled() {
        this.innerModel().withContainerDeleteRetentionPolicy(new DeleteRetentionPolicy().withEnabled(false));
        return this;
    }

    @Override
    public BlobServicePropertiesImpl withLastAccessTimeTrackingPolicyEnabled() {
        if (this.innerModel().lastAccessTimeTrackingPolicy() == null) {
            this.innerModel().withLastAccessTimeTrackingPolicy(new LastAccessTimeTrackingPolicy());
        }
        this.innerModel().lastAccessTimeTrackingPolicy().withEnable(true);
        return this;
    }

    @Override
    public BlobServicePropertiesImpl withLastAccessTimeTrackingPolicy(LastAccessTimeTrackingPolicy policy) {
        this.innerModel().withLastAccessTimeTrackingPolicy(policy);
        return this;
    }

    @Override
    public BlobServicePropertiesImpl withLastAccessTimeTrackingPolicyDisabled() {
        if (this.innerModel().lastAccessTimeTrackingPolicy() != null) {
            this.innerModel().lastAccessTimeTrackingPolicy().withEnable(false);
        }
        return this;
    }
}

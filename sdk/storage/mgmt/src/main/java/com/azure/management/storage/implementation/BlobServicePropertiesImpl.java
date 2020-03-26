/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.storage.implementation;

import com.azure.management.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import com.azure.management.storage.BlobServiceProperties;
import com.azure.management.storage.CorsRule;
import com.azure.management.storage.CorsRules;
import com.azure.management.storage.DeleteRetentionPolicy;
import com.azure.management.storage.models.BlobServicePropertiesInner;
import com.azure.management.storage.models.BlobServicesInner;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

class BlobServicePropertiesImpl extends CreatableUpdatableImpl<BlobServiceProperties, BlobServicePropertiesInner, BlobServicePropertiesImpl> implements BlobServiceProperties, BlobServiceProperties.Definition, BlobServiceProperties.Update {
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
        super(inner.getName(), inner);
        this.manager = manager;
        // Set resource name
        this.accountName = inner.getName();
        // set resource ancestor and positional variables
        this.resourceGroupName = IdParsingUtils.getValueFromIdByName(inner.getId(), "resourceGroups");
        this.accountName = IdParsingUtils.getValueFromIdByName(inner.getId(), "storageAccounts");
        //
    }

    @Override
    public StorageManager manager() {
        return this.manager;
    }

    @Override
    public Mono<BlobServiceProperties> createResourceAsync() {
        BlobServicesInner client = this.manager().inner().blobServices();
        return client.setServicePropertiesAsync(this.resourceGroupName, this.accountName, this.inner())
                .map(innerToFluentMap(this));
    }

    @Override
    public Mono<BlobServiceProperties> updateResourceAsync() {
        BlobServicesInner client = this.manager().inner().blobServices();
        return client.setServicePropertiesAsync(this.resourceGroupName, this.accountName, this.inner())
                .map(innerToFluentMap(this));
    }

    @Override
    protected Mono<BlobServicePropertiesInner> getInnerAsync() {
        BlobServicesInner client = this.manager().inner().blobServices();
        return client.getServicePropertiesAsync(this.resourceGroupName, this.accountName);
    }

    @Override
    public boolean isInCreateMode() {
        return this.inner().getId() == null;
    }


    @Override
    public CorsRules cors() {
        return this.inner().getCors();
    }

    @Override
    public String defaultServiceVersion() {
        return this.inner().getDefaultServiceVersion();
    }

    @Override
    public DeleteRetentionPolicy deleteRetentionPolicy() {
        return this.inner().getDeleteRetentionPolicy();
    }

    @Override
    public String id() {
        return this.inner().getId();
    }

    @Override
    public String name() {
        return this.inner().getName();
    }

    @Override
    public String type() {
        return this.inner().getType();
    }

    @Override
    public BlobServicePropertiesImpl withExistingStorageAccount(String resourceGroupName, String accountName) {
        this.resourceGroupName = resourceGroupName;
        this.accountName = accountName;
        return this;
    }

    @Override
    public BlobServicePropertiesImpl withCORSRules(List<CorsRule> corsRules) {
        this.inner().setCors(new CorsRules().setCorsRules(corsRules));
        return this;
    }

    @Override
    public BlobServicePropertiesImpl withCORSRule(CorsRule corsRule) {
        CorsRules corsRules = this.inner().getCors();
        if (corsRules == null) {
            List<CorsRule> firstCorsRule = new ArrayList<>();
            firstCorsRule.add(corsRule);
            this.inner().setCors(new CorsRules().setCorsRules(firstCorsRule));
        } else {
            List<CorsRule> currentCorsRules = corsRules.getCorsRules();
            currentCorsRules.add(corsRule);
            this.inner().setCors(corsRules.setCorsRules(currentCorsRules));
        }
        return this;
    }

    @Override
    public BlobServicePropertiesImpl withDefaultServiceVersion(String defaultServiceVersion) {
        this.inner().setDefaultServiceVersion(defaultServiceVersion);
        return this;
    }

    @Override
    public BlobServicePropertiesImpl withDeleteRetentionPolicy(DeleteRetentionPolicy deleteRetentionPolicy) {
        this.inner().setDeleteRetentionPolicy(deleteRetentionPolicy);
        return this;
    }

    @Override
    public BlobServicePropertiesImpl withDeleteRetentionPolicyEnabled(int numDaysEnabled) {
        this.inner().setDeleteRetentionPolicy(new DeleteRetentionPolicy().setEnabled(true).setDays(numDaysEnabled));
        return this;
    }

    @Override
    public BlobServicePropertiesImpl withDeleteRetentionPolicyDisabled() {
        this.inner().setDeleteRetentionPolicy(new DeleteRetentionPolicy().setEnabled(false));
        return this;
    }
}
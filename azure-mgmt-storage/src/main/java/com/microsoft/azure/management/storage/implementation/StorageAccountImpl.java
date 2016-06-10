/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.storage.KeyType;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.implementation.api.AccountType;
import com.microsoft.azure.management.storage.implementation.api.CustomDomain;
import com.microsoft.azure.management.storage.implementation.api.ProvisioningState;
import com.microsoft.azure.management.storage.implementation.api.StorageAccountCreateParametersInner;
import com.microsoft.azure.management.storage.implementation.api.StorageAccountInner;
import com.microsoft.azure.management.storage.implementation.api.StorageAccountKeysInner;
import com.microsoft.azure.management.storage.implementation.api.StorageAccountUpdateParametersInner;
import com.microsoft.azure.management.storage.implementation.api.StorageAccountsInner;
import com.microsoft.rest.ServiceResponse;
import org.joda.time.DateTime;

import java.io.IOException;

/**
 * Implementation for StorageAccount and its parent interfaces.
 */
class StorageAccountImpl
        extends GroupableResourceImpl<StorageAccount, StorageAccountInner, StorageAccountImpl>
        implements
        StorageAccount,
        StorageAccount.Definitions,
        StorageAccount.Update {

    private PublicEndpoints publicEndpoints;
    private AccountStatuses accountStatuses;
    private String name;

    private final StorageAccountsInner client;

    StorageAccountImpl(String name,
                              StorageAccountInner innerModel,
                              final StorageAccountsInner client,
                              final ResourceGroups resourceGroups) {
        super(name, innerModel, resourceGroups);
        this.name = name;
        this.client = client;
    }

    @Override
    public AccountStatuses accountStatuses() {
        if (accountStatuses == null) {
            accountStatuses = new AccountStatuses(this.inner().statusOfPrimary(), this.inner().statusOfSecondary());
        }
        return accountStatuses;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public AccountType accountType() {
        return this.inner().accountType();
    }

    @Override
    public DateTime creationTime() {
        return this.inner().creationTime();
    }

    @Override
    public CustomDomain customDomain() {
        return this.inner().customDomain();
    }

    @Override
    public DateTime lastGeoFailoverTime() {
        return this.inner().lastGeoFailoverTime();
    }

    @Override
    public ProvisioningState provisioningState() {
        return this.inner().provisioningState();
    }

    @Override
    public PublicEndpoints endPoints() {
        if (publicEndpoints == null) {
            publicEndpoints = new PublicEndpoints(this.inner().primaryEndpoints(), this.inner().secondaryEndpoints());
        }
        return publicEndpoints;
    }

    @Override
    public StorageAccountKeys getKeys() throws CloudException, IOException {
        ServiceResponse<StorageAccountKeysInner> response =
                this.client.listKeys(this.resourceGroupName(), this.key);
        StorageAccountKeysInner stroageAccountKeysInner = response.getBody();
        return new StorageAccountKeys(stroageAccountKeysInner.key1(), stroageAccountKeysInner.key2());
    }

    @Override
    public StorageAccountKeys regenerateKey(KeyType keyType) throws CloudException, IOException {
        ServiceResponse<StorageAccountKeysInner> response =
                this.client.regenerateKey(this.resourceGroupName(), this.key, keyType.toString());
        StorageAccountKeysInner stroageAccountKeysInner = response.getBody();
        return new StorageAccountKeys(stroageAccountKeysInner.key1(), stroageAccountKeysInner.key2());
    }

    @Override
    public StorageAccount refresh() throws Exception {
        ServiceResponse<StorageAccountInner> response =
            this.client.getProperties(this.resourceGroupName(), this.key);
        StorageAccountInner storageAccountInner = response.getBody();
        this.setInner(storageAccountInner);
        clearWrapperProperties();
        return this;
    }

    @Override
    public StorageAccountImpl create() throws Exception {
        super.creatablesCreate();
        return this;
    }

    @Override
    public StorageAccountImpl withAccountType(AccountType accountType) {
        this.inner().withAccountType(accountType);
        return this;
    }

    private void clearWrapperProperties() {
        accountStatuses = null;
        publicEndpoints = null;
    }

    @Override
    protected void createResource() throws Exception {
        StorageAccountCreateParametersInner createParameters = new StorageAccountCreateParametersInner();
        createParameters.withAccountType(this.inner().accountType());
        createParameters.withLocation(this.region());
        createParameters.withTags(this.inner().getTags());
        this.client.create(this.resourceGroupName(), this.name(), createParameters);
        // create response does not seems including the endpoints so fetching it again.
        StorageAccountInner storageAccountInner = this.client
                .getProperties(this.resourceGroupName(), this.name())
                .getBody();
        this.setInner(storageAccountInner);
        clearWrapperProperties();
    }

    @Override
    public Update update() throws Exception {
        return this;
    }

    @Override
    public StorageAccount apply() throws Exception {
        StorageAccountUpdateParametersInner updateParameters = new StorageAccountUpdateParametersInner();
        updateParameters.withAccountType(accountType());
        updateParameters.withCustomDomain(customDomain());
        this.setInner(client.update(resourceGroupName(), name(), updateParameters).getBody());
        return this;
    }

    @Override
    public Update withCustomDomain(CustomDomain customDomain) {
        inner().withCustomDomain(customDomain);
        return this;
    }

    @Override
    public Update withCustomDomain(String name) {
        return withCustomDomain(new CustomDomain().withName(name));
    }

    @Override
    public Update withCustomDomain(String name, boolean useSubDomain) {
        return withCustomDomain(new CustomDomain().withName(name).withUseSubDomain(useSubDomain));
    }
}

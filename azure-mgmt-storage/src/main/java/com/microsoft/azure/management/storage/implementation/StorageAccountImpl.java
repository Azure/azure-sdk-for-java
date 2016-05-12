package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.model.Provisionable;
import com.microsoft.azure.management.storage.AccountStatuses;
import com.microsoft.azure.management.storage.KeyType;
import com.microsoft.azure.management.storage.PublicEndpoints;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.implementation.api.*;
import com.microsoft.rest.ServiceResponse;
import org.joda.time.DateTime;

import java.io.IOException;

class StorageAccountImpl
        extends GroupableResourceImpl<StorageAccount, StorageAccountInner, StorageAccountImpl>
        implements
        StorageAccount,
        StorageAccount.DefinitionBlank,
        StorageAccount.DefinitionWithGroup,
        StorageAccount.DefinitionProvisionable
        {

    private PublicEndpoints publicEndpoints;
    private AccountStatuses accountStatuses;
    private String name;

    private final StorageAccountsInner client;

    StorageAccountImpl(String name,
                              StorageAccountInner innerModel,
                              final StorageAccountsInner client,
                              final ResourceGroups resourceGroups) {
        super(innerModel.id(), innerModel, resourceGroups);
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
                this.client.listKeys(this.groupName, this.id);
        StorageAccountKeysInner stroageAccountKeysInner = response.getBody();
        return new StorageAccountKeys(stroageAccountKeysInner.key1(), stroageAccountKeysInner.key2());
    }

    @Override
    public StorageAccountKeys regenerateKey(KeyType keyType) throws CloudException, IOException {
        ServiceResponse<StorageAccountKeysInner> response =
                this.client.regenerateKey(this.groupName, this.id, keyType.toString());
        StorageAccountKeysInner stroageAccountKeysInner = response.getBody();
        return new StorageAccountKeys(stroageAccountKeysInner.key1(), stroageAccountKeysInner.key2());
    }

    @Override
    public StorageAccount refresh() throws Exception {
        ServiceResponse<StorageAccountInner> response =
            this.client.getProperties(this.groupName, this.id);
        StorageAccountInner storageAccountInner = response.getBody();
        this.setInner(storageAccountInner);
        clearWrapperProperties();
        return this;
    }

    @Override
    public StorageAccountImpl provision() throws Exception {
        // Prerequisites
        for (Provisionable<?> provisionable : prerequisites()) {
            provisionable.provision();
        }

        StorageAccountCreateParametersInner createParameters = new StorageAccountCreateParametersInner();
        createParameters.setAccountType(this.inner().accountType());
        createParameters.setLocation(this.location());
        createParameters.setTags(this.inner().getTags());

        ServiceResponse<StorageAccountInner> response =
                this.client.create(this.resourceGroupName(), this.name(), createParameters);
        StorageAccountInner storageAccountInner = response.getBody();
        this.setInner(storageAccountInner);
        clearWrapperProperties();
        return this;
    }

    @Override
    public StorageAccountImpl withAccountType(AccountType accountType) {
        this.inner().setAccountType(accountType);
        return this;
    }

    private void clearWrapperProperties() {
        accountStatuses = null;
        publicEndpoints = null;
    }
}

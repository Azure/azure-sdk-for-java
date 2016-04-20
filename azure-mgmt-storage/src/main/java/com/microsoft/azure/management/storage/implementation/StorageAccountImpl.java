package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.storage.implementation.api.StorageManagementClientImpl;
import com.microsoft.azure.management.storage.AccountStatuses;
import com.microsoft.azure.management.storage.KeyType;
import com.microsoft.azure.management.storage.PublicEndpoints;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.implementation.api.*;
import com.microsoft.rest.ServiceResponse;
import org.joda.time.DateTime;

import java.io.IOException;

public class StorageAccountImpl
        extends GroupableResourceImpl<StorageAccount, StorageAccountInner, StorageAccountImpl>
        implements
        StorageAccount,
        StorageAccount.DefinitionBlank,
        StorageAccount.DefinitionWithGroup,
        StorageAccount.DefinitionWithGroupContextBlank,
        StorageAccount.DefinitionProvisionable
        {

    private PublicEndpoints publicEndpoints;
    private AccountStatuses accountStatuses;
    private String name;

    private final StorageManagementClientImpl storageManagmentClient;

    public StorageAccountImpl(String name, StorageAccountInner innerObject, StorageManagementClientImpl storageManagmentClient, ResourceGroups resourceGroups) {
        super(innerObject.id(), innerObject, resourceGroups);
        this.name = name;
        this.storageManagmentClient = storageManagmentClient;
    }

    public StorageAccountImpl(String name, StorageAccountInner innerObject, StorageManagementClientImpl storageManagmentClient, ResourceGroup resourceGroup) {
        super(innerObject.id(), innerObject, resourceGroup);
        this.name = name;
        this.storageManagmentClient = storageManagmentClient;
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
                this.storageManagmentClient.storageAccounts().listKeys(this.groupName, this.id);
        StorageAccountKeysInner stroageAccountKeysInner = response.getBody();
        return new StorageAccountKeys(stroageAccountKeysInner.key1(), stroageAccountKeysInner.key2());
    }

    @Override
    public StorageAccountKeys regenerateKey(KeyType keyType) throws CloudException, IOException {
        ServiceResponse<StorageAccountKeysInner> response =
                this.storageManagmentClient.storageAccounts().regenerateKey(this.groupName, this.id, keyType.toString());
        StorageAccountKeysInner stroageAccountKeysInner = response.getBody();
        return new StorageAccountKeys(stroageAccountKeysInner.key1(), stroageAccountKeysInner.key2());
    }

    @Override
    public StorageAccount refresh() throws Exception {
        ServiceResponse<StorageAccountInner> response =
            this.storageManagmentClient.storageAccounts().getProperties(this.groupName, this.id);
        StorageAccountInner storageAccountInner = response.getBody();
        this.setInner(storageAccountInner);
        clearWrapperProperties();
        return this;
    }

    @Override
    public StorageAccountImpl provision() throws Exception {
        ensureGroup();
        StorageAccountCreateParametersInner createParameters = new StorageAccountCreateParametersInner();
        createParameters.setAccountType(this.inner().accountType());
        createParameters.setLocation(this.region());
        createParameters.setTags(this.inner().getTags());

        ServiceResponse<StorageAccountInner> response =
                this.storageManagmentClient.storageAccounts().create(this.groupName, this.name(), createParameters);
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

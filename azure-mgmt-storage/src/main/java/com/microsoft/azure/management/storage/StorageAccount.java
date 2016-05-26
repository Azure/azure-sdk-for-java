package com.microsoft.azure.management.storage;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.storage.implementation.StorageAccountKeys;
import com.microsoft.azure.management.storage.implementation.api.AccountType;
import com.microsoft.azure.management.storage.implementation.api.CustomDomain;
import com.microsoft.azure.management.storage.implementation.api.ProvisioningState;
import com.microsoft.azure.management.storage.implementation.api.StorageAccountInner;
import org.joda.time.DateTime;

import java.io.IOException;

public interface StorageAccount extends
        GroupableResource,
        Refreshable<StorageAccount>,
        Wrapper<StorageAccountInner> {

    /***********************************************************
     * Getters
     ***********************************************************/

    /**
     * Gets the status indicating whether the primary and secondary location of
     * the storage account is available or unavailable. Possible values include:
     * 'Available', 'Unavailable'.
     */
    AccountStatuses accountStatuses();

    /**
     * Gets the type of this storage account. Possible values include:
     * 'Standard_LRS', 'Standard_ZRS', 'Standard_GRS', 'Standard_RAGRS',
     * 'Premium_LRS'.
     */
    AccountType accountType();

    /**
     * Gets the creation date and time of the storage account in UTC.
     */
    DateTime creationTime();

    /**
     * Gets the user assigned custom domain assigned to this storage account.
     */
    CustomDomain customDomain();

    /**
     * Gets the timestamp of the most recent instance of a failover to the
     * secondary location. Only the most recent timestamp is retained. This
     * element is not returned if there has never been a failover instance.
     * Only available if the accountType is StandardGRS or StandardRAGRS.
     */
    DateTime lastGeoFailoverTime();

    /**
     * Gets the status of the storage account at the time the operation was
     * called. Possible values include: 'Creating', 'ResolvingDNS',
     * 'Succeeded'.
     */
    ProvisioningState provisioningState();

    /**
     * Gets the URLs that are used to perform a retrieval of a public blob,
     * queue or table object. Note that StandardZRS and PremiumLRS accounts
     * only return the blob endpoint.
     */
    PublicEndpoints endPoints();

    /**
     * Gets the access keys for this storage account.
     *
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     */
    StorageAccountKeys getKeys() throws CloudException, IOException;

    /**
     * Regenerates the access keys for this storage account.
     *
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     */
    StorageAccountKeys regenerateKey(KeyType keyType) throws CloudException, IOException;

    /**************************************************************
     * Fluent interfaces to provision a StorageAccount
     **************************************************************/

    /**
     * Container interface for all the definitions
     */
    public interface Definitions extends 
        DefinitionBlank,
        DefinitionWithGroup,
        DefinitionAfterGroup,
        DefinitionCreatable {
    }
    
    public interface DefinitionBlank extends GroupableResource.DefinitionWithRegion<DefinitionWithGroup> {
    }

    public interface DefinitionWithGroup extends GroupableResource.DefinitionWithGroup<DefinitionAfterGroup> {
    }

    public interface DefinitionAfterGroup extends DefinitionCreatable {
    }
    
    public interface DefinitionCreatable extends Creatable<StorageAccount> {
        DefinitionCreatable withAccountType(AccountType accountType);
    }
}


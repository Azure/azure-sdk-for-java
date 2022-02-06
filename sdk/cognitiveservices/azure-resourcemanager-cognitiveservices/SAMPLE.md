# Code snippets and samples


## Accounts

- [Create](#accounts_create)
- [Delete](#accounts_delete)
- [GetByResourceGroup](#accounts_getbyresourcegroup)
- [List](#accounts_list)
- [ListByResourceGroup](#accounts_listbyresourcegroup)
- [ListKeys](#accounts_listkeys)
- [ListSkus](#accounts_listskus)
- [ListUsages](#accounts_listusages)
- [RegenerateKey](#accounts_regeneratekey)
- [Update](#accounts_update)

## CommitmentPlans

- [CreateOrUpdate](#commitmentplans_createorupdate)
- [Delete](#commitmentplans_delete)
- [Get](#commitmentplans_get)
- [List](#commitmentplans_list)

## CommitmentTiers

- [List](#commitmenttiers_list)

## DeletedAccounts

- [Get](#deletedaccounts_get)
- [List](#deletedaccounts_list)
- [Purge](#deletedaccounts_purge)

## Deployments

- [CreateOrUpdate](#deployments_createorupdate)
- [Delete](#deployments_delete)
- [Get](#deployments_get)
- [List](#deployments_list)

## Operations

- [List](#operations_list)

## PrivateEndpointConnections

- [CreateOrUpdate](#privateendpointconnections_createorupdate)
- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [List](#privateendpointconnections_list)

## PrivateLinkResources

- [List](#privatelinkresources_list)

## ResourceProvider

- [CheckDomainAvailability](#resourceprovider_checkdomainavailability)
- [CheckSkuAvailability](#resourceprovider_checkskuavailability)

## ResourceSkus

- [List](#resourceskus_list)
### Accounts_Create

```java
import com.azure.resourcemanager.cognitiveservices.models.AccountProperties;
import com.azure.resourcemanager.cognitiveservices.models.Encryption;
import com.azure.resourcemanager.cognitiveservices.models.Identity;
import com.azure.resourcemanager.cognitiveservices.models.KeySource;
import com.azure.resourcemanager.cognitiveservices.models.KeyVaultProperties;
import com.azure.resourcemanager.cognitiveservices.models.ResourceIdentityType;
import com.azure.resourcemanager.cognitiveservices.models.Sku;
import com.azure.resourcemanager.cognitiveservices.models.UserOwnedStorage;
import java.util.Arrays;

/** Samples for Accounts Create. */
public final class AccountsCreateSamples {
    /*
     * x-ms-original-file: specification/cognitiveservices/resource-manager/Microsoft.CognitiveServices/stable/2021-10-01/examples/CreateAccountMin.json
     */
    /**
     * Sample code: Create Account Min.
     *
     * @param manager Entry point to CognitiveServicesManager.
     */
    public static void createAccountMin(com.azure.resourcemanager.cognitiveservices.CognitiveServicesManager manager) {
        manager
            .accounts()
            .define("testCreate1")
            .withExistingResourceGroup("myResourceGroup")
            .withRegion("West US")
            .withKind("CognitiveServices")
            .withSku(new Sku().withName("S0"))
            .withIdentity(new Identity().withType(ResourceIdentityType.SYSTEM_ASSIGNED))
            .withProperties(new AccountProperties())
            .create();
    }

    /*
     * x-ms-original-file: specification/cognitiveservices/resource-manager/Microsoft.CognitiveServices/stable/2021-10-01/examples/CreateAccount.json
     */
    /**
     * Sample code: Create Account.
     *
     * @param manager Entry point to CognitiveServicesManager.
     */
    public static void createAccount(com.azure.resourcemanager.cognitiveservices.CognitiveServicesManager manager) {
        manager
            .accounts()
            .define("testCreate1")
            .withExistingResourceGroup("myResourceGroup")
            .withRegion("West US")
            .withKind("Emotion")
            .withSku(new Sku().withName("S0"))
            .withIdentity(new Identity().withType(ResourceIdentityType.SYSTEM_ASSIGNED))
            .withProperties(
                new AccountProperties()
                    .withEncryption(
                        new Encryption()
                            .withKeyVaultProperties(
                                new KeyVaultProperties()
                                    .withKeyName("KeyName")
                                    .withKeyVersion("891CF236-D241-4738-9462-D506AF493DFA")
                                    .withKeyVaultUri("https://pltfrmscrts-use-pc-dev.vault.azure.net/"))
                            .withKeySource(KeySource.MICROSOFT_KEY_VAULT))
                    .withUserOwnedStorage(
                        Arrays
                            .asList(
                                new UserOwnedStorage()
                                    .withResourceId(
                                        "/subscriptions/xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx/resourceGroups/myResourceGroup/providers/Microsoft.Storage/storageAccounts/myStorageAccount"))))
            .create();
    }
}
```

### Accounts_Delete

```java
import com.azure.core.util.Context;

/** Samples for Accounts Delete. */
public final class AccountsDeleteSamples {
    /*
     * x-ms-original-file: specification/cognitiveservices/resource-manager/Microsoft.CognitiveServices/stable/2021-10-01/examples/DeleteAccount.json
     */
    /**
     * Sample code: Delete Account.
     *
     * @param manager Entry point to CognitiveServicesManager.
     */
    public static void deleteAccount(com.azure.resourcemanager.cognitiveservices.CognitiveServicesManager manager) {
        manager.accounts().delete("myResourceGroup", "PropTest01", Context.NONE);
    }
}
```

### Accounts_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Accounts GetByResourceGroup. */
public final class AccountsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/cognitiveservices/resource-manager/Microsoft.CognitiveServices/stable/2021-10-01/examples/GetAccount.json
     */
    /**
     * Sample code: Get Account.
     *
     * @param manager Entry point to CognitiveServicesManager.
     */
    public static void getAccount(com.azure.resourcemanager.cognitiveservices.CognitiveServicesManager manager) {
        manager.accounts().getByResourceGroupWithResponse("myResourceGroup", "myAccount", Context.NONE);
    }
}
```

### Accounts_List

```java
import com.azure.core.util.Context;

/** Samples for Accounts List. */
public final class AccountsListSamples {
    /*
     * x-ms-original-file: specification/cognitiveservices/resource-manager/Microsoft.CognitiveServices/stable/2021-10-01/examples/ListAccountsBySubscription.json
     */
    /**
     * Sample code: List Accounts by Subscription.
     *
     * @param manager Entry point to CognitiveServicesManager.
     */
    public static void listAccountsBySubscription(
        com.azure.resourcemanager.cognitiveservices.CognitiveServicesManager manager) {
        manager.accounts().list(Context.NONE);
    }
}
```

### Accounts_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Accounts ListByResourceGroup. */
public final class AccountsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/cognitiveservices/resource-manager/Microsoft.CognitiveServices/stable/2021-10-01/examples/ListAccountsByResourceGroup.json
     */
    /**
     * Sample code: List Accounts by Resource Group.
     *
     * @param manager Entry point to CognitiveServicesManager.
     */
    public static void listAccountsByResourceGroup(
        com.azure.resourcemanager.cognitiveservices.CognitiveServicesManager manager) {
        manager.accounts().listByResourceGroup("myResourceGroup", Context.NONE);
    }
}
```

### Accounts_ListKeys

```java
import com.azure.core.util.Context;

/** Samples for Accounts ListKeys. */
public final class AccountsListKeysSamples {
    /*
     * x-ms-original-file: specification/cognitiveservices/resource-manager/Microsoft.CognitiveServices/stable/2021-10-01/examples/ListKeys.json
     */
    /**
     * Sample code: List Keys.
     *
     * @param manager Entry point to CognitiveServicesManager.
     */
    public static void listKeys(com.azure.resourcemanager.cognitiveservices.CognitiveServicesManager manager) {
        manager.accounts().listKeysWithResponse("myResourceGroup", "myAccount", Context.NONE);
    }
}
```

### Accounts_ListSkus

```java
import com.azure.core.util.Context;

/** Samples for Accounts ListSkus. */
public final class AccountsListSkusSamples {
    /*
     * x-ms-original-file: specification/cognitiveservices/resource-manager/Microsoft.CognitiveServices/stable/2021-10-01/examples/ListSkus.json
     */
    /**
     * Sample code: List SKUs.
     *
     * @param manager Entry point to CognitiveServicesManager.
     */
    public static void listSKUs(com.azure.resourcemanager.cognitiveservices.CognitiveServicesManager manager) {
        manager.accounts().listSkusWithResponse("myResourceGroup", "myAccount", Context.NONE);
    }
}
```

### Accounts_ListUsages

```java
import com.azure.core.util.Context;

/** Samples for Accounts ListUsages. */
public final class AccountsListUsagesSamples {
    /*
     * x-ms-original-file: specification/cognitiveservices/resource-manager/Microsoft.CognitiveServices/stable/2021-10-01/examples/GetUsages.json
     */
    /**
     * Sample code: Get Usages.
     *
     * @param manager Entry point to CognitiveServicesManager.
     */
    public static void getUsages(com.azure.resourcemanager.cognitiveservices.CognitiveServicesManager manager) {
        manager.accounts().listUsagesWithResponse("myResourceGroup", "TestUsage02", null, Context.NONE);
    }
}
```

### Accounts_RegenerateKey

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.cognitiveservices.models.KeyName;
import com.azure.resourcemanager.cognitiveservices.models.RegenerateKeyParameters;

/** Samples for Accounts RegenerateKey. */
public final class AccountsRegenerateKeySamples {
    /*
     * x-ms-original-file: specification/cognitiveservices/resource-manager/Microsoft.CognitiveServices/stable/2021-10-01/examples/RegenerateKey.json
     */
    /**
     * Sample code: Regenerate Keys.
     *
     * @param manager Entry point to CognitiveServicesManager.
     */
    public static void regenerateKeys(com.azure.resourcemanager.cognitiveservices.CognitiveServicesManager manager) {
        manager
            .accounts()
            .regenerateKeyWithResponse(
                "myResourceGroup", "myAccount", new RegenerateKeyParameters().withKeyName(KeyName.KEY2), Context.NONE);
    }
}
```

### Accounts_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.cognitiveservices.models.Account;
import com.azure.resourcemanager.cognitiveservices.models.Sku;

/** Samples for Accounts Update. */
public final class AccountsUpdateSamples {
    /*
     * x-ms-original-file: specification/cognitiveservices/resource-manager/Microsoft.CognitiveServices/stable/2021-10-01/examples/UpdateAccount.json
     */
    /**
     * Sample code: Update Account.
     *
     * @param manager Entry point to CognitiveServicesManager.
     */
    public static void updateAccount(com.azure.resourcemanager.cognitiveservices.CognitiveServicesManager manager) {
        Account resource =
            manager.accounts().getByResourceGroupWithResponse("bvttest", "bingSearch", Context.NONE).getValue();
        resource.update().withSku(new Sku().withName("S2")).apply();
    }
}
```

### CommitmentPlans_CreateOrUpdate

```java
import com.azure.resourcemanager.cognitiveservices.models.CommitmentPeriod;
import com.azure.resourcemanager.cognitiveservices.models.CommitmentPlanProperties;
import com.azure.resourcemanager.cognitiveservices.models.HostingModel;

/** Samples for CommitmentPlans CreateOrUpdate. */
public final class CommitmentPlansCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/cognitiveservices/resource-manager/Microsoft.CognitiveServices/stable/2021-10-01/examples/PutCommitmentPlan.json
     */
    /**
     * Sample code: PutCommitmentPlan.
     *
     * @param manager Entry point to CognitiveServicesManager.
     */
    public static void putCommitmentPlan(com.azure.resourcemanager.cognitiveservices.CognitiveServicesManager manager) {
        manager
            .commitmentPlans()
            .define("commitmentPlanName")
            .withExistingAccount("resourceGroupName", "accountName")
            .withProperties(
                new CommitmentPlanProperties()
                    .withHostingModel(HostingModel.WEB)
                    .withPlanType("Speech2Text")
                    .withCurrent(new CommitmentPeriod().withTier("T1"))
                    .withAutoRenew(true))
            .create();
    }
}
```

### CommitmentPlans_Delete

```java
import com.azure.core.util.Context;

/** Samples for CommitmentPlans Delete. */
public final class CommitmentPlansDeleteSamples {
    /*
     * x-ms-original-file: specification/cognitiveservices/resource-manager/Microsoft.CognitiveServices/stable/2021-10-01/examples/DeleteCommitmentPlan.json
     */
    /**
     * Sample code: DeleteCommitmentPlan.
     *
     * @param manager Entry point to CognitiveServicesManager.
     */
    public static void deleteCommitmentPlan(
        com.azure.resourcemanager.cognitiveservices.CognitiveServicesManager manager) {
        manager.commitmentPlans().delete("resourceGroupName", "accountName", "commitmentPlanName", Context.NONE);
    }
}
```

### CommitmentPlans_Get

```java
import com.azure.core.util.Context;

/** Samples for CommitmentPlans Get. */
public final class CommitmentPlansGetSamples {
    /*
     * x-ms-original-file: specification/cognitiveservices/resource-manager/Microsoft.CognitiveServices/stable/2021-10-01/examples/GetCommitmentPlan.json
     */
    /**
     * Sample code: GetCommitmentPlan.
     *
     * @param manager Entry point to CognitiveServicesManager.
     */
    public static void getCommitmentPlan(com.azure.resourcemanager.cognitiveservices.CognitiveServicesManager manager) {
        manager
            .commitmentPlans()
            .getWithResponse("resourceGroupName", "accountName", "commitmentPlanName", Context.NONE);
    }
}
```

### CommitmentPlans_List

```java
import com.azure.core.util.Context;

/** Samples for CommitmentPlans List. */
public final class CommitmentPlansListSamples {
    /*
     * x-ms-original-file: specification/cognitiveservices/resource-manager/Microsoft.CognitiveServices/stable/2021-10-01/examples/ListCommitmentPlans.json
     */
    /**
     * Sample code: ListCommitmentPlans.
     *
     * @param manager Entry point to CognitiveServicesManager.
     */
    public static void listCommitmentPlans(
        com.azure.resourcemanager.cognitiveservices.CognitiveServicesManager manager) {
        manager.commitmentPlans().list("resourceGroupName", "accountName", Context.NONE);
    }
}
```

### CommitmentTiers_List

```java
import com.azure.core.util.Context;

/** Samples for CommitmentTiers List. */
public final class CommitmentTiersListSamples {
    /*
     * x-ms-original-file: specification/cognitiveservices/resource-manager/Microsoft.CognitiveServices/stable/2021-10-01/examples/ListCommitmentTiers.json
     */
    /**
     * Sample code: ListCommitmentTiers.
     *
     * @param manager Entry point to CognitiveServicesManager.
     */
    public static void listCommitmentTiers(
        com.azure.resourcemanager.cognitiveservices.CognitiveServicesManager manager) {
        manager.commitmentTiers().list("location", Context.NONE);
    }
}
```

### DeletedAccounts_Get

```java
import com.azure.core.util.Context;

/** Samples for DeletedAccounts Get. */
public final class DeletedAccountsGetSamples {
    /*
     * x-ms-original-file: specification/cognitiveservices/resource-manager/Microsoft.CognitiveServices/stable/2021-10-01/examples/GetDeletedAccount.json
     */
    /**
     * Sample code: Get Account.
     *
     * @param manager Entry point to CognitiveServicesManager.
     */
    public static void getAccount(com.azure.resourcemanager.cognitiveservices.CognitiveServicesManager manager) {
        manager.deletedAccounts().getWithResponse("westus", "myResourceGroup", "myAccount", Context.NONE);
    }
}
```

### DeletedAccounts_List

```java
import com.azure.core.util.Context;

/** Samples for DeletedAccounts List. */
public final class DeletedAccountsListSamples {
    /*
     * x-ms-original-file: specification/cognitiveservices/resource-manager/Microsoft.CognitiveServices/stable/2021-10-01/examples/ListAccountsBySubscription.json
     */
    /**
     * Sample code: List Deleted Accounts by Subscription.
     *
     * @param manager Entry point to CognitiveServicesManager.
     */
    public static void listDeletedAccountsBySubscription(
        com.azure.resourcemanager.cognitiveservices.CognitiveServicesManager manager) {
        manager.deletedAccounts().list(Context.NONE);
    }
}
```

### DeletedAccounts_Purge

```java
import com.azure.core.util.Context;

/** Samples for DeletedAccounts Purge. */
public final class DeletedAccountsPurgeSamples {
    /*
     * x-ms-original-file: specification/cognitiveservices/resource-manager/Microsoft.CognitiveServices/stable/2021-10-01/examples/PurgeDeletedAccount.json
     */
    /**
     * Sample code: Delete Account.
     *
     * @param manager Entry point to CognitiveServicesManager.
     */
    public static void deleteAccount(com.azure.resourcemanager.cognitiveservices.CognitiveServicesManager manager) {
        manager.deletedAccounts().purge("westus", "myResourceGroup", "PropTest01", Context.NONE);
    }
}
```

### Deployments_CreateOrUpdate

```java
import com.azure.resourcemanager.cognitiveservices.models.DeploymentModel;
import com.azure.resourcemanager.cognitiveservices.models.DeploymentProperties;
import com.azure.resourcemanager.cognitiveservices.models.DeploymentScaleSettings;
import com.azure.resourcemanager.cognitiveservices.models.DeploymentScaleType;

/** Samples for Deployments CreateOrUpdate. */
public final class DeploymentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/cognitiveservices/resource-manager/Microsoft.CognitiveServices/stable/2021-10-01/examples/PutDeployment.json
     */
    /**
     * Sample code: PutDeployment.
     *
     * @param manager Entry point to CognitiveServicesManager.
     */
    public static void putDeployment(com.azure.resourcemanager.cognitiveservices.CognitiveServicesManager manager) {
        manager
            .deployments()
            .define("deploymentName")
            .withExistingAccount("resourceGroupName", "accountName")
            .withProperties(
                new DeploymentProperties()
                    .withModel(new DeploymentModel().withFormat("OpenAI").withName("ada").withVersion("1"))
                    .withScaleSettings(
                        new DeploymentScaleSettings().withScaleType(DeploymentScaleType.MANUAL).withCapacity(1)))
            .create();
    }
}
```

### Deployments_Delete

```java
import com.azure.core.util.Context;

/** Samples for Deployments Delete. */
public final class DeploymentsDeleteSamples {
    /*
     * x-ms-original-file: specification/cognitiveservices/resource-manager/Microsoft.CognitiveServices/stable/2021-10-01/examples/DeleteDeployment.json
     */
    /**
     * Sample code: DeleteDeployment.
     *
     * @param manager Entry point to CognitiveServicesManager.
     */
    public static void deleteDeployment(com.azure.resourcemanager.cognitiveservices.CognitiveServicesManager manager) {
        manager.deployments().delete("resourceGroupName", "accountName", "deploymentName", Context.NONE);
    }
}
```

### Deployments_Get

```java
import com.azure.core.util.Context;

/** Samples for Deployments Get. */
public final class DeploymentsGetSamples {
    /*
     * x-ms-original-file: specification/cognitiveservices/resource-manager/Microsoft.CognitiveServices/stable/2021-10-01/examples/GetDeployment.json
     */
    /**
     * Sample code: GetDeployment.
     *
     * @param manager Entry point to CognitiveServicesManager.
     */
    public static void getDeployment(com.azure.resourcemanager.cognitiveservices.CognitiveServicesManager manager) {
        manager.deployments().getWithResponse("resourceGroupName", "accountName", "deploymentName", Context.NONE);
    }
}
```

### Deployments_List

```java
import com.azure.core.util.Context;

/** Samples for Deployments List. */
public final class DeploymentsListSamples {
    /*
     * x-ms-original-file: specification/cognitiveservices/resource-manager/Microsoft.CognitiveServices/stable/2021-10-01/examples/ListDeployments.json
     */
    /**
     * Sample code: ListDeployments.
     *
     * @param manager Entry point to CognitiveServicesManager.
     */
    public static void listDeployments(com.azure.resourcemanager.cognitiveservices.CognitiveServicesManager manager) {
        manager.deployments().list("resourceGroupName", "accountName", Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/cognitiveservices/resource-manager/Microsoft.CognitiveServices/stable/2021-10-01/examples/GetOperations.json
     */
    /**
     * Sample code: Get Operations.
     *
     * @param manager Entry point to CognitiveServicesManager.
     */
    public static void getOperations(com.azure.resourcemanager.cognitiveservices.CognitiveServicesManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### PrivateEndpointConnections_CreateOrUpdate

```java
import com.azure.resourcemanager.cognitiveservices.models.PrivateEndpointConnectionProperties;
import com.azure.resourcemanager.cognitiveservices.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.cognitiveservices.models.PrivateLinkServiceConnectionState;

/** Samples for PrivateEndpointConnections CreateOrUpdate. */
public final class PrivateEndpointConnectionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/cognitiveservices/resource-manager/Microsoft.CognitiveServices/stable/2021-10-01/examples/PutPrivateEndpointConnection.json
     */
    /**
     * Sample code: PutPrivateEndpointConnection.
     *
     * @param manager Entry point to CognitiveServicesManager.
     */
    public static void putPrivateEndpointConnection(
        com.azure.resourcemanager.cognitiveservices.CognitiveServicesManager manager) {
        manager
            .privateEndpointConnections()
            .define("{privateEndpointConnectionName}")
            .withExistingAccount("res7687", "sto9699")
            .withProperties(
                new PrivateEndpointConnectionProperties()
                    .withPrivateLinkServiceConnectionState(
                        new PrivateLinkServiceConnectionState()
                            .withStatus(PrivateEndpointServiceConnectionStatus.APPROVED)
                            .withDescription("Auto-Approved")))
            .create();
    }
}
```

### PrivateEndpointConnections_Delete

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections Delete. */
public final class PrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file: specification/cognitiveservices/resource-manager/Microsoft.CognitiveServices/stable/2021-10-01/examples/DeletePrivateEndpointConnection.json
     */
    /**
     * Sample code: DeletePrivateEndpointConnection.
     *
     * @param manager Entry point to CognitiveServicesManager.
     */
    public static void deletePrivateEndpointConnection(
        com.azure.resourcemanager.cognitiveservices.CognitiveServicesManager manager) {
        manager
            .privateEndpointConnections()
            .delete("res6977", "sto2527", "{privateEndpointConnectionName}", Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections Get. */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: specification/cognitiveservices/resource-manager/Microsoft.CognitiveServices/stable/2021-10-01/examples/GetPrivateEndpointConnection.json
     */
    /**
     * Sample code: GetPrivateEndpointConnection.
     *
     * @param manager Entry point to CognitiveServicesManager.
     */
    public static void getPrivateEndpointConnection(
        com.azure.resourcemanager.cognitiveservices.CognitiveServicesManager manager) {
        manager
            .privateEndpointConnections()
            .getWithResponse("res6977", "sto2527", "{privateEndpointConnectionName}", Context.NONE);
    }
}
```

### PrivateEndpointConnections_List

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections List. */
public final class PrivateEndpointConnectionsListSamples {
    /*
     * x-ms-original-file: specification/cognitiveservices/resource-manager/Microsoft.CognitiveServices/stable/2021-10-01/examples/ListPrivateEndpointConnections.json
     */
    /**
     * Sample code: GetPrivateEndpointConnection.
     *
     * @param manager Entry point to CognitiveServicesManager.
     */
    public static void getPrivateEndpointConnection(
        com.azure.resourcemanager.cognitiveservices.CognitiveServicesManager manager) {
        manager.privateEndpointConnections().listWithResponse("res6977", "sto2527", Context.NONE);
    }
}
```

### PrivateLinkResources_List

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkResources List. */
public final class PrivateLinkResourcesListSamples {
    /*
     * x-ms-original-file: specification/cognitiveservices/resource-manager/Microsoft.CognitiveServices/stable/2021-10-01/examples/ListPrivateLinkResources.json
     */
    /**
     * Sample code: ListPrivateLinkResources.
     *
     * @param manager Entry point to CognitiveServicesManager.
     */
    public static void listPrivateLinkResources(
        com.azure.resourcemanager.cognitiveservices.CognitiveServicesManager manager) {
        manager.privateLinkResources().listWithResponse("res6977", "sto2527", Context.NONE);
    }
}
```

### ResourceProvider_CheckDomainAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.cognitiveservices.models.CheckDomainAvailabilityParameter;

/** Samples for ResourceProvider CheckDomainAvailability. */
public final class ResourceProviderCheckDomainAvailabilitySamples {
    /*
     * x-ms-original-file: specification/cognitiveservices/resource-manager/Microsoft.CognitiveServices/stable/2021-10-01/examples/CheckDomainAvailability.json
     */
    /**
     * Sample code: Check SKU Availability.
     *
     * @param manager Entry point to CognitiveServicesManager.
     */
    public static void checkSKUAvailability(
        com.azure.resourcemanager.cognitiveservices.CognitiveServicesManager manager) {
        manager
            .resourceProviders()
            .checkDomainAvailabilityWithResponse(
                new CheckDomainAvailabilityParameter()
                    .withSubdomainName("contosodemoapp1")
                    .withType("Microsoft.CognitiveServices/accounts"),
                Context.NONE);
    }
}
```

### ResourceProvider_CheckSkuAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.cognitiveservices.models.CheckSkuAvailabilityParameter;
import java.util.Arrays;

/** Samples for ResourceProvider CheckSkuAvailability. */
public final class ResourceProviderCheckSkuAvailabilitySamples {
    /*
     * x-ms-original-file: specification/cognitiveservices/resource-manager/Microsoft.CognitiveServices/stable/2021-10-01/examples/CheckSkuAvailability.json
     */
    /**
     * Sample code: Check SKU Availability.
     *
     * @param manager Entry point to CognitiveServicesManager.
     */
    public static void checkSKUAvailability(
        com.azure.resourcemanager.cognitiveservices.CognitiveServicesManager manager) {
        manager
            .resourceProviders()
            .checkSkuAvailabilityWithResponse(
                "westus",
                new CheckSkuAvailabilityParameter()
                    .withSkus(Arrays.asList("S0"))
                    .withKind("Face")
                    .withType("Microsoft.CognitiveServices/accounts"),
                Context.NONE);
    }
}
```

### ResourceSkus_List

```java
import com.azure.core.util.Context;

/** Samples for ResourceSkus List. */
public final class ResourceSkusListSamples {
    /*
     * x-ms-original-file: specification/cognitiveservices/resource-manager/Microsoft.CognitiveServices/stable/2021-10-01/examples/GetSkus.json
     */
    /**
     * Sample code: Regenerate Keys.
     *
     * @param manager Entry point to CognitiveServicesManager.
     */
    public static void regenerateKeys(com.azure.resourcemanager.cognitiveservices.CognitiveServicesManager manager) {
        manager.resourceSkus().list(Context.NONE);
    }
}
```


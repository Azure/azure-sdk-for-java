# Code snippets and samples


## Accounts

- [CheckNameAvailability](#accounts_checknameavailability)
- [Create](#accounts_create)
- [Delete](#accounts_delete)
- [GetByResourceGroup](#accounts_getbyresourcegroup)
- [List](#accounts_list)
- [ListByResourceGroup](#accounts_listbyresourcegroup)
- [Update](#accounts_update)

## ComputePolicies

- [CreateOrUpdate](#computepolicies_createorupdate)
- [Delete](#computepolicies_delete)
- [Get](#computepolicies_get)
- [ListByAccount](#computepolicies_listbyaccount)
- [Update](#computepolicies_update)

## DataLakeStoreAccounts

- [Add](#datalakestoreaccounts_add)
- [Delete](#datalakestoreaccounts_delete)
- [Get](#datalakestoreaccounts_get)
- [ListByAccount](#datalakestoreaccounts_listbyaccount)

## FirewallRules

- [CreateOrUpdate](#firewallrules_createorupdate)
- [Delete](#firewallrules_delete)
- [Get](#firewallrules_get)
- [ListByAccount](#firewallrules_listbyaccount)
- [Update](#firewallrules_update)

## Locations

- [GetCapability](#locations_getcapability)

## Operations

- [List](#operations_list)

## StorageAccounts

- [Add](#storageaccounts_add)
- [Delete](#storageaccounts_delete)
- [Get](#storageaccounts_get)
- [GetStorageContainer](#storageaccounts_getstoragecontainer)
- [ListByAccount](#storageaccounts_listbyaccount)
- [ListSasTokens](#storageaccounts_listsastokens)
- [ListStorageContainers](#storageaccounts_liststoragecontainers)
- [Update](#storageaccounts_update)
### Accounts_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datalakeanalytics.models.CheckNameAvailabilityParameters;

/** Samples for Accounts CheckNameAvailability. */
public final class AccountsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/datalake-analytics/resource-manager/Microsoft.DataLakeAnalytics/preview/2019-11-01-preview/examples/Accounts_CheckNameAvailability.json
     */
    /**
     * Sample code: Checks whether the specified account name is available or taken.
     *
     * @param manager Entry point to DataLakeAnalyticsManager.
     */
    public static void checksWhetherTheSpecifiedAccountNameIsAvailableOrTaken(
        com.azure.resourcemanager.datalakeanalytics.DataLakeAnalyticsManager manager) {
        manager
            .accounts()
            .checkNameAvailabilityWithResponse(
                "EastUS2", new CheckNameAvailabilityParameters().withName("contosoadla"), Context.NONE);
    }
}
```

### Accounts_Create

```java
import com.azure.resourcemanager.datalakeanalytics.models.AadObjectType;
import com.azure.resourcemanager.datalakeanalytics.models.AddDataLakeStoreWithAccountParameters;
import com.azure.resourcemanager.datalakeanalytics.models.AddStorageAccountWithAccountParameters;
import com.azure.resourcemanager.datalakeanalytics.models.CreateComputePolicyWithAccountParameters;
import com.azure.resourcemanager.datalakeanalytics.models.CreateFirewallRuleWithAccountParameters;
import com.azure.resourcemanager.datalakeanalytics.models.FirewallAllowAzureIpsState;
import com.azure.resourcemanager.datalakeanalytics.models.FirewallState;
import com.azure.resourcemanager.datalakeanalytics.models.TierType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Samples for Accounts Create. */
public final class AccountsCreateSamples {
    /*
     * x-ms-original-file: specification/datalake-analytics/resource-manager/Microsoft.DataLakeAnalytics/preview/2019-11-01-preview/examples/Accounts_Create.json
     */
    /**
     * Sample code: Creates the specified Data Lake Analytics account. This supplies the user with computation services
     * for Data Lake Analytics workloads.
     *
     * @param manager Entry point to DataLakeAnalyticsManager.
     */
    public static void
        createsTheSpecifiedDataLakeAnalyticsAccountThisSuppliesTheUserWithComputationServicesForDataLakeAnalyticsWorkloads(
            com.azure.resourcemanager.datalakeanalytics.DataLakeAnalyticsManager manager) {
        manager
            .accounts()
            .define("contosoadla")
            .withRegion("eastus2")
            .withExistingResourceGroup("contosorg")
            .withDefaultDataLakeStoreAccount("test_adls")
            .withDataLakeStoreAccounts(
                Arrays
                    .asList(
                        new AddDataLakeStoreWithAccountParameters().withName("test_adls").withSuffix("test_suffix")))
            .withTags(mapOf("test_key", "test_value"))
            .withStorageAccounts(
                Arrays
                    .asList(
                        new AddStorageAccountWithAccountParameters()
                            .withName("test_storage")
                            .withAccessKey("34adfa4f-cedf-4dc0-ba29-b6d1a69ab346")
                            .withSuffix("test_suffix")))
            .withComputePolicies(
                Arrays
                    .asList(
                        new CreateComputePolicyWithAccountParameters()
                            .withName("test_policy")
                            .withObjectId(UUID.fromString("34adfa4f-cedf-4dc0-ba29-b6d1a69ab345"))
                            .withObjectType(AadObjectType.USER)
                            .withMaxDegreeOfParallelismPerJob(1)
                            .withMinPriorityPerJob(1)))
            .withFirewallRules(
                Arrays
                    .asList(
                        new CreateFirewallRuleWithAccountParameters()
                            .withName("test_rule")
                            .withStartIpAddress("1.1.1.1")
                            .withEndIpAddress("2.2.2.2")))
            .withFirewallState(FirewallState.ENABLED)
            .withFirewallAllowAzureIps(FirewallAllowAzureIpsState.ENABLED)
            .withNewTier(TierType.CONSUMPTION)
            .withMaxJobCount(3)
            .withMaxDegreeOfParallelism(30)
            .withMaxDegreeOfParallelismPerJob(1)
            .withMinPriorityPerJob(1)
            .withQueryStoreRetention(30)
            .create();
    }

    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> mapOf(Object... inputs) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < inputs.length; i += 2) {
            String key = (String) inputs[i];
            T value = (T) inputs[i + 1];
            map.put(key, value);
        }
        return map;
    }
}
```

### Accounts_Delete

```java
import com.azure.core.util.Context;

/** Samples for Accounts Delete. */
public final class AccountsDeleteSamples {
    /*
     * x-ms-original-file: specification/datalake-analytics/resource-manager/Microsoft.DataLakeAnalytics/preview/2019-11-01-preview/examples/Accounts_Delete.json
     */
    /**
     * Sample code: Begins the delete process for the Data Lake Analytics account object specified by the account name.
     *
     * @param manager Entry point to DataLakeAnalyticsManager.
     */
    public static void beginsTheDeleteProcessForTheDataLakeAnalyticsAccountObjectSpecifiedByTheAccountName(
        com.azure.resourcemanager.datalakeanalytics.DataLakeAnalyticsManager manager) {
        manager.accounts().delete("contosorg", "contosoadla", Context.NONE);
    }
}
```

### Accounts_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Accounts GetByResourceGroup. */
public final class AccountsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/datalake-analytics/resource-manager/Microsoft.DataLakeAnalytics/preview/2019-11-01-preview/examples/Accounts_Get.json
     */
    /**
     * Sample code: Gets details of the specified Data Lake Analytics account.
     *
     * @param manager Entry point to DataLakeAnalyticsManager.
     */
    public static void getsDetailsOfTheSpecifiedDataLakeAnalyticsAccount(
        com.azure.resourcemanager.datalakeanalytics.DataLakeAnalyticsManager manager) {
        manager.accounts().getByResourceGroupWithResponse("contosorg", "contosoadla", Context.NONE);
    }
}
```

### Accounts_List

```java
import com.azure.core.util.Context;

/** Samples for Accounts List. */
public final class AccountsListSamples {
    /*
     * x-ms-original-file: specification/datalake-analytics/resource-manager/Microsoft.DataLakeAnalytics/preview/2019-11-01-preview/examples/Accounts_List.json
     */
    /**
     * Sample code: Gets the first page of Data Lake Analytics accounts, if any, within the current subscription. This
     * includes a link to the next page, if any.
     *
     * @param manager Entry point to DataLakeAnalyticsManager.
     */
    public static void
        getsTheFirstPageOfDataLakeAnalyticsAccountsIfAnyWithinTheCurrentSubscriptionThisIncludesALinkToTheNextPageIfAny(
            com.azure.resourcemanager.datalakeanalytics.DataLakeAnalyticsManager manager) {
        manager.accounts().list("test_filter", 1, 1, "test_select", "test_orderby", false, Context.NONE);
    }
}
```

### Accounts_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Accounts ListByResourceGroup. */
public final class AccountsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/datalake-analytics/resource-manager/Microsoft.DataLakeAnalytics/preview/2019-11-01-preview/examples/Accounts_ListByResourceGroup.json
     */
    /**
     * Sample code: Gets the first page of Data Lake Analytics accounts, if any, within a specific resource group. This
     * includes a link to the next page, if any.
     *
     * @param manager Entry point to DataLakeAnalyticsManager.
     */
    public static void
        getsTheFirstPageOfDataLakeAnalyticsAccountsIfAnyWithinASpecificResourceGroupThisIncludesALinkToTheNextPageIfAny(
            com.azure.resourcemanager.datalakeanalytics.DataLakeAnalyticsManager manager) {
        manager
            .accounts()
            .listByResourceGroup("contosorg", "test_filter", 1, 1, "test_select", "test_orderby", false, Context.NONE);
    }
}
```

### Accounts_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datalakeanalytics.models.AadObjectType;
import com.azure.resourcemanager.datalakeanalytics.models.DataLakeAnalyticsAccount;
import com.azure.resourcemanager.datalakeanalytics.models.FirewallAllowAzureIpsState;
import com.azure.resourcemanager.datalakeanalytics.models.FirewallState;
import com.azure.resourcemanager.datalakeanalytics.models.TierType;
import com.azure.resourcemanager.datalakeanalytics.models.UpdateComputePolicyWithAccountParameters;
import com.azure.resourcemanager.datalakeanalytics.models.UpdateFirewallRuleWithAccountParameters;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Samples for Accounts Update. */
public final class AccountsUpdateSamples {
    /*
     * x-ms-original-file: specification/datalake-analytics/resource-manager/Microsoft.DataLakeAnalytics/preview/2019-11-01-preview/examples/Accounts_Update.json
     */
    /**
     * Sample code: Updates the Data Lake Analytics account object specified by the accountName with the contents of the
     * account object.
     *
     * @param manager Entry point to DataLakeAnalyticsManager.
     */
    public static void
        updatesTheDataLakeAnalyticsAccountObjectSpecifiedByTheAccountNameWithTheContentsOfTheAccountObject(
            com.azure.resourcemanager.datalakeanalytics.DataLakeAnalyticsManager manager) {
        DataLakeAnalyticsAccount resource =
            manager.accounts().getByResourceGroupWithResponse("contosorg", "contosoadla", Context.NONE).getValue();
        resource
            .update()
            .withTags(mapOf("test_key", "test_value"))
            .withComputePoliciesForUpdate(
                Arrays
                    .asList(
                        new UpdateComputePolicyWithAccountParameters()
                            .withName("test_policy")
                            .withObjectId(UUID.fromString("34adfa4f-cedf-4dc0-ba29-b6d1a69ab345"))
                            .withObjectType(AadObjectType.USER)
                            .withMaxDegreeOfParallelismPerJob(1)
                            .withMinPriorityPerJob(1)))
            .withFirewallRulesForUpdate(
                Arrays
                    .asList(
                        new UpdateFirewallRuleWithAccountParameters()
                            .withName("test_rule")
                            .withStartIpAddress("1.1.1.1")
                            .withEndIpAddress("2.2.2.2")))
            .withFirewallState(FirewallState.ENABLED)
            .withFirewallAllowAzureIps(FirewallAllowAzureIpsState.ENABLED)
            .withNewTier(TierType.CONSUMPTION)
            .withMaxJobCount(1)
            .withMaxDegreeOfParallelism(1)
            .withMaxDegreeOfParallelismPerJob(1)
            .withMinPriorityPerJob(1)
            .withQueryStoreRetention(1)
            .apply();
    }

    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> mapOf(Object... inputs) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < inputs.length; i += 2) {
            String key = (String) inputs[i];
            T value = (T) inputs[i + 1];
            map.put(key, value);
        }
        return map;
    }
}
```

### ComputePolicies_CreateOrUpdate

```java
import com.azure.resourcemanager.datalakeanalytics.models.AadObjectType;
import java.util.UUID;

/** Samples for ComputePolicies CreateOrUpdate. */
public final class ComputePoliciesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/datalake-analytics/resource-manager/Microsoft.DataLakeAnalytics/preview/2019-11-01-preview/examples/ComputePolicies_CreateOrUpdate.json
     */
    /**
     * Sample code: Creates or updates the specified compute policy.
     *
     * @param manager Entry point to DataLakeAnalyticsManager.
     */
    public static void createsOrUpdatesTheSpecifiedComputePolicy(
        com.azure.resourcemanager.datalakeanalytics.DataLakeAnalyticsManager manager) {
        manager
            .computePolicies()
            .define("test_policy")
            .withExistingAccount("contosorg", "contosoadla")
            .withObjectId(UUID.fromString("776b9091-8916-4638-87f7-9c989a38da98"))
            .withObjectType(AadObjectType.USER)
            .withMaxDegreeOfParallelismPerJob(10)
            .withMinPriorityPerJob(30)
            .create();
    }
}
```

### ComputePolicies_Delete

```java
import com.azure.core.util.Context;

/** Samples for ComputePolicies Delete. */
public final class ComputePoliciesDeleteSamples {
    /*
     * x-ms-original-file: specification/datalake-analytics/resource-manager/Microsoft.DataLakeAnalytics/preview/2019-11-01-preview/examples/ComputePolicies_Delete.json
     */
    /**
     * Sample code: Deletes the specified compute policy from the adla account.
     *
     * @param manager Entry point to DataLakeAnalyticsManager.
     */
    public static void deletesTheSpecifiedComputePolicyFromTheAdlaAccount(
        com.azure.resourcemanager.datalakeanalytics.DataLakeAnalyticsManager manager) {
        manager.computePolicies().deleteWithResponse("contosorg", "contosoadla", "test_policy", Context.NONE);
    }
}
```

### ComputePolicies_Get

```java
import com.azure.core.util.Context;

/** Samples for ComputePolicies Get. */
public final class ComputePoliciesGetSamples {
    /*
     * x-ms-original-file: specification/datalake-analytics/resource-manager/Microsoft.DataLakeAnalytics/preview/2019-11-01-preview/examples/ComputePolicies_Get.json
     */
    /**
     * Sample code: Gets the specified compute policy.
     *
     * @param manager Entry point to DataLakeAnalyticsManager.
     */
    public static void getsTheSpecifiedComputePolicy(
        com.azure.resourcemanager.datalakeanalytics.DataLakeAnalyticsManager manager) {
        manager.computePolicies().getWithResponse("contosorg", "contosoadla", "test_policy", Context.NONE);
    }
}
```

### ComputePolicies_ListByAccount

```java
import com.azure.core.util.Context;

/** Samples for ComputePolicies ListByAccount. */
public final class ComputePoliciesListByAccountSamples {
    /*
     * x-ms-original-file: specification/datalake-analytics/resource-manager/Microsoft.DataLakeAnalytics/preview/2019-11-01-preview/examples/ComputePolicies_ListByAccount.json
     */
    /**
     * Sample code: Lists the compute policies within the adla account.
     *
     * @param manager Entry point to DataLakeAnalyticsManager.
     */
    public static void listsTheComputePoliciesWithinTheAdlaAccount(
        com.azure.resourcemanager.datalakeanalytics.DataLakeAnalyticsManager manager) {
        manager.computePolicies().listByAccount("contosorg", "contosoadla", Context.NONE);
    }
}
```

### ComputePolicies_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datalakeanalytics.models.ComputePolicy;

/** Samples for ComputePolicies Update. */
public final class ComputePoliciesUpdateSamples {
    /*
     * x-ms-original-file: specification/datalake-analytics/resource-manager/Microsoft.DataLakeAnalytics/preview/2019-11-01-preview/examples/ComputePolicies_Update.json
     */
    /**
     * Sample code: Updates the specified compute policy.
     *
     * @param manager Entry point to DataLakeAnalyticsManager.
     */
    public static void updatesTheSpecifiedComputePolicy(
        com.azure.resourcemanager.datalakeanalytics.DataLakeAnalyticsManager manager) {
        ComputePolicy resource =
            manager
                .computePolicies()
                .getWithResponse("contosorg", "contosoadla", "test_policy", Context.NONE)
                .getValue();
        resource.update().withMaxDegreeOfParallelismPerJob(11).withMinPriorityPerJob(31).apply();
    }
}
```

### DataLakeStoreAccounts_Add

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datalakeanalytics.models.AddDataLakeStoreParameters;

/** Samples for DataLakeStoreAccounts Add. */
public final class DataLakeStoreAccountsAddSamples {
    /*
     * x-ms-original-file: specification/datalake-analytics/resource-manager/Microsoft.DataLakeAnalytics/preview/2019-11-01-preview/examples/DataLakeStoreAccounts_Add.json
     */
    /**
     * Sample code: Adds a Data Lake Store account.
     *
     * @param manager Entry point to DataLakeAnalyticsManager.
     */
    public static void addsADataLakeStoreAccount(
        com.azure.resourcemanager.datalakeanalytics.DataLakeAnalyticsManager manager) {
        manager
            .dataLakeStoreAccounts()
            .addWithResponse(
                "contosorg",
                "contosoadla",
                "test_adls_account",
                new AddDataLakeStoreParameters().withSuffix("test_suffix"),
                Context.NONE);
    }
}
```

### DataLakeStoreAccounts_Delete

```java
import com.azure.core.util.Context;

/** Samples for DataLakeStoreAccounts Delete. */
public final class DataLakeStoreAccountsDeleteSamples {
    /*
     * x-ms-original-file: specification/datalake-analytics/resource-manager/Microsoft.DataLakeAnalytics/preview/2019-11-01-preview/examples/DataLakeStoreAccounts_Delete.json
     */
    /**
     * Sample code: Removes the specified Data Lake Store account.
     *
     * @param manager Entry point to DataLakeAnalyticsManager.
     */
    public static void removesTheSpecifiedDataLakeStoreAccount(
        com.azure.resourcemanager.datalakeanalytics.DataLakeAnalyticsManager manager) {
        manager
            .dataLakeStoreAccounts()
            .deleteWithResponse("contosorg", "contosoadla", "test_adls_account", Context.NONE);
    }
}
```

### DataLakeStoreAccounts_Get

```java
import com.azure.core.util.Context;

/** Samples for DataLakeStoreAccounts Get. */
public final class DataLakeStoreAccountsGetSamples {
    /*
     * x-ms-original-file: specification/datalake-analytics/resource-manager/Microsoft.DataLakeAnalytics/preview/2019-11-01-preview/examples/DataLakeStoreAccounts_Get.json
     */
    /**
     * Sample code: Gets the specified Data Lake Store account details.
     *
     * @param manager Entry point to DataLakeAnalyticsManager.
     */
    public static void getsTheSpecifiedDataLakeStoreAccountDetails(
        com.azure.resourcemanager.datalakeanalytics.DataLakeAnalyticsManager manager) {
        manager.dataLakeStoreAccounts().getWithResponse("contosorg", "contosoadla", "test_adls_account", Context.NONE);
    }
}
```

### DataLakeStoreAccounts_ListByAccount

```java
import com.azure.core.util.Context;

/** Samples for DataLakeStoreAccounts ListByAccount. */
public final class DataLakeStoreAccountsListByAccountSamples {
    /*
     * x-ms-original-file: specification/datalake-analytics/resource-manager/Microsoft.DataLakeAnalytics/preview/2019-11-01-preview/examples/DataLakeStoreAccounts_ListByAccount.json
     */
    /**
     * Sample code: Gets the first page of Data Lake Store accounts linked to the specified Data Lake Analytics account.
     *
     * @param manager Entry point to DataLakeAnalyticsManager.
     */
    public static void getsTheFirstPageOfDataLakeStoreAccountsLinkedToTheSpecifiedDataLakeAnalyticsAccount(
        com.azure.resourcemanager.datalakeanalytics.DataLakeAnalyticsManager manager) {
        manager
            .dataLakeStoreAccounts()
            .listByAccount(
                "contosorg", "contosoadla", "test_filter", 1, 1, "test_select", "test_orderby", false, Context.NONE);
    }
}
```

### FirewallRules_CreateOrUpdate

```java
/** Samples for FirewallRules CreateOrUpdate. */
public final class FirewallRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/datalake-analytics/resource-manager/Microsoft.DataLakeAnalytics/preview/2019-11-01-preview/examples/FirewallRules_CreateOrUpdate.json
     */
    /**
     * Sample code: Creates or updates the specified firewall rule.
     *
     * @param manager Entry point to DataLakeAnalyticsManager.
     */
    public static void createsOrUpdatesTheSpecifiedFirewallRule(
        com.azure.resourcemanager.datalakeanalytics.DataLakeAnalyticsManager manager) {
        manager
            .firewallRules()
            .define("test_rule")
            .withExistingAccount("contosorg", "contosoadla")
            .withStartIpAddress("1.1.1.1")
            .withEndIpAddress("2.2.2.2")
            .create();
    }
}
```

### FirewallRules_Delete

```java
import com.azure.core.util.Context;

/** Samples for FirewallRules Delete. */
public final class FirewallRulesDeleteSamples {
    /*
     * x-ms-original-file: specification/datalake-analytics/resource-manager/Microsoft.DataLakeAnalytics/preview/2019-11-01-preview/examples/FirewallRules_Delete.json
     */
    /**
     * Sample code: Deletes the specified firewall rule.
     *
     * @param manager Entry point to DataLakeAnalyticsManager.
     */
    public static void deletesTheSpecifiedFirewallRule(
        com.azure.resourcemanager.datalakeanalytics.DataLakeAnalyticsManager manager) {
        manager.firewallRules().deleteWithResponse("contosorg", "contosoadla", "test_rule", Context.NONE);
    }
}
```

### FirewallRules_Get

```java
import com.azure.core.util.Context;

/** Samples for FirewallRules Get. */
public final class FirewallRulesGetSamples {
    /*
     * x-ms-original-file: specification/datalake-analytics/resource-manager/Microsoft.DataLakeAnalytics/preview/2019-11-01-preview/examples/FirewallRules_Get.json
     */
    /**
     * Sample code: Gets the specified Data Lake Analytics firewall rule.
     *
     * @param manager Entry point to DataLakeAnalyticsManager.
     */
    public static void getsTheSpecifiedDataLakeAnalyticsFirewallRule(
        com.azure.resourcemanager.datalakeanalytics.DataLakeAnalyticsManager manager) {
        manager.firewallRules().getWithResponse("contosorg", "contosoadla", "test_rule", Context.NONE);
    }
}
```

### FirewallRules_ListByAccount

```java
import com.azure.core.util.Context;

/** Samples for FirewallRules ListByAccount. */
public final class FirewallRulesListByAccountSamples {
    /*
     * x-ms-original-file: specification/datalake-analytics/resource-manager/Microsoft.DataLakeAnalytics/preview/2019-11-01-preview/examples/FirewallRules_ListByAccount.json
     */
    /**
     * Sample code: Lists the Data Lake Analytics firewall rules.
     *
     * @param manager Entry point to DataLakeAnalyticsManager.
     */
    public static void listsTheDataLakeAnalyticsFirewallRules(
        com.azure.resourcemanager.datalakeanalytics.DataLakeAnalyticsManager manager) {
        manager.firewallRules().listByAccount("contosorg", "contosoadla", Context.NONE);
    }
}
```

### FirewallRules_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datalakeanalytics.models.FirewallRule;

/** Samples for FirewallRules Update. */
public final class FirewallRulesUpdateSamples {
    /*
     * x-ms-original-file: specification/datalake-analytics/resource-manager/Microsoft.DataLakeAnalytics/preview/2019-11-01-preview/examples/FirewallRules_Update.json
     */
    /**
     * Sample code: Updates the specified firewall rule.
     *
     * @param manager Entry point to DataLakeAnalyticsManager.
     */
    public static void updatesTheSpecifiedFirewallRule(
        com.azure.resourcemanager.datalakeanalytics.DataLakeAnalyticsManager manager) {
        FirewallRule resource =
            manager.firewallRules().getWithResponse("contosorg", "contosoadla", "test_rule", Context.NONE).getValue();
        resource.update().withStartIpAddress("1.1.1.1").withEndIpAddress("2.2.2.2").apply();
    }
}
```

### Locations_GetCapability

```java
import com.azure.core.util.Context;

/** Samples for Locations GetCapability. */
public final class LocationsGetCapabilitySamples {
    /*
     * x-ms-original-file: specification/datalake-analytics/resource-manager/Microsoft.DataLakeAnalytics/preview/2019-11-01-preview/examples/Locations_GetCapability.json
     */
    /**
     * Sample code: Gets subscription-level properties and limits for Data Lake Analytics specified by resource
     * location.
     *
     * @param manager Entry point to DataLakeAnalyticsManager.
     */
    public static void getsSubscriptionLevelPropertiesAndLimitsForDataLakeAnalyticsSpecifiedByResourceLocation(
        com.azure.resourcemanager.datalakeanalytics.DataLakeAnalyticsManager manager) {
        manager.locations().getCapabilityWithResponse("EastUS2", Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/datalake-analytics/resource-manager/Microsoft.DataLakeAnalytics/preview/2019-11-01-preview/examples/Operations_List.json
     */
    /**
     * Sample code: Lists all of the available Data Lake Analytics REST API operations.
     *
     * @param manager Entry point to DataLakeAnalyticsManager.
     */
    public static void listsAllOfTheAvailableDataLakeAnalyticsRESTAPIOperations(
        com.azure.resourcemanager.datalakeanalytics.DataLakeAnalyticsManager manager) {
        manager.operations().listWithResponse(Context.NONE);
    }
}
```

### StorageAccounts_Add

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datalakeanalytics.models.AddStorageAccountParameters;

/** Samples for StorageAccounts Add. */
public final class StorageAccountsAddSamples {
    /*
     * x-ms-original-file: specification/datalake-analytics/resource-manager/Microsoft.DataLakeAnalytics/preview/2019-11-01-preview/examples/StorageAccounts_Add.json
     */
    /**
     * Sample code: Adds an Azure Storage account.
     *
     * @param manager Entry point to DataLakeAnalyticsManager.
     */
    public static void addsAnAzureStorageAccount(
        com.azure.resourcemanager.datalakeanalytics.DataLakeAnalyticsManager manager) {
        manager
            .storageAccounts()
            .addWithResponse(
                "contosorg",
                "contosoadla",
                "test_storage",
                new AddStorageAccountParameters()
                    .withAccessKey("34adfa4f-cedf-4dc0-ba29-b6d1a69ab346")
                    .withSuffix("test_suffix"),
                Context.NONE);
    }
}
```

### StorageAccounts_Delete

```java
import com.azure.core.util.Context;

/** Samples for StorageAccounts Delete. */
public final class StorageAccountsDeleteSamples {
    /*
     * x-ms-original-file: specification/datalake-analytics/resource-manager/Microsoft.DataLakeAnalytics/preview/2019-11-01-preview/examples/StorageAccounts_Delete.json
     */
    /**
     * Sample code: Removes an Azure Storage account.
     *
     * @param manager Entry point to DataLakeAnalyticsManager.
     */
    public static void removesAnAzureStorageAccount(
        com.azure.resourcemanager.datalakeanalytics.DataLakeAnalyticsManager manager) {
        manager.storageAccounts().deleteWithResponse("contosorg", "contosoadla", "test_storage", Context.NONE);
    }
}
```

### StorageAccounts_Get

```java
import com.azure.core.util.Context;

/** Samples for StorageAccounts Get. */
public final class StorageAccountsGetSamples {
    /*
     * x-ms-original-file: specification/datalake-analytics/resource-manager/Microsoft.DataLakeAnalytics/preview/2019-11-01-preview/examples/StorageAccounts_Get.json
     */
    /**
     * Sample code: Gets the specified Azure Storage account.
     *
     * @param manager Entry point to DataLakeAnalyticsManager.
     */
    public static void getsTheSpecifiedAzureStorageAccount(
        com.azure.resourcemanager.datalakeanalytics.DataLakeAnalyticsManager manager) {
        manager.storageAccounts().getWithResponse("contosorg", "contosoadla", "test_storage", Context.NONE);
    }
}
```

### StorageAccounts_GetStorageContainer

```java
import com.azure.core.util.Context;

/** Samples for StorageAccounts GetStorageContainer. */
public final class StorageAccountsGetStorageContainerSamples {
    /*
     * x-ms-original-file: specification/datalake-analytics/resource-manager/Microsoft.DataLakeAnalytics/preview/2019-11-01-preview/examples/StorageAccounts_GetStorageContainer.json
     */
    /**
     * Sample code: Gets the specified Azure Storage container.
     *
     * @param manager Entry point to DataLakeAnalyticsManager.
     */
    public static void getsTheSpecifiedAzureStorageContainer(
        com.azure.resourcemanager.datalakeanalytics.DataLakeAnalyticsManager manager) {
        manager
            .storageAccounts()
            .getStorageContainerWithResponse(
                "contosorg", "contosoadla", "test_storage", "test_container", Context.NONE);
    }
}
```

### StorageAccounts_ListByAccount

```java
import com.azure.core.util.Context;

/** Samples for StorageAccounts ListByAccount. */
public final class StorageAccountsListByAccountSamples {
    /*
     * x-ms-original-file: specification/datalake-analytics/resource-manager/Microsoft.DataLakeAnalytics/preview/2019-11-01-preview/examples/StorageAccounts_ListByAccount.json
     */
    /**
     * Sample code: Gets the first page of Azure Storage accounts linked to the specified Data Lake Analytics account.
     *
     * @param manager Entry point to DataLakeAnalyticsManager.
     */
    public static void getsTheFirstPageOfAzureStorageAccountsLinkedToTheSpecifiedDataLakeAnalyticsAccount(
        com.azure.resourcemanager.datalakeanalytics.DataLakeAnalyticsManager manager) {
        manager
            .storageAccounts()
            .listByAccount(
                "contosorg", "contosoadla", "test_filter", 1, 1, "test_select", "test_orderby", false, Context.NONE);
    }
}
```

### StorageAccounts_ListSasTokens

```java
import com.azure.core.util.Context;

/** Samples for StorageAccounts ListSasTokens. */
public final class StorageAccountsListSasTokensSamples {
    /*
     * x-ms-original-file: specification/datalake-analytics/resource-manager/Microsoft.DataLakeAnalytics/preview/2019-11-01-preview/examples/StorageAccounts_ListSasTokens.json
     */
    /**
     * Sample code: Gets the SAS token.
     *
     * @param manager Entry point to DataLakeAnalyticsManager.
     */
    public static void getsTheSASToken(com.azure.resourcemanager.datalakeanalytics.DataLakeAnalyticsManager manager) {
        manager
            .storageAccounts()
            .listSasTokens("contosorg", "contosoadla", "test_storage", "test_container", Context.NONE);
    }
}
```

### StorageAccounts_ListStorageContainers

```java
import com.azure.core.util.Context;

/** Samples for StorageAccounts ListStorageContainers. */
public final class StorageAccountsListStorageContainersSamples {
    /*
     * x-ms-original-file: specification/datalake-analytics/resource-manager/Microsoft.DataLakeAnalytics/preview/2019-11-01-preview/examples/StorageAccounts_ListStorageContainers.json
     */
    /**
     * Sample code: Lists the Azure Storage containers.
     *
     * @param manager Entry point to DataLakeAnalyticsManager.
     */
    public static void listsTheAzureStorageContainers(
        com.azure.resourcemanager.datalakeanalytics.DataLakeAnalyticsManager manager) {
        manager.storageAccounts().listStorageContainers("contosorg", "contosoadla", "test_storage", Context.NONE);
    }
}
```

### StorageAccounts_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datalakeanalytics.models.UpdateStorageAccountParameters;

/** Samples for StorageAccounts Update. */
public final class StorageAccountsUpdateSamples {
    /*
     * x-ms-original-file: specification/datalake-analytics/resource-manager/Microsoft.DataLakeAnalytics/preview/2019-11-01-preview/examples/StorageAccounts_Update.json
     */
    /**
     * Sample code: Replaces Azure Storage blob account details.
     *
     * @param manager Entry point to DataLakeAnalyticsManager.
     */
    public static void replacesAzureStorageBlobAccountDetails(
        com.azure.resourcemanager.datalakeanalytics.DataLakeAnalyticsManager manager) {
        manager
            .storageAccounts()
            .updateWithResponse(
                "contosorg",
                "contosoadla",
                "test_storage",
                new UpdateStorageAccountParameters()
                    .withAccessKey("34adfa4f-cedf-4dc0-ba29-b6d1a69ab346")
                    .withSuffix("test_suffix"),
                Context.NONE);
    }
}
```


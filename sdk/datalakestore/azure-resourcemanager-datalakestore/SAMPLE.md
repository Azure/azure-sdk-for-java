# Code snippets and samples


## Accounts

- [CheckNameAvailability](#accounts_checknameavailability)
- [Create](#accounts_create)
- [Delete](#accounts_delete)
- [EnableKeyVault](#accounts_enablekeyvault)
- [GetByResourceGroup](#accounts_getbyresourcegroup)
- [List](#accounts_list)
- [ListByResourceGroup](#accounts_listbyresourcegroup)
- [Update](#accounts_update)

## FirewallRules

- [CreateOrUpdate](#firewallrules_createorupdate)
- [Delete](#firewallrules_delete)
- [Get](#firewallrules_get)
- [ListByAccount](#firewallrules_listbyaccount)
- [Update](#firewallrules_update)

## Locations

- [GetCapability](#locations_getcapability)
- [GetUsage](#locations_getusage)

## Operations

- [List](#operations_list)

## TrustedIdProviders

- [CreateOrUpdate](#trustedidproviders_createorupdate)
- [Delete](#trustedidproviders_delete)
- [Get](#trustedidproviders_get)
- [ListByAccount](#trustedidproviders_listbyaccount)
- [Update](#trustedidproviders_update)

## VirtualNetworkRules

- [CreateOrUpdate](#virtualnetworkrules_createorupdate)
- [Delete](#virtualnetworkrules_delete)
- [Get](#virtualnetworkrules_get)
- [ListByAccount](#virtualnetworkrules_listbyaccount)
- [Update](#virtualnetworkrules_update)
### Accounts_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datalakestore.models.CheckNameAvailabilityParameters;

/** Samples for Accounts CheckNameAvailability. */
public final class AccountsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/datalake-store/resource-manager/Microsoft.DataLakeStore/stable/2016-11-01/examples/Accounts_CheckNameAvailability.json
     */
    /**
     * Sample code: Checks whether the specified account name is available or taken.
     *
     * @param manager Entry point to DataLakeStoreManager.
     */
    public static void checksWhetherTheSpecifiedAccountNameIsAvailableOrTaken(
        com.azure.resourcemanager.datalakestore.DataLakeStoreManager manager) {
        manager
            .accounts()
            .checkNameAvailabilityWithResponse(
                "EastUS2", new CheckNameAvailabilityParameters().withName("contosoadla"), Context.NONE);
    }
}
```

### Accounts_Create

```java
import com.azure.resourcemanager.datalakestore.models.CreateFirewallRuleWithAccountParameters;
import com.azure.resourcemanager.datalakestore.models.CreateTrustedIdProviderWithAccountParameters;
import com.azure.resourcemanager.datalakestore.models.EncryptionConfig;
import com.azure.resourcemanager.datalakestore.models.EncryptionConfigType;
import com.azure.resourcemanager.datalakestore.models.EncryptionIdentity;
import com.azure.resourcemanager.datalakestore.models.EncryptionState;
import com.azure.resourcemanager.datalakestore.models.FirewallAllowAzureIpsState;
import com.azure.resourcemanager.datalakestore.models.FirewallState;
import com.azure.resourcemanager.datalakestore.models.KeyVaultMetaInfo;
import com.azure.resourcemanager.datalakestore.models.TierType;
import com.azure.resourcemanager.datalakestore.models.TrustedIdProviderState;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Accounts Create. */
public final class AccountsCreateSamples {
    /*
     * x-ms-original-file: specification/datalake-store/resource-manager/Microsoft.DataLakeStore/stable/2016-11-01/examples/Accounts_Create.json
     */
    /**
     * Sample code: Creates the specified Data Lake Store account.
     *
     * @param manager Entry point to DataLakeStoreManager.
     */
    public static void createsTheSpecifiedDataLakeStoreAccount(
        com.azure.resourcemanager.datalakestore.DataLakeStoreManager manager) {
        manager
            .accounts()
            .define("contosoadla")
            .withRegion("eastus2")
            .withExistingResourceGroup("contosorg")
            .withTags(mapOf("test_key", "test_value"))
            .withIdentity(new EncryptionIdentity())
            .withDefaultGroup("test_default_group")
            .withEncryptionConfig(
                new EncryptionConfig()
                    .withType(EncryptionConfigType.USER_MANAGED)
                    .withKeyVaultMetaInfo(
                        new KeyVaultMetaInfo()
                            .withKeyVaultResourceId("34adfa4f-cedf-4dc0-ba29-b6d1a69ab345")
                            .withEncryptionKeyName("test_encryption_key_name")
                            .withEncryptionKeyVersion("encryption_key_version")))
            .withEncryptionState(EncryptionState.ENABLED)
            .withFirewallRules(
                Arrays
                    .asList(
                        new CreateFirewallRuleWithAccountParameters()
                            .withName("test_rule")
                            .withStartIpAddress("1.1.1.1")
                            .withEndIpAddress("2.2.2.2")))
            .withFirewallState(FirewallState.ENABLED)
            .withFirewallAllowAzureIps(FirewallAllowAzureIpsState.ENABLED)
            .withTrustedIdProviders(
                Arrays
                    .asList(
                        new CreateTrustedIdProviderWithAccountParameters()
                            .withName("test_trusted_id_provider_name")
                            .withIdProvider("https://sts.windows.net/ea9ec534-a3e3-4e45-ad36-3afc5bb291c1")))
            .withTrustedIdProviderState(TrustedIdProviderState.ENABLED)
            .withNewTier(TierType.CONSUMPTION)
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
     * x-ms-original-file: specification/datalake-store/resource-manager/Microsoft.DataLakeStore/stable/2016-11-01/examples/Accounts_Delete.json
     */
    /**
     * Sample code: Deletes the specified Data Lake Store account.
     *
     * @param manager Entry point to DataLakeStoreManager.
     */
    public static void deletesTheSpecifiedDataLakeStoreAccount(
        com.azure.resourcemanager.datalakestore.DataLakeStoreManager manager) {
        manager.accounts().delete("contosorg", "contosoadla", Context.NONE);
    }
}
```

### Accounts_EnableKeyVault

```java
import com.azure.core.util.Context;

/** Samples for Accounts EnableKeyVault. */
public final class AccountsEnableKeyVaultSamples {
    /*
     * x-ms-original-file: specification/datalake-store/resource-manager/Microsoft.DataLakeStore/stable/2016-11-01/examples/Accounts_EnableKeyVault.json
     */
    /**
     * Sample code: Attempts to enable a user managed Key Vault for encryption of the specified Data Lake Store account.
     *
     * @param manager Entry point to DataLakeStoreManager.
     */
    public static void attemptsToEnableAUserManagedKeyVaultForEncryptionOfTheSpecifiedDataLakeStoreAccount(
        com.azure.resourcemanager.datalakestore.DataLakeStoreManager manager) {
        manager.accounts().enableKeyVaultWithResponse("contosorg", "contosoadla", Context.NONE);
    }
}
```

### Accounts_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Accounts GetByResourceGroup. */
public final class AccountsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/datalake-store/resource-manager/Microsoft.DataLakeStore/stable/2016-11-01/examples/Accounts_Get.json
     */
    /**
     * Sample code: Gets the specified Data Lake Store account.
     *
     * @param manager Entry point to DataLakeStoreManager.
     */
    public static void getsTheSpecifiedDataLakeStoreAccount(
        com.azure.resourcemanager.datalakestore.DataLakeStoreManager manager) {
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
     * x-ms-original-file: specification/datalake-store/resource-manager/Microsoft.DataLakeStore/stable/2016-11-01/examples/Accounts_List.json
     */
    /**
     * Sample code: Lists the Data Lake Store accounts within the subscription.
     *
     * @param manager Entry point to DataLakeStoreManager.
     */
    public static void listsTheDataLakeStoreAccountsWithinTheSubscription(
        com.azure.resourcemanager.datalakestore.DataLakeStoreManager manager) {
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
     * x-ms-original-file: specification/datalake-store/resource-manager/Microsoft.DataLakeStore/stable/2016-11-01/examples/Accounts_ListByResourceGroup.json
     */
    /**
     * Sample code: Lists the Data Lake Store accounts within a specific resource group.
     *
     * @param manager Entry point to DataLakeStoreManager.
     */
    public static void listsTheDataLakeStoreAccountsWithinASpecificResourceGroup(
        com.azure.resourcemanager.datalakestore.DataLakeStoreManager manager) {
        manager
            .accounts()
            .listByResourceGroup("contosorg", "test_filter", 1, 1, "test_select", "test_orderby", false, Context.NONE);
    }
}
```

### Accounts_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datalakestore.models.DataLakeStoreAccount;
import com.azure.resourcemanager.datalakestore.models.FirewallAllowAzureIpsState;
import com.azure.resourcemanager.datalakestore.models.FirewallState;
import com.azure.resourcemanager.datalakestore.models.TierType;
import com.azure.resourcemanager.datalakestore.models.TrustedIdProviderState;
import com.azure.resourcemanager.datalakestore.models.UpdateEncryptionConfig;
import com.azure.resourcemanager.datalakestore.models.UpdateKeyVaultMetaInfo;
import java.util.HashMap;
import java.util.Map;

/** Samples for Accounts Update. */
public final class AccountsUpdateSamples {
    /*
     * x-ms-original-file: specification/datalake-store/resource-manager/Microsoft.DataLakeStore/stable/2016-11-01/examples/Accounts_Update.json
     */
    /**
     * Sample code: Updates the specified Data Lake Store account information.
     *
     * @param manager Entry point to DataLakeStoreManager.
     */
    public static void updatesTheSpecifiedDataLakeStoreAccountInformation(
        com.azure.resourcemanager.datalakestore.DataLakeStoreManager manager) {
        DataLakeStoreAccount resource =
            manager.accounts().getByResourceGroupWithResponse("contosorg", "contosoadla", Context.NONE).getValue();
        resource
            .update()
            .withTags(mapOf("test_key", "test_value"))
            .withDefaultGroup("test_default_group")
            .withEncryptionConfig(
                new UpdateEncryptionConfig()
                    .withKeyVaultMetaInfo(
                        new UpdateKeyVaultMetaInfo().withEncryptionKeyVersion("encryption_key_version")))
            .withFirewallState(FirewallState.ENABLED)
            .withFirewallAllowAzureIps(FirewallAllowAzureIpsState.ENABLED)
            .withTrustedIdProviderState(TrustedIdProviderState.ENABLED)
            .withNewTier(TierType.CONSUMPTION)
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

### FirewallRules_CreateOrUpdate

```java
/** Samples for FirewallRules CreateOrUpdate. */
public final class FirewallRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/datalake-store/resource-manager/Microsoft.DataLakeStore/stable/2016-11-01/examples/FirewallRules_CreateOrUpdate.json
     */
    /**
     * Sample code: Creates or updates the specified firewall rule. During update, the firewall rule with the specified
     * name will be replaced with this new firewall rule.
     *
     * @param manager Entry point to DataLakeStoreManager.
     */
    public static void
        createsOrUpdatesTheSpecifiedFirewallRuleDuringUpdateTheFirewallRuleWithTheSpecifiedNameWillBeReplacedWithThisNewFirewallRule(
            com.azure.resourcemanager.datalakestore.DataLakeStoreManager manager) {
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
     * x-ms-original-file: specification/datalake-store/resource-manager/Microsoft.DataLakeStore/stable/2016-11-01/examples/FirewallRules_Delete.json
     */
    /**
     * Sample code: Deletes the specified firewall rule from the specified Data Lake Store account.
     *
     * @param manager Entry point to DataLakeStoreManager.
     */
    public static void deletesTheSpecifiedFirewallRuleFromTheSpecifiedDataLakeStoreAccount(
        com.azure.resourcemanager.datalakestore.DataLakeStoreManager manager) {
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
     * x-ms-original-file: specification/datalake-store/resource-manager/Microsoft.DataLakeStore/stable/2016-11-01/examples/FirewallRules_Get.json
     */
    /**
     * Sample code: Gets the specified Data Lake Store firewall rule.
     *
     * @param manager Entry point to DataLakeStoreManager.
     */
    public static void getsTheSpecifiedDataLakeStoreFirewallRule(
        com.azure.resourcemanager.datalakestore.DataLakeStoreManager manager) {
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
     * x-ms-original-file: specification/datalake-store/resource-manager/Microsoft.DataLakeStore/stable/2016-11-01/examples/FirewallRules_ListByAccount.json
     */
    /**
     * Sample code: Lists the Data Lake Store firewall rules within the specified Data Lake Store account.
     *
     * @param manager Entry point to DataLakeStoreManager.
     */
    public static void listsTheDataLakeStoreFirewallRulesWithinTheSpecifiedDataLakeStoreAccount(
        com.azure.resourcemanager.datalakestore.DataLakeStoreManager manager) {
        manager.firewallRules().listByAccount("contosorg", "contosoadla", Context.NONE);
    }
}
```

### FirewallRules_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datalakestore.models.FirewallRule;

/** Samples for FirewallRules Update. */
public final class FirewallRulesUpdateSamples {
    /*
     * x-ms-original-file: specification/datalake-store/resource-manager/Microsoft.DataLakeStore/stable/2016-11-01/examples/FirewallRules_Update.json
     */
    /**
     * Sample code: Updates the specified firewall rule.
     *
     * @param manager Entry point to DataLakeStoreManager.
     */
    public static void updatesTheSpecifiedFirewallRule(
        com.azure.resourcemanager.datalakestore.DataLakeStoreManager manager) {
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
     * x-ms-original-file: specification/datalake-store/resource-manager/Microsoft.DataLakeStore/stable/2016-11-01/examples/Locations_GetCapability.json
     */
    /**
     * Sample code: Gets subscription-level properties and limits for Data Lake Store specified by resource location.
     *
     * @param manager Entry point to DataLakeStoreManager.
     */
    public static void getsSubscriptionLevelPropertiesAndLimitsForDataLakeStoreSpecifiedByResourceLocation(
        com.azure.resourcemanager.datalakestore.DataLakeStoreManager manager) {
        manager.locations().getCapabilityWithResponse("EastUS2", Context.NONE);
    }
}
```

### Locations_GetUsage

```java
import com.azure.core.util.Context;

/** Samples for Locations GetUsage. */
public final class LocationsGetUsageSamples {
    /*
     * x-ms-original-file: specification/datalake-store/resource-manager/Microsoft.DataLakeStore/stable/2016-11-01/examples/Locations_GetUsage.json
     */
    /**
     * Sample code: UsageList.
     *
     * @param manager Entry point to DataLakeStoreManager.
     */
    public static void usageList(com.azure.resourcemanager.datalakestore.DataLakeStoreManager manager) {
        manager.locations().getUsage("WestUS", Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/datalake-store/resource-manager/Microsoft.DataLakeStore/stable/2016-11-01/examples/Operations_List.json
     */
    /**
     * Sample code: Lists all of the available Data Lake Store REST API operations.
     *
     * @param manager Entry point to DataLakeStoreManager.
     */
    public static void listsAllOfTheAvailableDataLakeStoreRESTAPIOperations(
        com.azure.resourcemanager.datalakestore.DataLakeStoreManager manager) {
        manager.operations().listWithResponse(Context.NONE);
    }
}
```

### TrustedIdProviders_CreateOrUpdate

```java
/** Samples for TrustedIdProviders CreateOrUpdate. */
public final class TrustedIdProvidersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/datalake-store/resource-manager/Microsoft.DataLakeStore/stable/2016-11-01/examples/TrustedIdProviders_CreateOrUpdate.json
     */
    /**
     * Sample code: Creates or updates the specified trusted identity provider. During update, the trusted identity
     * provider with the specified name will be replaced with this new provider.
     *
     * @param manager Entry point to DataLakeStoreManager.
     */
    public static void
        createsOrUpdatesTheSpecifiedTrustedIdentityProviderDuringUpdateTheTrustedIdentityProviderWithTheSpecifiedNameWillBeReplacedWithThisNewProvider(
            com.azure.resourcemanager.datalakestore.DataLakeStoreManager manager) {
        manager
            .trustedIdProviders()
            .define("test_trusted_id_provider_name")
            .withExistingAccount("contosorg", "contosoadla")
            .withIdProvider("https://sts.windows.net/ea9ec534-a3e3-4e45-ad36-3afc5bb291c1")
            .create();
    }
}
```

### TrustedIdProviders_Delete

```java
import com.azure.core.util.Context;

/** Samples for TrustedIdProviders Delete. */
public final class TrustedIdProvidersDeleteSamples {
    /*
     * x-ms-original-file: specification/datalake-store/resource-manager/Microsoft.DataLakeStore/stable/2016-11-01/examples/TrustedIdProviders_Delete.json
     */
    /**
     * Sample code: Deletes the specified trusted identity provider from the specified Data Lake Store account.
     *
     * @param manager Entry point to DataLakeStoreManager.
     */
    public static void deletesTheSpecifiedTrustedIdentityProviderFromTheSpecifiedDataLakeStoreAccount(
        com.azure.resourcemanager.datalakestore.DataLakeStoreManager manager) {
        manager
            .trustedIdProviders()
            .deleteWithResponse("contosorg", "contosoadla", "test_trusted_id_provider_name", Context.NONE);
    }
}
```

### TrustedIdProviders_Get

```java
import com.azure.core.util.Context;

/** Samples for TrustedIdProviders Get. */
public final class TrustedIdProvidersGetSamples {
    /*
     * x-ms-original-file: specification/datalake-store/resource-manager/Microsoft.DataLakeStore/stable/2016-11-01/examples/TrustedIdProviders_Get.json
     */
    /**
     * Sample code: Gets the specified Data Lake Store trusted identity provider.
     *
     * @param manager Entry point to DataLakeStoreManager.
     */
    public static void getsTheSpecifiedDataLakeStoreTrustedIdentityProvider(
        com.azure.resourcemanager.datalakestore.DataLakeStoreManager manager) {
        manager
            .trustedIdProviders()
            .getWithResponse("contosorg", "contosoadla", "test_trusted_id_provider_name", Context.NONE);
    }
}
```

### TrustedIdProviders_ListByAccount

```java
import com.azure.core.util.Context;

/** Samples for TrustedIdProviders ListByAccount. */
public final class TrustedIdProvidersListByAccountSamples {
    /*
     * x-ms-original-file: specification/datalake-store/resource-manager/Microsoft.DataLakeStore/stable/2016-11-01/examples/TrustedIdProviders_ListByAccount.json
     */
    /**
     * Sample code: Lists the Data Lake Store trusted identity providers within the specified Data Lake Store account.
     *
     * @param manager Entry point to DataLakeStoreManager.
     */
    public static void listsTheDataLakeStoreTrustedIdentityProvidersWithinTheSpecifiedDataLakeStoreAccount(
        com.azure.resourcemanager.datalakestore.DataLakeStoreManager manager) {
        manager.trustedIdProviders().listByAccount("contosorg", "contosoadla", Context.NONE);
    }
}
```

### TrustedIdProviders_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datalakestore.models.TrustedIdProvider;

/** Samples for TrustedIdProviders Update. */
public final class TrustedIdProvidersUpdateSamples {
    /*
     * x-ms-original-file: specification/datalake-store/resource-manager/Microsoft.DataLakeStore/stable/2016-11-01/examples/TrustedIdProviders_Update.json
     */
    /**
     * Sample code: Updates the specified trusted identity provider.
     *
     * @param manager Entry point to DataLakeStoreManager.
     */
    public static void updatesTheSpecifiedTrustedIdentityProvider(
        com.azure.resourcemanager.datalakestore.DataLakeStoreManager manager) {
        TrustedIdProvider resource =
            manager
                .trustedIdProviders()
                .getWithResponse("contosorg", "contosoadla", "test_trusted_id_provider_name", Context.NONE)
                .getValue();
        resource.update().withIdProvider("https://sts.windows.net/ea9ec534-a3e3-4e45-ad36-3afc5bb291c1").apply();
    }
}
```

### VirtualNetworkRules_CreateOrUpdate

```java
/** Samples for VirtualNetworkRules CreateOrUpdate. */
public final class VirtualNetworkRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/datalake-store/resource-manager/Microsoft.DataLakeStore/stable/2016-11-01/examples/VirtualNetworkRules_CreateOrUpdate.json
     */
    /**
     * Sample code: Creates or updates the specified virtual network rule. During update, the virtual network rule with
     * the specified name will be replaced with this new virtual network rule.
     *
     * @param manager Entry point to DataLakeStoreManager.
     */
    public static void
        createsOrUpdatesTheSpecifiedVirtualNetworkRuleDuringUpdateTheVirtualNetworkRuleWithTheSpecifiedNameWillBeReplacedWithThisNewVirtualNetworkRule(
            com.azure.resourcemanager.datalakestore.DataLakeStoreManager manager) {
        manager
            .virtualNetworkRules()
            .define("test_virtual_network_rules_name")
            .withExistingAccount("contosorg", "contosoadla")
            .withSubnetId("test_subnetId")
            .create();
    }
}
```

### VirtualNetworkRules_Delete

```java
import com.azure.core.util.Context;

/** Samples for VirtualNetworkRules Delete. */
public final class VirtualNetworkRulesDeleteSamples {
    /*
     * x-ms-original-file: specification/datalake-store/resource-manager/Microsoft.DataLakeStore/stable/2016-11-01/examples/VirtualNetworkRules_Delete.json
     */
    /**
     * Sample code: Deletes the specified virtual network rule from the specified Data Lake Store account.
     *
     * @param manager Entry point to DataLakeStoreManager.
     */
    public static void deletesTheSpecifiedVirtualNetworkRuleFromTheSpecifiedDataLakeStoreAccount(
        com.azure.resourcemanager.datalakestore.DataLakeStoreManager manager) {
        manager
            .virtualNetworkRules()
            .deleteWithResponse("contosorg", "contosoadla", "test_virtual_network_rules_name", Context.NONE);
    }
}
```

### VirtualNetworkRules_Get

```java
import com.azure.core.util.Context;

/** Samples for VirtualNetworkRules Get. */
public final class VirtualNetworkRulesGetSamples {
    /*
     * x-ms-original-file: specification/datalake-store/resource-manager/Microsoft.DataLakeStore/stable/2016-11-01/examples/VirtualNetworkRules_Get.json
     */
    /**
     * Sample code: Gets the specified Data Lake Store virtual network rule.
     *
     * @param manager Entry point to DataLakeStoreManager.
     */
    public static void getsTheSpecifiedDataLakeStoreVirtualNetworkRule(
        com.azure.resourcemanager.datalakestore.DataLakeStoreManager manager) {
        manager
            .virtualNetworkRules()
            .getWithResponse("contosorg", "contosoadla", "test_virtual_network_rules_name", Context.NONE);
    }
}
```

### VirtualNetworkRules_ListByAccount

```java
import com.azure.core.util.Context;

/** Samples for VirtualNetworkRules ListByAccount. */
public final class VirtualNetworkRulesListByAccountSamples {
    /*
     * x-ms-original-file: specification/datalake-store/resource-manager/Microsoft.DataLakeStore/stable/2016-11-01/examples/VirtualNetworkRules_ListByAccount.json
     */
    /**
     * Sample code: Lists the Data Lake Store virtual network rules within the specified Data Lake Store account.
     *
     * @param manager Entry point to DataLakeStoreManager.
     */
    public static void listsTheDataLakeStoreVirtualNetworkRulesWithinTheSpecifiedDataLakeStoreAccount(
        com.azure.resourcemanager.datalakestore.DataLakeStoreManager manager) {
        manager.virtualNetworkRules().listByAccount("contosorg", "contosoadla", Context.NONE);
    }
}
```

### VirtualNetworkRules_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.datalakestore.models.VirtualNetworkRule;

/** Samples for VirtualNetworkRules Update. */
public final class VirtualNetworkRulesUpdateSamples {
    /*
     * x-ms-original-file: specification/datalake-store/resource-manager/Microsoft.DataLakeStore/stable/2016-11-01/examples/VirtualNetworkRules_Update.json
     */
    /**
     * Sample code: Updates the specified virtual network rule.
     *
     * @param manager Entry point to DataLakeStoreManager.
     */
    public static void updatesTheSpecifiedVirtualNetworkRule(
        com.azure.resourcemanager.datalakestore.DataLakeStoreManager manager) {
        VirtualNetworkRule resource =
            manager
                .virtualNetworkRules()
                .getWithResponse("contosorg", "contosoadla", "test_virtual_network_rules_name", Context.NONE)
                .getValue();
        resource.update().withSubnetId("test_subnetId").apply();
    }
}
```


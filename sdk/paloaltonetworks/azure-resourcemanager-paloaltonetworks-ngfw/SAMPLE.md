# Code snippets and samples


## CertificateObjectGlobalRulestack

- [CreateOrUpdate](#certificateobjectglobalrulestack_createorupdate)
- [Delete](#certificateobjectglobalrulestack_delete)
- [Get](#certificateobjectglobalrulestack_get)
- [List](#certificateobjectglobalrulestack_list)

## CertificateObjectLocalRulestack

- [CreateOrUpdate](#certificateobjectlocalrulestack_createorupdate)
- [Delete](#certificateobjectlocalrulestack_delete)
- [Get](#certificateobjectlocalrulestack_get)
- [ListByLocalRulestacks](#certificateobjectlocalrulestack_listbylocalrulestacks)

## FirewallStatus

- [Get](#firewallstatus_get)
- [ListByFirewalls](#firewallstatus_listbyfirewalls)

## Firewalls

- [CreateOrUpdate](#firewalls_createorupdate)
- [Delete](#firewalls_delete)
- [GetByResourceGroup](#firewalls_getbyresourcegroup)
- [GetGlobalRulestack](#firewalls_getglobalrulestack)
- [GetLogProfile](#firewalls_getlogprofile)
- [GetSupportInfo](#firewalls_getsupportinfo)
- [List](#firewalls_list)
- [ListByResourceGroup](#firewalls_listbyresourcegroup)
- [SaveLogProfile](#firewalls_savelogprofile)
- [Update](#firewalls_update)

## FqdnListGlobalRulestack

- [CreateOrUpdate](#fqdnlistglobalrulestack_createorupdate)
- [Delete](#fqdnlistglobalrulestack_delete)
- [Get](#fqdnlistglobalrulestack_get)
- [List](#fqdnlistglobalrulestack_list)

## FqdnListLocalRulestack

- [CreateOrUpdate](#fqdnlistlocalrulestack_createorupdate)
- [Delete](#fqdnlistlocalrulestack_delete)
- [Get](#fqdnlistlocalrulestack_get)
- [ListByLocalRulestacks](#fqdnlistlocalrulestack_listbylocalrulestacks)

## GlobalRulestack

- [Commit](#globalrulestack_commit)
- [CreateOrUpdate](#globalrulestack_createorupdate)
- [Delete](#globalrulestack_delete)
- [Get](#globalrulestack_get)
- [GetChangeLog](#globalrulestack_getchangelog)
- [List](#globalrulestack_list)
- [ListAdvancedSecurityObjects](#globalrulestack_listadvancedsecurityobjects)
- [ListAppIds](#globalrulestack_listappids)
- [ListCountries](#globalrulestack_listcountries)
- [ListFirewalls](#globalrulestack_listfirewalls)
- [ListPredefinedUrlCategories](#globalrulestack_listpredefinedurlcategories)
- [ListSecurityServices](#globalrulestack_listsecurityservices)
- [Revert](#globalrulestack_revert)
- [Update](#globalrulestack_update)

## LocalRules

- [CreateOrUpdate](#localrules_createorupdate)
- [Delete](#localrules_delete)
- [Get](#localrules_get)
- [GetCounters](#localrules_getcounters)
- [ListByLocalRulestacks](#localrules_listbylocalrulestacks)
- [RefreshCounters](#localrules_refreshcounters)
- [ResetCounters](#localrules_resetcounters)

## LocalRulestacks

- [Commit](#localrulestacks_commit)
- [CreateOrUpdate](#localrulestacks_createorupdate)
- [Delete](#localrulestacks_delete)
- [GetByResourceGroup](#localrulestacks_getbyresourcegroup)
- [GetChangeLog](#localrulestacks_getchangelog)
- [GetSupportInfo](#localrulestacks_getsupportinfo)
- [List](#localrulestacks_list)
- [ListAdvancedSecurityObjects](#localrulestacks_listadvancedsecurityobjects)
- [ListAppIds](#localrulestacks_listappids)
- [ListByResourceGroup](#localrulestacks_listbyresourcegroup)
- [ListCountries](#localrulestacks_listcountries)
- [ListFirewalls](#localrulestacks_listfirewalls)
- [ListPredefinedUrlCategories](#localrulestacks_listpredefinedurlcategories)
- [ListSecurityServices](#localrulestacks_listsecurityservices)
- [Revert](#localrulestacks_revert)
- [Update](#localrulestacks_update)

## MetricsObjectFirewall

- [CreateOrUpdate](#metricsobjectfirewall_createorupdate)
- [Delete](#metricsobjectfirewall_delete)
- [Get](#metricsobjectfirewall_get)
- [ListByFirewalls](#metricsobjectfirewall_listbyfirewalls)

## Operations

- [List](#operations_list)

## PaloAltoNetworksCloudngfwOperations

- [CreateProductSerialNumber](#paloaltonetworkscloudngfwoperations_createproductserialnumber)
- [ListCloudManagerTenants](#paloaltonetworkscloudngfwoperations_listcloudmanagertenants)
- [ListProductSerialNumberStatus](#paloaltonetworkscloudngfwoperations_listproductserialnumberstatus)
- [ListSupportInfo](#paloaltonetworkscloudngfwoperations_listsupportinfo)

## PostRules

- [CreateOrUpdate](#postrules_createorupdate)
- [Delete](#postrules_delete)
- [Get](#postrules_get)
- [GetCounters](#postrules_getcounters)
- [List](#postrules_list)
- [RefreshCounters](#postrules_refreshcounters)
- [ResetCounters](#postrules_resetcounters)

## PreRules

- [CreateOrUpdate](#prerules_createorupdate)
- [Delete](#prerules_delete)
- [Get](#prerules_get)
- [GetCounters](#prerules_getcounters)
- [List](#prerules_list)
- [RefreshCounters](#prerules_refreshcounters)
- [ResetCounters](#prerules_resetcounters)

## PrefixListGlobalRulestack

- [CreateOrUpdate](#prefixlistglobalrulestack_createorupdate)
- [Delete](#prefixlistglobalrulestack_delete)
- [Get](#prefixlistglobalrulestack_get)
- [List](#prefixlistglobalrulestack_list)

## PrefixListLocalRulestack

- [CreateOrUpdate](#prefixlistlocalrulestack_createorupdate)
- [Delete](#prefixlistlocalrulestack_delete)
- [Get](#prefixlistlocalrulestack_get)
- [ListByLocalRulestacks](#prefixlistlocalrulestack_listbylocalrulestacks)
### CertificateObjectGlobalRulestack_CreateOrUpdate

```java
import com.azure.resourcemanager.paloaltonetworks.ngfw.fluent.models.CertificateObjectGlobalRulestackResourceInner;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.BooleanEnum;

/**
 * Samples for CertificateObjectGlobalRulestack CreateOrUpdate.
 */
public final class CertificateObjectGlobalRulestackCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-10-08/CertificateObjectGlobalRulestack_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: CertificateObjectGlobalRulestack_CreateOrUpdate_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void certificateObjectGlobalRulestackCreateOrUpdateMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.certificateObjectGlobalRulestacks()
            .createOrUpdate("praval", "armid1",
                new CertificateObjectGlobalRulestackResourceInner().withCertificateSelfSigned(BooleanEnum.TRUE),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/CertificateObjectGlobalRulestack_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: CertificateObjectGlobalRulestack_CreateOrUpdate_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void certificateObjectGlobalRulestackCreateOrUpdateMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.certificateObjectGlobalRulestacks()
            .createOrUpdate("praval", "armid1",
                new CertificateObjectGlobalRulestackResourceInner().withCertificateSignerResourceId("")
                    .withCertificateSelfSigned(BooleanEnum.TRUE)
                    .withAuditComment("comment")
                    .withDescription("description")
                    .withEtag("2bf4a339-294d-4c25-b0b2-ef649e9f5c27"),
                com.azure.core.util.Context.NONE);
    }
}
```

### CertificateObjectGlobalRulestack_Delete

```java
/**
 * Samples for CertificateObjectGlobalRulestack Delete.
 */
public final class CertificateObjectGlobalRulestackDeleteSamples {
    /*
     * x-ms-original-file: 2025-10-08/CertificateObjectGlobalRulestack_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: CertificateObjectGlobalRulestack_Delete_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void certificateObjectGlobalRulestackDeleteMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.certificateObjectGlobalRulestacks().delete("praval", "armid1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/CertificateObjectGlobalRulestack_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: CertificateObjectGlobalRulestack_Delete_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void certificateObjectGlobalRulestackDeleteMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.certificateObjectGlobalRulestacks().delete("praval", "armid1", com.azure.core.util.Context.NONE);
    }
}
```

### CertificateObjectGlobalRulestack_Get

```java
/**
 * Samples for CertificateObjectGlobalRulestack Get.
 */
public final class CertificateObjectGlobalRulestackGetSamples {
    /*
     * x-ms-original-file: 2025-10-08/CertificateObjectGlobalRulestack_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: CertificateObjectGlobalRulestack_Get_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void certificateObjectGlobalRulestackGetMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.certificateObjectGlobalRulestacks()
            .getWithResponse("praval", "armid1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/CertificateObjectGlobalRulestack_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: CertificateObjectGlobalRulestack_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void certificateObjectGlobalRulestackGetMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.certificateObjectGlobalRulestacks()
            .getWithResponse("praval", "armid1", com.azure.core.util.Context.NONE);
    }
}
```

### CertificateObjectGlobalRulestack_List

```java
/**
 * Samples for CertificateObjectGlobalRulestack List.
 */
public final class CertificateObjectGlobalRulestackListSamples {
    /*
     * x-ms-original-file: 2025-10-08/CertificateObjectGlobalRulestack_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: CertificateObjectGlobalRulestack_List_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void certificateObjectGlobalRulestackListMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.certificateObjectGlobalRulestacks().list("praval", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/CertificateObjectGlobalRulestack_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: CertificateObjectGlobalRulestack_List_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void certificateObjectGlobalRulestackListMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.certificateObjectGlobalRulestacks().list("praval", com.azure.core.util.Context.NONE);
    }
}
```

### CertificateObjectLocalRulestack_CreateOrUpdate

```java
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.BooleanEnum;

/**
 * Samples for CertificateObjectLocalRulestack CreateOrUpdate.
 */
public final class CertificateObjectLocalRulestackCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-10-08/CertificateObjectLocalRulestack_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: CertificateObjectLocalRulestack_CreateOrUpdate_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void certificateObjectLocalRulestackCreateOrUpdateMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.certificateObjectLocalRulestacks()
            .define("armid1")
            .withExistingLocalRulestack("rgopenapi", "lrs1")
            .withCertificateSelfSigned(BooleanEnum.TRUE)
            .create();
    }

    /*
     * x-ms-original-file: 2025-10-08/CertificateObjectLocalRulestack_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: CertificateObjectLocalRulestack_CreateOrUpdate_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void certificateObjectLocalRulestackCreateOrUpdateMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.certificateObjectLocalRulestacks()
            .define("armid1")
            .withExistingLocalRulestack("rgopenapi", "lrs1")
            .withCertificateSelfSigned(BooleanEnum.TRUE)
            .withCertificateSignerResourceId("")
            .withAuditComment("comment")
            .withDescription("description")
            .withEtag("2bf4a339-294d-4c25-b0b2-ef649e9f5c27")
            .create();
    }
}
```

### CertificateObjectLocalRulestack_Delete

```java
/**
 * Samples for CertificateObjectLocalRulestack Delete.
 */
public final class CertificateObjectLocalRulestackDeleteSamples {
    /*
     * x-ms-original-file: 2025-10-08/CertificateObjectLocalRulestack_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: CertificateObjectLocalRulestack_Delete_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void certificateObjectLocalRulestackDeleteMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.certificateObjectLocalRulestacks()
            .delete("rgopenapi", "lrs1", "armid1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/CertificateObjectLocalRulestack_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: CertificateObjectLocalRulestack_Delete_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void certificateObjectLocalRulestackDeleteMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.certificateObjectLocalRulestacks()
            .delete("rgopenapi", "lrs1", "armid1", com.azure.core.util.Context.NONE);
    }
}
```

### CertificateObjectLocalRulestack_Get

```java
/**
 * Samples for CertificateObjectLocalRulestack Get.
 */
public final class CertificateObjectLocalRulestackGetSamples {
    /*
     * x-ms-original-file: 2025-10-08/CertificateObjectLocalRulestack_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: CertificateObjectLocalRulestack_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void certificateObjectLocalRulestackGetMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.certificateObjectLocalRulestacks()
            .getWithResponse("rgopenapi", "lrs1", "armid1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/CertificateObjectLocalRulestack_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: CertificateObjectLocalRulestack_Get_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void certificateObjectLocalRulestackGetMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.certificateObjectLocalRulestacks()
            .getWithResponse("rgopenapi", "lrs1", "armid1", com.azure.core.util.Context.NONE);
    }
}
```

### CertificateObjectLocalRulestack_ListByLocalRulestacks

```java
/**
 * Samples for CertificateObjectLocalRulestack ListByLocalRulestacks.
 */
public final class CertificateObjectLocalRulestackListByLocalRulestacksSamples {
    /*
     * x-ms-original-file: 2025-10-08/CertificateObjectLocalRulestack_ListByLocalRulestacks_MaximumSet_Gen.json
     */
    /**
     * Sample code: CertificateObjectLocalRulestack_ListByLocalRulestacks_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void certificateObjectLocalRulestackListByLocalRulestacksMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.certificateObjectLocalRulestacks()
            .listByLocalRulestacks("rgopenapi", "lrs1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/CertificateObjectLocalRulestack_ListByLocalRulestacks_MinimumSet_Gen.json
     */
    /**
     * Sample code: CertificateObjectLocalRulestack_ListByLocalRulestacks_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void certificateObjectLocalRulestackListByLocalRulestacksMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.certificateObjectLocalRulestacks()
            .listByLocalRulestacks("rgopenapi", "lrs1", com.azure.core.util.Context.NONE);
    }
}
```

### FirewallStatus_Get

```java
/**
 * Samples for FirewallStatus Get.
 */
public final class FirewallStatusGetSamples {
    /*
     * x-ms-original-file: 2025-10-08/FirewallStatus_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: FirewallStatus_Get_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void firewallStatusGetMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.firewallStatus().getWithResponse("rgopenapi", "firewall1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/FirewallStatus_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: FirewallStatus_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void firewallStatusGetMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.firewallStatus().getWithResponse("rgopenapi", "firewall1", com.azure.core.util.Context.NONE);
    }
}
```

### FirewallStatus_ListByFirewalls

```java
/**
 * Samples for FirewallStatus ListByFirewalls.
 */
public final class FirewallStatusListByFirewallsSamples {
    /*
     * x-ms-original-file: 2025-10-08/FirewallStatus_ListByFirewalls_MaximumSet_Gen.json
     */
    /**
     * Sample code: FirewallStatus_ListByFirewalls_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void firewallStatusListByFirewallsMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.firewallStatus().listByFirewalls("rgopenapi", "firewall1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/FirewallStatus_ListByFirewalls_MinimumSet_Gen.json
     */
    /**
     * Sample code: FirewallStatus_ListByFirewalls_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void firewallStatusListByFirewallsMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.firewallStatus().listByFirewalls("rgopenapi", "firewall1", com.azure.core.util.Context.NONE);
    }
}
```

### Firewalls_CreateOrUpdate

```java
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.AzureResourceManagerManagedIdentityProperties;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.AzureResourceManagerUserAssignedIdentity;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.BillingCycle;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.BooleanEnum;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.DnsProxy;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.DnsSettings;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.EgressNat;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.EnabledDnsType;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.EndpointConfiguration;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.FrontendSetting;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.IpAddress;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.IpAddressSpace;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.ManagedIdentityType;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.MarketplaceDetails;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.MarketplaceSubscriptionStatus;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.NetworkProfile;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.NetworkType;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.PanoramaConfig;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.PlanData;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.ProtocolType;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.RulestackDetails;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.UsageType;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.VnetConfiguration;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.VwanConfiguration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Firewalls CreateOrUpdate.
 */
public final class FirewallsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-10-08/Firewalls_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firewalls_CreateOrUpdate_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void firewallsCreateOrUpdateMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.firewalls()
            .define("firewall1")
            .withRegion("eastus")
            .withExistingResourceGroup("firewall-rg")
            .withNetworkProfile(new NetworkProfile().withVnetConfiguration(new VnetConfiguration()
                .withVnet(new IpAddressSpace().withResourceId(
                    "/subscriptions/2bf4a339-294d-4c25-b0b2-ef649e9f5c27/resourceGroups/os-liftr-integration/providers/Microsoft.Network/virtualNetworks/os-liftr-integration-vnet")
                    .withAddressSpace("10.1.0.0/16"))
                .withTrustSubnet(new IpAddressSpace().withResourceId(
                    "/subscriptions/2bf4a339-294d-4c25-b0b2-ef649e9f5c27/resourceGroups/os-liftr-integration/providers/Microsoft.Network/virtualNetworks/os-liftr-integration-vnet/subnets/os-liftr-integration-trust-subnet")
                    .withAddressSpace("10.1.1.0/24"))
                .withUnTrustSubnet(new IpAddressSpace().withResourceId(
                    "/subscriptions/2bf4a339-294d-4c25-b0b2-ef649e9f5c27/resourceGroups/os-liftr-integration/providers/Microsoft.Network/virtualNetworks/os-liftr-integration-vnet/subnets/os-liftr-integration-untrust-subnet")
                    .withAddressSpace("10.1.1.0/24"))
                .withIpOfTrustSubnetForUdr(new IpAddress().withResourceId(
                    "/subscriptions/2bf4a339-294d-4c25-b0b2-ef649e9f5c27/resourceGroups/os-liftr-integration/providers/Microsoft.Network/virtualNetworks/os-liftr-integration-vnet/subnets/os-liftr-integration-untrust-subnet")
                    .withAddress("10.1.1.0/24")))
                .withVwanConfiguration(new VwanConfiguration()
                    .withNetworkVirtualApplianceId("2bf4a339-294d-4c25-b0b2-ef649e9f5c12")
                    .withVHub(new IpAddressSpace().withResourceId(
                        "/subscriptions/2bf4a339-294d-4c25-b0b2-ef649e9f5c27/resourceGroups/os-liftr-integration/providers/Microsoft.Network/virtualNetworks/os-liftr-integration-vnet/subnets/os-liftr-integration-untrust-subnet")
                        .withAddressSpace("10.1.1.0/24"))
                    .withTrustSubnet(new IpAddressSpace().withResourceId(
                        "/subscriptions/2bf4a339-294d-4c25-b0b2-ef649e9f5c27/resourceGroups/os-liftr-integration/providers/Microsoft.Network/virtualNetworks/os-liftr-integration-vnet/subnets/os-liftr-integration-trust-subnet")
                        .withAddressSpace("10.1.1.0/24"))
                    .withUnTrustSubnet(new IpAddressSpace().withResourceId(
                        "/subscriptions/2bf4a339-294d-4c25-b0b2-ef649e9f5c27/resourceGroups/os-liftr-integration/providers/Microsoft.Network/virtualNetworks/os-liftr-integration-vnet/subnets/os-liftr-integration-untrust-subnet")
                        .withAddressSpace("10.1.1.0/24"))
                    .withIpOfTrustSubnetForUdr(new IpAddress().withResourceId(
                        "/subscriptions/2bf4a339-294d-4c25-b0b2-ef649e9f5c27/resourceGroups/os-liftr-integration/providers/Microsoft.Network/virtualNetworks/os-liftr-integration-vnet/subnets/os-liftr-integration-untrust-subnet")
                        .withAddress("10.1.1.0/24")))
                .withNetworkType(NetworkType.VNET)
                .withPublicIps(Arrays.asList(new IpAddress().withResourceId(
                    "/subscriptions/01c7d41f-afaf-464e-8a8b-5c6f9f98cee8/resourceGroups/mj-liftr-integration/providers/Microsoft.Network/publicIPAddresses/mj-liftr-integration-PublicIp1")
                    .withAddress("20.22.92.11")))
                .withEnableEgressNat(EgressNat.ENABLED)
                .withEgressNatIp(Arrays.asList(new IpAddress().withResourceId(
                    "/subscriptions/01c7d41f-afaf-464e-8a8b-5c6f9f98cee8/resourceGroups/mj-liftr-integration/providers/Microsoft.Network/publicIPAddresses/mj-liftr-integration-egressNatIp1")
                    .withAddress("20.22.92.111")))
                .withTrustedRanges(Arrays.asList("20.22.92.11"))
                .withPrivateSourceNatRulesDestination(Arrays.asList("20.22.92.11")))
            .withDnsSettings(new DnsSettings().withEnableDnsProxy(DnsProxy.DISABLED)
                .withEnabledDnsType(EnabledDnsType.CUSTOM)
                .withDnsServers(Arrays.asList(new IpAddress().withResourceId(
                    "/subscriptions/01c7d41f-afaf-464e-8a8b-5c6f9f98cee8/resourceGroups/mj-liftr-integration/providers/Microsoft.Network/publicIPAddresses/mj-liftr-integration-egressNatIp1")
                    .withAddress("20.22.92.111"))))
            .withPlanData(new PlanData().withUsageType(UsageType.PAYG)
                .withBillingCycle(BillingCycle.MONTHLY)
                .withPlanId("liftrpantestplan"))
            .withMarketplaceDetails(new MarketplaceDetails().withOfferId("liftr-pan-ame-test")
                .withPublisherId("isvtestuklegacy")
                .withMarketplaceSubscriptionStatus(MarketplaceSubscriptionStatus.PENDING_FULFILLMENT_START))
            .withTags(mapOf("tagName", "value"))
            .withIdentity(new AzureResourceManagerManagedIdentityProperties().withType(ManagedIdentityType.NONE)
                .withUserAssignedIdentities(mapOf("key16",
                    new AzureResourceManagerUserAssignedIdentity().withClientId("aaaa")
                        .withPrincipalId("aaaaaaaaaaaaaaa"))))
            .withPanEtag("2bf4a339-294d-4c25-b0b2-ef649e9f5c12")
            .withIsPanoramaManaged(BooleanEnum.TRUE)
            .withPanoramaConfig(new PanoramaConfig().withConfigString("bas64EncodedString"))
            .withAssociatedRulestack(
                new RulestackDetails().withResourceId("lrs1").withRulestackId("PANRSID").withLocation("eastus"))
            .withFrontEndSettings(Arrays.asList(new FrontendSetting().withName("frontendsetting11")
                .withProtocol(ProtocolType.TCP)
                .withFrontendConfiguration(new EndpointConfiguration().withPort("80")
                    .withAddress(new IpAddress().withResourceId(
                        "/subscriptions/01c7d41f-afaf-464e-8a8b-5c6f9f98cee8/resourceGroups/mj-liftr-integration/providers/Microsoft.Network/publicIPAddresses/mj-liftr-integration-frontendSettingIp1")
                        .withAddress("20.22.91.251")))
                .withBackendConfiguration(new EndpointConfiguration().withPort("80")
                    .withAddress(new IpAddress().withResourceId(
                        "/subscriptions/01c7d41f-afaf-464e-8a8b-5c6f9f98cee8/resourceGroups/mj-liftr-integration/providers/Microsoft.Network/publicIPAddresses/mj-liftr-integration-frontendSettingIp2")
                        .withAddress("20.22.32.136")))))
            .create();
    }

    /*
     * x-ms-original-file: 2025-10-08/Firewalls_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firewalls_CreateOrUpdate_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void firewallsCreateOrUpdateMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.firewalls()
            .define("firewall1")
            .withRegion("eastus")
            .withExistingResourceGroup("firewall-rg")
            .withNetworkProfile(new NetworkProfile().withNetworkType(NetworkType.VNET)
                .withPublicIps(Arrays.asList(new IpAddress().withResourceId(
                    "/subscriptions/01c7d41f-afaf-464e-8a8b-5c6f9f98cee8/resourceGroups/mj-liftr-integration/providers/Microsoft.Network/publicIPAddresses/mj-liftr-integration-PublicIp1")
                    .withAddress("20.22.92.11")))
                .withEnableEgressNat(EgressNat.ENABLED))
            .withDnsSettings(new DnsSettings())
            .withPlanData(new PlanData().withBillingCycle(BillingCycle.MONTHLY).withPlanId("liftrpantestplan"))
            .withMarketplaceDetails(
                new MarketplaceDetails().withOfferId("liftr-pan-ame-test").withPublisherId("isvtestuklegacy"))
            .create();
    }

    // Use "Map.of" if available
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

### Firewalls_Delete

```java
/**
 * Samples for Firewalls Delete.
 */
public final class FirewallsDeleteSamples {
    /*
     * x-ms-original-file: 2025-10-08/Firewalls_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firewalls_Delete_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void firewallsDeleteMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.firewalls().delete("firewall-rg", "firewall1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/Firewalls_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firewalls_Delete_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void firewallsDeleteMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.firewalls().delete("firewall-rg", "firewall1", com.azure.core.util.Context.NONE);
    }
}
```

### Firewalls_GetByResourceGroup

```java
/**
 * Samples for Firewalls GetByResourceGroup.
 */
public final class FirewallsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-10-08/Firewalls_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firewalls_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void
        firewallsGetMaximumSetGen(com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.firewalls()
            .getByResourceGroupWithResponse("firewall-rg", "firewall1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/Firewalls_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firewalls_Get_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void
        firewallsGetMinimumSetGen(com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.firewalls()
            .getByResourceGroupWithResponse("firewall-rg", "firewall1", com.azure.core.util.Context.NONE);
    }
}
```

### Firewalls_GetGlobalRulestack

```java
/**
 * Samples for Firewalls GetGlobalRulestack.
 */
public final class FirewallsGetGlobalRulestackSamples {
    /*
     * x-ms-original-file: 2025-10-08/Firewalls_getGlobalRulestack_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firewalls_getGlobalRulestack_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void firewallsGetGlobalRulestackMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.firewalls()
            .getGlobalRulestackWithResponse("firewall-rg", "firewall1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/Firewalls_getGlobalRulestack_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firewalls_getGlobalRulestack_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void firewallsGetGlobalRulestackMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.firewalls()
            .getGlobalRulestackWithResponse("firewall-rg", "firewall1", com.azure.core.util.Context.NONE);
    }
}
```

### Firewalls_GetLogProfile

```java
/**
 * Samples for Firewalls GetLogProfile.
 */
public final class FirewallsGetLogProfileSamples {
    /*
     * x-ms-original-file: 2025-10-08/Firewalls_getLogProfile_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firewalls_getLogProfile_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void firewallsGetLogProfileMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.firewalls().getLogProfileWithResponse("firewall-rg", "firewall1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/Firewalls_getLogProfile_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firewalls_getLogProfile_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void firewallsGetLogProfileMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.firewalls().getLogProfileWithResponse("firewall-rg", "firewall1", com.azure.core.util.Context.NONE);
    }
}
```

### Firewalls_GetSupportInfo

```java
/**
 * Samples for Firewalls GetSupportInfo.
 */
public final class FirewallsGetSupportInfoSamples {
    /*
     * x-ms-original-file: 2025-10-08/Firewalls_getSupportInfo_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firewalls_getSupportInfo_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void firewallsGetSupportInfoMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.firewalls()
            .getSupportInfoWithResponse("rgopenapi", "firewall1", null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/Firewalls_getSupportInfo_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firewalls_getSupportInfo_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void firewallsGetSupportInfoMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.firewalls()
            .getSupportInfoWithResponse("rgopenapi", "firewall1", "user1@domain.com", com.azure.core.util.Context.NONE);
    }
}
```

### Firewalls_List

```java
/**
 * Samples for Firewalls List.
 */
public final class FirewallsListSamples {
    /*
     * x-ms-original-file: 2025-10-08/Firewalls_ListBySubscription_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firewalls_ListBySubscription_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void firewallsListBySubscriptionMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.firewalls().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/Firewalls_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firewalls_ListBySubscription_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void firewallsListBySubscriptionMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.firewalls().list(com.azure.core.util.Context.NONE);
    }
}
```

### Firewalls_ListByResourceGroup

```java
/**
 * Samples for Firewalls ListByResourceGroup.
 */
public final class FirewallsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-10-08/Firewalls_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firewalls_ListByResourceGroup_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void firewallsListByResourceGroupMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.firewalls().listByResourceGroup("firewall-rg", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/Firewalls_ListByResourceGroup_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firewalls_ListByResourceGroup_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void firewallsListByResourceGroupMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.firewalls().listByResourceGroup("firewall-rg", com.azure.core.util.Context.NONE);
    }
}
```

### Firewalls_SaveLogProfile

```java
import com.azure.resourcemanager.paloaltonetworks.ngfw.fluent.models.LogSettingsInner;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.ApplicationInsights;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.EventHub;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.LogDestination;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.LogOption;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.LogType;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.MonitorLog;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.StorageAccount;

/**
 * Samples for Firewalls SaveLogProfile.
 */
public final class FirewallsSaveLogProfileSamples {
    /*
     * x-ms-original-file: 2025-10-08/Firewalls_saveLogProfile_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firewalls_saveLogProfile_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void firewallsSaveLogProfileMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.firewalls()
            .saveLogProfileWithResponse("firewall-rg", "firewall1",
                new LogSettingsInner().withLogType(LogType.TRAFFIC)
                    .withLogOption(LogOption.SAME_DESTINATION)
                    .withApplicationInsights(
                        new ApplicationInsights().withId("aaaaaaaaaaaaaaaa").withKey("fakeTokenPlaceholder"))
                    .withCommonDestination(new LogDestination()
                        .withStorageConfigurations(new StorageAccount().withId("aaaaaaaaaaaaaaa")
                            .withSubscriptionId("aaaaaaaaa")
                            .withAccountName("aaaaaaaaaaaaaaaaaaaaaaa"))
                        .withEventHubConfigurations(new EventHub().withId("aaaaaaaaaa")
                            .withSubscriptionId("aaaaaaaaaa")
                            .withName("aaaaaaaa")
                            .withNameSpace("aaaaaaaaaaaaaaaaaaaaa")
                            .withPolicyName("aaaaaaaaaaaa"))
                        .withMonitorConfigurations(new MonitorLog().withId("aaaaaaaaaaaaaaaaaaa")
                            .withSubscriptionId("aaaaaaaaaaaaa")
                            .withWorkspace("aaaaaaaaaaa")
                            .withPrimaryKey("fakeTokenPlaceholder")
                            .withSecondaryKey("fakeTokenPlaceholder")))
                    .withTrafficLogDestination(new LogDestination()
                        .withStorageConfigurations(new StorageAccount().withId("aaaaaaaaaaaaaaa")
                            .withSubscriptionId("aaaaaaaaa")
                            .withAccountName("aaaaaaaaaaaaaaaaaaaaaaa"))
                        .withEventHubConfigurations(new EventHub().withId("aaaaaaaaaa")
                            .withSubscriptionId("aaaaaaaaaa")
                            .withName("aaaaaaaa")
                            .withNameSpace("aaaaaaaaaaaaaaaaaaaaa")
                            .withPolicyName("aaaaaaaaaaaa"))
                        .withMonitorConfigurations(new MonitorLog().withId("aaaaaaaaaaaaaaaaaaa")
                            .withSubscriptionId("aaaaaaaaaaaaa")
                            .withWorkspace("aaaaaaaaaaa")
                            .withPrimaryKey("fakeTokenPlaceholder")
                            .withSecondaryKey("fakeTokenPlaceholder")))
                    .withThreatLogDestination(new LogDestination()
                        .withStorageConfigurations(new StorageAccount().withId("aaaaaaaaaaaaaaa")
                            .withSubscriptionId("aaaaaaaaa")
                            .withAccountName("aaaaaaaaaaaaaaaaaaaaaaa"))
                        .withEventHubConfigurations(new EventHub().withId("aaaaaaaaaa")
                            .withSubscriptionId("aaaaaaaaaa")
                            .withName("aaaaaaaa")
                            .withNameSpace("aaaaaaaaaaaaaaaaaaaaa")
                            .withPolicyName("aaaaaaaaaaaa"))
                        .withMonitorConfigurations(new MonitorLog().withId("aaaaaaaaaaaaaaaaaaa")
                            .withSubscriptionId("aaaaaaaaaaaaa")
                            .withWorkspace("aaaaaaaaaaa")
                            .withPrimaryKey("fakeTokenPlaceholder")
                            .withSecondaryKey("fakeTokenPlaceholder")))
                    .withDecryptLogDestination(new LogDestination()
                        .withStorageConfigurations(new StorageAccount().withId("aaaaaaaaaaaaaaa")
                            .withSubscriptionId("aaaaaaaaa")
                            .withAccountName("aaaaaaaaaaaaaaaaaaaaaaa"))
                        .withEventHubConfigurations(new EventHub().withId("aaaaaaaaaa")
                            .withSubscriptionId("aaaaaaaaaa")
                            .withName("aaaaaaaa")
                            .withNameSpace("aaaaaaaaaaaaaaaaaaaaa")
                            .withPolicyName("aaaaaaaaaaaa"))
                        .withMonitorConfigurations(new MonitorLog().withId("aaaaaaaaaaaaaaaaaaa")
                            .withSubscriptionId("aaaaaaaaaaaaa")
                            .withWorkspace("aaaaaaaaaaa")
                            .withPrimaryKey("fakeTokenPlaceholder")
                            .withSecondaryKey("fakeTokenPlaceholder"))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/Firewalls_saveLogProfile_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firewalls_saveLogProfile_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void firewallsSaveLogProfileMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.firewalls()
            .saveLogProfileWithResponse("firewall-rg", "firewall1", null, com.azure.core.util.Context.NONE);
    }
}
```

### Firewalls_Update

```java
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.AzureResourceManagerManagedIdentityProperties;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.AzureResourceManagerUserAssignedIdentity;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.BillingCycle;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.BooleanEnum;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.DnsProxy;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.DnsSettings;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.EgressNat;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.EnabledDnsType;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.EndpointConfiguration;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.FirewallResource;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.FirewallResourceUpdateProperties;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.FrontendSetting;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.IpAddress;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.IpAddressSpace;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.ManagedIdentityType;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.MarketplaceDetails;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.MarketplaceSubscriptionStatus;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.NetworkProfile;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.NetworkType;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.PanoramaConfig;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.PlanData;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.ProtocolType;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.RulestackDetails;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.UsageType;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.VnetConfiguration;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.VwanConfiguration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Firewalls Update.
 */
public final class FirewallsUpdateSamples {
    /*
     * x-ms-original-file: 2025-10-08/Firewalls_Update_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firewalls_Update_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void firewallsUpdateMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        FirewallResource resource = manager.firewalls()
            .getByResourceGroupWithResponse("firewall-rg", "firewall1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().apply();
    }

    /*
     * x-ms-original-file: 2025-10-08/Firewalls_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firewalls_Update_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void firewallsUpdateMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        FirewallResource resource = manager.firewalls()
            .getByResourceGroupWithResponse("firewall-rg", "firewall1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("tagName", "value"))
            .withIdentity(new AzureResourceManagerManagedIdentityProperties().withType(ManagedIdentityType.NONE)
                .withUserAssignedIdentities(mapOf("key16",
                    new AzureResourceManagerUserAssignedIdentity().withClientId("aaaa")
                        .withPrincipalId("aaaaaaaaaaaaaaa"))))
            .withProperties(new FirewallResourceUpdateProperties().withPanEtag("2bf4a339-294d-4c25-b0b2-ef649e9f5c12")
                .withNetworkProfile(new NetworkProfile().withVnetConfiguration(new VnetConfiguration()
                    .withVnet(new IpAddressSpace().withResourceId(
                        "/subscriptions/2bf4a339-294d-4c25-b0b2-ef649e9f5c27/resourceGroups/os-liftr-integration/providers/Microsoft.Network/virtualNetworks/os-liftr-integration-vnet")
                        .withAddressSpace("10.1.0.0/16"))
                    .withTrustSubnet(new IpAddressSpace().withResourceId(
                        "/subscriptions/2bf4a339-294d-4c25-b0b2-ef649e9f5c27/resourceGroups/os-liftr-integration/providers/Microsoft.Network/virtualNetworks/os-liftr-integration-vnet/subnets/os-liftr-integration-trust-subnet")
                        .withAddressSpace("10.1.1.0/24"))
                    .withUnTrustSubnet(new IpAddressSpace().withResourceId(
                        "/subscriptions/2bf4a339-294d-4c25-b0b2-ef649e9f5c27/resourceGroups/os-liftr-integration/providers/Microsoft.Network/virtualNetworks/os-liftr-integration-vnet/subnets/os-liftr-integration-untrust-subnet")
                        .withAddressSpace("10.1.1.0/24"))
                    .withIpOfTrustSubnetForUdr(new IpAddress().withResourceId(
                        "/subscriptions/2bf4a339-294d-4c25-b0b2-ef649e9f5c27/resourceGroups/os-liftr-integration/providers/Microsoft.Network/virtualNetworks/os-liftr-integration-vnet/subnets/os-liftr-integration-untrust-subnet")
                        .withAddress("10.1.1.0/24")))
                    .withVwanConfiguration(new VwanConfiguration()
                        .withNetworkVirtualApplianceId("2bf4a339-294d-4c25-b0b2-ef649e9f5c12")
                        .withVHub(new IpAddressSpace().withResourceId(
                            "/subscriptions/2bf4a339-294d-4c25-b0b2-ef649e9f5c27/resourceGroups/os-liftr-integration/providers/Microsoft.Network/virtualNetworks/os-liftr-integration-vnet/subnets/os-liftr-integration-untrust-subnet")
                            .withAddressSpace("10.1.1.0/24"))
                        .withTrustSubnet(new IpAddressSpace().withResourceId(
                            "/subscriptions/2bf4a339-294d-4c25-b0b2-ef649e9f5c27/resourceGroups/os-liftr-integration/providers/Microsoft.Network/virtualNetworks/os-liftr-integration-vnet/subnets/os-liftr-integration-trust-subnet")
                            .withAddressSpace("10.1.1.0/24"))
                        .withUnTrustSubnet(new IpAddressSpace().withResourceId(
                            "/subscriptions/2bf4a339-294d-4c25-b0b2-ef649e9f5c27/resourceGroups/os-liftr-integration/providers/Microsoft.Network/virtualNetworks/os-liftr-integration-vnet/subnets/os-liftr-integration-untrust-subnet")
                            .withAddressSpace("10.1.1.0/24"))
                        .withIpOfTrustSubnetForUdr(new IpAddress().withResourceId(
                            "/subscriptions/2bf4a339-294d-4c25-b0b2-ef649e9f5c27/resourceGroups/os-liftr-integration/providers/Microsoft.Network/virtualNetworks/os-liftr-integration-vnet/subnets/os-liftr-integration-untrust-subnet")
                            .withAddress("10.1.1.0/24")))
                    .withNetworkType(NetworkType.VNET)
                    .withPublicIps(Arrays.asList(new IpAddress().withResourceId(
                        "/subscriptions/01c7d41f-afaf-464e-8a8b-5c6f9f98cee8/resourceGroups/mj-liftr-integration/providers/Microsoft.Network/publicIPAddresses/mj-liftr-integration-PublicIp1")
                        .withAddress("20.22.92.11")))
                    .withEnableEgressNat(EgressNat.ENABLED)
                    .withEgressNatIp(Arrays.asList(new IpAddress().withResourceId(
                        "/subscriptions/01c7d41f-afaf-464e-8a8b-5c6f9f98cee8/resourceGroups/mj-liftr-integration/providers/Microsoft.Network/publicIPAddresses/mj-liftr-integration-egressNatIp1")
                        .withAddress("20.22.92.111")))
                    .withTrustedRanges(Arrays.asList("20.22.92.11"))
                    .withPrivateSourceNatRulesDestination(Arrays.asList("20.22.92.11")))
                .withIsPanoramaManaged(BooleanEnum.TRUE)
                .withPanoramaConfig(new PanoramaConfig().withConfigString("bas64EncodedString"))
                .withAssociatedRulestack(new RulestackDetails().withResourceId("aaaaaaaaaa")
                    .withRulestackId("aaaaaaaaaaaaaaaa")
                    .withLocation("eastus"))
                .withDnsSettings(new DnsSettings().withEnableDnsProxy(DnsProxy.DISABLED)
                    .withEnabledDnsType(EnabledDnsType.CUSTOM)
                    .withDnsServers(Arrays.asList(new IpAddress().withResourceId(
                        "/subscriptions/01c7d41f-afaf-464e-8a8b-5c6f9f98cee8/resourceGroups/mj-liftr-integration/providers/Microsoft.Network/publicIPAddresses/mj-liftr-integration-egressNatIp1")
                        .withAddress("20.22.92.111"))))
                .withFrontEndSettings(Arrays.asList(new FrontendSetting().withName("frontendsetting11")
                    .withProtocol(ProtocolType.TCP)
                    .withFrontendConfiguration(new EndpointConfiguration().withPort("80")
                        .withAddress(new IpAddress().withResourceId(
                            "/subscriptions/01c7d41f-afaf-464e-8a8b-5c6f9f98cee8/resourceGroups/mj-liftr-integration/providers/Microsoft.Network/publicIPAddresses/mj-liftr-integration-frontendSettingIp1")
                            .withAddress("20.22.91.251")))
                    .withBackendConfiguration(new EndpointConfiguration().withPort("80")
                        .withAddress(new IpAddress().withResourceId(
                            "/subscriptions/01c7d41f-afaf-464e-8a8b-5c6f9f98cee8/resourceGroups/mj-liftr-integration/providers/Microsoft.Network/publicIPAddresses/mj-liftr-integration-frontendSettingIp2")
                            .withAddress("20.22.32.136")))))
                .withPlanData(new PlanData().withUsageType(UsageType.PAYG)
                    .withBillingCycle(BillingCycle.WEEKLY)
                    .withPlanId("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"))
                .withMarketplaceDetails(new MarketplaceDetails().withOfferId("aaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
                    .withPublisherId("aaaa")
                    .withMarketplaceSubscriptionStatus(MarketplaceSubscriptionStatus.PENDING_FULFILLMENT_START)))
            .apply();
    }

    // Use "Map.of" if available
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

### FqdnListGlobalRulestack_CreateOrUpdate

```java
import com.azure.resourcemanager.paloaltonetworks.ngfw.fluent.models.FqdnListGlobalRulestackResourceInner;
import java.util.Arrays;

/**
 * Samples for FqdnListGlobalRulestack CreateOrUpdate.
 */
public final class FqdnListGlobalRulestackCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-10-08/FqdnListGlobalRulestack_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: FqdnListGlobalRulestack_CreateOrUpdate_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void fqdnListGlobalRulestackCreateOrUpdateMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.fqdnListGlobalRulestacks()
            .createOrUpdate("praval", "armid1",
                new FqdnListGlobalRulestackResourceInner().withDescription("string")
                    .withFqdnList(Arrays.asList("string1", "string2"))
                    .withEtag("aaaaaaaaaaaaaaaaaa")
                    .withAuditComment("string"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/FqdnListGlobalRulestack_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: FqdnListGlobalRulestack_CreateOrUpdate_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void fqdnListGlobalRulestackCreateOrUpdateMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.fqdnListGlobalRulestacks()
            .createOrUpdate("praval", "armid1",
                new FqdnListGlobalRulestackResourceInner().withFqdnList(Arrays.asList("string1", "string2")),
                com.azure.core.util.Context.NONE);
    }
}
```

### FqdnListGlobalRulestack_Delete

```java
/**
 * Samples for FqdnListGlobalRulestack Delete.
 */
public final class FqdnListGlobalRulestackDeleteSamples {
    /*
     * x-ms-original-file: 2025-10-08/FqdnListGlobalRulestack_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: FqdnListGlobalRulestack_Delete_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void fqdnListGlobalRulestackDeleteMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.fqdnListGlobalRulestacks().delete("praval", "armid1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/FqdnListGlobalRulestack_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: FqdnListGlobalRulestack_Delete_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void fqdnListGlobalRulestackDeleteMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.fqdnListGlobalRulestacks().delete("praval", "armid1", com.azure.core.util.Context.NONE);
    }
}
```

### FqdnListGlobalRulestack_Get

```java
/**
 * Samples for FqdnListGlobalRulestack Get.
 */
public final class FqdnListGlobalRulestackGetSamples {
    /*
     * x-ms-original-file: 2025-10-08/FqdnListGlobalRulestack_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: FqdnListGlobalRulestack_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void fqdnListGlobalRulestackGetMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.fqdnListGlobalRulestacks().getWithResponse("praval", "armid1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/FqdnListGlobalRulestack_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: FqdnListGlobalRulestack_Get_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void fqdnListGlobalRulestackGetMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.fqdnListGlobalRulestacks().getWithResponse("praval", "armid1", com.azure.core.util.Context.NONE);
    }
}
```

### FqdnListGlobalRulestack_List

```java
/**
 * Samples for FqdnListGlobalRulestack List.
 */
public final class FqdnListGlobalRulestackListSamples {
    /*
     * x-ms-original-file: 2025-10-08/FqdnListGlobalRulestack_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: FqdnListGlobalRulestack_List_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void fqdnListGlobalRulestackListMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.fqdnListGlobalRulestacks().list("praval", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/FqdnListGlobalRulestack_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: FqdnListGlobalRulestack_List_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void fqdnListGlobalRulestackListMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.fqdnListGlobalRulestacks().list("praval", com.azure.core.util.Context.NONE);
    }
}
```

### FqdnListLocalRulestack_CreateOrUpdate

```java
import java.util.Arrays;

/**
 * Samples for FqdnListLocalRulestack CreateOrUpdate.
 */
public final class FqdnListLocalRulestackCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-10-08/FqdnListLocalRulestack_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: FqdnListLocalRulestack_CreateOrUpdate_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void fqdnListLocalRulestackCreateOrUpdateMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.fqdnListLocalRulestacks()
            .define("armid1")
            .withExistingLocalRulestack("rgopenapi", "lrs1")
            .withFqdnList(Arrays.asList("string1", "string2"))
            .withDescription("string")
            .withEtag("aaaaaaaaaaaaaaaaaa")
            .withAuditComment("string")
            .create();
    }

    /*
     * x-ms-original-file: 2025-10-08/FqdnListLocalRulestack_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: FqdnListLocalRulestack_CreateOrUpdate_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void fqdnListLocalRulestackCreateOrUpdateMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.fqdnListLocalRulestacks()
            .define("armid1")
            .withExistingLocalRulestack("rgopenapi", "lrs1")
            .withFqdnList(Arrays.asList("string1", "string2"))
            .create();
    }
}
```

### FqdnListLocalRulestack_Delete

```java
/**
 * Samples for FqdnListLocalRulestack Delete.
 */
public final class FqdnListLocalRulestackDeleteSamples {
    /*
     * x-ms-original-file: 2025-10-08/FqdnListLocalRulestack_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: FqdnListLocalRulestack_Delete_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void fqdnListLocalRulestackDeleteMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.fqdnListLocalRulestacks().delete("rgopenapi", "lrs1", "armid1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/FqdnListLocalRulestack_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: FqdnListLocalRulestack_Delete_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void fqdnListLocalRulestackDeleteMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.fqdnListLocalRulestacks().delete("rgopenapi", "lrs1", "armid1", com.azure.core.util.Context.NONE);
    }
}
```

### FqdnListLocalRulestack_Get

```java
/**
 * Samples for FqdnListLocalRulestack Get.
 */
public final class FqdnListLocalRulestackGetSamples {
    /*
     * x-ms-original-file: 2025-10-08/FqdnListLocalRulestack_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: FqdnListLocalRulestack_Get_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void fqdnListLocalRulestackGetMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.fqdnListLocalRulestacks()
            .getWithResponse("rgopenapi", "lrs1", "armid1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/FqdnListLocalRulestack_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: FqdnListLocalRulestack_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void fqdnListLocalRulestackGetMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.fqdnListLocalRulestacks()
            .getWithResponse("rgopenapi", "lrs1", "armid1", com.azure.core.util.Context.NONE);
    }
}
```

### FqdnListLocalRulestack_ListByLocalRulestacks

```java
/**
 * Samples for FqdnListLocalRulestack ListByLocalRulestacks.
 */
public final class FqdnListLocalRulestackListByLocalRulestacksSamples {
    /*
     * x-ms-original-file: 2025-10-08/FqdnListLocalRulestack_ListByLocalRulestacks_MinimumSet_Gen.json
     */
    /**
     * Sample code: FqdnListLocalRulestack_ListByLocalRulestacks_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void fqdnListLocalRulestackListByLocalRulestacksMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.fqdnListLocalRulestacks().listByLocalRulestacks("rgopenapi", "lrs1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/FqdnListLocalRulestack_ListByLocalRulestacks_MaximumSet_Gen.json
     */
    /**
     * Sample code: FqdnListLocalRulestack_ListByLocalRulestacks_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void fqdnListLocalRulestackListByLocalRulestacksMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.fqdnListLocalRulestacks().listByLocalRulestacks("rgopenapi", "lrs1", com.azure.core.util.Context.NONE);
    }
}
```

### GlobalRulestack_Commit

```java
/**
 * Samples for GlobalRulestack Commit.
 */
public final class GlobalRulestackCommitSamples {
    /*
     * x-ms-original-file: 2025-10-08/GlobalRulestack_commit_MaximumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_commit_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void globalRulestackCommitMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.globalRulestacks().commit("praval", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/GlobalRulestack_commit_MinimumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_commit_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void globalRulestackCommitMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.globalRulestacks().commit("praval", com.azure.core.util.Context.NONE);
    }
}
```

### GlobalRulestack_CreateOrUpdate

```java
import com.azure.resourcemanager.paloaltonetworks.ngfw.fluent.models.GlobalRulestackResourceInner;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.AzureResourceManagerManagedIdentityProperties;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.AzureResourceManagerUserAssignedIdentity;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.DefaultMode;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.ManagedIdentityType;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.ScopeType;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.SecurityServices;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for GlobalRulestack CreateOrUpdate.
 */
public final class GlobalRulestackCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-10-08/GlobalRulestack_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_CreateOrUpdate_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void globalRulestackCreateOrUpdateMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.globalRulestacks()
            .createOrUpdate("praval", new GlobalRulestackResourceInner().withLocation("eastus"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/GlobalRulestack_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_CreateOrUpdate_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void globalRulestackCreateOrUpdateMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.globalRulestacks()
            .createOrUpdate("praval",
                new GlobalRulestackResourceInner().withLocation("eastus")
                    .withIdentity(new AzureResourceManagerManagedIdentityProperties().withType(ManagedIdentityType.NONE)
                        .withUserAssignedIdentities(mapOf("key16",
                            new AzureResourceManagerUserAssignedIdentity().withClientId("aaaa")
                                .withPrincipalId("aaaaaaaaaaaaaaa"))))
                    .withPanEtag("2bf4a339-294d-4c25-b0b2-ef649e9f5c12")
                    .withPanLocation("eastus")
                    .withScope(ScopeType.GLOBAL)
                    .withAssociatedSubscriptions(Arrays.asList("2bf4a339-294d-4c25-b0b2-ef649e9f5c27"))
                    .withDescription("global rulestacks")
                    .withDefaultMode(DefaultMode.IPS)
                    .withMinAppIdVersion("8.5.3")
                    .withSecurityServices(new SecurityServices().withVulnerabilityProfile("default")
                        .withAntiSpywareProfile("default")
                        .withAntiVirusProfile("default")
                        .withUrlFilteringProfile("default")
                        .withFileBlockingProfile("default")
                        .withDnsSubscription("default")
                        .withOutboundUnTrustCertificate("default")
                        .withOutboundTrustCertificate("default")),
                com.azure.core.util.Context.NONE);
    }

    // Use "Map.of" if available
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

### GlobalRulestack_Delete

```java
/**
 * Samples for GlobalRulestack Delete.
 */
public final class GlobalRulestackDeleteSamples {
    /*
     * x-ms-original-file: 2025-10-08/GlobalRulestack_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_Delete_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void globalRulestackDeleteMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.globalRulestacks().delete("praval", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/GlobalRulestack_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_Delete_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void globalRulestackDeleteMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.globalRulestacks().delete("praval", com.azure.core.util.Context.NONE);
    }
}
```

### GlobalRulestack_Get

```java
/**
 * Samples for GlobalRulestack Get.
 */
public final class GlobalRulestackGetSamples {
    /*
     * x-ms-original-file: 2025-10-08/GlobalRulestack_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_Get_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void globalRulestackGetMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.globalRulestacks().getWithResponse("praval", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/GlobalRulestack_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void globalRulestackGetMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.globalRulestacks().getWithResponse("praval", com.azure.core.util.Context.NONE);
    }
}
```

### GlobalRulestack_GetChangeLog

```java
/**
 * Samples for GlobalRulestack GetChangeLog.
 */
public final class GlobalRulestackGetChangeLogSamples {
    /*
     * x-ms-original-file: 2025-10-08/GlobalRulestack_getChangeLog_MinimumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_getChangeLog_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void globalRulestackGetChangeLogMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.globalRulestacks().getChangeLogWithResponse("praval", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/GlobalRulestack_getChangeLog_MaximumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_getChangeLog_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void globalRulestackGetChangeLogMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.globalRulestacks().getChangeLogWithResponse("praval", com.azure.core.util.Context.NONE);
    }
}
```

### GlobalRulestack_List

```java
/**
 * Samples for GlobalRulestack List.
 */
public final class GlobalRulestackListSamples {
    /*
     * x-ms-original-file: 2025-10-08/GlobalRulestack_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_List_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void globalRulestackListMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.globalRulestacks().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/GlobalRulestack_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_List_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void globalRulestackListMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.globalRulestacks().list(com.azure.core.util.Context.NONE);
    }
}
```

### GlobalRulestack_ListAdvancedSecurityObjects

```java
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.AdvSecurityObjectTypeEnum;

/**
 * Samples for GlobalRulestack ListAdvancedSecurityObjects.
 */
public final class GlobalRulestackListAdvancedSecurityObjectsSamples {
    /*
     * x-ms-original-file: 2025-10-08/GlobalRulestack_listAdvancedSecurityObjects_MaximumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_listAdvancedSecurityObjects_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void globalRulestackListAdvancedSecurityObjectsMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.globalRulestacks()
            .listAdvancedSecurityObjectsWithResponse("praval", AdvSecurityObjectTypeEnum.fromString("globalRulestacks"),
                "a6a321", 20, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/GlobalRulestack_listAdvancedSecurityObjects_MinimumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_listAdvancedSecurityObjects_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void globalRulestackListAdvancedSecurityObjectsMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.globalRulestacks()
            .listAdvancedSecurityObjectsWithResponse("praval", AdvSecurityObjectTypeEnum.fromString("globalRulestacks"),
                null, null, com.azure.core.util.Context.NONE);
    }
}
```

### GlobalRulestack_ListAppIds

```java
/**
 * Samples for GlobalRulestack ListAppIds.
 */
public final class GlobalRulestackListAppIdsSamples {
    /*
     * x-ms-original-file: 2025-10-08/GlobalRulestack_listAppIds_MaximumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_listAppIds_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void globalRulestackListAppIdsMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.globalRulestacks()
            .listAppIdsWithResponse("praval", "8543", "pref", "a6a321", 20, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/GlobalRulestack_listAppIds_MinimumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_listAppIds_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void globalRulestackListAppIdsMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.globalRulestacks()
            .listAppIdsWithResponse("praval", null, null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### GlobalRulestack_ListCountries

```java
/**
 * Samples for GlobalRulestack ListCountries.
 */
public final class GlobalRulestackListCountriesSamples {
    /*
     * x-ms-original-file: 2025-10-08/GlobalRulestack_listCountries_MinimumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_listCountries_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void globalRulestackListCountriesMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.globalRulestacks().listCountriesWithResponse("praval", null, null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/GlobalRulestack_listCountries_MaximumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_listCountries_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void globalRulestackListCountriesMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.globalRulestacks().listCountriesWithResponse("praval", "a6a321", 20, com.azure.core.util.Context.NONE);
    }
}
```

### GlobalRulestack_ListFirewalls

```java
/**
 * Samples for GlobalRulestack ListFirewalls.
 */
public final class GlobalRulestackListFirewallsSamples {
    /*
     * x-ms-original-file: 2025-10-08/GlobalRulestack_listFirewalls_MaximumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_listFirewalls_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void globalRulestackListFirewallsMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.globalRulestacks().listFirewallsWithResponse("praval", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/GlobalRulestack_listFirewalls_MinimumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_listFirewalls_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void globalRulestackListFirewallsMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.globalRulestacks().listFirewallsWithResponse("praval", com.azure.core.util.Context.NONE);
    }
}
```

### GlobalRulestack_ListPredefinedUrlCategories

```java
/**
 * Samples for GlobalRulestack ListPredefinedUrlCategories.
 */
public final class GlobalRulestackListPredefinedUrlCategoriesSamples {
    /*
     * x-ms-original-file: 2025-10-08/GlobalRulestack_listPredefinedUrlCategories_MaximumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_listPredefinedUrlCategories_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void globalRulestackListPredefinedUrlCategoriesMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.globalRulestacks()
            .listPredefinedUrlCategoriesWithResponse("praval", "a6a321", 20, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/GlobalRulestack_listPredefinedUrlCategories_MinimumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_listPredefinedUrlCategories_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void globalRulestackListPredefinedUrlCategoriesMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.globalRulestacks()
            .listPredefinedUrlCategoriesWithResponse("praval", null, null, com.azure.core.util.Context.NONE);
    }
}
```

### GlobalRulestack_ListSecurityServices

```java
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.SecurityServicesTypeEnum;

/**
 * Samples for GlobalRulestack ListSecurityServices.
 */
public final class GlobalRulestackListSecurityServicesSamples {
    /*
     * x-ms-original-file: 2025-10-08/GlobalRulestack_listSecurityServices_MinimumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_listSecurityServices_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void globalRulestackListSecurityServicesMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.globalRulestacks()
            .listSecurityServicesWithResponse("praval", SecurityServicesTypeEnum.fromString("globalRulestacks"), null,
                null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/GlobalRulestack_listSecurityServices_MaximumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_listSecurityServices_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void globalRulestackListSecurityServicesMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.globalRulestacks()
            .listSecurityServicesWithResponse("praval", SecurityServicesTypeEnum.fromString("globalRulestacks"),
                "a6a321", 20, com.azure.core.util.Context.NONE);
    }
}
```

### GlobalRulestack_Revert

```java
/**
 * Samples for GlobalRulestack Revert.
 */
public final class GlobalRulestackRevertSamples {
    /*
     * x-ms-original-file: 2025-10-08/GlobalRulestack_revert_MinimumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_revert_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void globalRulestackRevertMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.globalRulestacks().revertWithResponse("praval", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/GlobalRulestack_revert_MaximumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_revert_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void globalRulestackRevertMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.globalRulestacks().revertWithResponse("praval", com.azure.core.util.Context.NONE);
    }
}
```

### GlobalRulestack_Update

```java
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.AzureResourceManagerManagedIdentityProperties;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.AzureResourceManagerUserAssignedIdentity;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.DefaultMode;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.GlobalRulestackResourceUpdate;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.GlobalRulestackResourceUpdateProperties;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.ManagedIdentityType;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.ScopeType;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.SecurityServices;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for GlobalRulestack Update.
 */
public final class GlobalRulestackUpdateSamples {
    /*
     * x-ms-original-file: 2025-10-08/GlobalRulestack_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_Update_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void globalRulestackUpdateMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.globalRulestacks()
            .updateWithResponse("praval", new GlobalRulestackResourceUpdate().withLocation("eastus")
                .withIdentity(new AzureResourceManagerManagedIdentityProperties().withType(ManagedIdentityType.NONE)
                    .withUserAssignedIdentities(mapOf("key16",
                        new AzureResourceManagerUserAssignedIdentity().withClientId("aaaa")
                            .withPrincipalId("aaaaaaaaaaaaaaa"))))
                .withProperties(
                    new GlobalRulestackResourceUpdateProperties().withPanEtag("2bf4a339-294d-4c25-b0b2-ef649e9f5c12")
                        .withPanLocation("eastus")
                        .withScope(ScopeType.GLOBAL)
                        .withAssociatedSubscriptions(Arrays.asList("2bf4a339-294d-4c25-b0b2-ef649e9f5c27"))
                        .withDescription("global rulestacks")
                        .withDefaultMode(DefaultMode.IPS)
                        .withMinAppIdVersion("8.5.3")
                        .withSecurityServices(new SecurityServices().withVulnerabilityProfile("default")
                            .withAntiSpywareProfile("default")
                            .withAntiVirusProfile("default")
                            .withUrlFilteringProfile("default")
                            .withFileBlockingProfile("default")
                            .withDnsSubscription("default")
                            .withOutboundUnTrustCertificate("default")
                            .withOutboundTrustCertificate("default"))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/GlobalRulestack_Update_MinimumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_Update_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void globalRulestackUpdateMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.globalRulestacks()
            .updateWithResponse("praval", new GlobalRulestackResourceUpdate(), com.azure.core.util.Context.NONE);
    }

    // Use "Map.of" if available
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

### LocalRules_CreateOrUpdate

```java
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.ActionEnum;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.BooleanEnum;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.Category;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.DecryptionRuleTypeEnum;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.DestinationAddr;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.SourceAddr;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.StateEnum;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.TagInfo;
import java.util.Arrays;

/**
 * Samples for LocalRules CreateOrUpdate.
 */
public final class LocalRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-10-08/LocalRules_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRules_CreateOrUpdate_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulesCreateOrUpdateMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRules()
            .define("1")
            .withExistingLocalRulestack("firewall-rg", "lrs1")
            .withRuleName("localRule1")
            .create();
    }

    /*
     * x-ms-original-file: 2025-10-08/LocalRules_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRules_CreateOrUpdate_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulesCreateOrUpdateMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRules()
            .define("1")
            .withExistingLocalRulestack("firewall-rg", "lrs1")
            .withRuleName("localRule1")
            .withTags(Arrays.asList(new TagInfo().withKey("fakeTokenPlaceholder").withValue("value")))
            .withEtag("c18e6eef-ba3e-49ee-8a85-2b36c863a9d0")
            .withDescription("description of local rule")
            .withRuleState(StateEnum.DISABLED)
            .withSource(new SourceAddr().withCidrs(Arrays.asList("1.0.0.1/10"))
                .withCountries(Arrays.asList("India"))
                .withFeeds(Arrays.asList("feed"))
                .withPrefixLists(Arrays.asList("PL1")))
            .withNegateSource(BooleanEnum.TRUE)
            .withDestination(new DestinationAddr().withCidrs(Arrays.asList("1.0.0.1/10"))
                .withCountries(Arrays.asList("India"))
                .withFeeds(Arrays.asList("feed"))
                .withPrefixLists(Arrays.asList("PL1"))
                .withFqdnLists(Arrays.asList("FQDN1")))
            .withNegateDestination(BooleanEnum.TRUE)
            .withApplications(Arrays.asList("app1"))
            .withCategory(
                new Category().withUrlCustom(Arrays.asList("https://microsoft.com")).withFeeds(Arrays.asList("feed")))
            .withProtocol("HTTP")
            .withProtocolPortList(Arrays.asList("80"))
            .withInboundInspectionCertificate("cert1")
            .withAuditComment("example comment")
            .withActionType(ActionEnum.ALLOW)
            .withEnableLogging(StateEnum.DISABLED)
            .withDecryptionRuleType(DecryptionRuleTypeEnum.SSLOUTBOUND_INSPECTION)
            .create();
    }
}
```

### LocalRules_Delete

```java
/**
 * Samples for LocalRules Delete.
 */
public final class LocalRulesDeleteSamples {
    /*
     * x-ms-original-file: 2025-10-08/LocalRules_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRules_Delete_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulesDeleteMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRules().delete("firewall-rg", "lrs1", "1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/LocalRules_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRules_Delete_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulesDeleteMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRules().delete("firewall-rg", "lrs1", "1", com.azure.core.util.Context.NONE);
    }
}
```

### LocalRules_Get

```java
/**
 * Samples for LocalRules Get.
 */
public final class LocalRulesGetSamples {
    /*
     * x-ms-original-file: 2025-10-08/LocalRules_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRules_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulesGetMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRules().getWithResponse("firewall-rg", "lrs1", "1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/LocalRules_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRules_Get_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulesGetMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRules().getWithResponse("firewall-rg", "lrs1", "1", com.azure.core.util.Context.NONE);
    }
}
```

### LocalRules_GetCounters

```java
/**
 * Samples for LocalRules GetCounters.
 */
public final class LocalRulesGetCountersSamples {
    /*
     * x-ms-original-file: 2025-10-08/LocalRules_getCounters_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRules_getCounters_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulesGetCountersMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRules()
            .getCountersWithResponse("firewall-rg", "lrs1", "1", "firewall1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/LocalRules_getCounters_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRules_getCounters_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulesGetCountersMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRules()
            .getCountersWithResponse("firewall-rg", "lrs1", "1", null, com.azure.core.util.Context.NONE);
    }
}
```

### LocalRules_ListByLocalRulestacks

```java
/**
 * Samples for LocalRules ListByLocalRulestacks.
 */
public final class LocalRulesListByLocalRulestacksSamples {
    /*
     * x-ms-original-file: 2025-10-08/LocalRules_ListByLocalRulestacks_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRules_ListByLocalRulestacks_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulesListByLocalRulestacksMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRules().listByLocalRulestacks("firewall-rg", "lrs1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/LocalRules_ListByLocalRulestacks_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRules_ListByLocalRulestacks_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulesListByLocalRulestacksMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRules().listByLocalRulestacks("firewall-rg", "lrs1", com.azure.core.util.Context.NONE);
    }
}
```

### LocalRules_RefreshCounters

```java
/**
 * Samples for LocalRules RefreshCounters.
 */
public final class LocalRulesRefreshCountersSamples {
    /*
     * x-ms-original-file: 2025-10-08/LocalRules_refreshCounters_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRules_refreshCounters_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulesRefreshCountersMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRules()
            .refreshCountersWithResponse("firewall-rg", "lrs1", "1", null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/LocalRules_refreshCounters_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRules_refreshCounters_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulesRefreshCountersMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRules()
            .refreshCountersWithResponse("firewall-rg", "lrs1", "1", "firewall1", com.azure.core.util.Context.NONE);
    }
}
```

### LocalRules_ResetCounters

```java
/**
 * Samples for LocalRules ResetCounters.
 */
public final class LocalRulesResetCountersSamples {
    /*
     * x-ms-original-file: 2025-10-08/LocalRules_resetCounters_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRules_resetCounters_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulesResetCountersMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRules()
            .resetCountersWithResponse("firewall-rg", "lrs1", "1", "firewall1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/LocalRules_resetCounters_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRules_resetCounters_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulesResetCountersMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRules()
            .resetCountersWithResponse("firewall-rg", "lrs1", "1", null, com.azure.core.util.Context.NONE);
    }
}
```

### LocalRulestacks_Commit

```java
/**
 * Samples for LocalRulestacks Commit.
 */
public final class LocalRulestacksCommitSamples {
    /*
     * x-ms-original-file: 2025-10-08/LocalRulestacks_commit_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_commit_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulestacksCommitMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRulestacks().commit("rgopenapi", "lrs1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/LocalRulestacks_commit_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_commit_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulestacksCommitMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRulestacks().commit("rgopenapi", "lrs1", com.azure.core.util.Context.NONE);
    }
}
```

### LocalRulestacks_CreateOrUpdate

```java
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.AzureResourceManagerManagedIdentityProperties;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.AzureResourceManagerUserAssignedIdentity;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.DefaultMode;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.ManagedIdentityType;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.ScopeType;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.SecurityServices;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for LocalRulestacks CreateOrUpdate.
 */
public final class LocalRulestacksCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-10-08/LocalRulestacks_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_CreateOrUpdate_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulestacksCreateOrUpdateMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRulestacks().define("lrs1").withRegion("eastus").withExistingResourceGroup("rgopenapi").create();
    }

    /*
     * x-ms-original-file: 2025-10-08/LocalRulestacks_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_CreateOrUpdate_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulestacksCreateOrUpdateMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRulestacks()
            .define("lrs1")
            .withRegion("eastus")
            .withExistingResourceGroup("rgopenapi")
            .withTags(mapOf("tagName", "value"))
            .withIdentity(new AzureResourceManagerManagedIdentityProperties().withType(ManagedIdentityType.NONE)
                .withUserAssignedIdentities(mapOf("key16",
                    new AzureResourceManagerUserAssignedIdentity().withClientId("aaaa")
                        .withPrincipalId("aaaaaaaaaaaaaaa"))))
            .withPanEtag("2bf4a339-294d-4c25-b0b2-ef649e9f5c12")
            .withPanLocation("eastus")
            .withScope(ScopeType.LOCAL)
            .withAssociatedSubscriptions(Arrays.asList("2bf4a339-294d-4c25-b0b2-ef649e9f5c27"))
            .withDescription("local rulestacks")
            .withDefaultMode(DefaultMode.IPS)
            .withMinAppIdVersion("8.5.3")
            .withSecurityServices(new SecurityServices().withVulnerabilityProfile("default")
                .withAntiSpywareProfile("default")
                .withAntiVirusProfile("default")
                .withUrlFilteringProfile("default")
                .withFileBlockingProfile("default")
                .withDnsSubscription("default")
                .withOutboundUnTrustCertificate("default")
                .withOutboundTrustCertificate("default"))
            .create();
    }

    // Use "Map.of" if available
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

### LocalRulestacks_Delete

```java
/**
 * Samples for LocalRulestacks Delete.
 */
public final class LocalRulestacksDeleteSamples {
    /*
     * x-ms-original-file: 2025-10-08/LocalRulestacks_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_Delete_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulestacksDeleteMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRulestacks().delete("rgopenapi", "lrs1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/LocalRulestacks_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_Delete_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulestacksDeleteMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRulestacks().delete("rgopenapi", "lrs1", com.azure.core.util.Context.NONE);
    }
}
```

### LocalRulestacks_GetByResourceGroup

```java
/**
 * Samples for LocalRulestacks GetByResourceGroup.
 */
public final class LocalRulestacksGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-10-08/LocalRulestacks_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulestacksGetMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRulestacks().getByResourceGroupWithResponse("rgopenapi", "lrs1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/LocalRulestacks_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_Get_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulestacksGetMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRulestacks().getByResourceGroupWithResponse("rgopenapi", "lrs1", com.azure.core.util.Context.NONE);
    }
}
```

### LocalRulestacks_GetChangeLog

```java
/**
 * Samples for LocalRulestacks GetChangeLog.
 */
public final class LocalRulestacksGetChangeLogSamples {
    /*
     * x-ms-original-file: 2025-10-08/LocalRulestacks_getChangeLog_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_getChangeLog_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulestacksGetChangeLogMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRulestacks().getChangeLogWithResponse("rgopenapi", "lrs1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/LocalRulestacks_getChangeLog_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_getChangeLog_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulestacksGetChangeLogMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRulestacks().getChangeLogWithResponse("rgopenapi", "lrs1", com.azure.core.util.Context.NONE);
    }
}
```

### LocalRulestacks_GetSupportInfo

```java
/**
 * Samples for LocalRulestacks GetSupportInfo.
 */
public final class LocalRulestacksGetSupportInfoSamples {
    /*
     * x-ms-original-file: 2025-10-08/LocalRulestacks_getSupportInfo_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_getSupportInfo_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulestacksGetSupportInfoMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRulestacks()
            .getSupportInfoWithResponse("rgopenapi", "lrs1", null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/LocalRulestacks_getSupportInfo_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_getSupportInfo_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulestacksGetSupportInfoMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRulestacks()
            .getSupportInfoWithResponse("rgopenapi", "lrs1", "user1@domain.com", com.azure.core.util.Context.NONE);
    }
}
```

### LocalRulestacks_List

```java
/**
 * Samples for LocalRulestacks List.
 */
public final class LocalRulestacksListSamples {
    /*
     * x-ms-original-file: 2025-10-08/LocalRulestacks_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_ListBySubscription_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulestacksListBySubscriptionMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRulestacks().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/LocalRulestacks_ListBySubscription_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_ListBySubscription_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulestacksListBySubscriptionMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRulestacks().list(com.azure.core.util.Context.NONE);
    }
}
```

### LocalRulestacks_ListAdvancedSecurityObjects

```java
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.AdvSecurityObjectTypeEnum;

/**
 * Samples for LocalRulestacks ListAdvancedSecurityObjects.
 */
public final class LocalRulestacksListAdvancedSecurityObjectsSamples {
    /*
     * x-ms-original-file: 2025-10-08/LocalRulestacks_listAdvancedSecurityObjects_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_listAdvancedSecurityObjects_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulestacksListAdvancedSecurityObjectsMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRulestacks()
            .listAdvancedSecurityObjectsWithResponse("rgopenapi", "lrs1",
                AdvSecurityObjectTypeEnum.fromString("localRulestacks"), "a6a321", 20,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/LocalRulestacks_listAdvancedSecurityObjects_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_listAdvancedSecurityObjects_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulestacksListAdvancedSecurityObjectsMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRulestacks()
            .listAdvancedSecurityObjectsWithResponse("rgopenapi", "lrs1",
                AdvSecurityObjectTypeEnum.fromString("localRulestacks"), null, null, com.azure.core.util.Context.NONE);
    }
}
```

### LocalRulestacks_ListAppIds

```java
/**
 * Samples for LocalRulestacks ListAppIds.
 */
public final class LocalRulestacksListAppIdsSamples {
    /*
     * x-ms-original-file: 2025-10-08/LocalRulestacks_listAppIds_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_listAppIds_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulestacksListAppIdsMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRulestacks()
            .listAppIds("rgopenapi", "lrs1", "8543", "pref", "a6a321", 20, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/LocalRulestacks_listAppIds_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_listAppIds_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulestacksListAppIdsMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRulestacks()
            .listAppIds("rgopenapi", "lrs1", null, null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### LocalRulestacks_ListByResourceGroup

```java
/**
 * Samples for LocalRulestacks ListByResourceGroup.
 */
public final class LocalRulestacksListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-10-08/LocalRulestacks_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_ListByResourceGroup_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulestacksListByResourceGroupMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRulestacks().listByResourceGroup("rgopenapi", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/LocalRulestacks_ListByResourceGroup_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_ListByResourceGroup_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulestacksListByResourceGroupMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRulestacks().listByResourceGroup("rgopenapi", com.azure.core.util.Context.NONE);
    }
}
```

### LocalRulestacks_ListCountries

```java
/**
 * Samples for LocalRulestacks ListCountries.
 */
public final class LocalRulestacksListCountriesSamples {
    /*
     * x-ms-original-file: 2025-10-08/LocalRulestacks_listCountries_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_listCountries_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulestacksListCountriesMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRulestacks().listCountries("rgopenapi", "lrs1", "a6a321", 20, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/LocalRulestacks_listCountries_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_listCountries_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulestacksListCountriesMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRulestacks().listCountries("rgopenapi", "lrs1", null, null, com.azure.core.util.Context.NONE);
    }
}
```

### LocalRulestacks_ListFirewalls

```java
/**
 * Samples for LocalRulestacks ListFirewalls.
 */
public final class LocalRulestacksListFirewallsSamples {
    /*
     * x-ms-original-file: 2025-10-08/LocalRulestacks_listFirewalls_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_listFirewalls_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulestacksListFirewallsMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRulestacks().listFirewallsWithResponse("rgopenapi", "lrs1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/LocalRulestacks_listFirewalls_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_listFirewalls_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulestacksListFirewallsMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRulestacks().listFirewallsWithResponse("rgopenapi", "lrs1", com.azure.core.util.Context.NONE);
    }
}
```

### LocalRulestacks_ListPredefinedUrlCategories

```java
/**
 * Samples for LocalRulestacks ListPredefinedUrlCategories.
 */
public final class LocalRulestacksListPredefinedUrlCategoriesSamples {
    /*
     * x-ms-original-file: 2025-10-08/LocalRulestacks_listPredefinedUrlCategories_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_listPredefinedUrlCategories_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulestacksListPredefinedUrlCategoriesMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRulestacks()
            .listPredefinedUrlCategories("rgopenapi", "lrs1", null, null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/LocalRulestacks_listPredefinedUrlCategories_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_listPredefinedUrlCategories_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulestacksListPredefinedUrlCategoriesMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRulestacks()
            .listPredefinedUrlCategories("rgopenapi", "lrs1", "a6a321", 20, com.azure.core.util.Context.NONE);
    }
}
```

### LocalRulestacks_ListSecurityServices

```java
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.SecurityServicesTypeEnum;

/**
 * Samples for LocalRulestacks ListSecurityServices.
 */
public final class LocalRulestacksListSecurityServicesSamples {
    /*
     * x-ms-original-file: 2025-10-08/LocalRulestacks_listSecurityServices_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_listSecurityServices_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulestacksListSecurityServicesMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRulestacks()
            .listSecurityServicesWithResponse("rgopenapi", "lrs1",
                SecurityServicesTypeEnum.fromString("localRulestacks"), null, null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/LocalRulestacks_listSecurityServices_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_listSecurityServices_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulestacksListSecurityServicesMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRulestacks()
            .listSecurityServicesWithResponse("rgopenapi", "lrs1",
                SecurityServicesTypeEnum.fromString("localRulestacks"), "a6a321", 20, com.azure.core.util.Context.NONE);
    }
}
```

### LocalRulestacks_Revert

```java
/**
 * Samples for LocalRulestacks Revert.
 */
public final class LocalRulestacksRevertSamples {
    /*
     * x-ms-original-file: 2025-10-08/LocalRulestacks_revert_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_revert_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulestacksRevertMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRulestacks().revertWithResponse("rgopenapi", "lrs1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/LocalRulestacks_revert_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_revert_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulestacksRevertMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.localRulestacks().revertWithResponse("rgopenapi", "lrs1", com.azure.core.util.Context.NONE);
    }
}
```

### LocalRulestacks_Update

```java
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.AzureResourceManagerManagedIdentityProperties;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.AzureResourceManagerUserAssignedIdentity;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.DefaultMode;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.LocalRulestackResource;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.LocalRulestackResourceUpdateProperties;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.ManagedIdentityType;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.ScopeType;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.SecurityServices;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for LocalRulestacks Update.
 */
public final class LocalRulestacksUpdateSamples {
    /*
     * x-ms-original-file: 2025-10-08/LocalRulestacks_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_Update_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulestacksUpdateMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        LocalRulestackResource resource = manager.localRulestacks()
            .getByResourceGroupWithResponse("rgopenapi", "lrs1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("tagName", "value"))
            .withIdentity(new AzureResourceManagerManagedIdentityProperties().withType(ManagedIdentityType.NONE)
                .withUserAssignedIdentities(mapOf("key16",
                    new AzureResourceManagerUserAssignedIdentity().withClientId("aaaa")
                        .withPrincipalId("aaaaaaaaaaaaaaa"))))
            .withProperties(
                new LocalRulestackResourceUpdateProperties().withPanEtag("2bf4a339-294d-4c25-b0b2-ef649e9f5c12")
                    .withPanLocation("eastus")
                    .withScope(ScopeType.LOCAL)
                    .withAssociatedSubscriptions(Arrays.asList("2bf4a339-294d-4c25-b0b2-ef649e9f5c27"))
                    .withDescription("local rulestacks")
                    .withDefaultMode(DefaultMode.IPS)
                    .withMinAppIdVersion("8.5.3")
                    .withSecurityServices(new SecurityServices().withVulnerabilityProfile("default")
                        .withAntiSpywareProfile("default")
                        .withAntiVirusProfile("default")
                        .withUrlFilteringProfile("default")
                        .withFileBlockingProfile("default")
                        .withDnsSubscription("default")
                        .withOutboundUnTrustCertificate("default")
                        .withOutboundTrustCertificate("default")))
            .apply();
    }

    /*
     * x-ms-original-file: 2025-10-08/LocalRulestacks_Update_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_Update_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void localRulestacksUpdateMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        LocalRulestackResource resource = manager.localRulestacks()
            .getByResourceGroupWithResponse("rgopenapi", "lrs1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().apply();
    }

    // Use "Map.of" if available
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

### MetricsObjectFirewall_CreateOrUpdate

```java
import com.azure.resourcemanager.paloaltonetworks.ngfw.fluent.models.MetricsObjectFirewallResourceInner;

/**
 * Samples for MetricsObjectFirewall CreateOrUpdate.
 */
public final class MetricsObjectFirewallCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-10-08/MetricsObjectFirewall_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: MetricsObjectFirewall_CreateOrUpdate_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void metricsObjectFirewallCreateOrUpdateMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.metricsObjectFirewalls()
            .createOrUpdate("rgopenapi", "aaaaaaaaaaaaaaaaaaaaaaaa",
                new MetricsObjectFirewallResourceInner().withApplicationInsightsResourceId("aaaaaaaaaaaaaaa")
                    .withApplicationInsightsConnectionString("aaa"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/MetricsObjectFirewall_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: MetricsObjectFirewall_CreateOrUpdate_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void metricsObjectFirewallCreateOrUpdateMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.metricsObjectFirewalls()
            .createOrUpdate("rgopenapi", "aaaaaaaaaaaaaaaaaaaaaaaa",
                new MetricsObjectFirewallResourceInner().withApplicationInsightsResourceId("aaaaaaaaaaaaaaa")
                    .withApplicationInsightsConnectionString("aaa")
                    .withPanEtag("aaaaaaaaaa"),
                com.azure.core.util.Context.NONE);
    }
}
```

### MetricsObjectFirewall_Delete

```java
/**
 * Samples for MetricsObjectFirewall Delete.
 */
public final class MetricsObjectFirewallDeleteSamples {
    /*
     * x-ms-original-file: 2025-10-08/MetricsObjectFirewall_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: MetricsObjectFirewall_Delete_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void metricsObjectFirewallDeleteMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.metricsObjectFirewalls()
            .delete("rgopenapi", "aaaaaaaaaaaaaaaaaaaaaaaa", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/MetricsObjectFirewall_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: MetricsObjectFirewall_Delete_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void metricsObjectFirewallDeleteMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.metricsObjectFirewalls()
            .delete("rgopenapi", "aaaaaaaaaaaaaaaaaaaaaaaa", com.azure.core.util.Context.NONE);
    }
}
```

### MetricsObjectFirewall_Get

```java
/**
 * Samples for MetricsObjectFirewall Get.
 */
public final class MetricsObjectFirewallGetSamples {
    /*
     * x-ms-original-file: 2025-10-08/MetricsObjectFirewall_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: MetricsObjectFirewall_Get_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void metricsObjectFirewallGetMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.metricsObjectFirewalls()
            .getWithResponse("rgopenapi", "aaaaaaaaaaaaaaaaaaaaaaaa", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/MetricsObjectFirewall_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: MetricsObjectFirewall_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void metricsObjectFirewallGetMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.metricsObjectFirewalls()
            .getWithResponse("rgopenapi", "aaaaaaaaaaaaaaaaaaaaaaaa", com.azure.core.util.Context.NONE);
    }
}
```

### MetricsObjectFirewall_ListByFirewalls

```java
/**
 * Samples for MetricsObjectFirewall ListByFirewalls.
 */
public final class MetricsObjectFirewallListByFirewallsSamples {
    /*
     * x-ms-original-file: 2025-10-08/MetricsObjectFirewall_ListByFirewalls_MinimumSet_Gen.json
     */
    /**
     * Sample code: MetricsObjectFirewall_ListByFirewalls_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void metricsObjectFirewallListByFirewallsMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.metricsObjectFirewalls().listByFirewalls("rgopenapi", "IFTDk", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/MetricsObjectFirewall_ListByFirewalls_MaximumSet_Gen.json
     */
    /**
     * Sample code: MetricsObjectFirewall_ListByFirewalls_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void metricsObjectFirewallListByFirewallsMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.metricsObjectFirewalls().listByFirewalls("rgopenapi", "IFTDk", com.azure.core.util.Context.NONE);
    }
}
```

### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2025-10-08/Operations_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void operationsListMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/Operations_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void operationsListMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### PaloAltoNetworksCloudngfwOperations_CreateProductSerialNumber

```java
/**
 * Samples for PaloAltoNetworksCloudngfwOperations CreateProductSerialNumber.
 */
public final class PaloAltoNetworksCloudngfwOperationsCreateProductSerialNumberSamples {
    /*
     * x-ms-original-file: 2025-10-08/PaloAltoNetworksCloudngfwOperations_createProductSerialNumber_MinimumSet_Gen.json
     */
    /**
     * Sample code: PaloAltoNetworksCloudngfwOperations_createProductSerialNumber_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void paloAltoNetworksCloudngfwOperationsCreateProductSerialNumberMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.paloAltoNetworksCloudngfwOperations()
            .createProductSerialNumberWithResponse(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/PaloAltoNetworksCloudngfwOperations_createProductSerialNumber_MaximumSet_Gen.json
     */
    /**
     * Sample code: PaloAltoNetworksCloudngfwOperations_createProductSerialNumber_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void paloAltoNetworksCloudngfwOperationsCreateProductSerialNumberMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.paloAltoNetworksCloudngfwOperations()
            .createProductSerialNumberWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### PaloAltoNetworksCloudngfwOperations_ListCloudManagerTenants

```java
/**
 * Samples for PaloAltoNetworksCloudngfwOperations ListCloudManagerTenants.
 */
public final class PaloAltoNetworksCloudngfwOperationsListCloudManagerTenantsSamples {
    /*
     * x-ms-original-file: 2025-10-08/PaloAltoNetworksCloudngfwOperations_listCloudManagerTenants_MinimumSet_Gen.json
     */
    /**
     * Sample code: PaloAltoNetworksCloudngfwOperations_listCloudManagerTenants_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void paloAltoNetworksCloudngfwOperationsListCloudManagerTenantsMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.paloAltoNetworksCloudngfwOperations()
            .listCloudManagerTenantsWithResponse(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/PaloAltoNetworksCloudngfwOperations_listCloudManagerTenants_MaximumSet_Gen.json
     */
    /**
     * Sample code: PaloAltoNetworksCloudngfwOperations_listCloudManagerTenants_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void paloAltoNetworksCloudngfwOperationsListCloudManagerTenantsMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.paloAltoNetworksCloudngfwOperations()
            .listCloudManagerTenantsWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### PaloAltoNetworksCloudngfwOperations_ListProductSerialNumberStatus

```java
/**
 * Samples for PaloAltoNetworksCloudngfwOperations ListProductSerialNumberStatus.
 */
public final class PaloAltoNetworksCloudngfwOperationsListProductSerialNumberStatusSamples {
    /*
     * x-ms-original-file:
     * 2025-10-08/PaloAltoNetworksCloudngfwOperations_listProductSerialNumberStatus_MinimumSet_Gen.json
     */
    /**
     * Sample code: PaloAltoNetworksCloudngfwOperations_listProductSerialNumberStatus_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void paloAltoNetworksCloudngfwOperationsListProductSerialNumberStatusMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.paloAltoNetworksCloudngfwOperations()
            .listProductSerialNumberStatusWithResponse(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * 2025-10-08/PaloAltoNetworksCloudngfwOperations_listProductSerialNumberStatus_MaximumSet_Gen.json
     */
    /**
     * Sample code: PaloAltoNetworksCloudngfwOperations_listProductSerialNumberStatus_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void paloAltoNetworksCloudngfwOperationsListProductSerialNumberStatusMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.paloAltoNetworksCloudngfwOperations()
            .listProductSerialNumberStatusWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### PaloAltoNetworksCloudngfwOperations_ListSupportInfo

```java
/**
 * Samples for PaloAltoNetworksCloudngfwOperations ListSupportInfo.
 */
public final class PaloAltoNetworksCloudngfwOperationsListSupportInfoSamples {
    /*
     * x-ms-original-file: 2025-10-08/PaloAltoNetworksCloudngfwOperations_listSupportInfo_MinimumSet_Gen.json
     */
    /**
     * Sample code: PaloAltoNetworksCloudngfwOperations_listSupportInfo_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void paloAltoNetworksCloudngfwOperationsListSupportInfoMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.paloAltoNetworksCloudngfwOperations().listSupportInfoWithResponse(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/PaloAltoNetworksCloudngfwOperations_listSupportInfo_MaximumSet_Gen.json
     */
    /**
     * Sample code: PaloAltoNetworksCloudngfwOperations_listSupportInfo_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void paloAltoNetworksCloudngfwOperationsListSupportInfoMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.paloAltoNetworksCloudngfwOperations().listSupportInfoWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### PostRules_CreateOrUpdate

```java
import com.azure.resourcemanager.paloaltonetworks.ngfw.fluent.models.PostRulesResourceInner;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.ActionEnum;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.BooleanEnum;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.Category;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.DecryptionRuleTypeEnum;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.DestinationAddr;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.SourceAddr;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.StateEnum;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.TagInfo;
import java.util.Arrays;

/**
 * Samples for PostRules CreateOrUpdate.
 */
public final class PostRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-10-08/PostRules_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: PostRules_CreateOrUpdate_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void postRulesCreateOrUpdateMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.postRules()
            .createOrUpdate("lrs1", "1",
                new PostRulesResourceInner().withEtag("c18e6eef-ba3e-49ee-8a85-2b36c863a9d0")
                    .withRuleName("postRule1")
                    .withDescription("description of post rule")
                    .withRuleState(StateEnum.DISABLED)
                    .withSource(new SourceAddr().withCidrs(Arrays.asList("1.0.0.1/10"))
                        .withCountries(Arrays.asList("India"))
                        .withFeeds(Arrays.asList("feed"))
                        .withPrefixLists(Arrays.asList("PL1")))
                    .withNegateSource(BooleanEnum.TRUE)
                    .withDestination(new DestinationAddr().withCidrs(Arrays.asList("1.0.0.1/10"))
                        .withCountries(Arrays.asList("India"))
                        .withFeeds(Arrays.asList("feed"))
                        .withPrefixLists(Arrays.asList("PL1"))
                        .withFqdnLists(Arrays.asList("FQDN1")))
                    .withNegateDestination(BooleanEnum.TRUE)
                    .withApplications(Arrays.asList("app1"))
                    .withCategory(new Category().withUrlCustom(Arrays.asList("https://microsoft.com"))
                        .withFeeds(Arrays.asList("feed")))
                    .withProtocol("HTTP")
                    .withProtocolPortList(Arrays.asList("80"))
                    .withInboundInspectionCertificate("cert1")
                    .withAuditComment("example comment")
                    .withActionType(ActionEnum.ALLOW)
                    .withEnableLogging(StateEnum.DISABLED)
                    .withDecryptionRuleType(DecryptionRuleTypeEnum.SSLOUTBOUND_INSPECTION)
                    .withTags(Arrays.asList(new TagInfo().withKey("fakeTokenPlaceholder").withValue("value"))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/PostRules_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: PostRules_CreateOrUpdate_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void postRulesCreateOrUpdateMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.postRules()
            .createOrUpdate("lrs1", "1", new PostRulesResourceInner().withRuleName("postRule1"),
                com.azure.core.util.Context.NONE);
    }
}
```

### PostRules_Delete

```java
/**
 * Samples for PostRules Delete.
 */
public final class PostRulesDeleteSamples {
    /*
     * x-ms-original-file: 2025-10-08/PostRules_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: PostRules_Delete_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void postRulesDeleteMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.postRules().delete("lrs1", "1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/PostRules_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: PostRules_Delete_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void postRulesDeleteMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.postRules().delete("lrs1", "1", com.azure.core.util.Context.NONE);
    }
}
```

### PostRules_Get

```java
/**
 * Samples for PostRules Get.
 */
public final class PostRulesGetSamples {
    /*
     * x-ms-original-file: 2025-10-08/PostRules_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: PostRules_Get_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void
        postRulesGetMinimumSetGen(com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.postRules().getWithResponse("lrs1", "1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/PostRules_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: PostRules_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void
        postRulesGetMaximumSetGen(com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.postRules().getWithResponse("lrs1", "1", com.azure.core.util.Context.NONE);
    }
}
```

### PostRules_GetCounters

```java
/**
 * Samples for PostRules GetCounters.
 */
public final class PostRulesGetCountersSamples {
    /*
     * x-ms-original-file: 2025-10-08/PostRules_getCounters_MinimumSet_Gen.json
     */
    /**
     * Sample code: PostRules_getCounters_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void postRulesGetCountersMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.postRules().getCountersWithResponse("lrs1", "1", null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/PostRules_getCounters_MaximumSet_Gen.json
     */
    /**
     * Sample code: PostRules_getCounters_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void postRulesGetCountersMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.postRules().getCountersWithResponse("lrs1", "1", "firewall1", com.azure.core.util.Context.NONE);
    }
}
```

### PostRules_List

```java
/**
 * Samples for PostRules List.
 */
public final class PostRulesListSamples {
    /*
     * x-ms-original-file: 2025-10-08/PostRules_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: PostRules_List_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void postRulesListMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.postRules().list("lrs1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/PostRules_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: PostRules_List_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void postRulesListMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.postRules().list("lrs1", com.azure.core.util.Context.NONE);
    }
}
```

### PostRules_RefreshCounters

```java
/**
 * Samples for PostRules RefreshCounters.
 */
public final class PostRulesRefreshCountersSamples {
    /*
     * x-ms-original-file: 2025-10-08/PostRules_refreshCounters_MaximumSet_Gen.json
     */
    /**
     * Sample code: PostRules_refreshCounters_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void postRulesRefreshCountersMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.postRules().refreshCountersWithResponse("lrs1", "1", "firewall1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/PostRules_refreshCounters_MinimumSet_Gen.json
     */
    /**
     * Sample code: PostRules_refreshCounters_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void postRulesRefreshCountersMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.postRules().refreshCountersWithResponse("lrs1", "1", null, com.azure.core.util.Context.NONE);
    }
}
```

### PostRules_ResetCounters

```java
/**
 * Samples for PostRules ResetCounters.
 */
public final class PostRulesResetCountersSamples {
    /*
     * x-ms-original-file: 2025-10-08/PostRules_resetCounters_MaximumSet_Gen.json
     */
    /**
     * Sample code: PostRules_resetCounters_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void postRulesResetCountersMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.postRules().resetCountersWithResponse("lrs1", "1", "firewall1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/PostRules_resetCounters_MinimumSet_Gen.json
     */
    /**
     * Sample code: PostRules_resetCounters_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void postRulesResetCountersMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.postRules().resetCountersWithResponse("lrs1", "1", null, com.azure.core.util.Context.NONE);
    }
}
```

### PreRules_CreateOrUpdate

```java
import com.azure.resourcemanager.paloaltonetworks.ngfw.fluent.models.PreRulesResourceInner;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.ActionEnum;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.BooleanEnum;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.Category;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.DecryptionRuleTypeEnum;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.DestinationAddr;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.SourceAddr;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.StateEnum;
import com.azure.resourcemanager.paloaltonetworks.ngfw.models.TagInfo;
import java.util.Arrays;

/**
 * Samples for PreRules CreateOrUpdate.
 */
public final class PreRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-10-08/PreRules_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: PreRules_CreateOrUpdate_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void preRulesCreateOrUpdateMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.preRules()
            .createOrUpdate("lrs1", "1",
                new PreRulesResourceInner().withEtag("c18e6eef-ba3e-49ee-8a85-2b36c863a9d0")
                    .withRuleName("preRule1")
                    .withDescription("description of pre rule")
                    .withRuleState(StateEnum.DISABLED)
                    .withSource(new SourceAddr().withCidrs(Arrays.asList("1.0.0.1/10"))
                        .withCountries(Arrays.asList("India"))
                        .withFeeds(Arrays.asList("feed"))
                        .withPrefixLists(Arrays.asList("PL1")))
                    .withNegateSource(BooleanEnum.TRUE)
                    .withDestination(new DestinationAddr().withCidrs(Arrays.asList("1.0.0.1/10"))
                        .withCountries(Arrays.asList("India"))
                        .withFeeds(Arrays.asList("feed"))
                        .withPrefixLists(Arrays.asList("PL1"))
                        .withFqdnLists(Arrays.asList("FQDN1")))
                    .withNegateDestination(BooleanEnum.TRUE)
                    .withApplications(Arrays.asList("app1"))
                    .withCategory(new Category().withUrlCustom(Arrays.asList("https://microsoft.com"))
                        .withFeeds(Arrays.asList("feed")))
                    .withProtocol("HTTP")
                    .withProtocolPortList(Arrays.asList("80"))
                    .withInboundInspectionCertificate("cert1")
                    .withAuditComment("example comment")
                    .withActionType(ActionEnum.ALLOW)
                    .withEnableLogging(StateEnum.DISABLED)
                    .withDecryptionRuleType(DecryptionRuleTypeEnum.SSLOUTBOUND_INSPECTION)
                    .withTags(Arrays.asList(new TagInfo().withKey("fakeTokenPlaceholder").withValue("value"))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/PreRules_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: PreRules_CreateOrUpdate_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void preRulesCreateOrUpdateMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.preRules()
            .createOrUpdate("lrs1", "1", new PreRulesResourceInner().withRuleName("preRule1"),
                com.azure.core.util.Context.NONE);
    }
}
```

### PreRules_Delete

```java
/**
 * Samples for PreRules Delete.
 */
public final class PreRulesDeleteSamples {
    /*
     * x-ms-original-file: 2025-10-08/PreRules_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: PreRules_Delete_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void preRulesDeleteMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.preRules().delete("lrs1", "1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/PreRules_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: PreRules_Delete_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void preRulesDeleteMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.preRules().delete("lrs1", "1", com.azure.core.util.Context.NONE);
    }
}
```

### PreRules_Get

```java
/**
 * Samples for PreRules Get.
 */
public final class PreRulesGetSamples {
    /*
     * x-ms-original-file: 2025-10-08/PreRules_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: PreRules_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void
        preRulesGetMaximumSetGen(com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.preRules().getWithResponse("lrs1", "1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/PreRules_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: PreRules_Get_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void
        preRulesGetMinimumSetGen(com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.preRules().getWithResponse("lrs1", "1", com.azure.core.util.Context.NONE);
    }
}
```

### PreRules_GetCounters

```java
/**
 * Samples for PreRules GetCounters.
 */
public final class PreRulesGetCountersSamples {
    /*
     * x-ms-original-file: 2025-10-08/PreRules_getCounters_MinimumSet_Gen.json
     */
    /**
     * Sample code: PreRules_getCounters_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void preRulesGetCountersMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.preRules().getCountersWithResponse("lrs1", "1", null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/PreRules_getCounters_MaximumSet_Gen.json
     */
    /**
     * Sample code: PreRules_getCounters_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void preRulesGetCountersMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.preRules().getCountersWithResponse("lrs1", "1", "firewall1", com.azure.core.util.Context.NONE);
    }
}
```

### PreRules_List

```java
/**
 * Samples for PreRules List.
 */
public final class PreRulesListSamples {
    /*
     * x-ms-original-file: 2025-10-08/PreRules_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: PreRules_List_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void
        preRulesListMinimumSetGen(com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.preRules().list("lrs1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/PreRules_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: PreRules_List_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void
        preRulesListMaximumSetGen(com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.preRules().list("lrs1", com.azure.core.util.Context.NONE);
    }
}
```

### PreRules_RefreshCounters

```java
/**
 * Samples for PreRules RefreshCounters.
 */
public final class PreRulesRefreshCountersSamples {
    /*
     * x-ms-original-file: 2025-10-08/PreRules_refreshCounters_MaximumSet_Gen.json
     */
    /**
     * Sample code: PreRules_refreshCounters_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void preRulesRefreshCountersMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.preRules().refreshCountersWithResponse("lrs1", "1", "firewall1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/PreRules_refreshCounters_MinimumSet_Gen.json
     */
    /**
     * Sample code: PreRules_refreshCounters_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void preRulesRefreshCountersMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.preRules().refreshCountersWithResponse("lrs1", "1", null, com.azure.core.util.Context.NONE);
    }
}
```

### PreRules_ResetCounters

```java
/**
 * Samples for PreRules ResetCounters.
 */
public final class PreRulesResetCountersSamples {
    /*
     * x-ms-original-file: 2025-10-08/PreRules_resetCounters_MinimumSet_Gen.json
     */
    /**
     * Sample code: PreRules_resetCounters_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void preRulesResetCountersMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.preRules().resetCountersWithResponse("lrs1", "1", null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/PreRules_resetCounters_MaximumSet_Gen.json
     */
    /**
     * Sample code: PreRules_resetCounters_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void preRulesResetCountersMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.preRules().resetCountersWithResponse("lrs1", "1", "firewall1", com.azure.core.util.Context.NONE);
    }
}
```

### PrefixListGlobalRulestack_CreateOrUpdate

```java
import com.azure.resourcemanager.paloaltonetworks.ngfw.fluent.models.PrefixListGlobalRulestackResourceInner;
import java.util.Arrays;

/**
 * Samples for PrefixListGlobalRulestack CreateOrUpdate.
 */
public final class PrefixListGlobalRulestackCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-10-08/PrefixListGlobalRulestack_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: PrefixListGlobalRulestack_CreateOrUpdate_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void prefixListGlobalRulestackCreateOrUpdateMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.prefixListGlobalRulestacks()
            .createOrUpdate("praval", "armid1",
                new PrefixListGlobalRulestackResourceInner().withDescription("string")
                    .withPrefixList(Arrays.asList("1.0.0.0/24"))
                    .withEtag("2bf4a339-294d-4c25-b0b2-ef649e9f5c27")
                    .withAuditComment("comment"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/PrefixListGlobalRulestack_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: PrefixListGlobalRulestack_CreateOrUpdate_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void prefixListGlobalRulestackCreateOrUpdateMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.prefixListGlobalRulestacks()
            .createOrUpdate("praval", "armid1",
                new PrefixListGlobalRulestackResourceInner().withPrefixList(Arrays.asList("1.0.0.0/24")),
                com.azure.core.util.Context.NONE);
    }
}
```

### PrefixListGlobalRulestack_Delete

```java
/**
 * Samples for PrefixListGlobalRulestack Delete.
 */
public final class PrefixListGlobalRulestackDeleteSamples {
    /*
     * x-ms-original-file: 2025-10-08/PrefixListGlobalRulestack_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: PrefixListGlobalRulestack_Delete_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void prefixListGlobalRulestackDeleteMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.prefixListGlobalRulestacks().delete("praval", "armid1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/PrefixListGlobalRulestack_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: PrefixListGlobalRulestack_Delete_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void prefixListGlobalRulestackDeleteMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.prefixListGlobalRulestacks().delete("praval", "armid1", com.azure.core.util.Context.NONE);
    }
}
```

### PrefixListGlobalRulestack_Get

```java
/**
 * Samples for PrefixListGlobalRulestack Get.
 */
public final class PrefixListGlobalRulestackGetSamples {
    /*
     * x-ms-original-file: 2025-10-08/PrefixListGlobalRulestack_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: PrefixListGlobalRulestack_Get_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void prefixListGlobalRulestackGetMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.prefixListGlobalRulestacks().getWithResponse("praval", "armid1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/PrefixListGlobalRulestack_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: PrefixListGlobalRulestack_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void prefixListGlobalRulestackGetMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.prefixListGlobalRulestacks().getWithResponse("praval", "armid1", com.azure.core.util.Context.NONE);
    }
}
```

### PrefixListGlobalRulestack_List

```java
/**
 * Samples for PrefixListGlobalRulestack List.
 */
public final class PrefixListGlobalRulestackListSamples {
    /*
     * x-ms-original-file: 2025-10-08/PrefixListGlobalRulestack_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: PrefixListGlobalRulestack_List_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void prefixListGlobalRulestackListMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.prefixListGlobalRulestacks().list("praval", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/PrefixListGlobalRulestack_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: PrefixListGlobalRulestack_List_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void prefixListGlobalRulestackListMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.prefixListGlobalRulestacks().list("praval", com.azure.core.util.Context.NONE);
    }
}
```

### PrefixListLocalRulestack_CreateOrUpdate

```java
import java.util.Arrays;

/**
 * Samples for PrefixListLocalRulestack CreateOrUpdate.
 */
public final class PrefixListLocalRulestackCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-10-08/PrefixListLocalRulestack_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: PrefixListLocalRulestack_CreateOrUpdate_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void prefixListLocalRulestackCreateOrUpdateMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.prefixListLocalRulestacks()
            .define("armid1")
            .withExistingLocalRulestack("rgopenapi", "lrs1")
            .withPrefixList(Arrays.asList("1.0.0.0/24"))
            .create();
    }

    /*
     * x-ms-original-file: 2025-10-08/PrefixListLocalRulestack_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: PrefixListLocalRulestack_CreateOrUpdate_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void prefixListLocalRulestackCreateOrUpdateMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.prefixListLocalRulestacks()
            .define("armid1")
            .withExistingLocalRulestack("rgopenapi", "lrs1")
            .withPrefixList(Arrays.asList("1.0.0.0/24"))
            .withDescription("string")
            .withEtag("2bf4a339-294d-4c25-b0b2-ef649e9f5c27")
            .withAuditComment("comment")
            .create();
    }
}
```

### PrefixListLocalRulestack_Delete

```java
/**
 * Samples for PrefixListLocalRulestack Delete.
 */
public final class PrefixListLocalRulestackDeleteSamples {
    /*
     * x-ms-original-file: 2025-10-08/PrefixListLocalRulestack_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: PrefixListLocalRulestack_Delete_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void prefixListLocalRulestackDeleteMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.prefixListLocalRulestacks().delete("rgopenapi", "lrs1", "armid1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/PrefixListLocalRulestack_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: PrefixListLocalRulestack_Delete_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void prefixListLocalRulestackDeleteMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.prefixListLocalRulestacks().delete("rgopenapi", "lrs1", "armid1", com.azure.core.util.Context.NONE);
    }
}
```

### PrefixListLocalRulestack_Get

```java
/**
 * Samples for PrefixListLocalRulestack Get.
 */
public final class PrefixListLocalRulestackGetSamples {
    /*
     * x-ms-original-file: 2025-10-08/PrefixListLocalRulestack_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: PrefixListLocalRulestack_Get_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void prefixListLocalRulestackGetMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.prefixListLocalRulestacks()
            .getWithResponse("rgopenapi", "lrs1", "armid1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/PrefixListLocalRulestack_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: PrefixListLocalRulestack_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void prefixListLocalRulestackGetMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.prefixListLocalRulestacks()
            .getWithResponse("rgopenapi", "lrs1", "armid1", com.azure.core.util.Context.NONE);
    }
}
```

### PrefixListLocalRulestack_ListByLocalRulestacks

```java
/**
 * Samples for PrefixListLocalRulestack ListByLocalRulestacks.
 */
public final class PrefixListLocalRulestackListByLocalRulestacksSamples {
    /*
     * x-ms-original-file: 2025-10-08/PrefixListLocalRulestack_ListByLocalRulestacks_MaximumSet_Gen.json
     */
    /**
     * Sample code: PrefixListLocalRulestack_ListByLocalRulestacks_MaximumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void prefixListLocalRulestackListByLocalRulestacksMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.prefixListLocalRulestacks()
            .listByLocalRulestacks("rgopenapi", "lrs1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-08/PrefixListLocalRulestack_ListByLocalRulestacks_MinimumSet_Gen.json
     */
    /**
     * Sample code: PrefixListLocalRulestack_ListByLocalRulestacks_MinimumSet_Gen.
     * 
     * @param manager Entry point to PaloAltoNetworksNgfwManager.
     */
    public static void prefixListLocalRulestackListByLocalRulestacksMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.ngfw.PaloAltoNetworksNgfwManager manager) {
        manager.prefixListLocalRulestacks()
            .listByLocalRulestacks("rgopenapi", "lrs1", com.azure.core.util.Context.NONE);
    }
}
```


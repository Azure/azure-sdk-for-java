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

## Operations

- [List](#operations_list)

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
import com.azure.resourcemanager.paloaltonetworks.fluent.models.CertificateObjectGlobalRulestackResourceInner;
import com.azure.resourcemanager.paloaltonetworks.models.BooleanEnum;

/** Samples for CertificateObjectGlobalRulestack CreateOrUpdate. */
public final class CertificateObjectGlobalRulestackCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/CertificateObjectGlobalRulestack_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: CertificateObjectGlobalRulestack_CreateOrUpdate_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void certificateObjectGlobalRulestackCreateOrUpdateMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .certificateObjectGlobalRulestacks()
            .createOrUpdate(
                "praval",
                "armid1",
                new CertificateObjectGlobalRulestackResourceInner().withCertificateSelfSigned(BooleanEnum.TRUE),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/CertificateObjectGlobalRulestack_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: CertificateObjectGlobalRulestack_CreateOrUpdate_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void certificateObjectGlobalRulestackCreateOrUpdateMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .certificateObjectGlobalRulestacks()
            .createOrUpdate(
                "praval",
                "armid1",
                new CertificateObjectGlobalRulestackResourceInner()
                    .withCertificateSignerResourceId("")
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
/** Samples for CertificateObjectGlobalRulestack Delete. */
public final class CertificateObjectGlobalRulestackDeleteSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/CertificateObjectGlobalRulestack_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: CertificateObjectGlobalRulestack_Delete_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void certificateObjectGlobalRulestackDeleteMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.certificateObjectGlobalRulestacks().delete("praval", "armid1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/CertificateObjectGlobalRulestack_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: CertificateObjectGlobalRulestack_Delete_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void certificateObjectGlobalRulestackDeleteMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.certificateObjectGlobalRulestacks().delete("praval", "armid1", com.azure.core.util.Context.NONE);
    }
}
```

### CertificateObjectGlobalRulestack_Get

```java
/** Samples for CertificateObjectGlobalRulestack Get. */
public final class CertificateObjectGlobalRulestackGetSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/CertificateObjectGlobalRulestack_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: CertificateObjectGlobalRulestack_Get_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void certificateObjectGlobalRulestackGetMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .certificateObjectGlobalRulestacks()
            .getWithResponse("praval", "armid1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/CertificateObjectGlobalRulestack_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: CertificateObjectGlobalRulestack_Get_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void certificateObjectGlobalRulestackGetMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .certificateObjectGlobalRulestacks()
            .getWithResponse("praval", "armid1", com.azure.core.util.Context.NONE);
    }
}
```

### CertificateObjectGlobalRulestack_List

```java
/** Samples for CertificateObjectGlobalRulestack List. */
public final class CertificateObjectGlobalRulestackListSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/CertificateObjectGlobalRulestack_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: CertificateObjectGlobalRulestack_List_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void certificateObjectGlobalRulestackListMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.certificateObjectGlobalRulestacks().list("praval", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/CertificateObjectGlobalRulestack_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: CertificateObjectGlobalRulestack_List_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void certificateObjectGlobalRulestackListMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.certificateObjectGlobalRulestacks().list("praval", com.azure.core.util.Context.NONE);
    }
}
```

### CertificateObjectLocalRulestack_CreateOrUpdate

```java
import com.azure.resourcemanager.paloaltonetworks.models.BooleanEnum;

/** Samples for CertificateObjectLocalRulestack CreateOrUpdate. */
public final class CertificateObjectLocalRulestackCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/CertificateObjectLocalRulestack_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: CertificateObjectLocalRulestack_CreateOrUpdate_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void certificateObjectLocalRulestackCreateOrUpdateMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .certificateObjectLocalRulestacks()
            .define("armid1")
            .withExistingLocalRulestack("rgopenapi", "lrs1")
            .withCertificateSelfSigned(BooleanEnum.TRUE)
            .create();
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/CertificateObjectLocalRulestack_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: CertificateObjectLocalRulestack_CreateOrUpdate_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void certificateObjectLocalRulestackCreateOrUpdateMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .certificateObjectLocalRulestacks()
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
/** Samples for CertificateObjectLocalRulestack Delete. */
public final class CertificateObjectLocalRulestackDeleteSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/CertificateObjectLocalRulestack_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: CertificateObjectLocalRulestack_Delete_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void certificateObjectLocalRulestackDeleteMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .certificateObjectLocalRulestacks()
            .delete("rgopenapi", "lrs1", "armid1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/CertificateObjectLocalRulestack_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: CertificateObjectLocalRulestack_Delete_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void certificateObjectLocalRulestackDeleteMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .certificateObjectLocalRulestacks()
            .delete("rgopenapi", "lrs1", "armid1", com.azure.core.util.Context.NONE);
    }
}
```

### CertificateObjectLocalRulestack_Get

```java
/** Samples for CertificateObjectLocalRulestack Get. */
public final class CertificateObjectLocalRulestackGetSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/CertificateObjectLocalRulestack_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: CertificateObjectLocalRulestack_Get_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void certificateObjectLocalRulestackGetMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .certificateObjectLocalRulestacks()
            .getWithResponse("rgopenapi", "lrs1", "armid1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/CertificateObjectLocalRulestack_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: CertificateObjectLocalRulestack_Get_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void certificateObjectLocalRulestackGetMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .certificateObjectLocalRulestacks()
            .getWithResponse("rgopenapi", "lrs1", "armid1", com.azure.core.util.Context.NONE);
    }
}
```

### CertificateObjectLocalRulestack_ListByLocalRulestacks

```java
/** Samples for CertificateObjectLocalRulestack ListByLocalRulestacks. */
public final class CertificateObjectLocalRulestackListByLocalRulestacksSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/CertificateObjectLocalRulestack_ListByLocalRulestacks_MaximumSet_Gen.json
     */
    /**
     * Sample code: CertificateObjectLocalRulestack_ListByLocalRulestacks_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void certificateObjectLocalRulestackListByLocalRulestacksMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .certificateObjectLocalRulestacks()
            .listByLocalRulestacks("rgopenapi", "lrs1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/CertificateObjectLocalRulestack_ListByLocalRulestacks_MinimumSet_Gen.json
     */
    /**
     * Sample code: CertificateObjectLocalRulestack_ListByLocalRulestacks_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void certificateObjectLocalRulestackListByLocalRulestacksMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .certificateObjectLocalRulestacks()
            .listByLocalRulestacks("rgopenapi", "lrs1", com.azure.core.util.Context.NONE);
    }
}
```

### Firewalls_CreateOrUpdate

```java
import com.azure.resourcemanager.paloaltonetworks.models.AzureResourceManagerManagedIdentityProperties;
import com.azure.resourcemanager.paloaltonetworks.models.AzureResourceManagerUserAssignedIdentity;
import com.azure.resourcemanager.paloaltonetworks.models.BillingCycle;
import com.azure.resourcemanager.paloaltonetworks.models.BooleanEnum;
import com.azure.resourcemanager.paloaltonetworks.models.DnsProxy;
import com.azure.resourcemanager.paloaltonetworks.models.DnsSettings;
import com.azure.resourcemanager.paloaltonetworks.models.EgressNat;
import com.azure.resourcemanager.paloaltonetworks.models.EnabledDnsType;
import com.azure.resourcemanager.paloaltonetworks.models.EndpointConfiguration;
import com.azure.resourcemanager.paloaltonetworks.models.FrontendSetting;
import com.azure.resourcemanager.paloaltonetworks.models.IpAddress;
import com.azure.resourcemanager.paloaltonetworks.models.IpAddressSpace;
import com.azure.resourcemanager.paloaltonetworks.models.ManagedIdentityType;
import com.azure.resourcemanager.paloaltonetworks.models.MarketplaceDetails;
import com.azure.resourcemanager.paloaltonetworks.models.MarketplaceSubscriptionStatus;
import com.azure.resourcemanager.paloaltonetworks.models.NetworkProfile;
import com.azure.resourcemanager.paloaltonetworks.models.NetworkType;
import com.azure.resourcemanager.paloaltonetworks.models.PanoramaConfig;
import com.azure.resourcemanager.paloaltonetworks.models.PlanData;
import com.azure.resourcemanager.paloaltonetworks.models.ProtocolType;
import com.azure.resourcemanager.paloaltonetworks.models.RulestackDetails;
import com.azure.resourcemanager.paloaltonetworks.models.UsageType;
import com.azure.resourcemanager.paloaltonetworks.models.VnetConfiguration;
import com.azure.resourcemanager.paloaltonetworks.models.VwanConfiguration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Firewalls CreateOrUpdate. */
public final class FirewallsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/Firewalls_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firewalls_CreateOrUpdate_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void firewallsCreateOrUpdateMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .firewalls()
            .define("firewall1")
            .withRegion("eastus")
            .withExistingResourceGroup("firewall-rg")
            .withNetworkProfile(
                new NetworkProfile()
                    .withVnetConfiguration(
                        new VnetConfiguration()
                            .withVnet(
                                new IpAddressSpace()
                                    .withResourceId(
                                        "/subscriptions/2bf4a339-294d-4c25-b0b2-ef649e9f5c27/resourceGroups/os-liftr-integration/providers/Microsoft.Network/virtualNetworks/os-liftr-integration-vnet")
                                    .withAddressSpace("10.1.0.0/16"))
                            .withTrustSubnet(
                                new IpAddressSpace()
                                    .withResourceId(
                                        "/subscriptions/2bf4a339-294d-4c25-b0b2-ef649e9f5c27/resourceGroups/os-liftr-integration/providers/Microsoft.Network/virtualNetworks/os-liftr-integration-vnet/subnets/os-liftr-integration-trust-subnet")
                                    .withAddressSpace("10.1.1.0/24"))
                            .withUnTrustSubnet(
                                new IpAddressSpace()
                                    .withResourceId(
                                        "/subscriptions/2bf4a339-294d-4c25-b0b2-ef649e9f5c27/resourceGroups/os-liftr-integration/providers/Microsoft.Network/virtualNetworks/os-liftr-integration-vnet/subnets/os-liftr-integration-untrust-subnet")
                                    .withAddressSpace("10.1.1.0/24"))
                            .withIpOfTrustSubnetForUdr(
                                new IpAddress()
                                    .withResourceId(
                                        "/subscriptions/2bf4a339-294d-4c25-b0b2-ef649e9f5c27/resourceGroups/os-liftr-integration/providers/Microsoft.Network/virtualNetworks/os-liftr-integration-vnet/subnets/os-liftr-integration-untrust-subnet")
                                    .withAddress("10.1.1.0/24")))
                    .withVwanConfiguration(
                        new VwanConfiguration()
                            .withNetworkVirtualApplianceId("2bf4a339-294d-4c25-b0b2-ef649e9f5c12")
                            .withVHub(
                                new IpAddressSpace()
                                    .withResourceId(
                                        "/subscriptions/2bf4a339-294d-4c25-b0b2-ef649e9f5c27/resourceGroups/os-liftr-integration/providers/Microsoft.Network/virtualNetworks/os-liftr-integration-vnet/subnets/os-liftr-integration-untrust-subnet")
                                    .withAddressSpace("10.1.1.0/24"))
                            .withTrustSubnet(
                                new IpAddressSpace()
                                    .withResourceId(
                                        "/subscriptions/2bf4a339-294d-4c25-b0b2-ef649e9f5c27/resourceGroups/os-liftr-integration/providers/Microsoft.Network/virtualNetworks/os-liftr-integration-vnet/subnets/os-liftr-integration-trust-subnet")
                                    .withAddressSpace("10.1.1.0/24"))
                            .withUnTrustSubnet(
                                new IpAddressSpace()
                                    .withResourceId(
                                        "/subscriptions/2bf4a339-294d-4c25-b0b2-ef649e9f5c27/resourceGroups/os-liftr-integration/providers/Microsoft.Network/virtualNetworks/os-liftr-integration-vnet/subnets/os-liftr-integration-untrust-subnet")
                                    .withAddressSpace("10.1.1.0/24"))
                            .withIpOfTrustSubnetForUdr(
                                new IpAddress()
                                    .withResourceId(
                                        "/subscriptions/2bf4a339-294d-4c25-b0b2-ef649e9f5c27/resourceGroups/os-liftr-integration/providers/Microsoft.Network/virtualNetworks/os-liftr-integration-vnet/subnets/os-liftr-integration-untrust-subnet")
                                    .withAddress("10.1.1.0/24")))
                    .withNetworkType(NetworkType.VNET)
                    .withPublicIps(
                        Arrays
                            .asList(
                                new IpAddress()
                                    .withResourceId(
                                        "/subscriptions/01c7d41f-afaf-464e-8a8b-5c6f9f98cee8/resourceGroups/mj-liftr-integration/providers/Microsoft.Network/publicIPAddresses/mj-liftr-integration-PublicIp1")
                                    .withAddress("20.22.92.11")))
                    .withEnableEgressNat(EgressNat.ENABLED)
                    .withEgressNatIp(
                        Arrays
                            .asList(
                                new IpAddress()
                                    .withResourceId(
                                        "/subscriptions/01c7d41f-afaf-464e-8a8b-5c6f9f98cee8/resourceGroups/mj-liftr-integration/providers/Microsoft.Network/publicIPAddresses/mj-liftr-integration-egressNatIp1")
                                    .withAddress("20.22.92.111"))))
            .withDnsSettings(
                new DnsSettings()
                    .withEnableDnsProxy(DnsProxy.DISABLED)
                    .withEnabledDnsType(EnabledDnsType.CUSTOM)
                    .withDnsServers(
                        Arrays
                            .asList(
                                new IpAddress()
                                    .withResourceId(
                                        "/subscriptions/01c7d41f-afaf-464e-8a8b-5c6f9f98cee8/resourceGroups/mj-liftr-integration/providers/Microsoft.Network/publicIPAddresses/mj-liftr-integration-egressNatIp1")
                                    .withAddress("20.22.92.111"))))
            .withPlanData(
                new PlanData()
                    .withUsageType(UsageType.PAYG)
                    .withBillingCycle(BillingCycle.MONTHLY)
                    .withPlanId("liftrpantestplan"))
            .withMarketplaceDetails(
                new MarketplaceDetails()
                    .withOfferId("liftr-pan-ame-test")
                    .withPublisherId("isvtestuklegacy")
                    .withMarketplaceSubscriptionStatus(MarketplaceSubscriptionStatus.PENDING_FULFILLMENT_START))
            .withTags(mapOf("tagName", "value"))
            .withIdentity(
                new AzureResourceManagerManagedIdentityProperties()
                    .withType(ManagedIdentityType.NONE)
                    .withUserAssignedIdentities(
                        mapOf(
                            "key16",
                            new AzureResourceManagerUserAssignedIdentity()
                                .withClientId("aaaa")
                                .withPrincipalId("aaaaaaaaaaaaaaa"))))
            .withPanEtag("2bf4a339-294d-4c25-b0b2-ef649e9f5c12")
            .withIsPanoramaManaged(BooleanEnum.TRUE)
            .withPanoramaConfig(new PanoramaConfig().withConfigString("bas64EncodedString"))
            .withAssociatedRulestack(
                new RulestackDetails().withResourceId("lrs1").withRulestackId("PANRSID").withLocation("eastus"))
            .withFrontEndSettings(
                Arrays
                    .asList(
                        new FrontendSetting()
                            .withName("frontendsetting11")
                            .withProtocol(ProtocolType.TCP)
                            .withFrontendConfiguration(
                                new EndpointConfiguration()
                                    .withPort("80")
                                    .withAddress(
                                        new IpAddress()
                                            .withResourceId(
                                                "/subscriptions/01c7d41f-afaf-464e-8a8b-5c6f9f98cee8/resourceGroups/mj-liftr-integration/providers/Microsoft.Network/publicIPAddresses/mj-liftr-integration-frontendSettingIp1")
                                            .withAddress("20.22.91.251")))
                            .withBackendConfiguration(
                                new EndpointConfiguration()
                                    .withPort("80")
                                    .withAddress(
                                        new IpAddress()
                                            .withResourceId(
                                                "/subscriptions/01c7d41f-afaf-464e-8a8b-5c6f9f98cee8/resourceGroups/mj-liftr-integration/providers/Microsoft.Network/publicIPAddresses/mj-liftr-integration-frontendSettingIp2")
                                            .withAddress("20.22.32.136")))))
            .create();
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/Firewalls_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firewalls_CreateOrUpdate_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void firewallsCreateOrUpdateMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .firewalls()
            .define("firewall1")
            .withRegion("eastus")
            .withExistingResourceGroup("firewall-rg")
            .withNetworkProfile(
                new NetworkProfile()
                    .withNetworkType(NetworkType.VNET)
                    .withPublicIps(
                        Arrays
                            .asList(
                                new IpAddress()
                                    .withResourceId(
                                        "/subscriptions/01c7d41f-afaf-464e-8a8b-5c6f9f98cee8/resourceGroups/mj-liftr-integration/providers/Microsoft.Network/publicIPAddresses/mj-liftr-integration-PublicIp1")
                                    .withAddress("20.22.92.11")))
                    .withEnableEgressNat(EgressNat.ENABLED))
            .withDnsSettings(new DnsSettings())
            .withPlanData(new PlanData().withBillingCycle(BillingCycle.MONTHLY).withPlanId("liftrpantestplan"))
            .withMarketplaceDetails(
                new MarketplaceDetails().withOfferId("liftr-pan-ame-test").withPublisherId("isvtestuklegacy"))
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

### Firewalls_Delete

```java
/** Samples for Firewalls Delete. */
public final class FirewallsDeleteSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/Firewalls_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firewalls_Delete_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void firewallsDeleteMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.firewalls().delete("firewall-rg", "firewall1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/Firewalls_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firewalls_Delete_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void firewallsDeleteMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.firewalls().delete("firewall-rg", "firewall1", com.azure.core.util.Context.NONE);
    }
}
```

### Firewalls_GetByResourceGroup

```java
/** Samples for Firewalls GetByResourceGroup. */
public final class FirewallsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/Firewalls_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firewalls_Get_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void firewallsGetMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .firewalls()
            .getByResourceGroupWithResponse("firewall-rg", "firewall1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/Firewalls_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firewalls_Get_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void firewallsGetMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .firewalls()
            .getByResourceGroupWithResponse("firewall-rg", "firewall1", com.azure.core.util.Context.NONE);
    }
}
```

### Firewalls_GetGlobalRulestack

```java
/** Samples for Firewalls GetGlobalRulestack. */
public final class FirewallsGetGlobalRulestackSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/Firewalls_getGlobalRulestack_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firewalls_getGlobalRulestack_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void firewallsGetGlobalRulestackMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .firewalls()
            .getGlobalRulestackWithResponse("firewall-rg", "firewall1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/Firewalls_getGlobalRulestack_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firewalls_getGlobalRulestack_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void firewallsGetGlobalRulestackMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .firewalls()
            .getGlobalRulestackWithResponse("firewall-rg", "firewall1", com.azure.core.util.Context.NONE);
    }
}
```

### Firewalls_GetLogProfile

```java
/** Samples for Firewalls GetLogProfile. */
public final class FirewallsGetLogProfileSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/Firewalls_getLogProfile_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firewalls_getLogProfile_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void firewallsGetLogProfileMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.firewalls().getLogProfileWithResponse("firewall-rg", "firewall1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/Firewalls_getLogProfile_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firewalls_getLogProfile_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void firewallsGetLogProfileMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.firewalls().getLogProfileWithResponse("firewall-rg", "firewall1", com.azure.core.util.Context.NONE);
    }
}
```

### Firewalls_GetSupportInfo

```java
/** Samples for Firewalls GetSupportInfo. */
public final class FirewallsGetSupportInfoSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/Firewalls_getSupportInfo_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firewalls_getSupportInfo_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void firewallsGetSupportInfoMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .firewalls()
            .getSupportInfoWithResponse("rgopenapi", "firewall1", null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/Firewalls_getSupportInfo_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firewalls_getSupportInfo_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void firewallsGetSupportInfoMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .firewalls()
            .getSupportInfoWithResponse("rgopenapi", "firewall1", "user1@domain.com", com.azure.core.util.Context.NONE);
    }
}
```

### Firewalls_List

```java
/** Samples for Firewalls List. */
public final class FirewallsListSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/Firewalls_ListBySubscription_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firewalls_ListBySubscription_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void firewallsListBySubscriptionMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.firewalls().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/Firewalls_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firewalls_ListBySubscription_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void firewallsListBySubscriptionMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.firewalls().list(com.azure.core.util.Context.NONE);
    }
}
```

### Firewalls_ListByResourceGroup

```java
/** Samples for Firewalls ListByResourceGroup. */
public final class FirewallsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/Firewalls_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firewalls_ListByResourceGroup_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void firewallsListByResourceGroupMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.firewalls().listByResourceGroup("firewall-rg", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/Firewalls_ListByResourceGroup_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firewalls_ListByResourceGroup_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void firewallsListByResourceGroupMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.firewalls().listByResourceGroup("firewall-rg", com.azure.core.util.Context.NONE);
    }
}
```

### Firewalls_SaveLogProfile

```java
import com.azure.resourcemanager.paloaltonetworks.fluent.models.LogSettingsInner;
import com.azure.resourcemanager.paloaltonetworks.models.ApplicationInsights;
import com.azure.resourcemanager.paloaltonetworks.models.EventHub;
import com.azure.resourcemanager.paloaltonetworks.models.LogDestination;
import com.azure.resourcemanager.paloaltonetworks.models.LogOption;
import com.azure.resourcemanager.paloaltonetworks.models.LogType;
import com.azure.resourcemanager.paloaltonetworks.models.MonitorLog;
import com.azure.resourcemanager.paloaltonetworks.models.StorageAccount;

/** Samples for Firewalls SaveLogProfile. */
public final class FirewallsSaveLogProfileSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/Firewalls_saveLogProfile_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firewalls_saveLogProfile_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void firewallsSaveLogProfileMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .firewalls()
            .saveLogProfileWithResponse(
                "firewall-rg",
                "firewall1",
                new LogSettingsInner()
                    .withLogType(LogType.TRAFFIC)
                    .withLogOption(LogOption.SAME_DESTINATION)
                    .withApplicationInsights(
                        new ApplicationInsights().withId("aaaaaaaaaaaaaaaa").withKey("fakeTokenPlaceholder"))
                    .withCommonDestination(
                        new LogDestination()
                            .withStorageConfigurations(
                                new StorageAccount()
                                    .withId("aaaaaaaaaaaaaaa")
                                    .withSubscriptionId("aaaaaaaaa")
                                    .withAccountName("aaaaaaaaaaaaaaaaaaaaaaa"))
                            .withEventHubConfigurations(
                                new EventHub()
                                    .withId("aaaaaaaaaa")
                                    .withSubscriptionId("aaaaaaaaaa")
                                    .withName("aaaaaaaa")
                                    .withNameSpace("aaaaaaaaaaaaaaaaaaaaa")
                                    .withPolicyName("aaaaaaaaaaaa"))
                            .withMonitorConfigurations(
                                new MonitorLog()
                                    .withId("aaaaaaaaaaaaaaaaaaa")
                                    .withSubscriptionId("aaaaaaaaaaaaa")
                                    .withWorkspace("aaaaaaaaaaa")
                                    .withPrimaryKey("fakeTokenPlaceholder")
                                    .withSecondaryKey("fakeTokenPlaceholder")))
                    .withTrafficLogDestination(
                        new LogDestination()
                            .withStorageConfigurations(
                                new StorageAccount()
                                    .withId("aaaaaaaaaaaaaaa")
                                    .withSubscriptionId("aaaaaaaaa")
                                    .withAccountName("aaaaaaaaaaaaaaaaaaaaaaa"))
                            .withEventHubConfigurations(
                                new EventHub()
                                    .withId("aaaaaaaaaa")
                                    .withSubscriptionId("aaaaaaaaaa")
                                    .withName("aaaaaaaa")
                                    .withNameSpace("aaaaaaaaaaaaaaaaaaaaa")
                                    .withPolicyName("aaaaaaaaaaaa"))
                            .withMonitorConfigurations(
                                new MonitorLog()
                                    .withId("aaaaaaaaaaaaaaaaaaa")
                                    .withSubscriptionId("aaaaaaaaaaaaa")
                                    .withWorkspace("aaaaaaaaaaa")
                                    .withPrimaryKey("fakeTokenPlaceholder")
                                    .withSecondaryKey("fakeTokenPlaceholder")))
                    .withThreatLogDestination(
                        new LogDestination()
                            .withStorageConfigurations(
                                new StorageAccount()
                                    .withId("aaaaaaaaaaaaaaa")
                                    .withSubscriptionId("aaaaaaaaa")
                                    .withAccountName("aaaaaaaaaaaaaaaaaaaaaaa"))
                            .withEventHubConfigurations(
                                new EventHub()
                                    .withId("aaaaaaaaaa")
                                    .withSubscriptionId("aaaaaaaaaa")
                                    .withName("aaaaaaaa")
                                    .withNameSpace("aaaaaaaaaaaaaaaaaaaaa")
                                    .withPolicyName("aaaaaaaaaaaa"))
                            .withMonitorConfigurations(
                                new MonitorLog()
                                    .withId("aaaaaaaaaaaaaaaaaaa")
                                    .withSubscriptionId("aaaaaaaaaaaaa")
                                    .withWorkspace("aaaaaaaaaaa")
                                    .withPrimaryKey("fakeTokenPlaceholder")
                                    .withSecondaryKey("fakeTokenPlaceholder")))
                    .withDecryptLogDestination(
                        new LogDestination()
                            .withStorageConfigurations(
                                new StorageAccount()
                                    .withId("aaaaaaaaaaaaaaa")
                                    .withSubscriptionId("aaaaaaaaa")
                                    .withAccountName("aaaaaaaaaaaaaaaaaaaaaaa"))
                            .withEventHubConfigurations(
                                new EventHub()
                                    .withId("aaaaaaaaaa")
                                    .withSubscriptionId("aaaaaaaaaa")
                                    .withName("aaaaaaaa")
                                    .withNameSpace("aaaaaaaaaaaaaaaaaaaaa")
                                    .withPolicyName("aaaaaaaaaaaa"))
                            .withMonitorConfigurations(
                                new MonitorLog()
                                    .withId("aaaaaaaaaaaaaaaaaaa")
                                    .withSubscriptionId("aaaaaaaaaaaaa")
                                    .withWorkspace("aaaaaaaaaaa")
                                    .withPrimaryKey("fakeTokenPlaceholder")
                                    .withSecondaryKey("fakeTokenPlaceholder"))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/Firewalls_saveLogProfile_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firewalls_saveLogProfile_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void firewallsSaveLogProfileMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .firewalls()
            .saveLogProfileWithResponse("firewall-rg", "firewall1", null, com.azure.core.util.Context.NONE);
    }
}
```

### Firewalls_Update

```java
import com.azure.resourcemanager.paloaltonetworks.models.AzureResourceManagerManagedIdentityProperties;
import com.azure.resourcemanager.paloaltonetworks.models.AzureResourceManagerUserAssignedIdentity;
import com.azure.resourcemanager.paloaltonetworks.models.BillingCycle;
import com.azure.resourcemanager.paloaltonetworks.models.BooleanEnum;
import com.azure.resourcemanager.paloaltonetworks.models.DnsProxy;
import com.azure.resourcemanager.paloaltonetworks.models.DnsSettings;
import com.azure.resourcemanager.paloaltonetworks.models.EgressNat;
import com.azure.resourcemanager.paloaltonetworks.models.EnabledDnsType;
import com.azure.resourcemanager.paloaltonetworks.models.EndpointConfiguration;
import com.azure.resourcemanager.paloaltonetworks.models.FirewallResource;
import com.azure.resourcemanager.paloaltonetworks.models.FirewallResourceUpdateProperties;
import com.azure.resourcemanager.paloaltonetworks.models.FrontendSetting;
import com.azure.resourcemanager.paloaltonetworks.models.IpAddress;
import com.azure.resourcemanager.paloaltonetworks.models.IpAddressSpace;
import com.azure.resourcemanager.paloaltonetworks.models.ManagedIdentityType;
import com.azure.resourcemanager.paloaltonetworks.models.MarketplaceDetails;
import com.azure.resourcemanager.paloaltonetworks.models.MarketplaceSubscriptionStatus;
import com.azure.resourcemanager.paloaltonetworks.models.NetworkProfile;
import com.azure.resourcemanager.paloaltonetworks.models.NetworkType;
import com.azure.resourcemanager.paloaltonetworks.models.PanoramaConfig;
import com.azure.resourcemanager.paloaltonetworks.models.PlanData;
import com.azure.resourcemanager.paloaltonetworks.models.ProtocolType;
import com.azure.resourcemanager.paloaltonetworks.models.RulestackDetails;
import com.azure.resourcemanager.paloaltonetworks.models.UsageType;
import com.azure.resourcemanager.paloaltonetworks.models.VnetConfiguration;
import com.azure.resourcemanager.paloaltonetworks.models.VwanConfiguration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Firewalls Update. */
public final class FirewallsUpdateSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/Firewalls_Update_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firewalls_Update_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void firewallsUpdateMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        FirewallResource resource =
            manager
                .firewalls()
                .getByResourceGroupWithResponse("firewall-rg", "firewall1", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().apply();
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/Firewalls_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firewalls_Update_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void firewallsUpdateMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        FirewallResource resource =
            manager
                .firewalls()
                .getByResourceGroupWithResponse("firewall-rg", "firewall1", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("tagName", "value"))
            .withIdentity(
                new AzureResourceManagerManagedIdentityProperties()
                    .withType(ManagedIdentityType.NONE)
                    .withUserAssignedIdentities(
                        mapOf(
                            "key16",
                            new AzureResourceManagerUserAssignedIdentity()
                                .withClientId("aaaa")
                                .withPrincipalId("aaaaaaaaaaaaaaa"))))
            .withProperties(
                new FirewallResourceUpdateProperties()
                    .withPanEtag("2bf4a339-294d-4c25-b0b2-ef649e9f5c12")
                    .withNetworkProfile(
                        new NetworkProfile()
                            .withVnetConfiguration(
                                new VnetConfiguration()
                                    .withVnet(
                                        new IpAddressSpace()
                                            .withResourceId(
                                                "/subscriptions/2bf4a339-294d-4c25-b0b2-ef649e9f5c27/resourceGroups/os-liftr-integration/providers/Microsoft.Network/virtualNetworks/os-liftr-integration-vnet")
                                            .withAddressSpace("10.1.0.0/16"))
                                    .withTrustSubnet(
                                        new IpAddressSpace()
                                            .withResourceId(
                                                "/subscriptions/2bf4a339-294d-4c25-b0b2-ef649e9f5c27/resourceGroups/os-liftr-integration/providers/Microsoft.Network/virtualNetworks/os-liftr-integration-vnet/subnets/os-liftr-integration-trust-subnet")
                                            .withAddressSpace("10.1.1.0/24"))
                                    .withUnTrustSubnet(
                                        new IpAddressSpace()
                                            .withResourceId(
                                                "/subscriptions/2bf4a339-294d-4c25-b0b2-ef649e9f5c27/resourceGroups/os-liftr-integration/providers/Microsoft.Network/virtualNetworks/os-liftr-integration-vnet/subnets/os-liftr-integration-untrust-subnet")
                                            .withAddressSpace("10.1.1.0/24"))
                                    .withIpOfTrustSubnetForUdr(
                                        new IpAddress()
                                            .withResourceId(
                                                "/subscriptions/2bf4a339-294d-4c25-b0b2-ef649e9f5c27/resourceGroups/os-liftr-integration/providers/Microsoft.Network/virtualNetworks/os-liftr-integration-vnet/subnets/os-liftr-integration-untrust-subnet")
                                            .withAddress("10.1.1.0/24")))
                            .withVwanConfiguration(
                                new VwanConfiguration()
                                    .withNetworkVirtualApplianceId("2bf4a339-294d-4c25-b0b2-ef649e9f5c12")
                                    .withVHub(
                                        new IpAddressSpace()
                                            .withResourceId(
                                                "/subscriptions/2bf4a339-294d-4c25-b0b2-ef649e9f5c27/resourceGroups/os-liftr-integration/providers/Microsoft.Network/virtualNetworks/os-liftr-integration-vnet/subnets/os-liftr-integration-untrust-subnet")
                                            .withAddressSpace("10.1.1.0/24"))
                                    .withTrustSubnet(
                                        new IpAddressSpace()
                                            .withResourceId(
                                                "/subscriptions/2bf4a339-294d-4c25-b0b2-ef649e9f5c27/resourceGroups/os-liftr-integration/providers/Microsoft.Network/virtualNetworks/os-liftr-integration-vnet/subnets/os-liftr-integration-trust-subnet")
                                            .withAddressSpace("10.1.1.0/24"))
                                    .withUnTrustSubnet(
                                        new IpAddressSpace()
                                            .withResourceId(
                                                "/subscriptions/2bf4a339-294d-4c25-b0b2-ef649e9f5c27/resourceGroups/os-liftr-integration/providers/Microsoft.Network/virtualNetworks/os-liftr-integration-vnet/subnets/os-liftr-integration-untrust-subnet")
                                            .withAddressSpace("10.1.1.0/24"))
                                    .withIpOfTrustSubnetForUdr(
                                        new IpAddress()
                                            .withResourceId(
                                                "/subscriptions/2bf4a339-294d-4c25-b0b2-ef649e9f5c27/resourceGroups/os-liftr-integration/providers/Microsoft.Network/virtualNetworks/os-liftr-integration-vnet/subnets/os-liftr-integration-untrust-subnet")
                                            .withAddress("10.1.1.0/24")))
                            .withNetworkType(NetworkType.VNET)
                            .withPublicIps(
                                Arrays
                                    .asList(
                                        new IpAddress()
                                            .withResourceId(
                                                "/subscriptions/01c7d41f-afaf-464e-8a8b-5c6f9f98cee8/resourceGroups/mj-liftr-integration/providers/Microsoft.Network/publicIPAddresses/mj-liftr-integration-PublicIp1")
                                            .withAddress("20.22.92.11")))
                            .withEnableEgressNat(EgressNat.ENABLED)
                            .withEgressNatIp(
                                Arrays
                                    .asList(
                                        new IpAddress()
                                            .withResourceId(
                                                "/subscriptions/01c7d41f-afaf-464e-8a8b-5c6f9f98cee8/resourceGroups/mj-liftr-integration/providers/Microsoft.Network/publicIPAddresses/mj-liftr-integration-egressNatIp1")
                                            .withAddress("20.22.92.111"))))
                    .withIsPanoramaManaged(BooleanEnum.TRUE)
                    .withPanoramaConfig(new PanoramaConfig().withConfigString("bas64EncodedString"))
                    .withAssociatedRulestack(
                        new RulestackDetails()
                            .withResourceId("aaaaaaaaaa")
                            .withRulestackId("aaaaaaaaaaaaaaaa")
                            .withLocation("eastus"))
                    .withDnsSettings(
                        new DnsSettings()
                            .withEnableDnsProxy(DnsProxy.DISABLED)
                            .withEnabledDnsType(EnabledDnsType.CUSTOM)
                            .withDnsServers(
                                Arrays
                                    .asList(
                                        new IpAddress()
                                            .withResourceId(
                                                "/subscriptions/01c7d41f-afaf-464e-8a8b-5c6f9f98cee8/resourceGroups/mj-liftr-integration/providers/Microsoft.Network/publicIPAddresses/mj-liftr-integration-egressNatIp1")
                                            .withAddress("20.22.92.111"))))
                    .withFrontEndSettings(
                        Arrays
                            .asList(
                                new FrontendSetting()
                                    .withName("frontendsetting11")
                                    .withProtocol(ProtocolType.TCP)
                                    .withFrontendConfiguration(
                                        new EndpointConfiguration()
                                            .withPort("80")
                                            .withAddress(
                                                new IpAddress()
                                                    .withResourceId(
                                                        "/subscriptions/01c7d41f-afaf-464e-8a8b-5c6f9f98cee8/resourceGroups/mj-liftr-integration/providers/Microsoft.Network/publicIPAddresses/mj-liftr-integration-frontendSettingIp1")
                                                    .withAddress("20.22.91.251")))
                                    .withBackendConfiguration(
                                        new EndpointConfiguration()
                                            .withPort("80")
                                            .withAddress(
                                                new IpAddress()
                                                    .withResourceId(
                                                        "/subscriptions/01c7d41f-afaf-464e-8a8b-5c6f9f98cee8/resourceGroups/mj-liftr-integration/providers/Microsoft.Network/publicIPAddresses/mj-liftr-integration-frontendSettingIp2")
                                                    .withAddress("20.22.32.136")))))
                    .withPlanData(
                        new PlanData()
                            .withUsageType(UsageType.PAYG)
                            .withBillingCycle(BillingCycle.WEEKLY)
                            .withPlanId("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"))
                    .withMarketplaceDetails(
                        new MarketplaceDetails()
                            .withOfferId("aaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
                            .withPublisherId("aaaa")
                            .withMarketplaceSubscriptionStatus(
                                MarketplaceSubscriptionStatus.PENDING_FULFILLMENT_START)))
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

### FqdnListGlobalRulestack_CreateOrUpdate

```java
import com.azure.resourcemanager.paloaltonetworks.fluent.models.FqdnListGlobalRulestackResourceInner;
import java.util.Arrays;

/** Samples for FqdnListGlobalRulestack CreateOrUpdate. */
public final class FqdnListGlobalRulestackCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/FqdnListGlobalRulestack_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: FqdnListGlobalRulestack_CreateOrUpdate_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void fqdnListGlobalRulestackCreateOrUpdateMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .fqdnListGlobalRulestacks()
            .createOrUpdate(
                "praval",
                "armid1",
                new FqdnListGlobalRulestackResourceInner()
                    .withDescription("string")
                    .withFqdnList(Arrays.asList("string1", "string2"))
                    .withEtag("aaaaaaaaaaaaaaaaaa")
                    .withAuditComment("string"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/FqdnListGlobalRulestack_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: FqdnListGlobalRulestack_CreateOrUpdate_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void fqdnListGlobalRulestackCreateOrUpdateMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .fqdnListGlobalRulestacks()
            .createOrUpdate(
                "praval",
                "armid1",
                new FqdnListGlobalRulestackResourceInner().withFqdnList(Arrays.asList("string1", "string2")),
                com.azure.core.util.Context.NONE);
    }
}
```

### FqdnListGlobalRulestack_Delete

```java
/** Samples for FqdnListGlobalRulestack Delete. */
public final class FqdnListGlobalRulestackDeleteSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/FqdnListGlobalRulestack_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: FqdnListGlobalRulestack_Delete_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void fqdnListGlobalRulestackDeleteMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.fqdnListGlobalRulestacks().delete("praval", "armid1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/FqdnListGlobalRulestack_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: FqdnListGlobalRulestack_Delete_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void fqdnListGlobalRulestackDeleteMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.fqdnListGlobalRulestacks().delete("praval", "armid1", com.azure.core.util.Context.NONE);
    }
}
```

### FqdnListGlobalRulestack_Get

```java
/** Samples for FqdnListGlobalRulestack Get. */
public final class FqdnListGlobalRulestackGetSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/FqdnListGlobalRulestack_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: FqdnListGlobalRulestack_Get_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void fqdnListGlobalRulestackGetMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.fqdnListGlobalRulestacks().getWithResponse("praval", "armid1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/FqdnListGlobalRulestack_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: FqdnListGlobalRulestack_Get_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void fqdnListGlobalRulestackGetMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.fqdnListGlobalRulestacks().getWithResponse("praval", "armid1", com.azure.core.util.Context.NONE);
    }
}
```

### FqdnListGlobalRulestack_List

```java
/** Samples for FqdnListGlobalRulestack List. */
public final class FqdnListGlobalRulestackListSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/FqdnListGlobalRulestack_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: FqdnListGlobalRulestack_List_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void fqdnListGlobalRulestackListMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.fqdnListGlobalRulestacks().list("praval", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/FqdnListGlobalRulestack_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: FqdnListGlobalRulestack_List_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void fqdnListGlobalRulestackListMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.fqdnListGlobalRulestacks().list("praval", com.azure.core.util.Context.NONE);
    }
}
```

### FqdnListLocalRulestack_CreateOrUpdate

```java
import java.util.Arrays;

/** Samples for FqdnListLocalRulestack CreateOrUpdate. */
public final class FqdnListLocalRulestackCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/FqdnListLocalRulestack_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: FqdnListLocalRulestack_CreateOrUpdate_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void fqdnListLocalRulestackCreateOrUpdateMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .fqdnListLocalRulestacks()
            .define("armid1")
            .withExistingLocalRulestack("rgopenapi", "lrs1")
            .withFqdnList(Arrays.asList("string1", "string2"))
            .withDescription("string")
            .withEtag("aaaaaaaaaaaaaaaaaa")
            .withAuditComment("string")
            .create();
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/FqdnListLocalRulestack_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: FqdnListLocalRulestack_CreateOrUpdate_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void fqdnListLocalRulestackCreateOrUpdateMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .fqdnListLocalRulestacks()
            .define("armid1")
            .withExistingLocalRulestack("rgopenapi", "lrs1")
            .withFqdnList(Arrays.asList("string1", "string2"))
            .create();
    }
}
```

### FqdnListLocalRulestack_Delete

```java
/** Samples for FqdnListLocalRulestack Delete. */
public final class FqdnListLocalRulestackDeleteSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/FqdnListLocalRulestack_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: FqdnListLocalRulestack_Delete_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void fqdnListLocalRulestackDeleteMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.fqdnListLocalRulestacks().delete("rgopenapi", "lrs1", "armid1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/FqdnListLocalRulestack_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: FqdnListLocalRulestack_Delete_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void fqdnListLocalRulestackDeleteMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.fqdnListLocalRulestacks().delete("rgopenapi", "lrs1", "armid1", com.azure.core.util.Context.NONE);
    }
}
```

### FqdnListLocalRulestack_Get

```java
/** Samples for FqdnListLocalRulestack Get. */
public final class FqdnListLocalRulestackGetSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/FqdnListLocalRulestack_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: FqdnListLocalRulestack_Get_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void fqdnListLocalRulestackGetMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .fqdnListLocalRulestacks()
            .getWithResponse("rgopenapi", "lrs1", "armid1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/FqdnListLocalRulestack_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: FqdnListLocalRulestack_Get_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void fqdnListLocalRulestackGetMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .fqdnListLocalRulestacks()
            .getWithResponse("rgopenapi", "lrs1", "armid1", com.azure.core.util.Context.NONE);
    }
}
```

### FqdnListLocalRulestack_ListByLocalRulestacks

```java
/** Samples for FqdnListLocalRulestack ListByLocalRulestacks. */
public final class FqdnListLocalRulestackListByLocalRulestacksSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/FqdnListLocalRulestack_ListByLocalRulestacks_MinimumSet_Gen.json
     */
    /**
     * Sample code: FqdnListLocalRulestack_ListByLocalRulestacks_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void fqdnListLocalRulestackListByLocalRulestacksMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.fqdnListLocalRulestacks().listByLocalRulestacks("rgopenapi", "lrs1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/FqdnListLocalRulestack_ListByLocalRulestacks_MaximumSet_Gen.json
     */
    /**
     * Sample code: FqdnListLocalRulestack_ListByLocalRulestacks_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void fqdnListLocalRulestackListByLocalRulestacksMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.fqdnListLocalRulestacks().listByLocalRulestacks("rgopenapi", "lrs1", com.azure.core.util.Context.NONE);
    }
}
```

### GlobalRulestack_Commit

```java
/** Samples for GlobalRulestack Commit. */
public final class GlobalRulestackCommitSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/GlobalRulestack_commit_MaximumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_commit_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void globalRulestackCommitMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.globalRulestacks().commit("praval", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/GlobalRulestack_commit_MinimumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_commit_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void globalRulestackCommitMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.globalRulestacks().commit("praval", com.azure.core.util.Context.NONE);
    }
}
```

### GlobalRulestack_CreateOrUpdate

```java
import com.azure.resourcemanager.paloaltonetworks.fluent.models.GlobalRulestackResourceInner;
import com.azure.resourcemanager.paloaltonetworks.models.AzureResourceManagerManagedIdentityProperties;
import com.azure.resourcemanager.paloaltonetworks.models.AzureResourceManagerUserAssignedIdentity;
import com.azure.resourcemanager.paloaltonetworks.models.DefaultMode;
import com.azure.resourcemanager.paloaltonetworks.models.ManagedIdentityType;
import com.azure.resourcemanager.paloaltonetworks.models.ScopeType;
import com.azure.resourcemanager.paloaltonetworks.models.SecurityServices;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for GlobalRulestack CreateOrUpdate. */
public final class GlobalRulestackCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/GlobalRulestack_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_CreateOrUpdate_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void globalRulestackCreateOrUpdateMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .globalRulestacks()
            .createOrUpdate(
                "praval", new GlobalRulestackResourceInner().withLocation("eastus"), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/GlobalRulestack_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_CreateOrUpdate_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void globalRulestackCreateOrUpdateMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .globalRulestacks()
            .createOrUpdate(
                "praval",
                new GlobalRulestackResourceInner()
                    .withLocation("eastus")
                    .withIdentity(
                        new AzureResourceManagerManagedIdentityProperties()
                            .withType(ManagedIdentityType.NONE)
                            .withUserAssignedIdentities(
                                mapOf(
                                    "key16",
                                    new AzureResourceManagerUserAssignedIdentity()
                                        .withClientId("aaaa")
                                        .withPrincipalId("aaaaaaaaaaaaaaa"))))
                    .withPanEtag("2bf4a339-294d-4c25-b0b2-ef649e9f5c12")
                    .withPanLocation("eastus")
                    .withScope(ScopeType.GLOBAL)
                    .withAssociatedSubscriptions(Arrays.asList("2bf4a339-294d-4c25-b0b2-ef649e9f5c27"))
                    .withDescription("global rulestacks")
                    .withDefaultMode(DefaultMode.IPS)
                    .withMinAppIdVersion("8.5.3")
                    .withSecurityServices(
                        new SecurityServices()
                            .withVulnerabilityProfile("default")
                            .withAntiSpywareProfile("default")
                            .withAntiVirusProfile("default")
                            .withUrlFilteringProfile("default")
                            .withFileBlockingProfile("default")
                            .withDnsSubscription("default")
                            .withOutboundUnTrustCertificate("default")
                            .withOutboundTrustCertificate("default")),
                com.azure.core.util.Context.NONE);
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

### GlobalRulestack_Delete

```java
/** Samples for GlobalRulestack Delete. */
public final class GlobalRulestackDeleteSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/GlobalRulestack_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_Delete_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void globalRulestackDeleteMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.globalRulestacks().delete("praval", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/GlobalRulestack_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_Delete_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void globalRulestackDeleteMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.globalRulestacks().delete("praval", com.azure.core.util.Context.NONE);
    }
}
```

### GlobalRulestack_Get

```java
/** Samples for GlobalRulestack Get. */
public final class GlobalRulestackGetSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/GlobalRulestack_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_Get_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void globalRulestackGetMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.globalRulestacks().getWithResponse("praval", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/GlobalRulestack_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_Get_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void globalRulestackGetMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.globalRulestacks().getWithResponse("praval", com.azure.core.util.Context.NONE);
    }
}
```

### GlobalRulestack_GetChangeLog

```java
/** Samples for GlobalRulestack GetChangeLog. */
public final class GlobalRulestackGetChangeLogSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/GlobalRulestack_getChangeLog_MinimumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_getChangeLog_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void globalRulestackGetChangeLogMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.globalRulestacks().getChangeLogWithResponse("praval", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/GlobalRulestack_getChangeLog_MaximumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_getChangeLog_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void globalRulestackGetChangeLogMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.globalRulestacks().getChangeLogWithResponse("praval", com.azure.core.util.Context.NONE);
    }
}
```

### GlobalRulestack_List

```java
/** Samples for GlobalRulestack List. */
public final class GlobalRulestackListSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/GlobalRulestack_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_List_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void globalRulestackListMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.globalRulestacks().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/GlobalRulestack_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_List_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void globalRulestackListMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.globalRulestacks().list(com.azure.core.util.Context.NONE);
    }
}
```

### GlobalRulestack_ListAdvancedSecurityObjects

```java
import com.azure.resourcemanager.paloaltonetworks.models.AdvSecurityObjectTypeEnum;

/** Samples for GlobalRulestack ListAdvancedSecurityObjects. */
public final class GlobalRulestackListAdvancedSecurityObjectsSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/GlobalRulestack_listAdvancedSecurityObjects_MaximumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_listAdvancedSecurityObjects_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void globalRulestackListAdvancedSecurityObjectsMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .globalRulestacks()
            .listAdvancedSecurityObjectsWithResponse(
                "praval",
                AdvSecurityObjectTypeEnum.fromString("globalRulestacks"),
                "a6a321",
                20,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/GlobalRulestack_listAdvancedSecurityObjects_MinimumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_listAdvancedSecurityObjects_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void globalRulestackListAdvancedSecurityObjectsMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .globalRulestacks()
            .listAdvancedSecurityObjectsWithResponse(
                "praval",
                AdvSecurityObjectTypeEnum.fromString("globalRulestacks"),
                null,
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### GlobalRulestack_ListAppIds

```java
/** Samples for GlobalRulestack ListAppIds. */
public final class GlobalRulestackListAppIdsSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/GlobalRulestack_listAppIds_MaximumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_listAppIds_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void globalRulestackListAppIdsMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .globalRulestacks()
            .listAppIdsWithResponse("praval", "8543", "pref", "a6a321", 20, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/GlobalRulestack_listAppIds_MinimumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_listAppIds_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void globalRulestackListAppIdsMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .globalRulestacks()
            .listAppIdsWithResponse("praval", null, null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### GlobalRulestack_ListCountries

```java
/** Samples for GlobalRulestack ListCountries. */
public final class GlobalRulestackListCountriesSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/GlobalRulestack_listCountries_MinimumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_listCountries_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void globalRulestackListCountriesMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.globalRulestacks().listCountriesWithResponse("praval", null, null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/GlobalRulestack_listCountries_MaximumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_listCountries_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void globalRulestackListCountriesMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.globalRulestacks().listCountriesWithResponse("praval", "a6a321", 20, com.azure.core.util.Context.NONE);
    }
}
```

### GlobalRulestack_ListFirewalls

```java
/** Samples for GlobalRulestack ListFirewalls. */
public final class GlobalRulestackListFirewallsSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/GlobalRulestack_listFirewalls_MaximumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_listFirewalls_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void globalRulestackListFirewallsMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.globalRulestacks().listFirewallsWithResponse("praval", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/GlobalRulestack_listFirewalls_MinimumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_listFirewalls_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void globalRulestackListFirewallsMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.globalRulestacks().listFirewallsWithResponse("praval", com.azure.core.util.Context.NONE);
    }
}
```

### GlobalRulestack_ListPredefinedUrlCategories

```java
/** Samples for GlobalRulestack ListPredefinedUrlCategories. */
public final class GlobalRulestackListPredefinedUrlCategoriesSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/GlobalRulestack_listPredefinedUrlCategories_MaximumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_listPredefinedUrlCategories_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void globalRulestackListPredefinedUrlCategoriesMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .globalRulestacks()
            .listPredefinedUrlCategoriesWithResponse("praval", "a6a321", 20, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/GlobalRulestack_listPredefinedUrlCategories_MinimumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_listPredefinedUrlCategories_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void globalRulestackListPredefinedUrlCategoriesMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .globalRulestacks()
            .listPredefinedUrlCategoriesWithResponse("praval", null, null, com.azure.core.util.Context.NONE);
    }
}
```

### GlobalRulestack_ListSecurityServices

```java
import com.azure.resourcemanager.paloaltonetworks.models.SecurityServicesTypeEnum;

/** Samples for GlobalRulestack ListSecurityServices. */
public final class GlobalRulestackListSecurityServicesSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/GlobalRulestack_listSecurityServices_MinimumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_listSecurityServices_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void globalRulestackListSecurityServicesMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .globalRulestacks()
            .listSecurityServicesWithResponse(
                "praval",
                SecurityServicesTypeEnum.fromString("globalRulestacks"),
                null,
                null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/GlobalRulestack_listSecurityServices_MaximumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_listSecurityServices_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void globalRulestackListSecurityServicesMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .globalRulestacks()
            .listSecurityServicesWithResponse(
                "praval",
                SecurityServicesTypeEnum.fromString("globalRulestacks"),
                "a6a321",
                20,
                com.azure.core.util.Context.NONE);
    }
}
```

### GlobalRulestack_Revert

```java
/** Samples for GlobalRulestack Revert. */
public final class GlobalRulestackRevertSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/GlobalRulestack_revert_MinimumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_revert_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void globalRulestackRevertMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.globalRulestacks().revertWithResponse("praval", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/GlobalRulestack_revert_MaximumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_revert_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void globalRulestackRevertMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.globalRulestacks().revertWithResponse("praval", com.azure.core.util.Context.NONE);
    }
}
```

### GlobalRulestack_Update

```java
import com.azure.resourcemanager.paloaltonetworks.models.AzureResourceManagerManagedIdentityProperties;
import com.azure.resourcemanager.paloaltonetworks.models.AzureResourceManagerUserAssignedIdentity;
import com.azure.resourcemanager.paloaltonetworks.models.DefaultMode;
import com.azure.resourcemanager.paloaltonetworks.models.GlobalRulestackResourceUpdate;
import com.azure.resourcemanager.paloaltonetworks.models.GlobalRulestackResourceUpdateProperties;
import com.azure.resourcemanager.paloaltonetworks.models.ManagedIdentityType;
import com.azure.resourcemanager.paloaltonetworks.models.ScopeType;
import com.azure.resourcemanager.paloaltonetworks.models.SecurityServices;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for GlobalRulestack Update. */
public final class GlobalRulestackUpdateSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/GlobalRulestack_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_Update_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void globalRulestackUpdateMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .globalRulestacks()
            .updateWithResponse(
                "praval",
                new GlobalRulestackResourceUpdate()
                    .withLocation("eastus")
                    .withIdentity(
                        new AzureResourceManagerManagedIdentityProperties()
                            .withType(ManagedIdentityType.NONE)
                            .withUserAssignedIdentities(
                                mapOf(
                                    "key16",
                                    new AzureResourceManagerUserAssignedIdentity()
                                        .withClientId("aaaa")
                                        .withPrincipalId("aaaaaaaaaaaaaaa"))))
                    .withProperties(
                        new GlobalRulestackResourceUpdateProperties()
                            .withPanEtag("2bf4a339-294d-4c25-b0b2-ef649e9f5c12")
                            .withPanLocation("eastus")
                            .withScope(ScopeType.GLOBAL)
                            .withAssociatedSubscriptions(Arrays.asList("2bf4a339-294d-4c25-b0b2-ef649e9f5c27"))
                            .withDescription("global rulestacks")
                            .withDefaultMode(DefaultMode.IPS)
                            .withMinAppIdVersion("8.5.3")
                            .withSecurityServices(
                                new SecurityServices()
                                    .withVulnerabilityProfile("default")
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
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/GlobalRulestack_Update_MinimumSet_Gen.json
     */
    /**
     * Sample code: GlobalRulestack_Update_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void globalRulestackUpdateMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .globalRulestacks()
            .updateWithResponse("praval", new GlobalRulestackResourceUpdate(), com.azure.core.util.Context.NONE);
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

### LocalRules_CreateOrUpdate

```java
import com.azure.resourcemanager.paloaltonetworks.models.ActionEnum;
import com.azure.resourcemanager.paloaltonetworks.models.BooleanEnum;
import com.azure.resourcemanager.paloaltonetworks.models.Category;
import com.azure.resourcemanager.paloaltonetworks.models.DecryptionRuleTypeEnum;
import com.azure.resourcemanager.paloaltonetworks.models.DestinationAddr;
import com.azure.resourcemanager.paloaltonetworks.models.SourceAddr;
import com.azure.resourcemanager.paloaltonetworks.models.StateEnum;
import com.azure.resourcemanager.paloaltonetworks.models.TagInfo;
import java.util.Arrays;

/** Samples for LocalRules CreateOrUpdate. */
public final class LocalRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRules_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRules_CreateOrUpdate_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulesCreateOrUpdateMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .localRules()
            .define("1")
            .withExistingLocalRulestack("firewall-rg", "lrs1")
            .withRuleName("localRule1")
            .create();
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRules_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRules_CreateOrUpdate_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulesCreateOrUpdateMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .localRules()
            .define("1")
            .withExistingLocalRulestack("firewall-rg", "lrs1")
            .withRuleName("localRule1")
            .withTags(Arrays.asList(new TagInfo().withKey("fakeTokenPlaceholder").withValue("value")))
            .withEtag("c18e6eef-ba3e-49ee-8a85-2b36c863a9d0")
            .withDescription("description of local rule")
            .withRuleState(StateEnum.DISABLED)
            .withSource(
                new SourceAddr()
                    .withCidrs(Arrays.asList("1.0.0.1/10"))
                    .withCountries(Arrays.asList("India"))
                    .withFeeds(Arrays.asList("feed"))
                    .withPrefixLists(Arrays.asList("PL1")))
            .withNegateSource(BooleanEnum.TRUE)
            .withDestination(
                new DestinationAddr()
                    .withCidrs(Arrays.asList("1.0.0.1/10"))
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
/** Samples for LocalRules Delete. */
public final class LocalRulesDeleteSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRules_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRules_Delete_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulesDeleteMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.localRules().delete("firewall-rg", "lrs1", "1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRules_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRules_Delete_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulesDeleteMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.localRules().delete("firewall-rg", "lrs1", "1", com.azure.core.util.Context.NONE);
    }
}
```

### LocalRules_Get

```java
/** Samples for LocalRules Get. */
public final class LocalRulesGetSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRules_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRules_Get_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulesGetMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.localRules().getWithResponse("firewall-rg", "lrs1", "1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRules_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRules_Get_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulesGetMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.localRules().getWithResponse("firewall-rg", "lrs1", "1", com.azure.core.util.Context.NONE);
    }
}
```

### LocalRules_GetCounters

```java
/** Samples for LocalRules GetCounters. */
public final class LocalRulesGetCountersSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRules_getCounters_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRules_getCounters_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulesGetCountersMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .localRules()
            .getCountersWithResponse("firewall-rg", "lrs1", "1", "firewall1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRules_getCounters_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRules_getCounters_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulesGetCountersMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .localRules()
            .getCountersWithResponse("firewall-rg", "lrs1", "1", null, com.azure.core.util.Context.NONE);
    }
}
```

### LocalRules_ListByLocalRulestacks

```java
/** Samples for LocalRules ListByLocalRulestacks. */
public final class LocalRulesListByLocalRulestacksSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRules_ListByLocalRulestacks_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRules_ListByLocalRulestacks_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulesListByLocalRulestacksMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.localRules().listByLocalRulestacks("firewall-rg", "lrs1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRules_ListByLocalRulestacks_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRules_ListByLocalRulestacks_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulesListByLocalRulestacksMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.localRules().listByLocalRulestacks("firewall-rg", "lrs1", com.azure.core.util.Context.NONE);
    }
}
```

### LocalRules_RefreshCounters

```java
/** Samples for LocalRules RefreshCounters. */
public final class LocalRulesRefreshCountersSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRules_refreshCounters_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRules_refreshCounters_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulesRefreshCountersMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .localRules()
            .refreshCountersWithResponse("firewall-rg", "lrs1", "1", null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRules_refreshCounters_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRules_refreshCounters_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulesRefreshCountersMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .localRules()
            .refreshCountersWithResponse("firewall-rg", "lrs1", "1", "firewall1", com.azure.core.util.Context.NONE);
    }
}
```

### LocalRules_ResetCounters

```java
/** Samples for LocalRules ResetCounters. */
public final class LocalRulesResetCountersSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRules_resetCounters_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRules_resetCounters_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulesResetCountersMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .localRules()
            .resetCountersWithResponse("firewall-rg", "lrs1", "1", "firewall1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRules_resetCounters_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRules_resetCounters_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulesResetCountersMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .localRules()
            .resetCountersWithResponse("firewall-rg", "lrs1", "1", null, com.azure.core.util.Context.NONE);
    }
}
```

### LocalRulestacks_Commit

```java
/** Samples for LocalRulestacks Commit. */
public final class LocalRulestacksCommitSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRulestacks_commit_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_commit_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulestacksCommitMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.localRulestacks().commit("rgopenapi", "lrs1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRulestacks_commit_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_commit_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulestacksCommitMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.localRulestacks().commit("rgopenapi", "lrs1", com.azure.core.util.Context.NONE);
    }
}
```

### LocalRulestacks_CreateOrUpdate

```java
import com.azure.resourcemanager.paloaltonetworks.models.AzureResourceManagerManagedIdentityProperties;
import com.azure.resourcemanager.paloaltonetworks.models.AzureResourceManagerUserAssignedIdentity;
import com.azure.resourcemanager.paloaltonetworks.models.DefaultMode;
import com.azure.resourcemanager.paloaltonetworks.models.ManagedIdentityType;
import com.azure.resourcemanager.paloaltonetworks.models.ScopeType;
import com.azure.resourcemanager.paloaltonetworks.models.SecurityServices;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for LocalRulestacks CreateOrUpdate. */
public final class LocalRulestacksCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRulestacks_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_CreateOrUpdate_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulestacksCreateOrUpdateMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.localRulestacks().define("lrs1").withRegion("eastus").withExistingResourceGroup("rgopenapi").create();
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRulestacks_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_CreateOrUpdate_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulestacksCreateOrUpdateMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .localRulestacks()
            .define("lrs1")
            .withRegion("eastus")
            .withExistingResourceGroup("rgopenapi")
            .withTags(mapOf("tagName", "value"))
            .withIdentity(
                new AzureResourceManagerManagedIdentityProperties()
                    .withType(ManagedIdentityType.NONE)
                    .withUserAssignedIdentities(
                        mapOf(
                            "key16",
                            new AzureResourceManagerUserAssignedIdentity()
                                .withClientId("aaaa")
                                .withPrincipalId("aaaaaaaaaaaaaaa"))))
            .withPanEtag("2bf4a339-294d-4c25-b0b2-ef649e9f5c12")
            .withPanLocation("eastus")
            .withScope(ScopeType.LOCAL)
            .withAssociatedSubscriptions(Arrays.asList("2bf4a339-294d-4c25-b0b2-ef649e9f5c27"))
            .withDescription("local rulestacks")
            .withDefaultMode(DefaultMode.IPS)
            .withMinAppIdVersion("8.5.3")
            .withSecurityServices(
                new SecurityServices()
                    .withVulnerabilityProfile("default")
                    .withAntiSpywareProfile("default")
                    .withAntiVirusProfile("default")
                    .withUrlFilteringProfile("default")
                    .withFileBlockingProfile("default")
                    .withDnsSubscription("default")
                    .withOutboundUnTrustCertificate("default")
                    .withOutboundTrustCertificate("default"))
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

### LocalRulestacks_Delete

```java
/** Samples for LocalRulestacks Delete. */
public final class LocalRulestacksDeleteSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRulestacks_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_Delete_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulestacksDeleteMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.localRulestacks().delete("rgopenapi", "lrs1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRulestacks_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_Delete_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulestacksDeleteMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.localRulestacks().delete("rgopenapi", "lrs1", com.azure.core.util.Context.NONE);
    }
}
```

### LocalRulestacks_GetByResourceGroup

```java
/** Samples for LocalRulestacks GetByResourceGroup. */
public final class LocalRulestacksGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRulestacks_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_Get_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulestacksGetMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.localRulestacks().getByResourceGroupWithResponse("rgopenapi", "lrs1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRulestacks_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_Get_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulestacksGetMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.localRulestacks().getByResourceGroupWithResponse("rgopenapi", "lrs1", com.azure.core.util.Context.NONE);
    }
}
```

### LocalRulestacks_GetChangeLog

```java
/** Samples for LocalRulestacks GetChangeLog. */
public final class LocalRulestacksGetChangeLogSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRulestacks_getChangeLog_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_getChangeLog_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulestacksGetChangeLogMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.localRulestacks().getChangeLogWithResponse("rgopenapi", "lrs1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRulestacks_getChangeLog_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_getChangeLog_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulestacksGetChangeLogMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.localRulestacks().getChangeLogWithResponse("rgopenapi", "lrs1", com.azure.core.util.Context.NONE);
    }
}
```

### LocalRulestacks_GetSupportInfo

```java
/** Samples for LocalRulestacks GetSupportInfo. */
public final class LocalRulestacksGetSupportInfoSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRulestacks_getSupportInfo_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_getSupportInfo_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulestacksGetSupportInfoMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .localRulestacks()
            .getSupportInfoWithResponse("rgopenapi", "lrs1", null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRulestacks_getSupportInfo_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_getSupportInfo_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulestacksGetSupportInfoMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .localRulestacks()
            .getSupportInfoWithResponse("rgopenapi", "lrs1", "user1@domain.com", com.azure.core.util.Context.NONE);
    }
}
```

### LocalRulestacks_List

```java
/** Samples for LocalRulestacks List. */
public final class LocalRulestacksListSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRulestacks_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_ListBySubscription_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulestacksListBySubscriptionMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.localRulestacks().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRulestacks_ListBySubscription_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_ListBySubscription_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulestacksListBySubscriptionMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.localRulestacks().list(com.azure.core.util.Context.NONE);
    }
}
```

### LocalRulestacks_ListAdvancedSecurityObjects

```java
import com.azure.resourcemanager.paloaltonetworks.models.AdvSecurityObjectTypeEnum;

/** Samples for LocalRulestacks ListAdvancedSecurityObjects. */
public final class LocalRulestacksListAdvancedSecurityObjectsSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRulestacks_listAdvancedSecurityObjects_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_listAdvancedSecurityObjects_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulestacksListAdvancedSecurityObjectsMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .localRulestacks()
            .listAdvancedSecurityObjectsWithResponse(
                "rgopenapi",
                "lrs1",
                AdvSecurityObjectTypeEnum.fromString("localRulestacks"),
                "a6a321",
                20,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRulestacks_listAdvancedSecurityObjects_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_listAdvancedSecurityObjects_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulestacksListAdvancedSecurityObjectsMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .localRulestacks()
            .listAdvancedSecurityObjectsWithResponse(
                "rgopenapi",
                "lrs1",
                AdvSecurityObjectTypeEnum.fromString("localRulestacks"),
                null,
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### LocalRulestacks_ListAppIds

```java
/** Samples for LocalRulestacks ListAppIds. */
public final class LocalRulestacksListAppIdsSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRulestacks_listAppIds_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_listAppIds_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulestacksListAppIdsMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .localRulestacks()
            .listAppIdsWithResponse(
                "rgopenapi", "lrs1", "8543", "pref", "a6a321", 20, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRulestacks_listAppIds_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_listAppIds_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulestacksListAppIdsMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .localRulestacks()
            .listAppIdsWithResponse("rgopenapi", "lrs1", null, null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### LocalRulestacks_ListByResourceGroup

```java
/** Samples for LocalRulestacks ListByResourceGroup. */
public final class LocalRulestacksListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRulestacks_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_ListByResourceGroup_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulestacksListByResourceGroupMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.localRulestacks().listByResourceGroup("rgopenapi", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRulestacks_ListByResourceGroup_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_ListByResourceGroup_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulestacksListByResourceGroupMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.localRulestacks().listByResourceGroup("rgopenapi", com.azure.core.util.Context.NONE);
    }
}
```

### LocalRulestacks_ListCountries

```java
/** Samples for LocalRulestacks ListCountries. */
public final class LocalRulestacksListCountriesSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRulestacks_listCountries_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_listCountries_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulestacksListCountriesMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .localRulestacks()
            .listCountriesWithResponse("rgopenapi", "lrs1", "a6a321", 20, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRulestacks_listCountries_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_listCountries_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulestacksListCountriesMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .localRulestacks()
            .listCountriesWithResponse("rgopenapi", "lrs1", null, null, com.azure.core.util.Context.NONE);
    }
}
```

### LocalRulestacks_ListFirewalls

```java
/** Samples for LocalRulestacks ListFirewalls. */
public final class LocalRulestacksListFirewallsSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRulestacks_listFirewalls_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_listFirewalls_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulestacksListFirewallsMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.localRulestacks().listFirewallsWithResponse("rgopenapi", "lrs1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRulestacks_listFirewalls_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_listFirewalls_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulestacksListFirewallsMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.localRulestacks().listFirewallsWithResponse("rgopenapi", "lrs1", com.azure.core.util.Context.NONE);
    }
}
```

### LocalRulestacks_ListPredefinedUrlCategories

```java
/** Samples for LocalRulestacks ListPredefinedUrlCategories. */
public final class LocalRulestacksListPredefinedUrlCategoriesSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRulestacks_listPredefinedUrlCategories_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_listPredefinedUrlCategories_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulestacksListPredefinedUrlCategoriesMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .localRulestacks()
            .listPredefinedUrlCategoriesWithResponse("rgopenapi", "lrs1", null, null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRulestacks_listPredefinedUrlCategories_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_listPredefinedUrlCategories_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulestacksListPredefinedUrlCategoriesMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .localRulestacks()
            .listPredefinedUrlCategoriesWithResponse(
                "rgopenapi", "lrs1", "a6a321", 20, com.azure.core.util.Context.NONE);
    }
}
```

### LocalRulestacks_ListSecurityServices

```java
import com.azure.resourcemanager.paloaltonetworks.models.SecurityServicesTypeEnum;

/** Samples for LocalRulestacks ListSecurityServices. */
public final class LocalRulestacksListSecurityServicesSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRulestacks_listSecurityServices_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_listSecurityServices_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulestacksListSecurityServicesMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .localRulestacks()
            .listSecurityServicesWithResponse(
                "rgopenapi",
                "lrs1",
                SecurityServicesTypeEnum.fromString("localRulestacks"),
                null,
                null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRulestacks_listSecurityServices_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_listSecurityServices_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulestacksListSecurityServicesMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .localRulestacks()
            .listSecurityServicesWithResponse(
                "rgopenapi",
                "lrs1",
                SecurityServicesTypeEnum.fromString("localRulestacks"),
                "a6a321",
                20,
                com.azure.core.util.Context.NONE);
    }
}
```

### LocalRulestacks_Revert

```java
/** Samples for LocalRulestacks Revert. */
public final class LocalRulestacksRevertSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRulestacks_revert_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_revert_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulestacksRevertMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.localRulestacks().revertWithResponse("rgopenapi", "lrs1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRulestacks_revert_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_revert_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulestacksRevertMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.localRulestacks().revertWithResponse("rgopenapi", "lrs1", com.azure.core.util.Context.NONE);
    }
}
```

### LocalRulestacks_Update

```java
import com.azure.resourcemanager.paloaltonetworks.models.AzureResourceManagerManagedIdentityProperties;
import com.azure.resourcemanager.paloaltonetworks.models.AzureResourceManagerUserAssignedIdentity;
import com.azure.resourcemanager.paloaltonetworks.models.DefaultMode;
import com.azure.resourcemanager.paloaltonetworks.models.LocalRulestackResource;
import com.azure.resourcemanager.paloaltonetworks.models.LocalRulestackResourceUpdateProperties;
import com.azure.resourcemanager.paloaltonetworks.models.ManagedIdentityType;
import com.azure.resourcemanager.paloaltonetworks.models.ScopeType;
import com.azure.resourcemanager.paloaltonetworks.models.SecurityServices;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for LocalRulestacks Update. */
public final class LocalRulestacksUpdateSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRulestacks_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_Update_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulestacksUpdateMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        LocalRulestackResource resource =
            manager
                .localRulestacks()
                .getByResourceGroupWithResponse("rgopenapi", "lrs1", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("tagName", "value"))
            .withIdentity(
                new AzureResourceManagerManagedIdentityProperties()
                    .withType(ManagedIdentityType.NONE)
                    .withUserAssignedIdentities(
                        mapOf(
                            "key16",
                            new AzureResourceManagerUserAssignedIdentity()
                                .withClientId("aaaa")
                                .withPrincipalId("aaaaaaaaaaaaaaa"))))
            .withProperties(
                new LocalRulestackResourceUpdateProperties()
                    .withPanEtag("2bf4a339-294d-4c25-b0b2-ef649e9f5c12")
                    .withPanLocation("eastus")
                    .withScope(ScopeType.LOCAL)
                    .withAssociatedSubscriptions(Arrays.asList("2bf4a339-294d-4c25-b0b2-ef649e9f5c27"))
                    .withDescription("local rulestacks")
                    .withDefaultMode(DefaultMode.IPS)
                    .withMinAppIdVersion("8.5.3")
                    .withSecurityServices(
                        new SecurityServices()
                            .withVulnerabilityProfile("default")
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
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/LocalRulestacks_Update_MinimumSet_Gen.json
     */
    /**
     * Sample code: LocalRulestacks_Update_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void localRulestacksUpdateMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        LocalRulestackResource resource =
            manager
                .localRulestacks()
                .getByResourceGroupWithResponse("rgopenapi", "lrs1", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().apply();
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

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/Operations_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void operationsListMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/Operations_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void operationsListMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### PostRules_CreateOrUpdate

```java
import com.azure.resourcemanager.paloaltonetworks.fluent.models.PostRulesResourceInner;
import com.azure.resourcemanager.paloaltonetworks.models.ActionEnum;
import com.azure.resourcemanager.paloaltonetworks.models.BooleanEnum;
import com.azure.resourcemanager.paloaltonetworks.models.Category;
import com.azure.resourcemanager.paloaltonetworks.models.DecryptionRuleTypeEnum;
import com.azure.resourcemanager.paloaltonetworks.models.DestinationAddr;
import com.azure.resourcemanager.paloaltonetworks.models.SourceAddr;
import com.azure.resourcemanager.paloaltonetworks.models.StateEnum;
import com.azure.resourcemanager.paloaltonetworks.models.TagInfo;
import java.util.Arrays;

/** Samples for PostRules CreateOrUpdate. */
public final class PostRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PostRules_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: PostRules_CreateOrUpdate_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void postRulesCreateOrUpdateMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .postRules()
            .createOrUpdate(
                "lrs1",
                "1",
                new PostRulesResourceInner()
                    .withEtag("c18e6eef-ba3e-49ee-8a85-2b36c863a9d0")
                    .withRuleName("postRule1")
                    .withDescription("description of post rule")
                    .withRuleState(StateEnum.DISABLED)
                    .withSource(
                        new SourceAddr()
                            .withCidrs(Arrays.asList("1.0.0.1/10"))
                            .withCountries(Arrays.asList("India"))
                            .withFeeds(Arrays.asList("feed"))
                            .withPrefixLists(Arrays.asList("PL1")))
                    .withNegateSource(BooleanEnum.TRUE)
                    .withDestination(
                        new DestinationAddr()
                            .withCidrs(Arrays.asList("1.0.0.1/10"))
                            .withCountries(Arrays.asList("India"))
                            .withFeeds(Arrays.asList("feed"))
                            .withPrefixLists(Arrays.asList("PL1"))
                            .withFqdnLists(Arrays.asList("FQDN1")))
                    .withNegateDestination(BooleanEnum.TRUE)
                    .withApplications(Arrays.asList("app1"))
                    .withCategory(
                        new Category()
                            .withUrlCustom(Arrays.asList("https://microsoft.com"))
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
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PostRules_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: PostRules_CreateOrUpdate_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void postRulesCreateOrUpdateMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .postRules()
            .createOrUpdate(
                "lrs1", "1", new PostRulesResourceInner().withRuleName("postRule1"), com.azure.core.util.Context.NONE);
    }
}
```

### PostRules_Delete

```java
/** Samples for PostRules Delete. */
public final class PostRulesDeleteSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PostRules_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: PostRules_Delete_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void postRulesDeleteMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.postRules().delete("lrs1", "1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PostRules_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: PostRules_Delete_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void postRulesDeleteMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.postRules().delete("lrs1", "1", com.azure.core.util.Context.NONE);
    }
}
```

### PostRules_Get

```java
/** Samples for PostRules Get. */
public final class PostRulesGetSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PostRules_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: PostRules_Get_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void postRulesGetMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.postRules().getWithResponse("lrs1", "1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PostRules_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: PostRules_Get_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void postRulesGetMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.postRules().getWithResponse("lrs1", "1", com.azure.core.util.Context.NONE);
    }
}
```

### PostRules_GetCounters

```java
/** Samples for PostRules GetCounters. */
public final class PostRulesGetCountersSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PostRules_getCounters_MinimumSet_Gen.json
     */
    /**
     * Sample code: PostRules_getCounters_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void postRulesGetCountersMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.postRules().getCountersWithResponse("lrs1", "1", null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PostRules_getCounters_MaximumSet_Gen.json
     */
    /**
     * Sample code: PostRules_getCounters_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void postRulesGetCountersMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.postRules().getCountersWithResponse("lrs1", "1", "firewall1", com.azure.core.util.Context.NONE);
    }
}
```

### PostRules_List

```java
/** Samples for PostRules List. */
public final class PostRulesListSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PostRules_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: PostRules_List_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void postRulesListMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.postRules().list("lrs1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PostRules_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: PostRules_List_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void postRulesListMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.postRules().list("lrs1", com.azure.core.util.Context.NONE);
    }
}
```

### PostRules_RefreshCounters

```java
/** Samples for PostRules RefreshCounters. */
public final class PostRulesRefreshCountersSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PostRules_refreshCounters_MaximumSet_Gen.json
     */
    /**
     * Sample code: PostRules_refreshCounters_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void postRulesRefreshCountersMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.postRules().refreshCountersWithResponse("lrs1", "1", "firewall1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PostRules_refreshCounters_MinimumSet_Gen.json
     */
    /**
     * Sample code: PostRules_refreshCounters_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void postRulesRefreshCountersMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.postRules().refreshCountersWithResponse("lrs1", "1", null, com.azure.core.util.Context.NONE);
    }
}
```

### PostRules_ResetCounters

```java
/** Samples for PostRules ResetCounters. */
public final class PostRulesResetCountersSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PostRules_resetCounters_MaximumSet_Gen.json
     */
    /**
     * Sample code: PostRules_resetCounters_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void postRulesResetCountersMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.postRules().resetCountersWithResponse("lrs1", "1", "firewall1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PostRules_resetCounters_MinimumSet_Gen.json
     */
    /**
     * Sample code: PostRules_resetCounters_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void postRulesResetCountersMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.postRules().resetCountersWithResponse("lrs1", "1", null, com.azure.core.util.Context.NONE);
    }
}
```

### PreRules_CreateOrUpdate

```java
import com.azure.resourcemanager.paloaltonetworks.fluent.models.PreRulesResourceInner;
import com.azure.resourcemanager.paloaltonetworks.models.ActionEnum;
import com.azure.resourcemanager.paloaltonetworks.models.BooleanEnum;
import com.azure.resourcemanager.paloaltonetworks.models.Category;
import com.azure.resourcemanager.paloaltonetworks.models.DecryptionRuleTypeEnum;
import com.azure.resourcemanager.paloaltonetworks.models.DestinationAddr;
import com.azure.resourcemanager.paloaltonetworks.models.SourceAddr;
import com.azure.resourcemanager.paloaltonetworks.models.StateEnum;
import com.azure.resourcemanager.paloaltonetworks.models.TagInfo;
import java.util.Arrays;

/** Samples for PreRules CreateOrUpdate. */
public final class PreRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PreRules_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: PreRules_CreateOrUpdate_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void preRulesCreateOrUpdateMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .preRules()
            .createOrUpdate(
                "lrs1",
                "1",
                new PreRulesResourceInner()
                    .withEtag("c18e6eef-ba3e-49ee-8a85-2b36c863a9d0")
                    .withRuleName("preRule1")
                    .withDescription("description of pre rule")
                    .withRuleState(StateEnum.DISABLED)
                    .withSource(
                        new SourceAddr()
                            .withCidrs(Arrays.asList("1.0.0.1/10"))
                            .withCountries(Arrays.asList("India"))
                            .withFeeds(Arrays.asList("feed"))
                            .withPrefixLists(Arrays.asList("PL1")))
                    .withNegateSource(BooleanEnum.TRUE)
                    .withDestination(
                        new DestinationAddr()
                            .withCidrs(Arrays.asList("1.0.0.1/10"))
                            .withCountries(Arrays.asList("India"))
                            .withFeeds(Arrays.asList("feed"))
                            .withPrefixLists(Arrays.asList("PL1"))
                            .withFqdnLists(Arrays.asList("FQDN1")))
                    .withNegateDestination(BooleanEnum.TRUE)
                    .withApplications(Arrays.asList("app1"))
                    .withCategory(
                        new Category()
                            .withUrlCustom(Arrays.asList("https://microsoft.com"))
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
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PreRules_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: PreRules_CreateOrUpdate_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void preRulesCreateOrUpdateMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .preRules()
            .createOrUpdate(
                "lrs1", "1", new PreRulesResourceInner().withRuleName("preRule1"), com.azure.core.util.Context.NONE);
    }
}
```

### PreRules_Delete

```java
/** Samples for PreRules Delete. */
public final class PreRulesDeleteSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PreRules_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: PreRules_Delete_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void preRulesDeleteMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.preRules().delete("lrs1", "1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PreRules_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: PreRules_Delete_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void preRulesDeleteMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.preRules().delete("lrs1", "1", com.azure.core.util.Context.NONE);
    }
}
```

### PreRules_Get

```java
/** Samples for PreRules Get. */
public final class PreRulesGetSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PreRules_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: PreRules_Get_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void preRulesGetMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.preRules().getWithResponse("lrs1", "1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PreRules_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: PreRules_Get_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void preRulesGetMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.preRules().getWithResponse("lrs1", "1", com.azure.core.util.Context.NONE);
    }
}
```

### PreRules_GetCounters

```java
/** Samples for PreRules GetCounters. */
public final class PreRulesGetCountersSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PreRules_getCounters_MinimumSet_Gen.json
     */
    /**
     * Sample code: PreRules_getCounters_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void preRulesGetCountersMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.preRules().getCountersWithResponse("lrs1", "1", null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PreRules_getCounters_MaximumSet_Gen.json
     */
    /**
     * Sample code: PreRules_getCounters_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void preRulesGetCountersMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.preRules().getCountersWithResponse("lrs1", "1", "firewall1", com.azure.core.util.Context.NONE);
    }
}
```

### PreRules_List

```java
/** Samples for PreRules List. */
public final class PreRulesListSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PreRules_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: PreRules_List_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void preRulesListMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.preRules().list("lrs1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PreRules_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: PreRules_List_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void preRulesListMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.preRules().list("lrs1", com.azure.core.util.Context.NONE);
    }
}
```

### PreRules_RefreshCounters

```java
/** Samples for PreRules RefreshCounters. */
public final class PreRulesRefreshCountersSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PreRules_refreshCounters_MaximumSet_Gen.json
     */
    /**
     * Sample code: PreRules_refreshCounters_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void preRulesRefreshCountersMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.preRules().refreshCountersWithResponse("lrs1", "1", "firewall1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PreRules_refreshCounters_MinimumSet_Gen.json
     */
    /**
     * Sample code: PreRules_refreshCounters_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void preRulesRefreshCountersMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.preRules().refreshCountersWithResponse("lrs1", "1", null, com.azure.core.util.Context.NONE);
    }
}
```

### PreRules_ResetCounters

```java
/** Samples for PreRules ResetCounters. */
public final class PreRulesResetCountersSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PreRules_resetCounters_MinimumSet_Gen.json
     */
    /**
     * Sample code: PreRules_resetCounters_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void preRulesResetCountersMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.preRules().resetCountersWithResponse("lrs1", "1", null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PreRules_resetCounters_MaximumSet_Gen.json
     */
    /**
     * Sample code: PreRules_resetCounters_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void preRulesResetCountersMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.preRules().resetCountersWithResponse("lrs1", "1", "firewall1", com.azure.core.util.Context.NONE);
    }
}
```

### PrefixListGlobalRulestack_CreateOrUpdate

```java
import com.azure.resourcemanager.paloaltonetworks.fluent.models.PrefixListGlobalRulestackResourceInner;
import java.util.Arrays;

/** Samples for PrefixListGlobalRulestack CreateOrUpdate. */
public final class PrefixListGlobalRulestackCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PrefixListGlobalRulestack_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: PrefixListGlobalRulestack_CreateOrUpdate_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void prefixListGlobalRulestackCreateOrUpdateMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .prefixListGlobalRulestacks()
            .createOrUpdate(
                "praval",
                "armid1",
                new PrefixListGlobalRulestackResourceInner()
                    .withDescription("string")
                    .withPrefixList(Arrays.asList("1.0.0.0/24"))
                    .withEtag("2bf4a339-294d-4c25-b0b2-ef649e9f5c27")
                    .withAuditComment("comment"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PrefixListGlobalRulestack_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: PrefixListGlobalRulestack_CreateOrUpdate_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void prefixListGlobalRulestackCreateOrUpdateMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .prefixListGlobalRulestacks()
            .createOrUpdate(
                "praval",
                "armid1",
                new PrefixListGlobalRulestackResourceInner().withPrefixList(Arrays.asList("1.0.0.0/24")),
                com.azure.core.util.Context.NONE);
    }
}
```

### PrefixListGlobalRulestack_Delete

```java
/** Samples for PrefixListGlobalRulestack Delete. */
public final class PrefixListGlobalRulestackDeleteSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PrefixListGlobalRulestack_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: PrefixListGlobalRulestack_Delete_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void prefixListGlobalRulestackDeleteMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.prefixListGlobalRulestacks().delete("praval", "armid1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PrefixListGlobalRulestack_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: PrefixListGlobalRulestack_Delete_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void prefixListGlobalRulestackDeleteMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.prefixListGlobalRulestacks().delete("praval", "armid1", com.azure.core.util.Context.NONE);
    }
}
```

### PrefixListGlobalRulestack_Get

```java
/** Samples for PrefixListGlobalRulestack Get. */
public final class PrefixListGlobalRulestackGetSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PrefixListGlobalRulestack_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: PrefixListGlobalRulestack_Get_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void prefixListGlobalRulestackGetMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.prefixListGlobalRulestacks().getWithResponse("praval", "armid1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PrefixListGlobalRulestack_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: PrefixListGlobalRulestack_Get_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void prefixListGlobalRulestackGetMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.prefixListGlobalRulestacks().getWithResponse("praval", "armid1", com.azure.core.util.Context.NONE);
    }
}
```

### PrefixListGlobalRulestack_List

```java
/** Samples for PrefixListGlobalRulestack List. */
public final class PrefixListGlobalRulestackListSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PrefixListGlobalRulestack_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: PrefixListGlobalRulestack_List_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void prefixListGlobalRulestackListMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.prefixListGlobalRulestacks().list("praval", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PrefixListGlobalRulestack_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: PrefixListGlobalRulestack_List_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void prefixListGlobalRulestackListMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.prefixListGlobalRulestacks().list("praval", com.azure.core.util.Context.NONE);
    }
}
```

### PrefixListLocalRulestack_CreateOrUpdate

```java
import java.util.Arrays;

/** Samples for PrefixListLocalRulestack CreateOrUpdate. */
public final class PrefixListLocalRulestackCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PrefixListLocalRulestack_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: PrefixListLocalRulestack_CreateOrUpdate_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void prefixListLocalRulestackCreateOrUpdateMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .prefixListLocalRulestacks()
            .define("armid1")
            .withExistingLocalRulestack("rgopenapi", "lrs1")
            .withPrefixList(Arrays.asList("1.0.0.0/24"))
            .create();
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PrefixListLocalRulestack_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: PrefixListLocalRulestack_CreateOrUpdate_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void prefixListLocalRulestackCreateOrUpdateMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .prefixListLocalRulestacks()
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
/** Samples for PrefixListLocalRulestack Delete. */
public final class PrefixListLocalRulestackDeleteSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PrefixListLocalRulestack_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: PrefixListLocalRulestack_Delete_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void prefixListLocalRulestackDeleteMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.prefixListLocalRulestacks().delete("rgopenapi", "lrs1", "armid1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PrefixListLocalRulestack_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: PrefixListLocalRulestack_Delete_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void prefixListLocalRulestackDeleteMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager.prefixListLocalRulestacks().delete("rgopenapi", "lrs1", "armid1", com.azure.core.util.Context.NONE);
    }
}
```

### PrefixListLocalRulestack_Get

```java
/** Samples for PrefixListLocalRulestack Get. */
public final class PrefixListLocalRulestackGetSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PrefixListLocalRulestack_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: PrefixListLocalRulestack_Get_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void prefixListLocalRulestackGetMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .prefixListLocalRulestacks()
            .getWithResponse("rgopenapi", "lrs1", "armid1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PrefixListLocalRulestack_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: PrefixListLocalRulestack_Get_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void prefixListLocalRulestackGetMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .prefixListLocalRulestacks()
            .getWithResponse("rgopenapi", "lrs1", "armid1", com.azure.core.util.Context.NONE);
    }
}
```

### PrefixListLocalRulestack_ListByLocalRulestacks

```java
/** Samples for PrefixListLocalRulestack ListByLocalRulestacks. */
public final class PrefixListLocalRulestackListByLocalRulestacksSamples {
    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PrefixListLocalRulestack_ListByLocalRulestacks_MaximumSet_Gen.json
     */
    /**
     * Sample code: PrefixListLocalRulestack_ListByLocalRulestacks_MaximumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void prefixListLocalRulestackListByLocalRulestacksMaximumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .prefixListLocalRulestacks()
            .listByLocalRulestacks("rgopenapi", "lrs1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/paloaltonetworks/resource-manager/PaloAltoNetworks.Cloudngfw/preview/2022-08-29-preview/examples/PrefixListLocalRulestack_ListByLocalRulestacks_MinimumSet_Gen.json
     */
    /**
     * Sample code: PrefixListLocalRulestack_ListByLocalRulestacks_MinimumSet_Gen.
     *
     * @param manager Entry point to PaloAltoNetworksManager.
     */
    public static void prefixListLocalRulestackListByLocalRulestacksMinimumSetGen(
        com.azure.resourcemanager.paloaltonetworks.PaloAltoNetworksManager manager) {
        manager
            .prefixListLocalRulestacks()
            .listByLocalRulestacks("rgopenapi", "lrs1", com.azure.core.util.Context.NONE);
    }
}
```


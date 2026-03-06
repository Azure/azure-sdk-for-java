# Code snippets and samples


## CdnPeeringPrefixes

- [List](#cdnpeeringprefixes_list)

## ConnectionMonitorTests

- [CreateOrUpdate](#connectionmonitortests_createorupdate)
- [Delete](#connectionmonitortests_delete)
- [Get](#connectionmonitortests_get)
- [ListByPeeringService](#connectionmonitortests_listbypeeringservice)

## LegacyPeerings

- [List](#legacypeerings_list)

## LookingGlass

- [Invoke](#lookingglass_invoke)

## Operations

- [List](#operations_list)

## PeerAsns

- [CreateOrUpdate](#peerasns_createorupdate)
- [Delete](#peerasns_delete)
- [Get](#peerasns_get)
- [List](#peerasns_list)

## PeeringLocations

- [List](#peeringlocations_list)

## PeeringServiceCountries

- [List](#peeringservicecountries_list)

## PeeringServiceLocations

- [List](#peeringservicelocations_list)

## PeeringServiceProviders

- [List](#peeringserviceproviders_list)

## PeeringServices

- [CreateOrUpdate](#peeringservices_createorupdate)
- [Delete](#peeringservices_delete)
- [GetByResourceGroup](#peeringservices_getbyresourcegroup)
- [InitializeConnectionMonitor](#peeringservices_initializeconnectionmonitor)
- [List](#peeringservices_list)
- [ListByResourceGroup](#peeringservices_listbyresourcegroup)
- [Update](#peeringservices_update)

## Peerings

- [CreateOrUpdate](#peerings_createorupdate)
- [Delete](#peerings_delete)
- [GetByResourceGroup](#peerings_getbyresourcegroup)
- [List](#peerings_list)
- [ListByResourceGroup](#peerings_listbyresourcegroup)
- [Update](#peerings_update)

## Prefixes

- [CreateOrUpdate](#prefixes_createorupdate)
- [Delete](#prefixes_delete)
- [Get](#prefixes_get)
- [ListByPeeringService](#prefixes_listbypeeringservice)

## ReceivedRoutes

- [ListByPeering](#receivedroutes_listbypeering)

## RegisteredAsns

- [CreateOrUpdate](#registeredasns_createorupdate)
- [Delete](#registeredasns_delete)
- [Get](#registeredasns_get)
- [ListByPeering](#registeredasns_listbypeering)

## RegisteredPrefixes

- [CreateOrUpdate](#registeredprefixes_createorupdate)
- [Delete](#registeredprefixes_delete)
- [Get](#registeredprefixes_get)
- [ListByPeering](#registeredprefixes_listbypeering)
- [Validate](#registeredprefixes_validate)

## ResourceProvider

- [CheckServiceProviderAvailability](#resourceprovider_checkserviceprovideravailability)

## RpUnbilledPrefixes

- [List](#rpunbilledprefixes_list)
### CdnPeeringPrefixes_List

```java
/**
 * Samples for CdnPeeringPrefixes List.
 */
public final class CdnPeeringPrefixesListSamples {
    /*
     * x-ms-original-file: 2025-05-01/ListCdnPeeringPrefixes.json
     */
    /**
     * Sample code: List all the cdn peering prefixes advertised at a particular peering location.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void listAllTheCdnPeeringPrefixesAdvertisedAtAParticularPeeringLocation(
        com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.cdnPeeringPrefixes().list("peeringLocation0", com.azure.core.util.Context.NONE);
    }
}
```

### ConnectionMonitorTests_CreateOrUpdate

```java
/**
 * Samples for ConnectionMonitorTests CreateOrUpdate.
 */
public final class ConnectionMonitorTestsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-05-01/CreateOrUpdateConnectionMonitorTest.json
     */
    /**
     * Sample code: Create or Update Connection Monitor Test.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void createOrUpdateConnectionMonitorTest(com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.connectionMonitorTests()
            .define("connectionMonitorTestName")
            .withExistingPeeringService("rgName", "peeringServiceName")
            .withSourceAgent("Example Source Agent")
            .withDestination("Example Destination")
            .withDestinationPort(443)
            .withTestFrequencyInSec(30)
            .create();
    }
}
```

### ConnectionMonitorTests_Delete

```java
/**
 * Samples for ConnectionMonitorTests Delete.
 */
public final class ConnectionMonitorTestsDeleteSamples {
    /*
     * x-ms-original-file: 2025-05-01/DeleteConnectionMonitorTest.json
     */
    /**
     * Sample code: Delete Connection Monitor Test.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void deleteConnectionMonitorTest(com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.connectionMonitorTests()
            .deleteWithResponse("rgName", "peeringServiceName", "connectionMonitorTestName",
                com.azure.core.util.Context.NONE);
    }
}
```

### ConnectionMonitorTests_Get

```java
/**
 * Samples for ConnectionMonitorTests Get.
 */
public final class ConnectionMonitorTestsGetSamples {
    /*
     * x-ms-original-file: 2025-05-01/GetConnectionMonitorTest.json
     */
    /**
     * Sample code: Get Connection Monitor Test.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void getConnectionMonitorTest(com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.connectionMonitorTests()
            .getWithResponse("rgName", "peeringServiceName", "connectionMonitorTestName",
                com.azure.core.util.Context.NONE);
    }
}
```

### ConnectionMonitorTests_ListByPeeringService

```java
/**
 * Samples for ConnectionMonitorTests ListByPeeringService.
 */
public final class ConnectionMonitorTestsListByPeeringServiceSamples {
    /*
     * x-ms-original-file: 2025-05-01/ListConnectionMonitorTestsByPeeringService.json
     */
    /**
     * Sample code: List all Connection Monitor Tests associated with the peering service.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void listAllConnectionMonitorTestsAssociatedWithThePeeringService(
        com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.connectionMonitorTests()
            .listByPeeringService("rgName", "peeringServiceName", com.azure.core.util.Context.NONE);
    }
}
```

### LegacyPeerings_List

```java
import com.azure.resourcemanager.peering.models.DirectPeeringType;
import com.azure.resourcemanager.peering.models.LegacyPeeringsKind;

/**
 * Samples for LegacyPeerings List.
 */
public final class LegacyPeeringsListSamples {
    /*
     * x-ms-original-file: 2025-05-01/ListLegacyPeerings.json
     */
    /**
     * Sample code: List legacy peerings.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void listLegacyPeerings(com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.legacyPeerings()
            .list("peeringLocation0", LegacyPeeringsKind.EXCHANGE, null, DirectPeeringType.EDGE,
                com.azure.core.util.Context.NONE);
    }
}
```

### LookingGlass_Invoke

```java
import com.azure.resourcemanager.peering.models.LookingGlassCommand;
import com.azure.resourcemanager.peering.models.LookingGlassSourceType;

/**
 * Samples for LookingGlass Invoke.
 */
public final class LookingGlassInvokeSamples {
    /*
     * x-ms-original-file: 2025-05-01/LookingGlassInvokeCommand.json
     */
    /**
     * Sample code: Call looking glass to execute a command.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void callLookingGlassToExecuteACommand(com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.lookingGlass()
            .invokeWithResponse(LookingGlassCommand.TRACEROUTE, LookingGlassSourceType.AZURE_REGION, "West US",
                "0.0.0.0", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2025-05-01/ListPeeringOperations.json
     */
    /**
     * Sample code: List peering operations.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void listPeeringOperations(com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### PeerAsns_CreateOrUpdate

```java
import com.azure.resourcemanager.peering.models.ContactDetail;
import com.azure.resourcemanager.peering.models.Role;
import java.util.Arrays;

/**
 * Samples for PeerAsns CreateOrUpdate.
 */
public final class PeerAsnsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-05-01/CreatePeerAsn.json
     */
    /**
     * Sample code: Create a peer ASN.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void createAPeerASN(com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.peerAsns()
            .define("peerAsnName")
            .withPeerAsn(65000)
            .withPeerContactDetail(Arrays.asList(
                new ContactDetail().withRole(Role.NOC).withEmail("noc@contoso.com").withPhone("+1 (234) 567-8999"),
                new ContactDetail().withRole(Role.POLICY).withEmail("abc@contoso.com").withPhone("+1 (234) 567-8900"),
                new ContactDetail().withRole(Role.TECHNICAL)
                    .withEmail("xyz@contoso.com")
                    .withPhone("+1 (234) 567-8900")))
            .withPeerName("Contoso")
            .create();
    }
}
```

### PeerAsns_Delete

```java
/**
 * Samples for PeerAsns Delete.
 */
public final class PeerAsnsDeleteSamples {
    /*
     * x-ms-original-file: 2025-05-01/DeletePeerAsn.json
     */
    /**
     * Sample code: Delete a peer ASN.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void deleteAPeerASN(com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.peerAsns().deleteWithResponse("peerAsnName", com.azure.core.util.Context.NONE);
    }
}
```

### PeerAsns_Get

```java
/**
 * Samples for PeerAsns Get.
 */
public final class PeerAsnsGetSamples {
    /*
     * x-ms-original-file: 2025-05-01/GetPeerAsn.json
     */
    /**
     * Sample code: Get a peer ASN.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void getAPeerASN(com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.peerAsns().getWithResponse("peerAsnName", com.azure.core.util.Context.NONE);
    }
}
```

### PeerAsns_List

```java
/**
 * Samples for PeerAsns List.
 */
public final class PeerAsnsListSamples {
    /*
     * x-ms-original-file: 2025-05-01/ListPeerAsnsBySubscription.json
     */
    /**
     * Sample code: List peer ASNs in a subscription.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void listPeerASNsInASubscription(com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.peerAsns().list(com.azure.core.util.Context.NONE);
    }
}
```

### PeeringLocations_List

```java
import com.azure.resourcemanager.peering.models.PeeringLocationsKind;

/**
 * Samples for PeeringLocations List.
 */
public final class PeeringLocationsListSamples {
    /*
     * x-ms-original-file: 2025-05-01/ListExchangePeeringLocations.json
     */
    /**
     * Sample code: List exchange peering locations.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void listExchangePeeringLocations(com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.peeringLocations().list(PeeringLocationsKind.EXCHANGE, null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-05-01/ListDirectPeeringLocations.json
     */
    /**
     * Sample code: List direct peering locations.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void listDirectPeeringLocations(com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.peeringLocations().list(PeeringLocationsKind.DIRECT, null, com.azure.core.util.Context.NONE);
    }
}
```

### PeeringServiceCountries_List

```java
/**
 * Samples for PeeringServiceCountries List.
 */
public final class PeeringServiceCountriesListSamples {
    /*
     * x-ms-original-file: 2025-05-01/ListPeeringServiceCountriesBySubscription.json
     */
    /**
     * Sample code: List peering service countries.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void listPeeringServiceCountries(com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.peeringServiceCountries().list(com.azure.core.util.Context.NONE);
    }
}
```

### PeeringServiceLocations_List

```java
/**
 * Samples for PeeringServiceLocations List.
 */
public final class PeeringServiceLocationsListSamples {
    /*
     * x-ms-original-file: 2025-05-01/ListPeeringServiceLocationsBySubscription.json
     */
    /**
     * Sample code: List peering service locations.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void listPeeringServiceLocations(com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.peeringServiceLocations().list(null, com.azure.core.util.Context.NONE);
    }
}
```

### PeeringServiceProviders_List

```java
/**
 * Samples for PeeringServiceProviders List.
 */
public final class PeeringServiceProvidersListSamples {
    /*
     * x-ms-original-file: 2025-05-01/ListPeeringServiceProviders.json
     */
    /**
     * Sample code: List peering service providers.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void listPeeringServiceProviders(com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.peeringServiceProviders().list(com.azure.core.util.Context.NONE);
    }
}
```

### PeeringServices_CreateOrUpdate

```java
/**
 * Samples for PeeringServices CreateOrUpdate.
 */
public final class PeeringServicesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-05-01/CreatePeeringService.json
     */
    /**
     * Sample code: Create a peering service.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void createAPeeringService(com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.peeringServices()
            .define("peeringServiceName")
            .withRegion("eastus")
            .withExistingResourceGroup("rgName")
            .withPeeringServiceLocation("state1")
            .withPeeringServiceProvider("serviceProvider1")
            .withProviderPrimaryPeeringLocation("peeringLocation1")
            .withProviderBackupPeeringLocation("peeringLocation2")
            .create();
    }
}
```

### PeeringServices_Delete

```java
/**
 * Samples for PeeringServices Delete.
 */
public final class PeeringServicesDeleteSamples {
    /*
     * x-ms-original-file: 2025-05-01/DeletePeeringService.json
     */
    /**
     * Sample code: Delete a peering service.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void deleteAPeeringService(com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.peeringServices()
            .deleteByResourceGroupWithResponse("rgName", "peeringServiceName", com.azure.core.util.Context.NONE);
    }
}
```

### PeeringServices_GetByResourceGroup

```java
/**
 * Samples for PeeringServices GetByResourceGroup.
 */
public final class PeeringServicesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-05-01/GetPeeringService.json
     */
    /**
     * Sample code: Get a peering service.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void getAPeeringService(com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.peeringServices()
            .getByResourceGroupWithResponse("rgName", "peeringServiceName", com.azure.core.util.Context.NONE);
    }
}
```

### PeeringServices_InitializeConnectionMonitor

```java
/**
 * Samples for PeeringServices InitializeConnectionMonitor.
 */
public final class PeeringServicesInitializeConnectionMonitorSamples {
    /*
     * x-ms-original-file: 2025-05-01/InitializeConnectionMonitor.json
     */
    /**
     * Sample code: Initialize Peering Service for Connection Monitor functionality.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void initializePeeringServiceForConnectionMonitorFunctionality(
        com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.peeringServices().initializeConnectionMonitorWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### PeeringServices_List

```java
/**
 * Samples for PeeringServices List.
 */
public final class PeeringServicesListSamples {
    /*
     * x-ms-original-file: 2025-05-01/ListPeeringServicesBySubscription.json
     */
    /**
     * Sample code: List peering services in a subscription.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void listPeeringServicesInASubscription(com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.peeringServices().list(com.azure.core.util.Context.NONE);
    }
}
```

### PeeringServices_ListByResourceGroup

```java
/**
 * Samples for PeeringServices ListByResourceGroup.
 */
public final class PeeringServicesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-05-01/ListPeeringServicesByResourceGroup.json
     */
    /**
     * Sample code: List peering services in a resource group.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void listPeeringServicesInAResourceGroup(com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.peeringServices().listByResourceGroup("rgName", com.azure.core.util.Context.NONE);
    }
}
```

### PeeringServices_Update

```java
import com.azure.resourcemanager.peering.models.PeeringService;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for PeeringServices Update.
 */
public final class PeeringServicesUpdateSamples {
    /*
     * x-ms-original-file: 2025-05-01/UpdatePeeringServiceTags.json
     */
    /**
     * Sample code: Update peering service tags.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void updatePeeringServiceTags(com.azure.resourcemanager.peering.PeeringManager manager) {
        PeeringService resource = manager.peeringServices()
            .getByResourceGroupWithResponse("rgName", "peeringServiceName", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("tag0", "value0", "tag1", "value1")).apply();
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

### Peerings_CreateOrUpdate

```java
import com.azure.core.management.SubResource;
import com.azure.resourcemanager.peering.models.BgpSession;
import com.azure.resourcemanager.peering.models.ConnectivityProbe;
import com.azure.resourcemanager.peering.models.DirectConnection;
import com.azure.resourcemanager.peering.models.DirectPeeringType;
import com.azure.resourcemanager.peering.models.ExchangeConnection;
import com.azure.resourcemanager.peering.models.Kind;
import com.azure.resourcemanager.peering.models.PeeringPropertiesDirect;
import com.azure.resourcemanager.peering.models.PeeringPropertiesExchange;
import com.azure.resourcemanager.peering.models.PeeringSku;
import com.azure.resourcemanager.peering.models.Protocol;
import com.azure.resourcemanager.peering.models.SessionAddressProvider;
import java.util.Arrays;

/**
 * Samples for Peerings CreateOrUpdate.
 */
public final class PeeringsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-05-01/CreatePeeringWithExchangeRouteServer.json
     */
    /**
     * Sample code: Create a peering with exchange route server.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void createAPeeringWithExchangeRouteServer(com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.peerings()
            .define("peeringName")
            .withRegion("eastus")
            .withExistingResourceGroup("rgName")
            .withSku(new PeeringSku().withName("Premium_Direct_Free"))
            .withKind(Kind.DIRECT)
            .withDirect(new PeeringPropertiesDirect()
                .withConnections(Arrays.asList(new DirectConnection().withBandwidthInMbps(10000)
                    .withSessionAddressProvider(SessionAddressProvider.PEER)
                    .withUseForPeeringService(true)
                    .withPeeringDBFacilityId(99999)
                    .withBgpSession(new BgpSession().withSessionPrefixV4("192.168.0.0/24")
                        .withMicrosoftSessionIPv4Address("192.168.0.123")
                        .withPeerSessionIPv4Address("192.168.0.234")
                        .withMaxPrefixesAdvertisedV4(1000)
                        .withMaxPrefixesAdvertisedV6(100))
                    .withConnectionIdentifier("5F4CB5C7-6B43-4444-9338-9ABC72606C16")))
                .withPeerAsn(
                    new SubResource().withId("/subscriptions/subId/providers/Microsoft.Peering/peerAsns/myAsn1"))
                .withDirectPeeringType(DirectPeeringType.IX_RS))
            .withConnectivityProbes(Arrays.asList(new ConnectivityProbe().withEndpoint("192.168.0.1")
                .withAzureRegion("eastus")
                .withProtocol(Protocol.TCP)))
            .withPeeringLocation("peeringLocation0")
            .create();
    }

    /*
     * x-ms-original-file: 2025-05-01/CreateExchangePeering.json
     */
    /**
     * Sample code: Create an exchange peering.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void createAnExchangePeering(com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.peerings()
            .define("peeringName")
            .withRegion("eastus")
            .withExistingResourceGroup("rgName")
            .withSku(new PeeringSku().withName("Basic_Exchange_Free"))
            .withKind(Kind.EXCHANGE)
            .withExchange(new PeeringPropertiesExchange()
                .withConnections(Arrays.asList(
                    new ExchangeConnection().withPeeringDBFacilityId(99999)
                        .withBgpSession(new BgpSession().withPeerSessionIPv4Address("192.168.2.1")
                            .withPeerSessionIPv6Address("fd00::1")
                            .withMaxPrefixesAdvertisedV4(1000)
                            .withMaxPrefixesAdvertisedV6(100)
                            .withMd5AuthenticationKey("fakeTokenPlaceholder"))
                        .withConnectionIdentifier("CE495334-0E94-4E51-8164-8116D6CD284D"),
                    new ExchangeConnection().withPeeringDBFacilityId(99999)
                        .withBgpSession(new BgpSession().withPeerSessionIPv4Address("192.168.2.2")
                            .withPeerSessionIPv6Address("fd00::2")
                            .withMaxPrefixesAdvertisedV4(1000)
                            .withMaxPrefixesAdvertisedV6(100)
                            .withMd5AuthenticationKey("fakeTokenPlaceholder"))
                        .withConnectionIdentifier("CDD8E673-CB07-47E6-84DE-3739F778762B")))
                .withPeerAsn(
                    new SubResource().withId("/subscriptions/subId/providers/Microsoft.Peering/peerAsns/myAsn1")))
            .withConnectivityProbes(Arrays.asList(new ConnectivityProbe().withEndpoint("192.168.0.1")
                .withAzureRegion("eastus")
                .withProtocol(Protocol.ICMP)))
            .withPeeringLocation("peeringLocation0")
            .create();
    }

    /*
     * x-ms-original-file: 2025-05-01/CreateDirectPeering.json
     */
    /**
     * Sample code: Create a direct peering.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void createADirectPeering(com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.peerings()
            .define("peeringName")
            .withRegion("eastus")
            .withExistingResourceGroup("rgName")
            .withSku(new PeeringSku().withName("Basic_Direct_Free"))
            .withKind(Kind.DIRECT)
            .withDirect(new PeeringPropertiesDirect()
                .withConnections(Arrays.asList(
                    new DirectConnection().withBandwidthInMbps(10000)
                        .withSessionAddressProvider(SessionAddressProvider.PEER)
                        .withUseForPeeringService(false)
                        .withPeeringDBFacilityId(99999)
                        .withBgpSession(new BgpSession().withSessionPrefixV4("192.168.0.0/31")
                            .withSessionPrefixV6("fd00::0/127")
                            .withMaxPrefixesAdvertisedV4(1000)
                            .withMaxPrefixesAdvertisedV6(100)
                            .withMd5AuthenticationKey("fakeTokenPlaceholder"))
                        .withConnectionIdentifier("5F4CB5C7-6B43-4444-9338-9ABC72606C16"),
                    new DirectConnection().withBandwidthInMbps(10000)
                        .withSessionAddressProvider(SessionAddressProvider.MICROSOFT)
                        .withUseForPeeringService(true)
                        .withPeeringDBFacilityId(99999)
                        .withConnectionIdentifier("8AB00818-D533-4504-A25A-03A17F61201C")))
                .withPeerAsn(
                    new SubResource().withId("/subscriptions/subId/providers/Microsoft.Peering/peerAsns/myAsn1"))
                .withDirectPeeringType(DirectPeeringType.EDGE))
            .withConnectivityProbes(Arrays.asList(new ConnectivityProbe().withEndpoint("192.168.0.1")
                .withAzureRegion("eastus")
                .withProtocol(Protocol.ICMP)))
            .withPeeringLocation("peeringLocation0")
            .create();
    }
}
```

### Peerings_Delete

```java
/**
 * Samples for Peerings Delete.
 */
public final class PeeringsDeleteSamples {
    /*
     * x-ms-original-file: 2025-05-01/DeletePeering.json
     */
    /**
     * Sample code: Delete a peering.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void deleteAPeering(com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.peerings().deleteByResourceGroupWithResponse("rgName", "peeringName", com.azure.core.util.Context.NONE);
    }
}
```

### Peerings_GetByResourceGroup

```java
/**
 * Samples for Peerings GetByResourceGroup.
 */
public final class PeeringsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-05-01/GetPeering.json
     */
    /**
     * Sample code: Get a peering.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void getAPeering(com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.peerings().getByResourceGroupWithResponse("rgName", "peeringName", com.azure.core.util.Context.NONE);
    }
}
```

### Peerings_List

```java
/**
 * Samples for Peerings List.
 */
public final class PeeringsListSamples {
    /*
     * x-ms-original-file: 2025-05-01/ListPeeringsBySubscription.json
     */
    /**
     * Sample code: List peerings in a subscription.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void listPeeringsInASubscription(com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.peerings().list(com.azure.core.util.Context.NONE);
    }
}
```

### Peerings_ListByResourceGroup

```java
/**
 * Samples for Peerings ListByResourceGroup.
 */
public final class PeeringsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-05-01/ListPeeringsByResourceGroup.json
     */
    /**
     * Sample code: List peerings in a resource group.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void listPeeringsInAResourceGroup(com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.peerings().listByResourceGroup("rgName", com.azure.core.util.Context.NONE);
    }
}
```

### Peerings_Update

```java
import com.azure.resourcemanager.peering.models.Peering;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Peerings Update.
 */
public final class PeeringsUpdateSamples {
    /*
     * x-ms-original-file: 2025-05-01/UpdatePeeringTags.json
     */
    /**
     * Sample code: Update peering tags.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void updatePeeringTags(com.azure.resourcemanager.peering.PeeringManager manager) {
        Peering resource = manager.peerings()
            .getByResourceGroupWithResponse("rgName", "peeringName", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("tag0", "value0", "tag1", "value1")).apply();
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

### Prefixes_CreateOrUpdate

```java
/**
 * Samples for Prefixes CreateOrUpdate.
 */
public final class PrefixesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-05-01/CreatePeeringServicePrefix.json
     */
    /**
     * Sample code: Create or update a prefix for the peering service.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void
        createOrUpdateAPrefixForThePeeringService(com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.prefixes()
            .define("peeringServicePrefixName")
            .withExistingPeeringService("rgName", "peeringServiceName")
            .withPrefix("192.168.1.0/24")
            .withPeeringServicePrefixKey("00000000-0000-0000-0000-000000000000")
            .create();
    }
}
```

### Prefixes_Delete

```java
/**
 * Samples for Prefixes Delete.
 */
public final class PrefixesDeleteSamples {
    /*
     * x-ms-original-file: 2025-05-01/DeletePeeringServicePrefix.json
     */
    /**
     * Sample code: Delete a prefix associated with the peering service.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void
        deleteAPrefixAssociatedWithThePeeringService(com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.prefixes()
            .deleteWithResponse("rgName", "peeringServiceName", "peeringServicePrefixName",
                com.azure.core.util.Context.NONE);
    }
}
```

### Prefixes_Get

```java
/**
 * Samples for Prefixes Get.
 */
public final class PrefixesGetSamples {
    /*
     * x-ms-original-file: 2025-05-01/GetPeeringServicePrefix.json
     */
    /**
     * Sample code: Get a prefix associated with the peering service.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void
        getAPrefixAssociatedWithThePeeringService(com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.prefixes()
            .getWithResponse("rgName", "peeringServiceName", "peeringServicePrefixName", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### Prefixes_ListByPeeringService

```java
/**
 * Samples for Prefixes ListByPeeringService.
 */
public final class PrefixesListByPeeringServiceSamples {
    /*
     * x-ms-original-file: 2025-05-01/ListPrefixesByPeeringService.json
     */
    /**
     * Sample code: List all the prefixes associated with the peering service.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void
        listAllThePrefixesAssociatedWithThePeeringService(com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.prefixes().listByPeeringService("rgName", "peeringServiceName", null, com.azure.core.util.Context.NONE);
    }
}
```

### ReceivedRoutes_ListByPeering

```java
/**
 * Samples for ReceivedRoutes ListByPeering.
 */
public final class ReceivedRoutesListByPeeringSamples {
    /*
     * x-ms-original-file: 2025-05-01/GetPeeringReceivedRoutes.json
     */
    /**
     * Sample code: Lists the prefixes received over the specified peering under the given subscription and resource
     * group.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void listsThePrefixesReceivedOverTheSpecifiedPeeringUnderTheGivenSubscriptionAndResourceGroup(
        com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.receivedRoutes()
            .listByPeering("rgName", "peeringName", "1.1.1.0/24", "123 456", "Valid", "Valid", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### RegisteredAsns_CreateOrUpdate

```java
/**
 * Samples for RegisteredAsns CreateOrUpdate.
 */
public final class RegisteredAsnsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-05-01/CreateRegisteredAsn.json
     */
    /**
     * Sample code: Create or update a registered ASN for the peering.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void
        createOrUpdateARegisteredASNForThePeering(com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.registeredAsns()
            .define("registeredAsnName")
            .withExistingPeering("rgName", "peeringName")
            .withAsn(65000)
            .create();
    }
}
```

### RegisteredAsns_Delete

```java
/**
 * Samples for RegisteredAsns Delete.
 */
public final class RegisteredAsnsDeleteSamples {
    /*
     * x-ms-original-file: 2025-05-01/DeleteRegisteredAsn.json
     */
    /**
     * Sample code: Deletes a registered ASN associated with the peering.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void
        deletesARegisteredASNAssociatedWithThePeering(com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.registeredAsns()
            .deleteWithResponse("rgName", "peeringName", "registeredAsnName", com.azure.core.util.Context.NONE);
    }
}
```

### RegisteredAsns_Get

```java
/**
 * Samples for RegisteredAsns Get.
 */
public final class RegisteredAsnsGetSamples {
    /*
     * x-ms-original-file: 2025-05-01/GetRegisteredAsn.json
     */
    /**
     * Sample code: Get a registered ASN associated with the peering.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void
        getARegisteredASNAssociatedWithThePeering(com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.registeredAsns()
            .getWithResponse("rgName", "peeringName", "registeredAsnName0", com.azure.core.util.Context.NONE);
    }
}
```

### RegisteredAsns_ListByPeering

```java
/**
 * Samples for RegisteredAsns ListByPeering.
 */
public final class RegisteredAsnsListByPeeringSamples {
    /*
     * x-ms-original-file: 2025-05-01/ListRegisteredAsnsByPeering.json
     */
    /**
     * Sample code: List all the registered ASNs associated with the peering.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void
        listAllTheRegisteredASNsAssociatedWithThePeering(com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.registeredAsns().listByPeering("rgName", "peeringName", com.azure.core.util.Context.NONE);
    }
}
```

### RegisteredPrefixes_CreateOrUpdate

```java
/**
 * Samples for RegisteredPrefixes CreateOrUpdate.
 */
public final class RegisteredPrefixesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-05-01/CreateRegisteredPrefix.json
     */
    /**
     * Sample code: Create or update a registered prefix for the peering.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void
        createOrUpdateARegisteredPrefixForThePeering(com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.registeredPrefixes()
            .define("registeredPrefixName")
            .withExistingPeering("rgName", "peeringName")
            .withPrefix("10.22.20.0/24")
            .create();
    }
}
```

### RegisteredPrefixes_Delete

```java
/**
 * Samples for RegisteredPrefixes Delete.
 */
public final class RegisteredPrefixesDeleteSamples {
    /*
     * x-ms-original-file: 2025-05-01/DeleteRegisteredPrefix.json
     */
    /**
     * Sample code: Deletes a registered prefix associated with the peering.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void
        deletesARegisteredPrefixAssociatedWithThePeering(com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.registeredPrefixes()
            .deleteWithResponse("rgName", "peeringName", "registeredPrefixName", com.azure.core.util.Context.NONE);
    }
}
```

### RegisteredPrefixes_Get

```java
/**
 * Samples for RegisteredPrefixes Get.
 */
public final class RegisteredPrefixesGetSamples {
    /*
     * x-ms-original-file: 2025-05-01/GetRegisteredPrefix.json
     */
    /**
     * Sample code: Get a registered prefix associated with the peering.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void
        getARegisteredPrefixAssociatedWithThePeering(com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.registeredPrefixes()
            .getWithResponse("rgName", "peeringName", "registeredPrefixName", com.azure.core.util.Context.NONE);
    }
}
```

### RegisteredPrefixes_ListByPeering

```java
/**
 * Samples for RegisteredPrefixes ListByPeering.
 */
public final class RegisteredPrefixesListByPeeringSamples {
    /*
     * x-ms-original-file: 2025-05-01/ListRegisteredPrefixesByPeering.json
     */
    /**
     * Sample code: List all the registered prefixes associated with the peering.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void
        listAllTheRegisteredPrefixesAssociatedWithThePeering(com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.registeredPrefixes().listByPeering("rgName", "peeringName", com.azure.core.util.Context.NONE);
    }
}
```

### RegisteredPrefixes_Validate

```java
/**
 * Samples for RegisteredPrefixes Validate.
 */
public final class RegisteredPrefixesValidateSamples {
    /*
     * x-ms-original-file: 2025-05-01/ValidateRegisteredPrefix.json
     */
    /**
     * Sample code: Validate a registered prefix associated with the peering.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void
        validateARegisteredPrefixAssociatedWithThePeering(com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.registeredPrefixes()
            .validateWithResponse("rgName", "peeringName", "registeredPrefixName", com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_CheckServiceProviderAvailability

```java
import com.azure.resourcemanager.peering.models.CheckServiceProviderAvailabilityInput;

/**
 * Samples for ResourceProvider CheckServiceProviderAvailability.
 */
public final class ResourceProviderCheckServiceProviderAvailabilitySamples {
    /*
     * x-ms-original-file: 2025-05-01/CheckServiceProviderAvailability.json
     */
    /**
     * Sample code: Check if peering service provider is available in customer location.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void checkIfPeeringServiceProviderIsAvailableInCustomerLocation(
        com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.resourceProviders()
            .checkServiceProviderAvailabilityWithResponse(
                new CheckServiceProviderAvailabilityInput().withPeeringServiceLocation("peeringServiceLocation1")
                    .withPeeringServiceProvider("peeringServiceProvider1"),
                com.azure.core.util.Context.NONE);
    }
}
```

### RpUnbilledPrefixes_List

```java
/**
 * Samples for RpUnbilledPrefixes List.
 */
public final class RpUnbilledPrefixesListSamples {
    /*
     * x-ms-original-file: 2025-05-01/ListRpUnbilledPrefixes.json
     */
    /**
     * Sample code: List all the RP unbilled prefixes advertised at a particular peering location.
     * 
     * @param manager Entry point to PeeringManager.
     */
    public static void listAllTheRPUnbilledPrefixesAdvertisedAtAParticularPeeringLocation(
        com.azure.resourcemanager.peering.PeeringManager manager) {
        manager.rpUnbilledPrefixes().list("rgName", "peeringName", true, com.azure.core.util.Context.NONE);
    }
}
```


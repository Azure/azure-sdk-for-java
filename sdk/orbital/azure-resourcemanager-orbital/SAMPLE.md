# Code snippets and samples


## AvailableGroundStations

- [List](#availablegroundstations_list)

## ContactProfiles

- [CreateOrUpdate](#contactprofiles_createorupdate)
- [Delete](#contactprofiles_delete)
- [GetByResourceGroup](#contactprofiles_getbyresourcegroup)
- [List](#contactprofiles_list)
- [ListByResourceGroup](#contactprofiles_listbyresourcegroup)
- [UpdateTags](#contactprofiles_updatetags)

## Contacts

- [Create](#contacts_create)
- [Delete](#contacts_delete)
- [Get](#contacts_get)
- [List](#contacts_list)

## Operations

- [List](#operations_list)

## OperationsResults

- [Get](#operationsresults_get)

## Spacecrafts

- [CreateOrUpdate](#spacecrafts_createorupdate)
- [Delete](#spacecrafts_delete)
- [GetByResourceGroup](#spacecrafts_getbyresourcegroup)
- [List](#spacecrafts_list)
- [ListAvailableContacts](#spacecrafts_listavailablecontacts)
- [ListByResourceGroup](#spacecrafts_listbyresourcegroup)
- [UpdateTags](#spacecrafts_updatetags)
### AvailableGroundStations_List

```java
import com.azure.resourcemanager.orbital.models.CapabilityParameter;

/** Samples for AvailableGroundStations List. */
public final class AvailableGroundStationsListSamples {
    /*
     * x-ms-original-file: specification/orbital/resource-manager/Microsoft.Orbital/stable/2022-11-01/examples/AvailableGroundStationsByCapabilityList.json
     */
    /**
     * Sample code: List of Ground Stations by Capability.
     *
     * @param manager Entry point to OrbitalManager.
     */
    public static void listOfGroundStationsByCapability(com.azure.resourcemanager.orbital.OrbitalManager manager) {
        manager.availableGroundStations().list(CapabilityParameter.EARTH_OBSERVATION, com.azure.core.util.Context.NONE);
    }
}
```

### ContactProfiles_CreateOrUpdate

```java
import com.azure.resourcemanager.orbital.models.AutoTrackingConfiguration;
import com.azure.resourcemanager.orbital.models.ContactProfileLink;
import com.azure.resourcemanager.orbital.models.ContactProfileLinkChannel;
import com.azure.resourcemanager.orbital.models.ContactProfileThirdPartyConfiguration;
import com.azure.resourcemanager.orbital.models.ContactProfilesPropertiesNetworkConfiguration;
import com.azure.resourcemanager.orbital.models.Direction;
import com.azure.resourcemanager.orbital.models.EndPoint;
import com.azure.resourcemanager.orbital.models.Polarization;
import com.azure.resourcemanager.orbital.models.Protocol;
import java.util.Arrays;

/** Samples for ContactProfiles CreateOrUpdate. */
public final class ContactProfilesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/orbital/resource-manager/Microsoft.Orbital/stable/2022-11-01/examples/ContactProfileCreate.json
     */
    /**
     * Sample code: Create a contact profile.
     *
     * @param manager Entry point to OrbitalManager.
     */
    public static void createAContactProfile(com.azure.resourcemanager.orbital.OrbitalManager manager) {
        manager
            .contactProfiles()
            .define("CONTOSO-CP")
            .withRegion("eastus2")
            .withExistingResourceGroup("contoso-Rgp")
            .withNetworkConfiguration(
                new ContactProfilesPropertiesNetworkConfiguration()
                    .withSubnetId(
                        "/subscriptions/c1be1141-a7c9-4aac-9608-3c2e2f1152c3/resourceGroups/contoso-Rgp/providers/Microsoft.Network/virtualNetworks/contoso-vnet/subnets/orbital-delegated-subnet"))
            .withLinks(
                Arrays
                    .asList(
                        new ContactProfileLink()
                            .withName("contoso-uplink")
                            .withPolarization(Polarization.LHCP)
                            .withDirection(Direction.UPLINK)
                            .withGainOverTemperature(0.0F)
                            .withEirpdBW(45.0F)
                            .withChannels(
                                Arrays
                                    .asList(
                                        new ContactProfileLinkChannel()
                                            .withName("contoso-uplink-channel")
                                            .withCenterFrequencyMHz(2250f)
                                            .withBandwidthMHz(2f)
                                            .withEndPoint(
                                                new EndPoint()
                                                    .withIpAddress("10.1.0.4")
                                                    .withEndPointName("ContosoTest_Uplink")
                                                    .withPort("50000")
                                                    .withProtocol(Protocol.TCP)))),
                        new ContactProfileLink()
                            .withName("contoso-downlink")
                            .withPolarization(Polarization.RHCP)
                            .withDirection(Direction.DOWNLINK)
                            .withGainOverTemperature(25.0F)
                            .withEirpdBW(0.0F)
                            .withChannels(
                                Arrays
                                    .asList(
                                        new ContactProfileLinkChannel()
                                            .withName("contoso-downlink-channel")
                                            .withCenterFrequencyMHz(8160f)
                                            .withBandwidthMHz(15f)
                                            .withEndPoint(
                                                new EndPoint()
                                                    .withIpAddress("10.1.0.5")
                                                    .withEndPointName("ContosoTest_Downlink")
                                                    .withPort("50001")
                                                    .withProtocol(Protocol.UDP))))))
            .withMinimumViableContactDuration("PT1M")
            .withMinimumElevationDegrees(5.0F)
            .withAutoTrackingConfiguration(AutoTrackingConfiguration.DISABLED)
            .withEventHubUri(
                "/subscriptions/c1be1141-a7c9-4aac-9608-3c2e2f1152c3/resourceGroups/contoso-Rgp/providers/Microsoft.EventHub/namespaces/contosoHub/eventhubs/contosoHub")
            .withThirdPartyConfigurations(
                Arrays
                    .asList(
                        new ContactProfileThirdPartyConfiguration()
                            .withProviderName("KSAT")
                            .withMissionConfiguration("Ksat_MissionConfiguration"),
                        new ContactProfileThirdPartyConfiguration()
                            .withProviderName("VIASAT")
                            .withMissionConfiguration("Viasat_Configuration")))
            .create();
    }
}
```

### ContactProfiles_Delete

```java
/** Samples for ContactProfiles Delete. */
public final class ContactProfilesDeleteSamples {
    /*
     * x-ms-original-file: specification/orbital/resource-manager/Microsoft.Orbital/stable/2022-11-01/examples/ContactProfileDelete.json
     */
    /**
     * Sample code: Delete Contact Profile.
     *
     * @param manager Entry point to OrbitalManager.
     */
    public static void deleteContactProfile(com.azure.resourcemanager.orbital.OrbitalManager manager) {
        manager.contactProfiles().delete("contoso-Rgp", "CONTOSO-CP", com.azure.core.util.Context.NONE);
    }
}
```

### ContactProfiles_GetByResourceGroup

```java
/** Samples for ContactProfiles GetByResourceGroup. */
public final class ContactProfilesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/orbital/resource-manager/Microsoft.Orbital/stable/2022-11-01/examples/ContactProfileGet.json
     */
    /**
     * Sample code: Get a contact profile.
     *
     * @param manager Entry point to OrbitalManager.
     */
    public static void getAContactProfile(com.azure.resourcemanager.orbital.OrbitalManager manager) {
        manager
            .contactProfiles()
            .getByResourceGroupWithResponse("contoso-Rgp", "CONTOSO-CP", com.azure.core.util.Context.NONE);
    }
}
```

### ContactProfiles_List

```java
/** Samples for ContactProfiles List. */
public final class ContactProfilesListSamples {
    /*
     * x-ms-original-file: specification/orbital/resource-manager/Microsoft.Orbital/stable/2022-11-01/examples/ContactProfilesBySubscriptionList.json
     */
    /**
     * Sample code: List of Contact Profiles.
     *
     * @param manager Entry point to OrbitalManager.
     */
    public static void listOfContactProfiles(com.azure.resourcemanager.orbital.OrbitalManager manager) {
        manager.contactProfiles().list("opaqueString", com.azure.core.util.Context.NONE);
    }
}
```

### ContactProfiles_ListByResourceGroup

```java
/** Samples for ContactProfiles ListByResourceGroup. */
public final class ContactProfilesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/orbital/resource-manager/Microsoft.Orbital/stable/2022-11-01/examples/ContactProfilesByResourceGroupList.json
     */
    /**
     * Sample code: List of Contact Profiles by Resource Group.
     *
     * @param manager Entry point to OrbitalManager.
     */
    public static void listOfContactProfilesByResourceGroup(com.azure.resourcemanager.orbital.OrbitalManager manager) {
        manager.contactProfiles().listByResourceGroup("contoso-Rgp", "opaqueString", com.azure.core.util.Context.NONE);
    }
}
```

### ContactProfiles_UpdateTags

```java
import com.azure.resourcemanager.orbital.models.ContactProfile;
import java.util.HashMap;
import java.util.Map;

/** Samples for ContactProfiles UpdateTags. */
public final class ContactProfilesUpdateTagsSamples {
    /*
     * x-ms-original-file: specification/orbital/resource-manager/Microsoft.Orbital/stable/2022-11-01/examples/ContactProfileUpdateTag.json
     */
    /**
     * Sample code: Update Contact Profile tags.
     *
     * @param manager Entry point to OrbitalManager.
     */
    public static void updateContactProfileTags(com.azure.resourcemanager.orbital.OrbitalManager manager) {
        ContactProfile resource =
            manager
                .contactProfiles()
                .getByResourceGroupWithResponse("contoso-Rgp", "CONTOSO-CP", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### Contacts_Create

```java
import com.azure.resourcemanager.orbital.models.ContactsPropertiesContactProfile;
import java.time.OffsetDateTime;

/** Samples for Contacts Create. */
public final class ContactsCreateSamples {
    /*
     * x-ms-original-file: specification/orbital/resource-manager/Microsoft.Orbital/stable/2022-11-01/examples/ContactCreate.json
     */
    /**
     * Sample code: Create a contact.
     *
     * @param manager Entry point to OrbitalManager.
     */
    public static void createAContact(com.azure.resourcemanager.orbital.OrbitalManager manager) {
        manager
            .contacts()
            .define("contact1")
            .withExistingSpacecraft("contoso-Rgp", "CONTOSO_SAT")
            .withReservationStartTime(OffsetDateTime.parse("2023-02-22T10:58:30Z"))
            .withReservationEndTime(OffsetDateTime.parse("2023-02-22T11:10:45Z"))
            .withGroundStationName("EASTUS2_0")
            .withContactProfile(
                new ContactsPropertiesContactProfile()
                    .withId(
                        "/subscriptions/c1be1141-a7c9-4aac-9608-3c2e2f1152c3/resourceGroups/contoso-Rgp/providers/Microsoft.Orbital/contactProfiles/CONTOSO-CP"))
            .create();
    }
}
```

### Contacts_Delete

```java
/** Samples for Contacts Delete. */
public final class ContactsDeleteSamples {
    /*
     * x-ms-original-file: specification/orbital/resource-manager/Microsoft.Orbital/stable/2022-11-01/examples/ContactDelete.json
     */
    /**
     * Sample code: Delete Contact.
     *
     * @param manager Entry point to OrbitalManager.
     */
    public static void deleteContact(com.azure.resourcemanager.orbital.OrbitalManager manager) {
        manager.contacts().delete("contoso-Rgp", "CONTOSO_SAT", "contact1", com.azure.core.util.Context.NONE);
    }
}
```

### Contacts_Get

```java
/** Samples for Contacts Get. */
public final class ContactsGetSamples {
    /*
     * x-ms-original-file: specification/orbital/resource-manager/Microsoft.Orbital/stable/2022-11-01/examples/ContactGet.json
     */
    /**
     * Sample code: Get Contact.
     *
     * @param manager Entry point to OrbitalManager.
     */
    public static void getContact(com.azure.resourcemanager.orbital.OrbitalManager manager) {
        manager.contacts().getWithResponse("contoso-Rgp", "CONTOSO_SAT", "contact1", com.azure.core.util.Context.NONE);
    }
}
```

### Contacts_List

```java
/** Samples for Contacts List. */
public final class ContactsListSamples {
    /*
     * x-ms-original-file: specification/orbital/resource-manager/Microsoft.Orbital/stable/2022-11-01/examples/ContactsBySpacecraftNameList.json
     */
    /**
     * Sample code: List of Spacecraft.
     *
     * @param manager Entry point to OrbitalManager.
     */
    public static void listOfSpacecraft(com.azure.resourcemanager.orbital.OrbitalManager manager) {
        manager.contacts().list("contoso-Rgp", "CONTOSO_SAT", "opaqueString", com.azure.core.util.Context.NONE);
    }
}
```

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/orbital/resource-manager/Microsoft.Orbital/stable/2022-11-01/examples/OperationsList.json
     */
    /**
     * Sample code: OperationsList.
     *
     * @param manager Entry point to OrbitalManager.
     */
    public static void operationsList(com.azure.resourcemanager.orbital.OrbitalManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### OperationsResults_Get

```java
/** Samples for OperationsResults Get. */
public final class OperationsResultsGetSamples {
    /*
     * x-ms-original-file: specification/orbital/resource-manager/Microsoft.Orbital/stable/2022-11-01/examples/OperationResultsGet.json
     */
    /**
     * Sample code: KustoOperationResultsGet.
     *
     * @param manager Entry point to OrbitalManager.
     */
    public static void kustoOperationResultsGet(com.azure.resourcemanager.orbital.OrbitalManager manager) {
        manager
            .operationsResults()
            .get("eastus2", "30972f1b-b61d-4fd8-bd34-3dcfa24670f3", com.azure.core.util.Context.NONE);
    }
}
```

### Spacecrafts_CreateOrUpdate

```java
import com.azure.resourcemanager.orbital.models.Direction;
import com.azure.resourcemanager.orbital.models.Polarization;
import com.azure.resourcemanager.orbital.models.SpacecraftLink;
import java.util.Arrays;

/** Samples for Spacecrafts CreateOrUpdate. */
public final class SpacecraftsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/orbital/resource-manager/Microsoft.Orbital/stable/2022-11-01/examples/SpacecraftCreate.json
     */
    /**
     * Sample code: Create a spacecraft.
     *
     * @param manager Entry point to OrbitalManager.
     */
    public static void createASpacecraft(com.azure.resourcemanager.orbital.OrbitalManager manager) {
        manager
            .spacecrafts()
            .define("CONTOSO_SAT")
            .withRegion("eastus2")
            .withExistingResourceGroup("contoso-Rgp")
            .withTitleLine("CONTOSO_SAT")
            .withTleLine1("1 27424U 02022A   22167.05119303  .00000638  00000+0  15103-3 0  9994")
            .withTleLine2("2 27424  98.2477 108.9546 0000928  92.9194 327.0802 14.57300770 69982")
            .withLinks(
                Arrays
                    .asList(
                        new SpacecraftLink()
                            .withName("uplink_lhcp1")
                            .withCenterFrequencyMHz(2250f)
                            .withBandwidthMHz(2f)
                            .withDirection(Direction.UPLINK)
                            .withPolarization(Polarization.LHCP),
                        new SpacecraftLink()
                            .withName("downlink_rhcp1")
                            .withCenterFrequencyMHz(8160f)
                            .withBandwidthMHz(15f)
                            .withDirection(Direction.DOWNLINK)
                            .withPolarization(Polarization.RHCP)))
            .withNoradId("36411")
            .create();
    }
}
```

### Spacecrafts_Delete

```java
/** Samples for Spacecrafts Delete. */
public final class SpacecraftsDeleteSamples {
    /*
     * x-ms-original-file: specification/orbital/resource-manager/Microsoft.Orbital/stable/2022-11-01/examples/SpacecraftDelete.json
     */
    /**
     * Sample code: Delete Spacecraft.
     *
     * @param manager Entry point to OrbitalManager.
     */
    public static void deleteSpacecraft(com.azure.resourcemanager.orbital.OrbitalManager manager) {
        manager.spacecrafts().delete("contoso-Rgp", "CONTOSO_SAT", com.azure.core.util.Context.NONE);
    }
}
```

### Spacecrafts_GetByResourceGroup

```java
/** Samples for Spacecrafts GetByResourceGroup. */
public final class SpacecraftsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/orbital/resource-manager/Microsoft.Orbital/stable/2022-11-01/examples/SpacecraftGet.json
     */
    /**
     * Sample code: Get Spacecraft.
     *
     * @param manager Entry point to OrbitalManager.
     */
    public static void getSpacecraft(com.azure.resourcemanager.orbital.OrbitalManager manager) {
        manager
            .spacecrafts()
            .getByResourceGroupWithResponse("contoso-Rgp", "CONTOSO_SAT", com.azure.core.util.Context.NONE);
    }
}
```

### Spacecrafts_List

```java
/** Samples for Spacecrafts List. */
public final class SpacecraftsListSamples {
    /*
     * x-ms-original-file: specification/orbital/resource-manager/Microsoft.Orbital/stable/2022-11-01/examples/SpacecraftsBySubscriptionList.json
     */
    /**
     * Sample code: List of Spacecraft by Subscription.
     *
     * @param manager Entry point to OrbitalManager.
     */
    public static void listOfSpacecraftBySubscription(com.azure.resourcemanager.orbital.OrbitalManager manager) {
        manager.spacecrafts().list("opaqueString", com.azure.core.util.Context.NONE);
    }
}
```

### Spacecrafts_ListAvailableContacts

```java
import com.azure.resourcemanager.orbital.models.ContactParameters;
import com.azure.resourcemanager.orbital.models.ContactParametersContactProfile;
import java.time.OffsetDateTime;

/** Samples for Spacecrafts ListAvailableContacts. */
public final class SpacecraftsListAvailableContactsSamples {
    /*
     * x-ms-original-file: specification/orbital/resource-manager/Microsoft.Orbital/stable/2022-11-01/examples/AvailableContactsList.json
     */
    /**
     * Sample code: List of Contact.
     *
     * @param manager Entry point to OrbitalManager.
     */
    public static void listOfContact(com.azure.resourcemanager.orbital.OrbitalManager manager) {
        manager
            .spacecrafts()
            .listAvailableContacts(
                "contoso-Rgp",
                "CONTOSO_SAT",
                new ContactParameters()
                    .withContactProfile(
                        new ContactParametersContactProfile()
                            .withId(
                                "/subscriptions/c1be1141-a7c9-4aac-9608-3c2e2f1152c3/resourceGroups/contoso-Rgp/providers/Microsoft.Orbital/contactProfiles/CONTOSO-CP"))
                    .withGroundStationName("EASTUS2_0")
                    .withStartTime(OffsetDateTime.parse("2022-11-01T11:30:00Z"))
                    .withEndTime(OffsetDateTime.parse("2022-11-02T11:30:00Z")),
                com.azure.core.util.Context.NONE);
    }
}
```

### Spacecrafts_ListByResourceGroup

```java
/** Samples for Spacecrafts ListByResourceGroup. */
public final class SpacecraftsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/orbital/resource-manager/Microsoft.Orbital/stable/2022-11-01/examples/SpacecraftsByResourceGroupList.json
     */
    /**
     * Sample code: List of Spacecraft by Resource Group.
     *
     * @param manager Entry point to OrbitalManager.
     */
    public static void listOfSpacecraftByResourceGroup(com.azure.resourcemanager.orbital.OrbitalManager manager) {
        manager.spacecrafts().listByResourceGroup("contoso-Rgp", "opaqueString", com.azure.core.util.Context.NONE);
    }
}
```

### Spacecrafts_UpdateTags

```java
import com.azure.resourcemanager.orbital.models.Spacecraft;
import java.util.HashMap;
import java.util.Map;

/** Samples for Spacecrafts UpdateTags. */
public final class SpacecraftsUpdateTagsSamples {
    /*
     * x-ms-original-file: specification/orbital/resource-manager/Microsoft.Orbital/stable/2022-11-01/examples/SpacecraftUpdateTags.json
     */
    /**
     * Sample code: Update Spacecraft tags.
     *
     * @param manager Entry point to OrbitalManager.
     */
    public static void updateSpacecraftTags(com.azure.resourcemanager.orbital.OrbitalManager manager) {
        Spacecraft resource =
            manager
                .spacecrafts()
                .getByResourceGroupWithResponse("contoso-Rgp", "CONTOSO_SAT", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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


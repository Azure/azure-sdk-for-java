# Code snippets and samples


## AvailableGroundStations

- [Get](#availablegroundstations_get)
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

## Spacecrafts

- [CreateOrUpdate](#spacecrafts_createorupdate)
- [Delete](#spacecrafts_delete)
- [GetByResourceGroup](#spacecrafts_getbyresourcegroup)
- [List](#spacecrafts_list)
- [ListAvailableContacts](#spacecrafts_listavailablecontacts)
- [ListByResourceGroup](#spacecrafts_listbyresourcegroup)
- [UpdateTags](#spacecrafts_updatetags)
### AvailableGroundStations_Get

```java
import com.azure.core.util.Context;

/** Samples for AvailableGroundStations Get. */
public final class AvailableGroundStationsGetSamples {
    /*
     * x-ms-original-file: specification/orbital/resource-manager/Microsoft.Orbital/preview/2021-04-04-preview/examples/AvailableGroundStationGet.json
     */
    /**
     * Sample code: Get GroundStation.
     *
     * @param manager Entry point to OrbitalManager.
     */
    public static void getGroundStation(com.azure.resourcemanager.orbital.OrbitalManager manager) {
        manager.availableGroundStations().getWithResponse("westus_gs1", Context.NONE);
    }
}
```

### AvailableGroundStations_List

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.orbital.models.AvailableGroundStationsCapability;

/** Samples for AvailableGroundStations List. */
public final class AvailableGroundStationsListSamples {
    /*
     * x-ms-original-file: specification/orbital/resource-manager/Microsoft.Orbital/preview/2021-04-04-preview/examples/AvailableGroundStationsByCapabilityList.json
     */
    /**
     * Sample code: List of Ground Stations by Capability.
     *
     * @param manager Entry point to OrbitalManager.
     */
    public static void listOfGroundStationsByCapability(com.azure.resourcemanager.orbital.OrbitalManager manager) {
        manager.availableGroundStations().list(AvailableGroundStationsCapability.EARTH_OBSERVATION, Context.NONE);
    }
}
```

### ContactProfiles_CreateOrUpdate

```java
import com.azure.resourcemanager.orbital.models.AutoTrackingConfiguration;
import com.azure.resourcemanager.orbital.models.ContactProfileLink;
import com.azure.resourcemanager.orbital.models.ContactProfileLinkChannel;
import com.azure.resourcemanager.orbital.models.Direction;
import com.azure.resourcemanager.orbital.models.EndPoint;
import com.azure.resourcemanager.orbital.models.Polarization;
import com.azure.resourcemanager.orbital.models.Protocol;
import java.util.Arrays;

/** Samples for ContactProfiles CreateOrUpdate. */
public final class ContactProfilesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/orbital/resource-manager/Microsoft.Orbital/preview/2021-04-04-preview/examples/ContactProfileCreate.json
     */
    /**
     * Sample code: Create a contact profile.
     *
     * @param manager Entry point to OrbitalManager.
     */
    public static void createAContactProfile(com.azure.resourcemanager.orbital.OrbitalManager manager) {
        manager
            .contactProfiles()
            .define("AQUA_DIRECTPLAYBACK_WITH_UPLINK")
            .withRegion("westus")
            .withExistingResourceGroup("rg1")
            .withMinimumViableContactDuration("PT1M")
            .withMinimumElevationDegrees(10.0f)
            .withAutoTrackingConfiguration(AutoTrackingConfiguration.X_BAND)
            .withLinks(
                Arrays
                    .asList(
                        new ContactProfileLink()
                            .withPolarization(Polarization.RHCP)
                            .withDirection(Direction.UPLINK)
                            .withGainOverTemperature(0.0f)
                            .withEirpdBW(45.0f)
                            .withChannels(
                                Arrays
                                    .asList(
                                        new ContactProfileLinkChannel()
                                            .withCenterFrequencyMHz(2106.4063f)
                                            .withBandwidthMHz(0.036f)
                                            .withEndPoint(
                                                new EndPoint()
                                                    .withIpAddress("10.0.1.0")
                                                    .withEndPointName("AQUA_command")
                                                    .withPort("4000")
                                                    .withProtocol(Protocol.TCP))
                                            .withModulationConfiguration("AQUA_UPLINK_BPSK")
                                            .withDemodulationConfiguration("na")
                                            .withEncodingConfiguration("AQUA_CMD_CCSDS")
                                            .withDecodingConfiguration("na"))),
                        new ContactProfileLink()
                            .withPolarization(Polarization.RHCP)
                            .withDirection(Direction.DOWNLINK)
                            .withGainOverTemperature(25.0f)
                            .withEirpdBW(0.0f)
                            .withChannels(
                                Arrays
                                    .asList(
                                        new ContactProfileLinkChannel()
                                            .withCenterFrequencyMHz(8160f)
                                            .withBandwidthMHz(150f)
                                            .withEndPoint(
                                                new EndPoint()
                                                    .withIpAddress("10.0.2.0")
                                                    .withEndPointName("AQUA_directplayback")
                                                    .withPort("4000")
                                                    .withProtocol(Protocol.TCP))
                                            .withModulationConfiguration("na")
                                            .withDemodulationConfiguration("AQUA_DOWNLINK_QPSK")
                                            .withEncodingConfiguration("na")
                                            .withDecodingConfiguration("AQUA_DIRECTPLAYBACK_CCSDS")))))
            .create();
    }
}
```

### ContactProfiles_Delete

```java
import com.azure.core.util.Context;

/** Samples for ContactProfiles Delete. */
public final class ContactProfilesDeleteSamples {
    /*
     * x-ms-original-file: specification/orbital/resource-manager/Microsoft.Orbital/preview/2021-04-04-preview/examples/ContactProfileDelete.json
     */
    /**
     * Sample code: Delete Contact Profile.
     *
     * @param manager Entry point to OrbitalManager.
     */
    public static void deleteContactProfile(com.azure.resourcemanager.orbital.OrbitalManager manager) {
        manager.contactProfiles().delete("rg1", "AQUA_DIRECTPLAYBACK_WITH_UPLINK", Context.NONE);
    }
}
```

### ContactProfiles_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for ContactProfiles GetByResourceGroup. */
public final class ContactProfilesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/orbital/resource-manager/Microsoft.Orbital/preview/2021-04-04-preview/examples/ContactProfileGet.json
     */
    /**
     * Sample code: Get a contact profile.
     *
     * @param manager Entry point to OrbitalManager.
     */
    public static void getAContactProfile(com.azure.resourcemanager.orbital.OrbitalManager manager) {
        manager
            .contactProfiles()
            .getByResourceGroupWithResponse("rg1", "AQUA_DIRECTPLAYBACK_WITH_UPLINK", Context.NONE);
    }
}
```

### ContactProfiles_List

```java
import com.azure.core.util.Context;

/** Samples for ContactProfiles List. */
public final class ContactProfilesListSamples {
    /*
     * x-ms-original-file: specification/orbital/resource-manager/Microsoft.Orbital/preview/2021-04-04-preview/examples/ContactProfilesBySubscriptionList.json
     */
    /**
     * Sample code: List of Contact Profiles by Subscription.
     *
     * @param manager Entry point to OrbitalManager.
     */
    public static void listOfContactProfilesBySubscription(com.azure.resourcemanager.orbital.OrbitalManager manager) {
        manager.contactProfiles().list(Context.NONE);
    }
}
```

### ContactProfiles_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for ContactProfiles ListByResourceGroup. */
public final class ContactProfilesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/orbital/resource-manager/Microsoft.Orbital/preview/2021-04-04-preview/examples/ContactProfilesByResourceGroupList.json
     */
    /**
     * Sample code: List of Contact Profiles by Resource Group.
     *
     * @param manager Entry point to OrbitalManager.
     */
    public static void listOfContactProfilesByResourceGroup(com.azure.resourcemanager.orbital.OrbitalManager manager) {
        manager.contactProfiles().listByResourceGroup("rg1", Context.NONE);
    }
}
```

### ContactProfiles_UpdateTags

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.orbital.models.ContactProfile;
import java.util.HashMap;
import java.util.Map;

/** Samples for ContactProfiles UpdateTags. */
public final class ContactProfilesUpdateTagsSamples {
    /*
     * x-ms-original-file: specification/orbital/resource-manager/Microsoft.Orbital/preview/2021-04-04-preview/examples/ContactProfileUpdateTag.json
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
                .getByResourceGroupWithResponse("rg1", "AQUA_DIRECTPLAYBACK_WITH_UPLINK", Context.NONE)
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
import com.azure.resourcemanager.orbital.models.ResourceReference;
import java.time.OffsetDateTime;

/** Samples for Contacts Create. */
public final class ContactsCreateSamples {
    /*
     * x-ms-original-file: specification/orbital/resource-manager/Microsoft.Orbital/preview/2021-04-04-preview/examples/ContactCreate.json
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
            .withExistingSpacecraft("rg1", "AQUA")
            .withReservationStartTime(OffsetDateTime.parse("2020-07-16T20:35:00.00Z"))
            .withReservationEndTime(OffsetDateTime.parse("2020-07-16T20:55:00.00Z"))
            .withGroundStationName("westus_gs1")
            .withContactProfile(
                new ResourceReference()
                    .withId(
                        "/subscriptions/subId/resourceGroups/rg/Microsoft.Orbital/contactProfiles/AQUA_DIRECTPLAYBACK_WITH_UPLINK"))
            .create();
    }
}
```

### Contacts_Delete

```java
import com.azure.core.util.Context;

/** Samples for Contacts Delete. */
public final class ContactsDeleteSamples {
    /*
     * x-ms-original-file: specification/orbital/resource-manager/Microsoft.Orbital/preview/2021-04-04-preview/examples/ContactDelete.json
     */
    /**
     * Sample code: Delete Contact.
     *
     * @param manager Entry point to OrbitalManager.
     */
    public static void deleteContact(com.azure.resourcemanager.orbital.OrbitalManager manager) {
        manager.contacts().delete("rg1", "AQUA", "contact1", Context.NONE);
    }
}
```

### Contacts_Get

```java
import com.azure.core.util.Context;

/** Samples for Contacts Get. */
public final class ContactsGetSamples {
    /*
     * x-ms-original-file: specification/orbital/resource-manager/Microsoft.Orbital/preview/2021-04-04-preview/examples/ContactGet.json
     */
    /**
     * Sample code: Get Contact.
     *
     * @param manager Entry point to OrbitalManager.
     */
    public static void getContact(com.azure.resourcemanager.orbital.OrbitalManager manager) {
        manager.contacts().getWithResponse("rg1", "AQUA", "contact1", Context.NONE);
    }
}
```

### Contacts_List

```java
import com.azure.core.util.Context;

/** Samples for Contacts List. */
public final class ContactsListSamples {
    /*
     * x-ms-original-file: specification/orbital/resource-manager/Microsoft.Orbital/preview/2021-04-04-preview/examples/ContactsBySpacecraftNameList.json
     */
    /**
     * Sample code: List of Contacts.
     *
     * @param manager Entry point to OrbitalManager.
     */
    public static void listOfContacts(com.azure.resourcemanager.orbital.OrbitalManager manager) {
        manager.contacts().list("rg1", "AQUA", Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/orbital/resource-manager/Microsoft.Orbital/preview/2021-04-04-preview/examples/OperationsList.json
     */
    /**
     * Sample code: OperationsList.
     *
     * @param manager Entry point to OrbitalManager.
     */
    public static void operationsList(com.azure.resourcemanager.orbital.OrbitalManager manager) {
        manager.operations().list(Context.NONE);
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
     * x-ms-original-file: specification/orbital/resource-manager/Microsoft.Orbital/preview/2021-04-04-preview/examples/SpacecraftCreate.json
     */
    /**
     * Sample code: Create a spacecraft.
     *
     * @param manager Entry point to OrbitalManager.
     */
    public static void createASpacecraft(com.azure.resourcemanager.orbital.OrbitalManager manager) {
        manager
            .spacecrafts()
            .define("AQUA")
            .withRegion("westus")
            .withExistingResourceGroup("rg1")
            .withNoradId("27424")
            .withTitleLine("(AQUA)")
            .withTleLine1("1 27424U 02022A   20195.59202355  .00000039  00000-0  18634-4 0  9991")
            .withTleLine2("2 27424  98.2098 135.8486 0000176  28.4050 144.5909 14.57108832967671")
            .withLinks(
                Arrays
                    .asList(
                        new SpacecraftLink()
                            .withCenterFrequencyMHz(2106.4063f)
                            .withBandwidthMHz(0.036f)
                            .withDirection(Direction.UPLINK)
                            .withPolarization(Polarization.RHCP),
                        new SpacecraftLink()
                            .withCenterFrequencyMHz(8125f)
                            .withBandwidthMHz(150f)
                            .withDirection(Direction.DOWNLINK)
                            .withPolarization(Polarization.RHCP)))
            .create();
    }
}
```

### Spacecrafts_Delete

```java
import com.azure.core.util.Context;

/** Samples for Spacecrafts Delete. */
public final class SpacecraftsDeleteSamples {
    /*
     * x-ms-original-file: specification/orbital/resource-manager/Microsoft.Orbital/preview/2021-04-04-preview/examples/SpacecraftDelete.json
     */
    /**
     * Sample code: Delete Spacecraft.
     *
     * @param manager Entry point to OrbitalManager.
     */
    public static void deleteSpacecraft(com.azure.resourcemanager.orbital.OrbitalManager manager) {
        manager.spacecrafts().delete("rg1", "AQUA", Context.NONE);
    }
}
```

### Spacecrafts_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Spacecrafts GetByResourceGroup. */
public final class SpacecraftsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/orbital/resource-manager/Microsoft.Orbital/preview/2021-04-04-preview/examples/SpacecraftGet.json
     */
    /**
     * Sample code: Get Spacecraft.
     *
     * @param manager Entry point to OrbitalManager.
     */
    public static void getSpacecraft(com.azure.resourcemanager.orbital.OrbitalManager manager) {
        manager.spacecrafts().getByResourceGroupWithResponse("rg1", "AQUA", Context.NONE);
    }
}
```

### Spacecrafts_List

```java
import com.azure.core.util.Context;

/** Samples for Spacecrafts List. */
public final class SpacecraftsListSamples {
    /*
     * x-ms-original-file: specification/orbital/resource-manager/Microsoft.Orbital/preview/2021-04-04-preview/examples/SpacecraftsBySubscriptionList.json
     */
    /**
     * Sample code: List of Spacecraft by Subscription.
     *
     * @param manager Entry point to OrbitalManager.
     */
    public static void listOfSpacecraftBySubscription(com.azure.resourcemanager.orbital.OrbitalManager manager) {
        manager.spacecrafts().list(Context.NONE);
    }
}
```

### Spacecrafts_ListAvailableContacts

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.orbital.models.ContactParameters;
import com.azure.resourcemanager.orbital.models.ResourceReference;
import java.time.OffsetDateTime;

/** Samples for Spacecrafts ListAvailableContacts. */
public final class SpacecraftsListAvailableContactsSamples {
    /*
     * x-ms-original-file: specification/orbital/resource-manager/Microsoft.Orbital/preview/2021-04-04-preview/examples/AvailableContactsList.json
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
                "rgName",
                "AQUA",
                new ContactParameters()
                    .withContactProfile(
                        new ResourceReference()
                            .withId(
                                "/subscriptions/subId/resourceGroups/rg/Microsoft.Orbital/contactProfiles/AQUA_DIRECTPLAYBACK_WITH_UPLINK"))
                    .withGroundStationName("westus_gs1")
                    .withStartTime(OffsetDateTime.parse("2020-07-16T05:40:21.00Z"))
                    .withEndTime(OffsetDateTime.parse("2020-07-17T23:49:40.00Z")),
                Context.NONE);
    }
}
```

### Spacecrafts_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Spacecrafts ListByResourceGroup. */
public final class SpacecraftsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/orbital/resource-manager/Microsoft.Orbital/preview/2021-04-04-preview/examples/SpacecraftsByResourceGroupList.json
     */
    /**
     * Sample code: List of Spacecraft by Resource Group.
     *
     * @param manager Entry point to OrbitalManager.
     */
    public static void listOfSpacecraftByResourceGroup(com.azure.resourcemanager.orbital.OrbitalManager manager) {
        manager.spacecrafts().listByResourceGroup("rg1", Context.NONE);
    }
}
```

### Spacecrafts_UpdateTags

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.orbital.models.Spacecraft;
import java.util.HashMap;
import java.util.Map;

/** Samples for Spacecrafts UpdateTags. */
public final class SpacecraftsUpdateTagsSamples {
    /*
     * x-ms-original-file: specification/orbital/resource-manager/Microsoft.Orbital/preview/2021-04-04-preview/examples/SpacecraftUpdateTags.json
     */
    /**
     * Sample code: Update Spacecraft tags.
     *
     * @param manager Entry point to OrbitalManager.
     */
    public static void updateSpacecraftTags(com.azure.resourcemanager.orbital.OrbitalManager manager) {
        Spacecraft resource =
            manager.spacecrafts().getByResourceGroupWithResponse("rg1", "AQUA", Context.NONE).getValue();
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


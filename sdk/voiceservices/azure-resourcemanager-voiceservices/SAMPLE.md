# Code snippets and samples


## CommunicationsGateways

- [CreateOrUpdate](#communicationsgateways_createorupdate)
- [Delete](#communicationsgateways_delete)
- [GetByResourceGroup](#communicationsgateways_getbyresourcegroup)
- [List](#communicationsgateways_list)
- [ListByResourceGroup](#communicationsgateways_listbyresourcegroup)
- [Update](#communicationsgateways_update)

## Contacts

- [CreateOrUpdate](#contacts_createorupdate)
- [Delete](#contacts_delete)
- [Get](#contacts_get)
- [ListByCommunicationsGateway](#contacts_listbycommunicationsgateway)
- [Update](#contacts_update)

## Operations

- [List](#operations_list)

## TestLines

- [CreateOrUpdate](#testlines_createorupdate)
- [Delete](#testlines_delete)
- [Get](#testlines_get)
- [ListByCommunicationsGateway](#testlines_listbycommunicationsgateway)
- [Update](#testlines_update)
### CommunicationsGateways_CreateOrUpdate

```java
import com.azure.resourcemanager.voiceservices.models.CommunicationsPlatform;
import com.azure.resourcemanager.voiceservices.models.Connectivity;
import com.azure.resourcemanager.voiceservices.models.E911Type;
import com.azure.resourcemanager.voiceservices.models.PrimaryRegionProperties;
import com.azure.resourcemanager.voiceservices.models.ServiceRegionProperties;
import com.azure.resourcemanager.voiceservices.models.TeamsCodecs;
import java.util.Arrays;

/** Samples for CommunicationsGateways CreateOrUpdate. */
public final class CommunicationsGatewaysCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/voiceservices/resource-manager/Microsoft.VoiceServices/preview/2022-12-01-preview/examples/CommunicationsGateways_CreateOrUpdate.json
     */
    /**
     * Sample code: CreateCommunicationsGatewayResource.
     *
     * @param manager Entry point to VoiceservicesManager.
     */
    public static void createCommunicationsGatewayResource(
        com.azure.resourcemanager.voiceservices.VoiceservicesManager manager) {
        manager
            .communicationsGateways()
            .define("myname")
            .withRegion("useast")
            .withExistingResourceGroup("testrg")
            .withServiceLocations(
                Arrays
                    .asList(
                        new ServiceRegionProperties()
                            .withName("useast")
                            .withPrimaryRegionProperties(
                                new PrimaryRegionProperties().withOperatorAddresses(Arrays.asList("198.51.100.1"))),
                        new ServiceRegionProperties()
                            .withName("useast2")
                            .withPrimaryRegionProperties(
                                new PrimaryRegionProperties().withOperatorAddresses(Arrays.asList("198.51.100.2")))))
            .withConnectivity(Connectivity.PUBLIC_ADDRESS)
            .withCodecs(Arrays.asList(TeamsCodecs.PCMA))
            .withE911Type(E911Type.STANDARD)
            .withPlatforms(Arrays.asList(CommunicationsPlatform.OPERATOR_CONNECT))
            .create();
    }
}
```

### CommunicationsGateways_Delete

```java
import com.azure.core.util.Context;

/** Samples for CommunicationsGateways Delete. */
public final class CommunicationsGatewaysDeleteSamples {
    /*
     * x-ms-original-file: specification/voiceservices/resource-manager/Microsoft.VoiceServices/preview/2022-12-01-preview/examples/CommunicationsGateways_Delete.json
     */
    /**
     * Sample code: DeleteCommunicationsGatewayResource.
     *
     * @param manager Entry point to VoiceservicesManager.
     */
    public static void deleteCommunicationsGatewayResource(
        com.azure.resourcemanager.voiceservices.VoiceservicesManager manager) {
        manager.communicationsGateways().delete("testrg", "myname", Context.NONE);
    }
}
```

### CommunicationsGateways_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for CommunicationsGateways GetByResourceGroup. */
public final class CommunicationsGatewaysGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/voiceservices/resource-manager/Microsoft.VoiceServices/preview/2022-12-01-preview/examples/CommunicationsGateways_Get.json
     */
    /**
     * Sample code: GetCommunicationsGatewayResource.
     *
     * @param manager Entry point to VoiceservicesManager.
     */
    public static void getCommunicationsGatewayResource(
        com.azure.resourcemanager.voiceservices.VoiceservicesManager manager) {
        manager.communicationsGateways().getByResourceGroupWithResponse("testrg", "myname", Context.NONE);
    }
}
```

### CommunicationsGateways_List

```java
import com.azure.core.util.Context;

/** Samples for CommunicationsGateways List. */
public final class CommunicationsGatewaysListSamples {
    /*
     * x-ms-original-file: specification/voiceservices/resource-manager/Microsoft.VoiceServices/preview/2022-12-01-preview/examples/CommunicationsGateways_ListBySubscription.json
     */
    /**
     * Sample code: ListCommunicationsGatewayResourceSub.
     *
     * @param manager Entry point to VoiceservicesManager.
     */
    public static void listCommunicationsGatewayResourceSub(
        com.azure.resourcemanager.voiceservices.VoiceservicesManager manager) {
        manager.communicationsGateways().list(Context.NONE);
    }
}
```

### CommunicationsGateways_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for CommunicationsGateways ListByResourceGroup. */
public final class CommunicationsGatewaysListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/voiceservices/resource-manager/Microsoft.VoiceServices/preview/2022-12-01-preview/examples/CommunicationsGateways_ListByResourceGroup.json
     */
    /**
     * Sample code: ListCommunicationsGatewayResource.
     *
     * @param manager Entry point to VoiceservicesManager.
     */
    public static void listCommunicationsGatewayResource(
        com.azure.resourcemanager.voiceservices.VoiceservicesManager manager) {
        manager.communicationsGateways().listByResourceGroup("testrg", Context.NONE);
    }
}
```

### CommunicationsGateways_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.voiceservices.models.CommunicationsGateway;

/** Samples for CommunicationsGateways Update. */
public final class CommunicationsGatewaysUpdateSamples {
    /*
     * x-ms-original-file: specification/voiceservices/resource-manager/Microsoft.VoiceServices/preview/2022-12-01-preview/examples/CommunicationsGateways_Update.json
     */
    /**
     * Sample code: UpdateCommunicationsGatewayResource.
     *
     * @param manager Entry point to VoiceservicesManager.
     */
    public static void updateCommunicationsGatewayResource(
        com.azure.resourcemanager.voiceservices.VoiceservicesManager manager) {
        CommunicationsGateway resource =
            manager
                .communicationsGateways()
                .getByResourceGroupWithResponse("testrg", "myname", Context.NONE)
                .getValue();
        resource.update().apply();
    }
}
```

### Contacts_CreateOrUpdate

```java
/** Samples for Contacts CreateOrUpdate. */
public final class ContactsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/voiceservices/resource-manager/Microsoft.VoiceServices/preview/2022-12-01-preview/examples/Contacts_CreateOrUpdate.json
     */
    /**
     * Sample code: CreateContactResource.
     *
     * @param manager Entry point to VoiceservicesManager.
     */
    public static void createContactResource(com.azure.resourcemanager.voiceservices.VoiceservicesManager manager) {
        manager
            .contacts()
            .define("name2")
            .withRegion("useast")
            .withExistingCommunicationsGateway("testrg", "myname")
            .withContactName("John Smith")
            .withPhoneNumber("+1-555-1234")
            .withEmail("johnsmith@example.com")
            .withRole("Network Manager")
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
     * x-ms-original-file: specification/voiceservices/resource-manager/Microsoft.VoiceServices/preview/2022-12-01-preview/examples/Contacts_Delete.json
     */
    /**
     * Sample code: DeleteContactResource.
     *
     * @param manager Entry point to VoiceservicesManager.
     */
    public static void deleteContactResource(com.azure.resourcemanager.voiceservices.VoiceservicesManager manager) {
        manager.contacts().delete("testrg", "myname", "myline", Context.NONE);
    }
}
```

### Contacts_Get

```java
import com.azure.core.util.Context;

/** Samples for Contacts Get. */
public final class ContactsGetSamples {
    /*
     * x-ms-original-file: specification/voiceservices/resource-manager/Microsoft.VoiceServices/preview/2022-12-01-preview/examples/Contacts_Get.json
     */
    /**
     * Sample code: GetContactResource.
     *
     * @param manager Entry point to VoiceservicesManager.
     */
    public static void getContactResource(com.azure.resourcemanager.voiceservices.VoiceservicesManager manager) {
        manager.contacts().getWithResponse("testrg", "myname", "name2", Context.NONE);
    }
}
```

### Contacts_ListByCommunicationsGateway

```java
import com.azure.core.util.Context;

/** Samples for Contacts ListByCommunicationsGateway. */
public final class ContactsListByCommunicationsGatewaySamples {
    /*
     * x-ms-original-file: specification/voiceservices/resource-manager/Microsoft.VoiceServices/preview/2022-12-01-preview/examples/Contacts_ListByCommunicationsGateway.json
     */
    /**
     * Sample code: ListContactsResource.
     *
     * @param manager Entry point to VoiceservicesManager.
     */
    public static void listContactsResource(com.azure.resourcemanager.voiceservices.VoiceservicesManager manager) {
        manager.contacts().listByCommunicationsGateway("testrg", "myname", Context.NONE);
    }
}
```

### Contacts_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.voiceservices.models.Contact;

/** Samples for Contacts Update. */
public final class ContactsUpdateSamples {
    /*
     * x-ms-original-file: specification/voiceservices/resource-manager/Microsoft.VoiceServices/preview/2022-12-01-preview/examples/Contacts_Update.json
     */
    /**
     * Sample code: UpdateContactResource.
     *
     * @param manager Entry point to VoiceservicesManager.
     */
    public static void updateContactResource(com.azure.resourcemanager.voiceservices.VoiceservicesManager manager) {
        Contact resource = manager.contacts().getWithResponse("testrg", "myname", "name2", Context.NONE).getValue();
        resource.update().apply();
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/voiceservices/resource-manager/Microsoft.VoiceServices/preview/2022-12-01-preview/examples/Operations_List.json
     */
    /**
     * Sample code: OperationsList.
     *
     * @param manager Entry point to VoiceservicesManager.
     */
    public static void operationsList(com.azure.resourcemanager.voiceservices.VoiceservicesManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### TestLines_CreateOrUpdate

```java
import com.azure.resourcemanager.voiceservices.models.TestLinePurpose;

/** Samples for TestLines CreateOrUpdate. */
public final class TestLinesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/voiceservices/resource-manager/Microsoft.VoiceServices/preview/2022-12-01-preview/examples/TestLines_CreateOrUpdate.json
     */
    /**
     * Sample code: CreateTestLineResource.
     *
     * @param manager Entry point to VoiceservicesManager.
     */
    public static void createTestLineResource(com.azure.resourcemanager.voiceservices.VoiceservicesManager manager) {
        manager
            .testLines()
            .define("myline")
            .withRegion("useast")
            .withExistingCommunicationsGateway("testrg", "myname")
            .withPhoneNumber("+1-555-1234")
            .withPurpose(TestLinePurpose.AUTOMATED)
            .create();
    }
}
```

### TestLines_Delete

```java
import com.azure.core.util.Context;

/** Samples for TestLines Delete. */
public final class TestLinesDeleteSamples {
    /*
     * x-ms-original-file: specification/voiceservices/resource-manager/Microsoft.VoiceServices/preview/2022-12-01-preview/examples/TestLines_Delete.json
     */
    /**
     * Sample code: DeleteTestLineResource.
     *
     * @param manager Entry point to VoiceservicesManager.
     */
    public static void deleteTestLineResource(com.azure.resourcemanager.voiceservices.VoiceservicesManager manager) {
        manager.testLines().delete("testrg", "myname", "myline", Context.NONE);
    }
}
```

### TestLines_Get

```java
import com.azure.core.util.Context;

/** Samples for TestLines Get. */
public final class TestLinesGetSamples {
    /*
     * x-ms-original-file: specification/voiceservices/resource-manager/Microsoft.VoiceServices/preview/2022-12-01-preview/examples/TestLines_Get.json
     */
    /**
     * Sample code: GetTestLineResource.
     *
     * @param manager Entry point to VoiceservicesManager.
     */
    public static void getTestLineResource(com.azure.resourcemanager.voiceservices.VoiceservicesManager manager) {
        manager.testLines().getWithResponse("testrg", "myname", "myline", Context.NONE);
    }
}
```

### TestLines_ListByCommunicationsGateway

```java
import com.azure.core.util.Context;

/** Samples for TestLines ListByCommunicationsGateway. */
public final class TestLinesListByCommunicationsGatewaySamples {
    /*
     * x-ms-original-file: specification/voiceservices/resource-manager/Microsoft.VoiceServices/preview/2022-12-01-preview/examples/TestLines_ListByCommunicationsGateway.json
     */
    /**
     * Sample code: ListTestLineResource.
     *
     * @param manager Entry point to VoiceservicesManager.
     */
    public static void listTestLineResource(com.azure.resourcemanager.voiceservices.VoiceservicesManager manager) {
        manager.testLines().listByCommunicationsGateway("testrg", "myname", Context.NONE);
    }
}
```

### TestLines_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.voiceservices.models.TestLine;

/** Samples for TestLines Update. */
public final class TestLinesUpdateSamples {
    /*
     * x-ms-original-file: specification/voiceservices/resource-manager/Microsoft.VoiceServices/preview/2022-12-01-preview/examples/TestLines_Update.json
     */
    /**
     * Sample code: UpdateTestLineResource.
     *
     * @param manager Entry point to VoiceservicesManager.
     */
    public static void updateTestLineResource(com.azure.resourcemanager.voiceservices.VoiceservicesManager manager) {
        TestLine resource = manager.testLines().getWithResponse("testrg", "myname", "myline", Context.NONE).getValue();
        resource.update().apply();
    }
}
```


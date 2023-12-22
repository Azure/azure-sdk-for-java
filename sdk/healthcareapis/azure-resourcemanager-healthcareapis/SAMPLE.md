# Code snippets and samples


## DicomServices

- [CreateOrUpdate](#dicomservices_createorupdate)
- [Delete](#dicomservices_delete)
- [Get](#dicomservices_get)
- [ListByWorkspace](#dicomservices_listbyworkspace)
- [Update](#dicomservices_update)

## FhirDestinations

- [ListByIotConnector](#fhirdestinations_listbyiotconnector)

## FhirServices

- [CreateOrUpdate](#fhirservices_createorupdate)
- [Delete](#fhirservices_delete)
- [Get](#fhirservices_get)
- [ListByWorkspace](#fhirservices_listbyworkspace)
- [Update](#fhirservices_update)

## IotConnectorFhirDestination

- [CreateOrUpdate](#iotconnectorfhirdestination_createorupdate)
- [Delete](#iotconnectorfhirdestination_delete)
- [Get](#iotconnectorfhirdestination_get)

## IotConnectors

- [CreateOrUpdate](#iotconnectors_createorupdate)
- [Delete](#iotconnectors_delete)
- [Get](#iotconnectors_get)
- [ListByWorkspace](#iotconnectors_listbyworkspace)
- [Update](#iotconnectors_update)

## OperationResults

- [Get](#operationresults_get)

## Operations

- [List](#operations_list)

## PrivateEndpointConnections

- [CreateOrUpdate](#privateendpointconnections_createorupdate)
- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [ListByService](#privateendpointconnections_listbyservice)

## PrivateLinkResources

- [Get](#privatelinkresources_get)
- [ListByService](#privatelinkresources_listbyservice)

## Services

- [CheckNameAvailability](#services_checknameavailability)
- [CreateOrUpdate](#services_createorupdate)
- [Delete](#services_delete)
- [GetByResourceGroup](#services_getbyresourcegroup)
- [List](#services_list)
- [ListByResourceGroup](#services_listbyresourcegroup)
- [Update](#services_update)

## WorkspacePrivateEndpointConnections

- [CreateOrUpdate](#workspaceprivateendpointconnections_createorupdate)
- [Delete](#workspaceprivateendpointconnections_delete)
- [Get](#workspaceprivateendpointconnections_get)
- [ListByWorkspace](#workspaceprivateendpointconnections_listbyworkspace)

## WorkspacePrivateLinkResources

- [Get](#workspaceprivatelinkresources_get)
- [ListByWorkspace](#workspaceprivatelinkresources_listbyworkspace)

## Workspaces

- [CreateOrUpdate](#workspaces_createorupdate)
- [Delete](#workspaces_delete)
- [GetByResourceGroup](#workspaces_getbyresourcegroup)
- [List](#workspaces_list)
- [ListByResourceGroup](#workspaces_listbyresourcegroup)
- [Update](#workspaces_update)
### DicomServices_CreateOrUpdate

```java
/**
 * Samples for DicomServices CreateOrUpdate.
 */
public final class DicomServicesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/dicomservices/DicomServices_Create.json
     */
    /**
     * Sample code: Create or update a Dicom Service.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void createOrUpdateADicomService(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.dicomServices().define("blue").withExistingWorkspace("testRG", "workspace1").withRegion("westus").create();
    }
}
```

### DicomServices_Delete

```java
/**
 * Samples for DicomServices Delete.
 */
public final class DicomServicesDeleteSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/dicomservices/DicomServices_Delete.json
     */
    /**
     * Sample code: Delete a dicomservice.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void deleteADicomservice(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.dicomServices().delete("testRG", "blue", "workspace1", com.azure.core.util.Context.NONE);
    }
}
```

### DicomServices_Get

```java
/**
 * Samples for DicomServices Get.
 */
public final class DicomServicesGetSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/dicomservices/DicomServices_Get.json
     */
    /**
     * Sample code: Get a dicomservice.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void getADicomservice(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.dicomServices().getWithResponse("testRG", "workspace1", "blue", com.azure.core.util.Context.NONE);
    }
}
```

### DicomServices_ListByWorkspace

```java
/**
 * Samples for DicomServices ListByWorkspace.
 */
public final class DicomServicesListByWorkspaceSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/dicomservices/DicomServices_List.json
     */
    /**
     * Sample code: List dicomservices.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void listDicomservices(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.dicomServices().listByWorkspace("testRG", "workspace1", com.azure.core.util.Context.NONE);
    }
}
```

### DicomServices_Update

```java
import com.azure.resourcemanager.healthcareapis.models.DicomService;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for DicomServices Update.
 */
public final class DicomServicesUpdateSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/dicomservices/DicomServices_Patch.json
     */
    /**
     * Sample code: Update a dicomservice.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void updateADicomservice(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        DicomService resource = manager.dicomServices().getWithResponse("testRG", "workspace1", "blue", com.azure.core.util.Context.NONE).getValue();
        resource.update().withTags(mapOf("tagKey", "fakeTokenPlaceholder")).apply();
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

### FhirDestinations_ListByIotConnector

```java
/**
 * Samples for FhirDestinations ListByIotConnector.
 */
public final class FhirDestinationsListByIotConnectorSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/iotconnectors/iotconnector_fhirdestination_List.json
     */
    /**
     * Sample code: List IoT Connectors.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void listIoTConnectors(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.fhirDestinations().listByIotConnector("testRG", "workspace1", "blue", com.azure.core.util.Context.NONE);
    }
}
```

### FhirServices_CreateOrUpdate

```java
import com.azure.resourcemanager.healthcareapis.models.Encryption;
import com.azure.resourcemanager.healthcareapis.models.EncryptionCustomerManagedKeyEncryption;
import com.azure.resourcemanager.healthcareapis.models.FhirServiceAcrConfiguration;
import com.azure.resourcemanager.healthcareapis.models.FhirServiceAuthenticationConfiguration;
import com.azure.resourcemanager.healthcareapis.models.FhirServiceCorsConfiguration;
import com.azure.resourcemanager.healthcareapis.models.FhirServiceExportConfiguration;
import com.azure.resourcemanager.healthcareapis.models.FhirServiceImportConfiguration;
import com.azure.resourcemanager.healthcareapis.models.FhirServiceKind;
import com.azure.resourcemanager.healthcareapis.models.ImplementationGuidesConfiguration;
import com.azure.resourcemanager.healthcareapis.models.ServiceManagedIdentityIdentity;
import com.azure.resourcemanager.healthcareapis.models.ServiceManagedIdentityType;
import com.azure.resourcemanager.healthcareapis.models.UserAssignedIdentity;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Samples for FhirServices CreateOrUpdate.
 */
public final class FhirServicesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/fhirservices/FhirServices_Create.json
     */
    /**
     * Sample code: Create or update a Fhir Service.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void createOrUpdateAFhirService(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.fhirServices().define("fhirservice1").withExistingWorkspace("testRG", "workspace1").withRegion("westus").withTags(mapOf("additionalProp1", "string", "additionalProp2", "string", "additionalProp3", "string")).withKind(FhirServiceKind.FHIR_R4).withIdentity(new ServiceManagedIdentityIdentity().withType(ServiceManagedIdentityType.USER_ASSIGNED).withUserAssignedIdentities(mapOf("/subscriptions/subid/resourcegroups/testRG/providers/Microsoft.ManagedIdentity/userAssignedIdentities/test-mi", new UserAssignedIdentity()))).withAcrConfiguration(new FhirServiceAcrConfiguration().withLoginServers(Arrays.asList("test1.azurecr.io"))).withAuthenticationConfiguration(new FhirServiceAuthenticationConfiguration().withAuthority("https://login.microsoftonline.com/abfde7b2-df0f-47e6-aabf-2462b07508dc").withAudience("https://azurehealthcareapis.com").withSmartProxyEnabled(true)).withCorsConfiguration(new FhirServiceCorsConfiguration().withOrigins(Arrays.asList("*")).withHeaders(Arrays.asList("*")).withMethods(Arrays.asList("DELETE", "GET", "OPTIONS", "PATCH", "POST", "PUT")).withMaxAge(1440).withAllowCredentials(false)).withExportConfiguration(new FhirServiceExportConfiguration().withStorageAccountName("existingStorageAccount")).withImportConfiguration(new FhirServiceImportConfiguration().withIntegrationDataStore("existingStorageAccount").withInitialImportMode(false).withEnabled(false)).withImplementationGuidesConfiguration(new ImplementationGuidesConfiguration().withUsCoreMissingData(false)).withEncryption(new Encryption().withCustomerManagedKeyEncryption(new EncryptionCustomerManagedKeyEncryption().withKeyEncryptionKeyUrl("fakeTokenPlaceholder"))).create();
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

### FhirServices_Delete

```java
/**
 * Samples for FhirServices Delete.
 */
public final class FhirServicesDeleteSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/fhirservices/FhirServices_Delete.json
     */
    /**
     * Sample code: Delete a Fhir Service.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void deleteAFhirService(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.fhirServices().delete("testRG", "fhirservice1", "workspace1", com.azure.core.util.Context.NONE);
    }
}
```

### FhirServices_Get

```java
/**
 * Samples for FhirServices Get.
 */
public final class FhirServicesGetSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/fhirservices/FhirServices_Get.json
     */
    /**
     * Sample code: Get a Fhir Service.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void getAFhirService(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.fhirServices().getWithResponse("testRG", "workspace1", "fhirservices1", com.azure.core.util.Context.NONE);
    }
}
```

### FhirServices_ListByWorkspace

```java
/**
 * Samples for FhirServices ListByWorkspace.
 */
public final class FhirServicesListByWorkspaceSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/fhirservices/FhirServices_List.json
     */
    /**
     * Sample code: List fhirservices.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void listFhirservices(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.fhirServices().listByWorkspace("testRG", "workspace1", com.azure.core.util.Context.NONE);
    }
}
```

### FhirServices_Update

```java
import com.azure.resourcemanager.healthcareapis.models.FhirService;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for FhirServices Update.
 */
public final class FhirServicesUpdateSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/fhirservices/FhirServices_Patch.json
     */
    /**
     * Sample code: Update a Fhir Service.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void updateAFhirService(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        FhirService resource = manager.fhirServices().getWithResponse("testRG", "workspace1", "fhirservice1", com.azure.core.util.Context.NONE).getValue();
        resource.update().withTags(mapOf("tagKey", "fakeTokenPlaceholder")).apply();
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

### IotConnectorFhirDestination_CreateOrUpdate

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.healthcareapis.models.IotIdentityResolutionType;
import com.azure.resourcemanager.healthcareapis.models.IotMappingProperties;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Samples for IotConnectorFhirDestination CreateOrUpdate.
 */
public final class IotConnectorFhirDestinationCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/iotconnectors/iotconnector_fhirdestination_Create.json
     */
    /**
     * Sample code: Create or update an Iot Connector FHIR destination.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void createOrUpdateAnIotConnectorFHIRDestination(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) throws IOException {
        manager.iotConnectorFhirDestinations().define("dest1").withExistingIotconnector("testRG", "workspace1", "blue").withResourceIdentityResolutionType(IotIdentityResolutionType.CREATE).withFhirServiceResourceId("subscriptions/11111111-2222-3333-4444-555566667777/resourceGroups/myrg/providers/Microsoft.HealthcareApis/workspaces/myworkspace/fhirservices/myfhirservice").withFhirMapping(new IotMappingProperties().withContent(SerializerFactory.createDefaultManagementSerializerAdapter().deserialize("{\"template\":[{\"template\":{\"codes\":[{\"code\":\"8867-4\",\"display\":\"Heart rate\",\"system\":\"http://loinc.org\"}],\"periodInterval\":60,\"typeName\":\"heartrate\",\"value\":{\"defaultPeriod\":5000,\"unit\":\"count/min\",\"valueName\":\"hr\",\"valueType\":\"SampledData\"}},\"templateType\":\"CodeValueFhir\"}],\"templateType\":\"CollectionFhirTemplate\"}", Object.class, SerializerEncoding.JSON))).withRegion("westus").create();
    }
}
```

### IotConnectorFhirDestination_Delete

```java
/**
 * Samples for IotConnectorFhirDestination Delete.
 */
public final class IotConnectorFhirDestinationDeleteSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/iotconnectors/iotconnector_fhirdestination_Delete.json
     */
    /**
     * Sample code: Delete an IoT Connector destination.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void deleteAnIoTConnectorDestination(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.iotConnectorFhirDestinations().delete("testRG", "workspace1", "blue", "dest1", com.azure.core.util.Context.NONE);
    }
}
```

### IotConnectorFhirDestination_Get

```java
/**
 * Samples for IotConnectorFhirDestination Get.
 */
public final class IotConnectorFhirDestinationGetSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/iotconnectors/iotconnector_fhirdestination_Get.json
     */
    /**
     * Sample code: Get an IoT Connector destination.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void getAnIoTConnectorDestination(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.iotConnectorFhirDestinations().getWithResponse("testRG", "workspace1", "blue", "dest1", com.azure.core.util.Context.NONE);
    }
}
```

### IotConnectors_CreateOrUpdate

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.healthcareapis.models.IotEventHubIngestionEndpointConfiguration;
import com.azure.resourcemanager.healthcareapis.models.IotMappingProperties;
import com.azure.resourcemanager.healthcareapis.models.ServiceManagedIdentityIdentity;
import com.azure.resourcemanager.healthcareapis.models.ServiceManagedIdentityType;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Samples for IotConnectors CreateOrUpdate.
 */
public final class IotConnectorsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/iotconnectors/iotconnector_Create.json
     */
    /**
     * Sample code: Create an IoT Connector.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void createAnIoTConnector(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) throws IOException {
        manager.iotConnectors().define("blue").withExistingWorkspace("testRG", "workspace1").withRegion("westus").withTags(mapOf("additionalProp1", "string", "additionalProp2", "string", "additionalProp3", "string")).withIdentity(new ServiceManagedIdentityIdentity().withType(ServiceManagedIdentityType.SYSTEM_ASSIGNED)).withIngestionEndpointConfiguration(new IotEventHubIngestionEndpointConfiguration().withEventHubName("MyEventHubName").withConsumerGroup("ConsumerGroupA").withFullyQualifiedEventHubNamespace("myeventhub.servicesbus.windows.net")).withDeviceMapping(new IotMappingProperties().withContent(SerializerFactory.createDefaultManagementSerializerAdapter().deserialize("{\"template\":[{\"template\":{\"deviceIdExpression\":\"$.deviceid\",\"timestampExpression\":\"$.measurementdatetime\",\"typeMatchExpression\":\"$..[?(@heartrate)]\",\"typeName\":\"heartrate\",\"values\":[{\"required\":\"true\",\"valueExpression\":\"$.heartrate\",\"valueName\":\"hr\"}]},\"templateType\":\"JsonPathContent\"}],\"templateType\":\"CollectionContent\"}", Object.class, SerializerEncoding.JSON))).create();
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

### IotConnectors_Delete

```java
/**
 * Samples for IotConnectors Delete.
 */
public final class IotConnectorsDeleteSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/iotconnectors/iotconnector_Delete.json
     */
    /**
     * Sample code: Delete an IoT Connector.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void deleteAnIoTConnector(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.iotConnectors().delete("testRG", "blue", "workspace1", com.azure.core.util.Context.NONE);
    }
}
```

### IotConnectors_Get

```java
/**
 * Samples for IotConnectors Get.
 */
public final class IotConnectorsGetSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/iotconnectors/iotconnector_Get.json
     */
    /**
     * Sample code: Get an IoT Connector.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void getAnIoTConnector(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.iotConnectors().getWithResponse("testRG", "workspace1", "blue", com.azure.core.util.Context.NONE);
    }
}
```

### IotConnectors_ListByWorkspace

```java
/**
 * Samples for IotConnectors ListByWorkspace.
 */
public final class IotConnectorsListByWorkspaceSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/iotconnectors/iotconnector_List.json
     */
    /**
     * Sample code: List iotconnectors.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void listIotconnectors(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.iotConnectors().listByWorkspace("testRG", "workspace1", com.azure.core.util.Context.NONE);
    }
}
```

### IotConnectors_Update

```java
import com.azure.resourcemanager.healthcareapis.models.IotConnector;
import com.azure.resourcemanager.healthcareapis.models.ServiceManagedIdentityIdentity;
import com.azure.resourcemanager.healthcareapis.models.ServiceManagedIdentityType;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Samples for IotConnectors Update.
 */
public final class IotConnectorsUpdateSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/iotconnectors/iotconnector_Patch.json
     */
    /**
     * Sample code: Patch an IoT Connector.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void patchAnIoTConnector(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        IotConnector resource = manager.iotConnectors().getWithResponse("testRG", "workspace1", "blue", com.azure.core.util.Context.NONE).getValue();
        resource.update().withTags(mapOf("additionalProp1", "string", "additionalProp2", "string", "additionalProp3", "string")).withIdentity(new ServiceManagedIdentityIdentity().withType(ServiceManagedIdentityType.SYSTEM_ASSIGNED)).apply();
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

### OperationResults_Get

```java
/**
 * Samples for OperationResults Get.
 */
public final class OperationResultsGetSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/OperationResultsGet.json
     */
    /**
     * Sample code: Get operation result.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void getOperationResult(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.operationResults().getWithResponse("westus", "exampleid", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/OperationsList.json
     */
    /**
     * Sample code: List operations.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void listOperations(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_CreateOrUpdate

```java
import com.azure.resourcemanager.healthcareapis.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.healthcareapis.models.PrivateLinkServiceConnectionState;
import java.util.stream.Collectors;

/**
 * Samples for PrivateEndpointConnections CreateOrUpdate.
 */
public final class PrivateEndpointConnectionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/legacy/ServiceCreatePrivateEndpointConnection.json
     */
    /**
     * Sample code: PrivateEndpointConnection_CreateOrUpdate.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void privateEndpointConnectionCreateOrUpdate(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.privateEndpointConnections().define("myConnection").withExistingService("rgname", "service1").withPrivateLinkServiceConnectionState(new PrivateLinkServiceConnectionState().withStatus(PrivateEndpointServiceConnectionStatus.APPROVED).withDescription("Auto-Approved")).create();
    }
}
```

### PrivateEndpointConnections_Delete

```java
/**
 * Samples for PrivateEndpointConnections Delete.
 */
public final class PrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/legacy/ServiceDeletePrivateEndpointConnection.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Delete.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void privateEndpointConnectionsDelete(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.privateEndpointConnections().delete("rgname", "service1", "myConnection", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
/**
 * Samples for PrivateEndpointConnections Get.
 */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/legacy/ServiceGetPrivateEndpointConnection.json
     */
    /**
     * Sample code: PrivateEndpointConnection_GetConnection.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void privateEndpointConnectionGetConnection(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.privateEndpointConnections().getWithResponse("rgname", "service1", "myConnection", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_ListByService

```java
/**
 * Samples for PrivateEndpointConnections ListByService.
 */
public final class PrivateEndpointConnectionsListByServiceSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/legacy/ServiceListPrivateEndpointConnections.json
     */
    /**
     * Sample code: PrivateEndpointConnection_List.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void privateEndpointConnectionList(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.privateEndpointConnections().listByService("rgname", "service1", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_Get

```java
/**
 * Samples for PrivateLinkResources Get.
 */
public final class PrivateLinkResourcesGetSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/legacy/PrivateLinkResourceGet.json
     */
    /**
     * Sample code: PrivateLinkResources_Get.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void privateLinkResourcesGet(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.privateLinkResources().getWithResponse("rgname", "service1", "fhir", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_ListByService

```java
/**
 * Samples for PrivateLinkResources ListByService.
 */
public final class PrivateLinkResourcesListByServiceSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/legacy/PrivateLinkResourcesListByService.json
     */
    /**
     * Sample code: PrivateLinkResources_ListGroupIds.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void privateLinkResourcesListGroupIds(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.privateLinkResources().listByServiceWithResponse("rgname", "service1", com.azure.core.util.Context.NONE);
    }
}
```

### Services_CheckNameAvailability

```java
import com.azure.resourcemanager.healthcareapis.models.CheckNameAvailabilityParameters;

/**
 * Samples for Services CheckNameAvailability.
 */
public final class ServicesCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/CheckNameAvailabilityPost.json
     */
    /**
     * Sample code: Check name availability.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void checkNameAvailability(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.services().checkNameAvailabilityWithResponse(new CheckNameAvailabilityParameters().withName("serviceName").withType("Microsoft.HealthcareApis/services"), com.azure.core.util.Context.NONE);
    }
}
```

### Services_CreateOrUpdate

```java
import com.azure.resourcemanager.healthcareapis.models.Kind;
import com.azure.resourcemanager.healthcareapis.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.healthcareapis.models.PublicNetworkAccess;
import com.azure.resourcemanager.healthcareapis.models.ServiceAccessPolicyEntry;
import com.azure.resourcemanager.healthcareapis.models.ServiceAuthenticationConfigurationInfo;
import com.azure.resourcemanager.healthcareapis.models.ServiceCorsConfigurationInfo;
import com.azure.resourcemanager.healthcareapis.models.ServiceCosmosDbConfigurationInfo;
import com.azure.resourcemanager.healthcareapis.models.ServiceExportConfigurationInfo;
import com.azure.resourcemanager.healthcareapis.models.ServicesProperties;
import com.azure.resourcemanager.healthcareapis.models.ServicesResourceIdentity;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Samples for Services CreateOrUpdate.
 */
public final class ServicesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/legacy/ServiceCreate.json
     */
    /**
     * Sample code: Create or Update a service with all parameters.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void createOrUpdateAServiceWithAllParameters(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.services().define("service1").withRegion("westus2").withExistingResourceGroup("rg1").withKind(Kind.FHIR_R4).withTags(mapOf()).withIdentity(new ServicesResourceIdentity().withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED)).withProperties(new ServicesProperties().withAccessPolicies(Arrays.asList(new ServiceAccessPolicyEntry().withObjectId("c487e7d1-3210-41a3-8ccc-e9372b78da47"), new ServiceAccessPolicyEntry().withObjectId("5b307da8-43d4-492b-8b66-b0294ade872f"))).withCosmosDbConfiguration(new ServiceCosmosDbConfigurationInfo().withOfferThroughput(1000).withKeyVaultKeyUri("fakeTokenPlaceholder")).withAuthenticationConfiguration(new ServiceAuthenticationConfigurationInfo().withAuthority("https://login.microsoftonline.com/abfde7b2-df0f-47e6-aabf-2462b07508dc").withAudience("https://azurehealthcareapis.com").withSmartProxyEnabled(true)).withCorsConfiguration(new ServiceCorsConfigurationInfo().withOrigins(Arrays.asList("*")).withHeaders(Arrays.asList("*")).withMethods(Arrays.asList("DELETE", "GET", "OPTIONS", "PATCH", "POST", "PUT")).withMaxAge(1440).withAllowCredentials(false)).withExportConfiguration(new ServiceExportConfigurationInfo().withStorageAccountName("existingStorageAccount")).withPrivateEndpointConnections(Arrays.asList()).withPublicNetworkAccess(PublicNetworkAccess.DISABLED)).create();
    }

    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/legacy/ServiceCreateInDataSovereignRegionWithCmkEnabled.json
     */
    /**
     * Sample code: Create or Update a service with all parameters and CMK enabled in a data sovereign region.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void createOrUpdateAServiceWithAllParametersAndCMKEnabledInADataSovereignRegion(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.services().define("service1").withRegion("Southeast Asia").withExistingResourceGroup("rg1").withKind(Kind.FHIR_R4).withTags(mapOf()).withIdentity(new ServicesResourceIdentity().withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED)).withProperties(new ServicesProperties().withAccessPolicies(Arrays.asList(new ServiceAccessPolicyEntry().withObjectId("c487e7d1-3210-41a3-8ccc-e9372b78da47"), new ServiceAccessPolicyEntry().withObjectId("5b307da8-43d4-492b-8b66-b0294ade872f"))).withCosmosDbConfiguration(new ServiceCosmosDbConfigurationInfo().withOfferThroughput(1000).withKeyVaultKeyUri("fakeTokenPlaceholder").withCrossTenantCmkApplicationId("de3fbeef-8c3a-428e-8b9f-4d229c8a85f4")).withAuthenticationConfiguration(new ServiceAuthenticationConfigurationInfo().withAuthority("https://login.microsoftonline.com/abfde7b2-df0f-47e6-aabf-2462b07508dc").withAudience("https://azurehealthcareapis.com").withSmartProxyEnabled(true)).withCorsConfiguration(new ServiceCorsConfigurationInfo().withOrigins(Arrays.asList("*")).withHeaders(Arrays.asList("*")).withMethods(Arrays.asList("DELETE", "GET", "OPTIONS", "PATCH", "POST", "PUT")).withMaxAge(1440).withAllowCredentials(false)).withExportConfiguration(new ServiceExportConfigurationInfo().withStorageAccountName("existingStorageAccount")).withPrivateEndpointConnections(Arrays.asList()).withPublicNetworkAccess(PublicNetworkAccess.DISABLED)).create();
    }

    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/legacy/ServiceCreateMinimum.json
     */
    /**
     * Sample code: Create or Update a service with minimum parameters.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void createOrUpdateAServiceWithMinimumParameters(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.services().define("service2").withRegion("westus2").withExistingResourceGroup("rg1").withKind(Kind.FHIR_R4).withTags(mapOf()).withProperties(new ServicesProperties().withAccessPolicies(Arrays.asList(new ServiceAccessPolicyEntry().withObjectId("c487e7d1-3210-41a3-8ccc-e9372b78da47")))).create();
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

### Services_Delete

```java
/**
 * Samples for Services Delete.
 */
public final class ServicesDeleteSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/legacy/ServiceDelete.json
     */
    /**
     * Sample code: Delete service.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void deleteService(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.services().delete("rg1", "service1", com.azure.core.util.Context.NONE);
    }
}
```

### Services_GetByResourceGroup

```java
/**
 * Samples for Services GetByResourceGroup.
 */
public final class ServicesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/legacy/ServiceGet.json
     */
    /**
     * Sample code: Get metadata.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void getMetadata(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.services().getByResourceGroupWithResponse("rg1", "service1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/legacy/ServiceGetInDataSovereignRegionWithCmkEnabled.json
     */
    /**
     * Sample code: Get metadata for CMK enabled service in data sovereign region.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void getMetadataForCMKEnabledServiceInDataSovereignRegion(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.services().getByResourceGroupWithResponse("rg1", "service1", com.azure.core.util.Context.NONE);
    }
}
```

### Services_List

```java
/**
 * Samples for Services List.
 */
public final class ServicesListSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/legacy/ServiceList.json
     */
    /**
     * Sample code: List all services in subscription.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void listAllServicesInSubscription(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.services().list(com.azure.core.util.Context.NONE);
    }
}
```

### Services_ListByResourceGroup

```java
/**
 * Samples for Services ListByResourceGroup.
 */
public final class ServicesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/legacy/ServiceListByResourceGroup.json
     */
    /**
     * Sample code: List all services in resource group.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void listAllServicesInResourceGroup(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.services().listByResourceGroup("rgname", com.azure.core.util.Context.NONE);
    }
}
```

### Services_Update

```java
import com.azure.resourcemanager.healthcareapis.models.ServicesDescription;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Services Update.
 */
public final class ServicesUpdateSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/legacy/ServicePatch.json
     */
    /**
     * Sample code: Patch service.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void patchService(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        ServicesDescription resource = manager.services().getByResourceGroupWithResponse("rg1", "service1", com.azure.core.util.Context.NONE).getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### WorkspacePrivateEndpointConnections_CreateOrUpdate

```java
import com.azure.resourcemanager.healthcareapis.fluent.models.PrivateEndpointConnectionDescriptionInner;
import com.azure.resourcemanager.healthcareapis.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.healthcareapis.models.PrivateLinkServiceConnectionState;
import java.util.stream.Collectors;

/**
 * Samples for WorkspacePrivateEndpointConnections CreateOrUpdate.
 */
public final class WorkspacePrivateEndpointConnectionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/privatelink/WorkspaceCreatePrivateEndpointConnection.json
     */
    /**
     * Sample code: WorkspacePrivateEndpointConnection_CreateOrUpdate.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void workspacePrivateEndpointConnectionCreateOrUpdate(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.workspacePrivateEndpointConnections().createOrUpdate("testRG", "workspace1", "myConnection", new PrivateEndpointConnectionDescriptionInner().withPrivateLinkServiceConnectionState(new PrivateLinkServiceConnectionState().withStatus(PrivateEndpointServiceConnectionStatus.APPROVED).withDescription("Auto-Approved")), com.azure.core.util.Context.NONE);
    }
}
```

### WorkspacePrivateEndpointConnections_Delete

```java
/**
 * Samples for WorkspacePrivateEndpointConnections Delete.
 */
public final class WorkspacePrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/privatelink/WorkspaceDeletePrivateEndpointConnection.json
     */
    /**
     * Sample code: WorkspacePrivateEndpointConnections_Delete.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void workspacePrivateEndpointConnectionsDelete(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.workspacePrivateEndpointConnections().delete("testRG", "workspace1", "myConnection", com.azure.core.util.Context.NONE);
    }
}
```

### WorkspacePrivateEndpointConnections_Get

```java
/**
 * Samples for WorkspacePrivateEndpointConnections Get.
 */
public final class WorkspacePrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/privatelink/WorkspaceGetPrivateEndpointConnection.json
     */
    /**
     * Sample code: WorkspacePrivateEndpointConnection_GetConnection.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void workspacePrivateEndpointConnectionGetConnection(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.workspacePrivateEndpointConnections().getWithResponse("testRG", "workspace1", "myConnection", com.azure.core.util.Context.NONE);
    }
}
```

### WorkspacePrivateEndpointConnections_ListByWorkspace

```java
/**
 * Samples for WorkspacePrivateEndpointConnections ListByWorkspace.
 */
public final class WorkspacePrivateEndpointConnectionsListByWorkspaceSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/privatelink/WorkspaceListPrivateEndpointConnections.json
     */
    /**
     * Sample code: WorkspacePrivateEndpointConnection_List.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void workspacePrivateEndpointConnectionList(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.workspacePrivateEndpointConnections().listByWorkspace("testRG", "workspace1", com.azure.core.util.Context.NONE);
    }
}
```

### WorkspacePrivateLinkResources_Get

```java
/**
 * Samples for WorkspacePrivateLinkResources Get.
 */
public final class WorkspacePrivateLinkResourcesGetSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/privatelink/WorkspacePrivateLinkResourceGet.json
     */
    /**
     * Sample code: WorkspacePrivateLinkResources_Get.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void workspacePrivateLinkResourcesGet(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.workspacePrivateLinkResources().getWithResponse("testRG", "workspace1", "healthcareworkspace", com.azure.core.util.Context.NONE);
    }
}
```

### WorkspacePrivateLinkResources_ListByWorkspace

```java
/**
 * Samples for WorkspacePrivateLinkResources ListByWorkspace.
 */
public final class WorkspacePrivateLinkResourcesListByWorkspaceSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/privatelink/PrivateLinkResourcesListByWorkspace.json
     */
    /**
     * Sample code: WorkspacePrivateLinkResources_ListGroupIds.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void workspacePrivateLinkResourcesListGroupIds(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.workspacePrivateLinkResources().listByWorkspace("testRG", "workspace1", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_CreateOrUpdate

```java
import com.azure.resourcemanager.healthcareapis.models.WorkspaceProperties;

/**
 * Samples for Workspaces CreateOrUpdate.
 */
public final class WorkspacesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/workspaces/Workspaces_Create.json
     */
    /**
     * Sample code: Create or update a workspace.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void createOrUpdateAWorkspace(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.workspaces().define("workspace1").withExistingResourceGroup("testRG").withRegion("westus").withProperties(new WorkspaceProperties()).create();
    }
}
```

### Workspaces_Delete

```java
/**
 * Samples for Workspaces Delete.
 */
public final class WorkspacesDeleteSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/workspaces/Workspaces_Delete.json
     */
    /**
     * Sample code: Delete a workspace.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void deleteAWorkspace(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.workspaces().delete("testRG", "workspace1", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_GetByResourceGroup

```java
/**
 * Samples for Workspaces GetByResourceGroup.
 */
public final class WorkspacesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/workspaces/Workspaces_Get.json
     */
    /**
     * Sample code: Get workspace.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void getWorkspace(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.workspaces().getByResourceGroupWithResponse("testRG", "workspace1", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_List

```java
/**
 * Samples for Workspaces List.
 */
public final class WorkspacesListSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/workspaces/Workspaces_ListBySubscription.json
     */
    /**
     * Sample code: Get workspaces by subscription.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void getWorkspacesBySubscription(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.workspaces().list(com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_ListByResourceGroup

```java
/**
 * Samples for Workspaces ListByResourceGroup.
 */
public final class WorkspacesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/workspaces/Workspaces_ListByResourceGroup.json
     */
    /**
     * Sample code: Get workspaces by resource group.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void getWorkspacesByResourceGroup(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        manager.workspaces().listByResourceGroup("testRG", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_Update

```java
import com.azure.resourcemanager.healthcareapis.models.Workspace;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Workspaces Update.
 */
public final class WorkspacesUpdateSamples {
    /*
     * x-ms-original-file: specification/healthcareapis/resource-manager/Microsoft.HealthcareApis/stable/2023-11-01/examples/workspaces/Workspaces_Patch.json
     */
    /**
     * Sample code: Update a workspace.
     * 
     * @param manager Entry point to HealthcareApisManager.
     */
    public static void updateAWorkspace(com.azure.resourcemanager.healthcareapis.HealthcareApisManager manager) {
        Workspace resource = manager.workspaces().getByResourceGroupWithResponse("testRG", "workspace1", com.azure.core.util.Context.NONE).getValue();
        resource.update().withTags(mapOf("tagKey", "fakeTokenPlaceholder")).apply();
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


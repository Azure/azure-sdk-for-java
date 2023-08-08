# Code snippets and samples


## AuthorizationPolicies

- [CreateOrUpdate](#authorizationpolicies_createorupdate)
- [Get](#authorizationpolicies_get)
- [ListByHub](#authorizationpolicies_listbyhub)
- [RegeneratePrimaryKey](#authorizationpolicies_regenerateprimarykey)
- [RegenerateSecondaryKey](#authorizationpolicies_regeneratesecondarykey)

## ConnectorMappings

- [CreateOrUpdate](#connectormappings_createorupdate)
- [Delete](#connectormappings_delete)
- [Get](#connectormappings_get)
- [ListByConnector](#connectormappings_listbyconnector)

## Connectors

- [CreateOrUpdate](#connectors_createorupdate)
- [Delete](#connectors_delete)
- [Get](#connectors_get)
- [ListByHub](#connectors_listbyhub)

## Hubs

- [CreateOrUpdate](#hubs_createorupdate)
- [Delete](#hubs_delete)
- [GetByResourceGroup](#hubs_getbyresourcegroup)
- [List](#hubs_list)
- [ListByResourceGroup](#hubs_listbyresourcegroup)
- [Update](#hubs_update)

## Images

- [GetUploadUrlForData](#images_getuploadurlfordata)
- [GetUploadUrlForEntityType](#images_getuploadurlforentitytype)

## Interactions

- [CreateOrUpdate](#interactions_createorupdate)
- [Get](#interactions_get)
- [ListByHub](#interactions_listbyhub)
- [SuggestRelationshipLinks](#interactions_suggestrelationshiplinks)

## Kpi

- [CreateOrUpdate](#kpi_createorupdate)
- [Delete](#kpi_delete)
- [Get](#kpi_get)
- [ListByHub](#kpi_listbyhub)
- [Reprocess](#kpi_reprocess)

## Links

- [CreateOrUpdate](#links_createorupdate)
- [Delete](#links_delete)
- [Get](#links_get)
- [ListByHub](#links_listbyhub)

## Operations

- [List](#operations_list)

## Predictions

- [CreateOrUpdate](#predictions_createorupdate)
- [Delete](#predictions_delete)
- [Get](#predictions_get)
- [GetModelStatus](#predictions_getmodelstatus)
- [GetTrainingResults](#predictions_gettrainingresults)
- [ListByHub](#predictions_listbyhub)
- [ModelStatus](#predictions_modelstatus)

## Profiles

- [CreateOrUpdate](#profiles_createorupdate)
- [Delete](#profiles_delete)
- [Get](#profiles_get)
- [GetEnrichingKpis](#profiles_getenrichingkpis)
- [ListByHub](#profiles_listbyhub)

## RelationshipLinks

- [CreateOrUpdate](#relationshiplinks_createorupdate)
- [Delete](#relationshiplinks_delete)
- [Get](#relationshiplinks_get)
- [ListByHub](#relationshiplinks_listbyhub)

## Relationships

- [CreateOrUpdate](#relationships_createorupdate)
- [Delete](#relationships_delete)
- [Get](#relationships_get)
- [ListByHub](#relationships_listbyhub)

## RoleAssignments

- [CreateOrUpdate](#roleassignments_createorupdate)
- [Delete](#roleassignments_delete)
- [Get](#roleassignments_get)
- [ListByHub](#roleassignments_listbyhub)

## Roles

- [ListByHub](#roles_listbyhub)

## Views

- [CreateOrUpdate](#views_createorupdate)
- [Delete](#views_delete)
- [Get](#views_get)
- [ListByHub](#views_listbyhub)

## WidgetTypes

- [Get](#widgettypes_get)
- [ListByHub](#widgettypes_listbyhub)
### AuthorizationPolicies_CreateOrUpdate

```java
import com.azure.resourcemanager.customerinsights.models.PermissionTypes;
import java.util.Arrays;

/** Samples for AuthorizationPolicies CreateOrUpdate. */
public final class AuthorizationPoliciesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/AuthorizationPoliciesCreateOrUpdate.json
     */
    /**
     * Sample code: AuthorizationPolicies_CreateOrUpdate.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void authorizationPoliciesCreateOrUpdate(
        com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager
            .authorizationPolicies()
            .define("testPolicy4222")
            .withExistingHub("TestHubRG", "azSdkTestHub")
            .withPermissions(Arrays.asList(PermissionTypes.READ, PermissionTypes.WRITE, PermissionTypes.MANAGE))
            .create();
    }
}
```

### AuthorizationPolicies_Get

```java
/** Samples for AuthorizationPolicies Get. */
public final class AuthorizationPoliciesGetSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/AuthorizationPoliciesGet.json
     */
    /**
     * Sample code: AuthorizationPolicies_Get.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void authorizationPoliciesGet(
        com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager
            .authorizationPolicies()
            .getWithResponse("TestHubRG", "azSdkTestHub", "testPolicy4222", com.azure.core.util.Context.NONE);
    }
}
```

### AuthorizationPolicies_ListByHub

```java
/** Samples for AuthorizationPolicies ListByHub. */
public final class AuthorizationPoliciesListByHubSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/AuthorizationPoliciesListByHub.json
     */
    /**
     * Sample code: AuthorizationPolicies_ListByHub.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void authorizationPoliciesListByHub(
        com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager.authorizationPolicies().listByHub("TestHubRG", "azSdkTestHub", com.azure.core.util.Context.NONE);
    }
}
```

### AuthorizationPolicies_RegeneratePrimaryKey

```java
/** Samples for AuthorizationPolicies RegeneratePrimaryKey. */
public final class AuthorizationPoliciesRegeneratePrimaryKeySamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/AuthorizationPoliciesRegeneratePrimaryKey.json
     */
    /**
     * Sample code: AuthorizationPolicies_RegeneratePrimaryKey.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void authorizationPoliciesRegeneratePrimaryKey(
        com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager
            .authorizationPolicies()
            .regeneratePrimaryKeyWithResponse(
                "TestHubRG", "azSdkTestHub", "testPolicy4222", com.azure.core.util.Context.NONE);
    }
}
```

### AuthorizationPolicies_RegenerateSecondaryKey

```java
/** Samples for AuthorizationPolicies RegenerateSecondaryKey. */
public final class AuthorizationPoliciesRegenerateSecondaryKeySamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/AuthorizationPoliciesRegenerateSecondaryKey.json
     */
    /**
     * Sample code: AuthorizationPolicies_RegenerateSecondaryKey.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void authorizationPoliciesRegenerateSecondaryKey(
        com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager
            .authorizationPolicies()
            .regenerateSecondaryKeyWithResponse(
                "TestHubRG", "azSdkTestHub", "testPolicy4222", com.azure.core.util.Context.NONE);
    }
}
```

### ConnectorMappings_CreateOrUpdate

```java
import com.azure.resourcemanager.customerinsights.models.CompletionOperationTypes;
import com.azure.resourcemanager.customerinsights.models.ConnectorMappingAvailability;
import com.azure.resourcemanager.customerinsights.models.ConnectorMappingCompleteOperation;
import com.azure.resourcemanager.customerinsights.models.ConnectorMappingErrorManagement;
import com.azure.resourcemanager.customerinsights.models.ConnectorMappingFormat;
import com.azure.resourcemanager.customerinsights.models.ConnectorMappingProperties;
import com.azure.resourcemanager.customerinsights.models.ConnectorMappingStructure;
import com.azure.resourcemanager.customerinsights.models.EntityTypes;
import com.azure.resourcemanager.customerinsights.models.ErrorManagementTypes;
import com.azure.resourcemanager.customerinsights.models.FrequencyTypes;
import java.util.Arrays;

/** Samples for ConnectorMappings CreateOrUpdate. */
public final class ConnectorMappingsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/ConnectorMappingsCreateOrUpdate.json
     */
    /**
     * Sample code: ConnectorMappings_CreateOrUpdate.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void connectorMappingsCreateOrUpdate(
        com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager
            .connectorMappings()
            .define("testMapping12491")
            .withExistingConnector("TestHubRG", "sdkTestHub", "testConnector8858")
            .withEntityType(EntityTypes.INTERACTION)
            .withEntityTypeName("TestInteractionType2967")
            .withDisplayName("testMapping12491")
            .withDescription("Test mapping")
            .withMappingProperties(
                new ConnectorMappingProperties()
                    .withFolderPath("http://sample.dne/file")
                    .withFileFilter("unknown")
                    .withHasHeader(false)
                    .withErrorManagement(
                        new ConnectorMappingErrorManagement()
                            .withErrorManagementType(ErrorManagementTypes.STOP_IMPORT)
                            .withErrorLimit(10))
                    .withFormat(new ConnectorMappingFormat().withColumnDelimiter("|"))
                    .withAvailability(
                        new ConnectorMappingAvailability().withFrequency(FrequencyTypes.HOUR).withInterval(5))
                    .withStructure(
                        Arrays
                            .asList(
                                new ConnectorMappingStructure()
                                    .withPropertyName("unknwon1")
                                    .withColumnName("unknown1")
                                    .withIsEncrypted(false),
                                new ConnectorMappingStructure()
                                    .withPropertyName("unknwon2")
                                    .withColumnName("unknown2")
                                    .withIsEncrypted(true)))
                    .withCompleteOperation(
                        new ConnectorMappingCompleteOperation()
                            .withCompletionOperationType(CompletionOperationTypes.DELETE_FILE)
                            .withDestinationFolder("fakePath")))
            .create();
    }
}
```

### ConnectorMappings_Delete

```java
/** Samples for ConnectorMappings Delete. */
public final class ConnectorMappingsDeleteSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/ConnectorMappingsDelete.json
     */
    /**
     * Sample code: ConnectorMappings_Delete.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void connectorMappingsDelete(
        com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager
            .connectorMappings()
            .deleteWithResponse(
                "TestHubRG", "sdkTestHub", "testConnector8858", "testMapping12491", com.azure.core.util.Context.NONE);
    }
}
```

### ConnectorMappings_Get

```java
/** Samples for ConnectorMappings Get. */
public final class ConnectorMappingsGetSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/ConnectorMappingsGet.json
     */
    /**
     * Sample code: ConnectorMappings_Get.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void connectorMappingsGet(
        com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager
            .connectorMappings()
            .getWithResponse(
                "TestHubRG", "sdkTestHub", "testConnector8858", "testMapping12491", com.azure.core.util.Context.NONE);
    }
}
```

### ConnectorMappings_ListByConnector

```java
/** Samples for ConnectorMappings ListByConnector. */
public final class ConnectorMappingsListByConnectorSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/ConnectorMappingsListByConnector.json
     */
    /**
     * Sample code: ConnectorMappings_ListByConnector.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void connectorMappingsListByConnector(
        com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager
            .connectorMappings()
            .listByConnector("TestHubRG", "sdkTestHub", "testConnector8858", com.azure.core.util.Context.NONE);
    }
}
```

### Connectors_CreateOrUpdate

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.customerinsights.models.ConnectorTypes;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** Samples for Connectors CreateOrUpdate. */
public final class ConnectorsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/ConnectorsCreateOrUpdate.json
     */
    /**
     * Sample code: Connectors_CreateOrUpdate.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void connectorsCreateOrUpdate(
        com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) throws IOException {
        manager
            .connectors()
            .define("testConnector")
            .withExistingHub("TestHubRG", "sdkTestHub")
            .withConnectorType(ConnectorTypes.AZURE_BLOB)
            .withDisplayName("testConnector")
            .withDescription("Test connector")
            .withConnectorProperties(
                mapOf(
                    "connectionKeyVaultUrl",
                    SerializerFactory
                        .createDefaultManagementSerializerAdapter()
                        .deserialize(
                            "{\"organizationId\":\"XXX\",\"organizationUrl\":\"https://XXX.crmlivetie.com/\"}",
                            Object.class,
                            SerializerEncoding.JSON)))
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

### Connectors_Delete

```java
/** Samples for Connectors Delete. */
public final class ConnectorsDeleteSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/ConnectorsDelete.json
     */
    /**
     * Sample code: Connectors_Delete.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void connectorsDelete(com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager.connectors().delete("TestHubRG", "sdkTestHub", "testConnector", com.azure.core.util.Context.NONE);
    }
}
```

### Connectors_Get

```java
/** Samples for Connectors Get. */
public final class ConnectorsGetSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/ConnectorsGet.json
     */
    /**
     * Sample code: Connectors_Get.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void connectorsGet(com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager
            .connectors()
            .getWithResponse("TestHubRG", "sdkTestHub", "testConnector", com.azure.core.util.Context.NONE);
    }
}
```

### Connectors_ListByHub

```java
/** Samples for Connectors ListByHub. */
public final class ConnectorsListByHubSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/ConnectorsListByHub.json
     */
    /**
     * Sample code: Connectors_ListByHub.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void connectorsListByHub(com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager.connectors().listByHub("TestHubRG", "sdkTestHub", com.azure.core.util.Context.NONE);
    }
}
```

### Hubs_CreateOrUpdate

```java
import com.azure.resourcemanager.customerinsights.models.HubBillingInfoFormat;

/** Samples for Hubs CreateOrUpdate. */
public final class HubsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/HubsCreateOrUpdate.json
     */
    /**
     * Sample code: Hubs_CreateOrUpdate.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void hubsCreateOrUpdate(com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager
            .hubs()
            .define("sdkTestHub")
            .withRegion("West US")
            .withExistingResourceGroup("TestHubRG")
            .withHubBillingInfo(new HubBillingInfoFormat().withSkuName("B0").withMinUnits(1).withMaxUnits(5))
            .create();
    }
}
```

### Hubs_Delete

```java
/** Samples for Hubs Delete. */
public final class HubsDeleteSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/HubsDelete.json
     */
    /**
     * Sample code: Hubs_Delete.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void hubsDelete(com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager.hubs().delete("TestHubRG", "sdkTestHub", com.azure.core.util.Context.NONE);
    }
}
```

### Hubs_GetByResourceGroup

```java
/** Samples for Hubs GetByResourceGroup. */
public final class HubsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/HubsGet.json
     */
    /**
     * Sample code: Hubs_Get.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void hubsGet(com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager.hubs().getByResourceGroupWithResponse("TestHubRG", "sdkTestHub", com.azure.core.util.Context.NONE);
    }
}
```

### Hubs_List

```java
/** Samples for Hubs List. */
public final class HubsListSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/HubsList.json
     */
    /**
     * Sample code: Hubs_List.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void hubsList(com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager.hubs().list(com.azure.core.util.Context.NONE);
    }
}
```

### Hubs_ListByResourceGroup

```java
/** Samples for Hubs ListByResourceGroup. */
public final class HubsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/HubsListByResourceGroup.json
     */
    /**
     * Sample code: Hubs_ListByResourceGroup.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void hubsListByResourceGroup(
        com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager.hubs().listByResourceGroup("TestHubRG", com.azure.core.util.Context.NONE);
    }
}
```

### Hubs_Update

```java
import com.azure.resourcemanager.customerinsights.models.Hub;
import com.azure.resourcemanager.customerinsights.models.HubBillingInfoFormat;

/** Samples for Hubs Update. */
public final class HubsUpdateSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/HubsUpdate.json
     */
    /**
     * Sample code: Hubs_Update.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void hubsUpdate(com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        Hub resource =
            manager
                .hubs()
                .getByResourceGroupWithResponse("TestHubRG", "sdkTestHub", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withHubBillingInfo(new HubBillingInfoFormat().withSkuName("B0").withMinUnits(1).withMaxUnits(5))
            .apply();
    }
}
```

### Images_GetUploadUrlForData

```java
import com.azure.resourcemanager.customerinsights.models.GetImageUploadUrlInput;

/** Samples for Images GetUploadUrlForData. */
public final class ImagesGetUploadUrlForDataSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/ImagesGetUploadUrlForData.json
     */
    /**
     * Sample code: Images_GetUploadUrlForData.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void imagesGetUploadUrlForData(
        com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager
            .images()
            .getUploadUrlForDataWithResponse(
                "TestHubRG",
                "sdkTestHub",
                new GetImageUploadUrlInput()
                    .withEntityType("Profile")
                    .withEntityTypeName("Contact")
                    .withRelativePath("images/profile1.png"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Images_GetUploadUrlForEntityType

```java
import com.azure.resourcemanager.customerinsights.models.GetImageUploadUrlInput;

/** Samples for Images GetUploadUrlForEntityType. */
public final class ImagesGetUploadUrlForEntityTypeSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/ImagesGetUploadUrlForEntityType.json
     */
    /**
     * Sample code: Images_GetUploadUrlForEntityType.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void imagesGetUploadUrlForEntityType(
        com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager
            .images()
            .getUploadUrlForEntityTypeWithResponse(
                "TestHubRG",
                "sdkTestHub",
                new GetImageUploadUrlInput()
                    .withEntityType("Profile")
                    .withEntityTypeName("Contact")
                    .withRelativePath("images/profile1.png"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Interactions_CreateOrUpdate

```java
import com.azure.resourcemanager.customerinsights.models.PropertyDefinition;
import java.util.Arrays;

/** Samples for Interactions CreateOrUpdate. */
public final class InteractionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/InteractionsCreateOrUpdate.json
     */
    /**
     * Sample code: Interactions_CreateOrUpdate.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void interactionsCreateOrUpdate(
        com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager
            .interactions()
            .define("TestProfileType396")
            .withExistingHub("TestHubRG", "sdkTestHub")
            .withIdPropertyNames(Arrays.asList("TestInteractionType6358"))
            .withPrimaryParticipantProfilePropertyName("profile1")
            .withApiEntitySetName("TestInteractionType6358")
            .withFields(
                Arrays
                    .asList(
                        new PropertyDefinition()
                            .withFieldName("TestInteractionType6358")
                            .withFieldType("Edm.String")
                            .withIsArray(false)
                            .withIsRequired(true),
                        new PropertyDefinition().withFieldName("profile1").withFieldType("Edm.String")))
            .withSmallImage("\\\\Images\\\\smallImage")
            .withMediumImage("\\\\Images\\\\MediumImage")
            .withLargeImage("\\\\Images\\\\LargeImage")
            .create();
    }
}
```

### Interactions_Get

```java
/** Samples for Interactions Get. */
public final class InteractionsGetSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/InteractionsGet.json
     */
    /**
     * Sample code: Interactions_Get.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void interactionsGet(com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager
            .interactions()
            .getWithResponse(
                "TestHubRG", "sdkTestHub", "TestInteractionType6358", null, com.azure.core.util.Context.NONE);
    }
}
```

### Interactions_ListByHub

```java
/** Samples for Interactions ListByHub. */
public final class InteractionsListByHubSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/InteractionsListByHub.json
     */
    /**
     * Sample code: Interactions_ListByHub.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void interactionsListByHub(
        com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager.interactions().listByHub("TestHubRG", "sdkTestHub", null, com.azure.core.util.Context.NONE);
    }
}
```

### Interactions_SuggestRelationshipLinks

```java
/** Samples for Interactions SuggestRelationshipLinks. */
public final class InteractionsSuggestRelationshipLinksSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/InteractionsSuggestRelationshipLinks.json
     */
    /**
     * Sample code: Interactions_SuggestRelationshipLinks.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void interactionsSuggestRelationshipLinks(
        com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager
            .interactions()
            .suggestRelationshipLinksWithResponse(
                "TestHubRG", "sdkTestHub", "Deposit", com.azure.core.util.Context.NONE);
    }
}
```

### Kpi_CreateOrUpdate

```java
import com.azure.resourcemanager.customerinsights.models.CalculationWindowTypes;
import com.azure.resourcemanager.customerinsights.models.EntityTypes;
import com.azure.resourcemanager.customerinsights.models.KpiAlias;
import com.azure.resourcemanager.customerinsights.models.KpiFunctions;
import com.azure.resourcemanager.customerinsights.models.KpiThresholds;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Kpi CreateOrUpdate. */
public final class KpiCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/KpiCreateOrUpdate.json
     */
    /**
     * Sample code: Kpi_CreateOrUpdate.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void kpiCreateOrUpdate(com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager
            .kpis()
            .define("kpiTest45453647")
            .withExistingHub("TestHubRG", "sdkTestHub")
            .withEntityType(EntityTypes.PROFILE)
            .withEntityTypeName("testProfile2327128")
            .withDisplayName(mapOf("en-us", "Kpi DisplayName"))
            .withDescription(mapOf("en-us", "Kpi Description"))
            .withCalculationWindow(CalculationWindowTypes.DAY)
            .withFunction(KpiFunctions.SUM)
            .withExpression("SavingAccountBalance")
            .withUnit("unit")
            .withGroupBy(Arrays.asList("SavingAccountBalance"))
            .withThresHolds(
                new KpiThresholds()
                    .withLowerLimit(new BigDecimal("5"))
                    .withUpperLimit(new BigDecimal("50"))
                    .withIncreasingKpi(true))
            .withAliases(Arrays.asList(new KpiAlias().withAliasName("alias").withExpression("Id+4")))
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

### Kpi_Delete

```java
/** Samples for Kpi Delete. */
public final class KpiDeleteSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/KpiDelete.json
     */
    /**
     * Sample code: Kpi_Delete.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void kpiDelete(com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager.kpis().delete("TestHubRG", "sdkTestHub", "kpiTest45453647", com.azure.core.util.Context.NONE);
    }
}
```

### Kpi_Get

```java
/** Samples for Kpi Get. */
public final class KpiGetSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/KpiGet.json
     */
    /**
     * Sample code: Kpi_Get.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void kpiGet(com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager.kpis().getWithResponse("TestHubRG", "sdkTestHub", "kpiTest45453647", com.azure.core.util.Context.NONE);
    }
}
```

### Kpi_ListByHub

```java
/** Samples for Kpi ListByHub. */
public final class KpiListByHubSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/KpiListByHub.json
     */
    /**
     * Sample code: Kpi_ListByHub.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void kpiListByHub(com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager.kpis().listByHub("TestHubRG", "sdkTestHub", com.azure.core.util.Context.NONE);
    }
}
```

### Kpi_Reprocess

```java
/** Samples for Kpi Reprocess. */
public final class KpiReprocessSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/KpiReprocess.json
     */
    /**
     * Sample code: Kpi_Reprocess.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void kpiReprocess(com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager
            .kpis()
            .reprocessWithResponse("TestHubRG", "sdkTestHub", "kpiTest45453647", com.azure.core.util.Context.NONE);
    }
}
```

### Links_CreateOrUpdate

```java
import com.azure.resourcemanager.customerinsights.models.EntityType;
import com.azure.resourcemanager.customerinsights.models.LinkTypes;
import com.azure.resourcemanager.customerinsights.models.ParticipantPropertyReference;
import com.azure.resourcemanager.customerinsights.models.TypePropertiesMapping;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Links CreateOrUpdate. */
public final class LinksCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/LinksCreateOrUpdate.json
     */
    /**
     * Sample code: Links_CreateOrUpdate.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void linksCreateOrUpdate(com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager
            .links()
            .define("linkTest4806")
            .withExistingHub("TestHubRG", "sdkTestHub")
            .withSourceEntityType(EntityType.INTERACTION)
            .withTargetEntityType(EntityType.PROFILE)
            .withSourceEntityTypeName("testInteraction1949")
            .withTargetEntityTypeName("testProfile1446")
            .withDisplayName(mapOf("en-us", "Link DisplayName"))
            .withDescription(mapOf("en-us", "Link Description"))
            .withMappings(
                Arrays
                    .asList(
                        new TypePropertiesMapping()
                            .withSourcePropertyName("testInteraction1949")
                            .withTargetPropertyName("testProfile1446")
                            .withLinkType(LinkTypes.UPDATE_ALWAYS)))
            .withParticipantPropertyReferences(
                Arrays
                    .asList(
                        new ParticipantPropertyReference()
                            .withSourcePropertyName("testInteraction1949")
                            .withTargetPropertyName("ProfileId")))
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

### Links_Delete

```java
/** Samples for Links Delete. */
public final class LinksDeleteSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/LinksDelete.json
     */
    /**
     * Sample code: Links_Delete.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void linksDelete(com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager.links().deleteWithResponse("TestHubRG", "sdkTestHub", "linkTest4806", com.azure.core.util.Context.NONE);
    }
}
```

### Links_Get

```java
/** Samples for Links Get. */
public final class LinksGetSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/LinksGet.json
     */
    /**
     * Sample code: Links_Get.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void linksGet(com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager.links().getWithResponse("TestHubRG", "sdkTestHub", "linkTest4806", com.azure.core.util.Context.NONE);
    }
}
```

### Links_ListByHub

```java
/** Samples for Links ListByHub. */
public final class LinksListByHubSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/LinksListByHub.json
     */
    /**
     * Sample code: Links_ListByHub.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void linksListByHub(com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager.links().listByHub("TestHubRG", "sdkTestHub", com.azure.core.util.Context.NONE);
    }
}
```

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/DCIOperations_List.json
     */
    /**
     * Sample code: DCIOperations_List.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void dCIOperationsList(com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Predictions_CreateOrUpdate

```java
import com.azure.resourcemanager.customerinsights.models.PredictionMappings;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Predictions CreateOrUpdate. */
public final class PredictionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/PredictionsCreateOrUpdate.json
     */
    /**
     * Sample code: Predictions_CreateOrUpdate.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void predictionsCreateOrUpdate(
        com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager
            .predictions()
            .define("sdktest")
            .withExistingHub("TestHubRG", "sdkTestHub")
            .withDescription(mapOf("en-us", "sdktest"))
            .withDisplayName(mapOf("en-us", "sdktest"))
            .withInvolvedInteractionTypes(Arrays.asList())
            .withInvolvedKpiTypes(Arrays.asList())
            .withInvolvedRelationships(Arrays.asList())
            .withNegativeOutcomeExpression("Customers.FirstName = 'Mike'")
            .withPositiveOutcomeExpression("Customers.FirstName = 'David'")
            .withPrimaryProfileType("Customers")
            .withPredictionName("sdktest")
            .withScopeExpression("*")
            .withAutoAnalyze(true)
            .withMappings(
                new PredictionMappings()
                    .withScore("sdktest_Score")
                    .withGrade("sdktest_Grade")
                    .withReason("sdktest_Reason"))
            .withScoreLabel("score label")
            .withGrades(Arrays.asList())
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

### Predictions_Delete

```java
/** Samples for Predictions Delete. */
public final class PredictionsDeleteSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/PredictionsDelete.json
     */
    /**
     * Sample code: Predictions_Delete.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void predictionsDelete(com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager.predictions().delete("TestHubRG", "sdkTestHub", "sdktest", com.azure.core.util.Context.NONE);
    }
}
```

### Predictions_Get

```java
/** Samples for Predictions Get. */
public final class PredictionsGetSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/PredictionsGet.json
     */
    /**
     * Sample code: Predictions_Get.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void predictionsGet(com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager.predictions().getWithResponse("TestHubRG", "sdkTestHub", "sdktest", com.azure.core.util.Context.NONE);
    }
}
```

### Predictions_GetModelStatus

```java
/** Samples for Predictions GetModelStatus. */
public final class PredictionsGetModelStatusSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/PredictionsGetModelStatus.json
     */
    /**
     * Sample code: Predictions_GetModelStatus.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void predictionsGetModelStatus(
        com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager
            .predictions()
            .getModelStatusWithResponse("TestHubRG", "sdkTestHub", "sdktest", com.azure.core.util.Context.NONE);
    }
}
```

### Predictions_GetTrainingResults

```java
/** Samples for Predictions GetTrainingResults. */
public final class PredictionsGetTrainingResultsSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/PredictionsGetTrainingResults.json
     */
    /**
     * Sample code: Predictions_GetTrainingResults.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void predictionsGetTrainingResults(
        com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager
            .predictions()
            .getTrainingResultsWithResponse("TestHubRG", "sdkTestHub", "sdktest", com.azure.core.util.Context.NONE);
    }
}
```

### Predictions_ListByHub

```java
/** Samples for Predictions ListByHub. */
public final class PredictionsListByHubSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/PredictionsListByHub.json
     */
    /**
     * Sample code: Predictions_ListByHub.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void predictionsListByHub(
        com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager.predictions().listByHub("TestHubRG", "sdkTestHub", com.azure.core.util.Context.NONE);
    }
}
```

### Predictions_ModelStatus

```java
import com.azure.resourcemanager.customerinsights.fluent.models.PredictionModelStatusInner;
import com.azure.resourcemanager.customerinsights.models.PredictionModelLifeCycle;

/** Samples for Predictions ModelStatus. */
public final class PredictionsModelStatusSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/PredictionsModelStatus.json
     */
    /**
     * Sample code: Predictions_ModelStatus.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void predictionsModelStatus(
        com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager
            .predictions()
            .modelStatusWithResponse(
                "TestHubRG",
                "sdkTestHub",
                "sdktest",
                new PredictionModelStatusInner().withStatus(PredictionModelLifeCycle.TRAINING),
                com.azure.core.util.Context.NONE);
    }
}
```

### Profiles_CreateOrUpdate

```java
import com.azure.resourcemanager.customerinsights.models.PropertyDefinition;
import com.azure.resourcemanager.customerinsights.models.StrongId;
import java.util.Arrays;

/** Samples for Profiles CreateOrUpdate. */
public final class ProfilesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/ProfilesCreateOrUpdate.json
     */
    /**
     * Sample code: Profiles_CreateOrUpdate.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void profilesCreateOrUpdate(
        com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager
            .profiles()
            .define("TestProfileType396")
            .withExistingHub("TestHubRG", "sdkTestHub")
            .withStrongIds(
                Arrays
                    .asList(
                        new StrongId()
                            .withKeyPropertyNames(Arrays.asList("Id", "SavingAccountBalance"))
                            .withStrongIdName("Id"),
                        new StrongId()
                            .withKeyPropertyNames(Arrays.asList("ProfileId", "LastName"))
                            .withStrongIdName("ProfileId")))
            .withApiEntitySetName("TestProfileType396")
            .withFields(
                Arrays
                    .asList(
                        new PropertyDefinition()
                            .withFieldName("Id")
                            .withFieldType("Edm.String")
                            .withIsArray(false)
                            .withIsRequired(true),
                        new PropertyDefinition()
                            .withFieldName("ProfileId")
                            .withFieldType("Edm.String")
                            .withIsArray(false)
                            .withIsRequired(true),
                        new PropertyDefinition()
                            .withFieldName("LastName")
                            .withFieldType("Edm.String")
                            .withIsArray(false)
                            .withIsRequired(true),
                        new PropertyDefinition()
                            .withFieldName("TestProfileType396")
                            .withFieldType("Edm.String")
                            .withIsArray(false)
                            .withIsRequired(true),
                        new PropertyDefinition()
                            .withFieldName("SavingAccountBalance")
                            .withFieldType("Edm.Int32")
                            .withIsArray(false)
                            .withIsRequired(true)))
            .withSchemaItemTypeLink("SchemaItemTypeLink")
            .withSmallImage("\\\\Images\\\\smallImage")
            .withMediumImage("\\\\Images\\\\MediumImage")
            .withLargeImage("\\\\Images\\\\LargeImage")
            .create();
    }
}
```

### Profiles_Delete

```java
/** Samples for Profiles Delete. */
public final class ProfilesDeleteSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/ProfilesDelete.json
     */
    /**
     * Sample code: Profiles_Delete.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void profilesDelete(com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager
            .profiles()
            .delete("TestHubRG", "sdkTestHub", "TestProfileType396", null, com.azure.core.util.Context.NONE);
    }
}
```

### Profiles_Get

```java
/** Samples for Profiles Get. */
public final class ProfilesGetSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/ProfilesGet.json
     */
    /**
     * Sample code: Profiles_Get.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void profilesGet(com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager
            .profiles()
            .getWithResponse("TestHubRG", "sdkTestHub", "TestProfileType396", null, com.azure.core.util.Context.NONE);
    }
}
```

### Profiles_GetEnrichingKpis

```java
/** Samples for Profiles GetEnrichingKpis. */
public final class ProfilesGetEnrichingKpisSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/ProfileGetEnrichingKpis.json
     */
    /**
     * Sample code: Profiles_GetEnrichingKpis.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void profilesGetEnrichingKpis(
        com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager
            .profiles()
            .getEnrichingKpisWithResponse(
                "TestHubRG", "sdkTestHub", "TestProfileType396", com.azure.core.util.Context.NONE);
    }
}
```

### Profiles_ListByHub

```java
/** Samples for Profiles ListByHub. */
public final class ProfilesListByHubSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/ProfilesListByHub.json
     */
    /**
     * Sample code: Profiles_ListByHub.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void profilesListByHub(com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager.profiles().listByHub("TestHubRG", "sdkTestHub", null, com.azure.core.util.Context.NONE);
    }
}
```

### RelationshipLinks_CreateOrUpdate

```java
import com.azure.resourcemanager.customerinsights.models.ParticipantProfilePropertyReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for RelationshipLinks CreateOrUpdate. */
public final class RelationshipLinksCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/RelationshipLinksCreateOrUpdate.json
     */
    /**
     * Sample code: RelationshipLinks_CreateOrUpdate.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void relationshipLinksCreateOrUpdate(
        com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager
            .relationshipLinks()
            .define("Somelink")
            .withExistingHub("TestHubRG", "sdkTestHub")
            .withDisplayName(mapOf("en-us", "Link DisplayName"))
            .withDescription(mapOf("en-us", "Link Description"))
            .withInteractionType("testInteraction4332")
            .withProfilePropertyReferences(
                Arrays
                    .asList(
                        new ParticipantProfilePropertyReference()
                            .withInteractionPropertyName("profile1")
                            .withProfilePropertyName("ProfileId")))
            .withRelatedProfilePropertyReferences(
                Arrays
                    .asList(
                        new ParticipantProfilePropertyReference()
                            .withInteractionPropertyName("profile1")
                            .withProfilePropertyName("ProfileId")))
            .withRelationshipName("testProfile2326994")
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

### RelationshipLinks_Delete

```java
/** Samples for RelationshipLinks Delete. */
public final class RelationshipLinksDeleteSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/RelationshipLinksDelete.json
     */
    /**
     * Sample code: RelationshipLinks_Delete.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void relationshipLinksDelete(
        com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager.relationshipLinks().delete("TestHubRG", "sdkTestHub", "Somelink", com.azure.core.util.Context.NONE);
    }
}
```

### RelationshipLinks_Get

```java
/** Samples for RelationshipLinks Get. */
public final class RelationshipLinksGetSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/RelationshipLinksGet.json
     */
    /**
     * Sample code: RelationshipLinks_Get.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void relationshipLinksGet(
        com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager
            .relationshipLinks()
            .getWithResponse("TestHubRG", "sdkTestHub", "Somelink", com.azure.core.util.Context.NONE);
    }
}
```

### RelationshipLinks_ListByHub

```java
/** Samples for RelationshipLinks ListByHub. */
public final class RelationshipLinksListByHubSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/RelationshipLinksListByHub.json
     */
    /**
     * Sample code: RelationshipLinks_ListByHub.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void relationshipLinksListByHub(
        com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager.relationshipLinks().listByHub("TestHubRG", "sdkTestHub", com.azure.core.util.Context.NONE);
    }
}
```

### Relationships_CreateOrUpdate

```java
import com.azure.resourcemanager.customerinsights.models.CardinalityTypes;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Relationships CreateOrUpdate. */
public final class RelationshipsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/RelationshipsCreateOrUpdate.json
     */
    /**
     * Sample code: Relationships_CreateOrUpdate.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void relationshipsCreateOrUpdate(
        com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager
            .relationships()
            .define("SomeRelationship")
            .withExistingHub("TestHubRG", "sdkTestHub")
            .withCardinality(CardinalityTypes.ONE_TO_ONE)
            .withDisplayName(mapOf("en-us", "Relationship DisplayName"))
            .withDescription(mapOf("en-us", "Relationship Description"))
            .withFields(Arrays.asList())
            .withProfileType("testProfile2326994")
            .withRelatedProfileType("testProfile2326994")
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

### Relationships_Delete

```java
/** Samples for Relationships Delete. */
public final class RelationshipsDeleteSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/RelationshipsDelete.json
     */
    /**
     * Sample code: Relationships_Delete.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void relationshipsDelete(com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager.relationships().delete("TestHubRG", "sdkTestHub", "SomeRelationship", com.azure.core.util.Context.NONE);
    }
}
```

### Relationships_Get

```java
/** Samples for Relationships Get. */
public final class RelationshipsGetSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/RelationshipsGet.json
     */
    /**
     * Sample code: Relationships_Get.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void relationshipsGet(com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager
            .relationships()
            .getWithResponse("TestHubRG", "sdkTestHub", "SomeRelationship", com.azure.core.util.Context.NONE);
    }
}
```

### Relationships_ListByHub

```java
/** Samples for Relationships ListByHub. */
public final class RelationshipsListByHubSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/RelationshipsListByHub.json
     */
    /**
     * Sample code: Relationships_ListByHub.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void relationshipsListByHub(
        com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager.relationships().listByHub("TestHubRG", "sdkTestHub", com.azure.core.util.Context.NONE);
    }
}
```

### RoleAssignments_CreateOrUpdate

```java
import com.azure.resourcemanager.customerinsights.models.AssignmentPrincipal;
import com.azure.resourcemanager.customerinsights.models.RoleTypes;
import java.util.Arrays;

/** Samples for RoleAssignments CreateOrUpdate. */
public final class RoleAssignmentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/RoleAssignmentsCreateOrUpdate.json
     */
    /**
     * Sample code: RoleAssignments_CreateOrUpdate.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void roleAssignmentsCreateOrUpdate(
        com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager
            .roleAssignments()
            .define("assignmentName8976")
            .withExistingHub("TestHubRG", "sdkTestHub")
            .withRole(RoleTypes.ADMIN)
            .withPrincipals(
                Arrays
                    .asList(
                        new AssignmentPrincipal()
                            .withPrincipalId("4c54c38ffa9b416ba5a6d6c8a20cbe7e")
                            .withPrincipalType("User"),
                        new AssignmentPrincipal()
                            .withPrincipalId("93061d15a5054f2b9948ae25724cf9d5")
                            .withPrincipalType("User")))
            .create();
    }
}
```

### RoleAssignments_Delete

```java
/** Samples for RoleAssignments Delete. */
public final class RoleAssignmentsDeleteSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/RoleAssignmentsDelete.json
     */
    /**
     * Sample code: RoleAssignments_Delete.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void roleAssignmentsDelete(
        com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager
            .roleAssignments()
            .deleteWithResponse("TestHubRG", "sdkTestHub", "assignmentName8976", com.azure.core.util.Context.NONE);
    }
}
```

### RoleAssignments_Get

```java
/** Samples for RoleAssignments Get. */
public final class RoleAssignmentsGetSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/RoleAssignmentsGet.json
     */
    /**
     * Sample code: RoleAssignments_Get.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void roleAssignmentsGet(com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager
            .roleAssignments()
            .getWithResponse("TestHubRG", "sdkTestHub", "assignmentName8976", com.azure.core.util.Context.NONE);
    }
}
```

### RoleAssignments_ListByHub

```java
/** Samples for RoleAssignments ListByHub. */
public final class RoleAssignmentsListByHubSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/RoleAssignmentsListByHub.json
     */
    /**
     * Sample code: RoleAssignments_ListByHub.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void roleAssignmentsListByHub(
        com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager.roleAssignments().listByHub("TestHubRG", "sdkTestHub", com.azure.core.util.Context.NONE);
    }
}
```

### Roles_ListByHub

```java
/** Samples for Roles ListByHub. */
public final class RolesListByHubSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/RolesListByHub.json
     */
    /**
     * Sample code: Roles_ListByHub.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void rolesListByHub(com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager.roles().listByHub("TestHubRG", "sdkTestHub", com.azure.core.util.Context.NONE);
    }
}
```

### Views_CreateOrUpdate

```java
import java.util.HashMap;
import java.util.Map;

/** Samples for Views CreateOrUpdate. */
public final class ViewsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/ViewsCreateOrUpdate.json
     */
    /**
     * Sample code: Views_CreateOrUpdate.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void viewsCreateOrUpdate(com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager
            .views()
            .define("testView")
            .withExistingHub("TestHubRG", "sdkTestHub")
            .withUserId("testUser")
            .withDisplayName(mapOf("en", "some name"))
            .withDefinition("{\\\"isProfileType\\\":false,\\\"profileTypes\\\":[],\\\"widgets\\\":[],\\\"style\\\":[]}")
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

### Views_Delete

```java
/** Samples for Views Delete. */
public final class ViewsDeleteSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/ViewsDelete.json
     */
    /**
     * Sample code: Views_Delete.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void viewsDelete(com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager
            .views()
            .deleteWithResponse("TestHubRG", "sdkTestHub", "testView", "*", com.azure.core.util.Context.NONE);
    }
}
```

### Views_Get

```java
/** Samples for Views Get. */
public final class ViewsGetSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/ViewsGet.json
     */
    /**
     * Sample code: Views_Get.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void viewsGet(com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager.views().getWithResponse("TestHubRG", "sdkTestHub", "testView", "*", com.azure.core.util.Context.NONE);
    }
}
```

### Views_ListByHub

```java
/** Samples for Views ListByHub. */
public final class ViewsListByHubSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/ViewsListByHub.json
     */
    /**
     * Sample code: Views_ListByHub.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void viewsListByHub(com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager.views().listByHub("TestHubRG", "sdkTestHub", "*", com.azure.core.util.Context.NONE);
    }
}
```

### WidgetTypes_Get

```java
/** Samples for WidgetTypes Get. */
public final class WidgetTypesGetSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/WidgetTypesGet.json
     */
    /**
     * Sample code: WidgetTypes_Get.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void widgetTypesGet(com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager
            .widgetTypes()
            .getWithResponse("TestHubRG", "sdkTestHub", "ActivityGauge", com.azure.core.util.Context.NONE);
    }
}
```

### WidgetTypes_ListByHub

```java
/** Samples for WidgetTypes ListByHub. */
public final class WidgetTypesListByHubSamples {
    /*
     * x-ms-original-file: specification/customer-insights/resource-manager/Microsoft.CustomerInsights/stable/2017-04-26/examples/WidgetTypesListByHub.json
     */
    /**
     * Sample code: WidgetTypes_ListByHub.
     *
     * @param manager Entry point to CustomerInsightsManager.
     */
    public static void widgetTypesListByHub(
        com.azure.resourcemanager.customerinsights.CustomerInsightsManager manager) {
        manager.widgetTypes().listByHub("TestHubRG", "sdkTestHub", com.azure.core.util.Context.NONE);
    }
}
```


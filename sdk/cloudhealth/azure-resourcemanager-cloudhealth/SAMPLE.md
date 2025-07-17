# Code snippets and samples


## AuthenticationSettings

- [CreateOrUpdate](#authenticationsettings_createorupdate)
- [Delete](#authenticationsettings_delete)
- [Get](#authenticationsettings_get)
- [ListByHealthModel](#authenticationsettings_listbyhealthmodel)

## DiscoveryRules

- [CreateOrUpdate](#discoveryrules_createorupdate)
- [Delete](#discoveryrules_delete)
- [Get](#discoveryrules_get)
- [ListByHealthModel](#discoveryrules_listbyhealthmodel)

## Entities

- [CreateOrUpdate](#entities_createorupdate)
- [Delete](#entities_delete)
- [Get](#entities_get)
- [ListByHealthModel](#entities_listbyhealthmodel)

## HealthModels

- [Create](#healthmodels_create)
- [Delete](#healthmodels_delete)
- [GetByResourceGroup](#healthmodels_getbyresourcegroup)
- [List](#healthmodels_list)
- [ListByResourceGroup](#healthmodels_listbyresourcegroup)
- [Update](#healthmodels_update)

## Operations

- [List](#operations_list)

## Relationships

- [CreateOrUpdate](#relationships_createorupdate)
- [Delete](#relationships_delete)
- [Get](#relationships_get)
- [ListByHealthModel](#relationships_listbyhealthmodel)

## SignalDefinitions

- [CreateOrUpdate](#signaldefinitions_createorupdate)
- [Delete](#signaldefinitions_delete)
- [Get](#signaldefinitions_get)
- [ListByHealthModel](#signaldefinitions_listbyhealthmodel)
### AuthenticationSettings_CreateOrUpdate

```java
import com.azure.resourcemanager.cloudhealth.models.ManagedIdentityAuthenticationSettingProperties;

/**
 * Samples for AuthenticationSettings CreateOrUpdate.
 */
public final class AuthenticationSettingsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/AuthenticationSettings_CreateOrUpdate.json
     */
    /**
     * Sample code: AuthenticationSettings_CreateOrUpdate.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void
        authenticationSettingsCreateOrUpdate(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.authenticationSettings()
            .define("myAuthSetting")
            .withExistingHealthmodel("myResourceGroup", "myHealthModel")
            .withProperties(new ManagedIdentityAuthenticationSettingProperties().withDisplayName("myDisplayName")
                .withManagedIdentityName("SystemAssigned"))
            .create();
    }
}
```

### AuthenticationSettings_Delete

```java
/**
 * Samples for AuthenticationSettings Delete.
 */
public final class AuthenticationSettingsDeleteSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/AuthenticationSettings_Delete.json
     */
    /**
     * Sample code: AuthenticationSettings_Delete.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void authenticationSettingsDelete(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.authenticationSettings()
            .deleteWithResponse("my-resource-group", "my-health-model", "my-auth-setting",
                com.azure.core.util.Context.NONE);
    }
}
```

### AuthenticationSettings_Get

```java
/**
 * Samples for AuthenticationSettings Get.
 */
public final class AuthenticationSettingsGetSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/AuthenticationSettings_Get.json
     */
    /**
     * Sample code: AuthenticationSettings_Get.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void authenticationSettingsGet(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.authenticationSettings()
            .getWithResponse("my-resource-group", "my-health-model", "my-auth-setting",
                com.azure.core.util.Context.NONE);
    }
}
```

### AuthenticationSettings_ListByHealthModel

```java
/**
 * Samples for AuthenticationSettings ListByHealthModel.
 */
public final class AuthenticationSettingsListByHealthModelSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/AuthenticationSettings_ListByHealthModel.json
     */
    /**
     * Sample code: AuthenticationSettings_ListByHealthModel.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void
        authenticationSettingsListByHealthModel(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.authenticationSettings()
            .listByHealthModel("my-resource-group", "my-health-model", com.azure.core.util.Context.NONE);
    }
}
```

### DiscoveryRules_CreateOrUpdate

```java
import com.azure.resourcemanager.cloudhealth.models.DiscoveryRuleProperties;
import com.azure.resourcemanager.cloudhealth.models.DiscoveryRuleRecommendedSignalsBehavior;
import com.azure.resourcemanager.cloudhealth.models.DiscoveryRuleRelationshipDiscoveryBehavior;

/**
 * Samples for DiscoveryRules CreateOrUpdate.
 */
public final class DiscoveryRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/DiscoveryRules_CreateOrUpdate.json
     */
    /**
     * Sample code: DiscoveryRules_CreateOrUpdate.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void discoveryRulesCreateOrUpdate(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.discoveryRules()
            .define("myDiscoveryRule")
            .withExistingHealthmodel("myResourceGroup", "myHealthModel")
            .withProperties(new DiscoveryRuleProperties().withDisplayName("myDisplayName")
                .withResourceGraphQuery(
                    "resources | where subscriptionId == '7ddfffd7-9b32-40df-1234-828cbd55d6f4' | where resourceGroup == 'my-rg'")
                .withAuthenticationSetting("authSetting1")
                .withDiscoverRelationships(DiscoveryRuleRelationshipDiscoveryBehavior.ENABLED)
                .withAddRecommendedSignals(DiscoveryRuleRecommendedSignalsBehavior.ENABLED))
            .create();
    }
}
```

### DiscoveryRules_Delete

```java
/**
 * Samples for DiscoveryRules Delete.
 */
public final class DiscoveryRulesDeleteSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/DiscoveryRules_Delete.json
     */
    /**
     * Sample code: DiscoveryRules_Delete.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void discoveryRulesDelete(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.discoveryRules()
            .deleteWithResponse("my-resource-group", "my-health-model", "my-discovery-rule",
                com.azure.core.util.Context.NONE);
    }
}
```

### DiscoveryRules_Get

```java
/**
 * Samples for DiscoveryRules Get.
 */
public final class DiscoveryRulesGetSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/DiscoveryRules_Get.json
     */
    /**
     * Sample code: DiscoveryRules_Get.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void discoveryRulesGet(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.discoveryRules()
            .getWithResponse("myResourceGroup", "myHealthModel", "myDiscoveryRule", com.azure.core.util.Context.NONE);
    }
}
```

### DiscoveryRules_ListByHealthModel

```java

/**
 * Samples for DiscoveryRules ListByHealthModel.
 */
public final class DiscoveryRulesListByHealthModelSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/DiscoveryRules_ListByHealthModel.json
     */
    /**
     * Sample code: DiscoveryRules_ListByHealthModel.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void
        discoveryRulesListByHealthModel(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.discoveryRules()
            .listByHealthModel("my-resource-group", "my-health-model", null, com.azure.core.util.Context.NONE);
    }
}
```

### Entities_CreateOrUpdate

```java
import com.azure.resourcemanager.cloudhealth.models.AlertConfiguration;
import com.azure.resourcemanager.cloudhealth.models.AlertSeverity;
import com.azure.resourcemanager.cloudhealth.models.AzureMonitorWorkspaceSignalGroup;
import com.azure.resourcemanager.cloudhealth.models.AzureResourceSignalGroup;
import com.azure.resourcemanager.cloudhealth.models.DependenciesAggregationType;
import com.azure.resourcemanager.cloudhealth.models.DependenciesSignalGroup;
import com.azure.resourcemanager.cloudhealth.models.EntityAlerts;
import com.azure.resourcemanager.cloudhealth.models.EntityCoordinates;
import com.azure.resourcemanager.cloudhealth.models.EntityImpact;
import com.azure.resourcemanager.cloudhealth.models.EntityProperties;
import com.azure.resourcemanager.cloudhealth.models.IconDefinition;
import com.azure.resourcemanager.cloudhealth.models.LogAnalyticsSignalGroup;
import com.azure.resourcemanager.cloudhealth.models.SignalAssignment;
import com.azure.resourcemanager.cloudhealth.models.SignalGroup;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Entities CreateOrUpdate.
 */
public final class EntitiesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/Entities_CreateOrUpdate.json
     */
    /**
     * Sample code: Entities_CreateOrUpdate.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void entitiesCreateOrUpdate(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.entities()
            .define("uszrxbdkxesdrxhmagmzywebgbjj")
            .withExistingHealthmodel("rgopenapi", "myHealthModel")
            .withProperties(new EntityProperties().withDisplayName("My entity")
                .withCanvasPosition(new EntityCoordinates().withX(14.0).withY(13.0))
                .withIcon(new IconDefinition().withIconName("Custom").withCustomData("rcitntvapruccrhtxmkqjphbxunkz"))
                .withHealthObjective(62.0D)
                .withImpact(EntityImpact.STANDARD)
                .withLabels(mapOf("key1376", "fakeTokenPlaceholder"))
                .withSignals(new SignalGroup().withAzureResource(new AzureResourceSignalGroup()
                    .withSignalAssignments(
                        Arrays.asList(new SignalAssignment().withSignalDefinitions(Arrays.asList("sigdef1"))))
                    .withAuthenticationSetting("B3P1X3e-FZtZ-4Ak-2VLHGQ-4m4-05DE-XNW5zW3P-46XY-DC3SSX")
                    .withAzureResourceId(
                        "/subscriptions/12345678-1234-1234-1234-123456789012/resourceGroups/rg1/providers/Microsoft.Compute/virtualMachines/vm1"))
                    .withAzureLogAnalytics(new LogAnalyticsSignalGroup()
                        .withSignalAssignments(Arrays.asList(new SignalAssignment().withSignalDefinitions(
                            Arrays.asList("B3P1X3e-FZtZ-4Ak-2VLHGQ-4m4-05DE-XNW5zW3P-46XY-DC3SSX"))))
                        .withAuthenticationSetting("B3P1X3e-FZtZ-4Ak-2VLHGQ-4m4-05DE-XNW5zW3P-46XY-DC3SSX")
                        .withLogAnalyticsWorkspaceResourceId(
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg/providers/Microsoft.OperationalInsights/workspaces/myworkspace"))
                    .withAzureMonitorWorkspace(new AzureMonitorWorkspaceSignalGroup()
                        .withSignalAssignments(
                            Arrays.asList(new SignalAssignment().withSignalDefinitions(Arrays.asList("sigdef2")),
                                new SignalAssignment().withSignalDefinitions(Arrays.asList("sigdef3"))))
                        .withAuthenticationSetting("B3P1X3e-FZtZ-4Ak-2VLHGQ-4m4-05DE-XNW5zW3P-46XY-DC3SSX")
                        .withAzureMonitorWorkspaceResourceId(
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg/providers/Microsoft.OperationalInsights/workspaces/myworkspace"))
                    .withDependencies(
                        new DependenciesSignalGroup().withAggregationType(DependenciesAggregationType.WORST_OF)))
                .withAlerts(new EntityAlerts().withUnhealthy(new AlertConfiguration().withSeverity(AlertSeverity.SEV1)
                    .withDescription("Alert description")
                    .withActionGroupIds(Arrays.asList(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg/providers/Microsoft.Insights/actionGroups/myactiongroup")))
                    .withDegraded(new AlertConfiguration().withSeverity(AlertSeverity.SEV4)
                        .withDescription("Alert description")
                        .withActionGroupIds(Arrays.asList(
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg/providers/Microsoft.Insights/actionGroups/myactiongroup")))))
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

### Entities_Delete

```java
/**
 * Samples for Entities Delete.
 */
public final class EntitiesDeleteSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/Entities_Delete.json
     */
    /**
     * Sample code: Entities_Delete.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void entitiesDelete(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.entities()
            .deleteWithResponse("rgopenapi", "model1", "U4VTRFlUkm9kR6H23-c-6U-XHq7n",
                com.azure.core.util.Context.NONE);
    }
}
```

### Entities_Get

```java
/**
 * Samples for Entities Get.
 */
public final class EntitiesGetSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/Entities_Get.json
     */
    /**
     * Sample code: Entities_Get.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void entitiesGet(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.entities().getWithResponse("rgopenapi", "myHealthModel", "entity1", com.azure.core.util.Context.NONE);
    }
}
```

### Entities_ListByHealthModel

```java

/**
 * Samples for Entities ListByHealthModel.
 */
public final class EntitiesListByHealthModelSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/Entities_ListByHealthModel.json
     */
    /**
     * Sample code: Entities_ListByHealthModel.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void entitiesListByHealthModel(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.entities()
            .listByHealthModel("rgopenapi", "gPWT6GP85xRV248L7LhNRTD--2Yc73wu-5Qk-0tS", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### HealthModels_Create

```java
import com.azure.resourcemanager.cloudhealth.models.DiscoveryRuleRecommendedSignalsBehavior;
import com.azure.resourcemanager.cloudhealth.models.HealthModelProperties;
import com.azure.resourcemanager.cloudhealth.models.ManagedServiceIdentity;
import com.azure.resourcemanager.cloudhealth.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.cloudhealth.models.ModelDiscoverySettings;
import com.azure.resourcemanager.cloudhealth.models.UserAssignedIdentity;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for HealthModels Create.
 */
public final class HealthModelsCreateSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/HealthModels_Create.json
     */
    /**
     * Sample code: HealthModels_Create.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void healthModelsCreate(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.healthModels()
            .define("model1")
            .withRegion("eastus2")
            .withExistingResourceGroup("rgopenapi")
            .withTags(mapOf("key2961", "fakeTokenPlaceholder"))
            .withProperties(new HealthModelProperties().withDiscovery(
                new ModelDiscoverySettings().withScope("/providers/Microsoft.Management/serviceGroups/myServiceGroup")
                    .withAddRecommendedSignals(DiscoveryRuleRecommendedSignalsBehavior.ENABLED)
                    .withIdentity("SystemAssigned")))
            .withIdentity(new ManagedServiceIdentity()
                .withType(ManagedServiceIdentityType.fromString("SystemAssigned, UserAssigned"))
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/4980D7D5-4E07-47AD-AD34-E76C6BC9F061/resourceGroups/rgopenapi/providers/Microsoft.ManagedIdentity/userAssignedIdentities/ua1",
                    new UserAssignedIdentity())))
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

### HealthModels_Delete

```java
/**
 * Samples for HealthModels Delete.
 */
public final class HealthModelsDeleteSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/HealthModels_Delete.json
     */
    /**
     * Sample code: HealthModels_Delete.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void healthModelsDelete(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.healthModels().delete("rgopenapi", "model1", com.azure.core.util.Context.NONE);
    }
}
```

### HealthModels_GetByResourceGroup

```java
/**
 * Samples for HealthModels GetByResourceGroup.
 */
public final class HealthModelsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/HealthModels_Get.json
     */
    /**
     * Sample code: HealthModels_Get.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void healthModelsGet(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.healthModels()
            .getByResourceGroupWithResponse("rgopenapi", "myHealthModel", com.azure.core.util.Context.NONE);
    }
}
```

### HealthModels_List

```java
/**
 * Samples for HealthModels List.
 */
public final class HealthModelsListSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/HealthModels_ListBySubscription.json
     */
    /**
     * Sample code: HealthModels_ListBySubscription.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void
        healthModelsListBySubscription(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.healthModels().list(com.azure.core.util.Context.NONE);
    }
}
```

### HealthModels_ListByResourceGroup

```java
/**
 * Samples for HealthModels ListByResourceGroup.
 */
public final class HealthModelsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/HealthModels_ListByResourceGroup.json
     */
    /**
     * Sample code: HealthModels_ListByResourceGroup.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void
        healthModelsListByResourceGroup(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.healthModels().listByResourceGroup("rgopenapi", com.azure.core.util.Context.NONE);
    }
}
```

### HealthModels_Update

```java
import com.azure.resourcemanager.cloudhealth.models.HealthModel;
import com.azure.resourcemanager.cloudhealth.models.ManagedServiceIdentity;
import com.azure.resourcemanager.cloudhealth.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.cloudhealth.models.UserAssignedIdentity;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for HealthModels Update.
 */
public final class HealthModelsUpdateSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/HealthModels_Update.json
     */
    /**
     * Sample code: HealthModels_Update.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void healthModelsUpdate(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        HealthModel resource = manager.healthModels()
            .getByResourceGroupWithResponse("rgopenapi", "model1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key21", "fakeTokenPlaceholder"))
            .withIdentity(new ManagedServiceIdentity()
                .withType(ManagedServiceIdentityType.fromString("SystemAssigned, UserAssigned"))
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/4980D7D5-4E07-47AD-AD34-E76C6BC9F061/resourceGroups/rgopenapi/providers/Microsoft.ManagedIdentity/userAssignedIdentities/ua1",
                    new UserAssignedIdentity())))
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

### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void operationsList(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Relationships_CreateOrUpdate

```java
import com.azure.resourcemanager.cloudhealth.models.RelationshipProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Relationships CreateOrUpdate.
 */
public final class RelationshipsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/Relationships_CreateOrUpdate.json
     */
    /**
     * Sample code: Relationships_CreateOrUpdate.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void relationshipsCreateOrUpdate(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.relationships()
            .define("rel1")
            .withExistingHealthmodel("rgopenapi", "model1")
            .withProperties(new RelationshipProperties().withDisplayName("My relationship")
                .withParentEntityName("Entity1")
                .withChildEntityName("Entity2")
                .withLabels(mapOf("key9681", "fakeTokenPlaceholder")))
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

### Relationships_Delete

```java
/**
 * Samples for Relationships Delete.
 */
public final class RelationshipsDeleteSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/Relationships_Delete.json
     */
    /**
     * Sample code: Relationships_Delete.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void relationshipsDelete(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.relationships().deleteWithResponse("rgopenapi", "model1", "rel1", com.azure.core.util.Context.NONE);
    }
}
```

### Relationships_Get

```java
/**
 * Samples for Relationships Get.
 */
public final class RelationshipsGetSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/Relationships_Get.json
     */
    /**
     * Sample code: Relationships_Get.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void relationshipsGet(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.relationships()
            .getWithResponse("rgopenapi", "myHealthModel", "Ue-21-F3M12V3w-13x18F8H-7HOk--kq6tP-HB",
                com.azure.core.util.Context.NONE);
    }
}
```

### Relationships_ListByHealthModel

```java

/**
 * Samples for Relationships ListByHealthModel.
 */
public final class RelationshipsListByHealthModelSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/Relationships_ListByHealthModel.json
     */
    /**
     * Sample code: Relationships_ListByHealthModel.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void
        relationshipsListByHealthModel(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.relationships().listByHealthModel("rgopenapi", "model1", null, com.azure.core.util.Context.NONE);
    }
}
```

### SignalDefinitions_CreateOrUpdate

```java
import com.azure.resourcemanager.cloudhealth.models.EvaluationRule;
import com.azure.resourcemanager.cloudhealth.models.MetricAggregationType;
import com.azure.resourcemanager.cloudhealth.models.RefreshInterval;
import com.azure.resourcemanager.cloudhealth.models.ResourceMetricSignalDefinitionProperties;
import com.azure.resourcemanager.cloudhealth.models.SignalOperator;
import com.azure.resourcemanager.cloudhealth.models.ThresholdRule;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SignalDefinitions CreateOrUpdate.
 */
public final class SignalDefinitionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/SignalDefinitions_CreateOrUpdate.json
     */
    /**
     * Sample code: SignalDefinitions_CreateOrUpdate.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void
        signalDefinitionsCreateOrUpdate(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.signalDefinitions()
            .define("sig1")
            .withExistingHealthmodel("rgopenapi", "myHealthModel")
            .withProperties(new ResourceMetricSignalDefinitionProperties().withDisplayName("cpu usage")
                .withRefreshInterval(RefreshInterval.PT1M)
                .withLabels(mapOf("key4788", "fakeTokenPlaceholder"))
                .withDataUnit("byte")
                .withEvaluationRules(new EvaluationRule()
                    .withDegradedRule(new ThresholdRule().withOperator(SignalOperator.LOWER_THAN).withThreshold("65"))
                    .withUnhealthyRule(new ThresholdRule().withOperator(SignalOperator.LOWER_THAN).withThreshold("60")))
                .withMetricNamespace("microsoft.compute/virtualMachines")
                .withMetricName("cpuusage")
                .withTimeGrain("PT1M")
                .withAggregationType(MetricAggregationType.NONE)
                .withDimension("nodename")
                .withDimensionFilter("node1"))
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

### SignalDefinitions_Delete

```java
/**
 * Samples for SignalDefinitions Delete.
 */
public final class SignalDefinitionsDeleteSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/SignalDefinitions_Delete.json
     */
    /**
     * Sample code: SignalDefinitions_Delete.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void signalDefinitionsDelete(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.signalDefinitions().deleteWithResponse("rgopenapi", "model1", "sig", com.azure.core.util.Context.NONE);
    }
}
```

### SignalDefinitions_Get

```java
/**
 * Samples for SignalDefinitions Get.
 */
public final class SignalDefinitionsGetSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/SignalDefinitions_Get.json
     */
    /**
     * Sample code: SignalDefinitions_Get.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void signalDefinitionsGet(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.signalDefinitions()
            .getWithResponse("rgopenapi", "myHealthModel", "sig1", com.azure.core.util.Context.NONE);
    }
}
```

### SignalDefinitions_ListByHealthModel

```java

/**
 * Samples for SignalDefinitions ListByHealthModel.
 */
public final class SignalDefinitionsListByHealthModelSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/SignalDefinitions_ListByHealthModel.json
     */
    /**
     * Sample code: SignalDefinitions_ListByHealthModel.
     * 
     * @param manager Entry point to CloudHealthManager.
     */
    public static void
        signalDefinitionsListByHealthModel(com.azure.resourcemanager.cloudhealth.CloudHealthManager manager) {
        manager.signalDefinitions()
            .listByHealthModel("rgopenapi", "myHealthModel", null, com.azure.core.util.Context.NONE);
    }
}
```


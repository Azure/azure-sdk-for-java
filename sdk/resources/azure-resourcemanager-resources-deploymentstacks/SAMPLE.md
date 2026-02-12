# Code snippets and samples


## DeploymentStacks

- [CreateOrUpdateAtManagementGroup](#deploymentstacks_createorupdateatmanagementgroup)
- [CreateOrUpdateAtResourceGroup](#deploymentstacks_createorupdateatresourcegroup)
- [CreateOrUpdateAtSubscription](#deploymentstacks_createorupdateatsubscription)
- [Delete](#deploymentstacks_delete)
- [DeleteAtManagementGroup](#deploymentstacks_deleteatmanagementgroup)
- [DeleteAtSubscription](#deploymentstacks_deleteatsubscription)
- [ExportTemplateAtManagementGroup](#deploymentstacks_exporttemplateatmanagementgroup)
- [ExportTemplateAtResourceGroup](#deploymentstacks_exporttemplateatresourcegroup)
- [ExportTemplateAtSubscription](#deploymentstacks_exporttemplateatsubscription)
- [GetAtManagementGroup](#deploymentstacks_getatmanagementgroup)
- [GetAtSubscription](#deploymentstacks_getatsubscription)
- [GetByResourceGroup](#deploymentstacks_getbyresourcegroup)
- [List](#deploymentstacks_list)
- [ListAtManagementGroup](#deploymentstacks_listatmanagementgroup)
- [ListByResourceGroup](#deploymentstacks_listbyresourcegroup)
- [ValidateStackAtManagementGroup](#deploymentstacks_validatestackatmanagementgroup)
- [ValidateStackAtResourceGroup](#deploymentstacks_validatestackatresourcegroup)
- [ValidateStackAtSubscription](#deploymentstacks_validatestackatsubscription)

## DeploymentStacksWhatIfResultsAtManagementGroup

- [CreateOrUpdate](#deploymentstackswhatifresultsatmanagementgroup_createorupdate)
- [Delete](#deploymentstackswhatifresultsatmanagementgroup_delete)
- [Get](#deploymentstackswhatifresultsatmanagementgroup_get)
- [List](#deploymentstackswhatifresultsatmanagementgroup_list)
- [WhatIf](#deploymentstackswhatifresultsatmanagementgroup_whatif)

## DeploymentStacksWhatIfResultsAtResourceGroup

- [CreateOrUpdate](#deploymentstackswhatifresultsatresourcegroup_createorupdate)
- [Delete](#deploymentstackswhatifresultsatresourcegroup_delete)
- [GetByResourceGroup](#deploymentstackswhatifresultsatresourcegroup_getbyresourcegroup)
- [ListByResourceGroup](#deploymentstackswhatifresultsatresourcegroup_listbyresourcegroup)
- [WhatIf](#deploymentstackswhatifresultsatresourcegroup_whatif)

## DeploymentStacksWhatIfResultsAtSubscription

- [CreateOrUpdate](#deploymentstackswhatifresultsatsubscription_createorupdate)
- [Delete](#deploymentstackswhatifresultsatsubscription_delete)
- [Get](#deploymentstackswhatifresultsatsubscription_get)
- [List](#deploymentstackswhatifresultsatsubscription_list)
- [WhatIf](#deploymentstackswhatifresultsatsubscription_whatif)
### DeploymentStacks_CreateOrUpdateAtManagementGroup

```java
import com.azure.resourcemanager.resources.deploymentstacks.fluent.models.DeploymentStackInner;
import com.azure.resourcemanager.resources.deploymentstacks.models.ActionOnUnmanage;
import com.azure.resourcemanager.resources.deploymentstacks.models.DenySettings;
import com.azure.resourcemanager.resources.deploymentstacks.models.DenySettingsMode;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentExtensionConfig;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentExtensionConfigItem;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentParameter;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentStackProperties;
import com.azure.resourcemanager.resources.deploymentstacks.models.UnmanageActionManagementGroupMode;
import com.azure.resourcemanager.resources.deploymentstacks.models.UnmanageActionResourceGroupMode;
import com.azure.resourcemanager.resources.deploymentstacks.models.UnmanageActionResourceMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for DeploymentStacks CreateOrUpdateAtManagementGroup.
 */
public final class DeploymentStacksCreateOrUpdateAtManagementGroupSamples {
    /*
     * x-ms-original-file: 2025-07-01/DeploymentStackManagementGroupCreate.json
     */
    /**
     * Sample code: Create or update a management group Deployment stack.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void createOrUpdateAManagementGroupDeploymentStack(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacks()
            .createOrUpdateAtManagementGroup("myMg", "simpleDeploymentStack",
                new DeploymentStackInner()
                    .withProperties(new DeploymentStackProperties()
                        .withParameters(mapOf("parameter1", new DeploymentParameter().withValue("a string")))
                        .withExtensionConfigs(mapOf("contoso",
                            new DeploymentExtensionConfig().withAdditionalProperties(
                                mapOf("configTwo", new DeploymentExtensionConfigItem().withValue(true), "configOne",
                                    new DeploymentExtensionConfigItem().withValue("config1Value")))))
                        .withActionOnUnmanage(new ActionOnUnmanage().withResources(UnmanageActionResourceMode.DELETE)
                            .withResourceGroups(UnmanageActionResourceGroupMode.DELETE)
                            .withManagementGroups(UnmanageActionManagementGroupMode.DETACH))
                        .withDenySettings(new DenySettings().withMode(DenySettingsMode.DENY_DELETE)
                            .withExcludedPrincipals(Arrays.asList("principal"))
                            .withExcludedActions(Arrays.asList("action"))
                            .withApplyToChildScopes(false)))
                    .withLocation("eastus")
                    .withTags(mapOf("tagkey", "fakeTokenPlaceholder")),
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

### DeploymentStacks_CreateOrUpdateAtResourceGroup

```java
import com.azure.resourcemanager.resources.deploymentstacks.models.ActionOnUnmanage;
import com.azure.resourcemanager.resources.deploymentstacks.models.DenySettings;
import com.azure.resourcemanager.resources.deploymentstacks.models.DenySettingsMode;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentExtensionConfig;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentExtensionConfigItem;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentParameter;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentStackProperties;
import com.azure.resourcemanager.resources.deploymentstacks.models.UnmanageActionManagementGroupMode;
import com.azure.resourcemanager.resources.deploymentstacks.models.UnmanageActionResourceGroupMode;
import com.azure.resourcemanager.resources.deploymentstacks.models.UnmanageActionResourceMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for DeploymentStacks CreateOrUpdateAtResourceGroup.
 */
public final class DeploymentStacksCreateOrUpdateAtResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-07-01/DeploymentStackResourceGroupCreate.json
     */
    /**
     * Sample code: Create or update a resource group Deployment stack.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void createOrUpdateAResourceGroupDeploymentStack(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacks()
            .define("simpleDeploymentStack")
            .withExistingResourceGroup("deploymentStacksRG")
            .withRegion("eastus")
            .withTags(mapOf("tagkey", "fakeTokenPlaceholder"))
            .withProperties(new DeploymentStackProperties()
                .withParameters(mapOf("parameter1", new DeploymentParameter().withValue("a string")))
                .withExtensionConfigs(mapOf("contoso",
                    new DeploymentExtensionConfig().withAdditionalProperties(
                        mapOf("configTwo", new DeploymentExtensionConfigItem().withValue(true), "configOne",
                            new DeploymentExtensionConfigItem().withValue("config1Value")))))
                .withActionOnUnmanage(new ActionOnUnmanage().withResources(UnmanageActionResourceMode.DELETE)
                    .withResourceGroups(UnmanageActionResourceGroupMode.DELETE)
                    .withManagementGroups(UnmanageActionManagementGroupMode.DETACH))
                .withDenySettings(new DenySettings().withMode(DenySettingsMode.DENY_DELETE)
                    .withExcludedPrincipals(Arrays.asList("principal"))
                    .withExcludedActions(Arrays.asList("action"))
                    .withApplyToChildScopes(false)))
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

### DeploymentStacks_CreateOrUpdateAtSubscription

```java
import com.azure.resourcemanager.resources.deploymentstacks.fluent.models.DeploymentStackInner;
import com.azure.resourcemanager.resources.deploymentstacks.models.ActionOnUnmanage;
import com.azure.resourcemanager.resources.deploymentstacks.models.DenySettings;
import com.azure.resourcemanager.resources.deploymentstacks.models.DenySettingsMode;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentExtensionConfig;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentExtensionConfigItem;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentParameter;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentStackProperties;
import com.azure.resourcemanager.resources.deploymentstacks.models.UnmanageActionManagementGroupMode;
import com.azure.resourcemanager.resources.deploymentstacks.models.UnmanageActionResourceGroupMode;
import com.azure.resourcemanager.resources.deploymentstacks.models.UnmanageActionResourceMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for DeploymentStacks CreateOrUpdateAtSubscription.
 */
public final class DeploymentStacksCreateOrUpdateAtSubscriptionSamples {
    /*
     * x-ms-original-file: 2025-07-01/DeploymentStackSubscriptionCreate.json
     */
    /**
     * Sample code: Create or update a subscription Deployment stack.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void createOrUpdateASubscriptionDeploymentStack(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacks()
            .createOrUpdateAtSubscription("simpleDeploymentStack",
                new DeploymentStackInner()
                    .withProperties(new DeploymentStackProperties()
                        .withParameters(mapOf("parameter1", new DeploymentParameter().withValue("a string")))
                        .withExtensionConfigs(mapOf("contoso",
                            new DeploymentExtensionConfig().withAdditionalProperties(
                                mapOf("configTwo", new DeploymentExtensionConfigItem().withValue(true), "configOne",
                                    new DeploymentExtensionConfigItem().withValue("config1Value")))))
                        .withActionOnUnmanage(new ActionOnUnmanage().withResources(UnmanageActionResourceMode.DELETE)
                            .withResourceGroups(UnmanageActionResourceGroupMode.DELETE)
                            .withManagementGroups(UnmanageActionManagementGroupMode.DETACH))
                        .withDenySettings(new DenySettings().withMode(DenySettingsMode.DENY_DELETE)
                            .withExcludedPrincipals(Arrays.asList("principal"))
                            .withExcludedActions(Arrays.asList("action"))
                            .withApplyToChildScopes(false)))
                    .withLocation("eastus")
                    .withTags(mapOf("tagkey", "fakeTokenPlaceholder")),
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

### DeploymentStacks_Delete

```java

/**
 * Samples for DeploymentStacks Delete.
 */
public final class DeploymentStacksDeleteSamples {
    /*
     * x-ms-original-file: 2025-07-01/DeploymentStackResourceGroupDelete.json
     */
    /**
     * Sample code: Delete a resource group Deployment stack.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void deleteAResourceGroupDeploymentStack(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacks()
            .delete("deploymentStacksRG", "simpleDeploymentStack", null, null, null, null, null,
                com.azure.core.util.Context.NONE);
    }
}
```

### DeploymentStacks_DeleteAtManagementGroup

```java

/**
 * Samples for DeploymentStacks DeleteAtManagementGroup.
 */
public final class DeploymentStacksDeleteAtManagementGroupSamples {
    /*
     * x-ms-original-file: 2025-07-01/DeploymentStackManagementGroupDelete.json
     */
    /**
     * Sample code: Delete a management group Deployment stack.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void deleteAManagementGroupDeploymentStack(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacks()
            .deleteAtManagementGroup("myMg", "simpleDeploymentStack", null, null, null, null, null,
                com.azure.core.util.Context.NONE);
    }
}
```

### DeploymentStacks_DeleteAtSubscription

```java

/**
 * Samples for DeploymentStacks DeleteAtSubscription.
 */
public final class DeploymentStacksDeleteAtSubscriptionSamples {
    /*
     * x-ms-original-file: 2025-07-01/DeploymentStackSubscriptionDelete.json
     */
    /**
     * Sample code: Delete a subscription Deployment stack.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void deleteASubscriptionDeploymentStack(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacks()
            .deleteAtSubscription("simpleDeploymentStack", null, null, null, null, null,
                com.azure.core.util.Context.NONE);
    }
}
```

### DeploymentStacks_ExportTemplateAtManagementGroup

```java
/**
 * Samples for DeploymentStacks ExportTemplateAtManagementGroup.
 */
public final class DeploymentStacksExportTemplateAtManagementGroupSamples {
    /*
     * x-ms-original-file: 2025-07-01/DeploymentStackManagementGroupExportTemplate.json
     */
    /**
     * Sample code: Export the Deployment template for a management group Deployment stack.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void exportTheDeploymentTemplateForAManagementGroupDeploymentStack(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacks()
            .exportTemplateAtManagementGroupWithResponse("myMg", "simpleDeploymentStack",
                com.azure.core.util.Context.NONE);
    }
}
```

### DeploymentStacks_ExportTemplateAtResourceGroup

```java
/**
 * Samples for DeploymentStacks ExportTemplateAtResourceGroup.
 */
public final class DeploymentStacksExportTemplateAtResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-07-01/DeploymentStackExportTemplate.json
     */
    /**
     * Sample code: Export the Deployment template for a resource group Deployment stack.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void exportTheDeploymentTemplateForAResourceGroupDeploymentStack(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacks()
            .exportTemplateAtResourceGroupWithResponse("deploymentStacksRG", "simpleDeploymentStack",
                com.azure.core.util.Context.NONE);
    }
}
```

### DeploymentStacks_ExportTemplateAtSubscription

```java
/**
 * Samples for DeploymentStacks ExportTemplateAtSubscription.
 */
public final class DeploymentStacksExportTemplateAtSubscriptionSamples {
    /*
     * x-ms-original-file: 2025-07-01/DeploymentStackSubscriptionExportTemplate.json
     */
    /**
     * Sample code: Export the Deployment template for a subscription Deployment stack.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void exportTheDeploymentTemplateForASubscriptionDeploymentStack(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacks()
            .exportTemplateAtSubscriptionWithResponse("simpleDeploymentStack", com.azure.core.util.Context.NONE);
    }
}
```

### DeploymentStacks_GetAtManagementGroup

```java
/**
 * Samples for DeploymentStacks GetAtManagementGroup.
 */
public final class DeploymentStacksGetAtManagementGroupSamples {
    /*
     * x-ms-original-file: 2025-07-01/DeploymentStackManagementGroupGet.json
     */
    /**
     * Sample code: Get a management group Deployment stack.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void getAManagementGroupDeploymentStack(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacks()
            .getAtManagementGroupWithResponse("myMg", "simpleDeploymentStack", com.azure.core.util.Context.NONE);
    }
}
```

### DeploymentStacks_GetAtSubscription

```java
/**
 * Samples for DeploymentStacks GetAtSubscription.
 */
public final class DeploymentStacksGetAtSubscriptionSamples {
    /*
     * x-ms-original-file: 2025-07-01/DeploymentStackSubscriptionGet.json
     */
    /**
     * Sample code: Get a subscription Deployment stack.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void getASubscriptionDeploymentStack(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacks()
            .getAtSubscriptionWithResponse("simpleDeploymentStack", com.azure.core.util.Context.NONE);
    }
}
```

### DeploymentStacks_GetByResourceGroup

```java
/**
 * Samples for DeploymentStacks GetByResourceGroup.
 */
public final class DeploymentStacksGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-07-01/DeploymentStackResourceGroupGet.json
     */
    /**
     * Sample code: Get a resource group Deployment stack.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void getAResourceGroupDeploymentStack(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacks()
            .getByResourceGroupWithResponse("deploymentStacksRG", "simpleDeploymentStack",
                com.azure.core.util.Context.NONE);
    }
}
```

### DeploymentStacks_List

```java
/**
 * Samples for DeploymentStacks List.
 */
public final class DeploymentStacksListSamples {
    /*
     * x-ms-original-file: 2025-07-01/DeploymentStackSubscriptionList.json
     */
    /**
     * Sample code: List subscription Deployment stacks.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void listSubscriptionDeploymentStacks(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacks().list(com.azure.core.util.Context.NONE);
    }
}
```

### DeploymentStacks_ListAtManagementGroup

```java
/**
 * Samples for DeploymentStacks ListAtManagementGroup.
 */
public final class DeploymentStacksListAtManagementGroupSamples {
    /*
     * x-ms-original-file: 2025-07-01/DeploymentStackManagementGroupList.json
     */
    /**
     * Sample code: List management group Deployment stacks.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void listManagementGroupDeploymentStacks(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacks().listAtManagementGroup("myMg", com.azure.core.util.Context.NONE);
    }
}
```

### DeploymentStacks_ListByResourceGroup

```java
/**
 * Samples for DeploymentStacks ListByResourceGroup.
 */
public final class DeploymentStacksListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-07-01/DeploymentStackResourceGroupList.json
     */
    /**
     * Sample code: List resource group Deployment stacks.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void listResourceGroupDeploymentStacks(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacks().listByResourceGroup("deploymentStacksRG", com.azure.core.util.Context.NONE);
    }
}
```

### DeploymentStacks_ValidateStackAtManagementGroup

```java
import com.azure.resourcemanager.resources.deploymentstacks.fluent.models.DeploymentStackInner;
import com.azure.resourcemanager.resources.deploymentstacks.models.ActionOnUnmanage;
import com.azure.resourcemanager.resources.deploymentstacks.models.DenySettings;
import com.azure.resourcemanager.resources.deploymentstacks.models.DenySettingsMode;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentParameter;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentStackProperties;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentStacksTemplateLink;
import com.azure.resourcemanager.resources.deploymentstacks.models.UnmanageActionManagementGroupMode;
import com.azure.resourcemanager.resources.deploymentstacks.models.UnmanageActionResourceGroupMode;
import com.azure.resourcemanager.resources.deploymentstacks.models.UnmanageActionResourceMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for DeploymentStacks ValidateStackAtManagementGroup.
 */
public final class DeploymentStacksValidateStackAtManagementGroupSamples {
    /*
     * x-ms-original-file: 2025-07-01/DeploymentStackManagementGroupValidate.json
     */
    /**
     * Sample code: Validate a management group Deployment stack.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void validateAManagementGroupDeploymentStack(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacks()
            .validateStackAtManagementGroup("myMg", "simpleDeploymentStack",
                new DeploymentStackInner()
                    .withProperties(new DeploymentStackProperties()
                        .withTemplateLink(
                            new DeploymentStacksTemplateLink().withUri("https://example.com/exampleTemplate.json"))
                        .withParameters(mapOf("parameter1", new DeploymentParameter().withValue("a string")))
                        .withActionOnUnmanage(new ActionOnUnmanage().withResources(UnmanageActionResourceMode.DETACH)
                            .withResourceGroups(UnmanageActionResourceGroupMode.DETACH)
                            .withManagementGroups(UnmanageActionManagementGroupMode.DETACH))
                        .withDenySettings(new DenySettings().withMode(DenySettingsMode.DENY_DELETE)
                            .withExcludedPrincipals(Arrays.asList("principal"))
                            .withExcludedActions(Arrays.asList("action"))
                            .withApplyToChildScopes(false)))
                    .withLocation("eastus")
                    .withTags(mapOf("tagkey", "fakeTokenPlaceholder")),
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

### DeploymentStacks_ValidateStackAtResourceGroup

```java
import com.azure.resourcemanager.resources.deploymentstacks.fluent.models.DeploymentStackInner;
import com.azure.resourcemanager.resources.deploymentstacks.models.ActionOnUnmanage;
import com.azure.resourcemanager.resources.deploymentstacks.models.DenySettings;
import com.azure.resourcemanager.resources.deploymentstacks.models.DenySettingsMode;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentParameter;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentStackProperties;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentStacksTemplateLink;
import com.azure.resourcemanager.resources.deploymentstacks.models.UnmanageActionManagementGroupMode;
import com.azure.resourcemanager.resources.deploymentstacks.models.UnmanageActionResourceGroupMode;
import com.azure.resourcemanager.resources.deploymentstacks.models.UnmanageActionResourceMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for DeploymentStacks ValidateStackAtResourceGroup.
 */
public final class DeploymentStacksValidateStackAtResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-07-01/DeploymentStackResourceGroupValidate.json
     */
    /**
     * Sample code: Validate a resource group Deployment stack.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void validateAResourceGroupDeploymentStack(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacks()
            .validateStackAtResourceGroup("deploymentStacksRG", "simpleDeploymentStack",
                new DeploymentStackInner().withProperties(new DeploymentStackProperties()
                    .withTemplateLink(
                        new DeploymentStacksTemplateLink().withUri("https://example.com/exampleTemplate.json"))
                    .withParameters(mapOf("parameter1", new DeploymentParameter().withValue("a string")))
                    .withActionOnUnmanage(new ActionOnUnmanage().withResources(UnmanageActionResourceMode.DELETE)
                        .withResourceGroups(UnmanageActionResourceGroupMode.DELETE)
                        .withManagementGroups(UnmanageActionManagementGroupMode.DELETE))
                    .withDenySettings(new DenySettings().withMode(DenySettingsMode.DENY_DELETE)
                        .withExcludedPrincipals(Arrays.asList("principal"))
                        .withExcludedActions(Arrays.asList("action"))
                        .withApplyToChildScopes(false)))
                    .withTags(mapOf("tagkey", "fakeTokenPlaceholder")),
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

### DeploymentStacks_ValidateStackAtSubscription

```java
import com.azure.resourcemanager.resources.deploymentstacks.fluent.models.DeploymentStackInner;
import com.azure.resourcemanager.resources.deploymentstacks.models.ActionOnUnmanage;
import com.azure.resourcemanager.resources.deploymentstacks.models.DenySettings;
import com.azure.resourcemanager.resources.deploymentstacks.models.DenySettingsMode;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentParameter;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentStackProperties;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentStacksTemplateLink;
import com.azure.resourcemanager.resources.deploymentstacks.models.UnmanageActionManagementGroupMode;
import com.azure.resourcemanager.resources.deploymentstacks.models.UnmanageActionResourceGroupMode;
import com.azure.resourcemanager.resources.deploymentstacks.models.UnmanageActionResourceMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for DeploymentStacks ValidateStackAtSubscription.
 */
public final class DeploymentStacksValidateStackAtSubscriptionSamples {
    /*
     * x-ms-original-file: 2025-07-01/DeploymentStackSubscriptionValidate.json
     */
    /**
     * Sample code: Validate a subscription Deployment stack.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void validateASubscriptionDeploymentStack(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacks()
            .validateStackAtSubscription("simpleDeploymentStack",
                new DeploymentStackInner()
                    .withProperties(new DeploymentStackProperties()
                        .withTemplateLink(
                            new DeploymentStacksTemplateLink().withUri("https://example.com/exampleTemplate.json"))
                        .withParameters(mapOf("parameter1", new DeploymentParameter().withValue("a string")))
                        .withActionOnUnmanage(new ActionOnUnmanage().withResources(UnmanageActionResourceMode.DELETE)
                            .withResourceGroups(UnmanageActionResourceGroupMode.DELETE)
                            .withManagementGroups(UnmanageActionManagementGroupMode.DELETE))
                        .withDenySettings(new DenySettings().withMode(DenySettingsMode.DENY_DELETE)
                            .withExcludedPrincipals(Arrays.asList("principal"))
                            .withExcludedActions(Arrays.asList("action"))
                            .withApplyToChildScopes(false)))
                    .withLocation("eastus")
                    .withTags(mapOf("tagkey", "fakeTokenPlaceholder")),
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

### DeploymentStacksWhatIfResultsAtManagementGroup_CreateOrUpdate

```java
import com.azure.resourcemanager.resources.deploymentstacks.fluent.models.DeploymentStacksWhatIfResultInner;
import com.azure.resourcemanager.resources.deploymentstacks.models.ActionOnUnmanage;
import com.azure.resourcemanager.resources.deploymentstacks.models.DenySettings;
import com.azure.resourcemanager.resources.deploymentstacks.models.DenySettingsMode;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentExtensionConfig;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentExtensionConfigItem;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentStacksTemplateLink;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentStacksWhatIfResultProperties;
import com.azure.resourcemanager.resources.deploymentstacks.models.UnmanageActionManagementGroupMode;
import com.azure.resourcemanager.resources.deploymentstacks.models.UnmanageActionResourceGroupMode;
import com.azure.resourcemanager.resources.deploymentstacks.models.UnmanageActionResourceMode;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for DeploymentStacksWhatIfResultsAtManagementGroup CreateOrUpdate.
 */
public final class DeploymentStacksWhatIfResultsAtManagementGroupCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-07-01/DeploymentStackWhatIfResultsManagementGroupCreate.json
     */
    /**
     * Sample code: Create or update a management group Deployment stack what-if result.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void createOrUpdateAManagementGroupDeploymentStackWhatIfResult(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacksWhatIfResultsAtManagementGroups()
            .createOrUpdate("myMg", "simpleDeploymentStackWhatIfResult",
                new DeploymentStacksWhatIfResultInner().withProperties(new DeploymentStacksWhatIfResultProperties()
                    .withTemplateLink(
                        new DeploymentStacksTemplateLink().withUri("https://example.com/exampleTemplate.json"))
                    .withParameters(mapOf())
                    .withExtensionConfigs(mapOf("contoso",
                        new DeploymentExtensionConfig().withAdditionalProperties(
                            mapOf("configTwo", new DeploymentExtensionConfigItem().withValue(true), "configOne",
                                new DeploymentExtensionConfigItem().withValue("config1Value")))))
                    .withActionOnUnmanage(new ActionOnUnmanage().withResources(UnmanageActionResourceMode.DELETE)
                        .withResourceGroups(UnmanageActionResourceGroupMode.DELETE)
                        .withManagementGroups(UnmanageActionManagementGroupMode.DETACH))
                    .withDenySettings(new DenySettings().withMode(DenySettingsMode.NONE).withApplyToChildScopes(false))
                    .withDeploymentStackResourceId(
                        "/providers/Microsoft.Management/managementGroups/myMg/providers/Microsoft.Resources/deploymentStacks/simpleDeploymentStack")
                    .withRetentionInterval(Duration.parse("P7D"))).withLocation("eastus"),
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

### DeploymentStacksWhatIfResultsAtManagementGroup_Delete

```java

/**
 * Samples for DeploymentStacksWhatIfResultsAtManagementGroup Delete.
 */
public final class DeploymentStacksWhatIfResultsAtManagementGroupDeleteSamples {
    /*
     * x-ms-original-file: 2025-07-01/DeploymentStackWhatIfResultsManagementGroupDelete.json
     */
    /**
     * Sample code: Delete a management group Deployment stack what-if result.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void deleteAManagementGroupDeploymentStackWhatIfResult(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacksWhatIfResultsAtManagementGroups()
            .deleteWithResponse("myMg", "simpleDeploymentStack", null, null, null, null, null,
                com.azure.core.util.Context.NONE);
    }
}
```

### DeploymentStacksWhatIfResultsAtManagementGroup_Get

```java
/**
 * Samples for DeploymentStacksWhatIfResultsAtManagementGroup Get.
 */
public final class DeploymentStacksWhatIfResultsAtManagementGroupGetSamples {
    /*
     * x-ms-original-file: 2025-07-01/DeploymentStackWhatIfResultsManagementGroupGet.json
     */
    /**
     * Sample code: Get a management group Deployment stack what-if result.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void getAManagementGroupDeploymentStackWhatIfResult(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacksWhatIfResultsAtManagementGroups()
            .getWithResponse("myMg", "simpleDeploymentStackWhatIfResult", com.azure.core.util.Context.NONE);
    }
}
```

### DeploymentStacksWhatIfResultsAtManagementGroup_List

```java
/**
 * Samples for DeploymentStacksWhatIfResultsAtManagementGroup List.
 */
public final class DeploymentStacksWhatIfResultsAtManagementGroupListSamples {
    /*
     * x-ms-original-file: 2025-07-01/DeploymentStackWhatIfResultsManagementGroupList.json
     */
    /**
     * Sample code: List the available Deployment stack what-if results at management group scope.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void listTheAvailableDeploymentStackWhatIfResultsAtManagementGroupScope(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacksWhatIfResultsAtManagementGroups().list("myMg", com.azure.core.util.Context.NONE);
    }
}
```

### DeploymentStacksWhatIfResultsAtManagementGroup_WhatIf

```java
/**
 * Samples for DeploymentStacksWhatIfResultsAtManagementGroup WhatIf.
 */
public final class DeploymentStacksWhatIfResultsAtManagementGroupWhatIfSamples {
    /*
     * x-ms-original-file: 2025-07-01/DeploymentStackWhatIfResultsManagementGroupWhatIf.json
     */
    /**
     * Sample code: Get a detailed management group Deployment stack what-if result.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void getADetailedManagementGroupDeploymentStackWhatIfResult(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacksWhatIfResultsAtManagementGroups()
            .whatIf("myMg", "changedDeploymentStackWhatIfResult", com.azure.core.util.Context.NONE);
    }
}
```

### DeploymentStacksWhatIfResultsAtResourceGroup_CreateOrUpdate

```java
import com.azure.resourcemanager.resources.deploymentstacks.models.ActionOnUnmanage;
import com.azure.resourcemanager.resources.deploymentstacks.models.DenySettings;
import com.azure.resourcemanager.resources.deploymentstacks.models.DenySettingsMode;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentExtensionConfig;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentExtensionConfigItem;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentStacksTemplateLink;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentStacksWhatIfResultProperties;
import com.azure.resourcemanager.resources.deploymentstacks.models.UnmanageActionManagementGroupMode;
import com.azure.resourcemanager.resources.deploymentstacks.models.UnmanageActionResourceGroupMode;
import com.azure.resourcemanager.resources.deploymentstacks.models.UnmanageActionResourceMode;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for DeploymentStacksWhatIfResultsAtResourceGroup CreateOrUpdate.
 */
public final class DeploymentStacksWhatIfResultsAtResourceGroupCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-07-01/DeploymentStackWhatIfResultsResourceGroupCreate.json
     */
    /**
     * Sample code: Create or update a resource group scoped Deployment stack what-if result.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void createOrUpdateAResourceGroupScopedDeploymentStackWhatIfResult(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacksWhatIfResultsAtResourceGroups()
            .define("simpleDeploymentStackWhatIfResult")
            .withExistingResourceGroup("myResourceGroup")
            .withRegion("eastus")
            .withProperties(new DeploymentStacksWhatIfResultProperties()
                .withTemplateLink(
                    new DeploymentStacksTemplateLink().withUri("https://example.com/exampleTemplate.json"))
                .withParameters(mapOf())
                .withExtensionConfigs(mapOf("contoso",
                    new DeploymentExtensionConfig().withAdditionalProperties(
                        mapOf("configTwo", new DeploymentExtensionConfigItem().withValue(true), "configOne",
                            new DeploymentExtensionConfigItem().withValue("config1Value")))))
                .withActionOnUnmanage(new ActionOnUnmanage().withResources(UnmanageActionResourceMode.DELETE)
                    .withResourceGroups(UnmanageActionResourceGroupMode.DELETE)
                    .withManagementGroups(UnmanageActionManagementGroupMode.DETACH))
                .withDenySettings(new DenySettings().withMode(DenySettingsMode.NONE).withApplyToChildScopes(false))
                .withDeploymentStackResourceId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.Resources/deploymentStacks/simpleDeploymentStack")
                .withRetentionInterval(Duration.parse("P7D")))
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

### DeploymentStacksWhatIfResultsAtResourceGroup_Delete

```java

/**
 * Samples for DeploymentStacksWhatIfResultsAtResourceGroup Delete.
 */
public final class DeploymentStacksWhatIfResultsAtResourceGroupDeleteSamples {
    /*
     * x-ms-original-file: 2025-07-01/DeploymentStackWhatIfResultsResourceGroupDelete.json
     */
    /**
     * Sample code: Delete a resource group Deployment stack what-if result.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void deleteAResourceGroupDeploymentStackWhatIfResult(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacksWhatIfResultsAtResourceGroups()
            .deleteWithResponse("myResourceGroup", "simpleDeploymentStack", null, null, null, null, null,
                com.azure.core.util.Context.NONE);
    }
}
```

### DeploymentStacksWhatIfResultsAtResourceGroup_GetByResourceGroup

```java
/**
 * Samples for DeploymentStacksWhatIfResultsAtResourceGroup GetByResourceGroup.
 */
public final class DeploymentStacksWhatIfResultsAtResourceGroupGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-07-01/DeploymentStackWhatIfResultsResourceGroupGet.json
     */
    /**
     * Sample code: Get a resource group Deployment stack what-if result.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void getAResourceGroupDeploymentStackWhatIfResult(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacksWhatIfResultsAtResourceGroups()
            .getByResourceGroupWithResponse("myResourceGroup", "simpleDeploymentStackWhatIfResult",
                com.azure.core.util.Context.NONE);
    }
}
```

### DeploymentStacksWhatIfResultsAtResourceGroup_ListByResourceGroup

```java
/**
 * Samples for DeploymentStacksWhatIfResultsAtResourceGroup ListByResourceGroup.
 */
public final class DeploymentStacksWhatIfResultsAtResourceGroupListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-07-01/DeploymentStackWhatIfResultsResourceGroupList.json
     */
    /**
     * Sample code: List the available Deployment stack what-if results at resource group scope.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void listTheAvailableDeploymentStackWhatIfResultsAtResourceGroupScope(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacksWhatIfResultsAtResourceGroups()
            .listByResourceGroup("myResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### DeploymentStacksWhatIfResultsAtResourceGroup_WhatIf

```java
/**
 * Samples for DeploymentStacksWhatIfResultsAtResourceGroup WhatIf.
 */
public final class DeploymentStacksWhatIfResultsAtResourceGroupWhatIfSamples {
    /*
     * x-ms-original-file: 2025-07-01/DeploymentStackWhatIfResultsResourceGroupWhatIf.json
     */
    /**
     * Sample code: Get a detailed resource group Deployment stack what-if result.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void getADetailedResourceGroupDeploymentStackWhatIfResult(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacksWhatIfResultsAtResourceGroups()
            .whatIf("myResourceGroup", "changedDeploymentStackWhatIfResult", com.azure.core.util.Context.NONE);
    }
}
```

### DeploymentStacksWhatIfResultsAtSubscription_CreateOrUpdate

```java
import com.azure.resourcemanager.resources.deploymentstacks.fluent.models.DeploymentStacksWhatIfResultInner;
import com.azure.resourcemanager.resources.deploymentstacks.models.ActionOnUnmanage;
import com.azure.resourcemanager.resources.deploymentstacks.models.DenySettings;
import com.azure.resourcemanager.resources.deploymentstacks.models.DenySettingsMode;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentExtensionConfig;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentExtensionConfigItem;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentStacksTemplateLink;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentStacksWhatIfResultProperties;
import com.azure.resourcemanager.resources.deploymentstacks.models.UnmanageActionManagementGroupMode;
import com.azure.resourcemanager.resources.deploymentstacks.models.UnmanageActionResourceGroupMode;
import com.azure.resourcemanager.resources.deploymentstacks.models.UnmanageActionResourceMode;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for DeploymentStacksWhatIfResultsAtSubscription CreateOrUpdate.
 */
public final class DeploymentStacksWhatIfResultsAtSubscriptionCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-07-01/DeploymentStackWhatIfResultsSubscriptionCreate.json
     */
    /**
     * Sample code: Create or update a subscription-scoped Deployment stack what-if result.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void createOrUpdateASubscriptionScopedDeploymentStackWhatIfResult(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacksWhatIfResultsAtSubscriptions()
            .createOrUpdate("simpleDeploymentStackWhatIfResult",
                new DeploymentStacksWhatIfResultInner().withProperties(new DeploymentStacksWhatIfResultProperties()
                    .withTemplateLink(
                        new DeploymentStacksTemplateLink().withUri("https://example.com/exampleTemplate.json"))
                    .withParameters(mapOf())
                    .withExtensionConfigs(mapOf("contoso",
                        new DeploymentExtensionConfig().withAdditionalProperties(
                            mapOf("configTwo", new DeploymentExtensionConfigItem().withValue(true), "configOne",
                                new DeploymentExtensionConfigItem().withValue("config1Value")))))
                    .withActionOnUnmanage(new ActionOnUnmanage().withResources(UnmanageActionResourceMode.DELETE)
                        .withResourceGroups(UnmanageActionResourceGroupMode.DELETE)
                        .withManagementGroups(UnmanageActionManagementGroupMode.DETACH))
                    .withDenySettings(new DenySettings().withMode(DenySettingsMode.NONE).withApplyToChildScopes(false))
                    .withDeploymentStackResourceId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/providers/Microsoft.Resources/deploymentStacks/simpleDeploymentStack")
                    .withRetentionInterval(Duration.parse("P7D"))).withLocation("eastus"),
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

### DeploymentStacksWhatIfResultsAtSubscription_Delete

```java

/**
 * Samples for DeploymentStacksWhatIfResultsAtSubscription Delete.
 */
public final class DeploymentStacksWhatIfResultsAtSubscriptionDeleteSamples {
    /*
     * x-ms-original-file: 2025-07-01/DeploymentStackWhatIfResultsSubscriptionDelete.json
     */
    /**
     * Sample code: Delete a subscription Deployment stack what-if result.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void deleteASubscriptionDeploymentStackWhatIfResult(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacksWhatIfResultsAtSubscriptions()
            .deleteWithResponse("simpleDeploymentStack", null, null, null, null, null,
                com.azure.core.util.Context.NONE);
    }
}
```

### DeploymentStacksWhatIfResultsAtSubscription_Get

```java
/**
 * Samples for DeploymentStacksWhatIfResultsAtSubscription Get.
 */
public final class DeploymentStacksWhatIfResultsAtSubscriptionGetSamples {
    /*
     * x-ms-original-file: 2025-07-01/DeploymentStackWhatIfResultsSubscriptionGet.json
     */
    /**
     * Sample code: Get a subscription Deployment stack what-if result.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void getASubscriptionDeploymentStackWhatIfResult(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacksWhatIfResultsAtSubscriptions()
            .getWithResponse("simpleDeploymentStackWhatIfResult", com.azure.core.util.Context.NONE);
    }
}
```

### DeploymentStacksWhatIfResultsAtSubscription_List

```java
/**
 * Samples for DeploymentStacksWhatIfResultsAtSubscription List.
 */
public final class DeploymentStacksWhatIfResultsAtSubscriptionListSamples {
    /*
     * x-ms-original-file: 2025-07-01/DeploymentStackWhatIfResultsSubscriptionList.json
     */
    /**
     * Sample code: List the available Deployment stack what-if results at subscription scope.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void listTheAvailableDeploymentStackWhatIfResultsAtSubscriptionScope(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacksWhatIfResultsAtSubscriptions().list(com.azure.core.util.Context.NONE);
    }
}
```

### DeploymentStacksWhatIfResultsAtSubscription_WhatIf

```java
/**
 * Samples for DeploymentStacksWhatIfResultsAtSubscription WhatIf.
 */
public final class DeploymentStacksWhatIfResultsAtSubscriptionWhatIfSamples {
    /*
     * x-ms-original-file: 2025-07-01/DeploymentStackWhatIfResultsSubscriptionWhatIf.json
     */
    /**
     * Sample code: Get a detailed subscription Deployment stack what-if result.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void getADetailedSubscriptionDeploymentStackWhatIfResult(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacksWhatIfResultsAtSubscriptions()
            .whatIf("changedDeploymentStackWhatIfResult", com.azure.core.util.Context.NONE);
    }
}
```


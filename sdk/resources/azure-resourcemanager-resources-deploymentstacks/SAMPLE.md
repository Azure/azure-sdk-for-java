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
### DeploymentStacks_CreateOrUpdateAtManagementGroup

```java
import com.azure.resourcemanager.resources.deploymentstacks.fluent.models.DeploymentStackInner;
import com.azure.resourcemanager.resources.deploymentstacks.models.ActionOnUnmanage;
import com.azure.resourcemanager.resources.deploymentstacks.models.DenySettings;
import com.azure.resourcemanager.resources.deploymentstacks.models.DenySettingsMode;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentParameter;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentStackProperties;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentStacksDeleteDetachEnum;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for DeploymentStacks CreateOrUpdateAtManagementGroup.
 */
public final class DeploymentStacksCreateOrUpdateAtManagementGroupSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Resources/deploymentStacks/stable/2024-03-01/examples/
     * DeploymentStackManagementGroupCreate.json
     */
    /**
     * Sample code: DeploymentStacksManagementGroupCreateOrUpdate.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void deploymentStacksManagementGroupCreateOrUpdate(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacks()
            .createOrUpdateAtManagementGroup("myMg", "simpleDeploymentStack", new DeploymentStackInner()
                .withLocation("eastus")
                .withTags(mapOf("tagkey", "fakeTokenPlaceholder"))
                .withProperties(new DeploymentStackProperties()
                    .withParameters(mapOf("parameter1", new DeploymentParameter().withValue("a string")))
                    .withActionOnUnmanage(new ActionOnUnmanage().withResources(DeploymentStacksDeleteDetachEnum.DELETE)
                        .withResourceGroups(DeploymentStacksDeleteDetachEnum.DELETE)
                        .withManagementGroups(DeploymentStacksDeleteDetachEnum.DETACH))
                    .withDenySettings(new DenySettings().withMode(DenySettingsMode.DENY_DELETE)
                        .withExcludedPrincipals(Arrays.asList("principal"))
                        .withExcludedActions(Arrays.asList("action"))
                        .withApplyToChildScopes(false))),
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
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentParameter;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentStackProperties;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentStacksDeleteDetachEnum;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for DeploymentStacks CreateOrUpdateAtResourceGroup.
 */
public final class DeploymentStacksCreateOrUpdateAtResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Resources/deploymentStacks/stable/2024-03-01/examples/
     * DeploymentStackResourceGroupCreate.json
     */
    /**
     * Sample code: DeploymentStacksResourceGroupCreateOrUpdate.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void deploymentStacksResourceGroupCreateOrUpdate(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacks()
            .define("simpleDeploymentStack")
            .withExistingResourceGroup("deploymentStacksRG")
            .withRegion("eastus")
            .withTags(mapOf("tagkey", "fakeTokenPlaceholder"))
            .withProperties(new DeploymentStackProperties()
                .withParameters(mapOf("parameter1", new DeploymentParameter().withValue("a string")))
                .withActionOnUnmanage(new ActionOnUnmanage().withResources(DeploymentStacksDeleteDetachEnum.DELETE)
                    .withResourceGroups(DeploymentStacksDeleteDetachEnum.DELETE)
                    .withManagementGroups(DeploymentStacksDeleteDetachEnum.DETACH))
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
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentParameter;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentStackProperties;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentStacksDeleteDetachEnum;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for DeploymentStacks CreateOrUpdateAtSubscription.
 */
public final class DeploymentStacksCreateOrUpdateAtSubscriptionSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Resources/deploymentStacks/stable/2024-03-01/examples/
     * DeploymentStackSubscriptionCreate.json
     */
    /**
     * Sample code: DeploymentStacksSubscriptionCreateOrUpdate.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void deploymentStacksSubscriptionCreateOrUpdate(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacks()
            .createOrUpdateAtSubscription("simpleDeploymentStack", new DeploymentStackInner().withLocation("eastus")
                .withTags(mapOf("tagkey", "fakeTokenPlaceholder"))
                .withProperties(new DeploymentStackProperties()
                    .withParameters(mapOf("parameter1", new DeploymentParameter().withValue("a string")))
                    .withActionOnUnmanage(new ActionOnUnmanage().withResources(DeploymentStacksDeleteDetachEnum.DELETE)
                        .withResourceGroups(DeploymentStacksDeleteDetachEnum.DELETE)
                        .withManagementGroups(DeploymentStacksDeleteDetachEnum.DETACH))
                    .withDenySettings(new DenySettings().withMode(DenySettingsMode.DENY_DELETE)
                        .withExcludedPrincipals(Arrays.asList("principal"))
                        .withExcludedActions(Arrays.asList("action"))
                        .withApplyToChildScopes(false))),
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
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Resources/deploymentStacks/stable/2024-03-01/examples/
     * DeploymentStackResourceGroupDelete.json
     */
    /**
     * Sample code: DeploymentStacksResourceGroupDelete.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void deploymentStacksResourceGroupDelete(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacks()
            .delete("deploymentStacksRG", "simpleDeploymentStack", null, null, null, null,
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
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Resources/deploymentStacks/stable/2024-03-01/examples/
     * DeploymentStackManagementGroupDelete.json
     */
    /**
     * Sample code: DeploymentStacksManagementGroupDelete.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void deploymentStacksManagementGroupDelete(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacks()
            .deleteAtManagementGroup("myMg", "simpleDeploymentStack", null, null, null, null,
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
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Resources/deploymentStacks/stable/2024-03-01/examples/
     * DeploymentStackSubscriptionDelete.json
     */
    /**
     * Sample code: DeploymentStacksSubscriptionDelete.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void deploymentStacksSubscriptionDelete(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacks()
            .deleteAtSubscription("simpleDeploymentStack", null, null, null, null, com.azure.core.util.Context.NONE);
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
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Resources/deploymentStacks/stable/2024-03-01/examples/
     * DeploymentStackManagementGroupExportTemplate.json
     */
    /**
     * Sample code: DeploymentStacksManagementGroupExportTemplate.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void deploymentStacksManagementGroupExportTemplate(
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
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Resources/deploymentStacks/stable/2024-03-01/examples/
     * DeploymentStackExportTemplate.json
     */
    /**
     * Sample code: DeploymentStacksResourceGroupExportTemplate.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void deploymentStacksResourceGroupExportTemplate(
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
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Resources/deploymentStacks/stable/2024-03-01/examples/
     * DeploymentStackSubscriptionExportTemplate.json
     */
    /**
     * Sample code: DeploymentStacksSubscriptionExportTemplate.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void deploymentStacksSubscriptionExportTemplate(
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
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Resources/deploymentStacks/stable/2024-03-01/examples/
     * DeploymentStackManagementGroupGet.json
     */
    /**
     * Sample code: DeploymentStacksManagementGroupGet.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void deploymentStacksManagementGroupGet(
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
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Resources/deploymentStacks/stable/2024-03-01/examples/
     * DeploymentStackSubscriptionGet.json
     */
    /**
     * Sample code: DeploymentStacksSubscriptionGet.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void deploymentStacksSubscriptionGet(
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
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Resources/deploymentStacks/stable/2024-03-01/examples/
     * DeploymentStackResourceGroupGet.json
     */
    /**
     * Sample code: DeploymentStacksResourceGroupGet.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void deploymentStacksResourceGroupGet(
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
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Resources/deploymentStacks/stable/2024-03-01/examples/
     * DeploymentStackSubscriptionList.json
     */
    /**
     * Sample code: DeploymentStacksSubscriptionList.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void deploymentStacksSubscriptionList(
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
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Resources/deploymentStacks/stable/2024-03-01/examples/
     * DeploymentStackManagementGroupList.json
     */
    /**
     * Sample code: DeploymentStacksManagementGroupList.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void deploymentStacksManagementGroupList(
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
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Resources/deploymentStacks/stable/2024-03-01/examples/
     * DeploymentStackResourceGroupList.json
     */
    /**
     * Sample code: DeploymentStacksResourceGroupList.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void deploymentStacksResourceGroupList(
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
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentStacksDeleteDetachEnum;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentStacksTemplateLink;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for DeploymentStacks ValidateStackAtManagementGroup.
 */
public final class DeploymentStacksValidateStackAtManagementGroupSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Resources/deploymentStacks/stable/2024-03-01/examples/
     * DeploymentStackManagementGroupValidate.json
     */
    /**
     * Sample code: DeploymentStacksManagementGroupValidate.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void deploymentStacksManagementGroupValidate(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacks()
            .validateStackAtManagementGroup("myMg", "simpleDeploymentStack", new DeploymentStackInner()
                .withLocation("eastus")
                .withTags(mapOf("tagkey", "fakeTokenPlaceholder"))
                .withProperties(new DeploymentStackProperties()
                    .withTemplateLink(
                        new DeploymentStacksTemplateLink().withUri("https://example.com/exampleTemplate.json"))
                    .withParameters(mapOf("parameter1", new DeploymentParameter().withValue("a string")))
                    .withActionOnUnmanage(new ActionOnUnmanage().withResources(DeploymentStacksDeleteDetachEnum.DETACH)
                        .withResourceGroups(DeploymentStacksDeleteDetachEnum.DETACH)
                        .withManagementGroups(DeploymentStacksDeleteDetachEnum.DETACH))
                    .withDenySettings(new DenySettings().withMode(DenySettingsMode.DENY_DELETE)
                        .withExcludedPrincipals(Arrays.asList("principal"))
                        .withExcludedActions(Arrays.asList("action"))
                        .withApplyToChildScopes(false))),
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
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentStacksDeleteDetachEnum;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentStacksTemplateLink;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for DeploymentStacks ValidateStackAtResourceGroup.
 */
public final class DeploymentStacksValidateStackAtResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Resources/deploymentStacks/stable/2024-03-01/examples/
     * DeploymentStackResourceGroupValidate.json
     */
    /**
     * Sample code: DeploymentStacksResourceGroupValidate.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void deploymentStacksResourceGroupValidate(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacks()
            .validateStackAtResourceGroup("deploymentStacksRG", "simpleDeploymentStack", new DeploymentStackInner()
                .withTags(mapOf("tagkey", "fakeTokenPlaceholder"))
                .withProperties(new DeploymentStackProperties()
                    .withTemplateLink(
                        new DeploymentStacksTemplateLink().withUri("https://example.com/exampleTemplate.json"))
                    .withParameters(mapOf("parameter1", new DeploymentParameter().withValue("a string")))
                    .withActionOnUnmanage(new ActionOnUnmanage().withResources(DeploymentStacksDeleteDetachEnum.DELETE)
                        .withResourceGroups(DeploymentStacksDeleteDetachEnum.DELETE)
                        .withManagementGroups(DeploymentStacksDeleteDetachEnum.DELETE))
                    .withDenySettings(new DenySettings().withMode(DenySettingsMode.DENY_DELETE)
                        .withExcludedPrincipals(Arrays.asList("principal"))
                        .withExcludedActions(Arrays.asList("action"))
                        .withApplyToChildScopes(false))),
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
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentStacksDeleteDetachEnum;
import com.azure.resourcemanager.resources.deploymentstacks.models.DeploymentStacksTemplateLink;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for DeploymentStacks ValidateStackAtSubscription.
 */
public final class DeploymentStacksValidateStackAtSubscriptionSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Resources/deploymentStacks/stable/2024-03-01/examples/
     * DeploymentStackSubscriptionValidate.json
     */
    /**
     * Sample code: DeploymentStacksSubscriptionValidate.
     * 
     * @param manager Entry point to DeploymentStacksManager.
     */
    public static void deploymentStacksSubscriptionValidate(
        com.azure.resourcemanager.resources.deploymentstacks.DeploymentStacksManager manager) {
        manager.deploymentStacks()
            .validateStackAtSubscription("simpleDeploymentStack", new DeploymentStackInner().withLocation("eastus")
                .withTags(mapOf("tagkey", "fakeTokenPlaceholder"))
                .withProperties(new DeploymentStackProperties()
                    .withTemplateLink(
                        new DeploymentStacksTemplateLink().withUri("https://example.com/exampleTemplate.json"))
                    .withParameters(mapOf("parameter1", new DeploymentParameter().withValue("a string")))
                    .withActionOnUnmanage(new ActionOnUnmanage().withResources(DeploymentStacksDeleteDetachEnum.DELETE)
                        .withResourceGroups(DeploymentStacksDeleteDetachEnum.DELETE)
                        .withManagementGroups(DeploymentStacksDeleteDetachEnum.DELETE))
                    .withDenySettings(new DenySettings().withMode(DenySettingsMode.DENY_DELETE)
                        .withExcludedPrincipals(Arrays.asList("principal"))
                        .withExcludedActions(Arrays.asList("action"))
                        .withApplyToChildScopes(false))),
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


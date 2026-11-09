# Code snippets and samples


## TemplateSpecVersions

- [CreateOrUpdate](#templatespecversions_createorupdate)
- [Delete](#templatespecversions_delete)
- [Get](#templatespecversions_get)
- [GetBuiltIn](#templatespecversions_getbuiltin)
- [List](#templatespecversions_list)
- [ListBuiltIns](#templatespecversions_listbuiltins)
- [Update](#templatespecversions_update)

## TemplateSpecs

- [CreateOrUpdate](#templatespecs_createorupdate)
- [Delete](#templatespecs_delete)
- [GetBuiltIn](#templatespecs_getbuiltin)
- [GetByResourceGroup](#templatespecs_getbyresourcegroup)
- [List](#templatespecs_list)
- [ListBuiltIns](#templatespecs_listbuiltins)
- [ListByResourceGroup](#templatespecs_listbyresourcegroup)
- [Update](#templatespecs_update)
### TemplateSpecVersions_CreateOrUpdate

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.resources.templatespecs.models.TemplateSpecVersionProperties;
import java.io.IOException;

/**
 * Samples for TemplateSpecVersions CreateOrUpdate.
 */
public final class TemplateSpecVersionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Resources/templateSpecs/stable/2022-02-01/examples/
     * TemplateSpecVersionsCreate.json
     */
    /**
     * Sample code: TemplateSpecVersionsCreateUpdate.
     * 
     * @param manager Entry point to TemplateSpecsManager.
     */
    public static void templateSpecVersionsCreateUpdate(
        com.azure.resourcemanager.resources.templatespecs.TemplateSpecsManager manager) throws IOException {
        manager.templateSpecVersions()
            .define("v1.0")
            .withRegion("eastus")
            .withExistingTemplateSpec("templateSpecRG", "simpleTemplateSpec")
            .withProperties(new TemplateSpecVersionProperties()
                .withDescription("This is version v1.0 of our template content")
                .withMainTemplate(SerializerFactory.createDefaultManagementSerializerAdapter()
                    .deserialize(
                        "{\"$schema\":\"http://schema.management.azure.com/schemas/2015-01-01/deploymentTemplate.json#\",\"contentVersion\":\"1.0.0.0\",\"parameters\":{},\"resources\":[]}",
                        Object.class, SerializerEncoding.JSON)))
            .create();
    }
}
```

### TemplateSpecVersions_Delete

```java
/**
 * Samples for TemplateSpecVersions Delete.
 */
public final class TemplateSpecVersionsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Resources/templateSpecs/stable/2022-02-01/examples/
     * TemplateSpecVersionsDelete.json
     */
    /**
     * Sample code: TemplateSpecVersionsDelete.
     * 
     * @param manager Entry point to TemplateSpecsManager.
     */
    public static void
        templateSpecVersionsDelete(com.azure.resourcemanager.resources.templatespecs.TemplateSpecsManager manager) {
        manager.templateSpecVersions()
            .deleteWithResponse("templateSpecRG", "simpleTemplateSpec", "v1.0", com.azure.core.util.Context.NONE);
    }
}
```

### TemplateSpecVersions_Get

```java
/**
 * Samples for TemplateSpecVersions Get.
 */
public final class TemplateSpecVersionsGetSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Resources/templateSpecs/stable/2022-02-01/examples/
     * TemplateSpecVersionsGet.json
     */
    /**
     * Sample code: TemplateSpecVersionsGet.
     * 
     * @param manager Entry point to TemplateSpecsManager.
     */
    public static void
        templateSpecVersionsGet(com.azure.resourcemanager.resources.templatespecs.TemplateSpecsManager manager) {
        manager.templateSpecVersions()
            .getWithResponse("templateSpecRG", "simpleTemplateSpec", "v1.0", com.azure.core.util.Context.NONE);
    }
}
```

### TemplateSpecVersions_GetBuiltIn

```java
/**
 * Samples for TemplateSpecVersions GetBuiltIn.
 */
public final class TemplateSpecVersionsGetBuiltInSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Resources/templateSpecs/stable/2022-02-01/examples/
     * BuiltInTemplateSpecVersionsGet.json
     */
    /**
     * Sample code: TemplateSpecVersions_GetBuiltIn.
     * 
     * @param manager Entry point to TemplateSpecsManager.
     */
    public static void
        templateSpecVersionsGetBuiltIn(com.azure.resourcemanager.resources.templatespecs.TemplateSpecsManager manager) {
        manager.templateSpecVersions()
            .getBuiltInWithResponse("nameOfTheBuiltIn", "v1.0", com.azure.core.util.Context.NONE);
    }
}
```

### TemplateSpecVersions_List

```java
/**
 * Samples for TemplateSpecVersions List.
 */
public final class TemplateSpecVersionsListSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Resources/templateSpecs/stable/2022-02-01/examples/
     * TemplateSpecVersionsList.json
     */
    /**
     * Sample code: TemplateSpecVersions_List.
     * 
     * @param manager Entry point to TemplateSpecsManager.
     */
    public static void
        templateSpecVersionsList(com.azure.resourcemanager.resources.templatespecs.TemplateSpecsManager manager) {
        manager.templateSpecVersions().list("templateSpecRG", "simpleTemplateSpec", com.azure.core.util.Context.NONE);
    }
}
```

### TemplateSpecVersions_ListBuiltIns

```java
/**
 * Samples for TemplateSpecVersions ListBuiltIns.
 */
public final class TemplateSpecVersionsListBuiltInsSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Resources/templateSpecs/stable/2022-02-01/examples/
     * BuiltInTemplateSpecVersionsList.json
     */
    /**
     * Sample code: TemplateSpecVersions_ListBuiltIns.
     * 
     * @param manager Entry point to TemplateSpecsManager.
     */
    public static void templateSpecVersionsListBuiltIns(
        com.azure.resourcemanager.resources.templatespecs.TemplateSpecsManager manager) {
        manager.templateSpecVersions().listBuiltIns("nameOfTheBuiltIn", com.azure.core.util.Context.NONE);
    }
}
```

### TemplateSpecVersions_Update

```java
import com.azure.resourcemanager.resources.templatespecs.models.TemplateSpecVersion;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for TemplateSpecVersions Update.
 */
public final class TemplateSpecVersionsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Resources/templateSpecs/stable/2022-02-01/examples/
     * TemplateSpecVersionsPatch.json
     */
    /**
     * Sample code: TemplateSpecsPatch.
     * 
     * @param manager Entry point to TemplateSpecsManager.
     */
    public static void
        templateSpecsPatch(com.azure.resourcemanager.resources.templatespecs.TemplateSpecsManager manager) {
        TemplateSpecVersion resource = manager.templateSpecVersions()
            .getWithResponse("templateSpecRG", "simpleTemplateSpec", "v1.0", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("myTag", "My Value")).apply();
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

### TemplateSpecs_CreateOrUpdate

```java
import com.azure.resourcemanager.resources.templatespecs.models.TemplateSpecProperties;

/**
 * Samples for TemplateSpecs CreateOrUpdate.
 */
public final class TemplateSpecsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Resources/templateSpecs/stable/2022-02-01/examples/
     * TemplateSpecsCreate.json
     */
    /**
     * Sample code: TemplateSpecsCreateUpdate.
     * 
     * @param manager Entry point to TemplateSpecsManager.
     */
    public static void
        templateSpecsCreateUpdate(com.azure.resourcemanager.resources.templatespecs.TemplateSpecsManager manager) {
        manager.templateSpecs()
            .define("simpleTemplateSpec")
            .withRegion("eastus")
            .withExistingResourceGroup("templateSpecRG")
            .withProperties(new TemplateSpecProperties().withDescription("A very simple Template Spec"))
            .create();
    }
}
```

### TemplateSpecs_Delete

```java
/**
 * Samples for TemplateSpecs Delete.
 */
public final class TemplateSpecsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Resources/templateSpecs/stable/2022-02-01/examples/
     * TemplateSpecsDelete.json
     */
    /**
     * Sample code: TemplateSpecsDelete.
     * 
     * @param manager Entry point to TemplateSpecsManager.
     */
    public static void
        templateSpecsDelete(com.azure.resourcemanager.resources.templatespecs.TemplateSpecsManager manager) {
        manager.templateSpecs()
            .deleteByResourceGroupWithResponse("templateSpecRG", "simpleTemplateSpec",
                com.azure.core.util.Context.NONE);
    }
}
```

### TemplateSpecs_GetBuiltIn

```java

/**
 * Samples for TemplateSpecs GetBuiltIn.
 */
public final class TemplateSpecsGetBuiltInSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Resources/templateSpecs/stable/2022-02-01/examples/
     * BuiltInTemplateSpecsGet.json
     */
    /**
     * Sample code: TemplateSpecs_GetBuiltIn.
     * 
     * @param manager Entry point to TemplateSpecsManager.
     */
    public static void
        templateSpecsGetBuiltIn(com.azure.resourcemanager.resources.templatespecs.TemplateSpecsManager manager) {
        manager.templateSpecs().getBuiltInWithResponse("nameOfTheBuiltIn", null, com.azure.core.util.Context.NONE);
    }
}
```

### TemplateSpecs_GetByResourceGroup

```java

/**
 * Samples for TemplateSpecs GetByResourceGroup.
 */
public final class TemplateSpecsGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Resources/templateSpecs/stable/2022-02-01/examples/
     * TemplateSpecsGet.json
     */
    /**
     * Sample code: TemplateSpecsGet.
     * 
     * @param manager Entry point to TemplateSpecsManager.
     */
    public static void
        templateSpecsGet(com.azure.resourcemanager.resources.templatespecs.TemplateSpecsManager manager) {
        manager.templateSpecs()
            .getByResourceGroupWithResponse("templateSpecRG", "simpleTemplateSpec", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### TemplateSpecs_List

```java

/**
 * Samples for TemplateSpecs List.
 */
public final class TemplateSpecsListSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Resources/templateSpecs/stable/2022-02-01/examples/
     * TemplateSpecsListBySubscription.json
     */
    /**
     * Sample code: TemplatesSpecsListBySubscription.
     * 
     * @param manager Entry point to TemplateSpecsManager.
     */
    public static void templatesSpecsListBySubscription(
        com.azure.resourcemanager.resources.templatespecs.TemplateSpecsManager manager) {
        manager.templateSpecs().list(null, com.azure.core.util.Context.NONE);
    }
}
```

### TemplateSpecs_ListBuiltIns

```java

/**
 * Samples for TemplateSpecs ListBuiltIns.
 */
public final class TemplateSpecsListBuiltInsSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Resources/templateSpecs/stable/2022-02-01/examples/
     * BuiltInTemplateSpecsList.json
     */
    /**
     * Sample code: TemplateSpecs_ListBuiltIns.
     * 
     * @param manager Entry point to TemplateSpecsManager.
     */
    public static void
        templateSpecsListBuiltIns(com.azure.resourcemanager.resources.templatespecs.TemplateSpecsManager manager) {
        manager.templateSpecs().listBuiltIns(null, com.azure.core.util.Context.NONE);
    }
}
```

### TemplateSpecs_ListByResourceGroup

```java

/**
 * Samples for TemplateSpecs ListByResourceGroup.
 */
public final class TemplateSpecsListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Resources/templateSpecs/stable/2022-02-01/examples/
     * TemplateSpecsListByResourceGroup.json
     */
    /**
     * Sample code: TemplateSpecsListByResourceGroup.
     * 
     * @param manager Entry point to TemplateSpecsManager.
     */
    public static void templateSpecsListByResourceGroup(
        com.azure.resourcemanager.resources.templatespecs.TemplateSpecsManager manager) {
        manager.templateSpecs().listByResourceGroup("templateSpecRG", null, com.azure.core.util.Context.NONE);
    }
}
```

### TemplateSpecs_Update

```java
import com.azure.resourcemanager.resources.templatespecs.models.TemplateSpec;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for TemplateSpecs Update.
 */
public final class TemplateSpecsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Resources/templateSpecs/stable/2022-02-01/examples/
     * TemplateSpecsPatch.json
     */
    /**
     * Sample code: TemplateSpecsPatch.
     * 
     * @param manager Entry point to TemplateSpecsManager.
     */
    public static void
        templateSpecsPatch(com.azure.resourcemanager.resources.templatespecs.TemplateSpecsManager manager) {
        TemplateSpec resource = manager.templateSpecs()
            .getByResourceGroupWithResponse("templateSpecRG", "simpleTemplateSpec", null,
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("myTag", "My Value")).apply();
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


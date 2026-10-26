# Code snippets and samples


## Deployments

- [CalculateTemplateHash](#deployments_calculatetemplatehash)
- [CreateOrUpdate](#deployments_createorupdate)
- [CreateOrUpdateAtManagementGroupScope](#deployments_createorupdateatmanagementgroupscope)
- [CreateOrUpdateAtScope](#deployments_createorupdateatscope)
- [CreateOrUpdateAtSubscriptionScope](#deployments_createorupdateatsubscriptionscope)
- [CreateOrUpdateAtTenantScope](#deployments_createorupdateattenantscope)
- [Validate](#deployments_validate)
- [ValidateAtManagementGroupScope](#deployments_validateatmanagementgroupscope)
- [ValidateAtScope](#deployments_validateatscope)
- [ValidateAtSubscriptionScope](#deployments_validateatsubscriptionscope)
- [ValidateAtTenantScope](#deployments_validateattenantscope)
- [WhatIf](#deployments_whatif)
- [WhatIfAtManagementGroupScope](#deployments_whatifatmanagementgroupscope)
- [WhatIfAtSubscriptionScope](#deployments_whatifatsubscriptionscope)
- [WhatIfAtTenantScope](#deployments_whatifattenantscope)
### Deployments_CalculateTemplateHash

```java
import com.azure.core.util.BinaryData;
import java.nio.charset.StandardCharsets;

/**
 * Samples for Deployments CalculateTemplateHash.
 */
public final class DeploymentsCalculateTemplateHashSamples {
    /*
     * x-ms-original-file: 2026-06-01/CalculateTemplateHash.json
     */
    /**
     * Sample code: Calculate template hash.
     * 
     * @param manager Entry point to DeploymentsManager.
     */
    public static void
        calculateTemplateHash(com.azure.resourcemanager.resources.deployments.DeploymentsManager manager) {
        manager.deployments()
            .calculateTemplateHashWithResponse(BinaryData.fromBytes(
                "{$schema=http://schemas.management.azure.com/deploymentTemplate?api-version=2014-04-01-preview, contentVersion=1.0.0.0, outputs={string={type=string, value=myvalue}}, parameters={string={type=string}}, resources=[], variables={array=[1, 2, 3, 4], bool=true, int=42, object={object={location=West US, vmSize=Large}}, string=string}}"
                    .getBytes(StandardCharsets.UTF_8)),
                com.azure.core.util.Context.NONE);
    }
}
```

### Deployments_CreateOrUpdate

```java
import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.resources.deployments.models.DeploymentExternalInput;
import com.azure.resourcemanager.resources.deployments.models.DeploymentExternalInputDefinition;
import com.azure.resourcemanager.resources.deployments.models.DeploymentMode;
import com.azure.resourcemanager.resources.deployments.models.DeploymentParameter;
import com.azure.resourcemanager.resources.deployments.models.DeploymentProperties;
import com.azure.resourcemanager.resources.deployments.models.OnErrorDeployment;
import com.azure.resourcemanager.resources.deployments.models.OnErrorDeploymentType;
import com.azure.resourcemanager.resources.deployments.models.TemplateLink;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Deployments CreateOrUpdate.
 */
public final class DeploymentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-06-01/PutDeploymentWithExternalInputs.json
     */
    /**
     * Sample code: Create deployment using external inputs.
     * 
     * @param manager Entry point to DeploymentsManager.
     */
    public static void createDeploymentUsingExternalInputs(
        com.azure.resourcemanager.resources.deployments.DeploymentsManager manager) {
        manager.deployments()
            .define("my-deployment")
            .withExistingResourceGroup("my-resource-group")
            .withProperties(new DeploymentProperties().withTemplate(BinaryData.fromBytes(
                "{$schema=https://schema.management.azure.com/schemas/2019-04-01/deploymentTemplate.json#, contentVersion=1.0.0.0, outputs={inputObj={type=object, value=[parameters('inputObj')]}}, parameters={inputObj={type=object}}, resources=[]}"
                    .getBytes(StandardCharsets.UTF_8)))
                .withParameters(mapOf("inputObj",
                    new DeploymentParameter().withExpression("[createObject('foo', externalInputs('fooValue'))]")))
                .withExternalInputs(mapOf("fooValue",
                    new DeploymentExternalInput()
                        .withValue(BinaryData.fromBytes("baz".getBytes(StandardCharsets.UTF_8)))))
                .withExternalInputDefinitions(mapOf("fooValue",
                    new DeploymentExternalInputDefinition().withKind("sys.envVar")
                        .withConfig(BinaryData.fromBytes("FOO_VALUE".getBytes(StandardCharsets.UTF_8)))))
                .withMode(DeploymentMode.INCREMENTAL))
            .create();
    }

    /*
     * x-ms-original-file: 2026-06-01/PutDeploymentResourceGroup.json
     */
    /**
     * Sample code: Create a deployment that will deploy a template with a uri and queryString.
     * 
     * @param manager Entry point to DeploymentsManager.
     */
    public static void createADeploymentThatWillDeployATemplateWithAUriAndQueryString(
        com.azure.resourcemanager.resources.deployments.DeploymentsManager manager) {
        manager.deployments()
            .define("my-deployment")
            .withExistingResourceGroup("my-resource-group")
            .withProperties(new DeploymentProperties().withTemplateLink(new TemplateLink()
                .withUri("https://example.com/exampleTemplate.json")
                .withQueryString(
                    "sv=2019-02-02&st=2019-04-29T22%3A18%3A26Z&se=2019-04-30T02%3A23%3A26Z&sr=b&sp=rw&sip=168.1.5.60-168.1.5.70&spr=https&sig=xxxxxxxx0xxxxxxxxxxxxx%2bxxxxxxxxxxxxxxxxxxxx%3d"))
                .withParameters(mapOf())
                .withMode(DeploymentMode.INCREMENTAL))
            .create();
    }

    /*
     * x-ms-original-file: 2026-06-01/PutDeploymentResourceGroupTemplateSpecsWithId.json
     */
    /**
     * Sample code: Create a deployment that will deploy a templateSpec with the given resourceId.
     * 
     * @param manager Entry point to DeploymentsManager.
     */
    public static void createADeploymentThatWillDeployATemplateSpecWithTheGivenResourceId(
        com.azure.resourcemanager.resources.deployments.DeploymentsManager manager) {
        manager.deployments()
            .define("my-deployment")
            .withExistingResourceGroup("my-resource-group")
            .withProperties(new DeploymentProperties().withTemplateLink(new TemplateLink().withId(
                "/subscriptions/00000000-0000-0000-0000-000000000001/resourceGroups/my-resource-group/providers/Microsoft.Resources/TemplateSpecs/TemplateSpec-Name/versions/v1"))
                .withParameters(mapOf())
                .withMode(DeploymentMode.INCREMENTAL))
            .create();
    }

    /*
     * x-ms-original-file: 2026-06-01/PutDeploymentWithOnErrorDeploymentLastSuccessful.json
     */
    /**
     * Sample code: Create a deployment that will redeploy the last successful deployment on failure.
     * 
     * @param manager Entry point to DeploymentsManager.
     */
    public static void createADeploymentThatWillRedeployTheLastSuccessfulDeploymentOnFailure(
        com.azure.resourcemanager.resources.deployments.DeploymentsManager manager) {
        manager.deployments()
            .define("my-deployment")
            .withExistingResourceGroup("my-resource-group")
            .withProperties(new DeploymentProperties()
                .withTemplateLink(new TemplateLink().withUri("https://example.com/exampleTemplate.json"))
                .withParameters(mapOf())
                .withMode(DeploymentMode.COMPLETE)
                .withOnErrorDeployment(new OnErrorDeployment().withType(OnErrorDeploymentType.LAST_SUCCESSFUL)))
            .create();
    }

    /*
     * x-ms-original-file: 2026-06-01/PutDeploymentWithOnErrorDeploymentSpecificDeployment.json
     */
    /**
     * Sample code: Create a deployment that will redeploy another deployment on failure.
     * 
     * @param manager Entry point to DeploymentsManager.
     */
    public static void createADeploymentThatWillRedeployAnotherDeploymentOnFailure(
        com.azure.resourcemanager.resources.deployments.DeploymentsManager manager) {
        manager.deployments()
            .define("my-deployment")
            .withExistingResourceGroup("my-resource-group")
            .withProperties(new DeploymentProperties()
                .withTemplateLink(new TemplateLink().withUri("https://example.com/exampleTemplate.json"))
                .withParameters(mapOf())
                .withMode(DeploymentMode.COMPLETE)
                .withOnErrorDeployment(new OnErrorDeployment().withType(OnErrorDeploymentType.SPECIFIC_DEPLOYMENT)
                    .withDeploymentName("name-of-deployment-to-use")))
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

### Deployments_CreateOrUpdateAtManagementGroupScope

```java
import com.azure.resourcemanager.resources.deployments.models.DeploymentMode;
import com.azure.resourcemanager.resources.deployments.models.DeploymentProperties;
import com.azure.resourcemanager.resources.deployments.models.ScopedDeployment;
import com.azure.resourcemanager.resources.deployments.models.TemplateLink;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Deployments CreateOrUpdateAtManagementGroupScope.
 */
public final class DeploymentsCreateOrUpdateAtManagementGroupScopeSamples {
    /*
     * x-ms-original-file: 2026-06-01/PutDeploymentAtManagementGroup.json
     */
    /**
     * Sample code: Create deployment at management group scope.
     * 
     * @param manager Entry point to DeploymentsManager.
     */
    public static void createDeploymentAtManagementGroupScope(
        com.azure.resourcemanager.resources.deployments.DeploymentsManager manager) {
        manager.deployments()
            .createOrUpdateAtManagementGroupScope("my-management-group-id", "my-deployment",
                new ScopedDeployment().withLocation("eastus")
                    .withProperties(new DeploymentProperties()
                        .withTemplateLink(new TemplateLink().withUri("https://example.com/exampleTemplate.json"))
                        .withParameters(mapOf())
                        .withMode(DeploymentMode.INCREMENTAL)),
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

### Deployments_CreateOrUpdateAtScope

```java
import com.azure.resourcemanager.resources.deployments.models.Deployment;
import com.azure.resourcemanager.resources.deployments.models.DeploymentMode;
import com.azure.resourcemanager.resources.deployments.models.DeploymentProperties;
import com.azure.resourcemanager.resources.deployments.models.TemplateLink;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Deployments CreateOrUpdateAtScope.
 */
public final class DeploymentsCreateOrUpdateAtScopeSamples {
    /*
     * x-ms-original-file: 2026-06-01/PutDeploymentAtScope.json
     */
    /**
     * Sample code: Create deployment at a given scope.
     * 
     * @param manager Entry point to DeploymentsManager.
     */
    public static void
        createDeploymentAtAGivenScope(com.azure.resourcemanager.resources.deployments.DeploymentsManager manager) {
        manager.deployments()
            .createOrUpdateAtScope("providers/Microsoft.Management/managementGroups/my-management-group-id",
                "my-deployment",
                new Deployment().withLocation("eastus")
                    .withProperties(new DeploymentProperties()
                        .withTemplateLink(new TemplateLink().withUri("https://example.com/exampleTemplate.json"))
                        .withParameters(mapOf())
                        .withMode(DeploymentMode.INCREMENTAL))
                    .withTags(mapOf("tagKey1", "fakeTokenPlaceholder", "tagKey2", "fakeTokenPlaceholder")),
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

### Deployments_CreateOrUpdateAtSubscriptionScope

```java
import com.azure.resourcemanager.resources.deployments.models.Deployment;
import com.azure.resourcemanager.resources.deployments.models.DeploymentMode;
import com.azure.resourcemanager.resources.deployments.models.DeploymentProperties;
import com.azure.resourcemanager.resources.deployments.models.TemplateLink;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Deployments CreateOrUpdateAtSubscriptionScope.
 */
public final class DeploymentsCreateOrUpdateAtSubscriptionScopeSamples {
    /*
     * x-ms-original-file: 2026-06-01/PutDeploymentSubscriptionTemplateSpecsWithId.json
     */
    /**
     * Sample code: Create a deployment that will deploy a templateSpec with the given resourceId.
     * 
     * @param manager Entry point to DeploymentsManager.
     */
    public static void createADeploymentThatWillDeployATemplateSpecWithTheGivenResourceId(
        com.azure.resourcemanager.resources.deployments.DeploymentsManager manager) {
        manager.deployments()
            .createOrUpdateAtSubscriptionScope("my-deployment", new Deployment().withLocation("eastus")
                .withProperties(new DeploymentProperties().withTemplateLink(new TemplateLink().withId(
                    "/subscriptions/00000000-0000-0000-0000-000000000001/resourceGroups/my-resource-group/providers/Microsoft.Resources/TemplateSpecs/TemplateSpec-Name/versions/v1"))
                    .withParameters(mapOf())
                    .withMode(DeploymentMode.INCREMENTAL)),
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

### Deployments_CreateOrUpdateAtTenantScope

```java
import com.azure.resourcemanager.resources.deployments.models.DeploymentMode;
import com.azure.resourcemanager.resources.deployments.models.DeploymentProperties;
import com.azure.resourcemanager.resources.deployments.models.ScopedDeployment;
import com.azure.resourcemanager.resources.deployments.models.TemplateLink;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Deployments CreateOrUpdateAtTenantScope.
 */
public final class DeploymentsCreateOrUpdateAtTenantScopeSamples {
    /*
     * x-ms-original-file: 2026-06-01/PutDeploymentAtTenant.json
     */
    /**
     * Sample code: Create deployment at tenant scope.
     * 
     * @param manager Entry point to DeploymentsManager.
     */
    public static void
        createDeploymentAtTenantScope(com.azure.resourcemanager.resources.deployments.DeploymentsManager manager) {
        manager.deployments()
            .createOrUpdateAtTenantScope("tenant-dep01",
                new ScopedDeployment().withLocation("eastus")
                    .withProperties(new DeploymentProperties()
                        .withTemplateLink(new TemplateLink().withUri("https://example.com/exampleTemplate.json"))
                        .withParameters(mapOf())
                        .withMode(DeploymentMode.INCREMENTAL))
                    .withTags(mapOf("tagKey1", "fakeTokenPlaceholder", "tagKey2", "fakeTokenPlaceholder")),
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

### Deployments_Validate

```java
import com.azure.resourcemanager.resources.deployments.models.Deployment;
import com.azure.resourcemanager.resources.deployments.models.DeploymentMode;
import com.azure.resourcemanager.resources.deployments.models.DeploymentProperties;
import com.azure.resourcemanager.resources.deployments.models.TemplateLink;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Deployments Validate.
 */
public final class DeploymentsValidateSamples {
    /*
     * x-ms-original-file: 2026-06-01/PostDeploymentValidateOnResourceGroup.json
     */
    /**
     * Sample code: Validates a template at resource group scope.
     * 
     * @param manager Entry point to DeploymentsManager.
     */
    public static void validatesATemplateAtResourceGroupScope(
        com.azure.resourcemanager.resources.deployments.DeploymentsManager manager) {
        manager.deployments()
            .validate("my-resource-group", "my-deployment", new Deployment().withProperties(new DeploymentProperties()
                .withTemplateLink(new TemplateLink().withUri("https://example.com/exampleTemplate.json")
                    .withQueryString(
                        "sv=2019-02-02&st=2019-04-29T22%3A18%3A26Z&se=2019-04-30T02%3A23%3A26Z&sr=b&sp=rw&sip=168.1.5.60-168.1.5.70&spr=https&sig=xxxxxxxx0xxxxxxxxxxxxx%2bxxxxxxxxxxxxxxxxxxxx%3d"))
                .withParameters(mapOf())
                .withMode(DeploymentMode.INCREMENTAL)), com.azure.core.util.Context.NONE);
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

### Deployments_ValidateAtManagementGroupScope

```java
import com.azure.resourcemanager.resources.deployments.models.DeploymentMode;
import com.azure.resourcemanager.resources.deployments.models.DeploymentProperties;
import com.azure.resourcemanager.resources.deployments.models.ScopedDeployment;
import com.azure.resourcemanager.resources.deployments.models.TemplateLink;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Deployments ValidateAtManagementGroupScope.
 */
public final class DeploymentsValidateAtManagementGroupScopeSamples {
    /*
     * x-ms-original-file: 2026-06-01/PostDeploymentValidateOnManagementGroup.json
     */
    /**
     * Sample code: Validates a template at management group scope.
     * 
     * @param manager Entry point to DeploymentsManager.
     */
    public static void validatesATemplateAtManagementGroupScope(
        com.azure.resourcemanager.resources.deployments.DeploymentsManager manager) {
        manager.deployments()
            .validateAtManagementGroupScope("my-management-group-id", "my-deployment",
                new ScopedDeployment().withLocation("eastus")
                    .withProperties(new DeploymentProperties()
                        .withTemplateLink(new TemplateLink().withUri("https://example.com/exampleTemplate.json"))
                        .withParameters(mapOf())
                        .withMode(DeploymentMode.INCREMENTAL)),
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

### Deployments_ValidateAtScope

```java
import com.azure.resourcemanager.resources.deployments.models.Deployment;
import com.azure.resourcemanager.resources.deployments.models.DeploymentMode;
import com.azure.resourcemanager.resources.deployments.models.DeploymentProperties;
import com.azure.resourcemanager.resources.deployments.models.TemplateLink;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Deployments ValidateAtScope.
 */
public final class DeploymentsValidateAtScopeSamples {
    /*
     * x-ms-original-file: 2026-06-01/PostDeploymentValidateOnScope.json
     */
    /**
     * Sample code: Validates a template at scope.
     * 
     * @param manager Entry point to DeploymentsManager.
     */
    public static void
        validatesATemplateAtScope(com.azure.resourcemanager.resources.deployments.DeploymentsManager manager) {
        manager.deployments()
            .validateAtScope("subscriptions/00000000-0000-0000-0000-000000000001/resourceGroups/my-resource-group",
                "my-deployment",
                new Deployment().withProperties(new DeploymentProperties().withTemplateLink(new TemplateLink()
                    .withUri("https://example.com/exampleTemplate.json")
                    .withQueryString(
                        "sv=2019-02-02&st=2019-04-29T22%3A18%3A26Z&se=2019-04-30T02%3A23%3A26Z&sr=b&sp=rw&sip=168.1.5.60-168.1.5.70&spr=https&sig=xxxxxxxx0xxxxxxxxxxxxx%2bxxxxxxxxxxxxxxxxxxxx%3d"))
                    .withParameters(mapOf())
                    .withMode(DeploymentMode.INCREMENTAL)),
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

### Deployments_ValidateAtSubscriptionScope

```java
import com.azure.resourcemanager.resources.deployments.models.Deployment;
import com.azure.resourcemanager.resources.deployments.models.DeploymentMode;
import com.azure.resourcemanager.resources.deployments.models.DeploymentProperties;
import com.azure.resourcemanager.resources.deployments.models.TemplateLink;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Deployments ValidateAtSubscriptionScope.
 */
public final class DeploymentsValidateAtSubscriptionScopeSamples {
    /*
     * x-ms-original-file: 2026-06-01/PostDeploymentValidateOnSubscription.json
     */
    /**
     * Sample code: Validates a template at subscription scope.
     * 
     * @param manager Entry point to DeploymentsManager.
     */
    public static void validatesATemplateAtSubscriptionScope(
        com.azure.resourcemanager.resources.deployments.DeploymentsManager manager) {
        manager.deployments()
            .validateAtSubscriptionScope("my-deployment",
                new Deployment().withLocation("eastus")
                    .withProperties(new DeploymentProperties()
                        .withTemplateLink(new TemplateLink().withUri("https://example.com/exampleTemplate.json"))
                        .withParameters(mapOf())
                        .withMode(DeploymentMode.INCREMENTAL)),
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

### Deployments_ValidateAtTenantScope

```java
import com.azure.resourcemanager.resources.deployments.models.DeploymentMode;
import com.azure.resourcemanager.resources.deployments.models.DeploymentProperties;
import com.azure.resourcemanager.resources.deployments.models.ScopedDeployment;
import com.azure.resourcemanager.resources.deployments.models.TemplateLink;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Deployments ValidateAtTenantScope.
 */
public final class DeploymentsValidateAtTenantScopeSamples {
    /*
     * x-ms-original-file: 2026-06-01/PostDeploymentValidateOnTenant.json
     */
    /**
     * Sample code: Validates a template at tenant scope.
     * 
     * @param manager Entry point to DeploymentsManager.
     */
    public static void
        validatesATemplateAtTenantScope(com.azure.resourcemanager.resources.deployments.DeploymentsManager manager) {
        manager.deployments()
            .validateAtTenantScope("my-deployment",
                new ScopedDeployment().withLocation("eastus")
                    .withProperties(new DeploymentProperties()
                        .withTemplateLink(new TemplateLink().withUri("https://example.com/exampleTemplate.json"))
                        .withParameters(mapOf())
                        .withMode(DeploymentMode.INCREMENTAL)),
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

### Deployments_WhatIf

```java
import com.azure.resourcemanager.resources.deployments.models.DeploymentMode;
import com.azure.resourcemanager.resources.deployments.models.DeploymentWhatIf;
import com.azure.resourcemanager.resources.deployments.models.DeploymentWhatIfProperties;
import com.azure.resourcemanager.resources.deployments.models.TemplateLink;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Deployments WhatIf.
 */
public final class DeploymentsWhatIfSamples {
    /*
     * x-ms-original-file: 2026-06-01/PostDeploymentWhatIfOnResourceGroup.json
     */
    /**
     * Sample code: Predict template changes at resource group scope.
     * 
     * @param manager Entry point to DeploymentsManager.
     */
    public static void predictTemplateChangesAtResourceGroupScope(
        com.azure.resourcemanager.resources.deployments.DeploymentsManager manager) {
        manager.deployments()
            .whatIf("my-resource-group", "my-deployment",
                new DeploymentWhatIf().withProperties(new DeploymentWhatIfProperties()
                    .withTemplateLink(new TemplateLink().withUri("https://example.com/exampleTemplate.json"))
                    .withParameters(mapOf())
                    .withMode(DeploymentMode.INCREMENTAL)),
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

### Deployments_WhatIfAtManagementGroupScope

```java
import com.azure.resourcemanager.resources.deployments.models.DeploymentMode;
import com.azure.resourcemanager.resources.deployments.models.DeploymentWhatIfProperties;
import com.azure.resourcemanager.resources.deployments.models.ScopedDeploymentWhatIf;
import com.azure.resourcemanager.resources.deployments.models.TemplateLink;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Deployments WhatIfAtManagementGroupScope.
 */
public final class DeploymentsWhatIfAtManagementGroupScopeSamples {
    /*
     * x-ms-original-file: 2026-06-01/PostDeploymentWhatIfOnManagementGroup.json
     */
    /**
     * Sample code: Predict template changes at management group scope.
     * 
     * @param manager Entry point to DeploymentsManager.
     */
    public static void predictTemplateChangesAtManagementGroupScope(
        com.azure.resourcemanager.resources.deployments.DeploymentsManager manager) {
        manager.deployments()
            .whatIfAtManagementGroupScope("myManagementGruop", "exampleDeploymentName",
                new ScopedDeploymentWhatIf().withLocation("eastus")
                    .withProperties(new DeploymentWhatIfProperties()
                        .withTemplateLink(new TemplateLink().withUri("https://example.com/exampleTemplate.json"))
                        .withParameters(mapOf())
                        .withMode(DeploymentMode.INCREMENTAL)),
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

### Deployments_WhatIfAtSubscriptionScope

```java
import com.azure.resourcemanager.resources.deployments.models.DeploymentMode;
import com.azure.resourcemanager.resources.deployments.models.DeploymentWhatIf;
import com.azure.resourcemanager.resources.deployments.models.DeploymentWhatIfProperties;
import com.azure.resourcemanager.resources.deployments.models.TemplateLink;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Deployments WhatIfAtSubscriptionScope.
 */
public final class DeploymentsWhatIfAtSubscriptionScopeSamples {
    /*
     * x-ms-original-file: 2026-06-01/PostDeploymentWhatIfOnSubscription.json
     */
    /**
     * Sample code: Predict template changes at subscription scope.
     * 
     * @param manager Entry point to DeploymentsManager.
     */
    public static void predictTemplateChangesAtSubscriptionScope(
        com.azure.resourcemanager.resources.deployments.DeploymentsManager manager) {
        manager.deployments()
            .whatIfAtSubscriptionScope("my-deployment",
                new DeploymentWhatIf().withLocation("westus")
                    .withProperties(new DeploymentWhatIfProperties()
                        .withTemplateLink(new TemplateLink().withUri("https://example.com/exampleTemplate.json"))
                        .withParameters(mapOf())
                        .withMode(DeploymentMode.INCREMENTAL)),
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

### Deployments_WhatIfAtTenantScope

```java
import com.azure.resourcemanager.resources.deployments.models.DeploymentMode;
import com.azure.resourcemanager.resources.deployments.models.DeploymentWhatIfProperties;
import com.azure.resourcemanager.resources.deployments.models.ScopedDeploymentWhatIf;
import com.azure.resourcemanager.resources.deployments.models.TemplateLink;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Deployments WhatIfAtTenantScope.
 */
public final class DeploymentsWhatIfAtTenantScopeSamples {
    /*
     * x-ms-original-file: 2026-06-01/PostDeploymentWhatIfOnTenant.json
     */
    /**
     * Sample code: Predict template changes at management group scope.
     * 
     * @param manager Entry point to DeploymentsManager.
     */
    public static void predictTemplateChangesAtManagementGroupScope(
        com.azure.resourcemanager.resources.deployments.DeploymentsManager manager) {
        manager.deployments()
            .whatIfAtTenantScope("exampleDeploymentName",
                new ScopedDeploymentWhatIf().withLocation("eastus")
                    .withProperties(new DeploymentWhatIfProperties()
                        .withTemplateLink(new TemplateLink().withUri("https://example.com/exampleTemplate.json"))
                        .withParameters(mapOf())
                        .withMode(DeploymentMode.INCREMENTAL)),
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


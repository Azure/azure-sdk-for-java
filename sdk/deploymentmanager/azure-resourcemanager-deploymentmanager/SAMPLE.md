# Code snippets and samples


## ArtifactSources

- [CreateOrUpdate](#artifactsources_createorupdate)
- [Delete](#artifactsources_delete)
- [GetByResourceGroup](#artifactsources_getbyresourcegroup)
- [List](#artifactsources_list)

## Operations

- [List](#operations_list)

## Rollouts

- [Cancel](#rollouts_cancel)
- [CreateOrUpdate](#rollouts_createorupdate)
- [Delete](#rollouts_delete)
- [GetByResourceGroup](#rollouts_getbyresourcegroup)
- [List](#rollouts_list)
- [Restart](#rollouts_restart)

## ServiceTopologies

- [CreateOrUpdate](#servicetopologies_createorupdate)
- [Delete](#servicetopologies_delete)
- [GetByResourceGroup](#servicetopologies_getbyresourcegroup)
- [List](#servicetopologies_list)

## ServiceUnits

- [CreateOrUpdate](#serviceunits_createorupdate)
- [Delete](#serviceunits_delete)
- [Get](#serviceunits_get)
- [List](#serviceunits_list)

## Services

- [CreateOrUpdate](#services_createorupdate)
- [Delete](#services_delete)
- [Get](#services_get)
- [List](#services_list)

## Steps

- [CreateOrUpdate](#steps_createorupdate)
- [Delete](#steps_delete)
- [GetByResourceGroup](#steps_getbyresourcegroup)
- [List](#steps_list)
### ArtifactSources_CreateOrUpdate

```java
import java.util.HashMap;
import java.util.Map;

/** Samples for ArtifactSources CreateOrUpdate. */
public final class ArtifactSourcesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/deploymentmanager/resource-manager/Microsoft.DeploymentManager/preview/2019-11-01-preview/examples/artifactsource_createorupdate.json
     */
    /**
     * Sample code: Create artifact source.
     *
     * @param manager Entry point to DeploymentManager.
     */
    public static void createArtifactSource(com.azure.resourcemanager.deploymentmanager.DeploymentManager manager) {
        manager
            .artifactSources()
            .define("myArtifactSource")
            .withRegion("centralus")
            .withExistingResourceGroup("myResourceGroup")
            .withTags(mapOf())
            .create();
    }

    /*
     * x-ms-original-file: specification/deploymentmanager/resource-manager/Microsoft.DeploymentManager/preview/2019-11-01-preview/examples/artifactsource_createorupdate_artifactroot.json
     */
    /**
     * Sample code: Create artifact source with artifact root, an offset into the storage container.
     *
     * @param manager Entry point to DeploymentManager.
     */
    public static void createArtifactSourceWithArtifactRootAnOffsetIntoTheStorageContainer(
        com.azure.resourcemanager.deploymentmanager.DeploymentManager manager) {
        manager
            .artifactSources()
            .define("myArtifactSource")
            .withRegion("centralus")
            .withExistingResourceGroup("myResourceGroup")
            .withTags(mapOf())
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

### ArtifactSources_Delete

```java
import com.azure.core.util.Context;

/** Samples for ArtifactSources Delete. */
public final class ArtifactSourcesDeleteSamples {
    /*
     * x-ms-original-file: specification/deploymentmanager/resource-manager/Microsoft.DeploymentManager/preview/2019-11-01-preview/examples/artifactsource_delete.json
     */
    /**
     * Sample code: Delete artifact source.
     *
     * @param manager Entry point to DeploymentManager.
     */
    public static void deleteArtifactSource(com.azure.resourcemanager.deploymentmanager.DeploymentManager manager) {
        manager.artifactSources().deleteWithResponse("myResourceGroup", "myArtifactSource", Context.NONE);
    }
}
```

### ArtifactSources_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for ArtifactSources GetByResourceGroup. */
public final class ArtifactSourcesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/deploymentmanager/resource-manager/Microsoft.DeploymentManager/preview/2019-11-01-preview/examples/artifactsource_get.json
     */
    /**
     * Sample code: Get artifact source.
     *
     * @param manager Entry point to DeploymentManager.
     */
    public static void getArtifactSource(com.azure.resourcemanager.deploymentmanager.DeploymentManager manager) {
        manager.artifactSources().getByResourceGroupWithResponse("myResourceGroup", "myArtifactSource", Context.NONE);
    }
}
```

### ArtifactSources_List

```java
import com.azure.core.util.Context;

/** Samples for ArtifactSources List. */
public final class ArtifactSourcesListSamples {
    /*
     * x-ms-original-file: specification/deploymentmanager/resource-manager/Microsoft.DeploymentManager/preview/2019-11-01-preview/examples/artifactsources_list.json
     */
    /**
     * Sample code: List steps.
     *
     * @param manager Entry point to DeploymentManager.
     */
    public static void listSteps(com.azure.resourcemanager.deploymentmanager.DeploymentManager manager) {
        manager.artifactSources().listWithResponse("myResourceGroup", Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/deploymentmanager/resource-manager/Microsoft.DeploymentManager/preview/2019-11-01-preview/examples/operations_list.json
     */
    /**
     * Sample code: Get operations.
     *
     * @param manager Entry point to DeploymentManager.
     */
    public static void getOperations(com.azure.resourcemanager.deploymentmanager.DeploymentManager manager) {
        manager.operations().listWithResponse(Context.NONE);
    }
}
```

### Rollouts_Cancel

```java
import com.azure.core.util.Context;

/** Samples for Rollouts Cancel. */
public final class RolloutsCancelSamples {
    /*
     * x-ms-original-file: specification/deploymentmanager/resource-manager/Microsoft.DeploymentManager/preview/2019-11-01-preview/examples/rollout_post_cancel.json
     */
    /**
     * Sample code: Cancel rollout.
     *
     * @param manager Entry point to DeploymentManager.
     */
    public static void cancelRollout(com.azure.resourcemanager.deploymentmanager.DeploymentManager manager) {
        manager.rollouts().cancelWithResponse("myResourceGroup", "myRollout", Context.NONE);
    }
}
```

### Rollouts_CreateOrUpdate

```java
import com.azure.resourcemanager.deploymentmanager.models.Identity;
import com.azure.resourcemanager.deploymentmanager.models.PrePostStep;
import com.azure.resourcemanager.deploymentmanager.models.StepGroup;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Rollouts CreateOrUpdate. */
public final class RolloutsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/deploymentmanager/resource-manager/Microsoft.DeploymentManager/preview/2019-11-01-preview/examples/rollout_createorupdate.json
     */
    /**
     * Sample code: Create or update rollout.
     *
     * @param manager Entry point to DeploymentManager.
     */
    public static void createOrUpdateRollout(com.azure.resourcemanager.deploymentmanager.DeploymentManager manager) {
        manager
            .rollouts()
            .define("myRollout")
            .withRegion("centralus")
            .withExistingResourceGroup("myResourceGroup")
            .withIdentity(
                new Identity()
                    .withType("userAssigned")
                    .withIdentityIds(
                        Arrays
                            .asList(
                                "/subscriptions/caac1590-e859-444f-a9e0-62091c0f5929/resourceGroups/myResourceGroup/providers/Microsoft.ManagedIdentity/userassignedidentities/myuseridentity")))
            .withBuildVersion("1.0.0.1")
            .withTargetServiceTopologyId(
                "/subscriptions/caac1590-e859-444f-a9e0-62091c0f5929/resourceGroups/myResourceGroup/Microsoft.DeploymentManager/serviceTopologies/myTopology")
            .withStepGroups(
                Arrays
                    .asList(
                        new StepGroup()
                            .withName("FirstRegion")
                            .withPreDeploymentSteps(
                                Arrays
                                    .asList(
                                        new PrePostStep()
                                            .withStepId("Microsoft.DeploymentManager/steps/preDeployStep1"),
                                        new PrePostStep()
                                            .withStepId("Microsoft.DeploymentManager/steps/preDeployStep2")))
                            .withDeploymentTargetId(
                                "Microsoft.DeploymentManager/serviceTopologies/myTopology/services/myService/serviceUnits/myServiceUnit1'")
                            .withPostDeploymentSteps(
                                Arrays
                                    .asList(
                                        new PrePostStep()
                                            .withStepId("Microsoft.DeploymentManager/steps/postDeployStep1"))),
                        new StepGroup()
                            .withName("SecondRegion")
                            .withDependsOnStepGroups(Arrays.asList("FirstRegion"))
                            .withPreDeploymentSteps(
                                Arrays
                                    .asList(
                                        new PrePostStep()
                                            .withStepId("Microsoft.DeploymentManager/steps/preDeployStep3"),
                                        new PrePostStep()
                                            .withStepId("Microsoft.DeploymentManager/steps/preDeployStep4")))
                            .withDeploymentTargetId(
                                "Microsoft.DeploymentManager/serviceTopologies/myTopology/services/myService/serviceUnits/myServiceUnit2'")
                            .withPostDeploymentSteps(
                                Arrays
                                    .asList(
                                        new PrePostStep()
                                            .withStepId("Microsoft.DeploymentManager/steps/postDeployStep5")))))
            .withTags(mapOf())
            .withArtifactSourceId(
                "/subscriptions/caac1590-e859-444f-a9e0-62091c0f5929/resourceGroups/myResourceGroup/Microsoft.DeploymentManager/artifactSources/myArtifactSource")
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

### Rollouts_Delete

```java
import com.azure.core.util.Context;

/** Samples for Rollouts Delete. */
public final class RolloutsDeleteSamples {
    /*
     * x-ms-original-file: specification/deploymentmanager/resource-manager/Microsoft.DeploymentManager/preview/2019-11-01-preview/examples/rollout_delete.json
     */
    /**
     * Sample code: Delete rollout.
     *
     * @param manager Entry point to DeploymentManager.
     */
    public static void deleteRollout(com.azure.resourcemanager.deploymentmanager.DeploymentManager manager) {
        manager.rollouts().deleteWithResponse("myResourceGroup", "myRollout", Context.NONE);
    }
}
```

### Rollouts_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Rollouts GetByResourceGroup. */
public final class RolloutsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/deploymentmanager/resource-manager/Microsoft.DeploymentManager/preview/2019-11-01-preview/examples/rollout_get.json
     */
    /**
     * Sample code: Get rollout.
     *
     * @param manager Entry point to DeploymentManager.
     */
    public static void getRollout(com.azure.resourcemanager.deploymentmanager.DeploymentManager manager) {
        manager.rollouts().getByResourceGroupWithResponse("myResourceGroup", "myRollout", null, Context.NONE);
    }
}
```

### Rollouts_List

```java
import com.azure.core.util.Context;

/** Samples for Rollouts List. */
public final class RolloutsListSamples {
    /*
     * x-ms-original-file: specification/deploymentmanager/resource-manager/Microsoft.DeploymentManager/preview/2019-11-01-preview/examples/rollouts_list.json
     */
    /**
     * Sample code: List rollouts by resource group.
     *
     * @param manager Entry point to DeploymentManager.
     */
    public static void listRolloutsByResourceGroup(
        com.azure.resourcemanager.deploymentmanager.DeploymentManager manager) {
        manager.rollouts().listWithResponse("myResourceGroup", Context.NONE);
    }
}
```

### Rollouts_Restart

```java
import com.azure.core.util.Context;

/** Samples for Rollouts Restart. */
public final class RolloutsRestartSamples {
    /*
     * x-ms-original-file: specification/deploymentmanager/resource-manager/Microsoft.DeploymentManager/preview/2019-11-01-preview/examples/rollout_post_restart.json
     */
    /**
     * Sample code: Restart rollout.
     *
     * @param manager Entry point to DeploymentManager.
     */
    public static void restartRollout(com.azure.resourcemanager.deploymentmanager.DeploymentManager manager) {
        manager.rollouts().restartWithResponse("myResourceGroup", "myRollout", true, Context.NONE);
    }
}
```

### ServiceTopologies_CreateOrUpdate

```java
import java.util.HashMap;
import java.util.Map;

/** Samples for ServiceTopologies CreateOrUpdate. */
public final class ServiceTopologiesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/deploymentmanager/resource-manager/Microsoft.DeploymentManager/preview/2019-11-01-preview/examples/servicetopology_createorupdate.json
     */
    /**
     * Sample code: Create a topology with Artifact Source.
     *
     * @param manager Entry point to DeploymentManager.
     */
    public static void createATopologyWithArtifactSource(
        com.azure.resourcemanager.deploymentmanager.DeploymentManager manager) {
        manager
            .serviceTopologies()
            .define("myTopology")
            .withRegion("centralus")
            .withExistingResourceGroup("myResourceGroup")
            .withTags(mapOf())
            .create();
    }

    /*
     * x-ms-original-file: specification/deploymentmanager/resource-manager/Microsoft.DeploymentManager/preview/2019-11-01-preview/examples/servicetopology_createorupdate_noartifactsource.json
     */
    /**
     * Sample code: Create a topology without Artifact Source.
     *
     * @param manager Entry point to DeploymentManager.
     */
    public static void createATopologyWithoutArtifactSource(
        com.azure.resourcemanager.deploymentmanager.DeploymentManager manager) {
        manager
            .serviceTopologies()
            .define("myTopology")
            .withRegion("centralus")
            .withExistingResourceGroup("myResourceGroup")
            .withTags(mapOf())
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

### ServiceTopologies_Delete

```java
import com.azure.core.util.Context;

/** Samples for ServiceTopologies Delete. */
public final class ServiceTopologiesDeleteSamples {
    /*
     * x-ms-original-file: specification/deploymentmanager/resource-manager/Microsoft.DeploymentManager/preview/2019-11-01-preview/examples/servicetopology_delete.json
     */
    /**
     * Sample code: Delete topology.
     *
     * @param manager Entry point to DeploymentManager.
     */
    public static void deleteTopology(com.azure.resourcemanager.deploymentmanager.DeploymentManager manager) {
        manager.serviceTopologies().deleteWithResponse("myResourceGroup", "myTopology", Context.NONE);
    }
}
```

### ServiceTopologies_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for ServiceTopologies GetByResourceGroup. */
public final class ServiceTopologiesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/deploymentmanager/resource-manager/Microsoft.DeploymentManager/preview/2019-11-01-preview/examples/servicetopology_get.json
     */
    /**
     * Sample code: Get topology.
     *
     * @param manager Entry point to DeploymentManager.
     */
    public static void getTopology(com.azure.resourcemanager.deploymentmanager.DeploymentManager manager) {
        manager.serviceTopologies().getByResourceGroupWithResponse("myResourceGroup", "myTopology", Context.NONE);
    }
}
```

### ServiceTopologies_List

```java
import com.azure.core.util.Context;

/** Samples for ServiceTopologies List. */
public final class ServiceTopologiesListSamples {
    /*
     * x-ms-original-file: specification/deploymentmanager/resource-manager/Microsoft.DeploymentManager/preview/2019-11-01-preview/examples/servicetopologies_list.json
     */
    /**
     * Sample code: List topologies.
     *
     * @param manager Entry point to DeploymentManager.
     */
    public static void listTopologies(com.azure.resourcemanager.deploymentmanager.DeploymentManager manager) {
        manager.serviceTopologies().listWithResponse("myResourceGroup", Context.NONE);
    }
}
```

### ServiceUnits_CreateOrUpdate

```java
import java.util.HashMap;
import java.util.Map;

/** Samples for ServiceUnits CreateOrUpdate. */
public final class ServiceUnitsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/deploymentmanager/resource-manager/Microsoft.DeploymentManager/preview/2019-11-01-preview/examples/serviceunit_createorupdate.json
     */
    /**
     * Sample code: Create service unit using relative paths into the artifact source.
     *
     * @param manager Entry point to DeploymentManager.
     */
    public static void createServiceUnitUsingRelativePathsIntoTheArtifactSource(
        com.azure.resourcemanager.deploymentmanager.DeploymentManager manager) {
        manager
            .serviceUnits()
            .define("myServiceUnit")
            .withRegion("centralus")
            .withExistingService("myResourceGroup", "myTopology", "myService")
            .withTags(mapOf())
            .create();
    }

    /*
     * x-ms-original-file: specification/deploymentmanager/resource-manager/Microsoft.DeploymentManager/preview/2019-11-01-preview/examples/serviceunit_createorupdate_noartifactsource.json
     */
    /**
     * Sample code: Create service unit using SAS URIs.
     *
     * @param manager Entry point to DeploymentManager.
     */
    public static void createServiceUnitUsingSASURIs(
        com.azure.resourcemanager.deploymentmanager.DeploymentManager manager) {
        manager
            .serviceUnits()
            .define("myServiceUnit")
            .withRegion("centralus")
            .withExistingService("myResourceGroup", "myTopology", "myService")
            .withTags(mapOf())
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

### ServiceUnits_Delete

```java
import com.azure.core.util.Context;

/** Samples for ServiceUnits Delete. */
public final class ServiceUnitsDeleteSamples {
    /*
     * x-ms-original-file: specification/deploymentmanager/resource-manager/Microsoft.DeploymentManager/preview/2019-11-01-preview/examples/serviceunit_delete.json
     */
    /**
     * Sample code: Delete service unit.
     *
     * @param manager Entry point to DeploymentManager.
     */
    public static void deleteServiceUnit(com.azure.resourcemanager.deploymentmanager.DeploymentManager manager) {
        manager
            .serviceUnits()
            .deleteWithResponse("myResourceGroup", "myTopology", "myService", "myServiceUnit", Context.NONE);
    }
}
```

### ServiceUnits_Get

```java
import com.azure.core.util.Context;

/** Samples for ServiceUnits Get. */
public final class ServiceUnitsGetSamples {
    /*
     * x-ms-original-file: specification/deploymentmanager/resource-manager/Microsoft.DeploymentManager/preview/2019-11-01-preview/examples/serviceunit_get.json
     */
    /**
     * Sample code: Get service unit.
     *
     * @param manager Entry point to DeploymentManager.
     */
    public static void getServiceUnit(com.azure.resourcemanager.deploymentmanager.DeploymentManager manager) {
        manager
            .serviceUnits()
            .getWithResponse("myResourceGroup", "myTopology", "myService", "myServiceUnit", Context.NONE);
    }
}
```

### ServiceUnits_List

```java
import com.azure.core.util.Context;

/** Samples for ServiceUnits List. */
public final class ServiceUnitsListSamples {
    /*
     * x-ms-original-file: specification/deploymentmanager/resource-manager/Microsoft.DeploymentManager/preview/2019-11-01-preview/examples/serviceunits_list.json
     */
    /**
     * Sample code: List service units.
     *
     * @param manager Entry point to DeploymentManager.
     */
    public static void listServiceUnits(com.azure.resourcemanager.deploymentmanager.DeploymentManager manager) {
        manager.serviceUnits().listWithResponse("myResourceGroup", "myTopology", "myService", Context.NONE);
    }
}
```

### Services_CreateOrUpdate

```java
import java.util.HashMap;
import java.util.Map;

/** Samples for Services CreateOrUpdate. */
public final class ServicesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/deploymentmanager/resource-manager/Microsoft.DeploymentManager/preview/2019-11-01-preview/examples/service_createorupdate.json
     */
    /**
     * Sample code: Create service.
     *
     * @param manager Entry point to DeploymentManager.
     */
    public static void createService(com.azure.resourcemanager.deploymentmanager.DeploymentManager manager) {
        manager
            .services()
            .define("myService")
            .withRegion("centralus")
            .withExistingServiceTopology("myResourceGroup", "myTopology")
            .withTags(mapOf())
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

### Services_Delete

```java
import com.azure.core.util.Context;

/** Samples for Services Delete. */
public final class ServicesDeleteSamples {
    /*
     * x-ms-original-file: specification/deploymentmanager/resource-manager/Microsoft.DeploymentManager/preview/2019-11-01-preview/examples/service_delete.json
     */
    /**
     * Sample code: Delete service.
     *
     * @param manager Entry point to DeploymentManager.
     */
    public static void deleteService(com.azure.resourcemanager.deploymentmanager.DeploymentManager manager) {
        manager.services().deleteWithResponse("myResourceGroup", "myTopology", "myService", Context.NONE);
    }
}
```

### Services_Get

```java
import com.azure.core.util.Context;

/** Samples for Services Get. */
public final class ServicesGetSamples {
    /*
     * x-ms-original-file: specification/deploymentmanager/resource-manager/Microsoft.DeploymentManager/preview/2019-11-01-preview/examples/service_get.json
     */
    /**
     * Sample code: Get service.
     *
     * @param manager Entry point to DeploymentManager.
     */
    public static void getService(com.azure.resourcemanager.deploymentmanager.DeploymentManager manager) {
        manager.services().getWithResponse("myResourceGroup", "myTopology", "myService", Context.NONE);
    }
}
```

### Services_List

```java
import com.azure.core.util.Context;

/** Samples for Services List. */
public final class ServicesListSamples {
    /*
     * x-ms-original-file: specification/deploymentmanager/resource-manager/Microsoft.DeploymentManager/preview/2019-11-01-preview/examples/services_list.json
     */
    /**
     * Sample code: List services.
     *
     * @param manager Entry point to DeploymentManager.
     */
    public static void listServices(com.azure.resourcemanager.deploymentmanager.DeploymentManager manager) {
        manager.services().listWithResponse("myResourceGroup", "myTopology", Context.NONE);
    }
}
```

### Steps_CreateOrUpdate

```java
import com.azure.resourcemanager.deploymentmanager.models.ApiKeyAuthentication;
import com.azure.resourcemanager.deploymentmanager.models.HealthCheckStepProperties;
import com.azure.resourcemanager.deploymentmanager.models.RestAuthLocation;
import com.azure.resourcemanager.deploymentmanager.models.RestHealthCheck;
import com.azure.resourcemanager.deploymentmanager.models.RestHealthCheckStepAttributes;
import com.azure.resourcemanager.deploymentmanager.models.RestMatchQuantifier;
import com.azure.resourcemanager.deploymentmanager.models.RestRequest;
import com.azure.resourcemanager.deploymentmanager.models.RestRequestMethod;
import com.azure.resourcemanager.deploymentmanager.models.RestResponse;
import com.azure.resourcemanager.deploymentmanager.models.RestResponseRegex;
import com.azure.resourcemanager.deploymentmanager.models.WaitStepAttributes;
import com.azure.resourcemanager.deploymentmanager.models.WaitStepProperties;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Steps CreateOrUpdate. */
public final class StepsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/deploymentmanager/resource-manager/Microsoft.DeploymentManager/preview/2019-11-01-preview/examples/step_health_check_createorupdate.json
     */
    /**
     * Sample code: Create health check step.
     *
     * @param manager Entry point to DeploymentManager.
     */
    public static void createHealthCheckStep(com.azure.resourcemanager.deploymentmanager.DeploymentManager manager) {
        manager
            .steps()
            .define("healthCheckStep")
            .withRegion("centralus")
            .withExistingResourceGroup("myResourceGroup")
            .withProperties(
                new HealthCheckStepProperties()
                    .withAttributes(
                        new RestHealthCheckStepAttributes()
                            .withWaitDuration("PT15M")
                            .withMaxElasticDuration("PT30M")
                            .withHealthyStateDuration("PT2H")
                            .withHealthChecks(
                                Arrays
                                    .asList(
                                        new RestHealthCheck()
                                            .withName("appHealth")
                                            .withRequest(
                                                new RestRequest()
                                                    .withMethod(RestRequestMethod.GET)
                                                    .withUri(
                                                        "https://resthealth.healthservice.com/api/applications/contosoApp/healthStatus")
                                                    .withAuthentication(
                                                        new ApiKeyAuthentication()
                                                            .withName("Code")
                                                            .withIn(RestAuthLocation.QUERY)
                                                            .withValue(
                                                                "NBCapiMOBQyAAbCkeytoPadnvO0eGHmidwFz5rXpappznKp3Jt7LLg==")))
                                            .withResponse(
                                                new RestResponse()
                                                    .withSuccessStatusCodes(Arrays.asList("OK"))
                                                    .withRegex(
                                                        new RestResponseRegex()
                                                            .withMatches(
                                                                Arrays
                                                                    .asList(
                                                                        "(?i)Contoso-App",
                                                                        "(?i)\"health_status\":((.|\n"
                                                                            + ")*)\"(green|yellow)\"",
                                                                        "(?mi)^(\"application_host\": 94781052)$"))
                                                            .withMatchQuantifier(RestMatchQuantifier.ALL))),
                                        new RestHealthCheck()
                                            .withName("serviceHealth")
                                            .withRequest(
                                                new RestRequest()
                                                    .withMethod(RestRequestMethod.GET)
                                                    .withUri(
                                                        "https://resthealth.healthservice.com/api/services/contosoService/healthStatus")
                                                    .withAuthentication(
                                                        new ApiKeyAuthentication()
                                                            .withName("code")
                                                            .withIn(RestAuthLocation.HEADER)
                                                            .withValue(
                                                                "NBCapiMOBQyAAbCkeytoPadnvO0eGHmidwFz5rXpappznKp3Jt7LLg==")))
                                            .withResponse(
                                                new RestResponse()
                                                    .withSuccessStatusCodes(Arrays.asList("OK"))
                                                    .withRegex(
                                                        new RestResponseRegex()
                                                            .withMatches(
                                                                Arrays
                                                                    .asList(
                                                                        "(?i)Contoso-Service-EndToEnd",
                                                                        "(?i)\"health_status\":((.|\n)*)\"(green)\""))
                                                            .withMatchQuantifier(RestMatchQuantifier.ALL)))))))
            .withTags(mapOf())
            .create();
    }

    /*
     * x-ms-original-file: specification/deploymentmanager/resource-manager/Microsoft.DeploymentManager/preview/2019-11-01-preview/examples/step_wait_createorupdate.json
     */
    /**
     * Sample code: Create wait step.
     *
     * @param manager Entry point to DeploymentManager.
     */
    public static void createWaitStep(com.azure.resourcemanager.deploymentmanager.DeploymentManager manager) {
        manager
            .steps()
            .define("waitStep")
            .withRegion("centralus")
            .withExistingResourceGroup("myResourceGroup")
            .withProperties(new WaitStepProperties().withAttributes(new WaitStepAttributes().withDuration("PT20M")))
            .withTags(mapOf())
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

### Steps_Delete

```java
import com.azure.core.util.Context;

/** Samples for Steps Delete. */
public final class StepsDeleteSamples {
    /*
     * x-ms-original-file: specification/deploymentmanager/resource-manager/Microsoft.DeploymentManager/preview/2019-11-01-preview/examples/step_delete.json
     */
    /**
     * Sample code: Delete deployment step.
     *
     * @param manager Entry point to DeploymentManager.
     */
    public static void deleteDeploymentStep(com.azure.resourcemanager.deploymentmanager.DeploymentManager manager) {
        manager.steps().deleteWithResponse("myResourceGroup", "deploymentStep1", Context.NONE);
    }
}
```

### Steps_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Steps GetByResourceGroup. */
public final class StepsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/deploymentmanager/resource-manager/Microsoft.DeploymentManager/preview/2019-11-01-preview/examples/step_get.json
     */
    /**
     * Sample code: Get deployment step.
     *
     * @param manager Entry point to DeploymentManager.
     */
    public static void getDeploymentStep(com.azure.resourcemanager.deploymentmanager.DeploymentManager manager) {
        manager.steps().getByResourceGroupWithResponse("myResourceGroup", "waitStep", Context.NONE);
    }
}
```

### Steps_List

```java
import com.azure.core.util.Context;

/** Samples for Steps List. */
public final class StepsListSamples {
    /*
     * x-ms-original-file: specification/deploymentmanager/resource-manager/Microsoft.DeploymentManager/preview/2019-11-01-preview/examples/steps_list.json
     */
    /**
     * Sample code: List steps.
     *
     * @param manager Entry point to DeploymentManager.
     */
    public static void listSteps(com.azure.resourcemanager.deploymentmanager.DeploymentManager manager) {
        manager.steps().listWithResponse("myResourceGroup", Context.NONE);
    }
}
```


# Code snippets and samples


## AttachedNetworks

- [CreateOrUpdate](#attachednetworks_createorupdate)
- [Delete](#attachednetworks_delete)
- [GetByDevCenter](#attachednetworks_getbydevcenter)
- [GetByProject](#attachednetworks_getbyproject)
- [ListByDevCenter](#attachednetworks_listbydevcenter)
- [ListByProject](#attachednetworks_listbyproject)

## Catalogs

- [CreateOrUpdate](#catalogs_createorupdate)
- [Delete](#catalogs_delete)
- [Get](#catalogs_get)
- [ListByDevCenter](#catalogs_listbydevcenter)
- [Sync](#catalogs_sync)
- [Update](#catalogs_update)

## DevBoxDefinitions

- [CreateOrUpdate](#devboxdefinitions_createorupdate)
- [Delete](#devboxdefinitions_delete)
- [Get](#devboxdefinitions_get)
- [GetByProject](#devboxdefinitions_getbyproject)
- [ListByDevCenter](#devboxdefinitions_listbydevcenter)
- [ListByProject](#devboxdefinitions_listbyproject)
- [Update](#devboxdefinitions_update)

## DevCenters

- [CreateOrUpdate](#devcenters_createorupdate)
- [Delete](#devcenters_delete)
- [GetByResourceGroup](#devcenters_getbyresourcegroup)
- [List](#devcenters_list)
- [ListByResourceGroup](#devcenters_listbyresourcegroup)
- [Update](#devcenters_update)

## EnvironmentTypes

- [CreateOrUpdate](#environmenttypes_createorupdate)
- [Delete](#environmenttypes_delete)
- [Get](#environmenttypes_get)
- [ListByDevCenter](#environmenttypes_listbydevcenter)
- [Update](#environmenttypes_update)

## Galleries

- [CreateOrUpdate](#galleries_createorupdate)
- [Delete](#galleries_delete)
- [Get](#galleries_get)
- [ListByDevCenter](#galleries_listbydevcenter)

## ImageVersions

- [Get](#imageversions_get)
- [ListByImage](#imageversions_listbyimage)

## Images

- [Get](#images_get)
- [ListByDevCenter](#images_listbydevcenter)
- [ListByGallery](#images_listbygallery)

## NetworkConnections

- [CreateOrUpdate](#networkconnections_createorupdate)
- [Delete](#networkconnections_delete)
- [GetByResourceGroup](#networkconnections_getbyresourcegroup)
- [GetHealthDetails](#networkconnections_gethealthdetails)
- [List](#networkconnections_list)
- [ListByResourceGroup](#networkconnections_listbyresourcegroup)
- [ListHealthDetails](#networkconnections_listhealthdetails)
- [RunHealthChecks](#networkconnections_runhealthchecks)
- [Update](#networkconnections_update)

## OperationStatuses

- [Get](#operationstatuses_get)

## Operations

- [List](#operations_list)

## Pools

- [CreateOrUpdate](#pools_createorupdate)
- [Delete](#pools_delete)
- [Get](#pools_get)
- [ListByProject](#pools_listbyproject)
- [Update](#pools_update)

## ProjectEnvironmentTypes

- [CreateOrUpdate](#projectenvironmenttypes_createorupdate)
- [Delete](#projectenvironmenttypes_delete)
- [Get](#projectenvironmenttypes_get)
- [List](#projectenvironmenttypes_list)
- [Update](#projectenvironmenttypes_update)

## Projects

- [CreateOrUpdate](#projects_createorupdate)
- [Delete](#projects_delete)
- [GetByResourceGroup](#projects_getbyresourcegroup)
- [List](#projects_list)
- [ListByResourceGroup](#projects_listbyresourcegroup)
- [Update](#projects_update)

## Schedules

- [CreateOrUpdate](#schedules_createorupdate)
- [Delete](#schedules_delete)
- [Get](#schedules_get)
- [ListByPool](#schedules_listbypool)
- [Update](#schedules_update)

## Skus

- [List](#skus_list)

## Usages

- [ListByLocation](#usages_listbylocation)
### AttachedNetworks_CreateOrUpdate

```java
/** Samples for AttachedNetworks CreateOrUpdate. */
public final class AttachedNetworksCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/AttachedNetworks_Create.json
     */
    /**
     * Sample code: AttachedNetworks_Create.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void attachedNetworksCreate(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager
            .attachedNetworks()
            .define("{attachedNetworkConnectionName}")
            .withExistingDevcenter("rg1", "Contoso")
            .withNetworkConnectionId(
                "/subscriptions/{subscriptionId}/resourceGroups/rg1/providers/Microsoft.DevCenter/NetworkConnections/network-uswest3")
            .create();
    }
}
```

### AttachedNetworks_Delete

```java
import com.azure.core.util.Context;

/** Samples for AttachedNetworks Delete. */
public final class AttachedNetworksDeleteSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/AttachedNetworks_Delete.json
     */
    /**
     * Sample code: AttachedNetworks_Delete.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void attachedNetworksDelete(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.attachedNetworks().delete("rg1", "Contoso", "{attachedNetworkConnectionName}", Context.NONE);
    }
}
```

### AttachedNetworks_GetByDevCenter

```java
import com.azure.core.util.Context;

/** Samples for AttachedNetworks GetByDevCenter. */
public final class AttachedNetworksGetByDevCenterSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/AttachedNetworks_GetByDevCenter.json
     */
    /**
     * Sample code: AttachedNetworks_GetByDevCenter.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void attachedNetworksGetByDevCenter(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.attachedNetworks().getByDevCenterWithResponse("rg1", "Contoso", "network-uswest3", Context.NONE);
    }
}
```

### AttachedNetworks_GetByProject

```java
import com.azure.core.util.Context;

/** Samples for AttachedNetworks GetByProject. */
public final class AttachedNetworksGetByProjectSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/AttachedNetworks_GetByProject.json
     */
    /**
     * Sample code: AttachedNetworks_GetByProject.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void attachedNetworksGetByProject(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.attachedNetworks().getByProjectWithResponse("rg1", "{projectName}", "network-uswest3", Context.NONE);
    }
}
```

### AttachedNetworks_ListByDevCenter

```java
import com.azure.core.util.Context;

/** Samples for AttachedNetworks ListByDevCenter. */
public final class AttachedNetworksListByDevCenterSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/AttachedNetworks_ListByDevCenter.json
     */
    /**
     * Sample code: AttachedNetworks_ListByDevCenter.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void attachedNetworksListByDevCenter(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.attachedNetworks().listByDevCenter("rg1", "Contoso", null, Context.NONE);
    }
}
```

### AttachedNetworks_ListByProject

```java
import com.azure.core.util.Context;

/** Samples for AttachedNetworks ListByProject. */
public final class AttachedNetworksListByProjectSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/AttachedNetworks_ListByProject.json
     */
    /**
     * Sample code: AttachedNetworks_ListByProject.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void attachedNetworksListByProject(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.attachedNetworks().listByProject("rg1", "{projectName}", null, Context.NONE);
    }
}
```

### Catalogs_CreateOrUpdate

```java
import com.azure.resourcemanager.devcenter.models.GitCatalog;

/** Samples for Catalogs CreateOrUpdate. */
public final class CatalogsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/Catalogs_CreateAdo.json
     */
    /**
     * Sample code: Catalogs_CreateOrUpdateAdo.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void catalogsCreateOrUpdateAdo(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager
            .catalogs()
            .define("{catalogName}")
            .withExistingDevcenter("rg1", "Contoso")
            .withAdoGit(
                new GitCatalog()
                    .withUri("https://contoso@dev.azure.com/contoso/contosoOrg/_git/centralrepo-fakecontoso")
                    .withBranch("main")
                    .withSecretIdentifier("https://contosokv.vault.azure.net/secrets/CentralRepoPat")
                    .withPath("/templates"))
            .create();
    }

    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/Catalogs_CreateGitHub.json
     */
    /**
     * Sample code: Catalogs_CreateOrUpdateGitHub.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void catalogsCreateOrUpdateGitHub(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager
            .catalogs()
            .define("{catalogName}")
            .withExistingDevcenter("rg1", "Contoso")
            .withGitHub(
                new GitCatalog()
                    .withUri("https://github.com/Contoso/centralrepo-fake.git")
                    .withBranch("main")
                    .withSecretIdentifier("https://contosokv.vault.azure.net/secrets/CentralRepoPat")
                    .withPath("/templates"))
            .create();
    }
}
```

### Catalogs_Delete

```java
import com.azure.core.util.Context;

/** Samples for Catalogs Delete. */
public final class CatalogsDeleteSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/Catalogs_Delete.json
     */
    /**
     * Sample code: Catalogs_Delete.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void catalogsDelete(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.catalogs().delete("rg1", "Contoso", "{catalogName}", Context.NONE);
    }
}
```

### Catalogs_Get

```java
import com.azure.core.util.Context;

/** Samples for Catalogs Get. */
public final class CatalogsGetSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/Catalogs_Get.json
     */
    /**
     * Sample code: Catalogs_Get.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void catalogsGet(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.catalogs().getWithResponse("rg1", "Contoso", "{catalogName}", Context.NONE);
    }
}
```

### Catalogs_ListByDevCenter

```java
import com.azure.core.util.Context;

/** Samples for Catalogs ListByDevCenter. */
public final class CatalogsListByDevCenterSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/Catalogs_List.json
     */
    /**
     * Sample code: Catalogs_ListByDevCenter.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void catalogsListByDevCenter(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.catalogs().listByDevCenter("rg1", "Contoso", null, Context.NONE);
    }
}
```

### Catalogs_Sync

```java
import com.azure.core.util.Context;

/** Samples for Catalogs Sync. */
public final class CatalogsSyncSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/Catalogs_Sync.json
     */
    /**
     * Sample code: Catalogs_Sync.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void catalogsSync(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.catalogs().sync("rg1", "Contoso", "{catalogName}", Context.NONE);
    }
}
```

### Catalogs_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devcenter.models.Catalog;
import com.azure.resourcemanager.devcenter.models.GitCatalog;

/** Samples for Catalogs Update. */
public final class CatalogsUpdateSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/Catalogs_Patch.json
     */
    /**
     * Sample code: Catalogs_Update.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void catalogsUpdate(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        Catalog resource =
            manager.catalogs().getWithResponse("rg1", "Contoso", "{catalogName}", Context.NONE).getValue();
        resource.update().withGitHub(new GitCatalog().withPath("/environments")).apply();
    }
}
```

### DevBoxDefinitions_CreateOrUpdate

```java
import com.azure.resourcemanager.devcenter.models.ImageReference;
import com.azure.resourcemanager.devcenter.models.Sku;

/** Samples for DevBoxDefinitions CreateOrUpdate. */
public final class DevBoxDefinitionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/DevBoxDefinitions_Create.json
     */
    /**
     * Sample code: DevBoxDefinitions_Create.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void devBoxDefinitionsCreate(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager
            .devBoxDefinitions()
            .define("WebDevBox")
            .withRegion("centralus")
            .withExistingDevcenter("rg1", "Contoso")
            .withImageReference(
                new ImageReference()
                    .withId(
                        "/subscriptions/0ac520ee-14c0-480f-b6c9-0a90c58ffff/resourceGroups/Example/providers/Microsoft.DevCenter/devcenters/Contoso/galleries/contosogallery/images/exampleImage/version/1.0.0"))
            .withSku(new Sku().withName("Preview"))
            .withOsStorageType("SSD_1024")
            .create();
    }
}
```

### DevBoxDefinitions_Delete

```java
import com.azure.core.util.Context;

/** Samples for DevBoxDefinitions Delete. */
public final class DevBoxDefinitionsDeleteSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/DevBoxDefinitions_Delete.json
     */
    /**
     * Sample code: DevBoxDefinitions_Delete.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void devBoxDefinitionsDelete(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.devBoxDefinitions().delete("rg1", "Contoso", "WebDevBox", Context.NONE);
    }
}
```

### DevBoxDefinitions_Get

```java
import com.azure.core.util.Context;

/** Samples for DevBoxDefinitions Get. */
public final class DevBoxDefinitionsGetSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/DevBoxDefinitions_Get.json
     */
    /**
     * Sample code: DevBoxDefinitions_Get.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void devBoxDefinitionsGet(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.devBoxDefinitions().getWithResponse("rg1", "Contoso", "WebDevBox", Context.NONE);
    }
}
```

### DevBoxDefinitions_GetByProject

```java
import com.azure.core.util.Context;

/** Samples for DevBoxDefinitions GetByProject. */
public final class DevBoxDefinitionsGetByProjectSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/DevBoxDefinitions_GetByProject.json
     */
    /**
     * Sample code: DevBoxDefinitions_GetByProject.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void devBoxDefinitionsGetByProject(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.devBoxDefinitions().getByProjectWithResponse("rg1", "ContosoProject", "WebDevBox", Context.NONE);
    }
}
```

### DevBoxDefinitions_ListByDevCenter

```java
import com.azure.core.util.Context;

/** Samples for DevBoxDefinitions ListByDevCenter. */
public final class DevBoxDefinitionsListByDevCenterSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/DevBoxDefinitions_ListByDevCenter.json
     */
    /**
     * Sample code: DevBoxDefinitions_ListByDevCenter.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void devBoxDefinitionsListByDevCenter(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.devBoxDefinitions().listByDevCenter("rg1", "Contoso", null, Context.NONE);
    }
}
```

### DevBoxDefinitions_ListByProject

```java
import com.azure.core.util.Context;

/** Samples for DevBoxDefinitions ListByProject. */
public final class DevBoxDefinitionsListByProjectSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/DevBoxDefinitions_ListByProject.json
     */
    /**
     * Sample code: DevBoxDefinitions_ListByProject.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void devBoxDefinitionsListByProject(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.devBoxDefinitions().listByProject("rg1", "ContosoProject", null, Context.NONE);
    }
}
```

### DevBoxDefinitions_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devcenter.models.DevBoxDefinition;
import com.azure.resourcemanager.devcenter.models.ImageReference;

/** Samples for DevBoxDefinitions Update. */
public final class DevBoxDefinitionsUpdateSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/DevBoxDefinitions_Patch.json
     */
    /**
     * Sample code: DevBoxDefinitions_Patch.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void devBoxDefinitionsPatch(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        DevBoxDefinition resource =
            manager.devBoxDefinitions().getWithResponse("rg1", "Contoso", "WebDevBox", Context.NONE).getValue();
        resource
            .update()
            .withImageReference(
                new ImageReference()
                    .withId(
                        "/subscriptions/0ac520ee-14c0-480f-b6c9-0a90c58ffff/resourceGroups/Example/providers/Microsoft.DevCenter/devcenters/Contoso/galleries/contosogallery/images/exampleImage/version/2.0.0"))
            .apply();
    }
}
```

### DevCenters_CreateOrUpdate

```java
import com.azure.resourcemanager.devcenter.models.ManagedServiceIdentity;
import com.azure.resourcemanager.devcenter.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.devcenter.models.UserAssignedIdentity;
import java.util.HashMap;
import java.util.Map;

/** Samples for DevCenters CreateOrUpdate. */
public final class DevCentersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/DevCenters_Create.json
     */
    /**
     * Sample code: DevCenters_Create.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void devCentersCreate(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager
            .devCenters()
            .define("Contoso")
            .withRegion("centralus")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("CostCode", "12345"))
            .create();
    }

    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/DevCenters_CreateWithUserIdentity.json
     */
    /**
     * Sample code: DevCenters_CreateWithUserIdentity.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void devCentersCreateWithUserIdentity(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager
            .devCenters()
            .define("Contoso")
            .withRegion("centralus")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("CostCode", "12345"))
            .withIdentity(
                new ManagedServiceIdentity()
                    .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/identityGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/testidentity1",
                            new UserAssignedIdentity())))
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

### DevCenters_Delete

```java
import com.azure.core.util.Context;

/** Samples for DevCenters Delete. */
public final class DevCentersDeleteSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/DevCenters_Delete.json
     */
    /**
     * Sample code: DevCenters_Delete.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void devCentersDelete(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.devCenters().delete("rg1", "Contoso", Context.NONE);
    }
}
```

### DevCenters_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for DevCenters GetByResourceGroup. */
public final class DevCentersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/DevCenters_Get.json
     */
    /**
     * Sample code: DevCenters_Get.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void devCentersGet(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.devCenters().getByResourceGroupWithResponse("rg1", "Contoso", Context.NONE);
    }
}
```

### DevCenters_List

```java
import com.azure.core.util.Context;

/** Samples for DevCenters List. */
public final class DevCentersListSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/DevCenters_ListBySubscription.json
     */
    /**
     * Sample code: DevCenters_ListBySubscription.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void devCentersListBySubscription(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.devCenters().list(null, Context.NONE);
    }
}
```

### DevCenters_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for DevCenters ListByResourceGroup. */
public final class DevCentersListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/DevCenters_ListByResourceGroup.json
     */
    /**
     * Sample code: DevCenters_ListByResourceGroup.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void devCentersListByResourceGroup(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.devCenters().listByResourceGroup("rg1", null, Context.NONE);
    }
}
```

### DevCenters_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devcenter.models.DevCenter;
import java.util.HashMap;
import java.util.Map;

/** Samples for DevCenters Update. */
public final class DevCentersUpdateSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/DevCenters_Patch.json
     */
    /**
     * Sample code: DevCenters_Update.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void devCentersUpdate(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        DevCenter resource =
            manager.devCenters().getByResourceGroupWithResponse("rg1", "Contoso", Context.NONE).getValue();
        resource.update().withTags(mapOf("CostCode", "12345")).apply();
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

### EnvironmentTypes_CreateOrUpdate

```java
import java.util.HashMap;
import java.util.Map;

/** Samples for EnvironmentTypes CreateOrUpdate. */
public final class EnvironmentTypesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/EnvironmentTypes_Put.json
     */
    /**
     * Sample code: EnvironmentTypes_CreateOrUpdate.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void environmentTypesCreateOrUpdate(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager
            .environmentTypes()
            .define("{environmentTypeName}")
            .withExistingDevcenter("rg1", "Contoso")
            .withTags(mapOf("Owner", "superuser"))
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

### EnvironmentTypes_Delete

```java
import com.azure.core.util.Context;

/** Samples for EnvironmentTypes Delete. */
public final class EnvironmentTypesDeleteSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/EnvironmentTypes_Delete.json
     */
    /**
     * Sample code: EnvironmentTypes_Delete.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void environmentTypesDelete(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.environmentTypes().deleteWithResponse("rg1", "Contoso", "{environmentTypeName}", Context.NONE);
    }
}
```

### EnvironmentTypes_Get

```java
import com.azure.core.util.Context;

/** Samples for EnvironmentTypes Get. */
public final class EnvironmentTypesGetSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/EnvironmentTypes_Get.json
     */
    /**
     * Sample code: EnvironmentTypes_Get.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void environmentTypesGet(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.environmentTypes().getWithResponse("rg1", "Contoso", "{environmentTypeName}", Context.NONE);
    }
}
```

### EnvironmentTypes_ListByDevCenter

```java
import com.azure.core.util.Context;

/** Samples for EnvironmentTypes ListByDevCenter. */
public final class EnvironmentTypesListByDevCenterSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/EnvironmentTypes_List.json
     */
    /**
     * Sample code: EnvironmentTypes_ListByDevCenter.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void environmentTypesListByDevCenter(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.environmentTypes().listByDevCenter("rg1", "Contoso", null, Context.NONE);
    }
}
```

### EnvironmentTypes_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devcenter.models.EnvironmentType;
import java.util.HashMap;
import java.util.Map;

/** Samples for EnvironmentTypes Update. */
public final class EnvironmentTypesUpdateSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/EnvironmentTypes_Patch.json
     */
    /**
     * Sample code: EnvironmentTypes_Update.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void environmentTypesUpdate(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        EnvironmentType resource =
            manager
                .environmentTypes()
                .getWithResponse("rg1", "Contoso", "{environmentTypeName}", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("Owner", "superuser")).apply();
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

### Galleries_CreateOrUpdate

```java
/** Samples for Galleries CreateOrUpdate. */
public final class GalleriesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/Galleries_Create.json
     */
    /**
     * Sample code: Galleries_CreateOrUpdate.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void galleriesCreateOrUpdate(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager
            .galleries()
            .define("{galleryName}")
            .withExistingDevcenter("rg1", "Contoso")
            .withGalleryResourceId(
                "/subscriptions/{subscriptionId}/resourceGroups/rg1/providers/Microsoft.Compute/galleries/{galleryName}")
            .create();
    }
}
```

### Galleries_Delete

```java
import com.azure.core.util.Context;

/** Samples for Galleries Delete. */
public final class GalleriesDeleteSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/Galleries_Delete.json
     */
    /**
     * Sample code: Galleries_Delete.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void galleriesDelete(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.galleries().delete("rg1", "Contoso", "{galleryName}", Context.NONE);
    }
}
```

### Galleries_Get

```java
import com.azure.core.util.Context;

/** Samples for Galleries Get. */
public final class GalleriesGetSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/Galleries_Get.json
     */
    /**
     * Sample code: Galleries_Get.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void galleriesGet(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.galleries().getWithResponse("rg1", "Contoso", "{galleryName}", Context.NONE);
    }
}
```

### Galleries_ListByDevCenter

```java
import com.azure.core.util.Context;

/** Samples for Galleries ListByDevCenter. */
public final class GalleriesListByDevCenterSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/Galleries_List.json
     */
    /**
     * Sample code: Galleries_ListByDevCenter.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void galleriesListByDevCenter(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.galleries().listByDevCenter("rg1", "Contoso", null, Context.NONE);
    }
}
```

### ImageVersions_Get

```java
import com.azure.core.util.Context;

/** Samples for ImageVersions Get. */
public final class ImageVersionsGetSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/ImageVersions_Get.json
     */
    /**
     * Sample code: Versions_Get.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void versionsGet(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager
            .imageVersions()
            .getWithResponse("rg1", "Contoso", "DefaultDevGallery", "Win11", "{versionName}", Context.NONE);
    }
}
```

### ImageVersions_ListByImage

```java
import com.azure.core.util.Context;

/** Samples for ImageVersions ListByImage. */
public final class ImageVersionsListByImageSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/ImageVersions_List.json
     */
    /**
     * Sample code: ImageVersions_ListByImage.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void imageVersionsListByImage(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.imageVersions().listByImage("rg1", "Contoso", "DefaultDevGallery", "Win11", Context.NONE);
    }
}
```

### Images_Get

```java
import com.azure.core.util.Context;

/** Samples for Images Get. */
public final class ImagesGetSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/Images_Get.json
     */
    /**
     * Sample code: Images_Get.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void imagesGet(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.images().getWithResponse("rg1", "Contoso", "DefaultDevGallery", "{imageName}", Context.NONE);
    }
}
```

### Images_ListByDevCenter

```java
import com.azure.core.util.Context;

/** Samples for Images ListByDevCenter. */
public final class ImagesListByDevCenterSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/Images_ListByDevCenter.json
     */
    /**
     * Sample code: Images_ListByDevCenter.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void imagesListByDevCenter(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.images().listByDevCenter("rg1", "Contoso", null, Context.NONE);
    }
}
```

### Images_ListByGallery

```java
import com.azure.core.util.Context;

/** Samples for Images ListByGallery. */
public final class ImagesListByGallerySamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/Images_ListByGallery.json
     */
    /**
     * Sample code: Images_ListByGallery.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void imagesListByGallery(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.images().listByGallery("rg1", "Contoso", "DevGallery", null, Context.NONE);
    }
}
```

### NetworkConnections_CreateOrUpdate

```java
import com.azure.resourcemanager.devcenter.models.DomainJoinType;

/** Samples for NetworkConnections CreateOrUpdate. */
public final class NetworkConnectionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/NetworkConnections_Put.json
     */
    /**
     * Sample code: NetworkConnections_CreateOrUpdate.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void networkConnectionsCreateOrUpdate(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager
            .networkConnections()
            .define("uswest3network")
            .withRegion("centralus")
            .withExistingResourceGroup("rg1")
            .withNetworkingResourceGroupName("NetworkInterfaces")
            .withDomainJoinType(DomainJoinType.HYBRID_AZURE_ADJOIN)
            .withSubnetId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/ExampleRG/providers/Microsoft.Network/virtualNetworks/ExampleVNet/subnets/default")
            .withDomainName("mydomaincontroller.local")
            .withDomainUsername("testuser@mydomaincontroller.local")
            .withDomainPassword("Password value for user")
            .create();
    }
}
```

### NetworkConnections_Delete

```java
import com.azure.core.util.Context;

/** Samples for NetworkConnections Delete. */
public final class NetworkConnectionsDeleteSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/NetworkConnections_Delete.json
     */
    /**
     * Sample code: NetworkConnections_Delete.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void networkConnectionsDelete(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.networkConnections().delete("rg1", "{networkConnectionName}", Context.NONE);
    }
}
```

### NetworkConnections_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for NetworkConnections GetByResourceGroup. */
public final class NetworkConnectionsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/NetworkConnections_Get.json
     */
    /**
     * Sample code: NetworkConnections_Get.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void networkConnectionsGet(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.networkConnections().getByResourceGroupWithResponse("rg1", "uswest3network", Context.NONE);
    }
}
```

### NetworkConnections_GetHealthDetails

```java
import com.azure.core.util.Context;

/** Samples for NetworkConnections GetHealthDetails. */
public final class NetworkConnectionsGetHealthDetailsSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/NetworkConnections_GetHealthDetails.json
     */
    /**
     * Sample code: NetworkConnections_GetHealthDetails.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void networkConnectionsGetHealthDetails(
        com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.networkConnections().getHealthDetailsWithResponse("rg1", "{networkConnectionName}", Context.NONE);
    }
}
```

### NetworkConnections_List

```java
import com.azure.core.util.Context;

/** Samples for NetworkConnections List. */
public final class NetworkConnectionsListSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/NetworkConnections_ListBySubscription.json
     */
    /**
     * Sample code: NetworkConnections_ListBySubscription.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void networkConnectionsListBySubscription(
        com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.networkConnections().list(null, Context.NONE);
    }
}
```

### NetworkConnections_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for NetworkConnections ListByResourceGroup. */
public final class NetworkConnectionsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/NetworkConnections_ListByResourceGroup.json
     */
    /**
     * Sample code: NetworkConnections_ListByResourceGroup.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void networkConnectionsListByResourceGroup(
        com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.networkConnections().listByResourceGroup("rg1", null, Context.NONE);
    }
}
```

### NetworkConnections_ListHealthDetails

```java
import com.azure.core.util.Context;

/** Samples for NetworkConnections ListHealthDetails. */
public final class NetworkConnectionsListHealthDetailsSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/NetworkConnections_ListHealthDetails.json
     */
    /**
     * Sample code: NetworkConnections_ListHealthDetails.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void networkConnectionsListHealthDetails(
        com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.networkConnections().listHealthDetails("rg1", "uswest3network", null, Context.NONE);
    }
}
```

### NetworkConnections_RunHealthChecks

```java
import com.azure.core.util.Context;

/** Samples for NetworkConnections RunHealthChecks. */
public final class NetworkConnectionsRunHealthChecksSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/NetworkConnections_RunHealthChecks.json
     */
    /**
     * Sample code: NetworkConnections_RunHealthChecks.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void networkConnectionsRunHealthChecks(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.networkConnections().runHealthChecksWithResponse("rg1", "uswest3network", Context.NONE);
    }
}
```

### NetworkConnections_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devcenter.models.NetworkConnection;

/** Samples for NetworkConnections Update. */
public final class NetworkConnectionsUpdateSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/NetworkConnections_Patch.json
     */
    /**
     * Sample code: NetworkConnections_Update.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void networkConnectionsUpdate(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        NetworkConnection resource =
            manager
                .networkConnections()
                .getByResourceGroupWithResponse("rg1", "uswest3network", Context.NONE)
                .getValue();
        resource.update().withDomainPassword("New Password value for user").apply();
    }
}
```

### OperationStatuses_Get

```java
import com.azure.core.util.Context;

/** Samples for OperationStatuses Get. */
public final class OperationStatusesGetSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/OperationStatus_Get.json
     */
    /**
     * Sample code: Get OperationStatus.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void getOperationStatus(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.operationStatuses().getWithResponse("{location}", "{operationId}", Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/Operations_Get.json
     */
    /**
     * Sample code: Operations_Get.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void operationsGet(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### Pools_CreateOrUpdate

```java
import com.azure.resourcemanager.devcenter.models.LicenseType;
import com.azure.resourcemanager.devcenter.models.LocalAdminStatus;

/** Samples for Pools CreateOrUpdate. */
public final class PoolsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/Pools_Put.json
     */
    /**
     * Sample code: Pools_CreateOrUpdate.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void poolsCreateOrUpdate(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager
            .pools()
            .define("{poolName}")
            .withRegion("centralus")
            .withExistingProject("rg1", "{projectName}")
            .withDevBoxDefinitionName("WebDevBox")
            .withNetworkConnectionName("Network1-westus2")
            .withLicenseType(LicenseType.WINDOWS_CLIENT)
            .withLocalAdministrator(LocalAdminStatus.ENABLED)
            .create();
    }
}
```

### Pools_Delete

```java
import com.azure.core.util.Context;

/** Samples for Pools Delete. */
public final class PoolsDeleteSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/Pools_Delete.json
     */
    /**
     * Sample code: Pools_Delete.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void poolsDelete(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.pools().delete("rg1", "{projectName}", "poolName", Context.NONE);
    }
}
```

### Pools_Get

```java
import com.azure.core.util.Context;

/** Samples for Pools Get. */
public final class PoolsGetSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/Pools_Get.json
     */
    /**
     * Sample code: Pools_Get.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void poolsGet(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.pools().getWithResponse("rg1", "{projectName}", "{poolName}", Context.NONE);
    }
}
```

### Pools_ListByProject

```java
import com.azure.core.util.Context;

/** Samples for Pools ListByProject. */
public final class PoolsListByProjectSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/Pools_List.json
     */
    /**
     * Sample code: Pools_ListByProject.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void poolsListByProject(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.pools().listByProject("rg1", "{projectName}", null, Context.NONE);
    }
}
```

### Pools_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devcenter.models.Pool;

/** Samples for Pools Update. */
public final class PoolsUpdateSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/Pools_Patch.json
     */
    /**
     * Sample code: Pools_Update.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void poolsUpdate(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        Pool resource = manager.pools().getWithResponse("rg1", "{projectName}", "{poolName}", Context.NONE).getValue();
        resource.update().withDevBoxDefinitionName("WebDevBox2").apply();
    }
}
```

### ProjectEnvironmentTypes_CreateOrUpdate

```java
import com.azure.resourcemanager.devcenter.models.EnableStatus;
import com.azure.resourcemanager.devcenter.models.EnvironmentRole;
import com.azure.resourcemanager.devcenter.models.ManagedServiceIdentity;
import com.azure.resourcemanager.devcenter.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.devcenter.models.ProjectEnvironmentTypeUpdatePropertiesCreatorRoleAssignment;
import com.azure.resourcemanager.devcenter.models.UserAssignedIdentity;
import com.azure.resourcemanager.devcenter.models.UserRoleAssignmentValue;
import java.util.HashMap;
import java.util.Map;

/** Samples for ProjectEnvironmentTypes CreateOrUpdate. */
public final class ProjectEnvironmentTypesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/ProjectEnvironmentTypes_Put.json
     */
    /**
     * Sample code: ProjectEnvironmentTypes_CreateOrUpdate.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void projectEnvironmentTypesCreateOrUpdate(
        com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager
            .projectEnvironmentTypes()
            .define("{environmentTypeName}")
            .withExistingProject("rg1", "ContosoProj")
            .withTags(mapOf("CostCenter", "RnD"))
            .withIdentity(
                new ManagedServiceIdentity()
                    .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/identityGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/testidentity1",
                            new UserAssignedIdentity())))
            .withDeploymentTargetId("/subscriptions/00000000-0000-0000-0000-000000000000")
            .withStatus(EnableStatus.ENABLED)
            .withCreatorRoleAssignment(
                new ProjectEnvironmentTypeUpdatePropertiesCreatorRoleAssignment()
                    .withRoles(mapOf("4cbf0b6c-e750-441c-98a7-10da8387e4d6", new EnvironmentRole())))
            .withUserRoleAssignments(
                mapOf(
                    "e45e3m7c-176e-416a-b466-0c5ec8298f8a",
                    new UserRoleAssignmentValue()
                        .withRoles(mapOf("4cbf0b6c-e750-441c-98a7-10da8387e4d6", new EnvironmentRole()))))
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

### ProjectEnvironmentTypes_Delete

```java
import com.azure.core.util.Context;

/** Samples for ProjectEnvironmentTypes Delete. */
public final class ProjectEnvironmentTypesDeleteSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/ProjectEnvironmentTypes_Delete.json
     */
    /**
     * Sample code: ProjectEnvironmentTypes_Delete.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void projectEnvironmentTypesDelete(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager
            .projectEnvironmentTypes()
            .deleteWithResponse("rg1", "ContosoProj", "{environmentTypeName}", Context.NONE);
    }
}
```

### ProjectEnvironmentTypes_Get

```java
import com.azure.core.util.Context;

/** Samples for ProjectEnvironmentTypes Get. */
public final class ProjectEnvironmentTypesGetSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/ProjectEnvironmentTypes_Get.json
     */
    /**
     * Sample code: ProjectEnvironmentTypes_Get.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void projectEnvironmentTypesGet(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.projectEnvironmentTypes().getWithResponse("rg1", "ContosoProj", "{environmentTypeName}", Context.NONE);
    }
}
```

### ProjectEnvironmentTypes_List

```java
import com.azure.core.util.Context;

/** Samples for ProjectEnvironmentTypes List. */
public final class ProjectEnvironmentTypesListSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/ProjectEnvironmentTypes_List.json
     */
    /**
     * Sample code: ProjectEnvironmentTypes_List.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void projectEnvironmentTypesList(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.projectEnvironmentTypes().list("rg1", "ContosoProj", null, Context.NONE);
    }
}
```

### ProjectEnvironmentTypes_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devcenter.models.EnableStatus;
import com.azure.resourcemanager.devcenter.models.EnvironmentRole;
import com.azure.resourcemanager.devcenter.models.ManagedServiceIdentity;
import com.azure.resourcemanager.devcenter.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.devcenter.models.ProjectEnvironmentType;
import com.azure.resourcemanager.devcenter.models.UserAssignedIdentity;
import com.azure.resourcemanager.devcenter.models.UserRoleAssignmentValue;
import java.util.HashMap;
import java.util.Map;

/** Samples for ProjectEnvironmentTypes Update. */
public final class ProjectEnvironmentTypesUpdateSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/ProjectEnvironmentTypes_Patch.json
     */
    /**
     * Sample code: ProjectEnvironmentTypes_Update.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void projectEnvironmentTypesUpdate(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        ProjectEnvironmentType resource =
            manager
                .projectEnvironmentTypes()
                .getWithResponse("rg1", "ContosoProj", "{environmentTypeName}", Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("CostCenter", "RnD"))
            .withIdentity(
                new ManagedServiceIdentity()
                    .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/identityGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/testidentity1",
                            new UserAssignedIdentity())))
            .withDeploymentTargetId("/subscriptions/00000000-0000-0000-0000-000000000000")
            .withStatus(EnableStatus.ENABLED)
            .withUserRoleAssignments(
                mapOf(
                    "e45e3m7c-176e-416a-b466-0c5ec8298f8a",
                    new UserRoleAssignmentValue()
                        .withRoles(mapOf("4cbf0b6c-e750-441c-98a7-10da8387e4d6", new EnvironmentRole()))))
            .apply();
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

### Projects_CreateOrUpdate

```java
import java.util.HashMap;
import java.util.Map;

/** Samples for Projects CreateOrUpdate. */
public final class ProjectsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/Projects_Put.json
     */
    /**
     * Sample code: Projects_CreateOrUpdate.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void projectsCreateOrUpdate(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager
            .projects()
            .define("{projectName}")
            .withRegion("centralus")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("CostCenter", "R&D"))
            .withDevCenterId(
                "/subscriptions/{subscriptionId}/resourceGroups/rg1/providers/Microsoft.DevCenter/devcenters/{devCenterName}")
            .withDescription("This is my first project.")
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

### Projects_Delete

```java
import com.azure.core.util.Context;

/** Samples for Projects Delete. */
public final class ProjectsDeleteSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/Projects_Delete.json
     */
    /**
     * Sample code: Projects_Delete.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void projectsDelete(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.projects().delete("rg1", "{projectName}", Context.NONE);
    }
}
```

### Projects_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Projects GetByResourceGroup. */
public final class ProjectsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/Projects_Get.json
     */
    /**
     * Sample code: Projects_Get.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void projectsGet(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.projects().getByResourceGroupWithResponse("rg1", "{projectName}", Context.NONE);
    }
}
```

### Projects_List

```java
import com.azure.core.util.Context;

/** Samples for Projects List. */
public final class ProjectsListSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/Projects_ListBySubscription.json
     */
    /**
     * Sample code: Projects_ListBySubscription.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void projectsListBySubscription(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.projects().list(null, Context.NONE);
    }
}
```

### Projects_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Projects ListByResourceGroup. */
public final class ProjectsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/Projects_ListByResourceGroup.json
     */
    /**
     * Sample code: Projects_ListByResourceGroup.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void projectsListByResourceGroup(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.projects().listByResourceGroup("rg1", null, Context.NONE);
    }
}
```

### Projects_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devcenter.models.Project;
import java.util.HashMap;
import java.util.Map;

/** Samples for Projects Update. */
public final class ProjectsUpdateSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/Projects_Patch.json
     */
    /**
     * Sample code: Projects_Update.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void projectsUpdate(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        Project resource =
            manager.projects().getByResourceGroupWithResponse("rg1", "{projectName}", Context.NONE).getValue();
        resource.update().withTags(mapOf("CostCenter", "R&D")).withDescription("This is my first project.").apply();
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

### Schedules_CreateOrUpdate

```java
import com.azure.resourcemanager.devcenter.models.EnableStatus;
import com.azure.resourcemanager.devcenter.models.ScheduledFrequency;
import com.azure.resourcemanager.devcenter.models.ScheduledType;

/** Samples for Schedules CreateOrUpdate. */
public final class SchedulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/Schedules_CreateDailyShutdownPoolSchedule.json
     */
    /**
     * Sample code: Schedules_CreateDailyShutdownPoolSchedule.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void schedulesCreateDailyShutdownPoolSchedule(
        com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager
            .schedules()
            .define("autoShutdown")
            .withExistingPool("rg1", "DevProject", "DevPool")
            .withTypePropertiesType(ScheduledType.STOP_DEV_BOX)
            .withFrequency(ScheduledFrequency.DAILY)
            .withTime("17:30")
            .withTimeZone("America/Los_Angeles")
            .withState(EnableStatus.ENABLED)
            .create();
    }
}
```

### Schedules_Delete

```java
import com.azure.core.util.Context;

/** Samples for Schedules Delete. */
public final class SchedulesDeleteSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/Schedules_Delete.json
     */
    /**
     * Sample code: Schedules_Delete.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void schedulesDelete(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.schedules().delete("rg1", "TestProject", "DevPool", "autoShutdown", null, Context.NONE);
    }
}
```

### Schedules_Get

```java
import com.azure.core.util.Context;

/** Samples for Schedules Get. */
public final class SchedulesGetSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/Schedules_Get.json
     */
    /**
     * Sample code: Schedules_GetByPool.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void schedulesGetByPool(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.schedules().getWithResponse("rg1", "TestProject", "DevPool", "autoShutdown", null, Context.NONE);
    }
}
```

### Schedules_ListByPool

```java
import com.azure.core.util.Context;

/** Samples for Schedules ListByPool. */
public final class SchedulesListByPoolSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/Schedules_ListByPool.json
     */
    /**
     * Sample code: Schedules_ListByPool.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void schedulesListByPool(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.schedules().listByPool("rg1", "TestProject", "DevPool", null, Context.NONE);
    }
}
```

### Schedules_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devcenter.models.ScheduleUpdate;

/** Samples for Schedules Update. */
public final class SchedulesUpdateSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/Schedules_Patch.json
     */
    /**
     * Sample code: Schedules_Update.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void schedulesUpdate(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager
            .schedules()
            .update(
                "rg1",
                "TestProject",
                "DevPool",
                "autoShutdown",
                new ScheduleUpdate().withTime("18:00"),
                null,
                Context.NONE);
    }
}
```

### Skus_List

```java
import com.azure.core.util.Context;

/** Samples for Skus List. */
public final class SkusListSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/Skus_ListBySubscription.json
     */
    /**
     * Sample code: Skus_ListBySubscription.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void skusListBySubscription(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.skus().list(null, Context.NONE);
    }
}
```

### Usages_ListByLocation

```java
import com.azure.core.util.Context;

/** Samples for Usages ListByLocation. */
public final class UsagesListByLocationSamples {
    /*
     * x-ms-original-file: specification/devcenter/resource-manager/Microsoft.DevCenter/preview/2022-08-01-preview/examples/Usages_ListByLocation.json
     */
    /**
     * Sample code: listUsages.
     *
     * @param manager Entry point to DevCenterManager.
     */
    public static void listUsages(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.usages().listByLocation("westus", Context.NONE);
    }
}
```


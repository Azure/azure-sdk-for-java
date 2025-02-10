# Code snippets and samples


## ErrorSummaries

- [Get](#errorsummaries_get)
- [ListBySite](#errorsummaries_listbysite)

## Operations

- [List](#operations_list)

## Springbootapps

- [Get](#springbootapps_get)
- [ListByResourceGroup](#springbootapps_listbyresourcegroup)
- [ListBySubscription](#springbootapps_listbysubscription)
- [Update](#springbootapps_update)

## Springbootservers

- [CreateOrUpdate](#springbootservers_createorupdate)
- [Delete](#springbootservers_delete)
- [Get](#springbootservers_get)
- [ListByResourceGroup](#springbootservers_listbyresourcegroup)
- [ListBySubscription](#springbootservers_listbysubscription)
- [Update](#springbootservers_update)

## Springbootsites

- [CreateOrUpdate](#springbootsites_createorupdate)
- [Delete](#springbootsites_delete)
- [GetByResourceGroup](#springbootsites_getbyresourcegroup)
- [List](#springbootsites_list)
- [ListByResourceGroup](#springbootsites_listbyresourcegroup)
- [TriggerRefreshSite](#springbootsites_triggerrefreshsite)
- [Update](#springbootsites_update)

## Summaries

- [Get](#summaries_get)
- [ListBySite](#summaries_listbysite)
### ErrorSummaries_Get

```java
/**
 * Samples for ErrorSummaries Get.
 */
public final class ErrorSummariesGetSamples {
    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/ErrorSummaries_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: ErrorSummaries_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void
        errorSummariesGetMaximumSetGen(com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        manager.errorSummaries()
            .getWithResponse("rgspringbootdiscovery", "xxkzlvbihwxunadjcpjpjmghmhxrqyvghtpfps", "K2lv",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/ErrorSummaries_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: ErrorSummaries_Get_MinimumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void
        errorSummariesGetMinimumSetGen(com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        manager.errorSummaries()
            .getWithResponse("rgspringbootdiscovery", "xxkzlvbihwxunadjcpjpjmghmhxrqyvghtpfps", "K2lv",
                com.azure.core.util.Context.NONE);
    }
}
```

### ErrorSummaries_ListBySite

```java
/**
 * Samples for ErrorSummaries ListBySite.
 */
public final class ErrorSummariesListBySiteSamples {
    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/ErrorSummaries_ListBySite_MaximumSet_Gen.json
     */
    /**
     * Sample code: ErrorSummaries_ListBySite_MaximumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void errorSummariesListBySiteMaximumSetGen(
        com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        manager.errorSummaries()
            .listBySite("rgspringbootdiscovery", "xxkzlvbihwxunadjcpjpjmghmhxrqyvghtpfps",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/ErrorSummaries_ListBySite_MinimumSet_Gen.json
     */
    /**
     * Sample code: ErrorSummaries_ListBySite_MinimumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void errorSummariesListBySiteMinimumSetGen(
        com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        manager.errorSummaries()
            .listBySite("rgspringbootdiscovery", "xxkzlvbihwxunadjcpjpjmghmhxrqyvghtpfps",
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/Operations_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MinimumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void
        operationsListMinimumSetGen(com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/Operations_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MaximumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void
        operationsListMaximumSetGen(com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Springbootapps_Get

```java
/**
 * Samples for Springbootapps Get.
 */
public final class SpringbootappsGetSamples {
    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/springbootapps_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: springbootapps_Get_MinimumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void
        springbootappsGetMinimumSetGen(com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        manager.springbootapps()
            .getWithResponse("rgspringbootapps", "pdfosfhtemfsaglvwjdyqlyeipucrd",
                "ofjeesoahqtnovlbuvflyknpbhcpeqqhekntvqxyemuwbcqnuxjgfhsf", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/springbootapps_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: springbootapps_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void
        springbootappsGetMaximumSetGen(com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        manager.springbootapps()
            .getWithResponse("rgspringbootapps", "pdfosfhtemfsaglvwjdyqlyeipucrd",
                "ofjeesoahqtnovlbuvflyknpbhcpeqqhekntvqxyemuwbcqnuxjgfhsf", com.azure.core.util.Context.NONE);
    }
}
```

### Springbootapps_ListByResourceGroup

```java
/**
 * Samples for Springbootapps ListByResourceGroup.
 */
public final class SpringbootappsListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/springbootapps_ListByResourceGroup_MinimumSet_Gen.json
     */
    /**
     * Sample code: springbootapps_ListByResourceGroup_MinimumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void springbootappsListByResourceGroupMinimumSetGen(
        com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        manager.springbootapps()
            .listByResourceGroup("rgspringbootapps", "pdfosfhtemfsaglvwjdyqlyeipucrd",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/springbootapps_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: springbootapps_ListByResourceGroup_MaximumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void springbootappsListByResourceGroupMaximumSetGen(
        com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        manager.springbootapps()
            .listByResourceGroup("rgspringbootapps", "pdfosfhtemfsaglvwjdyqlyeipucrd",
                com.azure.core.util.Context.NONE);
    }
}
```

### Springbootapps_ListBySubscription

```java
/**
 * Samples for Springbootapps ListBySubscription.
 */
public final class SpringbootappsListBySubscriptionSamples {
    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/springbootapps_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: springbootapps_ListBySubscription_MaximumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void springbootappsListBySubscriptionMaximumSetGen(
        com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        manager.springbootapps().listBySubscription("pdfosfhtemfsaglvwjdyqlyeipucrd", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/springbootapps_ListBySubscription_MinimumSet_Gen.json
     */
    /**
     * Sample code: springbootapps_ListBySubscription_MinimumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void springbootappsListBySubscriptionMinimumSetGen(
        com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        manager.springbootapps().listBySubscription("pdfosfhtemfsaglvwjdyqlyeipucrd", com.azure.core.util.Context.NONE);
    }
}
```

### Springbootapps_Update

```java
import com.azure.resourcemanager.springappdiscovery.models.SpringbootappsPatch;

/**
 * Samples for Springbootapps Update.
 */
public final class SpringbootappsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/springbootapps_Update_MinimumSet_Gen.json
     */
    /**
     * Sample code: springbootapps_Update_MinimumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void springbootappsUpdateMinimumSetGen(
        com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        manager.springbootapps()
            .update("rgspringbootapps", "pdfosfhtemfsaglvwjdyqlyeipucrd",
                "ofjeesoahqtnovlbuvflyknpbhcpeqqhekntvqxyemuwbcqnuxjgfhsf", new SpringbootappsPatch(),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/springbootapps_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: springbootapps_Update_MaximumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void springbootappsUpdateMaximumSetGen(
        com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        manager.springbootapps()
            .update("rgspringbootapps", "pdfosfhtemfsaglvwjdyqlyeipucrd",
                "ofjeesoahqtnovlbuvflyknpbhcpeqqhekntvqxyemuwbcqnuxjgfhsf", new SpringbootappsPatch(),
                com.azure.core.util.Context.NONE);
    }
}
```

### Springbootservers_CreateOrUpdate

```java
import com.azure.resourcemanager.springappdiscovery.models.SpringbootserversProperties;
import java.util.Arrays;

/**
 * Samples for Springbootservers CreateOrUpdate.
 */
public final class SpringbootserversCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/springbootservers_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: springbootservers_CreateOrUpdate_MaximumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void springbootserversCreateOrUpdateMaximumSetGen(
        com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        manager.springbootservers()
            .define("zkarbqnwnxeozvjrkpdqmgnwedwgtwcmmyqwaijkn")
            .withExistingSpringbootsite("rgspringbootservers",
                "hlkrzldhyobavtabgpubtjbhlslnjmsvkthwcfboriwyxndacjypzbj")
            .withProperties(new SpringbootserversProperties().withPort(10)
                .withServer("thhuxocfyqpeluqcgnypi")
                .withFqdnAndIpAddressList(Arrays.asList())
                .withMachineArmId("fvfkiapbqsprnbzczdfmuryknrna")
                .withTotalApps(5)
                .withSpringBootApps(17)
                .withErrors(Arrays.asList()))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/springbootservers_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: springbootservers_CreateOrUpdate_MinimumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void springbootserversCreateOrUpdateMinimumSetGen(
        com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        manager.springbootservers()
            .define("zkarbqnwnxeozvjrkpdqmgnwedwgtwcmmyqwaijkn")
            .withExistingSpringbootsite("rgspringbootservers",
                "hlkrzldhyobavtabgpubtjbhlslnjmsvkthwcfboriwyxndacjypzbj")
            .create();
    }
}
```

### Springbootservers_Delete

```java
/**
 * Samples for Springbootservers Delete.
 */
public final class SpringbootserversDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/springbootservers_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: springbootservers_Delete_MinimumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void springbootserversDeleteMinimumSetGen(
        com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        manager.springbootservers()
            .delete("rgspringbootservers", "hlkrzldhyobavtabgpubtjbhlslnjmsvkthwcfboriwyxndacjypzbj",
                "zkarbqnwnxeozvjrkpdqmgnwedwgtwcmmyqwaijkn", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/springbootservers_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: springbootservers_Delete_MaximumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void springbootserversDeleteMaximumSetGen(
        com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        manager.springbootservers()
            .delete("rgspringbootservers", "hlkrzldhyobavtabgpubtjbhlslnjmsvkthwcfboriwyxndacjypzbj",
                "zkarbqnwnxeozvjrkpdqmgnwedwgtwcmmyqwaijkn", com.azure.core.util.Context.NONE);
    }
}
```

### Springbootservers_Get

```java
/**
 * Samples for Springbootservers Get.
 */
public final class SpringbootserversGetSamples {
    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/springbootservers_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: springbootservers_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void springbootserversGetMaximumSetGen(
        com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        manager.springbootservers()
            .getWithResponse("rgspringbootservers", "hlkrzldhyobavtabgpubtjbhlslnjmsvkthwcfboriwyxndacjypzbj",
                "zkarbqnwnxeozvjrkpdqmgnwedwgtwcmmyqwaijkn", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/springbootservers_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: springbootservers_Get_MinimumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void springbootserversGetMinimumSetGen(
        com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        manager.springbootservers()
            .getWithResponse("rgspringbootservers", "hlkrzldhyobavtabgpubtjbhlslnjmsvkthwcfboriwyxndacjypzbj",
                "zkarbqnwnxeozvjrkpdqmgnwedwgtwcmmyqwaijkn", com.azure.core.util.Context.NONE);
    }
}
```

### Springbootservers_ListByResourceGroup

```java
/**
 * Samples for Springbootservers ListByResourceGroup.
 */
public final class SpringbootserversListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/springbootservers_ListByResourceGroup_MinimumSet_Gen.json
     */
    /**
     * Sample code: springbootservers_ListByResourceGroup_MinimumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void springbootserversListByResourceGroupMinimumSetGen(
        com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        manager.springbootservers()
            .listByResourceGroup("rgspringbootservers", "hlkrzldhyobavtabgpubtjbhlslnjmsvkthwcfboriwyxndacjypzbj",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/springbootservers_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: springbootservers_ListByResourceGroup_MaximumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void springbootserversListByResourceGroupMaximumSetGen(
        com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        manager.springbootservers()
            .listByResourceGroup("rgspringbootservers", "hlkrzldhyobavtabgpubtjbhlslnjmsvkthwcfboriwyxndacjypzbj",
                com.azure.core.util.Context.NONE);
    }
}
```

### Springbootservers_ListBySubscription

```java
/**
 * Samples for Springbootservers ListBySubscription.
 */
public final class SpringbootserversListBySubscriptionSamples {
    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/springbootservers_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: springbootservers_ListBySubscription_MaximumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void springbootserversListBySubscriptionMaximumSetGen(
        com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        manager.springbootservers()
            .listBySubscription("hlkrzldhyobavtabgpubtjbhlslnjmsvkthwcfboriwyxndacjypzbj",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/springbootservers_ListBySubscription_MinimumSet_Gen.json
     */
    /**
     * Sample code: springbootservers_ListBySubscription_MinimumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void springbootserversListBySubscriptionMinimumSetGen(
        com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        manager.springbootservers()
            .listBySubscription("hlkrzldhyobavtabgpubtjbhlslnjmsvkthwcfboriwyxndacjypzbj",
                com.azure.core.util.Context.NONE);
    }
}
```

### Springbootservers_Update

```java
import com.azure.resourcemanager.springappdiscovery.models.SpringbootserversModel;

/**
 * Samples for Springbootservers Update.
 */
public final class SpringbootserversUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/springbootservers_Update_MinimumSet_Gen.json
     */
    /**
     * Sample code: springbootservers_Update_MinimumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void springbootserversUpdateMinimumSetGen(
        com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        SpringbootserversModel resource = manager.springbootservers()
            .getWithResponse("rgspringbootservers", "hlkrzldhyobavtabgpubtjbhlslnjmsvkthwcfboriwyxndacjypzbj",
                "zkarbqnwnxeozvjrkpdqmgnwedwgtwcmmyqwaijkn", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().apply();
    }

    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/springbootservers_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: springbootservers_Update_MaximumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void springbootserversUpdateMaximumSetGen(
        com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        SpringbootserversModel resource = manager.springbootservers()
            .getWithResponse("rgspringbootservers", "hlkrzldhyobavtabgpubtjbhlslnjmsvkthwcfboriwyxndacjypzbj",
                "zkarbqnwnxeozvjrkpdqmgnwedwgtwcmmyqwaijkn", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().apply();
    }
}
```

### Springbootsites_CreateOrUpdate

```java
import com.azure.resourcemanager.springappdiscovery.models.SpringbootsitesModelExtendedLocation;
import com.azure.resourcemanager.springappdiscovery.models.SpringbootsitesProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Springbootsites CreateOrUpdate.
 */
public final class SpringbootsitesCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/springbootsites_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: springbootsites_CreateOrUpdate_MaximumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void springbootsitesCreateOrUpdateMaximumSetGen(
        com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        manager.springbootsites()
            .define("xrmzlavpewxtfeitghdrj")
            .withRegion("tgobtvxktootwhhvjtsmpddvlqlrq")
            .withExistingResourceGroup("rgspringbootsites")
            .withTags(mapOf("key3558", "fakeTokenPlaceholder"))
            .withProperties(new SpringbootsitesProperties().withMasterSiteId("xsoimrgshsactearljwuljmi")
                .withMigrateProjectId("wwuattybgco"))
            .withExtendedLocation(
                new SpringbootsitesModelExtendedLocation().withType("lvsb").withName("rywvpbfsqovhlfirtwisugsdsfsgf"))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/springbootsites_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: springbootsites_CreateOrUpdate_MinimumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void springbootsitesCreateOrUpdateMinimumSetGen(
        com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        manager.springbootsites()
            .define("xrmzlavpewxtfeitghdrj")
            .withRegion("tgobtvxktootwhhvjtsmpddvlqlrq")
            .withExistingResourceGroup("rgspringbootsites")
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

### Springbootsites_Delete

```java
/**
 * Samples for Springbootsites Delete.
 */
public final class SpringbootsitesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/springbootsites_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: springbootsites_Delete_MaximumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void springbootsitesDeleteMaximumSetGen(
        com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        manager.springbootsites()
            .delete("rgspringbootsites", "xrmzlavpewxtfeitghdrj", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/springbootsites_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: springbootsites_Delete_MinimumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void springbootsitesDeleteMinimumSetGen(
        com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        manager.springbootsites()
            .delete("rgspringbootsites", "xrmzlavpewxtfeitghdrj", com.azure.core.util.Context.NONE);
    }
}
```

### Springbootsites_GetByResourceGroup

```java
/**
 * Samples for Springbootsites GetByResourceGroup.
 */
public final class SpringbootsitesGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/springbootsites_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: springbootsites_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void springbootsitesGetMaximumSetGen(
        com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        manager.springbootsites()
            .getByResourceGroupWithResponse("rgspringbootsites", "xrmzlavpewxtfeitghdrj",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/springbootsites_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: springbootsites_Get_MinimumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void springbootsitesGetMinimumSetGen(
        com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        manager.springbootsites()
            .getByResourceGroupWithResponse("rgspringbootsites", "xrmzlavpewxtfeitghdrj",
                com.azure.core.util.Context.NONE);
    }
}
```

### Springbootsites_List

```java
/**
 * Samples for Springbootsites List.
 */
public final class SpringbootsitesListSamples {
    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/springbootsites_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: springbootsites_ListBySubscription_MaximumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void springbootsitesListBySubscriptionMaximumSetGen(
        com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        manager.springbootsites().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/springbootsites_ListBySubscription_MinimumSet_Gen.json
     */
    /**
     * Sample code: springbootsites_ListBySubscription_MinimumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void springbootsitesListBySubscriptionMinimumSetGen(
        com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        manager.springbootsites().list(com.azure.core.util.Context.NONE);
    }
}
```

### Springbootsites_ListByResourceGroup

```java
/**
 * Samples for Springbootsites ListByResourceGroup.
 */
public final class SpringbootsitesListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/springbootsites_ListByResourceGroup_MinimumSet_Gen.json
     */
    /**
     * Sample code: springbootsites_ListByResourceGroup_MinimumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void springbootsitesListByResourceGroupMinimumSetGen(
        com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        manager.springbootsites().listByResourceGroup("rgspringbootsites", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/springbootsites_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: springbootsites_ListByResourceGroup_MaximumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void springbootsitesListByResourceGroupMaximumSetGen(
        com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        manager.springbootsites().listByResourceGroup("rgspringbootsites", com.azure.core.util.Context.NONE);
    }
}
```

### Springbootsites_TriggerRefreshSite

```java
/**
 * Samples for Springbootsites TriggerRefreshSite.
 */
public final class SpringbootsitesTriggerRefreshSiteSamples {
    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/springbootsites_TriggerRefreshSite_MinimumSet_Gen.json
     */
    /**
     * Sample code: springbootsites_TriggerRefreshSite_MinimumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void springbootsitesTriggerRefreshSiteMinimumSetGen(
        com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        manager.springbootsites()
            .triggerRefreshSite("rgspringbootsites", "czarpuxwoafaqsuptutcwyu", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/springbootsites_TriggerRefreshSite_MaximumSet_Gen.json
     */
    /**
     * Sample code: springbootsites_TriggerRefreshSite_MaximumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void springbootsitesTriggerRefreshSiteMaximumSetGen(
        com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        manager.springbootsites()
            .triggerRefreshSite("rgspringbootsites", "czarpuxwoafaqsuptutcwyu", com.azure.core.util.Context.NONE);
    }
}
```

### Springbootsites_Update

```java
import com.azure.resourcemanager.springappdiscovery.models.SpringbootsitesModel;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Springbootsites Update.
 */
public final class SpringbootsitesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/springbootsites_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: springbootsites_Update_MaximumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void springbootsitesUpdateMaximumSetGen(
        com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        SpringbootsitesModel resource = manager.springbootsites()
            .getByResourceGroupWithResponse("rgspringbootsites", "xrmzlavpewxtfeitghdrj",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("key9581", "fakeTokenPlaceholder")).apply();
    }

    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/springbootsites_Update_MinimumSet_Gen.json
     */
    /**
     * Sample code: springbootsites_Update_MinimumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void springbootsitesUpdateMinimumSetGen(
        com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        SpringbootsitesModel resource = manager.springbootsites()
            .getByResourceGroupWithResponse("rgspringbootsites", "xrmzlavpewxtfeitghdrj",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().apply();
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

### Summaries_Get

```java
/**
 * Samples for Summaries Get.
 */
public final class SummariesGetSamples {
    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/Summaries_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Summaries_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void
        summariesGetMaximumSetGen(com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        manager.summaries()
            .getWithResponse("rgspringbootdiscovery", "xxkzlvbihwxunadjcpjpjmghmhxrqyvghtpfps", "vjB",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/Summaries_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: Summaries_Get_MinimumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void
        summariesGetMinimumSetGen(com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        manager.summaries()
            .getWithResponse("rgspringbootdiscovery", "xxkzlvbihwxunadjcpjpjmghmhxrqyvghtpfps", "vjB",
                com.azure.core.util.Context.NONE);
    }
}
```

### Summaries_ListBySite

```java
/**
 * Samples for Summaries ListBySite.
 */
public final class SummariesListBySiteSamples {
    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/Summaries_ListBySite_MaximumSet_Gen.json
     */
    /**
     * Sample code: Summaries_ListBySite_MaximumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void summariesListBySiteMaximumSetGen(
        com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        manager.summaries()
            .listBySite("rgspringbootdiscovery", "xxkzlvbihwxunadjcpjpjmghmhxrqyvghtpfps",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/offazurespringboot/resource-manager/Microsoft.OffAzureSpringBoot/preview/2023-01-01-preview/
     * examples/Summaries_ListBySite_MinimumSet_Gen.json
     */
    /**
     * Sample code: Summaries_ListBySite_MinimumSet_Gen.
     * 
     * @param manager Entry point to SpringAppDiscoveryManager.
     */
    public static void summariesListBySiteMinimumSetGen(
        com.azure.resourcemanager.springappdiscovery.SpringAppDiscoveryManager manager) {
        manager.summaries()
            .listBySite("rgspringbootdiscovery", "xxkzlvbihwxunadjcpjpjmghmhxrqyvghtpfps",
                com.azure.core.util.Context.NONE);
    }
}
```


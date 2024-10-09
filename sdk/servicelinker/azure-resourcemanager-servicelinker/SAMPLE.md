# Code snippets and samples


## ConfigurationNamesOperation

- [List](#configurationnamesoperation_list)

## Connector

- [CreateDryrun](#connector_createdryrun)
- [CreateOrUpdate](#connector_createorupdate)
- [Delete](#connector_delete)
- [DeleteDryrun](#connector_deletedryrun)
- [GenerateConfigurations](#connector_generateconfigurations)
- [Get](#connector_get)
- [GetDryrun](#connector_getdryrun)
- [List](#connector_list)
- [ListDryrun](#connector_listdryrun)
- [Update](#connector_update)
- [UpdateDryrun](#connector_updatedryrun)
- [Validate](#connector_validate)

## Linker

- [CreateOrUpdate](#linker_createorupdate)
- [Delete](#linker_delete)
- [Get](#linker_get)
- [List](#linker_list)
- [ListConfigurations](#linker_listconfigurations)
- [Update](#linker_update)
- [Validate](#linker_validate)

## LinkersOperation

- [CreateDryrun](#linkersoperation_createdryrun)
- [DeleteDryrun](#linkersoperation_deletedryrun)
- [GenerateConfigurations](#linkersoperation_generateconfigurations)
- [GetDryrun](#linkersoperation_getdryrun)
- [ListDaprConfigurations](#linkersoperation_listdaprconfigurations)
- [ListDryrun](#linkersoperation_listdryrun)
- [UpdateDryrun](#linkersoperation_updatedryrun)

## Operations

- [List](#operations_list)
### ConfigurationNamesOperation_List

```java
/**
 * Samples for ConfigurationNamesOperation List.
 */
public final class ConfigurationNamesOperationListSamples {
    /*
     * x-ms-original-file:
     * specification/servicelinker/resource-manager/Microsoft.ServiceLinker/preview/2024-07-01-preview/examples/
     * ConfigurationNamesList.json
     */
    /**
     * Sample code: GetConfigurationNames.
     * 
     * @param manager Entry point to ServiceLinkerManager.
     */
    public static void getConfigurationNames(com.azure.resourcemanager.servicelinker.ServiceLinkerManager manager) {
        manager.configurationNamesOperations().list(null, null, com.azure.core.util.Context.NONE);
    }
}
```

### Connector_CreateDryrun

```java
import com.azure.resourcemanager.servicelinker.models.AzureResource;
import com.azure.resourcemanager.servicelinker.models.CreateOrUpdateDryrunParameters;
import com.azure.resourcemanager.servicelinker.models.SecretAuthInfo;
import com.azure.resourcemanager.servicelinker.models.ValueSecretInfo;

/**
 * Samples for Connector CreateDryrun.
 */
public final class ConnectorCreateDryrunSamples {
    /*
     * x-ms-original-file:
     * specification/servicelinker/resource-manager/Microsoft.ServiceLinker/preview/2024-07-01-preview/examples/
     * ConnectorDryrunCreate.json
     */
    /**
     * Sample code: ConnectorDryrunCreate.
     * 
     * @param manager Entry point to ServiceLinkerManager.
     */
    public static void connectorDryrunCreate(com.azure.resourcemanager.servicelinker.ServiceLinkerManager manager) {
        manager.connectors()
            .define("dryrunName")
            .withExistingLocation("00000000-0000-0000-0000-000000000000", "test-rg", "westus")
            .withParameters(new CreateOrUpdateDryrunParameters().withTargetService(new AzureResource().withId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.DocumentDb/databaseAccounts/test-acc/mongodbDatabases/test-db"))
                .withAuthInfo(
                    new SecretAuthInfo().withName("name").withSecretInfo(new ValueSecretInfo().withValue("secret"))))
            .create();
    }
}
```

### Connector_CreateOrUpdate

```java
import com.azure.resourcemanager.servicelinker.fluent.models.LinkerResourceInner;
import com.azure.resourcemanager.servicelinker.models.AzureResource;
import com.azure.resourcemanager.servicelinker.models.SecretAuthInfo;
import com.azure.resourcemanager.servicelinker.models.SecretStore;

/**
 * Samples for Connector CreateOrUpdate.
 */
public final class ConnectorCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/servicelinker/resource-manager/Microsoft.ServiceLinker/preview/2024-07-01-preview/examples/
     * PutConnector.json
     */
    /**
     * Sample code: PutConnector.
     * 
     * @param manager Entry point to ServiceLinkerManager.
     */
    public static void putConnector(com.azure.resourcemanager.servicelinker.ServiceLinkerManager manager) {
        manager.connectors()
            .createOrUpdate("00000000-0000-0000-0000-000000000000", "test-rg", "westus", "connectorName",
                new LinkerResourceInner().withTargetService(new AzureResource().withId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.DocumentDb/databaseAccounts/test-acc/mongodbDatabases/test-db"))
                    .withAuthInfo(new SecretAuthInfo())
                    .withSecretStore(new SecretStore().withKeyVaultId("fakeTokenPlaceholder")),
                com.azure.core.util.Context.NONE);
    }
}
```

### Connector_Delete

```java
/**
 * Samples for Connector Delete.
 */
public final class ConnectorDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/servicelinker/resource-manager/Microsoft.ServiceLinker/preview/2024-07-01-preview/examples/
     * DeleteConnector.json
     */
    /**
     * Sample code: DeleteConnector.
     * 
     * @param manager Entry point to ServiceLinkerManager.
     */
    public static void deleteConnector(com.azure.resourcemanager.servicelinker.ServiceLinkerManager manager) {
        manager.connectors()
            .delete("00000000-0000-0000-0000-000000000000", "test-rg", "westus", "connectorName",
                com.azure.core.util.Context.NONE);
    }
}
```

### Connector_DeleteDryrun

```java
/**
 * Samples for Connector DeleteDryrun.
 */
public final class ConnectorDeleteDryrunSamples {
    /*
     * x-ms-original-file:
     * specification/servicelinker/resource-manager/Microsoft.ServiceLinker/preview/2024-07-01-preview/examples/
     * ConnectorDryrunDelete.json
     */
    /**
     * Sample code: ConnectorDryrunDelete.
     * 
     * @param manager Entry point to ServiceLinkerManager.
     */
    public static void connectorDryrunDelete(com.azure.resourcemanager.servicelinker.ServiceLinkerManager manager) {
        manager.connectors()
            .deleteDryrunWithResponse("00000000-0000-0000-0000-000000000000", "test-rg", "westus", "dryrunName",
                com.azure.core.util.Context.NONE);
    }
}
```

### Connector_GenerateConfigurations

```java
import com.azure.resourcemanager.servicelinker.models.ConfigurationInfo;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Connector GenerateConfigurations.
 */
public final class ConnectorGenerateConfigurationsSamples {
    /*
     * x-ms-original-file:
     * specification/servicelinker/resource-manager/Microsoft.ServiceLinker/preview/2024-07-01-preview/examples/
     * GenerateConfigurations.json
     */
    /**
     * Sample code: GenerateConfiguration.
     * 
     * @param manager Entry point to ServiceLinkerManager.
     */
    public static void generateConfiguration(com.azure.resourcemanager.servicelinker.ServiceLinkerManager manager) {
        manager.connectors()
            .generateConfigurationsWithResponse(
                "00000000-0000-0000-0000-000000000000", "test-rg", "westus", "connectorName", new ConfigurationInfo()
                    .withCustomizedKeys(mapOf("ASL_DocumentDb_ConnectionString", "MyConnectionstring")),
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

### Connector_Get

```java
/**
 * Samples for Connector Get.
 */
public final class ConnectorGetSamples {
    /*
     * x-ms-original-file:
     * specification/servicelinker/resource-manager/Microsoft.ServiceLinker/preview/2024-07-01-preview/examples/
     * Connectors.json
     */
    /**
     * Sample code: Connector.
     * 
     * @param manager Entry point to ServiceLinkerManager.
     */
    public static void connector(com.azure.resourcemanager.servicelinker.ServiceLinkerManager manager) {
        manager.connectors()
            .getWithResponse("00000000-0000-0000-0000-000000000000", "test-rg", "westus", "connectorName",
                com.azure.core.util.Context.NONE);
    }
}
```

### Connector_GetDryrun

```java
/**
 * Samples for Connector GetDryrun.
 */
public final class ConnectorGetDryrunSamples {
    /*
     * x-ms-original-file:
     * specification/servicelinker/resource-manager/Microsoft.ServiceLinker/preview/2024-07-01-preview/examples/
     * ConnectorDryrunGet.json
     */
    /**
     * Sample code: ConnectorDryrunGet.
     * 
     * @param manager Entry point to ServiceLinkerManager.
     */
    public static void connectorDryrunGet(com.azure.resourcemanager.servicelinker.ServiceLinkerManager manager) {
        manager.connectors()
            .getDryrunWithResponse("00000000-0000-0000-0000-000000000000", "test-rg", "westus", "dryrunName",
                com.azure.core.util.Context.NONE);
    }
}
```

### Connector_List

```java
/**
 * Samples for Connector List.
 */
public final class ConnectorListSamples {
    /*
     * x-ms-original-file:
     * specification/servicelinker/resource-manager/Microsoft.ServiceLinker/preview/2024-07-01-preview/examples/
     * ConnectorList.json
     */
    /**
     * Sample code: ConnectorList.
     * 
     * @param manager Entry point to ServiceLinkerManager.
     */
    public static void connectorList(com.azure.resourcemanager.servicelinker.ServiceLinkerManager manager) {
        manager.connectors()
            .list("00000000-0000-0000-0000-000000000000", "test-rg", "westus", com.azure.core.util.Context.NONE);
    }
}
```

### Connector_ListDryrun

```java
/**
 * Samples for Connector ListDryrun.
 */
public final class ConnectorListDryrunSamples {
    /*
     * x-ms-original-file:
     * specification/servicelinker/resource-manager/Microsoft.ServiceLinker/preview/2024-07-01-preview/examples/
     * ConnectorDryrunList.json
     */
    /**
     * Sample code: ConnectorDryrunList.
     * 
     * @param manager Entry point to ServiceLinkerManager.
     */
    public static void connectorDryrunList(com.azure.resourcemanager.servicelinker.ServiceLinkerManager manager) {
        manager.connectors()
            .listDryrun("00000000-0000-0000-0000-000000000000", "test-rg", "westus", com.azure.core.util.Context.NONE);
    }
}
```

### Connector_Update

```java
import com.azure.resourcemanager.servicelinker.models.AzureResource;
import com.azure.resourcemanager.servicelinker.models.LinkerPatch;
import com.azure.resourcemanager.servicelinker.models.ServicePrincipalSecretAuthInfo;

/**
 * Samples for Connector Update.
 */
public final class ConnectorUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/servicelinker/resource-manager/Microsoft.ServiceLinker/preview/2024-07-01-preview/examples/
     * PatchConnector.json
     */
    /**
     * Sample code: PatchConnector.
     * 
     * @param manager Entry point to ServiceLinkerManager.
     */
    public static void patchConnector(com.azure.resourcemanager.servicelinker.ServiceLinkerManager manager) {
        manager.connectors()
            .update("00000000-0000-0000-0000-000000000000", "test-rg", "westus", "connectorName",
                new LinkerPatch().withTargetService(new AzureResource().withId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.DocumentDb/databaseAccounts/test-acc/mongodbDatabases/test-db"))
                    .withAuthInfo(new ServicePrincipalSecretAuthInfo().withClientId("name")
                        .withPrincipalId("id")
                        .withSecret("fakeTokenPlaceholder")),
                com.azure.core.util.Context.NONE);
    }
}
```

### Connector_UpdateDryrun

```java
import com.azure.resourcemanager.servicelinker.models.AzureResource;
import com.azure.resourcemanager.servicelinker.models.CreateOrUpdateDryrunParameters;
import com.azure.resourcemanager.servicelinker.models.DryrunResource;
import com.azure.resourcemanager.servicelinker.models.SecretAuthInfo;
import com.azure.resourcemanager.servicelinker.models.ValueSecretInfo;

/**
 * Samples for Connector UpdateDryrun.
 */
public final class ConnectorUpdateDryrunSamples {
    /*
     * x-ms-original-file:
     * specification/servicelinker/resource-manager/Microsoft.ServiceLinker/preview/2024-07-01-preview/examples/
     * ConnectorDryrunUpdate.json
     */
    /**
     * Sample code: ConnectorDryrunUpdate.
     * 
     * @param manager Entry point to ServiceLinkerManager.
     */
    public static void connectorDryrunUpdate(com.azure.resourcemanager.servicelinker.ServiceLinkerManager manager) {
        DryrunResource resource = manager.connectors()
            .getDryrunWithResponse("00000000-0000-0000-0000-000000000000", "test-rg", "westus", "dryrunName",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withParameters(new CreateOrUpdateDryrunParameters().withTargetService(new AzureResource().withId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.DocumentDb/databaseAccounts/test-acc/mongodbDatabases/test-db"))
                .withAuthInfo(
                    new SecretAuthInfo().withName("name").withSecretInfo(new ValueSecretInfo().withValue("secret"))))
            .apply();
    }
}
```

### Connector_Validate

```java
/**
 * Samples for Connector Validate.
 */
public final class ConnectorValidateSamples {
    /*
     * x-ms-original-file:
     * specification/servicelinker/resource-manager/Microsoft.ServiceLinker/preview/2024-07-01-preview/examples/
     * ValidateConnectorSuccess.json
     */
    /**
     * Sample code: ValidateConnectorSuccess.
     * 
     * @param manager Entry point to ServiceLinkerManager.
     */
    public static void validateConnectorSuccess(com.azure.resourcemanager.servicelinker.ServiceLinkerManager manager) {
        manager.connectors()
            .validate("00000000-0000-0000-0000-000000000000", "test-rg", "westus", "connectorName",
                com.azure.core.util.Context.NONE);
    }
}
```

### Linker_CreateOrUpdate

```java
import com.azure.resourcemanager.servicelinker.models.AzureResource;
import com.azure.resourcemanager.servicelinker.models.SecretAuthInfo;
import com.azure.resourcemanager.servicelinker.models.VNetSolution;
import com.azure.resourcemanager.servicelinker.models.VNetSolutionType;
import com.azure.resourcemanager.servicelinker.models.ValueSecretInfo;

/**
 * Samples for Linker CreateOrUpdate.
 */
public final class LinkerCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/servicelinker/resource-manager/Microsoft.ServiceLinker/preview/2024-07-01-preview/examples/
     * PutLinker.json
     */
    /**
     * Sample code: PutLinker.
     * 
     * @param manager Entry point to ServiceLinkerManager.
     */
    public static void putLinker(com.azure.resourcemanager.servicelinker.ServiceLinkerManager manager) {
        manager.linkers()
            .define("linkName")
            .withExistingResourceUri(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.Web/sites/test-app")
            .withTargetService(new AzureResource().withId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.DBforPostgreSQL/servers/test-pg/databases/test-db"))
            .withAuthInfo(
                new SecretAuthInfo().withName("name").withSecretInfo(new ValueSecretInfo().withValue("secret")))
            .withVNetSolution(new VNetSolution().withType(VNetSolutionType.SERVICE_ENDPOINT))
            .create();
    }
}
```

### Linker_Delete

```java
/**
 * Samples for Linker Delete.
 */
public final class LinkerDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/servicelinker/resource-manager/Microsoft.ServiceLinker/preview/2024-07-01-preview/examples/
     * DeleteLinker.json
     */
    /**
     * Sample code: DeleteLinker.
     * 
     * @param manager Entry point to ServiceLinkerManager.
     */
    public static void deleteLinker(com.azure.resourcemanager.servicelinker.ServiceLinkerManager manager) {
        manager.linkers()
            .delete(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.Web/sites/test-app",
                "linkName", com.azure.core.util.Context.NONE);
    }
}
```

### Linker_Get

```java
/**
 * Samples for Linker Get.
 */
public final class LinkerGetSamples {
    /*
     * x-ms-original-file:
     * specification/servicelinker/resource-manager/Microsoft.ServiceLinker/preview/2024-07-01-preview/examples/Linker.
     * json
     */
    /**
     * Sample code: Linker.
     * 
     * @param manager Entry point to ServiceLinkerManager.
     */
    public static void linker(com.azure.resourcemanager.servicelinker.ServiceLinkerManager manager) {
        manager.linkers()
            .getWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.Web/sites/test-app",
                "linkName", com.azure.core.util.Context.NONE);
    }
}
```

### Linker_List

```java
/**
 * Samples for Linker List.
 */
public final class LinkerListSamples {
    /*
     * x-ms-original-file:
     * specification/servicelinker/resource-manager/Microsoft.ServiceLinker/preview/2024-07-01-preview/examples/
     * LinkerList.json
     */
    /**
     * Sample code: LinkerList.
     * 
     * @param manager Entry point to ServiceLinkerManager.
     */
    public static void linkerList(com.azure.resourcemanager.servicelinker.ServiceLinkerManager manager) {
        manager.linkers()
            .list(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.Web/sites/test-app",
                com.azure.core.util.Context.NONE);
    }
}
```

### Linker_ListConfigurations

```java
/**
 * Samples for Linker ListConfigurations.
 */
public final class LinkerListConfigurationsSamples {
    /*
     * x-ms-original-file:
     * specification/servicelinker/resource-manager/Microsoft.ServiceLinker/preview/2024-07-01-preview/examples/
     * GetConfigurations.json
     */
    /**
     * Sample code: GetConfiguration.
     * 
     * @param manager Entry point to ServiceLinkerManager.
     */
    public static void getConfiguration(com.azure.resourcemanager.servicelinker.ServiceLinkerManager manager) {
        manager.linkers()
            .listConfigurationsWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.App/containerApps/test-app",
                "linkName", com.azure.core.util.Context.NONE);
    }
}
```

### Linker_Update

```java
import com.azure.resourcemanager.servicelinker.models.AzureResource;
import com.azure.resourcemanager.servicelinker.models.LinkerResource;
import com.azure.resourcemanager.servicelinker.models.ServicePrincipalSecretAuthInfo;

/**
 * Samples for Linker Update.
 */
public final class LinkerUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/servicelinker/resource-manager/Microsoft.ServiceLinker/preview/2024-07-01-preview/examples/
     * PatchLinker.json
     */
    /**
     * Sample code: PatchLinker.
     * 
     * @param manager Entry point to ServiceLinkerManager.
     */
    public static void patchLinker(com.azure.resourcemanager.servicelinker.ServiceLinkerManager manager) {
        LinkerResource resource = manager.linkers()
            .getWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.Web/sites/test-app",
                "linkName", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTargetService(new AzureResource().withId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.DocumentDb/databaseAccounts/test-acc/mongodbDatabases/test-db"))
            .withAuthInfo(new ServicePrincipalSecretAuthInfo().withClientId("name")
                .withPrincipalId("id")
                .withSecret("fakeTokenPlaceholder"))
            .apply();
    }
}
```

### Linker_Validate

```java
/**
 * Samples for Linker Validate.
 */
public final class LinkerValidateSamples {
    /*
     * x-ms-original-file:
     * specification/servicelinker/resource-manager/Microsoft.ServiceLinker/preview/2024-07-01-preview/examples/
     * ValidateLinkerSuccess.json
     */
    /**
     * Sample code: ValidateLinkerSuccess.
     * 
     * @param manager Entry point to ServiceLinkerManager.
     */
    public static void validateLinkerSuccess(com.azure.resourcemanager.servicelinker.ServiceLinkerManager manager) {
        manager.linkers()
            .validate(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.Web/sites/test-app",
                "linkName", com.azure.core.util.Context.NONE);
    }
}
```

### LinkersOperation_CreateDryrun

```java
import com.azure.resourcemanager.servicelinker.fluent.models.DryrunResourceInner;
import com.azure.resourcemanager.servicelinker.models.AzureResource;
import com.azure.resourcemanager.servicelinker.models.CreateOrUpdateDryrunParameters;
import com.azure.resourcemanager.servicelinker.models.SecretAuthInfo;
import com.azure.resourcemanager.servicelinker.models.ValueSecretInfo;

/**
 * Samples for LinkersOperation CreateDryrun.
 */
public final class LinkersOperationCreateDryrunSamples {
    /*
     * x-ms-original-file:
     * specification/servicelinker/resource-manager/Microsoft.ServiceLinker/preview/2024-07-01-preview/examples/
     * PutDryrun.json
     */
    /**
     * Sample code: PutDryrun.
     * 
     * @param manager Entry point to ServiceLinkerManager.
     */
    public static void putDryrun(com.azure.resourcemanager.servicelinker.ServiceLinkerManager manager) {
        manager.linkersOperations()
            .createDryrun(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.Web/sites/test-app",
                "dryrunName",
                new DryrunResourceInner().withParameters(new CreateOrUpdateDryrunParameters()
                    .withTargetService(new AzureResource().withId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.DocumentDb/databaseAccounts/test-acc/mongodbDatabases/test-db"))
                    .withAuthInfo(new SecretAuthInfo().withName("name")
                        .withSecretInfo(new ValueSecretInfo().withValue("secret")))),
                com.azure.core.util.Context.NONE);
    }
}
```

### LinkersOperation_DeleteDryrun

```java
/**
 * Samples for LinkersOperation DeleteDryrun.
 */
public final class LinkersOperationDeleteDryrunSamples {
    /*
     * x-ms-original-file:
     * specification/servicelinker/resource-manager/Microsoft.ServiceLinker/preview/2024-07-01-preview/examples/
     * DeleteDryrun.json
     */
    /**
     * Sample code: DeleteDryrun.
     * 
     * @param manager Entry point to ServiceLinkerManager.
     */
    public static void deleteDryrun(com.azure.resourcemanager.servicelinker.ServiceLinkerManager manager) {
        manager.linkersOperations()
            .deleteDryrunWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.Web/sites/test-app",
                "dryrunName", com.azure.core.util.Context.NONE);
    }
}
```

### LinkersOperation_GenerateConfigurations

```java
import com.azure.resourcemanager.servicelinker.models.ConfigurationInfo;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for LinkersOperation GenerateConfigurations.
 */
public final class LinkersOperationGenerateConfigurationsSamples {
    /*
     * x-ms-original-file:
     * specification/servicelinker/resource-manager/Microsoft.ServiceLinker/preview/2024-07-01-preview/examples/
     * LinkerGenerateConfigurations.json
     */
    /**
     * Sample code: GenerateConfiguration.
     * 
     * @param manager Entry point to ServiceLinkerManager.
     */
    public static void generateConfiguration(com.azure.resourcemanager.servicelinker.ServiceLinkerManager manager) {
        manager.linkersOperations()
            .generateConfigurationsWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.Web/sites/test-app",
                "linkName", new ConfigurationInfo().withCustomizedKeys(
                    mapOf("ASL_DocumentDb_ConnectionString", "MyConnectionstring")),
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

### LinkersOperation_GetDryrun

```java
/**
 * Samples for LinkersOperation GetDryrun.
 */
public final class LinkersOperationGetDryrunSamples {
    /*
     * x-ms-original-file:
     * specification/servicelinker/resource-manager/Microsoft.ServiceLinker/preview/2024-07-01-preview/examples/
     * GetDryrun.json
     */
    /**
     * Sample code: GetDryrun.
     * 
     * @param manager Entry point to ServiceLinkerManager.
     */
    public static void getDryrun(com.azure.resourcemanager.servicelinker.ServiceLinkerManager manager) {
        manager.linkersOperations()
            .getDryrunWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.Web/sites/test-app",
                "dryrunName", com.azure.core.util.Context.NONE);
    }
}
```

### LinkersOperation_ListDaprConfigurations

```java
/**
 * Samples for LinkersOperation ListDaprConfigurations.
 */
public final class LinkersOperationListDaprConfigurationsSamples {
    /*
     * x-ms-original-file:
     * specification/servicelinker/resource-manager/Microsoft.ServiceLinker/preview/2024-07-01-preview/examples/
     * GetDaprConfigurations.json
     */
    /**
     * Sample code: GetDaprConfigurations.
     * 
     * @param manager Entry point to ServiceLinkerManager.
     */
    public static void getDaprConfigurations(com.azure.resourcemanager.servicelinker.ServiceLinkerManager manager) {
        manager.linkersOperations()
            .listDaprConfigurations(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.Web/sites/test-app",
                com.azure.core.util.Context.NONE);
    }
}
```

### LinkersOperation_ListDryrun

```java
/**
 * Samples for LinkersOperation ListDryrun.
 */
public final class LinkersOperationListDryrunSamples {
    /*
     * x-ms-original-file:
     * specification/servicelinker/resource-manager/Microsoft.ServiceLinker/preview/2024-07-01-preview/examples/
     * ListDryrun.json
     */
    /**
     * Sample code: ListDryrun.
     * 
     * @param manager Entry point to ServiceLinkerManager.
     */
    public static void listDryrun(com.azure.resourcemanager.servicelinker.ServiceLinkerManager manager) {
        manager.linkersOperations()
            .listDryrun(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.Web/sites/test-app",
                com.azure.core.util.Context.NONE);
    }
}
```

### LinkersOperation_UpdateDryrun

```java
import com.azure.resourcemanager.servicelinker.models.AzureResource;
import com.azure.resourcemanager.servicelinker.models.CreateOrUpdateDryrunParameters;
import com.azure.resourcemanager.servicelinker.models.DryrunPatch;
import com.azure.resourcemanager.servicelinker.models.SecretAuthInfo;
import com.azure.resourcemanager.servicelinker.models.ValueSecretInfo;

/**
 * Samples for LinkersOperation UpdateDryrun.
 */
public final class LinkersOperationUpdateDryrunSamples {
    /*
     * x-ms-original-file:
     * specification/servicelinker/resource-manager/Microsoft.ServiceLinker/preview/2024-07-01-preview/examples/
     * PatchDryrun.json
     */
    /**
     * Sample code: PatchDryrun.
     * 
     * @param manager Entry point to ServiceLinkerManager.
     */
    public static void patchDryrun(com.azure.resourcemanager.servicelinker.ServiceLinkerManager manager) {
        manager.linkersOperations()
            .updateDryrun(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.Web/sites/test-app",
                "dryrunName",
                new DryrunPatch().withParameters(new CreateOrUpdateDryrunParameters()
                    .withTargetService(new AzureResource().withId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.DocumentDb/databaseAccounts/test-acc/mongodbDatabases/test-db"))
                    .withAuthInfo(new SecretAuthInfo().withName("name")
                        .withSecretInfo(new ValueSecretInfo().withValue("secret")))),
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
     * specification/servicelinker/resource-manager/Microsoft.ServiceLinker/preview/2024-07-01-preview/examples/
     * OperationsList.json
     */
    /**
     * Sample code: GetConfiguration.
     * 
     * @param manager Entry point to ServiceLinkerManager.
     */
    public static void getConfiguration(com.azure.resourcemanager.servicelinker.ServiceLinkerManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```


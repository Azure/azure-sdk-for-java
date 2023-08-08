# Code snippets and samples


## Linker

- [CreateOrUpdate](#linker_createorupdate)
- [Delete](#linker_delete)
- [Get](#linker_get)
- [List](#linker_list)
- [ListConfigurations](#linker_listconfigurations)
- [Update](#linker_update)
- [Validate](#linker_validate)

## Operations

- [List](#operations_list)
### Linker_CreateOrUpdate

```java
import com.azure.resourcemanager.servicelinker.models.AzureResource;
import com.azure.resourcemanager.servicelinker.models.KeyVaultSecretUriSecretInfo;
import com.azure.resourcemanager.servicelinker.models.SecretAuthInfo;
import com.azure.resourcemanager.servicelinker.models.SecretStore;
import com.azure.resourcemanager.servicelinker.models.VNetSolution;
import com.azure.resourcemanager.servicelinker.models.VNetSolutionType;
import com.azure.resourcemanager.servicelinker.models.ValueSecretInfo;

/** Samples for Linker CreateOrUpdate. */
public final class LinkerCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/servicelinker/resource-manager/Microsoft.ServiceLinker/stable/2022-05-01/examples/PutLinkWithSecretStore.json
     */
    /**
     * Sample code: PutLinkWithSecretStore.
     *
     * @param manager Entry point to ServiceLinkerManager.
     */
    public static void putLinkWithSecretStore(com.azure.resourcemanager.servicelinker.ServiceLinkerManager manager) {
        manager
            .linkers()
            .define("linkName")
            .withExistingResourceUri(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.Web/sites/test-app")
            .withTargetService(
                new AzureResource()
                    .withId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.DocumentDb/databaseAccounts/test-acc/mongodbDatabases/test-db"))
            .withAuthInfo(new SecretAuthInfo())
            .withSecretStore(
                new SecretStore()
                    .withKeyVaultId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.KeyVault/vaults/test-kv"))
            .create();
    }

    /*
     * x-ms-original-file: specification/servicelinker/resource-manager/Microsoft.ServiceLinker/stable/2022-05-01/examples/PutLinkWithServiceEndpoint.json
     */
    /**
     * Sample code: PutLinkWithServiceEndpoint.
     *
     * @param manager Entry point to ServiceLinkerManager.
     */
    public static void putLinkWithServiceEndpoint(
        com.azure.resourcemanager.servicelinker.ServiceLinkerManager manager) {
        manager
            .linkers()
            .define("linkName")
            .withExistingResourceUri(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.Web/sites/test-app")
            .withTargetService(
                new AzureResource()
                    .withId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.DBforPostgreSQL/servers/test-pg/databases/test-db"))
            .withAuthInfo(
                new SecretAuthInfo()
                    .withName("name")
                    .withSecretInfo(
                        new KeyVaultSecretUriSecretInfo()
                            .withValue(
                                "https://vault-name.vault.azure.net/secrets/secret-name/00000000000000000000000000000000")))
            .withVNetSolution(new VNetSolution().withType(VNetSolutionType.SERVICE_ENDPOINT))
            .create();
    }

    /*
     * x-ms-original-file: specification/servicelinker/resource-manager/Microsoft.ServiceLinker/stable/2022-05-01/examples/PutLink.json
     */
    /**
     * Sample code: PutLink.
     *
     * @param manager Entry point to ServiceLinkerManager.
     */
    public static void putLink(com.azure.resourcemanager.servicelinker.ServiceLinkerManager manager) {
        manager
            .linkers()
            .define("linkName")
            .withExistingResourceUri(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.Web/sites/test-app")
            .withTargetService(
                new AzureResource()
                    .withId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.DBforPostgreSQL/servers/test-pg/databases/test-db"))
            .withAuthInfo(
                new SecretAuthInfo().withName("name").withSecretInfo(new ValueSecretInfo().withValue("secret")))
            .create();
    }
}
```

### Linker_Delete

```java
import com.azure.core.util.Context;

/** Samples for Linker Delete. */
public final class LinkerDeleteSamples {
    /*
     * x-ms-original-file: specification/servicelinker/resource-manager/Microsoft.ServiceLinker/stable/2022-05-01/examples/DeleteLink.json
     */
    /**
     * Sample code: DeleteLink.
     *
     * @param manager Entry point to ServiceLinkerManager.
     */
    public static void deleteLink(com.azure.resourcemanager.servicelinker.ServiceLinkerManager manager) {
        manager
            .linkers()
            .delete(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.Web/sites/test-app",
                "linkName",
                Context.NONE);
    }
}
```

### Linker_Get

```java
import com.azure.core.util.Context;

/** Samples for Linker Get. */
public final class LinkerGetSamples {
    /*
     * x-ms-original-file: specification/servicelinker/resource-manager/Microsoft.ServiceLinker/stable/2022-05-01/examples/Link.json
     */
    /**
     * Sample code: Link.
     *
     * @param manager Entry point to ServiceLinkerManager.
     */
    public static void link(com.azure.resourcemanager.servicelinker.ServiceLinkerManager manager) {
        manager
            .linkers()
            .getWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.Web/sites/test-app",
                "linkName",
                Context.NONE);
    }
}
```

### Linker_List

```java
import com.azure.core.util.Context;

/** Samples for Linker List. */
public final class LinkerListSamples {
    /*
     * x-ms-original-file: specification/servicelinker/resource-manager/Microsoft.ServiceLinker/stable/2022-05-01/examples/LinkList.json
     */
    /**
     * Sample code: LinkList.
     *
     * @param manager Entry point to ServiceLinkerManager.
     */
    public static void linkList(com.azure.resourcemanager.servicelinker.ServiceLinkerManager manager) {
        manager
            .linkers()
            .list(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.Web/sites/test-app",
                Context.NONE);
    }
}
```

### Linker_ListConfigurations

```java
import com.azure.core.util.Context;

/** Samples for Linker ListConfigurations. */
public final class LinkerListConfigurationsSamples {
    /*
     * x-ms-original-file: specification/servicelinker/resource-manager/Microsoft.ServiceLinker/stable/2022-05-01/examples/GetConfigurations.json
     */
    /**
     * Sample code: GetConfiguration.
     *
     * @param manager Entry point to ServiceLinkerManager.
     */
    public static void getConfiguration(com.azure.resourcemanager.servicelinker.ServiceLinkerManager manager) {
        manager
            .linkers()
            .listConfigurationsWithResponse(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.Web/sites/test-app",
                "linkName",
                Context.NONE);
    }
}
```

### Linker_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.servicelinker.models.AzureResource;
import com.azure.resourcemanager.servicelinker.models.LinkerResource;
import com.azure.resourcemanager.servicelinker.models.ServicePrincipalSecretAuthInfo;

/** Samples for Linker Update. */
public final class LinkerUpdateSamples {
    /*
     * x-ms-original-file: specification/servicelinker/resource-manager/Microsoft.ServiceLinker/stable/2022-05-01/examples/PatchLink.json
     */
    /**
     * Sample code: PatchLink.
     *
     * @param manager Entry point to ServiceLinkerManager.
     */
    public static void patchLink(com.azure.resourcemanager.servicelinker.ServiceLinkerManager manager) {
        LinkerResource resource =
            manager
                .linkers()
                .getWithResponse(
                    "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.Web/sites/test-app",
                    "linkName",
                    Context.NONE)
                .getValue();
        resource
            .update()
            .withTargetService(
                new AzureResource()
                    .withId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.DocumentDb/databaseAccounts/test-acc/mongodbDatabases/test-db"))
            .withAuthInfo(
                new ServicePrincipalSecretAuthInfo().withClientId("name").withPrincipalId("id").withSecret("secret"))
            .apply();
    }
}
```

### Linker_Validate

```java
import com.azure.core.util.Context;

/** Samples for Linker Validate. */
public final class LinkerValidateSamples {
    /*
     * x-ms-original-file: specification/servicelinker/resource-manager/Microsoft.ServiceLinker/stable/2022-05-01/examples/ValidateLinkSuccess.json
     */
    /**
     * Sample code: ValidateLinkSuccess.
     *
     * @param manager Entry point to ServiceLinkerManager.
     */
    public static void validateLinkSuccess(com.azure.resourcemanager.servicelinker.ServiceLinkerManager manager) {
        manager
            .linkers()
            .validate(
                "subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test-rg/providers/Microsoft.Web/sites/test-app",
                "linkName",
                Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/servicelinker/resource-manager/Microsoft.ServiceLinker/stable/2022-05-01/examples/OperationsList.json
     */
    /**
     * Sample code: GetConfiguration.
     *
     * @param manager Entry point to ServiceLinkerManager.
     */
    public static void getConfiguration(com.azure.resourcemanager.servicelinker.ServiceLinkerManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```


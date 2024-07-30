# Code snippets and samples


## ExtensionMetadata

- [Get](#extensionmetadata_get)
- [List](#extensionmetadata_list)

## Gateways

- [CreateOrUpdate](#gateways_createorupdate)
- [Delete](#gateways_delete)
- [GetByResourceGroup](#gateways_getbyresourcegroup)
- [List](#gateways_list)
- [ListByResourceGroup](#gateways_listbyresourcegroup)
- [Update](#gateways_update)

## Licenses

- [CreateOrUpdate](#licenses_createorupdate)
- [Delete](#licenses_delete)
- [GetByResourceGroup](#licenses_getbyresourcegroup)
- [List](#licenses_list)
- [ListByResourceGroup](#licenses_listbyresourcegroup)
- [Update](#licenses_update)

## MachineExtensions

- [CreateOrUpdate](#machineextensions_createorupdate)
- [Delete](#machineextensions_delete)
- [Get](#machineextensions_get)
- [List](#machineextensions_list)
- [Update](#machineextensions_update)

## MachineRunCommands

- [CreateOrUpdate](#machineruncommands_createorupdate)
- [Delete](#machineruncommands_delete)
- [Get](#machineruncommands_get)
- [List](#machineruncommands_list)

## Machines

- [AssessPatches](#machines_assesspatches)
- [Delete](#machines_delete)
- [GetByResourceGroup](#machines_getbyresourcegroup)
- [InstallPatches](#machines_installpatches)
- [List](#machines_list)
- [ListByResourceGroup](#machines_listbyresourcegroup)

## NetworkProfile

- [Get](#networkprofile_get)

## NetworkSecurityPerimeterConfigurations

- [GetByPrivateLinkScope](#networksecurityperimeterconfigurations_getbyprivatelinkscope)
- [ListByPrivateLinkScope](#networksecurityperimeterconfigurations_listbyprivatelinkscope)
- [ReconcileForPrivateLinkScope](#networksecurityperimeterconfigurations_reconcileforprivatelinkscope)

## Operations

- [List](#operations_list)

## PrivateEndpointConnections

- [CreateOrUpdate](#privateendpointconnections_createorupdate)
- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [ListByPrivateLinkScope](#privateendpointconnections_listbyprivatelinkscope)

## PrivateLinkResources

- [Get](#privatelinkresources_get)
- [ListByPrivateLinkScope](#privatelinkresources_listbyprivatelinkscope)

## PrivateLinkScopes

- [CreateOrUpdate](#privatelinkscopes_createorupdate)
- [Delete](#privatelinkscopes_delete)
- [GetByResourceGroup](#privatelinkscopes_getbyresourcegroup)
- [GetValidationDetails](#privatelinkscopes_getvalidationdetails)
- [GetValidationDetailsForMachine](#privatelinkscopes_getvalidationdetailsformachine)
- [List](#privatelinkscopes_list)
- [ListByResourceGroup](#privatelinkscopes_listbyresourcegroup)
- [UpdateTags](#privatelinkscopes_updatetags)

## ResourceProvider

- [UpgradeExtensions](#resourceprovider_upgradeextensions)

## SettingsOperation

- [Get](#settingsoperation_get)
- [Patch](#settingsoperation_patch)
- [Update](#settingsoperation_update)
### ExtensionMetadata_Get

```java
/**
 * Samples for ExtensionMetadata Get.
 */
public final class ExtensionMetadataGetSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/
     * extension/ExtensionMetadata_Get.json
     */
    /**
     * Sample code: GET an extensions metadata.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void gETAnExtensionsMetadata(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.extensionMetadatas()
            .getWithResponse("EastUS", "microsoft.azure.monitor", "azuremonitorlinuxagent", "1.9.1",
                com.azure.core.util.Context.NONE);
    }
}
```

### ExtensionMetadata_List

```java
/**
 * Samples for ExtensionMetadata List.
 */
public final class ExtensionMetadataListSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/
     * extension/ExtensionMetadata_List.json
     */
    /**
     * Sample code: GET a list of extensions.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void gETAListOfExtensions(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.extensionMetadatas()
            .list("EastUS", "microsoft.azure.monitor", "azuremonitorlinuxagent", com.azure.core.util.Context.NONE);
    }
}
```

### Gateways_CreateOrUpdate

```java
import com.azure.resourcemanager.hybridcompute.models.GatewayType;
import java.util.Arrays;

/**
 * Samples for Gateways CreateOrUpdate.
 */
public final class GatewaysCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/gateway/
     * Gateway_CreateOrUpdate.json
     */
    /**
     * Sample code: Create or Update a Gateway.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void createOrUpdateAGateway(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.gateways()
            .define("{gatewayName}")
            .withRegion("eastus2euap")
            .withExistingResourceGroup("myResourceGroup")
            .withGatewayType(GatewayType.PUBLIC)
            .withAllowedFeatures(Arrays.asList("*"))
            .create();
    }
}
```

### Gateways_Delete

```java
/**
 * Samples for Gateways Delete.
 */
public final class GatewaysDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/gateway/
     * Gateway_Delete.json
     */
    /**
     * Sample code: Delete a Gateway.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void deleteAGateway(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.gateways().delete("myResourceGroup", "{gatewayName}", com.azure.core.util.Context.NONE);
    }
}
```

### Gateways_GetByResourceGroup

```java
/**
 * Samples for Gateways GetByResourceGroup.
 */
public final class GatewaysGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/gateway/
     * Gateway_Get.json
     */
    /**
     * Sample code: Get Gateway.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void getGateway(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.gateways()
            .getByResourceGroupWithResponse("myResourceGroup", "{gatewayName}", com.azure.core.util.Context.NONE);
    }
}
```

### Gateways_List

```java
/**
 * Samples for Gateways List.
 */
public final class GatewaysListSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/gateway/
     * Gateway_ListBySubscription.json
     */
    /**
     * Sample code: List Gateways by Subscription.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void
        listGatewaysBySubscription(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.gateways().list(com.azure.core.util.Context.NONE);
    }
}
```

### Gateways_ListByResourceGroup

```java
/**
 * Samples for Gateways ListByResourceGroup.
 */
public final class GatewaysListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/gateway/
     * Gateway_ListByResourceGroup.json
     */
    /**
     * Sample code: List Gateways by Resource Group.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void
        listGatewaysByResourceGroup(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.gateways().listByResourceGroup("myResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### Gateways_Update

```java
import com.azure.resourcemanager.hybridcompute.models.Gateway;
import java.util.Arrays;

/**
 * Samples for Gateways Update.
 */
public final class GatewaysUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/gateway/
     * Gateway_Update.json
     */
    /**
     * Sample code: Update a Gateway.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void updateAGateway(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        Gateway resource = manager.gateways()
            .getByResourceGroupWithResponse("myResourceGroup", "{gatewayName}", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withAllowedFeatures(Arrays.asList("*")).apply();
    }
}
```

### Licenses_CreateOrUpdate

```java
import com.azure.resourcemanager.hybridcompute.models.LicenseCoreType;
import com.azure.resourcemanager.hybridcompute.models.LicenseDetails;
import com.azure.resourcemanager.hybridcompute.models.LicenseEdition;
import com.azure.resourcemanager.hybridcompute.models.LicenseState;
import com.azure.resourcemanager.hybridcompute.models.LicenseTarget;
import com.azure.resourcemanager.hybridcompute.models.LicenseType;

/**
 * Samples for Licenses CreateOrUpdate.
 */
public final class LicensesCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/license/
     * License_CreateOrUpdate.json
     */
    /**
     * Sample code: Create or Update a License.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void createOrUpdateALicense(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.licenses()
            .define("{licenseName}")
            .withRegion("eastus2euap")
            .withExistingResourceGroup("myResourceGroup")
            .withLicenseType(LicenseType.ESU)
            .withLicenseDetails(new LicenseDetails().withState(LicenseState.ACTIVATED)
                .withTarget(LicenseTarget.WINDOWS_SERVER_2012)
                .withEdition(LicenseEdition.DATACENTER)
                .withType(LicenseCoreType.P_CORE)
                .withProcessors(6))
            .create();
    }
}
```

### Licenses_Delete

```java
/**
 * Samples for Licenses Delete.
 */
public final class LicensesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/license/
     * License_Delete.json
     */
    /**
     * Sample code: Delete a License.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void deleteALicense(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.licenses().delete("myResourceGroup", "{licenseName}", com.azure.core.util.Context.NONE);
    }
}
```

### Licenses_GetByResourceGroup

```java
/**
 * Samples for Licenses GetByResourceGroup.
 */
public final class LicensesGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/license/
     * License_Get.json
     */
    /**
     * Sample code: Get License.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void getLicense(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.licenses()
            .getByResourceGroupWithResponse("myResourceGroup", "{licenseName}", com.azure.core.util.Context.NONE);
    }
}
```

### Licenses_List

```java
/**
 * Samples for Licenses List.
 */
public final class LicensesListSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/license/
     * License_ListBySubscription.json
     */
    /**
     * Sample code: List Licenses by Subscription.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void
        listLicensesBySubscription(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.licenses().list(com.azure.core.util.Context.NONE);
    }
}
```

### Licenses_ListByResourceGroup

```java
/**
 * Samples for Licenses ListByResourceGroup.
 */
public final class LicensesListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/license/
     * License_ListByResourceGroup.json
     */
    /**
     * Sample code: GET all Machine Extensions.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void gETAllMachineExtensions(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.licenses().listByResourceGroup("myResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### Licenses_Update

```java
import com.azure.resourcemanager.hybridcompute.models.License;
import com.azure.resourcemanager.hybridcompute.models.LicenseCoreType;
import com.azure.resourcemanager.hybridcompute.models.LicenseEdition;
import com.azure.resourcemanager.hybridcompute.models.LicenseState;
import com.azure.resourcemanager.hybridcompute.models.LicenseTarget;
import com.azure.resourcemanager.hybridcompute.models.LicenseType;

/**
 * Samples for Licenses Update.
 */
public final class LicensesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/license/
     * License_Update.json
     */
    /**
     * Sample code: Update a License.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void updateALicense(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        License resource = manager.licenses()
            .getByResourceGroupWithResponse("myResourceGroup", "{licenseName}", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withLicenseType(LicenseType.ESU)
            .withState(LicenseState.ACTIVATED)
            .withTarget(LicenseTarget.WINDOWS_SERVER_2012)
            .withEdition(LicenseEdition.DATACENTER)
            .withType(LicenseCoreType.P_CORE)
            .withProcessors(6)
            .apply();
    }
}
```

### MachineExtensions_CreateOrUpdate

```java
import com.azure.resourcemanager.hybridcompute.models.MachineExtensionProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for MachineExtensions CreateOrUpdate.
 */
public final class MachineExtensionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/
     * extension/Extension_CreateOrUpdate.json
     */
    /**
     * Sample code: Create or Update a Machine Extension.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void
        createOrUpdateAMachineExtension(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.machineExtensions()
            .define("CustomScriptExtension")
            .withRegion("eastus2euap")
            .withExistingMachine("myResourceGroup", "myMachine")
            .withProperties(
                new MachineExtensionProperties().withPublisher("Microsoft.Compute")
                    .withType("CustomScriptExtension")
                    .withTypeHandlerVersion("1.10")
                    .withSettings(mapOf("commandToExecute",
                        "powershell.exe -c \"Get-Process | Where-Object { $_.CPU -gt 10000 }\"")))
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

### MachineExtensions_Delete

```java
/**
 * Samples for MachineExtensions Delete.
 */
public final class MachineExtensionsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/
     * extension/Extension_Delete.json
     */
    /**
     * Sample code: Delete a Machine Extension.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void deleteAMachineExtension(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.machineExtensions().delete("myResourceGroup", "myMachine", "MMA", com.azure.core.util.Context.NONE);
    }
}
```

### MachineExtensions_Get

```java
/**
 * Samples for MachineExtensions Get.
 */
public final class MachineExtensionsGetSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/
     * extension/Extension_Get.json
     */
    /**
     * Sample code: GET Machine Extension.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void gETMachineExtension(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.machineExtensions()
            .getWithResponse("myResourceGroup", "myMachine", "CustomScriptExtension", com.azure.core.util.Context.NONE);
    }
}
```

### MachineExtensions_List

```java
/**
 * Samples for MachineExtensions List.
 */
public final class MachineExtensionsListSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/
     * extension/Extension_List.json
     */
    /**
     * Sample code: GET all Machine Extensions - List.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void
        gETAllMachineExtensionsList(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.machineExtensions().list("myResourceGroup", "myMachine", null, com.azure.core.util.Context.NONE);
    }
}
```

### MachineExtensions_Update

```java
import com.azure.resourcemanager.hybridcompute.models.MachineExtension;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for MachineExtensions Update.
 */
public final class MachineExtensionsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/
     * extension/Extension_Update.json
     */
    /**
     * Sample code: Create or Update a Machine Extension.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void
        createOrUpdateAMachineExtension(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        MachineExtension resource = manager.machineExtensions()
            .getWithResponse("myResourceGroup", "myMachine", "CustomScriptExtension", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withPublisher("Microsoft.Compute")
            .withType("CustomScriptExtension")
            .withTypeHandlerVersion("1.10")
            .withEnableAutomaticUpgrade(true)
            .withSettings(
                mapOf("commandToExecute", "powershell.exe -c \"Get-Process | Where-Object { $_.CPU -lt 100 }\""))
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

### MachineRunCommands_CreateOrUpdate

```java
import com.azure.resourcemanager.hybridcompute.models.MachineRunCommandScriptSource;
import com.azure.resourcemanager.hybridcompute.models.RunCommandInputParameter;
import java.util.Arrays;

/**
 * Samples for MachineRunCommands CreateOrUpdate.
 */
public final class MachineRunCommandsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/
     * runCommand/RunCommands_CreateOrUpdate.json
     */
    /**
     * Sample code: Create or Update a Run Command.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void createOrUpdateARunCommand(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.machineRunCommands()
            .define("myRunCommand")
            .withRegion("eastus2")
            .withExistingMachine("myResourceGroup", "myMachine")
            .withSource(new MachineRunCommandScriptSource().withScript("Write-Host Hello World!"))
            .withParameters(Arrays.asList(new RunCommandInputParameter().withName("param1").withValue("value1"),
                new RunCommandInputParameter().withName("param2").withValue("value2")))
            .withAsyncExecution(false)
            .withRunAsUser("user1")
            .withRunAsPassword("<runAsPassword>")
            .withTimeoutInSeconds(3600)
            .withOutputBlobUri(
                "https://mystorageaccount.blob.core.windows.net/myscriptoutputcontainer/MyScriptoutput.txt")
            .withErrorBlobUri("https://mystorageaccount.blob.core.windows.net/mycontainer/MyScriptError.txt")
            .create();
    }
}
```

### MachineRunCommands_Delete

```java
/**
 * Samples for MachineRunCommands Delete.
 */
public final class MachineRunCommandsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/
     * runCommand/RunCommands_Delete.json
     */
    /**
     * Sample code: Delete a Machine Run Command.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void deleteAMachineRunCommand(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.machineRunCommands()
            .delete("myResourceGroup", "myMachine", "myRunCommand", com.azure.core.util.Context.NONE);
    }
}
```

### MachineRunCommands_Get

```java
/**
 * Samples for MachineRunCommands Get.
 */
public final class MachineRunCommandsGetSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/
     * runCommand/RunCommands_Get.json
     */
    /**
     * Sample code: Get a Run Command.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void getARunCommand(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.machineRunCommands()
            .getWithResponse("myResourceGroup", "myMachine", "myRunCommand", com.azure.core.util.Context.NONE);
    }
}
```

### MachineRunCommands_List

```java
/**
 * Samples for MachineRunCommands List.
 */
public final class MachineRunCommandsListSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/
     * runCommand/RunCommands_List.json
     */
    /**
     * Sample code: GET all Machine Run Commands.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void gETAllMachineRunCommands(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.machineRunCommands().list("myResourceGroup", "myMachine", null, com.azure.core.util.Context.NONE);
    }
}
```

### Machines_AssessPatches

```java
/**
 * Samples for Machines AssessPatches.
 */
public final class MachinesAssessPatchesSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/machine/
     * Machine_AssessPatches.json
     */
    /**
     * Sample code: Assess patch state of a machine.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void
        assessPatchStateOfAMachine(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.machines().assessPatches("myResourceGroupName", "myMachineName", com.azure.core.util.Context.NONE);
    }
}
```

### Machines_Delete

```java
/**
 * Samples for Machines Delete.
 */
public final class MachinesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/machine/
     * Machines_Delete.json
     */
    /**
     * Sample code: Delete a Machine.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void deleteAMachine(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.machines()
            .deleteByResourceGroupWithResponse("myResourceGroup", "myMachine", com.azure.core.util.Context.NONE);
    }
}
```

### Machines_GetByResourceGroup

```java
/**
 * Samples for Machines GetByResourceGroup.
 */
public final class MachinesGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/machine/
     * Machines_Get_LicenseProfileInstanceView.json
     */
    /**
     * Sample code: Get Machine with License Profile Instance View.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void
        getMachineWithLicenseProfileInstanceView(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.machines()
            .getByResourceGroupWithResponse("myResourceGroup", "myMachine", "instanceView",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/machine/
     * Machines_Get.json
     */
    /**
     * Sample code: Get Machine.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void getMachine(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.machines()
            .getByResourceGroupWithResponse("myResourceGroup", "myMachine", null, com.azure.core.util.Context.NONE);
    }
}
```

### Machines_InstallPatches

```java
import com.azure.resourcemanager.hybridcompute.models.MachineInstallPatchesParameters;
import com.azure.resourcemanager.hybridcompute.models.VMGuestPatchClassificationWindows;
import com.azure.resourcemanager.hybridcompute.models.VMGuestPatchRebootSetting;
import com.azure.resourcemanager.hybridcompute.models.WindowsParameters;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;

/**
 * Samples for Machines InstallPatches.
 */
public final class MachinesInstallPatchesSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/machine/
     * Machine_InstallPatches.json
     */
    /**
     * Sample code: Install patch state of a machine.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void
        installPatchStateOfAMachine(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.machines()
            .installPatches("myResourceGroupName", "myMachineName",
                new MachineInstallPatchesParameters().withMaximumDuration(Duration.parse("PT4H"))
                    .withRebootSetting(VMGuestPatchRebootSetting.IF_REQUIRED)
                    .withWindowsParameters(new WindowsParameters()
                        .withClassificationsToInclude(Arrays.asList(VMGuestPatchClassificationWindows.CRITICAL,
                            VMGuestPatchClassificationWindows.SECURITY))
                        .withMaxPatchPublishDate(OffsetDateTime.parse("2021-08-19T02:36:43.0539904+00:00"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### Machines_List

```java
/**
 * Samples for Machines List.
 */
public final class MachinesListSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/machine/
     * Machines_ListBySubscription.json
     */
    /**
     * Sample code: List Machines by resource group.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void
        listMachinesByResourceGroup(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.machines().list(com.azure.core.util.Context.NONE);
    }
}
```

### Machines_ListByResourceGroup

```java
/**
 * Samples for Machines ListByResourceGroup.
 */
public final class MachinesListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/machine/
     * Machines_ListByResourceGroup.json
     */
    /**
     * Sample code: List Machines by resource group.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void
        listMachinesByResourceGroup(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.machines().listByResourceGroup("myResourceGroup", null, com.azure.core.util.Context.NONE);
    }
}
```

### NetworkProfile_Get

```java
/**
 * Samples for NetworkProfile Get.
 */
public final class NetworkProfileGetSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/
     * NetworkProfile_Get.json
     */
    /**
     * Sample code: GET Network Profile.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void gETNetworkProfile(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.networkProfiles().getWithResponse("myResourceGroup", "myMachine", com.azure.core.util.Context.NONE);
    }
}
```

### NetworkSecurityPerimeterConfigurations_GetByPrivateLinkScope

```java
/**
 * Samples for NetworkSecurityPerimeterConfigurations GetByPrivateLinkScope.
 */
public final class NetworkSecurityPerimeterConfigurationsGetByPrivateLinkScopeSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/
     * networkSecurityPerimeterConfiguration/NetworkSecurityPerimeterConfigurationGet.json
     */
    /**
     * Sample code: Gets the network security perimeter configuration of the private link scope.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void getsTheNetworkSecurityPerimeterConfigurationOfThePrivateLinkScope(
        com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.networkSecurityPerimeterConfigurations()
            .getByPrivateLinkScopeWithResponse("my-resource-group", "my-privatelinkscope",
                "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee.myAssociation", com.azure.core.util.Context.NONE);
    }
}
```

### NetworkSecurityPerimeterConfigurations_ListByPrivateLinkScope

```java
/**
 * Samples for NetworkSecurityPerimeterConfigurations ListByPrivateLinkScope.
 */
public final class NetworkSecurityPerimeterConfigurationsListByPrivateLinkScopeSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/
     * networkSecurityPerimeterConfiguration/NetworkSecurityPerimeterConfigurationList.json
     */
    /**
     * Sample code: Gets the list of network security perimeter configurations of the private link scope.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void getsTheListOfNetworkSecurityPerimeterConfigurationsOfThePrivateLinkScope(
        com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.networkSecurityPerimeterConfigurations()
            .listByPrivateLinkScope("my-resource-group", "my-privatelinkscope", com.azure.core.util.Context.NONE);
    }
}
```

### NetworkSecurityPerimeterConfigurations_ReconcileForPrivateLinkScope

```java
/**
 * Samples for NetworkSecurityPerimeterConfigurations ReconcileForPrivateLinkScope.
 */
public final class NetworkSecurityPerimeterConfigurationsReconcileForPrivateLinkScopeSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/
     * networkSecurityPerimeterConfiguration/NetworkSecurityPerimeterConfigurationReconcile.json
     */
    /**
     * Sample code: Reconciles the network security perimeter configuration of the private link scope.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void reconcilesTheNetworkSecurityPerimeterConfigurationOfThePrivateLinkScope(
        com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.networkSecurityPerimeterConfigurations()
            .reconcileForPrivateLinkScope("my-resource-group", "my-privatelinkscope",
                "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee.myAssociation", com.azure.core.util.Context.NONE);
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
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/
     * Operations_List.json
     */
    /**
     * Sample code: List Hybrid Compute Provider Operations.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void
        listHybridComputeProviderOperations(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_CreateOrUpdate

```java
import com.azure.resourcemanager.hybridcompute.models.PrivateEndpointConnectionProperties;
import com.azure.resourcemanager.hybridcompute.models.PrivateLinkServiceConnectionStateProperty;

/**
 * Samples for PrivateEndpointConnections CreateOrUpdate.
 */
public final class PrivateEndpointConnectionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/
     * privateEndpoint/PrivateEndpointConnection_Update.json
     */
    /**
     * Sample code: Approve or reject a private endpoint connection with a given name.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void approveOrRejectAPrivateEndpointConnectionWithAGivenName(
        com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.privateEndpointConnections()
            .define("private-endpoint-connection-name")
            .withExistingPrivateLinkScope("myResourceGroup", "myPrivateLinkScope")
            .withProperties(new PrivateEndpointConnectionProperties().withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionStateProperty().withStatus("Approved")
                    .withDescription("Approved by johndoe@contoso.com")))
            .create();
    }
}
```

### PrivateEndpointConnections_Delete

```java
/**
 * Samples for PrivateEndpointConnections Delete.
 */
public final class PrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/
     * privateEndpoint/PrivateEndpointConnection_Delete.json
     */
    /**
     * Sample code: Deletes a private endpoint connection with a given name.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void deletesAPrivateEndpointConnectionWithAGivenName(
        com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.privateEndpointConnections()
            .delete("myResourceGroup", "myPrivateLinkScope", "private-endpoint-connection-name",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
/**
 * Samples for PrivateEndpointConnections Get.
 */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/
     * privateEndpoint/PrivateEndpointConnection_Get.json
     */
    /**
     * Sample code: Gets private endpoint connection.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void
        getsPrivateEndpointConnection(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.privateEndpointConnections()
            .getWithResponse("myResourceGroup", "myPrivateLinkScope", "private-endpoint-connection-name",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_ListByPrivateLinkScope

```java
/**
 * Samples for PrivateEndpointConnections ListByPrivateLinkScope.
 */
public final class PrivateEndpointConnectionsListByPrivateLinkScopeSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/
     * privateEndpoint/PrivateEndpointConnection_List.json
     */
    /**
     * Sample code: Gets list of private endpoint connections on a private link scope.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void getsListOfPrivateEndpointConnectionsOnAPrivateLinkScope(
        com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.privateEndpointConnections()
            .listByPrivateLinkScope("myResourceGroup", "myPrivateLinkScope", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_Get

```java
/**
 * Samples for PrivateLinkResources Get.
 */
public final class PrivateLinkResourcesGetSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/
     * privateLinkScope/PrivateLinkScopePrivateLinkResource_Get.json
     */
    /**
     * Sample code: Gets private endpoint connection.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void
        getsPrivateEndpointConnection(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.privateLinkResources()
            .getWithResponse("myResourceGroup", "myPrivateLinkScope", "hybridcompute",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_ListByPrivateLinkScope

```java
/**
 * Samples for PrivateLinkResources ListByPrivateLinkScope.
 */
public final class PrivateLinkResourcesListByPrivateLinkScopeSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/
     * privateLinkScope/PrivateLinkScopePrivateLinkResource_ListGet.json
     */
    /**
     * Sample code: Gets private endpoint connection.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void
        getsPrivateEndpointConnection(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.privateLinkResources()
            .listByPrivateLinkScope("myResourceGroup", "myPrivateLinkScope", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkScopes_CreateOrUpdate

```java
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for PrivateLinkScopes CreateOrUpdate.
 */
public final class PrivateLinkScopesCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/
     * privateLinkScope/PrivateLinkScopes_Create.json
     */
    /**
     * Sample code: PrivateLinkScopeCreate.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void privateLinkScopeCreate(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.privateLinkScopes()
            .define("my-privatelinkscope")
            .withRegion("westus")
            .withExistingResourceGroup("my-resource-group")
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/
     * privateLinkScope/PrivateLinkScopes_Update.json
     */
    /**
     * Sample code: PrivateLinkScopeUpdate.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void privateLinkScopeUpdate(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.privateLinkScopes()
            .define("my-privatelinkscope")
            .withRegion("westus")
            .withExistingResourceGroup("my-resource-group")
            .withTags(mapOf("Tag1", "Value1"))
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

### PrivateLinkScopes_Delete

```java
/**
 * Samples for PrivateLinkScopes Delete.
 */
public final class PrivateLinkScopesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/
     * privateLinkScope/PrivateLinkScopes_Delete.json
     */
    /**
     * Sample code: PrivateLinkScopesDelete.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void privateLinkScopesDelete(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.privateLinkScopes()
            .delete("my-resource-group", "my-privatelinkscope", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkScopes_GetByResourceGroup

```java
/**
 * Samples for PrivateLinkScopes GetByResourceGroup.
 */
public final class PrivateLinkScopesGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/
     * privateLinkScope/PrivateLinkScopes_Get.json
     */
    /**
     * Sample code: PrivateLinkScopeGet.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void privateLinkScopeGet(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.privateLinkScopes()
            .getByResourceGroupWithResponse("my-resource-group", "my-privatelinkscope",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkScopes_GetValidationDetails

```java
/**
 * Samples for PrivateLinkScopes GetValidationDetails.
 */
public final class PrivateLinkScopesGetValidationDetailsSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/
     * privateLinkScope/PrivateLinkScopes_GetValidation.json
     */
    /**
     * Sample code: PrivateLinkScopeGet.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void privateLinkScopeGet(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.privateLinkScopes()
            .getValidationDetailsWithResponse("wus2", "f5dc51d3-92ed-4d7e-947a-775ea79b4919",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkScopes_GetValidationDetailsForMachine

```java
/**
 * Samples for PrivateLinkScopes GetValidationDetailsForMachine.
 */
public final class PrivateLinkScopesGetValidationDetailsForMachineSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/
     * privateLinkScope/PrivateLinkScopes_GetValidationForMachine.json
     */
    /**
     * Sample code: PrivateLinkScopeGet.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void privateLinkScopeGet(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.privateLinkScopes()
            .getValidationDetailsForMachineWithResponse("my-resource-group", "machineName",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkScopes_List

```java
/**
 * Samples for PrivateLinkScopes List.
 */
public final class PrivateLinkScopesListSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/
     * privateLinkScope/PrivateLinkScopes_List.json
     */
    /**
     * Sample code: PrivateLinkScopesList.json.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void privateLinkScopesListJson(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.privateLinkScopes().list(com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkScopes_ListByResourceGroup

```java
/**
 * Samples for PrivateLinkScopes ListByResourceGroup.
 */
public final class PrivateLinkScopesListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/
     * privateLinkScope/PrivateLinkScopes_ListByResourceGroup.json
     */
    /**
     * Sample code: PrivateLinkScopeListByResourceGroup.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void
        privateLinkScopeListByResourceGroup(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.privateLinkScopes().listByResourceGroup("my-resource-group", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkScopes_UpdateTags

```java
import com.azure.resourcemanager.hybridcompute.models.HybridComputePrivateLinkScope;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for PrivateLinkScopes UpdateTags.
 */
public final class PrivateLinkScopesUpdateTagsSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/
     * privateLinkScope/PrivateLinkScopes_UpdateTagsOnly.json
     */
    /**
     * Sample code: PrivateLinkScopeUpdateTagsOnly.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void
        privateLinkScopeUpdateTagsOnly(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        HybridComputePrivateLinkScope resource = manager.privateLinkScopes()
            .getByResourceGroupWithResponse("my-resource-group", "my-privatelinkscope",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("Tag1", "Value1", "Tag2", "Value2")).apply();
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

### ResourceProvider_UpgradeExtensions

```java
import com.azure.resourcemanager.hybridcompute.models.ExtensionTargetProperties;
import com.azure.resourcemanager.hybridcompute.models.MachineExtensionUpgrade;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ResourceProvider UpgradeExtensions.
 */
public final class ResourceProviderUpgradeExtensionsSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/
     * extension/Extensions_Upgrade.json
     */
    /**
     * Sample code: Upgrade Machine Extensions.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void upgradeMachineExtensions(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.resourceProviders()
            .upgradeExtensions("myResourceGroup", "myMachine",
                new MachineExtensionUpgrade().withExtensionTargets(mapOf("Microsoft.Azure.Monitoring",
                    new ExtensionTargetProperties().withTargetVersion("2.0"), "Microsoft.Compute.CustomScriptExtension",
                    new ExtensionTargetProperties().withTargetVersion("1.10"))),
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

### SettingsOperation_Get

```java
/**
 * Samples for SettingsOperation Get.
 */
public final class SettingsOperationGetSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/settings
     * /SettingsGet.json
     */
    /**
     * Sample code: NetworkConfigurationsGet.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void networkConfigurationsGet(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.settingsOperations()
            .getWithResponse("hybridRG", "Microsoft.HybridCompute", "machines", "testMachine", "default",
                com.azure.core.util.Context.NONE);
    }
}
```

### SettingsOperation_Patch

```java
import com.azure.resourcemanager.hybridcompute.fluent.models.SettingsInner;

/**
 * Samples for SettingsOperation Patch.
 */
public final class SettingsOperationPatchSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/settings
     * /SettingsPatch.json
     */
    /**
     * Sample code: NetworkConfigurationsPatch.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void
        networkConfigurationsPatch(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.settingsOperations()
            .patchWithResponse("hybridRG", "Microsoft.HybridCompute", "machines", "testMachine", "default",
                new SettingsInner().withGatewayResourceId(
                    "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/hybridRG/providers/Microsoft.HybridCompute/gateways/newGateway"),
                com.azure.core.util.Context.NONE);
    }
}
```

### SettingsOperation_Update

```java
import com.azure.resourcemanager.hybridcompute.fluent.models.SettingsInner;

/**
 * Samples for SettingsOperation Update.
 */
public final class SettingsOperationUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2024-05-20-preview/examples/settings
     * /SettingsUpdate.json
     */
    /**
     * Sample code: SettingsUpdate.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void settingsUpdate(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.settingsOperations()
            .updateWithResponse("hybridRG", "Microsoft.HybridCompute", "machines", "testMachine", "default",
                new SettingsInner().withGatewayResourceId(
                    "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/hybridRG/providers/Microsoft.HybridCompute/gateways/newGateway"),
                com.azure.core.util.Context.NONE);
    }
}
```


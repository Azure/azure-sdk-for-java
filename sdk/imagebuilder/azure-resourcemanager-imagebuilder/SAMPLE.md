# Code snippets and samples


## Operations

- [List](#operations_list)

## VirtualMachineImageTemplates

- [Cancel](#virtualmachineimagetemplates_cancel)
- [CreateOrUpdate](#virtualmachineimagetemplates_createorupdate)
- [Delete](#virtualmachineimagetemplates_delete)
- [GetByResourceGroup](#virtualmachineimagetemplates_getbyresourcegroup)
- [GetRunOutput](#virtualmachineimagetemplates_getrunoutput)
- [List](#virtualmachineimagetemplates_list)
- [ListByResourceGroup](#virtualmachineimagetemplates_listbyresourcegroup)
- [ListRunOutputs](#virtualmachineimagetemplates_listrunoutputs)
- [Run](#virtualmachineimagetemplates_run)
- [Update](#virtualmachineimagetemplates_update)
### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/imagebuilder/resource-manager/Microsoft.VirtualMachineImages/stable/2022-02-14/examples/OperationsList.json
     */
    /**
     * Sample code: Retrieve operations list.
     *
     * @param manager Entry point to ImageBuilderManager.
     */
    public static void retrieveOperationsList(com.azure.resourcemanager.imagebuilder.ImageBuilderManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### VirtualMachineImageTemplates_Cancel

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachineImageTemplates Cancel. */
public final class VirtualMachineImageTemplatesCancelSamples {
    /*
     * x-ms-original-file: specification/imagebuilder/resource-manager/Microsoft.VirtualMachineImages/stable/2022-02-14/examples/CancelImageBuild.json
     */
    /**
     * Sample code: Cancel the image build based on the imageTemplate.
     *
     * @param manager Entry point to ImageBuilderManager.
     */
    public static void cancelTheImageBuildBasedOnTheImageTemplate(
        com.azure.resourcemanager.imagebuilder.ImageBuilderManager manager) {
        manager.virtualMachineImageTemplates().cancel("myResourceGroup", "myImageTemplate", Context.NONE);
    }
}
```

### VirtualMachineImageTemplates_CreateOrUpdate

```java
import com.azure.resourcemanager.imagebuilder.models.ImageTemplateIdentity;
import com.azure.resourcemanager.imagebuilder.models.ImageTemplateIdentityUserAssignedIdentities;
import com.azure.resourcemanager.imagebuilder.models.ImageTemplateManagedImageDistributor;
import com.azure.resourcemanager.imagebuilder.models.ImageTemplateManagedImageSource;
import com.azure.resourcemanager.imagebuilder.models.ImageTemplatePowerShellCustomizer;
import com.azure.resourcemanager.imagebuilder.models.ImageTemplateRestartCustomizer;
import com.azure.resourcemanager.imagebuilder.models.ImageTemplateShellCustomizer;
import com.azure.resourcemanager.imagebuilder.models.ImageTemplateVmProfile;
import com.azure.resourcemanager.imagebuilder.models.ImageTemplateWindowsUpdateCustomizer;
import com.azure.resourcemanager.imagebuilder.models.ResourceIdentityType;
import com.azure.resourcemanager.imagebuilder.models.VirtualNetworkConfig;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for VirtualMachineImageTemplates CreateOrUpdate. */
public final class VirtualMachineImageTemplatesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/imagebuilder/resource-manager/Microsoft.VirtualMachineImages/stable/2022-02-14/examples/CreateImageTemplateLinux.json
     */
    /**
     * Sample code: Create an Image Template for Linux.
     *
     * @param manager Entry point to ImageBuilderManager.
     */
    public static void createAnImageTemplateForLinux(
        com.azure.resourcemanager.imagebuilder.ImageBuilderManager manager) {
        manager
            .virtualMachineImageTemplates()
            .define("myImageTemplate")
            .withRegion("westus")
            .withExistingResourceGroup("myResourceGroup")
            .withIdentity(
                new ImageTemplateIdentity()
                    .withType(ResourceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/rg1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/identity_1",
                            new ImageTemplateIdentityUserAssignedIdentities())))
            .withTags(mapOf("imagetemplate_tag1", "IT_T1", "imagetemplate_tag2", "IT_T2"))
            .withSource(
                new ImageTemplateManagedImageSource()
                    .withImageId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg1/providers/Microsoft.Compute/images/source_image"))
            .withCustomize(
                Arrays
                    .asList(
                        new ImageTemplateShellCustomizer()
                            .withName("Shell Customizer Example")
                            .withScriptUri("https://example.com/path/to/script.sh")))
            .withDistribute(
                Arrays
                    .asList(
                        new ImageTemplateManagedImageDistributor()
                            .withRunOutputName("image_it_pir_1")
                            .withArtifactTags(mapOf("tagName", "value"))
                            .withImageId(
                                "/subscriptions/{subscription-id}/resourceGroups/rg1/providers/Microsoft.Compute/images/image_it_1")
                            .withLocation("1_location")))
            .withVmProfile(
                new ImageTemplateVmProfile()
                    .withVmSize("Standard_D2s_v3")
                    .withOsDiskSizeGB(64)
                    .withVnetConfig(
                        new VirtualNetworkConfig()
                            .withSubnetId(
                                "/subscriptions/{subscription-id}/resourceGroups/rg1/providers/Microsoft.Network/virtualNetworks/vnet_name/subnets/subnet_name")))
            .create();
    }

    /*
     * x-ms-original-file: specification/imagebuilder/resource-manager/Microsoft.VirtualMachineImages/stable/2022-02-14/examples/CreateImageTemplateWindows.json
     */
    /**
     * Sample code: Create an Image Template for Windows.
     *
     * @param manager Entry point to ImageBuilderManager.
     */
    public static void createAnImageTemplateForWindows(
        com.azure.resourcemanager.imagebuilder.ImageBuilderManager manager) {
        manager
            .virtualMachineImageTemplates()
            .define("myImageTemplate")
            .withRegion("westus")
            .withExistingResourceGroup("myResourceGroup")
            .withIdentity(
                new ImageTemplateIdentity()
                    .withType(ResourceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/rg1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/identity_1",
                            new ImageTemplateIdentityUserAssignedIdentities())))
            .withTags(mapOf("imagetemplate_tag1", "IT_T1", "imagetemplate_tag2", "IT_T2"))
            .withSource(
                new ImageTemplateManagedImageSource()
                    .withImageId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg1/providers/Microsoft.Compute/images/source_image"))
            .withCustomize(
                Arrays
                    .asList(
                        new ImageTemplatePowerShellCustomizer()
                            .withName("PowerShell (inline) Customizer Example")
                            .withInline(
                                Arrays.asList("Powershell command-1", "Powershell command-2", "Powershell command-3")),
                        new ImageTemplatePowerShellCustomizer()
                            .withName("PowerShell (inline) Customizer Elevated user Example")
                            .withInline(
                                Arrays.asList("Powershell command-1", "Powershell command-2", "Powershell command-3"))
                            .withRunElevated(true),
                        new ImageTemplatePowerShellCustomizer()
                            .withName("PowerShell (inline) Customizer Elevated Local System user Example")
                            .withInline(
                                Arrays.asList("Powershell command-1", "Powershell command-2", "Powershell command-3"))
                            .withRunElevated(true)
                            .withRunAsSystem(true),
                        new ImageTemplatePowerShellCustomizer()
                            .withName("PowerShell (script) Customizer Example")
                            .withScriptUri("https://example.com/path/to/script.ps1")
                            .withValidExitCodes(Arrays.asList(0, 1)),
                        new ImageTemplatePowerShellCustomizer()
                            .withName("PowerShell (script) Customizer Elevated Local System user Example")
                            .withScriptUri("https://example.com/path/to/script.ps1")
                            .withRunElevated(true)
                            .withValidExitCodes(Arrays.asList(0, 1)),
                        new ImageTemplatePowerShellCustomizer()
                            .withName("PowerShell (script) Customizer Elevated Local System user Example")
                            .withScriptUri("https://example.com/path/to/script.ps1")
                            .withRunElevated(true)
                            .withRunAsSystem(true)
                            .withValidExitCodes(Arrays.asList(0, 1)),
                        new ImageTemplateRestartCustomizer()
                            .withName("Restart Customizer Example")
                            .withRestartCommand("shutdown /f /r /t 0 /c \"packer restart\"")
                            .withRestartCheckCommand("powershell -command \"& {Write-Output 'restarted.'}\"")
                            .withRestartTimeout("10m"),
                        new ImageTemplateWindowsUpdateCustomizer()
                            .withName("Windows Update Customizer Example")
                            .withSearchCriteria("BrowseOnly=0 and IsInstalled=0")
                            .withFilters(Arrays.asList("$_.BrowseOnly"))
                            .withUpdateLimit(100)))
            .withDistribute(
                Arrays
                    .asList(
                        new ImageTemplateManagedImageDistributor()
                            .withRunOutputName("image_it_pir_1")
                            .withArtifactTags(mapOf("tagName", "value"))
                            .withImageId(
                                "/subscriptions/{subscription-id}/resourceGroups/rg1/providers/Microsoft.Compute/images/image_it_1")
                            .withLocation("1_location")))
            .withVmProfile(
                new ImageTemplateVmProfile()
                    .withVmSize("Standard_D2s_v3")
                    .withOsDiskSizeGB(64)
                    .withVnetConfig(
                        new VirtualNetworkConfig()
                            .withSubnetId(
                                "/subscriptions/{subscription-id}/resourceGroups/rg1/providers/Microsoft.Network/virtualNetworks/vnet_name/subnets/subnet_name")))
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

### VirtualMachineImageTemplates_Delete

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachineImageTemplates Delete. */
public final class VirtualMachineImageTemplatesDeleteSamples {
    /*
     * x-ms-original-file: specification/imagebuilder/resource-manager/Microsoft.VirtualMachineImages/stable/2022-02-14/examples/DeleteImageTemplate.json
     */
    /**
     * Sample code: Delete an Image Template.
     *
     * @param manager Entry point to ImageBuilderManager.
     */
    public static void deleteAnImageTemplate(com.azure.resourcemanager.imagebuilder.ImageBuilderManager manager) {
        manager.virtualMachineImageTemplates().delete("myResourceGroup", "myImageTemplate", Context.NONE);
    }
}
```

### VirtualMachineImageTemplates_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachineImageTemplates GetByResourceGroup. */
public final class VirtualMachineImageTemplatesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/imagebuilder/resource-manager/Microsoft.VirtualMachineImages/stable/2022-02-14/examples/GetImageTemplate.json
     */
    /**
     * Sample code: Retrieve an Image Template.
     *
     * @param manager Entry point to ImageBuilderManager.
     */
    public static void retrieveAnImageTemplate(com.azure.resourcemanager.imagebuilder.ImageBuilderManager manager) {
        manager
            .virtualMachineImageTemplates()
            .getByResourceGroupWithResponse("myResourceGroup", "myImageTemplate", Context.NONE);
    }
}
```

### VirtualMachineImageTemplates_GetRunOutput

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachineImageTemplates GetRunOutput. */
public final class VirtualMachineImageTemplatesGetRunOutputSamples {
    /*
     * x-ms-original-file: specification/imagebuilder/resource-manager/Microsoft.VirtualMachineImages/stable/2022-02-14/examples/GetRunOutput.json
     */
    /**
     * Sample code: Retrieve single runOutput.
     *
     * @param manager Entry point to ImageBuilderManager.
     */
    public static void retrieveSingleRunOutput(com.azure.resourcemanager.imagebuilder.ImageBuilderManager manager) {
        manager
            .virtualMachineImageTemplates()
            .getRunOutputWithResponse("myResourceGroup", "myImageTemplate", "myManagedImageOutput", Context.NONE);
    }
}
```

### VirtualMachineImageTemplates_List

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachineImageTemplates List. */
public final class VirtualMachineImageTemplatesListSamples {
    /*
     * x-ms-original-file: specification/imagebuilder/resource-manager/Microsoft.VirtualMachineImages/stable/2022-02-14/examples/ListImageTemplates.json
     */
    /**
     * Sample code: List images by subscription.
     *
     * @param manager Entry point to ImageBuilderManager.
     */
    public static void listImagesBySubscription(com.azure.resourcemanager.imagebuilder.ImageBuilderManager manager) {
        manager.virtualMachineImageTemplates().list(Context.NONE);
    }
}
```

### VirtualMachineImageTemplates_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachineImageTemplates ListByResourceGroup. */
public final class VirtualMachineImageTemplatesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/imagebuilder/resource-manager/Microsoft.VirtualMachineImages/stable/2022-02-14/examples/ListImageTemplatesByRg.json
     */
    /**
     * Sample code: List images by resource group.
     *
     * @param manager Entry point to ImageBuilderManager.
     */
    public static void listImagesByResourceGroup(com.azure.resourcemanager.imagebuilder.ImageBuilderManager manager) {
        manager.virtualMachineImageTemplates().listByResourceGroup("myResourceGroup", Context.NONE);
    }
}
```

### VirtualMachineImageTemplates_ListRunOutputs

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachineImageTemplates ListRunOutputs. */
public final class VirtualMachineImageTemplatesListRunOutputsSamples {
    /*
     * x-ms-original-file: specification/imagebuilder/resource-manager/Microsoft.VirtualMachineImages/stable/2022-02-14/examples/ListRunOutputs.json
     */
    /**
     * Sample code: Retrieve a list of all outputs created by the last run of an Image Template.
     *
     * @param manager Entry point to ImageBuilderManager.
     */
    public static void retrieveAListOfAllOutputsCreatedByTheLastRunOfAnImageTemplate(
        com.azure.resourcemanager.imagebuilder.ImageBuilderManager manager) {
        manager.virtualMachineImageTemplates().listRunOutputs("myResourceGroup", "myImageTemplate", Context.NONE);
    }
}
```

### VirtualMachineImageTemplates_Run

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachineImageTemplates Run. */
public final class VirtualMachineImageTemplatesRunSamples {
    /*
     * x-ms-original-file: specification/imagebuilder/resource-manager/Microsoft.VirtualMachineImages/stable/2022-02-14/examples/RunImageTemplate.json
     */
    /**
     * Sample code: Create image(s) from existing imageTemplate.
     *
     * @param manager Entry point to ImageBuilderManager.
     */
    public static void createImageSFromExistingImageTemplate(
        com.azure.resourcemanager.imagebuilder.ImageBuilderManager manager) {
        manager.virtualMachineImageTemplates().run("myResourceGroup", "myImageTemplate", Context.NONE);
    }
}
```

### VirtualMachineImageTemplates_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.imagebuilder.models.ImageTemplate;
import com.azure.resourcemanager.imagebuilder.models.ImageTemplateIdentity;
import com.azure.resourcemanager.imagebuilder.models.ResourceIdentityType;
import java.util.HashMap;
import java.util.Map;

/** Samples for VirtualMachineImageTemplates Update. */
public final class VirtualMachineImageTemplatesUpdateSamples {
    /*
     * x-ms-original-file: specification/imagebuilder/resource-manager/Microsoft.VirtualMachineImages/stable/2022-02-14/examples/UpdateImageTemplateToRemoveIdentities.json
     */
    /**
     * Sample code: Remove identities for an Image Template.
     *
     * @param manager Entry point to ImageBuilderManager.
     */
    public static void removeIdentitiesForAnImageTemplate(
        com.azure.resourcemanager.imagebuilder.ImageBuilderManager manager) {
        ImageTemplate resource =
            manager
                .virtualMachineImageTemplates()
                .getByResourceGroupWithResponse("myResourceGroup", "myImageTemplate", Context.NONE)
                .getValue();
        resource.update().withIdentity(new ImageTemplateIdentity().withType(ResourceIdentityType.NONE)).apply();
    }

    /*
     * x-ms-original-file: specification/imagebuilder/resource-manager/Microsoft.VirtualMachineImages/stable/2022-02-14/examples/UpdateImageTemplateTags.json
     */
    /**
     * Sample code: Update the tags for an Image Template.
     *
     * @param manager Entry point to ImageBuilderManager.
     */
    public static void updateTheTagsForAnImageTemplate(
        com.azure.resourcemanager.imagebuilder.ImageBuilderManager manager) {
        ImageTemplate resource =
            manager
                .virtualMachineImageTemplates()
                .getByResourceGroupWithResponse("myResourceGroup", "myImageTemplate", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("new-tag", "new-value")).apply();
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


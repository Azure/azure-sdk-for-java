# Code snippets and samples


## GuestConfigurationAssignmentReports

- [Get](#guestconfigurationassignmentreports_get)
- [List](#guestconfigurationassignmentreports_list)

## GuestConfigurationAssignmentReportsVMSS

- [Get](#guestconfigurationassignmentreportsvmss_get)
- [List](#guestconfigurationassignmentreportsvmss_list)

## GuestConfigurationAssignments

- [CreateOrUpdate](#guestconfigurationassignments_createorupdate)
- [Delete](#guestconfigurationassignments_delete)
- [Get](#guestconfigurationassignments_get)
- [List](#guestconfigurationassignments_list)
- [ListByResourceGroup](#guestconfigurationassignments_listbyresourcegroup)
- [SubscriptionList](#guestconfigurationassignments_subscriptionlist)

## GuestConfigurationAssignmentsVMSS

- [CreateOrUpdate](#guestconfigurationassignmentsvmss_createorupdate)
- [Delete](#guestconfigurationassignmentsvmss_delete)
- [Get](#guestconfigurationassignmentsvmss_get)
- [List](#guestconfigurationassignmentsvmss_list)

## GuestConfigurationConnectedVMwarevSphereAssignments

- [CreateOrUpdate](#guestconfigurationconnectedvmwarevsphereassignments_createorupdate)
- [Delete](#guestconfigurationconnectedvmwarevsphereassignments_delete)
- [Get](#guestconfigurationconnectedvmwarevsphereassignments_get)
- [List](#guestconfigurationconnectedvmwarevsphereassignments_list)

## GuestConfigurationConnectedVMwarevSphereAssignmentsReports

- [Get](#guestconfigurationconnectedvmwarevsphereassignmentsreports_get)
- [List](#guestconfigurationconnectedvmwarevsphereassignmentsreports_list)

## GuestConfigurationHCRPAssignmentReports

- [Get](#guestconfigurationhcrpassignmentreports_get)
- [List](#guestconfigurationhcrpassignmentreports_list)

## GuestConfigurationHCRPAssignments

- [CreateOrUpdate](#guestconfigurationhcrpassignments_createorupdate)
- [Delete](#guestconfigurationhcrpassignments_delete)
- [Get](#guestconfigurationhcrpassignments_get)
- [List](#guestconfigurationhcrpassignments_list)

## Operations

- [List](#operations_list)
### GuestConfigurationAssignmentReports_Get

```java
/**
 * Samples for GuestConfigurationAssignmentReports Get.
 */
public final class GuestConfigurationAssignmentReportsGetSamples {
    /*
     * x-ms-original-file: 2024-04-05/getGuestConfigurationAssignmentReportById.json
     */
    /**
     * Sample code: Get a guest configuration assignment report by Id for a virtual machine.
     * 
     * @param manager Entry point to GuestConfigurationManager.
     */
    public static void getAGuestConfigurationAssignmentReportByIdForAVirtualMachine(
        com.azure.resourcemanager.guestconfiguration.GuestConfigurationManager manager) {
        manager.guestConfigurationAssignmentReports()
            .getWithResponse("myResourceGroupName", "myvm", "AuditSecureProtocol",
                "7367cbb8-ae99-47d0-a33b-a283564d2cb1", com.azure.core.util.Context.NONE);
    }
}
```

### GuestConfigurationAssignmentReports_List

```java
/**
 * Samples for GuestConfigurationAssignmentReports List.
 */
public final class GuestConfigurationAssignmentReportsListSamples {
    /*
     * x-ms-original-file: 2024-04-05/listAllGuestConfigurationAssignmentReports.json
     */
    /**
     * Sample code: List all guest configuration assignments for a virtual machine.
     * 
     * @param manager Entry point to GuestConfigurationManager.
     */
    public static void listAllGuestConfigurationAssignmentsForAVirtualMachine(
        com.azure.resourcemanager.guestconfiguration.GuestConfigurationManager manager) {
        manager.guestConfigurationAssignmentReports()
            .listWithResponse("myResourceGroupName", "myVMName", "AuditSecureProtocol",
                com.azure.core.util.Context.NONE);
    }
}
```

### GuestConfigurationAssignmentReportsVMSS_Get

```java
/**
 * Samples for GuestConfigurationAssignmentReportsVMSS Get.
 */
public final class GuestConfigurationAssignmentReportsVMSSGetSamples {
    /*
     * x-ms-original-file: 2024-04-05/getVMSSGuestConfigurationAssignmentReportById.json
     */
    /**
     * Sample code: Get a guest configuration assignment report by Id for a virtual machine scale set.
     * 
     * @param manager Entry point to GuestConfigurationManager.
     */
    public static void getAGuestConfigurationAssignmentReportByIdForAVirtualMachineScaleSet(
        com.azure.resourcemanager.guestconfiguration.GuestConfigurationManager manager) {
        manager.guestConfigurationAssignmentReportsVMSS()
            .getWithResponse("myResourceGroupName", "myvmss", "AuditSecureProtocol",
                "7367cbb8-ae99-47d0-a33b-a283564d2cb1", com.azure.core.util.Context.NONE);
    }
}
```

### GuestConfigurationAssignmentReportsVMSS_List

```java
/**
 * Samples for GuestConfigurationAssignmentReportsVMSS List.
 */
public final class GuestConfigurationAssignmentReportsVMSSListSamples {
    /*
     * x-ms-original-file: 2024-04-05/listAllVMSSGuestConfigurationAssignmentReports.json
     */
    /**
     * Sample code: List all reports for the VMSS guest configuration assignment with latest report first.
     * 
     * @param manager Entry point to GuestConfigurationManager.
     */
    public static void listAllReportsForTheVMSSGuestConfigurationAssignmentWithLatestReportFirst(
        com.azure.resourcemanager.guestconfiguration.GuestConfigurationManager manager) {
        manager.guestConfigurationAssignmentReportsVMSS()
            .list("myResourceGroupName", "myVMSSName", "AuditSecureProtocol", com.azure.core.util.Context.NONE);
    }
}
```

### GuestConfigurationAssignments_CreateOrUpdate

```java
import com.azure.resourcemanager.guestconfiguration.models.AssignmentType;
import com.azure.resourcemanager.guestconfiguration.models.ConfigurationParameter;
import com.azure.resourcemanager.guestconfiguration.models.GuestConfigurationAssignmentProperties;
import com.azure.resourcemanager.guestconfiguration.models.GuestConfigurationNavigation;
import java.util.Arrays;

/**
 * Samples for GuestConfigurationAssignments CreateOrUpdate.
 */
public final class GuestConfigurationAssignmentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-04-05/createOrUpdateGuestConfigurationAssignment.json
     */
    /**
     * Sample code: Create or update guest configuration assignment.
     * 
     * @param manager Entry point to GuestConfigurationManager.
     */
    public static void createOrUpdateGuestConfigurationAssignment(
        com.azure.resourcemanager.guestconfiguration.GuestConfigurationManager manager) {
        manager.guestConfigurationAssignments()
            .define("NotInstalledApplicationForWindows")
            .withExistingVirtualMachine("myResourceGroupName", "myVMName")
            .withRegion("westcentralus")
            .withProperties(new GuestConfigurationAssignmentProperties()
                .withGuestConfiguration(new GuestConfigurationNavigation().withName("NotInstalledApplicationForWindows")
                    .withVersion("1.0.0.3")
                    .withContentUri("https://thisisfake/pacakge")
                    .withContentHash("123contenthash")
                    .withContentManagedIdentity("test_identity")
                    .withAssignmentType(AssignmentType.APPLY_AND_AUTO_CORRECT)
                    .withConfigurationParameter(Arrays.asList(new ConfigurationParameter()
                        .withName("[InstalledApplication]NotInstalledApplicationResource1;Name")
                        .withValue("NotePad,sql"))))
                .withContext("Azure policy"))
            .create();
    }
}
```

### GuestConfigurationAssignments_Delete

```java
/**
 * Samples for GuestConfigurationAssignments Delete.
 */
public final class GuestConfigurationAssignmentsDeleteSamples {
    /*
     * x-ms-original-file: 2024-04-05/deleteGuestConfigurationAssignment.json
     */
    /**
     * Sample code: Delete an guest configuration assignment.
     * 
     * @param manager Entry point to GuestConfigurationManager.
     */
    public static void deleteAnGuestConfigurationAssignment(
        com.azure.resourcemanager.guestconfiguration.GuestConfigurationManager manager) {
        manager.guestConfigurationAssignments()
            .deleteWithResponse("myResourceGroupName", "myVMName", "SecureProtocol", com.azure.core.util.Context.NONE);
    }
}
```

### GuestConfigurationAssignments_Get

```java
/**
 * Samples for GuestConfigurationAssignments Get.
 */
public final class GuestConfigurationAssignmentsGetSamples {
    /*
     * x-ms-original-file: 2024-04-05/getGuestConfigurationAssignment.json
     */
    /**
     * Sample code: Get a guest configuration assignment.
     * 
     * @param manager Entry point to GuestConfigurationManager.
     */
    public static void getAGuestConfigurationAssignment(
        com.azure.resourcemanager.guestconfiguration.GuestConfigurationManager manager) {
        manager.guestConfigurationAssignments()
            .getWithResponse("myResourceGroupName", "myVMName", "SecureProtocol", com.azure.core.util.Context.NONE);
    }
}
```

### GuestConfigurationAssignments_List

```java
/**
 * Samples for GuestConfigurationAssignments List.
 */
public final class GuestConfigurationAssignmentsListSamples {
    /*
     * x-ms-original-file: 2024-04-05/listGuestConfigurationAssignments.json
     */
    /**
     * Sample code: List all guest configuration assignments for a virtual machine.
     * 
     * @param manager Entry point to GuestConfigurationManager.
     */
    public static void listAllGuestConfigurationAssignmentsForAVirtualMachine(
        com.azure.resourcemanager.guestconfiguration.GuestConfigurationManager manager) {
        manager.guestConfigurationAssignments()
            .list("myResourceGroupName", "myVMName", com.azure.core.util.Context.NONE);
    }
}
```

### GuestConfigurationAssignments_ListByResourceGroup

```java
/**
 * Samples for GuestConfigurationAssignments ListByResourceGroup.
 */
public final class GuestConfigurationAssignmentsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-04-05/listRGGuestConfigurationAssignments.json
     */
    /**
     * Sample code: List all guest configuration assignments for a resource group.
     * 
     * @param manager Entry point to GuestConfigurationManager.
     */
    public static void listAllGuestConfigurationAssignmentsForAResourceGroup(
        com.azure.resourcemanager.guestconfiguration.GuestConfigurationManager manager) {
        manager.guestConfigurationAssignments()
            .listByResourceGroup("myResourceGroupName", com.azure.core.util.Context.NONE);
    }
}
```

### GuestConfigurationAssignments_SubscriptionList

```java
/**
 * Samples for GuestConfigurationAssignments SubscriptionList.
 */
public final class GuestConfigurationAssignmentsSubscriptionListSamples {
    /*
     * x-ms-original-file: 2024-04-05/listSubGuestConfigurationAssignments.json
     */
    /**
     * Sample code: List all guest configuration assignments for a subscription.
     * 
     * @param manager Entry point to GuestConfigurationManager.
     */
    public static void listAllGuestConfigurationAssignmentsForASubscription(
        com.azure.resourcemanager.guestconfiguration.GuestConfigurationManager manager) {
        manager.guestConfigurationAssignments().subscriptionList(com.azure.core.util.Context.NONE);
    }
}
```

### GuestConfigurationAssignmentsVMSS_CreateOrUpdate

```java
import com.azure.resourcemanager.guestconfiguration.fluent.models.GuestConfigurationAssignmentInner;
import com.azure.resourcemanager.guestconfiguration.models.AssignmentType;
import com.azure.resourcemanager.guestconfiguration.models.ConfigurationParameter;
import com.azure.resourcemanager.guestconfiguration.models.GuestConfigurationAssignmentProperties;
import com.azure.resourcemanager.guestconfiguration.models.GuestConfigurationNavigation;
import java.util.Arrays;

/**
 * Samples for GuestConfigurationAssignmentsVMSS CreateOrUpdate.
 */
public final class GuestConfigurationAssignmentsVMSSCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-04-05/createOrUpdateGuestConfigurationVMSSAssignment.json
     */
    /**
     * Sample code: Create or update guest configuration assignment.
     * 
     * @param manager Entry point to GuestConfigurationManager.
     */
    public static void createOrUpdateGuestConfigurationAssignment(
        com.azure.resourcemanager.guestconfiguration.GuestConfigurationManager manager) {
        manager.guestConfigurationAssignmentsVMSS()
            .createOrUpdateWithResponse("myResourceGroupName", "myVMSSName", "NotInstalledApplicationForWindows",
                new GuestConfigurationAssignmentInner()
                    .withProperties(
                        new GuestConfigurationAssignmentProperties()
                            .withGuestConfiguration(
                                new GuestConfigurationNavigation().withName("NotInstalledApplicationForWindows")
                                    .withVersion("1.0.0.3")
                                    .withContentUri("https://thisisfake/pacakge")
                                    .withContentHash("123contenthash")
                                    .withContentManagedIdentity("test_identity")
                                    .withAssignmentType(AssignmentType.APPLY_AND_AUTO_CORRECT)
                                    .withConfigurationParameter(Arrays.asList(new ConfigurationParameter()
                                        .withName("[InstalledApplication]NotInstalledApplicationResource1;Name")
                                        .withValue("NotePad,sql"))))
                            .withContext("Azure policy"))
                    .withLocation("westcentralus"),
                com.azure.core.util.Context.NONE);
    }
}
```

### GuestConfigurationAssignmentsVMSS_Delete

```java
/**
 * Samples for GuestConfigurationAssignmentsVMSS Delete.
 */
public final class GuestConfigurationAssignmentsVMSSDeleteSamples {
    /*
     * x-ms-original-file: 2024-04-05/deleteGuestConfigurationVMSSAssignment.json
     */
    /**
     * Sample code: Delete an guest configuration assignment for VMSS.
     * 
     * @param manager Entry point to GuestConfigurationManager.
     */
    public static void deleteAnGuestConfigurationAssignmentForVMSS(
        com.azure.resourcemanager.guestconfiguration.GuestConfigurationManager manager) {
        manager.guestConfigurationAssignmentsVMSS()
            .deleteWithResponse("myResourceGroupName", "myVMSSName", "SecureProtocol",
                com.azure.core.util.Context.NONE);
    }
}
```

### GuestConfigurationAssignmentsVMSS_Get

```java
/**
 * Samples for GuestConfigurationAssignmentsVMSS Get.
 */
public final class GuestConfigurationAssignmentsVMSSGetSamples {
    /*
     * x-ms-original-file: 2024-04-05/getVMSSGuestConfigurationAssignment.json
     */
    /**
     * Sample code: Get a VMSS guest configuration assignment.
     * 
     * @param manager Entry point to GuestConfigurationManager.
     */
    public static void getAVMSSGuestConfigurationAssignment(
        com.azure.resourcemanager.guestconfiguration.GuestConfigurationManager manager) {
        manager.guestConfigurationAssignmentsVMSS()
            .getWithResponse("myResourceGroupName", "myVMSSName", "SecureProtocol", com.azure.core.util.Context.NONE);
    }
}
```

### GuestConfigurationAssignmentsVMSS_List

```java
/**
 * Samples for GuestConfigurationAssignmentsVMSS List.
 */
public final class GuestConfigurationAssignmentsVMSSListSamples {
    /*
     * x-ms-original-file: 2024-04-05/listVMSSGuestConfigurationAssignments.json
     */
    /**
     * Sample code: List all guest configuration assignments for VMSS.
     * 
     * @param manager Entry point to GuestConfigurationManager.
     */
    public static void listAllGuestConfigurationAssignmentsForVMSS(
        com.azure.resourcemanager.guestconfiguration.GuestConfigurationManager manager) {
        manager.guestConfigurationAssignmentsVMSS()
            .list("myResourceGroupName", "myVMSSName", com.azure.core.util.Context.NONE);
    }
}
```

### GuestConfigurationConnectedVMwarevSphereAssignments_CreateOrUpdate

```java
import com.azure.resourcemanager.guestconfiguration.fluent.models.GuestConfigurationAssignmentInner;
import com.azure.resourcemanager.guestconfiguration.models.AssignmentType;
import com.azure.resourcemanager.guestconfiguration.models.ConfigurationParameter;
import com.azure.resourcemanager.guestconfiguration.models.GuestConfigurationAssignmentProperties;
import com.azure.resourcemanager.guestconfiguration.models.GuestConfigurationNavigation;
import java.util.Arrays;

/**
 * Samples for GuestConfigurationConnectedVMwarevSphereAssignments CreateOrUpdate.
 */
public final class GuestConfigurationConnectedVMwarevSphereAssignmentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-04-05/createOrUpdateGuestConfigurationConnectedVMwarevSphereAssignment.json
     */
    /**
     * Sample code: Create or update guest configuration assignment.
     * 
     * @param manager Entry point to GuestConfigurationManager.
     */
    public static void createOrUpdateGuestConfigurationAssignment(
        com.azure.resourcemanager.guestconfiguration.GuestConfigurationManager manager) {
        manager.guestConfigurationConnectedVMwarevSphereAssignments()
            .createOrUpdateWithResponse("myResourceGroupName", "myVMName", "NotInstalledApplicationForWindows",
                new GuestConfigurationAssignmentInner()
                    .withProperties(
                        new GuestConfigurationAssignmentProperties()
                            .withGuestConfiguration(
                                new GuestConfigurationNavigation().withName("NotInstalledApplicationForWindows")
                                    .withVersion("1.0.0.0")
                                    .withContentUri("https://thisisfake/pacakge")
                                    .withContentHash("123contenthash")
                                    .withAssignmentType(AssignmentType.APPLY_AND_AUTO_CORRECT)
                                    .withConfigurationParameter(Arrays.asList(new ConfigurationParameter()
                                        .withName("[InstalledApplication]NotInstalledApplicationResource1;Name")
                                        .withValue("NotePad,sql"))))
                            .withContext("Azure policy"))
                    .withLocation("westcentralus"),
                com.azure.core.util.Context.NONE);
    }
}
```

### GuestConfigurationConnectedVMwarevSphereAssignments_Delete

```java
/**
 * Samples for GuestConfigurationConnectedVMwarevSphereAssignments Delete.
 */
public final class GuestConfigurationConnectedVMwarevSphereAssignmentsDeleteSamples {
    /*
     * x-ms-original-file: 2024-04-05/deleteGuestConfigurationConnectedVMwarevSphereAssignment.json
     */
    /**
     * Sample code: Delete an guest configuration assignment.
     * 
     * @param manager Entry point to GuestConfigurationManager.
     */
    public static void deleteAnGuestConfigurationAssignment(
        com.azure.resourcemanager.guestconfiguration.GuestConfigurationManager manager) {
        manager.guestConfigurationConnectedVMwarevSphereAssignments()
            .deleteWithResponse("myResourceGroupName", "myVMName", "SecureProtocol", com.azure.core.util.Context.NONE);
    }
}
```

### GuestConfigurationConnectedVMwarevSphereAssignments_Get

```java
/**
 * Samples for GuestConfigurationConnectedVMwarevSphereAssignments Get.
 */
public final class GuestConfigurationConnectedVMwarevSphereAssignmentsGetSamples {
    /*
     * x-ms-original-file: 2024-04-05/getGuestConfigurationConnectedVMwarevSphereAssignment.json
     */
    /**
     * Sample code: Get a guest configuration assignment.
     * 
     * @param manager Entry point to GuestConfigurationManager.
     */
    public static void getAGuestConfigurationAssignment(
        com.azure.resourcemanager.guestconfiguration.GuestConfigurationManager manager) {
        manager.guestConfigurationConnectedVMwarevSphereAssignments()
            .getWithResponse("myResourceGroupName", "myVMName", "SecureProtocol", com.azure.core.util.Context.NONE);
    }
}
```

### GuestConfigurationConnectedVMwarevSphereAssignments_List

```java
/**
 * Samples for GuestConfigurationConnectedVMwarevSphereAssignments List.
 */
public final class GuestConfigurationConnectedVMwarevSphereAssignmentsListSamples {
    /*
     * x-ms-original-file: 2024-04-05/listGuestConfigurationConnectedVMwarevSphereAssignments.json
     */
    /**
     * Sample code: List all guest configuration assignments for a virtual machine.
     * 
     * @param manager Entry point to GuestConfigurationManager.
     */
    public static void listAllGuestConfigurationAssignmentsForAVirtualMachine(
        com.azure.resourcemanager.guestconfiguration.GuestConfigurationManager manager) {
        manager.guestConfigurationConnectedVMwarevSphereAssignments()
            .list("myResourceGroupName", "myVMName", com.azure.core.util.Context.NONE);
    }
}
```

### GuestConfigurationConnectedVMwarevSphereAssignmentsReports_Get

```java
/**
 * Samples for GuestConfigurationConnectedVMwarevSphereAssignmentsReports Get.
 */
public final class GuestConfigurationConnectedVMwarevSphereAssignmentsReportsGetSamples {
    /*
     * x-ms-original-file: 2024-04-05/getGuestConfigurationConnectedVMwarevSphereAssignmentReportById.json
     */
    /**
     * Sample code: Get a guest configuration assignment report by Id for a virtual machine.
     * 
     * @param manager Entry point to GuestConfigurationManager.
     */
    public static void getAGuestConfigurationAssignmentReportByIdForAVirtualMachine(
        com.azure.resourcemanager.guestconfiguration.GuestConfigurationManager manager) {
        manager.guestConfigurationConnectedVMwarevSphereAssignmentsReports()
            .getWithResponse("myResourceGroupName", "myvm", "AuditSecureProtocol",
                "7367cbb8-ae99-47d0-a33b-a283564d2cb1", com.azure.core.util.Context.NONE);
    }
}
```

### GuestConfigurationConnectedVMwarevSphereAssignmentsReports_List

```java
/**
 * Samples for GuestConfigurationConnectedVMwarevSphereAssignmentsReports List.
 */
public final class GuestConfigurationConnectedVMwarevSphereAssignmentsReportsListSamples {
    /*
     * x-ms-original-file: 2024-04-05/listAllGuestConfigurationConnectedVMwarevSphereAssignmentsReports.json
     */
    /**
     * Sample code: List all guest configuration assignments for a virtual machine.
     * 
     * @param manager Entry point to GuestConfigurationManager.
     */
    public static void listAllGuestConfigurationAssignmentsForAVirtualMachine(
        com.azure.resourcemanager.guestconfiguration.GuestConfigurationManager manager) {
        manager.guestConfigurationConnectedVMwarevSphereAssignmentsReports()
            .listWithResponse("myResourceGroupName", "myVMName", "AuditSecureProtocol",
                com.azure.core.util.Context.NONE);
    }
}
```

### GuestConfigurationHCRPAssignmentReports_Get

```java
/**
 * Samples for GuestConfigurationHCRPAssignmentReports Get.
 */
public final class GuestConfigurationHCRPAssignmentReportsGetSamples {
    /*
     * x-ms-original-file: 2024-04-05/getGuestConfigurationHCRPAssignmentReportById.json
     */
    /**
     * Sample code: Get a guest configuration assignment report by Id for a virtual machine.
     * 
     * @param manager Entry point to GuestConfigurationManager.
     */
    public static void getAGuestConfigurationAssignmentReportByIdForAVirtualMachine(
        com.azure.resourcemanager.guestconfiguration.GuestConfigurationManager manager) {
        manager.guestConfigurationHCRPAssignmentReports()
            .getWithResponse("myResourceGroupName", "myMachineName", "AuditSecureProtocol",
                "7367cbb8-ae99-47d0-a33b-a283564d2cb1", com.azure.core.util.Context.NONE);
    }
}
```

### GuestConfigurationHCRPAssignmentReports_List

```java
/**
 * Samples for GuestConfigurationHCRPAssignmentReports List.
 */
public final class GuestConfigurationHCRPAssignmentReportsListSamples {
    /*
     * x-ms-original-file: 2024-04-05/listAllGuestConfigurationHCRPAssignmentReports.json
     */
    /**
     * Sample code: List all guest configuration assignments for a virtual machine.
     * 
     * @param manager Entry point to GuestConfigurationManager.
     */
    public static void listAllGuestConfigurationAssignmentsForAVirtualMachine(
        com.azure.resourcemanager.guestconfiguration.GuestConfigurationManager manager) {
        manager.guestConfigurationHCRPAssignmentReports()
            .listWithResponse("myResourceGroupName", "myMachineName", "AuditSecureProtocol",
                com.azure.core.util.Context.NONE);
    }
}
```

### GuestConfigurationHCRPAssignments_CreateOrUpdate

```java
import com.azure.resourcemanager.guestconfiguration.fluent.models.GuestConfigurationAssignmentInner;
import com.azure.resourcemanager.guestconfiguration.models.AssignmentType;
import com.azure.resourcemanager.guestconfiguration.models.ConfigurationParameter;
import com.azure.resourcemanager.guestconfiguration.models.GuestConfigurationAssignmentProperties;
import com.azure.resourcemanager.guestconfiguration.models.GuestConfigurationNavigation;
import java.util.Arrays;

/**
 * Samples for GuestConfigurationHCRPAssignments CreateOrUpdate.
 */
public final class GuestConfigurationHCRPAssignmentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-04-05/createOrUpdateGuestConfigurationHCRPAssignment.json
     */
    /**
     * Sample code: Create or update guest configuration assignment.
     * 
     * @param manager Entry point to GuestConfigurationManager.
     */
    public static void createOrUpdateGuestConfigurationAssignment(
        com.azure.resourcemanager.guestconfiguration.GuestConfigurationManager manager) {
        manager.guestConfigurationHCRPAssignments()
            .createOrUpdateWithResponse("myResourceGroupName", "myMachineName", "NotInstalledApplicationForWindows",
                new GuestConfigurationAssignmentInner()
                    .withProperties(
                        new GuestConfigurationAssignmentProperties()
                            .withGuestConfiguration(
                                new GuestConfigurationNavigation().withName("NotInstalledApplicationForWindows")
                                    .withVersion("1.0.0.3")
                                    .withContentUri("https://thisisfake/pacakge")
                                    .withContentHash("123contenthash")
                                    .withAssignmentType(AssignmentType.APPLY_AND_AUTO_CORRECT)
                                    .withConfigurationParameter(Arrays.asList(new ConfigurationParameter()
                                        .withName("[InstalledApplication]NotInstalledApplicationResource1;Name")
                                        .withValue("NotePad,sql"))))
                            .withContext("Azure policy"))
                    .withLocation("westcentralus"),
                com.azure.core.util.Context.NONE);
    }
}
```

### GuestConfigurationHCRPAssignments_Delete

```java
/**
 * Samples for GuestConfigurationHCRPAssignments Delete.
 */
public final class GuestConfigurationHCRPAssignmentsDeleteSamples {
    /*
     * x-ms-original-file: 2024-04-05/deleteGuestConfigurationHCRPAssignment.json
     */
    /**
     * Sample code: Delete an guest configuration assignment.
     * 
     * @param manager Entry point to GuestConfigurationManager.
     */
    public static void deleteAnGuestConfigurationAssignment(
        com.azure.resourcemanager.guestconfiguration.GuestConfigurationManager manager) {
        manager.guestConfigurationHCRPAssignments()
            .deleteWithResponse("myResourceGroupName", "myMachineName", "SecureProtocol",
                com.azure.core.util.Context.NONE);
    }
}
```

### GuestConfigurationHCRPAssignments_Get

```java
/**
 * Samples for GuestConfigurationHCRPAssignments Get.
 */
public final class GuestConfigurationHCRPAssignmentsGetSamples {
    /*
     * x-ms-original-file: 2024-04-05/getGuestConfigurationHCRPAssignment.json
     */
    /**
     * Sample code: Get a guest configuration assignment.
     * 
     * @param manager Entry point to GuestConfigurationManager.
     */
    public static void getAGuestConfigurationAssignment(
        com.azure.resourcemanager.guestconfiguration.GuestConfigurationManager manager) {
        manager.guestConfigurationHCRPAssignments()
            .getWithResponse("myResourceGroupName", "myMachineName", "SecureProtocol",
                com.azure.core.util.Context.NONE);
    }
}
```

### GuestConfigurationHCRPAssignments_List

```java
/**
 * Samples for GuestConfigurationHCRPAssignments List.
 */
public final class GuestConfigurationHCRPAssignmentsListSamples {
    /*
     * x-ms-original-file: 2024-04-05/listGuestConfigurationHCRPAssignments.json
     */
    /**
     * Sample code: List all guest configuration assignments for a virtual machine.
     * 
     * @param manager Entry point to GuestConfigurationManager.
     */
    public static void listAllGuestConfigurationAssignmentsForAVirtualMachine(
        com.azure.resourcemanager.guestconfiguration.GuestConfigurationManager manager) {
        manager.guestConfigurationHCRPAssignments()
            .list("myResourceGroupName", "myMachineName", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2024-04-05/listOperations.json
     */
    /**
     * Sample code: Lists all of the available GuestConfiguration REST API operations.
     * 
     * @param manager Entry point to GuestConfigurationManager.
     */
    public static void listsAllOfTheAvailableGuestConfigurationRESTAPIOperations(
        com.azure.resourcemanager.guestconfiguration.GuestConfigurationManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```


# Code snippets and samples


## AvailableWorkloadProfiles

- [Get](#availableworkloadprofiles_get)

## BillingMeters

- [Get](#billingmeters_get)

## Certificates

- [CreateOrUpdate](#certificates_createorupdate)
- [Delete](#certificates_delete)
- [Get](#certificates_get)
- [List](#certificates_list)
- [Update](#certificates_update)

## ConnectedEnvironments

- [CheckNameAvailability](#connectedenvironments_checknameavailability)
- [CreateOrUpdate](#connectedenvironments_createorupdate)
- [Delete](#connectedenvironments_delete)
- [GetByResourceGroup](#connectedenvironments_getbyresourcegroup)
- [List](#connectedenvironments_list)
- [ListByResourceGroup](#connectedenvironments_listbyresourcegroup)
- [Update](#connectedenvironments_update)

## ConnectedEnvironmentsCertificates

- [CreateOrUpdate](#connectedenvironmentscertificates_createorupdate)
- [Delete](#connectedenvironmentscertificates_delete)
- [Get](#connectedenvironmentscertificates_get)
- [List](#connectedenvironmentscertificates_list)
- [Update](#connectedenvironmentscertificates_update)

## ConnectedEnvironmentsDaprComponents

- [CreateOrUpdate](#connectedenvironmentsdaprcomponents_createorupdate)
- [Delete](#connectedenvironmentsdaprcomponents_delete)
- [Get](#connectedenvironmentsdaprcomponents_get)
- [List](#connectedenvironmentsdaprcomponents_list)
- [ListSecrets](#connectedenvironmentsdaprcomponents_listsecrets)

## ConnectedEnvironmentsStorages

- [CreateOrUpdate](#connectedenvironmentsstorages_createorupdate)
- [Delete](#connectedenvironmentsstorages_delete)
- [Get](#connectedenvironmentsstorages_get)
- [List](#connectedenvironmentsstorages_list)

## ContainerApps

- [CreateOrUpdate](#containerapps_createorupdate)
- [Delete](#containerapps_delete)
- [GetAuthToken](#containerapps_getauthtoken)
- [GetByResourceGroup](#containerapps_getbyresourcegroup)
- [List](#containerapps_list)
- [ListByResourceGroup](#containerapps_listbyresourcegroup)
- [ListCustomHostnameAnalysis](#containerapps_listcustomhostnameanalysis)
- [ListSecrets](#containerapps_listsecrets)
- [Start](#containerapps_start)
- [Stop](#containerapps_stop)
- [Update](#containerapps_update)

## ContainerAppsAuthConfigs

- [CreateOrUpdate](#containerappsauthconfigs_createorupdate)
- [Delete](#containerappsauthconfigs_delete)
- [Get](#containerappsauthconfigs_get)
- [ListByContainerApp](#containerappsauthconfigs_listbycontainerapp)

## ContainerAppsDiagnostics

- [GetDetector](#containerappsdiagnostics_getdetector)
- [GetRevision](#containerappsdiagnostics_getrevision)
- [GetRoot](#containerappsdiagnostics_getroot)
- [ListDetectors](#containerappsdiagnostics_listdetectors)
- [ListRevisions](#containerappsdiagnostics_listrevisions)

## ContainerAppsRevisionReplicas

- [GetReplica](#containerappsrevisionreplicas_getreplica)
- [ListReplicas](#containerappsrevisionreplicas_listreplicas)

## ContainerAppsRevisions

- [ActivateRevision](#containerappsrevisions_activaterevision)
- [DeactivateRevision](#containerappsrevisions_deactivaterevision)
- [GetRevision](#containerappsrevisions_getrevision)
- [ListRevisions](#containerappsrevisions_listrevisions)
- [RestartRevision](#containerappsrevisions_restartrevision)

## ContainerAppsSourceControls

- [CreateOrUpdate](#containerappssourcecontrols_createorupdate)
- [Delete](#containerappssourcecontrols_delete)
- [Get](#containerappssourcecontrols_get)
- [ListByContainerApp](#containerappssourcecontrols_listbycontainerapp)

## DaprComponents

- [CreateOrUpdate](#daprcomponents_createorupdate)
- [Delete](#daprcomponents_delete)
- [Get](#daprcomponents_get)
- [List](#daprcomponents_list)
- [ListSecrets](#daprcomponents_listsecrets)

## Jobs

- [CreateOrUpdate](#jobs_createorupdate)
- [Delete](#jobs_delete)
- [GetByResourceGroup](#jobs_getbyresourcegroup)
- [GetDetector](#jobs_getdetector)
- [List](#jobs_list)
- [ListByResourceGroup](#jobs_listbyresourcegroup)
- [ListDetectors](#jobs_listdetectors)
- [ListSecrets](#jobs_listsecrets)
- [ProxyGet](#jobs_proxyget)
- [Start](#jobs_start)
- [StopExecution](#jobs_stopexecution)
- [StopMultipleExecutions](#jobs_stopmultipleexecutions)
- [Update](#jobs_update)

## JobsExecutions

- [List](#jobsexecutions_list)

## ManagedCertificates

- [CreateOrUpdate](#managedcertificates_createorupdate)
- [Delete](#managedcertificates_delete)
- [Get](#managedcertificates_get)
- [List](#managedcertificates_list)
- [Update](#managedcertificates_update)

## ManagedEnvironmentDiagnostics

- [GetDetector](#managedenvironmentdiagnostics_getdetector)
- [ListDetectors](#managedenvironmentdiagnostics_listdetectors)

## ManagedEnvironmentUsages

- [List](#managedenvironmentusages_list)

## ManagedEnvironments

- [CreateOrUpdate](#managedenvironments_createorupdate)
- [Delete](#managedenvironments_delete)
- [GetAuthToken](#managedenvironments_getauthtoken)
- [GetByResourceGroup](#managedenvironments_getbyresourcegroup)
- [List](#managedenvironments_list)
- [ListByResourceGroup](#managedenvironments_listbyresourcegroup)
- [ListWorkloadProfileStates](#managedenvironments_listworkloadprofilestates)
- [Update](#managedenvironments_update)

## ManagedEnvironmentsDiagnostics

- [GetRoot](#managedenvironmentsdiagnostics_getroot)

## ManagedEnvironmentsStorages

- [CreateOrUpdate](#managedenvironmentsstorages_createorupdate)
- [Delete](#managedenvironmentsstorages_delete)
- [Get](#managedenvironmentsstorages_get)
- [List](#managedenvironmentsstorages_list)

## Namespaces

- [CheckNameAvailability](#namespaces_checknameavailability)

## Operations

- [List](#operations_list)

## ResourceProvider

- [GetCustomDomainVerificationId](#resourceprovider_getcustomdomainverificationid)
- [JobExecution](#resourceprovider_jobexecution)

## Usages

- [List](#usages_list)
### AvailableWorkloadProfiles_Get

```java
/**
 * Samples for AvailableWorkloadProfiles Get.
 */
public final class AvailableWorkloadProfilesGetSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/AvailableWorkloadProfiles_Get.json
     */
    /**
     * Sample code: BillingMeters_Get.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void billingMetersGet(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.availableWorkloadProfiles().get("East US", com.azure.core.util.Context.NONE);
    }
}
```

### BillingMeters_Get

```java
/**
 * Samples for BillingMeters Get.
 */
public final class BillingMetersGetSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/BillingMeters_Get.json
     */
    /**
     * Sample code: BillingMeters_Get.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void billingMetersGet(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.billingMeters().getWithResponse("East US", com.azure.core.util.Context.NONE);
    }
}
```

### Certificates_CreateOrUpdate

```java
import com.azure.resourcemanager.appcontainers.fluent.models.CertificateInner;
import com.azure.resourcemanager.appcontainers.models.CertificateProperties;

/**
 * Samples for Certificates CreateOrUpdate.
 */
public final class CertificatesCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/Certificate_CreateOrUpdate.json
     */
    /**
     * Sample code: Create or Update Certificate.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        createOrUpdateCertificate(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.certificates()
            .createOrUpdateWithResponse("examplerg", "testcontainerenv", "certificate-firendly-name",
                new CertificateInner().withLocation("East US")
                    .withProperties(new CertificateProperties().withPassword("fakeTokenPlaceholder")
                        .withValue("Y2VydA==".getBytes())),
                com.azure.core.util.Context.NONE);
    }
}
```

### Certificates_Delete

```java
/**
 * Samples for Certificates Delete.
 */
public final class CertificatesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/Certificate_Delete.json
     */
    /**
     * Sample code: Delete Certificate.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void deleteCertificate(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.certificates()
            .deleteWithResponse("examplerg", "testcontainerenv", "certificate-firendly-name",
                com.azure.core.util.Context.NONE);
    }
}
```

### Certificates_Get

```java
/**
 * Samples for Certificates Get.
 */
public final class CertificatesGetSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/Certificate_Get.json
     */
    /**
     * Sample code: Get Certificate.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void getCertificate(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.certificates()
            .getWithResponse("examplerg", "testcontainerenv", "certificate-firendly-name",
                com.azure.core.util.Context.NONE);
    }
}
```

### Certificates_List

```java
/**
 * Samples for Certificates List.
 */
public final class CertificatesListSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/Certificates_ListByManagedEnvironment
     * .json
     */
    /**
     * Sample code: List Certificates by Managed Environment.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        listCertificatesByManagedEnvironment(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.certificates().list("examplerg", "testcontainerenv", com.azure.core.util.Context.NONE);
    }
}
```

### Certificates_Update

```java
import com.azure.resourcemanager.appcontainers.models.CertificatePatch;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Certificates Update.
 */
public final class CertificatesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/Certificates_Patch.json
     */
    /**
     * Sample code: Patch Certificate.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void patchCertificate(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.certificates()
            .updateWithResponse("examplerg", "testcontainerenv", "certificate-firendly-name",
                new CertificatePatch().withTags(mapOf("tag1", "value1", "tag2", "value2")),
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

### ConnectedEnvironments_CheckNameAvailability

```java
import com.azure.resourcemanager.appcontainers.models.CheckNameAvailabilityRequest;

/**
 * Samples for ConnectedEnvironments CheckNameAvailability.
 */
public final class ConnectedEnvironmentsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/
     * ConnectedEnvironmentsCertificates_CheckNameAvailability.json
     */
    /**
     * Sample code: Certificates_CheckNameAvailability.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        certificatesCheckNameAvailability(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.connectedEnvironments()
            .checkNameAvailabilityWithResponse("examplerg", "testcontainerenv",
                new CheckNameAvailabilityRequest().withName("testcertificatename")
                    .withType("Microsoft.App/connectedEnvironments/certificates"),
                com.azure.core.util.Context.NONE);
    }
}
```

### ConnectedEnvironments_CreateOrUpdate

```java
import com.azure.resourcemanager.appcontainers.models.CustomDomainConfiguration;

/**
 * Samples for ConnectedEnvironments CreateOrUpdate.
 */
public final class ConnectedEnvironmentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/ConnectedEnvironments_CreateOrUpdate.
     * json
     */
    /**
     * Sample code: Create kube environments.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void createKubeEnvironments(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.connectedEnvironments()
            .define("testenv")
            .withRegion("East US")
            .withExistingResourceGroup("examplerg")
            .withStaticIp("1.2.3.4")
            .withDaprAIConnectionString(
                "InstrumentationKey=00000000-0000-0000-0000-000000000000;IngestionEndpoint=https://northcentralus-0.in.applicationinsights.azure.com/")
            .withCustomDomainConfiguration(new CustomDomainConfiguration().withDnsSuffix("www.my-name.com")
                .withCertificateValue("Y2VydA==".getBytes())
                .withCertificatePassword("fakeTokenPlaceholder"))
            .create();
    }
}
```

### ConnectedEnvironments_Delete

```java
/**
 * Samples for ConnectedEnvironments Delete.
 */
public final class ConnectedEnvironmentsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/ConnectedEnvironments_Delete.json
     */
    /**
     * Sample code: Delete connected environment by connectedEnvironmentName.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void deleteConnectedEnvironmentByConnectedEnvironmentName(
        com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.connectedEnvironments().delete("examplerg", "examplekenv", com.azure.core.util.Context.NONE);
    }
}
```

### ConnectedEnvironments_GetByResourceGroup

```java
/**
 * Samples for ConnectedEnvironments GetByResourceGroup.
 */
public final class ConnectedEnvironmentsGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/ConnectedEnvironments_Get.json
     */
    /**
     * Sample code: Get connected environment by connectedEnvironmentName.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void getConnectedEnvironmentByConnectedEnvironmentName(
        com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.connectedEnvironments()
            .getByResourceGroupWithResponse("examplerg", "examplekenv", com.azure.core.util.Context.NONE);
    }
}
```

### ConnectedEnvironments_List

```java
/**
 * Samples for ConnectedEnvironments List.
 */
public final class ConnectedEnvironmentsListSamples {
    /*
     * x-ms-original-file: specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/
     * ConnectedEnvironments_ListBySubscription.json
     */
    /**
     * Sample code: List connected environments by subscription.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void listConnectedEnvironmentsBySubscription(
        com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.connectedEnvironments().list(com.azure.core.util.Context.NONE);
    }
}
```

### ConnectedEnvironments_ListByResourceGroup

```java
/**
 * Samples for ConnectedEnvironments ListByResourceGroup.
 */
public final class ConnectedEnvironmentsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/
     * ConnectedEnvironments_ListByResourceGroup.json
     */
    /**
     * Sample code: List environments by resource group.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        listEnvironmentsByResourceGroup(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.connectedEnvironments().listByResourceGroup("examplerg", com.azure.core.util.Context.NONE);
    }
}
```

### ConnectedEnvironments_Update

```java
/**
 * Samples for ConnectedEnvironments Update.
 */
public final class ConnectedEnvironmentsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/ConnectedEnvironments_Patch.json
     */
    /**
     * Sample code: Patch Managed Environment.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        patchManagedEnvironment(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.connectedEnvironments().updateWithResponse("examplerg", "testenv", com.azure.core.util.Context.NONE);
    }
}
```

### ConnectedEnvironmentsCertificates_CreateOrUpdate

```java
import com.azure.resourcemanager.appcontainers.models.CertificateProperties;

/**
 * Samples for ConnectedEnvironmentsCertificates CreateOrUpdate.
 */
public final class ConnectedEnvironmentsCertificatesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/
     * ConnectedEnvironmentsCertificate_CreateOrUpdate.json
     */
    /**
     * Sample code: Create or Update Certificate.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        createOrUpdateCertificate(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.connectedEnvironmentsCertificates()
            .define("certificate-firendly-name")
            .withRegion("East US")
            .withExistingConnectedEnvironment("examplerg", "testcontainerenv")
            .withProperties(
                new CertificateProperties().withPassword("fakeTokenPlaceholder").withValue("Y2VydA==".getBytes()))
            .create();
    }
}
```

### ConnectedEnvironmentsCertificates_Delete

```java
/**
 * Samples for ConnectedEnvironmentsCertificates Delete.
 */
public final class ConnectedEnvironmentsCertificatesDeleteSamples {
    /*
     * x-ms-original-file: specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/
     * ConnectedEnvironmentsCertificate_Delete.json
     */
    /**
     * Sample code: Delete Certificate.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void deleteCertificate(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.connectedEnvironmentsCertificates()
            .deleteWithResponse("examplerg", "testcontainerenv", "certificate-firendly-name",
                com.azure.core.util.Context.NONE);
    }
}
```

### ConnectedEnvironmentsCertificates_Get

```java
/**
 * Samples for ConnectedEnvironmentsCertificates Get.
 */
public final class ConnectedEnvironmentsCertificatesGetSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/ConnectedEnvironmentsCertificate_Get.
     * json
     */
    /**
     * Sample code: Get Certificate.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void getCertificate(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.connectedEnvironmentsCertificates()
            .getWithResponse("examplerg", "testcontainerenv", "certificate-firendly-name",
                com.azure.core.util.Context.NONE);
    }
}
```

### ConnectedEnvironmentsCertificates_List

```java
/**
 * Samples for ConnectedEnvironmentsCertificates List.
 */
public final class ConnectedEnvironmentsCertificatesListSamples {
    /*
     * x-ms-original-file: specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/
     * ConnectedEnvironmentsCertificates_ListByConnectedEnvironment.json
     */
    /**
     * Sample code: List Certificates by Connected Environment.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void listCertificatesByConnectedEnvironment(
        com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.connectedEnvironmentsCertificates()
            .list("examplerg", "testcontainerenv", com.azure.core.util.Context.NONE);
    }
}
```

### ConnectedEnvironmentsCertificates_Update

```java
import com.azure.resourcemanager.appcontainers.models.Certificate;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ConnectedEnvironmentsCertificates Update.
 */
public final class ConnectedEnvironmentsCertificatesUpdateSamples {
    /*
     * x-ms-original-file: specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/
     * ConnectedEnvironmentsCertificates_Patch.json
     */
    /**
     * Sample code: Patch Certificate.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void patchCertificate(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        Certificate resource = manager.connectedEnvironmentsCertificates()
            .getWithResponse("examplerg", "testcontainerenv", "certificate-firendly-name",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### ConnectedEnvironmentsDaprComponents_CreateOrUpdate

```java
import com.azure.resourcemanager.appcontainers.models.DaprMetadata;
import com.azure.resourcemanager.appcontainers.models.Secret;
import java.util.Arrays;

/**
 * Samples for ConnectedEnvironmentsDaprComponents CreateOrUpdate.
 */
public final class ConnectedEnvironmentsDaprComponentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/
     * ConnectedEnvironmentsDaprComponents_CreateOrUpdate.json
     */
    /**
     * Sample code: Create or update dapr component.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        createOrUpdateDaprComponent(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.connectedEnvironmentsDaprComponents()
            .define("reddog")
            .withExistingConnectedEnvironment("examplerg", "myenvironment")
            .withComponentType("state.azure.cosmosdb")
            .withVersion("v1")
            .withIgnoreErrors(false)
            .withInitTimeout("50s")
            .withSecrets(Arrays.asList(new Secret().withName("masterkey").withValue("keyvalue")))
            .withMetadata(Arrays.asList(new DaprMetadata().withName("url").withValue("<COSMOS-URL>"),
                new DaprMetadata().withName("database").withValue("itemsDB"),
                new DaprMetadata().withName("collection").withValue("items"),
                new DaprMetadata().withName("masterkey").withSecretRef("fakeTokenPlaceholder")))
            .withScopes(Arrays.asList("container-app-1", "container-app-2"))
            .create();
    }
}
```

### ConnectedEnvironmentsDaprComponents_Delete

```java
/**
 * Samples for ConnectedEnvironmentsDaprComponents Delete.
 */
public final class ConnectedEnvironmentsDaprComponentsDeleteSamples {
    /*
     * x-ms-original-file: specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/
     * ConnectedEnvironmentsDaprComponents_Delete.json
     */
    /**
     * Sample code: Delete dapr component.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void deleteDaprComponent(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.connectedEnvironmentsDaprComponents()
            .deleteWithResponse("examplerg", "myenvironment", "reddog", com.azure.core.util.Context.NONE);
    }
}
```

### ConnectedEnvironmentsDaprComponents_Get

```java
/**
 * Samples for ConnectedEnvironmentsDaprComponents Get.
 */
public final class ConnectedEnvironmentsDaprComponentsGetSamples {
    /*
     * x-ms-original-file: specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/
     * ConnectedEnvironmentsDaprComponents_Get.json
     */
    /**
     * Sample code: Get Dapr Component.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void getDaprComponent(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.connectedEnvironmentsDaprComponents()
            .getWithResponse("examplerg", "myenvironment", "reddog", com.azure.core.util.Context.NONE);
    }
}
```

### ConnectedEnvironmentsDaprComponents_List

```java
/**
 * Samples for ConnectedEnvironmentsDaprComponents List.
 */
public final class ConnectedEnvironmentsDaprComponentsListSamples {
    /*
     * x-ms-original-file: specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/
     * ConnectedEnvironmentsDaprComponents_List.json
     */
    /**
     * Sample code: List Dapr Components.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void listDaprComponents(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.connectedEnvironmentsDaprComponents()
            .list("examplerg", "myenvironment", com.azure.core.util.Context.NONE);
    }
}
```

### ConnectedEnvironmentsDaprComponents_ListSecrets

```java
/**
 * Samples for ConnectedEnvironmentsDaprComponents ListSecrets.
 */
public final class ConnectedEnvironmentsDaprComponentsListSecretsSamples {
    /*
     * x-ms-original-file: specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/
     * ConnectedEnvironmentsDaprComponents_ListSecrets.json
     */
    /**
     * Sample code: List Container Apps Secrets.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        listContainerAppsSecrets(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.connectedEnvironmentsDaprComponents()
            .listSecretsWithResponse("examplerg", "myenvironment", "reddog", com.azure.core.util.Context.NONE);
    }
}
```

### ConnectedEnvironmentsStorages_CreateOrUpdate

```java
import com.azure.resourcemanager.appcontainers.models.AccessMode;
import com.azure.resourcemanager.appcontainers.models.AzureFileProperties;
import com.azure.resourcemanager.appcontainers.models.ConnectedEnvironmentStorageProperties;

/**
 * Samples for ConnectedEnvironmentsStorages CreateOrUpdate.
 */
public final class ConnectedEnvironmentsStoragesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/
     * ConnectedEnvironmentsStorages_CreateOrUpdate.json
     */
    /**
     * Sample code: Create or update environments storage.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        createOrUpdateEnvironmentsStorage(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.connectedEnvironmentsStorages()
            .define("jlaw-demo1")
            .withExistingConnectedEnvironment("examplerg", "env")
            .withProperties(new ConnectedEnvironmentStorageProperties()
                .withAzureFile(new AzureFileProperties().withAccountName("account1")
                    .withAccountKey("fakeTokenPlaceholder")
                    .withAccessMode(AccessMode.READ_ONLY)
                    .withShareName("share1")))
            .create();
    }
}
```

### ConnectedEnvironmentsStorages_Delete

```java
/**
 * Samples for ConnectedEnvironmentsStorages Delete.
 */
public final class ConnectedEnvironmentsStoragesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/ConnectedEnvironmentsStorages_Delete.
     * json
     */
    /**
     * Sample code: List environments storages by subscription.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void listEnvironmentsStoragesBySubscription(
        com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.connectedEnvironmentsStorages()
            .deleteWithResponse("examplerg", "env", "jlaw-demo1", com.azure.core.util.Context.NONE);
    }
}
```

### ConnectedEnvironmentsStorages_Get

```java
/**
 * Samples for ConnectedEnvironmentsStorages Get.
 */
public final class ConnectedEnvironmentsStoragesGetSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/ConnectedEnvironmentsStorages_Get.
     * json
     */
    /**
     * Sample code: get a environments storage properties by subscription.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void getAEnvironmentsStoragePropertiesBySubscription(
        com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.connectedEnvironmentsStorages()
            .getWithResponse("examplerg", "env", "jlaw-demo1", com.azure.core.util.Context.NONE);
    }
}
```

### ConnectedEnvironmentsStorages_List

```java
/**
 * Samples for ConnectedEnvironmentsStorages List.
 */
public final class ConnectedEnvironmentsStoragesListSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/ConnectedEnvironmentsStorages_List.
     * json
     */
    /**
     * Sample code: List environments storages by subscription.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void listEnvironmentsStoragesBySubscription(
        com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.connectedEnvironmentsStorages()
            .listWithResponse("examplerg", "managedEnv", com.azure.core.util.Context.NONE);
    }
}
```

### ContainerApps_CreateOrUpdate

```java
import com.azure.resourcemanager.appcontainers.models.Action;
import com.azure.resourcemanager.appcontainers.models.Affinity;
import com.azure.resourcemanager.appcontainers.models.AppProtocol;
import com.azure.resourcemanager.appcontainers.models.BindingType;
import com.azure.resourcemanager.appcontainers.models.Configuration;
import com.azure.resourcemanager.appcontainers.models.Container;
import com.azure.resourcemanager.appcontainers.models.ContainerAppProbe;
import com.azure.resourcemanager.appcontainers.models.ContainerAppProbeHttpGet;
import com.azure.resourcemanager.appcontainers.models.ContainerAppProbeHttpGetHttpHeadersItem;
import com.azure.resourcemanager.appcontainers.models.ContainerAppProbeTcpSocket;
import com.azure.resourcemanager.appcontainers.models.ContainerResources;
import com.azure.resourcemanager.appcontainers.models.CorsPolicy;
import com.azure.resourcemanager.appcontainers.models.CustomDomain;
import com.azure.resourcemanager.appcontainers.models.CustomScaleRule;
import com.azure.resourcemanager.appcontainers.models.Dapr;
import com.azure.resourcemanager.appcontainers.models.Ingress;
import com.azure.resourcemanager.appcontainers.models.IngressClientCertificateMode;
import com.azure.resourcemanager.appcontainers.models.IngressPortMapping;
import com.azure.resourcemanager.appcontainers.models.IngressStickySessions;
import com.azure.resourcemanager.appcontainers.models.IngressTransportMethod;
import com.azure.resourcemanager.appcontainers.models.InitContainer;
import com.azure.resourcemanager.appcontainers.models.IpSecurityRestrictionRule;
import com.azure.resourcemanager.appcontainers.models.LogLevel;
import com.azure.resourcemanager.appcontainers.models.Scale;
import com.azure.resourcemanager.appcontainers.models.ScaleRule;
import com.azure.resourcemanager.appcontainers.models.Service;
import com.azure.resourcemanager.appcontainers.models.ServiceBind;
import com.azure.resourcemanager.appcontainers.models.TcpScaleRule;
import com.azure.resourcemanager.appcontainers.models.Template;
import com.azure.resourcemanager.appcontainers.models.TrafficWeight;
import com.azure.resourcemanager.appcontainers.models.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ContainerApps CreateOrUpdate.
 */
public final class ContainerAppsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/ContainerApps_CreateOrUpdate.json
     */
    /**
     * Sample code: Create or Update Container App.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        createOrUpdateContainerApp(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.containerApps()
            .define("testcontainerapp0")
            .withRegion("East US")
            .withExistingResourceGroup("rg")
            .withEnvironmentId(
                "/subscriptions/34adfa4f-cedf-4dc0-ba29-b6d1a69ab345/resourceGroups/rg/providers/Microsoft.App/managedEnvironments/demokube")
            .withWorkloadProfileName("My-GP-01")
            .withConfiguration(
                new Configuration()
                    .withIngress(new Ingress().withExternal(true)
                        .withTargetPort(3000)
                        .withTraffic(Arrays.asList(new TrafficWeight()
                            .withRevisionName("testcontainerapp0-ab1234")
                            .withWeight(100)
                            .withLabel("production")))
                        .withCustomDomains(Arrays.asList(new CustomDomain()
                            .withName("www.my-name.com")
                            .withBindingType(BindingType.SNI_ENABLED)
                            .withCertificateId(
                                "/subscriptions/34adfa4f-cedf-4dc0-ba29-b6d1a69ab345/resourceGroups/rg/providers/Microsoft.App/managedEnvironments/demokube/certificates/my-certificate-for-my-name-dot-com"),
                            new CustomDomain().withName("www.my-other-name.com")
                                .withBindingType(BindingType.SNI_ENABLED)
                                .withCertificateId(
                                    "/subscriptions/34adfa4f-cedf-4dc0-ba29-b6d1a69ab345/resourceGroups/rg/providers/Microsoft.App/managedEnvironments/demokube/certificates/my-certificate-for-my-other-name-dot-com")))
                        .withIpSecurityRestrictions(
                            Arrays
                                .asList(
                                    new IpSecurityRestrictionRule().withName("Allow work IP A subnet")
                                        .withDescription(
                                            "Allowing all IP's within the subnet below to access containerapp")
                                        .withIpAddressRange("192.168.1.1/32")
                                        .withAction(Action.ALLOW),
                                    new IpSecurityRestrictionRule().withName("Allow work IP B subnet")
                                        .withDescription(
                                            "Allowing all IP's within the subnet below to access containerapp")
                                        .withIpAddressRange("192.168.1.1/8")
                                        .withAction(Action.ALLOW)))
                        .withStickySessions(new IngressStickySessions().withAffinity(Affinity.STICKY))
                        .withClientCertificateMode(IngressClientCertificateMode.ACCEPT)
                        .withCorsPolicy(new CorsPolicy()
                            .withAllowedOrigins(Arrays.asList("https://a.test.com", "https://b.test.com"))
                            .withAllowedMethods(Arrays.asList("GET", "POST"))
                            .withAllowedHeaders(Arrays.asList("HEADER1", "HEADER2"))
                            .withExposeHeaders(Arrays.asList("HEADER3", "HEADER4"))
                            .withMaxAge(1234)
                            .withAllowCredentials(true))
                        .withAdditionalPortMappings(Arrays.asList(
                            new IngressPortMapping().withExternal(true).withTargetPort(1234),
                            new IngressPortMapping().withExternal(false).withTargetPort(2345).withExposedPort(3456))))
                    .withDapr(new Dapr().withEnabled(true)
                        .withAppProtocol(AppProtocol.HTTP)
                        .withAppPort(3000)
                        .withHttpReadBufferSize(30)
                        .withHttpMaxRequestSize(10)
                        .withLogLevel(LogLevel.DEBUG)
                        .withEnableApiLogging(true))
                    .withMaxInactiveRevisions(10)
                    .withService(new Service().withType("redis")))
            .withTemplate(new Template()
                .withInitContainers(Arrays.asList(new InitContainer().withImage("repo/testcontainerapp0:v4")
                    .withName("testinitcontainerApp0")
                    .withCommand(Arrays.asList("/bin/sh"))
                    .withArgs(Arrays.asList("-c", "while true; do echo hello; sleep 10;done"))
                    .withResources(new ContainerResources().withCpu(0.5D).withMemory("1Gi"))))
                .withContainers(Arrays.asList(new Container()
                    .withImage("repo/testcontainerapp0:v1")
                    .withName("testcontainerapp0")
                    .withProbes(
                        Arrays.asList(new ContainerAppProbe().withHttpGet(new ContainerAppProbeHttpGet()
                            .withHttpHeaders(
                                Arrays.asList(new ContainerAppProbeHttpGetHttpHeadersItem().withName("Custom-Header")
                                    .withValue("Awesome")))
                            .withPath("/health")
                            .withPort(8080)).withInitialDelaySeconds(3).withPeriodSeconds(3).withType(Type.LIVENESS)))))
                .withScale(
                    new Scale().withMinReplicas(1)
                        .withMaxReplicas(5)
                        .withRules(
                            Arrays.asList(new ScaleRule().withName("httpscalingrule")
                                .withCustom(new CustomScaleRule().withType("http")
                                    .withMetadata(mapOf("concurrentRequests", "50"))))))
                .withServiceBinds(Arrays.asList(new ServiceBind().withServiceId(
                    "/subscriptions/34adfa4f-cedf-4dc0-ba29-b6d1a69ab345/resourceGroups/rg/providers/Microsoft.App/containerApps/redisService")
                    .withName("redisService"))))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/ContainerApps_TcpApp_CreateOrUpdate.
     * json
     */
    /**
     * Sample code: Create or Update Tcp App.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void createOrUpdateTcpApp(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.containerApps()
            .define("testcontainerapptcp")
            .withRegion("East US")
            .withExistingResourceGroup("rg")
            .withEnvironmentId(
                "/subscriptions/34adfa4f-cedf-4dc0-ba29-b6d1a69ab345/resourceGroups/rg/providers/Microsoft.App/managedEnvironments/demokube")
            .withConfiguration(
                new Configuration().withIngress(new Ingress().withExternal(true)
                    .withTargetPort(3000)
                    .withExposedPort(4000)
                    .withTransport(IngressTransportMethod.TCP)
                    .withTraffic(Arrays
                        .asList(new TrafficWeight().withRevisionName("testcontainerapptcp-ab1234").withWeight(100)))))
            .withTemplate(new Template()
                .withContainers(Arrays.asList(new Container().withImage("repo/testcontainerapptcp:v1")
                    .withName("testcontainerapptcp")
                    .withProbes(Arrays.asList(new ContainerAppProbe().withInitialDelaySeconds(3)
                        .withPeriodSeconds(3)
                        .withTcpSocket(new ContainerAppProbeTcpSocket().withPort(8080))
                        .withType(Type.LIVENESS)))))
                .withScale(new Scale().withMinReplicas(1)
                    .withMaxReplicas(5)
                    .withRules(Arrays.asList(new ScaleRule().withName("tcpscalingrule")
                        .withTcp(new TcpScaleRule().withMetadata(mapOf("concurrentConnections", "50")))))))
            .create();
    }

    /*
     * x-ms-original-file: specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/
     * ContainerApps_ManagedBy_CreateOrUpdate.json
     */
    /**
     * Sample code: Create or Update ManagedBy App.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        createOrUpdateManagedByApp(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.containerApps()
            .define("testcontainerappmanagedby")
            .withRegion("East US")
            .withExistingResourceGroup("rg")
            .withManagedBy(
                "/subscriptions/34adfa4f-cedf-4dc0-ba29-b6d1a69ab345/resourceGroups/rg/providers/Microsoft.AppPlatform/Spring/springapp")
            .withEnvironmentId(
                "/subscriptions/34adfa4f-cedf-4dc0-ba29-b6d1a69ab345/resourceGroups/rg/providers/Microsoft.App/managedEnvironments/demokube")
            .withConfiguration(new Configuration().withIngress(new Ingress().withExternal(true)
                .withTargetPort(3000)
                .withExposedPort(4000)
                .withTransport(IngressTransportMethod.TCP)
                .withTraffic(Arrays
                    .asList(new TrafficWeight().withRevisionName("testcontainerappmanagedby-ab1234").withWeight(100)))))
            .withTemplate(new Template()
                .withContainers(Arrays.asList(new Container().withImage("repo/testcontainerappmanagedby:v1")
                    .withName("testcontainerappmanagedby")
                    .withProbes(Arrays.asList(new ContainerAppProbe().withInitialDelaySeconds(3)
                        .withPeriodSeconds(3)
                        .withTcpSocket(new ContainerAppProbeTcpSocket().withPort(8080))
                        .withType(Type.LIVENESS)))))
                .withScale(new Scale().withMinReplicas(1)
                    .withMaxReplicas(5)
                    .withRules(Arrays.asList(new ScaleRule().withName("tcpscalingrule")
                        .withTcp(new TcpScaleRule().withMetadata(mapOf("concurrentConnections", "50")))))))
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

### ContainerApps_Delete

```java
/**
 * Samples for ContainerApps Delete.
 */
public final class ContainerAppsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/ContainerApps_Delete.json
     */
    /**
     * Sample code: Delete Container App.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void deleteContainerApp(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.containerApps().delete("rg", "testworkerapp0", com.azure.core.util.Context.NONE);
    }
}
```

### ContainerApps_GetAuthToken

```java
/**
 * Samples for ContainerApps GetAuthToken.
 */
public final class ContainerAppsGetAuthTokenSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/ContainerApps_GetAuthToken.json
     */
    /**
     * Sample code: Get Container App Auth Token.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        getContainerAppAuthToken(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.containerApps().getAuthTokenWithResponse("rg", "testcontainerapp0", com.azure.core.util.Context.NONE);
    }
}
```

### ContainerApps_GetByResourceGroup

```java
/**
 * Samples for ContainerApps GetByResourceGroup.
 */
public final class ContainerAppsGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/ContainerApps_Get.json
     */
    /**
     * Sample code: Get Container App.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void getContainerApp(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.containerApps()
            .getByResourceGroupWithResponse("rg", "testcontainerapp0", com.azure.core.util.Context.NONE);
    }
}
```

### ContainerApps_List

```java
/**
 * Samples for ContainerApps List.
 */
public final class ContainerAppsListSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/ContainerApps_ListBySubscription.json
     */
    /**
     * Sample code: List Container Apps by subscription.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        listContainerAppsBySubscription(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.containerApps().list(com.azure.core.util.Context.NONE);
    }
}
```

### ContainerApps_ListByResourceGroup

```java
/**
 * Samples for ContainerApps ListByResourceGroup.
 */
public final class ContainerAppsListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/ContainerApps_ListByResourceGroup.
     * json
     */
    /**
     * Sample code: List Container Apps by resource group.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        listContainerAppsByResourceGroup(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.containerApps().listByResourceGroup("rg", com.azure.core.util.Context.NONE);
    }
}
```

### ContainerApps_ListCustomHostnameAnalysis

```java
/**
 * Samples for ContainerApps ListCustomHostnameAnalysis.
 */
public final class ContainerAppsListCustomHostnameAnalysisSamples {
    /*
     * x-ms-original-file: specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/
     * ContainerApps_ListCustomHostNameAnalysis.json
     */
    /**
     * Sample code: Analyze Custom Hostname.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void analyzeCustomHostname(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.containerApps()
            .listCustomHostnameAnalysisWithResponse("rg", "testcontainerapp0", "my.name.corp",
                com.azure.core.util.Context.NONE);
    }
}
```

### ContainerApps_ListSecrets

```java
/**
 * Samples for ContainerApps ListSecrets.
 */
public final class ContainerAppsListSecretsSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/ContainerApps_ListSecrets.json
     */
    /**
     * Sample code: List Container Apps Secrets.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        listContainerAppsSecrets(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.containerApps().listSecretsWithResponse("rg", "testcontainerapp0", com.azure.core.util.Context.NONE);
    }
}
```

### ContainerApps_Start

```java
/**
 * Samples for ContainerApps Start.
 */
public final class ContainerAppsStartSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/ContainerApps_Start.json
     */
    /**
     * Sample code: Start Container App.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void startContainerApp(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.containerApps().start("rg", "testworkerapp0", com.azure.core.util.Context.NONE);
    }
}
```

### ContainerApps_Stop

```java
/**
 * Samples for ContainerApps Stop.
 */
public final class ContainerAppsStopSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/ContainerApps_Stop.json
     */
    /**
     * Sample code: Stop Container App.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void stopContainerApp(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.containerApps().stop("rg", "testworkerApp0", com.azure.core.util.Context.NONE);
    }
}
```

### ContainerApps_Update

```java
import com.azure.resourcemanager.appcontainers.models.Action;
import com.azure.resourcemanager.appcontainers.models.Affinity;
import com.azure.resourcemanager.appcontainers.models.AppProtocol;
import com.azure.resourcemanager.appcontainers.models.BindingType;
import com.azure.resourcemanager.appcontainers.models.Configuration;
import com.azure.resourcemanager.appcontainers.models.Container;
import com.azure.resourcemanager.appcontainers.models.ContainerApp;
import com.azure.resourcemanager.appcontainers.models.ContainerAppProbe;
import com.azure.resourcemanager.appcontainers.models.ContainerAppProbeHttpGet;
import com.azure.resourcemanager.appcontainers.models.ContainerAppProbeHttpGetHttpHeadersItem;
import com.azure.resourcemanager.appcontainers.models.ContainerResources;
import com.azure.resourcemanager.appcontainers.models.CustomDomain;
import com.azure.resourcemanager.appcontainers.models.CustomScaleRule;
import com.azure.resourcemanager.appcontainers.models.Dapr;
import com.azure.resourcemanager.appcontainers.models.Ingress;
import com.azure.resourcemanager.appcontainers.models.IngressStickySessions;
import com.azure.resourcemanager.appcontainers.models.InitContainer;
import com.azure.resourcemanager.appcontainers.models.IpSecurityRestrictionRule;
import com.azure.resourcemanager.appcontainers.models.LogLevel;
import com.azure.resourcemanager.appcontainers.models.Scale;
import com.azure.resourcemanager.appcontainers.models.ScaleRule;
import com.azure.resourcemanager.appcontainers.models.Service;
import com.azure.resourcemanager.appcontainers.models.ServiceBind;
import com.azure.resourcemanager.appcontainers.models.Template;
import com.azure.resourcemanager.appcontainers.models.TrafficWeight;
import com.azure.resourcemanager.appcontainers.models.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ContainerApps Update.
 */
public final class ContainerAppsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/ContainerApps_Patch.json
     */
    /**
     * Sample code: Patch Container App.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void patchContainerApp(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        ContainerApp resource = manager.containerApps()
            .getByResourceGroupWithResponse("rg", "testcontainerapp0", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("tag1", "value1", "tag2", "value2"))
            .withConfiguration(
                new Configuration()
                    .withIngress(new Ingress().withExternal(true)
                        .withTargetPort(3000)
                        .withTraffic(Arrays.asList(new TrafficWeight()
                            .withRevisionName("testcontainerapp0-ab1234")
                            .withWeight(100)
                            .withLabel("production")))
                        .withCustomDomains(Arrays.asList(new CustomDomain()
                            .withName("www.my-name.com")
                            .withBindingType(BindingType.SNI_ENABLED)
                            .withCertificateId(
                                "/subscriptions/34adfa4f-cedf-4dc0-ba29-b6d1a69ab345/resourceGroups/rg/providers/Microsoft.App/managedEnvironments/demokube/certificates/my-certificate-for-my-name-dot-com"),
                            new CustomDomain().withName("www.my-other-name.com")
                                .withBindingType(BindingType.SNI_ENABLED)
                                .withCertificateId(
                                    "/subscriptions/34adfa4f-cedf-4dc0-ba29-b6d1a69ab345/resourceGroups/rg/providers/Microsoft.App/managedEnvironments/demokube/certificates/my-certificate-for-my-other-name-dot-com")))
                        .withIpSecurityRestrictions(
                            Arrays
                                .asList(
                                    new IpSecurityRestrictionRule().withName("Allow work IP A subnet")
                                        .withDescription(
                                            "Allowing all IP's within the subnet below to access containerapp")
                                        .withIpAddressRange("192.168.1.1/32")
                                        .withAction(Action.ALLOW),
                                    new IpSecurityRestrictionRule().withName("Allow work IP B subnet")
                                        .withDescription(
                                            "Allowing all IP's within the subnet below to access containerapp")
                                        .withIpAddressRange("192.168.1.1/8")
                                        .withAction(Action.ALLOW)))
                        .withStickySessions(new IngressStickySessions().withAffinity(Affinity.STICKY)))
                    .withDapr(new Dapr().withEnabled(true)
                        .withAppProtocol(AppProtocol.HTTP)
                        .withAppPort(3000)
                        .withHttpReadBufferSize(30)
                        .withHttpMaxRequestSize(10)
                        .withLogLevel(LogLevel.DEBUG)
                        .withEnableApiLogging(true))
                    .withMaxInactiveRevisions(10)
                    .withService(new Service().withType("redis")))
            .withTemplate(new Template()
                .withInitContainers(Arrays.asList(new InitContainer().withImage("repo/testcontainerapp0:v4")
                    .withName("testinitcontainerApp0")
                    .withResources(new ContainerResources().withCpu(0.5D).withMemory("1Gi"))))
                .withContainers(Arrays.asList(new Container()
                    .withImage("repo/testcontainerapp0:v1")
                    .withName("testcontainerapp0")
                    .withProbes(
                        Arrays.asList(new ContainerAppProbe().withHttpGet(new ContainerAppProbeHttpGet()
                            .withHttpHeaders(
                                Arrays.asList(new ContainerAppProbeHttpGetHttpHeadersItem().withName("Custom-Header")
                                    .withValue("Awesome")))
                            .withPath("/health")
                            .withPort(8080)).withInitialDelaySeconds(3).withPeriodSeconds(3).withType(Type.LIVENESS)))))
                .withScale(
                    new Scale().withMinReplicas(1)
                        .withMaxReplicas(5)
                        .withRules(
                            Arrays.asList(new ScaleRule().withName("httpscalingrule")
                                .withCustom(new CustomScaleRule().withType("http")
                                    .withMetadata(mapOf("concurrentRequests", "50"))))))
                .withServiceBinds(Arrays.asList(new ServiceBind().withServiceId(
                    "/subscriptions/34adfa4f-cedf-4dc0-ba29-b6d1a69ab345/resourceGroups/rg/providers/Microsoft.App/containerApps/service")
                    .withName("service"))))
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

### ContainerAppsAuthConfigs_CreateOrUpdate

```java
import com.azure.resourcemanager.appcontainers.models.AppRegistration;
import com.azure.resourcemanager.appcontainers.models.AuthPlatform;
import com.azure.resourcemanager.appcontainers.models.EncryptionSettings;
import com.azure.resourcemanager.appcontainers.models.Facebook;
import com.azure.resourcemanager.appcontainers.models.GlobalValidation;
import com.azure.resourcemanager.appcontainers.models.IdentityProviders;
import com.azure.resourcemanager.appcontainers.models.UnauthenticatedClientActionV2;

/**
 * Samples for ContainerAppsAuthConfigs CreateOrUpdate.
 */
public final class ContainerAppsAuthConfigsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/AuthConfigs_CreateOrUpdate.json
     */
    /**
     * Sample code: Create or Update Container App AuthConfig.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        createOrUpdateContainerAppAuthConfig(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.containerAppsAuthConfigs()
            .define("current")
            .withExistingContainerApp("workerapps-rg-xj", "testcanadacentral")
            .withPlatform(new AuthPlatform().withEnabled(true))
            .withGlobalValidation(
                new GlobalValidation().withUnauthenticatedClientAction(UnauthenticatedClientActionV2.ALLOW_ANONYMOUS))
            .withIdentityProviders(new IdentityProviders().withFacebook(new Facebook().withRegistration(
                new AppRegistration().withAppId("123").withAppSecretSettingName("fakeTokenPlaceholder"))))
            .withEncryptionSettings(
                new EncryptionSettings().withContainerAppAuthEncryptionSecretName("fakeTokenPlaceholder")
                    .withContainerAppAuthSigningSecretName("fakeTokenPlaceholder"))
            .create();
    }
}
```

### ContainerAppsAuthConfigs_Delete

```java
/**
 * Samples for ContainerAppsAuthConfigs Delete.
 */
public final class ContainerAppsAuthConfigsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/AuthConfigs_Delete.json
     */
    /**
     * Sample code: Delete Container App AuthConfig.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        deleteContainerAppAuthConfig(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.containerAppsAuthConfigs()
            .deleteWithResponse("workerapps-rg-xj", "testcanadacentral", "current", com.azure.core.util.Context.NONE);
    }
}
```

### ContainerAppsAuthConfigs_Get

```java
/**
 * Samples for ContainerAppsAuthConfigs Get.
 */
public final class ContainerAppsAuthConfigsGetSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/AuthConfigs_Get.json
     */
    /**
     * Sample code: Get Container App's AuthConfig.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        getContainerAppSAuthConfig(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.containerAppsAuthConfigs()
            .getWithResponse("workerapps-rg-xj", "testcanadacentral", "current", com.azure.core.util.Context.NONE);
    }
}
```

### ContainerAppsAuthConfigs_ListByContainerApp

```java
/**
 * Samples for ContainerAppsAuthConfigs ListByContainerApp.
 */
public final class ContainerAppsAuthConfigsListByContainerAppSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/AuthConfigs_ListByContainer.json
     */
    /**
     * Sample code: List Auth Configs by Container Apps.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        listAuthConfigsByContainerApps(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.containerAppsAuthConfigs()
            .listByContainerApp("workerapps-rg-xj", "testcanadacentral", com.azure.core.util.Context.NONE);
    }
}
```

### ContainerAppsDiagnostics_GetDetector

```java
/**
 * Samples for ContainerAppsDiagnostics GetDetector.
 */
public final class ContainerAppsDiagnosticsGetDetectorSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/ContainerAppsDiagnostics_Get.json
     */
    /**
     * Sample code: Get Container App's diagnostics info.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        getContainerAppSDiagnosticsInfo(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.containerAppsDiagnostics()
            .getDetectorWithResponse("mikono-workerapp-test-rg", "mikono-capp-stage1", "cappcontainerappnetworkIO",
                com.azure.core.util.Context.NONE);
    }
}
```

### ContainerAppsDiagnostics_GetRevision

```java
/**
 * Samples for ContainerAppsDiagnostics GetRevision.
 */
public final class ContainerAppsDiagnosticsGetRevisionSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/Revisions_Get.json
     */
    /**
     * Sample code: Get Container App's revision.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        getContainerAppSRevision(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.containerAppsDiagnostics()
            .getRevisionWithResponse("rg", "testcontainerApp0", "testcontainerApp0-pjxhsye",
                com.azure.core.util.Context.NONE);
    }
}
```

### ContainerAppsDiagnostics_GetRoot

```java
/**
 * Samples for ContainerAppsDiagnostics GetRoot.
 */
public final class ContainerAppsDiagnosticsGetRootSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/ContainerApps_Get.json
     */
    /**
     * Sample code: Get Container App.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void getContainerApp(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.containerAppsDiagnostics()
            .getRootWithResponse("rg", "testcontainerapp0", com.azure.core.util.Context.NONE);
    }
}
```

### ContainerAppsDiagnostics_ListDetectors

```java
/**
 * Samples for ContainerAppsDiagnostics ListDetectors.
 */
public final class ContainerAppsDiagnosticsListDetectorsSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/ContainerAppsDiagnostics_List.json
     */
    /**
     * Sample code: Get the list of available diagnostics for a given Container App.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void getTheListOfAvailableDiagnosticsForAGivenContainerApp(
        com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.containerAppsDiagnostics()
            .listDetectors("mikono-workerapp-test-rg", "mikono-capp-stage1", com.azure.core.util.Context.NONE);
    }
}
```

### ContainerAppsDiagnostics_ListRevisions

```java
/**
 * Samples for ContainerAppsDiagnostics ListRevisions.
 */
public final class ContainerAppsDiagnosticsListRevisionsSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/Revisions_List.json
     */
    /**
     * Sample code: List Container App's revisions.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        listContainerAppSRevisions(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.containerAppsDiagnostics()
            .listRevisions("rg", "testcontainerApp0", null, com.azure.core.util.Context.NONE);
    }
}
```

### ContainerAppsRevisionReplicas_GetReplica

```java
/**
 * Samples for ContainerAppsRevisionReplicas GetReplica.
 */
public final class ContainerAppsRevisionReplicasGetReplicaSamples {
    /*
     * x-ms-original-file: specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/Replicas_Get.json
     */
    /**
     * Sample code: Get Container App's revision replica.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        getContainerAppSRevisionReplica(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.containerAppsRevisionReplicas()
            .getReplicaWithResponse("workerapps-rg-xj", "myapp", "myapp--0wlqy09", "myapp--0wlqy09-5d9774cff-5wnd8",
                com.azure.core.util.Context.NONE);
    }
}
```

### ContainerAppsRevisionReplicas_ListReplicas

```java
/**
 * Samples for ContainerAppsRevisionReplicas ListReplicas.
 */
public final class ContainerAppsRevisionReplicasListReplicasSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/Replicas_List.json
     */
    /**
     * Sample code: List Container App's replicas.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        listContainerAppSReplicas(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.containerAppsRevisionReplicas()
            .listReplicasWithResponse("workerapps-rg-xj", "myapp", "myapp--0wlqy09", com.azure.core.util.Context.NONE);
    }
}
```

### ContainerAppsRevisions_ActivateRevision

```java
/**
 * Samples for ContainerAppsRevisions ActivateRevision.
 */
public final class ContainerAppsRevisionsActivateRevisionSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/Revisions_Activate.json
     */
    /**
     * Sample code: Activate Container App's revision.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        activateContainerAppSRevision(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.containerAppsRevisions()
            .activateRevisionWithResponse("rg", "testcontainerApp0", "testcontainerApp0-pjxhsye",
                com.azure.core.util.Context.NONE);
    }
}
```

### ContainerAppsRevisions_DeactivateRevision

```java
/**
 * Samples for ContainerAppsRevisions DeactivateRevision.
 */
public final class ContainerAppsRevisionsDeactivateRevisionSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/Revisions_Deactivate.json
     */
    /**
     * Sample code: Deactivate Container App's revision.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        deactivateContainerAppSRevision(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.containerAppsRevisions()
            .deactivateRevisionWithResponse("rg", "testcontainerApp0", "testcontainerApp0-pjxhsye",
                com.azure.core.util.Context.NONE);
    }
}
```

### ContainerAppsRevisions_GetRevision

```java
/**
 * Samples for ContainerAppsRevisions GetRevision.
 */
public final class ContainerAppsRevisionsGetRevisionSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/Revisions_Get.json
     */
    /**
     * Sample code: Get Container App's revision.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        getContainerAppSRevision(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.containerAppsRevisions()
            .getRevisionWithResponse("rg", "testcontainerApp0", "testcontainerApp0-pjxhsye",
                com.azure.core.util.Context.NONE);
    }
}
```

### ContainerAppsRevisions_ListRevisions

```java
/**
 * Samples for ContainerAppsRevisions ListRevisions.
 */
public final class ContainerAppsRevisionsListRevisionsSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/Revisions_List.json
     */
    /**
     * Sample code: List Container App's revisions.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        listContainerAppSRevisions(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.containerAppsRevisions()
            .listRevisions("rg", "testcontainerApp0", null, com.azure.core.util.Context.NONE);
    }
}
```

### ContainerAppsRevisions_RestartRevision

```java
/**
 * Samples for ContainerAppsRevisions RestartRevision.
 */
public final class ContainerAppsRevisionsRestartRevisionSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/Revisions_Restart.json
     */
    /**
     * Sample code: Restart Container App's revision.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        restartContainerAppSRevision(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.containerAppsRevisions()
            .restartRevisionWithResponse("rg", "testStaticSite0", "testcontainerApp0-pjxhsye",
                com.azure.core.util.Context.NONE);
    }
}
```

### ContainerAppsSourceControls_CreateOrUpdate

```java
import com.azure.resourcemanager.appcontainers.models.AzureCredentials;
import com.azure.resourcemanager.appcontainers.models.GithubActionConfiguration;
import com.azure.resourcemanager.appcontainers.models.RegistryInfo;

/**
 * Samples for ContainerAppsSourceControls CreateOrUpdate.
 */
public final class ContainerAppsSourceControlsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/SourceControls_CreateOrUpdate.json
     */
    /**
     * Sample code: Create or Update Container App SourceControl.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void createOrUpdateContainerAppSourceControl(
        com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.containerAppsSourceControls()
            .define("current")
            .withExistingContainerApp("workerapps-rg-xj", "testcanadacentral")
            .withRepoUrl("https://github.com/xwang971/ghatest")
            .withBranch("master")
            .withGithubActionConfiguration(new GithubActionConfiguration()
                .withRegistryInfo(new RegistryInfo().withRegistryUrl("test-registry.azurecr.io")
                    .withRegistryUsername("test-registry")
                    .withRegistryPassword("fakeTokenPlaceholder"))
                .withAzureCredentials(new AzureCredentials().withClientId("<clientid>")
                    .withClientSecret("fakeTokenPlaceholder")
                    .withTenantId("<tenantid>")
                    .withKind("feaderated"))
                .withContextPath("./")
                .withGithubPersonalAccessToken("fakeTokenPlaceholder")
                .withImage("image/tag"))
            .create();
    }
}
```

### ContainerAppsSourceControls_Delete

```java
/**
 * Samples for ContainerAppsSourceControls Delete.
 */
public final class ContainerAppsSourceControlsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/SourceControls_Delete.json
     */
    /**
     * Sample code: Delete Container App SourceControl.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        deleteContainerAppSourceControl(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.containerAppsSourceControls()
            .delete("workerapps-rg-xj", "testcanadacentral", "current", com.azure.core.util.Context.NONE);
    }
}
```

### ContainerAppsSourceControls_Get

```java
/**
 * Samples for ContainerAppsSourceControls Get.
 */
public final class ContainerAppsSourceControlsGetSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/SourceControls_Get.json
     */
    /**
     * Sample code: Get Container App's SourceControl.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        getContainerAppSSourceControl(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.containerAppsSourceControls()
            .getWithResponse("workerapps-rg-xj", "testcanadacentral", "current", com.azure.core.util.Context.NONE);
    }
}
```

### ContainerAppsSourceControls_ListByContainerApp

```java
/**
 * Samples for ContainerAppsSourceControls ListByContainerApp.
 */
public final class ContainerAppsSourceControlsListByContainerAppSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/SourceControls_ListByContainer.json
     */
    /**
     * Sample code: List App's Source Controls.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void listAppSSourceControls(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.containerAppsSourceControls()
            .listByContainerApp("workerapps-rg-xj", "testcanadacentral", com.azure.core.util.Context.NONE);
    }
}
```

### DaprComponents_CreateOrUpdate

```java
import com.azure.resourcemanager.appcontainers.fluent.models.DaprComponentInner;
import com.azure.resourcemanager.appcontainers.models.DaprMetadata;
import com.azure.resourcemanager.appcontainers.models.Secret;
import java.util.Arrays;

/**
 * Samples for DaprComponents CreateOrUpdate.
 */
public final class DaprComponentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/DaprComponents_CreateOrUpdate_Secrets
     * .json
     */
    /**
     * Sample code: Create or update dapr component with secrets.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void createOrUpdateDaprComponentWithSecrets(
        com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.daprComponents()
            .createOrUpdateWithResponse("examplerg", "myenvironment", "reddog",
                new DaprComponentInner().withComponentType("state.azure.cosmosdb")
                    .withVersion("v1")
                    .withIgnoreErrors(false)
                    .withInitTimeout("50s")
                    .withSecrets(Arrays.asList(new Secret().withName("masterkey").withValue("keyvalue")))
                    .withMetadata(Arrays.asList(new DaprMetadata().withName("url").withValue("<COSMOS-URL>"),
                        new DaprMetadata().withName("database").withValue("itemsDB"),
                        new DaprMetadata().withName("collection").withValue("items"),
                        new DaprMetadata().withName("masterkey").withSecretRef("fakeTokenPlaceholder")))
                    .withScopes(Arrays.asList("container-app-1", "container-app-2")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/
     * DaprComponents_CreateOrUpdate_SecretStoreComponent.json
     */
    /**
     * Sample code: Create or update dapr component with secret store component.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void createOrUpdateDaprComponentWithSecretStoreComponent(
        com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.daprComponents()
            .createOrUpdateWithResponse("examplerg", "myenvironment", "reddog",
                new DaprComponentInner().withComponentType("state.azure.cosmosdb")
                    .withVersion("v1")
                    .withIgnoreErrors(false)
                    .withInitTimeout("50s")
                    .withSecretStoreComponent("fakeTokenPlaceholder")
                    .withMetadata(Arrays.asList(new DaprMetadata().withName("url").withValue("<COSMOS-URL>"),
                        new DaprMetadata().withName("database").withValue("itemsDB"),
                        new DaprMetadata().withName("collection").withValue("items"),
                        new DaprMetadata().withName("masterkey").withSecretRef("fakeTokenPlaceholder")))
                    .withScopes(Arrays.asList("container-app-1", "container-app-2")),
                com.azure.core.util.Context.NONE);
    }
}
```

### DaprComponents_Delete

```java
/**
 * Samples for DaprComponents Delete.
 */
public final class DaprComponentsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/DaprComponents_Delete.json
     */
    /**
     * Sample code: Delete dapr component.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void deleteDaprComponent(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.daprComponents()
            .deleteWithResponse("examplerg", "myenvironment", "reddog", com.azure.core.util.Context.NONE);
    }
}
```

### DaprComponents_Get

```java
/**
 * Samples for DaprComponents Get.
 */
public final class DaprComponentsGetSamples {
    /*
     * x-ms-original-file: specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/
     * DaprComponents_Get_SecretStoreComponent.json
     */
    /**
     * Sample code: Get Dapr Component with secret store component.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void getDaprComponentWithSecretStoreComponent(
        com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.daprComponents()
            .getWithResponse("examplerg", "myenvironment", "reddog", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/DaprComponents_Get_Secrets.json
     */
    /**
     * Sample code: Get Dapr Component with secrets.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        getDaprComponentWithSecrets(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.daprComponents()
            .getWithResponse("examplerg", "myenvironment", "reddog", com.azure.core.util.Context.NONE);
    }
}
```

### DaprComponents_List

```java
/**
 * Samples for DaprComponents List.
 */
public final class DaprComponentsListSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/DaprComponents_List.json
     */
    /**
     * Sample code: List Dapr Components.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void listDaprComponents(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.daprComponents().list("examplerg", "myenvironment", com.azure.core.util.Context.NONE);
    }
}
```

### DaprComponents_ListSecrets

```java
/**
 * Samples for DaprComponents ListSecrets.
 */
public final class DaprComponentsListSecretsSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/DaprComponents_ListSecrets.json
     */
    /**
     * Sample code: List Container Apps Secrets.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        listContainerAppsSecrets(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.daprComponents()
            .listSecretsWithResponse("examplerg", "myenvironment", "reddog", com.azure.core.util.Context.NONE);
    }
}
```

### Jobs_CreateOrUpdate

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.appcontainers.models.Container;
import com.azure.resourcemanager.appcontainers.models.ContainerAppProbe;
import com.azure.resourcemanager.appcontainers.models.ContainerAppProbeHttpGet;
import com.azure.resourcemanager.appcontainers.models.ContainerAppProbeHttpGetHttpHeadersItem;
import com.azure.resourcemanager.appcontainers.models.ContainerResources;
import com.azure.resourcemanager.appcontainers.models.InitContainer;
import com.azure.resourcemanager.appcontainers.models.JobConfiguration;
import com.azure.resourcemanager.appcontainers.models.JobConfigurationEventTriggerConfig;
import com.azure.resourcemanager.appcontainers.models.JobConfigurationManualTriggerConfig;
import com.azure.resourcemanager.appcontainers.models.JobScale;
import com.azure.resourcemanager.appcontainers.models.JobScaleRule;
import com.azure.resourcemanager.appcontainers.models.JobTemplate;
import com.azure.resourcemanager.appcontainers.models.TriggerType;
import com.azure.resourcemanager.appcontainers.models.Type;
import java.io.IOException;
import java.util.Arrays;

/**
 * Samples for Jobs CreateOrUpdate.
 */
public final class JobsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/Job_CreateorUpdate_EventTrigger.json
     */
    /**
     * Sample code: Create or Update Container Apps Job With Event Driven Trigger.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void createOrUpdateContainerAppsJobWithEventDrivenTrigger(
        com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) throws IOException {
        manager.jobs()
            .define("testcontainerappsjob0")
            .withRegion("East US")
            .withExistingResourceGroup("rg")
            .withEnvironmentId(
                "/subscriptions/34adfa4f-cedf-4dc0-ba29-b6d1a69ab345/resourceGroups/rg/providers/Microsoft.App/managedEnvironments/demokube")
            .withConfiguration(new JobConfiguration().withTriggerType(TriggerType.EVENT)
                .withReplicaTimeout(10)
                .withReplicaRetryLimit(10)
                .withEventTriggerConfig(new JobConfigurationEventTriggerConfig().withReplicaCompletionCount(1)
                    .withParallelism(4)
                    .withScale(new JobScale().withPollingInterval(40)
                        .withMinExecutions(1)
                        .withMaxExecutions(5)
                        .withRules(Arrays.asList(new JobScaleRule().withName("servicebuscalingrule")
                            .withType("azure-servicebus")
                            .withMetadata(SerializerFactory.createDefaultManagementSerializerAdapter()
                                .deserialize("{\"topicName\":\"my-topic\"}", Object.class,
                                    SerializerEncoding.JSON)))))))
            .withTemplate(new JobTemplate()
                .withInitContainers(Arrays.asList(new InitContainer().withImage("repo/testcontainerappsjob0:v4")
                    .withName("testinitcontainerAppsJob0")
                    .withCommand(Arrays.asList("/bin/sh"))
                    .withArgs(Arrays.asList("-c", "while true; do echo hello; sleep 10;done"))
                    .withResources(new ContainerResources().withCpu(0.5D).withMemory("1Gi"))))
                .withContainers(Arrays.asList(
                    new Container().withImage("repo/testcontainerappsjob0:v1").withName("testcontainerappsjob0"))))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/Job_CreateorUpdate.json
     */
    /**
     * Sample code: Create or Update Container Apps Job.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        createOrUpdateContainerAppsJob(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.jobs()
            .define("testcontainerappsjob0")
            .withRegion("East US")
            .withExistingResourceGroup("rg")
            .withEnvironmentId(
                "/subscriptions/34adfa4f-cedf-4dc0-ba29-b6d1a69ab345/resourceGroups/rg/providers/Microsoft.App/managedEnvironments/demokube")
            .withConfiguration(new JobConfiguration().withTriggerType(TriggerType.MANUAL)
                .withReplicaTimeout(10)
                .withReplicaRetryLimit(10)
                .withManualTriggerConfig(
                    new JobConfigurationManualTriggerConfig().withReplicaCompletionCount(1).withParallelism(4)))
            .withTemplate(new JobTemplate()
                .withInitContainers(Arrays.asList(new InitContainer().withImage("repo/testcontainerappsjob0:v4")
                    .withName("testinitcontainerAppsJob0")
                    .withCommand(Arrays.asList("/bin/sh"))
                    .withArgs(Arrays.asList("-c", "while true; do echo hello; sleep 10;done"))
                    .withResources(new ContainerResources().withCpu(0.5D).withMemory("1Gi"))))
                .withContainers(Arrays.asList(new Container().withImage("repo/testcontainerappsjob0:v1")
                    .withName("testcontainerappsjob0")
                    .withProbes(Arrays.asList(new ContainerAppProbe()
                        .withHttpGet(new ContainerAppProbeHttpGet()
                            .withHttpHeaders(
                                Arrays.asList(new ContainerAppProbeHttpGetHttpHeadersItem().withName("Custom-Header")
                                    .withValue("Awesome")))
                            .withPath("/health")
                            .withPort(8080))
                        .withInitialDelaySeconds(5)
                        .withPeriodSeconds(3)
                        .withType(Type.LIVENESS))))))
            .create();
    }
}
```

### Jobs_Delete

```java
/**
 * Samples for Jobs Delete.
 */
public final class JobsDeleteSamples {
    /*
     * x-ms-original-file: specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/Job_Delete.json
     */
    /**
     * Sample code: Delete Container Apps Job.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void deleteContainerAppsJob(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.jobs().delete("rg", "testworkercontainerappsjob0", com.azure.core.util.Context.NONE);
    }
}
```

### Jobs_GetByResourceGroup

```java
/**
 * Samples for Jobs GetByResourceGroup.
 */
public final class JobsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/Job_Get.json
     */
    /**
     * Sample code: Get Container Apps Job.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void getContainerAppsJob(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.jobs().getByResourceGroupWithResponse("rg", "testcontainerappsjob0", com.azure.core.util.Context.NONE);
    }
}
```

### Jobs_GetDetector

```java
/**
 * Samples for Jobs GetDetector.
 */
public final class JobsGetDetectorSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/Job_GetDetector.json
     */
    /**
     * Sample code: Get diagnostic data for a Container App Job.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        getDiagnosticDataForAContainerAppJob(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.jobs()
            .getDetectorWithResponse("mikono-workerapp-test-rg", "mikonojob1", "containerappjobnetworkIO",
                com.azure.core.util.Context.NONE);
    }
}
```

### Jobs_List

```java
/**
 * Samples for Jobs List.
 */
public final class JobsListSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/Jobs_ListBySubscription.json
     */
    /**
     * Sample code: List Container Apps Jobs by subscription.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        listContainerAppsJobsBySubscription(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.jobs().list(com.azure.core.util.Context.NONE);
    }
}
```

### Jobs_ListByResourceGroup

```java
/**
 * Samples for Jobs ListByResourceGroup.
 */
public final class JobsListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/Jobs_ListByResourceGroup.json
     */
    /**
     * Sample code: List Container Apps Jobs by resource group.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        listContainerAppsJobsByResourceGroup(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.jobs().listByResourceGroup("rg", com.azure.core.util.Context.NONE);
    }
}
```

### Jobs_ListDetectors

```java
/**
 * Samples for Jobs ListDetectors.
 */
public final class JobsListDetectorsSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/Job_ListDetectors.json
     */
    /**
     * Sample code: Get the list of available diagnostic data for a Container App Job.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void getTheListOfAvailableDiagnosticDataForAContainerAppJob(
        com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.jobs().listDetectors("mikono-workerapp-test-rg", "mikonojob1", com.azure.core.util.Context.NONE);
    }
}
```

### Jobs_ListSecrets

```java
/**
 * Samples for Jobs ListSecrets.
 */
public final class JobsListSecretsSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/Job_ListSecrets.json
     */
    /**
     * Sample code: List Container Apps Job Secrets.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        listContainerAppsJobSecrets(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.jobs().listSecretsWithResponse("rg", "testcontainerappsjob0", com.azure.core.util.Context.NONE);
    }
}
```

### Jobs_ProxyGet

```java
/**
 * Samples for Jobs ProxyGet.
 */
public final class JobsProxyGetSamples {
    /*
     * x-ms-original-file: specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/Job_ProxyGet.json
     */
    /**
     * Sample code: Get Container App Job by name.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        getContainerAppJobByName(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.jobs().proxyGetWithResponse("rg", "testcontainerappsjob0", "rootApi", com.azure.core.util.Context.NONE);
    }
}
```

### Jobs_Start

```java
import com.azure.resourcemanager.appcontainers.models.ContainerResources;
import com.azure.resourcemanager.appcontainers.models.JobExecutionContainer;
import com.azure.resourcemanager.appcontainers.models.JobExecutionTemplate;
import java.util.Arrays;

/**
 * Samples for Jobs Start.
 */
public final class JobsStartSamples {
    /*
     * x-ms-original-file: specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/Job_Start.json
     */
    /**
     * Sample code: Run a Container Apps Job.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void runAContainerAppsJob(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.jobs()
            .start("rg", "testcontainerappsjob0", new JobExecutionTemplate()
                .withContainers(Arrays.asList(new JobExecutionContainer().withImage("repo/testcontainerappsjob0:v4")
                    .withName("testcontainerappsjob0")
                    .withResources(new ContainerResources().withCpu(0.5D).withMemory("1Gi"))))
                .withInitContainers(Arrays.asList(new JobExecutionContainer().withImage("repo/testcontainerappsjob0:v4")
                    .withName("testinitcontainerAppsJob0")
                    .withCommand(Arrays.asList("/bin/sh"))
                    .withArgs(Arrays.asList("-c", "while true; do echo hello; sleep 10;done"))
                    .withResources(new ContainerResources().withCpu(0.5D).withMemory("1Gi")))),
                com.azure.core.util.Context.NONE);
    }
}
```

### Jobs_StopExecution

```java
/**
 * Samples for Jobs StopExecution.
 */
public final class JobsStopExecutionSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/Job_Stop_Execution.json
     */
    /**
     * Sample code: Terminate a Container Apps Job.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        terminateAContainerAppsJob(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.jobs().stopExecution("rg", "testcontainerappsjob0", "jobExecution1", com.azure.core.util.Context.NONE);
    }
}
```

### Jobs_StopMultipleExecutions

```java
/**
 * Samples for Jobs StopMultipleExecutions.
 */
public final class JobsStopMultipleExecutionsSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/Job_Stop_Multiple.json
     */
    /**
     * Sample code: Terminate Multiple Container Apps Job.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        terminateMultipleContainerAppsJob(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.jobs().stopMultipleExecutions("rg", "testcontainerappsjob0", com.azure.core.util.Context.NONE);
    }
}
```

### Jobs_Update

```java
import com.azure.resourcemanager.appcontainers.models.Container;
import com.azure.resourcemanager.appcontainers.models.ContainerAppProbe;
import com.azure.resourcemanager.appcontainers.models.ContainerAppProbeHttpGet;
import com.azure.resourcemanager.appcontainers.models.ContainerAppProbeHttpGetHttpHeadersItem;
import com.azure.resourcemanager.appcontainers.models.ContainerResources;
import com.azure.resourcemanager.appcontainers.models.InitContainer;
import com.azure.resourcemanager.appcontainers.models.Job;
import com.azure.resourcemanager.appcontainers.models.JobConfiguration;
import com.azure.resourcemanager.appcontainers.models.JobConfigurationManualTriggerConfig;
import com.azure.resourcemanager.appcontainers.models.JobPatchPropertiesProperties;
import com.azure.resourcemanager.appcontainers.models.JobTemplate;
import com.azure.resourcemanager.appcontainers.models.TriggerType;
import com.azure.resourcemanager.appcontainers.models.Type;
import java.util.Arrays;

/**
 * Samples for Jobs Update.
 */
public final class JobsUpdateSamples {
    /*
     * x-ms-original-file: specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/Job_Patch.json
     */
    /**
     * Sample code: Patch Container Apps Job.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void patchContainerAppsJob(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        Job resource = manager.jobs()
            .getByResourceGroupWithResponse("rg", "testcontainerappsjob0", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new JobPatchPropertiesProperties()
                .withConfiguration(new JobConfiguration().withTriggerType(TriggerType.MANUAL)
                    .withReplicaTimeout(10)
                    .withReplicaRetryLimit(10)
                    .withManualTriggerConfig(
                        new JobConfigurationManualTriggerConfig().withReplicaCompletionCount(1).withParallelism(4)))
                .withTemplate(new JobTemplate()
                    .withInitContainers(Arrays.asList(new InitContainer().withImage("repo/testcontainerappsjob0:v4")
                        .withName("testinitcontainerAppsJob0")
                        .withCommand(Arrays.asList("/bin/sh"))
                        .withArgs(Arrays.asList("-c", "while true; do echo hello; sleep 10;done"))
                        .withResources(new ContainerResources().withCpu(0.5D).withMemory("1Gi"))))
                    .withContainers(Arrays.asList(new Container().withImage("repo/testcontainerappsjob0:v1")
                        .withName("testcontainerappsjob0")
                        .withProbes(Arrays.asList(new ContainerAppProbe()
                            .withHttpGet(new ContainerAppProbeHttpGet()
                                .withHttpHeaders(Arrays
                                    .asList(new ContainerAppProbeHttpGetHttpHeadersItem().withName("Custom-Header")
                                        .withValue("Awesome")))
                                .withPath("/health")
                                .withPort(8080))
                            .withInitialDelaySeconds(3)
                            .withPeriodSeconds(3)
                            .withType(Type.LIVENESS)))))))
            .apply();
    }
}
```

### JobsExecutions_List

```java
/**
 * Samples for JobsExecutions List.
 */
public final class JobsExecutionsListSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/Job_Executions_Get.json
     */
    /**
     * Sample code: Get a Container Apps Job Executions.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        getAContainerAppsJobExecutions(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.jobsExecutions().list("rg", "testcontainerappsjob0", null, com.azure.core.util.Context.NONE);
    }
}
```

### ManagedCertificates_CreateOrUpdate

```java
import com.azure.resourcemanager.appcontainers.models.ManagedCertificateDomainControlValidation;
import com.azure.resourcemanager.appcontainers.models.ManagedCertificateProperties;

/**
 * Samples for ManagedCertificates CreateOrUpdate.
 */
public final class ManagedCertificatesCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/ManagedCertificate_CreateOrUpdate.
     * json
     */
    /**
     * Sample code: Create or Update Certificate.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        createOrUpdateCertificate(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.managedCertificates()
            .define("certificate-firendly-name")
            .withRegion("East US")
            .withExistingManagedEnvironment("examplerg", "testcontainerenv")
            .withProperties(new ManagedCertificateProperties().withSubjectName("my-subject-name.company.country.net")
                .withDomainControlValidation(ManagedCertificateDomainControlValidation.CNAME))
            .create();
    }
}
```

### ManagedCertificates_Delete

```java
/**
 * Samples for ManagedCertificates Delete.
 */
public final class ManagedCertificatesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/ManagedCertificate_Delete.json
     */
    /**
     * Sample code: Delete Certificate.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void deleteCertificate(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.managedCertificates()
            .deleteWithResponse("examplerg", "testcontainerenv", "certificate-firendly-name",
                com.azure.core.util.Context.NONE);
    }
}
```

### ManagedCertificates_Get

```java
/**
 * Samples for ManagedCertificates Get.
 */
public final class ManagedCertificatesGetSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/ManagedCertificate_Get.json
     */
    /**
     * Sample code: Get Certificate.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void getCertificate(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.managedCertificates()
            .getWithResponse("examplerg", "testcontainerenv", "certificate-firendly-name",
                com.azure.core.util.Context.NONE);
    }
}
```

### ManagedCertificates_List

```java
/**
 * Samples for ManagedCertificates List.
 */
public final class ManagedCertificatesListSamples {
    /*
     * x-ms-original-file: specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/
     * ManagedCertificates_ListByManagedEnvironment.json
     */
    /**
     * Sample code: List Managed Certificates by Managed Environment.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void listManagedCertificatesByManagedEnvironment(
        com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.managedCertificates().list("examplerg", "testcontainerenv", com.azure.core.util.Context.NONE);
    }
}
```

### ManagedCertificates_Update

```java
import com.azure.resourcemanager.appcontainers.models.ManagedCertificate;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ManagedCertificates Update.
 */
public final class ManagedCertificatesUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/ManagedCertificates_Patch.json
     */
    /**
     * Sample code: Patch Managed Certificate.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        patchManagedCertificate(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        ManagedCertificate resource = manager.managedCertificates()
            .getWithResponse("examplerg", "testcontainerenv", "certificate-firendly-name",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### ManagedEnvironmentDiagnostics_GetDetector

```java
/**
 * Samples for ManagedEnvironmentDiagnostics GetDetector.
 */
public final class ManagedEnvironmentDiagnosticsGetDetectorSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/ManagedEnvironmentDiagnostics_Get.
     * json
     */
    /**
     * Sample code: Get diagnostic data for a managed environments.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void getDiagnosticDataForAManagedEnvironments(
        com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.managedEnvironmentDiagnostics()
            .getDetectorWithResponse("mikono-workerapp-test-rg", "mikonokubeenv", "ManagedEnvAvailabilityMetrics",
                com.azure.core.util.Context.NONE);
    }
}
```

### ManagedEnvironmentDiagnostics_ListDetectors

```java
/**
 * Samples for ManagedEnvironmentDiagnostics ListDetectors.
 */
public final class ManagedEnvironmentDiagnosticsListDetectorsSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/ManagedEnvironmentDiagnostics_List.
     * json
     */
    /**
     * Sample code: Get the list of available diagnostic data for a managed environments.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void getTheListOfAvailableDiagnosticDataForAManagedEnvironments(
        com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.managedEnvironmentDiagnostics()
            .listDetectorsWithResponse("mikono-workerapp-test-rg", "mikonokubeenv", com.azure.core.util.Context.NONE);
    }
}
```

### ManagedEnvironmentUsages_List

```java
/**
 * Samples for ManagedEnvironmentUsages List.
 */
public final class ManagedEnvironmentUsagesListSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/ManagedEnvironmentUsages_List.json
     */
    /**
     * Sample code: List managed environment usages.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        listManagedEnvironmentUsages(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.managedEnvironmentUsages().list("examplerg", "jlaw-demo1", com.azure.core.util.Context.NONE);
    }
}
```

### ManagedEnvironments_CreateOrUpdate

```java
import com.azure.resourcemanager.appcontainers.models.AppLogsConfiguration;
import com.azure.resourcemanager.appcontainers.models.CustomDomainConfiguration;
import com.azure.resourcemanager.appcontainers.models.LogAnalyticsConfiguration;
import com.azure.resourcemanager.appcontainers.models.ManagedEnvironmentPropertiesPeerAuthentication;
import com.azure.resourcemanager.appcontainers.models.ManagedEnvironmentPropertiesPeerTrafficConfiguration;
import com.azure.resourcemanager.appcontainers.models.ManagedEnvironmentPropertiesPeerTrafficConfigurationEncryption;
import com.azure.resourcemanager.appcontainers.models.Mtls;
import com.azure.resourcemanager.appcontainers.models.VnetConfiguration;
import com.azure.resourcemanager.appcontainers.models.WorkloadProfile;
import java.util.Arrays;

/**
 * Samples for ManagedEnvironments CreateOrUpdate.
 */
public final class ManagedEnvironmentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/
     * ManagedEnvironments_CustomInfrastructureResourceGroup_Create.json
     */
    /**
     * Sample code: Create environment with custom infrastructureResourceGroup.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void createEnvironmentWithCustomInfrastructureResourceGroup(
        com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.managedEnvironments()
            .define("testcontainerenv")
            .withRegion("East US")
            .withExistingResourceGroup("examplerg")
            .withDaprAIConnectionString(
                "InstrumentationKey=00000000-0000-0000-0000-000000000000;IngestionEndpoint=https://northcentralus-0.in.applicationinsights.azure.com/")
            .withVnetConfiguration(new VnetConfiguration().withInfrastructureSubnetId(
                "/subscriptions/34adfa4f-cedf-4dc0-ba29-b6d1a69ab345/resourceGroups/RGName/providers/Microsoft.Network/virtualNetworks/VNetName/subnets/subnetName1"))
            .withAppLogsConfiguration(new AppLogsConfiguration().withLogAnalyticsConfiguration(
                new LogAnalyticsConfiguration().withCustomerId("string").withSharedKey("fakeTokenPlaceholder")))
            .withZoneRedundant(true)
            .withCustomDomainConfiguration(new CustomDomainConfiguration().withDnsSuffix("www.my-name.com")
                .withCertificateValue("Y2VydA==".getBytes())
                .withCertificatePassword("fakeTokenPlaceholder"))
            .withWorkloadProfiles(Arrays.asList(
                new WorkloadProfile().withName("My-GP-01")
                    .withWorkloadProfileType("GeneralPurpose")
                    .withMinimumCount(3)
                    .withMaximumCount(12),
                new WorkloadProfile().withName("My-MO-01")
                    .withWorkloadProfileType("MemoryOptimized")
                    .withMinimumCount(3)
                    .withMaximumCount(6),
                new WorkloadProfile().withName("My-CO-01")
                    .withWorkloadProfileType("ComputeOptimized")
                    .withMinimumCount(3)
                    .withMaximumCount(6),
                new WorkloadProfile().withName("My-consumption-01").withWorkloadProfileType("Consumption")))
            .withInfrastructureResourceGroup("myInfrastructureRgName")
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/ManagedEnvironments_CreateOrUpdate.
     * json
     */
    /**
     * Sample code: Create environments.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void createEnvironments(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.managedEnvironments()
            .define("testcontainerenv")
            .withRegion("East US")
            .withExistingResourceGroup("examplerg")
            .withDaprAIConnectionString(
                "InstrumentationKey=00000000-0000-0000-0000-000000000000;IngestionEndpoint=https://northcentralus-0.in.applicationinsights.azure.com/")
            .withVnetConfiguration(new VnetConfiguration().withInfrastructureSubnetId(
                "/subscriptions/34adfa4f-cedf-4dc0-ba29-b6d1a69ab345/resourceGroups/RGName/providers/Microsoft.Network/virtualNetworks/VNetName/subnets/subnetName1"))
            .withAppLogsConfiguration(new AppLogsConfiguration().withLogAnalyticsConfiguration(
                new LogAnalyticsConfiguration().withCustomerId("string").withSharedKey("fakeTokenPlaceholder")))
            .withZoneRedundant(true)
            .withCustomDomainConfiguration(new CustomDomainConfiguration().withDnsSuffix("www.my-name.com")
                .withCertificateValue("Y2VydA==".getBytes())
                .withCertificatePassword("fakeTokenPlaceholder"))
            .withWorkloadProfiles(Arrays.asList(
                new WorkloadProfile().withName("My-GP-01")
                    .withWorkloadProfileType("GeneralPurpose")
                    .withMinimumCount(3)
                    .withMaximumCount(12),
                new WorkloadProfile().withName("My-MO-01")
                    .withWorkloadProfileType("MemoryOptimized")
                    .withMinimumCount(3)
                    .withMaximumCount(6),
                new WorkloadProfile().withName("My-CO-01")
                    .withWorkloadProfileType("ComputeOptimized")
                    .withMinimumCount(3)
                    .withMaximumCount(6),
                new WorkloadProfile().withName("My-consumption-01").withWorkloadProfileType("Consumption")))
            .withPeerAuthentication(
                new ManagedEnvironmentPropertiesPeerAuthentication().withMtls(new Mtls().withEnabled(true)))
            .withPeerTrafficConfiguration(new ManagedEnvironmentPropertiesPeerTrafficConfiguration()
                .withEncryption(new ManagedEnvironmentPropertiesPeerTrafficConfigurationEncryption().withEnabled(true)))
            .create();
    }
}
```

### ManagedEnvironments_Delete

```java
/**
 * Samples for ManagedEnvironments Delete.
 */
public final class ManagedEnvironmentsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/ManagedEnvironments_Delete.json
     */
    /**
     * Sample code: Delete environment by name.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        deleteEnvironmentByName(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.managedEnvironments().delete("examplerg", "examplekenv", com.azure.core.util.Context.NONE);
    }
}
```

### ManagedEnvironments_GetAuthToken

```java
/**
 * Samples for ManagedEnvironments GetAuthToken.
 */
public final class ManagedEnvironmentsGetAuthTokenSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/ManagedEnvironments_GetAuthToken.json
     */
    /**
     * Sample code: Get Managed Environment Auth Token.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        getManagedEnvironmentAuthToken(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.managedEnvironments().getAuthTokenWithResponse("rg", "testenv", com.azure.core.util.Context.NONE);
    }
}
```

### ManagedEnvironments_GetByResourceGroup

```java
/**
 * Samples for ManagedEnvironments GetByResourceGroup.
 */
public final class ManagedEnvironmentsGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/ManagedEnvironments_Get.json
     */
    /**
     * Sample code: Get environments by name.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void getEnvironmentsByName(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.managedEnvironments()
            .getByResourceGroupWithResponse("examplerg", "jlaw-demo1", com.azure.core.util.Context.NONE);
    }
}
```

### ManagedEnvironments_List

```java
/**
 * Samples for ManagedEnvironments List.
 */
public final class ManagedEnvironmentsListSamples {
    /*
     * x-ms-original-file: specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/
     * ManagedEnvironments_ListBySubscription.json
     */
    /**
     * Sample code: List environments by subscription.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        listEnvironmentsBySubscription(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.managedEnvironments().list(com.azure.core.util.Context.NONE);
    }
}
```

### ManagedEnvironments_ListByResourceGroup

```java
/**
 * Samples for ManagedEnvironments ListByResourceGroup.
 */
public final class ManagedEnvironmentsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/
     * ManagedEnvironments_ListByResourceGroup.json
     */
    /**
     * Sample code: List environments by resource group.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        listEnvironmentsByResourceGroup(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.managedEnvironments().listByResourceGroup("examplerg", com.azure.core.util.Context.NONE);
    }
}
```

### ManagedEnvironments_ListWorkloadProfileStates

```java
/**
 * Samples for ManagedEnvironments ListWorkloadProfileStates.
 */
public final class ManagedEnvironmentsListWorkloadProfileStatesSamples {
    /*
     * x-ms-original-file: specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/
     * ManagedEnvironments_ListWorkloadProfileStates.json
     */
    /**
     * Sample code: List environments by subscription.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        listEnvironmentsBySubscription(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.managedEnvironments()
            .listWorkloadProfileStates("examplerg", "jlaw-demo1", com.azure.core.util.Context.NONE);
    }
}
```

### ManagedEnvironments_Update

```java
import com.azure.resourcemanager.appcontainers.models.ManagedEnvironment;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ManagedEnvironments Update.
 */
public final class ManagedEnvironmentsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/ManagedEnvironments_Patch.json
     */
    /**
     * Sample code: Patch Managed Environment.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        patchManagedEnvironment(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        ManagedEnvironment resource = manager.managedEnvironments()
            .getByResourceGroupWithResponse("examplerg", "testcontainerenv", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### ManagedEnvironmentsDiagnostics_GetRoot

```java
/**
 * Samples for ManagedEnvironmentsDiagnostics GetRoot.
 */
public final class ManagedEnvironmentsDiagnosticsGetRootSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/ManagedEnvironments_Get.json
     */
    /**
     * Sample code: Get environments by name.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void getEnvironmentsByName(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.managedEnvironmentsDiagnostics()
            .getRootWithResponse("examplerg", "jlaw-demo1", com.azure.core.util.Context.NONE);
    }
}
```

### ManagedEnvironmentsStorages_CreateOrUpdate

```java
import com.azure.resourcemanager.appcontainers.models.AccessMode;
import com.azure.resourcemanager.appcontainers.models.AzureFileProperties;
import com.azure.resourcemanager.appcontainers.models.ManagedEnvironmentStorageProperties;

/**
 * Samples for ManagedEnvironmentsStorages CreateOrUpdate.
 */
public final class ManagedEnvironmentsStoragesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/
     * ManagedEnvironmentsStorages_CreateOrUpdate.json
     */
    /**
     * Sample code: Create or update environments storage.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        createOrUpdateEnvironmentsStorage(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.managedEnvironmentsStorages()
            .define("jlaw-demo1")
            .withExistingManagedEnvironment("examplerg", "managedEnv")
            .withProperties(new ManagedEnvironmentStorageProperties()
                .withAzureFile(new AzureFileProperties().withAccountName("account1")
                    .withAccountKey("fakeTokenPlaceholder")
                    .withAccessMode(AccessMode.READ_ONLY)
                    .withShareName("share1")))
            .create();
    }
}
```

### ManagedEnvironmentsStorages_Delete

```java
/**
 * Samples for ManagedEnvironmentsStorages Delete.
 */
public final class ManagedEnvironmentsStoragesDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/ManagedEnvironmentsStorages_Delete.
     * json
     */
    /**
     * Sample code: List environments storages by subscription.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void listEnvironmentsStoragesBySubscription(
        com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.managedEnvironmentsStorages()
            .deleteWithResponse("examplerg", "managedEnv", "jlaw-demo1", com.azure.core.util.Context.NONE);
    }
}
```

### ManagedEnvironmentsStorages_Get

```java
/**
 * Samples for ManagedEnvironmentsStorages Get.
 */
public final class ManagedEnvironmentsStoragesGetSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/ManagedEnvironmentsStorages_Get.json
     */
    /**
     * Sample code: get a environments storage properties by subscription.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void getAEnvironmentsStoragePropertiesBySubscription(
        com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.managedEnvironmentsStorages()
            .getWithResponse("examplerg", "managedEnv", "jlaw-demo1", com.azure.core.util.Context.NONE);
    }
}
```

### ManagedEnvironmentsStorages_List

```java
/**
 * Samples for ManagedEnvironmentsStorages List.
 */
public final class ManagedEnvironmentsStoragesListSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/ManagedEnvironmentsStorages_List.json
     */
    /**
     * Sample code: List environments storages by subscription.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void listEnvironmentsStoragesBySubscription(
        com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.managedEnvironmentsStorages()
            .listWithResponse("examplerg", "managedEnv", com.azure.core.util.Context.NONE);
    }
}
```

### Namespaces_CheckNameAvailability

```java
import com.azure.resourcemanager.appcontainers.models.CheckNameAvailabilityRequest;

/**
 * Samples for Namespaces CheckNameAvailability.
 */
public final class NamespacesCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/Certificates_CheckNameAvailability.
     * json
     */
    /**
     * Sample code: Certificates_CheckNameAvailability.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        certificatesCheckNameAvailability(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.namespaces()
            .checkNameAvailabilityWithResponse("examplerg", "testcontainerenv",
                new CheckNameAvailabilityRequest().withName("testcertificatename")
                    .withType("Microsoft.App/managedEnvironments/certificates"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/ContainerApps_CheckNameAvailability.
     * json
     */
    /**
     * Sample code: ContainerApps_CheckNameAvailability.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        containerAppsCheckNameAvailability(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.namespaces()
            .checkNameAvailabilityWithResponse("examplerg", "testcontainerenv",
                new CheckNameAvailabilityRequest().withName("testcappname").withType("Microsoft.App/containerApps"),
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
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/Operations_List.json
     */
    /**
     * Sample code: List all operations.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void listAllOperations(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_GetCustomDomainVerificationId

```java
/**
 * Samples for ResourceProvider GetCustomDomainVerificationId.
 */
public final class ResourceProviderGetCustomDomainVerificationIdSamples {
    /*
     * x-ms-original-file: specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/
     * Subscriptions_GetCustomDomainVerificationId.json
     */
    /**
     * Sample code: List all operations.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void listAllOperations(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.resourceProviders().getCustomDomainVerificationIdWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_JobExecution

```java
/**
 * Samples for ResourceProvider JobExecution.
 */
public final class ResourceProviderJobExecutionSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/Job_Execution_Get.json
     */
    /**
     * Sample code: Get a single Job Execution.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void getASingleJobExecution(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.resourceProviders()
            .jobExecutionWithResponse("rg", "testcontainerappsjob0", "jobExecution1", com.azure.core.util.Context.NONE);
    }
}
```

### Usages_List

```java
/**
 * Samples for Usages List.
 */
public final class UsagesListSamples {
    /*
     * x-ms-original-file: specification/app/resource-manager/Microsoft.App/stable/2024-03-01/examples/Usages_List.json
     */
    /**
     * Sample code: List usages.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void listUsages(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.usages().list("westus", com.azure.core.util.Context.NONE);
    }
}
```


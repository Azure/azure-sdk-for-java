# Code snippets and samples


## Authentications

- [List](#authentications_list)

## Capabilities

- [Get](#capabilities_get)
- [List](#capabilities_list)

## ChangeDataCaptureCapabilities

- [CreateOrReplace](#changedatacapturecapabilities_createorreplace)
- [Delete](#changedatacapturecapabilities_delete)
- [Get](#changedatacapturecapabilities_get)
- [List](#changedatacapturecapabilities_list)
- [Update](#changedatacapturecapabilities_update)

## ChangeDataCaptureConfigurations

- [CreateOrReplace](#changedatacaptureconfigurations_createorreplace)
- [Delete](#changedatacaptureconfigurations_delete)
- [Get](#changedatacaptureconfigurations_get)
- [List](#changedatacaptureconfigurations_list)
- [Update](#changedatacaptureconfigurations_update)

## ChangeDataCaptureDestinations

- [ActivateIdentity](#changedatacapturedestinations_activateidentity)
- [CreateOrReplace](#changedatacapturedestinations_createorreplace)
- [Delete](#changedatacapturedestinations_delete)
- [Get](#changedatacapturedestinations_get)
- [List](#changedatacapturedestinations_list)
- [Update](#changedatacapturedestinations_update)

## Clusters

- [CreateOrReplace](#clusters_createorreplace)
- [Delete](#clusters_delete)
- [Failover](#clusters_failover)
- [GetByResourceGroup](#clusters_getbyresourcegroup)
- [List](#clusters_list)
- [ListByResourceGroup](#clusters_listbyresourcegroup)
- [Update](#clusters_update)

## DefaultParameterGroups

- [Get](#defaultparametergroups_get)
- [ListBySubscriptionAndRegion](#defaultparametergroups_listbysubscriptionandregion)

## Endpoints

- [Get](#endpoints_get)
- [List](#endpoints_list)

## FirewallRules

- [CreateOrReplace](#firewallrules_createorreplace)
- [Delete](#firewallrules_delete)
- [Get](#firewallrules_get)
- [List](#firewallrules_list)

## IdentityAssignments

- [CreateOrReplace](#identityassignments_createorreplace)
- [Delete](#identityassignments_delete)
- [Get](#identityassignments_get)
- [List](#identityassignments_list)

## LogCaptureConfigurations

- [Get](#logcaptureconfigurations_get)
- [List](#logcaptureconfigurations_list)
- [Update](#logcaptureconfigurations_update)

## LogFiles

- [Get](#logfiles_get)
- [List](#logfiles_list)
- [ListDownloadUrls](#logfiles_listdownloadurls)

## MaintenanceEvents

- [ApplyNow](#maintenanceevents_applynow)
- [Get](#maintenanceevents_get)
- [List](#maintenanceevents_list)
- [Reschedule](#maintenanceevents_reschedule)

## MicrosoftEntraAdministrators

- [CreateOrReplace](#microsoftentraadministrators_createorreplace)
- [Delete](#microsoftentraadministrators_delete)
- [Get](#microsoftentraadministrators_get)
- [List](#microsoftentraadministrators_list)

## MicrosoftEntraAuthentications

- [CreateOrReplace](#microsoftentraauthentications_createorreplace)
- [Get](#microsoftentraauthentications_get)
- [Update](#microsoftentraauthentications_update)

## Nodes

- [Get](#nodes_get)
- [List](#nodes_list)

## Operations

- [List](#operations_list)

## ParameterGroups

- [CreateOrReplace](#parametergroups_createorreplace)
- [Delete](#parametergroups_delete)
- [GetByResourceGroup](#parametergroups_getbyresourcegroup)
- [List](#parametergroups_list)
- [ListByResourceGroup](#parametergroups_listbyresourcegroup)
- [ListConnections](#parametergroups_listconnections)
- [Update](#parametergroups_update)

## PasswordAuthentications

- [CreateOrReplace](#passwordauthentications_createorreplace)
- [Get](#passwordauthentications_get)
- [Update](#passwordauthentications_update)

## Pools

- [CreateOrReplace](#pools_createorreplace)
- [Delete](#pools_delete)
- [Get](#pools_get)
- [List](#pools_list)
- [Restart](#pools_restart)
- [Start](#pools_start)
- [Stop](#pools_stop)
- [Update](#pools_update)

## PrivateEndpointConnections

- [ApproveOrReject](#privateendpointconnections_approveorreject)
- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [List](#privateendpointconnections_list)

## PrivateLinkResources

- [Get](#privatelinkresources_get)
- [List](#privatelinkresources_list)
### Authentications_List

```java
/**
 * Samples for Authentications List.
 */
public final class AuthenticationsListSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/Authentications_List.json
     */
    /**
     * Sample code: List the service-created authentication methods.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        listTheServiceCreatedAuthenticationMethods(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.authentications().list("example-resource-group", "example-cluster", com.azure.core.util.Context.NONE);
    }
}
```

### Capabilities_Get

```java
/**
 * Samples for Capabilities Get.
 */
public final class CapabilitiesGetSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/Capabilities_Get.json
     */
    /**
     * Sample code: Get HorizonDB capabilities by subscription and region.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        getHorizonDBCapabilitiesBySubscriptionAndRegion(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.capabilities().getWithResponse("eastus2", com.azure.core.util.Context.NONE);
    }
}
```

### Capabilities_List

```java
/**
 * Samples for Capabilities List.
 */
public final class CapabilitiesListSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/Capabilities_List.json
     */
    /**
     * Sample code: List HorizonDB capabilities by subscription and region.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        listHorizonDBCapabilitiesBySubscriptionAndRegion(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.capabilities().list("eastus2", com.azure.core.util.Context.NONE);
    }
}
```

### ChangeDataCaptureCapabilities_CreateOrReplace

```java
import com.azure.resourcemanager.horizondb.fluent.models.ChangeDataCaptureCapabilityInner;
import com.azure.resourcemanager.horizondb.models.ChangeDataCaptureCapabilityProperties;
import com.azure.resourcemanager.horizondb.models.ChangeDataCaptureDesiredState;

/**
 * Samples for ChangeDataCaptureCapabilities CreateOrReplace.
 */
public final class ChangeDataCaptureCapabilitiesCreateOrReplaceSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/ChangeDataCaptureCapabilities_CreateOrReplace.json
     */
    /**
     * Sample code: Enable the cluster change data capture capability.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        enableTheClusterChangeDataCaptureCapability(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.changeDataCaptureCapabilities()
            .createOrReplace("example-resource-group", "example-cluster",
                new ChangeDataCaptureCapabilityInner().withProperties(
                    new ChangeDataCaptureCapabilityProperties().withDesiredState(ChangeDataCaptureDesiredState.ENABLED)
                        .withAutoUpgradeExtension(true)),
                com.azure.core.util.Context.NONE);
    }
}
```

### ChangeDataCaptureCapabilities_Delete

```java
/**
 * Samples for ChangeDataCaptureCapabilities Delete.
 */
public final class ChangeDataCaptureCapabilitiesDeleteSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/ChangeDataCaptureCapabilities_Delete.json
     */
    /**
     * Sample code: Delete the cluster change data capture capability.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        deleteTheClusterChangeDataCaptureCapability(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.changeDataCaptureCapabilities()
            .delete("example-resource-group", "example-cluster", com.azure.core.util.Context.NONE);
    }
}
```

### ChangeDataCaptureCapabilities_Get

```java
/**
 * Samples for ChangeDataCaptureCapabilities Get.
 */
public final class ChangeDataCaptureCapabilitiesGetSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/ChangeDataCaptureCapabilities_Get.json
     */
    /**
     * Sample code: Get the cluster change data capture capability.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        getTheClusterChangeDataCaptureCapability(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.changeDataCaptureCapabilities()
            .getWithResponse("example-resource-group", "example-cluster", com.azure.core.util.Context.NONE);
    }
}
```

### ChangeDataCaptureCapabilities_List

```java
/**
 * Samples for ChangeDataCaptureCapabilities List.
 */
public final class ChangeDataCaptureCapabilitiesListSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/ChangeDataCaptureCapabilities_List.json
     */
    /**
     * Sample code: List the optional cluster change data capture capability.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void listTheOptionalClusterChangeDataCaptureCapability(
        com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.changeDataCaptureCapabilities()
            .list("example-resource-group", "example-cluster", com.azure.core.util.Context.NONE);
    }
}
```

### ChangeDataCaptureCapabilities_Update

```java
import com.azure.resourcemanager.horizondb.models.ChangeDataCaptureCapabilityForPatchUpdate;
import com.azure.resourcemanager.horizondb.models.ChangeDataCaptureCapabilityPropertiesForPatchUpdate;

/**
 * Samples for ChangeDataCaptureCapabilities Update.
 */
public final class ChangeDataCaptureCapabilitiesUpdateSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/ChangeDataCaptureCapabilities_Update.json
     */
    /**
     * Sample code: Update the cluster change data capture capability.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        updateTheClusterChangeDataCaptureCapability(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.changeDataCaptureCapabilities()
            .update("example-resource-group", "example-cluster",
                new ChangeDataCaptureCapabilityForPatchUpdate().withProperties(
                    new ChangeDataCaptureCapabilityPropertiesForPatchUpdate().withAutoUpgradeExtension(false)),
                com.azure.core.util.Context.NONE);
    }
}
```

### ChangeDataCaptureConfigurations_CreateOrReplace

```java
import com.azure.resourcemanager.horizondb.models.ChangeDataCaptureConfigurationProperties;
import com.azure.resourcemanager.horizondb.models.ChangeDataCaptureDesiredState;
import com.azure.resourcemanager.horizondb.models.ChangeDataCaptureUsageProfile;

/**
 * Samples for ChangeDataCaptureConfigurations CreateOrReplace.
 */
public final class ChangeDataCaptureConfigurationsCreateOrReplaceSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/ChangeDataCaptureConfigurations_CreateOrReplace.json
     */
    /**
     * Sample code: Enroll a database for Microsoft Fabric Mirroring.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        enrollADatabaseForMicrosoftFabricMirroring(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.changeDataCaptureConfigurations()
            .define("sales-database")
            .withExistingCluster("example-resource-group", "example-cluster")
            .withProperties(new ChangeDataCaptureConfigurationProperties().withDatabaseName("sales")
                .withUsageProfile(ChangeDataCaptureUsageProfile.MIRRORING)
                .withDestinationResourceId(
                    "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/example-resource-group/providers/Microsoft.HorizonDb/clusters/example-cluster/changeDataCaptureDestinations/fabric-mirroring")
                .withDesiredState(ChangeDataCaptureDesiredState.ENABLED))
            .create();
    }
}
```

### ChangeDataCaptureConfigurations_Delete

```java
/**
 * Samples for ChangeDataCaptureConfigurations Delete.
 */
public final class ChangeDataCaptureConfigurationsDeleteSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/ChangeDataCaptureConfigurations_Delete.json
     */
    /**
     * Sample code: Delete a Microsoft Fabric Mirroring configuration.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        deleteAMicrosoftFabricMirroringConfiguration(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.changeDataCaptureConfigurations()
            .delete("example-resource-group", "example-cluster", "sales-database", com.azure.core.util.Context.NONE);
    }
}
```

### ChangeDataCaptureConfigurations_Get

```java
/**
 * Samples for ChangeDataCaptureConfigurations Get.
 */
public final class ChangeDataCaptureConfigurationsGetSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/ChangeDataCaptureConfigurations_Get.json
     */
    /**
     * Sample code: Get a Microsoft Fabric Mirroring configuration.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        getAMicrosoftFabricMirroringConfiguration(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.changeDataCaptureConfigurations()
            .getWithResponse("example-resource-group", "example-cluster", "sales-database",
                com.azure.core.util.Context.NONE);
    }
}
```

### ChangeDataCaptureConfigurations_List

```java
/**
 * Samples for ChangeDataCaptureConfigurations List.
 */
public final class ChangeDataCaptureConfigurationsListSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/ChangeDataCaptureConfigurations_List.json
     */
    /**
     * Sample code: List Microsoft Fabric Mirroring configurations.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        listMicrosoftFabricMirroringConfigurations(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.changeDataCaptureConfigurations()
            .list("example-resource-group", "example-cluster", com.azure.core.util.Context.NONE);
    }
}
```

### ChangeDataCaptureConfigurations_Update

```java
import com.azure.resourcemanager.horizondb.models.ChangeDataCaptureConfiguration;
import com.azure.resourcemanager.horizondb.models.ChangeDataCaptureConfigurationPropertiesForPatchUpdate;
import com.azure.resourcemanager.horizondb.models.ChangeDataCaptureDesiredState;

/**
 * Samples for ChangeDataCaptureConfigurations Update.
 */
public final class ChangeDataCaptureConfigurationsUpdateSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/ChangeDataCaptureConfigurations_Update.json
     */
    /**
     * Sample code: Disable a Microsoft Fabric Mirroring configuration.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        disableAMicrosoftFabricMirroringConfiguration(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        ChangeDataCaptureConfiguration resource = manager.changeDataCaptureConfigurations()
            .getWithResponse("example-resource-group", "example-cluster", "sales-database",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new ChangeDataCaptureConfigurationPropertiesForPatchUpdate()
                .withDesiredState(ChangeDataCaptureDesiredState.DISABLED))
            .apply();
    }
}
```

### ChangeDataCaptureDestinations_ActivateIdentity

```java
import com.azure.resourcemanager.horizondb.models.ActivateChangeDataCaptureIdentityRequest;

/**
 * Samples for ChangeDataCaptureDestinations ActivateIdentity.
 */
public final class ChangeDataCaptureDestinationsActivateIdentitySamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/ChangeDataCaptureDestinations_ActivateIdentity.json
     */
    /**
     * Sample code: Activate a validated identity assignment for a change data capture destination.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void activateAValidatedIdentityAssignmentForAChangeDataCaptureDestination(
        com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.changeDataCaptureDestinations()
            .activateIdentity("example-resource-group", "*", "example-cluster", "fabric-mirroring",
                new ActivateChangeDataCaptureIdentityRequest().withIdentityAssignmentResourceId(
                    "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/example-resource-group/providers/Microsoft.HorizonDb/clusters/example-cluster/changeDataCaptureDestinations/fabric-mirroring/identityAssignments/fabric-writer")
                    .withDrainExistingSessions(true),
                com.azure.core.util.Context.NONE);
    }
}
```

### ChangeDataCaptureDestinations_CreateOrReplace

```java
import com.azure.resourcemanager.horizondb.models.ChangeDataCaptureDesiredState;
import com.azure.resourcemanager.horizondb.models.ChangeDataCaptureDestinationProperties;
import com.azure.resourcemanager.horizondb.models.FabricDestinationDetails;

/**
 * Samples for ChangeDataCaptureDestinations CreateOrReplace.
 */
public final class ChangeDataCaptureDestinationsCreateOrReplaceSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/ChangeDataCaptureDestinations_CreateOrReplace.json
     */
    /**
     * Sample code: Create a Microsoft Fabric change data capture destination.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void createAMicrosoftFabricChangeDataCaptureDestination(
        com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.changeDataCaptureDestinations()
            .define("fabric-mirroring")
            .withExistingCluster("example-resource-group", "example-cluster")
            .withProperties(new ChangeDataCaptureDestinationProperties()
                .withDestination(new FabricDestinationDetails().withWorkspaceId("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"))
                .withDesiredState(ChangeDataCaptureDesiredState.ENABLED))
            .create();
    }
}
```

### ChangeDataCaptureDestinations_Delete

```java
/**
 * Samples for ChangeDataCaptureDestinations Delete.
 */
public final class ChangeDataCaptureDestinationsDeleteSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/ChangeDataCaptureDestinations_Delete.json
     */
    /**
     * Sample code: Delete a Microsoft Fabric change data capture destination.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void deleteAMicrosoftFabricChangeDataCaptureDestination(
        com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.changeDataCaptureDestinations()
            .delete("example-resource-group", "example-cluster", "fabric-mirroring", com.azure.core.util.Context.NONE);
    }
}
```

### ChangeDataCaptureDestinations_Get

```java
/**
 * Samples for ChangeDataCaptureDestinations Get.
 */
public final class ChangeDataCaptureDestinationsGetSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/ChangeDataCaptureDestinations_Get.json
     */
    /**
     * Sample code: Get a Microsoft Fabric change data capture destination.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        getAMicrosoftFabricChangeDataCaptureDestination(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.changeDataCaptureDestinations()
            .getWithResponse("example-resource-group", "example-cluster", "fabric-mirroring",
                com.azure.core.util.Context.NONE);
    }
}
```

### ChangeDataCaptureDestinations_List

```java
/**
 * Samples for ChangeDataCaptureDestinations List.
 */
public final class ChangeDataCaptureDestinationsListSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/ChangeDataCaptureDestinations_List.json
     */
    /**
     * Sample code: List Microsoft Fabric change data capture destinations.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        listMicrosoftFabricChangeDataCaptureDestinations(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.changeDataCaptureDestinations()
            .list("example-resource-group", "example-cluster", com.azure.core.util.Context.NONE);
    }
}
```

### ChangeDataCaptureDestinations_Update

```java
import com.azure.resourcemanager.horizondb.models.ChangeDataCaptureDesiredState;
import com.azure.resourcemanager.horizondb.models.ChangeDataCaptureDestination;
import com.azure.resourcemanager.horizondb.models.ChangeDataCaptureDestinationPropertiesForPatchUpdate;

/**
 * Samples for ChangeDataCaptureDestinations Update.
 */
public final class ChangeDataCaptureDestinationsUpdateSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/ChangeDataCaptureDestinations_Update.json
     */
    /**
     * Sample code: Disable a Microsoft Fabric change data capture destination.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void disableAMicrosoftFabricChangeDataCaptureDestination(
        com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        ChangeDataCaptureDestination resource = manager.changeDataCaptureDestinations()
            .getWithResponse("example-resource-group", "example-cluster", "fabric-mirroring",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new ChangeDataCaptureDestinationPropertiesForPatchUpdate()
                .withDesiredState(ChangeDataCaptureDesiredState.DISABLED))
            .apply();
    }
}
```

### Clusters_CreateOrReplace

```java
import com.azure.resourcemanager.horizondb.models.BackupProperties;
import com.azure.resourcemanager.horizondb.models.ClusterCreateMode;
import com.azure.resourcemanager.horizondb.models.ClusterProperties;
import com.azure.resourcemanager.horizondb.models.GeoRedundancyState;
import com.azure.resourcemanager.horizondb.models.HorizonDbClusterDataEncryption;
import com.azure.resourcemanager.horizondb.models.HorizonDbClusterDataEncryptionType;
import com.azure.resourcemanager.horizondb.models.HorizonDbClusterIdentityType;
import com.azure.resourcemanager.horizondb.models.HorizonDbClusterManagedServiceIdentity;
import com.azure.resourcemanager.horizondb.models.Network;
import com.azure.resourcemanager.horizondb.models.PostgreSqlVersion;
import com.azure.resourcemanager.horizondb.models.PublicNetworkAccessState;
import com.azure.resourcemanager.horizondb.models.UserAssignedIdentity;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Clusters CreateOrReplace.
 */
public final class ClustersCreateOrReplaceSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/Clusters_CreateOrReplace.json
     */
    /**
     * Sample code: Create or replace a HorizonDB cluster.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void createOrReplaceAHorizonDBCluster(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.clusters()
            .define("example-cluster")
            .withRegion("eastus2")
            .withExistingResourceGroup("example-resource-group")
            .withTags(mapOf("environment", "production"))
            .withProperties(new ClusterProperties().withPostgreSqlVersion(PostgreSqlVersion.V17)
                .withNetwork(new Network().withPublicAccess(PublicNetworkAccessState.ENABLED))
                .withBackup(new BackupProperties().withGeoRedundancy(GeoRedundancyState.ENABLED).withRetentionDays(14)))
            .withIdentity(new HorizonDbClusterManagedServiceIdentity()
                .withType(HorizonDbClusterIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/identity-resource-group/providers/Microsoft.ManagedIdentity/userAssignedIdentities/fabric-writer",
                    new UserAssignedIdentity())))
            .create();
    }

    /*
     * x-ms-original-file: 2026-10-01-preview/Clusters_CreateOrReplaceMinimumDefaults.json
     */
    /**
     * Sample code: Create a PostgreSQL 18 HorizonDB cluster with minimum configuration and service defaults.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void createAPostgreSQL18HorizonDBClusterWithMinimumConfigurationAndServiceDefaults(
        com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.clusters()
            .define("minimum-defaults")
            .withRegion("eastus2")
            .withExistingResourceGroup("example-resource-group")
            .withProperties(new ClusterProperties().withPostgreSqlVersion(PostgreSqlVersion.V18)
                .withBackup(new BackupProperties().withGeoRedundancy(GeoRedundancyState.DISABLED)))
            .create();
    }

    /*
     * x-ms-original-file: 2026-10-01-preview/Clusters_CreateOrReplaceWithDataEncryption.json
     */
    /**
     * Sample code: Create a HorizonDB cluster with customer-managed key data encryption.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void createAHorizonDBClusterWithCustomerManagedKeyDataEncryption(
        com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.clusters()
            .define("example-cluster")
            .withRegion("eastus2")
            .withExistingResourceGroup("example-resource-group")
            .withProperties(new ClusterProperties().withPostgreSqlVersion(PostgreSqlVersion.V17)
                .withBackup(new BackupProperties().withGeoRedundancy(GeoRedundancyState.DISABLED).withRetentionDays(14))
                .withDataEncryption(new HorizonDbClusterDataEncryption()
                    .withType(HorizonDbClusterDataEncryptionType.AZURE_KEY_VAULT)
                    .withPrimaryKeyUri("fakeTokenPlaceholder")
                    .withPrimaryUserAssignedIdentityId(
                        "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/identity-resource-group/providers/Microsoft.ManagedIdentity/userAssignedIdentities/primary-encryption-identity")))
            .withIdentity(new HorizonDbClusterManagedServiceIdentity()
                .withType(HorizonDbClusterIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/identity-resource-group/providers/Microsoft.ManagedIdentity/userAssignedIdentities/primary-encryption-identity",
                    new UserAssignedIdentity())))
            .create();
    }

    /*
     * x-ms-original-file: 2026-10-01-preview/Clusters_CreateOrReplacePointInTimeRestore.json
     */
    /**
     * Sample code: Restore a HorizonDB cluster to a point in time.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        restoreAHorizonDBClusterToAPointInTime(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.clusters()
            .define("restored-cluster")
            .withRegion("eastus2")
            .withExistingResourceGroup("example-resource-group")
            .withProperties(new ClusterProperties().withCreationMode(ClusterCreateMode.POINT_IN_TIME_RESTORE)
                .withPointInTimeUtc(OffsetDateTime.parse("2026-07-27T12:00:00Z"))
                .withCreationSourceClusterResourceId(
                    "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/example-resource-group/providers/Microsoft.HorizonDb/clusters/source-cluster")
                .withNetwork(new Network().withPublicAccess(PublicNetworkAccessState.DISABLED))
                .withBackup(new BackupProperties().withGeoRedundancy(GeoRedundancyState.ENABLED).withRetentionDays(14)))
            .create();
    }

    /*
     * x-ms-original-file: 2026-10-01-preview/Clusters_CreateOrReplaceCrossRegionPointInTimeRestore.json
     */
    /**
     * Sample code: Restore a HorizonDB cluster across Azure regions from the latest available backup.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void restoreAHorizonDBClusterAcrossAzureRegionsFromTheLatestAvailableBackup(
        com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.clusters()
            .define("cross-region-restored-cluster")
            .withRegion("westus2")
            .withExistingResourceGroup("example-resource-group")
            .withProperties(new ClusterProperties().withCreationMode(ClusterCreateMode.POINT_IN_TIME_RESTORE)
                .withPointInTimeUtc(OffsetDateTime.parse("2026-07-27T12:00:00Z"))
                .withCreationSourceClusterResourceId(
                    "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/example-resource-group/providers/Microsoft.HorizonDb/clusters/source-cluster")
                .withCreationSourceClusterLocation("eastus2")
                .withNetwork(new Network().withPublicAccess(PublicNetworkAccessState.DISABLED))
                .withBackup(new BackupProperties().withGeoRedundancy(GeoRedundancyState.ENABLED).withRetentionDays(14)))
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

### Clusters_Delete

```java
/**
 * Samples for Clusters Delete.
 */
public final class ClustersDeleteSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/Clusters_Delete.json
     */
    /**
     * Sample code: Delete a HorizonDB cluster.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void deleteAHorizonDBCluster(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.clusters().delete("example-resource-group", "example-cluster", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_Failover

```java
/**
 * Samples for Clusters Failover.
 */
public final class ClustersFailoverSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/Clusters_Failover.json
     */
    /**
     * Sample code: Trigger failover within a HorizonDB cluster.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        triggerFailoverWithinAHorizonDBCluster(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.clusters().failover("example-resource-group", "example-cluster", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_GetByResourceGroup

```java
/**
 * Samples for Clusters GetByResourceGroup.
 */
public final class ClustersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/Clusters_GetMinimumDefaults.json
     */
    /**
     * Sample code: Get a minimum HorizonDB cluster with materialized service defaults.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void getAMinimumHorizonDBClusterWithMaterializedServiceDefaults(
        com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.clusters()
            .getByResourceGroupWithResponse("example-resource-group", "minimum-defaults",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-01-preview/Clusters_Get.json
     */
    /**
     * Sample code: Get a HorizonDB cluster.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void getAHorizonDBCluster(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.clusters()
            .getByResourceGroupWithResponse("example-resource-group", "example-cluster",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-01-preview/Clusters_GetAfterPoolCreationOnEmptyCluster.json
     */
    /**
     * Sample code: Get a cluster after pool creation while it had no existing pools.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void getAClusterAfterPoolCreationWhileItHadNoExistingPools(
        com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.clusters()
            .getByResourceGroupWithResponse("example-resource-group", "example-cluster",
                com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_List

```java
/**
 * Samples for Clusters List.
 */
public final class ClustersListSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/Clusters_ListBySubscription.json
     */
    /**
     * Sample code: List HorizonDB clusters in a subscription.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        listHorizonDBClustersInASubscription(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.clusters().list(com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_ListByResourceGroup

```java
/**
 * Samples for Clusters ListByResourceGroup.
 */
public final class ClustersListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/Clusters_ListByResourceGroup.json
     */
    /**
     * Sample code: List HorizonDB clusters in a resource group.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        listHorizonDBClustersInAResourceGroup(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.clusters().listByResourceGroup("example-resource-group", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_Update

```java
import com.azure.resourcemanager.horizondb.models.BackupPropertiesForPatchUpdate;
import com.azure.resourcemanager.horizondb.models.Cluster;
import com.azure.resourcemanager.horizondb.models.ClusterPropertiesForPatchUpdate;
import com.azure.resourcemanager.horizondb.models.HorizonDbClusterDataEncryptionForPatchUpdate;
import com.azure.resourcemanager.horizondb.models.NetworkForPatchUpdate;
import com.azure.resourcemanager.horizondb.models.PublicNetworkAccessState;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Clusters Update.
 */
public final class ClustersUpdateSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/Clusters_Update.json
     */
    /**
     * Sample code: Update a HorizonDB cluster.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void updateAHorizonDBCluster(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        Cluster resource = manager.clusters()
            .getByResourceGroupWithResponse("example-resource-group", "example-cluster",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("environment", "production", "team", "data-platform"))
            .withProperties(new ClusterPropertiesForPatchUpdate()
                .withNetwork(new NetworkForPatchUpdate().withPublicAccess(PublicNetworkAccessState.DISABLED))
                .withBackup(new BackupPropertiesForPatchUpdate().withRetentionDays(21)))
            .apply();
    }

    /*
     * x-ms-original-file: 2026-10-01-preview/Clusters_UpdateDataEncryption.json
     */
    /**
     * Sample code: Rotate the customer-managed key used by a HorizonDB cluster.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void rotateTheCustomerManagedKeyUsedByAHorizonDBCluster(
        com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        Cluster resource = manager.clusters()
            .getByResourceGroupWithResponse("example-resource-group", "example-cluster",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new ClusterPropertiesForPatchUpdate().withDataEncryption(
                new HorizonDbClusterDataEncryptionForPatchUpdate().withPrimaryKeyUri("fakeTokenPlaceholder")))
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

### DefaultParameterGroups_Get

```java
/**
 * Samples for DefaultParameterGroups Get.
 */
public final class DefaultParameterGroupsGetSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/DefaultParameterGroups_Get.json
     */
    /**
     * Sample code: Get a default HorizonDB parameter group by subscription, location, and name.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void getADefaultHorizonDBParameterGroupBySubscriptionLocationAndName(
        com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.defaultParameterGroups()
            .getWithResponse("eastus2", "default-postgresql-17-provisioned", com.azure.core.util.Context.NONE);
    }
}
```

### DefaultParameterGroups_ListBySubscriptionAndRegion

```java
/**
 * Samples for DefaultParameterGroups ListBySubscriptionAndRegion.
 */
public final class DefaultParameterGroupsListBySubscriptionAndRegionSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/DefaultParameterGroups_List.json
     */
    /**
     * Sample code: List default HorizonDB parameter groups by subscription and region.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void listDefaultHorizonDBParameterGroupsBySubscriptionAndRegion(
        com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.defaultParameterGroups().listBySubscriptionAndRegion("eastus2", com.azure.core.util.Context.NONE);
    }
}
```

### Endpoints_Get

```java
/**
 * Samples for Endpoints Get.
 */
public final class EndpointsGetSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/Endpoints_GetReadOnly.json
     */
    /**
     * Sample code: Get a read-only HorizonDB pool endpoint.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void getAReadOnlyHorizonDBPoolEndpoint(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.endpoints()
            .getWithResponse("example-resource-group", "example-cluster", "example-pool-01",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-01-preview/Endpoints_Get.json
     */
    /**
     * Sample code: Get a HorizonDB endpoint.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void getAHorizonDBEndpoint(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.endpoints()
            .getWithResponse("example-resource-group", "example-cluster", "example-cluster",
                com.azure.core.util.Context.NONE);
    }
}
```

### Endpoints_List

```java
/**
 * Samples for Endpoints List.
 */
public final class EndpointsListSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/Endpoints_List.json
     */
    /**
     * Sample code: List HorizonDB endpoints in a cluster.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void listHorizonDBEndpointsInACluster(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.endpoints().list("example-resource-group", "example-cluster", com.azure.core.util.Context.NONE);
    }
}
```

### FirewallRules_CreateOrReplace

```java
import com.azure.resourcemanager.horizondb.models.FirewallRuleProperties;

/**
 * Samples for FirewallRules CreateOrReplace.
 */
public final class FirewallRulesCreateOrReplaceSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/FirewallRules_CreateOrReplace.json
     */
    /**
     * Sample code: Create or replace a firewall rule on a HorizonDB pool.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        createOrReplaceAFirewallRuleOnAHorizonDBPool(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.firewallRules()
            .define("example-firewall-rule-01")
            .withExistingPool("example-resource-group", "example-cluster", "example-pool-01")
            .withProperties(new FirewallRuleProperties().withStartIpAddress("10.0.0.1")
                .withEndIpAddress("10.0.0.254")
                .withDescription("Allow access from the corporate network"))
            .create();
    }
}
```

### FirewallRules_Delete

```java
/**
 * Samples for FirewallRules Delete.
 */
public final class FirewallRulesDeleteSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/FirewallRules_Delete.json
     */
    /**
     * Sample code: Delete a firewall rule from a HorizonDB pool.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        deleteAFirewallRuleFromAHorizonDBPool(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.firewallRules()
            .delete("example-resource-group", "example-cluster", "example-pool-01", "example-firewall-rule-01",
                com.azure.core.util.Context.NONE);
    }
}
```

### FirewallRules_Get

```java
/**
 * Samples for FirewallRules Get.
 */
public final class FirewallRulesGetSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/FirewallRules_Get.json
     */
    /**
     * Sample code: Get a firewall rule on a HorizonDB pool.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void getAFirewallRuleOnAHorizonDBPool(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.firewallRules()
            .getWithResponse("example-resource-group", "example-cluster", "example-pool-01", "example-firewall-rule-01",
                com.azure.core.util.Context.NONE);
    }
}
```

### FirewallRules_List

```java
/**
 * Samples for FirewallRules List.
 */
public final class FirewallRulesListSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/FirewallRules_List.json
     */
    /**
     * Sample code: List firewall rules in a HorizonDB pool.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void listFirewallRulesInAHorizonDBPool(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.firewallRules()
            .list("example-resource-group", "example-cluster", "example-pool-01", com.azure.core.util.Context.NONE);
    }
}
```

### IdentityAssignments_CreateOrReplace

```java
import com.azure.resourcemanager.horizondb.models.ChangeDataCaptureIdentityAssignmentProperties;

/**
 * Samples for IdentityAssignments CreateOrReplace.
 */
public final class IdentityAssignmentsCreateOrReplaceSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/IdentityAssignments_CreateOrReplace.json
     */
    /**
     * Sample code: Create a change data capture identity assignment for a UAMI attached to the parent cluster.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void createAChangeDataCaptureIdentityAssignmentForAUAMIAttachedToTheParentCluster(
        com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.identityAssignments()
            .define("fabric-writer")
            .withExistingChangeDataCaptureDestination("example-resource-group", "example-cluster", "fabric-mirroring")
            .withProperties(new ChangeDataCaptureIdentityAssignmentProperties().withIdentityResourceId(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/identity-resource-group/providers/Microsoft.ManagedIdentity/userAssignedIdentities/fabric-writer"))
            .create();
    }
}
```

### IdentityAssignments_Delete

```java
/**
 * Samples for IdentityAssignments Delete.
 */
public final class IdentityAssignmentsDeleteSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/IdentityAssignments_Delete.json
     */
    /**
     * Sample code: Delete an inactive change data capture identity assignment.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void deleteAnInactiveChangeDataCaptureIdentityAssignment(
        com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.identityAssignments()
            .delete("example-resource-group", "example-cluster", "fabric-mirroring", "fabric-writer",
                com.azure.core.util.Context.NONE);
    }
}
```

### IdentityAssignments_Get

```java
/**
 * Samples for IdentityAssignments Get.
 */
public final class IdentityAssignmentsGetSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/IdentityAssignments_Get.json
     */
    /**
     * Sample code: Get a change data capture identity assignment.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        getAChangeDataCaptureIdentityAssignment(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.identityAssignments()
            .getWithResponse("example-resource-group", "example-cluster", "fabric-mirroring", "fabric-writer",
                com.azure.core.util.Context.NONE);
    }
}
```

### IdentityAssignments_List

```java
/**
 * Samples for IdentityAssignments List.
 */
public final class IdentityAssignmentsListSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/IdentityAssignments_List.json
     */
    /**
     * Sample code: List change data capture identity assignments.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        listChangeDataCaptureIdentityAssignments(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.identityAssignments()
            .list("example-resource-group", "example-cluster", "fabric-mirroring", com.azure.core.util.Context.NONE);
    }
}
```

### LogCaptureConfigurations_Get

```java
/**
 * Samples for LogCaptureConfigurations Get.
 */
public final class LogCaptureConfigurationsGetSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/LogCaptureConfigurations_Get.json
     */
    /**
     * Sample code: Get the PostgreSQL server log capture configuration.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        getThePostgreSQLServerLogCaptureConfiguration(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.logCaptureConfigurations()
            .getWithResponse("example-resource-group", "example-cluster", "PostgreSqlServer",
                com.azure.core.util.Context.NONE);
    }
}
```

### LogCaptureConfigurations_List

```java
/**
 * Samples for LogCaptureConfigurations List.
 */
public final class LogCaptureConfigurationsListSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/LogCaptureConfigurations_List.json
     */
    /**
     * Sample code: List log capture configurations.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void listLogCaptureConfigurations(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.logCaptureConfigurations()
            .list("example-resource-group", "example-cluster", com.azure.core.util.Context.NONE);
    }
}
```

### LogCaptureConfigurations_Update

```java
import com.azure.resourcemanager.horizondb.models.LogCaptureConfigurationForPatchUpdate;
import com.azure.resourcemanager.horizondb.models.LogCaptureConfigurationPropertiesForPatchUpdate;
import com.azure.resourcemanager.horizondb.models.LogCaptureState;

/**
 * Samples for LogCaptureConfigurations Update.
 */
public final class LogCaptureConfigurationsUpdateSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/LogCaptureConfigurations_Update.json
     */
    /**
     * Sample code: Disable PostgreSQL server log capture.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void disablePostgreSQLServerLogCapture(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.logCaptureConfigurations()
            .update("example-resource-group", "example-cluster", "PostgreSqlServer",
                new LogCaptureConfigurationForPatchUpdate().withProperties(
                    new LogCaptureConfigurationPropertiesForPatchUpdate().withState(LogCaptureState.DISABLED)),
                com.azure.core.util.Context.NONE);
    }
}
```

### LogFiles_Get

```java
/**
 * Samples for LogFiles Get.
 */
public final class LogFilesGetSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/LogFiles_Get.json
     */
    /**
     * Sample code: Get captured log file metadata.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void getCapturedLogFileMetadata(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.logFiles()
            .getWithResponse("example-resource-group", "example-cluster", "11111111-1111-1111-1111-111111111111",
                com.azure.core.util.Context.NONE);
    }
}
```

### LogFiles_List

```java
/**
 * Samples for LogFiles List.
 */
public final class LogFilesListSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/LogFiles_List.json
     */
    /**
     * Sample code: List captured log files.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void listCapturedLogFiles(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.logFiles().list("example-resource-group", "example-cluster", com.azure.core.util.Context.NONE);
    }
}
```

### LogFiles_ListDownloadUrls

```java
/**
 * Samples for LogFiles ListDownloadUrls.
 */
public final class LogFilesListDownloadUrlsSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/LogFiles_ListDownloadUrls.json
     */
    /**
     * Sample code: List a signed URL for downloading a captured log file.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        listASignedURLForDownloadingACapturedLogFile(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.logFiles()
            .listDownloadUrlsWithResponse("example-resource-group", "example-cluster",
                "22222222-2222-2222-2222-222222222222", com.azure.core.util.Context.NONE);
    }
}
```

### MaintenanceEvents_ApplyNow

```java
/**
 * Samples for MaintenanceEvents ApplyNow.
 */
public final class MaintenanceEventsApplyNowSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/MaintenanceEvents_ApplyNow.json
     */
    /**
     * Sample code: Apply a maintenance event immediately for a HorizonDB pool.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void applyAMaintenanceEventImmediatelyForAHorizonDBPool(
        com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.maintenanceEvents()
            .applyNow("example-resource-group", "example-cluster", "example-pool-01", "ABCD-123",
                com.azure.core.util.Context.NONE);
    }
}
```

### MaintenanceEvents_Get

```java
/**
 * Samples for MaintenanceEvents Get.
 */
public final class MaintenanceEventsGetSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/MaintenanceEvents_Get.json
     */
    /**
     * Sample code: Get information about a maintenance event for a HorizonDB pool.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void getInformationAboutAMaintenanceEventForAHorizonDBPool(
        com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.maintenanceEvents()
            .getWithResponse("example-resource-group", "example-cluster", "example-pool-01", "ABCD-123",
                com.azure.core.util.Context.NONE);
    }
}
```

### MaintenanceEvents_List

```java
/**
 * Samples for MaintenanceEvents List.
 */
public final class MaintenanceEventsListSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/MaintenanceEvents_ListWithFilter.json
     */
    /**
     * Sample code: List upcoming maintenance events for a HorizonDB pool.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        listUpcomingMaintenanceEventsForAHorizonDBPool(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.maintenanceEvents()
            .list("example-resource-group", "example-cluster", "example-pool-01",
                "properties/status eq 'Planned' or properties/status eq 'InProgress'",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-01-preview/MaintenanceEvents_List.json
     */
    /**
     * Sample code: List maintenance events for a HorizonDB pool.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        listMaintenanceEventsForAHorizonDBPool(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.maintenanceEvents()
            .list("example-resource-group", "example-cluster", "example-pool-01", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### MaintenanceEvents_Reschedule

```java
import com.azure.resourcemanager.horizondb.models.MaintenanceEventRescheduleRequest;
import java.time.OffsetDateTime;

/**
 * Samples for MaintenanceEvents Reschedule.
 */
public final class MaintenanceEventsRescheduleSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/MaintenanceEvents_Reschedule.json
     */
    /**
     * Sample code: Reschedule a maintenance event for a HorizonDB pool.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        rescheduleAMaintenanceEventForAHorizonDBPool(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.maintenanceEvents()
            .reschedule("example-resource-group", "example-cluster", "example-pool-01", "ABCD-123",
                new MaintenanceEventRescheduleRequest().withPostponeToDateTimeUtc(
                    OffsetDateTime.parse("2026-08-20T10:00:00Z")),
                com.azure.core.util.Context.NONE);
    }
}
```

### MicrosoftEntraAdministrators_CreateOrReplace

```java
import com.azure.resourcemanager.horizondb.models.MicrosoftEntraAdministratorPropertiesForAdd;
import com.azure.resourcemanager.horizondb.models.PrincipalTypes;

/**
 * Samples for MicrosoftEntraAdministrators CreateOrReplace.
 */
public final class MicrosoftEntraAdministratorsCreateOrReplaceSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/MicrosoftEntraAdministrators_CreateOrReplace.json
     */
    /**
     * Sample code: Create or replace a Microsoft Entra administrator for a HorizonDB cluster.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void createOrReplaceAMicrosoftEntraAdministratorForAHorizonDBCluster(
        com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.microsoftEntraAdministrators()
            .define("11111111-1111-1111-1111-111111111111")
            .withExistingCluster("example-resource-group", "example-cluster")
            .withProperties(new MicrosoftEntraAdministratorPropertiesForAdd()
                .withPrincipalName("database.administrators@contoso.com")
                .withPrincipalType(PrincipalTypes.GROUP)
                .withTenantId("22222222-2222-2222-2222-222222222222"))
            .create();
    }
}
```

### MicrosoftEntraAdministrators_Delete

```java
/**
 * Samples for MicrosoftEntraAdministrators Delete.
 */
public final class MicrosoftEntraAdministratorsDeleteSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/MicrosoftEntraAdministrators_Delete.json
     */
    /**
     * Sample code: Delete a Microsoft Entra administrator from a HorizonDB cluster.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void deleteAMicrosoftEntraAdministratorFromAHorizonDBCluster(
        com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.microsoftEntraAdministrators()
            .delete("example-resource-group", "example-cluster", "11111111-1111-1111-1111-111111111111",
                com.azure.core.util.Context.NONE);
    }
}
```

### MicrosoftEntraAdministrators_Get

```java
/**
 * Samples for MicrosoftEntraAdministrators Get.
 */
public final class MicrosoftEntraAdministratorsGetSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/MicrosoftEntraAdministrators_Get.json
     */
    /**
     * Sample code: Get a Microsoft Entra administrator for a HorizonDB cluster.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void getAMicrosoftEntraAdministratorForAHorizonDBCluster(
        com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.microsoftEntraAdministrators()
            .getWithResponse("example-resource-group", "example-cluster", "11111111-1111-1111-1111-111111111111",
                com.azure.core.util.Context.NONE);
    }
}
```

### MicrosoftEntraAdministrators_List

```java
/**
 * Samples for MicrosoftEntraAdministrators List.
 */
public final class MicrosoftEntraAdministratorsListSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/MicrosoftEntraAdministrators_List.json
     */
    /**
     * Sample code: List Microsoft Entra administrators for a HorizonDB cluster.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void listMicrosoftEntraAdministratorsForAHorizonDBCluster(
        com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.microsoftEntraAdministrators()
            .list("example-resource-group", "example-cluster", com.azure.core.util.Context.NONE);
    }
}
```

### MicrosoftEntraAuthentications_CreateOrReplace

```java
import com.azure.resourcemanager.horizondb.fluent.models.MicrosoftEntraAuthenticationInner;
import com.azure.resourcemanager.horizondb.models.AuthenticationState;
import com.azure.resourcemanager.horizondb.models.MicrosoftEntraAuthenticationProperties;

/**
 * Samples for MicrosoftEntraAuthentications CreateOrReplace.
 */
public final class MicrosoftEntraAuthenticationsCreateOrReplaceSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/MicrosoftEntraAuthentications_CreateOrReplaceDisabled.json
     */
    /**
     * Sample code: Disable Microsoft Entra authentication declaratively.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        disableMicrosoftEntraAuthenticationDeclaratively(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.microsoftEntraAuthentications()
            .createOrReplace("example-resource-group", "example-cluster",
                new MicrosoftEntraAuthenticationInner().withProperties(
                    new MicrosoftEntraAuthenticationProperties().withState(AuthenticationState.DISABLED)),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-01-preview/MicrosoftEntraAuthentications_CreateOrReplaceEnabled.json
     */
    /**
     * Sample code: Enable Microsoft Entra authentication declaratively.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        enableMicrosoftEntraAuthenticationDeclaratively(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.microsoftEntraAuthentications()
            .createOrReplace("example-resource-group", "example-cluster",
                new MicrosoftEntraAuthenticationInner().withProperties(
                    new MicrosoftEntraAuthenticationProperties().withState(AuthenticationState.ENABLED)),
                com.azure.core.util.Context.NONE);
    }
}
```

### MicrosoftEntraAuthentications_Get

```java
/**
 * Samples for MicrosoftEntraAuthentications Get.
 */
public final class MicrosoftEntraAuthenticationsGetSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/MicrosoftEntraAuthentications_Get.json
     */
    /**
     * Sample code: Get the Microsoft Entra authentication method.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        getTheMicrosoftEntraAuthenticationMethod(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.microsoftEntraAuthentications()
            .getWithResponse("example-resource-group", "example-cluster", com.azure.core.util.Context.NONE);
    }
}
```

### MicrosoftEntraAuthentications_Update

```java
import com.azure.resourcemanager.horizondb.models.AuthenticationState;
import com.azure.resourcemanager.horizondb.models.MicrosoftEntraAuthenticationForPatchUpdate;
import com.azure.resourcemanager.horizondb.models.MicrosoftEntraAuthenticationPropertiesForPatchUpdate;

/**
 * Samples for MicrosoftEntraAuthentications Update.
 */
public final class MicrosoftEntraAuthenticationsUpdateSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/MicrosoftEntraAuthentications_Disable.json
     */
    /**
     * Sample code: Disable Microsoft Entra authentication without modifying existing database roles.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void disableMicrosoftEntraAuthenticationWithoutModifyingExistingDatabaseRoles(
        com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.microsoftEntraAuthentications()
            .update("example-resource-group", "example-cluster",
                new MicrosoftEntraAuthenticationForPatchUpdate().withProperties(
                    new MicrosoftEntraAuthenticationPropertiesForPatchUpdate().withState(AuthenticationState.DISABLED)),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-01-preview/MicrosoftEntraAuthentications_Enable.json
     */
    /**
     * Sample code: Start enabling Microsoft Entra authentication.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        startEnablingMicrosoftEntraAuthentication(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.microsoftEntraAuthentications()
            .update("example-resource-group", "example-cluster",
                new MicrosoftEntraAuthenticationForPatchUpdate().withProperties(
                    new MicrosoftEntraAuthenticationPropertiesForPatchUpdate().withState(AuthenticationState.ENABLED)),
                com.azure.core.util.Context.NONE);
    }
}
```

### Nodes_Get

```java
/**
 * Samples for Nodes Get.
 */
public final class NodesGetSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/Nodes_Get.json
     */
    /**
     * Sample code: Get a HorizonDB node.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void getAHorizonDBNode(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.nodes()
            .getWithResponse("example-resource-group", "example-cluster", "example-pool-01", "node-01",
                com.azure.core.util.Context.NONE);
    }
}
```

### Nodes_List

```java
/**
 * Samples for Nodes List.
 */
public final class NodesListSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/Nodes_List.json
     */
    /**
     * Sample code: List the nodes allocated to a HorizonDB pool.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        listTheNodesAllocatedToAHorizonDBPool(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.nodes()
            .list("example-resource-group", "example-cluster", "example-pool-01", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-10-01-preview/Operations_List.json
     */
    /**
     * Sample code: List operations for Microsoft.HorizonDb.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        listOperationsForMicrosoftHorizonDb(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### ParameterGroups_CreateOrReplace

```java
import com.azure.resourcemanager.horizondb.models.ParameterGroupProperties;
import com.azure.resourcemanager.horizondb.models.ParameterProperties;
import com.azure.resourcemanager.horizondb.models.PostgreSqlVersion;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ParameterGroups CreateOrReplace.
 */
public final class ParameterGroupsCreateOrReplaceSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/ParameterGroups_CreateOrReplace.json
     */
    /**
     * Sample code: Create or replace a HorizonDB parameter group for a PostgreSQL version.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void createOrReplaceAHorizonDBParameterGroupForAPostgreSQLVersion(
        com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.parameterGroups()
            .define("production-parameters")
            .withRegion("eastus2")
            .withExistingResourceGroup("example-resource-group")
            .withTags(mapOf("environment", "production"))
            .withProperties(new ParameterGroupProperties()
                .withParameters(Arrays.asList(
                    new ParameterProperties().withName("application_name").withValue("horizondb-production"),
                    new ParameterProperties().withName("autovacuum").withValue("off"),
                    new ParameterProperties().withName("wal_receiver_timeout").withValue("45000"),
                    new ParameterProperties().withName("work_mem").withValue("8192"),
                    new ParameterProperties().withName("xmlbinary").withValue("base64")))
                .withDescription("Parameters for production transactional workloads")
                .withPostgreSqlVersion(PostgreSqlVersion.V17))
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

### ParameterGroups_Delete

```java
/**
 * Samples for ParameterGroups Delete.
 */
public final class ParameterGroupsDeleteSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/ParameterGroups_Delete.json
     */
    /**
     * Sample code: Delete a HorizonDB parameter group.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void deleteAHorizonDBParameterGroup(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.parameterGroups()
            .delete("example-resource-group", "production-parameters", com.azure.core.util.Context.NONE);
    }
}
```

### ParameterGroups_GetByResourceGroup

```java
/**
 * Samples for ParameterGroups GetByResourceGroup.
 */
public final class ParameterGroupsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/ParameterGroups_Get.json
     */
    /**
     * Sample code: Get a HorizonDB parameter group.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void getAHorizonDBParameterGroup(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.parameterGroups()
            .getByResourceGroupWithResponse("example-resource-group", "production-parameters",
                com.azure.core.util.Context.NONE);
    }
}
```

### ParameterGroups_List

```java
/**
 * Samples for ParameterGroups List.
 */
public final class ParameterGroupsListSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/ParameterGroups_ListBySubscription.json
     */
    /**
     * Sample code: List HorizonDB parameter groups in a subscription.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        listHorizonDBParameterGroupsInASubscription(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.parameterGroups().list(com.azure.core.util.Context.NONE);
    }
}
```

### ParameterGroups_ListByResourceGroup

```java
/**
 * Samples for ParameterGroups ListByResourceGroup.
 */
public final class ParameterGroupsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/ParameterGroups_ListByResourceGroup.json
     */
    /**
     * Sample code: List HorizonDB parameter groups in a resource group.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        listHorizonDBParameterGroupsInAResourceGroup(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.parameterGroups().listByResourceGroup("example-resource-group", com.azure.core.util.Context.NONE);
    }
}
```

### ParameterGroups_ListConnections

```java
/**
 * Samples for ParameterGroups ListConnections.
 */
public final class ParameterGroupsListConnectionsSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/ParameterGroups_ListConnections.json
     */
    /**
     * Sample code: List HorizonDB pools connected to a parameter group.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        listHorizonDBPoolsConnectedToAParameterGroup(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.parameterGroups()
            .listConnections("example-resource-group", "production-parameters", com.azure.core.util.Context.NONE);
    }
}
```

### ParameterGroups_Update

```java
import com.azure.resourcemanager.horizondb.models.ParameterGroup;
import com.azure.resourcemanager.horizondb.models.ParameterGroupPropertiesForPatchUpdate;
import com.azure.resourcemanager.horizondb.models.ParameterProperties;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ParameterGroups Update.
 */
public final class ParameterGroupsUpdateSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/ParameterGroups_Update.json
     */
    /**
     * Sample code: Update a HorizonDB parameter group.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void updateAHorizonDBParameterGroup(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        ParameterGroup resource = manager.parameterGroups()
            .getByResourceGroupWithResponse("example-resource-group", "production-parameters",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("environment", "production", "team", "data-platform"))
            .withProperties(new ParameterGroupPropertiesForPatchUpdate()
                .withParameters(Arrays.asList(
                    new ParameterProperties().withName("application_name").withValue("horizondb-production"),
                    new ParameterProperties().withName("autovacuum").withValue("off"),
                    new ParameterProperties().withName("wal_receiver_timeout").withValue("45000"),
                    new ParameterProperties().withName("work_mem").withValue("8192"),
                    new ParameterProperties().withName("xmlbinary").withValue("base64")))
                .withDescription("Updated parameters for production transactional workloads"))
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

### PasswordAuthentications_CreateOrReplace

```java
import com.azure.resourcemanager.horizondb.fluent.models.PasswordAuthenticationInner;
import com.azure.resourcemanager.horizondb.models.AuthenticationState;
import com.azure.resourcemanager.horizondb.models.PasswordAuthenticationProperties;

/**
 * Samples for PasswordAuthentications CreateOrReplace.
 */
public final class PasswordAuthenticationsCreateOrReplaceSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/PasswordAuthentications_CreateOrReplace.json
     */
    /**
     * Sample code: Enable password authentication and set the administrator login and password during initial setup.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void enablePasswordAuthenticationAndSetTheAdministratorLoginAndPasswordDuringInitialSetup(
        com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.passwordAuthentications()
            .createOrReplace("example-resource-group", "example-cluster",
                new PasswordAuthenticationInner()
                    .withProperties(new PasswordAuthenticationProperties().withState(AuthenticationState.ENABLED)
                        .withAdministratorLogin("adminlogin1")
                        .withAdministratorLoginPassword("fakeTokenPlaceholder")),
                com.azure.core.util.Context.NONE);
    }
}
```

### PasswordAuthentications_Get

```java
/**
 * Samples for PasswordAuthentications Get.
 */
public final class PasswordAuthenticationsGetSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/PasswordAuthentications_Get.json
     */
    /**
     * Sample code: Get the password authentication method.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        getThePasswordAuthenticationMethod(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.passwordAuthentications()
            .getWithResponse("example-resource-group", "example-cluster", com.azure.core.util.Context.NONE);
    }
}
```

### PasswordAuthentications_Update

```java
import com.azure.resourcemanager.horizondb.models.AuthenticationState;
import com.azure.resourcemanager.horizondb.models.PasswordAuthenticationForPatchUpdate;
import com.azure.resourcemanager.horizondb.models.PasswordAuthenticationPropertiesForPatchUpdate;

/**
 * Samples for PasswordAuthentications Update.
 */
public final class PasswordAuthenticationsUpdateSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/PasswordAuthentications_ResetPassword.json
     */
    /**
     * Sample code: Reset the sole PostgreSQL administrator role password.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        resetTheSolePostgreSQLAdministratorRolePassword(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.passwordAuthentications()
            .update("example-resource-group", "example-cluster",
                new PasswordAuthenticationForPatchUpdate()
                    .withProperties(new PasswordAuthenticationPropertiesForPatchUpdate()
                        .withAdministratorLoginPassword("fakeTokenPlaceholder")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-01-preview/PasswordAuthentications_Enable.json
     */
    /**
     * Sample code: Enable password authentication and reset the existing administrator password.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void enablePasswordAuthenticationAndResetTheExistingAdministratorPassword(
        com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.passwordAuthentications()
            .update("example-resource-group", "example-cluster",
                new PasswordAuthenticationForPatchUpdate().withProperties(
                    new PasswordAuthenticationPropertiesForPatchUpdate().withState(AuthenticationState.ENABLED)
                        .withAdministratorLoginPassword("fakeTokenPlaceholder")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-01-preview/PasswordAuthentications_Disable.json
     */
    /**
     * Sample code: Disable password authentication.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void disablePasswordAuthentication(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.passwordAuthentications()
            .update("example-resource-group", "example-cluster",
                new PasswordAuthenticationForPatchUpdate().withProperties(
                    new PasswordAuthenticationPropertiesForPatchUpdate().withState(AuthenticationState.DISABLED)),
                com.azure.core.util.Context.NONE);
    }
}
```

### Pools_CreateOrReplace

```java
import com.azure.resourcemanager.horizondb.models.ComputeProcessorType;
import com.azure.resourcemanager.horizondb.models.ParameterApplyMethod;
import com.azure.resourcemanager.horizondb.models.PoolComputeModel;
import com.azure.resourcemanager.horizondb.models.PoolComputeModelType;
import com.azure.resourcemanager.horizondb.models.PoolHighAvailability;
import com.azure.resourcemanager.horizondb.models.PoolHighAvailabilityMode;
import com.azure.resourcemanager.horizondb.models.PoolParameterGroupReference;
import com.azure.resourcemanager.horizondb.models.PoolProperties;
import com.azure.resourcemanager.horizondb.models.SystemScheduledMaintenanceProperties;

/**
 * Samples for Pools CreateOrReplace.
 */
public final class PoolsCreateOrReplaceSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/Pools_CreateOrReplace.json
     */
    /**
     * Sample code: Create a minimum provisioned HorizonDB pool.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        createAMinimumProvisionedHorizonDBPool(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.pools()
            .define("example-pool-01")
            .withExistingCluster("example-resource-group", "example-cluster")
            .withProperties(new PoolProperties()
                .withComputeModel(new PoolComputeModel().withType(PoolComputeModelType.PROVISIONED)
                    .withComputeProcessor(ComputeProcessorType.AMD)
                    .withComputeGeneration(3)
                    .withVCores(2))
                .withNodeCount(1)
                .withHighAvailability(new PoolHighAvailability().withMode(PoolHighAvailabilityMode.DISABLED))
                .withParameterGroup(new PoolParameterGroupReference().withParameterGroupResourceId(
                    "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/providers/Microsoft.HorizonDb/locations/eastus2/parameterGroups/default-postgresql-17-provisioned")
                    .withApplyMethod(ParameterApplyMethod.IMMEDIATE))
                .withScheduledMaintenance(new SystemScheduledMaintenanceProperties()))
            .create();
    }
}
```

### Pools_Delete

```java
/**
 * Samples for Pools Delete.
 */
public final class PoolsDeleteSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/Pools_Delete.json
     */
    /**
     * Sample code: Delete the last HorizonDB pool and disconnect private endpoint connections.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void deleteTheLastHorizonDBPoolAndDisconnectPrivateEndpointConnections(
        com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.pools()
            .delete("example-resource-group", "example-cluster", "example-pool-01", com.azure.core.util.Context.NONE);
    }
}
```

### Pools_Get

```java
/**
 * Samples for Pools Get.
 */
public final class PoolsGetSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/Pools_Get.json
     */
    /**
     * Sample code: Get a HorizonDB pool.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void getAHorizonDBPool(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.pools()
            .getWithResponse("example-resource-group", "example-cluster", "example-pool-01",
                com.azure.core.util.Context.NONE);
    }
}
```

### Pools_List

```java
/**
 * Samples for Pools List.
 */
public final class PoolsListSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/Pools_List.json
     */
    /**
     * Sample code: List HorizonDB pools in a cluster.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void listHorizonDBPoolsInACluster(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.pools().list("example-resource-group", "example-cluster", com.azure.core.util.Context.NONE);
    }
}
```

### Pools_Restart

```java
/**
 * Samples for Pools Restart.
 */
public final class PoolsRestartSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/Pools_Restart.json
     */
    /**
     * Sample code: Restart a HorizonDB pool and apply pending parameter changes.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void restartAHorizonDBPoolAndApplyPendingParameterChanges(
        com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.pools()
            .restart("exampleresourcegroup", "examplecluster", "example-pool-01", com.azure.core.util.Context.NONE);
    }
}
```

### Pools_Start

```java
/**
 * Samples for Pools Start.
 */
public final class PoolsStartSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/Pools_Start.json
     */
    /**
     * Sample code: Start a HorizonDB pool without applying pending parameter changes.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void startAHorizonDBPoolWithoutApplyingPendingParameterChanges(
        com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.pools()
            .start("exampleresourcegroup", "examplecluster", "example-pool-01", com.azure.core.util.Context.NONE);
    }
}
```

### Pools_Stop

```java
/**
 * Samples for Pools Stop.
 */
public final class PoolsStopSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/Pools_Stop.json
     */
    /**
     * Sample code: Stop and deallocate a HorizonDB pool without applying pending parameter changes.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void stopAndDeallocateAHorizonDBPoolWithoutApplyingPendingParameterChanges(
        com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.pools()
            .stop("exampleresourcegroup", "examplecluster", "example-pool-01", com.azure.core.util.Context.NONE);
    }
}
```

### Pools_Update

```java
import com.azure.resourcemanager.horizondb.models.ParameterApplyMethod;
import com.azure.resourcemanager.horizondb.models.Pool;
import com.azure.resourcemanager.horizondb.models.PoolComputeModelForPatchUpdate;
import com.azure.resourcemanager.horizondb.models.PoolHighAvailabilityMode;
import com.azure.resourcemanager.horizondb.models.PoolHighAvailabilityPatch;
import com.azure.resourcemanager.horizondb.models.PoolParameterGroupReferenceForPatchUpdate;
import com.azure.resourcemanager.horizondb.models.PoolPropertiesForPatchUpdate;
import com.azure.resourcemanager.horizondb.models.ScheduledMaintenanceManagementType;
import com.azure.resourcemanager.horizondb.models.ScheduledMaintenancePropertiesForPatchUpdate;

/**
 * Samples for Pools Update.
 */
public final class PoolsUpdateSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/Pools_Update.json
     */
    /**
     * Sample code: Update a single HorizonDB pool compute property.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        updateASingleHorizonDBPoolComputeProperty(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        Pool resource = manager.pools()
            .getWithResponse("example-resource-group", "example-cluster", "example-pool-01",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(
                new PoolPropertiesForPatchUpdate().withComputeModel(new PoolComputeModelForPatchUpdate().withVCores(16))
                    .withScheduledMaintenance(new ScheduledMaintenancePropertiesForPatchUpdate()
                        .withMaintenanceManagementType(ScheduledMaintenanceManagementType.SYSTEM)))
            .apply();
    }

    /*
     * x-ms-original-file: 2026-10-01-preview/Pools_UpdateParameterGroupDefault.json
     */
    /**
     * Sample code: Switch a HorizonDB pool back to its corresponding default parameter group.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void switchAHorizonDBPoolBackToItsCorrespondingDefaultParameterGroup(
        com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        Pool resource = manager.pools()
            .getWithResponse("example-resource-group", "example-cluster", "example-pool-01",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new PoolPropertiesForPatchUpdate()
                .withParameterGroup(new PoolParameterGroupReferenceForPatchUpdate().withParameterGroupResourceId(
                    "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/providers/Microsoft.HorizonDb/locations/eastus2/parameterGroups/default-postgresql-17-provisioned")
                    .withApplyMethod(ParameterApplyMethod.IMMEDIATE)))
            .apply();
    }

    /*
     * x-ms-original-file: 2026-10-01-preview/Pools_UpdateParameterGroupImmediate.json
     */
    /**
     * Sample code: Associate a HorizonDB pool with a parameter group immediately.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void associateAHorizonDBPoolWithAParameterGroupImmediately(
        com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        Pool resource = manager.pools()
            .getWithResponse("example-resource-group", "example-cluster", "example-pool-01",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new PoolPropertiesForPatchUpdate()
                .withParameterGroup(new PoolParameterGroupReferenceForPatchUpdate().withParameterGroupResourceId(
                    "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/example-resource-group/providers/Microsoft.HorizonDb/parameterGroups/production-parameters")
                    .withApplyMethod(ParameterApplyMethod.IMMEDIATE)))
            .apply();
    }

    /*
     * x-ms-original-file: 2026-10-01-preview/Pools_UpdateParameterGroupPendingReboot.json
     */
    /**
     * Sample code: Associate a HorizonDB pool with a parameter group pending reboot.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void associateAHorizonDBPoolWithAParameterGroupPendingReboot(
        com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        Pool resource = manager.pools()
            .getWithResponse("example-resource-group", "example-cluster", "example-pool-01",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new PoolPropertiesForPatchUpdate()
                .withParameterGroup(new PoolParameterGroupReferenceForPatchUpdate().withParameterGroupResourceId(
                    "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/example-resource-group/providers/Microsoft.HorizonDb/parameterGroups/maintenance-window-parameters")
                    .withApplyMethod(ParameterApplyMethod.PENDING_REBOOT)))
            .apply();
    }

    /*
     * x-ms-original-file: 2026-10-01-preview/Pools_UpdateHighAvailabilityMode.json
     */
    /**
     * Sample code: Require zone-redundant placement for a HorizonDB pool.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        requireZoneRedundantPlacementForAHorizonDBPool(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        Pool resource = manager.pools()
            .getWithResponse("example-resource-group", "example-cluster", "example-pool-01",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new PoolPropertiesForPatchUpdate().withHighAvailability(
                new PoolHighAvailabilityPatch().withMode(PoolHighAvailabilityMode.ZONE_REDUNDANT)))
            .apply();
    }
}
```

### PrivateEndpointConnections_ApproveOrReject

```java
import com.azure.resourcemanager.horizondb.models.PrivateEndpointConnectionProperties;
import com.azure.resourcemanager.horizondb.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.horizondb.models.PrivateLinkServiceConnectionState;

/**
 * Samples for PrivateEndpointConnections ApproveOrReject.
 */
public final class PrivateEndpointConnectionsApproveOrRejectSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/PrivateEndpointConnections_ApproveOrReject.json
     */
    /**
     * Sample code: Approve or reject a private endpoint connection.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void
        approveOrRejectAPrivateEndpointConnection(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.privateEndpointConnections()
            .define("example-private-endpoint-connection")
            .withExistingCluster("example-resource-group", "example-cluster")
            .withProperties(new PrivateEndpointConnectionProperties().withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionState().withStatus(PrivateEndpointServiceConnectionStatus.APPROVED)
                    .withDescription("Approved for the production database network")))
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
     * x-ms-original-file: 2026-10-01-preview/PrivateEndpointConnections_Delete.json
     */
    /**
     * Sample code: Delete a private endpoint connection.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void deleteAPrivateEndpointConnection(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.privateEndpointConnections()
            .delete("example-resource-group", "example-cluster", "example-private-endpoint-connection",
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
     * x-ms-original-file: 2026-10-01-preview/PrivateEndpointConnections_GetDisconnectedAfterLastPoolDeletion.json
     */
    /**
     * Sample code: Get a private endpoint connection disconnected after the last pool was deleted.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void getAPrivateEndpointConnectionDisconnectedAfterTheLastPoolWasDeleted(
        com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.privateEndpointConnections()
            .getWithResponse("example-resource-group", "example-cluster", "example-private-endpoint-connection",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-01-preview/PrivateEndpointConnections_Get.json
     */
    /**
     * Sample code: Get a private endpoint connection.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void getAPrivateEndpointConnection(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.privateEndpointConnections()
            .getWithResponse("example-resource-group", "example-cluster", "example-private-endpoint-connection",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_List

```java
/**
 * Samples for PrivateEndpointConnections List.
 */
public final class PrivateEndpointConnectionsListSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/PrivateEndpointConnections_List.json
     */
    /**
     * Sample code: List private endpoint connections on a HorizonDB cluster.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void listPrivateEndpointConnectionsOnAHorizonDBCluster(
        com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.privateEndpointConnections()
            .list("example-resource-group", "example-cluster", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-10-01-preview/PrivateLinkResources_Get.json
     */
    /**
     * Sample code: Get a HorizonDB private link resource.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void getAHorizonDBPrivateLinkResource(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.privateLinkResources()
            .getWithResponse("example-resource-group", "example-cluster", "example-cluster",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_List

```java
/**
 * Samples for PrivateLinkResources List.
 */
public final class PrivateLinkResourcesListSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/PrivateLinkResources_List.json
     */
    /**
     * Sample code: List HorizonDB private link resources.
     * 
     * @param manager Entry point to HorizonDbManager.
     */
    public static void listHorizonDBPrivateLinkResources(com.azure.resourcemanager.horizondb.HorizonDbManager manager) {
        manager.privateLinkResources()
            .list("example-resource-group", "example-cluster", com.azure.core.util.Context.NONE);
    }
}
```


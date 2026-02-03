# Code snippets and samples


## CheckNameAvailability

- [Post](#checknameavailability_post)

## DeploymentPreflight

- [Post](#deploymentpreflight_post)

## EmailConfiguration

- [Create](#emailconfiguration_create)
- [Get](#emailconfiguration_get)
- [List](#emailconfiguration_list)

## Event

- [Get](#event_get)
- [List](#event_list)

## Fabric

- [Create](#fabric_create)
- [Delete](#fabric_delete)
- [GetByResourceGroup](#fabric_getbyresourcegroup)
- [List](#fabric_list)
- [ListByResourceGroup](#fabric_listbyresourcegroup)
- [Update](#fabric_update)

## FabricAgent

- [Create](#fabricagent_create)
- [Delete](#fabricagent_delete)
- [Get](#fabricagent_get)
- [List](#fabricagent_list)

## Job

- [Get](#job_get)
- [List](#job_list)

## Operations

- [List](#operations_list)

## Policy

- [Create](#policy_create)
- [Delete](#policy_delete)
- [Get](#policy_get)
- [List](#policy_list)

## PrivateEndpointConnProxies

- [Create](#privateendpointconnproxies_create)
- [Delete](#privateendpointconnproxies_delete)
- [Get](#privateendpointconnproxies_get)
- [List](#privateendpointconnproxies_list)
- [Validate](#privateendpointconnproxies_validate)

## PrivateEndpointConnections

- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [List](#privateendpointconnections_list)
- [Update](#privateendpointconnections_update)

## PrivateLinkResources

- [Get](#privatelinkresources_get)
- [List](#privatelinkresources_list)

## ProtectedItem

- [Create](#protecteditem_create)
- [Delete](#protecteditem_delete)
- [Get](#protecteditem_get)
- [List](#protecteditem_list)
- [PlannedFailover](#protecteditem_plannedfailover)
- [Update](#protecteditem_update)

## RecoveryPoint

- [Get](#recoverypoint_get)
- [List](#recoverypoint_list)

## ReplicationExtension

- [Create](#replicationextension_create)
- [Delete](#replicationextension_delete)
- [Get](#replicationextension_get)
- [List](#replicationextension_list)

## Vault

- [Create](#vault_create)
- [Delete](#vault_delete)
- [GetByResourceGroup](#vault_getbyresourcegroup)
- [List](#vault_list)
- [ListByResourceGroup](#vault_listbyresourcegroup)
- [Update](#vault_update)
### CheckNameAvailability_Post

```java
import com.azure.resourcemanager.recoveryservicesdatareplication.models.CheckNameAvailabilityModel;

/**
 * Samples for CheckNameAvailability Post.
 */
public final class CheckNameAvailabilityPostSamples {
    /*
     * x-ms-original-file: 2024-09-01/CheckNameAvailability_Post.json
     */
    /**
     * Sample code: Performs the resource name availability check.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void performsTheResourceNameAvailabilityCheck(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.checkNameAvailabilities()
            .postWithResponse("trfqtbtmusswpibw",
                new CheckNameAvailabilityModel().withName("updkdcixs").withType("gngmcancdauwhdixjjvqnfkvqc"),
                com.azure.core.util.Context.NONE);
    }
}
```

### DeploymentPreflight_Post

```java
import com.azure.resourcemanager.recoveryservicesdatareplication.fluent.models.DeploymentPreflightModelInner;
import com.azure.resourcemanager.recoveryservicesdatareplication.models.DeploymentPreflightResource;
import java.util.Arrays;

/**
 * Samples for DeploymentPreflight Post.
 */
public final class DeploymentPreflightPostSamples {
    /*
     * x-ms-original-file: 2024-09-01/DeploymentPreflight_Post.json
     */
    /**
     * Sample code: Performs resource deployment validation.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void performsResourceDeploymentValidation(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.deploymentPreflights()
            .postWithResponse("rgswagger_2024-09-01", "lnfcwsmlowbwkndkztzvaj",
                new DeploymentPreflightModelInner()
                    .withResources(Arrays.asList(new DeploymentPreflightResource().withName("xtgugoflfc")
                        .withType("nsnaptduolqcxsikrewvgjbxqpt")
                        .withLocation("cbsgtxkjdzwbyp")
                        .withApiVersion("otihymhvzblycdoxo"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### EmailConfiguration_Create

```java
/**
 * Samples for EmailConfiguration Create.
 */
public final class EmailConfigurationCreateSamples {
    /*
     * x-ms-original-file: 2024-09-01/EmailConfiguration_Create.json
     */
    /**
     * Sample code: Creates email configuration settings.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void createsEmailConfigurationSettings(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.emailConfigurations().define("0").withExistingReplicationVault("rgswagger_2024-09-01", "4").create();
    }
}
```

### EmailConfiguration_Get

```java
/**
 * Samples for EmailConfiguration Get.
 */
public final class EmailConfigurationGetSamples {
    /*
     * x-ms-original-file: 2024-09-01/EmailConfiguration_Get.json
     */
    /**
     * Sample code: Gets the email configuration setting.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void getsTheEmailConfigurationSetting(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.emailConfigurations()
            .getWithResponse("rgswagger_2024-09-01", "4", "0", com.azure.core.util.Context.NONE);
    }
}
```

### EmailConfiguration_List

```java
/**
 * Samples for EmailConfiguration List.
 */
public final class EmailConfigurationListSamples {
    /*
     * x-ms-original-file: 2024-09-01/EmailConfiguration_List.json
     */
    /**
     * Sample code: Lists the email configuration settings.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void listsTheEmailConfigurationSettings(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.emailConfigurations().list("rgswagger_2024-09-01", "4", com.azure.core.util.Context.NONE);
    }
}
```

### Event_Get

```java
/**
 * Samples for Event Get.
 */
public final class EventGetSamples {
    /*
     * x-ms-original-file: 2024-09-01/Event_Get.json
     */
    /**
     * Sample code: Gets the event.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void getsTheEvent(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.events().getWithResponse("rgswagger_2024-09-01", "4", "231CIG", com.azure.core.util.Context.NONE);
    }
}
```

### Event_List

```java
/**
 * Samples for Event List.
 */
public final class EventListSamples {
    /*
     * x-ms-original-file: 2024-09-01/Event_List.json
     */
    /**
     * Sample code: Lists the events.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void listsTheEvents(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.events()
            .list("rgswagger_2024-09-01", "4", null, "gabpzsxrifposvleqqcjnvofz", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### Fabric_Create

```java
/**
 * Samples for Fabric Create.
 */
public final class FabricCreateSamples {
    /*
     * x-ms-original-file: 2024-09-01/Fabric_Create.json
     */
    /**
     * Sample code: Puts the fabric.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void putsTheFabric(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.fabrics()
            .define("wPR")
            .withRegion((String) null)
            .withExistingResourceGroup("rgswagger_2024-09-01")
            .create();
    }
}
```

### Fabric_Delete

```java
/**
 * Samples for Fabric Delete.
 */
public final class FabricDeleteSamples {
    /*
     * x-ms-original-file: 2024-09-01/Fabric_Delete.json
     */
    /**
     * Sample code: Deletes the fabric.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void deletesTheFabric(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.fabrics().delete("rgrecoveryservicesdatareplication", "wPR", com.azure.core.util.Context.NONE);
    }
}
```

### Fabric_GetByResourceGroup

```java
/**
 * Samples for Fabric GetByResourceGroup.
 */
public final class FabricGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-09-01/Fabric_Get.json
     */
    /**
     * Sample code: Gets the fabric.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void getsTheFabric(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.fabrics()
            .getByResourceGroupWithResponse("rgrecoveryservicesdatareplication", "wPR",
                com.azure.core.util.Context.NONE);
    }
}
```

### Fabric_List

```java
/**
 * Samples for Fabric List.
 */
public final class FabricListSamples {
    /*
     * x-ms-original-file: 2024-09-01/Fabric_ListBySubscription.json
     */
    /**
     * Sample code: Lists the fabrics by subscription.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void listsTheFabricsBySubscription(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.fabrics().list(com.azure.core.util.Context.NONE);
    }
}
```

### Fabric_ListByResourceGroup

```java
/**
 * Samples for Fabric ListByResourceGroup.
 */
public final class FabricListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-09-01/Fabric_List.json
     */
    /**
     * Sample code: Lists the fabrics.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void listsTheFabrics(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.fabrics().listByResourceGroup("rgswagger_2024-09-01", "jw", com.azure.core.util.Context.NONE);
    }
}
```

### Fabric_Update

```java
import com.azure.resourcemanager.recoveryservicesdatareplication.models.FabricModel;

/**
 * Samples for Fabric Update.
 */
public final class FabricUpdateSamples {
    /*
     * x-ms-original-file: 2024-09-01/Fabric_Update.json
     */
    /**
     * Sample code: Updates the fabric.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void updatesTheFabric(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        FabricModel resource = manager.fabrics()
            .getByResourceGroupWithResponse("rgswagger_2024-09-01", "wPR", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().apply();
    }
}
```

### FabricAgent_Create

```java
/**
 * Samples for FabricAgent Create.
 */
public final class FabricAgentCreateSamples {
    /*
     * x-ms-original-file: 2024-09-01/FabricAgent_Create.json
     */
    /**
     * Sample code: Puts the fabric agent.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void putsTheFabricAgent(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.fabricAgents().define("M").withExistingReplicationFabric("rgswagger_2024-09-01", "wPR").create();
    }
}
```

### FabricAgent_Delete

```java
/**
 * Samples for FabricAgent Delete.
 */
public final class FabricAgentDeleteSamples {
    /*
     * x-ms-original-file: 2024-09-01/FabricAgent_Delete.json
     */
    /**
     * Sample code: Deletes the Fabric Agent.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void deletesTheFabricAgent(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.fabricAgents().delete("rgswagger_2024-09-01", "wPR", "M", com.azure.core.util.Context.NONE);
    }
}
```

### FabricAgent_Get

```java
/**
 * Samples for FabricAgent Get.
 */
public final class FabricAgentGetSamples {
    /*
     * x-ms-original-file: 2024-09-01/FabricAgent_Get.json
     */
    /**
     * Sample code: Gets the fabric agent.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void getsTheFabricAgent(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.fabricAgents()
            .getWithResponse("rgrecoveryservicesdatareplication", "wPR", "M", com.azure.core.util.Context.NONE);
    }
}
```

### FabricAgent_List

```java
/**
 * Samples for FabricAgent List.
 */
public final class FabricAgentListSamples {
    /*
     * x-ms-original-file: 2024-09-01/FabricAgent_List.json
     */
    /**
     * Sample code: Lists the fabric agents.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void listsTheFabricAgents(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.fabricAgents().list("rgswagger_2024-09-01", "wPR", com.azure.core.util.Context.NONE);
    }
}
```

### Job_Get

```java
/**
 * Samples for Job Get.
 */
public final class JobGetSamples {
    /*
     * x-ms-original-file: 2024-09-01/Job_Get.json
     */
    /**
     * Sample code: Gets the job.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void getsTheJob(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.jobs()
            .getWithResponse("rgrecoveryservicesdatareplication", "4", "ZGH4y", com.azure.core.util.Context.NONE);
    }
}
```

### Job_List

```java
/**
 * Samples for Job List.
 */
public final class JobListSamples {
    /*
     * x-ms-original-file: 2024-09-01/Job_List.json
     */
    /**
     * Sample code: Lists the jobs.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void listsTheJobs(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.jobs()
            .list("rgrecoveryservicesdatareplication", "4", null, "rdavrzbethhslmkqgajontnxsue", null,
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
     * x-ms-original-file: 2024-09-01/Operations_List.json
     */
    /**
     * Sample code: Get a list of REST API operations supported by Microsoft.DataReplication.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void getAListOfRESTAPIOperationsSupportedByMicrosoftDataReplication(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Policy_Create

```java
/**
 * Samples for Policy Create.
 */
public final class PolicyCreateSamples {
    /*
     * x-ms-original-file: 2024-09-01/Policy_Create.json
     */
    /**
     * Sample code: Puts the policy.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void putsThePolicy(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.policies()
            .define("fafqwc")
            .withExistingReplicationVault("rgrecoveryservicesdatareplication", "4")
            .create();
    }
}
```

### Policy_Delete

```java
/**
 * Samples for Policy Delete.
 */
public final class PolicyDeleteSamples {
    /*
     * x-ms-original-file: 2024-09-01/Policy_Delete.json
     */
    /**
     * Sample code: Deletes the policy.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void deletesThePolicy(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.policies()
            .delete("rgrecoveryservicesdatareplication", "4", "wqfscsdv", com.azure.core.util.Context.NONE);
    }
}
```

### Policy_Get

```java
/**
 * Samples for Policy Get.
 */
public final class PolicyGetSamples {
    /*
     * x-ms-original-file: 2024-09-01/Policy_Get.json
     */
    /**
     * Sample code: Gets the policy.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void getsThePolicy(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.policies()
            .getWithResponse("rgrecoveryservicesdatareplication", "4", "wdqsacasc", com.azure.core.util.Context.NONE);
    }
}
```

### Policy_List

```java
/**
 * Samples for Policy List.
 */
public final class PolicyListSamples {
    /*
     * x-ms-original-file: 2024-09-01/Policy_List.json
     */
    /**
     * Sample code: Lists the policies.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void listsThePolicies(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.policies().list("rgrecoveryservicesdatareplication", "4", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnProxies_Create

```java
/**
 * Samples for PrivateEndpointConnProxies Create.
 */
public final class PrivateEndpointConnProxiesCreateSamples {
    /*
     * x-ms-original-file: 2024-09-01/PrivateEndpointConnectionProxy_Create.json
     */
    /**
     * Sample code: Creates the Private Endpoint Connection Proxy.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void createsThePrivateEndpointConnectionProxy(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.privateEndpointConnProxies()
            .define("d")
            .withExistingReplicationVault("rgswagger_2024-09-01", "4")
            .create();
    }
}
```

### PrivateEndpointConnProxies_Delete

```java
/**
 * Samples for PrivateEndpointConnProxies Delete.
 */
public final class PrivateEndpointConnProxiesDeleteSamples {
    /*
     * x-ms-original-file: 2024-09-01/PrivateEndpointConnectionProxy_Delete.json
     */
    /**
     * Sample code: Deletes the Private Endpoint Proxy Connection.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void deletesThePrivateEndpointProxyConnection(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.privateEndpointConnProxies().delete("rgswagger_2024-09-01", "4", "d", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnProxies_Get

```java
/**
 * Samples for PrivateEndpointConnProxies Get.
 */
public final class PrivateEndpointConnProxiesGetSamples {
    /*
     * x-ms-original-file: 2024-09-01/PrivateEndpointConnectionProxy_Get.json
     */
    /**
     * Sample code: Get Private Endpoint Connection Proxy.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void getPrivateEndpointConnectionProxy(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.privateEndpointConnProxies()
            .getWithResponse("rgswagger_2024-09-01", "4", "d", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnProxies_List

```java
/**
 * Samples for PrivateEndpointConnProxies List.
 */
public final class PrivateEndpointConnProxiesListSamples {
    /*
     * x-ms-original-file: 2024-09-01/PrivateEndpointConnectionProxy_List.json
     */
    /**
     * Sample code: Lists the Private Endpoint Connection Proxy.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void listsThePrivateEndpointConnectionProxy(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.privateEndpointConnProxies().list("rgswagger_2024-09-01", "4", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnProxies_Validate

```java
import com.azure.resourcemanager.recoveryservicesdatareplication.fluent.models.PrivateEndpointConnectionProxyInner;
import com.azure.resourcemanager.recoveryservicesdatareplication.models.ConnectionDetails;
import com.azure.resourcemanager.recoveryservicesdatareplication.models.GroupConnectivityInformation;
import com.azure.resourcemanager.recoveryservicesdatareplication.models.PrivateEndpointConnectionProxyProperties;
import com.azure.resourcemanager.recoveryservicesdatareplication.models.PrivateEndpointConnectionStatus;
import com.azure.resourcemanager.recoveryservicesdatareplication.models.PrivateLinkServiceConnection;
import com.azure.resourcemanager.recoveryservicesdatareplication.models.PrivateLinkServiceConnectionState;
import com.azure.resourcemanager.recoveryservicesdatareplication.models.PrivateLinkServiceProxy;
import com.azure.resourcemanager.recoveryservicesdatareplication.models.RemotePrivateEndpoint;
import com.azure.resourcemanager.recoveryservicesdatareplication.models.RemotePrivateEndpointConnection;
import java.util.Arrays;

/**
 * Samples for PrivateEndpointConnProxies Validate.
 */
public final class PrivateEndpointConnProxiesValidateSamples {
    /*
     * x-ms-original-file: 2024-09-01/PrivateEndpointConnectionProxy_Validate.json
     */
    /**
     * Sample code: Validates the Private Endpoint Connection Proxy.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void validatesThePrivateEndpointConnectionProxy(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.privateEndpointConnProxies()
            .validateWithResponse("rgswagger_2024-09-01", "4", "d",
                new PrivateEndpointConnectionProxyInner()
                    .withProperties(new PrivateEndpointConnectionProxyProperties()
                        .withRemotePrivateEndpoint(new RemotePrivateEndpoint().withId("yipalno")
                            .withPrivateLinkServiceConnections(
                                Arrays.asList(new PrivateLinkServiceConnection().withName("jqwntlzfsksl")
                                    .withGroupIds(Arrays.asList("hvejynjktikteipnioyeja"))
                                    .withRequestMessage("bukgzpkvcvfbmcdmpcbiigbvugicqa")))
                            .withManualPrivateLinkServiceConnections(
                                Arrays.asList(new PrivateLinkServiceConnection().withName("jqwntlzfsksl")
                                    .withGroupIds(Arrays.asList("hvejynjktikteipnioyeja"))
                                    .withRequestMessage("bukgzpkvcvfbmcdmpcbiigbvugicqa")))
                            .withPrivateLinkServiceProxies(
                                Arrays.asList(new PrivateLinkServiceProxy().withId("nzqxevuyqeedrqnkbnlcyrrrbzxvl")
                                    .withRemotePrivateLinkServiceConnectionState(new PrivateLinkServiceConnectionState()
                                        .withStatus(PrivateEndpointConnectionStatus.APPROVED)
                                        .withDescription("y")
                                        .withActionsRequired("afwbq"))
                                    .withRemotePrivateEndpointConnection(
                                        new RemotePrivateEndpointConnection().withId("ocunsgawjsqohkrcyxiv"))
                                    .withGroupConnectivityInformation(
                                        Arrays.asList(new GroupConnectivityInformation().withGroupId("per")
                                            .withMemberName("ybptuypgdqoxkuwqx")
                                            .withCustomerVisibleFqdns(Arrays.asList("vedcg"))
                                            .withInternalFqdn("maqavwhxwzzhbzjbryyquvitmup")
                                            .withRedirectMapId("pezncxcq")
                                            .withPrivateLinkServiceArmRegion("rerkqqxinteevmlbrdkktaqhcch")))))
                            .withConnectionDetails(Arrays.asList(new ConnectionDetails().withId("lenqkogzkes")
                                .withPrivateIpAddress("cyiacdzzyqmxjpijjbwgasegehtqe")
                                .withLinkIdentifier("ravfufhkdowufd")
                                .withGroupId("pjrlygpadir")
                                .withMemberName("ybuysjrlfupewxe")))))
                    .withEtag("hruibxrezstxroxrxweh"),
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2024-09-01/PrivateEndpointConnection_Delete.json
     */
    /**
     * Sample code: Deletes the Private Endpoint Connection.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void deletesThePrivateEndpointConnection(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.privateEndpointConnections()
            .delete("rgswagger_2024-09-01", "4", "sdwqtfhigjirrzhpbmqtzgs", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2024-09-01/PrivateEndpointConnection_Get.json
     */
    /**
     * Sample code: Gets the Private Endpoint Connection.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void getsThePrivateEndpointConnection(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.privateEndpointConnections()
            .getWithResponse("rgswagger_2024-09-01", "4", "vbkm", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2024-09-01/PrivateEndpointConnection_List.json
     */
    /**
     * Sample code: Lists the Private Endpoint Connections.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void listsThePrivateEndpointConnections(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.privateEndpointConnections().list("rgswagger_2024-09-01", "4", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_Update

```java
import com.azure.resourcemanager.recoveryservicesdatareplication.fluent.models.PrivateEndpointConnectionInner;
import com.azure.resourcemanager.recoveryservicesdatareplication.models.PrivateEndpoint;
import com.azure.resourcemanager.recoveryservicesdatareplication.models.PrivateEndpointConnectionResponseProperties;
import com.azure.resourcemanager.recoveryservicesdatareplication.models.PrivateEndpointConnectionStatus;
import com.azure.resourcemanager.recoveryservicesdatareplication.models.PrivateLinkServiceConnectionState;

/**
 * Samples for PrivateEndpointConnections Update.
 */
public final class PrivateEndpointConnectionsUpdateSamples {
    /*
     * x-ms-original-file: 2024-09-01/PrivateEndpointConnection_Update.json
     */
    /**
     * Sample code: Updates the Private Endpoint Connection.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void updatesThePrivateEndpointConnection(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.privateEndpointConnections()
            .updateWithResponse("rgswagger_2024-09-01", "4", "jitf",
                new PrivateEndpointConnectionInner()
                    .withProperties(
                        new PrivateEndpointConnectionResponseProperties()
                            .withPrivateEndpoint(new PrivateEndpoint().withId("cwcdqoynostmqwdwy"))
                            .withPrivateLinkServiceConnectionState(new PrivateLinkServiceConnectionState()
                                .withStatus(PrivateEndpointConnectionStatus.APPROVED)
                                .withDescription("y")
                                .withActionsRequired("afwbq"))),
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2024-09-01/PrivateLinkResource_Get.json
     */
    /**
     * Sample code: Get Private Link Resource.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void getPrivateLinkResource(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.privateLinkResources()
            .getWithResponse("rgswagger_2024-09-01", "4", "d", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2024-09-01/PrivateLinkResource_List.json
     */
    /**
     * Sample code: PrivateLinkResource_List - generated by [MaximumSet] rule.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void privateLinkResourceListGeneratedByMaximumSetRule(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.privateLinkResources().list("rgswagger_2024-09-01", "4", com.azure.core.util.Context.NONE);
    }
}
```

### ProtectedItem_Create

```java
/**
 * Samples for ProtectedItem Create.
 */
public final class ProtectedItemCreateSamples {
    /*
     * x-ms-original-file: 2024-09-01/ProtectedItem_Create.json
     */
    /**
     * Sample code: Puts the protected item.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void putsTheProtectedItem(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.protectedItems()
            .define("d")
            .withExistingReplicationVault("rgrecoveryservicesdatareplication", "4")
            .create();
    }
}
```

### ProtectedItem_Delete

```java
/**
 * Samples for ProtectedItem Delete.
 */
public final class ProtectedItemDeleteSamples {
    /*
     * x-ms-original-file: 2024-09-01/ProtectedItem_Delete.json
     */
    /**
     * Sample code: Deletes the protected item.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void deletesTheProtectedItem(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.protectedItems()
            .delete("rgrecoveryservicesdatareplication", "4", "d", true, com.azure.core.util.Context.NONE);
    }
}
```

### ProtectedItem_Get

```java
/**
 * Samples for ProtectedItem Get.
 */
public final class ProtectedItemGetSamples {
    /*
     * x-ms-original-file: 2024-09-01/ProtectedItem_Get.json
     */
    /**
     * Sample code: Gets the protected item.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void getsTheProtectedItem(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.protectedItems()
            .getWithResponse("rgrecoveryservicesdatareplication", "4", "d", com.azure.core.util.Context.NONE);
    }
}
```

### ProtectedItem_List

```java
/**
 * Samples for ProtectedItem List.
 */
public final class ProtectedItemListSamples {
    /*
     * x-ms-original-file: 2024-09-01/ProtectedItem_List.json
     */
    /**
     * Sample code: Lists the protected items.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void listsTheProtectedItems(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.protectedItems()
            .list("rgrecoveryservicesdatareplication", "4", null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### ProtectedItem_PlannedFailover

```java
import com.azure.resourcemanager.recoveryservicesdatareplication.fluent.models.PlannedFailoverModelInner;
import com.azure.resourcemanager.recoveryservicesdatareplication.models.PlannedFailoverModelCustomProperties;
import com.azure.resourcemanager.recoveryservicesdatareplication.models.PlannedFailoverModelProperties;

/**
 * Samples for ProtectedItem PlannedFailover.
 */
public final class ProtectedItemPlannedFailoverSamples {
    /*
     * x-ms-original-file: 2024-09-01/ProtectedItem_PlannedFailover.json
     */
    /**
     * Sample code: Performs planned failover.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void performsPlannedFailover(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.protectedItems()
            .plannedFailover("rgrecoveryservicesdatareplication", "4", "d",
                new PlannedFailoverModelInner().withProperties(new PlannedFailoverModelProperties()
                    .withCustomProperties(new PlannedFailoverModelCustomProperties())),
                com.azure.core.util.Context.NONE);
    }
}
```

### ProtectedItem_Update

```java
import com.azure.resourcemanager.recoveryservicesdatareplication.models.ProtectedItemModel;

/**
 * Samples for ProtectedItem Update.
 */
public final class ProtectedItemUpdateSamples {
    /*
     * x-ms-original-file: 2024-09-01/ProtectedItem_Update.json
     */
    /**
     * Sample code: Update Protected Item.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void updateProtectedItem(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        ProtectedItemModel resource = manager.protectedItems()
            .getWithResponse("rgswagger_2024-09-01", "4", "d", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().apply();
    }
}
```

### RecoveryPoint_Get

```java
/**
 * Samples for RecoveryPoint Get.
 */
public final class RecoveryPointGetSamples {
    /*
     * x-ms-original-file: 2024-09-01/RecoveryPoints_Get.json
     */
    /**
     * Sample code: Gets the recovery point.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void getsTheRecoveryPoint(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.recoveryPoints()
            .getWithResponse("rgrecoveryservicesdatareplication", "4", "d", "1X", com.azure.core.util.Context.NONE);
    }
}
```

### RecoveryPoint_List

```java
/**
 * Samples for RecoveryPoint List.
 */
public final class RecoveryPointListSamples {
    /*
     * x-ms-original-file: 2024-09-01/RecoveryPoints_List.json
     */
    /**
     * Sample code: Lists the recovery points.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void listsTheRecoveryPoints(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.recoveryPoints().list("rgrecoveryservicesdatareplication", "4", "d", com.azure.core.util.Context.NONE);
    }
}
```

### ReplicationExtension_Create

```java
/**
 * Samples for ReplicationExtension Create.
 */
public final class ReplicationExtensionCreateSamples {
    /*
     * x-ms-original-file: 2024-09-01/ReplicationExtension_Create.json
     */
    /**
     * Sample code: Puts the replication extension.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void putsTheReplicationExtension(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.replicationExtensions()
            .define("g16yjJ")
            .withExistingReplicationVault("rgrecoveryservicesdatareplication", "4")
            .create();
    }
}
```

### ReplicationExtension_Delete

```java
/**
 * Samples for ReplicationExtension Delete.
 */
public final class ReplicationExtensionDeleteSamples {
    /*
     * x-ms-original-file: 2024-09-01/ReplicationExtension_Delete.json
     */
    /**
     * Sample code: Deletes the replication extension.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void deletesTheReplicationExtension(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.replicationExtensions()
            .delete("rgrecoveryservicesdatareplication", "4", "g16yjJ", com.azure.core.util.Context.NONE);
    }
}
```

### ReplicationExtension_Get

```java
/**
 * Samples for ReplicationExtension Get.
 */
public final class ReplicationExtensionGetSamples {
    /*
     * x-ms-original-file: 2024-09-01/ReplicationExtension_Get.json
     */
    /**
     * Sample code: Gets the replication extension.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void getsTheReplicationExtension(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.replicationExtensions()
            .getWithResponse("rgrecoveryservicesdatareplication", "4", "g16yjJ", com.azure.core.util.Context.NONE);
    }
}
```

### ReplicationExtension_List

```java
/**
 * Samples for ReplicationExtension List.
 */
public final class ReplicationExtensionListSamples {
    /*
     * x-ms-original-file: 2024-09-01/ReplicationExtension_List.json
     */
    /**
     * Sample code: Lists the replication extensions.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void listsTheReplicationExtensions(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.replicationExtensions()
            .list("rgrecoveryservicesdatareplication", "4", com.azure.core.util.Context.NONE);
    }
}
```

### Vault_Create

```java
/**
 * Samples for Vault Create.
 */
public final class VaultCreateSamples {
    /*
     * x-ms-original-file: 2024-09-01/Vault_Create.json
     */
    /**
     * Sample code: Puts the vault.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void putsTheVault(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.vaults()
            .define("4")
            .withRegion((String) null)
            .withExistingResourceGroup("rgrecoveryservicesdatareplication")
            .create();
    }
}
```

### Vault_Delete

```java
/**
 * Samples for Vault Delete.
 */
public final class VaultDeleteSamples {
    /*
     * x-ms-original-file: 2024-09-01/Vault_Delete.json
     */
    /**
     * Sample code: Deletes the vault.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void deletesTheVault(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.vaults().delete("rgrecoveryservicesdatareplication", "4", com.azure.core.util.Context.NONE);
    }
}
```

### Vault_GetByResourceGroup

```java
/**
 * Samples for Vault GetByResourceGroup.
 */
public final class VaultGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-09-01/Vault_Get.json
     */
    /**
     * Sample code: Gets the vault.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void getsTheVault(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.vaults()
            .getByResourceGroupWithResponse("rgrecoveryservicesdatareplication", "4", com.azure.core.util.Context.NONE);
    }
}
```

### Vault_List

```java
/**
 * Samples for Vault List.
 */
public final class VaultListSamples {
    /*
     * x-ms-original-file: 2024-09-01/Vault_ListBySubscription.json
     */
    /**
     * Sample code: Lists the vaults by subscription.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void listsTheVaultsBySubscription(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.vaults().list(com.azure.core.util.Context.NONE);
    }
}
```

### Vault_ListByResourceGroup

```java
/**
 * Samples for Vault ListByResourceGroup.
 */
public final class VaultListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-09-01/Vault_List.json
     */
    /**
     * Sample code: Lists the vaults.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void listsTheVaults(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.vaults()
            .listByResourceGroup("rgrecoveryservicesdatareplication", "mwculdaqndp", com.azure.core.util.Context.NONE);
    }
}
```

### Vault_Update

```java
import com.azure.resourcemanager.recoveryservicesdatareplication.models.VaultModel;

/**
 * Samples for Vault Update.
 */
public final class VaultUpdateSamples {
    /*
     * x-ms-original-file: 2024-09-01/Vault_Update.json
     */
    /**
     * Sample code: Updates the vault.
     * 
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void updatesTheVault(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        VaultModel resource = manager.vaults()
            .getByResourceGroupWithResponse("rgrecoveryservicesdatareplication", "4", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().apply();
    }
}
```


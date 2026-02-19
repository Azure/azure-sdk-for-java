# Code snippets and samples


## Access

- [CreateRoleBinding](#access_createrolebinding)
- [DeleteRoleBinding](#access_deleterolebinding)
- [InviteUser](#access_inviteuser)
- [ListClusters](#access_listclusters)
- [ListEnvironments](#access_listenvironments)
- [ListInvitations](#access_listinvitations)
- [ListRoleBindingNameList](#access_listrolebindingnamelist)
- [ListRoleBindings](#access_listrolebindings)
- [ListServiceAccounts](#access_listserviceaccounts)
- [ListUsers](#access_listusers)

## Cluster

- [CreateOrUpdate](#cluster_createorupdate)
- [Delete](#cluster_delete)

## Connector

- [CreateOrUpdate](#connector_createorupdate)
- [Delete](#connector_delete)
- [Get](#connector_get)
- [List](#connector_list)

## Environment

- [CreateOrUpdate](#environment_createorupdate)
- [Delete](#environment_delete)

## MarketplaceAgreements

- [Create](#marketplaceagreements_create)
- [List](#marketplaceagreements_list)

## Organization

- [Create](#organization_create)
- [CreateApiKey](#organization_createapikey)
- [Delete](#organization_delete)
- [DeleteClusterApiKey](#organization_deleteclusterapikey)
- [GetByResourceGroup](#organization_getbyresourcegroup)
- [GetClusterApiKey](#organization_getclusterapikey)
- [GetClusterById](#organization_getclusterbyid)
- [GetEnvironmentById](#organization_getenvironmentbyid)
- [GetSchemaRegistryClusterById](#organization_getschemaregistryclusterbyid)
- [List](#organization_list)
- [ListByResourceGroup](#organization_listbyresourcegroup)
- [ListClusters](#organization_listclusters)
- [ListEnvironments](#organization_listenvironments)
- [ListRegions](#organization_listregions)
- [ListSchemaRegistryClusters](#organization_listschemaregistryclusters)
- [Update](#organization_update)

## OrganizationOperations

- [List](#organizationoperations_list)

## Topics

- [Create](#topics_create)
- [Delete](#topics_delete)
- [Get](#topics_get)
- [List](#topics_list)

## Validations

- [ValidateOrganization](#validations_validateorganization)
- [ValidateOrganizationV2](#validations_validateorganizationv2)
### Access_CreateRoleBinding

```java
import com.azure.resourcemanager.confluent.models.AccessCreateRoleBindingRequestModel;

/**
 * Samples for Access CreateRoleBinding.
 */
public final class AccessCreateRoleBindingSamples {
    /*
     * x-ms-original-file: 2024-07-01/Access_CreateRoleBinding.json
     */
    /**
     * Sample code: Access_CreateRoleBinding.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void accessCreateRoleBinding(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.access()
            .createRoleBindingWithResponse("myResourceGroup", "myOrganization",
                new AccessCreateRoleBindingRequestModel().withPrincipal("User:u-111aaa")
                    .withRoleName("CloudClusterAdmin")
                    .withCrnPattern(
                        "crn://confluent.cloud/organization=1111aaaa-11aa-11aa-11aa-111111aaaaaa/environment=env-aaa1111/cloud-cluster=lkc-1111aaa"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Access_DeleteRoleBinding

```java
/**
 * Samples for Access DeleteRoleBinding.
 */
public final class AccessDeleteRoleBindingSamples {
    /*
     * x-ms-original-file: 2024-07-01/Access_DeleteRoleBinding.json
     */
    /**
     * Sample code: Access_DeleteRoleBinding.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void accessDeleteRoleBinding(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.access()
            .deleteRoleBindingWithResponse("myResourceGroup", "myOrganization", "dlz-f3a90de",
                com.azure.core.util.Context.NONE);
    }
}
```

### Access_InviteUser

```java
import com.azure.resourcemanager.confluent.models.AccessInviteUserAccountModel;
import com.azure.resourcemanager.confluent.models.AccessInvitedUserDetails;

/**
 * Samples for Access InviteUser.
 */
public final class AccessInviteUserSamples {
    /*
     * x-ms-original-file: 2024-07-01/Access_InviteUser.json
     */
    /**
     * Sample code: Access_InviteUser.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void accessInviteUser(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.access()
            .inviteUserWithResponse("myResourceGroup", "myOrganization",
                new AccessInviteUserAccountModel()
                    .withInvitedUserDetails(new AccessInvitedUserDetails().withInvitedEmail("user2@onmicrosoft.com")
                        .withAuthType("AUTH_TYPE_SSO")),
                com.azure.core.util.Context.NONE);
    }
}
```

### Access_ListClusters

```java
import com.azure.resourcemanager.confluent.models.ListAccessRequestModel;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Access ListClusters.
 */
public final class AccessListClustersSamples {
    /*
     * x-ms-original-file: 2024-07-01/Access_ClusterList.json
     */
    /**
     * Sample code: Access_ClusterList.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void accessClusterList(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.access()
            .listClustersWithResponse("myResourceGroup", "myOrganization", new ListAccessRequestModel()
                .withSearchFilters(mapOf("pageSize", "10", "pageToken", "fakeTokenPlaceholder")),
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

### Access_ListEnvironments

```java
import com.azure.resourcemanager.confluent.models.ListAccessRequestModel;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Access ListEnvironments.
 */
public final class AccessListEnvironmentsSamples {
    /*
     * x-ms-original-file: 2024-07-01/Access_EnvironmentList.json
     */
    /**
     * Sample code: Access_EnvironmentList.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void accessEnvironmentList(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.access()
            .listEnvironmentsWithResponse("myResourceGroup", "myOrganization", new ListAccessRequestModel()
                .withSearchFilters(mapOf("pageSize", "10", "pageToken", "fakeTokenPlaceholder")),
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

### Access_ListInvitations

```java
import com.azure.resourcemanager.confluent.models.ListAccessRequestModel;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Access ListInvitations.
 */
public final class AccessListInvitationsSamples {
    /*
     * x-ms-original-file: 2024-07-01/Access_InvitationsList.json
     */
    /**
     * Sample code: Access_InvitationsList.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void accessInvitationsList(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.access()
            .listInvitationsWithResponse("myResourceGroup", "myOrganization",
                new ListAccessRequestModel().withSearchFilters(
                    mapOf("pageSize", "10", "pageToken", "fakeTokenPlaceholder", "status", "INVITE_STATUS_SENT")),
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

### Access_ListRoleBindingNameList

```java
import com.azure.resourcemanager.confluent.models.ListAccessRequestModel;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Access ListRoleBindingNameList.
 */
public final class AccessListRoleBindingNameListSamples {
    /*
     * x-ms-original-file: 2024-07-01/Access_RoleBindingNameList.json
     */
    /**
     * Sample code: Access_RoleBindingNameList.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void accessRoleBindingNameList(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.access()
            .listRoleBindingNameListWithResponse("myResourceGroup", "myOrganization",
                new ListAccessRequestModel().withSearchFilters(mapOf("crn_pattern",
                    "crn://confluent.cloud/organization=1aa7de07-298e-479c-8f2f-16ac91fd8e76", "namespace",
                    "public,dataplane,networking,identity,datagovernance,connect,streamcatalog,pipelines,ksql")),
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

### Access_ListRoleBindings

```java
import com.azure.resourcemanager.confluent.models.ListAccessRequestModel;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Access ListRoleBindings.
 */
public final class AccessListRoleBindingsSamples {
    /*
     * x-ms-original-file: 2024-07-01/Access_RoleBindingList.json
     */
    /**
     * Sample code: Access_RoleBindingList.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void accessRoleBindingList(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.access()
            .listRoleBindingsWithResponse("myResourceGroup", "myOrganization", new ListAccessRequestModel()
                .withSearchFilters(mapOf("pageSize", "10", "pageToken", "fakeTokenPlaceholder")),
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

### Access_ListServiceAccounts

```java
import com.azure.resourcemanager.confluent.models.ListAccessRequestModel;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Access ListServiceAccounts.
 */
public final class AccessListServiceAccountsSamples {
    /*
     * x-ms-original-file: 2024-07-01/Access_ServiceAccountsList.json
     */
    /**
     * Sample code: Access_ServiceAccountsList.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void accessServiceAccountsList(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.access()
            .listServiceAccountsWithResponse("myResourceGroup", "myOrganization", new ListAccessRequestModel()
                .withSearchFilters(mapOf("pageSize", "10", "pageToken", "fakeTokenPlaceholder")),
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

### Access_ListUsers

```java
import com.azure.resourcemanager.confluent.models.ListAccessRequestModel;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Access ListUsers.
 */
public final class AccessListUsersSamples {
    /*
     * x-ms-original-file: 2024-07-01/Access_UsersList.json
     */
    /**
     * Sample code: Access_UsersList.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void accessUsersList(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.access()
            .listUsersWithResponse("myResourceGroup", "myOrganization", new ListAccessRequestModel().withSearchFilters(
                mapOf("pageSize", "10", "pageToken", "fakeTokenPlaceholder")), com.azure.core.util.Context.NONE);
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

### Cluster_CreateOrUpdate

```java
import com.azure.resourcemanager.confluent.models.Package;
import com.azure.resourcemanager.confluent.models.SCClusterNetworkEnvironmentEntity;
import com.azure.resourcemanager.confluent.models.SCClusterSpecEntity;

/**
 * Samples for Cluster CreateOrUpdate.
 */
public final class ClusterCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-07-01/Cluster_Create.json
     */
    /**
     * Sample code: Cluster_CreateOrUpdate.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void clusterCreateOrUpdate(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.clusters()
            .define("cluster-1")
            .withExistingEnvironment("myResourceGroup", "myOrganization", "env-1")
            .withSpec(new SCClusterSpecEntity().withPackageProperty(Package.ESSENTIALS)
                .withRegion("us-east4")
                .withEnvironment(new SCClusterNetworkEnvironmentEntity().withId("env-1")))
            .create();
    }
}
```

### Cluster_Delete

```java
/**
 * Samples for Cluster Delete.
 */
public final class ClusterDeleteSamples {
    /*
     * x-ms-original-file: 2024-07-01/Cluster_Delete.json
     */
    /**
     * Sample code: Cluster_Delete.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void clusterDelete(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.clusters()
            .delete("myResourceGroup", "myOrganization", "env-12132", "dlz-f3a90de", com.azure.core.util.Context.NONE);
    }
}
```

### Connector_CreateOrUpdate

```java
import com.azure.resourcemanager.confluent.models.AuthType;
import com.azure.resourcemanager.confluent.models.AzureBlobStorageSinkConnectorServiceInfo;
import com.azure.resourcemanager.confluent.models.ConnectorClass;
import com.azure.resourcemanager.confluent.models.ConnectorInfoBase;
import com.azure.resourcemanager.confluent.models.ConnectorType;
import com.azure.resourcemanager.confluent.models.DataFormatType;
import com.azure.resourcemanager.confluent.models.KafkaAzureBlobStorageSinkConnectorInfo;
import java.util.Arrays;

/**
 * Samples for Connector CreateOrUpdate.
 */
public final class ConnectorCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-07-01/Organization_CreateConnectorByName.json
     */
    /**
     * Sample code: Connector_CreateOrUpdate.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void connectorCreateOrUpdate(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.connectors()
            .define("connector-1")
            .withExistingCluster("myResourceGroup", "myOrganization", "env-12132", "dlz-f3a90de")
            .withConnectorBasicInfo(new ConnectorInfoBase().withConnectorType(ConnectorType.SINK)
                .withConnectorClass(ConnectorClass.fromString("AZUREBLOBSTORAGESINK"))
                .withConnectorName("connector-1"))
            .withConnectorServiceTypeInfo(
                new AzureBlobStorageSinkConnectorServiceInfo().withStorageAccountName("stcfaccount-1")
                    .withStorageAccountKey("fakeTokenPlaceholder")
                    .withStorageContainerName("continer-1"))
            .withPartnerConnectorInfo(new KafkaAzureBlobStorageSinkConnectorInfo().withAuthType(AuthType.KAFKA_API_KEY)
                .withInputFormat(DataFormatType.JSON)
                .withOutputFormat(DataFormatType.JSON)
                .withApiKey("fakeTokenPlaceholder")
                .withApiSecret("fakeTokenPlaceholder")
                .withTopics(Arrays.asList("topic-1"))
                .withTopicsDir("topicsDir")
                .withFlushSize("1000")
                .withMaxTasks("2")
                .withTimeInterval("DAILY"))
            .create();
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
     * x-ms-original-file: 2024-07-01/Organization_DeleteConnectorByName.json
     */
    /**
     * Sample code: Connector_Delete.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void connectorDelete(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.connectors()
            .delete("myResourceGroup", "myOrganization", "env-12132", "dlz-f3a90de", "connector-1",
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2024-07-01/Organization_GetConnectorByName.json
     */
    /**
     * Sample code: Connector_Get.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void connectorGet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.connectors()
            .getWithResponse("myResourceGroup", "myOrganization", "env-12132", "dlz-f3a90de", "connector-1",
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
     * x-ms-original-file: 2024-07-01/Organization_ConnectorList.json
     */
    /**
     * Sample code: Connector_List.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void connectorList(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.connectors()
            .list("myResourceGroup", "myOrganization", "env-12132", "cluster-1", 10, null,
                com.azure.core.util.Context.NONE);
    }
}
```

### Environment_CreateOrUpdate

```java
import com.azure.resourcemanager.confluent.models.Package;
import com.azure.resourcemanager.confluent.models.StreamGovernanceConfig;

/**
 * Samples for Environment CreateOrUpdate.
 */
public final class EnvironmentCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-07-01/Environment_Create.json
     */
    /**
     * Sample code: Environment_CreateOrUpdate.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void environmentCreateOrUpdate(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.environments()
            .define("env-1")
            .withExistingOrganization("myResourceGroup", "myOrganization")
            .withStreamGovernanceConfig(new StreamGovernanceConfig().withPackageProperty(Package.ESSENTIALS))
            .create();
    }
}
```

### Environment_Delete

```java
/**
 * Samples for Environment Delete.
 */
public final class EnvironmentDeleteSamples {
    /*
     * x-ms-original-file: 2024-07-01/Environment_Delete.json
     */
    /**
     * Sample code: Environment_Delete.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void environmentDelete(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.environments()
            .delete("myResourceGroup", "myOrganization", "env-12132", com.azure.core.util.Context.NONE);
    }
}
```

### MarketplaceAgreements_Create

```java

/**
 * Samples for MarketplaceAgreements Create.
 */
public final class MarketplaceAgreementsCreateSamples {
    /*
     * x-ms-original-file: 2024-07-01/MarketplaceAgreements_Create.json
     */
    /**
     * Sample code: MarketplaceAgreements_Create.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void marketplaceAgreementsCreate(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.marketplaceAgreements().createWithResponse(null, com.azure.core.util.Context.NONE);
    }
}
```

### MarketplaceAgreements_List

```java
/**
 * Samples for MarketplaceAgreements List.
 */
public final class MarketplaceAgreementsListSamples {
    /*
     * x-ms-original-file: 2024-07-01/MarketplaceAgreements_List.json
     */
    /**
     * Sample code: MarketplaceAgreements_List.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void marketplaceAgreementsList(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.marketplaceAgreements().list(com.azure.core.util.Context.NONE);
    }
}
```

### Organization_Create

```java
import com.azure.resourcemanager.confluent.models.LinkOrganization;
import com.azure.resourcemanager.confluent.models.OfferDetail;
import com.azure.resourcemanager.confluent.models.UserDetail;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Organization Create.
 */
public final class OrganizationCreateSamples {
    /*
     * x-ms-original-file: 2024-07-01/Organization_Create.json
     */
    /**
     * Sample code: Organization_Create.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void organizationCreate(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations()
            .define("myOrganization")
            .withRegion("West US")
            .withExistingResourceGroup("myResourceGroup")
            .withOfferDetail(new OfferDetail().withPublisherId("string")
                .withId("string")
                .withPlanId("string")
                .withPlanName("string")
                .withTermUnit("string")
                .withPrivateOfferId("string")
                .withPrivateOfferIds(Arrays.asList("string")))
            .withUserDetail(new UserDetail().withFirstName("string")
                .withLastName("string")
                .withEmailAddress("contoso@microsoft.com")
                .withUserPrincipalName("contoso@microsoft.com")
                .withAadEmail("contoso@microsoft.com"))
            .withTags(mapOf("Environment", "Dev"))
            .withLinkOrganization(new LinkOrganization().withToken("fakeTokenPlaceholder"))
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

### Organization_CreateApiKey

```java
import com.azure.resourcemanager.confluent.models.CreateApiKeyModel;

/**
 * Samples for Organization CreateApiKey.
 */
public final class OrganizationCreateApiKeySamples {
    /*
     * x-ms-original-file: 2024-07-01/Organization_CreateClusterAPIKey.json
     */
    /**
     * Sample code: Organization_CreateClusterAPIKey.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void organizationCreateClusterAPIKey(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations()
            .createApiKeyWithResponse("myResourceGroup", "myOrganization", "env-12132", "clusterId-123",
                new CreateApiKeyModel().withName("CI kafka access key")
                    .withDescription("This API key provides kafka access to cluster x"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Organization_Delete

```java
/**
 * Samples for Organization Delete.
 */
public final class OrganizationDeleteSamples {
    /*
     * x-ms-original-file: 2024-07-01/Organization_Delete.json
     */
    /**
     * Sample code: Confluent_Delete.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void confluentDelete(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations().delete("myResourceGroup", "myOrganization", com.azure.core.util.Context.NONE);
    }
}
```

### Organization_DeleteClusterApiKey

```java
/**
 * Samples for Organization DeleteClusterApiKey.
 */
public final class OrganizationDeleteClusterApiKeySamples {
    /*
     * x-ms-original-file: 2024-07-01/Organization_DeleteClusterAPIKey.json
     */
    /**
     * Sample code: Organization_DeleteClusterAPIKey.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void organizationDeleteClusterAPIKey(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations()
            .deleteClusterApiKeyWithResponse("myResourceGroup", "myOrganization", "ZFZ6SZZZWGYBEIFB",
                com.azure.core.util.Context.NONE);
    }
}
```

### Organization_GetByResourceGroup

```java
/**
 * Samples for Organization GetByResourceGroup.
 */
public final class OrganizationGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-07-01/Organization_Get.json
     */
    /**
     * Sample code: Organization_Get.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void organizationGet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations()
            .getByResourceGroupWithResponse("myResourceGroup", "myOrganization", com.azure.core.util.Context.NONE);
    }
}
```

### Organization_GetClusterApiKey

```java
/**
 * Samples for Organization GetClusterApiKey.
 */
public final class OrganizationGetClusterApiKeySamples {
    /*
     * x-ms-original-file: 2024-07-01/Organization_GetClusterAPIKey.json
     */
    /**
     * Sample code: Organization_GetClusterAPIKey.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void organizationGetClusterAPIKey(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations()
            .getClusterApiKeyWithResponse("myResourceGroup", "myOrganization", "apiKeyId-123",
                com.azure.core.util.Context.NONE);
    }
}
```

### Organization_GetClusterById

```java
/**
 * Samples for Organization GetClusterById.
 */
public final class OrganizationGetClusterByIdSamples {
    /*
     * x-ms-original-file: 2024-07-01/Organization_GetClusterById.json
     */
    /**
     * Sample code: Organization_GetClusterById.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void organizationGetClusterById(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations()
            .getClusterByIdWithResponse("myResourceGroup", "myOrganization", "env-12132", "ProdKafkaCluster",
                com.azure.core.util.Context.NONE);
    }
}
```

### Organization_GetEnvironmentById

```java
/**
 * Samples for Organization GetEnvironmentById.
 */
public final class OrganizationGetEnvironmentByIdSamples {
    /*
     * x-ms-original-file: 2024-07-01/Organization_GetEnvironmentById.json
     */
    /**
     * Sample code: Organization_GetEnvironmentById.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void organizationGetEnvironmentById(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations()
            .getEnvironmentByIdWithResponse("myResourceGroup", "myOrganization", "prod-finance01",
                com.azure.core.util.Context.NONE);
    }
}
```

### Organization_GetSchemaRegistryClusterById

```java
/**
 * Samples for Organization GetSchemaRegistryClusterById.
 */
public final class OrganizationGetSchemaRegistryClusterByIdSamples {
    /*
     * x-ms-original-file: 2024-07-01/Organization_GetSchemaRegistryClusterById.json
     */
    /**
     * Sample code: Organization_GetSchemaRegistryClusterById.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void
        organizationGetSchemaRegistryClusterById(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations()
            .getSchemaRegistryClusterByIdWithResponse("myResourceGroup", "myOrganization", "env-stgcczjp2j3",
                "lsrc-stgczkq22z", com.azure.core.util.Context.NONE);
    }
}
```

### Organization_List

```java
/**
 * Samples for Organization List.
 */
public final class OrganizationListSamples {
    /*
     * x-ms-original-file: 2024-07-01/Organization_ListBySubscription.json
     */
    /**
     * Sample code: Organization_ListBySubscription.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void organizationListBySubscription(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Organization_ListByResourceGroup

```java
/**
 * Samples for Organization ListByResourceGroup.
 */
public final class OrganizationListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-07-01/Organization_ListByResourceGroup.json
     */
    /**
     * Sample code: Organization_ListByResourceGroup.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void organizationListByResourceGroup(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations().listByResourceGroup("myResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### Organization_ListClusters

```java
/**
 * Samples for Organization ListClusters.
 */
public final class OrganizationListClustersSamples {
    /*
     * x-ms-original-file: 2024-07-01/Organization_ClusterList.json
     */
    /**
     * Sample code: Organization_ListClusters.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void organizationListClusters(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations()
            .listClusters("myResourceGroup", "myOrganization", "env-12132", 10, null, com.azure.core.util.Context.NONE);
    }
}
```

### Organization_ListEnvironments

```java
/**
 * Samples for Organization ListEnvironments.
 */
public final class OrganizationListEnvironmentsSamples {
    /*
     * x-ms-original-file: 2024-07-01/Organization_EnvironmentList.json
     */
    /**
     * Sample code: Organization_ListEnvironments.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void organizationListEnvironments(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations()
            .listEnvironments("myResourceGroup", "myOrganization", 10, null, com.azure.core.util.Context.NONE);
    }
}
```

### Organization_ListRegions

```java
import com.azure.resourcemanager.confluent.models.ListAccessRequestModel;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Organization ListRegions.
 */
public final class OrganizationListRegionsSamples {
    /*
     * x-ms-original-file: 2024-07-01/Organization_ListRegions.json
     */
    /**
     * Sample code: Organization_ListRegions.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void organizationListRegions(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations()
            .listRegionsWithResponse("myResourceGroup", "myOrganization",
                new ListAccessRequestModel()
                    .withSearchFilters(mapOf("cloud", "azure", "packages", "ADVANCED,ESSENTIALS", "region", "eastus")),
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

### Organization_ListSchemaRegistryClusters

```java
/**
 * Samples for Organization ListSchemaRegistryClusters.
 */
public final class OrganizationListSchemaRegistryClustersSamples {
    /*
     * x-ms-original-file: 2024-07-01/Organization_ListSchemaRegistryClusters.json
     */
    /**
     * Sample code: Organization_ListSchemaRegistryClusters.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void
        organizationListSchemaRegistryClusters(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations()
            .listSchemaRegistryClusters("myResourceGroup", "myOrganization", "env-stgcczjp2j3", null, null,
                com.azure.core.util.Context.NONE);
    }
}
```

### Organization_Update

```java
import com.azure.resourcemanager.confluent.models.OrganizationResource;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Organization Update.
 */
public final class OrganizationUpdateSamples {
    /*
     * x-ms-original-file: 2024-07-01/Organization_Update.json
     */
    /**
     * Sample code: Confluent_Update.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void confluentUpdate(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        OrganizationResource resource = manager.organizations()
            .getByResourceGroupWithResponse("myResourceGroup", "myOrganization", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("client", "dev-client", "env", "dev")).apply();
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

### OrganizationOperations_List

```java
/**
 * Samples for OrganizationOperations List.
 */
public final class OrganizationOperationsListSamples {
    /*
     * x-ms-original-file: 2024-07-01/OrganizationOperations_List.json
     */
    /**
     * Sample code: OrganizationOperations_List.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void organizationOperationsList(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizationOperations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Topics_Create

```java
import com.azure.resourcemanager.confluent.models.TopicsInputConfig;
import java.util.Arrays;

/**
 * Samples for Topics Create.
 */
public final class TopicsCreateSamples {
    /*
     * x-ms-original-file: 2024-07-01/Topics_Create.json
     */
    /**
     * Sample code: Topics_Create.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void topicsCreate(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.topics()
            .define("topic-1")
            .withExistingCluster("myResourceGroup", "myOrganization", "env-12132", "dlz-f3a90de")
            .withInputConfigs(Arrays.asList(new TopicsInputConfig().withName("cleanup.policy").withValue("compact"),
                new TopicsInputConfig().withName("retention.ms").withValue("86400000")))
            .withPartitionsCount("1")
            .withReplicationFactor("3")
            .create();
    }
}
```

### Topics_Delete

```java
/**
 * Samples for Topics Delete.
 */
public final class TopicsDeleteSamples {
    /*
     * x-ms-original-file: 2024-07-01/Topics_Delete.json
     */
    /**
     * Sample code: Topics_Delete.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void topicsDelete(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.topics()
            .delete("myResourceGroup", "myOrganization", "env-12132", "dlz-f3a90de", "topic-1",
                com.azure.core.util.Context.NONE);
    }
}
```

### Topics_Get

```java
/**
 * Samples for Topics Get.
 */
public final class TopicsGetSamples {
    /*
     * x-ms-original-file: 2024-07-01/Topics_Get.json
     */
    /**
     * Sample code: Topics_Get.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void topicsGet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.topics()
            .getWithResponse("myResourceGroup", "myOrganization", "env-12132", "dlz-f3a90de", "topic-1",
                com.azure.core.util.Context.NONE);
    }
}
```

### Topics_List

```java
/**
 * Samples for Topics List.
 */
public final class TopicsListSamples {
    /*
     * x-ms-original-file: 2024-07-01/Organization_TopicList.json
     */
    /**
     * Sample code: Organization_ListTopics.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void organizationListTopics(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.topics()
            .list("myResourceGroup", "myOrganization", "env-12132", "cluster-1", 10, null,
                com.azure.core.util.Context.NONE);
    }
}
```

### Validations_ValidateOrganization

```java
import com.azure.resourcemanager.confluent.fluent.models.OrganizationResourceInner;
import com.azure.resourcemanager.confluent.models.OfferDetail;
import com.azure.resourcemanager.confluent.models.UserDetail;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Validations ValidateOrganization.
 */
public final class ValidationsValidateOrganizationSamples {
    /*
     * x-ms-original-file: 2024-07-01/Validations_ValidateOrganizations.json
     */
    /**
     * Sample code: Validations_ValidateOrganizations.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void validationsValidateOrganizations(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.validations()
            .validateOrganizationWithResponse("myResourceGroup", "myOrganization",
                new OrganizationResourceInner().withLocation("West US")
                    .withTags(mapOf("Environment", "Dev"))
                    .withOfferDetail(new OfferDetail().withPublisherId("string")
                        .withId("string")
                        .withPlanId("string")
                        .withPlanName("string")
                        .withTermUnit("string")
                        .withPrivateOfferId("string")
                        .withPrivateOfferIds(Arrays.asList("string")))
                    .withUserDetail(new UserDetail().withFirstName("string")
                        .withLastName("string")
                        .withEmailAddress("abc@microsoft.com")
                        .withUserPrincipalName("abc@microsoft.com")
                        .withAadEmail("abc@microsoft.com")),
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

### Validations_ValidateOrganizationV2

```java
import com.azure.resourcemanager.confluent.fluent.models.OrganizationResourceInner;
import com.azure.resourcemanager.confluent.models.OfferDetail;
import com.azure.resourcemanager.confluent.models.UserDetail;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Validations ValidateOrganizationV2.
 */
public final class ValidationsValidateOrganizationV2Samples {
    /*
     * x-ms-original-file: 2024-07-01/Validations_ValidateOrganizationsV2.json
     */
    /**
     * Sample code: Validations_ValidateOrganizations.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void validationsValidateOrganizations(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.validations()
            .validateOrganizationV2WithResponse("myResourceGroup", "myOrganization",
                new OrganizationResourceInner().withLocation("West US")
                    .withTags(mapOf("Environment", "Dev"))
                    .withOfferDetail(new OfferDetail().withPublisherId("string")
                        .withId("string")
                        .withPlanId("string")
                        .withPlanName("string")
                        .withTermUnit("string")
                        .withPrivateOfferId("string")
                        .withPrivateOfferIds(Arrays.asList("string")))
                    .withUserDetail(new UserDetail().withFirstName("string")
                        .withLastName("string")
                        .withEmailAddress("abc@microsoft.com")
                        .withUserPrincipalName("abc@microsoft.com")
                        .withAadEmail("abc@microsoft.com")),
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


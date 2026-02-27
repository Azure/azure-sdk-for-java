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
     * x-ms-original-file: 2025-08-18-preview/Access_CreateRoleBinding_MinimumSet_Gen.json
     */
    /**
     * Sample code: Access_CreateRoleBinding_MinimumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void accessCreateRoleBindingMinimumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.access()
            .createRoleBindingWithResponse("rgconfluent", "gdzfl", new AccessCreateRoleBindingRequestModel(),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-08-18-preview/Access_CreateRoleBinding_MaximumSet_Gen.json
     */
    /**
     * Sample code: Access_CreateRoleBinding_MaximumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void accessCreateRoleBindingMaximumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.access()
            .createRoleBindingWithResponse("rgconfluent", "ablufxskoyvgtbngsfexfkdw",
                new AccessCreateRoleBindingRequestModel().withPrincipal("xzbkopaxz")
                    .withRoleName("dhqxbrapwgqnmpbrredgxa")
                    .withCrnPattern("iif"),
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
     * x-ms-original-file: 2025-08-18-preview/Access_DeleteRoleBinding_MaximumSet_Gen.json
     */
    /**
     * Sample code: Access_DeleteRoleBinding_MaximumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void accessDeleteRoleBindingMaximumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.access()
            .deleteRoleBindingWithResponse("rgconfluent", "aeqwsawfoagclmfwwaw", "ucuqvcuiwmoreczccknufbhrwyp",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-08-18-preview/Access_DeleteRoleBinding_MinimumSet_Gen.json
     */
    /**
     * Sample code: Access_DeleteRoleBinding_MinimumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void accessDeleteRoleBindingMinimumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.access()
            .deleteRoleBindingWithResponse("rgconfluent", "kxbwvfhsqesuaswozdiivwo", "dqlmrdp",
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
     * x-ms-original-file: 2025-08-18-preview/Access_InviteUser_MinimumSet_Gen.json
     */
    /**
     * Sample code: Access_InviteUser_MinimumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void accessInviteUserMinimumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.access()
            .inviteUserWithResponse("rgconfluent", "skqsedhorkejhhntdsiwroffkjld", new AccessInviteUserAccountModel(),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-08-18-preview/Access_InviteUser_MaximumSet_Gen.json
     */
    /**
     * Sample code: Access_InviteUser_MaximumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void accessInviteUserMaximumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.access()
            .inviteUserWithResponse("rgconfluent", "aqwpihgldcvqwq",
                new AccessInviteUserAccountModel().withOrganizationId("aojvtivybqtuwwulokimwyh")
                    .withEmail("jtborwwroz")
                    .withUpn("eyck")
                    .withInvitedUserDetails(
                        new AccessInvitedUserDetails().withInvitedEmail("ozfkzouvjbvndqpyoxqbwtpzeiip")
                            .withAuthType("yaokrbtlql")),
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
     * x-ms-original-file: 2025-08-18-preview/Access_ListClusters_MaximumSet_Gen.json
     */
    /**
     * Sample code: Access_ListClusters_MaximumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void accessListClustersMaximumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.access()
            .listClustersWithResponse("rgconfluent", "zfs",
                new ListAccessRequestModel().withSearchFilters(mapOf("key8083", "fakeTokenPlaceholder")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-08-18-preview/Access_ListClusters_MinimumSet_Gen.json
     */
    /**
     * Sample code: Access_ListClusters_MinimumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void accessListClustersMinimumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.access()
            .listClustersWithResponse("rgconfluent", "kfmxlzmfkz", new ListAccessRequestModel(),
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
     * x-ms-original-file: 2025-08-18-preview/Access_ListEnvironments_MaximumSet_Gen.json
     */
    /**
     * Sample code: Access_ListEnvironments_MaximumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void accessListEnvironmentsMaximumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.access()
            .listEnvironmentsWithResponse("rgconfluent", "rnbjtcdqddweb",
                new ListAccessRequestModel().withSearchFilters(mapOf("key8083", "fakeTokenPlaceholder")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-08-18-preview/Access_ListEnvironments_MinimumSet_Gen.json
     */
    /**
     * Sample code: Access_ListEnvironments_MinimumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void accessListEnvironmentsMinimumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.access()
            .listEnvironmentsWithResponse("rgconfluent", "mv", new ListAccessRequestModel(),
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
     * x-ms-original-file: 2025-08-18-preview/Access_ListInvitations_MaximumSet_Gen.json
     */
    /**
     * Sample code: Access_ListInvitations_MaximumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void accessListInvitationsMaximumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.access()
            .listInvitationsWithResponse("rgconfluent", "ltmhusxnwxyfnbgcvwktxqrlqabbre",
                new ListAccessRequestModel().withSearchFilters(mapOf("key8083", "fakeTokenPlaceholder")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-08-18-preview/Access_ListInvitations_MinimumSet_Gen.json
     */
    /**
     * Sample code: Access_ListInvitations_MinimumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void accessListInvitationsMinimumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.access()
            .listInvitationsWithResponse("rgconfluent", "edpxevovxieanzlscvflmmcuoracwh", new ListAccessRequestModel(),
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
     * x-ms-original-file: 2025-08-18-preview/Access_ListRoleBindingNameList_MinimumSet_Gen.json
     */
    /**
     * Sample code: Access_ListRoleBindingNameList_MinimumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void
        accessListRoleBindingNameListMinimumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.access()
            .listRoleBindingNameListWithResponse("rgconfluent", "nlxbyyyyrdwjzwrcwfjlg", new ListAccessRequestModel(),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-08-18-preview/Access_ListRoleBindingNameList_MaximumSet_Gen.json
     */
    /**
     * Sample code: Access_ListRoleBindingNameList_MaximumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void
        accessListRoleBindingNameListMaximumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.access()
            .listRoleBindingNameListWithResponse("rgconfluent", "zgcfotubdmgowayipmpgujypv",
                new ListAccessRequestModel().withSearchFilters(mapOf("key8083", "fakeTokenPlaceholder")),
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
     * x-ms-original-file: 2025-08-18-preview/Access_ListRoleBindings_MinimumSet_Gen.json
     */
    /**
     * Sample code: Access_ListRoleBindings_MinimumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void accessListRoleBindingsMinimumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.access()
            .listRoleBindingsWithResponse("rgconfluent", "yuwchphweukvwtruurjgh", new ListAccessRequestModel(),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-08-18-preview/Access_ListRoleBindings_MaximumSet_Gen.json
     */
    /**
     * Sample code: Access_ListRoleBindings_MaximumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void accessListRoleBindingsMaximumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.access()
            .listRoleBindingsWithResponse("rgconfluent", "tefgundwswvwqcfryviyoulrrokl",
                new ListAccessRequestModel().withSearchFilters(mapOf("key8083", "fakeTokenPlaceholder")),
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
     * x-ms-original-file: 2025-08-18-preview/Access_ListServiceAccounts_MinimumSet_Gen.json
     */
    /**
     * Sample code: Access_ListServiceAccounts_MinimumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void
        accessListServiceAccountsMinimumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.access()
            .listServiceAccountsWithResponse("rgconfluent", "ambiyuv", new ListAccessRequestModel(),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-08-18-preview/Access_ListServiceAccounts_MaximumSet_Gen.json
     */
    /**
     * Sample code: Access_ListServiceAccounts_MaximumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void
        accessListServiceAccountsMaximumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.access()
            .listServiceAccountsWithResponse("rgconfluent", "go",
                new ListAccessRequestModel().withSearchFilters(mapOf("key8083", "fakeTokenPlaceholder")),
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
     * x-ms-original-file: 2025-08-18-preview/Access_ListUsers_MaximumSet_Gen.json
     */
    /**
     * Sample code: Access_ListUsers_MaximumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void accessListUsersMaximumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.access()
            .listUsersWithResponse("rgconfluent", "iggbjjnfqgutjxyvnlriqdm",
                new ListAccessRequestModel().withSearchFilters(mapOf("key8083", "fakeTokenPlaceholder")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-08-18-preview/Access_ListUsers_MinimumSet_Gen.json
     */
    /**
     * Sample code: Access_ListUsers_MinimumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void accessListUsersMinimumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.access()
            .listUsersWithResponse("rgconfluent", "elqetgujssclojggilbgl", new ListAccessRequestModel(),
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

### Cluster_CreateOrUpdate

```java
import com.azure.resourcemanager.confluent.models.ClusterConfigEntity;
import com.azure.resourcemanager.confluent.models.ClusterStatusEntity;
import com.azure.resourcemanager.confluent.models.Package;
import com.azure.resourcemanager.confluent.models.SCClusterByokEntity;
import com.azure.resourcemanager.confluent.models.SCClusterNetworkEnvironmentEntity;
import com.azure.resourcemanager.confluent.models.SCClusterSpecEntity;
import com.azure.resourcemanager.confluent.models.SCMetadataEntity;

/**
 * Samples for Cluster CreateOrUpdate.
 */
public final class ClusterCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-08-18-preview/Cluster_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: Cluster_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void clusterCreateOrUpdateMaximumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.clusters()
            .define("rdizpgcbewizsgffpg")
            .withExistingEnvironment("rgconfluent", "vwqtjoijzqitjmu", "rwmpydknnovcfsattscfm")
            .withKind("eroxushslwhufo")
            .withMetadata(new SCMetadataEntity().withSelf("bnbnbarlsvfifpzcnsnplf")
                .withResourceName("ciadqmxlpgllibvkz")
                .withCreatedTimestamp("ouqjivxfggaxzrsmxm")
                .withUpdatedTimestamp("ctrngbppcxdpzmp")
                .withDeletedTimestamp("gn"))
            .withSpec(new SCClusterSpecEntity().withName("cq")
                .withAvailability("mtt")
                .withCloud("zamxartuouxpglfbitjwhqy")
                .withZone("pqcxm")
                .withPackageProperty(Package.ESSENTIALS)
                .withRegion("gbodcnzmbifwyitnojrxali")
                .withKafkaBootstrapEndpoint("cnbkuhfnnqjb")
                .withHttpEndpoint("bircvfulzjdeobklsrbuxwr")
                .withApiEndpoint("axxhwauhucchb")
                .withConfig(new ClusterConfigEntity().withKind("hsruehsjppcnlxlsabwns"))
                .withEnvironment(new SCClusterNetworkEnvironmentEntity().withId("wbshmvpdhycxltclubn")
                    .withEnvironment("ern")
                    .withRelated("q")
                    .withResourceName("ewjrvururrahroszquhvhryqzmncp"))
                .withNetwork(new SCClusterNetworkEnvironmentEntity().withId("wbshmvpdhycxltclubn")
                    .withEnvironment("ern")
                    .withRelated("q")
                    .withResourceName("ewjrvururrahroszquhvhryqzmncp"))
                .withByok(new SCClusterByokEntity().withId("kfppxiwgcmp")
                    .withRelated("sfvjcdvrpzwwmplohiniuselqq")
                    .withResourceName("dvttcugicoklgyavt")))
            .withStatus(new ClusterStatusEntity().withPhase("qkpkryngvlvlostlvilptnfhpj").withCku(1))
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
     * x-ms-original-file: 2025-08-18-preview/Cluster_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: Cluster_Delete_MinimumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void clusterDeleteMinimumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.clusters()
            .delete("rgconfluent", "tvbhdezawspzzfprrnjoxfwtwlp", "mtmberahkmffekuuz", "nyfmkuwyeqhkgwehdjakbjheujj",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-08-18-preview/Cluster_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Cluster_Delete_MaximumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void clusterDeleteMaximumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.clusters()
            .delete("rgconfluent", "rwzpoelzgevhnkrvyqy", "gnijsroqxwwyyariafdnmkc", "zsvnfsirukovzkth",
                com.azure.core.util.Context.NONE);
    }
}
```

### Connector_CreateOrUpdate

```java
import com.azure.resourcemanager.confluent.models.ConnectorClass;
import com.azure.resourcemanager.confluent.models.ConnectorInfoBase;
import com.azure.resourcemanager.confluent.models.ConnectorServiceTypeInfoBase;
import com.azure.resourcemanager.confluent.models.ConnectorStatus;
import com.azure.resourcemanager.confluent.models.ConnectorType;
import com.azure.resourcemanager.confluent.models.PartnerInfoBase;

/**
 * Samples for Connector CreateOrUpdate.
 */
public final class ConnectorCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-08-18-preview/Connector_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: Connector_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void connectorCreateOrUpdateMaximumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.connectors()
            .define("fczksqy")
            .withExistingCluster("rgconfluent", "cppyvn", "tteibyyztawsguofmfn", "bfokzevhjixs")
            .withConnectorBasicInfo(new ConnectorInfoBase().withConnectorType(ConnectorType.SINK)
                .withConnectorClass(ConnectorClass.AZUREBLOBSOURCE)
                .withConnectorName("gxad")
                .withConnectorId("qlrrqyekgitbbes")
                .withConnectorState(ConnectorStatus.PROVISIONING))
            .withConnectorServiceTypeInfo(new ConnectorServiceTypeInfoBase())
            .withPartnerConnectorInfo(new PartnerInfoBase())
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
     * x-ms-original-file: 2025-08-18-preview/Connector_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: Connector_Delete_MinimumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void connectorDeleteMinimumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.connectors()
            .delete("rgconfluent", "frwocpndztguhgng", "duq", "chw", "suaugvwtvhexoqdrmxknvyiobq",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-08-18-preview/Connector_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Connector_Delete_MaximumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void connectorDeleteMaximumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.connectors()
            .delete("rgconfluent", "xqspbodq", "aabxehocioujmjjkgegijsmntw", "seivpzvrbyhjfmqb", "qznabwwh",
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
     * x-ms-original-file: 2025-08-18-preview/Connector_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Connector_Get_MaximumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void connectorGetMaximumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.connectors()
            .getWithResponse("rgconfluent", "pgwuoi", "rxbrvvdnplvbedrzwbgtwhbdm", "eknmpvbhtvwxdxddkos",
                "zakwjragxeiur", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2025-08-18-preview/Connector_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Connector_List_MaximumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void connectorListMaximumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.connectors()
            .list("rgconfluent", "ygxwgulsjztjoxuhmegodplubt", "mmxahiyh", "rslbzgqdgsnwzsqhlhethe", 18,
                "spklebovnebppxshqcmkyundbw", com.azure.core.util.Context.NONE);
    }
}
```

### Environment_CreateOrUpdate

```java
import com.azure.resourcemanager.confluent.models.Package;
import com.azure.resourcemanager.confluent.models.SCMetadataEntity;
import com.azure.resourcemanager.confluent.models.StreamGovernanceConfig;

/**
 * Samples for Environment CreateOrUpdate.
 */
public final class EnvironmentCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-08-18-preview/Environment_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: Environment_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void
        environmentCreateOrUpdateMaximumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.environments()
            .define("diycvbfypirqvomdkt")
            .withExistingOrganization("rgconfluent", "uf")
            .withKind("qhwbkvelujjbojvhrgiikildjdrqox")
            .withStreamGovernanceConfig(new StreamGovernanceConfig().withPackageProperty(Package.ESSENTIALS))
            .withMetadata(new SCMetadataEntity().withSelf("bnbnbarlsvfifpzcnsnplf")
                .withResourceName("ciadqmxlpgllibvkz")
                .withCreatedTimestamp("ouqjivxfggaxzrsmxm")
                .withUpdatedTimestamp("ctrngbppcxdpzmp")
                .withDeletedTimestamp("gn"))
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
     * x-ms-original-file: 2025-08-18-preview/Environment_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Environment_Delete_MaximumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void environmentDeleteMaximumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.environments()
            .delete("rgconfluent", "sowkvcymfiziohnofcudjyyaro", "lnmkjsylkxqqyrqmdaf",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-08-18-preview/Environment_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: Environment_Delete_MinimumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void environmentDeleteMinimumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.environments()
            .delete("rgconfluent", "yetpbmqrfbsanzjzkzdodlcygpj", "quuhiyvpfajfxrqcyxsb",
                com.azure.core.util.Context.NONE);
    }
}
```

### MarketplaceAgreements_Create

```java
import com.azure.resourcemanager.confluent.fluent.models.ConfluentAgreementResourceInner;
import java.time.OffsetDateTime;

/**
 * Samples for MarketplaceAgreements Create.
 */
public final class MarketplaceAgreementsCreateSamples {
    /*
     * x-ms-original-file: 2025-08-18-preview/MarketplaceAgreements_Create_MinimumSet_Gen.json
     */
    /**
     * Sample code: Create Confluent Marketplace agreement in the subscription. (MinimumSet).
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void createConfluentMarketplaceAgreementInTheSubscriptionMinimumSet(
        com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.marketplaceAgreements().createWithResponse(null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-08-18-preview/MarketplaceAgreements_Create_MaximumSet_Gen.json
     */
    /**
     * Sample code: Create Confluent Marketplace agreement in the subscription. (Maximumset).
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void createConfluentMarketplaceAgreementInTheSubscriptionMaximumset(
        com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.marketplaceAgreements()
            .createWithResponse(new ConfluentAgreementResourceInner().withPublisher("cxcrrfggvdmvcchohkyatlvbpyy")
                .withProduct("ogusipjbwihlwbfivdbjfuvoqwija")
                .withPlan("vgphlikczel")
                .withLicenseTextLink("ztckliskduxmcluia")
                .withPrivacyPolicyLink("wwvlrlfhzmvfjgimkhkqcaxn")
                .withRetrieveDatetime(OffsetDateTime.parse("2025-08-18T11:10:31.028Z"))
                .withSignature("cfdxpybzzsrgcdtebmqzzskxfiool")
                .withAccepted(true), com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2025-08-18-preview/MarketplaceAgreements_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: List Confluent marketplace agreements in the subscription. (Minimumset).
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void listConfluentMarketplaceAgreementsInTheSubscriptionMinimumset(
        com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.marketplaceAgreements().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-08-18-preview/MarketplaceAgreements_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: List Confluent marketplace agreements in the subscription. (Maximumset).
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void listConfluentMarketplaceAgreementsInTheSubscriptionMaximumset(
        com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.marketplaceAgreements().list(com.azure.core.util.Context.NONE);
    }
}
```

### Organization_Create

```java
import com.azure.resourcemanager.confluent.models.LinkOrganization;
import com.azure.resourcemanager.confluent.models.OfferDetail;
import com.azure.resourcemanager.confluent.models.SaaSOfferStatus;
import com.azure.resourcemanager.confluent.models.UserDetail;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Organization Create.
 */
public final class OrganizationCreateSamples {
    /*
     * x-ms-original-file: 2025-08-18-preview/Organization_Create_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organization_Create_MaximumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void organizationCreateMaximumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations()
            .define("qcqrbyx")
            .withRegion("ogifpylahax")
            .withExistingResourceGroup("rgconfluent")
            .withOfferDetail(new OfferDetail().withPublisherId("jvmchwpbqvavlgmuwquhqrnacgpvlobkkavwppwvhjfqcy")
                .withId("ufewkfngssvswmxfurnchnvgmnjuzzsoys")
                .withPlanId("l")
                .withPlanName(
                    "ycpeesrtyybhvmkdenugbkffjwistugfertrprgevcczlsnbcinotsdtsmealomyzsinypzimyyubepkuewirtcxhvxhsmwhwptvzuhirckvrgogahfwchvxnfkgfwqxqy")
                .withTermUnit("ipefrkgclpjaswyxpyjkppo")
                .withTermId("vujdve")
                .withPrivateOfferId(
                    "goshpcnjukfzfhubmynjxiulurrwplzcjpjstebtsiigbkovchcrlfmgoymqfuayhihnxruthwjywtedlcsqqsgaelqthvfzvafyjhsfzfjwotsiajpcogwrwylgcphxfhvvwemynoyovnvqcetftiofkthgdzfvybvhpviqlwlslaupndcxlvjssdap")
                .withPrivateOfferIds(Arrays.asList("nrbzkbcpvsakewlgubfmej"))
                .withStatus(SaaSOfferStatus.STARTED))
            .withUserDetail(new UserDetail().withFirstName("gqxqhtniapwvnsliaifhvmbtvvrciebktpeadanapfcqzflomz")
                .withLastName("vhdbyshxnnxivxbgzxscscdsvlwbsukqmcw")
                .withEmailAddress("user@example.com")
                .withUserPrincipalName("g")
                .withAadEmail("swugcwecfnkp"))
            .withTags(mapOf("key2047", "fakeTokenPlaceholder"))
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
     * x-ms-original-file: 2025-08-18-preview/Organization_CreateAPIKey_MinimumSet_Gen.json
     */
    /**
     * Sample code: Organization_CreateAPIKey_MinimumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void
        organizationCreateAPIKeyMinimumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations()
            .createApiKeyWithResponse("rgconfluent", "qbnpbkqxwtvjnytnconwynln", "un", "vuwuoryynnsuyfkicyejllc",
                new CreateApiKeyModel(), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-08-18-preview/Organization_CreateAPIKey_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organization_CreateAPIKey_MaximumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void
        organizationCreateAPIKeyMaximumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations()
            .createApiKeyWithResponse("rgconfluent", "pzvuoywx", "jqoxoahobqmhnklw", "ypyzlfhbml",
                new CreateApiKeyModel().withName("izlvofweryqgdgq").withDescription("vdxsmrddjlsfcsnwjezjraxgbkn"),
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
     * x-ms-original-file: 2025-08-18-preview/Organization_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: Organization_Delete_MinimumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void organizationDeleteMinimumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations().delete("rgconfluent", "w", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-08-18-preview/Organization_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organization_Delete_MaximumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void organizationDeleteMaximumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations().delete("rgconfluent", "zqp", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2025-08-18-preview/Organization_DeleteClusterAPIKey_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organization_DeleteClusterAPIKey_MaximumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void
        organizationDeleteClusterAPIKeyMaximumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations()
            .deleteClusterApiKeyWithResponse("rgconfluent", "y", "guahwdpdvzealjrnpgiqumxtbqq",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-08-18-preview/Organization_DeleteClusterAPIKey_MinimumSet_Gen.json
     */
    /**
     * Sample code: Organization_DeleteClusterAPIKey_MinimumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void
        organizationDeleteClusterAPIKeyMinimumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations()
            .deleteClusterApiKeyWithResponse("rgconfluent", "lokrfxecjwbnejqluwbwqcairu", "lqyopqadqide",
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
     * x-ms-original-file: 2025-08-18-preview/Organization_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organization_Get_MaximumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void organizationGetMaximumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations()
            .getByResourceGroupWithResponse("rgconfluent", "nnyqgkogkmwjubhfaynme", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2025-08-18-preview/Organization_GetClusterAPIKey_MinimumSet_Gen.json
     */
    /**
     * Sample code: Organization_GetClusterAPIKey_MinimumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void
        organizationGetClusterAPIKeyMinimumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations()
            .getClusterApiKeyWithResponse("rgconfluent", "oiywgdcgyrmdcquutyn", "gmgzzzwsoctmbdrgttw",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-08-18-preview/Organization_GetClusterAPIKey_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organization_GetClusterAPIKey_MaximumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void
        organizationGetClusterAPIKeyMaximumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations()
            .getClusterApiKeyWithResponse("rgconfluent", "puauqgrwsfgmolfhazfjcavnj", "xxsquwnsllkkzuyzlhdxdl",
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
     * x-ms-original-file: 2025-08-18-preview/Organization_GetClusterById_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organization_GetClusterById_MaximumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void
        organizationGetClusterByIdMaximumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations()
            .getClusterByIdWithResponse("rgconfluent", "qiasyqphlvkxxgyofmf", "xmkhyxmtjzez", "lirhyplbzq",
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
     * x-ms-original-file: 2025-08-18-preview/Organization_GetEnvironmentById_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organization_GetEnvironmentById_MaximumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void
        organizationGetEnvironmentByIdMaximumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations()
            .getEnvironmentByIdWithResponse("rgconfluent", "p", "kvifvjnmbilj", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2025-08-18-preview/Organization_GetSchemaRegistryClusterById_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organization_GetSchemaRegistryClusterById_MaximumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void organizationGetSchemaRegistryClusterByIdMaximumSet(
        com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations()
            .getSchemaRegistryClusterByIdWithResponse("rgconfluent", "hmhbrtw", "ztozszmpzhwevkpmaxslloijkicwt",
                "stfqijternpuzpleowkrbgzuutsgp", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-08-18-preview/Organization_GetSchemaRegistryClusterById_MinimumSet_Gen.json
     */
    /**
     * Sample code: Organization_GetSchemaRegistryClusterById_MinimumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void organizationGetSchemaRegistryClusterByIdMinimumSet(
        com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations()
            .getSchemaRegistryClusterByIdWithResponse("rgconfluent", "vcen", "zsbdbdljcfrnxxafcchr", "ivjcqxutsnlylxo",
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2025-08-18-preview/Organization_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organization_ListBySubscription_MaximumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void
        organizationListBySubscriptionMaximumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
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
     * x-ms-original-file: 2025-08-18-preview/Organization_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organization_ListByResourceGroup_MaximumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void
        organizationListByResourceGroupMaximumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations().listByResourceGroup("rgconfluent", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2025-08-18-preview/Organization_ListClusters_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organization_ListClusters_MaximumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void
        organizationListClustersMaximumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations()
            .listClusters("rgconfluent", "hpinjsodpkprhbvpzh", "qjeouprbl", 24, "esiyyipdkqikzcedkyrjnqvsbf",
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2025-08-18-preview/Organization_ListEnvironments_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organization_ListEnvironments_MaximumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void
        organizationListEnvironmentsMaximumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations()
            .listEnvironments("rgconfluent", "zgvcszgobzkrvomvhkabzamqincp", 21, "e", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2025-08-18-preview/Organization_ListRegions_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organization_ListRegions_MaximumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void organizationListRegionsMaximumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations()
            .listRegionsWithResponse("rgconfluent", "bnu",
                new ListAccessRequestModel().withSearchFilters(mapOf("key8083", "fakeTokenPlaceholder")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-08-18-preview/Organization_ListRegions_MinimumSet_Gen.json
     */
    /**
     * Sample code: Organization_ListRegions_MinimumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void organizationListRegionsMinimumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations()
            .listRegionsWithResponse("rgconfluent", "dvfvoveezvifybaptbuvprerr", new ListAccessRequestModel(),
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
     * x-ms-original-file: 2025-08-18-preview/Organization_ListSchemaRegistryClusters_MinimumSet_Gen.json
     */
    /**
     * Sample code: Organization_ListSchemaRegistryClusters_MinimumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void
        organizationListSchemaRegistryClustersMinimumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations()
            .listSchemaRegistryClusters("rgconfluent", "npek", "tdtxr", null, null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-08-18-preview/Organization_ListSchemaRegistryClusters_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organization_ListSchemaRegistryClusters_MaximumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void
        organizationListSchemaRegistryClustersMaximumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations()
            .listSchemaRegistryClusters("rgconfluent", "vkzifcygqhoewuixdmmg", "psxriyxxbjnctgeohah", 3,
                "npqeazvityguunrpgbumrqivvq", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2025-08-18-preview/Organization_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organization_Update_MaximumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void organizationUpdateMaximumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        OrganizationResource resource = manager.organizations()
            .getByResourceGroupWithResponse("rgconfluent", "nbpteobqdaoqi", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("key662", "fakeTokenPlaceholder")).apply();
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
     * x-ms-original-file: 2025-08-18-preview/OrganizationOperations_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: OrganizationOperations_List_MaximumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void
        organizationOperationsListMaximumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizationOperations().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-08-18-preview/OrganizationOperations_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: OrganizationOperations_List_MinimumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void
        organizationOperationsListMinimumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizationOperations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Topics_Create

```java
import com.azure.resourcemanager.confluent.models.TopicMetadataEntity;
import com.azure.resourcemanager.confluent.models.TopicsInputConfig;
import com.azure.resourcemanager.confluent.models.TopicsRelatedLink;
import java.util.Arrays;

/**
 * Samples for Topics Create.
 */
public final class TopicsCreateSamples {
    /*
     * x-ms-original-file: 2025-08-18-preview/Topics_Create_MaximumSet_Gen.json
     */
    /**
     * Sample code: Topics_Create_MaximumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void topicsCreateMaximumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.topics()
            .define("zlqnddejetmtrl")
            .withExistingCluster("rgconfluent", "pejjxxaztwoiqnzxsjqreefyuwriny", "kgozj", "bvhtsxflbnakmigqocm")
            .withKind("olpxpglrwgzffeibtxqbzqn")
            .withTopicId("pughhn")
            .withMetadata(new TopicMetadataEntity().withSelf("jvriqck").withResourceName("jdscdybqkdiknhnyjb"))
            .withPartitions(new TopicsRelatedLink().withRelated("bgeg"))
            .withConfigs(new TopicsRelatedLink().withRelated("bgeg"))
            .withInputConfigs(Arrays.asList(new TopicsInputConfig().withName("pkjzhjsbugwmpqawh").withValue("j")))
            .withPartitionsReassignments(new TopicsRelatedLink().withRelated("bgeg"))
            .withPartitionsCount("fxcu")
            .withReplicationFactor("ftsyww")
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
     * x-ms-original-file: 2025-08-18-preview/Topics_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Topics_Delete_MaximumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void topicsDeleteMaximumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.topics()
            .delete("rgconfluent", "xxoxo", "ohwjl", "llmaybvui", "xnprfffvbjtsnneofwwlpwuzua",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-08-18-preview/Topics_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: Topics_Delete_MinimumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void topicsDeleteMinimumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.topics()
            .delete("rgconfluent", "dmkqbkbzegenjirw", "flqluwoymahhtfjmx", "xrqfldtrcxvbxxqwbbouosmvnckut", "uflu",
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
     * x-ms-original-file: 2025-08-18-preview/Topics_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Topics_Get_MaximumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void topicsGetMaximumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.topics()
            .getWithResponse("rgconfluent", "mwvtthpz", "gjcsgothfog", "cbgic", "bspwihoyrewjny",
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
     * x-ms-original-file: 2025-08-18-preview/Topics_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Topics_List_MaximumSet.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void topicsListMaximumSet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.topics()
            .list("rgconfluent", "zkei", "cvgvhjgrodfwwhxkm", "majpwlefqsjqpfezvkvd", 28, "nqtivttbasuwnkum",
                com.azure.core.util.Context.NONE);
    }
}
```

### Validations_ValidateOrganization

```java
import com.azure.resourcemanager.confluent.fluent.models.OrganizationResourceInner;
import com.azure.resourcemanager.confluent.models.LinkOrganization;
import com.azure.resourcemanager.confluent.models.OfferDetail;
import com.azure.resourcemanager.confluent.models.SaaSOfferStatus;
import com.azure.resourcemanager.confluent.models.UserDetail;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Validations ValidateOrganization.
 */
public final class ValidationsValidateOrganizationSamples {
    /*
     * x-ms-original-file: 2025-08-18-preview/Validations_ValidateOrganization_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organization Validate proxy resource.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void organizationValidateProxyResource(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.validations()
            .validateOrganizationWithResponse("rgconfluent", "bqmqthdyixbkhlgw", new OrganizationResourceInner()
                .withLocation("ogifpylahax")
                .withTags(mapOf("key2047", "fakeTokenPlaceholder"))
                .withOfferDetail(new OfferDetail().withPublisherId("jvmchwpbqvavlgmuwquhqrnacgpvlobkkavwppwvhjfqcy")
                    .withId("ufewkfngssvswmxfurnchnvgmnjuzzsoys")
                    .withPlanId("l")
                    .withPlanName(
                        "ycpeesrtyybhvmkdenugbkffjwistugfertrprgevcczlsnbcinotsdtsmealomyzsinypzimyyubepkuewirtcxhvxhsmwhwptvzuhirckvrgogahfwchvxnfkgfwqxqy")
                    .withTermUnit("ipefrkgclpjaswyxpyjkppo")
                    .withTermId("vujdve")
                    .withPrivateOfferId(
                        "goshpcnjukfzfhubmynjxiulurrwplzcjpjstebtsiigbkovchcrlfmgoymqfuayhihnxruthwjywtedlcsqqsgaelqthvfzvafyjhsfzfjwotsiajpcogwrwylgcphxfhvvwemynoyovnvqcetftiofkthgdzfvybvhpviqlwlslaupndcxlvjssdap")
                    .withPrivateOfferIds(Arrays.asList("nrbzkbcpvsakewlgubfmej"))
                    .withStatus(SaaSOfferStatus.STARTED))
                .withUserDetail(new UserDetail().withFirstName("gqxqhtniapwvnsliaifhvmbtvvrciebktpeadanapfcqzflomz")
                    .withLastName("vhdbyshxnnxivxbgzxscscdsvlwbsukqmcw")
                    .withEmailAddress("user@example.com")
                    .withUserPrincipalName("g")
                    .withAadEmail("swugcwecfnkp"))
                .withLinkOrganization(new LinkOrganization().withToken("fakeTokenPlaceholder")),
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
import com.azure.resourcemanager.confluent.models.LinkOrganization;
import com.azure.resourcemanager.confluent.models.OfferDetail;
import com.azure.resourcemanager.confluent.models.SaaSOfferStatus;
import com.azure.resourcemanager.confluent.models.UserDetail;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Validations ValidateOrganizationV2.
 */
public final class ValidationsValidateOrganizationV2Samples {
    /*
     * x-ms-original-file: 2025-08-18-preview/Validations_ValidateOrganizationV2_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organization Validate proxy resource.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void organizationValidateProxyResource(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.validations()
            .validateOrganizationV2WithResponse("rgconfluent", "qhipfdfhxjzvwlergbvldnwhttfb",
                new OrganizationResourceInner().withLocation("ogifpylahax")
                    .withTags(mapOf("key2047", "fakeTokenPlaceholder"))
                    .withOfferDetail(new OfferDetail().withPublisherId("jvmchwpbqvavlgmuwquhqrnacgpvlobkkavwppwvhjfqcy")
                        .withId("ufewkfngssvswmxfurnchnvgmnjuzzsoys")
                        .withPlanId("l")
                        .withPlanName(
                            "ycpeesrtyybhvmkdenugbkffjwistugfertrprgevcczlsnbcinotsdtsmealomyzsinypzimyyubepkuewirtcxhvxhsmwhwptvzuhirckvrgogahfwchvxnfkgfwqxqy")
                        .withTermUnit("ipefrkgclpjaswyxpyjkppo")
                        .withTermId("vujdve")
                        .withPrivateOfferId(
                            "goshpcnjukfzfhubmynjxiulurrwplzcjpjstebtsiigbkovchcrlfmgoymqfuayhihnxruthwjywtedlcsqqsgaelqthvfzvafyjhsfzfjwotsiajpcogwrwylgcphxfhvvwemynoyovnvqcetftiofkthgdzfvybvhpviqlwlslaupndcxlvjssdap")
                        .withPrivateOfferIds(Arrays.asList("nrbzkbcpvsakewlgubfmej"))
                        .withStatus(SaaSOfferStatus.STARTED))
                    .withUserDetail(new UserDetail().withFirstName("gqxqhtniapwvnsliaifhvmbtvvrciebktpeadanapfcqzflomz")
                        .withLastName("vhdbyshxnnxivxbgzxscscdsvlwbsukqmcw")
                        .withEmailAddress("user@example.com")
                        .withUserPrincipalName("g")
                        .withAadEmail("swugcwecfnkp"))
                    .withLinkOrganization(new LinkOrganization().withToken("fakeTokenPlaceholder")),
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


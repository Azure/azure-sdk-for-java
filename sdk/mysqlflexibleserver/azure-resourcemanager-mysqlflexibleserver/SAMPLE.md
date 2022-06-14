# Code snippets and samples


## Advisors

- [Get](#advisors_get)
- [ListByServer](#advisors_listbyserver)

## CheckNameAvailability

- [Execute](#checknameavailability_execute)

## Configurations

- [CreateOrUpdate](#configurations_createorupdate)
- [Get](#configurations_get)
- [ListByServer](#configurations_listbyserver)

## Databases

- [CreateOrUpdate](#databases_createorupdate)
- [Delete](#databases_delete)
- [Get](#databases_get)
- [ListByServer](#databases_listbyserver)

## FirewallRules

- [CreateOrUpdate](#firewallrules_createorupdate)
- [Delete](#firewallrules_delete)
- [Get](#firewallrules_get)
- [ListByServer](#firewallrules_listbyserver)

## LocationBasedPerformanceTier

- [List](#locationbasedperformancetier_list)

## LocationBasedRecommendedActionSessionsOperationStatus

- [Get](#locationbasedrecommendedactionsessionsoperationstatus_get)

## LocationBasedRecommendedActionSessionsResult

- [List](#locationbasedrecommendedactionsessionsresult_list)

## LogFiles

- [ListByServer](#logfiles_listbyserver)

## Operations

- [List](#operations_list)

## PrivateEndpointConnections

- [CreateOrUpdate](#privateendpointconnections_createorupdate)
- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [ListByServer](#privateendpointconnections_listbyserver)
- [UpdateTags](#privateendpointconnections_updatetags)

## PrivateLinkResources

- [Get](#privatelinkresources_get)
- [ListByServer](#privatelinkresources_listbyserver)

## QueryTexts

- [Get](#querytexts_get)
- [ListByServer](#querytexts_listbyserver)

## RecommendedActions

- [Get](#recommendedactions_get)
- [ListByServer](#recommendedactions_listbyserver)

## RecoverableServers

- [Get](#recoverableservers_get)

## Replicas

- [ListByServer](#replicas_listbyserver)

## ResourceProvider

- [CreateRecommendedActionSession](#resourceprovider_createrecommendedactionsession)
- [ResetQueryPerformanceInsightData](#resourceprovider_resetqueryperformanceinsightdata)

## ServerAdministrators

- [CreateOrUpdate](#serveradministrators_createorupdate)
- [Delete](#serveradministrators_delete)
- [Get](#serveradministrators_get)
- [List](#serveradministrators_list)

## ServerBasedPerformanceTier

- [List](#serverbasedperformancetier_list)

## ServerKeys

- [CreateOrUpdate](#serverkeys_createorupdate)
- [Delete](#serverkeys_delete)
- [Get](#serverkeys_get)
- [List](#serverkeys_list)

## ServerParameters

- [ListUpdateConfigurations](#serverparameters_listupdateconfigurations)

## ServerSecurityAlertPolicies

- [CreateOrUpdate](#serversecurityalertpolicies_createorupdate)
- [Get](#serversecurityalertpolicies_get)
- [ListByServer](#serversecurityalertpolicies_listbyserver)

## Servers

- [Create](#servers_create)
- [Delete](#servers_delete)
- [GetByResourceGroup](#servers_getbyresourcegroup)
- [List](#servers_list)
- [ListByResourceGroup](#servers_listbyresourcegroup)
- [Restart](#servers_restart)
- [Start](#servers_start)
- [Stop](#servers_stop)
- [Update](#servers_update)
- [Upgrade](#servers_upgrade)

## TopQueryStatistics

- [Get](#topquerystatistics_get)
- [ListByServer](#topquerystatistics_listbyserver)

## VirtualNetworkRules

- [CreateOrUpdate](#virtualnetworkrules_createorupdate)
- [Delete](#virtualnetworkrules_delete)
- [Get](#virtualnetworkrules_get)
- [ListByServer](#virtualnetworkrules_listbyserver)

## WaitStatistics

- [Get](#waitstatistics_get)
- [ListByServer](#waitstatistics_listbyserver)
### Advisors_Get

```java
import com.azure.core.util.Context;

/** Samples for Advisors Get. */
public final class AdvisorsGetSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2018-06-01/examples/AdvisorsGet.json
     */
    /**
     * Sample code: AdvisorsGet.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void advisorsGet(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.advisors().getWithResponse("testResourceGroupName", "testServerName", "Index", Context.NONE);
    }
}
```

### Advisors_ListByServer

```java
import com.azure.core.util.Context;

/** Samples for Advisors ListByServer. */
public final class AdvisorsListByServerSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2018-06-01/examples/AdvisorsListByServer.json
     */
    /**
     * Sample code: AdvisorsListByServer.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void advisorsListByServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.advisors().listByServer("testResourceGroupName", "testServerName", Context.NONE);
    }
}
```

### CheckNameAvailability_Execute

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mysqlflexibleserver.models.NameAvailabilityRequest;

/** Samples for CheckNameAvailability Execute. */
public final class CheckNameAvailabilityExecuteSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2017-12-01/examples/CheckNameAvailability.json
     */
    /**
     * Sample code: NameAvailability.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void nameAvailability(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager
            .checkNameAvailabilities()
            .executeWithResponse(
                new NameAvailabilityRequest().withName("name1").withType("Microsoft.DBforMySQL"), Context.NONE);
    }
}
```

### Configurations_CreateOrUpdate

```java
/** Samples for Configurations CreateOrUpdate. */
public final class ConfigurationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2017-12-01/examples/ConfigurationCreateOrUpdate.json
     */
    /**
     * Sample code: ConfigurationCreateOrUpdate.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void configurationCreateOrUpdate(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager
            .configurations()
            .define("event_scheduler")
            .withExistingServer("TestGroup", "testserver")
            .withValue("off")
            .withSource("user-override")
            .create();
    }
}
```

### Configurations_Get

```java
import com.azure.core.util.Context;

/** Samples for Configurations Get. */
public final class ConfigurationsGetSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2017-12-01/examples/ConfigurationGet.json
     */
    /**
     * Sample code: ConfigurationGet.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void configurationGet(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.configurations().getWithResponse("TestGroup", "testserver", "event_scheduler", Context.NONE);
    }
}
```

### Configurations_ListByServer

```java
import com.azure.core.util.Context;

/** Samples for Configurations ListByServer. */
public final class ConfigurationsListByServerSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2017-12-01/examples/ConfigurationListByServer.json
     */
    /**
     * Sample code: ConfigurationList.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void configurationList(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.configurations().listByServer("testrg", "mysqltestsvc1", Context.NONE);
    }
}
```

### Databases_CreateOrUpdate

```java
/** Samples for Databases CreateOrUpdate. */
public final class DatabasesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2017-12-01/examples/DatabaseCreate.json
     */
    /**
     * Sample code: DatabaseCreate.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void databaseCreate(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager
            .databases()
            .define("db1")
            .withExistingServer("TestGroup", "testserver")
            .withCharset("utf8")
            .withCollation("utf8_general_ci")
            .create();
    }
}
```

### Databases_Delete

```java
import com.azure.core.util.Context;

/** Samples for Databases Delete. */
public final class DatabasesDeleteSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2017-12-01/examples/DatabaseDelete.json
     */
    /**
     * Sample code: DatabaseDelete.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void databaseDelete(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.databases().delete("TestGroup", "testserver", "db1", Context.NONE);
    }
}
```

### Databases_Get

```java
import com.azure.core.util.Context;

/** Samples for Databases Get. */
public final class DatabasesGetSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2017-12-01/examples/DatabaseGet.json
     */
    /**
     * Sample code: DatabaseGet.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void databaseGet(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.databases().getWithResponse("TestGroup", "testserver", "db1", Context.NONE);
    }
}
```

### Databases_ListByServer

```java
import com.azure.core.util.Context;

/** Samples for Databases ListByServer. */
public final class DatabasesListByServerSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2017-12-01/examples/DatabaseListByServer.json
     */
    /**
     * Sample code: DatabaseList.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void databaseList(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.databases().listByServer("TestGroup", "testserver", Context.NONE);
    }
}
```

### FirewallRules_CreateOrUpdate

```java
/** Samples for FirewallRules CreateOrUpdate. */
public final class FirewallRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2017-12-01/examples/FirewallRuleCreate.json
     */
    /**
     * Sample code: FirewallRuleCreate.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void firewallRuleCreate(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager
            .firewallRules()
            .define("rule1")
            .withExistingServer("TestGroup", "testserver")
            .withStartIpAddress("0.0.0.0")
            .withEndIpAddress("255.255.255.255")
            .create();
    }
}
```

### FirewallRules_Delete

```java
import com.azure.core.util.Context;

/** Samples for FirewallRules Delete. */
public final class FirewallRulesDeleteSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2017-12-01/examples/FirewallRuleDelete.json
     */
    /**
     * Sample code: FirewallRuleDelete.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void firewallRuleDelete(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.firewallRules().delete("TestGroup", "testserver", "rule1", Context.NONE);
    }
}
```

### FirewallRules_Get

```java
import com.azure.core.util.Context;

/** Samples for FirewallRules Get. */
public final class FirewallRulesGetSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2017-12-01/examples/FirewallRuleGet.json
     */
    /**
     * Sample code: FirewallRuleGet.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void firewallRuleGet(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.firewallRules().getWithResponse("TestGroup", "testserver", "rule1", Context.NONE);
    }
}
```

### FirewallRules_ListByServer

```java
import com.azure.core.util.Context;

/** Samples for FirewallRules ListByServer. */
public final class FirewallRulesListByServerSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2017-12-01/examples/FirewallRuleListByServer.json
     */
    /**
     * Sample code: FirewallRuleList.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void firewallRuleList(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.firewallRules().listByServer("TestGroup", "testserver", Context.NONE);
    }
}
```

### LocationBasedPerformanceTier_List

```java
import com.azure.core.util.Context;

/** Samples for LocationBasedPerformanceTier List. */
public final class LocationBasedPerformanceTierListSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2017-12-01/examples/PerformanceTiersListByLocation.json
     */
    /**
     * Sample code: PerformanceTiersList.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void performanceTiersList(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.locationBasedPerformanceTiers().list("WestUS", Context.NONE);
    }
}
```

### LocationBasedRecommendedActionSessionsOperationStatus_Get

```java
import com.azure.core.util.Context;

/** Samples for LocationBasedRecommendedActionSessionsOperationStatus Get. */
public final class LocationBasedRecommendedActionSessionsOperationStatusGetSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2018-06-01/examples/RecommendedActionSessionOperationStatus.json
     */
    /**
     * Sample code: RecommendedActionSessionOperationStatus.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void recommendedActionSessionOperationStatus(
        com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager
            .locationBasedRecommendedActionSessionsOperationStatus()
            .getWithResponse("WestUS", "aaaabbbb-cccc-dddd-0000-111122223333", Context.NONE);
    }
}
```

### LocationBasedRecommendedActionSessionsResult_List

```java
import com.azure.core.util.Context;

/** Samples for LocationBasedRecommendedActionSessionsResult List. */
public final class LocationBasedRecommendedActionSessionsResultListSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2018-06-01/examples/RecommendedActionSessionResult.json
     */
    /**
     * Sample code: RecommendedActionSessionResult.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void recommendedActionSessionResult(
        com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager
            .locationBasedRecommendedActionSessionsResults()
            .list("WestUS", "aaaabbbb-cccc-dddd-0000-111122223333", Context.NONE);
    }
}
```

### LogFiles_ListByServer

```java
import com.azure.core.util.Context;

/** Samples for LogFiles ListByServer. */
public final class LogFilesListByServerSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2017-12-01/examples/LogFileListByServer.json
     */
    /**
     * Sample code: LogFileList.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void logFileList(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.logFiles().listByServer("TestGroup", "testserver", Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2017-12-01/examples/OperationList.json
     */
    /**
     * Sample code: OperationList.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void operationList(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.operations().listWithResponse(Context.NONE);
    }
}
```

### PrivateEndpointConnections_CreateOrUpdate

```java
import com.azure.resourcemanager.mysqlflexibleserver.models.PrivateLinkServiceConnectionStateProperty;

/** Samples for PrivateEndpointConnections CreateOrUpdate. */
public final class PrivateEndpointConnectionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2018-06-01/examples/PrivateEndpointConnectionUpdate.json
     */
    /**
     * Sample code: Approve or reject a private endpoint connection with a given name.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void approveOrRejectAPrivateEndpointConnectionWithAGivenName(
        com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager
            .privateEndpointConnections()
            .define("private-endpoint-connection-name")
            .withExistingServer("Default", "test-svr")
            .withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionStateProperty()
                    .withStatus("Approved")
                    .withDescription("Approved by johndoe@contoso.com"))
            .create();
    }
}
```

### PrivateEndpointConnections_Delete

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections Delete. */
public final class PrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2018-06-01/examples/PrivateEndpointConnectionDelete.json
     */
    /**
     * Sample code: Deletes a private endpoint connection with a given name.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void deletesAPrivateEndpointConnectionWithAGivenName(
        com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager
            .privateEndpointConnections()
            .delete("Default", "test-svr", "private-endpoint-connection-name", Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections Get. */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2018-06-01/examples/PrivateEndpointConnectionGet.json
     */
    /**
     * Sample code: Gets private endpoint connection.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void getsPrivateEndpointConnection(
        com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager
            .privateEndpointConnections()
            .getWithResponse("Default", "test-svr", "private-endpoint-connection-name", Context.NONE);
    }
}
```

### PrivateEndpointConnections_ListByServer

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections ListByServer. */
public final class PrivateEndpointConnectionsListByServerSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2018-06-01/examples/PrivateEndpointConnectionList.json
     */
    /**
     * Sample code: Gets list of private endpoint connections on a server.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void getsListOfPrivateEndpointConnectionsOnAServer(
        com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.privateEndpointConnections().listByServer("Default", "test-svr", Context.NONE);
    }
}
```

### PrivateEndpointConnections_UpdateTags

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mysqlflexibleserver.models.PrivateEndpointConnection;
import java.util.HashMap;
import java.util.Map;

/** Samples for PrivateEndpointConnections UpdateTags. */
public final class PrivateEndpointConnectionsUpdateTagsSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2018-06-01/examples/PrivateEndpointConnectionUpdateTags.json
     */
    /**
     * Sample code: Update private endpoint connection Tags.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void updatePrivateEndpointConnectionTags(
        com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        PrivateEndpointConnection resource =
            manager
                .privateEndpointConnections()
                .getWithResponse("Default", "test-svr", "private-endpoint-connection-name", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("key1", "val1", "key2", "val2")).apply();
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

### PrivateLinkResources_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkResources Get. */
public final class PrivateLinkResourcesGetSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2018-06-01/examples/PrivateLinkResourcesGet.json
     */
    /**
     * Sample code: Gets a private link resource for MySQL.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void getsAPrivateLinkResourceForMySQL(
        com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.privateLinkResources().getWithResponse("Default", "test-svr", "plr", Context.NONE);
    }
}
```

### PrivateLinkResources_ListByServer

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkResources ListByServer. */
public final class PrivateLinkResourcesListByServerSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2018-06-01/examples/PrivateLinkResourcesList.json
     */
    /**
     * Sample code: Gets private link resources for MySQL.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void getsPrivateLinkResourcesForMySQL(
        com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.privateLinkResources().listByServer("Default", "test-svr", Context.NONE);
    }
}
```

### QueryTexts_Get

```java
import com.azure.core.util.Context;

/** Samples for QueryTexts Get. */
public final class QueryTextsGetSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2018-06-01/examples/QueryTextsGet.json
     */
    /**
     * Sample code: QueryTextsGet.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void queryTextsGet(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.queryTexts().getWithResponse("testResourceGroupName", "testServerName", "1", Context.NONE);
    }
}
```

### QueryTexts_ListByServer

```java
import com.azure.core.util.Context;
import java.util.Arrays;

/** Samples for QueryTexts ListByServer. */
public final class QueryTextsListByServerSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2018-06-01/examples/QueryTextsListByServer.json
     */
    /**
     * Sample code: QueryTextsListByServer.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void queryTextsListByServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager
            .queryTexts()
            .listByServer("testResourceGroupName", "testServerName", Arrays.asList("1", "2"), Context.NONE);
    }
}
```

### RecommendedActions_Get

```java
import com.azure.core.util.Context;

/** Samples for RecommendedActions Get. */
public final class RecommendedActionsGetSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2018-06-01/examples/RecommendedActionsGet.json
     */
    /**
     * Sample code: RecommendedActionsGet.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void recommendedActionsGet(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager
            .recommendedActions()
            .getWithResponse("testResourceGroupName", "testServerName", "Index", "Index-1", Context.NONE);
    }
}
```

### RecommendedActions_ListByServer

```java
import com.azure.core.util.Context;

/** Samples for RecommendedActions ListByServer. */
public final class RecommendedActionsListByServerSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2018-06-01/examples/RecommendedActionsListByServer.json
     */
    /**
     * Sample code: RecommendedActionsListByServer.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void recommendedActionsListByServer(
        com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager
            .recommendedActions()
            .listByServer("testResourceGroupName", "testServerName", "Index", null, Context.NONE);
    }
}
```

### RecoverableServers_Get

```java
import com.azure.core.util.Context;

/** Samples for RecoverableServers Get. */
public final class RecoverableServersGetSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2017-12-01/examples/RecoverableServersGet.json
     */
    /**
     * Sample code: ReplicasListByServer.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void replicasListByServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.recoverableServers().getWithResponse("testrg", "testsvc4", Context.NONE);
    }
}
```

### Replicas_ListByServer

```java
import com.azure.core.util.Context;

/** Samples for Replicas ListByServer. */
public final class ReplicasListByServerSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2017-12-01/examples/ReplicasListByServer.json
     */
    /**
     * Sample code: ReplicasListByServer.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void replicasListByServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.replicas().listByServer("TestGroup", "testmaster", Context.NONE);
    }
}
```

### ResourceProvider_CreateRecommendedActionSession

```java
import com.azure.core.util.Context;

/** Samples for ResourceProvider CreateRecommendedActionSession. */
public final class ResourceProviderCreateRecommendedActionSessionSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2018-06-01/examples/RecommendedActionSessionCreate.json
     */
    /**
     * Sample code: RecommendedActionSessionCreate.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void recommendedActionSessionCreate(
        com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager
            .resourceProviders()
            .createRecommendedActionSession(
                "testResourceGroupName", "testServerName", "Index", "someDatabaseName", Context.NONE);
    }
}
```

### ResourceProvider_ResetQueryPerformanceInsightData

```java
import com.azure.core.util.Context;

/** Samples for ResourceProvider ResetQueryPerformanceInsightData. */
public final class ResourceProviderResetQueryPerformanceInsightDataSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2018-06-01/examples/QueryPerformanceInsightResetData.json
     */
    /**
     * Sample code: QueryPerformanceInsightResetData.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void queryPerformanceInsightResetData(
        com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager
            .resourceProviders()
            .resetQueryPerformanceInsightDataWithResponse("testResourceGroupName", "testServerName", Context.NONE);
    }
}
```

### ServerAdministrators_CreateOrUpdate

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mysqlflexibleserver.fluent.models.ServerAdministratorResourceInner;
import com.azure.resourcemanager.mysqlflexibleserver.models.AdministratorType;
import java.util.UUID;

/** Samples for ServerAdministrators CreateOrUpdate. */
public final class ServerAdministratorsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2017-12-01/examples/ServerAdminCreateUpdate.json
     */
    /**
     * Sample code: ServerAdministratorCreate.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void serverAdministratorCreate(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager
            .serverAdministrators()
            .createOrUpdate(
                "testrg",
                "mysqltestsvc4",
                new ServerAdministratorResourceInner()
                    .withAdministratorType(AdministratorType.ACTIVE_DIRECTORY)
                    .withLogin("bob@contoso.com")
                    .withSid(UUID.fromString("c6b82b90-a647-49cb-8a62-0d2d3cb7ac7c"))
                    .withTenantId(UUID.fromString("c6b82b90-a647-49cb-8a62-0d2d3cb7ac7c")),
                Context.NONE);
    }
}
```

### ServerAdministrators_Delete

```java
import com.azure.core.util.Context;

/** Samples for ServerAdministrators Delete. */
public final class ServerAdministratorsDeleteSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2017-12-01/examples/ServerAdminDelete.json
     */
    /**
     * Sample code: ServerAdministratorsDelete.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void serverAdministratorsDelete(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.serverAdministrators().delete("testrg", "mysqltestsvc4", Context.NONE);
    }
}
```

### ServerAdministrators_Get

```java
import com.azure.core.util.Context;

/** Samples for ServerAdministrators Get. */
public final class ServerAdministratorsGetSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2017-12-01/examples/ServerAdminGet.json
     */
    /**
     * Sample code: ServerAdministratorGet.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void serverAdministratorGet(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.serverAdministrators().getWithResponse("testrg", "mysqltestsvc4", Context.NONE);
    }
}
```

### ServerAdministrators_List

```java
import com.azure.core.util.Context;

/** Samples for ServerAdministrators List. */
public final class ServerAdministratorsListSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2017-12-01/examples/ServerAdminList.json
     */
    /**
     * Sample code: get a list of server administrators.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void getAListOfServerAdministrators(
        com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.serverAdministrators().list("testrg", "mysqltestsvc4", Context.NONE);
    }
}
```

### ServerBasedPerformanceTier_List

```java
import com.azure.core.util.Context;

/** Samples for ServerBasedPerformanceTier List. */
public final class ServerBasedPerformanceTierListSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2017-12-01/examples/PerformanceTiersListByServer.json
     */
    /**
     * Sample code: PerformanceTiersList.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void performanceTiersList(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.serverBasedPerformanceTiers().list("testrg", "mysqltestsvc1", Context.NONE);
    }
}
```

### ServerKeys_CreateOrUpdate

```java
import com.azure.resourcemanager.mysqlflexibleserver.models.ServerKeyType;

/** Samples for ServerKeys CreateOrUpdate. */
public final class ServerKeysCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2020-01-01/examples/ServerKeyCreateOrUpdate.json
     */
    /**
     * Sample code: Creates or updates a MySQL Server key.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void createsOrUpdatesAMySQLServerKey(
        com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager
            .serverKeys()
            .define("someVault_someKey_01234567890123456789012345678901")
            .withExistingServer("testserver", "testrg")
            .withServerKeyType(ServerKeyType.AZURE_KEY_VAULT)
            .withUri("https://someVault.vault.azure.net/keys/someKey/01234567890123456789012345678901")
            .create();
    }
}
```

### ServerKeys_Delete

```java
import com.azure.core.util.Context;

/** Samples for ServerKeys Delete. */
public final class ServerKeysDeleteSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2020-01-01/examples/ServerKeyDelete.json
     */
    /**
     * Sample code: Delete the MySQL Server key.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void deleteTheMySQLServerKey(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager
            .serverKeys()
            .delete("testserver", "someVault_someKey_01234567890123456789012345678901", "testrg", Context.NONE);
    }
}
```

### ServerKeys_Get

```java
import com.azure.core.util.Context;

/** Samples for ServerKeys Get. */
public final class ServerKeysGetSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2020-01-01/examples/ServerKeyGet.json
     */
    /**
     * Sample code: Get the MySQL Server key.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void getTheMySQLServerKey(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager
            .serverKeys()
            .getWithResponse(
                "testrg", "testserver", "someVault_someKey_01234567890123456789012345678901", Context.NONE);
    }
}
```

### ServerKeys_List

```java
import com.azure.core.util.Context;

/** Samples for ServerKeys List. */
public final class ServerKeysListSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2020-01-01/examples/ServerKeyList.json
     */
    /**
     * Sample code: List the keys for a MySQL Server.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void listTheKeysForAMySQLServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.serverKeys().list("testrg", "testserver", Context.NONE);
    }
}
```

### ServerParameters_ListUpdateConfigurations

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mysqlflexibleserver.fluent.models.ConfigurationListResultInner;

/** Samples for ServerParameters ListUpdateConfigurations. */
public final class ServerParametersListUpdateConfigurationsSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2017-12-01/examples/ConfigurationsUpdateByServer.json
     */
    /**
     * Sample code: ConfigurationList.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void configurationList(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager
            .serverParameters()
            .listUpdateConfigurations("testrg", "mysqltestsvc1", new ConfigurationListResultInner(), Context.NONE);
    }
}
```

### ServerSecurityAlertPolicies_CreateOrUpdate

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mysqlflexibleserver.models.SecurityAlertPolicyName;
import com.azure.resourcemanager.mysqlflexibleserver.models.ServerSecurityAlertPolicy;
import com.azure.resourcemanager.mysqlflexibleserver.models.ServerSecurityAlertPolicyState;
import java.util.Arrays;

/** Samples for ServerSecurityAlertPolicies CreateOrUpdate. */
public final class ServerSecurityAlertPoliciesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2017-12-01/examples/ServerSecurityAlertsCreateMin.json
     */
    /**
     * Sample code: Update a server's threat detection policy with minimal parameters.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void updateAServerSThreatDetectionPolicyWithMinimalParameters(
        com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        ServerSecurityAlertPolicy resource =
            manager
                .serverSecurityAlertPolicies()
                .getWithResponse(
                    "securityalert-4799", "securityalert-6440", SecurityAlertPolicyName.DEFAULT, Context.NONE)
                .getValue();
        resource.update().withState(ServerSecurityAlertPolicyState.DISABLED).withEmailAccountAdmins(true).apply();
    }

    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2017-12-01/examples/ServerSecurityAlertsCreateMax.json
     */
    /**
     * Sample code: Update a server's threat detection policy with all parameters.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void updateAServerSThreatDetectionPolicyWithAllParameters(
        com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        ServerSecurityAlertPolicy resource =
            manager
                .serverSecurityAlertPolicies()
                .getWithResponse(
                    "securityalert-4799", "securityalert-6440", SecurityAlertPolicyName.DEFAULT, Context.NONE)
                .getValue();
        resource
            .update()
            .withState(ServerSecurityAlertPolicyState.ENABLED)
            .withDisabledAlerts(Arrays.asList("Access_Anomaly", "Usage_Anomaly"))
            .withEmailAddresses(Arrays.asList("testSecurityAlert@microsoft.com"))
            .withEmailAccountAdmins(true)
            .withStorageEndpoint("https://mystorage.blob.core.windows.net")
            .withStorageAccountAccessKey(
                "sdlfkjabc+sdlfkjsdlkfsjdfLDKFTERLKFDFKLjsdfksjdflsdkfD2342309432849328476458/3RSD==")
            .withRetentionDays(5)
            .apply();
    }
}
```

### ServerSecurityAlertPolicies_Get

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mysqlflexibleserver.models.SecurityAlertPolicyName;

/** Samples for ServerSecurityAlertPolicies Get. */
public final class ServerSecurityAlertPoliciesGetSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2017-12-01/examples/ServerSecurityAlertsGet.json
     */
    /**
     * Sample code: Get a server's threat detection policy.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void getAServerSThreatDetectionPolicy(
        com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager
            .serverSecurityAlertPolicies()
            .getWithResponse("securityalert-4799", "securityalert-6440", SecurityAlertPolicyName.DEFAULT, Context.NONE);
    }
}
```

### ServerSecurityAlertPolicies_ListByServer

```java
import com.azure.core.util.Context;

/** Samples for ServerSecurityAlertPolicies ListByServer. */
public final class ServerSecurityAlertPoliciesListByServerSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2017-12-01/examples/ServerSecurityAlertsListByServer.json
     */
    /**
     * Sample code: List the server's threat detection policies.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void listTheServerSThreatDetectionPolicies(
        com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.serverSecurityAlertPolicies().listByServer("securityalert-4799", "securityalert-6440", Context.NONE);
    }
}
```

### Servers_Create

```java
import com.azure.resourcemanager.mysqlflexibleserver.models.GeoRedundantBackup;
import com.azure.resourcemanager.mysqlflexibleserver.models.ServerPropertiesForDefaultCreate;
import com.azure.resourcemanager.mysqlflexibleserver.models.ServerPropertiesForGeoRestore;
import com.azure.resourcemanager.mysqlflexibleserver.models.ServerPropertiesForReplica;
import com.azure.resourcemanager.mysqlflexibleserver.models.ServerPropertiesForRestore;
import com.azure.resourcemanager.mysqlflexibleserver.models.Sku;
import com.azure.resourcemanager.mysqlflexibleserver.models.SkuTier;
import com.azure.resourcemanager.mysqlflexibleserver.models.SslEnforcementEnum;
import com.azure.resourcemanager.mysqlflexibleserver.models.StorageProfile;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/** Samples for Servers Create. */
public final class ServersCreateSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2017-12-01/examples/ServerCreateReplicaMode.json
     */
    /**
     * Sample code: Create a replica server.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void createAReplicaServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager
            .servers()
            .define("targetserver")
            .withRegion("westus")
            .withExistingResourceGroup("TargetResourceGroup")
            .withProperties(
                new ServerPropertiesForReplica()
                    .withSourceServerId(
                        "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/MasterResourceGroup/providers/Microsoft.DBforMySQL/servers/masterserver"))
            .create();
    }

    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2017-12-01/examples/ServerCreateGeoRestoreMode.json
     */
    /**
     * Sample code: Create a server as a geo restore.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void createAServerAsAGeoRestore(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager
            .servers()
            .define("targetserver")
            .withRegion("westus")
            .withExistingResourceGroup("TargetResourceGroup")
            .withProperties(
                new ServerPropertiesForGeoRestore()
                    .withSourceServerId(
                        "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/SourceResourceGroup/providers/Microsoft.DBforMySQL/servers/sourceserver"))
            .withTags(mapOf("ElasticServer", "1"))
            .withSku(
                new Sku().withName("GP_Gen5_2").withTier(SkuTier.GENERAL_PURPOSE).withCapacity(2).withFamily("Gen5"))
            .create();
    }

    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2017-12-01/examples/ServerCreate.json
     */
    /**
     * Sample code: Create a new server.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void createANewServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager
            .servers()
            .define("mysqltestsvc4")
            .withRegion("westus")
            .withExistingResourceGroup("testrg")
            .withProperties(
                new ServerPropertiesForDefaultCreate()
                    .withSslEnforcement(SslEnforcementEnum.ENABLED)
                    .withStorageProfile(
                        new StorageProfile()
                            .withBackupRetentionDays(7)
                            .withGeoRedundantBackup(GeoRedundantBackup.ENABLED)
                            .withStorageMB(128000))
                    .withAdministratorLogin("cloudsa")
                    .withAdministratorLoginPassword("<administratorLoginPassword>"))
            .withTags(mapOf("ElasticServer", "1"))
            .withSku(
                new Sku().withName("GP_Gen5_2").withTier(SkuTier.GENERAL_PURPOSE).withCapacity(2).withFamily("Gen5"))
            .create();
    }

    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2017-12-01/examples/ServerCreatePointInTimeRestore.json
     */
    /**
     * Sample code: Create a database as a point in time restore.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void createADatabaseAsAPointInTimeRestore(
        com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager
            .servers()
            .define("targetserver")
            .withRegion("brazilsouth")
            .withExistingResourceGroup("TargetResourceGroup")
            .withProperties(
                new ServerPropertiesForRestore()
                    .withSourceServerId(
                        "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/SourceResourceGroup/providers/Microsoft.DBforMySQL/servers/sourceserver")
                    .withRestorePointInTime(OffsetDateTime.parse("2017-12-14T00:00:37.467Z")))
            .withTags(mapOf("ElasticServer", "1"))
            .withSku(
                new Sku().withName("GP_Gen5_2").withTier(SkuTier.GENERAL_PURPOSE).withCapacity(2).withFamily("Gen5"))
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

### Servers_Delete

```java
import com.azure.core.util.Context;

/** Samples for Servers Delete. */
public final class ServersDeleteSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2017-12-01/examples/ServerDelete.json
     */
    /**
     * Sample code: ServerDelete.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void serverDelete(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.servers().delete("TestGroup", "testserver", Context.NONE);
    }
}
```

### Servers_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Servers GetByResourceGroup. */
public final class ServersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2017-12-01/examples/ServerGet.json
     */
    /**
     * Sample code: ServerGet.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void serverGet(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.servers().getByResourceGroupWithResponse("testrg", "mysqltestsvc4", Context.NONE);
    }
}
```

### Servers_List

```java
import com.azure.core.util.Context;

/** Samples for Servers List. */
public final class ServersListSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2017-12-01/examples/ServerList.json
     */
    /**
     * Sample code: ServerList.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void serverList(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.servers().list(Context.NONE);
    }
}
```

### Servers_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Servers ListByResourceGroup. */
public final class ServersListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2017-12-01/examples/ServerListByResourceGroup.json
     */
    /**
     * Sample code: ServerListByResourceGroup.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void serverListByResourceGroup(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.servers().listByResourceGroup("testrg", Context.NONE);
    }
}
```

### Servers_Restart

```java
import com.azure.core.util.Context;

/** Samples for Servers Restart. */
public final class ServersRestartSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2017-12-01/examples/ServerRestart.json
     */
    /**
     * Sample code: ServerRestart.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void serverRestart(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.servers().restart("TestGroup", "testserver", Context.NONE);
    }
}
```

### Servers_Start

```java
import com.azure.core.util.Context;

/** Samples for Servers Start. */
public final class ServersStartSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2020-01-01/examples/ServerStart.json
     */
    /**
     * Sample code: ServerStart.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void serverStart(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.servers().start("TestGroup", "testserver", Context.NONE);
    }
}
```

### Servers_Stop

```java
import com.azure.core.util.Context;

/** Samples for Servers Stop. */
public final class ServersStopSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2020-01-01/examples/ServerStop.json
     */
    /**
     * Sample code: ServerStop.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void serverStop(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.servers().stop("TestGroup", "testserver", Context.NONE);
    }
}
```

### Servers_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mysqlflexibleserver.models.Server;
import com.azure.resourcemanager.mysqlflexibleserver.models.SslEnforcementEnum;

/** Samples for Servers Update. */
public final class ServersUpdateSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2017-12-01/examples/ServerUpdate.json
     */
    /**
     * Sample code: ServerUpdate.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void serverUpdate(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        Server resource =
            manager.servers().getByResourceGroupWithResponse("testrg", "mysqltestsvc4", Context.NONE).getValue();
        resource
            .update()
            .withAdministratorLoginPassword("<administratorLoginPassword>")
            .withSslEnforcement(SslEnforcementEnum.DISABLED)
            .apply();
    }
}
```

### Servers_Upgrade

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mysqlflexibleserver.models.ServerUpgradeParameters;

/** Samples for Servers Upgrade. */
public final class ServersUpgradeSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2020-01-01/examples/ServerUpgrade.json
     */
    /**
     * Sample code: ServerUpgrade.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void serverUpgrade(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager
            .servers()
            .upgrade(
                "TestGroup", "testserver", new ServerUpgradeParameters().withTargetServerVersion("5.7"), Context.NONE);
    }
}
```

### TopQueryStatistics_Get

```java
import com.azure.core.util.Context;

/** Samples for TopQueryStatistics Get. */
public final class TopQueryStatisticsGetSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2018-06-01/examples/TopQueryStatisticsGet.json
     */
    /**
     * Sample code: TopQueryStatisticsGet.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void topQueryStatisticsGet(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager
            .topQueryStatistics()
            .getWithResponse(
                "testResourceGroupName",
                "testServerName",
                "66-636923268000000000-636923277000000000-avg-duration",
                Context.NONE);
    }
}
```

### TopQueryStatistics_ListByServer

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mysqlflexibleserver.models.TopQueryStatisticsInput;
import java.time.OffsetDateTime;

/** Samples for TopQueryStatistics ListByServer. */
public final class TopQueryStatisticsListByServerSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2018-06-01/examples/TopQueryStatisticsListByServer.json
     */
    /**
     * Sample code: TopQueryStatisticsListByServer.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void topQueryStatisticsListByServer(
        com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager
            .topQueryStatistics()
            .listByServer(
                "testResourceGroupName",
                "testServerName",
                new TopQueryStatisticsInput()
                    .withNumberOfTopQueries(5)
                    .withAggregationFunction("avg")
                    .withObservedMetric("duration")
                    .withObservationStartTime(OffsetDateTime.parse("2019-05-01T20:00:00.000Z"))
                    .withObservationEndTime(OffsetDateTime.parse("2019-05-07T20:00:00.000Z"))
                    .withAggregationWindow("PT15M"),
                Context.NONE);
    }
}
```

### VirtualNetworkRules_CreateOrUpdate

```java
/** Samples for VirtualNetworkRules CreateOrUpdate. */
public final class VirtualNetworkRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2017-12-01/examples/VirtualNetworkRulesCreateOrUpdate.json
     */
    /**
     * Sample code: Create or update a virtual network rule.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void createOrUpdateAVirtualNetworkRule(
        com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager
            .virtualNetworkRules()
            .define("vnet-firewall-rule")
            .withExistingServer("TestGroup", "vnet-test-svr")
            .withVirtualNetworkSubnetId(
                "/subscriptions/ffffffff-ffff-ffff-ffff-ffffffffffff/resourceGroups/TestGroup/providers/Microsoft.Network/virtualNetworks/testvnet/subnets/testsubnet")
            .withIgnoreMissingVnetServiceEndpoint(false)
            .create();
    }
}
```

### VirtualNetworkRules_Delete

```java
import com.azure.core.util.Context;

/** Samples for VirtualNetworkRules Delete. */
public final class VirtualNetworkRulesDeleteSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2017-12-01/examples/VirtualNetworkRulesDelete.json
     */
    /**
     * Sample code: Delete a virtual network rule.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void deleteAVirtualNetworkRule(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.virtualNetworkRules().delete("TestGroup", "vnet-test-svr", "vnet-firewall-rule", Context.NONE);
    }
}
```

### VirtualNetworkRules_Get

```java
import com.azure.core.util.Context;

/** Samples for VirtualNetworkRules Get. */
public final class VirtualNetworkRulesGetSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2017-12-01/examples/VirtualNetworkRulesGet.json
     */
    /**
     * Sample code: Gets a virtual network rule.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void getsAVirtualNetworkRule(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.virtualNetworkRules().getWithResponse("TestGroup", "vnet-test-svr", "vnet-firewall-rule", Context.NONE);
    }
}
```

### VirtualNetworkRules_ListByServer

```java
import com.azure.core.util.Context;

/** Samples for VirtualNetworkRules ListByServer. */
public final class VirtualNetworkRulesListByServerSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2017-12-01/examples/VirtualNetworkRulesList.json
     */
    /**
     * Sample code: List virtual network rules.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void listVirtualNetworkRules(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager.virtualNetworkRules().listByServer("TestGroup", "vnet-test-svr", Context.NONE);
    }
}
```

### WaitStatistics_Get

```java
import com.azure.core.util.Context;

/** Samples for WaitStatistics Get. */
public final class WaitStatisticsGetSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2018-06-01/examples/WaitStatisticsGet.json
     */
    /**
     * Sample code: WaitStatisticsGet.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void waitStatisticsGet(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager
            .waitStatistics()
            .getWithResponse(
                "testResourceGroupName",
                "testServerName",
                "636927606000000000-636927615000000000-send-wait/io/socket/sql/client_connection-2--0",
                Context.NONE);
    }
}
```

### WaitStatistics_ListByServer

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.mysqlflexibleserver.models.WaitStatisticsInput;
import java.time.OffsetDateTime;

/** Samples for WaitStatistics ListByServer. */
public final class WaitStatisticsListByServerSamples {
    /*
     * x-ms-original-file: specification/mysql/resource-manager/Microsoft.DBforMySQL/stable/2018-06-01/examples/WaitStatisticsListByServer.json
     */
    /**
     * Sample code: WaitStatisticsListByServer.
     *
     * @param manager Entry point to MySqlManager.
     */
    public static void waitStatisticsListByServer(com.azure.resourcemanager.mysqlflexibleserver.MySqlManager manager) {
        manager
            .waitStatistics()
            .listByServer(
                "testResourceGroupName",
                "testServerName",
                new WaitStatisticsInput()
                    .withObservationStartTime(OffsetDateTime.parse("2019-05-01T20:00:00.000Z"))
                    .withObservationEndTime(OffsetDateTime.parse("2019-05-07T20:00:00.000Z"))
                    .withAggregationWindow("PT15M"),
                Context.NONE);
    }
}
```


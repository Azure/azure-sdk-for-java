# Code snippets and samples


## AgentSpaces

- [CreateOrUpdate](#agentspaces_createorupdate)
- [Delete](#agentspaces_delete)
- [GetByResourceGroup](#agentspaces_getbyresourcegroup)
- [List](#agentspaces_list)
- [ListByResourceGroup](#agentspaces_listbyresourcegroup)
- [Update](#agentspaces_update)

## AgentSpacesConnectors

- [CreateOrUpdate](#agentspacesconnectors_createorupdate)
- [Delete](#agentspacesconnectors_delete)
- [Get](#agentspacesconnectors_get)
- [ListAllSecrets](#agentspacesconnectors_listallsecrets)
- [ListByAgentSpace](#agentspacesconnectors_listbyagentspace)
- [ListSecrets](#agentspacesconnectors_listsecrets)

## Agents

- [CreateOrUpdate](#agents_createorupdate)
- [Delete](#agents_delete)
- [GetByResourceGroup](#agents_getbyresourcegroup)
- [List](#agents_list)
- [ListByResourceGroup](#agents_listbyresourcegroup)
- [Start](#agents_start)
- [Stop](#agents_stop)
- [Update](#agents_update)

## AgentsConnectors

- [CreateOrUpdate](#agentsconnectors_createorupdate)
- [Delete](#agentsconnectors_delete)
- [Get](#agentsconnectors_get)
- [ListByAgent](#agentsconnectors_listbyagent)
- [ListSecrets](#agentsconnectors_listsecrets)
- [ListWithSecretsByAgent](#agentsconnectors_listwithsecretsbyagent)

## SupportedAgentModels

- [ListByLocation](#supportedagentmodels_listbylocation)
### AgentSpaces_CreateOrUpdate

```java
import com.azure.resourcemanager.appservice.sreagent.models.AgentSpacePolicies;
import com.azure.resourcemanager.appservice.sreagent.models.AgentSpaceProperties;
import com.azure.resourcemanager.appservice.sreagent.models.GenevaActionAuthenticationMode;
import com.azure.resourcemanager.appservice.sreagent.models.GenevaActionConfig;
import com.azure.resourcemanager.appservice.sreagent.models.GenevaActionParameter;
import com.azure.resourcemanager.appservice.sreagent.models.GenevaActionsPolicy;
import com.azure.resourcemanager.appservice.sreagent.models.ManagedServiceIdentity;
import com.azure.resourcemanager.appservice.sreagent.models.ManagedServiceIdentityType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for AgentSpaces CreateOrUpdate.
 */
public final class AgentSpacesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-01-01/AgentSpaces_CreateOrUpdate.json
     */
    /**
     * Sample code: AgentSpaces_CreateOrUpdate.
     * 
     * @param manager Entry point to AppServiceSreAgentManager.
     */
    public static void
        agentSpacesCreateOrUpdate(com.azure.resourcemanager.appservice.sreagent.AppServiceSreAgentManager manager) {
        manager.agentSpaces()
            .define("newAgentSpace")
            .withRegion("East US")
            .withExistingResourceGroup("examplerg")
            .withTags(mapOf("environment", "production", "team", "platform", "project", "aiAssistant"))
            .withProperties(
                new AgentSpaceProperties().withDescription("New production agent space for AI assistant services")
                    .withPolicies(
                        new AgentSpacePolicies()
                            .withGenevaActionsConfiguration(
                                new GenevaActionsPolicy().withAcisEndpoint("https://acis.eastus.monitoring.azure.com")
                                    .withClientId("12345678-1234-1234-1234-123456789012")
                                    .withCertificateSubjectName("CN=AgentSpaceAuth")
                                    .withAuthenticationMode(GenevaActionAuthenticationMode.OAUTH)
                                    .withExtensionName("GenevaActions")
                                    .withAllowedActions(Arrays.asList(
                                        new GenevaActionConfig()
                                            .withActionName("RestartService")
                                            .withExtension("GenevaActions")
                                            .withActionParameters(
                                                Arrays.asList(new GenevaActionParameter().withName("serviceName")
                                                    .withType("string")))
                                            .withApprovalRequired(true),
                                        new GenevaActionConfig().withActionName("GetMetrics")
                                            .withExtension("GenevaActions")
                                            .withActionParameters(Arrays.asList(
                                                new GenevaActionParameter().withName("metricName").withType("string"),
                                                new GenevaActionParameter().withName("timeRange").withType("string")))
                                            .withApprovalRequired(false)))))
                    .withMaxAgentCount(15)
                    .withServiceTreeId("abcdef12-3456-7890-abcd-ef1234567890"))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED))
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

### AgentSpaces_Delete

```java
/**
 * Samples for AgentSpaces Delete.
 */
public final class AgentSpacesDeleteSamples {
    /*
     * x-ms-original-file: 2026-01-01/AgentSpaces_Delete.json
     */
    /**
     * Sample code: AgentSpaces_Delete.
     * 
     * @param manager Entry point to AppServiceSreAgentManager.
     */
    public static void
        agentSpacesDelete(com.azure.resourcemanager.appservice.sreagent.AppServiceSreAgentManager manager) {
        manager.agentSpaces().delete("examplerg", "testAgentSpace", com.azure.core.util.Context.NONE);
    }
}
```

### AgentSpaces_GetByResourceGroup

```java
/**
 * Samples for AgentSpaces GetByResourceGroup.
 */
public final class AgentSpacesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-01-01/AgentSpaces_Get.json
     */
    /**
     * Sample code: AgentSpaces_Get.
     * 
     * @param manager Entry point to AppServiceSreAgentManager.
     */
    public static void agentSpacesGet(com.azure.resourcemanager.appservice.sreagent.AppServiceSreAgentManager manager) {
        manager.agentSpaces()
            .getByResourceGroupWithResponse("examplerg", "testAgentSpace", com.azure.core.util.Context.NONE);
    }
}
```

### AgentSpaces_List

```java
/**
 * Samples for AgentSpaces List.
 */
public final class AgentSpacesListSamples {
    /*
     * x-ms-original-file: 2026-01-01/AgentSpaces_ListBySubscription.json
     */
    /**
     * Sample code: AgentSpaces_ListBySubscription.
     * 
     * @param manager Entry point to AppServiceSreAgentManager.
     */
    public static void
        agentSpacesListBySubscription(com.azure.resourcemanager.appservice.sreagent.AppServiceSreAgentManager manager) {
        manager.agentSpaces().list(com.azure.core.util.Context.NONE);
    }
}
```

### AgentSpaces_ListByResourceGroup

```java
/**
 * Samples for AgentSpaces ListByResourceGroup.
 */
public final class AgentSpacesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-01-01/AgentSpaces_ListByResourceGroup.json
     */
    /**
     * Sample code: AgentSpaces_ListByResourceGroup.
     * 
     * @param manager Entry point to AppServiceSreAgentManager.
     */
    public static void agentSpacesListByResourceGroup(
        com.azure.resourcemanager.appservice.sreagent.AppServiceSreAgentManager manager) {
        manager.agentSpaces().listByResourceGroup("examplerg", com.azure.core.util.Context.NONE);
    }
}
```

### AgentSpaces_Update

```java
import com.azure.resourcemanager.appservice.sreagent.models.AgentSpace;
import com.azure.resourcemanager.appservice.sreagent.models.AgentSpacePatchProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for AgentSpaces Update.
 */
public final class AgentSpacesUpdateSamples {
    /*
     * x-ms-original-file: 2026-01-01/AgentSpaces_Update.json
     */
    /**
     * Sample code: AgentSpaces_Update.
     * 
     * @param manager Entry point to AppServiceSreAgentManager.
     */
    public static void
        agentSpacesUpdate(com.azure.resourcemanager.appservice.sreagent.AppServiceSreAgentManager manager) {
        AgentSpace resource = manager.agentSpaces()
            .getByResourceGroupWithResponse("examplerg", "testAgentSpace", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("environment", "staging", "version", "v2.0"))
            .withProperties(new AgentSpacePatchProperties().withMaxAgentCount(20))
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

### AgentSpacesConnectors_CreateOrUpdate

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.appservice.sreagent.models.AgentSpaceConnectorProperties;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for AgentSpacesConnectors CreateOrUpdate.
 */
public final class AgentSpacesConnectorsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-01-01/AgentSpacesConnectors_CreateOrUpdate.json
     */
    /**
     * Sample code: AgentSpacesConnectors_CreateOrUpdate.
     * 
     * @param manager Entry point to AppServiceSreAgentManager.
     */
    public static void agentSpacesConnectorsCreateOrUpdate(
        com.azure.resourcemanager.appservice.sreagent.AppServiceSreAgentManager manager) throws IOException {
        manager.agentSpacesConnectors()
            .define("new-shared-cosmosdb-connector")
            .withExistingAgentSpace("examplerg", "testAgentSpace")
            .withProperties(new AgentSpaceConnectorProperties().withEndpoint("https://newsharedkusto.kusto.windows.net")
                .withIdentity(
                    "/subscriptions/8efdecc5-919e-44eb-b179-915dca89ebf9/resourceGroups/examplerg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/sharedCosmosIdentity")
                .withExtendedProperties(mapOf("environment", "production", "owner", "alice", "additionalEndpoints",
                    SerializerFactory.createDefaultManagementSerializerAdapter()
                        .deserialize(
                            "[\"https://foo.kusto.windows.net/databasename\",\"https://bar.kusto.windows.net/databasename\"]",
                            Object.class, SerializerEncoding.JSON)))
                .withDataConnectorType("Kusto"))
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

### AgentSpacesConnectors_Delete

```java
/**
 * Samples for AgentSpacesConnectors Delete.
 */
public final class AgentSpacesConnectorsDeleteSamples {
    /*
     * x-ms-original-file: 2026-01-01/AgentSpacesConnectors_Delete.json
     */
    /**
     * Sample code: AgentSpacesConnectors_Delete.
     * 
     * @param manager Entry point to AppServiceSreAgentManager.
     */
    public static void
        agentSpacesConnectorsDelete(com.azure.resourcemanager.appservice.sreagent.AppServiceSreAgentManager manager) {
        manager.agentSpacesConnectors()
            .delete("examplerg", "testAgentSpace", "shared-sql-connector", com.azure.core.util.Context.NONE);
    }
}
```

### AgentSpacesConnectors_Get

```java
/**
 * Samples for AgentSpacesConnectors Get.
 */
public final class AgentSpacesConnectorsGetSamples {
    /*
     * x-ms-original-file: 2026-01-01/AgentSpacesConnectors_Get.json
     */
    /**
     * Sample code: AgentSpacesConnectors_Get.
     * 
     * @param manager Entry point to AppServiceSreAgentManager.
     */
    public static void
        agentSpacesConnectorsGet(com.azure.resourcemanager.appservice.sreagent.AppServiceSreAgentManager manager) {
        manager.agentSpacesConnectors()
            .getWithResponse("examplerg", "testAgentSpace", "shared-sql-connector", com.azure.core.util.Context.NONE);
    }
}
```

### AgentSpacesConnectors_ListAllSecrets

```java
/**
 * Samples for AgentSpacesConnectors ListAllSecrets.
 */
public final class AgentSpacesConnectorsListAllSecretsSamples {
    /*
     * x-ms-original-file: 2026-01-01/AgentSpacesConnectors_ListAllSecrets.json
     */
    /**
     * Sample code: AgentSpacesConnectors_ListAllSecrets.
     * 
     * @param manager Entry point to AppServiceSreAgentManager.
     */
    public static void agentSpacesConnectorsListAllSecrets(
        com.azure.resourcemanager.appservice.sreagent.AppServiceSreAgentManager manager) {
        manager.agentSpacesConnectors()
            .listAllSecretsWithResponse("examplerg", "testAgentSpace", com.azure.core.util.Context.NONE);
    }
}
```

### AgentSpacesConnectors_ListByAgentSpace

```java
/**
 * Samples for AgentSpacesConnectors ListByAgentSpace.
 */
public final class AgentSpacesConnectorsListByAgentSpaceSamples {
    /*
     * x-ms-original-file: 2026-01-01/AgentSpacesConnectors_ListByAgentSpace.json
     */
    /**
     * Sample code: AgentSpacesConnectors_ListByAgentSpace.
     * 
     * @param manager Entry point to AppServiceSreAgentManager.
     */
    public static void agentSpacesConnectorsListByAgentSpace(
        com.azure.resourcemanager.appservice.sreagent.AppServiceSreAgentManager manager) {
        manager.agentSpacesConnectors()
            .listByAgentSpace("examplerg", "testAgentSpace", com.azure.core.util.Context.NONE);
    }
}
```

### AgentSpacesConnectors_ListSecrets

```java
/**
 * Samples for AgentSpacesConnectors ListSecrets.
 */
public final class AgentSpacesConnectorsListSecretsSamples {
    /*
     * x-ms-original-file: 2026-01-01/AgentSpacesConnectors_ListSecrets.json
     */
    /**
     * Sample code: AgentSpacesConnectors_ListSecrets.
     * 
     * @param manager Entry point to AppServiceSreAgentManager.
     */
    public static void agentSpacesConnectorsListSecrets(
        com.azure.resourcemanager.appservice.sreagent.AppServiceSreAgentManager manager) {
        manager.agentSpacesConnectors()
            .listSecretsWithResponse("examplerg", "testAgentSpace", "shared-sql-connector",
                com.azure.core.util.Context.NONE);
    }
}
```

### Agents_CreateOrUpdate

```java
import com.azure.resourcemanager.appservice.sreagent.models.ActionConfiguration;
import com.azure.resourcemanager.appservice.sreagent.models.AgentAccessLevel;
import com.azure.resourcemanager.appservice.sreagent.models.AgentIdentity;
import com.azure.resourcemanager.appservice.sreagent.models.AgentMode;
import com.azure.resourcemanager.appservice.sreagent.models.AgentProperties;
import com.azure.resourcemanager.appservice.sreagent.models.ApplicationInsightsConfiguration;
import com.azure.resourcemanager.appservice.sreagent.models.DefaultModel;
import com.azure.resourcemanager.appservice.sreagent.models.KnowledgeGraphConfiguration;
import com.azure.resourcemanager.appservice.sreagent.models.LogConfiguration;
import com.azure.resourcemanager.appservice.sreagent.models.ManagedServiceIdentity;
import com.azure.resourcemanager.appservice.sreagent.models.ManagedServiceIdentityType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Agents CreateOrUpdate.
 */
public final class AgentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-01-01/Agents_CreateOrUpdate.json
     */
    /**
     * Sample code: Agents_CreateOrUpdate.
     * 
     * @param manager Entry point to AppServiceSreAgentManager.
     */
    public static void
        agentsCreateOrUpdate(com.azure.resourcemanager.appservice.sreagent.AppServiceSreAgentManager manager) {
        manager.agents()
            .define("testAgent")
            .withRegion("East US")
            .withExistingResourceGroup("examplerg")
            .withTags(mapOf("environment", "production", "team", "platform"))
            .withProperties(new AgentProperties().withAgentSpaceId(
                "/subscriptions/8efdecc5-919e-44eb-b179-915dca89ebf9/resourceGroups/examplerg/providers/Microsoft.App/agentSpaces/testAgentSpace")
                .withKnowledgeGraphConfiguration(new KnowledgeGraphConfiguration().withIdentity(
                    "/subscriptions/8efdecc5-919e-44eb-b179-915dca89ebf9/resourceGroups/examplerg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/testIdentity")
                    .withManagedResources(Arrays.asList(
                        "/subscriptions/8efdecc5-919e-44eb-b179-915dca89ebf9/resourceGroups/examplerg/providers/Microsoft.Storage/storageAccounts/teststorage")))
                .withActionConfiguration(new ActionConfiguration().withIdentity(
                    "/subscriptions/8efdecc5-919e-44eb-b179-915dca89ebf9/resourceGroups/examplerg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/actionIdentity")
                    .withMode(AgentMode.REVIEW)
                    .withAccessLevel(AgentAccessLevel.HIGH))
                .withLogConfiguration(new LogConfiguration().withApplicationInsightsConfiguration(
                    new ApplicationInsightsConfiguration().withAppId("87654321-4321-4321-4321-210987654321")
                        .withConnectionString(
                            "InstrumentationKey=87654321-4321-4321-4321-210987654321;IngestionEndpoint=https://eastus-0.in.applicationinsights.azure.com/")))
                .withAgentIdentity(
                    new AgentIdentity().withInitialSponsorGroupId("99999999-aaaa-bbbb-cccc-dddddddddddd"))
                .withDefaultModel(new DefaultModel().withProvider("MicrosoftFoundry").withName("gpt-5")))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED))
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

### Agents_Delete

```java
/**
 * Samples for Agents Delete.
 */
public final class AgentsDeleteSamples {
    /*
     * x-ms-original-file: 2026-01-01/Agents_Delete.json
     */
    /**
     * Sample code: Agents_Delete.
     * 
     * @param manager Entry point to AppServiceSreAgentManager.
     */
    public static void agentsDelete(com.azure.resourcemanager.appservice.sreagent.AppServiceSreAgentManager manager) {
        manager.agents().delete("examplerg", "testAgent", com.azure.core.util.Context.NONE);
    }
}
```

### Agents_GetByResourceGroup

```java
/**
 * Samples for Agents GetByResourceGroup.
 */
public final class AgentsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-01-01/Agents_Get.json
     */
    /**
     * Sample code: Agents_Get.
     * 
     * @param manager Entry point to AppServiceSreAgentManager.
     */
    public static void agentsGet(com.azure.resourcemanager.appservice.sreagent.AppServiceSreAgentManager manager) {
        manager.agents().getByResourceGroupWithResponse("examplerg", "testAgent", com.azure.core.util.Context.NONE);
    }
}
```

### Agents_List

```java
/**
 * Samples for Agents List.
 */
public final class AgentsListSamples {
    /*
     * x-ms-original-file: 2026-01-01/Agents_ListBySubscription.json
     */
    /**
     * Sample code: Agents_ListBySubscription.
     * 
     * @param manager Entry point to AppServiceSreAgentManager.
     */
    public static void
        agentsListBySubscription(com.azure.resourcemanager.appservice.sreagent.AppServiceSreAgentManager manager) {
        manager.agents().list(com.azure.core.util.Context.NONE);
    }
}
```

### Agents_ListByResourceGroup

```java
/**
 * Samples for Agents ListByResourceGroup.
 */
public final class AgentsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-01-01/Agents_ListByResourceGroup.json
     */
    /**
     * Sample code: Agents_ListByResourceGroup.
     * 
     * @param manager Entry point to AppServiceSreAgentManager.
     */
    public static void
        agentsListByResourceGroup(com.azure.resourcemanager.appservice.sreagent.AppServiceSreAgentManager manager) {
        manager.agents().listByResourceGroup("examplerg", com.azure.core.util.Context.NONE);
    }
}
```

### Agents_Start

```java
/**
 * Samples for Agents Start.
 */
public final class AgentsStartSamples {
    /*
     * x-ms-original-file: 2026-01-01/Agents_Start.json
     */
    /**
     * Sample code: Agents_Start.
     * 
     * @param manager Entry point to AppServiceSreAgentManager.
     */
    public static void agentsStart(com.azure.resourcemanager.appservice.sreagent.AppServiceSreAgentManager manager) {
        manager.agents().start("examplerg", "testAgent", com.azure.core.util.Context.NONE);
    }
}
```

### Agents_Stop

```java
/**
 * Samples for Agents Stop.
 */
public final class AgentsStopSamples {
    /*
     * x-ms-original-file: 2026-01-01/Agents_Stop.json
     */
    /**
     * Sample code: Agents_Stop.
     * 
     * @param manager Entry point to AppServiceSreAgentManager.
     */
    public static void agentsStop(com.azure.resourcemanager.appservice.sreagent.AppServiceSreAgentManager manager) {
        manager.agents().stop("examplerg", "testAgent", com.azure.core.util.Context.NONE);
    }
}
```

### Agents_Update

```java
import com.azure.resourcemanager.appservice.sreagent.models.Agent;
import com.azure.resourcemanager.appservice.sreagent.models.AgentPatchProperties;
import com.azure.resourcemanager.appservice.sreagent.models.UpgradeChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Agents Update.
 */
public final class AgentsUpdateSamples {
    /*
     * x-ms-original-file: 2026-01-01/Agents_Update.json
     */
    /**
     * Sample code: Agents_Update.
     * 
     * @param manager Entry point to AppServiceSreAgentManager.
     */
    public static void agentsUpdate(com.azure.resourcemanager.appservice.sreagent.AppServiceSreAgentManager manager) {
        Agent resource = manager.agents()
            .getByResourceGroupWithResponse("examplerg", "testAgent", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("environment", "production", "version", "v3.0"))
            .withProperties(new AgentPatchProperties().withUpgradeChannel(UpgradeChannel.STABLE))
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

### AgentsConnectors_CreateOrUpdate

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.appservice.sreagent.models.AgentConnectorProperties;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for AgentsConnectors CreateOrUpdate.
 */
public final class AgentsConnectorsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-01-01/AgentsConnectors_CreateOrUpdate.json
     */
    /**
     * Sample code: AgentsConnectors_CreateOrUpdate.
     * 
     * @param manager Entry point to AppServiceSreAgentManager.
     */
    public static void agentsConnectorsCreateOrUpdate(
        com.azure.resourcemanager.appservice.sreagent.AppServiceSreAgentManager manager) throws IOException {
        manager.agentsConnectors()
            .define("new-kusto-connector")
            .withExistingAgent("examplerg", "testAgent")
            .withProperties(new AgentConnectorProperties().withEndpoint("https://newcluster.eastus.kusto.windows.net")
                .withIdentity(
                    "/subscriptions/8efdecc5-919e-44eb-b179-915dca89ebf9/resourceGroups/examplerg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/kustoIdentity")
                .withExtendedProperties(mapOf("AuthType", "Custom", "CustomHeader",
                    SerializerFactory.createDefaultManagementSerializerAdapter()
                        .deserialize("{\"DD_API_KEY\":\"value 1\",\"DD_APPLICATION_KEY\":\"value 2\"}", Object.class,
                            SerializerEncoding.JSON)))
                .withDataConnectorType("Kusto"))
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

### AgentsConnectors_Delete

```java
/**
 * Samples for AgentsConnectors Delete.
 */
public final class AgentsConnectorsDeleteSamples {
    /*
     * x-ms-original-file: 2026-01-01/AgentsConnectors_Delete.json
     */
    /**
     * Sample code: AgentsConnectors_Delete.
     * 
     * @param manager Entry point to AppServiceSreAgentManager.
     */
    public static void
        agentsConnectorsDelete(com.azure.resourcemanager.appservice.sreagent.AppServiceSreAgentManager manager) {
        manager.agentsConnectors().delete("examplerg", "testAgent", "sql-connector", com.azure.core.util.Context.NONE);
    }
}
```

### AgentsConnectors_Get

```java
/**
 * Samples for AgentsConnectors Get.
 */
public final class AgentsConnectorsGetSamples {
    /*
     * x-ms-original-file: 2026-01-01/AgentsConnectors_Get.json
     */
    /**
     * Sample code: AgentsConnectors_Get.
     * 
     * @param manager Entry point to AppServiceSreAgentManager.
     */
    public static void
        agentsConnectorsGet(com.azure.resourcemanager.appservice.sreagent.AppServiceSreAgentManager manager) {
        manager.agentsConnectors()
            .getWithResponse("examplerg", "testAgent", "kusto-connector", com.azure.core.util.Context.NONE);
    }
}
```

### AgentsConnectors_ListByAgent

```java
/**
 * Samples for AgentsConnectors ListByAgent.
 */
public final class AgentsConnectorsListByAgentSamples {
    /*
     * x-ms-original-file: 2026-01-01/AgentsConnectors_ListByAgent.json
     */
    /**
     * Sample code: AgentsConnectors_ListByAgent.
     * 
     * @param manager Entry point to AppServiceSreAgentManager.
     */
    public static void
        agentsConnectorsListByAgent(com.azure.resourcemanager.appservice.sreagent.AppServiceSreAgentManager manager) {
        manager.agentsConnectors().listByAgent("examplerg", "testAgent", com.azure.core.util.Context.NONE);
    }
}
```

### AgentsConnectors_ListSecrets

```java
/**
 * Samples for AgentsConnectors ListSecrets.
 */
public final class AgentsConnectorsListSecretsSamples {
    /*
     * x-ms-original-file: 2026-01-01/AgentsConnectors_ListSecrets.json
     */
    /**
     * Sample code: AgentsConnectors_ListSecrets.
     * 
     * @param manager Entry point to AppServiceSreAgentManager.
     */
    public static void
        agentsConnectorsListSecrets(com.azure.resourcemanager.appservice.sreagent.AppServiceSreAgentManager manager) {
        manager.agentsConnectors()
            .listSecretsWithResponse("examplerg", "testAgent", "kusto-connector", com.azure.core.util.Context.NONE);
    }
}
```

### AgentsConnectors_ListWithSecretsByAgent

```java
/**
 * Samples for AgentsConnectors ListWithSecretsByAgent.
 */
public final class AgentsConnectorsListWithSecretsByAgentSamples {
    /*
     * x-ms-original-file: 2026-01-01/AgentsConnectors_ListWithSecretsByAgent.json
     */
    /**
     * Sample code: AgentsConnectors_ListWithSecretsByAgent.
     * 
     * @param manager Entry point to AppServiceSreAgentManager.
     */
    public static void agentsConnectorsListWithSecretsByAgent(
        com.azure.resourcemanager.appservice.sreagent.AppServiceSreAgentManager manager) {
        manager.agentsConnectors()
            .listWithSecretsByAgentWithResponse("examplerg", "testAgent", com.azure.core.util.Context.NONE);
    }
}
```

### SupportedAgentModels_ListByLocation

```java
/**
 * Samples for SupportedAgentModels ListByLocation.
 */
public final class SupportedAgentModelsListByLocationSamples {
    /*
     * x-ms-original-file: 2026-01-01/SupportedAgentModels_ListByLocation.json
     */
    /**
     * Sample code: SupportedAgentModels_ListByLocation.
     * 
     * @param manager Entry point to AppServiceSreAgentManager.
     */
    public static void supportedAgentModelsListByLocation(
        com.azure.resourcemanager.appservice.sreagent.AppServiceSreAgentManager manager) {
        manager.supportedAgentModels().listByLocation("eastus", com.azure.core.util.Context.NONE);
    }
}
```


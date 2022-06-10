# Code snippets and samples


## BotConnection

- [Create](#botconnection_create)
- [Delete](#botconnection_delete)
- [Get](#botconnection_get)
- [ListByBotService](#botconnection_listbybotservice)
- [ListServiceProviders](#botconnection_listserviceproviders)
- [ListWithSecrets](#botconnection_listwithsecrets)
- [Update](#botconnection_update)

## Bots

- [Create](#bots_create)
- [Delete](#bots_delete)
- [GetByResourceGroup](#bots_getbyresourcegroup)
- [GetCheckNameAvailability](#bots_getchecknameavailability)
- [List](#bots_list)
- [ListByResourceGroup](#bots_listbyresourcegroup)
- [Update](#bots_update)

## Channels

- [Create](#channels_create)
- [Delete](#channels_delete)
- [Get](#channels_get)
- [ListByResourceGroup](#channels_listbyresourcegroup)
- [ListWithKeys](#channels_listwithkeys)
- [Update](#channels_update)

## DirectLine

- [RegenerateKeys](#directline_regeneratekeys)

## HostSettings

- [Get](#hostsettings_get)

## OperationResults

- [Get](#operationresults_get)

## Operations

- [List](#operations_list)

## PrivateEndpointConnections

- [Create](#privateendpointconnections_create)
- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [List](#privateendpointconnections_list)

## PrivateLinkResources

- [ListByBotResource](#privatelinkresources_listbybotresource)
### BotConnection_Create

```java
import com.azure.resourcemanager.botservice.models.ConnectionSettingParameter;
import com.azure.resourcemanager.botservice.models.ConnectionSettingProperties;
import java.util.Arrays;

/** Samples for BotConnection Create. */
public final class BotConnectionCreateSamples {
    /*
     * x-ms-original-file: specification/botservice/resource-manager/Microsoft.BotService/preview/2021-05-01-preview/examples/PutConnection.json
     */
    /**
     * Sample code: Create Connection Setting.
     *
     * @param manager Entry point to BotServiceManager.
     */
    public static void createConnectionSetting(com.azure.resourcemanager.botservice.BotServiceManager manager) {
        manager
            .botConnections()
            .define("sampleConnection")
            .withRegion("West US")
            .withExistingBotService("OneResourceGroupName", "samplebotname")
            .withProperties(
                new ConnectionSettingProperties()
                    .withClientId("sampleclientid")
                    .withClientSecret("samplesecret")
                    .withScopes("samplescope")
                    .withServiceProviderId("serviceproviderid")
                    .withParameters(
                        Arrays
                            .asList(
                                new ConnectionSettingParameter().withKey("key1").withValue("value1"),
                                new ConnectionSettingParameter().withKey("key2").withValue("value2"))))
            .withEtag("etag1")
            .create();
    }
}
```

### BotConnection_Delete

```java
import com.azure.core.util.Context;

/** Samples for BotConnection Delete. */
public final class BotConnectionDeleteSamples {
    /*
     * x-ms-original-file: specification/botservice/resource-manager/Microsoft.BotService/preview/2021-05-01-preview/examples/DeleteConnection.json
     */
    /**
     * Sample code: Update Connection Setting.
     *
     * @param manager Entry point to BotServiceManager.
     */
    public static void updateConnectionSetting(com.azure.resourcemanager.botservice.BotServiceManager manager) {
        manager
            .botConnections()
            .deleteWithResponse("OneResourceGroupName", "samplebotname", "sampleConnection", Context.NONE);
    }
}
```

### BotConnection_Get

```java
import com.azure.core.util.Context;

/** Samples for BotConnection Get. */
public final class BotConnectionGetSamples {
    /*
     * x-ms-original-file: specification/botservice/resource-manager/Microsoft.BotService/preview/2021-05-01-preview/examples/GetConnection.json
     */
    /**
     * Sample code: Update Connection Setting.
     *
     * @param manager Entry point to BotServiceManager.
     */
    public static void updateConnectionSetting(com.azure.resourcemanager.botservice.BotServiceManager manager) {
        manager
            .botConnections()
            .getWithResponse("OneResourceGroupName", "samplebotname", "sampleConnection", Context.NONE);
    }
}
```

### BotConnection_ListByBotService

```java
import com.azure.core.util.Context;

/** Samples for BotConnection ListByBotService. */
public final class BotConnectionListByBotServiceSamples {
    /*
     * x-ms-original-file: specification/botservice/resource-manager/Microsoft.BotService/preview/2021-05-01-preview/examples/ListConnectionsByBotService.json
     */
    /**
     * Sample code: List Connection Settings.
     *
     * @param manager Entry point to BotServiceManager.
     */
    public static void listConnectionSettings(com.azure.resourcemanager.botservice.BotServiceManager manager) {
        manager.botConnections().listByBotService("OneResourceGroupName", "samplebotname", Context.NONE);
    }
}
```

### BotConnection_ListServiceProviders

```java
import com.azure.core.util.Context;

/** Samples for BotConnection ListServiceProviders. */
public final class BotConnectionListServiceProvidersSamples {
    /*
     * x-ms-original-file: specification/botservice/resource-manager/Microsoft.BotService/preview/2021-05-01-preview/examples/ListServiceProviders.json
     */
    /**
     * Sample code: List Auth Service Providers.
     *
     * @param manager Entry point to BotServiceManager.
     */
    public static void listAuthServiceProviders(com.azure.resourcemanager.botservice.BotServiceManager manager) {
        manager.botConnections().listServiceProvidersWithResponse(Context.NONE);
    }
}
```

### BotConnection_ListWithSecrets

```java
import com.azure.core.util.Context;

/** Samples for BotConnection ListWithSecrets. */
public final class BotConnectionListWithSecretsSamples {
    /*
     * x-ms-original-file: specification/botservice/resource-manager/Microsoft.BotService/preview/2021-05-01-preview/examples/GetConnection.json
     */
    /**
     * Sample code: Update Connection Setting.
     *
     * @param manager Entry point to BotServiceManager.
     */
    public static void updateConnectionSetting(com.azure.resourcemanager.botservice.BotServiceManager manager) {
        manager
            .botConnections()
            .listWithSecretsWithResponse("OneResourceGroupName", "samplebotname", "sampleConnection", Context.NONE);
    }
}
```

### BotConnection_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.botservice.models.ConnectionSetting;
import com.azure.resourcemanager.botservice.models.ConnectionSettingParameter;
import com.azure.resourcemanager.botservice.models.ConnectionSettingProperties;
import java.util.Arrays;

/** Samples for BotConnection Update. */
public final class BotConnectionUpdateSamples {
    /*
     * x-ms-original-file: specification/botservice/resource-manager/Microsoft.BotService/preview/2021-05-01-preview/examples/UpdateConnection.json
     */
    /**
     * Sample code: Update Connection Setting.
     *
     * @param manager Entry point to BotServiceManager.
     */
    public static void updateConnectionSetting(com.azure.resourcemanager.botservice.BotServiceManager manager) {
        ConnectionSetting resource =
            manager
                .botConnections()
                .getWithResponse("OneResourceGroupName", "samplebotname", "sampleConnection", Context.NONE)
                .getValue();
        resource
            .update()
            .withProperties(
                new ConnectionSettingProperties()
                    .withClientId("sampleclientid")
                    .withClientSecret("samplesecret")
                    .withScopes("samplescope")
                    .withServiceProviderId("serviceproviderid")
                    .withServiceProviderDisplayName("serviceProviderDisplayName")
                    .withParameters(
                        Arrays
                            .asList(
                                new ConnectionSettingParameter().withKey("key1").withValue("value1"),
                                new ConnectionSettingParameter().withKey("key2").withValue("value2"))))
            .withEtag("etag1")
            .apply();
    }
}
```

### Bots_Create

```java
import com.azure.resourcemanager.botservice.models.BotProperties;
import com.azure.resourcemanager.botservice.models.Kind;
import com.azure.resourcemanager.botservice.models.MsaAppType;
import com.azure.resourcemanager.botservice.models.PublicNetworkAccess;
import com.azure.resourcemanager.botservice.models.Sku;
import com.azure.resourcemanager.botservice.models.SkuName;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Bots Create. */
public final class BotsCreateSamples {
    /*
     * x-ms-original-file: specification/botservice/resource-manager/Microsoft.BotService/preview/2021-05-01-preview/examples/CreateBot.json
     */
    /**
     * Sample code: Create Bot.
     *
     * @param manager Entry point to BotServiceManager.
     */
    public static void createBot(com.azure.resourcemanager.botservice.BotServiceManager manager) {
        manager
            .bots()
            .define("samplebotname")
            .withRegion("West US")
            .withExistingResourceGroup("OneResourceGroupName")
            .withTags(mapOf("tag1", "value1", "tag2", "value2"))
            .withProperties(
                new BotProperties()
                    .withDisplayName("The Name of the bot")
                    .withDescription("The description of the bot")
                    .withIconUrl("http://myicon")
                    .withEndpoint("http://mybot.coffee")
                    .withMsaAppType(MsaAppType.USER_ASSIGNED_MSI)
                    .withMsaAppId("exampleappid")
                    .withMsaAppTenantId("exampleapptenantid")
                    .withMsaAppMsiResourceId(
                        "/subscriptions/foo/resourcegroups/bar/providers/microsoft.managedidentity/userassignedidentities/sampleId")
                    .withDeveloperAppInsightKey("appinsightskey")
                    .withDeveloperAppInsightsApiKey("appinsightsapikey")
                    .withDeveloperAppInsightsApplicationId("appinsightsappid")
                    .withLuisAppIds(Arrays.asList("luisappid1", "luisappid2"))
                    .withLuisKey("luiskey")
                    .withIsCmekEnabled(true)
                    .withCmekKeyVaultUrl("https://myCmekKey")
                    .withPublicNetworkAccess(PublicNetworkAccess.ENABLED)
                    .withDisableLocalAuth(true)
                    .withSchemaTransformationVersion("1.0"))
            .withSku(new Sku().withName(SkuName.S1))
            .withKind(Kind.SDK)
            .withEtag("etag1")
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

### Bots_Delete

```java
import com.azure.core.util.Context;

/** Samples for Bots Delete. */
public final class BotsDeleteSamples {
    /*
     * x-ms-original-file: specification/botservice/resource-manager/Microsoft.BotService/preview/2021-05-01-preview/examples/DeleteBot.json
     */
    /**
     * Sample code: Delete Bot.
     *
     * @param manager Entry point to BotServiceManager.
     */
    public static void deleteBot(com.azure.resourcemanager.botservice.BotServiceManager manager) {
        manager.bots().deleteWithResponse("OneResourceGroupName", "samplebotname", Context.NONE);
    }
}
```

### Bots_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Bots GetByResourceGroup. */
public final class BotsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/botservice/resource-manager/Microsoft.BotService/preview/2021-05-01-preview/examples/GetBot.json
     */
    /**
     * Sample code: Get Bot.
     *
     * @param manager Entry point to BotServiceManager.
     */
    public static void getBot(com.azure.resourcemanager.botservice.BotServiceManager manager) {
        manager.bots().getByResourceGroupWithResponse("OneResourceGroupName", "samplebotname", Context.NONE);
    }
}
```

### Bots_GetCheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.botservice.models.CheckNameAvailabilityRequestBody;

/** Samples for Bots GetCheckNameAvailability. */
public final class BotsGetCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/botservice/resource-manager/Microsoft.BotService/preview/2021-05-01-preview/examples/CheckNameAvailability.json
     */
    /**
     * Sample code: check Name Availability.
     *
     * @param manager Entry point to BotServiceManager.
     */
    public static void checkNameAvailability(com.azure.resourcemanager.botservice.BotServiceManager manager) {
        manager
            .bots()
            .getCheckNameAvailabilityWithResponse(
                new CheckNameAvailabilityRequestBody().withName("testbotname").withType("string"), Context.NONE);
    }
}
```

### Bots_List

```java
import com.azure.core.util.Context;

/** Samples for Bots List. */
public final class BotsListSamples {
    /*
     * x-ms-original-file: specification/botservice/resource-manager/Microsoft.BotService/preview/2021-05-01-preview/examples/ListBotsBySubscription.json
     */
    /**
     * Sample code: List Bots by Subscription.
     *
     * @param manager Entry point to BotServiceManager.
     */
    public static void listBotsBySubscription(com.azure.resourcemanager.botservice.BotServiceManager manager) {
        manager.bots().list(Context.NONE);
    }
}
```

### Bots_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Bots ListByResourceGroup. */
public final class BotsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/botservice/resource-manager/Microsoft.BotService/preview/2021-05-01-preview/examples/ListBotsByResourceGroup.json
     */
    /**
     * Sample code: List Bots by Resource Group.
     *
     * @param manager Entry point to BotServiceManager.
     */
    public static void listBotsByResourceGroup(com.azure.resourcemanager.botservice.BotServiceManager manager) {
        manager.bots().listByResourceGroup("OneResourceGroupName", Context.NONE);
    }
}
```

### Bots_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.botservice.models.Bot;
import com.azure.resourcemanager.botservice.models.BotProperties;
import com.azure.resourcemanager.botservice.models.Kind;
import com.azure.resourcemanager.botservice.models.MsaAppType;
import com.azure.resourcemanager.botservice.models.PublicNetworkAccess;
import com.azure.resourcemanager.botservice.models.Sku;
import com.azure.resourcemanager.botservice.models.SkuName;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Bots Update. */
public final class BotsUpdateSamples {
    /*
     * x-ms-original-file: specification/botservice/resource-manager/Microsoft.BotService/preview/2021-05-01-preview/examples/UpdateBot.json
     */
    /**
     * Sample code: Update Bot.
     *
     * @param manager Entry point to BotServiceManager.
     */
    public static void updateBot(com.azure.resourcemanager.botservice.BotServiceManager manager) {
        Bot resource =
            manager
                .bots()
                .getByResourceGroupWithResponse("OneResourceGroupName", "samplebotname", Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("tag1", "value1", "tag2", "value2"))
            .withProperties(
                new BotProperties()
                    .withDisplayName("The Name of the bot")
                    .withDescription("The description of the bot")
                    .withIconUrl("http://myicon")
                    .withEndpoint("http://mybot.coffee")
                    .withMsaAppType(MsaAppType.USER_ASSIGNED_MSI)
                    .withMsaAppId("msaappid")
                    .withMsaAppTenantId("msaapptenantid")
                    .withMsaAppMsiResourceId(
                        "/subscriptions/foo/resourcegroups/bar/providers/microsoft.managedidentity/userassignedidentities/sampleId")
                    .withDeveloperAppInsightKey("appinsightskey")
                    .withDeveloperAppInsightsApiKey("appinsightsapikey")
                    .withDeveloperAppInsightsApplicationId("appinsightsappid")
                    .withLuisAppIds(Arrays.asList("luisappid1", "luisappid2"))
                    .withLuisKey("luiskey")
                    .withIsCmekEnabled(true)
                    .withCmekKeyVaultUrl("https://myCmekKey")
                    .withPublicNetworkAccess(PublicNetworkAccess.ENABLED)
                    .withDisableLocalAuth(true)
                    .withSchemaTransformationVersion("1.0"))
            .withSku(new Sku().withName(SkuName.S1))
            .withKind(Kind.SDK)
            .withEtag("etag1")
            .apply();
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

### Channels_Create

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.botservice.fluent.models.BotChannelInner;
import com.azure.resourcemanager.botservice.models.AlexaChannel;
import com.azure.resourcemanager.botservice.models.AlexaChannelProperties;
import com.azure.resourcemanager.botservice.models.ChannelName;
import com.azure.resourcemanager.botservice.models.DirectLineSpeechChannel;
import com.azure.resourcemanager.botservice.models.DirectLineSpeechChannelProperties;
import com.azure.resourcemanager.botservice.models.EmailChannel;
import com.azure.resourcemanager.botservice.models.EmailChannelProperties;
import com.azure.resourcemanager.botservice.models.LineChannel;
import com.azure.resourcemanager.botservice.models.LineChannelProperties;
import com.azure.resourcemanager.botservice.models.LineRegistration;
import java.util.Arrays;

/** Samples for Channels Create. */
public final class ChannelsCreateSamples {
    /*
     * x-ms-original-file: specification/botservice/resource-manager/Microsoft.BotService/preview/2021-05-01-preview/examples/PutDirectLineSpeechChannel.json
     */
    /**
     * Sample code: Create DirectLine Speech Bot.
     *
     * @param manager Entry point to BotServiceManager.
     */
    public static void createDirectLineSpeechBot(com.azure.resourcemanager.botservice.BotServiceManager manager) {
        manager
            .channels()
            .createWithResponse(
                "OneResourceGroupName",
                "samplebotname",
                ChannelName.DIRECT_LINE_SPEECH_CHANNEL,
                new BotChannelInner()
                    .withLocation("global")
                    .withProperties(
                        new DirectLineSpeechChannel()
                            .withProperties(
                                new DirectLineSpeechChannelProperties()
                                    .withCognitiveServiceRegion("XcognitiveServiceRegionX")
                                    .withCognitiveServiceSubscriptionKey("XcognitiveServiceSubscriptionKeyX")
                                    .withIsEnabled(true))),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/botservice/resource-manager/Microsoft.BotService/preview/2021-05-01-preview/examples/PutChannel.json
     */
    /**
     * Sample code: Create Bot.
     *
     * @param manager Entry point to BotServiceManager.
     */
    public static void createBot(com.azure.resourcemanager.botservice.BotServiceManager manager) {
        manager
            .channels()
            .createWithResponse(
                "OneResourceGroupName",
                "samplebotname",
                ChannelName.EMAIL_CHANNEL,
                new BotChannelInner()
                    .withLocation("global")
                    .withProperties(
                        new EmailChannel()
                            .withProperties(
                                new EmailChannelProperties()
                                    .withEmailAddress("a@b.com")
                                    .withPassword("pwd")
                                    .withIsEnabled(true))),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/botservice/resource-manager/Microsoft.BotService/preview/2021-05-01-preview/examples/PutAlexaChannel.json
     */
    /**
     * Sample code: Create Alexa Bot.
     *
     * @param manager Entry point to BotServiceManager.
     */
    public static void createAlexaBot(com.azure.resourcemanager.botservice.BotServiceManager manager) {
        manager
            .channels()
            .createWithResponse(
                "OneResourceGroupName",
                "samplebotname",
                ChannelName.ALEXA_CHANNEL,
                new BotChannelInner()
                    .withLocation("global")
                    .withProperties(
                        new AlexaChannel()
                            .withProperties(
                                new AlexaChannelProperties().withAlexaSkillId("XAlexaSkillIdX").withIsEnabled(true))),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/botservice/resource-manager/Microsoft.BotService/preview/2021-05-01-preview/examples/PutLineChannel.json
     */
    /**
     * Sample code: Create Line Bot.
     *
     * @param manager Entry point to BotServiceManager.
     */
    public static void createLineBot(com.azure.resourcemanager.botservice.BotServiceManager manager) {
        manager
            .channels()
            .createWithResponse(
                "OneResourceGroupName",
                "samplebotname",
                ChannelName.LINE_CHANNEL,
                new BotChannelInner()
                    .withLocation("global")
                    .withProperties(
                        new LineChannel()
                            .withProperties(
                                new LineChannelProperties()
                                    .withLineRegistrations(
                                        Arrays
                                            .asList(
                                                new LineRegistration()
                                                    .withChannelSecret("channelSecret")
                                                    .withChannelAccessToken("channelAccessToken"))))),
                Context.NONE);
    }
}
```

### Channels_Delete

```java
import com.azure.core.util.Context;

/** Samples for Channels Delete. */
public final class ChannelsDeleteSamples {
    /*
     * x-ms-original-file: specification/botservice/resource-manager/Microsoft.BotService/preview/2021-05-01-preview/examples/DeleteChannel.json
     */
    /**
     * Sample code: Delete Bot.
     *
     * @param manager Entry point to BotServiceManager.
     */
    public static void deleteBot(com.azure.resourcemanager.botservice.BotServiceManager manager) {
        manager.channels().deleteWithResponse("OneResourceGroupName", "samplebotname", "EmailChannel", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/botservice/resource-manager/Microsoft.BotService/preview/2021-05-01-preview/examples/DeleteDirectLineSpeechChannel.json
     */
    /**
     * Sample code: Delete DirectLine Speech Bot.
     *
     * @param manager Entry point to BotServiceManager.
     */
    public static void deleteDirectLineSpeechBot(com.azure.resourcemanager.botservice.BotServiceManager manager) {
        manager
            .channels()
            .deleteWithResponse("OneResourceGroupName", "samplebotname", "DirectLineSpeechChannel", Context.NONE);
    }
}
```

### Channels_Get

```java
import com.azure.core.util.Context;

/** Samples for Channels Get. */
public final class ChannelsGetSamples {
    /*
     * x-ms-original-file: specification/botservice/resource-manager/Microsoft.BotService/preview/2021-05-01-preview/examples/GetDirectLineSpeechChannel.json
     */
    /**
     * Sample code: Get DirectLine Speech Bot.
     *
     * @param manager Entry point to BotServiceManager.
     */
    public static void getDirectLineSpeechBot(com.azure.resourcemanager.botservice.BotServiceManager manager) {
        manager
            .channels()
            .getWithResponse("OneResourceGroupName", "samplebotname", "DirectLineSpeechChannel", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/botservice/resource-manager/Microsoft.BotService/preview/2021-05-01-preview/examples/GetLineChannel.json
     */
    /**
     * Sample code: Get Line Bot.
     *
     * @param manager Entry point to BotServiceManager.
     */
    public static void getLineBot(com.azure.resourcemanager.botservice.BotServiceManager manager) {
        manager.channels().getWithResponse("OneResourceGroupName", "samplebotname", "LineChannel", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/botservice/resource-manager/Microsoft.BotService/preview/2021-05-01-preview/examples/GetChannel.json
     */
    /**
     * Sample code: Get Bot.
     *
     * @param manager Entry point to BotServiceManager.
     */
    public static void getBot(com.azure.resourcemanager.botservice.BotServiceManager manager) {
        manager.channels().getWithResponse("OneResourceGroupName", "samplebotname", "EmailChannel", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/botservice/resource-manager/Microsoft.BotService/preview/2021-05-01-preview/examples/GetAlexaChannel.json
     */
    /**
     * Sample code: Get Alexa Bot.
     *
     * @param manager Entry point to BotServiceManager.
     */
    public static void getAlexaBot(com.azure.resourcemanager.botservice.BotServiceManager manager) {
        manager.channels().getWithResponse("OneResourceGroupName", "samplebotname", "AlexaChannel", Context.NONE);
    }
}
```

### Channels_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Channels ListByResourceGroup. */
public final class ChannelsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/botservice/resource-manager/Microsoft.BotService/preview/2021-05-01-preview/examples/ListChannelsByBotService.json
     */
    /**
     * Sample code: List Bots by Resource Group.
     *
     * @param manager Entry point to BotServiceManager.
     */
    public static void listBotsByResourceGroup(com.azure.resourcemanager.botservice.BotServiceManager manager) {
        manager.channels().listByResourceGroup("OneResourceGroupName", "samplebotname", Context.NONE);
    }
}
```

### Channels_ListWithKeys

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.botservice.models.ChannelName;

/** Samples for Channels ListWithKeys. */
public final class ChannelsListWithKeysSamples {
    /*
     * x-ms-original-file: specification/botservice/resource-manager/Microsoft.BotService/preview/2021-05-01-preview/examples/ListChannel.json
     */
    /**
     * Sample code: List Channel.
     *
     * @param manager Entry point to BotServiceManager.
     */
    public static void listChannel(com.azure.resourcemanager.botservice.BotServiceManager manager) {
        manager
            .channels()
            .listWithKeysWithResponse("OneResourceGroupName", "samplebotname", ChannelName.EMAIL_CHANNEL, Context.NONE);
    }
}
```

### Channels_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.botservice.fluent.models.BotChannelInner;
import com.azure.resourcemanager.botservice.models.AlexaChannel;
import com.azure.resourcemanager.botservice.models.AlexaChannelProperties;
import com.azure.resourcemanager.botservice.models.ChannelName;
import com.azure.resourcemanager.botservice.models.DirectLineSpeechChannel;
import com.azure.resourcemanager.botservice.models.DirectLineSpeechChannelProperties;
import com.azure.resourcemanager.botservice.models.EmailChannel;
import com.azure.resourcemanager.botservice.models.EmailChannelProperties;
import com.azure.resourcemanager.botservice.models.LineChannel;
import com.azure.resourcemanager.botservice.models.LineChannelProperties;
import com.azure.resourcemanager.botservice.models.LineRegistration;
import java.util.Arrays;

/** Samples for Channels Update. */
public final class ChannelsUpdateSamples {
    /*
     * x-ms-original-file: specification/botservice/resource-manager/Microsoft.BotService/preview/2021-05-01-preview/examples/UpdateDirectLineSpeechChannel.json
     */
    /**
     * Sample code: Update DirectLine Speech.
     *
     * @param manager Entry point to BotServiceManager.
     */
    public static void updateDirectLineSpeech(com.azure.resourcemanager.botservice.BotServiceManager manager) {
        manager
            .channels()
            .updateWithResponse(
                "OneResourceGroupName",
                "samplebotname",
                ChannelName.DIRECT_LINE_SPEECH_CHANNEL,
                new BotChannelInner()
                    .withLocation("global")
                    .withProperties(
                        new DirectLineSpeechChannel()
                            .withProperties(
                                new DirectLineSpeechChannelProperties()
                                    .withCognitiveServiceRegion("XcognitiveServiceRegionX")
                                    .withCognitiveServiceSubscriptionKey("XcognitiveServiceSubscriptionKeyX")
                                    .withIsEnabled(true))),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/botservice/resource-manager/Microsoft.BotService/preview/2021-05-01-preview/examples/UpdateChannel.json
     */
    /**
     * Sample code: Update Bot.
     *
     * @param manager Entry point to BotServiceManager.
     */
    public static void updateBot(com.azure.resourcemanager.botservice.BotServiceManager manager) {
        manager
            .channels()
            .updateWithResponse(
                "OneResourceGroupName",
                "samplebotname",
                ChannelName.EMAIL_CHANNEL,
                new BotChannelInner()
                    .withLocation("global")
                    .withProperties(
                        new EmailChannel()
                            .withProperties(
                                new EmailChannelProperties()
                                    .withEmailAddress("a@b.com")
                                    .withPassword("pwd")
                                    .withIsEnabled(true))),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/botservice/resource-manager/Microsoft.BotService/preview/2021-05-01-preview/examples/UpdateLineChannel.json
     */
    /**
     * Sample code: Update Line.
     *
     * @param manager Entry point to BotServiceManager.
     */
    public static void updateLine(com.azure.resourcemanager.botservice.BotServiceManager manager) {
        manager
            .channels()
            .updateWithResponse(
                "OneResourceGroupName",
                "samplebotname",
                ChannelName.LINE_CHANNEL,
                new BotChannelInner()
                    .withLocation("global")
                    .withProperties(
                        new LineChannel()
                            .withProperties(
                                new LineChannelProperties()
                                    .withLineRegistrations(
                                        Arrays
                                            .asList(
                                                new LineRegistration()
                                                    .withChannelSecret("channelSecret")
                                                    .withChannelAccessToken("channelAccessToken"))))),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/botservice/resource-manager/Microsoft.BotService/preview/2021-05-01-preview/examples/UpdateAlexaChannel.json
     */
    /**
     * Sample code: Update Alexa.
     *
     * @param manager Entry point to BotServiceManager.
     */
    public static void updateAlexa(com.azure.resourcemanager.botservice.BotServiceManager manager) {
        manager
            .channels()
            .updateWithResponse(
                "OneResourceGroupName",
                "samplebotname",
                ChannelName.ALEXA_CHANNEL,
                new BotChannelInner()
                    .withLocation("global")
                    .withProperties(
                        new AlexaChannel()
                            .withProperties(
                                new AlexaChannelProperties().withAlexaSkillId("XAlexaSkillIdX").withIsEnabled(true))),
                Context.NONE);
    }
}
```

### DirectLine_RegenerateKeys

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.botservice.models.Key;
import com.azure.resourcemanager.botservice.models.RegenerateKeysChannelName;
import com.azure.resourcemanager.botservice.models.SiteInfo;

/** Samples for DirectLine RegenerateKeys. */
public final class DirectLineRegenerateKeysSamples {
    /*
     * x-ms-original-file: specification/botservice/resource-manager/Microsoft.BotService/preview/2021-05-01-preview/examples/DirectlineRegenerateKeys.json
     */
    /**
     * Sample code: Regenerate Keys for DirectLine Channel Site.
     *
     * @param manager Entry point to BotServiceManager.
     */
    public static void regenerateKeysForDirectLineChannelSite(
        com.azure.resourcemanager.botservice.BotServiceManager manager) {
        manager
            .directLines()
            .regenerateKeysWithResponse(
                "OneResourceGroupName",
                "samplebotname",
                RegenerateKeysChannelName.DIRECT_LINE_CHANNEL,
                new SiteInfo().withSiteName("testSiteName").withKey(Key.KEY1),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/botservice/resource-manager/Microsoft.BotService/preview/2021-05-01-preview/examples/WebChatRegenerateKeys.json
     */
    /**
     * Sample code: Regenerate Keys for WebChat Channel Site.
     *
     * @param manager Entry point to BotServiceManager.
     */
    public static void regenerateKeysForWebChatChannelSite(
        com.azure.resourcemanager.botservice.BotServiceManager manager) {
        manager
            .directLines()
            .regenerateKeysWithResponse(
                "OneResourceGroupName",
                "samplebotname",
                RegenerateKeysChannelName.WEB_CHAT_CHANNEL,
                new SiteInfo().withSiteName("testSiteName").withKey(Key.KEY1),
                Context.NONE);
    }
}
```

### HostSettings_Get

```java
import com.azure.core.util.Context;

/** Samples for HostSettings Get. */
public final class HostSettingsGetSamples {
    /*
     * x-ms-original-file: specification/botservice/resource-manager/Microsoft.BotService/preview/2021-05-01-preview/examples/GetHostSettings.json
     */
    /**
     * Sample code: Get Bot Host Settings.
     *
     * @param manager Entry point to BotServiceManager.
     */
    public static void getBotHostSettings(com.azure.resourcemanager.botservice.BotServiceManager manager) {
        manager.hostSettings().getWithResponse(Context.NONE);
    }
}
```

### OperationResults_Get

```java
import com.azure.core.util.Context;

/** Samples for OperationResults Get. */
public final class OperationResultsGetSamples {
    /*
     * x-ms-original-file: specification/botservice/resource-manager/Microsoft.BotService/preview/2021-05-01-preview/examples/OperationResultsGet.json
     */
    /**
     * Sample code: Get operation result.
     *
     * @param manager Entry point to BotServiceManager.
     */
    public static void getOperationResult(com.azure.resourcemanager.botservice.BotServiceManager manager) {
        manager.operationResults().get("exampleid", Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/botservice/resource-manager/Microsoft.BotService/preview/2021-05-01-preview/examples/GetOperations.json
     */
    /**
     * Sample code: Get Operations.
     *
     * @param manager Entry point to BotServiceManager.
     */
    public static void getOperations(com.azure.resourcemanager.botservice.BotServiceManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### PrivateEndpointConnections_Create

```java
import com.azure.resourcemanager.botservice.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.botservice.models.PrivateLinkServiceConnectionState;

/** Samples for PrivateEndpointConnections Create. */
public final class PrivateEndpointConnectionsCreateSamples {
    /*
     * x-ms-original-file: specification/botservice/resource-manager/Microsoft.BotService/preview/2021-05-01-preview/examples/PutPrivateEndpointConnection.json
     */
    /**
     * Sample code: Put Private Endpoint Connection.
     *
     * @param manager Entry point to BotServiceManager.
     */
    public static void putPrivateEndpointConnection(com.azure.resourcemanager.botservice.BotServiceManager manager) {
        manager
            .privateEndpointConnections()
            .define("{privateEndpointConnectionName}")
            .withExistingBotService("res7687", "sto9699")
            .withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionState()
                    .withStatus(PrivateEndpointServiceConnectionStatus.APPROVED)
                    .withDescription("Auto-Approved"))
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
     * x-ms-original-file: specification/botservice/resource-manager/Microsoft.BotService/preview/2021-05-01-preview/examples/DeletePrivateEndpointConnection.json
     */
    /**
     * Sample code: Delete Private Endpoint Connection.
     *
     * @param manager Entry point to BotServiceManager.
     */
    public static void deletePrivateEndpointConnection(com.azure.resourcemanager.botservice.BotServiceManager manager) {
        manager
            .privateEndpointConnections()
            .deleteWithResponse("res6977", "sto2527", "{privateEndpointConnectionName}", Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections Get. */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: specification/botservice/resource-manager/Microsoft.BotService/preview/2021-05-01-preview/examples/GetPrivateEndpointConnection.json
     */
    /**
     * Sample code: Get Private Endpoint Connection.
     *
     * @param manager Entry point to BotServiceManager.
     */
    public static void getPrivateEndpointConnection(com.azure.resourcemanager.botservice.BotServiceManager manager) {
        manager
            .privateEndpointConnections()
            .getWithResponse("res6977", "sto2527", "{privateEndpointConnectionName}", Context.NONE);
    }
}
```

### PrivateEndpointConnections_List

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections List. */
public final class PrivateEndpointConnectionsListSamples {
    /*
     * x-ms-original-file: specification/botservice/resource-manager/Microsoft.BotService/preview/2021-05-01-preview/examples/ListPrivateEndpointConnections.json
     */
    /**
     * Sample code: List Private Endpoint Connections.
     *
     * @param manager Entry point to BotServiceManager.
     */
    public static void listPrivateEndpointConnections(com.azure.resourcemanager.botservice.BotServiceManager manager) {
        manager.privateEndpointConnections().list("res6977", "sto2527", Context.NONE);
    }
}
```

### PrivateLinkResources_ListByBotResource

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkResources ListByBotResource. */
public final class PrivateLinkResourcesListByBotResourceSamples {
    /*
     * x-ms-original-file: specification/botservice/resource-manager/Microsoft.BotService/preview/2021-05-01-preview/examples/ListPrivateLinkResources.json
     */
    /**
     * Sample code: List Private Link Resources.
     *
     * @param manager Entry point to BotServiceManager.
     */
    public static void listPrivateLinkResources(com.azure.resourcemanager.botservice.BotServiceManager manager) {
        manager.privateLinkResources().listByBotResourceWithResponse("res6977", "sto2527", Context.NONE);
    }
}
```


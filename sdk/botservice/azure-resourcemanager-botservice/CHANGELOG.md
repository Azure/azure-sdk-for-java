# Release History

## 1.0.0-beta.7 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.6 (2024-10-09)

- Azure Resource Manager BotService client library for Java. This package contains Microsoft Azure SDK for BotService Management SDK. Azure Bot Service is a platform for creating smart conversational agents. Package tag package-preview-2021-05. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Bots` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Site` was modified

* `withIsWebchatPreviewEnabled(boolean)` was removed
* `models.Site withIsTokenEnabled(java.lang.Boolean)` -> `models.Site withIsTokenEnabled(java.lang.Boolean)`
* `withIsV1Enabled(boolean)` was removed
* `withIsEnabled(boolean)` was removed
* `boolean isV1Enabled()` -> `java.lang.Boolean isV1Enabled()`
* `boolean isV3Enabled()` -> `java.lang.Boolean isV3Enabled()`
* `withIsV3Enabled(boolean)` was removed
* `withSiteName(java.lang.String)` was removed

#### `models.DirectLineSite` was modified

* `isV1Enabled()` was removed
* `isSecureSiteEnabled()` was removed
* `siteName()` was removed
* `withIsV3Enabled(boolean)` was removed
* `isEnabled()` was removed
* `trustedOrigins()` was removed
* `isV3Enabled()` was removed
* `isBlockUserUploadEnabled()` was removed
* `withIsV1Enabled(boolean)` was removed

#### `models.SlackChannelProperties` was modified

* `withRegisterBeforeOAuthFlow(java.lang.Boolean)` was removed

#### `models.WebChatSite` was modified

* `withIsWebchatPreviewEnabled(boolean)` was removed
* `isEnabled()` was removed
* `isWebchatPreviewEnabled()` was removed
* `siteName()` was removed

### Features Added

* `models.TelephonyChannelProperties` was added

* `models.OutlookChannel` was added

* `models.TelephonyChannel` was added

* `models.TelephonyChannelResourceApiConfiguration` was added

* `models.SearchAssistant` was added

* `models.Omnichannel` was added

* `models.AcsChatChannel` was added

* `models.TelephonyPhoneNumbers` was added

* `models.M365Extensions` was added

* `models.EmailChannelAuthMethod` was added

#### `models.BotResponseList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ServiceProviderProperties` was modified

* `withIconUrl(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AlexaChannelProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Bots` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.SmsChannelProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ChannelResponseList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Channel` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `channelName()` was added

#### `models.OperationEntityListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ChannelSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `requireTermsAgreement()` was added
* `withRequireTermsAgreement(java.lang.Boolean)` was added

#### `models.DirectLineChannel` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `provisioningState()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `channelName()` was added

#### `models.KikChannel` was modified

* `channelName()` was added
* `provisioningState()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DirectLineChannelProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `extensionKey1()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `extensionKey2()` was added
* `withExtensionKey1(java.lang.String)` was added
* `withExtensionKey2(java.lang.String)` was added

#### `models.Sku` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DirectLineSpeechChannel` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `provisioningState()` was added
* `channelName()` was added

#### `models.WebChatChannel` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `provisioningState()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `channelName()` was added

#### `models.CheckNameAvailabilityResponseBody` was modified

* `absCode()` was added

#### `models.BotProperties` was modified

* `tenantId()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withTenantId(java.lang.String)` was added

#### `models.SiteInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.FacebookChannelProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateLinkServiceConnectionState` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TelegramChannelProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TelegramChannel` was modified

* `provisioningState()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `channelName()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateEndpointConnectionListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateLinkResource` was modified

* `type()` was added
* `id()` was added
* `name()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SlackChannel` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `channelName()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `provisioningState()` was added

#### `models.Site` was modified

* `withTenantId(java.lang.String)` was added
* `withIsNoStorageEnabled(java.lang.Boolean)` was added
* `isEndpointParametersEnabled()` was added
* `withAppId(java.lang.String)` was added
* `withIsV1Enabled(java.lang.Boolean)` was added
* `appId()` was added
* `withIsEndpointParametersEnabled(java.lang.Boolean)` was added
* `isWebchatPreviewEnabled()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withIsV3Enabled(java.lang.Boolean)` was added
* `withIsWebchatPreviewEnabled(java.lang.Boolean)` was added
* `isDetailedLoggingEnabled()` was added
* `isNoStorageEnabled()` was added
* `isWebChatSpeechEnabled()` was added
* `withIsDetailedLoggingEnabled(java.lang.Boolean)` was added
* `tenantId()` was added
* `withIsWebChatSpeechEnabled(java.lang.Boolean)` was added

#### `models.FacebookChannel` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `channelName()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `provisioningState()` was added

#### `models.ServiceProviderParameterMetadata` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MsTeamsChannel` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `channelName()` was added
* `provisioningState()` was added

#### `models.EmailChannelProperties` was modified

* `authMethod()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withMagicCode(java.lang.String)` was added
* `magicCode()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withAuthMethod(models.EmailChannelAuthMethod)` was added

#### `models.ConnectionSettingProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SkypeChannelProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WebChatChannelProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ServiceProviderParameterMetadataConstraints` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateEndpoint` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DirectLineSpeechChannelProperties` was modified

* `withCognitiveServiceResourceId(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `cognitiveServiceResourceId()` was added

#### `models.PrivateEndpointConnection` was modified

* `groupIds()` was added

#### `models.ServiceProvider` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateLinkResourceBase` was modified

* `type()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `id()` was added
* `name()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateEndpointConnection$Definition` was modified

* `withGroupIds(java.util.List)` was added

#### `models.ConnectionSettingParameter` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.KikChannelProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DirectLineSite` was modified

* `withAppId(java.lang.String)` was added
* `withIsWebchatPreviewEnabled(java.lang.Boolean)` was added
* `withTrustedOrigins(java.util.List)` was added
* `withSiteName(java.lang.String)` was added
* `withIsBlockUserUploadEnabled(java.lang.Boolean)` was added
* `withIsV3Enabled(java.lang.Boolean)` was added
* `withIsWebChatSpeechEnabled(java.lang.Boolean)` was added
* `withIsV1Enabled(java.lang.Boolean)` was added
* `withEtag(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withIsEndpointParametersEnabled(java.lang.Boolean)` was added
* `withTenantId(java.lang.String)` was added
* `withIsNoStorageEnabled(java.lang.Boolean)` was added
* `withIsEnabled(boolean)` was added
* `withIsDetailedLoggingEnabled(java.lang.Boolean)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `isTokenEnabled()` was added
* `withIsSecureSiteEnabled(java.lang.Boolean)` was added

#### `models.LineRegistration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EmailChannel` was modified

* `channelName()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `provisioningState()` was added

#### `models.OperationDisplayInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SkypeChannel` was modified

* `channelName()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `provisioningState()` was added

#### `models.MsTeamsChannelProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.LineChannel` was modified

* `provisioningState()` was added
* `channelName()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SlackChannelProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ServiceProviderParameter` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AlexaChannel` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `channelName()` was added
* `provisioningState()` was added

#### `models.SmsChannel` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `channelName()` was added
* `provisioningState()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WebChatSite` was modified

* `withIsNoStorageEnabled(java.lang.Boolean)` was added
* `withIsEnabled(boolean)` was added
* `withIsV3Enabled(java.lang.Boolean)` was added
* `withIsDetailedLoggingEnabled(java.lang.Boolean)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withTenantId(java.lang.String)` was added
* `withIsSecureSiteEnabled(java.lang.Boolean)` was added
* `withSiteName(java.lang.String)` was added
* `withEtag(java.lang.String)` was added
* `withAppId(java.lang.String)` was added
* `withIsWebchatPreviewEnabled(java.lang.Boolean)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withIsV1Enabled(java.lang.Boolean)` was added
* `withIsWebChatSpeechEnabled(java.lang.Boolean)` was added
* `withTrustedOrigins(java.util.List)` was added
* `withIsEndpointParametersEnabled(java.lang.Boolean)` was added
* `withIsBlockUserUploadEnabled(java.lang.Boolean)` was added
* `isTokenEnabled()` was added

#### `models.ConnectionSettingResponseList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FacebookPage` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.LineChannelProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CheckNameAvailabilityRequestBody` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

## 1.0.0-beta.5 (2022-06-20)

- Azure Resource Manager BotService client library for Java. This package contains Microsoft Azure SDK for BotService Management SDK. Azure Bot Service is a platform for creating smart conversational agents. Package tag package-preview-2021-05. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.Bot` was modified

* `resourceGroupName()` was added

#### `models.SlackChannelProperties` was modified

* `withRegisterBeforeOAuthFlow(java.lang.Boolean)` was added

#### `models.ConnectionSetting` was modified

* `resourceGroupName()` was added

## 1.0.0-beta.4 (2022-04-21)

- Azure Resource Manager BotService client library for Java. This package contains Microsoft Azure SDK for BotService Management SDK. Azure Bot Service is a platform for creating smart conversational agents. Package tag package-preview-2021-05. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `BotServiceManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `BotServiceManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

## 1.0.0-beta.3 (2022-01-26)

- Azure Resource Manager BotService client library for Java. This package contains Microsoft Azure SDK for BotService Management SDK. Azure Bot Service is a platform for creating smart conversational agents. Package tag package-preview-2021-05. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.BotChannel$Definition` was removed

* `models.BotChannel$UpdateStages` was removed

* `models.BotChannel$DefinitionStages` was removed

* `models.BotChannel$Update` was removed

#### `models.BotChannel` was modified

* `refresh(com.azure.core.util.Context)` was removed
* `listWithKeys()` was removed
* `region()` was removed
* `regionName()` was removed
* `refresh()` was removed
* `listWithKeysWithResponse(com.azure.core.util.Context)` was removed
* `update()` was removed

#### `models.BotProperties` was modified

* `withIsIsolated(java.lang.Boolean)` was removed
* `isIsolated()` was removed

#### `models.Channels` was modified

* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `models.BotChannel listWithKeys(java.lang.String,java.lang.String,models.ChannelName)` -> `models.ListChannelWithKeysResponse listWithKeys(java.lang.String,java.lang.String,models.ChannelName)`
* `getById(java.lang.String)` was removed
* `deleteById(java.lang.String)` was removed
* `define(models.ChannelName)` was removed

#### `models.WebChatSite` was modified

* `enablePreview()` was removed
* `withEnablePreview(boolean)` was removed

### Features Added

* `models.ListChannelWithKeysResponse` was added

* `models.ChannelSettings` was added

* `models.Site` was added

* `models.ServiceProviderParameterMetadata` was added

* `models.PublicNetworkAccess` was added

* `models.ServiceProviderParameterMetadataConstraints` was added

#### `models.BotChannel` was modified

* `zones()` was added

#### `models.Bot` was modified

* `zones()` was added

#### `models.Channel` was modified

* `location()` was added
* `withEtag(java.lang.String)` was added
* `etag()` was added
* `withLocation(java.lang.String)` was added
* `provisioningState()` was added

#### `models.DirectLineChannel` was modified

* `withEtag(java.lang.String)` was added
* `withLocation(java.lang.String)` was added
* `withEtag(java.lang.String)` was added
* `withLocation(java.lang.String)` was added

#### `models.KikChannel` was modified

* `withEtag(java.lang.String)` was added
* `withLocation(java.lang.String)` was added
* `withEtag(java.lang.String)` was added
* `withLocation(java.lang.String)` was added

#### `models.DirectLineChannelProperties` was modified

* `directLineEmbedCode()` was added
* `withDirectLineEmbedCode(java.lang.String)` was added

#### `models.DirectLineSpeechChannel` was modified

* `withEtag(java.lang.String)` was added
* `withEtag(java.lang.String)` was added
* `withLocation(java.lang.String)` was added
* `withLocation(java.lang.String)` was added

#### `models.WebChatChannel` was modified

* `withEtag(java.lang.String)` was added
* `withLocation(java.lang.String)` was added
* `withLocation(java.lang.String)` was added
* `withEtag(java.lang.String)` was added

#### `models.BotProperties` was modified

* `parameters()` was added
* `migrationToken()` was added
* `withPublishingCredentials(java.lang.String)` was added
* `withParameters(java.util.Map)` was added
* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added
* `publicNetworkAccess()` was added
* `manifestUrl()` was added
* `withStorageResourceId(java.lang.String)` was added
* `cmekEncryptionStatus()` was added
* `allSettings()` was added
* `storageResourceId()` was added
* `withOpenWithHint(java.lang.String)` was added
* `withAppPasswordHint(java.lang.String)` was added
* `openWithHint()` was added
* `isDeveloperAppInsightsApiKeySet()` was added
* `withManifestUrl(java.lang.String)` was added
* `provisioningState()` was added
* `appPasswordHint()` was added
* `isStreamingSupported()` was added
* `withIsStreamingSupported(java.lang.Boolean)` was added
* `publishingCredentials()` was added
* `withAllSettings(java.util.Map)` was added

#### `models.TelegramChannel` was modified

* `withEtag(java.lang.String)` was added
* `withLocation(java.lang.String)` was added
* `withEtag(java.lang.String)` was added
* `withLocation(java.lang.String)` was added

#### `models.SlackChannel` was modified

* `withLocation(java.lang.String)` was added
* `withLocation(java.lang.String)` was added
* `withEtag(java.lang.String)` was added
* `withEtag(java.lang.String)` was added

#### `models.FacebookChannel` was modified

* `withEtag(java.lang.String)` was added
* `withEtag(java.lang.String)` was added
* `withLocation(java.lang.String)` was added
* `withLocation(java.lang.String)` was added

#### `models.MsTeamsChannel` was modified

* `withLocation(java.lang.String)` was added
* `withEtag(java.lang.String)` was added
* `withLocation(java.lang.String)` was added
* `withEtag(java.lang.String)` was added

#### `models.ConnectionSettingProperties` was modified

* `withId(java.lang.String)` was added
* `withName(java.lang.String)` was added
* `provisioningState()` was added
* `id()` was added
* `name()` was added
* `withProvisioningState(java.lang.String)` was added

#### `models.SkypeChannelProperties` was modified

* `withIncomingCallRoute(java.lang.String)` was added
* `incomingCallRoute()` was added

#### `models.DirectLineSite` was modified

* `isBlockUserUploadEnabled()` was added
* `withIsBlockUserUploadEnabled(java.lang.Boolean)` was added

#### `models.EmailChannel` was modified

* `withEtag(java.lang.String)` was added
* `withLocation(java.lang.String)` was added
* `withLocation(java.lang.String)` was added
* `withEtag(java.lang.String)` was added

#### `models.SkypeChannel` was modified

* `withEtag(java.lang.String)` was added
* `withLocation(java.lang.String)` was added
* `withLocation(java.lang.String)` was added
* `withEtag(java.lang.String)` was added

#### `models.MsTeamsChannelProperties` was modified

* `withDeploymentEnvironment(java.lang.String)` was added
* `acceptedTerms()` was added
* `incomingCallRoute()` was added
* `withAcceptedTerms(java.lang.Boolean)` was added
* `withIncomingCallRoute(java.lang.String)` was added
* `deploymentEnvironment()` was added

#### `models.LineChannel` was modified

* `withLocation(java.lang.String)` was added
* `withEtag(java.lang.String)` was added
* `withLocation(java.lang.String)` was added
* `withEtag(java.lang.String)` was added

#### `models.Channels` was modified

* `createWithResponse(java.lang.String,java.lang.String,models.ChannelName,fluent.models.BotChannelInner,com.azure.core.util.Context)` was added
* `updateWithResponse(java.lang.String,java.lang.String,models.ChannelName,fluent.models.BotChannelInner,com.azure.core.util.Context)` was added
* `update(java.lang.String,java.lang.String,models.ChannelName,fluent.models.BotChannelInner)` was added
* `create(java.lang.String,java.lang.String,models.ChannelName,fluent.models.BotChannelInner)` was added

#### `models.ConnectionSetting` was modified

* `zones()` was added

#### `models.ServiceProviderParameter` was modified

* `metadata()` was added

#### `models.AlexaChannel` was modified

* `withEtag(java.lang.String)` was added
* `withEtag(java.lang.String)` was added
* `withLocation(java.lang.String)` was added
* `withLocation(java.lang.String)` was added

#### `models.SmsChannel` was modified

* `withLocation(java.lang.String)` was added
* `withLocation(java.lang.String)` was added
* `withEtag(java.lang.String)` was added
* `withEtag(java.lang.String)` was added

#### `models.WebChatSite` was modified

* `withIsWebchatPreviewEnabled(boolean)` was added
* `isWebchatPreviewEnabled()` was added

## 1.0.0-beta.2 (2021-10-09)

- Azure Resource Manager BotService client library for Java. This package contains Microsoft Azure SDK for BotService Management SDK. Azure Bot Service is a platform for creating smart conversational agents. Package tag package-preview-2021-05. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.OperationResultsDescription` was added

* `models.PrivateLinkResources` was added

* `models.PrivateEndpointConnection$DefinitionStages` was added

* `models.PrivateEndpointConnections` was added

* `models.OperationResultStatus` was added

* `models.PrivateLinkServiceConnectionState` was added

* `models.PrivateEndpointConnectionProvisioningState` was added

* `models.PrivateEndpointConnectionListResult` was added

* `models.PrivateLinkResource` was added

* `models.PrivateEndpoint` was added

* `models.PrivateEndpointConnection` was added

* `models.PrivateEndpointServiceConnectionStatus` was added

* `models.PrivateLinkResourceBase` was added

* `models.PrivateEndpointConnection$Definition` was added

* `models.MsaAppType` was added

* `models.OperationResults` was added

* `models.PrivateLinkResourceListResult` was added

#### `models.BotProperties` was modified

* `msaAppMsiResourceId()` was added
* `privateEndpointConnections()` was added
* `withMsaAppMsiResourceId(java.lang.String)` was added
* `disableLocalAuth()` was added
* `msaAppTenantId()` was added
* `withMsaAppType(models.MsaAppType)` was added
* `withDisableLocalAuth(java.lang.Boolean)` was added
* `msaAppType()` was added
* `withMsaAppTenantId(java.lang.String)` was added

#### `BotServiceManager$Configurable` was modified

* `withScope(java.lang.String)` was added

#### `models.SlackChannelProperties` was modified

* `scopes()` was added
* `withScopes(java.lang.String)` was added

#### `BotServiceManager` was modified

* `privateEndpointConnections()` was added
* `operationResults()` was added
* `privateLinkResources()` was added

## 1.0.0-beta.1 (2021-05-14)

- Azure Resource Manager BotService client library for Java. This package contains Microsoft Azure SDK for BotService Management SDK. Azure Bot Service is a platform for creating smart conversational agents. Package tag package-2021-03-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

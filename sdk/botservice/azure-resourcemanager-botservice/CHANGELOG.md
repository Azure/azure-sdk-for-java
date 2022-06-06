# Release History

## 1.0.0-beta.5 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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

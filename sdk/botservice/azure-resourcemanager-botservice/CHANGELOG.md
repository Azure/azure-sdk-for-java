# Release History

## 1.1.0-beta.1 (2026-03-04)

- Azure Resource Manager BotService client library for Java. This package contains Microsoft Azure SDK for BotService Management SDK. Azure Bot Service is a platform for creating smart conversational agents. Package api-version 2023-09-15-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.BotResponseList` was removed

#### `models.ChannelResponseList` was removed

#### `models.OperationEntityListResult` was removed

#### `models.ConnectionSettingResponseList` was removed

#### `models.TelephonyChannelProperties` was modified

* `withPremiumSku(java.lang.String)` was removed
* `premiumSku()` was removed
* `validate()` was removed

#### `models.ServiceProviderProperties` was modified

* `ServiceProviderProperties()` was changed to private access
* `validate()` was removed
* `withParameters(java.util.List)` was removed
* `withIconUrl(java.lang.String)` was removed

#### `models.AlexaChannelProperties` was modified

* `validate()` was removed

#### `models.SmsChannelProperties` was modified

* `accountSid()` was removed
* `withAccountSid(java.lang.String)` was removed
* `validate()` was removed

#### `models.Channel` was modified

* `validate()` was removed

#### `models.ChannelSettings` was modified

* `ChannelSettings()` was changed to private access
* `withExtensionKey1(java.lang.String)` was removed
* `withChannelDisplayName(java.lang.String)` was removed
* `withChannelId(java.lang.String)` was removed
* `withBotId(java.lang.String)` was removed
* `withRequireTermsAgreement(java.lang.Boolean)` was removed
* `withBotIconUrl(java.lang.String)` was removed
* `validate()` was removed
* `withDisableLocalAuth(java.lang.Boolean)` was removed
* `withIsEnabled(java.lang.Boolean)` was removed
* `withSites(java.util.List)` was removed
* `withExtensionKey2(java.lang.String)` was removed

#### `models.DirectLineChannel` was modified

* `validate()` was removed

#### `models.KikChannel` was modified

* `validate()` was removed

#### `models.DirectLineChannelProperties` was modified

* `validate()` was removed

#### `models.OperationEntity` was modified

* `java.lang.Object properties()` -> `com.azure.core.util.BinaryData properties()`

#### `models.Sku` was modified

* `validate()` was removed

#### `models.OutlookChannel` was modified

* `validate()` was removed

#### `models.DirectLineSpeechChannel` was modified

* `validate()` was removed

#### `models.WebChatChannel` was modified

* `validate()` was removed

#### `models.TelephonyChannel` was modified

* `validate()` was removed

#### `models.BotProperties` was modified

* `validate()` was removed
* `withMsaAppMsiResourceId(java.lang.String)` was removed
* `msaAppMsiResourceId()` was removed

#### `models.SiteInfo` was modified

* `validate()` was removed

#### `models.FacebookChannelProperties` was modified

* `validate()` was removed

#### `models.TelegramChannelProperties` was modified

* `validate()` was removed

#### `models.TelegramChannel` was modified

* `validate()` was removed

#### `models.SlackChannel` was modified

* `validate()` was removed

#### `models.Site` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed
* `etag()` was removed

#### `models.FacebookChannel` was modified

* `validate()` was removed

#### `models.ServiceProviderParameterMetadata` was modified

* `ServiceProviderParameterMetadata()` was changed to private access
* `validate()` was removed
* `withConstraints(models.ServiceProviderParameterMetadataConstraints)` was removed

#### `models.MsTeamsChannel` was modified

* `validate()` was removed

#### `models.EmailChannelProperties` was modified

* `validate()` was removed

#### `models.ConnectionSettingProperties` was modified

* `validate()` was removed

#### `models.SkypeChannelProperties` was modified

* `withCallingWebhook(java.lang.String)` was removed
* `callingWebhook()` was removed
* `validate()` was removed

#### `models.WebChatChannelProperties` was modified

* `validate()` was removed

#### `models.ServiceProviderParameterMetadataConstraints` was modified

* `ServiceProviderParameterMetadataConstraints()` was changed to private access
* `validate()` was removed
* `withRequired(java.lang.Boolean)` was removed

#### `models.TelephonyChannelResourceApiConfiguration` was modified

* `validate()` was removed

#### `models.SearchAssistant` was modified

* `validate()` was removed

#### `models.DirectLineSpeechChannelProperties` was modified

* `validate()` was removed

#### `models.ServiceProvider` was modified

* `ServiceProvider()` was changed to private access
* `withProperties(models.ServiceProviderProperties)` was removed
* `validate()` was removed

#### `models.ConnectionSettingParameter` was modified

* `validate()` was removed

#### `models.KikChannelProperties` was modified

* `validate()` was removed
* `withUsername(java.lang.String)` was removed
* `username()` was removed

#### `models.DirectLineSite` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.LineRegistration` was modified

* `validate()` was removed

#### `models.EmailChannel` was modified

* `validate()` was removed

#### `models.Omnichannel` was modified

* `validate()` was removed

#### `models.AcsChatChannel` was modified

* `validate()` was removed

#### `models.OperationDisplayInfo` was modified

* `OperationDisplayInfo()` was changed to private access
* `withDescription(java.lang.String)` was removed
* `withResource(java.lang.String)` was removed
* `withProvider(java.lang.String)` was removed
* `validate()` was removed
* `withOperation(java.lang.String)` was removed

#### `models.SkypeChannel` was modified

* `validate()` was removed

#### `models.MsTeamsChannelProperties` was modified

* `validate()` was removed

#### `models.LineChannel` was modified

* `validate()` was removed

#### `models.SlackChannelProperties` was modified

* `validate()` was removed

#### `models.ServiceProviderParameter` was modified

* `ServiceProviderParameter()` was changed to private access
* `validate()` was removed

#### `models.AlexaChannel` was modified

* `validate()` was removed

#### `models.SmsChannel` was modified

* `validate()` was removed

#### `models.WebChatSite` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.TelephonyPhoneNumbers` was modified

* `validate()` was removed

#### `models.M365Extensions` was modified

* `validate()` was removed

#### `models.FacebookPage` was modified

* `validate()` was removed

#### `models.EmailChannelAuthMethod` was modified

* `fromFloat(float)` was removed
* `toFloat()` was removed

#### `models.LineChannelProperties` was modified

* `validate()` was removed

#### `models.CheckNameAvailabilityRequestBody` was modified

* `validate()` was removed

#### `BotServiceManager` was modified

* `fluent.AzureBotService serviceClient()` -> `fluent.BotServiceManagementClient serviceClient()`

### Features Added

* `models.OperationResultsDescription` was added

* `models.ProvisioningState` was added

* `models.NspAccessRuleDirection` was added

* `models.QnAMakerEndpointKeysResponse` was added

* `models.AccessMode` was added

* `models.PrivateLinkResources` was added

* `models.CreateEmailSignInUrlResponse` was added

* `models.PrivateEndpointConnection$DefinitionStages` was added

* `models.ResourceAssociation` was added

* `models.PrivateEndpointConnections` was added

* `models.OperationResultStatus` was added

* `models.ProvisioningIssue` was added

* `models.QnAMakerEndpointKeysRequestBody` was added

* `models.ProvisioningIssueProperties` was added

* `models.Severity` was added

* `models.PrivateLinkServiceConnectionState` was added

* `models.PrivateEndpointConnectionProvisioningState` was added

* `models.PrivateLinkResource` was added

* `models.CreateEmailSignInUrlResponseProperties` was added

* `models.PrivateEndpoint` was added

* `models.PrivateEndpointConnection` was added

* `models.PrivateEndpointServiceConnectionStatus` was added

* `models.PrivateLinkResourceBase` was added

* `models.Emails` was added

* `models.PrivateEndpointConnection$Definition` was added

* `models.NetworkSecurityPerimeter` was added

* `models.NspAccessRulePropertiesSubscriptionsItem` was added

* `models.NspAccessRuleProperties` was added

* `models.NetworkSecurityPerimeterConfigurationProperties` was added

* `models.Profile` was added

* `models.QnAMakerEndpointKeys` was added

* `models.OperationResults` was added

* `models.NetworkSecurityPerimeterConfigurations` was added

* `models.NspAccessRule` was added

* `models.NetworkSecurityPerimeterConfiguration` was added

* `models.PrivateLinkResourceListResult` was added

#### `models.BotChannel` was modified

* `systemData()` was added

#### `models.TelephonyChannelProperties` was modified

* `withPremiumSKU(java.lang.String)` was added
* `premiumSKU()` was added

#### `models.Bot` was modified

* `systemData()` was added

#### `models.SmsChannelProperties` was modified

* `withAccountSID(java.lang.String)` was added
* `accountSID()` was added

#### `models.ListChannelWithKeysResponse` was modified

* `systemData()` was added

#### `models.BotProperties` was modified

* `networkSecurityPerimeterConfigurations()` was added
* `withMsaAppMSIResourceId(java.lang.String)` was added
* `msaAppMSIResourceId()` was added
* `privateEndpointConnections()` was added

#### `models.Site` was modified

* `withETag(java.lang.String)` was added
* `eTag()` was added

#### `models.ConnectionSettingProperties` was modified

* `withId(java.lang.String)` was added
* `name()` was added
* `withName(java.lang.String)` was added
* `id()` was added

#### `models.SkypeChannelProperties` was modified

* `callingWebHook()` was added
* `withCallingWebHook(java.lang.String)` was added

#### `models.KikChannelProperties` was modified

* `withUserName(java.lang.String)` was added
* `userName()` was added

#### `models.DirectLineSite` was modified

* `withETag(java.lang.String)` was added

#### `models.SlackChannelProperties` was modified

* `withRegisterBeforeOAuthFlow(java.lang.Boolean)` was added

#### `models.ConnectionSetting` was modified

* `systemData()` was added

#### `models.WebChatSite` was modified

* `withETag(java.lang.String)` was added

#### `models.EmailChannelAuthMethod` was modified

* `fromInt(int)` was added
* `toInt()` was added

#### `BotServiceManager` was modified

* `qnAMakerEndpointKeys()` was added
* `privateEndpointConnections()` was added
* `operationResults()` was added
* `privateLinkResources()` was added
* `emails()` was added
* `networkSecurityPerimeterConfigurations()` was added

## 1.0.0 (2025-01-02)

- Azure Resource Manager BotService client library for Java. This package contains Microsoft Azure SDK for BotService Management SDK. Azure Bot Service is a platform for creating smart conversational agents. Package tag package-2021-03-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Other Changes

- Release for Azure Resource Manager BotService client library for Java.

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

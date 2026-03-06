# Release History

## 1.2.0-beta.1 (2026-03-06)

- Azure Resource Manager Microsoft Datadog client library for Java. This package contains Microsoft Azure SDK for Microsoft Datadog Management SDK.  Package api-version 2025-12-26-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.OperationListResult` was removed

#### `models.DatadogMonitorResourceListResponse` was removed

#### `models.MonitoredSubscriptionPropertiesList` was removed

#### `models.MonitoringTagRulesListResponse` was removed

#### `models.MonitoredResourceListResponse` was removed

#### `models.DatadogSingleSignOnResourceListResponse` was removed

#### `models.DatadogApiKeyListResponse` was removed

#### `models.CreateResourceSupportedResponseList` was removed

#### `models.DatadogHostListResponse` was removed

#### `models.LinkedResourceListResponse` was removed

#### `models.DatadogAgreementResourceListResponse` was removed

#### `models.DatadogMonitorResourceUpdateParameters` was modified

* `validate()` was removed

#### `models.MetricRules` was modified

* `validate()` was removed

#### `models.DatadogHostMetadata` was modified

* `DatadogHostMetadata()` was changed to private access
* `validate()` was removed
* `withAgentVersion(java.lang.String)` was removed
* `withLogsAgent(models.DatadogLogsAgent)` was removed
* `withInstallMethod(models.DatadogInstallMethod)` was removed

#### `models.DatadogLogsAgent` was modified

* `DatadogLogsAgent()` was changed to private access
* `withTransport(java.lang.String)` was removed
* `validate()` was removed

#### `models.MonitorProperties` was modified

* `validate()` was removed

#### `models.DatadogSingleSignOnProperties` was modified

* `validate()` was removed

#### `models.DatadogAgreementProperties` was modified

* `validate()` was removed

#### `models.IdentityProperties` was modified

* `validate()` was removed

#### `MicrosoftDatadogManager` was modified

* `fluent.MicrosoftDatadogClient serviceClient()` -> `fluent.MicrosoftDatadogManagementClient serviceClient()`

#### `models.UserInfo` was modified

* `validate()` was removed

#### `models.DatadogOrganizationProperties` was modified

* `validate()` was removed

#### `models.MonitorUpdateProperties` was modified

* `validate()` was removed

#### `models.OperationDisplay` was modified

* `OperationDisplay()` was changed to private access
* `validate()` was removed
* `withResource(java.lang.String)` was removed
* `withOperation(java.lang.String)` was removed
* `withProvider(java.lang.String)` was removed
* `withDescription(java.lang.String)` was removed

#### `models.LogRules` was modified

* `validate()` was removed

#### `models.MonitoringTagRulesProperties` was modified

* `validate()` was removed

#### `models.DatadogInstallMethod` was modified

* `DatadogInstallMethod()` was changed to private access
* `validate()` was removed
* `withInstallerVersion(java.lang.String)` was removed
* `withToolVersion(java.lang.String)` was removed
* `withTool(java.lang.String)` was removed

#### `models.FilteringTag` was modified

* `validate()` was removed

#### `models.CreateResourceSupportedProperties` was modified

* `CreateResourceSupportedProperties()` was changed to private access
* `validate()` was removed

#### `models.SubscriptionList` was modified

* `validate()` was removed

#### `models.MonitoredSubscription` was modified

* `validate()` was removed

#### `models.ResourceSku` was modified

* `validate()` was removed

### Features Added

* `models.ConnectorAction` was added

* `models.LatestLinkedSaaSResponse` was added

* `models.AgentRules` was added

* `models.SaaSResourceDetailsResponse` was added

* `models.BillingInfoResponse` was added

* `models.ResubscribeProperties` was added

* `models.PartnerBillingEntity` was added

* `models.SreAgentConnectorRequest` was added

* `models.DatadogApplicationKey` was added

* `models.MarketplaceOfferDetails` was added

* `models.ActivateSaaSParameterRequest` was added

* `models.SaaSData` was added

* `models.SreAgentConfigurationListResponse` was added

* `models.DatadogMonitorResources` was added

* `models.SreAgentConfiguration` was added

* `models.Organizations` was added

* `models.SaaSOperationGroups` was added

* `models.BillingInfoes` was added

* `models.MarketplaceSaaSInfo` was added

#### `models.MonitorProperties` was modified

* `saaSData()` was added
* `sreAgentConfiguration()` was added
* `withMarketplaceOfferDetails(models.MarketplaceOfferDetails)` was added
* `withSaaSData(models.SaaSData)` was added
* `marketplaceOfferDetails()` was added
* `withSreAgentConfiguration(java.util.List)` was added

#### `models.DatadogMonitorResource` was modified

* `getDefaultApplicationKey()` was added
* `manageSreAgentConnectorsWithResponse(models.SreAgentConnectorRequest,com.azure.core.util.Context)` was added
* `manageSreAgentConnectors(models.SreAgentConnectorRequest)` was added
* `getDefaultApplicationKeyWithResponse(com.azure.core.util.Context)` was added

#### `models.MonitoredSubscriptionProperties` was modified

* `systemData()` was added

#### `MicrosoftDatadogManager` was modified

* `organizations()` was added
* `billingInfoes()` was added
* `saaSOperationGroups()` was added
* `datadogMonitorResources()` was added

#### `models.DatadogOrganizationProperties` was modified

* `resourceCollection()` was added
* `withResourceCollection(java.lang.Boolean)` was added

#### `models.MonitorUpdateProperties` was modified

* `withResourceCollection(java.lang.Boolean)` was added
* `resourceCollection()` was added

#### `models.Monitors` was modified

* `getDefaultApplicationKeyWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `manageSreAgentConnectors(java.lang.String,java.lang.String,models.SreAgentConnectorRequest)` was added
* `getDefaultApplicationKey(java.lang.String,java.lang.String)` was added
* `manageSreAgentConnectorsWithResponse(java.lang.String,java.lang.String,models.SreAgentConnectorRequest,com.azure.core.util.Context)` was added

#### `models.LinkedResource` was modified

* `location()` was added

#### `models.MonitoringTagRulesProperties` was modified

* `withCustomMetrics(java.lang.Boolean)` was added
* `agentRules()` was added
* `withAgentRules(models.AgentRules)` was added
* `customMetrics()` was added

## 1.1.0 (2024-12-11)

- Azure Resource Manager MicrosoftDatadog client library for Java. This package contains Microsoft Azure SDK for MicrosoftDatadog Management SDK.  Package tag package-2023-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

## 1.0.0 (2023-10-23)

- Azure Resource Manager MicrosoftDatadog client library for Java. This package contains Microsoft Azure SDK for MicrosoftDatadog Management SDK.  Package tag package-2023-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.CreateResourceSupportedResponse` was added

* `models.MonitoredSubscriptionProperties$UpdateStages` was added

* `models.CreationSupporteds` was added

* `models.Operation` was added

* `models.MonitoredSubscriptionProperties` was added

* `models.MonitoredSubscriptionPropertiesList` was added

* `models.MonitoredSubscriptionProperties$DefinitionStages` was added

* `models.CreateResourceSupportedResponseList` was added

* `models.MonitoredSubscriptions` was added

* `models.MonitoredSubscriptionProperties$Definition` was added

* `models.MonitoredSubscriptionProperties$Update` was added

* `models.CreateResourceSupportedProperties` was added

* `models.SubscriptionList` was added

* `models.MonitoredSubscription` was added

* `models.Status` was added

#### `MicrosoftDatadogManager` was modified

* `creationSupporteds()` was added
* `monitoredSubscriptions()` was added

#### `models.DatadogOrganizationProperties` was modified

* `withName(java.lang.String)` was added
* `withCspm(java.lang.Boolean)` was added
* `withId(java.lang.String)` was added
* `cspm()` was added

#### `models.MonitorUpdateProperties` was modified

* `cspm()` was added
* `withCspm(java.lang.Boolean)` was added

#### `models.MonitoringTagRulesProperties` was modified

* `automuting()` was added
* `withAutomuting(java.lang.Boolean)` was added

## 1.0.0-beta.4 (2023-01-16)

- Azure Resource Manager MicrosoftDatadog client library for Java. This package contains Microsoft Azure SDK for MicrosoftDatadog Management SDK.  Package tag package-2021-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `MicrosoftDatadogManager$Configurable` was modified

* `withScope(java.lang.String)` was added
* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.DatadogMonitorResource` was modified

* `resourceGroupName()` was added

#### `models.DatadogSingleSignOnResource` was modified

* `resourceGroupName()` was added

#### `models.MonitoringTagRules` was modified

* `resourceGroupName()` was added

#### `MicrosoftDatadogManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

## 1.0.0-beta.3 (2021-05-31)

- Azure Resource Manager MicrosoftDatadog client library for Java. This package contains Microsoft Azure SDK for MicrosoftDatadog Management SDK.  Package tag package-2021-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Change

#### `models.MonitorProperties` was modified

* `withProvisioningState(models.ProvisioningState)` was removed

#### `models.DatadogSingleSignOnProperties` was modified

* `withProvisioningState(models.ProvisioningState)` was removed

#### `models.MonitoringTagRulesProperties` was modified

* `withProvisioningState(models.ProvisioningState)` was removed

### New Feature

#### `models.DatadogMonitorResourceUpdateParameters` was modified

* `withSku(models.ResourceSku)` was added
* `sku()` was added

#### `models.DatadogMonitorResource$Update` was modified

* `withSku(models.ResourceSku)` was added

## 1.0.0-beta.2 (2021-03-30)

- Azure Resource Manager MicrosoftDatadog client library for Java. This package contains Microsoft Azure SDK for MicrosoftDatadog Management SDK.  Package tag package-2021-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### New Feature

#### `models.DatadogMonitorResource` was modified

* `systemData()` was added

#### `models.DatadogSingleSignOnResource` was modified

* `systemData()` was added

#### `models.MonitoringTagRules` was modified

* `systemData()` was added

#### `models.DatadogAgreementResource` was modified

* `systemData()` was added

## 1.0.0-beta.1 (2021-03-08)

- Azure Resource Manager MicrosoftDatadog client library for Java. This package contains Microsoft Azure SDK for MicrosoftDatadog Management SDK.  Package tag package-2020-02-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).


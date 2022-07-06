# Release History

## 1.2.0-beta.3 (2022-07-06)

- Azure Resource Manager EventGrid client library for Java. This package contains Microsoft Azure SDK for EventGrid Management SDK. Azure EventGrid Management Client. Package tag package-2022-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.PartnerDestinationInfo` was removed

* `models.PartnerUpdateDestinationInfo` was removed

* `models.EventChannel` was removed

* `models.Sku` was removed

* `models.PartnerDestination$Definition` was removed

* `models.ResourceSku` was removed

* `models.ResourceKind` was removed

* `models.PartnerClientAuthentication` was removed

* `models.EventChannelSource` was removed

* `models.PartnerEndpointType` was removed

* `models.ParentType` was removed

* `models.PartnerDestinationProvisioningState` was removed

* `models.TopicTypePropertiesSupportedScopesForSourceItem` was removed

* `models.PartnerDestinations` was removed

* `models.PartnerDestination` was removed

* `models.EventChannelFilter` was removed

* `models.ResourceMoveChangeHistory` was removed

* `models.EventChannel$UpdateStages` was removed

* `models.WebhookPartnerDestinationInfo` was removed

* `models.PartnerDestination$Update` was removed

* `models.PartnerEventSubscriptionDestination` was removed

* `models.EventChannel$Definition` was removed

* `models.EventChannels` was removed

* `models.EventChannelDestination` was removed

* `models.PartnerDestinationUpdateParameters` was removed

* `models.EventChannelProvisioningState` was removed

* `models.PartnerClientAuthenticationType` was removed

* `models.EventChannel$DefinitionStages` was removed

* `models.PartnerDestinationActivationState` was removed

* `models.PartnerTopicReadinessState` was removed

* `models.EventChannel$Update` was removed

* `models.WebhookUpdatePartnerDestinationInfo` was removed

* `models.EventChannelsListResult` was removed

* `models.PartnerRegistrationVisibilityState` was removed

* `models.AzureADPartnerClientAuthentication` was removed

* `models.PartnerDestination$UpdateStages` was removed

* `models.ExtendedLocation` was removed

* `models.PartnerDestination$DefinitionStages` was removed

* `models.PartnerDestinationsListResult` was removed

#### `models.EventSubscription$DefinitionStages` was modified

* `withExistingScope(java.lang.String)` was removed in stage 1

#### `models.Channel` was modified

* `partnerDestinationInfo()` was removed

#### `models.EventSubscription$Definition` was modified

* `withExistingScope(java.lang.String)` was removed

#### `models.TopicUpdateParameters` was modified

* `sku()` was removed
* `withSku(models.ResourceSku)` was removed

#### `models.Domain$Update` was modified

* `withSku(models.ResourceSku)` was removed

#### `models.Topic$Definition` was modified

* `withExtendedLocation(models.ExtendedLocation)` was removed
* `withSku(models.ResourceSku)` was removed
* `withKind(models.ResourceKind)` was removed

#### `models.DomainUpdateParameters` was modified

* `sku()` was removed
* `withSku(models.ResourceSku)` was removed

#### `models.PartnerRegistration$Definition` was modified

* `withPartnerResourceTypeName(java.lang.String)` was removed
* `withPartnerResourceTypeDescription(java.lang.String)` was removed
* `withCustomerServiceUri(java.lang.String)` was removed
* `withSetupUri(java.lang.String)` was removed
* `withPartnerCustomerServiceExtension(java.lang.String)` was removed
* `withPartnerCustomerServiceNumber(java.lang.String)` was removed
* `withPartnerResourceTypeDisplayName(java.lang.String)` was removed
* `withPartnerName(java.lang.String)` was removed
* `withLongDescription(java.lang.String)` was removed
* `withLogoUri(java.lang.String)` was removed
* `withAuthorizedAzureSubscriptionIds(java.util.List)` was removed
* `withVisibilityState(models.PartnerRegistrationVisibilityState)` was removed

#### `models.Topic$Update` was modified

* `withSku(models.ResourceSku)` was removed

#### `models.Domain` was modified

* `sku()` was removed

#### `models.PartnerRegistration` was modified

* `partnerCustomerServiceNumber()` was removed
* `visibilityState()` was removed
* `partnerName()` was removed
* `longDescription()` was removed
* `customerServiceUri()` was removed
* `logoUri()` was removed
* `authorizedAzureSubscriptionIds()` was removed
* `partnerResourceTypeDescription()` was removed
* `partnerResourceTypeName()` was removed
* `partnerCustomerServiceExtension()` was removed
* `setupUri()` was removed
* `partnerResourceTypeDisplayName()` was removed

#### `models.Channel$Update` was modified

* `withPartnerDestinationInfo(models.PartnerDestinationInfo)` was removed

#### `models.EventSubscriptions` was modified

* `define(java.lang.String)` was removed
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `deleteById(java.lang.String)` was removed
* `getById(java.lang.String)` was removed
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed

#### `EventGridManager` was modified

* `partnerDestinations()` was removed
* `eventChannels()` was removed

#### `models.TopicEventSubscriptions` was modified

* `update(java.lang.String,java.lang.String,java.lang.String,models.EventSubscriptionUpdateParameters)` was removed
* `createOrUpdate(java.lang.String,java.lang.String,java.lang.String,fluent.models.EventSubscriptionInner,com.azure.core.util.Context)` was removed
* `createOrUpdate(java.lang.String,java.lang.String,java.lang.String,fluent.models.EventSubscriptionInner)` was removed
* `update(java.lang.String,java.lang.String,java.lang.String,models.EventSubscriptionUpdateParameters,com.azure.core.util.Context)` was removed

#### `models.Topic` was modified

* `extendedLocation()` was removed
* `sku()` was removed
* `kind()` was removed

#### `models.PartnerRegistration$Update` was modified

* `withPartnerTopicTypeDescription(java.lang.String)` was removed
* `withPartnerTopicTypeDisplayName(java.lang.String)` was removed
* `withAuthorizedAzureSubscriptionIds(java.util.List)` was removed
* `withLogoUri(java.lang.String)` was removed
* `withPartnerTopicTypeName(java.lang.String)` was removed
* `withSetupUri(java.lang.String)` was removed

#### `models.Channel$Definition` was modified

* `withPartnerDestinationInfo(models.PartnerDestinationInfo)` was removed

#### `models.PrivateEndpointConnections` was modified

* `update(java.lang.String,models.ParentType,java.lang.String,java.lang.String,fluent.models.PrivateEndpointConnectionInner,com.azure.core.util.Context)` was removed
* `getWithResponse(java.lang.String,models.ParentType,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `listByResource(java.lang.String,models.ParentType,java.lang.String)` was removed
* `get(java.lang.String,models.ParentType,java.lang.String,java.lang.String)` was removed
* `update(java.lang.String,models.ParentType,java.lang.String,java.lang.String,fluent.models.PrivateEndpointConnectionInner)` was removed
* `delete(java.lang.String,models.ParentType,java.lang.String,java.lang.String)` was removed
* `delete(java.lang.String,models.ParentType,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `listByResource(java.lang.String,models.ParentType,java.lang.String,java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was removed

#### `models.PartnerRegistrationUpdateParameters` was modified

* `withPartnerTopicTypeDisplayName(java.lang.String)` was removed
* `withPartnerTopicTypeDescription(java.lang.String)` was removed
* `partnerTopicTypeDescription()` was removed
* `withSetupUri(java.lang.String)` was removed
* `setupUri()` was removed
* `authorizedAzureSubscriptionIds()` was removed
* `logoUri()` was removed
* `partnerTopicTypeDisplayName()` was removed
* `partnerTopicTypeName()` was removed
* `withPartnerTopicTypeName(java.lang.String)` was removed
* `withLogoUri(java.lang.String)` was removed
* `withAuthorizedAzureSubscriptionIds(java.util.List)` was removed

#### `models.ChannelUpdateParameters` was modified

* `partnerDestinationInfo()` was removed
* `withPartnerDestinationInfo(models.PartnerUpdateDestinationInfo)` was removed

#### `models.Domain$Definition` was modified

* `withSku(models.ResourceSku)` was removed

#### `models.VerifiedPartner` was modified

* `partnerDestinationDetails()` was removed

### Features Added

* `models.PrivateEndpointConnectionsParentType` was added

* `models.TopicTypeSourceScope` was added

#### `models.InlineEventProperties` was modified

* `displayName()` was added
* `withDisplayName(java.lang.String)` was added

#### `models.EventSubscription$Definition` was modified

* `withExistingTopic(java.lang.String,java.lang.String)` was added

#### `models.EventSubscription` was modified

* `resourceGroupName()` was added

#### `models.EventSubscriptions` was modified

* `createOrUpdate(java.lang.String,java.lang.String,fluent.models.EventSubscriptionInner)` was added
* `update(java.lang.String,java.lang.String,models.EventSubscriptionUpdateParameters,com.azure.core.util.Context)` was added
* `update(java.lang.String,java.lang.String,models.EventSubscriptionUpdateParameters)` was added
* `createOrUpdate(java.lang.String,java.lang.String,fluent.models.EventSubscriptionInner,com.azure.core.util.Context)` was added

#### `models.TopicEventSubscriptions` was modified

* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `getById(java.lang.String)` was added
* `deleteById(java.lang.String)` was added
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `define(java.lang.String)` was added

#### `models.PrivateEndpointConnections` was modified

* `getWithResponse(java.lang.String,models.PrivateEndpointConnectionsParentType,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `get(java.lang.String,models.PrivateEndpointConnectionsParentType,java.lang.String,java.lang.String)` was added
* `update(java.lang.String,models.PrivateEndpointConnectionsParentType,java.lang.String,java.lang.String,fluent.models.PrivateEndpointConnectionInner,com.azure.core.util.Context)` was added
* `listByResource(java.lang.String,models.PrivateEndpointConnectionsParentType,java.lang.String)` was added
* `listByResource(java.lang.String,models.PrivateEndpointConnectionsParentType,java.lang.String,java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was added
* `delete(java.lang.String,models.PrivateEndpointConnectionsParentType,java.lang.String,java.lang.String)` was added
* `update(java.lang.String,models.PrivateEndpointConnectionsParentType,java.lang.String,java.lang.String,fluent.models.PrivateEndpointConnectionInner)` was added
* `delete(java.lang.String,models.PrivateEndpointConnectionsParentType,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

## 1.2.0-beta.2 (2022-05-13)

- Azure Resource Manager EventGrid client library for Java. This package contains Microsoft Azure SDK for EventGrid Management SDK. Azure EventGrid Management Client. Package tag package-2021-10-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.PrivateEndpointConnectionsParentType` was removed

#### `models.PrivateEndpointConnections` was modified

* `listByResource(java.lang.String,models.PrivateEndpointConnectionsParentType,java.lang.String)` was removed
* `get(java.lang.String,models.PrivateEndpointConnectionsParentType,java.lang.String,java.lang.String)` was removed
* `update(java.lang.String,models.PrivateEndpointConnectionsParentType,java.lang.String,java.lang.String,fluent.models.PrivateEndpointConnectionInner)` was removed
* `getWithResponse(java.lang.String,models.PrivateEndpointConnectionsParentType,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `update(java.lang.String,models.PrivateEndpointConnectionsParentType,java.lang.String,java.lang.String,fluent.models.PrivateEndpointConnectionInner,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,models.PrivateEndpointConnectionsParentType,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,models.PrivateEndpointConnectionsParentType,java.lang.String,java.lang.String)` was removed
* `listByResource(java.lang.String,models.PrivateEndpointConnectionsParentType,java.lang.String,java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was removed

### Features Added

* `models.PartnerDestinationInfo` was added

* `models.PartnerUpdateDestinationInfo` was added

* `models.Channels` was added

* `models.PartnerNamespaceSharedAccessKeys` was added

* `models.InlineEventProperties` was added

* `models.EventChannel` was added

* `models.Sku` was added

* `models.Channel` was added

* `models.PartnerNamespace` was added

* `models.PartnerConfiguration` was added

* `models.Channel$UpdateStages` was added

* `models.PartnerRegistration$UpdateStages` was added

* `models.PartnerConfigurationUpdateParameters` was added

* `models.PartnerDestination$Definition` was added

* `models.PartnerTopic$Definition` was added

* `models.DataResidencyBoundary` was added

* `models.ResourceSku` was added

* `models.ResourceKind` was added

* `models.PartnerClientAuthentication` was added

* `models.EventChannelSource` was added

* `models.PartnerEndpointType` was added

* `models.PartnerConfigurations` was added

* `models.PartnerRegistration$Definition` was added

* `models.VerifiedPartners` was added

* `models.PartnerTopicActivationState` was added

* `models.ParentType` was added

* `models.PartnerDestinationProvisioningState` was added

* `models.PartnerRegistrationProvisioningState` was added

* `models.DomainEventSubscriptions` was added

* `models.PartnerTopics` was added

* `models.ChannelProvisioningState` was added

* `models.ChannelsListResult` was added

* `models.PartnerDestinations` was added

* `models.PartnerDestination` was added

* `models.PartnerRegistration` was added

* `models.PartnerTopicRoutingMode` was added

* `models.PartnerTopicInfo` was added

* `models.PartnerNamespace$DefinitionStages` was added

* `models.Channel$Update` was added

* `models.EventChannelFilter` was added

* `models.ResourceMoveChangeHistory` was added

* `models.PartnerTopicProvisioningState` was added

* `models.EventChannel$UpdateStages` was added

* `models.PartnerTopic` was added

* `models.EventDefinitionKind` was added

* `models.WebhookPartnerDestinationInfo` was added

* `models.PartnerNamespace$Definition` was added

* `models.PartnerNamespaceRegenerateKeyRequest` was added

* `models.PartnerAuthorization` was added

* `models.PartnerDestination$Update` was added

* `models.VerifiedPartnersListResult` was added

* `models.PartnerConfigurationProvisioningState` was added

* `models.PartnerNamespaces` was added

* `models.TopicEventSubscriptions` was added

* `models.DomainTopicEventSubscriptions` was added

* `models.PartnerConfigurationsListResult` was added

* `models.PartnerUpdateTopicInfo` was added

* `models.PartnerEventSubscriptionDestination` was added

* `models.EventChannel$Definition` was added

* `models.PartnerRegistration$Update` was added

* `models.EventChannels` was added

* `models.EventChannelDestination` was added

* `models.PartnerTopicUpdateParameters` was added

* `models.PartnerDetails` was added

* `models.PartnerDestinationUpdateParameters` was added

* `models.PartnerRegistration$DefinitionStages` was added

* `models.EventChannelProvisioningState` was added

* `models.PartnerTopicEventSubscriptions` was added

* `models.PartnerClientAuthenticationType` was added

* `models.PartnerNamespace$Update` was added

* `models.EventChannel$DefinitionStages` was added

* `models.PartnerDestinationActivationState` was added

* `models.PartnerTopic$Update` was added

* `models.EventTypeInfo` was added

* `models.PartnerTopicReadinessState` was added

* `models.PartnerNamespacesListResult` was added

* `models.EventChannel$Update` was added

* `models.WebhookUpdatePartnerDestinationInfo` was added

* `models.PartnerTopic$DefinitionStages` was added

* `models.ReadinessState` was added

* `models.PartnerNamespaceUpdateParameters` was added

* `models.Channel$Definition` was added

* `models.PartnerTopic$UpdateStages` was added

* `models.PartnerRegistrationsListResult` was added

* `models.PartnerRegistrationUpdateParameters` was added

* `models.Channel$DefinitionStages` was added

* `models.VerifiedPartnerProvisioningState` was added

* `models.EventChannelsListResult` was added

* `models.PartnerRegistrationVisibilityState` was added

* `models.AzureADPartnerClientAuthentication` was added

* `models.ChannelType` was added

* `models.ChannelUpdateParameters` was added

* `models.PartnerNamespaceProvisioningState` was added

* `models.PartnerTopicsListResult` was added

* `models.PartnerDestination$UpdateStages` was added

* `models.ExtendedLocation` was added

* `models.PartnerDestination$DefinitionStages` was added

* `models.PartnerRegistrations` was added

* `models.VerifiedPartner` was added

* `models.PartnerNamespace$UpdateStages` was added

* `models.Partner` was added

* `models.PartnerDestinationsListResult` was added

#### `models.SystemTopic` was modified

* `resourceGroupName()` was added

#### `models.TopicUpdateParameters` was modified

* `withSku(models.ResourceSku)` was added
* `dataResidencyBoundary()` was added
* `sku()` was added
* `withDataResidencyBoundary(models.DataResidencyBoundary)` was added

#### `models.Domain$Update` was modified

* `withDataResidencyBoundary(models.DataResidencyBoundary)` was added
* `withSku(models.ResourceSku)` was added

#### `models.Topic$Definition` was modified

* `withSku(models.ResourceSku)` was added
* `withDataResidencyBoundary(models.DataResidencyBoundary)` was added
* `withKind(models.ResourceKind)` was added
* `withExtendedLocation(models.ExtendedLocation)` was added

#### `models.DomainUpdateParameters` was modified

* `dataResidencyBoundary()` was added
* `withSku(models.ResourceSku)` was added
* `sku()` was added
* `withDataResidencyBoundary(models.DataResidencyBoundary)` was added

#### `models.Topic$Update` was modified

* `withSku(models.ResourceSku)` was added
* `withDataResidencyBoundary(models.DataResidencyBoundary)` was added

#### `models.Domain` was modified

* `resourceGroupName()` was added
* `sku()` was added
* `dataResidencyBoundary()` was added

#### `EventGridManager` was modified

* `domainTopicEventSubscriptions()` was added
* `partnerTopics()` was added
* `partnerTopicEventSubscriptions()` was added
* `verifiedPartners()` was added
* `channels()` was added
* `topicEventSubscriptions()` was added
* `partnerRegistrations()` was added
* `eventChannels()` was added
* `partnerNamespaces()` was added
* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added
* `domainEventSubscriptions()` was added
* `partnerConfigurations()` was added
* `partnerDestinations()` was added

#### `models.Topic` was modified

* `sku()` was added
* `dataResidencyBoundary()` was added
* `extendedLocation()` was added
* `kind()` was added
* `resourceGroupName()` was added

#### `models.Operation` was modified

* `isDataAction()` was added

#### `models.PrivateEndpointConnections` was modified

* `delete(java.lang.String,models.ParentType,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `get(java.lang.String,models.ParentType,java.lang.String,java.lang.String)` was added
* `update(java.lang.String,models.ParentType,java.lang.String,java.lang.String,fluent.models.PrivateEndpointConnectionInner)` was added
* `delete(java.lang.String,models.ParentType,java.lang.String,java.lang.String)` was added
* `getWithResponse(java.lang.String,models.ParentType,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listByResource(java.lang.String,models.ParentType,java.lang.String)` was added
* `update(java.lang.String,models.ParentType,java.lang.String,java.lang.String,fluent.models.PrivateEndpointConnectionInner,com.azure.core.util.Context)` was added
* `listByResource(java.lang.String,models.ParentType,java.lang.String,java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was added

#### `models.Domain$Definition` was modified

* `withSku(models.ResourceSku)` was added
* `withDataResidencyBoundary(models.DataResidencyBoundary)` was added

#### `EventGridManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

## 1.2.0-beta.1 (2022-03-28)

- Azure Resource Manager EventGrid client library for Java. This package contains Microsoft Azure SDK for EventGrid Management SDK. Azure EventGrid Management Client. Package tag package-2021-10-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.PrivateEndpointConnectionsParentType` was removed

#### `models.PrivateEndpointConnections` was modified

* `listByResource(java.lang.String,models.PrivateEndpointConnectionsParentType,java.lang.String)` was removed
* `update(java.lang.String,models.PrivateEndpointConnectionsParentType,java.lang.String,java.lang.String,fluent.models.PrivateEndpointConnectionInner,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,models.PrivateEndpointConnectionsParentType,java.lang.String,java.lang.String)` was removed
* `listByResource(java.lang.String,models.PrivateEndpointConnectionsParentType,java.lang.String,java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was removed
* `update(java.lang.String,models.PrivateEndpointConnectionsParentType,java.lang.String,java.lang.String,fluent.models.PrivateEndpointConnectionInner)` was removed
* `get(java.lang.String,models.PrivateEndpointConnectionsParentType,java.lang.String,java.lang.String)` was removed
* `getWithResponse(java.lang.String,models.PrivateEndpointConnectionsParentType,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,models.PrivateEndpointConnectionsParentType,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

* `models.PartnerDestinationInfo` was added

* `models.PartnerUpdateDestinationInfo` was added

* `models.Channels` was added

* `models.PartnerNamespaceSharedAccessKeys` was added

* `models.InlineEventProperties` was added

* `models.EventChannel` was added

* `models.Sku` was added

* `models.Channel` was added

* `models.PartnerNamespace` was added

* `models.PartnerConfiguration` was added

* `models.Channel$UpdateStages` was added

* `models.PartnerRegistration$UpdateStages` was added

* `models.PartnerConfigurationUpdateParameters` was added

* `models.PartnerDestination$Definition` was added

* `models.PartnerTopic$Definition` was added

* `models.DataResidencyBoundary` was added

* `models.ResourceSku` was added

* `models.ResourceKind` was added

* `models.PartnerClientAuthentication` was added

* `models.EventChannelSource` was added

* `models.PartnerEndpointType` was added

* `models.PartnerConfigurations` was added

* `models.PartnerRegistration$Definition` was added

* `models.VerifiedPartners` was added

* `models.PartnerTopicActivationState` was added

* `models.ParentType` was added

* `models.PartnerDestinationProvisioningState` was added

* `models.PartnerRegistrationProvisioningState` was added

* `models.DomainEventSubscriptions` was added

* `models.PartnerTopics` was added

* `models.ChannelProvisioningState` was added

* `models.ChannelsListResult` was added

* `models.PartnerDestinations` was added

* `models.PartnerDestination` was added

* `models.PartnerRegistration` was added

* `models.PartnerTopicRoutingMode` was added

* `models.PartnerTopicInfo` was added

* `models.PartnerNamespace$DefinitionStages` was added

* `models.Channel$Update` was added

* `models.EventChannelFilter` was added

* `models.ResourceMoveChangeHistory` was added

* `models.PartnerTopicProvisioningState` was added

* `models.EventChannel$UpdateStages` was added

* `models.PartnerTopic` was added

* `models.EventDefinitionKind` was added

* `models.WebhookPartnerDestinationInfo` was added

* `models.PartnerNamespace$Definition` was added

* `models.PartnerNamespaceRegenerateKeyRequest` was added

* `models.PartnerAuthorization` was added

* `models.PartnerDestination$Update` was added

* `models.VerifiedPartnersListResult` was added

* `models.PartnerConfigurationProvisioningState` was added

* `models.PartnerNamespaces` was added

* `models.TopicEventSubscriptions` was added

* `models.DomainTopicEventSubscriptions` was added

* `models.PartnerConfigurationsListResult` was added

* `models.PartnerUpdateTopicInfo` was added

* `models.PartnerEventSubscriptionDestination` was added

* `models.EventChannel$Definition` was added

* `models.PartnerRegistration$Update` was added

* `models.EventChannels` was added

* `models.EventChannelDestination` was added

* `models.PartnerTopicUpdateParameters` was added

* `models.PartnerDetails` was added

* `models.PartnerDestinationUpdateParameters` was added

* `models.PartnerRegistration$DefinitionStages` was added

* `models.EventChannelProvisioningState` was added

* `models.PartnerTopicEventSubscriptions` was added

* `models.PartnerClientAuthenticationType` was added

* `models.PartnerNamespace$Update` was added

* `models.EventChannel$DefinitionStages` was added

* `models.PartnerDestinationActivationState` was added

* `models.PartnerTopic$Update` was added

* `models.EventTypeInfo` was added

* `models.PartnerTopicReadinessState` was added

* `models.PartnerNamespacesListResult` was added

* `models.EventChannel$Update` was added

* `models.WebhookUpdatePartnerDestinationInfo` was added

* `models.PartnerTopic$DefinitionStages` was added

* `models.ReadinessState` was added

* `models.PartnerNamespaceUpdateParameters` was added

* `models.Channel$Definition` was added

* `models.PartnerTopic$UpdateStages` was added

* `models.PartnerRegistrationsListResult` was added

* `models.PartnerRegistrationUpdateParameters` was added

* `models.Channel$DefinitionStages` was added

* `models.VerifiedPartnerProvisioningState` was added

* `models.EventChannelsListResult` was added

* `models.PartnerRegistrationVisibilityState` was added

* `models.AzureADPartnerClientAuthentication` was added

* `models.ChannelType` was added

* `models.ChannelUpdateParameters` was added

* `models.PartnerNamespaceProvisioningState` was added

* `models.PartnerTopicsListResult` was added

* `models.PartnerDestination$UpdateStages` was added

* `models.ExtendedLocation` was added

* `models.PartnerDestination$DefinitionStages` was added

* `models.PartnerRegistrations` was added

* `models.VerifiedPartner` was added

* `models.PartnerNamespace$UpdateStages` was added

* `models.Partner` was added

* `models.PartnerDestinationsListResult` was added

#### `models.TopicUpdateParameters` was modified

* `sku()` was added
* `withDataResidencyBoundary(models.DataResidencyBoundary)` was added
* `withSku(models.ResourceSku)` was added
* `dataResidencyBoundary()` was added

#### `models.Domain$Update` was modified

* `withSku(models.ResourceSku)` was added
* `withDataResidencyBoundary(models.DataResidencyBoundary)` was added

#### `models.Topic$Definition` was modified

* `withExtendedLocation(models.ExtendedLocation)` was added
* `withDataResidencyBoundary(models.DataResidencyBoundary)` was added
* `withSku(models.ResourceSku)` was added
* `withKind(models.ResourceKind)` was added

#### `models.DomainUpdateParameters` was modified

* `dataResidencyBoundary()` was added
* `withSku(models.ResourceSku)` was added
* `withDataResidencyBoundary(models.DataResidencyBoundary)` was added
* `sku()` was added

#### `models.Topic$Update` was modified

* `withDataResidencyBoundary(models.DataResidencyBoundary)` was added
* `withSku(models.ResourceSku)` was added

#### `models.Domain` was modified

* `dataResidencyBoundary()` was added
* `sku()` was added

#### `EventGridManager` was modified

* `partnerRegistrations()` was added
* `partnerNamespaces()` was added
* `partnerTopicEventSubscriptions()` was added
* `eventChannels()` was added
* `channels()` was added
* `topicEventSubscriptions()` was added
* `partnerConfigurations()` was added
* `domainTopicEventSubscriptions()` was added
* `partnerTopics()` was added
* `verifiedPartners()` was added
* `domainEventSubscriptions()` was added
* `partnerDestinations()` was added

#### `models.Topic` was modified

* `dataResidencyBoundary()` was added
* `sku()` was added
* `kind()` was added
* `extendedLocation()` was added

#### `models.Operation` was modified

* `isDataAction()` was added

#### `models.PrivateEndpointConnections` was modified

* `listByResource(java.lang.String,models.ParentType,java.lang.String)` was added
* `listByResource(java.lang.String,models.ParentType,java.lang.String,java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was added
* `getWithResponse(java.lang.String,models.ParentType,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `update(java.lang.String,models.ParentType,java.lang.String,java.lang.String,fluent.models.PrivateEndpointConnectionInner,com.azure.core.util.Context)` was added
* `get(java.lang.String,models.ParentType,java.lang.String,java.lang.String)` was added
* `delete(java.lang.String,models.ParentType,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `delete(java.lang.String,models.ParentType,java.lang.String,java.lang.String)` was added
* `update(java.lang.String,models.ParentType,java.lang.String,java.lang.String,fluent.models.PrivateEndpointConnectionInner)` was added

#### `models.Domain$Definition` was modified

* `withDataResidencyBoundary(models.DataResidencyBoundary)` was added
* `withSku(models.ResourceSku)` was added

## 1.1.0 (2022-01-24)

- Azure Resource Manager EventGrid client library for Java. This package contains Microsoft Azure SDK for EventGrid Management SDK. Azure EventGrid Management Client. Package tag package-2021-12. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.ExtensionTopic` was added

* `models.SystemTopic` was added

* `models.EventSubscriptionIdentity` was added

* `models.StringNotBeginsWithAdvancedFilter` was added

* `models.DeadLetterWithResourceIdentity` was added

* `models.NumberInRangeAdvancedFilter` was added

* `models.IsNotNullAdvancedFilter` was added

* `models.SystemTopic$Definition` was added

* `models.UserIdentityProperties` was added

* `models.IsNullOrUndefinedAdvancedFilter` was added

* `models.NumberNotInRangeAdvancedFilter` was added

* `models.IdentityInfo` was added

* `models.SystemTopic$UpdateStages` was added

* `models.DeliveryAttributeListResult` was added

* `models.DeliveryAttributeMapping` was added

* `models.StringNotEndsWithAdvancedFilter` was added

* `models.SystemTopicUpdateParameters` was added

* `models.DeliveryAttributeMappingType` was added

* `models.IdentityType` was added

* `models.StaticDeliveryAttributeMapping` was added

* `models.DeliveryWithResourceIdentity` was added

* `models.TopicTypePropertiesSupportedScopesForSourceItem` was added

* `models.DynamicDeliveryAttributeMapping` was added

* `models.ExtensionTopics` was added

* `models.SystemTopicsListResult` was added

* `models.EventSubscriptionIdentityType` was added

* `models.StringNotContainsAdvancedFilter` was added

* `models.SystemTopic$Update` was added

* `models.SystemTopic$DefinitionStages` was added

* `models.SystemTopics` was added

* `models.SystemTopicEventSubscriptions` was added

#### `models.Topic` was modified

* `identity()` was added
* `disableLocalAuth()` was added

#### `models.EventSubscriptionFilter` was modified

* `enableAdvancedFilteringOnArrays()` was added
* `withEnableAdvancedFilteringOnArrays(java.lang.Boolean)` was added

#### `models.WebhookEventSubscriptionDestination` was modified

* `deliveryAttributeMappings()` was added
* `withDeliveryAttributeMappings(java.util.List)` was added

#### `models.StorageQueueEventSubscriptionDestination` was modified

* `queueMessageTimeToLiveInSeconds()` was added
* `withQueueMessageTimeToLiveInSeconds(java.lang.Long)` was added

#### `models.EventSubscription$Definition` was modified

* `withDeadLetterWithResourceIdentity(models.DeadLetterWithResourceIdentity)` was added
* `withDeliveryWithResourceIdentity(models.DeliveryWithResourceIdentity)` was added

#### `models.ServiceBusQueueEventSubscriptionDestination` was modified

* `deliveryAttributeMappings()` was added
* `withDeliveryAttributeMappings(java.util.List)` was added

#### `models.TopicUpdateParameters` was modified

* `disableLocalAuth()` was added
* `withIdentity(models.IdentityInfo)` was added
* `identity()` was added
* `withDisableLocalAuth(java.lang.Boolean)` was added

#### `models.Domain$Update` was modified

* `withAutoDeleteTopicWithLastSubscription(java.lang.Boolean)` was added
* `withAutoCreateTopicWithFirstSubscription(java.lang.Boolean)` was added
* `withDisableLocalAuth(java.lang.Boolean)` was added
* `withIdentity(models.IdentityInfo)` was added

#### `models.Topic$Definition` was modified

* `withDisableLocalAuth(java.lang.Boolean)` was added
* `withIdentity(models.IdentityInfo)` was added

#### `models.ServiceBusTopicEventSubscriptionDestination` was modified

* `deliveryAttributeMappings()` was added
* `withDeliveryAttributeMappings(java.util.List)` was added

#### `models.DomainUpdateParameters` was modified

* `identity()` was added
* `withAutoDeleteTopicWithLastSubscription(java.lang.Boolean)` was added
* `withIdentity(models.IdentityInfo)` was added
* `autoCreateTopicWithFirstSubscription()` was added
* `autoDeleteTopicWithLastSubscription()` was added
* `withAutoCreateTopicWithFirstSubscription(java.lang.Boolean)` was added
* `withDisableLocalAuth(java.lang.Boolean)` was added
* `disableLocalAuth()` was added

#### `models.Topic$Update` was modified

* `withDisableLocalAuth(java.lang.Boolean)` was added
* `withIdentity(models.IdentityInfo)` was added

#### `models.EventSubscription` was modified

* `getDeliveryAttributes()` was added
* `deadLetterWithResourceIdentity()` was added
* `deliveryWithResourceIdentity()` was added
* `getDeliveryAttributesWithResponse(com.azure.core.util.Context)` was added

#### `models.AzureFunctionEventSubscriptionDestination` was modified

* `deliveryAttributeMappings()` was added
* `withDeliveryAttributeMappings(java.util.List)` was added

#### `models.Domain` was modified

* `identity()` was added
* `disableLocalAuth()` was added
* `autoDeleteTopicWithLastSubscription()` was added
* `autoCreateTopicWithFirstSubscription()` was added

#### `models.EventSubscription$Update` was modified

* `withDeadLetterWithResourceIdentity(models.DeadLetterWithResourceIdentity)` was added
* `withDeliveryWithResourceIdentity(models.DeliveryWithResourceIdentity)` was added

#### `models.HybridConnectionEventSubscriptionDestination` was modified

* `withDeliveryAttributeMappings(java.util.List)` was added
* `deliveryAttributeMappings()` was added

#### `models.TopicTypeInfo` was modified

* `supportedScopesForSource()` was added

#### `models.EventHubEventSubscriptionDestination` was modified

* `deliveryAttributeMappings()` was added
* `withDeliveryAttributeMappings(java.util.List)` was added

#### `models.EventSubscriptions` was modified

* `getDeliveryAttributes(java.lang.String,java.lang.String)` was added
* `getDeliveryAttributesWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `EventGridManager` was modified

* `systemTopics()` was added
* `extensionTopics()` was added
* `systemTopicEventSubscriptions()` was added

#### `models.Domain$Definition` was modified

* `withAutoDeleteTopicWithLastSubscription(java.lang.Boolean)` was added
* `withAutoCreateTopicWithFirstSubscription(java.lang.Boolean)` was added
* `withIdentity(models.IdentityInfo)` was added
* `withDisableLocalAuth(java.lang.Boolean)` was added

#### `EventGridManager$Configurable` was modified

* `withScope(java.lang.String)` was added

#### `models.EventSubscriptionUpdateParameters` was modified

* `withDeliveryWithResourceIdentity(models.DeliveryWithResourceIdentity)` was added
* `deadLetterWithResourceIdentity()` was added
* `deliveryWithResourceIdentity()` was added
* `withDeadLetterWithResourceIdentity(models.DeadLetterWithResourceIdentity)` was added

## 1.1.0-beta.5 (2021-12-14)

- Azure Resource Manager EventGrid client library for Java. This package contains Microsoft Azure SDK for EventGrid Management SDK. Azure EventGrid Management Client. Package tag package-2021-12. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.StringNotBeginsWithAdvancedFilter` was added

* `models.NumberInRangeAdvancedFilter` was added

* `models.IsNotNullAdvancedFilter` was added

* `models.IsNullOrUndefinedAdvancedFilter` was added

* `models.NumberNotInRangeAdvancedFilter` was added

* `models.StringNotEndsWithAdvancedFilter` was added

* `models.StringNotContainsAdvancedFilter` was added

## 1.1.0-beta.4 (2021-10-25)

- Azure Resource Manager EventGrid client library for Java. This package contains Microsoft Azure SDK for EventGrid Management SDK. Azure EventGrid Management Client. Package tag package-2021-12. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.ExtensionTopic` was added

## 1.1.0-beta.3 (2021-10-18)

- Azure Resource Manager EventGrid client library for Java. This package contains Microsoft Azure SDK for EventGrid Management SDK. Azure EventGrid Management Client. Package tag package-2021-12. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.DeliveryAttributeListResult` was added

* `models.DeliveryAttributeMapping` was added

* `models.SystemTopic` was added

* `models.SystemTopicUpdateParameters` was added

* `models.DeliveryAttributeMappingType` was added

* `models.IdentityType` was added

* `models.StaticDeliveryAttributeMapping` was added

* `models.TopicTypePropertiesSupportedScopesForSourceItem` was added

* `models.SystemTopic$Definition` was added

* `models.UserIdentityProperties` was added

* `models.DynamicDeliveryAttributeMapping` was added

* `models.SystemTopicsListResult` was added

* `models.SystemTopic$Update` was added

* `models.SystemTopic$DefinitionStages` was added

* `models.IdentityInfo` was added

* `models.SystemTopics` was added

* `models.SystemTopicEventSubscriptions` was added

* `models.SystemTopic$UpdateStages` was added

#### `models.ServiceBusTopicEventSubscriptionDestination` was modified

* `withDeliveryAttributeMappings(java.util.List)` was added
* `deliveryAttributeMappings()` was added

#### `models.DomainUpdateParameters` was modified

* `autoDeleteTopicWithLastSubscription()` was added
* `withAutoCreateTopicWithFirstSubscription(java.lang.Boolean)` was added
* `withAutoDeleteTopicWithLastSubscription(java.lang.Boolean)` was added
* `withIdentity(models.IdentityInfo)` was added
* `disableLocalAuth()` was added
* `identity()` was added
* `withDisableLocalAuth(java.lang.Boolean)` was added
* `autoCreateTopicWithFirstSubscription()` was added

#### `models.Topic` was modified

* `disableLocalAuth()` was added

#### `models.Topic$Update` was modified

* `withDisableLocalAuth(java.lang.Boolean)` was added

#### `models.EventSubscriptionFilter` was modified

* `enableAdvancedFilteringOnArrays()` was added
* `withEnableAdvancedFilteringOnArrays(java.lang.Boolean)` was added

#### `models.WebhookEventSubscriptionDestination` was modified

* `deliveryAttributeMappings()` was added
* `withDeliveryAttributeMappings(java.util.List)` was added

#### `models.StorageQueueEventSubscriptionDestination` was modified

* `queueMessageTimeToLiveInSeconds()` was added
* `withQueueMessageTimeToLiveInSeconds(java.lang.Long)` was added

#### `models.EventSubscription` was modified

* `getDeliveryAttributes()` was added
* `getDeliveryAttributesWithResponse(com.azure.core.util.Context)` was added

#### `models.ServiceBusQueueEventSubscriptionDestination` was modified

* `withDeliveryAttributeMappings(java.util.List)` was added
* `deliveryAttributeMappings()` was added

#### `models.AzureFunctionEventSubscriptionDestination` was modified

* `withDeliveryAttributeMappings(java.util.List)` was added
* `deliveryAttributeMappings()` was added

#### `models.Domain` was modified

* `disableLocalAuth()` was added
* `autoDeleteTopicWithLastSubscription()` was added
* `identity()` was added
* `autoCreateTopicWithFirstSubscription()` was added

#### `models.TopicUpdateParameters` was modified

* `disableLocalAuth()` was added
* `withDisableLocalAuth(java.lang.Boolean)` was added

#### `models.HybridConnectionEventSubscriptionDestination` was modified

* `withDeliveryAttributeMappings(java.util.List)` was added
* `deliveryAttributeMappings()` was added

#### `models.TopicTypeInfo` was modified

* `supportedScopesForSource()` was added

#### `models.EventHubEventSubscriptionDestination` was modified

* `deliveryAttributeMappings()` was added
* `withDeliveryAttributeMappings(java.util.List)` was added

#### `models.EventSubscriptions` was modified

* `getDeliveryAttributesWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getDeliveryAttributes(java.lang.String,java.lang.String)` was added

#### `models.Domain$Update` was modified

* `withAutoCreateTopicWithFirstSubscription(java.lang.Boolean)` was added
* `withDisableLocalAuth(java.lang.Boolean)` was added
* `withAutoDeleteTopicWithLastSubscription(java.lang.Boolean)` was added
* `withIdentity(models.IdentityInfo)` was added

#### `EventGridManager` was modified

* `systemTopicEventSubscriptions()` was added
* `systemTopics()` was added

#### `models.Domain$Definition` was modified

* `withDisableLocalAuth(java.lang.Boolean)` was added
* `withAutoDeleteTopicWithLastSubscription(java.lang.Boolean)` was added
* `withAutoCreateTopicWithFirstSubscription(java.lang.Boolean)` was added
* `withIdentity(models.IdentityInfo)` was added

#### `models.Topic$Definition` was modified

* `withDisableLocalAuth(java.lang.Boolean)` was added

#### `EventGridManager$Configurable` was modified

* `withScope(java.lang.String)` was added

## 1.1.0-beta.2 (2021-09-16)

- Azure Resource Manager EventGrid client library for Java. This package contains Microsoft Azure SDK for EventGrid Management SDK. Azure EventGrid Management Client. Package tag package-2021-06-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.1.0-beta.1 (2021-06-21)

- Azure Resource Manager EventGrid client library for Java. This package contains Microsoft Azure SDK for EventGrid Management SDK. Azure EventGrid Management Client. Package tag package-2021-06-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### New Feature

* `models.ExtensionTopic` was added

* `models.PartnerNamespaces` was added

* `models.SystemTopic` was added

* `models.StringNotBeginsWithAdvancedFilter` was added

* `models.EventSubscriptionIdentity` was added

* `models.PartnerNamespaceSharedAccessKeys` was added

* `models.EventChannel` was added

* `models.Sku` was added

* `models.PartnerNamespace` was added

* `models.NumberInRangeAdvancedFilter` was added

* `models.DeadLetterWithResourceIdentity` was added

* `models.IsNotNullAdvancedFilter` was added

* `models.PartnerRegistration$UpdateStages` was added

* `models.PartnerRegistration$Update` was added

* `models.EventChannel$Definition` was added

* `models.UserIdentityProperties` was added

* `models.SystemTopic$Definition` was added

* `models.EventChannels` was added

* `models.EventChannelDestination` was added

* `models.IsNullOrUndefinedAdvancedFilter` was added

* `models.PartnerTopicUpdateParameters` was added

* `models.ResourceSku` was added

* `models.ResourceKind` was added

* `models.PartnerRegistration$DefinitionStages` was added

* `models.EventChannelProvisioningState` was added

* `models.NumberNotInRangeAdvancedFilter` was added

* `models.PartnerTopicEventSubscriptions` was added

* `models.PartnerNamespace$Update` was added

* `models.EventChannel$DefinitionStages` was added

* `models.IdentityInfo` was added

* `models.EventChannelSource` was added

* `models.PartnerTopicReadinessState` was added

* `models.PartnerNamespacesListResult` was added

* `models.SystemTopic$UpdateStages` was added

* `models.DeliveryAttributeListResult` was added

* `models.DeliveryAttributeMapping` was added

* `models.PartnerRegistration$Definition` was added

* `models.StringNotEndsWithAdvancedFilter` was added

* `models.EventChannel$Update` was added

* `models.PartnerTopicActivationState` was added

* `models.SystemTopicUpdateParameters` was added

* `models.PartnerRegistrationProvisioningState` was added

* `models.DeliveryAttributeMappingType` was added

* `models.PartnerNamespaceUpdateParameters` was added

* `models.IdentityType` was added

* `models.StaticDeliveryAttributeMapping` was added

* `models.PartnerTopics` was added

* `models.DeliveryWithResourceIdentity` was added

* `models.TopicTypePropertiesSupportedScopesForSourceItem` was added

* `models.PartnerRegistrationsListResult` was added

* `models.PartnerRegistrationUpdateParameters` was added

* `models.DynamicDeliveryAttributeMapping` was added

* `models.ExtensionTopics` was added

* `models.EventChannelsListResult` was added

* `models.PartnerRegistration` was added

* `models.SystemTopicsListResult` was added

* `models.PartnerRegistrationVisibilityState` was added

* `models.EventSubscriptionIdentityType` was added

* `models.PartnerNamespace$DefinitionStages` was added

* `models.StringNotContainsAdvancedFilter` was added

* `models.SystemTopic$Update` was added

* `models.EventChannelFilter` was added

* `models.SystemTopic$DefinitionStages` was added

* `models.PartnerNamespaceProvisioningState` was added

* `models.PartnerTopicProvisioningState` was added

* `models.PartnerTopicsListResult` was added

* `models.EventChannel$UpdateStages` was added

* `models.PartnerTopic` was added

* `models.SystemTopics` was added

* `models.ExtendedLocation` was added

* `models.PartnerNamespace$Definition` was added

* `models.PartnerRegistrations` was added

* `models.PartnerNamespaceRegenerateKeyRequest` was added

* `models.PartnerNamespace$UpdateStages` was added

* `models.SystemTopicEventSubscriptions` was added

#### `models.Topic` was modified

* `disableLocalAuth()` was added
* `kind()` was added
* `identity()` was added
* `extendedLocation()` was added
* `sku()` was added

#### `models.EventSubscriptionFilter` was modified

* `withEnableAdvancedFilteringOnArrays(java.lang.Boolean)` was added
* `enableAdvancedFilteringOnArrays()` was added

#### `models.WebhookEventSubscriptionDestination` was modified

* `withDeliveryAttributeMappings(java.util.List)` was added
* `deliveryAttributeMappings()` was added

#### `models.StorageQueueEventSubscriptionDestination` was modified

* `queueMessageTimeToLiveInSeconds()` was added
* `withQueueMessageTimeToLiveInSeconds(java.lang.Long)` was added

#### `models.EventSubscription$Definition` was modified

* `withDeliveryWithResourceIdentity(models.DeliveryWithResourceIdentity)` was added
* `withDeadLetterWithResourceIdentity(models.DeadLetterWithResourceIdentity)` was added

#### `models.ServiceBusQueueEventSubscriptionDestination` was modified

* `deliveryAttributeMappings()` was added
* `withDeliveryAttributeMappings(java.util.List)` was added

#### `models.TopicUpdateParameters` was modified

* `withIdentity(models.IdentityInfo)` was added
* `withDisableLocalAuth(java.lang.Boolean)` was added
* `identity()` was added
* `withSku(models.ResourceSku)` was added
* `disableLocalAuth()` was added
* `sku()` was added

#### `models.Domain$Update` was modified

* `withDisableLocalAuth(java.lang.Boolean)` was added
* `withIdentity(models.IdentityInfo)` was added
* `withSku(models.ResourceSku)` was added
* `withAutoDeleteTopicWithLastSubscription(java.lang.Boolean)` was added
* `withAutoCreateTopicWithFirstSubscription(java.lang.Boolean)` was added

#### `models.Topic$Definition` was modified

* `withExtendedLocation(models.ExtendedLocation)` was added
* `withKind(models.ResourceKind)` was added
* `withDisableLocalAuth(java.lang.Boolean)` was added
* `withIdentity(models.IdentityInfo)` was added
* `withSku(models.ResourceSku)` was added

#### `models.ServiceBusTopicEventSubscriptionDestination` was modified

* `deliveryAttributeMappings()` was added
* `withDeliveryAttributeMappings(java.util.List)` was added

#### `models.DomainUpdateParameters` was modified

* `autoDeleteTopicWithLastSubscription()` was added
* `identity()` was added
* `sku()` was added
* `withAutoCreateTopicWithFirstSubscription(java.lang.Boolean)` was added
* `withAutoDeleteTopicWithLastSubscription(java.lang.Boolean)` was added
* `withSku(models.ResourceSku)` was added
* `withIdentity(models.IdentityInfo)` was added
* `withDisableLocalAuth(java.lang.Boolean)` was added
* `autoCreateTopicWithFirstSubscription()` was added
* `disableLocalAuth()` was added

#### `models.Topic$Update` was modified

* `withIdentity(models.IdentityInfo)` was added
* `withDisableLocalAuth(java.lang.Boolean)` was added
* `withSku(models.ResourceSku)` was added

#### `models.Operation` was modified

* `isDataAction()` was added

#### `models.EventSubscription` was modified

* `deliveryWithResourceIdentity()` was added
* `getDeliveryAttributesWithResponse(com.azure.core.util.Context)` was added
* `deadLetterWithResourceIdentity()` was added
* `getDeliveryAttributes()` was added

#### `models.Domain` was modified

* `disableLocalAuth()` was added
* `autoCreateTopicWithFirstSubscription()` was added
* `identity()` was added
* `sku()` was added
* `autoDeleteTopicWithLastSubscription()` was added

#### `models.AzureFunctionEventSubscriptionDestination` was modified

* `withDeliveryAttributeMappings(java.util.List)` was added
* `deliveryAttributeMappings()` was added

#### `models.EventSubscription$Update` was modified

* `withDeadLetterWithResourceIdentity(models.DeadLetterWithResourceIdentity)` was added
* `withDeliveryWithResourceIdentity(models.DeliveryWithResourceIdentity)` was added

#### `models.HybridConnectionEventSubscriptionDestination` was modified

* `deliveryAttributeMappings()` was added
* `withDeliveryAttributeMappings(java.util.List)` was added

#### `models.TopicTypeInfo` was modified

* `supportedScopesForSource()` was added

#### `models.EventHubEventSubscriptionDestination` was modified

* `deliveryAttributeMappings()` was added
* `withDeliveryAttributeMappings(java.util.List)` was added

#### `models.EventSubscriptions` was modified

* `getDeliveryAttributes(java.lang.String,java.lang.String)` was added
* `getDeliveryAttributesWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `EventGridManager` was modified

* `partnerTopicEventSubscriptions()` was added
* `systemTopics()` was added
* `partnerNamespaces()` was added
* `partnerRegistrations()` was added
* `eventChannels()` was added
* `systemTopicEventSubscriptions()` was added
* `partnerTopics()` was added
* `extensionTopics()` was added

#### `models.Domain$Definition` was modified

* `withSku(models.ResourceSku)` was added
* `withAutoDeleteTopicWithLastSubscription(java.lang.Boolean)` was added
* `withDisableLocalAuth(java.lang.Boolean)` was added
* `withAutoCreateTopicWithFirstSubscription(java.lang.Boolean)` was added
* `withIdentity(models.IdentityInfo)` was added

#### `EventGridManager$Configurable` was modified

* `withScope(java.lang.String)` was added

#### `models.EventSubscriptionUpdateParameters` was modified

* `withDeadLetterWithResourceIdentity(models.DeadLetterWithResourceIdentity)` was added
* `deadLetterWithResourceIdentity()` was added
* `deliveryWithResourceIdentity()` was added
* `withDeliveryWithResourceIdentity(models.DeliveryWithResourceIdentity)` was added

## 1.0.0 (2021-04-09)

- Azure Resource Manager EventGrid client library for Java. This package contains Microsoft Azure SDK for EventGrid Management SDK. Azure EventGrid Management Client. Package tag package-2020-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.0.0-beta.2 (2021-02-22)

- Azure Resource Manager EventGrid client library for Java. This package contains Microsoft Azure SDK for EventGrid Management SDK. Azure EventGrid Management Client. Package tag package-2020-10-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Change

#### `models.Topics` was modified

* `regenerateKeyWithResponse(java.lang.String,java.lang.String,models.TopicRegenerateKeyRequest,com.azure.core.util.Context)` was removed

### New Feature

* `models.ExtensionTopic` was added

* `models.CreatedByType` was added

* `models.PartnerNamespaces` was added

* `models.SystemTopic` was added

* `models.StringNotBeginsWithAdvancedFilter` was added

* `models.EventSubscriptionIdentity` was added

* `models.PartnerNamespaceSharedAccessKeys` was added

* `models.PartnerTopicTypeAuthorizationState` was added

* `models.EventChannel` was added

* `models.Sku` was added

* `models.PartnerNamespace` was added

* `models.NumberInRangeAdvancedFilter` was added

* `models.DeadLetterWithResourceIdentity` was added

* `models.IsNotNullAdvancedFilter` was added

* `models.PartnerRegistration$UpdateStages` was added

* `models.PartnerRegistration$Update` was added

* `models.EventChannel$Definition` was added

* `models.UserIdentityProperties` was added

* `models.SystemTopic$Definition` was added

* `models.EventChannels` was added

* `models.EventChannelDestination` was added

* `models.IsNullOrUndefinedAdvancedFilter` was added

* `models.PartnerTopicUpdateParameters` was added

* `models.ResourceSku` was added

* `models.ResourceKind` was added

* `models.PartnerRegistration$DefinitionStages` was added

* `models.EventChannelProvisioningState` was added

* `models.NumberNotInRangeAdvancedFilter` was added

* `models.PartnerTopicEventSubscriptions` was added

* `models.SystemData` was added

* `models.PartnerNamespace$Update` was added

* `models.EventChannel$DefinitionStages` was added

* `models.IdentityInfo` was added

* `models.EventChannelSource` was added

* `models.PartnerTopicReadinessState` was added

* `models.PartnerNamespacesListResult` was added

* `models.SystemTopic$UpdateStages` was added

* `models.DeliveryAttributeListResult` was added

* `models.DeliveryAttributeMapping` was added

* `models.PartnerRegistration$Definition` was added

* `models.StringNotEndsWithAdvancedFilter` was added

* `models.EventChannel$Update` was added

* `models.PartnerTopicActivationState` was added

* `models.SystemTopicUpdateParameters` was added

* `models.PartnerRegistrationProvisioningState` was added

* `models.DeliveryAttributeMappingType` was added

* `models.PartnerTopicType` was added

* `models.PartnerNamespaceUpdateParameters` was added

* `models.IdentityType` was added

* `models.StaticDeliveryAttributeMapping` was added

* `models.PartnerTopics` was added

* `models.DeliveryWithResourceIdentity` was added

* `models.TopicTypePropertiesSupportedScopesForSourceItem` was added

* `models.PartnerRegistrationsListResult` was added

* `models.PartnerRegistrationUpdateParameters` was added

* `models.DynamicDeliveryAttributeMapping` was added

* `models.ExtensionTopics` was added

* `models.EventChannelsListResult` was added

* `models.PartnerRegistration` was added

* `models.SystemTopicsListResult` was added

* `models.PartnerRegistrationVisibilityState` was added

* `models.EventSubscriptionIdentityType` was added

* `models.PartnerNamespace$DefinitionStages` was added

* `models.StringNotContainsAdvancedFilter` was added

* `models.SystemTopic$Update` was added

* `models.EventChannelFilter` was added

* `models.SystemTopic$DefinitionStages` was added

* `models.PartnerNamespaceProvisioningState` was added

* `models.PartnerTopicProvisioningState` was added

* `models.PartnerTopicsListResult` was added

* `models.EventChannel$UpdateStages` was added

* `models.PartnerTopic` was added

* `models.SystemTopics` was added

* `models.ExtendedLocation` was added

* `models.PartnerNamespace$Definition` was added

* `models.PartnerRegistrations` was added

* `models.PartnerNamespaceRegenerateKeyRequest` was added

* `models.PartnerNamespace$UpdateStages` was added

* `models.SystemTopicEventSubscriptions` was added

#### `models.Topics` was modified

* `regenerateKey(java.lang.String,java.lang.String,models.TopicRegenerateKeyRequest,com.azure.core.util.Context)` was added

#### `models.Topic` was modified

* `regenerateKey(models.TopicRegenerateKeyRequest,com.azure.core.util.Context)` was added
* `listSharedAccessKeys()` was added
* `sku()` was added
* `listSharedAccessKeysWithResponse(com.azure.core.util.Context)` was added
* `regenerateKey(models.TopicRegenerateKeyRequest)` was added
* `identity()` was added
* `extendedLocation()` was added
* `kind()` was added

#### `models.EventSubscriptionFilter` was modified

* `enableAdvancedFilteringOnArrays()` was added
* `withEnableAdvancedFilteringOnArrays(java.lang.Boolean)` was added

#### `models.WebhookEventSubscriptionDestination` was modified

* `withDeliveryAttributeMappings(java.util.List)` was added
* `deliveryAttributeMappings()` was added

#### `models.StorageQueueEventSubscriptionDestination` was modified

* `queueMessageTimeToLiveInSeconds()` was added
* `withQueueMessageTimeToLiveInSeconds(java.lang.Long)` was added

#### `models.EventSubscription$Definition` was modified

* `withDeliveryWithResourceIdentity(models.DeliveryWithResourceIdentity)` was added
* `withDeadLetterWithResourceIdentity(models.DeadLetterWithResourceIdentity)` was added

#### `models.ServiceBusQueueEventSubscriptionDestination` was modified

* `withDeliveryAttributeMappings(java.util.List)` was added
* `deliveryAttributeMappings()` was added

#### `models.TopicUpdateParameters` was modified

* `identity()` was added
* `withSku(models.ResourceSku)` was added
* `sku()` was added
* `withIdentity(models.IdentityInfo)` was added

#### `models.Domain$Update` was modified

* `withIdentity(models.IdentityInfo)` was added
* `withSku(models.ResourceSku)` was added

#### `models.Topic$Definition` was modified

* `withSku(models.ResourceSku)` was added
* `withKind(models.ResourceKind)` was added
* `withExtendedLocation(models.ExtendedLocation)` was added
* `withIdentity(models.IdentityInfo)` was added

#### `models.ServiceBusTopicEventSubscriptionDestination` was modified

* `withDeliveryAttributeMappings(java.util.List)` was added
* `deliveryAttributeMappings()` was added

#### `models.DomainUpdateParameters` was modified

* `sku()` was added
* `withIdentity(models.IdentityInfo)` was added
* `withSku(models.ResourceSku)` was added
* `identity()` was added

#### `models.Topic$Update` was modified

* `withSku(models.ResourceSku)` was added
* `withIdentity(models.IdentityInfo)` was added

#### `models.EventSubscription` was modified

* `deadLetterWithResourceIdentity()` was added
* `getDeliveryAttributes()` was added
* `getFullUrl()` was added
* `getDeliveryAttributesWithResponse(com.azure.core.util.Context)` was added
* `deliveryWithResourceIdentity()` was added
* `getFullUrlWithResponse(com.azure.core.util.Context)` was added
* `systemData()` was added

#### `models.Domain` was modified

* `listSharedAccessKeysWithResponse(com.azure.core.util.Context)` was added
* `identity()` was added
* `regenerateKey(models.DomainRegenerateKeyRequest)` was added
* `listSharedAccessKeys()` was added
* `regenerateKeyWithResponse(models.DomainRegenerateKeyRequest,com.azure.core.util.Context)` was added
* `sku()` was added

#### `models.AzureFunctionEventSubscriptionDestination` was modified

* `deliveryAttributeMappings()` was added
* `withDeliveryAttributeMappings(java.util.List)` was added

#### `models.EventSubscription$Update` was modified

* `withDeadLetterWithResourceIdentity(models.DeadLetterWithResourceIdentity)` was added
* `withDeliveryWithResourceIdentity(models.DeliveryWithResourceIdentity)` was added

#### `models.HybridConnectionEventSubscriptionDestination` was modified

* `withDeliveryAttributeMappings(java.util.List)` was added
* `deliveryAttributeMappings()` was added

#### `models.TopicTypeInfo` was modified

* `supportedScopesForSource()` was added

#### `models.EventHubEventSubscriptionDestination` was modified

* `deliveryAttributeMappings()` was added
* `withDeliveryAttributeMappings(java.util.List)` was added

#### `models.EventSubscriptions` was modified

* `getDeliveryAttributes(java.lang.String,java.lang.String)` was added
* `getDeliveryAttributesWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `EventGridManager` was modified

* `partnerTopics()` was added
* `eventChannels()` was added
* `partnerRegistrations()` was added
* `partnerTopicEventSubscriptions()` was added
* `extensionTopics()` was added
* `systemTopics()` was added
* `systemTopicEventSubscriptions()` was added
* `partnerNamespaces()` was added

#### `models.Domain$Definition` was modified

* `withIdentity(models.IdentityInfo)` was added
* `withSku(models.ResourceSku)` was added

#### `models.EventSubscriptionUpdateParameters` was modified

* `withDeliveryWithResourceIdentity(models.DeliveryWithResourceIdentity)` was added
* `deadLetterWithResourceIdentity()` was added
* `withDeadLetterWithResourceIdentity(models.DeadLetterWithResourceIdentity)` was added
* `deliveryWithResourceIdentity()` was added

## 1.0.0-beta.1 (2020-12-18)

- Azure Resource Manager EventGrid client library for Java. This package contains Microsoft Azure SDK for EventGrid Management SDK. Azure EventGrid Management Client. Package tag package-2020-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

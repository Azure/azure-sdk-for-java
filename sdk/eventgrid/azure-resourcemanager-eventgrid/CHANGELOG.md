# Release History

## 1.1.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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

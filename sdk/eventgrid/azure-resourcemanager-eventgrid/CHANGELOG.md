# Release History

## 1.0.0 (2021-04-09)

- Azure Resource Manager EventGrid client library for Java. This package contains Microsoft Azure SDK for EventGrid Management SDK. Azure EventGrid Management Client. Package tag package-2020-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Change

* `models.ExtensionTopic` was removed

* `models.CreatedByType` was removed

* `models.PartnerNamespaces` was removed

* `models.SystemTopic` was removed

* `models.StringNotBeginsWithAdvancedFilter` was removed

* `models.EventSubscriptionIdentity` was removed

* `models.PartnerNamespaceSharedAccessKeys` was removed

* `models.PartnerTopicTypeAuthorizationState` was removed

* `models.EventChannel` was removed

* `models.Sku` was removed

* `models.PartnerNamespace` was removed

* `models.NumberInRangeAdvancedFilter` was removed

* `models.DeadLetterWithResourceIdentity` was removed

* `models.IsNotNullAdvancedFilter` was removed

* `models.PartnerRegistration$UpdateStages` was removed

* `models.PartnerRegistration$Update` was removed

* `models.EventChannel$Definition` was removed

* `models.UserIdentityProperties` was removed

* `models.SystemTopic$Definition` was removed

* `models.EventChannels` was removed

* `models.EventChannelDestination` was removed

* `models.IsNullOrUndefinedAdvancedFilter` was removed

* `models.PartnerTopicUpdateParameters` was removed

* `models.ResourceSku` was removed

* `models.ResourceKind` was removed

* `models.PartnerRegistration$DefinitionStages` was removed

* `models.EventChannelProvisioningState` was removed

* `models.NumberNotInRangeAdvancedFilter` was removed

* `models.PartnerTopicEventSubscriptions` was removed

* `models.SystemData` was removed

* `models.PartnerNamespace$Update` was removed

* `models.EventChannel$DefinitionStages` was removed

* `models.IdentityInfo` was removed

* `models.EventChannelSource` was removed

* `models.PartnerTopicReadinessState` was removed

* `models.PartnerNamespacesListResult` was removed

* `models.SystemTopic$UpdateStages` was removed

* `models.DeliveryAttributeListResult` was removed

* `models.DeliveryAttributeMapping` was removed

* `models.PartnerRegistration$Definition` was removed

* `models.StringNotEndsWithAdvancedFilter` was removed

* `models.EventChannel$Update` was removed

* `models.PartnerTopicActivationState` was removed

* `models.SystemTopicUpdateParameters` was removed

* `models.PartnerRegistrationProvisioningState` was removed

* `models.DeliveryAttributeMappingType` was removed

* `models.PartnerTopicType` was removed

* `models.PartnerNamespaceUpdateParameters` was removed

* `models.IdentityType` was removed

* `models.StaticDeliveryAttributeMapping` was removed

* `models.PartnerTopics` was removed

* `models.DeliveryWithResourceIdentity` was removed

* `models.TopicTypePropertiesSupportedScopesForSourceItem` was removed

* `models.PartnerRegistrationsListResult` was removed

* `models.PartnerRegistrationUpdateParameters` was removed

* `models.DynamicDeliveryAttributeMapping` was removed

* `models.ExtensionTopics` was removed

* `models.EventChannelsListResult` was removed

* `models.PartnerRegistration` was removed

* `models.SystemTopicsListResult` was removed

* `models.PartnerRegistrationVisibilityState` was removed

* `models.EventSubscriptionIdentityType` was removed

* `models.PartnerNamespace$DefinitionStages` was removed

* `models.StringNotContainsAdvancedFilter` was removed

* `models.SystemTopic$Update` was removed

* `models.EventChannelFilter` was removed

* `models.SystemTopic$DefinitionStages` was removed

* `models.PartnerNamespaceProvisioningState` was removed

* `models.PartnerTopicProvisioningState` was removed

* `models.PartnerTopicsListResult` was removed

* `models.EventChannel$UpdateStages` was removed

* `models.PartnerTopic` was removed

* `models.SystemTopics` was removed

* `models.ExtendedLocation` was removed

* `models.PartnerNamespace$Definition` was removed

* `models.PartnerRegistrations` was removed

* `models.PartnerNamespaceRegenerateKeyRequest` was removed

* `models.PartnerNamespace$UpdateStages` was removed

* `models.SystemTopicEventSubscriptions` was removed

#### `models.Topic` was modified

* `models.SystemData systemData()` -> `com.azure.core.management.SystemData systemData()`
* `kind()` was removed
* `identity()` was removed
* `extendedLocation()` was removed
* `sku()` was removed

#### `models.EventSubscriptionFilter` was modified

* `enableAdvancedFilteringOnArrays()` was removed
* `withEnableAdvancedFilteringOnArrays(java.lang.Boolean)` was removed

#### `models.WebhookEventSubscriptionDestination` was modified

* `deliveryAttributeMappings()` was removed
* `withDeliveryAttributeMappings(java.util.List)` was removed

#### `models.StorageQueueEventSubscriptionDestination` was modified

* `withQueueMessageTimeToLiveInSeconds(java.lang.Long)` was removed
* `queueMessageTimeToLiveInSeconds()` was removed

#### `models.EventSubscription$Definition` was modified

* `withDeadLetterWithResourceIdentity(models.DeadLetterWithResourceIdentity)` was removed
* `withDeliveryWithResourceIdentity(models.DeliveryWithResourceIdentity)` was removed

#### `models.ServiceBusQueueEventSubscriptionDestination` was modified

* `deliveryAttributeMappings()` was removed
* `withDeliveryAttributeMappings(java.util.List)` was removed

#### `models.TopicUpdateParameters` was modified

* `withIdentity(models.IdentityInfo)` was removed
* `identity()` was removed
* `sku()` was removed
* `withSku(models.ResourceSku)` was removed

#### `models.Domain$Update` was modified

* `withIdentity(models.IdentityInfo)` was removed
* `withSku(models.ResourceSku)` was removed

#### `models.Topic$Definition` was modified

* `withKind(models.ResourceKind)` was removed
* `withPrivateEndpointConnections(java.util.List)` was removed
* `withSku(models.ResourceSku)` was removed
* `withExtendedLocation(models.ExtendedLocation)` was removed
* `withIdentity(models.IdentityInfo)` was removed

#### `models.ServiceBusTopicEventSubscriptionDestination` was modified

* `deliveryAttributeMappings()` was removed
* `withDeliveryAttributeMappings(java.util.List)` was removed

#### `models.DomainUpdateParameters` was modified

* `identity()` was removed
* `withSku(models.ResourceSku)` was removed
* `sku()` was removed
* `withIdentity(models.IdentityInfo)` was removed

#### `models.DomainTopic` was modified

* `models.SystemData systemData()` -> `com.azure.core.management.SystemData systemData()`

#### `models.Topic$Update` was modified

* `withIdentity(models.IdentityInfo)` was removed
* `withSku(models.ResourceSku)` was removed

#### `models.EventSubscription` was modified

* `deliveryWithResourceIdentity()` was removed
* `models.SystemData systemData()` -> `com.azure.core.management.SystemData systemData()`
* `getDeliveryAttributes()` was removed
* `getDeliveryAttributesWithResponse(com.azure.core.util.Context)` was removed
* `deadLetterWithResourceIdentity()` was removed

#### `models.Domain` was modified

* `identity()` was removed
* `sku()` was removed
* `models.SystemData systemData()` -> `com.azure.core.management.SystemData systemData()`

#### `models.AzureFunctionEventSubscriptionDestination` was modified

* `deliveryAttributeMappings()` was removed
* `withDeliveryAttributeMappings(java.util.List)` was removed

#### `models.EventSubscription$Update` was modified

* `withDeadLetterWithResourceIdentity(models.DeadLetterWithResourceIdentity)` was removed
* `withDeliveryWithResourceIdentity(models.DeliveryWithResourceIdentity)` was removed

#### `models.HybridConnectionEventSubscriptionDestination` was modified

* `withDeliveryAttributeMappings(java.util.List)` was removed
* `deliveryAttributeMappings()` was removed

#### `models.TopicTypeInfo` was modified

* `supportedScopesForSource()` was removed

#### `models.EventHubEventSubscriptionDestination` was modified

* `deliveryAttributeMappings()` was removed
* `withDeliveryAttributeMappings(java.util.List)` was removed

#### `models.EventSubscriptions` was modified

* `getDeliveryAttributesWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `getDeliveryAttributes(java.lang.String,java.lang.String)` was removed

#### `EventGridManager` was modified

* `systemTopicEventSubscriptions()` was removed
* `partnerTopicEventSubscriptions()` was removed
* `systemTopics()` was removed
* `partnerRegistrations()` was removed
* `eventChannels()` was removed
* `extensionTopics()` was removed
* `partnerTopics()` was removed
* `partnerNamespaces()` was removed

#### `models.Domain$Definition` was modified

* `withSku(models.ResourceSku)` was removed
* `withPrivateEndpointConnections(java.util.List)` was removed
* `withIdentity(models.IdentityInfo)` was removed

#### `models.EventSubscriptionUpdateParameters` was modified

* `withDeliveryWithResourceIdentity(models.DeliveryWithResourceIdentity)` was removed
* `withDeadLetterWithResourceIdentity(models.DeadLetterWithResourceIdentity)` was removed
* `deadLetterWithResourceIdentity()` was removed
* `deliveryWithResourceIdentity()` was removed

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

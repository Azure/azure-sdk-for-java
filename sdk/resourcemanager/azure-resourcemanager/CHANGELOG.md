# Release History

## 2.66.0 (2026-11-04)

### azure-resourcemanager-storage

#### Features Added

* `models.BlobAccessPointAccessKeyAuthProperties` was added

* `models.BlobAccessPointEndpointConnectionPropertiesUpdate` was added

* `models.ResourceProvisioningState` was added

* `models.BlobAccessPointConnectionPropertiesUpdate` was added

* `models.BlobAccessPointAccessKeyAuthPropertiesUpdate` was added

* `models.BlobAccessPointNetAppOntapSourceProperties` was added

* `models.BlobAccessPointGenericS3SourcePropertiesUpdate` was added

* `models.BlobAccessPointDellOneFsSourcePropertiesUpdate` was added

* `models.BlobAccessPointQumuloSourceProperties` was added

* `models.BlobAccessPointProposedConnectionTestRequest` was added

* `models.BlobAccessPointConnectionTestRequest` was added

* `models.BlobAccessPointTlsVerification` was added

* `models.BlobAccessPointRemoteAuthProperties` was added

* `models.BlobAccessPointSourceType` was added

* `models.BlobAccessPointRemoteAuthType` was added

* `models.BlobAccessPointConfigurationUpdate` was added

* `models.TurboTierStatus` was added

* `models.BlobAccessPointPrivateLinkIdType` was added

* `models.TurboTier` was added

* `models.BlobAccessPointSourcePropertiesUpdate` was added

* `models.BlobAccessPointNasuniSourceProperties` was added

* `models.BlobAccessPointPrivateLinkConnectionPropertiesUpdate` was added

* `models.BlobAccessPointQumuloSourcePropertiesUpdate` was added

* `models.BlobAccessPointConnectionTestStatus` was added

* `models.BlobAccessPointConfigurationConnection` was added

* `models.BlobAccessPointConnectionType` was added

* `models.BlobAccessPointNetAppOntapSourcePropertiesUpdate` was added

* `models.ContextCacheCheckNameAvailabilityParameters` was added

* `models.BlobAccessPointAzureNetAppFilesSourceProperties` was added

* `models.BlobAccessPointAzureNetAppFilesSourcePropertiesUpdate` was added

* `models.BlobAccessPointConfigurationProperties` was added

* `models.BlobAccessPointConfigurationState` was added

* `models.BlobAccessPointConnectionProperties` was added

* `models.BlobAccessPointGenericS3SourceProperties` was added

* `models.BlobAccessPointCommvaultSourcePropertiesUpdate` was added

* `models.BlobAccessPointEndpointConnectionProperties` was added

* `models.BlobAccessPointCommvaultSourceProperties` was added

* `models.BlobAccessPointNasuniSourcePropertiesUpdate` was added

* `models.ContextCacheCheckNameAvailabilityFailureReason` was added

* `models.BlobAccessPointRemoteAuthPropertiesUpdate` was added

* `models.BlobAccessPointConfigurationPropertiesUpdate` was added

* `models.BlobAccessPointDellOneFsSourceProperties` was added

* `models.BlobAccessPointPrivateLinkConnectionProperties` was added

* `models.BlobAccessPointSourceProperties` was added

##### `models.StorageAccountUpdateParameters` was modified

* `withTurboTier(models.TurboTier)` was added
* `turboTier()` was added

##### `models.NspAccessRuleProperties` was modified

* `serviceTags()` was added

##### `models.StorageDataCollaborationPolicyProperties` was modified

* `withAllowBlobAccessPoints(java.lang.Boolean)` was added
* `allowBlobAccessPoints()` was added

##### `models.StorageAccountCreateParameters` was modified

* `withTurboTier(models.TurboTier)` was added
* `turboTier()` was added

#### Dependency Updates

- Package api-version 2026-09-01.

### azure-resourcemanager-compute

#### Features Added

* `models.FlexProfile` was added

##### `models.VirtualMachineScaleSetIpConfiguration` was modified

* `privateIPAddressPrefixLength()` was added
* `withPrivateIPAddressPrefixLength(java.lang.Integer)` was added

##### `models.VirtualMachineScaleSetUpdateIpConfiguration` was modified

* `privateIPAddressPrefixLength()` was added
* `withPrivateIPAddressPrefixLength(java.lang.Integer)` was added

##### `models.VirtualMachineNetworkInterfaceIpConfiguration` was modified

* `privateIPAddressPrefixLength()` was added
* `withPrivateIPAddressPrefixLength(java.lang.Integer)` was added

#### Dependency Updates

- Package api-version Compute: 2026-04-01, ComputeDisk: 2026-03-02, ComputeGallery: 2026-03-03, ComputeSku: 2021-07-01.

### azure-resourcemanager-keyvault

#### Features Added

##### `models.JsonWebKeyType` was modified

* `OCT_HSM` was added

##### `models.ManagedHsmSkuName` was modified

* `ManagedHsmSkuName()` was added
* `STANDARD_B5V2` was added
* `STANDARD_B10V2` was added
* `STANDARD_B20V2` was added
* `STANDARD_B1V2` was added
* `STANDARD_B15V2` was added

#### Breaking Changes

##### `models.ManagedHsmSkuName` was modified

* `toString()` was removed
* `models.ManagedHsmSkuName[] values()` -> `java.util.Collection values()`
* `valueOf(java.lang.String)` was removed

#### Dependency Updates

- Package api-version 2026-05-15.

### azure-resourcemanager-containerservice

#### Features Added

* `models.ManagementMode` was added

* `models.NvidiaGPUProfile` was added

##### `models.GpuProfile` was modified

* `withNvidia(models.NvidiaGPUProfile)` was added
* `nvidia()` was added

#### Dependency Updates

- Package api-version 2026-08-01.

### azure-resourcemanager-cdn

#### Features Added

* `models.EnforceMtlsEnabledState` was added

* `models.DeliveryRuleConditionParametersType` was added

* `models.DeliveryRuleConditionParameters` was added

* `models.Route$UpdateDefinitionStages` was added

* `models.DeliveryRuleEdgeActionParameters` was added

* `models.CertificateRevocationCheckEnabledState` was added

* `models.Route$DefinitionStages` was added

* `models.Route$Definition` was added

* `models.Rule$Update` was added

* `models.Rule$UpdateDefinitionStages$WithOrder` was added

* `models.AfdCipherSuiteSetType` was added

* `models.ClientCertificateRequiredAndValidatedAdvancedSettings` was added

* `models.OriginAuthenticationType` was added

* `models.OriginGroup$Update` was added

* `models.Origin$UpdateDefinitionStages$Blank` was added

* `models.AfdDomainMtlsParameters` was added

* `models.OriginAuthenticationTokenDestinationHeader` was added

* `models.Route$UpdateDefinitionStages$WithAttach` was added

* `models.AfdEndpoint$UpdateDefinitionStages$WithAttach` was added

* `models.ClientCertificateValidatedIfPresentedAdvancedSettings` was added

* `models.OriginGroup$UpdateDefinitionStages$Blank` was added

* `models.RuleSet$UpdateDefinitionStages$WithAttach` was added

* `models.Origin` was added

* `models.Route` was added

* `models.CertificateNameCheckValidationMode` was added

* `models.RuleSet$UpdateDefinitionStages$Blank` was added

* `models.Origin$UpdateDefinitionStages$WithAttach` was added

* `models.OriginGroup$UpdateDefinitionStages$WithAttach` was added

* `models.CompleteMtlsPassthroughToOriginAdvancedSettings` was added

* `models.DeliveryRuleActionParametersType` was added

* `models.CdnMigrationToAfdParameters` was added

* `models.AfdDomainHttpsCustomizedCipherSuiteSet` was added

* `models.AfdSecretMtlsCertificateChain` was added

* `models.MigrationEndpointMapping` was added

* `models.ClientCertificateRequiredAndOriginValidatesAdvancedSettings` was added

* `models.AfdEndpoint` was added

* `models.DeliveryRuleActionParameters` was added

* `models.RuleSet$Definition` was added

* `models.Origin$UpdateDefinitionStages$WithHostname` was added

* `models.RuleSet$Update` was added

* `models.OriginGroup$UpdateDefinitionStages` was added

* `models.Rule$UpdateDefinitionStages$WithAttach` was added

* `models.AfdEndpoint$UpdateDefinitionStages` was added

* `models.AfdEndpoint$UpdateDefinitionStages$Attachable` was added

* `models.Origin$UpdateDefinitionStages$Attachable` was added

* `models.Rule$UpdateDefinitionStages$Blank` was added

* `models.CertificateSourceParameters` was added

* `models.AfdServerTlsGroupPolicy` was added

* `models.RuleSet` was added

* `models.Usage` was added

* `models.AfdEndpoint$Definition` was added

* `models.Rule$UpdateDefinitionStages` was added

* `models.Rule` was added

* `models.Origin$DefinitionStages` was added

* `models.OriginGroup$DefinitionStages` was added

* `models.Route$UpdateDefinitionStages$Attachable` was added

* `models.EdgeAction` was added

* `models.Origin$Update` was added

* `models.Rule$DefinitionStages` was added

* `models.OriginGroup` was added

* `models.RuleSet$DefinitionStages` was added

* `models.BatchRuleProperties` was added

* `models.OriginAuthenticationProperties` was added

* `models.Origin$Definition` was added

* `models.InvocationPoint` was added

* `models.AfdCustomizedCipherSuiteForTls12` was added

* `models.AfdCustomizedCipherSuiteForTls13` was added

* `models.Rule$Definition` was added

* `models.Route$UpdateDefinitionStages$WithOriginGroup` was added

* `models.AfdServerTlsGroup` was added

* `models.CertificateSourceParametersType` was added

* `models.KeyVaultSigningKeyParametersType` was added

* `models.Route$Update` was added

* `models.RuleSet$UpdateDefinitionStages$Attachable` was added

* `models.Origin$UpdateDefinitionStages` was added

* `models.AfdEndpoint$Update` was added

* `models.OriginGroup$UpdateDefinitionStages$Attachable` was added

* `models.RuleSet$UpdateDefinitionStages` was added

* `models.OriginGroup$Definition` was added

* `models.Rule$UpdateDefinitionStages$WithActions` was added

* `models.Rule$UpdateDefinitionStages$Attachable` was added

* `models.MtlsScenarioType` was added

* `models.AfdEndpoint$DefinitionStages` was added

* `models.AfdEndpoint$UpdateDefinitionStages$Blank` was added

* `models.Route$UpdateDefinitionStages$Blank` was added

##### `models.CdnProfile` was modified

* `afdEndpoints()` was added
* `ruleSets()` was added
* `originGroups()` was added

##### `models.CdnProfile$Definition` was modified

* `withGlobal()` was added

##### `models.KeyVaultSigningKeyParameters` was modified

* `withTypeName(models.KeyVaultSigningKeyParametersType)` was added

##### `models.AfdOriginGroupUpdateParameters` was modified

* `authentication()` was added
* `withAuthentication(models.OriginAuthenticationProperties)` was added

##### `models.PolicySettingsDefaultCustomBlockResponseStatusCode` was modified

* `hashCode()` was added
* `toString()` was added
* `getValue()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `equals(java.lang.Object)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromValue(java.lang.Integer)` was added

##### `models.AfdMinimumTlsVersion` was modified

* `TLS13` was added

##### `models.SecurityPolicyWebApplicationFirewallAssociation` was modified

* `withRoutes(java.util.List)` was added
* `routes()` was added

##### `models.AfdOriginUpdateParameters` was modified

* `withCustomCertificateSubjects(java.util.List)` was added
* `withCertificateNameCheckValidationMode(models.CertificateNameCheckValidationMode)` was added
* `certificateNameCheckValidationMode()` was added
* `customCertificateSubjects()` was added

##### `models.AfdDomainHttpsParameters` was modified

* `withCustomizedCipherSuiteSet(models.AfdDomainHttpsCustomizedCipherSuiteSet)` was added
* `customizedCipherSuiteSet()` was added
* `withServerTlsGroupPolicy(models.AfdServerTlsGroupPolicy)` was added
* `withServerTlsGroups(java.util.List)` was added
* `withCipherSuiteSetType(models.AfdCipherSuiteSetType)` was added
* `serverTlsGroupPolicy()` was added
* `serverTlsGroups()` was added
* `cipherSuiteSetType()` was added

##### `models.AfdEndpointUpdateParameters` was modified

* `withEnforceMtls(models.EnforceMtlsEnabledState)` was added
* `enforceMtls()` was added

##### `models.AfdDomainUpdateParameters` was modified

* `mtlsSettings()` was added
* `withMtlsSettings(models.AfdDomainMtlsParameters)` was added

##### `models.SecretType` was modified

* `MTLS_CERTIFICATE_CHAIN` was added

##### `models.SecurityPolicyWebApplicationFirewallParameters` was modified

* `withIsProfileLevel(java.lang.Boolean)` was added
* `isProfileLevel()` was added

##### `models.DeliveryRuleActionValue` was modified

* `EDGE_ACTION` was added

#### Breaking Changes

##### `models.RuleListResult` was removed

##### `models.UsagesListResult` was removed

##### `models.SecurityPolicyListResult` was removed

##### `models.OperationsListResult` was removed

##### `models.RuleSetListResult` was removed

##### `models.CdnWebApplicationFirewallPolicyList` was removed

##### `models.ManagedRuleSetDefinitionList` was removed

##### `models.AfdDomainListResult` was removed

##### `models.ResourceUsageListResult` was removed

##### `models.AfdEndpointListResult` was removed

##### `models.RouteListResult` was removed

##### `models.AfdOriginGroupListResult` was removed

##### `models.OriginGroupListResult` was removed

##### `models.EndpointListResult` was removed

##### `models.ProfileListResult` was removed

##### `models.EdgenodeResult` was removed

##### `models.SecretListResult` was removed

##### `models.OriginListResult` was removed

##### `models.AfdOriginListResult` was removed

##### `models.CustomDomainListResult` was removed

##### `models.DeepCreatedCustomDomain` was modified

* `DeepCreatedCustomDomain()` was changed to private access
* `withHostname(java.lang.String)` was removed
* `withName(java.lang.String)` was removed
* `withValidationData(java.lang.String)` was removed

##### `models.RequestSchemeMatchConditionParameters` was modified

* `withTypeName(java.lang.String)` was removed
* `java.lang.String typeName()` -> `models.DeliveryRuleConditionParametersType typeName()`
* `withOperator(java.lang.String)` was removed

##### `models.RequestUriMatchConditionParameters` was modified

* `java.lang.String typeName()` -> `models.DeliveryRuleConditionParametersType typeName()`
* `withTypeName(java.lang.String)` was removed

##### `models.WafMetricsResponseSeriesItem` was modified

* `WafMetricsResponseSeriesItem()` was changed to private access
* `withGroups(java.util.List)` was removed
* `withMetric(java.lang.String)` was removed
* `withData(java.util.List)` was removed
* `withUnit(models.WafMetricsSeriesUnit)` was removed

##### `models.RankingsResponseTablesPropertiesItemsItem` was modified

* `RankingsResponseTablesPropertiesItemsItem()` was changed to private access
* `withName(java.lang.String)` was removed
* `withMetrics(java.util.List)` was removed

##### `models.EndpointResource` was modified

* `EndpointResource()` was changed to private access
* `withId(java.lang.String)` was removed

##### `models.KeyVaultCertificateSourceParameters` was modified

* `withTypeName(java.lang.String)` was removed
* `java.lang.String typeName()` -> `models.CertificateSourceParametersType typeName()`

##### `models.KeyVaultSigningKeyParameters` was modified

* `withTypeName(java.lang.String)` was removed
* `java.lang.String typeName()` -> `models.KeyVaultSigningKeyParametersType typeName()`

##### `models.MetricsResponseSeriesItem` was modified

* `MetricsResponseSeriesItem()` was changed to private access
* `withMetric(java.lang.String)` was removed
* `withUnit(models.MetricsSeriesUnit)` was removed
* `withGroups(java.util.List)` was removed
* `withData(java.util.List)` was removed

##### `models.IpAddressGroup` was modified

* `IpAddressGroup()` was changed to private access
* `withDeliveryRegion(java.lang.String)` was removed
* `withIpv6Addresses(java.util.List)` was removed
* `withIpv4Addresses(java.util.List)` was removed

##### `models.RequestMethodMatchConditionParameters` was modified

* `withTypeName(java.lang.String)` was removed
* `java.lang.String typeName()` -> `models.DeliveryRuleConditionParametersType typeName()`

##### `models.RankingsResponseTablesItem` was modified

* `RankingsResponseTablesItem()` was changed to private access
* `withData(java.util.List)` was removed
* `withRanking(java.lang.String)` was removed

##### `models.RankingsResponseTablesPropertiesItemsMetricsItem` was modified

* `RankingsResponseTablesPropertiesItemsMetricsItem()` was changed to private access
* `withMetric(java.lang.String)` was removed
* `withPercentage(java.lang.Float)` was removed
* `withValue(java.lang.Long)` was removed

##### `models.UrlFileNameMatchConditionParameters` was modified

* `java.lang.String typeName()` -> `models.DeliveryRuleConditionParametersType typeName()`
* `withTypeName(java.lang.String)` was removed

##### `models.RouteConfigurationOverrideActionParameters` was modified

* `withTypeName(java.lang.String)` was removed
* `java.lang.String typeName()` -> `models.DeliveryRuleActionParametersType typeName()`

##### `models.ContinentsResponseCountryOrRegionsItem` was modified

* `ContinentsResponseCountryOrRegionsItem()` was changed to private access
* `withId(java.lang.String)` was removed
* `withContinentId(java.lang.String)` was removed

##### `models.UrlRedirectActionParameters` was modified

* `java.lang.String typeName()` -> `models.DeliveryRuleActionParametersType typeName()`
* `withTypeName(java.lang.String)` was removed

##### `models.SslProtocolMatchConditionParameters` was modified

* `java.lang.String typeName()` -> `models.DeliveryRuleConditionParametersType typeName()`
* `withTypeName(java.lang.String)` was removed

##### `models.RequestHeaderMatchConditionParameters` was modified

* `withTypeName(java.lang.String)` was removed
* `java.lang.String typeName()` -> `models.DeliveryRuleConditionParametersType typeName()`

##### `models.CacheKeyQueryStringActionParameters` was modified

* `withTypeName(java.lang.String)` was removed
* `java.lang.String typeName()` -> `models.DeliveryRuleActionParametersType typeName()`

##### `models.HttpVersionMatchConditionParameters` was modified

* `withTypeName(java.lang.String)` was removed
* `java.lang.String typeName()` -> `models.DeliveryRuleConditionParametersType typeName()`

##### `models.MetricsResponseSeriesPropertiesItemsItem` was modified

* `MetricsResponseSeriesPropertiesItemsItem()` was changed to private access
* `withValue(java.lang.String)` was removed
* `withName(java.lang.String)` was removed

##### `models.UsageName` was modified

* `UsageName()` was changed to private access
* `withValue(java.lang.String)` was removed
* `withLocalizedValue(java.lang.String)` was removed

##### `models.PolicySettingsDefaultCustomBlockResponseStatusCode` was modified

* `PolicySettingsDefaultCustomBlockResponseStatusCode()` was removed
* `fromInt(int)` was removed

##### `models.ResourcesResponseCustomDomainsItem` was modified

* `ResourcesResponseCustomDomainsItem()` was changed to private access
* `withName(java.lang.String)` was removed
* `withId(java.lang.String)` was removed
* `withEndpointId(java.lang.String)` was removed
* `withHistory(java.lang.Boolean)` was removed

##### `models.CookiesMatchConditionParameters` was modified

* `withTypeName(java.lang.String)` was removed
* `java.lang.String typeName()` -> `models.DeliveryRuleConditionParametersType typeName()`

##### `models.MigrationErrorType` was modified

* `MigrationErrorType()` was changed to private access

##### `models.IsDeviceMatchConditionParameters` was modified

* `withTypeName(java.lang.String)` was removed
* `java.lang.String typeName()` -> `models.DeliveryRuleConditionParametersType typeName()`

##### `models.ManagedRuleGroupDefinition` was modified

* `ManagedRuleGroupDefinition()` was changed to private access

##### `models.UrlPathMatchConditionParameters` was modified

* `java.lang.String typeName()` -> `models.DeliveryRuleConditionParametersType typeName()`
* `withTypeName(java.lang.String)` was removed

##### `models.HostnameMatchConditionParameters` was modified

* `java.lang.String typeName()` -> `models.DeliveryRuleConditionParametersType typeName()`
* `withTypeName(java.lang.String)` was removed

##### `models.ResourcesResponseEndpointsItem` was modified

* `ResourcesResponseEndpointsItem()` was changed to private access
* `withHistory(java.lang.Boolean)` was removed
* `withId(java.lang.String)` was removed
* `withCustomDomains(java.util.List)` was removed
* `withName(java.lang.String)` was removed

##### `models.MetricsResponseSeriesItemData` was modified

* `MetricsResponseSeriesItemData()` was changed to private access
* `withValue(java.lang.Float)` was removed
* `withDateTime(java.time.OffsetDateTime)` was removed

##### `models.QueryStringMatchConditionParameters` was modified

* `withTypeName(java.lang.String)` was removed
* `java.lang.String typeName()` -> `models.DeliveryRuleConditionParametersType typeName()`

##### `models.WafMetricsResponseSeriesPropertiesItemsItem` was modified

* `WafMetricsResponseSeriesPropertiesItemsItem()` was changed to private access
* `withName(java.lang.String)` was removed
* `withValue(java.lang.String)` was removed

##### `models.ContinentsResponseContinentsItem` was modified

* `ContinentsResponseContinentsItem()` was changed to private access
* `withId(java.lang.String)` was removed

##### `models.HeaderActionParameters` was modified

* `java.lang.String typeName()` -> `models.DeliveryRuleActionParametersType typeName()`
* `withTypeName(java.lang.String)` was removed

##### `models.SocketAddrMatchConditionParameters` was modified

* `java.lang.String typeName()` -> `models.DeliveryRuleConditionParametersType typeName()`
* `withTypeName(java.lang.String)` was removed

##### `models.LogSpecification` was modified

* `LogSpecification()` was changed to private access
* `withDisplayName(java.lang.String)` was removed
* `withBlobDuration(java.lang.String)` was removed
* `withName(java.lang.String)` was removed
* `withLogFilterPattern(java.lang.String)` was removed

##### `models.RemoteAddressMatchConditionParameters` was modified

* `withTypeName(java.lang.String)` was removed
* `java.lang.String typeName()` -> `models.DeliveryRuleConditionParametersType typeName()`

##### `models.WafMetricsResponseSeriesItemData` was modified

* `WafMetricsResponseSeriesItemData()` was changed to private access
* `withValue(java.lang.Float)` was removed
* `withDateTime(java.time.OffsetDateTime)` was removed

##### `models.MetricAvailability` was modified

* `MetricAvailability()` was changed to private access
* `withBlobDuration(java.lang.String)` was removed
* `withTimeGrain(java.lang.String)` was removed

##### `models.WafRankingsResponseDataItemMetric` was modified

* `WafRankingsResponseDataItemMetric()` was changed to private access
* `withMetric(java.lang.String)` was removed
* `withPercentage(java.lang.Double)` was removed
* `withValue(java.lang.Long)` was removed

##### `models.DimensionProperties` was modified

* `DimensionProperties()` was changed to private access
* `withDisplayName(java.lang.String)` was removed
* `withInternalName(java.lang.String)` was removed
* `withName(java.lang.String)` was removed

##### `models.ServiceSpecification` was modified

* `ServiceSpecification()` was changed to private access
* `withLogSpecifications(java.util.List)` was removed
* `withMetricSpecifications(java.util.List)` was removed

##### `models.WafRankingsResponseDataItem` was modified

* `WafRankingsResponseDataItem()` was changed to private access
* `withMetrics(java.util.List)` was removed
* `withGroupValues(java.util.List)` was removed

##### `models.CustomerCertificateParameters` was modified

* `withSubjectAlternativeNames(java.util.List)` was removed

##### `models.ClientPortMatchConditionParameters` was modified

* `java.lang.String typeName()` -> `models.DeliveryRuleConditionParametersType typeName()`
* `withTypeName(java.lang.String)` was removed

##### `models.ServerPortMatchConditionParameters` was modified

* `withTypeName(java.lang.String)` was removed
* `java.lang.String typeName()` -> `models.DeliveryRuleConditionParametersType typeName()`

##### `models.DomainValidationProperties` was modified

* `DomainValidationProperties()` was changed to private access

##### `models.OperationDisplay` was modified

* `OperationDisplay()` was changed to private access

##### `models.UrlFileExtensionMatchConditionParameters` was modified

* `withTypeName(java.lang.String)` was removed
* `java.lang.String typeName()` -> `models.DeliveryRuleConditionParametersType typeName()`

##### `models.MetricSpecification` was modified

* `MetricSpecification()` was changed to private access
* `withDimensions(java.util.List)` was removed
* `withName(java.lang.String)` was removed
* `withDisplayDescription(java.lang.String)` was removed
* `withUnit(java.lang.String)` was removed
* `withIsInternal(java.lang.Boolean)` was removed
* `withMetricFilterPattern(java.lang.String)` was removed
* `withSupportedTimeGrainTypes(java.util.List)` was removed
* `withDisplayName(java.lang.String)` was removed
* `withAggregationType(java.lang.String)` was removed
* `withAvailabilities(java.util.List)` was removed
* `withFillGapWithZero(java.lang.Boolean)` was removed

##### `models.CacheExpirationActionParameters` was modified

* `withTypeName(java.lang.String)` was removed
* `java.lang.String typeName()` -> `models.DeliveryRuleActionParametersType typeName()`

##### `models.UrlRewriteActionParameters` was modified

* `withTypeName(java.lang.String)` was removed
* `java.lang.String typeName()` -> `models.DeliveryRuleActionParametersType typeName()`

##### `models.OriginGroupOverrideActionParameters` was modified

* `java.lang.String typeName()` -> `models.DeliveryRuleActionParametersType typeName()`
* `withTypeName(java.lang.String)` was removed

##### `models.UrlSigningActionParameters` was modified

* `java.lang.String typeName()` -> `models.DeliveryRuleActionParametersType typeName()`
* `withTypeName(java.lang.String)` was removed

##### `models.ManagedRuleDefinition` was modified

* `ManagedRuleDefinition()` was changed to private access

##### `models.ResourcesResponseEndpointsPropertiesItemsItem` was modified

* `ResourcesResponseEndpointsPropertiesItemsItem()` was changed to private access
* `withName(java.lang.String)` was removed
* `withHistory(java.lang.Boolean)` was removed
* `withEndpointId(java.lang.String)` was removed
* `withId(java.lang.String)` was removed

##### `models.RequestBodyMatchConditionParameters` was modified

* `java.lang.String typeName()` -> `models.DeliveryRuleConditionParametersType typeName()`
* `withTypeName(java.lang.String)` was removed

##### `models.PostArgsMatchConditionParameters` was modified

* `withTypeName(java.lang.String)` was removed
* `java.lang.String typeName()` -> `models.DeliveryRuleConditionParametersType typeName()`

##### `models.CdnCertificateSourceParameters` was modified

* `java.lang.String typeName()` -> `models.CertificateSourceParametersType typeName()`
* `withTypeName(java.lang.String)` was removed

##### `models.CidrIpAddress` was modified

* `CidrIpAddress()` was changed to private access
* `withBaseIpAddress(java.lang.String)` was removed
* `withPrefixLength(java.lang.Integer)` was removed

#### Dependency Updates

- Package api-version 2026-10-01.

### Other Changes

- Updated dependencies from resources.

## 2.65.0 (2026-10-09)

### azure-resourcemanager-resources

#### Breaking Changes

- Removed properties `ancestors`, `complianceReasonCode`, `policyAssignmentDisplayName`, `policyDefinitionDisplayName`,
  `policyDefinitionGroupNames`, `policyExemptionIds`, `policySetDefinitionCategory`, `policySetDefinitionDisplayName`, and
  `resourceLocation` from `PolicyLogInfo`, as they are not supported by the Policy service.

#### Dependency Updates

- Package api-version 2026-07-01.

- Updated `api-version` of management locks to `2020-05-01`.

### azure-resourcemanager-network

#### Bugs Fixed

- Fixed VMSS public IP address get and list operations to use the Compute API version `2018-10-01` instead of the Network API version.

#### Dependency Updates

- Package api-version Microsoft.Network: 2026-01-01, Microsoft.Compute: 2018-10-01.

### azure-resourcemanager-containerservice

#### Dependency Updates

- Package api-version 2026-07-01.

### azure-resourcemanager-trafficmanager

#### Dependency Updates

- Package api-version 2026-09-01.

### Other Changes

- Updated dependencies from resources.

## 2.64.0 (2026-09-20)

### azure-resourcemanager-compute

#### Other Changes

- Updated API versions to Compute `2026-04-01`, ComputeDisk `2026-03-02`, ComputeGallery `2025-12-03`, and
  ComputeSku `2021-07-01`.

### azure-resourcemanager-network

#### Breaking Changes

##### `models.EnableOnlyIpv6PeeringState` was removed

#### Other Changes

- Updated the Microsoft.Network API version to `2026-01-01`.

### azure-resourcemanager-msi

#### Features Added

- Supported configuring `IsolationScope` on `Identity` via the fluent `withIsolationScope(IsolationScope)` method in create and update flows.

#### Other Changes

- Updated the API version to `2024-11-30`.

### azure-resourcemanager-sql

#### Features Added

- Supported `withAzureActiveDirectoryOnlyAuthentication` and `withExternalActiveDirectoryAdministrator` for `SqlServer`.
- Supported `withManagedIdentity` for `SqlDatabase` import and export.

#### Breaking Changes

- Renamed `SecurityAlertPolicyNameAutoGenerated` class to `SecurityAlertPolicyName`.
- Removed `ManagedInstancePropertiesProvisioningState` class, replaced by `ProvisioningState`. Changed return type of `provisioningState()` in `ManagedInstanceUpdate` from `ManagedInstancePropertiesProvisioningState` to `ProvisioningState`.
- Removed `DataSyncParticipantIdentity`, `DataSyncParticipantIdentityType` and `DataSyncParticipantUserAssignedIdentity` classes. Removed `identity()` and `withIdentity()` methods from `SyncGroupInner` and `SyncMemberInner`.
- Removed `provisioningState()` method from `DatabaseInner` and `DatabaseUpdate`.
- Removed `currentSku()` method from `ElasticPoolInner` and `ElasticPoolUpdate`.
- Removed `fqdnEnabled()` and `withFqdnEnabled()` methods from `ManagedInstanceDtcInner`.
- Removed `fromInt()` method from `DiffBackupIntervalInHours`.
- Removed `zoneRedundant()` method from `ManagedInstanceEditionCapability`.
- Removed `family()` and `maintenanceConfigurationId()` methods from `VirtualClusterUpdate`.
- Removed `skip` parameter from `listByDatabase()` method in `DataMaskingRulesClient`.
- Replaced `top` and `skip` parameters with `skipToken` parameter in `listByServer()` method in `DatabasesClient`.

#### Other Changes

- Updated the API version to `2025-01-01`.

### azure-resourcemanager-appservice

#### Features Added

- Added `withManagedIdentityCredentials()` and `withManagedIdentityCredentials(String userAssignedManagedIdentityClientId)` to `WebApp` and `FunctionApp` (definition and update) and deployment slots (`DeploymentSlot`/`FunctionDeploymentSlot` update), to pull a private container image (e.g. from Azure Container Registry) using the app's or slot's managed identity instead of a registry username and password.

### azure-resourcemanager-containerservice

#### Other Changes

- Updated the API version to `2026-06-01`.

### Other Changes

- Updated dependencies.

## 2.63.0 (2026-07-07)

### azure-resourcemanager-compute

#### Features Added

- Added interconnect block/group profile support, including `InterconnectBlockProfile`, `InterconnectGroupProfile`, `InterconnectBlockProperties`, `InterconnectBlockInstanceView`, `InterconnectInstanceView`, `AutomaticSkuMigrationPolicy`, and related properties on virtual machine scale set VM/network profile and SKU profile models.

#### Dependency Updates

- Updated `ComputeRP api-version` to `2026-03-01`.

### azure-resourcemanager-containerservice

#### Dependency Updates

- Updated `api-version` to `2026-04-01`.

### Other Changes

- Updated dependencies from resources.

## 2.62.0 (2026-05-08)

### azure-resourcemanager-compute

#### Breaking Changes

- Moved `ComputeManager.serviceClient().getCloudServiceRoleInstances()` to `ComputeManager.cloudServiceClient().getCloudServiceRoleInstances()`.
- Moved `ComputeManager.serviceClient().getCloudServiceRoles()` to `ComputeManager.cloudServiceClient().getCloudServiceRoles()`.
- Moved `ComputeManager.serviceClient().getCloudServices()` to `ComputeManager.cloudServiceClient().getCloudServices()`.
- Moved `ComputeManager.serviceClient().getCloudServicesUpdateDomains()` to `ComputeManager.cloudServiceClient().getCloudServicesUpdateDomains()`.
- Moved `ComputeManager.serviceClient().getCloudServiceOperatingSystems()` to `ComputeManager.cloudServiceClient().getCloudServiceOperatingSystems()`.

#### Dependency Updates

- Updated `ComputeRP api-version` to `2025-11-01`.

### azure-resourcemanager-containerservice

#### Dependency Updates

- Updated `api-version` to `2026-02-01`.

### Other Changes

- Updated dependencies from resources.

## 2.61.0 (2026-04-03)

### azure-resourcemanager-containerservice

#### Breaking Changes

- Moved `ContainerServiceManager.serviceClient().getOpenShiftManagedClusters()` to `ContainerServiceManager.openShiftClient().getOpenShiftManagedClusters()`.
- Moved `ContainerServiceManager.serviceClient().getContainerServices()` to `ContainerServiceManager.orchestratorClient().getContainerServices()`.

#### Dependency Updates

- Updated `api-version` to `2026-01-01`.

### azure-resourcemanager-resources

- Updated `api-version` of policy to `2025-03-01`.

### azure-resourcemanager-keyvault

#### Dependency Updates

- Updated `api-version` to `2026-02-01`.

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.60.0 (2026-02-26)

### azure-resourcemanager-network

#### Breaking Changes

- Removed `ADVANCED`, `BASIC`, and `STANDARD` from `FirewallPolicyIntrusionDetectionProfileType` to align with the service model.

#### Dependency Updates

- Updated `api-version` to `2025-05-01`.

### azure-resourcemanager-containerregistry

#### Breaking Changes

- Removed `resourceGroupName` and `autoGeneratedDomainNameLabelScope` from `RegistryNameCheckRequest` class. These APIs do not exist in api-version 2025-11-01.

##### Changes to `ContainerRegistryManagementClient` Class

- Moved `ContainerRegistryManager.serviceClient().getAgentPools()` to `ContainerRegistryManager.taskClient().getAgentPools()`.
- Moved `ContainerRegistryManager.serviceClient().getRuns()` to `ContainerRegistryManager.taskClient().getRuns()`.
- Moved `ContainerRegistryManager.serviceClient().getTaskRuns()` to `ContainerRegistryManager.taskClient().getTaskRuns()`.
- Moved `ContainerRegistryManager.serviceClient().getTasks()` to `ContainerRegistryManager.taskClient().getTasks()`.
- Moved `ContainerRegistryManager.serviceClient().getRegistries().scheduleRun(...)` to `ContainerRegistryManager.taskClient().getRegistryTasks().scheduleRun(...)`.
- Moved `ContainerRegistryManager.serviceClient().getRegistries().getBuildSourceUploadUrl(...)` to `ContainerRegistryManager.taskClient().getRegistryTasks().getBuildSourceUploadUrl(...)`.

#### Other Changes

- Registry Tasks related feature is deprecated in this library. Please use "azure-resourcemanager-containerregistry-tasks" library.

### azure-resourcemanager-appservice

#### Breaking Changes

- Removed `withPassword` method from `CertificatePatchResource` class. The password property is read-only.

##### Changes to `WebSiteManagementClient` Class

- Moved `AppServiceManager.serviceClient().getCertificateRegistrationProviders()` to `AppServiceManager.certificateRegistrationClient().getCertificateRegistrationProviders()`.
- Moved `AppServiceManager.serviceClient().getAppServiceCertificateOrders()` to `AppServiceManager.certificateRegistrationClient().getAppServiceCertificateOrders()`.
- Moved `AppServiceManager.serviceClient().getCertificateOrdersDiagnostics()` to `AppServiceManager.certificateRegistrationClient().getCertificateOrdersDiagnostics()`.
- Moved `AppServiceManager.serviceClient().getDomainRegistrationProviders()` to `AppServiceManager.domainRegistrationClient().getDomainRegistrationProviders()`.
- Moved `AppServiceManager.serviceClient().getDomains()` to `AppServiceManager.domainRegistrationClient().getDomains()`.
- Moved `AppServiceManager.serviceClient().getTopLevelDomains()` to `AppServiceManager.domainRegistrationClient().getTopLevelDomains()`.

#### Other Changes

- Certificate Registration related feature is deprecated in this library. Please use "azure-resourcemanager-certificateregistration" library.
- Domain Registration related feature is deprecated in this library. Please use "azure-resourcemanager-domainregistration" library.

#### Dependency Updates

- Updated `api-version` of AppService to `2025-05-01`.

#### Dependency Updates

- Updated core dependency from resources.

## 2.59.0 (2026-01-30)

### azure-resourcemanager-redis

#### Features Added

- Supported enabling/disabling authentication through access keys for `RedisCache`.

### Other Changes

### azure-resourcemanager-compute

#### Dependency Updates

- Updated `GalleryRP api-version` to `2025-03-03`.

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.58.0 (2025-12-22)

### azure-resourcemanager-containerservice

#### Dependency Updates

- Updated `api-version` to `2025-10-01`.

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.57.0 (2025-11-25)

### azure-resourcemanager-containerregistry

#### Dependency Updates

- Updated `api-version` to `2025-11-01`.

### azure-resourcemanager-appservice

#### Dependency Updates

- Updated `api-version` of AppService to `2025-03-01`.

### azure-resourcemanager-network

#### Dependency Updates

- Updated `api-version` to `2025-03-01`.

#### Features Added

- Added `STANDARD_V2` to `PublicIPSkuType`.

#### Bugs Fixed

- Fixed the bug that `list()` in `LocalNetworkGateways`, `VirtualNetworkGateways`, `VirtualNetworkGatewayConnections` only return resources from 1 resource group.
- Improved `VirtualNetworkGateway` to create Public IP Address with Standard SKU (unless the gateway is Basic SKU).
- Improved `PublicIpAddress` to set the public IP allocation method to Static (unless the Public IP Address is Basic SKU).

### azure-resourcemanager-containerservice

#### Dependency Updates

- Updated `api-version` to `2025-09-01`.

### azure-resourcemanager-cosmos

#### Dependency Updates

- Updated `api-version` to `2025-10-15`

### azure-resourcemanager-resources

#### Other Changes

- Optimized `AzureServiceClient` to load library versions during class initialization.

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.56.0 (2025-10-31)

### azure-resourcemanager-containerservice

#### Dependency Updates

- Updated `api-version` to `2025-08-01`.

### azure-resourcemanager-network

#### Bugs Fixed

- Fixed a bug that `ApplicationGateway.availabilityZones()` throws exception.

#### Dependency Updates

- Updated `api-version` to `2024-10-01`.

### azure-resourcemanager-keyvault

#### Dependency Updates

- Updated `api-version` to `2025-05-01`.

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.55.0 (2025-09-30)

### azure-resourcemanager-network

#### Bugs Fixed

- Fixed a bug that `NetworkSecurityGroup.listAssociatedSubnets` returns a list of `null`.

### azure-resourcemanager-storage

#### Dependency Updates

- Updated `api-version` to `2025-01-01`.

### azure-resourcemanager-compute

#### Dependency Updates

- Updated `api-version` to `2025-05-01`.

### azure-resourcemanager-containerservice

#### Dependency Updates

- Updated `api-version` to `2025-07-01`.

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.54.0 (2025-08-27)

### azure-resourcemanager-search

#### Dependency Updates

- Updated `api-version` to `2025-05-01`.

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.53.1 (2025-08-05)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.53.0 (2025-07-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.52.0 (2025-06-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.51.0 (2025-05-26)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.50.0 (2025-04-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.49.0 (2025-03-28)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.48.0 (2025-02-28)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.47.0 (2025-01-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.46.0 (2024-12-23)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.45.0 (2024-11-28)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.44.0 (2024-10-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.43.0 (2024-09-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.42.0 (2024-08-23)

### Other Changes

- Replaced `Jackson` with `azure-json` for serialization/deserialization.

## 2.41.0 (2024-07-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.40.0 (2024-06-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.39.0 (2024-05-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.38.0 (2024-04-16)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.37.0 (2024-03-22)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.36.0 (2024-02-29)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.35.0 (2024-01-26)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.34.0 (2023-12-22)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.33.0 (2023-11-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.32.0 (2023-10-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.31.0 (2023-09-28)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.30.0 (2023-08-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.29.0 (2023-07-28)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.28.0 (2023-06-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.27.0 (2023-05-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.26.0 (2023-04-21)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.25.0 (2023-03-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.24.0 (2023-02-17)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.23.0 (2023-01-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.22.0 (2022-12-23)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.21.0 (2022-11-24)

### Breaking Changes

- Property `uri` removed from class `GalleryArtifactVersionSource`. This property was non-functional.
- Type of property `GalleryImageVersionStorageProfile.source` changed to the class `GalleryArtifactVersionFullSource`, a subclass of `GalleryArtifactVersionSource`.
- Type of property `GalleryOSDiskImage.source` changed to the class `GalleryDiskImageSource`, a subclass of `GalleryArtifactVersionSource`.

## 2.20.0 (2022-10-26)

### Bugs Fixed

- Fixed bug that `ProviderRegistrationPolicy` does not work.

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.19.0 (2022-09-23)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.18.0 (2022-08-26)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.17.0 (2022-07-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.16.0 (2022-06-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.15.0 (2022-05-25)

### Bugs Fixed

- Fixed a bug that configure of `RetryOptions` does not take effect.

## 2.14.0 (2022-04-11)

### Features Added

- Supported `authenticate(HttpPipeline, AzureProfile)` in managers.

## 2.13.0 (2022-03-11)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.12.0 (2022-02-14)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources

## 2.11.0 (2022-01-17)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources

## 2.10.0 (2021-11-22)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources

## 2.9.0 (2021-10-21)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources

## 2.8.0 (2021-09-15)

### Dependency Updates

- Updated core dependency from resources

## 2.7.0 (2021-08-12)

### Features Added

- Added overload of `create(Context)` for `Creatable`.
- Added overload of `apply(Context)` for `Appliable`.
### Dependency Updates

- Updated core dependency from resources

## 2.6.0 (2021-06-18)

- Added support for Challenge Based Authentication in `AuthenticationPolicy`.

## 2.5.0 (2021-05-28)
- Updated core dependency from resources

## 2.4.0 (2021-04-28)

- Updated parameter of `SupportsBatchCreation.create` from `List<Creatable<ResourceT>>` to `List<? extends Creatable<ResourceT>>`

## 2.3.0 (2021-03-30)

- Added client-side validation for `getByResourceGroup`, `listByResourceGroup`, `deleteByResourceGroup` methods.

## 2.2.0 (2021-02-24)

- Improved performance of `PagedIterable`

## 2.1.0 (2020-11-24)

- Updated core dependency from resources

## 2.0.0 (2020-10-19)

- Removed non-GA packages

## 2.0.0-beta.5 (2020-10-19)

- Renamed `Azure` to `AzureResourceManager`

## 2.0.0-beta.4 (2020-09-02)

- Added `PrivateDns`
- Added `Redis`
- Added `EventHubs`
- Added `TrafficMananger`
- Exposed `subscriptions`, `tenants` and `tenantId` API

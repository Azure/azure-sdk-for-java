# Release History

## 2.54.0 (2026-08-20)

- Package api-version 2026-07-01.

### Breaking Changes

#### `models.RuleListResult` was removed

#### `models.UsagesListResult` was removed

#### `models.SecurityPolicyListResult` was removed

#### `models.OperationsListResult` was removed

#### `models.RuleSetListResult` was removed

#### `models.CdnWebApplicationFirewallPolicyList` was removed

#### `models.ManagedRuleSetDefinitionList` was removed

#### `models.AfdDomainListResult` was removed

#### `models.ResourceUsageListResult` was removed

#### `models.AfdEndpointListResult` was removed

#### `models.RouteListResult` was removed

#### `models.AfdOriginGroupListResult` was removed

#### `models.OriginGroupListResult` was removed

#### `models.EndpointListResult` was removed

#### `models.ProfileListResult` was removed

#### `models.EdgenodeResult` was removed

#### `models.SecretListResult` was removed

#### `models.OriginListResult` was removed

#### `models.AfdOriginListResult` was removed

#### `models.CustomDomainListResult` was removed

#### `models.DeepCreatedCustomDomain` was modified

* `DeepCreatedCustomDomain()` was changed to private access
* `withHostname(java.lang.String)` was removed
* `withValidationData(java.lang.String)` was removed
* `withName(java.lang.String)` was removed

#### `models.RequestSchemeMatchConditionParameters` was modified

* `withOperator(java.lang.String)` was removed
* `java.lang.String typeName()` -> `models.DeliveryRuleConditionParametersType typeName()`
* `withTypeName(java.lang.String)` was removed

#### `models.RequestUriMatchConditionParameters` was modified

* `java.lang.String typeName()` -> `models.DeliveryRuleConditionParametersType typeName()`
* `withTypeName(java.lang.String)` was removed

#### `models.WafMetricsResponseSeriesItem` was modified

* `WafMetricsResponseSeriesItem()` was changed to private access
* `withUnit(models.WafMetricsSeriesUnit)` was removed
* `withData(java.util.List)` was removed
* `withMetric(java.lang.String)` was removed
* `withGroups(java.util.List)` was removed

#### `models.RankingsResponseTablesPropertiesItemsItem` was modified

* `RankingsResponseTablesPropertiesItemsItem()` was changed to private access
* `withMetrics(java.util.List)` was removed
* `withName(java.lang.String)` was removed

#### `models.EndpointResource` was modified

* `EndpointResource()` was changed to private access
* `withId(java.lang.String)` was removed

#### `models.KeyVaultCertificateSourceParameters` was modified

* `withTypeName(java.lang.String)` was removed
* `java.lang.String typeName()` -> `models.CertificateSourceParametersType typeName()`

#### `models.KeyVaultSigningKeyParameters` was modified

* `withTypeName(java.lang.String)` was removed
* `java.lang.String typeName()` -> `models.KeyVaultSigningKeyParametersType typeName()`

#### `models.MetricsResponseSeriesItem` was modified

* `MetricsResponseSeriesItem()` was changed to private access
* `withGroups(java.util.List)` was removed
* `withData(java.util.List)` was removed
* `withUnit(models.MetricsSeriesUnit)` was removed
* `withMetric(java.lang.String)` was removed

#### `models.IpAddressGroup` was modified

* `IpAddressGroup()` was changed to private access
* `withIpv4Addresses(java.util.List)` was removed
* `withDeliveryRegion(java.lang.String)` was removed
* `withIpv6Addresses(java.util.List)` was removed

#### `models.RequestMethodMatchConditionParameters` was modified

* `java.lang.String typeName()` -> `models.DeliveryRuleConditionParametersType typeName()`
* `withTypeName(java.lang.String)` was removed

#### `models.RankingsResponseTablesItem` was modified

* `RankingsResponseTablesItem()` was changed to private access
* `withRanking(java.lang.String)` was removed
* `withData(java.util.List)` was removed

#### `models.RankingsResponseTablesPropertiesItemsMetricsItem` was modified

* `RankingsResponseTablesPropertiesItemsMetricsItem()` was changed to private access
* `withMetric(java.lang.String)` was removed
* `withPercentage(java.lang.Float)` was removed
* `withValue(java.lang.Long)` was removed

#### `models.UrlFileNameMatchConditionParameters` was modified

* `java.lang.String typeName()` -> `models.DeliveryRuleConditionParametersType typeName()`
* `withTypeName(java.lang.String)` was removed

#### `models.RouteConfigurationOverrideActionParameters` was modified

* `withTypeName(java.lang.String)` was removed
* `java.lang.String typeName()` -> `models.DeliveryRuleActionParametersType typeName()`

#### `models.ContinentsResponseCountryOrRegionsItem` was modified

* `ContinentsResponseCountryOrRegionsItem()` was changed to private access
* `withContinentId(java.lang.String)` was removed
* `withId(java.lang.String)` was removed

#### `models.UrlRedirectActionParameters` was modified

* `java.lang.String typeName()` -> `models.DeliveryRuleActionParametersType typeName()`
* `withTypeName(java.lang.String)` was removed

#### `models.SslProtocolMatchConditionParameters` was modified

* `withTypeName(java.lang.String)` was removed
* `java.lang.String typeName()` -> `models.DeliveryRuleConditionParametersType typeName()`

#### `models.RequestHeaderMatchConditionParameters` was modified

* `withTypeName(java.lang.String)` was removed
* `java.lang.String typeName()` -> `models.DeliveryRuleConditionParametersType typeName()`

#### `models.CacheKeyQueryStringActionParameters` was modified

* `withTypeName(java.lang.String)` was removed
* `java.lang.String typeName()` -> `models.DeliveryRuleActionParametersType typeName()`

#### `models.HttpVersionMatchConditionParameters` was modified

* `withTypeName(java.lang.String)` was removed
* `java.lang.String typeName()` -> `models.DeliveryRuleConditionParametersType typeName()`

#### `models.MetricsResponseSeriesPropertiesItemsItem` was modified

* `MetricsResponseSeriesPropertiesItemsItem()` was changed to private access
* `withName(java.lang.String)` was removed
* `withValue(java.lang.String)` was removed

#### `models.UsageName` was modified

* `UsageName()` was changed to private access
* `withValue(java.lang.String)` was removed
* `withLocalizedValue(java.lang.String)` was removed

#### `models.PolicySettingsDefaultCustomBlockResponseStatusCode` was modified

* `PolicySettingsDefaultCustomBlockResponseStatusCode()` was removed
* `fromInt(int)` was removed

#### `models.ResourcesResponseCustomDomainsItem` was modified

* `ResourcesResponseCustomDomainsItem()` was changed to private access
* `withEndpointId(java.lang.String)` was removed
* `withId(java.lang.String)` was removed
* `withName(java.lang.String)` was removed
* `withHistory(java.lang.Boolean)` was removed

#### `models.CookiesMatchConditionParameters` was modified

* `withTypeName(java.lang.String)` was removed
* `java.lang.String typeName()` -> `models.DeliveryRuleConditionParametersType typeName()`

#### `models.MigrationErrorType` was modified

* `MigrationErrorType()` was changed to private access

#### `models.IsDeviceMatchConditionParameters` was modified

* `withTypeName(java.lang.String)` was removed
* `java.lang.String typeName()` -> `models.DeliveryRuleConditionParametersType typeName()`

#### `models.ManagedRuleGroupDefinition` was modified

* `ManagedRuleGroupDefinition()` was changed to private access

#### `models.UrlPathMatchConditionParameters` was modified

* `java.lang.String typeName()` -> `models.DeliveryRuleConditionParametersType typeName()`
* `withTypeName(java.lang.String)` was removed

#### `models.HostnameMatchConditionParameters` was modified

* `java.lang.String typeName()` -> `models.DeliveryRuleConditionParametersType typeName()`
* `withTypeName(java.lang.String)` was removed

#### `models.ResourcesResponseEndpointsItem` was modified

* `ResourcesResponseEndpointsItem()` was changed to private access
* `withHistory(java.lang.Boolean)` was removed
* `withName(java.lang.String)` was removed
* `withId(java.lang.String)` was removed
* `withCustomDomains(java.util.List)` was removed

#### `models.MetricsResponseSeriesItemData` was modified

* `MetricsResponseSeriesItemData()` was changed to private access
* `withDateTime(java.time.OffsetDateTime)` was removed
* `withValue(java.lang.Float)` was removed

#### `models.QueryStringMatchConditionParameters` was modified

* `java.lang.String typeName()` -> `models.DeliveryRuleConditionParametersType typeName()`
* `withTypeName(java.lang.String)` was removed

#### `models.WafMetricsResponseSeriesPropertiesItemsItem` was modified

* `WafMetricsResponseSeriesPropertiesItemsItem()` was changed to private access
* `withValue(java.lang.String)` was removed
* `withName(java.lang.String)` was removed

#### `models.ContinentsResponseContinentsItem` was modified

* `ContinentsResponseContinentsItem()` was changed to private access
* `withId(java.lang.String)` was removed

#### `models.HeaderActionParameters` was modified

* `withTypeName(java.lang.String)` was removed
* `java.lang.String typeName()` -> `models.DeliveryRuleActionParametersType typeName()`

#### `models.SocketAddrMatchConditionParameters` was modified

* `java.lang.String typeName()` -> `models.DeliveryRuleConditionParametersType typeName()`
* `withTypeName(java.lang.String)` was removed

#### `models.LogSpecification` was modified

* `LogSpecification()` was changed to private access
* `withDisplayName(java.lang.String)` was removed
* `withName(java.lang.String)` was removed
* `withLogFilterPattern(java.lang.String)` was removed
* `withBlobDuration(java.lang.String)` was removed

#### `models.RemoteAddressMatchConditionParameters` was modified

* `withTypeName(java.lang.String)` was removed
* `java.lang.String typeName()` -> `models.DeliveryRuleConditionParametersType typeName()`

#### `models.WafMetricsResponseSeriesItemData` was modified

* `WafMetricsResponseSeriesItemData()` was changed to private access
* `withDateTime(java.time.OffsetDateTime)` was removed
* `withValue(java.lang.Float)` was removed

#### `models.MetricAvailability` was modified

* `MetricAvailability()` was changed to private access
* `withBlobDuration(java.lang.String)` was removed
* `withTimeGrain(java.lang.String)` was removed

#### `models.WafRankingsResponseDataItemMetric` was modified

* `WafRankingsResponseDataItemMetric()` was changed to private access
* `withValue(java.lang.Long)` was removed
* `withMetric(java.lang.String)` was removed
* `withPercentage(java.lang.Double)` was removed

#### `models.DimensionProperties` was modified

* `DimensionProperties()` was changed to private access
* `withInternalName(java.lang.String)` was removed
* `withName(java.lang.String)` was removed
* `withDisplayName(java.lang.String)` was removed

#### `models.ServiceSpecification` was modified

* `ServiceSpecification()` was changed to private access
* `withMetricSpecifications(java.util.List)` was removed
* `withLogSpecifications(java.util.List)` was removed

#### `models.WafRankingsResponseDataItem` was modified

* `WafRankingsResponseDataItem()` was changed to private access
* `withGroupValues(java.util.List)` was removed
* `withMetrics(java.util.List)` was removed

#### `models.CustomerCertificateParameters` was modified

* `withSubjectAlternativeNames(java.util.List)` was removed

#### `models.ClientPortMatchConditionParameters` was modified

* `withTypeName(java.lang.String)` was removed
* `java.lang.String typeName()` -> `models.DeliveryRuleConditionParametersType typeName()`

#### `models.ServerPortMatchConditionParameters` was modified

* `java.lang.String typeName()` -> `models.DeliveryRuleConditionParametersType typeName()`
* `withTypeName(java.lang.String)` was removed

#### `models.DomainValidationProperties` was modified

* `DomainValidationProperties()` was changed to private access

#### `models.OperationDisplay` was modified

* `OperationDisplay()` was changed to private access

#### `models.UrlFileExtensionMatchConditionParameters` was modified

* `java.lang.String typeName()` -> `models.DeliveryRuleConditionParametersType typeName()`
* `withTypeName(java.lang.String)` was removed

#### `models.MetricSpecification` was modified

* `MetricSpecification()` was changed to private access
* `withUnit(java.lang.String)` was removed
* `withDimensions(java.util.List)` was removed
* `withDisplayDescription(java.lang.String)` was removed
* `withAvailabilities(java.util.List)` was removed
* `withAggregationType(java.lang.String)` was removed
* `withName(java.lang.String)` was removed
* `withFillGapWithZero(java.lang.Boolean)` was removed
* `withMetricFilterPattern(java.lang.String)` was removed
* `withSupportedTimeGrainTypes(java.util.List)` was removed
* `withIsInternal(java.lang.Boolean)` was removed
* `withDisplayName(java.lang.String)` was removed

#### `models.CacheExpirationActionParameters` was modified

* `java.lang.String typeName()` -> `models.DeliveryRuleActionParametersType typeName()`
* `withTypeName(java.lang.String)` was removed

#### `models.UrlRewriteActionParameters` was modified

* `withTypeName(java.lang.String)` was removed
* `java.lang.String typeName()` -> `models.DeliveryRuleActionParametersType typeName()`

#### `models.OriginGroupOverrideActionParameters` was modified

* `java.lang.String typeName()` -> `models.DeliveryRuleActionParametersType typeName()`
* `withTypeName(java.lang.String)` was removed

#### `models.UrlSigningActionParameters` was modified

* `java.lang.String typeName()` -> `models.DeliveryRuleActionParametersType typeName()`
* `withTypeName(java.lang.String)` was removed

#### `models.ManagedRuleDefinition` was modified

* `ManagedRuleDefinition()` was changed to private access

#### `models.ResourcesResponseEndpointsPropertiesItemsItem` was modified

* `ResourcesResponseEndpointsPropertiesItemsItem()` was changed to private access
* `withEndpointId(java.lang.String)` was removed
* `withName(java.lang.String)` was removed
* `withId(java.lang.String)` was removed
* `withHistory(java.lang.Boolean)` was removed

#### `models.RequestBodyMatchConditionParameters` was modified

* `java.lang.String typeName()` -> `models.DeliveryRuleConditionParametersType typeName()`
* `withTypeName(java.lang.String)` was removed

#### `models.PostArgsMatchConditionParameters` was modified

* `java.lang.String typeName()` -> `models.DeliveryRuleConditionParametersType typeName()`
* `withTypeName(java.lang.String)` was removed

#### `models.CdnCertificateSourceParameters` was modified

* `withTypeName(java.lang.String)` was removed
* `java.lang.String typeName()` -> `models.CertificateSourceParametersType typeName()`

#### `models.CidrIpAddress` was modified

* `CidrIpAddress()` was changed to private access
* `withPrefixLength(java.lang.Integer)` was removed
* `withBaseIpAddress(java.lang.String)` was removed

### Features Added

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

* `models.AfdUrlSigningActionParameters` was added

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

* `models.AfdUrlSigningAction` was added

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

* `models.TypeName` was added

* `models.Route$UpdateDefinitionStages$Blank` was added

#### `models.CdnProfile` was modified

* `ruleSets()` was added
* `afdEndpoints()` was added
* `originGroups()` was added

#### `models.CdnProfile$Definition` was modified

* `withGlobal()` was added

#### `models.KeyVaultSigningKeyParameters` was modified

* `withTypeName(models.KeyVaultSigningKeyParametersType)` was added

#### `models.AfdOriginGroupUpdateParameters` was modified

* `withAuthentication(models.OriginAuthenticationProperties)` was added
* `authentication()` was added

#### `models.PolicySettingsDefaultCustomBlockResponseStatusCode` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `hashCode()` was added
* `fromValue(java.lang.Integer)` was added
* `equals(java.lang.Object)` was added
* `getValue()` was added
* `toString()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AfdMinimumTlsVersion` was modified

* `TLS13` was added

#### `models.SecurityPolicyWebApplicationFirewallAssociation` was modified

* `withRoutes(java.util.List)` was added
* `routes()` was added

#### `models.AfdOriginUpdateParameters` was modified

* `withCustomCertificateSubjects(java.util.List)` was added
* `certificateNameCheckValidationMode()` was added
* `customCertificateSubjects()` was added
* `withCertificateNameCheckValidationMode(models.CertificateNameCheckValidationMode)` was added

#### `models.AfdDomainHttpsParameters` was modified

* `cipherSuiteSetType()` was added
* `withServerTlsGroups(java.util.List)` was added
* `serverTlsGroupPolicy()` was added
* `withCustomizedCipherSuiteSet(models.AfdDomainHttpsCustomizedCipherSuiteSet)` was added
* `serverTlsGroups()` was added
* `customizedCipherSuiteSet()` was added
* `withCipherSuiteSetType(models.AfdCipherSuiteSetType)` was added
* `withServerTlsGroupPolicy(models.AfdServerTlsGroupPolicy)` was added

#### `models.AfdEndpointUpdateParameters` was modified

* `withEnforceMtls(models.EnforceMtlsEnabledState)` was added
* `enforceMtls()` was added

#### `models.AfdDomainUpdateParameters` was modified

* `mtlsSettings()` was added
* `withMtlsSettings(models.AfdDomainMtlsParameters)` was added

#### `models.SecretType` was modified

* `MTLS_CERTIFICATE_CHAIN` was added

#### `models.SecurityPolicyWebApplicationFirewallParameters` was modified

* `isProfileLevel()` was added
* `withIsProfileLevel(java.lang.Boolean)` was added

#### `models.DeliveryRuleActionValue` was modified

* `AFD_URL_SIGNING` was added
* `EDGE_ACTION` was added

## 2.53.9 (2026-07-01)

### Other Changes

#### Dependency Updates

- Upgraded `azure-resourcemanager-resources` from `2.54.1` to version `2.54.2`.


## 2.53.8 (2026-05-05)

### Other Changes

#### Dependency Updates

- Upgraded `azure-resourcemanager-resources` from `2.54.0` to version `2.54.1`.

## 2.53.7 (2026-03-30)

### Other Changes

#### Dependency Updates

- Upgraded `azure-resourcemanager-resources` from `2.53.5` to version `2.54.0`.


## 2.53.6 (2026-01-29)

### Other Changes

#### Dependency Updates

- Upgraded core dependencies.

## 2.53.5 (2025-11-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.53.4 (2025-10-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.53.3 (2025-09-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.53.2 (2025-08-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.53.1 (2025-08-05)

### Bugs Fixed

- Fixed a bug in `CdnEndpoint.withoutCustomDomain(hostname)` implementation. Now it should work as expected.

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

- Updated `api-version` to `2024-02-01`.

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

### Breaking Changes

- Renamed `MetricsResponseGranularity` to `MetricsGranularity`.
- Renamed `MetricsResponseSeriesItemUnit` to `MetricsSeriesUnit`.
- Renamed `WafMetricsResponseGranularity` to `WafMetricsGranularity`.
- Renamed `WafMetricsResponseSeriesItemUnit` to `WafMetricsSeriesUnit`.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2023-05-01`.

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

### Features Added

- Supported `defineNewStandardRulesEngineRule` and `updateStandardRulesEngineRule` in `CdnEndpoint` for Standard Microsoft Sku.
- Supported `withStandardMicrosoftSku` in `CdnProfile`.

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

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.20.0 (2022-10-26)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.19.0 (2022-09-23)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.18.0 (2022-08-26)

### Breaking Changes

- Fixed incorrect class names of `MetricsResponseSeriesItemData`, `WafMetricsResponseSeriesItemData`, `WafRankingsResponseDataItemMetric`.
- Removed classes not supported in backend `ManagedServiceIdentity`, `UserAssignedIdentity`, `ManagedServiceIdentityType`.

## 2.17.0 (2022-07-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.16.0 (2022-06-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.15.0 (2022-05-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.14.0 (2022-04-11)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.13.0 (2022-03-11)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.12.0 (2022-02-14)

### Breaking Changes

- Azure Front Door.
- Changed in class `CheckNameAvailabilityInput` that `type` no longer have a default value and hence required to be specified.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2021-06-01`.

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

### Dependency Updates

- Updated core dependency from resources

## 2.6.0 (2021-06-18)

- Updated core dependency from resources

## 2.5.0 (2021-05-28)
- Updated core dependency from resources

## 2.4.0 (2021-04-28)

- Updated core dependency from resources

## 2.3.0 (2021-03-30)

- Updated core dependency from resources

## 2.2.0 (2021-02-24)

- Updated `api-version` to `2020-09-01`
- Removed `UrlSigningActionParametersOdataType`
- Type of property `odataType` in `UrlSigningActionParameters` changed from `UrlSigningActionParametersOdataType` to `String`
- Type of property `keyId` in `UrlSigningActionParameters` removed

## 2.1.0 (2020-11-24)

- Updated `api-version` to `2020-04-15`

## 2.0.0-beta.5 (2020-10-19)

- Migrated from previous sdk

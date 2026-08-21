# Release History

## 2.54.0 (2026-08-20)

- Package api-version 2026-07-01.

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

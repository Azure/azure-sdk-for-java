# Release History

## 2.1.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 2.0.0 (2025-04-23)

- Azure Resource Manager ApiManagement client library for Java. This package contains Microsoft Azure SDK for ApiManagement Management SDK. ApiManagement Client. Package tag package-2024-05. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.PortalConfigCollection` was modified

* `java.lang.String nextLink()` -> `java.lang.String nextLink()`
* `java.util.List value()` -> `java.util.List value()`
* `innerModel()` was removed

#### `models.ApiManagementServices` was modified

* `migrateToStv2(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Apis` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was removed
* `com.azure.core.http.rest.Response deleteByIdWithResponse(java.lang.String,java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` -> `void deleteByIdWithResponse(java.lang.String,java.lang.String,java.lang.Boolean,com.azure.core.util.Context)`

#### `models.ApiManagementServiceResource` was modified

* `migrateToStv2(com.azure.core.util.Context)` was removed

#### `models.Users` was modified

* `com.azure.core.http.rest.Response deleteByIdWithResponse(java.lang.String,java.lang.String,java.lang.Boolean,java.lang.Boolean,models.AppType,com.azure.core.util.Context)` -> `void deleteByIdWithResponse(java.lang.String,java.lang.String,java.lang.Boolean,java.lang.Boolean,models.AppType,com.azure.core.util.Context)`
* `deleteWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Boolean,java.lang.Boolean,models.AppType,com.azure.core.util.Context)` was removed

#### `models.PortalConfigs` was modified

* `models.PortalConfigCollection listByService(java.lang.String,java.lang.String)` -> `com.azure.core.http.rest.PagedIterable listByService(java.lang.String,java.lang.String)`
* `listByServiceWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.PolicyFragmentCollection` was modified

* `java.lang.Long count()` -> `java.lang.Long count()`
* `java.util.List value()` -> `java.util.List value()`
* `innerModel()` was removed
* `java.lang.String nextLink()` -> `java.lang.String nextLink()`

#### `models.OperationResultContract` was modified

* `com.azure.core.management.exception.ManagementError error()` -> `models.ErrorResponseBody error()`

#### `models.Policies` was modified

* `models.PolicyCollection listByService(java.lang.String,java.lang.String)` -> `com.azure.core.http.rest.PagedIterable listByService(java.lang.String,java.lang.String)`
* `listByServiceWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.ProductPolicies` was modified

* `listByProductWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `models.PolicyCollection listByProduct(java.lang.String,java.lang.String,java.lang.String)` -> `com.azure.core.http.rest.PagedIterable listByProduct(java.lang.String,java.lang.String,java.lang.String)`

#### `models.PolicyFragments` was modified

* `models.PolicyFragmentCollection listByService(java.lang.String,java.lang.String)` -> `com.azure.core.http.rest.PagedIterable listByService(java.lang.String,java.lang.String)`
* `listByServiceWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,java.lang.Integer,com.azure.core.util.Context)` was removed

### Features Added

* `models.WorkspaceSubscriptionsGetHeaders` was added

* `models.WorkspaceApiOperationsCreateOrUpdateHeaders` was added

* `models.WorkspaceLoggers` was added

* `models.WorkspaceProductApiLinks` was added

* `models.ApiManagementWorkspaceLinks` was added

* `models.AllPolicies` was added

* `models.TagApiLinkCollection` was added

* `models.WorkspaceApiDiagnosticsGetEntityTagHeaders` was added

* `models.WorkspaceCertificatesGetHeaders` was added

* `models.WorkspacesGetHeaders` was added

* `models.WorkspaceLoggersGetEntityTagResponse` was added

* `models.WorkspaceApiRevisions` was added

* `models.WorkspaceProductsGetHeaders` was added

* `models.WorkspaceProductGroupLinksGetHeaders` was added

* `models.WorkspaceLoggersGetResponse` was added

* `models.WorkspaceBackendsGetEntityTagHeaders` was added

* `models.ApiManagementGatewayListResult` was added

* `models.WorkspacePoliciesCreateOrUpdateHeaders` was added

* `models.ApiManagementGatewayConfigConnectionResource$Update` was added

* `models.ApiManagementGatewayConfigConnectionResource` was added

* `models.BackendPoolItem` was added

* `models.ProductApiLinks` was added

* `models.WorkspaceNamedValuesGetResponse` was added

* `models.ProductApiLinkContract$Definition` was added

* `models.WorkspaceGlobalSchemasGetResponse` was added

* `models.WorkspaceNamedValuesListValueResponse` was added

* `models.TagOperationLinksGetResponse` was added

* `models.WorkspaceGlobalSchemasGetEntityTagResponse` was added

* `models.WorkspaceSubscriptionsUpdateHeaders` was added

* `models.ProductGroupLinksGetResponse` was added

* `models.WorkspaceApiDiagnosticsCreateOrUpdateResponse` was added

* `models.BackendSubnetConfiguration` was added

* `models.WorkspaceApiOperationsGetResponse` was added

* `models.WorkspaceApiVersionSets` was added

* `models.WorkspaceNotifications` was added

* `models.WorkspaceTagApiLinksGetResponse` was added

* `models.WorkspaceTagProductLinks` was added

* `models.WorkspaceTagsGetEntityStateResponse` was added

* `models.PolicyRestrictionsCreateOrUpdateHeaders` was added

* `models.GatewaySku` was added

* `models.WorkspaceApisGetHeaders` was added

* `models.WorkspaceGlobalSchemasGetEntityTagHeaders` was added

* `models.WorkspaceProductsGetResponse` was added

* `models.TagProductLinkContract` was added

* `models.WorkspaceTagOperationLinksGetHeaders` was added

* `models.WorkspaceDiagnostics` was added

* `models.MigrateToStv2Contract` was added

* `models.WorkspacePolicyFragmentsGetResponse` was added

* `models.WorkspaceApiVersionSetsCreateOrUpdateResponse` was added

* `models.PolicyRestrictionContract$DefinitionStages` was added

* `models.PolicyRestrictionRequireBase` was added

* `models.ApiManagementGatewayConfigConnectionResource$UpdateStages` was added

* `models.WorkspacePoliciesGetEntityTagResponse` was added

* `models.WorkspacePolicyFragmentsGetEntityTagHeaders` was added

* `models.PolicyRestrictionsUpdateHeaders` was added

* `models.WorkspaceDiagnosticsUpdateHeaders` was added

* `models.WorkspaceTagApiLinksGetHeaders` was added

* `models.WorkspaceSubscriptionsCreateOrUpdateHeaders` was added

* `models.WorkspaceTagOperationLinksGetResponse` was added

* `models.WorkspaceApiOperationsUpdateResponse` was added

* `models.WorkspaceBackendsGetHeaders` was added

* `models.WorkspaceBackendsGetResponse` was added

* `models.TagApiLinks` was added

* `models.WorkspaceApiPoliciesGetHeaders` was added

* `models.TagApiLinkContract$UpdateStages` was added

* `models.WorkspaceNamedValuesGetEntityTagResponse` was added

* `models.AllPoliciesContract` was added

* `models.ApiManagementGatewayConfigConnectionResource$Definition` was added

* `models.ConfigurationApi` was added

* `models.WorkspaceSubscriptions` was added

* `models.WorkspaceApiPoliciesCreateOrUpdateResponse` was added

* `models.WorkspaceApiReleasesCreateOrUpdateResponse` was added

* `models.BackendType` was added

* `models.ProductGroupLinks` was added

* `models.WorkspaceTagsCreateOrUpdateHeaders` was added

* `models.WorkspaceBackendsUpdateHeaders` was added

* `models.WorkspaceTagProductLinksGetHeaders` was added

* `models.TagApiLinkContract` was added

* `models.WorkspaceProductGroupLinks` was added

* `models.MigrateToStv2Mode` was added

* `models.WorkspaceCollection` was added

* `models.ApiManagementGatewayConfigConnectionListResult` was added

* `models.TagApiLinkContract$Definition` was added

* `models.WorkspaceApisUpdateResponse` was added

* `models.WorkspaceApiVersionSetsCreateOrUpdateHeaders` was added

* `models.TagProductLinksGetHeaders` was added

* `models.FrontendConfiguration` was added

* `models.ApiManagementGatewayResource$Update` was added

* `models.WorkspacePolicyFragmentsGetHeaders` was added

* `models.WorkspaceProductApiLinksGetHeaders` was added

* `models.WorkspaceNamedValuesGetEntityTagHeaders` was added

* `models.LegacyApiState` was added

* `models.WorkspaceProductPolicies` was added

* `models.WorkspaceContract$UpdateStages` was added

* `models.ApiManagementGatewayResource` was added

* `models.TagProductLinks` was added

* `models.WorkspaceApisGetEntityTagHeaders` was added

* `models.WorkspacesCreateOrUpdateHeaders` was added

* `models.PolicyRestrictionsGetEntityTagHeaders` was added

* `models.ApiManagementWorkspaceLinksResource` was added

* `models.WorkspaceApisGetEntityTagResponse` was added

* `models.WorkspaceApiOperationsGetHeaders` was added

* `models.WorkspaceProductPoliciesCreateOrUpdateResponse` was added

* `models.WorkspaceGroupsCreateOrUpdateResponse` was added

* `models.WorkspaceApiVersionSetsGetResponse` was added

* `models.ApiManagementGatewaySkuPropertiesForPatch` was added

* `models.GatewayConfigurationApi` was added

* `models.WorkspaceApiOperationPoliciesCreateOrUpdateHeaders` was added

* `models.WorkspaceDiagnosticsGetEntityTagHeaders` was added

* `models.WorkspaceGroupsGetEntityTagResponse` was added

* `models.BackendCircuitBreaker` was added

* `models.PolicyRestrictionCollection` was added

* `models.WorkspaceNotificationRecipientEmails` was added

* `models.ProductGroupLinkContract$Update` was added

* `models.WorkspaceTagsGetResponse` was added

* `models.WorkspaceApiReleasesGetEntityTagHeaders` was added

* `models.ProductApiLinkContract$DefinitionStages` was added

* `models.WorkspaceApiDiagnostics` was added

* `models.WorkspaceTags` was added

* `models.ErrorFieldContract` was added

* `models.TagOperationLinkContract$Update` was added

* `models.WorkspaceSubscriptionsGetResponse` was added

* `models.WorkspaceContract` was added

* `models.ProductGroupLinkCollection` was added

* `models.WorkspaceNamedValuesListValueHeaders` was added

* `models.ApiGatewaySkuType` was added

* `models.WorkspaceLoggersUpdateHeaders` was added

* `models.WorkspaceDiagnosticsCreateOrUpdateResponse` was added

* `models.WorkspaceApiPoliciesCreateOrUpdateHeaders` was added

* `models.ApiManagementGatewayResource$UpdateStages` was added

* `models.WorkspaceProductPoliciesGetEntityTagResponse` was added

* `models.WorkspacesCreateOrUpdateResponse` was added

* `models.WorkspaceGlobalSchemas` was added

* `models.WorkspaceApiVersionSetsGetEntityTagHeaders` was added

* `models.WorkspaceApiReleases` was added

* `models.WorkspaceTagApiLinks` was added

* `models.ProductGroupLinkContract$Definition` was added

* `models.WorkspaceDiagnosticsGetHeaders` was added

* `models.GatewayListDebugCredentialsContractPurpose` was added

* `models.WorkspaceApiOperationPoliciesGetResponse` was added

* `models.WorkspaceProductPoliciesGetHeaders` was added

* `models.WorkspaceNamedValues` was added

* `models.ApiManagementWorkspaceLinkOperations` was added

* `models.PolicyRestrictionUpdateContract` was added

* `models.WorkspaceApiReleasesCreateOrUpdateHeaders` was added

* `models.WorkspacePolicies` was added

* `models.WorkspaceContract$Definition` was added

* `models.GatewayListTraceContract` was added

* `models.BackendBaseParametersPool` was added

* `models.WorkspaceCertificatesGetEntityTagResponse` was added

* `models.WorkspaceProductsCreateOrUpdateResponse` was added

* `models.GatewayDebugCredentialsContract` was added

* `models.WorkspaceCertificatesRefreshSecretHeaders` was added

* `models.TagOperationLinksGetHeaders` was added

* `models.TagOperationLinkContract$Definition` was added

* `models.TagProductLinkContract$Update` was added

* `models.WorkspaceNamedValuesGetHeaders` was added

* `models.WorkspaceApiExports` was added

* `models.WorkspaceApiVersionSetsGetHeaders` was added

* `models.WorkspaceGroupsGetEntityTagHeaders` was added

* `models.WorkspaceApiReleasesUpdateHeaders` was added

* `models.OperationsResults` was added

* `models.WorkspaceApiPoliciesGetEntityTagResponse` was added

* `models.WorkspaceApiReleasesGetHeaders` was added

* `models.TagOperationLinkContract$DefinitionStages` was added

* `models.DiagnosticUpdateContract` was added

* `models.BackendConfiguration` was added

* `models.TagOperationLinkCollection` was added

* `models.OperationsResultsGetHeaders` was added

* `models.WorkspaceProductPoliciesCreateOrUpdateHeaders` was added

* `models.PolicyRestrictionValidations` was added

* `models.PolicyRestrictionContract` was added

* `models.WorkspacesGetResponse` was added

* `models.ApiManagementGatewayResource$Definition` was added

* `models.WorkspaceApiReleasesGetResponse` was added

* `models.WorkspaceCertificatesRefreshSecretResponse` was added

* `models.GatewayResourceSkuResult` was added

* `models.WorkspaceSubscriptionsListSecretsResponse` was added

* `models.TagProductLinkContract$DefinitionStages` was added

* `models.WorkspaceLoggersCreateOrUpdateResponse` was added

* `models.WorkspaceProductsGetEntityTagHeaders` was added

* `models.WorkspaceApiSchemasGetEntityTagHeaders` was added

* `models.WorkspaceApiOperationPolicies` was added

* `models.TagOperationLinkContract` was added

* `models.AllPoliciesCollection` was added

* `models.OperationStatusResult` was added

* `models.WorkspaceProductsGetEntityTagResponse` was added

* `models.ApiManagementGatewayBaseProperties` was added

* `models.WorkspaceLinksBaseProperties` was added

* `models.TagApiLinkContract$DefinitionStages` was added

* `models.WorkspaceApiOperations` was added

* `models.WorkspaceApiDiagnosticsGetHeaders` was added

* `models.WorkspaceApiDiagnosticsUpdateResponse` was added

* `models.ProductGroupLinksGetHeaders` was added

* `models.WorkspaceGroupsCreateOrUpdateHeaders` was added

* `models.WorkspaceSubscriptionsGetEntityTagResponse` was added

* `models.WorkspaceLoggersCreateOrUpdateHeaders` was added

* `models.WorkspaceGroupsUpdateHeaders` was added

* `models.LegacyPortalStatus` was added

* `models.WorkspaceProductPoliciesGetResponse` was added

* `models.WorkspaceSubscriptionsCreateOrUpdateResponse` was added

* `models.WorkspaceLoggersGetEntityTagHeaders` was added

* `models.PolicyRestrictionsGetHeaders` was added

* `models.PolicyRestrictionsUpdateResponse` was added

* `models.WorkspaceCertificates` was added

* `models.WorkspaceProductsUpdateHeaders` was added

* `models.WorkspacesGetEntityTagHeaders` was added

* `models.WorkspaceApiOperationsGetEntityTagHeaders` was added

* `models.ProductApiLinkContract$Update` was added

* `models.WorkspaceLoggersGetHeaders` was added

* `models.GatewayListDebugCredentialsContract` was added

* `models.WorkspaceApiDiagnosticsGetResponse` was added

* `models.ApiManagementGatewayConfigConnectionResource$DefinitionStages` was added

* `models.WorkspaceNotificationRecipientUsers` was added

* `models.WorkspaceApiOperationPoliciesGetEntityTagHeaders` was added

* `models.WorkspaceApiSchemasGetHeaders` was added

* `models.WorkspaceSubscriptionsUpdateResponse` was added

* `models.WorkspaceTagsCreateOrUpdateResponse` was added

* `models.WorkspaceProductPoliciesGetEntityTagHeaders` was added

* `models.WorkspaceDiagnosticsCreateOrUpdateHeaders` was added

* `models.WorkspaceApiSchemasGetResponse` was added

* `models.WorkspaceApiReleasesGetEntityTagResponse` was added

* `models.ProductApiLinksGetResponse` was added

* `models.WorkspaceTagsUpdateHeaders` was added

* `models.PolicyRestrictionsGetResponse` was added

* `models.WorkspaceBackendsCreateOrUpdateHeaders` was added

* `models.ProductApiLinkContract` was added

* `models.WorkspacePoliciesGetEntityTagHeaders` was added

* `models.WorkspaceApisUpdateHeaders` was added

* `models.WorkspaceBackends` was added

* `models.WorkspaceApiPoliciesGetResponse` was added

* `models.WorkspaceLinksGateway` was added

* `models.WorkspaceApiReleasesUpdateResponse` was added

* `models.WorkspaceCertificatesCreateOrUpdateHeaders` was added

* `models.ApiManagementWorkspaceLinksListResult` was added

* `models.WorkspaceTagProductLinksGetResponse` was added

* `models.WorkspaceCertificatesGetEntityTagHeaders` was added

* `models.WorkspacesGetEntityTagResponse` was added

* `models.WorkspaceSubscriptionsListSecretsHeaders` was added

* `models.ApiManagementGatewaySkus` was added

* `models.WorkspacePolicyFragmentsGetEntityTagResponse` was added

* `models.TagProductLinksGetResponse` was added

* `models.WorkspaceCertificatesGetResponse` was added

* `models.WorkspaceTagsUpdateResponse` was added

* `models.WorkspaceContract$DefinitionStages` was added

* `models.ApiManagementGatewayUpdateParameters` was added

* `models.TagProductLinkCollection` was added

* `models.Workspaces` was added

* `models.WorkspaceContract$Update` was added

* `models.WorkspaceProducts` was added

* `models.WorkspaceCertificatesCreateOrUpdateResponse` was added

* `models.WorkspaceApiOperationPoliciesCreateOrUpdateResponse` was added

* `models.WorkspacesUpdateResponse` was added

* `models.WorkspaceApiSchemasGetEntityTagResponse` was added

* `models.TagApiLinksGetResponse` was added

* `models.PolicyRestrictionContract$UpdateStages` was added

* `models.ApiManagementGatewaySkuProperties` was added

* `models.WorkspaceApiOperationsCreateOrUpdateResponse` was added

* `models.PolicyComplianceState` was added

* `models.WorkspacePolicyFragments` was added

* `models.WorkspaceApiPoliciesGetEntityTagHeaders` was added

* `models.ProductGroupLinkContract$DefinitionStages` was added

* `models.BackendPool` was added

* `models.ProductApiLinkContract$UpdateStages` was added

* `models.ApiManagementGatewayResource$DefinitionStages` was added

* `models.CircuitBreakerRule` was added

* `models.GatewaySkuCapacityScaleType` was added

* `models.OperationStatus` was added

* `models.TagApiLinksGetHeaders` was added

* `models.WorkspaceGlobalSchemasGetHeaders` was added

* `models.WorkspaceApiSchemas` was added

* `models.WorkspacePoliciesCreateOrUpdateResponse` was added

* `models.WorkspaceApis` was added

* `models.WorkspaceTagOperationLinks` was added

* `models.WorkspaceTagsGetEntityStateHeaders` was added

* `models.WorkspacePoliciesGetHeaders` was added

* `models.WorkspaceSubscriptionsGetEntityTagHeaders` was added

* `models.WorkspaceDiagnosticsUpdateResponse` was added

* `models.WorkspaceBackendsUpdateResponse` was added

* `models.WorkspaceProductApiLinksGetResponse` was added

* `models.WorkspaceProductsUpdateResponse` was added

* `models.ProductApiLinkCollection` was added

* `models.ApiGatewayConfigConnections` was added

* `models.CircuitBreakerFailureCondition` was added

* `models.PolicyRestrictions` was added

* `models.KeyVaultRefreshState` was added

* `models.WorkspaceApiOperationsGetEntityTagResponse` was added

* `models.WorkspacePoliciesGetResponse` was added

* `models.WorkspaceApiPolicies` was added

* `models.WorkspaceGroups` was added

* `models.PolicyRestrictionsCreateOrUpdateResponse` was added

* `models.PolicyRestrictionContract$Definition` was added

* `models.WorkspaceBackendsGetEntityTagResponse` was added

* `models.WorkspaceTagsGetHeaders` was added

* `models.ProductGroupLinkContract` was added

* `models.TagApiLinkContract$Update` was added

* `models.WorkspaceApiVersionSetsGetEntityTagResponse` was added

* `models.PolicyRestrictionContract$Update` was added

* `models.WorkspaceProductsCreateOrUpdateHeaders` was added

* `models.WorkspaceGroupsGetHeaders` was added

* `models.WorkspacesUpdateHeaders` was added

* `models.ErrorResponseBody` was added

* `models.WorkspaceApiOperationPoliciesGetEntityTagResponse` was added

* `models.GatewaySkuCapacity` was added

* `models.WorkspaceApiOperationsUpdateHeaders` was added

* `models.OperationsResultsGetResponse` was added

* `models.WorkspaceApiDiagnosticsGetEntityTagResponse` was added

* `models.TagProductLinkContract$Definition` was added

* `models.TagProductLinkContract$UpdateStages` was added

* `models.WorkspaceDiagnosticsGetResponse` was added

* `models.WorkspaceApiDiagnosticsUpdateHeaders` was added

* `models.WorkspaceGroupUsers` was added

* `models.DeveloperPortalStatus` was added

* `models.WorkspaceApisGetResponse` was added

* `models.GatewayResourceSkuResults` was added

* `models.WorkspaceApiDiagnosticsCreateOrUpdateHeaders` was added

* `models.ApiGateways` was added

* `models.WorkspaceLoggersUpdateResponse` was added

* `models.WorkspaceGroupsGetResponse` was added

* `models.ProductGroupLinkContract$UpdateStages` was added

* `models.WorkspaceBackendsCreateOrUpdateResponse` was added

* `models.WorkspaceDiagnosticsGetEntityTagResponse` was added

* `models.TagOperationLinkContract$UpdateStages` was added

* `models.WorkspaceGroupsUpdateResponse` was added

* `models.PolicyRestrictionsGetEntityTagResponse` was added

* `models.WorkspaceApiOperationPoliciesGetHeaders` was added

* `models.WorkspaceProductGroupLinksGetResponse` was added

* `models.FailureStatusCodeRange` was added

* `models.ProductApiLinksGetHeaders` was added

* `models.WorkspaceApiVersionSetsUpdateResponse` was added

* `models.TagOperationLinks` was added

* `models.WorkspaceApiVersionSetsUpdateHeaders` was added

#### `models.AuthorizationAccessPolicyContract` was modified

* `appIds()` was added

#### `models.ApiManagementServiceUpdateParameters` was modified

* `withDeveloperPortalStatus(models.DeveloperPortalStatus)` was added
* `configurationApi()` was added
* `withConfigurationApi(models.ConfigurationApi)` was added
* `developerPortalStatus()` was added
* `legacyPortalStatus()` was added
* `withLegacyPortalStatus(models.LegacyPortalStatus)` was added

#### `models.BackendContract` was modified

* `pool()` was added
* `typePropertiesType()` was added
* `circuitBreaker()` was added

#### `models.PortalRevisionContract` was modified

* `provisioningState()` was added

#### `models.ApiManagementServiceBaseProperties` was modified

* `developerPortalStatus()` was added
* `withConfigurationApi(models.ConfigurationApi)` was added
* `withLegacyPortalStatus(models.LegacyPortalStatus)` was added
* `withDeveloperPortalStatus(models.DeveloperPortalStatus)` was added
* `legacyPortalStatus()` was added
* `configurationApi()` was added

#### `models.AuthorizationAccessPolicyContract$Definition` was modified

* `withAppIds(java.util.List)` was added

#### `models.BackendContract$Definition` was modified

* `withPool(models.BackendBaseParametersPool)` was added
* `withCircuitBreaker(models.BackendCircuitBreaker)` was added
* `withTypePropertiesType(models.BackendType)` was added

#### `models.PortalConfigCollection` was modified

* `validate()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withValue(java.util.List)` was added

#### `models.ApiManagementServices` was modified

* `migrateToStv2(java.lang.String,java.lang.String,models.MigrateToStv2Contract,com.azure.core.util.Context)` was added

#### `models.Apis` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was added

#### `models.ApiManagementServiceResource$Update` was modified

* `withConfigurationApi(models.ConfigurationApi)` was added
* `withDeveloperPortalStatus(models.DeveloperPortalStatus)` was added
* `withLegacyPortalStatus(models.LegacyPortalStatus)` was added

#### `models.ApiManagementServiceResource` was modified

* `legacyPortalStatus()` was added
* `configurationApi()` was added
* `developerPortalStatus()` was added
* `migrateToStv2(models.MigrateToStv2Contract,com.azure.core.util.Context)` was added

#### `models.SchemaContract` was modified

* `provisioningState()` was added

#### `models.Users` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Boolean,java.lang.Boolean,models.AppType,com.azure.core.util.Context)` was added

#### `models.PortalConfigs` was modified

* `listByService(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.NamedValueContract` was modified

* `provisioningState()` was added

#### `ApiManagementManager` was modified

* `workspaceApiVersionSets()` was added
* `workspaceSubscriptions()` was added
* `tagProductLinks()` was added
* `workspaceProductPolicies()` was added
* `workspaceDiagnostics()` was added
* `workspaceProducts()` was added
* `workspaceNotifications()` was added
* `operationStatus()` was added
* `workspaceTagOperationLinks()` was added
* `tagApiLinks()` was added
* `workspaceGroupUsers()` was added
* `workspaceGroups()` was added
* `workspaceCertificates()` was added
* `workspaceGlobalSchemas()` was added
* `workspaceNamedValues()` was added
* `workspaceNotificationRecipientEmails()` was added
* `workspaceApiOperations()` was added
* `apiGatewayConfigConnections()` was added
* `apiGateways()` was added
* `tagOperationLinks()` was added
* `apiManagementWorkspaceLinks()` was added
* `apiManagementGatewaySkus()` was added
* `workspaceApis()` was added
* `productApiLinks()` was added
* `workspaceTagProductLinks()` was added
* `workspaceApiExports()` was added
* `workspaceApiRevisions()` was added
* `workspaceNotificationRecipientUsers()` was added
* `workspaceApiDiagnostics()` was added
* `apiManagementWorkspaceLinkOperations()` was added
* `policyRestrictionValidations()` was added
* `policyRestrictions()` was added
* `workspaceTags()` was added
* `operationsResults()` was added
* `workspaceLoggers()` was added
* `workspaceTagApiLinks()` was added
* `workspaceApiOperationPolicies()` was added
* `workspaceBackends()` was added
* `workspacePolicies()` was added
* `productGroupLinks()` was added
* `workspaceApiSchemas()` was added
* `workspacePolicyFragments()` was added
* `allPolicies()` was added
* `workspaces()` was added
* `workspaceApiPolicies()` was added
* `workspaceApiReleases()` was added
* `workspaceProductApiLinks()` was added
* `workspaceProductGroupLinks()` was added

#### `models.ApiContract` was modified

* `provisioningState()` was added

#### `models.BackendBaseParameters` was modified

* `circuitBreaker()` was added
* `withType(models.BackendType)` was added
* `withPool(models.BackendBaseParametersPool)` was added
* `withCircuitBreaker(models.BackendCircuitBreaker)` was added
* `pool()` was added
* `type()` was added

#### `models.Gateways` was modified

* `listTraceWithResponse(java.lang.String,java.lang.String,java.lang.String,models.GatewayListTraceContract,com.azure.core.util.Context)` was added
* `listTrace(java.lang.String,java.lang.String,java.lang.String,models.GatewayListTraceContract)` was added
* `listDebugCredentials(java.lang.String,java.lang.String,java.lang.String,models.GatewayListDebugCredentialsContract)` was added
* `listDebugCredentialsWithResponse(java.lang.String,java.lang.String,java.lang.String,models.GatewayListDebugCredentialsContract,com.azure.core.util.Context)` was added
* `invalidateDebugCredentials(java.lang.String,java.lang.String,java.lang.String)` was added
* `invalidateDebugCredentialsWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.ApiManagementServiceResource$Definition` was modified

* `withLegacyPortalStatus(models.LegacyPortalStatus)` was added
* `withDeveloperPortalStatus(models.DeveloperPortalStatus)` was added
* `withConfigurationApi(models.ConfigurationApi)` was added

#### `models.ApiCreateOrUpdateParameter` was modified

* `provisioningState()` was added

#### `models.PolicyFragmentContract` was modified

* `provisioningState()` was added

#### `models.PolicyFragmentCollection` was modified

* `withValue(java.util.List)` was added
* `withNextLink(java.lang.String)` was added
* `withCount(java.lang.Long)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `validate()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BackendContract$Update` was modified

* `withCircuitBreaker(models.BackendCircuitBreaker)` was added
* `withType(models.BackendType)` was added
* `withPool(models.BackendBaseParametersPool)` was added

#### `models.Policies` was modified

* `listByService(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.GlobalSchemaContract` was modified

* `provisioningState()` was added

#### `models.ProductPolicies` was modified

* `listByProduct(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.GatewayContract` was modified

* `listTrace(models.GatewayListTraceContract)` was added
* `listDebugCredentialsWithResponse(models.GatewayListDebugCredentialsContract,com.azure.core.util.Context)` was added
* `listTraceWithResponse(models.GatewayListTraceContract,com.azure.core.util.Context)` was added
* `invalidateDebugCredentialsWithResponse(com.azure.core.util.Context)` was added
* `listDebugCredentials(models.GatewayListDebugCredentialsContract)` was added
* `invalidateDebugCredentials()` was added

#### `models.AuthorizationAccessPolicyContract$Update` was modified

* `withAppIds(java.util.List)` was added

#### `models.BackendUpdateParameters` was modified

* `withPool(models.BackendBaseParametersPool)` was added
* `withCircuitBreaker(models.BackendCircuitBreaker)` was added
* `pool()` was added
* `type()` was added
* `circuitBreaker()` was added
* `withType(models.BackendType)` was added

#### `models.PolicyFragments` was modified

* `listByService(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,java.lang.Integer,com.azure.core.util.Context)` was added

## 1.0.0 (2024-12-23)

- Azure Resource Manager ApiManagement client library for Java. This package contains Microsoft Azure SDK for ApiManagement Management SDK. ApiManagement Client. Package tag package-2022-08. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Other Changes

- Release for Azure Resource Manager ApiManagement client library for Java.

## 1.0.0-beta.5 (2024-12-03)

- Azure Resource Manager ApiManagement client library for Java. This package contains Microsoft Azure SDK for ApiManagement Management SDK. ApiManagement Client. Package tag package-2022-08. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

### Features Added

#### `models.PortalSettingsContract` was modified

* `name()` was added
* `id()` was added
* `type()` was added

#### `models.AssociationContract` was modified

* `id()` was added
* `name()` was added
* `type()` was added

#### `models.ResourceCollectionValueItem` was modified

* `name()` was added
* `id()` was added
* `type()` was added

#### `models.ApiManagementServiceUpdateParameters` was modified

* `name()` was added
* `id()` was added
* `type()` was added

#### `models.AuthorizationServerUpdateContract` was modified

* `name()` was added
* `id()` was added
* `type()` was added

#### `models.ApimResource` was modified

* `id()` was added
* `name()` was added
* `type()` was added

#### `models.BackendReconnectContract` was modified

* `id()` was added
* `type()` was added
* `name()` was added

#### `models.ApiTagResourceContractProperties` was modified

* `isOnline()` was added

#### `models.NamedValueCreateContract` was modified

* `name()` was added
* `id()` was added
* `type()` was added

#### `models.PolicyDescriptionContract` was modified

* `name()` was added
* `id()` was added
* `type()` was added

#### `models.IdentityProviderCreateContract` was modified

* `name()` was added
* `id()` was added
* `type()` was added

## 1.0.0-beta.4 (2023-08-25)

- Azure Resource Manager ApiManagement client library for Java. This package contains Microsoft Azure SDK for ApiManagement Management SDK. ApiManagement Client. Package tag package-2022-08. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.PortalRevisionsUpdateResponse` was removed

* `models.PortalRevisionsCreateOrUpdateHeaders` was removed

* `models.ApiSchemasCreateOrUpdateResponse` was removed

* `models.ApisCreateOrUpdateHeaders` was removed

* `models.ApiVersionSetContractDetailsVersioningScheme` was removed

* `models.NamedValuesRefreshSecretResponse` was removed

* `models.GlobalSchemasCreateOrUpdateHeaders` was removed

* `models.NamedValuesCreateOrUpdateHeaders` was removed

* `models.GlobalSchemasCreateOrUpdateResponse` was removed

* `models.NamedValuesUpdateHeaders` was removed

* `models.PortalRevisionsCreateOrUpdateResponse` was removed

* `models.NamedValuesUpdateResponse` was removed

* `models.NamedValuesRefreshSecretHeaders` was removed

* `models.NamedValuesCreateOrUpdateResponse` was removed

* `models.ApisCreateOrUpdateResponse` was removed

* `models.PortalRevisionsUpdateHeaders` was removed

* `models.ApiSchemasCreateOrUpdateHeaders` was removed

#### `models.ContentTypes` was modified

* `createOrUpdateWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `createOrUpdate(java.lang.String,java.lang.String,java.lang.String)` was removed

#### `models.ContentItems` was modified

* `createOrUpdateWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `createOrUpdate(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was removed

#### `models.ApiManagementServices` was modified

* `applyNetworkConfigurationUpdates(java.lang.String,java.lang.String,models.ApiManagementServiceApplyNetworkConfigurationParameters)` was removed

#### `models.ApiManagementServiceResource` was modified

* `applyNetworkConfigurationUpdates(models.ApiManagementServiceApplyNetworkConfigurationParameters)` was removed

#### `models.ApiVersionSetContractDetails` was modified

* `withVersioningScheme(models.ApiVersionSetContractDetailsVersioningScheme)` was removed
* `models.ApiVersionSetContractDetailsVersioningScheme versioningScheme()` -> `models.VersioningScheme versioningScheme()`

### Features Added

* `models.PortalConfigsGetEntityTagResponse` was added

* `models.AuthorizationContract$Definition` was added

* `models.ProductWikisGetHeaders` was added

* `models.AuthorizationError` was added

* `models.PortalConfigPropertiesSignin` was added

* `models.AuthorizationAccessPolicyContract$UpdateStages` was added

* `models.ContentTypeContract$Definition` was added

* `models.GraphQLApiResolversUpdateHeaders` was added

* `models.AuthorizationType` was added

* `models.TranslateRequiredQueryParametersConduct` was added

* `models.AuthorizationContract$UpdateStages` was added

* `models.ApiWikisUpdateResponse` was added

* `models.ResourceCollectionValueItem` was added

* `models.OAuth2GrantType` was added

* `models.AuthorizationAccessPolicyContract` was added

* `models.AuthorizationProvidersGetHeaders` was added

* `models.ResourceCollection` was added

* `models.DocumentationContract$Definition` was added

* `models.PolicyFragmentsGetResponse` was added

* `models.AuthorizationLoginLinksPostHeaders` was added

* `models.GraphQLApiResolverPolicies` was added

* `models.GraphQLApiResolversGetResponse` was added

* `models.AuthorizationProviderOAuth2GrantTypes` was added

* `models.Authorizations` was added

* `models.DocumentationsUpdateResponse` was added

* `models.ContentItemContract$DefinitionStages` was added

* `models.PolicyFragmentContract$Update` was added

* `models.PortalConfigContract$UpdateStages` was added

* `models.AuthorizationProvidersCreateOrUpdateHeaders` was added

* `models.ApiWikisGetEntityTagHeaders` was added

* `models.ContentItemContract$UpdateStages` was added

* `models.PortalConfigsGetResponse` was added

* `models.ContentTypeContract$UpdateStages` was added

* `models.PortalConfigContract$Definition` was added

* `models.AuthorizationAccessPolicyContract$Definition` was added

* `models.PortalConfigCspProperties` was added

* `models.ResolverContract$DefinitionStages` was added

* `models.PortalSettingsCspMode` was added

* `models.WikiContract` was added

* `models.ProductWikisOperationsListHeaders` was added

* `models.DocumentationsGetResponse` was added

* `models.PortalConfigCollection` was added

* `models.AuthorizationsGetHeaders` was added

* `models.DocumentationUpdateContract` was added

* `models.AuthorizationProviderContract$UpdateStages` was added

* `models.AuthorizationLoginRequestContract` was added

* `models.DocumentationContract$DefinitionStages` was added

* `models.AuthorizationLoginLinksPostResponse` was added

* `models.AuthorizationsGetResponse` was added

* `models.ProductWikis` was added

* `models.ApiWikisGetHeaders` was added

* `models.ApiWikisCreateOrUpdateResponse` was added

* `models.AuthorizationProviderContract$Definition` was added

* `models.PortalConfigContract$Update` was added

* `models.AuthorizationProviderOAuth2Settings` was added

* `models.AuthorizationContract` was added

* `models.ResolverUpdateContract` was added

* `models.PolicyFragmentsGetEntityTagHeaders` was added

* `models.ResolverContract$Definition` was added

* `models.DocumentationsUpdateHeaders` was added

* `models.ProductWikisOperationsListNextHeaders` was added

* `models.PortalConfigPropertiesSignup` was added

* `models.AuthorizationProvidersGetResponse` was added

* `models.Documentations` was added

* `models.PolicyFragmentContract$DefinitionStages` was added

* `models.AuthorizationProviders` was added

* `models.ResolverContract$Update` was added

* `models.AuthorizationConfirmConsentCodeRequestContract` was added

* `models.AuthorizationCollection` was added

* `models.AuthorizationsCreateOrUpdateHeaders` was added

* `models.ContentTypeContract$Update` was added

* `models.ApiWikisGetEntityTagResponse` was added

* `models.DocumentationsCreateOrUpdateHeaders` was added

* `models.PortalConfigs` was added

* `models.ResolverCollection` was added

* `models.WikiDocumentationContract` was added

* `models.AuthorizationsCreateOrUpdateResponse` was added

* `models.GraphQLApiResolversGetEntityTagResponse` was added

* `models.PolicyFragmentContract$UpdateStages` was added

* `models.GraphQLApiResolversGetHeaders` was added

* `models.AuthorizationAccessPoliciesGetResponse` was added

* `models.ProductWikisCreateOrUpdateHeaders` was added

* `models.DocumentationsGetHeaders` was added

* `models.GraphQLApiResolverPoliciesGetEntityTagResponse` was added

* `models.GraphQLApiResolverPoliciesCreateOrUpdateResponse` was added

* `models.ContentItemContract$Update` was added

* `models.GraphQLApiResolversCreateOrUpdateResponse` was added

* `models.ApiWikis` was added

* `models.ContentItemContract$Definition` was added

* `models.PortalConfigCorsProperties` was added

* `models.AuthorizationAccessPoliciesGetHeaders` was added

* `models.WikiCollection` was added

* `models.ApiWikisUpdateHeaders` was added

* `models.ProductWikisUpdateResponse` was added

* `models.GraphQLApiResolverPoliciesGetHeaders` was added

* `models.ApiWikisOperations` was added

* `models.PortalConfigContract` was added

* `models.AuthorizationAccessPolicyContract$DefinitionStages` was added

* `models.ProductWikisOperations` was added

* `models.AuthorizationProvidersCreateOrUpdateResponse` was added

* `models.ApiWikisCreateOrUpdateHeaders` was added

* `models.GraphQLApiResolverPoliciesCreateOrUpdateHeaders` was added

* `models.WikiUpdateContract` was added

* `models.NatGatewayState` was added

* `models.AuthorizationAccessPoliciesCreateOrUpdateResponse` was added

* `models.ProductWikisOperationsListResponse` was added

* `models.ProductWikisCreateOrUpdateResponse` was added

* `models.ResolverContract$UpdateStages` was added

* `models.GraphQLApiResolversCreateOrUpdateHeaders` was added

* `models.GraphQLApiResolversUpdateResponse` was added

* `models.PolicyFragmentContentFormat` was added

* `models.AuthorizationAccessPolicies` was added

* `models.PolicyFragmentContract` was added

* `models.PortalConfigsGetEntityTagHeaders` was added

* `models.AuthorizationsConfirmConsentCodeResponse` was added

* `models.AuthorizationLoginLinks` was added

* `models.GraphQLApiResolverPoliciesGetResponse` was added

* `models.PortalConfigsGetHeaders` was added

* `models.ResolverContract` was added

* `models.ApiWikisGetResponse` was added

* `models.ProductWikisUpdateHeaders` was added

* `models.PolicyFragmentCollection` was added

* `models.ContentTypeContract$DefinitionStages` was added

* `models.DocumentationContract$Update` was added

* `models.PortalConfigTermsOfServiceProperties` was added

* `models.DocumentationContract` was added

* `models.ProductWikisGetEntityTagHeaders` was added

* `models.DocumentationCollection` was added

* `models.GraphQLApiResolvers` was added

* `models.AuthorizationProviderCollection` was added

* `models.ProductWikisGetResponse` was added

* `models.ProductWikisGetEntityTagResponse` was added

* `models.DocumentationsCreateOrUpdateResponse` was added

* `models.AuthorizationContract$DefinitionStages` was added

* `models.PolicyFragmentContract$Definition` was added

* `models.AuthorizationProviderContract$Update` was added

* `models.AuthorizationProviderContract` was added

* `models.PortalConfigContract$DefinitionStages` was added

* `models.AuthorizationsConfirmConsentCodeHeaders` was added

* `models.AuthorizationAccessPoliciesCreateOrUpdateHeaders` was added

* `models.AuthorizationContract$Update` was added

* `models.GraphQLApiResolversGetEntityTagHeaders` was added

* `models.AuthorizationProviderContract$DefinitionStages` was added

* `models.DocumentationsGetEntityTagResponse` was added

* `models.AuthorizationAccessPolicyContract$Update` was added

* `models.PortalConfigDelegationProperties` was added

* `models.AuthorizationAccessPolicyCollection` was added

* `models.GraphQLApiResolverPoliciesGetEntityTagHeaders` was added

* `models.DocumentationContract$UpdateStages` was added

* `models.PolicyFragmentsGetHeaders` was added

* `models.DocumentationsGetEntityTagHeaders` was added

* `models.PolicyFragments` was added

* `models.PolicyFragmentsGetEntityTagResponse` was added

* `models.AuthorizationLoginResponseContract` was added

* `models.ProductWikisOperationsListNextResponse` was added

#### `models.ContentTypeContract` was modified

* `resourceGroupName()` was added
* `refresh()` was added
* `update()` was added
* `refresh(com.azure.core.util.Context)` was added

#### `models.ContentTypes` was modified

* `deleteById(java.lang.String)` was added
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `deleteByIdWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `define(java.lang.String)` was added
* `getById(java.lang.String)` was added

#### `models.PolicyContract` was modified

* `resourceGroupName()` was added

#### `models.IdentityProviderContract` was modified

* `resourceGroupName()` was added
* `clientLibrary()` was added

#### `ApiManagementManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.IssueCommentContract` was modified

* `resourceGroupName()` was added

#### `models.LoggerContract` was modified

* `resourceGroupName()` was added

#### `models.UserContract` was modified

* `resourceGroupName()` was added

#### `models.ApiManagementServiceUpdateParameters` was modified

* `natGatewayState()` was added
* `outboundPublicIpAddresses()` was added
* `withNatGatewayState(models.NatGatewayState)` was added

#### `models.BackendContract` was modified

* `resourceGroupName()` was added

#### `models.OpenidConnectProviderContract` was modified

* `useInApiDocumentation()` was added
* `resourceGroupName()` was added
* `useInTestConsole()` was added

#### `models.ContentItems` was modified

* `deleteById(java.lang.String)` was added
* `getById(java.lang.String)` was added
* `define(java.lang.String)` was added
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `deleteByIdWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.PortalRevisionContract` was modified

* `resourceGroupName()` was added

#### `models.EmailTemplateContract` was modified

* `resourceGroupName()` was added

#### `models.DiagnosticContract$Definition` was modified

* `withMetrics(java.lang.Boolean)` was added

#### `models.ApiManagementServiceBaseProperties` was modified

* `outboundPublicIpAddresses()` was added
* `natGatewayState()` was added
* `withNatGatewayState(models.NatGatewayState)` was added

#### `models.GatewayCertificateAuthorityContract` was modified

* `resourceGroupName()` was added

#### `models.OperationContract` was modified

* `resourceGroupName()` was added

#### `models.AuthenticationSettingsContract` was modified

* `openidAuthenticationSettings()` was added
* `withOpenidAuthenticationSettings(java.util.List)` was added
* `oAuth2AuthenticationSettings()` was added
* `withOAuth2AuthenticationSettings(java.util.List)` was added

#### `models.DiagnosticContract` was modified

* `metrics()` was added
* `resourceGroupName()` was added

#### `models.GroupContract` was modified

* `resourceGroupName()` was added

#### `models.ContentItemContract` was modified

* `refresh(com.azure.core.util.Context)` was added
* `resourceGroupName()` was added
* `refresh()` was added
* `update()` was added

#### `models.DiagnosticContract$Update` was modified

* `withMetrics(java.lang.Boolean)` was added

#### `models.IssueAttachmentContract` was modified

* `resourceGroupName()` was added

#### `models.GatewayHostnameConfigurationContract` was modified

* `resourceGroupName()` was added

#### `models.IdentityProviderContract$Update` was modified

* `withClientLibrary(java.lang.String)` was added

#### `models.IssueContract` was modified

* `resourceGroupName()` was added

#### `models.ApiManagementServices` was modified

* `migrateToStv2(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `migrateToStv2(java.lang.String,java.lang.String)` was added

#### `models.CertificateContract` was modified

* `resourceGroupName()` was added

#### `models.AdditionalLocation` was modified

* `outboundPublicIpAddresses()` was added
* `withNatGatewayState(models.NatGatewayState)` was added
* `natGatewayState()` was added

#### `models.ApiManagementServiceResource$Update` was modified

* `withNatGatewayState(models.NatGatewayState)` was added

#### `models.ApiManagementServiceResource` was modified

* `natGatewayState()` was added
* `migrateToStv2(com.azure.core.util.Context)` was added
* `migrateToStv2()` was added
* `outboundPublicIpAddresses()` was added
* `resourceGroupName()` was added

#### `models.SchemaContract` was modified

* `resourceGroupName()` was added

#### `models.PrivateEndpointConnection` was modified

* `resourceGroupName()` was added

#### `models.TagDescriptionContract` was modified

* `resourceGroupName()` was added

#### `models.CacheContract` was modified

* `resourceGroupName()` was added

#### `models.NamedValueContract` was modified

* `resourceGroupName()` was added

#### `ApiManagementManager` was modified

* `productWikis()` was added
* `policyFragments()` was added
* `authorizationLoginLinks()` was added
* `authorizationProviders()` was added
* `productWikisOperations()` was added
* `graphQLApiResolverPolicies()` was added
* `apiWikisOperations()` was added
* `graphQLApiResolvers()` was added
* `apiWikis()` was added
* `portalConfigs()` was added
* `documentations()` was added
* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added
* `authorizations()` was added
* `authorizationAccessPolicies()` was added

#### `models.AuthorizationServerUpdateContract` was modified

* `withUseInApiDocumentation(java.lang.Boolean)` was added
* `useInApiDocumentation()` was added
* `withUseInTestConsole(java.lang.Boolean)` was added
* `useInTestConsole()` was added

#### `models.AccessInformationContract` was modified

* `resourceGroupName()` was added

#### `models.ApiContract` was modified

* `resourceGroupName()` was added

#### `models.AuthorizationServerContract$Update` was modified

* `withUseInApiDocumentation(java.lang.Boolean)` was added
* `withUseInTestConsole(java.lang.Boolean)` was added

#### `models.TagContract` was modified

* `resourceGroupName()` was added

#### `models.IdentityProviderContract$Definition` was modified

* `withClientLibrary(java.lang.String)` was added

#### `models.ApiManagementServiceResource$Definition` was modified

* `withNatGatewayState(models.NatGatewayState)` was added

#### `models.ApiVersionSetContractDetails` was modified

* `withVersioningScheme(models.VersioningScheme)` was added

#### `models.ApiCreateOrUpdateParameter` was modified

* `withTranslateRequiredQueryParametersConduct(models.TranslateRequiredQueryParametersConduct)` was added
* `translateRequiredQueryParametersConduct()` was added

#### `models.OpenidConnectProviderContract$Definition` was modified

* `withUseInTestConsole(java.lang.Boolean)` was added
* `withUseInApiDocumentation(java.lang.Boolean)` was added

#### `models.ProductContract` was modified

* `resourceGroupName()` was added

#### `models.IdentityProviderUpdateParameters` was modified

* `clientLibrary()` was added
* `withClientLibrary(java.lang.String)` was added

#### `models.ApiContract$Definition` was modified

* `withTranslateRequiredQueryParametersConduct(models.TranslateRequiredQueryParametersConduct)` was added

#### `models.ApiReleaseContract` was modified

* `resourceGroupName()` was added

#### `models.AuthorizationServerContract$Definition` was modified

* `withUseInApiDocumentation(java.lang.Boolean)` was added
* `withUseInTestConsole(java.lang.Boolean)` was added

#### `models.AuthorizationServerContract` was modified

* `resourceGroupName()` was added
* `useInTestConsole()` was added
* `useInApiDocumentation()` was added

#### `models.GlobalSchemaContract` was modified

* `resourceGroupName()` was added

#### `models.GatewayContract` was modified

* `resourceGroupName()` was added

#### `models.ApiVersionSetContract` was modified

* `resourceGroupName()` was added

#### `models.OpenidConnectProviderUpdateContract` was modified

* `useInTestConsole()` was added
* `useInApiDocumentation()` was added
* `withUseInTestConsole(java.lang.Boolean)` was added
* `withUseInApiDocumentation(java.lang.Boolean)` was added

#### `models.IdentityProviderCreateContract` was modified

* `clientLibrary()` was added
* `withClientLibrary(java.lang.String)` was added

#### `models.IdentityProviderBaseParameters` was modified

* `withClientLibrary(java.lang.String)` was added
* `clientLibrary()` was added

#### `models.OpenidConnectProviderContract$Update` was modified

* `withUseInApiDocumentation(java.lang.Boolean)` was added
* `withUseInTestConsole(java.lang.Boolean)` was added

## 1.0.0-beta.3 (2022-03-29)

- Azure Resource Manager ApiManagement client library for Java. This package contains Microsoft Azure SDK for ApiManagement Management SDK. ApiManagement Client. Package tag package-2021-08. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.RepresentationContract` was modified

* `sample()` was removed
* `withSample(java.lang.String)` was removed

#### `models.Protocol` was modified

* `toString()` was removed
* `valueOf(java.lang.String)` was removed
* `models.Protocol[] values()` -> `java.util.Collection values()`

### Features Added

* `models.ConnectivityCheckRequestProtocolConfigurationHttpConfiguration` was added

* `models.ArmIdWrapper` was added

* `models.PrivateEndpointConnections` was added

* `models.PrivateEndpointConnection` was added

* `models.PrivateEndpointConnection$DefinitionStages` was added

* `models.GlobalSchemaContract$DefinitionStages` was added

* `models.PrivateEndpointConnectionRequestProperties` was added

* `models.Origin` was added

* `models.GlobalSchemaContract$Definition` was added

* `models.RemotePrivateEndpointConnectionWrapper` was added

* `models.Severity` was added

* `models.ConnectivityCheckRequestSource` was added

* `models.ConnectivityCheckProtocol` was added

* `models.OutboundNetworkDependenciesEndpoints` was added

* `models.PrivateEndpointConnectionListResult` was added

* `models.PrivateEndpointServiceConnectionStatus` was added

* `models.EndpointDetail` was added

* `models.GlobalSchemaContract$Update` was added

* `models.PrivateLinkResource` was added

* `models.PrivateEndpoint` was added

* `models.ApiContactInformation` was added

* `models.ApiLicenseInformation` was added

* `models.ResourceProviders` was added

* `models.ConnectivityCheckRequestDestination` was added

* `models.ConnectivityIssue` was added

* `models.Method` was added

* `models.CertificateSource` was added

* `models.CertificateStatus` was added

* `models.GlobalSchemasCreateOrUpdateHeaders` was added

* `models.AccessType` was added

* `models.GlobalSchemasGetEntityTagHeaders` was added

* `models.PrivateLinkServiceConnectionState` was added

* `models.EndpointDependency` was added

* `models.PublicNetworkAccess` was added

* `models.GlobalSchemasGetResponse` was added

* `models.PrivateEndpointConnection$Definition` was added

* `models.GlobalSchemaCollection` was added

* `models.HttpHeader` was added

* `models.PrivateEndpointConnection$UpdateStages` was added

* `models.PrivateEndpointConnection$Update` was added

* `models.ConnectivityHop` was added

* `models.ConnectionStatus` was added

* `models.ConnectivityCheckResponse` was added

* `models.GlobalSchemaContract` was added

* `models.GlobalSchemasGetEntityTagResponse` was added

* `models.GlobalSchemas` was added

* `models.SchemaType` was added

* `models.GlobalSchemasCreateOrUpdateResponse` was added

* `models.PrivateEndpointConnectionRequest` was added

* `models.OutboundEnvironmentEndpoint` was added

* `models.PrivateLinkResourceListResult` was added

* `models.GlobalSchemasGetHeaders` was added

* `models.PlatformVersion` was added

* `models.ParameterExampleContract` was added

* `models.PrivateEndpointConnectionProvisioningState` was added

* `models.GlobalSchemaContract$UpdateStages` was added

* `models.OutboundEnvironmentEndpointList` was added

* `models.IssueType` was added

* `models.ConnectivityCheckRequest` was added

* `models.PreferredIpVersion` was added

* `models.ConnectivityCheckRequestProtocolConfiguration` was added

#### `models.AdditionalLocation` was modified

* `platformVersion()` was added
* `publicIpAddressId()` was added
* `withPublicIpAddressId(java.lang.String)` was added

#### `models.ApiManagementServiceResource$Update` was modified

* `withPrivateEndpointConnections(java.util.List)` was added
* `withZones(java.util.List)` was added
* `withPublicIpAddressId(java.lang.String)` was added
* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added

#### `models.ApiManagementServiceResource` was modified

* `publicIpAddressId()` was added
* `platformVersion()` was added
* `privateEndpointConnections()` was added
* `systemData()` was added
* `publicNetworkAccess()` was added

#### `models.SchemaContract` was modified

* `components()` was added

#### `models.TenantConfigurationSyncStateContract` was modified

* `id()` was added
* `name()` was added
* `type()` was added

#### `models.ApiManagementServiceUpdateParameters` was modified

* `publicIpAddressId()` was added
* `withPrivateEndpointConnections(java.util.List)` was added
* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added
* `withPublicIpAddressId(java.lang.String)` was added
* `withZones(java.util.List)` was added
* `zones()` was added
* `privateEndpointConnections()` was added
* `publicNetworkAccess()` was added
* `platformVersion()` was added

#### `models.ApiEntityBaseContract` was modified

* `contact()` was added
* `license()` was added
* `withLicense(models.ApiLicenseInformation)` was added
* `withTermsOfServiceUrl(java.lang.String)` was added
* `termsOfServiceUrl()` was added
* `withContact(models.ApiContactInformation)` was added

#### `ApiManagementManager` was modified

* `globalSchemas()` was added
* `privateEndpointConnections()` was added
* `resourceProviders()` was added
* `outboundNetworkDependenciesEndpoints()` was added

#### `models.RepresentationContract` was modified

* `withExamples(java.util.Map)` was added
* `examples()` was added

#### `models.ApiContract` was modified

* `termsOfServiceUrl()` was added
* `license()` was added
* `contact()` was added

#### `models.ApiManagementServiceBackupRestoreParameters` was modified

* `withClientId(java.lang.String)` was added
* `withAccessType(models.AccessType)` was added
* `clientId()` was added
* `accessType()` was added

#### `models.ApiContract$Update` was modified

* `withLicense(models.ApiLicenseInformation)` was added
* `withContact(models.ApiContactInformation)` was added
* `withTermsOfServiceUrl(java.lang.String)` was added

#### `models.ApiManagementServiceBaseProperties` was modified

* `publicIpAddressId()` was added
* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added
* `platformVersion()` was added
* `withPrivateEndpointConnections(java.util.List)` was added
* `withPublicIpAddressId(java.lang.String)` was added
* `publicNetworkAccess()` was added
* `privateEndpointConnections()` was added

#### `models.SchemaContract$Update` was modified

* `withComponents(java.lang.Object)` was added

#### `models.ApiManagementServiceResource$Definition` was modified

* `withPrivateEndpointConnections(java.util.List)` was added
* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added
* `withPublicIpAddressId(java.lang.String)` was added

#### `models.ApiUpdateContract` was modified

* `license()` was added
* `withLicense(models.ApiLicenseInformation)` was added
* `withContact(models.ApiContactInformation)` was added
* `contact()` was added
* `withTermsOfServiceUrl(java.lang.String)` was added
* `termsOfServiceUrl()` was added

#### `models.SchemaContract$Definition` was modified

* `withComponents(java.lang.Object)` was added

#### `models.ApiCreateOrUpdateParameter` was modified

* `termsOfServiceUrl()` was added
* `contact()` was added
* `withLicense(models.ApiLicenseInformation)` was added
* `withTermsOfServiceUrl(java.lang.String)` was added
* `license()` was added
* `withContact(models.ApiContactInformation)` was added

#### `models.ApiContract$Definition` was modified

* `withLicense(models.ApiLicenseInformation)` was added
* `withTermsOfServiceUrl(java.lang.String)` was added
* `withContact(models.ApiContactInformation)` was added

#### `models.ApiTagResourceContractProperties` was modified

* `withContact(models.ApiContactInformation)` was added
* `withTermsOfServiceUrl(java.lang.String)` was added
* `withLicense(models.ApiLicenseInformation)` was added
* `withContact(models.ApiContactInformation)` was added
* `withLicense(models.ApiLicenseInformation)` was added
* `withTermsOfServiceUrl(java.lang.String)` was added

#### `models.HostnameConfiguration` was modified

* `withCertificateStatus(models.CertificateStatus)` was added
* `certificateStatus()` was added
* `withCertificateSource(models.CertificateSource)` was added
* `certificateSource()` was added

#### `models.ParameterContract` was modified

* `schemaId()` was added
* `examples()` was added
* `withSchemaId(java.lang.String)` was added
* `typeName()` was added
* `withTypeName(java.lang.String)` was added
* `withExamples(java.util.Map)` was added

## 1.0.0-beta.2 (2021-08-26)

- Azure Resource Manager ApiManagement client library for Java. This package contains Microsoft Azure SDK for ApiManagement Management SDK. ApiManagement Client. Package tag package-2020-12. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.OperationResultContract` was modified

* `models.ErrorResponseBody error()` -> `com.azure.core.management.exception.ManagementError error()`

#### `models.QuotaCounterContract` was modified

* `models.QuotaCounterValueContractProperties value()` -> `fluent.models.QuotaCounterValueContractProperties value()`

### Features Added

#### `models.TenantConfigurationSyncStateContract` was modified

* `lastOperationId()` was added

#### `ApiManagementManager$Configurable` was modified

* `withScope(java.lang.String)` was added

#### `models.OperationResultContract` was modified

* `name()` was added
* `idPropertiesId()` was added
* `type()` was added

## 1.0.0-beta.1 (2021-03-23)

- Azure Resource Manager ApiManagement client library for Java. This package contains Microsoft Azure SDK for ApiManagement Management SDK. ApiManagement Client. Package tag package-2020-12. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).


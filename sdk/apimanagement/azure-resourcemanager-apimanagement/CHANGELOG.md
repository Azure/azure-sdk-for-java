# Release History

## 1.0.0-beta.5 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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


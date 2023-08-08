# Release History

## 1.0.0-beta.4 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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


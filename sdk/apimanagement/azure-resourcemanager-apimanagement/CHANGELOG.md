# Release History

## 2.1.0-beta.1 (2026-09-15)

- Azure Resource Manager ApiManagement client library for Java. This package contains Microsoft Azure SDK for ApiManagement Management SDK. Resource provider operation status. Package api-version 2025-09-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ApiReleaseCollection` was removed

#### `models.ApiManagementSkusResult` was removed

#### `models.TagApiLinkCollection` was removed

#### `models.GatewayHostnameConfigurationCollection` was removed

#### `models.ApiManagementGatewayListResult` was removed

#### `models.DeletedServicesCollection` was removed

#### `models.PrivateEndpointConnectionListResult` was removed

#### `models.ResourceSkuResults` was removed

#### `models.GatewayCollection` was removed

#### `models.ApiCollection` was removed

#### `models.LoggerCollection` was removed

#### `models.EmailTemplateCollection` was removed

#### `models.ContentTypeCollection` was removed

#### `models.ProductWikisOperationsListHeaders` was removed

#### `models.WorkspaceCollection` was removed

#### `models.ApiManagementGatewayConfigConnectionListResult` was removed

#### `models.BackendCollection` was removed

#### `models.ReportCollection` was removed

#### `models.PortalConfigCollection` was removed

#### `models.GlobalSchemaCollection` was removed

#### `models.ProductCollection` was removed

#### `models.CertificateCollection` was removed

#### `models.PolicyRestrictionCollection` was removed

#### `models.IssueCommentCollection` was removed

#### `models.ProductWikisOperationsListNextHeaders` was removed

#### `models.NotificationCollection` was removed

#### `models.RequestReportCollection` was removed

#### `models.ProductGroupLinkCollection` was removed

#### `models.OpenIdConnectProviderCollection` was removed

#### `models.TagCollection` was removed

#### `models.UserCollection` was removed

#### `models.AuthorizationCollection` was removed

#### `models.ResolverCollection` was removed

#### `models.TagOperationLinkCollection` was removed

#### `models.IssueCollection` was removed

#### `models.AllPoliciesCollection` was removed

#### `models.GroupCollection` was removed

#### `models.WikiCollection` was removed

#### `models.GatewayCertificateAuthorityCollection` was removed

#### `models.TagDescriptionCollection` was removed

#### `models.ApiRevisionCollection` was removed

#### `models.NamedValueCollection` was removed

#### `models.OperationListResult` was removed

#### `models.DiagnosticCollection` was removed

#### `models.ProductWikisOperationsListResponse` was removed

#### `models.AccessInformationCollection` was removed

#### `models.IssueAttachmentCollection` was removed

#### `models.ApiManagementWorkspaceLinksListResult` was removed

#### `models.TenantSettingsCollection` was removed

#### `models.IdentityProviderList` was removed

#### `models.CacheCollection` was removed

#### `models.ApiManagementServiceListResult` was removed

#### `models.ProvisioningState` was removed

#### `models.UserIdentityCollection` was removed

#### `models.TagProductLinkCollection` was removed

#### `models.PortalRevisionCollection` was removed

#### `models.PolicyFragmentCollection` was removed

#### `models.AuthorizationServerCollection` was removed

#### `models.SchemaCollection` was removed

#### `models.OperationCollection` was removed

#### `models.DocumentationCollection` was removed

#### `models.ProductApiLinkCollection` was removed

#### `models.AuthorizationProviderCollection` was removed

#### `models.SubscriptionCollection` was removed

#### `models.RegionListResult` was removed

#### `models.TagResourceCollection` was removed

#### `models.AuthorizationAccessPolicyCollection` was removed

#### `models.GatewayResourceSkuResults` was removed

#### `models.ContentItemCollection` was removed

#### `models.ApiVersionSetCollection` was removed

#### `models.ProductWikisOperationsListNextResponse` was removed

#### `models.PortalRevisionsGetEntityTagHeaders` was modified

* `etag()` was removed
* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.AuthorizationServersCreateOrUpdateHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.WorkspaceSubscriptionsGetHeaders` was modified

* `etag()` was removed
* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.WorkspaceApiOperationsCreateOrUpdateHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.PortalSettingsContract` was modified

* `validate()` was removed

#### `models.ProductEntityBaseParameters` was modified

* `validate()` was removed

#### `models.ProductWikisGetHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed
* `etag()` was removed

#### `models.AuthorizationError` was modified

* `validate()` was removed

#### `models.ApiManagementWorkspaceLinks` was modified

* `listByService(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.PortalConfigPropertiesSignin` was modified

* `validate()` was removed

#### `models.GatewayApisGetEntityTagHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed
* `etag()` was removed

#### `models.IdentityProvidersListSecretsHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.GraphQLApiResolversUpdateHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed
* `etag()` was removed

#### `models.WorkspaceApiDiagnosticsGetEntityTagHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed
* `etag()` was removed

#### `models.AssociationContract` was modified

* `validate()` was removed
* `withProvisioningState(models.ProvisioningState)` was removed
* `models.ProvisioningState provisioningState()` -> `models.AssociationContractPropertiesProvisioningState provisioningState()`

#### `models.WorkspaceCertificatesGetHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.PrivateEndpointConnectionRequestProperties` was modified

* `validate()` was removed

#### `models.WorkspacesGetHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.RemotePrivateEndpointConnectionWrapper` was modified

* `validate()` was removed

#### `models.WorkspaceProductsGetHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.TenantAccessUpdateHeaders` was modified

* `etag()` was removed
* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.WorkspaceProductGroupLinksGetHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.GroupsGetHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed
* `etag()` was removed

#### `models.BackendCredentialsContract` was modified

* `validate()` was removed

#### `models.BodyDiagnosticSettings` was modified

* `validate()` was removed

#### `models.ProductsCreateOrUpdateHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed
* `etag()` was removed

#### `models.TagsGetByProductHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed
* `etag()` was removed

#### `models.BackendAuthorizationHeaderCredentials` was modified

* `validate()` was removed

#### `models.WorkspaceBackendsGetEntityTagHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.BackendTlsProperties` was modified

* `validate()` was removed

#### `models.ConnectivityCheckRequestSource` was modified

* `validate()` was removed

#### `models.WorkspacePoliciesCreateOrUpdateHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.ResourceCollectionValueItem` was modified

* `validate()` was removed

#### `models.ResourceLocationDataContract` was modified

* `validate()` was removed

#### `models.BackendPoolItem` was modified

* `validate()` was removed

#### `models.ProductsGetEntityTagHeaders` was modified

* `etag()` was removed
* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.ApiOperationsUpdateHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed
* `etag()` was removed

#### `models.LoggersUpdateHeaders` was modified

* `etag()` was removed
* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.IssuesGetHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.ApiManagementServiceUpdateParameters` was modified

* `validate()` was removed

#### `models.GatewaysUpdateHeaders` was modified

* `etag()` was removed
* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.ApiTagDescriptionsGetHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed
* `etag()` was removed

#### `models.TagDescriptionCreateParameters` was modified

* `validate()` was removed

#### `models.UsersGetHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.BackendProperties` was modified

* `validate()` was removed

#### `models.ApiOperationsCreateOrUpdateHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.WorkspaceSubscriptionsUpdateHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed
* `etag()` was removed

#### `models.AuthorizationProvidersGetHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.BackendSubnetConfiguration` was modified

* `validate()` was removed

#### `models.ProductUpdateParameters` was modified

* `validate()` was removed

#### `models.SaveConfigurationParameter` was modified

* `validate()` was removed

#### `models.CachesUpdateHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.EndpointDetail` was modified

* `EndpointDetail()` was changed to private access
* `withRegion(java.lang.String)` was removed
* `withPort(java.lang.Integer)` was removed
* `validate()` was removed

#### `models.LoggersGetEntityTagHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.QuotaCounterValueUpdateContract` was modified

* `validate()` was removed

#### `models.ApiIssueAttachmentsGetEntityTagHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.ApiTagDescriptionsCreateOrUpdateHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.AuthorizationLoginLinksPostHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.PolicyRestrictionsCreateOrUpdateHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.GatewaySku` was modified

* `GatewaySku()` was changed to private access
* `validate()` was removed
* `withName(models.ApiGatewaySkuType)` was removed

#### `models.WorkspaceApisGetHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed
* `etag()` was removed

#### `models.AuthorizationProviderOAuth2GrantTypes` was modified

* `validate()` was removed

#### `models.ApiDiagnosticsCreateOrUpdateHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.UserIdentityProperties` was modified

* `validate()` was removed

#### `models.UserSubscriptionsGetHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed
* `etag()` was removed

#### `models.GroupUpdateParameters` was modified

* `validate()` was removed

#### `models.PrivateEndpoint` was modified

* `validate()` was removed

#### `models.WorkspaceGlobalSchemasGetEntityTagHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.ApiContactInformation` was modified

* `validate()` was removed

#### `models.ApiIssuesGetHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed
* `etag()` was removed

#### `models.WorkspaceTagOperationLinksGetHeaders` was modified

* `etag()` was removed
* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.ApiLicenseInformation` was modified

* `validate()` was removed

#### `models.ApiManagementServiceBackupRestoreParameters` was modified

* `validate()` was removed

#### `models.MigrateToStv2Contract` was modified

* `validate()` was removed

#### `models.SignUpSettingsGetEntityTagHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed
* `etag()` was removed

#### `models.RegistrationDelegationSettingsProperties` was modified

* `validate()` was removed

#### `models.AuthorizationServersListSecretsHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.ApiIssueCommentsGetEntityTagHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.ConnectivityCheckRequestDestination` was modified

* `validate()` was removed

#### `models.ApiManagementServiceBaseProperties` was modified

* `validate()` was removed

#### `models.CertificateInformation` was modified

* `validate()` was removed

#### `models.GatewayHostnameConfigurationsGetEntityTagHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.SubscriptionsGetHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed
* `etag()` was removed

#### `models.ConnectivityIssue` was modified

* `ConnectivityIssue()` was changed to private access
* `validate()` was removed

#### `models.ApiReleasesGetHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed
* `etag()` was removed

#### `models.OpenIdAuthenticationSettingsContract` was modified

* `validate()` was removed

#### `models.WorkspacePolicyFragmentsGetEntityTagHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.PolicyRestrictionsUpdateHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.DeployConfigurationParameters` was modified

* `validate()` was removed

#### `models.DiagnosticsCreateOrUpdateHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.WorkspaceDiagnosticsUpdateHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed
* `etag()` was removed

#### `models.WorkspaceTagApiLinksGetHeaders` was modified

* `etag()` was removed
* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.WorkspaceSubscriptionsCreateOrUpdateHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.WorkspaceBackendsGetHeaders` was modified

* `etag()` was removed
* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.SubscriptionsUpdateHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.ApiIssuesGetEntityTagHeaders` was modified

* `etag()` was removed
* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.WorkspaceApiPoliciesGetHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed
* `etag()` was removed

#### `models.ApiVersionSetUpdateParameters` was modified

* `validate()` was removed

#### `models.ApiDiagnosticsUpdateHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.UsersUpdateHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.IssueUpdateContract` was modified

* `validate()` was removed

#### `models.AuthorizationProvidersCreateOrUpdateHeaders` was modified

* `etag()` was removed
* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.KeyVaultContractProperties` was modified

* `validate()` was removed

#### `models.ApiWikisGetEntityTagHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.AuthorizationServersUpdateHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.OperationDisplay` was modified

* `OperationDisplay()` was changed to private access
* `withResource(java.lang.String)` was removed
* `withProvider(java.lang.String)` was removed
* `withDescription(java.lang.String)` was removed
* `withOperation(java.lang.String)` was removed
* `validate()` was removed

#### `models.ConfigurationApi` was modified

* `validate()` was removed

#### `models.SubscriptionKeyParameterNamesContract` was modified

* `validate()` was removed

#### `models.DelegationSettingsGetEntityTagHeaders` was modified

* `etag()` was removed
* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.OperationTagResourceContractProperties` was modified

* `validate()` was removed

#### `models.ApiUpdateContract` was modified

* `validate()` was removed

#### `models.GlobalSchemasGetEntityTagHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.PortalConfigCspProperties` was modified

* `validate()` was removed

#### `models.HttpMessageDiagnostic` was modified

* `validate()` was removed

#### `models.SubscriptionsDelegationSettingsProperties` was modified

* `validate()` was removed

#### `models.ApiOperationPoliciesGetEntityTagHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.ProductsUpdateHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.AuthenticationSettingsContract` was modified

* `validate()` was removed

#### `models.WorkspaceTagsCreateOrUpdateHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.EndpointDependency` was modified

* `EndpointDependency()` was changed to private access
* `withEndpointDetails(java.util.List)` was removed
* `validate()` was removed
* `withDomainName(java.lang.String)` was removed

#### `models.DeletedServices` was modified

* `models.DeletedServiceContract purge(java.lang.String,java.lang.String)` -> `void purge(java.lang.String,java.lang.String)`
* `models.DeletedServiceContract purge(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `void purge(java.lang.String,java.lang.String,com.azure.core.util.Context)`

#### `models.WorkspaceBackendsUpdateHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.WorkspaceTagProductLinksGetHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.GatewayCertificateAuthoritiesGetHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.TagsGetHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.GroupsUpdateHeaders` was modified

* `etag()` was removed
* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.GatewayCertificateAuthoritiesCreateOrUpdateHeaders` was modified

* `etag()` was removed
* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.ApiDiagnosticsGetHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed
* `etag()` was removed

#### `models.EmailTemplateParametersContractProperties` was modified

* `validate()` was removed

#### `models.DelegationSettingsGetHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.VirtualNetworkConfiguration` was modified

* `validate()` was removed

#### `models.WorkspaceApiVersionSetsCreateOrUpdateHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.AuthorizationServersGetHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.TagProductLinksGetHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.FrontendConfiguration` was modified

* `validate()` was removed

#### `models.WorkspacePolicyFragmentsGetHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.WorkspaceProductApiLinksGetHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.CachesGetHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed
* `etag()` was removed

#### `models.LoggersCreateOrUpdateHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed
* `etag()` was removed

#### `models.WorkspaceNamedValuesGetEntityTagHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.AuthorizationsGetHeaders` was modified

* `etag()` was removed
* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.PortalRevisionsGetHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed
* `etag()` was removed

#### `models.ApiPoliciesGetHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.ApiManagementSkuCapacity` was modified

* `ApiManagementSkuCapacity()` was changed to private access
* `validate()` was removed

#### `models.DocumentationUpdateContract` was modified

* `validate()` was removed

#### `models.BackendsGetHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed
* `etag()` was removed

#### `models.SamplingSettings` was modified

* `validate()` was removed

#### `models.SubscriptionUpdateParameters` was modified

* `validate()` was removed

#### `models.ApiPoliciesGetEntityTagHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.TenantSettingsGetHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.WorkspaceApisGetEntityTagHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.PoliciesGetEntityTagHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.BackendsGetEntityTagHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed
* `etag()` was removed

#### `models.SignInSettingsGetEntityTagHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.WorkspacesCreateOrUpdateHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.AuthorizationLoginRequestContract` was modified

* `validate()` was removed

#### `models.TagsGetEntityStateByApiHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.ApisGetEntityTagHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.NamedValuesListValueHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.PolicyRestrictionsGetEntityTagHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed
* `etag()` was removed

#### `models.DiagnosticsGetHeaders` was modified

* `etag()` was removed
* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.ApiVersionSetEntityBase` was modified

* `validate()` was removed

#### `models.PipelineDiagnosticSettings` was modified

* `validate()` was removed

#### `models.WorkspaceApiOperationsGetHeaders` was modified

* `etag()` was removed
* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.ApiWikisGetHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.ApiManagementSkuRestrictions` was modified

* `ApiManagementSkuRestrictions()` was changed to private access
* `validate()` was removed

#### `models.ApiIssueAttachmentsGetHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.AuthorizationProviderOAuth2Settings` was modified

* `validate()` was removed

#### `models.AccessInformationUpdateParameters` was modified

* `validate()` was removed

#### `models.TenantAccessGetEntityTagHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.AuthorizationServersGetEntityTagHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.ApiManagementGatewaySkuPropertiesForPatch` was modified

* `validate()` was removed

#### `models.UsersGetEntityTagHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed
* `etag()` was removed

#### `models.ResolverUpdateContract` was modified

* `validate()` was removed

#### `models.GatewayConfigurationApi` was modified

* `validate()` was removed

#### `models.WorkspaceApiOperationPoliciesCreateOrUpdateHeaders` was modified

* `etag()` was removed
* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.ApiReleasesCreateOrUpdateHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.GatewayKeyRegenerationRequestContract` was modified

* `validate()` was removed

#### `models.WorkspaceDiagnosticsGetEntityTagHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.PolicyFragmentsGetEntityTagHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.ContentItemsGetEntityTagHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.IdentityProvidersUpdateHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed
* `etag()` was removed

#### `models.UsersCreateOrUpdateHeaders` was modified

* `etag()` was removed
* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.BackendCircuitBreaker` was modified

* `validate()` was removed

#### `models.TagsGetEntityStateByOperationHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.EmailTemplatesGetEntityTagHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.DocumentationsUpdateHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed
* `etag()` was removed

#### `models.GroupCreateParameters` was modified

* `validate()` was removed

#### `models.WorkspaceApiReleasesGetEntityTagHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.NamedValuesGetHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.OutboundEnvironmentEndpoint` was modified

* `OutboundEnvironmentEndpoint()` was changed to private access
* `withEndpoints(java.util.List)` was removed
* `validate()` was removed
* `withCategory(java.lang.String)` was removed

#### `models.ContentItemsCreateOrUpdateHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed
* `etag()` was removed

#### `models.HostnameConfiguration` was modified

* `validate()` was removed

#### `models.ErrorFieldContract` was modified

* `validate()` was removed

#### `models.PortalConfigPropertiesSignup` was modified

* `validate()` was removed

#### `models.OpenIdConnectProvidersCreateOrUpdateHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.SignInSettingsGetHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed
* `etag()` was removed

#### `models.ApiManagementServiceSkuProperties` was modified

* `validate()` was removed

#### `models.DiagnosticsUpdateHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.BackendProxyContract` was modified

* `validate()` was removed

#### `models.ApiManagementServices` was modified

* `list(com.azure.core.util.Context)` was removed
* `models.ApiManagementServiceResource delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `void delete(java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `models.ApiManagementServiceResource deleteById(java.lang.String)` -> `void deleteById(java.lang.String)`
* `models.ApiManagementServiceResource deleteByResourceGroup(java.lang.String,java.lang.String)` -> `void deleteByResourceGroup(java.lang.String,java.lang.String)`
* `models.ApiManagementServiceResource deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `listByResourceGroup(java.lang.String,com.azure.core.util.Context)` was removed

#### `models.WorkspaceNamedValuesListValueHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed
* `etag()` was removed

#### `models.ApiIssuesCreateOrUpdateHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed
* `etag()` was removed

#### `models.ParameterExampleContract` was modified

* `validate()` was removed

#### `models.ApiIssueCommentsCreateOrUpdateHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.WorkspaceLoggersUpdateHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.LoggerUpdateContract` was modified

* `validate()` was removed

#### `models.WorkspaceApiPoliciesCreateOrUpdateHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.ApiVersionSetsGetEntityTagHeaders` was modified

* `etag()` was removed
* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.TagResourceContractProperties` was modified

* `TagResourceContractProperties()` was changed to private access
* `withName(java.lang.String)` was removed
* `validate()` was removed
* `withId(java.lang.String)` was removed

#### `models.SubscriptionsCreateOrUpdateHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.WorkspaceApiVersionSetsGetEntityTagHeaders` was modified

* `etag()` was removed
* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.ApiManagementServiceApplyNetworkConfigurationParameters` was modified

* `validate()` was removed

#### `models.ApiPoliciesCreateOrUpdateHeaders` was modified

* `etag()` was removed
* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.WorkspaceDiagnosticsGetHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed
* `etag()` was removed

#### `models.GroupsGetEntityTagHeaders` was modified

* `etag()` was removed
* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.ConnectivityCheckRequestProtocolConfiguration` was modified

* `validate()` was removed

#### `models.AuthorizationConfirmConsentCodeRequestContract` was modified

* `validate()` was removed

#### `models.WorkspaceProductPoliciesGetHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.NamedValueEntityBaseParameters` was modified

* `validate()` was removed

#### `models.AdditionalLocation` was modified

* `validate()` was removed

#### `models.DiagnosticsGetEntityTagHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.PolicyRestrictionUpdateContract` was modified

* `validate()` was removed

#### `models.ConnectivityCheckRequestProtocolConfigurationHttpConfiguration` was modified

* `validate()` was removed

#### `models.WorkspaceApiReleasesCreateOrUpdateHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.ArmIdWrapper` was modified

* `validate()` was removed

#### `models.AuthorizationsCreateOrUpdateHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.GatewayListTraceContract` was modified

* `validate()` was removed

#### `models.BackendBaseParametersPool` was modified

* `validate()` was removed

#### `models.OpenIdConnectProvidersGetEntityTagHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.ApiIssuesUpdateHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.WorkspaceCertificatesRefreshSecretHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.TagOperationLinksGetHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.WorkspaceNamedValuesGetHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.ProductPoliciesGetEntityTagHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.EmailTemplatesUpdateHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed
* `etag()` was removed

#### `models.TenantAccessCreateHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.DocumentationsCreateOrUpdateHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.TagCreateUpdateParameters` was modified

* `validate()` was removed

#### `models.WorkspaceApiVersionSetsGetHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.WorkspaceGroupsGetEntityTagHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.WikiDocumentationContract` was modified

* `validate()` was removed

#### `models.ApiManagementSkuCapabilities` was modified

* `ApiManagementSkuCapabilities()` was changed to private access
* `validate()` was removed

#### `models.WorkspaceApiReleasesUpdateHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.SubscriptionCreateParameters` was modified

* `validate()` was removed

#### `models.ApiManagementServiceCheckNameAvailabilityParameters` was modified

* `validate()` was removed

#### `models.WorkspaceApiReleasesGetHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed
* `etag()` was removed

#### `models.DiagnosticUpdateContract` was modified

* `validate()` was removed

#### `models.BackendConfiguration` was modified

* `validate()` was removed

#### `models.OperationsResultsGetHeaders` was modified

* `withLocation(java.lang.String)` was removed
* `validate()` was removed

#### `models.WorkspaceProductPoliciesCreateOrUpdateHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.GraphQLApiResolversGetHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.ProductWikisCreateOrUpdateHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed
* `etag()` was removed

#### `models.KeyVaultLastAccessStatusContractProperties` was modified

* `validate()` was removed

#### `models.DocumentationsGetHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.ApiManagementServiceIdentity` was modified

* `validate()` was removed
* `java.util.UUID principalId()` -> `java.lang.String principalId()`
* `java.util.UUID tenantId()` -> `java.lang.String tenantId()`

#### `models.IdentityProvidersGetHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.ApiManagementSkuZoneDetails` was modified

* `ApiManagementSkuZoneDetails()` was changed to private access
* `validate()` was removed

#### `models.ProductTagResourceContractProperties` was modified

* `ProductTagResourceContractProperties()` was changed to private access
* `validate()` was removed
* `withName(java.lang.String)` was removed
* `withSubscriptionsLimit(java.lang.Integer)` was removed
* `withTerms(java.lang.String)` was removed
* `withSubscriptionRequired(java.lang.Boolean)` was removed
* `withDescription(java.lang.String)` was removed
* `withApprovalRequired(java.lang.Boolean)` was removed
* `withState(models.ProductState)` was removed
* `withId(java.lang.String)` was removed

#### `models.UserTokenParameters` was modified

* `validate()` was removed

#### `models.ApiOperationPoliciesGetHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.OAuth2AuthenticationSettingsContract` was modified

* `validate()` was removed

#### `models.ContentTypesGetHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed
* `etag()` was removed

#### `models.WorkspaceProductsGetEntityTagHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.RequestContract` was modified

* `validate()` was removed

#### `models.WorkspaceApiSchemasGetEntityTagHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.AuthorizationServerContractBaseProperties` was modified

* `validate()` was removed

#### `models.GatewaysGetEntityTagHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.ApiEntityBaseContract` was modified

* `validate()` was removed

#### `models.OperationStatusResult` was modified

* `java.lang.Float percentComplete()` -> `java.lang.Double percentComplete()`

#### `models.GatewayHostnameConfigurationsGetHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.ApiManagementGatewayBaseProperties` was modified

* `validate()` was removed

#### `models.WorkspaceLinksBaseProperties` was modified

* `validate()` was removed

#### `models.IdentityProvidersCreateOrUpdateHeaders` was modified

* `etag()` was removed
* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.WorkspaceApiDiagnosticsGetHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.OpenIdConnectProvidersListSecretsHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.PortalConfigCorsProperties` was modified

* `validate()` was removed

#### `models.ProductGroupLinksGetHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed
* `etag()` was removed

#### `models.WorkspaceGroupsCreateOrUpdateHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.DataMaskingEntity` was modified

* `validate()` was removed

#### `ApiManagementManager` was modified

* `fluent.ApiManagementClient serviceClient()` -> `fluent.ApiManagementManagementClient serviceClient()`

#### `models.AuthorizationAccessPoliciesGetHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.RepresentationContract` was modified

* `validate()` was removed

#### `models.AuthorizationServerUpdateContract` was modified

* `validate()` was removed

#### `models.ApisUpdateHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed
* `etag()` was removed

#### `models.ApiOperationsGetEntityTagHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.ApiWikisUpdateHeaders` was modified

* `etag()` was removed
* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.BackendsUpdateHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed
* `etag()` was removed

#### `models.ApiVersionSetsCreateOrUpdateHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.GatewaysListKeysHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.WorkspaceLoggersCreateOrUpdateHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed
* `etag()` was removed

#### `models.GraphQLApiResolverPoliciesGetHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed
* `etag()` was removed

#### `models.WorkspaceGroupsUpdateHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed
* `etag()` was removed

#### `models.ApiOperationPoliciesCreateOrUpdateHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.ApiManagementSkuLocationInfo` was modified

* `ApiManagementSkuLocationInfo()` was changed to private access
* `validate()` was removed

#### `models.UserCreateParameters` was modified

* `validate()` was removed

#### `models.ApiSchemasGetEntityTagHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed
* `etag()` was removed

#### `models.WorkspaceLoggersGetEntityTagHeaders` was modified

* `etag()` was removed
* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.CertificatesCreateOrUpdateHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed
* `etag()` was removed

#### `models.PolicyRestrictionsGetHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.LoggersGetHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed
* `etag()` was removed

#### `models.WorkspaceProductsUpdateHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.WorkspacesGetEntityTagHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed
* `etag()` was removed

#### `models.WorkspaceApiOperationsGetEntityTagHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.TagsUpdateHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.UserUpdateParameters` was modified

* `validate()` was removed

#### `models.ApiVersionSetsUpdateHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.ApiManagementSkuRestrictionInfo` was modified

* `ApiManagementSkuRestrictionInfo()` was changed to private access
* `validate()` was removed

#### `models.GatewaysCreateOrUpdateHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.WorkspaceLoggersGetHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.TagsGetEntityStateHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.CachesGetEntityTagHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed
* `etag()` was removed

#### `models.BackendBaseParameters` was modified

* `validate()` was removed

#### `models.OperationUpdateContract` was modified

* `validate()` was removed

#### `models.GatewayListDebugCredentialsContract` was modified

* `validate()` was removed

#### `models.IssueContractBaseProperties` was modified

* `validate()` was removed

#### `models.ApiWikisCreateOrUpdateHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed
* `etag()` was removed

#### `models.GraphQLApiResolverPoliciesCreateOrUpdateHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.ProductsGetHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed
* `etag()` was removed

#### `models.ApimResource` was modified

* `validate()` was removed

#### `models.GatewaysGetHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed
* `etag()` was removed

#### `models.WorkspaceApiOperationPoliciesGetEntityTagHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.WorkspaceApiSchemasGetHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.WikiUpdateContract` was modified

* `validate()` was removed

#### `models.ApiVersionConstraint` was modified

* `validate()` was removed

#### `models.WorkspaceProductPoliciesGetEntityTagHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.WorkspaceDiagnosticsCreateOrUpdateHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.WorkspaceTagsUpdateHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.ApiIssueCommentsGetHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed
* `etag()` was removed

#### `models.GatewayTokenRequestContract` was modified

* `validate()` was removed

#### `models.WorkspaceBackendsCreateOrUpdateHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.WorkspacePoliciesGetEntityTagHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.WorkspaceApisUpdateHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed
* `etag()` was removed

#### `models.WorkspaceLinksGateway` was modified

* `validate()` was removed

#### `models.GatewayHostnameConfigurationsCreateOrUpdateHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.AccessInformationCreateParameters` was modified

* `validate()` was removed

#### `models.WorkspaceCertificatesCreateOrUpdateHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.X509CertificateName` was modified

* `validate()` was removed

#### `models.ApiVersionSetContractDetails` was modified

* `validate()` was removed

#### `models.OpenIdConnectProvidersUpdateHeaders` was modified

* `etag()` was removed
* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.WorkspaceCertificatesGetEntityTagHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.TenantAccessListSecretsHeaders` was modified

* `etag()` was removed
* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.GraphQLApiResolversCreateOrUpdateHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed
* `etag()` was removed

#### `models.ApiCreateOrUpdatePropertiesWsdlSelector` was modified

* `validate()` was removed

#### `models.ProductPoliciesCreateOrUpdateHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed
* `etag()` was removed

#### `models.WorkspaceSubscriptionsListSecretsHeaders` was modified

* `etag()` was removed
* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.ApiTagDescriptionsGetEntityTagHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.PrivateLinkServiceConnectionState` was modified

* `validate()` was removed

#### `models.BackendReconnectContract` was modified

* `validate()` was removed

#### `models.ResourceSkuCapacity` was modified

* `ResourceSkuCapacity()` was changed to private access
* `validate()` was removed

#### `models.CacheUpdateParameters` was modified

* `validate()` was removed

#### `models.ApiCreateOrUpdateParameter` was modified

* `validate()` was removed

#### `models.EmailTemplateUpdateParameters` was modified

* `validate()` was removed

#### `models.CertificateCreateOrUpdateParameters` was modified

* `validate()` was removed

#### `models.ApisGetHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.ApiReleasesUpdateHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.ApiManagementGatewayUpdateParameters` was modified

* `validate()` was removed

#### `models.PortalConfigsGetEntityTagHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.TagsCreateOrUpdateHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.GatewayCertificateAuthoritiesGetEntityTagHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.KeyVaultContractCreateProperties` was modified

* `validate()` was removed

#### `models.CertificateConfiguration` was modified

* `validate()` was removed

#### `models.PortalConfigsGetHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed
* `etag()` was removed

#### `models.TagsAssignToApiHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.HttpHeader` was modified

* `validate()` was removed

#### `models.GroupsCreateOrUpdateHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.ApiManagementGatewaySkuProperties` was modified

* `validate()` was removed

#### `models.TagsGetByOperationHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.ProductWikisUpdateHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.IdentityProviderUpdateParameters` was modified

* `validate()` was removed

#### `models.ApiIssueAttachmentsCreateOrUpdateHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed
* `etag()` was removed

#### `models.ProductPoliciesGetHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed
* `etag()` was removed

#### `models.WorkspaceApiPoliciesGetEntityTagHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.ApiOperationsGetHeaders` was modified

* `etag()` was removed
* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.CachesCreateOrUpdateHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.BackendPool` was modified

* `validate()` was removed

#### `models.IdentityProvidersGetEntityTagHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed
* `etag()` was removed

#### `models.CircuitBreakerRule` was modified

* `validate()` was removed

#### `models.ConnectivityHop` was modified

* `validate()` was removed

#### `models.TagApiLinksGetHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.PortalConfigTermsOfServiceProperties` was modified

* `validate()` was removed

#### `models.ApiTagResourceContractProperties` was modified

* `ApiTagResourceContractProperties()` was changed to private access
* `withApiRevisionDescription(java.lang.String)` was removed
* `withApiVersion(java.lang.String)` was removed
* `withApiType(models.ApiType)` was removed
* `withApiRevision(java.lang.String)` was removed
* `withSubscriptionRequired(java.lang.Boolean)` was removed
* `withAuthenticationSettings(models.AuthenticationSettingsContract)` was removed
* `withApiVersionDescription(java.lang.String)` was removed
* `withIsCurrent(java.lang.Boolean)` was removed
* `withSubscriptionKeyParameterNames(models.SubscriptionKeyParameterNamesContract)` was removed
* `withDescription(java.lang.String)` was removed
* `validate()` was removed
* `withLicense(models.ApiLicenseInformation)` was removed
* `withProtocols(java.util.List)` was removed
* `withContact(models.ApiContactInformation)` was removed
* `withApiVersionSetId(java.lang.String)` was removed
* `withName(java.lang.String)` was removed
* `withTermsOfServiceUrl(java.lang.String)` was removed
* `withId(java.lang.String)` was removed
* `withPath(java.lang.String)` was removed
* `withServiceUrl(java.lang.String)` was removed

#### `models.WorkspaceGlobalSchemasGetHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed
* `etag()` was removed

#### `models.ProductWikisGetEntityTagHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.ApiManagementSkuCosts` was modified

* `ApiManagementSkuCosts()` was changed to private access
* `validate()` was removed

#### `models.EmailTemplatesGetHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.WorkspaceTagsGetEntityStateHeaders` was modified

* `etag()` was removed
* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.TokenBodyParameterContract` was modified

* `validate()` was removed

#### `models.SubscriptionsGetEntityTagHeaders` was modified

* `etag()` was removed
* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.WorkspacePoliciesGetHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.WorkspaceSubscriptionsGetEntityTagHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.OperationEntityBaseContract` was modified

* `validate()` was removed

#### `models.ApiReleasesGetEntityTagHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.RecipientsContractProperties` was modified

* `validate()` was removed

#### `models.ApiVersionSetsGetHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed
* `etag()` was removed

#### `models.BackendServiceFabricClusterProperties` was modified

* `validate()` was removed

#### `models.CertificatesGetHeaders` was modified

* `etag()` was removed
* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.ApiGatewayConfigConnections` was modified

* `listByGateway(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.CircuitBreakerFailureCondition` was modified

* `validate()` was removed

#### `models.NamedValueCreateContract` was modified

* `validate()` was removed

#### `models.WorkspaceTagsGetHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.ResponseContract` was modified

* `validate()` was removed

#### `models.OpenidConnectProviderUpdateContract` was modified

* `validate()` was removed

#### `models.ConnectivityStatusContract` was modified

* `ConnectivityStatusContract()` was changed to private access
* `withName(java.lang.String)` was removed
* `withResourceType(java.lang.String)` was removed
* `validate()` was removed
* `withLastStatusChange(java.time.OffsetDateTime)` was removed
* `withError(java.lang.String)` was removed
* `withLastUpdated(java.time.OffsetDateTime)` was removed
* `withStatus(models.ConnectivityStatusType)` was removed
* `withIsOptional(boolean)` was removed

#### `models.DataMasking` was modified

* `validate()` was removed

#### `models.WorkspaceProductsCreateOrUpdateHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.WorkspaceGroupsGetHeaders` was modified

* `etag()` was removed
* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.UserEntityBaseParameters` was modified

* `validate()` was removed

#### `models.WorkspacesUpdateHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.ErrorResponseBody` was modified

* `validate()` was removed

#### `models.PrivateEndpointConnectionRequest` was modified

* `validate()` was removed

#### `models.ApiExportResultValue` was modified

* `ApiExportResultValue()` was changed to private access
* `validate()` was removed
* `withLink(java.lang.String)` was removed

#### `models.GatewaySkuCapacity` was modified

* `GatewaySkuCapacity()` was changed to private access
* `validate()` was removed

#### `models.WorkspaceApiOperationsUpdateHeaders` was modified

* `etag()` was removed
* `withEtag(java.lang.String)` was removed
* `validate()` was removed

#### `models.CertificatesRefreshSecretHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.AuthorizationsConfirmConsentCodeHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.AuthorizationAccessPoliciesCreateOrUpdateHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed
* `etag()` was removed

#### `models.OperationResultLogItemContract` was modified

* `OperationResultLogItemContract()` was changed to private access
* `withObjectType(java.lang.String)` was removed
* `withObjectKey(java.lang.String)` was removed
* `validate()` was removed
* `withAction(java.lang.String)` was removed

#### `models.GlobalSchemasGetHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.GraphQLApiResolversGetEntityTagHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed
* `etag()` was removed

#### `models.WorkspaceApiDiagnosticsUpdateHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed
* `etag()` was removed

#### `models.SignUpSettingsGetHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed
* `etag()` was removed

#### `models.PoliciesCreateOrUpdateHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.ParameterContract` was modified

* `validate()` was removed

#### `models.TagsGetEntityStateByProductHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.BackendsCreateOrUpdateHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.CertificatesGetEntityTagHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.PortalConfigDelegationProperties` was modified

* `validate()` was removed

#### `models.GraphQLApiResolverPoliciesGetEntityTagHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.BackendUpdateParameters` was modified

* `validate()` was removed

#### `models.WorkspaceApiDiagnosticsCreateOrUpdateHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.SubscriptionsListSecretsHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.TagsGetByApiHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.ApiGateways` was modified

* `models.ApiManagementGatewayResource delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `void delete(java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `models.ApiManagementGatewayResource deleteById(java.lang.String)` -> `void deleteById(java.lang.String)`
* `models.ApiManagementGatewayResource deleteByResourceGroup(java.lang.String,java.lang.String)` -> `void deleteByResourceGroup(java.lang.String,java.lang.String)`
* `list(com.azure.core.util.Context)` was removed
* `listByResourceGroup(java.lang.String,com.azure.core.util.Context)` was removed
* `models.ApiManagementGatewayResource deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`

#### `models.PoliciesGetHeaders` was modified

* `validate()` was removed
* `withEtag(java.lang.String)` was removed
* `etag()` was removed

#### `models.ApiDiagnosticsGetEntityTagHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

#### `models.TermsOfServiceProperties` was modified

* `validate()` was removed

#### `models.TenantAccessGetHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.WorkspaceApiOperationPoliciesGetHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed
* `etag()` was removed

#### `models.ApiSchemasGetHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.PolicyDescriptionContract` was modified

* `validate()` was removed

#### `models.OpenIdConnectProvidersGetHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.PolicyFragmentsGetHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.ContentItemsGetHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.IdentityProviderCreateContract` was modified

* `validate()` was removed

#### `models.DocumentationsGetEntityTagHeaders` was modified

* `validate()` was removed
* `etag()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.FailureStatusCodeRange` was modified

* `validate()` was removed

#### `models.ProductApiLinksGetHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.ConnectivityCheckRequest` was modified

* `validate()` was removed

#### `models.IdentityProviderBaseParameters` was modified

* `validate()` was removed

#### `models.ResourceSku` was modified

* `ResourceSku()` was changed to private access
* `withName(models.SkuType)` was removed
* `validate()` was removed

#### `models.NamedValueUpdateParameters` was modified

* `validate()` was removed

#### `models.WorkspaceApiVersionSetsUpdateHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.ContentTypesCreateOrUpdateHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `validate()` was removed
* `etag()` was removed

#### `models.NamedValuesGetEntityTagHeaders` was modified

* `withEtag(java.lang.String)` was removed
* `etag()` was removed
* `validate()` was removed

### Features Added

* `models.BackendSessionIdSource` was added

* `models.ApiToolsUpdateResponse` was added

* `models.LLMDiagnosticSettings` was added

* `models.McpTransportType` was added

* `models.ClientApplicationsGetEntityTagResponse` was added

* `models.ApiToolsCreateOrUpdateHeaders` was added

* `models.ApiToolsGetResponse` was added

* `models.ManagedServiceIdentity` was added

* `models.AuthorizationProviderOAuth2FederatedIdentityCredentialsGrantType` was added

* `models.ApiToolsUpdateHeaders` was added

* `models.AuthorizationProviderFederatedIdentityCredentialsProperties` was added

* `models.ClientApplicationProductLinks` was added

* `models.CarbonEmissionCategory` was added

* `models.AuthorizationProvidersRefreshSecretResponse` was added

* `models.ProductEntityBaseParametersApplication` was added

* `models.GatewayHostnameBindingKeyVaultLastStatus` was added

* `models.GatewayHostnameBindingResource$Definition` was added

* `models.GatewayHostnameBindingResource$Update` was added

* `models.BackendSessionAffinity` was added

* `models.ClientApplicationContract$Definition` was added

* `models.ApiTools` was added

* `models.UserAssignedIdentity` was added

* `models.KeyVaultFetchCode` was added

* `models.ClientApplicationContract` was added

* `models.ApiToolsGetHeaders` was added

* `models.GatewayHostnameBindingResource$UpdateStages` was added

* `models.AuthorizationProvidersRefreshSecretHeaders` was added

* `models.GatewayHostnameBindingKeyVault` was added

* `models.ProductApplicationContractEntra` was added

* `models.ClientApplicationContract$UpdateStages` was added

* `models.ClientApplicationSecretsContract` was added

* `models.ApiGatewayHostnameBindings` was added

* `models.ClientApplicationProductLinksGetHeaders` was added

* `models.LlmMessageLogTypes` was added

* `models.ToolContract$DefinitionStages` was added

* `models.ClientApplicationContract$DefinitionStages` was added

* `models.ApiToolsCreateOrUpdateResponse` was added

* `models.ProductApplicationContract` was added

* `models.ClientApplicationsCreateOrUpdateResponse` was added

* `models.ClientApplicationsGetHeaders` was added

* `models.AssociationContractPropertiesProvisioningState` was added

* `models.AuthorizationProviderKeyVaultCreateProperties` was added

* `models.ClientApplicationsCreateOrUpdateHeaders` was added

* `models.ClientApplicationState` was added

* `models.ProductAuthType` was added

* `models.ClientApplicationProductLinkContract` was added

* `models.ReleaseChannel` was added

* `models.LLMMessageDiagnosticSettings` was added

* `models.ClientApplicationSecretsContractEntra` was added

* `models.AuthorizationProviderKeyVaultContract` was added

* `models.ToolContract$Definition` was added

* `models.BackendSessionId` was added

* `models.ManagedServiceIdentityType` was added

* `models.ToolContract$UpdateStages` was added

* `models.BackendFailureResponse` was added

* `models.ApiToolsGetEntityTagResponse` was added

* `models.McpProperties` was added

* `models.ApiToolsGetEntityTagHeaders` was added

* `models.ClientApplicationProductLinksGetResponse` was added

* `models.LlmDiagnosticSettingsValue` was added

* `models.ClientApplicationContract$Update` was added

* `models.GatewayHostnameBindingResource$DefinitionStages` was added

* `models.ClientApplications` was added

* `models.McpEndpoint` was added

* `models.GatewayHostnameBindingResource` was added

* `models.ToolContract$Update` was added

* `models.ClientApplicationProductLinkContract$DefinitionStages` was added

* `models.GatewayHostnameBindingCertificate` was added

* `models.ToolContract` was added

* `models.ClientApplicationsGetEntityTagHeaders` was added

* `models.ClientApplicationsGetResponse` was added

* `models.ClientApplicationProductLinkContract$Definition` was added

#### `models.PortalRevisionsGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.AuthorizationServersCreateOrUpdateHeaders` was modified

* `retryAfter()` was added
* `eTag()` was added

#### `models.ContentTypeContract` was modified

* `systemData()` was added

#### `models.WorkspaceSubscriptionsGetHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceApiOperationsCreateOrUpdateHeaders` was modified

* `retryAfter()` was added
* `eTag()` was added

#### `models.PortalSettingsContract` was modified

* `systemData()` was added

#### `models.ProductEntityBaseParameters` was modified

* `authenticationType()` was added
* `withApplication(models.ProductEntityBaseParametersApplication)` was added
* `application()` was added
* `withAuthenticationType(java.util.List)` was added

#### `models.ProductWikisGetHeaders` was modified

* `eTag()` was added

#### `models.ApiManagementWorkspaceLinks` was modified

* `listByService(java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

#### `models.PortalSigninSettings` was modified

* `systemData()` was added

#### `models.GatewayApisGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.PolicyContract` was modified

* `systemData()` was added

#### `models.IdentityProvidersListSecretsHeaders` was modified

* `eTag()` was added

#### `models.GraphQLApiResolversUpdateHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceApiDiagnosticsGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.AssociationContract` was modified

* `withProvisioningState(models.AssociationContractPropertiesProvisioningState)` was added
* `systemData()` was added

#### `models.WorkspaceCertificatesGetHeaders` was modified

* `eTag()` was added

#### `models.PortalSignupSettings` was modified

* `systemData()` was added

#### `models.WorkspacesGetHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceProductsGetHeaders` was modified

* `eTag()` was added

#### `models.TenantAccessUpdateHeaders` was modified

* `eTag()` was added

#### `models.IdentityProviderContract` was modified

* `certificateId()` was added
* `systemData()` was added

#### `models.WorkspaceProductGroupLinksGetHeaders` was modified

* `eTag()` was added

#### `models.ApiType` was modified

* `MCP` was added

#### `models.GroupsGetHeaders` was modified

* `eTag()` was added

#### `models.IssueCommentContract` was modified

* `systemData()` was added

#### `models.ProductsCreateOrUpdateHeaders` was modified

* `eTag()` was added

#### `models.TagsGetByProductHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceBackendsGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.BackendTlsProperties` was modified

* `serverCertificateThumbprints()` was added
* `withServerX509Names(java.util.List)` was added
* `serverX509Names()` was added
* `withServerCertificateThumbprints(java.util.List)` was added

#### `models.WorkspacePoliciesCreateOrUpdateHeaders` was modified

* `eTag()` was added

#### `models.ResourceCollectionValueItem` was modified

* `systemData()` was added

#### `models.LoggerContract` was modified

* `systemData()` was added

#### `models.ApiManagementGatewayConfigConnectionResource` was modified

* `systemData()` was added

#### `models.BackendPoolItem` was modified

* `preferredCarbonEmission()` was added
* `withPreferredCarbonEmission(models.CarbonEmissionCategory)` was added

#### `models.ProductsGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.ApiOperationsUpdateHeaders` was modified

* `eTag()` was added

#### `models.UserContract` was modified

* `systemData()` was added

#### `models.LoggersUpdateHeaders` was modified

* `eTag()` was added

#### `models.IssuesGetHeaders` was modified

* `eTag()` was added

#### `models.AuthorizationAccessPolicyContract` was modified

* `systemData()` was added

#### `models.ApiManagementServiceUpdateParameters` was modified

* `withZoneRedundant(java.lang.Boolean)` was added
* `zoneRedundant()` was added
* `releaseChannel()` was added
* `withReleaseChannel(models.ReleaseChannel)` was added

#### `models.BackendContract` was modified

* `systemData()` was added
* `azureRegion()` was added

#### `models.GatewaysUpdateHeaders` was modified

* `eTag()` was added

#### `models.ApiTagDescriptionsGetHeaders` was modified

* `eTag()` was added

#### `models.UsersGetHeaders` was modified

* `eTag()` was added

#### `models.OpenidConnectProviderContract` was modified

* `systemData()` was added

#### `models.ApiOperationsCreateOrUpdateHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceSubscriptionsUpdateHeaders` was modified

* `eTag()` was added

#### `models.AuthorizationProvidersGetHeaders` was modified

* `eTag()` was added

#### `models.ProductUpdateParameters` was modified

* `withApplication(models.ProductEntityBaseParametersApplication)` was added
* `withAuthenticationType(java.util.List)` was added
* `application()` was added
* `authenticationType()` was added

#### `models.PortalRevisionContract` was modified

* `systemData()` was added

#### `models.CachesUpdateHeaders` was modified

* `eTag()` was added

#### `models.LoggersGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.SubscriptionContract` was modified

* `systemData()` was added

#### `models.ApiIssueAttachmentsGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.ApiTagDescriptionsCreateOrUpdateHeaders` was modified

* `eTag()` was added
* `retryAfter()` was added

#### `models.EmailTemplateContract` was modified

* `systemData()` was added

#### `models.AuthorizationLoginLinksPostHeaders` was modified

* `eTag()` was added

#### `models.DiagnosticContract$Definition` was modified

* `withLargeLanguageModel(models.LLMDiagnosticSettings)` was added

#### `models.PolicyRestrictionsCreateOrUpdateHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceApisGetHeaders` was modified

* `eTag()` was added

#### `models.AuthorizationProviderOAuth2GrantTypes` was modified

* `withAuthorizationCodeWithFederatedIdentityCredentials(models.AuthorizationProviderOAuth2FederatedIdentityCredentialsGrantType)` was added
* `authorizationCodeWithFederatedIdentityCredentials()` was added

#### `models.ApiDiagnosticsCreateOrUpdateHeaders` was modified

* `eTag()` was added

#### `models.UserSubscriptionsGetHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceGlobalSchemasGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.ApiIssuesGetHeaders` was modified

* `eTag()` was added

#### `models.TagProductLinkContract` was modified

* `systemData()` was added

#### `models.WorkspaceTagOperationLinksGetHeaders` was modified

* `eTag()` was added

#### `models.SignUpSettingsGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.AuthorizationServersListSecretsHeaders` was modified

* `eTag()` was added

#### `models.ApiIssueCommentsGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.ApiManagementServiceBaseProperties` was modified

* `releaseChannel()` was added
* `withZoneRedundant(java.lang.Boolean)` was added
* `withReleaseChannel(models.ReleaseChannel)` was added
* `zoneRedundant()` was added

#### `models.GatewayHostnameConfigurationsGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.SubscriptionsGetHeaders` was modified

* `eTag()` was added

#### `models.ApiReleasesGetHeaders` was modified

* `eTag()` was added

#### `models.WorkspacePolicyFragmentsGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.PolicyRestrictionsUpdateHeaders` was modified

* `eTag()` was added

#### `models.ProductContract$Update` was modified

* `withApplication(models.ProductEntityBaseParametersApplication)` was added
* `withAuthenticationType(java.util.List)` was added

#### `models.DiagnosticsCreateOrUpdateHeaders` was modified

* `eTag()` was added
* `retryAfter()` was added

#### `models.WorkspaceDiagnosticsUpdateHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceTagApiLinksGetHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceSubscriptionsCreateOrUpdateHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceBackendsGetHeaders` was modified

* `eTag()` was added

#### `models.SubscriptionsUpdateHeaders` was modified

* `eTag()` was added

#### `models.GatewayCertificateAuthorityContract` was modified

* `systemData()` was added

#### `models.ApiIssuesGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceApiPoliciesGetHeaders` was modified

* `eTag()` was added

#### `models.ApiDiagnosticsUpdateHeaders` was modified

* `eTag()` was added

#### `models.AllPoliciesContract` was modified

* `systemData()` was added

#### `models.OperationContract` was modified

* `systemData()` was added

#### `models.UsersUpdateHeaders` was modified

* `eTag()` was added

#### `models.AuthorizationProvidersCreateOrUpdateHeaders` was modified

* `eTag()` was added
* `retryAfter()` was added

#### `models.ApiWikisGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.AuthorizationServersUpdateHeaders` was modified

* `eTag()` was added

#### `models.DelegationSettingsGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.ApiUpdateContract` was modified

* `mcpProperties()` was added
* `withMcpProperties(models.McpProperties)` was added

#### `models.GlobalSchemasGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.RecipientEmailContract` was modified

* `systemData()` was added

#### `models.ApiOperationPoliciesGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.ProductsUpdateHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceTagsCreateOrUpdateHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceBackendsUpdateHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceTagProductLinksGetHeaders` was modified

* `eTag()` was added

#### `models.GatewayCertificateAuthoritiesGetHeaders` was modified

* `eTag()` was added

#### `models.TagApiLinkContract` was modified

* `systemData()` was added

#### `models.TagsGetHeaders` was modified

* `eTag()` was added

#### `models.WikiContract` was modified

* `systemData()` was added

#### `models.GroupsUpdateHeaders` was modified

* `eTag()` was added

#### `models.GatewayCertificateAuthoritiesCreateOrUpdateHeaders` was modified

* `eTag()` was added

#### `models.BackendContract$Definition` was modified

* `withAzureRegion(java.lang.String)` was added

#### `models.ApiDiagnosticsGetHeaders` was modified

* `eTag()` was added

#### `models.DelegationSettingsGetHeaders` was modified

* `eTag()` was added

#### `models.DiagnosticContract` was modified

* `systemData()` was added
* `largeLanguageModel()` was added

#### `models.WorkspaceApiVersionSetsCreateOrUpdateHeaders` was modified

* `eTag()` was added

#### `models.AuthorizationServersGetHeaders` was modified

* `eTag()` was added

#### `models.TagProductLinksGetHeaders` was modified

* `eTag()` was added

#### `models.WorkspacePolicyFragmentsGetHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceProductApiLinksGetHeaders` was modified

* `eTag()` was added

#### `models.GroupContract` was modified

* `systemData()` was added

#### `models.CachesGetHeaders` was modified

* `eTag()` was added

#### `models.LoggersCreateOrUpdateHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceNamedValuesGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.DeletedServiceContract` was modified

* `systemData()` was added

#### `models.AuthorizationsGetHeaders` was modified

* `eTag()` was added

#### `models.PortalRevisionsGetHeaders` was modified

* `eTag()` was added

#### `models.ContentItemContract` was modified

* `systemData()` was added

#### `models.ApiPoliciesGetHeaders` was modified

* `eTag()` was added

#### `models.BackendsGetHeaders` was modified

* `eTag()` was added

#### `models.ApiPoliciesGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.TenantSettingsGetHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceApisGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.PoliciesGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.BackendsGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.SignInSettingsGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.WorkspacesCreateOrUpdateHeaders` was modified

* `eTag()` was added

#### `models.TagsGetEntityStateByApiHeaders` was modified

* `eTag()` was added

#### `models.ApisGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.NamedValuesListValueHeaders` was modified

* `eTag()` was added

#### `models.PolicyRestrictionsGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.ApiManagementWorkspaceLinksResource` was modified

* `systemData()` was added

#### `models.DiagnosticsGetHeaders` was modified

* `eTag()` was added

#### `models.DiagnosticContract$Update` was modified

* `withLargeLanguageModel(models.LLMDiagnosticSettings)` was added

#### `models.WorkspaceApiOperationsGetHeaders` was modified

* `eTag()` was added

#### `models.ApiWikisGetHeaders` was modified

* `eTag()` was added

#### `models.IssueAttachmentContract` was modified

* `systemData()` was added

#### `models.ApiIssueAttachmentsGetHeaders` was modified

* `eTag()` was added

#### `models.AuthorizationProviderOAuth2Settings` was modified

* `keyVault()` was added
* `withKeyVault(models.AuthorizationProviderKeyVaultContract)` was added
* `federatedIdentityCredentialsProperties()` was added

#### `models.AuthorizationContract` was modified

* `systemData()` was added

#### `models.TenantAccessGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.AuthorizationServersGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.UsersGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.GatewayHostnameConfigurationContract` was modified

* `systemData()` was added

#### `models.WorkspaceApiOperationPoliciesCreateOrUpdateHeaders` was modified

* `eTag()` was added
* `retryAfter()` was added

#### `models.ApiReleasesCreateOrUpdateHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceDiagnosticsGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.PolicyFragmentsGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.ContentItemsGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.IdentityProvidersUpdateHeaders` was modified

* `eTag()` was added

#### `models.UsersCreateOrUpdateHeaders` was modified

* `eTag()` was added
* `retryAfter()` was added

#### `models.TagsGetEntityStateByOperationHeaders` was modified

* `eTag()` was added

#### `models.EmailTemplatesGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.DocumentationsUpdateHeaders` was modified

* `eTag()` was added

#### `models.IdentityProviderContract$Update` was modified

* `withCertificateId(java.lang.String)` was added

#### `models.WorkspaceApiReleasesGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.NamedValuesGetHeaders` was modified

* `eTag()` was added

#### `models.ContentItemsCreateOrUpdateHeaders` was modified

* `eTag()` was added

#### `models.IssueContract` was modified

* `systemData()` was added

#### `models.OpenIdConnectProvidersCreateOrUpdateHeaders` was modified

* `eTag()` was added

#### `models.SignInSettingsGetHeaders` was modified

* `eTag()` was added

#### `models.DiagnosticsUpdateHeaders` was modified

* `eTag()` was added

#### `models.ApiManagementServices` was modified

* `refreshHostnames(java.lang.String,java.lang.String)` was added
* `refreshHostnames(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `list(java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `listByResourceGroup(java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

#### `models.WorkspaceContract` was modified

* `systemData()` was added

#### `models.WorkspaceNamedValuesListValueHeaders` was modified

* `eTag()` was added

#### `models.ApiIssuesCreateOrUpdateHeaders` was modified

* `eTag()` was added
* `retryAfter()` was added

#### `models.ApiIssueCommentsCreateOrUpdateHeaders` was modified

* `eTag()` was added
* `retryAfter()` was added

#### `models.WorkspaceLoggersUpdateHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceApiPoliciesCreateOrUpdateHeaders` was modified

* `eTag()` was added
* `retryAfter()` was added

#### `models.ApiVersionSetsGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.AuthorizationProviders` was modified

* `refreshSecretWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `refreshSecret(java.lang.String,java.lang.String,java.lang.String)` was added

#### `models.SubscriptionsCreateOrUpdateHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceApiVersionSetsGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.GatewayContract$Update` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.CertificateContract` was modified

* `systemData()` was added

#### `models.ApiPoliciesCreateOrUpdateHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceDiagnosticsGetHeaders` was modified

* `eTag()` was added

#### `models.GroupsGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceProductPoliciesGetHeaders` was modified

* `eTag()` was added

#### `models.DiagnosticsGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceApiReleasesCreateOrUpdateHeaders` was modified

* `eTag()` was added
* `retryAfter()` was added

#### `models.AuthorizationsCreateOrUpdateHeaders` was modified

* `eTag()` was added

#### `models.ApiManagementServiceResource$Update` was modified

* `withZoneRedundant(java.lang.Boolean)` was added
* `withReleaseChannel(models.ReleaseChannel)` was added

#### `models.BackendBaseParametersPool` was modified

* `withSessionAffinity(models.BackendSessionAffinity)` was added
* `withFailureResponse(models.BackendFailureResponse)` was added

#### `models.OpenIdConnectProvidersGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.ApiManagementServiceResource` was modified

* `refreshHostnames()` was added
* `releaseChannel()` was added
* `refreshHostnames(com.azure.core.util.Context)` was added
* `zoneRedundant()` was added

#### `models.ApiIssuesUpdateHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceCertificatesRefreshSecretHeaders` was modified

* `eTag()` was added

#### `models.SchemaContract` was modified

* `systemData()` was added

#### `models.TagOperationLinksGetHeaders` was modified

* `eTag()` was added

#### `models.TenantConfigurationSyncStateContract` was modified

* `systemData()` was added

#### `models.PrivateEndpointConnection` was modified

* `systemData()` was added
* `groupIds()` was added

#### `models.WorkspaceNamedValuesGetHeaders` was modified

* `eTag()` was added

#### `models.ProductPoliciesGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.EmailTemplatesUpdateHeaders` was modified

* `eTag()` was added

#### `models.TenantAccessCreateHeaders` was modified

* `eTag()` was added

#### `models.DocumentationsCreateOrUpdateHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceApiVersionSetsGetHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceGroupsGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceApiReleasesUpdateHeaders` was modified

* `eTag()` was added

#### `models.TagDescriptionContract` was modified

* `systemData()` was added

#### `models.WorkspaceApiReleasesGetHeaders` was modified

* `eTag()` was added

#### `models.DiagnosticUpdateContract` was modified

* `systemData()` was added

#### `models.CacheContract` was modified

* `systemData()` was added

#### `models.SkuType` was modified

* `PREMIUM_V2` was added

#### `models.OperationsResultsGetHeaders` was modified

* `retryAfter()` was added

#### `models.WorkspaceProductPoliciesCreateOrUpdateHeaders` was modified

* `retryAfter()` was added
* `eTag()` was added

#### `models.PolicyRestrictionContract` was modified

* `systemData()` was added

#### `models.GraphQLApiResolversGetHeaders` was modified

* `eTag()` was added

#### `models.ProductWikisCreateOrUpdateHeaders` was modified

* `retryAfter()` was added
* `eTag()` was added

#### `models.DocumentationsGetHeaders` was modified

* `eTag()` was added

#### `models.IdentityProvidersGetHeaders` was modified

* `eTag()` was added

#### `models.ApiOperationPoliciesGetHeaders` was modified

* `eTag()` was added

#### `models.ContentTypesGetHeaders` was modified

* `eTag()` was added

#### `models.NotificationContract` was modified

* `systemData()` was added

#### `models.WorkspaceProductsGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceApiSchemasGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.TagOperationLinkContract` was modified

* `systemData()` was added

#### `models.GatewaysGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.ApiEntityBaseContract` was modified

* `mcpProperties()` was added
* `withMcpProperties(models.McpProperties)` was added

#### `models.GatewayHostnameConfigurationsGetHeaders` was modified

* `eTag()` was added

#### `models.IdentityProvidersCreateOrUpdateHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceApiDiagnosticsGetHeaders` was modified

* `eTag()` was added

#### `models.OpenIdConnectProvidersListSecretsHeaders` was modified

* `eTag()` was added

#### `models.ProductGroupLinksGetHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceGroupsCreateOrUpdateHeaders` was modified

* `eTag()` was added

#### `models.NamedValueContract` was modified

* `systemData()` was added

#### `ApiManagementManager` was modified

* `apiTools()` was added
* `clientApplicationProductLinks()` was added
* `apiGatewayHostnameBindings()` was added
* `clientApplications()` was added

#### `models.AuthorizationAccessPoliciesGetHeaders` was modified

* `eTag()` was added

#### `models.ProductContract$Definition` was modified

* `withApplication(models.ProductEntityBaseParametersApplication)` was added
* `withAuthenticationType(java.util.List)` was added

#### `models.AuthorizationServerUpdateContract` was modified

* `systemData()` was added

#### `models.ApisUpdateHeaders` was modified

* `eTag()` was added

#### `models.ApiOperationsGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.ApiWikisUpdateHeaders` was modified

* `eTag()` was added

#### `models.BackendsUpdateHeaders` was modified

* `eTag()` was added

#### `models.ApiVersionSetsCreateOrUpdateHeaders` was modified

* `eTag()` was added

#### `models.GatewaysListKeysHeaders` was modified

* `eTag()` was added

#### `models.PrivateLinkResource` was modified

* `systemData()` was added

#### `models.WorkspaceLoggersCreateOrUpdateHeaders` was modified

* `eTag()` was added

#### `models.GraphQLApiResolverPoliciesGetHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceGroupsUpdateHeaders` was modified

* `eTag()` was added

#### `models.ApiOperationPoliciesCreateOrUpdateHeaders` was modified

* `eTag()` was added

#### `models.ApiSchemasGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceLoggersGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.CertificatesCreateOrUpdateHeaders` was modified

* `eTag()` was added

#### `models.AccessInformationContract` was modified

* `systemData()` was added

#### `models.ApiContract` was modified

* `mcpProperties()` was added
* `systemData()` was added

#### `models.PortalConfigContract` was modified

* `systemData()` was added

#### `models.PolicyRestrictionsGetHeaders` was modified

* `eTag()` was added

#### `models.LoggersGetHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceProductsUpdateHeaders` was modified

* `eTag()` was added

#### `models.WorkspacesGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceApiOperationsGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.TagsUpdateHeaders` was modified

* `eTag()` was added

#### `models.ApiContract$Update` was modified

* `withMcpProperties(models.McpProperties)` was added

#### `models.ApiVersionSetsUpdateHeaders` was modified

* `eTag()` was added

#### `models.GatewaysCreateOrUpdateHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceLoggersGetHeaders` was modified

* `eTag()` was added

#### `models.TagsGetEntityStateHeaders` was modified

* `eTag()` was added

#### `models.CachesGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.BackendBaseParameters` was modified

* `azureRegion()` was added
* `withAzureRegion(java.lang.String)` was added

#### `models.ApiWikisCreateOrUpdateHeaders` was modified

* `retryAfter()` was added
* `eTag()` was added

#### `models.GraphQLApiResolverPoliciesCreateOrUpdateHeaders` was modified

* `eTag()` was added

#### `models.ProductsGetHeaders` was modified

* `eTag()` was added

#### `models.TenantSettingsContract` was modified

* `systemData()` was added

#### `models.GatewaysGetHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceApiOperationPoliciesGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceApiSchemasGetHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceProductPoliciesGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceDiagnosticsCreateOrUpdateHeaders` was modified

* `eTag()` was added
* `retryAfter()` was added

#### `models.WorkspaceTagsUpdateHeaders` was modified

* `eTag()` was added

#### `models.ApiIssueCommentsGetHeaders` was modified

* `eTag()` was added

#### `models.TagContract` was modified

* `systemData()` was added

#### `models.IdentityProviderContract$Definition` was modified

* `withCertificateId(java.lang.String)` was added

#### `models.WorkspaceBackendsCreateOrUpdateHeaders` was modified

* `retryAfter()` was added
* `eTag()` was added

#### `models.ProductApiLinkContract` was modified

* `systemData()` was added

#### `models.WorkspacePoliciesGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.ApiManagementServiceResource$Definition` was modified

* `withReleaseChannel(models.ReleaseChannel)` was added
* `withZoneRedundant(java.lang.Boolean)` was added

#### `models.WorkspaceApisUpdateHeaders` was modified

* `eTag()` was added

#### `models.GatewayHostnameConfigurationsCreateOrUpdateHeaders` was modified

* `retryAfter()` was added
* `eTag()` was added

#### `models.WorkspaceCertificatesCreateOrUpdateHeaders` was modified

* `eTag()` was added

#### `models.OpenIdConnectProvidersUpdateHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceCertificatesGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.TenantAccessListSecretsHeaders` was modified

* `eTag()` was added

#### `models.GraphQLApiResolversCreateOrUpdateHeaders` was modified

* `eTag()` was added

#### `models.ProductPoliciesCreateOrUpdateHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceSubscriptionsListSecretsHeaders` was modified

* `eTag()` was added

#### `models.ApiTagDescriptionsGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.BackendReconnectContract` was modified

* `systemData()` was added

#### `models.ApiCreateOrUpdateParameter` was modified

* `withMcpProperties(models.McpProperties)` was added
* `mcpProperties()` was added

#### `models.PolicyFragmentContract` was modified

* `systemData()` was added

#### `models.ApisGetHeaders` was modified

* `eTag()` was added

#### `models.ApiReleasesUpdateHeaders` was modified

* `eTag()` was added

#### `models.PortalConfigsGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.TagsCreateOrUpdateHeaders` was modified

* `eTag()` was added

#### `models.ProductContract` was modified

* `systemData()` was added
* `authenticationType()` was added
* `application()` was added

#### `models.GatewayCertificateAuthoritiesGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.PortalConfigsGetHeaders` was modified

* `eTag()` was added

#### `models.GatewayContract$Definition` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.TagsAssignToApiHeaders` was modified

* `eTag()` was added

#### `models.ResolverContract` was modified

* `systemData()` was added

#### `models.GroupsCreateOrUpdateHeaders` was modified

* `retryAfter()` was added
* `eTag()` was added

#### `models.TagsGetByOperationHeaders` was modified

* `eTag()` was added

#### `models.ProductWikisUpdateHeaders` was modified

* `eTag()` was added

#### `models.IdentityProviderUpdateParameters` was modified

* `certificateId()` was added
* `withCertificateId(java.lang.String)` was added

#### `models.PortalDelegationSettings` was modified

* `systemData()` was added

#### `models.ApiIssueAttachmentsCreateOrUpdateHeaders` was modified

* `eTag()` was added
* `retryAfter()` was added

#### `models.ProductPoliciesGetHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceApiPoliciesGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.ApiOperationsGetHeaders` was modified

* `eTag()` was added

#### `models.CachesCreateOrUpdateHeaders` was modified

* `eTag()` was added

#### `models.BackendContract$Update` was modified

* `withAzureRegion(java.lang.String)` was added

#### `models.BackendPool` was modified

* `withSessionAffinity(models.BackendSessionAffinity)` was added
* `withFailureResponse(models.BackendFailureResponse)` was added
* `sessionAffinity()` was added
* `failureResponse()` was added

#### `models.IdentityProvidersGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.CircuitBreakerRule` was modified

* `failureResponse()` was added
* `withFailureResponse(models.BackendFailureResponse)` was added

#### `models.ApiContract$Definition` was modified

* `withMcpProperties(models.McpProperties)` was added

#### `models.SoapApiType` was modified

* `MCP` was added

#### `models.TagApiLinksGetHeaders` was modified

* `eTag()` was added

#### `models.OperationResultContract` was modified

* `systemData()` was added

#### `models.DocumentationContract` was modified

* `systemData()` was added

#### `models.WorkspaceGlobalSchemasGetHeaders` was modified

* `eTag()` was added

#### `models.ProductWikisGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.EmailTemplatesGetHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceTagsGetEntityStateHeaders` was modified

* `eTag()` was added

#### `models.SubscriptionsGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.WorkspacePoliciesGetHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceSubscriptionsGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.ApiReleaseContract` was modified

* `systemData()` was added

#### `models.ApiReleasesGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.ApiVersionSetsGetHeaders` was modified

* `eTag()` was added

#### `models.AuthorizationServerContract` was modified

* `systemData()` was added

#### `models.GlobalSchemaContract` was modified

* `systemData()` was added

#### `models.CertificatesGetHeaders` was modified

* `eTag()` was added

#### `models.ApiGatewayConfigConnections` was modified

* `listByGateway(java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

#### `models.GatewayContract` was modified

* `identity()` was added
* `systemData()` was added

#### `models.ApiVersionSetContract` was modified

* `systemData()` was added

#### `models.NamedValueCreateContract` was modified

* `systemData()` was added

#### `models.WorkspaceTagsGetHeaders` was modified

* `eTag()` was added

#### `models.ProductGroupLinkContract` was modified

* `systemData()` was added

#### `models.WorkspaceProductsCreateOrUpdateHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceGroupsGetHeaders` was modified

* `eTag()` was added

#### `models.WorkspacesUpdateHeaders` was modified

* `eTag()` was added

#### `models.RecipientUserContract` was modified

* `systemData()` was added

#### `models.AuthorizationProviderContract` was modified

* `refreshSecret()` was added
* `systemData()` was added
* `refreshSecretWithResponse(com.azure.core.util.Context)` was added

#### `models.PrivateLinkResourceListResult` was modified

* `nextLink()` was added

#### `models.WorkspaceApiOperationsUpdateHeaders` was modified

* `eTag()` was added

#### `models.CertificatesRefreshSecretHeaders` was modified

* `eTag()` was added

#### `models.AuthorizationsConfirmConsentCodeHeaders` was modified

* `eTag()` was added

#### `models.AuthorizationAccessPoliciesCreateOrUpdateHeaders` was modified

* `eTag()` was added

#### `models.GlobalSchemasGetHeaders` was modified

* `eTag()` was added

#### `models.GraphQLApiResolversGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceApiDiagnosticsUpdateHeaders` was modified

* `eTag()` was added

#### `models.SignUpSettingsGetHeaders` was modified

* `eTag()` was added

#### `models.PoliciesCreateOrUpdateHeaders` was modified

* `eTag()` was added

#### `models.TagsGetEntityStateByProductHeaders` was modified

* `eTag()` was added

#### `models.BackendsCreateOrUpdateHeaders` was modified

* `eTag()` was added
* `retryAfter()` was added

#### `models.CertificatesGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.GraphQLApiResolverPoliciesGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.BackendUpdateParameters` was modified

* `azureRegion()` was added
* `withAzureRegion(java.lang.String)` was added

#### `models.WorkspaceApiDiagnosticsCreateOrUpdateHeaders` was modified

* `retryAfter()` was added
* `eTag()` was added

#### `models.SubscriptionsListSecretsHeaders` was modified

* `eTag()` was added

#### `models.TagsGetByApiHeaders` was modified

* `eTag()` was added

#### `models.ApiGateways` was modified

* `list(java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `listByResourceGroup(java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

#### `models.PoliciesGetHeaders` was modified

* `eTag()` was added

#### `models.ApiDiagnosticsGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.TenantAccessGetHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceApiOperationPoliciesGetHeaders` was modified

* `eTag()` was added

#### `models.ApiSchemasGetHeaders` was modified

* `eTag()` was added

#### `models.PolicyDescriptionContract` was modified

* `systemData()` was added

#### `models.OpenIdConnectProvidersGetHeaders` was modified

* `eTag()` was added

#### `models.PolicyFragmentsGetHeaders` was modified

* `eTag()` was added

#### `models.ContentItemsGetHeaders` was modified

* `eTag()` was added

#### `models.IdentityProviderCreateContract` was modified

* `systemData()` was added
* `certificateId()` was added
* `withCertificateId(java.lang.String)` was added

#### `models.DocumentationsGetEntityTagHeaders` was modified

* `eTag()` was added

#### `models.ProductApiLinksGetHeaders` was modified

* `eTag()` was added

#### `models.WorkspaceApiVersionSetsUpdateHeaders` was modified

* `eTag()` was added

#### `models.ContentTypesCreateOrUpdateHeaders` was modified

* `eTag()` was added

#### `models.NamedValuesGetEntityTagHeaders` was modified

* `eTag()` was added

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


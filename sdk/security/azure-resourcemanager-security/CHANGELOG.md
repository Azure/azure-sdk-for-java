# Release History

## 1.0.0-beta.8 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.7 (2024-05-15)

- Azure Resource Manager Security client library for Java. This package contains Microsoft Azure SDK for Security Management SDK. API spec for Microsoft.Security (Azure Security Center) resource provider. Package tag package-composite-v3. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.DefenderFoDatabasesAwsOfferingArcAutoProvisioningConfiguration` was removed

* `models.DefenderForServersAwsOfferingArcAutoProvisioningConfiguration` was removed

* `models.DefenderForContainersAwsOfferingContainerVulnerabilityAssessment` was removed

* `models.DefenderForDevOpsGitLabOffering` was removed

* `models.InformationProtectionAwsOffering` was removed

* `models.DefenderForServersAwsOfferingVmScannersConfiguration` was removed

* `models.DefenderForDatabasesGcpOfferingArcAutoProvisioningConfiguration` was removed

* `models.ResourceProviders` was removed

* `models.DefenderForContainersAwsOfferingContainerVulnerabilityAssessmentTask` was removed

* `models.DefenderCspmAwsOfferingVmScannersConfiguration` was removed

* `models.DefenderCspmGcpOfferingVmScannersConfiguration` was removed

* `models.DefenderForServersGcpOfferingVmScannersConfiguration` was removed

* `models.DefenderForDevOpsGithubOffering` was removed

* `models.DefenderForServersGcpOfferingArcAutoProvisioningConfiguration` was removed

* `models.DefenderForContainersAwsOfferingKubernetesScubaReader` was removed

* `models.InformationProtectionAwsOfferingInformationProtection` was removed

* `models.DefenderForDevOpsAzureDevOpsOffering` was removed

#### `models.DefenderCspmGcpOfferingVmScanners` was modified

* `enabled()` was removed
* `withConfiguration(models.DefenderCspmGcpOfferingVmScannersConfiguration)` was removed
* `configuration()` was removed

#### `models.DefenderForServersGcpOfferingArcAutoProvisioning` was modified

* `enabled()` was removed
* `configuration()` was removed
* `withConfiguration(models.DefenderForServersGcpOfferingArcAutoProvisioningConfiguration)` was removed

#### `models.DefenderForServersGcpOfferingVmScanners` was modified

* `configuration()` was removed
* `withConfiguration(models.DefenderForServersGcpOfferingVmScannersConfiguration)` was removed
* `enabled()` was removed

#### `SecurityManager` was modified

* `resourceProviders()` was removed

#### `models.DefenderForDatabasesGcpOfferingArcAutoProvisioning` was modified

* `enabled()` was removed
* `configuration()` was removed
* `withConfiguration(models.DefenderForDatabasesGcpOfferingArcAutoProvisioningConfiguration)` was removed

#### `models.DefenderFoDatabasesAwsOfferingArcAutoProvisioning` was modified

* `cloudRoleArn()` was removed
* `enabled()` was removed
* `withConfiguration(models.DefenderFoDatabasesAwsOfferingArcAutoProvisioningConfiguration)` was removed
* `configuration()` was removed

#### `models.DefenderCspmAwsOfferingVmScanners` was modified

* `enabled()` was removed
* `configuration()` was removed
* `withConfiguration(models.DefenderCspmAwsOfferingVmScannersConfiguration)` was removed

#### `models.DefenderForServersAwsOfferingVmScanners` was modified

* `withConfiguration(models.DefenderForServersAwsOfferingVmScannersConfiguration)` was removed
* `enabled()` was removed
* `configuration()` was removed

#### `models.DefenderForContainersAwsOffering` was modified

* `autoProvisioning()` was removed
* `kubernetesScubaReader()` was removed
* `containerVulnerabilityAssessment()` was removed
* `enableContainerVulnerabilityAssessment()` was removed
* `withScubaExternalId(java.lang.String)` was removed
* `containerVulnerabilityAssessmentTask()` was removed
* `withAutoProvisioning(java.lang.Boolean)` was removed
* `withContainerVulnerabilityAssessmentTask(models.DefenderForContainersAwsOfferingContainerVulnerabilityAssessmentTask)` was removed
* `withContainerVulnerabilityAssessment(models.DefenderForContainersAwsOfferingContainerVulnerabilityAssessment)` was removed
* `scubaExternalId()` was removed
* `withKubernetesScubaReader(models.DefenderForContainersAwsOfferingKubernetesScubaReader)` was removed
* `withEnableContainerVulnerabilityAssessment(java.lang.Boolean)` was removed

#### `models.DefenderForContainersGcpOffering` was modified

* `defenderAgentAutoProvisioningFlag()` was removed
* `withDefenderAgentAutoProvisioningFlag(java.lang.Boolean)` was removed
* `policyAgentAutoProvisioningFlag()` was removed
* `withPolicyAgentAutoProvisioningFlag(java.lang.Boolean)` was removed
* `auditLogsAutoProvisioningFlag()` was removed
* `withAuditLogsAutoProvisioningFlag(java.lang.Boolean)` was removed

#### `models.DefenderForServersAwsOfferingArcAutoProvisioning` was modified

* `cloudRoleArn()` was removed
* `withConfiguration(models.DefenderForServersAwsOfferingArcAutoProvisioningConfiguration)` was removed
* `configuration()` was removed
* `enabled()` was removed

### Features Added

* `models.DefenderForContainersGcpOfferingVmScanners` was added

* `models.ArcAutoProvisioning` was added

* `models.ArcAutoProvisioningAws` was added

* `models.ArcAutoProvisioningConfiguration` was added

* `models.VmScannersGcp` was added

* `models.VmScannersBaseConfiguration` was added

* `models.VmScannersBase` was added

* `models.ArcAutoProvisioningGcp` was added

* `models.VmScannersAws` was added

* `models.DefenderForContainersAwsOfferingKubernetesDataCollection` was added

* `models.DevOpsCapability` was added

* `models.DefenderForContainersAwsOfferingVmScanners` was added

#### `models.AdditionalData` was modified

* `assessedResourceType()` was added

#### `models.NotificationsSourceAlert` was modified

* `sourceType()` was added

#### `models.GcpOrganizationalDataOrganization` was modified

* `organizationMembershipType()` was added

#### `models.DenylistCustomAlertRule` was modified

* `ruleType()` was added

#### `models.AzureResourceIdentifier` was modified

* `type()` was added

#### `models.DirectMethodInvokesNotInAllowedRange` was modified

* `ruleType()` was added

#### `models.AuthenticationDetailsProperties` was modified

* `authenticationType()` was added

#### `models.DefenderForServersGcpOffering` was modified

* `offeringType()` was added

#### `models.GcpProjectEnvironmentData` was modified

* `environmentType()` was added

#### `models.LogAnalyticsIdentifier` was modified

* `type()` was added

#### `models.AwsOrganizationalDataMaster` was modified

* `organizationMembershipType()` was added

#### `models.GcpOrganizationalDataMember` was modified

* `organizationMembershipType()` was added

#### `models.ThresholdCustomAlertRule` was modified

* `ruleType()` was added

#### `models.SensitivitySettings` was modified

* `createOrUpdate(models.UpdateSensitivitySettingsRequest)` was added
* `getWithResponse(com.azure.core.util.Context)` was added
* `get()` was added
* `createOrUpdateWithResponse(models.UpdateSensitivitySettingsRequest,com.azure.core.util.Context)` was added

#### `models.ResourceDetails` was modified

* `source()` was added

#### `models.ResourceIdentifier` was modified

* `type()` was added

#### `models.ConnectionToIpNotAllowed` was modified

* `ruleType()` was added

#### `models.GitlabScopeEnvironmentData` was modified

* `environmentType()` was added

#### `models.AwsOrganizationalDataMember` was modified

* `organizationMembershipType()` was added

#### `models.DevOpsConfigurationProperties` was modified

* `capabilities()` was added

#### `models.AzureDevOpsScopeEnvironmentData` was modified

* `environmentType()` was added

#### `models.FailedLocalLoginsNotInAllowedRange` was modified

* `ruleType()` was added

#### `models.NotificationsSourceAttackPath` was modified

* `sourceType()` was added

#### `models.FileUploadsNotInAllowedRange` was modified

* `ruleType()` was added

#### `models.MqttC2DMessagesNotInAllowedRange` was modified

* `ruleType()` was added

#### `models.ExternalSecuritySolution` was modified

* `kind()` was added

#### `models.ConnectionFromIpNotAllowed` was modified

* `ruleType()` was added

#### `models.DefenderCspmGcpOfferingVmScanners` was modified

* `withEnabled(java.lang.Boolean)` was added
* `withConfiguration(models.VmScannersBaseConfiguration)` was added

#### `models.AutomationActionWorkspace` was modified

* `actionType()` was added

#### `models.AmqpC2DRejectedMessagesNotInAllowedRange` was modified

* `ruleType()` was added

#### `models.CspmMonitorAzureDevOpsOffering` was modified

* `offeringType()` was added

#### `models.LocalUserNotAllowed` was modified

* `ruleType()` was added

#### `models.EnvironmentData` was modified

* `environmentType()` was added

#### `models.DefenderForServersAwsOffering` was modified

* `offeringType()` was added

#### `models.CspmMonitorGithubOffering` was modified

* `offeringType()` was added

#### `models.ContainerRegistryVulnerabilityProperties` was modified

* `assessedResourceType()` was added

#### `models.MqttC2DRejectedMessagesNotInAllowedRange` was modified

* `ruleType()` was added

#### `models.AwAssumeRoleAuthenticationDetailsProperties` was modified

* `authenticationType()` was added

#### `models.OnPremiseSqlResourceDetails` was modified

* `source()` was added

#### `models.AutomationAction` was modified

* `actionType()` was added

#### `models.CustomAlertRule` was modified

* `ruleType()` was added

#### `models.AzureServersSetting` was modified

* `kind()` was added

#### `models.AtaExternalSecuritySolution` was modified

* `kind()` was added

#### `models.AutomationActionEventHub` was modified

* `actionType()` was added

#### `models.AlertSyncSettings` was modified

* `kind()` was added

#### `models.DefenderForServersGcpOfferingArcAutoProvisioning` was modified

* `withEnabled(java.lang.Boolean)` was added
* `withConfiguration(models.ArcAutoProvisioningConfiguration)` was added

#### `models.TwinUpdatesNotInAllowedRange` was modified

* `ruleType()` was added

#### `models.DefenderForServersGcpOfferingVmScanners` was modified

* `withEnabled(java.lang.Boolean)` was added
* `withConfiguration(models.VmScannersBaseConfiguration)` was added

#### `models.CefExternalSecuritySolution` was modified

* `kind()` was added

#### `models.SqlServerVulnerabilityProperties` was modified

* `assessedResourceType()` was added

#### `models.AwsOrganizationalData` was modified

* `organizationMembershipType()` was added

#### `models.AwsEnvironmentData` was modified

* `environmentType()` was added

#### `models.AmqpC2DMessagesNotInAllowedRange` was modified

* `ruleType()` was added

#### `models.AlertSimulatorRequestProperties` was modified

* `kind()` was added

#### `models.HttpC2DRejectedMessagesNotInAllowedRange` was modified

* `ruleType()` was added

#### `models.DefenderForDatabasesGcpOfferingArcAutoProvisioning` was modified

* `withEnabled(java.lang.Boolean)` was added
* `withConfiguration(models.ArcAutoProvisioningConfiguration)` was added

#### `models.GcpOrganizationalData` was modified

* `organizationMembershipType()` was added

#### `models.DefenderFoDatabasesAwsOfferingArcAutoProvisioning` was modified

* `withEnabled(java.lang.Boolean)` was added
* `withConfiguration(models.ArcAutoProvisioningConfiguration)` was added
* `withCloudRoleArn(java.lang.String)` was added

#### `models.AmqpD2CMessagesNotInAllowedRange` was modified

* `ruleType()` was added

#### `models.CloudOffering` was modified

* `offeringType()` was added

#### `models.DataExportSettings` was modified

* `kind()` was added

#### `models.DefenderCspmAwsOfferingVmScanners` was modified

* `withEnabled(java.lang.Boolean)` was added
* `withCloudRoleArn(java.lang.String)` was added
* `withConfiguration(models.VmScannersBaseConfiguration)` was added

#### `models.ListCustomAlertRule` was modified

* `ruleType()` was added

#### `models.MqttD2CMessagesNotInAllowedRange` was modified

* `ruleType()` was added

#### `models.ActiveConnectionsNotInAllowedRange` was modified

* `ruleType()` was added

#### `models.AadExternalSecuritySolution` was modified

* `kind()` was added

#### `models.GithubScopeEnvironmentData` was modified

* `environmentType()` was added

#### `models.QueuePurgesNotInAllowedRange` was modified

* `ruleType()` was added

#### `models.DefenderForServersAwsOfferingVmScanners` was modified

* `withConfiguration(models.VmScannersBaseConfiguration)` was added
* `withCloudRoleArn(java.lang.String)` was added
* `withEnabled(java.lang.Boolean)` was added

#### `models.DefenderFoDatabasesAwsOffering` was modified

* `offeringType()` was added

#### `models.AutomationActionLogicApp` was modified

* `actionType()` was added

#### `models.HttpC2DMessagesNotInAllowedRange` was modified

* `ruleType()` was added

#### `models.CspmMonitorGcpOffering` was modified

* `offeringType()` was added

#### `models.AzureResourceDetails` was modified

* `source()` was added

#### `models.CspmMonitorAwsOffering` was modified

* `offeringType()` was added

#### `models.DefenderForDatabasesGcpOffering` was modified

* `offeringType()` was added

#### `models.AllowlistCustomAlertRule` was modified

* `ruleType()` was added

#### `models.ServerVulnerabilityAssessmentsSetting` was modified

* `kind()` was added

#### `models.OnPremiseResourceDetails` was modified

* `source()` was added

#### `models.DefenderForContainersAwsOffering` was modified

* `withEnableDefenderAgentAutoProvisioning(java.lang.Boolean)` was added
* `offeringType()` was added
* `withEnablePolicyAgentAutoProvisioning(java.lang.Boolean)` was added
* `kubernetesDataCollection()` was added
* `enableAuditLogsAutoProvisioning()` was added
* `withVmScanners(models.DefenderForContainersAwsOfferingVmScanners)` was added
* `withKubernetesDataCollection(models.DefenderForContainersAwsOfferingKubernetesDataCollection)` was added
* `dataCollectionExternalId()` was added
* `enableDefenderAgentAutoProvisioning()` was added
* `withEnableAuditLogsAutoProvisioning(java.lang.Boolean)` was added
* `withDataCollectionExternalId(java.lang.String)` was added
* `enablePolicyAgentAutoProvisioning()` was added
* `vmScanners()` was added

#### `models.HttpD2CMessagesNotInAllowedRange` was modified

* `ruleType()` was added

#### `models.UnauthorizedOperationsNotInAllowedRange` was modified

* `ruleType()` was added

#### `models.Setting` was modified

* `kind()` was added

#### `models.AlertSimulatorBundlesRequestProperties` was modified

* `kind()` was added

#### `models.DefenderForContainersGcpOffering` was modified

* `enableAuditLogsAutoProvisioning()` was added
* `offeringType()` was added
* `enablePolicyAgentAutoProvisioning()` was added
* `withEnablePolicyAgentAutoProvisioning(java.lang.Boolean)` was added
* `withEnableAuditLogsAutoProvisioning(java.lang.Boolean)` was added
* `enableDefenderAgentAutoProvisioning()` was added
* `withVmScanners(models.DefenderForContainersGcpOfferingVmScanners)` was added
* `withEnableDefenderAgentAutoProvisioning(java.lang.Boolean)` was added
* `vmScanners()` was added

#### `models.AwsCredsAuthenticationDetailsProperties` was modified

* `authenticationType()` was added

#### `models.DefenderCspmAwsOffering` was modified

* `offeringType()` was added

#### `models.GcpCredentialsDetailsProperties` was modified

* `authenticationType()` was added

#### `models.ServerVulnerabilityProperties` was modified

* `assessedResourceType()` was added

#### `models.CspmMonitorGitLabOffering` was modified

* `offeringType()` was added

#### `models.DefenderCspmGcpOffering` was modified

* `offeringType()` was added

#### `models.ProcessNotAllowed` was modified

* `ruleType()` was added

#### `models.DefenderForServersAwsOfferingArcAutoProvisioning` was modified

* `withEnabled(java.lang.Boolean)` was added
* `withConfiguration(models.ArcAutoProvisioningConfiguration)` was added
* `withCloudRoleArn(java.lang.String)` was added

#### `models.TimeWindowCustomAlertRule` was modified

* `ruleType()` was added

#### `models.NotificationsSource` was modified

* `sourceType()` was added

## 1.0.0-beta.6 (2024-03-14)

- Azure Resource Manager Security client library for Java. This package contains Microsoft Azure SDK for Security Management SDK. API spec for Microsoft.Security (Azure Security Center) resource provider. Package tag package-composite-v3. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ApiCollectionOffboardings` was removed

* `models.IngestionSettingList` was removed

* `models.IngestionConnectionString` was removed

* `models.Roles` was removed

* `models.ScopeName` was removed

* `models.IngestionSettings` was removed

* `models.IngestionSetting` was removed

* `models.IngestionSetting$Definition` was removed

* `models.ApiCollectionResponse` was removed

* `models.SecurityContactPropertiesAlertNotifications` was removed

* `models.IngestionSettingToken` was removed

* `models.ApiCollectionOnboardings` was removed

* `models.HealthReportOperations` was removed

* `models.ApiCollectionResponseList` was removed

* `models.IngestionSetting$DefinitionStages` was removed

* `models.ConnectionStrings` was removed

#### `models.Extension` was modified

* `models.OperationStatus operationStatus()` -> `models.OperationStatusAutoGenerated operationStatus()`

#### `models.SecurityContacts` was modified

* `delete(java.lang.String)` was removed
* `get(java.lang.String)` was removed
* `define(java.lang.String)` was removed
* `deleteWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `getWithResponse(java.lang.String,com.azure.core.util.Context)` was removed

#### `SecurityManager` was modified

* `apiCollectionOffboardings()` was removed
* `apiCollectionOnboardings()` was removed
* `ingestionSettings()` was removed
* `healthReportOperations()` was removed

#### `models.ApiCollections` was modified

* `list(java.lang.String,java.lang.String)` was removed
* `list(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `get(java.lang.String,java.lang.String,java.lang.String)` was removed
* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.SecurityContact$Definition` was modified

* `withAlertNotifications(models.SecurityContactPropertiesAlertNotifications)` was removed

#### `models.Pricings` was modified

* `getWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `get(java.lang.String)` was removed
* `updateWithResponse(java.lang.String,fluent.models.PricingInner,com.azure.core.util.Context)` was removed
* `listWithResponse(com.azure.core.util.Context)` was removed
* `update(java.lang.String,fluent.models.PricingInner)` was removed
* `list()` was removed

#### `models.Settings` was modified

* `getWithResponse(models.SettingName,com.azure.core.util.Context)` was removed
* `updateWithResponse(models.SettingName,fluent.models.SettingInner,com.azure.core.util.Context)` was removed
* `get(models.SettingName)` was removed
* `update(models.SettingName,fluent.models.SettingInner)` was removed

#### `models.HealthDataClassification` was modified

* `models.ScopeName scope()` -> `java.lang.String scope()`
* `withScope(models.ScopeName)` was removed

#### `models.OperationStatus` was modified

* `withCode(models.Code)` was removed
* `models.Code code()` -> `java.lang.String code()`

#### `models.SecurityContact` was modified

* `alertNotifications()` was removed

### Features Added

* `models.OnboardingState` was added

* `models.NotificationsSourceAlert` was added

* `models.ServerVulnerabilityAssessmentsAzureSettingSelectedProvider` was added

* `models.DevOpsProvisioningState` was added

* `models.AzureDevOpsRepository$UpdateStages` was added

* `models.AzureDevOpsProject$UpdateStages` was added

* `models.DefenderForStorageSetting$Definition` was added

* `models.AzureDevOpsRepository$Definition` was added

* `models.DefenderForContainersAwsOfferingMdcContainersImageAssessment` was added

* `models.DefenderForContainersGcpOfferingMdcContainersAgentlessDiscoveryK8S` was added

* `models.ActionableRemediationState` was added

* `models.GitHubRepositoryProperties` was added

* `models.SensitivitySettings` was added

* `models.SecurityContactRole` was added

* `models.AzureDevOpsProject$Update` was added

* `models.DefenderCspmGcpOfferingMdcContainersImageAssessment` was added

* `models.AzureDevOpsProjects` was added

* `models.DevOpsConfigurationProperties` was added

* `models.AzureDevOpsOrgProperties` was added

* `models.GitLabProject` was added

* `models.NotificationsSourceAttackPath` was added

* `models.GitLabProjects` was added

* `models.GitHubOwnerProperties` was added

* `models.Label` was added

* `models.DefenderForStorageSetting$DefinitionStages` was added

* `models.DefenderCspmGcpOfferingVmScanners` was added

* `models.ServerVulnerabilityAssessmentsSettingKindName` was added

* `models.AzureDevOpsRepository$DefinitionStages` was added

* `models.AzureDevOpsOrgListResponse` was added

* `models.AnnotateDefaultBranchState` was added

* `models.ActionableRemediation` was added

* `models.OperationStatusResult` was added

* `models.GitLabSubgroups` was added

* `models.GetSensitivitySettingsListResponse` was added

* `models.AzureDevOpsRepository$Update` was added

* `models.Enforce` was added

* `models.AzureDevOpsRepos` was added

* `models.GitLabGroup` was added

* `models.Inherited` was added

* `models.AzureDevOpsRepositoryListResponse` was added

* `models.GitHubRepository` was added

* `models.AzureServersSetting` was added

* `models.ServerVulnerabilityAssessmentsSettings` was added

* `models.SecurityContactName` was added

* `models.ApiCollectionList` was added

* `models.DefenderCspmGcpOfferingMdcContainersAgentlessDiscoveryK8S` was added

* `models.GitHubOwners` was added

* `models.SourceType` was added

* `models.DevOpsOperationResults` was added

* `models.DevOpsConfigurationListResponse` was added

* `models.ServerVulnerabilityAssessmentsSettingsList` was added

* `models.GetSensitivitySettingsResponseProperties` was added

* `models.GitLabProjectListResponse` was added

* `models.GitLabGroupProperties` was added

* `models.ResourcesCoverageStatus` was added

* `models.ServerVulnerabilityAssessmentsSettingKind` was added

* `models.DefenderForStorages` was added

* `models.GitHubOwner` was added

* `models.MipIntegrationStatus` was added

* `models.AzureDevOpsProject` was added

* `models.AzureDevOpsOrg$Definition` was added

* `models.DefenderForContainersGcpOfferingMdcContainersImageAssessment` was added

* `models.ResourceProviders` was added

* `models.AzureDevOpsProject$Definition` was added

* `models.GitLabGroupListResponse` was added

* `models.AutomationUpdateModel` was added

* `models.DefenderCspmAwsOfferingCiemOidc` was added

* `models.UpdateSensitivitySettingsRequest` was added

* `models.DefenderCspmGcpOfferingVmScannersConfiguration` was added

* `models.DevOpsConfigurations` was added

* `models.DefenderCspmAwsOfferingCiem` was added

* `models.AzureDevOpsOrg$UpdateStages` was added

* `models.AzureDevOpsOrg` was added

* `models.DefenderCspmGcpOfferingDataSensitivityDiscovery` was added

* `models.AzureDevOpsRepositoryProperties` was added

* `models.Authorization` was added

* `models.AzureDevOpsProjectListResponse` was added

* `models.GitHubRepositoryListResponse` was added

* `models.AzureDevOpsRepository` was added

* `models.GitHubRepos` was added

* `models.CategoryConfiguration` was added

* `models.DefenderCspmAwsOfferingCiemDiscovery` was added

* `models.SettingNameAutoGenerated` was added

* `models.RuleCategory` was added

* `models.BuiltInInfoType` was added

* `models.GitLabProjectProperties` was added

* `models.ServerVulnerabilityAssessmentsSetting` was added

* `models.DefenderForStorageSetting` was added

* `models.GitLabGroups` was added

* `models.OperationStatusAutoGenerated` was added

* `models.DefenderCspmGcpOfferingCiemDiscovery` was added

* `models.ApiCollection` was added

* `models.InfoType` was added

* `models.AutoDiscovery` was added

* `models.GitHubOwnerListResponse` was added

* `models.InheritFromParentState` was added

* `models.GetSensitivitySettingsResponse` was added

* `models.MinimalRiskLevel` was added

* `models.AzureDevOpsOrg$Update` was added

* `models.GetSensitivitySettingsResponsePropertiesMipInformation` was added

* `models.TargetBranchConfiguration` was added

* `models.AzureDevOpsProjectProperties` was added

* `models.DefenderCspmAwsOfferingMdcContainersAgentlessDiscoveryK8S` was added

* `models.DefenderCspmAwsOfferingMdcContainersImageAssessment` was added

* `models.AzureDevOpsOrg$DefinitionStages` was added

* `models.DevOpsConfiguration` was added

* `models.AzureDevOpsProject$DefinitionStages` was added

* `models.AzureDevOpsOrgs` was added

* `models.DefenderForContainersAwsOfferingMdcContainersAgentlessDiscoveryK8S` was added

* `models.NotificationsSource` was added

#### `models.GcpProjectEnvironmentData` was modified

* `scanInterval()` was added
* `withScanInterval(java.lang.Long)` was added

#### `models.SecurityContactList` was modified

* `withValue(java.util.List)` was added

#### `models.Pricing` was modified

* `resourcesCoverageStatus()` was added
* `inherited()` was added
* `enforce()` was added
* `inheritedFrom()` was added

#### `models.HealthReport` was modified

* `affectedDefendersSubPlans()` was added
* `reportAdditionalData()` was added

#### `models.HealthReports` was modified

* `get(java.lang.String,java.lang.String)` was added
* `getWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.SecurityContacts` was modified

* `define(models.SecurityContactName)` was added
* `getWithResponse(models.SecurityContactName,com.azure.core.util.Context)` was added
* `get(models.SecurityContactName)` was added
* `deleteWithResponse(models.SecurityContactName,com.azure.core.util.Context)` was added
* `delete(models.SecurityContactName)` was added

#### `models.AutomationActionEventHub` was modified

* `isTrustedServiceEnabled()` was added
* `withIsTrustedServiceEnabled(java.lang.Boolean)` was added

#### `models.AwsEnvironmentData` was modified

* `scanInterval()` was added
* `withScanInterval(java.lang.Long)` was added

#### `SecurityManager` was modified

* `serverVulnerabilityAssessmentsSettings()` was added
* `azureDevOpsOrgs()` was added
* `devOpsOperationResults()` was added
* `azureDevOpsProjects()` was added
* `devOpsConfigurations()` was added
* `gitLabProjects()` was added
* `gitHubRepos()` was added
* `azureDevOpsRepos()` was added
* `gitLabSubgroups()` was added
* `resourceProviders()` was added
* `sensitivitySettings()` was added
* `defenderForStorages()` was added
* `gitHubOwners()` was added
* `gitLabGroups()` was added

#### `models.ApiCollections` was modified

* `onboardAzureApiManagementApi(java.lang.String,java.lang.String,java.lang.String)` was added
* `listByResourceGroup(java.lang.String)` was added
* `list(com.azure.core.util.Context)` was added
* `getByAzureApiManagementService(java.lang.String,java.lang.String,java.lang.String)` was added
* `offboardAzureApiManagementApi(java.lang.String,java.lang.String,java.lang.String)` was added
* `offboardAzureApiManagementApiWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `onboardAzureApiManagementApi(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listByAzureApiManagementService(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listByAzureApiManagementService(java.lang.String,java.lang.String)` was added
* `list()` was added
* `getByAzureApiManagementServiceWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listByResourceGroup(java.lang.String,com.azure.core.util.Context)` was added

#### `models.StatusAutoGenerated` was modified

* `lastScannedDate()` was added
* `reason()` was added

#### `models.SecurityContact$Definition` was modified

* `withNotificationsSources(java.util.List)` was added
* `withIsEnabled(java.lang.Boolean)` was added

#### `models.DefenderForContainersAwsOffering` was modified

* `mdcContainersImageAssessment()` was added
* `withMdcContainersAgentlessDiscoveryK8S(models.DefenderForContainersAwsOfferingMdcContainersAgentlessDiscoveryK8S)` was added
* `withMdcContainersImageAssessment(models.DefenderForContainersAwsOfferingMdcContainersImageAssessment)` was added
* `mdcContainersAgentlessDiscoveryK8S()` was added

#### `models.Pricings` was modified

* `listWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `deleteByResourceGroup(java.lang.String,java.lang.String)` was added
* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `list(java.lang.String)` was added
* `update(java.lang.String,java.lang.String,fluent.models.PricingInner)` was added
* `get(java.lang.String,java.lang.String)` was added
* `getWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `updateWithResponse(java.lang.String,java.lang.String,fluent.models.PricingInner,com.azure.core.util.Context)` was added

#### `models.Settings` was modified

* `get(models.SettingNameAutoGenerated)` was added
* `getWithResponse(models.SettingNameAutoGenerated,com.azure.core.util.Context)` was added
* `update(models.SettingNameAutoGenerated,fluent.models.SettingInner)` was added
* `updateWithResponse(models.SettingNameAutoGenerated,fluent.models.SettingInner,com.azure.core.util.Context)` was added

#### `models.DefenderForContainersGcpOffering` was modified

* `withMdcContainersImageAssessment(models.DefenderForContainersGcpOfferingMdcContainersImageAssessment)` was added
* `mdcContainersAgentlessDiscoveryK8S()` was added
* `withMdcContainersAgentlessDiscoveryK8S(models.DefenderForContainersGcpOfferingMdcContainersAgentlessDiscoveryK8S)` was added
* `mdcContainersImageAssessment()` was added

#### `models.DefenderCspmAwsOffering` was modified

* `mdcContainersAgentlessDiscoveryK8S()` was added
* `mdcContainersImageAssessment()` was added
* `withCiem(models.DefenderCspmAwsOfferingCiem)` was added
* `ciem()` was added
* `withMdcContainersImageAssessment(models.DefenderCspmAwsOfferingMdcContainersImageAssessment)` was added
* `withMdcContainersAgentlessDiscoveryK8S(models.DefenderCspmAwsOfferingMdcContainersAgentlessDiscoveryK8S)` was added

#### `models.HealthDataClassification` was modified

* `withScope(java.lang.String)` was added

#### `models.DefenderCspmGcpOffering` was modified

* `ciemDiscovery()` was added
* `mdcContainersAgentlessDiscoveryK8S()` was added
* `withVmScanners(models.DefenderCspmGcpOfferingVmScanners)` was added
* `withCiemDiscovery(models.DefenderCspmGcpOfferingCiemDiscovery)` was added
* `withMdcContainersAgentlessDiscoveryK8S(models.DefenderCspmGcpOfferingMdcContainersAgentlessDiscoveryK8S)` was added
* `withDataSensitivityDiscovery(models.DefenderCspmGcpOfferingDataSensitivityDiscovery)` was added
* `vmScanners()` was added
* `withMdcContainersImageAssessment(models.DefenderCspmGcpOfferingMdcContainersImageAssessment)` was added
* `mdcContainersImageAssessment()` was added
* `dataSensitivityDiscovery()` was added

#### `models.OperationStatus` was modified

* `withCode(java.lang.String)` was added

#### `models.SecurityContact` was modified

* `notificationsSources()` was added
* `isEnabled()` was added

## 1.0.0-beta.5 (2023-04-18)

- Azure Resource Manager Security client library for Java. This package contains Microsoft Azure SDK for Security Management SDK. API spec for Microsoft.Security (Azure Security Center) resource provider. Package tag package-composite-v3. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.DefenderFoDatabasesAwsOfferingArcAutoProvisioningConfiguration` was added

* `models.Extension` was added

* `models.Code` was added

* `models.ResourceIdentityType` was added

* `models.DefenderFoDatabasesAwsOfferingDatabasesDspm` was added

* `models.GitlabScopeEnvironmentData` was added

* `models.DefenderForServersAwsOfferingArcAutoProvisioningConfiguration` was added

* `models.SecurityOperators` was added

* `models.SecurityOperatorList` was added

* `models.DefenderForDevOpsGitLabOffering` was added

* `models.DefenderCspmAwsOfferingDatabasesDspm` was added

* `models.DefenderForServersGcpOfferingVmScanners` was added

* `models.DefenderForDatabasesGcpOfferingArcAutoProvisioningConfiguration` was added

* `models.IsEnabled` was added

* `models.Identity` was added

* `models.DefenderForServersGcpOfferingVmScannersConfiguration` was added

* `models.DefenderForServersGcpOfferingArcAutoProvisioningConfiguration` was added

* `models.SecurityOperator` was added

* `models.DefenderCspmAwsOfferingDataSensitivityDiscovery` was added

* `models.CspmMonitorGitLabOffering` was added

* `models.OperationStatus` was added

#### `models.GcpOrganizationalDataOrganization` was modified

* `organizationName()` was added

#### `models.DefenderForServersGcpOffering` was modified

* `vmScanners()` was added
* `withVmScanners(models.DefenderForServersGcpOfferingVmScanners)` was added

#### `models.Pricing` was modified

* `extensions()` was added
* `enablementTime()` was added

#### `models.DefenderForServersGcpOfferingArcAutoProvisioning` was modified

* `configuration()` was added
* `withConfiguration(models.DefenderForServersGcpOfferingArcAutoProvisioningConfiguration)` was added

#### `models.AwsEnvironmentData` was modified

* `withRegions(java.util.List)` was added
* `regions()` was added
* `accountName()` was added

#### `SecurityManager` was modified

* `securityOperators()` was added

#### `models.DefenderForDatabasesGcpOfferingArcAutoProvisioning` was modified

* `configuration()` was added
* `withConfiguration(models.DefenderForDatabasesGcpOfferingArcAutoProvisioningConfiguration)` was added

#### `models.DefenderFoDatabasesAwsOfferingArcAutoProvisioning` was modified

* `withConfiguration(models.DefenderFoDatabasesAwsOfferingArcAutoProvisioningConfiguration)` was added
* `configuration()` was added

#### `models.DefenderFoDatabasesAwsOffering` was modified

* `withDatabasesDspm(models.DefenderFoDatabasesAwsOfferingDatabasesDspm)` was added
* `databasesDspm()` was added

#### `models.GcpProjectDetails` was modified

* `projectName()` was added

#### `models.DefenderCspmAwsOffering` was modified

* `dataSensitivityDiscovery()` was added
* `withDataSensitivityDiscovery(models.DefenderCspmAwsOfferingDataSensitivityDiscovery)` was added
* `withDatabasesDspm(models.DefenderCspmAwsOfferingDatabasesDspm)` was added
* `databasesDspm()` was added

#### `models.DefenderForServersAwsOfferingArcAutoProvisioning` was modified

* `withConfiguration(models.DefenderForServersAwsOfferingArcAutoProvisioningConfiguration)` was added
* `configuration()` was added

## 1.0.0-beta.4 (2023-03-21)

- Azure Resource Manager Security client library for Java. This package contains Microsoft Azure SDK for Security Management SDK. API spec for Microsoft.Security (Azure Security Center) resource provider. Package tag package-composite-v3. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.SecurityConnectorGovernanceRulesExecuteStatus` was removed

* `models.SecurityConnectorGovernanceRules` was removed

* `models.SecurityConnectorGovernanceRulesOperations` was removed

* `models.SubscriptionGovernanceRulesExecuteStatus` was removed

* `models.ExecuteRuleStatus` was removed

* `models.GovernanceRulesOperations` was removed

#### `SecurityManager` was modified

* `securityConnectorGovernanceRules()` was removed
* `securityConnectorGovernanceRulesOperations()` was removed
* `governanceRulesOperations()` was removed
* `securityConnectorGovernanceRulesExecuteStatus()` was removed
* `subscriptionGovernanceRulesExecuteStatus()` was removed

#### `models.GovernanceRule` was modified

* `ruleIdExecuteSingleSubscription()` was removed
* `ruleIdExecuteSingleSubscription(models.ExecuteGovernanceRuleParams,com.azure.core.util.Context)` was removed

#### `models.AutomationActionLogicApp` was modified

* `withUri(java.net.URL)` was removed
* `java.net.URL uri()` -> `java.lang.String uri()`

#### `models.GovernanceRules` was modified

* `list(com.azure.core.util.Context)` was removed
* `list()` was removed

### Features Added

* `models.GovernanceRulesOperationResultsHeaders` was added

* `models.OperationResultAutoGenerated` was added

* `models.OperationResult` was added

* `models.StatusName` was added

* `models.HealthReport` was added

* `models.ScopeName` was added

* `models.HealthReports` was added

* `models.Issue` was added

* `models.ResourceDetailsAutoGenerated` was added

* `models.GovernanceRuleMetadata` was added

* `models.StatusAutoGenerated` was added

* `models.HealthReportsList` was added

* `models.HealthReportOperations` was added

* `models.HealthDataClassification` was added

* `models.GovernanceRulesOperationResultsResponse` was added

* `models.EnvironmentDetails` was added

#### `models.GovernanceRule$Update` was modified

* `withMetadata(models.GovernanceRuleMetadata)` was added
* `withIncludeMemberScopes(java.lang.Boolean)` was added
* `withExcludedScopes(java.util.List)` was added

#### `models.GovernanceRule$Definition` was modified

* `withIncludeMemberScopes(java.lang.Boolean)` was added
* `withMetadata(models.GovernanceRuleMetadata)` was added
* `withExcludedScopes(java.util.List)` was added
* `withExistingScope(java.lang.String)` was added

#### `SecurityManager` was modified

* `healthReportOperations()` was added
* `healthReports()` was added

#### `models.GovernanceRule` was modified

* `execute()` was added
* `execute(models.ExecuteGovernanceRuleParams,com.azure.core.util.Context)` was added
* `metadata()` was added
* `tenantId()` was added
* `includeMemberScopes()` was added
* `excludedScopes()` was added

#### `models.AutomationActionLogicApp` was modified

* `withUri(java.lang.String)` was added

#### `models.ScanProperties` was modified

* `lastScanTime()` was added
* `withLastScanTime(java.time.OffsetDateTime)` was added

#### `models.GovernanceRules` was modified

* `getWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `operationResults(java.lang.String,java.lang.String,java.lang.String)` was added
* `list(java.lang.String)` was added
* `operationResultsWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `deleteByResourceGroup(java.lang.String,java.lang.String)` was added
* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `get(java.lang.String,java.lang.String)` was added
* `getById(java.lang.String)` was added
* `define(java.lang.String)` was added
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `list(java.lang.String,com.azure.core.util.Context)` was added
* `execute(java.lang.String,java.lang.String)` was added
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `execute(java.lang.String,java.lang.String,models.ExecuteGovernanceRuleParams,com.azure.core.util.Context)` was added
* `deleteById(java.lang.String)` was added

## 1.0.0-beta.3 (2022-11-18)

- Azure Resource Manager Security client library for Java. This package contains Microsoft Azure SDK for Security Management SDK. API spec for Microsoft.Security (Azure Security Center) resource provider. Package tag package-composite-v3. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.GovernanceRule` was modified

* `ruleIdExecuteSingleSubscription(models.ExecuteGovernanceRuleParams)` was removed

#### `models.SecurityConnectors` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Automations` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.AutomationActionLogicApp` was modified

* `java.lang.String uri()` -> `java.net.URL uri()`
* `withUri(java.lang.String)` was removed

#### `models.Assessments` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.CustomAssessmentAutomations` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.CustomEntityStoreAssignments` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.DeviceSecurityGroups` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.AdaptiveApplicationControls` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.GovernanceRulesOperations` was modified

* `ruleIdExecuteSingleSecurityConnector(java.lang.String,java.lang.String,java.lang.String,models.ExecuteGovernanceRuleParams)` was removed
* `ruleIdExecuteSingleSubscription(java.lang.String,models.ExecuteGovernanceRuleParams)` was removed

#### `models.IotSecuritySolutions` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

* `models.ApiCollectionOffboardings` was added

* `models.ApiCollections` was added

* `models.ApiCollectionResponse` was added

* `models.ApiCollectionOnboardings` was added

* `models.ApiCollectionResponseList` was added

#### `models.SecurityConnectors` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Automations` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.AutomationActionLogicApp` was modified

* `withUri(java.net.URL)` was added

#### `models.Assessments` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.CustomAssessmentAutomations` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.CustomEntityStoreAssignments` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.DeviceSecurityGroups` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.AdaptiveApplicationControls` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.IotSecuritySolutions` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `SecurityManager` was modified

* `apiCollectionOffboardings()` was added
* `apiCollectionOnboardings()` was added
* `apiCollections()` was added

## 1.0.0-beta.2 (2022-09-22)

- Azure Resource Manager Security client library for Java. This package contains Microsoft Azure SDK for Security Management SDK. API spec for Microsoft.Security (Azure Security Center) resource provider. Package tag package-composite-v3. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.Device` was removed

* `models.IotSensors` was removed

* `models.DevicesForSubscriptions` was removed

* `models.IotDefenderSettingsModel` was removed

* `models.IotSitesList` was removed

* `models.IotRecommendationType` was removed

* `models.SecurityAssessment$Definition` was removed

* `models.TiStatus` was removed

* `models.SecurityAssessmentMetadata$Definition` was removed

* `models.AlertNotifications` was removed

* `models.Devices` was removed

* `models.PurdueLevel` was removed

* `models.IotSites` was removed

* `models.NetworkInterface` was removed

* `models.PackageDownloadsSensor` was removed

* `models.SensorStatus` was removed

* `models.DeviceCriticality` was removed

* `models.IotAlertTypes` was removed

* `models.DeviceStatus` was removed

* `models.Site` was removed

* `models.AlertIntent` was removed

* `models.PackageDownloadsSensorFullOvf` was removed

* `models.OnPremiseIotSensor` was removed

* `models.PackageDownloadsCentralManagerFull` was removed

* `models.IotSensorsModel$UpdateStages` was removed

* `models.InformationProtectionPoliciesInformationProtectionPolicyName` was removed

* `models.IotSensorsList` was removed

* `models.IpAddress` was removed

* `models.OnboardingKind` was removed

* `models.AlertsToAdmins` was removed

* `models.ExternalSecuritySolutionKindValue` was removed

* `models.SecurityAssessment$Update` was removed

* `models.IotAlertListModel` was removed

* `models.SecurityAssessment$UpdateStages` was removed

* `models.IotSensorsModel` was removed

* `models.ScanningFunctionality` was removed

* `models.ProgrammingState` was removed

* `models.MacAddress` was removed

* `models.SettingsSettingName` was removed

* `models.ManagementState` was removed

* `models.IotAlertModel` was removed

* `models.IotSensorsModel$DefinitionStages` was removed

* `models.IotRecommendationTypes` was removed

* `models.IotDefenderSettings` was removed

* `models.Sensor` was removed

* `models.RelationToIpStatus` was removed

* `models.AuthorizationState` was removed

* `models.IotRecommendationListModel` was removed

* `models.SecurityAssessment$DefinitionStages` was removed

* `models.PackageDownloadsCentralManager` was removed

* `models.IotSitesModel` was removed

* `models.AadConnectivityStateValue` was removed

* `models.ErrorAdditionalInfo` was removed

* `models.SensorType` was removed

* `models.CloudErrorBody` was removed

* `models.SecurityAssessmentMetadataList` was removed

* `models.VersionKind` was removed

* `models.IotAlerts` was removed

* `models.PackageDownloadsCentralManagerFullOvf` was removed

* `models.SecurityAssessmentMetadata` was removed

* `models.KindValue` was removed

* `models.PackageDownloads` was removed

* `models.IotRecommendations` was removed

* `models.Firmware` was removed

* `models.SecurityAssessmentMetadata$DefinitionStages` was removed

* `models.IotAlertType` was removed

* `models.SecurityContact$UpdateStages` was removed

* `models.IotAlertTypeList` was removed

* `models.DevicesForHubs` was removed

* `models.SecurityAssessmentMetadataProperties` was removed

* `models.IotSensorsModel$Definition` was removed

* `models.IotRecommendationModel` was removed

* `models.UpgradePackageDownloadInfo` was removed

* `models.OnPremiseIotSensors` was removed

* `models.ResetPasswordInput` was removed

* `models.PackageDownloadInfo` was removed

* `models.OnPremiseIotSensorsList` was removed

* `models.DeviceList` was removed

* `models.IotSensorsModel$Update` was removed

* `models.PackageDownloadsSensorFull` was removed

* `models.IotDefenderSettingsList` was removed

* `models.ProtocolValue` was removed

* `models.TasksTaskUpdateActionType` was removed

* `models.RecommendationSeverity` was removed

* `models.IotRecommendationTypeList` was removed

* `models.SecurityContact$Update` was removed

* `models.MacSignificance` was removed

#### `models.AssessmentsMetadatas` was modified

* `models.SecurityAssessmentMetadata getInSubscription(java.lang.String)` -> `models.SecurityAssessmentMetadataResponse getInSubscription(java.lang.String)`
* `models.SecurityAssessmentMetadata$DefinitionStages$Blank define(java.lang.String)` -> `models.SecurityAssessmentMetadataResponse$DefinitionStages$Blank define(java.lang.String)`
* `models.SecurityAssessmentMetadata get(java.lang.String)` -> `models.SecurityAssessmentMetadataResponse get(java.lang.String)`
* `models.SecurityAssessmentMetadata getInSubscriptionById(java.lang.String)` -> `models.SecurityAssessmentMetadataResponse getInSubscriptionById(java.lang.String)`

#### `models.Kind` was modified

* `validate()` was removed
* `withKind(java.lang.String)` was removed
* `kind()` was removed

#### `SecurityManager` was modified

* `onPremiseIotSensors()` was removed
* `iotSensors()` was removed
* `devicesForHubs()` was removed
* `devices()` was removed
* `iotRecommendationTypes()` was removed
* `iotDefenderSettings()` was removed
* `iotRecommendations()` was removed
* `iotAlerts()` was removed
* `iotAlertTypes()` was removed
* `iotSites()` was removed
* `devicesForSubscriptions()` was removed

#### `models.JitNetworkAccessPortRule` was modified

* `models.ProtocolValue protocol()` -> `models.Protocol protocol()`
* `withProtocol(models.ProtocolValue)` was removed

#### `models.SecurityAssessment` was modified

* `models.SecurityAssessmentPartnerData partnersData()` -> `models.SecurityAssessmentPartnerData partnersData()`
* `models.SecurityAssessmentMetadataProperties metadata()` -> `fluent.models.SecurityAssessmentMetadataProperties metadata()`
* `models.AssessmentStatus status()` -> `models.AssessmentStatus status()`
* `java.util.Map additionalData()` -> `java.util.Map additionalData()`
* `id()` was removed
* `refresh()` was removed
* `type()` was removed
* `name()` was removed
* `update()` was removed
* `refresh(com.azure.core.util.Context)` was removed
* `models.ResourceDetails resourceDetails()` -> `models.ResourceDetails resourceDetails()`
* `java.lang.String displayName()` -> `java.lang.String displayName()`
* `innerModel()` was removed
* `models.AssessmentLinks links()` -> `models.AssessmentLinks links()`

#### `models.Protocol` was modified

* `identifiers()` was removed
* `withIdentifiers(java.lang.String)` was removed
* `name()` was removed
* `validate()` was removed

#### `models.InformationProtectionPolicies` was modified

* `define(models.InformationProtectionPoliciesInformationProtectionPolicyName)` was removed
* `getWithResponse(java.lang.String,models.InformationProtectionPoliciesInformationProtectionPolicyName,com.azure.core.util.Context)` was removed
* `get(java.lang.String,models.InformationProtectionPoliciesInformationProtectionPolicyName)` was removed

#### `models.AadConnectivityState` was modified

* `connectivityState()` was removed
* `validate()` was removed
* `withConnectivityState(models.AadConnectivityStateValue)` was removed

#### `models.Tasks` was modified

* `updateSubscriptionLevelTaskState(java.lang.String,java.lang.String,models.TasksTaskUpdateActionType)` was removed
* `updateSubscriptionLevelTaskStateWithResponse(java.lang.String,java.lang.String,models.TasksTaskUpdateActionType,com.azure.core.util.Context)` was removed
* `updateResourceGroupLevelTaskStateWithResponse(java.lang.String,java.lang.String,java.lang.String,models.TasksTaskUpdateActionType,com.azure.core.util.Context)` was removed
* `updateResourceGroupLevelTaskState(java.lang.String,java.lang.String,java.lang.String,models.TasksTaskUpdateActionType)` was removed

#### `models.Assessments` was modified

* `models.SecurityAssessment$DefinitionStages$Blank define(java.lang.String)` -> `models.SecurityAssessmentResponse$DefinitionStages$Blank define(java.lang.String)`
* `models.SecurityAssessment get(java.lang.String,java.lang.String)` -> `models.SecurityAssessmentResponse get(java.lang.String,java.lang.String)`
* `models.SecurityAssessment getById(java.lang.String)` -> `models.SecurityAssessmentResponse getById(java.lang.String)`

#### `models.SecurityContact$Definition` was modified

* `withAlertNotifications(models.AlertNotifications)` was removed
* `withAlertsToAdmins(models.AlertsToAdmins)` was removed
* `withEmail(java.lang.String)` was removed

#### `models.Settings` was modified

* `update(models.SettingsSettingName,fluent.models.SettingInner)` was removed
* `updateWithResponse(models.SettingsSettingName,fluent.models.SettingInner,com.azure.core.util.Context)` was removed
* `get(models.SettingsSettingName)` was removed
* `getWithResponse(models.SettingsSettingName,com.azure.core.util.Context)` was removed

#### `models.AadSolutionProperties` was modified

* `withConnectivityState(models.AadConnectivityStateValue)` was removed
* `models.AadConnectivityStateValue connectivityState()` -> `models.AadConnectivityState connectivityState()`

#### `models.SecurityContact` was modified

* `alertsToAdmins()` was removed
* `email()` was removed
* `models.AlertNotifications alertNotifications()` -> `models.SecurityContactPropertiesAlertNotifications alertNotifications()`
* `update()` was removed

### Features Added

* `models.GovernanceAssignment$Definition` was added

* `models.GcpOrganizationalDataOrganization` was added

* `models.SecurityConnector` was added

* `models.ExternalSecuritySolutionKind` was added

* `models.SecurityConnectorGovernanceRulesExecuteStatus` was added

* `models.SecurityConnector$Update` was added

* `models.SeverityEnum` was added

* `models.CustomAssessmentAutomationsListResult` was added

* `models.SecurityConnectorsList` was added

* `models.AadConnectivityStateAutoGenerated` was added

* `models.ExecuteGovernanceRuleParams` was added

* `models.Techniques` was added

* `models.CustomAssessmentAutomation$Definition` was added

* `models.SecurityConnectors` was added

* `models.SecurityAssessmentResponse` was added

* `models.SecurityAssessmentResponse$Definition` was added

* `models.CustomEntityStoreAssignmentRequest` was added

* `models.DefenderForServersGcpOffering` was added

* `models.SecurityConnector$UpdateStages` was added

* `models.GcpProjectEnvironmentData` was added

* `models.AwsOrganizationalDataMaster` was added

* `models.GcpOrganizationalDataMember` was added

* `models.DefenderForServersAwsOfferingSubPlan` was added

* `models.SupportedCloudEnum` was added

* `models.GovernanceAssignments` was added

* `models.GovernanceRuleSourceResourceType` was added

* `models.DefenderForServersGcpOfferingMdeAutoProvisioning` was added

* `models.CustomAssessmentAutomationRequest` was added

* `models.SecurityConnectorGovernanceRules` was added

* `models.AwsOrganizationalDataMember` was added

* `models.ScanningMode` was added

* `models.AzureDevOpsScopeEnvironmentData` was added

* `models.DefenderFoDatabasesAwsOfferingRds` was added

* `models.Tactics` was added

* `models.CustomAssessmentAutomations` was added

* `models.GovernanceRule$Update` was added

* `models.Roles` was added

* `models.GovernanceAssignment$Update` was added

* `models.Type` was added

* `models.SecurityConnectorApplications` was added

* `models.TaskUpdateActionType` was added

* `models.SecurityAssessmentResponse$UpdateStages` was added

* `models.MdeOnboardingData` was added

* `models.Application$UpdateStages` was added

* `models.GovernanceRuleType` was added

* `models.SecurityAssessmentResponse$Update` was added

* `models.CspmMonitorAzureDevOpsOffering` was added

* `models.DefenderForContainersAwsOfferingContainerVulnerabilityAssessment` was added

* `models.MdeOnboardings` was added

* `models.EnvironmentData` was added

* `models.DefenderForServersAwsOffering` was added

* `models.DefenderForServersAwsOfferingVaAutoProvisioning` was added

* `models.CspmMonitorGithubOffering` was added

* `models.CspmMonitorAwsOfferingNativeCloudConnection` was added

* `models.GovernanceRule$UpdateStages` was added

* `models.SettingName` was added

* `models.SecurityConnectorGovernanceRulesOperations` was added

* `models.EnvironmentType` was added

* `models.GovernanceAssignment` was added

* `models.DefenderForDatabasesGcpOfferingDefenderForDatabasesArcAutoProvisioning` was added

* `models.SecurityAssessmentPropertiesBase` was added

* `models.GovernanceRule$Definition` was added

* `models.OfferingType` was added

* `models.InformationProtectionAwsOffering` was added

* `models.DefenderForServersGcpOfferingArcAutoProvisioning` was added

* `models.Applications` was added

* `models.DefenderForServersGcpOfferingSubPlan` was added

* `models.SecurityAssessmentMetadataResponse$DefinitionStages` was added

* `models.GovernanceAssignment$UpdateStages` was added

* `models.AwsOrganizationalData` was added

* `models.KindAutoGenerated` was added

* `models.DefenderForServersAwsOfferingVmScannersConfiguration` was added

* `models.AssessmentStatusResponse` was added

* `models.AwsEnvironmentData` was added

* `models.SecurityAssessmentMetadataPropertiesResponsePublishDates` was added

* `models.GovernanceAssignmentsList` was added

* `models.SecurityAssessmentMetadataResponse` was added

* `models.SubscriptionGovernanceRulesExecuteStatus` was added

* `models.SecurityContactPropertiesNotificationsByRole` was added

* `models.GovernanceRule` was added

* `models.DefenderForDatabasesGcpOfferingArcAutoProvisioning` was added

* `models.DefenderForServersGcpOfferingVaAutoProvisioning` was added

* `models.GcpOrganizationalData` was added

* `models.MinimalSeverity` was added

* `models.CustomAssessmentAutomation$DefinitionStages` was added

* `models.DefenderFoDatabasesAwsOfferingArcAutoProvisioning` was added

* `models.DefenderForContainersAwsOfferingContainerVulnerabilityAssessmentTask` was added

* `models.Application$Definition` was added

* `models.CloudOffering` was added

* `models.DefenderCspmAwsOfferingVmScannersConfiguration` was added

* `models.CustomEntityStoreAssignment$DefinitionStages` was added

* `models.DefenderCspmAwsOfferingVmScanners` was added

* `models.SubPlan` was added

* `models.ExecuteRuleStatus` was added

* `models.SecurityConnector$Definition` was added

* `models.DefenderForContainersGcpOfferingDataPipelineNativeCloudConnection` was added

* `models.DefenderForContainersGcpOfferingNativeCloudConnection` was added

* `models.Application$Update` was added

* `models.CspmMonitorGcpOfferingNativeCloudConnection` was added

* `models.DefenderForContainersAwsOfferingCloudWatchToKinesis` was added

* `models.GithubScopeEnvironmentData` was added

* `models.DefenderForServersAwsOfferingVmScanners` was added

* `models.DefenderFoDatabasesAwsOffering` was added

* `models.DefenderForServersGcpOfferingVaAutoProvisioningConfiguration` was added

* `models.GovernanceRuleOwnerSource` was added

* `models.CspmMonitorGcpOffering` was added

* `models.DefenderForContainersAwsOfferingKubernetesService` was added

* `models.GovernanceAssignmentAdditionalData` was added

* `models.SecurityAssessmentResponse$DefinitionStages` was added

* `models.CustomEntityStoreAssignment$Definition` was added

* `models.CspmMonitorAwsOffering` was added

* `models.SecurityConnector$DefinitionStages` was added

* `models.CloudName` was added

* `models.DefenderForDatabasesGcpOffering` was added

* `models.GovernanceRuleList` was added

* `models.SecureScoreControlScoreDetails` was added

* `models.GovernanceAssignment$DefinitionStages` was added

* `models.DefenderForDevOpsGithubOffering` was added

* `models.CustomEntityStoreAssignments` was added

* `models.Application$DefinitionStages` was added

* `models.ApplicationsList` was added

* `models.OrganizationMembershipType` was added

* `models.DefenderForContainersAwsOfferingKubernetesScubaReader` was added

* `models.GovernanceRule$DefinitionStages` was added

* `models.CustomEntityStoreAssignmentsListResult` was added

* `models.DefenderForContainersAwsOffering` was added

* `models.SecurityContactPropertiesAlertNotifications` was added

* `models.InformationProtectionAwsOfferingInformationProtection` was added

* `models.Application` was added

* `models.CustomAssessmentAutomation` was added

* `models.GovernanceRuleOwnerSourceType` was added

* `models.DefenderForServersGcpOfferingDefenderForServers` was added

* `models.RemediationEta` was added

* `models.ApplicationOperations` was added

* `models.InformationProtectionPolicyName` was added

* `models.SecurityAssessmentMetadataResponse$Definition` was added

* `models.GcpProjectDetails` was added

* `models.AlertPropertiesSupportingEvidence` was added

* `models.DefenderForServersAwsOfferingDefenderForServers` was added

* `models.DefenderForContainersAwsOfferingKinesisToS3` was added

* `models.SecurityConnectorApplicationOperations` was added

* `models.ApplicationSourceResourceType` was added

* `models.DefenderForContainersGcpOffering` was added

* `models.DefenderCspmAwsOffering` was added

* `models.DefenderForServersAwsOfferingMdeAutoProvisioning` was added

* `models.GovernanceRules` was added

* `models.MdeOnboardingDataList` was added

* `models.SecurityAssessmentMetadataResponseList` was added

* `models.GovernanceEmailNotification` was added

* `models.GovernanceRuleEmailNotification` was added

* `models.CustomEntityStoreAssignment` was added

* `models.DefenderCspmGcpOffering` was added

* `models.DefenderForDevOpsAzureDevOpsOffering` was added

* `models.DefenderForServersAwsOfferingArcAutoProvisioning` was added

* `models.GovernanceRulesOperations` was added

* `models.DefenderForServersAwsOfferingVaAutoProvisioningConfiguration` was added

#### `models.Automation` was modified

* `resourceGroupName()` was added

#### `models.Alerts` was modified

* `updateSubscriptionLevelStateToInProgress(java.lang.String,java.lang.String)` was added
* `updateResourceGroupLevelStateToInProgress(java.lang.String,java.lang.String,java.lang.String)` was added
* `updateResourceGroupLevelStateToInProgressWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `updateSubscriptionLevelStateToInProgressWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Pricing` was modified

* `replacedBy()` was added
* `subPlan()` was added
* `deprecated()` was added

#### `models.Alert` was modified

* `version()` was added
* `supportingEvidence()` was added
* `techniques()` was added
* `subTechniques()` was added

#### `models.Kind` was modified

* `values()` was added
* `fromString(java.lang.String)` was added

#### `SecurityManager` was modified

* `securityConnectorGovernanceRulesOperations()` was added
* `securityConnectorApplicationOperations()` was added
* `securityConnectors()` was added
* `customAssessmentAutomations()` was added
* `governanceRulesOperations()` was added
* `mdeOnboardings()` was added
* `applications()` was added
* `applicationOperations()` was added
* `securityConnectorApplications()` was added
* `subscriptionGovernanceRulesExecuteStatus()` was added
* `governanceAssignments()` was added
* `customEntityStoreAssignments()` was added
* `securityConnectorGovernanceRulesExecuteStatus()` was added
* `securityConnectorGovernanceRules()` was added
* `governanceRules()` was added
* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.JitNetworkAccessPortRule` was modified

* `withProtocol(models.Protocol)` was added

#### `models.SecurityAssessment` was modified

* `withStatus(models.AssessmentStatus)` was added
* `withPartnersData(models.SecurityAssessmentPartnerData)` was added
* `withAdditionalData(java.util.Map)` was added
* `withResourceDetails(models.ResourceDetails)` was added
* `withMetadata(fluent.models.SecurityAssessmentMetadataProperties)` was added
* `validate()` was added

#### `models.Protocol` was modified

* `values()` was added
* `fromString(java.lang.String)` was added

#### `models.InformationProtectionPolicies` was modified

* `get(java.lang.String,models.InformationProtectionPolicyName)` was added
* `getWithResponse(java.lang.String,models.InformationProtectionPolicyName,com.azure.core.util.Context)` was added
* `define(models.InformationProtectionPolicyName)` was added

#### `models.AadConnectivityState` was modified

* `fromString(java.lang.String)` was added
* `values()` was added

#### `models.JitNetworkAccessPolicy` was modified

* `resourceGroupName()` was added

#### `models.Tasks` was modified

* `updateResourceGroupLevelTaskState(java.lang.String,java.lang.String,java.lang.String,models.TaskUpdateActionType)` was added
* `updateSubscriptionLevelTaskState(java.lang.String,java.lang.String,models.TaskUpdateActionType)` was added
* `updateSubscriptionLevelTaskStateWithResponse(java.lang.String,java.lang.String,models.TaskUpdateActionType,com.azure.core.util.Context)` was added
* `updateResourceGroupLevelTaskStateWithResponse(java.lang.String,java.lang.String,java.lang.String,models.TaskUpdateActionType,com.azure.core.util.Context)` was added

#### `models.IoTSecuritySolutionModel` was modified

* `resourceGroupName()` was added

#### `models.SecurityContact$Definition` was modified

* `withAlertNotifications(models.SecurityContactPropertiesAlertNotifications)` was added
* `withNotificationsByRole(models.SecurityContactPropertiesNotificationsByRole)` was added
* `withEmails(java.lang.String)` was added

#### `models.Settings` was modified

* `updateWithResponse(models.SettingName,fluent.models.SettingInner,com.azure.core.util.Context)` was added
* `getWithResponse(models.SettingName,com.azure.core.util.Context)` was added
* `get(models.SettingName)` was added
* `update(models.SettingName,fluent.models.SettingInner)` was added

#### `SecurityManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.AadSolutionProperties` was modified

* `withConnectivityState(models.AadConnectivityState)` was added

#### `models.SecurityContact` was modified

* `notificationsByRole()` was added
* `emails()` was added

## 1.0.0-beta.1 (2021-07-14)

- Azure Resource Manager Security client library for Java. This package contains Microsoft Azure SDK for Security Management SDK. API spec for Microsoft.Security (Azure Security Center) resource provider. Package tag package-composite-v3. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

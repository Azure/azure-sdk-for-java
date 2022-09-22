# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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

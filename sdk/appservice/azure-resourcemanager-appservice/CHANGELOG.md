# Release History

## 2.55.0 (2026-02-26)

### Breaking Changes

#### `models.WorkflowEnvelopeCollection` was removed

#### `models.IdentifierCollection` was removed

#### `models.DeletedWebAppCollection` was removed

#### `models.FunctionAppStackCollection` was removed

#### `models.AppServicePlanCollection` was removed

#### `models.CsmDeploymentStatusCollection` was removed

#### `models.WorkflowRunActionRepetitionDefinitionCollection` was removed

#### `models.SiteConfigResourceCollection` was removed

#### `models.UsageCollection` was removed

#### `models.AppServiceEnvironmentCollection` was removed

#### `models.HostnameBindingCollection` was removed

#### `models.CsmOperationCollection` was removed

#### `models.DatabaseConnectionCollection` was removed

#### `models.SkuInfoCollection` was removed

#### `models.WebAppInstanceStatusCollection` was removed

#### `models.StaticSiteCollection` was removed

#### `models.OutboundEnvironmentEndpointCollection` was removed

#### `models.ProcessInfoCollection` was removed

#### `models.WorkerPoolCollection` was removed

#### `models.RecommendationCollection` was removed

#### `models.BillingMeterCollection` was removed

#### `models.FunctionEnvelopeCollection` was removed

#### `models.SiteExtensionInfoCollection` was removed

#### `models.ResourceMetricDefinitionCollection` was removed

#### `models.WebJobCollection` was removed

#### `models.StaticSiteLinkedBackendsCollection` was removed

#### `models.SlotDifferenceCollection` was removed

#### `models.DeploymentCollection` was removed

#### `models.StaticSiteBuildCollection` was removed

#### `models.WorkflowVersionListResult` was removed

#### `models.ApiKVReferenceCollection` was removed

#### `models.WorkflowRunListResult` was removed

#### `models.DiagnosticDetectorCollection` was removed

#### `models.SiteContainerCollection` was removed

#### `models.CustomHostnameSitesCollection` was removed

#### `models.StaticSiteBasicAuthPropertiesCollection` was removed

#### `models.WorkflowTriggerListResult` was removed

#### `models.PrivateEndpointConnectionCollection` was removed

#### `models.ExpressionTraces` was removed

#### `models.CsmUsageQuotaCollection` was removed

#### `models.PrivateLinkConnectionApprovalRequestResource` was removed

#### `models.PublicCertificateCollection` was removed

#### `models.AseRegionCollection` was removed

#### `models.RequestHistoryListResult` was removed

#### `models.StaticSiteFunctionOverviewCollection` was removed

#### `models.StaticSiteUserProvidedFunctionAppsCollection` was removed

#### `models.ApplicationStackCollection` was removed

#### `models.StaticSiteCustomDomainOverviewCollection` was removed

#### `models.ProcessModuleInfoCollection` was removed

#### `models.TriggeredWebJobCollection` was removed

#### `models.ContinuousWebJobCollection` was removed

#### `models.HybridConnectionCollection` was removed

#### `models.PerfMonCounterCollection` was removed

#### `models.SiteConfigurationSnapshotInfoCollection` was removed

#### `models.WebAppStackCollection` was removed

#### `models.BackupItemCollection` was removed

#### `models.PremierAddOnOfferCollection` was removed

#### `models.CertificateCollection` was removed

#### `models.TriggeredJobHistoryCollection` was removed

#### `models.InboundEnvironmentEndpointCollection` was removed

#### `models.StaticSiteUserCollection` was removed

#### `models.SnapshotCollection` was removed

#### `models.DiagnosticCategoryCollection` was removed

#### `models.DiagnosticAnalysisCollection` was removed

#### `models.ProcessThreadInfoCollection` was removed

#### `models.StampCapacityCollection` was removed

#### `models.GeoRegionCollection` was removed

#### `models.WorkflowTriggerHistoryListResult` was removed

#### `models.WebAppCollection` was removed

#### `models.WorkflowRunActionListResult` was removed

#### `models.SourceControlCollection` was removed

#### `models.DetectorResponseCollection` was removed

#### `models.KubeEnvironmentCollection` was removed

#### `models.ResourceHealthMetadataCollection` was removed

#### `models.PublishingCredentialsPoliciesCollection` was removed

#### `models.ResourceCollection` was removed

#### `models.StaticSiteUserProvidedFunctionApp` was modified

* `StaticSiteUserProvidedFunctionApp()` was changed to private access
* `withFunctionAppResourceId(java.lang.String)` was removed
* `withFunctionAppRegion(java.lang.String)` was removed
* `withKind(java.lang.String)` was removed

#### `models.ContainerCpuUsage` was modified

* `ContainerCpuUsage()` was changed to private access
* `withKernelModeUsage(java.lang.Long)` was removed
* `withUserModeUsage(java.lang.Long)` was removed
* `withTotalUsage(java.lang.Long)` was removed
* `withPerCpuUsage(java.util.List)` was removed

#### `models.AppInsightsWebAppStackSettings` was modified

* `AppInsightsWebAppStackSettings()` was changed to private access

#### `models.MetricSpecification` was modified

* `MetricSpecification()` was changed to private access
* `withCategory(java.lang.String)` was removed
* `withFillGapWithZero(java.lang.Boolean)` was removed
* `withSupportedTimeGrainTypes(java.util.List)` was removed
* `withName(java.lang.String)` was removed
* `withDisplayDescription(java.lang.String)` was removed
* `withDimensions(java.util.List)` was removed
* `withAggregationType(java.lang.String)` was removed
* `withSourceMdmNamespace(java.lang.String)` was removed
* `withSupportsInstanceLevelAggregation(java.lang.Boolean)` was removed
* `withSourceMdmAccount(java.lang.String)` was removed
* `withSupportedAggregationTypes(java.util.List)` was removed
* `withAvailabilities(java.util.List)` was removed
* `withEnableRegionalMdmAccount(java.lang.Boolean)` was removed
* `withDisplayName(java.lang.String)` was removed
* `withUnit(java.lang.String)` was removed
* `withMetricFilterPattern(java.lang.String)` was removed
* `withIsInternal(java.lang.Boolean)` was removed

#### `models.Expression` was modified

* `models.Expression withSubexpressions(java.util.List)` -> `models.Expression withSubexpressions(java.util.List)`
* `models.Expression withValue(java.lang.Object)` -> `models.Expression withValue(java.lang.Object)`
* `models.Expression withText(java.lang.String)` -> `models.Expression withText(java.lang.String)`
* `models.Expression withError(models.AzureResourceErrorInfo)` -> `models.Expression withError(models.AzureResourceErrorInfo)`

#### `models.RepetitionIndex` was modified

* `RepetitionIndex()` was changed to private access
* `withItemIndex(int)` was removed
* `withScopeName(java.lang.String)` was removed

#### `models.CsmOperationDisplay` was modified

* `CsmOperationDisplay()` was changed to private access
* `withProvider(java.lang.String)` was removed
* `withDescription(java.lang.String)` was removed
* `withOperation(java.lang.String)` was removed
* `withResource(java.lang.String)` was removed

#### `models.WorkflowTriggerListCallbackUrlQueries` was modified

* `WorkflowTriggerListCallbackUrlQueries()` was changed to private access
* `withSe(java.lang.String)` was removed
* `withSv(java.lang.String)` was removed
* `withSp(java.lang.String)` was removed
* `withSig(java.lang.String)` was removed
* `withApiVersion(java.lang.String)` was removed

#### `models.PrivateLinkResource` was modified

* `PrivateLinkResource()` was changed to private access
* `withProperties(models.PrivateLinkResourceProperties)` was removed
* `withName(java.lang.String)` was removed
* `withId(java.lang.String)` was removed
* `withType(java.lang.String)` was removed

#### `models.RecurrenceSchedule` was modified

* `RecurrenceSchedule()` was changed to private access
* `withMonthDays(java.util.List)` was removed
* `withMonthlyOccurrences(java.util.List)` was removed
* `withMinutes(java.util.List)` was removed
* `withHours(java.util.List)` was removed
* `withWeekDays(java.util.List)` was removed

#### `models.RecurrenceScheduleOccurrence` was modified

* `RecurrenceScheduleOccurrence()` was changed to private access
* `withOccurrence(java.lang.Integer)` was removed
* `withDay(models.DayOfWeek)` was removed

#### `models.DataTableResponseColumn` was modified

* `DataTableResponseColumn()` was changed to private access
* `withColumnType(java.lang.String)` was removed
* `withDataType(java.lang.String)` was removed
* `withColumnName(java.lang.String)` was removed

#### `models.ContainerNetworkInterfaceStatistics` was modified

* `ContainerNetworkInterfaceStatistics()` was changed to private access
* `withRxPackets(java.lang.Long)` was removed
* `withTxBytes(java.lang.Long)` was removed
* `withRxBytes(java.lang.Long)` was removed
* `withTxDropped(java.lang.Long)` was removed
* `withRxDropped(java.lang.Long)` was removed
* `withTxErrors(java.lang.Long)` was removed
* `withTxPackets(java.lang.Long)` was removed
* `withRxErrors(java.lang.Long)` was removed

#### `models.StaticSiteLinkedBackend` was modified

* `StaticSiteLinkedBackend()` was changed to private access
* `withBackendResourceId(java.lang.String)` was removed
* `withRegion(java.lang.String)` was removed

#### `models.ServiceSpecification` was modified

* `ServiceSpecification()` was changed to private access
* `withMetricSpecifications(java.util.List)` was removed
* `withLogSpecifications(java.util.List)` was removed

#### `models.WebAppRuntimeSettings` was modified

* `WebAppRuntimeSettings()` was changed to private access

#### `models.WorkflowHealth` was modified

* `WorkflowHealth()` was changed to private access
* `withState(models.WorkflowHealthState)` was removed
* `withError(models.ErrorEntity)` was removed

#### `models.GlobalCsmSkuDescription` was modified

* `GlobalCsmSkuDescription()` was changed to private access
* `withLocations(java.util.List)` was removed
* `withCapacity(models.SkuCapacity)` was removed
* `withCapabilities(java.util.List)` was removed
* `withName(java.lang.String)` was removed
* `withSize(java.lang.String)` was removed
* `withFamily(java.lang.String)` was removed
* `withTier(java.lang.String)` was removed

#### `models.SupportTopic` was modified

* `SupportTopic()` was changed to private access

#### `models.StackMinorVersion` was modified

* `StackMinorVersion()` was changed to private access
* `withDisplayVersion(java.lang.String)` was removed
* `withRuntimeVersion(java.lang.String)` was removed
* `withIsDefault(java.lang.Boolean)` was removed
* `withIsRemoteDebuggingEnabled(java.lang.Boolean)` was removed

#### `models.AbnormalTimePeriod` was modified

* `AbnormalTimePeriod()` was changed to private access
* `withEvents(java.util.List)` was removed
* `withStartTime(java.time.OffsetDateTime)` was removed
* `withEndTime(java.time.OffsetDateTime)` was removed
* `withSolutions(java.util.List)` was removed

#### `models.PerfMonSample` was modified

* `PerfMonSample()` was changed to private access
* `withInstanceName(java.lang.String)` was removed
* `withTime(java.time.OffsetDateTime)` was removed
* `withValue(java.lang.Double)` was removed

#### `models.Correlation` was modified

* `Correlation()` was changed to private access
* `withClientTrackingId(java.lang.String)` was removed

#### `models.RemotePrivateEndpointConnection` was modified

* `RemotePrivateEndpointConnection()` was changed to private access
* `withKind(java.lang.String)` was removed
* `withPrivateLinkServiceConnectionState(models.PrivateLinkConnectionState)` was removed
* `withIpAddresses(java.util.List)` was removed
* `withPrivateEndpoint(models.ArmIdWrapper)` was removed

#### `models.ContainerMemoryStatistics` was modified

* `ContainerMemoryStatistics()` was changed to private access
* `withUsage(java.lang.Long)` was removed
* `withLimit(java.lang.Long)` was removed
* `withMaxUsage(java.lang.Long)` was removed

#### `models.QueryUtterancesResult` was modified

* `QueryUtterancesResult()` was changed to private access
* `withScore(java.lang.Float)` was removed
* `withSampleUtterance(models.SampleUtterance)` was removed

#### `models.DetectorAbnormalTimePeriod` was modified

* `DetectorAbnormalTimePeriod()` was changed to private access
* `withPriority(java.lang.Double)` was removed
* `withSolutions(java.util.List)` was removed
* `withSource(java.lang.String)` was removed
* `withStartTime(java.time.OffsetDateTime)` was removed
* `withMetadata(java.util.List)` was removed
* `withMessage(java.lang.String)` was removed
* `withType(models.IssueType)` was removed
* `withEndTime(java.time.OffsetDateTime)` was removed

#### `models.ArmPlan` was modified

* `ArmPlan()` was changed to private access
* `withPublisher(java.lang.String)` was removed
* `withVersion(java.lang.String)` was removed
* `withName(java.lang.String)` was removed
* `withPromotionCode(java.lang.String)` was removed
* `withProduct(java.lang.String)` was removed

#### `models.VnetValidationTestFailure` was modified

* `VnetValidationTestFailure()` was changed to private access
* `withKind(java.lang.String)` was removed
* `withTestName(java.lang.String)` was removed
* `withDetails(java.lang.String)` was removed

#### `models.GitHubActionWebAppStackSettings` was modified

* `GitHubActionWebAppStackSettings()` was changed to private access

#### `models.TriggeredJobRun` was modified

* `TriggeredJobRun()` was changed to private access
* `withDuration(java.lang.String)` was removed
* `withOutputUrl(java.lang.String)` was removed
* `withStartTime(java.time.OffsetDateTime)` was removed
* `withStatus(models.TriggeredWebJobStatus)` was removed
* `withTrigger(java.lang.String)` was removed
* `withEndTime(java.time.OffsetDateTime)` was removed
* `withErrorUrl(java.lang.String)` was removed
* `withJobName(java.lang.String)` was removed
* `withWebJobName(java.lang.String)` was removed
* `withUrl(java.lang.String)` was removed
* `withWebJobId(java.lang.String)` was removed

#### `models.WorkflowResource` was modified

* `withLocation(java.lang.String)` was removed
* `withTags(java.util.Map)` was removed

#### `models.Request` was modified

* `Request()` was changed to private access
* `withUri(java.lang.String)` was removed
* `withHeaders(java.lang.Object)` was removed
* `withMethod(java.lang.String)` was removed

#### `models.AnalysisData` was modified

* `AnalysisData()` was changed to private access
* `withDetectorMetadata(models.ResponseMetadata)` was removed
* `withSource(java.lang.String)` was removed
* `withDetectorDefinition(fluent.models.DetectorDefinition)` was removed
* `withMetrics(java.util.List)` was removed
* `withData(java.util.List)` was removed

#### `models.DiagnosticData` was modified

* `DiagnosticData()` was changed to private access
* `withRenderingProperties(models.Rendering)` was removed
* `withTable(models.DataTableResponseObject)` was removed

#### `models.WorkflowRunTrigger` was modified

* `WorkflowRunTrigger()` was changed to private access
* `withCorrelation(models.Correlation)` was removed

#### `models.DetectorInfo` was modified

* `DetectorInfo()` was changed to private access

#### `models.DataProviderMetadata` was modified

* `DataProviderMetadata()` was changed to private access
* `withProviderName(java.lang.String)` was removed

#### `models.AzureResourceErrorInfo` was modified

* `AzureResourceErrorInfo()` was changed to private access
* `withDetails(java.util.List)` was removed
* `withCode(java.lang.String)` was removed
* `withMessage(java.lang.String)` was removed

#### `models.QueryUtterancesResults` was modified

* `QueryUtterancesResults()` was changed to private access
* `withResults(java.util.List)` was removed
* `withQuery(java.lang.String)` was removed

#### `models.Dimension` was modified

* `Dimension()` was changed to private access
* `withInternalName(java.lang.String)` was removed
* `withName(java.lang.String)` was removed
* `withDisplayName(java.lang.String)` was removed
* `withToBeExportedForShoebox(java.lang.Boolean)` was removed

#### `models.WorkflowOutputParameter` was modified

* `WorkflowOutputParameter()` was changed to private access
* `withDescription(java.lang.String)` was removed
* `withType(models.ParameterType)` was removed
* `withValue(java.lang.Object)` was removed
* `withMetadata(java.lang.Object)` was removed

#### `models.ContainerCpuStatistics` was modified

* `ContainerCpuStatistics()` was changed to private access
* `withOnlineCpuCount(java.lang.Integer)` was removed
* `withSystemCpuUsage(java.lang.Long)` was removed
* `withThrottlingData(models.ContainerThrottlingData)` was removed
* `withCpuUsage(models.ContainerCpuUsage)` was removed

#### `models.ServerFarmInstance` was modified

* `ServerFarmInstance()` was changed to private access
* `withInstanceName(java.lang.String)` was removed
* `withIpAddress(java.lang.String)` was removed
* `withStatus(java.lang.String)` was removed

#### `models.DiagnosticMetricSet` was modified

* `DiagnosticMetricSet()` was changed to private access
* `withTimeGrain(java.lang.String)` was removed
* `withName(java.lang.String)` was removed
* `withStartTime(java.time.OffsetDateTime)` was removed
* `withValues(java.util.List)` was removed
* `withUnit(java.lang.String)` was removed
* `withEndTime(java.time.OffsetDateTime)` was removed

#### `models.SiteMachineKey` was modified

* `SiteMachineKey()` was changed to private access
* `withDecryptionKey(java.lang.String)` was removed
* `withValidationKey(java.lang.String)` was removed
* `withValidation(java.lang.String)` was removed
* `withDecryption(java.lang.String)` was removed

#### `models.ResponseMetadata` was modified

* `ResponseMetadata()` was changed to private access
* `withDataSource(models.DataSource)` was removed

#### `models.ResponseMessageEnvelopeRemotePrivateEndpointConnection` was modified

* `ResponseMessageEnvelopeRemotePrivateEndpointConnection()` was changed to private access
* `withPlan(models.ArmPlan)` was removed
* `withZones(java.util.List)` was removed
* `withTags(java.util.Map)` was removed
* `withId(java.lang.String)` was removed
* `withStatus(java.lang.String)` was removed
* `withName(java.lang.String)` was removed
* `withIdentity(models.ManagedServiceIdentity)` was removed
* `withLocation(java.lang.String)` was removed
* `withProperties(models.RemotePrivateEndpointConnection)` was removed
* `withSku(models.SkuDescription)` was removed
* `withError(models.ErrorEntity)` was removed
* `withType(java.lang.String)` was removed

#### `models.DataTableResponseObject` was modified

* `DataTableResponseObject()` was changed to private access
* `withRows(java.util.List)` was removed
* `withTableName(java.lang.String)` was removed
* `withColumns(java.util.List)` was removed

#### `models.Workflow` was modified

* `withLocation(java.lang.String)` was removed
* `withTags(java.util.Map)` was removed

#### `models.Status` was modified

* `Status()` was changed to private access
* `withMessage(java.lang.String)` was removed
* `withStatusId(models.InsightStatus)` was removed

#### `models.MetricAvailability` was modified

* `MetricAvailability()` was changed to private access
* `withTimeGrain(java.lang.String)` was removed
* `withBlobDuration(java.lang.String)` was removed

#### `models.KeyValuePairStringObject` was modified

* `KeyValuePairStringObject()` was changed to private access

#### `models.CertificatePatchResource` was modified

* `withPassword(java.lang.String)` was removed

#### `models.ResourceMetricAvailability` was modified

* `ResourceMetricAvailability()` was changed to private access

#### `models.SiteCloneabilityCriterion` was modified

* `SiteCloneabilityCriterion()` was changed to private access
* `withDescription(java.lang.String)` was removed
* `withName(java.lang.String)` was removed

#### `models.EndpointDependency` was modified

* `EndpointDependency()` was changed to private access
* `withDomainName(java.lang.String)` was removed
* `withEndpointDetails(java.util.List)` was removed

#### `models.HostingEnvironmentDeploymentInfo` was modified

* `HostingEnvironmentDeploymentInfo()` was changed to private access
* `withLocation(java.lang.String)` was removed
* `withName(java.lang.String)` was removed

#### `models.SiteConfigPropertiesDictionary` was modified

* `SiteConfigPropertiesDictionary()` was changed to private access

#### `models.LocalizableString` was modified

* `LocalizableString()` was changed to private access
* `withLocalizedValue(java.lang.String)` was removed
* `withValue(java.lang.String)` was removed

#### `models.DiagnosticMetricSample` was modified

* `DiagnosticMetricSample()` was changed to private access
* `withTimestamp(java.time.OffsetDateTime)` was removed
* `withTotal(java.lang.Double)` was removed
* `withRoleInstance(java.lang.String)` was removed
* `withMaximum(java.lang.Double)` was removed
* `withMinimum(java.lang.Double)` was removed
* `withIsAggregated(java.lang.Boolean)` was removed

#### `models.Rendering` was modified

* `Rendering()` was changed to private access
* `withDescription(java.lang.String)` was removed
* `withTitle(java.lang.String)` was removed
* `withType(models.RenderingType)` was removed

#### `models.RequestHistoryProperties` was modified

* `RequestHistoryProperties()` was changed to private access
* `withStartTime(java.time.OffsetDateTime)` was removed
* `withEndTime(java.time.OffsetDateTime)` was removed
* `withRequest(models.Request)` was removed
* `withResponse(models.Response)` was removed

#### `models.EndpointDetail` was modified

* `EndpointDetail()` was changed to private access
* `withIpAddress(java.lang.String)` was removed
* `withPort(java.lang.Integer)` was removed
* `withIsAccessible(java.lang.Boolean)` was removed
* `withLatency(java.lang.Double)` was removed

#### `models.ValidateResponseError` was modified

* `ValidateResponseError()` was changed to private access
* `withCode(java.lang.String)` was removed
* `withMessage(java.lang.String)` was removed

#### `models.ContainerInfo` was modified

* `ContainerInfo()` was changed to private access
* `withEth0(models.ContainerNetworkInterfaceStatistics)` was removed
* `withName(java.lang.String)` was removed
* `withId(java.lang.String)` was removed
* `withCurrentTimestamp(java.time.OffsetDateTime)` was removed
* `withPreviousCpuStats(models.ContainerCpuStatistics)` was removed
* `withPreviousTimestamp(java.time.OffsetDateTime)` was removed
* `withCurrentCpuStats(models.ContainerCpuStatistics)` was removed
* `withMemoryStats(models.ContainerMemoryStatistics)` was removed

#### `models.WebAppMajorVersion` was modified

* `WebAppMajorVersion()` was changed to private access

#### `models.WindowsJavaContainerSettings` was modified

* `WindowsJavaContainerSettings()` was changed to private access

#### `models.DataSource` was modified

* `DataSource()` was changed to private access
* `withDataSourceUri(java.util.List)` was removed
* `withInstructions(java.util.List)` was removed

#### `models.ErrorEntity` was modified

* `ErrorEntity()` was changed to private access
* `withExtendedCode(java.lang.String)` was removed
* `withDetails(java.util.List)` was removed
* `withInnerErrors(java.util.List)` was removed
* `withTarget(java.lang.String)` was removed
* `withCode(java.lang.String)` was removed
* `withMessage(java.lang.String)` was removed
* `withParameters(java.util.List)` was removed
* `withMessageTemplate(java.lang.String)` was removed

#### `models.StackMajorVersion` was modified

* `StackMajorVersion()` was changed to private access
* `withSiteConfigPropertiesDictionary(java.util.Map)` was removed
* `withApplicationInsights(java.lang.Boolean)` was removed
* `withDisplayVersion(java.lang.String)` was removed
* `withAppSettingsDictionary(java.util.Map)` was removed
* `withIsHidden(java.lang.Boolean)` was removed
* `withIsDeprecated(java.lang.Boolean)` was removed
* `withMinorVersions(java.util.List)` was removed
* `withIsDefault(java.lang.Boolean)` was removed
* `withIsPreview(java.lang.Boolean)` was removed
* `withRuntimeVersion(java.lang.String)` was removed

#### `models.FunctionAppRuntimeSettings` was modified

* `FunctionAppRuntimeSettings()` was changed to private access

#### `models.FunctionAppMinorVersion` was modified

* `FunctionAppMinorVersion()` was changed to private access

#### `models.WorkflowSku` was modified

* `WorkflowSku()` was changed to private access
* `withPlan(models.ResourceReference)` was removed
* `withName(models.WorkflowSkuName)` was removed

#### `models.VirtualIpMapping` was modified

* `VirtualIpMapping()` was changed to private access
* `withInUse(java.lang.Boolean)` was removed
* `withInternalHttpPort(java.lang.Integer)` was removed
* `withInternalHttpsPort(java.lang.Integer)` was removed
* `withServiceName(java.lang.String)` was removed
* `withVirtualIp(java.lang.String)` was removed

#### `models.Response` was modified

* `Response()` was changed to private access
* `withStatusCode(java.lang.Integer)` was removed
* `withBodyLink(models.ContentLink)` was removed
* `withHeaders(java.lang.Object)` was removed

#### `models.MSDeployLogEntry` was modified

* `MSDeployLogEntry()` was changed to private access

#### `models.ContentHash` was modified

* `ContentHash()` was changed to private access
* `withAlgorithm(java.lang.String)` was removed
* `withValue(java.lang.String)` was removed

#### `models.FunctionAppRuntimes` was modified

* `FunctionAppRuntimes()` was changed to private access

#### `models.DatabaseConnectionOverview` was modified

* `DatabaseConnectionOverview()` was changed to private access

#### `models.WorkflowTriggerRecurrence` was modified

* `WorkflowTriggerRecurrence()` was changed to private access
* `withFrequency(models.RecurrenceFrequency)` was removed
* `withEndTime(java.lang.String)` was removed
* `withSchedule(models.RecurrenceSchedule)` was removed
* `withTimeZone(java.lang.String)` was removed
* `withStartTime(java.lang.String)` was removed
* `withInterval(java.lang.Integer)` was removed

#### `models.WorkflowEnvelopeProperties` was modified

* `WorkflowEnvelopeProperties()` was changed to private access
* `withHealth(models.WorkflowHealth)` was removed
* `withFiles(java.util.Map)` was removed
* `withFlowState(models.WorkflowState)` was removed

#### `models.CsmOperationDescriptionProperties` was modified

* `CsmOperationDescriptionProperties()` was changed to private access
* `withServiceSpecification(models.ServiceSpecification)` was removed

#### `models.LogSpecification` was modified

* `LogSpecification()` was changed to private access
* `withDisplayName(java.lang.String)` was removed
* `withLogFilterPattern(java.lang.String)` was removed
* `withName(java.lang.String)` was removed
* `withBlobDuration(java.lang.String)` was removed

#### `models.Solution` was modified

* `Solution()` was changed to private access
* `withOrder(java.lang.Double)` was removed
* `withType(models.SolutionType)` was removed
* `withMetadata(java.util.List)` was removed
* `withDescription(java.lang.String)` was removed
* `withDisplayName(java.lang.String)` was removed
* `withId(java.lang.Double)` was removed
* `withData(java.util.List)` was removed

#### `models.OperationResultProperties` was modified

* `models.OperationResultProperties withCode(java.lang.String)` -> `models.OperationResultProperties withCode(java.lang.String)`
* `models.OperationResultProperties withError(java.lang.Object)` -> `models.OperationResultProperties withError(java.lang.Object)`
* `models.OperationResultProperties withStatus(models.WorkflowStatus)` -> `models.OperationResultProperties withStatus(models.WorkflowStatus)`
* `models.OperationResultProperties withEndTime(java.time.OffsetDateTime)` -> `models.OperationResultProperties withEndTime(java.time.OffsetDateTime)`
* `models.OperationResultProperties withCorrelation(models.RunActionCorrelation)` -> `models.OperationResultProperties withCorrelation(models.RunActionCorrelation)`
* `models.OperationResultProperties withStartTime(java.time.OffsetDateTime)` -> `models.OperationResultProperties withStartTime(java.time.OffsetDateTime)`

#### `models.LinuxJavaContainerSettings` was modified

* `LinuxJavaContainerSettings()` was changed to private access

#### `models.PrivateLinkResourceProperties` was modified

* `PrivateLinkResourceProperties()` was changed to private access

#### `models.ErrorInfo` was modified

* `models.ErrorInfo withCode(java.lang.String)` -> `models.ErrorInfo withCode(java.lang.String)`

#### `models.RunActionCorrelation` was modified

* `RunActionCorrelation()` was changed to private access
* `withClientTrackingId(java.lang.String)` was removed
* `withActionTrackingId(java.lang.String)` was removed
* `withClientKeywords(java.util.List)` was removed

#### `models.ExpressionRoot` was modified

* `ExpressionRoot()` was changed to private access
* `withText(java.lang.String)` was removed
* `withValue(java.lang.Object)` was removed
* `withError(models.AzureResourceErrorInfo)` was removed
* `withPath(java.lang.String)` was removed
* `withSubexpressions(java.util.List)` was removed

#### `models.PerfMonSet` was modified

* `PerfMonSet()` was changed to private access
* `withStartTime(java.time.OffsetDateTime)` was removed
* `withName(java.lang.String)` was removed
* `withValues(java.util.List)` was removed
* `withTimeGrain(java.lang.String)` was removed
* `withEndTime(java.time.OffsetDateTime)` was removed

#### `models.ContentLink` was modified

* `ContentLink()` was changed to private access
* `withUri(java.lang.String)` was removed

#### `models.DefaultErrorResponseError` was modified

* `DefaultErrorResponseError()` was changed to private access

#### `models.WebAppMinorVersion` was modified

* `WebAppMinorVersion()` was changed to private access

#### `models.ContainerThrottlingData` was modified

* `ContainerThrottlingData()` was changed to private access
* `withPeriods(java.lang.Integer)` was removed
* `withThrottledPeriods(java.lang.Integer)` was removed
* `withThrottledTime(java.lang.Integer)` was removed

#### `models.RetryHistory` was modified

* `RetryHistory()` was changed to private access
* `withStartTime(java.time.OffsetDateTime)` was removed
* `withClientRequestId(java.lang.String)` was removed
* `withCode(java.lang.String)` was removed
* `withServiceRequestId(java.lang.String)` was removed
* `withEndTime(java.time.OffsetDateTime)` was removed
* `withError(com.azure.core.management.exception.ManagementError)` was removed

#### `models.FunctionAppMajorVersion` was modified

* `FunctionAppMajorVersion()` was changed to private access

#### `models.SlotSwapStatus` was modified

* `SlotSwapStatus()` was changed to private access

#### `models.WebAppRuntimes` was modified

* `WebAppRuntimes()` was changed to private access

#### `models.SampleUtterance` was modified

* `SampleUtterance()` was changed to private access
* `withText(java.lang.String)` was removed
* `withQid(java.lang.String)` was removed
* `withLinks(java.util.List)` was removed

#### `models.CsmDeploymentStatus` was modified

* `CsmDeploymentStatus()` was changed to private access
* `withNumberOfInstancesFailed(java.lang.Integer)` was removed
* `withNumberOfInstancesSuccessful(java.lang.Integer)` was removed
* `withStatus(models.DeploymentBuildStatus)` was removed
* `withKind(java.lang.String)` was removed
* `withNumberOfInstancesInProgress(java.lang.Integer)` was removed
* `withDeploymentId(java.lang.String)` was removed
* `withFailedInstancesLogs(java.util.List)` was removed
* `withErrors(java.util.List)` was removed

#### `models.OperationResult` was modified

* `withStatus(models.WorkflowStatus)` was removed
* `withError(java.lang.Object)` was removed
* `withCode(java.lang.String)` was removed
* `withStartTime(java.time.OffsetDateTime)` was removed
* `models.OperationResult withIterationCount(java.lang.Integer)` -> `models.OperationResult withIterationCount(java.lang.Integer)`
* `models.OperationResult withRetryHistory(java.util.List)` -> `models.OperationResult withRetryHistory(java.util.List)`
* `withEndTime(java.time.OffsetDateTime)` was removed
* `withCorrelation(models.RunActionCorrelation)` was removed

#### `models.RunCorrelation` was modified

* `models.RunCorrelation withClientTrackingId(java.lang.String)` -> `models.RunCorrelation withClientTrackingId(java.lang.String)`
* `models.RunCorrelation withClientKeywords(java.util.List)` -> `models.RunCorrelation withClientKeywords(java.util.List)`

#### `models.StaticSiteDatabaseConnectionConfigurationFileOverview` was modified

* `StaticSiteDatabaseConnectionConfigurationFileOverview()` was changed to private access

### Features Added

* `models.SiteUpdateStrategyType` was added

* `models.FunctionsSiteUpdateStrategy` was added

* `models.NameIdentifier` was added

#### `models.TopLevelDomainCollection` was modified

* `withNextLink(java.lang.String)` was added

#### `models.TldLegalAgreementCollection` was modified

* `withNextLink(java.lang.String)` was added

#### `models.WorkflowResource` was modified

* `location()` was added
* `tags()` was added

#### `models.AzureResourceErrorInfo` was modified

* `code()` was added

#### `models.FunctionAppConfig` was modified

* `withSiteUpdateStrategy(models.FunctionsSiteUpdateStrategy)` was added
* `siteUpdateStrategy()` was added

#### `models.DomainOwnershipIdentifierCollection` was modified

* `withNextLink(java.lang.String)` was added

#### `models.AppServiceCertificateOrderCollection` was modified

* `withNextLink(java.lang.String)` was added

#### `AppServiceManager` was modified

* `certificateRegistrationClient()` was added
* `domainRegistrationClient()` was added

#### `models.AppServiceCertificateCollection` was modified

* `withNextLink(java.lang.String)` was added

#### `models.RunActionCorrelation` was modified

* `clientKeywords()` was added
* `clientTrackingId()` was added

#### `models.ExpressionRoot` was modified

* `subexpressions()` was added
* `value()` was added
* `text()` was added
* `error()` was added

#### `models.DomainCollection` was modified

* `withNextLink(java.lang.String)` was added

#### `models.CsmDeploymentStatus` was modified

* `kind()` was added
* `systemData()` was added

#### `models.OperationResult` was modified

* `code()` was added
* `error()` was added
* `startTime()` was added
* `status()` was added
* `endTime()` was added
* `correlation()` was added

#### `models.NameIdentifierCollection` was modified

* `withNextLink(java.lang.String)` was added

## 2.54.2 (2026-01-29)

### Other Changes

#### Dependency Updates

- Upgraded `azure-resourcemanager-storage` from `2.55.1` to version `2.55.2`.
- Upgraded `azure-resourcemanager-dns` from `2.53.4` to version `2.53.5`.
- Upgraded `azure-resourcemanager-keyvault` from `2.54.0` to version `2.54.1`.
- Upgraded `azure-resourcemanager-msi` from `2.53.4` to version `2.53.5`.

## 2.54.1 (2025-11-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.54.0 (2025-11-13)

### Other Changes

#### Dependency Updates

- Updated `api-version` of AppService to `2025-03-01`.

## 2.53.5 (2025-10-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.53.4 (2025-09-25)

### Other Changes

#### Dependency Updates

- Upgraded `azure-resourcemanager-resources` from `2.53.2` to version `2.53.3`.
- Upgraded `azure-resourcemanager-storage` from `2.54.0` to version `2.54.1`.
- Upgraded `azure-resourcemanager-dns` from `2.53.2` to version `2.53.3`.
- Upgraded `azure-resourcemanager-msi` from `2.53.2` to version `2.53.3`.

## 2.53.3 (2025-09-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.53.2 (2025-08-27)

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

### Breaking Changes

- Removed unused classes from ContainerApp.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2024-11-01`.

## 2.51.0 (2025-05-26)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.50.0 (2025-04-24)

### Features Added

- Supported Tomcat 11.0 and Java 11, 17, 21 in `RuntimeStack` for `WebApp`.

## 2.49.0 (2025-03-28)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.48.0 (2025-02-28)

### Features Added

- Supported Java SE 21 in `RuntimeStack` for `WebApp`.
- Supported Tomcat 10.1, JBoss EAP 8 and Java 21 in `RuntimeStack` for `WebApp`.
- Supported .Net 6, 8, 9 in `RuntimeStack` for `WebApp`.
- Supported PHP 8.2, 8.3 in `RuntimeStack` for `WebApp`.
- Supported Python 3.9, 3.10, 3.11, 3.12 in `RuntimeStack` for `WebApp`.

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

- Updated `api-version` to `2024-04-01`.

## 2.44.0 (2024-10-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.43.0 (2024-09-27)

### Features Added

- Added `deploy` and `pushDeploy` API to `FunctionApp` class for deploying zip file to FunctionApp of Flex Consumption plan.

## 2.42.0 (2024-08-23)

### Breaking Changes

- Type changed from `Float` to `Integer`, on `maximumInstanceCount` and `instanceMemoryMB` property in `FunctionsScaleAndConcurrency` class. 
- Type changed from `Float` to `Integer`, on `instanceCount` property in `FunctionsAlwaysReadyConfig` class.
- Type changed from `Float` to `Integer`, on `perInstanceConcurrency` property in `FunctionsScaleAndConcurrencyTriggersHttp` class.

### Other Changes

- Replaced `Jackson` with `azure-json` for serialization/deserialization.

## 2.41.0 (2024-07-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.40.0 (2024-06-24)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2023-12-01`.

## 2.39.0 (2024-05-24)

### Features Added

- Supported disabling public network access in `FunctionApp` via `disablePublicNetworkAccess()`, for private link feature.
- Supported disabling public network access in `DeploymentSlot` via `disablePublicNetworkAccess()`, for private link feature.
- Supported disabling public network access in `FunctionDeploymentSlot` via `disablePublicNetworkAccess()`, for private link feature.
- Added extra retry for Function App on ACA.

## 2.38.0 (2024-04-16)

### Features Added

- Supported disabling public network access in `WebApp` via `disablePublicNetworkAccess()`, for private link feature.

## 2.37.0 (2024-03-22)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.36.1 (2024-03-04)

### Bugs Fixed

- Fixed NullPointerException when updating Function App in Azure Container Apps.
- Fixed a bug that `withPrivateRegistryImage` doesn't work as expected for Function App in Azure Container Apps.

## 2.36.0 (2024-02-29)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.35.2 (2024-02-05)

### Bugs Fixed

- Fixed a bug that Function App in Azure Container Apps environment doesn't support creating from private container registry image.

## 2.35.1 (2024-01-31)

### Bugs Fixed

- Mitigated backend error when creating Function App in Azure Container Apps environment.

## 2.35.0 (2024-01-26)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.34.0 (2023-12-22)

### Features Added

- Supported Function App in Azure Container Apps environment.
  - Supported `withManagedEnvironmentId` for `FunctionApp`.
  - Supported `withMaxReplica` and `withMinReplica` for `FunctionApp`.

## 2.33.0 (2023-11-24)

### Bugs Fixed

- Deprecated `RuntimeStack.TOMCAT_10_0_JRE11`. Please use `RuntimeStack.TOMCAT_10_0_JAVA11`.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2023-01-01`.

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

### Features Added

- Supported `withContainerSize` for `FunctionApp`.

### Bugs Fixed

- Updated SKU that automatically set Function App "Always On".
Function App on `FREE`, `SHARED`, `DYNAMIC` (consumption plan), `ELASTIC_PREMIUM` (premium plan), `ELASTIC_ISOLATED` App Service has "Always On" turned off.

## 2.28.0 (2023-06-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.27.0 (2023-05-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.26.1 (2023-04-25)

### Breaking Changes

- Changed to use AAD Auth for Kudu deployment.

## 2.26.0 (2023-04-21)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.25.0 (2023-03-24)

### Bugs Fixed

- Fixed potential `NullPointerException`, when query tag on `WebApp` and `FunctionApp`. 

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2022-09-01`.

## 2.24.0 (2023-02-17)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.23.0 (2023-01-27)

### Other Changes

- Added "WEBSITE_CONTENTAZUREFILECONNECTIONSTRING" and "WEBSITE_CONTENTSHARE" app settings to FunctionApp of Linux Consumption plan and Premium plan.

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

### Features Added

- Supported Java SE 17 in `FunctionRuntimeStack` for `FunctionApp`.

## 2.17.0 (2022-07-25)

### Features Added

- Supported `getDeploymentStatus` in `SupportsOneDeploy`.

### Breaking Changes

- Merged multiple classes `AppServiceCertificateOrderPatchResourcePropertiesAppServiceCertificateNotRenewableReasonsItem`,
  `AppServiceCertificateOrderPropertiesAppServiceCertificateNotRenewableReasonsItem`, 
  `DomainPatchResourcePropertiesDomainNotRenewableReasonsItem` and `DomainPropertiesDomainNotRenewableReasonsItem` 
  into one class `ResourceNotRenewableReason`.
- `AppServiceEnvironmentPatchResource` was removed.
- `ValidateRequest` was removed.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2022-03-01`.

## 2.16.0 (2022-06-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.15.0 (2022-05-25)

### Features Added

- Supported `checkNameAvailability` method for `WebApps`.

### Breaking Changes

- Behavior changed on `WebApps.list()` and `WebApps.listByResourceGroup()` method, that results include sites with `kind` be empty or `linux`.

## 2.14.0 (2022-04-11)

###  Bugs Fixed

- Fixed a bug that `WebAppBase.getPublishingProfile()` failed to extract FTP profile, when web app is FTPS-only.
- Supported Java SE 17 in `RuntimeStack` for `WebApp`.

## 2.13.0 (2022-03-11)

### Features Added

- Supported Tomcat 10 and Java 8, 11, 17 in `RuntimeStack` for `WebApp`.

## 2.12.0 (2022-02-14)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources

## 2.11.0 (2022-01-17)

### Features Added

- Supported option for tracking deployment status via `pushDeploy` in `WebApp` and `DeploymentSlot`.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2021-03-01`.

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

- Updated `api-version` to `2021-02-01`.

### Features Added

- Supported `NODEJS_14_LTS` and `PHP_7_4` in `RuntimeStack`.

### Breaking Changes

- Renamed `ManagedServiceIdentityUserAssignedIdentities` class to `UserAssignedIdentity`.

## 2.7.0 (2021-08-12)

### Dependency Updates

- Updated core dependency from resources

## 2.6.0 (2021-06-18)

- Updated core dependency from resources

## 2.5.0 (2021-05-28)
- Supported the configuration of network access for `WebApp`, `FunctionApp`.

## 2.4.0 (2021-04-28)

- Updated `api-version` to `2020-12-01`
- Enum `IpFilterTag` changed to subclass of `ExpandableStringEnum`
- Major changes to `AppServiceEnvironment`
- Supported Private Link in `WebApp` and `FunctionApp`

## 2.3.0 (2021-03-30)

- Updated core dependency from resources

## 2.2.0 (2021-02-24)

- Updated core dependency from resources

## 2.1.0 (2020-11-24)

- Supported OneDeploy feature

## 2.0.0 (2020-10-19)

- Supported the configuration of container image for Windows web app.
- Supported the configuration of container image for deployment slot in update stage.
- Changed return type of `list` and `listByResourceGroup` in `WebApps`, `FunctionApps`, `DeploymentSlots`, `FunctionDeploymentSlots`.
- Added site properties for `WebApp`, `FunctionApp`, `DeploymentSlot`, `FunctionDeploymentSlot`.

## 2.0.0-beta.4 (2020-09-02)

- Fixed function app slot

# Release History

## 1.1.0 (2026-03-06)

- Azure Resource Manager CostManagement client library for Java. This package contains Microsoft Azure SDK for CostManagement Management SDK. CostManagement management client provides access to CostManagement resources for Azure Enterprise Subscriptions. Package api-version 2025-03-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.BenefitRecommendationsListResult` was removed

#### `models.OperationListResult` was removed

#### `models.ViewListResult` was removed

#### `models.BenefitUtilizationSummariesListResult` was removed

#### `models.ScheduledActionListResult` was removed

#### `models.DimensionsListResult` was removed

#### `models.ScheduledActionProxyResource` was removed

#### `models.AlertPropertiesDetails` was modified

* `validate()` was removed

#### `models.ForecastFilter` was modified

* `validate()` was removed

#### `models.ReportConfigSorting` was modified

* `validate()` was removed

#### `models.QueryDatasetConfiguration` was modified

* `validate()` was removed

#### `models.BenefitRecommendationProperties` was modified

* `models.BenefitRecommendationProperties withCommitmentGranularity(models.Grain)` -> `models.BenefitRecommendationProperties withCommitmentGranularity(models.Grain)`
* `models.BenefitRecommendationProperties withTerm(models.Term)` -> `models.BenefitRecommendationProperties withTerm(models.Term)`
* `models.BenefitRecommendationProperties withUsage(models.RecommendationUsageDetails)` -> `models.BenefitRecommendationProperties withUsage(models.RecommendationUsageDetails)`
* `validate()` was removed
* `models.BenefitRecommendationProperties withLookBackPeriod(models.LookBackPeriod)` -> `models.BenefitRecommendationProperties withLookBackPeriod(models.LookBackPeriod)`
* `models.BenefitRecommendationProperties withRecommendationDetails(models.AllSavingsBenefitDetails)` -> `models.BenefitRecommendationProperties withRecommendationDetails(models.AllSavingsBenefitDetails)`

#### `models.ExportDeliveryInfo` was modified

* `validate()` was removed

#### `models.Status` was modified

* `Status()` was changed to private access
* `validate()` was removed
* `withStatus(models.ReportOperationStatusType)` was removed

#### `models.BlobInfo` was modified

* `BlobInfo()` was changed to private access
* `withBlobLink(java.lang.String)` was removed
* `withByteCount(java.lang.Long)` was removed
* `validate()` was removed

#### `models.ReportConfigTimePeriod` was modified

* `validate()` was removed

#### `models.QueryDataset` was modified

* `validate()` was removed

#### `models.ForecastAggregation` was modified

* `validate()` was removed

#### `models.BenefitResource` was modified

* `validate()` was removed
* `models.BenefitResource withKind(models.BenefitKind)` -> `models.BenefitResource withKind(models.BenefitKind)`

#### `models.GenerateDetailedCostReportTimePeriod` was modified

* `validate()` was removed

#### `models.OperationDisplay` was modified

* `OperationDisplay()` was changed to private access
* `validate()` was removed

#### `models.CostManagementProxyResource` was modified

* `models.CostManagementProxyResource withEtag(java.lang.String)` -> `models.CostManagementProxyResource withEtag(java.lang.String)`
* `validate()` was removed

#### `models.QueryGrouping` was modified

* `validate()` was removed

#### `models.ReportConfigAggregation` was modified

* `validate()` was removed

#### `models.ReportConfigDataset` was modified

* `validate()` was removed

#### `models.ReportConfigDatasetConfiguration` was modified

* `validate()` was removed

#### `models.CostDetailsTimePeriod` was modified

* `validate()` was removed

#### `models.GenerateDetailedCostReportDefinition` was modified

* `validate()` was removed

#### `models.RecommendationUsageDetails` was modified

* `RecommendationUsageDetails()` was changed to private access
* `validate()` was removed
* `withUsageGrain(models.Grain)` was removed

#### `models.QueryDefinition` was modified

* `validate()` was removed

#### `models.SavingsPlanUtilizationSummary` was modified

* `SavingsPlanUtilizationSummary()` was changed to private access
* `validate()` was removed
* `withBenefitType(models.BenefitKind)` was removed

#### `models.Exports` was modified

* `executeWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.ForecastColumn` was modified

* `ForecastColumn()` was changed to private access
* `validate()` was removed
* `withName(java.lang.String)` was removed
* `withType(java.lang.String)` was removed

#### `models.ReportConfigFilter` was modified

* `validate()` was removed

#### `models.BenefitUtilizationSummaryProperties` was modified

* `validate()` was removed
* `models.BenefitUtilizationSummaryProperties withBenefitType(models.BenefitKind)` -> `models.BenefitUtilizationSummaryProperties withBenefitType(models.BenefitKind)`

#### `models.QueryComparisonExpression` was modified

* `validate()` was removed

#### `models.CostDetailsMetricType` was modified

* `AMORTIZED_COST` was removed
* `ACTUAL_COST` was removed

#### `models.QueryColumn` was modified

* `QueryColumn()` was changed to private access
* `withName(java.lang.String)` was removed
* `withType(java.lang.String)` was removed
* `validate()` was removed

#### `models.CostManagementResource` was modified

* `models.CostManagementResource withLocation(java.lang.String)` -> `models.CostManagementResource withLocation(java.lang.String)`
* `withTags(java.util.Map)` was removed
* `models.CostManagementResource withTags(java.util.Map)` -> `models.CostManagementResource withTags(java.util.Map)`
* `withLocation(java.lang.String)` was removed
* `validate()` was removed

#### `CostManagementManager` was modified

* `fluent.CostManagementClient serviceClient()` -> `fluent.CostManagementManagementClient serviceClient()`

#### `models.ForecastTimePeriod` was modified

* `validate()` was removed

#### `models.SingleScopeBenefitRecommendationProperties` was modified

* `SingleScopeBenefitRecommendationProperties()` was changed to private access
* `withCommitmentGranularity(models.Grain)` was removed
* `withTerm(models.Term)` was removed
* `withLookBackPeriod(models.LookBackPeriod)` was removed
* `withRecommendationDetails(models.AllSavingsBenefitDetails)` was removed
* `withUsage(models.RecommendationUsageDetails)` was removed
* `validate()` was removed

#### `models.ExportSchedule` was modified

* `validate()` was removed

#### `models.PivotProperties` was modified

* `validate()` was removed

#### `models.CheckNameAvailabilityRequest` was modified

* `validate()` was removed

#### `models.DismissAlertPayload` was modified

* `validate()` was removed

#### `models.KpiProperties` was modified

* `validate()` was removed

#### `models.GenerateCostDetailsReportRequestDefinition` was modified

* `validate()` was removed

#### `models.Export` was modified

* `executeWithResponse(com.azure.core.util.Context)` was removed

#### `models.ExportTimePeriod` was modified

* `validate()` was removed

#### `models.ForecastDefinition` was modified

* `validate()` was removed

#### `models.ForecastComparisonExpression` was modified

* `validate()` was removed

#### `models.ExportDatasetConfiguration` was modified

* `validate()` was removed

#### `models.ForecastDataset` was modified

* `validate()` was removed

#### `models.ExportRecurrencePeriod` was modified

* `validate()` was removed

#### `models.Operation` was modified

* `validate()` was removed
* `models.Operation withDisplay(models.OperationDisplay)` -> `models.Operation withDisplay(models.OperationDisplay)`

#### `models.PriceSheets` was modified

* `models.DownloadUrl downloadByBillingProfile(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.PricesheetDownloadProperties downloadByBillingProfile(java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `download(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `models.DownloadUrl downloadByBillingProfile(java.lang.String,java.lang.String)` -> `models.PricesheetDownloadProperties downloadByBillingProfile(java.lang.String,java.lang.String)`
* `download(java.lang.String,java.lang.String,java.lang.String)` was removed

#### `models.ExportDeliveryDestination` was modified

* `validate()` was removed

#### `models.QueryFilter` was modified

* `validate()` was removed

#### `models.CostDetailsStatusType` was modified

* `COMPLETED` was removed
* `FAILED` was removed
* `NO_DATA_FOUND` was removed

#### `models.ReportConfigComparisonExpression` was modified

* `validate()` was removed

#### `models.QueryAggregation` was modified

* `validate()` was removed

#### `models.CostDetailsDataFormat` was modified

* `CSV` was removed

#### `models.ExportDataset` was modified

* `validate()` was removed

#### `models.ExportDefinition` was modified

* `validate()` was removed

#### `models.AllSavingsList` was modified

* `AllSavingsList()` was changed to private access
* `validate()` was removed

#### `models.QueryTimePeriod` was modified

* `validate()` was removed

#### `models.ForecastDatasetConfiguration` was modified

* `validate()` was removed

#### `models.AllSavingsBenefitDetails` was modified

* `AllSavingsBenefitDetails()` was changed to private access
* `validate()` was removed

#### `models.IncludedQuantityUtilizationSummary` was modified

* `IncludedQuantityUtilizationSummary()` was changed to private access
* `validate()` was removed
* `withBenefitType(models.BenefitKind)` was removed

#### `models.SharedScopeBenefitRecommendationProperties` was modified

* `SharedScopeBenefitRecommendationProperties()` was changed to private access
* `withLookBackPeriod(models.LookBackPeriod)` was removed
* `withUsage(models.RecommendationUsageDetails)` was removed
* `withTerm(models.Term)` was removed
* `withRecommendationDetails(models.AllSavingsBenefitDetails)` was removed
* `validate()` was removed
* `withCommitmentGranularity(models.Grain)` was removed

#### `models.ReportConfigGrouping` was modified

* `validate()` was removed

#### `models.ScheduleProperties` was modified

* `validate()` was removed

#### `models.NotificationProperties` was modified

* `validate()` was removed

#### `models.FileDestination` was modified

* `validate()` was removed

#### `models.AlertPropertiesDefinition` was modified

* `validate()` was removed

### Features Added

* `models.Budget$Update` was added

* `models.CostAllocationRuleDetails` was added

* `models.BudgetFilter` was added

* `models.AsyncOperationStatusProperties` was added

* `models.FilterItemNames` was added

* `models.CostAllocationRuleCheckNameAvailabilityResponse` was added

* `models.ExportSuspensionContext` was added

* `models.ExportRunRequest` was added

* `models.CostAllocationRuleDefinition` was added

* `models.CompressionModeType` was added

* `models.CostAllocationRuleCheckNameAvailabilityRequest` was added

* `models.CostAllocationResource` was added

* `models.CostAllocationProportion` was added

* `models.BudgetComparisonExpression` was added

* `models.FilterItems` was added

* `models.SourceCostAllocationResource` was added

* `models.SystemAssignedServiceIdentity` was added

* `models.CostAllocationPolicyType` was added

* `models.BudgetOperatorType` was added

* `models.TimeGrainType` was added

* `models.CultureCode` was added

* `models.BudgetTimePeriod` was added

* `models.BenefitUtilizationSummariesOperationStatus` was added

* `models.CurrentSpend` was added

* `models.SettingsKind` was added

* `models.TagInheritanceProperties` was added

* `models.Notification` was added

* `models.BudgetFilterProperties` was added

* `models.BenefitUtilizationSummaryReportSchema` was added

* `models.Budgets` was added

* `models.MCAPriceSheetProperties` was added

* `models.Budget$UpdateStages` was added

* `models.PricesheetDownloadProperties` was added

* `models.BudgetNotificationOperatorType` was added

* `models.CostAllocationRuleProperties` was added

* `models.DestinationType` was added

* `models.TagInheritanceSetting` was added

* `models.Reason` was added

* `models.Setting` was added

* `models.SystemAssignedServiceIdentityType` was added

* `models.ForecastSpend` was added

* `models.Budget$DefinitionStages` was added

* `models.CategoryType` was added

* `models.CostAllocationResourceType` was added

* `models.BenefitUtilizationSummariesRequest` was added

* `models.GenerateBenefitUtilizationSummariesReports` was added

* `models.DataOverwriteBehaviorType` was added

* `models.CostAllocationRules` was added

* `models.RuleStatus` was added

* `models.Settings` was added

* `models.Frequency` was added

* `models.TargetCostAllocationResource` was added

* `models.SettingType` was added

* `models.Budget$Definition` was added

* `models.ThresholdType` was added

* `models.Budget` was added

#### `models.View$Update` was modified

* `withModifiedOn(java.time.OffsetDateTime)` was added

#### `models.View` was modified

* `systemData()` was added

#### `models.GenerateDetailedCostReportOperationResult` was modified

* `systemData()` was added

#### `models.BenefitResource` was modified

* `systemData()` was added

#### `models.View$Definition` was modified

* `withModifiedOn(java.time.OffsetDateTime)` was added

#### `models.CommonExportProperties` was modified

* `systemSuspensionContext()` was added
* `exportDescription()` was added
* `compressionMode()` was added
* `dataOverwriteBehavior()` was added

#### `models.SavingsPlanUtilizationSummary` was modified

* `systemData()` was added

#### `models.Exports` was modified

* `executeWithResponse(java.lang.String,java.lang.String,models.ExportRunRequest,com.azure.core.util.Context)` was added

#### `models.FormatType` was modified

* `PARQUET` was added

#### `models.Export$Definition` was modified

* `withDataOverwriteBehavior(models.DataOverwriteBehaviorType)` was added
* `withIdentity(models.SystemAssignedServiceIdentity)` was added
* `withCompressionMode(models.CompressionModeType)` was added
* `withExportDescription(java.lang.String)` was added
* `withRegion(java.lang.String)` was added
* `withRegion(com.azure.core.management.Region)` was added

#### `models.CostDetailsMetricType` was modified

* `AMORTIZED_COST_COST_DETAILS_METRIC_TYPE` was added
* `ACTUAL_COST_COST_DETAILS_METRIC_TYPE` was added

#### `models.CostManagementResource` was modified

* `tags()` was added
* `location()` was added

#### `CostManagementManager` was modified

* `costAllocationRules()` was added
* `generateBenefitUtilizationSummariesReports()` was added
* `budgets()` was added
* `settings()` was added

#### `models.Export` was modified

* `systemData()` was added
* `location()` was added
* `exportDescription()` was added
* `compressionMode()` was added
* `dataOverwriteBehavior()` was added
* `region()` was added
* `regionName()` was added
* `systemSuspensionContext()` was added
* `identity()` was added
* `executeWithResponse(models.ExportRunRequest,com.azure.core.util.Context)` was added

#### `models.ExportType` was modified

* `FOCUS_COST` was added
* `PRICE_SHEET` was added
* `RESERVATION_RECOMMENDATIONS` was added
* `RESERVATION_TRANSACTIONS` was added
* `RESERVATION_DETAILS` was added

#### `models.ExportRun` was modified

* `manifestFile()` was added
* `startDate()` was added
* `endDate()` was added

#### `models.ExportRunProperties` was modified

* `endDate()` was added
* `manifestFile()` was added
* `startDate()` was added

#### `models.ExportDatasetConfiguration` was modified

* `filters()` was added
* `dataVersion()` was added
* `withDataVersion(java.lang.String)` was added
* `withFilters(java.util.List)` was added

#### `models.BenefitUtilizationSummary` was modified

* `systemData()` was added

#### `models.TimeframeType` was modified

* `THE_CURRENT_MONTH` was added

#### `models.PriceSheets` was modified

* `downloadByBillingAccount(java.lang.String,java.lang.String)` was added
* `downloadByInvoice(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `downloadByBillingAccount(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `downloadByInvoice(java.lang.String,java.lang.String,java.lang.String)` was added

#### `models.ExportDeliveryDestination` was modified

* `type()` was added
* `withType(models.DestinationType)` was added

#### `models.CostDetailsStatusType` was modified

* `NO_DATA_FOUND_COST_DETAILS_STATUS_TYPE` was added
* `FAILED_COST_DETAILS_STATUS_TYPE` was added
* `COMPLETED_COST_DETAILS_STATUS_TYPE` was added

#### `models.BenefitRecommendationModel` was modified

* `systemData()` was added

#### `models.CostDetailsDataFormat` was modified

* `CSV_COST_DETAILS_DATA_FORMAT` was added

#### `models.Export$Update` was modified

* `withCompressionMode(models.CompressionModeType)` was added
* `withExportDescription(java.lang.String)` was added
* `withDataOverwriteBehavior(models.DataOverwriteBehaviorType)` was added
* `withIdentity(models.SystemAssignedServiceIdentity)` was added

#### `models.GranularityType` was modified

* `MONTHLY` was added

#### `models.ScheduledAction$Definition` was modified

* `withEtag(java.lang.String)` was added

#### `models.GenerateDetailedCostReportOperationStatuses` was modified

* `systemData()` was added

#### `models.Alert` was modified

* `systemData()` was added

#### `models.IncludedQuantityUtilizationSummary` was modified

* `systemData()` was added

#### `models.ScheduledAction$Update` was modified

* `withEtag(java.lang.String)` was added

## 1.0.0 (2024-12-24)

- Azure Resource Manager CostManagement client library for Java. This package contains Microsoft Azure SDK for CostManagement Management SDK. CostManagement management client provides access to CostManagement resources for Azure Enterprise Subscriptions. Package tag package-2022-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.SingleScopeBenefitRecommendationProperties` was modified

* `currencyCode()` was removed
* `costWithoutBenefit()` was removed
* `lastConsumptionDate()` was removed
* `totalHours()` was removed
* `firstConsumptionDate()` was removed
* `allRecommendationDetails()` was removed
* `armSkuName()` was removed

#### `models.SharedScopeBenefitRecommendationProperties` was modified

* `allRecommendationDetails()` was removed
* `firstConsumptionDate()` was removed
* `costWithoutBenefit()` was removed
* `totalHours()` was removed
* `lastConsumptionDate()` was removed
* `currencyCode()` was removed
* `armSkuName()` was removed

## 1.0.0-beta.7 (2024-10-31)

- Azure Resource Manager CostManagement client library for Java. This package contains Microsoft Azure SDK for CostManagement Management SDK. CostManagement management client provides access to CostManagement resources for Azure Enterprise Subscriptions. Package tag package-2022-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.View$Update` was modified

* `withDateRange(java.lang.String)` was added

#### `models.AlertPropertiesDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ForecastFilter` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ReportConfigSorting` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.QueryDatasetConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BenefitRecommendationProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `scope()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ExportDeliveryInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Status` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BlobInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ReportConfigTimePeriod` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.QueryDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ForecastAggregation` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BenefitResource` was modified

* `id()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `name()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `type()` was added

#### `models.GenerateDetailedCostReportTimePeriod` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CostManagementProxyResource` was modified

* `type()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `id()` was added
* `name()` was added

#### `models.QueryGrouping` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ReportConfigAggregation` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ReportConfigDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ReportConfigDatasetConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.View$Definition` was modified

* `withDateRange(java.lang.String)` was added

#### `models.BenefitRecommendationsListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CostDetailsTimePeriod` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.GenerateDetailedCostReportDefinition` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RecommendationUsageDetails` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.QueryDefinition` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SavingsPlanUtilizationSummary` was modified

* `kind()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `id()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `type()` was added
* `name()` was added

#### `models.ForecastColumn` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ReportConfigFilter` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BenefitUtilizationSummaryProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.QueryComparisonExpression` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.QueryColumn` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CostManagementResource` was modified

* `name()` was added
* `id()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `type()` was added

#### `models.ForecastTimePeriod` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SingleScopeBenefitRecommendationProperties` was modified

* `firstConsumptionDate()` was added
* `currencyCode()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `allRecommendationDetails()` was added
* `lastConsumptionDate()` was added
* `totalHours()` was added
* `armSkuName()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `costWithoutBenefit()` was added
* `scope()` was added

#### `models.ViewListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ExportSchedule` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PivotProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CheckNameAvailabilityRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DismissAlertPayload` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.KpiProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.GenerateCostDetailsReportRequestDefinition` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ExportTimePeriod` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ForecastDefinition` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ForecastComparisonExpression` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ExportDatasetConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BenefitUtilizationSummariesListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ForecastDataset` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BenefitUtilizationSummary` was modified

* `kind()` was added

#### `models.ExportRecurrencePeriod` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Operation` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ExportDeliveryDestination` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.QueryFilter` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ScheduledActionListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ReportConfigComparisonExpression` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DimensionsListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.QueryAggregation` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ExportDataset` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ExportDefinition` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AllSavingsList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.QueryTimePeriod` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ForecastDatasetConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AllSavingsBenefitDetails` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.IncludedQuantityUtilizationSummary` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `kind()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `type()` was added
* `id()` was added
* `name()` was added

#### `models.ScheduledActionProxyResource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `type()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `id()` was added
* `name()` was added

#### `models.SharedScopeBenefitRecommendationProperties` was modified

* `allRecommendationDetails()` was added
* `firstConsumptionDate()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `currencyCode()` was added
* `costWithoutBenefit()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `totalHours()` was added
* `scope()` was added
* `armSkuName()` was added
* `lastConsumptionDate()` was added

#### `models.ReportConfigGrouping` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ScheduleProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.NotificationProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.FileDestination` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AlertPropertiesDefinition` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

## 1.0.0-beta.6 (2023-05-22)

- Azure Resource Manager CostManagement client library for Java. This package contains Microsoft Azure SDK for CostManagement Management SDK. CostManagement management client provides access to CostManagement resources for Azure Enterprise Subscriptions. Package tag package-2022-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ScheduledActions` was modified

* `createOrUpdateWithResponse(java.lang.String,fluent.models.ScheduledActionInner,com.azure.core.util.Context)` was removed

### Features Added

#### `models.ScheduledActions` was modified

* `createOrUpdateWithResponse(java.lang.String,fluent.models.ScheduledActionInner,java.lang.String,com.azure.core.util.Context)` was added

#### `models.ScheduledAction$Definition` was modified

* `withIfMatch(java.lang.String)` was added

#### `models.GenerateDetailedCostReportOperationStatuses` was modified

* `endTime()` was added
* `startTime()` was added

#### `models.ScheduledAction$Update` was modified

* `withIfMatch(java.lang.String)` was added

## 1.0.0-beta.5 (2023-02-10)

- Azure Resource Manager CostManagement client library for Java. This package contains Microsoft Azure SDK for CostManagement Management SDK. CostManagement management client provides access to CostManagement resources for Azure Enterprise Subscriptions. Package tag package-2022-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ReportConfigSortingDirection` was removed

* `models.ExportExecutionProperties` was removed

* `models.ForecastTimeframeType` was removed

* `models.ReportConfigColumnType` was removed

* `models.ExportExecution` was removed

#### `models.View$Update` was modified

* `withDataset(models.ReportConfigDataset)` was removed

#### `models.View` was modified

* `dataset()` was removed

#### `models.ReportConfigSorting` was modified

* `withDirection(models.ReportConfigSortingDirection)` was removed
* `models.ReportConfigSortingDirection direction()` -> `models.ReportConfigSortingType direction()`

#### `models.View$Definition` was modified

* `withDataset(models.ReportConfigDataset)` was removed

#### `models.Forecasts` was modified

* `models.QueryResult usage(java.lang.String,models.ForecastDefinition)` -> `models.ForecastResult usage(java.lang.String,models.ForecastDefinition)`
* `models.QueryResult externalCloudProviderUsage(models.ExternalCloudProviderType,java.lang.String,models.ForecastDefinition)` -> `models.ForecastResult externalCloudProviderUsage(models.ExternalCloudProviderType,java.lang.String,models.ForecastDefinition)`

#### `models.ReportConfigFilter` was modified

* `tag()` was removed
* `not()` was removed
* `withTag(models.ReportConfigComparisonExpression)` was removed
* `withDimension(models.ReportConfigComparisonExpression)` was removed
* `dimension()` was removed
* `withNot(models.ReportConfigFilter)` was removed

#### `models.ForecastDefinition` was modified

* `withTimeframe(models.ForecastTimeframeType)` was removed
* `models.ForecastTimeframeType timeframe()` -> `models.ForecastTimeframe timeframe()`
* `models.QueryTimePeriod timePeriod()` -> `models.ForecastTimePeriod timePeriod()`
* `withTimePeriod(models.QueryTimePeriod)` was removed

#### `models.ForecastDataset` was modified

* `withFilter(models.QueryFilter)` was removed
* `models.QueryFilter filter()` -> `models.ForecastFilter filter()`
* `models.QueryDatasetConfiguration configuration()` -> `models.ForecastDatasetConfiguration configuration()`
* `withConfiguration(models.QueryDatasetConfiguration)` was removed

#### `models.Operation` was modified

* `java.lang.String name()` -> `java.lang.String name()`
* `innerModel()` was removed
* `models.OperationDisplay display()` -> `models.OperationDisplay display()`

#### `models.QueryFilter` was modified

* `dimension()` was removed
* `withTag(models.QueryComparisonExpression)` was removed
* `not()` was removed
* `tag()` was removed
* `withDimension(models.QueryComparisonExpression)` was removed
* `withNot(models.QueryFilter)` was removed

#### `models.Alert` was modified

* `tags()` was removed

#### `models.ReportConfigGrouping` was modified

* `withType(models.ReportConfigColumnType)` was removed
* `models.ReportConfigColumnType type()` -> `models.QueryColumnType type()`

### Features Added

* `models.GenerateReservationDetailsReports` was added

* `models.ReportOperationStatusType` was added

* `models.GenerateDetailedCostReportOperationResults` was added

* `models.ForecastFilter` was added

* `models.DownloadUrl` was added

* `models.BenefitRecommendationProperties` was added

* `models.GenerateDetailedCostReportOperationResult` was added

* `models.Status` was added

* `models.BlobInfo` was added

* `models.BenefitKind` was added

* `models.ScheduledActionStatus` was added

* `models.CheckNameAvailabilityReason` was added

* `models.Grain` was added

* `models.ForecastAggregation` was added

* `models.ScheduleFrequency` was added

* `models.BenefitResource` was added

* `models.DaysOfWeek` was added

* `models.BenefitUtilizationSummaries` was added

* `models.GenerateDetailedCostReportTimePeriod` was added

* `models.CostManagementProxyResource` was added

* `models.ForecastTimeframe` was added

* `models.FunctionName` was added

* `models.FileFormat` was added

* `models.BenefitRecommendationsListResult` was added

* `models.CostDetailsTimePeriod` was added

* `models.GenerateDetailedCostReportDefinition` was added

* `models.RecommendationUsageDetails` was added

* `models.SavingsPlanUtilizationSummary` was added

* `models.ReservationReportSchema` was added

* `models.ForecastColumn` was added

* `models.ScheduledActions` was added

* `models.GenerateDetailedCostReports` was added

* `models.ScheduledAction` was added

* `models.BenefitUtilizationSummaryProperties` was added

* `models.Term` was added

* `models.OperationStatus` was added

* `models.CostDetailsMetricType` was added

* `models.GenerateDetailedCostReportMetricType` was added

* `models.CostManagementResource` was added

* `models.LookBackPeriod` was added

* `models.ForecastResult` was added

* `models.ForecastTimePeriod` was added

* `models.SingleScopeBenefitRecommendationProperties` was added

* `models.CostDetailsOperationResults` was added

* `models.CheckNameAvailabilityRequest` was added

* `models.GenerateCostDetailsReportRequestDefinition` was added

* `models.ActionType` was added

* `models.WeeksOfMonth` was added

* `models.ForecastOperatorType` was added

* `models.ExportRun` was added

* `models.GenerateDetailedCostReportOperationStatus` was added

* `models.GrainParameter` was added

* `models.ExportRunProperties` was added

* `models.ForecastComparisonExpression` was added

* `models.BenefitUtilizationSummariesListResult` was added

* `models.ScheduledAction$DefinitionStages` was added

* `models.BenefitUtilizationSummary` was added

* `models.BenefitRecommendations` was added

* `models.PriceSheets` was added

* `models.CostDetailsStatusType` was added

* `models.BenefitRecommendationModel` was added

* `models.ScheduledActionListResult` was added

* `models.OperationStatusType` was added

* `models.CostDetailsDataFormat` was added

* `models.GenerateCostDetailsReports` was added

* `models.ScheduledActionKind` was added

* `models.CostManagementOperation` was added

* `models.ScheduledAction$Definition` was added

* `models.AllSavingsList` was added

* `models.ScheduledAction$UpdateStages` was added

* `models.CheckNameAvailabilityResponse` was added

* `models.ForecastDatasetConfiguration` was added

* `models.GenerateDetailedCostReportOperationStatuses` was added

* `models.AllSavingsBenefitDetails` was added

* `models.Origin` was added

* `models.Scope` was added

* `models.IncludedQuantityUtilizationSummary` was added

* `models.ScheduledActionProxyResource` was added

* `models.SharedScopeBenefitRecommendationProperties` was added

* `models.ScheduledAction$Update` was added

* `models.ScheduleProperties` was added

* `models.NotificationProperties` was added

* `models.FileDestination` was added

* `models.ReportConfigSortingType` was added

#### `models.View$Update` was modified

* `withDataSet(models.ReportConfigDataset)` was added
* `withIncludeMonetaryCommitment(java.lang.Boolean)` was added

#### `models.View` was modified

* `dateRange()` was added
* `includeMonetaryCommitment()` was added
* `dataSet()` was added
* `currency()` was added

#### `models.AlertPropertiesDetails` was modified

* `departmentName()` was added
* `companyName()` was added
* `withDepartmentName(java.lang.String)` was added
* `withCompanyName(java.lang.String)` was added
* `enrollmentNumber()` was added
* `invoicingThreshold()` was added
* `withEnrollmentNumber(java.lang.String)` was added
* `withEnrollmentEndDate(java.lang.String)` was added
* `enrollmentEndDate()` was added
* `withEnrollmentStartDate(java.lang.String)` was added
* `withInvoicingThreshold(java.math.BigDecimal)` was added
* `enrollmentStartDate()` was added

#### `models.ReportConfigSorting` was modified

* `withDirection(models.ReportConfigSortingType)` was added

#### `models.OperationDisplay` was modified

* `description()` was added

#### `models.View$Definition` was modified

* `withDataSet(models.ReportConfigDataset)` was added
* `withIncludeMonetaryCommitment(java.lang.Boolean)` was added

#### `models.CommonExportProperties` was modified

* `partitionData()` was added

#### `models.Export$Definition` was modified

* `withPartitionData(java.lang.Boolean)` was added

#### `models.ReportConfigFilter` was modified

* `withTags(models.ReportConfigComparisonExpression)` was added
* `tags()` was added
* `dimensions()` was added
* `withDimensions(models.ReportConfigComparisonExpression)` was added

#### `models.QueryResult` was modified

* `location()` was added
* `etag()` was added
* `sku()` was added

#### `CostManagementManager` was modified

* `priceSheets()` was added
* `benefitUtilizationSummaries()` was added
* `generateDetailedCostReportOperationStatus()` was added
* `generateCostDetailsReports()` was added
* `benefitRecommendations()` was added
* `generateDetailedCostReportOperationResults()` was added
* `generateDetailedCostReports()` was added
* `scheduledActions()` was added
* `generateReservationDetailsReports()` was added

#### `models.Dimension` was modified

* `sku()` was added
* `location()` was added
* `etag()` was added

#### `models.Export` was modified

* `partitionData()` was added

#### `models.ForecastDefinition` was modified

* `withTimePeriod(models.ForecastTimePeriod)` was added
* `withTimeframe(models.ForecastTimeframe)` was added

#### `models.ForecastDataset` was modified

* `withFilter(models.ForecastFilter)` was added
* `withConfiguration(models.ForecastDatasetConfiguration)` was added

#### `models.Operation` was modified

* `withDisplay(models.OperationDisplay)` was added
* `isDataAction()` was added
* `origin()` was added
* `validate()` was added
* `actionType()` was added

#### `models.ExportDeliveryDestination` was modified

* `sasToken()` was added
* `storageAccount()` was added
* `withSasToken(java.lang.String)` was added
* `withStorageAccount(java.lang.String)` was added

#### `models.QueryFilter` was modified

* `withTags(models.QueryComparisonExpression)` was added
* `withDimensions(models.QueryComparisonExpression)` was added
* `dimensions()` was added
* `tags()` was added

#### `models.Export$Update` was modified

* `withPartitionData(java.lang.Boolean)` was added

#### `models.Alert` was modified

* `etag()` was added

#### `models.ReportConfigGrouping` was modified

* `withType(models.QueryColumnType)` was added

## 1.0.0-beta.4 (2023-01-13)

- Azure Resource Manager CostManagement client library for Java. This package contains Microsoft Azure SDK for CostManagement Management SDK.  Package tag package-2020-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.CacheItem` was removed

* `models.GenerateReservationDetailsReports` was removed

* `models.GenerateReservationDetailsReportsByBillingProfileIdHeaders` was removed

* `models.GenerateReservationDetailsReportsByBillingAccountIdResponse` was removed

* `models.Status` was removed

* `models.SettingsPropertiesStartOn` was removed

* `models.Setting` was removed

* `models.GenerateReservationDetailsReportsByBillingAccountIdHeaders` was removed

* `models.OperationStatusType` was removed

* `models.QueryFilterAutoGenerated` was removed

* `models.ProxySettingResource` was removed

* `models.GenerateReservationDetailsReportsByBillingProfileIdResponse` was removed

* `models.Settings` was removed

* `models.SettingsListResult` was removed

* `models.OperationStatus` was removed

* `models.QueryDatasetAutoGenerated` was removed

#### `CostManagementManager` was modified

* `generateReservationDetailsReports()` was removed
* `settings()` was removed

#### `models.View$Update` was modified

* `withDataSet(models.ReportConfigDataset)` was removed

#### `models.View` was modified

* `dateRange()` was removed
* `includeMonetaryCommitment()` was removed
* `dataSet()` was removed
* `currency()` was removed

#### `models.ExportExecution` was modified

* `withRunSettings(models.CommonExportProperties)` was removed
* `java.time.OffsetDateTime processingStartTime()` -> `java.time.OffsetDateTime processingStartTime()`
* `tags()` was removed
* `models.ExecutionType executionType()` -> `models.ExecutionType executionType()`
* `models.ExecutionStatus status()` -> `models.ExecutionStatus status()`
* `java.lang.String submittedBy()` -> `java.lang.String submittedBy()`
* `withSubmittedTime(java.time.OffsetDateTime)` was removed
* `java.time.OffsetDateTime processingEndTime()` -> `java.time.OffsetDateTime processingEndTime()`
* `withProcessingStartTime(java.time.OffsetDateTime)` was removed
* `java.time.OffsetDateTime submittedTime()` -> `java.time.OffsetDateTime submittedTime()`
* `models.CommonExportProperties runSettings()` -> `models.CommonExportProperties runSettings()`
* `withStatus(models.ExecutionStatus)` was removed
* `java.lang.String fileName()` -> `java.lang.String fileName()`
* `withFileName(java.lang.String)` was removed
* `withProcessingEndTime(java.time.OffsetDateTime)` was removed
* `withSubmittedBy(java.lang.String)` was removed
* `validate()` was removed
* `withExecutionType(models.ExecutionType)` was removed

#### `models.ForecastDefinition` was modified

* `withDataset(models.QueryDataset)` was removed
* `models.QueryDataset dataset()` -> `models.ForecastDataset dataset()`

#### `models.QueryFilter` was modified

* `withTags(models.QueryComparisonExpression)` was removed
* `dimensions()` was removed
* `withDimensions(models.QueryComparisonExpression)` was removed
* `tags()` was removed

#### `models.ExportDefinition` was modified

* `models.QueryDatasetAutoGenerated dataSet()` -> `models.ExportDataset dataSet()`
* `models.QueryTimePeriod timePeriod()` -> `models.ExportTimePeriod timePeriod()`
* `withDataSet(models.QueryDatasetAutoGenerated)` was removed
* `withTimePeriod(models.QueryTimePeriod)` was removed

#### `models.View$Definition` was modified

* `withDataSet(models.ReportConfigDataset)` was removed

#### `models.CommonExportProperties` was modified

* `models.ExportDeliveryInfo deliveryInfo()` -> `models.ExportDeliveryInfo deliveryInfo()`
* `models.FormatType format()` -> `models.FormatType format()`
* `withFormat(models.FormatType)` was removed
* `models.ExportDefinition definition()` -> `models.ExportDefinition definition()`
* `validate()` was removed
* `withDefinition(models.ExportDefinition)` was removed
* `withDeliveryInfo(models.ExportDeliveryInfo)` was removed

#### `models.Exports` was modified

* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `listWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `getWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.ReportConfigFilter` was modified

* `dimensions()` was removed
* `withTagValue(models.ReportConfigComparisonExpression)` was removed
* `tagKey()` was removed
* `withTags(models.ReportConfigComparisonExpression)` was removed
* `tags()` was removed
* `withDimensions(models.ReportConfigComparisonExpression)` was removed
* `withTagKey(models.ReportConfigComparisonExpression)` was removed
* `tagValue()` was removed

#### `models.QueryComparisonExpression` was modified

* `withOperator(models.OperatorType)` was removed
* `models.OperatorType operator()` -> `models.QueryOperatorType operator()`

#### `models.QueryResult` was modified

* `location()` was removed
* `etag()` was removed
* `sku()` was removed

### Features Added

* `models.QueryOperatorType` was added

* `models.ExportTimePeriod` was added

* `models.ExportExecutionProperties` was added

* `models.ExportDatasetConfiguration` was added

* `models.ForecastDataset` was added

* `models.ExportDataset` was added

#### `CostManagementManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.View$Update` was modified

* `withDataset(models.ReportConfigDataset)` was added

#### `models.View` was modified

* `dataset()` was added

#### `CostManagementManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.Export` was modified

* `nextRunTimeEstimate()` was added
* `runHistory()` was added

#### `models.ExportExecution` was modified

* `etag()` was added
* `id()` was added
* `error()` was added
* `type()` was added
* `innerModel()` was added
* `name()` was added

#### `models.ForecastDefinition` was modified

* `withDataset(models.ForecastDataset)` was added

#### `models.QueryFilter` was modified

* `withDimension(models.QueryComparisonExpression)` was added
* `dimension()` was added
* `withTag(models.QueryComparisonExpression)` was added
* `not()` was added
* `tag()` was added
* `withNot(models.QueryFilter)` was added

#### `models.Export$Update` was modified

* `withRunHistory(fluent.models.ExportExecutionListResultInner)` was added

#### `models.ExportDefinition` was modified

* `withDataSet(models.ExportDataset)` was added
* `withTimePeriod(models.ExportTimePeriod)` was added

#### `models.View$Definition` was modified

* `withDataset(models.ReportConfigDataset)` was added

#### `models.CommonExportProperties` was modified

* `innerModel()` was added
* `nextRunTimeEstimate()` was added
* `runHistory()` was added

#### `models.Exports` was modified

* `getByIdWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Export$Definition` was modified

* `withRunHistory(fluent.models.ExportExecutionListResultInner)` was added

#### `models.ReportConfigFilter` was modified

* `withDimension(models.ReportConfigComparisonExpression)` was added
* `dimension()` was added
* `tag()` was added
* `withTag(models.ReportConfigComparisonExpression)` was added
* `not()` was added
* `withNot(models.ReportConfigFilter)` was added

#### `models.QueryComparisonExpression` was modified

* `withOperator(models.QueryOperatorType)` was added

## 1.0.0-beta.3 (2021-07-29)

- Azure Resource Manager CostManagement client library for Java. This package contains Microsoft Azure SDK for CostManagement Management SDK.  Package tag package-2019-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ExportProperties` was removed

#### `models.View$Update` was modified

* `withTypePropertiesQueryType(models.ReportType)` was removed

#### `models.View` was modified

* `typePropertiesQueryType()` was removed

#### `models.View$Definition` was modified

* `withTypePropertiesQueryType(models.ReportType)` was removed

### Features Added

* `models.CacheItem` was added

* `models.SettingsPropertiesStartOn` was added

#### `models.View$Update` was modified

* `withTypePropertiesType(models.ReportType)` was added

#### `models.View` was modified

* `typePropertiesType()` was added

#### `CostManagementManager$Configurable` was modified

* `withScope(java.lang.String)` was added

#### `models.Setting` was modified

* `startOn()` was added
* `cache()` was added

#### `models.View$Definition` was modified

* `withTypePropertiesType(models.ReportType)` was added

## 1.0.0-beta.2 (2021-04-07)

- Azure Resource Manager CostManagement client library for Java. This package contains Microsoft Azure SDK for CostManagement Management SDK.  Package tag package-2019-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Change

* `models.ReportConfigFilterAutoGenerated` was removed

* `models.ExportTimePeriod` was removed

* `models.ExportDatasetConfiguration` was removed

* `models.ForecastDataset` was removed

* `models.ReportConfigDatasetAutoGenerated` was removed

* `models.ExportDataset` was removed

#### `models.View$Update` was modified

* `withDataset(models.ReportConfigDataset)` was removed

#### `models.View` was modified

* `dataset()` was removed

#### `models.Export` was modified

* `nextRunTimeEstimate()` was removed
* `runHistory()` was removed

#### `models.ExportExecution` was modified

* `models.ExecutionType executionType()` -> `models.ExecutionType executionType()`
* `models.CommonExportProperties runSettings()` -> `models.CommonExportProperties runSettings()`
* `java.lang.String fileName()` -> `java.lang.String fileName()`
* `java.lang.String submittedBy()` -> `java.lang.String submittedBy()`
* `etag()` was removed
* `java.time.OffsetDateTime processingStartTime()` -> `java.time.OffsetDateTime processingStartTime()`
* `models.ExecutionStatus status()` -> `models.ExecutionStatus status()`
* `type()` was removed
* `error()` was removed
* `java.time.OffsetDateTime submittedTime()` -> `java.time.OffsetDateTime submittedTime()`
* `name()` was removed
* `id()` was removed
* `java.time.OffsetDateTime processingEndTime()` -> `java.time.OffsetDateTime processingEndTime()`
* `innerModel()` was removed

#### `models.ForecastDefinition` was modified

* `models.ForecastDataset dataset()` -> `models.QueryDataset dataset()`
* `withDataset(models.ForecastDataset)` was removed

#### `models.QueryFilter` was modified

* `dimension()` was removed
* `withTag(models.QueryComparisonExpression)` was removed
* `withNot(models.QueryFilter)` was removed
* `tag()` was removed
* `withDimension(models.QueryComparisonExpression)` was removed
* `not()` was removed

#### `models.Export$Update` was modified

* `withRunHistory(fluent.models.ExportExecutionListResultInner)` was removed

#### `models.ExportDefinition` was modified

* `withDataSet(models.ExportDataset)` was removed
* `withTimePeriod(models.ExportTimePeriod)` was removed
* `models.ExportTimePeriod timePeriod()` -> `models.QueryTimePeriod timePeriod()`
* `models.ExportDataset dataSet()` -> `models.QueryDatasetAutoGenerated dataSet()`

#### `models.View$Definition` was modified

* `withDataset(models.ReportConfigDataset)` was removed

#### `models.CommonExportProperties` was modified

* `models.ExportDefinition definition()` -> `models.ExportDefinition definition()`
* `innerModel()` was removed
* `runHistory()` was removed
* `models.ExportDeliveryInfo deliveryInfo()` -> `models.ExportDeliveryInfo deliveryInfo()`
* `nextRunTimeEstimate()` was removed
* `models.FormatType format()` -> `models.FormatType format()`

#### `models.Exports` was modified

* `listWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `getByIdWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Export$Definition` was modified

* `withRunHistory(fluent.models.ExportExecutionListResultInner)` was removed

#### `models.ReportConfigFilter` was modified

* `withNot(models.ReportConfigFilter)` was removed
* `dimension()` was removed
* `tag()` was removed
* `not()` was removed
* `withDimension(models.ReportConfigComparisonExpression)` was removed
* `withTag(models.ReportConfigComparisonExpression)` was removed

#### `models.ExportProperties` was modified

* `withRunHistory(fluent.models.ExportExecutionListResultInner)` was removed
* `fluent.models.CommonExportPropertiesInner withFormat(models.FormatType)` -> `models.CommonExportProperties withFormat(models.FormatType)`
* `fluent.models.CommonExportPropertiesInner withDefinition(models.ExportDefinition)` -> `models.CommonExportProperties withDefinition(models.ExportDefinition)`
* `fluent.models.CommonExportPropertiesInner withDeliveryInfo(models.ExportDeliveryInfo)` -> `models.CommonExportProperties withDeliveryInfo(models.ExportDeliveryInfo)`
* `withRunHistory(fluent.models.ExportExecutionListResultInner)` was removed

### New Feature

* `models.GenerateReservationDetailsReports` was added

* `models.GenerateReservationDetailsReportsByBillingProfileIdHeaders` was added

* `models.GenerateReservationDetailsReportsByBillingAccountIdResponse` was added

* `models.Status` was added

* `models.Setting` was added

* `models.GenerateReservationDetailsReportsByBillingAccountIdHeaders` was added

* `models.OperationStatusType` was added

* `models.QueryFilterAutoGenerated` was added

* `models.ProxySettingResource` was added

* `models.GenerateReservationDetailsReportsByBillingProfileIdResponse` was added

* `models.Settings` was added

* `models.SettingsListResult` was added

* `models.OperationStatus` was added

* `models.QueryDatasetAutoGenerated` was added

#### `CostManagementManager` was modified

* `settings()` was added
* `generateReservationDetailsReports()` was added

#### `models.View$Update` was modified

* `withDataSet(models.ReportConfigDataset)` was added

#### `models.View` was modified

* `currency()` was added
* `includeMonetaryCommitment()` was added
* `dataSet()` was added
* `dateRange()` was added

#### `models.Export` was modified

* `executeWithResponse(com.azure.core.util.Context)` was added
* `execute()` was added

#### `models.ExportExecution` was modified

* `withSubmittedBy(java.lang.String)` was added
* `tags()` was added
* `withSubmittedTime(java.time.OffsetDateTime)` was added
* `withExecutionType(models.ExecutionType)` was added
* `withRunSettings(models.CommonExportProperties)` was added
* `validate()` was added
* `withStatus(models.ExecutionStatus)` was added
* `withProcessingStartTime(java.time.OffsetDateTime)` was added
* `withFileName(java.lang.String)` was added
* `withProcessingEndTime(java.time.OffsetDateTime)` was added

#### `models.ForecastDefinition` was modified

* `withDataset(models.QueryDataset)` was added

#### `models.QueryFilter` was modified

* `withTags(models.QueryComparisonExpression)` was added
* `dimensions()` was added
* `withDimensions(models.QueryComparisonExpression)` was added
* `tags()` was added

#### `models.ExportDefinition` was modified

* `withTimePeriod(models.QueryTimePeriod)` was added
* `withDataSet(models.QueryDatasetAutoGenerated)` was added

#### `models.View$Definition` was modified

* `withDataSet(models.ReportConfigDataset)` was added

#### `models.CommonExportProperties` was modified

* `validate()` was added
* `withDeliveryInfo(models.ExportDeliveryInfo)` was added
* `withFormat(models.FormatType)` was added
* `withDefinition(models.ExportDefinition)` was added

#### `models.Exports` was modified

* `getWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `listWithResponse(java.lang.String,com.azure.core.util.Context)` was added

#### `models.ReportConfigFilter` was modified

* `tagKey()` was added
* `dimensions()` was added
* `withDimensions(models.ReportConfigComparisonExpression)` was added
* `tags()` was added
* `withTagKey(models.ReportConfigComparisonExpression)` was added
* `tagValue()` was added
* `withTags(models.ReportConfigComparisonExpression)` was added
* `withTagValue(models.ReportConfigComparisonExpression)` was added

#### `models.QueryResult` was modified

* `sku()` was added
* `etag()` was added
* `location()` was added

## 1.0.0-beta.1 (2020-12-18)

- Azure Resource Manager CostManagement client library for Java. This package contains Microsoft Azure SDK for CostManagement Management SDK.  Package tag package-2020-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

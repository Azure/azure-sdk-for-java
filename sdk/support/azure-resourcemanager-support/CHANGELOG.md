# Release History

## 1.1.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0 (2024-04-23)

- Azure Resource Manager support client library for Java. This package contains Microsoft Azure SDK for support Management SDK. Microsoft Azure Support Resource Provider. Package tag package-2024-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.0.0-beta.4 (2024-03-20)

- Azure Resource Manager support client library for Java. This package contains Microsoft Azure SDK for support Management SDK. Microsoft Azure Support Resource Provider. Package tag package-preview-2023-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.SupportTicketChatTranscriptsNoSubscriptions` was removed

* `models.SupportTicketCommunicationsNoSubscriptions` was removed

#### `models.FileDetails` was modified

* `java.lang.Float chunkSize()` -> `java.lang.Integer chunkSize()`
* `java.lang.Float numberOfChunks()` -> `java.lang.Integer numberOfChunks()`
* `java.lang.Float fileSize()` -> `java.lang.Integer fileSize()`

#### `models.FileDetails$Definition` was modified

* `withChunkSize(java.lang.Float)` was removed
* `withNumberOfChunks(java.lang.Float)` was removed
* `withFileSize(java.lang.Float)` was removed

#### `SupportManager` was modified

* `supportTicketChatTranscriptsNoSubscriptions()` was removed
* `supportTicketCommunicationsNoSubscriptions()` was removed

#### `models.UploadFile` was modified

* `java.lang.Float chunkIndex()` -> `java.lang.Integer chunkIndex()`
* `withChunkIndex(java.lang.Float)` was removed

### Features Added

* `models.ServiceClassifications` was added

* `models.LookUpResourceIdRequest` was added

* `models.ProblemClassificationsNoSubscriptions` was added

* `models.ServiceClassificationRequest` was added

* `models.IsTemporaryTicket` was added

* `models.ProblemClassificationsClassificationOutput` was added

* `models.LookUpResourceIds` was added

* `models.ServiceClassificationOutput` was added

* `models.ProblemClassificationsClassificationResult` was added

* `models.LookUpResourceIdResponse` was added

* `models.ServiceClassificationAnswer` was added

* `models.ServiceClassificationsNoSubscriptions` was added

* `models.ProblemClassificationProperties` was added

* `models.ProblemClassificationsClassificationInput` was added

* `models.ResourceType` was added

#### `models.ProblemClassification` was modified

* `metadata()` was added
* `parentProblemClassification()` was added

#### `models.ProblemClassifications` was modified

* `classifyProblems(java.lang.String,models.ProblemClassificationsClassificationInput)` was added
* `classifyProblemsWithResponse(java.lang.String,models.ProblemClassificationsClassificationInput,com.azure.core.util.Context)` was added

#### `models.Service` was modified

* `metadata()` was added

#### `models.FileDetails$Definition` was modified

* `withFileSize(java.lang.Integer)` was added
* `withChunkSize(java.lang.Integer)` was added
* `withNumberOfChunks(java.lang.Integer)` was added

#### `SupportManager` was modified

* `serviceClassificationsNoSubscriptions()` was added
* `problemClassificationsNoSubscriptions()` was added
* `lookUpResourceIds()` was added
* `serviceClassifications()` was added

#### `models.ChatTranscriptsNoSubscriptions` was modified

* `list(java.lang.String)` was added
* `list(java.lang.String,com.azure.core.util.Context)` was added

#### `models.UploadFile` was modified

* `withChunkIndex(java.lang.Integer)` was added

#### `models.CommunicationsNoSubscriptions` was modified

* `list(java.lang.String)` was added
* `list(java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

#### `models.SupportTicketDetails` was modified

* `isTemporaryTicket()` was added

#### `models.SupportTicketDetails$Definition` was modified

* `withEnrollmentId(java.lang.String)` was added

## 1.0.0-beta.3 (2023-10-23)

- Azure Resource Manager support client library for Java. This package contains Microsoft Azure SDK for support Management SDK. Microsoft Azure Support Resource Provider. Package tag package-preview-2022-09. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.UserConsent` was added

* `models.ChatTranscriptsListResult` was added

* `models.FileDetails` was added

* `models.ChatTranscriptDetails` was added

* `models.FilesListResult` was added

* `models.ChatTranscripts` was added

* `models.SupportTicketChatTranscriptsNoSubscriptions` was added

* `models.FileWorkspacesNoSubscriptions` was added

* `models.SupportTicketsNoSubscriptions` was added

* `models.FilesNoSubscriptions` was added

* `models.FileWorkspaceDetails` was added

* `models.SupportTicketCommunicationsNoSubscriptions` was added

* `models.MessageProperties` was added

* `models.SecondaryConsent` was added

* `models.FileDetails$Definition` was added

* `models.FileWorkspaces` was added

* `models.TranscriptContentType` was added

* `models.ChatTranscriptsNoSubscriptions` was added

* `models.SecondaryConsentEnabled` was added

* `models.UploadFile` was added

* `models.FileDetails$DefinitionStages` was added

* `models.CommunicationsNoSubscriptions` was added

* `models.Consent` was added

* `models.Files` was added

#### `models.ProblemClassification` was modified

* `secondaryConsentEnabled()` was added

#### `models.UpdateSupportTicket` was modified

* `secondaryConsent()` was added
* `withAdvancedDiagnosticConsent(models.Consent)` was added
* `withSecondaryConsent(java.util.List)` was added
* `advancedDiagnosticConsent()` was added

#### `SupportManager` was modified

* `files()` was added
* `fileWorkspacesNoSubscriptions()` was added
* `chatTranscriptsNoSubscriptions()` was added
* `supportTicketCommunicationsNoSubscriptions()` was added
* `fileWorkspaces()` was added
* `supportTicketChatTranscriptsNoSubscriptions()` was added
* `supportTicketsNoSubscriptions()` was added
* `communicationsNoSubscriptions()` was added
* `chatTranscripts()` was added
* `filesNoSubscriptions()` was added

#### `models.SupportTicketDetails` was modified

* `secondaryConsent()` was added
* `supportPlanId()` was added
* `supportPlanDisplayName()` was added
* `problemScopingQuestions()` was added
* `fileWorkspaceName()` was added
* `advancedDiagnosticConsent()` was added

#### `models.SupportTicketDetails$Definition` was modified

* `withFileWorkspaceName(java.lang.String)` was added
* `withAdvancedDiagnosticConsent(models.Consent)` was added
* `withSupportPlanId(java.lang.String)` was added
* `withProblemScopingQuestions(java.lang.String)` was added
* `withSecondaryConsent(java.util.List)` was added

#### `models.SupportTicketDetails$Update` was modified

* `withAdvancedDiagnosticConsent(models.Consent)` was added
* `withSecondaryConsent(java.util.List)` was added

## 1.0.0-beta.2 (2023-01-19)

- Azure Resource Manager support client library for Java. This package contains Microsoft Azure SDK for support Management SDK. Microsoft Azure Support Resource Provider. Package tag package-2020-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `SupportManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added
* `withScope(java.lang.String)` was added

#### `SupportManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

## 1.0.0-beta.1 (2021-04-19)

- Azure Resource Manager Support client library for Java. This package contains Microsoft Azure SDK for Support Management SDK. Microsoft Azure Support Resource Provider. Package tag package-2020-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
